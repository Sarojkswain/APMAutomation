package com.ca.apm.tests.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.tests.test.utils.TraceInfo;
import com.ca.apm.tests.test.utils.WebAppTraceDetails;
import com.ca.apm.tests.testbed.DotNetBaseTestBed;

/**
 * Test base class for .Net agent
 * @author kurma05
 */
public class DotNetAgentBaseTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(DotNetAgentBaseTest.class);
    protected static final String DOTNET_AGENT_PROFILE = 
        DotNetBaseTestBed.DOTNET_AGENT_HOME + "\\wily\\IntroscopeAgent.profile";
   
    protected void validateDotNetMap(String appName, String host, 
                                     String[] mapSubNodes) throws Exception {
        validateDotNetMap(appName, host, mapSubNodes, false);
    }
    
    protected void validateDotNetMap(String appName, String host, 
                                     String[] mapSubNodes, boolean isBAEnabled) throws Exception {
        
        sendHttpRequests("http://" + host + ":9091/Dinners", 50, isBAEnabled);
        
        //open map & add filter
        filterMapByHost(host);
        
        //validate bt node
        if(isBAEnabled) {            
            Assert.assertTrue(clickBtTab("/Dinners"), 
                "'Business Transaction' tab is missing for node '/Dinners/");
            validateBTMetricData();
        }
      
        //validate map nodes
        for(String node: mapSubNodes) {
            logger.info("Validating agent & generic frontends data for node '{}'...", node);
            //validate agent data
            Assert.assertTrue(clickAgentTab(node), "'Agent' tab is missing for node " + node);
            validateDotAgentTabData();  
            
            //validate generic frontends data
            Assert.assertTrue(mapHelper.clickComponentViewTab(node, "Generic Front-end"),
                "'Generic Front-end' tab is missing for node " + node + " or node itself didn't appear.");
            validateGenericFrontendData(appName);
        }
        
        //validate db node
        //due to DE306510 leaving this validation for onprem only
        //remove 'if' check once defect is fixed
        if(instance.equals("onprem")) {
            Assert.assertTrue(clickInferredDatabaseTab("DATABASE"), 
                "'Inferred Database' tab is missing for 'DATABASE' node or node itself didn't appear.");
            validateInferredDatabaseTabData();
        }
    }
    
    private boolean clickInferredDatabaseTab(String nodeName) throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        long maxWaiting = 300000; 

        while((System.currentTimeMillis() - startTime) < maxWaiting) {
            logger.info("Waiting for 'Inferred Database' tab to appear for node {}... Max {} ms.", nodeName, maxWaiting);
            if(!mapHelper.clickNode(nodeName)) {
                continue;
            }
            if(mapHelper.clickComponentViewTab("Inferred Database")) {
                return true;
            }
            Thread.sleep(20000);
        }
        
        return false;
    }

    private void validateDotAgentTabData() {

        String[] data =
            new String[] {"AGENT", ".NET Process", "Metric Count", "Enterprise Team Center"};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + " doesn't exist");
        }
    }
    
    private void validateInferredDatabaseTabData() {

        String[] data =
            new String[] {"INFERRED_DATABASE", "Enterprise Team Center", "DotNetTestDB-MVC5"};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + " doesn't exist");
        }
    }
    
    protected void validateDotNetTTViewer(String agentName, String host, String port,
                                          String nodeName, boolean isBAEnabled) throws Exception {
        
        filterMapByHost(host);
        
        for(TraceInfo trace: WebAppTraceDetails.TRACES_INFO) {
            if(trace.getNodeName().contains(nodeName)) {
                String url = "http://" + host + ":" + port + trace.getUrl();
                validateTrace(agentName, host, url, trace, isBAEnabled);
            }
        }
    }
    
    @Override
    protected void startTransactionTrace(String agentName, String host) throws Exception {
        
        wilyDir = DotNetBaseTestBed.DOTNET_AGENT_HOME + "\\wily";
        startTransactionTrace(agentName, wilyDir + "\\IntroscopeAgent.profile", host);
    }
    
    protected void validateBTMetricData() throws Exception {
        
        String[] allMetrics = new String[] { 
                "Average Response Time (ms)",
                "Responses Per Interval", 
                "Average Page Stall Time (ms)",
                "Average Page Render Time (ms)",
                "Average Connection Establishment Time (ms)",
                "Average Domain Lookup Time (ms)",
                "Average DOM Processing Time (ms)",
                "Average Previous Page Unload Time (ms)",
                "Average Page Load Time (ms)",
                "Average Time to First Byte (ms)",
                "Average Time to Last Byte (ms)", 
                "Page Hits Per Interval"
                };
    
        for (String metric : allMetrics) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric),
                    "Metric/Attribute '" + metric + "' doesn't exist");
        }
    }
}