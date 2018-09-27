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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.common.Util;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test for the existence of health metrics from the node agent.
 * <p/>
 * <p/>
 * TAS refresher:
 * <p/>
 * Lock a test bed:
 * mvn tas:lock -Dtas.testbed=com.ca.apm.tests.testbed.NodeJSAgentTestbed -Dtas.user=calpa08
 * <p/>
 * deploy:
 * mvn tas:deploy -Dtas.skipLocalRepo=false -Dtas.machines=nodejsMachine=tas-scx-n3:8888 -Dtas.testbed=com.ca.apm.tests.testbed.NodeJSAgentTestbed -Dtas.user=calpa08
 * <p/>
 * launch:
 * mvn tas:launch-from-index -Dtas.skipLocalRepo=false -Dtas.testId=com.ca.apm.tests.test.HealthMetricTest:com.ca.apm.tests.testbed.NodeJSAgentTestbed -Dtas.executeOn=tas-scx-n3:8888 -Dtas.user=calpa08
 * <p/>
 * [info] deploy    : mvn tas:deploy -Dtas.skipLocalRepo=false -Dtas.machines=nodejsMachine=tas-scx-n5:8888 -Dtas.testbed=com.ca.apm.tests.testbed.NodeJSAgentTestbed
 * mvn tas:revert -Dtas.machines=tas-scx-n5
 * mvn tas:snapshot -Dtas.machines=tas-scx-n5
 * mvn tas:unlock -Dtas.machines=tas-scx-n5
 *
 * @author calpa08
 */
public class HealthMetricTest extends BaseNodeAgentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthMetricTest.class);

    public static final int WAIT_TIME = 90000;

    @BeforeClass(alwaysRun = true)
    public void testClassSetup() {
        super.testClassSetup();
        startCollectorAgent();
    }

    @BeforeMethod(alwaysRun = true)
    public void executeBeforeMethod(Method method) {
        testMethodName = method.getName();
        probeConfig.updateLogFileName(method.getName() + LOG_FILE_EXT);
        LOGGER.info("executing test {}#{} ", testSetName, testMethodName);
        startAppAndWaitConn();
        
    }

    @Tas(testBeds = @TestBed(name = NodeJSAgentTestbed.class, executeOn = NodeJSAgentTestbed.NODEJS_MACHINE), size = SizeType.MEDIUM, owner = "calpa08")
    @Test(priority = 1, groups = {"bat", "nodeagent", "health"})
    public void testHealthMetrics() throws Exception {
        fireTxnRequest("/api/Items/1", 1);

        //el
        assertMetrics("Node.js Runtime:Event Loop");


        //http connections
        assertMetrics("Node.js Runtime:HTTP Connections / sec");
        assertMetrics("Node.js Runtime:HTTP Connection Count");

        //}


        //heap
        assertMetrics("Node.js Runtime:Heap Total");
        assertMetrics("Node.js Runtime:Heap Used");


        //heap
        fireTxnRequest("rest/triggergc", 2);
        //assertMetrics("Node.js Runtime:GC Heap Total", 120000);


        //cpu
        assertMetrics("Node.js Runtime:CPU Total (%)");
        assertMetrics("Node.js Runtime:CPU User (%)");
        assertMetrics("Node.js Runtime:CPU System (%)");
    }

    
	@Test(priority = 1, groups = { "full", "native-less" })
	public void testNativeModuleMissing() {
		File dir = new File(probeConfig.getHome(), "build");
		assertFalse(dir.exists(), "native module build directory exists");

		try {
			String path = probeConfig.getLogPath();
			String message = ".*System health monitoring module missing, install a compiler and rebuild.*";
			assertTrue(Util.findPattern(path, message),
			        String.format("Message '%s' was not found in log: %s", message, path));

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			fail("exception while verifying log messages");
		}
	}


    private void assertMetrics(String metricPath, long wait) {
        MetricAssertionData eventLoopAssertion = new MetricAssertionData.MinMaxBuilder(metricPath, 0, Integer.MAX_VALUE).setDuration(wait).build();
        mockEm.processMetrics(eventLoopAssertion);
    }

    private void assertMetrics(String metricPath) {
        assertMetrics(metricPath, WAIT_TIME);
    }

	private void fireTxnRequest(String path, int reqs) {
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(appUrlBase + path).setNumberReqs(reqs)
		        .build();
		txnGen.start();
	}

    @AfterMethod(alwaysRun = true)
    public void executeAfterMethod() {
        checkErrorInLogs();
    }

    @AfterClass(alwaysRun = true)
    public void testClassTeardown() {
        stopNodeApp();
        stopCollectorAgent();
        super.testClassTeardown();
    }
}
