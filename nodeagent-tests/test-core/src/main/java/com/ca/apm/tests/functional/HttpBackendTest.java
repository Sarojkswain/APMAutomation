package com.ca.apm.tests.functional;

import static com.ca.apm.automation.common.AutomationConstants.Agent.BACKEND_PATHGROUP_FORMAT;
import static com.ca.apm.automation.common.AutomationConstants.Agent.BACKEND_PATHGROUP_KEYS;
import static com.ca.apm.automation.common.AutomationConstants.Agent.BACKEND_PATHGROUP_PATHPREFIX;
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
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.ExecutionMode;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.MetricAssertionData;
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
 * Tests for http client request monitoring
 * 
 * @author sinka08
 *
 */
@Test(groups = { "nodeagent", "httpbackend", "http" })
public class HttpBackendTest extends BaseNodeAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpBackendTest.class);
	private String httpBackendAggMetricNodePath = "Backends|WebService at http_//localhost_3000";
	private String httpBackendGroupMetricNodePathPrefix;
	private String httpBackendDefaultGroupMetricNodePath;
	private static final String HTTP_BACKEND_CLASS_NAME = "httpclient";
	private static final String HTTP_BACKEND_METHOD_NAME = "request";

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		httpBackendAggMetricNodePath = String.format("Backends|WebService at http_//%s_%s",
		        tixChangeConfig.getHost(), tixChangeConfig.getPort());
		httpBackendGroupMetricNodePathPrefix = httpBackendAggMetricNodePath + "|Paths";
		httpBackendDefaultGroupMetricNodePath = httpBackendGroupMetricNodePathPrefix + "|Default";
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
	}

	/* Metric Tests */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "bat" })
	public void testRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendDefaultGroupMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.RPI;
		testRPI(metricPath);
	}

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
    @Test(groups = { "bat" })
    public void testRPINoFrontend() {
        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

        String metricPath = httpBackendDefaultGroupMetricNodePath + METRIC_NAME_DELIMETER
                + BlameMetricType.RPI;
        testRPINoFrontend(metricPath);
    }	
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "bat" })
	public void testAggregatedRPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendAggMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.RPI;
		testRPI(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testART() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendDefaultGroupMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.ART;
		testART(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	private void testAggregatedART() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendAggMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.ART;
		testART(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testEPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendDefaultGroupMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.EPI;
		testEPI(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testAggregatedEPI() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendAggMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.EPI;
		testEPI(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(enabled = true, groups = { "full" })
	public void testConcurrentInvocations() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendDefaultGroupMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.CI;
		testConcurrentInvocations(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(enabled = true, groups = { "full" })
	public void testAggregatedConcurrentInvocations() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendAggMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.CI;
		testConcurrentInvocations(metricPath);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testEPIBoundaryCase() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String metricPath = httpBackendDefaultGroupMetricNodePath + METRIC_NAME_DELIMETER
		        + BlameMetricType.EPI;
		int numRequests = 1;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeHttpClientGetRequest(numRequests, "/rest/httpQuery?code=399");

		long waitTime = 60000;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetricsForDuration(waitTime,
		        MetricValidatorFactory.getMetricValidator(metricPath, 0));

		checkErrorInLogs();
	}

	/**
	 * test for verifying that we do find out and report correct default port
	 * when user do not specify it in the url.
	 */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testHttpDefaultPort() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 2;
		String metricPath = String
		        .format("Backends|WebService at %s_//%s_%d", "http", "google.com", 80)
		        + METRIC_NAME_DELIMETER + BlameMetricType.RPI;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeGetRequestToUrl(numRequests, "http://google.com/", true);

		long waitTime = 60000;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, numRequests), true);

		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testHttps() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 2;
		String metricPath = String.format("Backends|WebService at %s_//%s_%d", "https",
		        "encrypted.google.com", 443) + METRIC_NAME_DELIMETER + BlameMetricType.RPI;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeGetRequestToUrl(numRequests, "https://encrypted.google.com/", true);

		long waitTime = 60000;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, numRequests), true);

		checkErrorInLogs();
	}

	private void testRPI(String metricPath) {
		int numRequests = 2;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeHttpClientGetRequest(numRequests, "/api/Items/1");

		long waitTime = 60000;
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(metricPath, numRequests), true);

		checkErrorInLogs();
	}

    private void testRPINoFrontend(String metricPath) {
        int numRequests = 2;
        tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);
        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();
        makeHttpClientGetRequest(numRequests, "/api/Items/1");

        long waitTime = 60000;
        mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
                new AggregatedMetricValueValidator(metricPath, numRequests), true);

        checkErrorInLogs();
    }	
	
	private void testART(String metricPath) {
		int numRequests = 2;
		long responseTime = 5000;
		long buffer = 500;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeHttpClientGetRequest(numRequests, "/stallService/timeout?duration=" + responseTime);

		long waitTime = 60000;
		MetricAssertionData metricData = new MetricAssertionData.MinMaxBuilder(metricPath,
		        responseTime - buffer, responseTime + buffer).setDuration(waitTime).build();
		mockEm.processMetrics(metricData);

		checkErrorInLogs();
	}

	private void testEPI(String metricPath) {
		int numRequests = 1;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeHttpClientGetRequest(numRequests, "/notfound");

		long waitTime = 60000;
		MetricAssertionData metricData = new MetricAssertionData.Builder(metricPath, numRequests)
		        .setDuration(waitTime).build();
		mockEm.processMetrics(metricData);

		checkErrorInLogs();
	}

	private void testConcurrentInvocations(String metricPath) {
		int numRequests = 5;
		long responseTime = 35000;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();
		makeHttpClientGetRequest(numRequests, "/stallService/timeout?duration=" + responseTime,
		        true);

		long waitTime = 60000;
		MetricAssertionData metricData = new MetricAssertionData.Builder(metricPath, numRequests)
		        .setDuration(waitTime).build();
		mockEm.processMetrics(metricData);

		checkErrorInLogs();
	}

	/* Transaction Trace Tests */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testTxnTraceGetRequest() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String resourcePath = "/api/Items/1";
		String calledUrl = appUrlBase + resourcePath;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(createExpectedTrace(calledUrl, "GET"));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
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

		makeGetRequestWithOptions(numRequests, resourcePath);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}
	
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testTxnTraceBadPath() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String resourcePath = "/api/Items/1";
		String calledUrl = appUrlBase + resourcePath;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(createExpectedTrace(calledUrl, "GET"));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
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

	    // This really is not correct by Node.js http API page, but it seems that the http library takes it.
	    // So our probe must handle it too.
		// bad path: http://localhost:3000/api/Items/1
		makeGetRequestWithOptions(numRequests, calledUrl);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

    /* Transaction Trace Tests */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
    @Test(groups = { "smoke" })
    public void testTxnTraceGetRequestNoFrontend() {
        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

        int numRequests = 1;
        String resourcePath = "/api/Items/1";
        String calledUrl = appUrlBase + resourcePath;
        final List<ITraceElement[]> expectedTraces = new ArrayList<>();        
        ITraceElement[] frag1 = createExpectedFragmentTrace(calledUrl, "GET", httpBackendAggMetricNodePath);
        ITraceElement[] frag2 =  {
		        new ExpectedTraceElement("Fragments\\|fragment\\|NoContext"),
		        new ExpectedTraceElement("Express.*"),
		        new ExpectedTraceElement("loopbackDAO::ACL_find"),
		        new ExpectedTraceElement("loopbackDAO::Item_find_by_id"),
		        new ExpectedTraceElement("loopbackDAO::Item_find"),
		        new ExpectedTraceElement("Backends.*"),
		        new ExpectedTraceElement("Backends.*") };      
        expectedTraces.add(frag1);
        expectedTraces.add(frag2);
        
        TraceValidationData traceValidationData = new TraceValidationData.Builder(2,
                new RequestProcessor.ITransactionTraceValidator() {
                    public boolean validate(TransactionComponentData t) {

                        return TraceCompareUtil.compareTraceToPatterns(t, expectedTraces);
                    }
                }).build();

        startCollectorAgentAndWaitConn();
        tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);        
        startAppAndWaitConn();

        // Set transaction trace filter
        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
                "dummy");
        mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

        makeHttpClientGetRequest(numRequests, resourcePath);

        mockEm.processTraces(traceValidationData);
        checkErrorInLogs();
    }	
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testTxnTracePOSTRequest() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String resourcePath = "/api/Items";
		String calledUrl = appUrlBase + resourcePath;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(createExpectedTrace(calledUrl, "POST"));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
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

		makeHttpClientPostRequest(numRequests);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testTxnTracePutRequest() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String resourcePath = "/api/Items";
		String calledUrl = appUrlBase + resourcePath;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(createExpectedTrace(calledUrl, "PUT"));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
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

		makeHttpClientPutRequest(numRequests);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" })
	public void testTxnTraceWithError() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String resourcePath = "/notfound";
		String calledUrl = appUrlBase + resourcePath;
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();
		expectedTraces.add(createExpectedErrorTrace(calledUrl, "GET"));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
		        new RequestProcessor.ITransactionTraceValidator() {
			        public boolean validate(TransactionComponentData t) {

				        if (!SIUtils.isErrorSnapshot(t)) {
					        return TraceCompareUtil.compareTraceToPatterns(t, expectedTraces);
				        }
				        return false;
			        }
		        }).build();

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);

		makeHttpClientGetRequest(numRequests, resourcePath);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" })
	public void testTxnTraceHttpsRequest() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		int numRequests = 1;
		String calledUrl = "https://encrypted.google.com/";
		String aggMetricNodePath = String.format("Backends|WebService at %s_//%s_%d", "https",
		        "encrypted.google.com", 443);
		final List<ITraceElement[]> expectedTraces = new ArrayList<>();

		expectedTraces.add(createExpectedTrace(calledUrl, "GET", aggMetricNodePath));
		TraceValidationData traceValidationData = new TraceValidationData.Builder(1,
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

		makeGetRequestToUrl(numRequests, calledUrl, false);

		mockEm.processTraces(traceValidationData);
		checkErrorInLogs();
	}

	/* Url Grouping tests */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" }, priority = 4)
	public void testUrlGroupingPostRequest() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String groupName = "POST";

		// configure backend url grouping
		umAgentConfig.updateProperty(BACKEND_PATHGROUP_KEYS, groupName);
		umAgentConfig.updateProperty(
		        BACKEND_PATHGROUP_PATHPREFIX.replace("[key]", groupName), "/api/Items*");
		umAgentConfig.updateProperty(BACKEND_PATHGROUP_FORMAT.replace("[key]", groupName),
		        groupName);
		String metricPath = httpBackendGroupMetricNodePathPrefix + "|" + groupName
		        + METRIC_NAME_DELIMETER + BlameMetricType.RPI;

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		// tests response metric and grouping for POST request
		int numRequests = 1;
		makeHttpClientPostRequest(numRequests);

		long waitTime = 60000;
		MetricAssertionData metricData = new MetricAssertionData.Builder(metricPath, numRequests)
		        .setDuration(waitTime).build();
		mockEm.processMetrics(metricData);

		checkErrorInLogs();

		stopCollectorAgentAndWaitDisc();
		// revert to original profile/config
		resetCollectorAgentConfigToOriginal();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "deep" }, priority = 5)
	public void testUrlGroupingGetRequest() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String groupName = "items";

		// configure backend url grouping
		umAgentConfig.updateProperty(BACKEND_PATHGROUP_KEYS, groupName);
		umAgentConfig.updateProperty(
		        BACKEND_PATHGROUP_PATHPREFIX.replace("[key]", groupName), "/api/Items*");
		umAgentConfig.updateProperty(BACKEND_PATHGROUP_FORMAT.replace("[key]", groupName),
		        groupName);
		String metricPath = httpBackendGroupMetricNodePathPrefix + "|" + groupName
		        + METRIC_NAME_DELIMETER + BlameMetricType.RPI;
		// tests response metric and grouping for GET request
		testRPI(metricPath);

		checkErrorInLogs();

		stopCollectorAgentAndWaitDisc();
		// revert to original profile/config
		resetCollectorAgentConfigToOriginal();
	}

	/* helper methods */
	private void makeHttpClientGetRequest(int numRequests, String resourcePath) {
		makeHttpClientGetRequest(numRequests, resourcePath, false);
	}

	private void makeHttpClientGetRequest(int numRequests, String resourcePath, boolean parallelMode) {
		String path1 = "/httpGetService";
		String url1 = appUrlBase + path1;
		ExecutionMode mode = parallelMode ? ExecutionMode.PARALLEL : ExecutionMode.SEQUENTIAL;

		// generate load: execute http client request
		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("host", tixChangeConfig.getHost());
		postParams.put("path", resourcePath);
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url1)
		        .setHttpMethod(HttpRequestMethod.POST).setParams(postParams)
		        .setNumberReqs(numRequests).setExecutionMode(mode).build();

		txnGen.start();
	}
	
	private void makeGetRequestWithOptions(int numRequests, String resourcePath) {
		String path1 = "/httpGetService";
		String url1 = appUrlBase + path1;

		// generate load: execute http client request
		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("host", tixChangeConfig.getHost());
		postParams.put("path", resourcePath);
		postParams.put("optionsMode", "true");
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url1)
		        .setHttpMethod(HttpRequestMethod.POST).setParams(postParams)
		        .setNumberReqs(numRequests).build();

		txnGen.start();
	}

	private void makeGetRequestToUrl(int numRequests, String url, boolean parallelMode) {
		String path1 = "/httpGetService";
		String url1 = appUrlBase + path1;

		Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("url", url);
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url1)
		        .setHttpMethod(HttpRequestMethod.POST).setParams(postParams)
		        .setNumberReqs(numRequests).build();
		if (parallelMode) {
			txnGen.startAsync();
		} else {
			txnGen.start();
		}

	}

	private void makeHttpClientPostRequest(int numRequests) {
		makeCreateResourceRequest(numRequests, HttpRequestMethod.POST);
	}

	private void makeHttpClientPutRequest(int numRequests) {
		makeCreateResourceRequest(numRequests, HttpRequestMethod.PUT);
	}

	private void makeCreateResourceRequest(int numRequests, HttpRequestMethod method) {
		String path1 = "/httpService/Items";
		String url1 = appUrlBase + path1;

		// generate load: execute http client PUT/POST request

		Map<String, String> params = new HashMap<String, String>();
		params.put("host", tixChangeConfig.getHost());
		params.put("path", "/api/Items");
		params.put("itemid", "100");
		params.put("listprice", "848");
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url1).setHttpMethod(method)
		        .setParams(params).setNumberReqs(numRequests).build();

		txnGen.start();
	}

	private ITraceElement[] createExpectedTrace(String calledUrl, String httpMethod) {

		return createExpectedTrace(calledUrl, httpMethod, httpBackendAggMetricNodePath);
	}

	private ITraceElement[] createExpectedTrace(String calledUrl, String httpMethod,
	        String aggMetricNodePath) {

		String groupNodePath = aggMetricNodePath + "|Paths|Default";

		String[][] aggNodeParams = { { "Called URL", calledUrl },
		        { "Class", HTTP_BACKEND_CLASS_NAME }, { "HTTP Method", httpMethod },
		        { "HTTP Status Code", "200 - OK" }, { "Method", HTTP_BACKEND_METHOD_NAME } };

		String[][] groupNodeParams = aggNodeParams;

		ExpectedTraceElement[] trace = { new ExpectedTraceElement("Frontends\\|Apps.*"),
		        new ExpectedTraceElement("Express.*"),
		        new ExpectedTraceElement(normalizeMetricPath(aggMetricNodePath), aggNodeParams),
		        new ExpectedTraceElement(normalizeMetricPath(groupNodePath), groupNodeParams) };
		return trace;
	}

    
	private ITraceElement[] createExpectedFragmentTrace(String calledUrl, String httpMethod,
	        String aggMetricNodePath) {
		String groupNodePath = aggMetricNodePath + "|Paths|Default";

		String[][] aggNodeParams = { { "Called URL", calledUrl },
		        { "Class", HTTP_BACKEND_CLASS_NAME }, { "HTTP Method", httpMethod },
		        { "HTTP Status Code", "200 - OK" }, { "Method", HTTP_BACKEND_METHOD_NAME } };

		String[][] groupNodeParams = aggNodeParams;

		ExpectedTraceElement[] trace = {
		        new ExpectedTraceElement("Fragments\\|fragment\\|NoContext"),
		        new ExpectedTraceElement("Express.*"),
		        new ExpectedTraceElement(normalizeMetricPath(aggMetricNodePath), aggNodeParams),
		        new ExpectedTraceElement(normalizeMetricPath(groupNodePath), groupNodeParams) };
		return trace;
	}
   

	private ITraceElement[] createExpectedErrorTrace(String calledUrl, String httpMethod) {
		String[][] aggNodeParams = {
		        { "Called URL", calledUrl },
		        { "Class", HTTP_BACKEND_CLASS_NAME },
		        {
		                "Error Message",
		                normalizeMetricPath(httpBackendDefaultGroupMetricNodePath)
		                        + ": Node.Error: Http 404: Not Found" },
		        { "Exception", "Http 404: Not Found" }, { "HTTP Method", httpMethod },
		        { "HTTP Status Code", "404 - Not Found" }, { "Method", HTTP_BACKEND_METHOD_NAME } };

		String[][] groupNodeParams = aggNodeParams;

		ExpectedTraceElement[] trace = {
		        new ExpectedTraceElement("Frontends\\|Apps.*"),
		        new ExpectedTraceElement("Express.*"),
		        new ExpectedTraceElement(normalizeMetricPath(httpBackendAggMetricNodePath),
		                aggNodeParams),
		        new ExpectedTraceElement(
		                normalizeMetricPath(httpBackendDefaultGroupMetricNodePath), groupNodeParams) };
		return trace;
	}

	private String normalizeMetricPath(String path) {
		return path.replace("|", "\\|");
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
