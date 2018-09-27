package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WaitForChartTooltipCondition;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.base.Splitter;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author haiva01
 */
public class MinMaxInChartTest extends BaseWebViewTest {
    private static final Logger log = LoggerFactory.getLogger(MinMaxInChartTest.class);
    private static final String CHART_MINMAX_SVG_PATH_XPATH
        = ".//svg:svg//svg:path[@pathid='webview-investigator-linechart-minmax-1']";
    private static final String CHART_SERIES_1_SVG_PATH_XPATH
        = ".//svg:svg//svg:path[@pathid='webview-investigator-linechart-lineseries-1']";
    private static final String WEBVIEW_HOME_PAGE_BT_ART = "webview-HomePage-BT-ART";

    private static final String[] HOME_TAB_CHARTS_IDS = {
        WEBVIEW_HOME_PAGE_BT_ART, "webview-HomePage-APP-ART", "webview-HomePage-BT-ERR",
        "webview-HomePage-APP-ERR"};

    private static final String CONSOLE_DASHBOARD_NAME
        = "XML TypeViewer Dashboard (A-Webview-FunctionalTest-Module in *SuperDomain*)";
    private static final String WEBVIEW_INVESTIGATOR_LINECHART_CONTAINER
        = "webview-investigator-linechart-container";

    private void checkChartMinMaxInvisibility(WebViewUi ui, SearchContext chart) {
        WebElement chartMinMaxPath = ui.getWebElementOrNull(chart, By.xpath(
            CHART_MINMAX_SVG_PATH_XPATH));
        String visibilityAttribute = chartMinMaxPath != null
            ? chartMinMaxPath.getAttribute("visibility") : null;
        assertTrue(chartMinMaxPath == null
                || (visibilityAttribute != null && visibilityAttribute.equals("hidden")),
            "chart's min/max should be invisible");
    }

    private boolean getMinMaxVisibility(WebViewUi ui, SearchContext chart) {
        WebElement chartMinMaxPath = ui.getWebElementOrNull(chart, By.xpath(
            CHART_MINMAX_SVG_PATH_XPATH));
        String visibilityAttribute = chartMinMaxPath != null
            ? chartMinMaxPath.getAttribute("visibility")
            : null;
        return chartMinMaxPath != null
            && (visibilityAttribute == null || visibilityAttribute.equals("visible"));
    }

    private Point parseFirstMinMaxPoint(WebViewUi ui, SearchContext chart) {
        return parseSomeSvgPathPoint(ui, chart, By.xpath(CHART_MINMAX_SVG_PATH_XPATH));
    }

    private Point parseSomeSvgPathPoint(WebViewUi ui, SearchContext chart, By selector) {
        final WebElement minMaxPath = ui.getWebElement(chart, selector);
        String pathData = minMaxPath.getAttribute("d");
        Pattern pattern = Pattern
            .compile("[L] *(\\d+\\.?\\d*)[, ] *(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pathData);
        assertTrue(matcher.find(), "Regex should match.");
        final int x = Math.round(Float.parseFloat(matcher.group(1)));
        final int y = Math.round(Float.parseFloat(matcher.group(2)));
        return new Point(x, y);
    }

    private Map<String, String> urlFragmentTokens(String urlStr) throws URISyntaxException {
        URI uri = new URI(urlStr);
        String frag = uri.getFragment();
        Map<String, String> tokensMap = new LinkedHashMap<>(10);
        for (String kvStr : Splitter.on(';').split(frag)) {
            String[] kv = StringUtils.splitPreserveAllTokens(kvStr, "=", 2);
            switch (kv.length) {
                case 2:
                    tokensMap.put(kv[0], kv[1]);
                    break;

                case 1:
                    tokensMap.put(kv[0], null);
                    break;

                default: {
                    String msg = MessageFormat
                        .format("Unexpected parsing result for token >{0}<", kvStr);
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        }
        return tokensMap;
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450859)
    public void minMaxTestHome() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", "Admin", "");

            // get Home tab chart and display menu and enable Min/Max showing.

            ui.clickHomeTab();

            // XXX: This is a hack for development that can be removed once charts are working in
            // live mode.
            //ui.changeTimeWindow(WebViewUi.TimeWindow.EIGHT_MINUTES);
            //TimeUnit.SECONDS.sleep(2);
            // XXX: ^^^

            // Check that Min/Max visibility is currently off for all 4 charts on Home tab.

            for (String chartId : HOME_TAB_CHARTS_IDS) {
                final WebElement chart = ui.getWebElement(By.id(chartId));
                checkChartMinMaxInvisibility(ui, chart);
            }

            // Toggle Min/Max visibility for webview-HomePage-BT-ART.

            WebElement btArtChart = ui.getWebElement(By.id(WEBVIEW_HOME_PAGE_BT_ART));
            ui.clickMinMaxMenuButton(btArtChart);

            // Check that minmax SVG path is visible by inspecting visibility attribute.

            verifyMinMaxVisibility(ui, btArtChart);

            // Check that all remaining 3 charts still do not show any Min/Max.

            for (String chartId : HOME_TAB_CHARTS_IDS) {
                if (chartId.equals(WEBVIEW_HOME_PAGE_BT_ART)) {
                    continue;
                }
                final WebElement chart = ui.getWebElement(By.id(chartId));
                checkChartMinMaxInvisibility(ui, chart);
            }

            // Toggle Min/Max visibility back off.

            ui.clickMinMaxMenuButton(btArtChart);

            // Check that Min/Max visibility is currently off for all 4 charts on Home tab.

            for (String chartId : HOME_TAB_CHARTS_IDS) {
                final WebElement chart = ui.getWebElement(By.id(chartId));
                checkChartMinMaxInvisibility(ui, chart);
            }

            // Check URL and make sure it does not contain smm token.

            String urlStr = ui.getCurrentUrl();
            Map<String, String> tokensMap = urlFragmentTokens(urlStr);
            assertFalse(tokensMap.containsKey("smm"));
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450893)
    public void minMaxAfterJumpFromConsolePageTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", "Admin", "");

            // Get console tab.

            ui.clickConsoleTab();

            // Select desired dashboard to show.

            ui.selectConsoleDashboard(CONSOLE_DASHBOARD_NAME);

            // Wait for the dashboard to appear.

            WebElement anchor = ui.waitForWebElement(
                By.xpath("//div[@title='Harvest, Smartstor, and GC Durations']"));

            // Find chart.

            WebElement chart = ui.getWebElement(anchor,
                By.xpath("./..//div[@id='webview-investigator-linechart-container']"));
            WebElement innerChart = ui.getWebElement(chart,
                By.xpath(".//div[@id='webview-investigator-linechart-chart']"));

            // Turn on Min/Max visualization.

            ui.clickMinMaxMenuButton(chart);
            verifyMinMaxVisibility(ui, chart);

            Collection<WebElement> allCharts = ui
                .getWebElements(By.id(WEBVIEW_INVESTIGATOR_LINECHART_CONTAINER));
            log.debug("Found {} charts.", allCharts.size());
            assertEquals(allCharts.size(), 8, "Expecting 8 charts here.");
            int minMaxEnabled = 0;
            for (WebElement webElement : allCharts) {
                if (getMinMaxVisibility(ui, webElement)) {
                    ++minMaxEnabled;
                }
            }
            assertEquals(minMaxEnabled, 1, "Min/Max should be enabled only for one chart.");

            // Disable polling so that the chart stops moving.

            ui.enablePolling(false);

            // Check that page URL does not contain smm token.

            String urlStr = ui.getCurrentUrl();
            Map<String, String> tokensMap = urlFragmentTokens(urlStr);
            assertFalse(tokensMap.containsKey("smm"));

            // Get point to hover above to get tooltip.

            Point point = parseFirstMinMaxPoint(ui, chart);

            // Hover over point to get tooltip.

            Wait<WebDriver> waitObj = new WebDriverWait(ui.getWebDriver(), 10);
            waitObj.until(
                new WaitForChartTooltipCondition(ui, innerChart, point.getX(), point.getY()));

            // Click on link in tooltip.

            WebElement link = ui
                .getWebElement(By.xpath("//a[@id='webview-investigator-linechart-tooltip']"));
            ui.clickByJs(link);

            // Restore page polling.

            ui.enablePolling(true);

            // Wait for the chart to render.

            WebElement investigatorChart = ui.waitForWebElement(
                By.id("webview-investigator-thornhillpreviewdrawingtypeviewer-FigureContainer"));
            verifyMinMaxVisibility(ui, investigatorChart);

            tokensMap = urlFragmentTokens(ui.getCurrentUrl());
            assertTrue(tokensMap.containsKey("smm"));
            assertTrue(MapUtils.getBooleanValue(tokensMap, "smm", false));

            // Get back to Console tab.

            ui.clickConsoleTab();

            // Check all charts have Min/Max turned off.

            ui.waitForWebElement(By.id(WEBVIEW_INVESTIGATOR_LINECHART_CONTAINER));
            allCharts = ui
                .getWebElements(By.id(WEBVIEW_INVESTIGATOR_LINECHART_CONTAINER));
            log.debug("Found {} charts.", allCharts.size());
            assertEquals(allCharts.size(), 8, "Expecting 8 charts here.");
            minMaxEnabled = 0;
            for (WebElement webElement : allCharts) {
                if (getMinMaxVisibility(ui, webElement)) {
                    ++minMaxEnabled;
                }
            }
            assertEquals(minMaxEnabled, 0, "No chart should have min/max enabled.");

            // Find chart.

            innerChart = ui.getWebElement(allCharts.iterator().next(),
                By.xpath(".//div[@id='webview-investigator-linechart-chart']"));

            // Get point to hover above.

            ui.enablePolling(false);

            point = parseSomeSvgPathPoint(ui, innerChart, By.xpath(
                "./svg:svg//svg:path[@pathid='webview-investigator-linechart-lineseries-1']"));

            // Hover over point to get tooltip.

            waitObj = new WebDriverWait(ui.getWebDriver(), 10);
            waitObj.until(
                new WaitForChartTooltipCondition(ui, innerChart, point.getX(), point.getY()));

            // Click on link in tooltip.

            link = ui
                .getWebElement(By.xpath("//a[@id='webview-investigator-linechart-tooltip']"));
            ui.clickByJs(link);

            // Re-enable polling.

            ui.enablePolling(true);

            // Check Min/Max invisibility.

            investigatorChart = ui.waitForWebElement(
                By.id("webview-investigator-thornhillpreviewdrawingtypeviewer-FigureContainer"));
            checkChartMinMaxInvisibility(ui, investigatorChart);

            // Check that smm token is present and set to false.

            tokensMap = urlFragmentTokens(ui.getCurrentUrl());
            assertTrue(tokensMap.containsKey("smm"));
            assertFalse(MapUtils.getBooleanValue(tokensMap, "smm", false));
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(450900)
    public void minMaxAfterJumpFromManagementPageTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", "Admin", "");

            // Go to Management tab.

            ui.clickManagementTab();

            // Navigate tree.

            final String[] treeNodes = {
                "management-tree_*SuperDomain*",
                "management-tree_*SuperDomain*|Management Modules",
                "management-tree_*SuperDomain*|Management Modules|TestSample",
                "management-tree_*SuperDomain*|Management Modules|TestSample|Metric Groupings",
                "management-tree_*SuperDomain*|Management Modules|TestSample"
                    + "|Metric Groupings|Backend Average Response Times"};
            ui.selectTreeNode(treeNodes);

            // Wait for chart to appear.

            WebElement chart = ui
                .waitForWebElement(By.id(WEBVIEW_INVESTIGATOR_LINECHART_CONTAINER));
            WebElement innerChart = ui.getWebElement(chart,
                By.xpath(".//div[@id='webview-investigator-linechart-chart']"));

            // Turn on Min/Max visualization.

            ui.clickMinMaxMenuButton(chart);

            // Check that Min/Max bars are visible.

            verifyMinMaxVisibility(ui, chart);

            // Check that page URL does not contain smm token.

            String urlStr = ui.getCurrentUrl();
            Map<String, String> tokensMap = urlFragmentTokens(urlStr);
            assertFalse(tokensMap.containsKey("smm"));

            // Disable polling so that the chart stops moving.

            ui.enablePolling(false);

            // Get point to hover above to get tooltip.

            Point point = parseFirstMinMaxPoint(ui, chart);

            // Hover over point to get tooltip.

            Wait<WebDriver> waitObj = new WebDriverWait(ui.getWebDriver(), 10);
            waitObj.until(
                new WaitForChartTooltipCondition(ui, innerChart, point.getX(), point.getY()));

            // Click on link in tooltip.

            WebElement link = ui
                .getWebElement(By.xpath("//a[@id='webview-investigator-linechart-tooltip']"));
            ui.clickByJs(link);

            // Restore page polling.

            ui.enablePolling(true);

            // Wait for the chart to render.

            WebElement investigatorChart = ui.waitForWebElement(
                By.id("webview-investigator-thornhillpreviewdrawingtypeviewer-FigureContainer"));
            verifyMinMaxVisibility(ui, investigatorChart);

            tokensMap = urlFragmentTokens(ui.getCurrentUrl());
            assertTrue(tokensMap.containsKey("smm"));
            assertTrue(MapUtils.getBooleanValue(tokensMap, "smm", false));

            // Go back to Management tab.

            ui.clickManagementTab();

            // Navigate tree.

            ui.selectTreeNode(treeNodes);

            // Wait for chart to appear.

            chart = ui
                .waitForWebElement(By.id(WEBVIEW_INVESTIGATOR_LINECHART_CONTAINER));
            innerChart = ui.getWebElement(chart,
                By.xpath(".//div[@id='webview-investigator-linechart-chart']"));

            // Disable polling so that the chart stops moving.

            ui.enablePolling(false);

            // Check that Min/Max bars are NOT visible.

            assertFalse(getMinMaxVisibility(ui, chart));

            // Get point to hover above to get tooltip.

            point = parseSomeSvgPathPoint(ui, chart, By.xpath(CHART_SERIES_1_SVG_PATH_XPATH));

            // Hover over point to get tooltip.

            waitObj = new WebDriverWait(ui.getWebDriver(), 10);
            waitObj.until(
                new WaitForChartTooltipCondition(ui, innerChart, point.getX(), point.getY()));

            // Click on link in tooltip.

            link = ui
                .getWebElement(By.xpath("//a[@id='webview-investigator-linechart-tooltip']"));
            ui.clickByJs(link);

            // Restore page polling.

            ui.enablePolling(true);

            // Wait for the chart to render.

            investigatorChart = ui.waitForWebElement(
                By.id("webview-investigator-thornhillpreviewdrawingtypeviewer-FigureContainer"));

            // Check that Min/Max bars are turned off and that smm token is set to false.

            assertFalse(getMinMaxVisibility(ui, investigatorChart));

            tokensMap = urlFragmentTokens(ui.getCurrentUrl());
            assertTrue(tokensMap.containsKey("smm"));
            assertFalse(MapUtils.getBooleanValue(tokensMap, "smm", true));

        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    private void verifyMinMaxVisibility(WebViewUi ui, WebElement chart) {
        try {
            ui.waitFor(
                ExpectedConditions.presenceOfNestedElementLocatedBy(
                    chart, By.xpath(CHART_MINMAX_SVG_PATH_XPATH)));
            assertTrue(getMinMaxVisibility(ui, chart));
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT"})
    @AlmId(344506)
    public void xmlTypeViewTest() {
        // This test in ALM requires creating new dashboard in Workstation and verifying it is
        // working in WebView. Since the test bed already has a custom dashboard that is used in
        // the other test, we just re-use this existing test for this as well.
        minMaxAfterJumpFromConsolePageTest();
    }
}
