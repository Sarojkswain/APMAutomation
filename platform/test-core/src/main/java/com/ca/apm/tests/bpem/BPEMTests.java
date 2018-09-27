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
 */
package com.ca.apm.tests.bpem;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.common.CLWCommons;


public class BPEMTests extends BPEMConstants {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BPEMTests.class);	
	CLWCommons clw = new CLWCommons();		

	private final String momRoleId;
	private final String collector1RoleId;
	private final String collector2RoleId;	
	private final String tomcatRoleId;	
	private String momMachineId;
	private String collector1MachineId;
	private String collector2MachineId;
	private String tomcatMachineId;
	private final String momHost;
	private final String collector1Host;
	private final String collector2Host;
	private final String tomcatAgentHost;
	private final int momPort;
	private final int collector1Port;
	private final int collector2Port;	
	private final String momLibDir;	
	private final String momConfigFile;
	private final String momConfigFile_copy;	
	private final String col1ConfigFile;
	private final String col1ConfigFile_copy;
	private final String col2ConfigFile;
	private final String col2ConfigFile_copy;
	private final String tomcatAgentProfile;
	private final String tomcatAgentProfile_copy;	
	private final String momLogFile;
	private final String col1LogFile;
	private final String col2LogFile;
	private final String tomcatLogFile;	
	private final String user;
	private final String password;
	private final String tomcatagentexpr;
	private String testcaseId;	
	private int collectorPort;
	private String collectorHost,collectorMachineId,colLogFile;		
	private String col1connectmsg,col2connectmsg;
	private String failuretocol1IP,failuretomomhost,failuretoemlimit,tomcatagentdisconnectmsg;
	private String collector1IP,momIP;	
	private String reconnectmsgtocol1IP,reconnectmsgtocol1host,reconnectmsgtomomhost;		
	private String momconnectedmsg1atcol,momconnectedmsg2atcol;
	private String agentcolconnectmsg,agentconnectmsg,agentconnectmsgtocol1;
	private String agenttomomHostconnectmsg,agenttomomIPconnectmsg;
	private String tempResult1;
	private String connectagentmsg;
	List<String> keyWords;
	List<String> collectors_List;
	List<Integer> collector_Port_List;
	List<String> collector_RoleIDs;
	
	public BPEMTests() 
	{

		momRoleId = BPEMConstants.MOM_ROLE_ID;
		collector1RoleId = BPEMConstants.COLLECTOR1_ROLE_ID;
		collector2RoleId = BPEMConstants.COLLECTOR2_ROLE_ID;		
		tomcatRoleId = BPEMConstants.TOMCAT_ROLE_ID;		
		momMachineId = BPEMConstants.MOM_MACHINE_ID;		
		collector1MachineId = BPEMConstants.COLLECTOR1_MACHINE_ID;
		collector2MachineId = BPEMConstants.COLLECTOR2_MACHINE_ID;
		tomcatMachineId = BPEMConstants.COLLECTOR2_MACHINE_ID;
		momHost = envProperties
				.getMachineHostnameByRoleId(BPEMConstants.MOM_ROLE_ID);
		collector1Host = envProperties
				.getMachineHostnameByRoleId(BPEMConstants.COLLECTOR1_ROLE_ID);
		collector2Host = envProperties
				.getMachineHostnameByRoleId(BPEMConstants.COLLECTOR2_ROLE_ID);	
		tomcatAgentHost = envProperties
				.getMachineHostnameByRoleId(BPEMConstants.COLLECTOR2_ROLE_ID);
		momPort = Integer.parseInt(envProperties.getRolePropertiesById(
				momRoleId).getProperty("emPort"));
		collector1Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector1RoleId).getProperty("emPort"));
		collector2Port = Integer.parseInt(envProperties.getRolePropertiesById(
				collector2RoleId).getProperty("emPort"));	
		try {
		collector1IP = returnIPforGivenHost(collector1Host);		
		momIP = returnIPforGivenHost(momHost);
		} catch(Exception e) {
			LOGGER.info("Failed to fetch the Host IP's with Exception",e);
		}		
		momLibDir = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_LIB_DIR);		
		momConfigFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		momConfigFile_copy = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		col1ConfigFile = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		col1ConfigFile_copy = envProperties.getRolePropertyById(
				collector1RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";
		col2ConfigFile = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		col2ConfigFile_copy = envProperties.getRolePropertyById(
				collector2RoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
				+ "/IntroscopeEnterpriseManager.properties.orig";		
		user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;
		tomcatAgentProfile = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile";
		tomcatAgentProfile_copy = envProperties.getRolePropertyById(
				tomcatRoleId, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/IntroscopeAgent.profile.orig";		
		tomcatLogFile = envProperties.getRolePropertyById(tomcatRoleId,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/logs/IntroscopeAgent.log";		
		momLogFile = envProperties.getRolePropertyById(momRoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col1LogFile = envProperties.getRolePropertyById(collector1RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		col2LogFile = envProperties.getRolePropertyById(collector2RoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		tomcatagentexpr = ".*Tomcat.*";
		testcaseId = "null";
		agentcolconnectmsg = "";
		keyWords = new ArrayList<String>();
		collectors_List = new ArrayList<String>();
		collector_Port_List = new ArrayList<Integer>();
		collector_RoleIDs = new ArrayList<String>();
		col1connectmsg = "[INFO] [Collector " + collector1Host + "@" + collector1Port+"] [Manager.Cluster] Connected to the Introscope Enterprise Manager at " + collector1Host + "@" + collector1Port;
		col2connectmsg = "[INFO] [Collector " + collector2Host + "@" + collector2Port+"] [Manager.Cluster] Connected to the Introscope Enterprise Manager at " + collector2Host + "@" + collector2Port;
		failuretocol1IP = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect to the Introscope Enterprise Manager at " + collector1IP;
		failuretomomhost = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect to the Introscope Enterprise Manager at " + momHost;
		failuretoemlimit = "[INFO] [IntroscopeAgent.ConnectionThread] The Agent will continue to attempt to connect to Introscope Enterprise Manager. Further failures will not be logged";
		tomcatagentdisconnectmsg = "[INFO] [PO Async Executor] [Manager.Agent] Disconnected from Agent \"SuperDomain|"+tomcatAgentHost+"|Tomcat|Tomcat Agent\"";
		reconnectmsgtocol1IP = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at "+ collector1IP + ":" + collector1Port + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory";
		reconnectmsgtocol1host = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at "+ collector1Host;
		reconnectmsgtomomhost = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at "+ momHost;
		momconnectedmsg1atcol = "MOM Introscope Enterprise Manager connected: Node=Workstation_0, Address=" + momHost + ".ca.com/" + momIP;
		momconnectedmsg2atcol = "[Manager.SessionBean] User \"WilyMOM\" logged in successfully from host \"Node=Workstation_0, Address=" + momHost + ".ca.com/" + momIP;
		agentconnectmsg = "[Manager.Agent] Connected to Agent \"SuperDomain|"+tomcatAgentHost+"|Tomcat|Tomcat Agent\"";
		agentconnectmsgtocol1 = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to " + collector1Host; 
		agenttomomHostconnectmsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + momHost;
		agenttomomIPconnectmsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + momIP;
	}
	
	@BeforeClass(alwaysRun = true)
	public void BPEMInitialize() {
		
		// set loadbalancing interval property
		replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
				"introscope.enterprisemanager.loadbalancing.interval=120",
				momMachineId, momConfigFile);
				
		// backup mom,col1,con2 config, tomcatagentprofile					
		backupFile(momConfigFile, momConfigFile_copy, momMachineId);
		backupFile(col1ConfigFile, col1ConfigFile_copy, collector1MachineId);
		backupFile(col2ConfigFile, col2ConfigFile_copy, collector2MachineId);				
		backupFile(tomcatAgentProfile, tomcatAgentProfile_copy, tomcatMachineId);				
				
		// sync time on mom and collectors
		List<String> machines = new ArrayList<String>();
		machines.add(momMachineId);
		machines.add(collector1MachineId);
		machines.add(collector2MachineId);				
		syncTimeOnMachines(machines);
		
	}	
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_295398_Bring_cluster_up_and_running_once_after_agent_starts() throws Exception {
					
		testcaseId = "295398";			
		startTomcatAgent(tomcatRoleId);		
		harvestWait(60);
		
		startEM(momRoleId);		
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		
		collectorstomomconnectioncheck();		
		getTomcatAgentConnectedColdetails();
		
	}
	
	@Test(groups = { "Deep" }, enabled = true)		
	public void verify_ALM_295406_Bringing_agent_down_in_cluster_when_connected_to_collector_directly() throws Exception {
		
		testcaseId = "295406";		
		startEM(momRoleId);		
		setTomcatagentManagerURL(collector1IP,collector1Port);
		startTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking if the Tomcat Agent is trying to connect to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, failuretocol1IP);
		
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		collectorstomomconnectioncheck();
		
		LOGGER.info("Checking if the Tomcat Agent is connected to collector1");
		waitForAgentNodes(tomcatagentexpr, collector1Host, collector1Port, momLibDir);
		
		stopTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking for the Tomcat Agent disconnect message in collector1");
		checkLogForMsg(envProperties, collector1MachineId, col1LogFile, tomcatagentdisconnectmsg);
		harvestWait(60);
		
		startTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking if the Tomcat AGent is connected back after the restart");
		waitForAgentNodes(tomcatagentexpr, collector1Host, collector1Port, momLibDir);	
		
	}
	
	@Test(groups = { "Deep" }, enabled = true)		
	public void verify_ALM_295405_Bringing_collector1_down_in_cluster_when_agent_is_connected_to_collector_directly() throws Exception {
		
		testcaseId = "295405";
		startEM(momRoleId);			
		
		setTomcatagentManagerURL(collector1IP,collector1Port);
		startTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking if the Tomcat Agent is trying to connect to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, failuretocol1IP);
		
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();		
		
		LOGGER.info("Checking if the Tomcat Agent is connected to collector1");
		waitForAgentNodes(tomcatagentexpr, collector1Host, collector1Port, momLibDir);
		harvestWait(180);
		
		stopCollectorEM(momRoleId,collector1RoleId);		
		
		LOGGER.info("Checking for the collector reconnect message in the Tomcat Agent");		
		keyWords.clear();
		keyWords.add(reconnectmsgtocol1IP);
		keyWords.add(reconnectmsgtocol1host);		
		verifyIfAtleastOneKeywordIsInLog(tomcatMachineId, tomcatLogFile, keyWords);
		
		LOGGER.info("Checking if the Tomcat Agent is connected to collector2");
		waitForAgentNodes(tomcatagentexpr, collector2Host, collector2Port, momLibDir);
		startEM(collector1RoleId);
		harvestWait(60);
		waitForAgentNodes(tomcatagentexpr, collector2Host, collector2Port, momLibDir);		
		
	}
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_295407_Bringing_MOM_down_in_cluster_when_agent_is_connected_to_MOM() throws Exception {
		
		testcaseId = "295407";
		startEM(momRoleId);		
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();	
		
		setTomcatagentManagerURL(momIP, momPort);		
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);		

		getTomcatAgentConnectedColdetails();      
        
        agentcolconnectmsg = "Connected to " + collectorHost;        
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agentcolconnectmsg);       	
        
        stopEM(momRoleId);
        harvestWait(120);
        
        LOGGER.info("Checking if the Tomcat Agent is still connected to collector after MOM shutdown" );
        agentcolconnectmsg = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at "+ collectorHost;
       
        try {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, agentcolconnectmsg);
        Assert.assertTrue(false);        
        } catch (Exception e) {
        LOGGER.info("Tomcat Agent is not disconnected from Collector after the MOM is shutdown");
        Assert.assertTrue(true);
        }
        
        startEM(momRoleId);
        harvestWait(120);
        
        LOGGER.info("Checking if the Tomcat Agent is still connected to collector after MOM restart" );
        try {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, agentcolconnectmsg);
        Assert.assertTrue(false);        
        } catch (Exception e) {
        	LOGGER.info("Tomcat Agent is not disconnected from Collector after the MOM is restarted");
        Assert.assertTrue(true);
        }
       
        LOGGER.info("Checking if the Agent and EM logs have NullPointerException");
        String msg = "NullPointerException";        
        try
        {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);
        isKeywordInFile(envProperties, momMachineId, momLogFile, msg);
        isKeywordInFile(envProperties, collector1MachineId, col1LogFile, msg);
        isKeywordInFile(envProperties, collector2MachineId, col2LogFile, msg);
        Assert.assertTrue(false);        
        } catch (Exception e) {
        LOGGER.info("NullPointerException is not present in either EM or Agent logs");
        Assert.assertTrue(true);        
        }
	
	}
	
	@Test(groups = { "BAT" }, enabled = true)		
	public void verify_ALM_295399_Connect_agent_to_cluster() throws Exception {
		
		testcaseId = "295399";
		LOGGER.info("Making the EM and Agent logs to run in DEBUG mode");
		replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile", momMachineId,
                momConfigFile);
		replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile", collector1MachineId,
                col1ConfigFile);
		replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile", collector2MachineId,
                col2ConfigFile);
		replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
	                "log4j.logger.IntroscopeAgent=DEBUG,logfile", tomcatMachineId,
	                tomcatAgentProfile);
		
		startEM(momRoleId);		
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();
		
		LOGGER.info("Checking for MOM-Collector connection messages at collector side");
		checkLogForMsg(envProperties, collector1MachineId, col1LogFile, momconnectedmsg1atcol);
		checkLogForMsg(envProperties, collector1MachineId, col1LogFile, momconnectedmsg2atcol);
		checkLogForMsg(envProperties, collector2MachineId, col2LogFile, momconnectedmsg1atcol);
		checkLogForMsg(envProperties, collector2MachineId, col2LogFile, momconnectedmsg2atcol);
		
		startTomcatAgent(tomcatRoleId);
		getTomcatAgentConnectedColdetails();	
		
		connectagentmsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + collectorHost + ".ca.com:" + collectorPort + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = \"" + tomcatAgentHost + "\", Process = \"Tomcat\", Agent Name = \"Tomcat Agent\", Active = \"true\""; 
		LOGGER.info("Checking Tomcat Agent logs for the connection message to a collector"); 
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, connectagentmsg); 
		LOGGER.info("Checking Collector logs for the connection message to Tomcat Agent"); 
		checkLogForMsg(envProperties, collectorMachineId, colLogFile, agentconnectmsg);
	}
	
	@Test(groups = { "Deep" }, enabled = true)		
	public void verify_ALM_295397_Connecting_agent_to_already_up_and_running_cluster() throws Exception {
		
		testcaseId = "295397";
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		startEM(momRoleId);		
		collectorstomomconnectioncheck();		
		setTomcatagentManagerURL(collector1IP, collector1Port);
		startTomcatAgent(tomcatRoleId);
		getTomcatAgentConnectedColdetails();	
		
	}
	
	@Test(groups = { "Deep" }, enabled = true)		
	public void verify_ALM_295401_Connecting_agent_when_Standalone_EM_is_down() throws Exception {
		
		testcaseId = "295401";
		replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
                "introscope.enterprisemanager.clustering.mode=StandAlone",
                momMachineId, momConfigFile);
		startTomcatAgent(tomcatRoleId);
		harvestWait(120);
		
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, failuretomomhost);
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile,failuretoemlimit);
		
		startEM(momRoleId);
		waitForAgentNodes(tomcatagentexpr, momHost, momPort, momLibDir);
		
		connectagentmsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + momHost; 
		LOGGER.info("Checking Tomcat Agent logs for the connection message to the Standalone EM"); 
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, connectagentmsg); 		
		
	}
	
	@Test(groups = { "Deep" }, enabled = true)		
	public void verify_ALM_295402_Standalone_EM_and_Agent_connectivity_test_EM_up_and_down_scenarios() throws Exception {
		
		testcaseId = "295402";
		replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
                "introscope.enterprisemanager.clustering.mode=StandAlone",
                momMachineId, momConfigFile);
		
		startEM(momRoleId);		
		startTomcatAgent(tomcatRoleId);
		
		connectagentmsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + momHost; 
		LOGGER.info("Checking Tomcat Agent logs for the connection message to the Standalone EM"); 
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, connectagentmsg);
		
		stopEM(momRoleId);
		LOGGER.info("Checking whether Tomcat Agent gets disconnected after EM stop");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, reconnectmsgtomomhost);
		
		startEM(momRoleId);
		LOGGER.info("Checking whether Tomcat Agent gets connected back after EM restart");
		waitForAgentNodes(tomcatagentexpr, momHost, momPort, momLibDir);
		
		stopTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking disconnect message on EM side after Tomcat agent is stopped");
		checkLogForMsg(envProperties, momMachineId, momLogFile,tomcatagentdisconnectmsg);
		harvestWait(60);
		
		startTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking if the Tomcat agent connects back to EM after the restart");
		waitForAgentNodes(tomcatagentexpr, momHost, momPort, momLibDir);
		
	} 
	
	@Test(groups = { "BAT" }, enabled = true)		
	public void verify_ALM_295404_Agent_connecting_directly_to_collector_in_a_cluster_MOM_Agent_Collectors() throws Exception {
		
		testcaseId = "295404";
		startEM(momRoleId);
		
		setTomcatagentManagerURL(collector1IP, collector1Port);		
		startTomcatAgent(tomcatRoleId);
		LOGGER.info("Checking if Tomcat Agent is trying to connect to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, failuretocol1IP);
		
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		collectorstomomconnectioncheck();
		
		LOGGER.info("Checking Collector1 logs for the connection message to Tomcat Agent"); 
		checkLogForMsg(envProperties, collector1MachineId, col1LogFile, agentconnectmsg);
		
	}	
	
	@Test(groups = { "BAT" }, enabled = true)		
	public void verify_ALM_454915_Agent_connecting_directly_to_collector_in_a_cluster_MOM_Collectors_Agent() throws Exception {
		
		testcaseId = "454915";
		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		collectorstomomconnectioncheck();		
		
		setTomcatagentManagerURL(collector1Host, collector1Port);		
		startTomcatAgent(tomcatRoleId);	
		LOGGER.info("Checking if Tomcat Agent is connected to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agentconnectmsgtocol1);		
		
	}
	
	@Test(groups = { "BAT" }, enabled = true)		
	public void verify_ALM_454916_Agent_connecting_directly_to_collector_in_a_cluster_Agent_MOM_Collectors() throws Exception {
		
		testcaseId = "454916";		
		setTomcatagentManagerURL(collector1Host, collector1Port);		
		startTomcatAgent(tomcatRoleId);
		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		collectorstomomconnectioncheck();
		
		LOGGER.info("Checking if Tomcat Agent is connected to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agentconnectmsgtocol1);		
		
	}
	
	@Test(groups = { "BAT" }, enabled = true)		
	public void verify_ALM_454917_Agent_connecting_directly_to_collector_in_a_cluster_Agent_Collectors_MOM() throws Exception {
		
		testcaseId = "454917";		
		setTomcatagentManagerURL(collector1Host, collector1Port);		
		startTomcatAgent(tomcatRoleId);	
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		
		LOGGER.info("Checking if Tomcat Agent is connected to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agentconnectmsgtocol1);
		
		startEM(momRoleId);
		collectorstomomconnectioncheck();
		
		tempResult1 = getcollectorconnectedmetricvalue(collector1Host,collector1Port);
		Assert.assertTrue("Connected Metric for collector1 is not reporting the correct value",tempResult1.equals("Integer:::1"));
		tempResult1 = getcollectorconnectedmetricvalue(collector2Host,collector2Port);
		Assert.assertTrue("Connected Metric for collector2 is not reporting the correct value",tempResult1.equals("Integer:::1"));
		
		tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|Tomcat\\|Tomcat Agent", "EM Host", collector1Host,
                collector1Port, momLibDir);         
        Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Host value",
                tempResult1.equals("String:::"+collector1Host));
		
		tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|Tomcat\\|Tomcat Agent", "EM Port", collector1Host,
                collector1Port, momLibDir);         
        Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                tempResult1.equals("String:::"+collector1Port));	
		
	} 
	
	@Test(groups = { "BAT" }, enabled = true)		
	public void verify_ALM_454918_Agent_connecting_directly_to_collector_in_a_cluster_Collectors_Agent_MOM() throws Exception {
		
		testcaseId = "454918";
		startEM(collector1RoleId);
		startEM(collector2RoleId);			
		setTomcatagentManagerURL(collector1Host, collector1Port);
		startTomcatAgent(tomcatRoleId);	
		
		LOGGER.info("Checking if Tomcat Agent is connected to collector1");
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agentconnectmsgtocol1);
		
		startEM(momRoleId);
		collectorstomomconnectioncheck();
		
		tempResult1 = getcollectorconnectedmetricvalue(collector1Host,collector1Port);
		Assert.assertTrue("Connected Metric for collector1 is not reporting the correct value",tempResult1.equals("Integer:::1"));
		tempResult1 = getcollectorconnectedmetricvalue(collector2Host,collector2Port);
		Assert.assertTrue("Connected Metric for collector2 is not reporting the correct value",tempResult1.equals("Integer:::1"));
		
		tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|Tomcat\\|Tomcat Agent", "EM Host", collector1Host,
                collector1Port, momLibDir);         
        Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Host value",
                tempResult1.equals("String:::"+collector1Host));
		
		tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|Tomcat\\|Tomcat Agent", "EM Port", collector1Host,
                collector1Port, momLibDir);         
        Assert.assertTrue("Tomcat Agent is not reporting the Correct EM Port value",
                tempResult1.equals("String:::"+collector1Port));
	
	}
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_295403_Agent_connecting_to_MOM_in_a_cluster_MOMIP_Agent_Collectors() throws Exception {
		
		testcaseId = "295403";			
		startEM(momRoleId);	
		
		setTomcatagentManagerURL(momIP, momPort);
		startTomcatAgent(tomcatRoleId); 		
		keyWords.clear();
        keyWords.add(agenttomomIPconnectmsg);
        keyWords.add(agenttomomHostconnectmsg);       
        verifyIfAtleastOneKeywordIsInLog(tomcatMachineId, tomcatLogFile, keyWords);        
        
        startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();		
		getTomcatAgentConnectedColdetails();
	}
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_454919_Agent_connecting_to_MOM_in_a_cluster_Agent_MOMIP_Collectors() throws Exception {
		
		testcaseId = "454919";
		setTomcatagentManagerURL(momIP, momPort);
		startTomcatAgent(tomcatRoleId); 
		
		startEM(momRoleId);
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agenttomomIPconnectmsg);
		
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();
		getTomcatAgentConnectedColdetails();
	}
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_454920_Agent_connecting_to_MOM_in_a_cluster_MOMIP_Collectors_Agent() throws Exception {
		
		testcaseId = "454920";
		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();
		
		setTomcatagentManagerURL(momIP, momPort);
		startTomcatAgent(tomcatRoleId);
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agenttomomIPconnectmsg);
		collectorstomomconnectioncheck();
		getTomcatAgentConnectedColdetails();
	} 
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_454921_Agent_connecting_to_MOM_in_a_cluster_Agent_Collectors_MOMIP() throws Exception {
		
		testcaseId = "454921";		
		setTomcatagentManagerURL(momIP, momPort);
		startTomcatAgent(tomcatRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		startEM(momRoleId);
		collectorstomomconnectioncheck();
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agenttomomIPconnectmsg);
		getTomcatAgentConnectedColdetails();
	}
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_454922_Agent_connecting_to_MOM_in_a_cluster_Collectors_Agent_MOMIP() throws Exception {
		
		testcaseId = "454922";
		startEM(collector1RoleId);
		startEM(collector2RoleId);		
		setTomcatagentManagerURL(momIP, momPort);
		startTomcatAgent(tomcatRoleId);
		startEM(momRoleId);
		collectorstomomconnectioncheck();
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agenttomomIPconnectmsg);
		getTomcatAgentConnectedColdetails();
		
	}
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_454923_Agent_connecting_to_MOM_in_a_cluster_Agent_MOMwithdomain_Collectors() throws Exception {
		
		testcaseId = "454923";		
		setTomcatagentManagerURL(momHost+".ca.com", momPort);
		startTomcatAgent(tomcatRoleId);
		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		collectorstomomconnectioncheck();
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agenttomomHostconnectmsg);
		getTomcatAgentConnectedColdetails();		
		
	} 
	
	@Test(groups = { "Smoke" }, enabled = true)		
	public void verify_ALM_454924_Agent_connecting_to_MOM_in_a_cluster_Agent_MOMlowerupper_Collectors() throws Exception {
		
		testcaseId = "454924";
		int momHostlength = momHost.length();
		String momHostlowercase = momHost.toLowerCase();		
		String momHostbothcase = momHostlowercase.substring(0,2).toUpperCase()+ momHostlowercase.substring(2,momHostlength);
		
		setTomcatagentManagerURL(momHostbothcase, momPort);
		startTomcatAgent(tomcatRoleId);
		
		startEM(momRoleId);
		startEM(collector1RoleId);
		startEM(collector2RoleId);
		
		agenttomomHostconnectmsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + momHostbothcase;
		checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, agenttomomHostconnectmsg);
		getTomcatAgentConnectedColdetails();
	}
		
	/**
	 * Methods returns the value of the Connected Metric of a collector which appears under CustomMetric
	 * @param collectorHost - Host of the collector whose metric needs to be fetched
	 * @param collectorPort - Port of the collector
	 * @return
	 */
	public String getcollectorconnectedmetricvalue(String collectorHost,int collectorPort) {
		
		String agentExpression =
					"(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
		String connectedmetricExpression =
                	"Enterprise Manager\\|MOM\\|Collectors\\|"+ collectorHost + "@" + collectorPort + ":Connected";
		String connectedmetricValue =
					clw.getLatestMetricValue("admin", "", agentExpression,
							connectedmetricExpression, momHost, momPort, momLibDir); 
		return connectedmetricValue;		
		
	}
	
	/**
	 * Checks if the collectors(collector1,2) are successfully connected to the MOM
	 */
	public void collectorstomomconnectioncheck() {			
			
		LOGGER.info("Checking if collector1 is connected to the MOM");
		checkLogForMsg(envProperties, momMachineId, momLogFile, col1connectmsg);
		LOGGER.info("Checking if collector2 is connected to the MOM");
		checkLogForMsg(envProperties, momMachineId, momLogFile, col2connectmsg);
	}
	
	/**
	 * Checks if the Tomcat Agent got connected to any of the available collectors(collector1,collector2)
	 * Also sets the connected collector Host , Port, Logfile and MachineID in a variable
	 */
	public void getTomcatAgentConnectedColdetails() {
	
		LOGGER.info("Checking if the Tomcat Agent is connected to any of the available collectors and then get the collector details");
		
		collector_RoleIDs.clear();
		collector_RoleIDs.add(COLLECTOR1_ROLE_ID);
		collector_RoleIDs.add(COLLECTOR2_ROLE_ID);

		collectors_List.clear();
		collectors_List.add(collector1Host);
		collectors_List.add(collector2Host);

		collector_Port_List.clear();
		collector_Port_List.add(collector1Port);
		collector_Port_List.add(collector2Port);

		collectorHost =
				getAgentConnectedCollectorName(collectors_List, collector_Port_List,
						collector_RoleIDs, tomcatagentexpr, "Host", momLibDir);  
    
		if (collectorHost.equals(collector1Host)) {
			LOGGER.info("Tomcat Agent is Connected to Collector1 Successfully");
			collectorPort = collector1Port;
			collectorMachineId = collector1MachineId;
			colLogFile = col1LogFile;    	
			Assert.assertTrue(true);
    	
		} else if (collectorHost.equals(collector2Host)) {
			LOGGER.info("Tomcat Agent is Connected to Collector2 Successfully");
			collectorPort = collector2Port;
			collectorMachineId = collector2MachineId;
			colLogFile = col2LogFile;
			Assert.assertTrue(true);
    	
		} else {
			Assert.assertTrue("Tomcat Agent is not connected to any of the available collectors",false);
		}
    	
	}
	
	/**
	 * sets the Tomcat Agent agentManager url
	 * @param Host - EM Host to which you want to connect your tomcat agent to
	 * @param port - EM port
	 */
	public void setTomcatagentManagerURL(String newHost, int newPort) {
		LOGGER.info("Change Tomcat Agent agentManager url");
		/*replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
				"agentManager.url.1=" + host + ":" + port,
				tomcatMachineId, tomcatAgentProfile);*/
		setAgentManagerUrl(tomcatAgentProfile, tomcatMachineId, momHost, momPort, newHost, newPort);
	}
	
	
	/** Stops entire cluster (mom,collector1,collector2)
	 * 
	 */
	public void stopcluster() {
		LOGGER.info("Stopping Cluster");
		stopEM(momRoleId);
		stopCollectorEM(momRoleId,collector1RoleId);
		stopCollectorEM(momRoleId,collector2RoleId);
	}
	
	/** This method reverts mom,collectors config and agent profile file
	 * and it is used in AfterMethod
	 */
	
	public void revertConfigFiles() {
		LOGGER.info("Reverting mom,col1,col2 and tomcat agent config files");
		revertFile(momConfigFile, momConfigFile_copy, momMachineId);
		revertFile(col1ConfigFile, col1ConfigFile_copy,collector1MachineId);
		revertFile(col2ConfigFile, col2ConfigFile_copy,collector2MachineId);		
		revertFile(tomcatAgentProfile, tomcatAgentProfile_copy,tomcatMachineId);
	}	
	
	
	/** This method renames all the log files created during the execution and 
	 * it runs after every test case in AfterMethod
	 */
	
	public void renamelogfiles() {
				
		try {
			renameFile(momLogFile, momLogFile + "_" + testcaseId, momMachineId);
		} catch (Exception e) { 
			LOGGER.info("Skipping rename as the logfile does not exist");
		}
				
		try {   
			renameFile(col1LogFile, col1LogFile + "_" + testcaseId, collector1MachineId); 
		} catch (Exception e) {
			LOGGER.info("Skipping rename as the logfile does not exist");
		}
		
		try {  
			renameFile(col2LogFile, col2LogFile + "_" + testcaseId, collector2MachineId);
		} catch (Exception e) {
				LOGGER.info("Skipping rename as the logfile does not exist");
		}
		
		try {    
			renameFile(tomcatLogFile, tomcatLogFile + "_" + "tomcat" + "_" + testcaseId, tomcatMachineId); 
		} catch (Exception e) {
			LOGGER.info("Skipping rename as the logfile does not exist");
		}
		
	}
		
		
	@AfterMethod(alwaysRun = true)
	public void stopservicesandrevertchanges() {
		
		stopcluster();
		try {
			LOGGER.info("Stopping tomcat");
			stopTomcatAgent(tomcatRoleId);
		} catch (Exception e) {
			LOGGER.info("Tomcat is already in stopped state");
		} 		
		renamelogfiles();
		revertConfigFiles();
		
	}

}
