package com.ca.apm.saas.test;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.pagefactory.*;
import com.ca.apm.saas.test.utils.SaaSUtils;
import com.ca.apm.saas.test.utils.TestDataProviders;
import com.ca.apm.saas.testbed.SaaSUIKonakartTestbed;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class SaaSEnd2EndTests extends SaaSBaseTest {
    
    private final String DEFAULT_TOMCAT_WILY_HOME = TasBuilder.WIN_SOFTWARE_LOC + "tomcatv80\\wily";
    private boolean agentStarted = false;
    private String appName;

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "akujo01")
    @Test(dataProvider = "agentBundleProps", dataProviderClass = TestDataProviders.class)
    public void testAgentBundles(String agentType, String agentSubType, String appName, 
                                 String appServer, String deployPath) throws Exception{
          
        try {
            //download & start agent, then validate em connectivity
            validateAgentsConnected(ui.getDriver(), agentType,  
                agentSubType, deployPath, appName, appServer, null);
        }
        finally {
            //stop agent
            boolean status = stopAgent(SaaSUtils.getOSType(), agentType, agentSubType, appServer, appName);
            Assert.assertTrue(status, "Application " + appName + " not stopped successfully");     
        }
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")
    @Test(dataProvider = "agentDisconnectedProps", dataProviderClass = TestDataProviders.class)
    public void testAgentDisconnected(String agentType, String agentSubType, String appName, 
                                 String appServer, String deployPath) throws Exception{
          
        //download & start agent, then validate em connectivity
        validateAgentsConnected(ui.getDriver(), agentType,  
            agentSubType, deployPath, appName, appServer, null);
    
        //stop agent
        boolean status = stopAgent(SaaSUtils.getOSType(), agentType, agentSubType, appServer, appName);
        Assert.assertTrue(status, "Application " + appName + " not stopped successfully");
        
        // validate agent is disconnected.
        status = validateAgentsDisconnected(ui.getDriver(), agentType,  
            agentSubType, deployPath, appName, appServer, null);
        
        Assert.assertTrue(status, "Application " + appName + " doesn't show Disconnected status");       
    }

    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "hsiwa01")
    @Test(dataProvider = "demoAppParams", dataProviderClass = TestDataProviders.class)
    public void testDemoAgent(String agentName, String demoHost, String appServer) throws Exception {
       
        //validate em connectivity
        validateDemoAgentsConnected(ui.getDriver(), agentName, demoHost, appServer);
    }
     
    @Tas(testBeds = @TestBed(name = SaaSUIKonakartTestbed.class, executeOn = SaaSUIKonakartTestbed.KONAKART_MACHINE_ID), size = SizeType.MEDIUM, owner = "sinab10")
    @Test(enabled = true, groups = {"saas_konakart"})
    public void testKonakartConnected() throws Exception {
        
        String host = envProperties.getMachineHostnameByRoleId(SaaSUIKonakartTestbed.KONAKART_ROLE_ID);
        this.testAgentConnected("Tomcat", "Spring", "Konakart", host, "tomcat.konakart.install.dir");
    }
    
    @Tas(testBeds = @TestBed(name = SaaSUIKonakartTestbed.class, executeOn = SaaSUIKonakartTestbed.KONAKART_MACHINE_ID), size = SizeType.MEDIUM, owner = "sinab10")
    @Test(enabled = true, groups = {"saas_konakart"})
    public void testKonakartMap() throws Exception {
        
    	//prereq:#testKonakartConnected or install/start tomcat manually
        init();
        String host = envProperties.getMachineHostnameByRoleId(SaaSUIKonakartTestbed.KONAKART_ROLE_ID);
        Assert.assertTrue(startJMeter(host, "" + SaaSUIKonakartTestbed.KONAKART_PORT, "konakart-jmeter.jmx", 120000), 
            "Jmeter script didn't start successfully");
        
        //check map
        String appDisplayName = envProperties.getTestbedPropertyById("konakart.display.name");
        filterMapByAppAndHost(appDisplayName, host);
        validateMap(appDisplayName, new String[]{"JspServlet"}, true);
    }
    
    @Tas(testBeds = @TestBed(name = SaaSUIKonakartTestbed.class, executeOn = SaaSUIKonakartTestbed.KONAKART_MACHINE_ID), size = SizeType.MEDIUM, owner = "sinab10")
    // @Test(enabled = true, groups = {"saas_konakart"})
    //TODO needs to be fixed
    public void testKonakartTTViewer() throws Exception {
        
        //prereq:#testKonakartConnected or install/start tomcat manually
        init();        
        if(wilyDir == null) {
            wilyDir = DEFAULT_TOMCAT_WILY_HOME;
        }
        
        String host = envProperties.getMachineHostnameByRoleId(SaaSUIKonakartTestbed.KONAKART_ROLE_ID);
        String appDisplayName = envProperties.getTestbedPropertyById("konakart.display.name");
        filterMapByAppAndHost(appDisplayName, host);
        validateTTViewer("Tomcat", "Spring", "Konakart", host, "" + SaaSUIKonakartTestbed.KONAKART_PORT);
    }
    
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_thieves"})
    public void testThievesConnected() {
        
        String host = envProperties.getMachineHostnameByRoleId(SaasUITestbed.TOMCAT_ROLE_ID);
        this.testAgentConnected("Tomcat", "Spring", "Thieves", host, "tomcat.thieves.install.dir");
    }
    
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_thieves"})
    public void testThievesMap() throws Exception {
        
        init();

        // Application in current test will always be "Archetype Created Web Application"
        String appFilter = "Archetype Created Web Application";
        
        //start load  
        String host = envProperties.getMachineHostnameByRoleId(SaasUITestbed.TOMCAT_ROLE_ID);
        Assert.assertTrue(startJMeter(host, "" + SaasUITestbed.TOMCAT_PORT, "thieves-slim.jmx", 120000), 
            "Jmeter script didn't start successfully");
        
        //check map
        filterMapByAppAndHost(appFilter, host);
        validateMap(appFilter, new String[] {"Escape|service"});
    }
    
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "kurma05")
    @Test(enabled = true, groups = {"saas_thieves"})
    public void testThievesTTViewer() throws Exception {
        
        init();
        // Application in current test will always be "Archetype Created Web Application"
        String appFilter = "Archetype Created Web Application";

        if(wilyDir == null) {
            wilyDir = DEFAULT_TOMCAT_WILY_HOME;
        }
        
        //send a few requests to generate a map (in case it's not there yet)
        String host = envProperties.getMachineHostnameByRoleId(SaasUITestbed.TOMCAT_ROLE_ID);
        String port = "" + SaasUITestbed.TOMCAT_PORT;
        sendHttpRequests("http://" + host + ":" + port + "/thieves/purchase?event=stall&seconds=1", 10); 
        
        //validate traces
        filterMapByAppAndHost(appFilter, host);
        validateTTViewer("Tomcat", "Spring", appFilter, host, port);
    }
    
    @AfterClass(alwaysRun = true)
    public void teardownTomcatApps() throws Exception {
        
        //stop agent
        if(agentStarted && wilyDir != null && !wilyDir.isEmpty()) {
            boolean status = stopAgent(SaaSUtils.getOSType(), "Tomcat", "Spring", "Tomcat", appName);
            Assert.assertTrue(status, "Application '" + appName + "' not stopped successfully");
            SaaSUtils.revertBackupProperties(
                wilyDir + "/core/config/IntroscopeAgent.profile", "test" + appName);
        }
    }
    
    private void init() throws Exception {
        
        attemptLogin(ui.getDriver());
        mapHelper = new MapPage(ui.getDriver());
        agentsHelper = new AgentsPage(ui.getDriver());
        homeHelper =  new HomePage(ui.getDriver());
    }
    
    private void testAgentConnected(String agentType, String agentSubType, 
                                    String appName, String host, String installDirPropName) {
        
        //update agent props
        HashMap<String,String> agentProfileUpdates = new HashMap<String,String>();
        agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.perinterval.count", "100");
        agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.interval.seconds", "1");
        agentProfileUpdates.put("introscope.agent.stalls.thresholdseconds", "10");
        agentProfileUpdates.put("introscope.agent.stalls.resolutionseconds", "1");
        agentProfileUpdates.put("introscope.agent.errorsnapshots.throttle", "50");
     
        //download & start agent, then validate em connectivity
        try {
            String deployPath = envProperties.getTestbedPropertyById(installDirPropName);
            validateAgentsConnected(ui.getDriver(), agentType, agentSubType, 
                deployPath, appName, agentType, host, agentProfileUpdates);
            this.appName = appName;
            agentStarted = true;            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurrerd during test execution: " + e.getMessage());
        }
    }
}   