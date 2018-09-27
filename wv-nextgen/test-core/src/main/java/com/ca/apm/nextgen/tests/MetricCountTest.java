/**
 * 
 */
package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvConstants;
import com.ca.apm.nextgen.tests.helpers.TableUi;
import com.ca.apm.nextgen.tests.helpers.WaitForElementCondition;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.WebViewUi.TimeResolution;
import com.ca.apm.nextgen.tests.helpers.WebViewUi.TimeWindow;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Selenium tests for WebView's Investigator 'Metric Browser'/'Metric Count' tab which is accessible on agents and 
 * some other nodes in the 'Metric Browser' tree. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MetricCountTest extends BaseWebViewTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricCountTest.class);
    
    private static final String[] METRIC_BROWSER_NODES = {
                                                          "investigator-tree-2_Domain:",
                                                          "investigator-tree-2_Host:Custom Metric Host (Virtual)",
                                                          "investigator-tree-2_Process:Custom Metric Host (Virtual)|Custom Metric Process (Virtual)",
                                                          "investigator-tree-2_Agent:SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)",
                                                          "investigator-tree-2_Path:SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager"
        };

    private static final String[] EXPECTED_METRIC_COUNT_GRID_TABLE_COLUMN_NAMES = {
                                                                                   "Color",
                                                                                   "Resource",
                                                                                   "Metric Count",
                                                                                   "Percent of Total"
    };
    
    private static final String OTHERS_TOOLTIP_LABEL = "Others";
    private static final String BACKGROUND_COLOR_CSS_PROPERTY = "background-color:";
    private static final String METRIC_BROWSER_ELEMENT_TEMPLATE = "//div[@ftid=\"%s\"]";
    private static final String EXPAND_PLUS_IMG_XPATH = "//img[2]";
    private static final String METRIC_COUNT_TAB_XPATH = "//span[text()='Metric Count']";
    private static final String WEBVIEW_TYPE_VIEW_METRIC_COUNT_ID = "webViewTypeview_2-Metric Count";
    private static final String WEBVIEW_TYPE_VIEW_METRIC_COUNT_XPATH = "//div[@id=\"" + WEBVIEW_TYPE_VIEW_METRIC_COUNT_ID + "\"]";
    private static final String TOTAL_METRICS_UNDER_THIS_BRANCH_XPATH = WEBVIEW_TYPE_VIEW_METRIC_COUNT_XPATH + "//div[contains(text(),'Metrics under this branch:')]";
    private static final String METRIC_COUNT_TYPE_VIEWER_GRID_ID = "webview-investigator-metriccounttypeviewer-grid";
    private static final String APM_SMALL_COLOR_DIV_CLASS = "apmSmallColorDiv";
    private static final String PIE_CHART_DIV_ID = "webview-investigator-metriccounttypeviewer-piechart";
    private static final String PIE_CHART_PATHS_XPATH = "//div[@id='" + PIE_CHART_DIV_ID + "']/*[name()='svg']/*[name()='path' and @fill='%s']"; 
    private static final String PIE_CHART_TOOLTIP_ID = "webview-investigator-metric-browser-metric-count-pie-chart-tooltip";
    private static final String PIE_CHART_TOOLTIP_TEXT_XPATH = "//div[@id='" + PIE_CHART_TOOLTIP_ID + "']//span[contains(text(), '%s:')]";
    private static final String COMMON_PIE_CHART_TOOLTIP_TEXT_XPATH = "//div[@id='" + PIE_CHART_TOOLTIP_ID + "']//span[contains(text(), ':')]";
    private static final String TIME_WINDOW_INPUT_ID = "webview-timecontroller-timewindow-combobox-input";
    
    private static final int NUMBER_OF_CYCLES_FOR_CONCURRENT_REGRESSION_TESTING = 50;
    private static final int TOTAL_METRIC_UNDER_THIS_BRANCH_READ_TRIAL_NUMBER = 10;
    private static final int TABLE_REPARSE_TRIAL_NUMBER = 10;
    private static final int TOOLTIP_FIND_TRIAL_NUMBER = 10;
    
    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.BIG,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MetricCount"})
    @AlmId(329438)
    public void testHistoricalModeMetricCount() throws Exception {
    
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
        
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost, 
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        
        testRunner.testHistoricalModeMetricCount(METRIC_BROWSER_NODES.length);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.BIG,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MetricCount"})
    @AlmId(329448)
    public void testSwitchingBetweenLiveAndHistoricalModeMetricCount() throws Exception {
    
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
        
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost, 
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        
        testRunner.testSwitchLiveAndHistoricalModes(METRIC_BROWSER_NODES.length);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.BIG,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MetricCount"})
    @AlmId(455845)
    public void testConcurrentModificationExceptionOnMetricCountTab() throws Exception {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
        
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());

        testRunner.testOpeningMetricCountMultipleTimes();
    }
    
    public static void main(String[] args) throws Exception {
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, "tas-cz-nb2", 
            8080, "localhost", "", 9515, DesiredCapabilities.chrome());
//        testRunner.testOpeningMetricCountMultipleTimes();
//        testRunner.testHistoricalModeMetricCount(METRIC_BROWSER_NODES.length);
        testRunner.testSwitchLiveAndHistoricalModes(METRIC_BROWSER_NODES.length);
    }
    
    /**
     * Test runner class to make it possible to run the tests as a standard Java app. 
     * 
     * 
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    private static class TestRunner extends BaseTestRunner {

        /**
         * Constructor.
         * 
         * @param webViewUserName
         * @param webViewUserPassword
         * @param webViewHost
         * @param webViewPort
         * @param hubHost
         * @param hubPath
         * @param hubPort
         * @param desiredCapabilities
         */
        public TestRunner(String webViewUserName, String webViewUserPassword, String webViewHost,
            int webViewPort, String hubHost, String hubPath, int hubPort, DesiredCapabilities desiredCapabilities) {
            super(webViewUserName, webViewUserPassword, webViewHost, webViewPort, hubHost, 
                hubPath, hubPort, desiredCapabilities);
        }

        /**
         * Test for Rally's defect DE256023 regression.
         * ALM 455845.
         *  
         * @throws Exception
         */
        public void testOpeningMetricCountMultipleTimes() throws Exception {
            for (int i = 0; i < NUMBER_OF_CYCLES_FOR_CONCURRENT_REGRESSION_TESTING; i++) {
                LOGGER.info("++ Opening metric count, try: {}", (i + 1));
                testNavigateToMetricCount(METRIC_BROWSER_NODES.length);
            }
        }
        
        /**
         * ALM 329448.
         * @throws Exception 
         */
        public void testSwitchLiveAndHistoricalModes(int expandDepth) throws Exception {
            TimeFrameToTest liveTimeFrame = new TimeFrameToTest(TimeWindow.LIVE);
            List<TimeFrameToTest> timeFramesForTesting = new ArrayList<TimeFrameToTest>(TimeWindow.values().length * 8);
            for (TimeWindow timeWindow : TimeWindow.values()) {
                if (timeWindow.getTimeResolutions() != null) {
                    for (TimeResolution timeResolution : timeWindow.getTimeResolutions()) {
                        if (timeFramesForTesting.get(timeFramesForTesting.size() - 1).getTimeWindow() != TimeWindow.LIVE) {
                            timeFramesForTesting.add(liveTimeFrame);
                        } 
                        timeFramesForTesting.add(new TimeFrameToTest(timeWindow, timeResolution));
                    }
                } else if ("Live".equalsIgnoreCase(timeWindow.getText())) {
                    timeFramesForTesting.add(liveTimeFrame);
                }
            }

            try (WebViewUi ui = createWebViewUi()) {
                loginAndInitMetricBrowser(ui, expandDepth, true);
                
                try {
                    for (TimeFrameToTest timeFrame : timeFramesForTesting) {
                        ui.changeTimeWindow(timeFrame.timeWindow);
                        TimeUnit.SECONDS.sleep(1);
                        
                        if (timeFrame.timeResolution != null) {
                            ui.changeTimeResolution(timeFrame.timeResolution);
                            TimeUnit.SECONDS.sleep(1);
                        }
                        
                        ui.enablePolling(false);
                        //Switching back to live mode sometimes requires a long wait to get the numbers updated
                        TimeUnit.SECONDS.sleep(30);
                        testMetricCount(ui);
                        ui.enablePolling(true);
                    }
                } catch (Exception e) {
                    ui.takeScreenShot("switch-live-to-historical-mode-metric-count-exception-screenshot-");
                    throw e;
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred: ", e);
                throw e;
            }
        }
        
        /**
         * ALM 329438.
         * 
         * @param expandDepth
         * @throws Exception
         */
        public void testHistoricalModeMetricCount(int expandDepth) throws Exception {
            try (WebViewUi ui = createWebViewUi()) {
                loginAndInitMetricBrowser(ui, expandDepth, true);
                
                try {
                    for (TimeWindow timeWindow : TimeWindow.values()) {
                        ui.changeTimeWindow(timeWindow);
                        TimeUnit.SECONDS.sleep(1);
                        if (timeWindow.getTimeResolutions() != null) {
                            for (TimeResolution timeResolution : timeWindow.getTimeResolutions()) {
                                ui.changeTimeResolution(timeResolution);
                                TimeUnit.SECONDS.sleep(1);
                                ui.enablePolling(false);
                                TimeUnit.SECONDS.sleep(1);
    
                                testMetricCount(ui);
                                
                                ui.enablePolling(true);
    
                            }
                        } else if ("Live".equalsIgnoreCase(timeWindow.getText())) {
                            ui.enablePolling(false);
                            TimeUnit.SECONDS.sleep(1);
    
                            testMetricCount(ui);
                            
                            ui.enablePolling(true);
    
                        }
                    }
                } catch (Exception e) {
                    ui.takeScreenShot("historical-mode-metric-count-exception-screenshot-");
                    throw e;
                }
            } catch (Exception e) {
                
                LOGGER.error("Error occurred: ", e);
                throw e;
            }
        }
        
        private void testNavigateToMetricCount(int expandDepth) throws Exception {
            try (WebViewUi ui = createWebViewUi()) {
                loginAndInitMetricBrowser(ui, expandDepth, false);
                
                int totalMetricCount = 0;
                for (int i = 0; i < TOTAL_METRIC_UNDER_THIS_BRANCH_READ_TRIAL_NUMBER; i++) {
                    LOGGER.info("Trying to read total metric count under this branch, try number: {}", i + 1);
                    totalMetricCount = getTotalMetricCountFromLabel(ui);    
                    if (totalMetricCount > 0) {
                        break;
                    }
                    LOGGER.warn("---- Unexpected total metric count under this branch value is read, " + 
                        "sleeping for a while and retrying re-reading it again...");
                    TimeUnit.SECONDS.sleep(2);
                }
                
                if (totalMetricCount < 1) {
                    LOGGER.error("Well, we tried our best but it is still not what we wanted to get..");
                }
                assertTrue(totalMetricCount > 0);
            } catch (Exception e) {
                LOGGER.error("Error occurred: ", e);
                throw e;
            }
        }

        private void testMetricCount(WebViewUi ui) throws Exception {
            int totalMetricCount = 0;
            for (int i = 0; i < TOTAL_METRIC_UNDER_THIS_BRANCH_READ_TRIAL_NUMBER; i++) {
                LOGGER.info("Trying to read total metric count under this branch, try number: {}", i + 1);
                totalMetricCount = getTotalMetricCountFromLabel(ui);    
                if (totalMetricCount > 0) {
                    break;
                }
                LOGGER.warn("---- Unexpected total metric count under this branch value is read, " + 
                    "sleeping for a while and retrying re-reading it again...");
                ui.enablePolling(true);
                TimeUnit.SECONDS.sleep(10);
                ui.enablePolling(false);
                TimeUnit.SECONDS.sleep(2);
            }
            
            if (totalMetricCount < 1) {
                LOGGER.error("Well, we tried our best but it is still not what we wanted to get..");
            }
            assertTrue(totalMetricCount > 0);
            
            Map<String, List<MetricCountInfo>> metricCountInfoTable = getSeparateMetricCounts(ui);
            int calculatedTotalMetricCount = calculateTotalMetricCountFromSeparateCounts(metricCountInfoTable);
            LOGGER.info("Calculated metric count (from table rows): {}", calculatedTotalMetricCount);
            LOGGER.info("Total metric count under this branch: {}", totalMetricCount);
            assertEquals(totalMetricCount, calculatedTotalMetricCount);
            
            runPieChartToTableComparisonChecks(ui, metricCountInfoTable);
        }
        
        private WebViewUi loginAndInitMetricBrowser(WebViewUi ui, int expandDepth, boolean disablePolling) throws Exception {
            login(ui);
 
            ui.clickInvestigatorTab();
            
            if (expandDepth > METRIC_BROWSER_NODES.length) {
                expandDepth = METRIC_BROWSER_NODES.length;
            }
            
            for (int i = 0; i < expandDepth; i++) {
                String metricBrowserElemDivXpath = String.format(METRIC_BROWSER_ELEMENT_TEMPLATE, METRIC_BROWSER_NODES[i]);
                WebElement metricBrowserElement = ui.waitForWebElement(By.xpath(metricBrowserElemDivXpath));
                
                if (i < expandDepth - 1) {
                    WebElement plusImg = metricBrowserElement.findElement(By.xpath(metricBrowserElemDivXpath + EXPAND_PLUS_IMG_XPATH));
                    LOGGER.info("Expanding '{}' node", METRIC_BROWSER_NODES[i]);
                    plusImg.click();
                } else {
                    LOGGER.info("Clicking on '{}' node", METRIC_BROWSER_NODES[i]);
                    metricBrowserElement.click();
                }
            }
            
            WebElement metricCountTabElement = ui.waitForWebElement(By.xpath(METRIC_COUNT_TAB_XPATH));
            LOGGER.info("Clicking on 'Metric Count' tab: {}", metricCountTabElement);
            metricCountTabElement.click();

            if (disablePolling) {
                //Disable polling to fix the numbers at the current point
                ui.enablePolling(false);
            }

            TimeUnit.SECONDS.sleep(2);
            return ui;
        }
        
        private int calculateTotalMetricCountFromSeparateCounts(Map<String, List<MetricCountInfo>> metricCountInfoTable) {
            int totalMetricCount = 0;
            for (List<MetricCountInfo> infoList : metricCountInfoTable.values()) {
                for (MetricCountInfo info : infoList) {
                    totalMetricCount += info.metricCount;
                }
            }
            return totalMetricCount;
        }
        
        private int getTotalMetricCountFromLabel(WebViewUi ui) throws Exception {
            WebElement totalMetricsDiv = ui.waitForWebElement(By.xpath(TOTAL_METRICS_UNDER_THIS_BRANCH_XPATH));
            String totalMetricsText = totalMetricsDiv.getText();
            LOGGER.info("Total metrics under the selected branch text: {}", totalMetricsText);
            int columnInd = totalMetricsText.indexOf(':');
        
            assertTrue(columnInd >= 0);

            int totalMetricsCount = Integer.parseInt(totalMetricsText.substring(columnInd + 1).trim());
            
            LOGGER.info("Parsed total metrics count under this branch: {}", totalMetricsCount);
            return totalMetricsCount;
        }
        
        /**
         * Returns a map containing mappings of color code to metric count info object list.
         * Some metric count rows in the table can have the same color code assigned to them when their total metric count 
         * is relatively small comparing to other metrics. On the pie chart they can be shown as one single slice with 
         * the "Others: (num, percents)" tooltip.  
         * 
         * @param ui
         * @return
         * @throws Exception 
         */
        private Map<String, List<MetricCountInfo>> getSeparateMetricCounts(WebViewUi ui) throws Exception {
            WebElement metricCountPanelElement = ui.getWebElement(By.id(WEBVIEW_TYPE_VIEW_METRIC_COUNT_ID));
            
            for (int i = 0; i < TABLE_REPARSE_TRIAL_NUMBER; i++) {
                try {
                    ui.enablePolling(false);
                    TimeUnit.SECONDS.sleep(2);
                    
                    TableUi tableUi = new TableUi(ui, metricCountPanelElement, By.id(METRIC_COUNT_TYPE_VIEWER_GRID_ID));

                    Map<Integer, String> headerMap = tableUi.getIndexToHeaderTextMap();
                    //check header
                    for (int columnI = 0; columnI < EXPECTED_METRIC_COUNT_GRID_TABLE_COLUMN_NAMES.length; columnI++) {
                        String expectedColumn = EXPECTED_METRIC_COUNT_GRID_TABLE_COLUMN_NAMES[columnI];
                        String realColumn = headerMap.get(columnI);
                        assertEquals(realColumn, expectedColumn);
                    }
                    
                    int rowCount = tableUi.rowCount();

                    Map<String, List<MetricCountInfo>> metricCountInfoTableMap = new HashMap<String, List<MetricCountInfo>>(); 
                    for (int rowI = 0; rowI < rowCount; rowI++) {
                        MetricCountInfo metricCountInfo = MetricCountInfo.createFromRow(tableUi, rowI);
                        List<MetricCountInfo> metricCounts = metricCountInfoTableMap.get(metricCountInfo.colorCode);
                        if (metricCounts == null) {
                            metricCounts = new LinkedList<MetricCountInfo>();
                            metricCountInfoTableMap.put(metricCountInfo.colorCode, metricCounts);
                        }
                        metricCounts.add(metricCountInfo);
                        
                    }
                    return metricCountInfoTableMap;
                } catch (StaleElementReferenceException e) {
                    if (i == (TABLE_REPARSE_TRIAL_NUMBER - 1)) {
                        throw e;
                    }
                }
            }
            return null;
        }
        
        private void runPieChartToTableComparisonChecks(WebViewUi ui, Map<String, List<MetricCountInfo>> metricCountInfos) {
            int i = 1;
            for (List<MetricCountInfo> metricCountInfoList : metricCountInfos.values()) {
                LOGGER.info("====================== Table metric to Pie chart metric comparison: {} =====================", i);
                LOGGER.info("Metric Count Info List: {}", metricCountInfoList);
                MetricCountInfo metricCountInfo0 = metricCountInfoList.get(0);
                String colorCode = metricCountInfo0.colorCode; 
                

                boolean combinedTooltip = metricCountInfoList.size() > 1;
                MetricCountInfo metricCountToCompare = metricCountInfo0;
                if (combinedTooltip) {
                    LOGGER.info("Calculating summary count and percentages for metrics combined under single color: {}", metricCountToCompare.colorCode);
                    //There are multiple metrics which are assigned the same color in the table
                    //but there should be only one single sector on the pie chart with the same 
                    //color and the summarized metric count which should be equal to the sum 
                    //of all those same colored table row values.
                    metricCountToCompare = getSummaryMetricCountInfoForColor(metricCountInfoList);
                }

                LOGGER.info("Table metrics to compare: metric count={}, percentage of total={}", 
                    metricCountToCompare.metricCount, metricCountToCompare.percentOfTotal);

                String pieChartTooltipXpath = String.format(COMMON_PIE_CHART_TOOLTIP_TEXT_XPATH);
                LOGGER.info("XPath for searching for pie chart tooltip: {}", pieChartTooltipXpath);
                By tooltipBy = By.xpath(pieChartTooltipXpath);
                
                String svgPathByFillXpath = String.format(PIE_CHART_PATHS_XPATH, colorCode);
                LOGGER.info("XPath for searching for SVG path by fill: {}", svgPathByFillXpath);
                WebElement svgPathElem = ui.getWebElement(By.xpath(svgPathByFillXpath));
                assertNotNull(svgPathElem);

                for (int j = 0; j < TOOLTIP_FIND_TRIAL_NUMBER; j++) {
                    Wait<WebDriver> waitObj = new WebDriverWait(ui.getWebDriver(), 10);
                    try {
                        waitObj.until(new WaitForElementCondition(ui, svgPathElem, tooltipBy));    
                    } catch (Exception e) {
                        LOGGER.error("Could not find any tooltips:", e);
                        throw e;
                    }
                    
                    boolean metricCountsMatch = false;
                    boolean metricPercentsMatch = false;
                    
                    WebElement tooltipElement = ui.getWebElement(tooltipBy);
                    String tooltipText = tooltipElement.getText();
                    int colonInd = tooltipText.indexOf(':');
                    assertTrue(colonInd != -1);
                    String metricName = tooltipText.substring(0, colonInd).trim();
                    LOGGER.info("Tooltip metric name: {}", metricName);
    
                    if (metricName.equals(metricCountToCompare.resource) || metricName.equals(OTHERS_TOOLTIP_LABEL)) {
                        LOGGER.info("** Trying to match tooltip element containing text: {}", tooltipText);
    
                        String metricsFromTooltip = tooltipText.substring(colonInd + 1).trim();
                        Pattern pattern = Pattern
                            .compile("^\\((\\d+), *(\\d+\\.?\\d*)%\\)$", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(metricsFromTooltip);
                        assertTrue(matcher.find(), "Could not parse metrics from the tooltip: " + metricsFromTooltip);
                        int metricCount = Integer.parseInt(matcher.group(1));
                        float metricPercentage = Float.parseFloat(matcher.group(2));
                        LOGGER.info("Parsed metrics from the tooltip={}: metric count={}, percents of total={}", 
                            metricsFromTooltip, metricCount, metricPercentage);
    
                        metricCountsMatch = metricCount == metricCountToCompare.metricCount;
                        metricPercentsMatch = metricPercentage == metricCountToCompare.percentOfTotal;
    
                        if (!metricCountsMatch || !metricPercentsMatch) {
                            ui.takeScreenShot("pie-slice-metric-count-failed-assertion-screenshot-");
                        }
    
                        assertEquals(metricCount, metricCountToCompare.metricCount,
                            "Table and Pie Chart metric counts do not match!");
                        assertEquals(metricPercentage, metricCountToCompare.percentOfTotal,
                            "Table and Pie Chart metric percentages do not match!");
                        break;
                    } else {
                        LOGGER.warn("Seems like the web driver failed to select the right slice "
                            + "and we've got a wrong (probably, neighbour's) tooltip shown.");
                        if (j < TOOLTIP_FIND_TRIAL_NUMBER - 1) {
                            LOGGER.info("Trying to open a correct tooltip one more time..");
                        } else {
                            LOGGER.info("Ignoring and continuing further. For now we fail only when "
                                + "the metric name matched but the numbers did not.");
                        }
                    }
                }
            }
        }

        private MetricCountInfo getSummaryMetricCountInfoForColor(List<MetricCountInfo> metricCountInfoList) {
            int sumMetricCount = 0;
            float sumMetricPercents = 0f;
            String color = null;
            int totalCombined = 0;
            for (MetricCountInfo info : metricCountInfoList) {
                LOGGER.info("Combining metric count: {}", info);
                sumMetricCount += info.metricCount;
                sumMetricPercents += info.percentOfTotal;
                if (color == null) {
                    color = info.colorCode;
                }
                totalCombined++;
            }
            
            LOGGER.info("Total combined percents before rounding up: {}", sumMetricPercents);
            BigDecimal percentsBD = new BigDecimal(sumMetricPercents);
            percentsBD = percentsBD.setScale(2, BigDecimal.ROUND_HALF_UP);
            LOGGER.info("Total combined percents after rounding up: {}", percentsBD.floatValue());
            LOGGER.info("Totally combined {} metrics under color {}", totalCombined, color);
            return new MetricCountInfo(null, color, sumMetricCount, percentsBD.floatValue());
        }

    }

    /**
     * 
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    public static class TimeFrameToTest {
        private TimeWindow timeWindow;
        private TimeResolution timeResolution;

        public TimeFrameToTest(TimeWindow timeWindow, TimeResolution timeResolution) {
            this.timeWindow = timeWindow;
            this.timeResolution = timeResolution;
        }
        
        public TimeFrameToTest(TimeWindow timeWindow) {
            this.timeWindow = timeWindow;
        }

        /**
         * @return the timeWindow
         */
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        /**
         * @return the timeResolution
         */
        public TimeResolution getTimeResolution() {
            return timeResolution;
        }
        
    }
    
    /**
     * Class representing metric statistics grabbed from each row of the metric count table. 
     * 
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    public static class MetricCountInfo {
        private String resource;
        private String colorCode;
        private int metricCount;
        private float percentOfTotal;
        
        public MetricCountInfo() {
        }
        
        public MetricCountInfo(String resource, String colorCode, int metricCount,
            float percentOfTotal) {
            this.resource = resource;
            this.colorCode = colorCode;
            this.metricCount = metricCount;
            this.percentOfTotal = percentOfTotal;
        }


        /**
         * @return the resource
         */
        public String getResource() {
            return resource;
        }

        /**
         * @param resource the resource to set
         */
        public void setResource(String resource) {
            this.resource = resource;
        }

        /**
         * @return the colorCode
         */
        public String getColorCode() {
            return colorCode;
        }

        /**
         * @param colorCode the colorCode to set
         */
        public void setColorCode(String colorCode) {
            this.colorCode = colorCode;
        }

        /**
         * @return the metricCount
         */
        public int getMetricCount() {
            return metricCount;
        }

        /**
         * @param metricCount the metricCount to set
         */
        public void setMetricCount(int metricCount) {
            this.metricCount = metricCount;
        }

        /**
         * @return the percentOfTotal
         */
        public float getPercentOfTotal() {
            return percentOfTotal;
        }

        /**
         * @param percentOfTotal the percentOfTotal to set
         */
        public void setPercentOfTotal(float percentOfTotal) {
            this.percentOfTotal = percentOfTotal;
        }
        
        public static MetricCountInfo createFromRow(TableUi tableUi, int rowInd) {
            MetricCountInfo info = new MetricCountInfo();
            WebElement colorCell = tableUi.getCellElement(rowInd, 0);
            WebElement colorDiv = colorCell.findElement(By.className(APM_SMALL_COLOR_DIV_CLASS));
            
            //Parse style attribute instead of getting directly a CSS value as Selenium 
            //returns color codes with alpha channel set, though it might not be explicitly set 
            //in the original color code.
            String styleAttr = colorDiv.getAttribute("style");
            int bgColorInd = styleAttr.indexOf(BACKGROUND_COLOR_CSS_PROPERTY);
            assertTrue(bgColorInd != -1);
            
            styleAttr = styleAttr.substring(bgColorInd + BACKGROUND_COLOR_CSS_PROPERTY.length());
            int semiColumnInd = styleAttr.indexOf(';');
            assertTrue(semiColumnInd != -1);
            styleAttr = styleAttr.substring(0, semiColumnInd);
            
            info.colorCode = styleAttr.trim();
            assertNotNull(info.colorCode);
            info.resource = tableUi.getCellValue(rowInd, 1);
            assertNotNull(info.resource);
            info.metricCount = Integer.parseInt(tableUi.getCellValue(rowInd, 2));
            assertNotNull(info.metricCount);
            info.percentOfTotal = Float.parseFloat(tableUi.getCellValue(rowInd, 3));
            assertNotNull(info.percentOfTotal);
            LOGGER.info("Parsed metric count row: {}", info);
            return info;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "MetricCountInfo [resource=" + resource + ", colorCode=" + colorCode
                + ", metricCount=" + metricCount + ", percentOfTotal=" + percentOfTotal + "]";
        }
        
    }

}
