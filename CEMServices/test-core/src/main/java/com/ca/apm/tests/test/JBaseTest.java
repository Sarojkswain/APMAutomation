package com.ca.apm.tests.test;

import static com.ca.apm.tests.cem.common.CEMConstants.BTS_LOC;
import static com.ca.apm.tests.cem.common.CEMConstants.EM_MACHINE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.EM_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_MACHINE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.WEBDRIVER_ROLE_ID;
import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.codehaus.plexus.util.Os;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.RunCommandFlow;
import com.ca.apm.commons.flow.RunCommandFlowContext;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.cem.common.AdminBTImportHelper;
import com.ca.apm.tests.cem.common.AdminBusinessApplicationHelper;
import com.ca.apm.tests.cem.common.AdminBusinessServiceHelper;
import com.ca.apm.tests.cem.common.CEMServices;
import com.ca.apm.tests.cem.common.DefectsHelper;
import com.ca.apm.tests.cem.common.SetupMonitorHelper;
import com.ca.apm.tests.cem.common.SetupWebFilterHelper;
import com.ca.apm.tests.utility.DBUtil;
import com.ca.apm.tests.utility.DefaultWebDriver;
import com.ca.apm.tests.utility.OracleUtil;
import com.ca.apm.tests.utility.PostgresUtil;
import com.ca.apm.tests.utility.QaFileUtils;
import com.ca.apm.tests.utility.QaUtils;
import com.ca.apm.tests.utility.SeleniumDetails;
import com.ca.apm.tests.utility.Utf8ResourceBundle;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.tas.builder.TasBuilder;

public class JBaseTest extends BaseAgentTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JBaseTest.class);
    public static String TEST_NAME = "NOT SET";
    public ResourceBundle environmentConstants = Utf8ResourceBundle
        .getBundle("environmentConstants");
    public ResourceBundle testData = Utf8ResourceBundle.getBundle("TestData");
    public CEMServices m_cemServices;
    public String WORKING_DIR = getEnvConstValue("workingDir");
    public int iGlobalTimeout = Integer.parseInt(getEnvConstValue("timeout"));
    public String pwd = getEnvConstValue("tessAdminPwd");
    public String uid = getEnvConstValue("tessAdmin");
    public String DB_TYPE = getEnvConstValue("dbType");
    public String DB_NAME = getEnvConstValue("dbName");
    public String DB_OWNER = getEnvConstValue("dbOwner");
    public String DB_PASSWORD = getEnvConstValue("dbOwnerPwd");
    public String DB_PORT = getEnvConstValue("dbPort");
    public String TIM_INSTALLDIR = getEnvConstValue("tim_installDir");
    public String TESS_LOGDIR;
    public String CLW_JAR = getEnvConstValue("clwJarPath") + "CLWorkstation.jar";
    public String EM_PORT = getEnvConstValue("emPort");
    public String MED_REC_HOSTNAME = "jamsa07-cembat1";
    public String MED_REC_HOST_PORT = getEnvConstValue("medrec.port");
    public String MED_REC_PHYSICIAN_LOGIN_PAGE = getEnvConstValue("medRec.physician.loginPage");
    public String PETSHOP_HOSTNAME = getEnvConstValue("petshop.hostname");
    public String PETSHOP_HOST_PORT = getEnvConstValue("petshop.port");
    public String PETSHOP_LOGIN_PAGE = getEnvConstValue("petshop.loginPage");
    public String MED_REC_PATIENT_REGISTER_PAGE = getEnvConstValue("medRec.patient.registerPage");
    public String PETSHOP_BASE_URL = format("http://%s:%s", PETSHOP_HOSTNAME, PETSHOP_HOST_PORT);
    public String MED_REC_BASE_URL = format("http://%s:%s", MED_REC_HOSTNAME, MED_REC_HOST_PORT);
    public String MEDREC_INDEX_PAGE = getEnvConstValue("medRec.index");
    public String TIM_HOST_NAME = envProperties.getMachineHostnameByRoleId(TIM_ROLE_ID);
    public String TIM_BUILD = getEnvConstValue("tim_build");
    public String TIM_REMOTELOGIN = "root";
    public String TIM_REMOTEPWD = "Lister@123";
    public String TIM_ADMIN = getEnvConstValue("timAdmin");
    public String TIM_ADMIN_PWD = getEnvConstValue("timAdminPwd");
    public String TESS_PORT = getEnvConstValue("tessPort");
    public String TESS_ADMIN = getEnvConstValue("tessAdmin");;
    public String TESS_ADMIN_PWD = getEnvConstValue("tessAdminPwd");
    public String TESS_REMOTELOGIN = getEnvConstValue("tess_remostLogin");
    public String TESS_REMOTEPWD = getEnvConstValue("tess_remostPwd");
    public String EM_OS_TYPE = getEnvConstValue("emOSType");
    public String workingDir = getEnvConstValue("workingDir");
    public String TIM_VERSION = getEnvConstValue("tim_version");
    public String SSH_SCRIPTS = getEnvConstValue("ssh_scriptName");
    public String TEXT_XML_FILE_PATH = getEnvConstValue("tesXMLFilePath");
    public String MED_REC_HOST_IP = MED_REC_HOSTNAME;
    public String TIM_IP = TIM_HOST_NAME;
    public CEMSetup setup;
    public CEMSecurity security;
    public CEMAdministration admin;
    public TimWeb tim;
    public Autogen autogen;
    public CemReports reports;
    public QaUtils util;
    public WebDriver driver;
    public OracleUtil dbo;
    public DBUtil db;
    public PostgresUtil dbp;
    public QaFileUtils file;
    public CemUtil cemutil;
    public ResourceBundle oR;
    public BaseSharedObject basesharedobj;
    public AdminBusinessApplicationHelper adminBA;
    public AdminBusinessServiceHelper adminBS;
    public AdminBTImportHelper adminBTImport;
    public DefectsHelper defects;
    public SetupMonitorHelper setupMonitor;
    public SetupWebFilterHelper setupWebFilter;
    public String JDBC_USERNAME = getEnvConstValue("jdbcUserName");
    public String JDBC_PASSWORD = getEnvConstValue("jdbcPassword");
    public String JDBC_CONNECTIONURL = getEnvConstValue("jdbcConnectionURL");
    public String JDBC_DRIVERCLASS = getEnvConstValue("jdbcDriverClass");
    public String JDBC_QUERY = getEnvConstValue("jdbcQuery");
    public String JDBC_DELAY = getEnvConstValue("jdbcDelay");
    public String AGENT_HOSTNAME = getEnvConstValue("agentHostName");
    public String TOMCAT_PORT = getEnvConstValue("tomcatPort");
    public String businessService = "physicianBS";
    public String appName = "Medrec";
    public String btImportFile;
    public String appURL;
    public String bTransactionName;
    public int impactLevelTHCol = 4;
    public int businessImpactTHCol = 5;
    public int reportsTotalDefectsTHCol = 4;
    public int reportsIntervalTHCol = 1;
    public String slowTimeDefect = "Slow Time";
    public String highThroughputDefect = "High Throughput";
    public String medRecBaseURL = "medrec/index.action";
    public String MEDREC_REQ_INDEX = getTestDataValue("MEDREC_REQ_INDEX");
    public String MEDREC_REQ_START = getTestDataValue("MEDREC_REQ_START");
    public String MEDREC_REQ_ADMIN = getTestDataValue("MEDREC_REQ_ADMIN");
    public String MEDREC_REQ_PATIENT = getTestDataValue("MEDREC_REQ_PATIENT");
    public String MEDREC_REQ_PHYSICIAN = getTestDataValue("MEDREC_REQ_PHYSICIAN");
    public String MEDREC_REQ_ADMIN_GROUP = getTestDataValue("MEDREC_REQ_ADMIN_GROUP");
    public String MEDREC_REQ_PATIENT_GROUP = getTestDataValue("MEDREC_REQ_PATIENT_GROUP");
    public String MEDREC_REQ_PHYSICIAN_GROUP = getTestDataValue("MEDREC_REQ_PHYSICIAN_GROUP");
    public String MEDREC_REQ_ADMIN_NAME = getTestDataValue("MEDREC_REQ_ADMIN_NAME");
    public String MEDREC_REQ_PATIENT_NAME = getTestDataValue("MEDREC_REQ_PATIENT_NAME");
    public String MEDREC_REQ_PHYSICIAN_NAME = getTestDataValue("MEDREC_REQ_PHYSICIAN_NAME");
    public String PETSHOP_REQ_HOME = getTestDataValue("PETSHOP_REQ_HOME");
    public String PETSHOP_REQ_CATEGORY = getTestDataValue("PETSHOP_REQ_CATEGORY");
    public String PETSHOP_REQ_ITEMS = getTestDataValue("PETSHOP_REQ_ITEMS");
    public String PETSHOP_REQ_ITEMDETAILS = getTestDataValue("PETSHOP_REQ_ITEMDETAILS");
    public String PETSHOP_REQ_SHOPPINGCART = getTestDataValue("PETSHOP_REQ_SHOPPINGCART");
    public String PETSHOP_REQ_CHECKOUT = getTestDataValue("PETSHOP_REQ_CHECKOUT");
    public String PETSHOP_REQ_SIGNIN = getTestDataValue("PETSHOP_REQ_SIGNIN");
    public String PETSHOP_REQ_ORDERBILLING = getTestDataValue("PETSHOP_REQ_ORDERBILLING");
    public String PETSHOP_REQ_ORDERPROCESS = getTestDataValue("PETSHOP_REQ_ORDERPROCESS");
    public String PETSHOP_REQ_SIGNOUT = getTestDataValue("PETSHOP_REQ_SIGNOUT");
    public String PETSHOP_HOSTIP = getEnvConstValue("petShopHostIp");
    public String MONITOR_NAME = getEnvConstValue("monitorName");
    public String MED_REC_HOST_IP_LANG = getEnvConstValue("medRecHostIp");
    public String MED_REC_START_PAGE = "http://" + MED_REC_HOST_IP + ":" + MED_REC_HOST_PORT
        + medRecBaseURL;
    public String EMAIL_ID = getEnvConstValue("emailId");
    public String SMTPHost = getEnvConstValue("emailSMTPHost");
    public String BUSINESS_APPLICATION = getEnvConstValue("businessApplication");
    public String BUSINESS_SERVICE = getEnvConstValue("businessService");
    public String BUSINESS_TRANSACTION = getEnvConstValue("businessTransaction");
    public String bitMode;
    String emRoleId = EM_ROLE_ID;
    public String DB_HOST = envProperties.getMachineHostnameByRoleId(emRoleId);
    public String TESS_INSTALLDIR = envProperties.getRolePropertyById(emRoleId,
        DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    public String TESS_INSTALLDIR_LINUX = envProperties.getRolePropertyById(emRoleId,
        DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    public String TESS_HOST = envProperties.getMachineHostnameByRoleId(emRoleId);
    TestUtils utility = new TestUtils();
    SeleniumDetails seleniumData;
    BaseAgentTest baseAgentTest = new BaseAgentTest();
    boolean windows = Os.isFamily(Os.FAMILY_WINDOWS);
    private ResourceBundle readProperties = Utf8ResourceBundle.getBundle("OR_CEM_Prop");
    public String tessUrl = "http://" + envProperties.getMachineHostnameByRoleId(emRoleId) + ":"
        + getEnvConstValue("tessPort") + "/" + getORPropValue("cem.login.url");
    public String CEM_LOGIN_URL = "http://" + TESS_HOST + ":" + TESS_PORT + "/"
        + getORPropValue("cem.login.url");

    public JBaseTest() {}

    @BeforeSuite(alwaysRun = true)
    public void initialize() {
        List<String> machines = new ArrayList<String>();

        machines.add(EM_MACHINE_ID);
        machines.add(TIM_MACHINE_ID);
        syncTimeOnMachines(machines);


        UnZip UnZip = new UnZip();
        try {
            UnZip.unzip(BTS_LOC + "CEM_BTS.zip", BTS_LOC);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        LOGGER.info("Inside JBaseTest initialize()");
        LOGGER.info("Web Driver to be initialized");
        DefaultWebDriver wd = new DefaultWebDriver();
        try {
            String browser = envProperties.getRolePropertyById(WEBDRIVER_ROLE_ID, "browser.type");
            if (browser.contains("internetexplorer"))
                bitMode = "64";
            else if (browser.contains("chrome")) bitMode = "32";
            driver = wd.initializeDriver(browser, bitMode, WORKING_DIR);
            seleniumData = new SeleniumDetails.Builder().browser(browser).build();

        } catch (FileNotFoundException e) {
            LOGGER.error("File Not Found Exception" + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception :" + e.getMessage());
        }
        LOGGER.info("Web Driver initialized");
        LOGGER.info("Creating Driver Objects");
        basesharedobj = new BaseSharedObject(driver);
        setup = new CEMSetup(driver);
        security = new CEMSecurity(driver);
        admin = new CEMAdministration(driver);
        tim = new TimWeb(driver);
        autogen = new Autogen(driver);
        reports = new CemReports(driver);
        cemutil = new CemUtil(driver);
        util = new QaUtils();
        file = new QaFileUtils();
        LOGGER.info("Driver Objects Created");
        String configFileEm =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        String xmlPath = getTessSecurityFile();
        updateTessSecurityFile(xmlPath);
        try {
            updateJarFile(xmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        replaceProp("log4j.logger.Manager=INFO,console,logfile",
            "log4j.logger.Manager=DEBUG,console,logfile", EM_MACHINE_ID, configFileEm);
        startEM(emRoleId);
        String TESS_HOST_CA;
        if (!TESS_HOST.contains("ca.com")) {
            TESS_HOST_CA = TESS_HOST + ".ca.com";
        } else {
            TESS_HOST_CA = TESS_HOST;
        }
        LOGGER.info("Initialize CEMServices");

        m_cemServices = new CEMServices(TESS_HOST_CA, TESS_PORT, TESS_ADMIN, TESS_ADMIN_PWD);
        m_cemServices.login();
        LOGGER.info("cemservices initialized");
        adminBA = new AdminBusinessApplicationHelper(m_cemServices);
        adminBS = new AdminBusinessServiceHelper(m_cemServices);
        adminBTImport = new AdminBTImportHelper(m_cemServices);
        defects = new DefectsHelper(m_cemServices);
        setupMonitor = new SetupMonitorHelper(m_cemServices);
        LOGGER.info("" + setupMonitor);
        setupWebFilter = new SetupWebFilterHelper(m_cemServices);
        LOGGER.info("CEMServices Initialized");
        appURL = getEnvConstValue("appURL");
        btImportFile = getEnvConstValue("btImportFile");
        bTransactionName = getEnvConstValue("bTransactionName");
        TESS_LOGDIR =
            util.convertPathSeparators(TESS_INSTALLDIR + "logs/IntroscopeEnterpriseManager.LOGGER");
    }

    public String getORPropValue(String key) {
        try {
            return readProperties.getString(key.trim());
        } catch (NullPointerException e) {
            LOGGER.error("corresponding key :" + key + " Not found in OR file *****\n "
                + e.getMessage());
        } catch (ClassCastException ce) {
            LOGGER.error("corresponding key :" + key + " Not found in OR file *****\n "
                + ce.getMessage());
        } catch (MissingResourceException mre) {
            LOGGER.error("corresponding key :" + key + " Not found in OR file *****\n "
                + mre.getMessage());
        }
        return null;
    }

    public String getTestDataValue(String key) {
        try {
            return testData.getString(key.trim());
        } catch (NullPointerException e) {
            LOGGER.info("corresponding key :" + key + " Not found in OR file *****\n "
                + e.getMessage());

        } catch (ClassCastException ce) {
            LOGGER.info("corresponding key :" + key + " Not found in OR file *****\n "
                + ce.getMessage());

        } catch (MissingResourceException mre) {
            LOGGER.info("corresponding key :" + key + " Not found in OR file *****\n "
                + mre.getMessage());

        }
        return null;
    }

    public String getEnvConstValue(String key) {
        try {
            return environmentConstants.getString(key.trim());
        } catch (NullPointerException e) {
            LOGGER.error("corresponding key :" + key + " Not found in OR file *****\n "
                + e.getMessage());
        } catch (ClassCastException ce) {
            LOGGER.error("corresponding key :" + key + " Not found in OR file *****\n "
                + ce.getMessage());
        } catch (MissingResourceException mre) {
            LOGGER.error("corresponding key :" + key + " Not found in OR file *****\n "
                + mre.getMessage());
        }
        return null;
    }

    public void logIn() throws Exception {
        try {
            if (driver == null) {
                DefaultWebDriver wd = new DefaultWebDriver();
                driver =
                    wd.initializeDriver(getEnvConstValue("browser"), getEnvConstValue("bitmode"),
                        WORKING_DIR);
            }
            LOGGER.info("Logging into CEM UI: " + CEM_LOGIN_URL);
            WebdriverWrapper.navigateToUrl(driver, CEM_LOGIN_URL);
            LOGGER.info("Inputs: " + getORPropValue("login.username.id") + "-->" + uid + " "
                + getORPropValue("login.password.id") + "-->" + pwd);
            WebdriverWrapper.inputText(driver, getORPropValue("login.username.id"), uid);


            WebdriverWrapper.inputText(driver, getORPropValue("login.password.id"), pwd);

            WebdriverWrapper.click(driver, getORPropValue("login.login.id"));

            assertTrue(WebdriverWrapper.isObjectPresent(driver, getORPropValue("login.logout")));
            LOGGER.info("Login to CEM UI Sucessful");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void logOut() throws Exception {
        try {
            if (!WebdriverWrapper.isElementDisplayed(driver, getORPropValue("login.logout"))) {
                logIn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WebdriverWrapper.click(driver, getORPropValue("login.logout"));
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
    }

    public void setupTest(String name) {
        // Add marker at the begining of the test
        TEST_NAME = name;
        addTestMarker("Begin:" + name);
    }

    /**
     * Adding Markers below in order to debug...
     */
    public void tearDownTest(String name) {
        // Add marker at the end of the test
        addTestMarker("End:" + name);
        TEST_NAME = "NOT SET";
    }

    public void initDB() {

        if (DB_TYPE.equalsIgnoreCase("Oracle")) {

            dbo = new OracleUtil(DB_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
            db = dbo;
        } else {
            LOGGER.info("Initializing dbp with: " + DB_HOST + "," + DB_PORT + "," + DB_NAME);
            dbp = new PostgresUtil(DB_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
            db = dbp;
        }
        LOGGER.info("DB Host:" + DB_HOST);
    }

    public void initDB(String DB_HOST, String DB_PORT, String DB_NAME, String DB_OWNER,
        String DB_PASSWORD) {
        if (DB_TYPE.equalsIgnoreCase("Oracle")) {
            dbo = new OracleUtil(DB_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
            db = dbo;
        } else {
            dbp = new PostgresUtil(DB_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
            db = dbp;
        }
        LOGGER.info("Connected to DB Host:" + DB_HOST);
    }

    public void assertTrue(boolean condition, String message) {
        if (condition == false) {
            addFailedMarker();
        }
        Assert.assertTrue(condition, message);
    }

    public void assertFalse(boolean condition, String message) {
        if (condition) {
            addFailedMarker();
        }
        Assert.assertFalse(condition, message);
    }

    public void assertTrue(boolean condition) {
        if (condition == false) {
            addFailedMarker();
        }
        Assert.assertTrue(condition);
    }

    public void assertFalse(boolean condition) {
        if (condition) {
            addFailedMarker();
        }
        Assert.assertFalse(condition);
    }

    public void assertEquals(Object actual, Object expected) {
        if (actual.equals(expected) == false) {
            addFailedMarker();
        }
        Assert.assertEquals(actual, expected);
    }

    public void assertEquals(Object actual, Object expected, String message) {
        if (actual.equals(expected) == false) {
            addFailedMarker();
        }
        Assert.assertEquals(actual, expected, message);
    }

    /**
     * Adding Markers below in order to debug...
     */
    public String startTestCaseName(String testCaseName) {
        LOGGER.info("  ****** Now Started Running TestCase****** " + testCaseName);
        return testCaseName;
    }

    public String endTestCaseName(String testCaseName) {
        LOGGER.info("   ****** Success: End of TestCase****** " + testCaseName);
        return testCaseName;
    }

    public String startInitializeString(String testSuiteName) {
        LOGGER.info("   ********** Started Initializing in *******  " + testSuiteName);
        return testSuiteName;
    }

    public String endInitializeString(String testSuiteName) {
        LOGGER.info("   ********** Successful Initializing in*******  " + testSuiteName);
        return testSuiteName;
    }

    public void getBrowser(String url) {
        LOGGER.info("Navigating to: " + url);
        WebdriverWrapper.navigateToUrl(driver, url);

    }

    public void openBrowser() {
        LOGGER.info("Tess URL: " + tessUrl);
        WebdriverWrapper.navigateToUrl(driver, tessUrl);
    }

    @AfterTest(alwaysRun = true)
    public void closeBrowser() {
        try {
            driver.close();
        } catch (Exception e) {
            LOGGER.info("Driver Object has been garbled");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void teardown() {
        driver.quit();
    }


    public void addFailedMarker() {
        try {
            String cookie =
                WebdriverWrapper.getCookieByName(driver, "_replay_transaction_details_");
            String port = WebdriverWrapper.getCookieByName(driver, "_replay_agent_port_");
            String error = ReplayMarker.addMarker(TESS_HOST, TEST_NAME, cookie, port);
            if (error != null) {
                System.err.println(error);
            }
        } catch (Throwable t) {
            // Replay cookie not found.
            // Either the app is not recording or the case before any
            // transactions have run
            ReplayMarker.addMarker(TESS_HOST, "Failed:" + TEST_NAME, null, "");
        }
    }

    public void addTestMarker(String name) {
        try {
            String port = WebdriverWrapper.getCookieByName(driver, "_replay_agent_port_");
            String error = ReplayMarker.addMarker(TESS_HOST, name, null, port);
            if (error != null) {
                System.err.println(error);
            }
        } catch (Throwable t) {
            // Replay cookie not found.
            // Either the app is not recording or the case before any
            // transactions have run
            ReplayMarker.addMarker(TESS_HOST, name, null, "");
        }
    }


    /**
     * @param appName
     * @param businessService
     * @param btImportFile
     */
    public void setupAppData(String appName, String businessService, String btImportFile) {

        try {
            adminBA.createBusinessApplication(appName, appName, "Generic", "Application Specific",
                true, true, "5", "E-Commerce", "UTF-8", TESS_HOST);
            // createBusinessApplication(appName, appName, "Generic", "Application Specific", true,
            // true, "E-Commerce", "5", "UTF-8");
            String bsImportFile = admin.getTestDataFullPath("GeneralApplication", btImportFile);
            adminBTImport.importZipFileToNewBS(appName, businessService, businessService,
                bsImportFile);
            util.sleep(10000);
            setupMonitor.syncMonitors();
            LOGGER.info("Current URL@@@@@@" + driver.getCurrentUrl());

            admin.enableBusinessServiceMonitoring(businessService);
            setupMonitor.syncMonitors();
        } catch (Exception e) {
            LOGGER.error("Failed to create Application: " + appName);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void generateData(String command) {
        LOGGER.info(command);
        RunCommandFlowContext.Builder runCommandContextbuilder =
            new RunCommandFlowContext.Builder();
        runCommandContextbuilder.directory("/jamsa07").command(command).build();
        RunCommandFlowContext runCommandContext =
            new RunCommandFlowContext(runCommandContextbuilder);
        runFlowByMachineId(TIM_MACHINE_ID, RunCommandFlow.class, runCommandContext);
    }

    public void setIPInHostsFile(List<String> roleIds) {
        String hostName, hostIp;
        try {
            for (String roleId : roleIds) {
                hostName = envProperties.getMachineHostnameByRoleId(roleId);
                hostIp = baseAgentTest.returnIPforGivenHost(hostName);
                LOGGER.info("The hostname is : " + hostName);
                LOGGER.info("The IP for that Host is : " + TIM_IP);
                if (windows) {
                    List<String> ipString = new ArrayList<String>();
                    ipString.add(hostIp + "\t" + hostName + "\t" + hostName + ".ca.com");

                    baseAgentTest.isKeywordInFile(envProperties,
                        envProperties.getMachineIdByRoleId(roleId),
                        "C:\\Windows\\System32\\drivers\\etc\\hosts", "127.0.0.1");
                    baseAgentTest.appendProp(ipString, envProperties.getMachineIdByRoleId(roleId),
                        "C:\\Windows\\System32\\drivers\\etc\\hosts");
                }
            }
        } catch (Exception e) {

        }
    }

    public void updateTessSecurityFile(String xmlPath) {

        replaceProp("<constructor-arg value=\"internal\"/>",
            "<constructor-arg value=\"Internal\"/>", EM_MACHINE_ID, xmlPath);
        replaceProp("<constructor-arg value=\"disallow\"/>", "<constructor-arg value=\"allow\"/>",
            EM_MACHINE_ID, xmlPath);
    }

    public String getTessSecurityFile() {

        String jarFile = "";
        String xmlFile = "";
        String destDir = TasBuilder.WIN_SOFTWARE_LOC + "EXTRACT_TESSJAR";
        String installDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);

        File dstDir = new File(destDir);
        if (!dstDir.exists()) dstDir.mkdir();

        try {
            File[] files =
                new File(installDir + "\\product\\enterprisemanager\\plugins\\").listFiles();

            for (File file : files) {
                if (file.toString().toLowerCase().contains("com.wily.apm.tess")
                    && !file.toString().toLowerCase().contains("com.wily.apm.tess.nl1")) {
                    jarFile = file.toString();
                    LOGGER.info("The Jar File is " + jarFile);
                    break;
                }
            }

            java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                java.io.File f =
                    new java.io.File(destDir + java.io.File.separator + file.getName());
                if (file.isDirectory()) { // if its a directory, create it
                    f.mkdir();
                    continue;
                }
                java.io.InputStream is = null;
                if (file.toString().toLowerCase().contains("tess-security.xml")) {
                    is = jar.getInputStream(file); // get the input stream
                    xmlFile = file.toString();
                    FileOutputStream fos = new FileOutputStream(f);
                    while (is.available() > 0) { // write contents of 'is' to 'fos'
                        fos.write(is.read());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred");
        }
        return destDir + java.io.File.separator + xmlFile;
    }


    public void updateJarFile(String xmlPath) throws IOException {
        String installDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        String jarName = "";
        try {
            File[] files =
                new File(installDir + "\\product\\enterprisemanager\\plugins\\").listFiles();

            for (File file : files) {
                if (file.toString().toLowerCase().contains("com.wily.apm.tess")
                    && !file.toString().toLowerCase().contains("com.wily.apm.tess.nl1")) {
                    jarName = file.toString();
                    LOGGER.info("The Jar File is " + jarName);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fileName = xmlPath;
        String f1 = "";
        File jarFile = new File(jarName);
        File tempJarFile = new File(jarName + ".temp");
        JarFile jar = new JarFile(jarFile);
        boolean jarUpdated = false;
        try {
            Manifest jarManifest = jar.getManifest();
            JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(tempJarFile));
            byte[] buffer = new byte[1024];
            int bytesRead;
            try {
                FileInputStream file = new FileInputStream(fileName);
                try {
                    f1 = fileName.split("WebContent")[1];
                    LOGGER.info("Before " + f1);
                    f1 = "WebContent" + f1;
                    f1 = f1.replace("\\", "/");
                    LOGGER.info("After " + f1);
                    JarEntry entry = new JarEntry(f1);
                    tempJar.putNextEntry(entry);
                    while ((bytesRead = file.read(buffer)) != -1) {
                        tempJar.write(buffer, 0, bytesRead);
                    }
                } finally {
                    file.close();
                    file=null;
                }
                for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (entry.getName().toLowerCase().contains(f1.toLowerCase())) {
                        continue;
                    } else {
                        InputStream entryStream = jar.getInputStream(entry);
                        tempJar.putNextEntry(entry);
                        while ((bytesRead = entryStream.read(buffer)) != -1) {
                            tempJar.write(buffer, 0, bytesRead);
                        }
                    }
                }
                jarUpdated = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                tempJar.putNextEntry(new JarEntry("stub"));
            } finally {
                tempJar.flush();
                tempJar.close();
                tempJar=null;
                System.gc();
            }
        } finally {
            jar.close();
            LOGGER.info(jarName + " closed.");
            if (!jarUpdated) {
                tempJarFile.delete();
            }
        }
        if (jarUpdated) {
            harvestWait(5);
            renameFile(jarName, jarName + "_old", EM_MACHINE_ID);
            harvestWait(5);
            renameFile(jarName + ".temp", jarName, EM_MACHINE_ID);
        }

    }

    public void setattributeinXML(String machineId, String xmlFilePath, String xpathtonode,
        String nodeattribute, String value) {

        XmlModifierFlowContext modifyXML =
            new XmlModifierFlowContext.Builder(xmlFilePath).setAttribute(xpathtonode,
                nodeattribute, value).build();

        runFlowByMachineId(machineId, XmlModifierFlow.class, modifyXML);
    }

}
