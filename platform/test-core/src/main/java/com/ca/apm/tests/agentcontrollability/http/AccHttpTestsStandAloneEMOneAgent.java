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
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;


public class AccHttpTestsStandAloneEMOneAgent extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccHttpTestsStandAloneEMOneAgent.class);
    protected final String emhost;
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
    protected final String emWebPort;
    protected final String emPort;
    protected final String tomcatagentProfileFile;
    protected final String emJettyFileEm;
    protected final String configFileEm_backup;
    protected final String tomcatAgentExp;
    protected String tomcatAgentLogFile;
    protected String oldProp;
    protected String newProp;

    public AccHttpTestsStandAloneEMOneAgent() {
        
        
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort");
        emWebPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emWebPort");


        tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);

        loadBalanceFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        emhost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
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

        enableHTTPOnEM(roleIds);
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_353477_DefaultAgentConnection_False() throws Exception {

        try {

            String agentDisAllowMsg = "";
            String agentAllowMsg = "";
            backupFile(configFileEm, configFileEm_backup, EM_MACHINE_ID);

            ApmbaseUtil.updateProperties(ApmbaseConstants.agentAllowed, "false", configFileEm);
            ApmbaseUtil.updateProperties(ApmbaseConstants.emMode, "StandAlone", configFileEm);

            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, emhost, emPort, emhost,
                emWebPort);

            startTomcatAgent(TOMCAT_ROLE_ID);
            startEM(EM_ROLE_ID);

            harvestWait(60);
            // check AgentLog for Active = "false"
            Assert.assertEquals(getNumberOfDisallowedAgents(emhost, Integer.parseInt(emPort), emLibDir), 1);

            try {
                LOGGER.info("Checking for disallowed mode message in agent log");
                agentDisAllowMsg =
                    " Connected to "
                        + emhost
                        + ":"
                        + emWebPort
                        + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in disallowed mode";
                isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                    agentDisAllowMsg);
            } catch (Exception e) {
                LOGGER.info("Disallowed message is not found at agent");
                e.printStackTrace();
            }

            stopEM(EM_ROLE_ID);

            ApmbaseUtil.updateProperties(ApmbaseConstants.agentAllowed, "true", configFileEm);

            startEM(EM_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, emhost, Integer.parseInt(emPort), emLibDir);

            Assert.assertEquals(getNumberOfDisallowedAgents(emhost, Integer.parseInt(emPort), emLibDir), 0);

            try {
                LOGGER.info("Checking status of Agent connection....");
                agentAllowMsg =
                    " Connected to "
                        + emhost
                        + ":"
                        + emWebPort
                        + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode";
                isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                    agentAllowMsg);
            } catch (Exception e) {
                LOGGER.info("Agent is NOT connnected to EM succesfully using HTTP connection");
                e.printStackTrace();
            }

            LOGGER.info("Test Passed Agent connected in Allowed mode properly");

        } catch (Exception e) {
            LOGGER.info("Some Error Occurred");
            e.printStackTrace();
        }

        finally {
            stopAllAgents();
            stopEMServices();

            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_353477", TOMCAT_MACHINE_ID);
            revertTomcatAgentProfile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            deleteFile(configFileEm, EM_MACHINE_ID);
            renameFile(configFileEm_backup, configFileEm, EM_MACHINE_ID);

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
