/*
 * Copyright (c) 2016 CA. All rights reserved.
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


package com.ca.apm.tests.test.noagent;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;

import com.ca.apm.tests.testbed.BrowserAgentBaseTestbed;
import com.ca.apm.tests.utils.AgentDetails;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.EmDetails;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.StopWatch;
import com.ca.apm.tests.utils.constants.AgentPropertyConstants.AgentDefaults;
import com.ca.apm.tests.utils.constants.AgentPropertyConstants.BrowseAgentProperties;
import com.ca.apm.tests.utils.constants.BusinessServiceConstants.Test_BS_BT;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.TasTestNgTest;


/**
 * Base class test execution on tas. Parent class for a suite of tests
 * 
 */

public class BANoAgentBaseTest extends TasTestNgTest {

    protected String browser;
    protected String seleniumUrl;

    // TODO: Better way to pick CLW command for Transaction trace tests
    protected int TransactionTraceFilter = 0;

    public static long totalSleepTime = 0;
    public static long totalMetricTime = 0;
    public static boolean resetExtensionFile = false;
    public static boolean agentRestart = false;
    protected static StopWatch totalExecutionTime;

    protected static EmDetails em;
    protected static AgentDetails agent;
    protected static SeleniumDetails seleniumData;

    protected static final Logger LOGGER = LoggerFactory.getLogger(BANoAgentBaseTest.class);


    protected String automationDirectoryString = null;
    protected String tomcatInstallDirString = null;

    /**
     * This string will contain the full path to the test app files such as
     * GETLocalDomain.jsp, this could vary by application server installed.
     */

    protected String testAppFileLocation = null;


    /**
     * Set up the test environment.<br>
     * Gets the configuration data from envProperties
     * Starts Selenium, Launches the specified browser.
     */

    protected void testSuiteSetUp() {
        System.out.println("testSuiteSetUp called from parent");
        String applicationServer;
        String agentHost;
        String agentHostUserName;
        String agentHostPassword;
        String agentHome;
        String agentProfileFileFullPath;
        String agentProcessName;
        String agentName;
        String applicationServerPort;
        String emHost;
        int emPort;
        String emUsername;
        String emPassword;
        String cemPort;
        String clwJar;

        totalExecutionTime = new StopWatch();
        totalExecutionTime.start();


        automationDirectoryString =
            envProperties.getMachinePropertyById("browserAgentMachine", "automationBaseDir");

        applicationServer =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "appServer");
        applicationServerPort =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "appServerPort");

        agentHost =
            envProperties.getMachinePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_MACHINE_ID,
                "hostname");
        agentHostUserName =
            envProperties.getMachinePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_MACHINE_ID,
                "localSSHUserName");
        agentHostPassword =
            envProperties.getMachinePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_MACHINE_ID,
                "localSSHPassword");
        agentHome =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "agentHome");
        agentProfileFileFullPath =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "agentProfileFileFullPath");
        agentName =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "agentName");
        agentProcessName =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "agentProcessName");

        agent =
            new AgentDetails.Builder()
                .applicationServer(applicationServer)
                .applicationServerPort(applicationServerPort)
                .agentHost(agentHost)
                .agentHostUserName(agentHostUserName)
                .agentHostPassword(agentHostPassword)
                .agentHome(agentHome)
                .agentProfileFileFullPath(agentProfileFileFullPath)
                .agentName(agentName)
                .agentProcessName(agentProcessName)
                .agentLogFile(agentHome + AgentDefaults.DEFAULT_LOG_FILE)
                .agentJsExtensionFileLocation(
                    agentHome + AgentDefaults.DEFAULT_EXTERNAL_JS_EXTENSION_FILE)
                .transactionTraceDirectory(automationDirectoryString)
                .transactionTraceFile(AgentDefaults.TRANSACTION_TRACE_FILE).build();

        emHost =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.EM_ROLE_ID, "em_hostname");
        emPort =
            Integer.parseInt(envProperties.getRolePropertyById(BrowserAgentBaseTestbed.EM_ROLE_ID,
                "emPort"));
        emUsername =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.EM_ROLE_ID, "dbUser");
        emPassword =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.EM_ROLE_ID, "emPassword");
        cemPort =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.EM_ROLE_ID, "emWebPort");

        clwJar =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.CLW_ROLE_ID, "clwJarPath");

        em =
            new EmDetails.Builder().emHost(emHost).emPort(emPort).emUsername(emUsername)
                .emPassword(emPassword).cemPort(cemPort).clwJar(clwJar).build();

        browser =
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.BROWSERAGENT_ROLE_ID,
                "browser");

        if (applicationServer.equals("TOMCAT")) {
            // URL will be different for different application servers
            seleniumUrl =
                envProperties.getRolePropertyById(BrowserAgentBaseTestbed.TOMCAT_ROLE_ID,
                    "brtmtestapp_url");
            LOGGER.info("BrowserAgentBaseTest seleniumUrl is: ", seleniumUrl);

            tomcatInstallDirString =
                envProperties.getRolePropertyById(BrowserAgentBaseTestbed.TOMCAT_ROLE_ID,
                    "tomcatInstallDir");

            // This sets the application specific file location
            testAppFileLocation =
                tomcatInstallDirString + File.separator + "webapps" + File.separator
                    + "brtmtestapp";

        } else {
            // TODO: Implement for other application servers
            // TODO: besure to set the weblogic, websphere, etc for testAppFileLocation
        }

        // Selenium driver properties
        System
            .setProperty(
                "webdriver.chrome.driver",
                envProperties.getRolePropertyById(
                    BrowserAgentBaseTestbed.CHROME_SELENIUM_DRIVER_ROLE_ID, "seleniumDriverHome")
                    + envProperties.getRolePropertyById(
                        BrowserAgentBaseTestbed.CHROME_SELENIUM_DRIVER_ROLE_ID,
                        "chromeSeleniumDriver"));

        System.setProperty(
            "webdriver.ie.driver",
            envProperties.getRolePropertyById(BrowserAgentBaseTestbed.IE_SELENIUM_DRIVER_ROLE_ID,
                "seleniumDriverHome")
                + envProperties.getRolePropertyById(
                    BrowserAgentBaseTestbed.IE_SELENIUM_DRIVER_ROLE_ID, "ieSeleniumDriver"));

        seleniumData =
            new SeleniumDetails.Builder().browser(browser).seleniumUrl(seleniumUrl).build();

        if (applicationServer.equals("TOMCAT")) {
            // start Tomcat application server before executing tests
            runSerializedCommandFlowFromRole(BrowserAgentBaseTestbed.TOMCAT_ROLE_ID,
                TomcatRole.ENV_TOMCAT_START);
            CommonUtils.sleep(30000);
        }

        // Delete TTXML file from previous runs
        try {
            File TTXML =
                new File(agent.getTransactionTraceDirectory() + agent.getTransactionTraceFile());

            boolean result = TTXML.delete();
            if (result) {
                LOGGER.info("TT XML File deleted successfully");
            } else {
                LOGGER.info("TT XML File not found");
            }

        } catch (Exception e) {
            // File permission problems are caught here.
            e.printStackTrace();
        }

        // Deleting old CEM definitions and creating test business transactions through the CEM
        // WebServices
        try {
            CommonUtils.deleteBizDef(em);
            CommonUtils.setupBizDef(Test_BS_BT.TEST_BUSINESS_SERVICE,
                Test_BS_BT.TEST_BUSINESS_TRANSACTION,
                Test_BS_BT.TEST_BUSINESS_TRANSACTION_COMPONENT, Test_BS_BT.TEST_BUSINESS_COMPONENT,
                "URL", "Port", "", "matches", agent.getApplicationServerPort(), em);
            LOGGER.info("Created Business Definitions - Test Suite setup");
        } catch (Exception e) {
            LOGGER.info("Error during Delete or Create biz def - printing stack trace");
            e.printStackTrace();
        }

        LOGGER.info("End - Before Class");

    }

    /**
     * Restores various agent properties to their test default values in the event that a test case
     * fails and does not properly clean up
     */
    protected void resetProps() {
        System.out.println("Start of resetProps");
        LOGGER
            .info("\nResetting BRTM agent properties back to defaults, possibily restart the app server\n");

        try {
            boolean agentHotPropWait = false;
            // TODO: Review if it will be better to have all properties listed in profile file,
            // instead of depending on default values


            String prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.BROWSER_AGENT_ENABLED, agent);
            if ((!prop.equals("")) && prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.BROWSER_AGENT_ENABLED, "true", agent);
                agentRestart = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.SNIPPET_INSERTION_ENABLED, agent);
            if ((!prop.equals("")) && prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.SNIPPET_INSERTION_ENABLED, "true", agent);
                agentRestart = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.SNIPPET_LOCATION, agent);
            if ((!prop.equals("")) && !prop.isEmpty()) {
                CommonUtils.updateIntroscopeAgentProfileForPropertyDelete(
                    BrowseAgentProperties.SNIPPET_LOCATION, agent);
                agentRestart = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.THROTTLE_RESPONSE_LIMIT, agent);
            if ((!prop.equals("")) && (Integer.parseInt(prop) != 1000)) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.THROTTLE_RESPONSE_LIMIT, "1000", agent);
                agentRestart = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(BrowseAgentProperties.RESOURCE_LIMIT,
                    agent);
            if (!prop.equals("") && (Integer.parseInt(prop) != 100)) {
                CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.RESOURCE_LIMIT,
                    "100", agent);
                agentRestart = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.JS_EXTENSION_LOCATION, agent);
            if (!prop.equals("") && (!prop.isEmpty())) {
                CommonUtils.updateIntroscopeAgentProfileForPropertyDelete(
                    BrowseAgentProperties.JS_EXTENSION_LOCATION, agent);
                agentRestart = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.METRIC_FREQUENCY, agent);
            if (prop.equals("") || (Integer.parseInt(prop) != 0)) {
                CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.METRIC_FREQUENCY,
                    "0", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.EXLUDE_URL_LIST, agent);
            if ((!prop.equals("")) && !prop.isEmpty()) {
                CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.EXLUDE_URL_LIST, "",
                    agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.INCLUDE_URL_LIST, agent);
            if ((!prop.equals("")) && !prop.isEmpty()) {
                CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.INCLUDE_URL_LIST,
                    "", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD, agent);
            if (prop.equals("") || (Integer.parseInt(prop) != 0)) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.PAGE_LOAD_METRICS_THRESHOLD, "0", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED, agent);
            if ((!prop.equals("")) && !prop.contains("true")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.PAGE_LOAD_METRICS_ENABLED, "true", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.AJAX_METRICS_THRESHOLD, agent);
            if (prop.equals("") || (Integer.parseInt(prop) != 0)) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.AJAX_METRICS_THRESHOLD, "0", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.AJAX_METRICS_ENABLED, agent);
            if ((!prop.equals("")) && !prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.AJAX_METRICS_ENABLED, "false", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.JS_FUNCTION_METRICS_THRESHOLD, agent);
            if (prop.equals("") || (Integer.parseInt(prop) != 0)) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.JS_FUNCTION_METRICS_THRESHOLD, "0", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.JS_FUNCTION_METRICS_ENABLED, agent);
            if ((!prop.equals("")) && !prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.JS_FUNCTION_METRICS_ENABLED, "false", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(BrowseAgentProperties.URL_METRIC_OFF,
                    agent);
            if ((!prop.equals("")) && !prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(BrowseAgentProperties.URL_METRIC_OFF,
                    "false", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.BROWSER_LOGGING_ENABLED, agent);
            if ((!prop.equals("")) || !prop.contains("true")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.BROWSER_LOGGING_ENABLED, "true", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.GEO_LOCATION_ENABLED, agent);
            if ((!prop.equals("")) || !prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.GEO_LOCATION_ENABLED, "false", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.CONTENT_ENCODING_ENABLED, agent);
            if ((!prop.equals("")) && !prop.contains("false")) {
                CommonUtils.updateIntroscopeAgentProfile(
                    BrowseAgentProperties.CONTENT_ENCODING_ENABLED, "false", agent);
                agentHotPropWait = true;
            }

            prop =
                CommonUtils.getIntroscopeAgentProfileProperty(
                    BrowseAgentProperties.INSTRUMENT_CLASS_AUTOSKIP_DEPTH, agent);
            if ((!prop.equals("")) && !prop.contains("1")) {
                CommonUtils.updateIntroscopeAgentProfileForPropertyDelete(
                    BrowseAgentProperties.INSTRUMENT_CLASS_AUTOSKIP_DEPTH, agent);
                agentHotPropWait = true;
            }

            if (!CommonUtils.doesBSExist(Test_BS_BT.TEST_BUSINESS_SERVICE, em)) {
                CommonUtils.setupBizDef(Test_BS_BT.TEST_BUSINESS_SERVICE,
                    Test_BS_BT.TEST_BUSINESS_TRANSACTION,
                    Test_BS_BT.TEST_BUSINESS_TRANSACTION_COMPONENT,
                    Test_BS_BT.TEST_BUSINESS_COMPONENT, "URL", "Port", "", "matches",
                    agent.getApplicationServerPort(), em);
                LOGGER.info("Created Business Definitions - reset props");
            }

            if (resetExtensionFile == true) {
                // If a test modified JS extension file, set agent restart flag
                LOGGER.info("resetprops: resetExtensionFile = true");
                agentRestart = true;
            }

            if (agentRestart) {
                LOGGER.info("********** Restarting app server ");
                // Call restartAppServer with flag=true for reverting to original JSExtension File
                restartAppServer(true);
                // Set agentRestart flag to false
                agentRestart = false;
            } else if (agentHotPropWait) {
                LOGGER.info("********** Wait for hot properties to load ");
                CommonUtils.sleep(75000);
            }
            LOGGER.info("Finished resetting props");
        } catch (Exception e) {
            LOGGER.info("Exception resetting props:");
            e.printStackTrace();
        }
        LOGGER.info("END of resetProps");
    }


    @AfterTest(alwaysRun = true)
    protected void afterTestCleanUp() {
        // If a test modified JS extension file, set agent restart flag
        if (resetExtensionFile == true) {
            LOGGER.info("afterTestCleanUp: resetExtensionFile = true. Revert File");
            // Skipping restart here to avoid multiple restarts
            agentRestart = true;
        }
    }



    // resetExtFile flag = true to revert ExtJSfile, false to restart without reverting
    // TODO: Create a separate restartAppServerWithJSExtensionRevert for use in JS Extension tests
    public void restartAppServer(boolean resetExtFile) {
        LOGGER.info("Restarting app server");

        if (agent.getApplicationServer().equals("TOMCAT")) {
            // Stop tomcat
            runSerializedCommandFlowFromRole(BrowserAgentBaseTestbed.TOMCAT_ROLE_ID,
                TomcatRole.ENV_TOMCAT_STOP);
            // If resetExtFile is true, revert the file to original version after stopping agent
            if (resetExtFile == true) {
                CommonUtils.revertJSExtensionFile(agent);
            }
            // Set the flag to false after file is successfully reverted
            resetExtensionFile = false;
            // Start tomcat
            runSerializedCommandFlowFromRole(BrowserAgentBaseTestbed.TOMCAT_ROLE_ID,
                TomcatRole.ENV_TOMCAT_START);
        } else {
            // TODO: Code for other Application servers
            LOGGER.info("Application servers other than Tomcat not supported right now");
        }
        // TODO: Find out if we still need to allow time for after Appserver start up.
        CommonUtils.sleep(30000);
    }

    @AfterSuite(alwaysRun = true, groups = {"*"})
    // TODO: Applicable Groups
    protected void testSuiteTeardown() {
        // TODO: Implement
    }

}
