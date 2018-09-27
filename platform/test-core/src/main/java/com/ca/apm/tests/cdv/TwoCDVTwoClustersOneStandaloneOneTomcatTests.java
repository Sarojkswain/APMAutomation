/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * Author : KETSW01
 */
package com.ca.apm.tests.cdv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlow;
import com.ca.apm.automation.action.flow.utility.XmlModifierFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.base.TwoCDVTwoClustersOneStandaloneOneTomcatTestsBase;
import com.ca.apm.tests.testbed.CDVOneClusterOneTomcatLinuxTestbed;
import com.ca.apm.tests.testbed.TwoCDVTwoClustersOneStandaloneOneTomcatLinuxTestbed;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.util.XMLFileManager;

import static com.ca.apm.tests.cdv.CDVConstants.*;

public class TwoCDVTwoClustersOneStandaloneOneTomcatTests extends TwoCDVTwoClustersOneStandaloneOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwoCDVTwoClustersOneStandaloneOneTomcatTests.class);
    private TestUtils utility = new TestUtils();
    private CLWCommons clwCommons = new CLWCommons();
    private String[] installLogMsgs = {"0 Warnings","0 NonFatalErrors","0 FatalErrors" };


    @Test(groups = {"SMOKE"}, enabled = true)
	public void verify_ALM_271287_StandaloneEM_Install_SilentMode(){
			
			testCaseId = "271287";
			testCaseName = "ALM_" + testCaseId + "_StandaloneEM_Install_SilentMode";
			
			
			try{
				testCaseStart(testCaseName);

				checkMessagesInSequence(envProperties, AGENT_MACHINE_ID, StandlaoneEMInstallLogFile, installLogMsgs);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally{			
				testCaseEnd(testCaseName);			
			}
	} 
    
    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_275491_ChangeEMModeofStandalone(){
		
		testCaseId = "275491";
		testCaseName = "ALM_" + testCaseId + "_Verify_EM_Mode_Change_Standalone";
		
		rolesInvolved.add(STANDALONE_EM_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			
			replaceStanadaloneEMProperty("introscope.enterprisemanager.clustering.mode=Standalone", "introscope.enterprisemanager.clustering.mode=MOM");
			startStandaloneEM();
			
			replaceStanadaloneEMProperty("introscope.enterprisemanager.clustering.mode=MOM", "introscope.enterprisemanager.clustering.mode=Collector");
			startStandaloneEM();
			
			replaceStanadaloneEMProperty("introscope.enterprisemanager.clustering.mode=Collector", "introscope.enterprisemanager.clustering.mode=CDV");
			startStandaloneEM();
		}
		finally{			
			stopStandaloneEM();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }	
    
    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_268359_OnlyOneMoMPerCollector(){
		
		testCaseId = "268359";
		testCaseName = "ALM_" + testCaseId + "_Verify_Only_One_MoM_Per_Collector";
		
		rolesInvolved.add(MOM1_ROLE_ID);
		rolesInvolved.add(MOM1_COL1_ROLE_ID);
		rolesInvolved.add(MOM2_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			//Updated MoM2 to connect to MoM1-Coll1
			replaceMoM2Property("introscope.enterprisemanager.clustering.login.em1.host=" + mom2Collector1Host, 
					"introscope.enterprisemanager.clustering.login.em1.host=" + mom1Collector1Host);
			replaceMoM2Property("introscope.enterprisemanager.clustering.login.em1.port=" + mom2Collector1Port, 
					"introscope.enterprisemanager.clustering.login.em1.port=" + mom1Collector1Port);
			//Start Cluster1,MoM2 and check for clamp message
			startCluster1();
			startMoM2();
			checkMoM2LogForMsg("The Collector has rejected the connection as the clamp value for MOM is reached.");
		}
		finally{			
			stopCluster1();
			stopMoM2();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }
    
    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_275497_AllCollectorsToDifferentCDV(){
		
		testCaseId = "275497";
		testCaseName = "ALM_" + testCaseId + "_Verify_All_Collectors_To_Different_CDV";
		
		rolesInvolved.add(MOM1_ROLE_ID);
		rolesInvolved.add(MOM1_COL1_ROLE_ID);
		rolesInvolved.add(MOM2_COL1_ROLE_ID);
		rolesInvolved.add(CDV1_ROLE_ID);
		rolesInvolved.add(CDV2_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			//Update MoM1 to connect to both collectors
			List<String> newProp = new ArrayList<String>();
			newProp.add("introscope.enterprisemanager.clustering.login.em2.host=" + mom2Collector1Host);
			newProp.add("introscope.enterprisemanager.clustering.login.em2.port=" + mom2Collector1Port);
			newProp.add("introscope.enterprisemanager.clustering.login.em2.publickey=config/internal/server/EM.public");
			appendMoM1Properties(newProp);
			//Point only MoM1-Coll1 to CDV1
			replaceCDV1Property(commentedCollectorProps());
			newProp.clear();
			newProp.add("introscope.enterprisemanager.clustering.login.em1.host=" + mom1Collector1Host);
			newProp.add("introscope.enterprisemanager.clustering.login.em1.port=" + mom1Collector1Port);
			newProp.add("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public");
			appendCDV1Properties(newProp);
			//Point only MoM2-Coll1 to CDV2
			replaceCDV2Property(commentedCollectorProps());
			newProp.clear();
			newProp.add("introscope.enterprisemanager.clustering.login.em1.host=" + mom2Collector1Host);
			newProp.add("introscope.enterprisemanager.clustering.login.em1.port=" + mom2Collector1Port);
			newProp.add("introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public");
			appendCDV2Properties(newProp);
			//Start EMs and validate
			startCluster1();
			startMoM2Collector1();
			startCDV1();
			startCDV2();
			checkCollectorsToMOM1Connectivity();
			//check if MoM1-Coll1 connected to CDV1 
			checkSpecificCollectorToMOMConnectivity(".*" + mom1Collector1Host + "@" + mom1Collector1Port + ".*", cpuMetricExpression,
		            cdv1Host, Integer.toString(cdv1Port), cdv1LibDir);
			//check if MoM2-Coll1 connected to CDV2 
			checkSpecificCollectorToMOMConnectivity(".*" + mom2Collector1Host + "@" + mom2Collector1Port + ".*", cpuMetricExpression,
		            cdv2Host, Integer.toString(cdv2Port), cdv1LibDir);
			checkMoM1LogForNoMsg("[ERROR]");
			checkMoM1Coll1LogForNoMsg("[ERROR]");
			checkMoM2Coll1LogForNoMsg("[ERROR]");
			checkCDV1LogForNoMsg("[ERROR]");
			checkCDV2LogForNoMsg("[ERROR]");			
		}
		finally{			
			stopCluster1();
			stopMoM2Collector1();
			stopCDV1();
			stopCDV2();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }
    
    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_275496_AllCollectorsToSameCDV(){
		
		testCaseId = "275496";
		testCaseName = "ALM_" + testCaseId + "_Verify_All_Collectors_To_Same_CDV";
		
		rolesInvolved.add(MOM1_ROLE_ID);
		rolesInvolved.add(MOM1_COL1_ROLE_ID);
		rolesInvolved.add(MOM2_COL1_ROLE_ID);
		rolesInvolved.add(CDV1_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			//Update MoM1 to connect to both collectors
			List<String> newProp = new ArrayList<String>();
			newProp.add("introscope.enterprisemanager.clustering.login.em2.host=" + mom2Collector1Host);
			newProp.add("introscope.enterprisemanager.clustering.login.em2.port=" + mom2Collector1Port);
			newProp.add("introscope.enterprisemanager.clustering.login.em2.publickey=config/internal/server/EM.public");
			appendMoM1Properties(newProp);			
			//Start EMs, Agents and validate
			startTomcatAgent();
			startCluster1();
			startMoM2Collector1();
			startCDV1();
			checkCollectorsToMOM1Connectivity();
			checkCollectorsToCDV1Connectivity();
			checkMoM1LogForNoMsg("[ERROR]");
			checkMoM1Coll1LogForNoMsg("[ERROR]");
			checkMoM2Coll1LogForNoMsg("[ERROR]");
			checkCDV1LogForNoMsg("[ERROR]");
			checkCDV2LogForNoMsg("[ERROR]");
			waitForAgentNodes(tomcatAgentExpression, cdv1Host, cdv1Port, cdv1LibDir);
		}
		finally{			
			stopCluster1();
			stopMoM2Collector1();
			stopCDV1();
			stopTomcatAgent();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }
    
    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_270880_ConcurrentConnectionsCDVCollector(){
		
		testCaseId = "270880";
		testCaseName = "ALM_" + testCaseId + "_Verify_concurrent_Connections_of__CDV_to_Collector";
		
		rolesInvolved.add(MOM1_ROLE_ID);
		rolesInvolved.add(MOM1_COL1_ROLE_ID);
		rolesInvolved.add(CDV1_ROLE_ID);
		rolesInvolved.add(CDV2_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			//Start EMs, Agents and validate
			startCluster1();
			startCDV1();
			startCDV2();
			checkSpecificCollectorToMOMConnectivity(".*" + mom1Collector1Host + "@" + mom1Collector1Port + ".*", cpuMetricExpression,
		            cdv1Host, Integer.toString(cdv1Port), cdv1LibDir);
			checkSpecificCollectorToMOMConnectivity(".*" + mom1Collector1Host + "@" + mom1Collector1Port + ".*", cpuMetricExpression,
		            cdv2Host, Integer.toString(cdv2Port), cdv1LibDir);
			checkMoM1Coll1LogForMsg("CDV Introscope Enterprise Manager connected");
			
		}
		finally{			
			stopCluster1();
			stopCDV1();
			stopCDV2();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }
    
    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_270882_NumberCDVsCollector(){
		
		testCaseId = "270882";
		testCaseName = "ALM_" + testCaseId + "_Verify_number_of_Connections_of_CDV_to_Collector";
		
		rolesInvolved.add(MOM1_COL1_ROLE_ID);
		rolesInvolved.add(MOM2_COL1_ROLE_ID);
		rolesInvolved.add(CDV1_ROLE_ID);
		rolesInvolved.add(CDV2_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			//Start EMs and validate
			startMoM1Collector1();
			startMoM2Collector1();
			startCDV1();
			startCDV2();
			String coll1AgentExp = "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\) \\(" + 
					mom1Collector1Host + "@" + mom1Collector1Port +"\\)";
			String coll2AgentExp = "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\) \\(" + 
					mom2Collector1Host + "@" + mom2Collector1Port +"\\)";
			String metricExpCDVClamped = "Enterprise Manager\\|Connections:Cross\\-Cluster Data Viewer Clamped";
			String metricExpNumberofCDV = "Enterprise Manager\\|Connections:Number of Cross\\-Cluster Data Viewers";
			harvestWait(30);
			String cdvClampedCol1 = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, metricExpCDVClamped, 
					mom1Collector1Host, Integer.parseInt(mom1Collector1Port), cdv1LibDir);
			String numberofCDVCol1 = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, metricExpNumberofCDV, 
					mom1Collector1Host, Integer.parseInt(mom1Collector1Port), cdv1LibDir);
			String cdvClampedCol2 = clwCommons.getLatestMetricValue(user, password, coll2AgentExp, metricExpCDVClamped, 
					mom2Collector1Host, Integer.parseInt(mom2Collector1Port), cdv1LibDir);
			String numberofCDVCol2 = clwCommons.getLatestMetricValue(user, password, coll2AgentExp, metricExpNumberofCDV, 
					mom2Collector1Host, Integer.parseInt(mom2Collector1Port), cdv1LibDir);
			LOGGER.info("CDV Clampled metric value of Coll1 is " + cdvClampedCol1);
			LOGGER.info("Number of CDV Connected to Collector metric value of Coll1 is " + numberofCDVCol1);
			LOGGER.info("CDV Clampled metric value of Coll2 is " + cdvClampedCol2);
			LOGGER.info("Number of CDV Connected to Collector metric value of Coll2 is " + numberofCDVCol2);
			Assert.assertEquals("CDV clampled metric value is wrong for Coll1(MoM1-Coll1)", "Integer:::0", cdvClampedCol1);
			Assert.assertEquals("Number of CDV connected metric value is wrong for Coll1(MoM1-Coll1)", 
					"Integer:::2", numberofCDVCol1);
			Assert.assertEquals("CDV clampled metric value is wrong for Coll2(MoM2-Coll1)", 
					"Integer:::0", cdvClampedCol2);
			Assert.assertEquals("Number of CDV connected metric value is wrong for Coll2(MoM2-Coll1)", 
					"Integer:::2", numberofCDVCol2);
			//Stop CDV2 and validate metrics
			stopCDV2();
			harvestWait(30);
			numberofCDVCol1 = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, metricExpNumberofCDV, 
					mom1Collector1Host, Integer.parseInt(mom1Collector1Port), cdv1LibDir);
			numberofCDVCol2 = clwCommons.getLatestMetricValue(user, password, coll2AgentExp, metricExpNumberofCDV, 
					mom2Collector1Host, Integer.parseInt(mom2Collector1Port), cdv1LibDir);
			LOGGER.info("Number of CDV Connected to Collector metric value of Coll1 after CDV2 stop is " + numberofCDVCol1);
			LOGGER.info("Number of CDV Connected to Collector metric value of Coll2 after CDV2 stop is " + numberofCDVCol2);
			Assert.assertEquals("Number of CDV connected metric value is wrong for Coll1(MoM1-Coll1) "
					+ "after CDV2 stop", "Integer:::1", numberofCDVCol1);
			Assert.assertEquals("Number of CDV connected metric value is wrong for Coll2(MoM2-Coll1) "
					+ "after CDV2 stop", "Integer:::1", numberofCDVCol2);
		}
		finally{			
			stopMoM1Collector1();
			stopMoM2Collector1();
			stopCDV1();
			stopCDV2();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }
    
//    @Test(groups = {"SMOKE"}, enabled = true)
//    public void verify_ALM_269785_MaxCDVsPerCollector(){
//		
//		testCaseId = "269785";
//		testCaseName = "ALM_" + testCaseId + "_Verify_Maximum_CDVs_allowed_Per_Collector";
//		
////		rolesInvolved.add(MOM1_ROLE_ID);
////		rolesInvolved.add(MOM1_COL1_ROLE_ID);
////		rolesInvolved.add(CDV1_ROLE_ID);
////		rolesInvolved.add(CDV2_ROLE_ID);
//		
//		try{
//			testCaseStart(testCaseName);
//			//Updated Max CDV to 1 for Coll1
//			System.out.println("File is " + mom1Collector1EventsThresholdConfigFile );
//			System.out.println("Host is " + mom1Collector1Host);
//			
//			XmlModifierFlowContext xmlFlowContext = new XmlModifierFlowContext.Builder(mom1Collector1EventsThresholdConfigFile)
//		     .setAttribute("/apmEvents/clamps/clamp[12]/threshold[@value='5']", "value", "1")
//		     .build();
//			runFlowByMachineId(mom1Collector1Host, XmlModifierFlow.class, xmlFlowContext);
////		     /apmEvents[@xmlns="http://www.ca.com/schema/apm/events"]/clamps/clamp[12]/threshold[@value="5"]@value
////		     "/apmEvents/clamps/clamp[12]/threshold[@value="5"]"@value
//		}
//		finally{			
////			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
////			rolesInvolved.clear();
//			testCaseEnd(testCaseName);			
//		}
//		
//    }
   
    private Map<String, String> commentedCollectorProps(){
    	
    	HashMap<String, String> collProps = new HashMap<String, String>();
    	
    	collProps.put("#introscope.enterprisemanager.clustering.login.em1.host=hostname", 
    			"#commentedeample.host=hostname");
    	collProps.put("#introscope.enterprisemanager.clustering.login.em1.port=5001",
    			"#commentedeample.port=5001");
    	collProps.put("#introscope.enterprisemanager.clustering.login.em1.publickey=internal/server/EM.public",
    			"#commentedeample.key=internal/server/EM.public");
    	collProps.put("introscope.enterprisemanager.clustering.login.em1.host",
    			"#introscope.enterprisemanager.clustering.login.em1.host");
    	collProps.put("introscope.enterprisemanager.clustering.login.em1.port",
    			"#introscope.enterprisemanager.clustering.login.em1.port");
    	collProps.put("introscope.enterprisemanager.clustering.login.em1.publickey",
    			"#introscope.enterprisemanager.clustering.login.em1.publickey");
    	collProps.put("introscope.enterprisemanager.clustering.login.em2.host",
    			"#introscope.enterprisemanager.clustering.login.em2.host");
    	collProps.put("introscope.enterprisemanager.clustering.login.em2.port",
    			"#introscope.enterprisemanager.clustering.login.em2.port");
    	collProps.put("introscope.enterprisemanager.clustering.login.em2.publickey",
    			"#introscope.enterprisemanager.clustering.login.em2.publickey");
		return collProps;
    }
     
}
