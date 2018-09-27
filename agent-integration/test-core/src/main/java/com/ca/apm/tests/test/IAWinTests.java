package com.ca.apm.tests.test;

import java.util.Arrays;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.test.utils.IABomUtil;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.apm.tests.testbed.IATomcatWin2008Testbed;
import com.ca.apm.tests.testbed.IAWinBaseTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class IAWinTests extends InfrastructureBaseTest {
   
    private String host = envProperties.getMachineHostnameByRoleId(IAWinBaseTestbed.TOMCAT_ROLE_ID);
    private String agentMachineId = envProperties.getMachineIdByRoleId(IAWinBaseTestbed.TOMCAT_ROLE_ID);
    private boolean agentStarted = false;
    
    public IAWinTests() {
        wilyDir = IAWinBaseTestbed.TOMCAT_8_HOME + "/wily";
    }
    
    @BeforeMethod(alwaysRun = true)
    public void setup () {
        try {
            mapHelper = new MapPage(ui.getDriver());
        }
        catch (Exception e) {
            Assert.fail("Error occurred during login: " + e.getMessage());
            e.printStackTrace();
        } 
    }
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testInfrastructureAgentInstall() {
         
        try {    
            //update Extensions.profile
            String file = IA_HOME_WIN + "\\extensions\\Extensions.profile";
            HashMap<String,String> replacePairs = new HashMap<String,String>();
            replacePairs.put("introscope.agent.extensions.bundles.load=", 
                "introscope.agent.extensions.bundles.load=HostMonitor");
           
            FileModifierFlowContext context = new FileModifierFlowContext.Builder()
                .replace(file, replacePairs)
                .build();
            runFlowByMachineId(agentMachineId, FileModifierFlow.class, context);
            
            //backup dir
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, "xcopy", "", 
                    Arrays.asList(IA_HOME_WIN, IA_HOME_WIN + ".backup", "/e", "/i", "/h")),
                "Infrastructure agent dir backup didn't succeed.");
            
            //install & start ia                                                                                            
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, IABomUtil.IA_INSTALL_SCRIPT_WIN, IA_HOME_WIN, Arrays.asList("install")),
                "Infrastructure agent installation/startup didn't succeed.");
        }
        catch (Exception e) {
            Assert.fail("Error occurred during IA install: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testHostMonitoring() {
        
        try {            
            //start tomcat
            boolean status = startTomcatwithThieves(agentMachineId);
            Assert.assertTrue(status, "Tomcat didn't start successfully");
            agentStarted = true; 
         
            //check map
            login(ui.getDriver(), host);
            validateAppLayerHost(host, "" + IAWinBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"});
            validateInfraLayerHost(host);
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurred during host monitor validation: " + e.getMessage());
        }
    }
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAppHostCorrelation() {
        
        try {    
            login(ui.getDriver(), host);
            validateHostCorrelation(host, "" + IAWinBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"});            
        }
        catch (Exception e) {
            Assert.fail("Error occurred during correlation validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }      

    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testInfrastructureAgentReInstall() {
        
        try {
            //uninstall
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, IABomUtil.IA_INSTALL_SCRIPT_WIN, 
                    IA_HOME_WIN, Arrays.asList("uninstall")),
                "Infrastructure agent uninstall didn't succeed.");
            
            //restore backup installer
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, "rmdir", "", 
                    Arrays.asList("/Q", "/S", IA_HOME_WIN)),
                "Infrastructure agent dir backup didn't succeed.");
            
            Thread.sleep(3000);
            
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, "xcopy", "", 
                    Arrays.asList(IA_HOME_WIN + ".backup", IA_HOME_WIN, "/e", "/i", "/h")),
                "Infrastructure agent dir backup didn't succeed.");
            
            //install & start ia            
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, IABomUtil.IA_INSTALL_SCRIPT_WIN, IA_HOME_WIN, Arrays.asList("install")),
                "Infrastructure agent installation/startup didn't succeed.");
            
            //validate map
            login(ui.getDriver(), host);
            validateAppLayerHost(host, "" + IAWinBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"});
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent reinstall validation:" + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testInfrastructureAgentForceStart() {
        
        try {
            //uninstall
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, IABomUtil.IA_INSTALL_SCRIPT_WIN, 
                    IA_HOME_WIN, Arrays.asList("uninstall")),
                "Infrastructure agent uninstall didn't succeed.");
            
            //start ia            
            Assert.assertTrue(
                runRemoteCommand(agentMachineId, IABomUtil.IA_INSTALL_SCRIPT_WIN, 
                    IA_HOME_WIN, Arrays.asList("force_start")),
                "Infrastructure agent force start didn't succeed.");
            
            //validate map
            login(ui.getDriver(), host);
            validateInfraLayerAgent(host, "" + IAWinBaseTestbed.TOMCAT_PORT);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent reinstall validation:" + e.getMessage());
            e.printStackTrace();
        }       
    }
     
    @AfterClass(alwaysRun = true)
    public void teardown() throws Exception {
        
        //stop agent
        if(agentStarted) {            
            boolean status = stopTomcatwithThieves(agentMachineId);
            Assert.assertTrue(status, "Tomcat didn't stop successfully");
            agentStarted = false;
        }
    }
}   