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
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.FilterMenu;
import com.ca.apm.test.atc.common.UI;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class FilterFilter extends UITest {

    private static final Logger logger = Logger.getLogger(FilterFilter.class);

    private static final String SUBSTR_1 = "DAT";
    private static final String SUBSTR_2 = "INFE";
    private static final String SUBSTR_3 = "SERV";
    private static final String SUBSTR_ABSENT = "fake";
    
    private static final String SUBSTR_2_MATCHING_ITEM = "INFERRED_SOCKET"; // SUBSTR_2 should match this while SUBSTR_3 should NOT!

    @Test
    public void testFilterFilter() throws Exception {
        final UI ui = getUI();

        logger.info("Log into APM Server");
        ui.login();

        logger.info("Switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();

        logger.info("Turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("Select the 'Type' perspective");
        ui.getPerspectivesControl().selectPerspectiveByName("Type");

        logger.info("Create Type as first filter");
        Assert.assertFalse(ui.getFilterBy().isPanelOpen());
        ui.getFilterBy().add("Type");

        logger.info("Uncheck the 'Show Entry points' option");
        ui.getFilterBy().uncheckShowEntryElement();

        logger.info("Verify the filter item 'type' is present");
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);
        String firstFilterName = typeFilterItem.getName();
        Assert.assertTrue(firstFilterName.toLowerCase().contains("type"), "First filter item is '"
            + firstFilterName + "' instead of the expected 'type'");

        typeFilterItem.expandDropDownMenu();
        String[] menuItemsNames = typeFilterItem.getArrayOfMenuItemsNames(); 
        int typeMenuCount = menuItemsNames.length;
        int substr1Count = getMatchingItemsCount(menuItemsNames, SUBSTR_1);
        int substr2Count = getMatchingItemsCount(menuItemsNames, SUBSTR_2);
        int substr3Count = getMatchingItemsCount(menuItemsNames, SUBSTR_3);
        int substr2And3Count = getMatchingItemsCount(menuItemsNames, SUBSTR_2, SUBSTR_3);

        checkThereAreNoMatchingItems(menuItemsNames, SUBSTR_ABSENT);
        Assert.assertTrue(substr1Count > 0);
        Assert.assertTrue(substr1Count < typeMenuCount);
        Assert.assertTrue(substr2Count > 0);
        Assert.assertTrue(substr2Count < typeMenuCount);
        Assert.assertTrue(substr3Count > 0);
        Assert.assertTrue(substr3Count < typeMenuCount);
        Assert.assertTrue(substr2And3Count < typeMenuCount);

        typeFilterItem.uncheckMenuOption(SUBSTR_2_MATCHING_ITEM);

        Assert.assertFalse(typeFilterItem.getMenuItem(SUBSTR_2_MATCHING_ITEM).isCheckboxChecked());
        typeFilterItem.confirmMenu();
        Assert.assertFalse(ui.getTrendCards().isCardByNamePresent(SUBSTR_2_MATCHING_ITEM));

        typeFilterItem.expandDropDownMenu();

        logger.info("Filter visible options");
        typeFilterItem.getMenuFilter().sendKeys(SUBSTR_1);
        checkItemNames(typeFilterItem.getArrayOfMenuItemsNames(), substr1Count, SUBSTR_1);
        typeFilterItem.confirmMenu();

        logger.info("Display DATABASE only");
        checkItemNames(ui.getTrendCards().getListOfAllCardNames(), substr1Count, SUBSTR_1);

        logger.info("Click on 'Select All'");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.checkSelectAll();
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), typeMenuCount);
        typeFilterItem.confirmMenu();
        Assert.assertTrue(ui.getTrendCards().getListOfCards().size() >= typeMenuCount);

        logger.info("Clear the search string");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.getMenuFilter().sendKeys(SUBSTR_2);
        checkItemNames(typeFilterItem.getArrayOfSelectedMenuItemsNames(), substr2Count, SUBSTR_2);

        typeFilterItem.getMenuClearFilter().click();
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), typeMenuCount);
        Assert.assertEquals(typeFilterItem.getListOfMenuItems().size(), typeMenuCount);
        Assert.assertTrue(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        typeFilterItem.confirmMenu();

        Assert.assertTrue(ui.getTrendCards().getListOfCards().size() >= typeMenuCount);

        logger.info("Display no results");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.getMenuFilter().sendKeys(SUBSTR_ABSENT);
        Assert.assertEquals(typeFilterItem.getListOfMenuItems().size(), 0);
        Assert.assertTrue(typeFilterItem.getMenuOK().isEnabled());
        typeFilterItem.cancelMenu();

        logger.info("Add to selection");
        typeFilterItem.expandDropDownMenu();
        typeFilterItem.uncheckSelectAll();
        Assert.assertEquals(typeFilterItem.getListOfSelectedItems().size(), 0);

        typeFilterItem.getMenuFilter().sendKeys(SUBSTR_3);
        typeFilterItem.checkSelectAll();
        typeFilterItem.checkAddToSelection();
        typeFilterItem.addToSelection();
        
        Assert.assertEquals(typeFilterItem.getListOfMenuItems().size(), typeMenuCount);
        checkItemNames(typeFilterItem.getArrayOfSelectedMenuItemsNames(), substr3Count, SUBSTR_3);

        Assert.assertEquals(typeFilterItem.getMenuFilter().getText(), "");

        typeFilterItem.getMenuFilter().sendKeys(SUBSTR_2);
        typeFilterItem.checkSelectAll();
        typeFilterItem.checkAddToSelection();
        typeFilterItem.addToSelection();
        checkItemNames(typeFilterItem.getArrayOfSelectedMenuItemsNames(), substr2Count + substr3Count, SUBSTR_2, SUBSTR_3);
        
        typeFilterItem.confirmMenu();
        Assert.assertTrue(ui.getTrendCards().getListOfCards().size() >= substr2Count + substr3Count - substr2And3Count);

        logger.info("Do cleanup tasks");
        ui.cleanup();
    }

    private boolean containsAll(String s, String[] substrs) {
        for (String substr : substrs) {
            if (!s.contains(substr)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAny(String s, String[] substrs) {
        for (String substr : substrs) {
            if (s.contains(substr)) {
                return true;
            }
        }
        return false;
    }

    private int getMatchingItemsCount(String[] menuItemsNames, String... substrs) {
        int cnt = 0;
        for (String itemName : menuItemsNames) {
            if (containsAll(itemName, substrs)) {
                cnt++;
            }
        }
        return cnt;
    }
    
    private void checkThereAreNoMatchingItems(String[] menuItemsNames, String substr) {
        for (String itemName : menuItemsNames) {
            Assert.assertFalse(itemName.contains(substr));
        }
    }
    
    private void checkItemNames(String[] menuItemsNames, int expectedCount, String... expectedSubtrsContained) {
        Assert.assertEquals(menuItemsNames.length, expectedCount, "Item counts should match.");
        for (String itemName : menuItemsNames) {
            Assert.assertTrue(containsAny(itemName, expectedSubtrsContained),
                "Item \"" + itemName + "\" does not contain any of the expected substrings: "
                    + Arrays.toString(expectedSubtrsContained));
        }
    }

    private void checkItemNames(List<String> menuItemsNames, int expectedCount, String... expectedSubtrsContained) {
        String[] array = new String[menuItemsNames.size()];
        menuItemsNames.toArray(array);
        checkItemNames(array, expectedCount, expectedSubtrsContained);
    }

    /**
     * Test that the Add as a new BT Coverage and Add as a new clause do not keep checked when
     * closed and re-opened.
     * 
     * @throws Exception
     */
    @Test
    public void testTheCheckBoxesDoNotKeepValue() throws Exception {
        final UI ui = getUI();
        final FilterBy filterBy = ui.getFilterBy();

        logger.info("should log into APM Server");
        ui.login();

        logger.info("should switch to Dashboard");
        ui.getLeftNavigationPanel().goToDashboardPage();
        
        logger.info("add the first filter item");
        filterBy.add("Type");

        logger.info("should check Add as new BT Coverage check box and close the menu");
        filterBy.expandFilterByMenu(0);
        filterBy.checkAddAsANewBTCoverage();
        filterBy.collapseFilterByMenu(0);

        logger
            .info("should re-open the menu and verify the state of the check-box did not persist");
        filterBy.expandFilterByMenu(0);
        Assert.assertFalse(filterBy.isCheckedAddAsANewBTCoverage());
        Assert.assertFalse(filterBy.isCheckedAddAsANewClause());

        logger.info("should check Add as new Clause check box and close the menu");
        filterBy.checkAddAsANewClause();
        filterBy.collapseFilterByMenu(0);

        logger
            .info("should re-open the menu and verify the state of the check-box did not persist");
        filterBy.expandFilterByMenu(0);
        Assert.assertFalse(filterBy.isCheckedAddAsANewBTCoverage());
        Assert.assertFalse(filterBy.isCheckedAddAsANewClause());

        logger.info("should do cleanup tasks");
        ui.cleanup();
    }
}
