package com.ca.apm.tests.utils;

/*
 * Copyright (c) 2014 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for connect to EM database with IntroscopeDriver.
 * 
 * @author Tatsunobu Murata
 * @author Martin Batelka
 * @author batma08
 *
 */
public class IntroscopeDBAccessUtil implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(IntroscopeDBAccessUtil.class);
    private Connection connection = null;


    /**
     * Loads driver class
     * 
     * @param driverPath - path to driver
     */
    public void setUpDriver(String driverPath) throws Exception {
        log.info("Driver path set to " + driverPath);
        final String driver = "com.wily.introscope.jdbc.IntroscopeDriver";
        String jarUrl = "jar:file:" + driverPath + "!/";
        URLClassLoader cl = new URLClassLoader(new URL[] {new URL(jarUrl)});
        Driver d = (Driver) Class.forName(driver, true, cl).newInstance();
        DriverManager.registerDriver(new DriverProxy(d));
    }

    /**
     * Try to establish a connection to DB.
     * 
     * @param connectionString - in format [username:password@host:port]
     * @param driverPath - path of the jar with com.wily.introscope.jdbc.IntroscopeDriver.
     * 
     * @return true if successfully connected
     */
    public Connection setUpConnection(String connectionString) throws Exception {
        final String url = "jdbc:introscope:net//" + connectionString;
        log.info("Connecting to " + url);
        connection = DriverManager.getConnection(url);
        return connection;
    }


    /**
     * Execute query and returns resultset
     * 
     * @param sql - sql query to execute
     * @return resultset
     */
    public ResultSet getResult(String sql) throws SQLException {
        return connection.createStatement().executeQuery(sql);
    }

    /**
     * At the start query a connection to DB have to be established.
     * Send query to DB, count rows in result and returns its count. After all close connection.
     * 
     * @param sql
     * @return count of results (rows) in result set
     */
    public int getCount(String sql) throws SQLException {
        int count = 0;
        try (ResultSet rs = getResult(sql)) {
            while (rs.next()) {
                count++;
            }
        }
        log.info("Query returned " + count + " rows.");
        return count;
    }

    @Override
    public void close() throws Exception {
        log.info("Closing the connection");
        if (connection != null) connection.close();
    }


    /**
     * Class which serve as proxy object for compilator. Without this proxy
     * object we would get ClassNotFoundException.
     * 
     * @see http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
     * 
     * @author batma08
     *
     */
    class DriverProxy implements Driver {
        private Driver driver;

        DriverProxy(Driver d) {
            this.driver = d;
        }

        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return (java.util.logging.Logger) log;

        }
    }



}
