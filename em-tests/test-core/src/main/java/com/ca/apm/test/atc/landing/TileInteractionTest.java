package com.ca.apm.test.atc.landing;

import java.util.List;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.landing.Breadcrumb;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.Tile;
import com.ca.apm.test.atc.common.landing.Tile.ChartType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TileInteractionTest extends UITest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UI ui;
    private LandingPage landingPage;
    private Breadcrumb breadcrumb;

    private final String TILE_TO_DRILL_DOWN_L1 = "Experiences in";
    private final String TILE_TO_DRILL_DOWN_L2 = "ApplicationService";

    private void init() throws Exception {
        ui = getUI();
        ui.login();
        landingPage = ui.getLandingPage();
        breadcrumb = ui.getBreadcrumb();
        logger.info("Wait for tiles to be loaded");
        landingPage.waitForTilesToLoad(true);
    }

    @Test(groups = "failing")
    public void testChartSwitching() throws Exception {
        init();

        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        
        logger.info("Chart switching on top page");
        Tile tile = landingPage.getNthTile(0);
        tile.changeChartTo(ChartType.HISTOGRAM);

        logger.info("Verify order of graphs");
        tile.clickDots();
        assertEquals(tile.getChartType(), ChartType.LINE_CHART, "Incorrect chart order");
        tile.clickDots();
        assertEquals(tile.getChartType(), ChartType.VOLUME_CHART, "Incorrect chart order");
        tile.clickDots();
        assertEquals(tile.getChartType(), ChartType.HISTOGRAM, "Incorrect chart order");

        logger.info("Top page - charts are switched on single tile only");
        assertTrue(landingPage.getTiles().size() > 1, "There is no more than one tile on top page");
        tile.clickDots();
        assertEquals(landingPage.getNthTile(1).getChartType(), landingPage.getNthTile(0).getChartType(),
            "Chart changed on other tile as well");
        tile.clickDots();
        assertEquals(landingPage.getNthTile(1).getChartType(), landingPage.getNthTile(0).getChartType(),
            "Chart changed on other tile as well");

        logger.info("Go to bussiness view");
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L2);
        assertTrue(landingPage.getTiles().size() > 1,
            "There is no more than one tile on bussiness view");

        logger.info("Bussiness view - charts are switched on the other tile as well");
        tile = landingPage.getNthTile(0);
        tile.changeChartTo(ChartType.HISTOGRAM);
        tile.clickDots();
        assertEquals(landingPage.getNthTile(1).getChartType(), ChartType.LINE_CHART,
            "Chart not switched on the other tile");
        tile.clickDots();
        assertEquals(landingPage.getNthTile(1).getChartType(), ChartType.VOLUME_CHART,
            "Chart not switched on the other tile");
        tile.clickDots();
        assertEquals(landingPage.getNthTile(1).getChartType(), ChartType.HISTOGRAM,
            "Chart not switched on the other tile");

    }

    @Test(groups = "failing")
    public void testSelectedTile() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L2);
        
        List<String> tileNamesL3 = landingPage.getTileNames();
        assertTrue(tileNamesL3.size() > 0, "There should be some tiles when drilling down under '" + TILE_TO_DRILL_DOWN_L2 + "'");
        final String selectedTileL3 = tileNamesL3.get(0);
        
        Tile tile = landingPage.getTileByName(selectedTileL3);
        tile.changeChartTo(ChartType.VOLUME_CHART);
        tile = landingPage.selectTile(selectedTileL3);

        logger.info("Check all charts are display at once");
        assertTrue(tile.getChartType(ChartType.HISTOGRAM).isDisplayed(),
            "Histogram chart is not displayed");
        assertTrue(tile.getChartType(ChartType.LINE_CHART).isDisplayed(),
            "Line chart is not displayed");
        assertTrue(tile.getChartType(ChartType.VOLUME_CHART).isDisplayed(),
            "Volume chart is not displayed");

        logger.info("Tile is collapsed on second click");
        tile = landingPage.selectTile(selectedTileL3);
        assertFalse(tile.getChartType(ChartType.HISTOGRAM).isDisplayed(), "Tile is collapsed");
        assertFalse(tile.getChartType(ChartType.LINE_CHART).isDisplayed(), "Tile is collapsed");
        assertTrue(tile.getChartType(ChartType.VOLUME_CHART).isDisplayed(), "Tile is collapsed");
        
        logger.info("Tile is collapsed by X button");
        tile.select();
        tile.unselect();
        assertFalse(tile.getChartType(ChartType.HISTOGRAM).isDisplayed(), "Tile is collapsed");
        assertFalse(tile.getChartType(ChartType.LINE_CHART).isDisplayed(), "Tile is collapsed");
        assertTrue(tile.getChartType(ChartType.VOLUME_CHART).isDisplayed(), "Tile is collapsed");
        
        logger.info("Open notebook");
        tile = landingPage.selectTile(selectedTileL3);
        tile.openNotebook();
        assertTrue(ui.getCurrentUrl().contains("/home/detail"));

        logger.info("Tile cannot be selected on top page");
        breadcrumb.goHome();
        tile = landingPage.getTileByNameSubstring(TILE_TO_DRILL_DOWN_L1);
        tile.changeChartTo(ChartType.HISTOGRAM);
        tile.select();
        assertTrue(tile.getChartType(ChartType.HISTOGRAM).isDisplayed(), "Tile is collapsed");
        assertFalse(tile.getChartType(ChartType.LINE_CHART).isDisplayed(), "Tile is collapsed");
        assertFalse(tile.getChartType(ChartType.VOLUME_CHART).isDisplayed(), "Tile is collapsed");
    }
    
    @Test
    public void testTileOverlay() throws Exception {
        init();
        Tile tile = landingPage.getNthTile(0);
        
        logger.info("Open tools overlay");
        tile.openToolsOverlay();
        assertTrue(tile.getDragIcon().isDisplayed(), "Drag icon is visible");
        assertTrue(tile.getEditIcon().isDisplayed(), "Edit icon is visible");

        logger.info("Close tools overlay");
        tile.closeToolsOverlay();
        assertFalse(tile.getDragIcon().isDisplayed(), "Drag icon is visible");
        assertFalse(tile.getEditIcon().isDisplayed(), "Edit icon is visible");
        
        logger.info("2nd click will close tools overlay");
        tile.openToolsOverlay();
        tile.getToolsOverlayIcon().click();
        ui.waitWhileVisible(tile.getToolsOverlaySelector());
        assertFalse(tile.getDragIcon().isDisplayed(), "Drag icon is visible");
        assertFalse(tile.getEditIcon().isDisplayed(), "Edit icon is visible");
    }
}
