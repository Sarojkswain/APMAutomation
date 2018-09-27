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
package com.ca.apm.tests.agentcontrollability.httptunnelling;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.RunCommandFlow;
import com.ca.apm.commons.flow.RunCommandFlowContext;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.testbed.HTTPTunnellingClusterLinuxTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class HttpTunnellingTests extends AgentControllabilityConstants {
    protected final String LINUX_APACHE_CONFIG_FILE;
    protected final String LINUX_APACHE_BIN_DIR;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTunnellingTests.class);
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
    List<String> roleIds = new ArrayList<String>();

    public HttpTunnellingTests() {
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
        LINUX_APACHE_BIN_DIR = "/usr/bin/";
        LINUX_APACHE_CONFIG_FILE = "/etc/httpd/conf/httpd.conf";
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

        backupFile(LINUX_APACHE_CONFIG_FILE, LINUX_APACHE_CONFIG_FILE + "_backup",
            APACHE_MACHINE_ID);
        List<String> machines = new ArrayList<String>();

        machines.add(MOM_MACHINE_ID);
        machines.add(COLLECTOR1_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        machines.add(APACHE_MACHINE_ID);

        syncTimeOnMachines(machines);
        setLoadBalancingPropValues(MOM_ROLE_ID);
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(TOMCAT_ROLE_ID);

        List<String> tunnellingProperties = new ArrayList<String>();
        tunnellingProperties.add("LoadModule proxy_module modules/mod_proxy.so");
        tunnellingProperties.add("LoadModule proxy_connect_module modules/mod_proxy_connect.so");
        tunnellingProperties.add("LoadModule proxy_http_module modules/mod_proxy_http.so");
        tunnellingProperties.add("\n");
        tunnellingProperties.add("\n");

        tunnellingProperties.add("AllowConnect 8444");
        tunnellingProperties.add("Listen 8099");
        tunnellingProperties.add("<VirtualHost *:8099>");
        tunnellingProperties.add("ProxyRequests On");
        tunnellingProperties.add("#ProxyPass http://localhost:8081/");
        tunnellingProperties.add("#ProxyPassReverse http://localhost:8081/");
        tunnellingProperties.add("<Proxy *>");
        tunnellingProperties.add("AuthType Basic");
        tunnellingProperties.add("AuthName \"Authentication\"");
        tunnellingProperties.add("AuthUserFile \"" + LINUX_APACHE_BIN_DIR + "password\"");
        tunnellingProperties.add("Require user admin");
        tunnellingProperties.add("</Proxy>");
        tunnellingProperties.add("</VirtualHost>");

        appendProp(tunnellingProperties, APACHE_MACHINE_ID, LINUX_APACHE_CONFIG_FILE);
        generatePassword();
        setAgentProperties();
        startApache();
    }


    /************************************************
     ************* Author: JAMSA07********************
     * **********************************************
     */
    @Tas(testBeds = @TestBed(name = HTTPTunnellingClusterLinuxTestbed.class, executeOn = MOM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Test(groups = {"DEEP"}, enabled = true, priority = 1)
    public void verify_ALM_310219_HTTPTunnellingWithAuth() {
        String testCaseId = "310219";
        try {
            startEMCollectors();
            startEM(MOM_ROLE_ID);
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopServices();
            renameLogWithTestCaseId(roleIds, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_310222_ProxyFailure() {
        String testCaseId = "310222";
        try {
            replaceProp("agentManager.httpProxy.port=8099", "agentManager.httpProxy.port=8098",
                TOMCAT_MACHINE_ID, tomcatagentProfileFile);
            startEMCollectors();
            startEM(MOM_ROLE_ID);
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momhost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopServices();
            replaceProp("agentManager.httpProxy.port=8098", "agentManager.httpProxy.port=8099",
                TOMCAT_MACHINE_ID, tomcatagentProfileFile);
            renameLogWithTestCaseId(roleIds, testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_310220_NoAuthentication() {
        String testCaseId = "310220";
        try {
            backupFile(tomcatagentProfileFile, tomcatagentProfileFile + "_modified",
                TOMCAT_MACHINE_ID);
            replaceProp("agentManager.httpProxy.username=admin",
                "agentManager.httpProxy.username=", TOMCAT_MACHINE_ID, tomcatagentProfileFile);

            replaceProp("agentManager.httpProxy.password=password",
                "agentManager.httpProxy.password=", TOMCAT_MACHINE_ID, tomcatagentProfileFile);

            startEMCollectors();
            startEM(MOM_ROLE_ID);
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momhost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            List<String> hostNames = new ArrayList<String>();
            hostNames.add(momhost);
            hostNames.add(collector1Host);
            checkAgentFailedToConnect(hostNames, tomcatAgentLogFile, TOMCAT_MACHINE_ID);
        } finally {
            stopServices();
            deleteFile(tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            moveFile(tomcatagentProfileFile + "_modified", tomcatagentProfileFile,
                TOMCAT_MACHINE_ID);
            renameLogWithTestCaseId(roleIds, testCaseId);
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

    public void generatePassword() {
        RunCommandFlowContext.Builder runCommandContextbuilder =
            new RunCommandFlowContext.Builder();
        runCommandContextbuilder.directory(LINUX_APACHE_BIN_DIR)
            .command("htpasswd -cb password admin password").build();
        RunCommandFlowContext runCommandContext =
            new RunCommandFlowContext(runCommandContextbuilder);
        runFlowByMachineId(APACHE_MACHINE_ID, RunCommandFlow.class, runCommandContext);
    }

    public void startApache() {

        String[] commands = {"/", "service httpd start", "\\r"};
        try {
            new TestUtils().execUnixCmd(envProperties.getMachineHostnameByRoleId(APACHE_ROLE_ID),
                22, "root", "Lister@123", commands);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void stopApache() {
        String[] commands = {"/", "service httpd stop", "\\r"};
        try {
            new TestUtils().execUnixCmd(envProperties.getMachineHostnameByRoleId(APACHE_ROLE_ID),
                22, "root", "Lister@123", commands);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAgentProperties() {
        setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momhost, momPort, momhost,
            momWebPort);
        replaceProp(
            "#agentManager.httpProxy.host=",
            "agentManager.httpProxy.host="
                + envProperties.getMachineHostnameByRoleId(APACHE_ROLE_ID), TOMCAT_MACHINE_ID,
            tomcatagentProfileFile);
        replaceProp("#agentManager.httpProxy.port=", "agentManager.httpProxy.port=8099",
            TOMCAT_MACHINE_ID, tomcatagentProfileFile);
        replaceProp("#agentManager.httpProxy.username=", "agentManager.httpProxy.username=admin",
            TOMCAT_MACHINE_ID, tomcatagentProfileFile);
        replaceProp("#agentManager.httpProxy.password=",
            "agentManager.httpProxy.password=password", TOMCAT_MACHINE_ID, tomcatagentProfileFile);
    }

    /**
     * Verify failed to reconnect messages in an agent with multiple em hostnames
     * 
     * @param hostNames
     * @param logFileName
     * @param machineID
     */
    public void checkAgentFailedToConnect(List<String> hostNames, String logFileName,
        String machineID) {

        List<String> httpsReconnectString = new ArrayList<String>();
        for (String hostName : hostNames) {
            httpsReconnectString.add("Failed to connect to the Introscope Enterprise Manager at "
                + hostName + ".ca.com" + ":" + momWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory");
            httpsReconnectString.add("Failed to connect to the Introscope Enterprise Manager at "
                + hostName + ":" + momWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory");
        }
        verifyIfAtleastOneKeywordIsInLog(machineID, logFileName, httpsReconnectString);
    }

}
