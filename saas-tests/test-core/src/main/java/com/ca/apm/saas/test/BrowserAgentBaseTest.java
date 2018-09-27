package com.ca.apm.saas.test;

import static com.ca.apm.test.atc.common.element.WebElementWrapper.wrapElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.saas.pagefactory.AgentsPage;
import com.ca.apm.saas.pagefactory.DownloadAgentPage;
import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.test.utils.SaaSUtils;
import com.ca.apm.saas.testbed.SaaSBATestbed;
import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;

/**
 * Test base class for Browser agent 
 * @author akujo01
 *
 */
public class BrowserAgentBaseTest extends SaaSBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BrowserAgentBaseTest.class);
    protected static final int TOMCAT_PORT = SaaSBATestbed.TOMCAT_PORT;
    protected static final String TOMCAT_HOME = SaaSBATestbed.TOMCAT_8_HOME;
    protected static final String AGENT_NAME = SaaSBATestbed.TOMCAT_AGENT_NAME;
//    protected static boolean agentLessContainerConfigured = false;
  
    protected void testAgentConnected(String agentHost, String agentPackage, String agentBundle, String testApp) throws Exception {
        
        DownloadAgentPage objDownloadAgent = new DownloadAgentPage(ui.getDriver()); 
        //download tomcat java agent
        String packagePrefix = "Tomcat_-_Spring";
        String agentPackageDownload = objDownloadAgent.downloadAgentPackage("Unix", packagePrefix, agentPackage, agentBundle);        
        Assert.assertTrue(agentPackageDownload != null, "Download didn't succeed for tomcat package.");    
        objDownloadAgent.closeDownloadAgent();
        
        //copy packages to remote machine agentHost from local path  tomcatPackage to destination folder  tomcatInstallDir (on agentHost)    
        copyPackageRemotely(SaaSBATestbed.MACHINE1WIN_ID, agentHost, agentPackageDownload,TOMCAT_HOME );
        
        //deploy agent
        installAndStartAgent(SaaSBATestbed.MACHINE2RH_ID, TOMCAT_HOME + "/" + new File(agentPackageDownload).getName(), agentHost);
        
        //verify agent connected to EM 
        checkAgentConnectedUI(objDownloadAgent, agentHost, AGENT_NAME, agentPackage, agentBundle,  testApp );
    }    
 
    protected void startTxnTrace(String agentName, String host) throws Exception {
        
         //start tt session
         AgentsPage agentPage = new AgentsPage(ui.getDriver());
         agentPage.clickAgentsTab();
         //sort for agents to be sorted in descending order of host names to locate easily
         agentPage.sortAgentsByHost();
         agentPage.sortAgentsByHost();
         agentPage.startTransactionTrace(agentName, host);       
     }
    private void installAndStartAgent(String machineId, String installerPath, String agentHost) {
             
        String baConfig = "/wily/BAConfig.sh";
        
        logger.info("Installing agent {}..." + installerPath);
         //untar downloaded agent package
        Assert.assertTrue(
        	runRemoteCommand(machineId, "tar", "", Arrays.asList("-xf", installerPath, "-C", TOMCAT_HOME)),
        		"Unpacking " + installerPath + " didn't succeed.");      
      
        updateBAConfig(machineId, TOMCAT_HOME + baConfig);
     
        logger.info("Starting agent...");
		Assert.assertTrue(
				runRemoteCommand(machineId, "catalina.sh", 
						TOMCAT_HOME + "/bin", 
						Arrays.asList("start")),
						"Starting Tomcat didn't succeed.");		
	}
   
    public void updateBAConfig(String machineId, String baConfig){
        

    	String axaApp = "BRTMTestApp";
    	PrintWriter writer = null;
    	//BAConfig.sh changes.
		HashMap<String,String> config = new HashMap<String,String>();		
		instance.toLowerCase();
		switch(instance){
			case "production":				
				break;
			case "staging":
				config.put("collector-axa.cloud", "dxc-route-8080-axa-ng.app.unvnp1.cs.saas");
				config.put("cloud.ca.com","adminui-route-8080-axa-ng.app.unvnp1.cs.saas.ca.com");
				break;
			case "dev":
				config.put("collector-axa.cloud", "cxc-route-edge-axa.app.unvdev1.cs.saas");
				config.put("cloud.ca.com","adminui-route-edge-axa.app.unvdev1.cs.saas.ca.com");					
				break;
			default:
				logger.error("Invalid instance {}", instance);
				break;

		}

		//Updates for all instances. Using Pattern/Matcher to escape $ 
		config.put(Pattern.quote("COHORT=$3"), Matcher.quoteReplacement("COHORT=$USER"));
		config.put(Pattern.quote("read -p \"Is the tenand id the same as the username ($USER): Y/N \"  TOPT"), Matcher.quoteReplacement("TOPT=Y"));
		config.put("read -s -p \"Enter password: \" PASS", "PASS=" + password);
		
		FileModifierFlowContext context = new FileModifierFlowContext.Builder()
				.replace(baConfig, config)
				.build();
		runFlowByMachineId(SaaSBATestbed.MACHINE2RH_ID, FileModifierFlow.class, context);		
		
		axaApp += new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		
		//Create a new app name with every deployment and write to a text file  
		//to prevent testing with stale data from previous automation runs.
		try{
			writer = new PrintWriter("axa_app_name.txt", "UTF-8");
		}catch(FileNotFoundException fne){
			fne.printStackTrace();
			Assert.fail("Unable to write AXA app name to file axa_app_name.txt {}" + fne.getMessage());
		}
		catch(UnsupportedEncodingException use) {
			use.printStackTrace();	
			Assert.fail("Unable to write AXA app name to file axa_app_name.txt {}" + use.getMessage());
		}
		writer.println(axaApp);
		writer.close();
		
		Assert.assertTrue(
				runRemoteCommand(machineId, "BAConfig.sh", 
						TOMCAT_HOME + "/wily", 
						Arrays.asList(email, axaApp ,  TOMCAT_HOME + "/wily")),
        		"Running BAConfig.sh to configure Browser Agent didn't succeed.");	
    }
    
    protected void validateBAMetricData(MapPage mapHelper, String nodeName)
			throws Exception {
		String[] BTMetricData = new String[] { 
				"Average Response Time (ms)",
				"Responses Per Interval", 
				"Average Page Stall Time (ms)",
				"Average Page Render Time (ms)",
				"Average Connection Establishment Time (ms)",
				"Average Domain Lookup Time (ms)",
				"Average DOM Processing Time (ms)",
				"Average Previous Page Unload Time (ms)",
				"Average Page Load Time (ms)",
				"Average Time to First Byte (ms)",
				"Average Time to Last Byte (ms)", 
				"Page Hits Per Interval"
				};
	
		for (String metric : BTMetricData) {
			Assert.assertTrue(mapHelper.isMetricPresent(metric),
					"Metric/Attribute '" + metric + "' doesn't exist");
		}
		
	}
    
    protected void validateResData(MapPage mapHelper, String nodeName)
			throws Exception {
    	
		String[] resMetricData = new String[] { 
				"Average Response Time (ms)",
				"Errors Per Interval",
				};
		
		for (String metric : resMetricData) {
			Assert.assertTrue(mapHelper.isMetricPresent(metric),
					"Metric/Attribute '" + metric + "' doesn't exist");
		}
		
	}
    
	protected void verifyMapMetrics(Canvas canvas, MapPage mapHelper, String nodeToSelect, String nodeType) throws Exception {

		logger.info("Checking transaction traces and metrics in 60 sec for " + nodeToSelect);

		//need wait time for map to show the node.
	    long startTime = System.currentTimeMillis();
        long maxWaitTime = 300000;
        long pollFrequency = 30000;
        int i = 0;

        while((System.currentTimeMillis() - startTime) < maxWaitTime) {
            logger.info("Waiting for Node to appear {} upto max {} mins... ", nodeToSelect,maxWaitTime/60000 );
            try{
            	switch(nodeType.toLowerCase()){         	
	            	case "bametrics":
	            		canvas.selectNodeByNameExactMatch(nodeToSelect);
	            		break;
	            	case "resmetrics":
	            		canvas.selectNodeByName(nodeToSelect);
	            		break;
	            	default: 
	            		logger.info("Invalide node type {}, skipping node validation. Supported node types are BAMetrics & resMetrics} ", nodeType);
	            		break;
	            		
            	}
	            logger.info("Node {} found. Fitting selected node to view" , nodeToSelect);
            	break;
            }catch(NoSuchElementException nse){
            	logger.info("Will check again for node {} to appear in {} seconds.  Attempt #", nodeToSelect, pollFrequency, (i+1));
            	Thread.sleep(pollFrequency);
            	continue;
            }
        }
		canvas.getCtrl().fitSelectedToView();
		Thread.sleep(20000);
		if (SaaSUtils.containsIgnoreCase(nodeType, "resMetrics")){
			logger.info("Verifying resource metrics for {}", nodeToSelect);
			validateResData(mapHelper, nodeToSelect);
		}	else {
			if (SaaSUtils.containsIgnoreCase(nodeType, "BAMetrics")) { 
				logger.info("Verifying BA metrics for {}", nodeToSelect);
				validateBAMetricData(mapHelper, nodeToSelect);
			}else
				Assert.fail("Invalid type " + nodeType + "for node " + nodeToSelect);
		}
		Thread.sleep(5000);
		
	}
	
	protected void validateTraces(Canvas canvas, RemoteWebDriver driver, String agentHost, String nodeToSelect) throws Exception {

	   	//navigate to map view
		logger.info("Navigating to map view");
		MapPage mapHelper = new MapPage(driver);
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view."); 
		mapHelper.clickLiveTimeRange();
		Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");  
		Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", new String[] { agentHost }), "Unable to add a host filter for host " + agentHost);
		Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check if map was unable to load and hanging.");
	  	
	   	logger.info("Validating TTViewer. Waiting 20 sec for trace...");
		//need wait time for map to show the node.
	    long startTime = System.currentTimeMillis();
        long maxWaitTime = 300000;
        long pollFrequency = 30000;
        int i = 0;

        while((System.currentTimeMillis() - startTime) < maxWaitTime) {
            logger.info("Waiting for Node to appear {} upto max {} mins... ", nodeToSelect, maxWaitTime/60000 );
            try{
            	canvas.selectNodeByNameExactMatch(nodeToSelect);
            	logger.info("Node {} found. Fitting selected node to view" , nodeToSelect);
            	break;
            }catch(NoSuchElementException nse){
            	logger.info("Will check again for node {} to appear in {} seconds.  Attempt #", nodeToSelect, pollFrequency, (i+1));
            	Thread.sleep(pollFrequency);
            	continue;
            }
        }
		canvas.getCtrl().fitSelectedToView();
		Thread.sleep(5000);
		WebElement bottomDrawerButton = Utils.waitForCondition(
						ui.getDriver(),
						ExpectedConditions.visibilityOfElementLocated(
								By.xpath("//*[contains(@id, 'bottomDrawerMaximizeButton')]")),
						120);
		new Actions(ui.getDriver()).moveToElement(bottomDrawerButton).click()
				.perform();

		WebElement transactionTracesTabHandle = Utils.waitForCondition(ui
				.getDriver(), ExpectedConditions.visibilityOfElementLocated(By
				.xpath("//a[contains(text(), 'Business Transactions')]")), 120);

		new Actions(ui.getDriver()).moveToElement(transactionTracesTabHandle)
				.click().perform();

		PageElement transactionTraceTab = wrapElement(
				Utils.waitUntilVisible(ui.getDriver(),
						By.className("tab-content")), ui);
		PageElement transactionTraceViewer = wrapElement(
				transactionTraceTab
						.findElement(By
								.xpath("//*[contains(@id, 'transaction-trace-viewer')]")),
				ui);
		Utils.waitUntilVisible(ui.getDriver(),
				By.xpath("//*[contains(@id, 'transaction-trace-viewer')]"));
		PageElement gridTransactionTraces = transactionTraceViewer
				.findElement(By
						.xpath("//*[contains(@id,'gridTransactionTraces')]"));
		WebElement table = getTable(gridTransactionTraces);
		
		//TODO fix for the new ttviewer
		WebElement oneDurationBar = table.findElement(By
				.cssSelector("div.tt-instance-bar"));
		new Actions(ui.getDriver()).moveToElement(oneDurationBar).click()
				.perform();
		Utils.waitForCondition(ui.getDriver(), ExpectedConditions
				.visibilityOfElementLocated(By
						.xpath("//*[contains(@id, 'tt-segment')]")), 120);
		canvas.getCanvas().scrollToTop();
		canvas.clickToUnusedPlaceInCanvas();
		Thread.sleep(30000);
	}

	protected static WebElement getTable(SearchContext gridTransactionTraces) {
		return gridTransactionTraces
				.findElement(By
						.cssSelector("div.ui-grid-contents-wrapper div[role=grid] div.ui-grid-viewport[role=rowgroup]"
								+ " div.ui-grid-canvas"));
	}
    
}
