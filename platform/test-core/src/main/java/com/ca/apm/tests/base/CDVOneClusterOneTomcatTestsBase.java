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

public class CDVOneClusterOneTomcatTestsBase extends BaseAgentTest {
	
	private static final Logger LOGGER = LoggerFactory
	        .getLogger(CDVOneClusterOneTomcatTestsBase.class);
	
	
	protected CLWCommons clw = new CLWCommons();
	protected EmUtils emUtils = utilities.createEmUtils();
	protected XMLUtil xmlutil = new XMLUtil();

    protected int cdvPort;
    protected String cdvHost;
    protected String momHost;
    protected String collector1Host;
 
    protected String agentHost;
    protected String agentPort;

    protected String user;
    protected String password;

    protected String cdvLibDir;
    protected String cdvConfigDir;
    protected String cdvlogFile;
    protected String cdvConfigFile;
    protected String cdvInstallLogFile;
    protected String cdvDomainsXmlFile;
    protected String momInstallLogFile;
    protected String collector1InstallLogFile;
    protected String momDomainsXmlFile;
    protected String collector1DomainsXmlFile;
    protected String collector1ConfigDir;
    
    
	protected String testCaseId;
	protected String testCaseName;
	protected ArrayList<String> rolesInvolved = new ArrayList<String>();
	
	// copied
	    public String momHostIP;
	    public final String momConfigFile;
	    public final String collector1ConfigFile;
	    public final String AgentExpression;
	    public final String tomcatAgentExpression;
	    public final String MetricExpression;
	    public final String loadBalanceFile;
	    public final String momlogFile;
	    public final String momLibDir;
	    public final String collector1logFile;
	    public final String loadBalanceFile_Backup;
	    public final String collector1Port;
	    public String collector1HostIP;
	    public final String tomcatHost;	    
	    public final String clwJarFileLoc;
	    public final String momWebPort;
	    public final String momSecureWebPort;
	    public final String momConfigDir;
	    public final String c1WebPort;
	    public final String momPort;
	    public final String tomcatAgentProfileFile;
	    public final String tomcatAgentProfileFile_backup;
	    public final String emSecureWebPort;
	    public final String configFileMom_backup;
	    public final String configFileC1_backup;
	    public final String tomcatAgentExp;
	    public String tomcatAgentLogFile;
	    protected ApmbaseUtil apmbaseutil;
	    public final String momlogDir;
	    protected List<String> roleIds = new ArrayList<String>();
	    protected String cdvInstallDir;
	    protected String momInstallDir;
	    protected String collector1InstallDir;

	    public CDVOneClusterOneTomcatTestsBase() {
	    	
	    	cdvInstallDir =
		            envProperties.getRolePropertyById(CDV_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	momInstallDir =
		            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	collector1InstallDir =
		            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    	cdvHost = envProperties.getMachineHostnameByRoleId(CDV_ROLE_ID);
	        cdvPort =
	            Integer.parseInt(envProperties.getRolePropertiesById(CDV_ROLE_ID).getProperty("emPort"));
	        momHost = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
	        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
	        cdvLibDir =
	            envProperties.getRolePropertyById(CDV_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
	        cdvConfigDir =
	            envProperties.getRolePropertyById(CDV_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
	        cdvDomainsXmlFile = cdvConfigDir + "/domains.xml"; 
	        agentHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);
	        agentPort =
	            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
	                DeployTomcatFlowContext.ENV_TOMCAT_PORT);
	        user = ApmbaseConstants.emUser;
	        password = ApmbaseConstants.emPassw;
	        
	        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
	        AgentExpression = "\".*\\|.*\\|.*\"";
	        tomcatAgentExp = ".*Tomcat.*";
	        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
	        MetricExpression = ".*CPU.*";
	        momPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emPort");
	        collector1Port = envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emPort");
	        momSecureWebPort = ApmbaseConstants.emSecureWebPort;
	        momWebPort = envProperties.getRolePropertiesById(MOM_ROLE_ID).getProperty("emWebPort");
	        c1WebPort =
	            envProperties.getRolePropertiesById(COLLECTOR1_ROLE_ID).getProperty("emWebPort");


	        collector1Host = envProperties.getMachineHostnameByRoleId(COLLECTOR1_ROLE_ID);
	        
	        
	        try {
	        	collector1HostIP= returnIPforGivenHost(collector1Host);
 			} catch (IOException e) {				
 				e.printStackTrace();
 			}

	        tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);        
	 

	        loadBalanceFile =
	            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
	                + "/loadbalancing.xml";
	        loadBalanceFile_Backup =
	            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
	                + "/loadbalancing_backup.xml";
	        momHost = envProperties.getMachineHostnameByRoleId(MOM_ROLE_ID);
	        
	        try {
	 				momHostIP= returnIPforGivenHost(momHost);
	 			} catch (IOException e) {				
	 				e.printStackTrace();
	 			}
	        
	        momLibDir =
	            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);

	        momlogFile =
	            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        momConfigDir= envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
	        momlogDir =
	            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_DIR);
	        momInstallLogFile = momInstallDir + "/install/" + getEMInstallLogFile();
	        momDomainsXmlFile = momConfigDir + "/domains.xml";
	        collector1logFile =
		            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        collector1InstallLogFile = collector1InstallDir + "/install/" + getEMInstallLogFile();
	        collector1ConfigDir = envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
	        collector1DomainsXmlFile = collector1ConfigDir + "/domains.xml";
	        cdvlogFile = 
	        		envProperties.getRolePropertyById(CDV_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        cdvConfigFile =
		            envProperties.getRolePropertyById(CDV_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        cdvInstallLogFile = cdvInstallDir + "/install/" + getEMInstallLogFile();
	        
	        momConfigFile =
	            envProperties.getRolePropertyById(MOM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        configFileMom_backup = momConfigFile + "_backup";
	        collector1ConfigFile =
	            envProperties.getRolePropertyById(COLLECTOR1_ROLE_ID,
	                DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        configFileC1_backup = collector1ConfigFile + "_backup";
	        
	        clwJarFileLoc = momLibDir + "CLWorkstation.jar";
	        tomcatAgentProfileFile =
	            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
	                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
	                + "/wily/core/config/IntroscopeAgent.profile";
	        tomcatAgentProfileFile_backup = tomcatAgentProfileFile + "_backup";

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
	        machines.add(EM_MACHINE_ID);
	        machines.add(CDV_MACHINE_ID);
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
	    	emRoles.add(MOM_ROLE_ID);
	    	emRoles.add(COLLECTOR1_ROLE_ID);
	    	
	    	return emRoles;
	    	
	    }
	    /**
	     * Stops all the EMs of the testbed
	     */
	    protected void stopEMServices() {
	    	stopCollector();
	    	stopMoM();
	        stopCDV();
	        harvestWait(10);
	    }

	    
	    /**
	     * Stops all the EMs and Agents of the testbed
	     */

	    protected void stopServices() {
	    	stopAgent();
	        stopEMServices();
	        harvestWait(5);
	    }
	    
	    /**
	     * Starts the Collector EMs of the testbed
	     */

	    protected void startEMCollectors() {
	    	startEM(COLLECTOR1_ROLE_ID);
	    }
	    
	    
	    
	    /**
	     * Starts the CDV EM of the testbed
	     */
	    protected void startCDV() {
	        try {
	            startEM(CDV_ROLE_ID);
	        	LOGGER.info("CDV is started");
	        } catch (Exception e) {
	        	LOGGER.error("CDV failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Starts the MoM EM of the testbed
	     */
	    protected void startMoM() {
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
	     * Starts the Collector EM of the testbed
	     */
	    protected void startCollector() {
	        try {
	            startEM(COLLECTOR1_ROLE_ID);
	        	LOGGER.info("Collector is started");
	        } catch (Exception e) {
	        	LOGGER.error("Collector failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Stops the CDV EM of the testbed
	     */
	    protected void stopCDV() {	        
	        stopEM(CDV_ROLE_ID);
	        stopEMServiceFlowExecutor(CDV_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the MoM EM of the testbed
	     */
	    protected void stopMoM() {	        
	    	stopCollectorEM(CDV_ROLE_ID, MOM_ROLE_ID);
	        stopEMServiceFlowExecutor(EM_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Stops the Collector EM of the testbed
	     */
	    protected void stopCollector() {	        
	    	stopCollectorEM(CDV_ROLE_ID, COLLECTOR1_ROLE_ID);
	        stopEMServiceFlowExecutor(EM_MACHINE_ID);
	        harvestWait(10);
	    }
	    
	    /**
	     * Starts and Stops the CDV
	     */
	    protected void startStopCDV(){
	    	startCDV();
	    	stopCDV();
	    }
	    
	    /**
	     * Starts and Stops the MoM
	     */
	    protected void startStopMoM(){
	    	startMoM();
	    	stopMoM();
	    }
	    
	    /**
	     * Starts and Stops the Collector
	     */
	    protected void startStopCollector(){
	    	startCollector();
	    	stopCollector();
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
	    
	    
	    /**
	     * Starts the Tomcat Agent of the testbed
	     */
	    protected void startAgent() {
	    	startTomcatAgent();
            
	    }
	    
	    /**
	     * Stops the Tomcat and JBoss Agent of the testbed
	     */
	    protected void stopAgent() {
	    	stopTomcatAgent();
	    }
	    
	    protected void startCluster(){
	    	startCollector();
	    	startMoM();
	    }
	    
	    
	    protected void stopCluster(){
	    	stopCollector();
	    	stopMoM();
	    }
	    /**
	     * Starts all the components EMs and Agents of the testbed
	     */
	    protected void startTestBed(){
	    	startEMCollectors();
	    	startMoM();
	    	startCDV();
	    	startAgent();   	
	    	LOGGER.info("All the components of the Testbed are started");
	    }
	    
	    /**
	     * Stops all the components EMs and Agent of the testbed
	     */
	    protected void stopTestBed(){
	    	stopEMServices();
	    	stopAgent();   	
	    }

	    /**
	     * Starts the EMs of the testbed
	     */
	    protected void startEMServices(){
	    	startEMCollectors();
	    	startMoM();
	    	startCDV();
	    }
	    
	    /**
	     * Restarts the MoM EM of the testbed
	     */
	    protected void restartMOM(){
	    	stopMoM();
	    	startMoM();
	    }
	    
	    /**
	     * Restarts the Collector1 EM of the testbed
	     */
	    protected void restartCollector(){
	    	stopCollector();
	    	startCollector();
	    }
	
	    
	    /**
	     * Restarts the TomcatAgent of the Testbed
	     */
	    protected void restartTomcatAgent(){
	    	stopTomcatAgent();
	    	startTomcatAgent();
	    }
	    
	    /**
	     * Checks the CDV log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkCDVLogForMsg(String message){
	    	checkLogForMsg(envProperties, CDV_MACHINE_ID, cdvlogFile, message);
	    }
	    
	    protected void checkCDVLogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, CDV_MACHINE_ID, cdvlogFile, message);
	    }
	    
	    protected void checkCDVInstallLogForMsg(String message){
	    	checkLogForMsg(envProperties, CDV_MACHINE_ID, cdvInstallLogFile, message);
	    }
	    
	    protected void checkCDVInstallLogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, CDV_MACHINE_ID, cdvInstallLogFile, message);
	    }
	    
	    
	    protected void checkMoMInstallLogForMsg(String message){
	    	checkLogForMsg(envProperties, EM_MACHINE_ID, momInstallLogFile, message);
	    }
	    
	    /**
	     * Checks the MoM log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkMoMLogForMsg(String message){
	    	checkLogForMsg(envProperties, EM_MACHINE_ID, momlogFile, message);
	    }
	    
	    /**
	     * Checks the MoM log of the testbed for the specified string message
	     * is not available
	     * @param message
	     */
	    protected void checkMoMLogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, EM_MACHINE_ID, momlogFile, message);
	    }
	    
	    protected void checkColl1InstallLogForMsg(String message){
	    	checkLogForMsg(envProperties, EM_MACHINE_ID, collector1InstallLogFile, message);
	    }
	    
	    /**
	     * Checks the Collector1 log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkColl1LogForMsg(String message){
	    	checkLogForMsg(envProperties, EM_MACHINE_ID, collector1logFile, message);	
	    }

	    protected void checkColl1LogForNoMsg(String message){
	    	checkLogForNoMsg(envProperties, EM_MACHINE_ID, collector1logFile, message);	
	    }
	    
	    /**
	     * Checks the Agent log of the testbed for the specified string message
	     * @param message
	     */
	    protected void checkTomcatAgentLogForMsg(String message){
	    	checkLogForMsg(envProperties, AGENT_MACHINE_ID, tomcatAgentLogFile, message);	
	    	
	    }	
	    
	    /**
	     * Replaces the specified string with the given string in MoM EM configuration file
	     * @param message
	     */
	    protected void replaceMoMProperty(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, EM_MACHINE_ID,
		                momConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given string in CDV EM configuration file
	     * @param message
	     */
	    protected void replaceCDVProperty(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, CDV_MACHINE_ID,
		                cdvConfigFile);	
	    }
	    
	    /**
	     * Replaces the specified string with the given string in Collector1 EM configuration file	    
	     * @param findStr
	     * @param replaceStr
	     */
	    protected void replaceCollector1Property(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, EM_MACHINE_ID,
		                collector1ConfigFile);	
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
	     * Checks if the Collector of the testbed connected to MOM
	     */
	    protected void checkCollectorsToMOMConnectivity() {
	        checkSpecificCollectorToMOMConnectivity(".*" + collector1Host + ".*", MetricExpression,
	            momHost, momPort, momLibDir);
	    }
	    	    
	    /** Enabled the debug mode of Logging for MoM EM
	     * 
	     */	    
	    protected void enableMoMDebugLog(){
	    	replaceMoMProperty("log4j.logger.Manager=INFO",
	                "log4j.logger.Manager=DEBUG");
	    }
	    
	    /** Enabled the debug mode of Logging for CDV EM
	     * 
	     */	    
	    protected void enableCDVDebugLog(){
	    	replaceCDVProperty("log4j.logger.Manager=INFO",
	                "log4j.logger.Manager=DEBUG");
	    }
	    
	    /*
	     * This creates a domain on CDV with FULL permissions to admin user with the 
	     * specified domain name and Agent mapping
	     */
	    protected void createCustomDomainCDV(String domainName, String agentMapping){
			List<String> args = new ArrayList<String>();
			args.add(cdvDomainsXmlFile);
			args.add(domainName);
			args.add(agentMapping);
			LOGGER.info("Creating a Custom Domain for CDV");
			XMLModifierFlowContext modifyXML =
					new XMLModifierFlowContext.Builder().arguments(args)
					.methodName("createCustomDomain").build();
			runFlowByMachineId(envProperties.getMachineIdByRoleId(CDV_ROLE_ID),
					XMLModifierFlow.class, modifyXML);
        
	    }
	    
	    /*
	     * This creates a domain on MoM with FULL permissions to admin user with the 
	     * specified domain name and Agent mapping
	     */
	    protected void createCustomDomainMoM(String domainName, String agentMapping){
			List<String> args = new ArrayList<String>();
			args.add(momDomainsXmlFile);
			args.add(domainName);
			args.add(agentMapping);
			LOGGER.info("Creating a Custom Domain for MOM");
			XMLModifierFlowContext modifyXML =
					new XMLModifierFlowContext.Builder().arguments(args)
					.methodName("createCustomDomain").build();
			runFlowByMachineId(envProperties.getMachineIdByRoleId(MOM_ROLE_ID),
					XMLModifierFlow.class, modifyXML);
        
	    }
	    
	    /*
	     * This creates a domain on Collector with FULL permissions to admin user with the 
	     * specified domain name and Agent mapping
	     */
	    protected void createCustomDomainCollector1(String domainName, String agentMapping){
			List<String> args = new ArrayList<String>();
			args.add(collector1DomainsXmlFile);
			args.add(domainName);
			args.add(agentMapping);
			LOGGER.info("Creating a Custom Domain for MOM");
			XMLModifierFlowContext modifyXML =
					new XMLModifierFlowContext.Builder().arguments(args)
					.methodName("createCustomDomain").build();
			runFlowByMachineId(envProperties.getMachineIdByRoleId(COLLECTOR1_ROLE_ID),
					XMLModifierFlow.class, modifyXML);
        
	    }
	    
	    protected void backupConfigs() {
	        roleIds.clear();
	        roleIds.add(MOM_ROLE_ID);
	        roleIds.add(COLLECTOR1_ROLE_ID);
	        roleIds.add(CDV_ROLE_ID);
	        roleIds.add(TOMCAT_ROLE_ID);
	        backupConfigDir(roleIds);
	    }

	    protected void revertConfigs(String testCaseId) {
	        roleIds.clear();
	        roleIds.add(MOM_ROLE_ID);
	        roleIds.add(COLLECTOR1_ROLE_ID);
	        roleIds.add(CDV_ROLE_ID);
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
	        roleIds.add(MOM_ROLE_ID);
	        roleIds.add(COLLECTOR1_ROLE_ID);
	        roleIds.add(CDV_ROLE_ID);
	        roleIds.add(TOMCAT_ROLE_ID);
	        revertConfigAndRenameLogsWithTestId(testCaseId, roleIds);
	    }
	    
	    private String getEMInstallLogFile(){
    		
    		String filename = null;  
    		
    		try {
				  		
				File emInstallDir = new File(cdvInstallDir + "/install");
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
