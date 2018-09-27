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

import com.ca.apm.tests.testbed.AgentPerformanceRegression104TestBed;
import com.ca.apm.tests.testbed.machines.*;
import com.ca.apm.tests.testbed.machines.template.JMeterMachineAbs;
import com.ca.apm.tests.testbed.machines.template.JavaAgentMachine;
import com.ca.apm.tests.testbed.machines.template.NetAgentMachine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SampleTest class
 * <p/>
 * Test description
 */
public class AgentPerformanceRegression104Test extends AgentPerformanceRegressionTest {

    public static final String EM_MACHINE_ID = AgentPerformanceRegression104TestBed.EM_MACHINE_ID;

    static boolean EXEC_CURRENT_NO_SI_BT; // only for 10.4+

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentPerformanceRegression104Test.class);

    // TEST IDs FOR ALM (QC)
    @SuppressWarnings("WeakerAccess")
    protected static String TESTID_IIS_NO_DI_BT;
    @SuppressWarnings("WeakerAccess")
    protected static String TESTID_TOMCAT_NO_SI_BT;
    @SuppressWarnings("WeakerAccess")
    protected static String TESTID_WAS_NO_SI_BT;
    @SuppressWarnings("WeakerAccess")
    protected static String TESTID_WLS_NO_SI_BT;


    AgentPerformanceRegression104Test() throws Exception {
    }

    protected String getEmMachineId() {
        return EM_MACHINE_ID;
    }

    protected String getAsTomcatMachineId() {
        return AgentPerformanceRegression104TestBed.AS_TOMCAT_MACHINE_ID;
    }

    protected String getAsWasMachineId() {
        return AgentPerformanceRegression104TestBed.AS_WAS_MACHINE_ID;
    }

    protected String getAsWlsMachineId() {
        return AgentPerformanceRegression104TestBed.AS_WLS_MACHINE_ID;
    }

    protected String getAsIisMachineId() {
        return AgentPerformanceRegression104TestBed.AS_IIS_MACHINE_ID;
    }

    protected String getDbTomcatMachineId() {
        return AgentPerformanceRegression104TestBed.DB_ORACLE_TOMCAT_MACHINE_ID;
    }

    protected String getDbWasMachineId() {
        return AgentPerformanceRegression104TestBed.DB_ORACLE_WAS_MACHINE_ID;
    }

    protected String getDbWlsMachineId() {
        return AgentPerformanceRegression104TestBed.DB_ORACLE_WLS_MACHINE_ID;
    }

    protected String getJmeterTomcatMachineId() {
        return AgentPerformanceRegression104TestBed.JMETER_TOMCAT_MACHINE_ID;
    }

    protected String getJmeterWasMachineId() {
        return AgentPerformanceRegression104TestBed.JMETER_WAS_MACHINE_ID;
    }

    protected String getJmeterWlsMachineId() {
        return AgentPerformanceRegression104TestBed.JMETER_WLS_MACHINE_ID;
    }

    protected String getJmeterIisMachineId() {
        return AgentPerformanceRegression104TestBed.JMETER_IIS_MACHINE_ID;
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        EXEC_CURRENT_NO_SI_BT = true;

        TESTID_IIS_NO_AGENT = "455626";
        TESTID_TOMCAT_NO_AGENT = "455633";
        TESTID_WAS_NO_AGENT = "455639";
        TESTID_WLS_NO_AGENT = "455644";

        TESTID_IIS_CURRENT_NO_DI = "455624";
        TESTID_TOMCAT_CURRENT_NO_SI = "455630";
        TESTID_WAS_CURRENT_NO_SI = "455637";
        TESTID_WLS_CURRENT_NO_SI = "455642";

        TESTID_IIS_CURRENT_DI = "455625";
        TESTID_TOMCAT_CURRENT_SI = "455632";
        TESTID_WAS_CURRENT_SI = "455638";
        TESTID_WLS_CURRENT_SI = "455643";

        TESTID_IIS_PREV_NO_DI = "455627";
        TESTID_TOMCAT_PREV_NO_SI = "455634";
        TESTID_WAS_PREV_NO_SI = "455640";
        TESTID_WLS_PREV_NO_SI = "455645";

        TESTID_IIS_PREV_DI = "455628";
        TESTID_TOMCAT_PREV_SI = "455635";
        TESTID_WAS_PREV_SI = "455641";
        TESTID_WLS_PREV_SI = "455646";

        TESTID_TOMCAT_CURRENT_ACC = "455629";
        TESTID_WAS_CURRENT_ACC = "455636";
        TESTID_WLS_CURRENT_ACC = "455647";

        TESTID_TOMCAT_CURRENT_BRTM = "455631";

        TESTID_IIS_NO_DI_BT = "455653";
        TESTID_TOMCAT_NO_SI_BT = "455660";
        TESTID_WAS_NO_SI_BT = "455666";
        TESTID_WLS_NO_SI_BT = "455671";

        TESTSET_FOLDER = "Root\\\\\\\\APM 10.5\\\\\\\\System\\\\\\\\Automation\\\\\\\\Data Gatherer\\\\\\\\Performance Overhead";
        TESTSET_NAME = "APM 10.5 - System Test - Agent Performance Overhead Regression";

        IIS_APP = IisAppType.NerdDinner4;
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
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
        super.regressionTestAll();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun1() throws Exception {
        super.regressionTestRun1();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun2() throws Exception {
        super.regressionTestRun2();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun3() throws Exception {
        super.regressionTestRun3();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun4() throws Exception {
        super.regressionTestRun4();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun5() throws Exception {
        super.regressionTestRun5();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void stopAppServers() {
        super.stopAppServers();
    }


    public int regressionTestRunAll(int runNumber) throws Exception {
        int errors = 0;
        errors += EXEC_NO_AGENT ? testNoAgentRun() : 0;
        errors += EXEC_CURRENT_NO_SI ? testCurrentNoSiRun(false) : 0;
        errors += EXEC_CURRENT_NO_SI_BT ? testCurrentNoSiBtRun(true) : 0;
        errors += EXEC_CURRENT_SI ? testCurrentSiRun(true) : 0;
        errors += EXEC_PREV_NO_SI ? testPrevNoSiRun(false) : 0;
        errors += EXEC_PREV_SI ? testPrevSiRun(false) : 0;
        errors += EXEC_CURRENT_ACC ? testCurrentNoSiAccRun(true) : 0;
        errors += EXEC_CURRENT_BRTM ? testCurrentNoSiBrtmRun(true) : 0;
        //
        generateXls(getEmMachineId() + EmMachine.CSV_TO_XLS_ROLE_ID, runNumber);
        Thread.sleep(10000);
        LOGGER.info("=============== Total Test Errors: " + errors + " ===============");
        return errors;
    }

    /**
     * /////////////////////////////////////
     * // current no SI BT (only for 10.4+)
     * /////////////////////////////////////
     */
    private int testCurrentNoSiBtRun(boolean startAccMock) throws Exception {
        List<Thread> startAppServerThreads = new CopyOnWriteArrayList<>();
        List<Thread> execThreads = new CopyOnWriteArrayList<>();
        List<Thread> stopAppServerThreads = new CopyOnWriteArrayList<>();
        try {
            // RECREATE DB SCHEMAS
            recreateDbSchemas();
            // ENABLE BT
            enableBt(getEmMachineId() + EmMachine.EM_ROLE_ID);
            // START
            if (EXECUTE_TOMCAT) {
                startAppServerThreads.add(getStartTomcatThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_BT_ROLE_ID, AppServerTomcatMachine.TOMCAT_ROLE_ID,
                        startAccMock ? AppServerTomcatMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_BT_ROLE_ID, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID,
                        startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_WAS) {
                startAppServerThreads.add(getStartWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_BT_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
            }
            if (EXECUTE_IIS) {
                startAppServerThreads.add(getStartIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_NO_DI_BT_ROLE_ID));
            }
            startThreads(startAppServerThreads);
            waitForThreads(startAppServerThreads, 5000);
            // CONFIGURE
            if (EXECUTE_TOMCAT) {
                execThreads.add(getTypeperfThread(getAsTomcatMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_BT_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsTomcatMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_BT_ROLE_ID,
                        getAsTomcatMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterTomcatMachineId(),
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_BT_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_BT_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterWeblogicMachine.JMETER_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_WAS) {
                execThreads.add(getTypeperfThread(getAsWasMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_BT_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJmxThread(getAsWasMachineId() + JavaAgentMachine.JMXMON_CURRENT_NO_SI_BT_ROLE_ID,
                        getAsWasMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterWebsphereMachine.JMETER_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC));
            }
            if (EXECUTE_IIS) {
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_CPU_CURRENT_NO_DI_BT_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getTypeperfThread(getAsIisMachineId() + NetAgentMachine.PERFMON_MEM_CURRENT_NO_DI_BT_ROLE_ID,
                        getAsIisMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterIisMachine.JMETER_CURRENT_NO_SI_BT_ROLE_ID,
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
                        getJmeterTomcatMachineId() + JMeterTomcatMachine.JMETER_STATS_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_TOMCAT_NO_SI_BT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_NO_SI_BT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_BT_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_NO_SI_BT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_IIS) {
                stopAppServerThreads.add(getStopIisThread(getAsIisMachineId() + AppServerIisMachine.IIS_ROLE_ID,
                        getAsIisMachineId() + NetAgentMachine.AGENT_CURRENT_NO_DI_BT_ROLE_ID));
                generateJMeterStats(
                        getJmeterIisMachineId(),
                        getJmeterIisMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_BT_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_IIS_NO_DI_BT, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            disableBt(getEmMachineId() + EmMachine.EM_ROLE_ID);
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
    @Override
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
            if (EXECUTE_WLS) {
                startAppServerThreads.add(getStartWlsThread(JavaAgentMachine.AGENT_CURRENT_NO_SI_ACC_ROLE_ID, AppServerWeblogicMachine.WEBLOGIC_ROLE_ID,
                        startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
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
            if (EXECUTE_WLS) {
                execThreads.add(getTypeperfThread(getAsWlsMachineId() + JavaAgentMachine.PERFMON_CURRENT_NO_SI_ACC_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJstatThread(getAsWlsMachineId() + JavaAgentMachine.JSTAT_CURRENT_NO_SI_ACC_ROLE_ID,
                        getAsWlsMachineId(), METRICS_RUN_TIME_SEC));
                execThreads.add(getJMeterThread(
                        getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterWeblogicMachine.JMETER_CURRENT_NO_SI_ACC_ROLE_ID,
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
                generateQcUploadResult(TESTID_TOMCAT_CURRENT_ACC, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WLS) {
                stopAppServerThreads.add(getStopWlsThread(AppServerWeblogicMachine.WEBLOGIC_ROLE_ID, startAccMock ? AppServerWeblogicMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(getJmeterWlsMachineId(),
                        getJmeterWlsMachineId() + JMeterWeblogicMachine.JMETER_STATS_CURRENT_NO_SI_ACC_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WLS_CURRENT_ACC, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            if (EXECUTE_WAS) {
                stopAppServerThreads.add(getStopWasThread(AppServerWebsphereMachine.WEBSPHERE_ROLE_ID, JavaAgentMachine.AGENT_CURRENT_NO_SI_ACC_ROLE_ID,
                        startAccMock ? AppServerWebsphereMachine.ACC_CONTROLLER_ROLE_ID : null));
                generateJMeterStats(
                        getJmeterWasMachineId(),
                        getJmeterWasMachineId() + JMeterMachineAbs.JMETER_STATS_CURRENT_NO_SI_ACC_ROLE_ID,
                        JMETER_RUN_TIME_SEC, JMETER_RAMP_UP_TIME_SEC, JMETER_STARTUP_DELAY_SEC);
                generateQcUploadResult(TESTID_WAS_CURRENT_ACC, true, TESTSET_FOLDER, TESTSET_NAME, getEmMachineId() + Em104Machine.QC_UPLOAD_ROLE_ID);
            }
            startThreads(stopAppServerThreads);
            waitForThreads(stopAppServerThreads, 5000);
            return 0;
        } catch (Exception e) {
            //noinspection unchecked
            return handleException(e, startAppServerThreads, execThreads);
        }
    }

    protected void generateXls(String csvToXlsRole, Integer runNumber) {
        generateXls(csvToXlsRole, runNumber, "AgentPerformanceTemplate_40min_10.5.xls", "Results_40min");
    }

}
