package com.ca.apm.nextgen.tests;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.common.WvManagementTabConstants;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.ManagementTabUtils;
import com.ca.apm.nextgen.tests.helpers.WaitForCssValueCondition;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.apm.nextgen.tests.helpers.WiserSmtpServer;
import com.ca.tas.tests.annotations.AlmId;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.WiserMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import de.svenjacobs.loremipsum.LoremIpsum;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static java.text.Normalizer.Form.NFC;
import static java.util.regex.Pattern.DOTALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author haiva01
 */
public class SmtpTest extends BaseWebViewTest {
    private static final Logger log = LoggerFactory.getLogger(SmtpTest.class);
    public static final String WEBVIEW_USER = "Admin";
    public static final String WEBVIEW_PASSWORD = "";
    private static final String TEST_FROM_EMAIL_ADDRESS_1 = "tester1@aquarius.ca.com";
    private static final String TEST_FROM_EMAIL_ADDRESS_2 = "tester2@aquarius.ca.com";
    private static final String TEST_TO_EMAIL_ADDRESS_1 = "admin1@aquarius.ca.com";
    private static final String TEST_TO_EMAIL_ADDRESS_2 = "admin2@aquarius.ca.com";
    private static final String TEST_EMAIL_SERVER_ADDRESS_1 = "mail.ca.com";
    private static final String TEST_EMAIL_SERVER_ADDRESS_2 = "eumail.ca.com";
    private static final String[] SEND_EMAIL_TREE_NODES = {
        "management-tree_*SuperDomain*",
        "management-tree_*SuperDomain*|Management Modules",
        "management-tree_*SuperDomain*|Management Modules|Default",
        "management-tree_*SuperDomain*|Management Modules|Default|Actions",
        "management-tree_*SuperDomain*|Management Modules|Default|Actions|Send eMail"};
    public static final String SEND_EMAIL_TREE_NODE
        = "management-tree_*SuperDomain*|Management Modules|Default|Actions|Send eMail";
    private static final String FROM_FIELD_ID
        = "webview-MMEditor-SMTPEditor-panel-from-field-input";
    private static final String TO_FIELD_ID
        = "webview-MMEditor-SMTPEditor-panel-to-field-input";
    private static final String EMAIL_SUBJECT_NON_ASCII_TEXT = Normalizer.normalize(
        "私はガラスを食べられます。", NFC);
    private static final String LINK_IN_EMAIL_HREF = "www.ca.com";
    private static final String LINK_IN_EMAIL_TEXT = "CA Inc";
    private static final String LINK_IN_EMAIL = "<a href=\"" + LINK_IN_EMAIL_HREF + "\">"
        + LINK_IN_EMAIL_TEXT + "</a>";
    private static final String EMAIL_BODY_NON_ASCII_TEXT = Normalizer.normalize(
        "それは私を傷つけません。", NFC);
    private static final String EMAIL_PLAIN_TEXT_BODY_LINE
        = "This is a testing text.";
    private static final String EMAIL_PLAIN_TEXT_SUBJECT = "Plain text test subject";
    private static final String SIMPLE_ALERT_EMAIL_SUBJECT
        = "CA APM Alert: ${Alert_Name} in ${Alert_State} state";
    private static final String SIMPLE_ALERT_EMAIL_BODY = "Alert Name: ${Alert_Name}\n"
        + "\n"
        + "Alert Status: ${Alert_State}\n"
        + "\n"
        + "Problem Details:\n"
        + "${Problem_Detail_List}\n"
        + "\n"
        + "Link to more information about the Alert:\n"
        + "${URL_Alert_Info}\n"
        + "\n"
        + "Links to the problem metrics or associated alerts:\n"
        + "${URL_Detail_List}";
    private static final String SIMPLE_ALERT_ACTION_TREE_NODE
        = "management-tree_*SuperDomain*|Management Modules|A-Webview-FunctionalTest-Module"
        + "|Actions|SMTP Test Simple Alert";
    private static final String SUMMARY_ALERT_ACTION_TREE_NODE
        = "management-tree_*SuperDomain*|Management Modules|A-Webview-FunctionalTest-Module"
        + "|Actions|SMTP Test Summary Alert";

    private static final String SIMPLE_ALERT_TEST_METRIC_NAME
        = "SMTP Test Simple Alert - Utilization % (process)";
    private static final String WEBVIEW_EMAIL_SUBJECT_FIELD_INPUT_ID
        = "webview-MMEditor-SMTPEditor-panel-email-subject-field-input";
    private static final String WEBVIEW_PLAIN_TEXT_CHECKBOX_NAME
        = "webview-MMEditor-SMTPEditor-panel-plain-text-check";
    private static final String WEBVIEW_ADVANCED_MAIL_SERVER_CONFIG_DIALOG_ID
        = "webview-advanced-mail-server-config-dialog";
    private static final String WEBVIEW_APPLY_BUTTON_ID = "webview-MMEditor-buttonBar-apply-button";
    private static final String WEBVIEW_CONFIG_MAIL_SERVER_LINK_ID
        = "webview-MMEditor-SMTPEditor-panel-config-mail-server-link";
    private static final String WEBVIEW_ACTIVE_CHECKBOX_NAME
        = WvManagementTabConstants.MM_EDITOR_ACTIVE_CECK_BOX_NAME;
    private static final String WEBVIEW_EMAIL_BODY_TEXT_INPUT_ID
        = "webview-MMEditor-SMTPEditor-panel-email-body-text-input";
    private static final String EMAIL_PLAIN_TEXT_SPECIAL_CHARACTERS
        = "~`!@#$%^&*()_+=-|}{\":?><,./;'[]\\";
    private static final String LONG_ENGLISH_WORD = "Honorificabilitudinitatibus";
    private static final String LONG_GERMAN_WORD
        = "Donaudampfschiffahrtselektrizitätenhauptbetriebswerkbauunterbeamtengesellschaft";
    private static final String SUMMARY_ALERT_NAME
        = "SMTP Test Summary Alert - SMTP Test Simple Alert";

    private static void fillSmtpServerSettingDialog(WebViewUi ui, WebElement dialog,
        String hostName, int port) {
        try {
            WebElement hostInputField = ui.getWebElement(dialog,
                By.id("webview-advanced-mail-server-config-host-field-input"));

            WebElement hostPortInputField = ui.getWebElement(dialog,
                By.id("webview-advanced-mail-server-config-port-field-input"));

            // Clear the host field and input SMTP host name.

            ui.clearAndSetInputField(hostInputField, hostName);

            // Clear port field and in input SMTP port value.

            ui.clearAndSetInputField(hostPortInputField, Integer.toString(port));

            // Click OK button of this dialogue.

            ui.clickDialogButton(dialog, By.id("webview-advanced-mail-server-config-ok-button"));

            // Wait for the dialog to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(dialog));
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }
    }

    private static void pressTestNowButton(WebViewUi ui) {
        try {
            // Press Test Now! button.

            ui.clickDialogButton(ui.getWebDriver(),
                By.id("webview-MMEditor-SMTPEditor-panel-testNow-button"));


            // Wait for info box dialog.

            WebElement infoBox = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-mmEditor-Element-mailActionMessage-AlertMessageBox")));

            // Check that the message box mentions success.

            WebElement infoBoxTextElement = ui.getWebElement(infoBox,
                By.id("webview-Common-InfoMessageBox-message"));
            String infoBoxText = infoBoxTextElement.getText();
            log.debug("Info box text: >{}<", infoBoxText);
            assertTrue(
                infoBoxText.matches(
                    "Action \"[^\"]+\" successfully sent SMTP mail to \"[^\"]+\""));

            // Dismiss the info box dialog.

            ui.clickDialogButton(infoBox, By.id("webview-Common-InfoMessageBox-button-ok"));

            // Wait for it to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(infoBox));
        } catch (Throwable e) {
            ui.takeScreenShot();
            throw e;
        }
    }

    private static void prepareEmailAction(WebViewUi ui, String smtpHost, int smtpPort,
        String emailSubject, String emailText, String fromAddress, String toAddress,
        boolean plainText, String managementTreeNode) {
        // Go to Console tab.

        ui.clickManagementTab();

        // Find email sending alert.

        ui.selectTreeNode(managementTreeNode);

        // Wait for the settings page to render.

        ui.waitFor(ExpectedConditions.visibilityOfElementLocated(
            By.id(WEBVIEW_CONFIG_MAIL_SERVER_LINK_ID)));

        fillEmailActionSettings(ui, smtpHost, smtpPort, emailSubject, emailText, fromAddress,
            toAddress, plainText);
    }

    private static void fillEmailActionSettings(WebViewUi ui, String smtpHost, int smtpPort,
        String emailSubject, String emailText, String fromAddress, String toAddress,
        boolean plainText) {

        // Activate this action.

        WebElement activateComboBox = ui.getWebElement(By.name(WEBVIEW_ACTIVE_CHECKBOX_NAME));
        ui.checkCheckBox(activateComboBox);

        // Change to and from email addresses.

        WebElement fromField = ui.getWebElement(By.id(FROM_FIELD_ID));
        ui.clearAndSetInputField(fromField, fromAddress);

        WebElement toField = ui.getWebElement(By.id(TO_FIELD_ID));
        ui.clearAndSetInputField(toField, toAddress);

        // Change email body.

        WebElement emailBodyElement = ui.getWebElement(
            By.id(WEBVIEW_EMAIL_BODY_TEXT_INPUT_ID));

        final String TEXT_TO_BE_DELETED = "This will be deleted.";
        ui.clearAndSetInputField(emailBodyElement, TEXT_TO_BE_DELETED);

        // Delete the just added text.

        final String BACKSPACES = StringUtils.repeat(Keys.BACK_SPACE.toString(),
            TEXT_TO_BE_DELETED.length());
        ui.getActions()
            .sendKeys(BACKSPACES)
            .sendKeys(emailText)
            .perform();

        // Change email subject.

        WebElement emailSubjectElement = ui.getWebElement(
            By.id(WEBVIEW_EMAIL_SUBJECT_FIELD_INPUT_ID));
        ui.clearAndSetInputField(emailSubjectElement, emailSubject);

        // Check checkbox to send it as plain text only.

        WebElement plainTextCheckBox = ui.getWebElement(
            By.name(WEBVIEW_PLAIN_TEXT_CHECKBOX_NAME));
        if (plainText) {
            ui.checkCheckBox(plainTextCheckBox);
        } else {
            ui.uncheckCheckBox(plainTextCheckBox);
        }

        // Click the link that should bring up settings dialogue.

        final WebElement link = ui.getWebElement(By.id(WEBVIEW_CONFIG_MAIL_SERVER_LINK_ID));
        ui.moveAndClick(link);

        // Wait for the dialog, host and port field to render.

        WebElement dialog = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(WEBVIEW_ADVANCED_MAIL_SERVER_CONFIG_DIALOG_ID)));

        // Fill SMTP server information.

        fillSmtpServerSettingDialog(ui, dialog, smtpHost, smtpPort);

        // Click apply button.

        final By applyButtonSelector = By.id(WEBVIEW_APPLY_BUTTON_ID);
        ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

        // Wait for the apply button to turn disabled as indication that the changes have
        // been saved. Disabled button has default pointer.

        ui.waitFor(
            new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));
    }

    private static void disableEmailAction(WebViewUi ui) {
        // Disable delivery of more emails.

        WebElement activateComboBox = ui.getWebElement(By.name(WEBVIEW_ACTIVE_CHECKBOX_NAME));
        ui.uncheckCheckBox(activateComboBox);

        // Click apply button.

        final By applyButtonSelector = By.id(WEBVIEW_APPLY_BUTTON_ID);
        ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

        // Wait for the apply button to turn disabled as indication that the changes have
        // been saved. Disabled button has default pointer.

        ui.waitFor(
            new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));
    }

    private static void verifyAlertEmailRichText(MimeMessage mimeMessage, String expectedSubject,
        String expectedInBody1, String expectedInBody2)
        throws MessagingException, IOException {

        ContentType emailContentType = new ContentType(mimeMessage.getContentType());
        final String baseContentType = emailContentType.getBaseType();
        log.debug("Base content type: {}", baseContentType);
        assertTrue(mimeMessage.isMimeType("multipart/*"));

        MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
        log.debug("MIME multipart preamble: >{}<", mimeMultipart.getPreamble());

        String htmlPart = null;
        for (int count = mimeMultipart.getCount(), i = 0; i != count; ++i) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                htmlPart = (String) bodyPart.getContent();
                log.debug("HTML part content type: {}", bodyPart.getContentType());
                final ContentType bodyPartContentType = new ContentType(bodyPart.getContentType());
                final String charsetValue = bodyPartContentType.getParameter("charset");
                log.debug("Content type charset: {}", charsetValue);
                assertEquals(charsetValue, "utf-8");
                break;
            }
        }
        assertNotNull(htmlPart);

        final Document document = Jsoup.parse(htmlPart);
        assertEquals(document.charset(), StandardCharsets.UTF_8);

        String emailText = document.text();
        log.debug("Email HTML body text: >{}<", emailText);
        emailText = Normalizer.normalize(emailText, NFC);

        // Check that email does contain expected text.

        Pattern pattern = Pattern.compile(
            ".*" + Pattern.quote(expectedInBody1) + ".*", DOTALL);
        Matcher matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        pattern = Pattern.compile(
            ".*" + Pattern.quote(expectedInBody2) + ".*", DOTALL);
        matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        // Check subject is present as sent.

        String emailSubject = mimeMessage.getSubject();
        log.debug("Email subject text: >{}<", emailSubject);
        emailSubject = Normalizer.normalize(emailSubject, NFC);
        pattern = Pattern.compile(
            ".*" + Pattern.quote(expectedSubject) + ".*", DOTALL);
        matcher = pattern.matcher(emailSubject);
        assertTrue(matcher.matches(),
            "The subject we have sent was not found in received message.");
    }

    private static void verifyAlertEmailPlainText(MimeMessage mimeMessage, String expectedSubject,
        String expectedInBody1, String expectedInBody2)
        throws MessagingException, IOException {
        ContentType emailContentType = new ContentType(mimeMessage.getContentType());
        final String baseContentType = emailContentType.getBaseType();
        log.debug("Base content type: {}", baseContentType);
        assertEquals(baseContentType, "text/plain",
            "Expecting text/plain base content type.");

        final String charsetValue = emailContentType.getParameter("charset");
        log.debug("Content type charset: {}", charsetValue);
        assertEquals(charsetValue, "utf-8");

        String emailText = mimeMessage.getContent().toString();
        log.debug("Email body text: >{}<", emailText);
        emailText = Normalizer.normalize(emailText, NFC);

        Pattern pattern = Pattern.compile(".*" + Pattern.quote(expectedInBody1) + ".*", DOTALL);
        Matcher matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        pattern = Pattern.compile(".*" + Pattern.quote(expectedInBody2) + ".*", DOTALL);
        matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        String emailSubject = mimeMessage.getSubject();
        log.debug("Email subject text: >{}<", emailSubject);
        emailSubject = Normalizer.normalize(emailSubject, NFC);
        assertEquals(emailSubject, expectedSubject,
            "The subject we have sent was not found in received message.");
    }

    private static void fillNewActionDialog(WebViewUi ui, WebElement newActionDialog,
        String actionName, String mmName, boolean forceUniqueness) {
        // Fill the dialog.

        ManagementTabUtils.fillNewElementDialog(ui, newActionDialog, actionName, mmName, forceUniqueness);

        // Click OK button.

        ui.clickDialogButton(ui.getWebDriver(), By.id("webview-mmEditor-Element-OK-Button"));

        // Wait for the dialog to disappear.

        ui.waitFor(ExpectedConditions.stalenessOf(newActionDialog));
    }

    @NotNull
    private static String createNewEmailAction(WebViewUi ui, String actionName,
        boolean forceUniqueness) {
        // Click Elements button.

        ui.clickDialogButton(ui.getWebDriver(), By.id("webview-MMEditor-elements-splitButton"));

        // Wait for menu to appear.

        WebElement elementsMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("webview-MMEditor-elements-menu")));

        // Click New Action button in the menu.

        ui.moveToMenuButton(elementsMenu, By.id("webview-MMEditor-newAction-menuItem"));

        // Wait for new actions menu to appear.

        WebElement newActionsMenu = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("webview-MMEditor-action-menu")));

        // Click New Send SMTP Mail Action in the menu.

        ui.clickMenuButton(newActionsMenu,
            By.id("webview-MMEditor-newSendSmtpMailAction-menuItem"));


        // Wait for dialog to appear.

        WebElement newActionDialog = ui.waitFor(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("webview-elementCreationPanel-verticalLayout-layout")));

        // Fill the new action dialog.

        fillNewActionDialog(ui, newActionDialog, actionName, "Default", forceUniqueness);

        // Wait for the action to be created and to render.

        ui.waitFor(
            ExpectedConditions.textToBePresentInElementValue(
                By.id("webview-mmEditor-mmeditorContent-name-field-input"), actionName));

        return actionName;
    }

    @NotNull
    private static String createNewEmailAction(WebViewUi ui) {
        final String actionName = "Our action " + UUID.randomUUID().toString();
        return createNewEmailAction(ui, actionName, false);
    }

    private void testSmtpEmailSetting(By fieldSelector, String value)
        throws Exception {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);

        // Edit field, save it and close the browser.

        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER,
                WEBVIEW_PASSWORD);

            // Go to Console tab.

            ui.clickManagementTab();

            // Find email sending alert.

            ui.selectTreeNode(SEND_EMAIL_TREE_NODES);

            // Wait for the settings page to render.

            WebElement field = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(fieldSelector));

            // Clear the input field and change the from field email address.

            field.clear();
            ui.getActions()
                .moveToElement(field)
                .click()
                .sendKeys(value)
                .perform();

            // Click apply button.

            final By applyButtonSelector = By.id(WEBVIEW_APPLY_BUTTON_ID);
            ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

            // Wait for the apply button to turn disabled as indication that the changes have
            // been saved. Disabled button has default pointer.

            ui.waitFor(new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));
        }

        // Re-open browser and everify the edit has persisted.

        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER,
                WEBVIEW_PASSWORD);

            // Go to Console tab.

            ui.clickManagementTab();

            // Find email sending alert.

            ui.selectTreeNode(SEND_EMAIL_TREE_NODES);

            // Wait for the settings page to render.

            WebElement fromField = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(fieldSelector));
            String fieldValue = fromField.getAttribute("value");
            Assert.assertEquals(fieldValue, value);
        }
    }

    private void testSmtpHostSetting(String value) throws Exception {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);

        // Edit field, save it and close the browser.

        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER,
                WEBVIEW_PASSWORD);

            // Go to Console tab.

            ui.clickManagementTab();

            // Find email sending alert.

            ui.selectTreeNode(SEND_EMAIL_TREE_NODES);

            // Wait for the settings page to render.

            WebElement link = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WEBVIEW_CONFIG_MAIL_SERVER_LINK_ID)));

            // Click the link that should bring up settings dialogue.

            ui.getActions()
                .moveToElement(link)
                .click()
                .perform();

            // Wait for the dialog and host field to render.

            WebElement dialog = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id(WEBVIEW_ADVANCED_MAIL_SERVER_CONFIG_DIALOG_ID)));

            WebElement hostInputField = ui.getWebElement(dialog,
                By.id("webview-advanced-mail-server-config-host-field-input"));

            // Clear the field and input value.

            hostInputField.clear();
            ui.getActions()
                .moveToElement(hostInputField)
                .click()
                .sendKeys(value)
                .perform();

            // Click OK button of this dialogue.

            ui.clickDialogButton(dialog, By.id("webview-advanced-mail-server-config-ok-button"));

            // Wait for the dialog to disappear.

            ui.waitFor(ExpectedConditions.stalenessOf(dialog));

            // Click apply button.

            final By applyButtonSelector = By.id(WEBVIEW_APPLY_BUTTON_ID);
            ui.clickDialogButton(ui.getWebDriver(), applyButtonSelector);

            // Wait for the apply button to turn disabled as indication that the changes have
            // been saved. Disabled button has default pointer.

            ui.waitFor(new WaitForCssValueCondition(ui, applyButtonSelector, "cursor", "default"));
        }

        // Re-open browser and verify the edit has persisted.

        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER,
                WEBVIEW_PASSWORD);

            // Go to Console tab.

            ui.clickManagementTab();

            // Find email sending alert.

            ui.selectTreeNode(SEND_EMAIL_TREE_NODES);

            // Wait for the settings page to render and check mail server value.

            WebElement label = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='webview-MMEditor-SMTPEditor-panel-host-value']/label")));
            String mailServerValue = label.getText();
            Assert.assertEquals(mailServerValue, value);
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(351811)
    public void smtpFromEmailSettingTest() {
        try {
            // We do the same procedure twice to make sure the email value is actually changed at
            // least once if the previous value in the field was set up by some previous test and
            // is equal to one of our testing values.
            final By fromFieldSelector = By.id(FROM_FIELD_ID);
            testSmtpEmailSetting(fromFieldSelector, TEST_FROM_EMAIL_ADDRESS_1);
            testSmtpEmailSetting(fromFieldSelector, TEST_FROM_EMAIL_ADDRESS_2);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(351812)
    public void smtpToEmailSettingTest() {
        try {
            // We do the same procedure twice to make sure the email value is actually changed at
            // least once if the previous value in the field was set up by some previous test and
            // is equal to one of our testing values.
            final By toFieldSelector = By.id(TO_FIELD_ID);
            testSmtpEmailSetting(toFieldSelector, TEST_TO_EMAIL_ADDRESS_1);
            testSmtpEmailSetting(toFieldSelector, TEST_TO_EMAIL_ADDRESS_2);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(351813)
    public void smtpHostEmailSettingTest() {
        try {
            // We do the same procedure twice to make sure the value is actually changed at least
            // once if the previous value in the field was set up by some previous test and is
            // equal to one of our testing values.
            testSmtpHostSetting(TEST_EMAIL_SERVER_ADDRESS_1);
            testSmtpHostSetting(TEST_EMAIL_SERVER_ADDRESS_2);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    private void verifyPlainTextEmail(MimeMessage mimeMessage, String expectedInSubject,
        String expectedInEmailBody, String expectedInEmailBody2)
        throws MessagingException, IOException {
        ContentType emailContentType = new ContentType(mimeMessage.getContentType());
        final String baseContentType = emailContentType.getBaseType();
        log.debug("Base content type: {}", baseContentType);
        assertEquals(baseContentType, "text/plain",
            "Expecting text/plain base content type.");

        final String charsetValue = emailContentType.getParameter("charset");
        log.debug("Content type charset: {}", charsetValue);
        assertEquals(charsetValue, "utf-8");

        String emailText = mimeMessage.getContent().toString();
        log.debug("Email body text: >{}<", emailText);
        emailText = Normalizer.normalize(emailText, NFC);
        Pattern pattern = Pattern.compile(".*" + Pattern.quote(expectedInEmailBody) + ".*", DOTALL);
        Matcher matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        pattern = Pattern.compile(".*" + Pattern.quote(expectedInEmailBody2) + ".*", DOTALL);
        matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        String emailSubject = mimeMessage.getSubject();
        log.debug("Email subject text: >{}<", emailSubject);
        emailSubject = Normalizer.normalize(emailSubject, NFC);
        pattern = Pattern.compile(
            ".*" + Pattern.quote(expectedInSubject) + ".*", DOTALL);
        matcher = pattern.matcher(emailSubject);
        assertTrue(matcher.matches(),
            "The subject we have sent was not found in received message.");
    }

    private void sendTestingEmail(String hubHost, String smtpHost, int smtpPort,
        String emailSubject, String emailText, boolean plainText, String managementTreeNode)
        throws Exception {
        try (WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
            ui.login("http://" + webViewHost + ":8080/#home;tr=0", WEBVIEW_USER,
                WEBVIEW_PASSWORD);

            // Prepare email action for sending the email.

            prepareEmailAction(ui, smtpHost, smtpPort, emailSubject, emailText,
                TEST_FROM_EMAIL_ADDRESS_1, TEST_TO_EMAIL_ADDRESS_1, plainText, managementTreeNode);

            // Press Test Now button.

            pressTestNowButton(ui);
        }
    }

    private void testSendingPlainTextEmail(String emailPlainTextSubject,
        String emailPlainTextBodyLine1, String emailPlainTextBodyLine2) {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);

        try (WiserSmtpServer wiser = new WiserSmtpServer(0)) {
            // Start local SMTP server.

            wiser.start();

            // Send testing email through WebView UI.

            sendTestingEmail(hubHost, wiser.getHost(), wiser.getPort(), emailPlainTextSubject,
                emailPlainTextBodyLine1 + "\n" + emailPlainTextBodyLine2, true,
                SEND_EMAIL_TREE_NODE);

            // Wait for the email to arrive.

            wiser.waitForEmail(10, TimeUnit.SECONDS);

            // Parse the email and verify it.

            List<WiserMessage> wiserMessageList = wiser.getMessages();
            assertEquals(wiserMessageList.size(), 1, "We are expecting exactly one email message.");

            WiserMessage wiserMessage = wiserMessageList.get(0);
            MimeMessage mimeMessage = wiserMessage.getMimeMessage();
            verifyPlainTextEmail(mimeMessage, emailPlainTextSubject, emailPlainTextBodyLine1,
                emailPlainTextBodyLine2);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    private void testSendingRichTextEmail(String emailPlainTextSubject,
        String emailPlainTextBodyLine1, String linkInEmailHref, String linkInEmailText) {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);

        try (WiserSmtpServer wiser = new WiserSmtpServer(0)) {
            // Start local SMTP server.

            wiser.start();

            // Send testing email through WebView UI.

            String linkInEmail = "<a href=\"" + LINK_IN_EMAIL_HREF + "\">"
                + LINK_IN_EMAIL_TEXT + "</a>";
            sendTestingEmail(hubHost, wiser.getHost(), wiser.getPort(), emailPlainTextSubject,
                emailPlainTextBodyLine1 + " " + linkInEmail, false, SEND_EMAIL_TREE_NODE);

            // Wait for the email to arrive.

            wiser.waitForEmail(10, TimeUnit.SECONDS);

            // Parse the email and verify it.

            List<WiserMessage> wiserMessageList = wiser.getMessages();
            assertEquals(wiserMessageList.size(), 1, "We are expecting exactly one email message.");

            WiserMessage wiserMessage = wiserMessageList.get(0);
            MimeMessage mimeMessage = wiserMessage.getMimeMessage();
            verifyRichTextEmail(mimeMessage, emailPlainTextSubject, emailPlainTextBodyLine1,
                linkInEmail, linkInEmailHref, linkInEmailText);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    private void verifyRichTextEmail(MimeMessage mimeMessage, String expectedInSubject,
        String expectedInEmailBody, String linkInEmail, String linkInEmailHref,
        String linkInEmailText) throws MessagingException, IOException {

        ContentType emailContentType = new ContentType(mimeMessage.getContentType());
        final String baseContentType = emailContentType.getBaseType();
        log.debug("Base content type: {}", baseContentType);
        assertTrue(mimeMessage.isMimeType("multipart/*"));

        MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
        log.debug("MIME multipart preamble: >{}<", mimeMultipart.getPreamble());

        String htmlPart = null;
        for (int count = mimeMultipart.getCount(), i = 0; i != count; ++i) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                htmlPart = (String) bodyPart.getContent();
                log.debug("HTML part content type: {}", bodyPart.getContentType());
                final ContentType bodyPartContentType = new ContentType(bodyPart.getContentType());
                final String charsetValue = bodyPartContentType.getParameter("charset");
                log.debug("Content type charset: {}", charsetValue);
                assertEquals(charsetValue, "utf-8");
                break;
            }
        }
        assertNotNull(htmlPart);

        final Document document = Jsoup.parse(htmlPart);
        assertEquals(document.charset(), StandardCharsets.UTF_8);

        String emailText = document.text();
        log.debug("Email HTML body text: >{}<", emailText);
        emailText = Normalizer.normalize(emailText, NFC);

        // Check that email does contain expected text.

        Pattern pattern = Pattern.compile(
            ".*" + Pattern.quote(expectedInEmailBody) + ".*", DOTALL);
        Matcher matcher = pattern.matcher(emailText);
        assertTrue(matcher.matches(),
            "The text we have sent was not found in received message.");

        // Check that the link is actually a link and that HTML tag is not in spelled out in text.

        pattern = Pattern.compile(".*" + linkInEmail + ".*", DOTALL);
        matcher = pattern.matcher(emailText);
        assertFalse(matcher.matches());

        // Check that link is actually part of the HTML DOM.

        Elements links = document.select("a[href]");
        assertNotNull(links);
        assertEquals(links.size(), 1);
        Element linkElement = links.get(0);
        String href = linkElement.attr("href");
        assertEquals(href, linkInEmailHref);
        String linkText = linkElement.text();
        assertEquals(linkText, linkInEmailText);

        // Check subject is present as sent.

        String emailSubject = mimeMessage.getSubject();
        log.debug("Email subject text: >{}<", emailSubject);
        emailSubject = Normalizer.normalize(emailSubject, NFC);
        pattern = Pattern.compile(
            ".*" + Pattern.quote(expectedInSubject) + ".*", DOTALL);
        matcher = pattern.matcher(emailSubject);
        assertTrue(matcher.matches(),
            "The subject we have sent was not found in received message.");
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(451077)
    public void smtpForeignCharactersTest() {
        testSendingPlainTextEmail(EMAIL_SUBJECT_NON_ASCII_TEXT, EMAIL_BODY_NON_ASCII_TEXT,
            LINK_IN_EMAIL);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(451004)
    public void smtpForeignCharactersRichTextTest() {
        testSendingRichTextEmail(EMAIL_SUBJECT_NON_ASCII_TEXT, EMAIL_BODY_NON_ASCII_TEXT,
            LINK_IN_EMAIL_HREF, LINK_IN_EMAIL_TEXT);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450977)
    public void smtpPlainTextNotificationTest() {
        testSendingPlainTextEmail(EMAIL_PLAIN_TEXT_SUBJECT, EMAIL_PLAIN_TEXT_BODY_LINE,
            LINK_IN_EMAIL);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450976)
    public void smtpRichTextNotificationTest() {
        testSendingRichTextEmail(EMAIL_PLAIN_TEXT_SUBJECT, EMAIL_PLAIN_TEXT_BODY_LINE,
            LINK_IN_EMAIL_HREF, LINK_IN_EMAIL_TEXT);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(451078)
    public void smtpPlainTextSpecialCharactersTest() {
        testSendingPlainTextEmail(EMAIL_PLAIN_TEXT_SPECIAL_CHARACTERS,
            EMAIL_PLAIN_TEXT_SPECIAL_CHARACTERS, LINK_IN_EMAIL);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(451003)
    public void smtpRichTextSpecialCharactersTest() {
        testSendingRichTextEmail(EMAIL_PLAIN_TEXT_SPECIAL_CHARACTERS,
            EMAIL_PLAIN_TEXT_SPECIAL_CHARACTERS, LINK_IN_EMAIL_HREF, LINK_IN_EMAIL_TEXT);
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450981)
    public void smtpSimpleAlertTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        final String expectedSubject = String.format(Locale.US,
            "CA APM Alert: %s in %s state", SIMPLE_ALERT_TEST_METRIC_NAME, "Danger");
        final String expectedInBody1 = String.format(Locale.US,
            "Alert Name: %s", SIMPLE_ALERT_TEST_METRIC_NAME);
        final String expectedInBody2 = String.format(Locale.US,
            "Alert Status: %s", "Danger");

        try (WiserSmtpServer wiser = new WiserSmtpServer(0);
             WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            // Start local SMTP server.

            wiser.start();

            // Set up action so that email alerts start getting generated in rich text.

            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);
            prepareEmailAction(ui, wiser.getHost(), wiser.getPort(), SIMPLE_ALERT_EMAIL_SUBJECT,
                SIMPLE_ALERT_EMAIL_BODY, TEST_FROM_EMAIL_ADDRESS_1, TEST_TO_EMAIL_ADDRESS_1, false,
                SIMPLE_ALERT_ACTION_TREE_NODE);

            // Wait for the email to arrive.

            wiser.waitForEmail(1, TimeUnit.MINUTES);

            // Disable email action so that it does not keep sending us emails.

            disableEmailAction(ui);

            // Parse the email and verify it.

            {
                List<WiserMessage> wiserMessageList = wiser.getMessages();
                assertEquals(wiserMessageList.size(), 1,
                    "We are expecting exactly one email message.");
                WiserMessage wiserMessage = wiserMessageList.get(0);
                verifyAlertEmailRichText(wiserMessage.getMimeMessage(), expectedSubject,
                    expectedInBody1, expectedInBody2);
            }

            // Clear delivered messages so that the next one is the first one.

            wiser.clearDeliverdMessages();

            // Prepare for plain text message.

            prepareEmailAction(ui, wiser.getHost(), wiser.getPort(), SIMPLE_ALERT_EMAIL_SUBJECT,
                SIMPLE_ALERT_EMAIL_BODY, TEST_FROM_EMAIL_ADDRESS_1, TEST_TO_EMAIL_ADDRESS_1, true,
                SIMPLE_ALERT_ACTION_TREE_NODE);

            // Wait for the email to arrive.

            wiser.waitForEmail(1, TimeUnit.MINUTES);

            // Disable email action so that it does not keep sending us emails.

            disableEmailAction(ui);

            // Parse the email and verify it.

            {
                List<WiserMessage> wiserMessageList = wiser.getMessages();
                assertEquals(wiserMessageList.size(), 1,
                    "We are expecting exactly one email message.");
                WiserMessage wiserMessage = wiserMessageList.get(0);
                verifyAlertEmailPlainText(wiserMessage.getMimeMessage(), expectedSubject,
                    expectedInBody1, expectedInBody2);
            }
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450888)
    public void smtpVerifyApply() {
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

            final String actionName = createNewEmailAction(ui);

            // Fill it and apply.

            final boolean plainTextValue = false;
            fillEmailActionSettings(ui, TEST_EMAIL_SERVER_ADDRESS_1, 25, EMAIL_PLAIN_TEXT_SUBJECT,
                EMAIL_PLAIN_TEXT_BODY_LINE, TEST_FROM_EMAIL_ADDRESS_1, TEST_TO_EMAIL_ADDRESS_1,
                plainTextValue);

            // Move to different part of tree.

            ui.selectTreeNode(SIMPLE_ALERT_ACTION_TREE_NODE);

            // Move back to our testing tree node.

            ui.selectTreeNode(
                "management-tree_*SuperDomain*|Management Modules|Default|Actions|" + actionName);

            // Verify the filled fields.

            WebElement fromField = ui.getWebElement(By.id(FROM_FIELD_ID));
            assertEquals(fromField.getAttribute("value"), TEST_FROM_EMAIL_ADDRESS_1);

            WebElement toField = ui.getWebElement(By.id(TO_FIELD_ID));
            assertEquals(toField.getAttribute("value"), TEST_TO_EMAIL_ADDRESS_1);

            WebElement subjectField = ui.getWebElement(By.id(WEBVIEW_EMAIL_SUBJECT_FIELD_INPUT_ID));
            assertEquals(subjectField.getAttribute("value"), EMAIL_PLAIN_TEXT_SUBJECT);

            WebElement bodyElement = ui.getWebElement(By.id(WEBVIEW_EMAIL_BODY_TEXT_INPUT_ID));
            assertEquals(bodyElement.getAttribute("value"), EMAIL_PLAIN_TEXT_BODY_LINE);

            WebElement activeCheckbox = ui.getWebElement(By.name(WEBVIEW_ACTIVE_CHECKBOX_NAME));
            assertTrue(activeCheckbox.isSelected());

            WebElement plainTextCheckbox = ui.getWebElement(
                By.name(WEBVIEW_PLAIN_TEXT_CHECKBOX_NAME));
            assertEquals(plainTextCheckbox.isSelected(), plainTextValue);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }


    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450975)
    public void smtpVerifyMainFormIntellisense() {
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

            createNewEmailAction(ui);

            // Start filling and testing email body input.

            WebElement bodyElement = ui.getWebElement(By.id(WEBVIEW_EMAIL_BODY_TEXT_INPUT_ID));
            bodyElement.clear();
            ui.getActions()
                .moveToElement(bodyElement)
                .click()
                .sendKeys("$")
                .perform();

            WebElement intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            WebElement firstSuggestion = ui.getWebElement(intellisenseMenu, By.xpath("./div[1]"));
            final String firstSuggestionText = firstSuggestion.getText();
            log.debug("Text of first suggestion: >{}<", firstSuggestionText);
            ui.clickMenuButton(intellisenseMenu, By.xpath("./div[1]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(firstSuggestion)));

            String bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            String line1 = "${" + firstSuggestionText + "}";
            Pattern pattern = Pattern.compile(
                "^" + Pattern.quote(line1) + "$", DOTALL);
            Matcher matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            ui.getActions()
                .sendKeys(LONG_ENGLISH_WORD)
                .sendKeys(Keys.ENTER)
                .perform();

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            line1 = line1 + LONG_ENGLISH_WORD;
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "\\n$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            ui.getActions()
                .sendKeys(LONG_GERMAN_WORD)
                .sendKeys(Keys.LEFT, Keys.LEFT, Keys.LEFT)
                .sendKeys("$")
                .perform();

            intellisenseMenu = ui.waitForWebElement(
                By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox"));
            List<WebElement> allSuggestions =
                ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());
            WebElement middleSuggestion = allSuggestions.get(allSuggestions.size() / 2);
            log.debug("Text of middle suggestion: >{}<", middleSuggestion);
            final String middleText = middleSuggestion.getText();
            ui.scrollIntoView(middleSuggestion);
            ui.clickButton(middleSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(middleSuggestion)));

            ui.getActions()
                .sendKeys(Keys.RIGHT, Keys.RIGHT, Keys.RIGHT)
                .sendKeys(Keys.ENTER)
                .perform();

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            final String line2
                = LONG_GERMAN_WORD.substring(0, LONG_GERMAN_WORD.length() - 3)
                + "${" + middleText + "}"
                + LONG_GERMAN_WORD.substring(LONG_GERMAN_WORD.length() - 3);
            log.debug("Expected 1st line: >{}<", line1);
            log.debug("Expected 2nd line: >{}<", line2);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "\\n"
                + Pattern.quote(line2) + "\\n"
                + "$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            String line3 = new LoremIpsum().getParagraphs(1);
            ui.getActions()
                .sendKeys(line3)
                .sendKeys("$")
                .perform();

            intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            allSuggestions = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());
            WebElement lastSuggestion = allSuggestions.get(allSuggestions.size() - 2);
            String lastText = lastSuggestion.getText();
            log.debug("Last suggestion text: >{}<", lastText);
            ui.scrollIntoView(lastSuggestion);
            ui.clickButton(lastSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(lastSuggestion)));

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            line3 = line3 + "${" + lastText + "}";
            log.debug("Expected 1st line: >{}<", line1);
            log.debug("Expected 2nd line: >{}<", line2);
            log.debug("Expected 3rd line: >{}<", line3);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "\\n"
                + Pattern.quote(line2) + "\\n"
                + Pattern.quote(line3) + "$", DOTALL);
            matcher = pattern.matcher(bodyText);

            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            for (int i = 0; i != 15; ++i) {
                ui.getActions()
                    .sendKeys(Keys.ENTER)
                    .perform();
            }
            // XXX The test description mentions checking that scrollbar is visible after we hit
            // ENTER 15 times. However, this does not seem to be detectable through Selenium so
            // it is not implemented in this test.

            ui.getActions()
                .sendKeys("$")
                .sendKeys("a")
                .sendKeys("l")
                .sendKeys("e")
                .sendKeys("r")
                .sendKeys("t")
                .perform();

            intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            allSuggestions = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());

            Collection<String> alertTexts = new TreeSet<>();
            for (WebElement el : allSuggestions) {
                alertTexts.add(el.getText());
            }
            assertTrue(alertTexts.contains("Alert_Name"));
            assertTrue(alertTexts.contains("Alert_State"));
            assertTrue(alertTexts.contains("Alert_Time"));
            assertTrue(alertTexts.contains("URL_Alert_Info"));
            assertTrue(alertTexts.contains("Previous_Alert_State"));

            WebElement alertSuggestion = allSuggestions.get(0);
            String alertText = alertSuggestion.getText();
            ui.scrollIntoView(alertSuggestion);
            ui.clickButton(alertSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(alertSuggestion)));

            log.debug("Suggestion with 'alert' text: >{}<", alertText);
            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            String lastLine = "${" + alertText + "}";
            log.debug("Expected 1st line: >{}<", line1);
            log.debug("Expected 2nd line: >{}<", line2);
            log.debug("Expected 3rd line: >{}<", line3);
            log.debug("Expected last line: >{}<", lastLine);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "\\n"
                + Pattern.quote(line2) + "\\n"
                + Pattern.quote(line3) + "\\n{15}"
                + Pattern.quote(lastLine) + "$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450974)
    public void smtpVerifyMainFormIntellisenseNoNewLines() {
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

            createNewEmailAction(ui);

            // Start filling and testing email body input.

            WebElement bodyElement = ui.getWebElement(By.id(WEBVIEW_EMAIL_BODY_TEXT_INPUT_ID));
            bodyElement.clear();
            ui.getActions()
                .moveToElement(bodyElement)
                .click()
                .sendKeys("$")
                .perform();

            // Select first suggested variable and add it to the start of the email body.

            WebElement intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            WebElement firstSuggestion = ui.getWebElement(intellisenseMenu, By.xpath("./div[1]"));
            final String firstSuggestionText = firstSuggestion.getText();
            log.debug("Text of first suggestion: >{}<", firstSuggestionText);
            ui.clickMenuButton(intellisenseMenu, By.xpath("./div[1]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(firstSuggestion)));

            String bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            String line1 = "${" + firstSuggestionText + "}";
            log.debug("Expected email body text: >{}<", line1);
            Pattern pattern = Pattern.compile(
                "^" + Pattern.quote(line1) + "$", DOTALL);
            Matcher matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            // Add some blind text, almost 300 characters.

            LoremIpsum loremIpsum = new LoremIpsum();
            final String text1 = loremIpsum.getParagraphs(1);
            ui.getActions()
                .sendKeys(text1)
                .perform();

            // Check that the blind text is there.

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            line1 = line1 + text1;
            log.debug("Expected email body text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            // Select last suggestion from list and append it after the blind text.

            ui.getActions()
                .sendKeys("$")
                .perform();

            intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            List<WebElement> allSuggestions
                = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());
            WebElement lastSuggestion = allSuggestions.get(allSuggestions.size() - 2);
            String lastText = lastSuggestion.getText();
            log.debug("Last suggestion text: >{}<", lastText);
            ui.scrollIntoView(lastSuggestion);
            ui.clickButton(lastSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(lastSuggestion)));

            // Check that all the variables and text are there.

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            line1 = line1 + "${" + lastText + "}";
            log.debug("Expected email body text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            // Go back to about a middle of the blind text.

            final int BACK_N_CHARACTERS = 150;
            for (int i = 0; i != BACK_N_CHARACTERS; ++i) {
                long cursorPosBefore = (Long) ui.getJavaScriptExecutor()
                    .executeScript("return arguments[0].selectionStart;", bodyElement);
                ui.getActions()
                    .sendKeys(Keys.LEFT)
                    .perform();
                long cursorPosAfter = (Long) ui.getJavaScriptExecutor()
                    .executeScript("return arguments[0].selectionStart;", bodyElement);
                if (cursorPosBefore == cursorPosAfter) {
                    // Try again. This is an oddity in how multi-line text area works.
                    ui.getActions()
                        .sendKeys(Keys.LEFT)
                        .perform();
                }
            }

            // Insert another variable in the middle of the text from middle of the suggestions
            // list.

            ui.getActions()
                .sendKeys("$")
                .perform();

            intellisenseMenu = ui.waitForWebElement(
                By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox"));
            allSuggestions = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());
            WebElement middleSuggestion = allSuggestions.get(allSuggestions.size() / 2);
            final String middleText = middleSuggestion.getText();
            log.debug("Text of middle suggestion: >{}<", middleSuggestion);
            ui.scrollIntoView(middleSuggestion);
            ui.clickButton(middleSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(middleSuggestion)));

            // Check that all the text is there.

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            line1
                = StringUtils.substring(line1, 0, -BACK_N_CHARACTERS)
                + "${" + middleText + "}"
                + StringUtils.substring(line1, -BACK_N_CHARACTERS);
            log.debug("Expected text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");

            // Move to the end of the email body text area.

            ui.getActions()
                .sendKeys(Keys.chord(Keys.CONTROL, Keys.END))
                .perform();

            // Appender first variable from suggestions list matching $alert.

            ui.getActions()
                .sendKeys("$")
                .sendKeys("a")
                .sendKeys("l")
                .sendKeys("e")
                .sendKeys("r")
                .sendKeys("t")
                .perform();

            intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            allSuggestions = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());

            Collection<String> alertTexts = new TreeSet<>();
            for (WebElement el : allSuggestions) {
                alertTexts.add(el.getText());
            }
            assertTrue(alertTexts.contains("Alert_Name"));
            assertTrue(alertTexts.contains("Alert_State"));
            assertTrue(alertTexts.contains("Alert_Time"));
            assertTrue(alertTexts.contains("URL_Alert_Info"));
            assertTrue(alertTexts.contains("Previous_Alert_State"));

            WebElement alertSuggestion = allSuggestions.get(0);
            String alertText = alertSuggestion.getText();
            ui.scrollIntoView(alertSuggestion);
            ui.clickButton(alertSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(alertSuggestion)));

            // Check that the text is there together with all the previous text.

            bodyText = bodyElement.getAttribute("value");
            log.debug("Current email body of this action: >{}<", bodyText);
            line1 = line1 + "${" + alertText + "}";
            log.debug("Expected text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(bodyText);
            assertTrue(matcher.matches(),
                "The email body of the action does not match the expected value.");
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450973)
    public void smtpVerifyEmailSubjectIntellisense() {
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

            createNewEmailAction(ui);

            // Start filling and testing email body input.

            WebElement subjecElement
                = ui.getWebElement(By.id(WEBVIEW_EMAIL_SUBJECT_FIELD_INPUT_ID));
            subjecElement.clear();
            ui.getActions()
                .moveToElement(subjecElement)
                .click()
                .sendKeys("$")
                .perform();

            // Select first suggested variable and add it to the start of the email body.

            WebElement intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            WebElement firstSuggestion = ui.getWebElement(intellisenseMenu, By.xpath("./div[1]"));
            final String firstSuggestionText = firstSuggestion.getText();
            log.debug("Text of first suggestion: >{}<", firstSuggestionText);
            ui.clickMenuButton(intellisenseMenu, By.xpath("./div[1]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(firstSuggestion)));

            String subjectText = subjecElement.getAttribute("value");
            log.debug("Current email subject of this action: >{}<", subjectText);
            String line1 = "${" + firstSuggestionText + "}";
            log.debug("Expected email subject text: >{}<", line1);
            Pattern pattern = Pattern.compile(
                "^" + Pattern.quote(line1) + "$", DOTALL);
            Matcher matcher = pattern.matcher(subjectText);
            assertTrue(matcher.matches(),
                "The email subject of the action does not match the expected value.");

            // Add some blind text, almost 300 characters.

            LoremIpsum loremIpsum = new LoremIpsum();
            final String text1 = loremIpsum.getParagraphs(1);
            ui.getActions()
                .sendKeys(text1)
                .perform();

            // Check that the blind text is there.

            subjectText = subjecElement.getAttribute("value");
            log.debug("Current email subject of this action: >{}<", subjectText);
            line1 = line1 + text1;
            log.debug("Expected email subject text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(subjectText);
            assertTrue(matcher.matches(),
                "The email subject of the action does not match the expected value.");

            // Select last suggestion from list and append it after the blind text.

            ui.getActions()
                .sendKeys("$")
                .perform();

            intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            List<WebElement> allSuggestions
                = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());
            WebElement lastSuggestion = allSuggestions.get(allSuggestions.size() - 2);
            String lastText = lastSuggestion.getText();
            log.debug("Last suggestion text: >{}<", lastText);
            ui.scrollIntoView(lastSuggestion);
            ui.clickButton(lastSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(lastSuggestion)));

            // Check that all the variables and text are there.

            subjectText = subjecElement.getAttribute("value");
            log.debug("Current email subject of this action: >{}<", subjectText);
            line1 = line1 + "${" + lastText + "}";
            log.debug("Expected email subject text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(subjectText);
            assertTrue(matcher.matches(),
                "The email subject of the action does not match the expected value.");

            // Go back to about a middle of the blind text.

            final int BACK_N_CHARACTERS = 150;
            for (int i = 0; i != BACK_N_CHARACTERS; ++i) {
                ui.getActions()
                    .sendKeys(Keys.LEFT)
                    .perform();
                sleep(250);
            }

            // Insert another variable in the middle of the text from middle of the suggestions
            // list.

            ui.getActions()
                .sendKeys("$")
                .perform();

            intellisenseMenu = ui.waitForWebElement(
                By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox"));
            allSuggestions = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());
            WebElement middleSuggestion = allSuggestions.get(allSuggestions.size() / 2);
            final String middleText = middleSuggestion.getText();
            log.debug("Text of middle suggestion: >{}<", middleSuggestion);
            ui.scrollIntoView(middleSuggestion);
            ui.clickButton(middleSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(middleSuggestion)));

            // Check that all the text is there.

            subjectText = subjecElement.getAttribute("value");
            log.debug("Current email subject of this action: >{}<", subjectText);
            line1
                = StringUtils.substring(line1, 0, -BACK_N_CHARACTERS)
                + "${" + middleText + "}"
                + StringUtils.substring(line1, -BACK_N_CHARACTERS);
            log.debug("Expected email subject text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(subjectText);
            assertTrue(matcher.matches(),
                "The email subject of the action does not match the expected value.");

            // Move to the end of the email body text area.

            ui.getActions()
                .sendKeys(Keys.END)
                .perform();

            // Appender first variable from suggestions list matching $alert.

            ui.getActions()
                .sendKeys("$")
                .sendKeys("a")
                .sendKeys("l")
                .sendKeys("e")
                .sendKeys("r")
                .sendKeys("t")
                .perform();

            intellisenseMenu = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-MMEditor-SMTPEditor-panel-suggestionbox")));
            allSuggestions = ui.getWebElements(intellisenseMenu, By.xpath("./div"));
            assertFalse(allSuggestions.isEmpty());
            assertTrue(allSuggestions.size() > 2);
            log.debug("Total count of suggestions: {}", allSuggestions.size());

            Collection<String> alertTexts = new TreeSet<>();
            for (WebElement el : allSuggestions) {
                alertTexts.add(el.getText());
            }
            assertTrue(alertTexts.contains("Alert_Name"));
            assertTrue(alertTexts.contains("Alert_State"));
            assertTrue(alertTexts.contains("Alert_Time"));
            assertTrue(alertTexts.contains("URL_Alert_Info"));
            assertTrue(alertTexts.contains("Previous_Alert_State"));

            WebElement alertSuggestion = allSuggestions.get(0);
            final String alertText = alertSuggestion.getText();
            ui.scrollIntoView(alertSuggestion);
            ui.clickButton(alertSuggestion, By.xpath(".//span[text()]"));
            ui.waitFor(ExpectedConditions
                .invisibilityOfAllElements(Collections.singletonList(alertSuggestion)));

            // Check that the text is there together with all the previous text.

            subjectText = subjecElement.getAttribute("value");
            log.debug("Current email subject of this action: >{}<", subjectText);
            line1 = line1 + "${" + alertText + "}";
            log.debug("Expected email subject text: >{}<", line1);
            pattern = Pattern.compile("^" + Pattern.quote(line1) + "$", DOTALL);
            matcher = pattern.matcher(subjectText);
            assertTrue(matcher.matches(),
                "The email subject of the action does not match the expected value.");
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(450982)
    public void smtpSummaryAlertTest() {
        final String hubHost = envProperties
            .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        final String expectedSubject = String.format(Locale.US,
            "CA APM Alert: %s in %s state", SUMMARY_ALERT_NAME, "Danger");
        final String expectedInBody1 = String.format(Locale.US,
            "Alert Name: %s", SUMMARY_ALERT_NAME);
        final String expectedInBody2 = String.format(Locale.US,
            "Alert Status: %s", "Danger");

        try (WiserSmtpServer wiser = new WiserSmtpServer(0);
             WebViewUi ui = WebViewUi.create(hubHost, prepareDesiredCapabilities())) {
            // Start local SMTP server.

            wiser.start();

            // Set up action so that email alerts start getting generated in rich text.

            ui.login(getWvUrl(), WEBVIEW_USER, WEBVIEW_PASSWORD);
            prepareEmailAction(ui, wiser.getHost(), wiser.getPort(), SIMPLE_ALERT_EMAIL_SUBJECT,
                SIMPLE_ALERT_EMAIL_BODY, TEST_FROM_EMAIL_ADDRESS_1, TEST_TO_EMAIL_ADDRESS_1, false,
                SUMMARY_ALERT_ACTION_TREE_NODE);

            // Wait for the email to arrive.

            wiser.waitForEmail(1, TimeUnit.MINUTES);

            // Disable email action so that it does not keep sending us emails.

            disableEmailAction(ui);

            // Parse the email and verify it.

            {
                List<WiserMessage> wiserMessageList = wiser.getMessages();
                assertEquals(wiserMessageList.size(), 1,
                    "We are expecting exactly one email message.");
                WiserMessage wiserMessage = wiserMessageList.get(0);
                verifyAlertEmailRichText(wiserMessage.getMimeMessage(), expectedSubject,
                    expectedInBody1, expectedInBody2);
            }

            // Clear delivered messages so that the next one is the first one.

            wiser.clearDeliverdMessages();

            // Prepare for plain text message.

            prepareEmailAction(ui, wiser.getHost(), wiser.getPort(), SIMPLE_ALERT_EMAIL_SUBJECT,
                SIMPLE_ALERT_EMAIL_BODY, TEST_FROM_EMAIL_ADDRESS_1, TEST_TO_EMAIL_ADDRESS_1, true,
                SUMMARY_ALERT_ACTION_TREE_NODE);

            // Wait for the email to arrive.

            wiser.waitForEmail(1, TimeUnit.MINUTES);

            // Disable email action so that it does not keep sending us emails.

            disableEmailAction(ui);

            // Parse the email and verify it.

            {
                List<WiserMessage> wiserMessageList = wiser.getMessages();
                assertEquals(wiserMessageList.size(), 1,
                    "We are expecting exactly one email message.");
                WiserMessage wiserMessage = wiserMessageList.get(0);
                verifyAlertEmailPlainText(wiserMessage.getMimeMessage(), expectedSubject,
                    expectedInBody1, expectedInBody2);
            }
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }

    @Tas(testBeds = @TestBed(name = WvNextgenTestbedNoCoda.class,
        executeOn = WvNextgenTestbedNoCoda.SELENIUM_GRID_MACHINE_ID),
        owner = "haiva01",
        size = SizeType.SMALL,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"BAT", "SMTP", "MANAGEMENT_TAB"})
    @AlmId(353514)
    public void smtpVerifyUniquenessCheckTest() {
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

            String actionName = "Unique action " + UUID.randomUUID().toString();
            actionName = createNewEmailAction(ui, actionName, false);

            // Try to create another new email sending action with the same name.

            createNewEmailAction(ui, actionName, false);

            // Wait for error dialog.

            WebElement errorDialog = ui.waitFor(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("webview-mmEditor-Element-ErrorMessage-AlertMessageBox")));

            WebElement errorDialogMessage = ui
                .getWebElement(errorDialog, By.id("webview-Common-AlertMessageBox-message"));
            final String errorDialogMessageText = errorDialogMessage.getText();
            log.debug("Message dialog text: >{}<", errorDialogMessageText);
            final String expectedText = String.format(Locale.US,
                "Could not create Action because the name \"%s\" is already in use.",
                actionName);
            log.debug("Expected text: >{}<", expectedText);
            ui.takeScreenShot();
            assertEquals(errorDialogMessageText, expectedText);
        } catch (Throwable e) {
            throw ErrorReport.logExceptionAndWrapFmt(log, e, "Test failed. Exception: {0}");
        }
    }
}
