package com.ca.apm.systemtest.fld.test.devel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.fakeWorkstation.FakeWorkstationPlugin;
import com.ca.apm.systemtest.fld.testbed.FakeWorkstationPluginTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Fake Workstation Plugin TAS test.
 *
 * @author SINAL04
 */
public class FakeWorkstationPluginTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(FakeWorkstationPluginTest.class);

    private static final String EM_USER = "cemadmin";
    private static final String EM_PASSWORD = "quality";
    private static final long EM_PORT = 5001;
    private static final String EM_HOST = "localhost";
    private static final long RESOLUTION = 15;
    private static final long SLEEP_BETWEEN = 15000;
    private static final String METRIC = "Servlets\\|Servlet_(.*):Average Response Time \\(ms\\)";
    private static final long MONITOR_TIME = 120000L;
    private static final String EXPECTED_LOG_ENTRY
        = "Average (for past 15 sec): Data Query (Agent\\|Servlets\\|Servlet_(.*):Average "
        + "Response Time \\(ms\\)) duration = 4 ms, count = 0; total queries: 1; queries/sec: 250"
        + ".0";
    private static final int INITIAL_LOG_CHUNK_SIZE_IN_KB = 4;

    public FakeWorkstationPluginTest() {
    }

    @Tas(testBeds = @TestBed(name = FakeWorkstationPluginTestbed.class,
        executeOn = FakeWorkstationPluginTestbed.TEST_MACHINE_ID),
        owner = "sinal04", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fldFakeWorkstation"})
    public void runLiveQueries() {
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
            "fld-tas-test-context.xml")) {

            FakeWorkstationPlugin fakeWorkstationPlugin = ctx.getBean(FakeWorkstationPlugin.class);

        String fakeWorkstationPath = fakeWorkstationPlugin.downloadFakeWorkstation(fakeWorkstationPlugin.getDefaultVersion(), "C:/sw/fakeWS", "fakeWorkstation", ".jar");
        LOGGER.info("Fake Workstation jar path: {}", fakeWorkstationPath);
        Assert.assertNotNull(fakeWorkstationPath);

            String id = fakeWorkstationPlugin.runQueriesAgainstMOM(fakeWorkstationPath,
                FakeWorkstationPlugin.DEFAULT_JVM_OPTIONS, EM_HOST, EM_PORT, EM_USER, EM_PASSWORD,
                RESOLUTION, SLEEP_BETWEEN, null, METRIC, "Agent");

            Assert.assertNotNull(id);

            try {
                Thread.sleep(60000L);
            } catch (InterruptedException e) {
                ErrorUtils.logExceptionFmt(LOGGER, e, "Exception: {0}");
            }

            long startMonitoringTime = System.currentTimeMillis();
            int readChunk = INITIAL_LOG_CHUNK_SIZE_IN_KB;
            String logContent = null;
            while ((System.currentTimeMillis() - startMonitoringTime) < MONITOR_TIME) {
                logContent = fakeWorkstationPlugin.getFakeWorkstationLogs(id, readChunk);

                if (logContent != null && (logContent.length() >= (INITIAL_LOG_CHUNK_SIZE_IN_KB
                    * 1024))) {
                    readChunk *= 2;
                }

                try {
                    Thread.sleep(15000L);
                } catch (InterruptedException e) {
                    ErrorUtils.logExceptionFmt(LOGGER, e, "Exception: {0}");
                }
            }

            Assert.assertNotNull(logContent);
            Assert.assertFalse(isBlank(logContent));

            LOGGER
                .info("FAKE WORKSTATION LOG [read buffer size = {}KB]: {}", readChunk, logContent);
            Assert.assertTrue(logContent.contains(EXPECTED_LOG_ENTRY));

            fakeWorkstationPlugin.stopFakeWorkstationProcess(id);
        }
    }

}
