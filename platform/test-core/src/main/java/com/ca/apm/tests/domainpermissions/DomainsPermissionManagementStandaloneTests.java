package com.ca.apm.tests.domainpermissions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.tests.base.StandAloneEMOneTomcatOneJBossTestsBase;

public class DomainsPermissionManagementStandaloneTests extends
		StandAloneEMOneTomcatOneJBossTestsBase {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DomainsPermissionManagementStandaloneTests.class);
	ApmbaseUtil apmbaseutil = new ApmbaseUtil();
	CLWCommons clw = new CLWCommons();
	private String userName, userName1, userName2, userPassword, permission,
			permission1, permission2, readPermission, fullPermission;
	private String domainName, domainName1, domainName2, agentMapping,
			domain1agentMapping;
	private String domain2agentMapping, domainUserName, tempResult1,
			domainGroupName, domainGroupName1, domainGroupName2;
	private String agentExpression;
	private String metricExpression;
	private boolean userlogincheck, tracesExists;
	private String userElementName;
	private String userAttrName;
	private String userAttrOldValue;
	private String userAttrNewValue;
	private String msg1, msg2;
	Map<String, String> groupMap = new HashMap<String, String>();
	Map<String, String> userMap = new HashMap<String, String>();
	List<String> listoutput = new ArrayList<String>();

	@BeforeClass(alwaysRun = true)
	public void initialize() {

		// enable domainsconfiguration dynamic update property
		replaceEMProperty(domainconfigdynamicupdateprop + "=false",
				domainconfigdynamicupdateprop + "=true");
		// set jboss agent naming property to false
		replaceJBossAgentProperty(
				"introscope.agent.agentAutoNamingEnabled=true",
				"introscope.agent.agentAutoNamingEnabled=false");
		readPermission = "read";
		fullPermission = "full";
		backupEMFiles();
		backupAgentFiles();
		syncMachines();

	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_305587_Changes_to_users_xml_file_without_restarting_the_EM_add_user_to_a_custom_domain() {

		try {
			testcaseId = "305587";
			testCaseNameIDPath = "verify_ALM_305587_Changes_to_users_xml_file_without_restarting_the_EM_add_user_to_a_custom_domain";
			testCaseStart(testCaseNameIDPath);
			userName = "qauser";
			userPassword = "";
			permission = "read";
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			startEM();
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			harvestWait(60);
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);
			domainName = "qauser1";
			agentMapping = "(.*)";
			domainUserName = "qauser1";
			userMap.clear();
			userMap.put(domainUserName, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					domainUserName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			harvestWait(60);
			msg1 = "User \"" + domainUserName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, domainUserName, userPassword,
					clwJarFileLoc, emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ domainUserName + " added to domains.xml", userlogincheck);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_280483_Authenticating_User_Not_Part_of_any_Domain() {

		try {
			testcaseId = "280483";
			testCaseNameIDPath = "verify_ALM_280483_Authenticating_User_Not_Part_of_any_Domain";
			testCaseStart(testCaseNameIDPath);
			userName = "user1";
			userPassword = "user1";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertFalse("Login was successful for the user " + userName
					+ " added to domains.xml which is not expected",
					userlogincheck);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_305591_User_Auth_Non_Existing_Agent_Configured_in_Domain() {

		try {
			testcaseId = "305591";
			testCaseNameIDPath = "verify_ALM_305591_User_Auth_Non_Existing_Agent_Configured_in_Domain";
			testCaseStart(testCaseNameIDPath);
			userName1 = "user1";
			userName2 = "user2";
			userPassword = "";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			domainName1 = "domain1";
			domain1agentMapping = "(.*)Tomcat(.*)";
			domainName2 = "domain2";
			agentMapping = "(.*)ABCD(.*)";
			permission = "full";
			userMap.clear();
			userMap.put(userName1, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName1, "",
					domain1agentMapping, userMap, null);
			userMap.clear();
			userMap.put(userName2, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName2, "",
					domain2agentMapping, userMap, null);
			startTestBed();
			tempResult1 = clw.getNodeList(userName1, userPassword, ".*",
					emHost, Integer.parseInt(emPort), emLibDir).toString();
			Assert.assertTrue(
					"Agents not reporting correctly for user " + userName1,
					tempResult1.contains("Tomcat")
							&& !tempResult1.contains("JBoss"));
			tempResult1 = clw.getNodeList(userName2, userPassword, ".*",
					emHost, Integer.parseInt(emPort), emLibDir).toString();
			Assert.assertTrue("Agents not reporting correctly for user "
					+ userName1, !tempResult1.contains("Tomcat")
					&& !tempResult1.contains("JBoss"));
			tempResult1 = clw.getNodeList(user, userPassword, ".*", emHost,
					Integer.parseInt(emPort), emLibDir).toString();
			Assert.assertTrue(
					"Agents not reporting correctly for user " + user,
					tempResult1.contains("Tomcat")
							&& tempResult1.contains("JBoss"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertEMFiles();
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_280484_SuperDomain_domains_xml_matching_All_and_any_agents() {

		try {
			testcaseId = "280484";
			testCaseNameIDPath = "verify_ALM_280484_SuperDomain_domains_xml_matching_All_and_any_agents";
			testCaseStart(testCaseNameIDPath);
			apmbaseutil.chkforSuperDomainNode(emdomainsxmlpath);
			startTestBed();
			agentExpression = "/*SuperDomain/*/|(.*)";
			metricExpression = "EM Host";

			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain," + tomcatHost
							+ ",Tomcat,Tomcat Agent"));
			Assert.assertTrue(
					"JBoss Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain," + jbossHost
							+ ",JBoss,JBoss Agent"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertEMFiles();
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_280485_Non_existing_user_and_group() {

		try {
			testcaseId = "280485";
			testCaseNameIDPath = "verify_ALM_280485_Non_existing_user_and_group";
			testCaseStart(testCaseNameIDPath);
			replaceEMProperty("log4j.logger.Manager=INFO, console, logfile",
					"log4j.logger.Manager=DEBUG, console, logfile");
			userName = "Silly";
			permission = "Admin";
			msg1 = "[DEBUG] [main] [Manager] Error starting up EM";
			msg2 = "Invalid permission \"" + permission + "\" for user \""
					+ userName + "\" in resource \"SuperDomain\"";
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			startEM();
			checkEMLogForMsg(msg1);
			checkEMLogForMsg(msg2);
			Assert.assertEquals(XMLUtil.deleteElement(emdomainsxmlpath,
					"grant", "user", userName), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupGrantForSuperDomain(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			startEM();
			checkEMLogForMsg(msg1);
			checkEMLogForMsg(msg2);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305609_Add_a_group_to_SuperDomain() {

		try {
			testcaseId = "305609";
			testCaseNameIDPath = "verify_ALM_305609_Add_a_group_to_SuperDomain";
			testCaseStart(testCaseNameIDPath);
			userName = "qauser";
			userPassword = "";
			permission = "read";
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "qauser Group", userName, userName),
					XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "User " + userName
							+ " does not have any permissions for Activity");
			Assert.assertTrue(
					userName
							+ " is able to run tracer though is has got only read permission failed",
					tracesExists);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_305612_Add_multiple_groups_to_custom_domain_with_read_permission() {

		try {
			testcaseId = "305612";
			testCaseNameIDPath = "verify_ALM_305609_Add_a_group_to_SuperDomain";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			agentMapping = "(.*)";
			groupMap.clear();
			domainGroupName1 = "One";
			domainGroupName2 = "Two";
			permission = "read";
			groupMap.put(domainGroupName1, permission);
			groupMap.put(domainGroupName2, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);
			permission = "read";
			userName1 = "one";
			userName2 = "two";
			userPassword = "";
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName1, userName1),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName2, userName2),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "User \"" + userName1 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName1, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);
			msg1 = "User \"" + userName2 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName2, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName1,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "User " + userName1
							+ " does not have any permissions for Activity");
			Assert.assertTrue(
					userName1
							+ " is able to run tracer though is has got only read permission failed",
					tracesExists);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName2,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "User " + userName2
							+ " does not have any permissions for Activity");
			Assert.assertTrue(
					userName2
							+ " is able to run tracer though is has got only read permission failed",
					tracesExists);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_305623_Realms_create_error_in_realms_xml_and_start_em() {

		try {
			testcaseId = "305623";
			testCaseNameIDPath = "verify_ALM_305623_Realms_create_error_in_realms_xml_and_start_em";
			testCaseStart(testCaseNameIDPath);
			replaceEMProperty("log4j.logger.Manager=INFO, console, logfile",
					"log4j.logger.Manager=DEBUG, console, logfile");
			LOGGER.info("Creating Error in realms.xml file");
			userElementName = "property";
			userAttrName = "name";
			userAttrOldValue = "usersFile";
			userAttrNewValue = "";
			Assert.assertEquals(XMLUtil.changeAttributeValue(emrealmsxmlpath,
					userElementName, userAttrName, userAttrOldValue,
					userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "[DEBUG] [main] [Manager] Error starting up EM";
			msg2 = "Local Users and Groups realm is misconfigured. Invalid property specified";
			checkEMLogForMsg(msg1);
			checkEMLogForMsg(msg2);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305592_SuperDomain_domains_xml_matching_only_a_few_of_All_Agents() {

		try {
			testcaseId = "305592";
			testCaseNameIDPath = "verify_ALM_305592_SuperDomain_domains_xml_matching_only_a_few_of_All_Agents";
			testCaseStart(testCaseNameIDPath);
			startTestBed();
			agentExpression = "/*SuperDomain/*/|(.*)";
			metricExpression = "EM Host";

			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain," + tomcatHost
							+ ",Tomcat,Tomcat Agent"));
			Assert.assertTrue(
					"JBoss Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain," + jbossHost
							+ ",JBoss,JBoss Agent"));

			XMLUtil.changeAttributeValue(emdomainsxmlpath, "agent", "mapping",
					"(.*)", "(.*)NONE(.*)");
			restartEM();
			harvestWait(60);
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent does not report after agent mapping is made to match none of the agents",
					tempResult1.contains("SuperDomain," + tomcatHost
							+ ",Tomcat,Tomcat Agent"));
			Assert.assertTrue(
					"JBoss Agent does not report after agent mapping is made to match none of the agents",
					tempResult1.contains("SuperDomain," + jbossHost
							+ ",JBoss,JBoss Agent"));

			XMLUtil.changeAttributeValue(emdomainsxmlpath, "agent", "mapping",
					"(.*)NONE(.*)", "(.*)Tomcat(.*)");
			restartEM();
			harvestWait(60);
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent does not report after agent mapping is made to match only Tomcat agents",
					tempResult1.contains("SuperDomain," + tomcatHost
							+ ",Tomcat,Tomcat Agent"));
			Assert.assertTrue(
					"JBoss Agent does not report after agent mapping is made to match only Tomcat agents",
					tempResult1.contains("SuperDomain," + jbossHost
							+ ",JBoss,JBoss Agent"));
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertEMFiles();
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_204943_Changes_to_users_xml_file_without_restarting_the_EM_add_user_to_a_custom_domain() {

		try {
			testcaseId = "204943";
			testCaseNameIDPath = "verify_ALM_204943_Changes_to_users_xml_file_without_restarting_the_EM_add_user_to_a_custom_domain";
			testCaseStart(testCaseNameIDPath);
			startEM();
			userName = "user1";
			userPassword = "";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			XMLUtil.addUserToAdminGroup(emusersxmlpath, userName);

			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);

			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName
							+ " is not able to run tracer though is has same permissions as Admin",
					tracesExists);

			metricExpression = "SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Port";
			clw.turnOffMetrics(userName, userPassword, metricExpression,
					emHost, Integer.parseInt(emPort), emLibDir);
			LOGGER.info("Checking if the user " + userName
					+ " has permissions to shutoff the metrics");
			msg1 = "MetricName=\"Port\" Shutoff=\"true\"";
			isKeywordInFile(envProperties, emMachineId,
					metricshutoffconfxmlpath, msg1);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_204941_Domains_xml_should_be_hot_configurable() {

		try {
			testcaseId = "204941";
			testCaseNameIDPath = "verify_ALM_204941_Domains_xml_should_be_hot_configurable";
			testCaseStart(testCaseNameIDPath);
			Assert.assertEquals(XMLUtil.deleteElement(emdomainsxmlpath,
					"grant", "group", user), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteElement(emserverxmlpath, "grant",
					"group", user), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			agentExpression = "(.*)";
			metricExpression = "(.*)GC Heap(.*)";

			listoutput = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertFalse(
					"Login was successful for the user "
							+ user
							+ " which was deleted from domains.xml which is not expected",
					listoutput.size() >= 3);

			revertFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
			revertFile(emserverxmlpath, emserverxmlpath_backup, emMachineId);
			msg1 = "Permissions file changed";
			checkEMLogForMsg(msg1);

			listoutput = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertTrue(
					"Login was not successful for the user "
							+ user
							+ " which was add back to domains.xml with hotconfig change",
					listoutput.size() >= 3);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_305628_Changes_to_users_xml_file_without_restarting_the_EM_delete_user_from_a_Domain() {

		try {
			testcaseId = "305628";
			testCaseNameIDPath = "verify_ALM_305628_Changes_to_users_xml_file_without_restarting_the_EM_delete_user_from_a_Domain";
			testCaseStart(testCaseNameIDPath);
			userName = "qauser";
			userPassword = "";
			permission = "read";
			Assert.assertEquals(XMLUtil.createUserGrantElement(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);
			Assert.assertEquals(XMLUtil.deleteElement(emusersxmlpath, "user",
					"name", userName), XMLUtil.SUCCESS_MESSAGE);
			harvestWait(60);
			agentExpression = "(.*)";
			metricExpression = "(.*)GC Heap(.*)";
			listoutput = clw.getMetricValueForTimeInMinutes(userName,
					userPassword, agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertFalse("Login was successful for the user " + userName
					+ " even after deleting from users.xml",
					listoutput.size() >= 3);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305627_TT50765_User_should_be_able_to_configure_plain_text_passwords() {

		try {
			testcaseId = "305627";
			testCaseNameIDPath = "verify_ALM_305627_TT50765_User_should_be_able_to_configure_plain_text_passwords";
			testCaseStart(testCaseNameIDPath);
			userElementName = "principals";
			userAttrName = "plainTextPasswords";
			userAttrOldValue = "false";
			userAttrNewValue = "true";
            userName = "user1";
            userPassword = "user1pwd";
            Assert.assertEquals(XMLUtil.grantPermissionUserDomainXml(emdomainsxmlpath, userName, readPermission),
                XMLUtil.SUCCESS_MESSAGE);
            startEM();
			Assert.assertEquals(XMLUtil.changeAttributeValue(emusersxmlpath,
					userElementName, userAttrName, userAttrOldValue,
					userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			harvestWait(60);
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to users.xml", userlogincheck);
			userAttrOldValue = "true";
			userAttrNewValue = "false";
			Assert.assertEquals(XMLUtil.changeAttributeValue(emusersxmlpath,
					userElementName, userAttrName, userAttrOldValue,
					userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
			userName = "user2";
			userPassword = "user2pwd";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			harvestWait(60);
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertFalse("Login was successful for the user " + userName
					+ " which is not expected", userlogincheck);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305602_Assign_multiple_users_permissions_for_a_domain() {

		try {
			testcaseId = "305602";
			testCaseNameIDPath = "verify_ALM_305602_Assign_multiple_users_permissions_for_a_domain";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			agentMapping = "(.*)";
			userName1 = "one";
			userName2 = "two";
			userPassword = "";
			permission1 = "read";
			permission2 = "full";
			userMap.clear();
			userMap.put(userName1, permission1);
			userMap.put(userName2, permission2);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			startJBossAgent();
			startEM();
			msg1 = "User \"" + userName1 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName1, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName1 + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName1,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "User " + userName1
							+ " does not have any permissions for Activity");
			Assert.assertTrue(
					userName1
							+ " is able to run tracer though is has only read permissions",
					tracesExists);
			msg1 = "User \"" + userName2 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName2, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName2 + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName2,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName2
							+ " is not able to run tracer though is has full permissions",
					tracesExists);
			metricExpression = "(.*)JBoss Agent";
			clw.turnOffAgents(userName2, userPassword, metricExpression,
					emHost, Integer.parseInt(emPort), emLibDir);
			msg1 = "Process=\"JBoss\" Shutoff=\"true\"";
			isKeywordInFile(envProperties, emMachineId,
					metricshutoffconfxmlpath, msg1);
			clw.turnOnAgents(userName2, userPassword, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopJBossAgent();
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_305611_Assign_multiple_users_to_a_single_group() {

		try {
			testcaseId = "305611";
			testCaseNameIDPath = "verify_ALM_305611_Assign_multiple_users_to_a_single_group";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			agentMapping = "(.*)";
			groupMap.clear();
			domainGroupName1 = "Test";
			permission = "full";
			groupMap.put(domainGroupName1, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);
			userName1 = "Ant";
			userName2 = "Bob";
			userPassword = "";
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "Test Group", domainGroupName1, userName1
							+ "," + userName2), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "User \"" + userName1 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName1, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName1 + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName1,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName1
							+ " is not able to run tracer though is has full permissions",
					tracesExists);
			msg1 = "User \"" + userName2 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName2, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName2 + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName2,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName2
							+ " is not able to run tracer though is has full permissions",
					tracesExists);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_305616_Assign_run_tracer_permission_to_a_group_with_multiple_users() {

		try {
			testcaseId = "305616";
			testCaseNameIDPath = "verify_ALM_305616_Assign_run_tracer_permission_to_a_group_with_multiple_users";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			domainGroupName1 = "Test";
			permission1 = "run_tracer";
			permission2 = "read";
			agentMapping = "(.*)";
			groupMap.clear();
			groupMap.put(domainGroupName1, permission1);
			groupMap.put(domainGroupName1, permission2);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);
			userName = "one";
			userPassword = "";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "Run tracer permission for group",
					domainGroupName1, userName), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName2
							+ " is not able to run tracer though is has full permissions",
					tracesExists);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305618_Create_multiple_domains_with_multiple_groups_permissions() {

		try {
			testcaseId = "305618";
			testCaseNameIDPath = "verify_ALM_305618_Create_multiple_domains_with_multiple_groups_permissions";
			testCaseStart(testCaseNameIDPath);
			userName = "one";
			userName1 = "two";
			userName2 = "three";
			userPassword = "";
			domainGroupName = "One";
			domainGroupName1 = "Two";
			domainGroupName2 = "Three";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName1, userName1),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName2, userName2),
					XMLUtil.SUCCESS_MESSAGE);

			domainName1 = "One";
			agentMapping = "(.*)Tomcat(.*)";
			permission = "read";
			groupMap.clear();
			groupMap.put(domainGroupName, permission);
			groupMap.put(domainGroupName1, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName1, "",
					agentMapping, null, groupMap);
			domainName2 = "Test";
			permission1 = "full";
			permission2 = "run_tracer";
			agentMapping = "(.*)JBoss(.*)";
			groupMap.clear();
			groupMap.put(domainGroupName, permission1);
			groupMap.put(domainGroupName2, permission2);
			XMLUtil.createDomain(emdomainsxmlpath, domainName2, "",
					agentMapping, null, groupMap);
			startTestBed();

			LOGGER.info("Checking if " + userName + " has read permissions");
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);

			LOGGER.info("Checking if " + userName
					+ " has full permissions for domain " + domainName1);
			metricExpression = "(.*)Tomcat Agent";
			tempResult1 = clw.turnOffAgents(userName, userPassword,
					metricExpression, emHost, Integer.parseInt(emPort),
					emLibDir).toString();
			msg1 = "Invalid operation: User " + userName
					+ " does not have sufficient permissions in domain "
					+ domainName1;
			Assert.assertTrue(
					"user "
							+ userName
							+ " is able to turn off metrics though is has only read permissions for domain "
							+ domainName1, tempResult1.contains(msg1));

			LOGGER.info("Checking if " + userName
					+ " has full permissions for domain " + domainName2);
			metricExpression = "(.*)JBoss Agent";
			clw.turnOffAgents(userName, userPassword, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
			msg1 = "Process=\"JBoss\" Shutoff=\"true\"";
			isKeywordInFile(envProperties, emMachineId,
					metricshutoffconfxmlpath, msg1);
			clw.turnOnAgents(userName, userPassword, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);

			LOGGER.info("Checking if " + userName1 + " has read permissions");
			msg1 = "User \"" + userName1 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName1, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName1 + " added to domains.xml", userlogincheck);

			LOGGER.info("Checking if " + userName1
					+ " has full permissions for domain " + domainName1);
			metricExpression = "(.*)Tomcat Agent";
			tempResult1 = clw.turnOffAgents(userName1, userPassword,
					metricExpression, emHost, Integer.parseInt(emPort),
					emLibDir).toString();
			msg1 = "Invalid operation: User " + userName1
					+ " does not have sufficient permissions in domain "
					+ domainName1;
			Assert.assertTrue(
					"user "
							+ userName1
							+ " is able to turn off metrics though is has only read permissions for domain "
							+ domainName1, tempResult1.contains(msg1));

			LOGGER.info("Checking if " + userName2 + " has read permissions");
			msg1 = "User \"" + userName2 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName2, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName2 + " added to domains.xml", userlogincheck);

			LOGGER.info("Checking if " + userName2
					+ " has run_tracer permissions for domain " + domainName2);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName2,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName2
							+ " is not able to run tracer though is has full permissions",
					tracesExists);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertEMFiles();
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_205021_SuperDomain_domains_xml_matching_only_a_few_of_All_Agents() {

		String nuberOfAgentsExpression = "(.*Number of Agents)";
		try {
			testcaseId = "205021";
			testCaseNameIDPath = "verify_ALM_205021_SuperDomain_domains_xml_matching_only_a_few_of_All_Agents";
			testCaseStart(testCaseNameIDPath);

			userElementName = "agent";
			userAttrName = "mapping";
			userAttrOldValue = "(.*)";
			userAttrNewValue = "(.*)Tomcat(.*)";
			userPassword = "";
			userName = "admin";

			Assert.assertEquals(XMLUtil.changeAttributeValue(emdomainsxmlpath,
					userElementName, userAttrName, userAttrOldValue,
					userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
			startTestBed();
			waitForAgentNodes(tomcatAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			LOGGER.info("Agents are connected.....");

			int count = Integer.parseInt(clw.getLatestMetricValue(userName, "",
					userAttrOldValue, nuberOfAgentsExpression, emHost,
					Integer.parseInt(emPort), emLibDir).split(":::")[1]);

			LOGGER.info("The agent count is..." + count);
			Assert.assertEquals(count, 2);
		} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305610_Assign_a_group_with_one_user_to_a_custom_domain() {

		String customUserPwd = "mypassword";
		String tomcatJbossExp = "(.*Tomcat.*)||(.*JBoss.*)";
		String portMetric = "(.*Port)";

		try {
			testcaseId = "305610";
			testCaseNameIDPath = "verify_ALM_305610_Assign_a_group_with_one_user_to_a_custom_domain";
			testCaseStart(testCaseNameIDPath);

			groupMap.clear();
			domainGroupName1 = "TestGroup";
			permission = "full";
			groupMap.put(domainGroupName1, permission);
			userMap.clear();
			agentMapping = "(.*)";
			domainName = "TestDomain";
			String description = "ForCustomPurpose";
			String userName = "ant";
			/*
			 * userPassword = ""; if
			 * (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
			 * { userPassword = apmbaseutil.encryptPassword(emHome + "/tools",
			 * "SHA2Encoder.bat  " + customUserPwd);
			 * 
			 * } else if (System.getProperty("os.name").toUpperCase()
			 * .contains("LINUX")) { userPassword =
			 * apmbaseutil.encryptPassword(emHome + "/tools",
			 * "./SHA2Encoder.sh   " + customUserPwd);
			 * 
			 * }
			 */

			userPassword = apmbaseutil.encryptPassword(emHome + "/tools",
					customUserPwd);
			LOGGER.info("Creating the.domain........");
			XMLUtil.createDomain(emdomainsxmlpath, domainName, description,
					agentMapping, userMap, groupMap);
			LOGGER.info("Creating the User........");

			XMLUtil.createGroupAddSingleUserInUsersXML(emusersxmlpath,
					description, domainGroupName1, userName);
			XMLUtil.createUserInUsersXML(emusersxmlpath, userName, userPassword);
			startTestBed();
			Assert.assertEquals(
					4,
					clw.getHistoricMetricValuesForTimeInMinutes(userName,
							customUserPwd, tomcatJbossExp, portMetric, emHost,
							Integer.parseInt(emPort), emLibDir, 1).size());

		} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_205024_Assign_a_user_with_multiple_permissions_for_SuperDomain() {

		String shutOffMsg = "MetricName=\"Port\" Shutoff=\"true\"";
		String metricshutoffconfxmlpath = emConfigDir
				+ "/shutoff/MetricShutoffConfiguration.xml";
		try {
			testcaseId = "205024";
			testCaseNameIDPath = "205024_Assign_a_user_with_multiple_permissions_for_SuperDomain";
			testCaseStart(testCaseNameIDPath);

			userName = "Bob";
			permission = "read";
			userPassword = "";
			metricExpression = "\"SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Port\"";

			XMLUtil.createUserInUsersXML(emusersxmlpath, userName, userPassword);
			XMLUtil.createUserGrantElement(emdomainsxmlpath, userName,
					permission);
			permission = "historical_agent_control";
			XMLUtil.createUserGrantElement(emdomainsxmlpath, userName,
					permission);
			permission = "full";
			XMLUtil.createUserGrantElement(emdomainsxmlpath, userName,
					permission);
			XMLUtil.addUserToAdminGroup(emusersxmlpath, userName);

			LOGGER.info("Added 3 permissions to the user....");
			startTestBed();

			LOGGER.info("The output of the clw command is...."
					+ clw.turnOffMetrics(userName, userPassword,
							metricExpression, emHost, Integer.parseInt(emPort),
							emLibDir));
			LOGGER.info("Checking if the user " + userName
					+ " has permissions to shutoff the metrics");

			LOGGER.info("Metric turn is over lets check the confirmation.....");
			checkLogForMsg(envProperties, EM_MACHINE_ID,
					metricshutoffconfxmlpath, shutOffMsg);
			LOGGER.info("Test Passed......");
		}

		finally {
			try {
				copyFile(metricshutoffconfxmlpath, metricshutoffconfxmlpath
						+ testcaseId, EM_MACHINE_ID);
			} catch (Exception e) {
				Assert.assertTrue(false);
			}
			clw.turnOnMetrics(userName, userPassword, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
			stopTestBed();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);

		}
	}

	@Test(groups = { "Smoke" }, enabled = true)
	public void verify_ALM_452552_351210_User_with_no_access_to_any_domain_can_query_the_agents_using_CLW() {

		try {
			testcaseId = "452552";
			testCaseNameIDPath = "verify_ALM_452552_351210_User_with_no_access_to_any_domain_can_query_the_agents_using_CLW";
			testCaseStart(testCaseNameIDPath);

			userName = "TestUser";
			userPassword = "";

			metricExpression = "SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Port";

			XMLUtil.createUserInUsersXML(emusersxmlpath, userName, userPassword);

			startTestBed();
			Assert.assertEquals("-1", clw.getLatestMetricValue(userName,
					userPassword, tomcatAgentExp, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir));
			LOGGER.info("The result is -1 means user dont have permissions, test is passed.....");
		}

		finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}

	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_204944_Changes_To_Users_Xml_File_Without_Restarting_EM_Delete_User_From_Domain() {

		try {
			testcaseId = "204944";
			testCaseNameIDPath = testcaseId
					+ "_Changes_To_Users_Xml_File_Without_Restarting_EM_Delete_User_From_Domain";
			userName = "Guest";
			userPassword = "Guest";
			testCaseStart(testCaseNameIDPath);

			startEM();

			Assert.assertEquals(XMLUtil.deleteUser(emusersxmlpath, userName),
					XMLUtil.SUCCESS_MESSAGE);
			LOGGER.info("User deletion successful user name :::" + userName);
			harvestWait(120); // wait for 2 min to reflect the changes made
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName2, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertFalse("Login LogIn was successful for the user "
					+ userName
					+ " added to domains.xml. Expected it should not be",
					userlogincheck);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305596_Realms_New_Realm() {

		try {
			testcaseId = "305596";
			testCaseNameIDPath = testcaseId + "_Realms_New_Realm";

			testCaseStart(testCaseNameIDPath);
			userName = "Bob";
			String groupName = "Illuminati";
			String permission = "full";
			userPassword = "";
			String emtestusersxmlpath = emConfigDir + "/testusers.xml";

			String replaceStr = "<realm descriptor=\"Local Users and Groups Realm\" id=\"Test Realm\" active=\"true\">  <property name=\"usersFile\">   <value>testusers.xml</value>   </property>  </realm> </realms>";

			replaceEMrelamXml("</realms>", replaceStr);

			LOGGER.info("User modification successful for relam xml file :::");

			backupFile(emusersxmlpath, emtestusersxmlpath, emMachineId);

			LOGGER.info("testusers.xml successful for created in config  :::");
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "cemadmin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "SaasAdmin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "Guest"),
					XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM Configuration Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM System Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteGroup(emtestusersxmlpath, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM Tenant Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteGroup(emtestusersxmlpath, "CEM Analyst"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM Incident Analyst"), XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("testusers.xml user and group deleted :::");

			Assert.assertEquals(
					XMLUtil.addUser(emtestusersxmlpath, userName, userPassword),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.addGroup(emtestusersxmlpath,
					"Global Conspiracy", groupName, userName),
					XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("testusers.xml user and group added :::");

			Assert.assertEquals(XMLUtil.grantPermissionGroupDomainXml(
					emdomainsxmlpath, groupName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.grantPermissionGroupServerXml(
					emserverxmlpath, groupName, permission),
					XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("domain.xml  and server.xml group permission added :::");

			startEM();

			LOGGER.info("user login validation start :::");

			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to testusers.xml", userlogincheck);
			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305597_Realms_Disabled_Realm() {

		try {
			testcaseId = "305597";
			testCaseNameIDPath = testcaseId + "_Realms_Disabled_Realm";

			testCaseStart(testCaseNameIDPath);
			userName = "Bob";
			String groupName = "Illuminati";
			String permission = "full";
			userPassword = "";
			String emtestusersxmlpath = emConfigDir + "/testusers.xml";

			String replaceStr = "<realm descriptor=\"Local Users and Groups Realm\" id=\"Test Realm\" active=\"false\">  <property name=\"usersFile\">   <value>testusers.xml</value>   </property>  </realm> </realms>";

			replaceEMrelamXml("</realms>", replaceStr);

			LOGGER.info("User modification successful for relam xml file :::");

			backupFile(emusersxmlpath, emtestusersxmlpath, emMachineId);

			LOGGER.info("testusers.xml successful for created in config  :::");
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "cemadmin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "SaasAdmin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteUser(emtestusersxmlpath, "Guest"),
					XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM Configuration Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM System Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteGroup(emtestusersxmlpath, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM Tenant Administrator"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(
					XMLUtil.deleteGroup(emtestusersxmlpath, "CEM Analyst"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteGroup(emtestusersxmlpath,
					"CEM Incident Analyst"), XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("testusers.xml user and group deleted :::");

			Assert.assertEquals(
					XMLUtil.addUser(emtestusersxmlpath, userName, userPassword),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.addGroup(emtestusersxmlpath,
					"Global Conspiracy", groupName, userName),
					XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("testusers.xml user and group added :::");

			Assert.assertEquals(XMLUtil.grantPermissionGroupDomainXml(
					emdomainsxmlpath, groupName, permission),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.grantPermissionGroupServerXml(
					emserverxmlpath, groupName, permission),
					XMLUtil.SUCCESS_MESSAGE);

			LOGGER.info("domain.xml  and server.xml group permission added :::");

			startEM();

			LOGGER.info("user login validation start :::");

			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertFalse(
					"Login was successful for the user "
							+ userName
							+ " added to testusers.xml. but expected should not sucessful",
					userlogincheck);
			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305598_Realms_Simple_Admin_Realm() {

		try {
			testcaseId = "305598";
			testCaseNameIDPath = testcaseId + "_Realms_Simple_Admin_Realm";
			String permission = "full";
			testCaseStart(testCaseNameIDPath);

			userAttrOldValue = "usersFile";
			userAttrNewValue = "userid";
			Assert.assertEquals(XMLUtil.modifyRelamXmlrelamAttribute(
					emrealmsxmlpath, "descriptor",
					"Local Users and Groups Realm", "Simple Admin Realm"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.modifyRelamXmlrelamAttribute(
					emrealmsxmlpath, "id", "Local Users and Groups",
					"Introscope Admin Realm"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteRelamXmlProperty(emrealmsxmlpath,
					userAttrOldValue), XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.addRelamXmlPropertyAttribute(
					emrealmsxmlpath, userAttrNewValue, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.addRelamXmlPropertyAttribute(
					emrealmsxmlpath, "useGuest", "true"),
					XMLUtil.SUCCESS_MESSAGE);

			userName = "Admin";

			Assert.assertEquals(
					XMLUtil.deleteGroupInDomainXml(emdomainsxmlpath, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.grantPermissionUserDomainXml(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);

			startEM();

			LOGGER.info("user login validation start :::");
			userName = "Admin";
			userPassword = "";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to relams.xml", userlogincheck);

			userName = "Guest";
			userPassword = "Guest";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to relams.xml", userlogincheck);

			userName = "Test";
			userPassword = "";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertFalse(
					"Login was  successful for the user "
							+ userName
							+ " added to relams.xml . but expected should not sucessful",
					userlogincheck);

			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305599_Realms_Simple_Admin_Realm_Without_Guest() {

		try {
			testcaseId = "305599";
			testCaseNameIDPath = testcaseId
					+ "_Realms_Simple_Admin_Realm_Without_Guest";
			String permission = "full";
			testCaseStart(testCaseNameIDPath);

			userAttrOldValue = "usersFile";
			userAttrNewValue = "userid";
			Assert.assertEquals(XMLUtil.modifyRelamXmlrelamAttribute(
					emrealmsxmlpath, "descriptor",
					"Local Users and Groups Realm", "Simple Admin Realm"),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.modifyRelamXmlrelamAttribute(
					emrealmsxmlpath, "id", "Local Users and Groups",
					"Introscope Admin Realm"), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteRelamXmlProperty(emrealmsxmlpath,
					userAttrOldValue), XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.addRelamXmlPropertyAttribute(
					emrealmsxmlpath, userAttrNewValue, "Admin"),
					XMLUtil.SUCCESS_MESSAGE);

			Assert.assertEquals(XMLUtil.addRelamXmlPropertyAttribute(
					emrealmsxmlpath, "useGuest", "false"),
					XMLUtil.SUCCESS_MESSAGE);

			userName = "Admin";

			Assert.assertEquals(
					XMLUtil.deleteGroupInDomainXml(emdomainsxmlpath, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.grantPermissionUserDomainXml(
					emdomainsxmlpath, userName, permission),
					XMLUtil.SUCCESS_MESSAGE);

			startEM();

			LOGGER.info("user login validation start :::");
			userName = "Admin";
			userPassword = "";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to relams.xml", userlogincheck);

			userName = "Guest";
			userPassword = "Guest";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(userName));
			Assert.assertFalse(
					"Login was  successful for the user "
							+ userName
							+ " added to relams.xml . but expected should not sucessful",
					userlogincheck);

			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_298793_TT_43254_Create_Domain_Check_For_EM_Port_Metric() {

		try {
			testcaseId = "298793";
			testCaseNameIDPath = testcaseId
					+ "_Create_Domain_Check_For_EM_Port_Metric";
			testCaseStart(testCaseNameIDPath);
			String metricName = "(.*EM Port.*)";

			domainName = "TomcatDomain";
			agentMapping = "(.*Tomcat.*)";
			domainUserName = "Admin";
			String newdomainUserName = "Rohit";
			userMap.clear();
			userMap.put(domainUserName, readPermission);
			userMap.put(newdomainUserName, fullPermission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "Tomcat Domain",
					agentMapping, userMap, null);

			domainName = "JbossDomain";
			agentMapping = "(.*JBoss.*)";
			userMap.clear();
			userMap.put(domainUserName, fullPermission);
			userMap.put(newdomainUserName, readPermission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "Jboss Domain",
					agentMapping, userMap, null);

			XMLUtil.addUser(emusersxmlpath, newdomainUserName, "");
			XMLUtil.addGroup(emusersxmlpath, "Readers Group", "MyReaders",
					newdomainUserName);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			waitForAgentNodes(jbossAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			LOGGER.info("Agents are connected.....");

			Assert.assertTrue(validateMetricValue(clw
					.getMetricValueForTimeInMinutes(domainUserName, "",
							tomcatAgentExpPath, metricName, emHost,
							Integer.parseInt(emPort), emLibDir, 1), emPort));
			Assert.assertTrue(validateMetricValue(clw
					.getMetricValueForTimeInMinutes(newdomainUserName, "",
							jbossAgentExpPath, metricName, emHost,
							Integer.parseInt(emPort), emLibDir, 1), emPort));

			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_439515_Exclusivity_Between_Different_Domains() {

		try {
			testcaseId = "439515";
			testCaseNameIDPath = testcaseId
					+ "_Exclusivity_Between_Different_Domains";
			testCaseStart(testCaseNameIDPath);

			String metricName = "(.*EM Port.*)";

			domainName = "TomcatDomain";
			agentMapping = "(.*Tomcat.*)";
			String testUserName = "test1";
			String test2UserName = "test2";
			userMap.clear();
			userMap.put(testUserName, readPermission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "Tomcat Domain",
					agentMapping, userMap, null);

			domainName = "JbossDomain";
			agentMapping = "(.*JBoss.*)";
			userMap.clear();
			userMap.put(test2UserName, readPermission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "Jboss Domain",
					agentMapping, userMap, null);

			XMLUtil.addUser(emusersxmlpath, testUserName, "");
			XMLUtil.addUser(emusersxmlpath, test2UserName, "");

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			waitForAgentNodes(jbossAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			LOGGER.info("Agents are connected.....");

			Assert.assertTrue(validateMetricValue(clw
					.getMetricValueForTimeInMinutes(testUserName, "",
							tomcatAgentExpPath, metricName, emHost,
							Integer.parseInt(emPort), emLibDir, 1), "Tomcat"));

			Assert.assertFalse(validateMetricValue(clw
					.getMetricValueForTimeInMinutes(test2UserName, "",
							tomcatAgentExpPath, metricName, emHost,
							Integer.parseInt(emPort), emLibDir, 1), "Tomcat"));

			Assert.assertTrue(validateMetricValue(clw
					.getMetricValueForTimeInMinutes(test2UserName, "",
							jbossAgentExpPath, metricName, emHost,
							Integer.parseInt(emPort), emLibDir, 1), "JBoss"));

			Assert.assertFalse(validateMetricValue(clw
					.getMetricValueForTimeInMinutes(testUserName, "",
							jbossAgentExpPath, metricName, emHost,
							Integer.parseInt(emPort), emLibDir, 1), "JBoss"));

			LOGGER.info("user login validation End :::");

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles();
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_439521_domainsxml_having_custom_domains_with_same_name() {

		try {
			testcaseId = "439521";
			testCaseNameIDPath = "verify_ALM_439521_domainsxml_having_custom_domains_with_same_name";
			testCaseStart(testCaseNameIDPath);

			domainName = "QADomain";
			agentMapping = "(.*)";
			domainUserName = "qauser1";
			permission = "full";
			userMap.clear();
			userMap.put(domainUserName, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					domainUserName, ""), XMLUtil.SUCCESS_MESSAGE);

			replaceEMProperty("log4j.logger.Manager=INFO, console, logfile",
					"log4j.logger.Manager=DEBUG, console, logfile");
			startEM();
			checkEMLogForMsg("Could not create domain SuperDomain: {1}");

		} finally {
			stopEM();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_439526_Domain_names_are_case_sensitive() {

		String agentExpPath = "(.*SuperDomain.*)|(.*)|Tomcat|(.*Tomcat.*)";
		String metricName = "(.*EM Port.*)";
		boolean QADomainResult = false;
		boolean qadomainresult = false;

		try {
			testcaseId = "439526";
			testCaseNameIDPath = "verify_ALM_439526_Domain_names_are_case_sensitive";
			testCaseStart(testCaseNameIDPath);

			domainName = "QADomain";
			agentMapping = "(.*)";
			domainUserName = "admin";
			permission = "full";
			userMap.clear();
			userMap.put(domainUserName, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);

			domainName = "qadomain";
			agentMapping = "(.*Tomcat.*)";
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);

			startTestBed();
			waitForAgentNodes(tomcatAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			LOGGER.info("Agents are connected.....");

			List<String> Result = new ArrayList<String>();
			Result = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpPath, metricName, emHost, Integer.parseInt(emPort),
					emLibDir, 1);

			LOGGER.info("The size for getMetricValueForTimeInMinutes is..."
					+ Result);

			for (int i = 0; i < Result.size(); i++) {
				LOGGER.info("Value is..." + Result.get(i));
				if (Result.get(i).contains("QADomain")) {
					LOGGER.info("FirstResult" + i);
					QADomainResult = true;
				}
				if (Result.get(i).contains("qadomain")) {
					LOGGER.info("SecondResult" + i);
					qadomainresult = true;
				}
			}

			LOGGER.info("The result for QADomain and qadomain is "
					+ QADomainResult + qadomainresult);
			Assert.assertTrue(QADomainResult);
			Assert.assertFalse(qadomainresult);
		} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305593_Non_Admin_user_with_domains_xml_matching_Agents() {

		String agentExpPathTomcat = "\"(.*SuperDomain.*)|(.*)|Tomcat|(.*Tomcat.*)\"";
		String agentExpPathJBoss = "\"(.*SuperDomain.*)|(.*)|JBoss|(.*JBoss.*)\"";
		String tomatAgentString = "Tomcat";
		String jbossAgentString = "JBoss";
		try {
			testcaseId = "305593";
			testCaseNameIDPath = "verify_ALM_305593_Non_Admin_user_with_domains_xml_matching_Agents";
			testCaseStart(testCaseNameIDPath);

			domainName = "QADomain";
			agentMapping = "(.*Tomcat.*)";
			domainUserName = "testUser"; // This needs to be addressed once the
											// issue is fixed,
											// right now listing the agents
			permission = "full";
			userMap.clear();
			userMap.put(domainUserName, permission);

			startTestBed();
			addCustomDomainWithUser(domainName, agentMapping, userMap);
			waitForAgentNodes(tomcatAgentExp, emHost, Integer.parseInt(emPort),
					emLibDir);
			LOGGER.info("Agents are present, now start the testing....");
			Assert.assertTrue(clw.isAgentPresent(domainUserName, "",
					agentExpPathTomcat, emHost, Integer.parseInt(emPort),
					emLibDir, tomatAgentString));
			Assert.assertFalse(clw.isAgentPresent(domainUserName, "",
					agentExpPathJBoss, emHost, Integer.parseInt(emPort),
					emLibDir, jbossAgentString));
			revertEMFiles(testcaseId + "_PART1");
			stopTestBed();
			LOGGER.info("PART 1 PASSED");

			agentMapping = "(.*WebLogic.*)";
			addCustomDomainWithUser(domainName, agentMapping, userMap);
			startTestBed();
			Assert.assertFalse(clw.isAgentPresent(domainUserName, "",
					agentExpPathTomcat, emHost, Integer.parseInt(emPort),
					emLibDir, tomatAgentString));
			Assert.assertFalse(clw.isAgentPresent(domainUserName, "",
					agentExpPathJBoss, emHost, Integer.parseInt(emPort),
					emLibDir, jbossAgentString));
			revertEMFiles(testcaseId + "_PART2");
			stopTestBed();
			LOGGER.info("PART 2 PASSED");

			agentMapping = "(.*)";
			addCustomDomainWithUser(domainName, agentMapping, userMap);
			startTestBed();
			Assert.assertTrue(clw.isAgentPresent(domainUserName, "",
					agentExpPathTomcat, emHost, Integer.parseInt(emPort),
					emLibDir, tomatAgentString));
			Assert.assertTrue(clw.isAgentPresent(domainUserName, "",
					agentExpPathJBoss, emHost, Integer.parseInt(emPort),
					emLibDir, jbossAgentString));
			LOGGER.info("PART 3 PASSED");
		} finally {
			revertEMFiles(testcaseId + "_PART3");
			stopTestBed();
			testCaseEnd(testcaseId);

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_305594_Encryption_tool_for_User_Passwords() {

		try {
			testcaseId = "305594";
			testCaseNameIDPath = "verify_ALM_305594_Encryption_tool_for_User_Passwords";
			testCaseStart(testCaseNameIDPath);
			/*
			 * if
			 * (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
			 * { userPassword = apmbaseutil.encryptPassword(emHome + "/tools",
			 * "SHA2Encoder.bat  " + guestPassword, guestPassword);
			 * 
			 * } else if (System.getProperty("os.name").toUpperCase()
			 * .contains("LINUX")) { userPassword =
			 * apmbaseutil.encryptPassword(emHome + "/tools",
			 * "./SHA2Encoder.sh   " + guestPassword, guestPassword);
			 * 
			 * }
			 */

			userPassword = apmbaseutil.encryptPassword(emHome + "/tools",
					guestPassword);
			XMLUtil.changeAttributeValueforUser(emConfigDir + "/users.xml",
					userliteral, nametag, guestUser, pwdliteral, guestPassword,
					userPassword);
			startEM();
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, guestUser, guestPassword, clwJarFileLoc,
					emLogDir, userLogInMsg(guestUser));
			Assert.assertTrue("Login was not successful for the user "
					+ guestUser + " added to users.xml", userlogincheck);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_305619_All_agents_visible_under_SuperDomain() {

		try {
			testcaseId = "305619";
			testCaseNameIDPath = "verify_ALM_305619_All_agents_visible_under_SuperDomain";
			testCaseStart(testCaseNameIDPath);
			userMap.clear();
			domainName = "One";
			agentMapping = "(.*)Tomcat(.*)";
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, null, null);
			Assert.assertEquals(XMLUtil.changeAttributeValueWithparentNode(
					emdomainsxmlpath, agenttag, superdomainliteral, mappingtag,
					"(.*)", "(.*)JBoss(.*)"), XMLUtil.SUCCESS_MESSAGE);
			startTestBed();
			agentExpression = "/*SuperDomain/*/|(.*)";
			metricExpression = "EM Host";

			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain/" + domainName + ","
							+ tomcatHost + ",Tomcat"));
			Assert.assertTrue(
					"JBoss Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain," + jbossHost + ",JBoss"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_280488_Agent_mapped_to_custom_domains() {

		try {
			testcaseId = "280488";
			testCaseNameIDPath = "verify_ALM_280488_Agent_mapped_to_custom_domains";
			testCaseStart(testCaseNameIDPath);
			domainGroupName = "Admin";
			permission = "Full";
			domainName = "Test";
			agentMapping = "(.*)Tomcat(.*)";
			groupMap.clear();
			groupMap.put(domainGroupName, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "TestDomain",
					agentMapping, null, groupMap);
			startTestBed();
			agentExpression = "/*SuperDomain/*/|(.*)";
			metricExpression = "EM Host";

			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain/" + domainName + ","
							+ tomcatHost + ",Tomcat,Tomcat Agent"));
			Assert.assertTrue(
					"JBoss Agent Metrics exist under Superdomain though agentmapping is set for Tomcat",
					!tempResult1.contains("SuperDomain/" + domainName + ","
							+ jbossHost + ",JBoss,JBoss Agent"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			stopTomcatAgent();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_305617_Assign_a_user_to_multiple_groups() {

		try {
			testcaseId = "305617";
			testCaseNameIDPath = "verify_ALM_305617_Assign_a_user_to_multiple_groups";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			agentMapping = "(.*)";
			domainGroupName = "One";
			domainGroupName1 = "Two";
			domainGroupName2 = "Three";
			permission = "read";
			permission1 = "write";
			permission2 = "full";
			userName = "Bob";
			userPassword = "";

			groupMap.clear();
			groupMap.put(domainGroupName, permission);
			groupMap.put(domainGroupName1, permission1);
			groupMap.put(domainGroupName2, permission2);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);

			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName1, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName2, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);

			startEM();
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName
							+ " is not able to run tracer though is has full permissions as Admin",
					tracesExists);

			startTomcatAgent();
			harvestWait(60);
			agentExpression = "(.*)Tomcat(.*)";
			metricExpression = "(.*)GC Heap(.*)";
			tempResult1 = clw.getLatestMetricValue(userName, userPassword,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
			Assert.assertTrue(
					"Tomcat Agent is not reporting metrics for the user "
							+ userName, !tempResult1.equals("-1"));
			metricExpression = "(.*)Tomcat Agent";
			clw.turnOffAgents(userName, userPassword, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
			msg1 = "Process=\"Tomcat\" Shutoff=\"true\"";
			isKeywordInFile(envProperties, emMachineId,
					metricshutoffconfxmlpath, msg1);
			clw.turnOnAgents(userName, userPassword, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			stopTomcatAgent();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_439522_shut_down_EM_with_custom_user() {
		try {
			testcaseId = "439522";
			testCaseNameIDPath = "verify_ALM_439522_shut_down_EM_with_custom_user";
			testCaseStart(testCaseNameIDPath);
			domainName = "QADomain" + testcaseId;
			agentMapping = "(.*)";
			domainUserName = "testUser";
			userMap.clear();
			userMap.put(domainUserName, fullPermission);
			addCustomDomainWithUser(domainName, agentMapping, userMap);
			XMLUtil.createUserGrantElementInServerXml(emserverxmlpath,
					domainUserName, fullPermission);

			if (Util.fileExist(emLogFile))
				deleteFile(emLogFile, EM_MACHINE_ID);

			startEM();
			Assert.assertTrue(clw.shutDownLocalEM(domainUserName, "", emHost,
					emPort, emLibDir));
			checkEMLogForMsg("Orderly shutdown complete");
			LOGGER.info("Test Success....");
		} finally {
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testcaseId);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_450915_Empty_domains_xml_CLW() {
		try {
			testcaseId = "450915";
			testCaseNameIDPath = "verify_ALM_450915_Empty_domains_xml_CLW";
			testCaseStart(testCaseNameIDPath);
			userName = "Admin";
			replaceProp("<SuperDomain>", "", emMachineId, emdomainsxmlpath);
			replaceProp("</SuperDomain>", "", emMachineId, emdomainsxmlpath);
			replaceProp("<agent mapping=\"(.*)\"/>", "", emMachineId,
					emdomainsxmlpath);
			replaceProp("<grant group=\"Admin\" permission=\"full\"/>", "",
					emMachineId, emdomainsxmlpath);
			replaceProp("<grant permission=\"read\" user=\"Guest\"/>", "",
					emMachineId, emdomainsxmlpath);
			replaceProp("<grant user=\"Guest\" permission=\"read\"/>", "",
					emMachineId, emdomainsxmlpath);

			startEM();
			LOGGER.info("Testbed startup is done");
			Assert.assertEquals("-1", clw.getLatestMetricValue(userName, "",
					".*", ".*", emHost, Integer.parseInt(emPort), emLibDir));
			LOGGER.info("Test Passed.....");
		} finally {
			stopTestBed();
			renameEMLogFile(testcaseId);
			revertEMFiles(testcaseId);
			testCaseEnd(testcaseId);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305606_Assign_live_agent_control_permission_when_creating_new_domain() {
		try {
			testcaseId = "305606";
			testCaseNameIDPath = "verify_ALM_305606_Assign_live_agent_control_permission_when_creating_new_domain";
			testCaseStart(testCaseNameIDPath);

			domainName = "LiveAgentControlDomain";
			agentMapping = "(.*Tomcat.*)";
			domainUserName = "TestUser";
			userPassword = "mypassword";

			userMap.clear();
			userMap.put(domainUserName, fullPermission);
			permission = "live_agent_control";
			agentExpression = "(.*SuperDomain/" + domainName + ".*)|(.*)";
			metricExpression = "(.*EM Port.*)";

			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			XMLUtil.createUserInUsersXML(emusersxmlpath, domainUserName,
					apmbaseutil
							.encryptPassword(emHome + "/tools", userPassword));
			XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
					domainName, domainUserName, permission);
			permission = "read";
			XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
					domainName, domainUserName, permission);
			startTestBed();

			listoutput.clear();
			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName,
					userPassword, agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertTrue(validateMetricValue(listoutput, domainName,
					"Tomcat Agent"));
			Assert.assertFalse(validateMetricValue(listoutput, domainName,
					"JBoss Agent"));
			LOGGER.info("All AGents are listed under custom domain...");

			Assert.assertTrue(tomcatAgentTurnOff(domainUserName, userPassword,
					true));
			LOGGER.info("AgentTurnoff Sucess...");
			Assert.assertTrue(tomcatAgentTurnOff(domainUserName, userPassword,
					false));
		} finally {
			stopTestBed();
			renameEMLogFile(testcaseId);
			revertEMFiles(testcaseId);
			testCaseEnd(testcaseId);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_305601_Assign_multiple_permissions_to_a_single_user_for_multiple_domains() {

		String dashboardName = "Welcome to APM Dashboards";
		String managementModuleName = "Default";
		String newDashboardName = "Automation_Dashboard";
		String tomcatDomainName = "TomcatDomain" + testcaseId;
		String jBossDomainName = "JBossDomain" + testcaseId;
		String allAgentsDomainName = "AllAgentsDomain" + testcaseId;
		String metricName = "(.*EM Port.*)";

		try {

			testcaseId = "305601";
			testCaseNameIDPath = "verify_ALM_305601_Assign_multiple_permissions_to_a_single_user_for_multiple_domains";
			testCaseStart(testCaseNameIDPath);
			domainName = "SuperDomain";
			domainUserName = "TestUser_PART1";
			permission = "read";

			XMLUtil.createUserInUsersXML(emusersxmlpath, domainUserName, "");
			XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
					domainName, domainUserName, permission);
			permission = "historical_agent_control";
			XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
					domainName, domainUserName, permission);
			permission = "full";
			XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
					domainName, domainUserName, permission);

			startTestBed();

			Assert.assertTrue(tomcatAgentTurnOff(domainUserName, "", true));
			LOGGER.info("AgentTurnoff Sucess...");
			Assert.assertTrue(tomcatAgentTurnOff(domainUserName, "", false));

			Assert.assertTrue(clw.renameDashboard(domainUserName, "", emHost,
					emPort, emLibDir, dashboardName, managementModuleName,
					newDashboardName));
			LOGGER.info("renaming dashboard is success");
			Assert.assertTrue(clw.renameDashboard(domainUserName, "", emHost,
					emPort, emLibDir, newDashboardName, managementModuleName,
					dashboardName));
			stopTestBed();
			renameEMLogFile(testcaseId + "PART1");
			revertEMFiles(testcaseId + "PART1");
			LOGGER.info("Test Case PART1 is completed...");

			agentMapping = "(.*Tomcat.*)";
			domainUserName = "TestUser";
			userPassword = "";
			userMap.clear();
			userMap.put(domainUserName, fullPermission);
			addCustomDomainWithUser(tomcatDomainName, agentMapping, userMap);

			agentMapping = "(.*JBoss.*)";
			addCustomDomainWithUser(jBossDomainName, agentMapping, userMap);

			agentMapping = "(.*)";
			addCustomDomainWithUser(allAgentsDomainName, agentMapping, userMap);

			startTestBed();
			agentExpression = "(.*SuperDomain/" + tomcatDomainName + ".*)|(.*)";

			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpression, metricName, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertTrue(validateMetricValue(listoutput, tomcatDomainName,
					"Tomcat Agent"));
			Assert.assertFalse(validateMetricValue(listoutput,
					tomcatDomainName, "JBoss Agent"));
			LOGGER.info("First Scenario Passed....");

			agentExpression = "(.*SuperDomain/" + jBossDomainName + ".*)|(.*)";
			listoutput.clear();
			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpression, metricName, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			LOGGER.info("Pranasd" + listoutput.toString());
			Assert.assertFalse(validateMetricValue(listoutput, jBossDomainName,
					"Tomcat Agent"));
			Assert.assertTrue(validateMetricValue(listoutput, jBossDomainName,
					"JBoss Agent"));
			LOGGER.info("Second Scenario Passed....");

			agentExpression = "(.*SuperDomain/" + allAgentsDomainName
					+ ".*)|(.*)";
			listoutput.clear();
			listoutput = clw.getMetricValueForTimeInMinutes(domainUserName, "",
					agentExpression, metricName, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertFalse(validateMetricValue(listoutput,
					allAgentsDomainName, "Tomcat Agent"));
			Assert.assertFalse(validateMetricValue(listoutput,
					allAgentsDomainName, "JBoss Agent"));
			LOGGER.info("Third Scenario and PART2 Passed....");
		} finally {
			stopTestBed();
			renameEMLogFile(testcaseId);
			revertEMFiles(testcaseId);
			testCaseEnd(testcaseId);
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_305625_Domain_xml_should_be_hot_configurable() {
		try {
			testcaseId = "305625";
			testCaseNameIDPath = "verify_ALM_305625_Domain_xml_should_be_hot_configurable";
			testCaseStart(testCaseNameIDPath);
			permission = "full";
			Assert.assertEquals(XMLUtil.deleteElement(emdomainsxmlpath,
					granttag, grouptag, user), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.deleteElement(emserverxmlpath,
					granttag, grouptag, user), XMLUtil.SUCCESS_MESSAGE);
			startEM();

			agentExpression = "(.*)";
			metricExpression = "(.*)GC Heap(.*)";

			listoutput = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertFalse(
					"Login was successful for the user "
							+ user
							+ " which was deleted from domains.xml which is not expected",
					listoutput.size() >= 3);

			revertFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
			revertFile(emserverxmlpath, emserverxmlpath_backup, emMachineId);
			msg1 = "Permissions file changed";
			checkEMLogForMsg(msg1);

			listoutput = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertTrue(
					"Login was not successful for the user "
							+ user
							+ " which was add back to domains.xml with hotconfig change",
					listoutput.size() >= 3);

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_205022_Assign_multiple_users_to_a_single_domain() {
		try {
			testcaseId = "205022";
			testCaseNameIDPath = "verify_ALM_205022_Assign_multiple_users_to_a_single_domain";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			userName1 = "qauser1";
			userName2 = "qauser2";
			userPassword = "";
			permission = "read";
			agentMapping = "(.*)";
			userMap.clear();
			userMap.put(userName1, permission);
			userMap.put(userName2, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			startEM();
			msg1 = "User \"" + userName1 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName1, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName1 + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName1,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "User " + userName1
							+ " does not have any permissions for Activity");
			Assert.assertTrue(
					userName1
							+ " is able to run tracer though is has got only read permission failed",
					tracesExists);
			msg1 = "User \"" + userName2 + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName2, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName2 + " added to domains.xml", userlogincheck);
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName2,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "User " + userName2
							+ " does not have any permissions for Activity");
			Assert.assertTrue(
					userName2
							+ " is able to run tracer though is has got only read permission failed",
					tracesExists);
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			revertEMFiles(testcaseId);
			renameEMLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Full" }, enabled = true)
	public void verify_ALM_205025_Assign_run_tracer_permission_when_creating_new_domain() {
		try {
			testcaseId = "205025";
			testCaseNameIDPath = "verify_ALM_205025_Assign_run_tracer_permission_when_creating_new_domain";
			testCaseStart(testCaseNameIDPath);
			domainName = "Test";
			userName = "test";
			userPassword = "test";
			String encrypteduserPassword = apmbaseutil.encryptPassword(emHome
					+ "/tools", userPassword);
			permission1 = "read";
			permission2 = "run_tracer";
			agentMapping = "(.*)";
			userMap.clear();
			userMap.put(userName, permission1);

			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
					domaintag, userName, permission2);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, encrypteduserPassword), XMLUtil.SUCCESS_MESSAGE);

			startEM();

			LOGGER.info("Checking if user " + userName + " has read permission");
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			Assert.assertTrue("Login was not successful for the user "
					+ userName + " added to domains.xml", userlogincheck);

			LOGGER.info("Checking if user " + userName
					+ " has run_tracer permission");
			tracesExists = ApmbaseUtil.checkInvestigatorTree(userName,
					userPassword, "(.*)", emHost, Integer.parseInt(emPort),
					emLibDir, "No transaction traces collected");
			Assert.assertTrue(
					userName
							+ " is not able to run tracer though is has same permissions as Admin",
					tracesExists);

			startAgents();

			LOGGER.info("Checking if user " + userName
					+ " does not have write permission");
			metricExpression = "(.*)Tomcat Agent";
			tempResult1 = clw.turnOffAgents(userName, userPassword,
					metricExpression, emHost, Integer.parseInt(emPort),
					emLibDir).toString();
			msg1 = "Invalid operation: User " + userName
					+ " does not have sufficient permissions in domain "
					+ domainName;
			Assert.assertTrue(
					"user "
							+ userName
							+ " is able to turn off metrics though is has only read and run_tracer permissions for domain "
							+ domainName, tempResult1.contains(msg1));

			LOGGER.info("Checking if all the agents report under domain "
					+ domainName);
			agentExpression = "(.*)";
			metricExpression = "EM Host";
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain/" + domainName + ","
							+ tomcatHost + ",Tomcat"));
			Assert.assertTrue(
					"JBoss Agent Metrics does not exist under Superdomain",
					tempResult1.contains("SuperDomain/" + domainName + ","
							+ jbossHost + ",JBoss"));

		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

	@Test(groups = { "Deep" }, enabled = true)
	public void verify_ALM_439527_host_is_case_sensitive_in_Domin_xml() {
		try {
			testcaseId = "439527";
			testCaseNameIDPath = "verify_ALM_439527_host_is_case_sensitive_in_Domin_xml";
			testCaseStart(testCaseNameIDPath);
			String casesensitivetomcatHost = tomcatHost.replace("tas", "TAS");
			agentMapping = "(.*)" + casesensitivetomcatHost + "(.*)";
			domainName = "Test";
			permission = "read";
			userMap.clear();
			userMap.put(user, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, userMap, null);
			startEM();
			startTomcatAgent();
			agentExpression = "(.*)";
			metricExpression = "EM Host";
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat is reporting metrics even after a casesensitive Host is given for agentMapping",
					!tempResult1.contains("SuperDomain/" + domainName + ","
							+ tomcatHost + ",Tomcat"));
			XMLUtil.changeAttributeValue(emdomainsxmlpath, agenttag,
					mappingtag, agentMapping, "(.*)" + tomcatHost + "(.*)");
			harvestWait(60);
			stopTomcatAgent();
			startTomcatAgent();
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat is not reporting metrics even after correct Host is given for agentMapping",
					tempResult1.contains("SuperDomain/" + domainName + ","
							+ tomcatHost + ",Tomcat"));
		} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
		} finally {
			stopEM();
			stopTomcatAgent();
			revertEMFiles(testcaseId);
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
	}

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_305604_Assign_run_tracer_permission_when_creating_new_domain() {
        String tomcatAgentURL =
            "http://" + tomcatHost + ":9091/qa-app/transactiontraces/RecursiveDuration.jsp";

        try {
            testcaseId = "305604";
            testCaseNameIDPath =
                "verify_ALM_305604_Assign_run_tracer_permission_when_creating_new_domain";
            testCaseStart(testCaseNameIDPath);

            domainName = "run_tracerDomain";
            agentMapping = "(.*)";
            domainUserName = "TestUser"; // need to change it to custom user
            userPassword = "mypassword";
            permission = "run_tracer";

            userMap.clear();
            userMap.put(domainUserName, permission);

            XMLUtil.createDomain(emdomainsxmlpath, domainName, "", agentMapping, userMap, null);
            XMLUtil.createUserInUsersXML(emusersxmlpath, domainUserName,
                apmbaseutil.encryptPassword(emHome + "/tools", userPassword));
            XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath, domainName,
                domainUserName, permission);
            startTestBed();

            Assert.assertTrue(hitURL(tomcatAgentURL, EM_MACHINE_ID));
            Assert.assertTrue(clw.verifyTransactionTrace(domainUserName, userPassword, ".*",
                emHost, Integer.parseInt(emPort), emLibDir));
            LOGGER.info("Test Passed, able to generate TT using custom user....");
        } finally {
            stopTestBed();
            renameEMLogFile(testcaseId);
            revertEMFiles(testcaseId);
            testCaseEnd(testcaseId);
        }
    } 
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_305615_Assign_live_agent_control_permission_to_a_group_with_a_single_user() {
    	
    	try {
            testcaseId = "305615";
            testCaseNameIDPath =
                "verify_ALM_305615_Assign_live_agent_control_permission_to_a_group_with_a_single_user";
            testCaseStart(testCaseNameIDPath);
            domainName = "Test";
			domainGroupName = "Test";
			permission1 = "live_agent_control";			
			permission2 = "read";			
			agentMapping = "(.*)";
			userName = "one";
			userPassword = "";
			agentExpression = "(.*SuperDomain/" + domainName + ".*)|(.*)";
			metricExpression = "(.*EM Port.*)";
			groupMap.clear();
			groupMap.put(domainGroupName, permission1);
			
			XMLUtil.createDomain(emdomainsxmlpath, domainName, "",
					agentMapping, null, groupMap);
			XMLUtil.createGroupGrantForElement(emdomainsxmlpath,
					domaintag, domainGroupName, permission2);
			
			Assert.assertEquals(XMLUtil.createGroupAddMultipleUsersInUsersXML(
					emusersxmlpath, "", domainGroupName, userName),
					XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName, userPassword), XMLUtil.SUCCESS_MESSAGE);

			startTestBed();			
			msg1 = "User \"" + userName + "\" connected successfully";
			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
					emHost, emPort, userName, userPassword, clwJarFileLoc,
					emLogDir, msg1);
			
			msg1 = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to " + emHost;
			checkLogForMsg(envProperties, tomcatMachineId, tomcatAgentLogFile, msg1);
			checkLogForMsg(envProperties, jbossMachineId, jbossAgentLogFile, msg1);			
			listoutput.clear();
			listoutput = clw.getMetricValueForTimeInMinutes(userName,
					userPassword, agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1);
			Assert.assertTrue(validateMetricValue(listoutput, domainName,
					"Tomcat Agent"));
			Assert.assertTrue(validateMetricValue(listoutput, domainName,
					"JBoss Agent"));
			LOGGER.info("All AGents are listed under custom domain and reporting data...");

			Assert.assertTrue(tomcatAgentTurnOff(userName, userPassword,
					true));
			LOGGER.info("Turned off the Tomcat Agent");			
			Assert.assertTrue(tomcatAgentTurnOff(userName, userPassword,
					false));
			LOGGER.info("Turned on the Tomcat Agent");
    	} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
    	} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
    }
    
    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_324203_Agent_Mapping_when_qualified_for_multiple_domains() {
    	
    	try {
            testcaseId = "324203";
            testCaseNameIDPath =
                "verify_ALM_324203_Agent_Mapping_when_qualified_for_multiple_domains";
            testCaseStart(testCaseNameIDPath);
            userName1 = "user1"; 
            userName2 = "user2";
			userPassword = "";			
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			domainName1 = "domain1";			
			domainName2 = "domain2";
			agentMapping = "(.*)";
			permission = "full";
			userMap.clear();
			userMap.put(userName1, permission);
			XMLUtil.createDomainAfterSuperDomain(emdomainsxmlpath, domainName1, "",
					agentMapping, userMap, null);
			userMap.clear();
			userMap.put(userName2, permission);
			XMLUtil.createDomainAfterSuperDomain(emdomainsxmlpath, domainName2, "",
					agentMapping, userMap, null);
			
			startTestBed(); 
			msg1 = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to " + emHost;
			checkLogForMsg(envProperties, tomcatMachineId, tomcatAgentLogFile, msg1);
			checkLogForMsg(envProperties, jbossMachineId, jbossAgentLogFile, msg1);
			agentExpression = "/*SuperDomain/*/|(.*)";
			metricExpression = "EM Host";
			LOGGER.info("Checking for agentmapping when a new domain is created after the SuperDomain");
			LOGGER.info(tomcatHost);
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();			
			Assert.assertTrue(
					"Tomcat Agent Metrics are not reporting under the right domain(SuperDomain)",
					 tempResult1.contains("SuperDomain," + tomcatHost + ",Tomcat") && 
					!tempResult1.contains("SuperDomain/" + domainName1 + "," + tomcatHost + ",Tomcat") && 
					!tempResult1.contains("SuperDomain/" + domainName2 + "," + tomcatHost + ",Tomcat"));
			Assert.assertTrue(
					"JBoss Agent Metrics are not reporting under the right domain(SuperDomain)",
					tempResult1.contains("SuperDomain," + jbossHost + ",JBoss") &&
					!tempResult1.contains("SuperDomain/" + domainName1 + "," + jbossHost + ",JBoss") && 
					!tempResult1.contains("SuperDomain/" + domainName2 + "," + jbossHost + ",JBoss"));
			
			revertFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
			userMap.clear();
			userMap.put(userName1, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName1, "",
					agentMapping, userMap, null);
			userMap.clear();
			userMap.put(userName2, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName2, "",
					agentMapping, userMap, null);
			harvestWait(60);
			LOGGER.info("Checking for agentmapping when a new domain is created before the SuperDomain");
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics are not reporting under the right domain(domain1)",
					!tempResult1.contains("SuperDomain," + tomcatHost + ",Tomcat") && 
					tempResult1.contains("SuperDomain/" + domainName1 + "," + tomcatHost + ",Tomcat") && 
					!tempResult1.contains("SuperDomain/" + domainName2 + "," + tomcatHost + ",Tomcat"));
			Assert.assertTrue(
					"JBoss Agent Metrics are not reporting under the right domain(domain1)",
					!tempResult1.contains("SuperDomain," + jbossHost + ",JBoss") &&
					tempResult1.contains("SuperDomain/" + domainName1 + "," + jbossHost + ",JBoss") && 
					!tempResult1.contains("SuperDomain/" + domainName2 + "," + jbossHost + ",JBoss"));
			
			revertFile(emdomainsxmlpath, emdomainsxmlpath_backup, emMachineId);
			userMap.clear();
			userMap.put(userName2, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName2, "",
					agentMapping, userMap, null);
			harvestWait(60);
			userMap.clear();
			userMap.put(userName1, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName1, "",
					agentMapping, userMap, null);		
			harvestWait(60);	
			LOGGER.info("Checking for agentmapping when domians trade positions");			
			tempResult1 = clw.getMetricValueForTimeInMinutes(user, password,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Tomcat Agent Metrics are not reporting under the right domain(domain2)",
					!tempResult1.contains("SuperDomain," + tomcatHost + ",Tomcat") && 
					!tempResult1.contains("SuperDomain/" + domainName1 + "," + tomcatHost + ",Tomcat") && 
					tempResult1.contains("SuperDomain/" + domainName2 + "," + tomcatHost + ",Tomcat"));
			Assert.assertTrue(
					"JBoss Agent Metrics are not reporting under the right domain(domain2)",
					!tempResult1.contains("SuperDomain," + jbossHost + ",JBoss") &&
					!tempResult1.contains("SuperDomain/" + domainName1 + "," + jbossHost + ",JBoss") && 
					tempResult1.contains("SuperDomain/" + domainName2 + "," + jbossHost + ",JBoss"));			
					
    	} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
    	} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
    }
    
    /* Test Case currently fails because of the open Defect DE242389 */
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_424135_Historical_and_live_data_of_agent_when_shift_to_other_domain() {
    	
    	try {
            testcaseId = "424135";
            testCaseNameIDPath =
                "verify_ALM_424135_Historical_and_live_data_of_agent_when_shift_to_other_domain";
            testCaseStart(testCaseNameIDPath);
            userName1 = "test1";
			userName2 = "test2";
			userPassword = "";
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName1, userPassword), XMLUtil.SUCCESS_MESSAGE);
			Assert.assertEquals(XMLUtil.createUserInUsersXML(emusersxmlpath,
					userName2, userPassword), XMLUtil.SUCCESS_MESSAGE);
			domainName1 = "domain1";
			domain1agentMapping = "(.*)Tomcat(.*)";
			domainName2 = "domain2";
			domain2agentMapping = "(.*)JBoss(.*)";
			permission = "full";
			userMap.clear();
			userMap.put(userName1, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName1, "",
					domain1agentMapping, userMap, null);
			userMap.clear();
			userMap.put(userName2, permission);
			XMLUtil.createDomain(emdomainsxmlpath, domainName2, "",
					domain2agentMapping, userMap, null);
			startTestBed();
			agentExpression = "/*SuperDomain/*/|(.*)";
			metricExpression = "EM Host";			
			LOGGER.info("Waiting for 5 mins to collect data to verify historcal metrics for "+domainName1);
			harvestWait(300);						
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Calendar cal = Calendar.getInstance();
	    	String currentTimefordomain1 = dateFormat.format(cal.getTime());
	    	LOGGER.info("Current Date Time : " + currentTimefordomain1);    			
	    	cal.add(Calendar.MINUTE, -5);
	    	String pastFiveMiuntesTimefordomain1 = dateFormat.format(cal.getTime());
	    	LOGGER.info("Subtract five minutes from current date : " + pastFiveMiuntesTimefordomain1);    	
	    	
	    	tempResult1 = clw.getMetricValueForTimeInMinutes(userName1, userPassword,
					agentExpression, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir, 1).toString();
			Assert.assertTrue(
					"Agent Mapping is not working correctly for " + domainName1,					 
					tempResult1.contains("SuperDomain/" + domainName1 + "," + tomcatHost + ",Tomcat") &&
					!tempResult1.contains("SuperDomain/" + domainName1 + "," + jbossHost + ",JBoss"));
	    	
	    	List<String> domain1agenthistoricData = clw.getMetricValueInTimeRange(userName1, userPassword,  ".*Tomcat.*", "GC Heap.*", emHost, Integer.parseInt(emPort), emLibDir, pastFiveMiuntesTimefordomain1, currentTimefordomain1);
	    	Assert.assertTrue("Historical data for agent Tomcat in "+domainName1+" is not reporting",domain1agenthistoricData.size() >= 3);
	    		    		    		    	
	    	LOGGER.info("Altering domains.xml file to report all the agents under "+domainName2);
	    	Assert.assertEquals(XMLUtil.changeAttributeValueWithparentNode(
					emdomainsxmlpath, agenttag, domaintag, mappingtag,
					"(.*)Tomcat(.*)", "(.*)ABC(.*)"), XMLUtil.SUCCESS_MESSAGE);
	    	Assert.assertEquals(XMLUtil.changeAttributeValueWithparentNode(
					emdomainsxmlpath, agenttag, domaintag, mappingtag,
					"(.*)JBoss(.*)", "(.*)"), XMLUtil.SUCCESS_MESSAGE);
	    	LOGGER.info("Waiting to collect metrics for "+domainName2);
	    	harvestWait(180);	
	    	
	    	tempResult1 = clw.getLatestMetricValue(userName2, userPassword,
	    			 ".*Tomcat.*", "GC Heap.*", emHost, Integer.parseInt(emPort), emLibDir);
	        Assert.assertTrue("Tomcat metrics are not getting reported under "+domainName2,
	                 !tempResult1.equals(-1));
	    	
	    	List<String> domain2agenthistoricData = clw.getMetricValueInTimeRange(userName2, userPassword,  ".*Tomcat.*", "GC Heap.*", emHost, Integer.parseInt(emPort), emLibDir, pastFiveMiuntesTimefordomain1, currentTimefordomain1);
	    	Assert.assertTrue("Historical Data for agent Tomcat in "+domainName2+"is not reporting correctly",domain2agenthistoricData.size() < 3);
	    	domain1agenthistoricData = clw.getMetricValueInTimeRange(userName1, userPassword,  ".*Tomcat.*", "GC Heap.*", emHost, Integer.parseInt(emPort), emLibDir, pastFiveMiuntesTimefordomain1, currentTimefordomain1);
	    	Assert.assertTrue("Historical data for agent Tomcat in "+domainName1+" is not reporting after altering domains",domain1agenthistoricData.size() >= 3);
	    				
    	} catch (Exception e) {
			Assert.assertTrue(testCaseNameIDPath
					+ " failed because of the Exception " + e, false);
    	} finally {
			stopTestBed();
			revertEMFiles(testcaseId);
			revertAgentFiles();
			renameEMLogFile(testcaseId);
			// renameTomcatAgentLogFile(testcaseId);
			// renameJBossAgentLogFile(testcaseId);
			testCaseEnd(testCaseNameIDPath);
		}
    }
    
	private boolean tomcatAgentTurnOff(String user, String password,
			boolean turnOff) {
		String turnOnOrOffResultString = "";
		turnOnOrOffResultString = "Process=\"Tomcat\" Shutoff=\"true\"";

		metricExpression = "(.*Tomcat Agent.*)";
		if (turnOff) {
			clw.turnOffAgents(user, password, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
			return checkForKeyword(envProperties, EM_MACHINE_ID,
					metricshutoffconfxmlpath, turnOnOrOffResultString);
		} else {

			clw.turnOnAgents(user, password, metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
			waitForAgentNodes(metricExpression, emHost,
					Integer.parseInt(emPort), emLibDir);
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

	private boolean validateMetricValue(List<String> result, String msg) {
		boolean flag = false;

		LOGGER.info("The size for getMetricValueForTimeInMinutes is..."
				+ result);

		for (int i = 0; i < result.size(); i++) {
			LOGGER.info("Value is..." + result.get(i));
			if (result.get(i).contains(msg)) {
				LOGGER.info("FirstResult" + i);
				flag = true;
			}

		}

		return flag;

	}

	private String userLogInMsg(String userName) {
		return "User \"" + userName + "\" connected successfully";
	}

	private void addCustomDomainWithUser(String domainName,
			String agentMapping, Map<String, String> userMap) {
		XMLUtil.createDomain(emdomainsxmlpath, domainName, "", agentMapping,
				userMap, null);
		XMLUtil.createUserInUsersXML(emusersxmlpath, domainUserName, "");
		XMLUtil.createUserGrantElementForCustomDomain(emdomainsxmlpath,
				domainName, domainUserName, permission);
	}
}
