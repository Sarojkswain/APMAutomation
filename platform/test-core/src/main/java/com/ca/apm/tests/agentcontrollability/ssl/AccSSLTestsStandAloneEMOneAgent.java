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


public class AccSSLTestsStandAloneEMOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AccSSLTestsStandAloneEMOneAgent.class);
    protected final String host;
    protected final String emLibDir;
    protected final String configFileEm;
    protected final String EMlogFile;
    protected final String emSecurePort;
    protected final String emPort;
    protected final String tomcatagentProfileFile;
    protected final String configFileEm_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected final String AgentExpression;

    public AccSSLTestsStandAloneEMOneAgent() {

        tomcatAgentExp = ".*Tomcat.*";
        emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort");
        emSecurePort = ApmbaseConstants.emSSLPort;
        host = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        EMlogFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
        configFileEm =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileEm_backup = configFileEm + "_backup";
        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        AgentExpression = "\".*\\|.*\\|.*\"";
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
        updateEMPropertiesForSSL(roleIds);
        updateEmJettyConfigXmlSecureAttributes(roleIds);

        roleIds.clear();
        roleIds.add(TOMCAT_ROLE_ID);
        updateTomcatPropertiesForSSL(roleIds);
        copyKeyStoreToAgent(EM_ROLE_ID, TOMCAT_ROLE_ID);

    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353431_default_Connection_Prop_StandAlone_SSL() throws IOException {
        LOGGER.info("This is to verify verify_ALM_353431_default_Connection_Prop_StandAlone_SSL");

        try {
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm, defaultEMAgentAllowedProp);
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm, defaultEMlistLookup);
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                defaultEMdisallowedConnLimit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353434_verifyNeedClientAuthentication() {
        String testCaseId = "353434";
        try {
            LOGGER.info("verify_ALM_353434_verifyNeedClientAuthentication");
            backupFile(configFileEm, configFileEm + "_backup", EM_MACHINE_ID);
            try {
                startEM(EM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            replaceProp(defaultEMAgentAllowedProp, defaultEMAgentAllowedPropFalse, EM_MACHINE_ID,
                configFileEm);
            checkLogForMsg(envProperties, EM_MACHINE_ID, EMlogFile, "Detected hot config change to");

            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, host, emPort, host,
                emSecurePort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            harvestWait(30);
            Assert.assertEquals(1,
                getNumberOfDisallowedAgents(host, Integer.parseInt(emPort), emLibDir));
            LOGGER.info("Agent now connected to EM in disallowed mode");

            stopEMServices();

            replaceProp(defaultEMAgentAllowedPropFalse, defaultEMAgentAllowedProp, EM_MACHINE_ID,
                configFileEm);

            try {
                startEM(EM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            waitForAgentNodes(tomcatAgentExp, host, Integer.parseInt(emPort), emLibDir);
            Assert.assertEquals(0,
                getNumberOfDisallowedAgents(host, Integer.parseInt(emPort), emLibDir));
            LOGGER.info("Agent now connected back to EM in allowed mode");

        } finally {
            stopEMServices();
            stopAllAgents();
            deleteFile(configFileEm, EM_MACHINE_ID);
            moveFile(configFileEm + "_backup", configFileEm, EM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, EM_MACHINE_ID, testCaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353436_default_agent_connection_mode_with_default_option_TRUE_SSL() {

        String testCaseId = "353436";
        try {
            LOGGER.info("verify_ALM_353436_default_agent_connection_mode_with_default_option_TRUE_SSL");
            backupFile(configFileEm, configFileEm + "_backup", EM_MACHINE_ID);
            
            replaceProp(defaultEMAgentAllowedProp, "", EM_MACHINE_ID,
                configFileEm);
            try {
                startEM(EM_ROLE_ID);
            } catch (Exception e) {
                LOGGER.error("Already started");
            }
            
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, host, emPort, host,
                emSecurePort);
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, host, Integer.parseInt(emPort), emLibDir);
            LOGGER.info("Agent connected EM even agent allowed property is missing");

        } finally {
            stopEMServices();
            stopAllAgents();
            deleteFile(configFileEm, EM_MACHINE_ID);
            moveFile(configFileEm + "_backup", configFileEm, EM_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, EM_MACHINE_ID, testCaseId);
        }
    }
    

 @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_353435_default_agent_connection_mode_with_TRUE_SSL() {
        try {

            backupFile(configFileEm, configFileEm + "_backup", EM_MACHINE_ID);
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, host, emPort, host,
                ApmbaseConstants.emSSLPort);

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
                    + ApmbaseConstants.emSSLPort
                    + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode");
            disallowedMsgList
                .add(" Connected to "
                    + host
                    + ".ca.com"
                    + ":"
                    + ApmbaseConstants.emSSLPort
                    + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in disallowed mode");

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
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "353435", TOMCAT_MACHINE_ID);

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
