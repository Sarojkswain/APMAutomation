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

package com.ca.apm.tests.test.selenium;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.ca.apm.browseragent.testsupport.collector.BATestCollector;
import com.ca.apm.browseragent.testsupport.collector.pojo.Attributes;
import com.ca.apm.browseragent.testsupport.collector.pojo.Configuration;
import com.ca.apm.browseragent.testsupport.collector.util.BATestCollectorUtils;
import com.ca.apm.tests.utils.SeleniumDetails;
import com.ca.apm.tests.utils.constants.TestAppUrlConstants.BrtmTestApp;

/***
 * This base class helpers broker the selenium driver when switching between local and tas
 * runs. Users should extend from this class, then call their tests from BANoAgentTestRunnerSuite
 * For local runs of your child classes right click -> Run as -> Run Configurations ...
 * then from arguments tab -> vm arguments enter something like:
 * -DbaCollectorWorkingDir=C:/MyCollectorWorkingDir
 * -DbaTestAppUrl=http://130.200.65.88:8181/brtmtestapp
 * -DchromeDriver=C:/Users/brido02/AppData/Roaming/npm/node_modules/webdriver-manager/selenium/
 * chromedriver.exe
 * 
 * Please note you should set to your own driver above
 * 
 */

public class SeleniumBase {
    private static final Logger LOGGER = Logger.getLogger(SeleniumBase.class);

    private SeleniumDetails seleniumData = null;
    private String testAppUrl = null;
    private String collectorWorkingDir = null;

    private WebDriver webDriver = null;


    private static final String BA_EXT_DIR_BACKUP_DIR = "backedupExtensionFiles";

    /**
     * The following are properties a test creator can pass in to the VM arguments to run
     * a test locally. full example:
     * 
     * -DbaTestAppEditDir=C:/apm/BrowserAgent/apache-tomcat-8.0.33/webapps/brtmtestapp/
     * -DbaCollectorWorkingDir=C:/MyCollectorWorkingDir
     * -DbaTestAppUrl=http://brido02-win7:8181/brtmtestapp
     * -DchromeDriver=C:/webdriver-manager/selenium/chromedriver.exe
     */

    // (optional) The local directory location where the test files live for snippet edit
    // If not specified the user must manually edit the pages that the test will use.
    // example: -DbaTestAppEditDir=C:/apm/BrowserAgent/apache-tomcat-8.0.33/webapps/brtmtestapp/
    private static final String BA_TEST_LOCAL_TEST_APP_DIRECTORY = "baTestAppEditDir";

    // The working directory on your local machine, must exist (create prior to test)
    // example: -DbaCollectorWorkingDir=C:/MyCollectorWorkingDir
    private static final String BA_TEST_COLLECTOR_WORKING_DR = "baCollectorWorkingDir";

    // The running tomcat (or whatever app server) runnning the brtm test app (start prior to test)
    // example: -DbaTestAppUrl=http://BRIDO02-WIN7:8181/brtmtestapp
    private static final String BA_TEST_APP_URL = "baTestAppUrl";

    // the location of the chrome driver
    // example: -DchromeDriver=C:/webdriver-manager/selenium/chromedriver.exe
    private static final String CHROME_DRIVER = "chromeDriver";



    /**
     * The collector used during each test, initialized in beforeTest method
     */

    protected BATestCollector baTestCollector = null;

    /**
     * The reverted default configuration, initialized in beforeTest method
     */

    protected Configuration collectorConfig = null;


    private volatile static String snippetInformation = null;


    /**
     * The value that is set can vary depending on where the test is running tas vs local
     */
    private String testAppDirectory = null;


    /**
     * This constructor is used by local runs, testng test via eclipse
     */

    public SeleniumBase() {
        this(null, System.getProperty(BA_TEST_APP_URL), System
            .getProperty(BA_TEST_COLLECTOR_WORKING_DR));


        // Local runs will modify the local test app files if the property is already specified
        //
        String localTestAppDirectory = System.getProperty(BA_TEST_LOCAL_TEST_APP_DIRECTORY);

        LOGGER.info(BA_TEST_LOCAL_TEST_APP_DIRECTORY + " found to be " + localTestAppDirectory);

        if (localTestAppDirectory != null && localTestAppDirectory.length() > 0) {

            setTestAppDirectoryPath(localTestAppDirectory);

            String[] newPages = getTestPages();

            for (String page : newPages) {
                String fullPagePath = localTestAppDirectory + File.separator + page;
                try {
                    SeleniumBase.insertSnippetIntoPage(fullPagePath);
                } catch (Exception e) {
                    LOGGER.error("Failed to modify page: " + fullPagePath, e);
                }
            }
        }

    }

    /**
     * This constructor is used by the test runner
     * 
     * @param details
     * @param testAppUrl
     * @param collectorWorkingDir
     */

    public SeleniumBase(SeleniumDetails details, String testAppUrl, String collectorWorkingDir) {
        this.testAppUrl = testAppUrl;
        this.collectorWorkingDir = collectorWorkingDir;

        // Assume local run
        if (details == null) {
            details = createLocalDetails();
        }

        setSeleniumDetails(details);
    }

    /**
     * Things to run before each test method.
     * 
     * @throws Exception
     */

    @BeforeMethod
    public void beforeMethod(Method method, ITestContext testContext) throws Exception {
        // Cleanup any files that may exists before any tests. This will likely be
        // user related files since this is also run on test end
        performExtBackup();

        baTestCollector = new BATestCollector(getCollectorWorkingDir(), method.getName());
        collectorConfig = baTestCollector.revertToDefaultConfiguration();
    }

    /**
     * Things to run after each test method
     */

    @AfterMethod
    public void afterMethod(Method method, ITestContext testContext, ITestResult testResult) {
        try {
            // Cleanup any files that may exists after the test
            performExtBackup();

            baTestCollector.stopServer();
            closeDriver();

        } catch (Exception e) {
            LOGGER.error("Exception in afterMethod could be just benign", e);
        }
    }

    /**
     * Helper for beforeMethod and afterMethod methods to cleanup any extension files
     * 
     * @throws Exception
     */

    private void performExtBackup() throws Exception {
        // First create the backup folder if not exists
        String backupDirString = collectorWorkingDir + File.separator + BA_EXT_DIR_BACKUP_DIR;
        File backupDir = new File(backupDirString);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        // Clean up the BAExt after each test, move to backup folder
        File extFile =
            new File(collectorWorkingDir + File.separator + BATestCollectorUtils.BIG_FILE_EXT);
        if (extFile.exists()) {
            File backupFile =
                new File(backupDirString + File.separator + BATestCollectorUtils.BIG_FILE_EXT + "."
                    + System.currentTimeMillis());

            com.google.common.io.Files.copy(extFile, backupFile);

            // since backedup , delete..
            extFile.delete();
            // if we decide to revert back to factory use call:
            // BATestCollectorUtils.revertExtensionToOriginal
        }
    }

    /**
     * This is a helper method for the constructor that is used for local runs.
     * 
     * @return SeleniumDetails created locally
     */

    private SeleniumDetails createLocalDetails() {
        SeleniumDetails details = null;
        String chromeDriverPath = System.getProperty(CHROME_DRIVER);

        String browser = null;

        // If the local run has passed a value for Chrome driver then use that.
        // otherwise this will attempt to use firefox
        if (chromeDriverPath != null) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            browser = "Chrome";
        } else {
            browser = "Firefox";
        }

        if (browser != null) {
            details =
                new SeleniumDetails.Builder().browser(browser).seleniumUrl(getTestAppURL()).build();
            String logFileName = browser + "Console.log";
            details.setBrowserLogFile(getCollectorWorkingDir() + File.separator + logFileName);
        }

        return details;
    }


    /**
     * This was abstract, instead implementers can just add an entry directly to this class.
     * The tas framework will use this returned list to insert the snippet. It is done this way
     * so tas doesnt insert snippet into files that may not be compliant, having head tag, etc.
     * 
     * Note: the returned pages contain paths assuming brtmtestapp is the base directory
     * so to return something like
     * C:/apache-tomcat-8.0.33/webapps/brtmtestapp/jserrors/error_MultipleErrors.jsp
     * You would just return: /jserrors/error_MultipleErrors.jsp
     * 
     * @return String[] array of file names part of the brtmtestapp test pages.
     */

    public String[] getTestPages() {
        return new String[] {
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.GET_LOCAL_DOMAIN_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.GET_LOCAL_DOMAIN_2_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.ERROR_MULTI_ERROR_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.GET_CORS_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.GET_LOCAL_QUERY_PARAMS_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.AJAX_CLAMP_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.INDEX_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.GEO_LOCATION_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.JS_FUNCTION_RETRY),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.SPA_INDEX),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.JQUERY_1_X_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.JQUERY_2_X_PAGE),
                BrtmTestApp.getPagePathInFileSystemFormat(BrtmTestApp.JQUERY_3_X_PAGE)
        };
    }



    public void setSeleniumDetails(SeleniumDetails details) {
        this.seleniumData = details;
    }

    public void setTestAppDirectoryPath(String directoryPath) {
        this.testAppDirectory = directoryPath;
    }

    public String getTestAppDirectoryPath() {
        return testAppDirectory;
    }

    /**
     * Helper method to return driver based on either tas or local run
     * 
     * @return WebDriver
     */

    public WebDriver getDriver() {
        WebDriver returnDriver = null;

        if (this.seleniumData != null) {
            returnDriver = seleniumData.getDriver();
            if (returnDriver == null) {
                com.ca.apm.tests.utils.CommonUtils.createWebDriver(this.seleniumData, 5000);
                returnDriver = seleniumData.getDriver();
            }
        } else {
            LOGGER.error("getDriver seleniumData was null");
        }

        return returnDriver;
    }

    /**
     * Helper method to close the driver either based on tas or local run
     * 
     */

    public void closeDriver() {
        closeDriverWithDelay(0);
    }

    /**
     * Some test cases may have to wait for metrics on browser closure.
     * The delay passed into closeBrowser is between closing the window and
     * complete exiting/process killing of the driver.
     * 
     * @param delay time in milliseconds
     */

    public void closeDriverWithDelay(long delay) {
        if (seleniumData != null) {
            com.ca.apm.tests.utils.CommonUtils.closeBrowserWithDelay(seleniumData, delay);
        } else {
            LOGGER.error("closeDriver seleniumData was null");
        }
    }

    /**
     * Test app location
     * 
     * @return String the url of the brtmtestapp page
     */

    public String getTestAppURL() {
        return testAppUrl;
    }

    /**
     * Working directory of the collectors that are created for the test
     * 
     * @return String working directory
     */

    public String getCollectorWorkingDir() {
        return collectorWorkingDir;
    }


    /**
     * Helper which opens page with built in sleep to ensure page is loaded before moving on
     * This is useful for cases in which the collector wont be used to wait for the
     * initial load.
     * 
     * @param url
     */


    public WebDriver openPage(String url) {
        WebDriver driverUsed = null;
        try {
            driverUsed = getDriver();
            driverUsed.get(url);
            LOGGER.info("Loading page: " + url);
            // Page loading is eating into this, add a little more
            // on top of the metric frequency
            sleepMetricFrequency(3000);
        } catch (Exception e) {
            LOGGER.fatal("unable to open page", e);
        }
        return driverUsed;
    }

    /**
     * In cases where the user wants to throw away the batch of metrics
     */

    public void sleepMetricFrequency() {
        // Create a little additional to avoid edge case
        sleepMetricFrequency(1000);
    }

    /**
     * In cases where the user wants to throw away the batch of metrics
     * 
     * @param extra time in ms on top of the current metric frequency
     */

    public void sleepMetricFrequency(long extra) {
        try {
            Attributes attrs = collectorConfig.getBaAttributes();
            Thread.currentThread().sleep(attrs.getMetricFrequency() + extra);
        } catch (Exception e) {
            LOGGER.fatal("unable to sleepFrequency", e);
        }
    }

    /**
     * Attempts to execute the passed script in the browser console
     * 
     * @param script
     * @return Object the results, null if unable to execute
     * @throws Exception
     */

    public Object executeInBrowserConsole(String script) throws Exception {
        WebDriver webDriver = getDriver();
        Object returnObject = null;

        if (webDriver instanceof JavascriptExecutor) {
            returnObject = ((JavascriptExecutor) webDriver).executeScript("return " + script);

            Thread.currentThread().sleep(1000);
        }

        return returnObject;
    }

    /**
     * Helper to find a message in the browser side log file.
     * WARNING: Only checks for Chrome browser types. Returns true for all others
     * 
     * @param message
     * @return
     */

    public boolean checkBrowserLogForMessage(String message) {
        String browser = seleniumData.getBrowser();

        browser = browser != null ? browser.toLowerCase() : "";
        // This will require further investigation why we cant get the logs on firefox
        // For now checking will only occur on chrome
        if (!browser.contains("chrome")) {
            return true;
        }


        WebDriver webDriver = getDriver();

        boolean foundMessage = false;

        // Someone might look at this code and say "OMG this is a big hack!",
        // that someone would be right! allow me to explain...
        // it appears that not all log entries are present when the call to
        // manage().logs().get is made. There doesnt appear to be a buffer issue,
        // but rather the browser or seleium has determined there was no change to
        // the log, when there in fact was. By executing a script this appears to
        // "trick" selenium into thinking there was change...
        //
        try {
            executeInBrowserConsole("console.log('');");
        } catch (Exception e) {
            LOGGER.error("Workaround for checkBrowserLogForMessage failed with exception", e);
        }

        LogEntries logEntries = webDriver.manage().logs().get(LogType.BROWSER);

        for (LogEntry entry : logEntries) {
            LOGGER.debug(entry.toString());
            if (entry.toString().contains(message)) {
                foundMessage = true;
                break;
            }
        }
        return foundMessage;
    }

    /**
     * Checks if the current browser where the selenium driver is running is chrome or not
     * 
     * @return true if Chrome otherwise false
     */
    public boolean isChrome() {
        String browser = seleniumData.getBrowser();
        if (browser != null) {
            browser = browser.toLowerCase();
        }
        if (browser.contains("chrome")) {
            LOGGER.debug("browser-string: " + browser);
            return true;
        }
        return false;
    }

    /**
     * Checks if the current browser where the selenium driver is running is Firefox or not
     * 
     * @return true if Firefox otherwise false
     */

    public boolean isFireFox() {
        String browser = seleniumData.getBrowser();
        if (browser != null) {
            browser = browser.toLowerCase();
        }
        if (browser.contains("firefox")) {
            LOGGER.debug("browser-string: " + browser);
            return true;
        }
        return false;
    }

    public String invertTestAppURL() throws Exception {
        String origUrl = getTestAppURL();

        String[] hostAndPort = EUMValidationUtils.getHostPortPage(origUrl);

        String host = hostAndPort[0];

        String newHost = null;
        // this is IP format i.e. 130.200.65.218

        if (Character.isDigit(host.charAt(0))) {
            byte[] b = new byte[4];
            String[] bytes = host.split("[.]");

            for (int i = 0; i < bytes.length; i++) {
                b[i] = new Integer(bytes[i]).byteValue();
            }

            // get Internet Address of this host address
            InetAddress inetAddress = InetAddress.getByAddress(b);
            newHost = inetAddress.getHostName();
        } else {// is by name brido2-win7

            InetAddress inetAddress = InetAddress.getByName(host);
            newHost = inetAddress.getHostAddress();
        }

        LOGGER.debug("host: " + host + " newHost: " + newHost);

        // Now reconstruct
        String protocolSuffix = "://";

        int indexStartHost = origUrl.indexOf(protocolSuffix) + protocolSuffix.length();
        String prefix = origUrl.substring(0, indexStartHost);

        LOGGER.debug("prefix: " + prefix);

        int indexEndHost = origUrl.lastIndexOf(":");
        String suffix = origUrl.substring(indexEndHost, origUrl.length());
        LOGGER.debug("suffix: " + suffix);

        String newUrl = prefix + newHost + suffix;

        LOGGER.debug("newUrl: " + newUrl);

        return newUrl;
    }

    /**
     * This method will take the passed in file name and insert the snippet into the page
     * 
     * @param fullPathFileName
     */

    public static void insertSnippetIntoPage(String fullPathFileName) throws Exception {
        BATestCollectorUtils.insertSnippetIntoPage(fullPathFileName);
    }

    /**
     * WARNING: Calls here will overwrite any existing BAExt.js file you have
     * in the collector working directory. They will be moved to a backup folder inside
     * the collector working dir
     * 
     * Takes a resource from:
     * testing\test-projects\browseragent-tests\test-core\src\main\resources\jstestfiles
     * and deploys as proper extension file. For example, ALM test 455003 may have test
     * resource BAExt-455003.js, this file will be extract and copied to BAExt.js
     * into the collector working directory.
     * 
     * @param extensionResource resource name you placed in jstestfiles i.e. BAExt-455003.js
     */

    public void deployExtensionFile(String extensionResource) throws Exception {
        String resourcePath = "/jstestfiles/" + extensionResource;

        String extDest = collectorWorkingDir + File.separator + BATestCollectorUtils.BIG_FILE_EXT;
        BATestCollectorUtils.extractJarResourceTo(resourcePath, extDest);
    }

    /**
     * This helper is used to fail a test providing informative information about the
     * last stack trace element
     * 
     * @param e the exception that caused the test to fail
     */

    public void assertFail(Exception e) {
        String message = null;

        if (e != null) {
            String subMessage = e.getMessage() != null ? " (Message: " + e.getMessage() + ") " : "";
            message = e.getClass().getName() + subMessage;

            StackTraceElement[] elems = e.getStackTrace();

            // The last call is our test method, its all we care about
            // Yes its in the front @ index 0
            StackTraceElement deepCall = elems[0];

            message +=
                " at " + deepCall.getMethodName() + "(" + deepCall.getFileName() + ":"
                    + deepCall.getLineNumber() + ")";
        }

        Assert.fail(message);
    }
}
