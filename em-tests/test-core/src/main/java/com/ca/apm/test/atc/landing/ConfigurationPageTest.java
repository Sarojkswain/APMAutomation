package com.ca.apm.test.atc.landing;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.*;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.landing.ConfigurationPage;
import com.ca.apm.test.atc.common.landing.LandingPage;
import com.ca.apm.test.atc.common.landing.Tile;
import com.ca.apm.test.atc.common.landing.Tile.ChartType;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class ConfigurationPageTest extends UITest {

    private static final String TESTING_UNIVERSE_NAME = "Universe for testing BV CRUD";
    private static final String TESTING_GUEST_UNIVERSE_NAME = "Universe for testing BVs for Guests";
    private static final String TESTING_BUSINESS_VIEW_NAME = "BV for testing CRUD";
    private static final String GROUPING_ATTR_1 = "Name";
    private static final String GROUPING_ATTR_2 = "Type";
    private static final String GROUPING_ATTR_3 = "Application";
    private static final String GROUPING_ATTR_4 = "Business Service";
    private static final String[] TESTING_GROUPING_ATTRIBUTES = new String[] {GROUPING_ATTR_1 , GROUPING_ATTR_2, GROUPING_ATTR_3, GROUPING_ATTR_4};

    private UI ui;
    private LeftNavigationPanel leftNav;
    private TopNavigationPanel topNav;
    private LandingPage landingPage;

    private void createUniverse(String universeName, boolean grantReadAccessToGuest) throws Exception {
        List<String> names = topNav.getUniverseNames();
        if (!names.contains(universeName)) {
            leftNav.goToMapViewPage();
            topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
            ui.getCanvas().waitForUpdate();
            ui.getFilterBy().add(GROUPING_ATTR_2);
            ui.getFilterBy().getSaveUniverseButton().click();

            ui.getModalDialog().waitForModalDialogFadeIn();
            ui.getModalDialog().getModalDialog().findElement(By.cssSelector("input")).sendKeys(universeName);
            ui.getModalDialog().clickButton(DialogButton.CONTINUE);
            
            leftNav.waitForWorkIndicator(LeftNavigationPanel.SettingsItems.UNIVERSES);
            if (grantReadAccessToGuest) {
                ui.getUniverseSettings().addUser(universeName, Role.GUEST.getUser(), UI.Permission.read);
            }

            logger.info("Go to Home Page");
            leftNav.goToHomePage();
            topNav.selectUniverse(Universe.ALL_MY_UNIVERSES);
        }
    }

    private void init(boolean includeUniverseForGuest) throws Exception {
        ui = getUI();
        ui.login();
        leftNav = ui.getLeftNavigationPanel();
        topNav = ui.getTopNavigationPanel();

        logger.info("Create testing universe(s)");
        createUniverse(TESTING_UNIVERSE_NAME, false);
        if (includeUniverseForGuest) {
            createUniverse(TESTING_GUEST_UNIVERSE_NAME, true);
        }

        logger.info("Wait for tiles to be loaded");
        landingPage = ui.getLandingPage();
        landingPage.waitForTilesToLoad(true);

        logger.info("Removing testing tiles if present");
        boolean testingBusinessViewFound;
        do {
            testingBusinessViewFound = false;
            List<String> tileNames = getTileNames(landingPage.getTiles());
            for (String tileName : tileNames) {
                if (tileName.startsWith(TESTING_BUSINESS_VIEW_NAME)) {
                    testingBusinessViewFound = true;
                    ConfigurationPage configPage = landingPage.editTile(tileName);
                    configPage.getDeleteButton().click();
                    ui.getModalDialog().clickButton(DialogButton.YES);
                    landingPage.waitForTilesToLoad(true);
                    break;
                }
            }
        } while (testingBusinessViewFound);
    }

    private void cleanup() throws Exception {
        logger.info("Delete testing universe");
        leftNav.goToUniverses();
        ui.getUniverseSettings().deleteUniverse(TESTING_UNIVERSE_NAME);
        if (ui.getUniverseSettings().isUniversePresent(TESTING_GUEST_UNIVERSE_NAME)) {
            ui.getUniverseSettings().deleteUniverse(TESTING_GUEST_UNIVERSE_NAME);
        }

        ui.cleanup();
    }

    private List<String> getTileNames(List<Tile> tiles) {
        List<String> names = new ArrayList<>(tiles.size());
        for (Tile tile : tiles) {
            names.add(tile.getName());
        }
        return names;
    }

    @Test(groups = "failing")
    public void testExperienceCRUD() throws Exception {
        init(false);

        logger.info("Create testing Business View");
        ConfigurationPage configPage = landingPage.clickAddNewExperienceIcon();
        
        assertTrue(configPage.getCancelButton().isEnabled());
        assertFalse(configPage.getSaveButton().isEnabled());
        assertFalse(configPage.getDeleteButton().isDisplayed());
        assertEquals(configPage.getExperienceName(), "");
        
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME);
        
        assertTrue(configPage.isPublicCheckboxChecked());
        
        configPage.uncheckPublicCheckbox();
        configPage.selectUniverseByName(TESTING_UNIVERSE_NAME);
        
        assertTrue(configPage.getExperiencesCount() > 0);
        
        configPage.setGroupingAttributes(new String[] {GROUPING_ATTR_1});
        
        assertEquals(configPage.getSelectedChartType(), ChartType.HISTOGRAM);
        
        configPage.selectChartType(ChartType.VOLUME_CHART);
        
        assertTrue(configPage.getCancelButton().isEnabled());
        assertTrue(configPage.getSaveButton().isEnabled());
        assertFalse(configPage.getDeleteButton().isDisplayed());
        
        configPage.waitForPreviewWorkIndicator();
        List<Tile> previewTiles = configPage.getPreviewTiles();
        List<String> previewTileNames = getTileNames(previewTiles);
        for (Tile previewTile : previewTiles) {
            if (previewTile.hasCharts()) {
                assertEquals(previewTile.getChartType(), ChartType.VOLUME_CHART);
            }
        }
        configPage.getSaveButton().click();

        logger.info("Verify the Business View that has just been created");
        landingPage.waitForTilesToLoad();
        
        assertEquals(topNav.getActiveUniverse(), Universe.ALL_MY_UNIVERSES);
        assertEquals(landingPage.getTileByName(TESTING_BUSINESS_VIEW_NAME).getChartType(), ChartType.VOLUME_CHART);
        
        landingPage.drillDownTheTile(TESTING_BUSINESS_VIEW_NAME);
        
        assertEquals(topNav.getActiveUniverse(), TESTING_UNIVERSE_NAME);
        assertEquals(landingPage.getBreadcrumbExperiences().getCustomAttributeName(), GROUPING_ATTR_1);
        
        List<Tile> tiles = landingPage.getTiles();
        List<String> tileNames = getTileNames(tiles);
        Collections.sort(previewTileNames);
        Collections.sort(tileNames);
        
        assertEquals(tileNames.size(), previewTileNames.size());
        
        for (int i = 0; i < tileNames.size(); i++) {
            assertEquals(tileNames.get(i), previewTileNames.get(i));
            assertEquals(tiles.get(i).getChartType(), ChartType.VOLUME_CHART);
            assertFalse(tiles.get(i).hasNextDrilldownLevel());
        }
        
        ui.getBreadcrumb().goHome();
        landingPage.waitForTilesToLoad();
        
        assertEquals(topNav.getActiveUniverse(), Universe.ALL_MY_UNIVERSES);

        logger.info("Edit testing Business View");
        landingPage.editTile(TESTING_BUSINESS_VIEW_NAME);
        
        assertTrue(configPage.getCancelButton().isEnabled());
        assertTrue(configPage.getSaveButton().isEnabled());
        assertTrue(configPage.getDeleteButton().isEnabled());
        assertEquals(configPage.getExperienceName(), TESTING_BUSINESS_VIEW_NAME);
        
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME + " edited");
        
        assertFalse(configPage.isPublicCheckboxChecked());
        
        FilterBy filter = ui.getFilterBy();
        FilterMenu menu = filter.add(GROUPING_ATTR_2);
        menu.expandDropDownMenu();
        menu.uncheckSelectAll();
        List<String> menuOptions = Arrays.asList(menu.getArrayOfMenuItemsNames());
        if (menuOptions.contains("BUSINESSTRANSACTION")) {
            menu.checkMenuOption("BUSINESSTRANSACTION");
        }
        if (menuOptions.contains("APPLICATION_ENTRYPOINT")) {
            menu.checkMenuOption("APPLICATION_ENTRYPOINT");
        }
        
        menu.confirmMenu();
        menu = filter.add(GROUPING_ATTR_1);
        menu.expandDropDownMenu();
        menu.uncheckSelectAll();
        
        String filteredTile = menu.getArrayOfMenuItemsNames()[0];
        menu.checkMenuOption(filteredTile);
        menu.confirmMenu();
        
        String[] groupingAttrs = configPage.getGroupingAttributes();
        System.out.println(Arrays.toString(groupingAttrs));
        
        assertEquals(groupingAttrs.length, 1);
        assertEquals(groupingAttrs[0], GROUPING_ATTR_1);
        
        configPage.setGroupingAttributes(TESTING_GROUPING_ATTRIBUTES);
        
        assertEquals(configPage.getSelectedChartType(), ChartType.VOLUME_CHART);
        
        configPage.selectChartType(ChartType.LINE_CHART);
        configPage.waitForPreviewWorkIndicator();
        previewTiles = configPage.getPreviewTiles();
        previewTileNames = getTileNames(previewTiles);
        for (Tile previewTile : previewTiles) {
            if (previewTile.hasCharts()) {
                assertEquals(previewTile.getChartType(), ChartType.LINE_CHART);
            }
        }
        
        assertEquals(previewTileNames.size(), 1);
        assertEquals(previewTileNames.get(0), filteredTile);
        
        configPage.getSaveButton().click();

        logger.info("Verify the Business View that has just been edited");
        landingPage.waitForTilesToLoad();
        
        assertEquals(topNav.getActiveUniverse(), Universe.ALL_MY_UNIVERSES);
        
        topNav.selectUniverse(TESTING_UNIVERSE_NAME);
        landingPage.waitForTilesToLoad();
        tiles = landingPage.getTiles();
        
        assertEquals(tiles.size(), 1);
        assertEquals(tiles.get(0).getName(), TESTING_BUSINESS_VIEW_NAME + " edited");
        assertEquals(tiles.get(0).getChartType(), ChartType.LINE_CHART);
        landingPage.drillDownTheTile(TESTING_BUSINESS_VIEW_NAME + " edited");
        
        for (String groupingAttribute : TESTING_GROUPING_ATTRIBUTES) {
            assertEquals(landingPage.getBreadcrumbExperiences().getCustomAttributeName(), groupingAttribute);
            tiles = landingPage.getTiles();
            assertEquals(tiles.size(), 1);
            assertEquals(tiles.get(0).getChartType(), ChartType.LINE_CHART);
            if (groupingAttribute.equals(TESTING_GROUPING_ATTRIBUTES[TESTING_GROUPING_ATTRIBUTES.length - 1])) {
                assertFalse(tiles.get(0).hasNextDrilldownLevel());
            } else {
                landingPage.drillDownTheTile(tiles.get(0).getName());
            }
        }
        
        ui.getBreadcrumb().goHome();
        landingPage.waitForTilesToLoad();

        logger.info("Delete testing Business View");
        landingPage.editTile(TESTING_BUSINESS_VIEW_NAME + " edited");
        groupingAttrs = configPage.getGroupingAttributes();
        assertEquals(groupingAttrs.length, TESTING_GROUPING_ATTRIBUTES.length);
        for (int i = 0; i < TESTING_GROUPING_ATTRIBUTES.length; i++) {
            assertEquals(groupingAttrs[i], TESTING_GROUPING_ATTRIBUTES[i]);
        }
        
        assertEquals(filter.getFilterClausesCount(), 1);
        assertEquals(filter.getFilterItemCount(0), 2);
        
        System.out.println(filter.getFilterItemNames());
        configPage.getDeleteButton().click();
        ui.getModalDialog().clickButton(DialogButton.YES);
        landingPage.waitForTilesToLoad(false);
        
        assertEquals(topNav.getActiveUniverse(), TESTING_UNIVERSE_NAME);
        assertEquals(landingPage.getTiles().size(), 0);
     
        cleanup();
    }

    @Test(groups = "failing")
    public void testExperienceAuthorization() throws Exception {
        init(true);

        logger.info("Create testing Business Views");
        ConfigurationPage configPage = landingPage.clickAddNewExperienceIcon();
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME + " 1");
        assertTrue(configPage.isPublicCheckboxChecked());
        assertTrue(configPage.isPublicCheckboxEnabled());
        configPage.selectUniverseByName(TESTING_UNIVERSE_NAME);
        configPage.setGroupingAttributes(new String[] {GROUPING_ATTR_1});
        configPage.getSaveButton().click();
        landingPage.waitForTilesToLoad();

        landingPage.clickAddNewExperienceIcon();
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME + " 2");
        assertTrue(configPage.isPublicCheckboxChecked());
        assertTrue(configPage.isPublicCheckboxEnabled());
        configPage.selectUniverseByName(TESTING_GUEST_UNIVERSE_NAME);
        configPage.setGroupingAttributes(new String[] {GROUPING_ATTR_1});
        configPage.getSaveButton().click();
        landingPage.waitForTilesToLoad();

        landingPage.clickAddNewExperienceIcon();
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME + " 3");
        assertTrue(configPage.isPublicCheckboxChecked());
        assertTrue(configPage.isPublicCheckboxEnabled());
        configPage.uncheckPublicCheckbox();
        configPage.selectUniverseByName(TESTING_GUEST_UNIVERSE_NAME);
        configPage.setGroupingAttributes(new String[] {GROUPING_ATTR_1});
        configPage.getSaveButton().click();
        landingPage.waitForTilesToLoad();

        logger.info("Log in as Guest");
        ui.logout();
        ui.login(Role.GUEST);
        landingPage.waitForTilesToLoad();

        logger.info("Check rights to previously created universes");
        List<String> tileNames = getTileNames(landingPage.getTiles());
        assertFalse(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 1"));
        assertTrue(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 2"));
        assertFalse(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 3"));
        Tile tile = landingPage.getTileByName(TESTING_BUSINESS_VIEW_NAME + " 2");
        tile.openToolsOverlay();
        assertFalse(tile.getEditIcon().isDisplayed());
        
        logger.info("Create a guest card");
        landingPage.clickAddNewExperienceIcon();
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME + " 4");
        assertFalse(configPage.isPublicCheckboxChecked());
        assertFalse(configPage.isPublicCheckboxEnabled());
        configPage.selectUniverseByName(TESTING_GUEST_UNIVERSE_NAME);
        configPage.setGroupingAttributes(new String[] {GROUPING_ATTR_1});
        configPage.getSaveButton().click();
        landingPage.waitForTilesToLoad();

        logger.info("Edit the card");
        landingPage.editTile(TESTING_BUSINESS_VIEW_NAME + " 4");
        assertFalse(configPage.isPublicCheckboxChecked());
        assertFalse(configPage.isPublicCheckboxEnabled());
        assertTrue(configPage.getSaveButton().isEnabled());
        assertTrue(configPage.getDeleteButton().isEnabled());
        assertEquals(configPage.getExperienceName(), TESTING_BUSINESS_VIEW_NAME + " 4");
        configPage.setExperienceName(TESTING_BUSINESS_VIEW_NAME + " 5");
        configPage.getSaveButton().click();
        landingPage.waitForTilesToLoad();
        tileNames = getTileNames(landingPage.getTiles());
        assertTrue(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 5"));

        logger.info("Log in as Admin");
        ui.logout();
        ui.login(Role.ADMIN);
        if (!topNav.getActiveUniverse().equals(Universe.ALL_MY_UNIVERSES)) {
            topNav.selectUniverse(Universe.ALL_MY_UNIVERSES);
        }
        landingPage.waitForTilesToLoad(true);

        logger.info("Check the guest's private universe is not visible");
        tileNames = getTileNames(landingPage.getTiles());
        assertTrue(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 1"));
        assertTrue(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 2"));
        assertTrue(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 3"));
        assertFalse(tileNames.contains(TESTING_BUSINESS_VIEW_NAME + " 5"));

        logger.info("Delete testing Business Views");
        for (int i = 1; i < 4; i++) {
            landingPage.editTile(TESTING_BUSINESS_VIEW_NAME + " " + i);
            configPage.getDeleteButton().click();
            ui.getModalDialog().clickButton(DialogButton.YES);
            landingPage.waitForTilesToLoad(true);
            assertFalse(getTileNames(landingPage.getTiles()).contains(TESTING_BUSINESS_VIEW_NAME + i));
        }

        cleanup();
    }
}
