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

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.element.PageElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class CreateFilter extends UITest {

    private static final Logger logger = Logger.getLogger(CreateFilter.class);

    private UI ui;
    
    /**
     * Test creating of the filters in both Live Mode and Historic mode, in Map view and Trend view,
     * keeping these filters when switching between modes and views.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateFilter() throws Exception {
        ui = getUI();
        
        final FilterBy f = ui.getFilterBy();
        final BottomBar b = ui.getBottomBar();

        ui.login();

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        Assert.assertEquals(b.getActiveFilterCount(), 0, "Active filter count in the bottom bar does not match the expected value");
        
        logger.info("create Type as first filter");
        Assert.assertEquals(f.getFilterItemCount(), 0);
        f.add("Type");
        Assert.assertTrue(f.getFilterItemNames().get(0).contains("Type"));

        Assert.assertEquals(b.getActiveFilterCount(), 1, "Active filter count in the bottom bar does not match the expected value");
        
        logger.info("create filter wsOperation (Live mode/Map view)");
        f.add("wsOperation");
        Assert.assertTrue(f.getFilterItemNames().get(1).contains("wsOperation"));

        Assert.assertEquals(b.getActiveFilterCount(), 2, "Active filter count in the bottom bar does not match the expected value");
        
        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("create filter Application (Historic mode/Map view)");
        FilterMenu application = f.add("Application");

        Assert.assertEquals(b.getActiveFilterCount(), 3, "Active filter count in the bottom bar does not match the expected value");
        
        logger.info("see the Application filter added");
        // there are both filters from live mode and historic mode in the view
        Assert.assertTrue(f.getFilterItemNames().get(2).contains("Application"));
        application.expandDropDownMenu();

        // all are active
        Assert.assertEquals(application.getListOfMenuItems().size(), application
            .getListOfSelectedItems().size());
        application.cancelMenu();

        logger.info("switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();
        Assert.assertEquals(b.getActiveFilterCount(), 3, "Active filter count in the bottom bar does not match the expected value");

        logger.info("create filter wsNamespace (Historic mode/Trend view)");
        f.add("wsNamespace");
        Assert.assertTrue(f.getFilterItemNames().get(3).contains("wsNamespace"));
        
        Assert.assertEquals(f.getFilterItemNames().size(), 4);
        Assert.assertEquals(b.getActiveFilterCount(), 4, "Active filter count in the bottom bar does not match the expected value");

        logger.info("switch to Live mode again");
        ui.getTimeline().turnOnLiveMode();

        logger.info("create filter item wsOperation in a new disjunctive clause (Live mode/Trend view)");
        f.add("wsOperation", 0, false, true);
        Assert.assertEquals(f.getFilterItemNames().size(), 5);
        Assert.assertEquals(b.getActiveFilterCount(), 5, "Active filter count in the bottom bar does not match the expected value");

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        // consistency check - no filter got lost
        Assert.assertEquals(f.getFilterItemNames().size(), 5);

        logger.info("add a filter item within a BT Coverage");
        f.add("Name", 1, true, false);
        Assert.assertEquals(f.getFilterItemNames().size(), 6);
        Assert.assertEquals(b.getActiveFilterCount(), 6, "Active filter count in the bottom bar does not match the expected value");
        
        logger.info("add another filter item into the previously created BT Coverage");
        f.addToBtGroup("Experience", 1);
        Assert.assertEquals(f.getFilterItemNames().size(), 7);
        Assert.assertEquals(b.getActiveFilterCount(), 7, "Active filter count in the bottom bar does not match the expected value");
        
        logger.info("check the active filter count in the collapsed Filter By toolbar");
        f.hidePanel();
        Assert.assertEquals(ui.getBottomBar().getActiveFilterCount(), 7);
        f.showPanel();
        Assert.assertEquals(ui.getFilterBy().getAddedFilterItemsCount(), 7);
        
        logger.info("check no extra filter clause was added");
        Assert.assertEquals(f.getFilterClauses().size(), 2, "Number of filter clauses does not match");
        Assert.assertEquals(f.getCountOfFilterItemsInAnyBTCoverage(), 2, "Number of items in a BT Coverage does not match");
        
        logger.info("verify the filter structure in clause 1");
        List<FilterMenu> items1 = f.getFilterItemObjects(0);
        
        Assert.assertEquals(items1.size(), 4, "Number of filter items in the first clause does not match");
        Assert.assertEquals(items1.get(0).getName(), "Type");
        Assert.assertEquals(items1.get(0).getBtGroupId(), FilterMenu.NOT_IN_BT_COVERAGE);
        Assert.assertEquals(items1.get(1).getName(), "wsOperation");
        Assert.assertEquals(items1.get(1).getBtGroupId(), FilterMenu.NOT_IN_BT_COVERAGE);
        Assert.assertEquals(items1.get(2).getName(), "Application");
        Assert.assertEquals(items1.get(2).getBtGroupId(), FilterMenu.NOT_IN_BT_COVERAGE);
        Assert.assertEquals(items1.get(3).getName(), "wsNamespace");
        Assert.assertEquals(items1.get(3).getBtGroupId(), FilterMenu.NOT_IN_BT_COVERAGE);
        
        logger.info("verify the filter structure in clause 2");
        List<FilterMenu> items2 = f.getFilterItemObjects(1);
        
        Assert.assertEquals(items2.size(), 3, "Number of filter items in the second clause does not match");
        Assert.assertEquals(items2.get(0).getName(), "wsOperation");
        Assert.assertEquals(items2.get(0).getBtGroupId(), FilterMenu.NOT_IN_BT_COVERAGE);
        Assert.assertEquals(items2.get(1).getName(), "Name");
        Assert.assertEquals(items2.get(1).getBtGroupId(), 1);
        Assert.assertEquals(items2.get(2).getName(), "Experience");
        Assert.assertEquals(items2.get(2).getBtGroupId(), 1);

        logger.info("do cleanup tasks");
        ui.cleanup();
    }
           
    private static final String NEW_ATTRIBUTE_NAME = "e2e_cr_filter_test_a1";
    private static final String NEW_ATTRIBUTE_VALUE = "123456789";
    private static final String GROUP_NAME = "SERVLET";
    private static final String USED_PERSPECTIVE = "Type";
   
    /**
     * This test case covers DE130297.
     * 
     * A newly added attribute to some node can be added as a new filter in the filter toolbar.
     * It means it is available in the list that opens on pressing the [+] button in the toolbar. 
     * 
     * @throws Exception
     */
    @Test 
    public void testAddedAttributeAvailableInFilterByMenu() throws Exception {
        ui = getUI();
        
        logger.info("log in");
        ui.login();

        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("Set default universe");
        ui.getTopNavigationPanel().selectUniverse(Universe.DEFAULT_UNIVERSE);
                
        logger.info("select the desired perspective");
        if (!ui.getPerspectivesControl().isPerspectiveActive(USED_PERSPECTIVE)) {
            ui.getPerspectivesControl().selectPerspectiveByName(USED_PERSPECTIVE);
        }
        
        Assert.assertTrue(ui.getPerspectivesControl().isPerspectiveActive(USED_PERSPECTIVE));
        
        logger.info("add new attribute");
        ui.getCanvas().addCustomAttributeToNode(GROUP_NAME, NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE);
        
        try {
            ui.getFilterBy().add("Type");
            ui.getFilterBy().expandFilterByMenu(0);
            
            boolean found = false;
            final List<PageElement> entries = ui.getFilterBy().getElementsInFilterBy();
                
            StringBuilder s = new StringBuilder("List of the attributes that can be filtered by: ");
            boolean first = true;
            String text;
            for (final WebElement entry : entries) {
                if (!first) {
                    s.append(", ");
                } else {
                    first = false;
                }
                
                text = entry.getText();
                s.append(text);
                if (NEW_ATTRIBUTE_NAME.equals(text)) {
                    found = true;
                }
            }
    
            logger.info(s);
            
            Assert.assertTrue(found, "The added attribute '" + NEW_ATTRIBUTE_NAME + "' is not among those listed above");
        }
        finally {
            String[] nodeNames = new String[1];
            nodeNames[0] = GROUP_NAME;
            ui.getCanvas().deleteCustomAttributeFromNodesIfItExists(nodeNames, NEW_ATTRIBUTE_NAME);
            
            ui.cleanup();
        }
    }
}
