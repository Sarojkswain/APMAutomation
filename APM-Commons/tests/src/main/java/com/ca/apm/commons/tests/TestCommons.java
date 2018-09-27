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
 * Author : GAMSA03/ SANTOSH JAMMI
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */
package com.ca.apm.commons.tests;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.ApmbaseUtil.SkipLineResult;
import com.ca.apm.commons.coda.common.AutomationConstants;
import com.ca.apm.commons.coda.common.SSToolsConstants;
import com.ca.apm.commons.coda.common.SSToolsUtil;
import com.ca.apm.commons.coda.common.SmartStorResult;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.testbed.CommonsLinuxTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.jdbc.IntroscopeDriver;


public class TestCommons extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommons.class);
    TestUtils utility = new TestUtils();

    private final String host;
    private final String watchDogDir;
    private final int sshPort;
    private final String emRoleId;
    private final String emPort;
    private final String libDir;
    private final String logDir;
    private final String configDir;
	private final String emLogDir;
	private final String emLogFile;
	private String errorMsg1;
	private String errorMsg2;
	private final String configFile;
	private  List<String> msgList = new ArrayList<String>();
    ApmbaseUtil util = new ApmbaseUtil();

    public TestCommons() {
        emRoleId = CommonsLinuxTestbed.EM_ROLE_ID;
        
        host =
            envProperties
            .getMachineHostnameByRoleId(CommonsLinuxTestbed.EM_ROLE_ID);
        watchDogDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_BIN_DIR);
        libDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        logDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LOG_DIR);
        configDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);
        
        sshPort = 22;
        emPort = envProperties
        .getRolePropertiesById(emRoleId).getProperty("emPort");
		emLogDir = envProperties.getRolePropertyById(emRoleId,
				DeployEMFlowContext.ENV_EM_LOG_DIR);
		emLogFile = envProperties.getRolePropertyById(emRoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		configFile = envProperties.getRolePropertyById(emRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);
		errorMsg1 = "INFO";
		errorMsg2 = "Post";

		msgList.add("manager");
		msgList.add("5001");
        
    }
    
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_257487_txBD01() {
        try {
//            LOGGER.info(Boolean.toString(ApmbaseUtil.isPortAvailable(5001, host)));
//            LOGGER.info(Boolean.toString(ApmbaseUtil.lookForPortAvailability(host, 5001, 100, true)));
//            
//            ApmbaseUtil.startWatchDogEM(watchDogDir);
//            LOGGER.info(Integer.toString(ApmbaseUtil.lookForPortReady(host, 5001)));
//            Thread.sleep(100000);
//            ApmbaseUtil.checkTranscationTraces("admin", "", "", host, 5001, libDir, "error");
//            ApmbaseUtil.checkListAgentsQuery("admin", "", "", host, 5001, libDir, "error");
//            
//            ApmbaseUtil.stopWatchDogEM(watchDogDir);
//            
//            String[] list ={"findstr INFO IntroscopeEnterpriseManager.log",""};
//            List<String> compareWith = new ArrayList<String>();
//            compareWith.add("INFO");
//            ApmbaseUtil.executeCommandAndCheckOutput(list, logDir, "jamsa07.txt", compareWith, 60);
//            
//            List<String> commands = new ArrayList<String>();
//            commands.add("tail -f IntroscopeEnterpriseManager.log");
//            
//            Process p = ApmbaseUtil.runCommand(commands, logDir);
//            ApmbaseUtil.sleep(10);
//            ApmbaseUtil.killProcess(p);
//            ApmbaseUtil.checkproperties("IntroscopeEnterpriseManager.properties", configDir, "introscope.enterprisemanager.port.channel1", "5002");
//            ApmbaseUtil.setproperties("IntroscopeEnterpriseManager.properties", configDir, "introscope.enterprisemanager.port.channel1", "5002");
//            commands.clear();
//            commands.add("introscope.enterprisemanager.query.datapointlimit=100000");
//            ApmbaseUtil.appendProperties(commands,"IntroscopeEnterpriseManager.properties", configDir);
//            ApmbaseUtil.removeProperties("IntroscopeEnterpriseManager.properties", configDir,commands);
            
            startEM(emRoleId);

            
        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }
    
//    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID),	size = SizeType.COLOSSAL, owner = "tuuja01")
//	@Test(groups = {"BAT"}, enabled = true)
	public void allTestAtOnce() throws Exception {
/*		verify_findTwoStringsInSingleLine();
		verify_checkMessages();
		verify_checkValidLastUpdateIgnoreCase();
		verify_checkValidLastUpdate();
		verify_checklogMsg();
		verify_skipToLine();
		verify_checkMultipleMsgsInSingleLine();
		verify_convertTimeStamp();
		verify_appendProperties();
		verify_fileRename_delteLogfile();
		verify_checkValidLastUpdate_1();
		verify_checklog();
		verify_convertTimeStamp();
		verify_setEmClock();
		verify_consoleOutput();	
		verify_SmartStore();		
		verify_findPattern();        
        verify_replaceLine();
        verify_writePropertiesToFile();
        verify_intolerence();*/
        
        verify_SSToolsConstants();
	}

	public void verify_findTwoStringsInSingleLine() {
		try {
			int returnvalue = ApmbaseUtil.findTwoStringsInSingleLine(
					"IntroscopeEnterpriseManager.log", emLogDir, errorMsg1,
					errorMsg2);
			LOGGER.info("The findTwoStringsInSingleLine method Returned Value is", returnvalue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void verify_checkMessages() {
		try {
			int returnValue = ApmbaseUtil.checkMessages(msgList, new File(
					emLogDir + "IntroscopeEnterpriseManager.log"));
			LOGGER.info("The checkMessages method Returned Value is", returnValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void verify_checkValidLastUpdateIgnoreCase() {
		try {
			if (ApmbaseUtil
					.checkValidLastUpdateIgnoreCase(emLogFile, errorMsg1))
				LOGGER.info("The method checkValidLastUpdateIgnoreCase Passed");
			else
				LOGGER.info("The method checkValidLastUpdateIgnoreCase Failed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void verify_checkValidLastUpdate() {
		try {
			if (ApmbaseUtil.checkValidLastUpdate(emLogFile, errorMsg1))
				LOGGER.info("The method checkValidLastUpdate Passed");
			else
				LOGGER.info("The method checkValidLastUpdate Failed");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void verify_checklogMsg() {
		try {

			LOGGER.info("The Log file Name is " + emLogFile);
			if (ApmbaseUtil.checklogMsg(emLogFile, errorMsg1)) {
				LOGGER.info("The checklogMsg Passed");
			} else {
				LOGGER.info("The method checklogMsg Failed");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_setEmClock() {
		try {

			if (ApmbaseUtil.setEmClock("1")) {
				LOGGER.info("Test setEmClock Passed");
			} else {
				LOGGER.info("The method setEmClock Failed");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_setSystemTime() {
		try {

			ApmbaseUtil.setSystemTime("50");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_skipToLine() {
		try {

			SkipLineResult skL = ApmbaseUtil
					.skipToLine(5, emLogFile, errorMsg1);
			int LineCount = skL.lineCount(0);
			boolean result = skL.foundCheck(true);

			LOGGER.info("The LineCount is ", LineCount);
			LOGGER.info("The the result is ", result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_appendProperties() {
		try {

			ApmbaseUtil.appendProperties(msgList,
					"IntroscopeEnterpriseManager.propeties", configDir);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_convertTimeStamp() {
		try {

			LOGGER.info(ApmbaseUtil.convertTimeStamp("0 Feb 19 2:56:00 0 2016"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_checkMultipleMsgsInSingleLine() {
		try {

			errorMsg1 = "DEBUG~Defects";
			if (ApmbaseUtil.checkMultipleMsgsInSingleLine(emLogFile, errorMsg1)) {
				LOGGER.info("The checkMultipleMsgsInSingleLine is Passed");
			}

			LOGGER.info("The Result Is", ApmbaseUtil
					.checkMultipleMsgsInSingleLine(emLogFile, errorMsg1));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_checkValidLastUpdate_1() {
		try {

			errorMsg1 = "DEBUG";
			TimeZone tz = Calendar.getInstance().getTimeZone();
			LOGGER.info("The Timzone Display name is"
					+ tz.getDisplayName()); // (i.e. Moscow Standard Time)
			LOGGER.info("The Timezone ID is" + tz.getID()); // (i.e.// Europe/Moscow)
			Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
			LOGGER.info("The current TIME here is " + calendar.getTime());
			// Long currentDate = calendar.getTime();
			// LOGGER.info(myDate);

			if (ApmbaseUtil.checkValidLastUpdate(emLogFile,
					calendar.getTimeInMillis(), errorMsg1)) {
				LOGGER.info("The checkValidLastUpdate is Passed");
			} else
				LOGGER.info("checkValidLastUpdate is failed");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void verify_fileRename_delteLogfile() {
		try {
			ApmbaseUtil.fileRename(emLogDir, "dynamicDomains", "TestingOne");
			ApmbaseUtil.deleteLogFile(emLogDir, "dynamicDomains", "TestingOne");
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public void verify_consoleOutput() throws InterruptedException {

		Process process = null;

		List<String> command = new ArrayList<>();
		command.add("ls -ltr");

		try {
			process = ApmbaseUtil.runCommand(command, "/");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			LOGGER.info("The consoleOUT value is"
					+ ApmbaseUtil.checkConsoleOutput(process, "TestFile"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void verify_checklog() {
		LOGGER.info("The checklog Method output is "
				+ ApmbaseUtil.checklog("IntroscopeEnterpriseManager.log",
						emLogDir, "DEBUG"));
	}

	
	public void verify_SmartStore(){
	String[] cmds= {"./SmartStorTools.sh list_agents -agents *.* -src ../data/",""};
	
	List<String> compareStrings = new ArrayList<>();
	compareStrings.add("No Matching Agents are found in Smartstor");
	
   try {
	if(SSToolsUtil.executeSmartCommand(cmds, "/opt/automation/deployed/em/tools", compareStrings)>=1)
		LOGGER.info("SmartStor Command is successfully returned the result");
	else
		LOGGER.info("Failed to execute SmarStor command");
	
} catch (IOException e) {

	e.printStackTrace();
}
	}
	
	public void verify_findPattern()
	{
	    String fileName=logDir+"IntroscopeEnterpriseManager.log";
	    try
        {
            if(com.ca.apm.commons.coda.common.Util.findPattern(fileName, "[ERROR]"))
            {
                System.out.println("The verify_findPattern method is passed !!!!");
            }
            else
                System.out.println("The verify_findPattern is FAILED");
        } catch (Exception e)
        {           
            e.printStackTrace();
        }
	    
	    
	}
	
	   public void verify_replaceLine()
	    {
	        String fileName=logDir+"IntroscopeEnterpriseManager.log";
	        String OldLine="3/02/16 04:20:47.892 PM TLT [INFO] [main] [Manager] Starting server...";
	        String replaceLine="This Line is replaced by this method";
	        try
	        {
	            if(com.ca.apm.commons.coda.common.Util.replaceLine(fileName, OldLine, replaceLine))
	            {
	                System.out.println("The verify_replaceLine method is passed !!!!");
	            }
	            else
	                System.out.println("The verify_replaceLine is FAILED");
	        } catch (Exception e)
	        {           
	            e.printStackTrace();
	        }
	        
	        
	    }
	    
       public void verify_writePropertiesToFile() throws Exception
       {
           Properties properties = com.ca.apm.commons.coda.common.Util.loadPropertiesFile(configFile);
           properties.setProperty(AutomationConstants.AGENT_AUTONAMING_PROPERTY, "ModifiedParameter from TAS");
   
           try
           {
               com.ca.apm.commons.coda.common.Util.writePropertiesToFile(configFile, properties);
               
           } catch (Exception e)
           {           
               e.printStackTrace();
           }
           
         }
       
       
       
       public void verify_intolerence() throws Exception
       {
           
           try
           {
               com.ca.apm.commons.coda.common.Util.inTolerance(10, 5, 6);
               
           } catch (Exception e)
           {           
               e.printStackTrace();
           }
           
         }
       
       public void verify_SSToolsConstants() throws Exception
       {
           
              System.out.println("The constants Result is"+ Arrays.toString(SSToolsConstants.HELP_CMD_KEEP_AGENTS));
               
                   
         }
       
       @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "jamsa07")
       @Test
       public void verify_SmartStorResult() throws Exception {
           //String agentExpression="\\(.*\\)\\|Tomcat\\|Tomcat Agent";
           //String metricExpression="\\(.*\\)Total\\(.*\\)";
           String agentExpression="(.*)Tomcat(.*)";
           //String metricExpression="(.*)Total(.*)";
           String metricExpression="(.*)Total(.*)";
           String url = "jdbc:introscope:net//Admin:@tas-itc-n35:5001";
           long intervalSec = 180;
           System.out.println("@@@@@@@@@Running Smartstor Tests@@@@@@@@");
           SmartStorResult ssresult = querySmartStorForMetrics(url, agentExpression, metricExpression, intervalSec);
           System.out.println("@@@@@@@Query run successfully@@@@@@@");
           System.out.println("@@@@@@@Smartstor result: "+ssresult.toString());
           System.out.println("Domain: "+ssresult.getDomain(1));
           System.out.println("Host: "+ssresult.getHost(1));
           System.out.println("Process: "+ssresult.getProcess(1));
           System.out.println("AgentName: "+ssresult.getAgentName(1));
           System.out.println("Resource: "+ssresult.getResource(1));
           System.out.println("MetricName: "+ssresult.getMetricName(1));
           System.out.println("RecordType: "+ssresult.getRecordType(1));
           System.out.println("Period: "+ssresult.getPeriod(1));
           System.out.println("getIntendedEndTimestamp: "+ssresult.getIntendedEndTimestamp(1));
           System.out.println("getActualStartTimestamp: "+ssresult.getActualStartTimestamp(1));
           System.out.println("getActualEndTimestamp: "+ssresult.getActualEndTimestamp(1));
           System.out.println("Count: "+ssresult.getCount(1));
           System.out.println("Type: "+ssresult.getType(1));
           System.out.println("Value: "+ssresult.getValue(1));
           System.out.println("Min: "+ssresult.getMin(1));
           System.out.println("Max: "+ssresult.getMax(1));
           System.out.println("StringValue: "+ssresult.getStringValue(1));
           System.out.println("Size: "+ssresult.size());
           System.out.println("Display: "+ssresult.display());       
           List result = querySmartStor(url, agentExpression, metricExpression, intervalSec);
           System.out.println("querySmartstorResult: "+result);
       }
       
       
       
       public static SmartStorResult querySmartStorForMetrics(String url,
                                                              String agentExpr,
                                                              String metricExpr,
                                                              long intervalSec)  throws Exception{

           IntroscopeDriver driver = new IntroscopeDriver();
           Connection conn = null;
           ResultSet rs = null;
           Statement stmt = null;
           SmartStorResult signature = null;
           try
           {
               conn = driver.connect(url, null);
               System.out.println("Connected to " + url);
               String query = "select * from metric_data " + "where agent='"
                              + agentExpr + "' and " + "metric='" + metricExpr
                              + "' and " + "timestamp between "
                              + lastIntervalQuery(intervalSec);

               stmt = conn.createStatement();
               stmt.execute(query);
               System.out.println("Executing query: " + query);
               rs = stmt.getResultSet();

               signature = new SmartStorResult(rs);

           } finally
           {

               DbUtils.closeQuietly(conn, stmt, rs);

           }

           return signature;
       }
       
       private static String lastIntervalQuery(long intervalSec) {

           Date now = new Date();
           Date past = new Date(System.currentTimeMillis() - (intervalSec * 1000));

           DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(
                   SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
           return "'" + dateFormat.format(past) + "' and '"
                   + dateFormat.format(now) + "'";
       }
       
       public static List<Long> querySmartStor(String url,
                                               String agentExpr,
                                               String metricExpr,
                                               long intervalSec) throws Exception
       {

           List<Long> smartStorData = new ArrayList<Long>();
           IntroscopeDriver driver = new IntroscopeDriver();
           Connection conn = null;
           ResultSet rs = null;
           Statement stmt = null;

           try
           {
               conn = driver.connect(url, null);
               System.out.println("Connected to " + url);

               String query = "select * from metric_data " + "where agent='"
                              + agentExpr + "' and " + "metric='" + metricExpr
                              + "' and " + "timestamp between "
                              + lastIntervalQuery(intervalSec) + "aggregateall";

               stmt = conn.createStatement();
               stmt.execute(query);
               System.out.println("Executing query: " + query);
               rs = stmt.getResultSet();

               while (rs.next())
               {

                   smartStorData.add(rs.getLong("Value"));
               }

           } finally
           {

               DbUtils.closeQuietly(conn, stmt, rs);

           }

           return smartStorData;
       }
}
   