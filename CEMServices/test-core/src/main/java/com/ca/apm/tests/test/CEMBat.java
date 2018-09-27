package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.testng.annotations.*;

import java.net.URL;
import org.testng.annotations.Test;

public class CEMBat extends JBaseTest {

    private String businessService = "physicianBS";
    private String appName = "Medrec";
    private String btImportFile;
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

    private String testSuiteName = "FilterSmokeTest";
    private CemReports cemReports;

    @BeforeClass
    public void initialize() {
        LOGGER.info("**********   Initializing in CEM BAT  *******");
        super.initialize();
        appURL = getEnvConstValue("appURL");
        btImportFile = getEnvConstValue("btImportFile");
        bTransactionName = getEnvConstValue("bTransactionName");
        try {
            logIn();
            setupMonitor.createMonitor(MONITOR_NAME, TIM_IP, TESS_HOST);
            try {
                setupMonitor.enableMonitor(MONITOR_NAME);
            } catch (Exception e) {
                setupMonitor.enableMonitor(MONITOR_NAME);
            }
            /*
             * setup.createMonitor(MONITOR_NAME, TIM_IP);
             * setup.enableMonitor(MONITOR_NAME);
             */
            cemReports = new CemReports(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********  Successful Initializing in CEM BAT  *******");
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432808 - testWebServerFilterTab")
    public void testWebServerFilterTab(@Optional("432808") String QCID,
        @Optional("testWebServerFilterTab") String testname) {
        try {
            String testCaseName = "testWebServerFilterTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            setupMonitor.createMonitor(MONITOR_NAME, TIM_IP, TESS_HOST);
            try {
                setupMonitor.enableMonitor(MONITOR_NAME);
            } catch (Exception e) {
                setupMonitor.enableMonitor(MONITOR_NAME);
            }
            /*
             * setup.createMonitor(MONITOR_NAME, TIM_IP);
             * setup.enableMonitor(MONITOR_NAME);
             */
            String webFilterName = testSuiteName + "Filter";
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.webserverfilters"));

            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.new")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.delete")));
            setup.createWebFilter(webFilterName, MONITOR_NAME, MED_REC_HOST_IP_LANG);

            assertTrue(WebdriverWrapper.isObjectPresent(driver, "xpath_//a[contains(text(),'"
                + webFilterName + "')]"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "xpath_//*[contains(text(),'"
                + MONITOR_NAME + "')]"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, "xpath_//*[contains(text(),'"
                + MED_REC_HOST_IP_LANG + "')]"));
            setup.deleteWebFilter(webFilterName);
            assertFalse(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")
                + webFilterName));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432805 - testIncidentSettingsTab")
    public void testIncidentSettingsTab(@Optional("432805") String QCID,
        @Optional("testIncidentSettingsTab") String testname) {
        try {
            String testCaseName = "testIncidentSettingsTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.incidentsettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.name.save")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.IncidentManagementSettingsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.evaluationIntervalEdit")));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.security.evaluationIntervalEdit"), "value"), "5");
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.deleteDefectsAfterEdit")));
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.security.deleteDefectsAfterEdit"), "value"), "7");
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.IncidentGeneratingRulesLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.IncidentGenerateIncidentLabel1")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.IncidentGenerateIncidentLabel2")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.IncidentGenerateIncidentLabel3")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.numDefectsPerIntervalEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.percentIncreaseAmountEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.numSustainedDefectEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.minSustainedIntervalsEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.totalSustainedIntervalsEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.simpleDefectRateEnableCheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.percentIncreaseEnableCheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.sustainedDefectRateEnableCheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.ImpactSettingsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.moderateSevertityEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.severeSeverityEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.criticalSeverityEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.EvidenceCollectionLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidenceonopencheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidenceonopenedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidencemoderatecheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidencemoderateedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidenceseverecheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidencesevereedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidencecriticalcheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.collectevidencecriticaledit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.is.IncidentAgeOutLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ageoutpendingminutesafterdefectcheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ageoutminutessincelastdefectedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.minSincePending")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ageoutifpendingminutescheck")));
            assertTrue(WebdriverWrapper
                .isObjectPresent(
                    driver,
                    getORPropValue("cem.security.ageouageoutifpendingminutessinceedittunsustainedcheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ageoutunsustainednumperintervaledit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ageoutunsustainedmaxintervalsedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ageoutunsustainedtotalintervalsedit")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432807 - testHTTPSSettingsTab")
    public void testHTTPSSettingsTab(@Optional("432807") String QCID,
        @Optional("testHTTPSSettingsTab") String testname) {
        try {
            String testCaseName = "testHTTPSSettingsTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.httpssettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.name.save")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.deleteAllPrivateKeysBtn")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.HTTPSSettings")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.AddressType")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.AddressType1")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.webServerport")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.privateKeyFileEdit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.keyFilePassPhrase")));

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.confirmPassPhrase")));
            assertTrue(WebdriverWrapper.isSelected(driver,
                getORPropValue("setup.httpssettings.IPAddressRadioBtn")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.webServeripaddressrange")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.IPAddressRangeLabel")));
            assertFalse(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.MACAddressLabel")));
            assertFalse(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.webServerMACaddressrange")));
            WebdriverWrapper.clickUsingXpath(driver,
                getORPropValue("setup.httpssettings.MacAddressRadioBtn"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.httpssettings.MACAddressLabel")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432809 - testPluginsSettingsTab")
    public void testPluginsSettingsTab(@Optional("432809") String QCID,
        @Optional("testPluginsSettingsTab") String testname) {
        try {
            String testCaseName = "testPluginsSettingsTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.plugins"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.plugins.SiteMinderLink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.plugins.UnicenterSDLink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.plugins.HTTPAnalyzerLink")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432806 testIntroscopeSettingsTab")
    public void testIntroscopeSettings(@Optional("432806") String QCID,
        @Optional("testIntroscopeSettings") String testname) {
        try {
            String testCaseName = "testIntroscopeSettings";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.introscopesettings"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.name.save")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.Introscope.WorkstationWebStartInfoLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.isenableworkstationwebstartcheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.Introscope.EnableWorkstationWebStartLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.Introscope.WebViewInformationLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.isenablewebviewcheck")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ishostnameedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.isportedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.iscontextpathedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.Introscope.TransactionTraceSettingsLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ismaxdurationedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.istransactionthresholdedit")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.isbackendcomponentsedit")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432795 testFipsSettingsTab")
    public void testFipsSettingsTab(@Optional("432795") String QCID,
        @Optional("testFipsSettingsTab") String testname) {
        try {
            String testCaseName = "testFipsSettingsTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            security.goToFIPSSettingsSetup();
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.name.save")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.HTTPDefectInformation")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.HTTPDetectInfo")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.UserSessionIDLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.UserSessionID")));

            assertFalse(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.security.HTTPDetectInfo")));
            assertFalse(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("cem.security.UserSessionID")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432796 - testPrivateParametersTab")
    public void testPrivateParametersTab(@Optional("432796") String QCID,
        @Optional("testPrivateParametersTab") String testname) {
        try {
            String testCaseName = "testPrivateParametersTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            String patternText = "testparameter" + Math.round(Math.random() * 100);

            security.goToPrivateParametersTab();
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.accessPolicyTab.apNewButton")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.accessPolicyTab.apDeleteButton")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.privateparam")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.accessidlink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.passlink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.pwlink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.pinlink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.ssnlink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.passcodelink")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.passwordlink")));

            // Add test parameter
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.SaveBtn")));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                getORPropValue("cem.security.parameterpatternlabel1")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.patternText")));
            WebdriverWrapper.inputText(driver, getORPropValue("cem.security.patternText"),
                patternText);
            WebdriverWrapper.click(driver, getORPropValue("cem.security.SaveBtn"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")
                + patternText));

            // delete test parameter
            WebdriverWrapper.click(driver, getORPropValue("linkText") + patternText);
            assertEquals(WebdriverWrapper.getAttribute(driver,
                getORPropValue("cem.security.patternText"), "value"), patternText);
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("cem.security.accessPolicyTab.apDeleteButton")));
            WebdriverWrapper.click(driver,
                getORPropValue("cem.security.accessPolicyTab.apDeleteButton"));
            assertTrue(WebdriverWrapper.isAlertTextPresent(driver,
                getORPropValue("cem.security.deleteParamWarningMsg")));
            WebdriverWrapper.selectPopUp(driver, "accept");
            assertFalse(WebdriverWrapper.isObjectPresent(driver, getORPropValue("linkText")
                + patternText));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432810 - testServicesConfigurationsTab")
    public void testServicesConfigurationTab(@Optional("432810") String QCID,
        @Optional("testServicesConfigurationTab") String testname) {
        try {
            String testCaseName = "testServicesConfigurationTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.services"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.services.DBCleanupService")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.services.StatsAggregationService")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.services.TIMCollectionService")));

            assertTrue(WebdriverWrapper.getElementText(driver,
                getORPropValue("setup.services.DBCleanupServiceValue")).contains(TESS_HOST));
            assertTrue(WebdriverWrapper.getElementText(driver,
                getORPropValue("setup.services.StatsAggregationServiceValue")).contains(TESS_HOST));
            assertTrue(WebdriverWrapper.getElementText(driver,
                getORPropValue("setup.services.TIMCollectionServiceValue")).contains(TESS_HOST));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "432811 - testServicesEMConfigurationsTab")
    public void testServicesEMConfigurationsTab(@Optional("432811") String QCID,
        @Optional("testServicesEMConfigurationsTab") String testname) {
        try {
            String testCaseName = "testServicesEMConfigurationsTab";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.setup"),
                getORPropValue("setup.services"));
            WebdriverWrapper.click(driver, getORPropValue("setup.services.EMConfigurationLink"));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.name.save")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("button.delete")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("button.name.cancel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.services.EMConfEnterpriseManagerLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.services.EMSelect")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("setup.services.EMConfEMInformationLabel")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"multipleRecordingSessions","BAT"}, description = "259549 - RecordingSession list page ")
    public void RECD02_S(@Optional("259549") String QCID, @Optional("ECD02_S") String testname) {
        try {
            String testCaseName = "RECD02_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String recordingSessionName = testCaseName + " record session";
            String webFilterName = testCaseName + " Web Filter";
            login();
            setupMonitor.createMonitor(testCaseName, TIM_IP, TESS_HOST);
            try {
                setupMonitor.enableMonitor(testCaseName);
            } catch (Exception e) {
                setupMonitor.enableMonitor(testCaseName);
            }
            /*
             * setup.deleteTim(MONITOR_NAME);
             * setup.createMonitor(testCaseName, TIM_IP);
             * setup.enableMonitor(testCaseName);
             */
            setup.createWebFilter(webFilterName, testCaseName, MED_REC_HOST_IP_LANG);
            // setup up several recording sessions
            for (int i = 1; i < 3; i++) {
                admin.newRecordingSession(recordingSessionName + " " + i);
                WebdriverWrapper.pageRefresh(driver);
            }
            admin.stopRecordingSessions();
            logOut();
            login();
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.recordingsessions"));
            assertTrue(admin.isGridPresent("recordingSession"));
            assertTrue(admin.getColNumByColTitle("recordingSession", "Date and Time") > 0);
            assertTrue(admin.getColNumByColTitle("recordingSession", "Author") > 0);
            assertTrue(admin.getColNumByColTitle("recordingSession", "Status") > 0);
            WebdriverWrapper.pageRefresh(driver);
            for (int i = 1; i < 3; i++) {
                assertTrue(WebdriverWrapper.isElementDisplayed(driver, getORPropValue("linkText")
                    + recordingSessionName + " " + i));
            }
            WebdriverWrapper.pageRefresh(driver);
            admin.deleteAllRecordingSession();

            setup.deleteTim(testCaseName);
            setup.deleteWebFilter(webFilterName);
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"createSession","BAT"}, description = "259550 - Create Recording Session")
    public void RECD03_S(@Optional("259550") String QCID, @Optional("createSession") String testname) {
        try {
            String testCaseName = "RECD03_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String recordingSessionName = testCaseName + " record session";
            String recordedTxnName = "MedRec Start page";
            login();

            setupMonitor.createMonitor(testCaseName, TIM_IP, TESS_HOST);
            try {
                setupMonitor.enableMonitor(testCaseName);
            } catch (Exception e) {
                setupMonitor.enableMonitor(testCaseName);
            }
            /*
             * setup.createMonitor(testCaseName, TIM_IP);
             * setup.enableMonitor(testCaseName);
             */

            admin.deleteAllRecordingSession();
            String rc = admin.newRecordingSession(true, recordingSessionName, TIM_IP, "", "", "");
            LOGGER.info("What is RC : " + rc);
            Thread.sleep(5000);
            assertTrue(rc.equalsIgnoreCase(""));
            generateDefectsForDefectProcessor();
            LOGGER.info("MED_REC_START_PAGE " + MED_REC_START_PAGE);
            // Make sure recorded
            int maxTimes = 4;
            int times = 1;
            while ((!admin.isGridPresent("comp")) && (times <= maxTimes)) {
                Thread.sleep(5000);
                generateDefectsForDefectProcessor();
                if (times % 2 == 0) WebdriverWrapper.pageRefresh(driver);
                times++;
            }

            WebdriverWrapper.click(driver, getORPropValue("administration.recording.stop"));
            assertTrue(WebdriverWrapper.isElementDisplayed(driver,
                getORPropValue("cem.security.accessPolicyTab.apUpdateButton")));
            LOGGER.info("TestCase Name:- RECD03_S ********" + MED_REC_HOST_IP + ":"
                + MED_REC_HOST_PORT);
            admin.updateRecordedTransactionNameBeforeFinishedRecording(MED_REC_HOST_IP + ":"
                + MED_REC_HOST_PORT + getEnvConstValue("medRecBaseURL"), recordedTxnName);
            WebdriverWrapper.click(driver, getORPropValue("button.done"));
            admin.deleteAllRecordingSession();

            setupMonitor.deleteMonitor(testCaseName);
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259555 - Deleting an existing RecordingSession")
    public void RECD08_S(@Optional("259555") String QCID, @Optional("RECD08_S") String testname) {
        try {
            String testCaseName = "RECD08_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            String recordingSessionName = testCaseName + " record session";
            String transactionName = "index.action";
            login();

            setupMonitor.createMonitor(testCaseName, TIM_IP, TESS_HOST);
            try {
                setupMonitor.enableMonitor(testCaseName);
            } catch (Exception e) {
                setupMonitor.enableMonitor(testCaseName);
            }
            /*
             * setup.createMonitor(MONITOR_NAME, TIM_IP);
             * setup.enableMonitor(MONITOR_NAME);
             */

            String rc = admin.newRecordingSession(true, recordingSessionName, TIM_IP, "", "", "");
            Thread.sleep(10000);

            assertTrue(rc.equalsIgnoreCase(""));
            generateDefectsForDefectProcessor();
            LOGGER.info("MED_REC_START_PAGE " + MED_REC_START_PAGE);
            // Make sure recorded
            int maxTimes = 4;
            int times = 1;
            while ((!admin.isGridPresent("comp")) && (times <= maxTimes)) {
                Thread.sleep(5000);
                generateDefectsForDefectProcessor();
                if (times % 2 == 0) WebdriverWrapper.pageRefresh(driver);
                times++;
            }

            WebdriverWrapper.click(driver, getORPropValue("administration.recording.stop"));
            WebdriverWrapper.click(driver, getORPropValue("button.done"));
            WebdriverWrapper.navigateToPage(driver, getORPropValue("home.administration"),
                getORPropValue("administration.recordingsessions"));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + recordingSessionName);
            // //admin.checkGridRow("tranList", transactionName);
            WebdriverWrapper.click(driver, "xpath_//td/a[text()='" + transactionName
                + "']//..//..//td/input");
            WebdriverWrapper.click(driver, getORPropValue("button.delete"));
            assertTrue(WebdriverWrapper.isAlertTextPresent(driver,
                "Are you sure you want to delete the selected Transaction?"));
            WebdriverWrapper.selectPopUp(driver, "accept");
            admin.deleteAllRecordingSession();

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

    public static void sendHttpRequest(String url) {
        try {
            URL actualUrl = new URL(url);
            WebConversation conversation = new WebConversation();
            WebRequest request = new GetMethodWebRequest(actualUrl, "");
            LOGGER.info("request: " + request);
            WebResponse response = conversation.getResponse(request);
            LOGGER.info("response: " + response);
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
    }

    private void generateMedRedTraffic() {
        try {
            for (int i = 0; i < 10; i++) {
                WebClient web = new WebClient();
                web.getPage("http://" + MED_REC_HOST_IP + ":" + MED_REC_HOST_PORT
                    + getEnvConstValue("medRecPhysicanBaseURL"));
                web.closeAllWindows();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateDefectsForDefectProcessor() {
        String scriptName = "/jamsa07/test.sh ";
        String url = MED_REC_BASE_URL + getEnvConstValue("medRecBaseURL");
        String count = " 2";
        LOGGER.info(scriptName + url + count);
        // //StringBuffer str = runScriptOnUnix(scriptName+url+count);
        StringBuffer str =
            Util.runScriptOnUnix(TIM_HOST_NAME, TIM_REMOTELOGIN, TIM_REMOTEPWD, scriptName + url
                + count);

        LOGGER.info(str.toString());
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
    }

    @AfterClass()
    public void close() {
        try {
            /*
             * setup.synchronizeAllMonitors();
             * setupWebFilter.deleteWebServerFilter(MED_REC_HOST_IP);
             * setup.deleteTim(TIM_HOST_NAME);
             */
            driver.quit();
            Util.runOSCommand("cmd.exe /c taskkill /im " + getEnvConstValue("browser") + "* -F");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
