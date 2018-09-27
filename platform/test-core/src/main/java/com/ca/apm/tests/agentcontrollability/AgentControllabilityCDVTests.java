/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * Author : KETSW01
 */
package com.ca.apm.tests.agentcontrollability;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.flow.webapp.jboss.DeployJbossFlowContext;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.cdv.CDVConstants;

public class AgentControllabilityCDVTests extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AgentControllabilityCDVTests.class);
    TestUtils utility = new TestUtils();
    CLWCommons clw = new CLWCommons();
    EmUtils emUtils = utilities.createEmUtils();
    XMLUtil xmlutil = new XMLUtil();

    private final String cdvRoleId;
    private final String cdvMachineId;
    private final String mom1MachineId;
    private final String mom2MachineId;
    private final String mom1Col1MachineId;
    private final String mom1Col2MachineId;
    private final String mom2Col1MachineId;
    private final String mom2Col2MachineId;
    private final int cdvPort;
    private final int cdvWebPort;
    private final String cdvHost;
    private final String mom1RoleId;
    private final String mom1Col1RoleId;
    private final String mom1Col2RoleId;
    private final String mom1Host;
    private final String mom1Col1Host;
    private final String mom1Col2Host;
    private final int mom1Port;
    private final int mom1Col1Port;
    private final int mom1Col2Port;
    private final int mom1WebPort;
    private final int mom1Col1WebPort;
    private final int mom1Col2WebPort;
    private final String mom2RoleId;
    private final String mom2Col1RoleId;
    private final String mom2Col2RoleId;
    private final String mom2Host;
    private final String mom2Col1Host;
    private final String mom2Col2Host;
    private final int mom2Port;
    private final int mom2Col1Port;
    private final int mom2Col2Port;
    private final int mom2WebPort;
    private final int mom2Col1WebPort;
    private final int mom2Col2WebPort;

    protected String tomcatMachineId;
    protected String jbossMachineId;
    protected String tomcatRoleId;
    protected String jbossRoleId;
    private final String agentTomcatID;
    private final String agentHost;
    private final String agentPort;

    private final String user;
    private final String password;

    private final String cdvLibDir;
    private final String cdvConfigDir;
    private final String cdvConfigFile;
    private final String cdvConfigFile_copy;
    private final String cdvLogFile;
    private final String mom1LibDir;
    private final String mom1Configdir;
    private final String mom1ConfigFile;
    private final String mom1ConfigFile_copy;
    private final String mom1LogFile;
    private final String mom1Col1ConfigFile;
    private final String mom1Col1ConfigFile_copy;
    private final String mom1Col2ConfigFile;
    private final String mom1Col2ConfigFile_copy;
    private final String mom1Col1LogFile;
    private final String mom1Col2LogFile;
    private final String mom1LoadBalanceFile;
    private final String mom1LoadBalanceFile_copy;
    private final String mom1Col1emJettyConfigFile;
    private final String mom1Col2emJettyConfigFile;
    private final String mom2Col1emJettyConfigFile;
    private final String mom2Col2emJettyConfigFile;
    private final String mom2LibDir;
    private final String mom2Configdir;
    private final String mom2ConfigFile;
    private final String mom2ConfigFile_copy;
    private final String mom2LogFile;
    private final String mom2Col1ConfigFile;
    private final String mom2Col1ConfigFile_copy;
    private final String mom2Col2ConfigFile;
    private final String mom2Col2ConfigFile_copy;
    private final String mom2Col1LogFile;
    private final String mom2Col2LogFile;
    private final String mom2LoadBalanceFile;
    private final String mom2LoadBalanceFile_copy;
    private final String tomcatAgentProfile;
    private final String tomcatAgentProfile_copy;
    public String jbossInstallDir;
    private final String jbossAgentProfile;
    private final String jbossAgentProfile_copy;
    private final String jbossLogFile;
    private final String tomcatLogFile;
    private final String tomcatAgentExpression;
    private final String agent_collector_name;
    private final String xpathToHttpsPortEMJetty;
    private String tempResult1, tempResult2, tempResult3, tempResult4;
    private List<String> roleIds;
    private String testCaseId;

    public AgentControllabilityCDVTests() {

        cdvRoleId = CDVConstants.CDV_ROLE_ID;
        mom1RoleId = CDVConstants.MOM1_ROLE_ID;
        mom1Col1RoleId = CDVConstants.MOM1_COL1_ROLE_ID;
        mom1Col2RoleId = CDVConstants.MOM1_COL2_ROLE_ID;
        mom2RoleId = CDVConstants.MOM2_ROLE_ID;
        mom2Col1RoleId = CDVConstants.MOM2_COL1_ROLE_ID;
        mom2Col2RoleId = CDVConstants.MOM2_COL2_ROLE_ID;
        agentTomcatID = CDVConstants.TOMCAT_ROLE_ID;
        cdvMachineId = CDVConstants.CDV_MACHINE_ID;
        mom1MachineId = CDVConstants.MOM1_MACHINE_ID;
        mom1Col1MachineId = CDVConstants.MOM1_MACHINE_ID;
        mom1Col2MachineId = CDVConstants.MOM1_MACHINE_ID;
        mom2MachineId = CDVConstants.MOM2_MACHINE_ID;
        mom2Col1MachineId = CDVConstants.MOM2_MACHINE_ID;
        mom2Col2MachineId = CDVConstants.MOM2_MACHINE_ID;

        cdvHost = envProperties.getMachineHostnameByRoleId(cdvRoleId);
        cdvPort =
            Integer.parseInt(envProperties.getRolePropertiesById(cdvRoleId).getProperty("emPort"));
        cdvWebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(cdvRoleId)
                .getProperty("emWebPort"));
        mom1Host = envProperties.getMachineHostnameByRoleId(mom1RoleId);
        mom1Col1Host = envProperties.getMachineHostnameByRoleId(mom1Col1RoleId);
        mom1Col2Host = envProperties.getMachineHostnameByRoleId(mom1Col2RoleId);
        mom1Port =
            Integer.parseInt(envProperties.getRolePropertiesById(mom1RoleId).getProperty("emPort"));
        mom1Col1Port =
            Integer.parseInt(envProperties.getRolePropertiesById(mom1Col1RoleId).getProperty(
                "emPort"));
        mom1Col2Port =
            Integer.parseInt(envProperties.getRolePropertiesById(mom1Col2RoleId).getProperty(
                "emPort"));
        mom1WebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(mom1RoleId).getProperty(
                "emWebPort"));
        mom1Col1WebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(mom1Col1RoleId).getProperty(
                "emWebPort"));
        mom1Col2WebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(mom1Col2RoleId).getProperty(
                "emWebPort"));
        mom2Host = envProperties.getMachineHostnameByRoleId(mom2RoleId);
        mom2Col1Host = envProperties.getMachineHostnameByRoleId(mom2Col1RoleId);
        mom2Col2Host = envProperties.getMachineHostnameByRoleId(mom2Col1RoleId);
        mom2Port =
            Integer.parseInt(envProperties.getRolePropertiesById(mom2RoleId).getProperty("emPort"));
        mom2Col1Port =
            Integer.parseInt(envProperties.getRolePropertiesById(mom2Col1RoleId).getProperty(
                "emPort"));
        mom2Col2Port =
            Integer.parseInt(envProperties.getRolePropertiesById(mom2Col2RoleId).getProperty(
                "emPort"));
        mom2WebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(mom2RoleId).getProperty(
                "emWebPort"));
        mom2Col1WebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(mom2Col1RoleId).getProperty(
                "emWebPort"));
        mom2Col2WebPort =
            Integer.parseInt(envProperties.getRolePropertiesById(mom2Col2RoleId).getProperty(
                "emWebPort"));
        tomcatMachineId = CDVConstants.AGENT_MACHINE_ID;
        jbossMachineId = CDVConstants.AGENT_MACHINE_ID;
        tomcatRoleId = CDVConstants.TOMCAT_ROLE_ID;
        jbossRoleId = CDVConstants.JBOSS_ROLE_ID;
        cdvLibDir =
            envProperties.getRolePropertyById(cdvRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        cdvConfigDir =
            envProperties.getRolePropertyById(cdvRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        cdvConfigFile =
            envProperties.getRolePropertyById(cdvRoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        cdvConfigFile_copy =
            envProperties.getRolePropertyById(cdvRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        cdvLogFile =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        agentHost = envProperties.getMachineHostnameByRoleId(agentTomcatID);
        agentPort =
            envProperties.getRolePropertyById(agentTomcatID,
                DeployTomcatFlowContext.ENV_TOMCAT_PORT);
        user = ApmbaseConstants.emUser;
        password = ApmbaseConstants.emPassw;
        mom1LibDir =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        mom1Configdir =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        mom1ConfigFile =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        mom1ConfigFile_copy =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        mom1Col1ConfigFile =
            envProperties.getRolePropertyById(mom1Col1RoleId,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        mom1Col1ConfigFile_copy =
            envProperties
                .getRolePropertyById(mom1Col1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        mom1Col2ConfigFile =
            envProperties.getRolePropertyById(mom1Col2RoleId,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        mom1Col2ConfigFile_copy =
            envProperties
                .getRolePropertyById(mom1Col2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        agent_collector_name = envProperties.getMachineHostnameByRoleId(CDVConstants.CDV_ROLE_ID);
        mom1LoadBalanceFile =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        mom1LoadBalanceFile_copy =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml.orig";
        mom1LogFile =
            envProperties.getRolePropertyById(mom1RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        mom1Col1LogFile =
            envProperties.getRolePropertyById(mom1Col1RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        mom1Col2LogFile =
            envProperties.getRolePropertyById(mom1Col2RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);

        mom2LibDir =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        mom2Configdir =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        mom2ConfigFile =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        mom2ConfigFile_copy =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        mom2Col1ConfigFile =
            envProperties.getRolePropertyById(mom2Col1RoleId,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        mom2Col1ConfigFile_copy =
            envProperties
                .getRolePropertyById(mom2Col1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        mom2Col2ConfigFile =
            envProperties.getRolePropertyById(mom2Col2RoleId,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        mom2Col2ConfigFile_copy =
            envProperties
                .getRolePropertyById(mom2Col2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/IntroscopeEnterpriseManager.properties.orig";
        mom2LoadBalanceFile =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        mom2LoadBalanceFile_copy =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml.orig";
        mom2LogFile =
            envProperties.getRolePropertyById(mom2RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        mom2Col1LogFile =
            envProperties.getRolePropertyById(mom2Col1RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        mom2Col2LogFile =
            envProperties.getRolePropertyById(mom2Col2RoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);

        tomcatAgentProfile =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatAgentProfile_copy =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile.orig";
        jbossAgentProfile =
            envProperties.getRolePropertyById(jbossRoleId,
                DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        jbossAgentProfile_copy =
            envProperties.getRolePropertyById(jbossRoleId,
                DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile.orig";
        jbossLogFile =
            envProperties.getRolePropertyById(jbossRoleId,
                DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        tomcatLogFile =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        tomcatAgentExpression = ".*\\|.*\\|.*Tomcat.*";
        mom1Col1emJettyConfigFile =
            envProperties
                .getRolePropertyById(mom1Col1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        mom1Col2emJettyConfigFile =
            envProperties
                .getRolePropertyById(mom1Col2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        mom2Col1emJettyConfigFile =
            envProperties
                .getRolePropertyById(mom2Col1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        mom2Col2emJettyConfigFile =
            envProperties
                .getRolePropertyById(mom2Col2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        tempResult1 = "";
        tempResult2 = "";
        tempResult3 = "";
        xpathToHttpsPortEMJetty =
            "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"port\"]";

        roleIds = new ArrayList<String>();
        roleIds.add(cdvRoleId);
        roleIds.add(mom1RoleId);
        roleIds.add(mom1Col1RoleId);
        roleIds.add(mom1Col2RoleId);
        roleIds.add(mom2RoleId);
        roleIds.add(mom2Col1RoleId);
        roleIds.add(mom2Col2RoleId);
    }

    @BeforeClass(alwaysRun = true)
    public void ACCInitialize() {
        // set loadbalancing interval property
        replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
            "introscope.enterprisemanager.loadbalancing.interval=120", mom1MachineId,
            mom1ConfigFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
            "introscope.enterprisemanager.loadbalancing.interval=120", mom2MachineId,
            mom2ConfigFile);
        List<String> appendclwenableprop = new ArrayList<String>();
        appendclwenableprop.add("introscope.apm.agentcontrol.clw.enable=true");
        appendProp(appendclwenableprop, mom1MachineId, mom1ConfigFile);
        appendProp(appendclwenableprop, mom2MachineId, mom2ConfigFile);

        // set jboss agent naming property to false
        replaceProp("introscope.agent.agentAutoNamingEnabled=true",
            "introscope.agent.agentAutoNamingEnabled=false", jbossMachineId, jbossAgentProfile);

        // backup all config files - loadbalancing file, mom_config, col_cofigs,
        // tomcatagentprofile, jbossagentprofile
        backupFile(mom1LoadBalanceFile, mom1LoadBalanceFile_copy, mom1MachineId);
        backupFile(mom1ConfigFile, mom1ConfigFile_copy, mom1MachineId);
        backupFile(mom1Col1ConfigFile, mom1Col1ConfigFile_copy, mom1Col1MachineId);
        backupFile(mom1Col2ConfigFile, mom1Col2ConfigFile_copy, mom1Col2MachineId);
        backupFile(mom2LoadBalanceFile, mom2LoadBalanceFile_copy, mom2MachineId);
        backupFile(mom2ConfigFile, mom2ConfigFile_copy, mom2MachineId);
        backupFile(mom2Col1ConfigFile, mom2Col1ConfigFile_copy, mom2Col1MachineId);
        backupFile(mom2Col2ConfigFile, mom2Col2ConfigFile_copy, mom2Col2MachineId);
        backupFile(tomcatAgentProfile, tomcatAgentProfile_copy, tomcatMachineId);
        backupFile(jbossAgentProfile, jbossAgentProfile_copy, jbossMachineId);
        backupFile(cdvConfigFile, cdvConfigFile_copy, cdvMachineId);
        backupEMJettyConfigFile(roleIds);
        List<String> machines = new ArrayList<String>();
        machines.add(cdvMachineId);
        machines.add(mom1MachineId);
        machines.add(mom2MachineId);
        machines.add(CDVConstants.AGENT_MACHINE_ID);
        syncTimeOnMachines(machines);
    }

    public void revertConfigFiles() {

        revertFile(mom1ConfigFile, mom1ConfigFile_copy, mom1MachineId);
        revertFile(mom1Col1ConfigFile, mom1Col1ConfigFile_copy, mom1Col1MachineId);
        revertFile(mom1Col2ConfigFile, mom1Col2ConfigFile_copy, mom1Col2MachineId);
        revertFile(mom2ConfigFile, mom2ConfigFile_copy, mom2MachineId);
        revertFile(mom2Col1ConfigFile, mom2Col1ConfigFile_copy, mom2Col1MachineId);
        revertFile(mom2Col2ConfigFile, mom2Col2ConfigFile_copy, mom2Col2MachineId);
        revertFile(mom1LoadBalanceFile, mom1LoadBalanceFile_copy, mom1MachineId);
        revertFile(mom2LoadBalanceFile, mom2LoadBalanceFile_copy, mom2MachineId);
        revertFile(tomcatAgentProfile, tomcatAgentProfile_copy, tomcatMachineId);
        revertFile(jbossAgentProfile, jbossAgentProfile_copy, jbossMachineId);
        revertFile(cdvConfigFile, cdvConfigFile_copy, cdvMachineId);
        revertEMJettyConfigFile(roleIds);
    }

    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_295429_redirect_agent_outside_cluster_to_CDV() {
        testCaseId = "_295429";
        try {
            startEM(CDVConstants.CDV_ROLE_ID);
            startEM(CDVConstants.MOM1_ROLE_ID);
            startEM(CDVConstants.MOM1_COL1_ROLE_ID);
            startEM(CDVConstants.MOM1_COL2_ROLE_ID);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                cdvHost, cdvPort, mom1Host, mom1Port, mom1LibDir);

            String msg1 =
                "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect to the Introscope Enterprise Manager at "
                    + cdvHost;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }

    }

    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_295427_redirect_agent_outside_cluster_to_collector() {
        testCaseId = "_295427";
        try {
            startEM(CDVConstants.CDV_ROLE_ID);
            startEM(CDVConstants.MOM1_ROLE_ID);
            startEM(CDVConstants.MOM1_COL1_ROLE_ID);
            startEM(CDVConstants.MOM1_COL2_ROLE_ID);
            startEM(CDVConstants.MOM2_ROLE_ID);
            startEM(CDVConstants.MOM2_COL1_ROLE_ID);
            startEM(CDVConstants.MOM2_COL2_ROLE_ID);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Col1Host, mom2Col1Port, mom1Host, mom1Port, mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at "
                    + mom2Col1Host;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collector of cluster 2",
                tempResult1.contains("Tomcat"));

        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_295428_redirect_agent_outside_cluster_to_MOM() {
        testCaseId = "_295428";
        try {
            startEM(CDVConstants.CDV_ROLE_ID);
            startEM(CDVConstants.MOM1_ROLE_ID);
            startEM(CDVConstants.MOM1_COL1_ROLE_ID);
            startEM(CDVConstants.MOM1_COL2_ROLE_ID);
            startEM(CDVConstants.MOM2_ROLE_ID);
            startEM(CDVConstants.MOM2_COL1_ROLE_ID);
            startEM(CDVConstants.MOM2_COL2_ROLE_ID);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Host, mom2Port, mom1Host, mom1Port, mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at " + mom2Host;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            tempResult2 =
                clw.getNodeList(user, password, ".*", mom2Col2Host, mom2Col2Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collectors of cluster 2",
                tempResult1.contains("Tomcat") || tempResult2.contains("Tomcat"));

        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }

    /*
     * HTTP Tests
     */
    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_353487_ACCHTTP_redirect_agent_outside_cluster_to_collector() {
        testCaseId = "_353487";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=http://" + mom1Host + ":" + mom1WebPort, tomcatMachineId,
                tomcatAgentProfile);

            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startEM(mom2RoleId);
            startEM(mom2Col1RoleId);
            startEM(mom2Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Col1Host, mom2Col1WebPort, mom1Host, mom1Port, mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at "
                    + mom2Col1Host;

            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collector of cluster 2",
                tempResult1.contains("Tomcat"));

            tempResult2 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Host",
                    mom2Col1Host, mom2Col1Port, mom1LibDir);

            Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Host value",
                tempResult2.contains("String:::" + mom2Col1Host));

            tempResult3 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                    mom2Col1Host, mom2Col1Port, mom1LibDir);

            Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                tempResult3.equals("String:::" + mom2Col1WebPort));

        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }

    /*
     * HTTPS Tests
     */
    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_353281_ACCHTTPS_redirect_agent_outside_cluster_to_CDV() {
        testCaseId = "_353281";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=https://" + mom1Host + ":" + CDVConstants.MOM1_HTTPS_PORT,
                tomcatMachineId, tomcatAgentProfile);
            updateEMPropertiesForHTTPSWithoutValidation(roleIds);
            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                cdvHost, Integer.parseInt(CDVConstants.CDV_HTTPS_PORT), mom1Host, mom1Port,
                mom1LibDir);

            String msg1 =
                "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect to the Introscope Enterprise Manager at "
                    + cdvHost;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }

    }

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_353280_ACCHTTPS_redirect_agent_outside_cluster_to_MOM() {
        testCaseId = "_353280";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=https://" + mom1Host + ":" + CDVConstants.MOM1_HTTPS_PORT,
                tomcatMachineId, tomcatAgentProfile);
            updateEMPropertiesForHTTPSWithoutValidation(roleIds);
            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startEM(mom2RoleId);
            startEM(mom2Col1RoleId);
            startEM(mom2Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Host, Integer.parseInt(CDVConstants.MOM2_HTTPS_PORT), mom1Host, mom1Port,
                mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at " + mom2Host;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            tempResult2 =
                clw.getNodeList(user, password, ".*", mom2Col2Host, mom2Col2Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collectors of cluster 2",
                tempResult1.contains("Tomcat") || tempResult2.contains("Tomcat"));

            if (tempResult1.contains("Tomcat")) {
                tempResult3 =
                    clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                        mom2Col1Host, mom2Col1Port, mom1LibDir);

                Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                    tempResult3.contains("String:::" + CDVConstants.MOM2COL1_HTTPS_PORT));
            } else if (tempResult2.contains("Tomcat")) {
                tempResult3 =
                    clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                        mom2Col2Host, mom2Col2Port, mom1LibDir);

                Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                    tempResult3.equals("String:::" + CDVConstants.MOM2COL2_HTTPS_PORT));
            }
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }

    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_353279_ACCHTTPS_redirect_agent_outside_cluster_to_collector() {
        testCaseId = "_353279";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=https://" + mom1Host + ":" + CDVConstants.MOM1_HTTPS_PORT,
                tomcatMachineId, tomcatAgentProfile);
            updateEMPropertiesForHTTPSWithoutValidation(roleIds);

            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startEM(mom2RoleId);
            startEM(mom2Col1RoleId);
            startEM(mom2Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Col1Host, Integer.parseInt(CDVConstants.MOM2COL1_HTTPS_PORT), mom1Host,
                mom1Port, mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at "
                    + mom2Col1Host;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collector of cluster 2",
                tempResult1.contains("Tomcat"));

            tempResult2 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Host",
                    mom2Col1Host, mom2Col1Port, mom1LibDir);

            Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Host value",
                tempResult2.contains("String:::" + mom2Col1Host));

            tempResult3 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                    mom2Col1Host, mom2Col1Port, mom1LibDir);

            Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                tempResult3.equals("String:::" + CDVConstants.MOM2COL1_HTTPS_PORT));
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }

    /*
     * SSL Tests
     */
    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_353463_ACCSSL_redirect_agent_outside_cluster_to_CDV() {
        testCaseId = "_353463";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=ssl://" + mom1Host + ":" + CDVConstants.MOM1_SSL_PORT,
                tomcatMachineId, tomcatAgentProfile);
            updateEMPropertiesForSSLWithoutValidation(roleIds);
            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                cdvHost, Integer.parseInt(CDVConstants.CDV_SSL_PORT), mom1Host, mom1Port,
                mom1LibDir);

            String msg1 =
                "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect to the Introscope Enterprise Manager at "
                    + cdvHost;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }

    }

    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_353462_ACCSSL_redirect_agent_outside_cluster_to_MOM() {
        testCaseId = "_353462";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=ssl://" + mom1Host + ":" + CDVConstants.MOM1_SSL_PORT,
                tomcatMachineId, tomcatAgentProfile);
            updateEMPropertiesForSSLWithoutValidation(roleIds);
            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startEM(mom2RoleId);
            startEM(mom2Col1RoleId);
            startEM(mom2Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Host, Integer.parseInt(CDVConstants.MOM2_SSL_PORT), mom1Host, mom1Port,
                mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at " + mom2Host;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            tempResult2 =
                clw.getNodeList(user, password, ".*", mom2Col2Host, mom2Col2Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collectors of cluster 2",
                tempResult1.contains("Tomcat") || tempResult2.contains("Tomcat"));

            if (tempResult1.contains("Tomcat")) {
                tempResult3 =
                    clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                        mom2Col1Host, mom2Col1Port, mom1LibDir);

                Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                    tempResult3.contains("String:::" + CDVConstants.MOM2COL1_SSL_PORT));
            } else if (tempResult2.contains("Tomcat")) {
                tempResult3 =
                    clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                        mom2Col2Host, mom2Col2Port, mom1LibDir);

                Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                    tempResult3.equals("String:::" + CDVConstants.MOM2COL2_SSL_PORT));
            }
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }

    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_353461_ACCSSL_redirect_agent_outside_cluster_to_collector() {
        testCaseId = "_353461";
        try {
            replaceProp("agentManager.url.1=" + mom1Host + ":" + mom1Port,
                "agentManager.url.1=ssl://" + mom1Host + ":" + CDVConstants.MOM1_SSL_PORT,
                tomcatMachineId, tomcatAgentProfile);
            updateEMPropertiesForSSLWithoutValidation(roleIds);

            startEM(cdvRoleId);
            startEM(mom1RoleId);
            startEM(mom1Col1RoleId);
            startEM(mom1Col2RoleId);
            startEM(mom2RoleId);
            startEM(mom2Col1RoleId);
            startEM(mom2Col2RoleId);
            startTomcatAgent(tomcatRoleId);

            clw.setAllowedAgentWithCollector(user, password, cdvRoleId, tomcatAgentExpression,
                mom2Col1Host, Integer.parseInt(CDVConstants.MOM2COL1_SSL_PORT), mom1Host, mom1Port,
                mom1LibDir);

            String msg1 =
                "Connected controllable Agent to the Introscope Enterprise Manager at "
                    + mom2Col1Host;
            checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

            tempResult1 =
                clw.getNodeList(user, password, ".*", mom2Col1Host, mom2Col1Port, mom1LibDir)
                    .toString();
            Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collector of cluster 2",
                tempResult1.contains("Tomcat"));

            tempResult2 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Host",
                    mom2Col1Host, mom2Col1Port, mom1LibDir);

            Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Host value",
                tempResult2.contains("String:::" + mom2Col1Host));

            tempResult3 =
                clw.getLatestMetricValue(user, password, tomcatAgentExpression, "EM Port",
                    mom2Col1Host, mom2Col1Port, mom1LibDir);

            Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                tempResult3.equals("String:::" + CDVConstants.MOM2COL1_SSL_PORT));
        } finally {
            stopEM(mom1RoleId);
            stopCollectorEM(mom1RoleId, cdvRoleId);
            stopCollectorEM(mom1RoleId, mom1Col1RoleId);
            stopCollectorEM(mom1RoleId, mom1Col2RoleId);
            stopCollectorEM(mom1RoleId, mom2RoleId);
            stopCollectorEM(mom1RoleId, mom2Col1RoleId);
            stopCollectorEM(mom1RoleId, mom2Col2RoleId);
            stopTomcatAgent(tomcatRoleId);

            revertConfigFiles();
            renameFile(cdvLogFile, cdvLogFile + testCaseId, cdvMachineId);
            renameFile(mom1LogFile, mom1LogFile + testCaseId, mom1MachineId);
            renameFile(mom1Col1LogFile, mom1Col1LogFile + testCaseId, mom1Col1MachineId);
            renameFile(mom1Col2LogFile, mom1Col2LogFile + testCaseId, mom1Col2MachineId);
            renameFile(mom2LogFile, mom2LogFile + testCaseId, mom2MachineId);
            renameFile(mom2Col1LogFile, mom2Col1LogFile + testCaseId, mom2Col1MachineId);
            renameFile(mom2Col2LogFile, mom2Col2LogFile + testCaseId, mom2Col2MachineId);
            renameFile(tomcatLogFile, tomcatLogFile + testCaseId, tomcatMachineId);
        }
    }



    /**
     * revert EM Jetty Config files for specified roleId's
     *
     * @param roleIds
     */
    public void revertEMJettyConfigFile(List<String> roleIds) {
        String machineId, emJettyConfigFile, emJettyConfigFile_copy;
        for (String roleId : roleIds) {
            machineId = envProperties.getMachineIdByRoleId(roleId);
            emJettyConfigFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            emJettyConfigFile_copy =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml.orig";
            revertFile(emJettyConfigFile, emJettyConfigFile_copy, machineId);
        }
    }

    /**
     * backup EM Jetty Config files for specified roleId's
     *
     * @param roleIds
     */
    public void backupEMJettyConfigFile(List<String> roleIds) {

        String machineId, emJettyConfigFile, emJettyConfigFile_copy;
        String OrigString =
            "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://jetty.mortbay.org/configure.dtd\">";
        String stringToReplace =
            "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://www.eclipse.org/jetty/configure.dtd\">";
        for (String roleId : roleIds) {
            machineId = envProperties.getMachineIdByRoleId(roleId);
            emJettyConfigFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            emJettyConfigFile_copy =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml.orig";
            replaceProp(OrigString, stringToReplace, machineId, emJettyConfigFile);
            backupFile(emJettyConfigFile, emJettyConfigFile_copy, machineId);
        }
    }

    /**
     * updates EM profile properties to enable https communication in machines
     * with specified roleIds
     * 
     * @param roleIds
     */
    public void updateEMPropertiesForHTTPSWithoutValidation(List<String> roleIds) {
        String machineId, configFile;

        for (String roleId : roleIds) {
            machineId = envProperties.getMachineIdByRoleId(roleId);
            configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            replaceProp(
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                machineId, configFile);
        }
        updateHttpsPortInCollectors();
    }

    /**
     * updates https listener ports in em-jetty-config.xml files for collectors
     */
    private void updateHttpsPortInCollectors() {
        updateNodeValueInXML(mom1Col1MachineId, mom1Col1emJettyConfigFile, xpathToHttpsPortEMJetty,
            CDVConstants.MOM1COL1_HTTPS_PORT);
        updateNodeValueInXML(mom1Col2MachineId, mom1Col2emJettyConfigFile, xpathToHttpsPortEMJetty,
            CDVConstants.MOM1COL2_HTTPS_PORT);
        updateNodeValueInXML(mom2Col1MachineId, mom2Col1emJettyConfigFile, xpathToHttpsPortEMJetty,
            CDVConstants.MOM2COL1_HTTPS_PORT);
        updateNodeValueInXML(mom2Col2MachineId, mom2Col2emJettyConfigFile, xpathToHttpsPortEMJetty,
            CDVConstants.MOM2COL2_HTTPS_PORT);
    }

    /**
     * updates EM profile properties to enable ssl communication in machines
     * with specified roleIds
     * 
     * @param roleIds
     */
    public void updateEMPropertiesForSSLWithoutValidation(List<String> roleIds) {
        String machineId, configFile;

        for (String roleId : roleIds) {
            machineId = envProperties.getMachineIdByRoleId(roleId);
            configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            replaceProp("introscope.enterprisemanager.enabled.channels=channel1",
                "introscope.enterprisemanager.enabled.channels=channel1,channel2", machineId,
                configFile);
            replaceProp(
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                machineId, configFile);
        }
        updateHttpsPortInCollectors();
        updateSSLPortInCollectors();
    }

    /**
     * updates ssl listener ports in IntroscopeEnterpriseManager.properties files for collectors
     */
    private void updateSSLPortInCollectors() {
        replaceProp("introscope.enterprisemanager.port.channel2=5443",
            "introscope.enterprisemanager.port.channel2=" + CDVConstants.MOM1COL1_SSL_PORT,
            mom1Col1MachineId, mom1Col1ConfigFile);
        replaceProp("introscope.enterprisemanager.port.channel2=5443",
            "introscope.enterprisemanager.port.channel2=" + CDVConstants.MOM1COL2_SSL_PORT,
            mom1Col2MachineId, mom1Col2ConfigFile);
        replaceProp("introscope.enterprisemanager.port.channel2=5443",
            "introscope.enterprisemanager.port.channel2=" + CDVConstants.MOM2COL1_SSL_PORT,
            mom2Col1MachineId, mom2Col1ConfigFile);
        replaceProp("introscope.enterprisemanager.port.channel2=5443",
            "introscope.enterprisemanager.port.channel2=" + CDVConstants.MOM2COL2_SSL_PORT,
            mom2Col2MachineId, mom2Col2ConfigFile);
    }

    /**
     * Updates a text value of a node specified by xpath.
     * 
     * @param machineId
     * @param xmlFilePath
     * @param xpathToNode
     *        The xpath to a node.
     * @param value
     *        The text value, empty string for empty node
     */
    public void updateNodeValueInXML(String machineId, String xmlFilePath, String xpathToNode,
        String value) {

        XmlModifierFlowContext modifyXML =
            new XmlModifierFlowContext.Builder(xmlFilePath).updateNode(xpathToNode, value).build();

        runFlowByMachineId(machineId, XmlModifierFlow.class, modifyXML);

    }

}
