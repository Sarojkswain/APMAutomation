package com.ca.apm.tests.test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.apm.tests.test.utils.IABomUtil;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.apm.tests.testbed.NodeJsRedHat7Testbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * NodeJs agent tests
 * @author kurma05
 */
public class NodeJSTests extends NodeJSAgentBaseTest {
    
    String host = envProperties.getMachinePropertiesById(BaseTestbed.MACHINE2_ID).getProperty("hostname");
    
    @BeforeMethod(alwaysRun = true)
    public void setup () {
        mapHelper = new MapPage(ui.getDriver());
    }

    @Tas(testBeds = @TestBed(name = NodeJsRedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testNodeJSInstall() {
         
        try {
            //update Extensions.profile
            String property = "introscope.agent.extensions.bundles.load";
            String file = IA_HOME_LINUX + "/extensions/Extensions.profile";
            Assert.assertTrue(runRemoteCommand(BaseTestbed.MACHINE2_ID, "sed", "", 
                Arrays.asList("-i", "s/" + property + "=/" + property + "=NodeExtension/g", file)),
                "Updating Extensions.profile didn't succeed.");
        
            //install & start ia                                                                                              
            Assert.assertTrue(
                runRemoteCommand(BaseTestbed.MACHINE2_ID, IABomUtil.IA_INSTALL_SCRIPT_UNIX, IA_HOME_LINUX, Arrays.asList("install")),
                "Infrastructure agent installation/startup didn't succeed.");
            
            //start nodejs app
            runSerializedCommandFlowFromRole(NodeJsRedHat7Testbed.TIXCHANGE_ROLE_ID,
                TixChangeRole.ENV_TIXCHANGE_START);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent install: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = NodeJsRedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentMap() {
         
        try {          
            login(ui.getDriver(), host);
            validateNodeJSMap("server", host, new String[]{"/count (GET)"});   
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = NodeJsRedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testTTViewer() {
         
        try {    
            login(ui.getDriver(), host);
            validateNodeJsTTViewer("Agent(server)", host, "3000", "server");
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
}   