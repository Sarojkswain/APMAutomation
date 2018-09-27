package com.ca.apm.test.atc.landing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.landing.BreadcrumbExperiences;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.SummaryPanel;
import com.ca.apm.test.atc.common.landing.Tile;

public class TilesToolbarTest extends UITest {

    private final String TILE_TO_DRILL_DOWN = "Drill me";
    private final String PERSPECTIVE_TO_SELECT = "Type";
    
    private LandingPage landingPage;
    private BreadcrumbExperiences expToolbar;
    
    private void init() throws Exception {
        getUI().login();
        landingPage = getUI().getLandingPage();
        expToolbar = landingPage.getBreadcrumbExperiences();
        landingPage.waitForTilesToLoad(true);
    }
    
    @Test
    public void testCustomPerspectiveDrilldown() throws Exception {
        init();
        
        assertFalse(expToolbar.isCustomAttributeDropdownPresent(),
            "Temporary perspective selector should not be present in Global View");
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN);
        expToolbar.setCustomAttribute(PERSPECTIVE_TO_SELECT);
        
        Tile tile = landingPage.getTiles().get(0);
        assertFalse(tile.hasNextDrilldownLevel(),
                "There should be no next drill-down level in the case of temporary perspective," +
                " i.e. link should lead directly to notebook page.");
    }
    
    @Test
    public void testFilterWontCancelCustomPerspective() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN);
        expToolbar.setCustomAttribute(PERSPECTIVE_TO_SELECT);
        
        SummaryPanel summary = landingPage.getSummaryPanel();
        summary.filterPoorTransactions();
        
        expToolbar.cancelSpecialView();
        assertTrue(expToolbar.getCustomAttributeName().startsWith(PERSPECTIVE_TO_SELECT));
    }
    
    @Test
    public void testGoBackButton() throws Exception {
        init();
        
        assertFalse(expToolbar.isGoBackButtonPresent(),
            "Go Back button should not be present in Global view");
        
        landingPage.getNthTile(0).drillDown();
        assertTrue(expToolbar.isGoBackButtonPresent(),
                "Go Back button should be present in Business view");
        
        expToolbar.goOneLevelBack();
        assertTrue(landingPage.isGlobalView());
    }
    
    @Test
    public void testComponentsInDangerIndicator() throws Exception {
        init();
        
        int tilesInDangerCount = 0;
        for (Tile tile : landingPage.getTiles()) {
            if (tile.isTileInDangerState()) {
                tilesInDangerCount++;
            }
        }
        
        assertEquals(expToolbar.getTilesInDangerIndicator().getInDangerCount(), tilesInDangerCount);
    }
}
