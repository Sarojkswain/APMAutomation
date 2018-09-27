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
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.utils.file.TasFileNameFilter;
import com.ca.apm.automation.utils.file.TasFileNameFilter.FilterMatchType;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.tas.builder.TasBuilder;

public class JavaScriptCalculatorsStandaloneTests extends BaseAgentTest {

    private TestUtils utility = new TestUtils();
    private CLWCommons clw = new CLWCommons();
    private TasFileNameFilter deleteJSfilter;
    private static final Logger LOGGER = LoggerFactory
        .getLogger(JavaScriptCalculatorsStandaloneTests.class);

    private final String emHost;
    private final String emPort;
    private final String emRoleId;

    private final String tomcatRoleId;
    private final String tomcatAgentHost;
    private final String tomcatAgentPort;

    private final String emMachineId;

    private final String emInstallDir;
    private final String emLibDir;
    private final String emScriptsDir;
    private final String emExamplesScriptsDir;
    private final String heapUsedPercentageJSFile;
    private final String emLogFile;
    private final String user;
    private final String password;

    private String delimiter;
    private String testCaseId;
    private String testCaseName;
    private List<String> rolesInvolved = new ArrayList<String>();
    private List<String> defaultEMJavaScriptLogMessages = new ArrayList<String>();

    private String jsFileExtension = ".js";
    private String javaScriptFileName;
    private String javaScriptFilesLoc;
    private String tempResult1, tempResult2, tempResult3;
    private String customMetricAgentExpression;
    private String sampleJSMetricExpression1;
    private String sampleJSMetricExpression2;
    private String sampleJSMetricExpression3;
    private String testCountMetric_kIntegerPercentage;
    private String testCountMetric_kIntegerRate;
    private String testCountMetric_kIntegerRate2;
    private String testCountMetric_kIntegerRate3;
    private String testCountMetric_kLongConstant;
    private String testCountMetric_kLongFluctuatingCounter;
    private String testCountMetric_kLongIntervalCounter;
    private String testStringMetricExpression1;
    private String testStringMetricExpression2;
    private String testConstantMetric_kIntegerConstant;
    private String testConstantMetric_kIntegerConstant2;
    private String testConstantMetric_kIntegerConstant3;
    private String testConstantMetric_kLongConstant;
    private String testConstantMetric_kLongConstant2;
    private String testConstantMetric_kLongConstant3;
    private String testConstantMetric_kLongConstant4;
    private String testFluctuateCntr_kIntegerFluctuatingCounter;
    private String testFluctuateCntr_kIntegerFluctuatingCounter2;
    private String testFluctuateCntr_kIntegerFluctuatingCounter3;
    private String testFluctuateCntr_kLongFluctuatingCounter;
    private String testFluctuateCntr_kLongFluctuatingCounter2;
    private String testFluctuateCntr_kLongFluctuatingCounter3;
    private String testFluctuateCntr_kLongFluctuatingCounter4;
    private String timestampLaunchTimeMetric;
    private String timestampWallClockTimeMetric;
    private String jspAverageResponseTime;
    private String testIntervalCounterMetricExpression;
    private String testIntegerRateMetric_kIntegerRate;
    private String testIntegerRateMetric_kIntegerRate2;
    private String testIntegerRateMetric_kIntegerRate3;

    private String tomcatAgentExpression;
    private String heapUsedPercentageMetricExpression;
    private String fluctuateHeapUsedPercentageMetricExpression;

    public JavaScriptCalculatorsStandaloneTests() {
        emRoleId = AgentControllabilityConstants.EM_ROLE_ID;
        tomcatRoleId = AgentControllabilityConstants.TOMCAT_ROLE_ID;

        emMachineId = AgentControllabilityConstants.EM_MACHINE_ID;

        emHost = envProperties.getMachineHostnameByRoleId(emRoleId);
        emPort = envProperties.getRolePropertiesById(emRoleId).getProperty("emPort");

        emInstallDir =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);

        emLibDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        emScriptsDir =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/scripts/";
        emExamplesScriptsDir =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/examples/scripts/";
        emLogFile =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        tomcatAgentHost = envProperties.getMachineHostnameByRoleId(tomcatRoleId);
        tomcatAgentPort =
            envProperties
                .getRolePropertyById(tomcatRoleId, DeployTomcatFlowContext.ENV_TOMCAT_PORT);

        user = ApmbaseConstants.emUser;
        password = ApmbaseConstants.emPassw;
        heapUsedPercentageJSFile =
            emExamplesScriptsDir + JavaScriptCalculatorsConstants.heapUsedPercentageJSFile;
        deleteJSfilter = new TasFileNameFilter("JS_", FilterMatchType.START_WITH);
        /*
         * metric expressions
         */
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        customMetricAgentExpression =
            "(.*)Custom Metric Host \\(Virtual\\)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)";
        sampleJSMetricExpression1 = "Sample_1 Metrics:Constant";
        sampleJSMetricExpression2 = "Sample_1 Metrics:Constant2";
        sampleJSMetricExpression3 = "Sample_1 Metrics:Constant3";
        testCountMetric_kIntegerPercentage = "TestCount Metrics:kIntegerPercentage";
        testCountMetric_kIntegerRate = "TestCount Metrics:kIntegerRate";
        testCountMetric_kIntegerRate2 = "TestCount Metrics:kIntegerRate2";
        testCountMetric_kIntegerRate3 = "TestCount Metrics:kIntegerRate3";
        testCountMetric_kLongConstant = "TestCount Metrics:kLongConstant";
        testCountMetric_kLongFluctuatingCounter = "TestCount Metrics:kLongFluctuatingCounter";
        testCountMetric_kLongIntervalCounter = "TestCount Metrics:kLongIntervalCounter";
        testStringMetricExpression1 = "TestString Metrics:kStringIndividualEvents1";
        testStringMetricExpression2 = "TestString Metrics:kStringIndividualEvents2";
        testConstantMetric_kIntegerConstant = "TestConstant Metrics:kIntegerConstant";
        testConstantMetric_kIntegerConstant2 = "TestConstant Metrics:kIntegerConstant2";
        testConstantMetric_kIntegerConstant3 = "TestConstant Metrics:kIntegerConstant3";
        testConstantMetric_kLongConstant = "TestConstant Metrics:kLongConstant";
        testConstantMetric_kLongConstant2 = "TestConstant Metrics:kLongConstant2";
        testConstantMetric_kLongConstant3 = "TestConstant Metrics:kLongConstant3";
        testConstantMetric_kLongConstant4 = "TestConstant Metrics:kLongConstant4";
        testFluctuateCntr_kIntegerFluctuatingCounter =
            "TestFluctuateCntr Metrics:kIntegerFluctuatingCounter";
        testFluctuateCntr_kIntegerFluctuatingCounter2 =
            "TestFluctuateCntr Metrics:kIntegerFluctuatingCounter2";
        testFluctuateCntr_kIntegerFluctuatingCounter3 =
            "TestFluctuateCntr Metrics:kIntegerFluctuatingCounter3";
        testFluctuateCntr_kLongFluctuatingCounter =
            "TestFluctuateCntr Metrics:kLongFluctuatingCounter";
        testFluctuateCntr_kLongFluctuatingCounter2 =
            "TestFluctuateCntr Metrics:kLongFluctuatingCounter2";
        testFluctuateCntr_kLongFluctuatingCounter3 =
            "TestFluctuateCntr Metrics:kLongFluctuatingCounter3";
        testFluctuateCntr_kLongFluctuatingCounter4 =
            "TestFluctuateCntr Metrics:kLongFluctuatingCounter4";
        testIntervalCounterMetricExpression = "TestIntervalCounter Metrics:kLongIntervalCounter";
        testIntegerRateMetric_kIntegerRate = "TestIntegerRate Metrics:kIntegerRate";
        testIntegerRateMetric_kIntegerRate2 = "TestIntegerRate Metrics:kIntegerRate2";
        testIntegerRateMetric_kIntegerRate3 = "TestIntegerRate Metrics:kIntegerRate3";
        heapUsedPercentageMetricExpression = "GC Heap:Heap Used \\(%\\)";
        timestampLaunchTimeMetric = "Enterprise Manager:Launch Time";
        timestampWallClockTimeMetric = "Host:Wall Clock Time";
        fluctuateHeapUsedPercentageMetricExpression = "GC Heap:Fluctuate Heap Used \\(%\\)";
        jspAverageResponseTime = "JSP:Average Response Time \\(ms\\)";
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


    }


    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_280499_Add_a_New_JS_calculator() throws Exception {

        testCaseId = "280499";
        testCaseName = "verify_ALM_" + testCaseId + "_Add_a_New_JS_calculator";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(heapUsedPercentageJSFile, emScriptsDir + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for heapUsedPercentageJsFile added",
                !tempResult1.equals(-1));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_300030_Revised_Function_addMetric() throws Exception {

        testCaseId = "300030";
        testCaseName = "verify_ALM_" + testCaseId + "_Revised_Function_addMetric";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300030, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    sampleJSMetricExpression1, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for SampleMetricJS added",
                tempResult1.equals("Integer:::9"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    sampleJSMetricExpression2, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for SampleMetricJS added",
                tempResult2.equals("Integer:::9"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    sampleJSMetricExpression3, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for SampleMetricJS added",
                tempResult3.equals("Integer:::9"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_300031_New_Function_addMetric_for_JS_calcs() throws Exception {

        testCaseId = "300031";
        testCaseName = "verify_ALM_" + testCaseId + "_New_Function_addMetric_for_JS_calcs";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300031, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kIntegerPercentage, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult1.equals("Integer:::10"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kIntegerRate, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult2.equals("Integer:::0"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kIntegerRate2, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult3.equals("Integer:::2"));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kIntegerRate3, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult1.equals("Integer:::2"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kLongConstant, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult2.equals("Long:::10"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kLongFluctuatingCounter, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult3.equals("Long:::10"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kLongIntervalCounter, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult3.equals("Long:::30"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_300032_Function_getCustomMetricAgentMetric() throws Exception {

        testCaseId = "300032";
        testCaseName = "verify_ALM_" + testCaseId + "_Function_getCustomMetricAgentMetric";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300032, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testCountMetric_kIntegerPercentage, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult1.equals("Integer:::10"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_300033_String_Metric_Type() throws Exception {

        testCaseId = "300033";
        testCaseName = "verify_ALM_" + testCaseId + "_String_Metric_Type";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300033, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testStringMetricExpression1, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for StringMetricJS added",
                tempResult1.equals("String:::Hello World"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testStringMetricExpression2, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for StringMetricJS added",
                tempResult2.equals("String:::Testing for String Metric Type"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_300034_Constant_metric_type_helper_Integer_Long() throws Exception {

        testCaseId = "300034";
        testCaseName = "verify_ALM_" + testCaseId + "_Constant_metric_type_helper_Integer_Long";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300034, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kIntegerConstant, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult1.equals("Integer:::9"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kIntegerConstant2, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult2.equals("Integer:::9"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kIntegerConstant3, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult3.equals("Integer:::9"));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kLongConstant, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult1.equals("Long:::9"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kLongConstant2, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult2.equals("Long:::9"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kLongConstant3, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult3.equals("Long:::9"));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testConstantMetric_kLongConstant4, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestConstantMetricJS added",
                tempResult3.equals("Long:::9"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_300035_FluctuatingCounter_metric_type_helper_int_long() throws Exception {
        String javaScriptFileName1;
        List<String> result = new ArrayList<>();
        boolean flag = false;
        testCaseId = "300035";
        testCaseName =
            "verify_ALM_" + testCaseId + "_FluctuatingCounter_metric_type_helper_int_long";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc
                + JavaScriptCalculatorsConstants.JS_300035_TestFluctuatingCounter1, emScriptsDir
                + javaScriptFileName, emMachineId);

            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kIntegerFluctuatingCounter, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult1.equals("Integer:::9"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kIntegerFluctuatingCounter2, emHost,
                    Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult2.equals("Integer:::9"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kIntegerFluctuatingCounter3, emHost,
                    Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult3.equals("Integer:::9"));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kLongFluctuatingCounter, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult1.equals("Long:::9"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kLongFluctuatingCounter2, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult2.equals("Long:::9"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kLongFluctuatingCounter3, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult3.equals("Long:::9"));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testFluctuateCntr_kLongFluctuatingCounter4, emHost, Integer.parseInt(emPort),
                    emLibDir);
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS added",
                tempResult1.equals("Long:::9"));

            javaScriptFileName1 = "JS_" + testCaseId + "_TestFluctuatingCounter2" + jsFileExtension;
            setLogMessages(javaScriptFileName1);
            copyFile(javaScriptFilesLoc
                + JavaScriptCalculatorsConstants.JS_300035_TestFluctuateCounter2, emScriptsDir
                + javaScriptFileName1, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            result =
                clw.getHistoricMetricValuesForTimeInMinutes(user, password, tomcatAgentExpression,
                    fluctuateHeapUsedPercentageMetricExpression, emHost, Integer.parseInt(emPort),
                    emLibDir, 2);
            for (String value : result) {
                if (Integer.parseInt(value) > 0) {
                    flag = true;
                    break;
                }
            }
            Assert.assertTrue(
                "Metrics are not getting reported for TestFluctuatingCounterMetricJS2 added", flag);

        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_300036_TimeStamp_for_JS_calcs() throws Exception {

        List<String> result1 = new ArrayList<>();
        testCaseId = "300036";
        testCaseName = "verify_ALM_" + testCaseId + "_TimeStamp_for_JS_calcs";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            startTestBed();

            waitForAgentNodes(tomcatAgentExpression, emHost, Integer.parseInt(emPort), emLibDir);
            harvestWait(60);
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    timestampLaunchTimeMetric, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("TimeStamp Metrics are not getting reported",
                tempResult1.contains("Date:::"));
            result1 =
                clw.getHistoricMetricValuesForTimeInMinutes(user, password,
                    customMetricAgentExpression, timestampLaunchTimeMetric, emHost,
                    Integer.parseInt(emPort), emLibDir, 1);
            Assert.assertTrue("Launch time metric displayed changes with time",
                (result1.get(result1.size() - 1).equals(result1.get(result1.size() - 2))));

            tempResult2 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression,
                    timestampWallClockTimeMetric, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("TimeStamp Metrics are not getting reported",
                tempResult2.contains("Date:::"));
            result1.clear();
            result1 =
                clw.getHistoricMetricValuesForTimeInMinutes(user, password, tomcatAgentExpression,
                    timestampWallClockTimeMetric, emHost, Integer.parseInt(emPort), emLibDir, 1);
            Assert.assertTrue("Wall Clock time metric displayed remains constant",
                !(result1.get(result1.size() - 1).equals(result1.get(result1.size() - 2))));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_300037_Duration_for_JS_calcs() throws Exception {

        List<String> result = new ArrayList<>();
        boolean output = false;
        String appUrl = "http://" + tomcatAgentHost + ":" + tomcatAgentPort + "/index.jsp";
        appUrl = appUrl.toLowerCase().trim();
        testCaseId = "300037";
        testCaseName = "verify_ALM_" + testCaseId + "_Duration_for_JS_calcs";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            startTestBed();

            waitForAgentNodes(tomcatAgentExpression, emHost, Integer.parseInt(emPort), emLibDir);
            LOGGER.info("Invoking application url: " + appUrl);
            utility.connectToURL(appUrl, 10);
            harvestWait(30);
            result =
                clw.getHistoricMetricValuesForTimeInMinutes(user, password, tomcatAgentExpression,
                    jspAverageResponseTime, emHost, Integer.parseInt(emPort), emLibDir, 2);
            for (String stringValue : result) {
                if (Integer.parseInt(stringValue) > 0) {
                    output = true;
                    break;
                }
            }
            Assert.assertTrue("Duration metrics are not getting reported", output);
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_300038_IntervalCounter_for_JS_calcs() throws Exception {

        String editedJSFile;
        String res;
        testCaseId = "300038";
        testCaseName = "verify_ALM_" + testCaseId + "_IntervalCounter_for_JS_calcs";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300038, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntervalCounterMetricExpression, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for IntervalCounterJSFile added",
                tempResult1.equals("Long:::30"));
            res = utility.convertFileToString(emScriptsDir + javaScriptFileName);
            LOGGER.info("file data to string is  : " + res);
            res = res.replace("30", "20");
            LOGGER.info("file data to string is  : " + res);
            editedJSFile = "JS_" + testCaseId + "_edit" + jsFileExtension;
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            Assert.assertTrue("Exception in  edit method of JavaScript",
                utility.convertStringToFile(res, emScriptsDir + editedJSFile, "false"));
            setLogMessages(editedJSFile);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntervalCounterMetricExpression, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for IntervalCounterJSFile added",
                tempResult2.equals("Long:::20"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_300039_Percentage_for_JS_calcs() throws Exception {
        List<String> result = new ArrayList<>();
        boolean output = false;
        testCaseId = "300039";
        testCaseName = "verify_ALM_" + testCaseId + "_Percentage_for_JS_calcs";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(heapUsedPercentageJSFile, emScriptsDir + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            result =
                clw.getHistoricMetricValuesForTimeInMinutes(user, password, tomcatAgentExpression,
                    heapUsedPercentageMetricExpression, emHost, Integer.parseInt(emPort), emLibDir,
                    1);
            for (String stringValue : result) {
                if (Integer.parseInt(stringValue) > 0) {
                    output = true;
                    break;
                }
            }
            Assert.assertTrue("Percentage metrics are not getting reported", output);
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }


    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_300040_Integer_Rate_for_JS_calcs() throws Exception {

        String editedJSFile;
        String res;
        testCaseId = "300040";
        testCaseName = "verify_ALM_" + testCaseId + "_Integer_Rate_for_JS_calcs";

        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);
        rolesInvolved.add(AgentControllabilityConstants.TOMCAT_ROLE_ID);

        try {
            testCaseStart(testCaseName);
            setJavaScriptFileName(testCaseId);
            setLogMessages(javaScriptFileName);
            startTestBed();

            copyFile(javaScriptFilesLoc + JavaScriptCalculatorsConstants.JS_300040, emScriptsDir
                + javaScriptFileName, emMachineId);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult1 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntegerRateMetric_kIntegerRate, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult1.equals("Integer:::0"));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntegerRateMetric_kIntegerRate2, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult2.equals("Integer:::2"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntegerRateMetric_kIntegerRate3, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult3.equals("Integer:::2"));

            res = utility.convertFileToString(emScriptsDir + javaScriptFileName);
            LOGGER.info("file data to string is  : " + res);
            res = res.replace("30", "15");
            LOGGER.info("String data to File is  : " + res);
            editedJSFile = "JS_" + testCaseId + "_edit" + jsFileExtension;
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            Assert.assertTrue("Exception in  edit method of JavaScript",
                utility.convertStringToFile(res, emScriptsDir + editedJSFile, "false"));
            setLogMessages(editedJSFile);
            Assert.assertTrue(isKeywordInFile(envProperties, emMachineId, emLogFile,
                defaultEMJavaScriptLogMessages));
            tempResult2 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntegerRateMetric_kIntegerRate2, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult2.equals("Integer:::1"));
            tempResult3 =
                clw.getLatestMetricValue(user, password, customMetricAgentExpression,
                    testIntegerRateMetric_kIntegerRate3, emHost, Integer.parseInt(emPort), emLibDir);
            Assert.assertTrue("Metrics are not getting reported for TestCountMetricJS added",
                tempResult3.equals("Integer:::1"));
        } finally {
            stopTestBed();
            deleteFilteredFiles(emScriptsDir, deleteJSfilter, emMachineId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_300042_HotDeployDirectoryMissing() {

        testCaseId = "300042";
        testCaseName = "verify_ALM_" + testCaseId + "_HotDeploy_Directory_Missin";
        String emDeployDir = emInstallDir + "/deploy";
        String msg = "[INFO] [TimerBean] [Manager.MMHotDeployEntity] Cannot find directory " + emInstallDir + delimiter + "deploy - creating it now";
        
        rolesInvolved.clear();
        rolesInvolved.add(AgentControllabilityConstants.EM_ROLE_ID);

        try {
            testCaseStart(testCaseName);

            startEM(emRoleId);
            
            LOGGER.info("Deleting the EM Deploy directory");            
            deleteFile(emDeployDir, emMachineId);           
            
            //Wait to check if the directory gets created
        	harvestWait(180);
        	checkLogForMsg(envProperties, emMachineId, emLogFile, msg);
        	LOGGER.info("Verifying if the EM Deploy directory is created");    
        	checkFileExistence(envProperties, emMachineId, emDeployDir);

            Assert.assertTrue(true);
        }catch(Exception e){
        	LOGGER.error("Exception occurred during the test: ", e);
        	Assert.assertTrue("Execption either while delete or creation of deploy folder", false);
        }finally {
            stopEM(emRoleId);
            renameLogWithTestCaseId(rolesInvolved, testCaseId);
            testCaseEnd(testCaseName);
        }
    }
    
    private String setJavaScriptFileName(String testCaseId) {
        this.javaScriptFileName = "JS_" + testCaseId + jsFileExtension;
        return javaScriptFileName;
    }

    private void setLogMessages(String javaScriptFile) {
        defaultEMJavaScriptLogMessages.clear();
        defaultEMJavaScriptLogMessages.add("Deploying JavaScript calculator " + emInstallDir
            + delimiter + "." + delimiter + "scripts" + delimiter + javaScriptFile);
        defaultEMJavaScriptLogMessages.add("Initializing script from " + emInstallDir + delimiter
            + "." + delimiter + "scripts" + delimiter + javaScriptFile);
        defaultEMJavaScriptLogMessages.add("Successfully added script " + emInstallDir + delimiter
            + "." + delimiter + "scripts" + delimiter + javaScriptFile);
    }

    private void startTestBed() {
        startEM(emRoleId);
        startTomcatAgent(tomcatRoleId);
        LOGGER.info("All the components of the Testbed are started");
    }

    private void stopTestBed() {
        stopEM(emRoleId);
        stopTomcatAgent(tomcatRoleId);
        LOGGER.info("All the components of the Testbed are stopped");
    }

}
