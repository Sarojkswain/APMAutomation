package com.ca.apm.tests.utils;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.automation.common.Util;
import com.ca.apm.tests.config.AppConfig;
import com.ca.apm.tests.config.BaseAppConfig;
//import com.ca.apm.tests.config.CollectorAgentConfig;
import com.ca.apm.tests.config.NodeJSProbeConfig;
import com.ca.apm.tests.config.UMAgentConfig;

/**
 * @author sinka08
 *
 */
public class CommonUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	public static final String[] EXPECTED_ERRORS = new String[]{".*Socket closed.*",
        ".*ARF Connection socket closed.*", ".*Attempted tcp connection by client .* on ARF server port.*",
        ".*IsengardServerConnectionManager.*Failed to disconnect agent bridge.*"};
	
	public static void createBackupOfAppConfig(BaseAppConfig[] appConfigs) {
		for (BaseAppConfig appConfig : appConfigs) {
			createBackupOfAppConfig(appConfig);
		}
	}

	public static void createBackupOfAppConfig(BaseAppConfig appConfig) {
		File backupDir = new File(appConfig.getBackupDir());

		try {
			File backupFile = new File(backupDir, appConfig.getConfigFileName());
			if (!backupFile.exists()) {
				FileUtils.copyFileToDirectory(appConfig.getConfigFile(), backupDir, true);
				LOGGER.info("Successfully created backup file: {} ", backupFile.getAbsolutePath());
			} else {
				LOGGER.info("Skipped creating backup file: {} as it already exists",
				        backupFile.getAbsolutePath());
			}

		} catch (IOException e) {
			LOGGER.error("error while creating backup file for: {}", appConfig.getConfigFilePath());
			LOGGER.error(e.getMessage(), e);
		}
	}

	public static void revertAppConfigFileToOriginal(BaseAppConfig[] appConfigs) {
		for (BaseAppConfig appConfig : appConfigs) {
			revertAppConfigFileToOriginal(appConfig);
		}
	}

	public static void revertAppConfigFileToOriginal(BaseAppConfig appConfig) {
		File backupDir = new File(appConfig.getBackupDir());
		File backupFile = new File(backupDir, appConfig.getConfigFileName());

		if (backupFile.exists()) {
			try {
				File currentFile = appConfig.getConfigFile();

				if (currentFile.exists()) {
					currentFile.delete();
				}
				FileUtils.copyFile(backupFile, appConfig.getConfigFile(), true);
				LOGGER.info("Copying file " + backupFile.getAbsolutePath() + " to "
				        + appConfig.getConfigFile().getAbsolutePath());
			} catch (IOException e) {
				LOGGER.error("Error while reverting file {} to original file",
				        appConfig.getConfigFileName());
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			LOGGER.info("Backup file: {} does not exist", backupFile.getAbsoluteFile());
		}
	}

	public static void copyLogs(BaseAppConfig[] appConfigs) {
		for (BaseAppConfig appConfig : appConfigs) {
			copyLogs(appConfig);
		}
	}

	public static void copyLogs(BaseAppConfig appConfig) {
		// move all logs from previous test run into separate dir(suffixed by
		// index)
		String logPath = appConfig.getLogPath();
		try {

			if (logPath != null) {
				Util.copyIntoIndexedDir(new File(logPath).getParentFile().getAbsolutePath());
			}

		} catch (Exception e) {
			LOGGER.error(String.format("Error occured while moving %s ", logPath), e);
		}
	}

	public static List<String> findUnexpectedMessagesInLog(AppConfig appConfig,
	        List<String> msgRegexList, List<String> expectedMessages) throws Exception {

		String path = appConfig.getLogPath();
		TreeSet<String> messages = new TreeSet<>();

		LOGGER.debug("checking messages: {} in log file: {}", msgRegexList, path);

		for (String msgStringRegex : msgRegexList) {
			// regex to match with complete line in log file
			String errorLineRegex = ".*" + msgStringRegex + ".*";

			messages.addAll(Util.getUniqueErrorMessages(
			        Util.collectUniqueLines(path, errorLineRegex), msgStringRegex));
		}

		Iterator<String> iter = messages.iterator();
		while (iter.hasNext()) {
			String message = iter.next();

			if (expectedMessages != null) {

				for (String expectedMessage : expectedMessages) {
					Matcher matcher = Pattern.compile(expectedMessage).matcher(message);

					if (matcher.matches()) {
						LOGGER.debug("expected message:' {} ' matched line: ' {} '",
						        expectedMessage, message);

						// this is expected error, remove from set
						iter.remove();
						break;
					}
				}
			}
		}

		return new ArrayList<String>(messages);
	}
	
	public static void checkErrorInCollectorLogs(UMAgentConfig umAgentConfig) {
	    checkErrorInCollectorLogs(umAgentConfig, Arrays.asList(EXPECTED_ERRORS));
	}

	public static void checkErrorInCollectorLogs(UMAgentConfig umAgentConfig,
	                                             List<String> expectedErrorMessages) {
		try {
			// check for errors in collector agent log
			List<String> errorRegexs = Arrays.asList("\\[ERROR\\]");
			List<String> unexpectedErrors = CommonUtils.findUnexpectedMessagesInLog(
					umAgentConfig, errorRegexs, expectedErrorMessages);
			String path = umAgentConfig.getLogPath();

			if (unexpectedErrors.size() > 5) {
				for (String errorMessage : unexpectedErrors) {
					LOGGER.debug("error message : {} ", errorMessage);
				}

				Assert.fail(String.format("%s errors were found in the collector agent log: %s",
				        unexpectedErrors.size(), path));
			} else {
				StringBuffer errorsListing = new StringBuffer();

				for (String error : unexpectedErrors) {
					errorsListing.append(error + "\n");
				}
				Assert.assertTrue(unexpectedErrors.size() == 0, String
				        .format("Errors: %s were found in the collector agent log: %s",
				                errorsListing, path));
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while checking error in collector agent logs");
		}
	}

	public static void checkErrorInProbeLogs(NodeJSProbeConfig probeConfig) {
		try {
			// check for error in probe log.
			// example of error message
			// 2016/04/21 10:27:35:35-07:00 - error: got exception: 'TypeError:
			// Cannot
			// TODO update the error expr to match winston logger pattern
			List<String> errorMessages = Arrays.asList(".*\\[error\\].*", ".*uncaughtException.*",
			        ".*error: got exception.*");
			String path = probeConfig.getLogPath();

			Set<String> patterns = new HashSet<String>();
			patterns.add(new File(path).getName() + "([0-9]*)?");
			String[] probeLogs = Util.getFilesMatchingPattern(new File(path).getParent(), patterns);

			for (String log : probeLogs) {
				for (String errorMsg : errorMessages) {
					assertFalse(Util.findPattern(log, errorMsg), String.format(
					        "Error '%s' was found in the probe log: %s", errorMsg, log));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while checking error in probe logs");
		}
	}

	public static boolean isKeywordInLog(LogUtils util) {
		boolean found = false;
		int numRetries = 0;

		while (!found && numRetries++ < 5) {
			found = util.isKeywordInLog();
			Util.sleep(1000);
		}

		return found;
	}

	public static String normalizeMetricPathForTraceComparison(String path) {
		return path.replace("|", "\\|");
	}

	public static String createFullPath(String... args) {
		if (args.length == 1) {
			return args[0];
		}

		StringBuilder sb = new StringBuilder();
		
		if(args[0].substring(args[0].length() -1, args[0].length()).equals(File.separator))
		{
			args[0] = args[0].substring(0, args[0].length()-1);
		}
		
		sb.append(args[0]);
		int l = args.length;
		int i = 1;

		while (i < l) {
			sb.append(File.separator);
			sb.append(args[i++]);
		}

		return sb.toString();
	}

	public static boolean waitForFile(String path) {
		return waitForFile(new File(path));
	}

	public static boolean waitForFile(File f) {
		int max = 10;
		int i = 0;

		try {
			while (!f.exists() && i++ < max) {
				TimeUnit.SECONDS.sleep(5);
			}
			return f.exists();
		} catch (InterruptedException e) {
			return false;
		}
	}

}
