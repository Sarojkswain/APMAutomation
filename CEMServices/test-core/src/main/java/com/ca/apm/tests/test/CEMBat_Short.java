package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.openqa.selenium.By;
import org.testng.annotations.*;
import org.testng.annotations.Test;

public class CEMBat_Short extends JBaseTest {

    public String appURL;
    public String bTransactionName;
    public long ttDurationInSec = 1800;
    long MODERATE_INCIDENT_TTHRESHOLD = 1000;
    long SEVERE_INCIDENT_TTHRESHOLD = 2500;
    long CRITICAL_INCIDENT_TTHRESHOLD = 5000;
    long DEFECT_MEDIUM_IMPACT_LEVEL = 64;
    int impactLevelTHCol = 4;
    int businessImpactTHCol = 5;
    int reportsTotalDefectsTHCol = 4;
    int reportsIntervalTHCol = 1;
    String slowTimeDefect = "Slow Time";
    String highThroughputDefect = "High Throughput";

    String autogenUI = "Transaction Discovery";
    String busiServices = "Business Services";
    String autogenBS = "Discovered Transactions";
    String templateName = "IIS_PetShop_Template";
    String appDefName = "Avitek Application";

    String userNameMail = environmentConstants.getString("userNameMail");
    String passwordMail = environmentConstants.getString("passwordMail");
    String fromAddressMail = environmentConstants.getString("fromAddressMail");
    String toAddressMail = environmentConstants.getString("toAddressMail");
    String hostNameValue = environmentConstants.getString("emailSMTPHost");;
    String portValue = environmentConstants.getString("portValue");
    
   /* String userNameMail="jamsa@cem.com";
    String passwordMail="quality";
    String fromAddressMail="Technicalsupport@ca.com";
    String toAddressMail="jamsa@cem.com";
    String hostNameValue = "bilsa02-test.ca.com";
    String portValue = "25";*/
    
    @BeforeClass
    public void initialize() {
        LOGGER.info("**********   Initializing in CEM BAT Short *******");
        super.initialize();
        appURL = getEnvConstValue("appURL");
        bTransactionName = getEnvConstValue("bTransactionName");
        CEMSystem system = new CEMSystem(driver);
        try {
            logIn();
            initDB();

            system.goToEmailSetup();
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.portedit"),
                portValue);
            if (!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.email.authcheck")))) {
                WebdriverWrapper.click(driver, getORPropValue("cem.system.email.authcheck"));
            }

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.fromnameedit"),
                fromAddressMail);
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.tonameedit"),
                toAddressMail);
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.smtphostedit"),
                hostNameValue);
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.usernameedit"),
                userNameMail);
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.passwordedit"),
                passwordMail);
            WebdriverWrapper.click(driver, getORPropValue("cem.system.email.testconnectionbtn"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgdatavalue")), "Check isElementPresent: "
                + getORPropValue("cem.system.email.testsuccessmsg"));
            assertTrue(WebdriverWrapper.verifyTextPresent(driver,
                getORPropValue("cem.system.email.msgdatavalue"),
                getORPropValue("cem.system.email.testsuccessmsg")));
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********  Successful Initializing in CEM BAT Short *******");
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248601 - Event Navigation")
    public void EVPR06_S(@Optional("248601") String QCID, @Optional("EVPR06_S") String testname) {
        try {
            String testCaseName = "EVPR06_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            assertTrue(WebdriverWrapper.isElementDisplayed(driver,
                getORPropValue("cem.system.events")));
            assertTrue(WebdriverWrapper.isElementDisplayed(driver,
                getORPropValue("cem.system.events.eventmanagerlink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventseveritythLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventdateandtimethLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventnamethLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventcategorythLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventsourcethLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventsourceipaddressthLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventdescriptionthLabel")));
            logOut();
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }

    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248606 - Event Manager Navigation", priority = 1)
    public void EVPR11_S(@Optional("248606") String QCID, @Optional("EVPR11_S") String testname) {
        try {
            String testCaseName = "EVPR11_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String AgedefaultValue = "7";
            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.deleteEventsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.EmailNotificationLabel")));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.maxEventsInput"), "value"), AgedefaultValue);
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.edit.save")));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248607 - Assign different values to Delete Events After field")
    public void EVPR12_S(@Optional("248607") String QCID, @Optional("EVPR12_S") String testname) {
        try {
            String testCaseName = "EVPR12_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String firstValue = "10";
            String secondValue = "100";
            String thirdValue = "0";
            String defaultValue = "7";
            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                firstValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.maxEventsInput"), "value"), firstValue);

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                secondValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.maxEventsInput"), "value"), secondValue);

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                thirdValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.maxEventsInput"), "value"), thirdValue);

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                defaultValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248657 -  Assign invalid value to Delete Events After field")
    public void EVPR16_S(@Optional("248657") String QCID, @Optional("VPR16_S") String testname) {
        try {
            String testCaseName = "EVPR16_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String errorMessage = "Delete Events After is not in the range 0 through 24,855.";
            String specCharErrMessage =
                "One or more request parameters are invalid, please resubmit the request with valid parameters.";
            String firstValue = "-10";
            String secondValue = "1a#&*";
            String thirdValue = "25000";
            String defaultValue = "7";

            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                firstValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertEquals(admin.getMessageFromErrorDiv(), errorMessage);

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                secondValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            WebdriverWrapper.isObjectPresent(driver, "xpath_//td[contains(text(),'"
                + specCharErrMessage + "')]");
            WebdriverWrapper.click(driver, getORPropValue("button.back"));

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                thirdValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertEquals(admin.getMessageFromErrorDiv(), errorMessage);

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.maxEventsInput"),
                defaultValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248612 -  Event Mail Notification checkbox")
    public void EVPR26_S(@Optional("248612") String QCID, @Optional("EVPR26_S") String testname) {
        try {
            String testCaseName = "EVPR26_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            if (!WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.NotificationScheduledChkBox"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("cem.system.events.NotificationScheduledChkBox"));
            }

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.EventNotificationSettingsLabel")));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }

    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248613 -  Mail Notification-Rule Setting options (UI)")
    public void EVPR27_S(@Optional("248613") String QCID, @Optional("EVPR27_S") String testname) {
        try {
            String testCaseName = "EVPR27_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            if (!WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.NotificationScheduledChkBox"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("cem.system.events.NotificationScheduledChkBox"));
            }
            // JAMSA07
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.EventTyepNotificationLabel")));

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.nameLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.statusLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.severityLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.categoryLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.sourceIPLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.fromIPLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.toiPLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.emailSettingsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.fromAddressLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.fromNameLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.toLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.subjectLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.eventMgr.messageLabel")));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "248614 - Mail Notification-Rule Setting options (UI validations)")
    public void EVPR28_S(@Optional("248614") String QCID, @Optional("EVPR28_S") String testname) {
        try {
            String testCaseName = "EVPR28_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            if (!WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.NotificationScheduledChkBox"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("cem.system.events.NotificationScheduledChkBox"));
            }

            assertTrue(WebdriverWrapper.isTextInSource(driver, "User group limit reached"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "TIM restarted"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "TIM deleted some of its data files because disk space was low"));
            assertTrue(WebdriverWrapper
                .isTextInSource(driver, "TIM Collection Service has stopped"));
            assertTrue(WebdriverWrapper
                .isTextInSource(driver, "TIM Collection Service has started"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Stats Aggregation Service has stopped"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Stats Aggregation Service has started"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "No network data"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Logins dropped"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Limit exceeded adding new components"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "HTTP Analyzer plug-in error"));

            String str =
                WebdriverWrapper.getElement(driver,
                    getORPropValue("cem.system.events.ErrorsProcessingLabel")).getText();
            assertTrue(str
                .contains("Errors processing statistics file(s) test. See EM log for details."));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Enterprise Manager restarted"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Enterprise Manager cannot reach TIM"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Duplicate session ids detected"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Discovered transactions dropped"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Defects dropped"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Database Cleanup Service has stopped"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Database Cleanup Service has started"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "CA APM TG Agent is up"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "CA APM TG Agent is down"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Business application name is not valid"));

            // Check categories are listed
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Processing"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Administration"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Communications"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "CA APM TG"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Monitoring"));

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432804 - Mail Notification-Rule Setting#Event Name-TESS and TIM restarted")
    public void EVPR29a_S(@Optional("432804") String QCID, @Optional("EVPR29a_S") String testname) {
        try {
            String testCaseName = "EVPR29a_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            if (!WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.NotificationScheduledChkBox"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("cem.system.events.NotificationScheduledChkBox"));
            }

            if (WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.headerCheckbox"))) {
                WebdriverWrapper.click(driver, getORPropValue("cem.system.headerCheckbox"));
            }
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            WebdriverWrapper.click(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[1]/td[1]/input[@id='idList']");
            WebdriverWrapper.click(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[12]/td[1]/input[@id='idList']");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertEquals(WebdriverWrapper.getElementText(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[1]/td[3]"), "Enabled");
            assertEquals(WebdriverWrapper.getElementText(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[12]/td[3]"), "Enabled");

            WebdriverWrapper.click(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[1]/td[1]/input[@id='idList']");
            WebdriverWrapper.click(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[12]/td[1]/input[@id='idList']");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));

            assertEquals(WebdriverWrapper.getElementText(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[1]/td[3]"), "Disabled");
            assertEquals(WebdriverWrapper.getElementText(driver,
                "xpath_//table[@id='eventTypeListId']/tbody/tr[12]/td[3]"), "Disabled");

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "EVPR29b_S")
    public void EVPR29b_S(@Optional("451151") String QCID, @Optional("EVPR29b_S") String testname) {
        try {
            String testCaseName = "EVPR29b_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            if (!WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.NotificationScheduledChkBox"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("cem.system.events.NotificationScheduledChkBox"));
            }
            assertTrue(WebdriverWrapper.getAttribute(driver, "name_from", "value").trim()
                .contains("cemadmin"));
            assertTrue(WebdriverWrapper.getAttribute(driver, "name_subject", "value").contains(
                "CA CEM Event"));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "This email has been sent to you by the CA CEM Events Engine."));

            WebdriverWrapper.inputText(driver, "name_fromAddress", "EVPR_29b");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            String errorMessage = admin.getMessageFromErrorDiv();
            assertTrue(errorMessage.equals("EVPR_29b is an invalid email address."));

            WebdriverWrapper.inputText(driver, "name_to", "Admin");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            String errorMessage2 = admin.getMessageFromErrorDiv();
            assertTrue(errorMessage2.equals("Admin is an invalid email address."));

            WebdriverWrapper.inputText(driver, "name_fromAddress", "wilytech@cem.com");
            WebdriverWrapper.inputText(driver, "name_from", "cemadmin@quality");
            WebdriverWrapper.inputText(driver, "name_to", "jamsa@cem.com");
            WebdriverWrapper.inputText(driver, "name_subject", "CA APM CEM Event via email");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));

            assertEquals(WebdriverWrapper.getAttribute(driver, "name_fromAddress", "value").trim(),
                "wilytech@cem.com");
            assertEquals(WebdriverWrapper.getAttribute(driver, "name_from", "value").trim(),
                "cemadmin@quality");
            assertEquals(WebdriverWrapper.getAttribute(driver, "name_to", "value").trim(),
                "jamsa@cem.com");
            assertEquals(WebdriverWrapper.getAttribute(driver, "name_subject", "value").trim(),
                "CA APM CEM Event via email");

            WebdriverWrapper.click(driver,
                getORPropValue("cem.system.events.NotificationScheduledChkBox"));
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432794 - testDomainSettings")
    public void testDomainSettings(@Optional("432794") String QCID,
        @Optional("testDomainSettings") String testname) {

        try {
            String testCaseName = "testDomainSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.edit.save")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.DomainSettinsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.domainNameLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.domainInput")));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.domain.domainInput"), "value"), "Local Domain");
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.domainCaptureLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.captureDetails")));
            String str =
                driver.findElement(By.xpath(getORPropValue("cem.system.domain.troubleshootLabel")))
                    .getText().replaceAll("\\n", " ");
            assertEquals(str, "Troubleshoot Defects by IP Subnet:");
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.fileterDefects")));

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }

    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432798 - verifyDataRetentionSettings")
    public void verifyDataRetentionSettings(@Optional("432798") String QCID,
        @Optional("verifyDataRetentionSettings") String testname) {
        try {
            String testCaseName = "verifyDataRetentionSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.DataRetenionSettingsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.makeuserLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.keepHourlyLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.keepyDailyLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.keepWeeklyLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.keepHourlyUserLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.keepDailyUserLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.keepWeeklyUserLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.inactivityTimeout")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.intervalsStats")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.dailyStats")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.weeklyStats")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.userGroupInterval")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.userGroupDailyStats")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.userGroupWeeklyStats")));

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432801 - verifyDefaultTransactionSettings")
    public void verifyDefaultTransactionSettings(@Optional("432801") String QCID,
        @Optional("verifyDefaultTransactionSettings") String testname) {
        try {
            String testCaseName = "verifyDefaultTransactionSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.DefaultTransactionLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.impactLevelLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.successRateLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.sigmaLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.transactionTimeLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.tranImportance")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.tranSuccessRateSla")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.tranSigmaSla")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.tranTranTimeSlaAsString")));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.domain.tranSuccessRateSla"), "value"), "95.0");
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.domain.tranSigmaSla"), "value"), "4.0");
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.system.domain.tranTranTimeSlaAsString"), "value"), "8.000");

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432799 - verifyIdentificationSettings")
    public void verifyIdentificationSettings(@Optional("432799") String QCID,
        @Optional("verifyIdentificationSettings") String testname) {
        try {
            String testCaseName = "verifyIdentificationSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.idSettingsLabel")));
            String str =
                driver.findElement(By.xpath(getORPropValue("cem.system.domain.ignoreAppsInUser")))
                    .getText().replaceAll("\\n", " ");
            assertEquals(str, getORPropValue("cem.system.domain.ignoreAppsText"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.ignoreAppsCheckBox")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.pathParamsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.pathParamDelimiters")));

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432802 - verifyLogin")
    public void verifyLogin(@Optional("432802") String QCID,
        @Optional("verifyLogin") String testname) {
        try {
            String testCaseName = "verifyLogin";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            assertTrue(WebdriverWrapper
                .isObjectPresent(driver, getORPropValue("cem.cemAdminLable")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("cem.logOut")));
            logOut();

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432803 - verifyUserGroupSettings")
    public void verifyUserGroupSettings(@Optional("432803") String QCID,
        @Optional("verifyUserGroupSettings") String testname) {
        try {
            String testCaseName = "verifyUserGroupSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.userGrpSettingsLabel")));
            String str =
                driver.findElement(By.xpath(getORPropValue("cem.system.domain.grpNewUserByIP")))
                    .getText().replaceAll("\\n", " ");
            assertEquals(str, getORPropValue("cem.system.domain.createUsrGrpByIPText"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.grpNewUserByIPSubnet")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.grpNewUserByIPSubnetEnable")));

            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432800 - verifyValueSettings")
    public void verifyValueSettings(@Optional("432800") String QCID,
        @Optional("verifyValueSettings") String testname) {
        try {
            String testCaseName = "verifyValueSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.ValueSettingLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.domain.calITValue")));
            String str =
                driver.findElement(By.xpath("//label[@for='itValue']")).getText()
                    .replaceAll("\\n", " ");
            assertEquals(str, getORPropValue("cem.system.domain.itValueText"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                "xpath_//label[text()='Calculate Incident Cost:']"));
            str =
                driver.findElement(By.xpath("//label[@for='userMinuteCost']")).getText()
                    .replaceAll("\\n", " ");
            assertEquals(str, "Incident Cost per User per Minute:");
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "id_calculateItValue"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "id_calculateItValue"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "id_itValue"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "id_calculateIncidentCost"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "id_userMinuteCost"));

            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432797 - verifyAllCEMTabs")
    public void verifyAllCEMTabs(@Optional("432797") String QCID,
        @Optional("verifyAllCEMTabs") String testname) {
        try {
            String testCaseName = "verifyAllCEMTabs";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            // Verify Systems tab
            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.system"),
                getORPropValue("cem.system.databasesettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.databasesettings")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.system"),
                getORPropValue("cem.system.emailsettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.emailsettings")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.system"),
                getORPropValue("cem.system.events"));
            assertTrue(WebdriverWrapper
                .isObjectPresent(driver, getORPropValue("cem.system.events")));

            // Verify Security Tab
            security.goToPrivateParametersTab();
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.privateparam")));
            security.goToFIPSSettingsSetup();
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.fipssettings")));

            // Verify Setup tab
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("setup.domain")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.monitors"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("setup.monitors")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.services"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("setup.services")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.webserverfilters"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.webserverfilters")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.incidentsettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.incidentsettings")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.httpssettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.plugins"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("setup.plugins")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.introscopesettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.introscopesettings")));

            // Verify Administration tab
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.overview"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.overview")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessapplication"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.businessapplication")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.businessservices")));
            WebdriverWrapper.click(driver, getORPropValue("administration.specifications"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.specifications")));
            WebdriverWrapper.click(driver, getORPropValue("administration.usergroups"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.usergroups")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.correlationslas"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.correlationslas")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.recordingsessions"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.recordingsessions")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.transactiondiscovery"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.transactiondiscovery")));

            // Verify Tools tab
            WebdriverWrapper.click(driver, getORPropValue("home.tools"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("tools.scriptRecorder")));

            // Verify CEM tab
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
                getORPropValue("cem.servicelevelManagement"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.servicelevelManagement")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
                getORPropValue("cem.incidentmanagemnt"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.incidentmanagemnt")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
                getORPropValue("cem.performancereports"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.performancereports")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
                getORPropValue("cem.qualityreports"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.qualityreports")));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.cem"),
                getORPropValue("cem.analysisgraphs"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.analysisgraphs")));
            WebdriverWrapper.click(driver, getORPropValue("cem.exportdata"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("cem.exportdata")));
            WebdriverWrapper.click(driver, getORPropValue("cem.myreports"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("cem.myreports")));
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }



    @BeforeTest(alwaysRun = true)
    public void login() {
        try {
            logIn();
        } catch (Exception e) {
            LOGGER.info("[ERROR]Login to CEM UI failed");
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @AfterClass()
    public void close() {
        driver.quit();
        Util.runOSCommand("cmd.exe /c taskkill /im " + getEnvConstValue("browser") + "* -F");
    }
}
