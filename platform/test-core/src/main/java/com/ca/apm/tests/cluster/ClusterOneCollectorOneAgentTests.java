package com.ca.apm.tests.cluster;

import java.util.ArrayList;

import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.base.OneCollectorOneTomcatTestsBase;
import com.ca.apm.tests.testbed.AgentControllability1Collector1TomcatAgentWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;



public class ClusterOneCollectorOneAgentTests extends OneCollectorOneTomcatTestsBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterOneCollectorOneAgentTests.class);
	
	protected String testCaseId;
	protected String testCaseName;
	protected ArrayList<String> rolesInvolved = new ArrayList<String>();
	
	
	@Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_280507_Cluster_MoMRestart(){
		
		testCaseId = "280507";
		testCaseName = "ALM_" + testCaseId + "_Verify_Collector_Connectivity_during_MoM_Restart";
		String momMsg = "Connected to the Introscope Enterprise Manager at "+ collector1Host+"@"+collector1Port;
		String collectorMsg =  "MOM Introscope Enterprise Manager connected";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		
		try{
			
			testCaseStart(testCaseName);
			
			startEMServices();			
			
			checkCollectorToMOMConnectivity();	
			LOGGER.info("Restarting MoM...!");
			restartEM(MOM_ROLE_ID);			
			checkCollectorToMOMConnectivity();						
			
			checkMoMLogForMsg(momMsg);			
			checkCollLogForMsg(collectorMsg);	
						
		}
		finally{			
			stopEMServices();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}
	
	
	@Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_297995_Cluster_WrongPortNumber(){
		
		testCaseId = "297995";
		testCaseName = "ALM_" + testCaseId + "_Verify_Collector_Connectivity_with_Wrong_Port_Number";
		String momMsg = "[Manager.LoadBalancer] No eligible collector for: SuperDomain|"+tomcatHost+"|Tomcat|Tomcat Agent";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		
		try{
			
			testCaseStart(testCaseName);
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.port="+ collector1Port,
		                "introscope.enterprisemanager.clustering.login.em1.port=50002");
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			startMoM();
			startAgent();
			
			checkMoMLogForMsg(momMsg);			
			
			
		}
		finally{
			
			stopMoM();
			stopAgent();
			
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);	
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
			
		}
		
	}	
	
	 
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_357225_Cluster_CollectorConnectedwithIP(){
		
		testCaseId = "357225";
		testCaseName = "ALM_" + testCaseId + "_Verify_Collector_Connectivity_with_IP_Addressr";
		String agentConnectivityMsg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at "+momHostIP+":"+momPort+",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory. Host = "+'"'+tomcatHost+'"'+", Process = "+'"'+ "Tomcat" + '"' +", Agent Name = "+'"'+"Tomcat Agent"+'"'+", Active = "+'"'+"false"+'"';		
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);

		
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.host=" + collector1Host,
					"introscope.enterprisemanager.clustering.login.em1.host=" + collector1HostIP);			
			replaceAgentProperty("agentManager.url.1=" + momHost + ":" + momPort,
					"agentManager.url.1=" + momHostIP + ":" + momPort);
			
			
			
			startTestBed();			
			checkAgentLogForMsg(agentConnectivityMsg);			
			
			
		}
		finally{
			
			stopTestBed();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);
			
		}
		
	}	


	@Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_392304_InvalidDisallowedConnectionLimit(){
		
		testCaseId = "392304";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Disallowed_Connection_Limit";
		String defaultValueMsg = "[INFO] [PO Async Executor] [Manager.LoadBalancer] Using default value for introscope.enterprisemanager.agent.disallowed.connection.limit: 0";
		String errorMsg = "[ERROR] [PO Async Executor] [Manager] Failed to set properties";
		String changedValueMsg = "[VERBOSE] [PO Async Executor] [Manager.ClusteredPropertyService] Changed introscope.enterprisemanager.agent.disallowed.connection.limit= (-1)";
		String verifyMsg;
		
		rolesInvolved.add(MOM_ROLE_ID);
		
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.agent.disallowed.connection.limit=0",
					"introscope.enterprisemanager.agent.disallowed.connection.limit=-1");			
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.agent.disallowed.connection.limit is negative: -1";			
			checkMoMLogForMsg(verifyMsg);
			checkMoMLogForMsg(defaultValueMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.agent.disallowed.connection.limit=-1",
					"introscope.enterprisemanager.agent.disallowed.connection.limit=");			 
			verifyMsg = "java.lang.NumberFormatException: For input string: ";
			checkMoMLogForMsg(changedValueMsg);
			checkMoMLogForMsg(errorMsg);
			checkMoMLogForMsg(verifyMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.agent.disallowed.connection.limit=",
					"introscope.enterprisemanager.agent.disallowed.connection.limit=1.23");			 
			verifyMsg = "java.lang.NumberFormatException: For input string: "+'"'+ "1.23" +'"';
			checkMoMLogForMsg(verifyMsg);
			
			replaceMoMProperty("introscope.enterprisemanager.agent.disallowed.connection.limit=1.23",
					"introscope.enterprisemanager.agent.disallowed.connection.limit=abc");			 
			verifyMsg = "java.lang.NumberFormatException: For input string: "+'"'+ "abc" +'"';
			checkMoMLogForMsg(verifyMsg);
			
			
		}
		finally{
			cleanupMomEMaftertest();			
		}
		
	}	

	
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_392497_InvalidCollectorWeight(){
		
		testCaseId = "392497";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Collector_Weight";
		String verifyMsg;
		rolesInvolved.add(MOM_ROLE_ID);
		
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("#introscope.enterprisemanager.clustering.login.em1.weight=",
					"introscope.enterprisemanager.clustering.login.em1.weight=-1");			
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[WARN] [main] [Manager.Cluster] Bad weight value: -1";		
			checkMoMLogForMsg(verifyMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.weight=-1",
					"introscope.enterprisemanager.clustering.login.em1.weight=abc");			 
			restartMOM();
			verifyMsg = "[WARN] [main] [Manager.Cluster] Bad weight value: abc";
			checkMoMLogForMsg(verifyMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.weight=abc",
					"introscope.enterprisemanager.clustering.login.em1.weight=1.23");	
			restartMOM();
			verifyMsg = "[WARN] [main] [Manager.Cluster] Bad weight value: 1.23";
			checkMoMLogForMsg(verifyMsg);	
			
		}
		finally{			
			cleanupMomEMaftertest();			
		}
		
	}	

	
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_372960_InvalidCollectorHostname(){
		
		testCaseId = "372960";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Collector_Hostname";
		String verifyMsg;
		rolesInvolved.add(MOM_ROLE_ID);
		
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.host=" + collector1Host,
					"introscope.enterprisemanager.clustering.login.em1.host=xyz");	
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[INFO] [main] [Manager.Cluster] Added collector xyz@"+ collector1Port;
			checkMoMLogForMsg(verifyMsg);
			verifyMsg = "[WARN] [Collector xyz@"+collector1Port+"] [Manager.Cluster] Failed to connect to the Introscope Enterprise Manager at xyz@"+collector1Port+" (1) because: java.net.UnknownHostException: xyz";
			checkMoMLogForMsg(verifyMsg);
						
			
		}
		finally{
			cleanupMomEMaftertest();			
		}
		
	}
	
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_372961_InvalidCollectorPort(){
		
		testCaseId = "372961";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Collector_Port";
		String verifyMsg;
		rolesInvolved.add(MOM_ROLE_ID);
		
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.port=" + collector1Port,
					"introscope.enterprisemanager.clustering.login.em1.port=123");	
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[WARN] [Collector "+ collector1Host+"@123] [Manager.Cluster] Failed to connect to the Introscope Enterprise Manager at "+collector1Host+"@123 (1) because: java.net.ConnectException: Connection refused:";
			checkMoMLogForMsg(verifyMsg);
						
			
		}
		finally{
			cleanupMomEMaftertest();			
		}
		

	}
	
	@Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_372962_InvalidPublicKey(){
		
		testCaseId = "372962";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Public_Key";
		String verifyMsg;
		rolesInvolved.add(MOM_ROLE_ID);
		
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public",
					"introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/invalidEM.public");
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[ERROR] [main] [Manager.Cluster] Unable to add collector "+collector1Host+ "@" + collector1Port;
			checkMoMLogForMsg(verifyMsg);
			verifyMsg = "java.io.FileNotFoundException: Unable to find the public key file config/internal/server/invalidEM.public";
			checkMoMLogForMsg(verifyMsg);
						
			
		}
		finally{
			cleanupMomEMaftertest();
		}
		

	}
	
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_392303_InvalidLoadbalancingInterval(){
		
		testCaseId = "392303";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Loadbalancing_Interval";
		String defaultValueMsg = "[INFO] [PO Async Executor] [Manager.LoadBalancer] Using default value for introscope.enterprisemanager.loadbalancing.interval: 600";
		String verifyMsg;
		rolesInvolved.add(MOM_ROLE_ID);
						
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.interval=600",
					"introscope.enterprisemanager.loadbalancing.interval=-600");			
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.interval is negative: -600";			
			checkMoMLogForMsg(verifyMsg);
			checkMoMLogForMsg(defaultValueMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.interval=-600",
					"introscope.enterprisemanager.loadbalancing.interval=");			 
			verifyMsg = " [WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.interval is not an integer:";
			checkMoMLogForMsg(verifyMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.interval=",
					"introscope.enterprisemanager.loadbalancing.interval=10.23");				 
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.interval is not an integer: 10.23";
			checkMoMLogForMsg(verifyMsg);
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.interval=10.23",
					"introscope.enterprisemanager.loadbalancing.interval=abc");	
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.interval is not an integer: abc";
			checkMoMLogForMsg(verifyMsg);
			
			
		}
		finally{
			cleanupMomEMaftertest();
		}
		

	}
	
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_392302_InvalidLoadbalancingThreshold(){
		
		testCaseId = "392302";
		testCaseName = "ALM_" + testCaseId + "_Verify_Invalid_Loadbalancing_Threshold";
		String defaultValueMsg = "[INFO] [PO Async Executor] [Manager.LoadBalancer] Using default value for introscope.enterprisemanager.loadbalancing.threshold: 20000";
		String verifyMsg;
		rolesInvolved.add(MOM_ROLE_ID);

						
		try{
			
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.threshold=20000",
					"introscope.enterprisemanager.loadbalancing.threshold=-20000");			
			replaceMoMProperty("log4j.logger.Manager=INFO, console, logfile",
	                "log4j.logger.Manager=DEBUG, console, logfile");
			
			
			startMoM();			
			
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.threshold is negative: -20000";			
			checkMoMLogForMsg(verifyMsg);
			checkMoMLogForMsg(defaultValueMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.threshold=-20000",
					"introscope.enterprisemanager.loadbalancing.threshold=");		
			restartMOM();
			verifyMsg = "[WARN] [main] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.threshold is not an integer:";
			checkMoMLogForMsg(verifyMsg);
			
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.threshold=",
					"introscope.enterprisemanager.loadbalancing.threshold=10.23");			 
			restartMOM();
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.threshold is not an integer: 10.23";
			checkMoMLogForMsg(verifyMsg);
			
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.threshold=10.23",
					"introscope.enterprisemanager.loadbalancing.threshold=abc");	
			restartMOM();
			verifyMsg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.enterprisemanager.loadbalancing.threshold is not an integer: abc";
			checkMoMLogForMsg(verifyMsg);
			
			
		}
		finally{
			cleanupMomEMaftertest();			
		}
		

	}
	
	private void cleanupMomEMaftertest(){
		stopMoM();
		renameLogWithTestCaseId(rolesInvolved, testCaseId);
		renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
		restorePropFiles(rolesInvolved);
		rolesInvolved.clear();
		testCaseEnd(testCaseName);
	}

}