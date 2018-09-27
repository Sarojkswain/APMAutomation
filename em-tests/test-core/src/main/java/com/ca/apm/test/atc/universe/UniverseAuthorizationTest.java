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
import com.ca.apm.test.atc.common.UI.Group;
import com.ca.apm.test.atc.common.UI.Permission;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.element.AttributeRow;
import com.ca.apm.test.atc.common.element.PageElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniverseAuthorizationTest extends UITest {

    private static final String GROUP_SERVLET = "SERVLET";
    private static final String NODE_TRADE_SERVICE = "TradeOptions|service";

    private static final String ATTRIBUTE_NAME_ACCESS = "accessTest";
    private static final String ATTRIBUTE_VALUE_ACCESS = "testAttrVal";
    private static final String TYPE_PERSPECTIVE = "Type";
    
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

        leftNav = ui.getLeftNavigationPanel();
        universeSettings = ui.getUniverseSettings();
    }

    @Test
    public void testNoAuthorization() throws Exception {
        final String testingUniverseName = "Test No Authorization";
        final String expectedNoDataMessage = "No data matching selected filters.";
        final String expectedInfoMessage = "Your administrator has not assigned a universe to you yet.\n"
            + "Refresh page and try it again or contact an administrator.";

        initialize();

        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }

        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);

        logger.info("testing that ENTERPRISE universe is available for admins");
        List<String> names = topNav.getUniverseNames();
        Assert.assertTrue(names.contains(Universe.DEFAULT_UNIVERSE));

        ui.logout();

        logger.info("logging in as guest");
        ui.login(Role.GHOST);
        leftNav.goToMapViewPage();

        logger.info("verify universes drop down is not visible");
        Assert.assertFalse(topNav.isUniverseDropdownPresent());

        logger.info("verify 'Not assigned universe' message is displayed");
        Assert.assertEquals(ui.getMapviewPage().getErrorDialogTitle(), "Info");
        Assert.assertEquals(ui.getMapviewPage().getErrorDialogText(), expectedInfoMessage);

        logger.info("EVERYTHING universe is not available for guests");
        leftNav.goToUniverses();
        boolean emptyTable = true;
        String noDataMessage = "";
        try {
            noDataMessage = universeSettings.getNoDataMessage();
        } catch (NoSuchElementException nse) {
            emptyTable = false;
        }

        Assert.assertTrue(emptyTable);
        Assert.assertEquals(noDataMessage, expectedNoDataMessage);

        ui.logout();

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
        ui.cleanup();
    }

    @Test
    public void testUserReadAuthorization() throws Exception {
        final String testingUniverseName = "Test Read Authorization For User";

        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }

        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addUser(testingUniverseName, Role.READER.name(), Permission.read);
        ui.logout();

        verifyReadOnlyPermissions(Role.READER, testingUniverseName);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();;
    }

    /**
     * Assign permissions to a group, verify with a user in the group
     * @throws Exception
     */
    @Test
    public void testGroupReadAuthorization() throws Exception {
        final String testingUniverseName = "Test Read Authorization For Group";

        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        
        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addGroup(testingUniverseName, Group.readers.name(), Permission.read);
        ui.logout();

        verifyReadOnlyPermissions(Role.READER, testingUniverseName);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
    }

    @Test
    public void testUserEditAuthorization() throws Exception {
        final String testingUniverseName = "Test Edit Authorization For User";

        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        
        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addUser(testingUniverseName, Role.EDITOR.name(), Permission.edit);
        ui.logout();

        verifyEditPermissions(Role.EDITOR, testingUniverseName);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();;
    }

    /**
     * Assign permissions to a group, verify with a user in the group
     * @throws Exception
     */
    @Test
    public void testGroupEditAuthorization() throws Exception {
        final String testingUniverseName = "Test Edit Authorization For Group";

        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        
        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addGroup(testingUniverseName, Group.editors.name(), Permission.edit);
        ui.logout();

        verifyEditPermissions(Role.EDITOR, testingUniverseName);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
    }

    @Test(groups = "failing")
    public void testUserManageAuthorization() throws Exception {
        final String testingUniverseName = "Test Manage Authorization For User";

        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        
        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addUser(testingUniverseName, Role.MANAGER.name(), Permission.manage);
        ui.logout();

        verifyManagePermissions(Role.MANAGER, testingUniverseName);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();;
    }

    /**
     * Assign permissions to a group, verify with a user in the group
     * @throws Exception
     */
    @Test(groups = "failing")
    public void testGroupManageAuthorization() throws Exception {
        final String testingUniverseName = "Test Manage Authorization For Group";

        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        
        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addGroup(testingUniverseName, Group.managers.name(), Permission.manage);
        ui.logout();

        verifyManagePermissions(Role.MANAGER, testingUniverseName);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
    }

    @Test(groups = "failing")
    public void testUniverseUserPermissionGrants() throws Exception {
        final String testingUniverseName = "Test Permission Grants For Users";
        
        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        logger.info("create the testing universe '" + testingUniverseName + "'");
        universeSettings.createUniverse(testingUniverseName);
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverseName);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());

        logger.info("grant user rights for universe");
        universeSettings.addUser(testingUniverseName, Role.READER.name(), UI.Permission.read);
        universeSettings.addUser(testingUniverseName, Role.EDITOR.name(), UI.Permission.edit);
        universeSettings.addUser(testingUniverseName, Role.MANAGER.name(), UI.Permission.manage);

        ui.logout();

        verifyReadOnlyPermissions(Role.READER, testingUniverseName);
        verifyEditPermissions(Role.EDITOR, testingUniverseName);
        verifyManagePermissions(Role.MANAGER, testingUniverseName, Role.READER, Role.EDITOR);

        logger.info("delete the testing universe '" + testingUniverseName + "'");
        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
    }

    @Test(groups = "failing")
    public void testUniverseGroupPermissionGrants() throws Exception {
        final String testingUniverseName = "Test Permission Grants For Groups";
        
        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        logger.info("it should create new universe");
        universeSettings.createUniverse(testingUniverseName);
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverseName);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());

        logger.info("grant user rights for universe");
        universeSettings.addGroup(testingUniverseName, Group.managers.name(), UI.Permission.manage);
        universeSettings.addGroup(testingUniverseName, Group.editors.name(), UI.Permission.edit);
        universeSettings.addGroup(testingUniverseName, Group.readers.name(), UI.Permission.read);

        ui.logout();

        verifyReadOnlyPermissions(Role.READER, testingUniverseName);
        verifyEditPermissions(Role.EDITOR, testingUniverseName);
        verifyManagePermissions(Role.MANAGER, testingUniverseName);

        ui.login();
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
    }

    @Test
    public void testAdminEditAttributes() throws Exception {
        initialize();
        ui.logout();

        verifyUserCanEditAttributes(Role.ADMIN, GROUP_SERVLET, ATTRIBUTE_NAME_ACCESS, ATTRIBUTE_VALUE_ACCESS);

        logger.info("clean up");
        ui.cleanup();
    }

    @Test
    public void testDuplicatedUniversePermissions() throws Exception {
        final String testingUniverseNameOriginal = "Test Duplicated Universe Original";
        final String testingUniverseNameClone = "Test Duplicated Universe Clone";

        // as admin create a universe
        initialize();

        if (universeSettings.isUniversePresent(testingUniverseNameOriginal)) {
            universeSettings.deleteUniverse(testingUniverseNameOriginal);
        }
        if (universeSettings.isUniversePresent(testingUniverseNameClone)) {
            universeSettings.deleteUniverse(testingUniverseNameClone);
        }
        logger.info("it should create new universe");
        universeSettings.createUniverse(testingUniverseNameOriginal);
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverseNameOriginal);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());

        logger.info("verify universe is configured correctly");
        WebElement expandedRow = universeSettings.expandRow(testingUniverseNameOriginal);
        universeSettings.expandAllFilters(testingUniverseNameOriginal);
        Assert.assertTrue(expandedRow.isDisplayed());
        Assert.assertTrue(universeSettings.isShowEntry(testingUniverseNameOriginal));
        List<PageElement> filters = universeSettings.getFilterContainers(testingUniverseNameOriginal);
        Assert.assertEquals(filters.size(), 1);

        logger.info("grant user rights for universe");
        universeSettings.addUser(testingUniverseNameOriginal, Role.MANAGER.name(), UI.Permission.manage);
        universeSettings.addUser(testingUniverseNameOriginal, Role.EDITOR.name(), UI.Permission.edit);
        universeSettings.addUser(testingUniverseNameOriginal, Role.READER.name(), UI.Permission.read);

        universeSettings.addGroup(testingUniverseNameOriginal, Group.managers.name(), UI.Permission.manage);
        universeSettings.addGroup(testingUniverseNameOriginal, Group.editors.name(), UI.Permission.edit);
        universeSettings.addGroup(testingUniverseNameOriginal, Group.readers.name(), UI.Permission.read);

        universeSettings.openUsersDialog(testingUniverseNameOriginal);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 6);
        universeSettings.closeUsersDialog();

        logger.info("clone universe");
        URL url = new URL(universeSettings.editUniverse(testingUniverseNameOriginal));
        Assert.assertTrue(url.getRef().startsWith("/map"));
        ui.getCanvas().waitForUpdate();
        ui.getFilterBy().add("Name", 0, true, false);
        ui.getFilterBy().getSaveAsUniverseButton().click();
        universeSettings.createUniverseName(testingUniverseNameClone);
        universeSettings.getUniverseRow(testingUniverseNameClone);
        Assert.assertEquals(topNav.getActiveUniverse(), testingUniverseNameClone);
        Assert.assertTrue(topNav.getUniverseDropdown().isEnabled());

        logger.info("verify universes are configured correctly");
        WebElement expandedRowOriginal = universeSettings.expandRow(testingUniverseNameOriginal);
        universeSettings.expandAllFilters(testingUniverseNameOriginal);
        Assert.assertTrue(expandedRowOriginal.isDisplayed());
        Assert.assertTrue(universeSettings.isShowEntry(testingUniverseNameOriginal));
        List<PageElement> filtersOriginal = universeSettings.getFilterContainers(testingUniverseNameOriginal);
        Assert.assertEquals(filtersOriginal.size(), 1);

        WebElement expandedRowClone = universeSettings.expandRow(testingUniverseNameClone);
        universeSettings.expandAllFilters(testingUniverseNameClone);
        Assert.assertTrue(expandedRowClone.isDisplayed());
        Assert.assertTrue(universeSettings.isShowEntry(testingUniverseNameClone));
        List<PageElement> filtersClone = universeSettings.getFilterContainers(testingUniverseNameClone);
        Assert.assertEquals(filtersClone.size(), 2);

        logger.info("check user rights for universe");
        universeSettings.openUsersDialog(testingUniverseNameOriginal);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 6);
        universeSettings.closeUsersDialog();

        universeSettings.openUsersDialog(testingUniverseNameClone);
        Assert.assertEquals(universeSettings.getUsersWithPermissions().size(), 0);
        universeSettings.closeUsersDialog();

        logger.info("it should delete the universes");
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseNameOriginal);
        universeSettings.deleteUniverse(testingUniverseNameClone);
        ui.logout();
    }

    @Test(groups = "failing")
    public void testToggleUserAccess() throws Exception {
        final String testingUniverseName = "Test toggle user access";
        initialize();
        
        if (universeSettings.isUniversePresent(testingUniverseName)) {
            universeSettings.deleteUniverse(testingUniverseName);
        }
        
        universeSettings.createUniverse(testingUniverseName);
        universeSettings.addUser(testingUniverseName, Role.TOGGLER.name(), Permission.read);
        
        ui.logout();

        verifyReadOnlyPermissions(Role.TOGGLER, testingUniverseName);

        ui.login();
        
        leftNav.goToUniverses();
        universeSettings.openUsersDialog(testingUniverseName);
        Map<String, String> usersWithPermissions = universeSettings.getUsersWithPermissions();
        Assert.assertEquals(usersWithPermissions.size(), 1);
        Assert.assertTrue(usersWithPermissions.containsKey(Role.TOGGLER.getUser()));
        Assert.assertEquals(usersWithPermissions.get(Role.TOGGLER.getUser()), Permission.read.name());
        
        // Clicking once on the permission should toggle permission to edit
        universeSettings.getUserAccessToggle(universeSettings.getDialogUserRows().get(0)).click();
        usersWithPermissions = universeSettings.getUsersWithPermissions();
        Assert.assertEquals(usersWithPermissions.size(),1);
        Assert.assertTrue(usersWithPermissions.containsKey(Role.TOGGLER.getUser()));
        Assert.assertEquals(usersWithPermissions.get(Role.TOGGLER.getUser()), Permission.edit.name());
        universeSettings.saveUsersDialog();
        
        ui.logout();

        verifyEditPermissions(Role.TOGGLER, testingUniverseName);

        ui.login();
        
        leftNav.goToUniverses();
        universeSettings.openUsersDialog(testingUniverseName);
        usersWithPermissions = universeSettings.getUsersWithPermissions();
        Assert.assertEquals(usersWithPermissions.size(),1);
        Assert.assertTrue(usersWithPermissions.containsKey(Role.TOGGLER.getUser()));
        Assert.assertEquals(usersWithPermissions.get(Role.TOGGLER.getUser()), Permission.edit.name());
        
        // Clicking once on the permission should toggle permission to manage
        universeSettings.getUserAccessToggle(universeSettings.getDialogUserRows().get(0)).click();
        usersWithPermissions = universeSettings.getUsersWithPermissions();
        Assert.assertEquals(usersWithPermissions.size(),1);
        Assert.assertTrue(usersWithPermissions.containsKey(Role.TOGGLER.getUser()));
        Assert.assertEquals(usersWithPermissions.get(Role.TOGGLER.getUser()), Permission.manage.name());
        universeSettings.saveUsersDialog();
        
        ui.logout();

        verifyManagePermissions(Role.TOGGLER, testingUniverseName);

        ui.login();
        
        leftNav.goToUniverses();
        universeSettings.deleteUniverse(testingUniverseName);
        ui.logout();
    }

    private void verifyReadOnlyPermissions(Role role, String universe) throws Exception {
        logger.info("verifying 'read-only' permissions of '" + role + "' to the universe '" + universe + "'");
        
        verifyReadPermissions(role, universe);
        verifyUserCannotEditAttributes(role, GROUP_SERVLET, NODE_TRADE_SERVICE);
    }

    private void verifyEditPermissions(Role role, String universe) throws Exception {
        logger.info("verifying 'edit' permissions of '" + role + "' to the universe '" + universe + "'");
        
        verifyReadPermissions(role, universe);
        verifyUserCanEditAttributes(role, GROUP_SERVLET, ATTRIBUTE_NAME_ACCESS, ATTRIBUTE_VALUE_ACCESS);
    }

    /**
     * Verifies that user with the 'manage' permission:
     * 1. Cannot see edit button
     * 2. Cannot see rename button
     * 3. Cannot see delete button
     * 4. Can grant permissions to the universe for other users and those assigned users can access universe according to their privilege
     * 
     * @param role
     * @param universe
     * @throws Exception
     */
    private void verifyManagePermissions(Role role, String universe, Role... omitUsers) throws Exception {
        logger.info("verifying 'manage' permissions of '" + role + "' to the universe '" + universe + "'");
        
        ui.login(role);
        leftNav.goToUniverses();

        PageElement universeRow = universeSettings.getUniverseRow(universe);
        Assert.assertFalse(universeSettings.isEditLinkPresent(universeRow));
        Assert.assertFalse(universeSettings.isRenameLinkPresent(universeRow));
        Assert.assertFalse(universeSettings.isDeleteLinkPresent(universeRow));

        List<Role> omitUsersList = Arrays.asList(omitUsers);
        grantCommonUserPermissions(universe, omitUsersList);
        ui.logout();

        if (!omitUsersList.contains(Role.READER)) {
            verifyReadOnlyPermissions(Role.READER, universe);
        }
        
        if (!omitUsersList.contains(Role.EDITOR)) {
            verifyEditPermissions(Role.EDITOR, universe);
        }
    }

    /**
     * Make sure the user 'Reader' has read permission, 'Editor' has edit permission and 'dummyUser' has manage permission for the universe.
     * User has to be logged in before calling this method
     * User does not log out after granting permissions
     *
     * @param role
     * @param universe
     * @param omitUsersList
     * @throws Exception
     */
    private void grantCommonUserPermissions(String universe, List<Role> omitUsersList) throws Exception {
        leftNav.goToUniverses();

        if (!omitUsersList.contains(Role.READER)) {
            universeSettings.addUser(universe, Role.READER.name(), Permission.read);
        }

        if (!omitUsersList.contains(Role.EDITOR)) {
            universeSettings.addUser(universe, Role.EDITOR.name(), Permission.edit);
        }

        universeSettings.addUser(universe, DUMMY_USER, Permission.manage);
        
        universeSettings.openUsersDialog(universe);
        
        Map<String, String> assignedUsers = universeSettings.getUsersWithPermissions();
        Map<String, String> expectedUserMap = new HashMap<>();
        expectedUserMap.put(Role.READER.getUser().toLowerCase(), Permission.read.name());
        expectedUserMap.put(Role.EDITOR.getUser().toLowerCase(), Permission.edit.name());
        expectedUserMap.put(DUMMY_USER.toLowerCase(), Permission.manage.name());
        logger.info("Expected users: {}", expectedUserMap);
        logger.info("Found users: {}", assignedUsers);
        
        for (String expectedUser : expectedUserMap.keySet()) {
            Assert.assertTrue(assignedUsers.containsKey(expectedUser), " User '" + expectedUser + "' expected in the list of assigned users of the universe '" + universe + "'");
            Assert.assertEquals(expectedUserMap.get(expectedUser), assignedUsers.get(expectedUser));
        }
        
        universeSettings.closeUsersDialog();
    }

    /**
     * Specified universe should have at least 1 node in the map
     *
     * Make sure that the previous user is logged out before calling this
     *
     * Verifies that the role
     *
     * 1. Cannot create a universe
     * 2. Can access given universe
     * 3. Can view the map
     * 4. Cannot edit given universe
     * 5. Cannot delete given universe
     * 6. Cannot grant permissions for given universe
     * 7. User cannot select rows (bulk editing)
     *
     * User is logged out after all the above is verified
     *
     * @param role
     * @param universe
     * @throws Exception
     */
    private void verifyReadPermissions(Role role, String universe) throws Exception {
        ui.login(role);
        leftNav.goToUniverses();

        logger.info("Guest shall not see 'create new universe' button");
        Assert.assertFalse(universeSettings.getCreateUniverseButton().isDisplayed());

        logger.info("User is able to access universe");
        universeSettings.isUniversePresent(universe);

        PageElement universeRow = universeSettings.getUniverseRow(universe);
        logger.info("User shall not be able to modify universe");
        Assert.assertFalse(universeSettings.isEditLinkPresent(universeRow));

        logger.info("User shall not be able to delete universe");
        Assert.assertFalse(universeSettings.isDeleteLinkPresent(universeRow));

        logger.info("User shall not be able to rename universe");
        Assert.assertFalse(universeSettings.isDeleteLinkPresent(universeRow));

        logger.info("User shall not be able to edit users");
        boolean isException = false;
        try {
            universeSettings.getUsersCellClickableContainer(universe).click();
            
            ui.waitUntilVisible(By.cssSelector(".modal-backdrop"), 2);
        } catch (TimeoutException e) {
            isException = true;
        }
        Assert.assertTrue(isException);

        logger.info("User shall not be able to select rows");
        By selector = By.cssSelector(".ui-grid input[type='checkbox']");
        Assert.assertEquals(ui.getDriver().findElements(selector).size(), 0);

        // Verify there are nodes in the map
        leftNav.goToMapViewPage();

        if (topNav.getUniverseNames().contains(Universe.DEFAULT_UNIVERSE)) {
        	topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        }

        ui.getCanvas().waitForUpdate();
        Assert.assertTrue(ui.getCanvas().getArrayOfNodeNames().length > 0);

        ui.logout();
    }

    /**
     * User has to be logged out before calling this method
     *
     * Verifies if user can edit attributes
     *
     * @param role
     * @param groupName
     * @param attributeName
     * @param attributeValue
     * @throws Exception
     */
    private void verifyUserCanEditAttributes(Role role, String groupName, String attributeName, String attributeValue) throws Exception {
        ui.login(role);
        leftNav.goToMapViewPage();

        if (topNav.getUniverseNames().contains(Universe.DEFAULT_UNIVERSE)) {
        	topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        }

        if (!ui.getPerspectivesControl().isPerspectiveActive(TYPE_PERSPECTIVE)) {
            ui.getPerspectivesControl().selectPerspectiveByName(TYPE_PERSPECTIVE);
        }

        Assert.assertTrue(ui.getPerspectivesControl().isPerspectiveActive(TYPE_PERSPECTIVE));

        Canvas canvas = ui.getCanvas();

        // User should be able to delete attribute
        canvas.deleteCustomAttributeFromNodesIfItExists(new String[] {GROUP_SERVLET}, attributeName);

        logger.info("select a group node");

        canvas.getNodeByNameSubstring(groupName).click();

        logger.info("type an attribute name and wait for auto refresh");
        DetailsPanel details = ui.getDetailsPanel();
        details.addNewAttribute(attributeName, attributeValue, false);
        Assert.assertTrue(details.isAttributeRowPresent(DetailsPanel.AttributeType.OTHER_ATTRIBUTES,
            attributeName, attributeValue, true, false), "Expected attribute to be present");

        logger.info("delete the attribute");
        details.deleteAttribute(attributeName, attributeValue);
        List<AttributeRow> rows = details.getAttributeRowsByName(
                DetailsPanel.AttributeType.OTHER_ATTRIBUTES, attributeName);
        Assert.assertEquals(rows.size(), 1);
        Assert.assertEquals(rows.get(0).getEndTimeValueCell().getText(), "<empty>");

        ui.logout();
    }

    /**
     * User has to be logged out before calling this method.
     * User must have access to the universe and universe must have at least one node
     *
     * @param role
     * @param groupName
     * @param nodeName
     * @throws Exception
     */
    private void verifyUserCannotEditAttributes(Role role, String groupName, String nodeName) throws Exception {
        ui.login(role);
        leftNav.goToMapViewPage();

        if (topNav.getUniverseNames().contains(Universe.DEFAULT_UNIVERSE)) {
        	topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        }

        if (!ui.getPerspectivesControl().isPerspectiveActive(TYPE_PERSPECTIVE)) {
            ui.getPerspectivesControl().selectPerspectiveByName(TYPE_PERSPECTIVE);
        }

        Assert.assertTrue(ui.getPerspectivesControl().isPerspectiveActive(TYPE_PERSPECTIVE));
        logger.info("selecting a group node");
        Canvas canvas = ui.getCanvas();
        canvas.expandGroup(canvas.getNodeByNameSubstring(groupName));
        canvas.getCtrl().fitAllToView();
        canvas.getNodeByName(nodeName).click();

        logger.info("checking the basic attributes are not editable");
        DetailsPanel details = ui.getDetailsPanel();
        details.waitUntilAttributeTableDataLoaded(DetailsPanel.AttributeType.BASIC_ATTRIBUTES);
        details.scrollToAttributesTable(DetailsPanel.AttributeType.BASIC_ATTRIBUTES);
        Assert.assertFalse(details.getAttributeRows(DetailsPanel.AttributeType.BASIC_ATTRIBUTES).isEmpty());

        logger.info("checking the decorated and custom attributes' names are not editable");
        details.scrollToAttributesTable(DetailsPanel.AttributeType.OTHER_ATTRIBUTES);
        boolean otherAttributes =
            details.getAttributeRows(DetailsPanel.AttributeType.OTHER_ATTRIBUTES).isEmpty();
        if(!otherAttributes) {
            AttributeRow attributeRow =
                details.getAttributeRows(DetailsPanel.AttributeType.OTHER_ATTRIBUTES).get(0);

            attributeRow.getNameCell().click();
            boolean isEditable = details.getAttributeInputField(DetailsPanel.AttributeType.OTHER_ATTRIBUTES).isDisplayed();
            Assert.assertFalse(isEditable, "Attributes should not be editable");
            attributeRow.getEndTimeValueCell().click();
            isEditable = details.getAttributeInputField(DetailsPanel.AttributeType.OTHER_ATTRIBUTES).isDisplayed();
            Assert.assertFalse(isEditable, "Attributes should not be editable");
        }
        ui.logout();
    }
}
