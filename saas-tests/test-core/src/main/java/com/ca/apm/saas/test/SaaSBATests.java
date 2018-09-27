package com.ca.apm.saas.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.AxaApmPage;
import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.testbed.SaaSBATestbed;
import com.ca.apm.test.atc.common.Canvas;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


/**
 * Test class for Browser agent
 * @author akujo01
 *
 */
public class SaaSBATests extends BrowserAgentBaseTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SaaSBATests.class); 
    private static String testApp = "BRTMTestApp";
    
    @Tas(testBeds = @TestBed(name = SaaSBATestbed.class, executeOn = SaaSBATestbed.MACHINE1WIN_ID), size = SizeType.MEDIUM, owner = "akujo01")
    @Test(enabled = true, groups = {"saas_ba"})
    public void testBrowserAgentConnected() {
         
        try {    
            attemptLogin(ui.getDriver());
            String agentHost = envProperties.getMachinePropertiesById(SaaSBATestbed.MACHINE2RH_ID).getProperty("hostname");
            testAgentConnected(agentHost, "Tomcat", "Spring", testApp);
        }
        catch (Exception e) {
            Assert.fail("Error occurred during testing: " + e.getMessage());
            e.printStackTrace();
        }       
    }
    
    @Tas(testBeds = @TestBed(name = SaaSBATestbed.class, executeOn = SaaSBATestbed.MACHINE1WIN_ID), size = SizeType.MEDIUM, owner = "akujo01")
    @Test(enabled = true, groups = {"saas_ba"})
	public void testBrowserAgentMapView() throws Exception {

        String agentHost = envProperties.getMachinePropertiesById(SaaSBATestbed.MACHINE2RH_ID).getProperty("hostname");
        String url = "http://" + agentHost + ":" + TOMCAT_PORT + "/" + testApp.toLowerCase() + "/GETLocalDomain.jsp";
   
        RemoteWebDriver driver = ui.getDriver();
    	attemptLogin(driver);    	
    	startTxnTrace(AGENT_NAME, agentHost);    
    	sendHttpRequests(url, 10, true);
    	    
    	logger.info("Sleeping for 5 sec to allow for Browser metrics to be collected for {}", url);
    	Thread.sleep(5000);
    	
		MapPage mapHelper = new MapPage(driver);
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view."); 
		mapHelper.clickLiveTimeRange();
		Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");  
		Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", new String[] { agentHost }), "Unable to add a host filter for host " + agentHost);
		Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check if map was unable to load and hanging.");
				
		Canvas canvas = ui.getCanvas();
		canvas.waitForUpdate();
		
		 Map<String,String> nodes = new HashMap<>();
		 nodes.put("/brtmtestapp/GETLocalDomain.jsp", "BAMetrics");
		 nodes.put("/brtmtestapp/framework.pack.js.seam", "resMetrics");
		 
		Iterator<String> keySetIterator = nodes.keySet().iterator();
		while(keySetIterator.hasNext()){ 
			String nodeToSelect = keySetIterator.next(); 
			String nodeType = nodes.get(nodeToSelect); 
			verifyMapMetrics(canvas, mapHelper, nodeToSelect,  nodeType);
		}
	}
    
    @Tas(testBeds = @TestBed(name = SaaSBATestbed.class, executeOn = SaaSBATestbed.MACHINE1WIN_ID), size = SizeType.MEDIUM, owner = "akujo01")
    @Test(enabled = true, groups = {"saas_ba"})
	public void testBrowserAgentTTViewer() throws Exception {
				
        String agentHost = envProperties.getMachinePropertiesById(SaaSBATestbed.MACHINE2RH_ID).getProperty("hostname");
        //http://agenthost:8080/brtmtestapp/GETLocalDomain.jsp
        String url = "http://" + agentHost + ":" + TOMCAT_PORT + "/" + testApp.toLowerCase() + "/GETLocalDomain.jsp";
        
        RemoteWebDriver driver = ui.getDriver();
    	attemptLogin(driver);    	
   
    	//Start a transaction trace session
    	startTxnTrace(AGENT_NAME, agentHost);
    	sendHttpRequests(url, 10, true);
        
    	//Generate a transaction
    	logger.info("Sleeping for 5 sec to allow for Browser metrics to be collected");
    	Thread.sleep(5000);
    	
    	Canvas canvas = ui.getCanvas();
		canvas.waitForUpdate();
		String nodeToSelect = new String ("/brtmtestapp/GETLocalDomain.jsp");
		validateTraces(canvas, driver, agentHost, nodeToSelect);
	}
    
    
    @Tas(testBeds = @TestBed(name = SaaSBATestbed.class, executeOn = SaaSBATestbed.MACHINE1WIN_ID), size = SizeType.MEDIUM, owner = "akujo01")
    @Test(enabled = true, groups = {"saas_ba"})
	public void testAxaApmIntegration() throws IOException, InterruptedException, Exception {
	
        String agentHost = envProperties.getMachinePropertiesById(SaaSBATestbed.MACHINE2RH_ID).getProperty("hostname");
        //http://agenthost:8080/brtmtestapp/GETLocalDomain.jsp
        String url = "http://" + agentHost + ":" + TOMCAT_PORT + "/" + testApp.toLowerCase() + "/GETLocalDomain.jsp";
        
        RemoteWebDriver driver = ui.getDriver();
    	attemptLogin(driver);    	
   
    	//Start a transaction trace session
    	startTxnTrace(AGENT_NAME, agentHost);
    	sendHttpRequests(url, 10, true);
    	
    	//Generate a transaction
    	logger.info("Sleeping for 5 sec to allow for Browser metrics to be collected");
    	Thread.sleep(5000);
    	    	
    	//retrieve axa app name from the file generated while running BAConfig.sh
    	String axaApp = null;
    	try{
	    	BufferedReader br = new BufferedReader(new FileReader("axa_app_name.txt"));
	    	axaApp = br.readLine();
	    	br.close();
    	} catch(IOException ioe){
    		ioe.printStackTrace();
    		Assert.fail("Unable to read AXA app name from the file axa_app_name.txt");
    	}
    	logger.info("AXA App name retrieved from the file is {}", axaApp);    	
    	
    	product = "AXA";
    	AxaApmPage objAxaApm = new AxaApmPage(driver);
    	Thread.sleep(5000);
    	Assert.assertTrue(objAxaApm.clickProductGridIcon(product), "Unable to click Product Grid Icon and navigate to product " + product);
    	Assert.assertTrue(objAxaApm.clickManageAppsLink(), "Unable to click Manage apps link using xpath //span[contains(@class,'section-title ng-binding') and contains(text(),'Manage Apps')]");
        Assert.assertTrue(objAxaApm.clickAxaAppLink(axaApp), "Unable to find/click "+ axaApp + " app using xpath //h4[contains(text(),'" + axaApp + "')]" );
    	Assert.assertTrue(objAxaApm.clickApmSetupLink(),"Unable to click APM Setup link for app " + axaApp + "{} using xpath //a/span/span[contains(text(),'APM Setup')]");
    	Assert.assertTrue(objAxaApm.setApmHostURL(instance), "Unable to set APM Host URL for " + instance);
    	Assert.assertTrue(objAxaApm.setApmConfig("SaaS"),"Unable to set APM config version to 'SAAS'");
    	Assert.assertTrue(objAxaApm.generateURL(),"Unable to click generate URL.");
    	Assert.assertTrue(objAxaApm.saveURL(),"Unable to save generated URL.");
     	Assert.assertTrue(objAxaApm.closeApmSetup(),"Unable to close the window to setup APM.");
     	Assert.assertTrue(objAxaApm.clickSessionsLink(driver, instance),"Unable to click Sessions link");
     	Assert.assertTrue(objAxaApm.filterSessions(axaApp),"Unable to find sessions for " + axaApp);
     	Assert.assertTrue(objAxaApm.clickDeepLink(),"Unable to find/click deep link "); 	
    	logger.info("Sleeping for 5 sec");
    	Thread.sleep(5000);
	} 

 
}   