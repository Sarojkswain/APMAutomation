/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.functional;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.Util;
import com.ca.apm.tests.config.NodeJSProbeConfig;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Loggertests for node agent automation
 *
 * @author jinaa01
 */

@Test(groups = {"nodeagent", "logger" })
public class LoggingTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTests.class);
	String probeHome;

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		probeHome = envProperties.getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_PROBE_ROLE_ID, NodeJSProbeRole.Builder.ENV_NODEJS_PROBE_HOME);
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
		updateCollectorLogFileName(testMethodName);

	}

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
    @Test(groups = { "bat", "nodeagent", "logger" })
    public void testLogPathWithPattern() {
		String expectedProbeName = "myNodeApp";
        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		File logsFolder  = new File(probeHome + File.separator + "logs");

		if(logsFolder.exists()) {
			logsFolder.delete();
		}

		probeConfig.addProperty(NodeJSProbeConfig.LOG_PATH_PROPERTY_KEY, "../logs/Probe-${probeName}.log");
		probeConfig.addProperty(NodeJSProbeConfig.PROBE_NAME_PROPERTY_KEY, expectedProbeName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String expectedLogFilePath = probeHome + File.separator + "logs" + File.separator + "Probe-" + expectedProbeName + ".log";
		Assert.assertTrue(new File(expectedLogFilePath).exists(), "Log file does not exist at " + expectedLogFilePath);

    }

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "bat", "nodeagent", "logger" })
	public void testLoggerPermissions() {
		String expectedProbeName = "LoggerPermissions";
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		File logFilePath  = new File(probeHome + File.separator + "logs" +  File.separator + "Probe-" + expectedProbeName + ".log");

		if(logFilePath.exists()) {
			logFilePath.delete();
		}

		try {
			logFilePath.createNewFile();
			logFilePath.setReadOnly();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		probeConfig.addProperty(NodeJSProbeConfig.LOG_PATH_PROPERTY_KEY, "../logs/Probe-${probeName}.log");
		probeConfig.addProperty(NodeJSProbeConfig.PROBE_NAME_PROPERTY_KEY, expectedProbeName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		Assert.assertTrue(logFilePath.length() == 0, "Log File is not empty");

	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "bat", "nodeagent", "logger" })
	public void testAbsoluteLogPath() {

		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		updateProbeLogFileName(testMethodName);

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String expectedLogFilePath = probeConfig.getLogPath();
		Assert.assertTrue(new File(expectedLogFilePath).exists(), "Log file does not exist at " + expectedLogFilePath);

	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "bat", "nodeagent", "logger" })
	public void testLogLevelCase() {
		LOGGER.info("executing test {}#{} ", testSetName, testMethodName);

		probeConfig.addProperty(NodeJSProbeConfig.LOG_LEVEL_PROPERTY_KEY, "DeBuG");

		String expectedLogFilePath = probeConfig.getLogPath();
		File logFile = new File(expectedLogFilePath);
		if(logFile.exists()) {
			logFile.delete();
		}

		startCollectorAgentAndWaitConn();
		startAppAndWaitConn();

		String expectedLog = ".*debug.*";
		try {
			Assert.assertTrue(Util.findPattern(expectedLogFilePath, expectedLog), "Log file does not contain debug message");
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.error("exception while checking probe logs");
		}
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
        stopAppAndWaitDisc();
        stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
	    
	    probeConfig.updateProperty(NodeJSProbeConfig.PROBE_NAME_PROPERTY_KEY, "");
        resetNodeJsProbeConfigToOriginal();
		super.testClassTeardown();
	}
}
