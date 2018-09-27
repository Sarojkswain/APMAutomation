package com.ca.apm.test.atc.grouping;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.ModalDialog;
import com.ca.apm.test.atc.common.PerspectiveSettings;
import com.ca.apm.test.atc.common.PerspectivesControl;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.UI.View;
import com.ca.apm.test.atc.common.UniverseSettings;
import com.ca.apm.test.atc.common.UrlUtils;

import static org.testng.Assert.assertFalse;

public class TemporaryPerspectiveTest extends UITest {

    private UI ui;

    private static final String TESTING_UNIVERSE_NAME = "temp_perspective_testing_universe";
    private String commonMapUrl;
        
    private void init() throws Exception {
        ui = getUI();
    }
    
    /**
     * Run this just once as the first test case to obtain a common application map URL.
     * This URL can then be modified in consequent test cases.
     * 
     * @throws Exception
     */
    @Test 
    private void testGoToMapAndGetCurrentUrl() throws Exception {
        init();
        
        ui.login();
        LeftNavigationPanel leftNav = ui.getLeftNavigationPanel();

        leftNav.goToUniverses();

        UniverseSettings universeSettings = ui.getUniverseSettings();
        if (!universeSettings.isUniversePresent(TESTING_UNIVERSE_NAME)) {
            universeSettings.createUniverse(TESTING_UNIVERSE_NAME);
            universeSettings.addUser(TESTING_UNIVERSE_NAME, Role.GUEST.getUser(), UI.Permission.read);
        }

        leftNav.goToMapViewPage();
        commonMapUrl = ui.getCurrentUrl();
        ui.logout();

        logger.info("Found out the common map URL: {}", commonMapUrl);
        
        ui.cleanup();
    }
    
    /**
     * Log in as the specified user with an URL bearing a temporary perspective value.
     * 
     * @throws Exception
     */
    private void doTestProcessTemporaryPerspectiveFromUrlAs(Role role, String[] attributes, String[] attributesLocalized) throws Exception {
        init();
        
        String[] attributesQuoted = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            attributesQuoted[i] = "\"" + attributes[i] + "\"";
        }
        
        String attrStringList = StringUtils.join(attributes, ", ");
        String attrQuotedStringList = StringUtils.join(attributesQuoted, ", ");
        
        String attrUrlPart = "[" + attrQuotedStringList + "]";
        String modURL = UrlUtils.getAtcMapUrlWithParamValue(commonMapUrl, "g", attrUrlPart);

        logger.info("Log in as {} with the URL: {}", role.getUser(), modURL);
        ui.login(role, modURL, View.MAPVIEW);
        
        Assert.assertTrue(ui.getLeftNavigationPanel().isViewActive(View.MAPVIEW));
        
        ui.getCanvas().waitForUpdate();
        
        PerspectivesControl control = ui.getPerspectivesControl();
        Assert.assertTrue(control.isPerspectiveActive(attrStringList));
        Assert.assertTrue(control.isCloneIconPresent());
        Assert.assertTrue(control.isActivePerspectiveFaded());
        Assert.assertTrue(control.getActivePerspectiveTooltip().startsWith("Temporary Perspective:"));
        
        assertFalse(ui.getPerspectivesControl().getNamesOfPersonalPerspectives().contains(attrStringList), "Temporary perspective should not be displayed in the personal perspective list");
        
        logger.info("Temporary perspective is not displayed in the public perspective list");
        for (WebElement el : ui.getPerspectivesControl().getListOfPerspectives()) {
            assertFalse(el.getText().contains(attrStringList));
        }
        
        control.clickClonePerspective();
        ModalDialog modalDialog = ui.getModalDialog();
        modalDialog.waitForModalDialogFadeIn();
        
        PerspectiveSettings perspectiveSettings = new PerspectiveSettings(ui);
        List<String> attrNames = perspectiveSettings.getListOfLevelsInEditDialogAsStrings();
        
        for (int i = 0; i < attributesLocalized.length; i++) {
            Assert.assertEquals(attrNames.get(i), attributesLocalized[i]);
        }
    }
    
    /**
     * Log in as Admin with an URL bearing a temporary perspective value.
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods="testGoToMapAndGetCurrentUrl")
    public void testProcessTemporaryPerspectiveFromUrlAsAdmin1() throws Exception {
        String[] attrs = { "applicationName", "hostname" };
        String[] attrsLocalized = { "Application", "Hostname" };
        doTestProcessTemporaryPerspectiveFromUrlAs(Role.ADMIN, attrs, attrsLocalized);
        
        ui.cleanup();
    }
    
    /**
     * Log in as Guest with an URL bearing a temporary perspective value.
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods="testGoToMapAndGetCurrentUrl")
    public void testProcessTemporaryPerspectiveFromUrlAsGuest1() throws Exception {
        String[] attrs = { "applicationName", "hostname" };
        String[] attrsLocalized = { "Application", "Hostname" };
        doTestProcessTemporaryPerspectiveFromUrlAs(Role.GUEST, attrs, attrsLocalized);
        
        ui.cleanup();
    }
    
    /**
     * Log in as Admin with an URL bearing a temporary perspective value.
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods="testGoToMapAndGetCurrentUrl")
    public void testProcessTemporaryPerspectiveFromUrlAsAdmin2() throws Exception {
        String[] attrs = { "applicationName", "xyz-nonexisting" };
        String[] attrsLocalized = { "Application", "xyz-nonexisting" };
        doTestProcessTemporaryPerspectiveFromUrlAs(Role.ADMIN, attrs, attrsLocalized);
        
        ui.cleanup();
    }
    
    /**
     * Log in as Guest with an URL bearing a temporary perspective value.
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods="testGoToMapAndGetCurrentUrl")
    public void testProcessTemporaryPerspectiveFromUrlAsGuest2() throws Exception {
        String[] attrs = { "applicationName", "hostname", "type", "serviceId", "transactionId"};
        String[] attrsLocalized = { "Application", "Hostname", "Type", "Business Service", "Business Transaction" };
        doTestProcessTemporaryPerspectiveFromUrlAs(Role.GUEST, attrs, attrsLocalized);
        
        ui.cleanup();
    }

    /**
     * Deletes the testing universe created for this test only.
     * 
     * Make this method depending on any method that uses the testing universe.
     */
    @Test(dependsOnMethods = {"testProcessTemporaryPerspectiveFromUrlAsGuest1",
            "testProcessTemporaryPerspectiveFromUrlAsGuest2"}, alwaysRun = true)
    public void cleanUpTestingUniverse() throws Exception {
        init();

        ui.login();

        ui.getLeftNavigationPanel().goToUniverses();

        UniverseSettings universeSettings = ui.getUniverseSettings();
        if (universeSettings.isUniversePresent(TESTING_UNIVERSE_NAME)) {
            universeSettings.deleteUniverse(TESTING_UNIVERSE_NAME);
        }

        ui.logout();
        ui.cleanup();
    }
}
