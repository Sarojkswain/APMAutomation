package com.ca.apm.tests.ControlScriptsTests.test;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.AutomationConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.tests.BaseAgentTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;







import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.apache.log4j.*;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.testbed.EMDirWithSpaceWindowsTestbed;
import com.ca.apm.tests.testbed.EMDirWithSpaceWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/***
 * This class is used to Execute the EM Control Script Test case with space for EM Home path
 * which was moved from Coda projects to TAS.
 * @by GAMSA03
 * 
 */
public class ControlScriptsEMHomeWithSpace extends BaseAgentTest {
	private static String resultsDir = System.getProperty("results.dir");

	private static Logger logger = Logger
			.getLogger(ControlScriptsEMHomeWithSpace.class);
	
	private String clwJarFileLoc = System
			.getProperty("testbed_em.install.parent.dir")
			+ AutomationConstants.CLWJARLOCATION;
	private String  	emRoleId = EMDirWithSpaceWindowsTestbed.EM_ROLE_ID;  
	private String emhost = envProperties
            .getMachineHostnameByRoleId(EMDirWithSpaceWindowsTestbed.EM_ROLE_ID);
	private String emuser= ApmbaseConstants.emUser;
	private String empassw = ApmbaseConstants.emPassw;
	private String emport = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_PORT);
	
	private String emhome = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    

	/**
	 * This method is used to initialize the CLW Bean.
	 * 
	 * @param emhost
	 *            //To pass EM host name
	 * @param emuser
	 *            //To pass user name
	 * @param empassw
	 *            //To pass user password
	 * @param emport
	 *            //To pass EM port
	 */
	@BeforeTest(alwaysRun = true)	
	public void initIscopeCLW() {
		logger.info("##########initIscopeCLW Start##########");
		logger.info(" ***** CLW object parameters: *** emhost: " + emhost
				+ " emuser: " + emuser + " empassw: " + empassw + "emport:"
				+ emport + " Location CLW Jar file: " + clwJarFileLoc);

		logger.info("##########initIscopeCLW End##########");
	}

	/**
	 * This method is used to run the given commands before starting the EM.
	 * 
	 * @param command
	 *            //To pass commands to be executed
	 * @param messages
	 *            //Messages to validate the status of the commands executed
	 * @param emBinPath
	 *            //To pass Path of the em bin location where the control scripts are present/available
	 * @throws Exception
	 */
	private boolean executeCommandsTest(String command,
                                        String messages,
                                        String emBinPath)
    {
        logger.info("##########executeCommandsTest Start##########");
        logger.info("messages:" + "" + messages);
        logger.info("command:" + "" + command);
        logger.info("emBinPath:" + "" + emBinPath);
        BufferedReader reader = null;
        Process process = null;
        String line = null;
        boolean messagesFound = false;
        try
        {

            String[] message = messages.split("~");
            String[] commands = command.split("~");

            logger.info("EXECUTING COMMAND:" + command);
            process = ApmbaseUtil.getProcess(commands, emBinPath);
            reader = new BufferedReader(
                                        new InputStreamReader(process
                                                .getInputStream()));

            for (int i = 0; i < message.length; i++)
            {

                while ((line = reader.readLine()) != null)
                {
                	System.out.println(message[i]);
                	
                	if (line.contains(message[i]))
                    {                		
                        messagesFound = true;
                        logger.info(line);
                        logger.info("##########MESSAGE FOUND##########");
                        break;
                    }
                }
                if (messagesFound == false)
                {
                    logger.info("##########MESSAGE NOT FOUND##########");
                    break;
                }
            }
        } catch (IOException e)
        {
            logger.error(e.getMessage());
        } finally
        {
            if (process != null)
            {
                try
                {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                    process.destroy();
                    logger.info("process killed sucessfully");
                } catch (IOException e)
                {
                    logger.error("Unable to kill the processes");
                }

            }

        }
        logger.info("##########executeCommandsTest End##########");
        return messagesFound;

    }

	/**
	 * This method is used to run the help command before starting the EM.
	 * 
	 * @param command
	 *            //String that holds the "help command"
	 * @param messages
	 *            //Messages to validate the status of help command execution
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 * @throws Exception
	 */
	@Test
	@Parameters(value = { "command", "messages", "emBinPath" })
	public void helpCommand(String command, String messages, String emBinPath)
			throws Exception {
		logger.info("##########helpCommand Start##########");
		logger.info("messages:" + "" + messages);
		logger.info("command:" + "" + command);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean messagesFound = false;
        try {
            logger.info("Executing help command...");
            messagesFound=executeCommandsTest(command, messages, emBinPath);
            logger.info("Status of message found is:"+messagesFound);
            Assert.assertTrue(messagesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########helpCommand End##########");
	}

	/**
	 * This Method is using register EM.
	 * 
	 * @param registerCommand
	 *            //Command for registering EM as service
	 * @param registerMessage
	 *            //Message to check after executing EMCtrl.bat register command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "registerCommand", "registerMessage", "emBinPath" })
	public void runRegisterCommand(String registerCommand,
			String registerMessage, String emBinPath) {
		logger.info("##########runRegisterCommand Start##########");
		logger.info("registerCommand:" + "" + registerCommand);
		logger.info("registerMessage:" + "" + registerMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean messagesFound = false;
        try {
            logger.info("Executing register service command");
            messagesFound=executeCommandsTest(registerCommand,
                                              registerMessage, emBinPath);
            logger.info("Status of regsiter message found is:"+messagesFound);
            Assert.assertTrue(messagesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########runRegisterCommand End##########");
	}

	/** jandi02
	 * This Method is using register EM.
	 * 
	 * @param registerCommand
	 *            //Command for registering EM as service
	 * @param registerMessage
	 *            //Message to check after executing EMCtrl.bat register command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "registerCommand", "registerMessage", "emBinPath" })
	public void runRegisterCommand_1(String registerCommand,
			String registerMessage, String emBinPath) {
		logger.info("##########runRegisterCommand_1 Start##########");
		logger.info("registerCommand:" + "" + registerCommand);
		logger.info("registerMessage:" + "" + registerMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean messagesFound = false;
        try {
            logger.info("Executing register service command");
            messagesFound=executeCommandsTest(registerCommand,
                                              registerMessage, emBinPath);
            logger.info("Status of regsiter message found is:"+messagesFound);
            Assert.assertTrue(messagesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########runRegisterCommand_1 End##########");
	}
	
	/**
	 * This Method is using unregister EM.
	 * 
	 * @param unRegisterCommand
	 *            //Command for unregistering EM as service
	 * @param unRegisterMessage
	 *            //Message to validate the execution of unregister command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "unRegisterCommand", "unRegisterMessage", "emBinPath" })
	public void runUnRegisterCommand(String unRegisterCommand,
			String unRegisterMessage, String emBinPath) {
		logger.info("##########runUnRegisterCommand Start##########");
		logger.info("unRegisterCommand:" + "" + unRegisterCommand);
		logger.info("unRegisterMessage:" + "" + unRegisterMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		 boolean messagesFound = false;
	        try {
	            logger.info("Executing unregister command");
	            messagesFound=executeCommandsTest(unRegisterCommand,
	                                              unRegisterMessage, emBinPath);
	            logger.info("Status of unregister message found is:"+messagesFound);
	            Assert.assertTrue(messagesFound);
	        } catch (Exception e) {
	            logger.error(e.getMessage());
	        }
		logger.info("##########runUnRegisterCommand End##########");
	}
	
	
	/** jandi02
	 * This Method is using unregister EM.
	 * 
	 * @param unRegisterCommand
	 *            //Command for unregistering EM as service
	 * @param unRegisterMessage
	 *            //Message to validate the execution of unregister command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "unRegisterCommand", "unRegisterMessage", "emBinPath" })
	public void runUnRegisterCommand_1(String unRegisterCommand,
			String unRegisterMessage, String emBinPath) {
		logger.info("##########runUnRegisterCommand_1 Start##########");
		logger.info("unRegisterCommand:" + "" + unRegisterCommand);
		logger.info("unRegisterMessage:" + "" + unRegisterMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		 boolean messagesFound = false;
	        try {
	            logger.info("Executing unregister command");
	            messagesFound=executeCommandsTest(unRegisterCommand,
	                                              unRegisterMessage, emBinPath);
	            logger.info("Status of unregister message found is:"+messagesFound);
	            Assert.assertTrue(messagesFound);
	        } catch (Exception e) {
	            logger.error(e.getMessage());
	        }
		logger.info("##########runUnRegisterCommand_1 End##########");
	}	

	
	/**
	 * This Method is used to start EM as service.
	 * 
	 * @param startCommand
	 *            //Command for starting EM as service
	 * @param startMessage
	 *            //Message to validate the execution of start command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "startCommand", "startMessage", "emBinPath" })
	public void startServiceEM(String startCommand, String startMessage,
			String emBinPath) {
		logger.info("##########startServiceEM Start##########");
		logger.info("startCommand:" + "" + startCommand);
		logger.info("startMessage:" + "" + startMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean messagesFound = false;
        try {
            logger.info("Executing start command...");
            messagesFound=executeCommandsTest(startCommand, startMessage,
                                              emBinPath);
            logger.info("Status of start message found is:"+messagesFound);
            Assert.assertTrue(messagesFound);
			Util.sleep(5*60000);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########startServiceEM End##########");
	}

	/**
	 * This Method is used to start EM as service.
	 * 
	 * @param startCommand1
	 *            //Command for starting EM as service
	 * @param startMessage1
	 *            //Message to valid the execution of start command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "startCommand1", "startMessage1", "emBinPath" })
	public void startServiceEM1(String startCommand1, String startMessage1,
			String emBinPath) {
		logger.info("##########startServiceEM1 Start##########");
		logger.info("startCommand1:" + "" + startCommand1);
		logger.info("startMessage1:" + "" + startMessage1);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean messagesFound = false;
        try {
            logger.info("Executing start EM script...");
            messagesFound=executeCommandsTest(startCommand1, startMessage1,
                                              emBinPath);
            logger.info("Status of start message1 found is:"+messagesFound);
            Assert.assertTrue(messagesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########startServiceEM1 End##########");
	}

	/**
	 * This Method is used to stop EM as service.
	 * 
	 * @param stopCommand
	 *            //Command for stoping EM as service
	 * @param stopMessage
	 *            //Message to validate the execution of stop EM command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "stopCommand", "stopMessage", "emBinPath" })
	public void stopServiceEM(String stopCommand, String stopMessage,
			String emBinPath) {
		logger.info("##########stopServiceEM Start##########");
		logger.info("stopCommand:" + "" + stopCommand);
		logger.info("stopMessage:" + "" + stopMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		 boolean messagesFound = false;
	        try {
	            logger.info("Executing stop EM command...");
	            messagesFound=executeCommandsTest(stopCommand, stopMessage,
	                                              emBinPath);
	            logger.info("Status of stop message found is:"+messagesFound);
	            Assert.assertTrue(messagesFound);
	        } catch (Exception e) {
	            logger.error(e.getMessage());
	        }
		logger.info("##########stopServiceEM End##########");
	}

	/**
	 * This Method is used to check the status.
	 * 
	 * @param statusCommand
	 *            //Command for checking status of EM
	 * @param statusMessage
	 *            //Message to validate the execution of status command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "statusCommand", "statusMessage", "emBinPath" })
	public void checkStatus(String statusCommand, String statusMessage,
			String emBinPath) {
		logger.info("##########checkStatus Start##########");
		logger.info("statusCommand:" + "" + statusCommand);
		logger.info("statusMessage:" + "" + statusMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean messagesFound = false;
        try {
            logger.info("Checking introscope enterprise manager status");
            messagesFound=executeCommandsTest(statusCommand, statusMessage,
                                              emBinPath);
            logger.info("Status of introscope enterprise manager is:"+messagesFound);
            Assert.assertTrue(messagesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########checkStatus End##########");
	}

	/**
	 * Test method to search for a message in a file.
	 * 
	 * @param emlogLocation
	 *            // Passing the location of EM log
	 * @param message
	 *            // Message/Messages to verify in the EM log file
	 * 
	 */
	private int checkLog(String emLogLocation, String message) {
		/**
		 * status=1 - Indicates the Log present; status=0 - Indicates the Log
		 * Not present.
		 */
		logger.info("##########logCheck Start##########");
		logger.info("emLogLocation:" + "" + emLogLocation);
		logger.info("message:" + "" + message);
		File file = new File(emLogLocation);
		String[] messages = message.split("~");
		int status = 0;
		for (int i = 0; i < messages.length; i++) {
			try {
				status = Util.checkMessage(messages[i], file);
				logger.info("Status of the logCheck is:" + status);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		logger.info("##########logCheck End##########");
		return status;

	}

	/**
	 * Test method to search for message in a introscope enterprise manager log
	 * file.
	 * 
	 * @param emlogLocation
	 *            // Passing the location of EM log
	 * @param message
	 *            // Message/Messages to verify in the EM log file
	 * 
	 */
	@Test
	@Parameters(value = { "emLogLocation", "message" })
	public void logCheck(String emLogLocation, String message)

	{
		/**
		 * status=1 - Indicates the Log present; status=0 - Indicates the Log
		 * Not present.
		 */
		logger.info("##########logCheck Start##########");
		logger.info("emLogLocation:" + "" + emLogLocation);
		logger.info("message:" + "" + message);
		logger.info("starting log check..");
		int status = checkLog(emLogLocation, message);
		logger.info("status of the logcheck is:" + status);
		Assert.assertEquals(status, 0);
		logger.info("##########logCheck End##########");
	}

	/**
	 * Test method to search for error message in a introscope enterprise
	 * manager log file.
	 * 
	 * @param errorEmLogLocation
	 *            // Passing the location of EM log
	 * @param errorMessage
	 *            // Error message/messages to verify in the EM log file
	 * 
	 */
	@Test
	@Parameters(value = { "errorEmLogLocation", "errorMessage" })
	public void errorLogCheck(String errorEmLogLocation, String errorMessage)

	{
		/**
		 * status=1 - Indicates the Log present status=0 - Indicates the Log Not
		 * present.
		 */
		logger.info("##########errorLogCheck Start##########");
		logger.info("errorEmLogLocation:" + "" + errorEmLogLocation);
		logger.info("errorMessage:" + "" + errorMessage);
		logger.info("Executing error logcheck....");
		int status = checkLog(errorEmLogLocation, errorMessage);
		logger.info("status of the errormessage is:" + status);
		Assert.assertEquals(status, 1);
		logger.info("##########errorLogCheck End##########");
	}

	/**
	 * Test method to search for message in a EMService.log file.
	 * 
	 * @param serviceLogLocation
	 *            // Passing the location of EMService.log file
	 * @param serviceMessage
	 *            // service message/messages to verify in EMService.log file
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "serviceLogLocation", "serviceMessage" })
	public void serviceLogCheck(String serviceLogLocation, String serviceMessage)

	{
		/**
		 * status=1 - Indicates the Log present status=0 - Indicates the Log Not
		 * present.
		 */
		logger.info("##########serviceLogCheck Start##########");
		logger.info("serviceLogLocation:" + "" + serviceLogLocation);
		logger.info("serviceMessage:" + "" + serviceMessage);
		logger.info("Executing serviceLog check...");
		int status = checkLog(serviceLogLocation, serviceMessage);
		logger.info("status of the serviceLogCheck is:" + status);
		Assert.assertEquals(status, 0);
		logger.info("##########serviceLogCheck End##########");
	}

	/**
	 * Test method to search for error message in a EMService.log file.
	 * 
	 * @param errorServiceLogLocation
	 *            // Passing the location of EMService.log file
	 * @param errorServiceMessage
	 *            // Error service message/messages to verify in EMService.log file
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "errorServiceLogLocation", "errorServiceMessage" })
	public void errorServiceLogCheck(String errorServiceLogLocation,
			String errorServiceMessage)

	{
		/**
		 * status=1 - Indicates the Log present status=0 - Indicates the Log Not
		 * present.
		 */
		logger.info("##########errorServiceLogCheck Start##########");
		logger.info("errorServiceLogLocation:" + "" + errorServiceLogLocation);
		logger.info("errorServiceMessage:" + "" + errorServiceMessage);
		int status = 0;
		int i = 0;
		while (true) {
			Util.sleep(60000);
			logger.info("checking error log message");
			i++;
			status = checkLog(errorServiceLogLocation, errorServiceMessage);
			if (status > 0) {
				logger.info("error message found in EMService.log file");
				break;
			}
			if (i > 5) {//Polling for five times (each time to sleep one minute) for checking the EMService.log file
				break;
			}
		}
		logger.info("status of the error message in EM.Servicelog is:" + status);
		
		Assert.assertEquals(status, 1);
		logger.info("##########errorServiceLogCheck End##########");
	}

	/**
	 * Method to rename the file/folder name.
	 * 
	 * @param oldFileName
	 *            // Name of the file before changing
	 * @param newFileName
	 *            // Name of the file after renaming
	 * 
	 */
	private boolean filerename(String oldFileName, String newFileName) {
		logger.info("##########filerename Start##########");
		logger.info("oldFileName:" + "" + oldFileName);
		logger.info("newFileName:" + "" + newFileName);
		File file = new File(oldFileName);
		File newFile = new File(newFileName);
		boolean renameStatus = false;
		try {
			renameStatus = file.renameTo(newFile);
			logger.info("renameStatus :" + renameStatus);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		logger.info("##########renameFolderName End##########");
		return renameStatus;

	}

	

	/**
	 * This Method is used to check the Windows services.
	 * 
	 * @param serviceCommand
	 *            //Command for checking services in Windows
	 * @param serviceMessage
	 *            //Status message of service to be checked
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "serviceCommand", "serviceMessage", "emBinPath" })
	public void checkServices(String serviceCommand, String serviceMessage,
			String emBinPath) {
		logger.info("##########checkServices Start##########");
		logger.info("serviceCommand:" + "" + serviceCommand);
		logger.info("serviceMessage:" + "" + serviceMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean servicesFound = false;
        try {
            logger.info("Executing service check command..");
            servicesFound = executeCommandsTest(serviceCommand, serviceMessage,
                                                emBinPath);
            logger.info("IscopeEm Message found in the services is:"+servicesFound);
            Assert.assertTrue(servicesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########checkServices End##########");
	}

	/**
	 * This Method is used to check the status in Windows services.
	 * 
	 * @param serviceStatusCommand
	 *            //Command for checking status of services in Windows
	 * @param serviceStatusMessage
	 *            //Message to check after executing status command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "serviceStatusCommand", "serviceStatusMessage",
			"emBinPath" })
	public void checkServicesStatus(String serviceStatusCommand,
			String serviceStatusMessage, String emBinPath) {
		logger.info("##########checkServicesStatus Start##########");
		logger.info("serviceStatusCommand:" + "" + serviceStatusCommand);
		logger.info("serviceStatusMessage:" + "" + serviceStatusMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean servicesFound = false;
        try {
        	logger.info("Executing status command...");
            servicesFound = executeCommandsTest(serviceStatusCommand,
					serviceStatusMessage, emBinPath);
            logger.info("Status of the IscopeEm in the services is:"+servicesFound);
            Assert.assertTrue(servicesFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########checkServicesStatus End##########");
	}

	/**
	 * This Method is used to run the watchDog and check the message.
	 * 
	 * @param watchDogCommand
	 *            //Command for running the watchdog
	 * @param watchDogMessage
	 *            //Message to check after executing watchdog command
	 * @param emBinPath
	 *            //To pass the path of EM bin location where the control scripts are present/?available
	 */
	@Test
	@Parameters(value = { "watchDogCommand", "watchDogMessage", "emBinPath" })
	public void runWatchDog(String watchDogCommand, String watchDogMessage,
			String emBinPath) {
		logger.info("##########runWatchDog Start##########");
		logger.info("watchDogCommand:" + "" + watchDogCommand);
		logger.info("watchDogMessage:" + "" + watchDogMessage);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean watchDogMessageFound = false;
        try {
            logger.info("Executing Run watchdog command...");
            watchDogMessageFound=executeCommandsTest(watchDogCommand,
                                              watchDogMessage, emBinPath);
            logger.info("Run WatchDog message found is:"+watchDogMessageFound);
            Assert.assertTrue(watchDogMessageFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########runWatchDog End##########");
	}

	@Test
	@Parameters(value = { "watchDogCommand1", "watchDogMessage1", "emBinPath" })
	public void runWatchDog1(String watchDogCommand1, String watchDogMessage1,
			String emBinPath) {
		logger.info("##########runWatchDog Start##########");
		logger.info("watchDogCommand1:" + "" + watchDogCommand1);
		logger.info("watchDogMessage:" + "" + watchDogMessage1);
		logger.info("emBinPath:" + "" + emBinPath);
		boolean watchDogMessageFound = false;
        try {
            logger.info("Executing Run watchdog command...");
            watchDogMessageFound=executeCommandsTest(watchDogCommand1,
                                              watchDogMessage1, emBinPath);
            logger.info("Run WatchDog message found is:"+watchDogMessageFound);
            Assert.assertTrue(watchDogMessageFound);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
		logger.info("##########runWatchDog End##########");
	}

	
	/**
	 * This Method is used to stop the watchDog and kill processes running
	 * background of EM folder
	 * 
	 * @param serviceWrapper32
	 *            //passing serviceWrapper of 32-bit machine
	 * @param serviceWrapper64
	 *            //passing serviceWrapper of 64-bit machine
	 * @param EM
	 *            //passing the introscope enterprise manager process
	 * 
	 */
	@Test
	@Parameters(value = { "serviceWrapper32", "serviceWrapper64", "EM" })
	public void stopWatchDog(String serviceWrapper32, String serviceWrapper64,
			String EM) {
		logger.info("##########stopWatchDog Start##########");
		logger.info("serviceWrapper32:" + "" + serviceWrapper32);
		logger.info("serviceWrapper64:" + "" + serviceWrapper64);
		logger.info("introscope enterprise manager:" + "" + EM);
		try {
			String line;
			String cmds[] = { "cmd", "/c", "wmic" };
			String otherCmds[] = { "cmd", "/c", "Path win32_process where \"CommandLine Like '%WatchDog.jar%'\" Call Terminate" };
			String killCmd = null;
			// Closing Java process
			Process p = Runtime.getRuntime().exec(cmds);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.info(line);
				if ((line.contains("LAX"))||(line.contains("Introscope"))||(line.contains("WatchDog"))) {
					if (line.substring(line.indexOf(" ") + 1, line.length())
							.equalsIgnoreCase("jar") || line.contains("LAX")) {
						killCmd = line.substring(0, line.indexOf(" "));
						logger.info("killCmd" + killCmd);
						Runtime.getRuntime()
								.exec("taskkill /F /PID " + killCmd);
					}

				}
			}

			// Closing ServiceWrapper process
			p = Runtime.getRuntime().exec(otherCmds);
			input = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				if (line.contains(serviceWrapper32)
						|| line.contains(serviceWrapper64) || line.contains(EM)) {
					killCmd = line.substring(0, line.indexOf(" "));
					logger.info("ServiceWrapper " + killCmd);
					Runtime.getRuntime().exec("taskkill /F /PID " + killCmd);
				}
			}
			Util.sleep(120000);
			input.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info("##########stopWatchDog End##########");
	}

	/**
	 * Test method to search for message in a watchDog log file.
	 * 
	 * @param watchDogLogLocation
	 *            // Passes the location of the watchdog log
	 * @param watchDogLogMessage
	 *            // Message to check in watchdog log
	 * 
	 */
	@Test
	@Parameters(value = { "watchDogLogLocation", "watchDogLogMessage" })
	public void watchDogLogCheck(String watchDogLogLocation,
			String watchDogLogMessage)

	{
		/**
		 * status=1 - Indicates the Log present status=0 - Indicates the Log Not
		 * present.
		 */
		logger.info("##########watchDogLogCheck Start##########");
		logger.info("watchDogLogLocation:" + "" + watchDogLogLocation);
		logger.info("watchDogLogMessage:" + "" + watchDogLogMessage);
		logger.info("Checking watchDogLogs...");
		int status = checkLog(watchDogLogLocation, watchDogLogMessage);
		logger.info("WatchDogLogMessage found in the watchDogLog is:" + status);
		Assert.assertEquals(status, 1);
		logger.info("##########watchDogLogCheck End##########");
	}

	/**
	 * Test method to rename the license file.
	 * 
	 * @param locationOfLicenseFile
	 *            // Passes the location and name of the License file
	 * @param backupFileNameForLicenseFile
	 *            // Passes new name and location to rename the License file
	 * 
	 */
	@Test
	@Parameters(value = { "locationOfLicenseFile",
			"backupFileNameForLicenseFile" })
	public void renameLicenseFile(String locationOfLicenseFile,
			String backupFileNameForLicenseFile)

	{
		logger.info("##########renameLicenseFile Start##########");
		logger.info("locationOfLicenseFile:" + "" + locationOfLicenseFile);
		logger.info("backupFileNameForLicenseFile:" + ""
				+ backupFileNameForLicenseFile);
		boolean isRenamed=false;
        try
        {
        	logger.info("Renaming EM license file...");
            isRenamed=filerename(locationOfLicenseFile,
					backupFileNameForLicenseFile);
            logger.info("License file renamed status:" + isRenamed);
            Assert.assertTrue(isRenamed);
        } catch (Exception e)
        {
            logger.error(e.getMessage());
        }
		logger.info("##########renameLicenseFile End##########");

	}

	/**
	 * Test method to revert the license file.
	 * 
	 * @param backupFileNameForLicenseFile
	 *            // Passes the oldname and location of the file
	 * @param locationOfLicenseFile
	 *            // Passes new name and location to revert filename
	 * 
	 */
	@Test
	@Parameters(value = { "backupFileNameForLicenseFile",
			"locationOfLicenseFile" })
	public void revertLicenseFile(String backupFileNameForLicenseFile,
			String locationOfLicenseFile)

	{
		logger.info("##########revertLicenseFile Start##########");
		logger.info("backupFileNameForLicenseFile:" + ""
				+ backupFileNameForLicenseFile);
		logger.info("locationOfLicenseFile:" + "" + locationOfLicenseFile);
		boolean isRenamed=false;
        try
        {
        	logger.info("File is renaming...");
            isRenamed=filerename(backupFileNameForLicenseFile, locationOfLicenseFile);
            logger.info("status of reverting the license file is:" + isRenamed);
            Assert.assertTrue(isRenamed);
        } catch (Exception e)
        {
            logger.error(e.getMessage());
        }
		logger.info("##########revertLicenseFile End##########");

	}

	/**
	 * Test method is used before starting of every testcase.
	 * 
	 * @param testCaseNameIDPath
	 *            // Passing perforce path and Testcase ID
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "testCaseNameIDPath" })
	public void testCaseStart(String testCaseNameIDPath) {
		logger.info("----------" + testCaseNameIDPath + "-----" + "Started");
	}

	/**
	 * Test method is used at the end of every testcase.
	 * 
	 * @param duration
	 *            // Passing perforce path and Testcase ID
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "testCaseNameIDPath" })
	public void testCaseEnd(String testCaseNameIDPath) {
		logger.info("----------" + testCaseNameIDPath + "-----" + "Ended");
	}

	/**
	 * Test method to sleep.
	 * 
	 * @param duration
	 *            // Duration of the time to sleep in milliseconds
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "duration" })
	public void sleep(long duration) {
		logger.info("########## sleep Start ##########");
		logger.info("duration:" + "" + duration);
		Util.sleep(duration);
		logger.info("########## sleep End ##########");

	}
	/**
	 * Test method to sleep.
	 * 
	 * @param duration
	 *            // Duration of the time to sleep in milliseconds
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "duration" })
	public void sleep1(long duration) {
		logger.info("########## sleep Start ##########");
		logger.info("duration:" + "" + duration);
		Util.sleep(duration);
		logger.info("########## sleep End ##########");

	}
	
	
	/** jandi02
	 * Test method to sleep.
	 * 
	 * @param duration
	 *            // Duration of the time to sleep in milliseconds
	 * 
	 * 
	 */
	@Test
	@Parameters(value = { "duration" })
	public void sleep1_1(long duration) {
		logger.info("########## sleep Start ##########");
		logger.info("duration:" + "" + duration);
		Util.sleep(duration);
		logger.info("########## sleep End ##########");

	}
	
	@Tas(testBeds = @TestBed(name = EMDirWithSpaceWindowsTestbed.class, executeOn = EMDirWithSpaceWindowsTestbed.MACHINE_ID), size = SizeType.COLOSSAL, owner = "gamsa03")
	@Test(groups = {"controlscripts", "bat"})
	public void verify_ALM_280481_ControlScript_WithSpace_EM_InstallHome()
	{
		long duration =120000;		
    	String testCaseNameIDPath ="EM\\Control Scripts\\006 - Start the EM using a Control Script - Space in EM Install";
    	String emBinPath =emhome + "/bin/";
    	String unRegisterCommand ="EMCtrl64.bat unregister";
    	String registerCommand ="EMCtrl64.bat register";
		String registerMessage ="Introscope Enterprise Manager service installed";
    	String unRegisterMessage ="Introscope Enterprise Manager service removed";
    	String serviceCommand ="sc getdisplayname iscopeem";
		String serviceMessage ="Introscope Enterprise Manager";
		String startCommand ="EMCtrl64.bat start";
		String startMessage ="Introscope Enterprise Manager started";
		String serviceStatusCommand ="sc query IScopeEM";
		String serviceStatusMessage ="RUNNING";
		String watchDogCommand ="java -jar " + '"' + emhome + "/bin/Watchdog.jar" + '"' + " start -startcmd " + '"' + emhome + 
						"/Introscope_Enterprise_Manager.exe" + '"';
		String watchDogMessage ="alreadyrunning & wdalreadysleeping";
		String watchDogLogLocation =emhome + "/logs/WatchDog.log";
		String watchDogLogMessage ="returning:  alreadyrunning & wdalreadysleeping";
		String watchDogCommand1 ="java -jar " + '"' + emhome + "/bin/Watchdog.jar" + '"' + " stop";
		String watchDogMessage1 ="stopcommandissued & wdstopcommandissued";
		
		
		
		testCaseStart(testCaseNameIDPath);
		runRegisterCommand(registerCommand, registerMessage, emBinPath);		
		checkServices(serviceCommand, serviceMessage, emBinPath);
		
		startServiceEM(startCommand, startMessage, emBinPath);
		sleep(duration);
		
		checkServicesStatus(serviceStatusCommand, serviceStatusMessage, emBinPath);
		sleep(duration);
		
		runWatchDog(watchDogCommand, watchDogMessage, emBinPath);
		
		watchDogLogCheck(watchDogLogLocation, watchDogLogMessage);
		
		runWatchDog1(watchDogCommand1, watchDogMessage1, emBinPath);
		testCaseEnd(testCaseNameIDPath);
		runUnRegisterCommand_1(unRegisterCommand, unRegisterMessage, emBinPath);
	}
	
}
