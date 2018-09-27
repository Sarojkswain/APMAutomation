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
package com.ca.apm.test.em.agc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.Browser;
import com.ca.apm.test.atc.common.EnterprisePage;
import com.ca.apm.test.atc.common.FollowersPage;
import com.ca.apm.test.atc.common.SecurityPage;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;

/**
 * This class contains only register steps test methods and is used by AgcRegisterTest.
 * For run one method from this class use testNgSuite located in localdev.
 *
 */
public class RegisterSteps {


    private Browser browser;
    
    private static final String UI_URL_SUFFIX = "/ApmServer/#/perspectives";
    
    public RegisterSteps() {

    }

    public RegisterSteps(Browser browser) {
        this.browser = browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        this.browser = new Browser();
        browser.open();
    }

    @AfterMethod
    public void after(ITestResult testResult) throws Exception {
        if (testResult.getStatus() == ITestResult.FAILURE) {
            testResult.getMethod().getMethodName();
            browser.takeScreenshot(RegisterSteps.class.getSimpleName(), testResult.getMethod()
                .getMethodName(), "FAILURE");
        }
        this.browser.close();
    }

    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void registerTest(String enterpriseTeamCenterUrl, String enterpriseTeamCenterWebviewUrl,
        String followerWebviewUrl, String followerApiUrl) throws Exception {

        UI masterUI = createUI(enterpriseTeamCenterWebviewUrl, true);

        LeftNavigationPanel mLeftNavigation = masterUI.getLeftNavigationPanel();
        mLeftNavigation.goToSecurity();
        
        SecurityPage security = masterUI.getSecurityPage();

        security.clickOnGenerateNewToken();

        Utils.sleep(200);
        security.fillLabel("test token");
        security.selectSystemToken();
        Utils.sleep(200);
        security.submitForm();
        security.waitForNextStep();
        assertTrue("Generating of security token failed.", security.isGeneratedTokenPresent());

        String agcToken = security.getGeneratedToken();

        security.clickOnClose();

        UI followerUI = createUI(followerWebviewUrl, false);

        String masterWindowHandle = browser.getDriver().getWindowHandle();
        followerUI.openUrlInANewTab(null);

        followerUI.login();

        LeftNavigationPanel fLeftNavigation = followerUI.getLeftNavigationPanel();
        fLeftNavigation.goToEnterprise();

        Utils.sleep(5000);
        followerUI.getEnterprisePage().getRegisterLink().click();

        followerUI.getEnterprisePage().waitForRegistrationDialogFadeIn();

        followerUI.getEnterprisePage().fillRegistrationForm(enterpriseTeamCenterUrl,
            enterpriseTeamCenterWebviewUrl, followerWebviewUrl, followerApiUrl, agcToken);

        followerUI.getEnterprisePage().clickTestConfiguration();

        followerUI.getEnterprisePage().waitForNextStep();

        assertTrue("Validating registration of follower failed.", followerUI.getEnterprisePage()
            .isSuccessTestRegistrationMessagePresent());

        followerUI.getEnterprisePage().clickRegister();

        followerUI.getEnterprisePage().waitForNextStep();

        assertTrue("Registration of follower failed.", followerUI.getEnterprisePage()
            .isSuccessRegistrationMessagePresent());

        followerUI.getEnterprisePage().closeDialog();

        followerUI.getEnterprisePage().waitReloadPageAfterSuccessRegistration();

        // followerUI.logout();

        // switch back to master window
        browser.getDriver().switchTo().window(masterWindowHandle);

        mLeftNavigation.goToFollowers();

        FollowersPage followers = masterUI.getFollowersPage();
        Integer row = followers.getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl + " is not REGISTERING.",
            followers.isStatusRegistering(row));

        // masterUI.getRibbon().goToMapTab();
        // masterUI.getRibbon().logout();
    }

    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testCheckFollowerOnline(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        // String followerApiUrl = "http://127.0.0.1:8082";

        UI ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        FollowersPage followers = ui.getFollowersPage();

        Integer row = followers.getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " in followers table on master is not ONLINE.", followers.isStatusOnline(row));

        ui = createUI(followerWebviewUrl, false);

        ui.login();

        ui.getLeftNavigationPanel().goToEnterprise();

        assertTrue("Status of follower " + followerApiUrl + " is not ONLINE.", ui
            .getEnterprisePage().isStatusOnline());

    }

    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testCheckUnregistered(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {
        UI ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        FollowersPage followers = ui.getFollowersPage();

        Integer row = followers.getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " in followers table on master is not UNREGISTERED.",
            followers.isStatusUnregister(row));

        ui = new UI(browser.getDriver(), followerWebviewUrl);

        ui.login();

        try {
            ui.waitUntilVisible(By.id("summary-range-selector"));
        } catch (Exception e) {
            assertTrue("Follower is not in standalone mode. Cannot se map tab on ribbon.", false);
        }
    }

    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testCancelRegistrationFromFollower(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(followerWebviewUrl, true);

        ui.getLeftNavigationPanel().goToEnterprise();

        ui.getEnterprisePage().getCancelRegistrationLink().click();

        Utils.sleep(200);

        WebElement button = ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons();

        button.click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertTrue("Canceling registration failed.", ui.getMultiStepDialog()
            .isSuccessMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        Utils.sleep(200);

        try {
            ui.waitUntilVisible(By.id("universe-selection-combo"));
        } catch (Exception e) {
            assertTrue("Follower is not in standalone mode. Cannot see the active universe drop-down.", false);
        }

        // go to master UI
        ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);

        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Followers record on master is not in UNREGISTERED state.", ui
            .getFollowersPage().isStatusUnregister(row));

    }

    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testCancelRegistrationFromMaster(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);

        assertTrue("Cancel registration link is not present.", ui.getFollowersPage()
            .isCancelRegistrationLinkPresent(row));

        ui.getFollowersPage().getCancelRegistrationLink(row).click();

        Utils.sleep(200);

        WebElement button = ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons();

        button.click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertTrue("Graceful canceling registration failed.", ui.getMultiStepDialog()
            .isSuccessMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        // go to follower UI
        UI followerUI = createUI(followerWebviewUrl, true);

        try {
            followerUI.waitUntilVisible(By.id("universe-selection-combo"));
        } catch (Exception e) {
            assertTrue("Follower is not in standalone mode. Cannot see the active universe drop-down.", false);
        }
    }

    /**
     * Test force deregistration follower from master.
     * Follower is stopped. Followers state in table on master is NOT RESPONDING.
     * Test is running on master.
     * 
     * @param enterpriseTeamCenterUrl
     * @param enterpriseTeamCenterWebviewUrl
     * @param followerWebviewUrl
     * @param followerApiUrl
     * @throws Exception
     */
    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testForceDeregistrationFromMaster(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " is not NOT RESPONDING. Cannot continue in test.", ui.getFollowersPage()
            .isStatusNotResponding(row));

        assertTrue("In follower table by record " + followerApiUrl
            + " is not present deregister link.", ui.getFollowersPage()
            .isDeregisterLinkPresent(row));

        ui.getFollowersPage().getDeregisterLink(row).click();

        Utils.sleep(200);

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertFalse("Deregistration procceded in graceful way. Test failed.", ui
            .getMultiStepDialog().isSuccessMessagePresent());

        assertTrue("Error message is not displayed.", ui.getMultiStepDialog()
            .isErrorMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertTrue("Force deregistration failed.", ui.getMultiStepDialog()
            .isSuccessMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();


        row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("After force deregistration is not status of follower " + followerApiUrl
            + " UNREGISTERED.", ui.getFollowersPage().isStatusUnregister(row));


    }

    /**
     * Test force deregistration from follower.
     * 
     * Follower was deregistered on master. Follower is running.
     * Test is running on follower. Expected state of follower on follower is TOKEN INVALID.
     * 
     * @param enterpriseTeamCenterUrl
     * @param enterpriseTeamCenterWebviewUrl
     * @param followerWebviewUrl
     * @param followerApiUrl
     * @throws Exception
     */
    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testForceDeregistrationFromFollower(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(followerWebviewUrl, false);

        ui.login();

        ui.getLeftNavigationPanel().goToEnterprise();

        assertTrue("Status of follower " + followerApiUrl
            + " is not TOKEN INVALID. Cannot continue test.", ui.getEnterprisePage()
            .isStatusTokenInvalid());

        assertTrue("Deregistration link is not present.", ui.getEnterprisePage()
            .isDeregistrationLinkPresent());

        ui.getEnterprisePage().getDeregistrationLink().click();

        Utils.sleep(200);

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertFalse("Deregistration procceded in graceful way. Test failed.", ui
            .getMultiStepDialog().isSuccessMessagePresent());

        assertTrue("Error message is not displayed.", ui.getMultiStepDialog()
            .isErrorMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertTrue("Force deregistration failed.", ui.getMultiStepDialog()
            .isSuccessMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.waitUntilVisible(
            By.cssSelector(EnterprisePage.CSS_SELECTOR_CANCEL_DEREGISTRATION_LINK));
    }

    /**
     * Test graceful deregistration follower from master.
     * Follower is running. Followers state in table on master is ONLINE.
     * Test is running on master.
     * 
     * @param enterpriseTeamCenterUrl
     * @param enterpriseTeamCenterWebviewUrl
     * @param followerWebviewUrl
     * @param followerApiUrl
     * @throws Exception
     */
    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testDeregistrationFromMaster(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " is not ONLINE. Cannot continue in test.", ui.getFollowersPage().isStatusOnline(row));

        assertTrue("In follower table by record " + followerApiUrl
            + " is not present deregister link.", ui.getFollowersPage()
            .isDeregisterLinkPresent(row));

        ui.getFollowersPage().getDeregisterLink(row).click();

        Utils.sleep(200);

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertTrue("Graceful deregistration failed.", ui.getMultiStepDialog()
            .isSuccessMessagePresent());

        ui = createUI(followerWebviewUrl, true);

        ui.getLeftNavigationPanel().goToEnterprise();

        assertTrue("Status of follower " + followerApiUrl
            + " is not LEAVING and waiting for restart.", ui.getEnterprisePage()
            .isCancelDeregistrationLinkPresent());
    }

    /**
     * Test graceful deregistration from follower.
     * 
     * Follower is running and ONLINE.
     * Test is running on follower.
     * 
     * @param enterpriseTeamCenterUrl
     * @param enterpriseTeamCenterWebviewUrl
     * @param followerWebviewUrl
     * @param followerApiUrl
     * @throws Exception
     */
    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testDeregistrationFromFollower(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(followerWebviewUrl, true);

        ui.getLeftNavigationPanel().goToEnterprise();

        assertTrue(
            "Status of follower " + followerApiUrl + " is not ONLINE. Cannot continue test.", ui
                .getEnterprisePage().isStatusOnline());

        assertTrue("Deregistration link is not present.", ui.getEnterprisePage()
            .isDeregistrationLinkPresent());

        ui.getEnterprisePage().getDeregistrationLink().click();

        Utils.sleep(200);

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        ui.getMultiStepDialog().waitForMultistepOperationIsDone();

        assertTrue("Graceful deregistration failed.", ui.getMultiStepDialog()
            .isSuccessMessagePresent());

        ui.getMultiStepDialog().getOKButtonOnMultistepDialogButtons().click();

        try {
            ui.waitUntilVisible(
                By.cssSelector(EnterprisePage.CSS_SELECTOR_CANCEL_DEREGISTRATION_LINK));
        } catch (Exception e) {
            assertTrue("Status of follower " + followerApiUrl
                + " is not LEAVING and waiting for restart.", false);
        }

        // check status on master

        ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " is not DEREGISTERING. Cannot continue in test.", ui.getFollowersPage()
            .isStatusDeregistering(row));

        assertTrue("In follower table by record " + followerApiUrl
            + " is not present cancel deregister link.", ui.getFollowersPage()
            .isCancelDeregistrationLinkPresent(row));
    }

    /**
     * Test cancel deregistration from follower.
     * Follower is running and is in state leaving (deregistering).
     * 
     * @param enterpriseTeamCenterUrl
     * @param enterpriseTeamCenterWebviewUrl
     * @param followerWebviewUrl
     * @param followerApiUrl
     * @throws Exception
     */
    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testCancelDeregistrationFromFollower(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(followerWebviewUrl, true);

        ui.getLeftNavigationPanel().goToEnterprise();

        assertTrue("Cancel deregistration link is not present.", ui.getEnterprisePage()
            .isCancelDeregistrationLinkPresent());

        ui.getEnterprisePage().getCancelDeregistrationLink().click();

        Utils.sleep(100);

        ui.getProgressDialog().getOKButton().click();

        ui.getProgressDialog().waitForMultistepOperationIsDone();

        assertTrue("Cancel deregistration failed.", ui.getProgressDialog()
            .isSuccessMessagePresent());

        ui.getProgressDialog().getOKButton().click();

        ui.waitUntilVisible(By.cssSelector(EnterprisePage.CSS_SELECTOR_DEREGISTER_LINK));

        assertTrue("Status of follower " + followerApiUrl + " is not ONLINE.", ui
            .getEnterprisePage().isStatusOnline());

        // switch to master and check status

        ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " is not ONLINE. Cannot continue in test.", ui.getFollowersPage().isStatusOnline(row));
    }

    @Parameters({"enterpriseTeamCenterUrl", "enterpriseTeamCenterWebviewUrl", "followerWebviewUrl",
            "followerApiUrl"})
    @Test
    public void testCancelDeregistrationFromMaster(String enterpriseTeamCenterUrl,
        String enterpriseTeamCenterWebviewUrl, String followerWebviewUrl, String followerApiUrl)
        throws Exception {

        UI ui = createUI(enterpriseTeamCenterWebviewUrl, true);

        ui.getLeftNavigationPanel().goToFollowers();

        Integer row = ui.getFollowersPage().getRowByFollowerAPIURL(followerApiUrl);
        assertNotNull("Could not find follower " + followerApiUrl + "  in followers table.", row);
        assertTrue("Status of follower " + followerApiUrl
            + " is not ONLINE. Cannot continue in test.", ui.getFollowersPage()
            .isStatusDeregistering(row));

        assertTrue("In follower table by record " + followerApiUrl
            + " is not present deregister link.", ui.getFollowersPage()
            .isCancelDeregistrationLinkPresent(row));

        ui.getFollowersPage().getCancelDeregistrationLink(row).click();

        Utils.sleep(100);

        ui.getProgressDialog().getOKButton().click();

        ui.getProgressDialog().waitForMultistepOperationIsDone();

        assertTrue("Canceling deregistration failed.", ui.getProgressDialog()
            .isSuccessMessagePresent());

        ui.getProgressDialog().getOKButton().click();
        try {
            ui.getFollowersPage().waitUntilFollowerIsOnline(followerApiUrl, 20);
        } catch (Exception e) {
            assertTrue("Follower " + followerApiUrl + " was not switched to ONLINE mode.", false);
        }

        ui = createUI(followerWebviewUrl, true);

        ui.getLeftNavigationPanel().goToEnterprise();

        assertTrue("Follower " + followerApiUrl + " is not ONLINE on follower side.", ui
            .getEnterprisePage().isStatusOnline());
    }

    private UI createUI(String baseUrl, boolean login) throws Exception {
        UI ui = new UI(browser.getDriver(), baseUrl + UI_URL_SUFFIX);

        if (login) {
            ui.login();
        }
        return ui;
    }
}
