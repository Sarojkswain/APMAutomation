package com.ca.apm.tests.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.flow.XMLModifierFlow;
import com.ca.apm.commons.flow.XMLModifierFlowContext;
import com.ca.apm.commons.tests.BaseAgentTest;

import static com.ca.apm.tests.cdv.CDVConstants.*;

public class TwoCDVTwoClustersOneStandaloneOneTomcatTestsBase extends BaseAgentTest {
	
		private static final Logger LOGGER = LoggerFactory
		        .getLogger(TwoCDVTwoClustersOneStandaloneOneTomcatTestsBase.class);
		
		
		protected CLWCommons clw = new CLWCommons();
		protected EmUtils emUtils = utilities.createEmUtils();
		protected XMLUtil xmlutil = new XMLUtil();
	    protected int cdv1Port;
	    protected int cdv2Port;
	    protected String cdv1Host;
	    protected String cdv2Host;
	    protected String mom1Host;
	    protected String mom1Collector1Host;
	    protected String mom2Collector1Host;
	    protected String tomcatAgentHost;
	    protected String tomcatAgentPort;
	    protected String user;
	    protected String password;
	    protected String cdv1LibDir;
	    protected String cdv1ConfigDir;
	    protected String cdv1logFile;
	    protected String cdv2logFile;
	    protected String cdv1ConfigFile;
	    protected String cdv2ConfigFile;
	    protected String cdv1InstallLogFile;
	    protected String cdv1DomainsXmlFile;
	    protected String mom1InstallLogFile;
	    protected String StandlaoneEMInstallLogFile;
	    protected String mom1Collector1InstallLogFile;
	    protected String mom1DomainsXmlFile;
	    protected String mom1Collector1DomainsXmlFile;
	    protected String mom1Collector1EventsThresholdConfigFile;
	    protected String mom1Collector1ConfigDir;
		protected String testCaseId;
		protected String testCaseName;
		protected ArrayList<String> rolesInvolved = new ArrayList<String>();
	    public String mom1HostIP;
	    protected final String mom1ConfigFile;
	    protected final String mom2ConfigFile;
	    protected final String standaloneEMConfigFile;
	    protected final String mom1collector1ConfigFile;
	    protected final String AllagentsExpression;
	    protected final String tomcatAgentExpression;
	    protected final String cpuMetricExpression;
	    protected final String mom1loadBalanceFile;
	    protected final String mom1logFile;
	    protected final String mom2logFile;
	    protected final String mom1LibDir;
	    protected final String mom1Collector1logFile;
	    protected final String mom2Collector1logFile;
	    protected final String mom1Collector1Port;
	    protected final String mom2Collector1Port;
	    protected String mom1Collector1HostIP;
	    protected final String mom1ClwJarFileLoc;
	    protected final String mom1WebPort;
	    protected final String mom1SecureWebPort;
	    protected final String mom1ConfigDir;
	    protected final String mom1Collector1WebPort;
	    protected final String mom1Port;
	    protected final String tomcatAgentProfileFile;
	    protected final String tomcatAgentExp;
	    protected String tomcatAgentLogFile;
	    protected ApmbaseUtil apmbaseutil;
	    protected final String mom1LogDir;
	    protected List<String> roleIds = new ArrayList<String>();
	    protected String cdv1InstallDir;
	    protected String mom1InstallDir;
	    protected String standaloneEMInstallDir;
	    protected String mom1Collector1InstallDir;

	    public TwoCDVTwoClustersOneStandaloneOneTomcatTestsBase() {
	    	
	    	cdv1InstallDir =
		            envProperties.getRolePropertyById(CDV1_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	mom1InstallDir =
		            envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	standaloneEMInstallDir = 
	    			envProperties.getRolePropertyById(STANDALONE_EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	mom1Collector1InstallDir =
		            envProperties.getRolePropertyById(MOM1_COL1_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	cdv1Host = envProperties.getMachineHostnameByRoleId(CDV1_ROLE_ID);
	    	cdv2Host = envProperties.getMachineHostnameByRoleId(CDV2_ROLE_ID);
	        cdv1Port =
	            Integer.parseInt(envProperties.getRolePropertiesById(CDV1_ROLE_ID).getProperty("emPort"));
	        cdv2Port =
		            Integer.parseInt(envProperties.getRolePropertiesById(CDV2_ROLE_ID).getProperty("emPort"));
	        mom1Host = envProperties.getMachineHostnameByRoleId(MOM1_COL1_ROLE_ID);
	        mom1Collector1Host = envProperties.getMachineHostnameByRoleId(MOM1_COL1_ROLE_ID);
	        mom2Collector1Host = envProperties.getMachineHostnameByRoleId(MOM2_COL1_ROLE_ID);
	        cdv1LibDir =
	            envProperties.getRolePropertyById(CDV1_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
	        cdv1ConfigDir =
	            envProperties.getRolePropertyById(CDV1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
	        cdv1DomainsXmlFile = cdv1ConfigDir + "/domains.xml"; 
	        tomcatAgentHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);
	        tomcatAgentPort =
	            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
	                DeployTomcatFlowContext.ENV_TOMCAT_PORT);
	        user = ApmbaseConstants.emUser;
	        password = ApmbaseConstants.emPassw;
	        AllagentsExpression = "\".*\\|.*\\|.*\"";
	        tomcatAgentExp = ".*Tomcat.*";
	        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
	        cpuMetricExpression = ".*CPU.*";
	        mom1Port = envProperties.getRolePropertiesById(MOM1_ROLE_ID).getProperty("emPort");
	        mom1Collector1Port = envProperties.getRolePropertiesById(MOM1_COL1_ROLE_ID).getProperty("emPort");
	        mom2Collector1Port = envProperties.getRolePropertiesById(MOM2_COL1_ROLE_ID).getProperty("emPort");
	        mom1SecureWebPort = ApmbaseConstants.emSecureWebPort;
	        mom1WebPort = envProperties.getRolePropertiesById(MOM1_ROLE_ID).getProperty("emWebPort");
	        mom1Collector1WebPort =
	            envProperties.getRolePropertiesById(MOM1_COL1_ROLE_ID).getProperty("emWebPort");
	        mom1Collector1Host = envProperties.getMachineHostnameByRoleId(MOM1_COL1_ROLE_ID);
	        try {
	        	mom1Collector1HostIP= returnIPforGivenHost(mom1Collector1Host);
 			} catch (IOException e) {				
 				e.printStackTrace();
 			}
	        mom1loadBalanceFile =
	            envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
	                + "/loadbalancing.xml";
	        mom1Host = envProperties.getMachineHostnameByRoleId(MOM1_ROLE_ID);
	        try {
	 				mom1HostIP= returnIPforGivenHost(mom1Host);
	 			} catch (IOException e) {				
	 				e.printStackTrace();
	 			}
	        mom1LibDir =
	            envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
	        mom1logFile =
	            envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        mom2logFile =
	        		envProperties.getRolePropertyById(MOM2_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        mom1ConfigDir= envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
	        mom1LogDir =
	            envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_DIR);
	        mom1InstallLogFile = mom1InstallDir + "/install/" + getEMInstallLogFile();
	        StandlaoneEMInstallLogFile = standaloneEMInstallDir + "/install/" + getEMInstallLogFile();
	        mom1DomainsXmlFile = mom1ConfigDir + "/domains.xml";
	        mom1Collector1logFile =
		            envProperties.getRolePropertyById(MOM1_COL1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        mom2Collector1logFile =
		            envProperties.getRolePropertyById(MOM2_COL1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        mom1Collector1InstallLogFile = mom1Collector1InstallDir + "/install/" + getEMInstallLogFile();
	        mom1Collector1ConfigDir = envProperties.getRolePropertyById(MOM1_COL1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
	        mom1Collector1DomainsXmlFile = mom1Collector1ConfigDir + "/domains.xml";
	        mom1Collector1EventsThresholdConfigFile = mom1Collector1ConfigDir + "/apm-events-thresholds-config.xml";
	        cdv1logFile = 
	        		envProperties.getRolePropertyById(CDV1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        cdv2logFile = 
	        		envProperties.getRolePropertyById(CDV2_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        cdv1ConfigFile =
		            envProperties.getRolePropertyById(CDV1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        cdv2ConfigFile = 
	        		envProperties.getRolePropertyById(CDV2_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        cdv1InstallLogFile = cdv1InstallDir + "/install/" + getEMInstallLogFile();
	        mom1ConfigFile =
	            envProperties.getRolePropertyById(MOM1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        mom2ConfigFile =
	        		envProperties.getRolePropertyById(MOM2_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        mom1collector1ConfigFile =
	            envProperties.getRolePropertyById(MOM1_COL1_ROLE_ID,
	                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        mom1ClwJarFileLoc = mom1LibDir + "CLWorkstation.jar";
	        standaloneEMConfigFile = 
	        		envProperties.getRolePropertyById(STANDALONE_EM_ROLE_ID,
	    	                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        tomcatAgentProfileFile =
	            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
	                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
	                + "/wily/core/config/IntroscopeAgent.profile";
	        tomcatAgentLogFile =
	            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
	                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
	        apmbaseutil = new ApmbaseUtil();
	        user = ApmbaseConstants.emUser;
	        password = ApmbaseConstants.emPassw;
	    }

	    @BeforeSuite(alwaysRun = true)
	    public void initialize() {
	        List<String> machines = new ArrayList<String>();
	        machines.add(CDV_MACHINE_ID);
	        machines.add(MOM1_MACHINE_ID);
	        machines.add(MOM2_MACHINE_ID);
	        machines.add(AGENT_MACHINE_ID);
	        syncTimeOnMachines(machines); 
	        backupConfigs();
	        
	    }
	    
	    /**
	     * Creates a List of all EMs of the Testbed
	     * @return emRoles
	     */
	    protected List<String> getemRoles(){
	    	List<String> emRoles = new ArrayList<String>();
	    	emRoles.add(MOM1_ROLE_ID);
	    	emRoles.add(MOM1_COL1_ROLE_ID);
	    	emRoles.add(MOM2_ROLE_ID);
	    	emRoles.add(MOM2_COL1_ROLE_ID);
	    	emRoles.add(CDV1_ROLE_ID);
	    	emRoles.add(CDV2_ROLE_ID);
	    	return emRoles;
	    }

	    /**
	     * Stops all the EMs of the testbed
	     */
	    protected void stopEMServices() {
	    	stopMoM1Collector1();
	    	stopMoM1();
	    	stopMoM2Collector1();
	    	stopMoM2();
	        stopCDV1();
	        stopCDV2();
	        stopStandaloneEM();
	        harvestWait(10);
	    }

	    
	    /**
	     * Stops all the EMs and Agents of the testbed
	     */

	    protected void stopServices() {
	    	stopTomcatAgent();
	        stopEMServices();
	        harvestWait(5);
	    }
	    
	    /**
	     * Starts the Collector EMs of the testbed
	     */

	    protected void startEMCollectors() {
	    	startMoM1Collector1();
	    	startMoM2Collector1();
	    }
	    
	    /**
	     * Starts the CDV1 EM of the testbed
	     */
	    protected void startCDV1() {
	        try {
	            startEM(CDV1_ROLE_ID);
	        	LOGGER.info("CDV1 is started");
	        } catch (Exception e) {
	        	LOGGER.error("CDV1 failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the CDV2 EM of the testbed
	     */
	    protected void startCDV2() {
	        try {
	            startEM(CDV2_ROLE_ID);
	        	LOGGER.info("CDV2 is started");
	        } catch (Exception e) {
	        	LOGGER.error("CDV2 failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the MoM1 EM of the testbed
	     */
	    protected void startMoM1() {
	        try {
	            startEM(MOM1_ROLE_ID);
	        	LOGGER.info("MoM1 is started");
	        } catch (Exception e) {
	        	LOGGER.error("MoM1 failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the MoM1 EM of the testbed
	     */
	    protected void startMoM2() {
	        try {
	            startEM(MOM2_ROLE_ID);
	        	LOGGER.info("MoM2 is started");
	        } catch (Exception e) {
	        	LOGGER.error("MoM2 failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the MoM1-Collector1 EM of the testbed
	     */
	    protected void startMoM1Collector1() {
	        try {
	            startEM(MOM1_COL1_ROLE_ID);
	        	LOGGER.info("MoM1-Collector1 is started");
	        } catch (Exception e) {
	        	LOGGER.error("MoM1-Collector1 failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the MoM2-Collector1 EM of the testbed
	     */
	    protected void startMoM2Collector1() {
	        try {
	            startEM(MOM2_COL1_ROLE_ID);
	        	LOGGER.info("MoM2-Collector1 is started");
	        } catch (Exception e) {
	        	LOGGER.error("MoM2-Collector1 failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the Standalone EM of the testbed
	     */
	    protected void startStandaloneEM() {
	        try {
	            startEM(STANDALONE_EM_ROLE_ID);
	        	LOGGER.info("Standalone EM is started");
	        } catch (Exception e) {
	        	LOGGER.error("Standalone EM failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Stops the CDV1 EM of the testbed
	     */
	    protected void stopCDV1() {	        
	        stopEM(CDV1_ROLE_ID);
	        stopEMServiceFlowExecutor(CDV_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the CDV2 EM of the testbed
	     */
	    protected void stopCDV2() {	        
	        stopEM(CDV2_ROLE_ID);
//	        stopEMServiceFlowExecutor(CDV_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the MoM1 EM of the testbed
	     */
	    protected void stopMoM1() {	        
	    	stopCollectorEM(CDV1_ROLE_ID, MOM1_ROLE_ID);
	        stopEMServiceFlowExecutor(MOM1_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the MoM2 EM of the testbed
	     */
	    protected void stopMoM2() {	        
	    	stopCollectorEM(CDV1_ROLE_ID, MOM1_ROLE_ID);
	        stopEMServiceFlowExecutor(MOM2_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the MoM1-Collector1 EM of the testbed
	     */
	    protected void stopMoM1Collector1() {	        
	    	stopCollectorEM(CDV1_ROLE_ID, MOM1_COL1_ROLE_ID);
	        stopEMServiceFlowExecutor(MOM1_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    
	    /**
	     * Stops the MoM2-Collector1 EM of the testbed
	     */
	    protected void stopMoM2Collector1() {	        
	    	stopCollectorEM(CDV1_ROLE_ID, MOM2_COL1_ROLE_ID);
	        stopEMServiceFlowExecutor(MOM2_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the Standalone EM of the testbed
	     */
	    protected void stopStandaloneEM() {	        
	    	stopCollectorEM(CDV1_ROLE_ID, STANDALONE_EM_ROLE_ID);
	        stopEMServiceFlowExecutor(AGENT_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Starts and Stops the CDV1
	     */
	    protected void startStopCDV1(){
	    	startCDV1();
	    	stopCDV1();
	    }
	    
	    /**
	     * Starts and Stops the MoM1
	     */
	    protected void startStopMoM1(){
	    	startMoM1();
	    	stopMoM1();
	    }
	    
	    /**
	     * Starts and Stops the Standalone EM
	     */
	    protected void startStopStandaloneEM(){
	    	startStandaloneEM();
	    	stopStandaloneEM();
	    }
	    
	    /**
	     * Starts and Stops the MoM1-Collector1
	     */
	    protected void startStopMoM1Collector1(){
	    	startMoM1Collector1();
	    	stopMoM1Collector1();
	    }
	    
	    protected void startTomcatAgent() {
            startTomcatAgent(TOMCAT_ROLE_ID);
	    }
	    
	    /**
	     * Stops the Tomcat Agent of the testbed
	     */
	    protected void stopTomcatAgent(){
	    	stopTomcatServiceFlowExecutor(AGENT_MACHINE_ID);
	    }
	    
	    protected void startCluster1(){
	    	startMoM1Collector1();
	    	startMoM1();
	    }
	    
	    
	    protected void stopCluster1(){
	    	stopMoM1Collector1();
	    	stopMoM1();
	    }
	    /**
	     * Starts all the components EMs and Agents of the testbed
	     */
	    protected void startTestBed(){
	    	startEMCollectors();
	    	startMoM1();
	    	startMoM2();
	    	startCDV1();
	    	startCDV2();
	    	startStandaloneEM();
	    	startTomcatAgent();
	    	LOGGER.info("All the components of the Testbed are started");
	    }
	    
	    /**
	     * Stops all the components EMs and Agent of the testbed
	     */
	    protected void stopTestBed(){
	    	stopEMServices();
	    	stopTomcatAgent();   	
	    }

	    /**
	     * Starts the EMs of the testbed
	     */
	    protected void startEMServices(){
	    	startEMCollectors();
	    	startMoM1();
	    	startMoM2();
	    	startCDV1();
	    	startCDV2();
	    	startStandaloneEM();
	    }
	    
	    /**
	     * Restarts the MoM1 EM of the testbed
	     */
	    protected void restartMOM1(){
	    	stopMoM1();
	    	startMoM1();
	    }
	    
	    /**
	     * Restarts the Collector1 EM of the testbed
	     */
	    protected void restartMoM1Collector1(){
	    	stopMoM1Collector1();
	    	startMoM1Collector1();
	    }
	
	    
	    /**
	     * Restarts the TomcatAgent of the Testbed
	     */
	    protected void restartTomcatAgent(){
	    	stopTomcatAgent();
	    	startTomcatAgent();
	    }
	    
	    /**
	     * Checks the CDV1 log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkCDV1LogForMsg(String message){
	    	checkLogForMsg(envProperties, CDV_MACHINE_ID, cdv1logFile, message);
	    }
	    
	    /**
	     * Checks the CDV2 log of the testbed for the specified string message
	     * does not exist
	     * @param message
	     */
	    protected void checkCDV2LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, CDV_MACHINE_ID, cdv2logFile, message);
	    }
	    
	    /**
	     * Checks the CDV1 log of the testbed for the specified string message
	     * does not exist
	     * @param message
	     */
	    protected void checkCDV1LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, CDV_MACHINE_ID, cdv1logFile, message);
	    }
	    
	    /**
	     * Checks the CDV1 Install log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkCDV1InstallLogForMsg(String message){
	    	checkLogForMsg(envProperties, CDV_MACHINE_ID, cdv1InstallLogFile, message);
	    }
	    
	    /**
	     * Checks the CDV1 Install log of the testbed for the specified string message
	     * does not exist
	     * @param message
	     */
	    protected void checkCDV1InstallLogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, CDV_MACHINE_ID, cdv1InstallLogFile, message);
	    }
	    
	    /**
	     * Checks the MoM1 Install log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoM1InstallLogForMsg(String message){
	    	checkLogForMsg(envProperties, MOM1_MACHINE_ID, mom1InstallLogFile, message);
	    }
	    
	    /**
	     * Checks the MoM1 log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoM1LogForMsg(String message){
	    	checkLogForMsg(envProperties, MOM1_MACHINE_ID, mom1logFile, message);
	    }
	    
	    /**
	     * Checks the MoM2 log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoM2LogForMsg(String message){
	    	checkLogForMsg(envProperties, MOM2_MACHINE_ID, mom2logFile, message);
	    }
	    
	    /**
	     * Checks the MoM1 log of the testbed for the specified string message
	     * is not available
	     * @param message
	     */
	    protected void checkMoM1LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, MOM1_MACHINE_ID, mom1logFile, message);
	    }
	    
	    /**
	     * Checks the MoM1 log of the testbed for the specified string message
	     * is not available
	     * @param message
	     */
	    protected void checkMoM2LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, MOM2_MACHINE_ID, mom2logFile, message);
	    }
	    
	    /**
	     * Checks the MOM1-Coll1 Install log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoM1Coll1InstallLogForMsg(String message){
	    	checkLogForMsg(envProperties, MOM1_MACHINE_ID, mom1Collector1InstallLogFile, message);
	    }
	    
	      
	    /**
	     * Checks the Mom1-Collector1 log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoM1Coll1LogForMsg(String message){
	    	checkLogForMsg(envProperties, MOM1_MACHINE_ID, mom1Collector1logFile, message);	
	    }

	    /**
	     * Checks the Mom1-Collector1 log of the testbed for the specified string message
	     * is not available
	     * @param message
	     */
	    protected void checkMoM1Coll1LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, MOM1_MACHINE_ID, mom1Collector1logFile, message);	
	    }
	    
	    
	    /**
	     * Checks the Mom2-Collector1 log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoM2Coll1LogForMsg(String message){
	    	checkLogForMsg(envProperties, MOM2_MACHINE_ID, mom2Collector1logFile, message);	
	    }

	    /**
	     * Checks the Mom1-Collector1 log of the testbed for the specified string message
	     * is not available
	     * @param message
	     */
	    protected void checkMoM2Coll1LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, MOM2_MACHINE_ID, mom2Collector1logFile, message);	
	    }
	    
	    
	    /**
	     * Checks the Agent log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkTomcatAgentLogForMsg(String message){
	    	checkLogForMsg(envProperties, AGENT_MACHINE_ID, tomcatAgentLogFile, message);	
	    	
	    }	
	    
	    /**
	     * Replaces the specified string with the given string in MoM1 EM configuration file
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceMoM1Property(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, MOM1_MACHINE_ID,
		                mom1ConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given string in MoM2 EM configuration file
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceMoM2Property(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, MOM2_MACHINE_ID,
		                mom2ConfigFile);	
	    }
	    
	    
	    /**
	     * Replaces the specified string with the given string in CDV1 EM configuration file
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceCDV1Property(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, CDV_MACHINE_ID,
		                cdv1ConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given Prop-Values Map  in CDV1 EM configuration file
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceCDV1Property(Map<String, String> propValues){
	    	replaceProp(propValues, CDV_MACHINE_ID,
		                cdv1ConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given Prop-Values Map  in CDV2 EM configuration file
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceCDV2Property(Map<String, String> propValues){
	    	replaceProp(propValues, CDV_MACHINE_ID,
		                cdv2ConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given string in Collector1 EM configuration file	    
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceMoM1Collector1Property(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, MOM1_MACHINE_ID,
		                mom1collector1ConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given string in Standalone EM configuration file
	     * @param message
	     */
	    protected void replaceStanadaloneEMProperty(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, AGENT_MACHINE_ID,
		                standaloneEMConfigFile);	
	    }
    
	    /**
	     * Replaces the specified string with the given string in Tomcat Agent configuration file
	     * @param message
	     */
	    protected void replaceTomcatAgentProperty(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, AGENT_MACHINE_ID,
		            tomcatAgentProfileFile);
	    }
	    
	    /**
	     * Appends given Properties List to the MoM1 EM Config file
	     * @param newProp
	     */
	    protected void appendMoM1Properties(List<String> newProp){
	    	appendProp(newProp , MOM1_MACHINE_ID, mom1ConfigFile);
	    }
	    
	    /**
	     * Appends given Properties List to the MoM2 EM Config file
	     * @param newProp
	     */
	    protected void appendMoM2Properties(List<String> newProp){
	    	appendProp(newProp , MOM2_MACHINE_ID, mom2ConfigFile);
	    }
	    
	    /**
	     * Appends given Properties List to the CDV1 EM Config file
	     * @param newProp
	     */
	    protected void appendCDV1Properties(List<String> newProp){
	    	appendProp(newProp , CDV_MACHINE_ID, cdv1ConfigFile);
	    }
	    
	    /**
	     * Appends given Properties List to the CDV2 EM Config file
	     * @param newProp
	     */
	    protected void appendCDV2Properties(List<String> newProp){
	    	appendProp(newProp , CDV_MACHINE_ID, cdv2ConfigFile);
	    }
	    
	    /**
	     * Checks if all the Collectors of the testbed connected to MOM1
	     */
	    protected void checkCollectorsToMOM1Connectivity() {
	        checkSpecificCollectorToMOMConnectivity(".*" + mom1Collector1Host + "@" + mom1Collector1Port + ".*", cpuMetricExpression,
	            mom1Host, mom1Port, cdv1LibDir);
	        checkSpecificCollectorToMOMConnectivity(".*" + mom2Collector1Host + "@" + mom2Collector1Port + ".*", cpuMetricExpression,
		            mom1Host, mom1Port, cdv1LibDir);
	    }
	    
	    /**
	     * Checks if all the Collectors of the testbed connected to CDV1
	     */
	    protected void checkCollectorsToCDV1Connectivity() {
	        checkSpecificCollectorToMOMConnectivity(".*" + mom1Collector1Host + "@" + mom1Collector1Port + ".*", cpuMetricExpression,
	            cdv1Host, Integer.toString(cdv1Port), cdv1LibDir);
	        checkSpecificCollectorToMOMConnectivity(".*" + mom2Collector1Host + "@" + mom2Collector1Port + ".*", cpuMetricExpression,
	        		cdv1Host, Integer.toString(cdv1Port), cdv1LibDir);
	    }
	    	    
	    /** Enabled the debug mode of Logging for MoM EM
	     * 
	     */	    
	    protected void enableMoM1DebugLog(){
	    	replaceMoM1Property("log4j.logger.Manager=INFO",
	                "log4j.logger.Manager=DEBUG");
	    }
	    
	    /** Enabled the debug mode of Logging for CDV EM
	     * 
	     */	    
	    protected void enableCDV1DebugLog(){
	    	replaceCDV1Property("log4j.logger.Manager=INFO",
	                "log4j.logger.Manager=DEBUG");
	    }
	    
	    /*
	     * This creates a domain on CDV with FULL permissions to admin user with the 
	     * specified domain name and Agent mapping
	     */
	    protected void createCustomDomainCDV1(String domainName, String agentMapping){
			List<String> args = new ArrayList<String>();
			args.add(cdv1DomainsXmlFile);
			args.add(domainName);
			args.add(agentMapping);
			LOGGER.info("Creating a Custom Domain for CDV");
			XMLModifierFlowContext modifyXML =
					new XMLModifierFlowContext.Builder().arguments(args)
					.methodName("createCustomDomain").build();
			runFlowByMachineId(envProperties.getMachineIdByRoleId(CDV1_ROLE_ID),
					XMLModifierFlow.class, modifyXML);
        
	    }
	    
	    /*
	     * This creates a domain on MoM with FULL permissions to admin user with the 
	     * specified domain name and Agent mapping
	     */
	    protected void createCustomDomainMoM1(String domainName, String agentMapping){
			List<String> args = new ArrayList<String>();
			args.add(mom1DomainsXmlFile);
			args.add(domainName);
			args.add(agentMapping);
			LOGGER.info("Creating a Custom Domain for MOM");
			XMLModifierFlowContext modifyXML =
					new XMLModifierFlowContext.Builder().arguments(args)
					.methodName("createCustomDomain").build();
			runFlowByMachineId(envProperties.getMachineIdByRoleId(MOM1_ROLE_ID),
					XMLModifierFlow.class, modifyXML);
        
	    }
	    
	    /*
	     * This creates a domain on Collector with FULL permissions to admin user with the 
	     * specified domain name and Agent mapping
	     */
	    protected void createCustomDomainMoM1Collector1(String domainName, String agentMapping){
			List<String> args = new ArrayList<String>();
			args.add(mom1Collector1DomainsXmlFile);
			args.add(domainName);
			args.add(agentMapping);
			LOGGER.info("Creating a Custom Domain for MOM");
			XMLModifierFlowContext modifyXML =
					new XMLModifierFlowContext.Builder().arguments(args)
					.methodName("createCustomDomain").build();
			runFlowByMachineId(envProperties.getMachineIdByRoleId(MOM1_COL1_ROLE_ID),
					XMLModifierFlow.class, modifyXML);
        
	    }
	    
	    protected void backupConfigs() {
	        roleIds.clear();
	        roleIds.add(MOM1_ROLE_ID);
	        roleIds.add(MOM1_COL1_ROLE_ID);
	        roleIds.add(MOM2_ROLE_ID);
	        roleIds.add(MOM2_COL1_ROLE_ID);
	        roleIds.add(CDV1_ROLE_ID);
	        roleIds.add(CDV2_ROLE_ID);
	        roleIds.add(STANDALONE_EM_ROLE_ID);
	        roleIds.add(TOMCAT_ROLE_ID);
	        backupConfigDir(roleIds);
	    }

	    protected void revertConfigs(String testCaseId) {
	        roleIds.clear();
	        roleIds.add(MOM1_ROLE_ID);
	        roleIds.add(MOM1_COL1_ROLE_ID);
	        roleIds.add(MOM2_ROLE_ID);
	        roleIds.add(MOM2_COL1_ROLE_ID);
	        roleIds.add(CDV1_ROLE_ID);
	        roleIds.add(CDV2_ROLE_ID);
	        roleIds.add(STANDALONE_EM_ROLE_ID);
	        roleIds.add(TOMCAT_ROLE_ID);
	        renameDirWithTestCaseId(roleIds, testCaseId);
	        restoreConfigDir(roleIds);
	    }
	    
	    protected void revertConfigAndRenameLogsWithTestId(String testCaseId, List<String> roleIds) {
	        renameDirWithTestCaseId(roleIds, testCaseId);
	        restoreConfigDir(roleIds);
	    }

	    protected void revertConfigAndRenameLogsWithTestId(String testCaseId) {
	    	roleIds.clear();
	    	 roleIds.add(MOM1_ROLE_ID);
		     roleIds.add(MOM1_COL1_ROLE_ID);
		     roleIds.add(MOM2_ROLE_ID);
		     roleIds.add(MOM2_COL1_ROLE_ID);
		     roleIds.add(CDV1_ROLE_ID);
		     roleIds.add(CDV2_ROLE_ID);
		     roleIds.add(STANDALONE_EM_ROLE_ID);
		     roleIds.add(TOMCAT_ROLE_ID);
	        revertConfigAndRenameLogsWithTestId(testCaseId, roleIds);
	    }
	    
	    private String getEMInstallLogFile(){
    		
    		String filename = null;  
    		
    		try {
				  		
				File emInstallDir = new File(cdv1InstallDir + "/install");
				if(!emInstallDir.isDirectory()){
					LOGGER.info("No Install directory in the given EM Home");
					return filename;
				}
				
				LOGGER.info("Searching the install dir contents for the file");
				
				File[] emInstallDirContents = emInstallDir.listFiles();
				for (File file : emInstallDirContents) {
					
					if (!file.isDirectory()) {
						
						if(file.getName().startsWith("Introscope_")){
							filename = file.getName();
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return filename;
    	}


}
