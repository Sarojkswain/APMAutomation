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

package com.ca.apm.test.atc.common;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class Browser {

    RemoteWebDriver driver;

    private static final String SCREENSHOTS_FOLDER_NAME = "screenshots";
    private static final String DEFAULT_WEB_DRIVER_URL = "http://cz-selenium1.ca.com:4444/wd/hub";

    protected final Logger logger = Logger.getLogger(getClass());

    private EnvironmentPropertyContext env = null;

    public static long IMPLICITLY_WAIT = 0;

    public RemoteWebDriver open() throws Exception {

        env = new EnvironmentPropertyContextFactory().createFromSystemProperty();

        if (env.getTestbedProperties().containsKey("chromeDriverPath")) {
            DesiredCapabilities dc = prepChromeCaps();
            createLocal(dc, env.getTestbedProperties().get("chromeDriverPath"));
        } else {
            String webdriverURL = System.getProperty("selenium.webdriverURL");
            
            if (webdriverURL == null) {
                webdriverURL = env.getTestbedProperties().get("selenium.webdriverURL");
                if (webdriverURL == null) {
                    webdriverURL = DEFAULT_WEB_DRIVER_URL;
                }
            }
            String browser = defaultString(env.getTestbedProperties().get("browser"), "CHROME");
            DesiredCapabilities dc = prepCapabilities(browser);
            createRemote(dc, webdriverURL);
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);

        return this.driver;
    }

    public RemoteWebDriver openDefault() throws Exception {

        DesiredCapabilities dc = prepChromeCaps();
        createRemote(dc, DEFAULT_WEB_DRIVER_URL);

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);

        return this.driver;
    }

    /**
     * Create instance of UI based on local driver
     * 
     * @param dc
     * @param localDriverPath
     *        to local driver
     * @return local instance of UI
     */
    protected void createLocal(DesiredCapabilities dc, String localDriverPath) {
        if (dc.getBrowserName().equals(DesiredCapabilities.chrome().getBrowserName())) {
            System.setProperty("webdriver.chrome.driver", localDriverPath);
            this.driver = new ChromeDriver(prepChromeCaps());
        } else {
            throw new IllegalArgumentException("Invalid browser specified");
        }
    }

    protected void createRemote(DesiredCapabilities dc, String remoteDriverUrl) throws Exception {
        this.driver = new RemoteWebDriver(new URL(remoteDriverUrl), dc);
    }
    
    public void close() {
        try {
            driver.quit();
        } catch (WebDriverException e) {
            //swallow it
        }
    }

    public RemoteWebDriver getDriver() {
        return this.driver;
    }

    public void takeScreenshot(String testName, String methodName, String stepName) {
        try {
            File screenshotFile = driver.getScreenshotAs(OutputType.FILE);
            String screenshotExtension =
                FilenameUtils.getExtension(screenshotFile.getAbsolutePath());

            File targetScreenshotFile =
                FileUtils.getFile(SCREENSHOTS_FOLDER_NAME, testName, methodName
                    + "-" + stepName + "." + screenshotExtension);
            targetScreenshotFile.getParentFile().mkdirs();
            FileUtils.copyFile(screenshotFile, targetScreenshotFile);
            logger.debug(format("Created a screenshots for test %s: %s", methodName,
                targetScreenshotFile.getAbsolutePath()));
        } catch (Exception e) {
            // be quiet
            logger.warn(format("Unable to take a screenshots for test %s!", methodName), e);
        }
    }

    private static final String DOWNLOAD_DIR = "C:\\";

    public static void addLoggingCaps(DesiredCapabilities cap) {
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.DRIVER, Level.INFO);
        logs.enable(LogType.BROWSER, Level.INFO);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logs);
    }

    public static void addCommonCaps(DesiredCapabilities cap) {
        addLoggingCaps(cap);
        cap.setJavascriptEnabled(true);
    }

    public static DesiredCapabilities prepFirefoxCaps() {
        final DesiredCapabilities cap = DesiredCapabilities.firefox();
        // Use old Firefox extension based driver instead of new Marionette driver.
        cap.setCapability("marionette", false);
        FirefoxProfile prof = new FirefoxProfile();
        prof.setPreference("browser.helperApps.neverAsk.openFile", "text/csv");
        prof.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv");
        prof.setPreference("browser.download.folderList", 2);
        prof.setPreference("browser.download.dir", DOWNLOAD_DIR);
        prof.setPreference("browser.download.manager.showWhenStarting", false);
        cap.setCapability("firefox_profile", prof);
        addCommonCaps(cap);
        return cap;
    }

    public static DesiredCapabilities prepChromeCaps() {
        Map<String, Object> chromePrefs = new HashMap<>(2);
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", DOWNLOAD_DIR);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("enable-precise-memory-info", "disable-extensions", "start-maximized",
            "disable-infobars");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        addCommonCaps(cap);
        return cap;
    }


    public static DesiredCapabilities prepInternetExplorerCaps() {
        DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
        cap.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        cap.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
        cap.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
        cap.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR,
            UnexpectedAlertBehaviour.DISMISS);
        cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
            true);
        cap.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
        cap.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
        cap.setCapability(InternetExplorerDriver.LOG_FILE, "iewebdriver.log");
        cap.setCapability(InternetExplorerDriver.LOG_LEVEL, InternetExplorerDriverLogLevel.TRACE);
        addCommonCaps(cap);
        return cap;
    }

    public static DesiredCapabilities prepInternetExplorer11Caps() {
        DesiredCapabilities cap = prepInternetExplorerCaps();
        cap.setCapability(CapabilityType.VERSION, "11");
        return cap;
    }

    public static DesiredCapabilities prepEdgeCaps() {
        DesiredCapabilities cap = DesiredCapabilities.edge();
        // XXX: Something added by these cause the tests to fail or stop indefinitely.
        //addCommonCaps(cap);
        //addLoggingCaps(cap);
        return cap;
    }

    public static DesiredCapabilities prepCapabilities(String browser) {
        switch (browser) {
            default:
            case "FIREFOX":
                return prepFirefoxCaps();

            case "CHROME":
                return prepChromeCaps();

            case "IE":
            case "IE11":
                return prepInternetExplorer11Caps();

            case "EDGE":
                return prepEdgeCaps();
        }
    }
}
