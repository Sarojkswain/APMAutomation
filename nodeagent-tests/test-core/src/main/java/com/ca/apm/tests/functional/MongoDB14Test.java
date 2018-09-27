package com.ca.apm.tests.functional;
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
 

package com.ca.apm.tests.test;

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
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

*//**
 * MongoDBTest class includes functional tests for verifying monitoring of
 * NodeJS Application's MongoDB Backend by APM NodeJS agent. It targets mongo
 * driver 1.4.*
 *
 * @author zheji01@ca.com
 *//*

@Test(enabled = false)
public class MongoDB14Test extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDB14Test.class);
	private long waitTime = 60000;

	@BeforeClass (enabled = false, alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		//startCollectorAgent();
		
		
	}

	@BeforeMethod (enabled = false, alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		//probeConfig.updateLogFileName(method.getName() + LOG_FILE_EXT);
		
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
		//super.startNodeApp();
		//startAppAndWaitConn();
		
		startCollectorAgentAndWaitConn();
	}
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
	@Test(enabled = false, groups = { "bat", "mongo14", "backends" })
	public void testMongoDBBackEndRPI() {
        startAppAndWaitConn();
		checkStartup();
        generateHTTP("/rest/clickstream", 2);

		String metricPathPrefix = "Backends|tixchange on localhost-27017 (MongoDB)";
		String summaryMetricPath = metricPathPrefix + ":Responses Per Interval";
		
		mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
            new AggregatedMetricValueValidator(summaryMetricPath, 2), true);
		
		checkKeywords("rest/clickstream");
		checkErrorInLogs();
	}
	
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
    @Test(enabled = false, groups = { "bat", "mongo14", "backends" })
    public void testMongoDBBackEndRPINoFrontend() {
        tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);
        startAppAndWaitConn();
        checkStartup();
        generateHTTP("/rest/clickstream", 2);

        String metricPathPrefix = "Backends|tixchange on localhost-27017 (MongoDB)";
        String summaryMetricPath = metricPathPrefix + ":Responses Per Interval";
        
        mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
            new AggregatedMetricValueValidator(summaryMetricPath, 2), true);
        
        checkKeywords("rest/clickstream");
        checkErrorInLogs();
    }	
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
    @Test(enabled = false, groups = { "bat", "mongo14", "backends" })
    public void testMongoDBBackEndOperationRPI() {
        startAppAndWaitConn();
        checkStartup();
        generateHTTP("/rest/clickstream", 2);

        String metricPathPrefix = "Backends|tixchange on localhost-27017 (MongoDB)";
        String urlMetricPath = metricPathPrefix + "|find:Responses Per Interval";
        
        mockEm.getReqProcessor(NODE_AGENT_EXPR).processMetrics(waitTime,
            new AggregatedMetricValueValidator(urlMetricPath, 2), true);

        checkKeywords("rest/clickstream");
        checkErrorInLogs();
    }
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
    @Test(enabled = false, groups = { "deep", "mongo14", "backends" })
    public void testMongoDBBackEndTrace() {
        startAppAndWaitConn();
        checkStartup();
        //generateHTTP("/rest/clickstream", 1);
        
        final ArrayList<ExpectedElement[]> httpScenario = new ArrayList<ExpectedElement[]>();

        ExpectedElement[] expectedTrace = {

                new ExpectedElement("Backends|tixchange on localhost-27017 (MongoDB)")
        };

        httpScenario.add(expectedTrace);
        
        *//**
         *  Frontend ------------- t 
         *    |---> Express  --------- t.getSubNodes() 
         *    |---> BackEnds --------- t.getSubNodes()[0].getSubNodes()
         *    |---> BackEnds --------- t.getSubNodes()[0].getSubNodes()[0].getSubNodes()
         * 
         *//*

        TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
            public boolean validate(TransactionComponentData t){
                
                TransactionComponentData backEnds = t.getSubNodes()[0].getSubNodes()[0];  // Backends
                LOGGER.info("backEnds.getSubNodeCount() = " + backEnds.getSubNodeCount());
                    
                HashMap<String, String> testMap = (HashMap<String, String>) backEnds.getParameters();
                
                for (String key: testMap.keySet()) {
                   LOGGER.info("Key = " + key + " ************************ Value = " + testMap.get(key));
                }
                LOGGER.info(":1 **************************** The Parameters are Method = " + backEnds.getParameterNames().contains("Method"));
                LOGGER.info(":2 **************************** The Parameters are Class = " + backEnds.getParameterNames().contains("Class"));
                LOGGER.info(":3 **************************** SIUtils.containsParameter(t, \"Class\") = " + SIUtils.containsParameter(backEnds, "Class"));
                LOGGER.info(":4 **************************** t.getParameterValue(\"Class\").equals(\"mongodb\") = " + backEnds.getParameterValue("Class").equals("mongodb"));
                    
                if(backEnds.getParameterNames().contains("Class") && backEnds.getParameterValue("Class").equals("mongodb")){
                    return SIUtils.containsAllComponents(backEnds, httpScenario);
                } else {
                    return false;
                }
                
            }
        }).build();
        
        // Set transaction trace filter
        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
                "dummy");
        mockEm.getReqProcessor(traceValidationDataData).addTraceFilter(filter);
        
        generateHTTP("/rest/clickstream", 1);
        
        mockEm.processTraces(traceValidationDataData);
        checkErrorInLogs();
    }
	
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
    @Test(enabled = false, groups = { "deep", "mongo14", "backends" })
    public void testMongoDBBackEndTraceNoFrontend() {
        tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);
        startAppAndWaitConn();
        checkStartup();
        //generateHTTP("/rest/clickstream", 1);
        
        final ArrayList<ExpectedElement[]> httpScenario = new ArrayList<ExpectedElement[]>();

        ExpectedElement[] expectedTrace = {

                new ExpectedElement("Backends|tixchange on localhost-27017 (MongoDB)")
        };

        httpScenario.add(expectedTrace);
        
        *//**
         *  Fragment ------------- t 
         *  	|Express ------------- t 
         *    		|---> BackEnds --------- t.getSubNodes()
         *    		|---> BackEnds --------- t.getSubNodes()[0].getSubNodes()
         * 
         *//*

        TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
            public boolean validate(TransactionComponentData t){
                
                TransactionComponentData backEnds = t; //refers Fragment
                
                if(backEnds.getSubNodeCount() > 0) backEnds = backEnds.getSubNodes()[0]; // refers Express
                if(backEnds.getSubNodeCount() > 0) backEnds = backEnds.getSubNodes()[0]; // refers Backends
                
                LOGGER.info("backEnds.getSubNodeCount() = " + backEnds.getSubNodeCount());
                    
                HashMap<String, String> testMap = (HashMap<String, String>) backEnds.getParameters();
                
                for (String key: testMap.keySet()) {
                   LOGGER.info("Key = " + key + " ************************ Value = " + testMap.get(key));
                }
                LOGGER.info(":1 **************************** The Parameters are Method = " + backEnds.getParameterNames().contains("Method"));
                LOGGER.info(":2 **************************** The Parameters are Class = " + backEnds.getParameterNames().contains("Class"));
                LOGGER.info(":3 **************************** SIUtils.containsParameter(t, \"Class\") = " + SIUtils.containsParameter(backEnds, "Class"));
                LOGGER.info(":4 **************************** t.getParameterValue(\"Class\").equals(\"mongodb\") = " + backEnds.getParameterValue("Class").equals("mongodb"));
                    
                if(backEnds.getParameterNames().contains("Class") && backEnds.getParameterValue("Class").equals("mongodb")){
                    return SIUtils.containsAllComponents(backEnds, httpScenario);
                } else {
                    return false;
                }
                
            }
        }).build();
        
        // Set transaction trace filter
        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
                "dummy");
        mockEm.getReqProcessor(traceValidationDataData).addTraceFilter(filter);
        
        generateHTTP("/rest/clickstream", 1);
        
        mockEm.processTraces(traceValidationDataData);
        checkErrorInLogs();
    }	
	
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "zheji01")
    @Test(enabled = false, groups = { "deep", "mongo14", "backends" })
    public void testMongoDBBackEndOperationTrace() {
        startAppAndWaitConn();
        checkStartup();
        //generateHTTP("/rest/clickstream", 1);
        
        final ArrayList<ExpectedElement[]> httpScenario = new ArrayList<ExpectedElement[]>();

        ExpectedElement[] expectedTrace = {

                new ExpectedElement("Backends|tixchange on localhost-27017 (MongoDB)|find")
        };

        httpScenario.add(expectedTrace);
        
        *//**
         *  Frontend ------------- t 
         *    |---> Express  --------- t.getSubNodes() 
         *    |---> BackEnds --------- t.getSubNodes()[0].getSubNodes()
         *    |---> BackEnds --------- t.getSubNodes()[0].getSubNodes()[0].getSubNodes()
         * 
         *//*

        TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
            public boolean validate(TransactionComponentData t){
                
                TransactionComponentData backEnds = t.getSubNodes()[0].getSubNodes()[0].getSubNodes()[0];  // Backends
                LOGGER.info("backEnds.getSubNodeCount() = " + backEnds.getSubNodeCount());
                    
                HashMap<String, String> testMap = (HashMap<String, String>) backEnds.getParameters();
                
                for (String key: testMap.keySet()) {
                   LOGGER.info("Key = " + key + " ************************ Value = " + testMap.get(key));
                }
                LOGGER.info(":1 **************************** The Parameters are Method = " + backEnds.getParameterNames().contains("Method"));
                LOGGER.info(":2 **************************** The Parameters are Class = " + backEnds.getParameterNames().contains("Class"));
                LOGGER.info(":3 **************************** SIUtils.containsParameter(t, \"Method\") = " + SIUtils.containsParameter(backEnds, "Method"));
                LOGGER.info(":4 **************************** t.getParameterValue(\"Method\").equals(\"find\") = " + backEnds.getParameterValue("Method").equals("find"));
                    
                if(backEnds.getParameterNames().contains("Method") && backEnds.getParameterValue("Method").equals("find")){
                    return SIUtils.containsAllComponents(backEnds, httpScenario);
                } else {
                    return false;
                }
                
            }
        }).build();
        
        // Set transaction trace filter
        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
                "dummy");
        mockEm.getReqProcessor(traceValidationDataData).addTraceFilter(filter);
        
        generateHTTP("/rest/clickstream", 1);
        
        mockEm.processTraces(traceValidationDataData);
        checkErrorInLogs();
    }
	
	private void checkStartup() {
        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

        // verify collector agent successfully connected to mock em
        verifyCollectorStartup();
    }
    
    private void generateHTTP(String url, int numReqs) {
        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + url)
		    .setNumberReqs(numReqs).build();
		txnGen.start();
        
    }
	
	private void checkKeywords(String key)
    {
        try {
            List<String> keywords = Arrays.asList(".*http.GET.*", ".*" + key + ".*");
            
            for (String keyword: keywords) {
                assertTrue(Util.findPattern(collAgentConfig.getLogPath(), keyword));
            }  
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            fail("exception while checking" + collAgentConfig.getLogPath());
        }
    }

	@AfterMethod(enabled = false, alwaysRun = true)
	public void executeAfterMethod() {
		//super.stopNodeApp();
		//super.checkErrorInLogs();
		
		stopAppAndWaitDisc();
        if (tixChangeConfig.getProperty("useRequestQueue") != "false") {
            tixChangeConfig.updateProperty("useRequestQueue", Boolean.FALSE);
        }		
        stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(enabled = false, alwaysRun = true)
	public void testClassTeardown() {
	//	stopCollectorAgent();
		super.testClassTeardown();
	}
}
*/