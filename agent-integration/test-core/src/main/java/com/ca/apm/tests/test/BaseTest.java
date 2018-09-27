package com.ca.apm.tests.test;

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
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.testbed.atc.SeleniumGridMachinesFactory;
import com.ca.apm.tests.pagefactory.HomePage;
import com.ca.apm.tests.pagefactory.LoginPage;
import com.ca.apm.tests.pagefactory.MapPage;
import com.ca.apm.tests.test.utils.EmailUtil;
import com.ca.apm.tests.test.utils.HttpTxnGen;
import com.ca.apm.tests.test.utils.TraceInfo;
import com.ca.apm.tests.test.utils.Utils;
import com.ca.apm.tests.test.utils.WebAppTraceDetails;
import com.ca.apm.tests.testbed.BaseTestbed;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;

public class BaseTest extends TasTestNgTest {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private EnvironmentPropertyContext env = null;
    protected UI ui = null;
    private Method methodUnderTest = null;
    protected String registrationURL = "";
    protected String wilyDir = null;
    protected MapPage mapHelper = null;
    protected HomePage homeHelper = null;
    protected static String instance;
    protected static String loginURL;
    protected static String email;
    protected static String user;
    protected static String password;
    protected static String tenant;
    protected static String testngEmailRecipients;
    protected static String dockerImage;
   
    @BeforeSuite(alwaysRun = true)
    public void setupTestSuite(ITestContext context) {

        instance = "onprem";
        user = "Admin";
        password = "";
        loginURL = "http://{host}:8082/ApmServer/";
        testngEmailRecipients = "Marina.Kur@ca.com,Anand.Krishnamurthy@ca.com,Martin.Janda@ca.com,Swetha.Bhamidipati@ca.com,Abhijit.Bhadra@ca.com,SarojK.Swain@ca.com";
        dockerImage = Utils.getPropertyValue("dockerImage", context, InfrastructureBaseTest.DEFAULT_DOCKER_MONITOR_IMAGE);
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
        Utils.fillChromeOptions(opt);
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
    
    protected void login(RemoteWebDriver driver, String host) throws Exception {
        
        loginURL = loginURL.replace("{host}", host);
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        logger.info("Login as: {}/{} to {}", user, password, loginURL);
        driver.get(loginURL);
        Thread.sleep(10000);

        LoginPage objLogin = new LoginPage(driver);
        objLogin.loginToApmServer(user, password);
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
            Utils.updateProperties(
                destWilyFolder + "/core/config/IntroscopeAgent.profile", agentProfileUpdates);
        }
        
        logger.info("Starting " + appName + " (" + appServer + ") with " + agentType + agentSubType
            + " agent package.  --> " + destWilyFolder);

        if (appServer.equalsIgnoreCase("StandAlone") && appName.equalsIgnoreCase("StressApp")) {
            startSingleStressApp(agentType, agentSubType);
            return true;
        } else if ((appServer.equalsIgnoreCase("Tomcat")) && (containsIgnoreCase(appName,"Thieves"))) {
            return startTomcatwithThieves(destWilyFolder, appName);        
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
        else{
            logger.info("Unsupported " + "App " + appName + " " + appServer);
            return false;
        }
    }
    
    public boolean startTomcatwithThieves(String destWilyFolder, String appName) {
        return startTomcatwithThieves("testMachine", destWilyFolder, appName);
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
    
    public boolean stopTomcatwithThieves(String destWilyFolder,String appName) {
        return stopTomcatwithThieves("testMachine", destWilyFolder, appName);
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
   
    // This method will stop all agents and loads running
    private void stopStressApp(String agentType, String subPackageType,
            String appServerHome) throws IOException {
        
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
            logger.info(agentType
                            + subPackageType
                            + " agent with StressApp doesn't seem to be running (PID file missing). Nothing to stop.");
            return;
        }

        BufferedReader buffReader = new BufferedReader(new FileReader(
                agentFolder + "\\pid.txt"));
        pid = buffReader.readLine();
        buffReader.close();

        RunCommandFlowContext stopAgentFlowContext = new RunCommandFlowContext.Builder(
                "taskkill").args(Arrays.asList("/F", "/PID", pid)).build();
        runCommandFlowByMachineId("testMachine",
                stopAgentFlowContext);

        RunCommandFlowContext deletePidFileFlowContext = new RunCommandFlowContext.Builder(
                "del").args(Arrays.asList(agentFolder + "\\pid.txt")).build();
        runCommandFlowByMachineId("testMachine",
                deletePidFileFlowContext);

        logger.info(agentType + subPackageType
                + " agent with StressApp stopped.");
    }
   
    /**
     * Do some cleanup after test
     */
    @AfterMethod(alwaysRun = true)
    public void after(ITestResult testResult) {
        
        if(ui != null) {
            if (testResult.getStatus() == ITestResult.FAILURE) {
                Utils.takeScreenshot(ui.getDriver(), "FAILURE", 
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
        Assert.assertTrue(mapHelper.clickPerspective(perspective), 
            "Unable to select '" + perspective + "' perspective. Check map loading wasn't hanging.");
        mapHelper.clickLiveTimeRange(); 
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
        Assert.assertTrue(mapHelper.clickNoPerspective(), 
            "Unable to select 'No Perspective' perspective. Check map loading wasn't hanging.");
        mapHelper.clickLiveTimeRange(); 
        Assert.assertTrue(mapHelper.addNewMapFilter("Application", 
            new String[]{appName.toLowerCase()}), "Failed to add application filter");
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
        
        //generate load
        logger.info("Sending http requests to generate '{}' traces...", traceType);
        sendHttpRequests(url, trace.getNumberRequests(), isBAEnabled);
               
        //validate traces
        logger.info("Validating {} traces...", traceType);         
        Assert.assertTrue(mapHelper.waitToClickNode(ttViewerNode), "Failed to click on node " + ttViewerNode);
        Assert.assertTrue(mapHelper.openBusinessTxns(), "Failed to open TTViewer.");       
        Assert.assertTrue(mapHelper.openLastTrace(traceType), "Failed to open last trace for type " + traceType); 
        
        String traceComponent = trace.getNodeName().substring(5).replace("…", "");

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
        Utils.updateProperties(agentProfile, agentProfileUpdates);
        Thread.sleep(40000);
        
        //start tt session
        startClwTransactionTrace();
    }
   
    protected void startClwTransactionTrace() {
        
        logger.info("Using clw to start tt session...");
        String emHost = envProperties.getRolePropertyById(BaseTestbed.EM_ROLE_ID, "em_hostname");
        String emDir = envProperties.getRolePropertyById(BaseTestbed.EM_ROLE_ID, "USER_INSTALL_DIR");
        
        List<String> args = new ArrayList<String>();
        args.add("-Duser=Admin");
        args.add("-Dpassword=");
        args.add("-Dhost=" + emHost);
        args.add("-Dport=5001");
        args.add("-jar");
        args.add(emDir + "/lib/CLWorkstation.jar");
        args.add("trace transactions exceeding 1 ms in agents matching .* for 120 s");
        
        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("java")
                .args(args)
                .build();
        
        String machineId = envProperties.getMachineIdByRoleId(BaseTestbed.EM_ROLE_ID);
        
        //removing async call as sometimes there is an issue with it
        //using thread instead
        //runCommandFlowByMachineIdAsync(machineId, command);
        new Thread() {
            public void run() {
                runCommandFlowByMachineId(machineId, command);
            }
        }.start();
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
    
    protected void validateApplicationServletData(String appName) {

        String[] data =
            new String[] {"Average Response Time (ms)", "Errors Per Interval", "Responses Per Interval",
                          "Stall Count", "Concurrent Invocations", appName};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + "' doesn't exist");
        }
    }
    
    protected void validateAgentTabData() {
        validateAgentTabData(false);
    }
    
    protected void validateAgentTabData(boolean shouldValidateAlerts) {

        String[] data =
            new String[] {"AGENT", "% Time Spent in GC", "Percentage of Java Heap Used",
                    "Percentage of Time Spent in GC during last 15 minutes", "Metric Count",
                    "Enterprise Team Center"};
        for (String metric : data) {
            Assert.assertTrue(mapHelper.isMetricPresent(metric), "Metric/Attribute '" + metric
                + "' doesn't exist");
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
    
    private void startSingleStressApp(String agentType, String agentSubType)
        throws InterruptedException {

        String startAppKey;
        if (containsIgnoreCase(agentType,"Java"))
            startAppKey = agentType + "_stressapp_load_start";
        else
            startAppKey = agentType + agentSubType + "_stressapp_load_start";

        try {
            runSerializedCommandFlowFromRoleAsync(BaseTestbed.LOAD_ROLE_ID, startAppKey);
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
        
        return startJMeter(envProperties.getMachineIdByRoleId(BaseTestbed.JMETER_SCRIPTS_ROLE_ID), 
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
        String screenshotsDir = "./" + Utils.SCREENSHOTS_FOLDER_NAME;
        
        //attach testng report
        ArrayList<String> attachments = new ArrayList<String>();
        Utils.addEmailAttachments(reportDir, "html", attachments);
        if(attachments.size() == 0) {          
            logger.warn("Unable to attach Testng report as html files don't exist under " + reportDir);
        }  
        
        //attach screenshots
        Utils.addEmailAttachments(screenshotsDir, "png", attachments);
       
        String msgText = "";
        if(status.equals("FAILED")) {
            msgText = "<b><font style='color:red'>TEST SUITE FAILED!</font></b><br/><br/>";
        }
        
        msgText += "Summary: <br/>passed " + passed.size() + "<br/>skipped " + 
            skipped.size() + "<br/>failed " + failed.size() + "<br/>failed config " + failedConfig.size();  
        
        String resmanApi = envProperties.getTestbedPropertyById("resmanApi");
        String taskId = envProperties.getTestbedPropertyById("taskId");
        msgText += Utils.getResmanInfo(resmanApi, taskId);
        
        //send email     
        String emailRecipients = Utils.getPropertyValue(
            "testngEmailRecipients", context, testngEmailRecipients);

        String msgPriority = "3";        
        if(status.equals("FAILED") && instance != null && instance.equals("production")) {
            msgPriority = "1";
        }
      
        String version = envProperties.getTestbedPropertyById(
            BaseTestbed.ARTIFACTS_VERSION_PROPERTY_KEY);
        String subject = "[ONPREM] " + suiteName + " - " + status;
        if(version != null) {
            subject += " - " + version.substring(0, version.indexOf("-SNAPSHOT"));
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