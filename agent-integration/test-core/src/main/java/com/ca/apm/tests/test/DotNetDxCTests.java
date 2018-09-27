package com.ca.apm.tests.test;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.role.DXCRole;
import com.ca.apm.tests.role.KafkaZookeeperRole;
import com.ca.apm.tests.role.LogstashRole;
import com.ca.apm.tests.test.utils.Utils;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.apm.tests.testbed.DotNetDxcTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * .Net agent tests
 * @author kurma05
 */
public class DotNetDxCTests extends DotNetAgentBaseTest {
    
    String agentEmHost = envProperties.getMachinePropertiesById(BaseTestbed.MACHINE1_ID).getProperty("hostname");
    String dxcHost = envProperties.getMachinePropertiesById(BaseTestbed.MACHINE2_ID).getProperty("hostname");
    
    public DotNetDxCTests() {
        
        String shouldSetupDxC = System.getProperty("setup.dxc");

        if(shouldSetupDxC == null || shouldSetupDxC.equalsIgnoreCase("true")) {   
            runSerializedCommandFlowFromRoleAsync(DotNetDxcTestBed.KAFKA_ROLE_ID,
                KafkaZookeeperRole.ZOOKEEPER_START);
            runSerializedCommandFlowFromRoleAsync(DotNetDxcTestBed.KAFKA_ROLE_ID,
                KafkaZookeeperRole.KAFKA_START);
            runSerializedCommandFlowFromRoleAsync(DotNetDxcTestBed.LOGSTASH_ROLE_ID,
                LogstashRole.LOGSTASH_APM_START);
            runSerializedCommandFlowFromRoleAsync(DotNetDxcTestBed.KAFKA_ROLE_ID,
                KafkaZookeeperRole.CREATE_TOPIC);
            runSerializedCommandFlowFromRoleAsync(DotNetDxcTestBed.DXC_ROLE_ID,
                DXCRole.DXC_START);
            runSerializedCommandFlowFromRoleAsync(DotNetDxcTestBed.DXC_ROLE_ID,
                DXCRole.UPLOAD_BA);    
        }
    }
   
    @BeforeMethod(alwaysRun = true)
    public void setupMethod () {
        try {           
            mapHelper = new MapPage(ui.getDriver());
        }
        catch (Exception e) {
            Assert.fail("Error occurred during login: " + e.getMessage());
            e.printStackTrace();
        } 
    }
    
    @Tas(testBeds = @TestBed(name = DotNetDxcTestBed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentMap() {
         
        try {
            //update profile
            HashMap<String,String> agentProfileUpdates = new HashMap<String,String>();
            agentProfileUpdates.put("introscope.agent.browseragent.autoInjectionEnabled", "true");
            agentProfileUpdates.put("introscope.agent.browseragent.snippetLocation", "brtmsnippet.js");
            agentProfileUpdates.put("introscope.agent.browseragent.responseCookieEnabled", "true");            
            agentProfileUpdates.put("introscope.agent.browseragent.dxchost", dxcHost);
            agentProfileUpdates.put("introscope.agent.browseragent.dxcport", "8080");            
            agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.perinterval.count", "100");
            agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.interval.seconds", "1");              
            agentProfileUpdates.put("introscope.agent.stalls.thresholdseconds", "10");
            agentProfileUpdates.put("introscope.agent.stalls.resolutionseconds", "1");
            agentProfileUpdates.put("introscope.agent.errorsnapshots.throttle", "50");
            
            Utils.updateProperties(DOTNET_AGENT_PROFILE, agentProfileUpdates);
            
            //validate map
            login(ui.getDriver(), agentEmHost);
            validateDotNetMap("NerdDinnerMVC5", agentEmHost, 
                new String[]{"Apps|NerdDinnerMVC5", "MVC|Controllers|Dinners"}, true);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error occurred during map validation: " + e.getMessage());
        } 
    }
    
    @Tas(testBeds = @TestBed(name = DotNetDxcTestBed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testTTViewer() {
         
        try {         
            sendHttpRequests("http://" + agentEmHost + ":9091/Dinners", 10);
            login(ui.getDriver(), agentEmHost);
            validateDotNetTTViewer("NerdDinnerMVC5", agentEmHost, "9091", "NerdDinnerMVC5", true);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error occurred during ttviewer validation: " + e.getMessage());
        }       
    }
}  