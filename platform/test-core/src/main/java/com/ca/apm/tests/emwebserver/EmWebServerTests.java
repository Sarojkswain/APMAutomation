package com.ca.apm.tests.emwebserver;

import java.util.Calendar;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.testbed.StandaloneOnlyEMLinuxTestbed;
import com.ca.apm.tests.testbed.StandaloneOnlyEMWindowsTestbed;
import com.ca.tas.builder.TasBuilder;

public class EmWebServerTests extends BaseAgentTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EmWebServerTests.class);
	protected final String emRoleId;
	protected final String emMachineId;
	protected String emhost;
	protected String emLogFile;
	protected String user;
	protected String emPropFile;
	protected String emPort;
	protected String emWebport;
	protected String emHome;
	protected String appDeployLogMessage;
	protected String appDirectory;
	protected String emJettyVersion="Path to a Jetty 6.1.25 XML";
	protected String negativeValueMsg = "[WARN] [main] [Manager.EMWebServer] Property \"introscope.enterprisemanager.webserver.max.threads\" has value \"-100\" where a positive integer value is expected";
	protected String zeroValueMsg = "[WARN] [main] [Manager.EMWebServer] Property \"introscope.enterprisemanager.webserver.max.threads\" has value \"0\" where a positive integer value is expected";
	protected String blankValueMsg = "[WARN] [main] [Manager.EMWebServer] Property \"introscope.enterprisemanager.webserver.max.threads\" has value \"\" where an integer value is expected";
	protected String alphanumericValueMsg = "[WARN] [main] [Manager.EMWebServer] Property \"introscope.enterprisemanager.webserver.max.threads\" has value \"abc\" where an integer value is expected";
	protected String floatingValueMsg = "[WARN] [main] [Manager.EMWebServer] Property \"introscope.enterprisemanager.webserver.max.threads\" has value \"100.12\" where an integer value is expected";
	protected String defaultValueMsg = "[INFO] [main] [Manager.EMWebServer] EM max server threads: 100";

	public EmWebServerTests() {

		boolean isWindows = Os.isFamily(Os.FAMILY_WINDOWS);

		emRoleId = isWindows ? StandaloneOnlyEMWindowsTestbed.EM_ROLE_ID
				: StandaloneOnlyEMLinuxTestbed.EM_ROLE_ID;
		emMachineId = isWindows ? StandaloneOnlyEMWindowsTestbed.EM_MACHINE_ID
				: StandaloneOnlyEMLinuxTestbed.EM_MACHINE_ID;

		emPort = envProperties.getRolePropertiesById(emRoleId).getProperty(
				"emPort");
		emWebport = envProperties.getRolePropertiesById(emRoleId).getProperty(
				"wvEmWebPort");;

		emhost = envProperties.getMachineHostnameByRoleId(emRoleId);

		emHome = envProperties.getRolePropertyById(emRoleId,
				DeployEMFlowContext.ENV_EM_INSTALL_DIR);

		emPropFile = envProperties.getRolePropertyById(emRoleId,
				DeployEMFlowContext.ENV_EM_CONFIG_FILE);

		emLogFile = envProperties.getRolePropertyById(emRoleId,
				DeployEMFlowContext.ENV_EM_LOG_FILE);
		LOGGER.info("EM DETAISL>>>>>>>>>>>>>>>>>>" + emhost + emWebport);

		appDeployLogMessage = isWindows ? "[INFO] [TimerBean] [Manager.EMWebServer] Deployed new web application: webapps\\"
				: "[INFO] [TimerBean] [Manager.EMWebServer] Deployed new web application: webapps/";
		appDirectory = isWindows ? TasBuilder.WIN_SOFTWARE_LOC
				: TasBuilder.LINUX_SOFTWARE_LOC;
	}

	@BeforeSuite(alwaysRun = true)
	public void initiate() {
		ApmbaseUtil.deleteFile(emHome + "/webapps/myapp1.war");
		startEM(emRoleId);
		replaceProp("log4j.logger.Manager=INFO,console,logfile",
				"log4j.logger.Manager=INFO,logfile", emMachineId, emPropFile);
		checkLogForMsg(envProperties, emMachineId, emLogFile,
				"Detected hot config change to");
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_280556_TT_47266_EM_WebServer_Hot_deploy_from_webapps_doesnot_work() {

		String appDownloadedPath = appDirectory + "sampleApps/myapp1.war";
		String appUrl = "http://" + emhost + ":" + emWebport
				+ "/myapp1/index.htm";
		ApmbaseUtil.copyFile(appDownloadedPath, emHome + "/webapps/myapp1.war");
		checkLogForMsg(envProperties, emMachineId, emLogFile,
				appDeployLogMessage + "myapp1.war");
		harvestWait(30);
		String result = callURLAndReturnResponse(appUrl);
		Assert.assertTrue(result.contains("Hello World!"));
		ApmbaseUtil.deleteFile(emHome + "/webapps/myapp1.war");

	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_305718_Run_simple_WebApp_TT41233() {

		Calendar calendar = Calendar.getInstance();
		String appDownloadedPath = appDirectory + "sampleApps/helloworld.war";
		String appUrl = "http://" + emhost + ":" + emWebport
				+ "/helloworld/hi.jsp";
		ApmbaseUtil.copyFile(appDownloadedPath, emHome
				+ "/webapps/helloworld.war");
		harvestWait(30);
		checkLogForMsg(envProperties, emMachineId, emLogFile,
				appDeployLogMessage + "helloworld.war");
		String result = callURLAndReturnResponse(appUrl);
		Assert.assertTrue(result.contains(calendar.getTime().toString()
				.substring(0, 10)));
		ApmbaseUtil.deleteFile(emHome + "/webapps/helloworld.war");

	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_280557_Test_webapps_after_EMRestart_TT42074() {

		String appsDirectory = appDirectory + "sampleApps/helloworld.war";
		String appUrl = "http://" + emhost + ":" + emWebport
				+ "/helloworld/hi.jsp";
		ApmbaseUtil.copyFile(appsDirectory, emHome + "/webapps/helloworld.war");
		harvestWait(30);
		replaceProp("log4j.logger.Manager=INFO,logfile",
				"log4j.logger.Manager=INFO,console,logfile", emMachineId,
				emPropFile);
		restartEM(emRoleId);

		String result = callURLAndReturnResponse(appUrl);
		Assert.assertTrue(result.contains("Hello, World"));
		ApmbaseUtil.deleteFile(emHome + "/webapps/helloworld.war");
	}

	@Test(groups = { "Full Regression" }, enabled = true)
	public void Verify_ALM_449142_Check_Jetty_server_version() {
		checkForKeyword(envProperties, emMachineId, emPropFile,
				emJettyVersion, true);
	}

	@Test(groups = { "Smoke Regression" }, enabled = true)
	public void Verify_ALM_392312_Invalid_webserver_max_threads_value() {

		replaceProp("#introscope.enterprisemanager.webserver.max.threads=100",
				"introscope.enterprisemanager.webserver.max.threads=-100",
				emMachineId, emPropFile);
		restartEM(emRoleId);

		checkLogForMsg(envProperties, emMachineId, emLogFile, negativeValueMsg);
		checkLogForMsg(envProperties, emMachineId, emLogFile, defaultValueMsg);
		LOGGER.info("####################################### scenario 1 Passed ################################");

		replaceProp("introscope.enterprisemanager.webserver.max.threads=-100",
				"introscope.enterprisemanager.webserver.max.threads=0",
				emMachineId, emPropFile);
		restartEM(emRoleId);

		checkLogForMsg(envProperties, emMachineId, emLogFile, zeroValueMsg);
		checkLogForMsg(envProperties, emMachineId, emLogFile, defaultValueMsg);
		LOGGER.info("####################################### scenario 2 Passed ################################");

		replaceProp("introscope.enterprisemanager.webserver.max.threads=0",
				"introscope.enterprisemanager.webserver.max.threads=",
				emMachineId, emPropFile);
		restartEM(emRoleId);

		checkLogForMsg(envProperties, emMachineId, emLogFile, blankValueMsg);
		checkLogForMsg(envProperties, emMachineId, emLogFile, defaultValueMsg);
		LOGGER.info("####################################### scenario 3 Passed ################################");

		replaceProp("introscope.enterprisemanager.webserver.max.threads=",
				"introscope.enterprisemanager.webserver.max.threads=abc",
				emMachineId, emPropFile);
		restartEM(emRoleId);

		checkLogForMsg(envProperties, emMachineId, emLogFile,
				alphanumericValueMsg);
		checkLogForMsg(envProperties, emMachineId, emLogFile, defaultValueMsg);

		LOGGER.info("####################################### scenario 4 Passed ################################");

		replaceProp("introscope.enterprisemanager.webserver.max.threads=abc",
				"introscope.enterprisemanager.webserver.max.threads=100.12",
				emMachineId, emPropFile);
		restartEM(emRoleId);

		checkLogForMsg(envProperties, emMachineId, emLogFile, floatingValueMsg);
		checkLogForMsg(envProperties, emMachineId, emLogFile, defaultValueMsg);
		LOGGER.info("####################################### scenario 5 Passed ################################");

		replaceProp("introscope.enterprisemanager.webserver.max.threads=abc",
				"#introscope.enterprisemanager.webserver.max.threads=100",
				emMachineId, emPropFile);
	}

	@AfterSuite(alwaysRun = true)
	public void cleanUP() {
		replaceProp("log4j.logger.Manager=INFO,logfile",
				"log4j.logger.Manager=INFO,console,logfile", emMachineId,
				emPropFile);
		stopEM(emRoleId);

	}
}
