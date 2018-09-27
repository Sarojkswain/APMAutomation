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

public class EditFilter extends UITest {

    private static final Logger logger = Logger.getLogger(EditFilter.class);

    @Test
    public void testEditFilterInTrendView() throws Exception {
        UI ui = getUI();

        logger.info("should log into APM Server");
        ui.login();

        logger.info("should switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();

        logger.info("should turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("should select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("should create Type as first filter");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        ui.getFilterBy().add("Type");

        logger.info("should uncheck the 'Show Entry points' option");
        ui.getFilterBy().uncheckShowEntryElement();
        ui.waitForWorkIndicator();

        logger.info("verify the filter item 'type' is present");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);
        String firstFilterName = typeFilterItem.getName();
        Assert.assertTrue(firstFilterName.toLowerCase().contains("type"), "First filter item is '"
            + firstFilterName + "' instead of the expected 'type'");

        logger.info("should create Name filter");
        FilterMenu nameFilterItem = ui.getFilterBy().add("Name");

        logger.info("should see the Name filter added");
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(1).contains("Name"));
        int cardCount1 = ui.getTrendCards().getListOfCards().size();

        logger.info("should de-select all options in the Name filter");
        nameFilterItem.expandDropDownMenu();
        int nameMenuValueCount = nameFilterItem.getListOfMenuItems().size();
        int nameMenuSelectedCount = nameFilterItem.getListOfSelectedItems().size();
        Assert.assertEquals(nameMenuSelectedCount, nameMenuValueCount);
        nameFilterItem.uncheckSelectAll();
        Assert.assertTrue(nameFilterItem.getMenuOK().isEnabled());

        logger.info("should de-select one option in the Name filter");
        nameFilterItem.checkSelectAll();
        Assert.assertTrue(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        String filterName = nameFilterItem.getArrayOfMenuItemsNames()[0];
        nameFilterItem.uncheckMenuOption(filterName);

        logger.info("should not confirm the selection");
        nameFilterItem.cancelMenu();
        nameFilterItem.expandDropDownMenu();
        int cardCount2 = ui.getTrendCards().getListOfCards().size();
        Assert.assertEquals(cardCount2, cardCount1);

        logger.info("should de-select one option in the Name filter and confirm");
        nameFilterItem.uncheckMenuOption(filterName);
        nameFilterItem.confirmMenu();

        logger.info("should check that the de-select option is not selected");
        nameFilterItem.expandDropDownMenu();
        Assert.assertFalse(nameFilterItem.getMenuItem(filterName).isCheckboxChecked());
        Assert.assertFalse(nameFilterItem.getMenuSelectAll().isCheckboxChecked());

        logger.info("should select the previously de-selected option");
        nameFilterItem.checkMenuOption(filterName);
        Assert.assertTrue(nameFilterItem.getMenuItem(filterName).isCheckboxChecked());
        Assert.assertTrue(nameFilterItem.getMenuSelectAll().isCheckboxChecked());

        logger.info("should cancel the selection");
        nameFilterItem.cancelMenu();

        logger.info("should check that the de-select option is not selected");
        nameFilterItem.expandDropDownMenu();
        Assert.assertFalse(nameFilterItem.getMenuItem(filterName).isCheckboxChecked());
        Assert.assertFalse(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        nameFilterItem.cancelMenu();

        logger.info("should select just SERVLET in the Type filter");
        typeFilterItem.expandDropDownMenu();
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), typeFilterItem
            .getListOfMenuItems().size());
        typeFilterItem.uncheckSelectAll();
        typeFilterItem.checkMenuOption("SERVLET");
        Assert.assertFalse(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        typeFilterItem.confirmMenu();
        Assert.assertEquals(ui.getTrendCards().getListOfCards().size(), 1);

        logger.info("should do cleanup tasks");
        ui.cleanup();
    }
}
