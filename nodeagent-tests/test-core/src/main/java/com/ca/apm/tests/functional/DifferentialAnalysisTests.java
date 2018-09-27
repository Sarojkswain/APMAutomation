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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.Util;
import com.ca.apm.automation.common.mockem.ExpectedElement;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.FullAgentNameExpr;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.spec.agent.beans.autotracing.AgentMetricThresholdTrigger;
import com.wily.introscope.spec.agent.beans.autotracing.IAutoTracingTrigger;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

/**
 * This class includes functional tests for testing
 * Metric Threshold Autotrace functionality
 *
 * @author talma06
 */
public class DifferentialAnalysisTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DifferentialAnalysisTests.class);
	
	private static final String frontendMetricPath = "Frontends|Apps|server|URLs|Default";
	private static final String artPathSuffix = ":Average Response Time (ms)";
	private static final String frontendArtMetricPath = DifferentialAnalysisTests.frontendMetricPath+DifferentialAnalysisTests.artPathSuffix;
	private static final ArrayList<ExpectedElement[]> scenario = new ArrayList<ExpectedElement[]>();

	private static final ExpectedElement[][] expectedTrace = {
	                                                              {
	                                                                  new ExpectedElement(frontendMetricPath)
	                                                              }
	                           };
	
	private static final String httpGETRequestUrl = "/stallService?duration=";
	
	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		
		startCollectorAgent();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		probeConfig.updateLogFileName(method.getName() + LOG_FILE_EXT);
		//super.startNodeApp();
		startAppAndWaitConn();
	}
	
	/*
	 * @method:  testExpressGETMetrics 
	 * @usecase: Tests the HTTP GET method based Express Metrics
	 * 
	 * */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
	@Test(groups = { "bat", "nodeagent", "autotracing" })
	public void testAutoTracingThresholdTriggerBasic() 
	{
	    
        LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(), "testAutoTracingThresholdTriggerBasic");
	    
        verifyCollectorStartup();
        // Execute tixChange stall URL once to create Frontend metric in agent.
        excecuteHttpTrx(100, 1);
        // Verifiy that Frontend metric got created
        verifyMetricsUtil(frontendArtMetricPath);
        // Set auto tracing trigger threshold through mock EM, and verify successful result.
        RequestProcessor reqProcessor = mockEm.getReqProcessor(new FullAgentNameExpr(".*", "nodejs-probes",
                ".*"));        
        boolean result = reqProcessor.setAutoTracingTriggers(new IAutoTracingTrigger[] {new AgentMetricThresholdTrigger(frontendArtMetricPath, 500)});
        assertTrue(result,"Failed to set DA auto trace triggers.");
        // Execute transaction again three times, with longer stall than 
        // the threshold.
        excecuteHttpTrx(2000, 3);
        // Verify that MockEM receives a auto trace with threshold trigger property
        checkTraceParameterUtil("Autotrace Trigger Criteria", 
                                "Response time of Component '!BRIDGE!server@null|Frontends|Apps|server|URLs|Default' exceeded baseline threshold 500 ms");
        
        // Clear threshold triggers in agent and verify successful result.
        result =  reqProcessor.clearAutoTracingTriggers(new IAutoTracingTrigger[] {new AgentMetricThresholdTrigger(frontendArtMetricPath, 500)});
        assertTrue(result,"Failed to clear DA auto trace triggers.");
	}
	    
    private void checkTraceParameterUtil(final String  parameter, final String parameterValue)
    {   
        DifferentialAnalysisTests.scenario.add(DifferentialAnalysisTests.expectedTrace[0]);
  
        TraceValidationData traceValidationData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator() {
       
            public boolean validate(TransactionComponentData t)
            {   
                return DifferentialAnalysisTests.autoTraceParameterValidation(t, parameter, parameterValue);
                
            }
        }).build();

        mockEm.processTraces(traceValidationData);
        
    }

    private static boolean autoTraceParameterValidation(TransactionComponentData componentData, String traceParameter, String traceParameterValue)
    {
        LOGGER.info("Auto Trace Parameter being tested: KEY="+ traceParameter + " and VALUE=" + traceParameterValue);
        LOGGER.info(SIUtils.dumpTrace(componentData, 0, "", true));
        if (SIUtils.isAutomaticTrace(componentData) && componentData.getParameterNames().contains(traceParameter) && componentData.getParameterValue(traceParameter).equals(traceParameterValue)) {
            return SIUtils.containsAllComponents(componentData, DifferentialAnalysisTests.scenario);
        } else {
            return false;
        }
    }
	
    private void verifyMetricsUtil(String metric)
    {   
        String[] expected = new String[3];
        String path = umAgentConfig.getLogPath();
                            
        long waitTime = 60000;
        MetricAssertionData metricData = new MetricAssertionData.MinMaxBuilder(metric, 0, 100000)
                .setDuration(waitTime).build();
        mockEm.processMetrics(metricData);
       
        for (String msg : expected) 
        {
            try
            {
                if (msg != null) {
                    assertTrue(Util.findPattern(path, msg),
                        String.format("Error '%s' was not found in log: %s", msg, path));
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
       
    }
	
    
    public void excecuteHttpTrx(long duration, int count)
    {                
		String httpRequestUrl = DifferentialAnalysisTests.httpGETRequestUrl + duration;

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + httpRequestUrl)
		        .setHttpMethod(HttpRequestMethod.GET).setNumberReqs(count).build();
		txnGen.start(); 
    }
    
    
	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		super.stopNodeApp();
		super.checkErrorInLogs();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		stopCollectorAgent();
		super.testClassTeardown();
	}
}
