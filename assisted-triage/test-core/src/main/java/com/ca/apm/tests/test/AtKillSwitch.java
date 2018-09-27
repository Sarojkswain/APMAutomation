package com.ca.apm.tests.test;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class AtKillSwitch extends TasTestNgTest {


    private final Logger log = LoggerFactory.getLogger(getClass());

    private String batFile = "run.bat";
    private String batLocation = AssistedTriageTestbed.TOMCAT_INSTALL_DIR
        + "\\webapps\\pipeorgan\\WEB-INF\\lib\\";

    private String scenarioFolderName = "scenarios";
    private Common common = new Common();
    private String agcHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"pipeorgan_generic_tests"})
    private void atKillSwitch_TestCase() throws InterruptedException, ClassNotFoundException,
        SQLException {
        disableAT(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
        restartEMandWV(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
        Timestamp Start_Time = common.getCurrentTimeinISO8601Format(0);
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER,
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-BTServletDBTC.xml"));
        runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER,
            common.runPipeOrganScenario(batLocation, batFile, scenarioFolderName,
                "Problem-DefaultAnalysts.xml"));
        
        log.info("Sleeping for 3 mins while scenario is generated");
        Thread.sleep(180000);
        
        if (checkNumberofStories(agcHost, Start_Time) > 1){
            Assert.fail("AT kill Switch is NOT working ");
        }
        
        //Enable AT again
        enableAT(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
        restartEMandWV(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    }

    public int checkNumberofStories(String agcHost, Timestamp start_time)
        throws ClassNotFoundException, SQLException {

        Connection c = null;
        Statement stmt = null;
        Class.forName("org.postgresql.Driver");
        c =
            DriverManager.getConnection("jdbc:postgresql://" + agcHost + ":5432/cemdb", "postgres",
                "Lister@123");
        String problemZoneStoryQuery =
            "select count(*) as Count from at_stories where statements LIKe '%ProblemZoneStatement%PipeOrganWebService_2.webservices.executor.pipeorgan.tools.wily.com|execute%' and start_time >"
                + "'" + common.timestamp2String(start_time) + "'";


        String dbtcStoryQuery =
            "select count(*) as Count from at_stories where statements LIKe '%Dbtc%ExecutorServlet_1|service%' and start_time >"
                + "'" + common.timestamp2String(start_time) + "'";

        log.info("Problem Query : " + problemZoneStoryQuery);
        log.info("Problem Query : " + dbtcStoryQuery);

        stmt = c.createStatement();
        ResultSet rsProblem = stmt.executeQuery(problemZoneStoryQuery);
        rsProblem.next();
        stmt = c.createStatement();
        ResultSet rsDbtc = stmt.executeQuery(dbtcStoryQuery);
        rsDbtc.next();

        return (rsDbtc.getInt("Count") + rsProblem.getInt("Count"));
    }

    private void disableAT(String roleId) {

        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig.put("introscope.triage.enabled", "false");
        Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
        data.put(
            envProperties.getRolePropertiesById(roleId).getProperty(
                DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
        runConfigureFlowByRoleId(roleId, data);

    }
    
    private void enableAT(String roleId) {

        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig.put("introscope.triage.enabled", "true");
        Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
        data.put(
            envProperties.getRolePropertiesById(roleId).getProperty(
                DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
        runConfigureFlowByRoleId(roleId, data);

    }

    private void restartEMandWV(String roleId) {
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner standaloneClwRunner = utilities.createClwUtils(roleId).getClwRunner();
        standaloneClwRunner.runClw("shutdown");
        try {
            emUtils.stopLocalEm(standaloneClwRunner, roleId);
        } catch (Exception e) {
            log.warn("EM was not stopped properly!");
        }
        startEmAndWebview(roleId);
        log.info(roleId + " restarted.");
    }

    private void startEmAndWebview(String roleId) {
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        checkWebview(roleId);
    }

    public void killWebview(String roleId) {
        try {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("taskkill").args(
                    Arrays.asList("/F", "/T", "/IM", EmRole.Builder.WEBVIEW_EXECUTABLE)).build();
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } catch (Exception e) {
            // swallow all
        }
    }

    private void checkWebview(String roleId) {
        String wvHost = envProperties.getMachineHostnameByRoleId(roleId);
        String wvPort = envProperties.getRolePropertyById(roleId, "wvPort");
        while (!loadPage("http://" + wvHost + ":" + wvPort)) {
            killWebview(roleId);
            runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        }
    }

    private boolean loadPage(String pageUrl) {
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int responseCode = con.getResponseCode();
            con.disconnect();
            log.info("Response code from Webview: " + Integer.toString(responseCode));
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                return true;
            }
        } catch (Exception e) {
            // swallow all
        }
        return false;
    }

}
