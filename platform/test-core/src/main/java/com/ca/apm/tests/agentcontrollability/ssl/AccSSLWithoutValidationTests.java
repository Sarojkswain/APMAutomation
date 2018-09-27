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
import com.ca.apm.tests.testbed.AgentControllability3Collectors3TomcatAgentWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.type.SizeType;

public class AccSSLWithoutValidationTests extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccSSLWithoutValidationTests.class);
    protected final String momhost;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String configFileC1;
    protected final String configFileC2;
    protected final String tomcatHost1;
    protected final String tomcatHost2;
    protected final String tomcatHost3;
    protected final String MetricExpression;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String c1Port;
    protected final String c2Port;
    protected final String collector1Host;
    protected final String collector2Host;
    protected final String collector3Host;
    protected final String c1WebPort;
    protected final String c2WebPort;
    protected final String momPort;
    protected final String tomcat1agentProfileFile;
    protected final String tomcat2agentProfileFile;
    protected final String tomcat3agentProfileFile;
    protected final String tomcat1AgentLogFile;
    protected final String tomcat2AgentLogFile;
    protected final String tomcat3AgentLogFile;
    protected final String emSSLPort;
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String agentConnectedCollectorHost;
    protected String agentConnectedCollector1Host;
    protected String agentConnectedCollector2Host;
    protected String testcaseId;
    protected String agentConnectedCollectorRole;

    List<String> collectors_List;
    List<Integer> collector_Port_List;
    List<String> collector_RoleIDs;
    List<String> sslConnectonMsg;
    List<String> cluster_Collectors;
    List<String> tempList;



    public AccSSLWithoutValidationTests() {

        emSSLPort = ApmbaseConstants.emSSLPort;
        tomcatAgentExp = ".*Tomcat.*";
        MetricExpression = ".*CPU.*";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emPort");
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");
        c2WebPort =
            envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emWebPort");

        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
        collector2Host = envProperties.getMachineHostnameByRoleId(COLLECTOR2_ROLE_ID);
        collector3Host = envProperties.getMachineHostnameByRoleId(COLLECTOR3_ROLE_ID);

        tomcatHost1 = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE1_ID);
        tomcatHost2 = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE2_ID);
        tomcatHost3 = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE3_ID);
        
        loadBalanceFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
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
        configFileC2 =
            envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
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

        collectors_List = new ArrayList<String>();
        collector_Port_List = new ArrayList<Integer>();
        collector_RoleIDs = new ArrayList<String>();
        sslConnectonMsg = new ArrayList<String>();
        cluster_Collectors = new ArrayList<String>();
        tempList = new ArrayList<String>();

    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        List<String> machines = new ArrayList<String>();
        machines.add(MOM_MACHINE_ID);
        machines.add(COLLECTOR1_MACHINE_ID);
        machines.add(COLLECTOR2_MACHINE_ID);
        machines.add(COLLECTOR3_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID1);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);

        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(COLLECTOR2_ROLE_ID);
        roleIds.add(COLLECTOR3_ROLE_ID);

        updateEMPropertiesForSSLWithoutValidation(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
    }

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_353451_AgentAllowDisallowCollector() {
        testcaseId = "353451";
        try {
            LOGGER.info("verify_ALM_353451_AgentAllowDisallowCollector");

            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                collector1Host, emSSLPort);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            String msg = "Active = \"false\"";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, msg);
            Assert.assertEquals(
                1,
                getNumberOfDisallowedAgentsAtCollector(collector1Host, Integer.parseInt(c1Port),
                    emLibDir));

        } finally {
            stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID1);
            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
            stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);

            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testcaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353452_AgentAllowDisallow_Redirect_MOM_Collector() {
        testcaseId = "353452";
        try {
            LOGGER.info("verify_ALM_353452_DisallowRedirectMomCollector");
            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);

            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);
            
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSSLPort);

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

            startTomcatAgent(TOMCAT_ROLE1_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            
            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + emSSLPort, "exclude");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String activeFalseMsg = "Active = \"false\"";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, activeFalseMsg);
            checkMetricClampOrCount(".*Custom Metric Process.*","Enterprise Manager\\|Connections:Number of Disallowed Agents", 1);
            

        } finally {
            stopSingleCollectorEMandAgentServices();

            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);

            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353443_OverriddenCollectorSettingsByMom() {
        testcaseId = "353443";
        try {
            LOGGER.info("verify_ALM_353443_OverriddenCollectorSettingsByMom");
            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            backupFile(configFileMom, configFileMom + "_backup", MOM_MACHINE_ID);
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);
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
                momhost, emSSLPort);
            setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort,
                momhost, emSSLPort);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            
            replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, MOM_MACHINE_ID,
                configFileMom);
            checkLogForMsg(envProperties, MOM_MACHINE_ID, EMlogFile,
                "Detected hot config change to");
            
            startTomcatAgent(TOMCAT_ROLE2_ID);
            harvestWait(120);
            
            Assert.assertEquals(1,
                getNumberOfDisallowedAgents(momhost, Integer.parseInt(momPort), emLibDir));

        } finally {
            stopSingleCollectorEMandAgentServices();
            stopTomcatAgent(TOMCAT_ROLE2_ID);
            stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID2);
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            moveFile(configFileMom + "_backup", configFileMom, MOM_MACHINE_ID);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testcaseId);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353430_Bringing_MOM_down_in_cluster_when_agent_is_connected_to_MOM_with_SSL()
        throws IOException {
        testcaseId = "353430";

        collectors_List.clear();
        collector_Port_List.clear();
        collector_RoleIDs.clear();

        collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
        collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
        collectors_List.add(collector1Host);
        collectors_List.add(collector2Host);
        collector_Port_List.add(Integer.parseInt(c1Port));
        collector_Port_List.add(Integer.parseInt(c2Port));

        try {
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                returnIPforGivenHost(momhost), emSSLPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            startEM(COLLECTOR2_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            LOGGER.info("Agent is seen in MOM");

            agentConnectedCollectorHost =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("Agent is Connected to collector host that is..."
                + agentConnectedCollectorHost);

            stopEM(MOM_ROLE_ID);
            LOGGER.info("MOM is stopped succesfully....");

            agentConnectedCollector1Host =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            startEM(MOM_ROLE_ID);

            agentConnectedCollector2Host =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            Assert.assertEquals(agentConnectedCollector1Host, agentConnectedCollector2Host);

            LOGGER.info("Agent connection is stable to collector even after MOM restart..."
                + agentConnectedCollector2Host);

        } finally {
            stopTwoCollectorEMandOneAgentServices();
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameFile(tomcat1AgentLogFile, tomcat1AgentLogFile + testcaseId, TOMCAT_MACHINE_ID1);

        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353470_no_ACC_Agent_Direct_Connection_To_Collector_SSL() {

        try {
            testcaseId = "353470";
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                collector1Host, emSSLPort);

            startEM(MOM_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            startEM(COLLECTOR2_ROLE_ID);

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

            agentConnectedCollectorHost =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            agentConnectedCollectorRole =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The Agent connected to Collector for the First Time is "
                + agentConnectedCollectorHost);

            stopCollectorEM(MOM_ROLE_ID, agentConnectedCollectorRole);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (agentConnectedCollectorHost.trim().equalsIgnoreCase(
                    collectors_List.get(i).trim())) {
                    LOGGER.info("Removed the Collector " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (agentConnectedCollectorRole.trim().equalsIgnoreCase(
                    collector_RoleIDs.get(j).trim())) {

                    LOGGER.info("Removed the Collector Role " + collector_RoleIDs.get(j));
                    collector_RoleIDs.remove(j);
                    break;
                } else
                    LOGGER.info("No Collector ROLE is removed this time "
                        + collector_RoleIDs.get(j));
            }

            LOGGER.info("The leftout collector host is...."
                + collectors_List.get(collectors_List.size() - 1));

            agentConnectedCollector1Host =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            LOGGER.info("The Agent connected to Collector after " + agentConnectedCollectorHost
                + " Restart is " + agentConnectedCollector1Host);

            startEM(agentConnectedCollectorRole);
            LOGGER.info("stopped Collector Started Succesfully");

            agentConnectedCollector2Host =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            agentallowConnectionMessageCheck(agentConnectedCollector2Host, tomcat1AgentLogFile, TOMCAT_MACHINE_ID1);
            
            Assert.assertEquals(agentConnectedCollector1Host.trim(),
                agentConnectedCollector2Host.trim());
            LOGGER.info("Test Passed,even after collector restart Agent pointed to same collector");

        } finally {
            stopTwoCollectorEMandOneAgentServices();
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameFile(tomcat1AgentLogFile, tomcat1AgentLogFile + testcaseId, TOMCAT_MACHINE_ID1);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353468_AgentConnectToLastKnownCollector() {
        testcaseId = "353468";
        LOGGER.info("verify_ALM_353468_AgentConnectToLastKnownCollector");
        try {
            backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort,
                collector1Host, emSSLPort);
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
            startTomcatAgent(TOMCAT_ROLE1_ID);
            waitForAgentNodes(tomcatAgentExp, collector1Host, Integer.parseInt(momPort), emLibDir);

            try {
                xmlUtil.addCollectorsEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector3Host + ":" + emSSLPort, collector2Host
                        + ":" + emSSLPort, "include");
            } catch (Exception e) {
                e.printStackTrace();
            }
           
            sslConnectonMsg.clear();
            //Failed to re-connect to the Introscope Enterprise Manager at tas-itc-n34.ca.com:8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory
            sslConnectonMsg.add(" Failed to re-connect to the Introscope Enterprise Manager at " + collector2Host + ".ca.com" + ":" + emSSLPort
                + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory");
            sslConnectonMsg.add(" Failed to re-connect to the Introscope Enterprise Manager at " + collector2Host + ":" + emSSLPort
                + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory");
            
            sslConnectonMsg.add(" Failed to re-connect to the Introscope Enterprise Manager at " + collector3Host + ".ca.com" + ":" + emSSLPort
                + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory");
            sslConnectonMsg.add(" Failed to re-connect to the Introscope Enterprise Manager at " + collector3Host + ":" + emSSLPort
                + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory");

            verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, sslConnectonMsg);
            

        } finally {
            stopThreeCollectorEMandOneAgentServices();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353429_ConnectAgentToClusterWithSSL() {
        testcaseId = "353429";
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

            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSSLPort);
            startTomcatAgent(TOMCAT_ROLE1_ID);
            agentConnectedCollectorHost =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            Assert.assertTrue(collectors_List.contains(agentConnectedCollectorHost));
            LOGGER.info("Agent connected to available collector " + agentConnectedCollectorHost);

        } finally {
            stopTwoCollectorEMandOneAgentServices();
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353453_AllowDisallowRedirectMomCollector() {
        testcaseId = "353453";
        String msg = "Active = \"true\"";
        backupFile(loadBalanceFile, loadBalanceFile + "_backup", MOM_MACHINE_ID);
        try {
            startEM(COLLECTOR1_ROLE_ID);
            startEM(COLLECTOR2_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentSSLUrl(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1, momhost, momPort, momhost,
                emSSLPort);
            setAgentSSLUrl(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2, momhost, momPort, momhost,
                emSSLPort);
            setAgentSSLUrl(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3, momhost, momPort, momhost,
                emSSLPort);
            startAllAgents();
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID1, tomcat1AgentLogFile, msg);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID2, tomcat2AgentLogFile, msg);
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID3, tomcat3AgentLogFile, msg);
            try {
                xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "AllowDisallow",
                    ".*\\|.*\\|.*Tomcat.*", collector2Host + ":" + emSSLPort, "include");
            } catch (Exception e) {
                e.printStackTrace();
            }

            harvestWait(120);
            List<String> list =
                getConnectedAgentsExpressionToEMHost(collector2Host, Integer.parseInt(c2Port),
                    ".*Tomcat.*", emLibDir);
            if (list.size() < 1) Assert.assertTrue(false);

        } finally {
            stopTwoCollectorEMandOneAgentServices();
            stopAllAgents();
            deleteFile(loadBalanceFile, MOM_MACHINE_ID);
            moveFile(loadBalanceFile + "_backup", loadBalanceFile, MOM_MACHINE_ID);
            revertTomcatAgentProfile(tomcat1agentProfileFile, TOMCAT_MACHINE_ID1);
            revertTomcatAgentProfile(tomcat2agentProfileFile, TOMCAT_MACHINE_ID2);
            revertTomcatAgentProfile(tomcat3agentProfileFile, TOMCAT_MACHINE_ID3);
            renameLogWithTestCaseID(tomcat1AgentLogFile, TOMCAT_MACHINE_ID1, testcaseId);
            renameLogWithTestCaseID(tomcat2AgentLogFile, TOMCAT_MACHINE_ID2, testcaseId);
            renameLogWithTestCaseID(tomcat3AgentLogFile, TOMCAT_MACHINE_ID3, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    public void updateEMPropertiesForSSLWithoutValidation(List<String> roleIds) {

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

    public void stopSingleCollectorEMandAgentServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        stopEM(MOM_ROLE_ID);
        stopEMServiceFlowExecutor(MOM_MACHINE_ID);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID1);
    }

    public void stopTwoCollectorEMandOneAgentServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
        stopEM(MOM_ROLE_ID);
        stopEMServiceFlowExecutor(MOM_MACHINE_ID);
        stopTomcatAgent(TOMCAT_ROLE1_ID);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID1);
    }

    public void stopThreeCollectorEMandOneAgentServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR3_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR3_MACHINE_ID);
        stopEM(MOM_ROLE_ID);
        stopEMServiceFlowExecutor(MOM_MACHINE_ID);
        stopTomcatAgent(TOMCAT_ROLE1_ID);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID1);
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

    public void stopAllAgents() {
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID1);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID2);
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID3);
    }

    public void agentFailedToReConnectionMessageCheck(String hostName,String logFileName,String machineID) {
        
        sslConnectonMsg.clear();
        //Failed to re-connect to the Introscope Enterprise Manager at tas-itc-n34.ca.com:8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory
        sslConnectonMsg.add(" Failed to re-connect to the Introscope Enterprise Manager at " + hostName + ".ca.com" + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory");
        sslConnectonMsg.add(" Failed to re-connect to the Introscope Enterprise Manager at " + hostName + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory");

        verifyIfAtleastOneKeywordIsInLog(machineID, logFileName, sslConnectonMsg);
    }
 
public void agentallowConnectionMessageCheck(String hostName,String logFileName,String machineID) {
        
        sslConnectonMsg.clear();

        sslConnectonMsg.add(" Connected to " + hostName + ".ca.com" + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in allowed mode");
        sslConnectonMsg.add(" Connected to " + hostName + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in allowed mode");

        verifyIfAtleastOneKeywordIsInLog(machineID, logFileName, sslConnectonMsg);
    }

public void checkMetricClampOrCount(String path,String metricName,int count)
{
    boolean flag= false;
    
    for (int k = 0; k < 20; k++) {
        List<String> list =
            getConnectedAgentMetricForEMHost(
                path,
                metricName, momhost,
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
