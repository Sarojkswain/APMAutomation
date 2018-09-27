/*
 * Copyright (c) 2016 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.test;

import com.ca.apm.automation.action.test.TimeoutExpiredException;
import com.ca.apm.systemtest.fld.role.MQMonitorRole;
import com.ca.apm.systemtest.fld.testbed.PerfMQLoadTestbed;
import com.ca.apm.systemtest.fld.testbed.STMQLoanTestbed;
import com.ca.apm.tests.flow.*;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlow;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.test.MyTasTestNgTest;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.testng.Assert.assertTrue;

/** Test for MQ + IIB10 FieldPack performance test
 *
 * The testbed has predeployed components:
 * - EM
 * - Fieldpack Agent (3x)
 * - WAS + MQ JMS App
 * - MQ Explorer (+ 600 queues)
 * - IIB 10 (+ 100 flows utilizing MQ queues)
 *
 * The testbed uses an external Java utility to push/pull messages from MQ queues
 *
 * SC issue 461560 / DE200642
 * SC issue 440683 / DE200849
 *
 * @Author Erik Melecky (meler02)
 */
public class MQPerfTest extends MyTasTestNgTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSBSystemTest.class);
    public ITasResolver tasResolver;

    public static Long JMETER_RUN_TIME_SEC;
    public static Long JMETER_RAMP_UP_TIME_SEC;
    public static Long JMETER_STARTUP_DELAY_SEC;
    public static Long METRICS_RUN_TIME_SEC;

    public static Long JMETER_DELAY_BETWEEN_REQUESTS_MS = 1000L;
    public static Long JMETER_THREADS = 80L;

    public static final String SHARE_DIR_PASSWORD = "Lister@123";

    public static final boolean CONTINUE_ON_TEST_FAIL = true;

    MQPerfTest() throws Exception {
        super();
    }

    @BeforeClass
    public void setUp() throws Exception {

        // SMOKE
//        JMETER_RAMP_UP_TIME_SEC = 1L * 60; // 1 min
//        JMETER_STARTUP_DELAY_SEC = 1L; // 1 sec
//        JMETER_RUN_TIME_SEC = 3L * 60; // 3 min
//        METRICS_RUN_TIME_SEC = JMETER_RUN_TIME_SEC + (1L * 60); // JMeter + 2 min

        // FULL
        JMETER_RAMP_UP_TIME_SEC = 2L * 60; // 2 min
        JMETER_STARTUP_DELAY_SEC = 30L; // 30 sec
        JMETER_RUN_TIME_SEC = 40L * 60; // 40 min
        METRICS_RUN_TIME_SEC = JMETER_RUN_TIME_SEC + (2L * 60); // JMeter + 2 min
    }

    @Tas(testBeds = @TestBed(name = PerfMQLoadTestbed.class,
            executeOn = PerfMQLoadTestbed.loadMachine),
            owner = "meler02", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"mq_fieldpack"})
    public void testRun1() throws Exception {
        try {
            assertTrue(testDeployment(1) == 0);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @Tas(testBeds = @TestBed(name = PerfMQLoadTestbed.class,
            executeOn = PerfMQLoadTestbed.loadMachine),
            owner = "meler02", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"mq_fieldpack"})
    public void testRun2() throws Exception {
        try {
            assertTrue(testDeployment(2) == 0);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @Tas(testBeds = @TestBed(name = PerfMQLoadTestbed.class,
            executeOn = PerfMQLoadTestbed.loadMachine),
            owner = "meler02", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"mq_fieldpack"})
    public void testRun3() throws Exception {
        try {
            assertTrue(testDeployment(3) == 0);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @Tas(testBeds = @TestBed(name = PerfMQLoadTestbed.class,
            executeOn = PerfMQLoadTestbed.loadMachine),
            owner = "meler02", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"mq_fieldpack"})
    public void testRun4() throws Exception {
        try {
            assertTrue(testDeployment(4) == 0);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @Tas(testBeds = @TestBed(name = PerfMQLoadTestbed.class,
            executeOn = PerfMQLoadTestbed.loadMachine),
            owner = "meler02", size = SizeType.DEBUG, exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"mq_fieldpack"})
    public void testRun5() throws Exception {
        try {
            assertTrue(testDeployment(5) == 0);
        } catch (Exception e) {
            Assert.fail("Exception during test execution !", e);
        }
    }

    @Tas(
            testBeds = @TestBed(name = PerfMQLoadTestbed.class,
                    executeOn = PerfMQLoadTestbed.loadMachine),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"mq_fieldpack"})
    public void stopAppServers() {
        try {
            stopMqMonitor("mqMonitorMqNoMbRole"); // TODO
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping MQ Monitor", ex);
        }
        try {
            stopWas();
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping Websphere", ex);
        }
    }

    public int testDeployment(int runNumber) throws Exception {

        int errors = 0;

        Map<String, String> logMap = new HashMap<>();
        logMap.put("logsGathererWasRole", PerfMQLoadTestbed.appServerMachine);
        logMap.put("logsGathererMqRole", PerfMQLoadTestbed.mqServerMachine);

        // no MQ
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        try {
            startWas();
            execThreads.add(getJMeterThread(PerfMQLoadTestbed.loadMachine, "jmeterNoMqRoleId", JMETER_RUN_TIME_SEC,
                    JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            execThreads.add(getTypeperfThread("perfMonitorNoMqRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            execThreads.add(getJmxThread("jmxMonitorNoMqRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            startThreads(execThreads);
            waitForThreads(execThreads, 5000);
            stopWas();
            moveLogs(logMap);
        } catch (Exception e) {
            errors += handleException(e, execThreads);
        }

        // MQ no MB
        execThreads = new CopyOnWriteArrayList<>();
        try {
            startMqMonitor("mqMonitorMqNoMbRole");
            startWas();
            execThreads.add(getJMeterThread(PerfMQLoadTestbed.loadMachine, "jmeterMqNoMbRoleId", JMETER_RUN_TIME_SEC,
                    JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            execThreads.add(getTypeperfThread("perfMonitorMqNoMbRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            execThreads.add(getJmxThread("jmxMonitorMqNoMbRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            startThreads(execThreads);
            waitForThreads(execThreads, 5000);
            stopWas();
            stopMqMonitor("mqMonitorMqNoMbRole");
            moveLogs(logMap);
        } catch (Exception e) {
            errors += handleException(e, execThreads);
        }

        // no MQ MB
        execThreads = new CopyOnWriteArrayList<>();
        try {
            startMqMonitor("mqMonitorNoMqMbRole");
            startWas();
            execThreads.add(getJMeterThread(PerfMQLoadTestbed.loadMachine, "jmeterNoMqMbRoleId", JMETER_RUN_TIME_SEC,
                    JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            execThreads.add(getTypeperfThread("perfMonitorNoMqMbRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            execThreads.add(getJmxThread("jmxMonitorNoMqMbRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            startThreads(execThreads);
            waitForThreads(execThreads, 5000);
            stopWas();
            stopMqMonitor("mqMonitorNoMqMbRole");
            moveLogs(logMap);
        } catch (Exception e) {
            errors += handleException(e, execThreads);
        }

        // MQ MB
        execThreads = new CopyOnWriteArrayList<>();
        try {
            startMqMonitor("mqMonitorMqMbRole");
            startWas();
            execThreads.add(getJMeterThread(PerfMQLoadTestbed.loadMachine, "jmeterMqMbRoleId", JMETER_RUN_TIME_SEC,
                    JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            execThreads.add(getTypeperfThread("perfMonitorMqMbRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            execThreads.add(getJmxThread("jmxMonitorMqMbRole", PerfMQLoadTestbed.appServerMachine, METRICS_RUN_TIME_SEC));
            startThreads(execThreads);
            waitForThreads(execThreads, 5000);
            stopWas();
            stopMqMonitor("mqMonitorMqMbRole");
            moveLogs(logMap);
        } catch (Exception e) {
            errors += handleException(e, execThreads);
        }

        generateXls("csvToXlsRoleId", runNumber);
        return errors;

    }

    private void startWas() {
        runSerializedCommandFlowFromRole(STMQLoanTestbed.WAS_85_ROLE_ID, WebSphere8Role.ENV_WEBSPHERE_START);
    }

    private void stopWas() {
        runSerializedCommandFlowFromRole(STMQLoanTestbed.WAS_85_ROLE_ID, WebSphere8Role.ENV_WEBSPHERE_STOP);
    }

    private void startMqMonitor(String roleId) {
        runSerializedCommandFlowFromRole(roleId, MQMonitorRole.ENV_RUN_MQMONITOR);
    }

    private void stopMqMonitor(String roleId) {
        runSerializedCommandFlowFromRole(roleId, MQMonitorRole.ENV_STOP_MQMONITOR);
    }

    protected Thread getJMeterThread(final String machineId, final String jMeterRole, final Long runTime,
                                     final Long rampUpTime, final Long startupDelay) {
        return new Thread() {

            @Override
            public String toString() {
                return "JMeter " + machineId;
            }

            @Override
            public void run() {
                try {
                    JMeterRunFlowContext context = (JMeterRunFlowContext) deserializeFlowContextFromRole(jMeterRole,
                            JMeterRole.RUN_JMETER, JMeterRunFlowContext.class);
                    context.getParams().put("testWarmupInSeconds", String.valueOf(rampUpTime));
                    context.getParams().put("testDurationInSeconds", String.valueOf(runTime));
                    context.getParams().put("startupDelaySeconds", String.valueOf(startupDelay));
                    context.getParams().put("delayBetweenRequestsMs", String.valueOf(JMETER_DELAY_BETWEEN_REQUESTS_MS));
                    context.getParams().put("testNumberOfCVUS", String.valueOf(JMETER_THREADS));
                    runFlowByMachineId(machineId, JMeterRunFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected Thread getJmxThread(final String jmxRole, final String machineId, final Long runTime) {
        return new Thread() {

            @Override
            public String toString() {
                return "JMX " + machineId;
            }

            @Override
            public void run() {
                try {
                    JmxMonitorFlowContext context = (JmxMonitorFlowContext) deserializeFlowContextFromRole(jmxRole, JmxMonitorRole.RUN_JMX_MONITOR,
                            JmxMonitorFlowContext.class);
                    if (runTime != null) {
                        context.setRunTime(runTime);
                    }
                    context.setCopyResultsDestinationPassword(SHARE_DIR_PASSWORD);
                    runFlowByMachineId(machineId, JmxMonitorFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected Thread getTypeperfThread(final String typeperfRole, final String machineId, final Long runTime) {
        return new Thread() {

            @Override
            public String toString() {
                return "Typeperf " + machineId;
            }

            @Override
            public void run() {
                try {
                    TypeperfFlowContext context = (TypeperfFlowContext) deserializeFlowContextFromRole(typeperfRole, TypeperfRole.RUN_TYPEPERF,
                            TypeperfFlowContext.class);
                    if (runTime != null) {
                        context.setRunTime(runTime);
                    }
                    context.setCopyResultsDestinationPassword(SHARE_DIR_PASSWORD);
                    runFlowByMachineId(machineId, TypeperfFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected void startThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.start();
        }
        LOGGER.info(threads.size() + " threads started");
    }

    private void waitForThread(Thread thread, long checkInterval) throws InterruptedException {
        waitForThread(thread, checkInterval, -1);
    }

    private void waitForThread(Thread thread, long checkInterval, long timeout) throws InterruptedException {
        List<Thread> threads = new CopyOnWriteArrayList<>();
        threads.add(thread);
        waitForThreads(threads, checkInterval, timeout);
    }

    protected void waitForThreads(List<Thread> threads, long checkInterval) throws InterruptedException {
        waitForThreads(threads, checkInterval, -1);
    }

    private void waitForThreads(List<Thread> threads, long checkInterval, long timeout) throws InterruptedException {
        GregorianCalendar cal = new GregorianCalendar();
        long startTime = System.currentTimeMillis();
        while (!threads.isEmpty()) {
            String threadsString = "";
            for (Thread thread : threads) {
                if (!thread.isAlive()) {
                    threads.remove(thread);
                } else {
                    threadsString += thread.toString() + "; ";
                }
            }
            long time = System.currentTimeMillis() - startTime;
            if (timeout > 0 && time > timeout) {
                throw new TimeoutExpiredException("Timeout (" + timeout + " ms) exceeded while waiting for threads [" + threadsString + "]");
            }
            cal.setTimeInMillis(time);
            LOGGER.info(cal.get(Calendar.MINUTE) + "m " + cal.get(Calendar.SECOND) +
                    "s - Waiting for " + threads.size() + " threads [" + threadsString + "]");
            Thread.sleep(checkInterval);
        }
    }

    private int handleException(Exception e, List<Thread>... runningThreads) throws Exception {
        for (List<Thread> runningThread : runningThreads) {
            for (Thread thread : runningThread) {
                try {
                    thread.interrupt();
                } catch (Exception ex) {
                    LOGGER.warn("Exception was thrown during interrupting thread", ex);
                }
            }
            try {
                waitForThreads(runningThread, 1000);
            } catch (Exception ex) {
                LOGGER.warn("Exception was thrown during interrupting thread", ex);
            }
        }
        stopAppServers();
        if (CONTINUE_ON_TEST_FAIL) {
            LOGGER.warn("Exception was thrown, continuing test execution", e);
            return 1;
        } else {
            throw e;
        }
    }

    protected void generateXls(String csvToXlsRole, Integer runNumber, String template, String outputPrefix) {
        CsvToXlsFlowContext context = (CsvToXlsFlowContext) deserializeFlowContextFromRole(csvToXlsRole,
                CsvToXlsRole.RUN_CSV_TO_XLS, CsvToXlsFlowContext.class);
        context.setTemplateFileName("C:\\sw\\wily\\csvToXls\\" + template);
        context.setOutputFileName(PerfMQLoadTestbed.SHARE_DIR + "\\" + outputPrefix + (runNumber == null ? "" : ("_run" + runNumber)) + ".xls");
        context.setHeapMemory("2048m");
        runFlowByMachineId(PerfMQLoadTestbed.loadMachine, CsvToXlsFlow.class, context);
    }

    protected void generateXls(String csvToXlsRole, Integer runNumber) {
        generateXls(csvToXlsRole, runNumber, "MqPerformanceTemplate_40min.xls", "Results_40min");
    }

    /**
     * @param logsGathererRoles map in [RoleId, MachineId] format
     */
    protected void moveLogs(Map<String, String> logsGathererRoles) {
        for (Map.Entry<String, String> logsGathererRole : logsGathererRoles.entrySet()) {
            LogsGathererFlowContext context = (LogsGathererFlowContext) deserializeFlowContextFromRole(logsGathererRole.getKey(),
                    LogsGathererRole.RUN_LOGS_GATHERER, LogsGathererFlowContext.class);
            runFlowByMachineId(logsGathererRole.getValue(), LogsGathererFlow.class, context);
        }
    }

}
