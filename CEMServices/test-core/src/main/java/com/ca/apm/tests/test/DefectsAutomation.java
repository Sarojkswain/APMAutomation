package com.ca.apm.tests.test;

import static com.ca.apm.tests.cem.common.CEMConstants.EM_MACHINE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.EM_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_MACHINE_ID;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.cem.logiProcessor.LoginProcessorUtils;
import com.ca.apm.cem.logiProcessor.test.UserIdentification;
import com.ca.apm.commons.flow.RunCommandFlow;
import com.ca.apm.commons.flow.RunCommandFlowContext;
import com.ca.apm.tests.testbed.CEMChromeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;



public class DefectsAutomation extends JBaseTest {

    public String timeLineHeader = "";
    final long AggrtimeOut = 4000000;
    final long timeOut = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefectsAutomation.class);
    public WebDriver driver;
    protected BufferedWriter writer = null;
    String scriptName = "sh " + getEnvConstValue("ssh_scriptName") + " ";
    UserIdentification userIdentifier;
    protected final String EMlogFile = envProperties.getRolePropertyById(EM_ROLE_ID,
        DeployEMFlowContext.ENV_EM_LOG_FILE);;
    private LoginProcessorUtils logInPUtils;
    String testCaseName1 = "User_455276";
    String testCaseName2 = "UserGroup_455276";

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        System.out.println("**********Initializing in Defect Processing*******");
        try {
            super.initialize();
            LOGGER.info("Setup Monitor : " + setupMonitor);
            logInPUtils = new LoginProcessorUtils();
            setupMonitor.createMonitor(TIM_HOST_NAME, TIM_IP, TESS_HOST);
            setupMonitor.enableMonitor(TIM_HOST_NAME);
            logIn();

            System.out.println("Current URL@@@@@@" + super.driver.getCurrentUrl());

            setupAppData(testCaseName1 + "_BA", testCaseName1 + "_BS", "455276_BT.zip");
            setupAppData(testCaseName2 + "_BA", testCaseName2 + "_BS", "455276_BT.zip");
            setupMonitor.syncMonitors();

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        System.out
            .println("**********Successful Initializing in Defect Processing Completed*******");
    }

    @Tas(testBeds = @TestBed(name = CEMChromeTestbed.class, executeOn = EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_455275_User() {
        String testCaseName = "verify_ALM_455275_User";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        String parameterType = "Post";
        String parameterNameUID = "user";
        String user =
            "123456789101112131415161718192021222324252627282930123456789101112131415161718192021222324252627282930123456789101112131415161718192021222324252627282930123456789101112131415161718192021222324252627282930";
        try {
            addUserIdParamToApplication(testCaseName1 + "_BA", parameterType, parameterNameUID);
            setupMonitor.syncMonitors();

            String baseUrl = "http://swasa02-win01/try1/home/posttest.php";
            String url = "curl --data \"" + parameterNameUID + "=" + user + "\" " + baseUrl; // URL
            String[] commands = {url};
            generateTraffic(commands);

            String url1 = "curl --data \"" + parameterNameUID + "\\=" + user + "\" " + baseUrl; // URL
            String[] commands1 = {url};
            generateTraffic(commands1);
            List<String> keyWords = new ArrayList<String>();
            keyWords.add("No user found/created for (app:");
            keyWords
                .add("(login: '1234567891011121314151617181920212223242526272829301234567891011121314151617181920212223242526272829')");
            verifyIfAtleastOneKeywordIsInLog(EM_MACHINE_ID, EMlogFile, keyWords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
    }


    @Tas(testBeds = @TestBed(name = CEMChromeTestbed.class, executeOn = EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_455276_User() {
        String testCaseName = "verify_ALM_455276_User";
        setupTest(testCaseName);
        startTestCaseName(testCaseName);
        String parameterType = "Post";
        String parameterNameUID = "user";
        String user =
            "123456789101112131415161718192021222324252627282930123456789101112131415161718192021222324252627282930123456789101112131415161718192021222324252627282930123456789101112131415161718192021222324252627282930";
        try {
            addUserIdParamToApplication(testCaseName2 + "_BA", parameterType, parameterNameUID);
            setupMonitor.syncMonitors();

            String baseUrl = "http://swasa02-win01/try1/home/posttest.php";
            String url = "curl --data \"" + parameterNameUID + "=" + user + "\" " + baseUrl; // URL
            String[] commands = {url};
            generateTraffic(commands);

            String url1 = "curl --data \"" + parameterNameUID + "\\=" + user + "\" " + baseUrl; // URL
            String[] commands1 = {url};
            generateTraffic(commands1);

            List<String> keyWords = new ArrayList<String>();
            keyWords.add("No user found/created for (app:");
            keyWords
                .add("(login: '1234567891011121314151617181920212223242526272829301234567891011121314151617181920212223242526272829')");
            verifyIfAtleastOneKeywordIsInLog(EM_MACHINE_ID, EMlogFile, keyWords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        endTestCaseName(testCaseName);
        tearDownTest(testCaseName);
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

    public void generateTraffic(String[] commands) {
        for (int j = 0; j < 20; j++){
            try {
                utility.execUnixCmd(TIM_HOST_NAME, 22, "root", "Lister@123", commands);
                harvestWait(1);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            harvestWait(1);
        }
    }
}
