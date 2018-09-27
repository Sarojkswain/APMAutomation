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
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.base.CDVOneClusterOneTomcatTestsBase;
import com.ca.apm.tests.testbed.CDVOneClusterOneTomcatLinuxTestbed;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import static com.ca.apm.tests.cdv.CDVConstants.*;

public class CDVOneClusterOneTomcatTests extends CDVOneClusterOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDVOneClusterOneTomcatTests.class);
    private TestUtils utility = new TestUtils();
    private CLWCommons clwCommons = new CLWCommons();
    private String[] installLogMsgs = {"0 Warnings","0 NonFatalErrors","0 FatalErrors" };

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_454128_DE139261_DifferentialAnalysis_stops_working_when_CDV_connects_to_Collectors(){
    	
    	testCaseId = "454128";
		testCaseName = "ALM_" + testCaseId + "_Verify_DE139261_DifferentialAnalysis_stops_working_when_CDV_connects_to_Collectors";
		
		try{
    	
        String metricExpression =
            "Variance\\|Default\\|Differential Analysis Control\\|Frontends(.*):Average Response Time \\(ms\\) Variance Intensity";

        startTestBed();
        harvestWait(60);
        // Hitting Frontends of Tomcat
        LOGGER.info("Hitting url for frontends: http://" + agentHost + ":" + agentPort);
        utility.connectToURL("http://" + agentHost + ":" + agentPort, 2);
        harvestWait(60);
        String actualMetricValue1 =
            clw.getLatestMetricValue(user, password, tomcatAgentExpression, metricExpression, cdvHost,
                cdvPort, cdvLibDir);
        LOGGER.info("Variance Average Response Time metric value: " + actualMetricValue1);

        Assert.assertFalse(actualMetricValue1.contains("-1"));
        stopCDV();
        harvestWait(180);
        moveFile(cdvConfigDir + "modules/DefaultMM.jar", cdvConfigDir
            + "modules/DefaultMM.jar.orig", CDV_MACHINE_ID);
        startCDV();
        // Hitting Frontends of Tomcat
        LOGGER.info("Hitting url for frontends: http://" + agentHost + ":" + agentPort);
        utility.connectToURL("http://" + agentHost + ":" + agentPort, 2);
        harvestWait(60);
        String actualMetricValue2 =
            clw.getLatestMetricValue(user, password, tomcatAgentExpression, metricExpression, cdvHost,
                cdvPort, cdvLibDir);
        LOGGER.info("Variance Average Response Time metric value: " + actualMetricValue2);
        Assert.assertTrue(actualMetricValue2.contains("-1"));
        moveFile(cdvConfigDir + "modules/DefaultMM.jar.orig", cdvConfigDir
            + "modules/DefaultMM.jar", CDV_MACHINE_ID);
		}
        finally{			
			stopTestBed();
			revertConfigAndRenameLogsWithTestId(testCaseId);
			testCaseEnd(testCaseName);			
		}
    }

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_269784_InstallMoMChangeToCDV(){
		
		testCaseId = "269784";
		testCaseName = "ALM_" + testCaseId + "_Install_MoM_ChangeTo_CDV";
		
		rolesInvolved.add(MOM_ROLE_ID);
		rolesInvolved.add(TOMCAT_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.mode=MOM", "introscope.enterprisemanager.clustering.mode=CDV");
			
			startMoM();
			startAgent();
			checkLogForNoMsg(envProperties, AGENT_MACHINE_ID, tomcatAgentLogFile, "Connected Controllable Agent");
								
		}
		finally{			
			stopMoM();
			stopAgent();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}
    
    
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_275492_EMModePropNoValue(){
		
		testCaseId = "275492";
		testCaseName = "ALM_" + testCaseId + "_Verify_EM_Mode_Property_No_Value";
		
		rolesInvolved.add(CDV_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			
			replaceCDVProperty("introscope.enterprisemanager.clustering.mode=CDV", "introscope.enterprisemanager.clustering.mode=");
			
			boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS);
			String emExecutable = isWindows ? "Introscope_Enterprise_Manager.exe"
					: "./Introscope_Enterprise_Manager";
			String[] commands = {emExecutable};
			String logMsg = "Invalid value  set for property introscope.enterprisemanager.clustering.mode";
			List<String> compareStrings = new ArrayList<String>();
			compareStrings.add(logMsg);
			
			boolean emStartMsgCheck = Util.validateCommandOutput(commands, cdvInstallDir, compareStrings);
			
			Assert.assertEquals(true, emStartMsgCheck);
								
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{			
			stopCDV();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}    
    
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_275493_EMModePropWrongValue(){
		
		testCaseId = "275493";
		testCaseName = "ALM_" + testCaseId + "_Verify_EM_Mode_Property_Wrong_Value";
		
		rolesInvolved.add(CDV_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			
			replaceCDVProperty("introscope.enterprisemanager.clustering.mode=CDV", "introscope.enterprisemanager.clustering.mode=WRONG");
			
			boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS);
			String emExecutable = isWindows ? "Introscope_Enterprise_Manager.exe"
					: "./Introscope_Enterprise_Manager";
			String[] commands = {emExecutable};
			String dirLoc =
		            envProperties.getRolePropertyById(CDV_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
			String logMsg = "Invalid value WRONG set for property introscope.enterprisemanager.clustering.mode";
			List<String> compareStrings = new ArrayList<String>();
			compareStrings.add(logMsg);
			
			boolean emStartMsgCheck = Util.validateCommandOutput(commands, dirLoc, compareStrings);
			
			Assert.assertEquals(true, emStartMsgCheck);
								
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{			
			stopCDV();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	}  
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_275488_ChangeEMModeofCDV(){
		
		testCaseId = "275488";
		testCaseName = "ALM_" + testCaseId + "_Verify_EM_Mode_Change_CDV";
		
		rolesInvolved.add(CDV_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			
			replaceCDVProperty("introscope.enterprisemanager.clustering.mode=CDV", "introscope.enterprisemanager.clustering.mode=MOM");
			startStopCDV();
			
			replaceCDVProperty("introscope.enterprisemanager.clustering.mode=MOM", "introscope.enterprisemanager.clustering.mode=Collector");
			startStopCDV();
			
			replaceCDVProperty("introscope.enterprisemanager.clustering.mode=Collector", "introscope.enterprisemanager.clustering.mode=Standalone");
			startStopCDV();
		}
		finally{			
			stopCDV();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
	} 
    
    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_275489_ChangeEMModeofMOM(){
		
		testCaseId = "275489";
		testCaseName = "ALM_" + testCaseId + "_Verify_EM_Mode_Change_MoM";
		
		rolesInvolved.add(MOM_ROLE_ID);
		
		try{
			testCaseStart(testCaseName);
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.mode=MOM", "introscope.enterprisemanager.clustering.mode=CDV");
			startStopMoM();
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.mode=CDV", "introscope.enterprisemanager.clustering.mode=Collector");
			startStopMoM();
			
			replaceMoMProperty("introscope.enterprisemanager.clustering.mode=Collector", "introscope.enterprisemanager.clustering.mode=Standalone");
			startStopMoM();
		}
		finally{			
			stopMoM();
			revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
			rolesInvolved.clear();
			testCaseEnd(testCaseName);			
		}
		
    }	
	
    @Test(groups = {"DEEP"}, enabled = true)
	public void verify_ALM_275490_ChangeEMModeofCollector(){
			
			testCaseId = "275490";
			testCaseName = "ALM_" + testCaseId + "_Verify_EM_Mode_Change_Collector";
			
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			
			try{
				testCaseStart(testCaseName);
				
				replaceCollector1Property("introscope.enterprisemanager.clustering.mode=Collector", "introscope.enterprisemanager.clustering.mode=CDV");
				startStopCollector();
				
				
				replaceCollector1Property("introscope.enterprisemanager.clustering.mode=CDV", "introscope.enterprisemanager.clustering.mode=MOM");
				startStopCollector();
				
				replaceCollector1Property("introscope.enterprisemanager.clustering.mode=MOM", "introscope.enterprisemanager.clustering.mode=Standalone");
				startStopCollector();
			}
			finally{			
				stopCollector();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
		
    
    @Test(groups = {"DEEP"}, enabled = true)
	public void verify_ALM_268357_Cluster_CDV_SameVersion(){
			
			testCaseId = "268357";
			testCaseName = "ALM_" + testCaseId + "_Verify_Cluster_CDV_SameVersionr";
			
			try{
				testCaseStart(testCaseName);
				
				startTestBed();
				waitForAgentNodes(tomcatAgentExpression, cdvHost, cdvPort, cdvLibDir);
			}
			finally{			
				stopTestBed();
				revertConfigAndRenameLogsWithTestId(testCaseId);
				testCaseEnd(testCaseName);			
			}
		} 
    
    @Test(groups = {"DEEP"}, enabled = true)
	public void verify_ALM_268360_MetricCount_Collector_CDV(){
			
			testCaseId = "268360";
			testCaseName = "ALM_" + testCaseId + "_Verify_MetricCount_of_Collector_and_at_CDV";
			
			rolesInvolved.add(CDV_ROLE_ID);
			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			
			try{
				testCaseStart(testCaseName);
				String coll1AgentExp = "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\) \\(" + collector1Host + "@" + collector1Port +"\\)";
				String numberofMetricExp = "Enterprise Manager\\|Connections:Number of Metrics";
				startEMServices();
				
				harvestWait(60);
				String coll1MetricCountCDV = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, numberofMetricExp, cdvHost, cdvPort, cdvLibDir);
				String coll1MetricCountLocal = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, numberofMetricExp, collector1Host, Integer.parseInt(collector1Port), cdvLibDir);		
				
				Assert.assertEquals(coll1MetricCountCDV, coll1MetricCountLocal);
			
			}
			finally{			
				stopEMServices();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
    
    
    @Test(groups = {"DEEP"}, enabled = true)
	public void verify_ALM_269793_Enable_all_MM_CDV(){
			
			testCaseId = "269793";
			testCaseName = "ALM_" + testCaseId + "_Verify_enabling_all_MM_on_CDV";
			
			rolesInvolved.add(CDV_ROLE_ID);
			
			try{
				testCaseStart(testCaseName);
				Assert.assertTrue(apmbaseutil.enableAllMMonEM(cdvInstallDir));
				
				startCDV();
				
				checkCDVLogForNoMsg("[ERROR]");
			}
			finally{			
				stopCDV();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
    
    	@Test(groups = {"DEEP"}, enabled = true)
    	public void verify_ALM_268374_verify_CDV_connecting_StandaloneEM(){
			
			testCaseId = "268374";
			testCaseName = "ALM_" + testCaseId + "_Verify_CDV_connecting_StandaloneEM";
			
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(CDV_ROLE_ID);
			
			try{
				testCaseStart(testCaseName);
				replaceCollector1Property("introscope.enterprisemanager.clustering.mode=Collector", 
						"introscope.enterprisemanager.clustering.mode=Standalone");
				
				startCollector();				
				startCDV();
				
				String logMsg = "Failed to connect to the Introscope Enterprise Manager at " 
				+ collector1Host + "@" + collector1Port + " (1) " + "because: "
						+ "com.wily.introscope.server.enterprise.entity.cluster.NotCollectorException: "
						+ "The Enterprise Manager is not running in a Collector role.";
				
				checkCDVLogForMsg(logMsg);
			}
			finally{			
				stopCollector();
				stopCDV();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
    	
    	
    	@Test(groups = {"DEEP"}, enabled = true)
    	public void verify_ALM_270879_Agent_Connection_CDV(){
    			
    			testCaseId = "270879";
    			testCaseName = "ALM_" + testCaseId + "_Agent_Connection_CDV";
    			
    			rolesInvolved.add(CDV_ROLE_ID);
    			rolesInvolved.add(TOMCAT_ROLE_ID);
    			
    			try{
    				testCaseStart(testCaseName);

    				replaceTomcatAgentProperty("agentManager.url.1=" + momHost + ":" + momPort, 
    						"agentManager.url.1=" + cdvHost + ":" + cdvPort);
    				startCDV();
    				startAgent();
    				
    				checkTomcatAgentLogForMsg("Failed to connect to the Introscope Enterprise Manager at " + 
    						cdvHost + ":" + cdvPort);
    				checkCDVLogForMsg("The agent is trying to connect to CDV . The agent connection to  "
    						+ "CDV is forbidden.");
    			}
    			finally{			
    				stopCDV();
    				stopAgent();
    				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
    				rolesInvolved.clear();
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    
    	@Test(groups = {"DEEP"}, enabled = true)
    	public void verify_ALM_276547_CDV_Supportablity_Metrics_Collector(){
    			
    			testCaseId = "276547";
    			testCaseName = "ALM_" + testCaseId + "_Verify_CDV_Supportablity_Metrics_Collector";
    			
    			rolesInvolved.add(CDV_ROLE_ID);
    			rolesInvolved.add(COLLECTOR1_ROLE_ID);
    			
    			try{
    				testCaseStart(testCaseName);
    				String coll1AgentExp = "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\) \\(" + collector1Host + "@" + collector1Port +"\\)";
    				String metricCDVClamped = "Enterprise Manager\\|Connections:Cross\\-Cluster Data Viewer Clamped";
    				String metricNumberofCDV = "Enterprise Manager\\|Connections:Number of Cross\\-Cluster Data Viewers";
    				
    				startCollector();
    				startCDV();
    				harvestWait(60);
    				
    				String cdvClamped = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, metricCDVClamped, cdvHost, cdvPort, cdvLibDir);
    				String numberofCDV = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, metricNumberofCDV, collector1Host, Integer.parseInt(collector1Port), cdvLibDir);		
    				
    				LOGGER.info("CDV Clampled metric value is " + cdvClamped);
    				LOGGER.info("Number of CDV Connected to Collector metric value is " + numberofCDV);
    				
    				Assert.assertEquals("CDV clampled metric value is wrong", "Integer:::0", cdvClamped);
    				Assert.assertEquals("Number of CDV connected metric value is wrong", "Integer:::1", numberofCDV);
    			
    			}
    			finally{			
    				stopEMServices();
    				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
    				rolesInvolved.clear();
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    	@Test(groups = {"SMOKE"}, enabled = true)
    	public void verify_ALM_276544_Calculators_CDV(){
    			
    			testCaseId = "276544";
    			testCaseName = "ALM_" + testCaseId + "_Verify_Calculators_CDV";
    			String cdvCalculatorfile = cdvInstallDir + "/scripts/HeapUsedPercentageUpdated.js";
    			
    			try{
    				testCaseStart(testCaseName);
    				String gcHeapUsedPercentageMetric = "GC Heap:Heap Used \\(%\\)";
    				String exampleCalculatorfile = cdvInstallDir + "/examples/scripts/HeapUsedPercentage.js";
    				String updatedCalculatorfile = cdvInstallDir + "/examples/scripts/HeapUsedPercentageUpdated.js";
    				//Start Testbed anc check for agent
    				startTestBed();
    				waitForAgentNodes(tomcatAgentExpression, cdvHost, cdvPort, cdvLibDir);
    				//Create a calculator script in cdv
    				copyFile(exampleCalculatorfile, updatedCalculatorfile, CDV_MACHINE_ID);
    				replaceProp("return false", "return true", CDV_MACHINE_ID, updatedCalculatorfile);
    				copyFile(updatedCalculatorfile, cdvCalculatorfile, CDV_MACHINE_ID);
    				checkColl1LogForMsg("Successfully added script " + collector1InstallDir + "/./scripts/HeapUsedPercentageUpdated.js");
    				harvestWait(20);
    				String gcHeapUsedPercentageMetricValue = clwCommons.getLatestMetricValue(user, password, tomcatAgentExpression, gcHeapUsedPercentageMetric, cdvHost, cdvPort, cdvLibDir);
    				LOGGER.info("Calculated metric value is " + gcHeapUsedPercentageMetricValue);
    				Assert.assertFalse("Calculated metric does not exist", gcHeapUsedPercentageMetricValue.toString().equalsIgnoreCase("-1"));
    			
    			} catch (Exception e) {
					e.printStackTrace();
				}
    			finally{			
    				stopTestBed();
    				revertConfigAndRenameLogsWithTestId(testCaseId);
    				deleteFile(cdvCalculatorfile, CDV_MACHINE_ID);
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    	@Test(groups = {"DEEP"}, enabled = true)
    	public void verify_ALM_275499_Enable_LoadBalancer_CDV(){
    			
    			testCaseId = "275499";
    			testCaseName = "ALM_" + testCaseId + "_Enable_LoadBalancer_CDV";
    			
    			rolesInvolved.add(CDV_ROLE_ID);
    			
    			try{
    				testCaseStart(testCaseName);

    				List<String> newProp = new ArrayList<String>();
    				newProp.add("introscope.enterprisemanager.loadbalancer.enable=true");
    				
					appendProp(newProp , CDV_MACHINE_ID, cdvConfigFile);
    				startCDV();
    				
    				checkCDVLogForNoMsg("[Manager.LoadBalancer] Loaded loadbalancing.xml");
    			}
    			finally{			
    				stopCDV();
    				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
    				rolesInvolved.clear();
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    	@Test(groups = {"SMOKE"}, enabled = true)
    	public void verify_ALM_275486_CDV_Install_SilentMode(){
    			
    			testCaseId = "275486";
    			testCaseName = "ALM_" + testCaseId + "_CDV_Install_SilentMode";
    			
    			
    			try{
    				testCaseStart(testCaseName);
    				
    				checkMessagesInSequence(envProperties, CDV_MACHINE_ID, cdvInstallLogFile, installLogMsgs);
    				
    			} catch (Exception e) {
					e.printStackTrace();
				}
    			finally{			
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    	@Test(groups = {"DEEP"}, enabled = true)
    	public void verify_ALM_271286_Collector_Install_SilentMode(){
    			
    			testCaseId = "271286";
    			testCaseName = "ALM_" + testCaseId + "_Collector_Install_SilentMode";
    			
    			
    			try{
    				testCaseStart(testCaseName);

    				checkMessagesInSequence(envProperties, EM_MACHINE_ID, collector1InstallLogFile, installLogMsgs);
    				
    			} catch (Exception e) {
					e.printStackTrace();
				}
    			finally{			
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    	@Test(groups = {"FULL"}, enabled = true)
    	public void verify_ALM_271285_MoM_Install_SilentMode(){
    			
    			testCaseId = "271285";
    			testCaseName = "ALM_" + testCaseId + "_MoM_Install_SilentMode";
    			
    			
    			try{
    				testCaseStart(testCaseName);

    				checkMessagesInSequence(envProperties, EM_MACHINE_ID, momInstallLogFile, installLogMsgs);
    				
    			} catch (Exception e) {
					e.printStackTrace();
				}
    			finally{			
    				testCaseEnd(testCaseName);			
    			}
    		} 
    	
    	@Test(groups = {"DEEP"}, enabled = true)
    	public void verify_ALM_269796_different_domains_Cluster(){
			testCaseId = "269796";
			testCaseName = "ALM_" + testCaseId + "_Verify_different_domains_Cluster";
			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(TOMCAT_ROLE_ID);
			try{
				testCaseStart(testCaseName);
				//Create a domain on Collector1
				String domainName = "Test";
				String agentMapping = "(.*)";
				createCustomDomainCollector1(domainName, agentMapping);
				//Start Cluster and Agent
				startCluster();
				startAgent();
//				harvestWait(60);
				checkColl1LogForMsg("Connected to Agent " + '"' + "SuperDomain/" + domainName);
				harvestWait(20);
				//Query the metric
				String agentExpression = "/*SuperDomain/*/|(.*)";
				String metricExpression = "EM Host";
				String getMetric = clw.getMetricValueForTimeInMinutes(user, password,
						agentExpression, metricExpression, collector1Host,
						Integer.parseInt(collector1Port), cdvLibDir, 1).toString();
				Assert.assertTrue(
						"Tomcat Agent not mapped to Test Domain",
						getMetric.contains("SuperDomain/" + domainName + ","
								+ tomcatHost + ",Tomcat,Tomcat Agent"));
			}
			finally{			
				stopCluster();
				stopAgent();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
    	
    	@Test(groups = {"FULL"}, enabled = true)
    	public void verify_ALM_275505_MoM_CDV_Different_Domains(){
			testCaseId = "275505";
			testCaseName = "ALM_" + testCaseId + "_Verify_MoM_CDV_Different_Domains";
			rolesInvolved.add(MOM_ROLE_ID);
			rolesInvolved.add(CDV_ROLE_ID);
			try{
				testCaseStart(testCaseName);
				//Create a domain on MoM
				String momDomainName = "Test";
				String agentMapping = "(.*)";
				createCustomDomainMoM(momDomainName, agentMapping);
				//Create a domain in CDV
				String cdvDomainName = "Test1";
				createCustomDomainCDV(cdvDomainName, agentMapping);
				enableMoMDebugLog();
				enableCDVDebugLog();
				startCDV();
				startMoM();
				//Verify logs for Domains
				checkMoMLogForMsg("Creating domain SuperDomain/" + momDomainName);
				checkCDVLogForMsg("Creating domain SuperDomain/" + cdvDomainName);
			}
			finally{			
				stopCDV();
				stopMoM();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 

    	
    	@Test(groups = {"SMOKE"}, enabled = true)
    	public void verify_ALM_275495_Multiple_Hosts_Same_Collector(){
			testCaseId = "275495";
			testCaseName = "ALM_" + testCaseId + "_Verify_Multiple_Hosts_Same_Collector";
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(CDV_ROLE_ID);
			try{
				testCaseStart(testCaseName);
				//Adding duplicate collector entry in cdv
				List<String> dupCollProp = new ArrayList<String>();
				dupCollProp.add("introscope.enterprisemanager.clustering.login.em2.host=" + collector1Host);
				dupCollProp.add("introscope.enterprisemanager.clustering.login.em2.port=" + collector1Port);
				dupCollProp.add("introscope.enterprisemanager.clustering.login.em2.publickey=config/internal/server/EM.public");
				appendProp(dupCollProp , CDV_MACHINE_ID, cdvConfigFile);
				//Start Coll and CDV and check for log msg
				startCollector();
				startCDV();
				checkCDVLogForMsg("Ignoring duplicate collector " + collector1Host + "@" + collector1Port);
				//Check Collector metrics
				String coll1AgentExp = "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\) \\(" + collector1Host + "@" + collector1Port +"\\)";
				String metricNumberofCDV = "Enterprise Manager\\|Connections:Number of Cross\\-Cluster Data Viewers";
				String numberofCDV = clwCommons.getLatestMetricValue(user, password, coll1AgentExp, metricNumberofCDV, collector1Host, Integer.parseInt(collector1Port), cdvLibDir);		
				LOGGER.info("Number of CDV Connected to Collector metric value is " + numberofCDV);
				Assert.assertEquals("Number of CDV connected metric value is wrong", "Integer:::1", numberofCDV);
			}
			finally{			
				stopCollector();
				stopCDV();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
    	
    	@Test(groups = {"SMOKE"}, enabled = true)
    	public void verify_ALM_268355_Old_Properties_Removed(){
			testCaseId = "268355";
			testCaseName = "ALM_" + testCaseId + "_Verify_Old_Properties_Removed";
			try{
				testCaseStart(testCaseName);
				int foundProperty = Util.checkMessage("introscope.enterprisemanager.clustering.collector.enable", new File(cdvConfigFile));
				Assert.assertEquals("Old properties exists in the file", 0, foundProperty);
				foundProperty = Util.checkMessage("introscope.enterprisemanager.clustering.manager.enable", new File(cdvConfigFile));
				Assert.assertEquals("Old properties exists in the file", 0, foundProperty);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally{			
				testCaseEnd(testCaseName);			
			}
		} 
    	
    	@Test(groups = {"SMOKE"}, enabled = true)
    	public void verify_ALM_270881_Connections_CDV(){
			testCaseId = "270881";
			testCaseName = "ALM_" + testCaseId + "_Verify_Connections_CDV";
			rolesInvolved.add(COLLECTOR1_ROLE_ID);
			rolesInvolved.add(CDV_ROLE_ID);
			try{
				testCaseStart(testCaseName);
				startCollector();
				startCDV();
				//Restart Collector multiple time
				restartCollector();
				restartCollector();
				restartCollector();
				checkCDVLogForNoMsg("[ERROR]");
				checkColl1LogForNoMsg("[ERROR]");
			}
			finally{			
				stopCollector();
				stopCDV();
				revertConfigAndRenameLogsWithTestId(testCaseId, rolesInvolved);
				rolesInvolved.clear();
				testCaseEnd(testCaseName);			
			}
		} 
}
