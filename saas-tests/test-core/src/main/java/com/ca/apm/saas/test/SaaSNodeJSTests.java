package com.ca.apm.saas.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.testbed.SaasNodeJSTestbed;
import com.ca.apm.saas.testbed.SaasNodeJSTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * NodeJs agent tests
 * @author kurma05
 */
public class SaaSNodeJSTests extends NodeJSAgentBaseTest {
    
    String agentHost = envProperties.getMachinePropertiesById(SaasNodeJSTestbed.MACHINE2_ID).getProperty("hostname");

    @BeforeMethod(alwaysRun = true)
    public void setup () {
        try {
            attemptLogin(ui.getDriver());
            mapHelper = new MapPage(ui.getDriver());
        }
        catch (Exception e) {
            Assert.fail("Error occurred during login: " + e.getMessage());
            e.printStackTrace();
        } 
    }
    
    @Tas(testBeds = @TestBed(name = SaasNodeJSTestbed.class, executeOn = SaasNodeJSTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentConnected() {
         
        try {    
            validateAgentsConnected(agentHost);                  
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaasNodeJSTestbed.class, executeOn = SaasNodeJSTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentMap() {
         
        try {    
            validateNodeJSMap("server", agentHost, new String[]{"/count (GET)"});   
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaasNodeJSTestbed.class, executeOn = SaasNodeJSTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testTTViewer() {
         
        try {    
            validateNodeJsTTViewer("Agent(server)", agentHost, "3000", "server");
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
}   