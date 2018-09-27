package com.ca.apm.tests.domainpermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.AutomationConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.AssertTests;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.common.introscope.util.AbstractMetricsUtil;
import com.ca.apm.tests.common.introscope.util.AbstractMetricsUtilFactory;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.testbed.DomainPermissionsLinuxStandaloneTestbed;
import com.ca.tas.role.webapp.TomcatRole;


/**
 * APM Base - Domains Permission Management Test cases
 * 
 * @author JAMSA07
 * 
 */
public class DomainsPermissionManagementTest extends BaseAgentTest {
    
    TestUtils utility = new TestUtils();
    protected String emMachineId;
    protected String agentMachineId;
    protected String emRoleId;
    protected String tomcatRoleId;
    protected String jbossRoleId;
    protected String tomcatAgentHost;
    protected String emhost;
    protected String emport;
    protected String emLibDir;
    protected String emLogFile;
    protected String emLogFileLoc;
    protected CLWCommons clwCommon;
    protected AssertTests assertTest;
    protected String agentProfilePath;
    protected String emConfigPath;
    protected CLWBean clw, clwAdmin = null;
    private static final Logger LOGGER = LoggerFactory
        .getLogger(DomainsPermissionManagementTest.class);
    public ResourceBundle rolePreperties = Utf8ResourceBundle.getBundle("role");
    protected String clwJarFileLoc;
    protected String emExe;
    protected String emPath;
    protected String domainXmlPath;
    protected String realmsXmlFilePath;
    protected String userXmlFilePath;
    protected String serverXmlFilePath;
    protected String encryptedPassword = "";
    protected String testUserXmlFilePath;
    protected String catlinaPath = "/logs/catalina.";
    protected String stopMessage = "Stopping service Catalina";
    protected String tomcatAgentProfilePath;
    protected String JBossAgentProfilePath;
    protected String webLogicAgentProfilePath;
    protected String webSphereAgentProfilePath;
    public static final String SUCCESS_MESSAGE = ApmbaseConstants.SUCCESS_MESSAGE;
    private boolean expectedValue = true;
    protected String agentTomcatLogPrefix = ApmbaseConstants.TOMCAT_AGENT_PREFIX;
    protected String agentJbossLogPrefix = ApmbaseConstants.JBOSS_AGENT_PREFIX;
    protected String agentTomcatLogPath;
    protected String agentJBossLogPath = System.getProperty("results.dir") + "/"
        + agentJbossLogPrefix + ".log";
    public String agentAutoprobeLogPath = System.getProperty("results.dir") + "/"
        + agentTomcatLogPrefix + ".Autoprobe.log";
    protected static String resultsDir = System.getProperty("results.dir");

    /**
     * Constructor for initializing variables
     */

    public DomainsPermissionManagementTest() {

        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
            emExe = ApmbaseConstants.EM_EXE;
        else
            emExe = ApmbaseConstants.EM_EXE_LINUX;


        emMachineId = DomainPermissionsLinuxStandaloneTestbed.EM_MACHINE_ID;

        emRoleId = DomainPermissionsLinuxStandaloneTestbed.EM_ROLE_ID;

        tomcatRoleId = DomainPermissionsLinuxStandaloneTestbed.TOMCAT_ROLE_ID;

        emPath =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        emConfigPath =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        domainXmlPath = emPath + ApmbaseConstants.DOMAINFilePath;
        userXmlFilePath = emPath + ApmbaseConstants.USERSFilePath;

        emhost = envProperties.getMachineHostnameByRoleId(DomainPermissionsLinuxStandaloneTestbed.EM_ROLE_ID);
        emport = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_PORT);
        emLibDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        emLogFile =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);
        emLogFileLoc =
            envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LOG_DIR);

        clwJarFileLoc = emLibDir + "CLWorkstation.jar";
        tomcatAgentHost =
            envProperties
                .getMachineHostnameByRoleId(DomainPermissionsLinuxStandaloneTestbed.TOMCAT_AGENT_ROLE_ID);
        tomcatAgentProfilePath =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/core/config/"
                + ApmbaseConstants.AGENT_PROFILE;

        realmsXmlFilePath = emPath + ApmbaseConstants.RealmsFilePath;
        serverXmlFilePath = emPath + ApmbaseConstants.ServerFilePath;
        testUserXmlFilePath = emPath + ApmbaseConstants.testUserXmlFilePath;
        agentTomcatLogPath =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/logs/"
                + agentTomcatLogPrefix
                + ".log";

    }


    /**
     * Global setup and creating CLW instance.
     * 
     */
    @BeforeTest
    public void initIscopeCLW() {

        LOGGER.info("Start of initIscopeCLW method");

        LOGGER.info(" CLW object parameters: emhost: " + emhost + " emuser: " + "admin"
            + " empassw: " + "" + "emport:" + 5001 + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, "admin", "", Integer.parseInt("5001"), clwJarFileLoc);
        LOGGER.info("End of initIscopeCLW method");

        syncTimeOnMachines(Arrays.asList(DomainPermissionsLinuxStandaloneTestbed.EM_MACHINE_ID,
            DomainPermissionsLinuxStandaloneTestbed.AGENT_MACHINE_ID));

        runSerializedCommandFlowFromRole(DomainPermissionsLinuxStandaloneTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_START);
        
        startEM(emRoleId);
        startTomcatAgent(tomcatRoleId);
        utility.connectToURL(
            "http://"
                + tomcatAgentHost
                + envProperties.getRolePropertyById(tomcatRoleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_PORT), 10);
        waitForAgentNodes();
        utility.connectToURL(
            "http://"
                + tomcatAgentHost
                + envProperties.getRolePropertyById(tomcatRoleId,
                    DeployTomcatFlowContext.ENV_TOMCAT_PORT), 100);
        
        stopEM(emRoleId);

    }

    @Test(groups = {"Domains", "DomainPermissions", "EM", "BAT"})
    public void verify_ALM_280488_AgentMappedToCustomDomains() {
        try {

            testCaseStart("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains - Permission Management/ 053 - Agent mapped to custom domains[TestID:280488]");

            backupDomainFile();
            checkAgentMappedToCustomDomain("Test", "TestDomain", "(.*)Tomcat(.*)", "Admin", "full");
            startEM(emRoleId);
            verifyAgentForCustomDomainMapping(emhost, emport, "admin", "", "Tomcat");
            stopEM(emRoleId);
            revertDomainFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        testCaseEnd("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains - Permission Management/ 053 - Agent mapped to custom domains[TestID:280488]");

    }

    @Test(groups = {"Domains", "DomainPermissions", "EM", "BAT"})
    public void verify_ALM_305594_EncryptionToolForUserPasswords() {
        try {

            testCaseStart("APM/APM Platform/Functional/GA EM/Domains - Permission Management/020 - Encryption tool for User Passwords[TestID:305594]");


            backupUserFile();
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
                encryptPassword(emPath + "/tools", "SHA2Encoder.bat mypassword", "mypassword",
                    "2a.100000.GyHhguOPnV8HTMkKg/gURg==.ebzOJWAZbuxETdonVUjbKQ==");
            else if (System.getProperty("os.name").toUpperCase().contains("LINUX"))
                encryptPassword(emPath + "/tools", "./SHA2Encoder.sh mypassword", "mypassword",
                    "2a.100000.GyHhguOPnV8HTMkKg/gURg==.ebzOJWAZbuxETdonVUjbKQ==");


            replacePasswordInUserXml(userXmlFilePath, "user", "password",
                "2a.1000.a9hZlUjIZUVV4vMjkv3BtA==.Yswf7wbWLN6rvbfb9jaXoQ==");
            startEM(emRoleId);
            loginToWS("Guest", "mypassword");
            stopEM(emRoleId);
            // revertUserFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        testCaseEnd("APM/APM Platform/Functional/GA EM/Domains - Permission Management/020 - Encryption tool for User Passwords[TestID:305594]");

    }


    @Test(groups = {"Domains", "DomainPermissions", "EM", "BAT"})
    public void verify_ALM_305619_AllAgentsVisibleUnderSuperDomain() {
        try {

            testCaseStart("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains - Permission Management/044 - All agents visible under super domain[TestID:305619]");

            backupDomainFile();
            createDomainWithUser("One", "", "(.*)TomcatAgent(.*)");
            changeAttributeInXml("agent", "SuperDomain", "(.*)", "(.*)Tomcat(.*)");
            startEM(emRoleId);

            verifyAgentsUnderSuperDomain(emhost, emport, "Admin", "", "Tomcat Agent", "Tomcat");

            stopEM(emRoleId);
            revertDomainFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        testCaseEnd("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains - Permission Management/044 - All agents visible under super domain[TestID:305619]");

    }

    @Test(groups = {"Domains", "DomainPermissions", "EM", "BAT"})
    public void verify_ALM_305625_Domains_xmlShouldbeHotConfigurable() {
        try {

            testCaseStart("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains -verify_ALM_305625_Domains_xmlShouldbeHotConfigurable[TestID:305625]");

            backupDomainFile();
            deletePermission("grant", "group", "Admin");
            deleteUserAdminPermission("grant", "user", "Admin");
            startEM(emRoleId);
            createCLWBean(
                emhost,
                "Admin",
                "",
                emport,
                "false",
                "*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Host");
            createPermission("Admin", "full");
            createCLWBean1(
                emhost,
                "Admin",
                "",
                emport,
                "true",
                "*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Host");
            stopEM(emRoleId);
            revertDomainFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        testCaseEnd("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains -verify_ALM_305625_Domains_xmlShouldbeHotConfigurable[TestID:305625]");

    }


    @Test(groups = {"Domains", "DomainPermissions", "EM", "BAT"})
    public void verify_ALM_305617_AssignUserToMultipleGroups() {
        try {

            testCaseStart("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains - verify_ALM_305617_AssignUserToMultipleGroups[TestID:305617]");

            backupUserFile();
            backupDomainFile();
            createDomainWithMultipleGroups1("Test", "", "One", "read", "Two", "write", "Three",
                "full");
            createUser("Bob", "");
            addUserTOGroupInUsersXML1("", "One", "Bob");
            addUserTOGroupInUsersXML2("", "Two", "Bob");
            addUserTOGroupInUsersXML3("", "Three", "Bob");
            startEM(emRoleId);

            createCLWBeanPermission1(
                emhost,
                "Bob",
                "",
                emport,
                "true",
                "No transaction traces collected",
                "*SuperDomain/Test*|"+tomcatAgentHost+"|Tomcat|Tomcat Agent|GC Heap:Bytes Total");
            stopEM(emRoleId);
            revertUserFile();
            revertDomainFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        testCaseEnd("APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Domains - verify_ALM_305617_AssignUserToMultipleGroups[TestID:305617]");

    }


    /**
     * This method is to checkAgentMappedToCustomDomain
     * 
     * @param domainName
     *        - name of the domain
     * @param description
     *        - description for the domain
     * @param agentMapping
     *        - mapping to agent
     * @param group
     *        - name of the group to be added in the domain
     * @param permission
     *        - permission for the group
     * 
     */
    public void checkAgentMappedToCustomDomain(String domainName, String description,
        String agentMapping, String group, String permission) {
        LOGGER.info("Executing  checkAgentMappedToCustomDomain");
        LOGGER.info("domainName is" + domainName);
        LOGGER.info("agentMapping is" + agentMapping);
        LOGGER.info("group is" + group);
        LOGGER.info("description is" + description);
        LOGGER.info("permission is" + permission);
        boolean fileEdit = false;
        try {
            LOGGER.info("Inside  try block of checkAgentMappedToCustomDomain");
            Map<String, String> groupsMap = new HashMap<String, String>();
            groupsMap.put(group, permission);
            XMLUtil.createDomain(domainXmlPath, domainName, description, agentMapping, null,
                groupsMap);

            Document document = XMLUtil.getDocument(domainXmlPath);
            Node domain = (Node) document.getElementsByTagName("domain").item(0);
            if (domain != null) {
                fileEdit = true;
                LOGGER.info("Created domain successfully");
            }
            LOGGER.info("domainXmlPath is" + domainXmlPath);
            LOGGER.info("Out of  try block of checkAgentMappedToCustomDomain");
        } catch (Exception e) {
            LOGGER.info("Inside  catch block of checkAgentMappedToCustomDomain");
            LOGGER.error(e.getMessage());
            fileEdit = false;
            LOGGER.info("Out of  catch block of checkAgentMappedToCustomDomain");
        }
        Assert.assertTrue(fileEdit);
        LOGGER.info("Exiting  checkAgentMappedToCustomDomain");
    }

    /**
     * This method is to verifyAgentForCustomDomainMapping
     * 
     * @param emhost
     *        - hostname of EM machine
     * @param emport
     *        - port number of EM
     * @param emuser
     *        - username
     * @param empassw
     *        - password for the user
     * @param agentName
     *        - name of the agent
     * @throws Exception
     * 
     */
    public void verifyAgentForCustomDomainMapping(String emhost, String emport, String emuser,
        String empassw, String agentName) throws Exception {
        LOGGER.info("Executing  verifyAgentForCustomDomainMapping method");
        LOGGER.info("EM Hostname" + emhost);
        LOGGER.info("EM Port" + emport);
        LOGGER.info("EM Username" + emuser);
        LOGGER.info("EM Password" + empassw);
        LOGGER.info("agentName" + agentName);

        boolean metricsExists = false;

        for (int i = 0; i < 3; ++i) {
            metricsExists = runGCHeapMetric(emhost, emport, emuser, empassw, agentName);
            LOGGER.info("metricsExists value is: " + metricsExists);
            if (metricsExists) {
                break;
            } else {
                LOGGER
                    .info("Metric is not yet found. Sleeping for 30 milliseconds and trying again.");
                Thread.sleep(30);
            }
        }

        if (metricsExists == true) {
            LOGGER.info("Metric found: " + agentName);
            Assert.assertEquals(metricsExists, true);
        } else {
            LOGGER.info("Unable to find metric: " + agentName);
            Assert.fail("Metric is not yet found");
        }
        LOGGER.info("Exiting verifyAgentForCustomDomainMapping method");
    }

    /**
     * Delete Node in the XML file
     * 
     * @param xmlFilePath
     *        - Input xml file with Path
     * @param element
     *        - Element which need to delete
     * @param attrName
     *        - Attribute need to delete
     * @param attrValue
     *        - Attribute value to be deleted
     */
    public void deleteNode(String element, String attrName, String attrValue) {
        LOGGER.info("Executing  deleteNode method");
        LOGGER.info("Value of element" + element);
        LOGGER.info("Value of attrName" + attrName);
        LOGGER.info("Value of attrValue" + attrValue);
        String result = null;
        LOGGER.info("Value of domainXmlPath" + domainXmlPath);
        result = XMLUtil.deleteElement(domainXmlPath, element, attrName, attrValue);
        LOGGER.info("Value of result" + result);

        LOGGER.info("Exiting  deleteNode method");
        if (result.equals(XMLUtil.SUCCESS_MESSAGE)) {
            LOGGER.info("Node deleted successfully");
        } else {
            LOGGER.info("Unable to delete node");
        }
        Assert.assertEquals(result, XMLUtil.SUCCESS_MESSAGE);
    }

    /**
     * Method to encrypt password
     * 
     * @param directoryLocation
     *        - location of the directory
     * 
     * @param command
     *        - command to be executed
     * 
     * @param message
     *        - message to be checked after executing the command
     * 
     */
    public String encryptPassword(String directoryLocation, String command, String message,
        String attrNewValue) {
        LOGGER.info("Executing  encryptPassword method");
        LOGGER.info("Value of directoryLocation" + directoryLocation);
        LOGGER.info("Value of command" + command);
        LOGGER.info("Value of message" + message);
        boolean assertion = false;
        try {
            LOGGER.info("Executing try block of  encryptPassword method");
            encryptedPassword = runCommand(directoryLocation, command, message);
            LOGGER.info("Exiting  try block of encryptPassword method : JAMMI" + encryptedPassword);
        } catch (IOException e) {
            LOGGER.info("Executing catch block of encryptPassword method");
            LOGGER.error(e.getMessage());
            LOGGER.info("Exiting catch block of encryptPassword method");
        }
        // the SHA2 hash cannot match
        // example of SHA2: "2a.1000.pWMuH2DqwJ3eQr2enP5cwA==.pfABNHqLOs+6iylrWpu8AQ=="
//         String[] encryptedList = encryptedPassword.split("\\.");
//         String[] newValueList = attrNewValue.split("\\.");
//         if ((encryptedList.length == 4) && (encryptedList.length == newValueList.length)
//         && (encryptedList[0].equals(newValueList[0]))
//         && (encryptedList[2].length() == newValueList[2].length())
//         && (encryptedList[3].length() == newValueList[3].length())) {
//        
//         assertion = true;
//         LOGGER.info("Got the encrypted password");
//         }
//         Assert.assertEquals(assertion, true);
//         LOGGER.info("Exiting  encryptPassword method");

        return encryptedPassword;

    }

    /**
     * Method to replace password for Guest user in user.xml
     * 
     * @param userXmlFilePath
     *        - path of the users.xml file
     * 
     * @param element020
     *        - name of the element
     * 
     * @param attrName020
     *        - name of the property to be replaced
     * 
     * @param attrOldValue
     *        - old value of the attribute
     * 
     */
    public void replacePasswordInUserXml(String userXmlFilePath, String element020,
        String attrName020, String attrOldValue) {

        LOGGER.info("Executing   replacePasswordInUserXml method");
        LOGGER.info("Value of userXmlFilePath" + userXmlFilePath);
        LOGGER.info("Value of element020" + element020);
        LOGGER.info("Value of attrName020" + attrName020);
        LOGGER.info("Value of attrOldValue" + attrOldValue);

        String result =
            XMLUtil.changeAttributeValue(userXmlFilePath, element020, attrName020, attrOldValue,
                encryptedPassword);
        LOGGER.info("Value of result" + result);

        if (result.equals(XMLUtil.SUCCESS_MESSAGE)) {
            LOGGER.info("Password replaced successfully");
        } else {
            LOGGER.info("Unable to replace password");
        }
        Assert.assertEquals(result, XMLUtil.SUCCESS_MESSAGE);
        LOGGER.info("Exiting   replacePasswordInUserXml method");
    }

    /**
     * method to WorkStation login
     * 
     * @param emhost
     *        - hostname of the EM
     * @param userName
     *        - name of the user
     * @param password
     *        - password for the user
     * @param emport
     *        - port number of EM
     * @param vMetricEMhost
     *        - metric to be verified
     */
    public void loginToWS(String user, String password) {
        String basicMetric =
            "*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager|Health:CPU Capacity (%)";
        // "\\\\*SuperDomain\\\\*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager\\:Host";



        LOGGER.info("Executing loginToWS method");
        try {
            LOGGER.info("Executing loginToWS method");

            CLWBean clw =
                new CLWBean(emhost, user, password, Integer.parseInt(emport), clwJarFileLoc);
            LOGGER.info("Exiting loginToWS method");
            boolean metricExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric);
            LOGGER.info("Mteric exists :" + metricExists);
            //Assert.assertTrue(metricExists);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Assert.fail("Failed WorkStation Login");
        }

        LOGGER.info("Exiting loginToWS method");
    }

    /**
     * Test method to run bat file
     * 
     * @param directoryLocation
     *        - location
     * @param command
     *        - command to be executed
     * @param message
     *        - message to be checked for
     */
    private String runCommand(String directoryLocation, String command, String message)
        throws IOException {
        LOGGER.info("Executing runCommand method");
        LOGGER.info("directoryLocation" + directoryLocation);
        LOGGER.info("command" + command);
        LOGGER.info("message" + message);
        BufferedReader reader = null;
        Process process = null;
        String messageFound = "";
        try {
            LOGGER.info("Executing try block of runCommand method");
            LOGGER.info("Command being executed is " + command);
            String[] startCmnd = {command};
            process = ApmbaseUtil.getProcess(startCmnd, directoryLocation);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                LOGGER.info("Executing while block of runCommand method");
                LOGGER.info("COMMAND EXECUTED SUCESSFULLY :" + line);
                System.out.println(count++);
                if (line.contains(message)) {
                    LOGGER.info("COMMAND EXECUTED SUCESSFULLY :" + line);
                    String[] encryptedPass = line.split(":");
                    if (encryptedPass.length >= 1) {
                        messageFound = encryptedPass[1];
                        LOGGER.info("Value of messageFound " + messageFound);
                    }
                }
                LOGGER.info("Exiting while block of runCommand method");
            }
            LOGGER.info("Exiting try block of runCommand method");
        } catch (Exception e) {
            LOGGER.info("Executing catch block of runCommand method");
            LOGGER.error(e.getMessage());
            LOGGER.info("Exiting while block of runCommand method");
        } finally {
            LOGGER.info("Executing finally  block of runCommand method");
            if (reader != null) {
                reader.close();
            }
            if (process != null) {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
                process.destroy();
            }
            LOGGER.info("Exiting finally  block of runCommand method");
        }
        LOGGER.info("Value of messageFound is:" + messageFound);
        LOGGER.info("Exiting runCommand method");
        return messageFound;
    }

    /**
     * This method is to Assign run_tracer permission to a group with multiple
     * users
     * 
     * @param domainName041
     *        - name of the domain
     * @param description041
     *        - description of the domain
     * @param agentMapping041
     *        - mapping for the agent
     * @param group041
     *        - name of the group associated to the domain
     * @param permission04101
     *        - permission for the group
     * @param permission04102
     *        - permission for the group
     */
    public void modifyDomainXmlForMultipleUser(String domainName041, String description041,
        String agentMapping041, String group041, String permission04101, String permission04102) {
        LOGGER.info("Executing modifyDomainXmlForMultipleUser method");
        LOGGER.info("Value of domainName041 :" + domainName041);
        LOGGER.info("Value of description041 :" + description041);
        LOGGER.info("Value of agentMapping041 :" + agentMapping041);
        LOGGER.info("Value of group041 :" + group041);
        LOGGER.info("Value of permission04101 :" + permission04101);
        LOGGER.info("Value of permission04102 :" + permission04102);
        LOGGER.info("Value of domainXmlPath :" + domainXmlPath);
        String message = null;
        Map<String, String> groupsMap = new HashMap<String, String>();
        groupsMap.put(group041, permission04101);
        XMLUtil.createDomain(domainXmlPath, domainName041, description041, agentMapping041, null,
            groupsMap);

        try {
            LOGGER.info("Executing try block of  modifyDomainXmlForMultipleUser method");
            Document document = XMLUtil.getDocument(domainXmlPath);

            /** getting root element 'SuperDomain' */
            Node superDomainElement = document.getElementsByTagName("domain").item(0);

            /** creating a new user element */
            Element grantElement = document.createElement("grant");
            grantElement.setAttribute("group", group041);
            grantElement.setAttribute("permission", permission04102);
            superDomainElement.appendChild(grantElement);

            XMLUtil.writeToXMLFile(document, domainXmlPath);
            message = AutomationConstants.SUCCESS_MESSAGE;
            LOGGER.info("Exiting try block of  modifyDomainXmlForMultipleUser method");

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        if (message.equals(AutomationConstants.SUCCESS_MESSAGE)) {
            LOGGER.info("Domain created successfully");
        } else {
            LOGGER.info("Unable to create domain");

        }
        Assert.assertEquals(message, AutomationConstants.SUCCESS_MESSAGE);
        LOGGER.info("Exiting modifyDomainXmlForMultipleUser method");
    }

    /**
     * Method to create user and add it in the Group in user.xml
     * 
     * @param userXmlFilePath
     *        - path of users.xml
     * @param userName041
     *        - name of the user
     * @param password041
     *        - password of the user
     * @param groupDescription041
     *        - description of the group
     * @param userGroup041
     *        - group name
     * @param user041
     *        - user to the group
     * 
     */
    
    public void addGroupWithUserInUserXml(String userXmlFilePath, String userName041,
        String password041, String groupDescription041, String userGroup041, String user041) {
        LOGGER.info("Executing addGroupWithUserInUserXml method");
        LOGGER.info("Value of userXmlFilePath :" + userXmlFilePath);
        LOGGER.info("Value of userName041 :" + userName041);
        LOGGER.info("Value of password041 :" + password041);
        LOGGER.info("Value of groupDescription041 :" + groupDescription041);
        LOGGER.info("Value of userGroup041 :" + userGroup041);
        LOGGER.info("Value of user041 :" + user041);
        String result = "";
        String result1 = XMLUtil.createUserInUsersXML(userXmlFilePath, userName041, password041);
        LOGGER.info("Value of result1 :" + result1);
        String result2 =
            XMLUtil.createGroupAddSingleUserInUsersXML(userXmlFilePath, groupDescription041,
                userGroup041, user041);
        LOGGER.info("Value of result2 :" + result2);
        if (result1.equals(XMLUtil.SUCCESS_MESSAGE) && result2.equals(XMLUtil.SUCCESS_MESSAGE))
            result = XMLUtil.SUCCESS_MESSAGE;

        if (result.equals(XMLUtil.SUCCESS_MESSAGE)) {
            LOGGER.info("Added user to xml successfully");

        } else {
            LOGGER.info("Unable to add user to XML");
        }
        LOGGER.info("Exiting addGroupWithUserInUserXml method");
        Assert.assertEquals(result, XMLUtil.SUCCESS_MESSAGE);
    }

    /**
     * Test method for taking backup of users.xml file
     * 
     * @throws IOException
     */
    public void backupUserFile() throws IOException {
        LOGGER.info("Executing backupUserFile method");
        String message = ApmbaseUtil.fileBackUp(userXmlFilePath);

        if (message == ApmbaseConstants.SUCCESS_MESSAGE) {
            LOGGER.info("Users xml backuped successfully");

        } else {
            LOGGER.info("Unable to backup user xml file");
        }
        LOGGER.info("Exiting backupUserFile method");
        Assert.assertEquals(message, ApmbaseConstants.SUCCESS_MESSAGE);
    }

    /**
     * Test method for taking backup of users.xml file
     * 
     * @throws IOException
     */
    public void revertUserFile() throws IOException {

        LOGGER.info("Executing revertUserFile method");
        String message = ApmbaseUtil.revertFile(userXmlFilePath);

        if (message == ApmbaseConstants.SUCCESS_MESSAGE) {
            LOGGER.info("Users xml reverted successfully");

        } else {
            LOGGER.info("Unable to revert user xml file");
        }
        LOGGER.info("Exiting revertUserFile method");
        Assert.assertEquals(message, ApmbaseConstants.SUCCESS_MESSAGE);
    }

    /**
     * Test method for taking backup of domains.xml file
     * 
     * @throws IOException
     */
    public void revertDomainFile() throws IOException {
        LOGGER.info("Executing revertDomainFile method");
        LOGGER.info("Value of domainXmlPath :" + domainXmlPath);
        String message = ApmbaseUtil.revertFile(domainXmlPath);

        if (message == ApmbaseConstants.SUCCESS_MESSAGE) {
            LOGGER.info("Domains xml reverted successfully");

        } else {
            LOGGER.info("Unable to revert domain xml file");
            Assert.fail("Unable to revert the domain.xml file");
        }
        LOGGER.info("Exiting revertDomainFile method");
        Assert.assertEquals(message, ApmbaseConstants.SUCCESS_MESSAGE);
    }

    /**
     * Test method for taking backup of domains.xml file
     * 
     * @throws IOException
     */
    public void backupDomainFile() throws IOException {
        LOGGER.info("Executing backupDomainFile method");
        LOGGER.info("Value of domainXmlPath: " + domainXmlPath);
        String message = ApmbaseUtil.fileBackUp(domainXmlPath);

        if (message == ApmbaseConstants.SUCCESS_MESSAGE) {
            LOGGER.info("Domains xml backedup successfully");

        } else {
            LOGGER.info("Unable to revert domain xml file");
        }
        LOGGER.info("Exiting backupDomainFile method");
        Assert.assertEquals(message, ApmbaseConstants.SUCCESS_MESSAGE);

    }

    /**
     * Test method to update Weblogic Agent Profile
     * 
     * @param autonaming
     *        - Autonaming property
     * @param agentName
     *        - name of the agent
     * @param agentProcess
     *        - Agentprocess
     * @param agentLogPrefix
     *        - Logprefix
     */
    public void updateAgentProfileBase1(String autonaming, String agentName, String agentProcess,
        String agentLogPrefix) {
        boolean propertyUpdated = false;
        LOGGER.info("Executing updateAgentProfileBase1 method");
        LOGGER.info("Value of autonaming: " + autonaming);
        LOGGER.info("Value of agentName: " + agentName);
        LOGGER.info("Value of agentProcess: " + agentProcess);
        LOGGER.info("Value of agentLogPrefix: " + agentLogPrefix);
        try {
            LOGGER.info("Executing try block of updateAgentProfileBase1 method");
            // update agent profile
            /*
             * baseAgentTest.agentProfilePath =
             * System.getProperty("role_agent.tomcat.install.dir") + "/core/config/"
             * + System.getProperty("role_agent.agent.profile");
             * baseAgentTest.agentLogPath =
             * System.getProperty("results.dir") + "/" + agentLogPrefix + ".log";
             * baseAgentTest.agentAutoprobeLogPath =
             * System.getProperty("results.dir") + "/" + agentLogPrefix + ".Autoprobe.log";
             */

            LOGGER.info("*** agentProfilePath: ** " + tomcatAgentProfilePath);
            LOGGER.info("*** agentLogPrefix: ** " + agentLogPrefix);
            LOGGER.info("*** agentLogPath: ** " + agentTomcatLogPath);
            LOGGER.info("*** agentAutoprobeLogPath: ** " + agentAutoprobeLogPath);

            Properties properties = Util.loadPropertiesFile(tomcatAgentProfilePath);
            properties.setProperty(AutomationConstants.AGENT_AUTONAMING_PROPERTY, autonaming);
            properties.setProperty(AutomationConstants.AGENT_NAME_PROPERTY, agentName);
            properties.setProperty(AutomationConstants.AGENT_CUSTOM_PROCESS_NAME_PROPERTY,
                agentProcess);
            properties.setProperty(AutomationConstants.AGENT_LOG_PATH_PROPERTY,
                tomcatAgentProfilePath);
            properties.setProperty(AutomationConstants.AGENT_AUTOPROBE_LOG_PATH_PROPERTY,
                agentAutoprobeLogPath);

            Util.writePropertiesToFile(tomcatAgentProfilePath, properties);
            String propertyvalue = "";
            int flag1 = 0, flag2 = 0, flag3 = 0, flag4 = 0, flag5 = 0;

            propertyvalue = properties.getProperty(AutomationConstants.AGENT_AUTONAMING_PROPERTY);
            if (propertyvalue.equalsIgnoreCase(autonaming)) flag1 = 1;

            propertyvalue = properties.getProperty(AutomationConstants.AGENT_NAME_PROPERTY);
            if (propertyvalue.equalsIgnoreCase(agentName)) flag2 = 1;

            propertyvalue =
                properties.getProperty(AutomationConstants.AGENT_CUSTOM_PROCESS_NAME_PROPERTY);

            if (propertyvalue.equalsIgnoreCase(agentProcess)) flag3 = 1;

            propertyvalue = properties.getProperty(AutomationConstants.AGENT_LOG_PATH_PROPERTY);
            if (propertyvalue.equalsIgnoreCase(tomcatAgentProfilePath)) flag4 = 1;

            propertyvalue =
                properties.getProperty(AutomationConstants.AGENT_AUTOPROBE_LOG_PATH_PROPERTY);
            if (propertyvalue.equalsIgnoreCase(agentAutoprobeLogPath)) flag5 = 1;

            if (flag1 == 1 && flag2 == 1 && flag3 == 1 && flag4 == 1 && flag5 == 1)
                propertyUpdated = true;
            else
                propertyUpdated = false;
            if (propertyUpdated) {
                LOGGER.info("Updated property successfully");
            }
            Assert.assertTrue(propertyUpdated);
            LOGGER.info("Exiting try block of updateAgentProfileBase1 method");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Assert.fail("Test failed because of the following reason: ", e);
        }
    }

    /**
     * This method is used to check for GCHeapMetric
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param user_name
     *        - WS Username
     * @param user_pass
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param agentName
     *        - name of agent
     */
    private boolean runGCHeapMetric(String emhost, String emport, String user_name,
        String user_pass, String agentName) throws Exception {
        LOGGER.info("Executing runGCHeapMetric method");
        String agentNameMatching = "\".*" + agentName + ".*\"";
        String metricGCHeap = "\\" + '"' + "GC Heap" + "\\" + ":Bytes Total" + "\\" + '"';
        String host = "-Dhost=" + emhost;
        String port = "-Dport=" + emport;
        String userName = "-Duser=" + user_name;
        String userPass = "-Dpassword=" + user_pass;

        CLWBean clwBean =
            new CLWBean(emhost, user_name, user_pass, Integer.parseInt(emport), clwJarFileLoc);

        AbstractMetricsUtil metricUtil = AbstractMetricsUtilFactory.create(clwBean);


        String agentRegExp = ".*" + agentName + ".*";
        boolean metricExists =
            metricUtil.metricExists(null, null, null, agentRegExp, "GC Heap:Bytes Total");
        LOGGER.info("Metric exists: " + metricExists);
        LOGGER.info("Exiting runGCHeapMetric method");
        return metricExists;
    }

    /**
     * This method is to run a command and check console. This method is
     * generalised for all other test cases.
     * 
     * @param command
     *        - command to be executed
     * @param dir
     *        - location from which it has to be executed
     * @param agentName
     *        - Name of the agent
     * @return
     * @throws Exception
     */
    public int executeCommand(String[] command, File dir, String agentName) throws Exception {
        LOGGER.info("Executing executeCommand method");
        int found = 0;
        Process process = null;
        List<String> clwOutputStrings = new ArrayList<String>();

        try {
            LOGGER.info("Command to execute: " + Arrays.toString(command));
            LOGGER.info("Agent Name: " + agentName);
            ProcessBuilder processbuilder = new ProcessBuilder(command);
            processbuilder.directory(dir);
            processbuilder.redirectErrorStream(true);
            harvestWait(60);
            process = processbuilder.start();
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                clwOutputStrings.add(line);
                LOGGER.info("Command output: " + line);
            }
            for (int j = 0; j < clwOutputStrings.size(); j++) {
                if (clwOutputStrings.get(j).contains(agentName)) {
                    found = 1;
                }
            }
            return found;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return 0;

        } finally {
            if (process != null) {
                LOGGER.info("Executing finally block of executeCommand method");
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
                LOGGER.info("Exiting finally block of executeCommand method");
            }
            LOGGER.info("Exiting executeCommand method");
        }
    }

    
    /**
     * Test method to create a user specified domain
     * 
     * @param domainName44
     *        - name of the domain to be created
     * @param description44
     *        - description of the created domain
     * @param agentMapping44
     *        - agent mapping
     */
    public void createDomainWithUser(String domainName44, String description44,
        String agentMapping44) {
        LOGGER.info("Executing createDomainWithUser method");
        LOGGER.info("domainName44 is " + domainName44);
        LOGGER.info("description44 is " + description44);
        LOGGER.info("agentMapping44 is " + agentMapping44);

        String message = "not Successful";
        try {
            LOGGER.info("Executing try block of createDomainWithUser method");
            XMLUtil.createDomain(domainXmlPath, domainName44, description44, agentMapping44, null,
                null);
            harvestWait(60);
            Document document = XMLUtil.getDocument(domainXmlPath);
            Node domain = document.getElementsByTagName("domain").item(0);
            if (domain.getNodeName() != null) {
                message = "Successful";
                LOGGER.info("Created domain successfully");
            }
            LOGGER.info("Exiting try block of createDomainWithUser method");

        } catch (Exception e) {
            message = "not Successful";
        }
        if (message.equals(SUCCESS_MESSAGE)) {
            LOGGER.info("created domain successfully");

        } else {
            LOGGER.info("Unable to create domain");
        }
        LOGGER.info("Exiting createDomainWithUser method");
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in users.xml
     * 
     * @param userElementName1
     *        - name of the element
     * @param userAttrName1
     *        - attribute name to be modified
     * @param userAttrOldValue1
     *        - Old Value
     * @param userAttrNewValue1
     *        - New Value
     */
    public void changeAttributeInXml(String userElementName44, String userAttrName44,
        String userAttrOldValue44, String userAttrNewValue44) {
        LOGGER.info("Executing changeAttributeInXml method");
        LOGGER.info("Value of userElementName44: " + userElementName44);
        LOGGER.info("Value of userAttrName44: " + userAttrName44);
        LOGGER.info("Value of userAttrOldValue44: " + userAttrOldValue44);
        LOGGER.info("Value of userAttrNewValue44: " + userAttrNewValue44);
        LOGGER.info("Value of domainXmlPath: " + domainXmlPath);

        String message =
            XMLUtil.changeAttributeValue(domainXmlPath, userElementName44, userAttrName44,
                userAttrOldValue44, userAttrNewValue44);
        harvestWait(60);
        if (message.equals(SUCCESS_MESSAGE)) {
            LOGGER.info("Change attribute successfully");
        } else {
            LOGGER.info("Unable change attribute");
        }
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
        LOGGER.info("Exiting changeAttributeInXml method");
    }

    /**
     * Test method to verify agents under super domain
     * 
     * @param emhost
     *        - hostname of the EM
     * @param emport
     *        - port number of EM
     * @param emuser
     *        - user name
     * @param empassw
     *        - password of User
     * @param agentName1
     *        - name of the element
     * @param agentName2
     *        - attribute name to be modified
     * @throws Exception
     * 
     */
    public void verifyAgentsUnderSuperDomain(String emhost, String emport, String emuser,
        String empassw, String agentName1, String agentName2) throws Exception {

        LOGGER.info("Executing verifyAgentsUnderSuperDomain method");
        LOGGER.info("Value of emhost: " + emhost);
        LOGGER.info("Value of emport: " + emport);
        LOGGER.info("Value of emuser: " + emuser);
        LOGGER.info("Value of empassw: " + empassw);
        LOGGER.info("Value of agentName1: " + agentName1);
        LOGGER.info("Value of agentName2: " + agentName2);
        boolean metricsExists1 = false, metricsExists2 = false;
        int i = 0;

        while (i < 20) {
            metricsExists1 = runGCHeapMetric(emhost, emport, emuser, empassw, agentName1);
            LOGGER.info("Value of metricsExists1" + metricsExists1);
            if (metricsExists1 == true) {
                LOGGER.info("Found metric for " + agentName1);
                break;
            } else {
                LOGGER.info("Unable to find metric for " + agentName1);
                harvestWait(60);
                i++;
            }
        }
        i=0;
        
        while (i < 20) {
            metricsExists2 = runGCHeapMetric(emhost, emport, emuser, empassw, agentName2);
            if (metricsExists2 == true) {
                LOGGER.info("Found metric for " + agentName2);
                break;
            } else {
                LOGGER.info("Unable to find metric for " + agentName2);
                harvestWait(60);
                i++;
            }

        }
        Assert.assertTrue(metricsExists1);
        Assert.assertTrue(metricsExists2);
        LOGGER.info("Exiting verifyAgentsUnderSuperDomain method");
    }

    /**
     * Test method to update Weblogic Agent Profile
     * 
     * @param autonaming2
     *        - Autonaming property
     * @param agentName2
     *        - name of the agent
     * @param agentProcess2
     *        - Agentprocess
     * @param agentLogPrefix2
     *        - Logprefix
     */

    public void deletePermission(String elementName, String attrName, String attrValue) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.deletePermission(String elementName, String attrName, String attrValue):");
        LOGGER.info("elementName: " + elementName);
        LOGGER.info("attrName: " + attrName);
        LOGGER.info("attrValue: " + attrValue);

        if (XMLUtil.containsElements(domainXmlPath, elementName, attrName, attrValue)) {
            String message;
            while(XMLUtil.containsElements(domainXmlPath, elementName, attrName, attrValue))
            {
                message = XMLUtil.deleteElement(domainXmlPath, elementName, attrName, attrValue);
                LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
                LOGGER.info("Real result message: " + message);
                Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
            }
            Assert.assertFalse(XMLUtil.containsElements(domainXmlPath, elementName, attrName,
                attrValue));
        } else {
            LOGGER.info("No elements matching input criteria found, skipping this test case.");
        }
    }


    /**
     **********************************************************************************
     *
     * Permissions1 methods starts here
     * 
     **********************************************************************************
     */


    /**
     * Test method for taking backup of realms.xml file
     * 
     * @throws IOException
     */
    public void backupRealmsFile() throws IOException {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.backupRealmsFile():");
        String message = ApmbaseUtil.fileBackUp(realmsXmlFilePath);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method for taking backup of server.xml file
     * 
     * @throws IOException
     */
    public void backupServerFile() throws IOException {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.backupServerFile():");
        String message = ApmbaseUtil.fileBackUp(serverXmlFilePath);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }


    /**
     * Test method for taking backup of realms.xml file
     * 
     * @throws IOException
     */
    public void revertRealmsFile() throws IOException {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.revertRealmsFile():");
        String message = ApmbaseUtil.revertFile(realmsXmlFilePath);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method for taking backup of server.xml file
     * 
     * @throws IOException
     */
    public void revertServerFile() throws IOException {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.revertServerFile():");
        String message = ApmbaseUtil.revertFile(serverXmlFilePath);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }


    /**
     * Global setup and creating CLW instance.
     * 
     * @param emhost
     *        - EM host - Testbed properties.
     * @param emuser
     *        - EM user name -- Testbed properties.
     * @param empassw
     *        - EM password - - Testbed properties.
     * @param emport
     *        - EM port - Testbed properties.
     */
    public void initIscopeCLW(String emhost, String emuser, String empassw, String emport) {

        LOGGER
            .info("BeforeTest com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.initIscopeCLW(String emhost, String emuser, String empassw, String emport):");
        LOGGER.info("emhost: " + emhost);
        LOGGER.info("emuser: " + emuser);
        LOGGER.info("empassw: " + empassw);
        LOGGER.info("emport: " + emport);

        clwAdmin = new CLWBean(emhost, emuser, empassw, Integer.parseInt(emport), clwJarFileLoc);
    }

    /**
     * Test method to grant permission for a specified user on domains.xml
     * 
     * @param userName
     *        - name of the user
     * @param permissionLevel
     *        - permission for the group specified
     */
    public void createPermission(String userName, String permissionLevel) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.createPermission(String userName, String permissionLevel):");
        LOGGER.info("userName: " + userName);
        LOGGER.info("permissionLevel: " + permissionLevel);

        String message = XMLUtil.createUserGrantElement(domainXmlPath, userName, permissionLevel);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to grant permission for a specified user on domains.xml
     * 
     * @param userName2
     *        - name of the user
     * @param permissionLevel2
     *        - permission for the group specified
     */
    public void createPermission2(String userName2, String permissionLevel2) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.createPermission2(String userName2, String permissionLevel2):");
        LOGGER.info("userName2: " + userName2);
        LOGGER.info("permissionLevel2: " + permissionLevel2);
        String message = XMLUtil.createUserGrantElement(domainXmlPath, userName2, permissionLevel2);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to grant permission for a specified user on domains.xml
     * 
     * @param userName3
     *        - name of the user
     * @param permissionLevel3
     *        - permission for the group specified
     */
    public void createPermission3(String userName3, String permissionLevel3) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.createPermission3(String userName3, String permissionLevel3):");
        LOGGER.info("userName3: " + userName3);
        LOGGER.info("permissionLevel3: " + permissionLevel3);
        String message = XMLUtil.createUserGrantElement(domainXmlPath, userName3, permissionLevel3);
        LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
        LOGGER.info("Real result message: " + message);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create a user specified domain
     * 
     * @param domainName
     *        - name of the domain to be created
     * @param description
     *        - description of the created domain
     * @param agentMapping
     *        - agent mapping
     * @param domainUserName
     *        - name of user to be added in the created domain
     * @param userPermission
     *        - permission for the user
     */
    public void createDomainWithUser(String domainName, String description, String agentMapping,
        String domainUserName, String userPermission) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.createDomainWithUser(String domainName, String description, String agentMapping, String domainUserName, String userPermission):");
        LOGGER.info("domainName: " + domainName);
        LOGGER.info("description: " + description);
        LOGGER.info("agentMapping: " + agentMapping);
        LOGGER.info("domainUserName: " + domainUserName);
        LOGGER.info("userPermission: " + userPermission);
        Map<String, String> usersMap = new HashMap<String, String>();
        usersMap.put(domainUserName, userPermission);
        XMLUtil.createDomain(domainXmlPath, domainName, description, agentMapping, usersMap, null);
    }

    /**
     * Test method to create a user specified domain
     * 
     * @param domainName1
     *        - name of the domain to be created
     * @param description1
     *        - description of the created domain
     * @param agentMapping1
     *        - agent mapping
     * @param domainUserName1
     *        - name of user to be added in the created domain
     * @param userPermission1
     *        - permission for the user
     */
    public void createDomainWithUser1(String domainName1, String description1,
        String agentMapping1, String domainUserName1, String userPermission1) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.createDomainWithUser1(String domainName1, String description1, String agentMapping1, String domainUserName1, String userPermission1):");
        LOGGER.info("domainName1: " + domainName1);
        LOGGER.info("description1: " + description1);
        LOGGER.info("agentMapping1: " + agentMapping1);
        LOGGER.info("domainUserName1: " + domainUserName1);
        LOGGER.info("userPermission1: " + userPermission1);

        Map<String, String> usersMap = new HashMap<String, String>();
        usersMap.put(domainUserName1, userPermission1);
        XMLUtil.createDomain(domainXmlPath, domainName1, description1, agentMapping1, usersMap,
            null);
    }

    /**
     * Test method to create a user specified domain
     * 
     * @param domainName2
     *        - name of the domain to be created
     * @param description2
     *        - description of the created domain
     * @param domainUserName2
     *        - name of user to be added in the created domain
     * @param userPermission2
     *        - permission for the user
     * @param agentMapping2
     *        - agent mapping
     */
    public void createDomainWithUser2(String domainName2, String description2,
        String agentMapping2, String domainUserName2, String userPermission2) {

        Map<String, String> usersMap = new HashMap<String, String>();
        usersMap.put(domainUserName2, userPermission2);
        XMLUtil.createDomain(domainXmlPath, domainName2, description2, agentMapping2, usersMap,
            null);
        harvestWait(20);
    }

    /**
     * 
     * Test method to create a group specified domain
     * 
     * @param domainName3
     *        - name of the domain to be created
     * @param description3
     *        - description of the created domain
     * @param domainGroupName3
     *        - name of user to be added in the created domain
     * @param domainGroupPermission3
     *        - permission for the user
     * @param agentMapping2
     *        - agent mapping
     */
    public void createDomainWithGroup(String domainName3, String description3,
        String agentMapping3, String domainGroupName3, String domainGroupPermission3) {

        Map<String, String> groupsMap = new HashMap<String, String>();
        groupsMap.put(domainGroupName3, domainGroupPermission3);
        XMLUtil.createDomain(domainXmlPath, domainName3, description3, agentMapping3, null,
            groupsMap);
        harvestWait(20);
    }

    /**
     * Test method to create multiple groups in a specified domain
     * 
     * @param userDomainName
     *        - name of the domain to be created
     * @param userDescription
     *        - description
     * @param domainUserName1
     *        - name of group to be added in the created domain
     * @param domainUserPermission1
     *        - permission for the group
     * @param domainUserName2
     *        - name of group to be added in the created domain
     * @param domainUserPermission2
     *        - permission for the group
     */
    public void createDomainWithMultipleUsers(String userDomainName, String userDescription,
        String userAgentMapping, String domainUserName1, String domainUserPermission1,
        String domainUserName2, String domainUserPermission2) {

        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put(domainUserName1, domainUserPermission1);
        userMap.put(domainUserName2, domainUserPermission2);

        XMLUtil.createDomain(domainXmlPath, userDomainName, userDescription, userAgentMapping,
            userMap, null);
        harvestWait(20);
    }

    /**
     * Test method to create multiple groups in a specified domain
     * 
     * @param groupDomainName
     *        - name of the domain to be created
     * @param groupDescription
     *        - description of the domain to be created
     * @param domainGroupName1
     *        - name of group to be added in the created domain
     * @param domainGroupPermission1
     *        - permission of the group created
     * @param domainGroupName2
     *        - name of group to be added in the created domain
     * @param domainGroupPermission2
     *        - permission of the group created
     */
    public void createDomainWithMultipleGroups(String groupDomainName, String groupDescription,
        String groupAgentMapping, String domainGroupName1, String domainGroupPermission1,
        String domainGroupName2, String domainGroupPermission2) {

        Map<String, String> groupMap = new HashMap<String, String>();
        groupMap.put(domainGroupName1, domainGroupPermission1);
        groupMap.put(domainGroupName2, domainGroupPermission2);

        XMLUtil.createDomain(domainXmlPath, groupDomainName, groupDescription, groupAgentMapping,
            null, groupMap);
         harvestWait(60);
    }

    /**
     * Test method to create multiple groups in a specified domain
     * 
     * @param groupDomainName1
     *        - name of the domain to be created
     * @param groupDescription1
     *        - description of the domain to be created
     * @param domainGroupName11
     *        - name of group to be added in the created domain
     * @param domainGroupPermission11
     *        - permission of the group created
     * @param domainGroupName21
     *        - name of group to be added in the created domain
     * @param domainGroupPermission21
     *        - permission of the group created
     * @param domainGroupName31
     *        - name of group to be added in the created domain
     * @param domainGroupPermission31
     *        - permission of the group created
     */
    public void createDomainWithMultipleGroups1(String groupDomainName1, String groupDescription1,
        String domainGroupName11, String domainGroupPermission11, String domainGroupName21,
        String domainGroupPermission21, String domainGroupName31, String domainGroupPermission31) {

        Map<String, String> groupMap = new HashMap<String, String>();
        groupMap.put(domainGroupName11, domainGroupPermission11);
        groupMap.put(domainGroupName21, domainGroupPermission21);
        groupMap.put(domainGroupName31, domainGroupPermission31);

        XMLUtil.createDomain(domainXmlPath, groupDomainName1, groupDescription1, "(.*)", null,
            groupMap);
        harvestWait(20);
    }

    /**
     * Test method to create user node in users.xml
     * 
     * @param userName
     *        - name for the user to be created
     * @param userPassword
     *        - password for the created user
     */
    public void createUser(String userName, String userPassword) {
        String message = XMLUtil.createUserInUsersXML(userXmlFilePath, userName, userPassword);
         harvestWait(60);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create user node in users.xml
     * 
     * @param userName3
     *        - name for the user to be created
     * @param userPassword3
     *        - password for the created user
     */
    public void createUser3(String userName3, String userPassword3) {
        String message = XMLUtil.createUserInUsersXML(userXmlFilePath, userName3, userPassword3);
         harvestWait(60);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create user node in testusers.xml
     * 
     * @param testUserName
     *        - name for the user to be created
     * @param testUserPassword
     *        - password for the created user
     */
    public void createUserInTestUserXml(String testUserName, String testUserPassword) {
        String message =
            XMLUtil.createUserInUsersXML(testUserXmlFilePath, testUserName, testUserPassword);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create user node in users.xml
     * 
     * @param userName1
     *        - name for the user to be created
     * @param userPassword1
     *        - password for the created user
     */
    public void createUser1(String userName1, String userPassword1) {
        String message = XMLUtil.createUserInUsersXML(userXmlFilePath, userName1, userPassword1);
         harvestWait(60);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create user node in users.xml
     * 
     * @param userName2
     *        - name for the user to be created
     * @param userPassword2
     *        - password for the created user
     */
    public void createUser2(String userName2, String userPassword2) {
        String message = XMLUtil.createUserInUsersXML(userXmlFilePath, userName2, userPassword2);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName
     *        - WS Username
     * @param password
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn
     *        - to check if user has loggedin or not
     * @param basicMetric
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean(String emhost, String userName, String password, String emport,
        String loggedIn, String basicMetric) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName + " empassw: " + password + "emport:" + emport + " Location CLW Jar file: "
            + clwJarFileLoc);

        clw = new CLWBean(emhost, userName, password, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
         harvestWait(60);
        boolean metricsExists = checkBasicMetricsExists(clw, basicMetric);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName1
     *        - WS Username
     * @param password1
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn1
     *        - to check if user has loggedin or not
     * @param basicMetric1
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean1(String emhost, String userName1, String password1, String emport,
        String loggedIn1, String basicMetric1) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName1 + " empassw: " + password1 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName1, password1, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(20);
        boolean metricsExists = checkBasicMetricsExists(clw, basicMetric1);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn1));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName2
     *        - WS Username
     * @param password2
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn2
     *        - to check if user has loggedin or not
     * @param basicMetric2
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean2(String emhost, String userName2, String password2, String emport,
        String loggedIn2, String basicMetric2) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName2 + " empassw: " + password2 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);
        clw = new CLWBean(emhost, userName2, password2, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(10);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric2);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn2));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName3
     *        - WS Username
     * @param password3
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn3
     *        - to check if user has loggedin or not
     * @param basicMetric3
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean3(String emhost, String userName3, String password3, String emport,
        String loggedIn3, String basicMetric3) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName3 + " empassw: " + password3 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);
        clw = new CLWBean(emhost, userName3, password3, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(10);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric3);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn3));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName4
     *        - WS Username
     * @param password4
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn4
     *        - to check if user has loggedin or not
     * @param basicMetric4
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean4(String emhost, String userName4, String password4, String emport,
        String loggedIn4, String basicMetric4) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName4 + " empassw: " + password4 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);
        clw = new CLWBean(emhost, userName4, password4, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
         harvestWait(60);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric4);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn4));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName5
     *        - WS Username
     * @param password5
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn5
     *        - to check if user has loggedin or not
     * @param basicMetric5
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean5(String emhost, String userName5, String password5, String emport,
        String loggedIn5, String basicMetric5) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName5 + " empassw: " + password5 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);
        clw = new CLWBean(emhost, userName5, password5, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(10);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric5);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn5));
    }

    /**
     * Test method to create property and value tag under existing realms node.
     * It is a one time creation at the time of startup.
     * 
     * @param realmElementName
     *        - Name of the element to be created
     * @param elementText
     *        - text value
     * @param realmParentNode
     *        - Parent node of the element to be created
     * @param realmParentattrName
     *        - Parent attribute name of the element to be created
     * @param realmParentAttrValue
     *        - Parent attribute value of the element to be created
     * @param descriptor
     *        - attribute of the element to be created
     * @param descriptorValue
     *        - attribute of the element to be created
     * @param id
     *        - attribute of property node
     * @param idValue
     *        - attribute value of property node
     * @param active
     *        - attribute of property node
     * @param activeValue
     *        - attribute value of property node
     */
    public void addPropToRealmsXml(String realmElementName, String realmParentNode,
        String realmParentattrName, String realmParentAttrValue, String descriptor,
        String descriptorValue, String id, String idValue, String active, String activeValue) {

        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(descriptor, descriptorValue);
        attributeMap.put(id, idValue);
        attributeMap.put(active, activeValue);

        String message =
            XMLUtil.createElement(realmsXmlFilePath, realmElementName, "", realmParentNode,
                realmParentattrName, realmParentAttrValue, attributeMap);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to copyfile from one location to another
     * 
     * @param destFileName
     *        - folder name
     * @param Filenames
     *        - files to be copied
     * @param isCopied
     *        - expected value to check if it is copied or not
     */
    public void copyFile(String destFileName, String Filenames, String isCopied) throws IOException {

        boolean isFileCopied =
            ApmbaseUtil.copyFile(emConfigPath + destFileName, emConfigPath + Filenames);
        Assert.assertEquals(Boolean.parseBoolean(isCopied), isFileCopied);
    }

    /**
     * Test method to delete a user node from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml(String element, String attrName, String attrValue) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml1(String element1, String attrName1, String attrValue1) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element1, attrName1, attrValue1);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue2
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml2(String element, String attrName, String attrValue2) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue2);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue3
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml3(String element, String attrName, String attrValue3) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue3);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue4
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml4(String element, String attrName, String attrValue4) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue4);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue5
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml5(String element, String attrName, String attrValue5) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue5);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue6
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml6(String element, String attrName, String attrValue6) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue6);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue7
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml7(String element, String attrName, String attrValue7) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue7);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to delete a user from users.xml
     * 
     * @param element
     *        - Name of the element to be deleted
     * @param attrName
     *        - Atrribute name of the element to be deleted
     * @param attrValue8
     *        - Atrribute value of the element to be deleted
     */
    public void deleteUserFromUserXml8(String element, String attrName, String attrValue8) {
        String message = XMLUtil.deleteElement(userXmlFilePath, element, attrName, attrValue8);
        Assert.assertEquals(message, SUCCESS_MESSAGE);
    }

    /**
     * Test method to create group and add users to a created group
     * 
     * @param groupDescription1
     *        - description for group to be created
     * @param groupName1
     *        - name of the group
     * @param user1
     *        - users to be added for the group created
     * @param user2
     *        - users to be added for the group created
     */
    public void addMultipleUserTOGroupInUsersXML1(String groupDescription1, String groupName1,
        String user1, String user2) {
        String message =
            XMLUtil.createGroupAddTwoUsersInUsersXML(userXmlFilePath, groupDescription1,
                groupName1, user1, user2);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create group and add users to a created group
     * 
     * @param description1
     *        - description for group to be created
     * @param userGroup1
     *        - name of the group
     * @param user1
     *        - users to be added for the group created
     */
    public void addUserTOGroupInUsersXML1(String description1, String userGroup1, String user1) {
        String message =
            XMLUtil.createGroupAddMultipleUsersInUsersXML(userXmlFilePath, description1,
                userGroup1, user1);
        // harvestWait(60);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create group and add users to a created group
     * 
     * @param testDescription1
     *        - description for group to be created
     * @param testUserGroup1
     *        - name of the group
     * @param testUser1
     *        - users to be added for the group created
     */
    public void addUserTOGroupInTestUsersXML1(String testDescription1, String testUserGroup1,
        String testUser1) {
        String message =
            XMLUtil.createGroupAddMultipleUsersInUsersXML(testUserXmlFilePath, testDescription1,
                testUserGroup1, testUser1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create group and add users to a created group
     * 
     * @param description2
     *        - description for group to be created
     * @param userGroup2
     *        - name of the group
     * @param user2
     *        - users to be added for the group created
     */
    public void addUserTOGroupInUsersXML2(String description2, String userGroup2, String user2) {
        String message =
            XMLUtil.createGroupAddMultipleUsersInUsersXML(userXmlFilePath, description2,
                userGroup2, user2);
        // harvestWait(60);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create group and add users to a created group
     * 
     * @param description3
     *        - description for group to be created
     * @param userGroup3
     *        - name of the group
     * @param user3
     *        - users to be added for the group created
     */
    public void addUserTOGroupInUsersXML3(String description3, String userGroup3, String user3) {
        String message =
            XMLUtil.createGroupAddMultipleUsersInUsersXML(userXmlFilePath, description3,
                userGroup3, user3);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to grant permission for a specified group on domains.xml
     * 
     * @param groupName1
     *        - name of the group
     * @param permissionLevel1
     *        - permission for the group specified
     */
    public void createPermission1(String groupName1, String permissionLevel1) {
        String message =
            XMLUtil.createGroupGrantForSuperDomain(domainXmlPath, groupName1, permissionLevel1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create a permission for a group in server.xml
     * 
     * @param groupGrantName1
     *        - name of the group
     * @param permissionGrantLevel1
     *        - Permission to the group
     * @param groupElementName
     *        - name of the element under which permission has tobe added
     */
    public void createGroupGrantinServerXml1(String groupElementName, String groupGrantName1,
        String permissionGrantLevel1) {
        String message =
            XMLUtil.createGroupGrantForElement(serverXmlFilePath, groupElementName,
                groupGrantName1, permissionGrantLevel1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }


   
    /**
     * Test method to create property and value tag under existing realms node.
     * It is a one time creation at the time of startup.
     * 
     * @param propElementName
     *        - Name of the element to be created
     * @param elementText
     *        - text value
     * @param propParentNode
     *        - parent node element
     * @param propAtrributeName
     *        - Parent attribute name of the element to be created
     * @param propParentAttrValue
     *        - Parent attribute value of the element to be created
     * @param propertyName
     *        - attribute name of the element to be created
     * @param propertyValue
     *        - attribute value of the element to be created
     */
    public void addPropToRealmsXml1(String propElementName, String elementText,
        String propParentNode, String propAtrributeName, String propParentAttrValue,
        String propertyName, String propertyValue) {

        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(propertyName, propertyValue);

        String message =
            XMLUtil.createElement(realmsXmlFilePath, propElementName, elementText, propParentNode,
                propAtrributeName, propParentAttrValue, attributeMap);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create property and value tag under existing realms node.
     * It is a one time creation at the time of startup.
     * 
     * @param valueElementName
     *        - Name of the element to be created
     * @param elementText
     *        - elementText value
     * @param valueParentNode
     *        - Parent node of the element to be created
     * @param valueAtrributeName
     *        - attribute name of the element to be created
     * @param valueParentAttrValue
     *        - attribute value of the element to be created
     * @param valueName
     *        - attribute name of the element to be created
     * @param valuePropValue
     *        - attribute value of the element to be created
     */
    public void addPropToRealmsXml2(String valueElementName, String elementText,
        String valueParentNode, String valueAtrributeName, String valueParentAttrValue,
        String valueName, String valuePropValue) {

        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(valueName, valuePropValue);

        String message =
            XMLUtil.createElementWhennoChild(realmsXmlFilePath, valueElementName, elementText,
                valueParentNode, valueAtrributeName, valueParentAttrValue, attributeMap);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create property and value tag under existing realms node.
     * It is a one time creation at the time of startup.
     * 
     * @param propElementName1
     *        - Name of the element to be created
     * @param elementText
     *        - elementText value
     * @param propParentNode1
     *        - Parent node of the element to be created
     * @param propAtrributeName1
     *        - attribute name of the element to be created
     * @param propParentAttrValue1
     *        - attribute value of the element to be created
     * @param propertyName1
     *        - attribute name of the element to be created
     * @param propertyValue1
     *        - attribute value of the element to be created
     */
    public void addPropToRealmsXml3(String propElementName1, String elementText,
        String propParentNode1, String propAtrributeName1, String propParentAttrValue1,
        String propertyName1, String propertyValue1) {

        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(propertyName1, propertyValue1);

        String message =
            XMLUtil.createElement(realmsXmlFilePath, propElementName1, elementText,
                propParentNode1, propAtrributeName1, propParentAttrValue1, attributeMap);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create property and value tag under existing realms node.
     * It is a one time creation at the time of startup.
     * 
     * @param valueElementName1
     *        - Name of the element to be created
     * @param elementText
     *        - elementText value
     * @param valueParentNode1
     *        - Parent node of the element to be created
     * @param valueAtrributeName1
     *        - attribute name of the element to be created
     * @param valueParentAttrValue1
     *        - attribute value of the element to be created
     * @param valueName1
     *        - attribute name of the element to be created
     * @param valuePropValue1
     *        - attribute value of the element to be created
     */
    public void addPropToRealmsXml4(String valueElementName1, String elementText,
        String valueParentNode1, String valueAtrributeName1, String valueParentAttrValue1,
        String valueName1, String valuePropValue1) {

        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(valueName1, valuePropValue1);

        String message =
            XMLUtil.createElement(realmsXmlFilePath, valueElementName1, elementText,
                valueParentNode1, valueAtrributeName1, valueParentAttrValue1, attributeMap);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value on domains.xml
     * 
     * @param domainElementName
     *        - name of the element
     * @param domainAttrName
     *        - name of the attribute to be changed
     * @param domainAttrOldValue
     *        - old value
     * @param domainAttrNewValue
     *        - new value to be replaced
     */
    public void changeAttributeValue(String domainElementName, String domainAttrName,
        String domainAttrOldValue, String domainAttrNewValue) {

        String message =
            XMLUtil.changeAttributeValue(domainXmlPath, domainElementName, domainAttrName,
                domainAttrOldValue, domainAttrNewValue);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }


    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userNamePermission
     *        - WS Username
     * @param password
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn
     *        - to check if user has loggedin or not
     * @param logMessage
     *        - to check messages
     * @param basicMetric3
     *        - metric to check user login
     */
    public void createCLWBeanPermission(String emhost, String userNamePermission, String password,
        String emport, String loggedIn, String logMessage, String basicMetric3) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userNamePermission + " empassw: " + password + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw =
            new CLWBean(emhost, userNamePermission, password, Integer.parseInt(emport),
                clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric3);
        System.out.println("Metrics Exists:" + metricsExists);
        boolean permission = ApmbaseUtil.checkInvestigatorTree(clw, logMessage, emLogFile);
        System.out.println("Permission:" + permission);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn));
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName1
     *        - WS Username
     * @param password1
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn1
     *        - to check if user has loggedin or not
     * @param logMessage
     *        - to check messages
     * @param metric1
     *        - metric to check user login
     */
    public void createCLWBeanPermission1(String emhost, String userName1, String password1,
        String emport, String loggedIn1, String logMessage, String metric1) {
        LOGGER.info("CLW object parameters: *** emhost: " + emhost + " userName: " + userName1
            + " empassw: " + password1 + " emport: " + emport + " Location CLW Jar file: "
            + clwJarFileLoc);

        clw = new CLWBean(emhost, userName1, password1, Integer.parseInt(emport), clwJarFileLoc);
        LOGGER.debug("CLW Bean created: " + clw);
        int i = 0;
        boolean metricsExists = false, permission = false;
        while (i < 20) {
            metricsExists = checkBasicMetricsExists1(clw, metric1);
            if (metricsExists) {
                LOGGER.info("Metrics Exists: " + metricsExists);
                break;
            } else {
                i++;
                harvestWait(60);
            }
        }
        while (i < 20) {
            permission = ApmbaseUtil.checkInvestigatorTree(clw, logMessage, emLogFile);
            if (permission) {
                LOGGER.info("Permission: " + permission);
                break;
            } else {
                i++;
                harvestWait(60);
            }

        }
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn1));

    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName2
     *        - WS Username
     * @param password2
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn2
     *        - to check if user has loggedin or not
     * @param logMessage
     *        - to check messages
     * @param metric2
     *        - metric to check user login
     */
    public void createCLWBeanPermission2(String emhost, String userName2, String password2,
        String emport, String loggedIn2, String logMessage, String metric2) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName2 + " empassw: " + password2 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName2, password2, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists = checkBasicMetricsExists1(clw, metric2);
        System.out.println("Metrics Exists:" + metricsExists);
        boolean permission = ApmbaseUtil.checkInvestigatorTree(clw, logMessage, emLogFile);
        System.out.println("Permission:" + permission);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn2));
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param agentUserName
     *        - WS Username
     * @param agentPassword
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param agentLoggedIn
     *        - to check if user has loggedin or not
     * @param logMessage
     *        - to check messages
     */
    public void createCLWBeanForCheckingAgents(String emhost, String agentUserName,
        String agentPassword, String emport, String agentLoggedIn, String logMessage) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + agentUserName + " empassw: " + agentPassword + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw =
            new CLWBean(emhost, agentUserName, agentPassword, Integer.parseInt(emport),
                clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists = ApmbaseUtil.checkListAgentsQuery(clw, logMessage, emLogFile);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(agentLoggedIn));
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param agentUserName1
     *        - WS Username
     * @param agentPassword1
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param agentLoggedIn1
     *        - to check if user has loggedin or not
     * @param logMessage
     *        - to check messages
     */
    public void createCLWBeanForCheckingAgents1(String emhost, String agentUserName1,
        String agentPassword1, String emport, String agentLoggedIn1, String logMessage1) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + agentUserName1 + " empassw: " + agentPassword1 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw =
            new CLWBean(emhost, agentUserName1, agentPassword1, Integer.parseInt(emport),
                clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists = ApmbaseUtil.checkListAgentsQuery(clw, logMessage1, emLogFile);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(agentLoggedIn1));
    }

    /**
     * This method checks if the metric exists for the passed metric value
     * 
     * @param clwBean
     *        - CLWBean to log in
     * @param basicMetricValue
     *        - basic metric to check user log in
     * @return metricExist - boolean(true/false) to check if metric exists
     */
    private boolean checkBasicMetricsExists1(CLWBean clwBean, String basicMetricValue) {
        boolean metricExist = false;
        try {
            harvestWait(45);
            LOGGER.info("basicMetricValue: " + basicMetricValue);
            LOGGER.debug("clwBean: " + clwBean);
            MetricUtil metricutil = new MetricUtil(basicMetricValue, clwBean);
            metricExist = metricutil.metricExists();
            LOGGER.info("checkBasicMetricsExists1--metricExist: " + metricExist);
        } catch (Exception e) {
            LOGGER.error("Exception: " + e.getMessage());
        }
        return metricExist;
    }

    public void deleteUserAdminPermission(String elementName1, String attrName1, String attrValue1) {
        LOGGER
            .info("TEST CASE com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.deleteUserAdminPermission(String elementName1, String attrName1, String attrValue1):");

        LOGGER.info("elementName1: " + elementName1);
        LOGGER.info("attrName1: " + attrName1);
        LOGGER.info("attrValue1: " + attrValue1);

        if (XMLUtil.containsElements(domainXmlPath, elementName1, attrName1, attrValue1)) {
            String message =
                XMLUtil.deleteElement(domainXmlPath, elementName1, attrName1, attrValue1);
            LOGGER.info("Expected result message: " + SUCCESS_MESSAGE);
            LOGGER.info("Real result message: " + message);
            Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
            Assert.assertFalse(XMLUtil.containsElements(domainXmlPath, elementName1, attrName1,
                attrValue1));
        } else {
            LOGGER.info("No elements matching input criteria found, skipping this test case.");
        }
    }

    /**
     * Test method to delete a permission for a specified group on domains.xml
     * 
     * @param elementPermissionName
     *        - Name of the element to be deleted
     * @param attrPermissionName1
     *        - Atrribute Name of the element to be deleted
     * @param attrPermissionValue1
     *        - Attribute Value of the element to be deleted
     */
    public void deletePermission1(String elementPermissionName, String attrPermissionName1,
        String attrPermissionValue1) {
        String message =
            XMLUtil.deleteElement(domainXmlPath, elementPermissionName, attrPermissionName1,
                attrPermissionValue1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in users.xml
     * 
     * @param userElementName1
     *        - name of the element
     * @param userAttrName1
     *        - attribute name to be modified
     * @param userAttrOldValue1
     *        - Old Value
     * @param userAttrNewValue1
     *        - New Value
     */
    public void changeAttributeInXml1(String userElementName1, String userAttrName1,
        String userAttrOldValue1, String userAttrNewValue1) {
        String message =
            XMLUtil.changeAttributeValue(userXmlFilePath, userElementName1, userAttrName1,
                userAttrOldValue1, userAttrNewValue1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in domains.xml
     * 
     * @param userElementName2
     *        - name of the element
     * @param userAttrName2
     *        - attribute name to be modified
     * @param userAttrOldValue2
     *        - Old Value
     * @param userAttrNewValue2
     *        - New Value
     */
    public void changeAttributeInXml2(String userElementName2, String userAttrName2,
        String userAttrOldValue2, String userAttrNewValue2) {
        String message =
            XMLUtil.changeAttributeValue(domainXmlPath, userElementName2, userAttrName2,
                userAttrOldValue2, userAttrNewValue2);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in realms.xml
     * 
     * @param realmElementName
     *        - name of the element
     * @param realmAttrName
     *        - attribute name to be modified
     * @param realmAttrOldValue
     *        - Old Value
     * @param realmAttrNewValue
     *        - New Value
     */
    public void changeAttributeInRealmsXml(String realmElementName, String realmAttrName,
        String realmAttrOldValue, String realmAttrNewValue) {
        String message =
            XMLUtil.changeAttributeValue(realmsXmlFilePath, realmElementName, realmAttrName,
                realmAttrOldValue, realmAttrNewValue);

        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in realms.xml
     * 
     * @param realmElementName1
     *        - name of the element
     * @param realmAttrName1
     *        - attribute name to be modified
     * @param realmAttrOldValue1
     *        - Old Value
     * @param realmAttrNewValue1
     *        - New Value
     */
    public void changeAttributeInRealmsXml1(String realmElementName1, String realmAttrName1,
        String realmAttrOldValue1, String realmAttrNewValue1) {
        String message =
            XMLUtil.changeAttributeValue(realmsXmlFilePath, realmElementName1, realmAttrName1,
                realmAttrOldValue1, realmAttrNewValue1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create a domain with multiple groups
     * 
     * @param groupDomainName2
     *        - domain name
     * @param groupDescription2
     *        - description of domain
     * @param mapping2
     *        - agent mapping
     * @param domainGroupName11
     *        - group name
     * @param domainGroupPermission11
     *        - permission for the group
     * @param domainGroupName21
     *        - group name
     * @param domainGroupPermission21
     *        - permission for the group
     */
    public void createDomainWithMultipleGroups2(String groupDomainName2, String groupDescription2,
        String mapping2, String domainGroupName11, String domainGroupPermission11,
        String domainGroupName21, String domainGroupPermission21) {

        Map<String, String> groupMap = new HashMap<String, String>();
        groupMap.put(domainGroupName11, domainGroupPermission11);
        groupMap.put(domainGroupName21, domainGroupPermission21);

        XMLUtil.createDomain(domainXmlPath, groupDomainName2, groupDescription2, mapping2, null,
            groupMap);
    }

    /**
     * Test method to create a domain with multiple groups
     * 
     * @param groupDomainName3
     *        - domain name
     * @param groupDescription3
     *        - description of domain
     * @param mapping3
     *        - agent mapping
     * @param domainGroupName31
     *        - group name
     * @param domainGroupPermission31
     *        - group permission
     * @param domainGroupName32
     *        -group name
     * @param domainGroupPermission32
     *        - group permission
     */
    public void createDomainWithMultipleGroups3(String groupDomainName3, String groupDescription3,
        String mapping3, String domainGroupName31, String domainGroupPermission31,
        String domainGroupName32, String domainGroupPermission32) {

        Map<String, String> groupMap = new HashMap<String, String>();
        groupMap.put(domainGroupName31, domainGroupPermission31);
        groupMap.put(domainGroupName32, domainGroupPermission32);

        XMLUtil.createDomain(domainXmlPath, groupDomainName3, groupDescription3, mapping3, null,
            groupMap);
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName3
     *        - WS Username
     * @param password3
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn3
     *        - to check if user has loggedin or not
     * @param logMessage3
     *        - to check messages
     * @param basicMetric5
     *        - basic metric to check user login
     */
    public void createCLWBeanPermission3(String emhost, String userName3, String password3,
        String emport, String loggedIn3, String logMessage3, String basicMetric5) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName3 + " empassw: " + password3 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName3, password3, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric5);
        System.out.println("Metrics Exists:" + metricsExists);
        boolean permission = ApmbaseUtil.checkInvestigatorTree(clw, logMessage3, emLogFile);
        System.out.println("Permission:" + permission);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn3));
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName33
     *        - WS Username
     * @param password33
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn33
     *        - to check if user has loggedin or not
     * @param logMessage33
     *        - to check messages
     * @param metric33
     *        - basic metric to check user login
     */
    public void createCLWBeanPermission33(String emhost, String userName33, String password33,
        String emport, String loggedIn33, String logMessage33, String metric33) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName33 + " empassw: " + password33 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName33, password33, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists = checkBasicMetricsExists1(clw, metric33);
        System.out.println("Metrics Exists:" + metricsExists);
        boolean permission = ApmbaseUtil.checkInvestigatorTree(clw, logMessage33, emLogFile);
        System.out.println("Permission:" + permission);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn33));
    }



    public void checkEMLog(String logFileName, String logMessage, String messageFound) {
        String emLogMessages[] = logMessage.split(",");
        try {
            for (int i = 0; i < emLogMessages.length; i++) {
                int result = ApmbaseUtil.checklog(logFileName, emLogFile, emLogMessages[i]);
                Assert.assertEquals(result, Integer.parseInt(messageFound));
            }
        } catch (Exception e) {
            Assert.fail("Exception while checking log message.");
        }
    }

    /**
     * Test method to grant permission for a specified user on domains.xml
     * 
     * @param customDomainName
     *        - domain name
     * @param customUserName
     *        - name of the user
     * @param customPermissionLevel
     *        - permission level for the domain
     */
    public void createPermissionForCustomDomain(String customDomainName, String customUserName,
        String customPermissionLevel) {
        String message =
            XMLUtil.createUserGrantElementForCustomDomain(domainXmlPath, customDomainName,
                customUserName, customPermissionLevel);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in domains.xml
     * 
     * @param userElementName
     *        - name of the element
     * @param userParentNodeName
     *        - name of the parent node
     * @param userAttrName
     *        - attribute name to be modified
     * @param userAttrOldValue
     *        - Old Value
     * @param userAttrNewValue
     *        - New Value
     */
    public void changeAttributeInXml3(String userElementName, String userParentNodeName,
        String userAttrName, String userAttrOldValue, String userAttrNewValue) {
        String message =
            XMLUtil.changeAttributeValueWithparentNode(domainXmlPath, userElementName,
                userParentNodeName, userAttrName, userAttrOldValue, userAttrNewValue);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName4
     *        - WS Username
     * @param password4
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn4
     *        - to check if user has loggedin or not
     * @param logMessage4
     *        - to check messages
     */
    public void createCLWBeanPermission4(String emhost, String userName4, String password4,
        String emport, String loggedIn4, String logMessage4) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName4 + " empassw: " + password4 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName4, password4, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean permission = ApmbaseUtil.checkTranscationTraces(clw, logMessage4);
        System.out.println("Permission:" + permission);
        Assert.assertEquals(permission, Boolean.parseBoolean(loggedIn4));
    }


    /**
     * This method checks if the metric exists for the passed metric value
     * 
     * @param clwBean
     * @param basicMetricValue
     * @return
     */
    private boolean checkAgentMetricsExists(String emhost, String emport, String user_name,
        String user_pass, String agentName) {
        boolean metricExist = false;
        try {
            harvestWait(45);
            metricExist = runGCHeapMetric(emhost, emport, user_name, user_pass, agentName);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricExist;
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param user_name
     *        - WS Username
     * @param user_password
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn6
     *        - to check if user has loggedin or not
     * @param agentName
     *        - name of the agent
     */
    public void checkLoginWS(String emhost, String user_name, String user_password, String emport,
        String loggedIn6, String agentName) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + user_name + " empassw: " + user_password + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw =
            new CLWBean(emhost, user_name, user_password, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists =
            checkAgentMetricsExists(emhost, emport, user_name, user_password, agentName);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn6));
    }

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param user_name1
     *        - WS Username
     * @param user_password1
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn7
     *        - to check if user has loggedin or not
     * @param agentName
     *        - name of the agent
     */
    public void checkLoginWS1(String emhost, String user_name1, String user_password1,
        String emport, String loggedIn7, String agentName) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + user_name1 + " empassw: " + user_password1 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw =
            new CLWBean(emhost, user_name1, user_password1, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);

        boolean metricsExists =
            checkAgentMetricsExists(emhost, emport, user_name1, user_password1, agentName);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn7));
    }

    /**
     * This method is to check the list of messages against the messages
     * generated on Console. This method is generalised for all other test
     * cases.
     * 
     * @param command
     * @param dir
     * @return
     * @throws Exception
     */
    public static int executeCommand(String[] command, File dir) throws Exception {
        int found = 0;
        Process process = null;
        List<String> clwOutputStrings = new ArrayList<String>();

        try {
            System.out.println("In Execute method " + command.length);
            System.out.println("Command to Execute " + command.toString());
            ProcessBuilder processbuilder = new ProcessBuilder(command);
            processbuilder.directory(dir);
            processbuilder.redirectErrorStream(true);
            process = processbuilder.start();
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                clwOutputStrings.add(line);
                System.out.println("Output of Command :" + line);
            }
            for (int j = 0; j < clwOutputStrings.size(); j++) {
                if (!clwOutputStrings.get(j).startsWith(
                    "Domain, Host, Process, AgentName, Resource, MetricName, Record Type,")) {
                    found = 1;
                }
            }
            return found;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;

        } finally {
            if (process != null) {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
            }
        }
    }

    /**
     * Test method to create CLWBean,check for permissions
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userNamePermission
     *        - WS Username
     * @param password
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn
     *        - to check if user has loggedin or not
     * @param logMessage
     *        - to check messages
     */
    public void checkLoginPermission(String emhost, String userNamePermission, String password,
        String emport, String loggedIn, String logMessage) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userNamePermission + " empassw: " + password + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw =
            new CLWBean(emhost, userNamePermission, password, Integer.parseInt(emport),
                clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(20);
        boolean permission = ApmbaseUtil.checkTranscationTraces(clw, logMessage);
        System.out.println("Permission:" + permission);
        Assert.assertEquals(permission, Boolean.parseBoolean(loggedIn));
    }

    /**
     * Test method to create user to admin group in users.xml
     * 
     * @param adminUserName
     *        - name for the user to be added to "Admin" group
     */
    public void addUserToAdminGroup(String adminUserName) {
        String message = XMLUtil.addUserToAdminGroup(userXmlFilePath, adminUserName);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create user to admin group in users.xml
     * 
     * @param adminUserName
     *        - name for the user to be added to "Admin" group
     */
    public void addUserToAdminGroup1(String adminUserName1) {
        String message = XMLUtil.addUserToAdminGroup(userXmlFilePath, adminUserName1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to change attribute value in users.xml
     * 
     * @param userElementName1
     *        - name of the element
     * @param userAttrName1
     *        - attribute name to be modified
     * @param userAttrOldValue1
     *        - Old Value
     * @param userAttrNewValue1
     *        - New Value
     */
    public void changeAttributeInXml4(String userElementName4, String userAttrName4,
        String userAttrOldValue4, String userAttrNewValue4) {
        String message =
            XMLUtil.changeAttributeValue(userXmlFilePath, userElementName4, userAttrName4,
                userAttrOldValue4, userAttrNewValue4);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Common method to check if metrics exists
     * 
     * @param clwBean
     *        - CLWBean created when user logs into WS
     */
    public static boolean checkBasicMetricsExists(CLWBean clw, String basicMetric) {
        LOGGER
            .info("Entering com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.checkBasicMetricsExists(CLWBean, String)...");

        try {
            Thread.sleep(30);
            return ApmbaseUtil.checkMetricExists(null, null, null, ".*Custom Metric Agent.*",
                ".*Enterprise Manager:Host", clw);
        } catch (Exception e) {
            System.out.println("Problems while running Basic metric" + e.getMessage());
        } finally {
            LOGGER
                .info("Leaving com.ca.apm.automation.domainpermissions_1.DomainPermissionManagementTest_1.checkBasicMetricsExists(CLWBean, String)...");
        }


        return false;
    }

    /**
     * Test method to change attribute value in domains.xml
     * 
     * @param userElementName
     *        - name of the element
     * @param userParentNodeName
     *        - name of the parent node
     * @param userAttrName
     *        - attribute name to be modified
     * @param userAttrOldValue
     *        - Old Value
     * @param userAttrNewValue
     *        - New Value
     */
    public void changeAttributeInXml5(String userElementName1, String userParentNodeName1,
        String userAttrName1, String userAttrOldValue1, String userAttrNewValue1) {
        String message =
            XMLUtil.changeAttributeValueWithparentNode(domainXmlPath, userElementName1,
                userParentNodeName1, userAttrName1, userAttrOldValue1, userAttrNewValue1);
        harvestWait(20);
        Assert.assertTrue(message.equals(SUCCESS_MESSAGE));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName6
     *        - WS Username
     * @param password6
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn6
     *        - to check if user has loggedin or not
     * @param basicMetric6
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean6(String emhost, String userName6, String password6, String emport,
        String loggedIn6, String basicMetric6) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName6 + " empassw: " + password6 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName6, password6, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(60);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric6);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn6));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName7
     *        - WS Username
     * @param password7
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn7
     *        - to check if user has loggedin or not
     * @param basicMetric7
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean7(String emhost, String userName7, String password7, String emport,
        String loggedIn7, String basicMetric7) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName7 + " empassw: " + password7 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName7, password7, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(20);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric7);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn7));
    }

    /**
     * Test method to create CLWBean and check if metrics exists
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName8
     *        - WS Username
     * @param password8
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn8
     *        - to check if user has loggedin or not
     * @param basicMetric8
     *        - custom metric to check if user loggedin or not
     */
    public void createCLWBean8(String emhost, String userName8, String password8, String emport,
        String loggedIn8, String basicMetric8) {
        System.out.println(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName8 + " empassw: " + password8 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName8, password8, Integer.parseInt(emport), clwJarFileLoc);
        System.out.println("CLW Bean created:" + clw);
        harvestWait(20);
        boolean metricsExists = ApmbaseUtil.checkBasicMetricsExists(clw, basicMetric8);
        System.out.println("Metrics Exists:" + metricsExists);
        Assert.assertEquals(metricsExists, Boolean.parseBoolean(loggedIn8));
    }


    /**
     **********************************************************************************
     *
     * Permissions1 methods end here
     * 
     **********************************************************************************
     */

    /**
     * Test method to create CLWBean,check if metrics exists and check for
     * permission
     * 
     * @param emhost
     *        - Hostname on which EM is setup
     * @param userName1
     *        - WS Username
     * @param password1
     *        - WS password
     * @param emport
     *        - PortNumber on which EM is setup
     * @param loggedIn1
     *        - to check if user has loggedin or not
     * @param logMessage1
     *        - to check messages
     */
    public void createCLWBeanPermission(String emhost, String userName1, String password1,
        String emport, String loggedIn1, String logMessage1) {
        LOGGER.info("Start of createCLWBeanPermission method");
        LOGGER.info(" ***** CLW object parameters: *** emhost: " + emhost + " userName: "
            + userName1 + " empassw: " + password1 + "emport:" + emport
            + " Location CLW Jar file: " + clwJarFileLoc);

        clw = new CLWBean(emhost, userName1, password1, Integer.parseInt(emport), clwJarFileLoc);
        LOGGER.info("CLW Bean created:" + clw);
        harvestWait(60);
        boolean permission =
            ApmbaseUtil.checkTranscationTraces("admin", "", "", emhost, 5001, emLibDir, "error");;
        LOGGER.info("Permission:" + permission);

        if (permission == Boolean.parseBoolean(loggedIn1)) {
            LOGGER
                .info("Logged in to workstation with specified used and user has specified permission");
        } else {
            LOGGER.info("User doesnot have specified permission");
        }
        LOGGER.info("End of createCLWBeanPermission method");
        Assert.assertEquals(permission, Boolean.parseBoolean(loggedIn1));
    }

    public void createCLWBeanPermission1(String emhost, String emuser, String password1,
        String emport, String loggedIn1, String logMessage1) {
        LOGGER.info("Start of createCLWBeanPermission method");
        LOGGER.info(" ***** CLW object parameters: *** emhost: " + emhost + " userName: " + emuser
            + " empassw: " + password1 + "emport:" + emport + " Location CLW Jar file: "
            + clwJarFileLoc);

        clw = new CLWBean(emhost, emuser, password1, Integer.parseInt(emport), clwJarFileLoc);
        LOGGER.info("CLW Bean created:" + clw);
        harvestWait(60);

        LOGGER.info("End of createCLWBeanPermission method");
        // Assert.assertTrue(true);
    }

    /**
     * Test method for test case start
     * 
     * @param testCaseNameIDPath
     *        -Test plan path for the test case
     */

    public void testCaseStart(String testCaseNameIDPath) {
        LOGGER.info("----------" + testCaseNameIDPath + "-----" + "Start");
    }

    /**
     * Test method for test case end -Test plan path for the test case
     * 
     * @param testCaseNameIDPath
     */
    public void testCaseEnd(String testCaseNameIDPath) {
        LOGGER.info("----------" + testCaseNameIDPath + "-----" + "End");
    }

    
    private void waitForAgentNodes() {
        final String tomcatNodeString = tomcatAgentHost + "|Tomcat|Tomcat Agent";
        String value;
        int i = 0;
        List<String> nodeList;
        int count = 0;
        for (i = 0; i < 20; i++) {
            nodeList =
                clwCommon
                    .getNodeList("Admin", "", ".*", emhost, Integer.parseInt(emport), emLibDir);
            if(!nodeList.isEmpty())
            {
            Iterator<String> nodeListIterator = nodeList.iterator();
            while (nodeListIterator.hasNext()) {
                value = nodeListIterator.next();
                if (value.equalsIgnoreCase(tomcatNodeString))
                    count++;
            }
            if (count >= 1)
                break;
            }
            else {
                count = 0;
                harvestWait(60);
            }
        }
        if (i == 20) Assert.assertTrue(false);
    }
}
