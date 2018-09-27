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

public class Sorting extends UITest {

    private final static Logger logger = Logger.getLogger(Sorting.class);

    public static final String[] SORTING = new String[] {"Sort Z to A", "Sort A to Z"};

    @Test
    public void testSorting() throws Exception {
        UI ui = getUI();

        logger.info("should log into APM Server");
        ui.login();

        logger.info("should switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();

        logger.info("should select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("should create Type as first filter");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        ui.getFilterBy().add("Type");

        logger.info("verify the default filter item 'type' is present");
        Assert.assertEquals(1, ui.getFilterBy().getFilterItemCount());
        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);
        String firstFilterName = typeFilterItem.getName();
        Assert.assertTrue(firstFilterName.toLowerCase().contains("type"), "First filter item is '"
            + firstFilterName + "' instead of the expected 'type'");

        logger.info("should turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        int sLength = SORTING.length;
        for (int i = 0; i < sLength; i++) {
            addTest(SORTING[i], typeFilterItem);
        }

        logger.info("should do cleanup tasks");
        ui.cleanup();
    }

    private void addTest(String test, FilterMenu type) throws Exception {
        logger.info("should \"" + test + "\" the Type filter");

        type.expandDropDownMenu();
        type.sort(test);

        String[] menuItems = type.getArrayOfMenuItemsNames();

        type.confirmMenu();

        if ("Sort Z to A".equals(test)) {
            for (int i = 1; i < menuItems.length; i++) {
                Assert.assertTrue(menuItems[i - 1].compareTo(menuItems[i]) >= 0, "Expected that '" + menuItems[i - 1] + "' @" + (i - 1) + " >= '" + menuItems[i] + "' @" + i);
            }
        } else if ("Sort A to Z".equals(test)) {
            for (int i = 1; i < menuItems.length; i++) {
                Assert.assertTrue(menuItems[i - 1].compareTo(menuItems[i]) <= 0, "Expected that '" + menuItems[i - 1] + "' @" + (i - 1) + "' <= '" + menuItems[i] + "' @" + i);
            }
        }
    }
}
