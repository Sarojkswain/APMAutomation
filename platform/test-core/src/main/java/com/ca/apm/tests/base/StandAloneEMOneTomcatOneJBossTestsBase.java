package com.ca.apm.tests.base;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.flow.webapp.jboss.DeployJbossFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;

public class StandAloneEMOneTomcatOneJBossTestsBase extends
		AgentControllabilityConstants {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StandAloneEMOneTomcatOneJBossTestsBase.class);
	public List<String> roleIds = new ArrayList<String>();

	public final String emHost;
	public final String emLibDir;
	public final String user;
	public final String password;
	public final String tomcatAgentExpression;
	public final String emLogDir;
	public final String emLogFile;
	public final String tomcatHost;
	public final String jbossHost;
	public final String clwJarFileLoc;
	public final String emPort;
	public final String tomcatAgentProfileFile;
	public final String tomcatAgentProfileFile_backup;
	public final String jbossAgentProfileFile;
	public final String jbossAgentProfileFile_backup;
	public final String emSecureWebPort;
	public final String tomcatAgentExp;
	public final String jbossAgentExp;
	public final String emConfigDir;
	public final String emConfigFile;
	public final String emConfigFile_backup;
	public final String emdomainsxmlpath;
	public final String emdomainsxmlpath_backup;
	public final String emusersxmlpath;
	public final String emusersxmlpath_backup;
	public final String emserverxmlpath;
	public final String emserverxmlpath_backup;
	public final String emrealmsxmlpath;
	public final String emrealmsxmlpath_backup;
	public final String metricshutoffconfxmlpath;
	public final String emMachineId;
	public final String tomcatMachineId;
	public final String jbossMachineId;
	public final String domainconfigdynamicupdateprop;
	public String tomcatAgentLogFile;
	public String jbossAgentLogFile;
	public String testcaseId, testCaseNameIDPath;
	public final String emHome;
	public final String userliteral, nametag, pwdliteral, guestUser,
			guestPassword, agenttag, superdomainliteral, mappingtag, granttag,
			grouptag, domaintag;
	public final String tomcatAgentExpPath = "(.*SuperDomain.*)|(.*)|Tomcat|(.*Tomcat.*)";
	public final String jbossAgentExpPath = "(.*SuperDomain.*)|(.*)|JBoss|(.*JBoss.*)";

	public StandAloneEMOneTomcatOneJBossTestsBase() {

		emMachineId = EM_MACHINE_ID;
		tomcatMachineId = TOMCAT_MACHINE_ID;
		jbossMachineId = TOMCAT_MACHINE_ID;
		emSecureWebPort = ApmbaseConstants.emSecureWebPort;
		tomcatAgentExp = ".*Tomcat.*";
		jbossAgentExp = ".*JBoss.*";
		tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
		emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty(
				"emPort");
		tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);
		jbossHost = envProperties.getMachineHostnameByRoleId(JBOSS_ROLE_ID);
		emHost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
		emLibDir = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LIB_DIR);
		emLogDir = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LOG_DIR);
		emLogFile = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		emConfigDir = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		emConfigFile = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		emHome = envProperties.getRolePropertyById(EM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR);
		emConfigFile_backup = emConfigFile + "_backup";
		emdomainsxmlpath = emConfigDir + "/domains.xml";
		emdomainsxmlpath_backup = emdomainsxmlpath + "_backup";
		emusersxmlpath = emConfigDir + "users.xml";
		emusersxmlpath_backup = emusersxmlpath + "_backup";
		emserverxmlpath = emConfigDir + "/server.xml";
		emserverxmlpath_backup = emserverxmlpath + "_backup";
		emrealmsxmlpath = emConfigDir + "/realms.xml";
		emrealmsxmlpath_backup = emrealmsxmlpath + "_backup";
		metricshutoffconfxmlpath = emConfigDir
				+ "/shutoff/MetricShutoffConfiguration.xml";
		clwJarFileLoc = emLibDir + "CLWorkstation.jar";
		tomcatAgentProfileFile = envProperties.getRolePropertyById(
				TOMCAT_ROLE_ID, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		tomcatAgentProfileFile_backup = tomcatAgentProfileFile + "_backup";
		tomcatAgentLogFile = envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		jbossAgentLogFile = envProperties.getRolePropertyById(JBOSS_ROLE_ID,
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		jbossAgentProfileFile = envProperties.getRolePropertyById(
				JBOSS_ROLE_ID, DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		jbossAgentProfileFile_backup = jbossAgentProfileFile + "_backup";
		user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;
		domainconfigdynamicupdateprop = ApmbaseConstants.DOMAINCONFIG_DYNAMICUPDATE_PROPERTY;
		userliteral = ApmbaseConstants.USER_LITERAL;
		nametag = ApmbaseConstants.NAME_TAG;
		pwdliteral = ApmbaseConstants.PWD_LITERAL;
		guestUser = ApmbaseConstants.guestUser;
		guestPassword = ApmbaseConstants.guestPassw;
		agenttag = ApmbaseConstants.AGENT_TAG;
		superdomainliteral = ApmbaseConstants.SUPERDOMAIN_LITERAL;
		mappingtag = ApmbaseConstants.MAPPING_TAG;
		granttag = ApmbaseConstants.GRANT_TAG;
		grouptag = ApmbaseConstants.GROUP_TAG;
		domaintag = ApmbaseConstants.DOMAIN_TAG;
		roleIds.add(EM_ROLE_ID);
		roleIds.add(TOMCAT_ROLE_ID);
		roleIds.add(JBOSS_ROLE_ID);
	}

	/**
	 * This methods syncs the time on all the machines in the testbed
	 */
	public void syncMachines() {
		List<String> machines = new ArrayList<String>();
		machines.add(emMachineId);
		machines.add(tomcatMachineId);
		syncTimeOnMachines(machines);

	}

	/**
	 * Starts the EM of the testbed
	 */
	public void startEM() {
		try {
			startEM(EM_ROLE_ID);
			LOGGER.info("EM is started");
		} catch (Exception e) {
			LOGGER.error("EM failed to start");
			Assert.assertTrue(false);
			e.printStackTrace();
		}
	}

	/**
	 * Stops the EM EM of the testbed
	 */
	public void stopEM() {
		stopEM(EM_ROLE_ID);
		stopEMServiceFlowExecutor(emMachineId);
		harvestWait(10);
	}

	/**
	 * Restarts the EM of the testbed
	 */
	public void restartEM() {
		restartEM(EM_ROLE_ID);
	}

	/**
	 * Starts the Tomcat Agent of the testbed
	 */
	public void startTomcatAgent() {
		try {
			startTomcatAgent(TOMCAT_ROLE_ID);
		} catch (Exception e) {
			LOGGER.error("Tomcat Agent failed to start");
			Assert.assertTrue(false);
			e.printStackTrace();
		}
	}

	/**
	 * Starts the JBoss Agent of the testbed
	 */
	public void startJBossAgent() {
		try {
			startJBossAgent(JBOSS_ROLE_ID);
		} catch (Exception e) {
			LOGGER.error("JBoss Agent failed to start");
			Assert.assertTrue(false);
			e.printStackTrace();
		}
	}

	/**
	 * Start all agents in the testbed
	 */
	public void startAgents() {

		startTomcatAgent();
		startJBossAgent();
		harvestWait(60);

	}

	/**
	 * Stops the Tomcat Agent of the testbed
	 */

	public void stopTomcatAgent() {
		stopTomcatAgent(TOMCAT_ROLE_ID);
		stopTomcatServiceFlowExecutor(envProperties
				.getMachineIdByRoleId(TOMCAT_ROLE_ID));
	}

	/**
	 * Stops the JBoss Agent of the testbed
	 */

	public void stopJBossAgent() {
		stopJBossAgent(JBOSS_ROLE_ID);
		stopJBossServiceFlowExecutor(envProperties
				.getMachineIdByRoleId(JBOSS_ROLE_ID));
	}

	/**
	 * Stops all the Agents of the testbed
	 */
	public void stopAgents() {
		stopTomcatAgent();
		stopJBossAgent();
	}

	/**
	 * Starts all the components EMs and Agent of the testbed
	 */
	public void startTestBed() {
		startEM();
		startAgents();
		LOGGER.info("All the components of the Testbed are started");
	}

	/**
	 * Stops all the components EMs and Agent of the testbed
	 */
	public void stopTestBed() {
		stopEM();
		stopAgents();
	}

	/**
	 * Checks the EM log of the testbed for the specified string message
	 * 
	 * @param message
	 */
	public void checkEMLogForMsg(String message) {
		checkLogForMsg(envProperties, emMachineId, emLogFile, message);
	}

	/**
	 * Checks the Tomcat Agent log of the testbed for the specified string
	 * message
	 * 
	 * @param message
	 */
	public void checkTomcatAgentLogForMsg(String message) {
		checkLogForMsg(envProperties, tomcatMachineId, tomcatAgentLogFile,
				message);

	}

	/**
	 * Checks the JBoss Agent log of the testbed for the specified string
	 * message
	 * 
	 * @param message
	 */
	public void checkJBossAgentLogForMsg(String message) {
		checkLogForMsg(envProperties, jbossMachineId, jbossAgentLogFile,
				message);

	}

	/**
	 * Replaces the specified string with the given string in EM configuration
	 * file
	 * 
	 * @param findStr
	 * @param replaceStr
	 */
	public void replaceEMProperty(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, emMachineId, emConfigFile);
	}

	/**
	 * Replaces the specified string with the given string in EM relam.xml file
	 * 
	 * @param filepath
	 * @param findStr
	 * @param replaceStr
	 */
	public void replaceEMrelamXml(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, emMachineId, emrealmsxmlpath);
	}

	/**
	 * Replaces the specified string with the given string in TomcatAgent
	 * configuration file
	 * 
	 * @param findStr
	 * @param replaceStr
	 */
	public void replaceTomcatAgentProperty(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, tomcatMachineId,
				tomcatAgentProfileFile);
	}

	/**
	 * Replaces the specified string with the given string in JbossAgent
	 * configuration file
	 * 
	 * @param findStr
	 * @param replaceStr
	 */
	public void replaceJBossAgentProperty(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, jbossMachineId, jbossAgentProfileFile);
	}

	public void backupEMFiles() {
		backupFile(emConfigFile, emConfigFile_backup, emMachineId);
		backupFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
		backupFile(emusersxmlpath, emusersxmlpath_backup, emMachineId);
		backupFile(emserverxmlpath, emserverxmlpath_backup, emMachineId);
		backupFile(emrealmsxmlpath, emrealmsxmlpath_backup, emMachineId);

	}

	public void backupAgentFiles() {
		backupFile(jbossAgentProfileFile, jbossAgentProfileFile_backup,
				jbossMachineId);
		backupFile(tomcatAgentProfileFile, tomcatAgentProfileFile_backup,
				tomcatMachineId);
	}

	public void revertEMFiles() {
		revertFile(emConfigFile, emConfigFile_backup, emMachineId);
		revertFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
		revertFile(emusersxmlpath, emusersxmlpath_backup, emMachineId);
		revertFile(emserverxmlpath, emserverxmlpath_backup, emMachineId);
		revertFile(emrealmsxmlpath, emrealmsxmlpath_backup, emMachineId);
	}

	public void revertEMFiles(String testCaseId) {

		List<String> filesList = new ArrayList<String>();
		filesList.add(emdomainsxmlpath);
		filesList.add(emusersxmlpath);
		filesList.add(emserverxmlpath);
		filesList.add(emrealmsxmlpath);
		filesList.add(emConfigFile);
		for (int i = 0; i < filesList.size(); i++) {
			try {
				copyFile(filesList.get(i), filesList.get(i) + "_" + testCaseId,
						emMachineId);
			} catch (Exception e) {
				LOGGER.info("Unable to copy the file.....");
				Assert.assertTrue(false);

			}
		}
		revertFile(emConfigFile, emConfigFile_backup, emMachineId);
		revertFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
		revertFile(emusersxmlpath, emusersxmlpath_backup, emMachineId);
		revertFile(emserverxmlpath, emserverxmlpath_backup, emMachineId);
		revertFile(emrealmsxmlpath, emrealmsxmlpath_backup, emMachineId);
	}
	
	

	public void revertAgentFiles() {
		revertFile(jbossAgentProfileFile, jbossAgentProfileFile_backup,
				jbossMachineId);
		revertFile(tomcatAgentProfileFile, tomcatAgentProfileFile_backup,
				tomcatMachineId);
	}

	public void renameEMLogFile(String testcaseId) {
		renameFile(emLogFile, emLogFile + "_" + testcaseId, emMachineId);
	}

	public void renameTomcatAgentLogFile(String testcaseId) {
		renameFile(tomcatAgentLogFile, tomcatAgentLogFile + "_tomcat_"
				+ testcaseId, tomcatMachineId);
	}

	public void renameJBossAgentLogFile(String testcaseId) {
		renameFile(jbossAgentLogFile, jbossAgentLogFile + "_jboss_"
				+ testcaseId, jbossMachineId);
	}
	
	public void backupConfigs() {
        roleIds.clear();
        roleIds.add(EM_ROLE_ID);
        roleIds.add(TOMCAT_ROLE_ID);
        roleIds.add(JBOSS_ROLE_ID);
        backupConfigDir(roleIds);
    }

    public void revertConfigAndRenameLogsWithTestId(String testCaseId) {
        roleIds.clear();
        roleIds.add(EM_ROLE_ID);
        roleIds.add(JBOSS_ROLE_ID);
        roleIds.add(TOMCAT_ROLE_ID);
        renameDirWithTestCaseId(roleIds, testCaseId);
        restoreConfigDir(roleIds);
    }
    
    public void revertConfigAndRenameLogsWithTestId(String testCaseId, List<String> roleIds) {
        renameDirWithTestCaseId(roleIds, testCaseId);
        restoreConfigDir(roleIds);
    }
	
	
}
