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
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Tas(owner = "sinal04", testBeds = @TestBed(name = FldControllerSmokeTestbed.class, executeOn = FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID))
@Test(groups = {"FLD_LOAD_REPORT"})
public class FLDTestLoadStartFailReportTest extends BaseFldLoadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FLDTestLoadStartFailReportTest.class);

    @Override
    public void runLoadTest() {
        try {
            super.runLoadTest();
        } catch (Throwable t) {
            LOGGER.error("FLDTestLoadStartFailReportTest | exception: ", t);
        }
    }
    
    @Override
    protected String getLoadName() {
        return "Test load that fails to start.";
    }

    @Override
    protected void startLoad() {
        LOGGER.info("Starting FLDTestLoadStartFailReportTest");
        throw new RuntimeException("FLDTestLoadStartFailReportTest throws exception");
    }

    @Override
    protected void stopLoad() {
        LOGGER.info("Stopping FLDTestLoadStartFailReportTest");
    }

}
