package com.ca.apm.tests.functional;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Tests for various communication scenarios between probe arf client and arf
 * server
 * 
 * @author sinka08
 *
 */
@Test(groups = { "nodeagent", "probeCommunication" })
public class ProbeCommunicationTest extends BaseNodeAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProbeCommunicationTest.class);
	private static final String ARF_SERVER_PATH = "/dummyArfServer";
	private static final String DELIVER_MESSAGE_PATH = ARF_SERVER_PATH + "/deliverMessage";
	private static final String EMIT_MESSAGE_REGEX = ".*emitting 'arf-message' event for message:.*";
	private static final String PARSING_EXCEPTION_MESSAGE_REGEX = ".*got exception: .* while processing message.*";
	private String arfServerUrl;
	private String deliverMessageUrl;
	private String arfSpeakUrl;

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		arfServerUrl = appUrlBase + ARF_SERVER_PATH;
		deliverMessageUrl = appUrlBase + DELIVER_MESSAGE_PATH;
		arfSpeakUrl = appUrlBase + ARF_SERVER_PATH + "/speak";
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		probeConfig.updateLogFileName(testSetName + "-" + testMethodName + LOG_FILE_EXT);
		startAppAndWaitConn();
		startDummyArfServer();
	}

	/**
	 * test a single json message gets successfully parsed by arf parser
	 */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke", "nodeagent", "probeCommunication" }, priority = 2)
	public void testSingleJSONMessage() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String speakMessage = "{\"op\":\"speak\"}";
		deliverSimpleMessage(speakMessage);

		String configMessage1 = "{\"op\":\"config\",\"cmd\":\"map\",\"fn\":\"MyObject.MyMethod\",\"fnid\":342}";
		deliverSimpleMessage(configMessage1);

		String configMessage2 = "{\"op\":\"config\",\"cmd\":\"require\",\"prms\":{\"0\":\"mysql#query\"},\"mod\":\"mysql4\"}";
		deliverSimpleMessage(configMessage2);

		// wait for delivery
		Util.sleep(1000);

		// check log messages to verify that message parsing was successful
		List<String> expectedMessages = new ArrayList<>();
		expectedMessages.add(EMIT_MESSAGE_REGEX
		        + speakMessage.replace("{", "\\{").replace("}", "\\}"));
		expectedMessages.add(EMIT_MESSAGE_REGEX
		        + configMessage1.replace("{", "\\{").replace("}", "\\}"));
		expectedMessages.add(EMIT_MESSAGE_REGEX
		        + configMessage2.replace("{", "\\{").replace("}", "\\}"));
		checkExpectedLogMessages(expectedMessages);

		checkParsingExceptionInProbeLogs();

		// now check for any errors in log
		checkErrorInProbeLogs();
	}

	/**
	 * test a multiple json messages in one tcp data chunk are successfully
	 * parsed by arf parser
	 */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke", "nodeagent", "probeCommunication" }, priority = 2)
	public void testMultipleJSONMessages() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String configMessage1 = "{\"op\":\"config\",\"fnid\":1}";
		String configMessage2 = "{\"op\":\"config\",\"fnid\":2}";

		// 2 messages in one data chunk
		deliverRawMessage(configMessage1 + "\n" + configMessage2 + "\n");

		// wait for delivery
		Util.sleep(1000);

		// check log messages to verify that data is parsed into 2 messages
		List<String> expectedMessages = new ArrayList<>();
		expectedMessages.add(EMIT_MESSAGE_REGEX
		        + configMessage1.replace("{", "\\{").replace("}", "\\}"));
		expectedMessages.add(EMIT_MESSAGE_REGEX
		        + configMessage2.replace("{", "\\{").replace("}", "\\}"));
		checkExpectedLogMessages(expectedMessages);

		checkParsingExceptionInProbeLogs();

		// now check for any errors in probe log
		checkErrorInProbeLogs();
	}

	/**
	 * test one complete json message sent over in multiple tcp data chunks,
	 * gets parsed successfully by arf parser
	 */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "deep", "nodeagent", "probeCommunication" }, priority = 3)
	public void testSingleJSONMessageMultipleChunks() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		// stop speak messaging to avoid corruption of config message
		updateSpeakMessagingStatus("stop");
		
		String partialMessage1 = "{\"op\":\"config\",\"cmd\":\"require\",";
		deliverRawMessage(partialMessage1);
		String partialMessage2 = "\"prms\":{\"0\":\"mysql#query\"},\"mod\":\"mysql4\"}";
		deliverRawMessage(partialMessage2 + "\n");

		String fullMessage = partialMessage1 + partialMessage2;
		
		// wait for delivery
		Util.sleep(500);

		// start speak messaging between collector and probe
		updateSpeakMessagingStatus("start");

		// check log messages to verify that data is parsed successfully as
		// single full message
		List<String> expectedMessages = new ArrayList<>();
		expectedMessages.add(EMIT_MESSAGE_REGEX
		        + fullMessage.replace("{", "\\{").replace("}", "\\}"));
		checkExpectedLogMessages(expectedMessages);

		checkParsingExceptionInProbeLogs();

		// now check for any errors in probe log
		checkErrorInProbeLogs();
	}

	/**
	 * test invalid json message does not cause application crash
	 */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full", "nodeagent", "probeCommunication" }, priority = 4)
	public void testInvalidJSONMessage() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
		String invalidMessage = "{\"op\":\"config\",\"testid\"=1}";

		deliverSimpleMessage(invalidMessage);

		// wait for delivery
		Util.sleep(1000);

		// check log messages to verify that data is parsed into 2 messages
		List<String> expectedMessages = new ArrayList<>();
		expectedMessages.add(PARSING_EXCEPTION_MESSAGE_REGEX
		        + invalidMessage.replace("{", "\\{").replace("}", "\\}") + ".*");
		checkExpectedLogMessages(expectedMessages);

		// now check for any errors in probe log
		checkErrorInProbeLogs();
	}

	private void checkParsingExceptionInProbeLogs() {
		List<String> unexpectedMessages = new ArrayList<>();
		unexpectedMessages.add(PARSING_EXCEPTION_MESSAGE_REGEX);
		checkUnexpectedLogMessages(unexpectedMessages);
	}

	private void checkExpectedLogMessages(List<String> expectedMessages) {
		try {
			String path = probeConfig.getLogPath();
			for (String msg : expectedMessages) {
				assertTrue(Util.findPattern(path, msg),
				        String.format("Message '%s' was not found in log: %s", msg, path));
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while verifying log messages");
		}
	}

	private void checkUnexpectedLogMessages(List<String> unexpectedMessages) {
		try {
			String path = probeConfig.getLogPath();
			for (String msg : unexpectedMessages) {
				assertFalse(Util.findPattern(path, msg),
				        String.format("Message '%s' was found in log: %s", msg, path));
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while verifying log messages");
		}
	}

	/**
	 * delivers a single message to arf-server. Message will be automatically
	 * suffixed by eol character which is used to as delimiter to identify
	 * individual json messages.
	 * 
	 * @param message
	 */
	private void deliverSimpleMessage(String message) {
		deliverMessageToProbe(message, "simple");
	}

	private void deliverRawMessage(String message) {
		deliverMessageToProbe(message, "raw");
	}

	private void deliverMessageToProbe(String message, String type) {
		try {
			Map<String, String> postParams = new HashMap<String, String>();
			postParams.put("message", message);
			postParams.put("type", type);
			HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(deliverMessageUrl)
			        .setHttpMethod(HttpRequestMethod.POST).setParams(postParams)
			        .setNumberReqs(1).build();
			txnGen.start();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while delivering message to arf server");
		}
	}
	
	private void updateSpeakMessagingStatus(String status) {
		try {
			HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(arfSpeakUrl + "?action=" + status)
			        .setDelayBetweenReqs(0).build();
			txnGen.start();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail(String.format("exception while setting speak status: " + status));
		}
	}

	private void startDummyArfServer() {
		try {
			HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(arfServerUrl
			        + "?action=start&port=5005").build();

			// make txn to start server
			FutureTask<TxnLoadReport> result = txnGen.startSync();
			assertTrue(result.isDone());
			TxnLoadReport data = result.get();
			assertTrue(data.getNumOKResponses() > 0,
			        "could not make request to start arf server");

			// verify successful startup
			String path = probeConfig.getLogPath();
			String connectedMessage = ".*\"op\":\"speak\".*";

			long maxWait = 60000;
			long finishTime = System.currentTimeMillis() + maxWait;
			boolean isStarted = false;

			while (System.currentTimeMillis() < finishTime) {
				if (Util.findPattern(path, connectedMessage)) {
					isStarted = true;
					break;
				}
				Util.sleep(500);
			}

			assertTrue(isStarted, "dummy arf server could not start");

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while starting dummy arf server");
		}
	}

	private void stopDummyArfServer() {
		try {
			HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(arfServerUrl + "?action=stop")
			        .build();
			txnGen.start();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail(String.format("exception while stopping dummy arf server"));
		}

	}

	protected void startAppAndWaitConn() {
		startNodeApp();

		try {
			long maxWait = 60000;
			long finishTime = System.currentTimeMillis() + maxWait;
			boolean requestSucceeded = false;
			HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(arfServerUrl + "?action=no-op")
			        .build();

			do {
				Util.sleep(2000);

				FutureTask<TxnLoadReport> result = txnGen.startSync();
				assertTrue(result.isDone());
				TxnLoadReport data = result.get();
				if (data.getNumOKResponses() > 0) {
					requestSucceeded = true;
					break;
				}
			} while (System.currentTimeMillis() < finishTime);

			assertTrue(requestSucceeded,
			        String.format("node app could not start in %s ms", maxWait));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		stopDummyArfServer();
		stopNodeApp();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		super.testClassTeardown();

	}
}
