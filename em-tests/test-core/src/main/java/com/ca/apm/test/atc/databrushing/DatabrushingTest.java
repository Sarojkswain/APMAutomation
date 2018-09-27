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
package com.ca.apm.test.atc.databrushing;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.AttributeRulesTable;
import com.ca.apm.test.atc.common.AttributeRulesTable.Operator;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.Ribbon.CollapsibleToolbar;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.AttributeRulesTableRow;
import com.ca.apm.test.atc.common.element.PageElement;
import org.junit.Ignore;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class DatabrushingTest extends UITest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final String NOT_SET = "Not set";  
    
    private UI ui;
        
    private void init() throws Exception {
        ui = getUI();

        logger.info("log into Team Center");
        ui.login(Role.ADMIN);
        
        logger.info("switch to Map view");
        ui.getLeftNavigationPanel().goToMapViewPage();
        
        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();
        
        logger.info("select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");
    }
    
    @Test(groups = "failing")
    public void testCreateCondition() throws Exception {
        init();

        logger.info("check the highlighting information");
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), false);
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 0);
        ui.getRibbon().expandHighlightToolbar();
        
        logger.info("create 'Name' data brushing filter");
        FilterMenu nameBrusher = ui.getDataBrushing().add("Name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 'name' data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();
        
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN"));
        ui.getCanvas().getCtrl().fitAllToView();
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("check the highlighting information");
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), true);
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 1);
        ui.getRibbon().expandHighlightToolbar();
        
        // TODO re-verify the requirement that any used highlight condition is disabled in the list of conditions
        /*
        logger.info("see existing filters inactive in filter menu");
        Assert.assertTrue(ui.getRibbon().isElementDisabled(ui.getDataBrushing().getElementInShowMe("name")));
        */

        ui.cleanup();
    }

    @Test(groups = "failing")
    public void testDisableCondition() throws Exception {
        init();
        
        logger.info("check the highlighting information");
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), false);
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 0);
        ui.getRibbon().expandHighlightToolbar();
        
        logger.info("create 'Name' data brushing filter");
        FilterMenu nameBrusher = ui.getDataBrushing().add("Name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("check the highlighting information - not active yet");
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), false); 
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 0);
        ui.getRibbon().expandHighlightToolbar();
        
        logger.info("select one item in the 'name' data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();
        
        logger.info("check the highlighting information - already active");
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), true); 
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 1);
        ui.getRibbon().expandHighlightToolbar();
        
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN"));
        ui.getCanvas().getCtrl().fitAllToView();
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("disable the 'name' data brushing filter");
        nameBrusher.disableFilterItem();
        Assert.assertFalse(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("check the highlighting information");
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), false);
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 0);
        ui.getRibbon().expandHighlightToolbar();
        
        logger.info("re-enable the 'Name' data brushing filter");
        nameBrusher.enableFilterItem();
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("check the highlighting information");
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), true);
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 1);
        ui.getRibbon().expandHighlightToolbar();
        
        logger.info("delete the 'Name' data brushing filter");
        ui.getDataBrushing().remove(nameBrusher);
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 0);
        Assert.assertFalse(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("check the highlighting information");
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), false);
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 0);
        ui.getRibbon().expandHighlightToolbar();
        
        ui.cleanup();
    }

    @Test(groups = "failing")
    public void testGroups() throws Exception {
        init();
        
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("create 'Name' data brushing filter");
        ui.getRibbon().expandHighlightToolbar();
        FilterMenu nameBrusher = ui.getDataBrushing().add("Name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 'name' data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();

        logger.info("see the whole group highlighted");
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN")));

        logger.info("expand the group and see the item highlighted");
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN"));
        ui.getCanvas().getCtrl().fitAllToView();
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Options Trading")));

        ui.cleanup();
    }

    /**
     * Tests map changes based on filtering and highlighting. 
     * 
     * @throws Exception
     */
    @Test(groups = "failing")
    public void testMapChanges() throws Exception {
        init();

        ui.getPerspectivesControl().selectPerspectiveByName("Type");
        
        ui.getFilterBy().add("Type");
        ui.getFilterBy().checkShowEntryElement();
        ui.getRibbon().expandHighlightToolbar();
        
        logger.info("should see Type as first filter");
        Assert.assertTrue(ui.getFilterBy().getFilterItemNames().get(0).contains("Type"));
        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);

        logger.info("select one item in the Type filter");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.uncheckSelectAll();
        typeFilterItem.checkMenuOption("SERVLET");

        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), 1);
        Assert.assertFalse(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        Assert.assertTrue(typeFilterItem.getMenuItem("SERVLET").isCheckboxChecked());

        typeFilterItem.confirmMenu();

        logger.info("create 'Type' data brushing filter");
        FilterMenu typeBrusher = ui.getDataBrushing().add("Type");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 'Type' data brushing filter");
        typeBrusher.expandDropDownMenu();
        typeBrusher.checkMenuOption("SERVLET");
        Assert.assertEquals(typeBrusher.getListOfSelectedItems().size(), 1);
        typeBrusher.confirmMenu();

        logger.info("see nodes highlighted");
        WebElement bTranNode = ui.getCanvas().getNodeByNameSubstring("SERVLE");
        Assert.assertTrue(ui.getCanvas().isHighlighted(bTranNode));
        ui.getCanvas().expandGroup(bTranNode);
        Assert.assertTrue(typeBrusher.getMenuContainer().getText().contains("Type (1)"));
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("TradeOptions|service")));

        logger.info("create 'servletClassname' data brushing filter");
        FilterMenu servletClassnameBrusher = ui.getDataBrushing().add("servletClassname");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);

        logger.info("select one item in the 'servletClassname' data brushing filter");
        servletClassnameBrusher.expandDropDownMenu();
        servletClassnameBrusher.uncheckSelectAll();
        servletClassnameBrusher.checkMenuOption("TradeOptions");
        Assert.assertEquals(servletClassnameBrusher.getListOfSelectedItems().size(), 1);
        servletClassnameBrusher.confirmMenu();
        Assert.assertTrue(servletClassnameBrusher.getMenuContainer().getText().contains("servletClassname (1)"));
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("TradeOptions|service")));

        logger.info("select additional item in the Type filter");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.checkMenuOption("DATABASE");
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), 2);
        Assert.assertFalse(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        Assert.assertTrue(typeFilterItem.getMenuItem("SERVLET").isCheckboxChecked());
        Assert.assertTrue(typeFilterItem.getMenuItem("DATABASE").isCheckboxChecked());
        typeFilterItem.confirmMenu();

        logger.info("un-select previously selected item in the Type filter");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.uncheckMenuOption("SERVLET");
        Assert.assertTrue(typeFilterItem.getMenuItem("DATABASE").isCheckboxChecked());
        Assert.assertFalse(typeFilterItem.getMenuItem("SERVLET").isCheckboxChecked());
        Assert.assertFalse(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), 1);
        typeFilterItem.confirmMenu();
        
        try {
            ui.getCanvas().getNodeByName("TradeOptions|service");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NoSuchElementException);
        }

        logger.info("check selection in data brushing menus");
        
        typeBrusher.expandDropDownMenu();
        Assert.assertEquals(typeBrusher.getListOfSelectedItems().size(), 0);
        typeBrusher.cancelMenu();
        
        servletClassnameBrusher.expandDropDownMenu();
        Assert.assertEquals(servletClassnameBrusher.getListOfSelectedItems().size(), 0);
        servletClassnameBrusher.cancelMenu();

        ui.cleanup();
    }
    
    /*
    public void testDependencyOnFiltering() throws Exception {
        init();

        logger.info("create Name filter");
        FilterMenu nameFilterItem = ui.getFilterBy().add("Name");
        ui.getCanvas().getCtrl().fitAllToView();
        Assert.assertTrue(ui.getFilterBy().getList().get(1).getText().contains("Name"));

        logger.info("select one item in the Name filter");
        nameFilterItem.expandDropDownMenu();
        nameFilterItem.uncheckSelectAll();
        nameFilterItem.checkMenuOption("Options Trading");

        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), 1);
        Assert.assertFalse(ui.getRibbon().isCheckboxChecked(nameFilterItem.getMenuSelectAll()));
        Assert.assertTrue(ui.getRibbon().isCheckboxChecked(nameFilterItem.getMenuItem("Options Trading")));

        nameFilterItem.confirmMenu();

        logger.info("create 'Name' data brushing filter");
        FilterMenu nameBrusher = ui.getDataBrushing().add("name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 'Name' data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();

        logger.info("see nodes highlighted");
        WebElement bTranNode = ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN");
        Assert.assertTrue(ui.getCanvas().isHighlighted(bTranNode));
        ui.getCanvas().expandGroup(bTranNode);
        Assert.assertTrue(nameBrusher.getMenuContainer().getText().contains("name (1)"));
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("create 'type' data brushing filter");
        FilterMenu typeBrusher = ui.getDataBrushing().add("type");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);

        logger.info("select one item in the 'Type' data brushing filter");
        typeBrusher.expandDropDownMenu();
        typeBrusher.checkMenuOption("BUSINESSTRANSACTION");
        Assert.assertTrue(typeBrusher.getListOfSelectedItems().size() > 0);
        typeBrusher.confirmMenu();
        Assert.assertTrue(typeBrusher.getMenuContainer().getText().contains("type (1)"));
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("select additional item in the Name filter");
        nameFilterItem.expandDropDownMenu();
        nameFilterItem.checkMenuOption("Login");
        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), 2);
        Assert.assertFalse(ui.getRibbon().isCheckboxChecked(nameFilterItem.getMenuSelectAll()));
        Assert.assertTrue(ui.getRibbon().isCheckboxChecked(nameFilterItem.getMenuItem("Login")));
        nameFilterItem.confirmMenu();

        logger.info("un-select previously selected item in the Name filter");
        nameFilterItem.expandDropDownMenu();
        nameFilterItem.uncheckMenuOption("Options Trading");
        Assert.assertTrue(ui.getRibbon().isCheckboxChecked(nameFilterItem.getMenuItem("Login")));
        Assert.assertFalse(ui.getRibbon().isCheckboxChecked(nameFilterItem.getMenuSelectAll()));
        Assert.assertEquals(nameFilterItem.getListOfSelectedItems().size(), 1);
        nameFilterItem.confirmMenu();
        try {
            ui.getCanvas().getNodeByName("Options Trading");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NoSuchElementException);
        }

        logger.info("check selection in data brushing menus");
        nameBrusher.expandDropDownMenu();
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 0);
        nameBrusher.cancelMenu();
        typeBrusher.expandDropDownMenu();
        Assert.assertTrue(ui.getRibbon().isCheckboxChecked(typeBrusher.getMenuSelectAll()));
        typeBrusher.cancelMenu();

        ui.cleanup();
    }
    */

    @Test(groups = "failing")
    public void testMultipleConditions() throws Exception {
        init();

        logger.info("create 'Name' data brushing filter");
        ui.getRibbon().expandHighlightToolbar();
        FilterMenu nameBrusher = ui.getDataBrushing().add("Name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 'name' data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();
        
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN"));
        ui.getCanvas().getCtrl().fitAllToView();
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("select second value for the 'name' data brushing filter");
        
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Login");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 2);
        nameBrusher.confirmMenu();
        Assert.assertTrue(nameBrusher.getMenuContainer().getText().contains("Name (2)"));
        
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Options Trading")));
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Login")));

        logger.info("create 'Type' data brushing filter");
        
        FilterMenu typeBrusher = ui.getDataBrushing().add("Type");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);

        logger.info("select one item in the 'Type' data brushing filter");
        
        typeBrusher.expandDropDownMenu();
        typeBrusher.checkMenuOption("BUSINESSTRANSACTION");
        
        if (Arrays.asList(typeBrusher.getArrayOfMenuItemsNames()).contains(NOT_SET)) {
            Assert.assertEquals(typeBrusher.getListOfSelectedItems().size(), 2);
        } else {
            Assert.assertEquals(typeBrusher.getListOfSelectedItems().size(), 1);
        }
        
        typeBrusher.confirmMenu();
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Options Trading")));
        Assert.assertTrue(ui.getCanvas().isHighlighted(
            ui.getCanvas().getNodeByName("Login")));
        
        typeBrusher.expandDropDownMenu();
        if (Arrays.asList(typeBrusher.getArrayOfMenuItemsNames()).contains(NOT_SET)) {
            Assert.assertTrue(typeBrusher.getMenuContainer().getText().contains("Type (2)"));
        } else {
            Assert.assertTrue(typeBrusher.getMenuContainer().getText().contains("Type (1)"));
        }
        
        Assert.assertTrue(nameBrusher.getMenuContainer().getText().contains("Name (2)"));

        ui.cleanup();
    }

    // @Test
    @Ignore
    public void testSwitchingViews() throws Exception {
        init();
        
        logger.info("create 'name' data brushing filter");
        FilterMenu nameBrusher = ui.getDataBrushing().add("name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 'name' data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();
        
        ui.getCanvas().expandGroup(ui.getCanvas().getNodeByNameSubstring("BUSINESSTRAN"));
        ui.getCanvas().getCtrl().fitAllToView();
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));

        logger.info("create 'type' data brushing filter");
        FilterMenu typeBrusher = ui.getDataBrushing().add("type");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);

        logger.info("select one item in the 'type' data brushing filter");
        typeBrusher.expandDropDownMenu();
        typeBrusher.checkMenuOption("BUSINESSTRANSACTION");
        Assert.assertEquals(typeBrusher.getListOfSelectedItems().size(), 1);
        typeBrusher.confirmMenu();
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));
        Assert.assertTrue(typeBrusher.getMenuContainer().getText().contains("type (1)"));
        Assert.assertTrue(nameBrusher.getMenuContainer().getText().contains("name (1)"));

        logger.info("switch to dashboard view and back");
        ui.getLeftNavigationPanel().goToDashboardPage();
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getRibbon().expandHighlightToolbar();
        
        Utils.sleep(1500);
        Assert.assertTrue(ui.getCanvas().isHighlighted(ui.getCanvas().getNodeByName("Options Trading")));
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);
        Assert.assertTrue(typeBrusher.getMenuContainer().getText().contains("type (1)"));
        Assert.assertTrue(nameBrusher.getMenuContainer().getText().contains("name (1)"));

        ui.cleanup();
    }

    private void goToDecorationPolicies() throws Exception {
        ui.getLeftNavigationPanel().goToDecorationPolicies();
    }

    @Test(groups = "failing")
    public void testScrollbar() throws Exception {
        ui = getUI();
        ui.login();

        logger.info("delete all existing attribute rules");
        goToDecorationPolicies();
        AttributeRulesTable attrRules = ui.getAttributeRulesTable();
        attrRules.deleteAll();
        Assert.assertEquals(attrRules.getRows().size(), 1);

        logger.info("initialy scrollbar should NOT be present");
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getRibbon().expandHighlightToolbar();
        ui.getDataBrushing().expandShowMeMenu();
        PageElement ul = ui.getDataBrushing().getShowMeMenuOptions();
        
        List<PageElement> databrushItems = ui.getDataBrushing().getShowMeItems();
        if (databrushItems.size() > 0 && ul.hasVerticalScrollbar()) {
            ui.cleanup();
            return;
        }
        
        Assert.assertFalse(ul.hasVerticalScrollbar());

        logger.info("create many new attribute rules");
        goToDecorationPolicies();
        final int RULES_COUNT = 20;
        List<AttributeRulesTableRow> rows;
        AttributeRulesTableRow lastRow;
        for (int i = 1; i <= RULES_COUNT; i++) {
            if (i == 1) {
                attrRules.createRow("r" + i, "a", "type", Operator.STARTS_WITH, "S");
            } else {
                rows = attrRules.getRowsByParams("r" + (i - 1), "a", "type", Operator.STARTS_WITH, "S");
                Assert.assertFalse(rows.isEmpty());
                lastRow = rows.get(rows.size() - 1);
                lastRow.duplicate();
                lastRow.editNewAttributeNameCell("r" + i);
            }
        }
        
        Assert.assertEquals(attrRules.getRows().size(), RULES_COUNT + 1);
        Utils.sleep(2000); // wait for update all attribute rules otherwise test fails

        logger.info("scrollbar should be present for long menu");
        ui.getLeftNavigationPanel().goToMapViewPage();
        ui.getRibbon().expandHighlightToolbar();
        ui.getDataBrushing().expandShowMeMenu();
        ul = ui.getDataBrushing().getShowMeMenuOptions();
        Assert.assertTrue(ul.hasVerticalScrollbar());

        logger.info("add and remove highlighting");

        ui.getDataBrushing().add("Type");
        ui.getDataBrushing().add("r" + (RULES_COUNT - 2));
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);
        ui.getDataBrushing().removeAll();
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 0);

        logger.info("remove all created attribute rules");
        goToDecorationPolicies();
        attrRules.deleteAll();
        Assert.assertEquals(attrRules.getRows().size(), 1);

        ui.cleanup();
    }
    
    // Test DE131267 - Highlighting does not work properly - it is "X AND nothing => X" while it should be "X AND nothing => nothing"
    @Test(groups = "failing")
    public void testHighlightingConjunction() throws Exception {
        init();

        logger.info("check the highlighting information");
        Assert.assertEquals(ui.getBottomBar().isHighlightingActive(), false);
        ui.getRibbon().collapseHighlightToolbar();
        Assert.assertEquals(ui.getRibbon().getCounterValueFromCollapsedToolbar(CollapsibleToolbar.HIGHLIGHTING), 0);
        ui.getRibbon().expandHighlightToolbar();    
        
        ui.getCanvas().getCtrl().fitAllToView();
        ui.getCanvas().getListOfHighlightedNodes();
        
        logger.info("create 1st data brushing filter");
        FilterMenu nameBrusher = ui.getDataBrushing().add("Name");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 1);

        logger.info("select one item in the 1st data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkMenuOption("Options Trading");
        Assert.assertEquals(nameBrusher.getListOfSelectedItems().size(), 1);
        nameBrusher.confirmMenu();
        
        int highlightedNodesCount1 = ui.getCanvas().getListOfHighlightedNodes().size();
        Assert.assertTrue(highlightedNodesCount1 > 0);
        
        logger.info("create 2nd data brushing filter");
        FilterMenu typeBrusher = ui.getDataBrushing().add("Type");
        Assert.assertEquals(ui.getDataBrushing().getFilters().size(), 2);
        // Just after adding another highlighting item, all of its options are selected so that 
        //  the current highlighting in the map does not change
        
        int highlightedNodesCount2 = ui.getCanvas().getListOfHighlightedNodes().size();
        Assert.assertTrue(highlightedNodesCount2 == highlightedNodesCount1);
        
        logger.info("clear the selection of the 2nd data brushing filter");
        typeBrusher.expandDropDownMenu();
        typeBrusher.uncheckSelectAll();
        typeBrusher.confirmMenu();
        
        // Something && Nothing -> Nothing
        int highlightedNodesCount3 = ui.getCanvas().getListOfHighlightedNodes().size();
        Assert.assertEquals(highlightedNodesCount3, 0);
        
        logger.info("select all items in the 1st data brushing filter");
        nameBrusher.expandDropDownMenu();
        nameBrusher.checkSelectAll();
        nameBrusher.confirmMenu();

        // Everything && Nothing -> Nothing
        int highlightedNodesCount4 = ui.getCanvas().getListOfHighlightedNodes().size();
        Assert.assertEquals(highlightedNodesCount4, 0);
        
        ui.cleanup();
    }
}
