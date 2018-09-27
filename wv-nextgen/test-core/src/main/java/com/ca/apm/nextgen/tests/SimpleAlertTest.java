package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvManagementTabConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.TableUi;
import com.ca.apm.nextgen.tests.helpers.TableUi.TableRowRecord;
import com.ca.apm.nextgen.tests.helpers.WaitForCssValueCondition;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.nextgen.tests.common.WvManagementTabConstants.MM_EDITOR_ACTIVE_CECK_BOX_NAME;
import static com.ca.apm.nextgen.tests.SmtpTest.SEND_EMAIL_TREE_NODE;
import static com.ca.apm.nextgen.tests.SmtpTest.WEBVIEW_PASSWORD;
import static com.ca.apm.nextgen.tests.SmtpTest.WEBVIEW_USER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author haiva01
 */
public class SimpleAlertTest extends BaseWebViewTest {
    private static final Logger log = LoggerFactory.getLogger(SimpleAlertTest.class);
    private static final String SIMPLE_ALERT_NAME = WvManagementTabConstants.AGENT_CONNECTION_STATUS_ALERT_NAME;
    private static final String CONSOLE_NOTIFICATION_ACTION = "Console Notification Action";
    private static final String TEST_MM_NAME = "TestSample";
    private static final String TEST_MM_PATH = "management-tree_*SuperDomain*|Management Modules|"
        + TEST_MM_NAME;
    private static final String TEST_NODE = TEST_MM_PATH + "|Alerts|" + SIMPLE_ALERT_NAME;
    private static final String WEBVIEW_ALERT_NAME_INPUT_ID
        = "webview-mmEditor-mmeditorContent-name-field-input";
    private static final String WEBVIEW_DELETE_BUTTON_ID
        = "webview-MMEditor-buttonBar-delete-button";

    private static void pickAction(WebViewUi ui, WebElement chooseActionDialog, String actionName,
        String mmName) {
        try {
            // Parse table and find desired row.

            TableUi tableUi = new TableUi(ui, chooseActionDialog,
                By.xpath(".//div[@id='webview-MMEditor-aEditor-cAction-Construct-Grid']"));
            Map<String, Integer> headersMap = tableUi.getHeaderTextToIndexMap();
            final int mmIndex = headersMap.get("Management Module");
            final int nameIndex = headersMap.get("Name");
            WebElement alertCellElement = null;
            for (int i = 0, rowCount = tableUi.rowCount(); i != rowCount; ++i) {
                List<String> rowValues = tableUi.getRowValues(i);
                if (StringUtils.trim(rowValues.get(nameIndex)).equals(actionName)
                    && rowValues.get(mmIndex).equals(mmName)) {
                    alertCellElement = tableUi.getCellElement(i, nameIndex);
                    break;
                }
            }
            Assert.assertNotNull(alertCellElement);

            // Choose the row.

            WebElement cellTextElement = ui.getWebElement(alertCellElement,
                By.xpath(".//div[text()]"));
            ui.getActions()
                .moveToElement(cellTextElement)
                .click()
                .perform();

            // Click Choose dialog button.

            ui.clickDialogButton(chooseActionDialog,
                By.id("webview-MMEditor-aEditor-Choose-Button"));

            // Wait for it to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(chooseActionDialog));
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to pick action ''{1}'' from Choose Action table. Exception: {0}",
                actionName);
        }
    }

    private static WebElement verifyActionInActionsList(WebViewUi ui,
        SearchContext actionsContainer, String actionName) {
        try {
            TableUi tableUi = new TableUi(ui, actionsContainer,
                By.id("webview-MMEditor-aEditor-danger-actions-Grid"));
            WebElement actionNameElement = null;
            for (TableRowRecord rowRec : tableUi) {
                List<String> rowValues = rowRec.getValues();
                assertEquals(rowValues.size(), 2);
                if (rowValues.get(1).equals(actionName)) {
                    log.info("Found row with \"{}\" action.", rowValues.get(1));
                    actionNameElement = rowRec.getValuesElements().get(1);
                    break;
                }
            }
            assertNotNull(actionNameElement);
            return actionNameElement;
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to find action ''{1}'' in actions list. Exception: {0}", actionName);
        }
    }

    private static void verifyActionNotInActionsList(WebViewUi ui,
        SearchContext actionsContainer, String actionName) {
        try {
            TableUi tableUi = new TableUi(ui, actionsContainer,
                By.id("webview-MMEditor-aEditor-danger-actions-Grid"));
            WebElement actionNameElement = null;
            for (TableRowRecord rowRec : tableUi) {
                List<String> rowValues = rowRec.getValues();
                if (rowValues.size() == 2 && rowValues.get(1).equals(actionName)) {
                    log.info("Found row with \"{}\" action.", rowValues.get(1));
                    actionNameElement = rowRec.getValuesElements().get(1);
                    break;
                }
            }
            assertNull(actionNameElement);
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to find action ''{1}'' in actions list. Exception: {0}", actionName);
        }
    }

    @NotNull
    private static String createNewSimpleAlert(WebViewUi ui, String mmName, String alertName,
        boolean forceUniqueness) {
        // Click Elements button.

        ui.clickDialogButton(ui.getWebDriver(),
            By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID));

        // Wait for menu to appear.

        WebElement elementsMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(WvManagementTabConstants.ELEMENTS_MENU_DROP_DOWN_ID)));

        // Click New Alert button in the menu.

        ui.moveToMenuButton(elementsMenu,
            By.id(WvManagementTabConstants.ELEMENTS_MENU_NEW_ALERT_ID));

        // Wait for new actions menu to appear.
        WebElement newAlertMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(WvManagementTabConstants.ELEMENTS_MENU_NEW_ALERT_SUMBEMNU_ID)));

        // Click New Simple Alert in the menu.

        ui.clickMenuButton(newAlertMenu, By.id("webview-MMEditor-newSimpleAlert-menuItem"));

        // Wait for dialog to appear.

        WebElement newAlertDialog = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("webview-elementCreationPanel-verticalLayout-layout")));

        // Fill the new action dialog.

        fillNewAlertDialog(ui, newAlertDialog, alertName, mmName, forceUniqueness);

        // Wait for the action to be created and to render.

        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id("webview-mmEditor-mmeditorContent-name-field-input"), alertName));

        return alertName;
    }

    private static void fillNewAlertDialog(WebViewUi ui, WebElement newActionDialog,
        String alertName, String mmName, boolean forceUniqueness) {
        // Fill the dialog.

        ManagementTabUtils
            .fillNewElementDialog(ui, newActionDialog, alertName, mmName, forceUniqueness);

        // Click OK button.

        ui.clickDialogButton(ui.getWebDriver(), By.id("webview-mmEditor-Element-OK-Button"));

        // Wait for the dialog to disappear.

        ui.waitFor(ExpectedConditions.stalenessOf(newActionDialog));
    }

    private static void verifySimpleAlertDeletion(WebViewUi ui, boolean activeAlert) {
        // Create unique simple alert that we can test on.

        final String alertName = "Simple Alert " + UUID.randomUUID().toString();
        createNewSimpleAlert(ui, TEST_MM_NAME, alertName, false);

        // Select alert node to test on.

        final String alertNodePath = TEST_MM_PATH + "|Alerts|" + alertName;
        ui.selectTreeNode(alertNodePath);
        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id(WEBVIEW_ALERT_NAME_INPUT_ID), alertName));

        // Activate the alert if necessary.

        if (activeAlert) {
            // Click checkbox.

            WebElement activeCheckBox = ui
                .getWebElement(By.name(MM_EDITOR_ACTIVE_CECK_BOX_NAME));
            ui.checkCheckBox(activeCheckBox);

            clickApply(ui);
        }

        // Click the Delete button.

        final By deleteButtonSelector = By.id(WEBVIEW_DELETE_BUTTON_ID);
        ui.clickDialogButton(ui.getWebDriver(), deleteButtonSelector);

        // Wait for confirmation dialog to render.

        WebElement confirmDialog = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
            By.id("webview-Common-ConfirmMessageBox")));

        // Click No.

        ui.clickDialogButton(confirmDialog,
            By.id("webview-Common-ConfirmMessageBox-button-no"));
        ui.waitFor(ExpectedConditions.stalenessOf(confirmDialog));

        // Navigate again to the same alert to make sure it is present.

        WebElement treeNode = ui.selectTreeNode(alertNodePath);
        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id(WEBVIEW_ALERT_NAME_INPUT_ID), alertName));

        // Click the Delete button.

        ui.clickDialogButton(ui.getWebDriver(), deleteButtonSelector);

        // Wait for confirmation dialog to render.

        confirmDialog = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
            By.id("webview-Common-ConfirmMessageBox")));

        // Click Yes.

        ui.clickDialogButton(confirmDialog,
            By.id("webview-Common-ConfirmMessageBox-button-yes"));
        ui.waitFor(ExpectedConditions.stalenessOf(confirmDialog));
        ui.waitFor(ExpectedConditions.stalenessOf(treeNode));

        // Check that it has disappeared.

        assertNull(ui.getWebElementOrNull(
            By.xpath(String.format(Locale.US, "//div[@ftid='%s']", alertNodePath))),
            "unexpectedly found node that should have been deleted");
    }

    private static void clickApply(WebViewUi ui) {
        // Click the Apply button.

        final By applyButtonSelector = By.id("webview-MMEditor-buttonBar-apply-button");
        ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

        // Wait for the apply button to turn disabled as indication that the changes have
        // been saved. Disabled button has default pointer.

        ui.waitFor(
            new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));
    }

    private static void clickApplyNoWait(WebViewUi ui) {
        // Click the Apply button.

        final By applyButtonSelector = By.id("webview-MMEditor-buttonBar-apply-button");
        ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);
    }

    private static void verifySimpleAlertRenaming(WebViewUi ui, boolean activeAlert) {
        // Create unique simple alert that we can test on.

        final String alertName = "Simple Alert " + UUID.randomUUID().toString();
        createNewSimpleAlert(ui, TEST_MM_NAME, alertName, false);

        // Select alert node to test on.

        final String alertNodeBasePath = TEST_MM_PATH + "|Alerts|";
        ui.selectTreeNode(alertNodeBasePath + alertName);
        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id(WEBVIEW_ALERT_NAME_INPUT_ID), alertName));

        // Change the name of the alert.

        final String aaName = "aaa" + alertName;
        WebElement alertNameElement = ui.getWebElement(By.id(WEBVIEW_ALERT_NAME_INPUT_ID));
        ui.clearAndSetInputField(alertNameElement, aaName);

        // Activate the alert if necessary.

        if (activeAlert) {
            // Click checkbox.

            WebElement activeCheckBox = ui
                .getWebElement(By.name(MM_EDITOR_ACTIVE_CECK_BOX_NAME));
            ui.checkCheckBox(activeCheckBox);
        }

        // Click the Apply button.

        final By applyButtonSelector = By.id("webview-MMEditor-buttonBar-apply-button");
        ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

        // Wait for the apply button to turn disabled as indication that the changes have
        // been saved. Disabled button has default pointer.

        ui.waitFor(
            new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));

        // Navigate again to the same alert to make sure it is present.

        ui.selectTreeNode(alertNodeBasePath + aaName);
        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id(WEBVIEW_ALERT_NAME_INPUT_ID), aaName));

        // Check that the alert is the first in group.

        final String aaXpath = String.format(Locale.US,
            "//div[@role='group']/div[position()=1 and @ftid='%s']", alertNodeBasePath + aaName);
        assertNotNull(ui.getWebElement(By.xpath(aaXpath)));

        // Change the name so that the alert is the last one.

        String zzName = "zzz" + alertName;
        alertNameElement = ui.getWebElement(By.id(WEBVIEW_ALERT_NAME_INPUT_ID));
        ui.clearAndSetInputField(alertNameElement, zzName);

        // Click the Apply button.

        ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

        // Wait for the apply button to turn disabled as indication that the changes have
        // been saved. Disabled button has default pointer.

        ui.waitFor(
            new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));

        // Navigate to the renamed alert.

        ui.selectTreeNode(alertNodeBasePath + zzName);
        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id(WEBVIEW_ALERT_NAME_INPUT_ID), zzName));

        // Check that the alert is the last in group.

        final String zzXpath = String.format(Locale.US,
            "//div[@role='group']/div[position()=count(../child::*) and @ftid='%s']",
            alertNodeBasePath + zzName);
        assertNotNull(ui.getWebElement(By.xpath(zzXpath)));

        // Click the Delete button.

        final By deleteButtonSelector = By.id(WEBVIEW_DELETE_BUTTON_ID);
        ui.clickDialogButton(ui.getWebDriver(), deleteButtonSelector);

        // Wait for confirmation dialog to render.

        WebElement confirmDialog = ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
            By.id("webview-Common-ConfirmMessageBox")));

        // Click Yes.

        ui.clickDialogButton(confirmDialog,
            By.id("webview-Common-ConfirmMessageBox-button-yes"));
        ui.waitFor(ExpectedConditions.stalenessOf(confirmDialog));

        // Check that it has disappeared.

        assertNull(ui.getWebElementOrNull(
            By.xpath(String.format(Locale.US, "//div[@ftid='%s']", alertNodeBasePath))),
            "unexpectedly found node that should have been deleted");
    }

    @NotNull
    private static void selectAndClickFromComboBox(WebViewUi ui, String itemString) {
        WebElement selectionElement = ui.getWebElement(
            By.xpath(String.format(Locale.US,
                "//span[contains(@class, 'webview-Common-ListItem') and text()='%s']",
                itemString)));
        ui.scrollIntoView(selectionElement);
        ui.getActions()
            .moveToElement(selectionElement)
            .click()
            .perform();
        ui.waitFor(ExpectedConditions.stalenessOf(selectionElement));
    }

    private static void clickComboBoxExpander(WebViewUi ui, String comboBoxInputId) {
        // Find combo box expander and click it.

        By expanderSelector = By.xpath(
            String.format(Locale.US, "//input[@id='%s']/../following-sibling::td/div",
                comboBoxInputId));
        WebElement metricGroupingListExpander = ui.getWebElement(expanderSelector);
        ui.getActions()
            .moveToElement(metricGroupingListExpander)
            .click()
            .perform();
    }

    private static void changeThresholds(WebViewUi ui, String dangerThreshold,
        String cautionThreshold) {
        ui.clearAndSetInputField(
            ui.getWebElement(By.id("webview-MMEditor-aEditor-danger-threshold-text-input")),
            dangerThreshold);

        ui.clearAndSetInputField(
            ui.getWebElement(By.id("webview-MMEditor-aEditor-caution-threshold-text-input")),
            cautionThreshold);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MANAGEMENT_TAB"})
    @AlmId(349745)
    public void addRemoveActionTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);

            // Go to management tab.

            ui.clickManagementTab();

            // Wait for it to render.

            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID)));

            // Select alert node to test on.

            ui.selectTreeNode(TEST_NODE);
            ui.waitFor(
                ExpectedConditions.textToBePresentInElementValue(
                    By.id(WEBVIEW_ALERT_NAME_INPUT_ID), SIMPLE_ALERT_NAME));

            // Hit Add button for Danger Action.

            ui.clickDialogButton(ui.getWebDriver(),
                By.id("webview-MMEditor-aEditor-danger-add-button"));

            // Wait for Choose Action dialogue to appear and pick action.

            WebElement chooseActionDialog = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[text()='Choose Action']/../../../../..")));

            pickAction(ui, chooseActionDialog, CONSOLE_NOTIFICATION_ACTION, TEST_MM_NAME);

            // Check that the selected action is in the Actions list.

            WebElement actionsList = ui.getWebElement(
                By.id("webview-MMEditor-aEditor-danger-actions-field"));
            verifyActionInActionsList(ui, actionsList, CONSOLE_NOTIFICATION_ACTION);

            // Save changes by pressing Apply button.

            ui.clickDialogButton(ui.getWebDriver(),
                By.id("webview-MMEditor-buttonBar-apply-button"));

            // Navigate elsewhere.

            ui.selectTreeNode(SEND_EMAIL_TREE_NODE);

            // Go back to our node.

            ui.selectTreeNode(TEST_NODE);
            ui.waitFor(
                ExpectedConditions.textToBePresentInElementValue(
                    By.id(WEBVIEW_ALERT_NAME_INPUT_ID), SIMPLE_ALERT_NAME));

            // Check that the selected action is still in the Actions list.

            actionsList = ui.getWebElement(
                By.id("webview-MMEditor-aEditor-danger-actions-field"));
            WebElement actionElement = verifyActionInActionsList(ui, actionsList,
                CONSOLE_NOTIFICATION_ACTION);

            // Select the action.

            ui.getActions()
                .moveToElement(actionElement)
                .click()
                .perform();

            // Click Remove button and wait for it.

            By removeButtonSelector = By.id("webview-MMEditor-aEditor-danger-remove-button");
            ui.clickDialogButton(ui.getWebDriver(), removeButtonSelector);
            ui.waitFor(
                new WaitForCssValueCondition(ui, removeButtonSelector, "cursor", "default"));

            // Verify the action has been removed from the list.

            verifyActionNotInActionsList(ui, actionsList, CONSOLE_NOTIFICATION_ACTION);

            // Save changes by pressing Apply button.

            ui.clickDialogButton(ui.getWebDriver(),
                By.id("webview-MMEditor-buttonBar-apply-button"));

            // Navigate elsewhere.

            ui.selectTreeNode(SEND_EMAIL_TREE_NODE);

            // Go back to our node.

            ui.selectTreeNode(TEST_NODE);
            ui.waitFor(
                ExpectedConditions.textToBePresentInElementValue(
                    By.id(WEBVIEW_ALERT_NAME_INPUT_ID), SIMPLE_ALERT_NAME));

            // Verify that the previously removed action is still removed from the list.

            actionsList = ui.getWebElement(
                By.id("webview-MMEditor-aEditor-danger-actions-field"));
            verifyActionNotInActionsList(ui, actionsList, CONSOLE_NOTIFICATION_ACTION);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MANAGEMENT_TAB"})
    @AlmId(352588)
    public void deleteSimpleAlert() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);

            // Go to management tab.

            ui.clickManagementTab();

            // Wait for it to render.

            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID)));

            verifySimpleAlertDeletion(ui, true);
            verifySimpleAlertDeletion(ui, false);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MANAGEMENT_TAB"})
    @AlmId(349729)
    public void renameAlert() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);

            // Go to management tab.

            ui.clickManagementTab();

            // Wait for it to render.

            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID)));

            verifySimpleAlertRenaming(ui, true);
            verifySimpleAlertRenaming(ui, false);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MANAGEMENT_TAB"})
    @AlmId(349732)
    public void changeMetricGrouping() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);

            // Go to management tab.

            ui.clickManagementTab();

            // Wait for it to render.

            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID)));

            final String alertName = SIMPLE_ALERT_NAME;
            final String alertNodePathBase
                = "management-tree_*SuperDomain*|Management Modules|TestSample|Alerts|";
            final String alertNodePath = alertNodePathBase + alertName;
            ui.selectTreeNode(alertNodePath);
            ui.waitFor(
                ExpectedConditions.textToBePresentInElementValue(
                    By.id(WEBVIEW_ALERT_NAME_INPUT_ID), alertName));

            // Find combo box expander and click it.

            clickComboBoxExpander(ui, "webview-MMEditor-aEditor-mgCombo-combo-input");

            // Select appropriate metric grouping

            final String metricGroupingName = "Backend Errors";
            selectAndClickFromComboBox(ui, metricGroupingName);

            clickApply(ui);

            // Go to different node.

            ui.selectTreeNode(alertNodePathBase + "Backend Heuristics");

            // Go back to previous node.

            ui.selectTreeNode(alertNodePath);

            // Check that the metric grouping is still set.

            WebElement metricGroupingInputElement = ui.getWebElement(
                By.xpath("//input[@id='webview-MMEditor-aEditor-mgCombo-combo-input']"));
            final String value = metricGroupingInputElement.getAttribute("value");
            assertEquals(value, metricGroupingName);

            // Restore previous metric grouping.
            // Find combo box expander and click it.

            clickComboBoxExpander(ui, "webview-MMEditor-aEditor-mgCombo-combo-input");
            selectAndClickFromComboBox(ui, SIMPLE_ALERT_NAME);
            clickApply(ui);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MANAGEMENT_TAB"})
    @AlmId(349741)
    public void greaterThanThreshold() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);

            // Go to management tab.

            ui.clickManagementTab();

            // Wait for it to render.

            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID)));

            final String alertName = SIMPLE_ALERT_NAME;
            final String alertNodePathBase
                = "management-tree_*SuperDomain*|Management Modules|TestSample|Alerts|";
            final String alertNodePath = alertNodePathBase + alertName;
            ui.selectTreeNode(alertNodePath);
            ui.waitFor(
                ExpectedConditions.textToBePresentInElementValue(
                    By.id(WEBVIEW_ALERT_NAME_INPUT_ID), alertName));

            // Select operator.

            clickComboBoxExpander(ui, "webview-MMEditor-aEditor-comparision-combo-input");
            final String comparisonOperatorName = "Not Equal To";
            selectAndClickFromComboBox(ui, comparisonOperatorName);
            changeThresholds(ui, "2", "1");

            // Click Apply button.

            clickApplyNoWait(ui);

            // Wait for alert box.

            WebElement mboxElement = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.MM_EDITOR_ALERT_MESSAGE_BOX_ID)));
            WebElement mboxTextElement = ui.getWebElement(mboxElement,
                By.id("webview-Common-AlertMessageBox-message"));
            String mboxAlertText = mboxTextElement.getText();
            assertEquals(mboxAlertText,
                "Did not apply changes. The danger threshold must be equal to the caution "
                    + "threshold.");
            ui.clickDialogButton(mboxElement, By.id("webview-Common-AlertMessageBox-button-ok"));
            ui.waitFor(ExpectedConditions.stalenessOf(mboxElement));

            // Change thresholds.

            changeThresholds(ui, "1", "1");

            // Click Apply button.

            clickApply(ui);

            // Check of no alert message box.

            ui.delay(TimeUnit.SECONDS, 1);
            assertNull(ui.getWebElementOrNull(By.id(WvManagementTabConstants.MM_EDITOR_ALERT_MESSAGE_BOX_ID)));

            // Go to different node.

            ui.selectTreeNode(alertNodePathBase + "Backend Heuristics");

            // Go back to previous node.

            ui.selectTreeNode(alertNodePath);

            // Check that comparison operator is still there.

            String dangerThresholdText = ui.getWebElement(
                By.id("webview-MMEditor-aEditor-danger-threshold-text-input"))
                .getAttribute("value");
            assertEquals(dangerThresholdText, "1");

            String cautionThresholdText = ui.getWebElement(
                By.id("webview-MMEditor-aEditor-caution-threshold-text-input"))
                .getAttribute("value");
            assertEquals(cautionThresholdText, "1");

            String operatorText = ui.getWebElement(
                By.id("webview-MMEditor-aEditor-comparision-combo-input"))
                .getAttribute("value");
            assertEquals(operatorText, comparisonOperatorName);

            // Change threshold.

            changeThresholds(ui, "1", "2");

            // Click Apply button.

            clickApplyNoWait(ui);

            // Wait for alert box.

            mboxElement = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WvManagementTabConstants.MM_EDITOR_ALERT_MESSAGE_BOX_ID)));
            mboxTextElement = ui.getWebElement(mboxElement,
                By.id("webview-Common-AlertMessageBox-message"));
            mboxAlertText = mboxTextElement.getText();
            assertEquals(mboxAlertText,
                "Did not apply changes. The danger threshold must be equal to the caution "
                    + "threshold.");
            ui.clickDialogButton(mboxElement, By.id("webview-Common-AlertMessageBox-button-ok"));
            ui.waitFor(ExpectedConditions.stalenessOf(mboxElement));

            // Restore previous settings.

            clickComboBoxExpander(ui, "webview-MMEditor-aEditor-comparision-combo-input");
            selectAndClickFromComboBox(ui, "Greater Than");
            changeThresholds(ui, "2", "1");
            clickApply(ui);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }
}
