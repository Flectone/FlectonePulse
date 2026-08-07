package net.flectone.pulse.persistence.database.driver;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public record DriverBackedDataSource(
        Driver driver,
        String jdbcUrl,
        Properties properties
) implements DataSource {

    @Override
    public Connection getConnection() throws SQLException {
        return driver.connect(jdbcUrl, properties);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties connectionProperties = new Properties(this.properties);
        connectionProperties.setProperty("user", username);
        connectionProperties.setProperty("password", password);
        return driver.connect(jdbcUrl, connectionProperties);
    }

    @Override
    public PrintWriter getLogWriter() {
        // Hikari does not use this method directly
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        // Hikari does not use this method directly
    }

    @Override
    public void setLoginTimeout(int seconds) {
        // Hikari does not use this method directly
    }

    @Override
    public int getLoginTimeout() {
        // Hikari does not use this method directly
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // Hikari does not use this method directly
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // Hikari does not use this method directly
        throw new SQLException("Not a wrapper");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        // Hikari does not use this method directly
        return false;
    }

}