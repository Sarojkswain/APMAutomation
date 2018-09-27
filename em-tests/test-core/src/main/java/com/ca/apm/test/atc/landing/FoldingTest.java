package com.ca.apm.test.atc.landing;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.SummaryPanel;

public class FoldingTest extends UITest {

    private UI ui;
    private LandingPage landingPage;
    private final static int ANIMATION_DELAY = 500;
    
    private final String TILE_TO_DRILL_DOWN_L1 = "Experiences in";

    private void init() throws Exception {
        ui = getUI();
        ui.login();

        landingPage = ui.getLandingPage();
    }

    @Test
    /**
     * Test collapsing of summary panel
     */
    public void testSummaryPanelFolding() throws Exception {
        init();
        
        landingPage.drillDownTheTile(TILE_TO_DRILL_DOWN_L1);
        
        SummaryPanel summaryPanel = landingPage.getSummaryPanel();
        
        logger.info("Check the summary panel is closed by default");
        Assert.assertFalse(summaryPanel.isPanelExpanded());
        
        logger.info("Opening summary panel");
        summaryPanel.expandPanel();
        
        logger.info("Check the summary panel has expanded");
        Assert.assertTrue(summaryPanel.isPanelExpanded());

        logger.info("Closing summary panel");
        summaryPanel.collapsePanel();
        
        logger.info("Checking summary panel is closed");
        Assert.assertFalse(summaryPanel.isPanelExpanded());

        logger.info("Check the panel state is preserved after page reload");
        summaryPanel.expandPanel();

        logger.info("Refreshing page");
        this.ui.pageRefresh();
        
        logger.info("Waiting for work indicator to disappear");
        Utils.waitWhileVisible(ui.getDriver(), By.cssSelector(".summary-content .work-indicator"));
        
        // Reset stale element
        summaryPanel = landingPage.getSummaryPanel();

        logger.info("Check the summary panel state was restored after page refresh");
        Assert.assertTrue(summaryPanel.isPanelExpanded());
    }
    
    @Test
    /**
     * Test folding of the right side problems/anomalies panel aka Story panel aka AT panel
     */
    public void testProblemsAnomaliesPanelFolding() throws Exception {
        init();

        landingPage.waitForTilesToLoad();
        landingPage.getTiles().get(0).drillDown();
        
        logger.info("Panel is open by default");
        Assert.assertTrue(landingPage.getATPanel().isATPanelVisible());
        
        logger.info("Panel can be expanded");
        landingPage.getATPanel().expand();
        Assert.assertTrue(landingPage.getATPanel().isATPanelVisible(), "Problems and anomalies visible");
        
        logger.info("Open state is preserved after page reload");
        ui.pageRefresh();
        Assert.assertTrue(landingPage.getATPanel().isATPanelVisible(), "Problems and anomalies visible");
        
        logger.info("Closed state is preserved after page reload");
        landingPage.getATPanel().collapse();
        Assert.assertFalse(landingPage.getATPanel().isATPanelVisible(), "Problems and anomalies visible");        
        ui.pageRefresh();
        Assert.assertFalse(landingPage.getATPanel().isATPanelVisible(), "Problems and anomalies visible");

        
    }
    
    @Test
    /**
     * Test folding of Relationship Flow panel in Notebook
     */
    public void testNotebookRelationshipFlowFolding() throws Exception {
        init();
        
        Utils.waitWhileVisible(ui.getDriver(), By.cssSelector(".summary-content .work-indicator"));
        landingPage.getTiles().get(0).openNotebook();
        
        WebElement homeDetailMapElement = this.landingPage.findElement(By.cssSelector("#relationship-flow-container .home-detail-map"));
        WebElement graphContent = ui.getElementProxy(By.id("graphContent"));
        int homeDetailMapElementOriginalHeight = getPixelPosition(String.valueOf(homeDetailMapElement.getSize().getHeight()));
        
        WebElement toggleElement = landingPage.getElement(By.cssSelector("#relationship-flow-container .attribute-table-header"));

        logger.info("Closing Relationship flow container");
        toggleElement.click();
        
        logger.info("Waiting for folding animation to finish");
        Utils.sleep(ANIMATION_DELAY + 200);

        logger.info("Checking graph is collapsed");
        Assert.assertFalse(graphContent.isDisplayed());
        logger.info("Checking map is collapsed to zero height");
        Assert.assertEquals(getPixelPosition(String.valueOf(homeDetailMapElement.getSize().getHeight())), 0);
        logger.info("Checking map has collapsed class name");
        Assert.assertTrue(homeDetailMapElement.getAttribute("class").contains("collapsed"));
        
        logger.info("Opening Relationship flow panel");
        toggleElement.click();
        
        logger.info("Waiting for folding animation to finish");
        Utils.sleep(ANIMATION_DELAY + 200);
        
        logger.info("Checking graph is displayed");
        Assert.assertTrue(graphContent.isDisplayed());
        logger.info("Checking map is restored to original height");
        Assert.assertEquals(getPixelPosition(String.valueOf(homeDetailMapElement.getSize().getHeight())), homeDetailMapElementOriginalHeight);
        logger.info("Checking map has expanded class name");
        Assert.assertTrue(homeDetailMapElement.getAttribute("class").contains("expanded"));
    }
    
    /**
     * Get position value as integer, "px" and decimal points stripped
     * @param position Position CSS value
     * @return Position as integer
     */
    private int getPixelPosition(String position) {
        String positionString = position.replace("px", "");
        int decimalPosition = positionString.indexOf(".");
        
        if (decimalPosition != -1) {
            positionString = positionString.substring(0, decimalPosition);
        }
        
        return Integer.parseInt(positionString);
    }
}
