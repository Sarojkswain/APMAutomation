package com.ca.apm.tests.functional;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.Util;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.HttpTxnGen.TxnLoadReport;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;

/**
 * Tests for verifying that error in probe logic does not cause application
 * crash or transaction failure
 * 
 * @author sinka08
 *
 */
@Test(groups = { "nodeagent", "probeerror" })
public class ProbeSafetyTest extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProbeSafetyTest.class);
	private static final String TEST_PROBE_FILE_NAME = "dummy-module.js";
	private static final String TEST_PROBE_PATH = "/probes/" + TEST_PROBE_FILE_NAME;
	private static final String TEST_MODULE_NAME = "dummy-module";
	private static final String TEST_MODULE_PATH = "/modules/" + TEST_MODULE_NAME;
	private static final String FAILURE_MESSAGE_UNFORMATTED = "http request was not successfull because of error caused by '%s' probe hook";
	private static final String LOG_MESSAGE_UNFORMATTED = "got exception: '%s' while executing '%s' trace logic in function";

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		copyTestProbe();
	}

	private void copyTestProbe() {
		File testProbeFile = new File(NodeJSAgentTestbed.TESTDATA_DEPLOY_DIR, TEST_PROBE_PATH);
		Assert.assertTrue(testProbeFile.exists(),
		        "could not find test probe: " + testProbeFile.getAbsolutePath());

		File testModuleDir = new File(NodeJSAgentTestbed.TESTDATA_DEPLOY_DIR, TEST_MODULE_PATH);
		Assert.assertTrue(testModuleDir.exists(),
		        "could not find test module: " + testProbeFile.getAbsolutePath());

		File probesDir = new File(probeConfig.getProbeHome());
		File modulesDir = new File(tixChangeConfig.getNodeModulesDir());

		try {
			FileUtils.copyFileToDirectory(testProbeFile, probesDir);
			FileUtils.copyDirectoryToDirectory(testModuleDir, modulesDir);
		} catch (IOException e) {
			String message = String.format("exception while copying %s to %s",
			        testProbeFile.getAbsolutePath(), probesDir.getAbsolutePath());
			LOGGER.error(message, e);
			fail(message);
		}
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testErrorInBeforeHook() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String errorMessage = "TypeError: .* is not a function";
		String hookPlace = "before";
		int numRequests = 1;
		boolean txnSucceeded = makeUrlRequest("/errorService/beforeHookError", numRequests);

		// verify txn did not fail because of probe logic error
		Assert.assertTrue(txnSucceeded, String.format(FAILURE_MESSAGE_UNFORMATTED, hookPlace));

		String logMsgRegex = ".*" + String.format(LOG_MESSAGE_UNFORMATTED, errorMessage, hookPlace)
		        + ".*";
		checkLogMessagePresence(logMsgRegex);

		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testErrorInAfterHook() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String errorMessage = "erroneous probe logic";
		String hookPlace = "after";
		int numRequests = 1;
		boolean txnSucceeded = makeUrlRequest("/errorService/afterHookError", numRequests);

		Assert.assertTrue(txnSucceeded, String.format(FAILURE_MESSAGE_UNFORMATTED, hookPlace));

		String logMsgRegex = ".*" + String.format(LOG_MESSAGE_UNFORMATTED, errorMessage, hookPlace)
		        + ".*";
		checkLogMessagePresence(logMsgRegex);

		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testErrorInCallbackHook() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String errorMessage = "TypeError: Cannot read property 'headers' of undefined";
		String hookPlace = "before";
		int numRequests = 1;
		boolean txnSucceeded = makeUrlRequest("/errorService/callbackError", numRequests);

		Assert.assertTrue(txnSucceeded, String.format(FAILURE_MESSAGE_UNFORMATTED, hookPlace));

		String logMsgRegex = ".*" + String.format(LOG_MESSAGE_UNFORMATTED, errorMessage, hookPlace)
		        + ".*";
		checkLogMessagePresence(logMsgRegex);

		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	private void checkLogMessagePresence(String logMsgRegex) {
		try {
			// expected log messages
			String[] expectedMessages = { logMsgRegex };
			String path = probeConfig.getLogPath();
			for (String msg : expectedMessages) {
				assertTrue(Util.findPattern(path, msg),
				        String.format("Error '%s' was not found in log: %s", msg, path));
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while verifying log messages");
		}
	}

	private boolean makeUrlRequest(String urlPath, int numRequests) {
		String url = appUrlBase + urlPath;

		try {
			HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url)
			        .setHttpMethod(HttpRequestMethod.GET).setNumberReqs(numRequests).build();
			TxnLoadReport report = txnGen.startSync().get();

			if (report.getNumOKResponses() == numRequests) {
				return true;
			}

		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(e.getMessage(), e);
			fail(String.format("url: %s syntax is incorrect", url));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail(e.getMessage());
		}

		return false;
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void removeTestProbe() {
		FileUtils.deleteQuietly(new File(probeConfig.getProbeHome(), TEST_PROBE_FILE_NAME));
		FileUtils.deleteQuietly(new File(tixChangeConfig.getNodeModulesDir(), TEST_MODULE_NAME));
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		super.testClassTeardown();
	}
}
