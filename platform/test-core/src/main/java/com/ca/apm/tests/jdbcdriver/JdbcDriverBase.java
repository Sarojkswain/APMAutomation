package com.ca.apm.tests.jdbcdriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.wily.introscope.spec.server.beans.jdbc.CannotParseException;

public class JdbcDriverBase extends BaseAgentTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JdbcDriverBase.class);
	private Connection fDConn = null;
	private Statement fDStmt = null;
	private ResultSet fDrs = null;
	private boolean blnAutoCommit = true;
	private final static String driver = "com.wily.introscope.jdbc.IntroscopeDriver";
	protected String connectionURL = "jdbc:introscope:net//";
	protected String baseQuery = "select * from metric_data where agent='.*' and metric='.*Number of Metrics .*'and timestamp between 'date1' and 'date2' period=60";
	protected String defaultQuery = "select * from metric_data where agent='.*' and metric='.*Number of Metrics .*' and timestamp between 'date1' and 'date2'";
	protected String customQuery = "select * from metric_data where agent='.*JBoss.*' and metric='.*' and timestamp between 'date1' and 'date2'";
	protected String aggregateTomcatQuery = "select * from metric_data where agent='.*Tomcat.*' and metric='.*GC Heap.*' and timestamp between 'date1' and 'date2' aggregateall";
	protected String defaultTomcatQuery = "select * from metric_data where agent='.*Tomcat.*' and metric='.*GC Heap.*' and timestamp between 'date1' and 'date2'";
	protected String wrongTablenameQuery = "select * from change_events";
	protected String invalidSQLQuery = "select agent from metric_data";
	private static int colControl[] = { 0, 0, 0, 0, 0, 1, 0, 1, 2, 2, 2, 2, 0,
			1, 1, 1 };
	protected static final String EM_ROLE_ID = "emRole";
	protected static final String EM_MACHINE_ID = "emMachine";
	protected static final String TOMCAT_ROLE_ID = "tomcatRole";
	protected static String platform = System.getProperty("os.name");
	protected String emhost;
	protected String emLibDir;
	protected String configFileEm;
	protected String emHome;
	protected String user;
	protected String passwd;
	protected String EMlogFile;
	protected String emPort;
	protected String listOpenPortsCmd;
	List<String> metricData = new ArrayList<String>();

	public JdbcDriverBase() {
		emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty(
				"emPort");
		emhost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
		EMlogFile = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		emHome = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR);
		configFileEm = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		user = "Admin";
		passwd = new String("");

		listOpenPortsCmd = platform.toUpperCase().contains("WINDOWS") ? "netstat -aon | find /C /i \"listening\""
				: "netstat -aon | grep -i \"listening\" |wc -l ";
	}

	/**
	 * This method is to establish the DB connection using JDBC Driver
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * 
	 * @return boolean
	 */
	protected boolean setConnection(String emHost, String emPort,
			String emUser, String emUserPasswd) throws SQLException {
		try {
			Class.forName(driver);
			LOGGER.info("The password is,,,,," + emUserPasswd);
			String hostport = emUser + ":" + emUserPasswd + "@" + emHost + ":"
					+ emPort;
			LOGGER.info("The connection URL is " + connectionURL
					+ hostport.trim());
			fDConn = DriverManager.getConnection(connectionURL
					+ hostport.trim());

			LOGGER.info("Connected to Database");
			return true;
		} catch (ClassNotFoundException e) {
			throw new SQLException(e.toString() + "JDBC Driver " + driver
					+ " not found.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			fDConn.setAutoCommit(blnAutoCommit);
		} catch (SQLException e) {
			throw new SQLException(e.toString()
					+ "Failed to connect to Database");
		}
		return false;
	}

	/**
	 * This method is to return the statement using JDBC Driver
	 * 
	 * @return Statement
	 */
	private Statement getStatement() throws SQLException {
		fDStmt = this.getConnction().createStatement();
		return fDStmt;
	}

	/**
	 * This method is to get the connection
	 * 
	 * @return Connection
	 */
	private Connection getConnction() throws SQLException {
		return fDConn;
	}

	/**
	 * This method is to get the result set for the given query using JDBC
	 * Driver
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * 
	 * @return ResultSet
	 */
	private ResultSet getResult(String sql) throws SQLException {
		ResultSet rs = null;
		try {
			rs = this.getStatement().executeQuery(sql);
			LOGGER.info("sql stmt excecuted" + sql);
		} catch (SQLException ex) {
			throw new SQLException(ex.toString() + "SQL Ramana");
		} finally {
		}
		if (fDrs != null) {
			fDrs.close();
			fDrs = null;
		}
		fDrs = rs;
		return rs;
	}

	/**
	 * Fetch the metrics from SmartStor using introscope JDBC SQL Query for past
	 * 'n' minutes and replaces the date timestamp only when query contains
	 * date1 and date2
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * @param queryStatement
	 * @param minutes
	 * 
	 * @return metricData
	 */
	protected List<String> getMetricData(String emHost, String emPort,
			String emUser, String emUserPasswd, String queryStatement,
			int minutes) throws SQLException {

		try {
			LOGGER.info("EM Details..." + emHost + emPort + emUser
					+ emUserPasswd);
			Assert.assertTrue(setConnection(emHost, emPort, emUser,
					emUserPasswd));
			String query = queryStatement;
			String currentDate = "";
			String pastDate = "";
			String col="";
			String val="";

			SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
			Calendar calendar = Calendar.getInstance();
			currentDate = ft.format(calendar.getTime());
			calendar.add(Calendar.MINUTE, -minutes);
			pastDate = ft.format(calendar.getTime());
			query = query.replace("date1", pastDate);
			query = query.replace("date2", currentDate);

			ResultSet rs = getResult(query);
			ResultSetMetaData md = null;
			metricData.clear();
			while (rs.next()) {
				md = rs.getMetaData();
				int colCnt = md.getColumnCount();
				String result = "";
				for (int i = 0; i < colCnt - 1; i++) {
					if (colControl[i] > 0) {
						if (colControl[i] == 2) {
							result += "\n\t";
						}
						 col = md.getColumnName(i + 1);
						 val = rs.getString(i + 1);
						result += (col + ":" + val + "/ ");
					}
				}
				metricData.add(result);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnction();
			LOGGER.info("Terminated the DB connection...");
		}
		return metricData;

	}

	/**
	 * Returns the rows count from the result set
	 * 
	 * @param resultset
	 * @param minutes
	 * 
	 * @return int
	 */
	private int getRowCount(ResultSet resultSet) {
		if (resultSet == null) {
			return 0;
		}
		try {
			resultSet.last();
			return resultSet.getRow();
		} catch (SQLException exp) {
			exp.printStackTrace();
		} finally {
			try {
				resultSet.beforeFirst();
			} catch (SQLException exp) {
				exp.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * This method is to close the DB connection using JDBC Driver
	 */
	private void closeConnction() throws SQLException {
		if (fDStmt != null) {
			fDStmt.close();
			fDStmt = null;
		}
		if (fDConn != null && !fDConn.isClosed()) {
			fDConn.commit();
			fDConn.close();
			fDConn = null;
		}
		blnAutoCommit = true;
	}

	/**
	 * Fetch the metrics from SmartStor using introscope JDBC SQL Query and
	 * return the count
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * @param queryStatement
	 * @param minutes
	 * 
	 * @return metricCount
	 */
	protected int getMetricDataInCount(String emHost, String emPort,
			String emUser, String emUserPasswd, String queryStatement,
			int minutes) throws SQLException {

		int rowCount;
		try {
			LOGGER.info("EM Details..." + emHost + emPort + emUser
					+ emUserPasswd);
			Assert.assertTrue(setConnection(emHost, emPort, emUser,
					emUserPasswd));
			String query = queryStatement;
			String currentDate = "";
			String pastDate = "";

			SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
			Calendar calendar = Calendar.getInstance();
			currentDate = ft.format(calendar.getTime());
			calendar.add(Calendar.MINUTE, -minutes);
			pastDate = ft.format(calendar.getTime());
			query = query.replace("date1", pastDate);
			query = query.replace("date2", currentDate);

			ResultSet rs = getResult(query);
			rowCount = getRowCount(rs);
			LOGGER.info("The row count for this query is..." + rowCount);
			return rowCount;
		}

		catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			closeConnction();
			LOGGER.info("Terminated the DB connection...");
		}

	}

	/**
	 * This method is to validate the Given String w.r.t to DateFormate
	 * 
	 * @param dateString
	 * 
	 * @return boolean
	 */
	protected boolean validateDateFormat(String dateString) {

		int year = Integer.parseInt(dateString.substring(0, 4));
		int month = Integer.parseInt(dateString.substring(5, 7));
		int day = Integer.parseInt(dateString.substring(8, 10));
		int hour = Integer.parseInt(dateString.substring(11, 13));
		int minute = Integer.parseInt(dateString.substring(14, 16));
		int seconds = Integer.parseInt(dateString.substring(17, 19));
		LOGGER.info("year --->" + year);
		LOGGER.info("month --->" + month);
		LOGGER.info("day --->" + day);
		LOGGER.info("hour --->" + hour);
		LOGGER.info("minute --->" + minute);
		LOGGER.info("seconds --->" + seconds);
		if (year > 1900 && year <= 2100)
			if (month >= 1 && month <= 12)
				if (day >= 1 && day <= 31)
					if (hour >= 0 && hour <= 23)
						if (minute >= 0 && minute <= 59)
							if (seconds >= 0 && seconds <= 59)
								return true;

		return false;

	}

	/**
	 * This method is to Append the period to the Given sql query
	 * 
	 * @param sqlQuery
	 * @param period
	 * 
	 * @return String
	 */
	protected String appendPeriodToQuery(String sqlQuery, int period) {
		return sqlQuery + " period=" + period;

	}

	/**
	 * This method is to add the date and time in LONG format to the given query
	 * for the 5 minute interval
	 * 
	 * @param sqlQuery
	 * @return String
	 */
	protected String updateLongDateInQuery(String sqlQuery) {

		Locale localeValue = Locale.getDefault();
		String currentDate = "";
		String pastDate = "";

		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.LONG, localeValue);
		Calendar calendar = Calendar.getInstance();
		currentDate = dateFormat.format(calendar.getTime());
		calendar.add(Calendar.MINUTE, -5);
		pastDate = dateFormat.format(calendar.getTime());
		sqlQuery = sqlQuery.replace("date1", pastDate);
		sqlQuery = sqlQuery.replace("date2", currentDate);
		LOGGER.info("The LongDateFormat Query is " + sqlQuery);
		return sqlQuery;

	}

	/**
	 * This method is to add the date and time to the given query in short
	 * format for the 5 minute interval
	 * 
	 * @param sqlQuery
	 * @return String
	 */
	protected String updateShortDateInQuery(String sqlQuery) {

		Locale localeValue = Locale.getDefault();
		String currentDate = "";
		String pastDate = "";

		DateFormat dateFormat = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.SHORT, localeValue);
		Calendar calendar = Calendar.getInstance();
		currentDate = dateFormat.format(calendar.getTime());
		calendar.add(Calendar.MINUTE, -5);
		pastDate = dateFormat.format(calendar.getTime());
		sqlQuery = sqlQuery.replace("date1", pastDate);
		sqlQuery = sqlQuery.replace("date2", currentDate);
		LOGGER.info("The SHORTDateFormat Query is " + sqlQuery);
		return sqlQuery;

	}

	/**
	 * This method is to add the date in SimpleDateFormat for the given query
	 * for the 5 minute interval
	 * 
	 * @param sqlQuery
	 * @return String
	 */
	protected String updateDateSimpleDateFormatInQuery(String sqlQuery,
			int pastMinutes) {

		String currentDate = "";
		String pastDate = "";
		SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
		Calendar calendar = Calendar.getInstance();
		currentDate = ft.format(calendar.getTime());
		calendar.add(Calendar.MINUTE, -pastMinutes);
		pastDate = ft.format(calendar.getTime());
		sqlQuery = sqlQuery.replace("date1", pastDate);
		sqlQuery = sqlQuery.replace("date2", currentDate);
		return sqlQuery;

	}

	/**
	 * This method is to add hidden property in a property file and verify
	 * weather added succesfully or not
	 * 
	 * @param propName
	 * @return boolean
	 */
	protected boolean addHiddenEMProp(String propName) {
		List<String> propToAppend = new ArrayList<String>();
		propToAppend.clear();
		propToAppend.add(propName);
		replaceProp(propName, "", EM_MACHINE_ID, configFileEm);
		restartEM(EM_ROLE_ID);
		appendProp(propToAppend, EM_MACHINE_ID, configFileEm);
		return checkForKeyword(envProperties, EM_MACHINE_ID, EMlogFile,
				" Added " + propName);
	}

	/**
	 * This method will take below arguments and returns true if got
	 * "java.lang.UnsupportedOperationException: Prepared Statements not supported yet"
	 * Support for PrepStmt was dropped from 8.x onwards
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * @param queryStatement
	 * 
	 * @return boolean
	 */
	protected boolean preparedStatementValidation(String emHost, String emPort,
			String user, String passwd, String sqlQuery) throws SQLException {
		Connection conn = null;
		try {

			Class.forName(driver);
			final String strUrl = "jdbc:introscope:net//" + user + ":" + passwd
					+ "@" + emHost + ":" + emPort;
			LOGGER.info("The Connection URL " + strUrl);
			conn = DriverManager.getConnection(strUrl);
			PreparedStatement statement = conn.prepareStatement(sqlQuery);
			statement.executeQuery();
			return false;
		} catch (UnsupportedOperationException e) {
			LOGGER.info("Prepared Statements not supported yet... which is expected here");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			LOGGER.info("Closing the DB Connection...");
			conn.close();
		}
	}

	/**
	 * This method will take below arguments and returns true if got
	 * "CannotParseException: Table name doesnot exists"
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * @param queryStatement
	 * 
	 * @return boolean
	 */
	protected boolean wrongTableNameEntryOrSQLQueryVerification(String emHost,
			String emPort, String user, String passwd, String sqlQuery)
			throws SQLException {
		Connection conn = null;
		try {

			Class.forName(driver);
			final String strUrl = "jdbc:introscope:net//" + user + ":" + passwd
					+ "@" + emHost + ":" + emPort;
			LOGGER.info("The connection url is...." + strUrl);
			conn = DriverManager.getConnection(strUrl);
			Statement stmt = conn.createStatement();
			stmt.executeQuery(sqlQuery);
			return false;
		} catch (CannotParseException e) {
			LOGGER.info("The given Table does not exists... which is expected here");
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			LOGGER.info("Closing the DB Connection...");
			conn.close();
		}
	}

	/**
	 * This method to verify when localziation settings are enable JDBC Driver
	 * throws that information when unable to parse the query
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * @param queryStatement
	 * @param minutes
	 * 
	 * @return metricCount
	 * @throws SQLException
	 */
	protected boolean localeDeVerify(String emHost, String emPort,
			String emUser, String emUserPasswd, String queryStatement,
			int minutes) throws SQLException {

		try {
			LOGGER.info("EM Details..." + emHost + emPort + emUser
					+ emUserPasswd);
			Assert.assertTrue(setConnection(emHost, emPort, emUser,
					emUserPasswd));
			String query = queryStatement;
			String currentDate = "";
			String pastDate = "";

			SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
			Calendar calendar = Calendar.getInstance();
			currentDate = ft.format(calendar.getTime());
			calendar.add(Calendar.MINUTE, -minutes);
			pastDate = ft.format(calendar.getTime());
			query = query.replace("date1", pastDate);
			query = query.replace("date2", currentDate);

			getResult(query);
			return false;
		}

		catch (SQLException e) {
			LOGGER.info("The exception is...." + e.toString());
			Assert.assertTrue(e.toString().contains("Locale=de_DE.SQL"));
			return true;
		} finally {
			closeConnction();
			LOGGER.info("Terminated the DB connection...");
		}

	}

	/**
	 * This method to return the Aggregated Metrics count for the given query
	 * 
	 * @param emHost
	 * @param emPort
	 * @param emUser
	 * @param emUserPasswd
	 * @param queryStatement
	 * @param coulumnNumber
	 * 
	 * @return metricCountInAggregatedMode
	 * @throws SQLException
	 */

	protected int returnAggregatedMetricsCount(String emHost, String emPort,
			String emUser, String emUserPasswd, String queryStatement,
			int coulumnNumber) throws SQLException {
		int count = 0;
		try {
			Assert.assertTrue(setConnection(emHost, emPort, emUser,
					emUserPasswd));
			ResultSet rs = getResult(queryStatement);
			ResultSetMetaData md = null;
			while (rs.next()) {
				md = rs.getMetaData();
				int colCnt = md.getColumnCount();
				String result = "";
				String[] splitIntoColumns = {};
				for (int i = 0; i < colCnt - 1; i++) {
					String column = md.getColumnName(i + 1);
					String value = rs.getString(i + 1);
					result += (column + ": " + value + ", ");
				}
				splitIntoColumns = result.split(",");
				LOGGER.info("The Colun Result -->"
						+ splitIntoColumns[coulumnNumber]);
				count = count
						+ Integer.parseInt(splitIntoColumns[coulumnNumber]
								.split(":")[1].trim());
			}
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnction();
			LOGGER.info("Terminated the DB connection...");
		}
		return 0;
	}
}
