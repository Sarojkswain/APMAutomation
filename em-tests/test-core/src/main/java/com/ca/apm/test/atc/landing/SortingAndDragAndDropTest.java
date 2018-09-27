package com.ca.apm.test.atc.landing;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.landing.BreadcrumbExperiences;
import com.ca.apm.test.atc.common.landing.BreadcrumbExperiences.SortedBy;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.Tile;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class SortingAndDragAndDropTest extends UITest {

    private final static Logger logger = Logger.getLogger(SortingAndDragAndDropTest.class);
        
    private UI ui;
    private LandingPage landingPage;
    private BreadcrumbExperiences brc;

    private final String TILE_TO_DRILL_DOWN_L1 = "Experiences in";
    
    private void init() throws Exception {
        ui = getUI();
        ui.login();
        
        landingPage = ui.getLandingPage();
        landingPage.waitForTilesToLoad(true);
        
        brc = landingPage.getBreadcrumbExperiences();
    }

    /** 
     * Test the sorting control of Top Page 
     * @throws Exception 
     */
    @Test
    public void testTopPageElements() throws Exception {
        init();
        
        final String where = "in top page";
        verifyIsSortingDropdownAvailable(where);
        verifySortingDropdownContainsElements(where, true, false);
        verifyCheckDefaultSortingOption(where, true);
    }

    /** 
     * Test the sorting control of the 1st level of Business View
     * @throws Exception 
     */
    @Test
    public void test1stLevelBuinessViewElements() throws Exception {
        init();
        
        String tileName1 = drillDownTo1stLevelOfBusinessView();
        
        final String where = "in the 1st level of business view '" + tileName1 + "'";
        verifyIsSortingDropdownAvailable(where);
        verifySortingDropdownContainsElements(where, false, false);
        verifyCheckDefaultSortingOption(where, true);
    }

    /** 
     * Test the sorting control of the 2nd level of Business View
     * @throws Exception 
     */
    @Test
    public void test2ndLevelBuinessViewElements() throws Exception {
        init();
        
        String tileName1 = drillDownTo1stLevelOfBusinessView();
        String tileName2 = drillDownToAnotherLevelOfBusinessView(tileName1);
        
        final String where = "in the 2nd level of business view '" + tileName1 + "' / '" + tileName2 + "'";
        verifyIsSortingDropdownAvailable(where);
        verifySortingDropdownContainsElements(where, false, true);
        verifyCheckDefaultSortingOption(where, false);
    }
    
    /** 
     * Verify that the "Sorted by" drop-down menu is available in the secondary breadcrumbs
     */
    private void verifyIsSortingDropdownAvailable(String where) {
        Assert.assertTrue(brc.isSortedByDropdownPresent(),
            "Sorted By dropdown not found in " + where);
    }
    
    /**
     * Verify the available sorting options 
     */
    private void verifySortingDropdownContainsElements(String where, boolean isTopPage, boolean is2ndLevelOfBV) {
        List<String> options = brc.getSortedByDropdownMenuOptions();

        for (SortedBy sb : SortedBy.values()) {
            boolean optionShouldBeDisplayed = (!isTopPage || sb.isOnTop()) && (!is2ndLevelOfBV || sb.isIn2ndAndDeeperLevelsOfBV());    
            Assert.assertEquals(optionShouldBeDisplayed, options.contains(sb.getText()),
                optionShouldBeDisplayed ?
                "Option '" + sb.getText() + "' not found in the Sorted By dropdown in " + where :
                    "Option '" + sb.getText() + "' was not supposed to be in the Sorted By dropdown in " + where);
        }
    }
    
    /**
     * Verify the default sorting option 
     */
    private void verifyCheckDefaultSortingOption(String where, boolean isTopPageOr1stLevelOfBusinessView) {
        if (isTopPageOr1stLevelOfBusinessView) {
            Assert.assertEquals(brc.getSortedBy(), BreadcrumbExperiences.SortedBy.SORTED_BY_MY_ORDER,
                "The sorting option, selected by default, should be 'My Order' in " + where);
        } else {
            Assert.assertEquals(brc.getSortedBy(), BreadcrumbExperiences.SortedBy.SORTED_BY_TRANSACTIONS_VOLUME,
                "The sorting option, selected by default, should be 'Transactions Volume' in " + where);
        }
    }

    @Test(dependsOnMethods="testDragAndDropTilesInTopPage", groups = "failing")
    public void testSortingOptionIsAppliedInTopPage() throws Exception {
        init();

        Assert.assertEquals(brc.getSortedBy(), BreadcrumbExperiences.SortedBy.SORTED_BY_MY_ORDER,
            "The selecting option, selected by default, should be 'My Order'");
        
        final String where = "in top page";
        verifySortingOptionIsApplied(where, true);
    }

    @Test(dependsOnMethods = "testDragAndDropTilesIn1stLevelBusinessView", timeOut = 5 * 60 * 1000L, groups = "failing")
    public void testSortingOptionIsAppliedIn1stLevelOfBusinessView() throws Exception {
        init();
        
        final String tileName1 = drillDownTo1stLevelOfBusinessView();
        
        final String where = "in the 1st level of business view '" + tileName1 + "'";
        verifySortingOptionIsApplied(where, true);
    }

    @Test(timeOut = 5 * 60 * 1000L)
    public void testSortingOptionIsAppliedIn2ndLevelOfBusinessView() throws Exception {
        init();
        
        String tileName1 = drillDownTo1stLevelOfBusinessView();
        String tileName2 = drillDownToAnotherLevelOfBusinessView(tileName1);
        
        final String where = "in the 2nd level of business view '" + tileName1 + "' / '" + tileName2 + "'";
        verifySortingOptionIsApplied(where, false);
    } 
    
    private String drillDownTo1stLevelOfBusinessView() {
        List<Tile> tiles = landingPage.getTiles();
        Assert.assertTrue(tiles.size() > 0, "There are no tiles in Top View.");
                
        String tileName = tiles.get(0).getName();
        tiles.get(0).drillDown();

        landingPage.waitForTilesToLoad(true);
        
        return tileName;
    }

    private String drillDownToAnotherLevelOfBusinessView(String tileName1) {
        List<Tile> tiles = landingPage.getTiles();

        Assert.assertTrue(tiles.size() > 0,
            "The test case expects there are some tiles when drilled down to the business view '"
                + tileName1 + "'.");

        String tileName2 = tiles.get(0).getName();
        tiles.get(0).drillDown();

        landingPage.waitForTilesToLoad(true);

        return tileName2;
    }
    
    /**
     * Verify that the sorting option is applied correctly and immediately upon selection
     */
    private void verifySortingOptionIsApplied(String where, boolean isTopPageOr1stLevelOfBusinessView) {
        if (isTopPageOr1stLevelOfBusinessView) {
            /* Sort by My Order */
            brc.setSortedBy(BreadcrumbExperiences.SortedBy.SORTED_BY_MY_ORDER);
        }
        
        /* Remember the initial order */
        List<String> myOrderTileNames = landingPage.getTileNames();

        /* Test sorting by transaction volume */
        brc.setSortedBy(BreadcrumbExperiences.SortedBy.SORTED_BY_TRANSACTIONS_VOLUME);
        List<Tile> tiles = landingPage.getTiles();
        for (int i = 1; i < tiles.size(); i++) {
            Assert.assertTrue(tiles.get(i - 1).getAllTransactionsVolume() >= tiles.get(i).getAllTransactionsVolume(), 
                "Sorting by transaction volume is wrong: the tile '" + tiles.get(i - 1).getName() 
                + "' should come before the tile '" + tiles.get(i).getName() + "' in " + where);
        }

        /* Test sorting by transaction health */
        brc.setSortedBy(BreadcrumbExperiences.SortedBy.SORTED_BY_TRANSACTIONS_HEALTH);
        tiles = landingPage.getTiles();
        for (int i = 1; i < tiles.size(); i++) {
            int appDex0 = tiles.get(i - 1).getAppdex();
            int appDex1 = tiles.get(i).getAppdex();
            Assert.assertTrue(appDex0 <= appDex1, 
                "Sorting by transaction health is wrong: the tile '" + tiles.get(i - 1).getName() 
                + "' (appdex " + appDex0 + ") should come before the tile '" + tiles.get(i).getName() 
                + "' (" + appDex1 + ") in " + where);
        }
        
        /* Test sorting by failed transaction count */
        brc.setSortedBy(BreadcrumbExperiences.SortedBy.SORTED_BY_FAILED_COUNT);
        tiles = landingPage.getTiles();
        for (int i = 1; i < tiles.size(); i++) {
            int failCnt0 = tiles.get(i - 1).getHistogram().getFailedTransactionsVolume();
            int failCnt1 = tiles.get(i).getHistogram().getFailedTransactionsVolume();
            Assert.assertTrue(failCnt0 >= failCnt1, "Sorting by failed transaction count is wrong: the tile '"
                + tiles.get(i - 1).getName() + "' (failed tx vol: " + failCnt0 + ") should come before the tile '"
                + tiles.get(i).getName() + "' (failed tx vol: " + failCnt1 + ") in " + where);
        }

        /* Test sorting by slow transaction count */
        brc.setSortedBy(BreadcrumbExperiences.SortedBy.SORTED_BY_SLOW_COUNT);
        tiles = landingPage.getTiles();
        for (int i = 1; i < tiles.size(); i++) {
            int slowCnt0 = tiles.get(i - 1).getHistogram().getFailedTransactionsVolume();
            int slowCnt1 = tiles.get(i).getHistogram().getFailedTransactionsVolume();
            Assert.assertTrue(slowCnt0 >= slowCnt1, "Sorting by slow transaction count is wrong: the tile '"
                + tiles.get(i - 1).getName() + "' (slow tx vol " + slowCnt0 + ") should come before the tile '"
                + tiles.get(i).getName() + "' (slow tx vol " + slowCnt1 + ") in " + where);
        }
        
        if (isTopPageOr1stLevelOfBusinessView) {
            /* Test sorting by My order */
            brc.setSortedBy(BreadcrumbExperiences.SortedBy.SORTED_BY_MY_ORDER);
            tiles = landingPage.getTiles();
            for (int i = 0; i < tiles.size(); i++) {
                Assert.assertEquals(tiles.get(i).getName(), myOrderTileNames.get(i), 
                    "Sorting by my order does not keep the original order. At index '" + i + "' "
                        + "the tile '" + myOrderTileNames.get(i) + "' was expected instead of the"
                            + " currently present '" + tiles.get(i).getName() + "' in " + where);
            }
        }
    }
        
    /**
     * Sorting option "Transactions Health" is temporarily applied by means of Special view 
     * when user clicks to the "Slow" label in the Healthy Experiences chart in the Summary panel.
     * 
     * Temporary sorting option is lost and the previously selected sorting option is applied 
     * when the Special view is removed from the secondary breadcrumb bar.
     * @throws Exception 
     */
    @Test
    public void testSpecialViewWhenSlowSelectedInSummary() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        
        landingPage.getSummaryPanel().expandPanel();
        if (landingPage.getSummaryPanel().getHistogram().getSlowTransactionsVolume() > 0) {
            // Do a user selection 
            landingPage.getBreadcrumbExperiences().setSortedBy(SortedBy.SORTED_BY_FAILED_COUNT);
                
            landingPage.getSummaryPanel().filterSlowTransactions();
                    
            Assert.assertEquals(landingPage.getBreadcrumbExperiences().getSortedBy(), SortedBy.SORTED_BY_TRANSACTIONS_HEALTH, 
                "When user clicks to the SLOW label in the Healthy Experiences chart in the Summary panel, "
                + "sorting option Transactions Health should be temporarily applied by means of Special view.");
            
            landingPage.getBreadcrumbExperiences().cancelSpecialView();
                    
            Assert.assertEquals(landingPage.getBreadcrumbExperiences().getSortedBy(), SortedBy.SORTED_BY_FAILED_COUNT, 
                "Temporary sorting option should be lost and the previously selected sorting option should be applied " 
                + "when the Special view is removed from the secondary breadcrumb bar.");
        } else {
            logger.warn("Volume of slow transactions in the summary panel histogram is 0, therefore this test case is being effectively skipped.");
        }
    }
        
    /**
     * Sorting option "Failed count" is temporarily applied by means of Special view 
     * when user clicks to the "FAIL" column of the Response time chart in the Summary panel
     * 
     * Temporary sorting option is lost and the previously selected sorting option is applied 
     * when the Special view is removed from the secondary breadcrumb bar
     * @throws Exception 
     */
    @Test
    public void testSpecialViewWhenFailedSelectedInSummary() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        
        landingPage.getSummaryPanel().expandPanel();
        if (landingPage.getSummaryPanel().getHistogram().getFailedTransactionsVolume() > 0) {
            // Do a user selection 
            landingPage.getBreadcrumbExperiences().setSortedBy(SortedBy.SORTED_BY_MY_ORDER);
      
            landingPage.getSummaryPanel().filterFailedTransactions();
            
            Assert.assertEquals(landingPage.getBreadcrumbExperiences().getSortedBy(), SortedBy.SORTED_BY_FAILED_COUNT, 
                "When user clicks to the FAIL label in the Healthy Experiences chart in the Summary panel, "
                + "sorting option Failed count should be temporarily applied by means of Special view.");
            
            landingPage.getBreadcrumbExperiences().cancelSpecialView();
            
            Assert.assertEquals(landingPage.getBreadcrumbExperiences().getSortedBy(), SortedBy.SORTED_BY_MY_ORDER, 
                "Temporary sorting option should be lost and the previously selected sorting option should be applied " 
                + "when the Special view is removed from the secondary breadcrumb bar.");
        } else {
            logger.warn("Volume of failed transactions in the summary panel histogram is 0, therefore this test case is being effectively skipped.");
        }
    }
    
    /**
     * Test that the sorting option is not transferred in URL
     * @throws Exception 
     */
    @Test
    public void testSortingIsNotTransferredInURL() throws Exception {
        init();
        
        // Do a user selection (non-default) 
        landingPage.getBreadcrumbExperiences().setSortedBy(SortedBy.SORTED_BY_FAILED_COUNT);
        
        // Reload the page
        ui.getDriver().navigate().refresh();
                
        landingPage = ui.getLandingPage();
        landingPage.waitForTilesToLoad(true);
        
        // The default option should be selected 
        Assert.assertEquals(landingPage.getBreadcrumbExperiences().getSortedBy(), SortedBy.SORTED_BY_MY_ORDER, 
            "In a new window, the default sorting option should be selected.");
    }
    
    @Test(groups = "dependency")
    public void testDragAndDropTilesInTopPage() throws Exception {
        init();
        
        List<String> tileNames1 = landingPage.getTileNames();
        if (tileNames1.size() < 2) {
            logger.warn("There are less than 2 tiles in the Top View. Drag and drop could not be tested.");
        } else {
            logger.info("Dragging the first tile and dropping it over the second one.");
            landingPage.moveTile(0, 1);
            
            List<String> tileNames2 = landingPage.getTileNames();
            
            Assert.assertEquals(tileNames1.size(), tileNames2.size(), "Number of tiles differ after the first drag and drop");

            logger.info("The first two tile names should be swapped, the rest tile names should be equal.");
            Assert.assertEquals(tileNames1.get(0), tileNames2.get(1));
            Assert.assertEquals(tileNames1.get(1), tileNames2.get(0));
            for (int i = 2; i < tileNames1.size(); i++) {
                Assert.assertEquals(tileNames1.get(i), tileNames2.get(i));    
            }
            
            if (tileNames2.size() > 2) {
                int lastTileNdx = tileNames2.size() - 1;
                logger.info("Dragging the tile before the last and dropping it over the last tile.");
                landingPage.moveTile(lastTileNdx - 1, lastTileNdx);
                
                List<String> tileNames3 = landingPage.getTileNames();
                
                Assert.assertEquals(tileNames2.size(), tileNames3.size(), "Number of tiles differ after the second drag and drop");

                logger.info("The last two tile names should be swapped, the rest tile names should be equal.");
                Assert.assertEquals(tileNames2.get(lastTileNdx), tileNames3.get(lastTileNdx - 1));
                Assert.assertEquals(tileNames2.get(lastTileNdx - 1), tileNames3.get(lastTileNdx));
                for (int i = 0; i < lastTileNdx - 1; i++) {
                    Assert.assertEquals(tileNames2.get(i), tileNames3.get(i));    
                }   
            }
        }
    }
    
    @Test(groups = "dependency",timeOut = 5 * 60 * 1000L)
    public void testDragAndDropTilesIn1stLevelBusinessView() throws Exception {
        init();
        
        drillDownTo1stLevelOfBusinessView();
        
        List<String> tileNames1 = landingPage.getTileNames();
        if (tileNames1.size() < 2) {
            logger.warn("There are less than 2 tiles in the 1st level of Business View. Drag and drop could not be tested.");
        } else {
            logger.info("Dragging the second tile and dropping it over the first one.");
            landingPage.moveTile(1, 0);
            
            List<String> tileNames2 = landingPage.getTileNames();
            
            Assert.assertEquals(tileNames1.size(), tileNames2.size(), "Number of tiles differ after the first drag and drop");

            logger.info("The first two tile names should be swapped, the rest tile names should be equal.");
            Assert.assertEquals(tileNames1.get(0), tileNames2.get(1));
            Assert.assertEquals(tileNames1.get(1), tileNames2.get(0));
            for (int i = 2; i < tileNames1.size(); i++) {
                Assert.assertEquals(tileNames1.get(i), tileNames2.get(i));    
            }
            
            if (tileNames2.size() > 2) {
                int lastTileNdx = tileNames2.size() - 1;
                logger.info("Dragging the last tile and dropping it over the tile before the last.");
                landingPage.moveTile(lastTileNdx, lastTileNdx - 1);
                
                List<String> tileNames3 = landingPage.getTileNames();
                
                Assert.assertEquals(tileNames2.size(), tileNames3.size(), "Number of tiles differ after the second drag and drop");

                logger.info("The last two tile names should be swapped, the rest tile names should be equal.");
                Assert.assertEquals(tileNames2.get(lastTileNdx), tileNames3.get(lastTileNdx - 1));
                Assert.assertEquals(tileNames2.get(lastTileNdx - 1), tileNames3.get(lastTileNdx));
                for (int i = 0; i < lastTileNdx - 1; i++) {
                    Assert.assertEquals(tileNames2.get(i), tileNames3.get(i));    
                }   
            }
        }
    }
    
    public void testDragAndDropIsNotAvailableIn2ndLevelBusinessView() throws Exception {
        init();
        
        String tileName1 = drillDownTo1stLevelOfBusinessView();
        drillDownToAnotherLevelOfBusinessView(tileName1);
        
        if (landingPage.getTileNames().size() < 1) {
            logger.warn("There are no tiles in the 2nd level of Business View. Tile cannot be tested.");
        } else {
            for (Tile tile : landingPage.getTiles()) {
                Assert.assertFalse(tile.isToolsOverlayIcon(), "There should be no overlay icon on the tile " + tile.getName());
            }
        }
    }
}
