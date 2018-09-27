package com.ca.apm.commons.coda.common;

import com.ca.apm.tests.common.Context;
import com.ca.apm.tests.common.IResponse;
import com.ca.apm.tests.common.io.ProcessUtil;
import com.ca.apm.tests.common.tasks.Task;
import com.ca.apm.tests.common.tasks.file.CopyTask;
import com.ca.apm.tests.common.tasks.file.ExistsTask;
import com.wily.introscope.jdbc.IntroscopeDriver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;


public class Util {
	private static Logger LOGGER = Logger.getLogger(Util.class);

	private static final int TOMCAT_PORT = 8181;
	
	private final static String platform = System.getProperty("os.name").toUpperCase();
	
	private static final String[] PROCESS_STARTUP_CONFIRM_MESSAGES = {
	        AutomationConstants.WEBSPHERE_START_MESSAGE,                                                             
			AutomationConstants.START_WEBLOGIC_PORTAL_MESSAGE,
			AutomationConstants.WLS_STARTING_MESSAGE_WINDOWS,
			AutomationConstants.JBOSS_AGENT_STARTUP_MESSAGE,
			AutomationConstants.TOMCAT_START_MESSAGE, "Using CATALINA_HOME:" };

	private static final String[] PROCESS_STOP_CONFIRM_MESSAGES = {
		AutomationConstants.STOP_WEBLOGIC_PORTAL_MESSAGE, 
		AutomationConstants.WLS_STOP_MESSAGE,
		"Exiting WebLogic Scripting Tool",
		"Using CATALINA_HOME:"
	};

	/**
	 * The method validates if the given list of strings are displayed on the console for the given command
	 * Each of the strings are verified in every line, removing the found strings.
	 * @param commands - Command to be executed
	 * @param dirLoc - Command to be executed from
	 * @param compareStrings - List of Strings to be compared
	 * @return boolean indicating if the given strings are found
	 * @throws IOException
	 */
	public static boolean validateCommandOutput(String[] commands, String dirLoc, List<String> compareStrings) throws IOException{
		
		boolean found = false ;
		BufferedReader reader = null;
		Process process = null;  
		process = ApmbaseUtil.getProcess(commands, dirLoc);
		
		if(process == null) return found;
		try{
		reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		if(compareStrings.size() > 0){
			while ((line = reader.readLine()) != null) {
				
				for (String str : compareStrings) {
					if(line.contains(str)){
					 compareStrings.remove(str);
					 }
				}
				System.out.println("Verified console line is " + line);
				System.out.println("Strings to be verified is " + compareStrings);
			}	
			if(compareStrings.size() == 0) found = true;
		}else{
			LOGGER.info("No strings to compare the console output");
		}
		
		}finally{
			if(reader!=null) reader.close();
			process.destroy();
		}
		return found;
	}
	
	public static StringBuffer invokeProcessBuilder(List<String> args)
			throws Exception {
		return ProcessUtil.invokeProcessBuilder(args, PROCESS_STARTUP_CONFIRM_MESSAGES, true, false, true, null);
	}

	public static StringBuffer invokeProcessBuilder(List<String> args, String[] confirmationMessages, boolean wait, boolean failOnTimeout, 
			boolean redirectErrorStream, File workDir)
			throws Exception {
		return ProcessUtil.invokeProcessBuilder(args, confirmationMessages, wait, failOnTimeout, redirectErrorStream, workDir);
	}

	public static void invokeProcessBuilderNoWait(List<String> args)
			throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.invokeProcessBuilderNoWait(List<String> args)");
		LOGGER.info("args: " + args);

		ProcessBuilder pb = new ProcessBuilder(args);
		Map<String, String> env = pb.environment();
		env.put(ProcessUtil.BUILD_ID_ENV_PROPERTY_NAME, ProcessUtil.BUILD_ID_ENV_PROPERTY_VALUE);

		try {
			pb.start();
		} catch (IOException e) {
			LOGGER.error("Failed to start process: ", e);
		} finally {
			LOGGER.info("Leaving com.apm.automation.Util.invokeProcessBuilderNoWait(List<String> args)");
		}
	}

	public static StringBuffer runCLW(String command, String clwInstallHome,
			String emHost, String outFile) throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.runCLW(String command, String clwInstallHome, String emHost, String outFile)");
		LOGGER.info("command: " + command);
		LOGGER.info("clwInstallHome: " + clwInstallHome);
		LOGGER.info("emHost: " + emHost);
		LOGGER.info("outFile: " + outFile);

		String clWorkstationJarFileLocation = clwInstallHome
				+ "/lib/CLWorkstation.jar";
		List<String> args = new ArrayList<String>();
		args.add("java");
		args.add("-jar");
		args.add("-Dtxn.file=" + outFile);
		args.add("-Dhost=" + emHost);
		args.add(clWorkstationJarFileLocation);
		args.add(command);
		StringBuffer result = ProcessUtil.invokeProcessBuilder(args, null, true, false, true, null);
		LOGGER.info("Leaving com.apm.automation.Util.runCLW(String command, String clwInstallHome, String emHost, String outFile)");
		return result;
	}

	public static void runPO(String poJarFileLocation, String command,
			String appserverHost, String appserverPort, Boolean isPOWait)
			throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.runPO(String poJarFileLocation, String command, String appserverHost, String appserverPort, Boolean isPOWait)");
		LOGGER.info("poJarFileLocation: " + poJarFileLocation);
		LOGGER.info("command: " + command);
		LOGGER.info("appserverHost: " + appserverHost);
		LOGGER.info("appserverPort: " + appserverPort);
		LOGGER.info("isPOWait: " + isPOWait);

		List<String> args = new ArrayList<String>();
		args.add("java");
		args.add("-jar");
		args.add("-Dpipeorgan.urlfetcher.host=" + appserverHost);
		args.add("-Dpipeorgan.sessionid.host=" + appserverHost);
		args.add("-Dpipeorgan.urlfetcher.port=" + appserverPort);
		args.add("-Dpipeorgan.sessionid.port=" + appserverPort);
		args.add(poJarFileLocation);
		args.add(command);

		LOGGER.info("[runPO] Pipeorgan command: " + args.toString());
		LOGGER.info("[runPO] Pipeorgan args: ");
		LOGGER.info("[runPO]     appserverHost: " + appserverHost);
		LOGGER.info("[runPO]     appserverPort: " + appserverPort);
		LOGGER.info("[runPO]     poJarFileLocation: " + poJarFileLocation);
		LOGGER.info("[runPO]     script: " + command);

		if (isPOWait) {
			ProcessUtil.invokeProcessBuilder(args, null, true, false, true, null);
		} else {
			invokeProcessBuilderNoWait(args);
		}
		LOGGER.info("Leaving com.apm.automation.Util.runPO(String poJarFileLocation, String command, String appserverHost, String appserverPort, Boolean isPOWait)");
	}

	public static void startAgentTomcatWindows(String appserverType,
			String appServerHome, String serverName) throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.startAgentTomcatWindows(String appserverType, String appServerHome, String serverName)");
		LOGGER.info("appserverType: " + appserverType);
		LOGGER.info("appServerHome: " + appServerHome);
		LOGGER.info("serverName: " + serverName);

		startAgentWindows(appserverType, appServerHome, serverName);

		LOGGER.info("Leaving com.apm.automation.Util.startAgentTomcatWindows(String appserverType, String appServerHome, String serverName)");
	}

	public static StringBuffer startAgentWindows(String appserverType,
			String appServerHome, String serverName) throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.startAgentWindows(String appserverType, String appServerHome, String serverName)");
		LOGGER.info("appserverType: " + appserverType);
		LOGGER.info("appServerHome: " + appServerHome);
		LOGGER.info("serverName: " + serverName);

		List<String> args = new ArrayList<String>();
		Map<String, String> envProps = new HashMap<String, String>();
		envProps.put(ProcessUtil.BUILD_ID_ENV_PROPERTY_NAME, ProcessUtil.BUILD_ID_ENV_PROPERTY_VALUE);
		if (appserverType.equalsIgnoreCase("websphere")) {
			args.add(appServerHome + "/AppServer/bin/"
					+ AutomationConstants.WAS_STARTUP_SCRIPT_WINDOWS);
			args.add(serverName);
		} else if (appserverType.equalsIgnoreCase("weblogic")) {
			args.add(appServerHome + "/bin/"
					+ AutomationConstants.WLS_STARTUP_SCRIPT_WINDOWS);
		} else if (appserverType.equalsIgnoreCase("jboss")) {
			args.add(appServerHome
					+ AutomationConstants.JBOSS_STARTUP_SCRIPT_WINDOWS);
			args.add(serverName);
		} else if (appserverType.equalsIgnoreCase("tomcat")) {
			envProps.put(AutomationConstants.CATALINA_HOME_ENV_PROPERTY,
					appServerHome);
			args.add(appServerHome
					+ AutomationConstants.TOMCAT_STARTUP_SCRIPT_PATH_WINDOWS);
		}

		StringBuffer result = ProcessUtil.invokeProcessBuilder(args, envProps, PROCESS_STARTUP_CONFIRM_MESSAGES, true, true, true, null);
		LOGGER.info("Leaving com.apm.automation.Util.startAgentWindows(String appserverType, String appServerHome, String serverName)");
		return result;
	}

	public static StringBuffer stopAgentWindows(String appserverType,
			String appServerHome, String serverName) throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.stopAgentWindows(String appserverType, String appServerHome, String serverName)");
		LOGGER.info("appserverType: " + appserverType);
		LOGGER.info("appServerHome: " + appServerHome);
		LOGGER.info("serverName: " + serverName);

		List<String> args = new ArrayList<String>();
		Map<String, String> envProps = new HashMap<String, String>();
		envProps.put(ProcessUtil.BUILD_ID_ENV_PROPERTY_NAME, ProcessUtil.BUILD_ID_ENV_PROPERTY_VALUE);

		if (appserverType.equalsIgnoreCase("websphere")) {

			args.add(appServerHome + "/AppServer/bin/"
					+ AutomationConstants.WAS_STOP_SCRIPT_WINDOWS);
			args.add(serverName);
		} else if (appserverType.equalsIgnoreCase("weblogic")) {
			args.add(appServerHome + "/bin/"
					+ AutomationConstants.WLS_STOP_SCRIPT_WINDOWS);
		} else if (appserverType.equalsIgnoreCase("tomcat")) {
			envProps.put(AutomationConstants.CATALINA_HOME_ENV_PROPERTY,
					appServerHome);
			args.add(appServerHome + AutomationConstants.TOMCAT_STOP_SCRIPT_PATH_WINDOWS);
		}

		StringBuffer result = ProcessUtil.invokeProcessBuilder(args, envProps, PROCESS_STOP_CONFIRM_MESSAGES, true, false, true, null);
		LOGGER.info("Leaving com.apm.automation.Util.stopAgentWindows(String appserverType, String appServerHome, String serverName)");
		return result;
	}

	public static void invokeProcessBuilderTomcat(String[] args)
			throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.invokeProcessBuilderTomcat(String[] args)");
		LOGGER.info("args: " + args);

		ProcessBuilder pb = new ProcessBuilder(args);
		Process p = pb.start();
		int maxTries = 30;
		while (isPortAvailable1(TOMCAT_PORT, "localhost")) {
			LOGGER.info("STILL LISTENING ON " + TOMCAT_PORT
					+ ": STOPPING TOMCAT");
			Thread.sleep(10000);// 10 sec
			if (--maxTries < 0) {
				throw new Exception("Timed out while waiting Tomcat to stop!");
			}
		}
		p.destroy();
		LOGGER.info("Tomcat agent stopped!");
		LOGGER.info("Leaving com.apm.automation.Util.invokeProcessBuilderTomcat(String[] args)");
	}

	public static boolean isPortAvailable1(int port, String hostName) {
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

	public static void stopAgentTomcatWindows(String appserverType,
			String appServerHome, String serverName) throws Exception {
		LOGGER.info("Entering com.apm.automation.Util.stopAgentTomcatWindows(String appserverType, String appServerHome, String serverName)");
		LOGGER.info("appserverType: " + appserverType);
		LOGGER.info("appServerHome: " + appServerHome);
		LOGGER.info("serverName: " + serverName);

		stopAgentWindows(appserverType, appServerHome, serverName);
		
//		List<String> args = new ArrayList<String>();
//		args.add(appServerHome
//				+ AutomationConstants.TOMCAT_STOP_SCRIPT_PATH_WINDOWS);
//		invokeProcessBuilderTomcat(args.toArray(new String[1]));
		
		LOGGER.info("Leaving com.apm.automation.Util.stopAgentTomcatWindows(String appserverType, String appServerHome, String serverName)");
	}

	public static List<Long> querySmartStor(String url, String agentExpr,
			String metricExpr, long intervalSec) throws Exception {

		List<Long> smartStorData = new ArrayList<Long>();
		IntroscopeDriver driver = new IntroscopeDriver();
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;

		try {
			conn = driver.connect(url, null);
			System.out.println("Connected to " + url);

			String query = "select * from metric_data " + "where agent='"
					+ agentExpr + "' and " + "metric='" + metricExpr + "' and "
					+ "timestamp between " + lastIntervalQuery(intervalSec)
					+ "aggregateall";

			stmt = conn.createStatement();
			stmt.execute(query);
			System.out.println("Executing query: " + query);
			rs = stmt.getResultSet();

			while (rs.next()) {

				smartStorData.add(rs.getLong("Value"));
			}

		} finally {

			DbUtils.closeQuietly(conn, stmt, rs);

		}

		return smartStorData;
	}

	public static SmartStorResult querySmartStorForMetrics(String url,
			String agentExpr, String metricExpr, long intervalSec)
			throws Exception {

		IntroscopeDriver driver = new IntroscopeDriver();
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		SmartStorResult signature = null;
		try {
			conn = driver.connect(url, null);
			System.out.println("Connected to " + url);
			String query = "select * from metric_data " + "where agent='"
					+ agentExpr + "' and " + "metric='" + metricExpr + "' and "
					+ "timestamp between " + lastIntervalQuery(intervalSec);

			stmt = conn.createStatement();
			stmt.execute(query);
			System.out.println("Executing query: " + query);
			rs = stmt.getResultSet();

			signature = new SmartStorResult(rs);

		} finally {

			DbUtils.closeQuietly(conn, stmt, rs);

		}

		return signature;
	}

	public static boolean findPattern(String filePath, String pattern)
			throws Exception {

		boolean patternFound = false;
		LineNumberReader lineReader = null;
		File file = new File(filePath);

		try {

			Pattern regexp = Pattern.compile(pattern);
			Matcher matcher = regexp.matcher("");
			lineReader = new LineNumberReader(new FileReader(file));

			String line = null;
			while ((line = lineReader.readLine()) != null && !patternFound) {
				matcher.reset(line); // reset the input
				if (matcher.find()) {
					patternFound = true;
				}
			}

		} finally {

			if (lineReader != null)
				lineReader.close();
		}

		return patternFound;
	}

	public static Properties loadPropertiesFile(String fileName)
			throws Exception {

		Properties properties = new Properties();
		InputStream propsFile = new FileInputStream(fileName);

		try {
			properties.load(propsFile);
		} finally {
			propsFile.close();
		}

		return properties;
	}

	public static void writePropertiesToFile(String fileName,
			Properties properties) throws Exception {

		OutputStream output = new FileOutputStream(fileName);
		try {
			properties.store(output, "");
		} finally {
			output.close();
		}
	}

	public static void sleep(long duration) {
		LOGGER.info("Sleeping for " + duration + " milliseconds.");
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			LOGGER.error("sleep failed:", e);
		}
	}

	public static boolean inTolerance(int observed, int expected, int tolerance) {

		System.out.println("**********  in Tolerance Method: observed: "
				+ observed + " - Expected: " + expected);
		if ((observed >= expected - tolerance)
				&& (observed <= expected + tolerance)) {
			System.out
					.println("**********  in  IF of Tolerance Method: expected-tolerance. Returning true ********* ");
			return true;

		}

		return false;
	}

	// To comment / uncomment the line in a file
	public static boolean replaceLine(String fileName, String oldLine,
			String newLine) {

		String line = "";
		String lines = "";
		boolean isFound = false;

		try {
			File file = new File(fileName);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				lines += line + "\r\n";
			}
			reader.close();

			if (lines.contains(oldLine)) {

				lines = lines.replaceAll(oldLine, newLine);
				// System.out.println("******* newLine: **** " + lines);
				System.out
						.println("*********** Value of oldLine is " + oldLine);
				System.out
						.println("*********** Value of newLine is " + newLine);

				FileWriter writer = new FileWriter(fileName);
				writer.write(lines);
				writer.close();
				isFound = true;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return isFound;

	}

    /**
     * checks for the availability of the processsID in task list
     * 
     * @param processId
     * @return
     * @throws IOException
     */

    public static boolean isProcessAvail(String processId) throws IOException {
        if (processId == null) return false;
        Process p = null;
        BufferedReader reader = null;
        boolean processIDfound = false;
        String workingDir = "/";
        try {
            if (platform.toUpperCase().contains("WINDOWS")) {
                p = Runtime.getRuntime().exec(AutomationConstants.TASK_LIST);
            } else {
                String[] command = {"ps -ef|grep " + processId};
                p = ApmbaseUtil.runCommand(Arrays.asList(command), workingDir);
            }

            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();

            while (line != null) {

                if (line.contains(processId)) {
                    System.out.println("%%%%%%%%%%RUNNING WITH PROCESSID:" + processId);

                    processIDfound = true;
                    break;

                }
                line = reader.readLine();
            }

            if (processIDfound) {
                return true;
            }
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return false;
    }
    
    /**
	 * Returns the process instance for the commands passed
	 * 
	 * @param commands
	 *            - commands start the sql agent
	 * @param dirLoc
	 *            - commands execution directory location
	 * @return - return the process
	 * @throws IOException
	 *             -Any IO Exception throws the error
	 */

	public static Process getProcess(String[] commands, String dirLoc)
			throws IOException {

		if (commands == null)
			return null;
		String[] execCommandStrings = new String[commands.length + 2];
		if (platform.toUpperCase().contains("WINDOWS")) {
		    execCommandStrings[0] = "cmd.exe";
	        execCommandStrings[1] = "/c";
        } else if (platform.toUpperCase().contains("LINUX")) {
            execCommandStrings[0] = "sh";
            execCommandStrings[1] = "-c";
        }
		
		for (int i = 0; i < commands.length; i++) {
			execCommandStrings[i + 2] = commands[i];
		}

		ProcessBuilder processBuilder = new ProcessBuilder(execCommandStrings);
		processBuilder.directory(new File(dirLoc));
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		return process;

	}

	/**
	 * check for the message in the file
	 * 
	 * @param compareStrings
	 * @param file
	 * @return
	 * @throws InterruptedException
	 */

	public static int checkMessage(String compareStrings, File file)
			throws InterruptedException {
		int found = 0;
		DataInputStream in = null;
		FileInputStream fstream = null;
		BufferedReader br = null;
		if (compareStrings != null) {
			try {
				fstream = new FileInputStream(file);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
				String str;
				while ((str = br.readLine()) != null) {
					if (str.toLowerCase()
							.contains(compareStrings.toLowerCase())) {
						System.out.println("---- " + compareStrings);
						found = 1;
						break;
					}

				}

			} catch (Exception e) {
				System.err.println(e);
			} finally {
				try {
					fstream.close();
					in.close();
					br.close();

				} catch (IOException e) {

					e.printStackTrace();
				}

			}

		}
		return found;

	}

	/**
	 * This method is used to check whether we are able to access the webpage or
	 * not
	 * 
	 * @param webPage
	 * @return
	 */
	public static boolean urlResponse(String webPage) {
		try {
			System.out.println("URL to be hit" + webPage);
			URL url = new URL(webPage);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				// System.out.println(str);
			}
			in.close();
			return true;
		} catch (Exception e) {
			System.out.println("PAGE NOT FOUND");
			return false;
		}
	}

	/**
	 * Method to copy the file from source to destination
	 * 
	 * @param srcFile
	 *            source file location
	 * @param destFile
	 *            destination file location
	 * @throws IOException
	 */
	public static void fileCopyTask(String srcFile, String destDir)
			throws IOException {

		Task task = null;
		Context context = null;
		IResponse response = null;

		System.out.println(" **** File Copy Source : " + srcFile);
		System.out.println(" **** File Copy Destination Dir : " + destDir);

		try {
			task = new CopyTask(srcFile, destDir);
			context = new Context();
			response = task.execute(context);

			System.out.println(" **** File Copy Task Completed : "
					+ response.isSuccess());
		} catch (Exception e) {
			System.out.println("*** Exception in fileCopyTask: "
					+ e.getMessage());
		} finally {
			response = null;
			context = null;
			task = null;
		}

	} // End of fileCopyTask

	/**
	 * Method to delete a file
	 * 
	 * @param fileToBeDeleted
	 * @throws IOException
	 */
	public static void fileDelete(String fileToBeDeleted) throws IOException {

		File file = null;

		try {

			if (fileExist(fileToBeDeleted)) {
				System.out.println(fileToBeDeleted);
				System.out
						.println("**************** DELETEING File **********");

				file = new File(fileToBeDeleted);

				boolean a = file.delete();

				System.out.println("**************** DELETED File ***********"
						+ a);

			} else {
				System.out.println(fileToBeDeleted
						+ "********FILE NOT FOUND********");
			}
		} catch (Exception e) {
			System.out
					.println("*** Exception in fileDelete: " + e.getMessage());
		} finally {
			file = null;
		}

	} // End of fileDelete

	/**
	 * Method to check for file exist
	 * 
	 * @param fileDir
	 *            Name of the file
	 * @return boolean
	 */
	public static boolean fileExist(String fileDir) {
		boolean isFileExists = false;
		try {
			Task task = new ExistsTask(fileDir);
			Context context = new Context();
			IResponse response = task.execute(context);
			isFileExists = response.isSuccess();
		} catch (Exception e) {
			System.out.println("***** Exception is in fileExist: "
					+ e.getMessage());

		}
		return isFileExists;
	} // fileExist

	/**
	 * Method to run a jar file app
	 * 
	 * @param jarfile
	 *            with absolute path
	 * @return boolean
	 */
	public static void runEchoServer(String vjarFile, String listPort,
			String shutdownPort) throws Exception {

		List<String> args = new ArrayList<String>();
		args.add("java");
		args.add("-jar");
		args.add(vjarFile);
		args.add(listPort);
		args.add(shutdownPort);

		invokeProcessBuilderNoWait(args);

	} // End of runEchoServer

	/**
	 * Method to check the port availability
	 * 
	 * @param port
	 *            and hostname
	 * @return boolean
	 */
	public static boolean isPortAvailable(int port, String hostName)
			throws Exception {
		Socket soc = null;
		boolean isAvailable = false;
		try {
			soc = new Socket(hostName, port);
			isAvailable = soc.isBound();

		} catch (Exception e) {
			System.out.println("******* Exception isPortAvailable: "
					+ e.getMessage());
			isAvailable = false;
		} finally {
			if (soc != null)
				soc.close();
		}
		return isAvailable;

	} // End of isPortAvailable

	/**
	 * This method is to delete the Directory
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static boolean deleteFileDir(File dir) throws IOException {

		boolean deleted = false;
		if (!dir.isDirectory()) {
			return deleted;
		}
		System.out.println("%%%%%%%%%%%% DELETEING DIRECOTRY %%%%%%%%%%%%%%");
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			if (file.isDirectory()) {
				deleteDir(file);
			} else {
				deleted = file.delete();
				if (!deleted) {
					// throw new IOException("Unable to delete file" + file);
					return deleted;
				}
			}
		}

		return dir.delete();
	}

	/**
	 * This method prepares the complete CLW Command by taking the part of
	 * command from input parameter and runs the same. This method internally
	 * calls runCommand to perform the actual task of CLW Command execution. If
	 * the command is successfully executed then the response is written to xml
	 * file named DynamicInstrumentationData-yyyymmddHHMMSS.xml and is stored in
	 * Local variable genXMLName.
	 * 
	 * @param command1
	 *            - holds sub-part of the CLW command which needs to be executed
	 *            for DI
	 * @return *
	 * @return - boolean value [true/false]
	 */
	public static String runCLWCommandHelper(String command) {
		String result = null;
		try {
			System.out.println("final query before execute" + command);
			StringBuilder consoleMessages = runCommand(command);
			System.out.println(consoleMessages.toString());
			result = consoleMessages.toString();
			Util.sleep(60 * 1000);
		} catch (Exception e) {
			System.out.println("Exception in method runCLWCommandHelper: "
					+ e.getMessage());
		}
		return result;
	}

	/**
	 * Common helper method used to Run CLW Command.
	 * 
	 * @param command
	 *            - Command that should be executed.
	 * @return - String which holds the CLW Command response line.
	 * @throws Exception
	 *             - the Exception raised by this method
	 */
	private static StringBuilder runCommand(String command) throws Exception {
		System.out.println("Start of runCommand");
		String line = null;
		Process process = null;
		StringBuilder cdChanges = new StringBuilder();
		ProcessBuilder processBuilder = null;
		processBuilder = new ProcessBuilder("cmd.exe", "/C", command.toString());
		processBuilder.redirectErrorStream(true);
		try {
			process = processBuilder.start();
			Util.sleep(60 * 3 * 1000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			System.out.println(reader.readLine() + "reader.readLine()");
			while ((line = reader.readLine()) != null) {

				line = reader.readLine();
				System.out.println(line);
				cdChanges.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
				process.getErrorStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				System.out.println("process destroyed");
			}
		}
		return cdChanges;
	}

	private static String lastIntervalQuery(long intervalSec) {

		Date now = new Date();
		Date past = new Date(System.currentTimeMillis() - (intervalSec * 1000));

		DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
		return "'" + dateFormat.format(past) + "' and '"
				+ dateFormat.format(now) + "'";
	}

	/*
	 * * This method is to delete the Directory
	 * 
	 * @param dir
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	private static boolean deleteDir(File dir) throws IOException {
		boolean deleted = false;
		if (!dir.isDirectory()) {
			return deleted;
		}
		System.out.println("%%%%%%%%%%%% DELETEING DIRECOTRY %%%%%%%%%%%%%%");
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			if (file.isDirectory()) {
				deleteDir(file);
			} else {
				deleted = file.delete();
				if (!deleted) {
					// throw new IOException("Unable to delete file" + file);
					return deleted;
				}
			}
		}

		return dir.delete();
	}

}