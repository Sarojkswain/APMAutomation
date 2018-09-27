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
 * Author : BALRA06/KETSW01
 */
package com.ca.apm.tests.agentcontrollability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.flow.webapp.jboss.DeployJbossFlowContext;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.testbed.ACCLinuxTestbed;


public class AgentControllabilityLinuxTests extends BaseAgentTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AgentControllabilityLinuxTests.class);
	TestUtils utility = new TestUtils();
	CLWCommons clw = new CLWCommons();	
	EmUtils emUtils = utilities.createEmUtils();	
	XMLUtil xmlutil = new XMLUtil();

	private final String momRoleId;
	private final String collector1RoleId;
	private final String collector2RoleId;
	private final String collector3RoleId;
	private final String tomcatRoleId;
	private final String jbossRoleId;
	protected String momMachineId;
	protected String collector1MachineId;
	protected String collector2MachineId;
	protected String collector3MachineId;
	protected String tomcatMachineId;
	protected String jbossMachineId;
	private final String momHost;
	private final String collector1Host;
	private final String collector2Host;
	private final String collector3Host;
	private final int momPort;
	private final int collector1Port;
	private final int collector2Port;
	private final int collector3Port;
	private final String momLibDir;
	private final String momConfigdir;
	private final String momConfigFile;
	private final String momConfigFile_copy;
	private final String momLogFile;
	private final String col1ConfigFile;
	private final String col1ConfigFile_copy;
	private final String col2ConfigFile;
	private final String col2ConfigFile_copy;
	private final String col3ConfigFile;
	private final String col3ConfigFile_copy;
	private final String col1LogFile;
	private final String col2LogFile;
	private final String col3LogFile;
	private final String user;
	private final String password;
	private final String loadBalanceFile;
	private final String loadBalanceFile_copy;
	private final String tomcatAgentProfile;
	private final String tomcatAgentProfile_copy;
	public String jbossInstallDir;
	private final String jbossAgentProfile;
	private final String jbossAgentProfile_copy;
	private final String tomcatLogFile;
	private final String jbossLogFile;
	private final String tomcatAgentExpression;	
	private final String agent_collector_name;
	private String tempResult1, tempResult2, tempResult3, tempResult4;
	

	public AgentControllabilityLinuxTests() {

		momRoleId = ACCLinuxTestbed.MOM_ROLE_ID;
		collector1RoleId = ACCLinuxTestbed.COLLECTOR1_ROLE_ID;
		collector2RoleId = ACCLinuxTestbed.COLLECTOR2_ROLE_ID;
		collector3RoleId = ACCLinuxTestbed.COLLECTOR3_ROLE_ID;
		tomcatRoleId = ACCLinuxTestbed.TOMCAT_ROLE_ID;
		jbossRoleId = ACCLinuxTestbed.JBOSS_ROLE_ID;
		momMachineId = ACCLinuxTestbed.MOM_MACHINE_ID;
		collector1MachineId = ACCLinuxTestbed.COLLECTOR1_MACHINE_ID;
		collector2MachineId = ACCLinuxTestbed.COLLECTOR2_MACHINE_ID;
		collector3MachineId = ACCLinuxTestbed.COLLECTOR3_MACHINE_ID;
		tomcatMachineId = ACCLinuxTestbed.AGENT_MACHINE_ID;
		jbossMachineId = ACCLinuxTestbed.AGENT_MACHINE_ID;
		tomcatAgentExpression = "\".*\\|.*\\|Tomcat.*\"";		
		momHost = envProperties
				.getMachineHostnameByRoleId(ACCLinuxTestbed.MOM_ROLE_ID);
		collector1Host = envProperties
				.getMachineHostnameByRoleId(ACCLinuxTestbed.COLLECTOR1_ROLE_ID);
		collector2Host = envProperties
				.getMachineHostnameByRoleId(ACCLinuxTestbed.COLLECTOR2_ROLE_ID);
		collector3Host = envProperties
				.getMachineHostnameByRoleId(ACCLinuxTestbed.COLLECTOR3_ROLE_ID);
		momPort = Integer.parseInt(envProperties.getRolePropertiesById(
				momRoleId).getProperty("emPort"));
		collector1Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector1RoleId).getProperty("emPort"));
		collector2Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector2RoleId).getProperty("emPort"));
		collector3Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector3RoleId).getProperty("emPort"));
		momLibDir = envProperties.getRolePropertyById(momRoleId,
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
		agent_collector_name = envProperties
				.getMachineHostnameByRoleId(ACCLinuxTestbed.COLLECTOR1_ROLE_ID);
		loadBalanceFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/loadbalancing.xml";
		loadBalanceFile_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/loadbalancing.xml.orig";
		momLogFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col1LogFile = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col2LogFile = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col3LogFile = envProperties.getRolePropertyById(collector3RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		user = ApmbaseConstants.emUser;
		password =  ApmbaseConstants.emPassw;
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
		tomcatLogFile = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		jbossLogFile = envProperties.getRolePropertyById(jbossRoleId, 
				DeployJbossFlowContext.ENV_JBOSS_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
		tempResult1 = "";
		tempResult2 = "";
		tempResult3 = "";
	}

	@BeforeClass(alwaysRun = true)	
	public void ACCInitialize() {
		// set loadbalancing interval property
		replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
				"introscope.enterprisemanager.loadbalancing.interval=120",
				momMachineId, momConfigFile);
		List<String> appendclwenableprop = new ArrayList<String>();
		appendclwenableprop.add("introscope.apm.agentcontrol.clw.enable=true");
		appendProp(appendclwenableprop, momMachineId, momConfigFile);

		// set jboss agent naming property
		replaceProp("introscope.agent.agentAutoNamingEnabled=true",
				"introscope.agent.agentAutoNamingEnabled=false",
				jbossMachineId, jbossAgentProfile);
		// backup all config files - loadbalancing file, mom_config, col_cofigs,
		// tomcatagentprofile, jbossagentprofile

		backupFile(loadBalanceFile, loadBalanceFile_copy, momMachineId);
		backupFile(momConfigFile, momConfigFile_copy, momMachineId);
		backupFile(col1ConfigFile, col1ConfigFile_copy, collector1MachineId);
		backupFile(col2ConfigFile, col2ConfigFile_copy, collector2MachineId);
		backupFile(col3ConfigFile, col3ConfigFile_copy, collector3MachineId);
		backupFile(tomcatAgentProfile, tomcatAgentProfile_copy, tomcatMachineId);
		backupFile(jbossAgentProfile, jbossAgentProfile_copy, jbossMachineId);

		// sync time on mom and collectors
		List<String> machines = new ArrayList<String>();
		machines.add(momMachineId);
		machines.add(collector1MachineId);
		machines.add(collector2MachineId);
		machines.add(collector3MachineId);
		syncTimeOnMachines(machines);
	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_450351_Agent_redirection_is_not_handled_when_lb_has_incorrect_collector()
			throws Exception {

		startTomcatAgent(tomcatRoleId);
		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startEM(collector3RoleId);

		clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
				tomcatAgentExpression, "abcd", collector1Port, momHost,
				momPort, momLibDir);
		harvestWait(300);

		String msg = "Connected to "
				+ momHost
				+ ":"
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);

		stopTomcatAgent(tomcatRoleId);
		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopCollectorEM(momRoleId,collector3RoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_450351", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_450351", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_450351", collector2MachineId);
		renameFile(col3LogFile, col3LogFile + "_450351", collector3MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_450351", tomcatMachineId);

	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_440535_Test_affinity_functionality_of_lb_file()
			throws Exception {

		String findStr = "AffinityAgent";
		replaceProp("introscope.agent.agentName=Tomcat Agent",
				"introscope.agent.agentName=AffinityAgent", tomcatMachineId,
				tomcatAgentProfile);
		replaceProp("introscope.agent.agentName=JBoss Agent",
				"introscope.agent.agentName=AffinityAgent", jbossMachineId,
				jbossAgentProfile);

		xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile, "Test-affinity",
				".*\\|.*\\|.*AffinityAgent.*", collector1Host + ":"
						+ collector1Port,
				collector2Host + ":" + collector2Port, "1:true");

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startTomcatAgent(tomcatRoleId);
		startJBossAgent(jbossRoleId);
		harvestWait(60);

		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		int count = StringUtils.countMatches(tempResult1, findStr);
		Assert.assertEquals(
				"AffinityAgent is not connected to Collector1 though affinitiy is set for collector1 ",
				2, count);

		xmlutil.changelatchedEntryInLoadBalXML(loadBalanceFile,
				"Test-affinity", collector2Host + ":" + collector2Port,
				collector1Host + ":" + collector1Port);
		harvestWait(240);

		tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
				collector2Port, momLibDir).toString();
		count = StringUtils.countMatches(tempResult2, findStr);
		Assert.assertEquals(
				"AffinityAgent is not connected to Collector2 though affinitiy is set for collector2 ",
				2, count);

		stopCollectorEM(momRoleId,collector2RoleId);
		harvestWait(120);

		tempResult3 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		count = StringUtils.countMatches(tempResult3, findStr);
		Assert.assertEquals(
				"AffinityAgent is not connected to Collector1 though collector2 is shutdown ",
				2, count);

		startEM(collector2RoleId);
		harvestWait(240);

		tempResult4 = clw.getNodeList(user, password, ".*", collector2Host,
				collector2Port, momLibDir).toString();
		count = StringUtils.countMatches(tempResult4, findStr);
		Assert.assertEquals(
				"AffinityAgent is not connected back to Collector2 though it is up ",
				2, count);

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopTomcatAgent(tomcatRoleId);
		stopJBossAgent(jbossRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_440535", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_440535", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_440535", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_440535", tomcatMachineId);
		renameFile(jbossLogFile, jbossLogFile + "_440535", jbossMachineId);

	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_268922_Overriden_Collector_settings_by_MOM_with_Agent()
			throws Exception {

		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector1MachineId, col1ConfigFile);
		startEM(momRoleId);
		startEM(collector1RoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue("Tomcat Agent is not connected to the Collector",
				tempResult1.contains("Tomcat"));

		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				momMachineId, momConfigFile);
		harvestWait(120);

		startJBossAgent(jbossRoleId);
		harvestWait(120);

		tempResult2 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue(
				"Both the agents are not connected to the appropriate MOM or Collector",
				tempResult2.contains("Tomcat")
						&& !tempResult2.contains("JBoss"));

		String msg = "Connected to "
				+ momHost
				+ ":"
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, jbossMachineId, jbossLogFile, msg);

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopTomcatAgent(tomcatRoleId);
		stopJBossAgent(jbossRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268922", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_268922", collector1MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_268922", tomcatMachineId);
		renameFile(jbossLogFile, jbossLogFile + "_268922", jbossMachineId);

	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_440678_Affinity_for_agents_connected_directly_to_collector()
			throws Exception {

		xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile, "Test-affinity",
				".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + collector1Port,
				collector2Host + ":" + collector2Port, "2:true");

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startEM(collector3RoleId);

		replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
				"agentManager.url.1=" + collector1Host + ":" + collector1Port,
				tomcatMachineId, tomcatAgentProfile);

		startTomcatAgent(tomcatRoleId);
		harvestWait(180);

		tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
				collector2Port, momLibDir).toString();
		Assert.assertTrue("Tomcat Agent is not connected to the Collector2",
				tempResult1.contains("Tomcat"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopCollectorEM(momRoleId,collector3RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_440678", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_440678", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_440678", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_440678", tomcatMachineId);

	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_268901_UAC_permission_ON_for_ACC() throws Exception {

		backupFile(momConfigdir + "/users.xml", momConfigdir
				+ "/users_bckup.xml", momMachineId);
		backupFile(momConfigdir + "/server.xml", momConfigdir
				+ "/server_bckup.xml", momMachineId);

		XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", "ACC", "");
		Map<String, String> attributeMap = new HashMap<String, String>();
		attributeMap.put("user", "ACC");
		attributeMap.put("permission", "agent_control");
		XMLUtil.createElement(momConfigdir + "/server.xml", "grant", "",
				"server", "version", "0.2", attributeMap);

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);

		tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime("ACC", "",
				momHost, momPort, momLibDir);
		LOGGER.info("clw output is " + tempResult1);
		Assert.assertTrue(
				"Tomcat agent specifier entry is not added successfully to the loadbalancing file",
				!tempResult1.contains("Invalid command"));

		int i = xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile,
				"Test-affinity", ".*\\|.*\\|.*Tomcat.*", collector1Host + ":"
						+ collector1Port,
				collector2Host + ":" + collector2Port, "1:true");
		Assert.assertTrue(
				"Tomcat agent specifier entry is not added successfully to the loadbalancing file",
				i == 1);
		harvestWait(120);

		tempResult2 = clw.removeAgentExpression("ACC", "",
				".*\\|.*\\|.*Tomcat.*", momHost, momPort, momLibDir).toString();
		Assert.assertTrue(
				"CLW command for removeAgentExpression failed to run successfully",
				tempResult2.contains("Command executed successfully"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);

		deleteFile(momConfigdir + "/users.xml", momMachineId);
		moveFile(momConfigdir + "/users_bckup.xml", momConfigdir
				+ "/users.xml", momMachineId);
		deleteFile(momConfigdir + "/server.xml", momMachineId);
		moveFile(momConfigdir + "/server_bckup.xml", momConfigdir
				+ "/server.xml", momMachineId);
		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268901", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_268901", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_268901", collector2MachineId);

	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_296852_UAC_permission_ON_for_ACC_group()
			throws Exception {

		backupFile(momConfigdir + "/users.xml", momConfigdir
				+ "/users_bckup.xml", momMachineId);
		backupFile(momConfigdir + "/server.xml", momConfigdir
				+ "/server_bckup.xml", momMachineId);

		XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", "ACC1", "");
		XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", "ACC2", "");
		XMLUtil.createGroupAddMultipleUsersInUsersXML(momConfigdir
				+ "/users.xml", "ACCgroup", "ACCgroup", "ACC1,ACC2");
		XMLUtil.createGroupGrantForElement(momConfigdir + "/server.xml",
				"server", "ACCgroup", "agent_control");

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);

		tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime("ACC1", "",
				momHost, momPort, momLibDir);
		LOGGER.info("clw output is " + tempResult1);
		Assert.assertTrue(
				"Tomcat agent specifier entry is not added successfully to the loadbalancing file",
				!tempResult1.contains("Invalid command"));

		int i = xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile,
				"Test-affinity", ".*\\|.*\\|.*Tomcat.*", collector1Host + ":"
						+ collector1Port,
				collector2Host + ":" + collector2Port, "1:true");
		Assert.assertTrue(
				"Tomcat agent specifier entry is not added successfully to the loadbalancing file",
				i == 1);
		harvestWait(120);
		tempResult2 = clw.removeAgentExpression("ACC2", "",
				".*\\|.*\\|.*Tomcat.*", momHost, momPort, momLibDir).toString();
		Assert.assertTrue(
				"CLW command for removeAgentExpression failed to run successfully",
				tempResult2.contains("Command executed successfully"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);

		deleteFile(momConfigdir + "/users.xml", momMachineId);
		moveFile(momConfigdir + "/users_bckup.xml", momConfigdir
				+ "/users.xml", momMachineId);
		deleteFile(momConfigdir + "/server.xml", momMachineId);
		moveFile(momConfigdir + "/server_bckup.xml", momConfigdir
				+ "/server.xml", momMachineId);
		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_296852", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_296852", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_296852", collector2MachineId);

	}
	
	@Test(groups = { "BAT" }, enabled = true)
	public void testCase_268891_Verify_default_agent_connection_mode_property_in_Collector()
			throws Exception {

		try {
			isKeywordInFile(envProperties, collector1MachineId, col1ConfigFile,
					"introscope.apm.agentcontrol.agent.allowed=true");
			isKeywordInFile(envProperties, collector1MachineId, col1ConfigFile,
					"introscope.apm.agentcontrol.agent.emlistlookup.enable=true");
			isKeywordInFile(envProperties, collector1MachineId, col1ConfigFile,
					"introscope.enterprisemanager.agent.disallowed.connection.limit=0");
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.assertTrue("Log check failed because of the Exception : "
					+ e, false);
		}
	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_268890_Verify_default_agent_connection_mode_property_in_MOM()
			throws Exception {

		try {
			isKeywordInFile(envProperties, momMachineId, momConfigFile,
					"introscope.apm.agentcontrol.agent.allowed=true");
			isKeywordInFile(envProperties, momMachineId, momConfigFile,
					"introscope.apm.agentcontrol.agent.emlistlookup.enable=true");
			isKeywordInFile(envProperties, momMachineId, momConfigFile,
					"introscope.enterprisemanager.agent.disallowed.connection.limit=0");
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.assertTrue("Log check failed because of the Exception : "
					+ e, false);
		}

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_295431_no_ACC_for_direct_collector_connection_global_allow()
			throws Exception {

		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector1MachineId, col1ConfigFile);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector2MachineId, col2ConfigFile);
		replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
				"agentManager.url.1=" + collector1Host + ":" + collector1Port,
				tomcatMachineId, tomcatAgentProfile);

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue("Tomcat Agent is not connected to the Collector1",
				tempResult1.contains("Tomcat"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		startEM(collector1RoleId);
		harvestWait(120);

		String msg = "Connected to "
				+ collector1Host
				+ ":"
				+ collector1Port
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);

		stopCollectorEM(momRoleId,collector1RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_295431", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_295431", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_295431", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_295431", tomcatMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_450352_no_ACC_for_direct_collector_connection_global_allow()
			throws Exception {

		replaceProp("log4j.logger.Manager=INFO, console, logfile",
				"log4j.logger.Manager=DEBUG, console, logfile", momMachineId,
				momConfigFile);

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);

		clw.setDisAllowedAgentWithCollector(user, password,
				agent_collector_name, tomcatAgentExpression, collector1Host,
				collector1Port, momHost, momPort, momLibDir);
		clw.setDisAllowedAgentWithCollector(user, password,
				agent_collector_name, tomcatAgentExpression, collector2Host,
				collector2Port, momHost, momPort, momLibDir);
		harvestWait(120);

		startTomcatAgent(tomcatRoleId);
		harvestWait(120);

		String msg1 = "Reject " + collector1Host + "@" + collector1Port
				+ " because agent is excluded";
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg1);

		String msg2 = "Reject " + collector2Host + "@" + collector2Port
				+ " because agent is excluded";
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg2);

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_450352", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_450352", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_450352", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_450352", tomcatMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_268902_UAC_OFF_for_ACC_Only_on_Cluster()
			throws Exception {

		backupFile(momConfigdir + "/users.xml", momConfigdir
				+ "/users_bckup.xml", momMachineId);
		String accuser = "ACC";
		XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", accuser, "");

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);

		tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime("ACC", "",
				momHost, momPort, momLibDir);
		Assert.assertTrue(
				"Expected output from CLW is not found",
				tempResult1
						.contains("com.wily.introscope.permission.PermissionException: User "
								+ accuser
								+ " does not have sufficient permissions in domain Server Resource"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);

		deleteFile(momConfigdir + "/users.xml", momMachineId);
		moveFile(momConfigdir + "/users_bckup.xml", momConfigdir
				+ "/users.xml", momMachineId);
		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268902", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_268902", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_268902", collector2MachineId);
	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_303682_Agent_does_not_iterate_through_EM_list_when_disallowed()
			throws Exception {

		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector1MachineId, col1ConfigFile);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector2MachineId, col2ConfigFile);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector3MachineId, col3ConfigFile);
		replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
				"agentManager.url.1=" + collector1Host + ":" + collector1Port,
				tomcatMachineId, tomcatAgentProfile);
		List<String> appendproplist = new ArrayList<String>();
		appendproplist.add("agentManager.url.2=" + collector2Host + ":"
				+ collector2Port);
		appendproplist.add("agentManager.url.3=" + collector3Host + ":"
				+ collector3Port);
		appendProp(appendproplist, tomcatMachineId, tomcatAgentProfile);

		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startEM(collector3RoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		String msg1 = "Lost contact with the Introscope Enterprise Manager at "
				+ collector1Host + ":" + collector1Port;
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg1);

		String msg2 = "Lost contact with the Introscope Enterprise Manager at "
				+ collector2Host + ":" + collector2Port;
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg2);

		String msg3 = "Lost contact with the Introscope Enterprise Manager at "
				+ collector3Host + ":" + collector3Port;
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg3);

		String msg4 = "Connected to "
				+ collector3Host
				+ ":"
				+ collector3Port
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg4);

		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopCollectorEM(momRoleId,collector3RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(col1LogFile, col1LogFile + "_303682", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_303682", collector2MachineId);
		renameFile(col3LogFile, col3LogFile + "_303682", collector3MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_303682", tomcatMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_269835_Agent_ALLOW_DISALLOW_REDIRECT_one_MOM_three_Collectors()
			throws Exception {

		replaceProp("introscope.agent.agentName=Tomcat Agent",
				"introscope.agent.agentName=AffinityAgent", tomcatMachineId,
				tomcatAgentProfile);
		replaceProp("introscope.agent.agentName=JBoss Agent",
				"introscope.agent.agentName=AffinityAgent", jbossMachineId,
				jbossAgentProfile);

		startEM(momRoleId);
		clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
				".*\\|.*\\|.*AffinityAgent.*", collector1Host, collector1Port,
				momHost, momPort, momLibDir);
		XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
				"false", "true");
		harvestWait(60);

		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startEM(collector3RoleId);
		startTomcatAgent(tomcatRoleId);
		startJBossAgent(jbossRoleId);
		harvestWait(60);

		String findStr = "AffinityAgent";
		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		int count1 = StringUtils.countMatches(tempResult1, findStr);
		Assert.assertEquals(
				"AffinityAgent is not connected to Collector1 though it is included in loadbal file ",
				2, count1);

		clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
				".*\\|.*\\|.*AffinityAgent.*", collector2Host, collector2Port,
				momHost, momPort, momLibDir);
		XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
				"false", "true");
		harvestWait(240);

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		harvestWait(60);

		String msg = "Connected to " + collector2Host;
		isKeywordInFile(envProperties, jbossMachineId, jbossLogFile, msg);
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);
		startEM(momRoleId);
		clw.setDisAllowedAgentWithCollector(user, password,
				agent_collector_name, ".*\\|.*\\|.*AffinityAgent.*",
				collector1Host, collector1Port, momHost, momPort, momLibDir);
		clw.setDisAllowedAgentWithCollector(user, password,
				agent_collector_name, ".*\\|.*\\|.*AffinityAgent.*",
				collector2Host, collector2Port, momHost, momPort, momLibDir);
		harvestWait(180);

		tempResult2 = clw.getNodeList(user, password, ".*", collector3Host,
				collector3Port, momLibDir).toString();
		int count2 = StringUtils.countMatches(tempResult2, findStr);
		Assert.assertEquals(
				"AffinityAgent is not connected to Collector3 though remaining 2 collectors are excluded ",
				2, count2);

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopCollectorEM(momRoleId,collector3RoleId);
		stopTomcatAgent(tomcatRoleId);
		stopJBossAgent(jbossRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_269835", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_269835", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_269835", collector2MachineId);
		renameFile(col3LogFile, col3LogFile + "_269835", collector3MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_269835", tomcatMachineId);
		renameFile(jbossLogFile, jbossLogFile + "_269835", jbossMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_440677_Custom_agents_disallowed_messages_in_Verbose_level()
			throws Exception {

		replaceProp("log4j.logger.Manager=INFO, console, logfile",
				"log4j.logger.Manager=DEBUG, console, logfile", momMachineId,
				momConfigFile);
		replaceProp("log4j.logger.Manager=INFO, console, logfile",
				"log4j.logger.Manager=DEBUG, console, logfile",
				collector1MachineId, col1ConfigFile);
		replaceProp("log4j.logger.Manager=INFO, console, logfile",
				"log4j.logger.Manager=DEBUG, console, logfile",
				collector2MachineId, col2ConfigFile);

		startEM(momRoleId);
		clw.setDisAllowedAgentWithCollector(user, password,
				agent_collector_name, tomcatAgentExpression, collector1Host,
				collector1Port, momHost, momPort, momLibDir);
		harvestWait(120);

		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startTomcatAgent(tomcatRoleId);

		String msg1 = "Disconnected SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)";
		String msg2 = "Disconnected SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)";

		try {
			isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg1);
			isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg2);
			Assert.assertTrue(false);
		} catch (Exception e) {
			Assert.assertTrue(true);
			LOGGER.info("Disconnect messages are not seen in verbose level in the collector log");
		}

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_440677", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_440677", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_440677", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_440677", tomcatMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_268894_default_agent_connection_mode_with_default_option_True()
			throws Exception {

		replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
				"introscope.enterprisemanager.clustering.mode=StandAlone",
				momMachineId, momConfigFile);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true", "#",
				momMachineId, momConfigFile);

		startEM(momRoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
				momLibDir).toString();
		Assert.assertTrue("Tomcat Agent is not connected to the standalone EM",
				tempResult1.contains("Tomcat"));

		stopEM(momRoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268894", momMachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_268894", tomcatMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_268892_default_agent_connection_mode_with_False()
			throws Exception {

		replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
				"introscope.enterprisemanager.clustering.mode=StandAlone",
				momMachineId, momConfigFile);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				momMachineId, momConfigFile);

		startEM(momRoleId);
		startJBossAgent(jbossRoleId);
		harvestWait(60);

		String msg = "Connected to "
				+ momHost
				+ ":"
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, jbossMachineId, jbossLogFile, msg);

		tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
				momLibDir).toString();
		Assert.assertTrue(
				"JBoss Agent is not connected to the standalone EM in disallowed mode",
				!tempResult1.contains("JBoss"));
		tempResult2 = clw.getLatestMetricValue(user, password,
				"(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
				momPort, momLibDir);
		Assert.assertTrue(
				"Metrics are getting reported though Jboss set to connect in disallowed mode",
				tempResult2.equals("-1"));

		stopEM(momRoleId);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
				"introscope.apm.agentcontrol.agent.allowed=true", momMachineId,
				momConfigFile);
		startEM(momRoleId);
		harvestWait(120);

		tempResult3 = clw.getNodeList(user, password, ".*", momHost, momPort,
				momLibDir).toString();
		Assert.assertTrue(
				"JBoss Agent is not connected to the standalone EM in allowed mode",
				tempResult3.contains("JBoss"));
		tempResult4 = clw.getLatestMetricValue(user, password,
				"(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
				momPort, momLibDir);
		Assert.assertTrue("Metrics are not getting reported from Jboss",
				!tempResult4.equals(-1));

		stopEM(momRoleId);
		stopJBossAgent(jbossRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268892", momMachineId);
		renameFile(jbossLogFile, jbossLogFile + "_268892", jbossMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_268889_Verify_default_agent_connection_mode_property_in_EM_properties_file_Standalone()
			throws Exception {

		try {
			isKeywordInFile(envProperties, momMachineId, momConfigFile,
					"introscope.apm.agentcontrol.agent.allowed=true");
			isKeywordInFile(envProperties, momMachineId, momConfigFile,
					"introscope.apm.agentcontrol.agent.emlistlookup.enable=true");
			isKeywordInFile(envProperties, momMachineId, momConfigFile,
					"introscope.enterprisemanager.agent.disallowed.connection.limit=0");
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.assertTrue("Log check failed because of the Exception : "
					+ e, false);
		}
	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_268897_Agent_Controllability_at_MOM_ON_and_Collector_ON()
			throws Exception {

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);

		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		String msg1 = "Connected controllable Agent to the Introscope Enterprise Manager at "
				+ momHost;
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg1);

		String msg2 = "Lost contact with the Introscope Enterprise Manager at "
				+ momHost + ":" + momPort;
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg2);

		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
				collector2Port, momLibDir).toString();
		Assert.assertTrue(
				"Tomcat Agent is not connected to any of the Collector",
				tempResult1.contains("Tomcat")
						|| tempResult2.contains("Tomcat"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268897", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_268897", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_268897", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_268897", tomcatMachineId);

	}
	
	@Test(groups = { "Smoke" }, enabled = true)
	public void testCase_303711_introscope_apm_agentcontrol_agent_reconnect_wait()
			throws Exception {

		String logfile = "null";
		String machineId = "null";
		replaceProp(
				"introscope.enterprisemanager.agent.disallowed.connection.limit=0",
				"introscope.enterprisemanager.agent.disallowed.connection.limit=1",
				momMachineId, momConfigFile);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				momMachineId, momConfigFile);
		replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
				"log4j.logger.IntroscopeAgent=DEBUG,logfile", tomcatMachineId,
				tomcatAgentProfile);
		replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
				"log4j.logger.IntroscopeAgent=DEBUG,logfile", jbossMachineId,
				jbossAgentProfile);

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startJBossAgent(jbossRoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(180);

		tempResult1 = clw.getCurrentAgentsDisAllowedList(user, password,
				momHost, momHost, momPort, momLibDir).toString();

		if (tempResult1.contains("Tomcat")) {
			logfile = jbossLogFile;
			machineId = jbossMachineId;
		}

		else if (tempResult1.contains("JBoss")) {
			logfile = tomcatLogFile;
			machineId = tomcatMachineId;
		} else
			Assert.assertTrue(
					"No agents are connected in diallowed mode to the MOM",
					false);

		String msg1 = "Waiting 45000 milliseconds for Introscope Enterprise Manager "
				+ momHost;
		harvestWait(600);
		isKeywordInFile(envProperties, machineId, logfile, msg1);

		String msg2 = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 45 to 10";
		String msg3 = "Waiting 75000 milliseconds for Introscope Enterprise Manager "
				+ momHost;
		replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=45",
				"introscope.apm.agentcontrol.agent.reconnect.wait=10",
				momMachineId, momConfigFile);
		harvestWait(120);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg2);
		harvestWait(120);
		isKeywordInFile(envProperties, machineId, logfile, msg3);

		String msg4 = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.apm.agentcontrol.agent.reconnect.wait is negative: -1";
		String msg5 = "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45";
		String msg6 = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 10 to 45";
		replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=10",
				"introscope.apm.agentcontrol.agent.reconnect.wait=-1",
				momMachineId, momConfigFile);
		harvestWait(120);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg4);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg5);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg6);

		String msg7 = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 45 to 60";
		String msg8 = "Waiting 90000 milliseconds for Introscope Enterprise Manager "
				+ momHost;
		replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=-1",
				"introscope.apm.agentcontrol.agent.reconnect.wait=60",
				momMachineId, momConfigFile);
		harvestWait(120);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg7);
		harvestWait(600);
		isKeywordInFile(envProperties, machineId, logfile, msg8);

		String msg9 = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.apm.agentcontrol.agent.reconnect.wait is not an integer:";
		String msg10 = "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45";
		String msg11 = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 60 to 45";
		String msg12 = "Value for the property introscope.apm.agentcontrol.agent.reconnect.wait is not an integer, setting it to default value of:45";
		replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=60",
				"introscope.apm.agentcontrol.agent.reconnect.wait= ",
				momMachineId, momConfigFile);
		harvestWait(120);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg9);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg10);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg11);
		isKeywordInFile(envProperties, momMachineId, momLogFile, msg12);

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopJBossAgent(jbossRoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_303711", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_303711", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_303711", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_303711", tomcatMachineId);
		renameFile(jbossLogFile, jbossLogFile + "_303711", jbossMachineId);

	}
	
	@Test(groups = { "Deep" }, enabled = true)
	public void testCase_268893_default_agent_connection_mode_with_TRUE()
			throws Exception {

		replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
				"introscope.enterprisemanager.clustering.mode=StandAlone",
				momMachineId, momConfigFile);

		startEM(momRoleId);
		startJBossAgent(jbossRoleId);
		harvestWait(60);

		tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
				momLibDir).toString();
		Assert.assertTrue(
				"JBoss Agent is not connected to the standalone EM in allowed mode",
				tempResult1.contains("JBoss"));
		tempResult2 = clw.getLatestMetricValue(user, password,
				"(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
				momPort, momLibDir);
		Assert.assertTrue("Metrics are not getting reported from Jboss",
				!tempResult2.equals(-1));

		stopEM(momRoleId);
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				momMachineId, momConfigFile);
		startEM(momRoleId);
		harvestWait(120);

		String msg = "Connected to "
				+ momHost
				+ ":"
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, jbossMachineId, jbossLogFile, msg);

		tempResult3 = clw.getNodeList(user, password, ".*", momHost, momPort,
				momLibDir).toString();
		Assert.assertTrue(
				"JBoss Agent is not connected to the standalone EM in disallowed mode",
				!tempResult3.contains("JBoss"));
		tempResult4 = clw.getLatestMetricValue(user, password,
				"(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
				momPort, momLibDir);
		Assert.assertTrue(
				"Metrics are getting reported though Jboss set to connect in disallowed mode",
				tempResult4.equals("-1"));

		stopEM(momRoleId);
		stopJBossAgent(jbossRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268893", momMachineId);
		renameFile(jbossLogFile, jbossLogFile + "_268893", jbossMachineId);

	}
	
	@Test(groups = { "Deep" }, enabled = true)
	public void testCase_268908_CLW_Get_List_of_disallowed_Agents_on_particular_EM_Collector()
			throws Exception {

		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				momMachineId, momConfigFile);
		replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
				"agentManager.url.1=" + collector1Host + ":" + collector1Port,
				tomcatMachineId, tomcatAgentProfile);

		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		tempResult1 = clw.getCurrentAgentsDisAllowedList(user, password,
				collector1Host, momHost, momPort, momLibDir).toString();

		Assert.assertTrue(
				"Tomcat is not found in the disallowed agents list of collector1",
				tempResult1.contains("Tomcat"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_268908", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_268908", collector1MachineId);
		renameFile(col2LogFile, col2LogFile + "_268908", collector2MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_268908", tomcatMachineId);

	}
	
	@Test(groups = { "Deep" }, enabled = true)
	public void testCase_295426_default_agent_connection_mode_with_TRUE_Cluster_Collector_mode()
			throws Exception {

		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector1MachineId, col1ConfigFile);
		startEM(momRoleId);
		startEM(collector1RoleId);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);

		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue("Tomcat Agent is not connected to the Collector",
				tempResult1.contains("Tomcat"));

		stopCollectorEM(momRoleId,collector1RoleId);
		harvestWait(120);

		String msg = "Connected to "
				+ momHost
				+ ":"
				+ momPort
				+ ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
		isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);

		startEM(collector1RoleId);
		harvestWait(240);

		tempResult2 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue(
				"Tomcat Agent is not connected back to the Collector after restart",
				tempResult2.contains("Tomcat"));

		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopTomcatAgent(tomcatRoleId);

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_295426", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_295426", collector1MachineId);
		renameFile(tomcatLogFile, tomcatLogFile + "_295426", tomcatMachineId);

	}
	
	@Test(groups = { "Deep" }, enabled = true)
	public void testCase_295425_default_agent_connection_mode_with_TRUE_Cluster_MOM_mode()
			throws Exception {
		
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				collector1MachineId, col1ConfigFile);
		
		startEM(momRoleId);
		startEM(collector1RoleId);
		startJBossAgent(jbossRoleId);
		harvestWait(120);
		
		tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue(
				"JBoss Agent is not redirected to the collector",
				 tempResult1.contains("JBoss"));
		
		replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
				"introscope.apm.agentcontrol.agent.allowed=false",
				momMachineId, momConfigFile);
		harvestWait(240);		
		
		tempResult2 = clw.getNodeList(user, password, ".*", collector1Host,
				collector1Port, momLibDir).toString();
		Assert.assertTrue(
				"JBoss Agent is not redirected to the collector",
				 tempResult2.contains("JBoss")); 		
		tempResult3 = clw.getLatestMetricValue(user, password,
				"(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", collector1Host,
				collector1Port, momLibDir);
		Assert.assertTrue(
				"Metrics from JBoss are not getting reported after the agentcontrol property change",
				!tempResult3.equals("-1"));
		
		
		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);		
		stopJBossAgent(jbossRoleId);	

		revertConfigFiles();
		renameFile(momLogFile, momLogFile + "_295425", momMachineId);
		renameFile(col1LogFile, col1LogFile + "_295425", collector1MachineId);		
		renameFile(jbossLogFile, jbossLogFile + "_295425", jbossMachineId);		
		
	}


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
	}

}
