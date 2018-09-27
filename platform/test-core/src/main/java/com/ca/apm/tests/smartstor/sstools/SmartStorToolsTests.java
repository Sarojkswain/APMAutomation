package com.ca.apm.tests.smartstor.sstools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.testbed.StandaloneEM1TomcatAgent1JBossAgentLinuxTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class SmartStorToolsTests extends SSToolsBase {

    TestUtils utility = new TestUtils();
    CLWCommons clwCommon = new CLWCommons();

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartStorToolsTests.class);
    List<String> dirCommand = new ArrayList<String>();

    public SmartStorToolsTests() {}

    @BeforeTest(alwaysRun = true)
    public void initialize() {
         startEM();
         startAgents();
         waitForAgentNodes(".*Tomcat.*", emHost, Integer.parseInt(emPort), emLibDir);
         stopEM();
         startEM();
         utility.connectToURL("http://" + tomcatHost + ":9091", 50);
         utility.connectToURL("http://" + tomcatHost + ":8080", 50);
         waitForAgentNodes(".*Tomcat.*", emHost, Integer.parseInt(emPort), emLibDir);
         stopEM();
    }


    @Test(groups = {"SmartStorTools", "EM", "SMOKE"}, enabled = true)
    public void verify_ALM_205049_SmartStorToolHelpOption_KeepAgents() {
        testCaseStart("verify_ALM_205049_SmartStorToolHelpOption_KeepAgents");
        execSSTCmdHelpKeepAgents("1");
    }

    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205058_SmartStorToolHelpOption_Merge() {
        testCaseStart("verify_ALM_205058_SmartStorToolHelpOption_Merge");
        execSSTCmdHelpMerge("1");
    }

    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205054_SmartStorToolHelpOption_Prune() {
        testCaseStart("verify_ALM_205054_SmartStorToolHelpOption_Prune");
        execSSTCmdHelpPrune("1");
    }

    @Test(groups = {"SmartStorTools", "EM", "SMOKE"}, enabled = true)
    public void verify_ALM_205043_SmartStorToolHelpOption_Remove_Metrics() {
        testCaseStart("verify_ALM_205043_SmartStorToolHelpOption_Remove_Metrics");
        execSSTCmdHelpRemoveMetrics("1");
    }

    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205044_SmartStorToolHelpOption_Remove_Agents() {
        testCaseStart("verify_ALM_205044_SmartStorToolHelpOption_Remove_Agents");
        execSSTCmdHelpRemoveAgents("1");
    }

    @Test(groups = {"SmartStorTools", "EM", "BAT"})
    public void verify_ALM_205055_SmartStorToolsPruneFunctionalityWithAllValidParameters() {
        testCaseStart("verify_ALM_205055_SmartStorToolsPruneFunctionalityWithAllValidParameters");
        checkDotDataFile(emHost, emPort, "Admin", "");
        pruneFun_validParams("1", "/data_backup", "metadata");
    }

    @Test(groups = {"SmartStorTools", "EM", "BAT"})
    public void verify_ALM_205031_SmartStorToolShouldBePresentInEMHomeTtoolsDirectory() {
        testCaseStart("verify_ALM_205031_SmartStorToolShouldBePresentInEMHomeTtoolsDirectory");
        checkSSToolsExistance();
    }

    @Test(groups = {"SmartStorTools", "EM", "SMOKE"})
    public void verify_ALM_205062_SmartStorToolWithEMNotRunning() {
        testCaseStart("verify_ALM_205062_SmartStorToolWithEMNotRunning");
        checkMetricsUsingTestRegex("Socket");
    }

    @Test(groups = {"SmartStorTools", "EM", "SMOKE"})
    public void verify_ALM_205036_SmartStorToolTestRegexAgentAndMetric() {
        testCaseStart("verify_ALM_205036_SmartStorToolTestRegexAgentAndMetric");
        checkMetricsUsingTestRegex("Socket");
        checkAgentsUsingTestRegex("");
    }

    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205037_SmartStorToolHelpOption_test_regex() {
        testCaseStart("verify_ALM_205037_SmartStorToolHelpOption_test_regex");
        execSSTCmdHelpTestRegex("1");
    }

    @Test(groups = {"SmartStorTools", "EM", "BAT"}, enabled = true)
    public void verify_ALM_205045_SmartStorToolRemoveAgents_withValid_Parameters(){
        testCaseStart("verify_ALM_205045_SmartStorToolRemoveAgents_withValid_Parameters");
        removeAgent_validParams("Tomcat", "/destDir");
    }

    @Test(groups = {"SmartStorTools", "EM", "BAT"}, enabled = true)
    public void verify_ALM_205046_SmartStorToolRemoveAgents_invalidValues(){
        testCaseStart("verify_ALM_205046_SmartStorToolRemoveAgents_invalidValues");
        removeAgent_inValidParams("ABCD", "/destDir");
    }

    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205047_SmartStorToolRemoveAgentsSrcDestNotPresent(){
        testCaseStart("verify_ALM_205047_SmartStorToolRemoveAgentsSrcDestNotPresent");
        removeAgentsWrongSrcAndWrongDest("Tomcat");
    }

    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205063_SmartStorToolRemoveCustomMetricsVerify(){
        testCaseStart("verify_ALM_205063_SmartStorToolRemoveCustomMetricsVerify");
        removemetricsVerify("Enterprise");
    }
    
    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205064_SmartStorToolRemoveMetricsVerify(){
        testCaseStart("verify_ALM_205064_SmartStorToolRemoveMetricsVerify");
        removemetricsVerify("Enterprise");
    }
    
    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205056_SmartStorToolPruneInvalidSrcAndDest(){
        testCaseStart("verify_ALM_205056_SmartStorToolPruneInvalidSrcAndDest");
        execInvalidPrune("/wrongDir1","/wrongDir2","Invalid SmartStor directory (-src)","Invalid backup directory (-backup):");
    }
    
    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205050_SmartStorToolKeepAgentsValidParams(){
        testCaseStart("verify_ALM_205050_SmartStorToolKeepAgentsValidParams");
        keepAgentsVerify("Tomcat");
    }
    
    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205052_SmartStorToolKeepAgentsInvalidSrcAndDest(){
        testCaseStart("verify_ALM_205052_SmartStorToolKeepAgentsInvalidSrcAndDest");
        keepAgentsWrongSrcAndWrongDest("Tomcat");
    }
    
    @Test(groups = {"SmartStorTools", "EM", "DEEP"}, enabled = true)
    public void verify_ALM_205051_SmartStorToolKeepAgentsInvalidAgents(){
        testCaseStart("verify_ALM_205051_SmartStorToolKeepAgentsInvalidAgents");
        keepAgents_inValidParams("ABCD", "/destDir");
    }
    
    
}
