package com.ca.apm.tests.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.tests.pagefactory.HomePage;
import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.apm.tests.testbed.IALinuxBaseTestbed;
import com.ca.apm.tests.testbed.IARedHat7Testbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test class for Infrastructure agent
 * @author kurma05
 */
public class IATests extends InfrastructureBaseTest {
    
    private String host = envProperties.getMachinePropertiesById(
        BaseTestbed.MACHINE2_ID).getProperty("hostname");
    
    @BeforeMethod(alwaysRun = true)
    public void setup () {
        mapHelper = new MapPage(ui.getDriver());
        homeHelper = new HomePage(ui.getDriver());
    }
        
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testInstallHostExtension() {
        
        installLinuxAgentOnPrem(host, BaseTestbed.MACHINE2_ID, false);
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testInstallDockerHostExtension() {
        
        //standalone docker not supported for now
        //installLinuxAgentOnPrem(host, BaseTestbed.MACHINE2_ID, true);
        
        //docker monitor via compose
        installDockerMonitor(host, BaseTestbed.MACHINE2_ID);
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testHostMonitoring() {
         
        try {    
            //start tomcat
            startTomcatLinux(BaseTestbed.MACHINE2_ID, IALinuxBaseTestbed.TOMCAT_LINUX_HOME);
            
            //validate map            
            login(ui.getDriver(), host);
            validateAppLayerHost(host, "" + IALinuxBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"});     
            validateInfraLayerHost(host); 
        }
        catch (Exception e) {
            Assert.fail("Error occurred during map validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testDockerHostMonitoring() {
         
        try {    
            login(ui.getDriver(), host);
            validateAppLayerDockerHost(host);     
            validateInfraLayerDockerHost(host);  
        }
        catch (Exception e) {
            Assert.fail("Error occurred during map validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testDockerHostMonitoringAndAlerts() {
         
        try {    
            login(ui.getDriver(), host);
            validateAppLayerDockerHost(host, true);     
            validateInfraLayerDockerHost(host, true);  
        }
        catch (Exception e) {
            Assert.fail("Error occurred during map validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testExperienceViewAlerts() {
         
        try {    
            login(ui.getDriver(), host);
            validateFrontendProblems(host);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during experience view validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testAppHostCorrelation() {
         
        try {    
            login(ui.getDriver(), host);
            validateHostCorrelation(host,  "" + IALinuxBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"});
        }
        catch (Exception e) {
            Assert.fail("Error occurred during correlation validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }    
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testAppDockerHostCorrelation() {
         
        try {    
            login(ui.getDriver(), host);
            validateDockerHostCorrelation(host);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during correlation validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }    
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testReInstallHostExtension() {
        
        try {
            reInstallLinuxAgentOnPrem(host, BaseTestbed.MACHINE2_ID, "install");
            login(ui.getDriver(), host);
            validateAppLayerHost(host, "" + IALinuxBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"}); 
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent reinstall validation:" + e.getMessage());
            e.printStackTrace();
        } 
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testReInstallDockerHostExtension() {
        
        try {
            reInstallLinuxAgentOnPrem(host, BaseTestbed.MACHINE2_ID, "install");
            login(ui.getDriver(), host);
            validateAppLayerDockerHost(host);  
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent reinstall validation:" + e.getMessage());
            e.printStackTrace();
        } 
    }    
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testForceStartHostExtension() {
        
        try {
            reInstallLinuxAgentOnPrem(host, BaseTestbed.MACHINE2_ID, "force_start");
            login(ui.getDriver(), host);
            validateAppLayerHost(host, "" + IALinuxBaseTestbed.TOMCAT_PORT, 
                new String[]{"Apps|Servlet", "JspServlet"});    
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent reinstall validation:" + e.getMessage());
            e.printStackTrace();
        } 
    }
    
    @Tas(testBeds = @TestBed(name = IARedHat7Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"onprem_ia"})
    public void testForceStartDockerHostExtension() {
        
        try {
            reInstallLinuxAgentOnPrem(host, BaseTestbed.MACHINE2_ID, "force_start");
            login(ui.getDriver(), host);
            validateAppLayerDockerHost(host);  
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent reinstall validation:" + e.getMessage());
            e.printStackTrace();
        } 
    }    
}   