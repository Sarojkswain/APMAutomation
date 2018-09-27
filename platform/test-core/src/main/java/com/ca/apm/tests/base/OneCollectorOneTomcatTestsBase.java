package com.ca.apm.tests.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;

public class OneCollectorOneTomcatTestsBase extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(OneCollectorOneTomcatTestsBase.class);
    public final String momHost;
    public String momHostIP;
    public final String emLibDir;
    public final String configFileMom;
    public final String configFileC1;
    public final String user;
    public final String password;
    public final String guestUser;
    public final String guestPassword;
    public final String AgentExpression;
    public final String tomcatAgentExpression;
    public final String MetricExpression;
    public final String loadBalanceFile;
    public final String momlogFile;
    public final String momlogDir;
    public final String collectorlogFile;
    public final String loadBalanceFile_Copy;
    public final String collector1Port;
    public final String collector1Host;
    public String collector1HostIP;
    public final String tomcatHost;
    public final String clwJarFileLoc;
    public final String momInstallDir;
    public final String momConfigDir;
    public final String momWebPort;
    public final String momSecureWebPort;
    public final String c1WebPort;
    public final String momPort;
    public final String tomcatagentProfileFile;
    public final String tomcatagentProfileFile_backup;
    public final String emSecureWebPort;
    public final String configFileMom_backup;
    public final String configFileC1_backup;
    public final String tomcatAgentExp;
    public String tomcatAgentLogFile;
    public ApmbaseUtil apmbaseutil;
	public final String momconfigFile;
	public final String colconfigFile;
	public final String userliteral,nametag,pwdliteral;
	protected final CLWCommons clw;
	    
    public List<String> roleIds = new ArrayList<String>();
    String httpsPort = ApmbaseConstants.emSecureWebPort;

    public OneCollectorOneTomcatTestsBase() {
        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExp = ".*Tomcat.*";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
        collector1Port =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
        momSecureWebPort = ApmbaseConstants.emSecureWebPort;
        momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emWebPort");
        c1WebPort =
            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");

	        momconfigFile =
                envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        colconfigFile =
                envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        
        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);

        try {
            collector1HostIP = returnIPforGivenHost(collector1Host);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);
        
        momInstallDir = envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        momConfigDir= envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        loadBalanceFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        momHost = envProperties.getMachineHostnameByRoleId(MOM_ROLE_ID);
        
        apmbaseutil = new ApmbaseUtil();

        try {
            momHostIP = returnIPforGivenHost(momHost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        emLibDir =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        momlogDir =
        envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_DIR);

        momlogFile =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);

        collectorlogFile =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_LOG_FILE);

        configFileMom =
            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileMom_backup = configFileMom + "_backup";
        configFileC1 =
            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileC1_backup = configFileC1 + "_backup";

        clwJarFileLoc = emLibDir + "CLWorkstation.jar";
        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatagentProfileFile_backup = tomcatagentProfileFile + "_backup";

        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";

        user = ApmbaseConstants.emUser;
        password = ApmbaseConstants.emPassw;
        guestUser = ApmbaseConstants.guestUser;
        guestPassword = ApmbaseConstants.guestPassw;
        userliteral = ApmbaseConstants.USER_LITERAL;
        nametag = ApmbaseConstants.NAME_TAG;
		pwdliteral = ApmbaseConstants.PWD_LITERAL;
	    clw = new CLWCommons();
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        LOGGER.info("Before Class initialize.....");
        List<String> machines = new ArrayList<String>();
        machines.add(MOM_MACHINE_ID);
        machines.add(COLLECTOR1_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);
        backupConfigs();
    }

    /**
     * Stops all the EMs of the testbed
     */
    public void stopEMServices() {
        stopCollectorEM(MOM_ROLE_ID, COLLECTOR1_ROLE_ID);
        stopEMServiceFlowExecutor(COLLECTOR1_MACHINE_ID);
        stopEM(MOM_ROLE_ID);
        stopEMServiceFlowExecutor(MOM_MACHINE_ID);
        harvestWait(10);
    }

    /**
     * Stops the TomcatAgent of the testbed
     */
    public void stopAgent() {
        stopTomcatServiceFlowExecutor(TOMCAT_MACHINE_ID);
    }

    /**
     * Stops all the EMs and Agents of the testbed
     */

    public void stopServices() {
        stopAgent();
        stopEMServices();
        harvestWait(5);
    }

    /**
     * Starts the Collector EM of the testbed
     */

    public void startEMCollectors() {
        try {
            startEM(COLLECTOR1_ROLE_ID);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false, "Collector EM Failed to Start");
        }
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
     * Starts the Tomcat Agent of the testbed
     */
    public void startAgent() {
        try {
            startTomcatAgent(TOMCAT_ROLE_ID);
        } catch (Exception e) {
            LOGGER.error("Tomcat Agent failed to start");
            Assert.assertTrue(false);
            e.printStackTrace();
        }
    }

    /**
     * Starts all the components EMs and Agent of the testbed
     */
    public void startTestBed() {
        startEMCollectors();
        startMoM();
        startAgent();
        LOGGER.info("All the components of the Testbed are started");
    }

    /**
     * Stops all the components EMs and Agent of the testbed
     */
    public void stopTestBed() {
        stopEMServices();
        stopAgent();
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
     * Checks the MoM log of the testbed for the specified string message
     * 
     * @param message
     */
    public void checkMoMLogForMsg(String message) {
        checkLogForMsg(envProperties, MOM_MACHINE_ID, momlogFile, message);
    }

    /**
     * Checks the MoM log of the non existence of specified keyword in the given file at the instance
     * 
     * @param message
     */
    public void checkMoMLogForNoMsg(String message) {
        checkLogForNoMsg(envProperties, MOM_MACHINE_ID, momlogFile, message);
    }
    
    /**
     * Checks the Collector log of the testbed for the specified string message
     * 
     * @param message
     */
    public void checkCollLogForMsg(String message) {
        checkLogForMsg(envProperties, COLLECTOR1_MACHINE_ID, collectorlogFile, message);
    }

    /**
     * Checks the Agent log of the testbed for the specified string message
     * 
     * @param message
     */
    public void checkAgentLogForMsg(String message) {
        checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, message);

    }

    /**
     * Replaces the specified string with the given string in MoM EM configuration file
     * 
     * @param message
     */
    public void replaceMoMProperty(String findStr, String replaceStr) {
        replaceProp(findStr, replaceStr, MOM_MACHINE_ID, configFileMom);
    }
    
    /**
     * Replaces the specified string with the given string in COL EM configuration file
     * 
     * @param message
     */
    public void replaceColProperty(String findStr, String replaceStr) {
        replaceProp(findStr, replaceStr, COLLECTOR1_MACHINE_ID, configFileC1);
    }

    /**
     * Replaces the specified string with the given string in Agent configuration file
     * 
     * @param message
     */
    public void replaceAgentProperty(String findStr, String replaceStr) {
        replaceProp(findStr, replaceStr, TOMCAT_MACHINE_ID, tomcatagentProfileFile);
    }

    /**
     * Checks if the Collector of the testbed connected to MOM
     */
    public void checkCollectorToMOMConnectivity() {
        checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
            momHost, momPort, emLibDir);
    }

    public void backupConfigs() {
        roleIds.clear();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(TOMCAT_ROLE_ID);
//        backupPropFiles(roleIds);
//        roleIds.clear();
//        roleIds.add(MOM_ROLE_ID);
//        roleIds.add(COLLECTOR1_ROLE_ID);
        backupConfigDir(roleIds);
      //  backupEMJettyFiles(roleIds);
      //  backupEMLoadBalancingXMLFiles(roleIds);
    }

    public void revertConfigAndRenameLogsWithTestId(String testCaseId) {
        roleIds.clear();
        roleIds.add(MOM_ROLE_ID);
        roleIds.add(COLLECTOR1_ROLE_ID);
        roleIds.add(TOMCAT_ROLE_ID);
      //  renamePropertyFilesWithTestCaseId(roleIds, testCaseId);
      //  restorePropFiles(roleIds);
        renameDirWithTestCaseId(roleIds, testCaseId);
//        renameLogWithTestCaseId(roleIds, testCaseId);
//        roleIds.clear();
//        roleIds.add(MOM_ROLE_ID);
//        roleIds.add(COLLECTOR1_ROLE_ID);
//        
        restoreConfigDir(roleIds);
//        renameEMJettyFilesWithTestCaseId(roleIds, testCaseId);
//        renameEMLoadBalancingXMLFilesWithTestCaseId(roleIds, testCaseId);
//        restoreEMJettyFiles(roleIds);
//        restoreEMLoadBalancingXMLFiles(roleIds);
    }
    
    public void revertConfigAndRenameLogsWithTestId(String testCaseId, List<String> roleIds) {
        renameDirWithTestCaseId(roleIds, testCaseId);
        restoreConfigDir(roleIds);
    }

}
