package com.ca.apm.test.atc.landing;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.AboutDialog;
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.TopNavigationPanel;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.Universe;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.apm.test.atc.common.landing.Tile;

public class TopNavigationTest extends UITest {
    
    private final static Logger logger = Logger.getLogger(TopNavigationTest.class);
    
    private final static int ANIMATION_DELAY = 500;
        
    private final String TYPE_PERSPECTIVE = "Type";
    private final String TESTING_UNIVERSE_NAME = "Temporary testing universe";

    private UI ui;
    private TopNavigationPanel topNav;
    private LeftNavigationPanel leftNav;
    
    private void init() throws Exception {
        ui = getUI();
        ui.login();
        
        topNav = ui.getTopNavigationPanel();
        leftNav = ui.getLeftNavigationPanel();
    }
    
    @Test
    /**
     * Test message center element toggle
     */
    public void testMessageCenterElement() throws Exception {
        init();
        
        leftNav.goToHomePage();
        
        WebElement messageCenterToggleOpenControl = ui.getElementProxy(topNav.getMessageCenterControlInClosedStateSelector());
        logger.info("Toggle open the message center");
        messageCenterToggleOpenControl.click();
        
        Utils.sleep(ANIMATION_DELAY);
        
        WebElement messageCenterElement = ui.getElementProxy(topNav.getMessageCenterPopoverContainerSelector());
        Assert.assertTrue(messageCenterElement.isDisplayed());
        
        WebElement messageCenterToggleCloseControl = ui.getElementProxy(topNav.getMessageCenterControlInOpenedStateSelector());
        logger.info("Toggle close the message center");
        messageCenterToggleCloseControl.click();
        
        Utils.sleep(ANIMATION_DELAY);
        
        Assert.assertFalse(messageCenterElement.isDisplayed());
    }
    
    @Test
    public void testSwitchToHomeView() throws Exception {
        init();

        // Navigate away from home to have the link in dropdown list
        leftNav.goToMapViewPage();

        leftNav.goToHomePage();

        Assert.assertTrue(ui.getDriver().getCurrentUrl().contains(View.HOMEPAGE.getCharacteristicUriPart()));
        Assert.assertFalse(ui.getDriver().getCurrentUrl().contains(View.NOTEBOOK.getCharacteristicUriPart()));
    }
    
    @Test
    public void testSwitchToDashboardView() throws Exception {
        init();

        // Navigate away from dashboard to have the link in dropdown list
        leftNav.goToMapViewPage();

        leftNav.goToDashboardPage();

        Assert.assertTrue(ui.getDriver().getCurrentUrl().contains(View.DASHBOARD.getCharacteristicUriPart()));
    }
    
    @Test
    public void testSwitchToMapView() throws Exception {
        init();

        // Navigate away from map to have the link in dropdown list
        leftNav.goToHomePage();

        leftNav.goToMapViewPage();

        Assert.assertTrue(ui.getDriver().getCurrentUrl().contains(View.MAPVIEW.getCharacteristicUriPart()));
    }
    
    @Test
    public void testBreadcrumbPersistency() throws Exception {
        init();

        ui.getLandingPage().waitForTilesToLoad(true);
        // Drill down 2 levels
        Tile tile = ui.getLandingPage().getTiles().get(0);
        tile.drillDown();
        
        tile = ui.getLandingPage().getTiles().get(0);
        tile.openNotebook();
        
        ui.getBreadcrumb().waitForLoad();

        List<PageElement> persistedBreadcrumbs = ui.getLandingPage().getSummaryPanel().getBreadcrumbLevelElements();
        
        leftNav.goToMapViewPage();
        leftNav.goToHomePage();
        
        ui.getBreadcrumb().waitForLoad();

        List<PageElement> restoredBreadcrumbs = ui.getLandingPage().getSummaryPanel().getBreadcrumbLevelElements();

        Assert.assertEquals(persistedBreadcrumbs.size(), restoredBreadcrumbs.size(), "Breadcrumbs are not restored properly on view change");
    }
    
    /** 
     * Test of preserving the selected Universe and Perspective when switching from Map to Homepage and back to Map.
     * 
     * @throws Exception
     */
    @Test
    public void testPreservingPerspectiveAndUniverseOnViewChange() throws Exception {
        init();
        
        logger.info("Navigate to Map view");
        leftNav.goToMapViewPage();
        
        logger.info("Create testing universe");
        List<String> names = topNav.getUniverseNames();
        if (!names.contains(TESTING_UNIVERSE_NAME)) {
            topNav.selectUniverse(Universe.DEFAULT_UNIVERSE);
            ui.getCanvas().waitForUpdate();
            ui.getFilterBy().add("Type");
            ui.getFilterBy().getSaveUniverseButton().click();

            ui.getModalDialog().waitForModalDialogFadeIn();
            ui.getModalDialog().getModalDialog().findElement(By.cssSelector("input"))
                .sendKeys(TESTING_UNIVERSE_NAME);
            ui.getModalDialog().clickButton(DialogButton.CONTINUE);
            this.leftNav.waitForWorkIndicator(LeftNavigationPanel.SettingsItems.UNIVERSES);
            leftNav.goToMapViewPage();
        }

        PerspectivesControl perspectivesControl = ui.getPerspectivesControl();
        Universe universe = topNav.getUniverse();

        logger.info("Select Type perspective");
        perspectivesControl.selectPerspectiveByName(TYPE_PERSPECTIVE);
        
        logger.info("Set default perspective");
        universe.selectUniverse(TESTING_UNIVERSE_NAME);
        
        logger.info("Navigate away from Map view and back");
        leftNav.goToHomePage();
        leftNav.goToMapViewPage();
        
        logger.info("Assert the selected universe name did not change");
        Assert.assertEquals(universe.getActiveUniverseName(), TESTING_UNIVERSE_NAME, "Universe must not change on view switch!");
        logger.info("Assert the active perspective name did not change");
        Assert.assertEquals(perspectivesControl.getActivePerspectiveName(), TYPE_PERSPECTIVE, "Perspective setting must not change on view switch!");

        logger.info("Delete testing universe");
        leftNav.goToUniverses();
        ui.getUniverseSettings().deleteUniverse(TESTING_UNIVERSE_NAME);
    }
    
    /** 
     * Test of preserving the selected Filter, Universe and Perspective when switching from Map to Dashboard and back to Map.
     * 
     * @throws Exception
     */
    @Test
    public void testPreservingFilterAndPerspectiveAndUniversePersistencyOnViewChange() throws Exception {
        init();
        
        logger.info("Navigate to Map view");
        leftNav.goToMapViewPage();

        FilterBy filterBy = ui.getFilterBy();
        PerspectivesControl perspectivesControl = ui.getPerspectivesControl();
        Universe universe = topNav.getUniverse();
        String firstFilterName, secondFilterName;

        logger.info("Set default universe");
        universe.selectUniverse(Universe.DEFAULT_UNIVERSE);
        
        logger.info("Add two filters");
        List<String> addFilterItems = filterBy.getAddFilterItemNames();
        logger.info("Filter items available: " + addFilterItems);
        
        Assert.assertTrue(addFilterItems.size() > 1, "There are not enough attribute names to filter by in the Add Filter drop down. This test case needs at least 2.");
        firstFilterName = addFilterItems.get(0);
        secondFilterName = addFilterItems.get(1);
        
        filterBy.expandFilterByMenu(0);
        filterBy.add(firstFilterName);
        filterBy.add(secondFilterName);
        
        logger.info("Select Type perspective");
        perspectivesControl.selectPerspectiveByName(TYPE_PERSPECTIVE);
        
        logger.info("Navigate away from Map view and back");
        leftNav.goToDashboardPage();
        leftNav.goToMapViewPage();
        
        logger.info("Assert the selected universe name did not change");
        Assert.assertEquals(universe.getActiveUniverseName(), Universe.DEFAULT_UNIVERSE, "Universe must not change on view switch!");
        logger.info("Assert the active perspective name did not change");
        Assert.assertEquals(perspectivesControl.getActivePerspectiveName(), TYPE_PERSPECTIVE, "Perspective setting must not change on view switch!");
        
        List<String> filterItemNames = filterBy.getFilterItemNames();
        
        logger.info("Assert that number of filters did not change");
        Assert.assertEquals(filterItemNames.size(), 2, "Filters setting must not change on view switch!");
        
        logger.info("Assert the filters names did not change");
        Assert.assertEquals(filterItemNames.get(0), firstFilterName, "Filters setting must not change on view switch!");
        Assert.assertEquals(filterItemNames.get(1), secondFilterName, "Filters setting must not change on view switch!");
    }
    
    @Test
    public void testBranding() throws Exception {
        init();
        
        logger.info("Test logo presence");
        WebElement logo = topNav.getLogoElement();
        Assert.assertNotNull(logo, "CA logo not found");
        
        WebElement logoImage = topNav.getLogoImageElement();
        Assert.assertNotNull(logoImage, "CA logo image not found");
        Assert.assertTrue(logoImage.getAttribute("src").contains("CA_Logo"));
        
        logger.info("Test branding name");
        Assert.assertEquals(topNav.getBrandingText(), "Application Performance Management", "CA product name does not match, has it changed?");
    }
    
    @Test
    public void testAboutDialog() throws Exception {
        init(); 
        
        AboutDialog aboutDialog = leftNav.openAboutDialog();
        
        Assert.assertTrue(aboutDialog.isDisplayed(), "About dialog was not displayed");
        Assert.assertNotNull(aboutDialog.getEMInfoElement(), "EM version info not found in About dialog");
        Assert.assertNotNull(aboutDialog.getWebviewInfoElement(), "Webview version info not found in About dialog");
        Assert.assertNotNull(aboutDialog.getSupportLinkElement(), "Support link not found in About dialog");
        Assert.assertNotNull(aboutDialog.getCopyrightInfoElement(), "Copyright message not found in About dialog");
    }
        
    @Test
    public void testUserLogout() throws Exception {
        init();
        
        logger.info("Test logout");

        topNav.getLogoutLinkElement().click();
        
        // Let the logout process
        Utils.sleep(100);
        
        Assert.assertTrue(ui.getElementProxy(By.id("LoginFrame")).isDisplayed(), "Login form not found");
    }
}
