package net.flectone.pulse.persistence.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.constant.DatabaseType;
import net.flectone.pulse.exception.DatabaseMigrationException;
import net.flectone.pulse.exception.DatabaseNotInitializedException;
import net.flectone.pulse.exception.UnsupportedDatabaseOperationException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.model.value.PlayTime;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.persistence.database.dao.FColorDao;
import net.flectone.pulse.persistence.database.dao.FPlayerDAO;
import net.flectone.pulse.persistence.database.dao.VersionDAO;
import net.flectone.pulse.persistence.database.driver.DriverBackedDataSource;
import net.flectone.pulse.persistence.database.migration.H2StoreMigrator;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.resolver.SystemVariableResolver;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.backup.BackupCreator;
import net.flectone.pulse.util.loader.DriverLoader;
import net.flectone.pulse.util.version.VersionComparator;
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

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DatabaseImpl implements Database {

    private final FileFacade fileFacade;
    private final VersionComparator versionComparator;
    private final @Named("projectPath") Path projectPath;
    private final SystemVariableResolver systemVariableResolver;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;
    private final LazyInstance<VersionDAO> versionDAO;
    private final BackupCreator backupCreator;
    private final DriverLoader driverLoader;
    private final H2StoreMigrator h2StoreMigrator;

    @Nullable private volatile HikariDataSource dataSource;
    @Nullable private volatile Jdbi jdbi;

    @Override
    public Config.Database config() {
        return fileFacade.config().database();
    }

    @Override
    public void connect() throws IOException {
        Driver driver = driverLoader.getOrLoad(config().type());

        HikariDataSource newDataSource = createDataSource(driver);

        try {
            Jdbi newJdbi = Jdbi.create(newDataSource);
            newJdbi.installPlugin(new SqlObjectPlugin());

            setupTemplateEngine(newJdbi);

            newJdbi.registerRowMapper(ConstructorMapper.factory(FColorDao.FColorInfo.class));
            newJdbi.registerRowMapper(ConstructorMapper.factory(FPlayerDAO.PlayerInfo.class));
            // these models are public api and carry no JDBI annotations, so the column mapping lives here
            newJdbi.registerRowMapper(Ignore.class, (rs, _) -> new Ignore(
                    rs.getInt("id"),
                    rs.getLong("date"),
                    rs.getInt("target")
            ));
            newJdbi.registerRowMapper(Mail.class, (rs, _) -> new Mail(
                    rs.getInt("id"),
                    rs.getLong("date"),
                    rs.getInt("sender"),
                    rs.getInt("receiver"),
                    rs.getString("message")
            ));
            newJdbi.registerRowMapper(Moderation.class, (rs, _) -> new Moderation(
                    rs.getInt("id"),
                    rs.getInt("player"),
                    rs.getLong("date"),
                    rs.getLong("time"),
                    rs.getString("reason"),
                    rs.getInt("moderator"),
                    Moderation.Type.valueOf(rs.getString("type").toUpperCase()),
                    rs.getBoolean("valid"),
                    rs.getString("server")
            ));
            newJdbi.registerRowMapper(PlayTime.class, (rs, _) -> new PlayTime(
                    rs.getInt("id"),
                    rs.getInt("player"),
                    rs.getLong("first"),
                    rs.getLong("last"),
                    rs.getLong("total"),
                    rs.getInt("sessions")
            ));

            this.dataSource = newDataSource;
            this.jdbi = newJdbi;

            executeInitSQLDatabaseFile();

            checkMigration();
        } catch (Exception e) {
            this.dataSource = null;
            this.jdbi = null;

            newDataSource.close();

            throw e;
        }

        init();
    }

    public Jdbi getJdbi() {
        if (jdbi == null) throw new DatabaseNotInitializedException();

        return jdbi;
    }

    @Override
    public void init() {
        fLogger.info("[+] Database connected: %s", config().type());
    }

    @Override
    public void disconnect() {
        HikariDataSource hikariDataSource = dataSource;
        if (hikariDataSource != null) {
            this.dataSource = null;
            this.jdbi = null;

            hikariDataSource.close();

            fLogger.info("[-] Database disconnected");
        }
    }

    private void setupTemplateEngine(Jdbi jdbi) {
        BiFunction<String, StatementContext, String> template = null;
        if (StringUtils.isNotEmpty(config().prefix())) {
            template = (sql, _) -> Strings.CS.replace(sql, "fp_", config().prefix());
        }

        if (config().type() == DatabaseType.POSTGRESQL) {
            if (template == null) {
                template = (sql, _) -> sql;
            }

            template = template.andThen(sql -> Strings.CS.replace(sql, "`", "\""));
        }

        if (template != null) {
            jdbi.getConfig(SqlStatements.class).setTemplateEngine(template::apply);
        }
    }

    @Override
    public boolean isClosed() {
        HikariDataSource hikariDataSource = dataSource;
        return hikariDataSource == null || hikariDataSource.isClosed();
    }

    private HikariDataSource createDataSource(Driver driver) {
        try {
            return new HikariDataSource(createHikariConfig(driver));
        } catch (RuntimeException e) {
            // servers that once ran with another plugin's older h2 on the class path are left with a
            // file the current h2 refuses to open, it has to be rewritten before the pool can start
            if (config().type() == DatabaseType.H2) {
                try {
                    if (h2StoreMigrator.migrate(h2DatabaseFile(), h2ConnectionURL(), driver)) {
                        return new HikariDataSource(createHikariConfig(driver));
                    }
                } catch (DatabaseMigrationException migrationException) {
                    migrationException.addSuppressed(e);

                    throw migrationException;
                }
            }

            throw e;
        }
    }

    private String h2ConnectionURL() {
        return "jdbc:h2:file:./" +
                projectPath +
                File.separator +
                systemVariableResolver.substituteEnvVars(config().name()) + ".h2" +
                ";TRACE_LEVEL_FILE=0;DB_CLOSE_DELAY=-1;MODE=MySQL";
    }

    private Path h2DatabaseFile() {
        return projectPath.resolve(systemVariableResolver.substituteEnvVars(config().name()) + ".h2.mv.db");
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
                connectionURL = h2ConnectionURL();

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
            default -> throw new UnsupportedDatabaseOperationException();
        }

        Config.Database.PoolSettings poolSettings = config().poolSettings();
        hikariConfig.setMaximumPoolSize(poolSettings.maxSize());
        hikariConfig.setMinimumIdle(poolSettings.minIdle());
        hikariConfig.setMaxLifetime(poolSettings.maxLifetime());
        hikariConfig.setKeepaliveTime(poolSettings.keepaliveTime());
        hikariConfig.setConnectionTimeout(poolSettings.connectionTimeout());

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
        VersionDAO versionDAOInstance = versionDAO.get();

        if (versionComparator.isOlderThan(fileFacade.getPreInitVersion(), fileFacade.config().version())) {
            backupCreator.backup(fileFacade.getPreInitVersion(), config());

            Optional<String> versionName = versionDAOInstance.find();

            String oldDatabaseVersion = versionName.orElse(null);

            Predicate<String> versionTest = version -> {
                if (StringUtils.isEmpty(oldDatabaseVersion)) return true;

                return versionComparator.isOlderThan(fileFacade.getPreInitVersion(), version)
                        && versionComparator.isOlderThan(oldDatabaseVersion, version, false);
            };
        }

        // always update to latest version
        versionDAOInstance.insertOrUpdate(fileFacade.config().version());
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

}