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
package com.ca.apm.test.atc.grouping;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.TopNavigationPanel;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.testbed.atc.TeamCenterRegressionTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class SemanticGroupingTest extends UITest {

    private UI ui;
    
    private LeftNavigationPanel leftNav;
    private TopNavigationPanel topNav;

    private static final String UNIVERSE = "SemanticGroupingTest - Guest universe";

    private static final String SCROLLBAR_PERSPECTIVE_PREFIX = "grp";
    
    private static final String ATTR_1 = "wsNamespace";
    private static final String ATTR_2 = "Experience";
    private static final String ATTR_3 = "Hostname";
    private static final String ATTR_4 = "Application";
    private static final String ATTR_5 = "Name";
    private static final String ATTR_6 = "wsOperation";
    private static final String ATTR_7 = "Type";
    private static final String ATTR_8 = "Location";

    private static final String EXISTING_PERSPECTIVE_3 = ATTR_3;
    private static final String EXISTING_PERSPECTIVE_7 = ATTR_7;
    private static final String EXISTING_PERSPECTIVE_8 = ATTR_8;

    protected void init() throws Exception {
        ui = getUI();
        
        logger.info("log into APM Server");
        ui.login();
        
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    public void testSwitchPerspective() throws Exception {
        init();
        
        leftNav.goToMapViewPage();
        
        ui.getTimeline().turnOffLiveMode();
        
        PerspectivesControl control = ui.getPerspectivesControl();
        
        logger.info("Switch to Dashboard");
        leftNav.goToDashboardPage();

        logger.info("Default perspective should be '{}'", PerspectivesControl.DEFAULT_PERSPECTIVE);
        assertTrue(control.isPerspectiveActive(PerspectivesControl.DEFAULT_PERSPECTIVE));
        assertEquals(ui.getBottomBar().getActivePerspectiveName(), PerspectivesControl.DEFAULT_PERSPECTIVE);

        String[] attributes = ui.getBottomBar().getActivePerspectiveAttributes();
        String[] boldAttributes = ui.getBottomBar().getActivePerspectiveAttributesInBold();
        String[] tabHeaders = ui.getTrendCards().getArrayOfTabHeaderTexts();

        assertEquals(boldAttributes.length, tabHeaders.length);
        for (int i = 0; i < tabHeaders.length; i++) {
            assertEquals(boldAttributes[i], tabHeaders[i]);
        }
        assertTrue(attributes.length >= tabHeaders.length);

        logger.info("Switch to Map view");
        leftNav.goToMapViewPage();

        logger.info("Default perspective should be '{}'", PerspectivesControl.DEFAULT_PERSPECTIVE);
        assertTrue(control.isPerspectiveActive(PerspectivesControl.DEFAULT_PERSPECTIVE));
        assertEquals(ui.getBottomBar().getActivePerspectiveName(), PerspectivesControl.DEFAULT_PERSPECTIVE);
        String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();

        logger.info("Switch perspective to '{}'", PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        control.selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
        assertTrue(control.getDropdown().getText().contains(PerspectivesControl.NO_GROUPS_PERSPECTIVE));
        assertEquals(ui.getBottomBar().getActivePerspectiveName(), PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        assertFalse(Arrays.equals(nodes1, nodes2));

        logger.info("Switch to Dashboard");
        leftNav.goToDashboardPage();

        logger.info("default perspective should be '{}'", PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        assertEquals(ui.getBottomBar().getActivePerspectiveName(), PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        assertEquals(ui.getBottomBar().getActivePerspectiveAttributes().length, 0);
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAddPerspective() throws Exception {
        final String PERSPECTIVE_1 = ATTR_1;
        final String PERSPECTIVE_2 = ATTR_2;
        
        init();
        
        logger.info("Switch to Map view");
        leftNav.goToMapViewPage();

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(ui.getPerspectivesControl().isPerspectiveActive(PERSPECTIVE_1));

        logger.info("Switch to Settings view");
        leftNav.goToPerspectives();

        ui.getPerspectiveSettings().deletePerspectiveIfExists(PERSPECTIVE_1);
        ui.getPerspectiveSettings().deletePerspectiveIfExists(PERSPECTIVE_2);

        logger.info("Create new main perspective '{}'", PERSPECTIVE_1);
        ui.getPerspectiveSettings().addPerspective(PERSPECTIVE_1, false);

        logger.info("Switch to newly added perspective '{}'", PERSPECTIVE_1);
        leftNav.goToMapViewPage();
        ui.getCanvas().waitForDisplay();
        ui.getPerspectivesControl().selectPerspectiveByName(PERSPECTIVE_1);
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(PERSPECTIVE_1));
        String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(Arrays.equals(nodes1, nodes2));

        logger.info("Create main perspective '{}' from dropdown", PERSPECTIVE_2);
        assertFalse(ui.getPerspectivesControl().isPerspectivePresent(PERSPECTIVE_2));
        ui.getPerspectivesControl().addPerspective(PERSPECTIVE_2, false);
        ui.getPerspectivesControl().selectPerspectiveByName(PERSPECTIVE_2);
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(PERSPECTIVE_2));
        String[] nodes3 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(Arrays.equals(nodes2, nodes3));

        logger.info("remove newly created perspectives '{}' and '{}'", PERSPECTIVE_1, PERSPECTIVE_2);
        leftNav.goToPerspectives();
        ui.getPerspectiveSettings().deletePerspective(PERSPECTIVE_1);
        ui.getPerspectiveSettings().deletePerspective(PERSPECTIVE_2);
        
        assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(PERSPECTIVE_1));
        assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(PERSPECTIVE_2));
        
        leftNav.goToMapViewPage();
        ui.getCanvas().waitForDisplay();
        
        String[] nodes4 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(ui.getPerspectivesControl().isPerspectivePresent(PERSPECTIVE_1));
        assertFalse(ui.getPerspectivesControl().isPerspectivePresent(PERSPECTIVE_2));
        
        assertFalse(Arrays.equals(nodes3, nodes4));
        
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(PerspectivesControl.DEFAULT_PERSPECTIVE));

        logger.info("Do cleanup tasks");
        ui.cleanup();
        ui.logout();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testAddMultiLevelPerspective() throws Exception {
        final String TEST_PERSPECTIVE_MLT_LVL_A = "grouping01 mltlvl";
        final String TEST_PERSPECTIVE_MLT_LVL_B = ATTR_4 + ", " + ATTR_5;
        
        init();
        
        logger.info("Switch to Map view");
        leftNav.goToMapViewPage();
        
        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();

        logger.info("Switch to Settings view");
        leftNav.goToPerspectives();

        logger.info("Create new multi level perspective");
        String[] attributes = new String[] {ATTR_3, ATTR_4, ATTR_1, ATTR_5, ATTR_6, ATTR_7};
        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        ui.getPerspectiveSettings().addMultiLevelPerspective(attributes, TEST_PERSPECTIVE_MLT_LVL_A, false);

        leftNav.goToMapViewPage();

        ui.getPerspectivesControl().selectPerspectiveByName(TEST_PERSPECTIVE_MLT_LVL_A);
        String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(Arrays.equals(nodes1, nodes2));
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(TEST_PERSPECTIVE_MLT_LVL_A));

        nodes1 = ui.getCanvas().getArrayOfNodeNames();
        
        logger.info("Create second new sub perspective");

        logger.info("Switch to Settings view");
        leftNav.goToPerspectives();

        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        ui.getPerspectiveSettings().selectItemInLevelMenu(1, ATTR_4);
        ui.getPerspectiveSettings().selectItemInLevelMenu(2, ATTR_5);
        assertEquals(ui.getPerspectiveSettings().getPerspectiveNameInputValue(), TEST_PERSPECTIVE_MLT_LVL_B);
        ui.getPerspectiveSettings().saveEdit();

        leftNav.goToMapViewPage();
        ui.getPerspectivesControl().selectPerspectiveByName(TEST_PERSPECTIVE_MLT_LVL_B);

        nodes2 = ui.getCanvas().getArrayOfNodeNames();
        logger.info("nodes1: {}", (Object) nodes1);
        logger.info("nodes2: {}", (Object) nodes2);
        assertFalse(Arrays.equals(nodes1, nodes2));
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(TEST_PERSPECTIVE_MLT_LVL_B));

        logger.info("remove newly created perspectives '{}' and '{}'", TEST_PERSPECTIVE_MLT_LVL_A, TEST_PERSPECTIVE_MLT_LVL_B);
        nodes1 = ui.getCanvas().getArrayOfNodeNames();

        leftNav.goToPerspectives();

        ui.getPerspectiveSettings().deletePerspective(TEST_PERSPECTIVE_MLT_LVL_A);
        ui.getPerspectiveSettings().deletePerspective(TEST_PERSPECTIVE_MLT_LVL_B);

        leftNav.goToMapViewPage();

        nodes2 = ui.getCanvas().getArrayOfNodeNames();
        logger.info("nodes1: {}", (Object) nodes1);
        logger.info("nodes2: {}", (Object) nodes2);
        assertFalse(Arrays.equals(nodes1, nodes2));
        assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(TEST_PERSPECTIVE_MLT_LVL_A));
        assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(TEST_PERSPECTIVE_MLT_LVL_B));

        logger.info("Do cleanup tasks");
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testDeletingLevels() throws Exception {
        final String TEST_PERSPECTIVE_FOR_DELETE = ATTR_3 + ", " + ATTR_5 + ", " + ATTR_4;
        
        init();

        leftNav.goToMapViewPage();
        
        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();

        logger.info("Switch to Settings view");
        leftNav.goToPerspectives();

        logger.info("Delete the perspective if it already exists");

        ui.getPerspectiveSettings().deletePerspectiveIfExists(TEST_PERSPECTIVE_FOR_DELETE);

        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        assertEquals(ui.getPerspectiveSettings().getListOfLevelsInEditDialog().size(), 3);

        ui.getPerspectiveSettings().getRemoveLevelBtn().click();
        assertEquals(ui.getPerspectiveSettings().getListOfLevelsInEditDialog().size(), 2);

        ui.getPerspectiveSettings().getRemoveLevelBtn().click();
        assertEquals(ui.getPerspectiveSettings().getListOfLevelsInEditDialog().size(), 1);
        assertFalse(ui.getPerspectiveSettings().isRemoveLevelBtnPresent());

        logger.info("Create new multi level perspective");
        ui.getPerspectiveSettings().selectItemInLevelMenu(1, ATTR_3);
        
        ui.getPerspectiveSettings().getAddLevelBtn().click();
        ui.getPerspectiveSettings().selectItemInLevelMenu(2, ATTR_5);

        ui.getPerspectiveSettings().getAddLevelBtn().click();
        ui.getPerspectiveSettings().selectItemInLevelMenu(3, ATTR_4);

        assertEquals(ui.getPerspectiveSettings().getPerspectiveNameInputValue(), TEST_PERSPECTIVE_FOR_DELETE);
        ui.getPerspectiveSettings().saveEdit();
        assertTrue(ui.getPerspectiveSettings().isPerspectivePresent(TEST_PERSPECTIVE_FOR_DELETE));

        leftNav.goToMapViewPage();
        ui.getCanvas().waitForDisplay();
        ui.getPerspectivesControl().selectPerspectiveByName(TEST_PERSPECTIVE_FOR_DELETE);
        String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(Arrays.equals(nodes1, nodes2));
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(TEST_PERSPECTIVE_FOR_DELETE));

        logger.info("remove newly created perspectives");
        leftNav.goToPerspectives();
        ui.getPerspectiveSettings().deletePerspective(TEST_PERSPECTIVE_FOR_DELETE);
        leftNav.goToMapViewPage();
        String[] nodes3 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(Arrays.equals(nodes2, nodes3));
        assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(TEST_PERSPECTIVE_FOR_DELETE));

        logger.info("Do cleanup tasks");
        ui.cleanup();
        ui.logout();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    public void testEditPerspectives() throws Exception {
        final String[] MULTI_LVL_PERSPECTIVE_DEF = new String[] {ATTR_3, ATTR_4, ATTR_6, ATTR_5, ATTR_1, ATTR_7};
        final String MULTI_LVL_PERSPECTIVE_NAME = "grouping01";
        final String MULTI_LVL_PERSPECTIVE_MOFID_NAME = "newgrouping01";
        
        final String[] SIMPLE_PERSPECTIVES_TO_CREATE = new String[] {ATTR_3, ATTR_4, ATTR_6, ATTR_1};
                
        final String PERSPECTIVE_EDITED = "test personal p. edited";
        
        init();

        ui.getLeftNavigationPanel().goToMapViewPage();

        logger.info("turn off the Live mode");
        ui.getTimeline().turnOffLiveMode();

        logger.info("Switch to Settings view");
        leftNav.goToPerspectives();

        logger.info("default perspective should be {}", PerspectivesControl.DEFAULT_PERSPECTIVE);
        assertTrue(ui.getPerspectiveSettings().isSetAsDefault(PerspectivesControl.DEFAULT_PERSPECTIVE));

        logger.info("Delete the perspectives if they already exist");
        for (String name : SIMPLE_PERSPECTIVES_TO_CREATE) {
            ui.getPerspectiveSettings().deletePerspectiveIfExists(name);
        }

        logger.info("Create {} new main perspectives", SIMPLE_PERSPECTIVES_TO_CREATE.length);
        int groups1 = ui.getPerspectiveSettings().getListOfPerspectiveNames().size();
        for (String name : SIMPLE_PERSPECTIVES_TO_CREATE) {
            ui.getPerspectiveSettings().addPerspective(name, false);
        }

        int groups2 = ui.getPerspectiveSettings().getListOfPerspectiveNames().size();
        assertNotEquals(groups1, groups2);

        leftNav.goToMapViewPage();
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(PerspectivesControl.DEFAULT_PERSPECTIVE));
        
        String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();

        logger.info("edit existing perspective '{}'", EXISTING_PERSPECTIVE_7);
        ui.getPerspectivesControl().selectPerspectiveByName(EXISTING_PERSPECTIVE_7);
        ui.getCanvas().waitForUpdate();
        ui.getPerspectivesControl().getEditIcon().click();
        
        ui.getPerspectiveSettings().selectItemInLevelMenu(1, ATTR_3);
        WebElement inp = ui.getPerspectiveSettings().getPerspectiveNameInput();
        inp.clear();
        inp.sendKeys(PERSPECTIVE_EDITED);
        ui.getPerspectiveSettings().saveEdit();

        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(PERSPECTIVE_EDITED));
        String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
        assertFalse(Arrays.equals(nodes1, nodes2));

        logger.info("not be possible to edit '{}'", PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        ui.getPerspectivesControl().selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);
        ui.getCanvas().waitForUpdate();
        assertFalse(ui.getPerspectivesControl().isEditIconPresent());
        assertFalse(ui.getPerspectivesControl().isCloneIconPresent());

        logger.info("check that it is not possible to delete default perspective '{}'", PerspectivesControl.DEFAULT_PERSPECTIVE);
        leftNav.goToPerspectives();
        assertTrue(ui.getPerspectiveSettings().isSetAsDefault(PerspectivesControl.DEFAULT_PERSPECTIVE));
        assertFalse(ui.getPerspectiveSettings().getDeleteButton(PerspectivesControl.DEFAULT_PERSPECTIVE).isDisplayed());

        logger.info("Create multi level perspective '{}'", MULTI_LVL_PERSPECTIVE_NAME);
        groups1 = ui.getPerspectiveSettings().getListOfPerspectiveNames().size();
        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        ui.getPerspectiveSettings().addMultiLevelPerspective(MULTI_LVL_PERSPECTIVE_DEF, MULTI_LVL_PERSPECTIVE_NAME, false);
        groups2 = ui.getPerspectiveSettings().getListOfPerspectiveNames().size();
        assertNotEquals(groups1, groups2);

        logger.info("rename new perspective to '{}'", MULTI_LVL_PERSPECTIVE_MOFID_NAME);
        List<String> groupsArray1 = ui.getPerspectiveSettings().getListOfPerspectiveNames();
        ui.getPerspectiveSettings().selectEditLink(MULTI_LVL_PERSPECTIVE_NAME);
        inp = ui.getPerspectiveSettings().getPerspectiveNameInput();
        inp.clear();
        inp.sendKeys(MULTI_LVL_PERSPECTIVE_MOFID_NAME);
        ui.getPerspectiveSettings().saveEdit();
        List<String> groupsArray2 = ui.getPerspectiveSettings().getListOfPerspectiveNames();
        assertNotEquals(groupsArray1, groupsArray2);

        logger.info("set new perspective '{}' as default", MULTI_LVL_PERSPECTIVE_MOFID_NAME);
        ui.getPerspectiveSettings().setAsDefault(MULTI_LVL_PERSPECTIVE_MOFID_NAME);
        assertTrue(ui.getPerspectiveSettings().isSetAsDefault(MULTI_LVL_PERSPECTIVE_MOFID_NAME));
        
        Utils.sleep(2000);
        logger.info("restore the original default perspective '{}'", PerspectivesControl.DEFAULT_PERSPECTIVE);
        ui.getPerspectiveSettings().setAsDefault(PerspectivesControl.DEFAULT_PERSPECTIVE);
        assertTrue(ui.getPerspectiveSettings().isSetAsDefault(
            PerspectivesControl.DEFAULT_PERSPECTIVE));

        logger.info("remove newly created test perspectives '{}' and '{}'", MULTI_LVL_PERSPECTIVE_MOFID_NAME, PERSPECTIVE_EDITED);
        groups1 = ui.getPerspectiveSettings().getListOfPerspectiveNames().size();
        ui.getPerspectiveSettings().deletePerspective(MULTI_LVL_PERSPECTIVE_MOFID_NAME);
        ui.getPerspectiveSettings().deletePerspective(PERSPECTIVE_EDITED);
        
        Utils.sleep(2000);
        
        groups2 = ui.getPerspectiveSettings().getListOfPerspectiveNames().size();
        assertNotEquals(groups1, groups2);

        logger.info("Do cleanup tasks");
        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testOpeningAndClosingDialog() throws Exception {
        init();

        logger.info("Switch to Settings view");
        leftNav.goToPerspectives();

        logger.info("Close the dialog by clicking on the close button");
        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        assertTrue(ui.getModalDialog().isModalDialogPresent());
        ui.getPerspectiveSettings().closeModalDialog();
        assertFalse(ui.getModalDialog().isModalDialogPresent());

        logger.info("Close the dialog by clicking outside the dialog");
        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        assertTrue(ui.getModalDialog().isModalDialogPresent());

        // this works in Firefox but not in Chrome
        ui.getPerspectiveSettings().getModalDialogSurround().click();
        try {
            ui.getModalDialog().waitForModalDialogFadeOut();
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage(), e);
        }

        // the dialog is still opened in Chrome
        if (ui.getModalDialog().isModalDialogPresent()) {
            ui.getPerspectiveSettings().closeModalDialog();
            ui.getPerspectiveSettings().displayAddPerspectiveDialog();
            // this works in Chrome but not in Firefox
            Actions actions = new Actions(ui.getDriver());
            actions.moveByOffset(50, 50).build().perform();;
            actions.click().build().perform();;
            ui.getModalDialog().waitForModalDialogFadeOut();
        }

        assertFalse(ui.getModalDialog().isModalDialogPresent());

        logger.info("Do cleanup tasks");
        ui.cleanup();
    }

    private void deleteUniverseIfExists(String universeName) throws Exception {
        logger.info("Delete the universe '{}' if it exists", universeName);
        
        ui.getLeftNavigationPanel().goToUniverses();
        if (ui.getUniverseSettings().isUniversePresent(universeName)) {
            ui.getUniverseSettings().deleteUniverse(universeName);
        } 
    }
    
    private void createGuestUniverse() throws Exception {
        logger.info("Create an universe for the guest user: " + UNIVERSE);

        ui.login(Role.ADMIN);

        deleteUniverseIfExists(UNIVERSE);

        ui.getUniverseSettings().createUniverse(UNIVERSE);
        ui.getUniverseSettings().addUser(UNIVERSE, Role.GUEST.getUser(), UI.Permission.read);

        ui.logout();
    }

    /**
     * Cleanup after each test case
     * 
     * @throws Exception
     */
    private void deleteGuestUniverse() throws Exception {
        ui = getUI();
        ui.login();
        
        logger.info("Delete the universe '{}' for the guest user if it exists", UNIVERSE);
        
        ui.getLeftNavigationPanel().goToUniverses();
        if (ui.getUniverseSettings().isUniversePresent(UNIVERSE)) {
            ui.getUniverseSettings().deleteUniverse(UNIVERSE);
        }
        
        ui.logout();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testReadOnly() throws Exception {
        final String GUESTS_PERSONAL_PERSPECTIVE = "test guest's personal p.";
        final String GUESTS_PERSONAL_PERSPECTIVE_EDITED = "test guest's personal p. edited";
        final String GUESTS_PERSONAL_PERSPECTIVE_2 = "test guest's personal p. 2";
        
        ui = getUI();

        createGuestUniverse();

        try {
            ui.login(Role.GUEST);
            ui.getTimeline().turnOffLiveMode();
            
            leftNav = ui.getLeftNavigationPanel();

            logger.info("Delete the guest's test perspectives first if they already exist");
            ui.getPerspectiveSettings().deletePerspectiveIfExists(GUESTS_PERSONAL_PERSPECTIVE);
            ui.getPerspectiveSettings().deletePerspectiveIfExists(GUESTS_PERSONAL_PERSPECTIVE_EDITED);
            
            logger.info("Guest is able to create personal perspective from dropdown");
            leftNav.goToPerspectives();

            leftNav.goToMapViewPage();
            String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();
            ui.getPerspectivesControl().addPerspective(GUESTS_PERSONAL_PERSPECTIVE, true);
            assertTrue(ui.getPerspectivesControl().isPerspectiveActive(GUESTS_PERSONAL_PERSPECTIVE));
            ui.getPerspectivesControl().expand();
            assertTrue(ui.getPerspectivesControl().isPersonalPerspectivePresent(GUESTS_PERSONAL_PERSPECTIVE));
            String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
            assertFalse(Arrays.equals(nodes1, nodes2));

            logger.info("Guest is able to edit personal perspective from dropdown");
            ui.getPerspectivesControl().getEditIcon().click();
            ui.getModalDialog().waitForModalDialogFadeIn();

            ui.getPerspectiveSettings().selectItemInLevelMenu(1, ATTR_3);
            WebElement inp = ui.getPerspectiveSettings().getPerspectiveNameInput();
            inp.clear();
            inp.sendKeys(GUESTS_PERSONAL_PERSPECTIVE_EDITED);
            ui.getPerspectiveSettings().saveEdit();

            assertTrue(ui.getPerspectivesControl().isPerspectiveActive(GUESTS_PERSONAL_PERSPECTIVE_EDITED));
            String[] nodes3 = ui.getCanvas().getArrayOfNodeNames();
            assertFalse(Arrays.equals(nodes2, nodes3));

            logger.info("Guest is can see newly created perspective in settings page");
            leftNav.goToPerspectives();
            assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(GUESTS_PERSONAL_PERSPECTIVE_EDITED));
            ui.getPerspectiveSettings().deletePerspective(GUESTS_PERSONAL_PERSPECTIVE_EDITED);

            logger.info("Guest is unable to remove public perspective");
            assertFalse(ui.getPerspectiveSettings().isPerspectivePersonal(EXISTING_PERSPECTIVE_3));
            assertFalse(ui.getPerspectiveSettings().isDeleteButtonPresent(EXISTING_PERSPECTIVE_3));

            logger.info("Guest is unable to create public perspective");
            ui.getPerspectiveSettings().displayAddPerspectiveDialog();
            assertFalse(ui.getPerspectiveSettings().isPublicCheckboxEnabled());
            ui.getPerspectiveSettings().closeModalDialog();

            logger.info("Guest is able to create personal perspective from settings page");
            ui.getPerspectiveSettings().displayAddPerspectiveDialog();
            ui.getPerspectiveSettings().addMultiLevelPerspective(new String[] {ATTR_4}, GUESTS_PERSONAL_PERSPECTIVE_2, false);

            assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(GUESTS_PERSONAL_PERSPECTIVE_2));
            assertTrue(ui.getPerspectiveSettings().isDeleteButtonPresent(GUESTS_PERSONAL_PERSPECTIVE_2));
            assertTrue(ui.getPerspectiveSettings().isEditLinkPresent(GUESTS_PERSONAL_PERSPECTIVE_2));
            assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(GUESTS_PERSONAL_PERSPECTIVE_2));
            ui.getPerspectiveSettings().deletePerspective(GUESTS_PERSONAL_PERSPECTIVE_2);

            logger.info("Guest cannot alter public perspectives from settings page");
            List<String> groupNames = ui.getPerspectiveSettings().getListOfPerspectiveNames();
            for (String groupName : groupNames) {
                if (!ui.getPerspectiveSettings().isPerspectivePersonal(groupName)) {
                    assertFalse(ui.getPerspectiveSettings().isPerspectivePersonal(groupName));
                    assertFalse(ui.getPerspectiveSettings().isDeleteButtonPresent(groupName));
                    assertFalse(ui.getPerspectiveSettings().isEditLinkPresent(groupName));
                    assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(groupName));
                }
            }

            assertTrue(ui.getPerspectiveSettings().isAddPerspectiveBtnPresent());
        } catch (Exception e) {
            logger.info("Do cleanup tasks");
            ui.logout();
            deleteGuestUniverse();
            ui.cleanup();
        }
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    public void testPerspectiveScrollbars() throws Exception {
        init();
        
        final int PERSPECTIVE_COUNT = 25;
        PerspectivesControl control = ui.getPerspectivesControl();

        leftNav.goToMapViewPage();
        ui.getTimeline().turnOffLiveMode();

        logger.info("Scrollbar should NOT be present in the dropdown");
        control.expand();
        assertFalse(control.getDropdown().findElement(By.cssSelector("ul")).hasVerticalScrollbar());

        logger.info("Scrollbar should NOT be present in perspectives page");
        leftNav.goToPerspectives();
        for (int i = 1; i <= PERSPECTIVE_COUNT; i++) {
            ui.getPerspectiveSettings().deletePerspectiveIfExists(SCROLLBAR_PERSPECTIVE_PREFIX + i);
        }
        assertFalse(ui.getElementProxy(By.cssSelector("div[class~='ui-grid-viewport']")).hasVerticalScrollbar());

        logger.info("Create many new perspectives");
        String[] subGroups = new String[] {ATTR_4};
        for (int i = 1; i <= PERSPECTIVE_COUNT; i++) {
            if (!ui.getPerspectiveSettings().isPerspectivePresent(SCROLLBAR_PERSPECTIVE_PREFIX + i)) {
                ui.getPerspectiveSettings().displayAddPerspectiveDialog();
                Utils.sleep(100);
                ui.getPerspectiveSettings().addMultiLevelPerspective(subGroups, SCROLLBAR_PERSPECTIVE_PREFIX + i, false);
                Utils.sleep(100);
            }
        }

        logger.info("Scrollbar should be present in perspectives page");
        assertTrue(ui.getElementProxy(By.cssSelector("div[class~='ui-grid-viewport']")).hasVerticalScrollbar());

        logger.info("Scrollbar should be present in the dropdown");
        leftNav.goToMapViewPage();
        assertTrue(control.isPerspectiveActive(PerspectivesControl.DEFAULT_PERSPECTIVE));
        String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();

        control.expand();
        assertTrue(control.getDropdown().findElement(By.cssSelector("ul > div")).hasVerticalScrollbar());
        control.selectPerspectiveByName(SCROLLBAR_PERSPECTIVE_PREFIX + PERSPECTIVE_COUNT);
        assertTrue(ui.getPerspectivesControl().isPerspectiveActive(SCROLLBAR_PERSPECTIVE_PREFIX + PERSPECTIVE_COUNT));
        ui.getCanvas().waitForUpdate();
        String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
        assertTrue(control.getDropdown().getText().contains(SCROLLBAR_PERSPECTIVE_PREFIX + PERSPECTIVE_COUNT));
        assertFalse(Arrays.equals(nodes1, nodes2));

        logger.info("Delete newly created perspectives");
        leftNav.goToPerspectives();
        for (int i = PERSPECTIVE_COUNT; i > 0; i--) {
            assertTrue(ui.getPerspectiveSettings().isPerspectivePresent(SCROLLBAR_PERSPECTIVE_PREFIX + i));
            ui.getPerspectiveSettings().deletePerspective(SCROLLBAR_PERSPECTIVE_PREFIX + i);
            Utils.sleep(100);
        }

        Utils.sleep(1000);

        for (String perspectiveName : ui.getPerspectiveSettings().getListOfPerspectiveNames()) {
            assertFalse(perspectiveName.startsWith(SCROLLBAR_PERSPECTIVE_PREFIX));
        }

        logger.info("Do cleanup tasks");
        ui.logout();

        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testPersonalPerspectives() throws Exception {
        final String ADMIN_PERSONAL = "Admins p.";
        final String GUEST_PERSONAL = "Guests p.";
        // perspective to be changed from public to personal
        final String PUBLIC_2_PERSONAL = EXISTING_PERSPECTIVE_8;

        ui = getUI();
        ui.login(Role.ADMIN);
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();

        logger.info("Create personal perspective for admin: '{}'", ADMIN_PERSONAL);
        leftNav.goToPerspectives();
        ui.getPerspectiveSettings().deletePerspectiveIfExists(ADMIN_PERSONAL);

        leftNav.goToPerspectives();
        ui.getPerspectiveSettings().displayAddPerspectiveDialog();
        ui.getPerspectiveSettings().addMultiLevelPerspective(new String[] {ATTR_3}, ADMIN_PERSONAL, true);
        
        assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(ADMIN_PERSONAL));
        assertTrue(ui.getPerspectiveSettings().isDeleteButtonPresent(ADMIN_PERSONAL));
        assertTrue(ui.getPerspectiveSettings().isEditLinkPresent(ADMIN_PERSONAL));
        assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(ADMIN_PERSONAL));

        logger.info("Change public perspective '{}' to personal", PUBLIC_2_PERSONAL);
        if (ui.getPerspectiveSettings().isPerspectivePersonal(PUBLIC_2_PERSONAL)) {
            ui.getPerspectiveSettings().selectEditLink(PUBLIC_2_PERSONAL);
            assertFalse(ui.getPerspectiveSettings().isPublicCheckboxChecked());
            ui.getPerspectiveSettings().checkPublicCheckbox();
            ui.getPerspectiveSettings().saveEdit();
        }

        assertFalse(ui.getPerspectiveSettings().isPerspectivePersonal(PUBLIC_2_PERSONAL));
        assertTrue(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(PUBLIC_2_PERSONAL));
        ui.getPerspectiveSettings().selectEditLink(PUBLIC_2_PERSONAL);
        assertTrue(ui.getPerspectiveSettings().isPublicCheckboxChecked());
        ui.getPerspectiveSettings().uncheckPublicCheckbox();
        ui.getPerspectiveSettings().saveEdit();

        assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(PUBLIC_2_PERSONAL));
        assertTrue(ui.getPerspectiveSettings().isDeleteButtonPresent(PUBLIC_2_PERSONAL));
        assertTrue(ui.getPerspectiveSettings().isEditLinkPresent(PUBLIC_2_PERSONAL));
        assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(PUBLIC_2_PERSONAL));

        logger.info("Verify we have 2 personal perspectives in dropdown: '{}' and '{}'", ADMIN_PERSONAL, PUBLIC_2_PERSONAL);
        leftNav.goToMapViewPage();
        ui.getPerspectivesControl().expand();
        List<String> namesOfPersonalPerspectives = ui.getPerspectivesControl().getNamesOfPersonalPerspectives();
        assertEquals(namesOfPersonalPerspectives.size(), 2, "Personal perspecives expected: " + ADMIN_PERSONAL + ", " + PUBLIC_2_PERSONAL);
        assertTrue(namesOfPersonalPerspectives.contains(ADMIN_PERSONAL), "List of personal perspectives should contain '" + ADMIN_PERSONAL + "'");
        assertTrue(namesOfPersonalPerspectives.contains(PUBLIC_2_PERSONAL), "List of personal perspectives should contain '" + PUBLIC_2_PERSONAL + "'");

        ui.logout();

        createGuestUniverse();

        try {
            logger.info("Guest user shall not see Admin's personal perspectives");
            
            ui.login(Role.GUEST);
            
            leftNav.goToPerspectives();
            assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(ADMIN_PERSONAL));
            assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(PUBLIC_2_PERSONAL));

            topNav.selectUniverse(UNIVERSE);
            leftNav.goToMapViewPage();
            ui.getPerspectivesControl().expand();
            for (WebElement el : ui.getPerspectivesControl().getListOfPerspectives()) {
                assertFalse(el.getText().contains(PUBLIC_2_PERSONAL), "Guest user should not see admin's personal perspective '" + PUBLIC_2_PERSONAL + " in dropdown");
                assertFalse(el.getText().contains(ADMIN_PERSONAL), "Guest user should not see admin's personal perspective '" + ADMIN_PERSONAL + " in dropdown");
            }

            logger.info("Create personal perspective for Guest");
            leftNav.goToPerspectives();
            ui.getPerspectiveSettings().displayAddPerspectiveDialog();
            ui.getPerspectiveSettings().addMultiLevelPerspective(new String[] {ATTR_3}, GUEST_PERSONAL, true);
            assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(GUEST_PERSONAL));
            assertTrue(ui.getPerspectiveSettings().isDeleteButtonPresent(GUEST_PERSONAL));
            assertTrue(ui.getPerspectiveSettings().isEditLinkPresent(GUEST_PERSONAL));
            assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(GUEST_PERSONAL));

            leftNav.goToMapViewPage();
            ui.getPerspectivesControl().expand();
            namesOfPersonalPerspectives = ui.getPerspectivesControl().getNamesOfPersonalPerspectives();
            assertTrue(namesOfPersonalPerspectives.size() > 0, "There should be at least 1 personal perspective in dropdown: '" + GUEST_PERSONAL + "'");
            assertTrue(namesOfPersonalPerspectives.contains(GUEST_PERSONAL), "There should be the following personal perspective '" + GUEST_PERSONAL + "' in dropdown");
            ui.logout();

            ui.login(Role.ADMIN);
            
            leftNav.goToPerspectives();
            assertFalse(ui.getPerspectiveSettings().isPerspectivePresent(GUEST_PERSONAL), 
                "Admin user shall not see Guest's personal perspective '" + GUEST_PERSONAL + "'");

            logger.info("Admin user shall not see Guest's personal perspective '{}' in the dropdown", GUEST_PERSONAL);
            leftNav.goToMapViewPage();
            ui.getCanvas().waitForUpdate();
            ui.getPerspectivesControl().expand();
            ui.getPerspectivesControl().isPerspectivePresent(GUEST_PERSONAL);

            logger.info("Change personal perspective '{}' back to public", PUBLIC_2_PERSONAL);
            ui.getPerspectivesControl().selectPerspectiveByName(PUBLIC_2_PERSONAL);
            ui.getCanvas().waitForUpdate();
            ui.getPerspectivesControl().getEditIcon().click();
            ui.getModalDialog().waitForModalDialogFadeIn();
            assertFalse(ui.getPerspectiveSettings().isPublicCheckboxChecked());
            ui.getPerspectiveSettings().checkPublicCheckbox();
            ui.getPerspectiveSettings().saveEdit();
            
            leftNav.goToPerspectives();
            assertFalse(ui.getPerspectiveSettings().isPerspectivePersonal(PUBLIC_2_PERSONAL));
            ui.getPerspectiveSettings().deletePerspective(ADMIN_PERSONAL);
            ui.logout();

            ui.login(Role.GUEST);
            
            leftNav.goToPerspectives();
            assertFalse(ui.getPerspectiveSettings().isPerspectivePersonal(PUBLIC_2_PERSONAL));
            ui.getPerspectiveSettings().deletePerspectiveIfExists(GUEST_PERSONAL);
        } catch (Exception e) {
            logger.info("Do cleanup tasks");
            ui.logout();
            deleteGuestUniverse();
            ui.cleanup();
        }
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testCannotMakeDefaultGroupPersonal() throws Exception {
        init();

        leftNav.goToPerspectives();

        String originalDefaultPerspective = ui.getPerspectiveSettings().getDefault();
        logger.info("Remember original default perspective '{}'", originalDefaultPerspective);

        logger.info("Set '{}' as default perspective", EXISTING_PERSPECTIVE_7);
        if (ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(EXISTING_PERSPECTIVE_7)) {
            ui.getPerspectiveSettings().setAsDefault(EXISTING_PERSPECTIVE_7);
        }

        logger.info("It is not possible to make default perspective personal (in Settings)");
        assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(EXISTING_PERSPECTIVE_7));
        ui.getPerspectiveSettings().selectEditLink(EXISTING_PERSPECTIVE_7);
        assertTrue(ui.getPerspectiveSettings().isPublicCheckboxChecked());
        assertFalse(ui.getPerspectiveSettings().isPublicCheckboxEnabled());
        ui.getPerspectiveSettings().closeModalDialog();

        logger.info("It is not possible to make default perspective personal (in dropdown)");
        leftNav.goToMapViewPage();
        ui.getPerspectivesControl().selectPerspectiveByName(EXISTING_PERSPECTIVE_7);
        ui.getCanvas().waitForUpdate();
        ui.getPerspectivesControl().getEditIcon().click();
        assertTrue(ui.getPerspectiveSettings().isPublicCheckboxChecked());
        assertFalse(ui.getPerspectiveSettings().isPublicCheckboxEnabled());
        ui.getPerspectiveSettings().closeModalDialog();

        leftNav.goToPerspectives();
        if (originalDefaultPerspective != null) {
            logger.info("Restoring original default perspective '{}'", originalDefaultPerspective);
            ui.getPerspectiveSettings().setAsDefault(originalDefaultPerspective);
        }

        ui.cleanup();
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testClonePerspective() throws Exception {
        final String ORIG_PERSPECTIVE1 = ATTR_3;
        final String DUPL_PERSPECTIVE1 = "My " + ATTR_3;
        final String ORIG_PERSPECTIVE2 = ATTR_7;
        final String DUPL_PERSPECTIVE2 = "My " + ATTR_7;
        
        ui = getUI();

        createGuestUniverse();

        try {
            ui.login(Role.GUEST);
            leftNav = ui.getLeftNavigationPanel();
            
            logger.info("Clean the list of used perspecives before test");
            leftNav.goToPerspectives();
            ui.getPerspectiveSettings().deletePerspectiveIfExists(DUPL_PERSPECTIVE1);
            ui.getPerspectiveSettings().deletePerspectiveIfExists(DUPL_PERSPECTIVE2);
            
            logger.info("Test that it is possible to duplicate the public perspective '{}'", ORIG_PERSPECTIVE1);
            leftNav.goToMapViewPage();
            ui.getPerspectivesControl().selectPerspectiveByName(ORIG_PERSPECTIVE1);
            ui.getCanvas().waitForUpdate();
            String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();
            ui.getPerspectivesControl().getCloneIcon().click();
            ui.getModalDialog().waitForModalDialogFadeIn();
            assertFalse(ui.getPerspectiveSettings().isPublicCheckboxChecked());
            assertFalse(ui.getPerspectiveSettings().isPublicCheckboxEnabled());

            ui.getPerspectiveSettings().selectItemInLevelMenu(2, ORIG_PERSPECTIVE2);
            WebElement inp = ui.getPerspectiveSettings().getPerspectiveNameInput();
            inp.clear();
            inp.sendKeys(DUPL_PERSPECTIVE1);
            ui.getPerspectiveSettings().saveEdit();

            ui.getCanvas().waitForUpdate();
            ui.getPerspectivesControl().isPerspectiveActive(DUPL_PERSPECTIVE1);
            String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
            assertFalse(Arrays.equals(nodes1, nodes2));

            logger.info("See the newly created personal perspective '{}'", DUPL_PERSPECTIVE1);
            ui.getPerspectivesControl().expand();
            
            assertTrue(ui.getPerspectivesControl().getNamesOfPersonalPerspectives().contains(DUPL_PERSPECTIVE1), 
                "Personal perspective '" + DUPL_PERSPECTIVE1 + "' should be present");
            
            ui.getPerspectivesControl().getEditIcon().click();
            ui.getModalDialog().waitForModalDialogFadeIn();
            ui.getModalDialog().clickButton(DialogButton.CLOSE);
            ui.getModalDialog().waitForModalDialogFadeOut();

            logger.info("Duplicated perspective '{}' is also visible in perspective settings page", DUPL_PERSPECTIVE1);
            leftNav.goToPerspectives();
            assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(DUPL_PERSPECTIVE1), "Perspective '" + DUPL_PERSPECTIVE1 + "' should be personal");
            assertTrue(ui.getPerspectiveSettings().isDeleteButtonPresent(DUPL_PERSPECTIVE1), "Perspective '" + DUPL_PERSPECTIVE1 + "' should be deletable");
            assertTrue(ui.getPerspectiveSettings().isEditLinkPresent(DUPL_PERSPECTIVE1), "Perspective '" + DUPL_PERSPECTIVE1 + "' should be editable");
            assertFalse(ui.getPerspectiveSettings().isSetAsDefaultLinkPresent(DUPL_PERSPECTIVE1), "Perspective '" + DUPL_PERSPECTIVE1 + "' should not be marked as default");

            ui.getPerspectiveSettings().deletePerspective(DUPL_PERSPECTIVE1);

            logger.info("It should be possible to duplicate the perspective '{}' from the Settings page", ORIG_PERSPECTIVE2);
            ui.getPerspectiveSettings().selectDuplicateLink(ORIG_PERSPECTIVE2);
            inp = ui.getPerspectiveSettings().getPerspectiveNameInput();
            inp.clear();
            inp.sendKeys(DUPL_PERSPECTIVE2);
            ui.getPerspectiveSettings().selectItemInLevelMenu(2, ATTR_6);
            ui.getPerspectiveSettings().saveEdit();
            
            assertTrue(ui.getPerspectiveSettings().isPerspectivePersonal(DUPL_PERSPECTIVE2));

            logger.info("Duplicated perspective '{}' is also visible in dropdown", DUPL_PERSPECTIVE2);
            leftNav.goToMapViewPage();
            ui.getCanvas().waitForUpdate();
            ui.getPerspectivesControl().expand();
            List<String> persPerspectiveNames = ui.getPerspectivesControl().getNamesOfPersonalPerspectives();
            assertEquals(persPerspectiveNames.size(), 1, "There should be 1 personal perspective, but in fact there are the following: " + persPerspectiveNames);
            assertTrue(persPerspectiveNames.contains(DUPL_PERSPECTIVE2), "List of personal perspectives should contain '" + DUPL_PERSPECTIVE2 + "'");

            logger.info("It should not be possible to edit or duplicate '{}'", PerspectivesControl.NO_GROUPS_PERSPECTIVE);
            ui.getPerspectivesControl().selectPerspectiveByName(PerspectivesControl.NO_GROUPS_PERSPECTIVE);
            ui.getCanvas().waitForUpdate();
            assertFalse(ui.getPerspectivesControl().isEditIconPresent(), "it should not be possible to edit '" + PerspectivesControl.NO_GROUPS_PERSPECTIVE + "'");
            assertFalse(ui.getPerspectivesControl().isCloneIconPresent(), "it should not be possible to duplicate '" + PerspectivesControl.NO_GROUPS_PERSPECTIVE + "'");

            leftNav.goToPerspectives();
            ui.getPerspectiveSettings().deletePerspectiveIfExists(DUPL_PERSPECTIVE1);
            ui.getPerspectiveSettings().deletePerspectiveIfExists(DUPL_PERSPECTIVE2);
        } catch (Exception e) {
            logger.info("Do cleanup tasks");
            ui.logout();
            deleteGuestUniverse();
            ui.cleanup();
        }
    }

    @Tas(
        testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"),
        size = SizeType.SMALL)
    @Test
    public void testExternalPerspective() throws Exception {
        final String EXTERNAL_PERSPECTIVE = "External test perspective";
        ui = getUI();

        createGuestUniverse();

        try {
            logger.info("Create new Admin's private perspective '{}'", EXTERNAL_PERSPECTIVE);
            ui.login();
            leftNav = ui.getLeftNavigationPanel();
                
            ui.getTimeline().turnOffLiveMode();
            leftNav.goToMapViewPage();
            String[] nodes1 = ui.getCanvas().getArrayOfNodeNames();
            ui.getPerspectivesControl().addPerspective(EXTERNAL_PERSPECTIVE, true);
            ui.getCanvas().waitForUpdate();
            
            assertTrue(ui.getPerspectivesControl().isPerspectiveActive(EXTERNAL_PERSPECTIVE));
            
            String[] nodes2 = ui.getCanvas().getArrayOfNodeNames();
            assertFalse(Arrays.equals(nodes1, nodes2));
            String url = ui.getDriver().getCurrentUrl();
            ui.logout();

            logger.info("Guest user is able to see the external perspective from the link");
            ui.login(Role.GUEST, url, View.MAPVIEW);
            assertTrue(ui.getPerspectivesControl().isPerspectiveActive(EXTERNAL_PERSPECTIVE));
            String[] nodes3 = ui.getCanvas().getArrayOfNodeNames();
            assertTrue(Arrays.equals(nodes2, nodes3));
            assertEquals(ui.getPerspectivesControl().getNamesOfPersonalPerspectives().size(), 0);
            assertFalse(ui.getPerspectivesControl().getNamesOfAllPerspectives().contains(EXTERNAL_PERSPECTIVE), 
                "External perspective '" + EXTERNAL_PERSPECTIVE + "' should not be displayed in the perspective list"); 
            
            ui.getPerspectivesControl().selectPerspectiveByName(EXISTING_PERSPECTIVE_7);
            
            ui.getCanvas().waitForUpdate();
            String[] nodes4 = ui.getCanvas().getArrayOfNodeNames();
            assertFalse(Arrays.equals(nodes3, nodes4));
        } catch (Exception e) {
            logger.info("Do cleanup tasks");
            ui.logout();
            deleteGuestUniverse();
            ui.cleanup();
        }
    }
}
