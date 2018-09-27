/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.functional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.automation.common.AutomationConstants;
import com.ca.apm.automation.common.mockem.IAgentStatusQueryHelper;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.tests.config.BaseAppConfig;
import com.ca.apm.tests.config.LoggingLevel;
import com.ca.apm.tests.config.NodeJSProbeConfig;
import com.ca.apm.tests.config.TixChangeAppConfig;
import com.ca.apm.tests.config.UMAgentConfig;
import com.ca.apm.tests.config.UMAgentConfig.AgentLoggingLevel;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.apm.tests.role.UMAgentRole;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.FullAgentNameExpr;
import com.ca.apm.tests.utils.IAgentNameExpr;
import com.ca.apm.tests.utils.MockEmWrapper;
import com.ca.tas.test.TasTestNgTest;

/**
 * Base test class for node agent automation
 *
 * @author sinka08
 */
public class BaseNodeAgentTest extends TasTestNgTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseNodeAgentTest.class);
	public static final String LOG_FILE_EXT = ".log";
	public static final String NODE_AGENT_NAME_EXPR = ".*node.*";
	public static final String UMAGENT_NAME_EXPR = ".*Infrastructure.*";
	public static final IAgentNameExpr NODE_AGENT_EXPR = new FullAgentNameExpr(".*",
	        NODE_AGENT_NAME_EXPR, ".*");
	public static final IAgentNameExpr UMAGENT_EXPR = new FullAgentNameExpr(".*",
			UMAGENT_NAME_EXPR, ".*");
	protected static volatile MockEmWrapper mockEm;
	
	protected static UMAgentConfig umAgentConfig;

	protected static NodeJSProbeConfig probeConfig;
	protected static TixChangeAppConfig tixChangeConfig;
	// TODO Support port config via role
	protected static String appUrlBase = "http://localhost:3000";
	// currently executing test class
	protected String testSetName = "nodeagenttests";
	// currently executing test method
	protected String testMethodName = "nodeagent-testmethod";
	
	private String iaDir = "apmia";
	protected String iaHome = envProperties.getRolePropertyById(NodeJSAgentTestbed.UMAGENT_ROLE_ID, UMAgentRole.Builder.ENV_UMA_AGENT_HOME) + iaDir;

	@BeforeSuite(alwaysRun = true)
	public void testSuiteSetup() {
		mockEm = new MockEmWrapper.Builder().build();
		mockEm.start();

		initUMAgentConfig();
		initProbeConfig();
		initTixChangeConfig();

		// good to create backup of original Collector Agent profile, probe
		// config.json
		BaseAppConfig[] appConfigs = { umAgentConfig, probeConfig };
		disableUrlGroups();
		CommonUtils.createBackupOfAppConfig(appConfigs);

		// copy logs from previous suite run
		CommonUtils.copyLogs(appConfigs);
	}

	protected void initUMAgentConfig() {
		umAgentConfig = new UMAgentConfig(iaHome);

		umAgentConfig.updateLogLevel(AgentLoggingLevel.DEBUG);
		umAgentConfig.updateProbeCollectorLogLevel(AgentLoggingLevel.TRACE);
		umAgentConfig.updateProperty(
		        AutomationConstants.Agent.LOG_APPENDER_LOGFILE_MAX_FILESIZE_PROPERTY, "200MB");
		umAgentConfig.updateProperty(
		        AutomationConstants.Agent.LOG_APPENDER_LOGFILE_MAX_BACKUP_INDEX_PROPERTY, "10");
	}

	private void initProbeConfig() {
		probeConfig = new NodeJSProbeConfig(envProperties.getRolePropertyById(
		        NodeJSAgentTestbed.TIXCHANGE_PROBE_ROLE_ID,
		        NodeJSProbeRole.Builder.ENV_NODEJS_PROBE_HOME));
		probeConfig.updateLogLevel(LoggingLevel.DEBUG);
	}

	private void initTixChangeConfig() {
		String roleId = NodeJSAgentTestbed.TIXCHANGE_ROLE_ID;
		tixChangeConfig = new TixChangeAppConfig(envProperties.getRolePropertyById(roleId,
		        TixChangeRole.ENV_TIXCHANGE_SERVER_DIR), envProperties.getRolePropertyById(roleId,
		        TixChangeRole.ENV_TIXCHANGE_STARTUP_SCRIPT_PATH),
		        envProperties.getRolePropertyById(roleId,
		                TixChangeRole.ENV_TIXCHANGE_SERVER_LOG_FILE));
		tixChangeConfig.setHost(envProperties.getMachineHostnameByRoleId(roleId));
		tixChangeConfig.setPort(envProperties.getRolePropertyById(roleId,
		        TixChangeRole.ENV_TIXCHANGE_PORT));
		appUrlBase = tixChangeConfig.getAppUrlBase();
	}

	public void testClassSetup() {
		testSetName = this.getClass().getSimpleName();
		updateCollectorAndProbeLogFileName(testSetName);
	}

	public void updateCollectorAndProbeLogFileName(String fileName) {
		probeConfig.updateLogFileName(fileName + LOG_FILE_EXT);
		umAgentConfig.updateLogFileName(fileName + LOG_FILE_EXT);
	}

	public void updateProbeLogFileName(String fileName) {
		probeConfig.updateLogFileName(fileName + LOG_FILE_EXT);
	}

	public void updateCollectorLogFileName(String fileName) {
		umAgentConfig.updateLogFileName(fileName + LOG_FILE_EXT);
	}

	protected void startCollectorAgent() {
		runSerializedCommandFlowFromRole(NodeJSAgentTestbed.UMAGENT_ROLE_ID,
		        UMAgentRole.Builder.ENV_START_UMA);
	}

	/**
	 * Start Collector Agent and wait for it to connect to mockem
	 */
	protected void startCollectorAgentAndWaitConn() {
		startCollectorAgent();
		Assert.assertTrue(isAgentConnected(UMAGENT_EXPR),
		        "um agent is not connected to mockem");
	}

	protected void stopCollectorAgent() {
		runSerializedCommandFlowFromRole(NodeJSAgentTestbed.UMAGENT_ROLE_ID,
		        UMAgentRole.Builder.ENV_STOP_UMA);
	}

	/**
	 * Stop Collector Agent and wait for it to disconnect from mockem
	 */
	protected void stopCollectorAgentAndWaitDisc() {
		stopCollectorAgent();
		Assert.assertTrue(isAgentDisconnected(UMAGENT_EXPR),
		        "um agent failed to disconnect");
	}

	protected void startNodeApp() {
		LOGGER.info("starting node app");
		runSerializedCommandFlowFromRole(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID,
		        TixChangeRole.ENV_TIXCHANGE_START);
	}

	/**
	 * Start Node Application and wait for node agent to connect to mockem
	 */
	protected void startAppAndWaitConn() {
		startNodeApp();
		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	protected void stopNodeApp() {
		LOGGER.info("stopping node app");
		runSerializedCommandFlowFromRole(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID,
		        TixChangeRole.ENV_TIXCHANGE_STOP);
	}

	/**
	 * Stop Node Application and wait for node agent to disconnect from mockem
	 */
	protected void stopAppAndWaitDisc() {
		stopNodeApp();
		Assert.assertTrue(isAgentDisconnected(NODE_AGENT_EXPR), "nodejs agent failed to disconnect");
	}

	protected boolean isAgentConnected(IAgentNameExpr agentNameExpr) {
		
		RequestProcessor reqProcessor = mockEm.getReqProcessor(agentNameExpr);
		long maxWaitTimeForRegistration = 120000;
		long maxWaitTimeForConnection = 120000;
		boolean isAgentConnected = false;

		IAgentStatusQueryHelper[] helpers = reqProcessor
		        .getMockEmAgentStatusHelper(maxWaitTimeForRegistration);

		if (helpers.length > 0) {
			if (helpers.length == 1 && helpers[0] != null) {
				IAgentStatusQueryHelper h = helpers[0];

				// agent already registered, wait for connection
				isAgentConnected = h.waitForConnection(maxWaitTimeForConnection);
				if (isAgentConnected) {
					LOGGER.info("verified agent: {} is connected to mockem", h.getFQAgentName());
				}
			}

			if (helpers.length > 1) {
				Assert.fail("Could not decide which agent connection we should wait for."
				        + "More than expected number of agents registered to mockem. please check test configuration");
			}
		}

		return isAgentConnected;
	}

	protected boolean isAgentDisconnected(IAgentNameExpr agentNameExpr) {
		
		RequestProcessor reqProcessor = mockEm.getReqProcessor(agentNameExpr);
		IAgentStatusQueryHelper[] helpers = reqProcessor.getAgentStatusQueryHelpers();
		boolean isAgentDisconnected = true;

		// if agent did not already disconnect
		if (helpers.length > 0) {
			if (helpers.length == 1) {
				long maxDiscWaitTime = 15000;
				isAgentDisconnected = helpers[0].waitForDisconnection(maxDiscWaitTime);
			} else {
				isAgentDisconnected = false;
				for (IAgentStatusQueryHelper h : helpers) {
					LOGGER.debug("agent: {} still connected", h.getFQAgentName());
				}

				Assert.fail("Could not decide which agent disconnection we should wait for."
				        + "More than expected number of agents connected to mockem. please check test configuration");
			}
		}
		return isAgentDisconnected;
	}

	protected void checkErrorInLogs() {
		checkErrorInCollectorLogs();
		checkErrorInProbeLogs();
	}

	protected void checkErrorInCollectorLogs() {
		CommonUtils.checkErrorInCollectorLogs(umAgentConfig);
	}

	protected void checkErrorInProbeLogs() {
		CommonUtils.checkErrorInProbeLogs(probeConfig);
	}

	protected void verifyCollectorStartup() {

		String keyword = "Introscope Agent startup complete";
		LogUtils util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);
		Assert.assertTrue(util.isKeywordInLog());
	}

	protected void resetCollectorAgentConfigToOriginal() {
		// revert to original profile
		CommonUtils.revertAppConfigFileToOriginal(umAgentConfig);
		// and re-initialize the config file object
		initUMAgentConfig();
	}

	protected void resetNodeJsProbeConfigToOriginal() {
		// revert to original profile
		CommonUtils.revertAppConfigFileToOriginal(probeConfig);
		// and re-initialize the config file object
		initProbeConfig();
	}

	public void testClassTeardown() {

	}
	
	public void disableUrlGroups() {
		umAgentConfig.updateProperty("introscope.agent.urlgroup.group.default.format", "Default");
		umAgentConfig.updateProperty("introscope.agent.backendpathgroup.group.default.format",
		        "Default");
	}

	@AfterSuite(alwaysRun = true)
	public void testSuiteTeardown() {
		if (mockEm != null) {
			mockEm.stop();
		}
		mockEm = null;
	}
}
