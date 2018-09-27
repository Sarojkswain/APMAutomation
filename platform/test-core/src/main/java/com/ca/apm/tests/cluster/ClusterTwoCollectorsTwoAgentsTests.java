package com.ca.apm.tests.cluster;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.base.TwoCollectorsOneTomcatOneJBossTestsBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;



public class ClusterTwoCollectorsTwoAgentsTests extends TwoCollectorsOneTomcatOneJBossTestsBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterTwoCollectorsTwoAgentsTests.class);
	
	protected String testCaseId;
	protected String testCaseName;
	protected String clwOutput;
	protected CLWCommons clw = new CLWCommons();
	protected String clwOut;
	protected TestUtils testUtils = new TestUtils();
	protected ArrayList<String> rolesInvolved = new ArrayList<String>();
	
	@Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_280509_Cluster_CollectorRestart(){
		
		testCaseId = "280509";
		testCaseName = "ALM_" + testCaseId + "_Verify_Agent_Connectivity_during_Collector_Restart";
		
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);
		rolesInvolved.add(JBOSS_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		
		try{
			
			testCaseStart(testCaseName);
			
			replaceTomcatAgentProperty("agentManager.url.1=" + momHost + ":" + momPort,
					"agentManager.url.1=" + collector1Host + ":" + collector1Port);
			replaceJBossAgentProperty("agentManager.url.1=" + momHost + ":" + momPort,
					"agentManager.url.1=" + collector1Host + ":" + collector1Port);
			
			
			startTestBed();
			checkCollectorsToMOMConnectivity();
			checkJBossAgentLogExistence();
			clwOut = clw.getNodeList(user, password, ".*", momHost,
					Integer.parseInt(momPort), momLibDir).toString();
			System.out.println(clwOut);
	        Assert.assertTrue(
	                "Both the agents are not connected to the cluster",
	                clwOut.contains("Tomcat")
	                        && clwOut.contains("JBoss"));	   
			LOGGER.info("Restarting Collector 1...!");
			 Assert.assertTrue(
		                "Both the agents are not connected to the cluster after restart of Collector",
		                clwOut.contains("Tomcat")
		                        && clwOut.contains("JBoss"));						
		}
		finally{			
			stopTestBed();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
//			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}
	
	
	@Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_280510_CollectorHotConfig(){
		
		testCaseId = "280510";
		testCaseName = "ALM_" + testCaseId + "_Verify_Collector_HotConfig";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);
		List<String> appendproplist = new ArrayList<String>();
		String emHostConfigProperty = "introscope.enterprisemanager.clustering.login.em2.host";
		String emHostConfig;
		String emPortConfigProperty = "introscope.enterprisemanager.clustering.login.em2.port";
		String emPortConfig;
		String emPublickeyConfigProperty = "introscope.enterprisemanager.clustering.login.em2.publickey";
		String emPublickeyConfig;
		
		
		
		try{
			testCaseStart(testCaseName);
			
			startEMServices();
			checkCollectorsToMOMConnectivity();
			
			emHostConfig = testUtils.lineWithSubString(configFileMom, emHostConfigProperty);
			emPortConfig = testUtils.lineWithSubString(configFileMom, emPortConfigProperty);
			emPublickeyConfig=testUtils.lineWithSubString(configFileMom, emPublickeyConfigProperty);
	        appendproplist.add(emHostConfig.replace(".em2.", ".em3."));
	        appendproplist.add(emPortConfig.replace(".em2.", ".em3."));
	        appendproplist.add(emPublickeyConfig.replace(".em2.", ".em3."));
	        //Add em2 collector as em3 
	        appendProp(appendproplist, MOM_MACHINE_ID, configFileMom );
			
			String momMsg = "[WARN] [PO Async Executor] [Manager.Cluster] Ignoring duplicate collector";
			checkMoMLogForMsg(momMsg);
			checkCollectorsToMOMConnectivity();
			
		}
		finally{			
			stopEMServices();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
			harvestWait(600);
//			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}
	
	@Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_450467_ChangeDetectorEnabled_loadbalancing(){
		
		testCaseId = "450467";
		testCaseName = "ALM_" + testCaseId + "_Verify_changedetectorenabled_loadbalancing";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);
		rolesInvolved.add(JBOSS_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		
		try{
			testCaseStart(testCaseName);
			
			replaceMoMProperty("#introscope.changeDetector.disable=true", "introscope.changeDetector.disable=false");
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.threshold=20000","introscope.enterprisemanager.loadbalancing.threshold=1");
			replaceMoMProperty("introscope.enterprisemanager.loadbalancing.interval=600", "introscope.enterprisemanager.loadbalancing.interval=120");
			replaceCollector1Property("#introscope.changeDetector.disable=true", "introscope.changeDetector.disable=false");
			replaceCollector2Property("#introscope.changeDetector.disable=true", "introscope.changeDetector.disable=false");
			enableMoMDebugLog();
			replaceTomcatAgentProperty("#introscope.changeDetector.enable=false", "introscope.changeDetector.enable=true");
			replaceTomcatAgentProperty("#introscope.changeDetector.agentID=SampleApplicationName","introscope.changeDetector.agentID=");
			replaceJBossAgentProperty("#introscope.changeDetector.enable=false", "introscope.changeDetector.enable=true");
			replaceJBossAgentProperty("#introscope.changeDetector.agentID=SampleApplicationName","introscope.changeDetector.agentID=");
			
			
			startTestBed();
			checkJBossAgentLogExistence();
			
			xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "LoadBalancewithCD",
				        ".*\\|.*\\|.*", collector1Host + ":" + collector1Port, "include");
			
			String agentLogMsg =
	                "Connected controllable Agent to the Introscope Enterprise Manager at "
	                    + collector1Host;
	        checkTomcatAgentLogForMsg(agentLogMsg);  
	        checkJBossAgentLogForMsg(agentLogMsg);
	        
			
			clwOut = clw.getNodeList(user, password, ".*", collector1Host,
						Integer.parseInt(collector1Port), momLibDir).toString();
		    Assert.assertTrue(
		                "Both the agents are not connected to the Collector1 " + collector1Host,
		                clwOut.contains("Tomcat")
		                        && clwOut.contains("JBoss"));

		    //Changing the hostname of assigned collector to another
		    replaceProp(collector1Host, collector2Host, MOM_MACHINE_ID, loadBalanceFile);
		    agentLogMsg =
	                "Connected controllable Agent to the Introscope Enterprise Manager at "
	                    + collector2Host;
	        checkTomcatAgentLogForMsg(agentLogMsg);  
	        checkJBossAgentLogForMsg(agentLogMsg);
	        
		    
		    clwOut = clw.getNodeList(user, password, ".*", collector2Host,
					Integer.parseInt(collector2Port), momLibDir).toString();
	        Assert.assertTrue(
	                "Both the agents are not connected to the Collector2 " + collector2Host,
	                clwOut.contains("Tomcat")
	                        && clwOut.contains("JBoss"));
			
		} catch (Exception e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{			
			stopTestBed();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
//			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			restoreFile(loadBalanceFile_Backup, loadBalanceFile, MOM_MACHINE_ID);			
			testCaseEnd(testCaseName);			
		}
	}
	
	@Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_305343_CollectorConnectivity_WeightHotConfig(){
		
		testCaseId = "305343";
		testCaseName = "ALM_" + testCaseId + "_Verify_CollectorConnectivity_WeightHotConfig";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);
		List<String> appendproplist = new ArrayList<String>();
		
		
		try{
			testCaseStart(testCaseName);
						
	        appendproplist.add("#introscope.enterprisemanager.clustering.login.em1.weight=50");
	        appendproplist.add("#introscope.enterprisemanager.clustering.login.em2.weight=50");
	        appendProp(appendproplist, MOM_MACHINE_ID, configFileMom );
			
			
			startTestBed();
			harvestWait(60);
			replaceMoMProperty("#introscope.enterprisemanager.clustering.login.em1.weight=50", "introscope.enterprisemanager.clustering.login.em1.weight=50");
			replaceMoMProperty("#introscope.enterprisemanager.clustering.login.em2.weight=50", "introscope.enterprisemanager.clustering.login.em2.weight=50");
			harvestWait(120);
			checkCollectorsToMOMConnectivity();
			String momMsg = "[ERROR] [Manager] Failed to set properties";
			checkMoMLogForNoMsg(momMsg);
			
		}
		finally{			
			stopEMServices();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
//			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}

	@Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_450709_WeirdAgentLogEntries_MoMRestart(){
		
		testCaseId = "450709";
		testCaseName = "ALM_" + testCaseId + "_WeirdAgentLogEntries_MoMRestart";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		
		try{
			testCaseStart(testCaseName);
			
			startEMServices();
			checkCollectorsToMOMConnectivity();
			for (int i = 0; i < 3; i++) {
				restartMOM();
				checkCollectorsToMOMConnectivity();
			}
			//Start the agent and check for connection status
			startTomcatAgent();
			waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), momLibDir);
			harvestWait(30);
			//Check the contents of the log file for EM list
			checkEMListContents(envProperties, COLLECTOR1_MACHINE_ID, tomcatAgentLogFile, getemRoles());
			
		}
		finally{			
			stopEMServices();
			stopTomcatAgent();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
//			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}
	
	@Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_450921_WeirdAgentLogEntries_Cluster_Agent_Restart(){
		testCaseId = "450921";
		testCaseName = "ALM_" + testCaseId + "_WeirdAgentLogEntries_Cluster_Agent_Restart";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(COLLECTOR1_ROLE_ID);
		rolesInvolved.add(COLLECTOR2_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		
		try{
			testCaseStart(testCaseName);
			
			startEMServices();
			checkCollectorsToMOMConnectivity();
			for (int i = 0; i < 3; i++) {
				restartMOM();
				checkCollectorsToMOMConnectivity();
				harvestWait(30);
			}
			
			restartCollectors();
			startTomcatAgent();
			harvestWait(60);
			restartTomcatAgent();
			waitForAgentNodes(tomcatAgentExp, momHost, Integer.parseInt(momPort), momLibDir);
			harvestWait(60);
			//Check the contents of the log file for EM list
			checkEMListContents(envProperties, COLLECTOR1_MACHINE_ID, tomcatAgentLogFile, getemRoles());
			LOGGER.info("Now checking the size");
			//the current environment has two collectors with each machine has one NIC so the list size is 6
			checkEMListSize(envProperties, COLLECTOR1_MACHINE_ID, tomcatAgentLogFile, "6");
			
			
		}
		finally{			
			stopEMServices();
			stopTomcatAgent();
			renameLogWithTestCaseId(rolesInvolved, testCaseId);
//			renamePropertyFilesWithTestCaseId(rolesInvolved, testCaseId);
			restorePropFiles(rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}
	
	
}