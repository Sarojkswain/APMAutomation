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

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.commons.coda.common.Util;


/**
 * Flow that checks if some keywords appear in chosen logs. Configuration in
 * context
 * 
 * @author gamsa03
 *
 */
public class CheckFileExistenceFlowOneTimeCounter implements IAutomationFlow {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CheckFileExistenceFlowOneTimeCounter.class);

	@FlowContext
	private CheckFileExistenceFlowOneTimeCounterContext context;

	@Override
	public void run() throws Exception {
		
		String filePath = context.getfilePath().trim();
		LOGGER.info("Checking for the existence of file " + filePath);
		
		 if (Util.fileExist(filePath)){
				LOGGER.info("The specified file exists");
				
			}else{
				LOGGER.info("The specified file does not exists");
				throw new FileNotFoundException("file not found exception");
			}
	
	}
	
}
