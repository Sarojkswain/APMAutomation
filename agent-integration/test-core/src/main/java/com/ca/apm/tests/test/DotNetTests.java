package com.ca.apm.tests.test;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.test.utils.Utils;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.apm.tests.testbed.DotNetTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * .Net agent tests
 * @author kurma05
 */
public class DotNetTests extends DotNetAgentBaseTest {
    
    String host = envProperties.getMachinePropertiesById(BaseTestbed.MACHINE1_ID).getProperty("hostname");
    
    @BeforeMethod(alwaysRun = true)
    public void setup () {
        try {
            login(ui.getDriver(), host);
            mapHelper = new MapPage(ui.getDriver());
        }
        catch (Exception e) {
            Assert.fail("Error occurred during login: " + e.getMessage());
            e.printStackTrace();
        } 
    }
    
    @Tas(testBeds = @TestBed(name = DotNetTestBed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testAgentMap() {
         
        try {    
            //update profile
            HashMap<String,String> agentProfileUpdates = new HashMap<String,String>();
            agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.perinterval.count", "100");
            agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.interval.seconds", "1");              
            agentProfileUpdates.put("introscope.agent.stalls.thresholdseconds", "10");
            agentProfileUpdates.put("introscope.agent.stalls.resolutionseconds", "1");
            agentProfileUpdates.put("introscope.agent.errorsnapshots.throttle", "50");
            
            Utils.updateProperties(DOTNET_AGENT_PROFILE, agentProfileUpdates);
      
            //validate map
            validateDotNetMap("NerdDinnerMVC5", host, 
                new String[]{"Apps|NerdDinnerMVC5", "MVC|Controllers|Dinners"});             
        }
        catch (Exception e) {
            Assert.fail("Error occurred during map validation: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = DotNetTestBed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testTTViewer() {
         
        try {         
            sendHttpRequests("http://" + host + ":9091/Dinners", 10);
            validateDotNetTTViewer("NerdDinnerMVC5", host, "9091", "NerdDinnerMVC5", false);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error occurred during ttviewer validation: " + e.getMessage());
        }       
    }
}   