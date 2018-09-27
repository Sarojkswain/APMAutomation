package com.ca.apm.saas.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.testbed.SaaSDotNetTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * .Net agent tests
 * @author kurma05
 */
public class SaaSDotNetTests extends DotNetAgentBaseTest {
    
    String agentHost = envProperties.getMachinePropertiesById(SaaSDotNetTestBed.MACHINE1_ID).getProperty("hostname");

    @Tas(testBeds = @TestBed(name = SaaSDotNetTestBed.class, executeOn = SaaSDotNetTestBed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentConnected() {
         
        try {    
            init();
            validateAgentsConnected(agentHost);                  
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaaSDotNetTestBed.class, executeOn = SaaSDotNetTestBed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentMap() {
         
        try {    
            init();
            validateDotNetMap("NerdDinnerMVC5", agentHost, 
                new String[]{"Apps|NerdDinnerMVC5", "MVC|Controllers|Dinners"});   
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaaSDotNetTestBed.class, executeOn = SaaSDotNetTestBed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testTTViewer() {
         
        try {    
            init(); 
            sendHttpRequests("http://" + agentHost + ":9091/Dinners", 10);
            validateDotNetTTViewer("NerdDinnerMVC5", agentHost, "9091", "NerdDinnerMVC5", false);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    private void init() throws Exception {
        
        attemptLogin(ui.getDriver());
        mapHelper = new MapPage(ui.getDriver());
    }
}   