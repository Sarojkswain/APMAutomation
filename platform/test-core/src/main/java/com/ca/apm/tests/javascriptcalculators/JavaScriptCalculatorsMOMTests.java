/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * 
 * Author : KETSW01
 */
package com.ca.apm.tests.javascriptcalculators;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.utils.file.TasFileNameFilter;
import com.ca.apm.automation.utils.file.TasFileNameFilter.FilterMatchType;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.base.OneCollectorOneTomcatTestsBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.role.EmRole;

public class JavaScriptCalculatorsMOMTests extends OneCollectorOneTomcatTestsBase {

    private CLWCommons clw = new CLWCommons();
    private TestUtils utility = new TestUtils();
    private TasFileNameFilter deleteJSfilter;
    private static final Logger LOGGER = LoggerFactory
        .getLogger(JavaScriptCalculatorsMOMTests.class);

    private final String momHost;
    private final String collector1Host;
    private final String momRoleId;
    private final String collector1RoleId;
    private final String tomcatRoleId;

    private final String momMachineId;
    private final String collector1MachineId;

    private final String momInstallDir;
    private final String momLibDir;
    private final String momScriptsDir;
    private final String collector1InstallDir;
    private final String collector1ScriptsDir;
    private final String collector1InternalScriptsLogFile;
    private final String momExamplesScriptsDir;
    private final String heapUsedPercentageJSFile;
    private final String cpuAverageJSFile;
    private final String momLogFile;
    private final String collector1LogFile;
    private final String momConfigFile;
    private final String collector1ConfigFile;

    private String delimiter;
    private String testCaseId;
    private String testCaseName;
    private List<String> rolesInvolved = new ArrayList<String>();
    private List<String> appendConfigFileProp = new ArrayList<String>();
    private List<String> defaultMOMJavaScriptLogMessages = new ArrayList<String>();
    private List<String> defaultCollectorJavaScriptLogMessages = new ArrayList<String>();
    private List<String> editJavaScriptLogMessagesInCollector = new ArrayList<String>();
    private List<String> runOnMOMTrueWithMetricReportingToCollectorLogMessage =
        new ArrayList<String>();
    private List<String> deleteJavaScriptFromCollectorLogMessages = new ArrayList<String>();
    private List<String> duplicateMetricLogMessages = new ArrayList<String>();
    private String runOnMOMFalseLogMessage;
    private String errorLoadingJavaScriptLogMessage;

    private String jsFileExtension = ".js";
    private String javaScriptFileName;
    private String javaScriptFilesLoc;
    private String tempResult1, tempResult2, tempResult3;
    private String tomcatAgentExpression;
    private String heapUsedPercentageMetricExpression;

    public JavaScriptCalculatorsMOMTests() {
        momRoleId = AgentControllabilityConstants.MOM_ROLE_ID;
        collector1RoleId = AgentControllabilityConstants.COLLECTOR1_ROLE_ID;
        tomcatRoleId = AgentControllabilityConstants.TOMCAT_ROLE_ID;

        momMachineId = AgentControllabilityConstants.MOM_MACHINE_ID;
        collector1MachineId = AgentControllabilityConstants.COLLECTOR1_MACHINE_ID;

        momHost = envProperties.getMachineHostnameByRoleId(momRoleId);
        collector1Host = envProperties.getMachineHostnameByRoleId(collector1RoleId);
        momInstallDir =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        collector1InstallDir =
            envProperties.getRolePropertyById(collector1RoleId,
                DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        momLibDir =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        momScriptsDir =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/scripts/";
        momExamplesScriptsDir =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/examples/scripts/";
        collector1ScriptsDir =
            envProperties.getRolePropertyById(collector1RoleId,
                DeployEMFlowContext.ENV_EM_INSTALL_DIR) + "/scripts/";
        momConfigFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "IntroscopeEnterpriseManager.properties";
        collector1ConfigFile =
            envProperties.getRolePropertyById(collector1RoleId,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "IntroscopeEnterpriseManager.properties";
        collector1InternalScriptsLogFile =
            envProperties.getRolePropertyById(collector1RoleId,
                DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/config/internal/server/scripts/JavaScriptCalculatorsMOM.properties";
        momLogFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        collector1LogFile =
            envProperties
                .getRolePropertyById(collector1RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);

        heapUsedPercentageJSFile =
            momExamplesScriptsDir + JavaScriptCalculatorsConstants.heapUsedPercentageJSFile;
        cpuAverageJSFile = momExamplesScriptsDir + JavaScriptCalculatorsConstants.cpuAverageJSFile;
        deleteJSfilter = new TasFileNameFilter("JS_", FilterMatchType.START_WITH);
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        heapUsedPercentageMetricExpression = "GC Heap:Heap Used \\(%\\)";
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            delimiter = TasBuilder.WIN_SEPARATOR;
            javaScriptFilesLoc = JavaScriptCalculatorsConstants.JAVASCRIPTFILES_LOC_WIN;
        } else {
            delimiter = TasBuilder.LINUX_SEPARATOR;
            javaScriptFilesLoc = JavaScriptCalculatorsConstants.JAVASCRIPTFILES_LOC_LINUX;
        }
    }

    @BeforeClass(alwaysRun = true)
    public void JavaScriptCalculatorsInitialize() {

        // set log mode of collector EM to DEBUG
        replaceProp("log4j.logger.Manager=INFO, console, logfile",
            "log4j.logger.Manager=DEBUG, console, logfile", collector1MachineId,
            collector1ConfigFile);
        backupFile(momConfigFile, momConfigFile + "_backup", momMachineId);
        List<String> machines = new ArrayList<String>();
        machines.add(momMachineId);
        machines.add(collector1MachineId);
        machines.add(AgentControllabilityConstants.TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
    }

    private String setJavaScriptFileName(String testCaseId) {
        this.javaScriptFileName = "JS_" + testCaseId + jsFileExtension;
        return javaScriptFileName;
    }

    public void setLogMessages(String javaScriptFile) {
        defaultMOMJavaScriptLogMessages.clear();
        defaultMOMJavaScriptLogMessages.add("Deploying JavaScript calculator " + momInstallDir
            + delimiter + "." + delimiter + "scripts" + delimiter + javaScriptFile);
        defaultMOMJavaScriptLogMessages.add("Initializing script from " + momInstallDir + delimiter
            + "." + delimiter + "scripts" + delimiter + javaScriptFile);
        defaultMOMJavaScriptLogMessages.add("Successfully added script " + momInstallDir
            + delimiter + "." + delimiter + "scripts" + delimiter + javaScriptFile);

        defaultCollectorJavaScriptLogMessages.clear();
        defaultCollectorJavaScriptLogMessages.add("Deploying JavaScript calculator "
            + collector1InstallDir + delimiter + "." + delimiter + "scripts" + delimiter
            + javaScriptFile);
        defaultCollectorJavaScriptLogMessages.add("Initializing script from "
            + collector1InstallDir + delimiter + "." + delimiter + "scripts" + delimiter
            + javaScriptFile);
        defaultCollectorJavaScriptLogMessages.add("Successfully added script "
            + collector1InstallDir + delimiter + "." + delimiter + "scripts" + delimiter
            + javaScriptFile);

        runOnMOMFalseLogMessage =
            "[INFO] [TimerBean] [Manager.JavaScriptCalculator] Not running " + javaScriptFile
                + " script on MOM because script method runOnMOM returned false";

        runOnMOMTrueWithMetricReportingToCollectorLogMessage.clear();
        runOnMOMTrueWithMetricReportingToCollectorLogMessage
            .add("JavaScript calculator "
                + momInstallDir
                + delimiter
                + "."
                + delimiter
                + "scripts"
                + delimiter
                + javaScriptFile
                + ".  A JavaScript calculator in the MOM cannot output metric data to an agent that exists in a Collector");
        runOnMOMTrueWithMetricReportingToCollectorLogMessage
            .add("[WARN] [Harvest Engine Pooled Worker] [Manager.MetricCalculatorBean] To prevent a JavaScript calculator from running in the MOM, the JavaScript should implement a runOnMOM function that returns false");

        deleteJavaScriptFromCollectorLogMessages.clear();
        deleteJavaScriptFromCollectorLogMessages.add("Received request to delete JavaScript file: "
            + javaScriptFileName);
        deleteJavaScriptFromCollectorLogMessages.add("Removed JavaScript file: "
            + collector1InstallDir + delimiter + "." + delimiter + "scripts" + delimiter
            + javaScriptFileName);
        deleteJavaScriptFromCollectorLogMessages.add("Undeploying JavaScript calculator "
            + collector1InstallDir + delimiter + "." + delimiter + "scripts" + delimiter
            + javaScriptFileName);

        errorLoadingJavaScriptLogMessage =
            "[ERROR] [TimerBean] [Manager.JavaScriptCalculator] Error loading script "
                + momInstallDir + delimiter + "." + delimiter + "scripts" + delimiter
                + javaScriptFile
                + ": org.mozilla.javascript.EvaluatorException: missing } after function body";

        duplicateMetricLogMessages.clear();
        duplicateMetricLogMessages
            .add("[ERROR] [Harvest Engine Pooled Worker] [Manager.JavascriptEngine] JavaScript Calculator "
                + collector1InstallDir
                + delimiter
                + "."
                + delimiter
                + "scripts"
                + delimiter
                + javaScriptFileName
                + " is generating the following duplicate metric with the metric url");
        duplicateMetricLogMessages
            .add("The duplicate metric generated will be dropped. Please rectify the script to prevent generating duplicate metrics.");

        editJavaScriptLogMessagesInCollector.clear();
        editJavaScriptLogMessagesInCollector.add("Undeploying JavaScript calculator "
            + collector1InstallDir + delimiter + "." + delimiter + "scripts" + delimiter
            + javaScriptFile);
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_280511_JS_calculator_is_added_in_MOM() throws Exception {

        testCaseId = "280511";
        testCaseName = "verify_ALM_" + testCaseId + "_JS_calculator_is_added_in_MOM";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(heapUsedPercentageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, defaultCollectorJavaScriptLogMessages));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_298245_JS_calculator_with_runOnMOM_function_that_returns_false()
        throws Exception {
        testCaseId = "298245";
        testCaseName =
            "verify_ALM_" + testCaseId + "_JS_calculator_with_runOnMOM_function_that_returns_false";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);
        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.sample1AddMetric,
                momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(checkForKeyword(envProperties, momMachineId, momLogFile,
                runOnMOMFalseLogMessage));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_280512_JS_calculator_with_runOnMOM_function_that_returns_true()
        throws Exception {
        testCaseId = "280512";
        testCaseName =
            "verify_ALM_" + testCaseId + "_JS_calculator_with_runOnMOM_function_that_returns_true";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);
        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_280512, momScriptsDir
                + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertFalse(checkForKeyword(envProperties, momMachineId, momLogFile,
                runOnMOMFalseLogMessage, false));
            tempResult1 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, momHost, Integer.parseInt(momPort),
                    momLibDir);
            Assert.assertTrue("Metrics are not getting reported for SampleMetricJS added",
                !tempResult1.equals("-1"));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298247_JS_calculator_with_no_runOnMOM_function() throws Exception {
        testCaseId = "298247";
        testCaseName = "verify_ALM_" + testCaseId + "_JS_calculator_with_no_runOnMOM_function";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);
        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_298247, momScriptsDir
                + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertFalse(checkForKeyword(envProperties, momMachineId, momLogFile,
                runOnMOMFalseLogMessage, false));
            tempResult1 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, momHost, Integer.parseInt(momPort),
                    momLibDir);
            Assert.assertTrue("Metrics are not getting reported for SampleMetricJS added",
                !tempResult1.equals("-1"));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_298248_Test_Warning_Message() throws Exception {
        testCaseId = "298248";
        testCaseName = "verify_ALM_" + testCaseId + "_Test_Warning_Message";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);
        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_298248, momScriptsDir
                + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                runOnMOMTrueWithMetricReportingToCollectorLogMessage));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298249_Messages_written_to_log_files_in_collector_EM() throws Exception {
        long currentTimestamp;
        String res = "//end of file tag";
        testCaseId = "298249";
        testCaseName =
            "verify_ALM_" + testCaseId + "_Messages_written_to_log_files_in_collector_EM";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);
        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(heapUsedPercentageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, defaultCollectorJavaScriptLogMessages));
            currentTimestamp = Calendar.getInstance().getTimeInMillis();
            Assert.assertTrue("Exception in  edit method of JavaScript",
                utility.convertStringToFile(res, momScriptsDir + javaScriptFileName, "true"));
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, editJavaScriptLogMessagesInCollector));
            LOGGER.info("Searching for this Javascript file entry in collector logs: "
                + javaScriptFileName);
            checkTimeStampValueOfKeyword(envProperties, collector1MachineId,
                collector1InternalScriptsLogFile, javaScriptFileName,
                String.valueOf(currentTimestamp));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_298250_Calculated_metric_shutOff_on_MOM_get_reflected_to_the_collectors()
        throws Exception {
        String heapUsedPercentageMetricExpressionShutOff = "";
        testCaseId = "298250";
        testCaseName =
            "verify_ALM_" + testCaseId
                + "_Calculated_metric_shutOff_on_MOM_get_reflected_to_the_collectors";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(heapUsedPercentageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, defaultCollectorJavaScriptLogMessages));
            waitForAgentNodes(tomcatAgentExpression, momHost, Integer.parseInt(momPort), emLibDir);
            tempResult1 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, momHost, Integer.parseInt(momPort),
                    momLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for heapUsedPercentageJSFile in MOM",
                !tempResult1.equals("-1"));
            // turn off metrics
            tempResult1 =
                clw.getNodeList(user, password, ".*Tomcat Agent", momHost,
                    Integer.parseInt(momPort), momLibDir).toString();
            tempResult1 =
                tempResult1.substring(tempResult1.indexOf("[") + 1, tempResult1.lastIndexOf("]"));
            heapUsedPercentageMetricExpressionShutOff =
                "\"SuperDomain|" + tempResult1 + "|" + "GC Heap:Heap Used (%)\"";
            clw.turnOffMetrics(user, password, heapUsedPercentageMetricExpressionShutOff, momHost,
                Integer.parseInt(momPort), momLibDir);
            harvestWait(60);
            tempResult2 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, momHost, Integer.parseInt(momPort),
                    momLibDir);
            Assert.assertTrue(
                "ShutOff Metrics are getting reported for heapUsedPercentageJSFile in MOM",
                tempResult2.equals("-1"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, collector1Host,
                    Integer.parseInt(collector1Port), momLibDir);
            Assert.assertTrue(
                "ShutOff Metrics are getting reported for heapUsedPercentageJSFile in Collector",
                tempResult3.equals("-1"));
        } finally {
            // turn on metrics
            clw.turnOnMetrics(user, password, heapUsedPercentageMetricExpressionShutOff, momHost,
                Integer.parseInt(momPort), momLibDir);
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_298253_JS_calculators_containing_syntax_errors() throws Exception {
        testCaseId = "298253";
        testCaseName = "verify_ALM_" + testCaseId + "_JS_calculators_containing_syntax_errors";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_298253, momScriptsDir
                + javaScriptFileName, momMachineId);

            Assert.assertTrue(checkForKeyword(envProperties, momMachineId, momLogFile,
                errorLoadingJavaScriptLogMessage));
            Assert.assertFalse(checkForKeyword(envProperties, collector1MachineId,
                collector1InternalScriptsLogFile, javaScriptFileName, false));
            stopEM(momRoleId);
            runSerializedCommandFlowFromRole(momRoleId, EmRole.ENV_START_EM);
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_298254_JS_calculators_Default_behavior() throws Exception {
        testCaseId = "298254";
        testCaseName = "verify_ALM_" + testCaseId + "_JS_calculators_Default_behavior";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(cpuAverageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, defaultCollectorJavaScriptLogMessages));
            deleteFile(momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, deleteJavaScriptFromCollectorLogMessages));
            copyFile(cpuAverageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, defaultCollectorJavaScriptLogMessages));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298256_TT41360_Order_in_which_messages_get_written_to_collectorEM_logs_should_be_logical()
        throws Exception {
        String[] deleteMessageInSequence;
        testCaseId = "298256";
        testCaseName =
            "verify_ALM_"
                + testCaseId
                + "_TT41360_Order_in_which_messages_get_written_to_collectorEM_logs_should_be_logical";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(cpuAverageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            deleteFile(momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, deleteJavaScriptFromCollectorLogMessages));
            deleteMessageInSequence =
                deleteJavaScriptFromCollectorLogMessages.toArray(new String[0]);
            checkMessagesInSequence(envProperties, collector1MachineId, collector1LogFile,
                deleteMessageInSequence);
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298257_harvest_errors_after_upgrade() throws Exception {
        testCaseId = "298257";
        testCaseName = "verify_ALM_" + testCaseId + "_harvest_errors_after_upgrade";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_298257, momScriptsDir
                + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, collector1MachineId,
                collector1LogFile, duplicateMetricLogMessages));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_205266_JS_calculators_hidden_property_set_to_false() throws Exception {

        String collectorJSLogMessage;
        testCaseId = "205266";
        testCaseName = "verify_ALM_" + testCaseId + "_JS_calculators_hidden_property_set_to_false";

        rolesInvolved.clear();
        rolesInvolved.add(MOM_ROLE_ID);
        rolesInvolved.add(COLLECTOR1_ROLE_ID);
        rolesInvolved.add(TOMCAT_ROLE_ID);

        appendConfigFileProp.clear();
        appendConfigFileProp
            .add("introscope.enterprisemanager.javascript.hotdeploy.collectors.enable=false");

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            appendProp(appendConfigFileProp, momMachineId, momConfigFile);
            startTestBed();

            copyFile(cpuAverageJSFile, momScriptsDir + javaScriptFileName, momMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, momMachineId, momLogFile,
                defaultMOMJavaScriptLogMessages));
            collectorJSLogMessage =
                "Deploying JavaScript calculator " + collector1InstallDir + delimiter + "."
                    + delimiter + "scripts" + delimiter + javaScriptFileName;
            Assert.assertFalse(checkForKeyword(envProperties, collector1MachineId,
                collector1LogFile, collectorJSLogMessage, false));
        } finally {
            deleteFilteredFiles(momScriptsDir, deleteJSfilter, momMachineId);
            deleteFilteredFiles(collector1ScriptsDir, deleteJSfilter, collector1MachineId);
            stopTestBed();
            revertFile(momConfigFile, momConfigFile + "_backup", momMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    public void startTestBed() {
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        LOGGER.info("All the components of the Testbed are started");
    }

    public void stopTestBed() {
        stopEM(momRoleId);
        stopCollectorEM(momRoleId, collector1RoleId);
        stopTomcatAgent(tomcatRoleId);
        LOGGER.info("All the components of the Testbed are stopped");
    }

}
