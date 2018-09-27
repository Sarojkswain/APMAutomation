package com.ca.apm.tests.test;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.apm.tests.testbed.IATomcatWin2008Testbed;
import com.ca.apm.tests.testbed.IAWinBaseTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class TomcatThievesTests extends BaseTest {
   
    private String host = envProperties.getMachineHostnameByRoleId(IAWinBaseTestbed.TOMCAT_ROLE_ID);
    private String agentMachineId = envProperties.getMachineIdByRoleId(IAWinBaseTestbed.TOMCAT_ROLE_ID);
    private boolean agentStarted = false;
    
    public TomcatThievesTests() {
        wilyDir = IAWinBaseTestbed.TOMCAT_8_HOME + "/wily";
    }
    
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
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testThievesStarted() {
        
        try {
            //update agent props
            HashMap<String,String> replacePairs = new HashMap<String,String>();
            replacePairs.put("#introscope.agent.transactiontracer.sampling.perinterval.count=1", 
                "introscope.agent.transactiontracer.sampling.perinterval.count=100");
            replacePairs.put("#introscope.agent.transactiontracer.sampling.interval.seconds=120", 
                "introscope.agent.transactiontracer.sampling.interval.seconds=1");
            replacePairs.put("introscope.agent.stalls.thresholdseconds=30", 
                "introscope.agent.stalls.thresholdseconds=10");
            replacePairs.put("introscope.agent.stalls.resolutionseconds=10", 
                "introscope.agent.stalls.resolutionseconds=1");
            replacePairs.put("introscope.agent.errorsnapshots.throttle=10", 
                "introscope.agent.errorsnapshots.throttle=50");
       
            String file = wilyDir + "/core/config/IntroscopeAgent.profile";           
            FileModifierFlowContext context = new FileModifierFlowContext.Builder()
                .replace(file, replacePairs)
                .build();
            runFlowByMachineId(agentMachineId, FileModifierFlow.class, context);
            
            //start tomcat            
            boolean status = startTomcatwithThieves(agentMachineId);
            Assert.assertTrue(status, "Tomcat didn't start successfully");
            agentStarted = true;            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurred during test execution: " + e.getMessage());
        }
    }
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testThievesMap() throws Exception {
        
        //send a few requests to generate a map for the specific subnode
        sendHttpRequests("http://" + host + ":" + IAWinBaseTestbed.TOMCAT_PORT + 
            "/thieves/escape?event=stall&seconds=1", 10);         
        //start additional jmeter load  
        Assert.assertTrue(startJMeter(host, "" + IAWinBaseTestbed.TOMCAT_PORT, "thieves-slim.jmx", 120000), 
            "Jmeter script didn't start successfully");
        
        //check map
        filterMapByAppAndHost("Thieves", host);
        validateMap("Thieves", new String[]{"Escape|service"});
    }
    
    @Tas(testBeds = @TestBed(name = IATomcatWin2008Testbed.class, executeOn = BaseTestbed.MACHINE1_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true)
    public void testThievesTTViewer() throws Exception {
        
        //send a few requests to generate a map (in case it's not there yet)        
        sendHttpRequests("http://" + host + ":" + IAWinBaseTestbed.TOMCAT_PORT + 
            "/thieves/purchase?event=stall&seconds=1", 10); 
        
        //validate traces
        filterMapByAppAndHost("Thieves", host);
        validateTTViewer("Tomcat", "Spring", "Thieves", host, "" + IAWinBaseTestbed.TOMCAT_PORT);
    }
     
    @AfterClass(alwaysRun = true)
    public void teardownTomcatApps() throws Exception {
        
        //stop agent
        if(agentStarted) {            
            boolean status = stopTomcatwithThieves(agentMachineId);
            Assert.assertTrue(status, "Tomcat didn't stop successfully");
            agentStarted = false;
        }
    }
}   