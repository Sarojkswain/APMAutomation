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

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.mockem.ExpectedElement;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.apm.tests.utils.MetricConstants;
import com.ca.apm.tests.utils.MetricConstants.BlameMetricType;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;


/**
 * ExpressTests class includes functional tests for verifying monitoring of
 * NodeJS Application's Express Routes by APM NodeJS agent
 *
 * @author bajde02
 */
@Test(groups = { "nodeagent", "express" })
public class ExpressTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressTests.class);
	
	private static final String expressMetricPathPrefixGET = "Express|/stallService/|GET";
	private static final String expressMetricPathPrefixPOST = "Express|/rest/account/login|POST";
	private static String expressMetricPathPostfix = "";
	
	private static final ArrayList<ExpectedElement[]> expressScenario = new ArrayList<ExpectedElement[]>();

	
	private String[][] expectedArfMessages = {
	    
	                    { 
                           ".*\"fn\":\"route.dispatch\".*" ,
                           ".*\"prms\":\\{\"route\":\"/stallService/\".*",
                           ".*\"http_method\":\"GET\".*"
	                    },
	                    {
	                        ".*\"fn\":\"route.dispatch\".*" ,
                            ".*\"prms\":\\{\"route\":\"/rest/account/login\".*",
                            ".*\"http_method\":\"POST\".*"
	                    }
	
	 };
	
	private static final ExpectedElement[][] expectedTrace = {
	                                                              {
	                                                                  new ExpectedElement(expressMetricPathPrefixGET)
	                                                              },
	                                                              {
                                                                      new ExpectedElement(expressMetricPathPrefixPOST)
                                                                  }
	                           };
	
	private static final String httpPOSTRequestUrl = "/rest/account/login?email=user1@users.com";
	private static final String httpGETRequestUrl = "/stallService?duration=3000";
	
	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		
		//startCollectorAgent();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
	    testMethodName = method.getName();
        probeConfig.updateLogFileName(testMethodName + LOG_FILE_EXT);
        umAgentConfig.updateLogFileName(testMethodName + LOG_FILE_EXT);
        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();
	}
	
	/*
	 * @method:  testExpressGETMetricsRPI
	 * @usecase: Tests the HTTP GET method based Express Metrics for Response Time per Interval
	 * 
	 * */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
	@Test(groups = { "bat", "nodeagent", "express" })
	public void testExpressGETMetricsRPI() 
	{
	    expressHttpTxnGen(ExpressTests.httpGETRequestUrl, HttpRequestMethod.GET);
        
        expressMetricsUtil(HttpRequestMethod.GET, BlameMetricType.RPI);
        
	}
	
	/*
     * @method:  testExpressGETMetricsART 
     * @usecase: Tests the HTTP GET method based Express Metrics for Average Response Time
     * 
     * */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "bat", "nodeagent", "express" })
    public void testExpressGETMetricsART() 
    {
        expressHttpTxnGen(ExpressTests.httpGETRequestUrl, HttpRequestMethod.GET);
        
        expressMetricsUtil(HttpRequestMethod.GET, BlameMetricType.ART);
        
    }
	
	
	/*
     * @method:  testExpressPOSTMetricsRPI
     * @usecase: Tests the HTTP POST method based Express Metrics for Response Time per Interval
     * 
     * */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "smoke", "nodeagent", "express" })
    public void testExpressPOSTMetricsRPI() {
        
        expressHttpTxnGen(ExpressTests.httpPOSTRequestUrl, HttpRequestMethod.POST);
        expressMetricsUtil(HttpRequestMethod.POST, BlameMetricType.RPI);
        
    }
    
    /*
     * @method:  testExpressPOSTTraceParameterHTTPMethod 
     * @usecase: Tests the HTTP POST method based Express Trace parameter HTTP Method with value POST
     * 
     * */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "express" })
    public void testExpressPOSTTraceParameterHTTPMethod() 
    {
        expressHttpTxnGen(ExpressTests.httpPOSTRequestUrl, HttpRequestMethod.POST);
        expressTraceParameterUtil(HttpRequestMethod.POST, "HTTP Method", "POST");
    }

    
    
    /*
     * @method:  testExpressPOSTTraceParameterExpressRoute 
     * @usecase: Tests the HTTP POST method based Express Trace parameter Express Route with value /rest/account/login
     * 
     * */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "express" })
    public void testExpressPOSTTraceParameterExpressRoute() 
	{    
	    expressHttpTxnGen(ExpressTests.httpPOSTRequestUrl, HttpRequestMethod.POST);
	    expressTraceParameterUtil(HttpRequestMethod.POST, "Express Route", "/rest/account/login");
     }

	
	
    /*
     * @method:  testExpressPOSTTraceParameterClass 
     * @usecase: Tests the HTTP POST method based Express Trace parameter Class with value route
     * 
     * */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "express" })
    public void testExpressPOSTTraceParameterClass() 
	{       
	    expressHttpTxnGen(ExpressTests.httpPOSTRequestUrl, HttpRequestMethod.POST);
	    expressTraceParameterUtil(HttpRequestMethod.POST, "Class", "route"); 
     }
	
    
	/*
     * @method:  testExpressPOSTTraceParameterMethod 
     * @usecase: Tests the HTTP POST method based Express Trace parameter Method with value dispatch
     * 
     * */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "express" })
    public void testExpressPOSTTraceParameterMethod() {
           
            ///*
	    expressHttpTxnGen(ExpressTests.httpPOSTRequestUrl, HttpRequestMethod.POST);
	    expressTraceParameterUtil(HttpRequestMethod.POST, "Method", "dispatch");  
        }
	
	
    /*
     * @method:  testExpressGETTraceParameterHTTPMethod 
     * @usecase: Tests the HTTP GET method based Express Route Trace parameter HTTP Method with value GET
     * 
     * */
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "smoke", "nodeagent", "express" })
    public void testExpressGETTraceParameterHTTPMethod() 
	{    
	    expressHttpTxnGen(ExpressTests.httpGETRequestUrl, HttpRequestMethod.GET);
	    expressTraceParameterUtil(HttpRequestMethod.GET, "HTTP Method", "GET");  
     }

    
    /*
     * @method:  testExpressGETTraceParameterExpressRoute
     * @usecase: Tests the HTTP GET method based Express Trace parameter Express Route with value /stallService/
     * 
     * */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "smoke", "nodeagent", "express" })
    public void testExpressGETTraceParameterExpressRoute() 
    {    
        expressHttpTxnGen(ExpressTests.httpGETRequestUrl, HttpRequestMethod.GET);
        expressTraceParameterUtil(HttpRequestMethod.GET, "Express Route", "/stallService/");  
    }

    
    
    /*
     * @method:  testExpressGETTraceParameterClass
     * @usecase: Tests the HTTP GET method based Express Trace parameter Class with value route
     * 
     * */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "smoke", "nodeagent", "express" })
    public void testExpressGETTraceParameterClass() {
           
        expressHttpTxnGen(ExpressTests.httpGETRequestUrl, HttpRequestMethod.GET);
        
        expressTraceParameterUtil(HttpRequestMethod.GET, "Class", "route");  
        }
    
    /*
     * @method:  testExpressGETTraceParameterMethod 
     * @usecase: Tests the HTTP GET method based Express Trace parameter Method with value dispatch
     * 
     * */
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "smoke", "nodeagent", "express" })
    public void testExpressGETTraceParameterMethod() 
    {       
        expressHttpTxnGen(ExpressTests.httpGETRequestUrl, HttpRequestMethod.GET);
        
        expressTraceParameterUtil(HttpRequestMethod.GET, "Method", "dispatch");   
        
    }

	
    
    private void expressTraceParameterUtil(HttpRequestMethod httpRequestMethod, final String  parameter, final String parameterValue)
    {   
        if(httpRequestMethod.equals(HttpRequestMethod.GET))
        {
            ExpressTests.expressScenario.add(ExpressTests.expectedTrace[0]);
        }
        else if(httpRequestMethod.equals(HttpRequestMethod.POST))
        {
            ExpressTests.expressScenario.add(ExpressTests.expectedTrace[1]);
        }
                
        TraceValidationData traceValidationData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator() {
       
            public boolean validate(TransactionComponentData t)
            {   
                return ExpressTests.expressTraceParameterValidation(t.getSubNodes()[0], parameter, parameterValue);
                
            }
        }).build();

        mockEm.processTraces(traceValidationData);
        
        super.checkErrorInLogs();
    }

    private static boolean expressTraceParameterValidation(TransactionComponentData expressData, String traceParameter, String traceParameterValue)
    {
        LOGGER.info("Express Component Trace Parameter being tested: KEY="+traceParameter+"and VALUE="+traceParameterValue);
        if (!SIUtils.isErrorSnapshot(expressData) && !SIUtils.isStallSnapshot(expressData) && expressData.getParameterNames().contains(traceParameter) && expressData.getParameterValue(traceParameter).equals(traceParameterValue)) {
            return SIUtils.containsAllComponents(expressData, ExpressTests.expressScenario);
        } else {
            return false;
        }

    }
	
    private void expressMetricsUtil(HttpRequestMethod httpRequestMethod, String blameMetricType)
    {   
        String summaryMetricPath = "";
        String[] expected = new String[3];
        String path = umAgentConfig.getLogPath();

        long waitTime = 60000;
        long lowerBoundRT = 0;
        long upperBoundRT = 108;
      
        
        expressMetricPathPostfix = MetricConstants.METRIC_NAME_DELIMETER+blameMetricType;
        
        if(httpRequestMethod.equals(HttpRequestMethod.GET))
        {
            summaryMetricPath = ExpressTests.expressMetricPathPrefixGET+ExpressTests.expressMetricPathPostfix;
            
            expected = expectedArfMessages[0];
            
            lowerBoundRT = 2500;
            upperBoundRT = 3500;
        }
            
        else if(httpRequestMethod.equals(HttpRequestMethod.POST))
        {
            summaryMetricPath = ExpressTests.expressMetricPathPrefixPOST+ExpressTests.expressMetricPathPostfix;
            
            expected = expectedArfMessages[1];
        }
        
        

        MetricAssertionData metricData = null;
        if(blameMetricType.equals(BlameMetricType.RPI))
        {
            metricData = new MetricAssertionData.Builder(summaryMetricPath, 1).setDuration(waitTime).build();
            
        }
        else if(blameMetricType.equals(BlameMetricType.ART))
        {
            metricData = new MetricAssertionData.MinMaxBuilder(summaryMetricPath, lowerBoundRT, upperBoundRT).setDuration(waitTime).build();
            
        }
        mockEm.processMetrics(metricData);
        
        /*for (String msg : expected) 
        {
            try
            {
                assertTrue(Util.findPattern(path, msg),
                        String.format("Error '%s' was not found in log: %s", msg, path));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }*/
        super.checkErrorInLogs();
    }
	
    
    private void expressHttpTxnGen(String httpRequestUrl, HttpRequestMethod httpRequestMethod)
    {
        LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(), "express");
        verifyCollectorStartup();
        
        //Util.sleep(35000);

        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase
                    + httpRequestUrl).setHttpMethod(httpRequestMethod)
                    .setNumberReqs(1).build();
        txnGen.start();
       
    }
    
    
	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() 
	{
	    stopAppAndWaitDisc();
        stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() 
	{
		stopCollectorAgent();
		super.testClassTeardown();
	}
}
