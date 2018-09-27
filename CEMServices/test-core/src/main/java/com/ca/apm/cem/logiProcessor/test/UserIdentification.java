package com.ca.apm.cem.logiProcessor.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.cem.logiProcessor.LoginProcessorUtils;
import com.ca.apm.tests.test.JBaseTest;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.apm.tests.cem.common.SetupMonitorHelper;

public class UserIdentification extends JBaseTest {


    private SetupMonitorHelper setupMonitor;
    private LoginProcessorUtils logInPUtils;
    private String businessService = "medrecBS";
    private String appName = "MedrecBA";
    private String btImportFile;
    private String petshopBTImportFile;
    private String match1UGID = "found login id";
    private String baseurl = MED_REC_BASE_URL + MED_REC_PHYSICIAN_LOGIN_PAGE;
    private String url = "";
    private String count = " 1";
    private String timLog = "";
    private String offsetVal = "";
    private String scriptName = SSH_SCRIPTS;
    private String tesXMLFilePath = TEXT_XML_FILE_PATH;


    @BeforeClass
    public void initialize() {
        LOGGER.info("**********Initializing in UGIdentification*******");
        super.initialize();

        setupMonitor = new SetupMonitorHelper(m_cemServices);
        logInPUtils = new LoginProcessorUtils();
        btImportFile = "BTExport_physcian_SlowTime_logiProcessor.zip";
        petshopBTImportFile = "BTExport_petshopBS_Login.zip";
        try {
            logIn();
            cleanUser_UserGroup();
           // setupTim(TIM_HOST_NAME, TIM_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********Successful Initializing in UGIdentification*******");
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418983_CEM_User_Identification_Bug_913_914_Offset_and_Length_Validation()
        throws Exception {
        String testCaseName =
            "verify_ALM_418983_CEM_User_Identification_Bug_913_914_Offset_and_Length_Validation";

        String parameterTypeUId = "Query";
        String offset = "3";
        String length = "5";
        String uIdName = "userName";
        String user = "user418983";
        String errMsg = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);

        errMsg =
            admin.addUserIdAdvancedParamToApplication(appName, parameterTypeUId, uIdName, "", "");
        assertTrue(errMsg.contains("Length is required."));
        assertTrue(errMsg.contains("Offset is required."));
        addUserIdParamToApplication(appName, parameterTypeUId, uIdName, offset, length);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl + "?" + uIdName + "=" + user; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        timLog = tim.readTimLog(match1UGID);
        offsetVal = user.substring(3, 8);
        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, offsetVal, ""));
        assertTrue(offsetVal.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports, appName,
            businessService)));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_351175_CEM_User_Identification_offset_Minus_1_and_its_impact_on_defects()
        throws Exception {
        String testCaseName =
            "verify_ALM_351175_CEM_User_Identification_offset_Minus_1_and_its_impact_on_defects";

        String parameterTypeUId = "Query";
        String offset = "-1";
        String length = "0";
        String uIdName = "userName";
        String user = "user418983";
        String match = "Unspecified";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);


        addUserIdParamToApplication(appName, parameterTypeUId, uIdName, offset, length);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl + "?" + uIdName + "=" + user; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);

        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(match.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports, appName,
            businessService)));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }


    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_440135_CEM_User_Identification_TT83187_Error_On_Creating_Logins_User_NullPointerException()
        throws Exception {
        String testCaseName =
            "verify_ALM_440135_CEM_User_Identification_TT83187_Error_On_Creating_Logins_User_NullPointerException";

        String parameterTypeUId = "Query";
        String uIdName = "userName440135";
        String user = "user440135";

        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);

        admin.deleteUserID(user);
        setup.synchronizeAllMonitors();
        addUserIdParamToApplication(appName, parameterTypeUId, uIdName);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl + "?" + uIdName + "=" + user; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);

        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();

        assertTrue(user.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports, appName,
            businessService)));
        assertTrue(admin.userIDSearch(user));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418989_User_Identification_XML_Select_Recording_Session()
        throws Exception {
        String testCaseName = "verify_ALM_418989_User_Identification_XML_Select_Recording_Session";
        String parameterTypeUId = "XML";
        String businessTransaction = "login.action";
        String sessionName = "xmlRec418989";
        String xmlAttribute = "test message";
        String recMsg = "";

        StringBuffer str = new StringBuffer();
        try {
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            CreatBABS(appName, businessService);

            url =
                "-H\\ \'Content-Type:text/xml\'\\ --data-binary\\ @" + tesXMLFilePath + "\\ "
                    + baseurl; // URL
            recMsg =
                admin.newRecordingSession(true, sessionName, TIM_HOST_NAME, "", "", "", scriptName,
                    url, count);
            LOGGER.info("After recoding return Message:::: " + recMsg);
            if (!recMsg.trim().equals("")) {
                LOGGER.info("*****Validate XML file present in speciified path********* "
                    + tesXMLFilePath);
            }
            assertTrue(recMsg.trim().equals(""));
            admin.promoteRecordingSession(sessionName, businessService, businessTransaction);

            enableBusinessServiceMonitoring(businessService);
            admin.enableSlowTimeBusinessTransactionDefects(businessService, businessTransaction);

            addXMLUserIdParamToApplication(appName, parameterTypeUId, sessionName,
                businessTransaction, xmlAttribute);

            setup.synchronizeAllMonitors();



            str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);


            assertTrue(xmlAttribute.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports,
                appName, businessService)));
            LOGGER.info("All validation complted ALM test case passed::" + testCaseName);

        } finally {
            admin.deleteUserID(xmlAttribute);
            admin.deleteRecordingSession(sessionName);
            setup.synchronizeAllMonitors();
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_418984_CEM_User_Identification_URL_Host_With_Advanced_Option()
        throws Exception {
        String testCaseName =
            "verify_ALM_418984_CEM_User_Identification_URL_Host_With_Advanced_Option";
        String parameterTypeUGId = "Cookie";
        String ugIdName = "JSESSIONID";
        String parameterTypeUId = "URL";
        String offset = "3";
        String length = "5";
        String uIdName = "Host";
        String jsessionId = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);


        addUserIdParamToApplication(appName, parameterTypeUId, uIdName, offset, length);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, ugIdName);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        jsessionId = logInPUtils.subStringJsessionID(str);



        timLog = tim.readTimLog(match1UGID);
        offsetVal = MED_REC_HOSTNAME.substring(3, 8);
        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, offsetVal, ""));
        assertTrue(offsetVal.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports, appName,
            businessService)));
        assertTrue(jsessionId.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
            appName, businessService)));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418988_CEM_User_Identification_Basic_Authentication() throws Exception {
        String testCaseName = "verify_ALM_418988_CEM_User_Identification_Basic_Authentication";

        String parameterTypeUId = "Basic Authentication";

        String uIdName = "UserName";
        String uIdNamevalue = "UserName418988";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);


        addUserIdParamToApplication(appName, parameterTypeUId, uIdName);

        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = "-v\\ --basic\\ -u\\ \'" + uIdNamevalue + ":password123\'\\ " + baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        timLog = tim.readTimLog(match1UGID);

        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, uIdNamevalue, ""));
        assertTrue(uIdNamevalue.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports,
            appName, businessService)));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418987_CEM_User_Identification_NTLM_Authentication() throws Exception {
        String testCaseName = "verify_ALM_418987_CEM_User_Identification_NTLM_Authentication";
        String baseurl = "";
        String parameterTypeUId = "NTLM Authentication";
        String uIdName = "UserName";
        String uIdNamevalue = "administrator";
        String uIdpassword = "Notallowed@ca";
        String appName = "petshopBA";
        String businessService = "petshopBS";
        boolean result = false;
        try {
            setupTest(testCaseName);
            startTestCaseName(testCaseName);

            setupAppData(appName, businessService, petshopBTImportFile);
            admin.deleteUserGroup(uIdNamevalue);
            admin.deleteUserID(uIdNamevalue);
            addUserIdParamToApplication(appName, parameterTypeUId, uIdName);


            setup.synchronizeAllMonitors();
            LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);
            tim.enableRequiredTimTraces();
            tim.eraseTimLog();
            baseurl = PETSHOP_BASE_URL + PETSHOP_LOGIN_PAGE;
            url =
                "-v\\ -u\\ \'" + uIdNamevalue + ":" + uIdpassword + "\'\\ " + "--ntlm\\ " + baseurl; // URL

            StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



            timLog = tim.readTimLog(match1UGID);

            LOGGER.info("Expected validation start ::" + testCaseName);
            logIn();
            result =
                uIdNamevalue.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports, appName,
                    businessService, uIdNamevalue));
            if (!result) {
                LOGGER.info("****validate NTLM Auth enable in IIS servwer for PETSHOP Application and petshop url accessible****** ");
                LOGGER.info("****To enable NTLM Go to ISS server > application> right click > properties > Directory Security >Edit Auth and Access control > select intigarated windows Auth  ****** ");
            }
            assertTrue(logInPUtils.regexPattern(timLog, match1UGID, uIdNamevalue, ""));
            assertTrue(result);
        } finally {
            adminBA.deleteBusinessApplicationByName(appName);
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    private void addXMLUserIdParamToApplication(String appName, String parameterTypeUId,
        String sessionName, String businessTransaction, String xmlAttribute) throws Exception {
        admin.deActiveUserID();
        if (!"".equals(xmlAttribute)) {

            admin.deleteUserID(xmlAttribute);
            setup.synchronizeAllMonitors();
        }
        admin.addXMLTypeToAppplicationSessionOrUserOrUserGroupIdentification("user", appName,
            parameterTypeUId, sessionName, businessTransaction, xmlAttribute);
    }

    private void addUserIdParamToApplication(String appName, String parameterTypeUId,
        String uIdName, String offset, String length) throws Exception {
        String paramValue = logInPUtils.paramValue(parameterTypeUId, uIdName);
        admin.deActiveUserID();
        if (!"".equals(paramValue)) {

            admin.deleteUserID(paramValue);
            setup.synchronizeAllMonitors();
        }

        admin.addUserIdAdvancedParamToApplication(appName, parameterTypeUId, uIdName, offset,
            length);
    }

    private void addUserIdParamToApplication(String appName, String parameterTypeUId,
        String uparameterName) throws Exception {
        String paramValue = logInPUtils.paramValue(parameterTypeUId, uparameterName);
        admin.deActiveUserID();
        if (!"".equals(paramValue)) {

            admin.deleteUserID(paramValue);
            setup.synchronizeAllMonitors();
        }
        admin.addUserIdParamToApplication(appName, parameterTypeUId, uparameterName);
    }

    private void CreatBABS(String appName, String businessService) {
        logInPUtils.creatBABS(adminBA, setupMonitor, admin, appName, businessService);

    }

    private void enableBusinessServiceMonitoring(String businessService2) {
        logInPUtils.enableBusinessServiceMonitoring(setupMonitor, admin, businessService);

    }

    private void addUserGroupIdParamToApplication(String appName, String parameterTypeUGId,
        String parameterName) throws Exception {
        String paramValue = logInPUtils.paramValue(parameterTypeUGId, parameterName);

        if (!"".equals(paramValue)) {
            admin.deleteUserGroup(paramValue);
            setup.synchronizeAllMonitors();
        }
        admin.addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName);

    }

    private void setupTim(String timName, String timIP) {
        try {
            setupMonitor.createMonitor(timName, timIP, TESS_HOST);
            setupMonitor.enableMonitor(timName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuffer runScriptOnUnix(String scriptFileName) {
        LOGGER.debug("In case script execution failure validate scription execution .sh file placed in remote machine");
        return Util.runScriptOnUnix(TIM_HOST_NAME, TIM_REMOTELOGIN, TIM_REMOTEPWD, scriptFileName);

    }

    public void cleanUser_UserGroup() {
        admin.deleteAllUserID();
        admin.deleteAllUserGroup();
    }


}
