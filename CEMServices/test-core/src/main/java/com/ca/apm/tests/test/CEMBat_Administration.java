package com.ca.apm.tests.test;

import static com.ca.apm.tests.cem.common.CEMConstants.EM_MACHINE_ID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.CEMChromeTestbed;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class CEMBat_Administration extends JBaseTest {


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

    public String type;
    public String authenticationType;
    public boolean caseSensitiveUrl;
    public boolean caseSensitiveLoginName;
    public String userProcessingType;
    public String appTimeOut;
    public String characterEncoding;
    public boolean inheritImpact;
    public String importXMLFileName;

    public CEMSystem system;

    public String testSuiteName;
    public String applicationName;
    public String applicationNameDefault;
    public String webFilterName;
    public String testSuiteServiceName;
    public String sessionName;
    public String businessTransactionNameMEDPhysician;
    public String businessTransactionNameMEDPatient = "MED Patient Login page";
    public String testSuiteServiceName2;

    public String EMAIL_ID;
    public String EMAIL_SMTP_HOST;
    public String JRE_DIR;

    {
        testSuiteName =
            testData.getString("TESSUI_Administration_BusinessApplication_TestSuiteName");
        applicationName =
            testSuiteName + testData.getString("TESSUI_Administration_BusinessApplication_Name");
        applicationNameDefault =
            testData.getString("TESSUI_Administration_BusinessApplication_DefaultApplication");
        webFilterName =
            testSuiteName
                + testData.getString("TESSUI_Administration_BusinessApplication_WebFilter");
        sessionName =
            testSuiteName
                + testData.getString("TESSUI_Administration_BusinessApplication_SessionName");

        type = testData.getString("TESSUI_Administration_BusinessApplication_Type");
        authenticationType =
            testData.getString("TESSUI_Administration_BusinessApplication_AuthenticationType");
        caseSensitiveUrl =
            Boolean.parseBoolean(testData
                .getString("TESSUI_Administration_BusinessApplication_CaseSensitiveUrl"));
        caseSensitiveLoginName =
            Boolean.parseBoolean(testData
                .getString("TESSUI_Administration_BusinessApplication_CaseSensitiveLoginName"));
        userProcessingType =
            testData.getString("TESSUI_Administration_BusinessApplication_UserProcessingType");
        appTimeOut = testData.getString("TESSUI_Administration_BusinessApplication_AppTimeOut");
        characterEncoding =
            testData.getString("TESSUI_Administration_BusinessApplication_CharacterEncoding");

        testSuiteServiceName =
            testSuiteName + testData.getString("TESSUI_Administration_BusinessService_Service");
        inheritImpact =
            Boolean.parseBoolean(testData
                .getString("TESSUI_Administration_BusinessService_InheritImpact"));
        testSuiteServiceName2 =
            testSuiteName + testData.getString("TESSUI_Administration_BusinessService_Service2");
        importXMLFileName = testData.getString("TESSUI_Administration_BusinessService_ImportXML");
        businessTransactionNameMEDPhysician =
            testData.getString("TESSUI_Administration_BusinessService_BusinessTransactionName");

        EMAIL_ID = environmentConstants.getString("emailId");
        EMAIL_SMTP_HOST = environmentConstants.getString("emailSMTPHost");
        JRE_DIR = environmentConstants.getString("JRE_Dir");
        EM_OS_TYPE = environmentConstants.getString("emOSType");

    }

    /* JAMSA07 - Adding username and password to verify the same */
    String userNameMail = environmentConstants.getString("userNameMail");
    String passwordMail = environmentConstants.getString("passwordMail");
    String fromAddressMail = environmentConstants.getString("fromAddressMail");
    String toAddressMail = environmentConstants.getString("toAddressMail");
    String hostNameValue = environmentConstants.getString("emailSMTPHost");;
    String portValue = environmentConstants.getString("emailPortValue");

    @BeforeClass
    public void initialize() {

        LOGGER.info("**********    Initializing in AdministrationBATTest *******");
        super.initialize();
        system = new CEMSystem(driver);
//        initDB();
        try {
            // login();
            setupMonitor.createMonitor(MONITOR_NAME, TIM_IP, TESS_HOST);
            try {
                setupMonitor.enableMonitor(MONITOR_NAME);
            } catch (Exception e) {
                setupMonitor.enableMonitor(MONITOR_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********     Successful Initializing in AdministrationBATTest    *******");
    }

    @Tas(testBeds = @TestBed(name = CEMChromeTestbed.class, executeOn = EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Parameters(value = {"QCID", "testname"})
    // Failing because the checkbox is seleted by default
    @Test(groups = {"BAT"}, description = "257006 - Email Settings-Default Value for Authentication Required", priority = 1)
    public void ADMN76_S_System_Email_AuthenticationRequiredField_check(
        @Optional("257006") String QCID,
        @Optional("ADMN76_S_System_Email_AuthenticationRequiredField_check") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN76_S_System_Email_AuthenticationRequiredField_check";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            logOut();
            login();
            system.goToEmailSetup();

            String authenticationRequiredInputCheckboxName = "requireAuthentication";
            boolean authFound = false;
            system.goToEmailSetup();
            if ((WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.authcheck")))
                && (WebdriverWrapper.isElementSelected(driver,
                    getORPropValue("cem.system.email.authcheck")))) authFound = true;

            assertTrue(!authFound, "Check field: " + authenticationRequiredInputCheckboxName);

            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "256999 - Email Settings-Default Form objects")
    public void ADMN68_S_System_Email_DefaultFields_Check(@Optional("256999") String QCID,
        @Optional("ADMN68_S_System_Email_DefaultFields_Check") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN68_S_System_Email_DefaultFields_Check";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            // test data
            String hostNameInputTextName = "smtpServerHostname";
            String portInputTextName = "smtpServerPort";
            String authenticationRequiredInputCheckboxName = "requireAuthentication";
            String userNameInputTextName = "smtpServerUsername";
            String passwordInputPasswordName = "smtpServerPassword";
            String subjectInputTextName = "subject";
            String bodyTextAreaName = "body";
            String footerTextAreaName = "footerText";
            String saveInputSubmitName = "save";
            boolean[] inputFieldsFound = {false, false, false, false, false, false, false, false};
            String testInputSubmitName = "test";
            String[] inputFieldsName =
                {hostNameInputTextName, portInputTextName, authenticationRequiredInputCheckboxName,
                        userNameInputTextName, passwordInputPasswordName, subjectInputTextName,
                        testInputSubmitName, saveInputSubmitName};
            boolean[] textareaFieldsFound = {false, false};
            String[] textareaFieldsName = {bodyTextAreaName, footerTextAreaName};
            system.goToEmailSetup();

            assertEquals(WebdriverWrapper.getElementText(driver,
                getORPropValue("cem.system.email.smtpLabel")), "SMTP Server Settings");
            assertEquals(WebdriverWrapper.getElementText(driver,
                getORPropValue("cem.system.email.emailLabel")), "Email Settings");
            int n = inputFieldsName.length;
            for (int i = 0; i < n; i++) {
                if (WebdriverWrapper.isObjectPresent(driver, "xpath_//input[@name='"
                    + inputFieldsName[i] + "']")) inputFieldsFound[i] = true;
            }
            for (int i = 0; i < n; i++) {
                assertTrue(inputFieldsFound[i] == true, "Check field: " + inputFieldsName[i]);
            }
            int m = textareaFieldsName.length;
            for (int i = 0; i < m; i++) {
                if (WebdriverWrapper.isObjectPresent(driver, "xpath_//textarea[@name='"
                    + textareaFieldsName[i] + "']")) textareaFieldsFound[i] = true;
            }
            for (int i = 0; i < m; i++) {
                assertTrue(textareaFieldsFound[i] == true, "Check field: " + textareaFieldsName[i]);
            }
            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "257012 - Email Settings-Enable Authentication Required-Blank Username")
    public void ADMN82_S_System_Email_BlankUserName_Check(@Optional("257012") String QCID,
        @Optional("ADMN82_S_System_Email_BlankUserName_Check") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN82_S_System_Email_BlankUserName_Check";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String hostNameInputTextValue = EMAIL_SMTP_HOST;
            String testPasswordValid = "quality";
            system.goToEmailSetup();

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.smtphostedit"),
                hostNameInputTextValue);
            if (!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.email.authcheck")))) {
                WebdriverWrapper.click(driver, getORPropValue("cem.system.email.authcheck"));
            }
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.usernameedit"), "");
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.passwordedit"),
                testPasswordValid);

            WebdriverWrapper.click(driver, getORPropValue("cem.system.email.testconnectionbtn"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorvalue")), "Check isElementPresent: "
                + getORPropValue("cem.system.email.testfailedusrpwdmsg"));
            assertTrue(WebdriverWrapper.verifyTextPresent(driver,
                getORPropValue("cem.system.email.msgerrorvalue"),
                getORPropValue("cem.system.email.testfailedusrpwdmsg")));

            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "257013 - Email Settings-Enable Authentication Required-Blank Password")
    public void ADMN83_S_System_Email_BlankPassword_Check(@Optional("257013") String QCID,
        @Optional("ADMN83_S_System_Email_BlankPassword_Check") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN83_S_System_Email_BlankPassword_Check";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String hostNameInputTextValue = EMAIL_SMTP_HOST;
            String testUsername = "tester";
            system.goToEmailSetup();

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.smtphostedit"),
                hostNameInputTextValue);
            if (!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.email.authcheck")))) {
                WebdriverWrapper.click(driver, getORPropValue("cem.system.email.authcheck"));
            }
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.usernameedit"),
                testUsername);
            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.passwordedit"), "");

            WebdriverWrapper.click(driver, getORPropValue("cem.system.email.testconnectionbtn"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorvalue")), "Check isElementPresent: "
                + getORPropValue("cem.system.email.testfailedusrpwdmsg"));
            assertTrue(WebdriverWrapper.verifyTextPresent(driver,
                getORPropValue("cem.system.email.msgerrorvalue"),
                getORPropValue("cem.system.email.testfailedusrpwdmsg")));

            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "257035 - Email Settings:Test connection with incorrect settings")
    public void ADMN108_S_System_Email_HostnameInvalid_Test(@Optional("257035") String QCID,
        @Optional("ADMN108_S_System_Email_HostnameInvalid_Test") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN108_S_System_Email_HostnameInvalid_Test";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String hostNameInputTextValueInvalid = "mail.ca.com1";
            system.goToEmailSetup();
            if (!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.email.authcheck")))) {
                WebdriverWrapper.click(driver, getORPropValue("cem.system.email.authcheck"));
            }

            WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.smtphostedit"),
                hostNameInputTextValueInvalid);
            WebdriverWrapper.click(driver, getORPropValue("cem.system.email.testconnectionbtn"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorvalue")), "Check isElementPresent: "
                + getORPropValue("cem.system.email.testfailedconnectmsg"));
            assertTrue(WebdriverWrapper.verifyTextPresent(driver,
                getORPropValue("cem.system.email.msgerrorvalue"),
                getORPropValue("cem.system.email.testfailedconnectmsg")));

            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "257033 - Email Settings:Validating correctness of mandatory fields")
    public void ADMN106_S_107_S_System_Email_HostnameHostname_Test(@Optional("257033") String QCID,
        @Optional("ADMN106_S_107_S_System_Email_HostnameHostname_Test") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN106_S_107_S_System_Email_HostnameHostname_Test";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

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

            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "257048 - Error (Failed to convert property) caused by Category link on Events Manager")
    public void ADMN181_S_System_Events_EventEmailNotification_Check_Modified(
        @Optional("257048") String QCID,
        @Optional("ADMN181_S_System_Events_EventEmailNotification_Check_Modified") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ADMN181_S_System_Events_EventEmailNotification_Check_Modified";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            system.goToEventsTab();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));

            if (!(WebdriverWrapper.isElementEnabled(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")))) {
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
                WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.usernameedit"),
                    userNameMail);
                WebdriverWrapper.inputText(driver, getORPropValue("cem.system.email.passwordedit"),
                    passwordMail);

                WebdriverWrapper
                    .click(driver, getORPropValue("cem.system.email.testconnectionbtn"));
                WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
                Thread.sleep(10000);
            }

            login();
            LOGGER.info("The Current page is: " + driver.getCurrentUrl());
            system.goToEventsTab();
            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.eventmanagerlink"));
            assertTrue(WebdriverWrapper.isElementEnabled(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")));

            if (!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("cem.system.events.eventemailnotificationcheck"));
            }

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.events.eventemailnotificationlabel")));
            assertTrue(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")));

            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.emnamelink"));
            assertTrue(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")));

            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.emstatuslink"));
            assertTrue(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")));

            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.emseveritylink"));
            assertTrue(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")));

            WebdriverWrapper.click(driver, getORPropValue("cem.system.events.emcategorylink"));
            assertTrue(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.system.events.eventemailnotificationcheck")));

            endTestCaseName(testCaseName);
            tearDownTest(testCaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }

    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "256970 - IAE when trying to set Transaction Time SLA to a non-integer value")
    public void ADMN23_S_Admin_BS_BusinessTransaction_Search(@Optional("256970") String QCID,
        @Optional("ADMN23_S_Admin_BS_BusinessTransaction_Search") String testname)
        throws InterruptedException {
        try {
            String testcaseName = "ADMN23_S_Admin_BS_BusinessTransaction_Search";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            // test data //system save 86400.000 for 86400, save .000 for 0.
            final String minValue = "0";
            final String maxValue = "86400.000";
            final String negMinValue = "-0.001";
            final String negMaxValue = "86400.001";

            // String testSuiteServiceName = "physicianBS";
            // String businessTransactionNameMEDPhysician = "test";

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + testSuiteServiceName);
            WebdriverWrapper.click(driver, getORPropValue("linkText")
                + businessTransactionNameMEDPhysician);
            WebdriverWrapper.click(driver, getORPropValue("admin.ug.generaltab"));

            // save negMaxValue
            if (WebdriverWrapper.isElementSelected(driver,
                getORPropValue("administration.businessservice.bsInheritTransactionTime"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("administration.businessservice.bsInheritTransactionTime"));
            }
            WebdriverWrapper
                .inputText(driver,
                    getORPropValue("administration.businessservice.bsTransactionTimeEdit"),
                    negMaxValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorlable")),
                "Check isElementPresent: isClassErrorLableElementPresent.");
            assertEquals(WebdriverWrapper.getElementText(driver,
                getORPropValue("cem.system.email.msgerrorvalue")),
                "Transaction Time SLA is not in the range 0 through 86,400.");

            // save maxValue
            if (WebdriverWrapper.isElementSelected(driver,
                getORPropValue("administration.businessservice.bsInheritTransactionTime"))) {
                WebdriverWrapper.click(driver,
                    getORPropValue("administration.businessservice.bsInheritTransactionTime"));
            }
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessservice.bsTransactionTimeEdit"), maxValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertFalse(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorlable")),
                "Check isElementPresent: isClassErrorLableElementPresent.");

            // save maxValue - check maxValue
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.businessService.btSerach"));
            WebdriverWrapper.click(driver, getORPropValue("button.search"));
            WebdriverWrapper.click(driver, getORPropValue("linkText")
                + businessTransactionNameMEDPhysician);
            WebdriverWrapper.click(driver, getORPropValue("admin.ug.generaltab"));

            assertTrue(!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("administration.businessservice.bsInheritTransactionTime"))),
                "Verify - not checked");
            assertTrue(
                WebdriverWrapper
                    .getAttribute(driver,
                        getORPropValue("administration.businessservice.bsTransactionTimeEdit"),
                        "value").equals(maxValue), "Verify - equals Max Value Set.");

            // save negMinValue
            WebdriverWrapper
                .inputText(driver,
                    getORPropValue("administration.businessservice.bsTransactionTimeEdit"),
                    negMinValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorlable")),
                "Check isElementPresent: isClassErrorLableElementPresent.");
            assertEquals(WebdriverWrapper.getElementText(driver,
                getORPropValue("cem.system.email.msgerrorvalue")),
                "Transaction Time SLA is not in the range 0 through 86,400.");

            // save minValue
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.businessservice.bsTransactionTimeEdit"), minValue);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            assertFalse(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.system.email.msgerrorlable")));

            // save minValue - check minValue
            WebdriverWrapper.click(driver, getORPropValue("linkText")
                + businessTransactionNameMEDPhysician);
            WebdriverWrapper.click(driver, getORPropValue("admin.ug.generaltab"));
            assertTrue(!(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("administration.businessservice.bsInheritTransactionTime"))),
                "Verify - not checked");
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("administration.businessservice.bsTransactionTimeEdit"), "value"),
                ".000", "Verify - equals bsAdminBServiceBTGeneralField_tranTimeSlaAsString.");

            endTestCaseName(testcaseName);
            tearDownTest(testcaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "256982 - Creating new param with type HTTP Header gives error")
    public void ADMN45_S_Admin_BS_ComponentIDentificatonField_Create(
        @Optional("256982") String QCID,
        @Optional("ADMN45_S_Admin_BS_ComponentIDentificatonField_Create") String testname)
        throws InterruptedException {
        try {
            String testcaseName = "ADMN45_S_Admin_BS_ComponentIDentificatonField_Create";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            // String testSuiteServiceName = "physicianBS";
            // String businessTransactionNameMEDPhysician = "test";

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + testSuiteServiceName);
            WebdriverWrapper.click(driver, getORPropValue("linkText")
                + businessTransactionNameMEDPhysician);
            WebdriverWrapper.click(driver, getORPropValue("administration.busApp.serviceLink"));
            WebdriverWrapper.click(driver, getORPropValue("administration.busApp.transLink"));
            WebdriverWrapper.click(driver,
                getORPropValue("administration.busApp.identifcationLink"));
            WebdriverWrapper.click(driver, getORPropValue("button.new"));

            // // toDo add delete before input same.
            String Name = "Content-Language" + Math.round(Math.random() * 100);
            WebdriverWrapper.selectBox(driver,
                getORPropValue("administration.component.parameter.bsCompTypeSelect"),
                getORPropValue("administration.autogen.autogenHttpReqHeaderTxt"));
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.component.parameter.bsCompNameEdit"), Name);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.component.parameter.bsCompPatternEdit"), "en");
            WebdriverWrapper.click(driver, getORPropValue("button.save"));

            // verify saved.
            assertTrue(admin.getRowNumByContentAndColumn("paramDef", Name, 2) > 0, "Check row.");
            endTestCaseName(testcaseName);
            tearDownTest(testcaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "256964 - IT value and User Minute cost not updated")
    public void ADMN16_S_Setup_Domain_DomainITValue_Update(@Optional("256964") String QCID,
        @Optional("ADMN16_S_Setup_Domain_DomainITValue_Update") String testname)
        throws InterruptedException {
        try {
            String testcaseName = "ADMN16_S_Setup_Domain_DomainITValue_Update";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.domain"));
            WebdriverWrapper.inputText(driver, getORPropValue("setup.domain.itvalue"), "0.25");
            WebdriverWrapper
                .inputText(driver, getORPropValue("setup.domain.userforminute"), "1.55");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));

            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("setup.domain.itvalue"), "value"), "0.25");
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("setup.domain.userforminute"), "value"), "1.55");

            endTestCaseName(testcaseName);
            tearDownTest(testcaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "256961 - Disabling and enabling a BusinessTransaction doesn`t trigger a synch notification")
    public void ADMN09_S_Admin_BS_BTMonitorsSynchronized_Trigger(@Optional("256961") String QCID,
        @Optional("ADMN09_S_Admin_BS_BTMonitorsSynchronized_Trigger") String testname)
        throws InterruptedException {
        try {
            String testcaseName = "ADMN09_S_Admin_BS_BTMonitorsSynchronized_Trigger";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            // String testSuiteServiceName = "physicianBS";
            // String businessTransactionNameMEDPhysician = "test";

            setup.synchronizeAllMonitors();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + testSuiteServiceName);
            WebdriverWrapper.click(driver, "xpath_//table[@id='tranSetDef']//a[text()='"
                + businessTransactionNameMEDPhysician + "']//../../td[1]/input");
            WebdriverWrapper.click(driver, getORPropValue("button.disable"));
            if (WebdriverWrapper.isAlertPresent(driver)) {
                WebdriverWrapper.selectPopUp(driver, "accept");
            }

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.monitors.syncMessage")));
            setup.synchronizeAllMonitors();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + testSuiteServiceName);
            WebdriverWrapper.click(driver, "xpath_//table[@id='tranSetDef']//a[text()='"
                + businessTransactionNameMEDPhysician + "']//../../td[1]/input");
            WebdriverWrapper.click(driver, getORPropValue("button.enable"));

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.monitors.syncMessage")));
            // go to Business Transaction Specifications page.
            setup.synchronizeAllMonitors();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + testSuiteServiceName);
            WebdriverWrapper.click(driver, getORPropValue("linkText")
                + businessTransactionNameMEDPhysician);
            WebdriverWrapper.click(driver,
                getORPropValue("administration.businessservice.BusinessTransSpec"));

            // click enable
            WebdriverWrapper.click(driver,
                getORPropValue("administration.businessservice.slowTimeDefectInput"));
            WebdriverWrapper.click(driver, getORPropValue("button.enable"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.monitors.syncMessage")));
            // click disable
            setup.synchronizeAllMonitors();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.businessservices"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + testSuiteServiceName);
            WebdriverWrapper.click(driver, getORPropValue("linkText")
                + businessTransactionNameMEDPhysician);
            WebdriverWrapper.click(driver,
                getORPropValue("administration.businessservice.BusinessTransSpec"));;

            WebdriverWrapper.click(driver,
                getORPropValue("administration.businessservice.slowTimeDefectInput"));
            WebdriverWrapper.click(driver, getORPropValue("button.disable"));
            if (WebdriverWrapper.isAlertPresent(driver)) {
                WebdriverWrapper.selectPopUp(driver, "accept");
            }
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.monitors.syncMessage")));

            endTestCaseName(testcaseName);
            tearDownTest(testcaseName);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    // //may move to ShareObject class
    public Integer getRowCountForCellLocator(String attributeofTable, String gridName,
        String cellLocator) throws Exception {
        Integer rows = 1;
        Integer nextRow = rows + 1;

        String gridLocator = "//table[@" + attributeofTable + "='" + gridName + "']";
        String gridCellLocatorStart = "/tbody" + cellLocator + "[";
        String gridCellLocatorEnd = "]";
        while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath") + gridLocator
            + gridCellLocatorStart + rows + gridCellLocatorEnd)) {
            rows++;
            nextRow++;
        }
        return rows;
    }

    private void setupBSTSData() throws Exception {

        // Use utf-8 as encoding.
        admin.createBusinessApplication(applicationName, applicationName, type, authenticationType,
            caseSensitiveUrl, caseSensitiveLoginName, userProcessingType, appTimeOut,
            characterEncoding);

        // Create bs
        admin.createBusinessService(testSuiteServiceName, testSuiteServiceName, applicationName,
            inheritImpact, "");
        admin.createBusinessService(testSuiteServiceName2, testSuiteServiceName2, applicationName,
            inheritImpact, "");
        admin.importBusinessTranXML(testSuiteServiceName, file.getTestDataFullPath(
            CEMGeneralApplication.testSuiteNameDefault, "BTExport_PhysicianLogin.zip"));
        setupMonitor.syncMonitors();
        // setup.synchronizeAllMonitors();

        LOGGER.info("setupBSTSData - Done.");
    }

//    @BeforeTest(alwaysRun = true)
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
