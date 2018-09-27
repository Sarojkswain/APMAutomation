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
 * AUTHOR: MARSA22/SAI KUMAR MAROJU
 * DATE: 09/19/2017
 */
package com.ca.apm.tests.agentextension;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounter;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounterContext;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.resolver.ITasResolver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class F5UmaAgentExtensionTest extends StandAloneEMOneTomcatTestsBase {

	public static final String EXTENSION_LOC_WIN = TasBuilder.WIN_SOFTWARE_LOC;
	public static final String EXTENSION_LOC_LINUX = TasBuilder.LINUX_SOFTWARE_LOC;

	public static final String UMA_INSTALL_COMMAND = "install";
	public static final String UMA_START_COMMAND = "start";
	public static final String UMA_STOP_COMMAND = "stop";

	public static final String UMA_WIN_BAT_FILE = EXTENSION_LOC_WIN
			+ "apmia\\apmia-ca-installer.bat";
	public static final String UMA_LINUX_SH_FILE = EXTENSION_LOC_LINUX
			+ "apmia/apmia-ca-installer.sh";
	public static final String UMA_ROLE_ID = "umaRole";
	public static final String UMA_F5_EXT = EXTENSION_LOC_LINUX
			+ "apmia/extensions/";
	public static final String UMA_F5_EXT_BUNDLE_PROPERTIES = EXTENSION_LOC_LINUX
			+ "apmia/extensions/F5LTMExtension/bundle.properties";

	public static final String UMA_F5_EXT_PROFILE = EXTENSION_LOC_LINUX
			+ "apmia/extensions/Extensions.profile";
	public static final String UMA_F5_EXT_LOG = EXTENSION_LOC_LINUX
			+ "apmia/logs/IntroscopeAgent.log";
	public static final String WIN_UMA_LOG_FILE = EXTENSION_LOC_WIN
			+ "extension\\UMA\\logs\\IntroscopeAgent.log";

	String agentExpression = "(.*)\\|Common\\|Agent";

	protected RunCommandFlowContext umaInstall;
	protected RunCommandFlowContext umaStart;
	protected RunCommandFlowContext umaStop;
	String url = "http://172.20.75.105/examples/servlets/servlet/RequestInfoExample";
	boolean linux;
	protected String extensionLocation;

	

	String umagentProfileFile;

	

	CLWCommons clw = new CLWCommons();

	TestUtils utility = new TestUtils();

	static int counter = 1;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(F5UmaAgentExtensionTest.class);

	ITasResolver tasResolver;

	String emHostName;

	/**
	 * Constructor
	 */
	public F5UmaAgentExtensionTest() {
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			extensionLocation = EXTENSION_LOC_WIN;
			
			linux = false;
			umagentProfileFile = extensionLocation
					+ "UMA\\core\\config\\IntroscopeAgent.profile";

		} else {
			extensionLocation = EXTENSION_LOC_LINUX;
			
			linux = true;
			umagentProfileFile = extensionLocation
					+ "apmia/core/config/IntroscopeAgent.profile";

		}

		emHostName = envProperties
				.getMachineHostnameByRoleId(AgentControllabilityConstants.EM_ROLE_ID);

		

	}

	@BeforeClass(alwaysRun = true)
	public void initialize() {

		LOGGER.info("Initialize begins here");

		LOGGER.info("Invoked testmethod in extensionjava");

		startEM();

		if (linux) {

			installLinuxUMA();

			harvestWait(5);
			stopLinuxUMA();
			harvestWait(5);

			try {
				renameDir(UMA_F5_EXT + "f5ltmExtension", UMA_F5_EXT
						+ "F5LTMExtension",
						envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));
				List<String> automaticF5ExtensionProperties = new ArrayList<String>();

				replaceProp("agentManager.url.1=localhost:5001",
						"agentManager.url.1=" + emHostName + ":5001",
						envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID),
						umagentProfileFile);

				replaceProp(
						"introscope.agent.extensions.bundles.load=",
						"introscope.agent.extensions.bundles.load=F5LTMExtension",
						envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID),
						UMA_F5_EXT_PROFILE);

				automaticF5ExtensionProperties
						.add("introscope.agent.f5.monitoredRules=_sys_auth_ldap,_sys_auth_radius");
				automaticF5ExtensionProperties
						.add("introscope.agent.f5.password=admin@123");
				automaticF5ExtensionProperties
						.add("introscope.agent.f5.host=tas-itc-f5.ca.com");
				automaticF5ExtensionProperties
						.add("introscope.agent.f5.virtualServer.hosts=172.20.75.105");
				automaticF5ExtensionProperties
						.add("introscope.agent.f5.update.interval=10");
				automaticF5ExtensionProperties
						.add("introscope.agent.f5.user=admin");
				automaticF5ExtensionProperties
						.add("introscope.agent.f5.port=443");

				appendProp(automaticF5ExtensionProperties, TOMCAT_MACHINE_ID,
						UMA_F5_EXT_BUNDLE_PROPERTIES);

			} catch (Exception e) {
				LOGGER.info("Unable to copy DatapowerMonitor-config.xml file ");
				e.printStackTrace();
			}

		} else {

			try {

			} catch (Exception e) {
				LOGGER.info("Unable to copy DatapowerMonitor-config.xml file ");
				e.printStackTrace();
			}
			harvestWait(30);
			// stopUMA();
		}

	}

	// Test No. 1
	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457095_F5_For_Hosts() throws Exception {

		LOGGER.info("This is to verify _ClientSSLProfile");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);
		LOGGER.info("This is to verify _ClientSSLProfile123");
		LOGGER.info("This is to verify _ClientSSLProfile");

		startLinuxUMA();

		harvestWait(60);

		String metricExpression2 = "F5\\|Hosts\\|0\\|CPU:Active Count";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {

			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457095_F5_For_Hosts");

		}

	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457094_F5_For_Client_SSL_Profile() {
		LOGGER.info("This is to verify _ClientSSLProfile");

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|Client SSL Profile\\|Common_clientssl:Max Connections";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {
			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457094_F5_For_Client_SSL_Profile");
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457096_F5_For_Logical_Disks() {
		LOGGER.info("This is to verify _Logical_Disks");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|Logical Disks\\|HD1:Size Bytes";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457096_F5_For_Logical_Disks");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457097_F5_For_Network_Interfaces() {
		LOGGER.info("This is to verify _Network_Interfaces");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);
		LOGGER.info("This is to verify _ClientSSLProfile123");
		LOGGER.info("This is to verify _ClientSSLProfile");

		startLinuxUMA();

		harvestWait(60);

		String metricExpression2 = "F5\\|Network Interfaces\\|1.1:Incoming Bytes";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {
			
			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457097_F5_For_Network_Interfaces");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457098_F5_For_Pools() {
		LOGGER.info("This is to verify _For_Pools");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|Pools\\|Common_TIXweb:Total Connections";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457098_F5_For_Pools");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457099_F5_For_Rules() {
		LOGGER.info("This is to verify _For_Rules");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);

		startLinuxUMA();

		harvestWait(60);

		String metricExpression2 = "F5\\|HTTP\\|Common_http:Total 2xx Response";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457099_F5_For_Rules");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457093_F5_For_Virtual_Server() {

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|Virtual Servers\\|Common_TIXweb:Usage Ratio";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457093_F5_For_Virtual_Server");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457100_F5_For_TCP() {
		LOGGER.info("This is to verify _ClientSSLProfile");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);
		LOGGER.info("This is to verify _ClientSSLProfile123");
		LOGGER.info("This is to verify _ClientSSLProfile");

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|TCP\\|Common_apm-forwarding-client-tcp:Connects";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457100_F5_For_TCP");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_454097_F5_For_HTTP() {
		LOGGER.info("This is to verify _ClientSSLProfile");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|HTTP\\|Common_http:Total 2xx Response";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans_For_HTTP");

		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_457101_F5_For_Server_SSL_Profile() {
		LOGGER.info("This is to verify _ClientSSLProfile");
		LOGGER.info("TOMCAT_ROLE_ID.........." + TOMCAT_ROLE_ID);

		startLinuxUMA();

		harvestWait(30);

		String metricExpression2 = "F5\\|Server SSL Profile\\|Common_apm-default-serverssl:Max Connections";

		String command = "get historical data from agents matching \""
				+ agentExpression + "\" and metrics matching \""
				+ metricExpression2 + "\" for past " + 1 + " minutes";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Unable to reach F5, either environment details provided are not valid or environment is down  ",
				tempResult2.contains("-1"));

		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, UMA_F5_EXT_LOG,
					"[ERROR]");
		} finally {

			renameLogWithTestCaseID(UMA_F5_EXT_LOG, TOMCAT_MACHINE_ID,
					"ALM_457101_F5_For_Server_SSL_Profile");

		}
	}

	@AfterMethod
	public void tearDown() {
		try {

			/*
			 * checkFileExistenceOneTimeCounter(envProperties,
			 * TOMCAT_MACHINE_ID, tomcatLogFile);
			 * LOGGER.info("entered into tearDown try method");
			 */
			if (linux) {
				stopLinuxUMA();
			} else {
				stopUMA();
			}
			
			/*
			 * LOGGER.info("counter value :"+counter);
			 * renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
			 * "testFailed" + counter); counter++;
			 * LOGGER.info("counter value increased");
			 */

		} catch (Exception e) {
			// Do nothing
			LOGGER.info("Tomcat log not available do nothing");

		}

	}

	public void checkFileExistenceOneTimeCounter(
			EnvironmentPropertyContext envProps, String machineId,
			String filePath) {
		CheckFileExistenceFlowOneTimeCounterContext checkFileExistenceFlowOneTimeCounterContext = new CheckFileExistenceFlowOneTimeCounterContext.Builder()
				.filePath(filePath).build();
		runFlowByMachineId(machineId,
				CheckFileExistenceFlowOneTimeCounter.class,
				checkFileExistenceFlowOneTimeCounterContext);
	}

	public String currentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date date = new Date();
		String date1 = dateFormat.format(date);
		return date1;

	}

	/*
	 * public void renameLogFiles(String testIdName){ try{ stopAgent();
	 * LOGGER.info("Tomcat Stopped Successfully"); }catch(Exception e){
	 * LOGGER.info("Tomcat Stopping Failed"); }
	 * 
	 * finally{ renameLogWithTestCaseID(tomcatAgentLogFile,
	 * TOMCAT_MACHINE_ID,testIdName); renameLogWithTestCaseID(tomcatLogFile,
	 * TOMCAT_MACHINE_ID,testIdName); } }
	 */

	/*
	 * public void startUMAWindowsAgent(){ List<String> argumentsStart = new
	 * ArrayList<>(); //argumentsStart.add(UMA_WIN_FILE);
	 * argumentsStart.add(UMA_WIN_START_COMMAND);
	 * LOGGER.info("UMA_WIN_FILE..."+UMA_WIN_FILE); RunCommandFlowContext
	 * umaStart =new RunCommandFlowContext.Builder(UMA_WIN_FILE)
	 * .args(argumentsStart) .build();
	 * 
	 * executeUmaCommand(umaStart); }
	 * 
	 * public void stopUMAWindowsAgent(){
	 * 
	 * List<String> argumentsStop = new ArrayList<>(); //
	 * argumentsStop.add(UMA_SH_FILE); argumentsStop.add(UMA_WIN_STOP_COMMAND);
	 * LOGGER.info("UMA_WIN_FILE........ "+UMA_WIN_FILE); RunCommandFlowContext
	 * umaStop =new RunCommandFlowContext.Builder(UMA_WIN_FILE)
	 * .args(argumentsStop) .build();
	 * 
	 * executeUmaCommand(umaStop); }
	 */

	public void installUMA() {
		List<String> argumentsInstall = new ArrayList<>();
		argumentsInstall.add(UMA_WIN_BAT_FILE);
		argumentsInstall.add(UMA_INSTALL_COMMAND);
		umaInstall = new RunCommandFlowContext.Builder("bat").args(
				argumentsInstall).build();

		executeUmaCommand(umaInstall);
	}

	public void startUMA() {
		List<String> argumentsStart = new ArrayList<>();
		argumentsStart.add(UMA_WIN_BAT_FILE);
		argumentsStart.add(UMA_START_COMMAND);
		umaStart = new RunCommandFlowContext.Builder("bat")
				.args(argumentsStart).build();

		executeUmaCommand(umaStart);
	}

	public void stopUMA() {

		List<String> argumentsStop = new ArrayList<>();
		argumentsStop.add(UMA_WIN_BAT_FILE);
		argumentsStop.add(UMA_STOP_COMMAND);
		umaStop = new RunCommandFlowContext.Builder("bat").args(argumentsStop)
				.build();

		executeUmaCommand(umaStop);
	}

	public void installLinuxUMA() {
		List<String> argumentsInstall = new ArrayList<>();
		argumentsInstall.add(UMA_LINUX_SH_FILE);
		argumentsInstall.add(UMA_INSTALL_COMMAND);
		umaInstall = new RunCommandFlowContext.Builder("sh").args(
				argumentsInstall).build();

		executeUmaCommand(umaInstall);
	}

	public void startLinuxUMA() {
		List<String> argumentsStart = new ArrayList<>();
		argumentsStart.add(UMA_LINUX_SH_FILE);
		argumentsStart.add(UMA_START_COMMAND);
		umaStart = new RunCommandFlowContext.Builder("sh").args(argumentsStart)
				.build();

		executeUmaCommand(umaStart);
	}

	public void stopLinuxUMA() {

		List<String> argumentsStop = new ArrayList<>();
		argumentsStop.add(UMA_LINUX_SH_FILE);
		argumentsStop.add(UMA_STOP_COMMAND);
		umaStop = new RunCommandFlowContext.Builder("sh").args(argumentsStop)
				.build();

		executeUmaCommand(umaStop);
	}

	public String getDataUsingCLW(String metricExpression) {
		String metricData;
		String agentExpression = "(.*)\\|Common\\|Agent";

		metricData = clw.getLatestMetricValue(user, password, agentExpression,
				metricExpression, EMHost, Integer.parseInt(emPort), emLibDir);

		LOGGER.info("Metric data : " + metricData);

		return metricData;
	}

	public void executeUmaCommand(RunCommandFlowContext uma) {
		try {
			LOGGER.info("UMA_ROLE_ID... " + UMA_ROLE_ID);
			runFlowByMachineId(AgentControllabilityConstants.TOMCAT_MACHINE_ID,
					RunCommandFlow.class, uma);
		} catch (Exception e) {
			LOGGER.error("UMA command Already executed: Exception");
		}
	}

	public void checkError(EnvironmentPropertyContext envProperties,
			String tomcatMachineId, String tomcatAgentLogFile, String string) {

		try {
			isKeywordInFileOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
					tomcatAgentLogFile, "[ERROR]");
			Assert.assertTrue(false);
			LOGGER.info("[ERROR]'s displayed in log , failing the testcase");
		} catch (Exception e) {
			Assert.assertTrue(true);
			LOGGER.info("Message:::" + "No [ERROR]" + ":::: in agent log file");
		}
	}
}
