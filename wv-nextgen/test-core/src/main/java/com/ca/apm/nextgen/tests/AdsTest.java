package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WaitForCssValueCondition;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Locale;

import static com.ca.apm.nextgen.tests.SmtpTest.WEBVIEW_PASSWORD;
import static com.ca.apm.nextgen.tests.SmtpTest.WEBVIEW_USER;

/**
 * @author haiva01
 */
public class AdsTest extends BaseWebViewTest {
    private static final Logger log = LoggerFactory.getLogger(AdsTest.class);
    private static final String TEST_SCHEDULE_NODE
        = "management-tree_*SuperDomain*|Management Modules|AdsTestMM|Alert Downtime Schedules"
        + "|Test Schedule";
    private static final int YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final String GOOD_CRON_YEAR_INPUTS[] = {
        "1981-" + (YEAR + 1), "*", "" + (YEAR + 1) + "," + (YEAR + 2), " ", "*/2"};
    private static final String BAD_CRON_YEAR_INPUTS[] = {"xyz", "^", "$"};
    private static final String WEBVIEW_APPLY_BUTTON_ID = "webview-MMEditor-buttonBar-apply-button";
    private static final String WEBVIEW_CRON_SCHEDULE_YEAR_INPUT_ID
        = "webview-downtimeEditor-cronSchedule-panel-year-input-input";

    private static void verifyCronYearGoodValue(WebViewUi ui, String value) {
        try {
            // Set the cron year input field.

            WebElement yearInput = ui.getWebElement(
                By.id(WEBVIEW_CRON_SCHEDULE_YEAR_INPUT_ID));
            ui.clearAndSetInputField(yearInput, value);

            // Click the Apply button.

            final By applyButtonSelector = By.id(WEBVIEW_APPLY_BUTTON_ID);
            ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

            // Wait for the apply button to turn disabled as indication that the changes have
            // been saved. Disabled button has default pointer.

            ui.waitFor(
                new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to verify good value {1} for cron schedule year. Exception: {0}", value);
        }
    }

    private static void verifyCronYearBadValue(WebViewUi ui, String value, String expectedText) {
        try {
            // Set the cron year input field.

            WebElement yearInput = ui.getWebElement(
                By.id(WEBVIEW_CRON_SCHEDULE_YEAR_INPUT_ID));
            ui.clearAndSetInputField(yearInput, value);

            // Click the Apply button.

            final By applyButtonSelector = By.id(WEBVIEW_APPLY_BUTTON_ID);
            ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

            // Wait for error dialog to pop up.

            WebElement errorDialog = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-downtimeEditor-alert-mbox")));
            WebElement errorMessage = ui.getWebElement(errorDialog,
                By.id("webview-Common-AlertMessageBox-message"));
            String errorText = errorMessage.getText();
            log.info("Error dialog text: >{}<", errorText);
            log.info("Expected error dialog text: >{}<", expectedText);
            Assert.assertEquals(errorText, expectedText);

            // Dismiss the dialog.

            ui.clickDialogButton(errorDialog, By.id("webview-Common-AlertMessageBox-button-ok"));

            // Wait for it to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(errorDialog));
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw ErrorReport.logExceptionAndWrapFmt(log, e,
                "Failed to verify good value {1} for cron schedule year. Exception: {0}", value);
        }
    }

    private static String buildInvalidValueString(String value) {
        return String.format(Locale.US,
            "Did not apply changes because Year field contains invalid value %s.", value);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "ADS", "MANAGEMENT_TAB"})
    @AlmId(355349)
    public void adsCronYearTest() {
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

            // Navigate to testing schedule.

            ui.selectTreeNode(TEST_SCHEDULE_NODE);
            ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WEBVIEW_CRON_SCHEDULE_YEAR_INPUT_ID)));

            // Try all good inputs.

            for (String input : GOOD_CRON_YEAR_INPUTS) {
                log.info("Trying '{}' for cron year field.", input);
                verifyCronYearGoodValue(ui, input);
            }

            // Try a bad input to verify error dialogue.

            verifyCronYearBadValue(ui, "xyz",
                "Did not apply changes because Year field contains invalid range.");

            verifyCronYearBadValue(ui, "^", buildInvalidValueString("^"));

        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }
}
