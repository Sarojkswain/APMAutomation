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
package com.ca.apm.commons.tests;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.common.XMLFileUtil;
import com.ca.apm.commons.flow.FailoverModifierFlow;
import com.ca.apm.commons.flow.FailoverModifierFlowContext;
import com.ca.apm.commons.flow.XMLModifierFlow;
import com.ca.apm.commons.flow.XMLModifierFlowContext;
import com.ca.apm.tests.common.introscope.util.CLWBean;

public class AgentControllabilityCommons extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentControllabilityCommons.class);

    String user;
    String password;

    TestUtils utility = new TestUtils();
    CLWCommons clw = new CLWCommons();
    BaseAgentTest bt = new BaseAgentTest();
    EmUtils emUtils = utilities.createEmUtils();
    CLWBean clwbean;
    protected XMLUtil xmlUtil = new XMLUtil();
    XMLFileUtil xmlFileUtil = new XMLFileUtil();

    boolean windows = Os.isFamily(Os.FAMILY_WINDOWS);
    String keyStoreDir;


    public AgentControllabilityCommons() {
        user = ApmbaseConstants.emUser;
        password = ApmbaseConstants.emPassw;
        List<String> nfsServerWindowsCommandMom = new ArrayList<String>();

        nfsServerWindowsCommandMom
            .add("net share keystore_EM=c:\\automation\\deployed\\em\\config\\internal\\server /GRANT:Everyone,FULL");
        try {
            ApmbaseUtil.invokeProcessBuilder(nfsServerWindowsCommandMom, "/");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Revert the EM Jetty File changes
     * 
     * @param emJettyFile
     * @param emMachineId
     */
    public void revertEMJetty(String emJettyFile, String emMachineId) {
        deleteFile(emJettyFile, emMachineId);
        moveFile(emJettyFile + "_backup", emJettyFile, emMachineId);
    }

    /**
     * Creates a FILE_main as a backup in EMs
     * 
     * @param roleIds
     */
    public void mainBackupEM(List<String> roleIds) {
        String machineId, emConfigfile, emJettyFile;
        for (String roleId : roleIds) {

            machineId = envProperties.getMachineIdByRoleId(roleId);
            emConfigfile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            emJettyFile =
                envProperties.getRolePropertyById(roleId, emConfigfile) + "/em-jetty-config.xml";

            backupFile(emConfigfile, emConfigfile + "_main", machineId);
            backupFile(emJettyFile, emJettyFile + "_main", machineId);
        }
    }

    /**
     * Creates a FILE_main as a backup in Agents
     * 
     * @param roleIds
     */
    public void mainBackupAgent(List<String> roleIds) {
        String machineId, agentProfileFile;
        for (String roleId : roleIds) {

            machineId = envProperties.getMachineIdByRoleId(roleId);
            agentProfileFile =
                envProperties.getRolePropertyById(roleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                    + "/wily/core/config/IntroscopeAgent.profile";

            backupFile(agentProfileFile, agentProfileFile + "_main", machineId);
        }
    }

    /**
     * Set the load-balancing Clustering properties for EM
     * 
     * @param roleId
     */
    public void setLoadBalancingPropValues(String roleId) {
        String configFile =
            envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);

        List<String> colWeights = new ArrayList<String>();
        colWeights.add("introscope.apm.agentcontrol.clw.enable=true");
        colWeights.add("log4j.logger.Manager.LoadBalancer=DEBUG");
        colWeights.add("log4j.logger.additivity.Manager.LoadBalancer=false");
        appendProp(colWeights, envProperties.getMachineIdByRoleId(roleId), configFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
            "introscope.enterprisemanager.loadbalancing.interval=120",
            envProperties.getMachineIdByRoleId(roleId), configFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.threshold=20000",
            "introscope.enterprisemanager.loadbalancing.threshold=1",
            envProperties.getMachineIdByRoleId(roleId), configFile);
    }

    /**
     * This is to change the agent AgentUrl for HTTP Connection
     * agentManager.url.1=jamsa07-i152259:5001
     *
     * @param agentProfileFile
     * @param AgentMachineId
     * @param emHostOriginal
     * @param portOriginal
     * @param replaceEmHost
     * @param replaceemPort
     */
    public void setAgentHttpUrl(String agentProfileFile, String AgentMachineId,
        String emHostOriginal, String portOriginal, String replaceEmHost, String replaceemPort) {
        backupFile(agentProfileFile, agentProfileFile + "_backup", AgentMachineId);
        replaceProp("agentManager.url.1=" + emHostOriginal + ":" + portOriginal,
            "agentManager.url.1=http://" + replaceEmHost + ":" + replaceemPort, AgentMachineId,
            agentProfileFile);
    }

    /**
     * This is to change the agent AgentUrl for HTTPS Connection
     * agentManager.url.1=jamsa07-i152259:5001
     *
     * @param agentProfileFile
     * @param AgentMachineId
     * @param emHostOriginal
     * @param portOriginal
     * @param replaceEmHost
     * @param replaceemPort
     */
    public void setAgentHttpsUrl(String agentProfileFile, String AgentMachineId,
        String emHostOriginal, String portOriginal, String replaceEmHost, String replaceemPort) {
        backupFile(agentProfileFile, agentProfileFile + "_backup", AgentMachineId);
        replaceProp("agentManager.url.1=" + emHostOriginal + ":" + portOriginal,
            "agentManager.url.1=https://" + replaceEmHost + ":" + replaceemPort, AgentMachineId,
            agentProfileFile);
    }

    /**
     * This is to change the default agent AgentUrl for SSL Connection
     * agentManager.url.1=tuuja01-i152259:5001
     * agentManager.url.1=ssl://tuuja01-i152259:5443
     * 
     * @param agentProfileFile
     * @param AgentMachineId
     * @param emHostOriginal
     * @param portOriginal
     * @param replaceEmHost
     * @param replaceemPort
     */
    public void setAgentSSLUrl(String agentProfileFile, String AgentMachineId,
        String emHostOriginal, String portOriginal, String replaceEmHost, String replaceemPort) {
        backupFile(agentProfileFile, agentProfileFile + "_backup", AgentMachineId);
        replaceProp("agentManager.url.1=" + emHostOriginal + ":" + portOriginal,
            "agentManager.url.1=ssl://" + replaceEmHost + ":" + replaceemPort, AgentMachineId,
            agentProfileFile);
    }

    /**
     * Revert the agent profile
     * 
     * @param agentProfileFile
     * @param AgentMachineId
     */
    public void revertTomcatAgentProfile(String agentProfileFile, String AgentMachineId) {
        deleteFile(agentProfileFile, AgentMachineId);
        moveFile(agentProfileFile + "_backup", agentProfileFile, AgentMachineId);
    }
    
       /**
     * Update the EM properties required for HTTPS
     * 
     * @param roleIds
     */
    public void updateEMPropertiesForHTTPS(List<String> roleIds) {

        List<String> list = new ArrayList<String>();
        list.add("introscope.enterprisemanager.enabled.channels=channel1,channel2");

        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String emJetty =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "/em-jetty-config.xml";
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);

            /**
             * EM Jetty file changes
             */
            addHTTPEntryInEMJetty(emJetty, MACHINE_ID);


            /**
             * Replace EM properties
             */
            replaceProp(
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                MACHINE_ID, configFile);
            replaceProp("introscope.enterprisemanager.enabled.channels=channel1",
                "#introscope.enterprisemanager.enabled.channels=channel1", MACHINE_ID, configFile);
            replaceProp("introscope.enterprisemanager.needclientauth.channel2=false",
                "introscope.enterprisemanager.needclientauth.channel2=true", MACHINE_ID, configFile);
            replaceProp(
                "#introscope.enterprisemanager.truststore.channel2=internal/server/keystore",
                "introscope.enterprisemanager.truststore.channel2=internal/server/keystore",
                MACHINE_ID, configFile);
            replaceProp("#introscope.enterprisemanager.trustpassword.channel2=password",
                "introscope.enterprisemanager.trustpassword.channel2=password", MACHINE_ID,
                configFile);
            appendProp(list, MACHINE_ID, configFile);
        }
    }

    /**
	 * Update the EM properties required for HTTPS
	 * 
	 * @param roleIds
	 */
	public void updateEMPropertiesForHTTPS(List<String> roleIds,
			String customPort) {

		List<String> list = new ArrayList<String>();
		list.add("introscope.enterprisemanager.enabled.channels=channel1,channel2");

		for (String roleId : roleIds) {
			String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
			String emJetty = envProperties.getRolePropertyById(roleId,
					DeployEMFlowContext.ENV_EM_CONFIG_DIR)
					+ "/em-jetty-config.xml";
			String configFile = envProperties.getRolePropertyById(roleId,
					DeployEMFlowContext.ENV_EM_CONFIG_FILE);

			/**
			 * EM Jetty file changes
			 */
			addHTTPEntryInEMJetty(emJetty, MACHINE_ID);

			/**
			 * Replace EM properties
			 */
			replaceProp(
					"#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
					"introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
					MACHINE_ID, configFile);
			replaceProp(
					"introscope.enterprisemanager.enabled.channels=channel1",
					"#introscope.enterprisemanager.enabled.channels=channel1",
					MACHINE_ID, configFile);
			replaceProp(
					"introscope.enterprisemanager.needclientauth.channel2=false",
					"introscope.enterprisemanager.needclientauth.channel2=true",
					MACHINE_ID, configFile);
			replaceProp(
					"#introscope.enterprisemanager.truststore.channel2=internal/server/keystore",
					"introscope.enterprisemanager.truststore.channel2=internal/server/keystore",
					MACHINE_ID, configFile);
			replaceProp(
					"#introscope.enterprisemanager.trustpassword.channel2=password",
					"introscope.enterprisemanager.trustpassword.channel2=password",
					MACHINE_ID, configFile);
			appendProp(list, MACHINE_ID, configFile);

		}

		updateEmJettyConfigXmlSecureAttributes(roleIds);

		for (String roleID : roleIds){
			replaceProp(
					"<Set name=\"port\">8444</Set>",
					"<Set name=\"port\">" + customPort + "</Set>",
					envProperties.getMachineIdByRoleId(roleID),
					envProperties.getRolePropertyById(roleID,
							DeployEMFlowContext.ENV_EM_CONFIG_DIR)
							+ "/em-jetty-config.xml");
		}
		
	}
	
    /**
     * Update the agent properties required for HTTPS
     */
    public void updateTomcatPropertiesForHTTPS(List<String> roleIds) {
        if (windows)
            keyStoreDir = "S:\\\\";
        else
            keyStoreDir = "/opt/";
        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String tomcatProfile =
                envProperties.getRolePropertyById(roleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                    + "/wily/core/config/IntroscopeAgent.profile";
            replaceProp("#agentManager.trustStore.1=", "agentManager.trustStore.1=" + keyStoreDir
                + "keystore", MACHINE_ID, tomcatProfile);
            replaceProp("#agentManager.trustStorePassword.1=",
                "agentManager.trustStorePassword.1=password", MACHINE_ID, tomcatProfile);
            replaceProp("#agentManager.keyStore.1=", "agentManager.keyStore.1=" + keyStoreDir
                + "keystore", MACHINE_ID, tomcatProfile);
            replaceProp("#agentManager.keyStorePassword.1=",
                "agentManager.keyStorePassword.1=password", MACHINE_ID, tomcatProfile);

        }
    }

    /**
     * Update the EM properties required for SSL
     * 
     * @param roleIds
     */
    public void updateEMPropertiesForSSL(List<String> roleIds) {

        updateEMPropertiesForHTTPS(roleIds);

    }

    /**
     * Update the agent properties required for SSL
     */
    public void updateTomcatPropertiesForSSL(List<String> roleIds) {

        updateTomcatPropertiesForHTTPS(roleIds);

    }

    public void copyKeyStoreToAgent(String emRoleId, String tomcatRoleId) {
        if (windows)
            keyStoreDir = "C:\\";
        else
            keyStoreDir = "/opt/";
        keyStoreDir = keyStoreDir.replace(":", "$");
        LOGGER.info(keyStoreDir);
        String configDir =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        String tomcatHost = envProperties.getMachineHostnameByRoleId(tomcatRoleId);
        String tomcatMachineId = envProperties.getMachineIdByRoleId(tomcatRoleId);
        if (windows) {
            List<String> nfsClientWindowsCommandMom = new ArrayList<String>();

            nfsClientWindowsCommandMom.add("net use s: \\\\"
                + envProperties.getMachineHostnameByRoleId(emRoleId)
                + "\\keystore_EM /user:administrator Lister@123");
            FailoverModifierFlowContext FOM =
                FailoverModifierFlowContext.remoteMount(nfsClientWindowsCommandMom, "/");

            runFlowByMachineId(tomcatMachineId, FailoverModifierFlow.class, FOM);
        } else {
            utility.copyToRemoteMachine(tomcatHost, "root", "Lister@123", configDir
                + "internal/server/keystore", keyStoreDir);
        }
    }

    /**
     * Specify roles and update the secure attributes
     * 
     * @param roleIds
     */
    public void updateEmJettyConfigXmlSecureAttributes(List<String> roleIds) {

        for (String roleId : roleIds) {
            String emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            String searchExpr1 =
                "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"validateCertificates\"]";
            String searchExpr2 =
                "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"needClientAuth\"]";
            String searchExpr3 =
                "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"verifyHostnames\"]";
            try {
                List<String> args = new ArrayList<String>();
                args.add(emJettyFile);
                args.add(searchExpr1 + ":::" + searchExpr2 + ":::" + searchExpr3);
                args.add("false");
                args.add("true");
                XMLModifierFlowContext modifyXML =
                    new XMLModifierFlowContext.Builder().arguments(args)
                        .methodName("xmlFileUtil.updateXmlFile").build();
                runFlowByMachineId(envProperties.getMachineIdByRoleId(roleId),
                    XMLModifierFlow.class, modifyXML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
