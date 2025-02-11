package net.flectone.pulse.database;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.util.SystemUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class Database {

    private final Config.Database config;

    private final Path projectPath;
    private final InputStream SQLFile;
    private final SystemUtil systemUtil;
    private final FLogger fLogger;

    private HikariDataSource dataSource;

    @Inject
    public Database(FileManager fileManager,
                    @Named("projectPath") Path projectPath,
                    @Named("SQLFile") InputStream SQLFile,
                    SystemUtil systemUtil,
                    FLogger fLogger) {

        this.projectPath = projectPath;
        this.SQLFile = SQLFile;
        this.systemUtil = systemUtil;
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
                        systemUtil.substituteEnvVars(config.getName()) +
                        ".db";

                hikariConfig.setMaximumPoolSize(5);
                hikariConfig.setMinimumIdle(1);
                hikariConfig.addDataSourceProperty("journal_mode", "WAL");
                hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
                hikariConfig.addDataSourceProperty("journal_size_limit", "6144000");
            }

            case MYSQL -> {
                connectionURL = connectionURL +
                        "//" +
                        systemUtil.substituteEnvVars(config.getHost()) +
                        ":" +
                        systemUtil.substituteEnvVars(config.getPort()) +
                        "/" +
                        systemUtil.substituteEnvVars(config.getName()) +
                        config.getParameters();

                hikariConfig.setUsername(systemUtil.substituteEnvVars(config.getUser()));
                hikariConfig.setPassword(systemUtil.substituteEnvVars(config.getPassword()));
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
}
