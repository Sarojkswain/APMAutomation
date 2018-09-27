package com.ca.apm.test.atc.trendview;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.DetailsPanel;
import com.ca.apm.test.atc.common.DetailsPanel.AttributeType;
import com.ca.apm.test.atc.common.TrendCards;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.AttributeRow;
import com.ca.apm.test.atc.common.element.PageElement;

/**
 * Test displaying the Component view (Details panel) in the Dashboard (Trend view).
 * 
 * Coverage of US121453.
 * 
 * @author strma15
 */
public class ComponentViewInDashboardTest extends UITest {

    private static final boolean LIVE = true;
    private static final boolean HISTORIC = false;
    
    private static final boolean MINIMIZED = true;
    private static final boolean MAXIMIZED = false;
    
    private UI ui;
    private TrendCards dashboard;
    private DetailsPanel componentView;

    private void init() throws Exception {
        ui = getUI();
        dashboard = ui.getTrendCards();
        componentView = ui.getDetailsPanel();

        ui.login(Role.ADMIN);
    }
    
    /**
     * Test that the component view (details panel) appears when clicked on the card and
     * that it disappears when clicked outside any card.
     * Test that the type of the selection is Group and the name of the selection
     * corresponds to what was clicked on.
     * @throws Exception 
     */
    @Test
    public void testClickOnCardsInLiveMode() throws Exception {
        doTestClickOnCardsInMode(LIVE, MAXIMIZED);
    }

    /**
     * Test that the component view (details panel) is displayed when clicked
     * on the card as well as when no card is selected (the same way as in the map).
     * Test that the type of the selection is Group and the name of the selection
     * corresponds to what was clicked on. If no card is selected there should
     * be the corresponding message in the component view.
     * @throws Exception 
     */
    @Test
    public void testClickOnCardsInHistoricMode() throws Exception {
        doTestClickOnCardsInMode(HISTORIC, MAXIMIZED);
    }
    
    /**
     * Test that the component view (details panel) keeps minimized 
     * since it has been minimized in Live mode.
     * @throws Exception 
     */
    @Test
    public void testClickOnCardsInLiveModeWithCompViewMinimized() throws Exception {
        doTestClickOnCardsInMode(LIVE, MINIMIZED);
    }

    /**
     * Test that the component view (details panel) keeps minimized 
     * since it has been minimized in Historic mode.
     * @throws Exception 
     */
    @Test
    public void testClickOnCardsInHistoricModeWithCompViewMinimized() throws Exception {
        doTestClickOnCardsInMode(HISTORIC, MINIMIZED);
    }
    
    private void doTestClickOnCardsInMode(final boolean isLiveMode, final boolean minimize) throws Exception {
        init();

        ui.getLeftNavigationPanel().goToDashboardPage();
        
        if (isLiveMode) {
            ui.getTimeline().turnOnLiveMode();
        } else {
            ui.getTimeline().turnOffLiveMode();
        }

        dashboard.waitForUpdate();
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        dashboard.waitForUpdate();
        final WebElement container = dashboard.getTrendCardsContainer();

        /* Do not change the old plain for cycle to a more clever and sexy Java5 for cycle with iteration over tabs. 
         * We want to get the reference to the tab on the beginning of each cycle to minimize the risk of Stale element exception. */
        int tabsCount = dashboard.getTabs().size();
        for (int i = 0; i < tabsCount; i++) {
            final int finalI = i;
            Utils.runAgainOnStaleReferenceException(new Runnable() {
                @Override
                public void run() {
                    boolean first = (finalI == 0);

                    PageElement tab = dashboard.getTab(finalI);
                    List<String> cardNames = dashboard.getListOfCardNamesWithinElement(tab);
                    for (String cardName : cardNames) {
                        WebElement cardBody = dashboard.getCardHeaderByNodeName(cardName);

                        // Click on a card
                        logger.info("Selecting card: {}", cardName);
                        cardBody.click();

                        componentView.waitUntilVisible();
                        Assert.assertTrue(componentView.isDetailsPanelDisplayed(),
                            "Component view should be displayed when a card is selected.");

                        if (minimize) {
                            if (first) {
                                first = false;
                                if (!componentView.isDetailsPanelMinimized()) {
                                    componentView.getMinimizeButton().click();
                                }
                            } else {
                                Assert.assertTrue(componentView.isDetailsPanelMinimized(),
                                    "Component view should have been minimized.");
                            }
                        } else {
                            if (first) {
                                first = false;
                                if (componentView.isDetailsPanelMinimized()) {
                                    componentView.getMaximizeButton().click();
                                }
                            } else {
                                Assert.assertFalse(componentView.isDetailsPanelMinimized(), "Component view should not be minimized.");
                            }

                            componentView.waitUntilAttributeTableDataLoaded(AttributeType.IDENT_ATTRIBUTES);

                            List<AttributeRow> attribs = componentView.getAttributeRowsByName(AttributeType.IDENT_ATTRIBUTES, "Type");
                            Assert.assertEquals(attribs.size(), 1);
                            String compViewType = attribs.get(0).getEndTimeValueCell().getText();
                            Assert.assertEquals(compViewType, cardName);
                        }

                        // Click outside of cards
                        container.click();

                        if (isLiveMode) {
                            Assert.assertFalse(componentView.isDetailsPanelDisplayed(),
                                "Component view should not be displayed in live mode when no card is selected.");
                        } else {
                            componentView.waitUntilVisible();
                            Assert.assertTrue(componentView.isDetailsPanelDisplayed(),
                                "Component view should be displayed in historic mode even if no card is selected.");
                            Assert.assertEquals(componentView.isDetailsPanelMinimized(), minimize,
                                "Component view should " + (minimize ? "be" : "not be")
                                    + " minimized");

                            if (!minimize) {
                                Assert.assertTrue(componentView.isNoComponentSelected(),
                                    "In Component view there should be a message displayed that no component is selected.");
                            }
                        }
                    }
                }
            });
        }

        ui.cleanup();
    }
}
