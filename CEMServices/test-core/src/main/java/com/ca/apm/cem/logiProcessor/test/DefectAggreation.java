package com.ca.apm.cem.logiProcessor.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.cem.logiProcessor.LoginProcessorUtils;
import com.ca.apm.tests.test.JBaseTest;
import com.ca.apm.tests.utility.DBUtil;
import com.ca.apm.tests.utility.OracleUtil;
import com.ca.apm.tests.utility.PostgresUtil;
import com.ca.apm.tests.utility.QaUtils;
import com.ca.apm.tests.utility.Util;
import com.ca.apm.tests.cem.common.DefectsHelper;
import com.ca.apm.tests.cem.common.SetupWebFilterHelper;

public class DefectAggreation extends JBaseTest {

    QaUtils util = new QaUtils();
    public final long miliSec = 3960000;
    private LoginProcessorUtils logInPUtils;
    private String businessService = "medrecBS";
    private String appName = "MedrecBA";
    private String btImportFile;
    private OracleUtil dbo;
    private DBUtil db;
    private PostgresUtil dbp;
    private String baseurl = MED_REC_BASE_URL + MED_REC_PHYSICIAN_LOGIN_PAGE;
    private String url = "";
    private String count = " 1";

    private String scriptName = SSH_SCRIPTS;
    private String match1UGID = "found user group";
    private String match2UID = "found login id";

    @BeforeClass
    public void initialize() {
        LOGGER.info("**********Initializing in CEM Bugs*******");
        super.initialize();

        defects = new DefectsHelper(m_cemServices);
        setupWebFilter = new SetupWebFilterHelper(m_cemServices);
        btImportFile = "BTExport_physcian_SlowTime_logiProcessor.zip";
        logInPUtils = new LoginProcessorUtils();
        try {
            logIn();
            initDB();
            setupTim(TIM_HOST_NAME, TIM_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("**********Successful Initializing in CEM Bugs*******");
    }


    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351155_CEM_Verify_that_Login_Processor_Works() throws Exception {
        String testCaseName = "verify_ALM_351155_CEM_Verify_that_Login_Processor_Works";
        String parameterTypeU_UGId = "URL";
        String UGparameterName = "Port";
        String UparameterName = "Host";
        String ts_transet_id = "";
        String ts_user_incarnation_id = "";
        String ts_biz_event_Id = "";
        String ts_defect_def_id = "";
        String resultOne = "";
        String fromTime = getCurrentTime();
        String toTime = getAggreationTime();
        String strQuery1 =
            "select ts_transet_id, ts_user_incarnation_id, ts_biz_event_Id, ts_defect_def_id, count(*) from ts_defects where ts_occur_date >= '"
                + fromTime
                + "' and ts_occur_date < '"
                + toTime
                + "' group by ts_transet_id, ts_user_incarnation_id, ts_biz_event_Id, ts_defect_def_id";
        String strQuery2 = "";
        ArrayList<ArrayList<String>> list1;
        ArrayList<String> mainList;
        String secondResult;
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


        tim.goToTimLogPage();
        String timLog = tim.readTimLog(match1UGID);
        logIn();
        LOGGER.info("Expected validation start ::" + testCaseName);
        assertTrue(logInPUtils.regexPattern(timLog, match1UGID, MED_REC_HOST_PORT.toString(), ""));
        timLog = tim.readTimLog(match2UID);
        assertTrue(logInPUtils.regexPattern(timLog, match2UID, MED_REC_HOSTNAME, ""));
        Thread.sleep(miliSec);
        LOGGER.info("DB query first to execute ::" + strQuery1);
        list1 = db.getArrayListStringFromQuery(strQuery1);
        LOGGER.info("database result for first query:::" + list1);
        Iterator<ArrayList<String>> iterator = list1.iterator();
        while (iterator.hasNext()) {
            mainList = (ArrayList<String>) iterator.next();
            Iterator<String> iterator2 = mainList.iterator();
            while (iterator2.hasNext()) {
                ts_transet_id = (String) iterator2.next();
                ts_user_incarnation_id = (String) iterator2.next();
                ts_biz_event_Id = (String) iterator2.next();
                ts_defect_def_id = (String) iterator2.next();
                resultOne = (String) iterator2.next();
                break;
            }
            break;
        }
        strQuery2 =
            "select ts_defect_count from ts_defects_interval where ts_transet_id = '"
                + ts_transet_id + "'and ts_user_incarnation_id='" + ts_user_incarnation_id
                + "'and ts_biz_event_Id='" + ts_biz_event_Id + "'and ts_defect_def_id='"
                + ts_defect_def_id + "'";

        LOGGER.info("DB query second to execute ::" + strQuery2);
        secondResult = db.getResultStringFromQuery(strQuery2);
        LOGGER.info("database result for second query:::" + secondResult);
        assertTrue(resultOne.equalsIgnoreCase(secondResult));
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
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


    private void setupTim(String timName, String timIP) {
        try {
            setup.createMonitor(timName, timIP);
            setup.enableMonitor(timName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initDB() {

        if (DB_TYPE.equalsIgnoreCase("Oracle")) {

            dbo = new OracleUtil(DB_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
            db = dbo;
        } else {
            System.out.println("Initializing dbp with: " + DB_HOST + "," + DB_PORT + "," + DB_NAME);
            dbp = new PostgresUtil(DB_HOST, DB_PORT, DB_NAME, DB_OWNER, DB_PASSWORD);
            db = dbp;
        }
        System.out.println("DB Host:" + DB_HOST);
    }

    private StringBuffer runScriptOnUnix(String scriptFileName) {
        return Util.runScriptOnUnix(TIM_HOST_NAME, TIM_REMOTELOGIN, TIM_REMOTEPWD, scriptFileName);

    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.FFFFFFFFF aaa");
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) - 1);
        return dateFormat.format(cal.getTime());

    }

    private String getAggreationTime() {
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.FFFFFFFFF aaa");
        System.out.println(cal.get(Calendar.HOUR));
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 6);

        return dateFormat.format(cal.getTime());
    }
}
