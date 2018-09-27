package com.ca.apm.tests.propertyconfiguration;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.AutomationConstants;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.win.WinHKEY;
import com.ca.apm.commons.coda.common.win.WindowsRegistry;
import com.ca.apm.commons.coda.common.win.WindowsRegistryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;













import com.ca.apm.tests.common.Context;
import com.ca.apm.tests.common.IResponse;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.tasks.properties.PropertiesFileTask;
import com.ca.apm.tests.testbed.EMPropertyConfigurationtWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/***
 * This class is used to Execute the RebindPortCommTest Testcases
 * which was moved from Coda projects to TAS.
 * @by GAMSA03
 * 
 */
public class RebindPortCommTest extends BaseAgentTest
{
    /** holds the hudson run results path */
    private static String       resultsDir            = System.getProperty("results.dir");                            

    /** holds the logger object */
    private static Logger       logger                = Logger.getLogger(RebindPortCommTest.class);
    /** Holds the EM Log path */
   

    /** Holds the EM execution path */
    private String              emExeLoc              = ApmbaseConstants.em_install_parent_dir
                                                        + ApmbaseConstants.EM_FOLDER_NAME
                                                        + "/"
                                                        + ApmbaseConstants.EM_EXE;

    private String emRoleId = EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID;
    /** holds clw bean instance */
    private CLWBean             clw                   = null;

    /** Carries the class level workstation Jar file location */
    private String              clwJarFileLoc         = System.getProperty("testbed_em.install.parent.dir")
                                                        + AutomationConstants.CLWJARLOCATION;

    Process                     process               = null;
    String             emHost           = envProperties
            .getMachineHostnameByRoleId(EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID);
    String             emPath              = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    String             emPropFile          = emPath + ApmbaseConstants.EM_PROP_FILE;
    String              emLogpath             = emPath + ApmbaseConstants.IntroscopeEMfileName;
 String 	emUser = ApmbaseConstants.emUser;
    
    String 	emPassw = ApmbaseConstants.emPassw;
    
    String 	emPort = ApmbaseConstants.emPort;

    /**
     * This method is used to initialize the CLW Bean.
     * 
     * @param emhost
     *            //To pass EM host name
     * @param emuser
     *            //To pass EM user name
     * @param empassw
     *            //To pass EM user password
     * @param emport
     *            //To pass EM port
     */
    @BeforeTest(alwaysRun = true)
    
    public void initIscopeCLW()
    {
        logger.info("########## initIscopeCLW Start ##########");
        logger.info("***** CLW object parameters: *****");
        logger.info("emhost: " + emHost);
        logger.info("emuser: " + emUser);
        logger.info("empassw: " + emPassw);
        logger.info("emport: " + emPort);
        logger.info("CLW Jar file location: " + clwJarFileLoc);
        clw = new CLWBean(emHost, emUser, emPassw, Integer.parseInt(emPort),
                          clwJarFileLoc);
        logger.info("########## initIscopeCLW End ##########");
    }

    /**
     * Test method to sleep.
     * 
     * @param duration
     *            -Duration of the time to sleep in milliseconds
     */
    @Test
    @Parameters(value = { "duration" })
    public void sleep(long duration)
    {
        logger.info("########## sleep Start ##########");
        logger.info("duration: " + String.valueOf(duration) + "ms");
        Util.sleep(duration);
        logger.info("########## sleep End ##########");

    }

    /**
     * Test method is used before starting of every testcase.
     * 
     * @param testCaseNameIDPath
     *            - Passing perforce path and Testcase ID
     */
    @Test
    @Parameters(value = { "testCaseNameIDPath" })
    public void testCaseStart(String testCaseNameIDPath)
    {
        logger.info("---------- " + testCaseNameIDPath + " ----- Start");
    }

    /**
     * Test method is used at the end of every testcase.
     * 
     * @param duration
     *            -Passing perforce path and Testcase ID
     */
    @Test
    @Parameters(value = { "testCaseNameIDPath" })
    public void testCaseEnd(String testCaseNameIDPath)
    {
        logger.info("---------- " + testCaseNameIDPath + " ----- End");
    }

    /**
     * This method is to check the list of messages against the messages
     * generated on Console. This method is generalised for all other test
     * cases.
     * 
     * @param command
     *            --This Parameter Specifies the Command that needs to be
     *            Executed.
     * @param processName
     *            ---This Parameter Specifies the name of the process for which
     *            ID is required
     * @return
     * @throws Exception
     */
    private String getTimeWaitProcessID(String command, String processName)
        throws Exception
    {
        logger.info("########## getTimeWaitProcessID Start ##########");
        Process process = null;
        BufferedReader reader = null;
        String processID = null;
        try
        {
            boolean isFound = false;
            String[] startCmnd = new String[] { command };
            logger.info("In Execute method: " + startCmnd.length);
            logger.info("Command to Execute: " + startCmnd.toString());

            process = ApmbaseUtil.getProcess(startCmnd, "c:/");
            reader = new BufferedReader(
                                        new InputStreamReader(process
                                                .getInputStream()));
            String line = null;
			boolean portFound = false;
            while (((line = reader.readLine()) != null) && (portFound==false))
            {
                logger.info("Inside the Command: " + line);
                if (line.contains(processName))
                {
                    logger.info("Process: '" + line + "' is found");
                    InetAddress thisIp = InetAddress.getLocalHost();
                    String ipAddr = thisIp.getHostAddress();
                    logger.info("IP Adress: " + ipAddr);
                    if (line.contains(ipAddr))
                    {
                        int startInd = line.indexOf(':');
                        processID = line.substring(startInd + 1, 30);
						if(!processID.contains("8080"))
						{
							portFound = true;
							logger.info("process Id for TIME_WAIT: " + processID);
							break;
						}
                        else
						{
							logger.info("Ignoring process Id for TIME_WAIT since it selected Hudson's Port: " + processID);
							portFound = false;
						}
                    }
                }
            }
        } catch (Exception e)
        {
            logger.error("Unable to execute due to exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Unable to pass the method due to the Exception: "
                        + e.getMessage());

        } finally
        {
            if (process != null)
            {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
            }
        }
        logger.info("########## getTimeWaitProcessID End ##########");
        return processID;
    }

    /**
     * this Method will check the Log message in the given log path
     * 
     * @param logMessage
     *            - log message needs to be checked in the log
     * @param filePath
     *            - log File Path
     */
    private boolean checkLog(String logMessage, String filePath)
    {
        boolean isFound = false;
        try
        {
            logger.info("########## checkLog Start ##########");
            logger.info("########## log parameters ##########");
            logger.info("logMessage: " + logMessage);
            logger.info("filePath: " + filePath); 
            logger.info("isLogExists: " + isFound);
            String[] messages = logMessage.split("~");
            for (int i = 0; i < messages.length; i++)
            {

                isFound = ApmbaseUtil.checkValidLastUpdate(filePath,
                                                           messages[i]);
                logger.info("log message: '" + messages[i] + "', is log exists: " + isFound);
                if (!isFound)
                {
                    break;
                }
            }
            logger.info("########## checkLog End ##########");
        } catch (Exception e)
        {
            logger.error("Exception occurred!");
            logger.error(e.getMessage(), e);
            Assert.fail("Unable to pass the method due to the Exception: "
                        + e.getMessage());
        }
        return isFound;
    }

    /**
     * This method check log message in EM Logs
     * 
     * @param emMessage
     *            -log message to check
     * @param emLogFileNameAndPath
     *            -to pass the log location
     */
    @Test
    @Parameters(value = { "emMessage", "emLogFileNameAndPath" })
    public void checkNoEMLog(String emMessage, String emLogFileNameAndPath)
    {
        logger.info("########## checkNoEMLog Start ##########");
        boolean logfound = false;
        try
        {
            logger.info("Log message to check: " + emMessage);
            logfound = checkLog(emMessage, emLogFileNameAndPath);
        } catch (Exception e)
        {
            logger.error("Unable to check the EM log due to exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Unable to pass the method due to the Exception: "
                        + e.getMessage());
        }
        Assert.assertFalse(logfound);
        logger.info("########## checkNoEMLog End ##########");
    }

    /**
     * This method is used to get the process and update in property file.
     * 
     * @param emProp
     *            -To pass a name of the property
     * @param command
     *            -To pass the command to be executed
     * @param processName
     *            -To pass the name of the process
     * @param propertyFilePath
     *            -To pass file path of property
     */
    @Test
    @Parameters(value = { "emProp", "command", "processName",
            "propertyFilePath" })
    public void setProcessIdToEMprop(String emProp,
                                     String command,
                                     String processName,
                                     String propertyFilePath)
    {
        try
        {
            logger.info("########## setProcessIdToEMprop Start ##########");
            logger.info("Property to be updated: " + emProp);
            logger.info("Command to execute: " + command);
            logger.info("Process Name to get ID: " + processName);
            logger.info("propertyFilePath is: " + propertyFilePath);
            
            String timeWaitPort = getTimeWaitProcessID(command, processName);
            if (timeWaitPort == null) {
                logger.info("No port in " + processName);
                try {
                    URL url = new URL("http://www.google.com");
                    for (int i = 0; i < 3; i++) {
                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.connect();
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    // swallow all
                }
                timeWaitPort = getTimeWaitProcessID(command, processName);
            }
            Assert.assertTrue(timeWaitPort != null, "No port in " + processName);
            
            Assert.assertTrue(updateFileProperties(emProp,
                                                   timeWaitPort,
                                                   propertyFilePath));
            logger.info("########## setProcessIdToEMprop End ##########");
        } catch (Exception e)
        {
            logger.error("Unable to update the properties due to exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Unable to pass the method due to the exception: "
                        + e.getMessage());
        }
    }

    /**
     * This method is to check a given file exists or not
     * 
     * @param filePath
     *            -to pass the file path
     */
    private boolean checkFileExists(String filePath)
    {
        logger.info("########## checkFileExists Start ##########");
        boolean isExists = false;
        try
        {
            File file = new File(filePath);
            isExists = file.exists();
            logger.info("File '" + filePath + "' exists: " + isExists);
        } catch (Exception e)
        {
            logger.error("Unable to check the file '" + filePath+ "' due to the exception: ");
            logger.error(e.getMessage(), e);
        }
        logger.info("########## checkFileExists End ##########");
        return isExists;
    }

    /**
     * Method to update the properties in a given file
     * 
     * @param propertyKeys
     *            -to pass the name of the property
     * @param propertyValues
     *            -to pass the value of the property
     * @param filePath
     *            -to pass the property file path
     * 
     */
    private boolean updateFileProperties(String propertyKeys,
                                         String propertyValues,
                                         String filePath)
    {
        logger.info("########## updateFileProperties Start ##########");
        boolean isPropertyUpdated = false;
        boolean isFileExists = false;
        String[] propKeys = propertyKeys.split("~");
        String[] propValues = propertyValues.split("~");
        try
        {
            isFileExists = checkFileExists(filePath);
            if (isFileExists)
            {
                PropertiesFileTask task = new PropertiesFileTask(filePath);
                Context context = new Context();
                for (int i = 0; i < propKeys.length; i++)
                    task.setProperty(propKeys[i], propValues[i]);
                IResponse response = task.execute(context);
                // delay is to save the changes in the file
                Util.sleep(20000);
                logger.info("Sleeping for 20000 milliseconds");
                for (int i = 0; i < propKeys.length; i++)
                {
                    String propKey = propKeys[i];
                    String propertyValue = task.getProperties()
                            .getProperty(propKey);
                    logger.info("Property value is: " + propertyValue);
                    isPropertyUpdated = propertyValue
                            .equalsIgnoreCase(propValues[i]);
                    if (!isPropertyUpdated)
                    {
                        isPropertyUpdated = false;
                        logger.info("******** Property '" + propKey + "' not updated properly ********");
                        break;
                    }
                }
            } else
            {
                logger.info("******** FILE '" + filePath + "' NOT FOUND ********");
            }
        } catch (Exception e)
        {
            logger.error("Cannot update the property due to the exception: ");
            logger.error(e.getMessage(), e);
        }
        logger.info("########## updateFileProperties End ##########");
        return isPropertyUpdated;
    }

    /**
     * This method is to update the properties in a file
     * 
     * @param propertyKeys
     *            -to pass the name of the property
     * @param propertyValues
     *            -to pass the value of the property
     * @param propertyFilePath
     *            -to pass the property file path
     */
    @Test
    @Parameters(value = { "propertyKeys", "propertyValues", "propertyFilePath" })
    public void updateProperties(String propertyKeys,
                                 String propertyValues,
                                 String propertyFilePath)
    {
        logger.info("########## updateProperties Start ##########");
        logger.info("propertyKeys are: " + propertyKeys);
        logger.info("propertyValues are: " + propertyValues);
        logger.info("propertyFilePath is: " + propertyFilePath);
        boolean isPropertyUpdated = false;
        try
        {
            isPropertyUpdated = updateFileProperties(propertyKeys,
                                                     propertyValues,
                                                     propertyFilePath);
            logger.info("File properties update status: " + isPropertyUpdated);
        } catch (Exception e)
        {
            logger.error("Cannot update the property due to the exception: ");
            logger.error(e.getMessage(), e);
        }
        logger.info("########## updateProperties End ##########");
        Assert.assertTrue(isPropertyUpdated);
    }

    /**
     * This method is to create the registry key
     * 
     * @param registryPath
     *            -to pass the path of the registry
     * @param keyName
     *            -to pass the registry keyname
     * @param registryValue
     *            -to pass the registry keyvalue
     * @param status
     *            -to pass the status to check whether registry created or not
     */
    @Test
    @Parameters(value = { "registryPath", "keyName", "registryValue", "status" })
    public void createRegistryKey(String registryPath,
                                  String keyName,
                                  int registryValue,
                                  String status)
    {
        logger.info("########## createRegistryKey Start ##########");
        logger.info("Registry path to create a new key: " + registryPath);
        logger.info("Key name: " + keyName);
        logger.info("Key value to be set: " + registryValue);
        logger.info("Status of the key creation: " + status);
        WindowsRegistry winReg = WindowsRegistryFactory.getInstance().getWindowsRegistryImpl();
        try
        {
            winReg.setREG_DWORDValue(WinHKEY.HKEY_LOCAL_MACHINE, registryPath, keyName, registryValue);
            Assert.assertTrue(winReg.checkRegKeyExists(WinHKEY.HKEY_LOCAL_MACHINE, registryPath, keyName));
            Assert.assertEquals(winReg.getREG_DWORDValue(WinHKEY.HKEY_LOCAL_MACHINE, registryPath, keyName), registryValue);
        } catch (Exception e)
        {
            logger.error("Unable to create a registry key due to the exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Unable to pass the method due to the exception: "
                        + e.getMessage());
        }
        logger.info("######createRegistryKey Ended##########");
    }

    /**
     * This method is to create the registry key
     * 
     * @param registryPath
     *            -to pass the path of the registry
     * @param keyName1
     *            -to pass the registry keyname
     * @param registryValue1
     *            -to pass the registry keyvalue
     * @param status
     *            -to pass the status to check whether registry created or not
     */
    @Test
    @Parameters(value = { "registryPath", "keyName1", "registryValue1",
            "status" })
    public void createRegistryKey1(String registryPath,
                                   String keyName1,
                                   int registryValue1,
                                   String status)
    {
        logger.info("########## createRegistryKey1 Start ##########");
        logger.info("Registry path to create a new key: " + registryPath);
        logger.info("Key name: " + keyName1);
        logger.info("Key value to be set: " + registryValue1);
        logger.info("Status of the key creation: " + status);
        WindowsRegistry winReg = WindowsRegistryFactory.getInstance().getWindowsRegistryImpl();
        try
        {
            winReg.setREG_DWORDValue(WinHKEY.HKEY_LOCAL_MACHINE, registryPath, keyName1, registryValue1);
            Assert.assertTrue(winReg.checkRegKeyExists(WinHKEY.HKEY_LOCAL_MACHINE, registryPath, keyName1));
            Assert.assertEquals(winReg.getREG_DWORDValue(WinHKEY.HKEY_LOCAL_MACHINE, registryPath, keyName1), registryValue1);
        } catch (Exception e)
        {
            logger.error("Unable to create a registry key due to the exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Unable to pass the method due to the exception: "
                        + e.getMessage());
        }
        logger.info("########## createRegistryKey1 End ##########");
    }

    /**
     * This method is used to startEM
     * 
     * @param emhost
     *            //To pass EM host name
     * @param emport
     *            //To pass EM port
     */
    

    /**
     * This method is used to startEM after editing registry and properties
     * 
     * @param emLogFileNameAndPath
     *            //To pass logfile name path of the EM
     * @param messagetoVerify
     *            //To pass message to verify whether EM started or not
     */
    @Test
    @Parameters(value = { "emLogFileNameAndPath", "messagetoVerify" })
    public void startEMAfterEditingProperties(String emLogFileNameAndPath,
                                              String messagetoVerify)
    {
        logger.info("########## startEMAfterEditingProperties Start ##########");
        logger.info("emLogFileNameAndPath is: " + emLogFileNameAndPath);
        logger.info("messagetoVerify is: " + messagetoVerify);
        Process process = null;
        boolean status = false;
        //TODO [sinal04]: what is this log entry for without real checking the status?
        logger.info("EM initial startup status: " + status);
        try
        {
            process = Runtime.getRuntime().exec(emExeLoc);

        } catch (Exception ex)
        {
            logger.error(ex.getMessage());
        } finally
        {
            if (process != null)
            {
                try
                {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    int timer = 0;
                    while (true)
                    {
                        timer++;
                        Util.sleep(30000);
                        status = checkLog(messagetoVerify, emLogFileNameAndPath);
                        if (status) break;
                        if (timer > 20) break;
                    }
                } catch (IOException e)
                {
                    logger.error("Unable to start EM due to the exception: ");
                    logger.error(e.getMessage(), e);
                    Assert.fail("Unable to start EM due to the exception: "
                            + e.getMessage());
                    
                }

            }
        }
        logger.info("EM startup status: " + status);
        logger.info("########## startEMAfterEditingProperties End ##########");
        Assert.assertTrue(status);
    }

    

    /***
     * Method to Kill the INTROSCOPE_ENTERPRISE_MANAGER.exe process.
     * 
     * @param taskKillCmd
     *            --task kill Command value
     */
    @Test
    @Parameters(value = { "taskKillCmd" })
    public void stopNewlyStartedEM(String taskKillCmd)
    {
        boolean status1 = false;
        try
        {
            logger.info("########## stopNewlyStartedEM Start ##########");
            status1 = runCommandToBoolean(taskKillCmd);
            logger.info("Status of running kill EM command: " + status1);
            Assert.assertTrue(status1);
            logger.info("########## stopNewlyStartedEM End ##########");
        } catch (Exception ex)
        {
            logger.error("Unable to stop EM due to the exception: ");
            logger.error(ex.getMessage(), ex);
            Assert.fail("Unable to stop EM due to the exception: "
                    + ex.getMessage());
            
        }
    }

    /**
     * This method is to update the properties in a file
     * 
     * @param propertyKeys1
     *            -to pass the name of the property
     * @param propertyValues1
     *            -to pass the value of the property
     * @param propertyFilePath
     *            -to pass the property file path
     */
    @Test
    @Parameters(value = { "propertyKeys1", "propertyValues1",
            "propertyFilePath" })
    public void updateProperties1(String propertyKeys1,
                                  String propertyValues1,
                                  String propertyFilePath)
    {
        logger.info("########## updateProperties1 Start ##########");
        logger.info("propertyKeys1 are: " + propertyKeys1);
        logger.info("propertyValues1 are: " + propertyValues1);
        logger.info("propertyFilePath is: " + propertyFilePath);
        boolean isPropertyUpdated = false;
        try
        {
            isPropertyUpdated = updateFileProperties(propertyKeys1,
                                                     propertyValues1,
                                                     propertyFilePath);
            logger.info("Status of the property update is: " + isPropertyUpdated);
        } catch (Exception e)
        {
            logger.error("Cannot update the property due to the exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Cannot update the property due to the exception: "
                    + e.getMessage());
            
        }
        logger.info("########## updateProperties1 End ##########");
        Assert.assertTrue(isPropertyUpdated);
    }

	 /**
     * This method is to update the properties in a file
     * 
     * @param propertyKeys
     *            -to pass the name of the property
     * @param propertyValues
     *            -to pass the value of the property
     * @param propertyFilePath
     *            -to pass the property file path
     */
    @Test
    @Parameters(value = { "propertyKeys2", "propertyValues2", "propertyFilePath" })
    public void updateProperties2(String propertyKeys2,
                                 String propertyValues2,
                                 String propertyFilePath)
    {
        logger.info("########## updateProperties2 Start ##########");
        logger.info("propertyKeys are: " + propertyKeys2);
        logger.info("propertyValues are: " + propertyValues2);
        logger.info("propertyFilePath is: " + propertyFilePath);
        boolean isPropertyUpdated = false;
        try
        {
            isPropertyUpdated = updateFileProperties(propertyKeys2,
                                                     propertyValues2,
                                                     propertyFilePath);
            logger.info("Status of the property update is: " + isPropertyUpdated);
        } catch (Exception e)
        {
            logger.error("Cannot update the property due to the exception: ");
            logger.error(e.getMessage(), e);
            Assert.fail("Cannot update the property due to the exception: "
                    + e.getMessage());
        }
        logger.info("########## updateProperties2 End ##########");
        Assert.assertTrue(isPropertyUpdated);
    }

    /**
     * This method is used to run the command and return the true value on
     * success else false
     * 
     * 
     * @param cmd the command to run
     * @return boolean value
     */
    private boolean runCommandToBoolean(String cmd)
    {
        logger.info("################## Start of runCommandToBoolean ##################");
        boolean flag = false;
        try
        {
            logger.info("Running command: " + cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader bri = null;
            bri = new BufferedReader(
                                     new InputStreamReader(process
                                             .getInputStream()));
            String line = null;
            while ((line = bri.readLine()) != null)
            {
                if (line.contains("SUCCESS"))
                {
                    logger.info(line);
                    flag = true;
                    break;
                }
            }

        } catch (Exception e)
        {
            logger.error(e.getMessage());
            return false;
        }
        logger.info("################## Stop of runCommandToBoolean ##################");
        return flag;
    }

    
    @Test(groups = {"rebindportcommunication", "bat"})
	public void verify_ALM_305685_DefaultCommunicationChannel()
	{
    	String registryPath="System\\CurrentControlSet\\Services\\Tcpip\\Parameters\\";
    	String keyName = "TcpTimedWaitDelay";
    	int registryValue = 258;
    	String status="Created";    	
    	String keyName1 = "TCPFinWait2Delay";
    	int registryValue1 = 384;
    	String emProp = "introscope.enterprisemanager.port.default";
    	String command="netstat -an";
    	String processName = "TIME_WAIT";
    	String propertyKeys="introscope.enterprisemanager.serversockets.reuseaddr";
    	String propertyValues="true";
    	String emMessage = ApmbaseConstants.EM_STARTED_STRING;
    	
    	stopEM(emRoleId);
    	harvestWait(60);
    	createRegistryKey(registryPath, keyName, registryValue, status);
    	createRegistryKey(registryPath, keyName1, registryValue1, status);
    	startEM(emRoleId);
    	harvestWait(60);
    	setProcessIdToEMprop(emProp, command, processName, emPropFile);
    	stopEM(emRoleId);
    	harvestWait(60);
    	updateProperties(propertyKeys, propertyValues, emPropFile);
    	startEM(emRoleId);
    	harvestWait(60);
    	Assert.assertTrue(checkLog(emMessage, emLogpath));    	
    	
    	
	}
    
}
