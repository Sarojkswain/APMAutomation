package com.ca.apm.tests.test.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.apache.log4j.Logger;

public class Utils {
	private static Logger logger = Logger
			.getLogger(Utils.class);


	/**
	 * Test method to add properties to the Agent profile file
	 * 
	 * @param property
	 *            -holds property which needs to be added in the Agent profile
	 *            file
	 * @param propFileName
	 *            - holds the name of the file in which property needs to be
	 *            added
	 */
	@Test
	@Parameters(value = { "property", "propFileName" })
	public void addProperty(String property,File propFileName) {
		logger.info("Start of addProperty method");
		logger.info("addPropToProfile uses property with value :" + property);
		String fileName = propFileName.toString();
		logger.info("Appending properties");
		boolean status = appendLines(property, fileName);
		if (!status)
			logger.info("Unable to append lines");
		else
			logger.info("Lines appended successfully");
		logger.info("End of addProperty method");
		Assert.assertTrue(status);
	}

	
	/**
	 * Helper method to add required lines in the required file
	 * 
	 * @param fileContent
	 *            -holds content which needs to be added in the profile file
	 * @param fileName
	 *            -holds path of the file in which the content needs to be added
	 * @return -returs boolean value [true/false] whether the content is added
	 *         successfully or not
	 */
	private boolean appendLines(String fileContent, String fileName) {
		boolean isWritten = false;
		logger.info("inside appendContentToFile [begin]");
		logger.info("appending the fileContent to " + fileName);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(fileName, true));
			String messages[] = fileContent.split("~");
			logger.info("messages_length " + messages.length);
			for (int j = 0; j < messages.length; j++) {
				logger.info("--->>" + messages[j]);
				out.newLine();
				out.write(messages[j]);
				out.newLine();
			}
			out.flush();
			logger.info("appending COMPLETED ");
			isWritten = true;
		} catch (Exception ex) {

			logger.error("Unable to add the content to the file due to :"
					+ ex.getMessage());
			Assert.fail("Unable to add the content to the file due to :" + ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {

					logger.error("Unable to add the content to the file due to :"
							+ e.getMessage());
					Assert.fail("Unable to add the content to the file due to :"
							+ e);
				}
			}
		}
		logger.info("inside appendContentToFile [end]");
		return isWritten;
	}	
	
	
	
		
	/*
	
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
    

  
    @Test
    @Parameters(value = { "oldTrace", "newTrace" })
    public void transUpdateIEMProp(String oldTrace, String newTrace)
    {
        Util.replaceLine(emPropFile, oldTrace, newTrace);
    }
    
   

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

	@Test(enabled = true)
	public void backupPropertiesFile() {
		message = ApmbaseUtil.fileBackUp(introscopePropertiesFilePath);
		Assert.assertEquals(message, msg);
	}
	

	@Test(enabled = true)
	public void revertPropertiesFile() {
		message = ApmbaseUtil.revertFile(introscopePropertiesFilePath);
		Assert.assertEquals(message, msg);
	}

	@Test(enabled = true)
	@Parameters(value = { "keys", "values" })
	public void updateIntroscopeProperties(String keys, String values) {

		boolean value = ApmbaseUtil.updateProperties(keys, values,
				introscopePropertiesFilePath);
		Assert.assertEquals(value, true);

	}



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
				}}
	

	*/
	
	public void end(){
		
	}
	
}
