package com.ca.apm.cem.logiProcessor.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.cem.logiProcessor.LoginProcessorUtils;
import com.ca.apm.tests.test.JBaseTest;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.apm.tests.cem.common.DefectsHelper;
import com.ca.apm.tests.cem.common.SetupMonitorHelper;
import com.ca.apm.tests.cem.common.SetupWebFilterHelper;

public class SessionIdentification extends JBaseTest {
    private SetupMonitorHelper setupMonitor;
    private LoginProcessorUtils logInPUtils;
    private String businessService = "medrecBS";
    private String appName = "MedrecBA";
    private String btImportFile;
    private String delimiters = ";";
    private String parameterType = "Query";
    private String sessionNameType = "Literal string";
    private String jsessionId = "";
    private String baseurl = MED_REC_BASE_URL + MED_REC_PHYSICIAN_LOGIN_PAGE;
    private String url = "";
    private String count = " 1";
    private String offsetVal = "";
    private String scriptName = SSH_SCRIPTS;
    private String tesXMLFilePath = TEXT_XML_FILE_PATH;

    @BeforeClass
    public void initialize() {
        LOGGER.info("**********Initializing in SessionIdentification*******");
        super.initialize();

        defects = new DefectsHelper(m_cemServices);
        setupMonitor = new SetupMonitorHelper(m_cemServices);
        setupWebFilter = new SetupWebFilterHelper(m_cemServices);
        btImportFile = "BTExport_physcian_SlowTime_logiProcessor.zip";
        logInPUtils = new LoginProcessorUtils();
        try {
            logIn();
            cleanUser_UserGroup();
            //setupTim(TIM_HOST_NAME, TIM_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********Successful Initializing in SessionIdentification*******");
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_419019_CEM_Session_Identification_Advanced_options() throws Exception {
        String testCaseName = "verify_ALM_419019_CEM_Session_Identification_Advanced_options";

        String parameterTypeUGId = "URL";
        String UGparameterName = "Port";
        String parameterTypeSessionId = "Cookie";
        String offset = "3";
        String length = "5";
        String sessionName = "JSESSIONID";
        String user = "u419019";
        String parameterNameUID = "userId419019";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        String match = "TranUnit";
        String timLog="";
        setupAppData(appName, businessService, btImportFile);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        setup.setPathParameterDelimiters(delimiters);


        addUserIdParamToApplication(appName, parameterType, parameterNameUID);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, UGparameterName);
        admin.addSessionIdAdvancedParamToApplication(appName, parameterTypeSessionId, sessionName,
            sessionNameType, offset, length);

        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);

        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url =
            baseurl + "?" + parameterNameUID + "=" + user ; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        jsessionId = logInPUtils.subStringJsessionID(str);



        
        offsetVal = jsessionId.substring(3, 8);
        timLog = tim.readTimLog(offsetVal);
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match, user, offsetVal));
        logIn();

        LOGGER.info("All validation complted ALM test case passed::" + testCaseName);
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_419009_CEM_Session_Identification_Cookie() throws Exception {
        String testCaseName = "verify_ALM_419009_CEM_Session_Identification_Cookie";
        String parameterTypeSessionId = "Cookie";
        String parameterName = "JSESSIONID";
        String parameterType = "Post";
        String parameterNameUID = "param1";
        String postPValue = "value419009";
        String userGroup = "ug419009";
        String user = "u419009";
        String parameterNameUGID = "ugid419009";
        String timLog="";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        String match = "map login";
        setupAppData(appName, businessService, btImportFile);

        addUserIdParamToApplication(appName, parameterType, parameterNameUID);
        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
            sessionNameType);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted :: " + testCaseName);
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url =
            "--data\\ param1=" + postPValue + "\\ " + baseurl + "?" + parameterNameUID + "=" + user
                + "\\&" + parameterNameUGID + "=" + userGroup; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        jsessionId = logInPUtils.subStringJsessionID(str);
        logIn();


        
        offsetVal = jsessionId.substring(3, 8);
        timLog = tim.readTimLog(offsetVal);
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match, postPValue, offsetVal));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_419018_CEM_Session_Identification_Path() throws Exception {
        String testCaseName = "verify_ALM_419018_CEM_Session_Identification_Path";
        String parameterTypeSessionId = "Path";
        String parameterName = "sid419018";
        String parameterNameUGID = "ugid419018";
        String parameterNameUID = "userId419018";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        String match1 = "session id for appdef";
        String match = "4DB7419018";

        setupAppData(appName, businessService, btImportFile);
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        setup.setPathParameterDelimiters(delimiters);
        LOGGER.info("Found Text : The domain information was saved successfully");

        addUserIdParamToApplication(appName, parameterType, parameterNameUID);
        addUserGroupIdParamToApplication(appName, parameterType, parameterNameUGID);
        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
            sessionNameType);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        // http://10.131.72.186:6001/CEMTestApp/home.jsp;jsessionid=4DB7?sleep=4
        url = baseurl + "\\;" + parameterName + "=" + match;// URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(match);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1, appName, match));



        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419017_CEM_Session_Identification_Plugin() throws Exception {
        String testCaseName = "verify_ALM_419017_CEM_Session_Identification_Plugin";
        String parameterTypeSessionId = "Plug-in";
        String parameterName = "xbrowser";
        String pluginName = "SessionPlugIn";
        String businessTransaction = "login.action";
        String ip = MED_REC_HOST_IP;
        String request = "";
        String sessionName = "plugInRec419017";
        String componentidValue = "";
        StringBuffer str = new StringBuffer();
        try {
            String jarFileLocation = getEnvConstValue("plug_in_Jar_with_Location");
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            CreatBABS(appName, businessService);
            setup.createPlugin(pluginName, ip, request, jarFileLocation);
            setup.enablePlugin();
            tim.enableRequiredTimTraces();
            tim.goToTimNetWorkInterface();
            //tim.setLoopback();
            logIn();

            url = baseurl; // URL
            admin.newRecordingSession(true, sessionName, TIM_HOST_NAME, "", "", "", scriptName,
                url, count);

            admin.promoteRecordingSession(sessionName, businessService, businessTransaction);
            enableBusinessServiceMonitoring(businessService);
            componentidValue = admin.getRecordPlugInParam(sessionName);
            admin.enableSlowTimeBusinessTransactionDefects(businessService, businessTransaction);
            admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
                sessionNameType);
            setup.synchronizeAllMonitors();


            url = baseurl; // URL
            str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
            assertTrue(validateplugIn(componentidValue));

            LOGGER.info("All validation complted ALM test case passed::" + testCaseName);

        } finally {
            //tim.disableLoopback();
            setup.disablePlugin();
            setup.deletePlugin();
            admin.deleteRecordingSession(sessionName);
            setup.synchronizeAllMonitors();

        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }



    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419012_CEM_Session_Identification_Post() throws Exception {
        String testCaseName = "verify_ALM_419012_CEM_Session_Identification_Post";
        String parameterTypeSessionId = "Post";
        String parameterName = "param1";
        String postPValue = "value419012";
        String userGroup = "ug419012";
        String user = "u419012";
        String parameterNameUGID = "ugid419012";
        String parameterNameUID = "userId419012";
        String match = "session id for appdef";
        String timLog="";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
            sessionNameType);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        url =
            "--data\\ param1=" + postPValue + "\\ " + baseurl + "?" + parameterNameUID + "=" + user
                + "\\&" + parameterNameUGID + "=" + userGroup; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);


        timLog = tim.readTimLog(postPValue);
        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match, appName, postPValue));


        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419013_CEM_Session_Identification_Query() throws Exception {
        String testCaseName = "verify_ALM_419013_CEM_Session_Identification_Query";
        String parameterTypeSessionId = "Query";
        String parameterName = "xbrowser";
        String match1 = "session id for appdef";
        String user = "usr419013";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
            sessionNameType);
        setup.synchronizeAllMonitors();

        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        // http://10.131.72.186:6001/CEMTestApp/home.jsp;jsessionid=4DB7?sleep=4
        url = baseurl + "?" + parameterName + "=" + user; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        logIn();
        String timLog = tim.readTimLog(user);
        
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1, appName, user));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419007_CEM_Session_Identification_URL_Host() throws Exception {
        String testCaseName = "verify_ALM_419013_CEM_Session_Identification_Query";
        String parameterTypeSessionId = "URL";
        String parameterName = "Host";
        String match1 = "session id for appdef";
        String user = "u419007";
        // String match="www.google.com";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName, "");
        setup.synchronizeAllMonitors();

        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        // http://10.131.72.186:6001/CEMTestApp/home.jsp;jsessionid=4DB7?sleep=4
        url = baseurl + "?" + parameterName + "=" + user; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        logIn();


        String timLog = tim.readTimLog(MED_REC_HOSTNAME);
        
        LOGGER.info("Expected validation start ::" + testCaseName);

        assertTrue(logInPUtils.regexPattern(timLog, match1, appName, MED_REC_HOSTNAME));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419014_CEM_Session_Identification_URL_Path() throws Exception {
        String testCaseName = "verify_ALM_419014_CEM_Session_Identification_URL_Path";
        String parameterTypeSessionId = "URL";
        String parameterName = "Path";
        String match1 = "session id for appdef";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName, "");
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(MED_REC_PHYSICIAN_LOGIN_PAGE);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1, appName, MED_REC_PHYSICIAN_LOGIN_PAGE));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419008_CEM_Session_Identification_URL_Port() throws Exception {
        String testCaseName = "verify_ALM_419008_CEM_Session_Identification_URL_Port";
        String parameterTypeSessionId = "URL";
        String parameterName = "Port";
        String match1 = "session id for appdef";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);


        setupAppData(appName, businessService, btImportFile);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName, "");
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();

        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(MED_REC_HOST_PORT);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1, appName, MED_REC_HOST_PORT));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }



    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419010_CEM_Session_Interim_Identification_Locaiton_URL()
        throws Exception {
        String testCaseName = "verify_ALM_419010_CEM_Session_Interim_Identification_Locaiton_URL";
        String parameterTypeSessionId = "Query";
        String parameterName = "sessionid";
        String interimParameterTypeSessionId = "Location URL";
        String interiPparameterName = "Port";
        String match1 = "interim-session";
        String match = "xxxx419010";
        String match2 = "xxxxsession2419010";
        String parameterNameUGID = "ugid419010";
        String parameterNameUID = "u419010";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterType, parameterNameUID);
        addUserGroupIdParamToApplication(appName, parameterType, parameterNameUGID);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
            sessionNameType);
        admin.addInterimSessionIdParamToApplication(appName, interimParameterTypeSessionId,
            interiPparameterName);

        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        // http://10.131.72.186:6001/CEMTestApp/home.jsp?sleep=4&userId=xxxx
        // http://10.131.72.186:6001/CEMTestApp/home.jsp?sleep=4&sesionId=session2
        url = baseurl + "?sleep=4\\&" + parameterNameUID + "=" + match; // URL1
        runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        url = baseurl + "?sleep=4\\&" + parameterName + "=" + match2; // URL1
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);


        String timLog = tim.readTimLog(match);

        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1, match, MED_REC_HOST_PORT));
        timLog = tim.readTimLog(match2);
        assertTrue(logInPUtils.regexPattern(timLog, match1, match2, MED_REC_HOST_PORT));


        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419011_CEM_Session_Interim_Identification_Locaiton_Query()
        throws Exception {
        String testCaseName = "verify_ALM_419011_CEM_Session_Interim_Identification_Locaiton_Query";
        String parameterTypeSessionId = "Query";
        String parameterName = "sessionid";
        String interimParameterTypeSessionId = "Location Query";
        String interiPparameterName = "IsessionId";
        String matchv1 = "map interim-session";
        String match2v1_2 = "xyz";
        String match3v1 = "session1";
        String matchv2 = "interim-session";
        String match2v2 = "xxxx419011";
        String match3 = "venkat419011";
        String parameterNameUGID = "ugid419011";
        String parameterNameUID = "u419011";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterType, parameterNameUID);
        addUserGroupIdParamToApplication(appName, parameterType, parameterNameUGID);

        admin.addSessionIdParamToApplication(appName, parameterTypeSessionId, parameterName,
            sessionNameType);
        admin.addInterimSessionIdParamToApplication(appName, interimParameterTypeSessionId,
            interiPparameterName);
        setup.synchronizeAllMonitors();
        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        // http://10.131.72.186:6001/CEMTestApp/home.jsp?sleep=4&userId=xxxx&IsessionId=xyz
        // http://10.131.72.186:6001/CEMTestApp/home.jsp?sleep=4&IsessionId=xyz&sessionId=session1
        // http://10.131.72.186:6001/CEMTestApp/home.jsp?sleep=4&userId=venkat
        url =
            baseurl + "?sleep=4\\&" + parameterNameUID + "=" + match2v2 + "\\&"
                + interiPparameterName + "=" + match2v1_2; // URL1
        runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        url =
            baseurl + "?sleep=4\\&" + interiPparameterName + "=" + match2v1_2 + "\\&"
                + parameterName + "=" + match3v1; // URL2
        runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        url = baseurl + "?sleep=4\\&" + parameterNameUID + "=" + match3; // URL3
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(matchv1);

        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, matchv1, match2v1_2, match3v1));
        timLog = tim.readTimLog(match2v2);
        assertTrue(logInPUtils.regexPattern(timLog, matchv2, match2v2, match2v1_2));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419015_CEM_Session_Identification_XML_Select_Recording_Session()
        throws Exception {
        String testCaseName =
            "verify_ALM_419015_CEM_Session_Identification_XML_Select_Recording_Session";

        String parameterTypeUGId = "XML";
        String businessTransaction = "login.action";
        String recsessionName = "xmlRec419015";
        String xmlAttribute = "test message";
        String recMsg = "";
        String match1 = "session id for appdef";

        StringBuffer str = new StringBuffer();
        try {
            setupTest(testCaseName);
            startTestCaseName(testCaseName);
            CreatBABS(appName, businessService);

            url =
                "-H\\ \'Content-Type:text/xml\'\\ --data-binary\\ @" + tesXMLFilePath + "\\ "
                    + baseurl; // URL
            recMsg =
                admin.newRecordingSession(true, recsessionName, TIM_HOST_NAME, "", "", "",
                    scriptName, url, count);
            LOGGER.info("After recoding return Message:::: " + recMsg);
            if (!recMsg.trim().equals("")) {
                LOGGER.info("*****Validate XML file present in speciified path********* "
                    + tesXMLFilePath);
            }
            assertTrue(recMsg.trim().equals(""));
            admin.promoteRecordingSession(recsessionName, businessService, businessTransaction);

            enableBusinessServiceMonitoring(businessService);
            admin.enableSlowTimeBusinessTransactionDefects(businessService, businessTransaction);

            addXMLSessionParamToApplication(appName, parameterTypeUGId, recsessionName,
                businessTransaction, xmlAttribute);

            setup.synchronizeAllMonitors();

            tim.enableRequiredTimTraces();
            tim.eraseTimLog();
            str = runScriptOnUnix(scriptName.trim() + " " + url + count);

            String timLog = tim.readTimLog(match1);
            logIn();
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);


            assertTrue(logInPUtils.regexPattern(timLog, match1, appName, xmlAttribute));
        } finally {
            admin.deleteUserGroup(xmlAttribute);
            admin.deleteRecordingSession(recsessionName);
            setup.synchronizeAllMonitors();
        }
        LOGGER.info("All validation complted ALM test case passed::" + testCaseName);


        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    private void addXMLSessionParamToApplication(String appName, String parameterTypeSId,
        String recsessionName, String businessTransaction, String xmlAttribute) throws Exception {
        if (!"".equals(parameterTypeSId)) {
            admin.deleteUserGroup(parameterTypeSId);
            setup.synchronizeAllMonitors();
        }
        admin.addXMLTypeToAppplicationSessionOrUserOrUserGroupIdentification("session", appName,
            parameterTypeSId, recsessionName, businessTransaction, xmlAttribute);
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


    private void enableBusinessServiceMonitoring(String businessService2) {
        logInPUtils.enableBusinessServiceMonitoring(setupMonitor, admin, businessService);

    }

    private void CreatBABS(String appName2, String businessService2) {
        logInPUtils.creatBABS(adminBA, setupMonitor, admin, appName2, businessService2);

    }

    private boolean validateplugIn(String componentidValue) throws Exception {

        return logInPUtils.validateplugIn(driver, reports, componentidValue, appName,
            businessService);
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
        admin.deleteAllUserID();
        admin.deleteAllUserGroup();
    }
}
