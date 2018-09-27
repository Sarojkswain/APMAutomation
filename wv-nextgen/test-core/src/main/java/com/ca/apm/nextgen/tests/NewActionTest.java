package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvManagementTabConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apm.nextgen.tests.SmtpTest.WEBVIEW_PASSWORD;
import static com.ca.apm.nextgen.tests.SmtpTest.WEBVIEW_USER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author haiva01
 */
public class NewActionTest extends BaseWebViewTest {
    private static final Logger log = LoggerFactory.getLogger(NewActionTest.class);
    private static final String DEFAULT_MM_NAME = "Default";

    @NotNull
    private static String createNewConsoleNotificationAction(WebViewUi ui, String mmName,
        String actionName, boolean forceUniqueness) {
        // Click Elements button.

        ui.clickDialogButton(ui.getWebDriver(), By.id(WvManagementTabConstants.ELEMENTS_DROP_DOWN_BUTTON_ID));

        // Wait for menu to appear.

        WebElement elementsMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(WvManagementTabConstants.ELEMENTS_MENU_DROP_DOWN_ID)));

        // Click New Action button in the menu.

        ui.moveToMenuButton(elementsMenu, By.id(WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_ID));

        // Wait for new actions menu to appear.
        WebElement newActionsMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(WvManagementTabConstants.ELEMENTS_MENU_NEW_ACTION_SUBMENU_ID)));

        // Click New Console Notification Action in the menu.

        ui.clickMenuButton(newActionsMenu,
            By.id("webview-MMEditor-newConsoleNotificationAction-menuItem"));

        // Wait for dialog to appear.

        WebElement newActionDialog = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("webview-elementCreationPanel-verticalLayout-layout")));

        // Fill the new action dialog.

        fillNewConsoleActionDialog(ui, newActionDialog, actionName, mmName, forceUniqueness);

        // Wait for the action to be created and to render.

        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id("webview-mmEditor-mmeditorContent-name-field-input"), actionName));

        return actionName;
    }

    private static void fillNewConsoleActionDialog(WebViewUi ui, WebElement newActionDialog,
        String actionName, String mmName, boolean forceUniqueness) {
        // Fill the dialog.

        ManagementTabUtils.fillNewElementDialog(ui, newActionDialog, actionName, mmName, forceUniqueness);

        // Click OK button.

        ui.clickDialogButton(ui.getWebDriver(), By.id("webview-mmEditor-Element-OK-Button"));

        // Wait for the dialog to disappear.

        ui.waitFor(ExpectedConditions.stalenessOf(newActionDialog));
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "MANAGEMENT_TAB"})
    @AlmId(353506)
    public void newUniqueConsoleActionTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);

            // Go to management tab.

            ui.clickManagementTab();

            // Wait for it to render.

            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-elements-splitButton")));

            // Create new email sending action.

            String actionName = "Console action " + UUID.randomUUID().toString();
            actionName = createNewConsoleNotificationAction(ui, DEFAULT_MM_NAME, actionName, false);

            // Try to create another new email sending action with the same name.

            createNewConsoleNotificationAction(ui, DEFAULT_MM_NAME, actionName, true);

            // Try to navigate to parent node of our new action node.

            final String newActionParentNodeID = String.format(Locale.US,
                "management-tree_*SuperDomain*|Management Modules|%s|Actions", DEFAULT_MM_NAME);
            WebElement parentNode = ui.selectTreeNode(newActionParentNodeID);
            parentNode = ui.getWebElement(parentNode, By.xpath("../.."));
            log.debug("Found tree node: {}", parentNode);

            // List all child nodes of this parent node and find those that have the same prefix
            // as our initial action name. Check that new one has #number appended.

            final String prefix = newActionParentNodeID + "|" + actionName;
            Collection<WebElement> webElements = ui.getWebElements(parentNode,
                By.xpath(String.format(Locale.US, ".//div[starts-with(@ftid,'%s')]//span[text()]",
                    prefix)));
            assertEquals(webElements.size(), 2,
                "We expect to find only two elements here. One is the original action and the "
                    + "other is the new action with appended #number.");
            Collection<String> texts = new ArrayList<>(webElements.size());
            for (WebElement webElement : webElements) {
                texts.add(webElement.getText());
            }

            log.debug("Found these texts: {}", texts);
            for (String text : texts) {
                if (text.equals(actionName)) {
                    log.info("This is the original action: >{}<", text);
                } else {
                    log.info("This should be the new unique action with #number appended: >{}<",
                        text);
                    Pattern pattern = Pattern.compile(
                        "^" + Pattern.quote(actionName) + "\\s#\\d+$");
                    Matcher matcher = pattern.matcher(text);
                    assertTrue(matcher.matches());
                }
            }
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }
}
