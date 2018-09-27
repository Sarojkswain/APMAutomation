package com.ca.apm.saas.pagefactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;

import com.ca.apm.saas.test.utils.JavaAgentBomUtil;
import com.ca.apm.tests.common.file.ZipUtils;
//import java.io.IOException;

/*
 * @author Liddy Hsieh, Jyotsna Akula, Abhishek Sinha  
 * 
 */

public class DownloadAgentPage
{
 
    WebDriver driver;
    DownloadAgentPage objDownloadAgent;
    LoginPage objLogin;
    AgentsPage objAgents;
    
    String url="https://989489.apm.beta.ca.com/ApmServer/"; //--- should be your instance name and login credentials
    int waitSeconds = 3000;
    int delay = 10;
    public static String downloadPath = System.getProperty("downloads.path", "C:\\Users\\Administrator\\Downloads");
    private static final String AGENT_DEPLOY_ROOT = "C:\\automation\\deployed\\agents\\";
    private String agentDestDirectory = AGENT_DEPLOY_ROOT;
    private static final Logger logger = LoggerFactory.getLogger(DownloadAgentPage.class);

    @FindBy(id="settings-view-link")
    WebElement settingsViewLink; 
    
    @FindBy(xpath = ("//*[@id='settings-agents-link']"))
    WebElement agentsTab;
    
    @FindBy(id = "mapview")
    WebElement mapView;
        
    @FindBy(id="settings-download-agent-button")
    WebElement agentButton;

    @FindBy(xpath=("//label[contains(text(),'Unix')]"))
    WebElement osUnix; 

    @FindBy(xpath=("//label[contains(text(),'Windows')]"))
    WebElement osWin; 
    
    @FindBy(xpath = ("//*[@id='widget-download-agent-button-text']"))
    WebElement downloadAgentButton;
    
    @FindBy(xpath=("//div[@class='drop-title'][contains(text(),'Marketplace')]"))
    WebElement marketplacePage;
    
    @FindBy(linkText="Start exploring")
    WebElement startExploringLink;    
    
    @FindBy(id="close-agent-download")
    WebElement closeAgentDownload;
    
    @FindBy(xpath=("//span[contains(text(), 'Start Transaction Tracing')]"))
    WebElement ttStart;
    
    @FindBy(id="agent-selected")
    WebElement dropDownAgentLink;

    @FindBy(xpath=("//div[@class='agent-page-dropdown dropdown']"))
    WebElement selectYourAgent;
    
    @FindBy(id="agent-option-glassfish")
    WebElement agentGlassFish;
    
    @FindBy(id="agent-option-java")
    WebElement agentJava;
    
    @FindBy(id="agent-option-jboss")
    WebElement agentJBoss;
    
    @FindBy(xpath = "//label[text()='Tomcat']")
    WebElement agentTomcat;// label[text()='Tomcat']

    // Below 3 Elements are added since the new pop-up is coming when user
    // wants to download Agent package,
    @FindBy(xpath = "//*[@class='modal-footer agent-download-wizard-actions ng-scope']/span[1]")
    WebElement nextButton;

    @FindBy(xpath = "//*[@class='agent-page-download-btn']")
    WebElement downloadAgntButton;

    @FindBy(xpath = "//*[@class='modal-footer agent-download-wizard-actions ng-scope']/button[1]")
    WebElement downloadAgntClose;
    
    @FindBy(id="agent-option-weblogic")
    WebElement agentWebLogic;
    
    @FindBy(id="agent-option-websphere")
    WebElement agentWebSphere;

    @FindBy(id="agent-option-node.js")
    WebElement agentNodeJs;    
    
    @FindBy(id="package-selected")
    WebElement selectYourAgentSubType;

    @FindBy(id="package-option-struts")
    WebElement scopeStruts;

    /*@FindBy(xpath=("//a[@title='Spring']"))
    WebElement scopeSpring;*/
    
    @FindBy(id="package-option-spring")
    WebElement scopeSpring;
    
    /*@FindBy(linkText=("Java"))
    WebElement scopeJava;*/
    
    @FindBy(id="package-option-java")
    WebElement scopeJava;

    /*@FindBy(xpath=("//a[@class='agent-page-download-btn']"))
    WebElement agentDownloadButton;*/
    
    @FindBy(id="download-agent-button")
    WebElement agentDownloadButton;
    
    @FindBy(xpath=("//*[contains(@class, 'ui-grid-row ng-scope')]//div[starts-with(text(),'Tomcat Agent')]"))
    WebElement connectedTomcatAgent;
    
    @FindBy(xpath=("//div[contains(@class, 'ui-grid-cell-contents ng-binding ng-scope') and @title='Connected']"))
    WebElement connectedString;
  
    @FindBy(xpath=("//span[@class='ng-scope'][contains(text(),'Download installation package')]"))
    WebElement downloaInstallationPackagedButton;
    
    @FindBy(xpath=("//span[@class='ng-scope'][contains(text(),'Agent Download')]"))
    WebElement agentDownloadHeader;

    /*@FindBy(linkText=("Visit CA Marketplace"))
    WebElement visitCAMarketplaceLink;*/
    
    @FindBy(id="market-place-button")
    WebElement visitCAMarketplaceLink;

    @FindBy(xpath=("//*[contains(@class, 'ui-grid-row ng-scope')]"))
    WebElement newAgentTable;

    @FindBy(xpath=("//*[contains(@class, 'ui-grid-row ng-scope')]//div[starts-with(text(),'Tomcat Agent')]"))
    WebElement newTomcatAgent;
    
    @FindBy(xpath=("//div[contains(@class, 'ui-grid-cell-contents ng-binding ng-scope') and @title='Connected']"))
    WebElement newConnected;

    @FindBy(id="agent-option-.net")
    WebElement dotnet;  
    
    //infrastructure agent elements    
    @FindBy(id="agent-option-infrastructure agent")
    WebElement infrastructureAgent;  
    
    @FindBy(id="package-option-host monitoring")
    WebElement scopeHostMonitoring;
    
    @FindBy(id="package-option-docker monitoring")
    WebElement scopeDockerMonitoring;
    
    @FindBy(id="package-option-docker & host monitoring")
    WebElement scopeDockerAndHostMonitoring;
    
    @FindBy(id="package-option-openshift monitoring")
    WebElement scopeOpenshiftMonitoring;
    
    @FindBy(xpath="//span[@class='ng-scope'][contains(text(),'Trace All Agents')]")
    WebElement traceAllAgentsButton;

    @FindBy(xpath="//span[@class='ng-scope'][contains(text(),'Start')]")
    WebElement startButton;

    @FindBy(xpath="//span[@class='ng-scope'][contains(text(),'Close')]")
    WebElement closeButton;
        
    
    public DownloadAgentPage(WebDriver driver){
        this.driver = driver;
        // This initElements method will create all WebElements
        PageFactory.initElements(driver, this);
    }
    
    public boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }    

    public boolean isElementPresent(WebDriver webdriver, WebElement webelement) {    
        boolean exists = false;

        webdriver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);

        try {
            webelement.getTagName();
            exists = true;
        } catch (NoSuchElementException e) {
            // nothing to do.
        }

        webdriver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);

        return exists;
    }

    public void verifyOpenedTabTitle(WebElement linkToOpenWebElement, String newPageTitle) throws InterruptedException {
        //Get Current Page 
        String currentPageHandle = driver.getWindowHandle();                
        
        logger.info("Verifying the WebElement link - {} - to Open New tab exist and clicked ...", linkToOpenWebElement.getText());
        try {
            logger.info("Checking if 'Visit CA MarketplaceLink' link exist or not ... if it exists, click it, otherwise wait for 10 seconds and then skip the step ...");
            Assert.assertTrue(isElementPresent(driver, linkToOpenWebElement), "WebElement Link you provided: " + linkToOpenWebElement + " does NOT exist");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(linkToOpenWebElement)).click();
            logger.info("Successfully Clicked the link!!");
        } catch (Exception e) {
            logger.info("Link provided doesn't appear. ... ignoring" );
        }
       
        //Add Logic to Wait till Page Load
        Thread.sleep(15000);

        // Get all Open Tabs
        ArrayList<String> tabHandles = new ArrayList<String>(driver.getWindowHandles());

        String pageTitle = newPageTitle;
        boolean myNewTabFound = false;

        for(String eachHandle : tabHandles)
        {
            driver.switchTo().window(eachHandle);
            // Check Your Page Title 
            if(driver.getTitle().equalsIgnoreCase(pageTitle))
            {
                // Report ur new tab is found with appropriate title
                logger.info("Expected page title in New tab is -{} - and Actual page title of new tab is - {} - ", pageTitle, driver.getTitle());
                //Close the current tab
                driver.close(); // Note driver.quit() will close all tabs
                Thread.sleep(2000);
                //Switch focus to Old tab
                driver.switchTo().window(currentPageHandle);
                myNewTabFound = true;           
            }
        }

        if(!myNewTabFound)
        {
            // Report page not opened as expected
            logger.info("New tab with title " + pageTitle + " is NOT opened as expected.");
        }

    }

    // Get Web Elements 
    public WebElement getWebElementSettingsViewLink() {
        return settingsViewLink;
    }
    
    public WebElement getWebElementDownloaInstallationPackagedButton() {
        return downloaInstallationPackagedButton;
    }
    
    public WebElement getWebElementAgentDownloadHeader() {
        return agentDownloadHeader;
    }
    
    public WebElement getWebElementVisitCAMarketplaceLink () {
        return visitCAMarketplaceLink;
    }
    
    public WebElement getWebElementVisitCAMarketplacePage () {
        return marketplacePage;
    }
    
    public WebElement getWebElementStartExploringLink () {
        return startExploringLink;
    }
    
    public WebElement getWebElementDownloadAgentLink() {
        return downloadAgentButton;
    }
    
    public WebElement getNewAgentTable() 
    {
        return newAgentTable;
    }
    
    public WebElement getNewTomcatAgent()
    {
        return newTomcatAgent;
    }
    
    public WebElement getNewConnected()
    {
        return newConnected;
    }
    
    public WebElement getNewTomcatAgentElement()
    {
        WebElement tableElement = getNewAgentTable();
        WebElement tomcatAgentCell = tableElement.findElement(By.xpath("//*[contains(@class, 'ui-grid-row ng-scope')]//div[starts-with(text(),'Tomcat Agent')]"));
        return tomcatAgentCell;
    }
    
    // to be deleted later
    public WebElement getUnnamedAgentElement()
    {
        //WebElement tableElement = getNewAgentTable();
        //WebElement unNamedAgentCell = tableElement.findElement(By.xpath("//*[@class, 'ui-grid-cell-contents ng-binding ng-scope']//*[text()='UnnamedAgent']"));
    	WebElement unNamedAgentCell = driver.findElement(By.xpath("//*[contains(@class, 'ui-grid-row ng-scope')]//div[starts-with(text(),'UnnamedAgent')]"));
	return unNamedAgentCell;									
    }
    
    public WebElement getNewConnectedElement() {
        WebElement tableElement = getNewAgentTable();
        WebElement connectCell = tableElement.findElement(By.xpath("//div[contains(@class, 'ui-grid-cell-contents ng-binding ng-scope') and @title='Connected']"));
        return connectCell;
    }
    
    
    public boolean isUnix() {
        if (osUnix.isSelected())
            logger.info("Unix is selected");
        return true;
    }
    
    public void setOS(String OS) throws InterruptedException {

    	//TODO return boolean	
        
        logger.info("Set OS to [" + OS + "]");
        OS = OS.toLowerCase();
    	if (OS.contains("windows")) {
            osWin.click();
            Thread.sleep(waitSeconds);
            return;
        }
        if (OS.contains("unix")) {
            osUnix.click();
            Thread.sleep(waitSeconds);
            return;
        }
        logger.info("Unsupported OS [" + OS + "]");
   }
  
    
	public void setAgentType(String agentType) throws InterruptedException {
	    
		//TODO return boolean	
		 logger.info("Set agent type to [" + agentType + "]");
		Thread.sleep(waitSeconds);
		agentType = agentType.toLowerCase();
		 switch(agentType){
		    case "glassfish":
		    	agentGlassFish.click();
		    	break;
		    case "jboss":
		    	agentJBoss.click();
		    	break;
		    case "tomcat":
		    	agentTomcat.click();
		    	break;
		    case "weblogic":
		    	agentWebLogic.click();
		    	break;
		    case "websphere":
		    	agentWebSphere.click();
		    	break;
		    case "java":
		    	agentJava.click();
		    	break;
		    case "infrastructure-agent":
		        infrastructureAgent.click();
                break;	
		    case "dotnet":
                dotnet.click();
                break;
		    case "nodejs":
		        agentNodeJs.click();
                break;  
		    default:
		    	logger.info("Unsupported Agent type [" + agentType + "]");
		    	break;
		 }
		Thread.sleep(waitSeconds);
	 }
    
	public void setAgentSubType(String agentSubType) throws InterruptedException {
	        
		//TODO return boolean	
		logger.info("Set agent sub-type to [" + agentSubType + "]");
			agentSubType = agentSubType.toLowerCase();
			//Thread.sleep(waitSeconds);
			// click agent drop-down list
	        selectYourAgentSubType.click();
		     switch(agentSubType){
		     	case "spring":
		        	scopeSpring.click();
		        	break;
		        case "java":
		        	scopeJava.click();
		        	break;
		        case "host-monitoring":
		            scopeHostMonitoring.click();
                    break;		
		        case "docker-host-monitoring":
		            scopeDockerAndHostMonitoring.click();
                    break;
		        case "docker-monitoring":
		            scopeDockerMonitoring.click();
                    break;
		        case "openshift-monitoring":
		            scopeOpenshiftMonitoring.click();
                    break;   
		        default:
		        	logger.info("Unsupported agent sub-type [" + agentSubType + "]");
		        	break;
		     }
		     Thread.sleep(waitSeconds); 
	}
    
    public void download(String Agent) throws InterruptedException{
        try {
            // click agent drop-down list
            Thread.sleep(5000);
            dropDownAgentLink.click();
            
            // select agent to download
            if (Agent.equals("GlassFish")) {
                agentGlassFish.click();
                selectAllScopeAndDownload();
            }
            if (Agent.equals("JBoss")) {
                agentJBoss.click();
                selectAllScopeAndDownload();
            }
            if (Agent.equals("Tomcat")) {
                agentTomcat.click();
                selectAllScopeAndDownload();
            }
             if (Agent.equals("WebLogic")) {
                agentWebLogic.click();
                selectAllScopeAndDownload();
            }
            if (Agent.equals("WebSphere")) {
                agentWebSphere.click();
                selectAllScopeAndDownload();
            }            
            if (Agent.equals("Java")) {
                agentJava.click();
                Thread.sleep(waitSeconds);
                agentDownloadButton.click();
                Thread.sleep(waitSeconds);
            }
        }
        catch(Exception e) {
            logger.info("Agent download failed for ---- "+ Agent );
            e.printStackTrace();
        }
    }
    
    public void selectAllScopeAndDownload() {
        try{
  
            // click scope drop-down list,select Spring and click download button  
            Thread.sleep(waitSeconds);
            selectYourAgentSubType.click();
            Thread.sleep(waitSeconds);
            scopeSpring.click();
            Thread.sleep(waitSeconds);
            agentDownloadButton.click();
            Thread.sleep(waitSeconds);
            
            // click scope drop-down list,select Java and click download button  
            Thread.sleep(waitSeconds);
            selectYourAgentSubType.click();
            Thread.sleep(waitSeconds);
            scopeJava.click();
            Thread.sleep(waitSeconds);
            agentDownloadButton.click();
            Thread.sleep(waitSeconds);
            }
        catch(Exception e){
            logger.info("Agent download failed");
            e.printStackTrace();
        }
    }
    
    public void clickSettings () throws InterruptedException {
        //new WebDriverWait(driver, waitSeconds).until(ExpectedConditions.visibilityOf(settingsViewLink));y
        Assert.assertTrue(isElementPresent(By.id("settings-view-link"))); 
        //Thread.sleep(waitSeconds);
        settingsViewLink.click();
    }
    
    public void clickAgentsTab() throws InterruptedException {
        
        logger.info("Clicking on the agent tab...");
        Thread.sleep(5000);
        try {
            agentsTab.click();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Clicked on the agent tab...");
        Thread.sleep(waitSeconds);
    }
    
    public boolean sortAgentsByHost() throws InterruptedException {

        String xpathExpression = "//span[contains(text(),'Host')]";
        for (int k = 0;    k < 2 ; k++ ){
        	try{   
	        	logger.info("Clicking Host to sort agents by Host - sleeping for {} sec. Attempt[{}/2] ", (waitSeconds * 5)/1000, k+1);
	        	driver.findElement(By.xpath(xpathExpression)).click();
	        	logger.info("Clicked Host to sort agents by Host. Attempt [{}/2] ",k+1);
	        	return true;
	        }catch(NoSuchElementException nse){
	            logger.info("Unable to find Host column header in the Agents page to sort. Attempt [{}/2] ",k+1);
	            nse.printStackTrace();
        		Thread.sleep(waitSeconds*5);
	        }
        }
		return false;
    }
   
    public boolean clickDownloadAgentButton() throws InterruptedException {   
        try {
            logger.info("Clicking 'Download Agent' ...");
            new WebDriverWait(driver, delay).until(ExpectedConditions.elementToBeClickable(downloadAgentButton)).click();
            logger.info("Successfully clicked 'Download Agent'");
            Thread.sleep(waitSeconds);
            return true;
        } catch (Exception e) {
            logger.info("'Download Agent' button not showing up - " + e.getMessage() );
            e.printStackTrace();
        }
        return false;
        
    }
    
    public boolean gotoAgentDownloadPage() throws InterruptedException {

        logger.info("Opening download page...");
        clickAgentsTab();
        return clickDownloadAgentButton();
     }
    
    public void clickDownloadAgent() throws InterruptedException {
        downloadAgentButton.click();   
        Thread.sleep(waitSeconds);
    }
    
    public void closeDownloadAgent() throws InterruptedException {
        
        logger.info("Closing download page...");
        closeAgentDownload.click();
        Thread.sleep(waitSeconds);
        //Assert.assertTrue(,"Agent download page didn't close successfully");
    }
        
    public void startTraceAllAgents() throws InterruptedException {
        try {
            logger.info("Clicking'Trace All Agents' button ...");
            traceAllAgentsButton.click();
            Thread.sleep(waitSeconds);
            
            logger.info("Starting 'Trace All Agents' ...");
            startButton.click();
            Thread.sleep(waitSeconds);
            
            logger.info("Closing 'Trace All Agents' ...");
            closeButton.click();
            Thread.sleep(waitSeconds);
        } catch (NoSuchElementException e) {
            logger.warn("'Trace All Agent/Start/Stop buttons is missing. Error: " + e.getMessage());
        }
        
    }   
  
    public void verifyDownloadWithFileNameTags(String Agent) throws InterruptedException {
        Thread.sleep(waitSeconds);
        logger.info("DownloadAgentPage.verifyDownloadWithFileNameTags Verifying " + Agent + " downloaded ...");
        if(Agent.equals("Java")){
        	Assert.assertTrue(isFileDownloaded(downloadPath,Agent,"zip"), "Failed to download " + Agent + " agent file");
        }
        else{
        	// Note: isFileDownloaded method can handle more tags also if needed (for example, a specific version "v3"). Just add to the end of arguments.
        	 Assert.assertTrue(isFileDownloaded(downloadPath,Agent,"Spring","zip"), "Failed to download " + Agent + "  Spring agent file");
             Assert.assertTrue(isFileDownloaded(downloadPath,Agent,"Java","zip"), "Failed to download " + Agent + " Java agent file");
        }       
    }
    
    public void unzipAgentAndVerify(String... agentTags) throws InterruptedException{
    	Thread.sleep(waitSeconds);
    	// Get the exact file path
        String agentFilePath = getFilePath(downloadPath,agentTags);
        if(agentFilePath==null){
        	logger.info("Unable to locate agent per the input tags in the downloads folder");
        	throw new InterruptedException();
        }
        agentDestDirectory = AGENT_DEPLOY_ROOT;
        for (int i=0; i < agentTags.length;i++){
        	agentDestDirectory = agentDestDirectory + agentTags[i];
        }	
       
      // Unzip the agent zip file
		try {
            ZipUtils.unzip(agentFilePath, agentDestDirectory);
            logger.info(agentFilePath + " unzipped at "+ agentDestDirectory);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
		//Delete the downloaded zip
		/*logger.info ("******************************************About to delete downloaded agent zip " + agentFilePath);
		File agentFile = new File(agentFilePath);
		if (agentFile.delete())	        
				logger.info ("******************************************Successfully deleted agent zip " + agentFilePath);*/
		
		//Verify the contents of unzipped folder
        String agentJarpath = getFilePath(agentDestDirectory+"\\wily","Agent","jar");
        Assert.assertTrue((agentJarpath!=null),"Agent contents (Agent.jar) missing in unzipped file.");
        logger.info("Unzipped Agent.jar file verified at: "+ agentJarpath);
        Thread.sleep(5000);
    }
    
    // Returns the file path identified per the tags or null if none is identified.
    public String getFilePath(String folderPath, String... nameTags){

        File dir = new File(folderPath);
    	Assert.assertTrue(dir.exists(), "Directory " + dir + " doesn't exist");
    	
        File[] dir_contents = dir.listFiles();
        String currFileName;
        boolean standAloneAgentFlag = false;
        
        // Check for the standalone Java agent.
        if (isPresentInArray("GlassFish", nameTags) || isPresentInArray("JBoss", nameTags)
            || isPresentInArray("Tomcat", nameTags) || isPresentInArray("WebLogic", nameTags)
            || isPresentInArray("WebSphere", nameTags)) {
            standAloneAgentFlag = true;
        }

        for (int i = 0; i < dir_contents.length; i++) {
            currFileName = dir_contents[i].getName();
            // Changed the logic below to check if Agent's name matches with
            // either one of them, we no longer have to check subagent type etc
            if (currFileName.contains("GlassFish") || currFileName.contains("JBoss")
                || currFileName.contains("Tomcat") || currFileName.contains("WebLogic")
                || currFileName.contains("WebSphere")) {
                logger.info("GetFilePath: " + folderPath + "/" + currFileName);
                return folderPath + "/" + currFileName;
            }
        }
        
       return null;
    }
    
    private <T> boolean  isPresentInArray(T elementUnderInvestigation,T[] arr){
    	for(T curr:arr){
    		if (  (curr != null)  &&  (curr.equals(elementUnderInvestigation))  ){
    			return true;
   			
    		}
    	}
    	return false;
    }
    
   /* public void verifyDownloadWithFileExtension(String ext) throws InterruptedException {
        Thread.sleep(waitSeconds);
        if (ext.equalsIgnoreCase("zip")){
            Assert.assertTrue(isFileDownloaded_Ext(downloadPath, "zip"), "Failed to download Agent file which has extension .zip");
        }
        if (ext.equalsIgnoreCase("tar")){
            Assert.assertTrue(isFileDownloaded_Ext(downloadPath, "tar"), "Failed to download Agent file which has extension .tar");
        }
    }*/
    
    public void verifyExpectedFileName(String Agent) throws InterruptedException {
        Thread.sleep(waitSeconds);
        File getLatestFile = getLatestFilefromDir(downloadPath);
        Thread.sleep(waitSeconds);
        String fileName = getLatestFile.getName();
        logger.info("The latest file name is: " + fileName);
    }
    
    /*private boolean isFileDownloaded_Ext(String dirPath, String ext)
    {
        boolean flag = false;
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            flag = false;
        }
        
        for (int i = 1; i < files.length; i++) {
            if (files[i].getName().contains(ext)) {
                flag = true;
            }
        }
        return flag;
    }*/

    // Get the latest file from a specific directory 
    private File getLatestFilefromDir(String dirPath) throws InterruptedException {
        Thread.sleep(waitSeconds);
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        
        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
                logger.info("getLatestFilefromDir " + lastModifiedFile);
            }
        }
        return lastModifiedFile;
    }
    
    private boolean isFileDownloaded(String downloadPath, String... fileNameTags)
    {        
    	File dir = new File(downloadPath);
        File[] dir_contents = dir.listFiles();
        String currFileName;
        boolean standAloneAgentFlag = false;
        
      //Check for standalone Java agent.
        if(isPresentInArray("Java",fileNameTags) 
        		&& !isPresentInArray("GlassFish",fileNameTags)
        		&& !isPresentInArray("JBoss",fileNameTags)
        		&& !isPresentInArray("Tomcat",fileNameTags)
        		&& !isPresentInArray("WebLogic",fileNameTags)
        		&& !isPresentInArray("WebSphere",fileNameTags)
        		){
        	standAloneAgentFlag = true;
        }
        
        newFile:
        for (int i = 0; i < dir_contents.length; i++) {
        	currFileName = dir_contents[i].getName();
        	
        	//Check for the standalone Java agent. This step is needed since Standalone Java's file name is generally a subset of other appserver's Java agent filename.
        	if(standAloneAgentFlag){
        			if(currFileName.contains("Java") 
	        			&& !currFileName.contains("GlassFish")
	        			&& !currFileName.contains("JBoss") 
	        			&& !currFileName.contains("Tomcat") 
	        			&& !currFileName.contains("WebLogic") 
	        			&& !currFileName.contains("WebSphere")){
        				logger.info("Download verification successful: "+ currFileName);
       					return true;
        			}
        	}
        	else{
        		for(int k=0;k<fileNameTags.length;k++){
            		if (!dir_contents[i].getName().contains(fileNameTags[k])){
            			continue newFile;        			
            		}        		
            	}
            	logger.info("Download verification successful: "+ currFileName);
            	return true;
        	}
        }
        return false;
    }    
    
    @AfterMethod
    @AfterTest
    public void tearDown() throws Exception {
    	driver.quit();
    }
    
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    
    public boolean downloadAgentInstruction(String agentType, String agentSubType) throws InterruptedException {
  
        Thread.sleep(5000);
        setAgentType(agentType);
        
        if (!containsIgnoreCase(agentType,"Java")) {
            setAgentSubType(agentSubType);
        }
        return true; 
    }
    
    public String downloadAgentPackage(String os, String agentType, String agentSubType) throws InterruptedException {
  
        int i = 0;

        // go to agent download page
        if (!gotoAgentDownloadPage())
            return null;
		setOS(os);
		
		//Check for same agent packages from previous downloads. If present, delete all.
		String packageFromLastDownload = getFilePath(downloadPath,agentType,agentSubType);
		while(packageFromLastDownload!=null){
			new File(packageFromLastDownload).delete();
			logger.info("Detected and deleted an old package: "+packageFromLastDownload);
			packageFromLastDownload = getFilePath(downloadPath,agentType,agentSubType);
		}
	
    	setAgentType(agentType);

        while (i < 5) {
            if (nextButton.isDisplayed()) {
                i = 5;
                nextButton.click();
            } else {
                i++;
                Thread.sleep(1000);
            }
        }

        i = 0;
        while (i < 5) {
            if (downloadAgntButton.isDisplayed()) {
                i = 5;
                downloadAgntButton.click();
                logger.info("Download clicked");
            } else {
                i++;
                Thread.sleep(1000);
            }
        }
        

        // Wait for 30 seconds till Package is downloaded,
        Thread.sleep(30000);
    	
        // Close the Agent download pop-up once Download is started
        downloadAgntClose.click();
    	
    	String filePath = waitForAgentDownload(os, agentType, agentSubType);
    	return filePath;
	}
 
    /**
     * Download Agent
     * @param os
     * @param packagePrefix
     * @param agentType
     * @param subType
     * @return package path
     * @throws Exception
     */
    public String downloadAgentPackage(String os, String packagePrefix, String agentType, String subType) throws Exception {
        
        logger.info("Download started for type {}, subType {} and package {}...", agentType, subType, packagePrefix);
        
        //delete old one  
        String extension = ".zip";
        if(os.equalsIgnoreCase("Unix")) {
            extension = ".tar";
        }
       
        String file = getFileNamebyPrefix(downloadPath, packagePrefix, extension);
        if(file != null) {
            logger.info("File '{}' already exists..deleting it to download the latest one...", file);
            new File(file).delete();
        }     

        //download file      
        if (!gotoAgentDownloadPage())
            return null;
        
        setOS(os);
        setAgentType(agentType);
        if(subType != null) {
            setAgentSubType(subType);
        }
        agentDownloadButton.click();
        
        //wait for file to be downloaded
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 360000) {            
            file = getFileNamebyPrefix(downloadPath, packagePrefix, extension);
            if(file != null) {
            	logger.info("Found the downloaded file {} with matching criteria", file );
                return file;
            }
            logger.info("Waiting 20 sec for download to be completed...");
            Thread.sleep(20000);
        }
        
        logger.warn("Download didn't finish on time for package: " + packagePrefix);
        return null;
    }
    
    private String getFileNamebyPrefix(String dir, String prefix, String extension) throws Exception {
        
        for (File file : new File(dir).listFiles()) {
            logger.info("Found file: " + file.getCanonicalPath());
            if(file.getName().startsWith(prefix) && file.getName().endsWith(extension)) {
                return file.getCanonicalPath();
            }                
        }
        
        logger.info("Unable to find file in dir '{}' with prefix '{}' & extension '{}'", 
            dir, prefix, extension);        
        return null;
    }
    
    public String waitForAgentDownload(String os, String agentType, String agentSubType) throws InterruptedException{
 
    	Boolean bExt = false;
    	int i = 0;
    	String osExt = "zip";//default assigned for windows. reassigned  later
    	String filePath = null;
    	String fileExt = null;
    	File f;

    	if (os.equalsIgnoreCase("Unix")){	 		
			//TO DO: change for unix
			downloadPath = "/root/Downloads";
			osExt = "tar";
		}
    	
    	while  ( !bExt && (i <= 9)  ) {

    		i++;
    		Thread.sleep(10000);
			filePath = getFilePath(downloadPath, agentType, agentSubType);
	     	if (filePath == null) {
	     		logger.info("Cannot find Agent package [" + agentType + agentSubType+  "] in downloads folder. Will retry in 10 seconds. Attempt " + i + "/9");
	     		i++;
	     		Thread.sleep(10000);
	     		continue;
	     	}
		f = new File(filePath);
	     	fileExt = getFileExtension(f);
	    	bExt = fileExt.equalsIgnoreCase(osExt);
	    	logger.info("downloaded file [" + filePath + "] with extension[" + fileExt + "]");
			if (!bExt) {
				logger.warn("Unable to verify downloaded Agent package in " + downloadPath + ". Retrying in 10 seconds. Attempt " + i + "/9");
			}
    	}
	
    	return filePath;
	}
        
    public String unzipPackage(String filePath, String destFolder){
    	
    	// Check for existing wily folder at destination, delete if present.
		File currFolder = new File(destFolder);
		Assert.assertTrue(currFolder.exists(), "Directory " + currFolder + " doesn't exist.");
		
		File[] contents = currFolder.listFiles();
		boolean isWilyPresent = false;
		for(File temp:contents){
			if(temp.getName().equalsIgnoreCase("wily") && temp.isDirectory()){
				isWilyPresent = true;
				break;
			}
		}
		if(isWilyPresent){
			boolean success = recursivelyDeleteFolder(new File(destFolder + "/wily"));
			if(success)logger.info("Old wily folder deleted.");
			else logger.info("Unable to delete old wily folder");
		}
  		
      
    	try {
            ZipUtils.unzip(filePath, destFolder);
            
        }catch (Exception ex) {
            ex.printStackTrace();
            logger.info("Unable to unzip agent package " + filePath + " to " + destFolder);
        }
    	String wilyPath = destFolder + "/wily";
		File f = new File(wilyPath);
		Boolean b = f.exists();
		if(b)
			return wilyPath;
		else {
			return null;
		}
	}
    
    public boolean recursivelyDeleteFolder(File f){
    	boolean success = true;
    	File[] f_contents = f.listFiles();
    	for(File temp:f_contents){
    		if(temp.isDirectory())recursivelyDeleteFolder(temp);
    		else success = success && temp.delete();
    	}
    	success = success && f.delete();
    	return success;
    }
 
    public void verifyAgentBundles(String agentType, String agentSubType, 
                                   String wilyFolder) throws Exception {
        
        logger.info("Verifying Agent Bundles for type '{}' and subtype '{}'...", agentType, agentSubType);
        
        //validate common files
        for(String file: JavaAgentBomUtil.COMMON_FILES) {
            logger.info("Validating " + file);
            Assert.assertTrue(new File(wilyFolder + "/" + file).exists(), 
                "Expected file '" + file + "' doesn't exist.");
        }
        
        //validate webappsupport
        if(!agentType.equalsIgnoreCase("java")) {
            String ext = JavaAgentBomUtil.WEBAPPSUPPORT_CORE_EXT;
            logger.info("Validating " + ext);
            Assert.assertTrue(new File(wilyFolder + "/" + ext).exists(), 
                "Expected extension '" + ext + "' doesn't exist.");
        }
        
        //validate agent jar
        String agentJar = JavaAgentBomUtil.AGENT_JAR;
        if(agentType.equalsIgnoreCase("websphere")) {
            agentJar = JavaAgentBomUtil.NOREDEF_NORETRANS_JAR;
        }
        logger.info("Validating " + agentJar);
        Assert.assertTrue(new File(wilyFolder + "/" + agentJar).exists(), 
            "Expected jar '" + agentJar + "' doesn't exist.");
        
        //validate dynamic extensions
        logger.info("Validating dynamic extensions...");
        
        List<String> expectedExt = JavaAgentBomUtil.JAVA_PACKAGE_EXTENSIONS;
        if(agentSubType != null && agentSubType.equalsIgnoreCase("spring")) {            
            expectedExt = JavaAgentBomUtil.SPRING_PACKAGE_EXTENSIONS;
        }
        for(String ext: expectedExt) {
            Assert.assertTrue(JavaAgentBomUtil.isDynamicExtExist(wilyFolder, ext), 
                "Dynamic extension '" + ext + "' doesn't exist.");
        }
    }    
	
	//TODO Update Search logic from starting with agent name to full search. especially standalone vs app server match.
	//TODO loop through all rows and have a map - loop through the map to see connected value. b
	/**
	 * This method assumes that the browser has the agent's status page loaded. 
	 * 
	 * @param agentType
	 * @param subPackageType
	 * @param appName
	 * @param loadMachineHostname
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public boolean singleAgentConnectionStatus(String agentName,
	                                           String agentType, 
	                                           String subPackageType, 
	                                           String appName, 
	                                           String loadMachineHostname)throws InterruptedException,IOException {
		
	    logger.info("Checking AgentConnectionStatus on UI for host " + loadMachineHostname);
          
	    /*
	    List<WebElement> listofRows = driver.findElements(By.xpath("//div[contains(@class, 'ui-grid-row ng-scope')]"));
		if(listofRows.size()==0){
        	logger.info("No agent listed in the Agent view.");
        	return false;
        }
		else {
		    logger.info("********  AGENTS ********");
		    for (WebElement element: listofRows) {
		        logger.info("element text = {}, element tagName = {}", element.getText(), element.getTagName());
		    }
		    logger.info("*************************");
		}
		*/

		if(agentName == null || agentName.isEmpty()) {		    
		    agentName = agentType + subPackageType + "_" + appName;
    		if (agentType.contains("Java")) {
    			agentName = agentType + "_" + appName;
    		}    		
		}
		
        logger.info("Checking Connected status of '{}' on '{}'", agentName, loadMachineHostname);
        try {
            String xpath = "//tr[(td/div[contains(text(),'" + agentName + "')]) and (td/div[contains(text(),'" + loadMachineHostname + "')]) and (td/div[contains(text(),'Connected')])]";
            boolean exists = driver.findElements(By.xpath(xpath)).size() !=0;
        
            if (exists) {
                logger.info(agentName+" on "+loadMachineHostname+" is listed as Connected.");
                return true;
            }
            else {
                logger.info("Agent '{}' on host '{}' either isn't listed or listed as disconnected.", agentName, loadMachineHostname);
            }    
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
	}
	
   public boolean singleAgentDisconnectionStatus(String agentName,
                                               String agentType, 
                                               String subPackageType, 
                                               String appName, 
                                               String loadMachineHostname)throws InterruptedException,IOException {
        
        logger.info("Checking AgentDisConnectionStatus on UI for host " + loadMachineHostname);
        
        /*
        List<WebElement> listofRows = driver.findElements(By.xpath("//div[contains(@class, 'ui-grid-row ng-scope')]"));
        if(listofRows.size()==0){
            logger.info("No agent listed in the Agent view.");
            return false;
        }

        if(agentName == null || agentName.isEmpty()) {          
            agentName = agentType + subPackageType + "_" + appName;
            if (agentType.contains("Java")) {
                agentName = agentType + "_" + appName;
            }           
        }
        */
        
        logger.info("Checking Disconnected status of {} on {}", agentName, loadMachineHostname);
        try {
            String xpath = "//tr[(td/div[contains(text(),'" + agentName + "')]) and (td/div[contains(text(),'" + loadMachineHostname + "')]) and (td/div[contains(text(),'Disconnected')])]";
            boolean exists = driver.findElements(By.xpath(xpath)).size() !=0;
        
            if (exists) {
                logger.info(agentName+" on "+loadMachineHostname+" is listed as disconnected.");
                return true; 
            }
            else {
                logger.info(agentName+" on "+loadMachineHostname+" is listed as connected.");
            }       
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
   
	public boolean containsIgnoreCase(String mainString, String subString){
    	mainString = mainString.toLowerCase();
    	subString = subString.toLowerCase();
    	if(mainString.contains(subString)) return true;
    	else return false;
    }
	
}