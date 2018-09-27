/*
 * Copyright (c) 2015 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.test.atc.search;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.UI;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import static org.junit.Assert.*;

public class SearchTest extends UITest {

    @Test(groups = "failing")
    public void testDataBrushing() throws Exception {
        UI ui = getUI();
        
        logger.info("log into APM Server");
        ui.login();
        
        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();
        
        logger.info("select the desired perspective");
        if (!ui.getPerspectivesControl().isPerspectiveActive("Type")) {
            ui.getPerspectivesControl().selectPerspectiveByName("Type");
        }
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive("Type"));
        
        logger.info("create Type filter");
        FilterMenu type = ui.getFilterBy().add("Type");
        type.expandDropDownMenu();
        type.uncheckSelectAll();
        type.checkMenuOption("BUSINESSTRANSACTION");
        assertTrue(type.getMenuItem("BUSINESSTRANSACTION").isCheckboxChecked());
        type.confirmMenu();
        
        logger.info("create \"Name\" data brushing filter");
        ui.getRibbon().expandHighlightToolbar();
        FilterMenu nameBrusher = ui.getDataBrushing().add("Name");

        logger.info("select one item in the \"Name\" data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Place Order");
        assertEquals(1, nameBrusher.getListOfSelectedItems().size());
        nameBrusher.confirmMenu();

        logger.info("see nodes highlighted");
        WebElement bTranNode = ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN");
        assertTrue(ui.getCanvas().isHighlighted(bTranNode));
        ui.getCanvas().expandGroup(bTranNode);
        getUI().getCanvas().getCtrl().fitAllToView();
        assertTrue(nameBrusher.getMenuContainer().getText().contains("Name (1)"));
        assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Place Order")));

        logger.info("enter search string that is not matching any node");
        ui.getSearch().inputSearch("AAA");
        assertEquals(0, ui.getSearch().getResultsCount());

        logger.info("see data brushing disabled");
        assertFalse(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Place Order")));
        assertFalse(ui.getDataBrushing().getShowMeBtn().isEnabled());
        assertFalse(nameBrusher.isAllOnAllOffMenuChecked());
        assertFalse(nameBrusher.getDropdownToggle().isEnabled());

        logger.info("enter search string");
        ui.getSearch().inputSearch("Login");
        assertTrue(ui.getSearch().getResultsCount() > 0);

        ui.getCanvas().getCtrl().fitAllToView();

        logger.info("see that data brushing is still disabled");
        assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Login")));
        assertFalse(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Place Order")));
        assertFalse(ui.getDataBrushing().getShowMeBtn().isEnabled());
        assertFalse(nameBrusher.isAllOnAllOffMenuChecked());
        assertFalse(nameBrusher.getDropdownToggle().isEnabled());

        logger.info("close the search dialog");
        ui.getSearch().close();

        logger.info("close the search dialog and see data brushing re-enabled");
        assertFalse(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByNameSubstring("Login")));
        assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Place Order")));
        assertTrue(ui.getDataBrushing().getShowMeBtn().isEnabled());
        assertTrue(nameBrusher.isAllOnAllOffMenuChecked());
        assertTrue(nameBrusher.getDropdownToggle().isEnabled());

        logger.info("do cleanup tasks");
        ui.cleanup();
    }

    @Test
    public void testGroupedItems() throws Exception {
        UI ui = getUI();

        logger.info("log into APM Server");
        ui.login();

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("select the desired perspective");
        if (!ui.getPerspectivesControl().isPerspectiveActive("Type")) {
            ui.getPerspectivesControl().selectPerspectiveByName("Type");
        }

        assertTrue(ui.getPerspectivesControl().isPerspectiveActive("Type"));

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("create Type filter");
        FilterMenu type = ui.getFilterBy().add("Type");
        type.expandDropDownMenu();
        type.uncheckSelectAll();
        type.checkMenuOption("SERVLET");
        assertTrue(type.getMenuItem("SERVLET").isCheckboxChecked());
        type.confirmMenu();

        logger.info("enter search string");
        ui.getSearch().inputSearch("servlet");
        int resCount1 = ui.getSearch().getResultsCount();
        assertTrue(resCount1 > 0);
        assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByNameSubstring("SERVLET")));

        logger.info("expand the grouped element");
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("SERVLET"));
        int resCount2 = ui.getSearch().getResultsCount();
        assertTrue(resCount2 != resCount1);
        assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByNameSubstring("service")));

        logger.info("collapse the group");
        ui.getCanvas().collapseGroup(ui.getCanvas().getNodeByNameSubstring("SERVLET"));
        resCount1 = ui.getSearch().getResultsCount();
        assertTrue(resCount2 != resCount1);
        assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByNameSubstring("SERVLET")));

        logger.info("do cleanup tasks");
        ui.cleanup();
    }

    @Test(groups = "failing")
    public void testSearchNavigation() throws Exception {
        UI ui = getUI();
        logger.info("log into APM Server");
        ui.login();

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("group items using selected group");

        boolean applicationGroupPresent = ui.getPerspectivesControl().isPerspectivePresent("Application");
        if (!applicationGroupPresent) {
            ui.getPerspectivesControl().addPerspective("Application", false);
        }

        ui.getMapviewPage().waitForReload();
        
        ui.getPerspectivesControl().selectPerspectiveByName("Application");
        ui.getCanvas().waitForUpdate();
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive("Application"));

        logger.info("enter search string");
        ui.getSearch().inputSearch("Place");
        assertTrue(ui.getSearch().getResultsCount() > 0);

        logger.info("cycle through map");
        String checkNode = "TradeService";
        Point loc1, loc2;
        String order1, order2;

        WebElement selectedNode = null;

        for (WebElement node : ui.getCanvas().getListOfHighlightedNodes()) {
            if (ui.getCanvas().getNodeName(node).contains(checkNode)) {
                selectedNode = node;
                break;
            }
        }

        assertTrue("node " + checkNode + " not found", selectedNode != null);

        logger.info("expand the grouped element");

        ui.getCanvas().expandGroup(selectedNode);
        ui.getCanvas().getCtrl().fitAllToView();

        logger.info("cycle through map when grouped element is expanded");
        checkNode = "PlaceOrder|service";

        selectedNode = null;
        for (WebElement node : ui.getCanvas().getListOfHighlightedNodes()) {
            if (ui.getCanvas().getNodeName(node).equals(checkNode)) {
                selectedNode = node;
                break;
            }
        }
        assertTrue("node " + checkNode + " not found", selectedNode != null);

        loc1 = ui.getCanvas().getNodeByNameSubstring(checkNode).getLocation();
        order1 = ui.getSearch().getResultOrder();
        ui.getSearch().clickNext();
        loc2 = ui.getCanvas().getNodeByNameSubstring(checkNode).getLocation();
        order2 = ui.getSearch().getResultOrder();
        assertNotEquals(loc1, loc2);
        assertNotEquals(order1, order2);

        ui.getSearch().clickNext();
        loc1 = ui.getCanvas().getNodeByNameSubstring(checkNode).getLocation();
        order1 = ui.getSearch().getResultOrder();
        assertNotEquals(loc1, loc2);
        assertNotEquals(order1, order2);

        logger.info("close the search dialog");
        ui.getSearch().close();
        assertFalse(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByNameSubstring("PlaceOrder|service")));

        logger.info("do cleanup tasks");

        if (!applicationGroupPresent) {
            ui.getLeftNavigationPanel().goToPerspectives();
            ui.getPerspectiveSettings().deletePerspective("Application");
        }
        
        ui.logout();
        ui.cleanup();
    }

    @Test
    public void testSearchUI() throws Exception {
        UI ui = getUI();
        logger.info("log into APM Server");
        ui.login();

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("select the desired perspective");
        if (!ui.getPerspectivesControl().isPerspectiveActive("Type")) {
            ui.getPerspectivesControl().selectPerspectiveByName("Type");
        }

        assertTrue(ui.getPerspectivesControl().isPerspectiveActive("Type"));

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("enter search string");
        ui.getSearch().inputSearch("Serv");
        int resCount1 = ui.getSearch().getResultsCount();
        assertTrue(resCount1 > 0);

        logger.info("expand the grouped element");
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("SERVLET"));
        int resCount2 = ui.getSearch().getResultsCount();
        assertNotEquals(resCount2, resCount1);

        logger.info("do cleanup tasks");
        ui.cleanup();
    }
}
