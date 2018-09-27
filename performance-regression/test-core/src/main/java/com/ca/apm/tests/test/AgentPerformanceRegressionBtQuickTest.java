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
import com.ca.apm.tests.testbed.machines.Em104Machine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * SampleTest class
 * <p/>
 * Test description
 */
public class AgentPerformanceRegressionBtQuickTest extends AgentPerformanceRegression104Test {

    public AgentPerformanceRegressionBtQuickTest() throws Exception {
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        EXEC_NO_AGENT = false;
        EXEC_CURRENT_NO_SI = false;
        EXEC_CURRENT_SI = false;
        EXEC_CURRENT_ACC = false;
        EXEC_CURRENT_BRTM = false;
        EXEC_PREV_NO_SI = false;
        EXEC_PREV_SI = false;

        JMETER_RAMP_UP_TIME_SEC = 0L; // 2 min
        JMETER_STARTUP_DELAY_SEC = 0L; // 30 sec
        JMETER_RUN_TIME_SEC = 30L; // 40 min
        METRICS_RUN_TIME_SEC = JMETER_RUN_TIME_SEC + (1L * 60);
        RUNS = 1;
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104TestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void btTest() throws Exception {
        enableBt(getEmMachineId() + Em104Machine.EM_ROLE_ID);
        disableBt(getEmMachineId() + Em104Machine.EM_ROLE_ID);
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
}
