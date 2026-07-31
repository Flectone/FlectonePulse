package net.flectone.pulse.data.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.data.database.dao.FColorDao;
import net.flectone.pulse.data.database.dao.FPlayerDAO;
import net.flectone.pulse.data.database.dao.VersionDAO;
import net.flectone.pulse.data.database.driver.DriverBackedDataSource;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.comparator.VersionComparator;
import net.flectone.pulse.util.creator.BackupCreator;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.loader.DriverLoader;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Driver;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Database for FlectonePulse.
 * Handles database connection, configuration, and migrations.
 *
 * @author TheFaser
 * @since 0.0.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Database {

    private final FileFacade fileFacade;
    private final VersionComparator versionComparator;
    private final @Named("projectPath") Path projectPath;
    private final SystemVariableResolver systemVariableResolver;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;
    private final Provider<VersionDAO> versionDAOProvider;
    private final BackupCreator backupCreator;
    private final DriverLoader driverLoader;

    @Nullable private volatile HikariDataSource dataSource;
    @Nullable private Jdbi jdbi;

    /**
     * Gets the database configuration.
     *
     * @return the database configuration
     */
    public Config.Database config() {
        return fileFacade.config().database();
    }

    /**
     * Connects to the database and initializes it.
     *
     * @throws IOException if connection fails
     */
    public void connect() throws IOException {
        Driver driver = driverLoader.getOrLoad(config().type());
        HikariConfig hikariConfig = createHikariConfig(driver);

        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        this.dataSource = hikariDataSource;

        jdbi = Jdbi.create(hikariDataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        setupTemplateEngine(jdbi);

        jdbi.registerRowMapper(ConstructorMapper.factory(FColorDao.FColorInfo.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(FPlayerDAO.PlayerInfo.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Ignore.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Mail.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Moderation.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(PlayTime.class));

        executeInitSQLDatabaseFile();

        checkMigration();

        init();
    }

    /**
     * Gets the JDBI instance.
     *
     * @return the JDBI instance
     * @throws IllegalStateException if JDBI is not initialized
     */
    public Jdbi getJdbi() throws IllegalStateException {
        if (jdbi == null) throw new IllegalStateException("JDBI not initialized");

        return jdbi;
    }

    /**
     * Initializes the database connection.
     */
    public void init() {
        fLogger.info("[+] Database connected: %s", config().type());
    }

    /**
     * Disconnects from the database.
     */
    public void disconnect() {
        HikariDataSource hikariDataSource = dataSource;
        if (hikariDataSource != null) {
            hikariDataSource.close();
            this.dataSource = null;

            fLogger.info("[-] Database disconnected");
        }
    }

    private void setupTemplateEngine(Jdbi jdbi) {
        BiFunction<String, StatementContext, String> template = null;
        if (StringUtils.isNotEmpty(config().prefix())) {
            template = (sql, _) -> Strings.CS.replace(sql, "fp_", config().prefix());
        }

        if (config().type() == Type.POSTGRESQL) {
            if (template == null) {
                template = (sql, _) -> sql;
            }

            template = template.andThen(sql -> Strings.CS.replace(sql, "`", "\""));
        }

        if (template != null) {
            jdbi.getConfig(SqlStatements.class).setTemplateEngine(template::apply);
        }
    }

    public boolean isClosed() {
        HikariDataSource hikariDataSource = dataSource;
        return hikariDataSource == null || hikariDataSource.isClosed();
    }

    private HikariConfig createHikariConfig(Driver driver) {
        HikariConfig hikariConfig = new HikariConfig();

        String connectionURL = "jdbc:" + config().type().name().toLowerCase() + ":";
        Properties properties = new Properties();

        switch (config().type()) {
            case POSTGRESQL -> {
                connectionURL = connectionURL +
                        "//" +
                        systemVariableResolver.substituteEnvVars(config().host()) +
                        ":" +
                        systemVariableResolver.substituteEnvVars(config().port()) +
                        "/" +
                        systemVariableResolver.substituteEnvVars(config().name()) +
                        config().parameters();

                properties.setProperty("user", systemVariableResolver.substituteEnvVars(config().user()));
                properties.setProperty("password", systemVariableResolver.substituteEnvVars(config().password()));
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            }
            case H2 -> {
                connectionURL = connectionURL +
                        "file:./" + projectPath.toString() +
                        File.separator +
                        systemVariableResolver.substituteEnvVars(config().name()) + ".h2" +
                        ";TRACE_LEVEL_FILE=0;DB_CLOSE_DELAY=-1;MODE=MySQL";

                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            }
            case SQLITE -> {
                connectionURL = connectionURL +
                        projectPath.toString() +
                        File.separator +
                        systemVariableResolver.substituteEnvVars(config().name()) +
                        ".db";

                hikariConfig.addDataSourceProperty("busy_timeout", 30000);
                hikariConfig.addDataSourceProperty("journal_mode", "WAL");
                hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
                hikariConfig.addDataSourceProperty("journal_size_limit", "6144000");
            }
            case MYSQL, MARIADB -> {
                connectionURL = connectionURL +
                        "//" +
                        systemVariableResolver.substituteEnvVars(config().host()) +
                        ":" +
                        systemVariableResolver.substituteEnvVars(config().port()) +
                        "/" +
                        systemVariableResolver.substituteEnvVars(config().name()) +
                        config().parameters();

                properties.setProperty("user", systemVariableResolver.substituteEnvVars(config().user()));
                properties.setProperty("password", systemVariableResolver.substituteEnvVars(config().password()));
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
                hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
                hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
                hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
                hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
                hikariConfig.addDataSourceProperty("alwaysSendSetIsolation", "false");
                hikariConfig.addDataSourceProperty("cacheCallableStmts", "true");
            }
            default -> throw new IllegalStateException(config().type() + " not supported");
        }

        hikariConfig.setDataSource(new DriverBackedDataSource(driver, connectionURL, properties));
        hikariConfig.setPoolName("FlectonePulseDatabase");

        return hikariConfig;
    }

    private void executeSQLFile(InputStream inputStream) throws IOException {
        String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        String[] statements = sql.split(";");

        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;

            String finalStatement = trimmed + ";";
            getJdbi().useHandle(handle -> {
                try {
                    handle.execute(finalStatement);
                } catch (Exception e) {
                    // skip migration errors
                    if (!e.getMessage().contains("Duplicate key")
                            && !e.getMessage().contains("already exists")
                            && !e.getMessage().contains("Incorrect prefix key")
                            && !e.getMessage().contains("key was too long")) {
                        throw e;
                    }
                }
            });
        }
    }

    private void checkMigration() throws IOException {
        VersionDAO versionDAO = versionDAOProvider.get();

        if (versionComparator.isOlderThan(fileFacade.getPreInitVersion(), fileFacade.config().version())) {
            backupCreator.backup(config());

            Optional<String> versionName = versionDAO.find();

            String oldDatabaseVersion = versionName.orElse(null);

            Predicate<String> versionTest = version -> {
                if (StringUtils.isEmpty(oldDatabaseVersion)) return true;

                return versionComparator.isOlderThan(fileFacade.getPreInitVersion(), version)
                        && versionComparator.isOlderThan(oldDatabaseVersion, version, false);
            };
        }

        // always update to latest version
        versionDAO.insertOrUpdate(fileFacade.config().version());
    }

    private void migration(String version) {
        try {
            InputStream sqlFile = platformServerAdapter.getResource("sqls/migrations/" + version + ".sql");
            executeSQLFile(sqlFile);
        } catch (IOException e) {
            fLogger.warning(e);
        }
    }

    private void executeInitSQLDatabaseFile() throws IOException {
        executeSQLFile(platformServerAdapter.getResource("sqls/" + config().type().name().toLowerCase() + ".sql"));
    }

    /**
     * Database types supported by FlectonePulse.
     */
    public enum Type {
        POSTGRESQL,
        H2,
        SQLITE,
        MYSQL,
        MARIADB
    }
}