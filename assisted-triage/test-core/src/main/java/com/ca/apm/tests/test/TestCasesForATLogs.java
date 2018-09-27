package com.ca.apm.tests.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class TestCasesForATLogs extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private String standaloneHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.SA_PROVIDER_EM_ROLE);
    private String momHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.MOM_PROVIDER_EM_ROLE);
    private String collectorHost = envProperties
        .getMachineHostnameByRoleId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER_EM_ROLE);

    private String agcRole = envProperties
        .getMachineIdByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
    private String[] logFiles = {"IntroscopeEnterpriseManager.log",
            "IntroscopeEnterpriseManagerSupport.log", "TeamCenterRegistration.log",
            "IntroscopeWebView.log"};
    private LinkedHashMap<String, String> emRoleLogsMap = new LinkedHashMap<String, String>();



    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        @SuppressWarnings("unused")
        LocalStorage localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "patpr15")
    @Test(groups = {"At_logs"})
    private void ATLogs_TestCase_ErrorsAndExcpetionsCheck() throws Exception {


        emRoleLogsMap.put("AGC", AssistedTriageTestbed.DEPLOY_DIR + "\\em\\logs\\");
        emRoleLogsMap.put("Collector", AssistedTriageTestbed.DEPLOY_DIR + "\\CollectorLogs\\");
        emRoleLogsMap.put("Standalone Provider", AssistedTriageTestbed.DEPLOY_DIR
            + "\\Provider1Logs\\");
        emRoleLogsMap.put("MOM Provider", AssistedTriageTestbed.DEPLOY_DIR + "\\Provider2Logs\\");

        for (Map.Entry<String, String> entry : emRoleLogsMap.entrySet()) {

            switch (entry.getKey()) {

                case "AGC":
                    changeLogLevel(AssistedTriageTestbed.SA_MASTER_EM_ROLE);
                    Thread.sleep(30000);
                    if (checkForErrorsAndExcpetions(entry.getKey(), entry.getValue()))
                        Assert.fail(" Errors or Exception Exist in AGC Logs");
                    else if (!checkLogsforMessages(entry.getKey(), entry.getValue()))
                        Assert.fail("Log Level changes are not working properly on AGC");
                    break;

                case "Standalone Provider":
                    changeLogLevel(AssistedTriageTestbed.SA_PROVIDER_EM_ROLE);
                    Thread.sleep(30000);
                    runCommandFlowByMachineId(agcRole,
                        copyLogFilesToAGCMachine(standaloneHost, entry.getValue()));
                    if (checkForErrorsAndExcpetions(entry.getKey(), entry.getValue()))
                        Assert.fail(" Errors or Exception Exist in Standalone EM Provider Logs");
                    else if (!checkLogsforMessages(entry.getKey(), entry.getValue()))
                        Assert
                            .fail("Log Level changes are not working properly on Standalone EM Provider");
                    break;

                case "MOM Provider":
                    changeLogLevel(AssistedTriageTestbed.MOM_PROVIDER_EM_ROLE);
                    Thread.sleep(30000);
                    runCommandFlowByMachineId(agcRole,
                        copyLogFilesToAGCMachine(momHost, entry.getValue()));

                    if (checkForErrorsAndExcpetions(entry.getKey(), entry.getValue()))
                        Assert.fail(" Errors or Exception Exist in MoM EM Provider Logs");
                    else if (!checkLogsforMessages(entry.getKey(), entry.getValue()))
                        Assert.fail("Log Level changes are not working properly on MOM Provider");
                    break;

                case "Collector":
                    changeLogLevel(AssistedTriageTestbed.COL_TO_MOM_PROVIDER_EM_ROLE);
                    Thread.sleep(30000);

                    runCommandFlowByMachineId(agcRole,
                        copyLogFilesToAGCMachine(collectorHost, entry.getValue()));

                    if (checkForErrorsAndExcpetions(entry.getKey(), entry.getValue()))
                        Assert.fail(" Errors or Exception Exist in Collector Logs");

                    else if (!checkLogsforMessages(entry.getKey(), entry.getValue()))
                        Assert.fail("Log Level changes are not working properly on Collector");
                    break;

            }
        }
    }

    private RunCommandFlowContext copyLogFilesToAGCMachine(String Host, String copyLocation) {

        String command =
            "net use /persistent:no && net use x: \\\\" + Host + ".ca.com"
                + "\\C$ && xcopy x:\\SW\\em\\logs " + copyLocation + " && net use x: /delete";
        System.out.println("Command : " + command);
        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();
        return runCommandFlowContext;
    }

    private boolean checkAllLogFilesExist(String role, String location) {
        boolean check = true;
        File dir = new File(location);
        List<String> logFilesList = new ArrayList<String>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".log"))) {
                logFilesList.add(file.getName());
            }
        }
        for (String logFile : logFiles)

        {

            if (!logFilesList.contains(logFile) && !role.equalsIgnoreCase("AGC")
                && !role.equals("Collector")) {
                Assert.fail("All " + role + " Log files didn't get copy to AGC Machine "
                    + "(Either " + logFile + "doesn't exist or didn't get copy)");
                check = false;
            }

            else if (role.equalsIgnoreCase("AGC")) {
                if (!logFilesList.contains(logFile)) {
                    Assert.fail("All " + role + " Log files don't exist on AGC Machine " + "("
                        + logFile + "doesn't exist)");
                    check = false;
                }

                else if (role.equals("Collector")) {
                    if (!logFilesList.contains(logFile)
                        && !logFile.equalsIgnoreCase("IntroscopeWebView.log"))
                        Assert.fail("All " + role + " Log files don't exist on Collector Machine "
                            + "(" + logFile + "doesn't exist)");
                    check = false;

                }


            }
        }
        return check;
    }


    private void changeLogLevel(String roleId) throws InterruptedException {

        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig
            .put("log4j.logger.Manager.AT=INFO,console,logfile",
                "log4j.logger.Manager.AT=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel,console,logfile");
        Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
        data.put(
            envProperties.getRolePropertiesById(roleId).getProperty(
                DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
        runConfigureFlowByRoleId(roleId, data);

        Thread.sleep(40000);
        replacePairsConfig.clear();
        data.clear();
        replacePairsConfig
            .put(
                "log4j.logger.Manager.AT=VERBOSE#com.wily.util.feedback.Log4JSeverityLevel,console,logfile",
                "log4j.logger.Manager.AT=TRACE#com.wily.util.feedback.Log4JSeverityLevel,console,logfile");

        data = new HashMap<String, Map<String, String>>();
        data.put(
            envProperties.getRolePropertiesById(roleId).getProperty(
                DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
        runConfigureFlowByRoleId(roleId, data);

        Thread.sleep(40000);

        replacePairsConfig.clear();
        data.clear();
        replacePairsConfig
            .put(
                "log4j.logger.Manager.AT=TRACE#com.wily.util.feedback.Log4JSeverityLevel,console,logfile",
                "log4j.logger.Manager.AT=INFO,console,logfile");

        data = new HashMap<String, Map<String, String>>();
        data.put(
            envProperties.getRolePropertiesById(roleId).getProperty(
                DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
        runConfigureFlowByRoleId(roleId, data);

        Thread.sleep(40000);
    }

    private boolean checkForErrorsAndExcpetions(String role, String location)
        throws FileNotFoundException {
        File dir = new File(location);
        boolean checkErrorException = false;
        for (File file : dir.listFiles())

        {
            if (file.getName().endsWith(".log") && checkAllLogFilesExist(role, location)) {
                @SuppressWarnings("resource")
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    // remove "Separate JVM for defect aggregation failed to start" message check
                    // after dev merge.
                    if ((line.contains("[Error") && !(line
                        .contains("Separate JVM for defect aggregation failed to start") || line
                        .contains("Unreachable service on follower")))
                        || (line.contains("[Manager.AT") && !line.contains("[INFO") && line.matches(".*\\bException\\b.*"))) {
                        {
                            log.info("Error or Exception Line : " + line);
                            checkErrorException = true;

                            break;
                        }
                    }
                }
            }
        }
        return checkErrorException;
    }

    private boolean checkLogsforMessages(String role, String location) {
        String text = "";
        try {
            text =
                FileUtils.readFileToString(new File(location + "IntroscopeEnterpriseManager.log"));
        } catch (IOException e) {
            Assert.fail("Introscope log file not found on " + role);
            e.printStackTrace();
        }

        /*
         * should get these patterns in AGC logs
         */
        Pattern patternTrace = Pattern.compile(".*[TRACE][Manager.AT].*");
        Pattern patternVerbose = Pattern.compile(".*[VERBOSE][Manager.AT].*");
        Pattern patternDebug = Pattern.compile(".*[DEBUG][Manager.AT].*");
        Pattern patternInfo = Pattern.compile(".*[INFO].*");

        Pattern logChange =
            Pattern
                .compile(".*Detected hot config change.*IntroscopeEnterpriseManager.properties.*");



        if (role.equalsIgnoreCase("AGC"))
            return patternTrace.matcher(text).find() && patternVerbose.matcher(text).find()
                && patternDebug.matcher(text).find() && patternInfo.matcher(text).find()
                && logChange.matcher(text).find();
        else
            return logChange.matcher(text).find();


    }

}
