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


public class AccHttpsTestsOneCollectorOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpsTestsOneCollectorOneAgent.class);
    protected final String momhost;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String configFileC1;
    protected final String user;
    protected final String password;
    protected final String AgentExpression;
    protected final String tomcatAgentExpression;
    protected final String MetricExpression;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String c1Port;
    protected final String collector1Host;
    protected final String tomcatHost;
    protected final String clwJarFileLoc;
    protected final String momWebPort;
    protected final String momSecureWebPort;
    protected final String c1WebPort;
    protected final String momPort;
    protected final String tomcatagentProfileFile;
    protected final String emSecureWebPort;
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;

    public AccHttpsTestsOneCollectorOneAgent() {
        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        momSecureWebPort = ApmbaseConstants.emSecureWebPort;
        momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emWebPort");
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");


        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);

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

        EMlogFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);

        configFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileMom_backup = configFileMom + "_backup";
        configFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
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
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);

        updateEMPropertiesForHTTPS(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        roleIds.clear();

        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForHTTPS(roleIds);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353278_default_agent_connection_mode_with_TRUE_Cluster_Collector_mode_HTTPS() {

        try {
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            replaceProp(defaultEMAgentAllowedProp, defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            if (getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                Integer.parseInt(momPort), emLibDir).size() < 3) Assert.assertTrue(false);
            LOGGER.info("Agent connected to Collector");


            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            Assert.assertEquals(
                getNumberOfDisallowedAgents(momhost, Integer.parseInt(momPort), emLibDir), 1);
            LOGGER.info("Agent connected to MOM in Disallowed Mode...");

            startEM(COLLECTOR1_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            LOGGER.info("Agent connected to MOM in in allowed mode after collector restart...");

        } catch (Exception e) {
            LOGGER.info("Some exception occured");
            e.printStackTrace();
        }

        finally {
            stopServices();
            replaceProp(defaultEMAgentAllowedPropFalse, defaultEMAgentAllowedProp,
                COLLECTOR1_MACHINE_ID, configFileC1);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353278", TOMCAT_MACHINE_ID);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353277_default_agent_connection_mode_with_TRUE_Cluster_MOM_mode__HTTPS() {
        try {
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            replaceProp(defaultEMAgentAllowedProp, defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            if (getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                Integer.parseInt(momPort), emLibDir).size() < 3) Assert.assertTrue(false);
            LOGGER.info("Agent connected to Collector");

            replaceProp(defaultEMAgentAllowedProp, defaultEMAgentAllowedPropFalse, MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Hot config property introscope.apm.agentcontrol.agent.allowed changed from true to false");
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            LOGGER
                .info("Even after making agent allowed property to FALSE in MOM, NO agent got disconnected.. TestPassed");
            replaceProp(defaultEMAgentAllowedPropFalse, defaultEMAgentAllowedProp, MOM_MACHINE_ID,
                configFileMom);
        }finally {
            stopServices();
            replaceProp(defaultEMAgentAllowedPropFalse, defaultEMAgentAllowedProp,
                COLLECTOR1_MACHINE_ID, configFileC1);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353277", TOMCAT_MACHINE_ID);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353261_OverriddenCollectorSettingsByMom() {
        String testCaseId = "353261";
        try {
            LOGGER.info("verify_ALM_353261_OverriddenCollectorSettingsByMom");
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
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopServices();
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353251_default_Connection_Prop_Collector_HTTPS(){
        LOGGER.info("This is to verify verify_ALM_353251_default_Connection_Prop_Collector_HTTPS");

        try {
            isKeywordInFile(envProperties, COLLECTOR1_MACHINE_ID, configFileC1,
                defaultEMAgentAllowedProp);
            isKeywordInFile(envProperties, COLLECTOR1_MACHINE_ID, configFileC1, defaultEMlistLookup);
            isKeywordInFile(envProperties, COLLECTOR1_MACHINE_ID, configFileC1,
                defaultEMdisallowedConnLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353250_default_Connection_Prop_MOM_HTTPS(){
        LOGGER.info("This is to verify verify_ALM_353250_default_Connection_Prop_MOM_HTTPS");

        try {
            isKeywordInFile(envProperties, MOM_MACHINE_ID, configFileMom, defaultEMAgentAllowedProp);
            isKeywordInFile(envProperties, MOM_MACHINE_ID, configFileMom, defaultEMlistLookup);
            isKeywordInFile(envProperties, MOM_MACHINE_ID, configFileMom,
                defaultEMdisallowedConnLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_353268_Agent_ALLOW_DISALLOW_On_Collector_With_Certificates() {
        try {
            LOGGER.info("verify_ALM_353268_Agent_ALLOW_DISALLOW_On_Collector_With_Certificates");
            List<String> cluster_Collectors = new ArrayList<String>();
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.host="
                + collector1Host);
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.port="
                + "5001");
            cluster_Collectors
                .add("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public");
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);

            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);
            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);
            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);
            // set Agent allowed to false in collector
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);

            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            harvestWait(60);

            LOGGER.info("Agent now connected to MOM in allowed Mode, and assertion is passed");
            String logMsg =
                " Connected to "
                    + momhost
                    + ":"
                    + emSecureWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode";
            LOGGER.info("Checking for disallowed mode message in agent log:::" + logMsg);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, logMsg);

            LOGGER
                .info("verify_ALM_353268_Agent_ALLOW_DISALLOW_On_Collector_With_Certificates Ended");
        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
                "introscope.apm.agentcontrol.agent.allowed=true", COLLECTOR1_MACHINE_ID,
                configFileC1);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "353268");
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353266_Agent_ALLOW_DISALLOW_On_Collector_With_Hostname() {
        try {
            LOGGER.info("verify_ALM_353266_Agent_ALLOW_DISALLOW_On_Collector_With_Hostname");
            List<String> cluster_Collectors = new ArrayList<String>();

            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.host="
                + collector1Host);
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.port="
                + "5001");
            cluster_Collectors
                .add("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public");
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost, emSecureWebPort);


            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);

            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);

            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);

            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);

            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
            momhost, momPort, emLibDir);

            // set Agent allowed to false in collector
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);

            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            harvestWait(60);

            LOGGER.info("Agent now connected to MOM in allowed Mode, and assertion is passed");
            String logMsg =
                " Connected to "
                    + momhost
                    + ":"
                    + emSecureWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode";
            LOGGER.info("Checking for disallowed mode message in agent log:::" + logMsg);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,logMsg);

            LOGGER.info("verify_ALM_353266_Agent_ALLOW_DISALLOW_On_Collector_With_Hostname Ended");
        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
                "introscope.apm.agentcontrol.agent.allowed=true", COLLECTOR1_MACHINE_ID,
                configFileC1);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "353266");
        }
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_353267_Agent_ALLOW_DISALLOW_On_Collector_With_Client_Authentication() {
        try {
            LOGGER
                .info("verify_ALM_353267_Agent_ALLOW_DISALLOW_On_Collector_With_Client_Authentication");
            List<String> cluster_Collectors = new ArrayList<String>();
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.host="
                + collector1Host);
            cluster_Collectors.add("introscope.enterprisemanager.clustering.login.em1.port="
                + "5001");
            cluster_Collectors
                .add("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public");
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,momhost, emSecureWebPort);
            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);

            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);
            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);

            // set Agent allowed to false in collector
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);
            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);

            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            harvestWait(60);

            LOGGER.info("Agent now connected to MOM in allowed Mode, and assertion is passed");
            String logMsg =
                " Connected to "
                    + momhost
                    + ":"
                    + emSecureWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode";
            LOGGER.info("Checking for disallowed mode message in agent log:::" + logMsg);

            LOGGER
                .info("verify_ALM_353267_Agent_ALLOW_DISALLOW_On_Collector_With_Client_Authentication Ended");
        } finally {
            stopServices();
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
                "introscope.apm.agentcontrol.agent.allowed=true", COLLECTOR1_MACHINE_ID,
                configFileC1);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, "353267");
        }
    }

    public void stopEMServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
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
    }
}
