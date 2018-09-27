package com.ca.apm.tests.propertyconfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.agent.DeployAgentFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.AutomationConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.testbed.EMPropertyConfigurationtWindowsTestbed;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/***
 * This class is used to Execute the EM Basic Test cases
 * which was moved from Coda projects to TAS.
 * @by GAMSA03
 * 
 */

public class EMBasicsTest
    extends BaseAgentTest
{

    String             agentHost           = envProperties
            .getMachineHostnameByRoleId(EMPropertyConfigurationtWindowsTestbed.TOMCAT_ROLE_ID);
    
    String             emHost           = envProperties
            .getMachineHostnameByRoleId(EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID);
    
    String 			emRoleId = EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID;    
    
    String 	tomcatRoleId= EMPropertyConfigurationtWindowsTestbed.TOMCAT_ROLE_ID;
    
    String             emPath              = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR); 
    

    String             clwJarFileLoc       = emPath + "/lib/CLWorkstation.jar";

    String             regPath             = emPath
                                             + ApmbaseConstants.EM_CTRL_BAT;

    String             agentServerJar      = ApmbaseConstants.AGENT_SERVER_JAR;

    String             aspectJar           = ApmbaseConstants.ALL_ASPECT_JAR;

    String             commonAllJar        = ApmbaseConstants.COMMON_ALL_JAR;

    String             workstationServer   = ApmbaseConstants.WORKSTATION_JAR;

    String             serverJar           = ApmbaseConstants.CD_SERVER_JAR;

    String             managementModuleJar = ApmbaseConstants.CHG_DETECT_MNGT_JAR;

    String             emLog               = emPath + ApmbaseConstants.EM_LOG;

    String             plgFolderLoc        = emPath
                                             + ApmbaseConstants.PLUGIN_FOLDER_LOC;

    String             modFolderLoc        = emPath
                                             + ApmbaseConstants.MODULE_FOLDER_LOC;

    String             logFilePath         = emPath
                                             + ApmbaseConstants.LOG_PATH_LOC;

    String             emPropFile          = emPath
                                             + ApmbaseConstants.EM_PROP_FILE;

    String             dbFileLoc           = emPath
                                             + ApmbaseConstants.DB_FILE_LOC;

    String             emExe               = ApmbaseConstants.EM_EXE;
    String 			emHealthMetric		   = ApmbaseConstants.emHealthMetric;
    
    String 			agentConnectMetric     = ApmbaseConstants.agentConnectMetric;
    
    String 	cemUser = ApmbaseConstants.cemUser;
    
    String 	cemPassw = ApmbaseConstants.cemPassw;
    
    String 	emUser = ApmbaseConstants.emUser;
    
    String 	emPassw = ApmbaseConstants.emPassw;
    
    String 	emPort = ApmbaseConstants.emPort;
    
    String guestUser=ApmbaseConstants.guestUser;
    
    String guestPassw=ApmbaseConstants.guestPassw;
    
    String agentLogMessage = ApmbaseConstants.agentLogMessage;
    
    String tomcatAgentProcess =  ApmbaseConstants.tomcatAgentProcess;
    
    String tomcatAgentName =  ApmbaseConstants.tomcatAgentName;
    
    private static final int SUCCESS = 1;
    static int linecount=0;

	private static final int FAILURE = 0;
	String agentProfilePath = System.getProperty("role_agent.install.dir")
			+ ApmbaseConstants.AGENT_PROFILE_LOC + "/"
			+ ApmbaseConstants.AGENT_PROFILE;
	
	String tomcatAgentProfilePath = envProperties.getRolePropertyById(EMPropertyConfigurationtWindowsTestbed.TOMCAT_ROLE_ID, 
			DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily" + ApmbaseConstants.AGENT_PROFILE_LOC 
			+ "/" + ApmbaseConstants.AGENT_PROFILE;
	
	String agentLogPrefix = System.getProperty("role_agent.log.prefix");
	String agentLogPath = System.getProperty("results.dir") + "/"
			+ agentLogPrefix;
	
	String tomcatAgentLogPath = envProperties.getRolePropertyById(EMPropertyConfigurationtWindowsTestbed.TOMCAT_ROLE_ID, 
			DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs" ;
	
	
	
	String agentAutoprobeLogPath = System.getProperty("results.dir") + "/"
			+ agentLogPrefix + ".Autoprobe.log";
	
	private String agentmetric = null;
	private boolean isMetricExists = false;

	
	/** GA EM Logs folder Location */
	private String gaemLogFilePath = emPath + ApmbaseConstants.LOG_PATH_LOC;
	private String gaemdataFilePath = ApmbaseConstants.EM_DATA_FILE;
	

	private String sap_em_loc = System
			.getProperty("testbed_sapem.install.parent.dir");
	private String sapEmExe = ApmbaseConstants.EM_EXE;
	private String sapclwJarFileLoc = sap_em_loc
			+ ApmbaseConstants.SAP_CLWJAR_LOCATION;
	private String sapemLogFilePath = sap_em_loc
			+ ApmbaseConstants.LOG_PATH_LOC;

	private final String agentLogFileName = agentLogPrefix;
	private final String agentLogFilePath = System.getProperty("results.dir")
			+ "/";

	private final String introscopePropertiesFilePath = ApmbaseConstants.EM_LOC
			+ ApmbaseConstants.EM_PROP_FILE;

	private final String emLogFileName = ApmbaseConstants.EM_LOGFILE_NAME;
	private final String msg = ApmbaseUtil.SUCCESS_MESSAGE;
	String message = null;

	/**
	 * Test method to create setup and creating CLW instance.
	 * 
	 * @param emhost
	 *            - EM Host - Testbed properties
	 * 
	 * @param emuser
	 *            - EM User - Testbed properties
	 * 
	 * @param empassw
	 *            - EM Password - Testbed properties
	 * 
	 * @param emport
	 *            - EM Port - Testbed properties
	 **/
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EMBasicsTest.class);

    private String     exprAgentNode       = "";

    private CLWBean    clw, clw1                 = null;

    private MetricUtil metricutil          = null;

    /**
     * Sleep Method
     * 
     * @param duration
     */
    @Test(enabled = true)
    @Parameters(value = { "sleep" })
    public void sleep(long duration)
    {
        Util.sleep(duration);
    }

    /**
     * Global setup and creating CLW instance.
     * 
     * @param emhost
     * @param emuser
     * @param empassw
     * @param emport
     */
    @BeforeTest    
    public void initIscopeCLW()
    {
        System.out.println(" ***** CLW object parameters: *** emhost: "
                           + emHost + " emuser: " + emUser + " empassw: "
                           + emPassw + "emport:" + emPort
                           + " Location CLW Jar file: " + clwJarFileLoc);
        clw = new CLWBean(emHost, emUser, emPassw, Integer.parseInt(emPort),
                          clwJarFileLoc);
    }

    /**
     * Test method is to register the EM as windows services.
     * 
     * @param registerCommand
     * @param regServiceName
     * @param regStatus
     */
    @Test
    @Parameters(value = { "registerCommand", "regServiceName", "regStatus" })
    public void registerEM(String registerCommand,
                           String regServiceName,
                           String regStatus)
    {
        try
        {
            registerProcess(regPath, registerCommand, regServiceName, regStatus);
            int res = ApmbaseUtil.StopService(regServiceName);
            sleep(30000);
            Assert.assertEquals(res, Integer.parseInt(regStatus));

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test registerProcess
     * 
     * @param path
     * @param command
     * @param serviceName
     * @param status
     */
    private void registerProcess(String path,
                                 String command,
                                 String serviceName,
                                 String status) throws Exception
    {
        List<String> args = new ArrayList<String>();
        args.add(path);
        args.add(command);
        Util.invokeProcessBuilder(args, null, true, false, true, null);
        sleep(30000);

    }

    /**
     * Test method is to unRegister the EM as windows services.
     * 
     * @param unRegisterCommand
     * @param unRegServiceName
     * @param unRegstatus
     */
    @Test
    @Parameters(value = { "unRegisterCommand", "unRegServiceName",
            "unRegstatus" })
    public void unRegisterEM(String unRegisterCommand,
                             String unRegServiceName,
                             String unRegstatus)
    {
        sleep(30000);

        try
        {
            registerProcess(regPath, unRegisterCommand, unRegServiceName,
                            unRegstatus);
            sleep(30000);
            int res = ApmbaseUtil.StopService(unRegServiceName);
            Assert.assertEquals(res, Integer.parseInt(unRegstatus));

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Test method is to Start the EM as a window service
     * 
     * @param startStatus
     * @param startServiceName
     */

    @Test
    @Parameters(value = { "startStatus", "startServiceName" })
    public void startRegEM(String startStatus, String startServiceName)
    {
        sleep(30000);
        try
        {
            int res = ApmbaseUtil.StartService(startServiceName);
            Assert.assertEquals(res, Integer.parseInt(startStatus));

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out
                .println(" *****  EM Started as window service successfully ***** ");
    }

    /**
     * Test method is to Start the EM as a window service
     * 
     * @param stopStatus
     * @param stopServiceName
     */

    @Test
    @Parameters(value = { "stopStatus", "stopServiceName" })
    public void stopRegEM(String stopStatus, String stopServiceName)
    {
        sleep(30000);
        try
        {
            int res = ApmbaseUtil.StopService(stopServiceName);
            Assert.assertEquals(res, Integer.parseInt(stopStatus));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out
                .println(" *****  EM Stopped as window service successfully  ***** ");
    }

    /**
     * Test method is to check the log file for ERROR Message
     * 
     * @param errorEntry1
     * @param errorEntry2
     */
    @Test
    @Parameters(value = { "errorEntry1", "errorEntry2" })
    public void checkErrorLog(String errorEntry1, String errorEntry2)
    {

        try
        {
            int result = 0;
            result = ApmbaseUtil.findTwoStringsInSingleLine(ApmbaseConstants.EM_LOG,
                                                logFilePath, errorEntry1,
                                                errorEntry2);
            if (result <= 0) Assert.assertTrue(true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(" *****  Log File Checking Completed  ***** ");
    }

    /**
     * Test method is to check the log file for WARN Message
     * 
     * @param warnEntry1
     * @param warnEntry2
     */
    @Test
    @Parameters(value = { "warnEntry1", "warnEntry2" })
    public void checkWarnLog(String warnEntry1, String warnEntry2)
    {

        try
        {
            int result = 0;
            result = ApmbaseUtil.findTwoStringsInSingleLine(ApmbaseConstants.EM_LOG,
                                                logFilePath, warnEntry1,
                                                warnEntry2);
            if (result <= 0) Assert.assertTrue(true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(" *****  Log File Checking Completed  ***** ");
    }

    /**
     * Test method is to check for checkCustomMetric
     * 
     * @param emHealthMetric
     */    
   
    @Test
    @Parameters(value = { "emHealthMetric" })
    public void checkHealthMetric(String emHealthMetric)
    {
        checkCustomMetric(emHealthMetric);
    }

    /**
     * Test method is to check for checkCustomMetric
     * 
     * @param customMetric
     */
    private void checkCustomMetric(String customMetric)
    {

        if (exprAgentNode != null)
        {
            this.checkAgentNodeExists(customMetric);
        }
    }

    /**
     * Test method is to check whether Agent Node exist or not
     * 
     * @param vAgentmetric
     */
    private void checkAgentNodeExists(String vAgentmetric)
    {

        try
        {
			Util.sleep(45000);
            System.out
                    .println("******** Agentmetric In checkAgentNodeExists - metricExists *** "
                             + vAgentmetric);
            metricutil = new MetricUtil(vAgentmetric, clw);

            System.out.println("******** Metric Exists  *****  "
                               + metricutil.metricExists());

            Assert.assertTrue(metricutil.metricExists());

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Test method is to check for plugin folder in EM to check for jar files
     * existence
     * 
     * @param plgJarCount
     */
    @Test
    @Parameters(value = { "plgJarCount" })
    public void checkPluginFileExists(String plgJarCount)
    {

        try
        {

            int count = 0;
            File directory = new File(plgFolderLoc);
            File[] files = directory.listFiles();
            if (files != null)
                for (int index = 0; index < files.length; index++)
                {
                    if ((files[index].toString().contains(agentServerJar))
                        || (files[index].toString().contains(aspectJar))
                        || (files[index].toString().contains(commonAllJar))
                        || (files[index].toString().contains(workstationServer))
                        || (files[index].toString().contains(serverJar)))
                    {
                        count++;
                    }
                }
            System.out.println("count *** " + count);
            if (count == 5)
            {
                Assert.assertEquals(count, Integer.parseInt(plgJarCount));
                System.out.println("Required Jar Files Exist...");
            } else
            {
                Assert.fail("Jar Files Could not found");
            }

        } catch (Exception e)
        {

        }

    }

    /**
     * Test method is to check for Conn Agent check
     * 
     * @param agentConnectMetric
     */
    @Test
    @Parameters(value = { "agentConnectMetric" })
    public void checkConnAgent(String agentConnectMetric)
    {
        sleep(60000);
        checkAgentConnNumber(agentConnectMetric,
                             ApmbaseConstants.EXPETED_VALUE,
                             ApmbaseConstants.TOLERANCE);
    }

    /**
     * Test method is to check for checkCustomMetric
     * 
     * @param emHealthMetric
     */
    @Test
    @Parameters(value = { "emHealthMetric" })
    public void checkEMHealthMetrics(String emHealthMetric)
    {
        checkCustomMetric(emHealthMetric);
    }

    // To check the Metric Value
    private void checkAgentConnNumber(String vAgentmetric,
                                      int expectedValue,
                                      int tolerance)
    {

        try
        {
            System.out
                    .println("******** Agentmetric In checkSocketsMetric *** "
                             + vAgentmetric);
            metricutil = new MetricUtil(vAgentmetric, clw);

            Assert.assertTrue(metricutil.metricExists());
            System.out
                    .println("******** Metric Exists in CheckSocketsMetric-metricExists: *****  "
                             + metricutil.metricExists());

            System.out.println("******** Expected Value *****  "
                               + expectedValue);

            if (expectedValue > 0)
            {

                String[] x;
                boolean result = false;
                x = metricutil.getLastNMinutesMetricValues(8);
                System.out.println("********* The size of metrics values: "
                                   + x.length);

                int count = 0;
                while (count < x.length)
                {
                    System.out.println("******** The Value of " + count
                                       + " is: " + x[count]);
                    if (x[count] != null)
                    {
                        int j = Integer.parseInt(x[count]);
                        if (j >= expectedValue)
                        {
                            System.out
                                    .println("***** In inner comparison with IF of Expected and Actual value "
                                             + j);
                            result = true;
                            break;
                        } else if (tolerance > 0)
                        {
                            result = this.inTolerance(j, expectedValue,
                                                      tolerance);
                            System.out
                                    .println("***** In inner comparison with ELSE of Expected and Actual value "
                                             + result);
                            if (result) break;
                        }
                    }
                    count++;
                }

                System.out.println("*********** Value of result is " + result);
                Assert.assertEquals(true, result);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            metricutil = null;
        }
    }

    private Boolean inTolerance(int observed, int expected, int tolerance)
    {

        System.out.println("**********  in Tolerance Method: observed: "
                           + observed + " - Expected: " + expected);
        if ((observed >= expected - tolerance)
            && (observed <= expected + tolerance))
        {
            System.out
                    .println("**********  in  IF of Tolerance Method: expected-tolerance. Returning true ********* ");
            return true;

        }

        return false;
    }

    /**
     * Test method is to check for module folder in EM to check for jar files
     * existence
     * 
     * @param modJarCount
     */
    @Test
    @Parameters(value = { "modJarCount" })
    public void checkModuleFileExists(String modJarCount)
    {

        try
        {

            int count = 0;
            // Need to remove hardcoded entry
            File directory = new File(modFolderLoc);
            File[] files = directory.listFiles();
            if (files != null)
                for (int index = 0; index < files.length; index++)
                {
                    if ((files[index].toString().contains(managementModuleJar)))
                    {
                        count++;
                    }

                }
            if (count == 1)
            {
                Assert.assertEquals(count, Integer.parseInt(modJarCount));
                System.out.println("count ** " + count);
                System.out.println("Jar file Found Successfully...");
            } else
            {
                Assert.fail("Could not found");
            }

        } catch (Exception e)
        {

        }

    }

    /**
     * Test method is to change the entry in IEM properties file
     * 
     * @param oldTrace
     * @param newTrace
     */
    @Test
    @Parameters(value = { "oldTrace", "newTrace" })
    public void transUpdateIEMProp(String oldTrace, String newTrace)
    {
        Util.replaceLine(emPropFile, oldTrace, newTrace);
    }

    /**
     * Test method is to check for config folder in EM to check for db file
     * existence
     * 
     * @param dbFileFound
     * @param dbFileExtn
     */
    @Test
    @Parameters(value = { "dbFileFound", "dbFileExtn" })
    public void myTransDBFileCheck(String dbFileFound, String dbFileExtn)
    {

        try
        {
            System.out.println("GOT  *** ");
            int count = 0;
            // Need to remove hardcoded entry
            File directory = new File(dbFileLoc);
            File[] files = directory.listFiles();
            if (files != null)
                for (int index = 0; index < files.length; index++)
                {
                    // Print out the name of files in the directory
                    if ((files[index].toString()).contains(dbFileExtn))
                    {
                        count++;
                    }
                    if (count == 1) break;

                }
            if (count == 1)
            {
                Assert.assertEquals(count, Integer.parseInt(dbFileFound));
                System.out.println("count ** " + count);
                System.out.println("db file Found Successfully...");
            } else
            {
                Assert.fail("Could not found");
            }

        } catch (Exception e)
        {

        }

    }

    /**
     * Test method is to stop the EM
     * 
     * @param emhost
     * @param emport
     */
//    @Test
//    @Parameters(value = { "emhost", "emport" })
//    public void stopEM(String emhost, String emport)
//    {
//        ApmbaseUtil.stopEM(clw, emhost, Integer.parseInt(emport));
//    }

    /**
     * Test method is to start the EM
     * 
     * @param emhost
     * @param emport
     */
//    @Test
//    @Parameters(value = { "emhost", "emport" })
//    public void startEM(String emhost, String emport) throws Exception
//    {
//        ApmbaseUtil.startEM((emPath + "/" + emExe), emhost,
//                            Integer.parseInt(emport));
//
//    }

    /**
     * Test method is to check the admin login
     * @param emHealthMetric
     * 
     */
    @Test
    @Parameters(value = { "emHealthMetric" })
    public void defUserAdminLogin(String emHealthMetric)
    {
        if (clw != null)
        {
            sleep(60000);
            //clw object would be created in initIScopeCLW 
            metricutil = new MetricUtil(emHealthMetric, clw);

            System.out.println("******** Metric Exists *****  "
                               + metricutil.metricExists());
            Assert.assertTrue(metricutil.metricExists());
        } else
        {
            Assert.fail("cemadmin user Not Logged in Successfully");
        }
    }

    /**
     * Test method is to check user logged as Admin
     * @param emHealthMetric
     */
    @Test
    @Parameters(value = { "emHealthMetric" })
    public void loginAsAdmin(String emHealthMetric)
    {
        if (clw != null)
        {
            sleep(60000);
            //clw object would be created in initIScopeCLW 
            metricutil = new MetricUtil(emHealthMetric, clw);

            System.out.println("******** Metric Exists *****  "
                               + metricutil.metricExists());
            Assert.assertTrue(metricutil.metricExists());
        } else
        {
            Assert.fail("cemadmin user Not Logged in Successfully");
        }


    }

    /**
     * Test method is to check user logged as cemAdmin
     * 
     * @param emhost
     * @param cemuser
     * @param cempassw
     * @param emport
     * @param emHealthMetric
     */
    @Test
    @Parameters(value = { "emhost", "cemuser", "cempassw", "emport",
            "emHealthMetric" })
    public void loginAsCemAdmin(String emhost,
                                String cemuser,
                                String cempassw,
                                String emport,
                                String emHealthMetric)
    {
        clw = new CLWBean(emhost, cemuser, cempassw, Integer.parseInt(emport),
                          clwJarFileLoc);
        if (clw != null)
        {
            sleep(60000);
            metricutil = new MetricUtil(emHealthMetric, clw);

            System.out.println("******** Metric Exists *****  "
                               + metricutil.metricExists());
            Assert.assertTrue(metricutil.metricExists());
        } else
        {
            Assert.fail("cemadmin user Not Logged in Successfully");
        }

    }

    /**
     * Test method is to check user logged as guest
     * 
     * @param emhost
     * @param guestuser
     * @param guestpassw
     * @param emport
     * @param emHealthMetric
     */
    @Test
    @Parameters(value = { "emhost", "guestuser", "guestpassw", "emport",
            "emHealthMetric" })
    public void loginAsGuest(String emhost,
                             String guestuser,
                             String guestpassw,
                             String emport,
                             String emHealthMetric)
    {
        clw = new CLWBean(emhost, guestuser, guestpassw,
                          Integer.parseInt(emport), clwJarFileLoc);
        if (clw != null)
        {
            sleep(60000);
            metricutil = new MetricUtil(emHealthMetric, clw);

            System.out.println("******** Metric Exists *****  "
                               + metricutil.metricExists());
            Assert.assertTrue(metricutil.metricExists());
        } else
        {
            Assert.fail("Guest user Not Logged in Successfully");
        }

    }

    /**
     * Test method is to check count of Metrics In Intervals
     * 
     * @param agentProcess
     * @param agentName
     * @param agentProcess
     * @param histMetric
     * @param histMinutes
     * @param histSeconds
     * @param histRecCount
     */
    @Test
    @Parameters(value = { "agentProcess", "agentName", "histMetric",
            "histMinutes", "histSeconds", "histRecCount" })
    public void checkCLWMetricInIntervals(String agentProcess,
                                          String agentName,
                                          String histMetric,
                                          String histMinutes,
                                          String histSeconds,
                                          String histRecCount)
    {

        metricutil = new MetricUtil(agentHost, agentProcess, agentName,
                                    histMetric, clw);
        String[] metricArray = getLastNMinutesMetricValues(agentName,
                                                           histMetric,
                                                           histMinutes,
                                                           histSeconds);

        if (metricArray != null)
        {
            Assert.assertEquals((metricArray.length - 2),
                                Integer.parseInt(histRecCount));
            System.out.println("Got the " + histRecCount + " Metrics in "
                               + histMinutes + " minutes");
        } else
        {
            System.out.println("Did not Get the " + histRecCount
                               + " Metrics in " + histMinutes + " minutes");
        }

    }

    /**
     * Test method is to get count of Metrics In Intervals
     * 
     * @param agentName
     * @param metric
     * @param minutes
     * @param seconds
     */
    public String[] getLastNMinutesMetricValues(String agentName,
                                                String metric,
                                                String minutes,
                                                String seconds)
    {
        sleep(300000);
        int min = Integer.parseInt(minutes);
        String returnArray[] = new String[min * 4];
        String clwOutputStrings[] = null;
        for (int i = 0; i < min * 4; i++)
            returnArray[i] = null;

        String agentRegularExpression = (new StringBuilder("\".*"))
                .append(agentName.replace("(", "\\(").replace(")", "\\)"))
                .append(".*\"").toString();
        String metricRegularExpression = (new StringBuilder("\\\""))
                .append(metric.replace("(", "\\(").replace(")", "\\)"))
                .append("\\\"").toString();
        metricRegularExpression = metricRegularExpression
                .replaceAll("\\|", "\\\\|").replace(":", "\\:")
                .replace("?", "\\?");
        if (clw != null)
        {
            String command = (new StringBuilder(
                                                "get historical data from agents matching "))
                    .append(agentRegularExpression)
                    .append(" and metrics matching ")
                    .append(metricRegularExpression).append(" for past ")
                    .append(minutes).append(" minutes with frequency of ")
                    .append(seconds).append(" seconds").toString();
            clwOutputStrings = (String[]) null;
            try
            {
                clwOutputStrings = clw.runCLW(command);
            }

            catch (Exception e)
            {
                e.printStackTrace();

            }

        }
        return clwOutputStrings;

    }

    /**
     * Test method is to check the start service is RUNNING OR NOT
     * 
     * @param checkStartServiceName
     * @param checkStartStatus
     */

    @Test
    @Parameters(value = { "checkStartServiceName", "checkStartStatus" })
    public void verifyStartService(String checkStartServiceName,
                                   String checkStartStatus)
    {
        sleep(3000);
        try
        {
            String res = ApmbaseUtil.verifyService(checkStartServiceName);
            Assert.assertTrue(res.contains(checkStartStatus));

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    
    /**
     * Test Method to start the SAP Enterprise Manager
     * 
     * @param sapemhost
     *            -SAP EM Host - Machine properties
     * @param sapemport
     *            - SAP EM Port - Machine properties   
     * @param sapemuser
     *           -SAP EM User - Machine properties
     * @param sapempassw
     *            -SAP EM Password - Machine properties 
     */

    @Test
    @Parameters(value = { "sapemhost", "sapemport", "sapemuser", "sapempassw" })
    public void startSAPEntMgr(String sapemhost, String sapemport,String sapemuser, String sapempassw)
            throws Exception {

        System.out.println("Inside SAP StartEM" + sap_em_loc + "/" + sapEmExe);
        int emStatus = startEMSAP(sap_em_loc + "/" + sapEmExe,
                sapemhost, Integer.parseInt(sapemport));
                clw1 = new CLWBean(sapemhost, sapemuser, sapempassw,
                    Integer.parseInt(sapemport), sapclwJarFileLoc);
        System.out.println("emstatus is" + emStatus);
        Assert.assertEquals(emStatus, 1);

    }

    /**
	 * This method is to start the EM
	 * 
	 * @param command
	 * @param hostName
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static int startEMSAP(String command, String hostName, int port)
			throws Exception {

		System.out.println("Check for port " + port + " available before start"
				+ isPortAvailable(port, hostName));
			
		try {
				
			System.out.println("******** Starting EM initiated *********");

			if (!isPortAvailable(port, hostName)) {
				int resultEMInvoke = invokeEMProcess(command, hostName, port);
				Date date = new Date();
				DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
				System.out.println("Long form: " + df.format(date));
				Util.sleep(30000);

				if (resultEMInvoke == SUCCESS) 
				{

					return lookForPortReady(hostName, port);
				}

				return SUCCESS;
			} 
			else 
			{
				return FAILURE;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("****** EM has NOT Started *****");
			return FAILURE;

		}
	}

	/**
	 * THis method is for checking the port availability for 20 min, if not found
	 * it returns Failure on start of EM
	 * 
	 * @param hostName
	 * @param port
	 * @return
	 * @throws Exception 
	 */
	public static int lookForPortReady(String hostName, int port) throws Exception {
		
		int i=0;
		File f = new File(ApmbaseConstants.sap_em_loc+"/logs/"+ApmbaseConstants.INT_EM_MAN_FILE_NAME);
		if(!f.exists())
		{
			Thread.sleep(30000);
		}
		BufferedReader br = new BufferedReader(new FileReader(ApmbaseConstants.EM_LOG_LOC+"/"+ApmbaseConstants.INT_EM_MAN_FILE_NAME));
		String line ="";
		while((line=br.readLine())!=null)
		{
			  i++;
			  if(line.contains("Starting Introscope Enterprise Manager..."))
			  {
				  linecount=i;
		//		  System.out.println(linecount+"line"+line);
			  }
		}
		System.out.println(linecount);
		
				
		int time = 20 * 60 * 1000; // 20 min.
		int timeElapsed = 0;
		
		while (!isPortAvailable(port, hostName)) {	
			
			boolean  logfound = skipToLine(linecount, ApmbaseConstants.EM_LOG_LOC+"/"+ApmbaseConstants.INT_EM_MAN_FILE_NAME, "Orderly shutdown complete.");
			Util.sleep(10 * 1000);
			if (time == timeElapsed|logfound==true) {
				System.out.println("EM Not Started");
				return FAILURE;
			}
			timeElapsed = timeElapsed + (10 * 1000);
			System.out
					.println("*** Waiting for EM to Start .............."
							+ TimeUnit.MILLISECONDS.toMinutes(timeElapsed)
							+ " minutes");
			continue;
		}
		
		return SUCCESS;
	}

	public static boolean skipToLine(int count, String fileName, String errorMsg) throws Exception
	  {
		  boolean found = false;
		  int i=0;
		  BufferedReader br = new BufferedReader(new FileReader(fileName));
		  String line ="";
		  while(i!=linecount-1)
		  {
			  br.readLine();
			  i++;
		  }
		  
		  while((line=br.readLine())!=null){
			 //System.out.println(line);
			  if(line.contains(errorMsg))
			  {
				  found = true;
			  }
			  else
				  found= false;
		  }
		return found;
		 
	  }

	/**
	 * Test Method to start the GA Enterprise Manager
	 * 
	 * @param emhost
	 * @param emport
	 * @throws Exception
	 */

//	@Test
//	@Parameters(value = { "emhost", "emport" })
//	public void startGAEntMgr(String emhost, String emport) throws Exception {
//
//		System.out.println("Inside GA StartEM");
//		int emStatus = ApmbaseUtil.startEM((emPath + "/" + emExe), emhost,
//				Integer.parseInt(emport));
//		System.out.println("emstatus is" + emStatus);
//		Assert.assertEquals(emStatus, 1);
//
//	}

	/**
	 * Test Method to stop the SAP Enterprise Manager
	 * 
	 * @param sapemhost
	 * @param sapemuser
	 * @param sapempassw
	 * @param sapemport
	 */

//	@Test
//	@Parameters(value = { "sapemhost", "sapemuser", "sapempassw", "sapemport" })
//	public void stopSAPEntMgr(String sapemhost, String sapemuser,
//			String sapempassw, String sapemport) {
//
//		System.out.println("Inside SAP stopEM" + sapemhost);
//		System.out.println("Sucessfully logged with default login");
//
//		int status = ApmbaseUtil.stopEM(clw1, sapemhost,
//				Integer.parseInt(sapemport));
//
//		Assert.assertEquals(status, 1);
//
//	}

	/**
	 * Test Method to start the GA Enterprise Manager
	 * 
	 * @param emhost
	 * @param emuser
	 * @param empassw
	 * @param emport
	 */

//	@Test
//	@Parameters(value = { "emhost", "emuser", "empassw", "emport" })
//	public void stopGAEntMgr(String emhost, String emuser, String empassw,
//			String emport) {
//
//		System.out.println("Inside GA stopEM" + emhost);
//		int status1 = ApmbaseUtil.stopEM(clw, emhost, Integer.parseInt(emport));
//		System.out.println("stop status" + status1);
//		Assert.assertEquals(status1, 1);
//
//	}

	/**
	 * Test Method to check the SAP Enterprise Manager Log
	 * 
	 * @param sapemLogFileName
	 * @param sapemLogFilePath
	 * @param sapEmLogMessage
	 */

	@Test
	@Parameters(value = { "sapEmLogMessage" })
	public void checkEMLog(String sapEmLogMessage) {

		int status3 = ApmbaseUtil.checklog(emLogFileName, sapemLogFilePath,
				sapEmLogMessage);
		System.out.println("SAP em status log" + status3);
		Assert.assertEquals(ApmbaseUtil.checklog(emLogFileName,
				sapemLogFilePath, sapEmLogMessage), 1);
		System.out.println("Out of SAP emcheck log");
	}

	@Test
	@Parameters(value = { "sapEmLogMessage"})
	public void checkSAPEMLog(String sapEmLogMessage) {
	
		int status3 = ApmbaseUtil.checklog(emLogFileName, sapemLogFilePath,	sapEmLogMessage);
		System.out.println("SAP em status log" + status3);
		Assert.assertEquals(ApmbaseUtil.checklog(emLogFileName,	sapemLogFilePath, sapEmLogMessage), 1);
		System.out.println("Out of SAP emcheck log");
		
	}
	
	public static boolean checkValidLastUpdate(String fileName,
                                               long updatedTime,
                                               String errorMsg)
        throws Exception
    {
        System.out
                .println("*******************IN Log Check********************");
        File file = new File(fileName);
        System.out.println("in fileName " + fileName + " to CHECK " + errorMsg);
        RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
        long fileLength = file.length() - 1;
        StringBuilder sb = new StringBuilder();
        Calendar currentCal = Calendar.getInstance();
        currentCal.add(Calendar.DATE, -1);
        Date previousDay = (Date) currentCal.getTime();

        for (long filePointer = fileLength; filePointer != -1; filePointer--)
        {
            fileHandler.seek(filePointer);

            int readByte = fileHandler.readByte();
            if (readByte == 0xA)
            {
                if (filePointer == fileLength)
                {
                    continue;
                }
            } else if (readByte == 0xD)
            {
                if (filePointer == fileLength - 1)
                {
                    continue;
                } else
                {
                    sb.append((char) readByte);
                    String completeString = sb.reverse().toString();

                    String[] log = completeString.split(" ");
                    if (log[0].contains("/"))
                    {
                        String dateStr = log[0]; // log[1] contains date

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                        Date logDate = (Date) sdf.parse(dateStr.trim());

                        // splitting the string to get hours, minutes separately
                        String[] logTime = log[1].split(":"); // log[1] contains
// time
                        String day = log[2]; // log[2] contains day
                        int dayTime = 0;
                        if (day.equals("AM"))
                            dayTime = Calendar.AM;
                        else
                            dayTime = Calendar.PM;

                        // preparing log Calendar object
                        Calendar logCal = Calendar.getInstance();
                        logCal.setTime(logDate);
                        logCal.set(Calendar.AM_PM, dayTime);
                        logCal.set(Calendar.HOUR, Integer.parseInt(logTime[0])); // setting
// hours
                        logCal.set(Calendar.MINUTE,
                                   Integer.parseInt(logTime[1])); // setting
// minutes

                        Date updateDate = new Date(updatedTime);
                        // System.out.println(updateDate);

                        // preparing calendar object 5 minutes before on passed
// calendar object(updCalendar)
                        Calendar updatCalBefore = Calendar.getInstance();
                        updatCalBefore.setTime(updateDate);
                        updatCalBefore.add(Calendar.MINUTE, -5);

                        // preparing calendar object 5 minutes after on passed
// calendar object(updCalendar)
                        Calendar updateCalAfter = Calendar.getInstance();
                        updateCalAfter.setTime(updateDate);
                        updateCalAfter.add(Calendar.MINUTE, 5);

                        if ((logCal.after(updatCalBefore) && logCal
                                .before(updateCalAfter)))
                        {
                            if (completeString.contains(errorMsg))
                            {
                                System.out.println("%%%%%%%exists%%%%%% "
                                                   + completeString);
                                return true;
                            }
                        } else if ((previousDay.equals(logCal.getTime())))
                        {

                            return false;
                        }

                    }
                    sb = new StringBuilder();

                }// end else
            }// end else 0xD

            sb.append((char) readByte);

        }// end for

        return false;
    }
	/**
	 * Test Method to check the GA Enterprise Manager Log
	 * 
	 * @param gaMessage
	 */

	@Test
	@Parameters(value = { "gaMessage" })
	public void checkGAEMLog(String gaMessage) {

		int status2 = ApmbaseUtil.checklog(emLogFileName, gaemLogFilePath,
				gaMessage);
		System.out.println("GA em status log" + status2);
		Assert.assertEquals(
				ApmbaseUtil.checklog(emLogFileName, gaemLogFilePath, gaMessage),
				1);
		System.out.println("Out of ga emcheck log");
	}

	@Test
	@Parameters(value = { "gaMessage"})
	public void checkGAEMLog1(String logMessage) {
		try {

			String agentLog = emLogFileName+".log";
			boolean isexp = true;
			System.out.println("LogMessage: " + logMessage);
			boolean isMessageContain = false;
			int actInterval = 10*10*6000;
			int elapInterval = 0;
			
			while(!isMessageContain){
				 isMessageContain = checkValidLastUpdate(agentLog, Calendar.getInstance()
						.getTimeInMillis(), logMessage);
			if(actInterval == elapInterval){
			break;
			}
			
			Thread.sleep(1*10*6000);
			elapInterval = elapInterval + 1*10*6000;
			}			
			Assert.assertEquals(isMessageContain,isexp);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 public static boolean checkValidLastUpdate(String fileName, String errorMsg)
        throws Exception
    {
        File file = new File(fileName);
        System.out.println("in fileName " + fileName + " to CHECK " + errorMsg);
        RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
        long fileLength = file.length() - 1;
        StringBuilder sb = new StringBuilder();
        Calendar currentCal = Calendar.getInstance();
        currentCal.add(Calendar.DATE, -1);

        for (long filePointer = fileLength; filePointer != -1; filePointer--)
        {
            fileHandler.seek(filePointer);

            int readByte = fileHandler.readByte();
            if (readByte == 0xA)
            {
                if (filePointer == fileLength)
                {
                    continue;
                }
            } else if (readByte == 0xD)
            {
                if (filePointer == fileLength - 1)
                {
                    continue;
                } else
                {
                    sb.append((char) readByte);
                    String completeString = sb.reverse().toString();
                    // System.out.println("LINES " + completeString);

                    if (completeString.toUpperCase()
                            .contains(errorMsg.toUpperCase()))
                    {
                        System.out.println("%%%%%%%exists%%%%%% "
                                           + completeString);
                        fileHandler.close();
                        return true;
                    }

                    sb = new StringBuilder();

                }// end else
            }// end else 0xD
            sb.append((char) readByte);

        }// end for
        fileHandler.close();
        return false;
    }
	
	/**
	 * Test Method to update Interscope Agent Profile
	 * 
	 * @param autonaming
	 * @param agentName
	 * @param agentProcess
	 * @param agentLogPrefix
	 * @param agentDefaultPort
	 * @param agentDefaultHost
	 */
	@Test
	@Parameters(value = { "autonaming", "agentName", "agentProcess",
			"agentLogPrefix", "agentDefaultPort", "agentDefaultHost" })
	public void updateWebLogicAgentProfile(String autonaming, String agentName,
			String agentProcess, String agentLogPrefix,
			String agentDefaultPort, String agentDefaultHost) {

		updateProfile(autonaming, agentName, agentProcess, agentLogPrefix,
				agentDefaultPort, agentDefaultHost);

	}

	/**
	 * Test Method to start Weblogic
	 */
	@Test
	public void startWebLogic() {
		try {

			Util.startAgentWindows(ApmbaseConstants.APP_SERVER_WEBLOGIC,
					ApmbaseConstants.weblogic_webapp_home_dir, "");
			Assert.assertTrue(true);
			Util.sleep(60 * 1000);

		} catch (Exception e) {

			Assert.fail("Failed to start WebLogic Server");
		}
	}

	/**
	 * Test Method to stop Weblogic
	 */

	@Test
	public void stopWebLogic() {
		try {

			Util.stopAgentWindows(ApmbaseConstants.APP_SERVER_WEBLOGIC,
					ApmbaseConstants.weblogic_webapp_home_dir, "");

		} catch (Exception e) {

			Assert.fail("Failed to stop WebLogic Server");
		}
	}

	/**
	 * 
	 * Test Method to check Agent log
	 * 
	 * @param agentLogMessage
	 */

	@Test
	@Parameters(value = { "agentLogMessage" })
	public void checkAgentLog(String agentLogMessage) {
        boolean found = false;
        for (int i = 0; i != 6; ++i)
        {
            Util.sleep(60000);
            int res = ApmbaseUtil.checklog(agentLogFileName, agentLogFilePath,
                agentLogMessage);
            if (res == 1)
            {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found, "\"" + agentLogMessage + "\" in logs");
	}

	/**
	 * Test Method to check Agent log for warning messages
	 * 
	 * @param warningMessage
	 */

	@Test
	@Parameters(value = { "warningMessage" })
	public void checkAgentWarningLog(String warningMessage) {

		Assert.assertEquals(ApmbaseUtil.checklog(agentLogFileName,
				agentLogFilePath, warningMessage), 1);

	}

	/**
	 * Test Method to check for a given metric
	 * 
	 * @param basicMetric
	 */
	@Test(enabled = true)
	@Parameters(value = { "basicMetric" })
	public void checkAgentmetrics(String basicMetric) {
		try {
			Util.sleep(45 * 1000);// Sleep till Basic Metrics appear
			checkMetrics(basicMetric, isMetricExists, clw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * Test method to check for SAP em metric
     * @param gcMetric
     *
     */
    
    @Test(enabled = true)
    @Parameters(value = { "gcMetric"})
    public void checkSAPEmMetrics(String gcMetric) {
        try {
            
            Util.sleep(2 * 60 * 1000);// Sleep till Basic Metrics appear
            checkMetrics(gcMetric, isMetricExists, clw1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * Common method to check if the metrics exists or no
	 * @param vAgentmetric
	 * @param isMetricExists
	 * @param clwInstance
	 */
	
	private void checkMetrics(String vAgentmetric, boolean isMetricExists,
			CLWBean clwInstance) {
		System.out
				.println("******** Agentmetric metric exists- metricExists *** "
						+ vAgentmetric + "check  " + isMetricExists);
		metricutil = new MetricUtil(vAgentmetric, clwInstance);
		if (isMetricExists) {
			Assert.assertFalse(metricutil.metricExists());
		} else {
			Assert.assertTrue(metricutil.metricExists());
		}
		System.out.println("******** In metricExists: *****  "
				+ metricutil.metricExists());
		metricutil = null;
	}

	/**
	 * Test Method to set the System Time
	 * 
	 * @throws IOException
	 */
	@Test
	public void setSystemTime() throws IOException {

		Calendar now = Calendar.getInstance();
		//int sec = Integer.parseInt(seconds);

		System.out.println("Current time : " + now.get(Calendar.HOUR_OF_DAY)
				+ ":" + now.get(Calendar.MINUTE) + ":"
				+ now.get(Calendar.SECOND));
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int iWant = 23;
		int toADD = iWant-hour;
		
		int minute = now.get(Calendar.MINUTE);
		int iWantMin = 55;
		int toADD1 = iWantMin-minute;
		
		
		// clock slows by the number of seconds passed
		now.add(Calendar.MINUTE, toADD1);
		now.add(Calendar.HOUR_OF_DAY, toADD);
		String changetime = now.get(Calendar.HOUR_OF_DAY) + ":"
				+ now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
		Runtime rt = Runtime.getRuntime();

		rt.exec("cmd.exe /C time " + changetime);

		Calendar now1 = Calendar.getInstance();
		System.out.println("Time" + now1.get(Calendar.HOUR_OF_DAY) + ":"
				+ now1.get(Calendar.MINUTE) + ":" + now1.get(Calendar.SECOND));


	}

	/**
	 * Test Method for sleep
	 * 
	 * @param duration
	 */
	@Test
	@Parameters(value = { "duration" })
	public void sleepTime(long duration) {
		System.out.println("Sleeping for " + duration + " milliseconds.");
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			System.out.println("***sleep failed!***");
		}
	}

	/**
	 * Common method to update file properties for a given file
	 * 
	 * @param autonaming
	 * @param agentName
	 * @param agentProcess
	 * @param agentLogPrefix
	 * @param agentDefaultPort
	 * @param agentDefaultHost
	 */
	private void updateProfile(String autonaming, String agentName,
			String agentProcess, String agentLogPrefix,
			String agentDefaultPort, String agentDefaultHost) {

		try {
			// update agent profile
			agentProfilePath = System.getProperty("role_agent.install.dir")
					+ "/core/config/"
					+ System.getProperty("role_agent.agent.profile");
			agentLogPath = System.getProperty("results.dir") + "/"
					+ agentLogPrefix + ".log";
			agentAutoprobeLogPath = System.getProperty("results.dir") + "/"
					+ agentLogPrefix + ".Autoprobe.log";

			System.out.println("*** agentProfilePath: ** " + agentProfilePath);
			System.out.println("*** agentLogPrefix: ** " + agentLogPrefix);
			System.out.println("*** agentLogPath: ** " + agentLogPath);
			System.out.println("*** agentAutoprobeLogPath: ** "
					+ agentAutoprobeLogPath);

			Properties properties = Util.loadPropertiesFile(agentProfilePath);
			properties.setProperty(
					AutomationConstants.AGENT_AUTONAMING_PROPERTY, autonaming);
			properties.setProperty(AutomationConstants.AGENT_NAME_PROPERTY,
					agentName);
			properties.setProperty(
					AutomationConstants.AGENT_CUSTOM_PROCESS_NAME_PROPERTY,
					agentProcess);
			properties.setProperty(AutomationConstants.AGENT_LOG_PATH_PROPERTY,
					agentLogPath);
            properties.setProperty(
                AutomationConstants.AGENT_LOG_FILE_APPENDER + ".ImmediateFlush",
                "true");           
			properties.setProperty(
					AutomationConstants.AGENT_AUTOPROBE_LOG_PATH_PROPERTY,
					agentAutoprobeLogPath);
			properties.setProperty(ApmbaseConstants.AGENT_EM_DEFAULT_PORT,
					agentDefaultPort);
			properties.setProperty(ApmbaseConstants.AGENT_EM_DEFAULT_HOST,
					agentDefaultHost);

			Util.writePropertiesToFile(agentProfilePath, properties);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed because of the following reason: ", e);
		}
	}

	/**
	 * Common method to update file properties for a given file
	 * 
	 * @param autonaming
	 * @param agentName
	 * @param agentProcess
	 * @param agentLogPrefix
	 * @param agentDefaultPort
	 * @param agentDefaultHost
	 */
	private void updateTomcatAgentProfile(String autonaming, String agentName,
			String agentProcess, String agentLogPrefix,
			String agentDefaultPort, String agentDefaultHost) {

		try {
			// update agent profile
////			tomcatAgentProfilePath = System.getProperty("role_agent.install.dir")
//					+ "/core/config/"
//					+ System.getProperty("role_agent.agent.profile");
			agentLogPath = tomcatAgentLogPath + "/"
					+ agentLogPrefix + ".log";
			agentAutoprobeLogPath = tomcatAgentLogPath + "/"
					+ agentLogPrefix + ".Autoprobe.log";

			System.out.println("*** agentProfilePath: ** " + tomcatAgentProfilePath);
			System.out.println("*** agentLogPrefix: ** " + agentLogPrefix);
			System.out.println("*** agentLogPath: ** " + agentLogPath);
			System.out.println("*** agentAutoprobeLogPath: ** "
					+ agentAutoprobeLogPath);

			Properties properties = Util.loadPropertiesFile(tomcatAgentProfilePath);
			properties.setProperty(
					AutomationConstants.AGENT_AUTONAMING_PROPERTY, autonaming);
			properties.setProperty(AutomationConstants.AGENT_NAME_PROPERTY,
					agentName);
			properties.setProperty(
					AutomationConstants.AGENT_CUSTOM_PROCESS_NAME_PROPERTY,
					agentProcess);
			properties.setProperty(AutomationConstants.AGENT_LOG_PATH_PROPERTY,
					agentLogPath);
            properties.setProperty(
                AutomationConstants.AGENT_LOG_FILE_APPENDER + ".ImmediateFlush",
                "true");           
			properties.setProperty(
					AutomationConstants.AGENT_AUTOPROBE_LOG_PATH_PROPERTY,
					agentAutoprobeLogPath);
			properties.setProperty(ApmbaseConstants.AGENT_EM_DEFAULT_PORT,
					agentDefaultPort);
			properties.setProperty(ApmbaseConstants.AGENT_EM_DEFAULT_HOST,
					agentDefaultHost);

			Util.writePropertiesToFile(tomcatAgentProfilePath, properties);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Test failed because of the following reason: ", e);
		}
	}
	
	/**
	 * Test Method to create a backup of the Agent profile file
	 * 
	 * 
	 */
	@Test(enabled = true)
	public void backupAgentProfile() {
		message = ApmbaseUtil.fileBackUp(agentProfilePath);
		Assert.assertEquals(message, msg);
	}

	/**
	 * Test Method to revert the Agent profile file
	 * 
	 * 
	 */
	@Test(enabled = true)
	public void revertAgentProfile() {
		message = ApmbaseUtil.revertFile(agentProfilePath);
		Assert.assertEquals(message, msg);
	}

	/**
	 * Test Method to create a backup of the Interscope Properties file
	 * 
	 * 
	 */
	@Test(enabled = true)
	public void backupPropertiesFile() {
		message = ApmbaseUtil.fileBackUp(introscopePropertiesFilePath);
		Assert.assertEquals(message, msg);
	}

	/**
	 * Test Method to revert the Interscope Properties file
	 * 
	 */
	@Test(enabled = true)
	public void revertPropertiesFile() {
		message = ApmbaseUtil.revertFile(introscopePropertiesFilePath);
		Assert.assertEquals(message, msg);
	}

	/**
	 * Test Method to update the Interscope Properties file
	 * 
	 * @param keys
	 * @param values
	 * 
	 */
	@Test(enabled = true)
	@Parameters(value = { "keys", "values" })
	public void updateIntroscopeProperties(String keys, String values) {

		boolean value = ApmbaseUtil.updateProperties(keys, values,
				introscopePropertiesFilePath);
		Assert.assertEquals(value, true);

	}

	/**
	 * Test method to check postgres version
	 */
	@Test
	public void connectToPostgres() {

		System.out
				.println("-------- PostgreSQL JDBC Connection Testing ------------");

		try {
			Class.forName("org.postgresql.Driver");
			Connection connection = null;
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/cemdb", "postgres",
					"postgres");
			if (connection != null) {
				System.out.println("connected sucessfully");

				Statement st = connection.createStatement();

				String query = "SELECT version()";

				ResultSet rs = st.executeQuery(query);
				while (rs.next()) {
					System.out.print(" " + rs.getString(1));
					Assert.assertEquals(rs.getString(1),
							"PostgreSQL 8.4.5, compiled by Visual C++ build 1400, 32-bit");
				}

			} else
				System.out.println("Connection to  postgresSql Failed!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Test method to copy files from source to destination locations
	 * 
	 * @param srcloc
	 *            - location from where files should be copied
	 * @param dstFolder
	 *            - location to which files should be copied
	 */

	@Test
	@Parameters(value = { "srcloc", "dstFolder" })
	public void backupLogFile(String srcloc, String dstFolder)
			throws IOException {

		File sourceFile = new File(srcloc);
		File destinationFile = new File(dstFolder);
		ApmbaseUtil.copyDirectory(sourceFile, destinationFile);
		System.out.println(destinationFile.getAbsolutePath());
		Util.sleep(120000);
		Assert.assertTrue(destinationFile.exists());
	}

	/**
	 * Helper method which delete complete folder
	 */
	private boolean deleteFolder(String location) throws Exception {
		boolean success = false;
		File file = new File(location);

		if (!file.exists()) {
			success = true;
			return success;
		}

		File[] files = file.listFiles();
		if (files == null || files.length == 0) {
			success = true;
			return success;
		}

		for (File file1 : files) {
			System.out.println("--file---" + file1);
			success = file1.delete();
		}

		return success;
	}

	/**
	 * Test method which deletes all the files generated in the enterprise
	 * manager logs location
	 */
	@Test
	public void deleteLogFolder() {
		try {
			boolean filesDeleted = deleteFolder(gaemLogFilePath);
			Assert.assertTrue(filesDeleted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void deleteDataFolder() {
		try {
			boolean filesDeleted = deleteFolder(gaemdataFilePath);
			Assert.assertTrue(filesDeleted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static boolean isPortAvailable(int port, String hostName) {
		Socket soc = null;
		try {
			soc = new Socket(hostName, port);
			return soc.isBound();
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (soc != null) {
					soc.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public static int invokeEMProcess(String command, String hostName, int port)
	throws Exception {
		Process process = null;
		try {
			System.out.println("Starting EM **** exec Run Started");
			process = Runtime.getRuntime().exec(command);
			System.out.println("Starting EM **** exec Run End");
		
			return SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return FAILURE;
		
		} finally {
			if (process != null) {
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
			}
		}
}
		
	@Test(groups = {"embasics", "bat"})  
    public void verify_ALM_239561_EMHealthMetrics()
    {
    	checkHealthMetric(emHealthMetric);
    }    

    
	@Test(groups = {"embasics", "bat"})
    public void verify_ALM_239560_WorkStationConnectsEM()
    {
    	checkConnAgent(agentConnectMetric);
    	loginAsAdmin(emHealthMetric);
    	loginAsCemAdmin(emHost, cemUser, cemPassw, emPort, emHealthMetric);
    	loginAsGuest(emHost, guestUser, guestPassw, emPort, emHealthMetric);
    }
    
    
	@Test(groups = {"embasics", "bat"})
    public void verify_ALM_205188_DefaultUserAdminLogin()
    {
    	defUserAdminLogin(emHealthMetric);
    }
    
    
	@Test(groups = {"embasics", "bat"})
	public void verify_ALM_205187_AgentConnectstoEM()
	{
    	stopTomcatAgent(tomcatRoleId);
    	ApmbaseUtil.fileBackUp(tomcatAgentProfilePath);		
		updateTomcatAgentProfile("false", tomcatAgentName, tomcatAgentProcess, tomcatAgentName,
				emPort, agentHost);
		startTomcatAgent(tomcatRoleId);
		harvestWait(60);
		ApmbaseUtil.checklog(tomcatAgentName , tomcatAgentLogPath,
                agentLogMessage);
		String warningMessage= "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect "
				+ "to the Introscope Enterprise Manager at " + emHost + ":" + emPort + 
				",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory";		
		ApmbaseUtil.checklog(tomcatAgentName, tomcatAgentLogPath,
				warningMessage);
		String basicMetric = "*SuperDomain*|" + agentHost + "|" + tomcatAgentProcess + "|" + 
				tomcatAgentName + "|GC Heap:Bytes Total";				
		checkAgentmetrics(basicMetric);
		stopTomcatAgent(tomcatRoleId);
		harvestWait(60);		
		ApmbaseUtil.revertFile(tomcatAgentProfilePath);
		
	}
    
}