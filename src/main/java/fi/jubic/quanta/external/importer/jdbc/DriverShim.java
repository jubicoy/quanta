package fi.jubic.quanta.external.importer.jdbc;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class DriverShim implements java.sql.Driver {
    private final java.sql.Driver driver;

    private DriverShim(java.sql.Driver driver) {
        this.driver = driver;
    }

    public static DriverShim of(java.sql.Driver driver) {
        return new DriverShim(driver);
    }

    @Override
    public Connection connect(
            String s,
            Properties properties
    ) throws SQLException {
        return driver.connect(s, properties);
    }

    @Override
    public boolean acceptsURL(String s) throws SQLException {
        return driver.acceptsURL(s);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(
            String s,
            Properties properties
    ) throws SQLException {
        return driver.getPropertyInfo(s, properties);
    }

    @Override
    public int getMajorVersion() {
        return driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return driver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }
}
