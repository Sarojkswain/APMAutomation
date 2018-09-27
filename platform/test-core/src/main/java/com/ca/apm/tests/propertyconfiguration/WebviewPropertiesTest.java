/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : KETSW01
 */
package com.ca.apm.tests.propertyconfiguration;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.testbed.EMPropertyConfigurationtWindowsTestbed;


public class WebviewPropertiesTest extends BaseAgentTest{

	private static String emRoleId = EMPropertyConfigurationtWindowsTestbed.EM_ROLE_ID;
	private static String emMachineId = EMPropertyConfigurationtWindowsTestbed.EM_MACHINE_ID;
	
	private String emConfigDir  = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);

	
	private static Logger logger = Logger
			.getLogger(WebviewPropertiesTest.class);
	
	 
	@Test(groups = {"WebviewProperties", "deep"})
	public void verify_ALM_454129_DE132344_10x_webview_jetty_config_file_missing_new_certAlias_property()
	{
		String testCaseNameIDPath = "verify_ALM_454129_DE132344_10x_webview_jetty_config_file_missing_new_certAlias_property";
		String text = "<Set name=\"certAlias\">wily</Set>";
				
		testCaseStart(testCaseNameIDPath);
		try{
		isKeywordInFile(envProperties, emMachineId, emConfigDir+"webview-jetty-config.xml", text);
		Assert.assertTrue(true);
		}catch(Exception e){
		    e.printStackTrace();
		    Assert.assertTrue(false);
		}    
		testCaseEnd(testCaseNameIDPath);
	}
	
}
