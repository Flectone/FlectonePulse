package net.flectone.pulse.data.database;


import com.alessiodp.libby.Library;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.dao.FPlayerDAO;
import net.flectone.pulse.data.database.dao.VersionDAO;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Singleton
public class Database {

    private final FileResolver fileResolver;
    private final Path projectPath;
    private final SystemVariableResolver systemVariableResolver;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;
    private final PacketProvider packetProvider;
    private final ReflectionResolver reflectionResolver;
    private final Provider<VersionDAO> versionDAOProvider;

    private HikariDataSource dataSource;
    private Jdbi jdbi;

    @Inject
    public Database(FileResolver fileResolver,
                    @Named("projectPath") Path projectPath,
                    SystemVariableResolver systemVariableResolver,
                    PlatformServerAdapter platformServerAdapter,
                    FLogger fLogger,
                    PacketProvider packetProvider,
                    ReflectionResolver reflectionResolver,
                    Provider<VersionDAO> versionDAOProvider) {
        this.fileResolver = fileResolver;
        this.projectPath = projectPath;
        this.systemVariableResolver = systemVariableResolver;
        this.platformServerAdapter = platformServerAdapter;
        this.fLogger = fLogger;
        this.packetProvider = packetProvider;
        this.reflectionResolver = reflectionResolver;
        this.versionDAOProvider = versionDAOProvider;
    }

    public Config.Database config() {
        return fileResolver.getConfig().getDatabase();
    }

    public void connect() throws IOException {
        if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_10_2)
                && config().getType() == Type.SQLITE) {
            fLogger.warning("SQLite database is not supported on this version of Minecraft");
            fLogger.warning("H2 Database will be used");
            config().setType(Type.H2);
        }

        HikariConfig hikariConfig = createHikaryConfig();

        try {
            dataSource = new HikariDataSource(hikariConfig);
            jdbi = Jdbi.create(dataSource);
            jdbi.installPlugin(new SqlObjectPlugin());

            if (config().getType() == Type.POSTGRESQL) {
                jdbi.getConfig(SqlStatements.class).setTemplateEngine((sql, ctx) ->
                        Strings.CS.replace(sql, "`", "\"")
                );
            }

            jdbi.registerRowMapper(ConstructorMapper.factory(FColor.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(FPlayerDAO.PlayerInfo.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(Ignore.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(Mail.class));
            jdbi.registerRowMapper(ConstructorMapper.factory(Moderation.class));
        } catch (HikariPool.PoolInitializationException e) {
            throw new RuntimeException(e);
        }

        executeSQLFile(platformServerAdapter.getResource("sqls/" + config().getType().name().toLowerCase() + ".sql"));

        checkMigration();

        init();
    }

    @NotNull
    public Jdbi getJdbi() throws IllegalStateException {
        if (jdbi == null) throw new IllegalStateException("JDBI not initialized");

        return jdbi;
    }

    public void init() {
        fLogger.info(config().getType() + " database connected");
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.getHikariPoolMXBean().softEvictConnections();
            dataSource.close();

            fLogger.info("Database disconnected");
        }
    }

    private HikariConfig createHikaryConfig() {
        HikariConfig hikariConfig = new HikariConfig();

        String connectionURL = "jdbc:" + config().getType().name().toLowerCase() + ":";
        switch (config().getType()) {
            case POSTGRESQL -> {
                reflectionResolver.hasClassOrElse("org.postgresql.Driver", libraryResolver ->
                        libraryResolver.loadLibrary(Library.builder()
                                .groupId("org{}postgresql")
                                .artifactId("postgresql")
                                .version(BuildConfig.POSTGRESQL_VERSION)
                                .repository(BuildConfig.MAVEN_REPOSITORY)
                                .build()
                        )
                );

                connectionURL = connectionURL +
                        "//" +
                        systemVariableResolver.substituteEnvVars(config().getHost()) +
                        ":" +
                        systemVariableResolver.substituteEnvVars(config().getPort()) +
                        "/" +
                        systemVariableResolver.substituteEnvVars(config().getName()) +
                        config().getParameters();

                hikariConfig.setDriverClassName("org.postgresql.Driver");
                hikariConfig.setUsername(systemVariableResolver.substituteEnvVars(config().getUser()));
                hikariConfig.setPassword(systemVariableResolver.substituteEnvVars(config().getPassword()));
                hikariConfig.setMaximumPoolSize(8);
                hikariConfig.setMinimumIdle(2);
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
            }
            case H2 -> {
                reflectionResolver.hasClassOrElse("org.h2.Driver", libraryResolver ->
                        libraryResolver.loadLibrary(Library.builder()
                                .groupId("com{}h2database")
                                .artifactId("h2")
                                .version(BuildConfig.H2_VERSION)
                                .repository(BuildConfig.MAVEN_REPOSITORY)
                                .build()
                        )
                );

                connectionURL = connectionURL +
                        "file:./" + projectPath.toString() +
                        File.separator +
                        systemVariableResolver.substituteEnvVars(config().getName()) + ".h2" +
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
                        systemVariableResolver.substituteEnvVars(config().getName()) +
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
                reflectionResolver.hasClassOrElse("com.mysql.jdbc.Driver", libraryResolver ->
                        libraryResolver.loadLibrary(Library.builder()
                                .groupId("com{}mysql")
                                .artifactId("mysql-connector-j")
                                .version(BuildConfig.MYSQL_CONNECTOR_VERSION)
                                .repository(BuildConfig.MAVEN_REPOSITORY)
                                .build()
                        )
                );

                connectionURL = connectionURL +
                        "//" +
                        systemVariableResolver.substituteEnvVars(config().getHost()) +
                        ":" +
                        systemVariableResolver.substituteEnvVars(config().getPort()) +
                        "/" +
                        systemVariableResolver.substituteEnvVars(config().getName()) +
                        config().getParameters();

                hikariConfig.setUsername(systemVariableResolver.substituteEnvVars(config().getUser()));
                hikariConfig.setPassword(systemVariableResolver.substituteEnvVars(config().getPassword()));
                hikariConfig.setMaximumPoolSize(8);
                hikariConfig.setMinimumIdle(2);
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "500");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            }
            default -> throw new IllegalStateException(config().getType() + " not supported");
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

    private void checkMigration() {
        if (!fileResolver.isVersionOlderThan(fileResolver.getPreInitVersion(), fileResolver.getConfig().getVersion())) return;

        VersionDAO versionDAO = versionDAOProvider.get();
        Optional<String> versionName = versionDAO.find();

        if (versionName.isEmpty() && fileResolver.isVersionOlderThan(fileResolver.getPreInitVersion(), "1.3.0")) {
            migration("1_3_0");
        }

        if (versionName.isEmpty()) {
            migration("1_6_0");
        }

        versionDAO.insertOrUpdate(fileResolver.getConfig().getVersion());
    }

    private void migration(String version) {
        backupDatabase();

        try {
            InputStream sqlFile = platformServerAdapter.getResource("sqls/migrations/" + version + ".sql");
            executeSQLFile(sqlFile);
        } catch (IOException e) {
            fLogger.warning(e);
        }
    }

    private void backupDatabase() {
        if (config().getType() == Type.SQLITE) {
            String databaseName = systemVariableResolver.substituteEnvVars(config().getName()) + ".db";

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String copiedDatabaseName = databaseName + "_backup_" + timeStamp;

            try {
                Files.copy(projectPath.resolve(databaseName), projectPath.resolve(copiedDatabaseName));
            } catch (IOException e) {
                fLogger.warning(e);
            }
        }
    }

    public enum Type {
        POSTGRESQL,
        H2,
        SQLITE,
        MYSQL
    }
}
