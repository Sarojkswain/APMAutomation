/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * Author : BALRA06
 * 
 */
package com.ca.apm.tests.em.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;

public class ConfigurableConfigDirectoryTests extends ConfigurableConfigDirectoryConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableConfigDirectoryTests.class);    	
	CLWCommons clw = new CLWCommons();
	TestUtils tutil = new TestUtils();
	ApmbaseUtil apmutil = new ApmbaseUtil();
	private final String emMachineId;
	private final String em1RoleId;
	private final String em2RoleId;    
	private final String emHost;
	private final int emPort;     
    private final String em1LibDir;
    private final String em2LibDir;
    private final String em1installDir;
    private final String em2installDir;
    private final String em1configFile;
    private final String em1configFile_bckup;         
    private final String em1configDir;    
    private String emconfigfilename;     
    private final String em1LogFile;
    private final String em2LogFile;
    private final String emlaxfilename;
    private final String em1laxFile;
    private final String em1laxFile_bckup;
    private final String em2laxFile;
    private final String em2laxFile_bckup;
    private final String laxadditionalProperty;
    private final String user;
    private final String password; 
    private final String configurableconfigDir;
    private final String configurableconfigPath;
    private final String configurableconfigFile;
    private final String configurableconfigFile_bckup; 
    private final String configdirproperty; 
    private final String configurableLogFile;
    private final String configurablesupportlogfile;
    private final String configurableappenderperffile;
    private final String configurablequerylogfile;
    private final String configurablesmartstordirfile;
    private final String configurablesmartstordirarchivefile;
    private final String configurabletraneventstoragefile;
    private final String metricshutoffconfxmlpath;   
    private String testcaseId;
    private String testCaseNameIDPath;
    private String laxadditionalValue;
    private String msg1,msg2;      
	private String emlogfileprop,supportlogfileprop,appenderperffileprop;
	private String querylogfileprop,smartstordirprop,smartstordirarchiveprop;
	private String traneventstorageprop,empropfilelocation;	
	private String invalidpath; 
	private String em1installWindowsDir;
    List<String> keyWords;
    List<String> properties;
    File emdefaultConfigDir,emConfigurableConfigDir;

    public ConfigurableConfigDirectoryTests() {
    	
    	emMachineId = ConfigurableConfigDirectoryConstants.EM_MACHINE_ID;
    	em1RoleId = ConfigurableConfigDirectoryConstants.EM1_ROLE_ID; 
    	em2RoleId = ConfigurableConfigDirectoryConstants.EM2_ROLE_ID;
    	emHost = envProperties.getMachineHostnameByRoleId(EM1_ROLE_ID);    	
    	emPort = Integer.parseInt(envProperties.getRolePropertiesById(EM1_ROLE_ID).getProperty("emPort"));
    	em1LibDir = envProperties.getRolePropertyById(EM1_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
    	em2LibDir = envProperties.getRolePropertyById(EM2_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
    	user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;		
		em1installDir = envProperties.getRolePropertyById(EM1_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);		 
		em2installDir = envProperties.getRolePropertyById(EM2_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    	em1configFile = envProperties.getRolePropertyById(EM1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);    	
    	em1configFile_bckup = em1configFile + "_backup";    	 
    	emlaxfilename = ApmbaseConstants.EM_LAX_FILE;
    	em1laxFile = em1installDir + "/" + emlaxfilename;
    	em1laxFile_bckup = em1laxFile + "_backup";
    	em2laxFile = em2installDir + "/" + emlaxfilename;
    	em2laxFile_bckup = em2laxFile + "_backup";    	
    	em1LogFile = envProperties.getRolePropertyById(EM1_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
    	em2LogFile = envProperties.getRolePropertyById(EM2_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
    	em1configDir = envProperties.getRolePropertyById(EM1_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);    	
    	emconfigfilename  = ApmbaseConstants.EM_PROPERTIES_FILE;    	
    	laxadditionalProperty = ApmbaseConstants.laxAdditionalProperty; 
    	configdirproperty = ApmbaseConstants.EM_DIR_CONFIG;
        emlogfileprop = ApmbaseConstants.EM_LOG_FILE_PRPTY;
        supportlogfileprop = ApmbaseConstants.SUPPORT_LOG_FILE;
        appenderperffileprop = ApmbaseConstants.APPENDER_PERF_FILE;
        querylogfileprop = ApmbaseConstants.QUERY_LOG_FILE;
        smartstordirprop = ApmbaseConstants.SMARTSTOR_DIR;
        smartstordirarchiveprop = ApmbaseConstants.SMARTSTOR_DIR_ARCHIVE;
        traneventstorageprop = ApmbaseConstants.TRAN_EVENTS_STORAGE;
        empropfilelocation = ApmbaseConstants.EM_PROP_FILE_LOC;
        configurableconfigDir = em1installDir + "/" + "ccdir";
    	configurableconfigPath = configurableconfigDir + empropfilelocation;
    	configurableconfigFile = configurableconfigPath + "/IntroscopeEnterpriseManager.properties";
    	configurableconfigFile_bckup = configurableconfigFile + "_backup";
    	configurableLogFile = configurableconfigDir + "/logs/IntroscopeEnterpriseManager.log";
    	configurablesupportlogfile = configurableconfigDir + "/logs/IntroscopeEnterpriseManagerSupport.log";
    	configurableappenderperffile = configurableconfigDir + "/logs/perflog.txt";
    	configurablequerylogfile =  configurableconfigDir + "/logs/querylog.txt";
    	configurablesmartstordirfile = configurableconfigDir + "/data";
    	configurablesmartstordirarchivefile = configurableconfigDir + "/data/archive";
    	configurabletraneventstoragefile = configurableconfigDir + "/traces";
    	metricshutoffconfxmlpath = configurableconfigPath + "/shutoff/MetricShutoffConfiguration.xml";
    	laxadditionalValue = "-Dintroscope.config=" + configurableconfigPath + " -Dcom.wily.introscope.em.properties=" + configurableconfigPath;    	
    	em1installWindowsDir = em1installDir.replace("\\\\", "\\");
    	testcaseId = "null";
		testCaseNameIDPath = "null";		
		emdefaultConfigDir = new File(em1configDir);
        emConfigurableConfigDir = new File(configurableconfigPath);
        keyWords = new ArrayList<String>();
        properties = new ArrayList<String>();
        
    }        

   @BeforeClass(alwaysRun = true)
    public void initialize() {
        
    	try {
    	LOGGER.info("Creating the ConfigurableConfigDirectory by copying config folder of EM to a new location");
		ApmbaseUtil.copyDirectory(emdefaultConfigDir, emConfigurableConfigDir);
    	} catch(Exception e) {
    		Assert.assertTrue("Copying config folder failed because of the Exception "+e , false);
    	}    	
    	
    	//backing up the em config and lax files
		backupFile(em1configFile, em1configFile_bckup, emMachineId);
		backupFile(configurableconfigFile, configurableconfigFile_bckup, emMachineId);
		backupFile(em1laxFile, em1laxFile_bckup, emMachineId);
		backupFile(em2laxFile, em2laxFile_bckup, emMachineId);         
    }  
      
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_280478_Configurable_Config_Directory_with_single_EM() {  	
    	
    	try {
    	testcaseId="280478";
    	testCaseNameIDPath = "verify_ALM_280478_Configurable_Config_Directory_with_single_EM";    	
    	testCaseStart(testCaseNameIDPath);    	
    	tutil.changePropValwithAbsolutePath(emlogfileprop,configurableLogFile,configurableconfigPath,emconfigfilename);
    	appendPropwithAbsolutePath(emMachineId,configurableconfigFile,configdirproperty,configurableconfigPath);    	    	
    	tutil.setadditionalargtoproperty(emlaxfilename,em1installDir,laxadditionalProperty,laxadditionalValue);
    	startEM(em1RoleId);
    	msg1 = "[INFO] [main] [Manager] Introscope Enterprise Manager started.";
    	checkLogForMsg(envProperties, emMachineId, configurableLogFile, msg1);
    	} catch(Exception e) {
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(em1RoleId); 
        	stopEMServiceFlowExecutor(emMachineId);
        	revertFile(configurableconfigFile, configurableconfigFile_bckup, emMachineId);
    		revertFile(em1laxFile, em1laxFile_bckup, emMachineId);
        	moveFile(configurableLogFile, em1LogFile + "_" + testcaseId, emMachineId);        	
    		testCaseEnd(testCaseNameIDPath);
    	}
    
    }  
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_298224_Configurable_Config_Directory_with_single_EM_Cleanup_for_data_trace() {  
    	try {
        	testcaseId="298224";
        	testCaseNameIDPath = "verify_ALM_298224_Configurable_Config_Directory_with_single_EM_Cleanup_for_data_trace";    	
        	testCaseStart(testCaseNameIDPath); 
        	tutil.changePropValwithAbsolutePath(emlogfileprop,configurableLogFile,configurableconfigPath,emconfigfilename);
        	tutil.changePropValwithAbsolutePath(supportlogfileprop,configurablesupportlogfile,configurableconfigPath,emconfigfilename);
        	tutil.changePropValwithAbsolutePath(appenderperffileprop,configurableappenderperffile,configurableconfigPath,emconfigfilename);
        	tutil.changePropValwithAbsolutePath(querylogfileprop,configurablequerylogfile,configurableconfigPath,emconfigfilename);
        	tutil.changePropValwithAbsolutePath(smartstordirprop,configurablesmartstordirfile,configurableconfigPath,emconfigfilename);
        	tutil.changePropValwithAbsolutePath(smartstordirarchiveprop,configurablesmartstordirarchivefile,configurableconfigPath,emconfigfilename);
        	tutil.changePropValwithAbsolutePath(traneventstorageprop,configurabletraneventstoragefile,configurableconfigPath,emconfigfilename);
        	appendPropwithAbsolutePath(emMachineId,configurableconfigFile,configdirproperty,configurableconfigPath);
        	tutil.setadditionalargtoproperty(emlaxfilename,em1installDir,laxadditionalProperty,laxadditionalValue);
        	startEM(em1RoleId);
        	msg1 = "[INFO] [main] [Manager] Introscope Enterprise Manager started.";
        	checkLogForMsg(envProperties, emMachineId, configurableLogFile, msg1);
        	msg1 = "[ERROR]";
        	try {
        	isKeywordInFile(envProperties, emMachineId, configurableLogFile, msg1);
        	Assert.assertTrue("ERRORS exists in the EM log file",false);
        	} catch(Exception e) {
        		Assert.assertTrue(true);
        	}
    	} catch(Exception e) {
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(em1RoleId); 
        	stopEMServiceFlowExecutor(emMachineId);
        	revertFile(configurableconfigFile, configurableconfigFile_bckup, emMachineId);
    		revertFile(em1laxFile, em1laxFile_bckup, emMachineId);
        	moveFile(configurableLogFile, em1LogFile + "_" + testcaseId, emMachineId);        	
    		testCaseEnd(testCaseNameIDPath);
    	}
    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_280479_Configurable_Config_Directory_with_two_EMs() {  
    	try {
        	testcaseId="280479";
        	testCaseNameIDPath = "verify_ALM_280479_Configurable_Config_Directory_with_two_EMs";    	
        	testCaseStart(testCaseNameIDPath); 
        	tutil.changePropValwithAbsolutePath(emlogfileprop,configurableLogFile,configurableconfigPath,emconfigfilename);
        	appendPropwithAbsolutePath(emMachineId,configurableconfigFile,configdirproperty,configurableconfigPath); 
        	tutil.setadditionalargtoproperty(emlaxfilename,em1installDir,laxadditionalProperty,laxadditionalValue);
        	startEM(em1RoleId);
        	msg1 = "[INFO] [main] [Manager] Introscope Enterprise Manager started.";
        	checkLogForMsg(envProperties, emMachineId, configurableLogFile, msg1);
        	stopEM(em1RoleId); 
        	stopEMServiceFlowExecutor(emMachineId);
        	moveFile(configurableLogFile, em1LogFile + "_em1_" + testcaseId, emMachineId); 
        	tutil.setadditionalargtoproperty(emlaxfilename,em2installDir,laxadditionalProperty,laxadditionalValue);
        	startEM(em2RoleId);
        	checkLogForMsg(envProperties, emMachineId, configurableLogFile, msg1);
    	} catch(Exception e) {
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(em1RoleId);
    		stopEM(em2RoleId);
        	stopEMServiceFlowExecutor(emMachineId);
        	revertFile(configurableconfigFile, configurableconfigFile_bckup, emMachineId);
    		revertFile(em1laxFile, em1laxFile_bckup, emMachineId);
    		revertFile(em2laxFile, em2laxFile_bckup, emMachineId);
        	moveFile(configurableLogFile, em2LogFile + "_em2_" + testcaseId, emMachineId);        	
    		testCaseEnd(testCaseNameIDPath);
    	}
    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_372943_Invalid_name_for_config_directory() {  
    	try {
        	testcaseId="372943";
        	testCaseNameIDPath = "verify_ALM_372943_Invalid_name_for_config_directory";    	
        	testCaseStart(testCaseNameIDPath);
        	invalidpath = "config*";
        	properties.clear();
        	properties.add(configdirproperty + "=" + invalidpath);
        	appendProp(properties, emMachineId, em1configFile);
        	startEM(em1RoleId);
        	msg1 = "[ERROR] [main] [Manager] Bad value for system property " + configdirproperty + " : " + em1installDir + "/" + invalidpath + " does not exist or is not a directory.";
        	msg2 = "[ERROR] [main] [Manager] Bad value for system property " + configdirproperty + " : " + em1installWindowsDir + "\\" + invalidpath + " does not exist or is not a directory.";
        	keyWords.clear();
        	keyWords.add(msg1);
        	keyWords.add(msg2);
        	verifyIfAtleastOneKeywordIsInLog(emMachineId, em1LogFile, keyWords);
        	negativescenerioverification();
        	stopEM(em1RoleId);
        	revertFile(em1configFile, em1configFile_bckup, emMachineId); 
        	invalidpath = "";
        	properties.clear();
        	properties.add(configdirproperty + "=" + invalidpath);
        	appendProp(properties, emMachineId, em1configFile);
        	startEM(em1RoleId);        	
        	msg1 = "[ERROR] [main] [Manager] Bad value for system property " + configdirproperty + " : " + em1installDir + invalidpath + " does not exist or is not a directory.";
        	msg2 = "[ERROR] [main] [Manager] Bad value for system property " + configdirproperty + " : " + em1installWindowsDir + invalidpath + " does not exist or is not a directory.";
        	keyWords.clear();
        	keyWords.add(msg1);
        	keyWords.add(msg2);
        	verifyIfAtleastOneKeywordIsInLog(emMachineId, em1LogFile, keyWords);
        	negativescenerioverification(); 

    	} catch(Exception e) {
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(em1RoleId); 
        	stopEMServiceFlowExecutor(emMachineId);
        	revertFile(em1configFile, em1configFile_bckup, emMachineId);    		
        	renameFile(em1LogFile, em1LogFile + "_" + testcaseId, emMachineId);        	
    		testCaseEnd(testCaseNameIDPath);
    	}
    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_298226_Metric_shutoff() {  
    	try {
        	testcaseId="298226";
        	testCaseNameIDPath = "verify_ALM_298226_Metric_shutoff";    	
        	testCaseStart(testCaseNameIDPath);
        	tutil.changePropValwithAbsolutePath(emlogfileprop,configurableLogFile,configurableconfigPath,emconfigfilename);
        	appendPropwithAbsolutePath(emMachineId,configurableconfigFile,configdirproperty,configurableconfigPath);    	    	
        	tutil.setadditionalargtoproperty(emlaxfilename,em1installDir,laxadditionalProperty,laxadditionalValue);
        	startEM(em1RoleId);   
        	harvestWait(60);
        	String metricExpression =
    				"SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Port";
        	clw.turnOffMetrics(user, password, metricExpression, emHost, emPort, em1LibDir);
        	msg1 = "MetricName=\"Port\" Shutoff=\"true\"";
        	isKeywordInFile(envProperties, emMachineId, metricshutoffconfxmlpath, msg1);   	
        	stopEM(em1RoleId);
        	stopEMServiceFlowExecutor(emMachineId);
        	moveFile(configurableLogFile, em1LogFile + "_em1_" + testcaseId, emMachineId);
        	moveFile(metricshutoffconfxmlpath, em1installDir + "/logs/MetricShutoffConfiguration_em1.xml", emMachineId);        	
        	tutil.setadditionalargtoproperty(emlaxfilename,em2installDir,laxadditionalProperty,laxadditionalValue);
        	startEM(em2RoleId);
        	harvestWait(60);
        	clw.turnOffMetrics(user, password, metricExpression, emHost, emPort, em2LibDir);
        	isKeywordInFile(envProperties, emMachineId, metricshutoffconfxmlpath, msg1);
        	moveFile(metricshutoffconfxmlpath, em2installDir + "/logs/MetricShutoffConfiguration_em2.xml", emMachineId);
        	
    	} catch(Exception e) {
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(em1RoleId);
    		stopEM(em2RoleId);
        	stopEMServiceFlowExecutor(emMachineId);
        	revertFile(configurableconfigFile, configurableconfigFile_bckup, emMachineId);
    		revertFile(em1laxFile, em1laxFile_bckup, emMachineId);
    		revertFile(em2laxFile, em2laxFile_bckup, emMachineId);
        	moveFile(configurableLogFile, em2LogFile + "_em2_" + testcaseId, emMachineId);         	     	
    		testCaseEnd(testCaseNameIDPath);
    	}
    }
    
    public void negativescenerioverification() {
    	LOGGER.info("Verifying negative scenerio of ConfigurableConfigDirectory");
    	msg1 = "[INFO] [main] [Manager] Using config directory: " + em1installDir + "/./config";
    	msg2 = "[INFO] [main] [Manager] Using config directory: " + em1installWindowsDir + "\\.\\config";
    	keyWords.clear();
    	keyWords.add(msg1);
    	keyWords.add(msg2);
    	verifyIfAtleastOneKeywordIsInLog(emMachineId, em1LogFile, keyWords);
    	msg1 = "[INFO] [main] [Manager] Introscope Enterprise Manager started.";
    	checkLogForMsg(envProperties, emMachineId, em1LogFile, msg1);
    }
    
}
