package com.ca.apm.test.atc.trendview;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.TopNavigationPanel;
import com.ca.apm.test.atc.common.TrendCards;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UrlUtils;
import com.ca.apm.test.atc.common.Utils;

public class DetailViewTest extends UITest {

    private final String PERSPECTIVE_NAME = "DetailViewTestPerspective";
    private final String[] PERSPECTIVE = {"Type", "Name", "Application"};
    private final String DETAIL_VIEW_NAME_PARAM = "dvn";
    private final String DETAIL_VIEW_VALUE_PARAM = "dvv";
    private String trendCardName;
    private String trendCardColumnName;

    private UI ui;
    private LeftNavigationPanel leftNav;
    private TopNavigationPanel topNav;
    
    private void init() {
        ui = getUI();
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();
    }

    @Test
    public void testDetailView() throws Exception {
        
        init();
        ui.login(Role.ADMIN);
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getTimeline().turnOffLiveMode();

        // Add temporary perspective
        ui.getLeftNavigationPanel().goToPerspectives();

        if (!ui.getPerspectiveSettings().isPerspectivePresent(PERSPECTIVE_NAME)) {
            ui.getPerspectiveSettings().displayAddPerspectiveDialog();
            ui.getPerspectiveSettings()
                    .addMultiLevelPerspective(PERSPECTIVE, PERSPECTIVE_NAME, false);
        }

        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getPerspectivesControl().selectPerspectiveByName(PERSPECTIVE_NAME);
        
        TrendCards trendCards = ui.getTrendCards();
        trendCards.waitForUpdate();
        
        trendCardColumnName = trendCards.getTabHeaderText(1);

        List<String> cardNames = trendCards.getArrayOfCardNamesWithinTab(1);
        
        for (int i = 0; i < cardNames.size(); i++) {
            // Test first 3 cards
            if (i >= 3) {
                break;
            }
            
            trendCardName = cardNames.get(i);
            testDetailViewForColumn();
        }

        cleanUp();
    }
    
    private void testDetailViewForColumn() throws Exception {
        ui.getTrendCards().getLinkToMapByNodeName(trendCardName).click();
        String tabWindowHandler = Utils.switchToAnotherTab(ui.getDriver(), 2);

        ui.getDetailView().waitForUpdate();
        
        // check URL
        String url = ui.getDriver().getCurrentUrl();
        assertNull(UrlUtils.getQueryStringParam(url, DETAIL_VIEW_NAME_PARAM));
        assertNull(UrlUtils.getQueryStringParam(url, DETAIL_VIEW_VALUE_PARAM));

        // check certain Top navigation controls are not visible in Detail view
        assertFalse(topNav.isUniverseDropdownPresent(), "Universe selection combo should be hidden in the Isolation view");
        assertFalse(topNav.isUserFieldPresent(), "User field should be hidden in the Isolation view");
        
        // check header
        assertTrue(ui.getDetailView().getDetailViewBar().getText().contains(trendCardColumnName));
        assertTrue(ui.getDetailView().getDetailViewBar().getText().contains(trendCardName));

        FilterBy filter = ui.getFilterBy();
        assertFalse(filter.isSaveUniverseButtonVisible());

        // check filter
        filter.showPanel();
        assertEquals(filter.getAddedActiveFilterItemsCount(), 1);
        assertTrue(filter.getFilterItemCount() == 1);
        assertTrue(filter.getCountOfFilterItemsInAnyBTCoverage() == 1);

        FilterMenu f1 = filter.getFilterItem(0);
        assertEquals(trendCardColumnName, f1.getName().trim());
        f1.expandDropDownMenu();
        assertEquals(f1.getMenuSelectedItemsCountPerFilterTitle(), 1);
        f1.cancelMenu();

        // check perspective drop-down
        PerspectivesControl pspCtrl = ui.getPerspectivesControl();

        if (ui.getCanvas().getArrayOfNodeNames().length > 0) {
            assertTrue(pspCtrl.isActivePerspectiveFaded());
            assertTrue(pspCtrl.getActivePerspectiveTooltip().startsWith("Temporary Perspective: "));
    
            // check map group
            assertNotNull(ui.getCanvas().getNodeByNameSubstring(trendCardName));
        }
        
        Utils.closeTab(ui.getDriver(), tabWindowHandler);
    }

    private void cleanUp() throws Exception {
    	leftNav.goToPerspectives();
        ui.getPerspectiveSettings().deletePerspective(PERSPECTIVE_NAME);
    }
}
