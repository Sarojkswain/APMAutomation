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

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.automation.common.AutomationConstants;
import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.automation.common.mockem.ITraceElement;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.RequestProcessor.ITransactionTraceValidator;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

/**
 * Http tests for node agent automation
 *
 * @author jinaa01
 */

@Test(groups = { "http", "nodeagent" })
public class HttpTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTests.class);
	private static final int HTTP_DEFAULT_PORT = 80;

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorAndProbeLogFileName(testMethodName);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "smoke", "http", "nodeagent" })
	public void testHttpFrontendRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		// verify collector agent successfully connected to mock em
		String keyword = "Connected controllable Agent to the Introscope Enterprise Manager";
		LogUtils util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);
		assertTrue(util.isKeywordInLog());

		// make some http transactions

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/api/Items/1")
		        .setNumberReqs(2).build();
		txnGen.start();

		String metricPathPrefix = "Frontends|Apps|server";
		String summaryMetricPath = metricPathPrefix + ":Responses Per Interval";
		String urlMetricPath = metricPathPrefix + "|URLs|Default:Responses Per Interval";
		long waitTime = 60000;

		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new MetricValidatorFactory.AggregatedMetricValueValidator(urlMetricPath, 2), true);

		// TODO need to support regexs for matching
		keyword = "\"fn\":\"http.GET\"";
		util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);
		assertTrue(util.isKeywordInLog());

		keyword = "\"prms\":{\"url\":\"/api/Items/1\"";
		util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);
		assertTrue(util.isKeywordInLog());

		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "bat", "http", "nodeagent" })
	public void testDefaultHttpPort() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int savedCurrentPort = Integer.valueOf(tixChangeConfig.getPort());
		int nRequests = 2;

		tixChangeConfig.updateProperty("port", HTTP_DEFAULT_PORT);
		tixChangeConfig.setPort(String.valueOf(HTTP_DEFAULT_PORT));

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String appUrlBase = String.format("http://%s", tixChangeConfig.getHost());

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/api/Items/1")
		        .setNumberReqs(nRequests).build();
		txnGen.start();

		// reset to saved port
		tixChangeConfig.updateProperty("port", savedCurrentPort);
		tixChangeConfig.setPort(String.valueOf(savedCurrentPort));

		String metricPathPrefix = "Frontends|Apps|server";
		String urlMetricPath = metricPathPrefix + "|URLs|Default:Responses Per Interval";
		long waitTime = 60000;

		mockEm.getReqProcessor(NODE_AGENT_EXPR)
		        .processMetrics(
		                waitTime,
		                new MetricValidatorFactory.AggregatedMetricValueValidator(urlMetricPath,
		                        nRequests), true);

		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "smoke", "http", "nodeagent" })
	public void testDefaultHttpPortTrace() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int savedCurrentPort = Integer.valueOf(tixChangeConfig.getPort());
		int nRequests = 1;

		tixChangeConfig.updateProperty("port", HTTP_DEFAULT_PORT);
		tixChangeConfig.setPort(String.valueOf(HTTP_DEFAULT_PORT));

		String appUrlBase = String.format("http://%s", tixChangeConfig.getHost());
		String resourcePath = "/api/Items/1";
		String metricPath = "Frontends|Apps|server|URLs|Default";
		final ITraceElement expectedElement = createExpectedFrontendElement(
		        CommonUtils.normalizeMetricPathForTraceComparison(metricPath), resourcePath,
		        String.valueOf(HTTP_DEFAULT_PORT));

		TraceValidationData traceValidationData = new TraceValidationData.Builder(nRequests,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {

				        return TraceCompareUtil.compareTraceComponentToPattern(t, expectedElement);
			        }
		        }).build();

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + resourcePath)
		        .setNumberReqs(nRequests).build();
		txnGen.start();

		// reset to saved port before trace validation
		tixChangeConfig.updateProperty("port", savedCurrentPort);
		tixChangeConfig.setPort(String.valueOf(savedCurrentPort));

		mockEm.processTraces(traceValidationData);

		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "deep", "nodeagent", "errors", "http" })
	public void testHttpErrorSnapshot() {
		LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(), "testHttpError");
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		// verfiyCollectorStartup();

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest")
		        .setNumberReqs(1).build();
		txnGen.start();

		final List<ITraceElement[]> httpScenario = new ArrayList<ITraceElement[]>();

		String[][] PARAMS1 = {
		        // regex for parameter value, to be used with TraceCompareUtil
				// which supports regex
		        { "Class", "http" },
		        { "Error Message",
		                "Frontends\\|Apps\\|server\\|URLs\\|Default: Node.Error: Http 404.*" },
		        { "Exception", "Frontends\\|Apps\\|server\\|URLs\\|Default: Node.Error: Http 404.*" },
		        { "Thread Group Name", "NA" }, { "Thread Name", "NA.*" } };

		ExpectedTraceElement[] expectedTrace = { new ExpectedTraceElement(
		        "Frontends\\|Apps\\|server\\|URLs\\|Default", PARAMS1), };

		httpScenario.add(expectedTrace);

		TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {
				        if (SIUtils.isErrorSnapshot(t) && !SIUtils.isStallSnapshot(t)) {
					        return TraceCompareUtil.compareTraceToPatterns(t, httpScenario);
				        } else {
					        return false;
				        }
			        }
		        }).build();

		mockEm.processTraces(traceValidationDataData);

		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "deep", "nodeagent", "errors", "http" })
	public void testHttpErrorMetrics() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest")
		        .setNumberReqs(1).build();
		txnGen.start();

		String metricPathPrefix = "Frontends|Apps|server";
		String urlMetricPath = metricPathPrefix + "|URLs|Default:Errors Per Interval";
		long waitTime = 60000;
		MetricAssertionData metricData = new MetricAssertionData.Builder(urlMetricPath, 1)
		        .setDuration(waitTime).build();
		mockEm.processMetrics(metricData);

		String keyword = "\"exc\":{\"class\":\"Http 404\"";
		LogUtils util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);

		assertTrue(CommonUtils.isKeywordInLog(util));
		
		// checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "deep", "nodeagent", "errors", "http" })
	public void testErrorTriggerAutoTraces() {

		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
		umAgentConfig.updateProperty(AutomationConstants.Agent.TT_SAMPLING_ENABLED_PROPERTY,
		        "false");
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest")
		        .setNumberReqs(2).build();
		txnGen.start();

		final List<ITraceElement[]> httpAutoTraceScenario = new ArrayList<ITraceElement[]>();

		String[][] PARAMS1 = {
		        // regex for parameter value, to be used with TraceCompareUtil
				// which supports regex
		        { "Class", "http" }, { "Error Message", "http::GET: Node.Error: Http 404.*" },
		        { "Exception", "http::GET: Node.Error: Http 404.*" },
		        { "Thread Group Name", "NA" }, { "Thread Name", "NA.*" },
		        { "Autotrace Trigger Criteria", "Error" } };

		ExpectedTraceElement[] expectedTrace = {
		        new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default", PARAMS1),
/*		        new ExpectedTraceElement("Backends\\|fs\\|readFile"),
		        new ExpectedTraceElement("Backends\\|fs\\|readFile") */};

		httpAutoTraceScenario.add(expectedTrace);

		TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {
				        if (SIUtils.isAutomaticTrace(t)) {
					        return TraceCompareUtil
					                .compareTraceToPatterns(t, httpAutoTraceScenario);
				        } else {
					        return false;
				        }
			        }
		        }).build();

		mockEm.processTraces(traceValidationDataData);

		checkErrorInLogs();
		// re-enable sampling
		umAgentConfig.updateProperty(AutomationConstants.Agent.TT_SAMPLING_ENABLED_PROPERTY,
		        "true");
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "deep", "nodeagent", "errors", "http" })
	public void testHttp399ResponseSuccess() {
		LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(),
		        "testHttp399ResponseSuccess");
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase
		        + "/rest/httpQuery?code=399").setNumberReqs(1).build();
		txnGen.start();

		ITransactionTraceValidator validator = new RequestProcessor.ITransactionTraceValidator() {
			public boolean validate(TransactionComponentData t) {
				if (SIUtils.isErrorSnapshot(t) && !SIUtils.isStallSnapshot(t)) {
					return true;
				}
				return false;
			}
		};

		RequestProcessor reqProcessor = mockEm.getReqProcessor(NODE_AGENT_EXPR);
		// should not get any trace validated - 0
		reqProcessor.processAllTracesInPeriod(0, 30000, validator);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "deep", "nodeagent", "errors", "http" })
	public void testHttp400ResponseError() {
		LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(),
		        "testHttp400ResponseError");
		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase
		        + "/rest/httpQuery?code=400").setNumberReqs(1).build();
		txnGen.start();

		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {
				        if (SIUtils.isErrorSnapshot(t) && !SIUtils.isStallSnapshot(t)) {
					        return true;
				        } else {
					        return false;
				        }
			        }
		        }).build();

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	private ITraceElement createExpectedFrontendElement(String componentName,
	        String urlResoucePath, String port) {
		String[][] frontendParams = { { "URL", urlResoucePath }, { "Server Port", port } };
		ExpectedTraceElement fronendElement = new ExpectedTraceElement(componentName,
		        frontendParams);
		return fronendElement;
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		resetCollectorAgentConfigToOriginal();
		super.testClassTeardown();
	}
}
