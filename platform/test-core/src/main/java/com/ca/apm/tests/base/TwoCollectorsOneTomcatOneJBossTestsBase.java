package com.ca.apm.tests.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.flow.webapp.jboss.DeployJbossFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;

public class TwoCollectorsOneTomcatOneJBossTestsBase extends
		AgentControllabilityConstants {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TwoCollectorsOneTomcatOneJBossTestsBase.class);
	CLWCommons clw = new CLWCommons();
	public final String momHost;
	public String momHostIP;
	public final String configFileMom;
	public final String configFileC1;
	public final String configFileC2;
	public final String user;
	public final String password;
	public final String AgentExpression;
	public final String tomcatAgentExpression;
	public final String MetricExpression;
	public final String loadBalanceFile;
	public final String momlogFile;
	public final String momLibDir;
	public final String collector1logFile;
	public final String collector2logFile;
	public final String loadBalanceFile_Backup;
	public final String collector1Port;
	public final String collector1Host;
	public String collector1HostIP;
	public final String collector2Host;
	public final String collector2Port;
	public final String tomcatHost;
	public final String jbossHost;
	public final String clwJarFileLoc;
	public final String momWebPort;
	public final String momSecureWebPort;
	public final String momConfigDir;
	public final String col1ConfigDir;
	public final String col2ConfigDir;
	public final String momHome;
	public final String c1WebPort;
	public final String momPort;
	public final String tomcatAgentProfileFile;
	public final String tomcatAgentProfileFile_backup;
	public final String jbossAgentProfileFile;
	public final String jbossAgentProfileFile_backup;
	public final String emSecureWebPort;
	public final String configFileMom_backup;
	public final String configFileC1_backup;
	public final String configFileC2_backup;
	public final String tomcatAgentExp;
	public final String jbossAgentExp;
	public String tomcatAgentLogFile;
	public String jbossAgentLogFile;
	public ApmbaseUtil apmbaseutil;
	public final String momlogDir;
	public String dashboardName, managementModuleName, managementModuleJAR,
			tomcatPort, tomcatAgentURL, metricshutoffconfxmlpath,
			tomcatAgentProcess, jbossAgentProcess;
	protected List<String> roleIds = new ArrayList<String>();

	public TwoCollectorsOneTomcatOneJBossTestsBase() {
		emSecureWebPort = ApmbaseConstants.emSecureWebPort;
		AgentExpression = "\".*\\|.*\\|.*\"";
		tomcatAgentExp = ".*Tomcat.*";
		tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
		jbossAgentExp = ".*JBoss.*";
		MetricExpression = ".*CPU.*";
		momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty(
				"emPort");
		collector1Port = envProperties
				.getRolePropertiesById(COLLECTOR1_ROLE_ID)
				.getProperty("emPort");
		momSecureWebPort = ApmbaseConstants.emSecureWebPort;
		momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID)
				.getProperty("emWebPort");
		c1WebPort = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID)
				.getProperty("emWebPort");

		collector1Host = envProperties
				.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
		collector2Host = envProperties
				.getMachineHostnameByRoleId(COLLECTOR2_ROLE_ID);

		collector2Port = envProperties
				.getRolePropertiesById(COLLECTOR2_ROLE_ID)
				.getProperty("emPort");

		try {
			collector1HostIP = returnIPforGivenHost(collector1Host);
		} catch (IOException e) {
			e.printStackTrace();
		}

		tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);
		jbossHost = envProperties.getMachineHostnameByRoleId(JBOSS_ROLE_ID);

		loadBalanceFile = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/loadbalancing.xml";
		loadBalanceFile_Backup = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/loadbalancing_backup.xml";
		momHost = envProperties.getMachineHostnameByRoleId(MOM_ROLE_ID);

		try {
			momHostIP = returnIPforGivenHost(momHost);
		} catch (IOException e) {
			e.printStackTrace();
		}

		momHome = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR);
		momLibDir = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LIB_DIR);

		momlogFile = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		momConfigDir = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		momlogDir = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_LOG_DIR);
		col1ConfigDir = envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		col2ConfigDir = envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		collector1logFile = envProperties.getRolePropertyById(
				COLLECTOR1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);

		collector2logFile = envProperties.getRolePropertyById(
				COLLECTOR2_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);

		configFileMom = envProperties.getRolePropertyById(MOM_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		configFileMom_backup = configFileMom + "_backup";
		configFileC1 = envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		configFileC1_backup = configFileC1 + "_backup";

		configFileC2 = envProperties.getRolePropertyById(COLLECTOR2_ROLE_ID,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		configFileC2_backup = configFileC2 + "_backup";

		clwJarFileLoc = momLibDir + "CLWorkstation.jar";
		tomcatAgentProfileFile = envProperties.getRolePropertyById(
				TOMCAT_ROLE_ID, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		tomcatAgentProfileFile_backup = tomcatAgentProfileFile + "_backup";

		tomcatAgentLogFile = envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";

		jbossAgentProfileFile = envProperties.getRolePropertyById(
				JBOSS_ROLE_ID, DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		jbossAgentProfileFile_backup = jbossAgentProfileFile + "_backup";

		jbossAgentLogFile = envProperties.getRolePropertyById(JBOSS_ROLE_ID,
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.JBoss_Agent.log";

		apmbaseutil = new ApmbaseUtil();
		user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;
		dashboardName = ApmbaseConstants.WELCOME_DASHBOARD;
		managementModuleName = ApmbaseConstants.DEFAULT_MM;
		managementModuleJAR = ApmbaseConstants.DEFAULT_MM_JAR;
		tomcatAgentProcess = "Tomcat";
		jbossAgentProcess = "JBoss";
		tomcatPort = "9091";
		tomcatAgentURL = "http://" + tomcatHost + ":" + tomcatPort
				+ "/qa-app/transactiontraces/RecursiveDuration.jsp";
		metricshutoffconfxmlpath = momConfigDir
				+ "/shutoff/MetricShutoffConfiguration.xml";

	}

	@BeforeSuite(alwaysRun = true)
	public void initialize() {
		List<String> machines = new ArrayList<String>();
		machines.add(MOM_MACHINE_ID);
		machines.add(COLLECTOR1_MACHINE_ID);
		machines.add(COLLECTOR2_MACHINE_ID);
		syncTimeOnMachines(machines);
		backupConfigs();
		// backupFile(configFileMom, configFileMom_backup, MOM_MACHINE_ID);
		// backupFile(loadBalanceFile, loadBalanceFile_Backup, MOM_MACHINE_ID);
		// backupFile(configFileC1, configFileC1_backup, COLLECTOR1_MACHINE_ID);
		// backupFile(configFileC2, configFileC2_backup, COLLECTOR2_MACHINE_ID);
		// backupFile(tomcatAgentProfileFile, tomcatAgentProfileFile_backup,
		// COLLECTOR1_MACHINE_ID);
		// backupFile(jbossAgentProfileFile, jbossAgentProfileFile_backup,
		// COLLECTOR2_MACHINE_ID);

	}

	/**
	 * Creates a List of all EMs of the Testbed
	 * 
	 * @return emRoles
	 */
	public List<String> getemRoles() {
		List<String> emRoles = new ArrayList<String>();
		emRoles.add(MOM_ROLE_ID);
		emRoles.add(COLLECTOR1_ROLE_ID);
		emRoles.add(COLLECTOR2_ROLE_ID);

		return emRoles;

	}

	/**
	 * Stops all the EMs of the testbed
	 */
	public void stopEMServices() {
		stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
		stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
		stopCollectorEM(MOM_ROLE_ID, COLLECTOR2_ROLE_ID);
		stopEMServiceFlowExecutor(COLLECTOR2_MACHINE_ID);
		stopEM(MOM_ROLE_ID);
		stopEMServiceFlowExecutor(MOM_MACHINE_ID);
		harvestWait(10);
	}

	/**
	 * Stops all the EMs and Agents of the testbed
	 */

	public void stopServices() {
		stopAgents();
		stopEMServices();
		harvestWait(5);
	}

	/**
	 * Starts the Collector EMs of the testbed
	 */

	public void startEMCollectors() {
		startEM(COLLECTOR1_ROLE_ID);
		startEM(COLLECTOR2_ROLE_ID);
	}

	/**
	 * Starts the MoM EM of the testbed
	 */
	public void startMoM() {
		try {
			startEM(MOM_ROLE_ID);
			LOGGER.info("MoM is started");
		} catch (Exception e) {
			LOGGER.error("MoM failed to start");
			Assert.assertTrue(false);
			e.printStackTrace();
		}
	}

	/**
	 * Stops the MoM EM of the testbed
	 */
	public void stopMoM() {
		stopEM(MOM_ROLE_ID);
		stopEMServiceFlowExecutor(MOM_MACHINE_ID);
		harvestWait(10);
	}

	/**
	 * Starts the JBoss Agent of the testbed
	 */
	public void startJBossAgent() {
		startJBossAgent(JBOSS_ROLE_ID);
	}

	/**
	 * Stops the JBoss Agent of the testbed
	 */
	public void stopJBossAgent() {
		stopJBossAgent(JBOSS_ROLE_ID);
	}

	public void startTomcatAgent() {
		startTomcatAgent(TOMCAT_ROLE_ID);
	}

	/**
	 * Stops the Tomcat Agent of the testbed
	 */
	public void stopTomcatAgent() {
		stopTomcatServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
	}

	/**
	 * Starts the Tomcat and JBoss Agent of the testbed
	 */
	public void startAgents() {
		startTomcatAgent();
		startJBossAgent();

	}

	/**
	 * Stops the Tomcat and JBoss Agent of the testbed
	 */
	public void stopAgents() {
		stopTomcatAgent();
		stopJBossAgent();
	}

	/**
	 * Starts all the components EMs and Agents of the testbed
	 */
	public void startTestBed() {
		startEMCollectors();
		startMoM();
		startAgents();
		LOGGER.info("All the components of the Testbed are started");
	}

	/**
	 * Stops all the components EMs and Agent of the testbed
	 */
	public void stopTestBed() {
		stopEMServices();
		stopAgents();
	}

	/**
	 * Starts the EMs of the testbed
	 */
	public void startEMServices() {
		startEMCollectors();
		startMoM();
	}

	/**
	 * Restarts the MoM EM of the testbed
	 */
	public void restartMOM() {
		restartEM(MOM_ROLE_ID);
	}

	/**
	 * Restarts the Collector1 EM of the testbed
	 */
	public void restartCollector1() {
		restartEM(COLLECTOR1_ROLE_ID);
	}

	/**
	 * Restarts the Collector2 EM of the testbed
	 */
	public void restartCollector2() {
		restartEM(COLLECTOR2_ROLE_ID);
	}

	/**
	 * Restarts the Collector EMs of the testbed
	 */
	public void restartCollectors() {
		restartCollector1();
		restartCollector2();
	}

	/**
	 * Restarts the TomcatAgent of the Testbed
	 */
	public void restartTomcatAgent() {
		stopTomcatAgent();
		startTomcatAgent();
	}

	/**
	 * Check for existence of JBoss Agent log
	 */
	public void checkJBossAgentLogExistence() {
		checkFileExistence(envProperties, COLLECTOR2_MACHINE_ID,
				jbossAgentLogFile);
	}

	/**
	 * Checks the MoM log of the testbed for the specified string message
	 * 
	 * @param message
	 */
	public void checkMoMLogForMsg(String message) {
		checkLogForMsg(envProperties, MOM_MACHINE_ID, momlogFile, message);
	}

	/**
	 * Checks the MoM log of the testbed for the specified string message is not
	 * available
	 * 
	 * @param message
	 */
	public void checkMoMLogForNoMsg(String message) {
		checkLogForNoMsg(envProperties, MOM_MACHINE_ID, momlogFile, message);
	}

	/**
	 * Checks the Collector1 log of the testbed for the specified string message
	 * 
	 * @param message
	 */
	public void checkColl1LogForMsg(String message) {
		checkLogForMsg(envProperties, COLLECTOR1_MACHINE_ID, collector1logFile,
				message);
	}

	/**
	 * Checks the Collector1 log of the testbed for the specified string message
	 * 
	 * @param message
	 */
	public void checkColl2LogForMsg(String message) {
		checkLogForMsg(envProperties, COLLECTOR2_MACHINE_ID, collector2logFile,
				message);
	}

	/**
	 * Checks the Agent log of the testbed for the specified string message
	 * 
	 * @param message
	 */
	public void checkTomcatAgentLogForMsg(String message) {
		checkLogForMsg(envProperties, COLLECTOR1_MACHINE_ID,
				tomcatAgentLogFile, message);

	}

	/**
	 * Checks the Agent log of the testbed for the specified string message
	 * 
	 * @param message
	 */
	public void checkJBossAgentLogForMsg(String message) {
		checkLogForMsg(envProperties, COLLECTOR2_MACHINE_ID, jbossAgentLogFile,
				message);

	}

	/**
	 * Replaces the specified string with the given string in MoM EM
	 * configuration file
	 * 
	 * @param message
	 */
	public void replaceMoMProperty(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, MOM_MACHINE_ID, configFileMom);
	}

	/**
	 * Replaces the specified string with the given string in Collector1 EM
	 * configuration file
	 * 
	 * @param findStr
	 * @param replaceStr
	 */
	public void replaceCollector1Property(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, COLLECTOR1_MACHINE_ID, configFileC1);
	}

	/**
	 * Replaces the specified string with the given string in Collector2 EM
	 * configuration file
	 * 
	 * @param findStr
	 * @param replaceStr
	 */
	public void replaceCollector2Property(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, COLLECTOR2_MACHINE_ID, configFileC2);
	}

	/**
	 * Replaces the specified string with the given string in Tomcat Agent
	 * configuration file
	 * 
	 * @param message
	 */
	public void replaceTomcatAgentProperty(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, COLLECTOR1_MACHINE_ID,
				tomcatAgentProfileFile);
	}

	/**
	 * Replaces the specified string with the given string in Tomcat Agent
	 * configuration file
	 * 
	 * @param message
	 */
	public void replaceJBossAgentProperty(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, COLLECTOR2_MACHINE_ID,
				jbossAgentProfileFile);
	}

	/**
	 * Checks if the Collector of the testbed connected to MOM
	 */
	public void checkCollectorsToMOMConnectivity() {
		checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*",
				MetricExpression, momHost, momPort, momLibDir);
		checkSpecificCollectorToMOMConnectivity(".*" + collector2Host + ".*",
				MetricExpression, momHost, momPort, momLibDir);
	}

	/**
	 * Enabled the debug mode of Logging for MoM EM
	 * 
	 */
	public void enableMoMDebugLog() {
		replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
				"log4j.logger.Manager=DEBUG, console, logfile");
	}

	public void backupConfigs() {
		roleIds.clear();
		roleIds.add(MOM_ROLE_ID);
		roleIds.add(COLLECTOR1_ROLE_ID);
		roleIds.add(COLLECTOR2_ROLE_ID);
		roleIds.add(TOMCAT_ROLE_ID);
		roleIds.add(JBOSS_ROLE_ID);
		backupConfigDir(roleIds);
		// backupPropFiles(roleIds);
		// roleIds.clear();
		// roleIds.add(MOM_ROLE_ID);
		// roleIds.add(COLLECTOR1_ROLE_ID);
		// backupEMJettyFiles(roleIds);
		// backupEMLoadBalancingXMLFiles(roleIds);
	}

	public void revertConfigs(String testCaseId) {
		roleIds.clear();
		roleIds.add(MOM_ROLE_ID);
		roleIds.add(COLLECTOR1_ROLE_ID);
		roleIds.add(COLLECTOR2_ROLE_ID);
		roleIds.add(TOMCAT_ROLE_ID);
		roleIds.add(JBOSS_ROLE_ID);
		renameDirWithTestCaseId(roleIds, testCaseId);
		restoreConfigDir(roleIds);
		// renamePropertyFilesWithTestCaseId(roleIds, testCaseId);
		// restorePropFiles(roleIds);
		// roleIds.clear();
		// roleIds.add(MOM_ROLE_ID);
		// roleIds.add(COLLECTOR1_ROLE_ID);
		// roleIds.add(COLLECTOR2_ROLE_ID);
		// renameLogWithTestCaseId(roleIds, testCaseId);
		// renameEMJettyFilesWithTestCaseId(roleIds, testCaseId);
		// renameEMLoadBalancingXMLFilesWithTestCaseId(roleIds, testCaseId);
		// restoreEMJettyFiles(roleIds);
		// restoreEMLoadBalancingXMLFiles(roleIds);
	}

	public void revertConfigAndRenameLogsWithTestId(String testCaseId,
			List<String> roleIds) {
		renameDirWithTestCaseId(roleIds, testCaseId);
		restoreConfigDir(roleIds);
	}

	/*
	 * method returns the MachineID of the Collector to which the agent got
	 * connected
	 */
	public String getAgentConnectedColMachineId(String agentExpression) {
		String agentConnectedCollector_Host;
		String AgentConnectedEM_MachineId = null;
		List<String> collectors_List = new ArrayList<String>();
		List<Integer> collector_Port_List = new ArrayList<Integer>();
		List<String> collector_RoleIDs = new ArrayList<String>();

		collector_RoleIDs.add(COLLECTOR2_ROLE_ID);
		collector_RoleIDs.add(COLLECTOR1_ROLE_ID);

		collectors_List.add(collector2Host);
		collectors_List.add(collector1Host);

		collector_Port_List.add(Integer.parseInt(collector2Port));
		collector_Port_List.add(Integer.parseInt(collector1Port));

		agentConnectedCollector_Host = getAgentConnectedCollectorName(
				collectors_List, collector_Port_List, collector_RoleIDs,
				agentExpression, "Host", momLibDir);
		if (agentConnectedCollector_Host.equalsIgnoreCase(collector1Host))
			AgentConnectedEM_MachineId = COLLECTOR1_MACHINE_ID;

		else if (agentConnectedCollector_Host.equalsIgnoreCase(collector2Host))
			AgentConnectedEM_MachineId = COLLECTOR2_MACHINE_ID;
		else

			LOGGER.info("Agent is not connected to any of the collectors");

		return AgentConnectedEM_MachineId;

	}

	public boolean agentTurnOff(String user, String password,
			String agentProcessName, String agentExpr, boolean turnOff,
			String MACHINE_ID) {
		String turnOnOrOffResultString = "";
		turnOnOrOffResultString = "Process=\"" + agentProcessName
				+ "\" Shutoff=\"true\"";

		if (turnOff) {
			clw.turnOffAgents(user, password, agentExpr, momHost,
					Integer.parseInt(momPort), momLibDir);
			return checkForKeyword(envProperties, MACHINE_ID,
					metricshutoffconfxmlpath, turnOnOrOffResultString, true);
		} else {

			clw.turnOnAgents(user, password, agentExpr, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(agentExpr, momHost, Integer.parseInt(momPort),
					momLibDir);
			return true;
		}

	}
}
