package com.ca.apm.tests.domainpermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.tests.base.OneCollectorOneTomcatTestsBase;

public class DomiansPermissionOneCollectorOneAgent extends OneCollectorOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomiansPermissionOneCollectorOneAgent.class);

    protected String testCaseId;
    protected String testCaseName;
    public String momdomainsxmlpath;
    public String momusersxmlpath;
    public String momserverxmlpath;
    public String momrealmsxmlpath;


    private String userName, userPassword, readPermission, superDomain, agentMapping, domainName, permission, fullPermission; // ,
    // fullPermission;

    private boolean userlogincheck;
    private String agentExpression;
    private String metricExpression;

    Map<String, String> groupMap = new HashMap<String, String>();
    Map<String, String> userMap = new HashMap<String, String>();

    
    List<String> listoutput = new ArrayList<String>();

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        super.initialize();

        replaceMoMProperty(ApmbaseConstants.DOMAINCONFIG_DYNAMICUPDATE_PROPERTY + "=false",
            ApmbaseConstants.DOMAINCONFIG_DYNAMICUPDATE_PROPERTY + "=true"); // enable
                                                                             // domainsconfiguration
                                                                             // dynamic update
                                                                             // property
        momdomainsxmlpath = momConfigDir + "/domains.xml";
        momusersxmlpath = momConfigDir + "users.xml";
        momserverxmlpath = momConfigDir + "/server.xml";
        momrealmsxmlpath = momConfigDir + "/realms.xml";
        readPermission = "read";
        fullPermission = "full";
        superDomain = "SuperDomain";
        agentMapping = "(.*)";
        // fullPermission = "full";

    }


    @Test(groups = {"smoke"}, enabled = true)
    public void verify_ALM_351829_Realms_Disabled_Realm() {

        try {
            testCaseId = "351829";
            testCaseName = testCaseId + "_Realms_Disabled_Realm";

            testCaseStart(testCaseName);
            userName = "Bob";
            String groupName = "Illuminati";
            String permission = "full";
            userPassword = "";
            String momtestusersxmlpath = momConfigDir + "/testusers.xml";

            String replaceStr =
                "<realm descriptor=\"Local Users and Groups Realm\" id=\"Test Realm\" active=\"false\">  <property name=\"usersFile\">   <value>testusers.xml</value>   </property>  </realm> </realms>";

            replaceMOMrelamXml("</realms>", replaceStr);

            LOGGER.info("User modification successful for relam xml file :::");

            backupFile(momusersxmlpath, momtestusersxmlpath, MOM_MACHINE_ID);

            LOGGER.info("testusers.xml successful for created in config  :::");
            Assert.assertEquals(XMLUtil.deleteUser(momtestusersxmlpath, ApmbaseConstants.CEM_ADMIN_USER),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteUser(momtestusersxmlpath, ApmbaseConstants.SAAS_ADMIN_USER),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteUser(momtestusersxmlpath, ApmbaseConstants.ADMIN_USER),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteUser(momtestusersxmlpath, ApmbaseConstants.GUEST_USER),
                XMLUtil.SUCCESS_MESSAGE);

            Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath, ApmbaseConstants.CEM_CONFIG_GROUP),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath, ApmbaseConstants.CEM_SYS_ADMIN_GROUP),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath, ApmbaseConstants.ADMIN_GROUP),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath, ApmbaseConstants.CEM_TENANT_GROUP),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath, ApmbaseConstants.CEM_ANALYST_GROUP),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.deleteGroup(momtestusersxmlpath, ApmbaseConstants.CEM_INCIDENT_ANALYST_GROUP),
                XMLUtil.SUCCESS_MESSAGE);

            LOGGER.info("testusers.xml user and group deleted :::");

            Assert.assertEquals(XMLUtil.addUser(momtestusersxmlpath, userName, userPassword), XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.addGroup(momtestusersxmlpath, "Global Conspiracy", groupName, userName),
                XMLUtil.SUCCESS_MESSAGE);

            LOGGER.info("testusers.xml user and group added :::");

            Assert.assertEquals(XMLUtil.grantPermissionGroupDomainXml(momdomainsxmlpath, groupName, permission),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.grantPermissionGroupServerXml(momserverxmlpath, groupName, permission),
                XMLUtil.SUCCESS_MESSAGE);

            LOGGER.info("domain.xml  and server.xml group permission added :::");

            startEMServices();

            LOGGER.info("user login validation start :::");

            userlogincheck =
                apmbaseutil.validateUserLoginWithCheckingAgents(momHost, momPort, userName, userPassword,
                    clwJarFileLoc, momlogDir, userLogInMsg(userName));
            Assert.assertFalse("Login was successful for the user " + userName
                + " added to testusers.xml. but expected should not sucessful", userlogincheck);
            LOGGER.info("user login validation End :::");

        } catch (Exception e) {
            Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
        } finally {
            stopEMServices();
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            revertConfigAndRenameLogsWithTestId(testCaseId, roleIds);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"smoke"}, enabled = true)
    public void verify_ALM_351821_Changes_Users_Xml_File_Without_Restarting_EM_Add_User_to_Custom_Domain() {

        try {
            testCaseId = "351821";
            testCaseName = testCaseId + "_Changes_Users_Xml_File_Without_Restarting_EM_Add_User_to_Custom_Domain";

            domainName = "Test";
            userName = "qauser";
            userPassword = "";

            testCaseStart(testCaseName);

            Assert
                .assertEquals(XMLUtil.createUserGrantElementForCustomDomain(momdomainsxmlpath, superDomain, userName,
                    readPermission), XMLUtil.SUCCESS_MESSAGE);

            LOGGER.info("domain.xml  user with  permission added :::");

            startEMServices();

            Assert.assertEquals(XMLUtil.addUser(momusersxmlpath, userName, userPassword), XMLUtil.SUCCESS_MESSAGE);

            userlogincheck =
                apmbaseutil.validateUserLoginWithCheckingAgents(momHost, momPort, userName, userPassword,
                    clwJarFileLoc, momlogDir, userLogInMsg(userName));
            Assert.assertTrue("Login was not successful for the user " + userName + " added to users.xml",
                userlogincheck);

            userName = "qauser1";
            userMap.put(userName, readPermission);
            XMLUtil.createDomain(momdomainsxmlpath, domainName, "", agentMapping, userMap, null);
            restartMOM();

            Assert.assertEquals(XMLUtil.addUser(momusersxmlpath, userName, userPassword), XMLUtil.SUCCESS_MESSAGE);

            userlogincheck =
                apmbaseutil.validateUserLoginWithCheckingAgents(momHost, momPort, userName, userPassword,
                    clwJarFileLoc, momlogDir, userLogInMsg(userName));
            Assert.assertTrue("Login was not successful for the user " + userName + " added to users.xml",
                userlogincheck);



        } catch (Exception e) {
            Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
        } finally {
            stopEMServices();
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            revertConfigAndRenameLogsWithTestId(testCaseId, roleIds);
            testCaseEnd(testCaseName);
        }
    }

    @Test(groups = {"smoke"}, enabled = true)
    public void verify_ALM_351818_Non_Existing_User_And_Group() {


        userName = "Silly";
        userPassword = "";
        permission = "Admin";

        try {
            testCaseId = "351818";
            testCaseName = testCaseId + "_Non_Existing_User_And_Group";
            String newlogFile = "IntroscopeEnterpriseManager_new.log";
            testCaseStart(testCaseName);
            replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile");

            XMLUtil.grantPermissionUserDomainXml(momdomainsxmlpath, userName, permission);
            LOGGER.info("domain.xml  user with  permission added :::");

            startEMServices();
            checkLogForMsg(
                envProperties,
                MOM_MACHINE_ID,
                momlogFile,
                "com.wily.introscope.server.enterprise.EnterpriseManagerCannotStartException: Invalid permission \"Admin\" for user \"Silly\" in resource \"SuperDomain\"");

            XMLUtil.deleteUserInDomainXml(momdomainsxmlpath, userName);

            XMLUtil.grantPermissionGroupDomainXml(momdomainsxmlpath, userName, permission);

            replaceMoMProperty("log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager.log",
                "log4j.appender.logfile.File=logs/" + newlogFile);

            restartMOM();

            checkLogForMsg(
                envProperties,
                MOM_MACHINE_ID,
                momlogDir + "/" + newlogFile,
                "com.wily.introscope.server.enterprise.EnterpriseManagerCannotStartException: Invalid permission \"Admin\" for user \"Silly\" in resource \"SuperDomain\"");

        } catch (Exception e) {
            Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
        } finally {
            stopEMServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
            testCaseEnd(testCaseName);
        }
    }
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351827_pwd_encryption_on_Cluster() {
    	
    	 try {
             testCaseId = "351827";
             testCaseName = testCaseId + "_pwd_encryption_on_Cluster";             
             testCaseStart(testCaseName);
             userPassword = apmbaseutil.encryptPassword(momInstallDir + "/tools",
 					guestPassword);
 			XMLUtil.changeAttributeValueforUser(momConfigDir + "/users.xml",
 					userliteral, nametag, guestUser, pwdliteral, guestPassword,
 					userPassword);
 			startTestBed(); 			 
 			userlogincheck = apmbaseutil.validateUserLoginWithCheckingAgents(
 					momHost, momPort, guestUser, guestPassword, clwJarFileLoc,
 					 momlogDir, userLogInMsg(guestUser));
 			Assert.assertTrue("Login was not successful for the user "
 					+ guestUser + " added to users.xml", userlogincheck); 			
    	 } catch (Exception e) {
             Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
         } finally {
             stopTestBed();            
             revertConfigAndRenameLogsWithTestId(testCaseId);
             testCaseEnd(testCaseName);
         }
    	
    }
    
    @Test(groups = {"smoke"}, enabled = true)
    public void verify_ALM_351820_Agent_Mapped_To_Custom_Domains() {

        testCaseId = "351820";
        testCaseName = testCaseId + "_Agent_Mapped_To_Custom_Domains";
        testCaseStart(testCaseName);
        userName = "Admin";
        userPassword = "";
        domainName = "Test";
        agentMapping = "(.*)Tomcat(.*)";
        List<String> result;
        try {

            groupMap.clear();
            groupMap.put(userName, fullPermission);
            XMLUtil.createDomain(momdomainsxmlpath, domainName, "TestDomain", agentMapping, null, groupMap);
            startTestBed();
            LOGGER.info("Test Bed started .....");
            agentExpression = "/*SuperDomain/*/|(.*)";
            metricExpression = "EM Host";
            harvestWait(120);
            result =
                clw.getMetricValueForTimeInMinutes(user, password, agentExpression, metricExpression, momHost,
                    Integer.parseInt(momPort), emLibDir, 1);
            LOGGER
                .info("getMetricValue result for last one minutes..." + result.size() + ">>>>>>>" + result.toString());
            // harvestWait(60*20);
            Assert.assertTrue("Tomcat Agent Metrics does not exist under Superdomain",
                validateMetricValue(result, ("SuperDomain/" + domainName + "," + tomcatHost + ",Tomcat,Tomcat Agent")));

        } catch (Exception e) {
            Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testCaseId);
            testCaseEnd(testCaseName);
        }

    }


    @Test(groups = {"smoke"}, enabled = true)
    public void verify_ALM_351853_TT50765_User_should_be_able_to_configure_plain_text_passwords() {

        testCaseId = "351853";
        testCaseName = testCaseId + "_TT50765_User_should_be_able_to_configure_plain_text_passwords";
        testCaseStart(testCaseName);

        String userElementName = "principals";
        String userAttrName = "plainTextPasswords";
        String userAttrOldValue = "false";
        String userAttrNewValue = "true";
        userName = "user1";
        userPassword = "user1pwd";


        try {
            Assert.assertEquals(XMLUtil.grantPermissionUserDomainXml(momdomainsxmlpath, userName, readPermission),
                XMLUtil.SUCCESS_MESSAGE);
            startTestBed();
            Assert.assertEquals(XMLUtil.changeAttributeValue(momusersxmlpath, userElementName, userAttrName,
                userAttrOldValue, userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath, userName, userPassword),
                XMLUtil.SUCCESS_MESSAGE);
            harvestWait(60);
            userlogincheck =
                apmbaseutil.validateUserLoginWithCheckingAgents(momHost, momPort, userName, userPassword,
                    clwJarFileLoc, momlogDir, userLogInMsg(userName));
            Assert.assertTrue("Login was not successful for the user " + userName + " added to users.xml",
                userlogincheck);
            userAttrOldValue = "true";
            userAttrNewValue = "false";
            Assert.assertEquals(XMLUtil.changeAttributeValue(momusersxmlpath, userElementName, userAttrName,
                userAttrOldValue, userAttrNewValue), XMLUtil.SUCCESS_MESSAGE);
            userName = "user2";
            userPassword = "user2pwd";
            Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath, userName, userPassword),
                XMLUtil.SUCCESS_MESSAGE);
            harvestWait(60);
            userlogincheck =
                apmbaseutil.validateUserLoginWithCheckingAgents(momHost, momPort, userName, userPassword,
                    clwJarFileLoc, momlogDir, userLogInMsg(userName));
            Assert.assertFalse("Login was successful for the user " + userName + " which is not expected",
                userlogincheck);


        } catch (Exception e) {
            Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testCaseId);
            testCaseEnd(testCaseName);
        }

    }


    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_351854_Changes_to_users_xml_file_without_restarting_the_EM_delete_user_from_a_Domain() {

        try {
            testCaseId = "351854";
            testCaseName =
                testCaseId + "_Changes_to_users_xml_file_without_restarting_the_EM_delete_user_from_a_Domain";
            testCaseStart(testCaseName);
            userName = "qauser";
            userPassword = "";
            permission = "read";
            Assert.assertEquals(XMLUtil.createUserGrantElement(momdomainsxmlpath, userName, permission),
                XMLUtil.SUCCESS_MESSAGE);
            Assert.assertEquals(XMLUtil.createUserInUsersXML(momusersxmlpath, userName, userPassword),
                XMLUtil.SUCCESS_MESSAGE);
            startEMServices();
            userlogincheck =
                apmbaseutil.validateUserLoginWithCheckingAgents(momHost, momPort, userName, userPassword,
                    clwJarFileLoc, momlogDir, userLogInMsg(userName));
            Assert.assertTrue("Login was not successful for the user " + userName + " added to domains.xml",
                userlogincheck);
            Assert.assertEquals(XMLUtil.deleteElement(momusersxmlpath, "user", "name", userName),
                XMLUtil.SUCCESS_MESSAGE);
            harvestWait(60);
            agentExpression = "(.*)";
            metricExpression = "(.*)GC Heap(.*)";
            listoutput =
                clw.getMetricValueForTimeInMinutes(userName, userPassword, agentExpression, metricExpression, momHost,
                    Integer.parseInt(momPort), emLibDir, 1);
            Assert.assertFalse("Login was successful for the user " + userName + " even after deleting from users.xml",
                listoutput.size() >= 3);
        } catch (Exception e) {
            Assert.assertTrue(testCaseName + " failed because of the Exception " + e, false);
        } finally {
            stopEMServices();
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            revertConfigAndRenameLogsWithTestId(testCaseId, roleIds);
            testCaseEnd(testCaseName);
        }
    }

    private boolean validateMetricValue(List<String> result, String msg) {
        boolean flag = false;

        LOGGER.info("The size for getMetricValueForTimeInMinutes is..." + result);

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

    /**
     * Replaces the specified string with the given string in EM relam.xml file
     * 
     * @param filepath
     * @param findStr
     * @param replaceStr
     */
    private void replaceMOMrelamXml(String findStr, String replaceStr) {
        replaceProp(findStr, replaceStr, MOM_MACHINE_ID, momrealmsxmlpath);
    }
}
