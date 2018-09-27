package com.ca.apm.tests.ControlScriptsTests.test;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.tests.BaseAgentTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.testbed.EMControlScriptsWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/***
 * This class is used to Execute the Windowsservicecommands Testcases
 * 
 * @author talsa04
 * 
 */
public class WindowsServiceWrapperTest extends BaseAgentTest
{

    private String  	emRoleId = EMControlScriptsWindowsTestbed.EM_ROLE_ID;  
	

	private String     emPath              = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR); 
    private String     emBinPath     = emPath + ApmbaseConstants.emBinLoc + "/";
    
    private String    clwJarFileLoc       = emPath + "/lib/CLWorkstation.jar";


    private String     emLaxFile     = ApmbaseConstants.em_install_parent_dir
                                       + ApmbaseConstants.EM_FOLDER_NAME + "/"
                                       + ApmbaseConstants.EM_LAX_FILE;

    private CLWBean    clw           = null;

    private MetricUtil metricUtil    = null;
    
    private String emhost = envProperties
            .getMachineHostnameByRoleId(emRoleId);
    String emuser= ApmbaseConstants.emUser;
    String empassw = ApmbaseConstants.emPassw;
    String emport = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_PORT);

    /**
     * This methos is used to initialize the CLW Bean.
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
    public void initIscopeCLW()
    {
        System.out.println(" ***** CLW object parameters: *** emhost: "
                           + emhost + " emuser: " + emuser + " empassw: "
                           + empassw + "emport:" + emport
                           + " Location CLW Jar file: " + clwJarFileLoc);
        clw = new CLWBean(emhost, emuser, empassw, Integer.parseInt(emport),
                          clwJarFileLoc);
    }
    
    /**
     * This method is used to check whether the files are present in EM bin
     * folder or not.
     * 
     * @param filesName
     *            //To pass mutiple file name.
     */
    @Test
    @Parameters(value = { "filesName" })
    public void emHomeFilesCheck(String filesName)
    {
        boolean isExists = false;
        String[] files = filesName.split("~");
        for (int i = 0; i < files.length; i++)
        {
            File file = new File(emBinPath + files[i]);
            isExists = file.exists();
            System.out.println("EM Bin Folder having file " + files[i]
                               + " exists:" + isExists);
            if (!isExists)
            {
                break;
            }
        }
        Assert.assertTrue(isExists);
    }

    /**
     * This method is used to execute the command and check the metric.
     * 
     * @param startCommand
     *            //To pass Command which is used to execute.
     * @param emMessage
     *            //To pass a message which is used to check whether the command
     *            is executed successfully or not.
     * @param metric
     *            //To pass a metric to check.
     * @param commonmetricExpr
     *            //To pass a common metric
     * @throws Exception
     */
    @Test
    @Parameters(value = { "startCommand", "emMessage", "metric",
            "commonmetricExpr" })
    public void runCommandForMetricCheck(String startCommand,
                                         String emMessage,
                                         String metric,
                                         String commonmetricExpr)
        throws Exception
    {
        System.out.println(emMessage);
        BufferedReader reader = null;
        Process process = null;
        String line = null;
        try
        {
            String[] startCmnd = { startCommand };
            System.out.println("EXECUTING COMMAND:" + startCommand);
            process = ApmbaseUtil.getProcess(startCmnd, emBinPath);
            reader = new BufferedReader(
                                        new InputStreamReader(process
                                                .getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                System.out.println(line);
                if (line.contains(emMessage))
                {
                    Assert.assertTrue(true);
                    System.out.println("***COMMAND EXECUTED SUCESSFULLY***");
                    Util.sleep(5 * 60 * 1000);
                    checkMetricExists(metric, commonmetricExpr);

                    break;

                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (process != null)
            {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.destroy();
            }

        }

    }

    /**
     * This method is used to run status command and checks the status.
     * 
     * @param statusCommand
     *            //To pass a status command
     * @param message
     *            //To pass a message which is used to check the status.
     * @throws Exception
     */
    @Test
    @Parameters(value = { "statusCommand", "message" })
    public void runCommand(String statusCommand, String message)
        throws Exception
    {
        System.out.println(message);
        BufferedReader reader = null;
        Process process = null;
        String line = null;
        try
        {
            String[] startCmnd = { statusCommand };
            System.out.println("EXECUTING COMMAND:" + statusCommand);
            process = ApmbaseUtil.getProcess(startCmnd, emBinPath);
            reader = new BufferedReader(
                                        new InputStreamReader(process
                                                .getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                System.out.println(line);
                if (line.contains(message))
                {
                    Assert.assertTrue(true);
                    System.out.println("***COMMAND EXECUTED SUCESSFULLY***");

                    break;

                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (process != null)
            {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.destroy();
            }
        }
    }

    /**
     * This method is used to run the register commands before starting the EM.
     * 
     * @param registerCommand
     *            //To pass register commands
     * @param messages
     *            //To pass messages to check the commands are executed or not.
     * @throws Exception
     */
    @Test
    @Parameters(value = { "registerCommand", "messages" })
    public void runRegisterCommands(String registerCommand, String messages)
        throws Exception
    {
        BufferedReader reader = null;
        Process process = null;
        String line = null;
        try
        {
            String[] message = messages.split("~");
            String[] startCmnds = registerCommand.split("~");
            for (int i = 0; i < startCmnds.length; i++)
            {
                String[] startCmnd = { startCmnds[i] };
                System.out.println("EXECUTING COMMAND:" + startCmnds[i]);
                process = ApmbaseUtil.getProcess(startCmnd, emBinPath);
                reader = new BufferedReader(
                                            new InputStreamReader(process
                                                    .getInputStream()));
                while ((line = reader.readLine()) != null)
                {
                    System.out.println(line);
                    if (line.contains(message[i]))
                    {
                        Assert.assertTrue(true);
                        System.out
                                .println("***COMMAND EXECUTED SUCESSFULLY***");
                        break;
                    }
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (process != null)
            {
                process.getErrorStream().close();
                process.getInputStream().close();
                process.destroy();
            }

        }
    }

    /**
     * This method is used to check the metric in work station.
     * 
     * @param vAgentmetric
     *            //To pass a metric to check.
     * @return
     */
    private boolean checkMetricExists(String vAgentmetric)
    {
        boolean metricCheck = false;
        try
        {
            System.out
                    .println("******** Agentmetric In checkMetric - metricExists *** "
                             + vAgentmetric);

            metricUtil = new MetricUtil(vAgentmetric, clw);
            int elapsedInterval = 0;
            int chkInterval = 5 * 60 * 1000;
            while (true)
            {
                Thread.sleep(30000);
                elapsedInterval = elapsedInterval + 30000;
                if (metricUtil.metricExists())
                {
                    metricCheck = true;
                    break;
                }
                if (elapsedInterval == chkInterval)
                {
                    break;
                }
            }

        } catch (Exception ex)
        {
            metricCheck = false;
            ex.printStackTrace();
        }
        return metricCheck;
    }

    /**
     * To check the metric information
     * 
     * @param metric
     *            -Metric information
     * @param commonmetricExpr
     *            - Common Metric string
     * @throws Exception
     */

    public void checkMetricExists(String metric, String commonmetricExpr)
        throws Exception
    {
        try
        {
            boolean metricCheck;
            metric = commonmetricExpr + "|" + metric;
            System.out.println("Metric to check:" + metric);
            metricCheck = checkMetricExists(metric);
            System.out.println(metricCheck);
            Assert.assertTrue(metricCheck);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * This method is used to search particular string in EM lax file.
     * 
     * @param argumentXrs
     *            //To pass a string to search.
     * @throws FileNotFoundException
     */
    @Test
    @Parameters(value = { "argumentXrs" })
    public void readStringExistence(String argumentXrs)
        throws FileNotFoundException
    {
        File source = new File(emLaxFile);
        BufferedReader br = new BufferedReader(new FileReader(source));
        try
        {

            String line = "";
            while ((line = br.readLine()) != null)
            {
                if (line.contains(argumentXrs))
                {
                    System.out.println(argumentXrs + "Exists");
                    Assert.fail(argumentXrs + "Exists");
                } else
                {
                    Assert.assertTrue(true);
                    System.out
                            .println(argumentXrs
                                     + " doesn't Exists in the specified file");
                    break;
                }
            }
            br.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This Method is using unregister EM.
     * 
     * @param unRegisterCommand
     * @param unRegisterMessage
     */
    @Test
    @Parameters(value = { "unRegisterCommand", "unRegisterMessage" })
    public void runUnRegisterCommand(String unRegisterCommand,
                                     String unRegisterMessage)
    {
        try
        {
            runRegisterCommands(unRegisterCommand, unRegisterMessage);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


@Test
	@Parameters(value = { "stopCommand", "stopMessage" })
	public void stopServiceEM(String stopCommand, String stopMessage) {
		System.out.println("##########stopServiceEM Start##########");
		System.out.println("stopCommand:" + "" + stopCommand);
		System.out.println("stopMessage:" + "" + stopMessage);
		System.out.println("emBinPath:" + "" + emBinPath);
		 boolean messagesFound = false;
	        try {
	            System.out.println("Executing stop EM command...");
	            messagesFound=executeCommandsTest(stopCommand, stopMessage,
	                                              emBinPath);
	            System.out.println("Status of stop message found is:"+messagesFound);
	            Assert.assertTrue(messagesFound);
	        } catch (Exception e) {
	            System.out.println(e.getMessage());
	        }
		System.out.println("##########stopServiceEM End##########");
	}
@Test
	@Parameters(value = { "stopCommand1", "stopMessage1" })
	public void stopServiceEM1(String stopCommand, String stopMessage) {
		System.out.println("##########stopServiceEM Start##########");
		System.out.println("stopCommand:" + "" + stopCommand);
		System.out.println("stopMessage:" + "" + stopMessage);
		System.out.println("emBinPath:" + "" + emBinPath);
		 boolean messagesFound = false;
	        try {
	            System.out.println("Executing stop EM command...");
	            messagesFound=executeCommandsTest(stopCommand, stopMessage,
	                                              emBinPath);
	            System.out.println("Status of stop message found is:"+messagesFound);
	            Assert.assertTrue(messagesFound);
	        } catch (Exception e) {
	            System.out.println(e.getMessage());
	        }
		System.out.println("##########stopServiceEM End##########");
	}

private boolean executeCommandsTest(String command,
                                        String messages,
                                        String emBinPath)
    {
        System.out.println("##########executeCommandsTest Start##########");
        System.out.println("messages:" + "" + messages);
        System.out.println("command:" + "" + command);
        System.out.println("emBinPath:" + "" + emBinPath);
        BufferedReader reader = null;
        Process process = null;
        String line = null;
        boolean messagesFound = false;
        try
        {

            String[] message = messages.split("~");
            String[] commands = command.split("~");

            System.out.println("EXECUTING COMMAND:" + command);
            process = ApmbaseUtil.getProcess(commands, emBinPath);
            reader = new BufferedReader(
                                        new InputStreamReader(process
                                                .getInputStream()));

            for (int i = 0; i < message.length; i++)
            {

                while ((line = reader.readLine()) != null)
                {
                    if (line.contains(message[i]))
                    {
                        messagesFound = true;
                        System.out.println(line);
                        System.out.println("##########MESSAGE FOUND##########");
                        break;
                    }
                }
                if (messagesFound == false)
                {
                    System.out.println("##########MESSAGE NOT FOUND##########");
                    break;
                }
            }
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        } finally
        {
            if (process != null)
            {
                try
                {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                    process.destroy();
                    System.out.println("process killed sucessfully");
                } catch (IOException e)
                {
                    System.out.println("Unable to kill the processes");
                }

            }

        }
        System.out.println("##########executeCommandsTest End##########");
        return messagesFound;

    }

	
	@Test(groups = {"WindowsServiceWrappers", "bat"})
	public void verify_ALM_280559_ServicePersistence()
	{
		String registerCommand = "EMCtrl64.bat register";
		String registerMessage = "Introscope Enterprise Manager installed";
		String commonmetricExpr ="*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)";
		String metric="Enterprise Manager:Host";
		String startCommand ="EMCtrl64.bat start";
		String emMessage = "Introscope Enterprise Manager started";
		String statusCommand = "EMCtrl64.bat status";
		String statusMessage = "Running: Yes";
		String unRegisterCommand = "EMCtrl64.bat unregister";
		String unRegisterMessage= "Introscope Enterprise Manager removed";
		
		stopEM(emRoleId);
		try {
			runRegisterCommands(registerCommand, registerMessage);
			runCommandForMetricCheck(startCommand, emMessage, metric, commonmetricExpr);
			runCommand(statusCommand, statusMessage);
			runUnRegisterCommand(unRegisterCommand, unRegisterMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		startEM(emRoleId);		
	}



}
