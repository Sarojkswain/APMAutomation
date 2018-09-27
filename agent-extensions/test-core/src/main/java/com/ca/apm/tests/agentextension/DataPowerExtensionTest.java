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
 * AUTHOR: MARSA22/SAI KUMAR MAROJU
 * DATE: 09/19/2017
 */
package com.ca.apm.tests.agentextension;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounter;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounterContext;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.resolver.ITasResolver;

public class DataPowerExtensionTest extends StandAloneEMOneTomcatTestsBase
{
    
    public static final String  EXTENSION_LOC_WIN   = TasBuilder.WIN_SOFTWARE_LOC;
    public static final String  EXTENSION_LOC_LINUX   = TasBuilder.LINUX_SOFTWARE_LOC;
                                                      
    
    public static final String UMA_INSTALL_COMMAND = "install";
    public static final String UMA_START_COMMAND = "start";
    public static final String UMA_STOP_COMMAND = "stop";
    public static final String UMA_WIN_BAT_FILE=EXTENSION_LOC_WIN+"apmia\\apmia-ca-installer.bat";
    public static final String UMA_LINUX_SH_FILE=EXTENSION_LOC_LINUX+"apmia/apmia-ca-installer.sh";
    public static final String UMA_ROLE_ID = "umaRole";
    
    protected RunCommandFlowContext umaInstall;
    protected RunCommandFlowContext umaStart;
    protected RunCommandFlowContext umaStop;
    
    boolean linux;
    protected String            extensionLocation;
    
    String                      testIdName;
    
    String                      umagentProfileFile;
    
    String                      dataPowerConfigFile;
    
    String 						umaAgentLogFile;
    
    String 						umaLogFile;
    
    CLWCommons                  clw               = new CLWCommons();

    TestUtils                   utility           = new TestUtils();

    static int                  counter           = 1;

    private static final Logger LOGGER            = LoggerFactory.getLogger(DataPowerExtensionTest.class);

      
    ITasResolver tasResolver;
    
    String emHostName;

    /**
     * Constructor
     */
    public DataPowerExtensionTest()
    {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            extensionLocation = EXTENSION_LOC_WIN;
            LOGGER.info("inside windows");
            linux=false;
            umagentProfileFile = extensionLocation +"apmia\\core\\config\\IntroscopeAgent.profile";
            dataPowerConfigFile = extensionLocation +"apmia\\extensions\\DataPowerMonitor\\config\\DatapowerMonitor-config.xml";
            umaLogFile = extensionLocation +"apmia\\APMIA_install.log";
            umaAgentLogFile = extensionLocation +"apmia\\logs\\IntroscopeAgent.log"; 
        }else{
            extensionLocation = EXTENSION_LOC_LINUX;
            LOGGER.info("inside linux");
            linux=true;
            umagentProfileFile = extensionLocation +"apmia/core/config/IntroscopeAgent.profile";
            dataPowerConfigFile = extensionLocation +"apmia/extensions/DataPowerMonitor/config/DatapowerMonitor-config.xml";
            umaLogFile = extensionLocation +"apmia/APMIA_install.log";
            umaAgentLogFile = extensionLocation +"apmia/logs/IntroscopeAgent.log";
        }
        
        
        emHostName = envProperties.getMachineHostnameByRoleId(AgentControllabilityConstants.EM_ROLE_ID);

        LOGGER.info("Em Host name:: "+emHostName);
        
    }

    @BeforeClass(alwaysRun = true)
    public void initialize()
    {

    	LOGGER.info("Initialize begins here");
    	
        LOGGER.info("Invoked testmethod in extensionjava");

        startEM();
        
        replaceProp("agentManager.url.1=localhost:5001",
                "agentManager.url.1="+emHostName+":5001",
                envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID),
                umagentProfileFile);
        
        
        copyDir(extensionLocation + "extensions/DataPower", extensionLocation +"apmia/extensions/deploy",envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));
        installLinuxUMA();
        //startLinuxUMA();
        harvestWait(5);
        stopLinuxUMA();
        harvestWait(5);
        renameLogFiles("initializetest");

    }

    //Test No. 1
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_455711_DataPowerMetrics() throws Exception
    {
        LOGGER.info("This is to verify ALM_455711_DataPowerMetrics");
        
        LOGGER.info("Starting UMA..");
        startLinuxUMA();
        
        LOGGER.info("Started UMA..");
        
        harvestWait(60);
        
        String metricExpression = "DataPower\\|WebServices\\|WSDL Status\\|Testing_DPM:Refresh Interval \\(seconds\\)";
        
        String componentMetric = getDataUsingCLW(metricExpression);
        
        Assert.assertFalse("Component metric didn't reported  ",componentMetric.equals("-1"));
        
        
        testIdName = "ALM_455711_DataPowerMetrics";
        renameLogFiles(testIdName);
    }
    
    //Test No. 2
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_455710_DataPowerAuthentication() throws Exception
    {
        LOGGER.info("This is to verify ALM_455710_DataPowerAuthentication");
        
        LOGGER.info("Starting UMA..");
        startLinuxUMA();
       
        LOGGER.info("Started UMA..");
        
        harvestWait(60);
        isKeywordInFile(envProperties, TOMCAT_MACHINE_ID, umaAgentLogFile,"[IntroscopeAgent.DataPowerMonitor] Connected to Datapower device");
        
        LOGGER.info("Found keyword!!!");

		testIdName = "ALM_455710_DataPowerAuthentication";
		renameLogFiles(testIdName);
		
       
    }

    
    @AfterMethod
    public void tearDown()
    {
        try
        {

            checkFileExistenceOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
                                             umaLogFile);
            LOGGER.info("entered into tearDown try method");
        	
            LOGGER.info("Stopping UMA..");
            stopLinuxUMA();
            LOGGER.info("Stopped UMA..");
            LOGGER.info("counter value :"+counter);
            renameLogWithTestCaseID(umaLogFile, TOMCAT_MACHINE_ID,
                                    "testFailed" + counter);
            counter++;
            LOGGER.info("counter value increased");
            
        } catch (Exception e)
        {
            // Do nothing
            LOGGER.info("APM IA log not available do nothing");

        }

    }
    
    public void checkFileExistenceOneTimeCounter(EnvironmentPropertyContext envProps,
                                                 String machineId,
                                                 String filePath)
    {
        CheckFileExistenceFlowOneTimeCounterContext checkFileExistenceFlowOneTimeCounterContext = new CheckFileExistenceFlowOneTimeCounterContext.Builder()
                .filePath(filePath).build();
        runFlowByMachineId(machineId,
                           CheckFileExistenceFlowOneTimeCounter.class,
                           checkFileExistenceFlowOneTimeCounterContext);
    }

    public void installLinuxUMA(){
        List<String> argumentsInstall = new ArrayList<>();
        argumentsInstall.add(UMA_LINUX_SH_FILE);
        argumentsInstall.add(UMA_INSTALL_COMMAND);
        umaInstall =new RunCommandFlowContext.Builder("sh")
        .args(argumentsInstall)
        .build();
        
        executeUmaCommand(umaInstall);
    }
    
    public void startLinuxUMA(){
        List<String> argumentsStart = new ArrayList<>();
        argumentsStart.add(UMA_LINUX_SH_FILE);
        argumentsStart.add(UMA_START_COMMAND);
        umaStart =new RunCommandFlowContext.Builder("sh")
        .args(argumentsStart)
        .build();
        
        executeUmaCommand(umaStart);
    }
    
    public void stopLinuxUMA(){
     
        List<String> argumentsStop = new ArrayList<>();
        argumentsStop.add(UMA_LINUX_SH_FILE);
        argumentsStop.add(UMA_STOP_COMMAND);
        umaStop =new RunCommandFlowContext.Builder("sh")
        .args(argumentsStop)
        .build();
        
        executeUmaCommand(umaStop);
    }

    
    public String getDataUsingCLW(String metricExpression)
    {   
        String metricData;
        String agentExpression = "(.*)\\|Common\\|Agent";
        
        metricData = clw.getLatestMetricValue(user, password, agentExpression, metricExpression, EMHost, Integer.parseInt(emPort), emLibDir);
        
        LOGGER.info("Metric data : "+ metricData);
        
        return metricData;
    }
    
    public void executeUmaCommand(RunCommandFlowContext uma) {
        try {
            LOGGER.info("UMA_ROLE_ID... "+UMA_ROLE_ID);
            runFlowByMachineId(AgentControllabilityConstants.TOMCAT_MACHINE_ID, RunCommandFlow.class, uma);
        } catch (Exception e) {
            LOGGER.error("UMA command Already executed: Exception");
        }
    } 
    
    public void renameLogFiles(String testIdName) {
		try {
			stopAgent();
			LOGGER.info("APM Infrastructure Agent Stopped Successfully");
		} catch (Exception e) {
			LOGGER.info("APM Infrastructure Agent Stopping Failed");
		}

		finally {
			try {
				renameLogWithTestCaseID(umaAgentLogFile, TOMCAT_MACHINE_ID,
						testIdName);
				renameLogWithTestCaseID(umaLogFile, TOMCAT_MACHINE_ID,
						testIdName);
			} catch (IllegalStateException ie) {
				LOGGER.info("Log Files not generated!!");
			}
		}
	}
}



