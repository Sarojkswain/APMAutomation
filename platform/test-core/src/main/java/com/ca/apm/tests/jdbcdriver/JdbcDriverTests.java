package com.ca.apm.tests.jdbcdriver;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.XMLUtil;

public class JdbcDriverTests extends JdbcDriverBase {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JdbcDriverTests.class);
	ApmbaseUtil util = new ApmbaseUtil();
	private int timeInMinutes = 5;

	@BeforeSuite
	public void initiate() {

		startEM(EM_ROLE_ID);
		startTomcatAgent(TOMCAT_ROLE_ID);
		harvestWait(300);

	}

	@Test(groups = { "Deep Regression" }, enabled = true)
	public void Verify_ALM_297930_Verify_timestamp_returned_by_Smartstor_JDBC()
			throws Exception {
		String testCaseName = "Verify_ALM_297930_Verify_timestamp_returned_by_Smartstor_JDBC";
		testCaseStart(testCaseName);

		Assert.assertEquals(
				getMetricDataInCount(
						emhost,
						emPort,
						user,
						passwd,
						appendPeriodToQuery(
								updateDateSimpleDateFormatInQuery(defaultQuery,
										4), 15), 4), 16);
		Assert.assertEquals(
				getMetricDataInCount(
						emhost,
						emPort,
						user,
						passwd,
						appendPeriodToQuery(
								updateDateSimpleDateFormatInQuery(defaultQuery,
										4), 30), 4), 8);

		metricData.clear();
		metricData = getMetricData(emhost, emPort, user, passwd,
				updateShortDateInQuery(defaultQuery), timeInMinutes);
		String timeStamp = metricData.get(1).split("Actual_Start_Timestamp:")[1]
				.substring(0, 22);
		Assert.assertTrue(validateDateFormat(timeStamp));
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void Verify_ALM_333561_Enable_introscope_enterprisemanager_jdbc_synchronous() {

		String testCaseName = "Verify_ALM_333561_Enable_introscope_enterprisemanager_jdbc_synchronous";
		int queryPeriod15Count, queryPeriod30Count, queryPeriod45Count, queryPeriod60Count;
		int newQueryPeriod15Count, newQueryPeriod30Count, newQueryPeriod45Count, newQueryPeriod60Count;

		String sqlQuery = updateDateSimpleDateFormatInQuery(defaultQuery,
				timeInMinutes);
		String queryPeriod15 = appendPeriodToQuery(sqlQuery, 15);
		String queryPeriod30 = appendPeriodToQuery(sqlQuery, 30);
		String queryPeriod45 = appendPeriodToQuery(sqlQuery, 45);
		String queryPeriod60 = appendPeriodToQuery(sqlQuery, 60);

		testCaseStart(testCaseName);
		try {

			replaceProp(ApmbaseConstants.log4jInfoProp, ApmbaseConstants.log4jDebugProp, EM_MACHINE_ID,
					configFileEm);
			checkLogForMsg(envProperties, EM_MACHINE_ID, EMlogFile,
					"Detected hot config change to");
			addHiddenEMProp(ApmbaseConstants.jdbcSynchronousTrueProp);

			queryPeriod15Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod15, timeInMinutes);
			queryPeriod30Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod30, timeInMinutes);

			queryPeriod45Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod45, timeInMinutes);

			queryPeriod60Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod60, timeInMinutes);

			LOGGER.info("Synchronous True...." + queryPeriod15Count + "---->"
					+ queryPeriod30Count + "-----" + queryPeriod45Count
					+ "-------=" + queryPeriod60Count);

			replaceProp(ApmbaseConstants.jdbcSynchronousTrueProp, ApmbaseConstants.jdbcSynchronousFalseProp,
					EM_MACHINE_ID, configFileEm);
			checkLogForMsg(envProperties, EM_MACHINE_ID, EMlogFile,
					"Changed introscope.enterprisemanager.jdbc.synchronous=false (true)");
			newQueryPeriod15Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod15, timeInMinutes);
			newQueryPeriod30Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod30, timeInMinutes);
			newQueryPeriod45Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod45, timeInMinutes);
			newQueryPeriod60Count = getMetricDataInCount(emhost, emPort, user,
					passwd, queryPeriod60, timeInMinutes);

			LOGGER.info("Synchronous False...." + newQueryPeriod15Count
					+ "---->" + newQueryPeriod30Count + "-----"
					+ newQueryPeriod45Count + "-------="
					+ newQueryPeriod60Count);

			if (queryPeriod15Count == newQueryPeriod15Count
					&& queryPeriod30Count == newQueryPeriod30Count
					&& queryPeriod45Count == newQueryPeriod45Count
					&& queryPeriod60Count == newQueryPeriod60Count)
				Assert.assertTrue(true);
			else {
				LOGGER.error("The metric count is not same when we turned on synchronous to true/false");
				Assert.assertTrue(false);
			}

		} catch (SQLException e) {
			LOGGER.info("Unable to execute the SQL Query....");
			e.printStackTrace();
		} finally {

			LOGGER.info("Reset the values to defaults");
			replaceProp(ApmbaseConstants.log4jDebugProp, ApmbaseConstants.log4jInfoProp, EM_MACHINE_ID,
					configFileEm);
			replaceProp(ApmbaseConstants.jdbcSynchronousFalseProp, "", EM_MACHINE_ID,
					configFileEm);
		}
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void Verify_ALM_333562_Hot_configure_introscope_enterprisemanager_jdbc_synchronous_property() {

		String testCaseName = "Verify_ALM_333562_Hot_configure_introscope_enterprisemanager_jdbc_synchronous_property";
		testCaseStart(testCaseName);

		Assert.assertTrue(addHiddenEMProp(ApmbaseConstants.jdbcSynchronousTrueProp));
		replaceProp(ApmbaseConstants.jdbcSynchronousTrueProp, "", EM_MACHINE_ID, configFileEm);
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_280503_JDBC_driver_throws_exception_when_using_PreparedStatments() {

		String testCaseName = "Verify_ALM_280503_JDBC_driver_throws_exception_when_using_PreparedStatments";
		testCaseStart(testCaseName);
		try {
			Assert.assertTrue(preparedStatementValidation(
					emhost,
					emPort,
					user,
					passwd,
					updateDateSimpleDateFormatInQuery(defaultQuery,
							timeInMinutes)));
		} catch (SQLException e) {
			Assert.assertTrue(false);
			e.printStackTrace();
		}
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void Verify_ALM_297934_TT_57603_Problems_in_JDBC_API_when_user_password_contains_an_AT_Symbol() {

		String testCaseName = "Verify_ALM_297934_TT_57603_Problems_in_JDBC_API_when_user_password_contains_an_AT_Symbol";
		testCaseStart(testCaseName);
		String plainPassword = "jayaram@";
		int metricCount;

		String encryptedPasswd = util.encryptPassword(emHome + "/tools",
				plainPassword);
		try {
			Assert.assertEquals(
					XMLUtil.deleteUser(emHome + "/config/users.xml", "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.addUser(emHome + "/config/users.xml",
					"Admin", encryptedPasswd), XMLUtil.SUCCESS_MESSAGE);

			harvestWait(30);
			metricCount = getMetricDataInCount(emhost, emPort, user,
					plainPassword,
					updateDateSimpleDateFormatInQuery(defaultQuery, 5),
					timeInMinutes);
			LOGGER.info("The size of the list is ..." + metricCount);
			Assert.assertTrue(metricCount > 3);

		} catch (SQLException e) {
			LOGGER.info("Unable to retrieve metrics when user password contains @");
			Assert.assertTrue(false);
			e.printStackTrace();
		} finally {
			Assert.assertEquals(
					XMLUtil.deleteUser(emHome + "/config/users.xml", "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.addUser(emHome + "/config/users.xml", "Admin", ""),
					XMLUtil.SUCCESS_MESSAGE);
			harvestWait(30);
		}

		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_203523_Wrong_table_name() {
		String testCaseName = "Verify_ALM_203523_Wrong_table_name";
		testCaseStart(testCaseName);
		try {
			Assert.assertTrue(wrongTableNameEntryOrSQLQueryVerification(emhost,
					emPort, user, passwd, wrongTablenameQuery));
		} catch (SQLException e) {
			Assert.assertTrue(false);
			e.printStackTrace();
		}
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_203524_Invalid_SQL_stmt() {
		String testCaseName = "Verify_ALM_203524_Invalid_SQL_stmt";
		testCaseStart(testCaseName);
		try {
			Assert.assertTrue(wrongTableNameEntryOrSQLQueryVerification(emhost,
					emPort, user, passwd, invalidSQLQuery));
		} catch (SQLException e) {
			Assert.assertTrue(false);
			e.printStackTrace();
		}
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Deep Regression" }, enabled = true)
	public void Verify_ALM_297942_Test_LONG_DateFormat_With_JDBC_Driver() {
		String testCaseName = "Verify_ALM_297942_Test_LONG_DateFormat_With_JDBC_Driver";
		testCaseStart(testCaseName);

		metricData.clear();
		try {
			metricData = getMetricData(emhost, emPort, user, passwd,
					updateLongDateInQuery(defaultQuery), 5);
		} catch (SQLException e) {
			Assert.assertTrue(false);
			e.printStackTrace();
		}
		String timeStamp = metricData.get(1).split("Actual_Start_Timestamp:")[1]
				.substring(0, 22);
		LOGGER.info("The time stamp is..." + timeStamp);
		Assert.assertTrue(validateDateFormat(timeStamp));

		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Deep Regression" }, enabled = true)
	public void Verify_ALM_297943_Test_SHORT_DateFormat_With_JDBC_Driver() {
		String testCaseName = "Verify_ALM_297943_Test_SHORT_DateFormat_With_JDBC_Driver";
		testCaseStart(testCaseName);

		metricData.clear();
		try {
			metricData = getMetricData(emhost, emPort, user, passwd,
					updateShortDateInQuery(defaultQuery), timeInMinutes);
		} catch (SQLException e) {
			Assert.assertTrue(false);
			e.printStackTrace();
		}
		String timeStamp = metricData.get(1).split("Actual_Start_Timestamp:")[1]
				.substring(0, 22);
		LOGGER.info("The time stamp is..." + timeStamp);
		Assert.assertTrue(validateDateFormat(timeStamp));

		testCaseEnd(testCaseName);
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void Verify_ALM_350849_JDBC_Driver_Connection_leak_TT67689() {
		String testCaseName = "Verify_ALM_350849_JDBC_Driver_Connection_leak_TT67689";
		testCaseStart(testCaseName);

		int beforeQueryPortCount, afterQueryPortCount;
		String falseConnectionURL = connectionURL + user + ":" + "test1234"
				+ "@" + emhost + ":" + emPort;
		beforeQueryPortCount = util.OpenPortsCount();
		try {
			DriverManager.getConnection(falseConnectionURL);
		} catch (SQLException e) {
			Assert.assertTrue(true);
		}
		afterQueryPortCount = util.OpenPortsCount();
		Assert.assertEquals(beforeQueryPortCount, afterQueryPortCount);
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Deep Regression" }, enabled = true)
	public void Verify_ALM_350852_JDBC_driver_should_not_report_erroneous_value_Zero_when_agent_is_down_TT63257() {
		String testCaseName = "Verify_ALM_350852_JDBC_driver_should_not_report_erroneous_value_Zero_when_agent_is_down_TT63257";
		testCaseStart(testCaseName);
		int count = 0;

		try {
			count = getMetricDataInCount(emhost, emPort, user, passwd,
					updateShortDateInQuery(customQuery), timeInMinutes);
		} catch (SQLException e) {
			Assert.assertTrue(false);
			e.printStackTrace();
		}
		Assert.assertEquals(0, count);
		LOGGER.info("The test passed, As agent is not available the driver returned 0, Interpret this 0 as No rows returned");
		testCaseEnd(testCaseName);
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_204955_Print_out_em_server_locale_info_when_JDBC_date_parsing_fails_on_the_client() {
		String testCaseName = "Verify_ALM_204955_Print_out_em_server_locale_info_when_JDBC_date_parsing_fails_on_the_client";
		testCaseStart(testCaseName);
		List<String> localeProperties = new ArrayList<String>();

		try {
			replaceProp(ApmbaseConstants.log4jInfoProp, ApmbaseConstants.log4jDebugProp, EM_MACHINE_ID,
					configFileEm);
			checkLogForMsg(envProperties, EM_MACHINE_ID, EMlogFile,
					"Detected hot config change to");
			localeProperties.add(ApmbaseConstants.localeDe);
			localeProperties.add(ApmbaseConstants.regionDe);
			appendProp(localeProperties, EM_MACHINE_ID, configFileEm);
			restartEM(EM_ROLE_ID);

			Assert.assertTrue(localeDeVerify(emhost, emPort, user, passwd,
					updateShortDateInQuery(customQuery), timeInMinutes));
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}

		finally {
			replaceProp(ApmbaseConstants.localeDe, "", EM_MACHINE_ID, configFileEm);
			replaceProp(ApmbaseConstants.regionDe, "", EM_MACHINE_ID, configFileEm);
			replaceProp(ApmbaseConstants.log4jDebugProp, ApmbaseConstants.log4jInfoProp, EM_MACHINE_ID,
					configFileEm);
			checkLogForMsg(envProperties, EM_MACHINE_ID, EMlogFile,
					"Detected hot config change to");
			restartEM(EM_ROLE_ID);
			harvestWait(300);
		}
		testCaseEnd(testCaseName);

	}

	@Test(groups = { "BAT" }, enabled = true)
	public void Verify_ALM_350851_Aggregate_All() {

		String testCaseName = "Verify_ALM_350851_Aggregate_All";
		testCaseStart(testCaseName);
		int metricCountWithoutAggregation, metricCountWithAggregation;
		try {

			// Here 2 minutes with 15 seconds interval and column 11 is Count
			metricCountWithoutAggregation = returnAggregatedMetricsCount(
					emhost,
					emPort,
					user,
					passwd,
					updateDateSimpleDateFormatInQuery(
							appendPeriodToQuery(defaultTomcatQuery, 15), 2), 11);

			metricCountWithAggregation = returnAggregatedMetricsCount(
					emhost,
					emPort,
					user,
					passwd,
					updateDateSimpleDateFormatInQuery(
							appendPeriodToQuery(aggregateTomcatQuery, 15), 2),
					11);

			Assert.assertEquals(metricCountWithAggregation,
					metricCountWithoutAggregation);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
		testCaseEnd(testCaseName);
	}

	@AfterSuite
	public void cleanUp() {
		stopEM(EM_ROLE_ID);
		stopTomcatAgent(TOMCAT_ROLE_ID);
	}

}
