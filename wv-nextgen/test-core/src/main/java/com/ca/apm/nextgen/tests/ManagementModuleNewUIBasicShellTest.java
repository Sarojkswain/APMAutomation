package com.ca.apm.nextgen.tests;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.AGENT_CONNECTION_STATUS_ALERT_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_ALERT_MESSAGE_BOX_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_MESSAGE_BOX_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_MESSAGE_BOX_MESSAGE_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_MESSAGE_BOX_OK_BUTTON_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.ALERT_NODE_TEMPLATE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_NAME_FIELD_INPUT_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.SIMPLE_ALERT_MUST_SPECIFY_NON_EMPTY_NAME_ALERT_MESSAGE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.FIELD_MUST_HAVE_A_VALUE_ALERT_MESSAGE;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.TEST_SAMPLE_MANAGEMENT_MODULE_NAME;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.DANGER_THRESHOLD_TEXT_INPUT_ID;
import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.CAUTION_THRESHOLD_TEXT_INPUT_ID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvConstants;
import com.ca.apm.nextgen.tests.common.WvManagementTabConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * Test suite implementing tests from ALM's Management Module NewUI Basic Shell Tests.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ManagementModuleNewUIBasicShellTest extends BaseWebViewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementModuleNewUIBasicShellTest.class);

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MMNewUIBasicShell", "MANAGEMENT_TAB"})
    @AlmId(354367)
    public void testSavingAlertWithBlankNameShouldFail() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        testRunner.testSavingBlankNameAlertShouldFail();
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "sinal04",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MMNewUIBasicShell", "MANAGEMENT_TAB"})
    @AlmId(352683)
    public void testSavingAlertWithBlankThresholdValueShouldFail() throws Exception {
        String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);

        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, webViewHost,
            8080, hubHost, WebViewUi.HUB_PATH, WebViewUi.HUB_PORT, prepareDesiredCapabilities());
        testRunner.testSavingBlankThresholdValueAlertShouldFail();
    }

    public static void main(String[] args) throws Exception {
        TestRunner testRunner = new TestRunner(WvConstants.ADMIN_USER, WvConstants.ADMIN_USER_PASSWORD, "tas-cz-n17", 
            8080, "localhost", "", 9515, DesiredCapabilities.chrome());
        testRunner.testSavingBlankNameAlertShouldFail();
        testRunner.testSavingBlankThresholdValueAlertShouldFail();
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
         * Implements ALM #354367 test case.
         */
        public void testSavingBlankNameAlertShouldFail() {
            try (WebViewUi ui = createWebViewUi()) {
                //Log into WebView.
                login(ui);

                //Go to management tab.
                ui.clickManagementTab();
                
                final String agentConnectionStatusAlertId = String.format(ALERT_NODE_TEMPLATE, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME, AGENT_CONNECTION_STATUS_ALERT_NAME);
                ui.selectTreeNode(agentConnectionStatusAlertId);

                WebElement alertNameTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_EDITOR_NAME_FIELD_INPUT_ID)));
                
                assertNotNull(alertNameTextField);
                String alertName = alertNameTextField.getAttribute("value");
                assertEquals(alertName, AGENT_CONNECTION_STATUS_ALERT_NAME);

                //Clear the name field and type some dummy text and then clear it again 
                //so that the Apply button gets enabled. 
                ui.clearAndSetInputField(alertNameTextField, "123");
                alertNameTextField.clear();
                
                //Save changes by pressing Apply button.
                ui.clickDialogButton(ui.getWebDriver(),
                    By.id(WvManagementTabConstants.MM_EDITOR_APPLY_BUTTON_ID));

                WebElement alertBoxElement = ui.waitForWebElement(By.id(ALERT_MESSAGE_BOX_ID));
                WebElement alertMessageElement = ui.getWebElement(alertBoxElement, By.id(ALERT_MESSAGE_BOX_MESSAGE_ID));
                assertNotNull(alertMessageElement);
                
                //We should not be allowed to save alerts with blank names.
                String alertText = alertMessageElement.getText();
                assertEquals(alertText, SIMPLE_ALERT_MUST_SPECIFY_NON_EMPTY_NAME_ALERT_MESSAGE);
                
                ui.clickDialogButton(alertBoxElement, By.id(ALERT_MESSAGE_BOX_OK_BUTTON_ID));
                ui.waitFor(ExpectedConditions.stalenessOf(alertBoxElement));

                //Navigate away and then back and see if the alert name did not really change.
                
                //Select *SuperDomain* node in the tree.
                ManagementTabUtils.clickOnNavigationTreeNode(ui, WvManagementTabConstants.SUPER_DOMAIN_NODE);
                ManagementTabUtils.clickOnNavigationTreeNode(ui, agentConnectionStatusAlertId);
                
                alertNameTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_EDITOR_NAME_FIELD_INPUT_ID)));
                
                assertNotNull(alertNameTextField);
                alertName = alertNameTextField.getAttribute("value");
                assertEquals(alertName, AGENT_CONNECTION_STATUS_ALERT_NAME);
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Save alert with a blank name' test failed. Exception: {0}");
            }
        }
        
        /**
         * Implements ALM #352683 test case.
         */
        public void testSavingBlankThresholdValueAlertShouldFail() {
            try (WebViewUi ui = createWebViewUi()) {
                //Log into WebView.
                login(ui);

                //Go to management tab.
                ui.clickManagementTab();
                
                final String agentConnectionStatusAlertId = String.format(ALERT_NODE_TEMPLATE, 
                    TEST_SAMPLE_MANAGEMENT_MODULE_NAME, AGENT_CONNECTION_STATUS_ALERT_NAME);
                ui.selectTreeNode(agentConnectionStatusAlertId);

                WebElement alertNameTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_EDITOR_NAME_FIELD_INPUT_ID)));
                
                assertNotNull(alertNameTextField);
                String alertName = alertNameTextField.getAttribute("value");
                assertEquals(alertName, AGENT_CONNECTION_STATUS_ALERT_NAME);

                testSavingEmptyThresholdValue(ui, agentConnectionStatusAlertId, 
                    AGENT_CONNECTION_STATUS_ALERT_NAME, CAUTION_THRESHOLD_TEXT_INPUT_ID);
                testSavingEmptyThresholdValue(ui, agentConnectionStatusAlertId, 
                    AGENT_CONNECTION_STATUS_ALERT_NAME, DANGER_THRESHOLD_TEXT_INPUT_ID);
            } catch (Throwable e) {
                throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, 
                    "'Save alert with a blank threshold value' test failed. Exception: {0}");
            }
        }
        
        private void testSavingEmptyThresholdValue(WebViewUi ui, String alertId, String alertName, String thresholdFieldId) {
            //Get the threshold field element.
            WebElement thresholdTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(thresholdFieldId)));
            assertNotNull(thresholdTextField);
            
            //Remember the original threshold value.
            String thresholdValue = thresholdTextField.getAttribute("value");
            
            //Clear the threshold field value. "123" is just a dummy text which activates dirty state on the field.
            //We immediately remove it anyway.
            ui.clearAndSetInputField(thresholdTextField, "123");
            thresholdTextField.clear();
            
            //Save changes by pressing Apply button.
            ui.clickDialogButton(ui.getWebDriver(),
                By.id(WvManagementTabConstants.MM_EDITOR_APPLY_BUTTON_ID));

            WebElement alertBoxElement = ui.waitForWebElement(By.id(MM_EDITOR_ALERT_MESSAGE_BOX_ID));
            WebElement alertMessageElement = ui.getWebElement(alertBoxElement, By.id(ALERT_MESSAGE_BOX_MESSAGE_ID));
            assertNotNull(alertMessageElement);
            
            //We should not be allowed to save alerts with blank threshold values.
            String alertText = alertMessageElement.getText();
            assertEquals(alertText, FIELD_MUST_HAVE_A_VALUE_ALERT_MESSAGE);
            
            ui.clickDialogButton(alertBoxElement, By.id(ALERT_MESSAGE_BOX_OK_BUTTON_ID));
            ui.waitFor(ExpectedConditions.stalenessOf(alertBoxElement));

            //Navigate away and then back and see if the alert threshold value did not really change.
            
            //Select *SuperDomain* node in the tree.
            ManagementTabUtils.clickOnNavigationTreeNode(ui, WvManagementTabConstants.SUPER_DOMAIN_NODE);
            ManagementTabUtils.clickOnNavigationTreeNode(ui, alertId);
            
            WebElement alertNameTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MM_EDITOR_NAME_FIELD_INPUT_ID)));
            
            assertNotNull(alertNameTextField);
            String alertElemName = alertNameTextField.getAttribute("value");
            assertEquals(alertElemName, alertName);
            
            thresholdTextField = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(thresholdFieldId)));
            assertNotNull(thresholdTextField);
            
            String thresholdValueToCheck = thresholdTextField.getAttribute("value");
            assertEquals(thresholdValueToCheck, thresholdValue);
        }
    }
}
