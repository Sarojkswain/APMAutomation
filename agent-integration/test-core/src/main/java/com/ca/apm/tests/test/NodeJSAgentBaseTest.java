package com.ca.apm.tests.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.tests.test.utils.TraceInfo;
import com.ca.apm.tests.test.utils.WebAppTraceDetails;

/**
 * Test base class for NodeJS agent
 * @author kurma05
 */
public class NodeJSAgentBaseTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(NodeJSAgentBaseTest.class);
    protected static final String IA_HOME_LINUX = "/opt/apmia";
   
    protected void validateNodeJSMap(String appName, String host, 
                                     String[] mapSubNodes) throws Exception {
        
        //start load  
        Assert.assertTrue(startJMeter(host, "3000", "tixChange_nodejs_simple.jmx", 120000), 
            "Jmeter script didn't start successfully");  
        
        //open map & add filter
        filterMapByHost(host);
        
        //validate generic frontends data
        String frontendNode = "Apps|" + appName;
        Assert.assertTrue(mapHelper.clickComponentViewTab(frontendNode, "Generic Front-end"), 
            "'Generic Front-end' tab is missing for node " + frontendNode + " or node itself didn't appear.");
        validateGenericFrontendData(appName);
        
        for(String node: mapSubNodes) {
            logger.info("Validating agent & expressjs data for node '{}'...", node);
            Assert.assertTrue(
                mapHelper.clickFitAll(), "Failed to click on 'Fit all to view' map option.");
            
            //validate Agent data
            Assert.assertTrue(clickAgentTab(node), 
                "Either 'Agent' tab is missing for node '" + node + "' or node itself didn't appear.");
            validateAgentTabData(); 
            
            //validate Expressjs data
            Assert.assertTrue(mapHelper.clickComponentViewTab(node, "Express.js"), 
                "'Express.js' tab is missing for node " + node + " or node itself didn't appear.");
            validateExpressjsData(appName);
        }
    }
    
    private void validateExpressjsData(String appName) {
        
        String[] data =
            new String[] {"EXPRESSJS", "Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval",
                          "Stall Count", "Concurrent Invocations", appName};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + " doesn't exist");
        }
    }
    
    protected void validateNodeJsTTViewer(String agentName, String host, String port,
                                          String nodeName) throws Exception {
        
        filterMapByHost(host);
        
        for(TraceInfo trace: WebAppTraceDetails.TRACES_INFO) {
            if(trace.getNodeName().contains(nodeName)) {
                String url = "http://" + host + ":" + port + trace.getUrl();
                validateTrace(agentName, host, url, trace);
            }
        }
    }
    
    @Override   
    protected void startTransactionTrace(String agentName, String host) throws Exception {
        
        startClwTransactionTrace();
    }
}