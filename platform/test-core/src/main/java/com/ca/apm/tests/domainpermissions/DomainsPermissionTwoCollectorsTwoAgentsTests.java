package com.ca.apm.tests.domainpermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.base.TwoCollectorsOneTomcatOneJBossTestsBase;

public class DomainsPermissionTwoCollectorsTwoAgentsTests extends
		TwoCollectorsOneTomcatOneJBossTestsBase {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DomainsPermissionTwoCollectorsTwoAgentsTests.class);

	protected String testCaseId;
	protected String testCaseName;
	protected String clwOutput;
	protected CLWCommons clw = new CLWCommons();
	protected String clwOut;
	protected TestUtils testUtils = new TestUtils();
	protected ArrayList<String> rolesInvolved = new ArrayList<String>();
	public String momdomainsxmlpath;
	public String momusersxmlpath;
	public String momserverxmlpath;
	public String momrealmsxmlpath;
	public String col1realmsxmlpath;
	public String col2realmsxmlpath;
	public String metricshutoffconfxmlpath;
	private String userName, userPassword, readPermission, agentMapping,
			domainName, domainUserName, domainGroupName, description,
			fullPermission, writePermission, historicalagentcontrolPermission,
			liveagentcontrolPermission, runtracerPermission, userAttrName,
			userElementName, tempResult1, machineID;
	private String metricExpression, agentExpression, metricName;
	Map<String, String> groupMap = new HashMap<String, String>();
	Map<String, String> userMap = new HashMap<String, String>();
	List<String> listoutput = new ArrayList<String>();
	private boolean userlogincheck;

	@BeforeClass(alwaysRun = true)
	public void initialize() {
		super.initialize();
		momdomainsxmlpath = momConfigDir + "/domains.xml";
		momusersxmlpath = momConfigDir + "/users.xml";
		momserverxmlpath = momConfigDir + "/server.xml";
		momrealmsxmlpath = momConfigDir + "/realms.xml";
		col1realmsxmlpath = col1ConfigDir + "/realms.xml";
		col2realmsxmlpath = col2ConfigDir + "/realms.xml";
		metricshutoffconfxmlpath = momConfigDir
				+ "/shutoff/MetricShutoffConfiguration.xml";
		readPermission = "read";
		fullPermission = "full";
		writePermission = "write";
		historicalagentcontrolPermission = "historical_agent_control";
		liveagentcontrolPermission = "live_agent_control";
		runtracerPermission = "run_tracer";
		agentMapping = "(.*)";
		metricName = "(.*EM Port.*)";
	}

	@BeforeTest(alwaysRun = true)
	public void enabledynamicdomainprop() {

		// enable domains configuration dynamic update property
		replaceMoMProperty(ApmbaseConstants.DOMAINCONFIG_DYNAMICUPDATE_PROPERTY
				+ "=false",
				ApmbaseConstants.DOMAINCONFIG_DYNAMICUPDATE_PROPERTY + "=true");
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_351816_Authenticating_User_Not_Part_Of_Any_Domain() {

		testCaseId = "351816";
		testCaseName = "verify_ALM_351816_Authenticating_User_Not_Part_Of_Any_Domain";
		userName = "user1";
		userPassword = "user1";
		userElementName = "principals";
		userAttrName = "plainTextPasswords";
		String userAttrOldValue = "false";
		String userAttrNewValue = "true";

		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);

		try {

			testCaseStart(testCaseName);

			Assert.assertEquals(
					XMLUtil.addUser(momusersxmlpath, userName, userPassword),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.changeAttributeValue(momusersxmlpath,
					userElementName, userAttrName, userAttrOldValue,
					userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
			startEMServices();
			checkCollectorsToMOMConnectivity();

			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					momHost, momPort, userName, userPassword, clwJarFileLoc,
					momlogDir, userLogInMsg(userName));
			Assert.assertFalse(
					"Login was successful for the user "
							+ userName
							+ " added to users.xml. but it should not as its not added in doamin",
					userlogincheck);

			LOGGER.info("user login validation End :::");

		} finally {
			stopEMServices();

			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_351828_Realms_New_Realm() {

		try {

			testCaseId = "351828";
			testCaseName = "verify_ALM_351828_Realms_New_Realm";

			testCaseStart(testCaseName);
			userName = "Bob";
			String groupName = "Illuminati";
			String permission = "full";
			userPassword = "";
			String momtestusersxmlpath = momConfigDir + "/testusers.xml";

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);

			String replaceStr = "<realm descriptor=\"Local Users and Groups Realm\" id=\"Test Realm\" active=\"true\">  <property name=\"usersFile\">   <value>testusers.xml</value>   </property>  </realm> </realms>";

			replaceMOMrelamXml("</realms>", replaceStr);

			LOGGER.info("User modification successful for relam xml file :::");

			backupFile(momusersxmlpath, momtestusersxmlpath, MOM_MACHINE_ID);

			LOGGER.info("testusers.xml successful for created in config  :::");
			Assert.assertEquals(
					XMLUtil.deleteUser(momtestusersxmlpath, "cemadmin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(momtestusersxmlpath, "SaasAdmin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(momtestusersxmlpath, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(momtestusersxmlpath, "Guest"),
					XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath,
					"CEM Configuration Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath,
					"CEM System Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteGroup(momtestusersxmlpath, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath,
					"CEM Tenant Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteGroup(momtestusersxmlpath, "CEM Analyst"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath,
					"CEM Incident Analyst"), XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("testusers.xml user and group deleted :::");

			Assert.assertEquals(XMLUtil.addUser(momtestusersxmlpath, userName,
					userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.addGroup(momtestusersxmlpath,
					"Global Conspiracy", groupName, userName),
					XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("testusers.xml user and group added :::");

			Assert.assertEquals(XMLUtil.grantPermissionGroupDomainXml(
					momdomainsxmlpath, groupName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.grantPermissionGroupServerXml(
					momserverxmlpath, groupName, permission),
					XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("domain.xml  and server.xml group permission added :::");

			startEMServices();
			checkCollectorsToMOMConnectivity();
			LOGGER.info("user login validation start :::");

			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					momHost, momPort, userName, userPassword, clwJarFileLoc,
					momlogDir, userLogInMsg(userName));
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to testusers.xml", userlogincheck);
			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEMServices();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_351842_Add_Multiple_Groups_To_Custom_Domain_With_READ_Permission() {

		try {
			testCaseId = "351842";
			testCaseName = "verify_ALM_351842_Add_Multiple_Groups_To_Custom_Domain_With_READ_Permission";
			domainName = "Test";
			String group1 = "One";
			String group2 = "Two";
			String userName1 = "one";
			String userName2 = "two";
			userPassword = "";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			groupMap.clear();
			groupMap.put(group1, readPermission);
			groupMap.put(group2, readPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);

			Assert.assertEquals(
					XMLUtil.addUser(momusersxmlpath, userName1, userPassword),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.addUser(momusersxmlpath, userName2, userPassword),
					XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(
					XMLUtil.addGroup(momusersxmlpath, "", group1, userName1),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.addGroup(momusersxmlpath, "", group2, userName2),
					XMLUtil.SUCCESS_MESSAGE);

			startTestBed();
			checkCollectorsToMOMConnectivity();
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					momHost, momPort, userName1, userPassword, clwJarFileLoc,
					momlogDir, userLogInMsg(userName1));
			Assert.assertTrue("Login was not successful for the user "
					+ userName1 + " added to users.xml", userlogincheck);

			Assert.assertFalse(tomcatAgentTurnOff(userName2, userPassword, true));

			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					momHost, momPort, userName2, userPassword, clwJarFileLoc,
					momlogDir, userLogInMsg(userName1));
			Assert.assertTrue("Login was not successful for the user "
					+ userName2 + " added to users.xml", userlogincheck);

			Assert.assertFalse(tomcatAgentTurnOff(userName2, userPassword, true));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "FULL" }, enabled = true)
	public void verify_ALM_351824_User_Auth_Non_Existing_Agent_Configured_in_Domain() {

		try {
			testCaseId = "351824";
			testCaseName = "verify_ALM_351824_User_Auth_Non_Existing_Agent_Configured_in_Domain";
			String userName1 = "user1";
			String userName2 = "user2";
			String domainName1 = "domain1";
			String domain1agentMapping = "(.*)Tomcat(.*)";
			String domainName2 = "domain2";
			String domain2agentMapping = "(.*)ABCD(.*)";
			userPassword = "";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);

			userMap.clear();
			userMap.put(userName1, fullPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName1, "",
					domain1agentMapping, userMap, null);
			userMap.clear();
			userMap.put(userName2, fullPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName2, "",
					domain2agentMapping, userMap, null);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(jbossAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);

			tempResult1 = clw.getNodeList(userName1, userPassword, ".*",
					momHost, Integer.parseInt(momPort), momLibDir).toString();
			Assert.assertTrue(
					"Agents not reporting correctly for user " + userName1,
					tempResult1.contains("Tomcat")
							&& !tempResult1.contains("JBoss"));
			tempResult1 = clw.getNodeList(userName2, userPassword, ".*",
					momHost, Integer.parseInt(momPort), momLibDir).toString();
			Assert.assertTrue("Agents not reporting correctly for user "
					+ userName1, !tempResult1.contains("Tomcat")
					&& !tempResult1.contains("JBoss"));
			tempResult1 = clw.getNodeList(user, password, ".*", momHost,
					Integer.parseInt(momPort), momLibDir).toString();
			Assert.assertTrue(
					"Agents not reporting correctly for user " + user,
					tempResult1.contains("Tomcat")
							&& tempResult1.contains("JBoss"));
		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351833_Assign_multiple_users_permissions_for_a_domain() {

		try {

			testCaseId = "351833";
			testCaseName = "verify_ALM_351833_Assign_multiple_users_permissions_for_a_domain";
			String userName1 = "one";
			String userName2 = "two";
			userPassword = "";
			domainName = "Test";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(jbossAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);

			userMap.clear();
			userMap.put(userName1, readPermission);
			userMap.put(userName2, fullPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			harvestWait(60);

			agentExpression = "(.*)";
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1).toString();

			Assert.assertTrue(
					"All the Agents are not reporting under the right domain",
					tempResult1.contains("SuperDomain/" + domainName + ","
							+ tomcatHost + ",Tomcat")
							&& tempResult1.contains("SuperDomain/" + domainName
									+ "," + jbossHost + ",JBoss"));
			copyFile(momConfigDir + "/modules/" + managementModuleJAR,
					momConfigDir + "/modules/" + domainName + "/"
							+ managementModuleJAR, MOM_MACHINE_ID);

			restartMOM();
			Assert.assertFalse(
					"user "
							+ userName1
							+ " was able to deactivate MM though it has only read permissions",
					clw.deactivateManamementModule(userName1, userPassword,
							momHost, momPort, momLibDir, managementModuleName));
			Assert.assertTrue(
					"user "
							+ userName2
							+ " was unable to deactivate MM though it has full permissions",
					clw.deactivateManamementModule(userName2, userPassword,
							momHost, momPort, momLibDir, managementModuleName));
			Assert.assertTrue(
					"user "
							+ userName2
							+ " was unable to activate MM though it has full permissions",
					clw.activateManamementModule(userName2, userPassword,
							momHost, momPort, momLibDir, managementModuleName));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351839_Add_a_group_to_SuperDomain() {

		try {

			testCaseId = "351839";
			testCaseName = "verify_ALM_351839_Add_a_group_to_SuperDomain";
			domainGroupName = "Music";
			domainUserName = "Music";
			userPassword = "";
			description = "Music Group";
			String loginmsg = "User \"" + domainUserName
					+ "\" connected successfully";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);

			startEMServices();
			startTomcatAgent();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			machineID = getAgentConnectedColMachineId(tomcatAgentExp);

			XMLUtil.createGroupGrantForSuperDomain(momdomainsxmlpath,
					domainGroupName, readPermission);

			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName, domainUserName);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName, userPassword), XMLUtil.SUCCESS_MESSAGE);

			Assert.assertTrue("Login was not successful for the user "
					+ domainUserName + " added to domains.xml", apmbaseutil
					.validateUserLoginWithCheckingAgents(momHost, momPort,
							domainUserName, userPassword, clwJarFileLoc,
							momlogDir, loginmsg));

			Assert.assertTrue(hitURL(tomcatAgentURL, machineID));
			Assert.assertFalse(
					domainUserName
							+ " user is able to run tracer though is has only read permissions",
					clw.verifyTransactionTrace(domainUserName, userPassword,
							".*", momHost, Integer.parseInt(momPort), momLibDir));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEMServices();
			stopTomcatAgent();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351840_Assign_a_group_with_one_user_to_a_custom_domain() {

		try {

			testCaseId = "351840";
			testCaseName = "verify_ALM_351840_Assign_a_group_with_one_user_to_a_custom_domain";
			domainName = "Test";
			domainGroupName = "Test";
			domainUserName = "Ant";
			userPassword = "Ant";
			description = "Test Group";
			String encrypteduserPassword;
			String loginmsg = "User \"" + domainUserName
					+ "\" connected successfully";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);

			encrypteduserPassword = apmbaseutil.encryptPassword(momHome
					+ "/tools", userPassword);

			groupMap.clear();
			groupMap.put(domainGroupName, fullPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);

			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName, domainUserName);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName, encrypteduserPassword),
					XMLUtil.SUCCESS_MESSAGE);

			startEMServices();
			startTomcatAgent();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			machineID = getAgentConnectedColMachineId(tomcatAgentExp);

			Assert.assertTrue("Login was not successful for the user "
					+ domainUserName + " added to domains.xml", apmbaseutil
					.validateUserLoginWithCheckingAgents(momHost, momPort,
							domainUserName, userPassword, clwJarFileLoc,
							momlogDir, loginmsg));
			Assert.assertTrue(hitURL(tomcatAgentURL, machineID));
			Assert.assertTrue(
					domainUserName
							+ " user is unable to run tracer though is has full permissions",
					clw.verifyTransactionTrace(domainUserName, userPassword,
							".*", momHost, Integer.parseInt(momPort), momLibDir));

			agentExpression = "(.*SuperDomain/" + domainName + ".*)|(.*)";

			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName,
					userPassword, agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1);
			Assert.assertTrue(validateMetricValue(listoutput, domainName,
					"Tomcat Agent"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEMServices();
			stopTomcatAgent();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void verify_ALM_351841_Assign_multiple_users_to_a_single_group() {

		try {

			testCaseId = "351841";
			testCaseName = "verify_ALM_351841_Assign_multiple_users_to_a_single_group";
			domainName = "Test";
			domainGroupName = "Test";
			description = "Test Group";
			String userName1 = "Ant";
			String userName2 = "Bob";
			String loginmsg;
			userPassword = "";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			groupMap.clear();
			groupMap.put(domainGroupName, fullPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);

			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					momusersxmlpath, description, domainGroupName, userName1
							+ "," + userName2), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(jbossAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);

			loginmsg = "User \"" + userName1 + "\" connected successfully";

			Assert.assertTrue("Login was not successful for the user "
					+ userName1 + " added to domains.xml", apmbaseutil
					.validateUserLoginWithCheckingAgents(momHost, momPort,
							userName1, userPassword, clwJarFileLoc, momlogDir,
							loginmsg));
			loginmsg = "User \"" + userName2 + "\" connected successfully";

			Assert.assertTrue(
					userName1
							+ " is not able to run tracer though is has full permissions",
					clw.verifyTransactionTracewithnoTraces(userName1,
							userPassword, "(.*)", momHost,
							Integer.parseInt(momPort), momLibDir));

			Assert.assertTrue(
					userName2
							+ " is not able to run tracer though is has full permissions",
					clw.verifyTransactionTracewithnoTraces(userName2,
							userPassword, "(.*)", momHost,
							Integer.parseInt(momPort), momLibDir));

			copyFile(momConfigDir + "/modules/" + managementModuleJAR,
					momConfigDir + "/modules/" + domainName + "/"
							+ managementModuleJAR, MOM_MACHINE_ID);

			restartMOM();
			Assert.assertTrue(
					"user "
							+ userName1
							+ " was not able to deactivate MM though it has full permissions",
					clw.deactivateManamementModule(userName1, userPassword,
							momHost, momPort, momLibDir, managementModuleName));

			Assert.assertTrue(
					"user "
							+ userName2
							+ " was not able to activate MM though it has full permissions",
					clw.activateManamementModule(userName2, userPassword,
							momHost, momPort, momLibDir, managementModuleName));

			agentExpression = "(.*SuperDomain/" + domainName + ".*)|(.*)";

			listoutput = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1);
			Assert.assertTrue(validateMetricValue(listoutput, domainName,
					"Tomcat Agent"));
			Assert.assertTrue(validateMetricValue(listoutput, domainName,
					"JBoss Agent"));
			LOGGER.info("All AGents are listed under custom domain and reporting data...");

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}

	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351845_Assign_live_agent_control_permission_to_a_group_with_a_single_user() {

		try {

			testCaseId = "351845";
			testCaseName = "verify_ALM_351845_Assign_live_agent_control_permission_to_a_group_with_a_single_user";
			domainName = "Test";
			domainGroupName = "Test";
			description = "LiveAgent permission for group";
			domainUserName = "one";
			userPassword = "";
			String loginmsg;
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);

			groupMap.clear();
			groupMap.put(domainGroupName, liveagentcontrolPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);
			XMLUtil.createGroupGrantForElement(momdomainsxmlpath, "domain",
					domainGroupName, readPermission);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName, domainUserName);

			startEMServices();
			startTomcatAgent();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			machineID = getAgentConnectedColMachineId(tomcatAgentExp);

			loginmsg = "User \"" + domainUserName + "\" connected successfully";
			Assert.assertTrue("Login was not successful for the user "
					+ domainUserName + " added to domains.xml", apmbaseutil
					.validateUserLoginWithCheckingAgents(momHost, momPort,
							domainUserName, userPassword, clwJarFileLoc,
							momlogDir, loginmsg));

			Assert.assertTrue(
					"AgentTurnoff was not Sucessful with user "
							+ domainUserName
							+ "though it has live agent control permissions",
					agentTurnOff(domainUserName, "", tomcatAgentProcess,
							tomcatAgentExp, true, machineID));
			Assert.assertTrue(agentTurnOff(domainUserName, "",
					tomcatAgentProcess, tomcatAgentExp, false, machineID));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEMServices();
			stopTomcatAgent();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}

	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void verify_ALM_351846_Assign_run_tracer_permission_to_a_group_with_multiple_users() {

		try {

			testCaseId = "351846";
			testCaseName = "verify_ALM_351846_Assign_run_tracer_permission_to_a_group_with_multiple_users";
			domainName = "Test";
			domainGroupName = "Test";
			domainUserName = "one";
			userPassword = "";
			description = "Run tracer permission for group";
			String loginmsg;
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);

			groupMap.clear();
			groupMap.put(domainGroupName, runtracerPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);
			XMLUtil.createGroupGrantForElement(momdomainsxmlpath, "domain",
					domainGroupName, readPermission);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName, domainUserName);

			startEMServices();

			loginmsg = "User \"" + domainUserName + "\" connected successfully";
			Assert.assertTrue("Login was not successful for the user "
					+ domainUserName + " added to domains.xml", apmbaseutil
					.validateUserLoginWithCheckingAgents(momHost, momPort,
							domainUserName, userPassword, clwJarFileLoc,
							momlogDir, loginmsg));
			Assert.assertTrue(
					domainUserName
							+ " is not able to run tracer though is has full permissions",
					clw.verifyTransactionTracewithnoTraces(domainUserName,
							userPassword, "(.*)", momHost,
							Integer.parseInt(momPort), momLibDir));
		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEMServices();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}

	}

	@Test(groups = { "SMOKE" }, enabled = true)
	public void verify_ALM_351850_Realms_create_error_in_realms_xml_and_start_em() {

		try {

			testCaseId = "351850";
			testCaseName = "verify_ALM_351850_Realms_create_error_in_realms_xml_and_start_em";
			String userElementName = "property";
			String userAttrName = "name";
			String userAttrOldValue = "usersFile";
			String userAttrNewValue = "";
			String logerrormsg = "[ERROR] [main] [Manager] The EM failed to start. Local Users and Groups realm is misconfigured. Invalid property specified";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);

			changeAttributeValue(momrealmsxmlpath, userElementName,
					userAttrName, userAttrOldValue, userAttrNewValue,
					MOM_MACHINE_ID);
			changeAttributeValue(col1realmsxmlpath, userElementName,
					userAttrName, userAttrOldValue, userAttrNewValue,
					COLLECTOR1_MACHINE_ID);
			changeAttributeValue(col2realmsxmlpath, userElementName,
					userAttrName, userAttrOldValue, userAttrNewValue,
					COLLECTOR2_MACHINE_ID);

			startEMServices();

			checkLogForMsg(envProperties, MOM_MACHINE_ID, momlogFile,
					logerrormsg);
			checkLogForMsg(envProperties, COLLECTOR1_MACHINE_ID,
					collector1logFile, logerrormsg);
			checkLogForMsg(envProperties, COLLECTOR2_MACHINE_ID,
					collector2logFile, logerrormsg);
			LOGGER.info("MOM and 2 collectors failed to start as expected");

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEMServices();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}

	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351832_Assign_multiple_permissions_to_a_single_user_for_multiple_domains() {

		try {
			testCaseId = "351832";
			testCaseName = "verify_ALM_351832_Assign_multiple_permissions_to_a_single_user_for_multiple_domains";
			userName = "Bob";
			domainUserName = "superstar";
			userPassword = "";
			String tomcatAgentdomainName = "One";
			String jbossAgentdomainName = "Two";
			String allAgentsdomainName = "Three";
			String domain1agentMapping = "(.*)Tomcat(.*)";
			String domain2agentMapping = "(.*)JBoss(.*)";
			String domain3agentMapping = "(.*)";
			String newDashboardName = "Automation_Dashboard";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					momdomainsxmlpath, userName, readPermission),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					momdomainsxmlpath, userName,
					historicalagentcontrolPermission), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					momdomainsxmlpath, userName, fullPermission),
					XMLUtil.SUCCESS_MESSAGE);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(jbossAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			machineID = getAgentConnectedColMachineId(tomcatAgentExp);

			LOGGER.info("Checking if the user " + userName
					+ " has full permissions");

			Assert.assertTrue(agentTurnOff(userName, "", tomcatAgentProcess,
					tomcatAgentExp, true, machineID));
			LOGGER.info("AgentTurnoff was Sucessful with user " + userName);
			Assert.assertTrue(agentTurnOff(userName, "", tomcatAgentProcess,
					tomcatAgentExp, false, machineID));

			Assert.assertTrue(clw.renameDashboard(userName, "", momHost,
					momPort, momLibDir, dashboardName, managementModuleName,
					newDashboardName));
			LOGGER.info("Renaming a dashboard was successful with user "
					+ userName);
			Assert.assertTrue(clw.renameDashboard(userName, "", momHost,
					momPort, momLibDir, newDashboardName, managementModuleName,
					dashboardName));

			Assert.assertTrue(
					userName
							+ " is not able to run tracer though is has full permissions",
					clw.verifyTransactionTracewithnoTraces(userName,
							userPassword, "(.*)", momHost,
							Integer.parseInt(momPort), momLibDir));

			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName, userPassword), XMLUtil.SUCCESS_MESSAGE);

			userMap.clear();
			userMap.put(domainUserName, readPermission);
			XMLUtil.createDomain(momdomainsxmlpath, tomcatAgentdomainName, "",
					domain1agentMapping, userMap, null);
			userMap.clear();
			userMap.put(domainUserName, writePermission);
			XMLUtil.createDomain(momdomainsxmlpath, jbossAgentdomainName, "",
					domain2agentMapping, userMap, null);
			userMap.clear();
			userMap.put(domainUserName, fullPermission);
			XMLUtil.createDomain(momdomainsxmlpath, allAgentsdomainName, "",
					domain3agentMapping, userMap, null);

			harvestWait(120);

			agentExpression = "(.*SuperDomain/" + tomcatAgentdomainName
					+ ".*)|(.*)";

			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1);
			Assert.assertTrue(validateMetricValue(listoutput,
					tomcatAgentdomainName, "Tomcat Agent"));
			Assert.assertFalse(validateMetricValue(listoutput,
					tomcatAgentdomainName, "JBoss Agent"));
			LOGGER.info("Agents are reporting correctly for custom domain "
					+ tomcatAgentdomainName);

			agentExpression = "(.*SuperDomain/" + jbossAgentdomainName
					+ ".*)|(.*)";
			listoutput.clear();
			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1);
			Assert.assertFalse(validateMetricValue(listoutput,
					jbossAgentdomainName, "Tomcat Agent"));
			Assert.assertTrue(validateMetricValue(listoutput,
					jbossAgentdomainName, "JBoss Agent"));
			LOGGER.info("Agents are reporting correctly for custom domain "
					+ jbossAgentdomainName);

			agentExpression = "(.*SuperDomain/" + allAgentsdomainName
					+ ".*)|(.*)";
			listoutput.clear();
			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1);
			Assert.assertFalse(validateMetricValue(listoutput,
					allAgentsdomainName, "Tomcat Agent"));
			Assert.assertFalse(validateMetricValue(listoutput,
					allAgentsdomainName, "JBoss Agent"));
			LOGGER.info("Agents are reporting correctly for custom domain "
					+ allAgentsdomainName);

			LOGGER.info("Checking if the user " + domainUserName
					+ " has full permissions");

			Assert.assertTrue(
					domainUserName
							+ " is not able to run tracer though is has full permissions",
					clw.verifyTransactionTracewithnoTraces(domainUserName,
							userPassword, "(.*)", momHost,
							Integer.parseInt(momPort), momLibDir));

			copyFile(momConfigDir + "/modules/" + managementModuleJAR,
					momConfigDir + "/modules/" + allAgentsdomainName + "/"
							+ managementModuleJAR, MOM_MACHINE_ID);

			restartMOM();
			Assert.assertTrue(
					"user "
							+ domainUserName
							+ " was unable to deactivate MM though it has full permissions",
					clw.deactivateManamementModule(domainUserName,
							userPassword, momHost, momPort, momLibDir,
							managementModuleName));
			Assert.assertTrue(
					"user "
							+ domainUserName
							+ " was unable to activate MM though it has full permissions",
					clw.activateManamementModule(domainUserName, userPassword,
							momHost, momPort, momLibDir, managementModuleName));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "DEEP" }, enabled = true)
	public void verify_ALM_351848_Create_multiple_domains_with_multiple_groups_permissions() {

		try {
			testCaseId = "351848";
			testCaseName = "verify_ALM_351848_Create_multiple_domains_with_multiple_groups_permissions";
			String domainName1 = "One";
			String domainName2 = "Test";
			String domainGroupName1 = "One";
			String domainGroupName2 = "Two";
			String domainGroupName3 = "Three";
			String tomcatagentMapping = "(.*)Tomcat(.*)";
			String jbossagentMapping = "(.*)JBoss(.*)";
			String domainUserName1 = "one";
			String domainUserName2 = "two";
			String domainUserName3 = "three";
			String tomcatConnectedMachineID;
			String jbossConnectedMachineID;
			userPassword = "";
			description = "";
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			groupMap.clear();
			groupMap.put(domainGroupName1, readPermission);
			groupMap.put(domainGroupName2, readPermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName1, description,
					tomcatagentMapping, null, groupMap);

			groupMap.clear();
			groupMap.put(domainGroupName1, fullPermission);
			groupMap.put(domainGroupName3, writePermission);
			XMLUtil.createDomain(momdomainsxmlpath, domainName2, description,
					jbossagentMapping, null, groupMap);

			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath,
					domainUserName3, userPassword), XMLUtil.SUCCESS_MESSAGE);
			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName1, domainUserName1);
			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName2, domainUserName2);
			XMLUtil.createGroupAddSingleUserInUsersXML(momusersxmlpath,
					description, domainGroupName3, domainUserName3);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(jbossAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			tomcatConnectedMachineID = getAgentConnectedColMachineId(tomcatAgentExp);
			jbossConnectedMachineID = getAgentConnectedColMachineId(jbossAgentExp);

			LOGGER.info("Checking permissions of user " + domainUserName1);
			Assert.assertFalse(
					"TomcatAgent Turnoff was Successful though user "
							+ domainUserName1
							+ "has only read permissions on domain "
							+ domainName1,
					agentTurnOff(domainUserName1, "", tomcatAgentProcess,
							tomcatAgentExp, true, tomcatConnectedMachineID));

			Assert.assertTrue(
					"JBoss Turnoff was not Successful though user "
							+ domainUserName1
							+ "has full permissions on damain " + domainName2,
					agentTurnOff(domainUserName1, "", jbossAgentProcess,
							jbossAgentExp, true, jbossConnectedMachineID));
			Assert.assertTrue(agentTurnOff(domainUserName1, "",
					jbossAgentProcess, jbossAgentExp, false,
					jbossConnectedMachineID));

			LOGGER.info("Checking permissions of user " + domainUserName2);
			Assert.assertFalse(
					"TomcatAgent Turnoff was Successful though user "
							+ domainUserName2
							+ "has only read permissions on domain "
							+ domainName1,
					agentTurnOff(domainUserName1, "", tomcatAgentProcess,
							tomcatAgentExp, true, tomcatConnectedMachineID));

			LOGGER.info("Checking permissions of user " + domainUserName3);
			Assert.assertFalse(
					"JBoss Turnoff was Successful though user "
							+ domainUserName3
							+ "has only write permissions on damain "
							+ domainName2,
					agentTurnOff(domainUserName3, "", jbossAgentProcess,
							jbossAgentExp, true, jbossConnectedMachineID));
			copyFile(momConfigDir + "/modules/" + managementModuleJAR,
					momConfigDir + "/modules/" + domainName2 + "/"
							+ managementModuleJAR, MOM_MACHINE_ID);
			restartMOM();
			Assert.assertTrue(
					"user "
							+ domainUserName3
							+ " was not able to deactivate MM though it has write permissions",
					clw.deactivateManamementModule(domainUserName3,
							userPassword, momHost, momPort, momLibDir,
							managementModuleName));

			Assert.assertTrue(
					"user "
							+ domainUserName3
							+ " was not able to activate MM though it has write permissions",
					clw.activateManamementModule(domainUserName3, userPassword,
							momHost, momPort, momLibDir, managementModuleName));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_351849_All_agents_visible_under_SuperDomain() {

		try {
			testCaseId = "351849";
			testCaseName = "verify_ALM_351849_All_agents_visible_under_SuperDomain";
			domainName = "One";
			agentMapping = "(.*)Tomcat(.*)";
			String agenttag = ApmbaseConstants.AGENT_TAG;
			String superdomainliteral = ApmbaseConstants.SUPERDOMAIN_LITERAL;
			String mappingtag = ApmbaseConstants.MAPPING_TAG;
			testCaseStart(testCaseName);

			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(COLLECTOR2_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			rolesInvolved.add(JBOSS_ROLE_ID);

			XMLUtil.createDomain(momdomainsxmlpath, domainName, "",
					agentMapping, null, null);
			Assert.assertEquals(XMLUtil.changeAttributeValueWithparentNode(
					momdomainsxmlpath, agenttag, superdomainliteral,
					mappingtag, "(.*)", "(.*)JBoss(.*)"),
					XMLUtil.SUCCESS_MESSAGE);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(jbossAgentExp, momHost,
					Integer.parseInt(momPort), momLibDir);

			LOGGER.info("Checking if all the agents exists under SuperDomain");

			agentExpression = "/*SuperDomain/*/|(.*)";
			harvestWait(60);

			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricName, momHost,
					Integer.parseInt(momPort), momLibDir, 1).toString();

			Assert.assertTrue(
					"Tomcat Agent Metrics does not exist under Superdomain",
					tempResult1.contains("Tomcat Agent"));
			Assert.assertTrue(
					"JBoss Agent Metrics does not exist under Superdomain",
					tempResult1.contains("JBoss Agent"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseName
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
		}
	}

	private boolean tomcatAgentTurnOff(String user, String password,
			boolean turnOff) {
		String turnOnOrOffResultString = "";
		turnOnOrOffResultString = "Process=\"Tomcat\" Shutoff=\"true\"";

		metricExpression = "(.*)Tomcat Agent";
		if (turnOff) {
			clw.turnOffAgents(user, password, metricExpression, momHost,
					Integer.parseInt(momPort), momLibDir);
			return checkForKeyword(envProperties, MOM_MACHINE_ID,
					metricshutoffconfxmlpath, turnOnOrOffResultString, false);
		} else {

			clw.turnOnAgents(user, password, metricExpression, momHost,
					Integer.parseInt(momPort), momLibDir);
			waitForAgentNodes(metricExpression, momHost,
					Integer.parseInt(momPort), momLibDir);
			return true;
		}

	}

	private boolean validateMetricValue(List<String> result, String firstText,
			String secondText) {
		boolean flag = false;
		for (int i = 0; i < result.size(); i++) {
			LOGGER.info("Value is..." + result.get(i));
			if (result.get(i).contains(firstText)
					&& result.get(i).contains(secondText)) {
				flag = true;
			}
		}
		return flag;
	}

	private String userLogInMsg(String userName) {
		return "User \"" + userName + "\" connected successfully";
	}

	private void replaceMOMrelamXml(String findStr, String replaceStr) {
		replaceProp(findStr, replaceStr, MOM_MACHINE_ID, momrealmsxmlpath);
	}

}
