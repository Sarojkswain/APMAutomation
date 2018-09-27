package com.ca.apm.test.atc.landing;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.landing.ATPanel;
import com.ca.apm.test.atc.common.landing.BreadcrumbExperiences;
import com.ca.apm.test.atc.common.landing.BreadcrumbExperiences.SortedBy;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.LandingUtils;
import com.ca.apm.test.atc.common.landing.StoriesGroup;
import com.ca.apm.test.atc.common.landing.Story;
import com.ca.apm.test.atc.common.landing.StoryPanel;
import com.ca.apm.test.atc.common.landing.SummaryPanel;
import com.ca.apm.test.atc.common.landing.Tile;
import com.ca.apm.test.atc.common.landing.Tile.ChartType;

public class FilteringTest extends UITest {

    private UI ui;
    private LandingPage landingPage;

    private void init() throws Exception {
        ui = getUI();
        ui.login();

        landingPage = ui.getLandingPage();
    }
    
    @Test
    public void testFiltersActivation() throws Exception {
        init();
        
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        BreadcrumbExperiences breadcrumbExperiences = landingPage.getBreadcrumbExperiences();
        ATPanel atPanel = landingPage.getATPanel();
        
        summaryPanel.expandPanel();
        
        logger.info("Test Poor Transactions filter is activated");
        summaryPanel.filterPoorTransactions();
        Assert.assertEquals(breadcrumbExperiences.getTileSpecialFiltering(), BreadcrumbExperiences.TileSpecialFiltering.POOR_TRANSACTIONS);
        
        logger.info("Test Failed Transactions filter is activated");
        summaryPanel.filterFailedTransactions();
        Assert.assertEquals(breadcrumbExperiences.getTileSpecialFiltering(), BreadcrumbExperiences.TileSpecialFiltering.FAILED_TRANSACTIONS);
        
        logger.info("Test Tile selection filter is activated");
        breadcrumbExperiences.cancelAllFilters();
        landingPage.getRandomProblemTile().drillDown();
        Tile problemTile = landingPage.getRandomProblemTile();
        problemTile.select();
        Assert.assertEquals(breadcrumbExperiences.getTileSelectionFiltering(), BreadcrumbExperiences.TileSpecialFiltering.FILTERED_EXPERIENCES);
        
        logger.info("Test Story selection filter is activated");
        atPanel.getFirstProblemStory().expand();
        Assert.assertEquals(breadcrumbExperiences.getTileSelectionFiltering(), BreadcrumbExperiences.TileSpecialFiltering.FILTERED_EXPERIENCES);
    }
    
    @Test
    public void testPoorTransactionsFilter() throws Exception {
        init();
        
        landingPage.getRandomProblemTile().drillDown();

        ATPanel atPanel = landingPage.getATPanel();
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        BreadcrumbExperiences breadcrumbExperiences = new BreadcrumbExperiences(this.ui, this.landingPage);
        List<Tile> originalTileSet = landingPage.getTiles();
        ChartType originalChartType = originalTileSet.get(0).getChartType();
        List<Story> originalStorySet = atPanel.getAllVisibleStories();
        String originalGroupByValue = breadcrumbExperiences.getCustomAttributeName();
        int storyOriginalCount = atPanel.getProblemsCount() + atPanel.getAnomaliesCount();
        
        summaryPanel.expandPanel();
        summaryPanel.filterPoorTransactions();
        
        logger.info("Checking that cancel filter control is visible");
        Assert.assertTrue(breadcrumbExperiences.getCancelSpecialViewElement().isDisplayed());
        logger.info("Checking the tiles are grouped by name");
        Assert.assertEquals(breadcrumbExperiences.getGroupedBy(), BreadcrumbExperiences.GroupedBy.GROUPED_BY_NAME);
        
        int lastApdexValue = 0;

        logger.info("Checking filter properties on the visible tiles");
        for (Tile tile : landingPage.getTiles()) {
            if (tile.isDisplayed()) {
                int transactionCount = LandingUtils.getValueFromFormattedString(tile.getPoorTransactionsVolumeElement().getText());
                ChartType chartType = tile.getChartType();
                
                Assert.assertTrue(transactionCount > 0);
                Assert.assertFalse(tile.hasNextDrilldownLevel());
                Assert.assertEquals(chartType, Tile.ChartType.VOLUME_CHART);
                Assert.assertTrue(tile.getAppdex() >= lastApdexValue);
                
                lastApdexValue = tile.getAppdex();
            }
        }
        
        logger.info("Cancel Poor Transaction filter");
        breadcrumbExperiences.cancelAllFilters();
        
        Assert.assertFalse(breadcrumbExperiences.isTileFilteredView());
        Assert.assertFalse(breadcrumbExperiences.isTileSpecialView());
        Assert.assertEquals(landingPage.getTiles().size(), originalTileSet.size());
        Assert.assertEquals(atPanel.getAllVisibleStories().size(), originalStorySet.size());
        Assert.assertEquals(breadcrumbExperiences.getCustomAttributeName(), originalGroupByValue);
        Assert.assertEquals(landingPage.getTiles().get(0).getChartType(), originalChartType);
        Assert.assertFalse(breadcrumbExperiences.isTileSpecialView());
        Assert.assertEquals(atPanel.getProblemsCount() + atPanel.getAnomaliesCount(), storyOriginalCount);
    }
    
    @Test
    public void testTileFilterInSpecialView() throws Exception {
        init();
        
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        BreadcrumbExperiences breadcrumbExperiences = new BreadcrumbExperiences(this.ui, this.landingPage);

        summaryPanel.expandPanel();
        summaryPanel.filterPoorTransactions();
        
        Tile tile = this.landingPage.getTiles().get(0);
        PageElement tileProblemContainer = tile.getProblemContainer();
        tile.select();
        
        Assert.assertTrue(tileProblemContainer.getAttribute("class").contains("active"), "Tile element did not reveive \"active\" class name");
        Assert.assertEquals(this.landingPage.getVisibleTiles().size(), 1, "Tile elemens seems to have not been filtered");
        Assert.assertTrue(breadcrumbExperiences.getCancelTilesFilterElement().isDisplayed(), "Cancel Tile filter element is not visible");
    }
    
    @Test
    public void testStoryFilterInSpecialView() throws Exception {
        init();
        
        logger.info("Drill down to the Business View");
        this.landingPage.getTiles().get(0).drillDown();
        
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        summaryPanel.setTimeRange(SummaryPanel.TimeRange.CUSTOM_RANGE);
        summaryPanel.filterPoorTransactions();
        
        ATPanel atPanel = landingPage.getATPanel();
        atPanel.expand();
        
        Story firstProblemStory = atPanel.getFirstProblemStory();
        
        if (firstProblemStory != null) {
            logger.info("Expand first problem story");
            firstProblemStory.expand();

            List<StoriesGroup> problems = atPanel.getProblemsGroups();
            logger.info("Checking that only the selected problem story is visible");
            Assert.assertEquals(problems.size(), 1);
            atPanel.cancelDetailView();
        }
        
        Story firstAnomalyStory = atPanel.getFirstAnomalyStory();
        
        if (firstAnomalyStory != null) {
            logger.info("Scroll to and expand the first anomaly story");
            atPanel.scrollToAnomaliesPanel();
            firstAnomalyStory.expand();
            
            List<StoriesGroup> anomalies = atPanel.getAnomaliesGroups();
            logger.info("Checking that only the selected problem story is visible");
            Assert.assertEquals(anomalies.size(), 1);
        }
    }
    
    @Test
    public void testProblemsAndAnomaliesAffectedComponentsCount() throws Exception {
        init();

        this.landingPage.getTiles().get(0).drillDown();

        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        summaryPanel.expandPanel();
        summaryPanel.setTimeRange(SummaryPanel.TimeRange.CUSTOM_RANGE);

        int tilesProblemsCount = 0;
        int tilesAnomaliesCount = 0;
        
        for (Tile tile : this.landingPage.getTiles()) {
            tilesProblemsCount += tile.getProblemsCount();
            tilesAnomaliesCount += tile.getAnomaliesCount();
        }
        
        StoryPanel storyPanel = new StoryPanel(this.ui, this.landingPage);
        
        Assert.assertEquals(tilesProblemsCount, storyPanel.getProblemsCount());
        Assert.assertEquals(tilesAnomaliesCount, storyPanel.getAnomaliesCount(), "Number of anomalies in tiles and story panel don't match");
    }
    
    @Test
    public void testFailedTransactionFiltering() throws Exception {
        init();
        
        SummaryPanel summaryPanel = new SummaryPanel(ui, landingPage);
        BreadcrumbExperiences breadcrumbExperiences = new BreadcrumbExperiences(ui, landingPage);
        
        List<Tile> originalTiles = landingPage.getTiles();
        SortedBy originalSortedBy = breadcrumbExperiences.getSortedBy();
        ChartType originalChartType = originalTiles.get(0).getChartType();
        
        summaryPanel.setTimeRange(SummaryPanel.TimeRange.CUSTOM_RANGE);
        summaryPanel.filterFailedTransactions();
        
        List<Tile> tiles = landingPage.getTiles();
        
        Assert.assertEquals(breadcrumbExperiences.getSortedBy(), BreadcrumbExperiences.SortedBy.SORTED_BY_FAILED_COUNT);
        
        for (int i = tiles.size() - 1; i >= 0; i--) {
            logger.info("Checking tiles graphs was switched to Histogram");
            ChartType chartType = tiles.get(i).getChartType();
            if (chartType != null) {
                Assert.assertEquals(chartType, ChartType.HISTOGRAM);
            }
            
            logger.info("Checking tiles are sorted by Failed count");
            if (i > 0) { // check only if a tile has next one
                Assert.assertTrue(tiles.get(i - 1).getHistogramFailedVolume() >= tiles.get(i).getHistogramFailedVolume());
            }
            
            logger.info("Checking that drilldown is disabled");
            Assert.assertFalse(tiles.get(i).hasNextDrilldownLevel());
            
            logger.info("Checking tile has link to Notebook");
            Assert.assertTrue(tiles.get(i).hasNotebookLink());
        }
        
        logger.info("Checking if special view indicator is present");
        Assert.assertTrue(breadcrumbExperiences.getCancelSpecialViewElement().isDisplayed());
        
        logger.info("Cancelling Failed filter");
        breadcrumbExperiences.cancelSpecialView();
        
        List<Tile> restoredTiles = landingPage.getTiles();
        
        logger.info("Checking if special view indicator is not present");
        Assert.assertFalse(breadcrumbExperiences.getCancelSpecialViewElement().isDisplayed());
        
        logger.info("Checking the sort type was returned to original value");
        Assert.assertEquals(breadcrumbExperiences.getSortedBy(), originalSortedBy);
        
        logger.info("Checking the chart type was reset to original type");
        Assert.assertEquals(restoredTiles.get(0).getChartType(), originalChartType);
        Assert.assertEquals(restoredTiles.size(), originalTiles.size());
    }
    
    @Test
    public void testTilesFiltering() throws Exception {
        init();
        
        logger.info("Drill down to Business View");
        landingPage.getTiles().get(0).drillDown();
        
        ATPanel atPanel = landingPage.getATPanel();
        List<Tile> tiles = landingPage.getVisibleTiles();
        Tile selectedTile = tiles.get(0);
        int tileOriginalProblemsCount = selectedTile.getProblemsCount();
        int tileOriginalAnomaliesCount = selectedTile.getAnomaliesCount();
        int problemsOriginalCount = atPanel.getProblemsCount();
        int anomaliesOriginalCount = atPanel.getAnomaliesCount();
        
        
        logger.info("Selecting first tile");
        selectedTile.select();
        
        logger.info("Checking the tile has been filtered");
        selectedTile.isSelected();
        
        logger.info("Checking the non-filtered items are not visible");
        Assert.assertEquals(landingPage.getVisibleTiles().size(), 1);
        
        logger.info("Checking the number of problems did not change");
        Assert.assertEquals(atPanel.getProblemsCount(), problemsOriginalCount);

        logger.info("Checking the number of anomalies did not change");
        Assert.assertEquals(atPanel.getAnomaliesCount(), anomaliesOriginalCount);
        
        logger.info("Checking the breadcrumb contains text that the tile is filtered");
        BreadcrumbExperiences breadcrumbExperiences = landingPage.getBreadcrumbExperiences();
        Assert.assertEquals(breadcrumbExperiences.getTileSelectionFiltering(), BreadcrumbExperiences.TileSpecialFiltering.FILTERED_EXPERIENCES);
        
        logger.info("Checking the number of problems on tile does not change on at panel filtering");
        atPanel.getFirstProblemStory().expand();
        Assert.assertEquals(selectedTile.getProblemsCount(), tileOriginalProblemsCount);
        logger.info("Checking the tile filter has been cancelled after problem selection");
        Assert.assertFalse(selectedTile.isSelected());
        
        logger.info("Cancelling problem selection");
        atPanel.cancelDetailView();

        logger.info("Checking the number of anomalies on tile does not change on at panel filtering");
        atPanel.getFirstAnomalyStory().expand();
        Assert.assertEquals(selectedTile.getAnomaliesCount(), tileOriginalAnomaliesCount);
        
        logger.info("Checking the tile selection can be cancelles fron breadcrumb and tile");
        selectedTile.select();
        Assert.assertTrue(selectedTile.isSelected());
        selectedTile.unselect();
        Assert.assertFalse(selectedTile.isSelected());
        selectedTile.select();
        breadcrumbExperiences.cancelTilesFilter();
        Assert.assertFalse(selectedTile.isSelected());
    }
    
    @Test
    public void testStoriesFiltering() throws Exception {
        init();
        
        logger.info("Drill down to Business View");
        landingPage.getTiles().get(0).drillDown();
        
        ATPanel atPanel = landingPage.getATPanel();
        List<Tile> tiles = landingPage.getVisibleTiles();
        Tile selectedTile = tiles.get(0);
        int tilesOriginalCount = tiles.size();
        int storiesOriginalCount = atPanel.getAllVisibleStories().size();
        
        logger.info("Select a tile to activate stories filter");
        atPanel.expand();
        selectedTile.select();
        Assert.assertEquals(atPanel.getProblemsCount(), selectedTile.getProblemsCount());
        Assert.assertEquals(atPanel.getAnomaliesCount(), selectedTile.getAnomaliesCount());
        
        logger.info("Expanding the first story");
        Story firstProblemStory = atPanel.getFirstProblemStory();
        firstProblemStory.expand();
        Assert.assertEquals(atPanel.getAllVisibleStories().size(), 1);
        
        logger.info("Checking the breadcrumb contains text that the view is filtered");
        BreadcrumbExperiences breadcrumbExperiences = landingPage.getBreadcrumbExperiences();
        Assert.assertNotEquals(null, breadcrumbExperiences.getTileSelectionFiltering());
        
        logger.info("Cancel story filter by collapsing the element");
        firstProblemStory.collapse();
        Assert.assertEquals(landingPage.getVisibleTiles().size(), tilesOriginalCount);
        Assert.assertEquals(atPanel.getAllVisibleStories().size(), storiesOriginalCount);
        
        logger.info("Cancel story filter from breadcrumb");
        atPanel.getFirstProblemStory().expand();
        Assert.assertEquals(atPanel.getAllVisibleStories().size(), 1);
        breadcrumbExperiences.cancelTilesFilter();
        Assert.assertEquals(atPanel.getAllVisibleStories().size(), storiesOriginalCount);
        
        logger.info("Checking the filter indicator is removed from breadcrumb bar");
        Assert.assertFalse(breadcrumbExperiences.getCancelTilesFilterElement().isDisplayed());
    }
}
