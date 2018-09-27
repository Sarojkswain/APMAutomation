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
package com.ca.apm.test.atc.universe;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.LeftNavigationPanel.SettingsItems;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.UI.Group;
import com.ca.apm.test.atc.common.UI.Permission;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.PageElement;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UniverseTest extends UITest {

    private static final String DUMMY_USER = "dummyUser";

    private UI ui;
    private UniverseSettings universeSettings;
    private LeftNavigationPanel leftNav;
    private TopNavigationPanel topNav;


    private void initialize() throws Exception {
        ui = getUI();
        
        logger.info("go to universes tab");
        
        ui.login();
        
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();
        
        leftNav.goToUniverses();
        
        universeSettings = ui.getUniverseSettings();
    }

    @Test(groups = "failing")
    public void testUniverseCRUD() throws Exception {
        final String testCRUD = "Test CRUD";
        final String testCRUDrename = "test CRUD rename";
        final String testCRUDsaveAs = "test CRUD save as";
        
        initialize();

        if (universeSettings.isUniversePresent(testCRUD)) {
            universeSettings.deleteUniverse(testCRUD);
        }
        if (universeSettings.isUniversePresent(testCRUDrename)) {
            universeSettings.deleteUniverse(testCRUDrename);
        }
        if (universeSettings.isUniversePresent(testCRUDsaveAs)) {
            universeSettings.deleteUniverse(testCRUDsaveAs);
        }
        
        logger.info("it should create new universe");
        universeSettings.getCreateUniverseButton().click();
        URL url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        
        ui.getCanvas().waitForUpdate();
        ui.getFilterBy().add("Type", 0, false, false);
        ui.getFilterBy().add("Name", 0, true, false);
        ui.getFilterBy().add("Application", 0, false, true);
        ui.getFilterBy().add("servletClassname", 0, true, true);
        ui.getFilterBy().addToBtGroup("wsNamespace", 2);
        ui.getFilterBy().getSaveUniverseButton().click();
        universeSettings.createUniverseName(testCRUD);
        universeSettings.getUniverseRow(testCRUD);
        Assert.assertEquals(topNav.getActiveUniverse(), testCRUD);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());

        logger.info("verify universe is created correctly");
        WebElement expandedRow = universeSettings.expandRow(testCRUD);
        universeSettings.expandAllFilters(testCRUD);
        Assert.assertTrue(expandedRow.isDisplayed());
        Assert.assertTrue(universeSettings.isShowEntry(testCRUD));
        List<PageElement> filters = universeSettings.getFilterContainers(testCRUD);
        Assert.assertEquals(filters.size(), 5);
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 0), "type (All)");
        Assert.assertFalse(universeSettings.isFilterBtCoverage(testCRUD, 0));
        Assert.assertEquals(universeSettings.getFilterOperators(testCRUD).get(0), "AND");
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 1), "name (All)");
        Assert.assertTrue(universeSettings.isFilterBtCoverage(testCRUD, 1));
        Assert.assertEquals(universeSettings.getFilterOperators(testCRUD).get(1), "OR");
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 2), "applicationName (0)");
        Assert.assertFalse(universeSettings.isFilterBtCoverage(testCRUD, 2));
        Assert.assertEquals(universeSettings.getFilterOperators(testCRUD).get(2), "OR");
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 3), "servletClassname (0)");
        Assert.assertTrue(universeSettings.isFilterBtCoverage(testCRUD, 3));
        Assert.assertEquals(universeSettings.getFilterOperators(testCRUD).get(3), "AND");
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 4), "wsNamespace (All)");
        Assert.assertTrue(universeSettings.isFilterBtCoverage(testCRUD, 4));
        
        logger.info("it is NOT possible to create universe with the same name");
        universeSettings.getCreateUniverseButton().click();
        ui.getFilterBy().getSaveUniverseButton().click();
        String exceptionMessage = "";
        try {
            universeSettings.createUniverseName(testCRUD);
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
        }
        
        Assert.assertEquals(exceptionMessage, "Name already exists. Use a different name.");
        ui.getFilterBy().cancelUniverseEditing();

        logger.info("it should edit the universe");
        logger.info("{}", universeSettings.editUniverse(testCRUD));
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        ui.getFilterBy().uncheckShowEntryElement();
        FilterMenu typeFilter = ui.getFilterBy().getFilterItem(0);
        typeFilter.expandDropDownMenu();
        int allTypesCount = typeFilter.getArrayOfMenuItemsNames().length;
        typeFilter.uncheckMenuOption("SERVLET");
        typeFilter.uncheckMenuOption("DATABASE");
        typeFilter.confirmMenu();
        ui.getFilterBy().remove(ui.getFilterBy().getFilterItem(1));
        ui.getFilterBy().removeFilterClause(2);
        ui.getFilterBy().add("servletClassname", 1, false, false);
        ui.getFilterBy().getSaveUniverseButton().click();
        
        logger.info("verify universe is edited correctly");
        Assert.assertTrue(universeSettings.getUniverseRow(testCRUD).isDisplayed());
        expandedRow = universeSettings.expandRow(testCRUD);
        universeSettings.expandAllFilters(testCRUD);
        Assert.assertFalse(universeSettings.isShowEntry(testCRUD));
        filters = universeSettings.getFilterContainers(testCRUD);
        Assert.assertEquals(filters.size(), 3);
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 0), "type (" + (allTypesCount - 2) + ")");
        Assert.assertFalse(universeSettings.isFilterBtCoverage(testCRUD, 0));
        List<String> typeValues = universeSettings.getFilterValues(testCRUD, 0);
        Assert.assertTrue(typeValues.contains("SOCKET") || typeValues.contains("INFERRED_SOCKET"));
        Assert.assertTrue(typeValues.contains("WEBSERVICE") || typeValues.contains("WEBSERVICE_SERVER"));
        Assert.assertTrue(typeValues.contains("BUSINESSTRANSACTION") || typeValues.contains("GENERICFRONTEND"));
        Assert.assertEquals(universeSettings.getFilterOperators(testCRUD).get(0), "OR");
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 1), "applicationName (0)");
        Assert.assertFalse(universeSettings.isFilterBtCoverage(testCRUD, 1));
        Assert.assertEquals(universeSettings.getFilterOperators(testCRUD).get(1), "AND");
        Assert.assertEquals(universeSettings.getFilterName(testCRUD, 2), "servletClassname (All)");
        Assert.assertFalse(universeSettings.isFilterBtCoverage(testCRUD, 2));
        
        logger.info("it is NOT possible to delete cluster universe");
        Assert.assertFalse(universeSettings.canDeleteUniverse(universeSettings.getClusterUniverseName()));
        
        logger.info("create copy of cluster universe using 'save as'");
        universeSettings.editUniverse(universeSettings.getClusterUniverseName());
        Assert.assertFalse(ui.getFilterBy().getSaveUniverseButton().isEnabled());
        ui.getFilterBy().getSaveAsUniverseButton().click();
        universeSettings.createUniverseName(testCRUDsaveAs);
        Assert.assertTrue(universeSettings.getUniverseRow(testCRUDsaveAs).isDisplayed());
        Assert.assertEquals(topNav.getActiveUniverse(), testCRUDsaveAs);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());
        
        logger.info("it should rename the universe");
        topNav.selectUniverse(testCRUD);
        ui.waitForWorkIndicator();
        universeSettings.renameUniverse(testCRUD, testCRUDrename);
        Utils.sleep(1000);
        Assert.assertEquals(topNav.getActiveUniverse(), testCRUDrename);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());
        
        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testCRUDrename);
        universeSettings.deleteUniverse(testCRUDsaveAs);
        ui.logout();
    }
    
    @Test
    public void testAddUnknownUsers() throws Exception {
        final String testAddUsers = "Test add unknown users";
        final String testAddUsers2 = "Test add users 2";
        final String noRealmUser1 = "noRealmUser1";
        final String commaSeparatedUser = "no,realm,user";

        initialize();
        if (universeSettings.isUniversePresent(testAddUsers)) {
            universeSettings.deleteUniverse(testAddUsers);
        }
        if (universeSettings.isUniversePresent(testAddUsers2)) {
            universeSettings.deleteUniverse(testAddUsers2);
        }
        universeSettings.createUniverse(testAddUsers);

        logger.info("it should add new user");
        universeSettings.openUsersDialog(testAddUsers);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 0);
        Assert.assertFalse(universeSettings.getAddUserButton().isEnabled());
        universeSettings.getAddUserInput().sendKeys(noRealmUser1);
        universeSettings.getAddUserButton().click();
        universeSettings.getAddAsUserButton().click();
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 1);
        Map<String, String> assignedUsers = universeSettings.getUsersWithPermissions();
        Assert.assertTrue(assignedUsers.containsKey(noRealmUser1.toLowerCase()));
        Assert.assertEquals(assignedUsers.get(noRealmUser1.toLowerCase()), UI.Permission.read.name());
        universeSettings.saveUsersDialog();

        logger.info("new user is saved");
        leftNav.goToPerspectives();
        leftNav.goToUniverses();
        Assert.assertEquals(universeSettings.getUsersCell(testAddUsers).getText(), noRealmUser1.toLowerCase());
        logger.info("it should not be possible to add multiple users at once");
        universeSettings.openUsersDialog(testAddUsers);
        universeSettings.getAddUserInput().sendKeys(commaSeparatedUser);
        universeSettings.getAddUserButton().click();
        universeSettings.getAddAsUserButton().click();
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 2);
        universeSettings.saveUsersDialog();
        Assert.assertTrue(universeSettings.getUsersCell(testAddUsers).getText().contains(noRealmUser1.toLowerCase()));
        Assert.assertTrue(universeSettings.getUsersCell(testAddUsers).getText().contains(commaSeparatedUser.toLowerCase()));

        logger.info("it cannot add unknown user using intellisense");
        universeSettings.createUniverse(testAddUsers2);
        universeSettings.openUsersDialog(testAddUsers2);

        universeSettings.getAddUserInput().click();
        universeSettings.getAddUserInput().sendKeys(noRealmUser1);
        universeSettings.getAddUserButton().click();
        universeSettings.getAddAsUserButton().click();
        universeSettings.saveUsersDialog();
        Assert.assertEquals(universeSettings.getUsersCell(testAddUsers2).getText(), noRealmUser1.toLowerCase());

        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testAddUsers);
        universeSettings.deleteUniverse(testAddUsers2);
        ui.logout();
    }

    @Test
    public void testUserAutoSuggest() throws Exception {
        final String testUserAutoSuggest = "Test users auto suggest";
        final List<String> expectedSuggestions = Arrays.asList("editor (Local Users and Groups)", "editors (Local Users and Groups)");

        initialize();
        if (universeSettings.isUniversePresent(testUserAutoSuggest)) {
            universeSettings.deleteUniverse(testUserAutoSuggest);
        }
        universeSettings.createUniverse(testUserAutoSuggest);
        ui.logout();

        // Only users logged in at least once are shown in auto suggest
        ui.login(Role.EDITOR);
        leftNav.goToDecorationPolicies(); // some activity
        ui.logout();

        ui.login();
        leftNav.goToUniverses();

        logger.info("it should add new user");
        universeSettings.openUsersDialog(testUserAutoSuggest);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 0);
        Assert.assertFalse(universeSettings.getAddUserButton().isEnabled());
        universeSettings.getAddUserInput().sendKeys("e");
        Utils.sleep(200);
        universeSettings.getAddUserInput().sendKeys("d");

        logger.info("intellisense should auto-suggest users");
        int options = universeSettings.getTypeAheadOptions().size();
        Assert.assertTrue(options > 0, "There should be some auto-suggested users. Visible options: " + options);
        List<String> values = universeSettings.getTypeAheadValues(); 
        Assert.assertTrue(values.containsAll(expectedSuggestions), "Auto suggested users: " + values + ". They should contain all of: " + expectedSuggestions);
        universeSettings.closeUsersDialog();
        
        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testUserAutoSuggest);
        ui.logout();
    }

    @Test
    public void testUserValidate() throws Exception {
        final String testUserValidate = "Test user validate";
        initialize();
        if (universeSettings.isUniversePresent(testUserValidate)) {
            universeSettings.deleteUniverse(testUserValidate);
        }
        universeSettings.createUniverse(testUserValidate);
        logger.info("it should add new user");
        universeSettings.openUsersDialog(testUserValidate);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 0);
        Assert.assertFalse(universeSettings.getAddUserButton().isEnabled());
        // Test with never login user so we verify that users are validated with realms and not cache
        universeSettings.getAddUserInput().sendKeys(Role.NEVERLOGIN.getUser());
        universeSettings.getValidateUserButton().click();
        Utils.sleep(2000);
        boolean isException = false;
        WebElement userValidatedIcon = null;
        try{
            userValidatedIcon = universeSettings.getUserValidatedIcon();
        } catch (NoSuchElementException nse) {
            isException = true;
        }
        Assert.assertFalse(isException, "User should have been validated");
        Assert.assertNotNull(userValidatedIcon);
        universeSettings.getAddUserButton().click();
        Utils.sleep(1000);
        // Now test with a dummy user to make sure that the type is not inherited for a new record
        universeSettings.getAddUserInput().sendKeys(DUMMY_USER);
        universeSettings.getAddUserButton().click();
        universeSettings.getAddAsUserButton().click();
        universeSettings.closeUsersDialog();
        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testUserValidate);
        ui.logout();
    }

    @Test
    public void testUserNotFound() throws Exception {
        final String testUserNotFound = "Test user not found";

        initialize();
        if (universeSettings.isUniversePresent(testUserNotFound)) {
            universeSettings.deleteUniverse(testUserNotFound);
        }
        universeSettings.createUniverse(testUserNotFound);

        logger.info("it should add new user");
        universeSettings.openUsersDialog(testUserNotFound);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 0);
        Assert.assertFalse(universeSettings.getAddUserButton().isEnabled());
        universeSettings.getAddUserInput().sendKeys("invaliduser");
        universeSettings.getValidateUserButton().click();
        boolean isException = false;
        WebElement userValidatedIcon = null;
        try{
            // wait for user-not-found icon to show up
            Utils.sleep(500);
            userValidatedIcon = universeSettings.getUserNotFoundIcon();
        } catch (NoSuchElementException nse) {
            isException = true;
        }
        Assert.assertFalse(isException, "User not found icon should be visible");
        Assert.assertNotNull(userValidatedIcon);

        universeSettings.closeUsersDialog();

        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testUserNotFound);
        ui.logout();
    }

    @Test
    public void testUsersRemove() throws Exception {
        final String testUsersRemove = "Test Remove";

        initialize();
        if (universeSettings.isUniversePresent(testUsersRemove)) {
            universeSettings.deleteUniverse(testUsersRemove);
        }
        universeSettings.createUniverse(testUsersRemove);
        universeSettings.addUser(testUsersRemove, "tester404", Permission.read);
        universeSettings.addUser(testUsersRemove, "tester500", Permission.read);
        universeSettings.addUser(testUsersRemove, "tester403", Permission.read);

        logger.info("it should remove single user");
        universeSettings.openUsersDialog(testUsersRemove);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 3);
        universeSettings.removeUser("tester500");
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 2);
        universeSettings.saveUsersDialog();

        leftNav.goToPerspectives();
        leftNav.goToUniverses();
        String addedUsers = universeSettings.getUsersCell(testUsersRemove).getText();
        Assert.assertTrue(addedUsers.contains("tester403"));
        Assert.assertTrue(addedUsers.contains("tester404"));

        logger.info("it should cancel editing without change");
        universeSettings.openUsersDialog(testUsersRemove);
        universeSettings.removeUser("tester404");
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 1);
        ui.getModalDialog().clickButton(DialogButton.CLOSE);
        addedUsers = universeSettings.getUsersCell(testUsersRemove).getText();
        Assert.assertTrue(addedUsers.contains("tester403"));
        Assert.assertTrue(addedUsers.contains("tester404"));
        leftNav.goToPerspectives();
        leftNav.goToUniverses();
        addedUsers = universeSettings.getUsersCell(testUsersRemove).getText();
        Assert.assertTrue(addedUsers.contains("tester403"));
        Assert.assertTrue(addedUsers.contains("tester404"));

        logger.info("it should remove all users");
        universeSettings.openUsersDialog(testUsersRemove);
        universeSettings.getRemoveAllUsersButton().click();
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 0);
        universeSettings.saveUsersDialog();
        Assert.assertEquals(universeSettings.getUsersCell(testUsersRemove).getText(), "");

        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testUsersRemove);
        ui.logout();
    }

    @Test
    public void testBulkEditingNotPossible() throws Exception {
        final String testBulk = "Test Bulk 1";
        final String testBulk2 = "Test Bulk 2";

        initialize();
        if (universeSettings.isUniversePresent(testBulk)) {
            universeSettings.deleteUniverse(testBulk);
        }
        if (universeSettings.isUniversePresent(testBulk2)) {
            universeSettings.deleteUniverse(testBulk2);
        }
        universeSettings.createUniverse(testBulk);
        universeSettings.createUniverse(testBulk2);

        logger.info("it should not find Users button after two Universes are selected");
        // universeSettings.selectRow(testBulk);
        // universeSettings.selectRow(testBulk2);
        WebElement usersButton = universeSettings.getEditUsersButton();
        Assert.assertFalse(usersButton.isDisplayed(), "It is no longer possible to bulk add users to multiple universes");

        universeSettings.addUser(testBulk,"tester", Permission.read);
        universeSettings.addUser(testBulk,"testovic", Permission.read);
        universeSettings.openUsersDialog(testBulk);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 2);
        universeSettings.saveUsersDialog();
        universeSettings.openUsersDialog(testBulk2);
        Map<String, String> assignedUsers = universeSettings.getUsersWithPermissions();
        Assert.assertEquals(assignedUsers.size(), 0, "No users should be assigned to the selected but not clicked universe");
        universeSettings.closeUsersDialog();
        universeSettings.openUsersDialog(testBulk);
        assignedUsers = universeSettings.getUsersWithPermissions();
        Assert.assertEquals(assignedUsers.size(), 2, "Two users should be assigned to the selected and clicked universe");
        universeSettings.closeUsersDialog();

        // add testovic to the second universe as well
        universeSettings.addUser(testBulk2, "testovic", Permission.read);
        universeSettings.openUsersDialog(testBulk);
        ui.getModalDialog().waitForModalDialogFadeIn();
        universeSettings.removeUser("testovic");
        universeSettings.saveUsersDialog();

        logger.info("it should remove user from one universe and keep in the other");

        universeSettings.openUsersDialog(testBulk2);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 1); // testovic
        universeSettings.closeUsersDialog();
        universeSettings.openUsersDialog(testBulk);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 1); // tester
        universeSettings.closeUsersDialog();

        logger.info("it should delete the universes");
        universeSettings.deleteUniverse(testBulk);
        universeSettings.deleteUniverse(testBulk2);
        ui.logout();
    }

    @Test
    public void testUsersFilter() throws Exception {
        final String testFilter = "Test Users Filter";
        final String testFilter2 = "Test Users Filter 2";

        initialize();
        if (universeSettings.isUniversePresent(testFilter)) {
            universeSettings.deleteUniverse(testFilter);
        }
        if (universeSettings.isUniversePresent(testFilter2)) {
            universeSettings.deleteUniverse(testFilter2);
        }
        FilterMenu filter =
                new FilterMenu("Users and groups", new SimpleFilter(ui), ui);

        universeSettings.createUniverse(testFilter);
        universeSettings.createUniverse(testFilter2);
        universeSettings.addUser(testFilter, "user1", Permission.read);
        universeSettings.addUser(testFilter, "user2", Permission.read);
        universeSettings.addUser(testFilter2, "user2", Permission.read);
        universeSettings.addUser(testFilter2, "user3", Permission.read);

        filter.expandDropDownMenu();
        int filterCount = filter.getListOfMenuItems().size();

        logger.info("it should hide universes");
        filter.uncheckMenuOption("user1");
        filter.uncheckMenuOption("user2");
        int initialCount = universeSettings.getAllRows().size();
        Assert.assertFalse(filter.getMenuItem("user1").isCheckboxChecked());
        Assert.assertFalse(filter.getMenuItem("user2").isCheckboxChecked());
        filter.confirmMenu();
        Assert.assertTrue(universeSettings.getAllRows().size() < initialCount);
        Assert.assertTrue(universeSettings.getUniverseRow(testFilter2).isDisplayed());
        for (PageElement el : universeSettings.getAllRows()) {
            String users = universeSettings.getUsersCell(el).getText();
            Assert.assertFalse(users.contains("user1"));
        }

        logger.info("should filter visible options");
        filter.expandDropDownMenu();
        filter.getMenuFilter().sendKeys("er1");
        filter.checkMenuOption("user1");
        Assert.assertEquals(filter.getListOfSelectedItems().size(), 1);
        filter.confirmMenu();
        Assert.assertTrue(universeSettings.getAllRows().size() < initialCount);
        Assert.assertTrue(universeSettings.getUniverseRow(testFilter).isDisplayed());
        for (PageElement el : universeSettings.getAllRows()) {
            String users = universeSettings.getUsersCell(el).getText();
            Assert.assertFalse(users.contains("user3"));
        }

        logger.info("should click on \"Select All\"");
        Assert.assertTrue(universeSettings.getAllRows().size() < initialCount);
        filter.expandDropDownMenu();
        filter.checkSelectAll();
        Assert.assertEquals(filter.getListOfSelectedItems().size(), filterCount);
        filter.confirmMenu();
        Assert.assertEquals(universeSettings.getAllRows().size(), initialCount);

        logger.info("should clear the search string");
        filter.expandDropDownMenu();
        filter.getMenuFilter().sendKeys("er1");
        Assert.assertEquals(filter.getListOfSelectedItems().size(), 1);
        filter.getMenuClearFilter().click();
        Assert.assertEquals(filter.getListOfSelectedItems().size(), filterCount);
        Assert.assertEquals(filter.getListOfMenuItems().size(), filterCount);
        Assert.assertTrue(filter.getMenuSelectAll().isCheckboxChecked());
        filter.confirmMenu();
        Assert.assertEquals(universeSettings.getAllRows().size(), initialCount);

        logger.info("should display no results");
        filter.expandDropDownMenu();
        filter.getMenuFilter().sendKeys("fake");
        Assert.assertEquals(filter.getListOfMenuItems().size(), 0);
        Assert.assertTrue(filter.getMenuOK().isEnabled());
        filter.cancelMenu();

        logger.info("should add to selection");
        filter.expandDropDownMenu();
        filter.uncheckSelectAll();
        Assert.assertEquals(filter.getListOfSelectedItems().size(), 0);
        filter.getMenuFilter().sendKeys("er2");
        filter.checkSelectAll();
        filter.checkAddToSelection();
        filter.addToSelection();
        Assert.assertEquals(filter.getListOfMenuItems().size(), filterCount);
        Assert.assertEquals(filter.getListOfSelectedItems().size(), 1);
        Assert.assertEquals(filter.getMenuFilter().getText(), "");
        filter.getMenuFilter().sendKeys("er1");
        filter.getMenuItem("user1").click();
        filter.checkAddToSelection();
        filter.addToSelection();
        Assert.assertEquals(filter.getListOfSelectedItems().size(), 2);
        filter.confirmMenu();
        Assert.assertTrue(universeSettings.getUniverseRow(testFilter).isDisplayed());
        Assert.assertTrue(universeSettings.getUniverseRow(testFilter2).isDisplayed());

        logger.info("it should show universe without users");
        universeSettings.removeAllUsers(testFilter2);
        filter.expandDropDownMenu();
        filter.uncheckSelectAll();
        filter.checkMenuOption("Not Set");
        Assert.assertEquals(filter.getListOfSelectedItems().size(), 1);
        filter.confirmMenu();
        Assert.assertTrue(universeSettings.getUniverseRow(testFilter2).isDisplayed());
        for (PageElement el : universeSettings.getAllRows()) {
            String users = universeSettings.getUsersCell(el).getText();
            Assert.assertEquals(users, "");
        }

        logger.info("it should delete the universes");
        filter.expandDropDownMenu();
        filter.checkSelectAll();
        filter.confirmMenu();
        universeSettings.deleteUniverse(testFilter);
        universeSettings.deleteUniverse(testFilter2);
        ui.logout();
    }

    @Test
    public void testCancelButton() throws Exception {
        final String testCancelButton = "Test Cancel button";

        initialize();
        if (universeSettings.isUniversePresent(testCancelButton)) {
            universeSettings.deleteUniverse(testCancelButton);
        }
        universeSettings.createUniverse(testCancelButton);
        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        
        ui.getCanvas().waitForUpdate();
        
        logger.info("editing the universe");
        universeSettings.editUniverse(testCancelButton);
        URL url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        Assert.assertEquals(topNav.getActiveUniverse(), testCancelButton);
        Assert.assertFalse(topNav.getUniverseDropdown().isEnabled());
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);
        Assert.assertTrue(ui.getFilterBy().isSaveUniverseButtonVisible());
        Assert.assertTrue(ui.getFilterBy().isSaveAsUniverseButtonVisible());
        Assert.assertTrue(ui.getFilterBy().isCancelUniverseEditingVisible());

        logger.info("clicking the Cancel button");
        ui.getFilterBy().cancelUniverseEditing();
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/universes"));
        Assert.assertEquals(topNav.getActiveUniverse(), Universe.DEFAULT_UNIVERSE);
        
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());
        
        logger.info("switching back to map");
        leftNav.goToMapViewPage();
        Assert.assertEquals(topNav.getActiveUniverse(), Universe.DEFAULT_UNIVERSE);
        
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        Assert.assertFalse(ui.getFilterBy().isSaveUniverseButtonVisible());
        Assert.assertFalse(ui.getFilterBy().isSaveAsUniverseButtonVisible());
        Assert.assertFalse(ui.getFilterBy().isCancelUniverseEditingVisible());
        
        logger.info("creating new universe from the settings tab");
        leftNav.goToUniverses();
        topNav.selectUniverse(testCancelButton);
        ui.waitForWorkIndicator();
        universeSettings.getCreateUniverseButton().click();
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        Assert.assertEquals(topNav.getActiveUniverse(), Universe.DEFAULT_UNIVERSE);
        
        Assert.assertFalse(topNav.getUniverseDropdown().isEnabled());
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        Assert.assertTrue(ui.getFilterBy().isSaveUniverseButtonVisible());
        Assert.assertFalse(ui.getFilterBy().isSaveAsUniverseButtonVisible());
        Assert.assertTrue(ui.getFilterBy().isCancelUniverseEditingVisible());
        
        logger.info("clicking the Cancel button");
        ui.getFilterBy().cancelUniverseEditing();
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/universes"));
        Assert.assertEquals(topNav.getActiveUniverse(), testCancelButton);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());

        logger.info("switching back to map");
        leftNav.goToMapViewPage();
        Assert.assertEquals(topNav.getActiveUniverse(), testCancelButton);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        Assert.assertFalse(ui.getFilterBy().isSaveUniverseButtonVisible());
        Assert.assertFalse(ui.getFilterBy().isSaveAsUniverseButtonVisible());
        Assert.assertFalse(ui.getFilterBy().isCancelUniverseEditingVisible());

        logger.info("cleaning up");
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testCancelButton);
        ui.logout();
    }

    @Test
    public void testDeleteUniverse() throws Exception {
        // Only admin can delete a universe
        final String testForAdmin = "Test Delete Universe Admin";
        initialize();
        if (universeSettings.isUniversePresent(testForAdmin)) {
            universeSettings.deleteUniverse(testForAdmin);
        }
        logger.info("it should create new universes");
        universeSettings.createUniverse(testForAdmin);
        logger.info("grant user rights for universe");
        universeSettings.addGroup(testForAdmin, Group.managers.name(), UI.Permission.edit);
        PageElement universeRowForAdmin = universeSettings.getUniverseRow(testForAdmin);
        Assert.assertTrue(universeSettings.isDeleteLinkPresent(universeRowForAdmin));
        universeSettings.deleteUniverse(testForAdmin);
        List<String> universeNamesAfterDeleteAdmin = topNav.getUniverseNames();
        Assert.assertFalse(universeNamesAfterDeleteAdmin.contains(testForAdmin));
        ui.logout();
    }

    private void testEscapeWasPrevented(boolean editingExistingUniverse, boolean editingClusterUniverse) throws Exception {
        URL url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        ModalDialog modalDialog = ui.getModalDialog();
        modalDialog.waitForModalDialogFadeIn();
        Assert.assertEquals(modalDialog.getModalDialog().findElement(By.cssSelector(".modal-title")).getText(), "This Universe Has Not Been Saved");
        Assert.assertEquals(!editingClusterUniverse, modalDialog.getButton(DialogButton.SAVE_UNIVERSE).isDisplayed());
        Assert.assertEquals(editingExistingUniverse, modalDialog.getButton(DialogButton.SAVE_AS_UNIVERSE).isDisplayed());
        Assert.assertTrue(modalDialog.getButton(DialogButton.CONTINUE_EDITING).isDisplayed());
        modalDialog.clickButton(DialogButton.CONTINUE_EDITING);
        Assert.assertFalse(modalDialog.isModalDialogPresent());
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
    }

    private void testEditModeEscaping(boolean editingExistingUniverse, boolean editingClusterUniverse) throws Exception {
        ui.getLeftNavigationPanel().clickMenuItem(ui.getLeftNavigationPanel().getDashboardLink());
        testEscapeWasPrevented(editingExistingUniverse, editingClusterUniverse);

        ui.getLeftNavigationPanel().clickMenuItem(ui.getLeftNavigationPanel().getHomepage());
        testEscapeWasPrevented(editingExistingUniverse, editingClusterUniverse);

        ui.getLeftNavigationPanel().clickMenuItem(ui.getElementProxy(SettingsItems.ATTRIBUTE_RULES.getLinkSelector()));
        testEscapeWasPrevented(editingExistingUniverse, editingClusterUniverse);
    }

    @Test
    public void testUniverseEditingCantBeEscaped() throws Exception {
        // Only admin can delete a universe
        final String testingUniverse = "Universe for testing the edit mode";
        final String testingUniverse2 = testingUniverse + " 2";
        initialize();
        if (universeSettings.isUniversePresent(testingUniverse)) {
            universeSettings.deleteUniverse(testingUniverse);
        }
        if (universeSettings.isUniversePresent(testingUniverse2)) {
            universeSettings.deleteUniverse(testingUniverse2);
        }
        logger.info("test of creating a new universe");
        universeSettings.getCreateUniverseButton().click();
        URL url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        
        ui.getCanvas().waitForUpdate();
        ui.getFilterBy().add("Type");
        testEditModeEscaping(false, false);

        ui.getLeftNavigationPanel().clickMenuItem(ui.getLeftNavigationPanel().getDashboardLink());
        ModalDialog modalDialog = ui.getModalDialog();
        modalDialog.waitForModalDialogFadeIn();
        modalDialog.getButton(DialogButton.SAVE_UNIVERSE).click();
        universeSettings.createUniverseName(testingUniverse);
        Assert.assertFalse(modalDialog.isModalDialogPresent());
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/dashboard"));
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverse);
        ui.getLeftNavigationPanel().goToUniverses();

        
        logger.info("test of editing the existing universe");
        universeSettings.editUniverse(testingUniverse);
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        testEditModeEscaping(true, false);

        FilterMenu typeFilter = ui.getFilterBy().getFilterItem(0);
        typeFilter.expandDropDownMenu();
        int allTypesCount = typeFilter.getArrayOfMenuItemsNames().length;
        typeFilter.uncheckMenuOption("SERVLET");
        typeFilter.confirmMenu();

        ui.getLeftNavigationPanel().clickMenuItem(ui.getLeftNavigationPanel().getHomepage());
        modalDialog.waitForModalDialogFadeIn();
        modalDialog.clickButton(DialogButton.SAVE_UNIVERSE);
        Assert.assertFalse(modalDialog.isModalDialogPresent());
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/home"));
        Assert.assertEquals(topNav.getActiveUniverse(), Universe.ALL_MY_UNIVERSES);

        logger.info("test of editing the existing universe and saving is as new one");
        ui.getLeftNavigationPanel().goToUniverses();
        universeSettings.editUniverse(testingUniverse);
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));

        ui.getFilterBy().uncheckShowEntryElement();
        typeFilter = ui.getFilterBy().getFilterItem(0);
        typeFilter.expandDropDownMenu();
        typeFilter.checkMenuOption("SERVLET");
        typeFilter.confirmMenu();

        ui.getLeftNavigationPanel().clickMenuItem(ui.getElementProxy(SettingsItems.ATTRIBUTE_RULES.getLinkSelector()));
        modalDialog.waitForModalDialogFadeIn();
        modalDialog.getButton(DialogButton.SAVE_AS_UNIVERSE).click();
        universeSettings.createUniverseName(testingUniverse2);
        Assert.assertFalse(modalDialog.isModalDialogPresent());
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/attribute-rules"));
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverse2);

        logger.info("checking the created universes and deleting them");
        ui.getLeftNavigationPanel().goToUniverses();
        WebElement expandedRow = universeSettings.expandRow(testingUniverse);
        universeSettings.expandAllFilters(testingUniverse);
        Assert.assertTrue(expandedRow.isDisplayed());
        Assert.assertTrue(universeSettings.isShowEntry(testingUniverse));
        List<PageElement> filters = universeSettings.getFilterContainers(testingUniverse);
        Assert.assertEquals(filters.size(), 1);
        Assert.assertEquals(universeSettings.getFilterName(testingUniverse, 0), "type (" + (allTypesCount - 1) + ")");
        universeSettings.deleteUniverse(testingUniverse);
        
        expandedRow = universeSettings.expandRow(testingUniverse2);
        universeSettings.expandAllFilters(testingUniverse2);
        Assert.assertTrue(expandedRow.isDisplayed());
        Assert.assertFalse(universeSettings.isShowEntry(testingUniverse2));
        filters = universeSettings.getFilterContainers(testingUniverse2);
        Assert.assertEquals(filters.size(), 1);
        Assert.assertEquals(universeSettings.getFilterName(testingUniverse2, 0), "type (All)");
        universeSettings.deleteUniverse(testingUniverse2);
        Assert.assertNotEquals(topNav.getActiveUniverse(), testingUniverse2);

        logger.info("creating a copy of cluster universe using 'save as'");
        universeSettings.editUniverse(universeSettings.getClusterUniverseName());
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/map"));
        testEditModeEscaping(true, true);

        ui.getLeftNavigationPanel().clickMenuItem(ui.getElementProxy(SettingsItems.PERSPECTIVES.getLinkSelector()));
        modalDialog.waitForModalDialogFadeIn();
        modalDialog.getButton(DialogButton.SAVE_AS_UNIVERSE).click();
        universeSettings.createUniverseName(testingUniverse);
        url = new URL(ui.getCurrentUrl());
        Assert.assertTrue(url.getRef().startsWith("/perspectives"));
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverse);

        ui.getLeftNavigationPanel().goToUniverses();
        universeSettings.deleteUniverse(testingUniverse);

        ui.logout();
    }
}
