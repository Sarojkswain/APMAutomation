/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * @author keyja01
 *
 */
@Test
public class HVRAgentLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "hvr-agent";
    }

    @Override
    protected void startLoad() {
        //wait 10 minutes with start
        shortWait(600000L);
        
        runSerializedCommandFlowFromRoleAsync(HVR_LOAD_ROLE_ID, HVRAgentLoadRole.START_HVR_LOAD_KEY, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(HVR_LOAD_ROLE_ID, HVRAgentLoadRole.STOP_HVR_LOAD_KEY);
    }
    
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
        }
    }
}
