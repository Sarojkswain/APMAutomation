package com.ca.apm.tests.functional;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.automation.common.mockem.ITraceElement;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

public class StallsTest extends BaseNodeAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StallsTest.class);
	
	@BeforeClass(alwaysRun = true)
	public void setup() {
	    
	    testSetName = this.getClass().getSimpleName();
		updateCollectorAndProbeLogFileName(testSetName);
		umAgentConfig.updateProperty("introscope.agent.stalls.thresholdseconds", "1");
        umAgentConfig.updateProperty("introscope.agent.stalls.resolutionseconds", "1");
        
        startCollectorAgent();
		startAppAndWaitConn();
	}

	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
	public void testStallsTimeout_Frontends() {

		makeHttpRequest("/stallService/timeout?duration=3000", 1);
		checkMetrics("Frontends|Apps|server|URLs|Default:Stall Count", 1);		
		checkErrorInLogs();
	}
	
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
	public void testStallsTimeout_Express() {

        makeHttpRequest("/stallService/timeout?duration=3000", 1);
        checkMetrics("Express|/stallService/timeout/|GET:Stall Count", 1);        
        checkErrorInLogs();
    }
	
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
	public void testStallsTimeout_Event() {

        TraceValidationData traceValidationData = getTraceData(STALL_EVENT_TIMEOUT, 1);
        makeHttpRequest("/stallService/timeout?duration=3000", 1);       
        mockEm.processTraces(traceValidationData);        
        checkErrorInLogs();
    }
		
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallWithinHttpResponse_Frontends() {

        makeHttpRequest("/mysqlStallService1?duration=3", 1);
        checkMetrics("Frontends|Apps|server|URLs|Default:Stall Count", 1);        
        checkErrorInLogs();
    }
		
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallWithinHttpResponse_CalledBackends() {

        makeHttpRequest("/mysqlStallService1?duration=3", 1);
        checkMetrics("Frontends|Apps|server|URLs|Default|Called Backends|mysql on localhost-3306 (MySQL DB):Stall Count", 1);
        checkErrorInLogs();
    }
		
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallWithinHttpResponse_BackendsSummary() {

        makeHttpRequest("/mysqlStallService1?duration=3", 1);       
        checkMetrics("Backends|mysql on localhost-3306 (MySQL DB):Stall Count", 1);
        checkErrorInLogs();
    }
	
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallWithinHttpResponse_BackendsSQL() {

        makeHttpRequest("/mysqlStallService1?duration=3", 1);        
        checkMetrics("Backends|mysql on localhost-3306 (MySQL DB)|SQL|Dynamic|Query|SELECT SLEEP (?):Stall Count", 1);
        checkErrorInLogs();
    }
		
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallWithinHttpResponse_Event() {

        TraceValidationData traceValidationData = getTraceData(STALL_EVENT_MYSQL, 1);
        makeHttpRequest("/mysqlStallService1?duration=3", 1);
        mockEm.processTraces(traceValidationData);
        checkErrorInLogs();
    }
	
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallAfterHttpResponse_Frontends() {

        makeHttpRequest("/mysqlStallService2?duration=3", 1);
        checkMetrics("Frontends|Apps|server|URLs|Default:Stall Count", 0);        
        checkErrorInLogs();
    }
    
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallAfterHttpResponse_Backends() {

        makeHttpRequest("/mysqlStallService2?duration=3", 1);
        checkMetrics("Backends|mysql on localhost-3306 (MySQL DB):Stall Count", 0);        
        checkErrorInLogs();
    }
    
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallAfterHttpResponse_Event() {

        TraceValidationData traceValidationData = getTraceData(STALL_EVENT_MYSQL, 0);
        makeHttpRequest("/mysqlStallService2?duration=3", 1);
        mockEm.processTraces(traceValidationData);        
        checkErrorInLogs();
    }
		
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallBeforeHttpResponse_Frontends() {

        makeHttpRequest("/mysqlStallService3?duration=3", 1);
        checkMetrics("Frontends|Apps|server|URLs|Default:Stall Count", 0);        
        checkErrorInLogs();
    }
    
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallBeforeHttpResponse_Backends() {

        makeHttpRequest("/mysqlStallService3?duration=3", 1);
        checkMetrics("Backends|mysql on localhost-3306 (MySQL DB):Stall Count", 0);        
        checkErrorInLogs();
    }
    
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(groups = {"bat", "nodeagent", "stalls"})
    public void testDbStallBeforeHttpResponse_Event() {

        TraceValidationData traceValidationData = getTraceData(STALL_EVENT_MYSQL, 0);
        makeHttpRequest("/mysqlStallService3?duration=3", 1);
        mockEm.processTraces(traceValidationData);        
        checkErrorInLogs();
    }
    
	private void checkMetrics(String metricPath, long maxValue) {
	    
	    LOGGER.info("Checking metric: " + metricPath);
	    long waitTime = 60000;
        MetricAssertionData metricData = new MetricAssertionData.
                       MaxDataPointBuilder(metricPath, maxValue).setDuration(waitTime).build();
        mockEm.processMetrics(metricData);
	}

	private TraceValidationData getTraceData(ExpectedTraceElement[] expectedTrace,
	                                         int numExpected) {
	  
        final List<ITraceElement[]> snapshotList = new ArrayList<ITraceElement []>();
        snapshotList.add(expectedTrace);
        
        TraceValidationData traceValidationData = new TraceValidationData.Builder(
            numExpected, new RequestProcessor.ITransactionTraceValidator() {
                public boolean validate(TransactionComponentData t) {
                    
                    LOGGER.info(SIUtils.dumpTrace(t,0,"",true)); 
                        
                    if (SIUtils.isStallSnapshot(t)) {
                        return TraceCompareUtil.compareTraceToPatterns(t, snapshotList);
                    }
                    return false;
                }
            }).build();

        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username", "dummy");
        mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);
        return traceValidationData;
    }
	
	private static final ExpectedTraceElement[] STALL_EVENT_TIMEOUT = {
        new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default", "Stalled Transaction"),
        new ExpectedTraceElement("Express\\|/stallService/timeout/\\|GET", "Stalled Transaction")
    };
	
	private static final ExpectedTraceElement[] STALL_EVENT_MYSQL = {
        new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default", "Stalled Transaction"),
        new ExpectedTraceElement("Express\\|/mysqlStallService1/\\|GET", "Stalled Transaction"),
        new ExpectedTraceElement("Backends\\|mysql on localhost-3306 \\(MySQL DB\\)", "Stalled Transaction"),
        new ExpectedTraceElement("Backends\\|mysql on localhost-3306 \\(MySQL DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT SLEEP \\(\\?\\)", "Stalled Transaction")
    };
	 
	private void makeHttpRequest(String path, int numRequests) {

		String url = appUrlBase + path;
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url).setNumberReqs(numRequests)
		        .build();
		txnGen.start();
	}
	
	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
	    
	    stopAppAndWaitDisc();  
        stopCollectorAgentAndWaitDisc();
        resetCollectorAgentConfigToOriginal();
		super.testClassTeardown();
	}
}
