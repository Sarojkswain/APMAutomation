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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.automation.common.mockem.ITraceElement;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory.AggregatedMetricValueValidator;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.ExecutionMode;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
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
 * MongoDBTest class includes functional tests for verifying monitoring of
 * NodeJS Application's MongoDB Backend by APM NodeJS probe for mongodb driver
 * 2.1.x.
 *
 * @author sinka08@ca.com
 */

@Test(enabled = true)
public class MongoDBTest extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBTest.class);
	private long waitTime = 60000;

	@BeforeClass(enabled = true, alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "bat" })
	public void testFindRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 2;
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		invokeFindOp(numRequests);

		String metricPath = "Backends|tixchange (MongoDB)|Read Operations|find"
		        + METRIC_NAME_DELIMETER + BlameMetricType.RPI;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, numRequests), true);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "smoke" })
	public void testReadOpsSummaryRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 2;
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		invokeFindOp(numRequests);

		String summaryMetricPath = "Backends|tixchange (MongoDB)" + METRIC_NAME_DELIMETER
		        + BlameMetricType.RPI;

		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        MetricValidatorFactory.getMinMetricValidator(summaryMetricPath, 1), true);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "smoke" })
	public void testReadOpsRPINoFrontend() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);
		int numRequests = 2;
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		invokeFindOp(numRequests);

		String summaryMetricPath = "Backends|tixchange (MongoDB)" + METRIC_NAME_DELIMETER
		        + BlameMetricType.RPI;

		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        MetricValidatorFactory.getMinMetricValidator(summaryMetricPath, 1), true);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "bat" })
	public void testInsertRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 2;
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		invokeInsertOp(numRequests);

		String metricPath = "Backends|tixchange (MongoDB)|Write Operations|insert"
		        + METRIC_NAME_DELIMETER + BlameMetricType.RPI;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, numRequests), true);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "smoke" })
	public void testWriteOpsSummaryRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 2;
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		invokeInsertOp(numRequests);

		String metricPath = "Backends|tixchange (MongoDB)" + METRIC_NAME_DELIMETER
		        + BlameMetricType.RPI;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, numRequests), true);
		checkErrorInLogs();
	}

	/* Transaction Trace Tests */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testTxnTraceFindOp() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(buildExpectedTraceForFindOp());
		TraceValidationData traceValidationData = new TraceValidationData.Builder(numRequests,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {

				        return TraceCompareUtil.compareTraceToPatterns(t, expectedTraces);
			        }
		        }).build();

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		invokeFindOp(numRequests);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testTxnTraceInsertOp() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(buildExpectedTraceForInsertOp());
		TraceValidationData traceValidationData = new TraceValidationData.Builder(numRequests,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {

				        return TraceCompareUtil.compareTraceToPatterns(t, expectedTraces);
			        }
		        }).build();

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		invokeInsertOp(numRequests);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	private void generateGetLoad(String url, int numReqs) {
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + url).setNumberReqs(
		        numReqs).build();
		txnGen.start();
	}

	private void generatePostLoad(String url, int numReqs, Map<String, String> params) {
		long delay = 100;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + url)
		        .setHttpMethod(HttpRequestMethod.POST).setParams(params)
		        .setExecutionMode(ExecutionMode.SEQUENTIAL).setNumberReqs(numReqs)
		        .setDelayBetweenReqs(delay).build();
		txnGen.start();
	}

	private void invokeFindOp(int numRequests) {
		generateGetLoad("/rest/clickstream?user=ryan", numRequests);
	}

	private void invokeInsertOp(int numRequests) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user", "ryan");
		params.put("timestamp", "123");
		params.put("elapsed", "123");
		generatePostLoad("/rest/clickstream", numRequests, params);
	}

	private ITraceElement[] buildExpectedTraceForFindOp() {
		String metricPath1 = "Backends|tixchange (MongoDB)|Read Operations|find";
		String[][] params1 = { { "Class", "mongodbCollection" },
		        { "CollectionName", "clickstreams" }, { "DbName", "tixchange" },
		        { "Method", "find" }, { "DbServer", "localhost:27017" } };

		String metricPath2 = "Backends|tixchange (MongoDB)|Read Operations|toArray";
		String[][] params2 = { { "Class", "mongodbCursor" }, { "CollectionName", "clickstreams" },
		        { "DbName", "tixchange" }, { "Method", "toArray" } };

		ExpectedTraceElement[] trace = { new ExpectedTraceElement("Frontends\\|Apps.*"),
		        new ExpectedTraceElement("Express.*"),
		        new ExpectedTraceElement(normalizeMetricPath(metricPath1), params1),
		        new ExpectedTraceElement(normalizeMetricPath(metricPath2), params2) };
		return trace;
	}

	private ITraceElement[] buildExpectedTraceForInsertOp() {
		String metricPath1 = "Backends|tixchange (MongoDB)|Write Operations|insert";
		String[][] params1 = { { "Class", "mongodbCollection" },
		        { "CollectionName", "clickstreams" }, { "DbName", "tixchange" },
		        { "Method", "insert" }, { "DbServer", "localhost:27017" } };

		ExpectedTraceElement[] trace = { new ExpectedTraceElement("Frontends\\|Apps.*"),
		        new ExpectedTraceElement("Express.*"),
		        new ExpectedTraceElement(normalizeMetricPath(metricPath1), params1) };
		return trace;
	}

	private String normalizeMetricPath(String path) {
		return path.replace("|", "\\|").replace("(", "\\(").replace(")", "\\)");
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		stopAppAndWaitDisc();
		if (tixChangeConfig.getProperty("useRequestQueue") != "false") {
			tixChangeConfig.updateProperty("useRequestQueue", Boolean.FALSE);
		}
		stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		resetCollectorAgentConfigToOriginal();
		super.testClassTeardown();
	}
}
