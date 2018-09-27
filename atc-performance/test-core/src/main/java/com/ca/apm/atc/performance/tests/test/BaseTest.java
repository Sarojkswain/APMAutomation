package com.ca.apm.atc.performance.tests.test;

import com.ca.apm.atc.performance.tests.model.TestCycleMetric;
import com.ca.apm.atc.performance.tests.testbed.ATCPerformanceTestBed;
import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.landing.Tile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  Base test class providing common test scenarios implementation as well as
 *  common utility methods.
 *
 *  @author Alexander Sinyushkin (sinal04@ca.com)
 */
public abstract class BaseTest extends UITest {
    public static final int CYCLES_NUM = 50;

    public static final String REPORT_FOLDER = "C:\\atc-perf-report";
    public static final String REPORT_INDEX_HTML_FILE = "report-index.html";
    public static final String RESOURCE_REPORT_INDEX_HTML_FILE = "/report/report-index.html";
    public static final String REPORT_HTML_TEMPLATE = "resultChartTemplate.html";
    public static final String RESOURCE_REPORT_HTML_TEMPLATE = "/report/resultChartTemplate.html";
    public static final String REPORT_JS_FILE = "atcPerfResultChartGenerator.js";
    public static final String RESOURCE_REPORT_JS_FILE = "/report/atcPerfResultChartGenerator.js";
    public static final String REPORT_CSS_FILE = "atcPerfResultChartGenerator.css";
    public static final String RESOURCE_REPORT_CSS_FILE = "/report/atcPerfResultChartGenerator.css";
    public static final String REPORT_HOME_ICON_PNG_FILE = "homeIcon.png";
    public static final String REPORT_LOOP_ICON_PNG_FILE = "loopIcon.png";
    public static final String RESOURCE_REPORT_HOME_ICON_PNG_FILE = "/report/homeIcon.png";
    public static final String RESOURCE_REPORT_LOOP_ICON_PNG_FILE = "/report/loopIcon.png";
    public static final String REPORT_NO_SCREENSHOT_PNG_FILE = "noScreenshot.png";
    public static final String RESOURCE_REPORT_NO_SCREENSHOT_PNG_FILE = "/report/noScreenshot.png";
    public static final String RESOURCE_FONT_NAME = "ca_sans_web-regular";
    public static final String RESOURCE_REPORT_FONT_NAME = "/report/ca_sans_web-regular";
    public static final String[] RESOURCE_FONT_EXTENSIONS = { ".eot", ".svg", ".woff" };

    //Placeholders
    public static final String APM_VERSION_PLACEHOLDER = "APM_VERSION";
    public static final String CHROME_METRICS_VAR_PLACEHOLDER = "CHROME_METRICS";
    public static final String FIREFOX_METRICS_VAR_PLACEHOLDER = "FIREFOX_METRICS";
    public static final String IE_METRICS_VAR_PLACEHOLDER = "IE_METRICS";
    public static final String EDGE_METRICS_VAR_PLACEHOLDER = "EDGE_METRICS";
    public static final String SCENARIO_TITLE_PLACEHOLDER = "SCENARIO_TITLE";
    public static final String TEST_DESCRIPTION_PLACEHOLDER = "TEST_DESCRIPTION";
    public static final String SCREENSHOT_BEFORE_PLACEHOLDER = "SCREENSHOT_BEFORE";
    public static final String SCREENSHOT_AFTER_PLACEHOLDER = "SCREENSHOT_AFTER";

    //Map perspectives change scenario
    public static final String MAP_PERSPECTIVE_CHANGE_REPORT_HTML_FILE = "map-perspective-change-report.html";

    //Map perspectives change scenario metric var names
    public static final String MAP_PERSPECTIVE_CHANGE_CHROME_DATA_VAR_NAME = "mapPerspChangeChromeMetrics";
    public static final String MAP_PERSPECTIVE_CHANGE_FIREFOX_DATA_VAR_NAME = "mapPerspChangeFirefoxMetrics";
    public static final String MAP_PERSPECTIVE_CHANGE_IE_DATA_VAR_NAME = "mapPerspChangeIEMetrics";
    public static final String MAP_PERSPECTIVE_CHANGE_EDGE_DATA_VAR_NAME = "mapPerspChangeEdgeMetrics";
    public static final String MAP_PERSPECTIVE_CHANGE_SCREENSHOT_BEFORE_NAME = "mapDefaultPerspective";
    public static final String MAP_PERSPECTIVE_CHANGE_SCREENSHOT_AFTER_NAME = "mapNoGroupsPerspective";
    public static final String MAP_PERSPECTIVE_CHANGE_SCENARIO_TITLE = "Map 'Default' to 'No Group' perspectives switching.";
    public static final String MAP_PERSPECTIVE_CHANGE_SCENARIO_TEST_DESCRIPTION = "Cycle: <ol><li>Selects 'Default' perspective.</li>" +
            "<li>Selects 'No Group' Perspective.</li></ol>";

    //Map to Dashboard switching scenario
    public static final String MAP_TO_DASHBOARD_SWITCH_REPORT_HTML_FILE = "map-to-dashboard-switch-report.html";
    //Map to Dashboard switching scenario metric var names
    public static final String MAP_TO_DASHBOARD_SWITCH_CHROME_DATA_VAR_NAME = "mapToDashboardSwitchChromeMetrics";
    public static final String MAP_TO_DASHBOARD_SWITCH_FIREFOX_DATA_VAR_NAME = "mapToDashboardSwitchFirefoxMetrics";
    public static final String MAP_TO_DASHBOARD_SWITCH_IE_DATA_VAR_NAME = "mapToDashboardSwitchIEMetrics";
    public static final String MAP_TO_DASHBOARD_SWITCH_EDGE_DATA_VAR_NAME = "mapToDashboardSwitchEdgeMetrics";
    public static final String MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_BEFORE_NAME = "mapView";
    public static final String MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_AFTER_NAME = "dashboardView";
    public static final String MAP_TO_DASHBOARD_SWITCH_SCENARIO_TITLE = "Map to Dashboard views switching.";
    public static final String MAP_TO_DASHBOARD_SWITCH_SCENARIO_TEST_DESCRIPTION = "Cycle: <ol>" +
            "<li>Selects the 'Dashboard' view and waits until the work indicator disappears and the view combo box becomes clickable.</li>" +
            "<li>Selects the 'Map' view and waits until the work indicator disappears and the view combo box becomes clickable.</li></ol>";

    //Map nodes clicking scenario
    public static final String MAP_NODES_CLICKING_REPORT_HTML_FILE          = "map-nodes-clicking-report.html";
    //Map nodes clicking scenario metric var names
    public static final String MAP_NODES_CLICKING_CHROME_DATA_VAR_NAME      = "mapNodesClickingChromeMetrics";
    public static final String MAP_NODES_CLICKING_FIREFOX_DATA_VAR_NAME     = "mapNodesClickingFirefoxMetrics";
    public static final String MAP_NODES_CLICKING_IE_DATA_VAR_NAME          = "mapNodesClickingIEMetrics";
    public static final String MAP_NODES_CLICKING_EDGE_DATA_VAR_NAME        = "mapNodesClickingEdgeMetrics";
    public static final String MAP_NODES_CLICKING_SCREENSHOT_BEFORE_NAME    = "mapNodeSelected";
    public static final String MAP_NODES_CLICKING_SCREENSHOT_AFTER_NAME     = "mapNodeUnselected";
    public static final String MAP_NODES_CLICKING_SCENARIO_TITLE            = "Map nodes clicking.";
    public static final String MAP_NODES_CLICKING_SCENARIO_TEST_DESCRIPTION = "Cycle: <ol><li>Selects a node and waits until its attributes are loaded.</li>" +
            "<li>Presses 'Select highlighted' deselecting the node.</li></ol>";


    //Map moving in the past scenario
    public static final String MAP_MOVE_IN_PAST_REPORT_HTML_FILE          = "map-move-in-the-past-report.html";
    //Map moving in the past scenario metric var names
    public static final String MAP_MOVE_IN_PAST_CHROME_DATA_VAR_NAME      = "mapMoveInPastChromeMetrics";
    public static final String MAP_MOVE_IN_PAST_FIREFOX_DATA_VAR_NAME     = "mapMoveInPastFirefoxMetrics";
    public static final String MAP_MOVE_IN_PAST_IE_DATA_VAR_NAME          = "mapMoveInPastIEMetrics";
    public static final String MAP_MOVE_IN_PAST_EDGE_DATA_VAR_NAME        = "mapMoveInPastEdgeMetrics";
    public static final String MAP_MOVE_IN_PAST_SCREENSHOT_BEFORE_NAME    = "mapNow";
    public static final String MAP_MOVE_IN_PAST_SCREENSHOT_AFTER_NAME     = "mapMovedInPast";
    public static final String MAP_MOVE_IN_PAST_SCENARIO_TITLE            = "Map moving in the past.";
    public static final String MAP_MOVE_IN_PAST_SCENARIO_TEST_DESCRIPTION = "Opens the 'Map' view in historical mode and selects an arbitrary node. <br/>Cycle: " +
            "<ol><li>Opens up a start time point setup dialog.</li>" +
            "<li>Decrements the time range start point time by 1 minute.</li>" +
            "<li>Clicks 'Ok' button.</li>" +
            "<li>Waits until the map reloads.</li>" +
            "</ol>";

    //Timeline modes switching scenario
    public static final String TIMELINE_MODES_SWITCHING_REPORT_HTML_FILE          = "timeline-modes-switching-report.html";
    //Timeline modes switching scenario metric var names
    public static final String TIMELINE_MODES_SWITCHING_CHROME_DATA_VAR_NAME      = "timelineModesSwitchingChromeMetrics";
    public static final String TIMELINE_MODES_SWITCHING_FIREFOX_DATA_VAR_NAME     = "timelineModesSwitchingFirefoxMetrics";
    public static final String TIMELINE_MODES_SWITCHING_IE_DATA_VAR_NAME          = "timelineModesSwitchingIEMetrics";
    public static final String TIMELINE_MODES_SWITCHING_EDGE_DATA_VAR_NAME        = "timelineModesSwitchingEdgeMetrics";
    public static final String TIMELINE_MODES_SWITCHING_SCREENSHOT_BEFORE_NAME    = "timeline24Hours";
    public static final String TIMELINE_MODES_SWITCHING_SCREENSHOT_AFTER_NAME     = "timeline12Hours";
    public static final String TIMELINE_MODES_SWITCHING_SCENARIO_TITLE            = "Timeline modes switching.";
    public static final String TIMELINE_MODES_SWITCHING_SCENARIO_TEST_DESCRIPTION = "Cycle: " +
            "<ol><li>Opens up a mode options combo box.</li>" +
            "<li>Selects a mode with index <code>i % MODE_OPTIONS_COUNT</code>.</li>" +
            "<li>Waits until the tiles gets redrawn.</li>" +
            "<li>Increments the mode index <code>i</code>.</li>" +
            "</ol>";

    //Experience view: drill down to services card scenario
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_REPORT_HTML_FILE = "experience-drill-down-to-services-card-report.html";
    //Experience view: drill down to services card scenario metric var names
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_CHROME_DATA_VAR_NAME = "expDrillDownToServicesCardChromeMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_FIREFOX_DATA_VAR_NAME = "expDrillDownToServicesCardFirefoxMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_IE_DATA_VAR_NAME = "expDrillDownToServicesCardIEMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_EDGE_DATA_VAR_NAME = "expDrillDownToServicesCardEdgeMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_BEFORE_NAME = "expBeforeDrillDownToServices";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_AFTER_NAME = "expAfterDrillDownToServices";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCENARIO_TITLE = "Experience: drill down to services card.";
    public static final String EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCENARIO_TEST_DESCRIPTION = "Cycle: " +
            "<ol><li>Drills down to the services tile.</li>" +
            "<li>Waits until it loads its tiles.</li>" +
            "<li>Scrolls up the window and clicks the home icon in the breadcrumb.</li>" +
            "<li>Waits until the home screen tiles get loaded.</li>" +
            "</ol>";

    //Experience view: drill down to applications card scenario
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_REPORT_HTML_FILE = "experience-drill-down-to-apps-card-report.html";
    //Experience view: drill down to apps card scenario metric var names
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_CHROME_DATA_VAR_NAME = "expDrillDownToAppsCardChromeMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_FIREFOX_DATA_VAR_NAME = "expDrillDownToAppsCardFirefoxMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_IE_DATA_VAR_NAME = "expDrillDownToAppsCardIEMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_EDGE_DATA_VAR_NAME = "expDrillDownToAppsCardEdgeMetrics";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_BEFORE_NAME = "expBeforeDrillDownToApps";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_AFTER_NAME = "expAfterDrillDownToApps";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCENARIO_TITLE = "Experience: drill down to applications card.";
    public static final String EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCENARIO_TEST_DESCRIPTION = "Cycle: " +
            "<ol><li>Drills down to the applications tile.</li>" +
            "<li>Waits until it loads its tiles.</li>" +
            "<li>Scrolls up the window and clicks the home icon in the breadcrumb.</li>" +
            "<li>Waits until the home screen tiles get loaded.</li>" +
            "</ol>";


    //Experience view: study anomalies scenario
    public static final String EXPERIENCE_STUDY_ANOMALIES_REPORT_HTML_FILE = "experience-study-anomalies-report.html";
    //Experience view: study anomalies scenario metric var names
    public static final String EXPERIENCE_STUDY_ANOMALIES_CHROME_DATA_VAR_NAME = "expStudyAnomaliesChromeMetrics";
    public static final String EXPERIENCE_STUDY_ANOMALIES_FIREFOX_DATA_VAR_NAME = "expStudyAnomaliesFirefoxMetrics";
    public static final String EXPERIENCE_STUDY_ANOMALIES_IE_DATA_VAR_NAME = "expStudyAnomaliesIEMetrics";
    public static final String EXPERIENCE_STUDY_ANOMALIES_EDGE_DATA_VAR_NAME = "expStudyAnomaliesEdgeMetrics";
    public static final String EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_BEFORE_NAME = "expBeforeDrillDownToAnomaly";
    public static final String EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_AFTER_NAME = "expAfterDrillDownToAnomaly";
    public static final String EXPERIENCE_STUDY_ANOMALIES_SCENARIO_TITLE = "Experience: analyse anomalies.";
    public static final String EXPERIENCE_STUDY_ANOMALIES_SCENARIO_TEST_DESCRIPTION = "Cycle: <ol><li>Drills down to the 'Services' tile.</li>" +
            "<li>Selects the first from the top anomaly in the right-hand assisted triage panel.</li>" +
            "<li>Clicks on it and selects 'Open an Analysis Notebook'.</li>" +
            "<li>Sequentially goes through suspected nodes by clicking on them.</li>" +
            "<li>Returns back to the experience home view.</li>" +
            "</ol>";




    public static final String TEST_DATA_JS_FILE = "test-data.js";

    private static final int TIMELINE_MODES_COUNT = 7;

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH_mm_ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

    private static final String[] ALL_SCREENSHOT_NAMES = {
            MAP_PERSPECTIVE_CHANGE_SCREENSHOT_BEFORE_NAME,
            MAP_PERSPECTIVE_CHANGE_SCREENSHOT_AFTER_NAME,
            MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_BEFORE_NAME,
            MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_AFTER_NAME,
            MAP_NODES_CLICKING_SCREENSHOT_BEFORE_NAME,
            MAP_NODES_CLICKING_SCREENSHOT_AFTER_NAME,
            MAP_MOVE_IN_PAST_SCREENSHOT_BEFORE_NAME,
            MAP_MOVE_IN_PAST_SCREENSHOT_AFTER_NAME,
            TIMELINE_MODES_SWITCHING_SCREENSHOT_BEFORE_NAME,
            TIMELINE_MODES_SWITCHING_SCREENSHOT_AFTER_NAME,
            EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_BEFORE_NAME,
            EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_AFTER_NAME,
            EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_BEFORE_NAME,
            EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_AFTER_NAME,
            EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_BEFORE_NAME,
            EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_AFTER_NAME
    };

    private static Map<String, Map<String, String>> SCENARIOS = new HashMap<>();
    static {
        //Map perspectives changing scenario.
        Map<String, String> mapPerspChangeVarMap = new HashMap<>();
        mapPerspChangeVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_CHROME_DATA_VAR_NAME);
        mapPerspChangeVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_FIREFOX_DATA_VAR_NAME);
        mapPerspChangeVarMap.put(IE_METRICS_VAR_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_IE_DATA_VAR_NAME);
        mapPerspChangeVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_EDGE_DATA_VAR_NAME);
        mapPerspChangeVarMap.put(SCENARIO_TITLE_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_SCENARIO_TITLE);
        mapPerspChangeVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_SCREENSHOT_BEFORE_NAME);
        mapPerspChangeVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_SCREENSHOT_AFTER_NAME);
        mapPerspChangeVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, MAP_PERSPECTIVE_CHANGE_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(MAP_PERSPECTIVE_CHANGE_REPORT_HTML_FILE, mapPerspChangeVarMap);

        //Map to Dashboard view switching scenario.
        Map<String, String> mapToDashboardSwitchVarMap = new HashMap<>();
        mapToDashboardSwitchVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_CHROME_DATA_VAR_NAME);
        mapToDashboardSwitchVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_FIREFOX_DATA_VAR_NAME);
        mapToDashboardSwitchVarMap.put(IE_METRICS_VAR_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_IE_DATA_VAR_NAME);
        mapToDashboardSwitchVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_EDGE_DATA_VAR_NAME);
        mapToDashboardSwitchVarMap.put(SCENARIO_TITLE_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_SCENARIO_TITLE);
        mapToDashboardSwitchVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_BEFORE_NAME);
        mapToDashboardSwitchVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_AFTER_NAME);
        mapToDashboardSwitchVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, MAP_TO_DASHBOARD_SWITCH_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(MAP_TO_DASHBOARD_SWITCH_REPORT_HTML_FILE, mapToDashboardSwitchVarMap);

        //Map nodes clicking scenario.
        Map<String, String> mapNodesClickingVarMap = new HashMap<>();
        mapNodesClickingVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, MAP_NODES_CLICKING_CHROME_DATA_VAR_NAME);
        mapNodesClickingVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, MAP_NODES_CLICKING_FIREFOX_DATA_VAR_NAME);
        mapNodesClickingVarMap.put(IE_METRICS_VAR_PLACEHOLDER, MAP_NODES_CLICKING_IE_DATA_VAR_NAME);
        mapNodesClickingVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, MAP_NODES_CLICKING_EDGE_DATA_VAR_NAME);
        mapNodesClickingVarMap.put(SCENARIO_TITLE_PLACEHOLDER, MAP_NODES_CLICKING_SCENARIO_TITLE);
        mapNodesClickingVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, MAP_NODES_CLICKING_SCREENSHOT_BEFORE_NAME);
        mapNodesClickingVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, MAP_NODES_CLICKING_SCREENSHOT_AFTER_NAME);
        mapNodesClickingVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, MAP_NODES_CLICKING_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(MAP_NODES_CLICKING_REPORT_HTML_FILE, mapNodesClickingVarMap);

        //Map moving in the past scenario.
        Map<String, String> mapMovingInPastVarMap = new HashMap<>();
        mapMovingInPastVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, MAP_MOVE_IN_PAST_CHROME_DATA_VAR_NAME);
        mapMovingInPastVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, MAP_MOVE_IN_PAST_FIREFOX_DATA_VAR_NAME);
        mapMovingInPastVarMap.put(IE_METRICS_VAR_PLACEHOLDER, MAP_MOVE_IN_PAST_IE_DATA_VAR_NAME);
        mapMovingInPastVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, MAP_MOVE_IN_PAST_EDGE_DATA_VAR_NAME);
        mapMovingInPastVarMap.put(SCENARIO_TITLE_PLACEHOLDER, MAP_MOVE_IN_PAST_SCENARIO_TITLE);
        mapMovingInPastVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, MAP_MOVE_IN_PAST_SCREENSHOT_BEFORE_NAME);
        mapMovingInPastVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, MAP_MOVE_IN_PAST_SCREENSHOT_AFTER_NAME);
        mapMovingInPastVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, MAP_MOVE_IN_PAST_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(MAP_MOVE_IN_PAST_REPORT_HTML_FILE, mapMovingInPastVarMap);

        //Timeline modes switching scenario
        Map<String, String> timelineModesSwitchingVarMap = new HashMap<>();
        timelineModesSwitchingVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, TIMELINE_MODES_SWITCHING_CHROME_DATA_VAR_NAME);
        timelineModesSwitchingVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, TIMELINE_MODES_SWITCHING_FIREFOX_DATA_VAR_NAME);
        timelineModesSwitchingVarMap.put(IE_METRICS_VAR_PLACEHOLDER, TIMELINE_MODES_SWITCHING_IE_DATA_VAR_NAME);
        timelineModesSwitchingVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, TIMELINE_MODES_SWITCHING_EDGE_DATA_VAR_NAME);
        timelineModesSwitchingVarMap.put(SCENARIO_TITLE_PLACEHOLDER, TIMELINE_MODES_SWITCHING_SCENARIO_TITLE);
        timelineModesSwitchingVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, TIMELINE_MODES_SWITCHING_SCREENSHOT_BEFORE_NAME);
        timelineModesSwitchingVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, TIMELINE_MODES_SWITCHING_SCREENSHOT_AFTER_NAME);
        timelineModesSwitchingVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, TIMELINE_MODES_SWITCHING_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(TIMELINE_MODES_SWITCHING_REPORT_HTML_FILE, timelineModesSwitchingVarMap);

        //Experience view: drill down to services card scenario
        Map<String, String> expDrillDownServicesVarMap = new HashMap<>();
        expDrillDownServicesVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_CHROME_DATA_VAR_NAME);
        expDrillDownServicesVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_FIREFOX_DATA_VAR_NAME);
        expDrillDownServicesVarMap.put(IE_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_IE_DATA_VAR_NAME);
        expDrillDownServicesVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_EDGE_DATA_VAR_NAME);
        expDrillDownServicesVarMap.put(SCENARIO_TITLE_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCENARIO_TITLE);
        expDrillDownServicesVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_BEFORE_NAME);
        expDrillDownServicesVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_AFTER_NAME);
        expDrillDownServicesVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_REPORT_HTML_FILE, expDrillDownServicesVarMap);

        //Experience view: drill down to applications card scenario
        Map<String, String> expDrillDownAppsVarMap = new HashMap<>();
        expDrillDownAppsVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_CHROME_DATA_VAR_NAME);
        expDrillDownAppsVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_FIREFOX_DATA_VAR_NAME);
        expDrillDownAppsVarMap.put(IE_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_IE_DATA_VAR_NAME);
        expDrillDownAppsVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_EDGE_DATA_VAR_NAME);
        expDrillDownAppsVarMap.put(SCENARIO_TITLE_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCENARIO_TITLE);
        expDrillDownAppsVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_BEFORE_NAME);
        expDrillDownAppsVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_AFTER_NAME);
        expDrillDownAppsVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_REPORT_HTML_FILE, expDrillDownAppsVarMap);

        //Experience view: analyse anomalies
        Map<String, String> expStudyAnomaliesVarMap = new HashMap<>();
        expStudyAnomaliesVarMap.put(CHROME_METRICS_VAR_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_CHROME_DATA_VAR_NAME);
        expStudyAnomaliesVarMap.put(FIREFOX_METRICS_VAR_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_FIREFOX_DATA_VAR_NAME);
        expStudyAnomaliesVarMap.put(IE_METRICS_VAR_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_IE_DATA_VAR_NAME);
        expStudyAnomaliesVarMap.put(EDGE_METRICS_VAR_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_EDGE_DATA_VAR_NAME);
        expStudyAnomaliesVarMap.put(SCENARIO_TITLE_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_SCENARIO_TITLE);
        expStudyAnomaliesVarMap.put(SCREENSHOT_BEFORE_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_BEFORE_NAME);
        expStudyAnomaliesVarMap.put(SCREENSHOT_AFTER_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_AFTER_NAME);
        expStudyAnomaliesVarMap.put(TEST_DESCRIPTION_PLACEHOLDER, EXPERIENCE_STUDY_ANOMALIES_SCENARIO_TEST_DESCRIPTION);
        SCENARIOS.put(EXPERIENCE_STUDY_ANOMALIES_REPORT_HTML_FILE, expStudyAnomaliesVarMap);

    }

    protected WebDriverWait wait;
    protected File reportDir;

    public BaseTest() throws IOException {
        initReport();
    }

    /**
     * Constructor to create and run the test outside TAS and TestNg.
     *
     * @param startUrl
     * @param wd
     * @throws Exception
     */
    public BaseTest(String startUrl, RemoteWebDriver wd) throws Exception {
        UI ui = new UI(wd, startUrl, UI.View.HOMEPAGE);
        setUI(ui);
        initReport();
        init();
    }

    @BeforeMethod(alwaysRun = true)
    public void before(Method method) throws Exception {
        super.before(method);
        init();
    }

    @AfterMethod(alwaysRun = true)
    public void after(ITestResult testResult) {
        super.after(testResult);
    }

    public void cleanup() {
        if (getUI() != null) {
            getUI().cleanup();
            try {
                getUI().getDriver().quit();
            } catch(Exception e) {
                getLogger().error("Error occurred while cleaning up the UI driver: ", e);
            }
        }
    }

    protected void withoutLiveMode() {
        turnOffLiveMode();
    }

    protected void testTimelineModesSwitching(String varName) throws IOException {
        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);
        int modeInd = 0;
        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("testTimelineModesSwitching(varName=" + varName + "): i=" + i);
                long startTime = System.currentTimeMillis();
                selectNthTimelineMode((modeInd++) % TIMELINE_MODES_COUNT);
                long endTime = System.currentTimeMillis();
                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));
            } catch (Throwable t) {
                String screenShot = getClass().getSimpleName() + "_testTimelineModesSwitching_" + i;
                takeScreenShot(screenShot, false);

                String errMessage = "Error: testTimelineModesSwitching(varName=" + varName + "), i=" + i + ": ";
                getLogger().error(errMessage, t);
                metrics.add(new TestCycleMetric(errMessage + t.getMessage(), screenShot));
            }
        }

        writeAsJson(metrics, varName);
        getTimelineSwitchingScreenshots();
    }

    protected void testDrillDownToExperienceServicesCard(String varName) throws IOException {
        drillDownExperienceCard(0, varName);
        getExperienceServicesDrillDownScreenshots();
    }

    protected void testDrillDownToExperienceAppsCard(String varName) throws IOException {
        drillDownExperienceCard(1, varName);
        getExperienceAppsDrillDownScreenshots();
    }

    private void drillDownExperienceCard(int tileInd, String varName) throws IOException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.experience-card-base.experience-card")));

        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);
        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("drillDownExperienceCard(tileInd=" + tileInd + ", varName=" + varName + "): i=" + i);

                long startTime = System.currentTimeMillis();


                PageElement homeMainEl = getUI().findElement(By.cssSelector(".home-main-section .home-main"));
                List<WebElement> tileElems = homeMainEl.findElements(By.cssSelector("div.experience-card-base.experience-card"));
                WebElement tile = tileElems.get(tileInd);

                WebElement tileHeadingElem = tile.findElement(By.className("experience-card-heading-link"));
                tileHeadingElem.click();

                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".home-main .work-indicator")));

                goHome();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.experience-card-base.experience-card")));

                long endTime = System.currentTimeMillis();
                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));

            } catch (Throwable t) {
                String screenShot = getClass().getSimpleName() + "_drillDownExperienceCard_" + i;
                takeScreenShot(screenShot, false);
                String errMessage = "Error: drillDownExperienceCard(tileInd=" + tileInd +
                        ", varName=" + varName + "), i=" + i + ": ";
                getLogger().error(errMessage, t);
                metrics.add(new TestCycleMetric(errMessage + t.getMessage(), screenShot));
            }
        }

        writeAsJson(metrics, varName);
    }

    protected void testMapNodesClicking(String varName) throws IOException {
        withoutLiveMode();

        selectMapView();

        selectDefaultPerspective();

        fitAllNodesToMapView();

        List<PageElement> mapNodes = getUI().findElements(By.xpath(".//*[local-name()=\"g\" and starts-with(@class,\"nodeSelector \")]"));

        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);

        if (mapNodes == null || mapNodes.isEmpty()) {
            String msg = "Found no nodes on the map view! Exiting.";
            getLogger().warn(msg);
            metrics.add(new TestCycleMetric(msg));
            return;
        }

        int nodeInd = 0;
//        final PageElement node = mapNodes.get(nodeInd);
        //Get arbitrary node to test on
//        final PageElement node = mapNodes.get(0);

        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("testMapNodesClicking(varName=" + varName + "): i=" + i);

                long startTime = System.currentTimeMillis();

                //Get arbitrary node to test on
                final PageElement node = mapNodes.get(nodeInd);
                node.click();

                wait.until(ExpectedConditions.visibilityOfElementLocated(bySelectedMapRectangleNode()));
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));


                Actions builder = new Actions(getUI().getDriver());
                builder.moveToElement(getUI().getDriver().findElement(By.cssSelector("#graphCanvas")), 2, 2);
                builder.click();
                builder.build().perform();

                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));

//                selectHighlighted();

                wait.until(new ExpectedCondition<Boolean>() {

                    @Nullable
                    @Override
                    public Boolean apply(WebDriver driver) {
                        List<PageElement> selNodes = getUI().findElements(By.xpath(".//*[local-name()=\"g\"]/*[local-name()=\"rect\""
                                + " and @fill=\"none\" and @stroke=\"" + getNodeSelectionColor() + "\"]"));

                        return selNodes.size() == 0;
                    }
                });

                long endTime = System.currentTimeMillis();

                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));
                nodeInd++;
                if (nodeInd >= mapNodes.size()) {
                    nodeInd = 0;
                }
            } catch (Exception e) {
                String screenShot = getClass().getSimpleName() + "_testMapNodesClicking_" + i;
                takeScreenShot(screenShot, false);

                String errMessage = "Error: testMapNodesClicking(varName=" + varName + "), i=" + i + ": ";
                getLogger().error(errMessage, e);
                metrics.add(new TestCycleMetric(errMessage + e.getMessage(), screenShot));
            }
        }

        writeAsJson(metrics, varName);

        getMapNodesClickingScreenshots(mapNodes.get(0));
    }

    protected void testMapDefaultToNoGroupPerspectiveSwitching(String varName) throws IOException {
        withoutLiveMode();

        selectMapView();

        selectDefaultPerspective();

        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);
        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("testMapDefaultToNoGroupPerspectiveSwitching(varName=" + varName + "): i=" + i);
                long startTime = System.currentTimeMillis();
                selectNoGroupsPerspective();
                selectDefaultPerspective();
                long endTime = System.currentTimeMillis();
                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));
            } catch (Throwable t) {
                String screenShot = getClass().getSimpleName() + "_testMapDefaultToNoGroupPerspectiveSwitching_" + i;
                takeScreenShot(screenShot, false);

                String errMessage = "Error: testMapDefaultToNoGroupPerspectiveSwitching(varName=" + varName + "), i=" + i + ": ";
                getLogger().error(errMessage, t);
                metrics.add(new TestCycleMetric(errMessage + t.getMessage(), screenShot));
            }
        }

        writeAsJson(metrics, varName);
        getMapPerspectiveChangeScreenshots();
    }

    protected void testMapToDashboardSwitching(String varName) throws IOException {
        withoutLiveMode();
        selectMapView();

        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);
        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("testMapToDashboardSwitching(varName=" + varName + "): i=" + i);
                long startTime = System.currentTimeMillis();
                selectDashboardView();
                selectMapView();
                long endTime = System.currentTimeMillis();
                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));
            } catch (Throwable t) {
                String screenShot = getClass().getSimpleName() + "_testMapToDashboardSwitching_" + i;
                takeScreenShot(screenShot, false);

                String errMessage = "Error: testMapToDashboardSwitching(varName=" + varName + "), i=" + i + ": ";
                getLogger().error(errMessage, t);
                metrics.add(new TestCycleMetric(errMessage + t.getMessage(), screenShot));
            }
        }

        writeAsJson(metrics, varName);
        getMapToDashboardSwitchScreenshots();
    }

    protected void testMoveMapInThePast(String varName) throws IOException {
        withoutLiveMode();
        selectMapView();
        selectDefaultPerspective();
        fitAllNodesToMapView();

        List<PageElement> mapNodes = getUI().findElements(By.xpath(".//*[local-name()=\"g\" and starts-with(@class,\"nodeSelector \")]"));

        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);

        if (mapNodes == null || mapNodes.isEmpty()) {
            String msg = "Found no nodes on the map view! Exiting.";
            getLogger().warn(msg);
            metrics.add(new TestCycleMetric(msg));
            return;
        }

        final PageElement node = mapNodes.get(0);
        node.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(bySelectedMapRectangleNode()));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));

        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("testMoveMapInThePast(varName=" + varName + "): i=" + i);

                long startTime = System.currentTimeMillis();
                goOneMinuteIntoThePast();
                long endTime = System.currentTimeMillis();
                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));
            } catch (Throwable t) {
                String screenShot = getClass().getSimpleName() + "_testMoveMapInThePast_" + i;
                takeScreenShot(screenShot, false);

                String msg = "Error: testMoveInThePast(varName=" + varName + "), i=" + i + ": ";
                getLogger().error(msg, t);
                metrics.add(new TestCycleMetric(msg + t.getMessage(), screenShot));
            }
        }

        writeAsJson(metrics, varName);
        getMapMovingInThePastScreenshots();
    }

    protected void testAnalyseAnomalies(String varName) throws IOException {

        List<TestCycleMetric> metrics = new ArrayList<>(CYCLES_NUM);
        for (int i = 0; i < CYCLES_NUM; i++) {
            try {
                getLogger().info("testAnalyseAnomalies(varName=" + varName + "): i=" + i);

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.experience-card-base.experience-card")));

                long startTime = System.currentTimeMillis();

                List<PageElement> suspectedNodes = drillDownToAnomalySuspectedNodes();
                int suspectedNodesCount = suspectedNodes != null ? suspectedNodes.size() : 0;
                getLogger().info("testAnalyseAnomalies(i={}): found {} suspected nodes", i, suspectedNodesCount);
                for (PageElement suspectedNode : suspectedNodes) {
                    suspectedNode.click();
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));
                }

                goHome();

                long endTime = System.currentTimeMillis();
                metrics.add(new TestCycleMetric(endTime - startTime, getJSHeapMemory()));

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.experience-card-base.experience-card")));

            } catch (Throwable t) {
                String screenShot = getClass().getSimpleName() + "_testAnalyseAnomalies_" + i;
                takeScreenShot(screenShot, false);

                String errMessage = "Error: testAnalyseAnomalies(varName=" + varName + "), i=" + i + ": ";
                getLogger().error(errMessage, t);
                metrics.add(new TestCycleMetric(errMessage + t.getMessage(), screenShot));

                try {
                    goHome();
                } catch (Exception e) {
                    getLogger().error("Error: testAnalyseAnomalies(varName=" + varName +
                            "), i=" + i + ", error while going home from a catch block: ", e);
                }
            }
        }

        try {
            writeAsJson(metrics, varName);
        } catch (Exception e) {
            getLogger().error("Failed to persist 'Analyse anomalies test' metrics on disk: ", e);
        }

        try {
            getAnomalyAnalysingScreenshots();
        } catch (Exception e) {
            getLogger().error("Failed to obtain 'Analyse anomalies test' screenshots: ", e);
        }

    }

    protected void goOneMinuteIntoThePast() {
        WebElement startTimeIndElem = getElemOrNull(By.id("start-time-indication"));
        startTimeIndElem.click();

        By startTimeDialogElem = By.id("startTimeDtpDialog");
//        wait.until(ExpectedConditions.visibilityOfElementLocated(startTimeDialogElem));
        By byDecrementMinutes = By.cssSelector(".uib-decrement.minutes");
        wait.until(ExpectedConditions.elementToBeClickable(byDecrementMinutes));


        WebElement decrMinutesElem = getElemOrNull(byDecrementMinutes);
        decrMinutesElem.click();

//                By.className("common-button common-button-primary")
        WebElement okButtonElem = getElemOrNull(By.cssSelector(".common-button.common-button-primary"));
        wait.until(ExpectedConditions.elementToBeClickable(okButtonElem));

        okButtonElem.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(startTimeDialogElem));

        waitUntilAppMapWorkIndicatorDisappears();

    }

    protected void goHome() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("breadcrumb-work-indicator")));
        getUI().getDriver().executeScript("window.scrollTo(0, 0);");
        getUI().findElement(By.className("icon-home")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("breadcrumb-work-indicator")));
        waitUntilHomeMainWorkIndicatorDisappears();
//        waitUntilAppMapWorkIndicatorDisappears();
    }


    protected void waitUntilProblemsAndAnomaliesWorkIndicatorDisappears() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".problems-anomalies-content .work-indicator")));
    }

    protected void waitUntilHomeMainWorkIndicatorDisappears() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".home-main .work-indicator")));
    }

    protected void waitUntilAppMapWorkIndicatorDisappears() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(byWorkIndicator()));
    }

    protected By bySelectedMapRectangleNode() {
        return By.cssSelector("rect[stroke='" + getNodeSelectionColor() + "']");
    }

    protected String getNodeSelectionColor() {
        return Canvas.NODE_SELECTION;
    }

    protected void fitAllNodesToMapView() {
        WebElement fitAllToViewButton = getElemOrNull(By.cssSelector("img[title=\"Fit All To View\"]"));
        waitUntilAppMapWorkIndicatorDisappears();
        fitAllToViewButton.click();

        waitUntilAppMapWorkIndicatorDisappears();
        wait.until(ExpectedConditions.elementToBeClickable(fitAllToViewButton));
    }

    protected void selectHighlighted() {
        WebElement selectHighlightedButton = getElemOrNull(By.cssSelector("img[title=\"Select Highlighted\"]"));
        selectHighlightedButton.click();

        waitUntilAppMapWorkIndicatorDisappears();
        wait.until(ExpectedConditions.elementToBeClickable(selectHighlightedButton));
    }

    protected void openTimeline() {
        WebElement viewSelector = getElemOrNull(By.id("view-selector"));
        if (viewSelector != null) { // Pre 10.6 version detected

        } else { // > 10.5 version
            getUI().getTimeline().expand();
        }

    }

    protected void getTimelineSwitchingScreenshots() {
        selectNthTimelineMode(0);
        takeScreenShot(TIMELINE_MODES_SWITCHING_SCREENSHOT_BEFORE_NAME);
        selectNthTimelineMode(1);
        takeScreenShot(TIMELINE_MODES_SWITCHING_SCREENSHOT_AFTER_NAME);
    }

    protected void getExperienceAppsDrillDownScreenshots() {
        takeScreenShot(EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_BEFORE_NAME);
        Tile appsTile = getUI().getLandingPage().getNthTile(1);
        appsTile.drillDown();
        takeScreenShot(EXPERIENCE_DRILL_DOWN_TO_APPS_CARD_SCREENSHOT_AFTER_NAME);
    }

    protected void getExperienceServicesDrillDownScreenshots() {
        takeScreenShot(EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_BEFORE_NAME);
        Tile appsTile = getUI().getLandingPage().getNthTile(0);
        appsTile.drillDown();
        takeScreenShot(EXPERIENCE_DRILL_DOWN_TO_SERVICES_CARD_SCREENSHOT_AFTER_NAME);
    }

    protected void getMapPerspectiveChangeScreenshots() {
        selectDefaultPerspective();
        takeScreenShot(MAP_PERSPECTIVE_CHANGE_SCREENSHOT_BEFORE_NAME);
        selectNoGroupsPerspective();
        takeScreenShot(MAP_PERSPECTIVE_CHANGE_SCREENSHOT_AFTER_NAME);
    }

    protected void selectDefaultPerspective() {
        PerspectivesControl perspectivesCtrl = getUI().getPerspectivesControl();
        perspectivesCtrl.selectDefaultPerspective();
        waitUntilAppMapWorkIndicatorDisappears();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.perspective-dropdown")));
    }

    protected void selectNoGroupsPerspective() {
        PerspectivesControl perspectivesCtrl = getUI().getPerspectivesControl();
        perspectivesCtrl.selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        waitUntilAppMapWorkIndicatorDisappears();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.perspective-dropdown")));
    }

    protected List<PageElement> drillDownToAnomalySuspectedNodes() {
        PageElement homeMainEl = getUI().findElement(By.cssSelector(".home-main-section .home-main"));
//        List<WebElement> tileElems = homeMainEl.findElements(By.xpath("//div[contains(@class, 'experience-card-base') and contains(@class, 'experience-card')]//div[contains(@class, 'comp-cards-anomalies-number') and text() > 0 ]"));
        List<WebElement> tileElems = homeMainEl.findElements(By.cssSelector("div.experience-card-base.experience-card"));
        WebElement tile = null;

        for (WebElement tileElem : tileElems) {
            WebElement anomaliesNumberElem = tileElem.findElement(By.className("comp-cards-anomalies-number"));
            String anomaliesNumAsTxt = anomaliesNumberElem.getText();
            int anomaliesNum = Integer.parseInt(anomaliesNumAsTxt);
            if (anomaliesNum > 0) {
                tile = tileElem;
                break;
            }
        }

        WebElement tileHeadingElem = tile.findElement(By.className("experience-card-heading-link"));
        tileHeadingElem.click();

        waitUntilHomeMainWorkIndicatorDisappears();
        waitUntilProblemsAndAnomaliesWorkIndicatorDisappears();

//        waitUntilAppMapWorkIndicatorDisappears();

//        WebElement anomaliesPanelElem = getElemOrNull(By.className("t-anomalies"));
//        List<WebElement> anomalies = anomaliesPanelElem.findElements(By.cssSelector(".at-story.story-text"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".at-story.story-text")));
        List<WebElement> anomalies = getUI().getDriver().findElements(By.cssSelector(".at-story.story-text"));

        WebElement firstAnomaly = anomalies.get(0);
        firstAnomaly.click();

        waitUntilHomeMainWorkIndicatorDisappears();
//        waitUntilAppMapWorkIndicatorDisappears();

        WebElement openNotebookLink = firstAnomaly.findElement(By.linkText("Open an Analysis Notebook"));
        openNotebookLink.click();

        waitUntilAppMapWorkIndicatorDisappears();

        return getUI().findElements(By.cssSelector(".container-level-2.t-actor"));
    }


    protected void getAnomalyAnalysingScreenshots() {
        goHome();

        takeScreenShot(EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_BEFORE_NAME);

        List<PageElement> suspectedNodes = drillDownToAnomalySuspectedNodes();

        suspectedNodes.get(0).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));

        takeScreenShot(EXPERIENCE_STUDY_ANOMALIES_SCREENSHOT_AFTER_NAME);

    }

    protected void getMapMovingInThePastScreenshots() {
        selectNthTimelineMode(0);
        withoutLiveMode();
        takeScreenShot(MAP_MOVE_IN_PAST_SCREENSHOT_BEFORE_NAME);
        goOneMinuteIntoThePast();
        takeScreenShot(MAP_MOVE_IN_PAST_SCREENSHOT_AFTER_NAME);
    }

    protected void getMapNodesClickingScreenshots(PageElement node) {
        node.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(bySelectedMapRectangleNode()));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));

        takeScreenShot(MAP_NODES_CLICKING_SCREENSHOT_BEFORE_NAME);
        selectHighlighted();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".work-indicator")));

        takeScreenShot(MAP_NODES_CLICKING_SCREENSHOT_AFTER_NAME);
    }

    protected void getMapToDashboardSwitchScreenshots() {
        takeScreenShot(MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_BEFORE_NAME);
        selectDashboardView();
        takeScreenShot(MAP_TO_DASHBOARD_SWITCH_SCREENSHOT_AFTER_NAME);
    }

    protected void prepareCommonReportFiles() throws IOException {
        //Report index
        File reportIndexHTMLFile = new File(reportDir, REPORT_INDEX_HTML_FILE);
        if (!reportIndexHTMLFile.exists()) {
            getLogger().info("Creating report index at {}", reportIndexHTMLFile);
            reportIndexHTMLFile.createNewFile();
            URL reportIndexHtmlUrl = getClass().getResource(RESOURCE_REPORT_INDEX_HTML_FILE);
            FileUtils.copyURLToFile(reportIndexHtmlUrl, reportIndexHTMLFile);
            String buffer = new String(Files.readAllBytes(reportIndexHTMLFile.toPath()));
            String introscopeVersion = envProperties.getTestbedProperties().getOrDefault(ATCPerformanceTestBed.INTROSCOPE_VERSION_PROP_NAME,
                    "<UNKNOWN>");
            String emAddress = envProperties.getTestbedProperties().getOrDefault(ATCPerformanceTestBed.TEST_APP_BASE_URL_PROP_NAME, "http://");
            buffer = buffer.replace(APM_VERSION_PLACEHOLDER, introscopeVersion + " (<a href=\"" + emAddress + "\" target=\"_blank\" class=\"emAddrLink\">" + emAddress + "</a>)");
            FileUtils.writeStringToFile(reportIndexHTMLFile, buffer, false);
        }

        //Common report JS
        File reportJSFile = new File(reportDir, REPORT_JS_FILE);
        if (!reportJSFile.exists()) {
            getLogger().info("Creating report JS script at {}", reportJSFile);
            reportJSFile.createNewFile();
            URL reportJsUrl = getClass().getResource(RESOURCE_REPORT_JS_FILE);
            FileUtils.copyURLToFile(reportJsUrl, reportJSFile);
        }

        //Common report CSS
        File reportCssFile = new File(reportDir, REPORT_CSS_FILE);
        if (!reportCssFile.exists()) {
            getLogger().info("Creating report CSS file at {}", reportCssFile);
            reportCssFile.createNewFile();
            URL reportCssUrl = getClass().getResource(RESOURCE_REPORT_CSS_FILE);
            FileUtils.copyURLToFile(reportCssUrl, reportCssFile);
        }

        //Home icon file
        File homeIconPngFile = new File(reportDir, REPORT_HOME_ICON_PNG_FILE);
        if (!homeIconPngFile.exists()) {
            getLogger().info("Creating report home icon file at {}", homeIconPngFile);
            homeIconPngFile.createNewFile();
            URL reportHomeIconUrl = getClass().getResource(RESOURCE_REPORT_HOME_ICON_PNG_FILE);
            FileUtils.copyURLToFile(reportHomeIconUrl, homeIconPngFile);
        }

        //Loop icon file
        File loopIconPngFile = new File(reportDir, REPORT_LOOP_ICON_PNG_FILE);
        if (!loopIconPngFile.exists()) {
            getLogger().info("Creating report loop icon file at {}", loopIconPngFile);
            loopIconPngFile.createNewFile();
            URL loopIconUrl = getClass().getResource(RESOURCE_REPORT_LOOP_ICON_PNG_FILE);
            FileUtils.copyURLToFile(loopIconUrl, loopIconPngFile);
        }

        //Fonts
        for (String fontExt : RESOURCE_FONT_EXTENSIONS) {
            String fullFontName = RESOURCE_FONT_NAME + fontExt;
            File fontFile = new File(reportDir, fullFontName);
            if (!fontFile.exists()) {
                getLogger().info("Creating font file at {}", fontFile);
                fontFile.createNewFile();
                URL fontFileUrl = getClass().getResource(RESOURCE_REPORT_FONT_NAME + fontExt);
                FileUtils.copyURLToFile(fontFileUrl, fontFile);
            }
        }

        //No screenshot file
        File noScreenshotPngFile = new File(reportDir, REPORT_NO_SCREENSHOT_PNG_FILE);
        if (!noScreenshotPngFile.exists()) {
            getLogger().info("Creating report no screenshot file at {}", noScreenshotPngFile);
            noScreenshotPngFile.createNewFile();
            URL reportNoScreenshotPngUrl = getClass().getResource(RESOURCE_REPORT_NO_SCREENSHOT_PNG_FILE);
            FileUtils.copyURLToFile(reportNoScreenshotPngUrl, noScreenshotPngFile);
        }

        for (String screenshotName : ALL_SCREENSHOT_NAMES) {
            screenshotName += ".png";
            File screenshotFile = new File(reportDir, screenshotName);
            if (!screenshotFile.exists()) {
                getLogger().info("Creating screenshot placeholder image file at {}", screenshotFile);
                screenshotFile.createNewFile();
                URL reportNoScreenshotPngUrl = getClass().getResource(RESOURCE_REPORT_NO_SCREENSHOT_PNG_FILE);
                FileUtils.copyURLToFile(reportNoScreenshotPngUrl, screenshotFile);
            }

        }

        //Prepare separate scenario report HTML files
        for (String scenarioReportHtmlFile : SCENARIOS.keySet()) {
            Map<String, String> placeholdersToVarNames = SCENARIOS.get(scenarioReportHtmlFile);

            File reportHTMLFile = new File(reportDir, scenarioReportHtmlFile);

            if (!reportHTMLFile.exists()) {
                getLogger().info("Creating scenario report HTML file at {}", reportHTMLFile);
                reportHTMLFile.createNewFile();
                URL reportHtmlUrl = getClass().getResource(RESOURCE_REPORT_HTML_TEMPLATE);
                FileUtils.copyURLToFile(reportHtmlUrl, reportHTMLFile);

                String buffer = new String(Files.readAllBytes(reportHTMLFile.toPath()));

                for (String placeholderName : placeholdersToVarNames.keySet()) {
                    String varName = placeholdersToVarNames.get(placeholderName);
                    buffer = buffer.replace(placeholderName, varName);
                }

                FileUtils.writeStringToFile(reportHTMLFile, buffer, false);
            }
        }
    }

    protected void selectMapView() {
        selectView("Map");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.perspective-dropdown")));
    }

    protected void selectDashboardView() {
        selectView("Dashboard");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.perspective-dropdown")));
    }

    protected void selectView(String name) {
        WebElement viewSelector = getElemOrNull(By.id("view-selector"));
        if (viewSelector != null) { // Pre 10.6 version detected
            viewSelector.click();

            WebElement comboElem = getElemOrNull(By.id("view-selection-combo-menu"));
            comboElem.findElement(By.linkText(name)).click();
            waitUntilAppMapWorkIndicatorDisappears();
        } else { // > 10.5 version
            switch (name) {
                case "Map":
                    getUI().getMapviewPage().go();
                    break;
                case "Dashboard":
                    getUI().getDashboardPage().go();
                    break;
            }
        }
    }

    protected void turnOffLiveMode() {
        List<WebElement> options = getTimelineOptions();
        options.get(options.size() - 1).findElement(By.tagName("a")).click();
        waitUntilAppMapWorkIndicatorDisappears();
    }

    protected void selectLast24HoursMode() {
        selectNthTimelineMode(0);
    }

    protected void selectLast12HoursMode() {
        selectNthTimelineMode(1);
    }

    protected void selectLast6HoursMode() {
        selectNthTimelineMode(2);
    }

    protected void selectLast2HoursMode() {
        selectNthTimelineMode(3);
    }

    protected void selectLast30MinsMode() {
        selectNthTimelineMode(4);
    }

    protected void selectNthTimelineMode(int n) {
        List<WebElement> options = getTimelineOptions();
        WebElement elem = options.get(n);
        WebElement anchorElem = elem.findElement(By.tagName("a"));
        anchorElem.click();
        waitUntilAppMapWorkIndicatorDisappears();
    }

    protected List<WebElement> getTimelineOptions() {
        WebElement timeRangeSelectionCombo = getElemOrNull(By.id("time-range-selection-combo"));
        timeRangeSelectionCombo.click();
        WebElement timeRangeMenu = getElemOrNull(By.id("time-range-selection-combo-menu"));
        List<WebElement> elems = timeRangeMenu.findElements(By.tagName("li"));
        return elems;
    }

    protected WebElement getElemOrNull(By selector) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(selector));
        } catch (Throwable e) {
            getLogger().error("Failed to get element by selector: " + selector, e);
            return null;
        }
    }

    protected By byWorkIndicator() {
        return By.id("appMapWorkIndicator");
    }

    protected String getOutputJSFileName() {
        return TEST_DATA_JS_FILE;
    }

    protected void writeAsJson(List<TestCycleMetric> metrics, String varName) throws IOException {
        for (int i = 0; i < metrics.size(); i++) {
            metrics.get(i).setId(i + 1);
        }

        File outputFile = new File(reportDir, getOutputJSFileName());

        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            getLogger().info("Output JSON file: {}", outputFile.getAbsolutePath());
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, true))) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(metrics);
                writer.println();
                writer.println(String.format("var %s = %s;", varName, json));
            }
        } catch (IOException e) {
            getLogger().error("Failed to write to '" + outputFile + "': ", e);
            throw e;
        }
    }

    protected Logger getLogger() {
        return LOGGER;
    }


    protected void takeScreenShot(String fileName) {
        takeScreenShot(fileName, true);
    }

    protected void takeScreenShot(String fileName, boolean explicitWait) {
        File destFile = new File(reportDir, fileName + ".png");
        if (destFile.exists()) {
            destFile.delete();
        }

        waitUntilAppMapWorkIndicatorDisappears();

        if (explicitWait) {
            try {
                //This is to make sure the work indicator is really visually not present on the screenshot
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                getLogger().error("Error in takeScreenShot(): ", e);
            }
        }

        File scrFile = getUI().getDriver().getScreenshotAs(OutputType.FILE)
                .getAbsoluteFile();
        try {
            FileUtils.copyFile(scrFile, destFile);
        } catch (IOException e) {
            getLogger().error("Failed to copy screenshot from '" + scrFile.getAbsolutePath() +
                    "' over to '" + destFile.getAbsolutePath() + "': ", e);
        }
    }


    protected long getJSHeapMemory() {
        return 0;
    }

    protected void init() throws Exception {
        try {
            RemoteWebDriver driver = getUI().getDriver();
            //Rewrite em-project's setting of 3 seconds
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
            wait = new WebDriverWait(driver, 10, 100);
            getUI().login();
        } catch (Throwable t) {
            getLogger().error("Error in init(): ", t);
            throw t;
        }
    }

    protected void initReport() throws IOException {
        reportDir = new File(REPORT_FOLDER);

        if (!reportDir.exists()) {
            getLogger().info("Creating report directory at {}", reportDir);
            reportDir.mkdir();
        }

        prepareCommonReportFiles();
    }
}
