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
package com.ca.apm.test.atc.permalink;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FiltersPermalinkTest extends UITest {
    
    private final static String FILTER_1_NAME = "Type";
    
    private final static String FILTER_2_NAME = "Application";
    private final static String FILTER_2_VALUE_1 = "OrderEngine";
    
    private UI ui;
    private LeftNavigationPanel nav;
    private Timeline timeline;
        
    private void init() throws Exception {
        logger.info("should log into Team Center");
        
        ui = getUI();
        nav = ui.getLeftNavigationPanel();
        timeline = ui.getTimeline();
        
        ui.login(Role.ADMIN);

        logger.info("should switch to Map view");
        nav.goToMapViewPage();

        logger.info("should turn off the Live mode");
        timeline.turnOffLiveMode();
    }
    
    private FilterMenu createTestFilter() throws Exception {
        logger.info("should create " + FILTER_1_NAME + " and " + FILTER_2_NAME + " filter");
        ui.getFilterBy().add(FILTER_1_NAME);
        
        FilterMenu filter = ui.getFilterBy().add(FILTER_2_NAME);
        filter.expandDropDownMenu();
        filter.uncheckSelectAll();
        filter.checkMenuOption(FILTER_2_VALUE_1);
        Assert.assertTrue(filter.getListOfSelectedItems().size() == 1);
        Assert.assertFalse(filter.getMenuSelectAll().isCheckboxChecked());
        Assert.assertTrue(filter.getMenuItem(FILTER_2_VALUE_1).isCheckboxChecked());
        filter.confirmMenu();
        return filter;
    }

    @Test
    public void closingBrowserTest() throws Exception {
        init();
        
        FilterMenu filter = createTestFilter();
        
        logger.info("should log out and login again ");
        ui.logout();
        ui.login(Role.ADMIN, View.MAPVIEW);
        
        logger.info("should check " + FILTER_2_NAME + " filter exists and that just the " + FILTER_2_VALUE_1 + " option is selected");
        nav.goToMapViewPage();        
        ui.getRibbon().expandFilterToolbar();
        filter.expandDropDownMenu();
        
        Assert.assertTrue(filter.getListOfSelectedItems().size() == 1);
        Assert.assertFalse(filter.getMenuSelectAll().isCheckboxChecked());
        Assert.assertTrue(filter.getMenuItem(FILTER_2_VALUE_1).isCheckboxChecked());
        
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(1).contains(FILTER_2_NAME));

        ui.cleanup();
    }
    
    @Test
    public void copyUrlTest() throws Exception {
        init();
        
        FilterMenu filter = createTestFilter();

        logger.info("should switch to Settings view");
        nav.goToPerspectives();
        //Assert.assertTrue(ui.getSettingsPage().isSelected());
        
        String url = ui.getCurrentUrl();
        
        logger.info("should log out and login again with previous url");
        ui.logout();
        ui.login(Role.ADMIN, url, null);

        ui.getCanvas().waitForUpdate();
        
        logger.info("should switch to Map view");
        nav.goToMapViewPage();
        ui.getRibbon().expandFilterToolbar();

        logger.info("should check " + FILTER_2_NAME + " filter exists and that just the " + FILTER_2_VALUE_1 + " option is selected");
        filter.expandDropDownMenu();

        Assert.assertTrue(filter.getListOfSelectedItems().size() == 1);
        Assert.assertFalse(filter.getMenuSelectAll().isCheckboxChecked());
        Assert.assertTrue(filter.getMenuItem(FILTER_2_VALUE_1).isCheckboxChecked());
        
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(1).contains(FILTER_2_NAME));

        ui.cleanup();
    }
    
    @Test
    public void html5Test() throws Exception {
        init();
        
        logger.info("App global state should changed");
        String state = ui.getSavedState(Role.ADMIN.getUser());
        createTestFilter();        
        Assert.assertFalse(ui.getSavedState(Role.ADMIN.getUser()).equals(state));
        
        ui.cleanup();
    }

    @Test
    public void logoutTest() throws Exception {
        init();
        
        FilterMenu filter = createTestFilter();
        
        logger.info("should switch to Dashboard");
        nav.goToDashboardPage();
        
        logger.info("should log out and login again");
        ui.logout();
        ui.login(Role.ADMIN, View.DASHBOARD);

        ui.getCanvas().waitForUpdate();
        
        logger.info("should check that the Dashboard is displayed");
        Assert.assertTrue(ui.getDashboardPage().isSelected());
        
        logger.info("should check " + FILTER_2_NAME + " filter exists and that just the " + FILTER_2_VALUE_1 + " option is selected");
        filter.expandDropDownMenu();
        Assert.assertTrue(filter.getListOfSelectedItems().size() == 1);
        Assert.assertFalse(filter.getMenuSelectAll().isCheckboxChecked());
        Assert.assertTrue(filter.getMenuItem(FILTER_2_VALUE_1).isCheckboxChecked());
        
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(1).contains(FILTER_2_NAME));
        
        ui.cleanup();
    }
}
