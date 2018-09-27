/**
 * 
 */
package com.ca.apm.systemtest.fld.test.smoke;

import java.util.Timer;
import java.util.TimerTask;

import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.FldControllerSmokeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * @author keyja01
 *
 */
@Tas(owner = "keyja01", testBeds = @TestBed(name = FldControllerSmokeTestbed.class, executeOn = FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID))
@Test
public class FldControllerSmokeTest extends BaseFldLoadTest {

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#getLoadName()
     */
    @Override
    protected String getLoadName() {
        return "FLD";
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#startLoad()
     */
    @Override
    protected void startLoad() {
        logger.info("Starting FldControllerSmokeLoad");
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            
            @Override
            public void run() {
                FileModifierFlowContext ctx = new FileModifierFlowContext.Builder().delete("c:\\fld\\markers\\FLD.started").build();
                logger.info("Deleting FLD.started marker file to stop the test");
                runFlowByMachineId(FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID, FileModifierFlow.class, ctx);
                logger.info("Deleted FLD.started marker file to stop the test");
            }
        }, 15000L);
        
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#stopLoad()
     */
    @Override
    protected void stopLoad() {
        logger.info("Stopping FldControllerSmokeLoad");
    }

}
