package com.ca.apm.tests.agentextension;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.ca.apm.commons.flow.ClientMachineFlow;
import com.ca.apm.commons.flow.ClientMachineFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounter;
import com.ca.apm.commons.flow.CheckFileExistenceFlowOneTimeCounterContext;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.role.webapp.TomcatRole;

public class SpringBeanExtensionTest extends StandAloneEMOneTomcatTestsBase {

	public static final String EXTENSION_LOC_WIN = TasBuilder.WIN_SOFTWARE_LOC
			+ "extension" + TasBuilder.WIN_SEPARATOR;
			public static final String EXTENSION_LOC_LINUX = TasBuilder.LINUX_SOFTWARE_LOC
			+ "extension" + TasBuilder.LINUX_SEPARATOR;
			protected String extensionLocation;
      boolean linux;
	String tomcatagentInstall;
	String tomcatagentwebappInstall;
	String tomcatLogFile;
	CLWCommons clw = new CLWCommons();
	TestUtils utility = new TestUtils();
	static int counter = 1;
	String agentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
	String springTogglesFilePath = envProperties.getRolePropertyById(
			TOMCAT_ROLE_ID, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
			+ "/wily/core/config/spring-toggles.pbd";
	String url = "http://"
			+ tomcatHost
			+ ":"
			+ envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
					DeployTomcatFlowContext.ENV_TOMCAT_PORT)
			+ "/spring-petclinic/owners/new";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SpringBeanExtensionTest.class);

	TestApplications sbObject = new TestApplications();

	public SpringBeanExtensionTest() {
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
    		LOGGER.info("Inside windows");
    		linux=false; 
    		}
        else {
             linux=true;
			 }
		tomcatagentInstall = envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily";
		String date = currentDate();

		tomcatagentwebappInstall = envProperties.getRolePropertyById(
				TOMCAT_ROLE_ID, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/webapps";

		tomcatLogFile = envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
				DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/logs/catalina." + date + ".log";

	}

	@BeforeClass(alwaysRun = true)
	public void initialize() {

		LOGGER.info("Initialize begins here");

		System.out.println("Invoked testmethod in extensionjava");

		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			extensionLocation = EXTENSION_LOC_WIN;
			System.out.println("inside windows");
		} else {
			extensionLocation = EXTENSION_LOC_LINUX;
			System.out.println("inside linux");
		}
		copyDir(extensionLocation + "/wily", tomcatagentInstall,
				envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));
		replaceProp(
				"introscope.autoprobe.directivesFile=tomcat-full.pbl,hotdeploy",
				"introscope.autoprobe.directivesFile=tomcat-full.pbl,hotdeploy,spring-bean.pbd,spring-toggles.pbd",
				envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID),
				tomcatagentProfileFile);

		String springTogglesFilePath = envProperties.getRolePropertyById(
				TOMCAT_ROLE_ID, DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
				+ "/wily/core/config/spring-toggles.pbd";

		replaceProp(
				"introscope.agent.remoteagentdynamicinstrumentation.enabled=false",
				"introscope.agent.remoteagentdynamicinstrumentation.enabled=true",
				TOMCAT_MACHINE_ID, tomcatagentProfileFile);
		replaceProp("#TurnOn: BeanFactoryTracing",
				"TurnOn: BeanFactoryTracing", TOMCAT_MACHINE_ID,
				springTogglesFilePath);

		renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID, "Oldlogs");
		copyDir(extensionLocation + "/webapps", tomcatagentwebappInstall,
				envProperties.getMachineIdByRoleId(TOMCAT_ROLE_ID));

		startEM(EM_ROLE_ID);
		startTomcatAgent(TOMCAT_ROLE_ID);
		checkTomcatStartupMessage();

		checkLogForMsg(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
				"Spring Monitoring Field Pack Version 3.3");

		stopAgent();
		try {
			isKeywordInFileOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
					tomcatAgentLogFile, "[ERROR]");
			Assert.assertTrue(false);
			LOGGER.info("[ERROR]'s displayed in log , failing the testcase");
		} catch (Exception e) {
			Assert.assertTrue(true);
			LOGGER.info("Message:::" + "No [ERROR]" + ":::: in agent log file");
		}

		renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
				"initializetest");
		renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
				"initializetest");

	}

	/*
	 * 
	 * This method covers all bean related test case
	 */
	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_454097_SpringBeans_For_InstrumentedBeanMethods() {
		LOGGER.info("This is to verify 454097_SpringBeans");

		startTomcatAgent(TOMCAT_ROLE_ID);
		checkTomcatStartupMessage();
		if(linux){
			executeClentFlow(url, "CommonBeanMethods");
		}else{
		sbObject.SpringValidatorApp(url);
		}
		
		harvestWait(60);

		String metricExpression2 = "Agent Stats\\|Sustainability\\|Spring Bean Detection:Instrumented Bean Methods";

		String tempResult2 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression2, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Spring Instrumented Bean Method metric didn't reported  ",
				tempResult2.contains("-1"));
		// closeBrowser();
		stopAgent();
		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
					"[ERROR]");
		} finally {
			renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
			renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
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

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_454097_SpringBeans_For_InstrumentedBeans() {
		LOGGER.info("This is to verify 454097_SpringBeans");

		startTomcatAgent(TOMCAT_ROLE_ID);
		checkTomcatStartupMessage();
		if(linux){
			executeClentFlow(url, "CommonBeanMethods");
		}else{
		sbObject.SpringValidatorApp(url);
		}
		//sbObject.SpringValidatorApp(url);
		harvestWait(60);
		// Agent Stats|Sustainability|Spring Bean Detection

		String metricExpression3 = "Agent Stats\\|Sustainability\\|Spring Bean Detection:Instrumented Beans";

		String tempResult3 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression3, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Spring Instrumented Beans metric didn't reported  ",
				tempResult3.contains("-1"));

		// closeBrowser();
		stopAgent();
		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
					"[ERROR]");
		} finally {
			renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
			renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_454097_SpringBeans_For_SkippedBeans() {
		LOGGER.info("This is to verify 454097_SpringBeans");

		startTomcatAgent(TOMCAT_ROLE_ID);
		checkTomcatStartupMessage();
		if(linux){
			executeClentFlow(url, "CommonBeanMethods");
		}else{
		sbObject.SpringValidatorApp(url);
		}
		//sbObject.SpringValidatorApp(url);
		harvestWait(60);
		// Agent Stats|Sustainability|Spring Bean Detection

		String metricExpression4 = "Agent Stats\\|Sustainability\\|Spring Bean Detection:Skipped Beans";

		String tempResult4 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression4, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse("Spring Skipped Beans metric didn't reported  ",
				tempResult4.contains("-1"));

		// closeBrowser();
		stopAgent();
		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
					"[ERROR]");
		} finally {
			renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
			renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_454097_SpringBeans_BeansToInstrument() {
		LOGGER.info("This is to verify 454097_SpringBeans");

		startTomcatAgent(TOMCAT_ROLE_ID);
		checkTomcatStartupMessage();
		if(linux){
			executeClentFlow(url, "CommonBeanMethods");
		}else{
		sbObject.SpringValidatorApp(url);
		}
		//sbObject.SpringValidatorApp(url);
		harvestWait(60);
		// Agent Stats|Sustainability|Spring Bean Detection

		String metricExpression1 = "Agent Stats\\|Sustainability\\|Spring Bean Detection:Beans To Instrument";

		String tempResult1 = clw.getLatestMetricValue(user, password,
				agentExpression, metricExpression1, EMHost,
				Integer.parseInt(emPort), emLibDir);

		Assert.assertFalse(
				"Spring Beans to Instrument metric didn't reported  ",
				tempResult1.contains("-1"));

		// closeBrowser();
		stopAgent();
		try {
			checkError(envProperties, TOMCAT_MACHINE_ID, tomcatAgentLogFile,
					"[ERROR]");
		} finally {
			renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
			renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
					"ALM_454097_SpringBeans");
		}
	}

	@Test(groups = { "BAT" }, enabled = true)
	public void verify_ALM_SpringBeansHttpsTest() {
		stopAgent();
		stopEM();
		LOGGER.info("This is to verify_SpringBeansHttpsTest");
		List<String> roleIds = new ArrayList<String>();
		roleIds.add(EM_ROLE_ID);
		roleIds.add(TOMCAT_ROLE_ID);
		backupConfigDir(roleIds);
		roleIds.clear();
		roleIds.add(EM_ROLE_ID);
		updateEMPropertiesForHTTPSWithoutSecureAttributes(roleIds);

		setAgentHttpsUrl(tomcatagentProfileFile, TOMCAT_MACHINE_ID, EMHost,
				emPort, EMHost, "8444");
				startEM();
				
				startTomcatAgent(TOMCAT_ROLE_ID);
		//startAgent();
		
harvestWait(60);
		try {
			isKeywordInFileOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
					tomcatAgentLogFile,
					"Connected controllable Agent to the Introscope Enterprise Manager");
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.assertTrue(false);
		}

		stopAgent();

		renameLogWithTestCaseID(tomcatAgentLogFile, TOMCAT_MACHINE_ID,
				"ALM_SpringBeansHttpsTest");
		renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
				"ALM_SpringBeansHttpsTest");

	}

	@AfterMethod
	public void tearDown() {
		try {

			checkFileExistenceOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
					tomcatLogFile);
			LOGGER.info("entered into tearDown try method");
			stopAgent();
			renameLogWithTestCaseID(tomcatLogFile, TOMCAT_MACHINE_ID,
					"testFailed" + counter);
			counter++;
			LOGGER.info("counter value increated");

		} catch (Exception e) {
			// Do nothing
			LOGGER.info("Tomcat log not available do nothing");

		}

	}

	@AfterClass
	public void replacingAgntProperties() {
		replaceProp(
				"introscope.agent.remoteagentdynamicinstrumentation.enabled=true",
				"introscope.agent.remoteagentdynamicinstrumentation.enabled=false",
				TOMCAT_MACHINE_ID, tomcatagentProfileFile);
		replaceProp("TurnOn: BeanFactoryTracing",
				"#TurnOn: BeanFactoryTracing", TOMCAT_MACHINE_ID,
				springTogglesFilePath);
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

	/*
	 * public void closeBrowser() {
	 * 
	 * ArrayList<String>handleCount = new
	 * ArrayList<String>(fd.getWindowHandles()); for(int i=0;
	 * i<handleCount.size();i++) { fd.switchTo().window(handleCount.get(i));
	 * fd.close();
	 * 
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	public void startTomcatAgent(String tomcatRoleId) {
		try {
			runSerializedCommandFlowFromRole(tomcatRoleId,
					TomcatRole.ENV_TOMCAT_START);
			// harvestWait(60);

		} catch (Exception e) {
			LOGGER.error("Already started");
		}
	}

	public void checkTomcatStartupMessage() {

		System.out.println(tomcatLogFile);
		boolean message = false;
		while (!message)
			try {
				isKeywordInFileOneTimeCounter(envProperties, TOMCAT_MACHINE_ID,
						tomcatLogFile, "Server startup in");
				message = true;
				LOGGER.info("Tomcat started successfully ");
				break;

			} catch (Exception e) {
				message = false;
				LOGGER.info("tomcat not yet started");
			}

	}

	public String currentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date date = new Date();
		String date1 = dateFormat.format(date);
		return date1;

	}
	public void executeClentFlow(String url, String testId) {

		try {
			ClientMachineFlowContext clientMachine = new ClientMachineFlowContext.Builder()
					.url(url).testId(testId).build();
			System.out.println("Started runFlowByMachineId method!!");
			runFlowByMachineId("clientMachine", ClientMachineFlow.class,
					clientMachine,
					FlowConfig.FlowConfigBuilder.TIME_UNIT_DEFAULT, 120);

			System.out.println("Completed runFlowByMachineId method!!");

		} catch (Exception e) {
			System.out
					.println("Completed runFlowByMachineId method!! with Exception");
			e.printStackTrace();
		}

	}

}
