package net.flectone.pulse.data.database;


import com.alessiodp.libby.Library;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
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
import net.flectone.pulse.processing.processor.YamlFileProcessor;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.creator.BackupCreator;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Database {

    private final FileResolver fileResolver;
    private final @Named("projectPath") Path projectPath;
    private final SystemVariableResolver systemVariableResolver;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;
    private final PacketProvider packetProvider;
    private final ReflectionResolver reflectionResolver;
    private final Provider<VersionDAO> versionDAOProvider;
    private final YamlFileProcessor yamlFileProcessor;
    private final BackupCreator backupCreator;

    private HikariDataSource dataSource;
    private Jdbi jdbi;

    public Config.Database config() {
        return fileResolver.getConfig().getDatabase();
    }

    public void connect() throws IOException {
        if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)
                && config().getType() == Type.SQLITE) {
            fLogger.warning("SQLite database is not supported on this version of Minecraft, H2 Database will be used");

            config().setType(Type.H2);
            yamlFileProcessor.save(fileResolver.getConfig());
        }

        downloadDriver();

        HikariConfig hikariConfig = createHikariConfig();

        dataSource = new HikariDataSource(hikariConfig);
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        setupTemplateEngine();

        jdbi.registerRowMapper(ConstructorMapper.factory(FColor.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(FPlayerDAO.PlayerInfo.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Ignore.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Mail.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Moderation.class));

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

    private void setupTemplateEngine() {
        BiFunction<String, StatementContext, String> template = null;
        if (StringUtils.isNotEmpty(config().getPrefix())) {
            template = (sql, ctx) -> Strings.CS.replace(sql, "fp_", config().getPrefix());
        }

        if (config().getType() == Type.POSTGRESQL) {
            if (template == null) {
                template = (sql, ctx) -> sql;
            }

            template = template.andThen(sql -> Strings.CS.replace(sql, "`", "\""));
        }

        if (template != null) {
            jdbi.getConfig(SqlStatements.class).setTemplateEngine(template::apply);
        }
    }

    private HikariConfig createHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();

        String connectionURL = "jdbc:" + config().getType().name().toLowerCase() + ":";
        switch (config().getType()) {
            case POSTGRESQL -> {
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
                connectionURL = connectionURL +
                        "file:./" + projectPath.toString() +
                        File.separator +
                        systemVariableResolver.substituteEnvVars(config().getName()) + ".h2" +
                        ";TRACE_LEVEL_FILE=0;DB_CLOSE_DELAY=-1;MODE=MySQL";

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
            case MYSQL, MARIADB -> {
                if (config().getType() == Type.MARIADB) {
                    hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
                }

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

        backupCreator.backup(config());

        VersionDAO versionDAO = versionDAOProvider.get();
        Optional<String> versionName = versionDAO.find();

        if (versionName.isEmpty() && fileResolver.isVersionOlderThan(fileResolver.getPreInitVersion(), "1.3.0")) {
            migration("1_3_0");
        }

        if (versionName.isEmpty() && fileResolver.isVersionOlderThan(fileResolver.getPreInitVersion(), "1.6.0")) {
            if (config().getType() == Type.POSTGRESQL) {
                migration("1_6_0_postgre");
            } else {
                migration("1_6_0");
            }
        }

        versionDAO.insertOrUpdate(fileResolver.getConfig().getVersion());
    }

    private void migration(String version) {
        try {
            InputStream sqlFile = platformServerAdapter.getResource("sqls/migrations/" + version + ".sql");
            executeSQLFile(sqlFile);
        } catch (IOException e) {
            fLogger.warning(e);
        }
    }

    public void downloadDriver() {
        boolean needChecking = !config().isIgnoreExistingDriver();
        switch (config().getType()) {
            case POSTGRESQL -> reflectionResolver.hasClassOrElse("org.postgresql.Driver", needChecking, libraryResolver ->
                    libraryResolver.loadLibrary(Library.builder()
                            .groupId("org{}postgresql")
                            .artifactId("postgresql")
                            .version(BuildConfig.POSTGRESQL_VERSION)
                            .repository(BuildConfig.MAVEN_REPOSITORY)
                            .resolveTransitiveDependencies(true)
                            .build()
                    )
            );
            case H2 -> reflectionResolver.hasClassOrElse("org.h2.Driver", needChecking, libraryResolver ->
                    libraryResolver.loadLibrary(Library.builder()
                            .groupId("com{}h2database")
                            .artifactId("h2")
                            .version(BuildConfig.H2_VERSION)
                            .repository(BuildConfig.MAVEN_REPOSITORY)
                            .resolveTransitiveDependencies(true)
                            .build()
                    )
            );
            case SQLITE -> reflectionResolver.hasClassOrElse("org.sqlite.JDBC", needChecking, libraryResolver ->
                    libraryResolver.loadLibrary(Library.builder()
                            .groupId("org{}xerial")
                            .artifactId("sqlite-jdbc")
                            .version(BuildConfig.SQLITE_JDBC_VERSION)
                            .repository(BuildConfig.MAVEN_REPOSITORY)
                            .resolveTransitiveDependencies(true)
                            .build()
                    )
            );
            case MARIADB -> reflectionResolver.hasClassOrElse("com.mysql.jdbc.Driver", needChecking, libraryResolver ->
                    libraryResolver.loadLibrary(Library.builder()
                            .groupId("com{}mysql")
                            .artifactId("mysql-connector-j")
                            .version(BuildConfig.MYSQL_CONNECTOR_VERSION)
                            .repository(BuildConfig.MAVEN_REPOSITORY)
                            .resolveTransitiveDependencies(true)
                            .build()
                    )
            );
            case MYSQL -> reflectionResolver.hasClassOrElse("org.mariadb.jdbc.Driver", needChecking, libraryResolver ->
                    libraryResolver.loadLibrary(Library.builder()
                            .groupId("org{}mariadb{}jdbc")
                            .artifactId("mariadb-java-client")
                            .version(BuildConfig.MARIADB_JAVA_CLIENT_VERSION)
                            .repository(BuildConfig.MAVEN_REPOSITORY)
                            .resolveTransitiveDependencies(true)
                            .build()
                    )
            );
        }
    }

    public enum Type {
        POSTGRESQL,
        H2,
        SQLITE,
        MYSQL,
        MARIADB
    }
}
