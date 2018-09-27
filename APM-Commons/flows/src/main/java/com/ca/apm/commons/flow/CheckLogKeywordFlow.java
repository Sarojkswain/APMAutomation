/*
 * Copyright (c) 2015 CA. All rights reserved.
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
 */
package com.ca.apm.commons.flow;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.commons.coda.common.ApmbaseUtil;

//import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;

/**
 * Flow that checks if some keywords appear in chosen logs. Configuration in
 * context
 * 
 * @author ketsw01
 *
 */
public class CheckLogKeywordFlow implements IAutomationFlow {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RunCommandFlow.class);

	@FlowContext
	private CheckLogKeywordFlowContext context;

	/*
	 * (non-Javadoc) Will fail the test by throwing exception if keyword is not
	 * found
	 * 
	 * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
	 */
	@Override
	public void run() throws Exception {
		System.out.println("Inside run method of CheckLogKeywordFlow");
		String methodName = context.getMethodName().trim();
		List<String> arguments = context.getArguments();
		LOGGER.debug(methodName);
		LOGGER.info("List of arguments: " + arguments);
		switch (methodName) {
		case "isKeywordInFile":
			if (arguments.size() == 2) {
				if (isKeywordInFile(arguments.get(0), arguments.get(1))) {
					LOGGER.info("Keyword found in file, everything's fine.");
				} else {
					throw new IllegalStateException(
							"Required keyword not in file.");
				}
			}
			break;

		case "checkMessagesInSequence":
			if (arguments.size() != 0) {
				String filePath = arguments.get(0);
				arguments.remove(0);
				String[] keywords = arguments.toArray(new String[arguments
						.size()]);
				if (checkMessagesInSequence(filePath, keywords)) {
					LOGGER.info("Keywords found in sequence, everything's fine.");
				} else {
					throw new IllegalStateException("Keywords not in sequence.");
				}
			}
			break;

		case "checkTimeStampValueOfKeyword":
			if (arguments.size() == 3) {
				if (checkTimeStampValueOfKeyword(arguments.get(0),
						arguments.get(1), arguments.get(2))) {
					LOGGER.info("TimeStamp value logged correctly, everything's fine.");
				} else {
					throw new IllegalStateException(
							"Incorrect timeStamp value logged");
				}
			}
			break;
			
		//To check the contents of the EM list in the specified file with theprovided EM list	
		case "checkEMListContents":
			if (arguments.size() > 1){
				ApmbaseUtil apmbaseUtil = new ApmbaseUtil();
				String filePath = arguments.get(0);
				//Remove the filePath to make it contains only required EMs list
				arguments.remove(0);
				if(apmbaseUtil.checkEMListContents(filePath, arguments)){
					LOGGER.info("The latest EM list of the file@ " + filePath + " contains all "
							+ "the expected entries of EMs");
				}else{
					throw new IllegalStateException("The latest EM list of the file@ " + filePath + 
							" does not contain all the expected entries of EMs" );
				}
			}
			else{
				LOGGER.info("Not all the required arguments available to verify");
			}
		break;
			
		//To check the size of the EM list with the given size in the specified file
		case "checkEMListSize":
			if (arguments.size() > 1){
				ApmbaseUtil apmbaseUtil = new ApmbaseUtil();
				String filePath = arguments.get(0);
				//Get the specified size of the list
				LOGGER.info("The expected list size is " + arguments.get(1));
				Integer emListSize = Integer.parseInt(arguments.get(1));
				if(apmbaseUtil.checkEMListSize(filePath, emListSize)){
					LOGGER.info("The latest EM list of the file@ " + filePath + " matches the "
							+ "the expected size of EM list");
				}else{
					throw new IllegalStateException("The latest EM list of the file@ " + filePath + 
							" does not match expected size of EM list" );
				}
			}
			else{
				LOGGER.info("Not all the required arguments available to verify");
			}
		break;			
			
		default:
			System.out
					.println("No matching method in CheckLogKeywordFlow class");
		break;

		}
	}

	/**
	 * Looks through log and checks if some keyword appears in it.
	 * 
	 * 
	 * @param keyword
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean isKeywordInFile(String logfilePath, String keyword)
			throws FileNotFoundException, IOException {

		LOGGER.info("Looking for " + keyword + " in " + logfilePath);
		try (FileReader fr = new FileReader(logfilePath);
				BufferedReader br = new BufferedReader(fr);) {
			String line = br.readLine();
			while (line != null) {
				if (line.contains(keyword)) {
					LOGGER.info("Keyword found");
					return true;
				}
				line = br.readLine();
			}

		}
		return false;
	}

	/**
	 * Looks through log and returns the line number that contains the keyword
	 * 
	 * @param logfilePath
	 * @param keyword
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static int returnLineNumberWithKeyword(String logfilePath,
			String keyword) throws FileNotFoundException, IOException {
		int count = 0;
		LOGGER.info("Looking for " + keyword + " in " + logfilePath);
		try (FileReader fr = new FileReader(logfilePath);
				BufferedReader br = new BufferedReader(fr);) {
			String line = br.readLine();
			while (line != null) {
				count++;
				if (line.contains(keyword)) {
					LOGGER.info("Keyword found");
					return count;
				}
				line = br.readLine();
			}

		}
		return -1;
	}

	/**
	 * This method is to get the property value on its key
	 * 
	 * @param paramName
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getPropertyValue(String paramName, String filePath)
			throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(filePath));
		String value = properties.getProperty(paramName);
		return value;

	}

	/**
	 * Looks through file and checks if keywords appears in sequence. 2 or more
	 * keywords should be passed as arguments
	 * 
	 * @param logfilePath
	 * @param keywords
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean checkMessagesInSequence(String logfilePath,
			String... keywords) throws FileNotFoundException, IOException {
		boolean result = false;
		if (keywords.length > 1) {
			for (int i = 0; i < keywords.length-1; i++) {
				LOGGER.info("Checking sequence of messages: " + keywords[i] + "," + keywords[i+1] + " in " + logfilePath);
				if (returnLineNumberWithKeyword(logfilePath, keywords[i]) < returnLineNumberWithKeyword(
						logfilePath, keywords[++i])) {
					result = true;
				} else {
					result = false;
					break;
				}
			}
		} else {
			throw new IllegalStateException(
					"Atleast two keywords should be passed as arguments to check the sequence");
		}
		return result;
	}

	/**
	 * Checks if timestamp value of a String logged as a string-value pair in
	 * file is greater than the timestamp passed
	 * 
	 * @param filePath
	 * @param keyword
	 * @param timestamp
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean checkTimeStampValueOfKeyword(String filePath,
			String keyword, String timestamp) throws FileNotFoundException,
			IOException {
		String timestampValueInLog = getPropertyValue(keyword, filePath);
		LOGGER.info("Comparing timestamps: "+timestamp+","+timestampValueInLog);
		return Long.parseLong(timestamp) < Long.parseLong(timestampValueInLog);
	}
	
}
