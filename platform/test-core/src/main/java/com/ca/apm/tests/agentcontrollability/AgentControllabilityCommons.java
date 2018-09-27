package com.ca.apm.tests.agentcontrollability;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.ca.apm.commons.flow.StopServiceFlow;
import com.ca.apm.commons.flow.StopServiceFlowContext;

public class AgentControllabilityCommons extends AccConstants{
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AgentControllabilityTests.class);
	TestUtils utility = new TestUtils();
	CLWCommons clw = new CLWCommons();	
	EmUtils emUtils = utilities.createEmUtils();
	XMLUtil xmlutil = new XMLUtil();

	protected final String momRoleId;
	protected final String collector1RoleId;
	protected final String collector2RoleId;
	protected final String collector3RoleId;
	protected final String standaloneRoleId;
	protected final String tomcatRoleId;
	protected final String jbossRoleId;
	protected final String tomcat1RoleId;
	protected final String jboss1RoleId;
	protected String momMachineId;
	protected String collector1MachineId;
	protected String collector2MachineId;
	protected String collector3MachineId;
	protected String standaloneMachineId;
	protected String tomcat1MachineId;
	protected String jboss1MachineId;
	protected String tomcatMachineId;
	protected String jbossMachineId;
	protected final String momHost;
	protected final String collector1Host;
	protected final String collector2Host;
	protected final String collector3Host;
	protected final String standaloneHost;
	protected final int momPort;
	protected final int collector1Port;
	protected final int collector2Port;
	protected final int collector3Port;
	protected final int standalonePort;
	protected final String momLibDir;
	protected final String standaloneLibDir;
	protected final String momConfigdir;
	protected final String momConfigFile;
	protected final String momConfigFile_copy;
	protected final String momLogFile;
	protected final String col1ConfigFile;
	protected final String col1ConfigFile_copy;
	protected final String col2ConfigFile;
	protected final String col2ConfigFile_copy;
	protected final String col3ConfigFile;
	protected final String col3ConfigFile_copy;
	protected final String standaloneConfigFile;
	protected final String standaloneConfigFile_copy;
	protected final String col1LogFile;
	protected final String col2LogFile;
	protected final String col3LogFile;
	protected final String standaloneLogFile;
	protected final String user;
	protected final String password;
	protected final String loadBalanceFile;
	protected final String loadBalanceFile_copy;
	protected final String tomcatAgentProfile;
	protected final String tomcatAgentProfile_copy;
	protected String jbossInstallDir;
	protected final String jbossAgentProfile;
	protected final String jbossAgentProfile_copy;
	protected final String tomcat1AgentProfile;
	protected final String tomcat1AgentProfile_copy;
	protected final String jboss1AgentProfile;
	protected final String jboss1AgentProfile_copy;
	protected final String tomcatLogFile;
	protected final String jbossLogFile;
	protected final String tomcat1LogFile;
	protected final String jboss1LogFile;
	protected final String tomcatAgentExpression;
	protected final String agent_collector_name;
	protected final String usersxmlFile;
	protected final String usersxmlFile_copy;
	protected final String serverxmlFile;
	protected final String serverxmlFile_copy;	
	protected final String momapmthresholdxmlpath;
	protected final String momapmthresholdxmlpath_copy;
	protected final String standaloneapmthresholdxmlpath;
	protected final String standaloneapmthresholdxmlpath_copy;
	protected final String col1apmthresholdxmlpath;
	protected final String col1apmthresholdxmlpath_copy;
	protected final String col2apmthresholdxmlpath;
	protected final String col2apmthresholdxmlpath_copy;
	protected final String agentconnlimitxpath;
	protected final String agentmetricslivelimitxpath;
	protected final String agentevntthresholdattribute;
	protected String tempResult1, tempResult2, tempResult3, tempResult4;
	protected String testcaseId;
	protected String[] serversList ;	
	ArrayList<String> serverstoadd = new ArrayList<String>();
	List<String> tempResult = new ArrayList<String>();
	
	public AgentControllabilityCommons()
	{

		momRoleId = AccConstants.MOM_ROLE_ID;
		collector1RoleId = AccConstants.COLLECTOR1_ROLE_ID;
		collector2RoleId = AccConstants.COLLECTOR2_ROLE_ID;
		collector3RoleId = AccConstants.COLLECTOR3_ROLE_ID;
		standaloneRoleId = AccConstants.STANDALONE_ROLE_ID;
		tomcatRoleId = AccConstants.TOMCAT_ROLE_ID;
		jbossRoleId = AccConstants.JBOSS_ROLE_ID;
		tomcat1RoleId = AccConstants.TOMCAT1_ROLE_ID;
		jboss1RoleId = AccConstants.JBOSS1_ROLE_ID;
		momMachineId = AccConstants.MOM_MACHINE_ID;
		standaloneMachineId = AccConstants.STANDALONE_MACHINE_ID;
		collector1MachineId = AccConstants.COLLECTOR1_MACHINE_ID;
		collector2MachineId = AccConstants.COLLECTOR2_MACHINE_ID;
		collector3MachineId = AccConstants.COLLECTOR3_MACHINE_ID;
		tomcatMachineId = AccConstants.AGENT_MACHINE_ID;
		jbossMachineId = AccConstants.AGENT_MACHINE_ID;
		tomcat1MachineId = AccConstants.STANDALONE_MACHINE_ID;
		jboss1MachineId = AccConstants.STANDALONE_MACHINE_ID;
		tomcatAgentExpression = ".*\\|.*\\|.*Tomcat.*";
		momHost = envProperties
				.getMachineHostnameByRoleId(AccConstants.MOM_ROLE_ID);
		collector1Host = envProperties
				.getMachineHostnameByRoleId(AccConstants.COLLECTOR1_ROLE_ID);
		collector2Host = envProperties
				.getMachineHostnameByRoleId(AccConstants.COLLECTOR2_ROLE_ID);
		collector3Host = envProperties
				.getMachineHostnameByRoleId(AccConstants.COLLECTOR3_ROLE_ID);
		standaloneHost = envProperties
				.getMachineHostnameByRoleId(AccConstants.STANDALONE_ROLE_ID);
		momPort = Integer.parseInt(envProperties.getRolePropertiesById(
				momRoleId).getProperty("emPort"));
		collector1Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector1RoleId).getProperty("emPort"));
		collector2Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector2RoleId).getProperty("emPort"));
		collector3Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector3RoleId).getProperty("emPort"));
		standalonePort = Integer.parseInt(envProperties.getRolePropertiesById(
				standaloneRoleId).getProperty("emPort"));
		momLibDir = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_LIB_DIR);
		standaloneLibDir = envProperties.getRolePropertyById(standaloneRoleId,
				DeployEMFlowContext.ENV_EM_LIB_DIR);
		momConfigdir = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR);
		momConfigFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		momConfigFile_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		col1ConfigFile = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		col1ConfigFile_copy = envProperties.getRolePropertyById(
				collector1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		col2ConfigFile = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		col2ConfigFile_copy = envProperties.getRolePropertyById(
				collector2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		col3ConfigFile = envProperties.getRolePropertyById(collector3RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		col3ConfigFile_copy = envProperties.getRolePropertyById(
				collector3RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		standaloneConfigFile = envProperties.getRolePropertyById(standaloneRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		standaloneConfigFile_copy = envProperties.getRolePropertyById(standaloneRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		agent_collector_name = envProperties
				.getMachineHostnameByRoleId(AccConstants.COLLECTOR1_ROLE_ID);
		loadBalanceFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/loadbalancing.xml";
		loadBalanceFile_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/loadbalancing.xml.orig";
		usersxmlFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/users.xml";
		usersxmlFile_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/users.xml.orig";
		serverxmlFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/server.xml";
		serverxmlFile_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/server.xml.orig";
		momapmthresholdxmlpath = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml";
		momapmthresholdxmlpath_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml.orig";
		col1apmthresholdxmlpath = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml";
		col1apmthresholdxmlpath_copy = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml.orig";
		col2apmthresholdxmlpath = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml";
		col2apmthresholdxmlpath_copy = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml.orig";
		standaloneapmthresholdxmlpath = envProperties.getRolePropertyById(standaloneRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml";
		standaloneapmthresholdxmlpath_copy = envProperties.getRolePropertyById(standaloneRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "apm-events-thresholds-config.xml.orig";		
		momLogFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col1LogFile = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col2LogFile = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col3LogFile = envProperties.getRolePropertyById(collector3RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		standaloneLogFile = envProperties.getRolePropertyById(standaloneRoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;
		tomcatAgentProfile = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		tomcatAgentProfile_copy = envProperties.getRolePropertyById(
				tomcatRoleId, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile.orig";
		jbossAgentProfile = envProperties.getRolePropertyById(jbossRoleId,
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		jbossAgentProfile_copy = envProperties.getRolePropertyById(jbossRoleId, 
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile.orig";	
		tomcat1AgentProfile = envProperties.getRolePropertyById(tomcat1RoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		tomcat1AgentProfile_copy = envProperties.getRolePropertyById(tomcat1RoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile.orig";
		jboss1AgentProfile = envProperties.getRolePropertyById(jboss1RoleId,
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		jboss1AgentProfile_copy = envProperties.getRolePropertyById(jboss1RoleId, 
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile.orig";
		tomcatLogFile = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		jbossLogFile = envProperties.getRolePropertyById(jbossRoleId,
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		tomcat1LogFile = envProperties.getRolePropertyById(tomcat1RoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		jboss1LogFile = envProperties.getRolePropertyById(jboss1RoleId,
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		agentconnlimitxpath = "/apmEvents/clamps/clamp[@id=\"introscope.enterprisemanager.agent.connection.limit\"]/threshold";
		agentmetricslivelimitxpath = "/apmEvents/clamps/clamp[@id=\"introscope.enterprisemanager.metrics.live.limit\"]/threshold";
		agentevntthresholdattribute = "value";
		tempResult1 = "";
		tempResult2 = "";
		tempResult3 = "";
		testcaseId = "";
	}
	
	
	/** This method runs before AgentControllabilityTests Class
	 * It has the pre-requisites which needs to be done before
	 * running the test cases
	 */	
	
	public void ACCInitialize() {
		// set loadbalancing interval property
		replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
				"introscope.enterprisemanager.loadbalancing.interval=120",
				momMachineId, momConfigFile);
		List<String> appendclwenableprop = new ArrayList<String>();
		appendclwenableprop.add("introscope.apm.agentcontrol.clw.enable=true");
		appendProp(appendclwenableprop, momMachineId, momConfigFile);

		// set jboss agent naming property to false
		replaceProp("introscope.agent.agentAutoNamingEnabled=true",
				"introscope.agent.agentAutoNamingEnabled=false",
				jbossMachineId, jbossAgentProfile);
		replaceProp("introscope.agent.agentAutoNamingEnabled=true",
				"introscope.agent.agentAutoNamingEnabled=false",
				jboss1MachineId, jboss1AgentProfile);

		// backup all config files - mom_config, col_config, tomcatagentprofile,
		// jbossagentprofile , loadbalancing ,users and server xml file
		backupFile(loadBalanceFile, loadBalanceFile_copy, momMachineId);
		backupFile(momConfigFile, momConfigFile_copy, momMachineId);
		backupFile(col1ConfigFile, col1ConfigFile_copy, collector1MachineId);
		backupFile(col2ConfigFile, col2ConfigFile_copy, collector2MachineId);
		backupFile(col3ConfigFile, col3ConfigFile_copy, collector3MachineId);
		backupFile(tomcatAgentProfile, tomcatAgentProfile_copy, tomcatMachineId);
		backupFile(tomcat1AgentProfile, tomcat1AgentProfile_copy, standaloneMachineId);		
		backupFile(jbossAgentProfile, jbossAgentProfile_copy, jbossMachineId);
		backupFile(jboss1AgentProfile, jboss1AgentProfile_copy, standaloneMachineId);
		backupFile(usersxmlFile, usersxmlFile_copy, momMachineId);
		backupFile(serverxmlFile, serverxmlFile_copy, momMachineId);
		backupFile(momapmthresholdxmlpath, momapmthresholdxmlpath_copy, momMachineId);
		backupFile(standaloneapmthresholdxmlpath, standaloneapmthresholdxmlpath_copy, standaloneMachineId);
		backupFile(col1apmthresholdxmlpath, col1apmthresholdxmlpath_copy, collector1MachineId);
		backupFile(col2apmthresholdxmlpath, col2apmthresholdxmlpath_copy, collector2MachineId);
		backupFile(standaloneConfigFile, standaloneConfigFile_copy, standaloneMachineId);
		
		// sync time on mom and collectors
		List<String> machines = new ArrayList<String>();
		machines.add(momMachineId);
		machines.add(collector1MachineId);
		machines.add(collector2MachineId);
		machines.add(collector3MachineId);
		syncTimeOnMachines(machines);
	}
	
	/** This method reverts the files with their respective backups
	 *  which runs at the end of every test case in Aftermethod
	 */
	
	public void revertConfigFiles() {

		revertFile(momConfigFile, momConfigFile_copy, momMachineId);
		revertFile(col1ConfigFile, col1ConfigFile_copy,
				collector1MachineId);
		revertFile(col2ConfigFile, col2ConfigFile_copy,
				collector2MachineId);
		revertFile(col3ConfigFile, col3ConfigFile_copy,
				collector3MachineId);
		revertFile(loadBalanceFile, loadBalanceFile_copy, momMachineId);
		revertFile(tomcatAgentProfile, tomcatAgentProfile_copy,
				tomcatMachineId);
		revertFile(jbossAgentProfile, jbossAgentProfile_copy,
				jbossMachineId);
		revertFile(tomcat1AgentProfile, tomcat1AgentProfile_copy,
				standaloneMachineId);
		revertFile(jboss1AgentProfile, jboss1AgentProfile_copy,
				standaloneMachineId);
		revertFile(momConfigFile, momConfigFile_copy, momMachineId);
		revertFile(usersxmlFile, usersxmlFile_copy, momMachineId);
		revertFile(serverxmlFile, serverxmlFile_copy, momMachineId);
		revertFile(momapmthresholdxmlpath, momapmthresholdxmlpath_copy, momMachineId);
		revertFile(standaloneapmthresholdxmlpath, standaloneapmthresholdxmlpath_copy, standaloneMachineId);
		revertFile(col1apmthresholdxmlpath, col1apmthresholdxmlpath_copy, collector1MachineId);
		revertFile(col2apmthresholdxmlpath, col2apmthresholdxmlpath_copy, collector2MachineId);
		revertFile(standaloneConfigFile, standaloneConfigFile_copy, standaloneMachineId);
	}
	
	/** This method initializes the testcaseID and serversList which needs to be 
	 * called at the beginning of every testcase in AgentCOntrollabilityTests
	 */
	
	public void setuptestcase(String testId,String[] serverslist) {		
		
		testcaseId = testId;
		serverstoadd.clear();		
		for (int i = 0; i < serverslist.length; i++) {
            serverstoadd.add(serverslist[i]);
        }
	}
	
	/** This method is used to stop the EM services which are being used and it 
	 * runs after every test case of AgentControllabilityTests in After Method
	 */	
	
	public void stopEMServices() {	 
		
		if (serverstoadd.contains("col1")) {
			LOGGER.info("Stopping Collector1");
			stopCollectorEM(momRoleId,collector1RoleId); 
			stopEMServiceFlowExecutor(collector1MachineId);
		}
		
		if (serverstoadd.contains("col2")) {
			LOGGER.info("Stopping Collector2");
			stopCollectorEM(momRoleId, collector2RoleId);
			stopEMServiceFlowExecutor(collector2MachineId);
		}
		
		if (serverstoadd.contains("col3")) {
			LOGGER.info("Stopping Collector3");
			stopCollectorEM(momRoleId,collector3RoleId);
			stopEMServiceFlowExecutor(collector3MachineId);
		}
		
		if (serverstoadd.contains("mom")) {
			LOGGER.info("Stopping MOM");
			stopEM(momRoleId);
			stopEMServiceFlowExecutor(momMachineId);
		}
		
		if (serverstoadd.contains("standalone")) {
			LOGGER.info("Stopping Standalone EM");
			stopEM(standaloneRoleId);
			stopEMServiceFlowExecutor(standaloneMachineId);
		}
		
		if (serverstoadd.contains("empty")) {
			LOGGER.info("No Services to Stop");
		}
		
        harvestWait(10);
    }
	
	/** This method is used to stop the appservers which are being used and it 
	 * runs after every test case of AgentControllabilityTests in After Method
	 */	
	
	public void stopAllAgents()
	{
		
		if (serverstoadd.contains("tomcat")) {
			try {
				LOGGER.info("Stopping tomcat");
				stopTomcatAgent(tomcatRoleId);
			} catch (Exception e) {
				LOGGER.info("Tomcat is already in stopped state");
			} 
		}
		
		if (serverstoadd.contains("jboss")) {
			try {
				LOGGER.info("Stopping jboss");
				stopJBossAgent(jbossRoleId);
			} catch (Exception e) {
				LOGGER.info("JBoss is already in stopped state");
			}
		}
		
		if (serverstoadd.contains("tomcat1")) {
			try {
				LOGGER.info("Stopping tomcat1");
				stopTomcatAgent(tomcat1RoleId);
			} catch (Exception e) {
			LOGGER.info("Tomcat1 is already in stopped state");
			}
		}
		
		if (serverstoadd.contains("jboss1")) {
			try {
				LOGGER.info("Stopping jboss1");
				stopJBossAgent(jboss1RoleId);
			} catch (Exception e) {
				LOGGER.info("JBoss1 is already in stopped state");
			}
		}
		
	}
	
	/** This method renames all the log files created during the execution and 
	 * it runs after every test case in AfterMethod
	 */
	
	public void renamelogfiles() {
		
		if (serverstoadd.contains("mom")) {
			try 
			{
				renameFile(momLogFile, momLogFile + "_" + testcaseId, momMachineId);
			} catch (Exception e) { 
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("col1")) {	
			try 
			{   
				renameFile(col1LogFile, col1LogFile + "_" + testcaseId, collector1MachineId); 
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("col2")) {
			try 
			{  
				renameFile(col2LogFile, col2LogFile + "_" + testcaseId, collector2MachineId);
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("col3")) {		
			try 
			{  
				renameFile(col3LogFile, col3LogFile + "_" + testcaseId, collector3MachineId);
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("tomcat")) {
			try 
			{    
				renameFile(tomcatLogFile, tomcatLogFile + "_" + "tomcat" + "_" + testcaseId, tomcatMachineId); 
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("jboss")) {		
			try 
			{    
				renameFile(jbossLogFile, jbossLogFile + "_" + "jboss" + "_" + testcaseId, jbossMachineId); 
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("standalone")) {
			try 
			{    
				renameFile(standaloneLogFile, standaloneLogFile + "_" + testcaseId, standaloneMachineId); 
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("tomcat1")) {
			try 
			{    
				renameFile(tomcat1LogFile, tomcat1LogFile + "_" + "tomcat1" + "_" + testcaseId, tomcat1MachineId); 
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
		
		if (serverstoadd.contains("jboss1")) {
			try 
			{    
				renameFile(jboss1LogFile, jboss1LogFile + "_" + "jboss1" + "_" + testcaseId, jboss1MachineId); 
			} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
			}
		}
	
	}
	
	
	/**
     * Adds a new attribute. If an attribute with that name is already present
     * in the element, its value is changed to be that of the value
     * parameter.
     *
     * @param xpathtonode The xpath to a node to change attribute.
     * @param nodeattribue  The name of the attribute to create or alter.
     * @param value Value to set in string form.
     * @return this builder
     */	
	
	public void setattributeinapmthresholdXML (String machineId, String xmlFilePath, String xpathtonode,
	        String nodeattribue, String Value) {		
	        
		XmlModifierFlowContext modifyXML =
                new XmlModifierFlowContext.Builder(xmlFilePath).setAttribute(xpathtonode, nodeattribue, Value).build();
	
		runFlowByMachineId(machineId, XmlModifierFlow.class, modifyXML); 	    
		
	}
	
	
	public void stopEMServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("Introscope_Enterprise_Manager.lax").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }	
	 
	}



