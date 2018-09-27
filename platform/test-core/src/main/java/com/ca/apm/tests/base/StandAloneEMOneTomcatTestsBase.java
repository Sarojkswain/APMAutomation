package com.ca.apm.tests.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;

public class StandAloneEMOneTomcatTestsBase extends AgentControllabilityConstants {
	
	private static final Logger LOGGER = LoggerFactory
	        .getLogger(StandAloneEMOneTomcatTestsBase.class);
	    public final String EMHost;
	    public String EMHostIP;
	    public final String emLibDir;
	    public final String configFileEM;
	    public final String user;
	    public final String password;
	    public final String AgentExpression;
	    public final String tomcatAgentExpression;
	    public final String MetricExpression;
	    public final String loadBalanceFile;
	    public final String EMlogFile;
	    public final String loadBalanceFile_Copy;
	    public final String tomcatHost;	    
	    public final String clwJarFileLoc;
	    public final String emPort;
	    public final String tomcatagentProfileFile;
	    public final String tomcatagentProfileFile_backup;
	    public final String emSecureWebPort;
	    public final String tomcatAgentExp;
	    public String tomcatAgentLogFile;
	    protected final String emWebPort;
	    protected final String configFileEm_backup;

	    public StandAloneEMOneTomcatTestsBase() {
	        emSecureWebPort = ApmbaseConstants.emSecureWebPort;
	        AgentExpression = "\".*\\|.*\\|.*\"";
	        tomcatAgentExp = ".*Tomcat.*";
	        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
	        MetricExpression = ".*CPU.*";
	        emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort");
	        emWebPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emWebPort");
	        tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);        
	        

	        loadBalanceFile =
	            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
	                + "/loadbalancing.xml";
	        loadBalanceFile_Copy =
	            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
	                + "/loadbalancing_1.xml";
	        EMHost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
	        
	        try {
	 				EMHostIP= returnIPforGivenHost(EMHost);
	 			} catch (IOException e) {				
	 				e.printStackTrace();
	 			}
	        
	        emLibDir =
	            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);

	        EMlogFile =
	            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
	        
	        configFileEM =
	            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
	        configFileEm_backup = configFileEM + "backup";
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
	    }

	    @BeforeSuite(alwaysRun = true)
	    public void initialize() {
	        List<String> machines = new ArrayList<String>();
	        machines.add(EM_MACHINE_ID);
	        machines.add(TOMCAT_MACHINE_ID);
	        syncTimeOnMachines(machines); 
	        
	        backupFile(configFileEM, configFileEM+"backup", EM_MACHINE_ID);
	        backupFile(tomcatagentProfileFile, tomcatagentProfileFile_backup, TOMCAT_MACHINE_ID);

	    }
	    
	    /**
	     * Stops all the EMs of the testbed
	     */
	    public void stopEMServices() {
	        stopEM(EM_ROLE_ID);
	        stopEMServiceFlowExecutor(EM_MACHINE_ID);
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
	     * Starts the EM of the testbed
	     */
	    public void startEM() {
	        try {
	            startEM(EM_ROLE_ID);
	        	LOGGER.info("EM is started");
	        } catch (Exception e) {
	        	LOGGER.error("EM failed to start");
	        	Assert.assertTrue(false);
	        	e.printStackTrace();	            
	        }
	    }
	    
	    /**
	     * Stops the EM EM of the testbed
	     */
	    public void stopEM() {	        
	        stopEM(EM_ROLE_ID);
	        stopEMServiceFlowExecutor(EM_MACHINE_ID);
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
	    public void startTestBed(){
	    	startEM();
	    	startAgent();   	
	    	LOGGER.info("All the components of the Testbed are started");
	    }
	    
	    /**
	     * Stops all the components EMs and Agent of the testbed
	     */
	    public void stopTestBed(){
	    	stopEMServices();
	    	stopAgent();   	
	    }

	    /**
	     * Starts the EMs of the testbed
	     */
	    public void startEMServices(){
	    	startEM();
	    }
	    
	    /**
	     * Restarts the EM EM of the testbed
	     */
	    public void restartEM(){
	    	restartEM(EM_ROLE_ID);
	    }
	    
	    /**
	     * Checks the EM log of the testbed for the specified string message
	     * @param message
	     */
	    public void checkEMLogForMsg(String message){
	    	checkLogForMsg(envProperties, EM_MACHINE_ID, EMlogFile, message);
	    }
	    
	    /**
	     * Checks the Agent log of the testbed for the specified string message
	     * @param message
	     */
	    public void checkAgentLogForMsg(String message){
	    	checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile, message);	
	    	
	    }	
	    
	    /**
	     * Replaces the specified string with the given string in EM EM configuration file
	     * @param message
	     */
	    public void replaceEMProperty(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, EM_MACHINE_ID,
		                configFileEM);	
	    }
	    
	    /**
	     * Replaces the specified string with the given string in Agent configuration file
	     * @param message
	     */
	    public void replaceAgentProperty(String findStr, String replaceStr){
	    	replaceProp(findStr,
	    			replaceStr, TOMCAT_MACHINE_ID,
		            tomcatagentProfileFile);
	    }
	    
	    public void revertConfigAndRenameLogsWithTestId(String testCaseId) {
	        
	        revertFile(configFileEm_backup, configFileEM, EM_MACHINE_ID);
            revertFile(tomcatagentProfileFile_backup, tomcatagentProfileFile, TOMCAT_MACHINE_ID);
            renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID, testCaseId);
            renameLogWithTestCaseID(EMlogFile, EM_MACHINE_ID, testCaseId);
	      //  renameDirWithTestCaseId(roleIds, testCaseId);
	     //   restoreConfigDir(roleIds);
	    }
	    

}
