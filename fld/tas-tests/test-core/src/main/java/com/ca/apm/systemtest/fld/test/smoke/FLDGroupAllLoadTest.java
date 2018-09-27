package com.ca.apm.systemtest.fld.test.smoke;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.FldControllerSmokeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * Smoke test that groups other loads and cancells them automatically after delay of 5 mins.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Tas(owner = "sinal04", testBeds = @TestBed(name = FldControllerSmokeTestbed.class, executeOn = FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID))
@Test
public class FLDGroupAllLoadTest extends BaseFldLoadTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FLDGroupAllLoadTest.class);
    private static final Long DELAY_5_MIN = 300000L;
    
    @Override
    protected String getLoadName() {
        return "FLD";
    }

    @Override
    protected void startLoad() {
        LOGGER.info("Starting FLDGroupAllLoadTest");
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                FileModifierFlowContext ctx = new FileModifierFlowContext.Builder().delete("c:\\fld\\markers\\FLD.started").build();
                LOGGER.info("Deleting FLD.started marker file to stop the test");
                runFlowByMachineId(FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID, FileModifierFlow.class, ctx);
                LOGGER.info("Deleted FLD.started marker file to stop the test");
            }
        }, DELAY_5_MIN);
    }

    @Override
    protected void stopLoad() {
        LOGGER.info("Stopping FLDGroupAllLoadTest");
    }

}
