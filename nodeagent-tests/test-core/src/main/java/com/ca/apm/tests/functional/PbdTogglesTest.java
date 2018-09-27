package com.ca.apm.tests.functional;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.AutomationConstants;
import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;

public class PbdTogglesTest extends PbdConfigBaseTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(PbdTogglesTest.class);
	private int id = 1;

	@BeforeClass(alwaysRun = true)
    public void setup() {
        super.setup();        
        testSetName = this.getClass().getSimpleName();
        updateCollectorAndProbeLogFileName(testSetName);
    
        try {
            FileUtils.copy(umAgentConfig.getTogglesPbdPath(), 
                           umAgentConfig.getTogglesPbdPath() + ".backup");
        } catch (Exception e) { 
            LOGGER.error("Exception occurred while saving toggles backup copy: " + e.getMessage());
            e.printStackTrace();
        }
    }
	
	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName() + id++;
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
	}
	
	@Tas(snapshotPolicy=SnapshotPolicy.ALWAYS, snapshot=SnapshotMode.LIVE, testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "kurma05")
	@Test(dataProvider = "togglesTestData", groups = {"full", "nodeagent", "pbdconfig"})
    public void testToggles(String tracer, boolean isEnabled, String url,  
                            HttpRequestMethod requestMethod, int numberRequests, String metric, int expectedValue, 
                            ExpectedTraceElement[] expectedTrace, int numberTraces, String deepTraceEnabled) {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
		
	    //update pbd & start agents	   
        updateToggle(tracer, isEnabled);
        umAgentConfig.updateProperty("introscope.remoteagent.probe.agent.name", testMethodName);
        umAgentConfig.updateProperty(
                  AutomationConstants.Agent.INTELLIGENT_INSTRUMENTATION_ENABLE_PROPERTY, "true");
        umAgentConfig.updateProperty(
                  AutomationConstants.Agent.INTELLIGENT_INSTRUMENTATION_TRACE_ENABLED_PROPERTY, deepTraceEnabled);
        startCollectorAgent();
        startAppAndWaitConn(); 
         
        
        TraceValidationData traceValidationData = getTraceData(expectedTrace, numberTraces);
        
        //generate load
        makeHttpRequest(url, numberRequests, requestMethod);
        
        //validate data
        checkMetrics(metric, expectedValue);
        
        mockEm.processTraces(traceValidationData);
        checkErrorInLogs();
    }
	
	@AfterMethod(alwaysRun = true)
    public void executeAfterMethod() {
        
	    try {
            FileUtils.copy(umAgentConfig.getTogglesPbdPath() + ".backup",
                           umAgentConfig.getTogglesPbdPath());
        } catch (Exception e) { 
            LOGGER.error("Exception occurred while reverting toggles backup: " + e.getMessage());
            e.printStackTrace();
        }
	    
        stopAppAndWaitDisc();  
        stopCollectorAgentAndWaitDisc();
    }
	
	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {	  
		resetCollectorAgentConfigToOriginal();
		super.testClassTeardown();
	}
}
