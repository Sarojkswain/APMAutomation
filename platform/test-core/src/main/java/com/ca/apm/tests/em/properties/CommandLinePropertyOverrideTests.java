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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;


public class CommandLinePropertyOverrideTests extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLinePropertyOverrideTests.class);    	
	CLWCommons clw = new CLWCommons();
	TestUtils tutil = new TestUtils();
	ApmbaseUtil apmutil = new ApmbaseUtil();
	private final String emMachineId;
    private final String emRoleId; 
	private final String emHost;
	private final int emPort;     
    private final String emLibDir;
    private final String eminstallDir;
    private final String emconfigFile;
    private final String emconfigFile_bckup;
    private final String emlaxfilename;
    private final String emlaxFile;
    private final String emlaxFile_bckup;
    private final String emconfigDir;
    private final String laxadditionalProperty;
    private final String user;
    private final String password;
    private final String guestuser;
    private final String guestpwd;
    private final String emLogFile;    
    private final String tomcatMachineId;
    private final String tomcatRoleId; 
    private final String tomcatHost;
    private final String tomcatConfigDir;
    private final String tomcatAgentProfile;
    private final String tomcatAgentProfile_bckup;
    private final String tomcatLogFile;
    private final String tomcatAgentExp; 
    private final String tomcatprocessname;
    private final String tomcatagentname;
    private String testcaseId;
    private String testCaseNameIDPath;
    private String msg,tempResult1;
    private String laxadditionalValue,filetosend;
    private int customport;
    private boolean isFileExists;
    private String newlogpath1,newlogpath2;     
    List<String> clwoutput;

    public CommandLinePropertyOverrideTests() {
    	
    	emMachineId = EM_MACHINE_ID;
    	emRoleId = EM_ROLE_ID; 
    	emHost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
    	emPort = Integer.parseInt(envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort"));
    	emLibDir = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
    	user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;
		guestuser = ApmbaseConstants.guestUser;
		guestpwd = ApmbaseConstants.guestPassw;
		emlaxfilename = ApmbaseConstants.EM_LAX_FILE;
		laxadditionalProperty = ApmbaseConstants.laxAdditionalProperty;
		eminstallDir = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    	emconfigFile = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);    	
    	emconfigFile_bckup = emconfigFile + "_backup";    	
    	emlaxFile = eminstallDir + "/" + emlaxfilename;
    	emlaxFile_bckup = emlaxFile + "_backup";
    	emLogFile = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
    	emconfigDir = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR);    	
    	tomcatMachineId = TOMCAT_MACHINE_ID;
    	tomcatRoleId = TOMCAT_ROLE_ID;
    	tomcatHost = envProperties.getMachineHostnameByRoleId(TOMCAT_ROLE_ID);    	
    	tomcatConfigDir = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config";
    	tomcatAgentProfile = tomcatConfigDir + "/IntroscopeAgent.profile";
		tomcatAgentProfile_bckup = tomcatAgentProfile + "_backup";
		tomcatLogFile = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";
        tomcatAgentExp = "(.*)Tomcat(.*)"; 
        tomcatprocessname = "Tomcat";
        tomcatagentname = "Tomcat Agent";
        filetosend = "domains.xml";       
		testcaseId = "null";
		testCaseNameIDPath = "null";
		laxadditionalValue = "null";		;
		clwoutput = new ArrayList<String>();
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        
    	// backup em config file
	   	backupFile(emconfigFile, emconfigFile_bckup, emMachineId);
		backupFile(emlaxFile, emlaxFile_bckup, emMachineId);
		backupFile(tomcatAgentProfile,tomcatAgentProfile_bckup,tomcatMachineId);
    	
    	// sync time on EM and Agent machine
    	List<String> machines = new ArrayList<String>();
        machines.add(EM_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);         
        
    }  
      
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_298755_Command_Line_Property_Override__EM_Port() {
    	
    	try {
    	testcaseId="298755";
    	testCaseNameIDPath = "verify_ALM_298755_verify_Command_Line_Property_Override__EM_Port";    	
    	testCaseStart(testCaseNameIDPath);
    	customport = 5002;
    	laxadditionalValue = "-Dintroscope.enterprisemanager.port.channel1="+customport;    	
    	tutil.setadditionalargtoproperty(emlaxfilename,eminstallDir,laxadditionalProperty,laxadditionalValue);
    	startEM(emRoleId);
    	harvestWait(60);
    	String agentExpression =
				"(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
    	String metricExpression =
            	"Enterprise Manager:Port";
    	tempResult1 = clw.getLatestMetricValue(user, password,
    			agentExpression, metricExpression, emHost, customport, emLibDir);         
        Assert.assertTrue("EM Port has not been picked from the lax file",
                tempResult1.equals("String:::"+5002));
    	} catch(Exception e)
    	{
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);   		
    	} 
    	finally {
    		stopEM(emRoleId); 
        	stopEMServiceFlowExecutor(emMachineId); 
        	renameFile(emLogFile, emLogFile + "_" + testcaseId, emMachineId);    			
    		revertFiles();
    		testCaseEnd(testCaseNameIDPath);    		
    	}
    	
    }
    
    /* Test Case fails currently because of the open Defect DE202579
     * Also the warn message which is being checked will be changed after the fix is available  */
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_298756_Command_Line_Property_Override__EM_Log_File_Name_Hot_Config() {    	
    	
    	try { 
    	testcaseId="298756";
    	testCaseNameIDPath = "verify_ALM_298756_verify_Command_Line_Property_Override__EM_Log_File_Name_Hot_Config";    	
    	testCaseStart(testCaseNameIDPath);
    	newlogpath1 = "logs/myEnterpriseManager.log";
    	newlogpath2 = "logs/mySecondEnterpriseManager.log ";
    	laxadditionalValue = "-Dlog4j.appender.logfile.File="+newlogpath1;    	
    	tutil.setadditionalargtoproperty(emlaxfilename,eminstallDir,laxadditionalProperty,laxadditionalValue);
    	startEM(emRoleId);
    	isFileExists = ApmbaseUtil.fileExists(eminstallDir+ "/"+newlogpath1);
    	Assert.assertTrue("EM log is not seen in the newlogpath set in the EM lax file", isFileExists);
    	replaceProp("log4j.appender.logfile.File=logs/IntroscopeEnterpriseManager.log", "log4j.appender.logfile.File="+newlogpath2, emMachineId, emconfigFile);
    	harvestWait(120);    	
    	msg = "[WARN]overridden";   	
    	isKeywordInFile(envProperties, EM_MACHINE_ID,  eminstallDir + "/" + newlogpath1, msg);    	
    	isFileExists = ApmbaseUtil.fileExists(eminstallDir+ "/"+newlogpath2);
    	Assert.assertFalse("EM log file property change has overridden the EM lax file entry and created the new log file", isFileExists);    	   	
    	} catch(Exception e)
    	{
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(emRoleId); 
        	stopEMServiceFlowExecutor(emMachineId); 
        	renameFile(eminstallDir + "/" + newlogpath1,  eminstallDir + "/" + newlogpath1 + "_" + testcaseId, emMachineId);    			
    		revertFiles();
    		testCaseEnd(testCaseNameIDPath);    		
    	}
    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_298757_TT46455_vulnerability_in_secure_EM_Agent_communication() {
    	
    	try {    		
    	testcaseId="298757";
    	testCaseNameIDPath = "verify_ALM_298757_TT46455_vulnerability_in_secure_EM_Agent_communication";    	
    	testCaseStart(testCaseNameIDPath);
    	startEM(emRoleId);    	
    	replaceProp("introscope.agent.remoteagentconfiguration.allowedFiles=domainconfig.xml", "introscope.agent.remoteagentconfiguration.allowedFiles="+filetosend, tomcatMachineId, tomcatAgentProfile);
    	startTomcatAgent(tomcatRoleId);
    	harvestWait(60);
    	clwoutput = clw.sendConfigFiletoAgents(user, password, tomcatAgentExp, emconfigDir+"/"+filetosend, emHost, emPort, emLibDir);
    	msg = "Successfully sent " + filetosend + " to agent " + tomcatHost + "|" + tomcatprocessname + "|" + tomcatagentname + ".";    	
    	Assert.assertTrue("Failure in sending the domains file to the agent for Admin user",clwoutput.contains(msg));
    	clwoutput.clear();
    	deleteFile(tomcatConfigDir+"/"+filetosend, tomcatMachineId);
    	clwoutput = clw.sendConfigFiletoAgents(guestuser, guestpwd, tomcatAgentExp, emconfigDir+"/"+filetosend, emHost, emPort, emLibDir);
       	msg = "Failed to send file: /" + filetosend + " to agent: " + tomcatagentname + " because: com.wily.introscope.permission.PermissionException: User Guest does not have sufficient permissions in domain SuperDomain";
       	Assert.assertTrue("sendConfigFiletoAgents is not working as expected for the Guest user",clwoutput.contains(msg));
    	} catch(Exception e)
    	{
    		Assert.assertTrue(testCaseNameIDPath + " failed because of the Exception "+e,false);
    	}
    	finally {
    		stopEM(emRoleId); 
        	stopEMServiceFlowExecutor(emMachineId); 
        	stopTomcatAgent(tomcatRoleId);
			stopTomcatServiceFlowExecutor(tomcatMachineId);
			renameFile(emLogFile, emLogFile + "_" + testcaseId, emMachineId); 
        	renameFile(tomcatLogFile, tomcatLogFile + "_" + testcaseId, tomcatMachineId);
    		revertFiles();
    		testCaseEnd(testCaseNameIDPath);    		
    	}
   }   
    
    public void revertFiles() {
    	
    	revertFile(emconfigFile, emconfigFile_bckup, emMachineId);
		revertFile(emlaxFile, emlaxFile_bckup, emMachineId);
		revertFile(tomcatAgentProfile,tomcatAgentProfile_bckup,tomcatMachineId);
    }  
}
