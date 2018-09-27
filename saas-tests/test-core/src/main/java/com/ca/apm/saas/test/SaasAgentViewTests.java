package com.ca.apm.saas.test;

import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.AgentsPage;
import com.ca.apm.saas.pagefactory.HomePage;
import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.test.utils.TestDataProviders;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class SaasAgentViewTests extends AgentViewBaseTest 
{
    private void init() throws Exception {
        
        attemptLogin(ui.getDriver());
        mapHelper = new MapPage(ui.getDriver());
        agentsHelper = new AgentsPage(ui.getDriver());
        homeHelper =  new HomePage(ui.getDriver());
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")
    @Test(dataProvider = "agentIsolationViewProps", dataProviderClass = TestDataProviders.class)
    public void testAgentIsolationView(String cardName, String agentName, String demoHost, String appServer, String appName) throws Exception {
        
        init();
        // TO-DO: Add dependency to testDemoAgent test case. Currently Assume demo agents are connected by default
        // Open an Isolated view for 'All Agents' card/tile
        startAgentIsolationView(cardName);
               
        // add filters in new Map tab
        initMapInIsolatedView(agentName, appServer, demoHost, appName);
        
        // check node
        validateMapInIsolatedView(appServer, agentName, new String[]{demoHost}, true);

    }

}
