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
package com.ca.apm.test.atc.filtersnavigation;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Universe;

public class MapView extends UITest {

    private static final Logger logger = Logger.getLogger(MapView.class);

    public static final String NAME_ITEM = "TradeOptions|service";

    @Test
    public void testMapView() throws Exception {
        UI ui = getUI();
        logger.info("should log into APM Server");
        ui.login();

        logger.info("should switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("should turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();
        
        ui.getTopNavigationPanel().selectUniverse(Universe.DEFAULT_UNIVERSE);

        logger.info("should select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("should create Type as first filter");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        ui.getFilterBy().add("Type");

        logger.info("should uncheck the 'Show Entry points' option");
        ui.getFilterBy().uncheckShowEntryElement();

        logger.info("verify the default filter item 'type' is present");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);
        String firstFilterName = typeFilterItem.getName();
        Assert.assertTrue(firstFilterName.toLowerCase().contains("type"), "First filter item is '"
            + firstFilterName + "' instead of the expected 'type'");

        ui.getMapviewPage().waitForReload();

        logger.info("should create Name filter");
        FilterMenu nameFilterItem = ui.getFilterBy().add("Name");

        logger.info("should see the Name filter added");
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(1).contains("Name"));

        logger.info("should de-select one option in the Name filter");
        nameFilterItem.expandDropDownMenu();
        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), nameFilterItem
            .getListOfMenuItems().size());
        Assert.assertTrue(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        nameFilterItem.uncheckMenuOption(NAME_ITEM);
        Assert.assertFalse(nameFilterItem.getMenuItem(NAME_ITEM).isCheckboxChecked());

        logger.info("should cancel the selection and re-open the menu again");
        nameFilterItem.cancelMenu();
        nameFilterItem.expandDropDownMenu();
        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), nameFilterItem
            .getListOfMenuItems().size());
        Assert.assertTrue(nameFilterItem.getMenuItem(NAME_ITEM).isCheckboxChecked());
        Assert.assertTrue(nameFilterItem.getMenuSelectAll().isCheckboxChecked());

        logger.info("should de-select one option in the Name filter and confirm");
        nameFilterItem.uncheckMenuOption(NAME_ITEM);
        nameFilterItem.confirmMenu();

        logger.info("should check that the previously de-selected option is not selected");
        nameFilterItem.expandDropDownMenu();
        Assert.assertFalse(nameFilterItem.getMenuItem(NAME_ITEM).isCheckboxChecked());
        Assert.assertFalse(nameFilterItem.getMenuSelectAll().isCheckboxChecked());

        logger.info("should select just one option in the Name filter");
        nameFilterItem.checkSelectAll();
        Assert.assertTrue(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        nameFilterItem.uncheckSelectAll();
        nameFilterItem.checkMenuOption(NAME_ITEM);
        Assert.assertFalse(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), 1);
        Assert.assertTrue(nameFilterItem.getMenuItem(NAME_ITEM).isCheckboxChecked());
        nameFilterItem.confirmMenu();

        logger.info("should select just DATABASE in the Type filter");
        typeFilterItem.expandDropDownMenu();
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), typeFilterItem
            .getListOfMenuItems().size());
        typeFilterItem.uncheckSelectAll();
        typeFilterItem.checkMenuOption("SERVLET"); // originally was here DATABASE
        typeFilterItem.confirmMenu();

        logger.info("should check the Type menu");
        typeFilterItem.expandDropDownMenu();
        Assert.assertFalse(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), 1);
        typeFilterItem.cancelMenu();

        logger.info("should check the Name menu");
        nameFilterItem.expandDropDownMenu();
        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), 1);
        Assert.assertTrue(nameFilterItem.getMenuItem(NAME_ITEM).isCheckboxChecked());
        nameFilterItem.cancelMenu();

        logger.info("should delete the Name filter");
        ui.getFilterBy().remove(nameFilterItem);
        nameFilterItem = null;

        logger.info("should create wsOperation filter");
        FilterMenu wsOperationFilterItem = ui.getFilterBy().add("wsOperation");

        logger.info("should see the wsOperation filter added");
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(1).contains("wsOperation"));
        wsOperationFilterItem.expandDropDownMenu();
        Assert.assertTrue(wsOperationFilterItem.getListOfSelectedItems().size() > 0);
        Assert.assertTrue(wsOperationFilterItem.getMenuSelectAll().isCheckboxChecked());
        wsOperationFilterItem.cancelMenu();

        logger.info("should disable the Type filter");
        typeFilterItem.disableFilterItem();
        wsOperationFilterItem.expandDropDownMenu();
        Assert.assertEquals(wsOperationFilterItem.getListOfSelectedItems().size(),
            wsOperationFilterItem.getListOfMenuItems().size());
        Assert.assertTrue(wsOperationFilterItem.getMenuSelectAll().isCheckboxChecked());
        wsOperationFilterItem.cancelMenu();

        logger.info("should re-enable the Type filter");
        typeFilterItem.enableFilterItem();

        logger.info("should check the wsOperation filter menu is updated after re-enable");
        wsOperationFilterItem.expandDropDownMenu();
        Assert.assertTrue(wsOperationFilterItem.getListOfSelectedItems().size() > 0);
        Assert.assertTrue(wsOperationFilterItem.getMenuSelectAll().isCheckboxChecked());
        wsOperationFilterItem.cancelMenu();

        logger.info("should check the Type filter menu is updated after re-enable");
        typeFilterItem.expandDropDownMenu();
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), 1);
        Assert.assertFalse(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        typeFilterItem.cancelMenu();

        logger.info("should do cleanup tasks");
        ui.cleanup();
    }
}
