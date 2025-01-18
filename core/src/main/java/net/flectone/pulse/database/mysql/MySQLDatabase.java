package net.flectone.pulse.database.mysql;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class MySQLDatabase extends Database {

    private final Config.Database config;
    private final InputStream SQLFile;
    private final FLogger fLogger;

    private HikariDataSource dataSource;

    @Inject
    public MySQLDatabase(FileManager fileManager,
                         @Named("SQLFile") InputStream SQLFile,
                         FLogger fLogger) {
        this.SQLFile = SQLFile;
        this.fLogger = fLogger;

        config = fileManager.getConfig().getDatabase();
    }

    @Override
    public void connect() throws SQLException, IOException {
        String connectionURL = new StringBuilder()
                .append("jdbc:mysql://")
                .append(config.getHost())
                .append(":")
                .append(config.getPort())
                .append("/")
                .append(config.getName())
                .append(config.getParameters())
                .toString();

        HikariConfig hikariConfig = getHikariConfig(connectionURL);

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            fLogger.warning("Failed to initialize MySQL. Check database settings");
            fLogger.warning(e);

            throw new RuntimeException();
        }

        try (Connection connection = dataSource.getConnection()){
            if (connection != null) {
                executeFile(SQLFile);
                init();

                fLogger.info("MySQL Database connected");
            }
        }
    }

    private HikariConfig getHikariConfig(String connectionURL) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(connectionURL);
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        hikariConfig.setPoolName("FlectonePulseMySQL");
        return hikariConfig;
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void init() throws SQLException {

    }

    @Override
    public void disconnect() {
        if(dataSource != null) {
            dataSource.getHikariPoolMXBean().softEvictConnections();
            dataSource.close();
        }

        fLogger.info("MySQL Database disconnected");
    }
}
