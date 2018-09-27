package com.ca.apm.cem.logiProcessor.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.cem.logiProcessor.LoginProcessorUtils;
import com.ca.apm.tests.test.JBaseTest;
import com.ca.apm.tests.utility.QaUtils;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.utility.WebdriverWrapper;
import com.ca.apm.tests.cem.common.SetupMonitorHelper;

public class UGIdentification extends JBaseTest {

    QaUtils util = new QaUtils();// TODO once JBaseTest initialization work remove this line
    private String emLogFile = util.convertPathSeparators(TESS_INSTALLDIR
        + "logs/IntroscopeEnterpriseManager.log");
    private SetupMonitorHelper setupMonitor;
    private LoginProcessorUtils logInPUtils;
    private String businessService = "medrecBS";
    private String appName = "MedrecBA";
    private String btImportFile;
    private String petshopBTImportFile;
    private String delimiters = ";";
    private String jsessionId = "";
    private String match1UGID = "found user group";
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
          //  setupTim(TIM_HOST_NAME, TIM_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********Successful Initializing in UGIdentification*******");
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418983_CEM_User_Group_Identification_Cookie_Group_Advanced_options()
        throws Exception {
        String testCaseName = "verify_ALM_419019_CEM_Session_Identification_Advanced_options";

        String parameterTypeUId = "URL";
        String UparameterName = "Host";
        String parameterTypeUGId = "Cookie";
        String offset = "3";
        String length = "5";
        String ugIdName = "JSESSIONID";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);


        setup.setPathParameterDelimiters(delimiters);

        addUserIdParamToApplication(appName, parameterTypeUId, UparameterName);
        addAdvancedUserGroupIdParamToApplication(appName, parameterTypeUGId, ugIdName, offset,
            length);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);


        tim.enableRequiredTimTraces();
        tim.eraseTimLog();
        url = baseurl;// + "?" + parameterNameUID + "=" + user + "\\&" + parameterNameUGID + "="+
                      // userGroup; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        jsessionId = logInPUtils.subStringJsessionID(str);



        timLog = tim.readTimLog(match1UGID);
        offsetVal = jsessionId.substring(3, 8);
        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, offsetVal, ""));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }



    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418980_CEM_User_Group_Identification_Plugin() throws Exception {
        String testCaseName = "verify_ALM_418980_CEM_User_Group_Identification_Plugin";
        String parameterTypeUId = "URL";
        String UparameterName = "Host";
        String parameterTypeUGId = "Plug-in";
        String parameterName = "component-id";
        String pluginName = "SessionPlugIn";
        String businessTransaction = "login.action";
        String ip = MED_REC_HOST_IP;
        String request = "";
        String sessionName = "plugInRec418980";
        String errMsg = "";
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
            tim.setLoopback();
            logIn();

            url = baseurl; // URL
            errMsg =
                admin.newRecordingSession(true, sessionName, TIM_HOST_NAME, "", "", "", scriptName,
                    url, count);
            LOGGER.info("After recoding Message:::" + errMsg);
            assertTrue(errMsg.trim().equals(""));
            admin.promoteRecordingSession(sessionName, businessService, businessTransaction);

            enableBusinessServiceMonitoring(businessService);
            componentidValue = admin.getRecordPlugInParam(sessionName);
            admin.enableSlowTimeBusinessTransactionDefects(businessService, businessTransaction);
            addUserIdParamToApplication(appName, parameterTypeUId, UparameterName);
            addUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName);
            setup.synchronizeAllMonitors();

            tim.eraseTimLog();
            url = baseurl; // URL
            str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
            WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

            timLog = tim.readTimLog(match1UGID);
            logIn();
            LOGGER.info("plugin parameterName" + parameterName + "--value--" + componentidValue);
            assertTrue(logInPUtils.regexPattern(timLog, match1UGID,
                getUserGroupvalue(appName, businessService), ""));
            LOGGER.info("All validation complted ALM test case passed::" + testCaseName);

        } finally {
            tim.disableLoopback();
            admin.deleteRecordingSession(sessionName);
            setup.synchronizeAllMonitors();
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }


    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418974_CEM_User_Group_Identification_URL_Port() throws Exception {
        String testCaseName = "verify_ALM_418974_CEM_User_Group_Identification_URL_Port";
        String parameterTypeU_UGId = "URL";
        String UGparameterName = "Port";
        String UparameterName = "Host";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);


        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
        addUserGroupIdParamToApplication(appName, parameterTypeU_UGId, UGparameterName);
        setup.synchronizeAllMonitors();
        //
        // tim.enableRequiredTimTraces();
        // tim.eraseTimLog();

        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        String timLog = tim.readTimLog(match1UGID);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, MED_REC_HOST_PORT.toString(), ""));


        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    // new
    public void verify_ALM_418982_CEM_User_Group_Identification_IP_Subnet_Mask() throws Exception {
        String testCaseName = "verify_ALM_418982_CEM_User_Group_Identification_IP_Subnet_Mask";
        String subnetMask = "255.255.255.0";
        String parameterTypeU_UGId = "URL";
        String UparameterName = "Client IP";
        String UGparameterType = "IP Subnet Mask";
        String userGroupName = "";
        String tmpSubnetIP = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        tmpSubnetIP = subnetIP(TIM_IP);
        userGroupName = "UserGroup-" + tmpSubnetIP;
        admin.deleteUserGroup(userGroupName);
        setupAppData(appName, businessService, btImportFile);
        addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
        addUserGroupIdParamToApplication(appName, UGparameterType, subnetMask);
        setup.synchronizeAllMonitors();
        //
        // tim.enableRequiredTimTraces();
        // tim.eraseTimLog();

        url = baseurl; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(userGroupName.equalsIgnoreCase(getUserGroupvalue(appName, businessService)));


        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }


    @Test(groups = {"SMOKE"}, enabled = true)
    // new
    public void verify_ALM_432944_CEM_User_Group_Identification_IP_Subnet_Mask_At_Domains_Page()
        throws Exception {
        String testCaseName =
            "verify_ALM_432944_CEM_User_Group_Identification_IP_Subnet_Mask_At_Domains_Page";
        String subnetMask = "255.255.255.0";
        String parameterTypeUId = "URL";
        String UparameterName = "Client IP";
        String userGroupName = "";
        String tmpSubnetIP = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        try {
            tmpSubnetIP = subnetIP(TIM_IP);
            userGroupName = "UserGroup-" + tmpSubnetIP;
            admin.deleteUserGroup(userGroupName);
            setupAppData(appName, businessService, btImportFile);
            setup.setUserGroupSettings(subnetMask);
            addUserIdParamToApplication(appName, parameterTypeUId, UparameterName);
            setup.synchronizeAllMonitors();
            //
            // tim.enableRequiredTimTraces();
            tim.eraseTimLog();

            url = baseurl; // URL
            StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
            logIn();
            LOGGER.info("Expected validation start ::" + testCaseName);

            assertTrue(userGroupName.equalsIgnoreCase(getUserGroupvalue(appName, businessService)));
        } finally {
            setup.disableUserGroupSettings();
        }

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    // new
    public void verify_ALM_421298_CEM_User_Group_Identification_IP_Subnet_Mask_And_Modifed_UG_Name()
        throws Exception {
        String testCaseName =
            "verify_ALM_421298_CEM_User_Group_Identification_IP_Subnet_Mask_And_Modifed_UG_Name";
        String subnetMask = "255.255.255.0";
        String parameterTypeU_UGId = "URL";
        String UparameterName = "Client IP";
        String UGparameterType = "IP Subnet Mask";
        String newUserGroup = "UserGroup421298";
        String tmpSubnetIP = "";
        String userGroupName = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        try {
            tmpSubnetIP = subnetIP(TIM_IP);
            userGroupName = "UserGroup-" + tmpSubnetIP;
            admin.deleteUserGroup(userGroupName);
            setupAppData(appName, businessService, btImportFile);
            addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
            addUserGroupIdParamToApplication(appName, UGparameterType, subnetMask);
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

            assertTrue(logInPUtils.regexPattern(timLog, match1UGID, "UserGroupTim", tmpSubnetIP));
            assertTrue(userGroupName.equalsIgnoreCase(getUserGroupvalue(appName, businessService)));
            admin.editUserGroupIdParam(userGroupName, newUserGroup);
            str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            assertTrue(newUserGroup.equalsIgnoreCase(getUserGroupvalue(appName, businessService)));
        } finally {
            admin.deleteUserGroup(newUserGroup);
        }

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    // new
    public void verify_ALM_381671_CEM_78244_CBFV91B_Creation_of_UserGroup_results_in_error_with_Oracle_as_APM_DB()
        throws Exception {
        String testCaseName =
            "verify_ALM_381671_CEM_78244_CBFV91B_Creation_of_UserGroup_results_in_error_with_Oracle_as_APM_DB";
        String subnetMask = "255.255.255.0";

        String tmpSubnetIP = "";
        String userGroupName = "";
        String addRetunMsg = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);

        try {
            tmpSubnetIP = subnetIP(TIM_IP);
            userGroupName = "UserGroup-" + tmpSubnetIP;
            admin.deleteUserGroup(userGroupName);
            setupAppData(appName, businessService, btImportFile);
            addRetunMsg =
                admin.addUserGroup(userGroupName, userGroupName, true, "", true, tmpSubnetIP,
                    subnetMask);
            LOGGER.info("User Add msg::" + addRetunMsg);
            LOGGER.info("Expected validation start ::" + testCaseName);

            assertTrue(addRetunMsg.equals(""));
            assertFalse(util.checkLog(emLogFile, "[ERROR]"));

        } finally {

            admin.deleteUserGroup(userGroupName);
        }

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_351188_CEM_User_Group_Identification_setting_Advanced_value()
        throws Exception {
        String testCaseName =
            "verify_ALM_351188_CEM_User_Group_Identification_setting_Advanced_value";

        String parameterTypeU_UGId = "Query";
        String offset = "3";
        String length = "5";
        String UparameterName = "user351188";
        String uparameterValue = "uservalue351188";
        String ugIdName = "userGName351188";
        String userGroup = "userGroup351188";
        String errMsg = "";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);

        errMsg =
            admin.addAdvancedUserGroupIdParamToApplication(appName, parameterTypeU_UGId, ugIdName,
                "", "");
        WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);
        assertTrue(errMsg.contains("Length is required."));
        assertTrue(errMsg.contains("Offset is required."));
        addUserIdParamToApplication(appName, parameterTypeU_UGId, UparameterName);
        addAdvancedUserGroupIdParamToApplication(appName, parameterTypeU_UGId, ugIdName, offset,
            length);
        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);

        tim.eraseTimLog();
        url =
            baseurl + "?" + ugIdName + "=" + userGroup + "\\&" + UparameterName + "="
                + uparameterValue; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        timLog = tim.readTimLog(match1UGID);
        offsetVal = userGroup.substring(3, 8);
        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, offsetVal, ""));
        assertTrue(offsetVal.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
            appName, businessService)));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_418977_User_Group_Identification_XML_Select_Recording_Session()
        throws Exception {
        String testCaseName =
            "verify_ALM_418977_User_Group_Identification_XML_Select_Recording_Session";
        String parameterTypeUId = "Query";
        String UparameterName = "user418977";
        String uparameterValue = "uservalue418977";
        String parameterTypeUGId = "XML";
        String businessTransaction = "login.action";
        String recsessionName = "xmlRec418977";
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
            addUserIdParamToApplication(appName, parameterTypeUId, UparameterName);
            addXMLUserGroupIdParamToApplication(appName, parameterTypeUGId, recsessionName,
                businessTransaction, xmlAttribute);

            setup.synchronizeAllMonitors();

            url = url + "?" + UparameterName + "=" + uparameterValue;
            tim.eraseTimLog();
            str = runScriptOnUnix(scriptName.trim() + " " + url + count);

            String timLog = tim.readTimLog(match1UGID);
            logIn();
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);
            // WebdriverWrapper.waitForPageToLoad(driver, iGlobalTimeout);

            assertTrue(logInPUtils.regexPattern(timLog, match1UGID, xmlAttribute, ""));
            LOGGER.info("All validation complted ALM test case passed::" + testCaseName);

        } finally {
            admin.deleteUserGroup(xmlAttribute);
            admin.deleteRecordingSession(recsessionName);
            setup.synchronizeAllMonitors();
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_351186_CEM_User_Group_Identification_Basic_Authentication()
        throws Exception {
        String testCaseName =
            "verify_ALM_351186_CEM_User_Group_Identification_Basic_Authentication";
        String parameterTypeUId = "Query";
        String parameterTypeUGId = "Basic Authentication";
        String uparameterName = "user351186";
        String uparameterValue = "uservalue351186";
        String uGIdName = "UserName";
        String uGIdNamevalue = "UserGroup351186";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        setupAppData(appName, businessService, btImportFile);

        addUserIdParamToApplication(appName, parameterTypeUId, uparameterName);
        addUserGroupIdParamToApplication(appName, parameterTypeUGId, uGIdName);

        setup.synchronizeAllMonitors();
        LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);

        tim.eraseTimLog();
        url =
            "-v\\ --basic\\ -u\\ \'" + uGIdNamevalue + ":password123\'\\ " + baseurl + "?"
                + uparameterName + "=" + uparameterValue; // URL
        StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
        LOGGER.info("hit URL::" + url);
        LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



        timLog = tim.readTimLog(match1UGID);

        LOGGER.info("Expected validation start ::" + testCaseName);
        logIn();
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, uGIdNamevalue, ""));
        assertTrue(uGIdNamevalue.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
            appName, businessService)));

        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_351185_CEM_User_Group_Identification_NTLM_Authentication()
        throws Exception {
        String testCaseName = "verify_ALM_351185_CEM_User_Group_Identification_NTLM_Authentication";
        String baseurl = "";

        String parameterTypeU_UGId = "NTLM Authentication";
        String u_ugIdName = "UserName";
        String u_ugIdNamevalue = "administrator";
        String u_ugIdpassword = "Notallowed@ca";
        String appName = "petshopBA";
        String businessService = "petshopBS";
        boolean result = false;
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        try {
            setupAppData(appName, businessService, petshopBTImportFile);
            admin.deleteUserGroup(u_ugIdNamevalue);
            admin.deleteUserID(u_ugIdNamevalue);
            addUserIdParamToApplication(appName, parameterTypeU_UGId, u_ugIdName);
            addUserGroupIdParamToApplication(appName, parameterTypeU_UGId, u_ugIdName);

            setup.synchronizeAllMonitors();
            LOGGER.info("All Prerequisites for Test complted for :: " + testCaseName);

            tim.eraseTimLog();
            baseurl = PETSHOP_BASE_URL + PETSHOP_LOGIN_PAGE;
            url =
                "-v\\ -u\\ \'" + u_ugIdNamevalue + ":" + u_ugIdpassword + "\'\\ " + "--ntlm\\ "
                    + baseurl; // URL

            StringBuffer str = runScriptOnUnix(scriptName.trim() + " " + url + count);
            LOGGER.info("hit URL::" + url);
            LOGGER.info("TestApp hit Process of Defect generation -- Scripts run output::" + str);



            timLog = tim.readTimLog(match1UGID);

            LOGGER.info("Expected validation start ::" + testCaseName);
            logIn();
            result =
                u_ugIdNamevalue.equalsIgnoreCase(logInPUtils.getUserGroupvalue(driver, reports,
                    appName, businessService, u_ugIdNamevalue));
            if (!result) {
                LOGGER.info("****validate NTLM Auth enable in IIS servwer for PETSHOP Application and petshop url accessible****** ");
                LOGGER.info("****To enable NTLM Go to ISS server > application> right click > properties > Directory Security >Edit Auth and Access control > select intigarated windows Auth  ****** ");
            }
            assertTrue(logInPUtils.regexPattern(timLog, match1UGID, u_ugIdNamevalue, ""));
            assertTrue(result);
        } finally {
            adminBA.deleteBusinessApplicationByName(appName);
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }

    private void addXMLUserGroupIdParamToApplication(String appName, String parameterTypeUGId,
        String recsessionName, String businessTransaction, String xmlAttribute) throws Exception {
        if (!"".equals(xmlAttribute)) {

            admin.deleteUserGroup(xmlAttribute);
            setup.synchronizeAllMonitors();
        }
        admin.addXMLTypeToAppplicationSessionOrUserOrUserGroupIdentification("userGroup", appName,
            parameterTypeUGId, recsessionName, businessTransaction, xmlAttribute);


    }

    private void addAdvancedUserGroupIdParamToApplication(String appName, String parameterTypeUGId,
        String parameterName, String offset, String length) throws Exception {
        String paramValue = logInPUtils.paramValue(parameterTypeUGId, parameterName);

        if (!"".equals(paramValue)) {
            admin.deleteUserGroup(paramValue);

            setup.synchronizeAllMonitors();
        }
        admin.addAdvancedUserGroupIdParamToApplication(appName, parameterTypeUGId, parameterName,
            offset, length);

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

    private void CreatBABS(String appName, String businessService) {
        logInPUtils.creatBABS(adminBA, setupMonitor, admin, appName, businessService);

    }

    private String getUserGroupvalue(String appName, String businessService) throws Exception {

        return logInPUtils.getUserGroupvalue(driver, reports, appName, businessService);
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

    private String subnetIP(String tim_IP) {
        String subnetIP = "";
        LOGGER.info("TIM IP for subnet::" + tim_IP);
        if (!tim_IP.equals(null) || !tim_IP.trim().equals("")) {
            String str[] = tim_IP.split("\\.");
            subnetIP = str[0] + "." + str[1] + "." + str[2] + ".0";
        }
        return subnetIP;
    }

    public void cleanUser_UserGroup() {
        admin.deleteAllUserID();
        admin.deleteAllUserGroup();
    }

}
