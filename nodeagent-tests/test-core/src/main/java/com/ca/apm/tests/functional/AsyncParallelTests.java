/* Copyright (c) 2015 CA.  All rights reserved.
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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.AutomationConstants;
import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.automation.common.mockem.ITraceElement;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

/**
 * Async-Parallel tests for node agent automation
 *
 * @author bajde02
 */

@Test(groups = { "nodeagent", "asyncParallel" })
public class AsyncParallelTests
    extends BaseNodeAgentTest
{
    private static final Logger LOGGER                                      = LoggerFactory.getLogger(AsyncParallelTests.class);

    private static final String httpGETRequestUrlAsyncServiceReadWriteFiles = "/asyncService/interesting/";

    private static final String httpGETRequestUrlAsyncServiceComplicated    = "/asyncService/complicated/";

    private static final String httpGETRequestUrlAsyncServiceParallel       = "/asyncService/parallel/";

    private static final String httpGETRequestUrlAsyncServiceAfter1         = "/asyncService/after1/";

    private static final String httpGETRequestUrlAsyncServiceAfter2         = "/asyncService/after2/";

    @BeforeClass(alwaysRun = true)
    public void testClassSetup()
    {
        super.testClassSetup();
    }

    @BeforeMethod(alwaysRun = true)
    public void executeBeforeMethod(Method method)
    {
        testMethodName = method.getName();
        probeConfig.updateLogFileName(method.getName() + LOG_FILE_EXT);
        umAgentConfig.updateLogFileName(testMethodName + LOG_FILE_EXT);
        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "bat", "nodeagent", "asyncParallel" })
    public void testAsyncParallelReadWriteFilesTraceWriteFS()
    {

        ExpectedTraceElement[] expectedTrace = { 
                                                 new ExpectedTraceElement ("Fragments\\|fragment\\|route_dispatch"), 
                                                 new ExpectedTraceElement ("Backends\\|fs\\|writeFile"), 
                                                 };

        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceReadWriteFiles);
    
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "bat", "nodeagent", "asyncParallel" })
    public void testAsyncParallelReadWriteFilesTraceReadFS()
    {
        ExpectedTraceElement[] expectedTrace = 
            { 
              new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
              new ExpectedTraceElement("Express\\|/asyncService/interesting/\\|GET"),
              new ExpectedTraceElement("Backends\\|fs\\|readFile"), };

        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceReadWriteFiles);
    }


    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelComplicatedTraceFragmentsWriteFS()
    {
        /*
         * Fragments|fragment|route_dispatch timers::setTimeout_400
         * Backends|fs|writeFile timers::setTimeout_200
         */

        ExpectedTraceElement[] expectedTrace =
            { 
                 new ExpectedTraceElement("Fragments\\|fragment\\|route_dispatch"),
                 // new ExpectedTraceElement("timers::setTimeout_400"), 
                 new ExpectedTraceElement("Backends\\|fs\\|writeFile"),
                 // new ExpectedTraceElement("timers::setTimeout_200")
            };

        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceComplicated);
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelComplicatedTraceFrontendsReadFS()
    {
        // "fragments should be created only if more than one component is executing in parallel"
    	// for async parallel scenario - /asyncService/complicated/, read operations happen one after other
    	// so should come in same trace
        ExpectedTraceElement[] expectedTrace =

        {

            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
    
            new ExpectedTraceElement("Express\\|/asyncService/complicated/\\|GET"),
    
            new ExpectedTraceElement("Backends\\|fs\\|readFile"),
            
            new ExpectedTraceElement("Backends\\|fs\\|readFile")

        };

        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceComplicated);
    }


    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelPlTraceFragmentsWriteFS() 
    {
        // "fragments should be created only if more than one component is executing in parallel"
    	// for async parallel scenario - /asyncService/complicated/, read operation 
    	// and write operation execute in parallel. Write operation starts while read is still executing.
    	// so we expect a separate fragmented trace for write operation
                        
        
        ExpectedTraceElement[] expectedTrace = {
                                                        

            new ExpectedTraceElement("Fragments\\|fragment\\|route_dispatch"),
                                                            
    
            new ExpectedTraceElement("Backends\\|fs\\|writeFile"),
                                                            
    
            //new ExpectedTraceElement("timers::setTimeout_100"),
                                                                                                                   
         };
                                                
         asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceParallel);
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelPlTraceFragmentsReadFS() 
    {
                   /*
                   Fragments|fragment|route_dispatch
                       Backends|fs|readFile
                       timers::setTimeout_120 [Deep]
                       */
        
        ExpectedTraceElement[] expectedTrace = {
                                                        

           new ExpectedTraceElement("Fragments\\|fragment\\|route_dispatch"),
                                                                
        
           new ExpectedTraceElement("Backends\\|fs\\|readFile"),
                                                                
        
           //new ExpectedTraceElement("timers::setTimeout_120"),
                                                                
        };
                                           
        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceParallel);
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelPlTraceFrontends() 
    {
                   /*
                   
                   Frontends|Apps|server|URLs|Default
                     Express|/asyncService/parallel/|GET
                      Backends|fs|readFile
                      timers::setTimeout_110 [Deep]
                   
                   */
                   
        ExpectedTraceElement[] expectedTrace = {
                                                        

           new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
                                                                
        
           new ExpectedTraceElement("Express\\|/asyncService/parallel/\\|GET"),
                                                                
        
           new ExpectedTraceElement("Backends\\|fs\\|readFile"),
                                                                
        
           //new ExpectedTraceElement("timers::setTimeout_110"),
                                                                
   
                                                   };
                                           
        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceParallel);
                   
               }    
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelAfter1TraceFrontends() 
    {
                   /*
                   Frontends|Apps|server|URLs|Default
                     Express|/asyncService/after1/|GET
                      Backends|fs|readFile
                      timers::setTimeout_5 [Deep]
                     timers::setTimeout_10 [Deep]
                    */
                   
        ExpectedTraceElement[] expectedTrace = {

           new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
                                                                
        
           new ExpectedTraceElement("Express\\|/asyncService/after1/\\|GET"),
                                                                
        
           new ExpectedTraceElement("Backends\\|fs\\|readFile"),
                                                                

           //new ExpectedTraceElement("timers::setTimeout_5"),
                                                        

           //new ExpectedTraceElement("timers::setTimeout_10"),
   
        };
                                           
        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceAfter1);
                   
    }


    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelAfter2TraceFragmentsReadFS() 
    {              
                   /*
                   Frontends|Apps|server|URLs|Default
                      Express|/asyncService/after2/|GET
                       timers::setTimeout_200 [Deep]
                       Backends|fs|readFile
                       timers::setTimeout_300 [Deep]
                    */
                   
        ExpectedTraceElement[] expectedTrace = {
                                                        

               new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
                                                                    
            
               new ExpectedTraceElement("Express\\|/asyncService/after2/\\|GET"),
                                                                    
            
               //new ExpectedTraceElement("timers::setTimeout_200"),
                                                                    
            
               new ExpectedTraceElement("Backends\\|fs\\|readFile"),
                                                                    
            
               //new ExpectedTraceElement("timers::setTimeout_300")
                                                                    

        };
                                           
        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceAfter2);
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "deep", "nodeagent", "asyncParallel" })
    public void testAsyncParallelAfter2TraceFragmentsWriteFS() 
    {

                   /*
                   Fragments|fragment|timers_setTimeout_750
                     timers::setTimeout_400 [Deep]
                     Backends|fs|writeFile
                     timers::setTimeout_200 [Deep]
                    */
                   
        ExpectedTraceElement[] expectedTrace = {
                                                        

           new ExpectedTraceElement("Fragments\\|fragment\\|route_dispatch"),
                                                                
        
           //new ExpectedTraceElement("timers::setTimeout_400"),
                                                                
        
           new ExpectedTraceElement("Backends\\|fs\\|writeFile"),
                                                                
        
           //new ExpectedTraceElement("timers::setTimeout_200")
        };
                                           
        asyncParallelTracesUtil(expectedTrace,httpGETRequestUrlAsyncServiceAfter2);
                   
    }
    
    
    
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "bajde02")
    @Test(groups = { "smoke", "nodeagent", "asyncParallel" })
    public void testAsyncParallelPlCheckCorrelationIds() 
    {
    final ArrayList<String> correlationIds = new ArrayList<String>();
                    
    TraceValidationData traceValidationData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator()
    {   
        public boolean validate (TransactionComponentData t)
        {   
            String corrId = t.getParameterValue(AutomationConstants.CROSS_PROCESS_DATA_KEY).toString();
            if(corrId!=null)
                correlationIds.add(corrId);
            
            if(correlationIds.size()>=2)
            {   
                for(int i=0; i<correlationIds.size()-1;i++)
                {   
                    String corrId1 = correlationIds.get(i);
                    LOGGER.info("Correlation id 1 = "+corrId1);
                    for(int j=i+1; j<correlationIds.size(); j++)
                    {
                        String corrId2 = correlationIds.get(j);
                        LOGGER.info("Correlation id 2 = "+corrId2);
                        
                      //validate if the correlation ids are same or not
                      if(corrId1.equals(corrId2))
                          return true;
                    }
                       
                }
            }
            
            return false;
         }
    }).build();
                    
    // Set transaction trace filter
    ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username","dummy");
    mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);
    asyncParallelHttpTxnGen(httpGETRequestUrlAsyncServiceReadWriteFiles);
    mockEm.processTraces(traceValidationData);
    super.checkErrorInLogs();
                    
                    
                     
                }
    
    
    

    private void asyncParallelHttpTxnGen(String httpRequestUrl)
    {
        LOGGER.info("executing test {}#{} ",this.getClass().getSimpleName(), "asyncParallel");
        verifyCollectorStartup();

        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + httpRequestUrl).setHttpMethod(HttpRequestMethod.GET).setNumberReqs(1).build();
        txnGen.start();
    }

    private void asyncParallelTracesUtil(ExpectedTraceElement[] expectedTrace, String asyncHttpTxnUrl)
    {
        final List<ITraceElement[]>asyncScenario = new ArrayList<ITraceElement[]>();

        asyncScenario.add(expectedTrace);

        TraceValidationData traceValidationData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator()
        {

            public boolean validate(TransactionComponentData t)
            {
                return TraceCompareUtil.compareTraceToPatterns(t, asyncScenario);
            }
        }).build();
        
        
        // Set transaction trace filter
        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
                "dummy");
        mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);
        
        asyncParallelHttpTxnGen(asyncHttpTxnUrl);
        
        mockEm.processTraces(traceValidationData);
        super.checkErrorInLogs();
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
        super.testClassTeardown();
    }

}