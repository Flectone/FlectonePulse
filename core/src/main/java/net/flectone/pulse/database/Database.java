package net.flectone.pulse.database;


import com.alessiodp.libby.Library;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.dao.ColorsDAO;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ignore;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Singleton
public class Database {

    private final Config.Database config;

    private final Injector injector;
    private final FileResolver fileResolver;
    private final Path projectPath;
    private final SystemVariableResolver systemVariableResolver;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;
    private final PacketProvider packetProvider;
    private final LibraryResolver libraryResolver;

    private HikariDataSource dataSource;
    private Jdbi jdbi;

    @Inject
    public Database(FileResolver fileResolver,
                    Injector injector,
                    @Named("projectPath") Path projectPath,
                    SystemVariableResolver systemVariableResolver,
                    PlatformServerAdapter platformServerAdapter,
                    FLogger fLogger,
                    PacketProvider packetProvider,
                    LibraryResolver libraryResolver) {
        this.config = fileResolver.getConfig().getDatabase();
        this.injector = injector;
        this.fileResolver = fileResolver;
        this.projectPath = projectPath;
        this.systemVariableResolver = systemVariableResolver;
        this.platformServerAdapter = platformServerAdapter;
        this.fLogger = fLogger;
        this.packetProvider = packetProvider;
        this.libraryResolver = libraryResolver;
    }

    public void connect() throws IOException {
        if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_10_2)
                && config.getType() == Type.SQLITE) {
            fLogger.warning("SQLite database is not supported on this version of Minecraft");
            fLogger.warning("H2 Database will be used");
            config.setType(Type.H2);
        }

        HikariConfig hikariConfig = createHikaryConfig();

        try {
            dataSource = new HikariDataSource(hikariConfig);
            jdbi = Jdbi.create(dataSource);
            jdbi.installPlugin(new SqlObjectPlugin());
            jdbi.registerRowMapper(ConstructorMapper.factory(ColorsDAO.ColorEntry.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(FPlayerDAO.PlayerInfo.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(Ignore.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(Mail.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(Moderation.class));
        } catch (HikariPool.PoolInitializationException e) {
            throw new RuntimeException(e);
        }

        InputStream sqlFile = platformServerAdapter.getResource("sqls/" + config.getType().name().toLowerCase() + ".sql");
        executeSQLFile(sqlFile);

        if (fileResolver.isVersionOlderThan(fileResolver.getPreInitVersion(), "0.9.0")) {
            MIGRATION_0_9_0();
        }

        if (config.getType() == Type.SQLITE) {
            injector.getInstance(FPlayerDAO.class).updateAllToOffline();
        }

        init();
    }

    @NotNull
    public Jdbi getJdbi() throws IllegalStateException {
        if (jdbi == null) throw new IllegalStateException("JDBI not initialized");

        return jdbi;
    }

    public void init() {
        fLogger.info(config.getType() + " database connected");
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.getHikariPoolMXBean().softEvictConnections();
            dataSource.close();
        }

        fLogger.info(config.getType() + " database disconnected");
    }

    private HikariConfig createHikaryConfig() {
        HikariConfig hikariConfig = new HikariConfig();

        String connectionURL = "jdbc:" + config.getType().name().toLowerCase() + ":";
        switch (config.getType()) {
            case POSTGRESQL -> {
                setupPostgreSQLLibrary();

                connectionURL = connectionURL +
                        "postgresql://" +
                        systemVariableResolver.substituteEnvVars(config.getHost()) +
                        ":" +
                        systemVariableResolver.substituteEnvVars(config.getPort()) +
                        "/" +
                        systemVariableResolver.substituteEnvVars(config.getName()) +
                        config.getParameters();

                hikariConfig.setUsername(systemVariableResolver.substituteEnvVars(config.getUser()));
                hikariConfig.setPassword(systemVariableResolver.substituteEnvVars(config.getPassword()));
                hikariConfig.setMaximumPoolSize(8);
                hikariConfig.setMinimumIdle(2);
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
            }
            case H2 -> {
                setupH2Library();

                connectionURL = connectionURL +
                        "file:./" + projectPath.toString() +
                        File.separator +
                        systemVariableResolver.substituteEnvVars(config.getName()) + ".h2" +
                        ";DB_CLOSE_DELAY=-1;MODE=MySQL";

                hikariConfig.setDriverClassName("org.h2.Driver");
                hikariConfig.setMaximumPoolSize(5);
                hikariConfig.setMinimumIdle(1);
                hikariConfig.setConnectionTimeout(30000);
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
            }
            case SQLITE -> {
                connectionURL = connectionURL +
                        projectPath.toString() +
                        File.separator +
                        systemVariableResolver.substituteEnvVars(config.getName()) +
                        ".db";

                hikariConfig.setMaximumPoolSize(5);
                hikariConfig.setMinimumIdle(1);
                hikariConfig.setConnectionTimeout(30000);
                hikariConfig.addDataSourceProperty("busy_timeout", 30000);
                hikariConfig.addDataSourceProperty("journal_mode", "WAL");
                hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
                hikariConfig.addDataSourceProperty("journal_size_limit", "6144000");
            }
            case MYSQL -> {
                connectionURL = connectionURL +
                        "//" +
                        systemVariableResolver.substituteEnvVars(config.getHost()) +
                        ":" +
                        systemVariableResolver.substituteEnvVars(config.getPort()) +
                        "/" +
                        systemVariableResolver.substituteEnvVars(config.getName()) +
                        config.getParameters();

                hikariConfig.setUsername(systemVariableResolver.substituteEnvVars(config.getUser()));
                hikariConfig.setPassword(systemVariableResolver.substituteEnvVars(config.getPassword()));
                hikariConfig.setMaximumPoolSize(8);
                hikariConfig.setMinimumIdle(2);
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            }
            default -> throw new IllegalStateException(config.getType() + " not supported");
        }

        hikariConfig.setJdbcUrl(connectionURL);
        hikariConfig.setPoolName("FlectonePulseDatabase");

        return hikariConfig;
    }

    private void executeSQLFile(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("--")) continue;

            builder.append(line);

            if (line.endsWith(";")) {
                String sql = builder.toString();
                getJdbi().useHandle(handle -> handle.execute(sql));
                builder.setLength(0);
            }
        }
    }

    private void MIGRATION_0_9_0() {
        backupDatabase();

        SettingDAO settingDAO = injector.getInstance(SettingDAO.class);
        injector.getInstance(FPlayerDAO.class).getFPlayers().forEach(fPlayer -> {
            if (fPlayer.isUnknown()) return;

            fPlayer.setSetting(FPlayer.Setting.ANON);
            settingDAO.insertOrUpdate(fPlayer, FPlayer.Setting.ANON);
        });
    }

    private void backupDatabase() {
        if (config.getType() == Type.SQLITE) {
            String databaseName = systemVariableResolver.substituteEnvVars(config.getName()) + ".db";

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String copiedDatabaseName = databaseName + "_backup_" + timeStamp;

            try {
                Files.copy(projectPath.resolve(databaseName), projectPath.resolve(copiedDatabaseName));
            } catch (IOException e) {
                fLogger.warning(e);
            }
        }
    }

    private void setupH2Library() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ignored) {
            libraryResolver.loadLibrary(Library.builder()
                    .groupId("com{}h2database")
                    .artifactId("h2")
                    .version(BuildConfig.H2_VERSION)
                    .build()
            );
        }
    }

    private void setupPostgreSQLLibrary() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ignored) {
            libraryResolver.loadLibrary(Library.builder()
                    .groupId("org{}postgresql")
                    .artifactId("posrgresql")
                    .version(BuildConfig.POSTGRESQL_VERSION)
                    .build()
            );
        }
    }

    public enum Type {
        POSTGRESQL,
        H2,
        SQLITE,
        MYSQL;

        }
    }
}
