package com.ca.apm.tests.propertyconfiguration;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.coda.common.ApmbaseUtil;


import com.ca.apm.commons.tests.BaseAgentTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.testbed.EMPropertyConfigurationtWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * # ~ Copyright (c) 2012. Wily Technology, Inc. All rights reserved.
 * 
 * This class is used to Execute the EM configuration and LAX properties  Test cases
 * which was moved from Coda projects to TAS.
 * @by GAMSA03
 * 
 */

public class EMPropertiesEMLaxTest extends BaseAgentTest{

	private static String emRoleId = EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID;   
	
	private String emPath  = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_INSTALL_DIR);

	private String emhost = envProperties
            .getMachineHostnameByRoleId(EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID);
	private String emuser= ApmbaseConstants.emUser;
	private String empassw = ApmbaseConstants.emPassw;
	private String emport = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_PORT);
    
	private static Logger logger = Logger
			.getLogger(EMPropertiesEMLaxTest.class);
	
	private String              emExeLoc              = ApmbaseConstants.em_install_parent_dir
                                                        + ApmbaseConstants.EM_FOLDER_NAME
                                                        + "/"
                                                        + ApmbaseConstants.EM_EXE;

	private String successMessage = ApmbaseConstants.SUCCESS_MESSAGE;

	private final String IntroscopePropertiesFileName = ApmbaseConstants.EM_PROPERTIES_FILE;

	private final String IntroscopePropertiesFilePath = emPath
			+ ApmbaseConstants.EM_PROP_FILE_LOC + "/";

	private final String propertiesFilePath = IntroscopePropertiesFilePath
			+ IntroscopePropertiesFileName;

	public String emHomeDir = System
			.getProperty("testbed_client.install.parent.dir");

	private String clwJarFileLoc = emPath + "/lib/CLWorkstation.jar";

	private String usedPort = null;

	// Carries the clw Bean
	private CLWBean clw = null;

	private int sleep;

	private int minuteInMillis = 60000;

	private int MBinKBs = 1024;

	/** Database driver Information */
	private static String dbdriverinfo = System
			.getProperty("role_client.dbdriverinfo");

	/** Database Connection String */
	private static String dbconString = System
			.getProperty("role_client.dbconString");

	/** Database User ID */
	private static String dbsystemuser = System
			.getProperty("role_client.dbsystemuser");

	/** Database Password */
	private static String dbsystempwd = System
			.getProperty("role_client.dbsystempwd");

	/** Database User ID */
	private static String dbuser = System.getProperty("role_client.dbuser");

	/** Database Password */
	private static String dbpwd = System.getProperty("role_client.dbpwd");

	/** Oracle Delete statements */
	private static String deleteQueries = System
			.getProperty("role_client.deleteQueries");

	/**
	 * Global setup and creating CLW instance.
	 * 
	 * @param emhost
	 *            - EM host - machine name of the EM from testbed
	 * @param emuser
	 *            - EM user - username for EM to login with
	 * @param empassw
	 *            - EM password - Password for the user above to login to EM
	 * @param emport
	 *            - EM port - EM port no..on which EM is running
	 */
	@BeforeClass
	public void initIscopeCLW() {
		logger.info("Start of initIscopeCLW");
		logger.info("initIscopeCLW uses emhost with value :" + emhost);
		logger.info("initIscopeCLW uses emuser with value :" + emuser);
		logger.info("initIscopeCLW uses empassw with value :" + empassw);
		logger.info("initIscopeCLW uses emport with value :" + emport);
		logger.info(" * CLW object parameters:  emhost: " + emhost
				+ " emuser: " + emuser + " empassw: " + empassw + "emport:"
				+ emport + " Location CLW Jar file: " + clwJarFileLoc);

		clw = new CLWBean(emhost, emuser, empassw, Integer.parseInt(emport),
				clwJarFileLoc);
		logger.info("End of initIscopeCLW");
	}// end method

	/**
	 * Private method to create backup of any file given by filePath. The method
	 * will add "bak" to the filename,returns true if its successfull.
	 * 
	 * @param filePath
	 *            -holds path of the file which needs a backup
	 * 
	 * @return String -returns fail or success message depending on fileBackUp
	 *         is successful or not
	 */
	private String fileBackUp(String filePath) {
		logger.info("Start of fileBackUp");
		logger.info("fileBackUp uses filePath with value :" + filePath);
		logger.info("Taking backup for the files required");
		String message = "Failed";
		try {
			String bakFileName = filePath.substring(0,
					filePath.lastIndexOf("."))
					+ "bak" + filePath.substring(filePath.lastIndexOf("."));
			logger.info("Backup FileName:" + bakFileName);
			logger.info("Creating Backup");
			File file = new File(filePath);
			File bakFile = new File(bakFileName);
			FileUtils.copy(file, bakFile);
			Util.sleep(10000);
			if (bakFile.exists()) {
				message = successMessage;
				logger.info("Backup Created.");
			}
		} catch (Exception e) {
			logger.info("Method failed due to :" + e.getMessage());
		}
		logger.info("End of fileBackUp");
		return message;
	}

	/**
	 * Private Method to delete the given file and rename the "bak" file back to
	 * original
	 * 
	 * @param filePath
	 *            -holds the path of the file which needs to be reverted * @return
	 *            String -returns fail or success message depending on
	 *            revertFiles is successful or not
	 */
	private String revertFiles(String filePath) {
		logger.info("Start of revertFile");
		logger.info("revertFile uses filePath with value :" + filePath);
		logger.info("Reverting the name of the backup file to the original file");
		String message = "Failed";
		try {
			logger.info("Reverting file");
			File file = new File(filePath);
			file.delete();
			String bakFileName = filePath.substring(0,
					filePath.lastIndexOf("."))
					+ "bak" + filePath.substring(filePath.lastIndexOf("."));
			File bakFile = new File(bakFileName);
			file = new File(filePath);
			bakFile.renameTo(file);
			Util.sleep(10000);
			if (file.exists()) {
				message = successMessage;
				logger.info("Backup Renamed");
			}
		} catch (Exception e) {
			logger.info("Method failed due to :" + e.getMessage());
		}
		logger.info("End of revertFile");
		return message;
	}

	/**
	 * Test Method to rename a file
	 * 
	 * @param actualFile
	 *            -holds the actual filename.
	 * @param destFile
	 *            -holds the new file name
	 */
	@Test
	@Parameters(value = { "actualFile", "destFile" })
	public void renameFile(String actualFile, String destFile) {
		logger.info("Start of renameFile");
		logger.info("Renaming the file :" + actualFile + " to:" + destFile);
		boolean isRenamed = false;
		try {
			logger.info("Reverting file");
			File newFile = new File(destFile);
			File actFile = new File(actualFile);
			actFile.renameTo(newFile);
			Util.sleep(10000);
			if (newFile.exists()) {
				isRenamed = true;
				logger.info("Actual file is renamed");
			}
		} catch (Exception e) {
			Assert.fail("Method failed due to :" + e);
			logger.info("Method failed due to :" + e.getMessage());
		}
		logger.info("End of renameFile");
		Assert.assertTrue(isRenamed);
	}

	/**
	 * Test Method to rename a file
	 * 
	 * @param actualFile2
	 *            -holds the actual filename.
	 * @param destFile2
	 *            -holds the new file name
	 */
	@Test
	@Parameters(value = { "actualFile2", "destFile2" })
	public void renameFile2(String actualFile2, String destFile2) {
		logger.info("Start of renameFile2");
		logger.info("Renaming the file :" + actualFile2 + " to:" + destFile2);
		boolean isRenamed = false;
		try {
			logger.info("Reverting file");
			File newFile = new File(destFile2);
			File actFile = new File(actualFile2);
			actFile.renameTo(newFile);
			Util.sleep(10000);
			if (newFile.exists()) {
				isRenamed = true;
				logger.info("Actual file is renamed");
			}
		} catch (Exception e) {
			Assert.fail("Method failed due to :" + e);
			logger.info("Method failed due to :" + e.getMessage());
		}
		logger.info("End of renameFile2");
		Assert.assertTrue(isRenamed);
	}

	/**
	 * Test Method to delete flatfiles
	 * 
	 * @param flatFilesFolder
	 *            -holds the flatFilesFolder
	 * @param archiveFolder
	 *            -holds the archiveFolder
	 */
	@Test
	@Parameters(value = { "flatFilesFolder", "archiveFolder" })
	public void removeFlatFiles(String flatFilesFolder, String archiveFolder) {
		logger.info("Start of removeFlatFiles");
		logger.info("Parameters flatFilesFolder :" + flatFilesFolder
				+ ", archiveFolder:" + archiveFolder);
		boolean isArchiveRemoved = false;
		boolean isFlatFilesRemoved = false;
		File folder = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder
				+ "/" + archiveFolder);
		File[] listOfFiles = null;
		if (folder.exists() && folder.isDirectory()) {
			listOfFiles = folder.listFiles();
			for (int fileNo = 0; fileNo < listOfFiles.length; fileNo++) {

				logger.info("file[fileNo]:" + listOfFiles[fileNo]);
				String filenames = listOfFiles[fileNo].getName();
				if (listOfFiles[fileNo].delete()) {
					logger.info("file:" + filenames + " is deleted");
				}
			}
		} else {
			logger.info("The file:" + ApmbaseConstants.EM_LOC + "/"
					+ flatFilesFolder + "/" + archiveFolder
					+ " is not exist or not a folder.");
		}
		if ((!folder.exists())
				|| (folder.exists() && folder.listFiles().length == 0)) {
			isArchiveRemoved = true;
		}
		folder = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder);
		listOfFiles = null;
		if (folder.exists() && folder.isDirectory()) {
			listOfFiles = folder.listFiles();
			for (int fileNo = 0; fileNo < listOfFiles.length; fileNo++) {

				logger.info("file[fileNo]:" + listOfFiles[fileNo]);
				String filenames = listOfFiles[fileNo].getName();
				if (listOfFiles[fileNo].delete()) {
					logger.info("file:" + filenames + " is deleted");
				}
			}
		} else {
			logger.info("The file:" + ApmbaseConstants.EM_LOC + "/"
					+ flatFilesFolder + " is not exist or not a folder.");
		}
		if (folder.exists() && folder.listFiles().length == 0) {
			logger.info("folder.list():" + folder.listFiles().length);
			isFlatFilesRemoved = true;
		}
		logger.info("End of removeFlatFiles");
		Assert.assertTrue(isArchiveRemoved && isFlatFilesRemoved);
	}

	/**
	 * Test method to have a backup of required files
	 * 
	 * @param fileNames
	 *            -holds the files for which backup is required
	 */
	@Test
	@Parameters(value = { "fileNames" })
	public void backupFile(String fileNames) {
		logger.info("Start of backupFile");
		String message = "Failed";
		String[] fileName = fileNames.split("~");
		for (int index = 0; index < fileName.length; index++) {
			String filePath = emPath + "/" + fileName[index];
			logger.info("Taking backup of the file" + filePath);
			message = fileBackUp(filePath);
			if (message != successMessage)
				break;
		}
		if (message != successMessage)
			logger.info("Unable to copy file");
		else
			logger.info("File copied successfuly");
		logger.info("End of backupFile");
		Assert.assertEquals(message, successMessage);
	}

	/**
	 * Test method to delete the given file and rename the "bak" file back to
	 * original
	 * 
	 * @param fileNames
	 *            -holds the file names which needs to be reverted
	 */
	@Test
	@Parameters(value = { "fileNames" })
	public void revertFile(String fileNames) {
		logger.info("Start of revertFile");
		String[] fileName = fileNames.split("~");
		String message = "Failed";
		for (int index = 0; index < fileName.length; index++) {
			String filePath = emPath + "/" + fileName[index];
			logger.info("Reverting file");
			message = revertFiles(filePath);
			if (message != successMessage)
				break;
		}
		if (message != successMessage)
			logger.info("Unable to revert file");
		else
			logger.info("File reverted successfuly");
		Util.sleep(10000);
		logger.info("End of revertFile");
		Assert.assertEquals(message, successMessage);
	}

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
	public void addProperty(String property, String propFileName) {
		logger.info("Start of addProperty method");
		logger.info("addPropToProfile uses property with value :" + property);
		String fileName = ApmbaseConstants.EM_LOC + "/" + propFileName;
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
	 * Test method to check the metric
	 * 
	 * @param metric
	 *            -holds the metric which needs to be verified in the
	 *            investigator
	 */
	@Test
	@Parameters(value = { "metric" })
	public void checkMetrics(String metric) {
		logger.info("Start of checkMetrics");
		logger.info("checkMetrics uses metric with value :" + metric);
		boolean status = false;
		status = ApmbaseUtil.checkMetricExists(metric, clw);
		if (!status)
			logger.info("Unable to find the required metrics");
		else
			logger.info("Successfully found the required metrics");
		logger.info("End of checkMetrics");
		Assert.assertTrue(status);
	}

	/**
	 * This method is used to check if a given metric exist in investigator.
	 * 
	 * @param clw
	 *            - CLWBean for loggedin user
	 * @param metric
	 *            - metrics to be checked
	 * @param metricValue
	 *            - metric value to be checked
	 * @return Return true if metric exist else false
	 */
	private boolean checkMetricVal(CLWBean clw, String metric,
			String metricValue) {
		logger.info("Start of checkMetricVal method");

		logger.info("clw:" + "" + clw);
		logger.info("metric:" + "" + metric);
		logger.info("metricValue:" + "" + metricValue);

		boolean metricExist, actualMetricValue = false;
		try {
			Util.sleep(60000);
			MetricUtil metricutil = new MetricUtil(metric, clw);
			metricExist = metricutil.metricExists();
			logger.info("checkBasicMetricsExists----metricExist:" + metricExist);
			if (metricExist) {
				while (!actualMetricValue) {
					Util.sleep(30000);
					String[] value = metricutil.getLastNMinutesMetricValues(8);
					for (int i = 0; i < value.length; i++) {
						if (metricValue.equals(value[i])) {
							actualMetricValue = true;
							break;
						}

					}

				}
			}
			if (actualMetricValue) {
				logger.info("Found metric value");
			} else {
				logger.info("Unable to find metric value");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info("End of checkMetricVal method");
		return actualMetricValue;
	}

	/**
	 * Test method to check the value for a given metric
	 * 
	 * @param metric
	 *            -holds the metric for which value needs to be verified
	 * @param expMtrcVal
	 *            -holds the expected metric value
	 */
	@Test
	@Parameters(value = { "metric", "expMtrcVal" })
	public void checkMetricValue(String metric, String expMtrcVal) {
		logger.info("Start of checkMetricValue");
		logger.info("checkMetricValue uses metric with value :" + metric);
		logger.info("checkMetricValue uses expMtrcVal with value :"
				+ expMtrcVal);
		boolean isValFound = false;
		isValFound = checkMetricVal(clw, metric, expMtrcVal);
		logger.info("is Metric value found :" + isValFound);
		if (!isValFound)
			logger.info("Unable to find required metric value");
		else
			logger.info("Found required metric value");
		logger.info("End of checkMetricValue");
		Assert.assertTrue(isValFound);
	}

	/**
	 * Private method to check the log messages in the log File
	 * 
	 * @param filenames
	 *            -Name of the files
	 * @param textmsg
	 *            -Message to be checked in the log file
	 * @return boolean - Whether message is found or not
	 */
	private boolean checkLogs(String filenames, String textmsg) {
		boolean isMessageChecked = false;
		int ischecked = 0;
		String emFilePath = emPath
				+ ApmbaseConstants.LOG_PATH_LOC + "/" + filenames;
		String[] logMsg = textmsg.split("~");
		try {
			logger.info("***********Start of checkLogs*******");
			logger.info("filenames*******" + filenames);
			logger.info("textmsg*******" + textmsg);
			for (int count = 0; count < logMsg.length; count++) {
				isMessageChecked = ApmbaseUtil.checkValidLastUpdateIgnoreCase(emFilePath,
						logMsg[count]);
				logger.info("isMessageChecked" + isMessageChecked);
				if (isMessageChecked) {
					ischecked++;
				}
			}
		} catch (Exception e) {
			logger.error("caught exception due to:" + e.getMessage());
		}
		if (ischecked == logMsg.length) {
			logger.info("All log messages  exist");
			isMessageChecked = true;
		} else {
			logger.info("Some log messages   do not  not  Exist  ");
		}
		logger.info("***********End of checkLogs*******");
		return isMessageChecked;
	}

	/**
	 * Test method to check for the message in the log file
	 * 
	 * @param filenames
	 *            -Name of the file in which log should be checked for
	 * @param textmsg1
	 *            -Message that should be checked in the log message
	 */
	@Test
	@Parameters(value = { "filenames", "textmsg1" })
	public void checkLogMessage1(String filenames, String textmsg1) {
		logger.info("***********Inside  checkLogMessage1  method*******");
		logger.info("Wait for EM to initialize");
		Util.sleep(60000);
		boolean isMessageChecked = false;
		try {
			isMessageChecked = checkLogs(filenames, textmsg1);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("***********Outside  of   checkLogMessage1 method*******");
		Assert.assertTrue(isMessageChecked);
	}

	/**
	 * Test method to check for the message in the log file
	 * 
	 * @param filenames2
	 *            -Name of the file in which log should be checked for
	 * @param textmsg2
	 *            -Message that should be checked in the log message
	 */
	@Test
	@Parameters(value = { "filenames2", "textmsg2" })
	public void checkLogMessage2(String filenames2, String textmsg2) {
		logger.info("***********Inside  checkLogMessage2 method*******");
		boolean isMessageChecked = false;
		try {
			isMessageChecked = checkLogs(filenames2, textmsg2);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("***********Outside  of   checkLogMessage2 method*******");
		Assert.assertTrue(isMessageChecked);
	}

	/**
	 * Test method to set the properties in a particular files
	 * 
	 * @param setProperties4
	 *            -Properties that are to be updated
	 * @param setValues4
	 *            -Values with which the properties need to be set to
	 * @param expected
	 *            -Value that is expected
	 */
	@Test
	@Parameters(value = { "setProperties4", "setValues4", "expected" })
	public void setProperties4(String setProperties4, String setValues4,
			String expected) {
		logger.info("In setProperties4 method");
		int status = 0;
		try {
			status = updateEMProps(setProperties4, setValues4);
			logger.info("status is ::" + status);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("Out of  setProperties4 method");
		Assert.assertEquals(status, Integer.parseInt(expected));
	}

	/**
	 * Test method to set the properties in a particular files
	 * 
	 * @param setProperties3
	 *            -Properties that are to be updated
	 * @param setValues3
	 *            -Values with which the properties need to be set to
	 * @param expected
	 *            -Value that is expected
	 */
	@Test
	@Parameters(value = { "setProperties3", "setValues3", "expected" })
	public void setProperties3(String setProperties3, String setValues3,
			String expected) {
		logger.info("In setProperties3 method");
		int status = 0;
		try {
			status = updateEMProps(setProperties3, setValues3);
			logger.info("status is ::" + status);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("Out of  setProperties3 method");
		Assert.assertEquals(status, Integer.parseInt(expected));
	}

	/**
	 * Test method to set the properties in a particular files
	 * 
	 * @param setProperties2
	 *            -Properties that are to be updated
	 * @param setValues2
	 *            -Values with which the properties need to be set to
	 * @param expected
	 *            -Value that is expected
	 */
	@Test
	@Parameters(value = { "setProperties2", "setValues2", "expected" })
	public void setProperties2(String setProperties2, String setValues2,
			String expected) {
		logger.info("In setProperties2 method");
		int status = 0;
		try {
			status = updateEMProps(setProperties2, setValues2);
			logger.info("status is ::" + status);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("Out of  setProperties2 method");
		Assert.assertEquals(status, Integer.parseInt(expected));
	}

	/**
	 * Test method to set the properties for used port
	 * 
	 * @throws IOException
	 */
	@Test
	@Parameters(value = {"emhost"}) 
	public void setPropertiesForUsedPort(String emHost) throws IOException {
		logger.info("Inside setPropertiesForUsedPort method");
		int status = 0;
		usedPort = getUsedPort(emHost);
		try {
			status = updateEMProps(
					"introscope.enterprisemanager.webserver.port", usedPort);
			logger.info("status is ::" + status);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("Outside of  setPropertiesForUsedPort method");
		Assert.assertEquals(status, 1);
	}

	/**
	 * Test method to set the properties in a particular files
	 * 
	 * @param setProperties1
	 *            -Properties that are to be updated
	 * @param setValues1
	 *            -Values with which the properties need to be set to
	 * @param expected
	 *            -Value that is expected
	 */
	@Test
	@Parameters(value = { "setProperties1", "setValues1", "expected" })
	public void setProperties1(String setProperties1, String setValues1,
			String expected) {
		logger.info("Inside setProperties1 method");
		int status = 0;
		try {
			status = updateEMProps(setProperties1, setValues1);
			logger.info("status is ::" + status);
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
			Assert.fail("Method failed due to :" + e.getMessage());
		}
		logger.info("Outside of  setProperties1 method");
		Assert.assertEquals(status, Integer.parseInt(expected));
	}

	/**
	 * Test method to wait for required interval of time
	 * 
	 * @param minutes
	 *            -holds the interval of time for which sleep is required
	 */
	@Test
	@Parameters(value = { "minutes" })
	public void sleepTime(String minutes) {
		logger.info("Start of sleepTime method");
		logger.info("sleepTime method uses minutes with value :" + minutes);
		Util.sleep(Integer.parseInt(minutes) * 60000);
		logger.info("End of sleepTime method");
	}

	/**
	 * Test method to wait for required interval of time
	 * 
	 * @param minutes
	 *            -holds the interval of time for which sleep is required
	 */
	@Test
	@Parameters(value = { "minutes" })
	public void sleepTime2(String minutes) {
		logger.info("Start of sleepTime2 method");
		logger.info("sleepTime method uses minutes with value :" + minutes);
		Util.sleep(Integer.parseInt(minutes) * 60000);
		logger.info("End of sleepTime2 method");
	}

	/**
	 * Test method to wait for required interval of time
	 * 
	 * @param minutes
	 *            -holds the interval of time for which sleep is required
	 */
	@Test
	@Parameters(value = { "minutes" })
	public void sleepTime3(String minutes) {
		logger.info("Start of sleepTime3 method");
		logger.info("sleepTime method uses minutes with value :" + minutes);
		Util.sleep(Integer.parseInt(minutes) * 60000);
		logger.info("End of sleepTime3 method");
	}

	/**
	 * Test method to wait for required interval of time
	 * 
	 * @param minutes
	 *            -holds the interval of time for which sleep is required
	 */
	@Test
	@Parameters(value = { "minutes" })
	public void sleepTime4(String minutes) {
		logger.info("Start of sleepTime4 method");
		logger.info("sleepTime method uses minutes with value :" + minutes);
		Util.sleep(Integer.parseInt(minutes) * 60000);
		logger.info("End of sleepTime4 method");
	}

	/**
	 * Test method to check if file exists in the log folder
	 * 
	 * @param fileNames
	 *            -names of the files that need to checked in the logs folder
	 */
	@Test
	@Parameters(value = { "fileName" })
	public void checkLogFileExist(String fileName) {
		boolean status = false;
		logger.info("Inside checkLogFileExist method");
		try {
			status = checkFileExist(fileName);
			logger.info("status ::" + status);
		} catch (Exception e) {
			logger.error("caught exception due to:" + e.getMessage());
			Assert.fail("Unable to Execute the command " + e.getMessage());
		}
		logger.info("Outside of  checkLogFileExist method");
		Assert.assertTrue(status);
	}

	/**
	 * Test method to check if file exists in the log folder
	 * 
	 * @param fileNames2
	 *            -names of the files that need to checked in the logs folder
	 */
	@Test
	@Parameters(value = { "fileNames2" })
	public void checkLogFileNotExist(String fileNames2) {
		boolean status = false;
		logger.info("Inside checkLogFileNotExist method");
		try {
			status = checkFileExist(fileNames2);
			logger.info("status ::" + status);
		} catch (Exception e) {
			logger.error("caught exception due to:" + e.getMessage());
			Assert.fail("Unable to Execute the command " + e.getMessage());
		}
		logger.info("Outside of  checkLogFileNotExist method");
		Assert.assertFalse(status);
	}

	/**
	 * Private method to check if file exists in the logs folder
	 * 
	 * @param fileNames
	 *            -names of the files that need to checked in the logs folder
	 * @return boolean -If success returns true,else false
	 */
	private boolean checkFileExist(String fileNames) {
		boolean status = false;
		int fileExist = 0;
		String filenames[] = fileNames.split("~");
		for (int count = 0; count <= filenames.length - 1; count++) {
			try {
				logger.info("Inside of  checkFileExist method");
				String filepath = emPath
						+ ApmbaseConstants.LOG_PATH_LOC + "/"
						+ filenames[count];
				logger.info("filepath is ::" + filepath);
				logger.info("filenames ::" + filenames[count]);
				logger.info("Complete filepath is  ::" + filepath);
				status = ApmbaseUtil.fileExists(filepath);
				logger.info("status is  ::" + status);
				if (status) {
					fileExist++;
				}
			} catch (Exception e) {
				logger.error("Unable to check the content of the  the file due to :"
						+ e.getMessage());
			}
		}
		if (fileExist == filenames.length) {
			status = true;
			logger.info("All Files  exist");
		} else {
			logger.info("Some files  do not  not  Exist  ");
		}
		logger.info("Outside of  checkFileExist  method");
		return status;
	}
	/**
     * This method is used to startEM after editing registry and properties
     * 
     * @param emLogFileNameAndPath
     *            //To pass logfile name path of the EM
     * @param messagetoVerify
     *            //To pass message to verify whether EM started or not
     */
    @Test   
    public void startEMPortInUse()
    {
        logger.info("##########startEMAfterEditingProperties Start##########");
        Process process = null;
        boolean status = false;
        logger.info("EM StartUp Initial status:" + status);
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
                    while (timer<=30000)
                    {
                        timer=timer+10000;
                        Util.sleep(10000);                 
                    }
					status = true;
					} catch (IOException e)
                {
                    logger.error(e.getMessage());
                }

            }
        }
		Assert.assertTrue(status);
        
    }
	

//	@Test
//	@Parameters(value = { "emhost", "emport", "notExpected" })
//	public void startEMFails(String emhost, String emport, String notExpected) {
//		logger.info("Start of startEMFails");
//		logger.info("Parameters for method are,emhost:" + emhost + ",emport:"
//				+ emport + ",notExpected:" + notExpected);
//		int status = 0;
//		try {
//			status = ApmbaseUtil.startEM(
//					(ApmbaseConstants.EM_LOC + "/" + ApmbaseConstants.EM_EXE),
//					emhost, Integer.parseInt(emport));
//			logger.info("emstatus is" + status);
//		} catch (Exception e) {
//			logger.error("Method failed due to :" + e.getMessage());
//			Assert.fail("Method failed due to :" + e.getMessage());
//		}
//		logger.info("End of startEMFails");
//		Assert.assertEquals(status, Integer.parseInt(notExpected));
//	}
	

	/**
	 * This method is to set a property in Introscope_Enterprise_Manager.lax for
	 * making collector slow
	 * 
	 * @param file_name
	 *            - name of the file
	 * @param prop_name
	 *            - name of the property under which we need to add a new one
	 * @param propLst
	 *            value of the property
	 * 
	 * @throws Exception
	 */
	@Test
	@Parameters(value = { "file_name", "prop_name", "propLst" })
	public static void setSlowProperties(String file_name, String prop_name,
			String propLst) throws Exception {

		logger.info("Start of setSlowProperties method");

		logger.info("file_name:" + "" + file_name);
		logger.info("prop_name" + "" + prop_name);
		logger.info("prop_value:" + "" + propLst);

		boolean propSet = false;
		BufferedWriter out = null;

		String file = ApmbaseConstants.EM_LOC + "\\" + file_name;
		BufferedReader br1 = new BufferedReader(new FileReader(file));
		String line = "";

		FileWriter fstream = new FileWriter(file, true);
		out = new BufferedWriter(fstream);

		List<String> propList = new ArrayList<String>();
		propList.add(propLst);

		while ((line = br1.readLine()) != null) {
			String prop_line = prop_name.trim();
			logger.info("Appending a property to Introscope_Enterprise_Manager.lax under "
					+ prop_line);
			if ((line.trim().contains(prop_line))) {
				int set = ApmbaseUtil.setproperties(file_name,
						ApmbaseConstants.EM_LOC, prop_name, propLst);
				logger.info("added property:" + set);
				break;
			}
		}

		logger.info("End of setSlowProperties method");
		ApmbaseUtil util = new ApmbaseUtil();
		int propertySet = util.checkproperties(file_name,
				ApmbaseConstants.EM_LOC, prop_name, propLst);
		if (propertySet == 1) {
			propSet = true;
		}
		Assert.assertTrue(propSet);
	}

	/**
	 * Method to copy specified file to specified location
	 * 
	 * @param srcfilePath
	 *            -holds path of the file for which copy is needed
	 * @param destfilePath
	 *            -holds path of the file to which copy needs to be created
	 * @return - success message whether the file is copied successfully or not
	 */
	private String copyFiletoDest(String srcfilePath, String destfilePath) {
		logger.info("Start of copyFiletoDest");
		logger.info("copyFile uses srcfilePath with value :" + srcfilePath);
		logger.info("copyFile uses destfilePath with value :" + destfilePath);
		logger.info("Copying the files required");
		String message = "failed";
		try {
			logger.info("Creating copy");
			File srcPath = new File(srcfilePath);
			File destPath = new File(destfilePath);
			FileUtils.copy(srcPath, destPath);
			if (destPath.exists()) {
				message = successMessage;
				logger.info("copy Created.");
			}
		} catch (Exception e) {
			logger.error("Method failed due to :" + e.getMessage());
		}
		logger.info("End of copyFiletoDest");
		return message;
	}

	/**
	 * Test Method to copy specified file to specified location
	 * 
	 * @param fileNametoCpy
	 *            -holds file name for which copy needs to be created
	 * @param srcPath
	 *            -holds path of the file for which copy is needed
	 * @param destPath
	 *            -holds path of the file to which copy needs to be created
	 */
	@Test
	@Parameters(value = { "fileNametoCpy", "srcPath", "destPath" })
	public void copyFile(String fileNametoCpy, String srcPath, String destPath) {
		logger.info("Start of copyFile");
		logger.info("copyFile uses fileNametoCpy with value" + fileNametoCpy);
		String srcfilePath = srcPath + "/" + fileNametoCpy;
		logger.info("copyFile uses srcfilePath with value" + srcfilePath);
		String destfilePath = destPath + "/" + fileNametoCpy;
		if (!new File(destPath).exists() && !new File(destPath).mkdir()) {
			logger.info("The destination directory:" + destPath
					+ " does not exist and also not able to create one");
		}
		logger.info("copyFile uses destfilePath with value" + destfilePath);
		logger.info("Taking backup of the file");
		String message = copyFiletoDest(srcfilePath, destfilePath);
		if (message != successMessage)
			logger.info("Unable to copy file");
		else
			logger.info("File copied successfuly");
		Util.sleep(10000);
		logger.info("End of copyFile");
		Assert.assertEquals(message, successMessage);
	}

	/**
	 * Test Method to copy specified file to specified location
	 * 
	 * @param fileNametoCpy1
	 *            -holds file name for which copy needs to be created
	 * @param srcPath
	 *            -holds path of the file for which copy is needed
	 * @param destPath
	 *            -holds path of the file to which copy needs to be created
	 */
	@Test
	@Parameters(value = { "fileNametoCpy1", "srcPath", "destPath" })
	public void copyFile1(String fileNametoCpy1, String srcPath, String destPath) {
		logger.info("Start of copyFile1");
		logger.info("copyFile1 uses fileNametoCpy1 with value" + fileNametoCpy1);
		String srcfilePath = srcPath + fileNametoCpy1;
		logger.info("copyFile1 uses srcfilePath with value" + srcfilePath);
		String destfilePath = destPath + "/" + fileNametoCpy1;
		logger.info("copyFile1 uses destfilePath with value" + destfilePath);
		logger.info("Taking backup of the file");
		String message = copyFiletoDest(srcfilePath, destfilePath);
		if (message != successMessage)
			logger.info("Unable to copy file");
		else
			logger.info("File copied successfuly");
		Util.sleep(10000);
		logger.info("End of copyFile1");
		Assert.assertEquals(message, successMessage);
	}

	/**
	 * Helper method to hit the URL passed
	 * 
	 * @param appURL
	 *            - carries the URL to be hit
	 * @param hostName
	 *            -holds the host name
	 * @return responseCode - carries the appropriate URL Hit response
	 * @throws IOException
	 */
	private int appInvoke(String appURL, String hostName) throws IOException {
		OutputStreamWriter out = null;
		logger.info("Start of appInvoke method");
		logger.info("appInvoke method uses appURL with value :" + appURL);
		try {
			logger.info("Invoking the TestApp");
			logger.info("Hitting the Url :  http://" + hostName + appURL);
			URL url = new URL("http://" + hostName + appURL);
			HttpURLConnection httpCon = (HttpURLConnection) url
					.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			out = new OutputStreamWriter(httpCon.getOutputStream());
			try {
				logger.info(httpCon.getResponseMessage());
			} catch (Exception e) {
				logger.error("Method failed due to " + e.getMessage());
			}
			int responseCode = httpCon.getResponseCode();
			logger.info("response code for URL is:" + responseCode);
			logger.info("End of appInvoke method");
			return responseCode;
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Helper method internally calls the appInvoke and checks for the response.
	 * 
	 * @param appURL
	 *            - carries the URL to start the app
	 * @param hostName
	 *            -holds the host name
	 * @return response code
	 */
	private int startApp(String appURL, String hostName) {
		int responseCode = 0;
		try {
			logger.info("Start of startApp");
			logger.info("startApp uses appURL with value :" + appURL);
			int actualTime = 3 * 60000;
			int respTime = 0;
			int pollInterval = 30000;
			logger.info("Browsing URL");
			while (true) {
				responseCode = appInvoke(appURL, hostName);
				logger.info("response code for URL is:" + responseCode);
				if (respTime > actualTime || (responseCode == 200)) {
					break;
				}
				respTime = respTime + pollInterval;
				logger.info("Elapsed Time is:" + respTime);
				Thread.sleep(pollInterval);
			}
		} catch (Exception e) {
			logger.error("Method failed due to the following reason :"
					+ e.getMessage());
		}
		logger.info("End of startApp");
		return responseCode;
	}

	/**
	 * Helper method internally calls the appInvoke and checks for the response.
	 * 
	 * @param appURL
	 *            - carries the URL to start the app
	 * @param hostName
	 *            -holds the host name
	 * @return response code
	 */
	private boolean startAppWithPort(String hostName, String port) {
		boolean isInvokeSucessful = false;
		try {
			logger.info("Start of startAppWithPort");
			logger.info("startApp uses hostName with value :" + hostName);
			logger.info("Browsing URL");
			OutputStreamWriter out = null;
			try {
				logger.info("Invoking the TestApp");
				logger.info("Hitting the Url :  http://" + hostName + ":"
						+ port);
				URL url = new URL("http://" + hostName + ":" + port);
				HttpURLConnection httpCon = (HttpURLConnection) url
						.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestMethod("POST");
				out = new OutputStreamWriter(httpCon.getOutputStream());
				try {
					logger.info(httpCon.getResponseMessage());
				} catch (Exception e) {
					logger.info("Method failed due to " + e.getMessage());
					isInvokeSucessful = true;
				}
				int responseCode = httpCon.getResponseCode();
				logger.info("response code for URL is:" + responseCode);
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} catch (Exception e) {
			logger.info("Method failed due to the following reason :"
					+ e.getMessage());
			isInvokeSucessful = true;
		}
		logger.info("End of startAppWithPort");
		return isInvokeSucessful;
	}

	/**
	 * Used to hit an app url
	 * 
	 * @param appUrl
	 *            - carries the URL to perform the transaction on app
	 * @param hostName
	 *            -holds the host name
	 * 
	 */
	@Test
	@Parameters({ "appUrl", "hostName" })
	public void hitAppUrl(String appUrl, String hostName) {
		logger.info("Start of hitAppUrl");
		logger.info("hitAppUrl uses appUrl with value :" + appUrl);
		logger.info("hitAppUrl uses hostName with value :" + hostName);
		int actualResponse = startApp(appUrl, hostName);
		logger.info("actualResponse:" + actualResponse);
		Util.sleep(10000);
		if (actualResponse == 200) {
			logger.info("App launched successfully");
		} else
			logger.info("Unable to launch the App");
		logger.info("End of hitAppUrl");
		Assert.assertEquals(actualResponse, 200);
	}

	/**
	 * Used to hit an app url
	 * 
	 * @param hostName
	 *            -holds the host name
	 * 
	 */
	@Test
	@Parameters({ "hostName" })
	public void hitAppUrl3(String hostName) {
		logger.info("Start of hitAppUrl3");
		logger.info("hitAppUrl uses hostName with value :" + hostName);
		boolean actualResponse = startAppWithPort(hostName, "8081");
		logger.info("actualResponse from appInvokeWithPort:" + actualResponse);
		if (actualResponse) {
			logger.info("the port is not listening as expected");
		} else
			logger.info("Unable to launch the App");
		logger.info("End of hitAppUrl3");
		Assert.assertTrue(actualResponse);
	}

	/**
	 * Used to hit an app URL
	 * 
	 * @param appUrl1
	 *            - carries the URL to perform the transaction on app
	 * @param hostName
	 *            -holds the host name
	 * 
	 */
	@Test
	@Parameters({ "appUrl1", "hostName" })
	public void hitAppUrl1(String appUrl1, String hostName) {
		logger.info("Start of hitAppUrl1");
		logger.info("hitAppUrl1 uses appUrl with value :" + appUrl1);
		logger.info("hitAppUrl1 uses hostName with value :" + hostName);
		int actualResponse = startApp(appUrl1, hostName);
		logger.info("actual Response code:" + actualResponse);
		if (actualResponse == 200)
			logger.info("App launched successfully");
		else
			logger.info("Unable to launch the App");
		logger.info("End of hitAppUrl1");
		Assert.assertEquals(actualResponse, 200);
	}

	/**
	 * Test method is used before starting of every testcase.
	 * 
	 * @param testCaseNameIDPath
	 *            -Passing perforce path and Testcase ID
	 */
	@Test
	@Parameters(value = { "testCaseNameIDPath" })
	public void testCaseStart(String testCaseNameIDPath) {
		logger.info("##########" + testCaseNameIDPath + "#########" + "Start");
	}

	/**
	 * Test method is used at the end of every Test case.
	 * 
	 * @param duration
	 *            // Passing perforce path and Test case ID
	 */
	@Test
	@Parameters(value = { "testCaseNameIDPath" })
	public void testCaseEnd(String testCaseNameIDPath) {
		logger.info("##########" + testCaseNameIDPath + "#########" + "End");
	}

	/**
	 * Helper method used to update the property and propertyValue in the
	 * IntroscopeAgent.profile file
	 * 
	 * @param setValues
	 *            - holds the property value to be set to a property in the
	 *            profile file.
	 * @param setProperties
	 *            - holds the property name which should edited
	 * 
	 * 
	 * @return int - returns whether the property is updated(1) or not(0)
	 */

	private int updateEMProps(String setProperties, String setValues) {
		int result = 0;
		try {
			logger.info("Executing updateEMProps method");
			Properties properties = Util
					.loadPropertiesFile(IntroscopePropertiesFilePath + "/"
							+ IntroscopePropertiesFileName);
			String setproperties[] = setProperties.split("~");
			String setvalues[] = setValues.split("~");
			int setProps = 0;
			for (int count = 0; count <= setproperties.length - 1; count++) {
				properties.setProperty(setproperties[count], setvalues[count]);
			}
			Util.writePropertiesToFile(IntroscopePropertiesFilePath + "/"
					+ IntroscopePropertiesFileName, properties);
			Util.sleep(10000);
			properties = Util.loadPropertiesFile(IntroscopePropertiesFilePath
					+ "/" + IntroscopePropertiesFileName);
			for (int count = 0; count <= setproperties.length - 1; count++) {
				if (((String) properties.get(setproperties[count]))
						.equalsIgnoreCase(setvalues[count])) {
					setProps++;
				}
			}
			if (setProps == setproperties.length) {
				logger.info("All properties are set ");
				result = 1;
			}
		} catch (Exception e) {
			logger.info("caught exception due to:" + e.getMessage());
		}
		logger.info("Some properties are not  set ");
		logger.info("Outside of  updateEMProps  method");
		return result;
	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the first message to be checked
	 * @param secondMessage
	 *            -Holds the second message to be checked
	 * @param emStartUpMessage
	 *            -Holds the em start message
	 * @param expected3
	 *            -Holds true/false whether message is required or not
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @param iscopeHelpCMD
	 *            -Holds the Introscope Help Command
	 * @param iscopeHelpCMD
	 *            -Holds the Introscope Shutdown Command
	 * @throws IOException
	 * @return true if it is in Interactive mode
	 */
	@Test
	@Parameters(value = { "firstMessage", "secondMessage", "emStartUpMessage",
			"expected3", "maxTimeInMTS", "iscopeHelpCMD", "iscopeShutCMD" })
	private boolean CheckForEMInteractiveModeCommon(String firstMessage,
			String secondMessage, String emStartUpMessage, String expected3,
			String maxTimeInMTS, String iscopeHelpCMD, String iscopeShutCMD,
			boolean isInteractiveMode) throws IOException {
		logger.info("start of CheckForEMInteractiveModeCommon method");
		logger.info("Parameters are,firstMessage:" + firstMessage
				+ ",secondMessage:" + secondMessage + ",emStartUpMessage:"
				+ emStartUpMessage + ",expected3:" + expected3
				+ ",maxTimeInMTS:" + maxTimeInMTS + ",iscopeHelpCMD:"
				+ iscopeHelpCMD + ",iscopeShutCMD:" + iscopeShutCMD);
		String finalCmnd[] = { "cmd.exe", "/c",
				ApmbaseConstants.EM_LOC + ApmbaseConstants.emExePath, "<",
				ApmbaseConstants.EM_LOC + "/" + "temp.txt" };
		ProcessBuilder processBuilder = new ProcessBuilder(finalCmnd);
		processBuilder.redirectErrorStream(true);
		Process process = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		File tempFile = new File(ApmbaseConstants.EM_LOC + "/" + "temp.txt");
		if (tempFile.exists()) {
			tempFile.delete();
		}
		if (tempFile.createNewFile()) {
			logger.info("temporory file is created with required commands.");
		}
		bw = new BufferedWriter(new FileWriter(tempFile));
		bw.write(iscopeHelpCMD + "\n");
		bw.write(iscopeShutCMD + "\n");
		bw.close();
		String line = "";
		boolean isEMStarted = false;
		boolean foundFirst = false;
		boolean foundSecond = false;
		try {
			process = processBuilder.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String firstMessages[] = firstMessage.split("~");
			String secondMessages[] = null;
			if (secondMessage != null) {
				secondMessages = secondMessage.split("~");
			}
			int count = 0;
			int count2 = 0;
			int maxTimeOut = Integer.parseInt(maxTimeInMTS);
			long beforeReading = Calendar.getInstance().getTimeInMillis();
			logger.info("Time Out:" + maxTimeOut);
			while ((Calendar.getInstance().getTimeInMillis() - beforeReading)
					/ minuteInMillis < maxTimeOut
					&& (line = br.readLine()) != null) {
			//	logger.info("<<<<<<<<The Line>>>>>>>:" + line);
			/*	logger.info("Time Elapsed:"
						+ (Calendar.getInstance().getTimeInMillis() - beforeReading)
						/ minuteInMillis);*/
				if (line.contains("Press 'Enter' to acknowledge")) {
					logger.info("failed while starting EM,killing em process");
					break;
				}

				if (line.contains(emStartUpMessage)) {
					isEMStarted = true;
				}
				if (!foundFirst) {
					if (findMessage(line, firstMessages[count])) {
						count++;
					}
					if (count == firstMessages.length) {
						foundFirst = true;
						logger.info("reading shutdown command to process:"
								+ iscopeShutCMD);
					}
				}
				if (foundFirst && !foundSecond) {
					if (findMessage(line, secondMessages[count2])) {
						count2++;
					}
					if (count2 == secondMessages.length) {
						foundSecond = true;
						break;
					}
				}
			}
			if (!isEMStarted) {
				logger.info("EM not started");
			}
			logger.info("foundFirst:" + foundFirst + ",foundSecond:"
					+ foundSecond);
		} catch (IOException e) {
			logger.info("Unable to read the console due to :" + e.getMessage());
		} finally {
			br.close();
			if (process != null) {
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		}
		logger.info("CheckForEMInteractiveModeCommon Method Ended");
		if (isInteractiveMode) {
			return (foundFirst && foundSecond);
		}
		return (!foundFirst);
	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the first message to be checked
	 * @param secondMessage
	 *            -Holds the second message to be checked
	 * @param emStartUpMessage
	 *            -Holds the em start message
	 * @param expected3
	 *            -Holds true/false whether message is required or not
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @param iscopeHelpCMD
	 *            -Holds the Introscope Help Command
	 * @param iscopeHelpCMD
	 *            -Holds the Introscope Shutdown Command
	 * @throws IOException
	 */
	@Test
	@Parameters(value = { "firstMessage", "secondMessage", "emStartUpMessage",
			"expected3", "maxTimeInMTS", "iscopeHelpCMD", "iscopeShutCMD" })
	public void CheckForEMInteractiveMode(String firstMessage,
			String secondMessage, String emStartUpMessage, String expected3,
			String maxTimeInMTS, String iscopeHelpCMD, String iscopeShutCMD)
			throws IOException {
		logger.info("start of CheckForEMInteractiveMode method");
		logger.info("Parameters are,firstMessage:" + firstMessage
				+ ",secondMessage:" + secondMessage + ",emStartUpMessage:"
				+ emStartUpMessage + ",expected3:" + expected3
				+ ",maxTimeInMTS:" + maxTimeInMTS + ",iscopeHelpCMD:"
				+ iscopeHelpCMD + ",iscopeShutCMD:" + iscopeShutCMD);
		Assert.assertTrue(CheckForEMInteractiveModeCommon(firstMessage,
				secondMessage, emStartUpMessage, expected3, maxTimeInMTS,
				iscopeHelpCMD, iscopeShutCMD, true));
	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the first message to be checked
	 * 
	 * @param emStartUpMessage
	 *            -Holds the em start message
	 * @param expected2
	 *            -Holds true/false whether message is required or not
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @param iscopeHelpCMD
	 *            -Holds the Introscope Help Command
	 * 
	 * @throws IOException
	 */
	@Test
	@Parameters(value = { "firstMessage", "emStartUpMessage", "expected2",
			"maxTimeInMTS2", "iscopeHelpCMD" })
	public void CheckForEMInteractiveMode2(String firstMessage,
			String emStartUpMessage, String expected2, String maxTimeInMTS2,
			String iscopeHelpCMD) throws IOException {
		logger.info("start of CheckForEMInteractiveMode2 method");
		logger.info("Parameters are,firstMessage:" + firstMessage
				+ ",emStartUpMessage:" + emStartUpMessage + ",expected2:"
				+ expected2 + ",maxTimeInMTS2:" + maxTimeInMTS2
				+ ",iscopeHelpCMD:" + iscopeHelpCMD);
		logger.info("CheckForEMInteractiveMode2 Method Ended");
		Assert.assertTrue(CheckForEMInteractiveModeCommon(firstMessage, null,
				emStartUpMessage, expected2, maxTimeInMTS2, iscopeHelpCMD,
				null, false));
	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the message to be checked
	 * @param emStartUpMessage
	 *            -Message that says EM has started .It is taken from log file
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @throws IOException
	 * @return true if all messages found present in firstMessage
	 */
	@Test
	@Parameters(value = { "firstMessage", "emStartUpMessage", "maxTimeInMTS" })
	private boolean ReadEMMessageCommon(String firstMessage,
			String emStartUpMessage, String maxTimeInMTS) throws IOException {
		logger.info("start of ReadEMMessageCommon method");
		logger.info("Parameters are,firstMessage:" + firstMessage
				+ ",emStartUpMessage:" + emStartUpMessage + ",maxTimeInMTS:"
				+ maxTimeInMTS);
		String finalCmnd[] = { "cmd.exe", "/c",
				ApmbaseConstants.EM_LOC + ApmbaseConstants.emExePath };
		ProcessBuilder processBuilder = new ProcessBuilder(finalCmnd);
		processBuilder.redirectErrorStream(true);
		Process process = null;
		BufferedReader br = null;
		String line = "";
		boolean isEMStarted = false;
		boolean foundFirst = false;
		try {
			process = processBuilder.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String firstMessages[] = firstMessage.split("~");
			int count = 0;
			int maxTimeOut = Integer.parseInt(maxTimeInMTS);
			long beforeReading = Calendar.getInstance().getTimeInMillis();
			logger.info("Time Out:" + maxTimeOut);
			while ((Calendar.getInstance().getTimeInMillis() - beforeReading)
					/ minuteInMillis < maxTimeOut
					&& (line = br.readLine()) != null) {
				//logger.info("<<<<<<<<The Line>>>>>>>:" + line);
				/*logger.info("Time Elapsed:"
						+ (Calendar.getInstance().getTimeInMillis() - beforeReading)
						/ minuteInMillis);
				*/
				if (line.contains("Press 'Enter' to acknowledge")) {
					logger.info("failed while starting EM,killing em process");
					break;
				}

				if (line.contains(emStartUpMessage)) {
					isEMStarted = true;
				}
				if (!foundFirst) {
					if (findMessage(line, firstMessages[count])) {
						count++;
					}
					if (count == firstMessages.length) {
						foundFirst = true;
						break;
					}
				}
			}
			if (!isEMStarted) {
				logger.info("EM not started");
			}
			logger.info("foundFirst:" + foundFirst);
		} catch (IOException e) {
			logger.info("Unable to read EM Console due to :" + e.getMessage());
		} finally {
			br.close();
			if (process != null) {
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		}
		logger.info("ReadEMMessageCommon Method Ended");
		return (foundFirst);
	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the message to be checked
	 * @param emStartUpMessage
	 *            -Message that says EM has started .It is taken from log file
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @throws IOException
	 */
	@Test
	@Parameters(value = { "firstMessage", "emStartUpMessage", "maxTimeInMTS" })
	public void ReadEMMessage(String firstMessage, String emStartUpMessage,
			String maxTimeInMTS) throws IOException {
		logger.info("start of ReadEMMessage method");
		logger.info("Parameters are,firstMessage:" + firstMessage
				+ ",emStartUpMessage:" + emStartUpMessage + ",maxTimeInMTS:"
				+ maxTimeInMTS);
		logger.info("ReadEMMessage Method Ended");
		Assert.assertTrue(ReadEMMessageCommon(firstMessage, emStartUpMessage,
				maxTimeInMTS));
	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the message to be checked
	 * @param emStartUpMessage
	 *            -Message that says EM has started .It is taken from log file
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @throws IOException
	 */
	@Test
	@Parameters(value = { "firstMessage2"})
	public void ReadEMMessage2(String firstMessage2) throws IOException {

		logger.info("start of ReadEMMessage2 method");



		logger.info("Parameters are,firstMessage2:" + firstMessage2);
		logger.info("ReadEMMessage2 Method Ended");
		Assert.assertTrue(checkLogs("IntroscopeEnterpriseManager.log",firstMessage2));

	}

	/**
	 * Test method is used to start the Introscope Enterprise Manager and to
	 * read its console
	 * 
	 * @param firstMessage
	 *            -Holds the message to be checked
	 * @param emStartUpMessage
	 *            -Message that says EM has started .It is taken from log file
	 * @param maxTimeInMTS
	 *            -Holds the waiting time
	 * @throws IOException
	 */
	@Test
	@Parameters(value = { "firstMessage3" })
	public void ReadEMMessage3(String firstMessage3) throws IOException {
		logger.info("start of ReadEMMessage3 method");
		logger.info("Parameters are,firstMessage3:" + firstMessage3);
		firstMessage3 = firstMessage3.replace("XXXX", "8080");
		logger.info("ReadEMMessage3 Method Ended");
		Assert.assertTrue(checkLogs("IntroscopeEnterpriseManager.log",firstMessage3));
	}

	/**
	 * Common method to find message in the log file
	 * 
	 * @param line
	 *            -Line at which the message is
	 * @param message
	 *            -Messages that needs to be checked
	 * @return -Returns boolean if message are found
	 * @throws IOException
	 */
	private boolean findMessage(String line, String message) throws IOException {
		boolean found = false;
		//logger.info("Inside findMessage method");
		//logger.info("Parameters are,line:" + line + ",message:" + message);
		if (line.contains(message)) {
			found = true;
			logger.info("the message:" + message + ",found in line:" + line);
		}
		//logger.info("Value of found is" + found);
		//logger.info("End of findMessage method");
		return found;
	}

	/**
	 * Test method to check the size of the given csv file and later check in
	 * the archives if required
	 * 
	 * @param AgentCSVFile
	 *            -holds the name of the csv file
	 * @param csvMaxSizeINKB
	 *            -holds the maximum size of the file
	 * @param isArchiveNeeded
	 *            -holds boolean value whether requires to check for archives or
	 *            not
	 * @param maxWaitTime
	 *            -holds the maximum size of the file
	 * @param fileSizeTolerance
	 *            -holds the tolerance value
	 * @param flatFilesFolder
	 *            -holds the flat files folder
	 * @param archiveFolder
	 *            -holds the archiveFolder
	 * @param needSizeCheck
	 *            -holds true/false
	 */
	@Test
	@Parameters(value = { "AgentCSVFile", "csvMaxSizeINKB", "isArchiveNeeded",
			"maxWaitTime", "fileSizeTolerance", "flatFilesFolder",
			"archiveFolder", "needSizeCheck" })
	private boolean checkForAgentArchiveFileCommon(String AgentCSVFile,
			String csvMaxSizeINKB, String isArchiveNeeded, String maxWaitTime,
			String fileSizeTolerance, String flatFilesFolder,
			String archiveFolder, String needSizeCheck) {
		File csvFile = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder
				+ "/" + AgentCSVFile + ".csv");
		logger.info("Start of checkForAgentArchiveFileCommon");
		logger.info("Parameters are,AgentCSVFile:" + AgentCSVFile
				+ ",csvMaxSizeINKB:" + csvMaxSizeINKB + ",isArchiveNeeded:"
				+ isArchiveNeeded + ",maxWaitTime:" + maxWaitTime
				+ ",fileSizeTolerance:" + fileSizeTolerance
				+ ",flatFilesFolder:" + flatFilesFolder + ",archiveFolder:"
				+ archiveFolder + ",needSizeCheck:" + needSizeCheck);
		logger.info("ApmbaseConstants.EM_LOC +\"/\"+flatFilesFolder + \"/\"+ AgentCSVFile + \".csv\""
				+ ApmbaseConstants.EM_LOC
				+ "/"
				+ flatFilesFolder
				+ "/"
				+ AgentCSVFile + ".csv");
		boolean isArchived = false;
		boolean isNormalFile = false;
		int maxTimeOut = Integer.parseInt(maxWaitTime);
		int tolerance = Integer.parseInt(fileSizeTolerance);
		double prevFileSize = 0;
		long beforeReading = Calendar.getInstance().getTimeInMillis();
		logger.info("Time Out:" + maxTimeOut);
		if (csvFile.isFile()) {
			while (((Calendar.getInstance().getTimeInMillis() - beforeReading)
					/ minuteInMillis < maxTimeOut)
					&& (csvFile.length() / ((double) MBinKBs) >= prevFileSize)) {
				logger.info("The present file size is:" + csvFile.length()
						/ ((double) MBinKBs) + " KB");
				logger.info("Time Elapsed:"
						+ (Calendar.getInstance().getTimeInMillis() - beforeReading)
						/ minuteInMillis);
				prevFileSize = csvFile.length() / ((double) MBinKBs);
				Util.sleep(Long.parseLong("30000"));
			}
			File dir = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder
					+ "/" + archiveFolder);
			if (dir.list() != null) {
				if (checkForNormalFile(AgentCSVFile, csvMaxSizeINKB, tolerance,
						flatFilesFolder, archiveFolder, needSizeCheck)) {
					isNormalFile = true;
				}
				if (checkForArchivedFile(AgentCSVFile, flatFilesFolder,
						archiveFolder)) {
					isArchived = true;
				}
			} else {
				logger.info("There are no files present in the Directory:"
						+ ApmbaseConstants.EM_LOC + "/" + flatFilesFolder + "/"
						+ archiveFolder);
			}
		} else {
			logger.info("There is no such file");
		}
		logger.info("End of checkForAgentArchiveFileCommon");
		if (Boolean.parseBoolean(isArchiveNeeded)) {
			return (Boolean.parseBoolean(isArchiveNeeded) == isArchived);
		} else {
			return (isNormalFile);
		}
	}

	/**
	 * Test method to check the size of the given csv file and later check in
	 * the archives if required
	 * 
	 * @param AgentCSVFile
	 *            -holds the name of the csv file
	 * @param csvMaxSizeINKB
	 *            -holds the maximum size of the file
	 * @param isArchiveNeeded
	 *            -holds boolean value whether requires to check for archives or
	 *            not
	 * @param maxWaitTime
	 *            -holds the maximum size of the file
	 * @param fileSizeTolerance
	 *            -holds the tolerance value
	 * @param flatFilesFolder
	 *            -holds the flat files folder
	 * @param archiveFolder
	 *            -holds the archiveFolder
	 * @param needSizeCheck
	 *            -holds true/false
	 */
	@Test
	@Parameters(value = { "AgentCSVFile", "csvMaxSizeINKB", "isArchiveNeeded",
			"maxWaitTime", "fileSizeTolerance", "flatFilesFolder",
			"archiveFolder", "needSizeCheck" })
	public void checkForAgentArchiveFile(String AgentCSVFile,
			String csvMaxSizeINKB, String isArchiveNeeded, String maxWaitTime,
			String fileSizeTolerance, String flatFilesFolder,
			String archiveFolder, String needSizeCheck) {
		logger.info("Start of checkForAgentArchiveFile");
		logger.info("Parameters are,AgentCSVFile:" + AgentCSVFile
				+ ",csvMaxSizeINKB:" + csvMaxSizeINKB + ",isArchiveNeeded:"
				+ isArchiveNeeded + ",maxWaitTime:" + maxWaitTime
				+ ",fileSizeTolerance:" + fileSizeTolerance
				+ ",flatFilesFolder:" + flatFilesFolder + ",archiveFolder:"
				+ archiveFolder + ",needSizeCheck:" + needSizeCheck);
		logger.info("End of checkForAgentArchiveFile");
		Assert.assertTrue(checkForAgentArchiveFileCommon(AgentCSVFile,
				csvMaxSizeINKB, isArchiveNeeded, maxWaitTime,
				fileSizeTolerance, flatFilesFolder, archiveFolder,
				needSizeCheck));

	}

	/**
	 * Test method to check the size of the given csv file and later check in
	 * the archives if required
	 * 
	 * @param AgentCSVFile2
	 *            -holds the name of the csv file
	 * @param csvMaxSizeINKB2
	 *            -holds the maximum size of the file
	 * @param isArchiveNeeded2
	 *            -holds boolean value whether requires to check for archives or
	 *            not
	 * @param maxWaitTime2
	 *            -holds the maximum size of the file
	 * @param fileSizeTolerance2
	 *            -holds the tolerance value
	 * @param flatFilesFolder2
	 *            -holds the flat files folder
	 * @param archiveFolder2
	 *            -holds the archiveFolder
	 * @param needSizeCheck2
	 *            -holds true/false
	 */
	@Test
	@Parameters(value = { "AgentCSVFile2", "csvMaxSizeINKB2",
			"isArchiveNeeded2", "maxWaitTime2", "fileSizeTolerance2",
			"flatFilesFolder", "archiveFolder", "needSizeCheck2" })
	public void checkForAgentArchiveFile2(String AgentCSVFile2,
			String csvMaxSizeINKB2, String isArchiveNeeded2,
			String maxWaitTime2, String fileSizeTolerance2,
			String flatFilesFolder, String archiveFolder, String needSizeCheck2) {
		logger.info("Start of the checkForAgentArchiveFile2 method");
		logger.info("Parameters are,AgentCSVFile2:" + AgentCSVFile2
				+ ",csvMaxSizeINKB2:" + csvMaxSizeINKB2 + ",isArchiveNeeded2:"
				+ isArchiveNeeded2 + ",maxWaitTime2:" + maxWaitTime2
				+ ",fileSizeTolerance2:" + fileSizeTolerance2
				+ ",flatFilesFolder:" + flatFilesFolder + ",archiveFolder:"
				+ archiveFolder + ",needSizeCheck2:" + needSizeCheck2);
		logger.info("End of the method checkForAgentArchiveFile2");
		Assert.assertTrue(checkForAgentArchiveFileCommon(AgentCSVFile2,
				csvMaxSizeINKB2, isArchiveNeeded2, maxWaitTime2,
				fileSizeTolerance2, flatFilesFolder, archiveFolder,
				needSizeCheck2));
	}

	/**
	 * Private method to check the size of the given file and check for required
	 * size
	 * 
	 * @param AgentCSVFile
	 *            -holds the name of the csv file
	 * @param csvMaxSizeINKB
	 *            -holds the maximum size of the file
	 * @param tolerance
	 *            -holds the tolerance value
	 * @param flatFilesFolder
	 *            -holds the flat files folder
	 * @param archiveFolder
	 *            -holds the archiveFolder
	 * @param needSizeCheck
	 *            -holds true/false
	 * @return true/false
	 */
	private boolean checkForNormalFile(String AgentCSVFile,
			String csvMaxSizeINKB, int tolerance, String flatFilesFolder,
			String archiveFolder, String needSizeCheck) {
		boolean isFileFound = false;
		logger.info("Start of the method checkForNormalFile");
		logger.info("Parameters are,AgentCSVFile:" + AgentCSVFile
				+ ",csvMaxSizeINKB:" + csvMaxSizeINKB + ",tolerance:"
				+ tolerance + ",flatFilesFolder:" + flatFilesFolder
				+ ",archiveFolder:" + archiveFolder + ",needSizeCheck:"
				+ needSizeCheck);
		File dir = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder
				+ "/" + archiveFolder);
		String[] children = dir.list();
		Pattern normalPattern = Pattern.compile(AgentCSVFile + ".*" + "\\.csv");
		Matcher normalMatcher = null;
		for (int i = 0; i < children.length; i++) {
			normalMatcher = normalPattern.matcher(children[i].replaceAll("\\.",
					"\\."));
			if (normalMatcher.matches() && !Boolean.parseBoolean(needSizeCheck)) {
				isFileFound = true;
			}
			if (normalMatcher.matches()
					&& Math.abs(((new File(ApmbaseConstants.EM_LOC + "/"
							+ flatFilesFolder + "/" + archiveFolder + "/"
							+ children[i]).length() / ((double) MBinKBs)) - Integer
							.parseInt(csvMaxSizeINKB))) < tolerance) {
				logger.info("size of the file:"
						+ ApmbaseConstants.EM_LOC
						+ "/"
						+ flatFilesFolder
						+ "/"
						+ archiveFolder
						+ "/"
						+ children[i]
						+ " is:"
						+ (new File(ApmbaseConstants.EM_LOC + "/"
								+ flatFilesFolder + "/" + archiveFolder + "/"
								+ children[i]).length()) / ((double) MBinKBs)
						+ " KB,is matched with required size:" + csvMaxSizeINKB);
				isFileFound = true;
			}
		}
		logger.info("End of the method checkForNormalFile");
		return isFileFound;
	}

	/**
	 * Private method to check whether file is archived or not(zipped)
	 * 
	 * @param AgentCSVFile
	 *            -holds the name of the csv file
	 * @return true/false
	 */

	private boolean checkForArchivedFile(String AgentCSVFile,
			String flatFilesFolder, String archiveFolder) {
		logger.info("Start of the method checkForArchivedFile");
		logger.info("Parameters are,AgentCSVFile:" + AgentCSVFile
				+ ",flatFilesFolder:" + flatFilesFolder + ",archiveFolder:"
				+ archiveFolder);
		boolean isFileFound = false;
		File dir = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder
				+ "/" + archiveFolder);
		String[] children = dir.list();
		Pattern archivePattern = Pattern
				.compile(AgentCSVFile + ".*" + "\\.zip");
		Matcher archiveMatcher = null;
		for (int i = 0; i < children.length; i++) {
			archiveMatcher = archivePattern.matcher(children[i].replaceAll(
					"\\.", "\\."));
			if (archiveMatcher.matches()) {
				logger.info("FOUND the archived(zipped) file,as:" + children[i]);
				isFileFound = true;
			}
		}
		logger.info("End of the method checkForArchivedFile");
		return isFileFound;
	}

	/**
	 * Test method to change the system time
	 * 
	 * @param noOfHours
	 *            - holds the number of hours by which system time needs to be
	 *            changed
	 */
	@Test
	@Parameters(value = { "noOfHours" })
	private boolean changeTimeByHoursCommon(String noOfHours)
			throws IOException {
		boolean isTimeSet = false;
		boolean isDateSet = false;
		logger.info("Start of changeTimeByHoursCommon");
		Calendar calendar = Calendar.getInstance();
		logger.info("the present time:" + calendar);
		calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(noOfHours));
		logger.info("time after changing the hours:" + calendar);
		String AMorPM = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "AM"
				: "PM";
		String dateCmnd[] = {
				"cmd.exe",
				"/c",
				"DATE",
				new Integer(calendar.get(Calendar.MONTH) + 1) + "-"
						+ new Integer(calendar.get(Calendar.DATE)) + "-"
						+ new Integer(calendar.get(Calendar.YEAR)) };
		String timeCmnd[] = {
				"cmd.exe",
				"/c",
				"TIME",
				new Integer(calendar.get(Calendar.HOUR)) + ":"
						+ new Integer(calendar.get(Calendar.MINUTE)) + ":"
						+ new Integer(calendar.get(Calendar.SECOND)), AMorPM };
		logger.info("dateCmnd:" + "DATE"
				+ new Integer(calendar.get(Calendar.MONTH)) + "-"
				+ new Integer(calendar.get(Calendar.DATE)) + "-"
				+ new Integer(calendar.get(Calendar.YEAR)));
		logger.info("timeCmnd:" + "TIME"
				+ new Integer(calendar.get(Calendar.HOUR)) + ":"
				+ new Integer(calendar.get(Calendar.MINUTE)) + ":"
				+ new Integer(calendar.get(Calendar.SECOND)) + " " + AMorPM);
		ProcessBuilder processBuilder = new ProcessBuilder(dateCmnd);
		Process process = processBuilder.start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		if (br.readLine() == null) {
			isDateSet = true;
			br.close();
			if (process != null) {
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		}
		processBuilder = new ProcessBuilder(timeCmnd);
		process = processBuilder.start();
		is = process.getInputStream();
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		if (br.readLine() == null) {
			isTimeSet = true;
			br.close();
			if (process != null) {
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		}
		logger.info("End of changeTimeByHoursCommon");
		return (isTimeSet && isDateSet);
	}

	/**
	 * Test method to change the system time
	 * 
	 * @param noOfHours
	 *            - holds the number of hours by which system time needs to be
	 *            changed
	 */
	@Test
	@Parameters(value = { "noOfHours" })
	public void changeTimeByHours(String noOfHours) throws IOException {
		logger.info("Start of changeTimeByHours");
		logger.info("End of changeTimeByHours");
		Assert.assertTrue(changeTimeByHoursCommon(noOfHours));
	}

	/**
	 * Test method to change the system time
	 * 
	 * @param noOfHours
	 *            - holds the number of hours by which system time needs to be
	 *            changed
	 */
	@Test
	@Parameters(value = { "noOfHours" })
	public void changeTimeByHours2(String noOfHours) throws IOException {
		logger.info("Start of changeTimeByHours2");
		logger.info("End of changeTimeByHours2");
		Assert.assertTrue(changeTimeByHoursCommon(noOfHours));
	}

	/**
	 * Private method method is get the frequency of metrics
	 * 
	 * @param flatFilesFolder
	 *            - holds name of the flat file folder
	 * @param csvFile
	 *            - holds name of csv file
	 * @param noOfSeconds
	 *            - holds expected number of seconds
	 * @throws ParseException
	 */

	private boolean checkMetricFrequencyCommon(String flatFilesFolder,
			String csvFile, String noOfSeconds, String patternForPeriod)
			throws ParseException {
		logger.info("Start of checkMetricFrequencyCommon");
		logger.info("Parameters are,flatFilesFolder:" + flatFilesFolder
				+ ",csvFile:" + csvFile + ",noOfSeconds:" + noOfSeconds
				+ ",patternForPeriod:" + patternForPeriod);
		String valueFrmFile = "";
		try {
			Pattern p = Pattern.compile(patternForPeriod);
			File f = new File(ApmbaseConstants.EM_LOC + "/" + flatFilesFolder
					+ "/" + csvFile + ".csv");
			logger.info("The agent csv file is " + ApmbaseConstants.EM_LOC
					+ "/" + flatFilesFolder + "/" + csvFile + ".csv");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			while ((line = br.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					logger.info("Found m.group(0): " + m.group(0));
					valueFrmFile = m.group(0).split(",")[1];
					logger.info("valueFrmFile: " + valueFrmFile);
					break;
				} else {
					logger.info("NO MATCH");
				}
			}

			br.close();
		} catch (IOException e) {
			logger.info("Exception caught due to:" + e.getMessage());

		}
		if (valueFrmFile.contains(noOfSeconds)) {
			logger.info("The Timestamps are same the expected Frequency");
		}
		logger.info("End of checkMetricFrequencyCommon");
		return (valueFrmFile.contains(noOfSeconds));
	}

	/**
	 * Test method is get the frequency of metrics
	 * 
	 * @param flatFilesFolder
	 *            - holds name of the flat file folder
	 * @param csvFile
	 *            - holds name of csv file
	 * @param noOfSeconds
	 *            - holds expected number of seconds
	 * @throws ParseException
	 */
	@Test
	@Parameters(value = { "flatFilesFolder", "csvFile", "noOfSeconds",
			"patternForPeriod" })
	public void checkMetricFrequency(String flatFilesFolder, String csvFile,
			String noOfSeconds, String patternForPeriod) throws ParseException {
		logger.info("Start of checkMetricFrequency");
		logger.info("Parameters are,flatFilesFolder:" + flatFilesFolder
				+ ",csvFile:" + csvFile + ",noOfSeconds:" + noOfSeconds
				+ ",patternForPeriod:" + patternForPeriod);
		logger.info("End of checkMetricFrequency");
		Assert.assertTrue(checkMetricFrequencyCommon(flatFilesFolder, csvFile,
				noOfSeconds, patternForPeriod));
	}

	/**
	 * Test method is get the frequency of metrics
	 * 
	 * @param flatFilesFolder2
	 *            - holds name of the flat file folder
	 * @param csvFile2
	 *            - holds name of csv file
	 * @param noOfSeconds2
	 *            - holds expected number of seconds
	 * @throws ParseException
	 */
	@Test
	@Parameters(value = { "flatFilesFolder", "csvFile2", "noOfSeconds2",
			"patternForPeriod" })
	public void checkMetricFrequency2(String flatFilesFolder, String csvFile2,
			String noOfSeconds2, String patternForPeriod) throws ParseException {
		logger.info("Start of checkMetricFrequency2");
		logger.info("Parameters are,flatFilesFolder:" + flatFilesFolder
				+ ",csvFile2:" + csvFile2 + ",noOfSeconds2:" + noOfSeconds2
				+ ",patternForPeriod:" + patternForPeriod);
		logger.info("End of checkMetricFrequency2");
		Assert.assertTrue(checkMetricFrequencyCommon(flatFilesFolder, csvFile2,
				noOfSeconds2, patternForPeriod));
	}

	/**
	 * This method is used to check permissions
	 * 
	 * @param emHostName
	 *            - holds name of emhost
	 * @param userName
	 *            - holds name of emhost
	 * @param password
	 *            - holds password
	 * @param emport
	 *            - holds port number
	 * @param directoryLocation
	 *            - holds location of the jar file
	 * @param message
	 *            - holds message
	 * @param query
	 *            - holds the qeury to be executed
	 */
	@Test
	@Parameters(value = { "emHostName", "userName", "password", "emport",
			"directoryLocation", "message", "query" })
	public void checkPermissions(String emHostName, String userName,
			String password, String emport, String directoryLocation,
			String message, String query) {
		logger.info("Start of checkPermissions");
		logger.info("Parameters emHostName:" + emHostName + "userName:"
				+ userName + "password" + password + "emport" + emport
				+ "directoryLocation:" + directoryLocation + "message:"
				+ message + "query:" + query);
		BufferedReader reader = null;
		Process process = null;
		boolean messageFound = false;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("java -Xmx512M").append(" ").append(" ")
					.append("-Duser=").append(userName).append(" ")
					.append("-Dpassword=").append(password).append(" ")
					.append("-Dhost=").append(emHostName).append(" ")
					.append("-Dport=").append(emport).append(" -jar ")
					.append(clwJarFileLoc).append(" ").append(query);

			String command = sb.toString();

			logger.info("command to be executed :: " + command);
			String[] startCmnd = { command };
			process = Util.getProcess(startCmnd, directoryLocation);
			reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				logger.info("console :: " + line);
				if (line.toLowerCase().contains(message.toLowerCase())) {
					logger.info("COMMAND EXECUTED SUCESSFULLY");
					messageFound = true;
					break;
				}
			}
			if (messageFound) {
				logger.info("Message found is successful");
			} else {
				logger.info("Unable to find message");
			}
			logger.info("End of checkPermissions");
			Assert.assertTrue(messageFound);
		} catch (Exception e) {
			logger.info("Unable to execute the command:" + e.getMessage());
			Assert.fail("failed due to " + e.getMessage());
		} finally {

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.info("Failed due to " + e.getMessage());
					Assert.fail("Method failed " + e.getMessage());
				}
			}
			if (process != null) {
				try {
					process.getErrorStream().close();
					process.getInputStream().close();
					process.getOutputStream().close();
					process.destroy();
				} catch (IOException e) {
					logger.info("Failed due to " + e.getMessage());
					Assert.fail("Method failed due to " + e.getMessage());
				}
			}
		}

	}

	/**
	 * Test method to check if file exists with specified extension
	 * 
	 * @param fileLoc
	 *            - location of folder
	 * @param fileExtension
	 *            - file extension to be verified
	 */
	@Test
	@Parameters(value = { "fileLoc", "fileExtension" })
	public void checkFileExtn(String fileLoc, String fileExtension) {
		logger.info("Start of checkFileExtn");
		File folderpath = new File(fileLoc);
		String[] filesExt = fileExtension.split("~");
		int count = 0;
		boolean isExists;
		if (folderpath.exists()) {
			for (int j = 0; j < filesExt.length; j++) {
				File[] listOfFiles = folderpath.listFiles();
				isExists = false;
				for (int i = 0; i < listOfFiles.length; i++) {

					logger.info("i:" + i + "file[i]:" + listOfFiles[i]);
					String filenames = listOfFiles[i].getName();
					if (filenames.contains(filesExt[j])) {
						isExists = true;
						logger.info("filenames:" + filenames);
						logger.info("File Extension:" + filesExt[j]);
						logger.info("count:" + count);
					}
				}
				if (isExists) {
					count++;
				}
			}
			logger.info("filesExt.length" + filesExt.length);
			logger.info("count:" + count);
			logger.info("End of checkFileExtn");
			Assert.assertEquals(count, filesExt.length);
		}
	}

	/**
	 * Private method to retrieve port number
	 * 
	 * @return String -Holds the port number
	 * @throws IOException
	 */
	private String getUsedPort(String emHost) throws IOException {
		logger.info("start of getPort method");
		String finalCmnd[] = { "cmd.exe", "/c", "netstat" };
		ProcessBuilder processBuilder = new ProcessBuilder(finalCmnd);
		processBuilder.redirectErrorStream(true);
		Process process = null;
		BufferedReader br = null;
		String line = "";
		String port = "";
		int colonIndex = 0;
		int i=1;
		try {
			process = processBuilder.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			while ((line = br.readLine()) != null) {
				
				logger.info("<<<<<<<<The Line>>>>>>>:" + line);
				colonIndex = line.indexOf(":");
				if(line.contains(emHost))
				if (colonIndex > 0) {
					while(!Character.isWhitespace(line.charAt(colonIndex+i)))
					{
						System.out.println(line.charAt(colonIndex+i));
						i++;
					}	
					port = line.substring(colonIndex + 1, colonIndex + i);
					System.out.println(port);
					if (port != ""&& (port.charAt(0) <= '9' && port.charAt(0) >= '0')
							&& !port.equals("8080") &&  !port.equals("135")) {
						logger.info("port got by running netstat command is:"
								+ port);
								
						break;

					}
				}
			}

		} catch (IOException e) {
			logger.info("Unable to get port number due to :" + e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
				br.close();

			}

		}
		logger.info("getPort Method Ended");
		return port;

	}

	/***
     * 
     * 
     * @param pwdProperty
     * 			-The database password property for which we assign value for
     * 
     * @param plainPwd
     * 			-The password set as plain for the password property.
     * 
     * @param plainTextPWDProp
     * 			-The property to set the value for,whether the password is plain or not.
     * 
     * @param plainTextPWDVal
     * 			-The value of the above property,true/false
     */
    @Test
    @Parameters(value = { "pwdProperty","plainPwd","plainTextPWDProp","plainTextPWDVal" })
    public void checkIfPwdEncripted(String pwdProperty,String plainPwd,String plainTextPWDProp,String plainTextPWDVal)
    {  
    	logger.info("Start of the method checkIfPwdEncripted");
    	logger.info("Parameters are,pwdProperty:" + pwdProperty+",plainPwd"+plainPwd);
    	boolean isPwdEncrypted = false;
        try
        {
        	Properties properties = Util.loadPropertiesFile(propertiesFilePath);
        	if (!((String) properties.get(pwdProperty)).equalsIgnoreCase(plainPwd) && ((String) properties.get(plainTextPWDProp)).equalsIgnoreCase(plainTextPWDVal)) {
        		isPwdEncrypted = true;
        	}
        } catch (Exception ex)
        {
            logger.error("caught exception due to:" + ex.getMessage());
        }
        logger.info("End of the method checkIfPwdEncripted");
        Assert.assertTrue(isPwdEncrypted);
    }

	/***
	 * PrerequistieCopy the classes12.jar
	 * 
	 * @param srcFileNameandPath
	 *            -source path for copying the jar
	 * @param destFileNameandPath
	 *            -destination path for copying the jar
	 * @param oldLine
	 *            -Old line which needs to be replaced
	 * @param newLine
	 *            -New line with which old line is to be replaced
	 * @param emLaxFileNameandPath
	 *            -EM lax file path and location
	 */
	@Test
	@Parameters(value = { "srcFileNameandPath", "destFileNameandPath",
			"oldLine", "newLine", "emLaxFileNameandPath" })
	public void setStart(String srcFileNameandPath, String destFileNameandPath,
			String oldLine, String newLine, String emLaxFileNameandPath) {
		try {
			logger.info("Inside of  setStart method");
			boolean isFileCopied = ApmbaseUtil.copyFile(srcFileNameandPath,
					destFileNameandPath);

			boolean isLineReplaced = Util.replaceLine(emLaxFileNameandPath,
					oldLine, newLine);

			logger.info("isFileCopied ***" + isFileCopied);
			logger.info("isLineReplaced ***" + isLineReplaced);

			boolean isLineandFileCopied = false;
			if ((isFileCopied) && (isLineReplaced)) {
				isLineandFileCopied = true;
			}

			Assert.assertTrue(isLineandFileCopied);

		} catch (Exception ex) {
			logger.error("An Exception due to:::::" + ex.getMessage());

			Assert.fail("An Exception due to:::::" + ex.getMessage());
		}
		logger.info("outside of  setStart method");
	}

	/***
	 * Test method to create Tables
	 * 
	 * @param scripts
	 *            -Scripts that are to be run for table creation
	 * @param getTableCreatedInfo
	 *            -This is used to get the table information
	 * @param expectedValue
	 *            -Expected value used for asserting
	 */

    @Test
    @Parameters(value = { "scripts", "getTableCreatedInfo", "expectedValue" })
    public void createTables(String scripts,
                             String getTableCreatedInfo,
                             String expectedValue)
    {
        Statement stmt = null;
        Connection conn = null;
        ResultSet rset = null;
        int recCount = 0;
        try
        {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(scripts);

        } catch (Exception ex)
        {
            logger.error("Exception:*******" + ex.getMessage());
        } finally
        {
            try
            {
                logger.info("dbdriverinfo ***" + dbdriverinfo);
                try
                {
                    Class.forName(dbdriverinfo);
                } catch (ClassNotFoundException e)
                {
                    // TODO Auto-generated catch block
                   logger.error("This method failed because of"+e.getMessage());
                }

                conn = DriverManager.getConnection(dbconString, dbuser, dbpwd);
                logger.info("dbconString ***" + dbconString);
                logger.info("dbuser ***" + dbuser);
                logger.info("dbpwd ***" + dbpwd);
                logger.info("getTableCreatedInfo ***" + getTableCreatedInfo);

                stmt = conn.createStatement();
                rset = stmt.executeQuery(getTableCreatedInfo);

                while (rset.next())

                {
                    recCount = Integer.parseInt(rset.getString(1));

                }
                Assert.assertEquals(recCount, Integer.parseInt(expectedValue));
                stmt.close();
                conn.close();
                rset.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

	/***
	 * Test method to create User
	 * 
	 * @param createUserQueries
	 *            -Queries that are used to create users
	 * @param selectUserQuery
	 *            -SQL queries
	 */
	@Test
	@Parameters(value = { "createUserQueries", "selectUserQuery" })
	public void createUser(String createUserQueries, String selectUserQuery) {
		Statement stmt = null;
		Connection conn = null;
		ResultSet rset = null;
		int recCount = 0;
		try {
			logger.info("Inside of  createUser method");
			logger.info("dbdriverinfo ***" + dbdriverinfo);
			Class.forName(dbdriverinfo);

			conn = DriverManager.getConnection(dbconString, dbsystemuser,
					dbsystempwd);
			logger.info("dbconString ***" + dbconString);
			logger.info("dbsystemuser ***" + dbsystemuser);
			logger.info("dbsystempwd ***" + dbsystempwd);
			logger.info("createUserQueries ***" + createUserQueries);

			stmt = conn.createStatement();
			rset = stmt.executeQuery(selectUserQuery);

			while (rset.next())

			{
				recCount = Integer.parseInt(rset.getString(1));

			}
			logger.info("recCount Before User Creation ****" + recCount);
			if (recCount == 0) {
				String splittedQueries[] = createUserQueries.split("~");
				for (String i : splittedQueries) {
					stmt.executeQuery(i);
				}
			}
			rset = stmt.executeQuery(selectUserQuery);

			while (rset.next())

			{
				recCount = Integer.parseInt(rset.getString(1));

			}
			logger.info("recCount After User Creation ****" + recCount);
			Assert.assertTrue(recCount > 0);

		} catch (Exception ex) {
			logger.error("Exception:*******" + ex.getMessage());
			Assert.assertTrue(recCount > 0);
		} finally {
			try {
				stmt.close();
				conn.close();
				rset.close();
				Assert.assertTrue(recCount > 0);
			} catch (SQLException e) {
				logger.error("This method failed because of::" + e.getMessage());
				Assert.assertTrue(recCount > 0);
			}
		}
		logger.info("Outside of  createUser method");
	}

	/**
	 * Test method to revert the Introscope Properties file
	 * 
	 * @param emLaxFilePath
	 *            -EM lax file path
	 */
	@Test
	@Parameters(value = { "emLaxFilePath" })
	public void revertEMLaxFile(String emLaxFilePath) {
		logger.info("IN   revertEMLaxFile method");
		try {
			logger.info("propertyFilePath is ::" + emLaxFilePath);
			String message = ApmbaseUtil.revertFile(emLaxFilePath);
			logger.info("Reverted   files with status" + message);
			Assert.assertEquals(message, ApmbaseConstants.SUCCESS_MESSAGE);
		} catch (Exception e) {
			logger.error("The mesage failed because" + e.getMessage());
			Assert.fail("Unable to Execute the command " + e.getMessage());
		}
		logger.info("out of  revertEMLaxFile method");
	}

	/**
	 * Test method to revert the Introscope Properties file
	 * 
	 * @param emLaxFilePath
	 *            -EM lax file path
	 */
	@Test
	@Parameters(value = { "emLaxFilePath" })
	public void backupEMLaxFile(String emLaxFilePath) {
		logger.info("IN   backupEMLaxFile method");
		try {
			logger.info("propertyFilePath is ::" + emLaxFilePath);
			String message = ApmbaseUtil.fileBackUp(emLaxFilePath);
			logger.info("Reverted   files with status" + message);
			Assert.assertEquals(message, ApmbaseConstants.SUCCESS_MESSAGE);
		} catch (Exception e) {
			logger.error("The mesage failed because" + e.getMessage());
			Assert.fail("Unable to Execute the command " + e.getMessage());
		}
		logger.info("out of  backupEMLaxFile method");
	}

	/**
	 * This method is using the delete the data from database
	 */
	@Test
	public void deleteDatafromDatabase() {
		int recCount = 0;
		Connection conn = null;
		Statement stmt = null;
		try {
			logger.info("dbdriverinfo ***" + dbdriverinfo);
			Class.forName(dbdriverinfo);

			conn = DriverManager.getConnection(dbconString, dbuser, dbpwd);
			logger.info("dbconString ***" + dbconString);
			logger.info("dbuser ***" + dbuser);
			logger.info("dbpwd ***" + dbpwd);
			logger.info("deleteQueries ***" + deleteQueries);

			stmt = conn.createStatement();
			String splittedQueries[] = deleteQueries.split("~");
			for (String i : splittedQueries) {
				stmt.executeQuery(i);
			}

			conn.commit();

		} catch (Exception ex) {
			logger.error("Exception:*******" + ex.getMessage());
		} finally {
			try {
				stmt.close();
				conn.close();

			} catch (SQLException e) {

				logger.error("SQL Exception:*******" + e.getMessage());
				Assert.fail("Method failed due to :" + e);
			}
		}

	}

	/**
	 * Test method to append properties to a particular file
	 * 
	 * @param property
	 *            -Properties that are to be appended to a particular file
	 */
	@Test
	@Parameters(value = { "property" })
	public void addPropTofile(String property) {
		logger.info("Start of addPropTofile method");
		logger.info("addPropToProfile uses property with value :" + property);

		logger.info("Appending properties");
		boolean status = appendLines(property, propertiesFilePath);
		if (!status)
			logger.info("Unable to append lines");
		else
			logger.info("Lines appended successfully");
		logger.info("End of addPropTofile method");
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
	@Test
	@Parameters(value = { "srcFile" })
	public void copyFileStart(String srcFile)throws Exception {
		logger.info("Start copyFile");
		String filePath = ApmbaseConstants.EM_LOC + "/" + srcFile;
		String dest = ApmbaseConstants.EM_LOC + "/config/IntroscopeEnterpriseManager.properties";
		ApmbaseUtil.copy(filePath, dest);
		}
		
	@Test
	@Parameters(value = { "module1" })
	public void deleteFileStart1(String module1)throws Exception {
		logger.info("Start delete file");
		Assert.assertTrue(ApmbaseUtil.deleteFile(module1));
		}	
		
	@Test
	@Parameters(value = { "module2" })
	public void deleteFileStart2(String module2)throws Exception {
		logger.info("Start delete file");
		Assert.assertTrue(ApmbaseUtil.deleteFile(module2));
		}	
	
	
	@Test(groups = {"EMPropertiesEMLax", "bat"})
	public void verify_ALM_359211_Enable_Disable_Hot_Config()
	{
		String testCaseNameIDPath = "Enable or Disable Hot Config(Test ID-359211)";
		String fileNames = "config/IntroscopeEnterpriseManager.properties";
		String setProperties1 = "introscope.enterprisemanager.hotconfig.enable~introscope.enterprisemanager.hotconfig.pollingInterval~log4j.logger.Manager";
		String setValues1 = "true~60~DEBUG, console, logfile";
		String expected = "1";
		
		
		testCaseStart(testCaseNameIDPath);
		
		stopEM(emRoleId);
		backupFile(fileNames);
		
		//setProperties1  is used to set  the properties in  Introscope Enterprise  manager properties files					
		setProperties1(setProperties1, setValues1, expected);
		
		//To start the EM
		startEM(emRoleId);
		
		//checkLogMessage1 is used to check for the given property in the EM log file
		String textmsg1 = "Started hot config polling.  Polling interval set to 60 seconds~Checking for hot config changes to " + emPath + "\\.\\config\\IntroscopeEnterpriseManager.properties";
		String filenames = "/IntroscopeEnterpriseManager.log";
		checkLogMessage1(filenames, textmsg1);
		
		//setProperties2 is used to set  the properties in  Introscope Enterprise  manager properties files
		String setProperties2 = "log4j.appender.logfile.File";
		String setValues2 = "logs/change1IntroscopeEnterpriseManager.log";					
		setProperties2(setProperties2, setValues2, expected);
		
		String minutes = "5";					
		sleepTime(minutes);;
		
		//checkLogFileExist is used to check if the given files exists
		String fileName = "change1IntroscopeEnterpriseManager.log";					
		checkLogFileExist(fileName);
		
		//setProperties3 is used to sets  the properties in  Introscope Enterprise  manager properties files
		String setProperties3 = "introscope.enterprisemanager.hotconfig.enable";
		String setValues3 = "false";					
		setProperties3(setProperties3, setValues3, expected);;
		
		sleepTime2(minutes);
		
		//checkLogMessage2 is used to check for the given property in the EM log file
		String filenames2 = "/change1IntroscopeEnterpriseManager.log";
		String textmsg2 = "Detected hot config change to " + emPath+ "\\.\\config\\IntroscopeEnterpriseManager.properties~stopped hot config polling";
		checkLogMessage2(filenames2, textmsg2);
		
		//setProperties4 is used to sets  the properties in  Introscope Enterprise  manager properties files
		String setProperties4 = "log4j.appender.logfile.File";
		String setValues4 = "logs/change2IntroscopeEnterpriseManager.log";					
		setProperties4(setProperties4, setValues4, expected);
		
		sleepTime3(minutes);		
		//checkLogFileExist is used to check if the given files exists
		String fileNames2 = "change2IntroscopeEnterpriseManager.log";					
		checkLogFileNotExist(fileNames2);
		
		//stopEM is used to stop the EM
		stopEM(emRoleId);
		
		//revertFile is used to revert the given files
		revertFile(fileNames);
		
		startEM(emRoleId);
		//testCaseEnd is the end of the test case
        testCaseEnd(testCaseNameIDPath);		
	}
	
	
	@Test(groups = {"EMPropertiesEMLax", "bat"})
	public void verify_ALM_359216_Dedicated_Controller_Smartstor()
	{
		stopEM(emRoleId);
		//testCaseStart is used to parameterize the testplan path of module
		String testCaseNameIDPath = "Dedicated controller for smartstor";
		testCaseStart(testCaseNameIDPath);
					
		//backupFile is used to backup the Introscope Enterprise  manager properties files
		String fileNames = "config/IntroscopeEnterpriseManager.properties";
		backupFile(fileNames);
					
		//setProperties1  is used to set  the properties in  Introscope Enterprise  manager properties files
		String setProperties1 = "introscope.enterprisemanager.smartstor.directory~introscope.enterprisemanager.smartstor.directory.archive~introscope.enterprisemanager.smartstor.dedicatedcontroller";
		String setValues1 = "C:/data~C:/data/archive~true";
		String expected ="1";
		setProperties1(setProperties1, setValues1, expected);

		//To start the EM
		startEM(emRoleId);

		//sleepTime  is used to for delay time
		String minutes = "5";				  
		sleepTime(minutes);
						  
		//stopEM is used to stop the EM-->
		stopEM(emRoleId);

		//To start the EM
		startEM(emRoleId);

		//checkFileExtn is used to check if file with particular extension exists or not
		String fileLoc = "C:/data";
		String fileExtension = ".spool~.data";	
		checkFileExtn(fileLoc, fileExtension);

		stopEM(emRoleId);

		//revertFile is used to revert the given files
		revertFile(fileNames);

		startEM(emRoleId);
		//testCaseEnd is the end of the test case
		testCaseEnd(testCaseNameIDPath);		
	}
	
	
	@Test(groups = {"EMPropertiesEMLax", "bat"})
	public void verify_ALM_392282_Default_Enterprise_Manager_Name()
	{
		String testCaseNameIDPath = "Default Enterprise Manager Name_392282";
		String metric = "*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager:Name";
		String expMtrcVal = "Introscope Enterprise Manager";
		
		testCaseStart(testCaseNameIDPath);
		checkMetricValue(metric, expMtrcVal);
		testCaseEnd(testCaseNameIDPath);
	}
	
	
	
}
