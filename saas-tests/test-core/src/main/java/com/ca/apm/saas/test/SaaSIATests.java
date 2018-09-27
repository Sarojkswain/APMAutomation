package com.ca.apm.saas.test;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.DownloadAgentPage;
import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.testbed.SaasIATestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test class for Infrastructure agent
 * @author kurma05
 *
 */
public class SaaSIATests extends InfrastructureBaseTest {
    
    private String agentHost = envProperties.getMachinePropertiesById(
        SaasIATestbed.MACHINE2_ID).getProperty("hostname");

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
    
    @Tas(testBeds = @TestBed(name = SaasIATestbed.class, executeOn = SaasIATestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_ia"})
    public void testInfrastructureAgentInstall() {
         
        try {    
            setupIA(agentHost);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during agent install: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaasIATestbed.class, executeOn = SaasIATestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_ia"})
    public void testInfrastructureAgentConnected() {
         
        try {    
            validateIAConnection(agentHost);   
        }
        catch (Exception e) {
            Assert.fail("Error occurred during connection check: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaasIATestbed.class, executeOn = SaasIATestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_ia"})
    public void testInfrastructureAgentMap() {
         
        try {    
            validateInfraLayerDockerHost(agentHost);            
            validateAppLayerDockerHost(agentHost);            
        }
        catch (Exception e) {
            Assert.fail("Error occurred during map validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaasIATestbed.class, executeOn = SaasIATestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_ia"})
    public void testInfrastructureAgentCorrelation() {
         
        try {    
            validateDockerHostCorrelation(agentHost);            
        }
        catch (Exception e) {
            Assert.fail("Error occurred during correlation validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaasIATestbed.class, executeOn = SaasIATestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_ia"})
    public void testInfrastructureAgentBom() {
        
        try {    
            DownloadAgentPage objDownloadAgent = new DownloadAgentPage(ui.getDriver()); 
            
            HashMap<String,String> packages = new HashMap<String,String>();
            packages.put("docker-host-monitoring", "Infrastructure_Agent_-_Docker_-_Host_Monitoring");
            packages.put("docker-monitoring", "Infrastructure_Agent_-_Docker_Monitoring");
            packages.put("host-monitoring", "Infrastructure_Agent_-_Host_Monitoring");
            packages.put("openshift-monitoring", "Infrastructure_Agent_-_Openshift_Monitoring"); 
            
            for (Map.Entry<String, String> agentPackage : packages.entrySet()) {            
                String packagePath = objDownloadAgent.downloadAgentPackage("Unix", agentPackage.getValue(), "infrastructure-agent", agentPackage.getKey());        
                Assert.assertTrue(packagePath != null, "Download didn't succeed for IA Host monitoring package " + agentPackage.getValue());
                validatePackageContents(packagePath, agentPackage.getKey());                
                objDownloadAgent.closeDownloadAgent();
            }
        }
        catch (Exception e) {
            Assert.fail("Error occurred during bom validation: " + e.getMessage());
            e.printStackTrace();
        }     
    }
}   