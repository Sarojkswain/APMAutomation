package com.ca.apm.atc.performance.tests.test;

import com.ca.apm.atc.performance.tests.testbed.ATCPerformanceTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 *  Chrome implementation for ATC UI performance tests.
 *
 *  @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class ChromeTests extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeTests.class);

    public ChromeTests() throws IOException {
        super();
    }

    public ChromeTests(String startUrl, RemoteWebDriver wd) throws Exception {
        super(startUrl, wd);
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testTimelineModesSwitchingPerformanceInChrome() throws Exception {
        runTimelineModesSwitchingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testDrillDownToExperienceServicesCardInChrome() throws Exception {
        runDrillDownToExperienceServicesCardUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testDrillDownToExperienceAppsCardInChrome() throws Exception {
        runDrillDownToExperienceAppsCardUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testStudyAnomaliesPerformanceInChrome() throws Exception {
        runStudyAnomaliesUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG,
            owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testMapPerspectivesSwitchingPerformanceInChrome() throws Exception {
        runMapPerspectivesSwitchingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testMapToDashboardSwitchingPerformanceInChrome() throws Exception {
        runMapToDashboardSwitchingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testMapNodesClickingPerformanceInChrome() throws Exception {
        runMapNodesClickingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"CHROME"})
    public void testMovingMapInThePastPerformanceInChrome() throws Exception {
        runMoveMapInThePastUIPerfTest();
    }

    @Override
    protected long getJSHeapMemory() {
        try {
            Long usedHeapSize = (Long) getUI().getDriver().executeScript("return window.performance.memory.usedJSHeapSize");
            return usedHeapSize != null ? usedHeapSize : 0;
        } catch (WebDriverException e) {
            getLogger().error("Error while trying to read used JS heap size value: ", e);
        }
        return 0;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String getOutputJSFileName() {
        return "test-chrome-data.js";
    }

    void runStudyAnomaliesUIPerfTest() throws IOException {
        testAnalyseAnomalies(EXPERIENCE_STUDY_ANOMALIES_CHROME_DATA_VAR_NAME);
    }

    void runMoveMapInThePastUIPerfTest() throws IOException {
        testMoveMapInThePast(MAP_MOVE_IN_PAST_CHROME_DATA_VAR_NAME);
    }

    void runMapNodesClickingUIPerfTest() throws IOException {
        testMapNodesClicking(MAP_NODES_CLICKING_CHROME_DATA_VAR_NAME);
    }

    void runMapPerspectivesSwitchingUIPerfTest() throws IOException {
        testMapDefaultToNoGroupPerspectiveSwitching(MAP_PERSPECTIVE_CHANGE_CHROME_DATA_VAR_NAME);
    }

    void runMapToDashboardSwitchingUIPerfTest() throws IOException {
        testMapToDashboardSwitching(MAP_TO_DASHBOARD_SWITCH_CHROME_DATA_VAR_NAME);
    }

    void runTimelineModesSwitchingUIPerfTest() throws IOException {
        testTimelineModesSwitching(TIMELINE_MODES_SWITCHING_CHROME_DATA_VAR_NAME);
    }

    void runDrillDownToExperienceServicesCardUIPerfTest() throws IOException {
        testDrillDownToExperienceServicesCard(EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_CHROME_DATA_VAR_NAME);
    }

    void runDrillDownToExperienceAppsCardUIPerfTest() throws IOException {
        testDrillDownToExperienceAppsCard(EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_CHROME_DATA_VAR_NAME);
    }

    public static void main(String[] args) {
        String url = "http://tas-cz-n30.ca.com:8080/ApmServer/";
        try {
            runTimelineModesSwitchingTest(url);
            runDrillDownToExperienceServicesCardTest(url);
            runDrillDownToExperienceAppsCardTest(url);
            runStudyAnomaliesTest(url);
            runMapPerspectivesSwitchingTest(url);
            runMapToDashboardSwitchingTest(url);
            runMapNodesClickingTest(url);
            runMoveMapInThePastTest(url);
        } catch (Throwable e) {
            LOGGER.error("Error happened: ", e);
        }
    }

    private static void runDrillDownToExperienceAppsCardTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runDrillDownToExperienceAppsCardUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runDrillDownToExperienceServicesCardTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runDrillDownToExperienceServicesCardUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runStudyAnomaliesTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runStudyAnomaliesUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runTimelineModesSwitchingTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runTimelineModesSwitchingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runMoveMapInThePastTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runMoveMapInThePastUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runMapNodesClickingTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runMapNodesClickingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runMapPerspectivesSwitchingTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runMapPerspectivesSwitchingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runMapToDashboardSwitchingTest(String url) throws Exception {
        ChromeTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new ChromeTests(url, driver);
            tests.runMapToDashboardSwitchingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    public static RemoteWebDriver createWebDriver() throws MalformedURLException {
//        DesiredCapabilities cap = DesiredCapabilities.chrome();
//        LoggingPreferences logPrefs = new LoggingPreferences();
//        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//        cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://127.0.0.1:9515"), prepareChromeCaps());
        driver.manage().window().maximize();

        return driver;
    }

    public static DesiredCapabilities prepareChromeCaps() {
        Map<String, Object> chromePrefs = new HashMap<>(2);
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", "C:\\");
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

    public static void addCommonCaps(DesiredCapabilities cap) {
        addLoggingCaps(cap);
        cap.setJavascriptEnabled(true);
    }

    public static void addLoggingCaps(DesiredCapabilities cap) {
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.PERFORMANCE, Level.ALL);
        logs.enable(LogType.DRIVER, Level.INFO);
        logs.enable(LogType.BROWSER, Level.INFO);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logs);
    }

}
