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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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


public class AccHttpsTestsTwoCollectorsOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpsTestsTwoCollectorsOneAgent.class);
    protected final String momhost;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String configFileC1;
    protected final String configFileC2;
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
    protected final String collector1Host;
    protected final String collector2Host;
    protected final String tomcatHost;
    protected final String clwJarFileLoc;
    protected final String momWebPort;
    protected final String momSecureWebPort;
    protected final String c1WebPort;
    protected final String c2WebPort;
    protected final String momPort;
    protected final String tomcatagentProfileFile;
    protected final String emSecureWebPort;
    protected final String col1LibDir;
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;

    public AccHttpsTestsTwoCollectorsOneAgent() {
        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        jBossAgentExpression = "\".*\\|.*\\|JBoss.*\"";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emPort");
        momSecureWebPort = ApmbaseConstants.emSecureWebPort;
        momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emWebPort");
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");
        c2WebPort =
            envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emWebPort");


        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
        collector2Host = envProperties.getMachineHostnameByRoleId(COLLECTOR2_ROLE_ID);

        tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);

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
        clwJarFileLoc = emLibDir + "CLWorkstation.jar";
        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";

        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
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
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(COLLECTOR2_ROLE_ID);

        updateEMPropertiesForHTTPS(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        roleIds.clear();

        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForHTTPS(roleIds);

        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);


    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_351571_down_Agent_Connected_Collector_on_Cluster_HTTPS() {
        LOGGER
            .info("This is to verify verify_ALM_351571_down_Agent_Connected_Collector_on_Cluster_HTTPS");
        try {
            List<String> cluster_Collectors = new ArrayList<String>();

            cluster_Collectors.add(clusterEM1Host + collector1Host);
            cluster_Collectors.add(clusterEM1Port + c1Port);
            cluster_Collectors.add(clusterEM1PublicKey);
            cluster_Collectors.add(clusterEM2Host + collector2Host);
            cluster_Collectors.add(clusterEM2Port + c2Port);
            cluster_Collectors.add(clusterEM2PublicKey);

            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);
            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);
            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);

            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSecureWebPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);


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

            LOGGER.info("The Agent that connected to Collector for the First Time is "
                + collectorHostname);

            stopCollectorEM(MOM_ROLE_ID, collectorRoleID);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (collectorHostname.trim().equalsIgnoreCase(collectors_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collectors_List.get(i));
                    collectors_List.remove(i);
                    collector_RoleIDs.remove(i);
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

            LOGGER.info("After first collector down, the agent now pointed to ..."
                + collectorHostname_2);

            String agentMessage =
                " Connected to "
                    + collector1Host
                    + ":"
                    + "8444"
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode.";

            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, agentMessage);

            Assert.assertEquals(collectorHostname_1.trim(), collectorHostname_2.trim());
            LOGGER
                .info("The Agent connection is stable to the previous connected collector eventhough shutted down collector is restarted");
        }

        finally {
            stopAllAgents();
            stopEMServices();
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "351571");
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, "351571");
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_351573_Bringing_MOM_down_in_cluster_when_agent_is_connected_to_MOM_with_HTTPS() {
        try {
            try {
                setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                    returnIPforGivenHost(momhost), emSecureWebPort);
            } catch (IOException e) {
                LOGGER.info("Unable to get the IP Address for the given hostname");
                e.printStackTrace();
            }

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            LOGGER.info("Agent is seen in MOM");

            getConnectedAgentMetricForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                Integer.parseInt(momPort), emLibDir);
            LOGGER.info("Agent is Connected to collector1 host that is..." + collector1Host);

            stopEM(MOM_ROLE_ID);
            LOGGER.info("MOM is stopped succesfully....");

            getConnectedAgentMetricForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                Integer.parseInt(momPort), emLibDir);

            startEM(MOM_ROLE_ID);
            getConnectedAgentMetricForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                Integer.parseInt(momPort), emLibDir);
            LOGGER.info("Agent is Connected to collector1 host Even after mom restart..."
                + collector1Host);

        } finally {

            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_351573", TOMCAT_MACHINE_ID);

        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353327_no_ACC_Agent_Direct_Connection_To_Collector_HTTPS() {
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
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSecureWebPort);

            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

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

            checkAgentAllowedMessage(collectorHostname_2, tomcatAgentLogFile);

            Assert.assertEquals(collectorHostname_1.trim(), collectorHostname_2.trim());
            LOGGER
                .info("The Agent connection is stable to the previous connected collector eventhough shutdowned collector is restarted");

        } finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353327", TOMCAT_MACHINE_ID);

        }

    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353259_Self_lookup_MOM_and_connected_collector_down_HTTPS() {
        try {
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
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
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353259", TOMCAT_MACHINE_ID);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353274_Self_lookup_restart_agent_after_selflookup__HTTPS() {
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

            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

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
            stopEM(MOM_ROLE_ID);

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
            String collectorHostname_1 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after " + collectorHostname
                + " Restart is " + collectorHostname_1);
            stopTomcatAgent(TOMCAT_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            String agentConnectionFailureMessage = " Failed to connect to";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                agentConnectionFailureMessage);
            startEM(MOM_ROLE_ID);
            LOGGER.info("stopped Collector Started Succesfully");
            String collectorHostname_2 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER
                .info("After first collector down and MOM is restarted now, the agent now pointed to ..."
                    + collectorHostname_2);
            checkAgentAllowedMessage(collectorHostname_2, tomcatAgentLogFile);
            LOGGER
                .info("After Agent & MOM restart and MOM is Up, Agent connected to the available collectors...that is"
                    + collectorHostname_2);
        } finally {
            stopAllAgents();
            stopEMServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, "353274");
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "353274");
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353282_Proper_Log_Messages() {
        try {
            LOGGER.info("verify_ALM_353282_Proper_Log_Messages");
            List<String> cluster_Collectors = new ArrayList<String>();
            List<String> collectors_List = new ArrayList<String>();
            List<Integer> collector_Port_List = new ArrayList<Integer>();
            List<String> collector_RoleIDs = new ArrayList<String>();

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

            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);

            startTomcatAgent(TOMCAT_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
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
            String collectorHostName =
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
            checkAgentDisallowedMessage(momhost, newtomcat1AgentLogFile);
            LOGGER.info("verify_ALM_353464_Proper_Log_Messages Ended");
        } finally {
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            stopServices();
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "353464");
        }

    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_450353_80077_Agent_Connection_Issues_With_Exception_When_EM_Is_Shutdown_Using_CLW() {
        try {
            LOGGER
                .info("verify_ALM_450353_80077_Agent_Connection_Issues_With_Exception_When_EM_Is_Shutdown_Using_CLW");
            List<String> cluster_Collectors = new ArrayList<String>();

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
            try {
                setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                    returnIPforGivenHost(collector1Host), emSecureWebPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            Assert.assertTrue(verifyColTomcatAgent(collector1Host, c1Port));
            stopCollector(COLLECTOR1_ROLE_ID, COLLECTOR1_MACHINE_ID);
            harvestWait(120);
            Assert.assertTrue(verifyColTomcatAgent(collector2Host, c2Port));
            startCollector(COLLECTOR1_ROLE_ID, COLLECTOR1_MACHINE_ID);
            Assert.assertTrue(verifyColTomcatAgent(collector2Host, c2Port));
            LOGGER
                .info("verify_ALM_450353_80077_Agent_Connection_Issues_With_Exception_When_EM_Is_Shutdown_Using_CLW Ended");
        } finally {
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            stopServices();
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "450353");
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353262_Test_latch_functionality_in_loadbalancing_xml_file_Https() throws IOException {
       String  testcaseId = "353262";
        
        try {
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);
            
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                momhost, emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            
            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
            
            try {
                xmlUtil.addlatchedEntryInLoadBalXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + momPort, collector2Host + ":"
                        + momPort, "collector1Host:true");
                
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            
            waitForAgentNodes(".*Tomcat.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            LOGGER.info("Agent now pointed to collector1 host due to latched=true for collector1");
            
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            backupFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            try {
                xmlUtil.addlatchedEntryInLoadBalXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + momPort, collector2Host + ":"
                        + momPort, "collector2Host:true");
                
            } catch (Exception e) {
                Assert.assertTrue(false);
            }
            waitForAgentNodes(".*Tomcat.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
            LOGGER.info("Agent now pointed to collector2 host due to latched=true for collector2");
            
            stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
            
            waitForAgentNodes(".*Tomcat.*", collector1Host, Integer.parseInt(c1Port), emLibDir);
            LOGGER.info("Agent now pointed to collector1 host as collector 2 is shoutdown");
            
            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", collector2Host, Integer.parseInt(c2Port), emLibDir);
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
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID);
    }

    public void stopServices() {
        stopAllAgents();
        stopEMServices();
        harvestWait(5);
    }

    public void restartCollector(String COLLECTOR1_ROLE_ID, String COLLECTOR1_MACHINE_ID) {
        LOGGER.info("Restart of collector process Initiated for ::" + COLLECTOR1_ROLE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        LOGGER.info("Stop of collector process completed  for ::" + COLLECTOR1_ROLE_ID);
        try {
            startEM(COLLECTOR1_ROLE_ID);
        } catch (Exception e) {
            LOGGER.info("Issue with restart of collector process  for ::" + COLLECTOR1_ROLE_ID);
            e.printStackTrace();
        }
        harvestWait(60);
        LOGGER.info("Restart of collector process completed sucessfully  for ::"
            + COLLECTOR1_ROLE_ID);
    }

    public void stopCollector(String COLLECTOR1_ROLE_ID, String COLLECTOR1_MACHINE_ID) {
        LOGGER.info("Stop of collector process Initiated for ::" + COLLECTOR1_ROLE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        LOGGER.info("Stop of collector process completed  for ::" + COLLECTOR1_ROLE_ID);

    }

    public void startCollector(String COLLECTOR1_ROLE_ID, String COLLECTOR1_MACHINE_ID) {
        LOGGER.info("Start of collector process Initiated for ::" + COLLECTOR1_ROLE_ID);
        try {
            startEM(COLLECTOR1_ROLE_ID);
        } catch (Exception e) {
            LOGGER.info("Issue with restart of collector process  for ::" + COLLECTOR1_ROLE_ID);
            e.printStackTrace();
        }
        LOGGER
            .info("Start of collector process completed sucessfully  for ::" + COLLECTOR1_ROLE_ID);
    }

    protected boolean verifyColTomcatAgent(String collectorHost, String cPort) {
        List<String> list =
            getAgentMetricsForEMHost(".*Tomcat.*", MetricExpression, collectorHost,
                Integer.parseInt(cPort), emLibDir);
        Iterator<String> i = list.iterator();
        boolean found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        return found;
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

        verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, logFile, agentAllowedMessage);
    }

    public void checkAgentDisallowedMessage(String emHostName, String logFile) {
        List<String> agentDisallowedMessage = new ArrayList<String>();
        agentDisallowedMessage
            .add(" Connected to "
                + emHostName
                + ":"
                + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode");

        agentDisallowedMessage
            .add(" Connected to "
                + emHostName
                + ".ca.com:"
                + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode");

        verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, logFile, agentDisallowedMessage);
    }

}
