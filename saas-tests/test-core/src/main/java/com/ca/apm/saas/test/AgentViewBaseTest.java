package com.ca.apm.saas.test;

import java.io.IOException;

import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AgentViewBaseTest extends SaaSBaseTest 
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected void startAgentIsolationView(String agentName) throws InterruptedException, IOException {
        logger.info( "Starting 'Agent Isolation View' ...");
            // click Agent view
            Assert.assertTrue(agentsHelper.clickAgentsView(), "Agent View NOT present");
            // click Open an IsolationView 
            Assert.assertTrue(agentsHelper.openAnIsolationViewForAgent(agentName), "Cannot open Isolated View for Agent - " + agentName );            
            // Assert a Map View new tab is opened
            Assert.assertTrue(agentsHelper.gotoNewOpenedTab(), "No new tab for Isolated View opened");
    }
   
    protected void validateMapInIsolatedView(String agentType, String appName, 
                               String[] mapSubNodes, boolean shouldFitAllToView) throws Exception {
        
        if(mapSubNodes == null || mapSubNodes.length == 0) {
            logger.info("Skipping map validation as node info wasn't provided...");
            return;
        }
                
        //validate agent & application tabs
        for(String node: mapSubNodes) {
            logger.info("Validating agent & application data for node '{}'...", node);
            if(shouldFitAllToView) {
                Assert.assertTrue(
                    mapHelper.clickFitAll(), "Failed to click on 'Fit all to view' map option.");
            }
            
            //validate node exist and Agent data. 
            Assert.assertTrue(clickAgentTab(node), "Either 'Agent' tab is missing for node '" + node + "' or node itself didn't appear.");
            validateAgentTabData();
            
            if(node.equalsIgnoreCase("CA APM Demo Host")) {
                // validate Application data if it's "CA APM Demo Host" v.s. Experience Collector Host doesn't have Application tab. 
                Assert.assertTrue(mapHelper.clickComponentViewTab("Application"), "Unable to click 'Application tab'");
                validateApplicationTabData();
            }
        }
    }
    
    protected void validateApplicationTabData() {
        Assert.assertTrue(mapHelper.clickLoadCharts(), "'Load Charts' button is either missing or not clickable");
        String[] data =
            new String[] {"Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval", 
                          "Stall Count", "Concurrent Invocations", "Enterprise Team Center"};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + "' doesn't exist");
        }
    }
    
    protected void initMapInIsolatedView(String agentName, String appServer, String host, String appName) throws Exception {

        logger.info( "Setting up filters ...");
        // Leave "CA APM" as default value and click Live button
        mapHelper.clickLiveTimeRange();     
        // click "Show Filter" if it shows up. 
        mapHelper.clickShowFilters();
        // Leave default "agent" Filter and add additional filters. i.e. "IsDemo" and "Hostname".
        Assert.assertTrue(mapHelper.setMapFilter("agent", new String[]{appName}), "Failed to set agent filter");
        
    }
      

}
