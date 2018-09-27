/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * Author : JAMSA07/ SANTOSH JAMMI
 * Author : TUUJA01/ JAYARAM PRASAD
 * Date : 11/03/2016
 */
package com.ca.apm.tests.agentcontrollability.https;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.testbed.AgentControllability3Collectors3TomcatAgentWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class AccHttpsTestsThreeCollectorsThreeAgents extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpsTestsThreeCollectorsThreeAgents.class);
    protected final String momhost;
    protected final String tomcatMachineID1;
    protected final String tomcatMachineID2;
    protected final String tomcatMachineID3;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String configFileC1;
    protected final String configFileC2;
    protected final String configFileC3;
    protected final String user;
    protected final String password;
    protected final String AgentExpression;
    protected final String tomcatAgentExpression;
    protected final String MetricExpression;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String jBossAgentExpression;
    protected final String c1Port;
    protected final String c2Port;
    protected final String c3Port;
    protected final String collector1Host;
    protected final String collector2Host;
    protected final String collector3Host;
    protected final String tomcatHost1;
    protected final String tomcatHost2;
    protected final String tomcatHost3;
    protected final String clwJarFileLoc;
    protected final String momWebPort;
    protected final String momSecureWebPort;
    protected final String c1WebPort;
    protected final String c2WebPort;
    protected final String c3WebPort;
    protected final String momPort;
    protected final String tomcat1agentProfileFile;
    protected final String tomcat2agentProfileFile;
    protected final String tomcat3agentProfileFile;
    protected final String emJettyFileMom;
    protected final String emJettyFileC1;
    protected final String emJettyFileC2;
    protected final String emJettyFileC3;
    protected final String tomcat1AgentLogFile;
    protected final String tomcat2AgentLogFile;
    protected final String tomcat3AgentLogFile;
    protected final String emSecureWebPort;
    protected final String col1LibDir;
    public String metric = "Enterprise Manager:Host";
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String oldProp;
    protected String newProp;

    public AccHttpsTestsThreeCollectorsThreeAgents() {

        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        jBossAgentExpression = "\".*\\|.*\\|JBoss.*\"";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emPort");
        c3Port = envProperties.getRolePropertiesById(COLLECTOR3_ROLE_ID).getProperty("emPort");
        momSecureWebPort = ApmbaseConstants.emSecureWebPort;
        momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emWebPort");
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");
        c2WebPort =
            envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emWebPort");
        c3WebPort =
            envProperties.getRolePropertiesById(COLLECTOR3_ROLE_ID).getProperty("emWebPort");

        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
        collector2Host = envProperties.getMachineHostnameByRoleId(COLLECTOR2_ROLE_ID);
        collector3Host = envProperties.getMachineHostnameByRoleId(COLLECTOR3_ROLE_ID);

        tomcatHost1 = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE1_ID);
        tomcatHost2 = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE2_ID);
        tomcatHost3 = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE3_ID);

        loadBalanceFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        momhost = envProperties.getMachineHostnameByRoleId(MOM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);

        col1LibDir =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_LIB_DIR);
        EMlogFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);

        configFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileMom_backup = configFileMom + "_backup";
        configFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileC2 =
            envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileC3 =
            envProperties.getRolePropertyById(COLLECTOR3_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        emJettyFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        emJettyFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/em-jetty-config.xml";
        emJettyFileC2 =
            envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/em-jetty-config.xml";
        emJettyFileC3 =
            envProperties.getRolePropertyById(COLLECTOR3_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/em-jetty-config.xml";
        tomcatMachineID1 = TOMCAT_MACHINE_ID1;
        tomcatMachineID2 = TOMCAT_MACHINE_ID2;
        tomcatMachineID3 = TOMCAT_MACHINE_ID3;
        clwJarFileLoc = emLibDir + "CLWorkstation.jar";
        tomcat1agentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE1_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcat2agentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE2_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcat3agentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE3_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";

        tomcat1AgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE1_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        tomcat2AgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE2_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        tomcat3AgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE3_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";

        user = "Admin";
        password = "";
    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        List<String> machines = new ArrayList<String>();
        machines.add(MOM_MACHINE_ID);
        machines.add(COLLECTOR1_MACHINE_ID);
        machines.add(COLLECTOR2_MACHINE_ID);
        machines.add(COLLECTOR3_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID1);
        machines.add(TOMCAT_MACHINE_ID2);
        machines.add(TOMCAT_MACHINE_ID3);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(COLLECTOR2_ROLE_ID);
        roleIds.add(COLLECTOR3_ROLE_ID);

        updateEMPropertiesForHTTPS(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        roleIds.clear();

        roleIds.add(TOMCAT_ROLE1_ID);
        roleIds.add(TOMCAT_ROLE2_ID);
        roleIds.add(TOMCAT_ROLE3_ID);
        updateTomcatPropertiesForHTTPS(roleIds);

        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE1_ID);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE2_ID);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE3_ID);
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353258_AgentDisallowMetric() {
        String testCaseId = "353258";
        try {
            LOGGER.info("verify_ALM_353258_AgentDisallowMetric");
            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", MOM_MACHINE_ID, configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.allowed changed from true to false");
            harvestWait(30);
            startAllAgents();
            int metricValue =
                validateDisallowedAgentMetricsByCount(momhost, Integer.parseInt(momPort), emLibDir,
                    ".*Custom Metric Process.*",
                    "Enterprise Manager\\|Connections:Number of Disallowed Agents",3);
            Assert.assertEquals(3, metricValue);

        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353257_AgentDisallowClamp() {
        String testCaseId = "353257";
        try {
            LOGGER.info("verify_ALM_353257_AgentDisallowClamp");
            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            // boolean flag = false;
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", MOM_MACHINE_ID, configFileMom);
            replaceProp("introscope.enterprisemanager.agent.disallowed.connection.limit=0",
                "introscope.enterprisemanager.agent.disallowed.connection.limit=1", MOM_MACHINE_ID,
                configFileMom);
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startAllAgents();
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.allowed changed from true to false");
            harvestWait(30);
            int metricValue =
                getDisallowedAgentMetrics(
                    momhost,
                    Integer.parseInt(momPort),
                    emLibDir,
                    "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
                    "Enterprise Manager\\|Connections:Disallowed Agents Clamped");
            Assert.assertEquals(1, metricValue);
        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353252_verifyNeedClientAuthentication() {
        String testCaseId = "353252";
        try {
            LOGGER.info("verify_ALM_353252_verifyNeedClientAuthentication");
            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", MOM_MACHINE_ID, configFileMom);
            boolean flag = false;
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startAllAgents();
            // Hot config property
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.allowed changed from true to false");
            harvestWait(30);
            for (int k = 0; k < 20; k++) {
                List<String> list =
                    getConnectedAgentMetricForEMHost(".*Custom Metric Process.*",
                        "Enterprise Manager\\|Connections:Number of Disallowed Agents", momhost,
                        Integer.parseInt(momPort), emLibDir);
                if (list.size() >= 3) {
                    for (int i = 2; i < list.size(); i++) {
                        String value = list.get(i).split(",")[13];
                        LOGGER.debug("The value is " + value);
                        if (Integer.parseInt(value) >= 1) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag == false) harvestWait(15);
                if (flag == true) break;
            }
            Assert.assertTrue(flag);

            stopEMServices();
            replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
                "introscope.apm.agentcontrol.agent.allowed=true", MOM_MACHINE_ID, configFileMom);

            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353271_AllowDisallowRedirectMomCollector() {
        String testCaseId = "353271";
        String msg = "Active = \"true\"";
        backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
        try {
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startAllAgents();
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, msg);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, tomcat2AgentLogFile, msg);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID3, tomcat3AgentLogFile, msg);
            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector2Host + ":" + emSecureWebPort, "include");
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            List<String> list =
                ValidateCountForConnectedAgentsExpressionToEMHost(collector2Host, Integer.parseInt(c2Port),
                    ".*Tomcat.*", emLibDir,3);
            Assert.assertTrue(list.size()==3);

        } finally {
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Tas(testBeds = @TestBed(name = AgentControllability3Collectors3TomcatAgentWindowsTestbed.class, executeOn = MOM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353285_AgentReconnect() {
        String testCaseId = "353285";
        String agentLogDir =
            envProperties.getRolePropertyById(TOMCAT_ROLE3_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/";
        String origTomcatLog = "IntroscopeAgent.log";
        String Tomcat_10Log = "IntroscopeAgent_353285_10.log";
        String Tomcat_M1Log = "IntroscopeAgent_353285_-1.log";
        String Tomcat_BLANKLog = "IntroscopeAgent_353285_BLANK.log";
        String Tomcat_60Log = "IntroscopeAgent_353285_60.log";

        List<String> logCreationVerificationList = new ArrayList<String>();
        logCreationVerificationList.add("DEBUG");

        List<String> agentMachineIds = new ArrayList<String>();
        agentMachineIds.add(TOMCAT_MACHINE_ID3);

        List<String> keyWords = new ArrayList<String>();
        keyWords.add("Waiting 15000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 30000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 45000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 60000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 75000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 90000 milliseconds for Introscope Enterprise Manager " + momhost);

        /**
         * Scenario 1
         */
        LOGGER
            .info("**************************************************************************************");
        LOGGER
            .info("**********************************SCENARIO 1 STARTED**********************************");
        LOGGER
            .info("**************************************************************************************");
        try {
            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", MOM_MACHINE_ID, configFileMom);
            replaceProp("introscope.enterprisemanager.agent.disallowed.connection.limit=0",
                "introscope.enterprisemanager.agent.disallowed.connection.limit=1", MOM_MACHINE_ID,
                configFileMom);

            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            checkAllCollectorToMOMConnectivity();
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
                "log4j.logger.IntroscopeAgent=DEBUG,logfile", TOMCAT_MACHINE_ID1,
                tomcat1agentProfileFile);
            replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
                "log4j.logger.IntroscopeAgent=DEBUG,logfile", TOMCAT_MACHINE_ID2,
                tomcat2agentProfileFile);
            replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
                "log4j.logger.IntroscopeAgent=DEBUG,logfile", TOMCAT_MACHINE_ID3,
                tomcat3agentProfileFile);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            boolean flag = false;
            for (int k = 0; k < 20; k++) {
                List<String> list =
                    getConnectedAgentMetricForEMHost(
                        "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
                        "Enterprise Manager\\|Connections:Disallowed Agents Clamped", momhost,
                        Integer.parseInt(momPort), emLibDir);
                if (list.size() >= 3) {
                    for (int i = 2; i < list.size(); i++) {
                        String value = list.get(i).split(",")[13];
                        LOGGER.debug("The value is " + value);
                        if (Integer.parseInt(value) >= 1) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag == false) harvestWait(15);
                if (flag == true) break;
            }

            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE2_ID);
            startTomcatAgent(TOMCAT_ROLE3_ID);
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 2 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(origTomcatLog, Tomcat_10Log, TOMCAT_MACHINE_ID3, tomcat3agentProfileFile);
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_10Log,
                logCreationVerificationList);
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=45",
                "introscope.apm.agentcontrol.agent.reconnect.wait=10", MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 45 to 10");
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Received agent reconnect wait interval.  Value = 45");
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_10Log, keyWords);

            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 3 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(Tomcat_10Log, Tomcat_M1Log, TOMCAT_MACHINE_ID3, tomcat3agentProfileFile);
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_M1Log,
                logCreationVerificationList);
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=10",
                "introscope.apm.agentcontrol.agent.reconnect.wait=-1", MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "introscope.apm.agentcontrol.agent.reconnect.wait is negative: -1");
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45");
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_M1Log,
                keyWords);

            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 4 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(Tomcat_M1Log, Tomcat_BLANKLog, TOMCAT_MACHINE_ID3, tomcat3agentProfileFile);
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_BLANKLog,
                logCreationVerificationList);
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=-1",
                "introscope.apm.agentcontrol.agent.reconnect.wait=", MOM_MACHINE_ID, configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "introscope.apm.agentcontrol.agent.reconnect.wait is not an integer:");
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45");
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_BLANKLog,
                keyWords);

            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 5 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(Tomcat_BLANKLog, Tomcat_60Log, TOMCAT_MACHINE_ID3, tomcat3agentProfileFile);
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_60Log, logCreationVerificationList);
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=",
                "introscope.apm.agentcontrol.agent.reconnect.wait=60", MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait:");
            checkLogForAgentReconnectMsg(agentMachineIds, agentLogDir + Tomcat_60Log,
                keyWords);

        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353272_AllowDisallowFromMomToCollector() {
        String testCaseId = "353272";
        String msg = "Active = \"false\"";
        backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
        try {
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startAllAgents();
            waitForAgentNodes(".*Tomcat1.*", momhost, Integer.parseInt(momPort), emLibDir);
            waitForAgentNodes(".*Tomcat2.*", momhost, Integer.parseInt(momPort), emLibDir);
            waitForAgentNodes(".*Tomcat3.*", momhost, Integer.parseInt(momPort), emLibDir);
            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat1.*", collector1Host + ":" + emSecureWebPort, "exclude");
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, msg);
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat2.*", collector1Host + ":" + emSecureWebPort, "exclude");
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, tomcat2AgentLogFile, msg);
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat3.*", collector1Host + ":" + emSecureWebPort, "exclude");
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID3, tomcat3AgentLogFile, msg);
        } finally {
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353262_AllowDisallowRedirectMomCollector() {
        String testCaseId = "353262";
        backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
        try {
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startAllAgents();
            waitForAgentNodes(".*Tomcat1.*", momhost, Integer.parseInt(momPort), emLibDir);
            waitForAgentNodes(".*Tomcat2.*", momhost, Integer.parseInt(momPort), emLibDir);
            waitForAgentNodes(".*Tomcat3.*", momhost, Integer.parseInt(momPort), emLibDir);
            try {
                xmlUtil.addlatchedEntryInLoadBalXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + emSecureWebPort, collector2Host + ":"
                        + emSecureWebPort, "collector1Host:true");
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            waitForAgentNodes(".*Tomcat1.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            waitForAgentNodes(".*Tomcat2.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            waitForAgentNodes(".*Tomcat3.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            try {
                xmlUtil.addlatchedEntryInLoadBalXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + emSecureWebPort, collector2Host + ":"
                        + emSecureWebPort, "collector2Host:true");
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            waitForAgentNodes(".*Tomcat1.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
            waitForAgentNodes(".*Tomcat2.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
            waitForAgentNodes(".*Tomcat3.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
            waitForAgentNodes(".*Tomcat1.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            waitForAgentNodes(".*Tomcat2.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            waitForAgentNodes(".*Tomcat3.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat1.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
            waitForAgentNodes(".*Tomcat2.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
            waitForAgentNodes(".*Tomcat3.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
        } finally {
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    public void startEMCollectors() {
        try {
            startEM(COLLECTOR1_ROLE_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            startEM(COLLECTOR2_ROLE_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            startEM(COLLECTOR3_ROLE_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startAllAgents() {
        try {
            startTomcatAgent(TOMCAT_ROLE1_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            startTomcatAgent(TOMCAT_ROLE2_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            startTomcatAgent(TOMCAT_ROLE3_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkAllCollectorToMOMConnectivity() {
        checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
            momhost, momPort, emLibDir);
        checkSpecificCollectorToMOMConnectivity(".*" + collector2Host + ".*", MetricExpression,
            momhost, momPort, emLibDir);
        checkSpecificCollectorToMOMConnectivity(".*" + collector3Host + ".*", MetricExpression,
            momhost, momPort, emLibDir);
    }

    public void stopEMServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR3_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR3_MACHINE_ID);
        stopEM(MOM_ROLE_ID);
        stopEMServiceFlowExecutor(MOM_MACHINE_ID);
        harvestWait(10);
    }

    public void stopAllAgents() {
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID1);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID2);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID3);
    }

    public void stopServices() {
        stopAllAgents();
        stopEMServices();
        harvestWait(5);
    }

    public void checkLogForAgentReconnectMsg(List<String> machineIds, String logPath,
        List<String> keyWords) {

        for (String machineId : machineIds) {
            int i = 0;
            boolean flag = false;
            for (i = 0; i < 80; i++) {
                try {
                    for (String keyWord : keyWords) {
                        try {
                            isKeywordInFile(envProperties, machineId, logPath, keyWord);
                            flag = true;
                            break;
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    if (flag) break;
                } catch (Exception e) {
                    if (i < 80) {
                        harvestWait(15);
                        continue;
                    }
                }
            }
            Assert.assertTrue(flag);
        }

    }
}
