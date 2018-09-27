package com.ca.apm.test.atc.universe;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.filtersnavigation.MapView;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ActiveUniverseTest extends UITest {

    private static final String TESTING_UNIVERSE_NAME = "e2e_testing_universe";

    private UI ui = null;
    private LeftNavigationPanel leftNav = null;
    private TopNavigationPanel topNav = null;
    
    private void init() {
        ui = getUI();
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();
    }

    @Test
    public void testActiveUniversePermalink() throws Exception {
        init();

        logger.info("logging in as Admin");
        ui.login(Role.ADMIN);
        leftNav.goToMapViewPage();

        logger.info("creating testing universe");
        createTestingUniverse();

        logger.info("go to MapView to be sure to have a defined active view");
        leftNav.goToMapViewPage();
        
        logger.info("selecting testing universe");
        topNav.selectUniverse(TESTING_UNIVERSE_NAME);

        logger.info("logging out and in again with previous url");
        String url = ui.getCurrentUrl();
        ui.logout();
        ui.login(Role.ADMIN, url, View.MAPVIEW);
        
        topNav.getUniverseDropdown().waitWhileEmptyText();

        logger.info("checking that the testing universe is selected");
        Assert.assertEquals(topNav.getActiveUniverse(), TESTING_UNIVERSE_NAME);

        logger.info("clean up");
        deleteTestingUniverse();
        ui.logout();
        ui.cleanup();
    }

    @Test(groups = "failing")
    public void testEverythingUniverseAvailability() throws Exception {
        init();

        logger.info("logging in as Admin");
        ui.login(Role.ADMIN);
        leftNav.goToMapViewPage();

        if (topNav.getUniverseNames().contains(TESTING_UNIVERSE_NAME)) {
            leftNav.goToUniverses();
            deleteTestingUniverse();
            leftNav.goToMapViewPage();
        }

        logger.info("testing that ENTERPRISE universe is available for admins");
        List<String> names = topNav.getUniverseNames();
        Assert.assertTrue(names.contains(Universe.DEFAULT_UNIVERSE));

        logger.info("logging in as guest");
        ui.logout();
        ui.login(Role.GUEST);
        leftNav.goToMapViewPage();

        logger.info("verify universes drop down is not visible");
        Assert.assertFalse(topNav.isUniverseDropdownPresent());
        
        logger.info("verify 'Not assigned universe' message is displayed");
        Assert.assertEquals(ui.getMapviewPage().getErrorDialogTitle(), "Info");
        ui.logout();
        
        logger.info("assign Guest user to testing universe");
        ui.login(Role.ADMIN);
        leftNav.goToMapViewPage();
        createTestingUniverse();
        ui.getUniverseSettings().addUser(TESTING_UNIVERSE_NAME, "Guest", UI.Permission.read);
        ui.logout();
        
        logger.info("EVERYTHING universe is not available for guests");
        ui.login(Role.GUEST);
        leftNav.goToMapViewPage();
        names = topNav.getUniverseNames();
        Assert.assertFalse(names.contains(Universe.DEFAULT_UNIVERSE));

        logger.info("testing universe is available for Guest");
        Assert.assertTrue(names.contains(TESTING_UNIVERSE_NAME));
        Assert.assertFalse(ui.getMapviewPage().getErrorDialog().isDisplayed());
        ui.logout();
        
        logger.info("clean up");
        ui.login();
        leftNav.goToUniverses();
        deleteTestingUniverse();
        ui.logout();
        ui.cleanup();
    }

    @Test
    public void testUniverseFiltersRemembering() throws Exception {
        init();

        logger.info("logging in as Admin");
        ui.login(Role.ADMIN);
        leftNav.goToMapViewPage();

        logger.info("creating testing universe");
        createTestingUniverse();

        logger.info("selecting EVERYTHING universe");
        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);

        logger.info("switching to Map view");
        leftNav.goToMapViewPage();

        logger.info("creating filter 1");
        createFilter1();
        ui.getCanvas().waitForUpdate();
        testFilter1();

        logger.info("selecting testing universe and creating filter 2");
        topNav.selectUniverse(TESTING_UNIVERSE_NAME);
        ui.getCanvas().waitForUpdate();
        createFilter2();
        ui.getCanvas().waitForUpdate();
        testFilter2();

        logger.info("switching to EVERYTHING universe and testing that filter 1 was restored");
        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        ui.getCanvas().waitForUpdate();
        testFilter1();

        logger.info("switching to testing universe and testing that filter 2 was restored");
        topNav.selectUniverse(TESTING_UNIVERSE_NAME);
        ui.getCanvas().waitForUpdate();
        testFilter2();

        logger.info("clean up");
        deleteTestingUniverse();
        ui.logout();
        ui.cleanup();
    }

    private void createFilter1() throws Exception {
        FilterBy filterBy = ui.getFilterBy();
        
        if (filterBy.isPanelOpen()) {
            switch (filterBy.getFilterItemCount()) {
                case 0:
                    filterBy.add("Type");
                    break;
                case 1:
                    // If the TESTING_UNIVERSE_NAME universe has been created by this test,
                    // the Type filter will be remembered for ENTERPRISE universe.
                    Assert.assertEquals(filterBy.getFilterItem(0).getName().toLowerCase(), "type");
                    break;
                default:
                    Assert.fail("Unexpected filters are set.");
            }
        } else {
            filterBy.add("Type");
        }        

        // creating Name filter
        FilterMenu nameFilterItem = filterBy.add("Name");
        nameFilterItem.expandDropDownMenu();
        nameFilterItem.uncheckMenuOption(MapView.NAME_ITEM);
        nameFilterItem.confirmMenu();

        ui.getFilterBy().checkShowEntryElement();
    }

    private void testFilter1() throws Exception {
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 2);

        FilterMenu typeFilterItem = ui.getFilterBy().getFilterItem(0);
        Assert.assertEquals(typeFilterItem.getName().toLowerCase(), "type");
        typeFilterItem.expandDropDownMenu();
        Assert.assertTrue(typeFilterItem.getMenuSelectAll().isCheckboxChecked());
        typeFilterItem.cancelMenu();

        FilterMenu nameFilterItem = ui.getFilterBy().getFilterItem(1);
        Assert.assertEquals(nameFilterItem.getName().toLowerCase(), "name");
        nameFilterItem.expandDropDownMenu();
        Assert.assertFalse(nameFilterItem.getMenuItem(MapView.NAME_ITEM).isCheckboxChecked());
        Assert.assertFalse(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        nameFilterItem.cancelMenu();

        Assert.assertTrue(ui.getFilterBy().isCheckedShowEntryElement());
    }

    private void createFilter2() throws Exception {
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 0);
        ui.getFilterBy().add("Name");

        ui.getFilterBy().uncheckShowEntryElement();
    }

    private void testFilter2() throws Exception {
        Assert.assertEquals(ui.getFilterBy().getFilterItemCount(), 1);

        FilterMenu nameFilterItem = ui.getFilterBy().getFilterItem(0);
        Assert.assertEquals(nameFilterItem.getName().toLowerCase(), "name");
        nameFilterItem.expandDropDownMenu();
        Assert.assertTrue(nameFilterItem.getMenuSelectAll().isCheckboxChecked());
        nameFilterItem.cancelMenu();

        Assert.assertFalse(ui.getFilterBy().isCheckedShowEntryElement());
    }

    private void createTestingUniverse() throws Exception {
        List<String> names = topNav.getUniverseNames();
        if (names.contains(TESTING_UNIVERSE_NAME)) {
            return;
        }

        topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
        ui.getCanvas().waitForUpdate();
        ui.getFilterBy().add("Type");
        ui.getFilterBy().getSaveUniverseButton().click();

        ui.getModalDialog().waitForModalDialogFadeIn();
        ui.getModalDialog().getModalDialog().findElement(By.cssSelector("input"))
            .sendKeys(TESTING_UNIVERSE_NAME);
        ui.getModalDialog().clickButton(DialogButton.CONTINUE);
        ui.getLeftNavigationPanel().waitForWorkIndicator(LeftNavigationPanel.SettingsItems.UNIVERSES);
    }

    private void deleteTestingUniverse() throws Exception {
        leftNav.goToUniverses();
        ui.getUniverseSettings().deleteUniverse(TESTING_UNIVERSE_NAME);
    }

}
