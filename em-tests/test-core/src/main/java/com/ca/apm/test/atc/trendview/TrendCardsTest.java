package com.ca.apm.test.atc.trendview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.wily.apm.model.util.Assert;
import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.AttributeRulesTable;
import com.ca.apm.test.atc.common.DetailsPanel;
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.Ribbon;
import com.ca.apm.test.atc.common.Timeline;
import com.ca.apm.test.atc.common.TrendCards;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.PageElement;

public class TrendCardsTest extends UITest {

    private UI ui;
    private Ribbon ribbon;
    private Timeline timeline;
    private TrendCards trendCards;
    private AttributeRulesTable attributeRules;
    private PerspectivesControl perspectiveControl;
    
    private void init() throws Exception {
        ui = getUI();
        ribbon = ui.getRibbon();
        timeline = ui.getTimeline();
        trendCards = ui.getTrendCards();
        attributeRules = ui.getAttributeRulesTable();
        perspectiveControl = ui.getPerspectivesControl();
        ui.login(Role.ADMIN);
    }

    @Test
    public void testTrendCardsSelection() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToDashboardPage();
        timeline.turnOffLiveMode();
        
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        
        logger.info("Check if trend cards are selectable");
        for (WebElement card : cards) {
            card.click();
            
            // Do not test if empty card was selected
            // It is invisible in the dashboard, selection is not activated
            if (card.getAttribute("data-vertexids").length() == 0) {
                continue;
            }

            Assert.isTrue(card.getAttribute("class").matches("^.*selected.*$"));
        }
        
        logger.info("Click on Trend cards container to un-select all cards");
        trendCards.getTrendCardsPanel().click();
        
        logger.info("Check trend cards selection is cleared");
        for (WebElement card : cards) {
            Assert.state(card.getAttribute("class").matches("^.*selected.*$") == false, "Card was not un-selected");
        }
    }
    
    /**
     * Test selection persistence on dashboard. When a card is selected, the only way to 
     * cancel that selection should be the click on the map canvas (outside of he selected card)
     * @throws Exception
     */
    @Test
    public void testTrendCardsSelectionPersistenceOnPerspectiveSelect() throws Exception {
        
        logger.info("Login");
        init();
        
        logger.info("Go to dashboard, expand timeline panel");
        
        ui.getLeftNavigationPanel().goToDashboardPage();
        ribbon.expandTimelineToolbar();
        
        logger.info("Disable Live mode if enabled");
        timeline.turnOffLiveMode();
        
        logger.info("Click the first trend card");
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        cards.get(0).click();
        
        logger.info("Click the perspective dropdown button");
        WebElement perspectiveButton = perspectiveControl.getDropdown().findElement(By.cssSelector("button"));
        perspectiveButton.click();
        
        logger.info("Assert the selected card is still selected");
        String cardClass = cards.get(0).getAttribute("class");
        Assert.isTrue(cardClass.matches("^.*selected.*$"), "Card must remain selected on perspective dropdown click");
    }
    
    /**
     * Test selection is cancelled after the card disappears from the dashboard
     * @throws Exception
     */
    @Test
    public void testTrendCardsSelectionCancelOnCardRemove() throws Exception {
        
        logger.info("Login");
        init();
        
        logger.info("Go to dashboard, expand timeline panel");
        ui.getLeftNavigationPanel().goToDashboardPage();
        ribbon.expandTimelineToolbar();
        
        logger.info("Disable Live mode if enabled");
        timeline.turnOffLiveMode();
        
        logger.info("Set \"Type\" perspective, opend Filters panel");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");
        ui.getRibbon().expandFilterToolbar();
        
        logger.info("Add filter \"Type\" (all options should be checked by default)");
        FilterBy filterBy = ui.getFilterBy();
        filterBy.add("Type");
        
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        WebElement card = cards.get(0);
        String cardName = card.findElement(By.cssSelector("a[title*='View Details']")).getText();
        
        logger.info("Click the first trend card to select");
        card.click();
        
        logger.info("Change filter, uncheck the option that contains the selected card name");
        FilterMenu filterMenu = filterBy.getFilterItem(0);
        filterMenu.expandDropDownMenu();

        for (WebElement filterItem : filterMenu.getListOfMenuItems()) {
            if (filterItem.getText().trim().equals(cardName)) {
                filterItem.click();
            }
        }
        
        filterMenu.confirmMenu();
        filterBy.uncheckShowEntryElement();

        logger.info("Assert the selection is gone by checking that the attributes panel has been reset");
        ui.getElementProxy(By.cssSelector("attribute-grid")).waitForAttributeChange("selected-ids");
        
        String selectedIds = attributeRules.getAttributeRulesPanel().getAttribute("selected-ids");
        Assert.isTrue(selectedIds.length() == 0, "Attribute table must not show elements when selected node is removed from the dashboard");
    }
    
    /**
     * Test the dashboard cards selection is cancelled on dashboard clear. This will happen
     * when no trend cards are shown due to filtering options or time range with no data
     * @throws Exception
     */
    @Test
    public void testTrendCardsSelectionCancelOnDashboardClear() throws Exception {
        
        logger.info("Login");
        init();
        
        logger.info("Go to dashboard, expand timeline panel");
        ui.getLeftNavigationPanel().goToDashboardPage();
        ribbon.expandTimelineToolbar();
        
        logger.info("Disable Live mode if enabled");
        timeline.turnOffLiveMode();
        
        logger.info("Click the first trend card to select");
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        cards.get(0).click();
        
        logger.info("Move the time range beyond the maximum allowed range");
        timeline.zoomOut(1000);        
        WebElement firstLabel = timeline.getFirstMinorLabelElement();
        firstLabel.click();
        
        logger.info("Assert the selection is cancelled by checking if the attributes table panel has been reset");
        String selectedIds = attributeRules.getAttributeRulesPanel().getAttribute("selected-ids");
        Assert.isTrue(selectedIds.length() == 0, "Attribute table must not show elements when selected node is not on the dashboard");
    }
    
    @Test
    public void testTrendCardSelectionAfterLiveModeRefresh() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getRibbon().expandTimelineToolbar();
        timeline.turnOnLiveMode();
        
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        
        cards.get(0).click();
        
        // Make sure card was selected
        Assert.isTrue(cards.get(0).getAttribute("class").indexOf("selected") != -1);
        
        ui.waitForAutorefresh();

        // Refresh cards elements
        cards = trendCards.getListOfCardWrappers();
        Assert.isTrue(cards.get(0).getAttribute("class").indexOf("selected") != -1);
    }
    
    @Test
    public void testTrendCardsHighlighting() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getRibbon().expandTimelineToolbar();
        timeline.turnOffLiveMode();
        
        // Increase time range to get events
        timeline.setRange(Timeline.DAYS);
        
        // Test highlight of each event types
        timeline.checkStatusChangeCheckbox();
        timeline.checkTopologicalChangeCheckbox();
        timeline.checkAttributeChangeCheckbox();

        String[] eventTypes = {Timeline.STATUS_CHANGE, Timeline.TOPOLOGICAL_CHANGE, Timeline.ATTRIBUTE_CHANGE};
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        
        for (String eventType : eventTypes) {
            
            List<PageElement> events = timeline.getInRangeEventsByType(eventType);
            
            if (events.size() == 0) {
                logger.warn("highlighting of {} events not tested, no events found", eventType);
                continue;
            }
            
            for (PageElement event : events) {
                testEventHighlighting(event, cards);
            }
        }
    }
    
    protected void testEventHighlighting(PageElement event, List<PageElement> cards) {
        List<String> eventVertices = Arrays.asList(event.getAttribute("data-vertexids").split(","));
        
        // Click on event to activate highlight
        // Events too close to draggable time bars are not clickable, skip them
        try {
            event.click();
        } catch (WebDriverException e) {
            logger.warn("Skipping event click test, reason: {}", e.getMessage(), e);
            return;
        }
        
        for (WebElement card : cards) {
            List<String> vertices = new ArrayList<String>(eventVertices);
            List<String> cardVertices = Arrays.asList(card.getAttribute("data-vertexids").split(","));
            
            vertices.retainAll(cardVertices);
            
            if (vertices.size() > 0) {
                Assert.isTrue(card.findElements(By.cssSelector("div.trend-card-body.highlighted")).size() > 0, "Trend card with matching vertex IDs was NOT highlighted");
            } else {
                Assert.isTrue(card.findElements(By.cssSelector("div.trend-card-body.highlighted")).size() == 0, "Trend card with no vertex IDs was highlighted");
            }
        }
    }
    
    @Test
    public void testAttributeEventsClickHighlighting() throws Exception {
        init();
        
        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getRibbon().expandTimelineToolbar();
        timeline.turnOffLiveMode();
        
        // Increase time range to get events
        timeline.setRange(Timeline.DAYS);
        timeline.checkStatusChangeCheckbox();
        timeline.checkTopologicalChangeCheckbox();
        timeline.checkAttributeChangeCheckbox();

        DetailsPanel details = ui.getDetailsPanel();
        List<PageElement> events = details.getEventsRows(DetailsPanel.SECTION_EVENTS_STATUS);
        List<PageElement> topologicalEvents = details.getEventsRows(DetailsPanel.SECTION_EVENTS_TOPOLOGICAL);
        List<PageElement> attributeEvents = details.getEventsRows(DetailsPanel.SECTION_EVENTS_ATTRIBUTES);
        
        events.addAll(topologicalEvents);
        events.addAll(attributeEvents);
        
        for (WebElement event : events) {
            event.click();
            WebElement wrapper = event.findElement(By.cssSelector(".row-inner"));
            testCardHighlightPerVertexId(wrapper.getAttribute("data-vertex"));
        }
    }
    
    protected void testCardHighlightPerVertexId(String vertexId) {
        List<PageElement> cards = trendCards.getListOfCardWrappers();
        
        for (WebElement card : cards) {
            String cardVertices = card.getAttribute("data-vertexids");
            int idx = cardVertices.indexOf(vertexId);
            
            if (idx != -1) {
                WebElement body = card.findElement(By.className("trend-card-body"));
                Assert.isTrue(body.getAttribute("class").indexOf("highlighted") != -1);
            }
        }
    }
}
