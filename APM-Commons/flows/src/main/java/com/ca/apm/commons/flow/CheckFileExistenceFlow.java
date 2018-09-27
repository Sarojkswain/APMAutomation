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
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.common.TestUtils;


/**
 * Flow that checks if some keywords appear in chosen logs. Configuration in
 * context
 * 
 * @author gamsa03
 *
 */
public class CheckFileExistenceFlow implements IAutomationFlow {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CheckFileExistenceFlow.class);

	@FlowContext
	private CheckFileExistenceFlowContext context;

	@Override
	public void run() throws Exception {
		int i;
		String filePath = context.getfilePath().trim();
		LOGGER.info("Checking for the existence of file " + filePath);
		
		for (i = 1; i <=20; i++) {
		
			if (Util.fileExist(filePath)){
				LOGGER.info("The specified file exists");
				break;
			}else{
				LOGGER.info("The specified file does not exists");
			}
			LOGGER.info("Waiting for 30 seconds to check for file existence");
			Thread.sleep(30000);
		}
		
		LOGGER.info("Confirmation of the file existence took about" + i*30 + "seconds.." );
		
	}
	
}
