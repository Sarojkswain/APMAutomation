package com.ca.apm.tests.test;

import com.ca.apm.test.atc.common.Canvas;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.Notebook;
import com.ca.apm.test.atc.common.landing.Tile;
import com.ca.apm.tests.test.atc.UITest;
import com.ca.apm.tests.test.helpers.ErrorReport;
import com.ca.apm.tests.testbed.StandAloneSTTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.ca.apm.test.atc.common.element.WebElementWrapper.wrapElement;
import static com.ca.apm.tests.utils.Common.matchRegExAndLog;
import static java.lang.Integer.parseInt;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Transaction trace viewer ST tests.
 *
 * @author ahmal01
 */
public class TransactionTraceViewerSTTest extends UITest {
    private static final Logger log = LoggerFactory.getLogger(TransactionTraceViewerSTTest.class);

    @NotNull
    private static Collection<String> collectTextFromElements(Collection<WebElement> webElements) {
        Collection<String> durationTexts = new ArrayList<>(Collections2.transform(
            webElements, new Function<WebElement, String>() {
                @Override
                public String apply(@Nullable WebElement webElement) {
                    return webElement.getText();
                }
            }));

        Pattern pat = Pattern.compile("\\d+ms");
        for (String text : durationTexts) {
            Matcher matcher = pat.matcher(text);
            assertTrue(matcher.matches());
        }

        return durationTexts;
    }

    private static WebElement getTable(SearchContext gridTransactionTraces) {
        return gridTransactionTraces.findElement(
            By.cssSelector(
                "div.ui-grid-contents-wrapper div[role=grid] div.ui-grid-viewport[role=rowgroup]"
                    + " div.ui-grid-canvas"));
    }

    private static String changeDurationOrdering(UI ui, SearchContext tableHeaders) {
        WebElement durationTextElem = tableHeaders.findElement(
            By.xpath(".//div[@role='columnheader']/div[@role='button']/span[text()='Duration']"));
        final WebElement durationColumnHeaderElem = durationTextElem
            .findElement(By.xpath("./../.."));
        final String previousSort = durationColumnHeaderElem.getAttribute("aria-sort");
        new Actions(ui.getDriver())
            .moveToElement(durationTextElem)
            .click()
            .perform();
        Utils.waitForCondition(ui.getDriver(),
            new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(@Nullable WebDriver from) {
                    return !(durationColumnHeaderElem.getAttribute("aria-sort")
                        .equals(previousSort));
                }
            }, 20);
        return durationColumnHeaderElem.getAttribute("aria-sort");
    }

    /**
     * This method changes ordering to the given one by cycling through states.
     *
     * @param ui
     * @param tableHeaders
     * @param newOrdering  one of "ascending", "descending", "none"
     * @return the new ordering name
     */
    private static String changeDurationOrderingTo(UI ui, SearchContext tableHeaders,
        String newOrdering) {
        String lastOrdering;
        while (!((lastOrdering = changeDurationOrdering(ui, tableHeaders)).equals(newOrdering))) {
            log.info("Cycling through ordering. Last: {}", lastOrdering);
        }
        log.info("At last: {}", lastOrdering);
        return lastOrdering;
    }

    @NotNull
    private static List<Integer> extractDurationValues(Collection<String> durationTexts) {
        return new ArrayList<>(Collections2.transform(durationTexts,
            new Function<String, Integer>() {
                @Override
                public Integer apply(String from) {
                    from = from.substring(0, from.indexOf("ms"));
                    return parseInt(from);
                }
            }));
    }

    private static void verifyOrdering(String orderingStr, Ordering<Integer> ordering,
        UI ui, SearchContext table, SearchContext tableHeaders) {
        changeDurationOrderingTo(ui, tableHeaders, orderingStr);
        Collection<WebElement> durationTextElements
            = table.findElements(
            By.cssSelector("div[role=row] div.tt-grid-duration-cell span"));
        Collection<String> durationTexts = collectTextFromElements(durationTextElements);
        List<Integer> durationValues = extractDurationValues(durationTexts);
        assertTrue(ordering.isOrdered(durationValues),
            "durations should be ordered in " + orderingStr + " order");
    }

    @NotNull
    private static String parseAvgDurationLabel(WebElement avgLabel) {
        String avgText = avgLabel.getText();
        Matcher matcher = matchRegExAndLog("^Avg\\s+(\\d+)$", avgText);
        assertNotNull(matcher);
        String avgValueText = matcher.group(1);
        assertNotNull(avgValueText);
        return avgValueText;
    }

    private static WebElement extractComponentDetailsPanelValue(SearchContext componentDetailsPanel,
        String valueLabel) {
        return componentDetailsPanel.findElement(
            By.xpath(".//li/span[contains(@class,"
                + " 'transaction-trace-component-details-attribute-title')]"
                + "/descendant-or-self::span[starts-with(text(), '" + valueLabel + "')]"
                + "/ancestor::li"
                + "/span[contains(@class,"
                + " 'transaction-trace-component-details-attribute-value')]"));
    }

    @NotNull
    private void verifyTransactionTracesTable(UI ui, String boxName, SearchContext tableParent,
        SearchContext breadCrumbsParent) {
        PageElement transactionTraceViewer = wrapElement(tableParent.findElement(By.xpath("//*[contains(@id, 'transaction-trace-viewer')]")), ui);
        
        Utils.waitUntilVisible(ui.getDriver(), By.xpath("//*[contains(@id, 'transaction-trace-viewer')]"));
        PageElement transactionTraceListViewer
            = transactionTraceViewer.findElement(By.xpath("//*[contains(@id,'transaction-trace-list-viewer')]"));
    
        PageElement transactionTraceListFilterBar
            = transactionTraceViewer.findElement(By.xpath("//*[contains(@id,'transaction-trace-list-filter-bar')]"));
    
        PageElement gridTransactionTraces
            = transactionTraceListViewer.findElement(By.xpath("//*[contains(@id,'gridTransactionTraces')]"));
        
        WebElement table = getTable(gridTransactionTraces);
    
        WebElement tableHeaders = gridTransactionTraces.findElement(
            By.cssSelector("div.ui-grid-contents-wrapper div[role=grid] div[role=rowgroup]"
                + " div.ui-grid-top-panel div.ui-grid-header-viewport div.ui-grid-header-canvas"));
    
        Utils.waitForCondition(ui.getDriver(),
            ExpectedConditions.visibilityOfNestedElementsLocatedBy(table,
                By.cssSelector("div[role=row]")), 120);
          
        verifyOrdering("descending", Ordering.<Integer>natural().reverse(), ui, table,
            tableHeaders);
        verifyOrdering("ascending", Ordering.<Integer>natural(), ui, table, tableHeaders);
    
        WebElement breadcrumbsHeader = breadCrumbsParent.findElement(
              By.xpath("//*[contains(@id,'transaction-trace-breadcrumbs-panel')]"));
    
          
        verifyBreadcrumbs(boxName, /*durationValues.size()*/-1, breadcrumbsHeader);
    
        WebElement oneDurationBar
            = table.findElement(By.cssSelector("div.tt-instance-bar"));
        new Actions(ui.getDriver())
            .moveToElement(oneDurationBar)
            .click()
            .perform();

        ui.waitUntilVisible(By.xpath("//*[contains(@id, 'transaction-trace-viewer')]"));

        transactionTraceViewer = wrapElement(
            tableParent.findElement(By.xpath("//*[contains(@id, 'transaction-trace-viewer')]")), ui);
        WebElement transactionTraceWeddingCakeViewer =
            transactionTraceViewer.findElement(By.xpath("//*[contains(@id, 'transaction-trace-wedding-cake-viewer')]"));
        WebElement segment0 = transactionTraceWeddingCakeViewer.findElement(By.xpath("//*[contains(@id, 'tt-segment-components-header')]"));
        
        new Actions(ui.getDriver())
            .moveToElement(segment0)
            .click()
            .perform();
        
        PageElement componentPanel = 
                wrapElement(segment0.findElement(By.xpath("//*[contains(@id, 'tt-segment-components-panel')]")), ui);
        
        PageElement componentLabel = 
                componentPanel.findElement(By.xpath("//*[contains(@class, 'tt-component-name')]"));

        componentLabel.scrollIntoView();
        final String componentLabelText = componentLabel.getText();
        log.info("component text: >{}<", componentLabelText);

        Matcher matcher = matchRegExAndLog("^(.+)\\s+\\([^\\d]+(\\d+)\\s*ms\\).*$",
            componentLabelText);
        assertNotNull(matcher);
        final String componentLabelPath = matcher.group(1);
        final String componentLabelDuration = matcher.group(2);

        new Actions(ui.getDriver())
            .moveToElement(componentLabel, 2, 2)
            .click()
            .perform();
        WebElement componentDetailsPanel = Utils.waitUntilVisible(ui.getDriver(), 
                                                                  By.xpath("//*[contains(@id, 'transaction-trace-component-details-panel')]"));

        WebElement pathValueElement = extractComponentDetailsPanelValue(componentDetailsPanel, "Path:");
        
        String pathValueText = pathValueElement.getText();
        log.info("path value text: >{}<", pathValueText);
        assertEquals(pathValueText, componentLabelPath);

        WebElement durationValueElement = extractComponentDetailsPanelValue(componentDetailsPanel,
            "Duration:");
        String durationValueText = durationValueElement.getText();
        matcher = matchRegExAndLog("^(\\d+)\\s*ms$", durationValueText);
        assertNotNull(matcher);
        assertEquals(matcher.group(1), componentLabelDuration);

        WebElement componentDetailsPanelCloseButton = componentDetailsPanel.findElement(
            By.xpath("//*[contains(@id, 'transaction-trace-component-details-panel')]"));
        new Actions(ui.getDriver())
            .moveToElement(componentDetailsPanelCloseButton)
            .click()
            .perform();
    }

       private void verifyBreadcrumbs(String boxName, int durationBarsCount,
        SearchContext breadcrumbsContainer) {
        WebElement breadCrumbsTitle = breadcrumbsContainer.findElement(By.xpath("//*[contains(@class, 'transaction-trace-title-breadcrumb')]"));
        assertEquals(breadCrumbsTitle.getText(), boxName);

        List<WebElement> breadCrumbs = breadcrumbsContainer.findElements(By.xpath("//*[contains(@class, 'transaction-trace-breadcrumb breadcrumb-link')]"));
        assertEquals(breadCrumbs.size(), 1);
        
        WebElement transactionTraceCountBreadcrumb = breadCrumbs.get(0);
          
        Matcher matcher;
        assertNotNull(matcher = matchRegExAndLog("^(\\d+) Business Transactions$",
            transactionTraceCountBreadcrumb.getText()));
        int matchedNumber = parseInt(matcher.group(1));
        // XXX: Unfortunately, the table is "virtualized" and the number of rows that we get
        // from the table is roughly equal to the number of rows that are visible and not equal
        // to the total number of transaction traces. Hence this test cannot be made by simply
        // counting divs and comparing with the breadcrumb number.
        //
        //assertEquals(matchedNumber, durationBarsCount,
        //    "breadcrumb transaction traces count should be equal to number of displayed "
        //        + "transaction trace bars");
    }


    @NotNull
    private Canvas initialMapNavigation(UI ui, String boxName) {
        ui.getTimeline().turnOffLiveMode();

        // Extend time span to make sure we get some transaction traces instead of none.

        Timeline timeline = ui.getTimeline();
        timeline.openStartTimeCalendar();
        PageElement decreaseHourHandler = timeline.getStartTimeCalendarHourDecreaseBtn();
        for (int i = 0; i != 6; ++i) {
            decreaseHourHandler.click();
            Utils.sleep(100);
        }
        timeline.calendarApply();

        // Select node.

        Canvas canvas = ui.getCanvas();
        canvas.waitForUpdate();
        canvas.getCtrl().fitAllToView();
        canvas.selectNodeByName(boxName);

        return canvas;
    }

    @Tas(testBeds = @TestBed(name = StandAloneSTTestBed.class,
        executeOn = StandAloneSTTestBed.SA_MASTER), size = SizeType.BIG, owner = "ahmal01")
    @Parameters({"browserType"})
    @Test(groups = {"ttviewer_ui"}, threadPoolSize = 20, invocationCount = 20)
    public void testInMapViewThroughComponentViewLink(String browserType) throws Exception {
        
        log.info("Browser Type: " + browserType);
        while(true){
            prepareATCUI(browserType);
        UI ui = getUI();
        try {
            ui.login();
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Login into ATC has failed. Exception: {0}");
        }

        LeftNavigationPanel nav = ui.getLeftNavigationPanel();
        nav.goToMapViewPage();

        final String perspective = "Hostname";
        try {
            if (!ui.getPerspectivesControl().isPerspectiveActive(perspective)) {
                ui.getPerspectivesControl().selectPerspectiveByName(perspective);
            }
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to change perspective to {1}. Exception: {0}", perspective);
        }

        try {
            final String boxName = "PipeOrgan Application";
            Canvas canvas = initialMapNavigation(ui, boxName);

            WebElement transactionTracePanel = Utils.waitForCondition(ui.getDriver(),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@id, 'TransactionTracePanel')]")));
            PageElement transactionTracesLink = wrapElement(transactionTracePanel.findElement(
                By.cssSelector("div.transaction-trace-summary-panel li.parentTrace a")), ui);
            transactionTracesLink.scrollIntoView();
            new Actions(ui.getDriver())
                .moveToElement(transactionTracesLink)
                .click()
                .perform();

            PageElement bottomDrawer = wrapElement(
                Utils.waitUntilVisible(ui.getDriver(), By.className("bottom-drawer")), ui);
            bottomDrawer.scrollIntoView();

            WebElement bottomDrawerContent = Utils.waitUntilVisible(
                ui.getDriver(), By.xpath("//*[contains(@class, 'bottom-drawer-content')]"));
            WebElement bottomDrawerHeader
                = bottomDrawer.findElement(By.xpath("//*[contains(@id, 'bottom-drawer-header-content')]"));
            verifyTransactionTracesTable(ui, boxName, bottomDrawerContent, bottomDrawerHeader);

            PageElement minimizeIcon = bottomDrawer.findElement(
                By.cssSelector("a#bottomDrawerMinimizeIcon"));
            minimizeIcon.click();

            canvas.clickToUnusedPlaceInCanvas();
            Utils.waitForCondition(ui.getDriver(), ExpectedConditions.stalenessOf(bottomDrawer));

            ui.logout();
            Utils.sleep(5000);
            cleanUI(ui);
        } catch (Throwable e) {
            log.error("Test failed. Exception: {0}" + e.getMessage());
            cleanUI(ui);
            this.testInMapViewThroughComponentViewLink(browserType);
        }
        Utils.sleep(60000);
    }
    }

    @Tas(testBeds = @TestBed(name = StandAloneSTTestBed.class,
        executeOn = StandAloneSTTestBed.SA_MASTER), size = SizeType.BIG, owner = "ahmal01")
    @Parameters({"browserType"})
    @Test(groups = {"ttviewer_ui"}, threadPoolSize = 20, invocationCount = 20)
    public void testInExperienceView(String browserType) throws Exception {
        
        while(true){
            prepareATCUI(browserType);
        UI ui = getUI();
        try {
            ui.login();
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Login into ATC has failed. Exception: {0}");
        }

        LeftNavigationPanel nav = ui.getLeftNavigationPanel();
        nav.goToHomePage();

        try {
            LandingPage landingPage = ui.getLandingPage();
            Tile tile = landingPage.getNthTile(0);
            tile.openNotebook();
            Notebook notebook = ui.getNotebook();
            Canvas canvas = notebook.getCanvas();

            final String boxName = "User_Story_Updated via iOS 9";
            
            canvas.selectNodeByName(boxName);
            canvas.getCtrl().fitSelectedToView();

            WebElement transactionTracesTabHandle = Utils.waitForCondition(ui.getDriver(),
                ExpectedConditions.visibilityOfElementLocated(
                   By.xpath("//a[contains(text(), 'Business Transactions')]")),
                60);

            new Actions(ui.getDriver())
                .moveToElement(transactionTracesTabHandle)
                .click()
                .perform();

            PageElement transactionTraceTab = wrapElement(
                Utils.waitUntilVisible(ui.getDriver(), By.className("tab-content")), ui);

            verifyTransactionTracesTable(ui, boxName, transactionTraceTab, transactionTraceTab);

            canvas.getCanvas().scrollToTop();
            canvas.clickToUnusedPlaceInCanvas();
            
            Utils.waitForCondition(ui.getDriver(), 
                                   ExpectedConditions.invisibilityOfAllElements(Arrays.<WebElement>asList(transactionTraceTab)));
            ui.logout();
            Utils.sleep(5000);
            cleanUI(ui);
            Utils.sleep(60000);
        } catch (Throwable e) {
            log.error("Test failed. Exception: {0}" + e.getMessage());
            cleanUI(ui);
            this.testInExperienceView(browserType);
        }
        
    }
  }
    private void cleanUI(UI ui){
        if (ui != null) {
            ui.cleanup();
            try {
                ui.getDriver().quit();
            } catch(Exception e) {
                throw ErrorReport.logExceptionAndWrapFmt (log, e, "Failed to quite browser.");
            }
        }
    }
} 
