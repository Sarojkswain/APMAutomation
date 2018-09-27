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
package com.ca.apm.tests.agentcontrollability.ssl;

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
import com.ca.apm.tests.testbed.AgentControllability3Collectors1TomcatAgentWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class AccSSLTestsThreeCollectorsThreeAgents extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccSSLTestsThreeCollectorsThreeAgents.class);
    protected final String momhost;
    protected final String tomcatMachineID1;
    protected final String tomcatMachineID2;
    protected final String tomcatMachineID3;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String configFileC1;
    protected final String configFileC2;
    protected final String configFileC3;
    protected final String MetricExpression;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String c2Port;
    protected final String collector1Host;
    protected final String collector2Host;
    protected final String collector3Host;
    protected final String momPort;
    protected final String tomcat1agentProfileFile;
    protected final String tomcat2agentProfileFile;
    protected final String tomcat3agentProfileFile;
    protected final String tomcat1AgentLogFile;
    protected final String tomcat2AgentLogFile;
    protected final String tomcat3AgentLogFile;
    protected final String emSecurePort;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;

    public AccSSLTestsThreeCollectorsThreeAgents() {

        emSecurePort = ApmbaseConstants.emSSLPort;
        tomcatAgentExp = ".*Tomcat.*";
        MetricExpression = ".*CPU.*";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emPort");
        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
        collector2Host = envProperties.getMachineHostnameByRoleId(COLLECTOR2_ROLE_ID);
        collector3Host = envProperties.getMachineHostnameByRoleId(COLLECTOR3_ROLE_ID);
        loadBalanceFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        momhost = envProperties.getMachineHostnameByRoleId(MOM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        EMlogFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
        configFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);

        configFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileC2 =
            envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileC3 =
            envProperties.getRolePropertyById(COLLECTOR3_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);

        tomcatMachineID1 = TOMCAT_MACHINE_ID1;
        tomcatMachineID2 = TOMCAT_MACHINE_ID2;
        tomcatMachineID3 = TOMCAT_MACHINE_ID3;
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

        updateEMPropertiesForSSL(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        roleIds.clear();

        roleIds.add(TOMCAT_ROLE1_ID);
        roleIds.add(TOMCAT_ROLE2_ID);
        roleIds.add(TOMCAT_ROLE3_ID);
        updateTomcatPropertiesForSSL(roleIds);

        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE1_ID);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE2_ID);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE3_ID);
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353440_Number_of_Disallowed_Agents_Clamped_SSL() {
        String testCaseId = "353440";
        try {
            LOGGER.info("verify_ALM_353440_AgentDisallowMetric");
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
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSecurePort);
            setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort, momhost,
                emSecurePort);
            setAgentSSLUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort, momhost,
                emSecurePort);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, MOM_MACHINE_ID,
                configFileMom);
            replaceProp("introscope.enterprisemanager.agent.disallowed.connection.limit=0",
                "introscope.enterprisemanager.agent.disallowed.connection.limit=2", MOM_MACHINE_ID,
                configFileMom);
            // Hot config property
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.allowed changed from true to false");
            harvestWait(30);
            startAllAgents();

            checkMetricClampOrCount(
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
                "Enterprise Manager\\|Connections:Disallowed Agents Clamped", 1);
            checkMetricClampOrCount(".*Custom Metric Process.*",
                "Enterprise Manager\\|Connections:Number of Disallowed Agents", 2);

            checkMetricClampOrCount(
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
                "Enterprise Manager\\|Connections:Disallowed Agents Clamped", 0);
            checkMetricClampOrCount(".*Custom Metric Process.*",
                "Enterprise Manager\\|Connections:Number of Disallowed Agents", 3);

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
    public void verify_ALM_353467_AgentReconnect() {

        String testCaseId = "353467";
        String agentLogDir =
            envProperties.getRolePropertyById(TOMCAT_ROLE2_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/";
        String origTomcatLog = "IntroscopeAgent.log";
        String Tomcat_10Log = "IntroscopeAgent_353285_10.log";
        String Tomcat_M1Log = "IntroscopeAgent_353285_-1.log";
        String Tomcat_BLANKLog = "IntroscopeAgent_353285_BLANK.log";
        String Tomcat_60Log = "IntroscopeAgent_353285_60.log";

        List<String> keyWords = new ArrayList<String>();
        keyWords.add("Waiting 15000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 30000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 45000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 60000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 75000 milliseconds for Introscope Enterprise Manager " + momhost);
        keyWords.add("Waiting 90000 milliseconds for Introscope Enterprise Manager " + momhost);

        String keyWord = "Waiting 45000 milliseconds for Introscope Enterprise Manager " + momhost;

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
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, MOM_MACHINE_ID,
                configFileMom);
            replaceProp("introscope.enterprisemanager.agent.disallowed.connection.limit=0",
                "introscope.enterprisemanager.agent.disallowed.connection.limit=1", MOM_MACHINE_ID,
                configFileMom);

            startEMCollectors();
            try {


            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            checkAllCollectorToMOMConnectivity();
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSecurePort);
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
            setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort, momhost,
                emSecurePort);
            setAgentSSLUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort, momhost,
                emSecurePort);
            startTomcatAgent(TOMCAT_ROLE2_ID);
            startTomcatAgent(TOMCAT_ROLE3_ID);
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 2 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(origTomcatLog, Tomcat_10Log, TOMCAT_MACHINE_ID2, tomcat2agentProfileFile);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_10Log, "DEBUG");
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=45",
                "introscope.apm.agentcontrol.agent.reconnect.wait=10", MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 45 to 10");
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Received agent reconnect wait interval.  Value = 45");
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_10Log, keyWord);

            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 3 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(Tomcat_10Log, Tomcat_M1Log, TOMCAT_MACHINE_ID2, tomcat2agentProfileFile);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_M1Log, "DEBUG");
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=10",
                "introscope.apm.agentcontrol.agent.reconnect.wait=-1", MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "introscope.apm.agentcontrol.agent.reconnect.wait is negative: -1");
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45");
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_M1Log, keyWord);

            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 4 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(Tomcat_M1Log, Tomcat_BLANKLog, TOMCAT_MACHINE_ID2, tomcat2agentProfileFile);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_BLANKLog,
                "DEBUG");
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=-1",
                "introscope.apm.agentcontrol.agent.reconnect.wait=", MOM_MACHINE_ID, configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "introscope.apm.agentcontrol.agent.reconnect.wait is not an integer:");
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45");
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_BLANKLog,
                keyWord);

            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 5 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            replaceProp(Tomcat_BLANKLog, Tomcat_60Log, TOMCAT_MACHINE_ID2, tomcat2agentProfileFile);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_60Log, "DEBUG");
            replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=",
                "introscope.apm.agentcontrol.agent.reconnect.wait=60", MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait:");
            verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID2, agentLogDir + Tomcat_60Log,
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

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353442_Overriden_Collector_settings_by_MOM_SSL() {
        String testCaseId = "353442";

        try {
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR2_MACHINE_ID, configFileC2);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR3_MACHINE_ID, configFileC3);


            startEMCollectors();
            startEM(MOM_ROLE_ID);
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSecurePort);
            setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSecurePort);

            startTomcatAgent(TOMCAT_ROLE1_ID);
            waitForAgentNodes(".*Tomcat1.*", momhost, Integer.parseInt(momPort), emLibDir);

            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, MOM_MACHINE_ID,
                configFileMom);

            startTomcatAgent(TOMCAT_ROLE2_ID);
            LOGGER.info("Confirm that already connected tomcat1 agent is not effected....");
            waitForAgentNodes(".*Tomcat1.*", momhost, Integer.parseInt(momPort), emLibDir);
            LOGGER.info("Already connected agent is not effected, Test Passed");
        } finally {

            stopServices();
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                AgentControllabilityConstants.defaultEMAgentAllowedProp, COLLECTOR1_MACHINE_ID,
                configFileC1);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                AgentControllabilityConstants.defaultEMAgentAllowedProp, COLLECTOR2_MACHINE_ID,
                configFileC2);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                AgentControllabilityConstants.defaultEMAgentAllowedProp, COLLECTOR3_MACHINE_ID,
                configFileC3);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                AgentControllabilityConstants.defaultEMAgentAllowedProp, MOM_MACHINE_ID,
                configFileMom);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testCaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, TOMCAT_MACHINE_ID2, testCaseId);


        }

    }

  
  @Test(groups = {"SMOKE"}, enabled = true)
  public void verify_ALM_353454_AllowDisallowFromMomToCollector() {
      String testCaseId = "353454";
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
          setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
              momhost, emSecurePort);
          setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
              momhost, emSecurePort);
          setAgentSSLUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
              momhost, emSecurePort);
          startAllAgents();
          waitForAgentNodes(".*Tomcat1.*", momhost, Integer.parseInt(momPort), emLibDir);
          waitForAgentNodes(".*Tomcat2.*", momhost, Integer.parseInt(momPort), emLibDir);
          waitForAgentNodes(".*Tomcat3.*", momhost, Integer.parseInt(momPort), emLibDir);
          try {
              xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                  ".*\\|.*\\|.*Tomcat1.*", collector1Host + ":" + emSecurePort, "exclude");
          } catch (Exception e) {
              Assert.assertTrue(false);
          }
          checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, msg);
          deleteFile(loadBalanceFile, MOM_MACHINE_ID);
          backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
          try {
              xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                  ".*\\|.*\\|.*Tomcat2.*", collector1Host + ":" + emSecurePort, "exclude");
          } catch (Exception e) {
              Assert.assertTrue(false);
          }
          checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, tomcat2AgentLogFile, msg);
          deleteFile(loadBalanceFile, MOM_MACHINE_ID);
          backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
          try {
              xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                  ".*\\|.*\\|.*Tomcat3.*", collector1Host + ":" + emSecurePort, "exclude");
          } catch (Exception e) {
              Assert.assertTrue(false);
          }
          checkLogForMsg(envProperties, TOMCAT_MACHINE_ID3, tomcat3AgentLogFile, msg);
      } finally {
          stopServices();
          deleteFile(loadBalanceFile, MOM_MACHINE_ID);
          revertFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
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
  public void verify_ALM_353439_AgentDisallowClamp() {
      String testCaseId = "353439";
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
          setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
              momhost, emSecurePort);
          setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
              momhost, emSecurePort);
          setAgentSSLUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
              momhost, emSecurePort);
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
          revertFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
          renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
          renameLogWithTestCaseID(tomcat2AgentLogFile, tomcatMachineID2, testCaseId);
          renameLogWithTestCaseID(tomcat3AgentLogFile, tomcatMachineID3, testCaseId);
          revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
          revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
          revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
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

    public void checkMetricClampOrCount(String path, String metricName, int count) {
        boolean flag = false;

        for (int k = 0; k < 20; k++) {
            List<String> list =
                getConnectedAgentMetricForEMHost(path, metricName, momhost,
                    Integer.parseInt(momPort), emLibDir);
            if (list.size() >= 3) {
                for (int i = 2; i < list.size(); i++) {
                    String value = list.get(i).split(",")[13];
                    LOGGER.debug("The value is " + value);
                    if (Integer.parseInt(value) == count) {
                        flag = true;
                        break;
                    }
                }
            }
            if (flag == false) harvestWait(15);
            if (flag == true) break;
        }
    }
}
