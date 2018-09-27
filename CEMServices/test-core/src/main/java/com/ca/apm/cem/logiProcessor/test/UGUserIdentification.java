package com.ca.apm.cem.logiProcessor.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.cem.logiProcessor.LoginProcessorUtils;
import com.ca.apm.tests.test.JBaseTest;
import com.ca.apm.tests.utility.QaUtils;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.apm.tests.cem.common.DefectsHelper;
import com.ca.apm.tests.cem.common.SetupWebFilterHelper;

public class UGUserIdentification extends JBaseTest {

    QaUtils util = new QaUtils();
    private String tess_default = util.convertPathSeparators(TESS_INSTALLDIR
        + "config/tess-default.properties");
    private LoginProcessorUtils logInPUtils;
    private String businessService = "medrecBS";
    private String appName = "MedrecBA";
    private String btImportFile;
    private String jsessionId = "";
    private String delimiters = ";";
    private String baseurl = MED_REC_BASE_URL + MED_REC_PHYSICIAN_LOGIN_PAGE;
    private String url = "";
    private String count = " 1";
    private String timLog = "";
    private String scriptName = SSH_SCRIPTS;
    private String match1UGID = "found user group";
    private String match2UID = "found login id";

    @BeforeClass
    public void initialize() {
        LOGGER.info("**********Initializing in UGUserIdentification*******");
        super.initialize();

        defects = new DefectsHelper(m_cemServices);
        setupWebFilter = new SetupWebFilterHelper(m_cemServices);
        btImportFile = "BTExport_physcian_SlowTime_logiProcessor.zip";

        logInPUtils = new LoginProcessorUtils();
        try {
            logIn();
            cleanUser_UserGroup();
          //  setupTim(TIM_HOST_NAME, TIM_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********Successful Initializing in UGUserIdentification*******");
    }



    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_351184_CEM_UG_Identification_Cookie_User_Identification_Post()
        throws Exception {
        String testCaseName =
            "verify_ALM_351184_CEM_UG_Identification_Cookie_User_Identification_Post";
        String parameterTypeUGId = "Cookie";
        String parameterName = "JSESSIONID";
        String parameterType = "Post";
        String parameterNameUID = "param351184";
        String user = "u351184";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);

        addUserIdParamToApplication(appName, parameterType, parameterNameUID);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = "--data\\ " + parameterNameUID + "=" + user + "\\ " + baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        jsessionId = logInPUtils.subStringJsessionID(str);



        // timLog = tim.readTimLog();
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(jsessionId.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
            appName, businessService)));
        assertTrue(user.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports, appName,
            businessService)));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_418981_CEM_UG_Identification_Path_User_Identification_Cookie()
        throws Exception {
        String testCaseName = "verify_ALM_419018_CEM_Session_Identification_Path";
        String parameterTypeUGId = "Path";
        String parameterTypeUGIdValue = "pval418981";
        String parameterTypeUId = "Cookie";
        String parameterNameUGID = "JSESSIONID";

        String match = "4DB418981";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);


        setupAppData(appName, businessService, btImportFile);

        setup.setPathParameterDelimiters(delimiters);
        LOGGER.info("Found Text : The domain information was saved successfully");

        addUserIdParamToApplication(appName, parameterTypeUId, parameterNameUGID);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterTypeUGIdValue);

        setup.synchronizeAllMonitors();

        tim.enableRequiredTimTraces();
        tim.eraseTimLog();

        url = baseurl + "\\;" + parameterTypeUGIdValue + "=" + match; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        jsessionId = logInPUtils.subStringJsessionID(str);


        String timLog = tim.readTimLog(match1UGID);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, match, ""));
        timLog = tim.readTimLog(match2UID);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, jsessionId, ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418972_CEM_UG_Identification_Post_User_Identification_URL_Path()
        throws Exception {
        String testCaseName =
            "verify_ALM_418972_CEM_UG_Identification_Post_User_Identification_URL_Path";
        String parameterTypeUGIDId = "Post";
        String parameterTypeUIDId = "URL";
        String parameterNameUIDId = "Path";
        String parameterName = "param1";
        String postPValue = "value3125";

        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);

        addUserGroupIdParamToApplication(appName, parameterTypeUGIDId, parameterName);
        addUserIdParamToApplication(appName, parameterTypeUIDId, parameterNameUIDId);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        url = "--data\\ " + parameterName + "=" + postPValue + "\\ " + baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);


        timLog = tim.readTimLog(postPValue);
        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, postPValue, ""));
        timLog = tim.readTimLog(MED_REC_PHYSICIAN_LOGIN_PAGE);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, MED_REC_PHYSICIAN_LOGIN_PAGE, ""));


        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418973_CEM_UG_Identification_Query_User_Identification_Path()
        throws Exception {
        String testCaseName =
            "verify_ALM_418973_CEM_UG_Identification_Query_User_Identification_Path";
        String parameterTypeUGId = "Query";
        String parameterName = "xbrowser";
        String parameterNameUIDId = "Path";
        String parameterUIDIdValue = "UIDPath";
        String match1 = "user418973";
        String match = "dd4UID418973";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);
        setup.setPathParameterDelimiters(delimiters);
        addUserIdParamToApplication(appName, parameterNameUIDId, parameterUIDIdValue);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName);
        setup.synchronizeAllMonitors();

        tim.enableRequiredTimTraces();
        tim.eraseTimLog();

        url =
            baseurl + "\\;" + parameterUIDIdValue + "=" + match + "?" + parameterName + "="
                + match1; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(match1UGID);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, match1, ""));
        timLog = tim.readTimLog(match2UID);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, match, ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_351181_CEM_UG_Identification_URL_Host_User_Identification_URL_Port()
        throws Exception {
        String testCaseName =
            "verify_ALM_351181_CEM_UG_Identification_URL_Host_User_Identification_URL_Port";
        String parameterTypeUGId = "URL";
        String UGparameterName = "Host";
        String UparameterNameValue = "Port";


        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeUGId, UparameterNameValue);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, UGparameterName);
        setup.synchronizeAllMonitors();

        tim.enableRequiredTimTraces();
        tim.eraseTimLog();

        url = baseurl;// //URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(MED_REC_HOSTNAME);

        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, MED_REC_HOSTNAME, ""));
        timLog = tim.readTimLog(MED_REC_HOST_PORT.toString());
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, MED_REC_HOST_PORT.toString(), ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_418975_CEM_UG_Identification_URL_Path_User_Identification_Query()
        throws Exception {
        String testCaseName =
            "verify_ALM_418975_CEM_UG_Identification_URL_Path_User_Identification_Query";
        String parameterTypeUGId = "URL";
        String parameterName = "Path";
        String uparameterValue = "qUID";
        String uidvalue = "user123";
        String parameterType = "Query";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterType, uparameterValue);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl + "?" + uparameterValue + "=" + uidvalue; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(match1UGID);

        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, MED_REC_PHYSICIAN_LOGIN_PAGE, ""));
        timLog = tim.readTimLog(match2UID);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, uidvalue, ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_351182_CEM_UG_Identification_URL_Port_User_Identification_URL_Host()
        throws Exception {
        String testCaseName =
            "verify_ALM_351182_CEM_UG_Identification_URL_Port_User_Identification_URL_Host";
        String parameterTypeU_UGId = "URL";
        String UGparameterName = "Port";
        String UparameterName = "Host";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);


        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
        addUserGroupIdParamToApplication(appName, parameterTypeU_UGId, UGparameterName);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();

        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(MED_REC_HOST_PORT.toString());
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, MED_REC_HOST_PORT.toString(), ""));
        timLog = tim.readTimLog(MED_REC_HOSTNAME);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, MED_REC_HOSTNAME, ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_356397_CEM_TESS_Specifies_Incorrect_Regex_For_Parsing_URL_Query_Parameters()
        throws Exception {
        String testCaseName =
            "verify_ALM_356397_CEM_TESS_Specifies_Incorrect_Regex_For_Parsing_URL_Query_Parameters";

        String parameterTypeU_UGId = "Query";

        String UparameterName = "user356397";
        String uparameterValue = "userValue356397";
        String ugIdName = "userGName356397";
        String userGroupval = "user356397Group";

        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
        addUserGroupIdParamToApplication(appName, parameterTypeU_UGId, ugIdName);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);


        url =
            baseurl + "?" + ugIdName + "=" + userGroupval + "\\&" + UparameterName + "="
                + uparameterValue; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);


        LOGGER.info("Expected validation start ::" + testCaseName);


        assertTrue(userGroupval.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
            appName, businessService)));
        assertTrue(uparameterValue.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports,
            appName, businessService)));
        LOGGER.info("Tess Deafult Directory::"+tess_default+":::::");
        assertTrue(util.checkLog(tess_default, "recordingComponent.queryParamSeparators=[&;]"));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_440913_CEM_Move_An_User_To_Another_Usergroup() throws Exception {
        String testCaseName = "verify_ALM_440913_CEM_Move_An_User_To_Another_Usergroup";

        String parameterTypeU_UGId = "Query";
        String addRetunMsg = null;
        String UparameterName = "user440913";
        String uparameterValue = "user440913";
        String ugIdName = "userGName440913";
        String ugIdName2 = "userGMove440913";
        String userGroupval = "user440913Group";

        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
        addUserGroupIdParamToApplication(appName, parameterTypeU_UGId, ugIdName);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);

        // tim.eraseTimLog();
        url =
            baseurl + "?" + ugIdName + "=" + userGroupval + "\\&" + UparameterName + "="
                + uparameterValue; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);

        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(userGroupval.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
            appName, businessService)));
        assertTrue(uparameterValue.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports,
            appName, businessService)));

        addRetunMsg = admin.addUserGroup(ugIdName2, ugIdName2, true, "", false, "", "");
        LOGGER.info("UserGroup created name::" + addRetunMsg);
        assertTrue(!addRetunMsg.equals(null));
        setup.synchronizeAllMonitors();
        admin.moveUserIDToOtherGroup(uparameterValue, ugIdName2);
        // tim.eraseTimLog();
        runScriptOnUnix(scriptName.trim() + " " + url + count);
        logIn();
        assertTrue(uparameterValue.equalsIgnoreCase(logInPUtils.getUserIDValue(driver, reports,
            appName, businessService)));
        String tmp = logInPUtils.getUserGroupvalue(driver, reports, appName, businessService);
        assertTrue(ugIdName2.equalsIgnoreCase(tmp));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_418976_CEM_UG_Identification_HTTP_Request_Header_And_UserIdentifcation_URL_Host()
        throws Exception {
        String testCaseName =
            "verify_ALM_418976_CEM_UG_Identification_HTTP_Request_Header_And_UserIdentifcation_URL_Host";
        String parameterTypeUId = "URL";
        String parameterTypeUGId = "HTTP Request Header";
        String UGparameterName = "Host";
        String UparameterName = "Host";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);


        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeUId, UparameterName);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, UGparameterName);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();

        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(match1UGID);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, MED_REC_HOSTNAME, ""));
        timLog = tim.readTimLog(match2UID);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, MED_REC_HOSTNAME, ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    public void addUserGroupIdParamToApplication(String appName, String parameterTypeUGId,
        String parameterName) throws Exception {
        String paramValue = logInPUtils.paramValue(parameterTypeUGId, parameterName);

        if (!"".equals(paramValue)) {
            admin.deleteUserGroup(paramValue);
            setup.synchronizeAllMonitors();
        }
        admin.addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName);

    }

    public void addUserIdParamToApplication(String appName, String parameterTypeUId,
        String uparameterName) throws Exception {
        String paramValue = logInPUtils.paramValue(parameterTypeUId, uparameterName);
        admin.deActiveUserID();
        if (!"".equals(paramValue)) {
            admin.deleteUserID(paramValue);
            setup.synchronizeAllMonitors();
        }
        admin.addUserIdParamToApplication(appName, parameterTypeUId, uparameterName);
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
        return Util.runScriptOnUnix(TIM_HOST_NAME, TIM_REMOTELOGIN, TIM_REMOTEPWD, scriptFileName);

    }

    public void cleanUser_UserGroup() {
        LOGGER.info("Run after class clean up process for UGUserIdentification class");
        admin.deleteAllUserID();
        admin.deleteAllUserGroup();
    }
}
