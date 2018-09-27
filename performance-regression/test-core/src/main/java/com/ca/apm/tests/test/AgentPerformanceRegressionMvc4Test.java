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

import com.ca.apm.tests.testbed.featureTestbeds.AgentPerformanceRegression104Mvc4PredeployedTestBed;
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
public class AgentPerformanceRegressionMvc4Test extends AgentPerformanceRegressionFullTest {

    public AgentPerformanceRegressionMvc4Test() throws Exception {
    }

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();

        // Execute only IIS
        EXECUTE_TOMCAT = false;
        EXECUTE_WAS = false;
        EXECUTE_WLS = false;
        EXECUTE_IIS = true;

        EXEC_NO_AGENT = true;
        EXEC_CURRENT_NO_SI = true;
        EXEC_CURRENT_SI = true;
        EXEC_CURRENT_ACC = false; // NO ACC
        EXEC_CURRENT_BRTM = false; // NO BRTM
        EXEC_PREV_NO_SI = true;
        EXEC_PREV_SI = true;

        IIS_APP = IisAppType.NerdDinner4;
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
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
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun1() throws Exception {
        super.regressionTestRun1();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun2() throws Exception {
        super.regressionTestRun2();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun3() throws Exception {
        super.regressionTestRun3();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun4() throws Exception {
        super.regressionTestRun4();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void regressionTestRun5() throws Exception {
        super.regressionTestRun5();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void stopAppServers() {
        super.stopAppServers();
    }

    @Tas(
            testBeds = @TestBed(name = AgentPerformanceRegression104Mvc4PredeployedTestBed.class,
                    executeOn = AgentPerformanceRegression104Mvc4PredeployedTestBed.EM_MACHINE_ID),
            size = SizeType.COLOSSAL,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void moveLogsTest() throws Exception {
        super.moveLogsTest();
    }

    protected void generateXls(String csvToXlsRole, Integer runNumber) {
        generateXls(csvToXlsRole, runNumber, "AgentPerformanceTemplate_40min_10.4.xls", "Results_40min");
    }
}
