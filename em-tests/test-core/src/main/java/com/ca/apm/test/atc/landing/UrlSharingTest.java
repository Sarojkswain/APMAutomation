package com.ca.apm.test.atc.landing;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.UrlUtils;
import com.ca.apm.test.atc.common.landing.*;
import com.ca.apm.test.atc.common.landing.SummaryPanel.TimeRange;
import com.ca.apm.test.atc.common.landing.Tile.ChartType;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.*;

public class UrlSharingTest extends UITest {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private UI ui;
    private LandingPage landingPage;
    
    private final String TILE_TO_DRILL_DOWN_L1 = "Experiences in";
        
    private void init() throws Exception {
        ui = getUI();
        ui.login();
        landingPage = ui.getLandingPage();
        landingPage.waitForTilesToLoad(true);
    }
    
    @Test
    public void testLiveMode() throws Exception {
        init();
        
        SummaryPanel sumPanel = landingPage.getSummaryPanel();
        
        // "Custom range" is historic
        sumPanel.setTimeRange(TimeRange.CUSTOM_RANGE);
        
        // "Last X hours" is live
        sumPanel.setTimeRange(TimeRange.LAST_12_HOURS);
        
        String url = ui.getCurrentUrl();
        String modeParamTab1 = UrlUtils.getQueryStringParam(url, "m");
        assertEquals(modeParamTab1, "L");
        
        ui.logout();
        ui.login(Role.ADMIN, url, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        assertNotNull(sumPanel.getSelectedTimeRange());
        assertTrue(sumPanel.getSelectedTimeRange().isLive());
        
        sumPanel.setTimeRange(TimeRange.CUSTOM_RANGE);
        
        url = ui.getCurrentUrl();
        modeParamTab1 = UrlUtils.getQueryStringParam(url, "m");
        assertEquals(modeParamTab1, "H");
        
        ui.logout();
        ui.login(Role.ADMIN, url, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        assertNotNull(sumPanel.getSelectedTimeRange());
        assertFalse(sumPanel.getSelectedTimeRange().isLive());
    }
    
    @Test
    public void testTimeRange() throws Exception {
        init();
        
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        summaryPanel.setTimeRange(SummaryPanel.TimeRange.LAST_6_HOURS);
        landingPage.waitForTilesToLoad();
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        assertEquals(summaryPanel.getSelectedTimeRange(), SummaryPanel.TimeRange.LAST_6_HOURS);
    }
    
    @Test
    public void testPoorExperienceFilter() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        landingPage.cancelViewFilter();
        
        String viewParam1 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "view");
        List<String> tiles1 = landingPage.getTileNames();
        
        landingPage.getSummaryPanel().filterPoorTransactions();

        assertTrue(landingPage.isFilteredViewOn(),
            "Filtered view is not active, there are probably no poor transactions at all.");
        
        String url = ui.getCurrentUrl();
        String viewParam2 = UrlUtils.getQueryStringParam(url, "view");
        assertNotEquals(viewParam1, viewParam2);

        List<String> tiles2 = landingPage.getTileNames();
        assertFalse(new HashSet<String>(tiles1).equals(new HashSet<String>(tiles2)));
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        assertTrue(landingPage.isFilteredViewOn());
        
        String viewParam3 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "view");
        assertEquals(viewParam2, viewParam3);
        
        List<String> tiles3 = landingPage.getTileNames();
        assertTrue(new HashSet<String>(tiles2).equals(new HashSet<String>(tiles3)));
    }
    
    @Test
    public void testSlowTransactionsFilter() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        summaryPanel.expandPanel();
        if (summaryPanel.getSlowTransactionsCount() == 0) {
            logger.warn("Slow transactions not found, skipping test");
            return;
        }
        
        landingPage.cancelViewFilter();
        
        String viewParam1 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "view");
        List<String> tiles1 = landingPage.getTileNames();
        
        summaryPanel.filterSlowTransactions();

        assertTrue(landingPage.isFilteredViewOn(),
            "Filtered view is not active, there are probably no slow transactions at all.");
        
        String url = ui.getCurrentUrl();
        String viewParam2 = UrlUtils.getQueryStringParam(url, "view");
        assertNotEquals(viewParam1, viewParam2);

        List<String> tiles2 = landingPage.getTileNames();
        assertFalse(new HashSet<String>(tiles1).equals(new HashSet<String>(tiles2)));
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        assertTrue(landingPage.isFilteredViewOn());
        
        String viewParam3 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "view");
        assertEquals(viewParam2, viewParam3);
        
        List<String> tiles3 = landingPage.getTileNames();
        assertTrue(new HashSet<String>(tiles2).equals(new HashSet<String>(tiles3)));
    }
    
    @Test(groups = "failing")
    public void testFailedTransactionsFilter() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        landingPage.cancelViewFilter();
        
        String viewParam1 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "view");
        List<String> tiles1 = landingPage.getTileNames();
        
        landingPage.getSummaryPanel().filterFailedTransactions();
        
        assertTrue(landingPage.isFilteredViewOn(),
            "Filtered view is not active, there are probably no failed transactions at all.");
        
        String url = ui.getCurrentUrl();
        String viewParam2 = UrlUtils.getQueryStringParam(url, "view");
        assertNotEquals(viewParam1, viewParam2);

        List<String> tiles2 = landingPage.getTileNames();
        assertFalse(new HashSet<String>(tiles1).equals(new HashSet<String>(tiles2)));
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        assertTrue(landingPage.isFilteredViewOn());
        
        String viewParam3 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "view");
        assertEquals(viewParam2, viewParam3);
        
        List<String> tiles3 = landingPage.getTileNames();
        assertTrue(new HashSet<String>(tiles2).equals(new HashSet<String>(tiles3)));
    }
    
    @Test
    public void testDrilldown() throws Exception {
        init();
        landingPage.getNthTile(0).drillDown();
        landingPage.getNthTile(0).drillDown();
        List<String> tilesTab1 = landingPage.getTileNames();
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        List<String> tilesTab2 = landingPage.getTileNames();
        assertTrue(new HashSet<String>(tilesTab1).equals(new HashSet<String>(tilesTab2)));
    }
    
    @Test
    public void testBreadcrumbNavigation() throws Exception {
        init();
        landingPage.getNthTile(0).drillDown();
        landingPage.getNthTile(0).drillDown();
        List<String> tilesL2Tab1 = landingPage.getTileNames();
        Breadcrumb breadcrumb = ui.getBreadcrumb();
        
        breadcrumb.scrollToTop();
        breadcrumb.goTo(0);
        landingPage.waitForTilesToLoad();
        
        List<String> tilesL1Tab1 = landingPage.getTileNames();
        assertFalse(new HashSet<String>(tilesL2Tab1).equals(new HashSet<String>(tilesL1Tab1)));
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad();
        
        List<String> tilesTab2 = landingPage.getTileNames();
        assertTrue(new HashSet<String>(tilesL1Tab1).equals(new HashSet<String>(tilesTab2)));
    }
    
    @Test
    public void testTileSelection() throws Exception {
        init();
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        landingPage.cancelViewFilter();
        
        Tile tile0 = landingPage.getNthTile(0);
        tile0.select();
        String selTileParam0 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "selectedTile");
        tile0.unselect();
        Tile tile1 = landingPage.getNthTile(1);
        final String tile1Name = tile1.getName();
        tile1.select();
        String selTileParam1 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "selectedTile");
        assertNotEquals(selTileParam0, selTileParam1);
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad();
        
        assertTrue(landingPage.isFilteredViewOn());
        
        List<Tile> selectedTiles = landingPage.getSelectedTiles();
        assertEquals(selectedTiles.size(), 1);
        assertEquals(selectedTiles.get(0).getName(), tile1Name);
    }
    
    @Test
    public void testStorySelection() throws Exception {
        init();
        landingPage.getSummaryPanel().turnOffLiveMode();
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        landingPage.cancelViewFilter();
        
        ATPanel atPanel = landingPage.getATPanel();
        
        if (atPanel.getProblemsCount() == 0 && atPanel.getAnomaliesCount() == 0) {
            logger.warn("Neither problems or anomalies found in the AT panel, skipping test.");
            return;
        }
        
        String selStoryParam1 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "expandedStories");
        landingPage.getATPanel().expand();
        
        Story selectedStory = atPanel.getFirstProblemStory();
        if (atPanel.getProblemsCount() == 0) {
            selectedStory = atPanel.getFirstAnomalyStory();
        }
        selectedStory.expand();
        String selectedStoryName = selectedStory.getName();

        String selStoryParam2 = UrlUtils.getQueryStringParam(ui.getCurrentUrl(), "expandedStories");
        assertNotEquals(selStoryParam1, selStoryParam2);
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);

        assertTrue(landingPage.isFilteredViewOn());
        assertTrue(atPanel.isStorySelected(selectedStoryName));
    }
    
    @Test
    public void testBreadcrumbAttributeSelection() throws Exception {
        final String NEW_ATTRIBUTE = "Name";
        init();
        BreadcrumbExperiences bc = landingPage.getBreadcrumbExperiences();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        
        List<String> tiles1Tab1 = landingPage.getTileNames();
        bc.setCustomAttribute(NEW_ATTRIBUTE);
        List<String> tiles2Tab1 = landingPage.getTileNames();
        assertFalse(new HashSet<String>(tiles1Tab1).equals(new HashSet<String>(tiles2Tab1)));
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, currentUrl, View.HOMEPAGE);
        landingPage.waitForTilesToLoad(true);
        
        List<String> tilesTab2 = landingPage.getTileNames();
        assertTrue(bc.getCustomAttributeName().startsWith(NEW_ATTRIBUTE));
        assertTrue(new HashSet<String>(tiles2Tab1).equals(new HashSet<String>(tilesTab2)));
    }
    
    @Test
    public void testNotebook() throws Exception {
        init();
        
        logger.info("drill down to notebook");
        landingPage.getNthTile(0).drillDown();
        landingPage.getNthTile(0).openNotebook();
        
        logger.info("check the number of map nodes");
        String[] mapNodesTab1 = ui.getCanvas().getArrayOfNodeNames();
        Arrays.sort(mapNodesTab1);
        
        logger.info("check the number of problems and anomalies");
        ATPanel atPanel = landingPage.getATPanel();
        atPanel.expand();
        List<String> problemsTab1 = atPanel.getProblemsNames();
        List<String> anomaliesTab1 = atPanel.getAnomaliesNames();
        Collections.sort(problemsTab1);
        Collections.sort(anomaliesTab1);
        
        String currentUrl = ui.getCurrentUrl();
        ui.logout();
        
        ui.login(Role.ADMIN, currentUrl, View.NOTEBOOK);
        
        logger.info("check that map nodes match");
        String[] mapNodesTab2 = ui.getCanvas().getArrayOfNodeNames();
        Arrays.sort(mapNodesTab2);
        assertEquals(mapNodesTab1, mapNodesTab2);
        
        logger.info("check that problems and anomalies match");
        List<String> problemsTab2 = atPanel.getProblemsNames();
        List<String> anomaliesTab2 = atPanel.getAnomaliesNames();
        Collections.sort(problemsTab2);
        Collections.sort(anomaliesTab2);
        
        assertEquals(problemsTab1, problemsTab2);
        assertEquals(anomaliesTab1, anomaliesTab2);
    }
    
    @Test
    public void testShareUrlBox() throws Exception {
        init();
        
        landingPage.getNthTile(0).drillDown();
        Tile tile = landingPage.getNthTile(0);
        tile.select();
        
        logger.info("Check input text.");
        ShareUrlBox shareUrlBox = tile.openShareUrlBox();
        String copiedUrl = shareUrlBox.getText();
        assertEquals(copiedUrl, ui.getCurrentUrl());
        
        logger.info("Check copying to clipboard.");
        shareUrlBox.copyUrlToClipboard();
        shareUrlBox.clearText();
        shareUrlBox.getUrlInputElement().sendKeys(Keys.chord(Keys.CONTROL, "v"));
        assertEquals(shareUrlBox.getText(), copiedUrl);
    }
    
    @Test(groups = "failing")
    public void testGraphSwitching() throws Exception {
        init();
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        Tile tile = landingPage.getNthTile(0);
        
        logger.info("URL is changed on chart change");
        String initialUrl = ui.getCurrentUrl();
        tile.clickDots();
        assertTrue(initialUrl != ui.getCurrentUrl(), "URL has not changed after chart change");
        ChartType origChartType = tile.getChartType();
        ui.logout();
        
        logger.info("Check graph type is initialized from URL");
        init();
        tile = landingPage.getNthTile(0);
        assertEquals(tile.getChartType(), origChartType, "Graph type is not retrieved from URL");
    }
}
