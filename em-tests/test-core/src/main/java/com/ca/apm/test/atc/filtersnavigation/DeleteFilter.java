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

public class DeleteFilter extends UITest {

    private final static Logger logger = Logger.getLogger(DeleteFilter.class);

    @Test
    public void testDeleteFilter() throws Exception {
        UI ui = getUI();

        logger.info("log into APM Server");
        ui.login();

        logger.info("switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();


        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("display the filter panel");
        ui.getFilterBy().add("Type");

        logger.info("select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("create Type as first filter");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 1);

        logger.info("check the active filter count");
        ui.getFilterBy().hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 1);
        ui.getFilterBy().showPanel();
        
        logger.info("verify the filter item 'type' is present");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);
        String firstFilterName = typeFilterItem.getName();
        Assert.assertTrue(firstFilterName.toLowerCase().contains("type"), "First filter item is '"
            + firstFilterName + "' instead of the expected 'type'");

        logger.info("add filter Application");
        ui.getFilterBy().add("Application");
        FilterMenu wsOperationFilterItem = ui.getFilterBy().add("wsOperation");
        FilterMenu wsNamespaceFilterItem = ui.getFilterBy().add("wsNamespace");

        logger.info("check the active filter count");
        ui.getFilterBy().hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 4);
        ui.getFilterBy().showPanel();
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 4);
        
        logger.info("de-select all available options in Type filter and select SERVLET");
        int cardCount1 = ui.getTrendCards().getListOfCards().size();
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.uncheckSelectAll();
        typeFilterItem.checkMenuOption("SERVLET");
        typeFilterItem.confirmMenu();

        int cardCount2 = ui.getTrendCards().getListOfCards().size();
        Assert.assertTrue(cardCount2 >= 1);
        Assert.assertNotEquals(cardCount2, cardCount1);

        logger.info("delete the Type filter");
        ui.getFilterBy().remove(typeFilterItem);
        typeFilterItem = null;
        int cardCount3 = ui.getTrendCards().getListOfCards().size();
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(0).contains("Application"));
        Assert.assertNotEquals(cardCount2, cardCount3);

        logger.info("check the active filter count");
        ui.getFilterBy().hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 3);
        ui.getFilterBy().showPanel();
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 3);
        
        int historicCardViewFilterCount = ui.getFilterBy().getFilterItemNames().size();
        logger.info("check that there is one filter less: " + historicCardViewFilterCount); // 3
        logger.info("turn on the Live mode");
        ui.getTimeline().turnOnLiveMode();
        int liveCardViewCount = ui.getFilterBy().getFilterItemNames().size();
        logger.info("filters:" + ui.getFilterBy().getFilterItemNames());
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(0).contains("Application"));
        Assert.assertEquals(historicCardViewFilterCount, liveCardViewCount); // 3

        logger.info("delete the wsOperation filter");
        ui.getFilterBy().remove(wsOperationFilterItem);
        int liveCardViewCount2 = ui.getFilterBy().getFilterItemNames().size();
        
        logger.info("check the active filter count");
        ui.getFilterBy().hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 2);
        ui.getFilterBy().showPanel();
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 2);
        
        logger.info("check that there is one filter less: " + liveCardViewCount2); // 2
        Assert.assertNotEquals(liveCardViewCount2, liveCardViewCount); // 3 != 2

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        int liveMapViewCount = ui.getFilterBy().getFilterItemNames().size();
        Assert.assertEquals(liveCardViewCount2, liveMapViewCount); // 2
        
        logger.info("check the active filter count in bottom bar");
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 2);

        logger.info("delete the wsNamespace filter");
        ui.getFilterBy().remove(wsNamespaceFilterItem);
        int liveMapViewCount2 = ui.getFilterBy().getFilterItemNames().size();
        
        logger.info("check the active filter count");
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 1);

        ui.getFilterBy().hidePanel();
        ui.getFilterBy().showPanel();
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        
        logger.info("check that there is have one filter less: " + liveMapViewCount2); // 1
        Assert.assertNotEquals(liveMapViewCount2, liveMapViewCount); // 2 != 1

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        int historicMapViewCount = ui.getFilterBy().getFilterItemNames().size();
        Assert.assertEquals(liveMapViewCount2, historicMapViewCount); // 1

        logger.info("do cleanup tasks");
        ui.cleanup();
    }
}
