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
 * Author : TUUJA01/ JAYARAM PRASAD
 * Date : 11/07/2016
 */
package com.ca.apm.tests.agentcontrollability.ssl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.testbed.AgentControllability2Collectors1TomcatAgentLinuxTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.type.SizeType;


public class AccSSLTestsTwoCollectorsOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccSSLTestsTwoCollectorsOneAgent.class);
    protected final String momhost;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String MetricExpression;
    protected final String loadbalancingAgentExp;
    protected final String EMlogFile;
    protected final String c1Port;
    protected final String c2Port;
    protected final String collector1Host;
    protected final String collector2Host;
    protected final String momWebPort;
    protected final String c1WebPort;
    protected final String c2WebPort;
    protected final String momPort;
    protected final String tomcatagentProfileFile;
    protected final String emSSLPort;
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected final String tomcatAgentLogFile;
    protected final String configFileC1;
    protected final String configFileC2;
    protected final String loadBalanceFile;
    protected final String loadBalanceFile_Copy;
    protected String collectorHostName;
    protected String collector1HostName;
    protected String collector2HostName;
    protected String collectorRoleID;
    protected String testcaseId;


    List<String> cluster_Collectors;
    List<String> collectors_List;
    List<Integer> collector_Port_List;
    List<String> collector_RoleIDs;
    List<String> sslConnectonMsg;
    List<String> tempList;

    public AccSSLTestsTwoCollectorsOneAgent() {
        emSSLPort = ApmbaseConstants.emSSLPort;
        tomcatAgentExp = ".*Tomcat.*";
        MetricExpression = ".*CPU.*";
        loadbalancingAgentExp=".*\\|.*\\|.*Tomcat.*";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emPort");
        momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emWebPort");
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");
        c2WebPort =
            envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emWebPort");
        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
        collector2Host = envProperties.getMachineHostnameByRoleId(COLLECTOR2_ROLE_ID);
        momhost = envProperties.getMachineHostnameByRoleId(MOM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        EMlogFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
        configFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileMom_backup = configFileMom + "_backup";

        loadBalanceFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";

        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";

        configFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileC2 =
            envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);

        cluster_Collectors = new ArrayList<String>();

        collectors_List = new ArrayList<String>();
        collector_Port_List = new ArrayList<Integer>();
        collector_RoleIDs = new ArrayList<String>();
        sslConnectonMsg = new ArrayList<String>();
        tempList = new ArrayList<String>();
    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        List<String> machines = new ArrayList<String>();
        machines.add(MOM_MACHINE_ID);
        machines.add(COLLECTOR1_MACHINE_ID);
        machines.add(COLLECTOR2_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(COLLECTOR2_ROLE_ID);

        updateEMPropertiesForSSL(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        roleIds.clear();

        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForSSL(roleIds);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);


    }

   @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353428_down_Agent_Connected_Collector_on_Cluster_SSL()
        throws IOException {
        testcaseId = "353428";
        LOGGER.info("This is to verify_ALM_353428_down_Agent_Connected_Collector_on_Cluster_SSL");
        try {

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                returnIPforGivenHost(collector1Host), emSSLPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(returnIPforGivenHost(collector1Host));
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));

            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            collectorRoleID =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent that connected to Collector for the First Time is "
                + collectorHostName);

            stopCollectorEM(MOM_ROLE_ID, collectorRoleID);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (collectorHostName.trim().equalsIgnoreCase(collectors_List.get(i).trim())) {
                    LOGGER.info("Removed the Collector" + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed from the list this time "
                        + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (collectorRoleID.trim().equalsIgnoreCase(collector_RoleIDs.get(j).trim())) {

                    LOGGER.info("Removed the Collector Role " + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed fom the list this time "
                        + collector_RoleIDs.get(j));
            }


            LOGGER.info("The leftout collector host is...."
                + collectors_List.get(collectors_List.size() - 1));

            collector1HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after " + collectorHostName
                + " Restart is " + collector1HostName);

            startEM(collectorRoleID);
            LOGGER.info("Started the collector which was shutdown");

            collector2HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            LOGGER.info("After first collector down, the agent now pointed to ..."
                + collector2HostName);

            checkAgentAllowedMessage(collector2HostName, tomcatAgentLogFile);

            Assert.assertEquals(collector1HostName.trim(), collector2HostName.trim());
            LOGGER
                .info("The Agent connection is stable to the previous connected collector eventhough shutdown collector is restarted");
        }

        finally {
            stopAllAgents();
            stopEMServices();
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
        }
    }

   @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353471_no_ACC_Agent_Direct_Connection_To_Collector_SSL() {
        try {
            testcaseId = "353471";
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSSLPort);

            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));

            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            collectorRoleID =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent connected to Collector for the First Time is "
                + collectorHostName);

            stopCollectorEM(MOM_ROLE_ID, collectorRoleID);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (collectorHostName.trim().equalsIgnoreCase(collectors_List.get(i).trim())) {
                    LOGGER.info("Removed the Collector " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (collectorRoleID.trim().equalsIgnoreCase(collector_RoleIDs.get(j).trim())) {

                    LOGGER.info("Removed the Collector Role " + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collector_RoleIDs.get(j));
            }

            LOGGER.info("The leftout collector host is...."
                + collectors_List.get(collectors_List.size() - 1));

            tempList.clear();
            tempList =
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*",
                    collectors_List.get(collectors_List.size() - 1), Integer.parseInt(momPort),
                    emLibDir);

            if (tempList.size() < 3) Assert.assertTrue(false);

            collector1HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after " + collectorHostName
                + " Restart is " + collector1HostName);

            startEM(collectorRoleID);
            LOGGER.info("stopped Collector Started Succesfully");

            collector2HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            checkAgentAllowedMessage(collector2HostName, tomcatAgentLogFile);

            Assert.assertEquals(collector1HostName.trim(), collector2HostName.trim());
            LOGGER.info("Test Passed,even after collector restart Agent pointed to same collector");

        } finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + testcaseId, TOMCAT_MACHINE_ID);
        }
    }

   @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353441_Self_lookup_MOM_and_connected_collector_down_SSL() {
        testcaseId = "353441";
        try {
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);
            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));

            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            collectorRoleID =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent connected to Collector for the First Time is "
                + collectorHostName);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (collectorHostName.trim().equalsIgnoreCase(collectors_List.get(i).trim())) {
                    LOGGER.info("Removed the Collector " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }
            for (int j = 0; j < collector_RoleIDs.size(); j++) {
                if (collectorRoleID.trim().equalsIgnoreCase(collector_RoleIDs.get(j).trim())) {
                    LOGGER.info("Removed the Collector Role " + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collector_RoleIDs.get(j));
            }
            stopEM(MOM_ROLE_ID);
            stopCollectorEM(MOM_ROLE_ID, collectorRoleID);

            String leftOutCollector = collectors_List.get(collectors_List.size() - 1);

            collector1HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            LOGGER.info("Agent now connected remaining Col.." + collector1HostName);

            Assert.assertEquals(leftOutCollector, collector1HostName);

        } finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + testcaseId, TOMCAT_MACHINE_ID);
        }
    }

   @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353456_Self_lookup_restart_agent_after_selflookup_SSL() {
        testcaseId = "353456";
        try {

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();


            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));

            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            collectorRoleID =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent connected to Collector for the First Time is "
                + collectorHostName);

            stopCollectorEM(MOM_ROLE_ID, collectorRoleID);
            stopEM(MOM_ROLE_ID);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (collectorHostName.trim().equalsIgnoreCase(collectors_List.get(i).trim())) {
                    LOGGER.info("Removed the Collector" + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {
                if (collectorRoleID.trim().equalsIgnoreCase(collector_RoleIDs.get(j).trim())) {

                    LOGGER.info("Removed the Collector Role" + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collector_RoleIDs.get(j));
            }
            LOGGER.info("The leftout collector host is...."
                + collectors_List.get(collectors_List.size() - 1));

            collector1HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after " + collectorHostName
                + " Restart is " + collector1HostName);
            stopTomcatAgent(TOMCAT_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            String agentConnectionFailureMessage =
                "Failed to connect to the Introscope Enterprise Manager at";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                agentConnectionFailureMessage);
            startEM(MOM_ROLE_ID);
            LOGGER.info("MOM restarted succesfully");
            collector2HostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("After C1 down & MOM restart, the agent now pointed to ..."
                + collector2HostName);

            checkAgentAllowedMessage(collector2HostName, tomcatAgentLogFile);

            LOGGER
                .info("After Agent & MOM restart and MOM is Up, Agent connected to the available collectors...that is"
                    + collector2HostName);
        } finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
        }
    }

   @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353438_MomAndCollectorOnSSL() {
        testcaseId = "353438";
        try {
            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));

            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            checkAgentAllowedMessage(collectorHostName, tomcatAgentLogFile);

            Assert.assertTrue(collectors_List.toString().contains(collectorHostName));
            LOGGER.info("Agent connected to available collector " + collectorHostName);

        } finally {
            stopEMServices();
            stopAllAgents();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

   @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353469_NoACCGlobalAllowSSL() {
        testcaseId = "353469";
        try {
            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));

            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);
            backupFile(configFileC2, configFileC2 + "_backup", COLLECTOR2_MACHINE_ID);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR2_MACHINE_ID, configFileC2);
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSSLPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            checkAllCollectorToMOMConnectivity();

            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("Agent connected to collector" + collectorHostName);

            collectorRoleID =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            stopEMServices();
            startEM(collectorRoleID);
            int disallowedCount =
                getNumberOfDisallowedAgentsAtCollector(collectorHostName, 5001, emLibDir);
            LOGGER.info("The disallowed agent count at collector is" + disallowedCount);
            if (disallowedCount >= 1)
                Assert.assertTrue(true);
            else
                Assert.assertTrue(false);

        } finally {
            stopEMServices();
            stopAllAgents();
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            deleteFile(configFileC2, COLLECTOR2_MACHINE_ID);
            moveFile(configFileC2 + "_backup", configFileC2, COLLECTOR2_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

   @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353464_Proper_Log_Messages() {
        try {
            LOGGER.info("verify_ALM_353464_Proper_Log_Messages");
            List<String> cluster_Collectors = new ArrayList<String>();
            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));


            String newtomcat1AgentLogFile =
                envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                    DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                    + "/wily/logs/IntroscopeAgent_new.log";
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.host="
                + collector1Host);
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.port="
                + c1Port);
            cluster_Collectors
                .add("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public");
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em2.host="
                + collector2Host);
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em2.port="
                + c2Port);
            cluster_Collectors
                .add("introscope.enterprisemanager.clustering.login.em2.publickey=config/internal/server/EM.public");

            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);
            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);
            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);

            startTomcatAgent(TOMCAT_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);


            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            replaceProp("IntroscopeAgent.log", "IntroscopeAgent_new.log", TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            harvestWait(120);

            stopCollector(COLLECTOR1_ROLE_ID, COLLECTOR1_MACHINE_ID);
            stopCollector(COLLECTOR2_ROLE_ID, COLLECTOR2_MACHINE_ID);
            String logMsg =
                "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at ";
            checkTomcatLog(logMsg, newtomcat1AgentLogFile);

            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            collectorHostName =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            checkAgentAllowedMessage(collectorHostName, newtomcat1AgentLogFile);

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
            LOGGER.info("Checking for disallowed mode message in agent log");
            Assert.assertEquals(1,
                getNumberOfDisallowedAgents(momhost, Integer.parseInt(momPort), emLibDir));
            checkAgentAllowedMessage(momhost, newtomcat1AgentLogFile);
            LOGGER.info("verify_ALM_353464_Proper_Log_Messages Ended");
        } finally {
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            stopServices();
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "353464");
        }
    }

   @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353444_Test_latch_functionality_in_loadbalancing_xml_file_SSL()
        throws IOException {
        testcaseId = "353444";

        try {
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);

            try {
                xmlUtil.addlatchedEntryInLoadBalXML(loadBalanceFile, "AllowDisallow",
                    loadbalancingAgentExp, collector1Host + ":" + momPort, collector2Host + ":"
                        + momPort, "collector1Host:true");

            } catch (Exception e) {
                Assert.assertTrue(false);
            }

            waitForAgentNodes(tomcatAgentExp, collector1Host, Integer.parseInt(c1Port), emLibDir);
            LOGGER.info("Agent now pointed to collector1 host due to latched=true for collector1");

            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            try {
                xmlUtil.addlatchedEntryInLoadBalXML(loadBalanceFile, "AllowDisallow",
                    loadbalancingAgentExp, collector1Host + ":" + momPort, collector2Host + ":"
                        + momPort, "collector2Host:true");

            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            waitForAgentNodes(tomcatAgentExp, collector2Host, Integer.parseInt(c2Port), emLibDir);
            LOGGER.info("Agent now pointed to collector2 host due to latched=true for collector2");

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);

            waitForAgentNodes(tomcatAgentExp, collector1Host, Integer.parseInt(c1Port), emLibDir);
            LOGGER.info("Agent now pointed to collector1 host as collector 2 is shoutdown");

            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(tomcatAgentExp, collector2Host, Integer.parseInt(c2Port), emLibDir);
            LOGGER.info("Agent now pointed to collector2 host as collector 2 is UP");

        } finally {
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

   @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_440694_TT_83185_unexpected_exception_in_agent_log_after_stopping_collector_SSL()
        throws IOException {
        testcaseId = "440694";

        try {

            LOGGER
                .info("############################ Secnario 1 #####################################");

            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSSLPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, collector1Host, Integer.parseInt(momPort), emLibDir);

            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);

            try {

                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    loadbalancingAgentExp, collector2Host + ":" + momPort, "include");

            } catch (Exception e) {
                Assert.assertTrue(false);
            }

            waitForAgentNodes(tomcatAgentExp, collector2Host, Integer.parseInt(c2Port), emLibDir);
            LOGGER
                .info("Agent now pointed to collector2 host due to loadbalancing entry for collector2");

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            checkAgentDisallowedMessage(collector1Host, tomcatAgentLogFile);

            // There is an open defect for this test case #258956
            /*
             * try{
             * checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
             * "[ERROR] [IntroscopeAgent.CommandQueue] Agent reporter thread caught unexpected exception"
             * );
             * Assert.assertTrue(false);
             * }
             * catch(Exception e){
             * 
             * LOGGER.info("The given error message is not found in agent log test is passed");
             * Assert.assertTrue(true);
             * 
             * }
             */
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId + "Secnario1");

            LOGGER
                .info("############################ Secnario 2 #####################################");

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(c2Port), emLibDir);
            LOGGER.info("Agent appears in MOM... Secnario #2");

            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    loadbalancingAgentExp, collector2Host + ":" + momPort, "include");

            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            waitForAgentNodes(tomcatAgentExp, collector2Host, Integer.parseInt(c2Port), emLibDir);
            LOGGER
                .info("Agent now pointed to collector2 host due to loadbalancing entry for collector2 Secnario #2");

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            checkAgentDisallowedMessage(momhost, tomcatAgentLogFile);

            // There is an open defect for this test case #258956
            /*
             * try{
             * checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
             * "[ERROR] [IntroscopeAgent.CommandQueue] Agent reporter thread caught unexpected exception"
             * );
             * Assert.assertTrue(false);
             * }
             * catch(Exception e){
             * 
             * LOGGER.info("The given error message is not found in agent log test is passed");
             * Assert.assertTrue(true);
             * 
             * }
             */

        } finally {
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId + "Secnario2");
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_353465_restart_collector_within_two_rebalancing_periods_SSL()
        throws IOException {
        testcaseId = "353444";

        try {
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSSLPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);

            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    loadbalancingAgentExp, collector2Host + ":" + momPort, "include");

            } catch (Exception e) {
                Assert.assertTrue(false);
            }

            waitForAgentNodes(tomcatAgentExp, collector2Host, Integer.parseInt(c1Port), emLibDir);
            LOGGER.info("Agent now pointed to collector2 host due to update in loadbalancing file");
            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);

            checkAgentDisallowedMessage(momhost, tomcatAgentLogFile);
            startEM(COLLECTOR2_ROLE_ID);
            
            waitForAgentNodes(tomcatAgentExp, collector2Host, Integer.parseInt(c1Port), emLibDir);
            LOGGER.info("Disallowed agent pointed to collector2 as collector is available");

        } finally {
            stopServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }
    
    public void stopEMServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
        stopEM(MOM_ROLE_ID);
        stopEMServiceFlowExecutor(MOM_MACHINE_ID);
        harvestWait(10);
    }

    public void stopAllAgents() {
        stopTomcatAgent(TOMCAT_ROLE_ID);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID);
    }

    public void stopCollector(String COLLECTOR1_ROLE_ID, String COLLECTOR1_MACHINE_ID) {
        LOGGER.info("Stop of collector process Initiated for ::" + COLLECTOR1_ROLE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        LOGGER.info("Stop of collector process completed  for ::" + COLLECTOR1_ROLE_ID);

    }

    public void checkTomcatLog(String msg, String tomcat1AgentLogFile) {
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 20; i++) {
            try {
                isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, tomcat1AgentLogFile, msg);
                flag = true;
                break;
            } catch (Exception e) {
                if (i < 20) {
                    harvestWait(15);
                    continue;
                }
            }
        }
        Assert.assertTrue(flag);
    }

    public void checkAgentAllowedMessage(String emHostName, String logFile) {
        List<String> agentAllowedMessage = new ArrayList<String>();
        agentAllowedMessage.add(" Connected to " + emHostName + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in allowed mode.");

        agentAllowedMessage.add(" Connected to " + emHostName + ".ca.com:" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in allowed mode.");

        verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, logFile, agentAllowedMessage);
    }

    public void checkAgentDisallowedMessage(String emHostName, String logFile) {
        List<String> agentDisallowedMessage = new ArrayList<String>();
        agentDisallowedMessage.add(" Connected to " + emHostName + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode");

        agentDisallowedMessage.add(" Connected to " + emHostName + ".ca.com:" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode");

        verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, logFile, agentDisallowedMessage);
    }

    public void stopServices() {
        stopAllAgents();
        stopEMServices();
        harvestWait(5);
    }

    public void checkAllCollectorToMOMConnectivity() {
        checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
            momhost, momPort, emLibDir);
        checkSpecificCollectorToMOMConnectivity(".*" + collector2Host + ".*", MetricExpression,
            momhost, momPort, emLibDir);
    }
}
