package com.ca.apm.nextgen.tests;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static com.ca.apm.nextgen.tests.common.ManagementElementType.NEW_SUMMARY_ALERT;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_NODE_TEMPLATE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.AQUARIUS_SUMMARY_ALERT_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.CONSOLE_NOTIFICATION_ACTION;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_APPLY_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_NAME_FIELD_INPUT_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_REMOVE_ACTION_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.SUPER_DOMAIN_NODE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.TEST_SAMPLE_MANAGEMENT_MODULE_NAME;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.WaitForCssValueCondition;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * Test suite implementing tests from ALM's Editing Summary Alerts test plan.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class EditingSummaryAlertsTest extends BaseWebViewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditingSummaryAlertsTest.class);

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "EditingSummaryAlerts", "MANAGEMENT_TAB"})
    @AlmId(352589)
    public void testDeleteSummaryAlert() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        testRunner.testDeleteSummaryAlert();
    }
    
    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "EditingSummaryAlerts", "MANAGEMENT_TAB"})
    @AlmId(350805)
    public void testAddAndRemoveAction() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        testRunner.testAddAndRemoveAction();
    }

    public static void main(String[] args) throws Exception {
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, "tas-cz-nbd", 
            8080, "localhost", "", 9515, DesiredCapabilities.chrome());
        testRunner.testDeleteSummaryAlert();
        testRunner.testAddAndRemoveAction();
    }

    private static class TestRunner extends BaseTestRunner {

        /**
         * Constructor.
         * 
         * @param webViewUserName
         * @param webViewUserPassword
         * @param webViewHost
         * @param webViewPort
         * @param hubHost
         * @param hubPath
         * @param hubPort
         * @param desiredCapabilities
         */
        public TestRunner(String webViewUserName, String webViewUserPassword, String webViewHost,
            int webViewPort, String hubHost, String hubPath, int hubPort,
            DesiredCapabilities desiredCapabilities) {
            super(webViewUserName, webViewUserPassword, webViewHost, webViewPort, hubHost, hubPath, hubPort,
                desiredCapabilities);
        }
        
        /**
         * Implements ALM #352589.
         */
        public void testDeleteSummaryAlert() {
            LOGGER.info("Trying deleting a non-active summary alert.");
            doDeleteSummaryAlert(false);
            LOGGER.info("Trying deleting an active summary alert.");
            doDeleteSummaryAlert(true);
        }
        
        /**
         * Implements ALM #350805.
         */
        public void testAddAndRemoveAction() {
            try (WebViewUi ui = createWebViewUi()) {
                login(ui);

                //Go to management tab.
                ui.clickManagementTab();

                String createdAlertName = ManagementTabUtils.createNewManagementElement(ui, 
                    AQUARIUS_SUMMARY_ALERT_NAME, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME,
                    NEW_SUMMARY_ALERT, true, false);

                assertNotNull(createdAlertName);
                assertTrue(!createdAlertName.isEmpty());

                LOGGER.info("Created a new summary alert named: {}", createdAlertName);
                
                //Select *SuperDomain* node in the tree.
                ui.selectTreeNode(SUPER_DOMAIN_NODE);
                
                //Navigate back to the created summary alert node.
                final String newSummaryAlertNodeId = String.format(ALERT_NODE_TEMPLATE, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME, createdAlertName);
                ui.selectTreeNode(newSummaryAlertNodeId);

                //Open up a Danger Action selection dialog.
                WebElement chooseActionDialog = ManagementTabUtils.openChooseActionDialog(ui);
                ManagementTabUtils.chooseAction(ui, chooseActionDialog, 
                    CONSOLE_NOTIFICATION_ACTION, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME);

                WebElement placedActionElement = ManagementTabUtils.findActionElementInActionsList(ui, 
                    CONSOLE_NOTIFICATION_ACTION);
                
                assertNotNull(placedActionElement);
                
                //Save changes by pressing Apply button.
                ui.clickDialogButton(ui.getWebDriver(),
                    By.id(MM_EDITOR_APPLY_BUTTON_ID));

                //Navigate elsewhere.
                ui.selectTreeNode(SUPER_DOMAIN_NODE);

                //Go back to our node.
                ui.selectTreeNode(newSummaryAlertNodeId);
                ui.waitFor(
                    ExpectedConditions.textToBePresentInElementValue(
                        By.id(MM_EDITOR_NAME_FIELD_INPUT_ID), createdAlertName));

                //Check that the selected action is still in the Actions list.
                placedActionElement = ManagementTabUtils.findActionElementInActionsList(ui, 
                    CONSOLE_NOTIFICATION_ACTION);
                assertNotNull(placedActionElement);

                //Select the action.
                ui.getActions()
                    .moveToElement(placedActionElement)
                    .click()
                    .perform();

                //Click Remove button and wait for it.
                By removeButtonSelector = By.id(MM_EDITOR_REMOVE_ACTION_BUTTON_ID);
                ui.clickDialogButton(ui.getWebDriver(), removeButtonSelector);
                ui.waitFor(new WaitForCssValueCondition(ui, removeButtonSelector, "cursor", "default"));

                //Verify the action has been removed from the list.
                placedActionElement = ManagementTabUtils.findActionElementInActionsList(ui, 
                    CONSOLE_NOTIFICATION_ACTION);
                assertNull(placedActionElement);

                //Save changes by pressing Apply button.
                ui.clickDialogButton(ui.getWebDriver(),
                    By.id(MM_EDITOR_APPLY_BUTTON_ID));

                //Navigate elsewhere.
                ui.selectTreeNode(SUPER_DOMAIN_NODE);

                //Go back to our node.
                ui.selectTreeNode(newSummaryAlertNodeId);
                ui.waitFor(
                    ExpectedConditions.textToBePresentInElementValue(
                        By.id(MM_EDITOR_NAME_FIELD_INPUT_ID), createdAlertName));

                //Verify that the previously removed action was really removed.
                placedActionElement = ManagementTabUtils.findActionElementInActionsList(ui, 
                    CONSOLE_NOTIFICATION_ACTION);

                assertNull(placedActionElement);
                
                //Remove the test alert.
                ManagementTabUtils.deleteManagementElement(ui, newSummaryAlertNodeId, true);
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Add & remove summary alert danger action' test failed. Exception: {0}");
            }
        }
        
        private void doDeleteSummaryAlert(boolean active) {
            try (WebViewUi ui = createWebViewUi()) {
                login(ui);

                //Go to management tab.
                ui.clickManagementTab();

                String createdAlertName = ManagementTabUtils.createNewManagementElement(ui, 
                    AQUARIUS_SUMMARY_ALERT_NAME, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME,
                    NEW_SUMMARY_ALERT, true, active);

                assertNotNull(createdAlertName);
                assertTrue(!createdAlertName.isEmpty());
                
                LOGGER.info("Created a new summary alert named: {}", createdAlertName);
                
                //Select *SuperDomain* node in the tree.
                ui.selectTreeNode(SUPER_DOMAIN_NODE);
                
                //Navigate back to the created summary alert node.
                final String newSummaryAlertNodeId = String.format(ALERT_NODE_TEMPLATE, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME, createdAlertName);

                //First click Delete the node but choose No. 
                ManagementTabUtils.deleteManagementElement(ui, newSummaryAlertNodeId, false);

                //This time choose Yes to remove it.
                ManagementTabUtils.deleteManagementElement(ui, newSummaryAlertNodeId, true);
                
                String nodeIdXpath = ManagementTabUtils.getTreeNodeIdXpath(newSummaryAlertNodeId);
                By byNodeIdXpath = By.xpath(nodeIdXpath);
                WebElement alertNodeElement = ui.getWebElementOrNull(byNodeIdXpath);
                if (alertNodeElement != null) {
                    ui.waitFor(ExpectedConditions.stalenessOf(alertNodeElement));    
                }
                
                alertNodeElement = ui.getWebElementOrNull(byNodeIdXpath);
                //Check that it has disappeared.
                assertNull(alertNodeElement,
                    "Expected: deleted. Actual: exists.");
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Delete Summary Alert' test failed. Exception: {0}");
            }

        }
    }
}
