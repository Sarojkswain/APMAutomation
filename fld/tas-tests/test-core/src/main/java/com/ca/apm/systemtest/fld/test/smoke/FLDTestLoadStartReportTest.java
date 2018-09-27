package com.ca.apm.systemtest.fld.test.smoke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.FldControllerSmokeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * Test load that starts successfully and is terminated by the grouping "FLD" load after 5 mins.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Tas(owner = "sinal04", testBeds = @TestBed(name = FldControllerSmokeTestbed.class, executeOn = FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID))
@Test(groups = {"FLD_LOAD_REPORT"})
public class FLDTestLoadStartReportTest extends BaseFldLoadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FLDTestLoadStartReportTest.class);
    private static final Long DELAY_2_MINS = 120000L;
    
    @Override
    public void runLoadTest() {
        try {
            super.runLoadTest();
            Thread.sleep(DELAY_2_MINS);
        } catch (Throwable t) {
            LOGGER.error("FLDTestLoadStartReportTest | exception: ", t);
        }
    }
    
    @Override
    protected String getLoadName() {
        return "Test load that starts successfully.";
    }

    @Override
    protected void startLoad() {
        LOGGER.info("Starting FLDTestLoadStartReportTest");
    }

    @Override
    protected void stopLoad() {
        LOGGER.info("Stopping FLDTestLoadStartReportTest");
    }

}
