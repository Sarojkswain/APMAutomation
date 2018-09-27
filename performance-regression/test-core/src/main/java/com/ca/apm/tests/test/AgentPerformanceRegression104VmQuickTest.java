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

import com.ca.apm.tests.testbed.AgentPerformanceRegression104VmTestBed;
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
public class AgentPerformanceRegression104VmQuickTest extends AgentPerformanceRegression104Test {

    public AgentPerformanceRegression104VmQuickTest() throws Exception {
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        JMETER_RAMP_UP_TIME_SEC = 0L; // 2 min
        JMETER_STARTUP_DELAY_SEC = 0L; // 30 sec
        JMETER_RUN_TIME_SEC = 30L; // 40 min
        METRICS_RUN_TIME_SEC = JMETER_RUN_TIME_SEC + (1L * 60);
        RUNS = 1;

        EXEC_NO_AGENT = true;
        EXEC_CURRENT_NO_SI = true;
        EXEC_CURRENT_NO_SI_BT = true;
        EXEC_CURRENT_SI = true;
        EXEC_CURRENT_ACC = true;
        EXEC_CURRENT_BRTM = true;
        EXEC_PREV_NO_SI = true;
        EXEC_PREV_SI = true;

        RECREATE_DB = false;
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104VmTestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun1() throws Exception {
        super.regressionTestRun1();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104VmTestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun2() throws Exception {
        super.regressionTestRun2();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104VmTestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun3() throws Exception {
        super.regressionTestRun3();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104VmTestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun4() throws Exception {
        super.regressionTestRun4();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104VmTestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun5() throws Exception {
        super.regressionTestRun5();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104VmTestBed.class,
                    executeOn = EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void stopAppServers() {
        super.stopAppServers();
    }
}
