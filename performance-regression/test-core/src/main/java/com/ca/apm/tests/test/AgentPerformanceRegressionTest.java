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
package com.ca.apm.tests.test;

import com.ca.apm.automation.action.flow.DeployEmptyFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.test.TimeoutExpiredException;
import com.ca.apm.tests.flow.*;
import com.ca.apm.tests.flow.agent.*;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlow;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.flow.jMeter.JMeterStatsFlow;
import com.ca.apm.tests.flow.jMeter.JMeterStatsFlowContext;
import com.ca.apm.tests.flow.oracleDb.OracleDbRestartFlow;
import com.ca.apm.tests.flow.oracleDb.WebAppTradeDbScriptFlow;
import com.ca.apm.tests.flow.oracleDb.WebAppTradeDbScriptFlowContext;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.AgentPerformanceRegression103TestBed;
import com.ca.apm.tests.testbed.machines.*;
import com.ca.apm.tests.testbed.machines.template.JMeterMachine;
import com.ca.apm.tests.testbed.machines.template.JMeterMachineAbs;
import com.ca.apm.tests.testbed.machines.template.JavaAgentMachine;
import com.ca.apm.tests.testbed.machines.template.NetAgentMachine;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.testng.Assert.assertTrue;

/**
 * SampleTest class
 * <p/>
 * Test description
 */
public class AgentPerformanceRegressionTest extends MyTasTestNgTest implements INetShareUser {

    public enum IisAppType {NetStockTrader, NerdDinner4, NerdDinner5}

    // TEST IDs FOR ALM (QC)
    @SuppressWarnings("WeakerAccess")
    protected static String TESTID_IIS_NO_AGENT;
    protected static String TESTID_TOMCAT_NO_AGENT;
    protected static String TESTID_WAS_NO_AGENT;
    protected static String TESTID_WLS_NO_AGENT;

    protected static String TESTID_IIS_CURRENT_NO_DI;
    protected static String TESTID_TOMCAT_CURRENT_NO_SI;
    protected static String TESTID_WAS_CURRENT_NO_SI;
    protected static String TESTID_WLS_CURRENT_NO_SI;

    protected static String TESTID_IIS_CURRENT_DI;
    protected static String TESTID_TOMCAT_CURRENT_SI;
    protected static String TESTID_WAS_CURRENT_SI;
    protected static String TESTID_WLS_CURRENT_SI;

    protected static String TESTID_IIS_PREV_NO_DI;
    protected static String TESTID_TOMCAT_PREV_NO_SI;
    protected static String TESTID_WAS_PREV_NO_SI;
    protected static String TESTID_WLS_PREV_NO_SI;

    protected static String TESTID_IIS_PREV_DI;
    protected static String TESTID_TOMCAT_PREV_SI;
    protected static String TESTID_WAS_PREV_SI;
    protected static String TESTID_WLS_PREV_SI;

    protected static String TESTID_TOMCAT_CURRENT_ACC;
    protected static String TESTID_WAS_CURRENT_ACC;
    protected static String TESTID_WLS_CURRENT_ACC;

    protected static String TESTID_TOMCAT_CURRENT_BRTM;

    protected static String TESTSET_FOLDER;
    protected static String TESTSET_NAME;


    public static boolean EXECUTE_TOMCAT;
    public static boolean EXECUTE_WAS;
    public static boolean EXECUTE_WLS;
    public static boolean EXECUTE_IIS;

    public static boolean RECREATE_DB = true;

    public static final boolean CONTINUE_ON_TEST_FAIL = true;

    public static boolean EXEC_NO_AGENT;
    public static boolean EXEC_CURRENT_NO_SI;
    public static boolean EXEC_CURRENT_SI;
    public static boolean EXEC_CURRENT_ACC;
    public static boolean EXEC_CURRENT_BRTM;
    public static boolean EXEC_PREV_NO_SI;
    public static boolean EXEC_PREV_SI;

    public static Long JMETER_RAMP_UP_TIME_SEC;
    public static Long JMETER_STARTUP_DELAY_SEC;
    public static Long JMETER_RUN_TIME_SEC;
    public static Long METRICS_RUN_TIME_SEC;
    public static Integer RUNS;

    public static IisAppType IIS_APP;

    public static final String EM_MACHINE_ID = AgentPerformanceRegression103TestBed.EM_MACHINE_ID;


    private static final Logger LOGGER = LoggerFactory.getLogger(AgentPerformanceRegressionTest.class);

    AgentPerformanceRegressionTest() throws Exception {
    }

    protected String getEmMachineId() {
        return EM_MACHINE_ID;
    }

    protected String getAsTomcatMachineId() {
        return AgentPerformanceRegression103TestBed.AS_TOMCAT_MACHINE_ID;
    }

    protected String getAsWasMachineId() {
        return AgentPerformanceRegression103TestBed.AS_WAS_MACHINE_ID;
    }

    protected String getAsWlsMachineId() {
        return AgentPerformanceRegression103TestBed.AS_WLS_MACHINE_ID;
    }

    protected String getAsIisMachineId() {
        return AgentPerformanceRegression103TestBed.AS_IIS_MACHINE_ID;
    }

    protected String getDbTomcatMachineId() {
        return AgentPerformanceRegression103TestBed.DB_ORACLE_TOMCAT_MACHINE_ID;
    }

    protected String getDbWasMachineId() {
        return AgentPerformanceRegression103TestBed.DB_ORACLE_WAS_MACHINE_ID;
    }

    protected String getDbWlsMachineId() {
        return AgentPerformanceRegression103TestBed.DB_ORACLE_WLS_MACHINE_ID;
    }

    protected String getJmeterTomcatMachineId() {
        return AgentPerformanceRegression103TestBed.JMETER_TOMCAT_MACHINE_ID;
    }

    protected String getJmeterWasMachineId() {
        return AgentPerformanceRegression103TestBed.JMETER_WAS_MACHINE_ID;
    }

    protected String getJmeterWlsMachineId() {
        return AgentPerformanceRegression103TestBed.JMETER_WLS_MACHINE_ID;
    }

    protected String getJmeterIisMachineId() {
        return AgentPerformanceRegression103TestBed.JMETER_IIS_MACHINE_ID;
    }

    @BeforeClass
    public void setUp() throws Exception {

        EXECUTE_TOMCAT = true;
        EXECUTE_WAS = true;
        EXECUTE_WLS = true;
        EXECUTE_IIS = true;

        EXEC_NO_AGENT = true;
        EXEC_CURRENT_NO_SI = true;
        EXEC_CURRENT_SI = true;
        EXEC_CURRENT_ACC = true;
        EXEC_CURRENT_BRTM = true;
        EXEC_PREV_NO_SI = true;
        EXEC_PREV_SI = true;

        JMETER_RAMP_UP_TIME_SEC = 0L; // 2 min
        JMETER_STARTUP_DELAY_SEC = 0L; // 30 sec
        JMETER_RUN_TIME_SEC = 0L; // 40 min
        METRICS_RUN_TIME_SEC = 0L;
        RUNS = 1;

        IIS_APP = IisAppType.NetStockTrader;
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void moveLogsTest() throws Exception {
        moveLogsWls();
        moveLogsWas();
        moveLogsTomcat();
        moveLogsIis();
        assertTrue(true);
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void generateXlsTest() throws Exception {
        generateXls(getEmMachineId() + EmMachine.CSV_TO_XLS_ROLE_ID, null);
        assertTrue(true);
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void generateXlsTest1() throws Exception {
        generateXls(getEmMachineId() + EmMachine.CSV_TO_XLS_ROLE_ID, null, "AgentPerformanceTemplate_40min_10.2.xls", "tmpRes102");
        generateXls(getEmMachineId() + EmMachine.CSV_TO_XLS_ROLE_ID, null, "Template_40min.xls", "tmpRes");
        generateXls(getEmMachineId() + EmMachine.CSV_TO_XLS_ROLE_ID, null, "Template_40min_pre10.1.xls", "tmpResPre101");
        assertTrue(true);
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    /**
     * ///////////////////////
     * // Run complete test
     * ///////////////////////
     * */
    public void regressionTestAll() throws Exception {

        int xlsOffset = 0; // todo 0
        int errors = 0;
        for (int i = 1 + xlsOffset; i <= RUNS + xlsOffset; i++) {
            errors += regressionTestRunAll(i);
        }
        LOGGER.info("Errors: " + errors);
        assertTrue(errors == 0, "There were some errors during running the performance tests");
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun1() throws Exception {
        assertTrue(regressionTestRunAll(1) == 0, "There were some errors during running the performance tests");
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun2() throws Exception {
        assertTrue(regressionTestRunAll(2) == 0, "There were some errors during running the performance tests");
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun3() throws Exception {
        assertTrue(regressionTestRunAll(3) == 0, "There were some errors during running the performance tests");
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun4() throws Exception {
        assertTrue(regressionTestRunAll(4) == 0, "There were some errors during running the performance tests");
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun5() throws Exception {
        assertTrue(regressionTestRunAll(5) == 0, "There were some errors during running the performance tests");
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression103TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void stopAppServers() {
        try {
            stopAccController(getAsTomcatMachineId() + AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping ACC Tomcat", ex);
        }
        try {
            stopAccController(getAsWlsMachineId() + AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping ACC Websphere", ex);
        }
        try {
            stopAccController(getAsWasMachineId() + AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping ACC Websphere", ex);
        }
        try {
            Thread t = getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, null);
            t.start();
            waitForThread(t, 5000, 120000);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping Tomcat", ex);
        }
        try {
            Thread t = getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, null);
            t.start();
            waitForThread(t, 5000, 120000);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping Weblogic", ex);
        }
        try {
            Thread t = getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_SI_ROLE_ID, null); // todo
            t.start();
            waitForThread(t, 5000, 120000);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping Websphere", ex);
        }
        try {
            //TODO - if we are running only EXEC_CURRENT_ACC or BRTM, we dont need to derigster .NET agent
            Thread t = getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                    getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_DI_ROLE_ID);
            t.start();
            waitForThread(t, 5000, 120000);
        } catch (Exception ex) {
            LOGGER.warn("Exception was thrown during stopping IIS", ex);
        }
    }

    public int regressionTestRunAll(int runNumber) throws Exception {
        int errors = 0;
        errors += EXEC_NO_AGENT ? testNoAgentRun() : 0;
        errors += EXEC_CURRENT_NO_SI ? testCurrentNoSiRun(false) : 0;
        errors += EXEC_CURRENT_SI ? testCurrentSiRun(false) : 0;
        errors += EXEC_PREV_NO_SI ? testPrevNoSiRun(false) : 0;
        errors += EXEC_PREV_SI ? testPrevSiRun(false) : 0;
        errors += EXEC_CURRENT_ACC ? testCurrentNoSiAccRun(true) : 0;
        errors += EXEC_CURRENT_BRTM ? testCurrentNoSiBrtmRun(false) : 0;
        //
        generateXls(getEmMachineId() + EmMachine.CSV_TO_XLS_ROLE_ID, runNumber);
        Thread.sleep(10000);
        LOGGER.info("=============== Total Test Errors: " + errors + " ===============");
        return errors;
    }

    @SuppressWarnings("WeakerAccess")
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
        List<Thread> threads = new ArrayList<>();
        threads.add(thread);
        waitForThreads(threads, checkInterval, timeout);
    }

    @SuppressWarnings("WeakerAccess")
    protected void waitForThreads(List<Thread> threads, long checkInterval) throws InterruptedException {
        waitForThreads(threads, checkInterval, -1);
    }

    private void waitForThreads(List<Thread> threads, long checkInterval, long timeout) throws InterruptedException {
        GregorianCalendar cal = new GregorianCalendar();
        long startTime = System.currentTimeMillis();
        while (!threads.isEmpty()) {
            String threadsString = "";
            synchronized (threads) {
                Thread[] threadArray = threads.toArray(new Thread[threads.size()]);

                for (Thread thread : threadArray) {
                    if (!thread.isAlive()) {
                        threads.remove(thread);
                    } else {
                        threadsString += thread.toString() + "; ";
                    }
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

    @SuppressWarnings("WeakerAccess")
    protected int handleException(Exception e, List<Thread>... runningThreads) throws Exception {
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

    @SuppressWarnings("WeakerAccess")
    public void recreateDbSchemas() throws InterruptedException {
        if (RECREATE_DB) {
            LOGGER.info("Recreating DB ...");
            List<Thread> threads = new CopyOnWriteArrayList<>();
            if (EXECUTE_TOMCAT) {
                Thread recreateThread = getRecreateOracleDbTablesThread(getDbTomcatMachineId() + DbOracleKonakartTradeDbMachine.KONAKART_SCRIPT_ROLE_ID,
                        getDbTomcatMachineId(), KonakartTradeDbScriptRole.RECREATE_DB_TABLES);
                threads.add(recreateThread);
            }
            if (EXECUTE_WLS) {
                Thread recreateThread = getRecreateOracleDbTablesThread(getDbWlsMachineId() + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID,
                        getDbWlsMachineId(), StocktraderTradeDbScriptRole.RECREATE_DB_TABLES);
                threads.add(recreateThread);
            }
            if (EXECUTE_WAS) {
                Thread recreateThread = getRecreateOracleDbTablesThread(getDbWasMachineId() + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID,
                        getDbWasMachineId(), StocktraderTradeDbScriptRole.RECREATE_DB_TABLES);
                threads.add(recreateThread);
            }
            if (EXECUTE_IIS) {
                // TODO
//            getStartTomcatThread(null, AgentPerformanceRegressionTestBed.AS_TOMCAT_MACHINE_ID + AppServerTomcatMachine.TOMCAT_ROLE_ID);
            }
            startThreads(threads);
            waitForThreads(threads, 5000);
        } else {
            LOGGER.info("Recreating DB skipped.");
        }
        assertTrue(true);
    }

    /**
     * /////////////////////////////
     * // no agent
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testNoAgentRun() throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START

            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(null, AppServerTomcatMachine.TOMCAT_ROLE_ID, null));
            }
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(null, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, null));
            }
            if (EXECUTE_WAS) {
                unregisterWasJavaAgent(getAsWasMachineId() + JavaAgentMachine.AGENT_CURRENT_NO_SI_ROLE_ID); // TODO figure out how to init noagent WAS (JMX)
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, null, null));
            }
            if (EXECUTE_IIS) {
                startAppServerThreads.add(getStartIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID, null));
            }
            startThreads(startAppServerThreads);
//            if (true) {
//                Thread.sleep(30000);
//                throw new RuntimeException("Test Exception"); // todo remove
//            }
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_NO_AGENT_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_NO_AGENT_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_NO_AGENT_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_NO_AGENT_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterWeblogicMachine.JMETER_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_NO_AGENT_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_NO_AGENT_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_IIS) {
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_CPU_NO_AGENT_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_MEM_NO_AGENT_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterIisMachine.JMETER_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC, IIS_APP));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_NO_AGENT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, null));
                generateJMeterStats(getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterWeblogicMachine.JMETER_STATS_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_NO_AGENT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, null, null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_NO_AGENT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_IIS) {
                stopAppServerThreads.add(getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID, null));
                generateJMeterStats(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterMachineAbs.JMETER_STATS_NO_AGENT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_IIS_NO_AGENT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    /**
     * /////////////////////////////
     * // current no SI
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testCurrentNoSiRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_ROLE_ID, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID,
                        startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WAS) {
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_IIS) {
                startAppServerThreads.add(getStartIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_NO_DI_ROLE_ID));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterWeblogicMachine.JMETER_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_CURRENT_NO_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_IIS) {
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_CPU_CURRENT_NO_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_MEM_CURRENT_NO_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterIisMachine.JMETER_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC, IIS_APP));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_CURRENT_NO_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_CURRENT_NO_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_CURRENT_NO_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_IIS) {
                stopAppServerThreads.add(getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_NO_DI_ROLE_ID));
                generateJMeterStats(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_IIS_CURRENT_NO_DI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    /**
     * /////////////////////////////
     * // current no SI ACC
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testCurrentNoSiAccRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_ACC_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WAS) {
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_ACC_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_ACC_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_ACC_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_CURRENT_NO_SI_ACC_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_ACC_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_CURRENT_NO_SI_ACC_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_CURRENT_NO_SI_ACC_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_CURRENT_NO_SI_ACC_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_ACC_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_ACC_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    /**
     * /////////////////////////////
     * // current no SI BRTM
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testCurrentNoSiBrtmRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_BRTM_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_BRTM_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_BRTM_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_CURRENT_NO_SI_BRTM_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_CURRENT_NO_SI_BRTM_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_CURRENT_BRTM, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    /**
     * /////////////////////////////
     * // current SI
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testCurrentSiRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_CURRENT_SI_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(JavaAgentMachine.AGENT_CURRENT_SI_ROLE_ID, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID,
                        startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WAS) {
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_IIS) {
                startAppServerThreads.add(getStartIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_DI_ROLE_ID));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_CURRENT_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_CURRENT_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_CURRENT_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_CURRENT_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_CURRENT_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_CURRENT_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_IIS) {
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_CPU_CURRENT_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_MEM_CURRENT_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterIisMachine.JMETER_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC, IIS_APP));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_CURRENT_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_CURRENT_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_CURRENT_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_IIS) {
                stopAppServerThreads.add(getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_DI_ROLE_ID));
                generateJMeterStats(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_IIS_CURRENT_DI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    /**
     * /////////////////////////////
     * // prev no SI
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testPrevNoSiRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_PREV_NO_SI_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(JavaAgentMachine.AGENT_PREV_NO_SI_ROLE_ID, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID,
                        startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WAS) {
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_PREV_NO_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_IIS) {
                startAppServerThreads.add(getStartIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_PREV_NO_DI_ROLE_ID));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_PREV_NO_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_PREV_NO_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_PREV_NO_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_PREV_NO_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_PREV_NO_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_PREV_NO_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_IIS) {
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_CPU_PREV_NO_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_MEM_PREV_NO_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterIisMachine.JMETER_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC, IIS_APP));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_PREV_NO_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_STATS_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_PREV_NO_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_PREV_NO_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_PREV_NO_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_IIS) {
                stopAppServerThreads.add(getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_PREV_NO_DI_ROLE_ID));
                generateJMeterStats(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterMachineAbs.JMETER_STATS_PREV_NO_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_IIS_PREV_NO_DI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    /**
     * /////////////////////////////
     * // prev SI
     * /////////////////////////////
     */
    @SuppressWarnings("WeakerAccess")
    public int testPrevSiRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_PREV_SI_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(JavaAgentMachine.AGENT_PREV_SI_ROLE_ID, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID,
                        startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WAS) {
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_PREV_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_IIS) {
                startAppServerThreads.add(getStartIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_PREV_DI_ROLE_ID));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_PREV_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_PREV_SI_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_PREV_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_PREV_SI_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_PREV_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_PREV_SI_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_IIS) {
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_CPU_PREV_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_MEM_PREV_DI_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterIisMachine.JMETER_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC, IIS_APP));
            }
            // EXECUTE
            startThreads(execThreads);
            // WAIT
//        Thread.sleep(SLEEP_TIME_MS);
            waitForThreads(execThreads, 5000);
            // STOP
            if (EXECUTE_TOMCAT) {
                stopAppServerThreads.add(getStopTomcatThread(AppServerTomcatMachine.TOMCAT_ROLE_ID, startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_PREV_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_STATS_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_PREV_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_PREV_SI_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_PREV_SI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_IIS) {
                stopAppServerThreads.add(getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_PREV_DI_ROLE_ID));
                generateJMeterStats(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterMachineAbs.JMETER_STATS_PREV_SI_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_IIS_PREV_DI, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + EmMachine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void registerNetAgent(String agentRole) {
        RegisterNetAgentFlowContext context = (RegisterNetAgentFlowContext) deserializeFlowContextFromRole(agentRole,
                NetAgentRole.REGISTER_AGENT, RegisterNetAgentFlowContext.class);
        runFlowByMachineId(getAsIisMachineId(), RegisterNetAgentFlow.class, context);
    }

    @SuppressWarnings("WeakerAccess")
    protected void unregisterNetAgent(String agentRole) {
        RegisterNetAgentFlowContext context = (RegisterNetAgentFlowContext) deserializeFlowContextFromRole(agentRole,
                NetAgentRole.REGISTER_AGENT, RegisterNetAgentFlowContext.class);
        runFlowByMachineId(getAsIisMachineId(), UnregisterNetAgentFlow.class, context);
    }

    @SuppressWarnings("WeakerAccess")
    protected void registerWasJavaAgent(String agentRole) {
        RegisterJavaAgentFlowContext context = (RegisterJavaAgentFlowContext) deserializeFlowContextFromRole(agentRole,
                JavaAgentRole.REGISTER_AGENT, RegisterJavaAgentFlowContext.class);
        runFlowByMachineId(getAsWasMachineId(), RegisterJavaAgentFlow.class, context);
    }

    @SuppressWarnings("WeakerAccess")
    protected void unregisterWasJavaAgent(String agentRole) {
        RegisterJavaAgentFlowContext context = (RegisterJavaAgentFlowContext) deserializeFlowContextFromRole(agentRole,
                JavaAgentRole.REGISTER_AGENT, RegisterJavaAgentFlowContext.class);
        runFlowByMachineId(getAsWasMachineId(), UnregisterJavaAgentFlow.class, context);
    }

    @SuppressWarnings("WeakerAccess")
    protected void startNetAgent() {
        runFlowByMachineId(getAsIisMachineId(), StartNetAgentFlow.class,
                new DeployEmptyFlowContext.Builder().build());
    }

    @SuppressWarnings("WeakerAccess")
    protected void stopNetAgent() {
        runFlowByMachineId(getAsIisMachineId(), StopNetAgentFlow.class,
                new DeployEmptyFlowContext.Builder().build());
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getRecreateOracleDbTablesThread(final String dbScriptRole, final String machineId, final String envProp) {
        return new Thread() {

            @Override
            public String toString() {
                return "RecreateDB " + machineId;
            }

            @Override
            public void run() {
                try {
                    WebAppTradeDbScriptFlowContext context = (WebAppTradeDbScriptFlowContext) deserializeFlowContextFromRole(dbScriptRole,
                            envProp, WebAppTradeDbScriptFlowContext.class);
                    runFlowByMachineId(machineId, WebAppTradeDbScriptFlow.class, context);
                    runFlowByMachineId(machineId, OracleDbRestartFlow.class, new DeployEmptyFlowContext.Builder().build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStartIisThread(final String iisRole, final String agentRole) {
        return new Thread() {

            @Override
            public String toString() {
                return "IIS start";
            }

            @Override
            public void run() {
                try {
                    if (agentRole != null) {
                        registerNetAgent(agentRole);
                        startNetAgent();
                    }
                    runSerializedCommandFlowFromRole(iisRole, IisRole.ENV_IIS_START);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStopIisThread(final String iisRole, final String agentRole) {
        return new Thread() {

            @Override
            public String toString() {
                return "IIS stop";
            }

            @Override
            public void run() {
                try {
                    runSerializedCommandFlowFromRole(iisRole, IisRole.ENV_IIS_STOP);
                    if (agentRole != null) {
                        stopNetAgent();
                        unregisterNetAgent(agentRole);
                    }
                    moveLogsIis();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStartWasThread(@NotNull final String wasRoleSuffix, @Nullable final String agentRoleSuffix, @Nullable final String accMockRoleSuffix) {
        return new Thread() {

            @Override
            public String toString() {
                return "WebSphere start";
            }

            @Override
            public void run() {
                try {
                    if (accMockRoleSuffix != null) {
                        startAccController(getAsWasMachineId() + accMockRoleSuffix);
                    }
                    if (agentRoleSuffix != null) {
                        registerWasJavaAgent(getAsWasMachineId() + agentRoleSuffix);
                    }
                    runSerializedCommandFlowFromRole(getAsWasMachineId() + wasRoleSuffix, Websphere85Role.ENV_WEBSPHERE_START);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStopWasThread(@NotNull final String wasRoleSuffix, @Nullable final String agentRoleSuffix, @Nullable final String accMockRoleSuffix) {
        return new Thread() {

            @Override
            public String toString() {
                return "WebSphere stop";
            }

            @Override
            public void run() {
                try {
                    runSerializedCommandFlowFromRole(getAsWasMachineId() + wasRoleSuffix, Websphere85Role.ENV_WEBSPHERE_STOP);
                    if (agentRoleSuffix != null) {
                        unregisterWasJavaAgent(getAsWasMachineId() + agentRoleSuffix);
                    }
                    moveLogsWas();
                    if (accMockRoleSuffix != null) {
                        stopAccController(getAsWasMachineId() + accMockRoleSuffix);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStartTomcatThread(@Nullable final String agentRoleSuffix, @NotNull final String tomcatRoleSuffix, @Nullable final String accMockRoleSuffix) {
        return new Thread() {

            @Override
            public String toString() {
                return "Tomcat start";
            }

            @Override
            public void run() {
                try {
                    if (accMockRoleSuffix != null) {
                        startAccController(getAsTomcatMachineId() + accMockRoleSuffix);
                    }
                    String tomcatRole = getAsTomcatMachineId() + tomcatRoleSuffix;
                    String javaOptions = "-Xms10m";
                    if (agentRoleSuffix != null) {
                        String agentRole = getAsTomcatMachineId() + agentRoleSuffix;
                        RegisterJavaAgentFlowContext agentContext = (RegisterJavaAgentFlowContext) deserializeFlowContextFromRole(agentRole,
                                JavaAgentRole.REGISTER_AGENT, RegisterJavaAgentFlowContext.class);
                        String profile = agentContext.getAgentPath() + RegisterJavaAgentFlow.PROFILE_LOCATION_REL;
                        String agent = agentContext.getAgentPath() + RegisterJavaAgentFlow.AGENT_LOCATION_REL;
                        javaOptions += " -javaagent:" + agent + " -Dcom.wily.introscope.agentProfile=" + profile;
                    }
                    RunCommandFlowContext tomcatFlowContext = deserializeCommandFlowFromRole(tomcatRole, TomcatRole.ENV_TOMCAT_START);
                    tomcatFlowContext.getEnvironment().put("JAVA_OPTS", javaOptions);
                    runCommandFlowByMachineId(getAsTomcatMachineId(), tomcatFlowContext);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStopTomcatThread(@NotNull final String tomcatRoleSuffix, @Nullable final String accMockRoleSuffix) {
        return new Thread() {

            @Override
            public String toString() {
                return "Tomcat stop";
            }

            @Override
            public void run() {
                try {
                    runSerializedCommandFlowFromRole(getAsTomcatMachineId() + tomcatRoleSuffix, TomcatRole.ENV_TOMCAT_STOP);
                    moveLogsTomcat();
                    if (accMockRoleSuffix != null) {
                        stopAccController(getAsTomcatMachineId() + accMockRoleSuffix);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStartWlsThread(@Nullable final String agentRoleSuffix, @NotNull final String wlsRoleSuffix, @Nullable final String accMockRoleSuffix) {
        return new Thread() {

            @Override
            public String toString() {
                return "Weblogic start";
            }

            @Override
            public void run() {
                try {
                    if (accMockRoleSuffix != null) {
                        startAccController(getAsWlsMachineId() + accMockRoleSuffix);
                    }
                    String wlsRole = getAsWlsMachineId() + wlsRoleSuffix;
                    String javaOptions = "";
                    if (agentRoleSuffix != null) {
                        String agentRole = getAsTomcatMachineId() + agentRoleSuffix;
                        RegisterJavaAgentFlowContext agentContext = (RegisterJavaAgentFlowContext) deserializeFlowContextFromRole(agentRole,
                                JavaAgentRole.REGISTER_AGENT, RegisterJavaAgentFlowContext.class);
                        String profile = agentContext.getAgentPath() + RegisterJavaAgentFlow.PROFILE_LOCATION_REL;
                        String agent = agentContext.getAgentPath() + RegisterJavaAgentFlow.AGENT_LOCATION_REL;
                        javaOptions = "-javaagent:" + agent + " -Dcom.wily.introscope.agentProfile=" + profile;
                    }
                    RunCommandFlowContext wlsFlowContext = deserializeCommandFlowFromRole(wlsRole, Weblogic103Role.ENV_WEBLOGIC_START);
                    wlsFlowContext.getEnvironment().put("JAVA_OPTIONS", javaOptions);
//                    wlsFlowContext.getEnvironment().put("USER_MEM_ARGS", "-XX:PermSize=512m -XX:MaxPermSize=512m -Xms10m -Xmx256m");
                    wlsFlowContext.getEnvironment().put("USER_MEM_ARGS", "-XX:PermSize=512m -XX:MaxPermSize=512m -Xms256m -Xmx1024m");
                    runCommandFlowByMachineId(getAsWlsMachineId(), wlsFlowContext);
                    // KILL IE THAT WAS STARTED AUTOMATICALLY
                    RunCommandFlowContext killIeFlowContext = new RunCommandFlowContext.Builder("taskkill")
                            .args(Arrays.asList("/F", "/FI", "IMAGENAME eq iexplore.exe")).build();
                    runCommandFlowByMachineId(getAsWlsMachineId(), killIeFlowContext);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getStopWlsThread(@NotNull final String wlsRoleSuffix, @Nullable final String accMockRoleSuffix) {
        return new Thread() {

            @Override
            public String toString() {
                return "Weblogic stop";
            }

            @Override
            public void run() {
                try {
                    runSerializedCommandFlowFromRole(getAsWlsMachineId() + wlsRoleSuffix, Weblogic103Role.ENV_WEBLOGIC_STOP);
                    moveLogsWls();
                    if (accMockRoleSuffix != null) {
                        stopAccController(getAsWlsMachineId() + accMockRoleSuffix);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected void startAccController(String accRole) {
        runSerializedCommandFlowFromRole(accRole, PerfAccControllerRole.ACC_SERVICE_INSTALL);
        //runSerializedCommandFlowFromRole(accRole, MyAccControllerRole.ACC_SERVICE_RUN);
    }

    @SuppressWarnings("WeakerAccess")
    protected void stopAccController(String accRole) {
        runSerializedCommandFlowFromRole(accRole, PerfAccControllerRole.ACC_SERVICE_STOP);
        runSerializedCommandFlowFromRole(accRole, PerfAccControllerRole.ACC_SERVICE_UNINSTALL);
    }

    @SuppressWarnings("WeakerAccess")
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
                    context.setCopyResultsDestinationPassword(DEFAULT_COPY_RESULTS_PASSWORD);
                    runFlowByMachineId(machineId, TypeperfFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getJstatThread(final String jstatRole, final String machineId, final Long runTime) {
        return new Thread() {

            @Override
            public String toString() {
                return "Jstat " + machineId;
            }

            @Override
            public void run() {
                try {
                    JstatFlowContext context = (JstatFlowContext) deserializeFlowContextFromRole(jstatRole, JstatRole.RUN_JSTAT,
                            JstatFlowContext.class);
                    if (runTime != null) {
                        context.setRunTime(runTime);
                    }
                    context.setCopyResultsDestinationPassword(DEFAULT_COPY_RESULTS_PASSWORD);
                    runFlowByMachineId(machineId, JstatFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getJmxThread(final String jstatRole, final String machineId, final Long runTime) {
        return new Thread() {

            @Override
            public String toString() {
                return "JMX " + machineId;
            }

            @Override
            public void run() {
                try {
                    JmxMonitorFlowContext context = (JmxMonitorFlowContext) deserializeFlowContextFromRole(jstatRole, JmxMonitorRole.RUN_JMX_MONITOR,
                            JmxMonitorFlowContext.class);
                    if (runTime != null) {
                        context.setRunTime(runTime);
                    }
                    context.setCopyResultsDestinationPassword(DEFAULT_COPY_RESULTS_PASSWORD);
                    runFlowByMachineId(machineId, JmxMonitorFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getJMeterThread(final String machineId, final String jMeterRole, final Long runTime, final Long rampUpTime, final Long startupDelay) {
        return getJMeterThread(machineId, jMeterRole, runTime, rampUpTime, startupDelay, null);
    }

    @SuppressWarnings("WeakerAccess")
    protected Thread getJMeterThread(final String machineId, final String jMeterRole, final Long runTime, final Long rampUpTime, final Long startupDelay, final IisAppType iisAppType) {
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
                    if (IisAppType.NerdDinner4 == iisAppType) { // TODO parametrize
                        context.setScriptFilePath("C:\\SW\\jmx\\nerddinner-jmeter-var.jmx");
                    }
                    context.getParams().put(JMeterMachine.PARAM_RAMP_UP_TIME, String.valueOf(rampUpTime));
                    context.getParams().put(JMeterMachine.PARAM_TEST_RUNTIME, String.valueOf(runTime));
                    context.getParams().put(JMeterMachine.PARAM_TEST_STARTUP_DELAY, String.valueOf(startupDelay));
                    context.setCopyResultsDestinationPassword(DEFAULT_COPY_RESULTS_PASSWORD);
                    runFlowByMachineId(machineId, JMeterRunFlow.class, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    protected void generateXls(String csvToXlsRole, Integer runNumber, String template, String outputPrefix) {
        CsvToXlsFlowContext context = (CsvToXlsFlowContext) deserializeFlowContextFromRole(csvToXlsRole,
                CsvToXlsRole.RUN_CSV_TO_XLS, CsvToXlsFlowContext.class);
        context.setTemplateFileName("C:\\sw\\wily\\csvToXls\\" + template);
        context.setOutputFileName("C:\\share\\" + outputPrefix + (runNumber == null ? "" : ("_run" + runNumber)) + ".xls");
        context.setHeapMemory("2048m");
        runFlowByMachineId(getEmMachineId(), CsvToXlsFlow.class, context);
    }

    protected void generateXls(String csvToXlsRole, Integer runNumber) {
        generateXls(csvToXlsRole, runNumber, "AgentPerformanceTemplate_40min_10.3.xls", "Results_40min");
    }

    @SuppressWarnings("WeakerAccess")
    protected void generateJMeterStats(final String machineId, String jmeterStatsRole, final Long runTime, final Long rampUpTime, final Long startupDelay) {
        JMeterStatsFlowContext context = (JMeterStatsFlowContext) deserializeFlowContextFromRole(jmeterStatsRole,
                JMeterStatsRole.RUN_JMETER_STATS, JMeterStatsFlowContext.class);
        context.setRampUpTime(rampUpTime);
        context.setRunMinutes(runTime);
        context.setStartupDelaySeconds(startupDelay);
        context.setCopyResultsDestinationPassword(DEFAULT_COPY_RESULTS_PASSWORD);
        runFlowByMachineId(machineId, JMeterStatsFlow.class, context);
    }

    @SuppressWarnings("WeakerAccess")
    protected void moveLogsWls() {
        Map<String, String> logMap = new HashMap<>();
        logMap.put(getAsWlsMachineId() + AppServerWeblogicMachine.LOGS_GATHERER_ROLE_ID,
                getAsWlsMachineId());
        moveLogs(logMap);
    }

    @SuppressWarnings("WeakerAccess")
    protected void moveLogsWas() {
        Map<String, String> logMap = new HashMap<>();
        logMap.put(getAsWasMachineId() + AppServerWebsphereMachine.LOGS_GATHERER_ROLE_ID,
                getAsWasMachineId());
        moveLogs(logMap);
    }

    @SuppressWarnings("WeakerAccess")
    protected void moveLogsTomcat() {
        Map<String, String> logMap = new HashMap<>();
        logMap.put(getAsTomcatMachineId() + AppServerTomcatMachine.LOGS_GATHERER_ROLE_ID,
                getAsTomcatMachineId());
        moveLogs(logMap);
    }

    @SuppressWarnings("WeakerAccess")
    protected void moveLogsIis() {
        Map<String, String> logMap = new HashMap<>();
        logMap.put(getAsIisMachineId() + AppServerIisMachine.LOGS_GATHERER_ROLE_ID,
                getAsIisMachineId());
        moveLogs(logMap);
    }

    /**
     * @param logsGathererRoles map in [RoleId, MachineId] format
     */
    @SuppressWarnings("WeakerAccess")
    protected void moveLogs(Map<String, String> logsGathererRoles) {
        for (Map.Entry<String, String> logsGathererRole : logsGathererRoles.entrySet()) {
            LogsGathererFlowContext context = (LogsGathererFlowContext) deserializeFlowContextFromRole(logsGathererRole.getKey(),
                    LogsGathererRole.RUN_LOGS_GATHERER, LogsGathererFlowContext.class);
            runFlowByMachineId(logsGathererRole.getValue(), LogsGathererFlow.class, context);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void enableBt(String emRole) {
        EmEmptyDeployFlowContext context = (EmEmptyDeployFlowContext) deserializeFlowContextFromRole(emRole,
                EmEmptyRole.ENV_EM_CTX, EmEmptyDeployFlowContext.class);
        context.getProperties().put("enable.default.BusinessTransaction", "true");
        runFlowByMachineId(getEmMachineId(), EmEmptyConfigureFlow.class, context);
        runFlowByMachineId(getEmMachineId(), EmEmptyRestartFlow.class, context);
    }

    @SuppressWarnings("WeakerAccess")
    protected void disableBt(String emRole) {
        EmEmptyDeployFlowContext context = (EmEmptyDeployFlowContext) deserializeFlowContextFromRole(emRole,
                EmEmptyRole.ENV_EM_CTX, EmEmptyDeployFlowContext.class);
        context.getProperties().put("enable.default.BusinessTransaction", "false");
        runFlowByMachineId(getEmMachineId(), EmEmptyConfigureFlow.class, context);
        runFlowByMachineId(getEmMachineId(), EmEmptyRestartFlow.class, context);
    }

    protected void generateQcUploadResult(String testId, boolean passed, String testSetFolder, String testSetName, String qcUploadToolRole) {
        if (testId != null && !testId.isEmpty()) {
            QcUploadToolSimpleUploadFlowContext context = (QcUploadToolSimpleUploadFlowContext) deserializeFlowContextFromRole(qcUploadToolRole,
                    QcUploadToolRole.RUN_QC_SIMPLE_UPLOAD, QcUploadToolSimpleUploadFlowContext.class);
            context.setTestSetFolder(testSetFolder);
            context.setTestSetName(testSetName);
            context.setTestId(testId);
            context.setPassed(passed);
            runFlowByMachineId(getEmMachineId(), QcUploadToolSimpleUploadFlow.class, context);
        } else {
            LOGGER.warn("ALM TestId is not defined. Skipping ALM upload.");
        }
    }

}
