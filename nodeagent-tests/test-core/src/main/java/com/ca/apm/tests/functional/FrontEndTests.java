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
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.Util;
import com.ca.apm.automation.common.mockem.ExpectedElement;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory.AggregatedMetricValueValidator;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
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
 * FrontTest class includes functional tests for verifying monitoring of
 * NodeJS Application's Frontend by APM NodeJS agent
 *
 * @author zheji01@ca.com
 */

public class FrontEndTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(FrontEndTests.class);
	private long waitTime = 60000;

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		startCollectorAgent();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		probeConfig.updateLogFileName(method.getName() + LOG_FILE_EXT);
		super.startAppAndWaitConn();
	}
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "bat", "nodeagent", "frontends" })
	public void testFrontEndRPI() {
		checkStartup();
        generateHTTP("/api/Items/1", 2);

		String metricPathPrefix = "Frontends|Apps|server";
		String urlMetricPath = metricPathPrefix + "|URLs|Default:Responses Per Interval";
		
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
		        new AggregatedMetricValueValidator(urlMetricPath, 2), true);
		checkKeywords("api/Items/1");
		checkErrorInLogs();
	}
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(groups = { "bat", "nodeagent", "frontends" })
	public void testFrontEndCI() {
	    checkStartup();
        generateHTTP("/stallService/timeout?duration=30000", 1);

	    String metricPathPrefix = "Frontends|Apps|server";
	    String urlMetricPath = metricPathPrefix + "|URLs|Default:Concurrent Invocations";
	        
	    MetricAssertionData metricData = new MetricAssertionData.Builder(urlMetricPath, 1).setDuration(waitTime).build();
	    mockEm.processMetrics(metricData);

	    checkKeywords("stallService/timeout");
	    checkErrorInLogs();
	}
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
    @Test(groups = { "deep", "nodeagent", "frontends" })
    public void testFrontEndTrace() {
        checkStartup();        
        
        final ArrayList<ExpectedElement[]> httpScenario = new ArrayList<ExpectedElement[]>();

        ExpectedElement[] expectedTrace = {

                new ExpectedElement("Frontends|Apps|server|URLs|Default")
        };

        httpScenario.add(expectedTrace);
        
        /**
         *  Frontend ------------- t 
         *    |---> Express  --------- t.getSubNodes() 
         *    |---> BackEnds --------- t.getSubNodes()[0].getSubNodes()
         *    |---> BackEnds --------- t.getSubNodes()[0].getSubNodes()[0].getSubNodes()
         * 
         */

        TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
            public boolean validate(TransactionComponentData t){
                
                
                LOGGER.info("frontEnds.getSubNodeCount() = " + t.getSubNodeCount());
                    
                @SuppressWarnings("unchecked")
                HashMap<String, String> testMap = (HashMap<String, String>) t.getParameters();
                
                for (String key: testMap.keySet()) {
                   LOGGER.info("Key = " + key + " ************************ Value = " + testMap.get(key));
                }
                LOGGER.info(":1 **************************** The Parameters are Method = " + t.getParameterNames().contains("Method"));
                LOGGER.info(":2 **************************** The Parameters are Class = " + t.getParameterNames().contains("Class"));
                LOGGER.info(":3 **************************** SIUtils.containsParameter(t, \"Method\") = " + SIUtils.containsParameter(t, "Method"));
                LOGGER.info(":4 **************************** t.getParameterValue(\"Method\").equals(\"GET\") = " + t.getParameterValue("Method").equals("GET"));
                LOGGER.info(":5 **************************** SIUtils.containsParameter(t, \"URL\") = " + SIUtils.containsParameter(t, "URL"));
                LOGGER.info(":6 **************************** t.getParameterValue(\"URL\").equals(\"/api/Items/1\") = " + t.getParameterValue("URL").equals("/api/Items/1"));
                
                if(t.getParameterNames().contains("Method") && t.getParameterValue("Method").equals("GET") && t.getParameterNames().contains("URL") && t.getParameterValue("URL").equals("/api/Items/1")){
                    String[] parameters = {"Server Name","Server Port"} ;
                    String[] expectedValues = {tixChangeConfig.getHost(), tixChangeConfig.getPort()}; 
                    
					        for (int i = 0; i < parameters.length; i++) {
					        	
						        if (!t.getParameterValue(parameters[i]).equals(expectedValues[i])) {
							        LOGGER.error(
							                "for parameter '{}',  actual value: '{}' did not match expected: '{}'",
							                parameters[i], t.getParameterValue(parameters[i]),
							                expectedValues[i]);
							        return false;
						        }
					        }

                    return SIUtils.containsAllComponents(t, httpScenario);
                } else {
                    return false;
                }
                
            }
        }).build();
        
		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(traceValidationDataData).addTraceFilter(filter);
		
		generateHTTP("/api/Items/1", 1);
        
        mockEm.processTraces(traceValidationDataData);
        checkErrorInLogs();
    }
	
	private void checkStartup() {
	    LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

        // verify collector agent successfully connected to mock em
        verifyCollectorStartup();
	}
	
	private void generateHTTP(String url, int numReqs) {
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + url).setNumberReqs(
		        numReqs).build();
		txnGen.start();
	}
	
	private void checkKeywords(String key)
	{
	    try {
	        List<String> keywords = Arrays.asList(".*http.GET.*", ".*" + key + ".*");
	        
	        for (String keyword: keywords) {
	            assertTrue(Util.findPattern(umAgentConfig.getLogPath(), keyword));
	        }  
	    } catch (Exception e) {
	        LOGGER.error(e.getMessage(), e);
	        fail("exception while checking" + umAgentConfig.getLogPath());
	    }
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
		super.stopAppAndWaitDisc();
		//super.checkErrorInLogs();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		stopCollectorAgent();
		super.testClassTeardown();
	}
}