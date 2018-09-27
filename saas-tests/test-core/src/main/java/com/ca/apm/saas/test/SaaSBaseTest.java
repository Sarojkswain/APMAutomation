/*
 * Copyright (c) 2015 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.saas.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.SshUploadFlow;
import com.ca.apm.automation.action.flow.utility.SshUploadFlowContext;
import com.ca.apm.saas.pagefactory.AgentsPage;
import com.ca.apm.saas.pagefactory.AxaPage;
import com.ca.apm.saas.pagefactory.DownloadAgentPage;
import com.ca.apm.saas.pagefactory.HomePage;
import com.ca.apm.saas.pagefactory.LoginPage;
import com.ca.apm.saas.pagefactory.LoginPageOnPrem;
import com.ca.apm.saas.pagefactory.MapPage;
import com.ca.apm.saas.pagefactory.OnBoardingPage;
import com.ca.apm.saas.test.utils.EmailUtil;
import com.ca.apm.saas.test.utils.HttpTxnGen;
import com.ca.apm.saas.test.utils.SaaSUtils;
import com.ca.apm.saas.test.utils.TraceInfo;
import com.ca.apm.saas.test.utils.WebAppTraceDetails;
import com.ca.apm.saas.testbed.SaaSUIKonakartTestbed;
import com.ca.apm.saas.testbed.SaasBaseTestbed;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.testbed.atc.SeleniumGridMachinesFactory;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;

public abstract class SaaSBaseTest extends TasTestNgTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SaaSBaseTest.class);
    private EnvironmentPropertyContext env = null;
    protected UI ui = null;
    private Method methodUnderTest = null;
    protected String registrationURL = "";
    protected String wilyDir = null;
    protected MapPage mapHelper = null;
    protected AgentsPage agentsHelper = null;
    protected HomePage homeHelper = null;
    protected static String instance;
    protected static String loginURL;
    protected static String email;
    protected static String user;
    protected static String password;
    protected static String tenant;
    protected static String testngEmailRecipients;
    protected String product = "APM";
   
    @BeforeSuite(alwaysRun = true)
    public void setupTestSuite(ITestContext context) {

        if(instance == null) {
            instance = SaaSUtils.getPropertyValue("instance", context, "staging");
        }
    	//instance = SaaSUtils.getPropertyValue("instance", context, "production");
    	
        registrationURL = SaaSUtils.getPropertyValue("registrationURL", context, 
            SaaSUtils.LOGIN_MAP.get(instance + "_registrationURL"));      
        loginURL = SaaSUtils.getPropertyValue("loginURL", context, 
            SaaSUtils.LOGIN_MAP.get(instance + "_loginURL"));
        email = SaaSUtils.getPropertyValue("email", context, 
            SaaSUtils.LOGIN_MAP.get(instance + "_email"));
        user = SaaSUtils.getPropertyValue("user", context, 
            SaaSUtils.LOGIN_MAP.get(instance + "_user"));
        password = SaaSUtils.getPropertyValue("password", context, 
            SaaSUtils.LOGIN_MAP.get(instance + "_password"));
        tenant = SaaSUtils.getPropertyValue("tenant", context, 
            SaaSUtils.LOGIN_MAP.get(instance + "_tenant"));
        
    }

    /**
     * Prepare for test
     * 
     * @throws Exception
     */
    @BeforeMethod(alwaysRun = true)
    public void before(Method method) throws Exception {
        
        this.methodUnderTest = method;
        ui = getUIInstance();
    }
    
    protected UI getUIInstance() throws Exception {

        ChromeOptions opt = new ChromeOptions();
        SaaSUtils.fillChromeOptions(opt);
        UI ui = null;

        DesiredCapabilities dc = DesiredCapabilities.chrome();
        dc.setCapability(ChromeOptions.CAPABILITY, opt);

        env = new EnvironmentPropertyContextFactory().createFromSystemProperty();

        String chromeDriverPath =
            System.getProperty("chromeDriverPath",
                env.getTestbedProperties().get("chromeDriverPath"));

        if (chromeDriverPath != null) {
            ui = UI.getLocal(dc, chromeDriverPath);
            return ui;
        }

        try {
            String hostname =
                env.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
            ui = UI.getRemote(dc, String.format("http://%s:4444/wd/hub", hostname));
            
        } catch (IllegalStateException e) {
            // Fallback to URL from config
            logger.info(env.getTestbedProperties().get("selenium.webdriverURL"));
            ui = UI.getRemote(dc, env.getTestbedProperties().get("selenium.webdriverURL"));
        }
        
        return ui;
    }

    public void attemptLogin(RemoteWebDriver driver) throws InterruptedException, IOException {

        // TODO use #logintoInstance call directly once defect is fixed
        int maxAttempts = 2;

        for (int i = 1; i <= maxAttempts; i++) {
            try {
                logger.info(
                    "Attempt #{} to login (will repeat again as a workaround to http 408 error).",i);
                logintoInstance(driver, i);
            } catch (java.lang.AssertionError error) {
                if (!error.getMessage().contains(SaaSUtils.LOGIN_ERROR) || i == maxAttempts) {
                    // throw error if another issue happened or max attempts reached
                    throw error;
                }
                // try to login again (max 3 times)
                continue;
            }
            // exit since login succeeded
            return;
        }
    }
    
    public void logintoInstance(RemoteWebDriver driver, int attemptNbr) 
            throws InterruptedException, IOException {

        // Adding Timeout of 60 seconds since sometimes Login instance takes
        // more than anticipated time
        driver.manage().timeouts().implicitlyWait(60,TimeUnit.SECONDS) ;
        if (containsIgnoreCase(instance,"dev")) {
            extractInstanceInfoFromFile();
        }

        logger
            .info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        logger.info("Login to instance with email [" + email + "]  password [" + password
            + "] tenandId [" + email + "] \nurl [" + loginURL + "]");
        driver.get(loginURL);
        Thread.sleep(10000);

        LoginPage objLogin = new LoginPage(driver);
        OnBoardingPage onBoardingPg = new OnBoardingPage(driver);
        
        if(attemptNbr == 1) {
            objLogin.signInToDemo(email, password, email);
        }        
        
        // Introducing delay of 20 seconds since sometimes after login
        // Home page is taking more than anticipated time for loading
        Thread.sleep(20000);
        
        if(product.equalsIgnoreCase("APM")) {
            Assert.assertTrue(
                              onBoardingPg.isElementPresent(driver, onBoardingPg.getWebElementApmBtn()),
                              "APM button in Onboarding page didn't load properly");

            onBoardingPg.clickApmButton();
        }
        else if(product.equalsIgnoreCase("AXA")) {
            onBoardingPg.clickEumButton();
            logger.info("AXA: Clicked AXA button");
            // close pop-up window if present
            AxaPage objAxa = new AxaPage(driver);
            objAxa.closeAxaPopUp();
            objAxa.closeWalkThroughOK();
            logger.info("AXA: Title after login to AXA is : " + driver.getTitle());
            Assert.assertTrue(driver.getTitle().contains("App Experience Analytics"));
        }
        else {
            //TODO
        }

        // Check for a popup and close it, if present.
        objLogin.closePopUp();
        Assert.assertTrue(objLogin.isElementPresent(By.xpath("//*[@id='user-logout']")),
            SaaSUtils.LOGIN_ERROR);
    }
    
    protected void loginOnPrem(RemoteWebDriver driver, String host) throws Exception {
        
        loginURL = loginURL.replace("{host}", host);
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        logger.info("Login as: {}/{} to {}", user, password, loginURL);
        driver.get(loginURL);
        Thread.sleep(10000);

        LoginPageOnPrem objLogin = new LoginPageOnPrem(driver);
        objLogin.loginToApmServer(user, password);
    }
   
    protected void extractInstanceInfoFromFile() {

        BufferedReader buffReader = null;
        try {
            logger.info("Reading dev email/password from: " + 
                new File(NewRegistrationTest.DEV_INFO_FILE_NAME).getCanonicalPath());
            
            FileReader fileReader = new FileReader(NewRegistrationTest.DEV_INFO_FILE_NAME);
            buffReader = new BufferedReader(fileReader);
            email = buffReader.readLine();
            password = buffReader.readLine();
        } catch (Exception e) {
            logger.warn("Error occurred while reading output file for dev instance: " + e.getMessage());
            logger.info("Using default values for email/password...");
        }
        finally {
            if(buffReader != null) {
                try {
                    buffReader.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    protected boolean startAgent(String OS, String agentType, String agentSubType,
                                 String destWilyFolder, String appServer, String appName) 
                                     throws Exception {
        return startAgent(OS, agentType, agentSubType, destWilyFolder, appServer, appName, null);
    }
    
    protected boolean startAgent(String OS, String agentType, String agentSubType,
                                 String destWilyFolder, String appServer, String appName, 
                                 HashMap<String,String> agentProfileUpdates) throws Exception {

        //set agent properties
        if(agentProfileUpdates != null && agentProfileUpdates.size() != 0) {    
            SaaSUtils.updateProperties(
                destWilyFolder + "/core/config/IntroscopeAgent.profile", agentProfileUpdates);
        }
        
        logger.info("Starting " + appName + " (" + appServer + ") with " + agentType + agentSubType
            + " agent package.  --> " + destWilyFolder);

        if (appServer.equalsIgnoreCase("StandAlone") && appName.equalsIgnoreCase("StressApp")) {
            startSingleStressApp(agentType, agentSubType);
            return true;
        } else if ((appServer.equalsIgnoreCase("Tomcat")) && (containsIgnoreCase(appName,"Thieves"))) {
            return startTomcatwithThieves(destWilyFolder, appName);
        } else if ((appServer.equalsIgnoreCase("JBoss")) && (containsIgnoreCase(appName,"TicketMonster"))) {
            return startTixmonsterWithJBoss(destWilyFolder, appName);
        } else if((appServer.equalsIgnoreCase("Tomcat")) && (containsIgnoreCase(appName,"Konakart"))){
        	return startKonakartOnTomcat(destWilyFolder, appName);
        } else {
            logger.info("Unsupported " + "App " + appName + " " + appServer);
            return false;
        }
    }
    
    protected boolean stopAgent( String OS, String agentType, String agentSubType, String appServer, String appName) 
        throws InterruptedException, IOException {

 
        Assert.assertTrue(wilyDir != null, "Agent home directory wasn't initialized."); 
        logger.info("Stopping " + appName + " (" + appServer + ") with " + agentType + agentSubType + " agent package.  --> " + wilyDir);
        String appServerHome = (new File(wilyDir)).getParent();
        
        if (appServer.equalsIgnoreCase("StandAlone") && appName.equalsIgnoreCase("StressApp")){
            stopStressApp(agentType, agentSubType,appServerHome);
            return true; 
        } 
        else if ( (appServer.equalsIgnoreCase("Tomcat")) && (containsIgnoreCase(appName,"Thieves"))  ){          
            return stopTomcatwithThieves(wilyDir,appName);
        }
        else if((appServer.equalsIgnoreCase("JBoss")) && (containsIgnoreCase(appName,"TicketMonster"))){
            return stopTixmonsterWithJBoss(wilyDir,appName);
        }
        else if((appServer.equalsIgnoreCase("Tomcat")) && (containsIgnoreCase(appName,"Konakart"))){
        	return stopKonakartOnTomcat(wilyDir, appName);
        }
        else{
            logger.info("Unsupported " + "App " + appName + " " + appServer);
            return false;
        }
    }
    
    public boolean startTomcatwithThieves(String destWilyFolder, String appName) {
        return startTomcatwithThieves(SaasUITestbed.MACHINE_ID, destWilyFolder, appName);
    }
    
    public boolean startTomcatwithThieves(String machineId) {
        return startTomcatwithThieves(machineId, wilyDir, "Thieves");
    }
    
    public boolean startTomcatwithThieves(String machineId, 
                                          String destWilyFolder, String appName) {
        
        return runRemoteCommand(
            machineId, "catalina.bat", 
            (new File(destWilyFolder)).getParent() + "/bin", 
            Arrays.asList("start"), 
            "Tomcat with " + appName + " started.", 30000);
    }
    
    public boolean startKonakartOnTomcat(String destWilyFolder, String appName){
    	 return runRemoteCommand(
    			 	SaaSUIKonakartTestbed.KONAKART_MACHINE_ID, "startkonakart.bat", 
    	            (new File(destWilyFolder)).getParent() + "/bin", 
    	            null, 
    	            "Tomcat with " + appName + " started.", 30000);
    }

    public boolean stopTomcatwithThieves(String destWilyFolder,String appName) {
        return stopTomcatwithThieves(SaasUITestbed.MACHINE_ID, destWilyFolder, appName);
    }
    
    public boolean stopTomcatwithThieves(String machineId) {
        return stopTomcatwithThieves(machineId, wilyDir, "Thieves");
    }
    
    public boolean stopTomcatwithThieves(String machineId, 
                                         String destWilyFolder,String appName) {
        
        return runRemoteCommand(
            machineId, "catalina.bat", 
            (new File(destWilyFolder)).getParent() + "/bin", 
            Arrays.asList("stop"), 
            "Tomcat with " + appName + " stopped.", 0);
    }
    
    public boolean stopKonakartOnTomcat(String destWilyFolder,String appName){
    	return runRemoteCommand(
    			SaaSUIKonakartTestbed.KONAKART_MACHINE_ID, "stopkonakart.bat", 
                (new File(destWilyFolder)).getParent() + "/bin", 
                null, 
                "Tomcat with " + appName + " stopped.", 10000);
    }

    public boolean stopTixmonsterWithJBoss(String destWilyFolder, String appName) {
        
        String stopJBossCommand = 
            "wmic process where \"CommandLine like '%standalone.bat%' and not (CommandLine like '%wmic%')\" Call Terminate";
        
        return runRemoteCommand(
            SaasUITestbed.MACHINE_ID, stopJBossCommand, 
            null, null, 
            "JBoss with " + appName + " stopped.", 0);
    }

    // This method will stop all agents and loads running
    private void stopStressApp(String agentType, String subPackageType,
            String appServerHome) throws IOException {
        
        // String agentFolder = ALL_AGENTS_LOC+"\\"+agentType+subPackageType;
        String agentFolder = appServerHome;
        logger.info("Test print: appserverHome = " + appServerHome);
        boolean ifPidFilePresent = false;
        String pid;

        File dir = new File(agentFolder);
        File[] dir_contents = dir.listFiles();
        for (File curr : dir_contents) {
            if (curr.getName().equals("pid.txt")) {
                ifPidFilePresent = true;
                break;
            }
        }

        if (!ifPidFilePresent) {
            System.out
                    .println(agentType
                            + subPackageType
                            + " agent with StressApp doesn't seem to be running (PID file missing). Nothing to stop.");
            // writer.println(agentType+subPackageType+" agent with StressApp doesn't seem to be running (PID file missing). Nothing to stop.");
            return;
        }

        BufferedReader buffReader = new BufferedReader(new FileReader(
                agentFolder + "\\pid.txt"));
        pid = buffReader.readLine();
        buffReader.close();

        RunCommandFlowContext stopAgentFlowContext = new RunCommandFlowContext.Builder(
                "taskkill").args(Arrays.asList("/F", "/PID", pid)).build();
        runCommandFlowByMachineId(SaasUITestbed.MACHINE_ID,
                stopAgentFlowContext);

        RunCommandFlowContext deletePidFileFlowContext = new RunCommandFlowContext.Builder(
                "del").args(Arrays.asList(agentFolder + "\\pid.txt")).build();
        runCommandFlowByMachineId(SaasUITestbed.MACHINE_ID,
                deletePidFileFlowContext);

        logger.info(agentType + subPackageType
                + " agent with StressApp stopped.");
        // writer.println(agentType+subPackageType+" agent with StressApp stopped.");
    }
   
    /**
     * Do some cleanup after test
     */
    @AfterMethod(alwaysRun = true)
    public void after(ITestResult testResult) {
        
        if(ui != null) {
            if (testResult.getStatus() == ITestResult.FAILURE) {
                SaaSUtils.takeScreenshot(ui.getDriver(), "FAILURE", 
                    getClass().getSimpleName(), methodUnderTest.getName());
            }
            
            closeBrowser();
            
            if (this.methodUnderTest != null) {
                this.methodUnderTest = null;
            }
        }
    }

    protected void closeBrowser() {
        
        if (ui != null) {
            ui.cleanup();
            try {
                ui.getDriver().quit();
                ui = null;
            } catch (Exception e) {

            }
        }
    }
    
    protected boolean runRemoteCommand(String machineId, String command, String workDir,
                                       List<String> args) {
    
        return runRemoteCommand(machineId, command, workDir, args,
            "Command " + command + " successfully completed.", 0);
    }
        
    protected boolean runRemoteCommand(String machineId, String command, String workDir,
        List<String> args, String message, long sleep) {

        try {
            RunCommandFlowContext.Builder builder =
                new RunCommandFlowContext.Builder(command);
    
            if (workDir != null && !workDir.isEmpty()) {
                builder.workDir(workDir);
            }
            if (args != null && !args.isEmpty()) {
                builder.args(args);
            }
    
            runCommandFlowByMachineId(machineId, builder.build());                     
            logger.info(message); 
            Thread.sleep(sleep);
            return true;
        } catch (Exception ex) {
            logger.error("Error encountered while running command {}.", command);
            ex.printStackTrace();
            return false;
        }
    }

    protected void validateDemoAgentsConnected(RemoteWebDriver driver, String agentName, String demoHost, String appServer) 
                                                       throws InterruptedException, IOException {
        logger.info( "agentName " + agentName + " demoHost " + demoHost + " appServer " + appServer);

        attemptLogin(driver);
        DownloadAgentPage objDownloadAgent = new DownloadAgentPage(driver);
            
        // check UI if Demo agent is connected
        checkAgentConnectedUI(objDownloadAgent, demoHost, agentName, null, null, appServer);
    }
    
    protected boolean validateAgentsDisconnected(RemoteWebDriver driver, String agentType, String agentSubType,
                                           String destFolder, String appName, String appServer, HashMap<String,String> agentProfileUpdates) 
                                                   throws Exception {

        logger.info("agentType " + agentType + " agentSubType " + agentSubType + " destFolder "
                                               + destFolder + " appServer " + appServer + " appName " + appName);
        //attemptLogin(driver);
        DownloadAgentPage objDownloadAgent = new DownloadAgentPage(driver);

        // check UI if agents is disconnected
        //checkAgentDisconnectedUI(objDownloadAgent, demoHost, agentName, null, null, appServer);
        
        Thread.sleep(15000);
        return checkAgentDisconnectedUI(objDownloadAgent,
                              envProperties.getMachineHostnameByRoleId(SaasUITestbed.LOAD_ROLE_ID), null, agentType,
                              agentSubType, appName);
    }
    
    protected void validateAgentsConnected(RemoteWebDriver driver, String agentType, String agentSubType,
                                           String destFolder, String appName, String appServer, HashMap<String,String> agentProfileUpdates) 
                                               throws Exception {
        
        String host = envProperties.getMachineHostnameByRoleId(SaasUITestbed.LOAD_ROLE_ID);
        validateAgentsConnected(driver, agentType, agentSubType, destFolder,
            appName, appServer, host, agentProfileUpdates);
    }
    
    protected void validateAgentsConnected(RemoteWebDriver driver, String agentType, String agentSubType,
        String destFolder, String appName, String appServer, String host, HashMap<String,String> agentProfileUpdates) 
            throws Exception {
        
        logger.info("agentType {}, agentSubType {}, destFolder {}, appServer {}, appName {}",
            agentType, agentSubType, destFolder, appServer, appName);
        attemptLogin(driver);

        // download the agent based on OS, agent Type and agentSubType
        DownloadAgentPage objDownloadAgent = new DownloadAgentPage(driver);
        String filePackage =
            objDownloadAgent.downloadAgentPackage(SaaSUtils.getOSType(), agentType, agentSubType);
        Assert.assertTrue((filePackage != null), "Cannot find downloaded Agent Package ["
            + agentType + agentSubType + "] filePackage [" + filePackage + "]");

        // Unzip the downloaded agent
        wilyDir =
            objDownloadAgent.unzipPackage(filePackage, SaaSUtils.getDeployPath(destFolder));
        logger.info("Agent '{}/{}' is unzipped to {}.", agentType, agentSubType, SaaSUtils.getDeployPath(destFolder));

        // check wily dir
        if (wilyDir == null) {
            String error =
                "Cannot find wily Folder after unzipping agent package @ "
                    + SaaSUtils.getDeployPath(destFolder);
            logger.error(error);
            Assert.fail(error);
        }

        // Verify whether the downloaded agent has the required bundles
        objDownloadAgent.verifyAgentBundles(agentType, agentSubType, wilyDir);

        // Start agent and verify whether it has started successfully
        boolean status = startAgent(SaaSUtils.getOSType(), agentType, agentSubType, wilyDir, appServer,
                appName, agentProfileUpdates);
        Assert.assertTrue(status, " Application '" + appName + "' not started successfully");

        // Search agent log for connection status. Wait for 10 attempts retrying
        // every 15 seconds.
        String logFolder = wilyDir + "/logs";
        HashMap<Integer, String> allMatches = new HashMap<Integer, String>();
        allMatches =
            SaaSUtils.parseLog(logFolder, "IntroscopeAgent",
                "Connected controllable Agent to the Introscope Enterprise Manager");
        int i = 0;
        while ((allMatches.size() == 0) && (i < 10)) {
            logger
                .info("Unable to verify in log - \"Connected controllable Agent to the Introscope Enterprise Manager\" "
                    + logFolder + ". Retrying in 15 seconds. Attempt " + (i + 1) + "/10");
            i++;
            Thread.sleep(15000);
            allMatches =
                SaaSUtils.parseLog(logFolder, "IntroscopeAgent",
                    "Connected controllable Agent to the Introscope Enterprise Manager");
        }
        Assert.assertTrue((allMatches.size() != 0),
            "Could not verify agent connection status. Please review agent log");
       
        checkAgentConnectedUI(objDownloadAgent, host, null, agentType,
        						agentSubType, appName);
        // TODO Add an ignore list, for known errors.
        // Search agent log for errors
        allMatches = SaaSUtils.parseLog(logFolder, "IntroscopeAgent", "ERROR");
        Assert.assertTrue((allMatches.size() == 0), "Errors found in log file @" + wilyDir
            + "/logs - Line numbers " + allMatches.keySet());
    }
    
    protected void validateMap(String appName, String[] mapSubNodes) throws Exception {
        validateMap(appName, mapSubNodes, false);
    }

    protected void validateMap(String appName, 
                               String[] mapSubNodes, boolean shouldFitAllToView) throws Exception {
        
        if(mapSubNodes == null || mapSubNodes.length == 0) {
            logger.info("Skipping map validation as node info wasn't provided...");
            return;
        }
        
        //validate generic frontends data
        String nodeName = "Apps|" + appName.toLowerCase();
        Assert.assertTrue(mapHelper.clickComponentViewTab(nodeName, "Generic Front-end"), 
            "'Generic Front-end' tab is missing for node " + nodeName + " or node itself didn't appear.");
        validateGenericFrontendData(appName);
        
        //validate agent/servlet tabs
        for(String node: mapSubNodes) {
            logger.info("Validating agent & servlet data for node '{}'...", node);
            if(shouldFitAllToView) {
                Assert.assertTrue(
                    mapHelper.clickFitAll(), "Failed to click on 'Fit all to view' map option.");
            }
            
            //validate Agent data
            Assert.assertTrue(clickAgentTab(node), 
                "Either 'Agent' tab is missing for node '" + node + "' or node itself didn't appear.");
            validateAgentTabData();
            
            //validate Servlet data
            mapHelper.clickComponentViewTab("Servlet");
            validateApplicationServletData(appName.toLowerCase());
        }
    }
    
    /**
     * Open map view & add filter by hostname
     * 
     * @param host host name
     * @throws Exception
     */
    protected void filterMapByHost(String host) throws Exception {
        filterMapByHost(host, "No Perspective");
    }
        
    /**
     * Open map view & add filter by hostname
     * 
     * @param host host name
     * @param perspective perspective name
     * @throws Exception
     */
    protected void filterMapByHost(String host, String perspective) throws Exception {

        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view."); 
        if (!containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");
        }
        Assert.assertTrue(mapHelper.clickPerspective(perspective), 
            "Unable to select '" + perspective + "' perspective. Check map loading wasn't hanging.");
        mapHelper.clickLiveTimeRange();
        
        // Adding a delay of 60 sec since it takes more than anticipiated time
        // for the Agent to be reflected in MapView
        Thread.sleep(60000);
        
        if (!containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", new String[]{host}), "Failed to add host filter");
        }
    }
 
    /**
     * Open map view & add filter by hostname & application
     * 
     * @param appName application name
     * @param host host name
     * @throws Exception
     */
    protected void filterMapByAppAndHost(String appName, String host) throws Exception {
        
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");   
        if (!containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(mapHelper.clickApplicationLayer(), "Unable to select 'Application Layer'");
        }
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        mapHelper.clickLiveTimeRange();  

        // Adding a delay of 60 sec since it takes more than anticipiated time
        // for the Agent to be reflected in MapView
        Thread.sleep(60000);
       
        if (containsIgnoreCase(instance, "onprem")) {
            Assert.assertTrue(
                mapHelper.addNewMapFilter("Application", new String[] {appName.toLowerCase()}),
                "Failed to add application filter");
        } else {
            Assert.assertTrue(mapHelper.addNewMapFilter("Hostname", new String[] {host}),
                "Failed to add host filter");
            Assert.assertTrue(
                mapHelper.addAdditionalMapFilter("Application", new String[] {appName}),
                "Failed to add application filter");
        }
    }

    protected void validateTTViewer(String agentType, String agentSubType, String appName,
                                    String host, String port) throws Exception {
       
        String agentName = agentType + agentSubType + "_" + appName;
        
        for(TraceInfo trace: WebAppTraceDetails.TRACES_INFO) {
            if(trace.getNodeName().contains(appName.toLowerCase())) {
                String url = "http://" + host + ":" + port + trace.getUrl();
                validateTrace(agentName, host, url, trace);
            }
        }
    }
     
    protected void validateTrace(String agentName, String host, 
                                 String url, TraceInfo trace) throws Exception {
        
        validateTrace(agentName, host, url, trace, false);
    }
    
    protected void validateTrace(String agentName, String host, 
                                 String url, TraceInfo trace, 
                                 boolean isBAEnabled) throws Exception {
            
        String traceType = trace.getTraceType();
        String ttViewerNode = trace.getNodeName();
        
        if(isBAEnabled) {
            if(!traceType.equalsIgnoreCase("Normal")) {
                logger.info("Skipping '{}' tracing for BA tests...", traceType); return;
            }
            ttViewerNode = trace.getBtNodeName();
        }
        
        //start transaction trace session
        if(traceType.equalsIgnoreCase("Normal")) {
           startTransactionTrace(agentName, host);
        }
        else if(traceType.equalsIgnoreCase("TraceAll_Normal")) {            
            if(instance.contains("onprem")) {
                logger.info("Skipping '{}' tracing for onprem tests...", traceType); return;
            }
            startAllAgentsTransactionTrace();
        }
        
        //generate load
        logger.info("Sending http requests to generate '{}' traces...", traceType);
        sendHttpRequests(url, trace.getNumberRequests(), isBAEnabled);
               
        //validate traces
        logger.info("Validating {} traces...", traceType);         
        Assert.assertTrue(mapHelper.waitToClickNode(ttViewerNode), "Failed to click on node " + ttViewerNode);
        Assert.assertTrue(mapHelper.openBusinessTxns(), "Failed to open TTViewer.");       
        Assert.assertTrue(mapHelper.openLastTrace(traceType), "Failed to open last trace for type " + traceType); 
        
        String traceComponent = trace.getNodeName().substring(5).replace("�", "");

        if(isBAEnabled) {
            mapHelper.selectTraceComponent("Business Segment|" + host + "/");
            mapHelper.expandTraceComponents(traceComponent);
        }
        
        //check trace components
        mapHelper.selectTraceComponent(traceComponent);
        validateTraceComponents(trace);
        mapHelper.collapseBusinessTxns();
    }
    
    private void validateTraceComponents(TraceInfo trace) throws Exception {
        
        for(HashMap<String,String> component: trace.getComponents()) {
            
            //verify main view
            for (Map.Entry<String, String> property : component.entrySet()) {
                if(property.getKey().equals("Path")) {
                    continue; //TODO fix later
                }
                Assert.assertTrue(mapHelper.isTracePropertyPresent(property.getKey(), property.getValue()), 
                    "Trace 'Details' property doesn't exist: " + property.getKey() + "=" + property.getValue());
            }
            
            //verify summary & tree view for standard trace
            if(trace.getTraceType().equals("Standard")) {                
                String element = component.get("Path"); 
                if(element == null) {
                    element = component.get("Resource Name");
                }   
                
                mapHelper.openTraceView("Summary");
                Assert.assertTrue(mapHelper.isTraceSummaryPresent(element), "Trace summary check failed for " + element);
                mapHelper.openTraceView("Tree");
                if(!mapHelper.expandTreeView()) {
                    mapHelper.colapseTreeView();
                    Assert.assertTrue(mapHelper.expandTreeView(), "Unable to expand tree view");
                }              
                Assert.assertTrue(mapHelper.isTraceSummaryPresent(element), "Trace tree check failed for " + element);
            }
        }        
    }
    
    protected void startTransactionTrace(String agentName, String host) throws Exception {
        
        startTransactionTrace(agentName, wilyDir + "/core/config/IntroscopeAgent.profile", host);
    }
    
    protected void startTransactionTrace(String agentName, String agentProfile, 
                                         String host) throws Exception {
        
        //disable sampling
        Assert.assertTrue(wilyDir != null, "Agent home directory wasn't initialized.");
        logger.info("Disabling agent sampling...");
        HashMap<String,String> agentProfileUpdates = new HashMap<String,String>();
        agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.enabled", "false");           
        SaaSUtils.updateProperties(agentProfile, agentProfileUpdates);
        Thread.sleep(40000);
        
        //start tt session
        startUITransactionTrace(agentName, host, "1000", "1");
    }
   
    protected void startUITransactionTrace(String agentName, 
                                         String agentHost,
                                         String minTraceDuration, 
                                         String traceSessionDuration) throws Exception {
        
        AgentsPage page = new AgentsPage(ui.getDriver());
        page.clickAgentsTab();
        page.startTransactionTrace(agentName, agentHost, minTraceDuration, traceSessionDuration);
        
        //return to the map
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");
        mapHelper.clickLiveTimeRange();
    }

    protected void startAllAgentsTransactionTrace() throws Exception {
        
        //leave for debugging: wilyDir = "C:\\automation\\deployed\\tomcatv80\\wily";
        startAllAgentsTransactionTrace(wilyDir + "/core/config/IntroscopeAgent.profile");
    }

    protected void startAllAgentsTransactionTrace(String agentProfile) throws Exception {
        
        //disable sampling
        Assert.assertTrue(wilyDir != null, "Agent home directory wasn't initialized.");
        logger.info("Disabling agent sampling...");
        HashMap<String,String> agentProfileUpdates = new HashMap<String,String>();
        agentProfileUpdates.put("introscope.agent.transactiontracer.sampling.enabled", "false");           
        SaaSUtils.updateProperties(agentProfile, agentProfileUpdates);
        Thread.sleep(40000);
        //start tt session
        AgentsPage page = new AgentsPage(ui.getDriver());
        page.clickAgentsTab();
        page.startTraceAllAgents();
        
        //return to the map
        Assert.assertTrue(mapHelper.clickMapView(10000), "Unable to open Map view.");
        mapHelper.clickLiveTimeRange();
    }
    
    protected boolean clickBtTab(String nodeName) throws InterruptedException {
        return waitForComponentViewTab(nodeName, "Business Transaction");
    }
    
    protected boolean clickAgentTab(String nodeName) throws InterruptedException {
        return waitForComponentViewTab(nodeName, "Agent");
    }
    
    protected boolean waitForComponentViewTab(String nodeName, String tabName) throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        long maxWaiting = 1200000;//20 min
        
        while((System.currentTimeMillis() - startTime) < maxWaiting) {
            logger.info("Waiting for '{}' tab to appear for node {}... Max {} ms.", 
                tabName, nodeName, maxWaiting);
            if(!mapHelper.clickNode(nodeName)) {
                continue;
            }
            if(mapHelper.clickComponentViewTab(tabName)) {
                return true;
            }
            mapHelper.clickReloadMap();
            Thread.sleep(20000);
        }
        
        return false;
    }
   
    protected void validateGenericFrontendData(String appName) {

        String[] data =
            new String[] {"GENERICFRONTEND", "Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval",
                          "Stall Count", "Concurrent Invocations", appName};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + "' doesn't exist");
        }
    }
    
    protected void validateApplicationEPData(String appName) {

        //verify metrics
        String[] data =
            new String[] {"Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval",
                          "Stall Count", "Concurrent Invocations", appName, "Alerts Summary"};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + "' doesn't exist");
        }
        
        //verify alerts
        String[] alerts = new String[]{"Application Errors", "Response Time Variance Intensity"};
        for(String alert: alerts) {
            Assert.assertTrue(mapHelper.isAlertPresent(alert), "Alert '" + alert + "' is missing.");
        }
    }
    
    private void validateApplicationServletData (String appName) throws InterruptedException {

        String[] data =
            new String[] {"Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval",
                          "Stall Count", "Concurrent Invocations", appName};
        
        int i = 0;
        
        for (String metric : data) {

            // Added below condition since appName is always passed in lower case, while
            // the script expects to match camel case
            if (metric.equals("archetype created web application")) {
                metric = "Archetype Created Web Application";
            }
            while (i < 5) {
                try {
                    Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '"
                        + metric + "' doesn't exist");
                    i = 5;
                } catch (java.lang.AssertionError error) {
                    i++;
                    logger.info("Exception while locating " + metric
                        + ". Will try again in 120 seconds (Attempt " + i + "/5).");
                    // Wait for 120 seconds and try again 5 times
                    Thread.sleep(120000);
                    continue;
                }
            }
        }
    }
    
    protected void validateAgentTabData() throws InterruptedException {
        validateAgentTabData(false);
    }
    
    protected void validateAgentTabData(boolean shouldValidateAlerts) throws InterruptedException {

        String[] data =
            new String[] {"AGENT", "% Time Spent in GC", "Percentage of Java Heap Used",
                    "Percentage of Time Spent in GC during last 15 minutes", "Metric Count",
                    "Enterprise Team Center"};
        
        int i = 0;
        
        for (String metric : data) {
            while (i < 5) {
                try {
                    Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '"
                        + metric + "' doesn't exist");
                    i = 5;
                } catch (java.lang.AssertionError error) {
                    i++;
                    logger.info("Exception while locating " + metric
                        + ". Will try again in 120 seconds (Attempt " + i + "/5).");
                    // Wait for 120 seconds and try again 5 times
                    Thread.sleep(120000);
                    continue;
                }
            }
        }
        
        if(shouldValidateAlerts) {
            logger.info("Validating danger alerts for AGENT component view...");            
            String alertStatusXpath = "//*[td/span[text()[contains(.,'Alert Status')]] and td/span[text()[contains(.,'Danger')]]]";
            String alertMetricXpath = "//div[img[@ng-src='res/APM-Icons_Danger.svg'] and a[text()[contains(.,'CPU')]]]";
            Assert.assertTrue(mapHelper.attemptToFindElement(alertStatusXpath, 5000, 20), 
                "Expected alert status doesn't exist.");
            Assert.assertTrue(mapHelper.attemptToFindElement(alertMetricXpath, 5000, 20), 
                "Expected metric doesn't exist.");
        }
    }
    
    /**
     * @param objDownloadAgent
     * @param agents map with key='agent host' & value='agent name'
     * @throws Exception
     */
    public void checkAgentsConnectedUI(DownloadAgentPage objDownloadAgent, 
                                       HashMap<String,String> agents) throws Exception {
        
        for (Map.Entry<String, String> item : agents.entrySet()) {
            checkAgentConnectedUI(objDownloadAgent, item.getKey(), item.getValue(),"","","");
        }
    }
    
    public void checkAgentConnectedUI(DownloadAgentPage objDownloadAgent, String host,
        String agentName, String agentType, String agentSubType, String appName)
        throws InterruptedException, IOException {

        // Navigate to agents tab and verify connection status every 30 seconds, 20 times
        // attempts retrying every 15 seconds.
        boolean isConnected = false;
        int i = 0;
        long sleep = 30000;
 
        while (!isConnected && (i < 10)) {
            logger.info("Attempt {}/10 to check agent connection status in UI. " + 
                "Retrying every {} seconds", (i + 1), (sleep/1000));
            objDownloadAgent.clickAgentsTab();
            i++;
            Thread.sleep(sleep);   

            objDownloadAgent.sortAgentsByHost();//ascending order sort
            objDownloadAgent.sortAgentsByHost();//descending order sort - easy to locate tas machines
           
            isConnected =
                objDownloadAgent.singleAgentConnectionStatus(agentName, agentType, agentSubType,
                    appName, host);
       }
        Assert.assertTrue(isConnected, "Unable to verify agent connection status.");
    }

    public boolean checkAgentDisconnectedUI(DownloadAgentPage objDownloadAgent, String host,
        String agentName, String agentType, String agentSubType, String appName)
        throws InterruptedException, IOException {
    
        // Navigate to agents tab and verify Disconnection status. Wait for 10
        // attempts retrying every 40 seconds.
        objDownloadAgent.clickAgentsTab();
    
        boolean isDisconnected = false;
        int i = 0;
    
        while (!isDisconnected && (i < 10)) {
            logger.info("Attempt " + (i + 1)
                + "/10 to check agent Disconnection status in UI. Retrying every 40 seconds");
            i++;
            Thread.sleep(40000);
            isDisconnected =
                objDownloadAgent.singleAgentDisconnectionStatus(agentName, agentType, agentSubType, appName, host);           
        }
        Assert.assertTrue(isDisconnected, "Unable to verify agent Disconnection status.");
        logger.info("Agent showing disconnected status is: " + isDisconnected);
        return isDisconnected;
    }
    
    private void startSingleStressApp(String agentType, String agentSubType)
        throws InterruptedException {

        String startAppKey;
        if (containsIgnoreCase(agentType,"Java"))
            startAppKey = agentType + "_stressapp_load_start";
        else
            startAppKey = agentType + agentSubType + "_stressapp_load_start";

        try {
            runSerializedCommandFlowFromRoleAsync(SaasUITestbed.LOAD_ROLE_ID, startAppKey);
            logger.info("Agent and load started: " + startAppKey);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("Error encountered while starting " + startAppKey);
        }
        Thread.sleep(30000);
    }
  
    public boolean startJMeter(String webAppHost,                               
                               String webAppPort, 
                               String jmeterScript, 
                               long duration) throws InterruptedException {
        
        return startJMeter(envProperties.getMachineIdByRoleId(SaasUITestbed.JMETER_SCRIPTS_ROLE_ID), 
            webAppHost, webAppPort, jmeterScript, duration);
    }
    
    public boolean startJMeter(String machineId,
                               String webAppHost,                               
                               String webAppPort, 
                               String jmeterScript, 
                               long duration) throws InterruptedException {
        
        try {  
            String scriptDir = envProperties.getTestbedPropertyById("jmeter.scripts.install.dir");
            String installDir = envProperties.getTestbedPropertyById("jmeter.install.dir");
            
            List<String> jmeterArgs = new ArrayList<String>();
            jmeterArgs.add("-n");
            jmeterArgs.add("-t");
            jmeterArgs.add(scriptDir + "/" + jmeterScript);
            jmeterArgs.add("-Jhost=" + webAppHost);
            jmeterArgs.add("-Jport=" + webAppPort);
            jmeterArgs.add("-Jduration=" + duration/1000);
            
            logger.info("[JMETER STARTUP] Starting {}/{}", scriptDir, jmeterScript);
            RunCommandFlowContext runJmeter =
                new RunCommandFlowContext.Builder("jmeter.bat")
                    .workDir(installDir + "/bin")
                    .args(jmeterArgs)
                    .build();
            
            logger.info("[JMETER STARTUP] jmeter command: " + installDir + 
                "/bin/jmeter.bat " + jmeterArgs.toString().replace(",", ""));
            runCommandFlowByMachineId(machineId, runJmeter);
            
            return true;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("Error encountered while starting Jmeter");
            return false;
        }
    }

    public boolean startTixmonsterWithJBoss(String destWilyFolder, String appName)
        throws InterruptedException {
        String jBossStartKey = "jBossStartKey";
        try {
            runSerializedCommandFlowFromRole(SaasUITestbed.JBOSS_START_ROLE_ID, jBossStartKey);
            logger.info("JBoss server and Agent started");
            Thread.sleep(60000);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("Error encountered while starting JBoss server/Agent");
            return false;
        }
    }
    
    public boolean containsIgnoreCase(String mainString, String subString){
    	mainString = mainString.toLowerCase();
    	subString = subString.toLowerCase();
    	if(mainString.contains(subString)) return true;
    	else return false;
    }
    
    @AfterSuite(alwaysRun = true)
    public void emailTestngResults(ITestContext context) {
       
        String emailEnabled = context.getCurrentXmlTest().getParameter("testngEmailEnabled");
        
        if (!Boolean.parseBoolean(emailEnabled)) {
            logger.info("Skipping emailing testng report as email isn't enabled.");
            logger.info("Provide 'testngEmailEnabled' & 'testngEmailRecipients' params in your test suite.");
            return;
        }
        
        Collection<ITestResult> passed = context.getPassedTests().getAllResults();
        Collection<ITestResult> failed = context.getFailedTests().getAllResults();
        Collection<ITestResult> skipped = context.getSkippedTests().getAllResults();
        Collection<ITestResult> failedConfig = context.getFailedConfigurations().getAllResults();
        
        String status = "FAILED";
        if(failed.size() == 0 && failedConfig.size() == 0) {
            status = "PASSED";
        }
            
        //attach testng report
        String suiteName = context.getSuite().getName();
        String reportDir = "./test-output/" + suiteName;
        String screenshotsDir = "./" + SaaSUtils.SCREENSHOTS_FOLDER_NAME;
        
        //attach testng report
        ArrayList<String> attachments = new ArrayList<String>();
        SaaSUtils.addEmailAttachments(reportDir, "html", attachments);
        if(attachments.size() == 0) {          
            logger.warn("Unable to attach Testng report as html files don't exist under " + reportDir);
        }  
        
        //attach screenshots
        SaaSUtils.addEmailAttachments(screenshotsDir, "png", attachments);
       
        String msgText = "";
        if(status.equals("FAILED")) {
            msgText = "<b><font style='color:red'>TEST SUITE FAILED!</font></b><br/><br/>";
        }
        
        msgText += "Summary: <br/>passed " + passed.size() + "<br/>skipped " + 
            skipped.size() + "<br/>failed " + failed.size() + "<br/>failed config " + failedConfig.size();  
        
        String resmanApi = envProperties.getTestbedPropertyById("resmanApi");
        String taskId = envProperties.getTestbedPropertyById("taskId");
        msgText += SaaSUtils.getResmanInfo(resmanApi, taskId);
        
        //send email     
        String emailRecipients = SaaSUtils.getPropertyValue("testngEmailRecipients", context, 
                SaaSUtils.LOGIN_MAP.get(instance + "_testngEmailRecipients"));

        String msgPriority = "3";        
        if(status.equals("FAILED") && instance != null && instance.equals("production")) {
            msgPriority = "1";
        }
        
        String subject = "[SAAS " + instance + "] " + suiteName + " - " + status;
        if(instance.equals("onprem")) {
            String version = envProperties.getTestbedPropertyById(
                SaasBaseTestbed.ARTIFACTS_VERSION_PROPERTY_KEY);
            subject = "[ONPREM] " + suiteName + " - " + status;
            if(version != null) {
                subject += " - " + version.substring(0, version.indexOf("-SNAPSHOT"));
            }
        }
        EmailUtil.sendEmail(subject, msgText, emailRecipients, attachments, msgPriority);
    }
    
    protected void sendHttpRequests(String url, int numReqs) {
        sendHttpRequests(url, numReqs, false);
    }
    
    protected void sendHttpRequests(String url, int numReqs, boolean isBAEnabled) {
        
        if(isBAEnabled) {
            logger.info("Sending http requests via browser..."); 
            try {
                WebDriver tempDriver = getUIInstance().getDriver();
                for(int i=0; i<numReqs; i++) {
                    //ui.getDriver().get(url);
                    tempDriver.get(url);
                    Thread.sleep(1000);               
                } 
                Thread.sleep(20000);
                tempDriver.quit();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error occurred while generating load via browser: " + e.getMessage());
            }
        }
        else {
            logger.info("Sending http requests via HttpTxnGen..."); 
            HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url)
                .setNumberReqs(numReqs)
                .setSocketTimeout(3L, TimeUnit.MINUTES)
                .build();
            txnGen.start();
        }
    }
    
    protected void disableDocsAuth() {
        
        HashMap<String,String> customHeaders = new HashMap<String,String>();
        customHeaders.put("X-Referer", "CA_APMSAAS_");
        String url = "https://docops.ca.com/rest/ca/product/latest/topic?hid=HID_Map&space=APMSAAS&format=rendered&language=";
        
        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url)
            .setNumberReqs(1)
            .setCustomHeaders(customHeaders)
            .build();
        txnGen.start();
    }
    
    protected void copyPackageRemotely(String localMachineId, String agentHost, 
                                       String localPath, String destPath) {

        SshUploadFlowContext copyContext = new SshUploadFlowContext.Builder()
            .destDir(destPath)
            .file(localPath)
            .host(agentHost)
            .user("root")
            .password("Lister@123")
            .build();            
        runFlowByMachineId(localMachineId, SshUploadFlow.class, copyContext);
    }
}