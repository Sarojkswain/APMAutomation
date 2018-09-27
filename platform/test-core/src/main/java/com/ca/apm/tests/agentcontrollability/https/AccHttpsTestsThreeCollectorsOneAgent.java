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


public class AccHttpsTestsThreeCollectorsOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpsTestsThreeCollectorsOneAgent.class);
    protected final String momhost;
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
    protected final String tomcatHost;
    protected final String clwJarFileLoc;
    protected final String momWebPort;
    protected final String momSecureWebPort;
    protected final String c1WebPort;
    protected final String c2WebPort;
    protected final String c3WebPort;
    protected final String momPort;
    protected final String tomcatagentProfileFile;
    protected final String emSecureWebPort;
    protected final String col1LibDir;
    public String metric = "Enterprise Manager:Host";
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String oldProp;
    protected String newProp;

    public AccHttpsTestsThreeCollectorsOneAgent() {

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
        configFileC3 =
            envProperties.getRolePropertyById(COLLECTOR3_ROLE_ID,
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
        machines.add(COLLECTOR3_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
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

        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForHTTPS(roleIds);

        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353286_AgentConnectToLastKnownCollector() {
        String testCaseId = "353286";
        LOGGER.info("verify_ALM_353286_AgentConnectToLastKnownCollector");
        try {
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSecureWebPort);
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
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            List<String> list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            try {
                xmlUtil.addCollectorsEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector3Host + ":" + emSecureWebPort, collector2Host
                        + ":" + emSecureWebPort, "include");
            } catch (Exception e) {
                e.printStackTrace();
            }

            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile, "Reject " + collector1Host);

            String msg =
                "Lost contact with the Introscope Enterprise Manager at " + collector1Host + ":"
                    + emSecureWebPort;
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
            msg="Active = \"false\"";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
            List<String> hostnames = new ArrayList<String>();
            hostnames.add(collector2Host);
            hostnames.add(collector3Host);
            checkAgentFailedToReConnect(hostnames, tomcatAgentLogFile, TOMCAT_MACHINE_ID);
            
            msg="Failed to re-connect to the Introscope Enterprise Manager at";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351570_DirectConnectToCollectorUsingHTTPS() {
        String testCaseId = "351570";
        LOGGER.info("This is to verify verify_ALM_351570_DirectConnectToClusterUsingHTTP");
        try {
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSecureWebPort);

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
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startTomcatAgent(TOMCAT_ROLE_ID);
            List<String> list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            try {
                startEM(COLLECTOR3_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            list.clear();
            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            stopServices();

            /**
             * Scenario 2
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 2 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
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
            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startTomcatAgent(TOMCAT_ROLE_ID);
            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            stopServices();

            /**
             * Scenario 3
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 3 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startTomcatAgent(TOMCAT_ROLE_ID);
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
            try {
                startEM(COLLECTOR2_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }

            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            stopServices();

            /**
             * Scenario 4
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 4 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startTomcatAgent(TOMCAT_ROLE_ID);
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
            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            stopServices();

            /**
             * Scenario 5
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 5 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
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
            harvestWait(5);
            startTomcatAgent(TOMCAT_ROLE_ID);
            harvestWait(15);
            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            list =
                getAgentMetricsForEMHost(".*Tomcat.*", ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351569_AgentconnectingToMOMUsingHTTPS() {
        String testCaseId = "351569";
        LOGGER.info("This is to verify verify_ALM_351569_DirectConnectToClusterUsingHTTP");

        try {

            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);

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
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startTomcatAgent(TOMCAT_ROLE_ID);
            startEMCollectors();
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();
            stopServices();

            /**
             * Scenario 2
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 2 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startEMCollectors();
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();
            stopServices();

            /**
             * Scenario 3
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 3 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startEMCollectors();
            checkAllCollectorToMOMConnectivity();
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            stopServices();

            /**
             * Scenario 4
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 4 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startTomcatAgent(TOMCAT_ROLE_ID);
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();
            stopServices();

            /**
             * Scenario 5
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 5 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startEMCollectors();
            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();
            stopServices();
            /**
             * Scenario 6
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 6 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startTomcatAgent(TOMCAT_ROLE_ID);
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();
            stopServices();

            /**
             * Scenario 7
             */
            LOGGER
                .info("**************************************************************************************");
            LOGGER
                .info("**********************************SCENARIO 7 STARTED**********************************");
            LOGGER
                .info("**************************************************************************************");
            startEMCollectors();
            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();

        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353256_MomAndCollectorOn() {
        String testCaseId = "353256";
        try {
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            verifyAllCollectors();
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351572_ConnectAgentToClusterWithHTTPS() {
        String testCaseId = "351572";
        try {
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            verifyAllCollectors();
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353325_NoACCGlobalAllow() {
        String testCaseId = "353325";
        try {
            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);
            backupFile(configFileC2, configFileC2 + "_backup", COLLECTOR2_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR2_MACHINE_ID,
                configFileC2);
            backupFile(configFileC3, configFileC3 + "_backup", COLLECTOR3_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR3_MACHINE_ID,
                configFileC3);
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            checkAllCollectorToMOMConnectivity();
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            stopEMServices();
            startEM(COLLECTOR1_ROLE_ID);

            List<String> agentDisallowedMessage = new ArrayList<String>();
            agentDisallowedMessage
                .add("Connected controllable Agent to the Introscope Enterprise Manager at "
                    + collector1Host
                    + ":8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory.");
            agentDisallowedMessage
                .add("Connected controllable Agent to the Introscope Enterprise Manager at "
                    + collector1Host
                    + ".ca.com:8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory.");
            verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                agentDisallowedMessage);
        } finally {
            stopServices();
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            deleteFile(configFileC2, COLLECTOR2_MACHINE_ID);
            moveFile(configFileC2 + "_backup", configFileC2, COLLECTOR2_MACHINE_ID);
            deleteFile(configFileC3, COLLECTOR3_MACHINE_ID);
            moveFile(configFileC3 + "_backup", configFileC3, COLLECTOR3_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353255_ACCOn() {
        String testCaseId = "353255";
        try {
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            verifyAllCollectors();
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353275_Self_lookup_MOM_and_collector__HTTPS() {
        try {

            String agentConnectedCollector_Host_1 = "";
            String agentConnectedCollector_Host_2 = "";
            String agentConnectedCollector_Host_3 = "";

            String agentConnectedCollector_RoleID_1 = "";
            String agentConnectedCollector_RoleID_2 = "";

            List<String> collectors_List = new ArrayList<String>();
            List<Integer> collector_Port_List = new ArrayList<Integer>();
            List<String> collector_RoleIDs = new ArrayList<String>();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR3_ROLE_ID);

            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collectors_List.add(collector3Host);

            collector_Port_List.add(5001);
            collector_Port_List.add(5001);
            collector_Port_List.add(5001);

            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecureWebPort);
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startTomcatAgent(TOMCAT_ROLE_ID);
            agentConnectedCollector_Host_1 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, ".*Tomcat.*", "Host", emLibDir);
            agentConnectedCollector_RoleID_1 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, ".*Tomcat.*", "Role", emLibDir);

            LOGGER.info("The agent connected collector is " + agentConnectedCollector_Host_1);
            stopCollectorEM(MOM_ROLE_ID, agentConnectedCollector_RoleID_1);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (agentConnectedCollector_Host_1.trim().equalsIgnoreCase(
                    collectors_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;

                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (agentConnectedCollector_RoleID_1.trim().equalsIgnoreCase(
                    collector_RoleIDs.get(j).trim())) {

                    LOGGER.info("Remove the Collector ROLE from the List "
                        + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(j));
            }

            LOGGER.info("The count of collectors after First Collector Removal "
                + collectors_List.size());

            // wait till agent got redirected to available collector
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            agentConnectedCollector_Host_2 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, ".*Tomcat.*", "Host", emLibDir);
            agentConnectedCollector_RoleID_2 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, ".*Tomcat.*", "Role", emLibDir);

            try {
                LOGGER.info("Checking for agent connection message in agent log");
                checkAgentAllowedMessage(agentConnectedCollector_Host_2, tomcatAgentLogFile);
            } catch (Exception e) {
                LOGGER.info("Allowed message is not found at agent");
                e.printStackTrace();
            }

            stopCollectorEM(MOM_ROLE_ID, agentConnectedCollector_RoleID_2);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (agentConnectedCollector_Host_2.trim().equalsIgnoreCase(
                    collectors_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collectors_List.get(i));
                    collectors_List.remove(i);
                    collector_RoleIDs.remove(i); // check here once
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (agentConnectedCollector_RoleID_2.trim().equalsIgnoreCase(
                    collector_RoleIDs.get(j).trim())) {

                    LOGGER.info("Remove the Collector ROLE from the List "
                        + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collector_RoleIDs.get(j));
            }

            LOGGER.info("The count of collectors after Second Collector Removal "
                + collectors_List.size());
            waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            agentConnectedCollector_Host_3 =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, ".*Tomcat.*", "Host", emLibDir);

            try {
                LOGGER.info("Checking for agent connection message in agent log");
                checkAgentAllowedMessage(agentConnectedCollector_Host_3, tomcatAgentLogFile);

            } catch (Exception e) {
                LOGGER.info("Allowed message is not found at agent");
                e.printStackTrace();
            }

            Assert.assertEquals(agentConnectedCollector_Host_3,
                collectors_List.get(collectors_List.size() - 1));
            LOGGER
                .info("The Agent is finally connected to the leftout Collector and Test got passed..."
                    + agentConnectedCollector_Host_3);
        } catch (Exception e) {
            LOGGER.info("Some exception occured need to be handled");
            e.printStackTrace();
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353275", TOMCAT_MACHINE_ID);
        }
    }
    
    //#DE199894 will add this test after defect fix
    // @Test(groups = {"BAT"}, enabled = true)
     public void verify_ALM_353284_71630_Agent_Does_Not_Iterate_Through_EM_List_When_Disallowed() {
         String testCaseId = "353284";   
         List<String> agnetUrl = new ArrayList<String>();
         try {
             
                 replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                     AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, COLLECTOR1_MACHINE_ID, configFileC1);
                 replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                     AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, COLLECTOR2_MACHINE_ID, configFileC2);
                 replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                     AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, COLLECTOR3_MACHINE_ID, configFileC3);
             
                 setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                         collector1Host, emSecureWebPort);
                 
                 agnetUrl.add("agentManager.url.2=https://"+collector2Host+":"+emSecureWebPort);
                 agnetUrl.add("agentManager.url.3=https://"+collector3Host+":"+emSecureWebPort);
                 appendProp(agnetUrl, TOMCAT_MACHINE_ID, tomcatagentProfileFile);
                             
                 startEM(COLLECTOR1_ROLE_ID);
                 startEM(COLLECTOR2_ROLE_ID);
                 startEM(COLLECTOR3_ROLE_ID);
                 startTomcatAgent(TOMCAT_ROLE_ID);
             
                 LOGGER
                 .info("******************************#353466#************************************************");
             LOGGER
                 .info("*********************All Prerequsite Set and EM and Agnet started*********************");
             LOGGER
                 .info("**************************************************************************************");
             
             String msg="Connected controllable Agent to the Introscope Enterprise Manager at "+collector1Host+":"+emSecureWebPort+",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory";
             checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcatAgentLogFile, msg);
              msg="Connected controllable Agent to the Introscope Enterprise Manager at "+collector2Host+":"+emSecureWebPort+",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory";
             checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcatAgentLogFile, msg);
              msg="Connected controllable Agent to the Introscope Enterprise Manager at "+collector3Host+":"+emSecureWebPort+",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory";
             checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcatAgentLogFile, msg);
       
            stopEMServices();
            stopTomcatAgent(TOMCAT_ROLE_ID);
            
          } finally {
              
              replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                  AgentControllabilityConstants.defaultEMAgentAllowedProp, COLLECTOR1_MACHINE_ID, configFileC1);
              replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                  AgentControllabilityConstants.defaultEMAgentAllowedProp, COLLECTOR2_MACHINE_ID, configFileC2);
              replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                  AgentControllabilityConstants.defaultEMAgentAllowedProp, COLLECTOR3_MACHINE_ID, configFileC3);
              
             revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
             renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
         }
     }
     
    public void checkLogAndErrorMessages(String machineID, String collectorName, String logFile) {

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
        } catch (Exception e) {}
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
        try {
            startEM(COLLECTOR3_ROLE_ID);
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

    public void verifyAllCollectors() {

        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int i = 0;
        boolean flag = false;
        for (i = 0; i < 80; i++) {
            try {
                count1 =
                    getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression,
                        collector1Host, Integer.parseInt(c1WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            try {
                count2 =
                    getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression,
                        collector2Host, Integer.parseInt(c2WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            try {
                count3 =
                    getConnectedAgentMetricForEMHost(".*Tomcat.*", MetricExpression,
                        collector3Host, Integer.parseInt(c3WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            if (count1 >= 3 || count2 >= 3 || count3 >= 3) {
                flag = true;
                break;
            } else
                harvestWait(15);
        }
        if (i == 80) flag = false;
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

    /**
     * Verify failed to reconnect messages in an agent with multiple em hostnames
     * 
     * @param hostNames
     * @param logFileName
     * @param machineID
     */
    public void checkAgentFailedToReConnect(List<String> hostNames,String logFileName,String machineID) {
        
        List<String> httpsReconnectString = new ArrayList<String>();
        for(String hostName:hostNames)
        {
        //Failed to re-connect to the Introscope Enterprise Manager at tas-itc-n34.ca.com:8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory
        httpsReconnectString.add(" Failed to re-connect to the Introscope Enterprise Manager at " + hostName + ".ca.com" + ":" + emSecureWebPort
            + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory");
        httpsReconnectString.add(" Failed to re-connect to the Introscope Enterprise Manager at " + hostName + ":" + emSecureWebPort
            + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory");
        }
        verifyIfAtleastOneKeywordIsInLog(machineID, logFileName, httpsReconnectString);
    }
}
