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

import com.ca.apm.tests.testbed.featureTestbeds.AgentPerformanceRegressionF21462PredeployedTestBed;
import com.ca.apm.tests.testbed.machines.EmMachine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class for testing performance of 10.4 Agent with NerdDinner IIS APP.
 * <p/>
 * Test description
 */
public class AgentPerformanceRegressionF21462Test extends AgentPerformanceRegression104FullTest {

    public AgentPerformanceRegressionF21462Test() throws Exception {
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

//        JMETER_RAMP_UP_TIME_SEC = 0L; // 2 min
//        JMETER_STARTUP_DELAY_SEC = 0L; // 30 sec
//        JMETER_RUN_TIME_SEC = 30L; // 40 min
//        METRICS_RUN_TIME_SEC = JMETER_RUN_TIME_SEC + (1L * 60);
//        RUNS = 1;

        // Execute only TOMCAT
        EXECUTE_TOMCAT = true;
        EXECUTE_WAS = false;
        EXECUTE_WLS = false;
        EXECUTE_IIS = false;

        EXEC_NO_AGENT = false; // NO NOAGENT
        EXEC_CURRENT_NO_SI = false; // NO NOSI
        EXEC_CURRENT_NO_SI_BT = false; // NO NOSI BT
        EXEC_CURRENT_SI = true;
        EXEC_CURRENT_ACC = false; // NO ACC
        EXEC_CURRENT_BRTM = false; // NO BRTM
        EXEC_PREV_NO_SI = false; // NO NOSI
        EXEC_PREV_SI = true;
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegressionF21462PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegressionF21462PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun1() throws Exception {
        EXEC_CURRENT_SI = true;
        EXEC_PREV_SI = true;

        enableBt(getEmMachineId() + EmMachine.EM_ROLE_ID);
        super.regressionTestRun1();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegressionF21462PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegressionF21462PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestFeatureRun1() throws Exception {
        EXEC_CURRENT_SI = true;
        EXEC_PREV_SI = false;

        enableBt(getEmMachineId() + EmMachine.EM_ROLE_ID);
        super.regressionTestRun1();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegressionF21462PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegressionF21462PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestSysRun1() throws Exception {
        EXEC_CURRENT_SI = false;
        EXEC_PREV_SI = true;

        enableBt(getEmMachineId() + EmMachine.EM_ROLE_ID);
        super.regressionTestRun1();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegressionF21462PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegressionF21462PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void stopAppServers() {
        super.stopAppServers();
    }

    protected void generateXls(String csvToXlsRole, Integer runNumber) {
        generateXls(csvToXlsRole, runNumber, "AgentPerformanceF21462Template_40min_10.4.xls", "F21462_Results_40min");
    }
}
