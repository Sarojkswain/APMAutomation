package com.ca.apm.nextgen.tests.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.ClassUtils;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ca.tas.role.seleniumgrid.BrowserType;

import static com.ca.tas.role.seleniumgrid.BrowserType.EDGE;
import static com.ca.tas.role.seleniumgrid.BrowserType.INTERNET_EXPLORER;

/**
 * @author haiva01
 */
public class SeleniumHelpers {

    private static final String DOWNLOAD_DIR = "C:\\SW";

    public static void addCommonCaps(DesiredCapabilities cap) {
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.DRIVER, Level.INFO);
        logs.enable(LogType.BROWSER, Level.INFO);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logs);
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
        // This is intentionally commented out. It appears something in the common capabilities
        // breaks some Microsoft Edge uses for us.
        //addCommonCaps(cap);
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

    public static boolean isRemoteDriver(WebDriver driver) {
        return ClassUtils.isAssignable(driver.getClass(), RemoteWebDriver.class);
    }

    public static BrowserType toBrowserType(String browserName) {
        switch (browserName) {
            case "chrome":
                return BrowserType.CHROME;

            case "firefox":
                return BrowserType.FIREFOX;

            case "htmlunit":
                return BrowserType.HTMLUNIT;

            case "internet explorer":
                return BrowserType.INTERNET_EXPLORER;

            case "MicrosoftEdge":
                return BrowserType.EDGE;

            default:
                throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }

    public static BrowserType toBrowserType(Class<?> klass) {
        if (ClassUtils.isAssignable(klass, ChromeDriver.class)) {
            return BrowserType.CHROME;
        } else if (ClassUtils.isAssignable(klass, FirefoxDriver.class)) {
            return BrowserType.FIREFOX;
        } else if (ClassUtils.isAssignable(klass, InternetExplorerDriver.class)) {
            return INTERNET_EXPLORER;
        } else if (ClassUtils.isAssignable(klass, EdgeDriver.class)) {
            return EDGE;
        } else if (ClassUtils.isAssignable(klass, HtmlUnitDriver.class)) {
            return BrowserType.HTMLUNIT;
        } else {
            throw new RuntimeException("Unsupported browser: " + klass.getName());
        }
    }

    public static BrowserType browserType(WebDriver driver) {
        if (isRemoteDriver(driver)) {
            HasCapabilities rd = (HasCapabilities) driver;
            String browserName = rd.getCapabilities().getBrowserName();
            return toBrowserType(browserName);
        } else {
            return toBrowserType(driver.getClass());
        }
    }
}
