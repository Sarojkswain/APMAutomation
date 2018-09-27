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

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;


public class AccSSLTestsThreeCollectorsOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccSSLTestsThreeCollectorsOneAgent.class);
    protected final String momhost;
    protected final String emLibDir;
    protected final String configFileMom;
    protected final String configFileC1;
    protected final String configFileC2;
    protected final String configFileC3;
    protected final String MetricExpression;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String c1Port;
    protected final String c2Port;
    protected final String c3Port;
    protected final String collector1Host;
    protected final String collector2Host;
    protected final String collector3Host;
    protected final String momSecureWebPort;
    protected final String c1WebPort;
    protected final String c2WebPort;
    protected final String c3WebPort;
    protected final String momPort;
    protected final String tomcatagentProfileFile;
    protected final String emSecurePort;
    protected final String configFileMom_backup;
    protected final String tomcatAgentLogFile;
    protected String agentConnectedCollectorHost;
    protected String agentConnectedCollector1Host;
    protected String agentConnectedCollector2Host;
    protected String agentConnectedCollectorRole;
    protected String agentConnectedCollector1Role;
    protected String testcaseId;
    protected final String tomcatAgentExp;

    List<String> collectors_List;
    List<Integer> collector_Port_List;
    List<String> collector_RoleIDs;
    List<String> sslConnectonMsg;

    public AccSSLTestsThreeCollectorsOneAgent() {

        emSecurePort = ApmbaseConstants.emSSLPort;
        MetricExpression = ".*CPU.*";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        c3Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        momSecureWebPort = ApmbaseConstants.emSecureWebPort;
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");
        c2WebPort =
            envProperties.getRolePropertiesById(COLLECTOR2_ROLE_ID).getProperty("emWebPort");
        c3WebPort =
            envProperties.getRolePropertiesById(COLLECTOR3_ROLE_ID).getProperty("emWebPort");

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
        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        tomcatAgentExp = ".*Tomcat.*";

        collectors_List = new ArrayList<String>();
        collector_Port_List = new ArrayList<Integer>();
        collector_RoleIDs = new ArrayList<String>();

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

        updateEMPropertiesForSSL(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        roleIds.clear();

        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForSSL(roleIds);
        copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353427_DirectConnectToCollectorUsingSSL() {
        testcaseId = "353427";
        LOGGER.info("This is to verify_ALM_353427_DirectConnectToClusterUsingSSL");
        try {
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, emSecurePort);

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
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
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
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
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
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
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
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
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
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            list =
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
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
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            list =
                getAgentMetricsForEMHost(tomcatAgentExp, ".*CPU.*", collector1Host,
                    Integer.parseInt(c1Port), emLibDir);
            Assert.assertTrue(list.size()>=3);
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353426_AgentconnectingToMOMUsingSSL() {
        testcaseId = "353426";
        LOGGER.info("This is to verify_ALM_353426_DirectConnectToClusterUsingSSL");

        try {
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecurePort);

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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
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
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            checkAllCollectorToMOMConnectivity();

        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testcaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353457_Self_lookup_MOM_and_collector__SSL() throws IOException {
        testcaseId = "353457";
        try {
            collectors_List.clear();
            collector_Port_List.clear();
            collector_RoleIDs.clear();

            collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
            collector_RoleIDs.add(COLLECTOR3_ROLE_ID);
            collectors_List.add(collector1Host);
            collectors_List.add(collector2Host);
            collectors_List.add(collector3Host);
            collector_Port_List.add(Integer.parseInt(c1Port));
            collector_Port_List.add(Integer.parseInt(c2Port));
            collector_Port_List.add(Integer.parseInt(c3Port));

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecurePort);
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            startTomcatAgent(TOMCAT_ROLE_ID);
            agentConnectedCollectorHost =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            agentConnectedCollectorRole =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("The agent connected collector is " + agentConnectedCollectorHost);

            stopCollectorEM(MOM_ROLE_ID, agentConnectedCollectorRole);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (agentConnectedCollectorHost.trim().equalsIgnoreCase(
                    collectors_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;

                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (agentConnectedCollectorRole.trim().equalsIgnoreCase(
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
            waitForAgentNodes("*.Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
            agentConnectedCollector1Host =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);
            agentConnectedCollector1Role =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Role", emLibDir);

            LOGGER.info("Now the agent is conected to....."+agentConnectedCollector1Host);

            stopCollectorEM(MOM_ROLE_ID, agentConnectedCollector1Role);

            for (int i = 0; i < collectors_List.size(); i++) {
                if (agentConnectedCollector1Host.trim().equalsIgnoreCase(
                    collectors_List.get(i).trim())) {
                    LOGGER.info("Remove the Collector from the List " + collectors_List.get(i));
                    collectors_List.remove(i);
                    break;
                } else
                    LOGGER.info("No Collector is removed this time " + collectors_List.get(i));
            }

            for (int j = 0; j < collector_RoleIDs.size(); j++) {

                if (agentConnectedCollector1Role.trim().equalsIgnoreCase(
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

            // wait till agent got redirected to available collector
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
            agentConnectedCollector2Host =
                getAgentConnectedCollectorName(collectors_List, collector_Port_List,
                    collector_RoleIDs, tomcatAgentExp, "Host", emLibDir);

            Assert.assertEquals(agentConnectedCollector2Host,
                collectors_List.get(collectors_List.size() - 1));
            LOGGER
                .info("The Agent is finally connected to the leftout Collector and Test got passed..."
                    + agentConnectedCollector2Host);
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + testcaseId, TOMCAT_MACHINE_ID);
        }
    }

    //There is an open defect#DE199894 on this test case once its fixed will un-comment this test case
  // @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353466_71630_Agent_does_not_iterate_through_EM_list_when_disallowed_SSL() throws IOException {
        testcaseId = "353466";
        
        List<String> agnetUrl = new ArrayList<String>();
        try {
            
                replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                    AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, COLLECTOR1_MACHINE_ID, configFileC1);
                replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                    AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, COLLECTOR2_MACHINE_ID, configFileC2);
                replaceProp(AgentControllabilityConstants.defaultEMAgentAllowedProp,
                    AgentControllabilityConstants.defaultEMAgentAllowedPropFalse, COLLECTOR3_MACHINE_ID, configFileC3);
            
                setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                        collector1Host, emSecurePort);
                
                agnetUrl.add("agentManager.url.2=ssl://"+collector2Host+":"+emSecurePort);
                agnetUrl.add("agentManager.url.3=ssl://"+collector3Host+":"+emSecurePort);
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

            String msg="Connected controllable Agent to the Introscope Enterprise Manager at "+collector1Host+":"+emSecurePort+",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode";
           checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
            
           msg="Connected controllable Agent to the Introscope Enterprise Manager at "+collector2Host+":"+emSecurePort+",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode";
           checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
           
           msg="Connected controllable Agent to the Introscope Enterprise Manager at "+collector3Host+":"+emSecurePort+",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode";
           checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
           
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
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testcaseId);
        }

    }
    
  @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353437_ACCOn() {
        String testCaseId = "353437";
        try {
            startEMCollectors();
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                emSecurePort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            verifyAllCollectors();
        } finally {
            stopServices();
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
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
                    getConnectedAgentMetricForEMHost(tomcatAgentExp, MetricExpression,
                        collector1Host, Integer.parseInt(c1WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            try {
                count2 =
                    getConnectedAgentMetricForEMHost(tomcatAgentExp, MetricExpression,
                        collector2Host, Integer.parseInt(c2WebPort), emLibDir).size();
            } catch (Exception e) {
                LOGGER.error("Collector Not up");
            }
            try {
                count3 =
                    getConnectedAgentMetricForEMHost(tomcatAgentExp, MetricExpression,
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
        if (i == 20) flag = false;
        Assert.assertTrue(flag);
    }
}
