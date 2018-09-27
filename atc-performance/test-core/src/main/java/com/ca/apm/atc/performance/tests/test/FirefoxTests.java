package com.ca.apm.atc.performance.tests.test;

import com.ca.apm.atc.performance.tests.testbed.ATCPerformanceTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 *  Firefox implementation for ATC UI performance tests.
 *
 *  @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class FirefoxTests extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirefoxTests.class);

    public FirefoxTests() throws IOException {
        super();
    }

    /**
     * Constructor to create and run the test outside TAS and TestNg.
     *
     * @param startUrl
     * @param wd
     * @throws Exception
     */
    public FirefoxTests(String startUrl, RemoteWebDriver wd) throws Exception {
        super(startUrl, wd);
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testTimelineModesSwitchingPerformanceInFirefox() throws Exception {
        runTimelineModesSwitchingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testDrillDownToExperienceServicesCardInFirefox() throws Exception {
        runDrillDownToExperienceServicesCardUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testDrillDownToExperienceAppsCardInFirefox() throws Exception {
        runDrillDownToExperienceAppsCardUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testStudyAnomaliesPerformanceInFirefox() throws Exception {
        runStudyAnomaliesUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG,
            owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testMapPerspectivesSwitchingPerformanceInFirefox() throws Exception {
        runMapPerspectivesSwitchingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testMapToDashboardSwitchingPerformanceInFirefox() throws Exception {
        runMapToDashboardSwitchingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testMapNodesClickingPerformanceInFirefox() throws Exception {
        runMapNodesClickingUIPerfTest();
    }

    @Tas(testBeds = @TestBed(name = ATCPerformanceTestBed.class,
            executeOn = ATCPerformanceTestBed.SELENIUM_GRID_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = {"FIREFOX"})
    public void testMovingMapInThePastPerformanceInFirefox() throws Exception {
        runMoveMapInThePastUIPerfTest();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String getOutputJSFileName() {
        return "test-firefox-data.js";
    }

    @Override
    protected DesiredCapabilities prepCapabilities(String browser) {
        return super.prepCapabilities("FIREFOX");
    }

    void runStudyAnomaliesUIPerfTest() throws IOException {
        testAnalyseAnomalies(EXPERIENCE_STUDY_ANOMALIES_FIREFOX_DATA_VAR_NAME);
    }

    void runMoveMapInThePastUIPerfTest() throws IOException {
        testMoveMapInThePast(MAP_MOVE_IN_PAST_FIREFOX_DATA_VAR_NAME);
    }

    void runMapNodesClickingUIPerfTest() throws IOException {
        testMapNodesClicking(MAP_NODES_CLICKING_FIREFOX_DATA_VAR_NAME);
    }

    void runMapPerspectivesSwitchingUIPerfTest() throws IOException {
        testMapDefaultToNoGroupPerspectiveSwitching(MAP_PERSPECTIVE_CHANGE_FIREFOX_DATA_VAR_NAME);
    }

    void runMapToDashboardSwitchingUIPerfTest() throws IOException {
        testMapToDashboardSwitching(MAP_TO_DASHBOARD_SWITCH_FIREFOX_DATA_VAR_NAME);
    }

    void runTimelineModesSwitchingUIPerfTest() throws IOException {
        testTimelineModesSwitching(TIMELINE_MODES_SWITCHING_FIREFOX_DATA_VAR_NAME);
    }

    void runDrillDownToExperienceServicesCardUIPerfTest() throws IOException {
        testDrillDownToExperienceServicesCard(EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_FIREFOX_DATA_VAR_NAME);
    }

    void runDrillDownToExperienceAppsCardUIPerfTest() throws IOException {
        testDrillDownToExperienceAppsCard(EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_FIREFOX_DATA_VAR_NAME);
    }

    public static void main(String[] args) {
        String url = "http://tas-cz-nf1.ca.com:8080/ApmServer/";
        try {
            runTimelineModesSwitchingTest(url);
            runDrillDownToExperienceServicesCardTest(url);
            runDrillDownToExperienceAppsCardTest(url);
            runMapPerspectivesSwitchingTest(url);
            runMapToDashboardSwitchingTest(url);
            runMapNodesClickingTest(url);
        } catch (Throwable e) {
            LOGGER.error("Error happened: ", e);
        }
    }

    private static void runDrillDownToExperienceAppsCardTest(String url) throws Exception {
        FirefoxTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new FirefoxTests(url, driver);
            tests.runDrillDownToExperienceAppsCardUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runDrillDownToExperienceServicesCardTest(String url) throws Exception {
        FirefoxTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new FirefoxTests(url, driver);
            tests.runDrillDownToExperienceServicesCardUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runTimelineModesSwitchingTest(String url) throws Exception {
        FirefoxTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new FirefoxTests(url, driver);
            tests.runTimelineModesSwitchingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runMapNodesClickingTest(String url) throws Exception {
        FirefoxTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new FirefoxTests(url, driver);
            tests.runMapNodesClickingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }

        }
    }

    private static void runMapPerspectivesSwitchingTest(String url) throws Exception {
        FirefoxTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new FirefoxTests(url, driver);
            tests.runMapPerspectivesSwitchingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    private static void runMapToDashboardSwitchingTest(String url) throws Exception {
        FirefoxTests tests = null;
        try {
            RemoteWebDriver driver = createWebDriver();
            tests = new FirefoxTests(url, driver);
            tests.runMapToDashboardSwitchingUIPerfTest();
        } finally {
            if (tests != null) {
                tests.cleanup();
            }
        }
    }

    public static RemoteWebDriver createWebDriver() throws MalformedURLException {
        DesiredCapabilities cap = DesiredCapabilities.firefox();
        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://127.0.0.1:9515"), cap);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        return driver;
    }

}