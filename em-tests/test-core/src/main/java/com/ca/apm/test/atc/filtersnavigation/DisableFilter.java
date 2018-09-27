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
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.UI;

public class DisableFilter extends UITest {

    private final static Logger logger = Logger.getLogger(DisableFilter.class);

    @Test
    public void testDisableFilter() throws Exception {
        UI ui = getUI();
        FilterBy filter = ui.getFilterBy();

        logger.info("log into APM Server");
        ui.login();

        logger.info("switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("check the active filter count");
        Assert.assertEquals(filter.getFilterItemCount(), 0);

        logger.info("select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("create Type as first filter");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        filter.add("Type");
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(0).contains("Type"));

        logger.info("check the active filter count");
        filter.hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 1);
        filter.showPanel();
        Assert.assertEquals(filter.getAddedFilterItemsCount(), 1);
        
        logger.info("uncheck the 'Show Entry points' option");
        filter.uncheckShowEntryElement();
        ui.waitForWorkIndicator();

        logger.info("add filter Name");
        FilterMenu nameFilterItem = filter.add("Name");

        logger.info("see the Name filter added");
        Assert.assertTrue(filter.getFilterItemNames().get(1).contains("Name"));
        int cardCount1 = ui.getTrendCards().getListOfCards().size();
        
        logger.info("check the active filter count");
        filter.hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 2);
        filter.showPanel();
        Assert.assertEquals(filter.getAddedFilterItemsCount(), 2);

        logger.info("de-select all available options in Name filter and select one item");
        nameFilterItem.expandDropDownMenu();
        nameFilterItem.uncheckSelectAll();
        nameFilterItem.checkMenuOption("TradeOptions|service");
        nameFilterItem.confirmMenu();

        int cardCount2 = ui.getTrendCards().getListOfCards().size();
        Assert.assertEquals(cardCount2, 1);

        logger.info("disable the Name filter");
        nameFilterItem.disableFilterItem();
        Assert.assertEquals(ui.getTrendCards().getListOfCards().size(), cardCount1);

        logger.info("check the active filter count");
        filter.hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 1);
        filter.showPanel();
        Assert.assertEquals(filter.getAddedActiveFilterItemsCount(), 1);
        
        logger.info("enable the Name filter");
        nameFilterItem.enableFilterItem();
        Assert.assertEquals(ui.getTrendCards().getListOfCards().size(), 1);
        
        logger.info("check the active filter count");
        filter.hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 2);
        filter.showPanel();
        Assert.assertEquals(filter.getAddedFilterItemsCount(), 2);

        logger.info("do cleanup tasks");
        ui.cleanup();
    }
}
