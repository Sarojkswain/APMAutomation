package com.ca.apm.systemtest.fld.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntroscopeDriverDBAccessHelper implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IntroscopeDriverDBAccessHelper.class);

    public static final String INTROSCOPE_DRIVER_CLASS =
        "com.wily.introscope.jdbc.IntroscopeDriver";

    private String url;
    private Connection connection;
    private Statement statement;
    private ResultSet rs;

    public IntroscopeDriverDBAccessHelper(URL jarUrl) throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, SQLException {
        this(INTROSCOPE_DRIVER_CLASS, jarUrl);
    }

    private IntroscopeDriverDBAccessHelper(String driverClass, URL jarUrl)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        log.info("IntroscopeDriver path set to {}", jarUrl);
        @SuppressWarnings("rawtypes")
        Class clazz =
            Class.forName(driverClass, true, new URLClassLoader(new URL[] {jarUrl},
                IntroscopeDriverDBAccessHelper.class.getClassLoader()));
        Driver driver = (Driver) clazz.newInstance();
        DriverManager.registerDriver(new DriverProxy(driver));
    }

    public Connection setUpConnection(String url) throws SQLException {
        this.url = url;
        connection = DriverManager.getConnection(this.url);
        log.info("Connected to {}", this.url);
        return connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement createStatement() throws SQLException {
        statement = connection.createStatement();
        return statement;
    }

    public Statement getStatement() throws SQLException {
        return statement == null ? createStatement() : statement;
    }

    public ResultSet getResult(String sql) throws SQLException {
        close(rs);
        rs = getStatement().executeQuery(sql);
        return rs;
    }

    public int getCount(String sql) throws SQLException {
        int count = 0;
        try (ResultSet rs = getResult(sql)) {
            while (rs.next()) {
                count++;
            }
        }
        return count;
    }

    public boolean close(ResultSet rs) {
        if (rs == null) {
            return false;
        }
        try {
            rs.close();
        } catch (Exception e) {}
        rs = null;
        return true;
    }

    @Override
    public void close() throws Exception {
        log.info("close()::");
        url = null;

        close(rs);

        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {}
        }
        statement = null;

        if (connection != null) {
            try {
                connection.close();
                log.info("Connection closed");
            } catch (Exception e) {}
        }
        connection = null;
    }

    @Override
    protected void finalize() throws Throwable {
        log.info("finalize()::");
        try {
            close();
        } catch (Exception e) {}
        super.finalize();
    }


    private static class DriverProxy implements Driver {
        private Driver driver;

        private DriverProxy(Driver d) {
            this.driver = d;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return driver.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
            return driver.getPropertyInfo(url, info);
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
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return (java.util.logging.Logger) log;
        }
    }

}
