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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for connect to EM database with IntroscopeDriver.
 * 
 * @author katra03
 *
 */
public class IntroscopeDBAccessUtil implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(IntroscopeDBAccessUtil.class);
	private static String token = null;
    private Connection connection = null;


    /**
     * Loads driver class
     * 
     * @param driverPath - path to driver
     */
    public void setUpDriver(String driverPath) throws Exception {
        log.info("Driver path set to " + driverPath);
        final String driver = "org.teiid.jdbc.TeiidDriver";
        String jarUrl = "jar:file:" + driverPath + "!/";
        try{
        URLClassLoader cl = new URLClassLoader(new URL[] {new URL(jarUrl)});
        Driver d = (Driver) Class.forName(driver, true, cl).newInstance();
        DriverManager.registerDriver(new DriverProxy(d));
        }catch(Exception e){
            e.printStackTrace();
        }
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
        try{
        final String url = "jdbc:teiid:apm_base@mm://"+ connectionString;
        log.info("Connecting to " + url);        
        connection = DriverManager.getConnection(url,  "admin", "");
        }catch(Exception e){
            e.printStackTrace();
        }
        return connection;
    }
	
	public void getToken() throws JSONException {
		
		WebClient client = null;
		JSONObject reqBody = new JSONObject();
		reqBody.put("username", "admin");
		reqBody.put("password", "");
		client = WebClient.create("http://localhost:8081/apm/appmap/private/token/temporaryToken");
		Response response = client.accept("application/json")
				.type("application/json").post(reqBody.toString());

		if (Response.Status.OK.getStatusCode() == response.getStatus()) {
			 

			JSONObject jsonObject = new JSONObject(
					response.readEntity(String.class));
			token =(String) jsonObject.get("token");
			System.out.println("Token"+token);

			
		} 
		
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
    
    public List getString(String sql) throws SQLException {
        List<String> listOfString = new ArrayList<String>();
        int count=0;
        try (ResultSet rs = getResult(sql)) {
            while (rs.next()) { 
                  listOfString.add(rs.getString(1)); 
                count++;
                
            }
        }
        log.info("Query returned " + listOfString + " data.");
        log.info("returned count"+count);
        return listOfString;        
    }
    
    public List getValue(String sql) throws SQLException {
        List<Integer> returnedValue = new ArrayList<Integer>();
      
        int count = 0;
        try (ResultSet rs = getResult(sql)) {
            while (rs.next()) {
                returnedValue.add(rs.getInt(1));
                count++;
            }
        }
        log.info("Query returned " + count + " rows.");
        log.info("Query returned " + returnedValue + " data.");
        return returnedValue;
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
     * @author katra03
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
