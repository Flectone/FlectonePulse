package net.flectone.pulse.data.database.driver;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public record DriverWrapper(
        Driver delegate
) implements Driver {

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return delegate.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return delegate.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

}