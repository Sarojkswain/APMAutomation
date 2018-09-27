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


public class AccHttpsTestsStandAloneEMOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccHttpsTestsStandAloneEMOneAgent.class);
    protected final String host;
    protected final String emLibDir;
    protected final String configFileEm;
    protected final String user;
    protected final String password;
    protected final String AgentExpression;
    protected final String tomcatAgentExpression;
    protected final String MetricExpression;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String tomcatHost;
    protected final String clwJarFileLoc;
    protected final String emSecureWebPort;
    protected final String emPort;
    protected final String tomcatagentProfileFile;
    protected final String emJettyFileEm;
    protected final String configFileEm_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String oldProp;
    protected String newProp;

    public AccHttpsTestsStandAloneEMOneAgent() {


        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort");
        emSecureWebPort = ApmbaseConstants.emSecureWebPort;


        tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);

        loadBalanceFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        host = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);

        EMlogFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);

        configFileEm =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileEm_backup = configFileEm + "_backup";
        emJettyFileEm =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
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

        machines.add(EM_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(EM_ROLE_ID);
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(EM_ROLE_ID);
        updateEMPropertiesForHTTPS(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);
        
        roleIds.clear();
        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForHTTPS(roleIds);
        copyKeyStoreToAgent(EM_ROLE_ID, TOMCAT_ROLE_ID);

    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353249_default_Connection_Prop_StandAlone_HTTPS(){
        LOGGER.info("This is to verify verify_ALM_353249_default_Connection_Prop_StandAlone_HTTPS");

        try {
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm, defaultEMAgentAllowedProp);
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm, defaultEMlistLookup);
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                defaultEMdisallowedConnLimit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353253_default_agent_connection_mode_with_TRUE_HTTPS() {
        try {

            backupFile(configFileEm, configFileEm + "_backup", EM_MACHINE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, host, emPort, host,
                ApmbaseConstants.emSecureWebPort);

            startEM(EM_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

            waitForAgentNodes(AgentExpression, host, Integer.parseInt(emPort), emLibDir);

            LOGGER.info("Agent connected to EM");
            replaceProp(defaultEMAgentAllowedProp, defaultEMAgentAllowedPropFalse, EM_MACHINE_ID,
                configFileEm);

            stopEM(EM_ROLE_ID);
            startEM(EM_ROLE_ID);

            List<String> disallowedMsgList = new ArrayList<String>();

            disallowedMsgList
                .add(" Connected to "
                    + host
                    + ":"
                    + ApmbaseConstants.emSecureWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode");
            disallowedMsgList
                .add(" Connected to "
                    + host
                    + ".ca.com"
                    + ":"
                    + ApmbaseConstants.emSecureWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in disallowed mode");

            verifyIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile, disallowedMsgList);
            Assert.assertEquals(
                getNumberOfDisallowedAgents(host, Integer.parseInt(emPort), emLibDir), 1);

            LOGGER.info("Agent connected to EM in Disallowed Mode...");


        } catch (Exception e) {
            LOGGER.info("Some exception occured");
            e.printStackTrace();
        }

        finally {

            stopAllAgents();
            stopEM(EM_ROLE_ID);

            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileEm, EM_MACHINE_ID);
            renameFile(configFileEm + "_backup", configFileEm, EM_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353489", TOMCAT_MACHINE_ID);

        }

    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353254_remove_default_agent_connection_mode_Property_HTTPS() {
        try {

            backupFile(configFileEm, configFileEm + "_backup", EM_MACHINE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, host, emPort, host,
                ApmbaseConstants.emSecureWebPort);
            replaceProp(defaultEMAgentAllowedProp, "#" + defaultEMAgentAllowedProp, EM_MACHINE_ID,
                configFileEm);

            startEM(EM_ROLE_ID);
            startTomcatAgent(TOMCAT_ROLE_ID);

            waitForAgentNodes(AgentExpression, host, Integer.parseInt(emPort), emLibDir);

            String disallowMsg =
                "Connected to "
                    + host
                    + ":"
                    + ApmbaseConstants.emSecureWebPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode";
            try {
                isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, disallowMsg);
            } catch (Exception e) {
                LOGGER
                    .info("Failed to connect to EM on HTTPS Port check the logs for additional info");
                e.printStackTrace();
            }

            LOGGER
                .info("Agent connected to EM in Allowed mode using https socket factory, TestPassed...");
        }

        finally {

            stopAllAgents();
            stopEM(EM_ROLE_ID);

            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileEm, EM_MACHINE_ID);
            renameFile(configFileEm + "_backup", configFileEm, EM_MACHINE_ID);
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353254", TOMCAT_MACHINE_ID);

        }
    }

    public void stopEMServices() {
        stopEM(EM_ROLE_ID);
        stopEMServiceFlowExecutor(EM_MACHINE_ID);
        harvestWait(10);
    }

    public void stopAllAgents() {
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID);
    }


}
