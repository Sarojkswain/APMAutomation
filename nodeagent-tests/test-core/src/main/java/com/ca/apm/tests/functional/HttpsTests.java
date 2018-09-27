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

import static com.ca.apm.tests.utils.MetricConstants.METRIC_NAME_DELIMETER;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.automation.common.mockem.ITraceElement;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory.AggregatedMetricValueValidator;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.config.BaseAppConfig;
import com.ca.apm.tests.config.HelloWorldAppConfig;
import com.ca.apm.tests.config.LoggingLevel;
import com.ca.apm.tests.config.NodeJSProbeConfig;
import com.ca.apm.tests.role.HelloWorldAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.MetricConstants.BlameMetricType;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

/**
 * Test for https monitoring on hello world app
 *
 * @author sinka08
 */

@Test(groups = { "https", "nodeagent" })
public class HttpsTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsTests.class);
	private static final String HELLO_WORLD_APP_FRONTEND_DEFAULT_URL_PATH = "Frontends|Apps|hello-world|URLs|Default";
	private static final int HTTPS_DEFAULT_PORT = 443;
	protected static NodeJSProbeConfig hwAppProbeConfig;
	protected static HelloWorldAppConfig hwAppConfig;

	@BeforeSuite(alwaysRun = true)
	public void setupHelloWorldApp() {
		initHelloWorldAppProbeConfig();
		initHelloWorldAppConfig();

		// create backup of original config
		BaseAppConfig[] appConfigs = { hwAppProbeConfig };
		CommonUtils.createBackupOfAppConfig(appConfigs);

		// copy logs from previous suite run
		CommonUtils.copyLogs(appConfigs);
	}

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		String fileName = testSetName + "-" + testMethodName;
		updateCollectorLogFileName(fileName);
		hwAppProbeConfig.updateLogFileName(fileName + LOG_FILE_EXT);
	}

	/* Metric Tests */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testDefaultHttpsPort() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = HELLO_WORLD_APP_FRONTEND_DEFAULT_URL_PATH + METRIC_NAME_DELIMETER
		        + BlameMetricType.RPI;
		// this url further makes https call to resource on same app
		String url = String.format("http://%s", hwAppConfig.getHost()) + "/httpToHttpsCaller";
		int numRequests = 2;
		int expectedRPI = 2 * numRequests;

		startCollectorAgentAndWaitConn();
		startHelloWorldAppAndWaitConn();
		makeHttpGetRequest(url, numRequests);

		long waitTime = 60000;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, expectedRPI), true);
		checkErrorInLogs();
	}

	/* Transaction Trace Tests */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testDefaultHttpsPortTrace() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String httpResPath = "/httpToHttpsCaller";
		String httpsResourcePath = "/time";
		
		// this url further makes https call to resource on same app
		String url = String.format("http://%s", hwAppConfig.getHost()) + httpResPath;
		final ITraceElement expectedElement = createExpectedTraceElement(httpsResourcePath, String.valueOf(HTTPS_DEFAULT_PORT));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {

				        return TraceCompareUtil.compareTraceComponentToPattern(t, expectedElement);
			        }
		        }).build();

		startCollectorAgentAndWaitConn();
		startHelloWorldAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		makeHttpGetRequest(url, numRequests);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	/* helper methods */
	private void makeHttpGetRequest(String url, int numRequests) {
		// generate load: execute http client request
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url).setNumberReqs(numRequests)
		        .build();
		txnGen.start();
	}

	private ITraceElement createExpectedTraceElement(String path, String port) {
		String[][] frontendParams = { { "URL", path }, { "Server Port", port } };
		ExpectedTraceElement fronendElement = new ExpectedTraceElement("Frontends\\|Apps.*",
		        frontendParams);
		return fronendElement;
	}

	private void initHelloWorldAppProbeConfig() {
		hwAppProbeConfig = new NodeJSProbeConfig(envProperties.getRolePropertyById(
		        NodeJSAgentTestbed.HELLOWORLD_PROBE_ROLE_ID,
		        NodeJSProbeRole.Builder.ENV_NODEJS_PROBE_HOME));
		hwAppProbeConfig.updateLogLevel(LoggingLevel.DEBUG);
	}

	private void initHelloWorldAppConfig() {
		String roleId = NodeJSAgentTestbed.HELLOWORLD_APP_ROLE_ID;
		hwAppConfig = new HelloWorldAppConfig(envProperties.getRolePropertyById(roleId,
		        HelloWorldAppRole.ENV_HELLOWORLD_APP_HOME_DIR), envProperties.getRolePropertyById(
		        roleId, HelloWorldAppRole.ENV_HELLOWORLD_APP_STARTUP_SCRIPT_PATH),
		        envProperties.getRolePropertyById(roleId,
		                HelloWorldAppRole.ENV_HELLOWORLD_APP_LOG_FILE));
		hwAppConfig.setHost(envProperties.getMachineHostnameByRoleId(roleId));
	}

	protected void startHelloWorldApp() {
		LOGGER.info("starting hello-world node app");
		runSerializedCommandFlowFromRole(NodeJSAgentTestbed.HELLOWORLD_APP_ROLE_ID,
		        HelloWorldAppRole.ENV_HELLOWORLD_APP_START);
	}

	protected void startHelloWorldAppAndWaitConn() {
		startHelloWorldApp();
		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	protected void stopHelloWorldApp() {
		LOGGER.info("stopping hello-world node app");
		runSerializedCommandFlowFromRole(NodeJSAgentTestbed.HELLOWORLD_APP_ROLE_ID,
		        HelloWorldAppRole.ENV_HELLOWORLD_APP_STOP);
	}

	protected void stopHelloWorldAppAndWaitDisc() {
		stopHelloWorldApp();
		Assert.assertTrue(isAgentDisconnected(NODE_AGENT_EXPR), "nodejs agent failed to disconnect");
	}

	protected void resetHelloWorldProbeConfigToOriginal() {
		// revert to original profile
		CommonUtils.revertAppConfigFileToOriginal(hwAppProbeConfig);
		// and re-initialize the config file object
		initHelloWorldAppProbeConfig();
	}

	protected void checkErrorInLogs() {
		checkErrorInCollectorLogs();
		CommonUtils.checkErrorInProbeLogs(hwAppProbeConfig);
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		stopHelloWorldAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		resetCollectorAgentConfigToOriginal();
		super.testClassTeardown();
	}
}
