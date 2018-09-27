package com.ca.apm.tests.securityauthentication.sslsupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.XMLModifierFlow;
import com.ca.apm.commons.flow.XMLModifierFlowContext;
import com.ca.apm.tests.base.OneCollectorOneTomcatTestsBase;

public class SslSupportTests extends OneCollectorOneTomcatTestsBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SslSupportTests.class);
    List<String> hostNames = new ArrayList<String>();
    protected final String emSSLPort;
    protected final String emCustomHTTPSPort;
    protected final String emCustomHTTPPort;
    protected String testcaseId;
    protected String emJettyFileC1;
    protected String emJettyFileC1_backup;
    protected String emJettyFileMom;
    protected String emJettyFileMom_backup;
    protected String newEmJettyLocation;
    protected String OsBasednewEmJettyLocation;
    protected String emConfigDir;
    protected String emConfigCustomDirLocation;
    protected String emLaxFile;
    protected String emLogFile;
    CLWCommons clw;
    TestUtils testUtils;
    List<String> connectionMessage;
    List<String> roleIds;
    List<String> machines;

    public SslSupportTests() {

        roleIds = new ArrayList<String>();
        connectionMessage = new ArrayList<String>();
        machines = new ArrayList<String>();
        clw = new CLWCommons();
        testUtils = new TestUtils();

        emCustomHTTPSPort = "9444";
        emCustomHTTPPort = "9081";

        emJettyFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/em-jetty-config.xml";
        emJettyFileMom_backup = emJettyFileMom + "_backup";

        newEmJettyLocation =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/shutoff/em-jetty-config.xml";

        boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS);

        OsBasednewEmJettyLocation =
            isWindows ? "c:/em-jetty-config.xml" : "/root/em-jetty-config.xml";
        emConfigCustomDirLocation = isWindows ? "c:/config/" : "/root/config/";

        emJettyFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR) + "/em-jetty-config.xml";

        emJettyFileC1_backup = emJettyFileC1 + "_backup";

        emLogFile =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_LOG_FILE);
        emSSLPort = ApmbaseConstants.emSSLPort;

        emConfigDir =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR);

        emLaxFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/Introscope_Enterprise_Manager.lax";

    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        super.initialize();
        setLoadBalancingPropValues(MOM_ROLE_ID);
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 1)
    public void verify_ALM_310236_VerifyHostnamesTrueNoValidcertificate() {
        String testCaseId = "310236";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            updateEMJettyVerifyHostName(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 2)
    public void verify_ALM_310237_ValidateCertificatesTrueNoValidcertificate() {
        String testCaseId = "310237";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            updateEMJettyValidCertificate(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            hostNames.clear();
            hostNames.add(momHost);
            hostNames.add(collector1Host);
            checkAgentFailedToConnect(hostNames, tomcatAgentLogFile, TOMCAT_MACHINE_ID);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 3)
    public void verify_ALM_310238_VerifyHostnamesTrueValidcertificate() {
        String testCaseId = "310238";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            updateEMJettyVerifyHostName(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            List<String> agents = new ArrayList<String>();
            agents.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(agents);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 4)
    public void verify_ALM_310239_ValidateCertificatesTrueValidcertificate() {
        String testCaseId = "310239";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            updateEMJettyVerifyHostName(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            List<String> agents = new ArrayList<String>();
            agents.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(agents);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 5)
    public void verify_ALM_310240_NeedClientAuthenticationJettyConfiguredWithoutKeyStore() {
        String testCaseId = "310240";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            updateEMJettyNeedClientAuth(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            hostNames.clear();
            hostNames.add(momHost);
            hostNames.add(collector1Host);
            checkAgentFailedToConnect(hostNames, tomcatAgentLogFile, TOMCAT_MACHINE_ID);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 6)
    public void verify_ALM_310241_NeedClientAuthenticationEMConfiguredWithoutKeyStore() {
        String testCaseId = "310241";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 7)
    public void verify_ALM_310242_NeedClientAuthenticationJettyConfiguredWithKeyStore() {
        String testCaseId = "310242";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            updateEMJettyVerifyHostName(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            List<String> agents = new ArrayList<String>();
            agents.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(agents);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 8)
    public void verify_ALM_310243_NeedClientAuthenticationEMConfiguredWithKeyStore() {
        String testCaseId = "310243";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            updateEMJettyDTD(roleIds);
            updateEMJettyVerifyHostName(roleIds);
            enableBothChannelsOnEM(roleIds);
            unCommentEMJettyEntryOnEM(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            List<String> agents = new ArrayList<String>();
            agents.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(agents);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 9)
    public void verify_ALM_310245_ConfigureTrustStoreNoValidcertificate() {
        String testCaseId = "310245";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            enableBothChannelsOnEM(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            unCommentEMJettyEntryOnEM(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            enableNeedClientAuthForEM(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            List<String> agents = new ArrayList<String>();
            agents.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(agents);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true, priority = 10)
    public void verify_ALM_310246_ConfigureTrustStoreValidcertificate() {
        String testCaseId = "310246";
        testCaseStart(testCaseId);
        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            enableBothChannelsOnEM(roleIds);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            unCommentEMJettyEntryOnEM(roleIds);
            enableHTTPSPropertiesForEM(roleIds);
            enableNeedClientAuthForEM(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            List<String> agents = new ArrayList<String>();
            agents.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(agents);
            startEMServices();
            checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
                momHost, momPort, emLibDir);
            LOGGER.info("Mom to Collector Connection Successful");
            startTomcatAgent(TOMCAT_ROLE_ID);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            testCaseEnd(testCaseId);
            stopServices();
            revertConfigAndRenameLogsWithTestId(testCaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true,priority = 11)
    public void verify_ALM_310225_Default_Jetty() {
        testcaseId = "310225";
        testCaseStart(testcaseId);

        try {
            LOGGER.info("EM HTTP Test Started....");
            Assert.assertTrue(enableACC("http", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            httpConnMessage(collector1Host);
            stopTestBed();
            LOGGER.info("EM HTTP Test Ended....");
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART1");

            backupConfigs();
            Assert.assertTrue(enableACC("https", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSecureWebPort), emLibDir);
            httpsConnMessage(collector1Host);
            stopTestBed();
            LOGGER.info("EM HTTPS Test Ended....");

            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART2");

            backupConfigs();
            Assert.assertTrue(enableACC("ssl", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSSLPort), emLibDir);
            sslConnMessage(collector1Host);
            LOGGER.info("EM SSL Test Ended....");
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"Deep"}, enabled = true,priority = 12)
    public void verify_ALM_310226_Change_HTTPS_Port() {
        testcaseId = "310226";
        testCaseStart(testcaseId);

        try {
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);

            updateEMPropertiesForHTTPS(roleIds, emCustomHTTPSPort);
            roleIds.clear();
            roleIds.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emCustomHTTPSPort);

            startTestBed();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
            LOGGER
                .info("Checking for HTTPs AGENT connection message in Agent log with Custom Port.....");

            connectionMessage.clear();
            connectionMessage
                .add(" Connected to "
                    + collector1Host
                    + ".ca.com"
                    + ":"
                    + emCustomHTTPSPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode");

            connectionMessage
                .add(" Connected to "
                    + collector1Host
                    + ":"
                    + emCustomHTTPSPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode");
            verifiesIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                connectionMessage);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);

        }
    }

    @Test(groups = {"FULL"}, enabled = true,priority = 13)
    public void verify_ALM_310227_HTTP_and_HTTPS_configuration() {
        testcaseId = "310227";
        try {
            testCaseStart(testcaseId);

            LOGGER.info("EM HTTP Test Started....");
            Assert.assertTrue(enableACC("http", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            httpConnMessage(collector1Host);
            stopServices();
            LOGGER.info("EM HTTP Test Ended....");

            revertConfigAndRenameLogsWithTestId(testcaseId + "PART_1");
            backupConfigs();
            Assert.assertTrue(enableACC("https", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSecureWebPort), emLibDir);
            httpsConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("MOM and Collectors Stopped succesfully");
            startEMServices();
            LOGGER.info("MOM and Collectors Started succesfully");

            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSecureWebPort), emLibDir);
            stopServices();
            LOGGER.info("EM HTTPS Test Ended....");

        } finally {
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }

    }

    @Test(groups = {"smoke"}, enabled = true,priority = 14)
    public void verify_ALM_310228_Change_HTTP_Port_in_EM_Properties_file() {
        testcaseId = "310228";
        try {
            testCaseStart(testcaseId);

            LOGGER
                .info("EM HTTP Test Started.... and deleting emjetty backup which already existis as addcustomHTTPEntryInEMJetty method is taking now....");
            deleteFile(emJettyFileMom_backup, MOM_MACHINE_ID);
            deleteFile(emJettyFileC1_backup, COLLECTOR1_MACHINE_ID);

            addCustomHTTPEntryInEMJetty(emJettyFileMom, MOM_MACHINE_ID, emCustomHTTPPort);
            addCustomHTTPEntryInEMJetty(emJettyFileC1, COLLECTOR1_MACHINE_ID, emCustomHTTPPort);

            replaceProp("introscope.enterprisemanager.webserver.port=8081",
                "introscope.enterprisemanager.webserver.port=" + emCustomHTTPPort, MOM_MACHINE_ID,
                configFileMom);
            replaceProp("introscope.enterprisemanager.webserver.port=8081",
                "introscope.enterprisemanager.webserver.port=" + emCustomHTTPPort,
                COLLECTOR1_MACHINE_ID, configFileC1);

            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emCustomHTTPPort);

            startTestBed();

            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emCustomHTTPPort), emLibDir);
            connectionMessage.clear();
            connectionMessage
                .add(" Connected to "
                    + collector1Host
                    + ":"
                    + emCustomHTTPPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");
            connectionMessage
                .add(" Connected to "
                    + collector1Host
                    + ".ca.com"
                    + ":"
                    + emCustomHTTPPort
                    + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");
            verifiesIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                connectionMessage);
            LOGGER.info("EM HTTP Test with Custom port is Ended....");

        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }

    }

    @Test(groups = {"smoke"}, enabled = true,priority = 15)
    public void verify_ALM_310229_OpenSSL_Communication_Channel_Port() {
        testcaseId = "310229";
        try {
            testCaseStart(testcaseId);
            Assert.assertTrue(enableACC("ssl", true));

            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSSLPort), emLibDir);

            sslConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("MOM collector Services are stopped....");
            startEMServices();
            LOGGER.info("MOM collector Services are Started....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSSLPort), emLibDir);
            stopAgent();
            LOGGER.info("Agent is stopped......");
            startAgent();
            LOGGER.info("Agent is started......");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSSLPort), emLibDir);
            LOGGER.info("Test Passed....");

        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);

        }
    }

    @Test(groups = {"smoke"}, enabled = true,priority = 16)
    public void verify_ALM_310230_Jetty_Syntax_Error() {
        testcaseId = "310230";

        try {
            backupConfigs();
            testCaseStart(testcaseId);
            Assert.assertTrue(enableACC("https", false));
            replaceProp("</Configure>", " ", MOM_MACHINE_ID, emJettyFileMom);
            startTestBed();
            checkAgentLogForMsg("Failed to connect to the Introscope Enterprise Manager at");
            LOGGER.info("Before doing http settings set back agent profile url to normal....");
            replaceProp("agentManager.url.1=https://" + momHost + ":" + emSecureWebPort,
                "agentManager.url.1=http://" + momHost + ":" + momWebPort, TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            replaceProp(
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                MOM_MACHINE_ID, configFileMom);
            restartEM(MOM_ROLE_ID);
            stopAgent();
            LOGGER.info("Stopped the Agent.....");
            startAgent();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            httpConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("MOM and collectors are stopped.....");
            startEMServices();
            LOGGER.info("MOM and Collectors started succesfully.....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);

            replaceProp("agentManager.url.1=https://" + momHost + ":" + emSecureWebPort,
                "agentManager.url.1=" + momHost + ":" + momPort, TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            stopAgent();
            LOGGER.info("Stopped the Agent second Time.....");
            startAgent();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);

            stopEMServices();
            LOGGER.info("MOM and collectors are stopped second Time.....");
            startEMServices();
            LOGGER.info("MOM and Collectors started succesfully second Time.....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }

    }

    @Test(groups = {"DEEP"}, enabled = true,priority = 17)
    public void verify_ALM_310231_Jetty_Config_file_missing() {
        testcaseId = "310231";
        try {
            LOGGER.info("This test case can be ignored it taking lot of time");
            testCaseStart(testcaseId);
            Assert.assertTrue(enableACC("https", false));
            renameFile(emJettyFileMom, emJettyFileMom + "_PART1", MOM_MACHINE_ID);
            LOGGER.info("EM JETTY file is renamed .....");
            startTestBed();
            checkAgentLogForMsg("Failed to connect to the Introscope Enterprise Manager at");
            stopAgent();
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + testcaseId + "_PART1",
                TOMCAT_MACHINE_ID);
            replaceProp("agentManager.url.1=https://" + momHost + ":" + emSecureWebPort,
                "agentManager.url.1=http://" + momHost + ":" + momWebPort, TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            startAgent();
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                "Lost contact with the Introscope Enterprise Manager at " + momHost + ":"
                    + momWebPort);
        }
        finally {
            stopTestBed();
            renameFile(emJettyFileMom + "_PART1", emJettyFileMom, MOM_MACHINE_ID);
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true,priority = 18)
    public void verify_ALM_310232_Jetty_config_on_relative_path() {
        testcaseId = "310232";

        try {
            testCaseStart(testcaseId);
            try {
                copyFile(emJettyFileMom, newEmJettyLocation, MOM_MACHINE_ID);
            } catch (Exception e) {
                LOGGER.info("Unable to copy the file ");
                Assert.assertTrue(false);
            }
            enableACC("https", false);
            replaceMoMProperty(
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=shutoff/em-jetty-config.xml");
            startTestBed();
            httpsConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("MOM services are stopped..");
            startEMServices();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
            stopTestBed();
            LOGGER.info("EM services stopped after first test finished....");
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART1");
            enableACC("http", true);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);

            stopEMServices();
            LOGGER.info("MOM services are stopped..");
            startEMServices();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"DEEP"}, enabled = true,priority = 19)
    public void verify_ALM_310233_Jetty_config_on_absolute_path() {
        testcaseId = "310233";
        try {
            testCaseStart(testcaseId);
            enableACC("https", false);
            try {
                copyFile(emJettyFileMom, OsBasednewEmJettyLocation, MOM_MACHINE_ID);
            } catch (Exception e) {
                LOGGER.info("Unable to copy the file ");
                Assert.assertTrue(false);
            }
            LOGGER.info("The new emjetty file path is..." + OsBasednewEmJettyLocation);
            replaceMoMProperty(
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile="
                    + OsBasednewEmJettyLocation);
            startTestBed();
            httpsConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("EM services are stopped....");
            startEMServices();
            LOGGER.info("Restarted EM services....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART1");
            LOGGER.info("Reverted the config files as first test is finished");
            enableACC("http", true);
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            stopEMServices();
            LOGGER.info("MOM services are stopped....");
            startEMServices();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"Full"}, enabled = true,priority = 25)
    public void verify_ALM_310234_HTTPS_with_Configurable_Config_Directory() {
        testcaseId = "310234";

        try {
            testCaseStart(testcaseId);
            Assert.assertTrue(enableACC("https", false));
            LOGGER.info("HTTPS configuration is done");
            startEMServices();
            stopEMServices();
            try {
                ApmbaseUtil.copyDirectory(new File(emConfigDir),
                    new File(emConfigCustomDirLocation));
            } catch (IOException e) {
                LOGGER.info("Unable to copy em config directory");
                Assert.assertTrue(false);
            }
            LOGGER.info("Copying the config dir work is done......" + emConfigCustomDirLocation);
            emConfigDirChange();
            LOGGER.info("Replaced the default em config path to custom path..");
            startTestBed();
            Assert.assertTrue(ApmbaseUtil.fileExists(emConfigCustomDirLocation + "data"));
            Assert.assertTrue(ApmbaseUtil.fileExists(emConfigCustomDirLocation + "traces"));
            Assert.assertTrue(ApmbaseUtil.fileExists(emConfigCustomDirLocation
                + "logs/IntroscopeEnterpriseManager.log"));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
            httpsConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("MOM collector services are stopped.....");
            startEMServices();
            LOGGER.info("MOM collector services restarted....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART1");
            LOGGER.info("Successfully finished first test....");
            Assert.assertTrue(enableACC("http", false));
            LOGGER.info("HTTP configuration is done");
            try {
                ApmbaseUtil.deleteDir(new File(emConfigCustomDirLocation));
            } catch (IOException e) {
                LOGGER.info("Unable topy the delete for second test");
                Assert.assertTrue(false);
            }
            LOGGER.info("succesfully deleted the directory");
            try {
                ApmbaseUtil.copyDirectory(new File(emConfigDir),
                    new File(emConfigCustomDirLocation));
            } catch (IOException e) {
                LOGGER.info("Unable topy the directory for second test");
                Assert.assertTrue(false);
            }
            emConfigDirChange();
            startTestBed();
            Assert.assertTrue(ApmbaseUtil.fileExists(emConfigCustomDirLocation + "data"));
            Assert.assertTrue(ApmbaseUtil.fileExists(emConfigCustomDirLocation + "traces"));
            Assert.assertTrue(ApmbaseUtil.fileExists(emConfigCustomDirLocation
                + "logs/IntroscopeEnterpriseManager.log"));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            httpConnMessage(collector1Host);
            LOGGER.info("scenario passed lets revert the configuration to stop....");
            try {
                copyFile(emConfigCustomDirLocation + "logs/IntroscopeEnterpriseManager.log",
                    emLogFile, MOM_MACHINE_ID);
                LOGGER.info("Copied EM LOG file succesfully......");
            } catch (Exception e) {
                LOGGER.info("EM LOG unable to copy.....");
            }
            stopTomcatAgent(TOMCAT_ROLE_ID);
            LOGGER.info("Tomcat Agent is stopped");
            stopEMServiceFlowExecutor(MOM_MACHINE_ID);
            LOGGER.info("MOM forcefully killed to revert the files....");
            roleIds.clear();
            roleIds.add(MOM_ROLE_ID);
            roleIds.add(COLLECTOR1_ROLE_ID);
            roleIds.add(TOMCAT_ROLE_ID);
            renamePropertyFilesWithTestCaseId(roleIds, testcaseId);
            restorePropFiles(roleIds);
            LOGGER.info("Successfully finished second test....");
        }

        finally {
            replaceProp(
                "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -Dcom.wily.introscope.em.properties="
                    + emConfigCustomDirLocation, "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC",
                MOM_MACHINE_ID, emLaxFile);
            stopTestBed();
            renameLogWithTestCaseId(roleIds, testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true,priority = 21)
    public void verify_ALM_440683_82316_EMPort_number_refresh_always_requires_EM_restart_when_connection_type_is_changed() {
        testcaseId = "440683";
        try {
            testCaseStart(testcaseId);
            startTestBed();
            defaultConnMessage(collector1Host);
            // enableACC("http", false);
            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                momWebPort);
            stopAgent();
            LOGGER.info("Agent stopped.....");
            startAgent();
            httpConnMessage(collector1Host);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true,priority = 22)
    public void verify_ALM_430041_75627_EMPort_number_refresh_always_requires_EM_restart_when_connection_type_is_changed() {
        testcaseId = "430041";
        try {
            testCaseStart(testcaseId);
            startTestBed();
            defaultConnMessage(collector1Host);
            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                momWebPort);
            stopAgent();
            LOGGER.info("Agent stopped.....");
            startAgent();
            httpConnMessage(collector1Host);
            stopTestBed();
            LOGGER.info("Default port and HTTP port Test Completed...");
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART1");
            enableACC("https", true);
            httpsConnMessage(collector1Host);
            LOGGER.info("Default HTTPS port Test Completed...");
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART2");
            enableACC("ssl", true);
            sslConnMessage(collector1Host);
            LOGGER.info("Default SSL port Test Completed...");
        }
        finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART3");
            testCaseEnd(testcaseId);
        }
    }
    
    @Test(groups = {"BAT"}, enabled = true,priority = 20)
    public void verify_ALM_205008_DefaultEMJettyVerify() {
        testcaseId = "205008";
        testCaseStart(testcaseId);

        try {
            LOGGER.info("EM HTTP Test Started....");
            Assert.assertTrue(enableACC("http", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            httpConnMessage(collector1Host);
            stopTestBed();
            LOGGER.info("EM HTTP Test Ended....");
            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART1");

            backupConfigs();
            Assert.assertTrue(enableACC("https", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSecureWebPort), emLibDir);
            httpsConnMessage(collector1Host);
            stopTestBed();
            LOGGER.info("EM HTTPS Test Ended....");

            revertConfigAndRenameLogsWithTestId(testcaseId + "_PART2");

            backupConfigs();
            Assert.assertTrue(enableACC("ssl", true));
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(emSSLPort), emLibDir);
            sslConnMessage(collector1Host);
            LOGGER.info("EM SSL Test Ended....");
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    @Test(groups = {"BAT"}, enabled = true,priority = 23)
    public void verify_ALM_205011_Jetty_Syntax_Error() {
        testcaseId = "205011";

        try {
            backupConfigs();
            testCaseStart(testcaseId);
            Assert.assertTrue(enableACC("https", false));
            replaceProp("</Configure>", " ", MOM_MACHINE_ID, emJettyFileMom);
            startTestBed();
            checkAgentLogForMsg("Failed to connect to the Introscope Enterprise Manager at");
            LOGGER.info("Before doing http settings set back agent profile url to normal....");
            replaceProp("agentManager.url.1=https://" + momHost + ":" + emSecureWebPort,
                "agentManager.url.1=http://" + momHost + ":" + momWebPort, TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            replaceProp(
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                MOM_MACHINE_ID, configFileMom);
            restartEM(MOM_ROLE_ID);
            stopAgent();
            LOGGER.info("Stopped the Agent.....");
            startAgent();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);
            httpConnMessage(collector1Host);
            stopEMServices();
            LOGGER.info("MOM and collectors are stopped.....");
            startEMServices();
            LOGGER.info("MOM and Collectors started succesfully.....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momWebPort), emLibDir);

            replaceProp("agentManager.url.1=https://" + momHost + ":" + emSecureWebPort,
                "agentManager.url.1=" + momHost + ":" + momPort, TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            stopAgent();
            LOGGER.info("Stopped the Agent second Time.....");
            startAgent();
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);

            stopEMServices();
            LOGGER.info("MOM and collectors are stopped second Time.....");
            startEMServices();
            LOGGER.info("MOM and Collectors started succesfully second Time.....");
            waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), emLibDir);
        } finally {
            stopTestBed();
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }

    }

    @Test(groups = {"DEEP"}, enabled = true,priority = 24)
    public void verify_ALM_205012_Jetty_Config_file_missing() {
        testcaseId = "205012";
        try {
            LOGGER.info("This test case can be ignored it taking lot of time");
            testCaseStart(testcaseId);
            Assert.assertTrue(enableACC("https", false));
            renameFile(emJettyFileMom, emJettyFileMom + "_PART1", MOM_MACHINE_ID);
            LOGGER.info("EM JETTY file is renamed .....");
            startTestBed();
            checkAgentLogForMsg("Failed to connect to the Introscope Enterprise Manager at");
            stopAgent();
            renameFile(tomcatAgentLogFile, tomcatAgentLogFile + testcaseId + "_PART1",
                TOMCAT_MACHINE_ID);
            replaceProp("agentManager.url.1=https://" + momHost + ":" + emSecureWebPort,
                "agentManager.url.1=http://" + momHost + ":" + momWebPort, TOMCAT_MACHINE_ID,
                tomcatagentProfileFile);
            startAgent();
            checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
                "Lost contact with the Introscope Enterprise Manager at " + momHost + ":"
                    + momWebPort);
        }
        finally {
            stopTestBed();
            renameFile(emJettyFileMom + "_PART1", emJettyFileMom, MOM_MACHINE_ID);
            revertConfigAndRenameLogsWithTestId(testcaseId);
            testCaseEnd(testcaseId);
        }
    }

    public void setBadTrustStorePassword(List<String> roleIds) {
        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            replaceProp("introscope.enterprisemanager.trustpassword.channel2=password",
                "introscope.enterprisemanager.trustpassword.channel2=badpassword", MACHINE_ID,
                configFile);
        }
    }

    public void setBadKeyStorePassword(List<String> roleIds) {
        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            replaceProp("introscope.enterprisemanager.keypassword.channel2=password",
                "introscope.enterprisemanager.keypassword.channel2=badpassword", MACHINE_ID,
                configFile);
        }
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
            // Failed to connect to the Introscope Enterprise Manager at
            // tas-itc-n56:8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory
            httpsReconnectString.add("Failed to connect to the Introscope Enterprise Manager at "
                + hostName + ".ca.com" + ":" + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory");
            httpsReconnectString.add("Failed to connect to the Introscope Enterprise Manager at "
                + hostName + ":" + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory");
        }
        verifyIfAtleastOneKeywordIsInLog(machineID, logFileName, httpsReconnectString);
    }

    private void enableBothChannelsOnEM(List<String> roleIds) {
        List<String> list = new ArrayList<String>();
        list.add("introscope.enterprisemanager.enabled.channels=channel1,channel2");
        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            replaceProp("introscope.enterprisemanager.enabled.channels=channel1",
                "#introscope.enterprisemanager.enabled.channels=channel1", MACHINE_ID, configFile);
            appendProp(list, MACHINE_ID, configFile);
        }

    }

    private void unCommentEMJettyEntryOnEM(List<String> roleIds) {
        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
            replaceProp(
                "#introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                "introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml",
                MACHINE_ID, configFile);
        }
    }

    public void updateEMJettyDTD(List<String> roleIds) {
        String emJettyFile = null;
        String emMachineId = null;
        for (String roleId : roleIds) {
            emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            emMachineId = envProperties.getMachineIdByRoleId(roleId);
            String OrigString =
                "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://jetty.mortbay.org/configure.dtd\">";
            String stringToReplace =
                "<!DOCTYPE Configure PUBLIC \"-//Mort Bay Consulting//DTD Configure//EN\" \"http://www.eclipse.org/jetty/configure.dtd\">";
            replaceProp(OrigString, stringToReplace, emMachineId, emJettyFile);
        }
    }

    /**
     * Update verify entry in EMJetty file
     * 
     * @param roleIds
     */
    public void updateEMJettyVerifyHostName(List<String> roleIds) {
        String emJettyFile = null;
        for (String roleId : roleIds) {
            emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            try {
                List<String> args = new ArrayList<String>();
                args.add(emJettyFile);
                args.add("false");
                args.add("true");
                XMLModifierFlowContext modifyXML =
                    new XMLModifierFlowContext.Builder().arguments(args)
                        .methodName("xmlFileUtil.xmlFileUtil.updateEMJettyVerifyHostName").build();
                runFlowByMachineId(envProperties.getMachineIdByRoleId(roleId),
                    XMLModifierFlow.class, modifyXML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update verify entry in EMJetty file
     * 
     * @param roleIds
     */
    public void updateEMJettyValidCertificate(List<String> roleIds) {
        for (String roleId : roleIds) {
            String emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            try {
                List<String> args = new ArrayList<String>();
                args.add(emJettyFile);
                args.add("false");
                args.add("true");
                XMLModifierFlowContext modifyXML =
                    new XMLModifierFlowContext.Builder().arguments(args)
                        .methodName("xmlFileUtil.updateEMJettyValidateCertificate").build();
                runFlowByMachineId(envProperties.getMachineIdByRoleId(roleId),
                    XMLModifierFlow.class, modifyXML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update verify entry in EMJetty file
     * 
     * @param roleIds
     */
    public void updateEMJettyNeedClientAuth(List<String> roleIds) {
        for (String roleId : roleIds) {
            String emJettyFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                    + "em-jetty-config.xml";
            try {
                List<String> args = new ArrayList<String>();
                args.add(emJettyFile);
                args.add("false");
                args.add("true");
                XMLModifierFlowContext modifyXML =
                    new XMLModifierFlowContext.Builder().arguments(args)
                        .methodName("xmlFileUtil.updateEMJettyNeedClientAuth").build();
                runFlowByMachineId(envProperties.getMachineIdByRoleId(roleId),
                    XMLModifierFlow.class, modifyXML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void enableHTTPSPropertiesForEM(List<String> roleIds) {
        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);

            replaceProp("introscope.enterprisemanager.needclientauth.channel2=false",
                "introscope.enterprisemanager.needclientauth.channel2=true", MACHINE_ID, configFile);
            replaceProp(
                "#introscope.enterprisemanager.truststore.channel2=internal/server/keystore",
                "introscope.enterprisemanager.truststore.channel2=internal/server/keystore",
                MACHINE_ID, configFile);
            replaceProp("#introscope.enterprisemanager.trustpassword.channel2=password",
                "introscope.enterprisemanager.trustpassword.channel2=password", MACHINE_ID,
                configFile);
        }
    }


    public void enableNeedClientAuthForEM(List<String> roleIds) {

        for (String roleId : roleIds) {
            String MACHINE_ID = envProperties.getMachineIdByRoleId(roleId);
            String configFile =
                envProperties.getRolePropertyById(roleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);

            replaceProp("introscope.enterprisemanager.needclientauth.channel2=false",
                "introscope.enterprisemanager.needclientauth.channel2=true", MACHINE_ID, configFile);
        }
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
        replaceProp("agentManager.url.1=" + emHostOriginal + ":" + portOriginal,
            "agentManager.url.1=https://" + replaceEmHost + ":" + replaceemPort, AgentMachineId,
            agentProfileFile);
    }

    private boolean enableACC(String type, boolean startServices) {
        boolean result = false;
        setLoadBalancingPropValues(MOM_ROLE_ID);
        roleIds.clear();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);

        if (type.equalsIgnoreCase("http")) {

            enableHTTPOnEM(roleIds);
            setAgentHttpUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                momWebPort);
            result = true;
        } else if (type.equalsIgnoreCase("https")) {

            updateEMPropertiesForHTTPS(roleIds);
            updateEmJettyConfigXmlSecureAttributes(roleIds);
            roleIds.clear();

            roleIds.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForHTTPS(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSecureWebPort);
            result = true;

        } else if (type.equalsIgnoreCase("ssl")) {
            updateEMPropertiesForSSL(roleIds);
            updateEmJettyConfigXmlSecureAttributes(roleIds);
            roleIds.clear();

            roleIds.add(TOMCAT_ROLE_ID);
            updateTomcatPropertiesForSSL(roleIds);
            copyKeyStoreToAgent(MOM_ROLE_ID, TOMCAT_ROLE_ID);
            setAgentSSLUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, momHost, momPort, momHost,
                emSSLPort);
            result = true;
        } else
            result = false;

        if (startServices) {
            startMoM();
            startEMCollectors();
            startAgent();
        } else
            LOGGER.info("Not starting any Services.....");
        return result;

    }

    private void httpConnMessage(String connectedHost) {
        LOGGER.info("Checking for HTTP AGENT connection message in Agent log.....");
        connectionMessage.clear();
        connectionMessage
            .add(" Connected to "
                + connectedHost
                + ":"
                + momWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");
        connectionMessage
            .add(" Connected to "
                + connectedHost
                + ".ca.com"
                + ":"
                + momWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory in allowed mode");
        Assert.assertTrue(verifiesIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
            connectionMessage));

    }

    private void defaultConnMessage(String connectedHost) {

        LOGGER.info("Checking for HTTPs AGENT connection message in Agent log.....");

        connectionMessage.clear();
        connectionMessage.add(" Connected to " + connectedHost + ".ca.com" + ":" + momPort
            + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in allowed mode");

        connectionMessage.add(" Connected to " + connectedHost + ":" + momPort
            + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in allowed mode");
        Assert.assertTrue(verifiesIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
            connectionMessage));

    }

    private void httpsConnMessage(String connectedHost) {

        LOGGER.info("Checking for HTTPs AGENT connection message in Agent log.....");

        connectionMessage.clear();
        connectionMessage
            .add(" Connected to "
                + connectedHost
                + ".ca.com"
                + ":"
                + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode");

        connectionMessage
            .add(" Connected to "
                + connectedHost
                + ":"
                + emSecureWebPort
                + ",com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory in allowed mode");
        Assert.assertTrue(verifiesIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
            connectionMessage));

    }

    private void sslConnMessage(String connectedHost) {
        LOGGER.info("Checking for SSL AGENT connection message in Agent log.....");
        connectionMessage.clear();
        connectionMessage.add(" Connected to " + connectedHost + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in allowed mode");
        connectionMessage.add(" Connected to " + connectedHost + ".ca.com" + ":" + emSSLPort
            + ",com.wily.isengard.postofficehub.link.net.SSLSocketFactory in allowed mode");
        Assert.assertTrue(verifiesIfAtleastOneKeywordIsInLog(TOMCAT_MACHINE_ID, tomcatAgentLogFile,
            connectionMessage));

    }

    private void emConfigDirChange() {
        replaceProp("=logs/", "=" + emConfigCustomDirLocation + "/logs/", MOM_MACHINE_ID,
            configFileMom);
        replaceProp("=data", "=" + emConfigCustomDirLocation + "data", MOM_MACHINE_ID,
            configFileMom);
        replaceProp("introscope.enterprisemanager.transactionevents.storage.dir=traces",
            "introscope.enterprisemanager.transactionevents.storage.dir="
                + emConfigCustomDirLocation + "traces", MOM_MACHINE_ID, configFileMom);
        replaceProp(
            "#introscope.enterprisemanager.directory.config=config",
            "introscope.enterprisemanager.directory.config=" + emConfigCustomDirLocation + "config",
            MOM_MACHINE_ID, configFileMom);

        replaceProp("-XX:+UseConcMarkSweepGC -XX:+UseParNewGC",
            "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC  -Dcom.wily.introscope.em.properties="
                + emConfigCustomDirLocation, MOM_MACHINE_ID, emLaxFile);
    }
}
