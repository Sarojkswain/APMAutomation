package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import org.testng.annotations.*;
import org.testng.annotations.Test;

import java.util.Calendar;

public class CEMBat_AutoGen extends JBaseTest {

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
    String serviceName = "Avitek Service";


    @BeforeClass
    public void initialize() {
        LOGGER.info("**********   Initializing in CEM BAT AutoGen *******");
        super.initialize();
        appURL = getEnvConstValue("appURL");
        bTransactionName = getEnvConstValue("bTransactionName");
        try {
            logIn();
            initDB();

            setupMonitor.createMonitor("CEMQA", "10.131.98.43", "10.131.116.29");
            try {
                setupMonitor.enableMonitor(MONITOR_NAME);
            } catch (Exception e) {
                setupMonitor.enableMonitor(MONITOR_NAME);
            }
            /*
             * setup.createMonitor("CEMQA", "10.131.98.43");
             * setup.enableMonitor(MONITOR_NAME);
             */
            autogen.setAutogenUI();
            admin.createBusinessApplication(appDefName, "AutogenSMokeTest2", "Generic",
              "Application Specific", true, true, "Enterprise", "60", "ISO-8859-1");
            admin.createBusinessService(serviceName, serviceName, appDefName, true, "");
            setupMonitor.syncMonitors();

        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********   Successful Initializing in CEM BAT  AutoGen *******");
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259419 - Presence of Transaction Discovery tabs")
    public void ATD01_S(@Optional("259419") String QCID, @Optional("ATD01_S") String testname) {
        try {
            String testCaseName = "ATD01_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            autogen.setAutogenUI();

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.discoveredtxn.statusLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.discoveredtxn.AutoTranDiscLabel")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.discoveredtxn.TemplatesLabel")));

            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.autogen.saveBtn")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.autogen.startBtn")));
            assertTrue(WebdriverWrapper.isObjectPresent(driver,
                getORPropValue("administration.autogen.stopBtn")));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259420 - Parameters in Transaction Discovery tab")
    public void ATD02_S(@Optional("259420") String QCID, @Optional("ATD02_S") String testname) {
        try {
            String testCaseName = "ATD02_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            autogen.setAutogenUI();
            autogen.stopAutogen();

            String limitTxns =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.maxBusinessTransactions"), "value");
            assertEquals(limitTxns, "0");
            assertTrue(WebdriverWrapper.isElementSelected(driver,
                getORPropValue("administration.autogen.autogenDisabled")));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                getORPropValue("administration.autogen.stoppedStatusMsg")));
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                getORPropValue("administration.autogen.paramSeparator")));
            String defaultPathSeparator =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.pathParamSeparatorSelect"), "value");
            assertEquals(defaultPathSeparator.trim(), "");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259422 - Limit the number of  txns generated- acceptable values")
    public void ATD04_S(@Optional("259422") String QCID, @Optional("ATD04_S") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ATD04_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            autogen.setAutogenUI();

            if (WebdriverWrapper.isTextInSource(driver,
                getORPropValue("administration.autogen.stoppedStatusMsg"))) {
                autogen.startAutogen();
            }
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                getORPropValue("administration.autogen.runningStatusMsg")));
            if (!WebdriverWrapper.isElementSelected(driver,
                getORPropValue("administration.autogen.checkLimit"))) {
                WebdriverWrapper.click(driver, getORPropValue("administration.autogen.checkLimit"));
            }
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.autogen.maxBusinessTransactions"), "10");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            String limitTxns1 =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.maxBusinessTransactions"), "value");
            assertEquals(limitTxns1, "10");

            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.autogen.maxBusinessTransactions"), "250");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            String limitTxns2 =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.maxBusinessTransactions"), "value");
            assertEquals(limitTxns2, "250");

            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.autogen.maxBusinessTransactions"), "3000");
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            String limitTxns3 =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.maxBusinessTransactions"), "value");
            assertEquals(limitTxns3, "3000");

            WebdriverWrapper.click(driver, getORPropValue("administration.autogen.checkLimit"));
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259519 - Start button functionality in Transaction Discovery Tab")
    public void ATD0115_S(@Optional("259519") String QCID, @Optional("ATD0115_S") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ATD0115_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            autogen.setAutogenUI();
            autogen.startAutogen();

            assertTrue(WebdriverWrapper.isTextInSource(driver,
                getORPropValue("administration.autogen.runningStatusMsg")));
            assertFalse(WebdriverWrapper.isElementEnabled(driver,
                getORPropValue("administration.autogen.startBtn1")));
            assertTrue(WebdriverWrapper.isElementEnabled(driver,
                getORPropValue("administration.autogen.stopBtn1")));
            autogen.stopAutogen();
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259520 - Stop button functionality in Transaction Discovery Tab")
    public void ATD0116_S(@Optional("259520") String QCID, @Optional("ATD0116_S") String testname)
        throws InterruptedException {
        try {
            String testCaseName = "ATD0116_S";
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            autogen.setAutogenUI();
            autogen.stopAutogen();
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                getORPropValue("administration.autogen.stoppedStatusMsg")));
            autogen.stopAutogen();
            tearDownTest(testCaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259436 - Path Paramter Delimiter UI")
    public void ATD019_S(@Optional("259436") String QCID, @Optional("ATD019_S") String testname) {
        try {
            String testcaseName = "ATD019_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            String PathSeparatorValue[] =
                {"!", "@", "$", ",", "-", ".", ":", ";", "^", "|", "~", "_", ""};
            int arraylen = PathSeparatorValue.length;
            autogen.setAutogenUI();
            for (int i = 0; i < arraylen; i++) {
                WebdriverWrapper.selectBox(driver,
                    getORPropValue("administration.autogen.pathParamSeparatorSelect"),
                    PathSeparatorValue[i]);
                WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));
                String defaultPathSeparator =
                    WebdriverWrapper.getSelectedValue(driver,
                        getORPropValue("administration.autogen.pathParamSeparatorSelect"));
                assertEquals(defaultPathSeparator, PathSeparatorValue[i]);
            }
            tearDownTest(testcaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259439 - New transaction template UI")
    public void ATD25_S(@Optional("259439") String QCID, @Optional("ATD25_S") String testname) {
        try {
            String testcaseName = "ATD25_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            autogen.setAutogenUI();
            WebdriverWrapper.click(driver, getORPropValue("button.new"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Name:"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Description:"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "URL Path Filter:"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Content Type Filter:"));
            assertTrue(WebdriverWrapper.isTextInSource(driver, "Business Application:"));
            String defaultURLPath =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.urlPath"), "value");
            assertEquals(defaultURLPath, "/*");
            String defaultContentType =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.contentType"), "value");
            assertEquals(defaultContentType, "text/html");
            String defaultappDef =
                WebdriverWrapper.getAttribute(driver,
                    getORPropValue("administration.autogen.appDef"), "value");
            assertEquals(defaultappDef, "1");
            tearDownTest(testcaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }

    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259445 - Transaction templates enabled,disabled.")
    public void ATD33_S(@Optional("259445") String QCID, @Optional("ATD33_S") String testname) {
        try {
            String testcaseName = "ATD33_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            String urlPathFilter = "/MSPetShop/*";
            String contentTypeFilter = "text/html";
            String appDefName = "Default Application";
            String URLType = "URL";
            String URLName = "Host";
            String actionEdit = "Matches";
            String patternEdit = "*/";

            Calendar cal = Calendar.getInstance();
            String templateName1 = templateName + cal.getTime().toString();
            autogen.setAutogenUI();
            autogen.addTemplate(templateName1, urlPathFilter, contentTypeFilter, appDefName);
            assertTrue(WebdriverWrapper.isElementDisplayed(driver, getORPropValue("linkText")
                + templateName1));
            autogen.addParameter(templateName1, URLType, URLName, actionEdit, patternEdit, true);
            autogen.setAutogenUI();
            assertEquals(setup.getGridCellValue("command", templateName1, "Status"), "Disabled");
            autogen.enableTemplate();
            assertEquals(setup.getGridCellValue("command", templateName1, "Status"), "Enabled");
            autogen.disableTemplate();
            assertEquals(setup.getGridCellValue("command", templateName1, "Status"), "Disabled");
            tearDownTest(testcaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259444 - Transaction templates created,updated,deleted.")
    public void ADT32_S(@Optional("259444") String QCID, @Optional("ADT32_S") String testname) {
        try {
            String testcaseName = "ADT32_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            String urlPathFilter = "/MSPetShop/*";
            String contentTypeFilter = "text/html";
            String appDefName = "Default Application";
            String urlPathFilterUpdate = "/sampleportal/*";
            String contentTypeFilterUpdate = "text/xml";
            String appDefNameUpdate = "Avitek Application";

            Calendar cal = Calendar.getInstance();
            String templateName1 = templateName + Math.round(Math.random() * 100);

            autogen.setAutogenUI();
            autogen.addTemplate(templateName1, urlPathFilter, contentTypeFilter, appDefName);
            assertTrue(WebdriverWrapper.isElementDisplayed(driver, getORPropValue("linkText")
                + templateName1));
            assertEquals(setup.getGridCellValue("command", templateName1, "Status"), "Disabled");
            assertEquals(setup.getGridCellValue("command", templateName1, "Description"), "");
            assertEquals(setup.getGridCellValue("command", templateName1, "Content Type"),
                contentTypeFilter);
            assertEquals(setup.getGridCellValue("command", templateName1, "URL Path"),
                urlPathFilter);
            assertEquals(setup.getGridCellValue("command", templateName1, "Business Application"),
                appDefName);
            assertEquals(setup.getGridCellValue("command", templateName1, "Number of Parameters"),
                "0");

            // Update a transaction Template
            WebdriverWrapper.click(driver, getORPropValue("linkText") + templateName1);
            WebdriverWrapper.inputText(driver, getORPropValue("administration.autogen.urlPath"),
                urlPathFilterUpdate);
            WebdriverWrapper.inputText(driver,
                getORPropValue("administration.autogen.contentType"), contentTypeFilterUpdate);
            WebdriverWrapper.selectBox(driver, getORPropValue("administration.autogen.appDef"),
                appDefNameUpdate);
            WebdriverWrapper.click(driver, getORPropValue("button.edit.save"));

            assertEquals(setup.getGridCellValue("command", templateName1, "Content Type"),
                contentTypeFilterUpdate);
            assertEquals(setup.getGridCellValue("command", templateName1, "URL Path"),
                urlPathFilterUpdate);
            assertEquals(setup.getGridCellValue("command", templateName1, "Business Application"),
                appDefNameUpdate);

            assertFalse(WebdriverWrapper.isObjectPresent(driver, "name_" + templateName1));
            tearDownTest(testcaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259491 - Autogen Business Process- Read Only")
    public void ATD84_S(@Optional("259491") String QCID, @Optional("ATD84_S") String testname) {
        try {
            String testcaseName = "ATD84_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            autogen.setAutogenUI();
            autogen.gotoBSUI();
            assertTrue(WebdriverWrapper.isElementDisplayed(driver, getORPropValue("linkText")
                + autogenBS));
            WebdriverWrapper.click(driver, getORPropValue("linkText") + autogenBS);
            assertFalse(WebdriverWrapper.isElementEnabled(driver, getORPropValue("button.new")));
            assertFalse(WebdriverWrapper.isElementEnabled(driver, getORPropValue("button.enable")));
            assertFalse(WebdriverWrapper.isElementEnabled(driver, getORPropValue("button.disable")));
            WebdriverWrapper.click(driver, getORPropValue("admin.ug.generaltab"));
            assertFalse(WebdriverWrapper.isTextInSource(driver, "Transaction Impact Level"));
            assertFalse(WebdriverWrapper.isTextInSource(driver, "Success Rate SLA"));
            assertFalse(WebdriverWrapper.isTextInSource(driver, "Sigma SLA"));
            assertFalse(WebdriverWrapper.isTextInSource(driver, "Transaction Time SLA"));
            tearDownTest(testcaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259401 -  Warning when no parameters for a template.")
    // (dependsOnMethods = {"ATD33_S"})
    public void ATD_Med2_02_S(@Optional("259401") String QCID,
        @Optional("ATD_Med2_02_S") String testname) {
        try {
            String testcaseName = "ATD_Med2_02_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            String urlPathFilter = "/MSPetShop/*";
            String contentTypeFilter = "text/html";
            String appDefName = "Default Application";

            Calendar cal = Calendar.getInstance();
            String templateName1 = templateName + cal.getTime();
            autogen.setAutogenUI();
            autogen.addTemplate(templateName1, urlPathFilter, contentTypeFilter, appDefName);
            assertTrue(WebdriverWrapper.isElementDisplayed(driver, getORPropValue("linkText")
                + templateName1));
            assertEquals(setup.getGridCellValue("command", templateName1, "Status"), "Disabled");
            autogen.enableTemplate();
            assertTrue(WebdriverWrapper.isTextInSource(driver,
                "Create one or more parameters for the template : " + templateName1)
                || WebdriverWrapper.isTextInSource(driver, templateName1
                    + " have been detected without parameters"));
            tearDownTest(testcaseName);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "TEST CASE FAILED WITH THE ABOVE EXCEPTION");
        }
    }


    @Parameters(value = {"QCID", "testname"})
    @Test(groups = {"BAT"}, description = "259402 -  Adding Parameter using New button")
    public void ATD_Med2_04_S(@Optional("259402") String QCID,
        @Optional("ATD_Med2_04_S") String testname) {
        try {
            String testcaseName = "ATD_Med2_04_S";
            setupTest(testcaseName);
            startTestCaseName(testcaseName);

            String urlPathFilter = "/MSPetShop/*";
            String contentTypeFilter = "text/html";
            String appDefName = "Default Application";
            String URLType = "URL";
            String URLName = "Path";
            String actionEdit = "Matches";
            String patternEdit = "*";
            boolean isRequired = false;

            Calendar cal = Calendar.getInstance();
            String templateName1 = templateName + cal.getTime().toString();
            autogen.setAutogenUI();
            autogen.addTemplate(templateName1, urlPathFilter, contentTypeFilter, appDefName);
            autogen.addParameter(templateName1, URLType, URLName, actionEdit, patternEdit,
                isRequired);
            WebdriverWrapper.click(driver, getORPropValue("linkText") + templateName1);
            assertTrue(WebdriverWrapper.isElementDisplayed(driver, getORPropValue("linkText")
                + URLName));
            assertEquals(setup.getCellContentsByRowAndCol("autogenParams", 1, "Type"), URLType);
            assertEquals(setup.getCellContentsByRowAndCol("autogenParams", 1, "Name"), URLName);
            String pId = basesharedobj.getAttributeFromCell("autogenParams", 1, 1, "/input/@value");
            String actionValue = pId + "," + URLType + "," + URLName + "-action";
            assertEquals(
                WebdriverWrapper.getSelectedValue(driver, "xpath_//select[@name='" + actionValue
                    + "']"), actionEdit);
            assertTrue(WebdriverWrapper.isTextInSource(driver, patternEdit));
            tearDownTest(testcaseName);

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
