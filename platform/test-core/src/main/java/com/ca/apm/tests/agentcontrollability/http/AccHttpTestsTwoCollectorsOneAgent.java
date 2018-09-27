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
package com.ca.apm.tests.agentcontrollability.http;

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


public class AccHttpTestsTwoCollectorsOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpTestsTwoCollectorsOneAgent.class);
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
    protected final String emJettyFileMom;
    protected final String emJettyFileC1;
    protected final String emJettyFileC2;
    protected final String emSecureWebPort;
    protected final String col1LibDir;
    public String metric = "Enterprise Manager:Host";
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String oldProp;
    protected String newProp;

    public AccHttpTestsTwoCollectorsOneAgent() {
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
        emJettyFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        emJettyFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/em-jetty-config.xml";
        emJettyFileC2 =
            envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/em-jetty-config.xml";
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

        enableHTTPOnEM(roleIds);
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_351130_Shutdown_MOM_When_AgentConnected_To_MOM() throws Exception {

        try {
            List<String> cluster_Collectors = new ArrayList<String>();

            cluster_Collectors.add(clusterEM1Host + collector1Host);
            cluster_Collectors.add(clusterEM1Port + c1Port);
            cluster_Collectors.add(clusterEM1PublicKey);
            cluster_Collectors.add(clusterEM2Host + collector2Host);
            cluster_Collectors.add(clusterEM2Port + c2Port);
            cluster_Collectors.add(clusterEM2PublicKey);

            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                momWebPort);
            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);
            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);
            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);


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
            LOGGER.info("The Agent connected to Collector before MOM Restart is "
                + collectorHostname);
            stopEM(MOM_ROLE_ID);
            // check Agent Connectivity
            String collectorHostname_1 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after MOM Restart is "
                + collectorHostname_1);

            startEM(MOM_ROLE_ID);
            LOGGER.info("MOM Started Succesfully");

            Assert.assertEquals(collectorHostname.trim(), collectorHostname_1.trim());
            LOGGER
                .info("The Agent connection is stable to same collector eventhough mom is restarted");

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Some Exception occured");
        }

        finally {

            stopAllAgents();
            stopEMServices();

            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_351130", TOMCAT_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);


        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351129_ConnectAgent_to_Cluster_in_HTTP() throws Exception {

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

            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                momWebPort);

            startTomcatAgent(TOMCAT_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

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
            checkLogAndErrorMessages(TOMCAT_MACHINE_ID, collectorHostname, tomcatAgentLogFile);
            LOGGER
                .info("Test Passed as HTTP agent connection message found in Agent log for collector "
                    + collectorHostname);

        } catch (Exception e) {
            LOGGER.info("Some Error Occurred");
            e.printStackTrace();
        }

        finally {
            stopAllAgents();
            stopEMServices();

            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_351129", TOMCAT_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);

        }
    }

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_353489_NO_ACC_For_Agent_Direct_Connection_To_Collector()
        throws Exception {
        try {
            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, c1WebPort);
            LOGGER.info("The agent now should get pointed to the collector...." + collector1Host
                + "  On Port" + c1WebPort);


            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            startEM(MOM_ROLE_ID);

            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);
            checkSpecificCollectorToMOMConnectivity(".*" + collector2Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            List<String> collector1ConVeriy = new ArrayList<String>();

            collector1ConVeriy
                .add(" Connected to "
                    + collector1Host
                    + ":"
                    + "8081"
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");

            collector1ConVeriy
                .add(" Connected to "
                    + collector1Host
                    + ".ca.com"
                    + ":"
                    + "8081"
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");

            verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile, collector1ConVeriy);

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            LOGGER.info("Stopped the collector1..");
            LOGGER.info("Verifying next => MOM redirecting to collector 2");
            verifyAllCollectors();
            startEM(COLLECTOR1_ROLE_ID);
            LOGGER.info("stopped Collector Started Succesfully");

            List<String> collector2ConVeriy = new ArrayList<String>();

            collector2ConVeriy
                .add(" Connected to "
                    + collector2Host
                    + ":"
                    + "8081"
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");

            collector2ConVeriy
                .add(" Connected to "
                    + collector2Host
                    + ".ca.com"
                    + ":"
                    + "8081"
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");

            LOGGER
                .info("The agent connection is stable even after stopped collector is restarted...");
        } catch (Exception e) {
            LOGGER.info("Some exception occured");
            e.printStackTrace();
        } finally {
            stopAllAgents();
            stopEMServices();
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353489", TOMCAT_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
        }
    }

    public void checkLogAndErrorMessages(String machineID, String collectorName, String logFile)
        throws Exception {

        try {
            isKeywordInFile(
                envProperties,
                machineID,
                logFile,
                " Connected to "
                    + collectorName
                    + ".ca.com"
                    + ":"
                    + momWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode.");

        } catch (IllegalStateException e) {
            Assert.assertTrue(false);
        }
    }

    public void verifyAllCollectors() {
        int count1 = 0;
        int count2 = 0;
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            try {
                count1 =
                    getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression, collector1Host,
                        Integer.parseInt(c1WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            try {
                count2 =
                    getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression, collector2Host,
                        Integer.parseInt(c2WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            if (count1 >= 3 || count2 >= 3) {
                flag = true;
                break;
            } else
                harvestWait(15);
        }
        if (i == 20) flag = false;
        Assert.assertTrue(flag);
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
    }
}
