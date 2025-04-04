package net.flectone.pulse.database;


import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class Database {

    private final Config.Database config;

    private final Injector injector;
    private final FileManager fileManager;
    private final Path projectPath;
    private final InputStream SQLFile;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;

    private HikariDataSource dataSource;

    @Inject
    public Database(FileManager fileManager,
                    Injector injector,
                    @Named("projectPath") Path projectPath,
                    @Named("SQLFile") InputStream SQLFile,
                    SystemVariableResolver systemVariableResolver,
                    FLogger fLogger) {

        this.injector = injector;
        this.fileManager = fileManager;
        this.projectPath = projectPath;
        this.SQLFile = SQLFile;
        this.systemVariableResolver = systemVariableResolver;
        this.fLogger = fLogger;

        config = fileManager.getConfig().getDatabase();
    }

    public void connect() throws SQLException, IOException {
        HikariConfig hikariConfig = createHikaryConfig();

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            fLogger.warning("Failed to initialize Database. Check database settings");

            throw new RuntimeException(e);
        }

        try (Connection ignored = getConnection()){
            executeFile(SQLFile);

            if (fileManager.isOlderThan(fileManager.getPreInitVersion(), "0.6.0")) {
                MIGRATION_0_6_0();
            }

            if (config.getType() == Config.Database.Type.SQLITE) {
                injector.getInstance(FPlayerDAO.class).updateAllToOffline();
            }

            init();
        }
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("Not initialized");

        return dataSource.getConnection();
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
        }

        hikariConfig.setJdbcUrl(connectionURL);
        hikariConfig.setPoolName("FlectonePulseDatabase");

        return hikariConfig;
    }

    private void executeFile(InputStream inputStream) throws SQLException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            StringBuilder builder = new StringBuilder();

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("--")) continue;

                builder.append(line);

                if (line.endsWith(";")) {
                    statement.execute(builder.toString());
                    builder.setLength(0);
                }
            }
        }
    }

    private void MIGRATION_0_6_0() {
        if (config.getType() == Config.Database.Type.SQLITE) {
            String databaseName = systemVariableResolver.substituteEnvVars(config.getName()) + ".db";

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String copiedDatabaseName = databaseName + "_backup_" + timeStamp;

            try {
                Files.copy(projectPath.resolve(databaseName), projectPath.resolve(copiedDatabaseName));
            } catch (IOException e) {
                fLogger.warning(e);
            }
        }

        injector.getInstance(SettingDAO.class).MIGRATION_0_6_0();

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement("ALTER TABLE `player` DROP COLUMN `chat`");
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("ALTER TABLE `player` DROP COLUMN `locale`");
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("ALTER TABLE `player` DROP COLUMN `world_prefix`");
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("ALTER TABLE `player` DROP COLUMN `stream_prefix`");
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("ALTER TABLE `player` DROP COLUMN `afk_suffix`");
            preparedStatement.executeUpdate();
            preparedStatement = connection.prepareStatement("ALTER TABLE `player` DROP COLUMN `setting`");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }
}
