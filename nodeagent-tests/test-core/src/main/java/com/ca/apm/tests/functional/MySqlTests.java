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

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.automation.common.mockem.ExpectedElement;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.apm.tests.utils.TraceValidationData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;
/**
 * MySql tests for node agent automation
 *
 * @author jinaa01
 */

@Test(groups = {"nodeagent", "mysql" })
public class MySqlTests extends BaseNodeAgentTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MySqlTests.class);
	String pbdPath;
	File pbdFile = null;
	File backupFile = null;
	String pbdBackupPath = null;

	@BeforeClass(alwaysRun = true)
	public void testClassSetup() {
		super.testClassSetup();
		//startCollectorAgent();
	}

	@BeforeMethod(alwaysRun = true)
	public void executeBeforeMethod(Method method) {
		testMethodName = method.getName();
        updateCollectorAndProbeLogFileName(testMethodName);
	}

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
    @Test(groups = { "bat", "nodeagent", "mysql" })
    public void testBackendSqlMetrics() {

        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();

        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest/account/login?email=user1@users.com")
		        .setHttpMethod(HttpRequestMethod.POST)
		        .setNumberReqs(1).build();
		txnGen.start();

        String urlMetricPath = "Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT.*:Responses Per Interval";
        long waitTime = 60000;
        MetricAssertionData metricData = new MetricAssertionData.RegexMetricNameBuilder(urlMetricPath, 1).setDuration(waitTime).build();
        mockEm.processMetrics(metricData);

        checkErrorInLogs();
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "talma06")
    @Test(groups = { "bat", "nodeagent", "mysql" })
    public void testBackendSqlMetricsNoFrontend() {

        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
        tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);
        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();

        // make some http transactions
      
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase
		        + "/rest/account/login?email=user1@users.com")
		        .setHttpMethod(HttpRequestMethod.POST).setNumberReqs(1).build();
		txnGen.start();
        

        String urlMetricPath = "Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT.*:Responses Per Interval";
        long waitTime = 60000;
        MetricAssertionData metricData = new MetricAssertionData.RegexMetricNameBuilder(urlMetricPath, 1).setDuration(waitTime).build();
        mockEm.processMetrics(metricData);
        checkErrorInLogs();
    }    
    
    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
    @Test(groups = { "deep", "nodeagent", "errors", "mysql" })
    public void testBackendSqlTraces() {

        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();

        LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(), "testSqlError");

        
		// Set transaction trace filter
		ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
		        "dummy");
		mockEm.getReqProcessor(NODE_AGENT_EXPR).addTraceFilter(filter);

        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest/account/login?email=user1@users.com")
		        .setHttpMethod(HttpRequestMethod.POST)
		        .setNumberReqs(1).build();
		txnGen.start();

        final ArrayList<ExpectedElement[]> sqlScenario = new ArrayList<ExpectedElement[]>();

        ExpectedElement[] expectedTrace = {
                new ExpectedElement("Frontends|Apps|server|URLs|Default"),
                new ExpectedElement("Express|/rest/account/login|POST"),
                new ExpectedElement("Backends|nodetix on localhost-3306 (MySQL DB)|SQL|Dynamic|Query|SELECT ` EMAIL `, ` PASSWORD `, ` FIRSTNAME `, ` LASTNAME `, ` ADDR1 `, ` ADDR2 `, ` CITY `, ` STATE `, ` ZIP `, ` COUNTRY `, ` PHONE ` FROM ` ACCOUNT ` WHERE ` EMAIL ` = ? ORDER BY ` EMAIL ` LIMIT ?"),
				new ExpectedElement("Backends|nodetix on localhost-3306 (MySQL DB)")
        };

        sqlScenario.add(expectedTrace);

        TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
            public boolean validate(TransactionComponentData t) {
                if (!SIUtils.isErrorSnapshot(t) && !SIUtils.isStallSnapshot(t)) {
                    return SIUtils.containsAllComponents(t, sqlScenario);
                } else {
                    return false;
                }
            }
        }).build();

        mockEm.processTraces(traceValidationDataData);

        checkErrorInLogs();
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "talma06")
    @Test(groups = { "deep", "nodeagent", "errors", "mysql" })
    public void testBackendSqlTracesNoFrontend() {

        tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);

        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();

        LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(), "testSqlError");

        String STARTSWITH = "<STARTS>";
        
        // Set transaction trace filter
        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username",
                "dummy");
        mockEm.getReqProcessor(NODE_AGENT_EXPR).addTraceFilter(filter);

        // make some http transactions
 
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase
		        + "/rest/account/login?email=user1@users.com")
		        .setHttpMethod(HttpRequestMethod.POST).setNumberReqs(1).build();
		txnGen.start();
        

        final ArrayList<ExpectedElement[]> sqlScenario = new ArrayList<ExpectedElement[]>();

        ExpectedElement[] expectedTrace = {
                new ExpectedElement("Fragments|fragment|NoContext"),
                new ExpectedElement("Backends|nodetix on localhost-3306 (MySQL DB)|SQL|Dynamic|Query|SELECT ` EMAIL `, ` PASSWORD `, ` FIRSTNAME `, ` LASTNAME `, ` ADDR1 `, ` ADDR2 `, ` CITY `, ` STATE `, ` ZIP `, ` COUNTRY `, ` PHONE ` FROM ` ACCOUNT ` WHERE ` EMAIL ` = ? ORDER BY ` EMAIL ` LIMIT ?"),
                new ExpectedElement("Backends|nodetix on localhost-3306 (MySQL DB)")
        };

        sqlScenario.add(expectedTrace);

        TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
            public boolean validate(TransactionComponentData t) {
                if (!SIUtils.isErrorSnapshot(t) && !SIUtils.isStallSnapshot(t)) {
                    return SIUtils.containsAllComponents(t, sqlScenario);
                } else {
                    return false;
                }
            }
        }).build();

        mockEm.processTraces(traceValidationDataData);

        checkErrorInLogs();
    }    
    
	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "deep", "nodeagent", "errors", "mysql" })
	public void testSqlErrorSnapshot() {

		initializePbdFile();
		writeToPbd();

        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();

        LOGGER.info("executing test {}#{} ", this.getClass().getSimpleName(), "testSqlError");
//       / verfiyCollectorStartup();

        String STARTSWITH = "<STARTS>";

		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest/account/duplicate")
				.setNumberReqs(1).build();
		txnGen.start();

		final ArrayList<ExpectedElement[]> sqlScenario = new ArrayList<ExpectedElement[]>();

		ExpectedElement[] expectedTrace1 = {
				new ExpectedElement("Frontends|Apps|server|URLs|Default", STARTSWITH + "Backends|loopbackDAO: Node.Error: MySQLError: name: ValidationError , "),
				//new ExpectedElement("Express|{route}|{http_method}", STARTSWITH + "Backends|loopbackDAO: Node.Error: MySQLError: name: ValidationError , "),
				new ExpectedElement("Express|/rest/account/duplicate|GET", STARTSWITH + "Backends|loopbackDAO: Node.Error: MySQLError: name: ValidationError , "),
				new ExpectedElement("Backends|loopbackDAO", STARTSWITH + "Backends|loopbackDAO: Node.Error: MySQLError: name: ValidationError , ")
		};

		sqlScenario.add(expectedTrace1);

		TraceValidationData traceValidationDataData = new TraceValidationData.Builder(1, new RequestProcessor.ITransactionTraceValidator(){
			public boolean validate(TransactionComponentData t) {
                if (SIUtils.isErrorSnapshot(t) && !SIUtils.isStallSnapshot(t)) {
					return SIUtils.containsAllComponents(t, sqlScenario);
				} else {
					return false;
				}
			}
		}).build();

		mockEm.processTraces(traceValidationDataData);

		revertPbd();
		checkErrorInLogs();
	}

	@Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "jinaa01")
	@Test(groups = { "bat", "nodeagent", "errors", "mysql" })
	public void testSqlErrorMetrics() {

		initializePbdFile();
		writeToPbd();
        startCollectorAgentAndWaitConn();
        startAppAndWaitConn();

        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
        verifyCollectorStartup();

        HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + "/rest/account/duplicate")
				.setNumberReqs(1).build();
		txnGen.start();

		String metricPathPrefix = "Frontends|Apps|server";
		String urlMetricPath = metricPathPrefix + "|URLs|Default:Errors Per Interval";
		long waitTime = 60000;
		MetricAssertionData metricData = new MetricAssertionData.Builder(urlMetricPath, 2).setDuration(waitTime).build();
		mockEm.processMetrics(metricData);

		String keyword = "\"exc\":{\"class\":\"MySQLError\"";
		LogUtils util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);
		assertTrue(util.isKeywordInLog());

		revertPbd();
		checkErrorInLogs();
	}

	private void initializePbdFile() {
		pbdPath = umAgentConfig.getPbdPath();
		pbdBackupPath = pbdPath+".backup";
		pbdFile = new File(pbdPath);
		backupFile = new File(pbdBackupPath);
	}

	private void revertPbd() {

		if (backupFile.exists()){
			try {
				FileUtils.copy(backupFile, pbdFile);
				FileUtils.delete(pbdBackupPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeToPbd(){

		if (!backupFile.exists()){
			try {
				FileUtils.copyFile(pbdFile, backupFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String list[] = {"SetFlag: LoopbackDAOTracing\n", "TurnOn: LoopbackDAOTracing\n", "IdentifyClassAs: loopbackDAO LoopbackDAOTracing\n",
		 "TraceAllMethodsIfFlagged: LoopbackDAOTracing NodeBlamePointTracer \"Backends|loopbackDAO\"\n",
				"TraceAllMethodsIfFlagged: LoopbackDAOTracing NodeExceptionErrorReporter \"Backends|loopbackDAO:Errors Per Interval\"\n"};

		try {
			for (int i = 0; i < list.length; i++) {
				FileUtils.insert(pbdPath, list[i], pbdFile.length());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterMethod(alwaysRun = true)
	public void executeAfterMethod() {
        stopAppAndWaitDisc();
        if (tixChangeConfig.getProperty("useRequestQueue") != "false") {
            tixChangeConfig.updateProperty("useRequestQueue", Boolean.FALSE);
	    }
        stopCollectorAgentAndWaitDisc();
	}

	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		super.testClassTeardown();
	}
}
