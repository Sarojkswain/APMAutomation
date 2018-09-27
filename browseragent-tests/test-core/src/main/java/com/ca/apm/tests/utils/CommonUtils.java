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

package com.ca.apm.tests.utils;

/**
 * Common Utilities class for BrowserAgent Automation
 *
 * @author Legacy Browser Agent automation code
 *         Updates - gupra04
 * 
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;

import com.ca.apm.tests.test.BrowserAgentBaseTest;
import com.ca.apm.tests.utils.constants.BusinessServiceConstants.Test_BS_BT;
import com.ca.apm.tests.utils.constants.JSExtensionConstants.JSExtensionBackUpFile;
import com.ca.wily.cem.qa.api.AgentFilterDefinition;
import com.ca.wily.cem.qa.api.BusinessProcessDefinition;
import com.ca.wily.cem.qa.common.AdminAgentFilterHelper;
import com.ca.wily.cem.qa.common.AdminBusinessServiceHelper;
import com.ca.wily.cem.qa.common.AdminBusinessTransactionHelper;
import com.ca.wily.cem.qa.common.AdminComponentHelper;
import com.ca.wily.cem.qa.common.AdminParameterHelper;
import com.ca.wily.cem.qa.common.AdminTransactionHelper;
import com.ca.wily.cem.qa.common.CEMServices;
import com.ca.wily.cem.qa.common.SetupMonitorHelper;

public class CommonUtils {

    private static CEMServices cemServices;
    private static AdminBusinessTransactionHelper bsTransactionHelper;
    private static AdminBusinessServiceHelper bsHelper;
    private static AdminTransactionHelper transactionHelper;
    private static AdminComponentHelper componentHelper;
    private static AdminParameterHelper parameterHelper;
    private static SetupMonitorHelper setupMonitorHelper;
    private static AdminAgentFilterHelper agentFilterHelper;
    private static boolean cemLoggedIn = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * 
     * Return the name of the routine that called getCurrentMethodName
     *
     */
    public static String getCurrentMethodName() {
        String t = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);
            (new Throwable()).printStackTrace(pw);
            pw.flush();
            String stackTrace = baos.toString();
            pw.close();
            StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
            String l = tok.nextToken(); // 'java.lang.Throwable'
            l = tok.nextToken(); // 'at ...getCurrentMethodName'
            l = tok.nextToken(); // 'at ...<caller to getCurrentRoutine>'
            // Parse line 3
            tok = new StringTokenizer(l.trim(), " <(");
            t = tok.nextToken(); // 'at'
            t = tok.nextToken(); // '...<caller to getCurrentRoutine>'
        } catch (Exception e) {
            // TODO: handle exception
        }
        return t;
    }

    public static void updateIntroscopeAgentProfile(String key, String value, AgentDetails agent) {
        FileUtils.changeRemotePropertiesFile(agent.getAgentHost(), agent.getAgentHostUsername(),
            agent.getAgentHostPassword(), key, value, agent.getAgentProfileFileFullPath());
    }

    public static void updateIntroscopeAgentProfileForPropertyDelete(String key, AgentDetails agent) {
        FileUtils.deletePropertyFromRemotePropertiesFile(agent.getAgentHost(),
            agent.getAgentHostUsername(), agent.getAgentHostPassword(), key,
            agent.getAgentProfileFileFullPath());
    }

    public static void sleep(long millis) {
        LOGGER.info("Sleeping for " + millis / 1000 + " secs..");
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        counter(millis, "sleeptime");
    }

    public static String getIntroscopeAgentProfileProperty(String key, AgentDetails agent) {

        return FileUtils.getPropertyFromRemotePropertiesFile(agent.getAgentHost(),
            agent.getAgentHostUsername(), agent.getAgentHostPassword(), key,
            agent.getAgentProfileFileFullPath());

    }

    // Use this option instead of launchSinglePageNoRefresh when you want to invoke the page
    // multiple times - Used by most migrated tests
    public static void launchSinglePageWithRefresh(String pageUrl, SeleniumDetails seleniumData) {
        LOGGER.info("Accessing URL:" + pageUrl);
        seleniumData.getDriver().get(pageUrl);
        clearCache(seleniumData);
        sleep(15000);
        LOGGER.info("Refreshing URL:" + pageUrl);
        seleniumData.getDriver().navigate().refresh();
        sleep(15000);
        LOGGER.info("Refreshing URL:" + pageUrl);
        seleniumData.getDriver().navigate().refresh();
        sleep(15000);
    }

    // Use this option instead of launchSinglePageWithRefresh when invoking the page once is
    // sufficient. This is preferred.
    public static void launchSinglePageNoRefresh(String pageUrl, SeleniumDetails seleniumData) {
        LOGGER.info("Accessing URL:" + pageUrl);
        seleniumData.getDriver().get(pageUrl);
        clearCache(seleniumData);
        sleep(45000);
    }

    /**
     * 
     * @param time
     * @param timeof Possible values: metrictime or sleeptime
     */
    public static void counter(long time, String timeof) {
        // counter(sw.getElapsedTimeSecs(),"metricTime");
        if (timeof.equals("metrictime")) BrowserAgentBaseTest.totalMetricTime += time;
        if (timeof.equals("sleeptime")) BrowserAgentBaseTest.totalSleepTime += time;
    }

    public static void customAssertTrue(boolean condition, String msg) {
        if (!condition) {
            LOGGER.info(":::TEST CASE FAILED::: " + msg);
        } else if (condition) {
            LOGGER.info(":::TEST CASE PASSED:::");
        }
        Assert.assertTrue(condition, msg);
    }

    public static void customAssertTrue(boolean condition) {
        if (!condition) {
            LOGGER.info(":::TEST CASE FAILED::: ");
        } else if (condition) {
            LOGGER.info(":::TEST CASE PASSED:::");
        }
        Assert.assertTrue(condition);
    }

    public static void customAssertFalse(boolean condition) {
        if (!condition) {
            LOGGER.info(":::TEST CASE PASSED:::");
        } else if (condition) {
            LOGGER.info(":::TEST CASE FAILED::: ");
        }
        Assert.assertFalse(condition);
    }

    public static void closeBrowser(SeleniumDetails testBrowser) {
        closeBrowserWithDelay(testBrowser, 0);
    }

    public static void closeBrowserWithDelay(SeleniumDetails testBrowser, long delay) {
        LOGGER.info("Start of closeBrowser delay: " + delay);
        try {
            LOGGER.info("Write console log to file and close driver");

            // If the driver is null, assume that closer browser was
            // already called. Kill what remains and return nothing more to do
            if (testBrowser.getDriver() == null) {
                killBrowser(testBrowser);
                return;
            }

            writeBrowserLogToFile(testBrowser);
            testBrowser.getDriver().close();

            // browser closure may send metrics, give some time before quit
            // so they can be sent. quit and kill are too forceful and aborts
            // anything ongoing
            if (delay > 0) {
                Thread.currentThread().sleep(delay);
            }

            testBrowser.getDriver().quit();
            killBrowser(testBrowser);
        } catch (Exception e) {
            killBrowser(testBrowser);
            LOGGER.error("closeBrowserWithDelay exception found", e);
        } finally {
            // Subsequent tests will reuse the details object. Condition for
            // creating a new driver is if its null or not. Unfortunately there is
            // various reasons as to why exceptions could be thrown while processing.
            // for example firefox appears to have issue calling quit() if close() was
            // previously called on the only open window. Exception or no exception
            // the most important thing to do is null out this driver.
            testBrowser.setDriver(null);
        }
        LOGGER.info("End of closeBrowser");
    }

    private static void killBrowser(SeleniumDetails testBrowser) {
        String browserProcessname = "";
        LOGGER.info("Browser to kill: " + testBrowser.getBrowser());
        if (testBrowser.getBrowser().contains("Firefox")) {
            browserProcessname = "firefox.exe";
        } else if (testBrowser.getBrowser().contains("InternetExplorer")
            || testBrowser.getBrowser().contains("*iexplore")) {
            browserProcessname = "iexplore.exe";
            FileUtils.invokeProcess("taskkill /f /im " + "IEDriverServer.exe");
        } else if (testBrowser.getBrowser().contains("Chrome")) {
            browserProcessname = "chrome.exe";
            FileUtils.invokeProcess("taskkill /f /im " + "chromedriver.exe");
        }
        FileUtils.invokeProcess("taskkill /f /im " + browserProcessname);
    }

    public static void resetBrowser(SeleniumDetails testBrowser) {
        LOGGER.info("Start of resetBrowser");
        if (testBrowser.getDriver() != null) {
            writeBrowserLogToFile(testBrowser);
            LOGGER.info("Close driver");
            testBrowser.getDriver().close();
            testBrowser.getDriver().quit();
            sleep(30000);
        }
        createWebDriver(testBrowser);
        LOGGER.info("End of resetBrowser");
    }

    public static void createWebDriver(SeleniumDetails seleniumData) {
        createWebDriver(seleniumData, 15000);
    }

    public static void createWebDriver(SeleniumDetails seleniumData, long sleep) {
        LOGGER.info("Creating Web Driver for browser: " + seleniumData.getBrowser()
            + " ################################################ ");
        if (seleniumData.getBrowser().contains("Firefox")) {
            LOGGER.info("Creating FirefoxDriver ");
            seleniumData.setDriver(new FirefoxDriver(seleniumData.getBrowserCapability()));
        } else if (seleniumData.getBrowser().contains("InternetExplorer")
            || seleniumData.getBrowser().contains("*iexplore")) {
            LOGGER.info("Creating InternetExplorerDriver ");
            seleniumData.setDriver(new InternetExplorerDriver(seleniumData.getBrowserCapability()));
        } else if (seleniumData.getBrowser().contains("Chrome")) {
            LOGGER.info("Creating ChromeDriver ");
            seleniumData.setDriver(new ChromeDriver(seleniumData.getBrowserCapability()));
        } else {
            throw new SkipException(
                "Please enter a valid browser name Firefox, InternetExplorer, Chrome");
        }
        sleep(sleep);
        clearCache(seleniumData);
        LOGGER.info("Web Driver created");
    }

    public static void clearCache(SeleniumDetails seleniumData) {
        // clear the browser cache
        LOGGER.info("Clearing browser page cache, or at least trying to");
        Actions actionObject = new Actions(seleniumData.getDriver());

        actionObject.keyDown(Keys.CONTROL).sendKeys(Keys.F5).keyUp(Keys.CONTROL).perform();
        sleep(5000);
        LOGGER.info("Finished clearing browser cache");
    }

    public static String getBrowserName(SeleniumDetails seleniumData) {
        /*
         * String browser_version = null;
         * Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
         * String browsername = cap.getBrowserName();
         * // This block to find out IE Version number
         * if ("internet explorer".equalsIgnoreCase(browsername)) {
         * browsername = "IE";
         * } else if ("firefox".equalsIgnoreCase(browsername)) {
         * browsername = "Firefox";
         * } else if ("chrome".equalsIgnoreCase(browsername)) {
         * browsername = "Chrome";
         * }
         */
        String browsername = seleniumData.getBrowser();
        if ("internet explorer".equalsIgnoreCase(browsername))
            browsername = "IE";
        else if ("firefox".equalsIgnoreCase(browsername))
            browsername = "Firefox";
        else if ("chrome".equalsIgnoreCase(browsername)) browsername = "Chrome";
        return browsername;
    }

    public static String getBrowserVersion(SeleniumDetails seleniumData) {
        String browser_version = null;
        // Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
        Capabilities cap = ((RemoteWebDriver) seleniumData.getDriver()).getCapabilities();
        browser_version = cap.getVersion();// .split(".")[0];
        if (browser_version.contains(".")) {
            browser_version = browser_version.substring(0, browser_version.indexOf("."));
        }
        return browser_version;
    }

    public static void writeBrowserLogToFile(SeleniumDetails seleniumData) {
        LOGGER.info("Start of writeBrowserLogToFile for browser: " + seleniumData.getBrowser()
            + " ################################################ ");
        try {
            if (seleniumData.getBrowser().contains("Chrome")) {
                FileWriter fw;
                fw = new FileWriter(seleniumData.getBrowserLogFile(), true);
                LOGGER.info("get log entries");
                LogEntries logEntries =
                    seleniumData.getDriver().manage().logs().get(LogType.BROWSER);
                LOGGER.info("logentries=" + logEntries);
                for (LogEntry entry : logEntries) {
                    fw.write(entry.getMessage() + "\n");
                }
                fw.close();
            } else if (seleniumData.getBrowser().contains("Firefox")) {
                FileWriter fw;
                fw = new FileWriter(seleniumData.getBrowserLogFile(), true);
                LOGGER.info("get log entries");
                LogEntries logEntries =
                    seleniumData.getDriver().manage().logs().get(LogType.BROWSER);
                LOGGER.info("logentries=" + logEntries);
                for (LogEntry entry : logEntries) {
                    fw.write(entry.getMessage() + "\n");
                }
            } else {
                // TODO: Implement writing to log file for IE
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info("End of writeBrowserLogToFile");
    }

    private static void initCEMHelpers(EmDetails em) throws UnknownHostException {

        cemServices =
            new CEMServices(Inet4Address.getByName(em.getEmHost()).getHostAddress(), ""
                + em.getCemPort(), em.getEmUsername(), em.getEmPassword());
        Assert.assertTrue(cemServices.login(),
            "CEM Login failed. Check em port/host & user credentials are correct.");
        bsTransactionHelper = new AdminBusinessTransactionHelper(cemServices);
        bsHelper = new AdminBusinessServiceHelper(cemServices);
        transactionHelper = new AdminTransactionHelper(cemServices);
        componentHelper = new AdminComponentHelper(cemServices);
        parameterHelper = new AdminParameterHelper(cemServices);
        setupMonitorHelper = new SetupMonitorHelper(cemServices);
        agentFilterHelper = new AdminAgentFilterHelper(cemServices);
        cemLoggedIn = true;
    }

    public static void setupBizDef(String bsName, String btName, String txnName, String compName,
        String paramType, String paramName, String paramNameType, String paramAction,
        String paramPattern, EmDetails em) throws Exception {

        // init helpers & login to cem
        if (!cemLoggedIn) initCEMHelpers(em);
        // create BS
        bsHelper.createBusinessServiceWithInheritedSLAValues(bsName, bsName, "Default Application");
        // create and enable BT
        bsTransactionHelper.createBusinessTransactionByBusinesServiceName(bsName, btName, btName,
            true, true, true, true, true, true, "", "", "", "", "300");
        // create txn, component
        // action should be one of the following - matches, not_exist, expression
        transactionHelper.createIdentifyingTransactionWithInheritedSLAValues(bsName, btName,
            txnName, txnName);
        componentHelper.createIdentifyingComponent(bsName, btName, txnName, compName, compName,
            true, true, true);
        // create or update parameter
        parameterHelper.createRequestParameter(bsName, btName, txnName, compName, paramType,
            paramName, paramNameType, paramAction, paramPattern);

        // enable business transaction
        bsTransactionHelper.enableBusinessTxMonitoringUsingName(bsName, btName);

        // add filter
        // example 1: agentFilterType,1,agentFilterValue,SuperDomain
        // example 2: agentFilterType,2,agentFilterValue,(.*)AgentName(.*)
        // agentFilterHelper.addAgentFilter(bsName, 2, BRTMAgentName);

        // sync monitor
        setupMonitorHelper.syncMonitors();
    }

    public static void deleteBizDef(EmDetails em) throws Exception {

        if (!cemLoggedIn) {
            initCEMHelpers(em);
            // System.out.println("Not logged in to CEM..skipping delete bizdef task.");
            // return;
        }

        // export all business txns
        BusinessProcessDefinition[] allBusinessSvs = bsHelper.getAllBusinessProcessDefinitions();
        for (BusinessProcessDefinition bs : allBusinessSvs) {
            if (bs.getName().equalsIgnoreCase("Discovered Transactions")) continue;
            LOGGER.info("########## Business Process: " + bs.getName() + " Variable: "
                + Test_BS_BT.TEST_BUSINESS_SERVICE);
            // delete business service
            if (bs.getName().equals(Test_BS_BT.TEST_BUSINESS_SERVICE)) {
                LOGGER.info("######### Delete Business Process: " + bs.getName() + " Variable: "
                    + Test_BS_BT.TEST_BUSINESS_SERVICE);
                bsHelper.deleteBusinessService(bs.getName());
            }
        }

        // delete agent filters
        AgentFilterDefinition[] agentFilters = agentFilterHelper.getAgentFilters();
        for (AgentFilterDefinition agentFilter : agentFilters) {
            agentFilterHelper.deleteAgentFilterById(agentFilter.getId());
        }

        setupMonitorHelper.syncMonitors();
    }

    public static boolean doesBSExist(String bsName, EmDetails em) throws Exception {

        if (!cemLoggedIn) {
            initCEMHelpers(em);
        }
        boolean result = false;
        // export all business txns
        BusinessProcessDefinition[] allBusinessSvs = bsHelper.getAllBusinessProcessDefinitions();
        for (BusinessProcessDefinition bs : allBusinessSvs) {
            if (bs.getName().contains(bsName)) result = true;
        }
        return result;
    }

    public static void revertJSExtensionFile(AgentDetails agent) {
        try {
            if (new File(agent.getAgentJsExtensionFileLocation() + JSExtensionBackUpFile.BACKUP)
                .exists()) {
                LOGGER
                    .info("revertJSExtensionFile: Reverting JS Extension file to original version.");
                Files.copy(
                    Paths.get(agent.getAgentJsExtensionFileLocation()
                        + JSExtensionBackUpFile.BACKUP),
                    Paths.get(agent.getAgentJsExtensionFileLocation()),
                    StandardCopyOption.REPLACE_EXISTING);
                BrowserAgentBaseTest.agentRestart = true;
                BrowserAgentBaseTest.resetExtensionFile = false;
            } else {
                LOGGER
                    .info("revertJSExtensionFile: Unable to revert JS Extension file to original version");
            }
        } catch (Exception e) {
            LOGGER
                .info("revertJSExtensionFile : Something went wrong. Unable to revert JS extension file "
                    + agent.getAgentJsExtensionFileLocation() + " to original version");
            e.printStackTrace();
        }
    }

    /**
     * Look for a specified message in the passed in log file
     * after the passed in time. Note, this is meant to be used
     * with the IntroscopeAgent.log file, but it will work
     * for any log file that has the same logging/timestamp format
     * as the IntroscopeAgent.log file.
     *
     * @param expectedMsg - String [] - String array allows for eacy check for multiple matches
     *        (Does not use regex
     * @param fileName - Absolute path to the file to be used
     * @param startTime - Date after which we will start looking for the message
     * @return testStatus - true if the message was found after the passed in date, false otherwise.
     * @throws FileNotFoundException - if file does not exist.
     * @throws IOException
     */
    public static boolean checkForMultipleMsgs(String[] expectedMsg, String fileName, Date startTime)
        throws IOException, FileNotFoundException {

        boolean testStatus = false;
        try (FileInputStream fis = new FileInputStream(fileName);
            Scanner scanner = new Scanner(fis);) {
            // A line in the Agent log looks like:
            // 4/20/16 03:03:08 PM PDT [INFO] [IntroscopeAgent.BrowserAgent] Using built-in
            // JavaScript extension file.
            // We want to compare the passed in date with a date on a log line.
            SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yy hh:mm:ss a z");
            while (scanner.hasNextLine()) {
                String aLine = scanner.nextLine();
                // Extract timestamp from current log message
                String lineDate = aLine.split("\\[")[0].trim();
                Date theLogDate;
                try {
                    theLogDate = dtf.parse(lineDate);
                } catch (ParseException pe) {
                    // the line did not start with a timestamp,
                    // so we just want to skip the line
                    // No Error logged. No action needed
                    continue;
                }
                // Log message time stamp is older than provided start time stamp, skip the line
                if (theLogDate.compareTo(startTime) <= 0) {
                    continue;
                }
                // Iterate through expectedMsg String array to validate all messages specified in
                // the array are contained in the log message
                for (String msg : expectedMsg) {
                    // Log message should have all messages in expectedMsg for test to pass
                    if (aLine.contains(msg)) {
                        LOGGER.info("checkForMultipleMsgs - Log message: " + aLine
                            + " - contains - " + msg);
                        testStatus = true;
                    } else {
                        if (testStatus) {
                            LOGGER.info("checkForMultipleMsgs - Log message: " + aLine
                                + " - does not contains - " + msg + " setting testStatus to false");
                        }
                        testStatus = false;
                    }

                }
                // If all messages were found, return status True - Else continue parsing log file.
                if (testStatus == true) {
                    return testStatus;
                }
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            LOGGER.info("checkForMultipleMsgs - Error validating message exists in log file "
                + fileName);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.info("checkForMultipleMsgs - IO Error on checking for message");
            e.printStackTrace();
        }
        return testStatus;
    }

    /**
     * Look for a specified message in the passed in log file
     * after the passed in time. Note, this is meant to be used
     * with the IntroscopeAgent.log file, but it will work
     * for any log file that has the same logging/timestamp format
     * as the IntroscopeAgent.log file.
     *
     * @param expectedMsg regex to be matched in the specified file
     * @param fileName Absolute path to the file to be checked
     * @param startTime Date after which we will start looking for the message
     * @return true if the message was found after the passed in date
     *         or false otherwise.
     * @throws FileNotFoundException if the passed in file does not exist.
     */
    // TODO: As tests using this common utility are migrated, check if we can replace utility with
    // checkForMultipleMsgs
    public static boolean checkForMsg(String expectedMsg, String fileName, Date startTime)
        throws IOException, FileNotFoundException {

        try (FileInputStream fis = new FileInputStream(fileName);
            Scanner scanner = new Scanner(fis);) {

            // A line in the Agent log looks like:
            //
            // 4/08/15 06:01:45 PM EDT [DEBUG] [IntroscopeAgent.IntelligentInstrumentationService]
            // Setting auto tracing triggers.
            //
            // We want to compare the passed in date with a date on a log line.
            SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yy hh:mm:ss a z");

            while (scanner.hasNextLine()) {
                String aLine = scanner.nextLine();
                // pull what should be the timestamp off of the line
                String lineDate = aLine.split("\\[")[0].trim();
                Date theLogDate;
                try {
                    theLogDate = dtf.parse(lineDate);
                } catch (ParseException pe) {
                    // the line did not start with a timestamp,
                    // so we just want to skip the line
                    LOGGER.info("checkForMsg - Error on checking for message");
                    pe.printStackTrace();
                    continue;
                }
                // if the timestamp of the log line is prior to the passed in start time, skip the
                // line
                if (theLogDate.compareTo(startTime) <= 0) {
                    continue;
                }
                if (aLine.matches(expectedMsg)) {
                    LOGGER.info("\nFound message: \n\t" + aLine);
                    scanner.close();
                    return true;
                }
            }
            scanner.close();

        } catch (IOException e) {
            LOGGER.info("checkForMsg - Error on checking for message");
            e.printStackTrace();
        }
        return false;
    }
}
