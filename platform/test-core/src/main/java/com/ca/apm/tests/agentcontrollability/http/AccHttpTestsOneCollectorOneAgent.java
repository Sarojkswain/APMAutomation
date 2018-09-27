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

import java.io.BufferedReader;
import java.io.FileReader;
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
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;


public class AccHttpTestsOneCollectorOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpTestsOneCollectorOneAgent.class);
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
    protected final String jBossAgentExpression;
    protected final String c1Port;
    protected final String collector1Host;
    protected final String tomcatHost;
    protected final String clwJarFileLoc;
    protected final String momWebPort;
    protected final String momSecureWebPort;
    protected final String c1WebPort;
    protected final String momPort;
    protected final String tomcatagentProfileFile;
    protected final String emJettyFileMom;
    protected final String emJettyFileC1;
    protected final String emSecureWebPort;
    protected final String col1LibDir;
    public String metric = "Enterprise Manager:Host";
    protected final String configFileMom_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String oldProp;
    protected String newProp;

    public AccHttpTestsOneCollectorOneAgent() {
        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        jBossAgentExpression = "\".*\\|.*\\|JBoss.*\"";
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
        emJettyFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        emJettyFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
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
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);

        enableHTTPOnEM(roleIds);
    }


    /************************************************
     ************* Author: JAMSA07********************
     * **********************************************
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_205009_HTTP_configuration() {
        String testCaseId = "205009";
        try {
            LOGGER.info("verify_ALM_205009_HTTP_and_HTTPS_configuration");
            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                momWebPort);
            startEMCollectors();
            startTomcatAgent(TOMCAT_ROLE_ID);
            try {
                startEM(MOM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
        } finally {
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            stopServices();
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, MOM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_440681_EMJettyConfigVerify() {
        String testCaseId = "440681";
        LOGGER.info("verify_ALM_440681_EMJettyConfigVerify");
        BufferedReader br = null;
        try {
            String sCurrentLine;
            String sPreviousLine = "";
            br = new BufferedReader(new FileReader(emJettyFileMom + "_backup"));
            boolean flag = false;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.trim().contains("<Call name=\"addConnector\">")) {
                    System.out.println(sPreviousLine);
                    if (sPreviousLine.trim().equals("<!--")) flag = true;
                }
                sPreviousLine = sCurrentLine;
            }
            Assert.assertTrue(flag);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353483_AgentAllowDisallowCollector() {
        String testCaseId = "353483";
        try {
            LOGGER.info("verify_ALM_353483_AgentAllowDisallowCollector");

            backupFile(configFileC1, configFileC1 + "_backup", COLLECTOR1_MACHINE_ID);
            replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false", COLLECTOR1_MACHINE_ID,
                configFileC1);
            try {
                startEM(COLLECTOR1_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort,
                collector1Host, c1WebPort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            String msg = "Active = \"false\"";
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, msg);
        } finally {
            stopServices();
            deleteFile(configFileC1, COLLECTOR1_MACHINE_ID);
            moveFile(configFileC1 + "_backup", configFileC1, COLLECTOR1_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
        }
    }

    /************************************************
     ************* Author: TUUJA01 *******************
     * **********************************************
     */

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_353486_Default_ConnectionMode_TRUE_collector() throws Exception {

        try {

            String agentDisallowConnMsg = "";
            List<String> cluster_Collectors = new ArrayList<String>();

            cluster_Collectors.add(clusterEM1Host + collector1Host);
            cluster_Collectors.add(clusterEM1Port + c1Port);
            cluster_Collectors.add(clusterEM1PublicKey);


            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
                momWebPort);
            backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);
            ApmbaseUtil.updateProperties(ApmbaseConstants.agentAllowed, "true", configFileMom);

            // set Agent allowed to false in collector
            replaceProp(defaultEMAgentAllowedProp, defaultEMAgentAllowedPropFalse,
                COLLECTOR1_MACHINE_ID, configFileC1);

            replaceProp("introscope.enterprisemanager.clustering.login",
                "#introscope.enterprisemanager.clustering.login", MOM_MACHINE_ID, configFileMom);

            appendProp(cluster_Collectors, MOM_MACHINE_ID, configFileMom);


            startTomcatAgent(TOMCAT_ROLE_ID);
            startEM(COLLECTOR1_ROLE_ID);
            startEM(MOM_ROLE_ID);

            stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);

            harvestWait(60);
            Assert.assertEquals(
                getNumberOfDisallowedAgents(momhost, Integer.parseInt(momPort), emLibDir), 1);
            LOGGER
                .info("Agent now connected to MOM in Disallowed Mode, as introscope.apm.agentcontrol.agent.allowed set to false in collector");

            startEM(COLLECTOR1_ROLE_ID);

            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);

            LOGGER.info("Now check the assertion.....");
            Assert.assertEquals(
                getNumberOfDisallowedAgents(momhost, Integer.parseInt(momPort), emLibDir), 0);
            LOGGER.info("Agent now connected to MOM in allowed Mode, and assertion is passed");
            try {
                LOGGER.info("Checking for disallowed mode message in agent log");
                agentDisallowConnMsg =
                    " Connected to "
                        + momhost
                        + ":"
                        + momWebPort
                        + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in disallowed mode";
                isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                    agentDisallowConnMsg);
            } catch (Exception e) {
                LOGGER.info("Disallowed message is not found at agent");
                e.printStackTrace();
            }

        }

        catch (Exception e) {
            LOGGER.info("Some exception occured need to be handled");
            e.printStackTrace();
        } finally {
            stopAllAgents();
            stopEMServices();

            replaceProp(defaultEMAgentAllowedPropFalse, defaultEMAgentAllowedProp,
                COLLECTOR1_MACHINE_ID, configFileC1);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353486", TOMCAT_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileMom, MOM_MACHINE_ID);
            renameFile(configFileMom_backup, configFileMom, MOM_MACHINE_ID);

        }


    }

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_353476_Default_ConnectionMode_collector() throws Exception {

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
