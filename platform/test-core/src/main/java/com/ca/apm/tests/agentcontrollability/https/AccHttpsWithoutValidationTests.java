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

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;

public class AccHttpsWithoutValidationTests extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpsWithoutValidationTests.class);
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

    public AccHttpsWithoutValidationTests() {

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

        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(COLLECTOR2_ROLE_ID);
        roleIds.add(COLLECTOR3_ROLE_ID);

        updateEMPropertiesForHTTPSWithoutValidation(roleIds);
    }


    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353269_AgentAllowDisallowCollector() {
        String testCaseId = "353269";
        try {
            LOGGER.info("verify_ALM_353269_AgentAllowDisallowCollector");

            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                collector1Host, emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            String msg = "Active = \"false\"";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, msg);
        } finally {
            stopServices();
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353270_AgentAllowDisallow_Redirect_MOM_Collector() {
        String testCaseId = "353270";
        try {
            LOGGER.info("verify_ALM_353484_DisallowRedirectMomCollector");
            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);

            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + emSecureWebPort, "include");
            } catch (Exception e) {
                e.printStackTrace();
            }
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSecureWebPort);
            setAgentHttpsUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort,
                momhost, emSecureWebPort);
            startAllAgents();
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            List<String> list =
                getConnectedAgentMetricForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                    Integer.parseInt(c1WebPort), emLibDir);
            Assert.assertTrue(list.size() >= 3);

            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
            try {
                xmlUtil.addCollectorsEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + emSecureWebPort, collector2Host
                        + ":" + emSecureWebPort, "include");
            } catch (Exception e) {
                e.printStackTrace();
            }
            verifyTwoCollectors();
        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353260_OverriddenCollectorSettingsByMom() {
        String testCaseId = "353260";
        try {
            LOGGER.info("verify_ALM_353260_OverriddenCollectorSettingsByMom");
            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);
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
                collector1Host, emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopServices();
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, tomcatMachineID1, testCaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353276_Self_lookup_Without_Validation_HTTPS() {
        try {
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                momhost, emSecureWebPort);
            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            List<String> collector_List = new ArrayList<String>();
            List<Integer> collector_Port_List = new ArrayList<Integer>();
            List<String> collectors_RoleIDs = new ArrayList<String>();

            collectors_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collectors_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collector_List.add(collector1Host);
            collector_List.add(collector2Host);
            collector_Port_List.add(5001);
            collector_Port_List.add(5001);

            String collectorHostname =
                getAgentConnectedCollectorName(collector_List, collector_Port_List,
                    collectors_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            String collector_RoleID =
                getAgentConnectedCollectorName(collector_List, collector_Port_List,
                    collectors_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent connected to Collector for the First Time is "
                + collectorHostname);
            stopCollectorEM(MOM_ROLE_ID, collector_RoleID);
            for (int i = 0; i < collector_List.size(); i++) {
                if (collectorHostname.trim().equalsIgnoreCase(collector_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collector_List.get(i));
                    collector_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collector_List.get(i));
            }
            for (int j = 0; j < collectors_RoleIDs.size(); j++) {
                if (collector_RoleID.trim().equalsIgnoreCase(collectors_RoleIDs.get(j).trim())) {
                    LOGGER.info("Remove the Collector ROLE from the List "
                        + collectors_RoleIDs.get(j));
                    collectors_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collectors_RoleIDs.get(j));
            }
            stopEM(MOM_ROLE_ID);
            stopCollectorEM(MOM_ROLE_ID, collector_RoleID);

            String collector2Hostname =
                getAgentConnectedCollectorName(collector_List, collector_Port_List,
                    collectors_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The leftout connected to which agent is connected is..."
                + collector2Hostname);
            if (collector2Hostname.contains(collector2Hostname))
                Assert.assertTrue(true);
            else
                Assert.assertTrue(false);
        } finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameFile(tomcat1AgentLogFile, tomcat1AgentLogFile + "_353259", TOMCAT_MACHINE_ID1);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353326_no_ACC_Agent_Direct_Connection_To_Collector_HTTPS() {

        try {
            List<String> cluster_Collectors = new ArrayList<String>();

            cluster_Collectors.add(clusterEM1Host + collector1Host);
            cluster_Collectors.add(clusterEM1Port + c1Port);
            cluster_Collectors.add(clusterEM1PublicKey);
            cluster_Collectors.add(clusterEM2Host + collector2Host);
            cluster_Collectors.add(clusterEM2Port + c2Port);
            cluster_Collectors.add(clusterEM2PublicKey);

            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);
            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);
            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);
            setAgentHttpsUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                collector1Host, emSecureWebPort);

            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE1_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            List<String> collectors_List = new ArrayList<String>();
            List<Integer> collector_Port_List = new ArrayList<Integer>();
            List<String> collector_RoleIDs = new ArrayList<String>();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);

            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);

            collector_Port_List.add(5001);
            collector_Port_List.add(5001);

            String collectorHostname =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            String collectorRoleID =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent connected to Collector for the First Time is "
                + collectorHostname);

            stopCollectorEM(MOM_ROLE_ID, collectorRoleID);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (collectorHostname.trim().equalsIgnoreCase(collectors_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {
                if (collectorRoleID.trim().equalsIgnoreCase(collector_RoleIDs.get(j).trim())) {
                    LOGGER.info("Remove the Collector ROLE from the List "
                        + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collector_RoleIDs.get(j));
            }

            LOGGER.info("The leftout collector host is...."
                + collectors_List.get(collectors_List.size() - 1));

            getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*",
                collectors_List.get(collectors_List.size() - 1), Integer.parseInt(momPort),
                emLibDir);

            String collectorHostname_1 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after " + collectorHostname
                + " Restart is " + collectorHostname_1);

            startEM(collectorRoleID);
            LOGGER.info("stopped Collector Started Succesfully");

            String collectorHostname_2 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);


            checkAgentAllowedMessage(collectorHostname_2, tomcat1AgentLogFile);

            Assert.assertEquals(collectorHostname_1.trim(), collectorHostname_2.trim());
            LOGGER
                .info("The Agent connection is stable to the previous connected collector eventhough shutdowned collector is restarted");

        } catch (Exception e) {
            LOGGER.info("Some exception occured");
            e.printStackTrace();
        }

        finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            renameFile(tomcat1AgentLogFile, tomcat1AgentLogFile + "_353326", TOMCAT_MACHINE_ID1);

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

    public void verifyTwoCollectors() {
        int count1 = 0;
        int count2 = 0;
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            count1 =
                getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression, collector1Host,
                    Integer.parseInt(c1WebPort), emLibDir).size();
            count2 =
                getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression, collector2Host,
                    Integer.parseInt(c2WebPort), emLibDir).size();
            i++;
            if (count1 >= 3 || count2 >= 3) {
                flag = true;
                break;
            } else
                harvestWait(15);
        }
        if (i == 20) flag = false;
        Assert.assertTrue(flag);
    }

    public void updateEMPropertiesForHTTPSWithoutValidation(List<String> roleIds) {

        List<String> list = new ArrayList<String>();
        list.add("introscope.enterprisemanager.enabled.channels=channel1,channel2");

        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String emJetty =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/em-jetty-config.xml";
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            addHTTPEntryInEMJetty(emJetty, MACHINE_ID);
            /**
             * Replace MOM properties
             */
            replaceProp(
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                MACHINE_ID, configFile);
            replaceProp("introscope.enterprisemanager.enabled.channels=channel1",
                "#introscope.enterprisemanager.enabled.channels=channel1", MACHINE_ID, configFile);
            appendProp(list, MACHINE_ID, configFile);

        }
    }

    public void checkAgentAllowedMessage(String emHostName, String logFile) {
        List<String> agentAllowedMessage = new ArrayList<String>();
        agentAllowedMessage
            .add(" Connected to "
                + emHostName
                + ":"
                + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode.");

        agentAllowedMessage
            .add(" Connected to "
                + emHostName
                + ".ca.com:"
                + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode.");

        verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID1, logFile, agentAllowedMessage);
    }
}
