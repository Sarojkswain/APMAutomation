package com.ca.apm.tests.functional;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.common.mockem.IAgentStatusQueryHelper;
import com.ca.apm.automation.utils.configuration.JsonConfiguration;
import com.ca.apm.tests.config.NodeJSProbeConfig;
import com.ca.apm.tests.config.TixChangeAppConfig;
import com.ca.apm.tests.flow.DeployCollectorAgentFlowContext.Builder.CollectorAgentProperty;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.FullAgentNameExpr;
import com.ca.apm.tests.utils.IAgentNameExpr;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;
import com.wily.introscope.spec.metric.AgentName;

/**
 * Tests for verifying different options to set nodejs probe name
 * 
 * @author sinka08
 *
 */
@Test(groups = { "nodeagent", "probenaming" })
public class ProbeNamingTest extends BaseNodeAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProbeNamingTest.class);
	public static final IAgentNameExpr ALL_AGENT_EXPR = new FullAgentNameExpr(".*", ".*", ".*");
	public static final String PROBENAME_ENV_KEY = "CA_APM_PROBENAME";
	private static String defaultStartupScriptName = "server.js";

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		defaultStartupScriptName = new File(tixChangeConfig.getStartupScriptPath()).getName();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorAndProbeLogFileName(testSetName + "-" + testMethodName);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" }, priority = 0)
	public void testAppScriptBasedProbeName() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		File pkgFile = new File(tixChangeConfig.getServerDir(), "package.json");
		FileUtils.deleteQuietly(pkgFile);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String scriptFileName = new File(tixChangeConfig.getStartupScriptPath()).getName();
		String scriptName = scriptFileName.substring(0, scriptFileName.indexOf('.'));

		boolean matches = probeNameMatches(getProbeAgentName(scriptName));

		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();

		Assert.assertTrue(matches, String.format(
		        "Probe name does not match Application startup script: '%s'", scriptName));
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" }, priority = 1)
	public void testModuleNameBasedProbeName() throws IOException {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String pkgFileName = "package.json";
		String key = "name";
		File mainPkgFile = new File(tixChangeConfig.getHome(), pkgFileName);
		// package.json should be under same dir as startup script
		FileUtils.copyFileToDirectory(mainPkgFile, new File(tixChangeConfig.getServerDir()));

		File newPkgFile = new File(tixChangeConfig.getServerDir(), pkgFileName);
		JsonConfiguration file = new JsonConfiguration(newPkgFile);
		String expectedProbeName = file.getString(key);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));

		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();

		Assert.assertTrue(
		        matches,
		        String.format(
		                "Probe name does not match with value: '%s' set for key: '%s' in module's package.json",
		                expectedProbeName, key));
		checkErrorInLogs();

		FileUtils.deleteQuietly(newPkgFile);
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "bat" }, priority = 2)
	public void testConfigBasedProbeName() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String expectedProbeName = "myNodeApp";
		// Name can contain environment variables with ${} notation
		String[] keys = { "foo", "bar" };
		String[] values = { "my", "App" };
		probeConfig.updateProperty(NodeJSProbeConfig.PROBE_NAME_PROPERTY_KEY, "${foo}Node${bar}");

		startCollectorAgentAndWaitConn();
				
		startAppWithEnvSettings(keys, values);

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));
		probeConfig.updateProperty(NodeJSProbeConfig.PROBE_NAME_PROPERTY_KEY, "");

		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();

		Assert.assertTrue(matches, String.format(
		        "Probe name does not match with value: '%s' set for key: '%s' in config.json",
		        expectedProbeName, NodeJSProbeConfig.PROBE_NAME_PROPERTY_KEY));
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "smoke" }, priority = 3)
	public void testConfigEnvKeyBasedProbeName() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String expectedProbeName = "node-on-port-3000";
		String envKey = "server-name";
		probeConfig.updateProperty(NodeJSProbeConfig.PROBE_NAME_ENV_PROPERTY_KEY, envKey);

		startCollectorAgentAndWaitConn();
		// startAppAndWaitConn();
		startAppWithEnvSettings(envKey, expectedProbeName);

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));
		probeConfig.updateProperty(NodeJSProbeConfig.PROBE_NAME_ENV_PROPERTY_KEY, "");

		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();

		Assert.assertTrue(
		        matches,
		        String.format(
		                "Probe name does not match with value: '%s' of environment variable: '%s' specified in config.json",
		                expectedProbeName, envKey));
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" }, priority = 3)
	public void testEnvVariableBasedProbeName() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String expectedProbeName = "nodeApp-1";

		startCollectorAgentAndWaitConn();
		startAppWithEnvSettings(PROBENAME_ENV_KEY, expectedProbeName);

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));

		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();

		Assert.assertTrue(matches, String.format(
		        "Probe name does not match with value: '%s' of environment variable: '%s'",
		        expectedProbeName, PROBENAME_ENV_KEY));
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" }, priority = 5)
	public void testRequireBasedProbeName() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String machineId = envProperties
		        .getMachineIdByRoleId(NodeJSAgentTestbed.TIXCHANGE_PROBE_ROLE_ID);

		// create backup of startup script
		createBackupOfStartupScript(tixChangeConfig);

		@SuppressWarnings("unused")
        String existingProbeHook = envProperties.getRolePropertyById(
		        NodeJSAgentTestbed.TIXCHANGE_PROBE_ROLE_ID, NodeJSProbeRole.ENV_PROBE_REQUIRE_STMT);
		String existingProbeHookPrefix = "var probe";
		Map<String, String> replaceData = Collections.singletonMap(existingProbeHookPrefix, "//"
		        + existingProbeHookPrefix);

		// comment out existing require statement
		FileModifierFlowContext replaceContext = new FileModifierFlowContext.Builder().replace(
		        tixChangeConfig.getStartupScriptPath(), replaceData).build();
		runFlowByMachineId(machineId, FileModifierFlow.class, replaceContext);

		String expectedProbeName = "probe-name-test-require";
		int index = 0;
		int port = Integer.valueOf(umAgentConfig.getProperty(CollectorAgentProperty.TCP_PORT
		        .getKey()));
		// new require statement to be inserted in beginning of nodejs app
		// startup script
		String newProbeHook = String.format(
		        "var probe = require('ca-apm-probe').start('%s', %d, '%s');", "localhost", port,
		        expectedProbeName);
		FileModifierFlowContext insertContext = new FileModifierFlowContext.Builder().insertAt(
		        tixChangeConfig.getStartupScriptPath(), index,
		        Collections.singletonList(newProbeHook)).build();
		runFlowByMachineId(machineId, FileModifierFlow.class, insertContext);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));

		// revert to backup
		revertStartupScriptToOriginal(tixChangeConfig);
		stopAppAndWaitDisc();
		stopCollectorAgentAndWaitDisc();

		Assert.assertTrue(
		        matches,
		        String.format(
		                "Probe name does not match with value set in require statement: '%s' of start up script: '%s'",
		                newProbeHook, tixChangeConfig.getStartupScriptPath()));
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" }, priority = 6)
	public void testDirBasedProbeName() throws IOException {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String newServerDirName = "server1";
		String newStartupScriptName = "index.js";
		File exServerDir = new File(tixChangeConfig.getServerDir());
		File newServerDir = new File(exServerDir.getParent(), newServerDirName);
		FileUtils.copyDirectory(exServerDir, newServerDir);
		FileUtils.copyFile(new File(newServerDir, defaultStartupScriptName), new File(newServerDir,
		        newStartupScriptName));

		String expectedProbeName = newServerDirName;

		startCollectorAgentAndWaitConn();
		startNodeAppWithScript(newServerDirName, newStartupScriptName);

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));

		stopNodeAppWithScript(newServerDirName, newStartupScriptName);
		stopCollectorAgentAndWaitDisc();
		FileUtils.deleteDirectory(newServerDir);

		Assert.assertTrue(matches, String.format(
		        "Probe name does not match with startup script dir name: '%s'", expectedProbeName));
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "sinka08", snapshotPolicy = SnapshotPolicy.ALWAYS, snapshot = SnapshotMode.LIVE)
	@Test(groups = { "full" }, priority = 6)
	public void testDefaultProbeName() throws IOException {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		String newServerDirName = "index";
		String newStartupScriptName = "index.js";
		File exServerDir = new File(tixChangeConfig.getServerDir());
		File newServerDir = new File(exServerDir.getParent(), newServerDirName);
		FileUtils.copyDirectory(exServerDir, newServerDir);
		FileUtils.copyFile(new File(newServerDir, defaultStartupScriptName), new File(newServerDir,
		        newStartupScriptName));

		// when we can not determine the probe name based on user settings or
		// app configuration; we fallback to this name
		String expectedProbeName = "NodeApplication";

		startCollectorAgentAndWaitConn();
		startNodeAppWithScript(newServerDirName, newStartupScriptName);

		boolean matches = probeNameMatches(getProbeAgentName(expectedProbeName));

		stopNodeAppWithScript(newServerDirName, newStartupScriptName);
		stopCollectorAgentAndWaitDisc();
		FileUtils.deleteDirectory(newServerDir);

		Assert.assertTrue(matches, String.format(
		        "Probe name does not match with default name: '%s'", expectedProbeName));
	}

	private boolean probeNameMatches(String expectedName) {
		IAgentStatusQueryHelper[] helpers = mockEm.getReqProcessor(ALL_AGENT_EXPR)
		        .getAgentStatusQueryHelpers();

		LOGGER.info("Expected probe name: " + expectedName);

		for (IAgentStatusQueryHelper h : helpers) {
			AgentName fqAgentName = h.getFQAgentName();
			LOGGER.info("Agent info: " + fqAgentName);

			if (fqAgentName != null && fqAgentName.getAgentName().equals(expectedName)) {
				return true;
			}
		}

		return false;
	}

	private static void createBackupOfStartupScript(TixChangeAppConfig appConfig) {
		File backupDir = new File(appConfig.getBackupDir());
		File scriptFile = new File(appConfig.getStartupScriptPath());

		try {
			File backupFile = new File(backupDir, scriptFile.getName());

			if (!backupFile.exists()) {
				FileUtils.copyFileToDirectory(scriptFile, backupDir, true);
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

	private static void revertStartupScriptToOriginal(TixChangeAppConfig appConfig) {
		File backupFile = new File(appConfig.getBackupDir(), new File(
		        appConfig.getStartupScriptPath()).getName());

		if (backupFile.exists()) {
			try {
				File currentFile = new File(appConfig.getStartupScriptPath());

				if (currentFile.exists()) {
					currentFile.delete();
				}
				FileUtils.copyFile(backupFile, currentFile, true);
			} catch (IOException e) {
				LOGGER.error("Error while reverting file {} to original file",
				        appConfig.getConfigFileName());
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			LOGGER.info("Backup file: {} does not exist", backupFile.getAbsoluteFile());
		}
	}

	private void startAppWithEnvSettings(String key, String value) {
		LOGGER.info(
		        "starting node application with special environment variable settings: key: {}, value: {}",
		        key, value);

		RunCommandFlowContext runCommandFlowContext = deserializeCommandFlowFromRole(
		        NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, TixChangeRole.ENV_TIXCHANGE_START);
		runCommandFlowContext.getEnvironment().put(key, value);
		String machineId = envProperties.getMachineIdByRoleId(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID);
		runCommandFlowByMachineId(machineId, runCommandFlowContext);

		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	private void startAppWithEnvSettings(String[] keys, String[] values) {
		LOGGER.info(
		        "starting node application with special environment variable settings:");

		RunCommandFlowContext runCommandFlowContext = deserializeCommandFlowFromRole(
		        NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, TixChangeRole.ENV_TIXCHANGE_START);
		for (int i = 0; i < keys.length && i < values.length; i++) {
			runCommandFlowContext.getEnvironment().put(keys[i], values[i]);
			LOGGER.info(" key: {}, value: {}", keys[i], values[i]);
		}
		String machineId = envProperties.getMachineIdByRoleId(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID);
		runCommandFlowByMachineId(machineId, runCommandFlowContext);

		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}	
	
	private void startNodeAppWithScript(String serverDirName, String startupScriptName) {
		String newStartScriptPath = tixChangeConfig.getHome() + File.separator + serverDirName
		        + File.separator + startupScriptName;

		LOGGER.info("starting node application using startup script: {} ", newStartScriptPath);

		RunCommandFlowContext runCommandFlowContext = deserializeCommandFlowFromRole(
		        NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, TixChangeRole.ENV_TIXCHANGE_START);
		ArrayList<String> args = (ArrayList<String>) runCommandFlowContext.getArgs();

		// Arrays.asList("start", "-l", logFile, "-a", serverExecPath)
		int index = 4;
		args.remove(index);
		args.add(index, newStartScriptPath);
		String machineId = envProperties.getMachineIdByRoleId(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID);

		runCommandFlowByMachineId(machineId, runCommandFlowContext);

		Assert.assertTrue(isAgentConnected(NODE_AGENT_EXPR),
		        "nodejs agent is not connected to mockem");
	}

	private void stopNodeAppWithScript(String serverDirName, String startupScriptName) {
		String newStartScriptPath = tixChangeConfig.getHome() + File.separator + serverDirName
		        + File.separator + startupScriptName;

		LOGGER.info("stopping node application, script: {} ", newStartScriptPath);
		RunCommandFlowContext runCommandFlowContext = deserializeCommandFlowFromRole(
		        NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, TixChangeRole.ENV_TIXCHANGE_STOP);
		ArrayList<String> args = (ArrayList<String>) runCommandFlowContext.getArgs();

		// Arrays.asList("stop", serverExecPath)
		int index = 1;
		args.remove(index);
		args.add(index, newStartScriptPath);
		String machineId = envProperties.getMachineIdByRoleId(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID);

		runCommandFlowByMachineId(machineId, runCommandFlowContext);

		Assert.assertTrue(isAgentDisconnected(NODE_AGENT_EXPR), "nodejs agent failed to disconnect");
	}

	private String getProbeAgentName(String probeName) {
		return String.format("%s Agent", probeName);
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		super.testClassTeardown();
	}
}
