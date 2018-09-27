package com.ca.apm.tests.functional;

import static com.ca.apm.tests.config.NodeJSProbeConfig.HTTP_REQ_DEC_PROPERTY_KEY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.Util;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory.AdvCrossProcessTxnTraceValidator;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory.CrossProcessTxnTraceValidator;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.RequestProcessor.ITransactionTraceValidator;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.FullAgentNameExpr;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.TixChangeUtil;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;

/**
 * Tests for http transaction correlation between node-> java
 * 
 * @author sinka08
 *
 */
@Test(groups = { "nodeagent", "correlation", "http" })
public class HttpCorrelationTest extends BaseNodeAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpCorrelationTest.class);
	private static final String TEST_SESSION = "testsession";
	private static final String EMAIL_ID = "user1@users.com";
	private static final String LOGIN_API_NODE = "/httpService/account/login";
	private static final String LOGIN_API_TOMCAT = "/tixchangeRest/rest/account/login";

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "bat", "nodeagent", "correlation", "http" }, priority = 1)
	public void testNodeToJavaCorrelation() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
		        new AdvCrossProcessTxnTraceValidator(LOGIN_API_NODE, LOGIN_API_TOMCAT)).build();
		
		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		makeNodeToJavaCall();

		mockEm.processTraces(traceValidationData);

		checkLogMessages();
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke", "nodeagent", "correlation", "http" }, priority = 2)
	public void testNodeToNodeCorrelation() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String path1 = "/httpGetService";
		// path to rest api on node
		String path2 = "/api/Items/1";
		String url1 = appUrlBase + path1;
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
		        new AdvCrossProcessTxnTraceValidator(path1, path2)).build();

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		// generate load: execute cross process call from node -> node

		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("path", path2);
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url1)
		        .setHttpMethod(HttpRequestMethod.POST).setParams(postParams).setNumberReqs(1)
		        .build();
		txnGen.start();

		mockEm.processTraces(traceValidationData);

		checkLogMessages();
		checkErrorInLogs();
	}

	// this test should be executed last in this class, as it turns off
	// correlation
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full", "nodeagent", "correlation", "http" }, priority = 4)
	public void testHeaderDecFalse() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		LOGGER.info("current value for http request decoration property: "
		        + probeConfig.getProperty(HTTP_REQ_DEC_PROPERTY_KEY));
		probeConfig.updateProperty(HTTP_REQ_DEC_PROPERTY_KEY, Boolean.FALSE);

		LOGGER.info("updated value for http request decoration property: "
		        + probeConfig.getProperty(HTTP_REQ_DEC_PROPERTY_KEY));
		assertFalse(Boolean.valueOf(probeConfig.getProperty(HTTP_REQ_DEC_PROPERTY_KEY)),
		        String.format("value of key: '%s' in config.json is not false",
		                HTTP_REQ_DEC_PROPERTY_KEY));

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		RequestProcessor reqProcessor = mockEm.getReqProcessor(new FullAgentNameExpr(".*", ".*",
		        ".*"));
		reqProcessor.addTraceFilter(filter);

		makeNodeToJavaCall();

		ITransactionTraceValidator validator = new CrossProcessTxnTraceValidator(LOGIN_API_NODE,
		        LOGIN_API_TOMCAT);
		reqProcessor.processAllTracesInPeriod(0, 60000, validator);

		checkErrorInLogs();

		// re-enable correlation
		probeConfig.updateProperty(HTTP_REQ_DEC_PROPERTY_KEY, Boolean.TRUE);
	}

	private void makeNodeToJavaCall() {
		String session = TEST_SESSION;
		String emailId = EMAIL_ID;
		String path1 = LOGIN_API_NODE;
		// path to rest api on tomcat
		String path2 = LOGIN_API_TOMCAT;
		String url1 = appUrlBase + path1;

		TixChangeUtil.createCustomSession(appUrlBase, emailId, session);

		// generate load: execute cross process call from node -> java

		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("email", emailId);
		postParams.put("path", path2);
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url1)
		        .setHttpMethod(HttpRequestMethod.POST).setParams(postParams).setNumberReqs(1)
		        .build();
		txnGen.start();
	}

	private void checkLogMessages() {
		try {
			// inappropriate messages
			String[] unexpectedMessages = { ".*Received an older cross process data on wire.*" };
			String path = umAgentConfig.getLogPath();

			for (String msg : unexpectedMessages) {
				assertFalse(Util.findPattern(path, msg),
				        String.format("Error '%s' was found in log: %s", msg, path));
			}

			// expected log messages
			String[] expectedMessages = { ".*debug.*Decorated outgoing http request with correlation header.*" };
			path = probeConfig.getLogPath();
			for (String msg : expectedMessages) {
				assertTrue(Util.findPattern(path, msg),
				        String.format("Error '%s' was not found in log: %s", msg, path));
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while verifying log messages");
		}
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		super.testClassTeardown();
		probeConfig.updateProperty(HTTP_REQ_DEC_PROPERTY_KEY, Boolean.TRUE);
	}
}
