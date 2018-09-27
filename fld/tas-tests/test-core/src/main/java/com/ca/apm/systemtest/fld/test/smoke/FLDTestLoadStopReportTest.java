package com.ca.apm.systemtest.fld.test.smoke;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.flow.controller.FldLoadStatus;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.FldControllerSmokeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * Test load that stops successfully after approx. 150 sec since it started.  
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Tas(owner = "sinal04", testBeds = @TestBed(name = FldControllerSmokeTestbed.class, executeOn = FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID))
@Test(groups = {"FLD_LOAD_REPORT"})
public class FLDTestLoadStopReportTest extends BaseFldLoadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FLDTestLoadStopReportTest.class);
    private static final Long DELAY_150_SEC = 150000L;//2.5 mins
    
    @Override
    public void runLoadTest() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                getDistributedStatusMap().put(getLoadName(), FldLoadStatus.DOSTOP);
            }
        }, DELAY_150_SEC);

        try {
            super.runLoadTest();
        } catch (Throwable t) {
            LOGGER.error("FLDTestLoadStopReportTest | exception: ", t);
        }
    }
    
    @Override
    protected String getLoadName() {
        return "Test load that stops successfully.";
    }

    @Override
    protected void startLoad() {
        LOGGER.info("Starting FLDTestLoadStopReportTest");
    }

    @Override
    protected void stopLoad() {
        LOGGER.info("Stopping FLDTestLoadStopReportTest");
    }

}
