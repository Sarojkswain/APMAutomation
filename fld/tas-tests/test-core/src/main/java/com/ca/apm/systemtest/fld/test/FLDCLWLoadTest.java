/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Command Line Workstation Load Scripts for EM FLD. Real agent load should be
 * up and running before running these queries
 * @author filja01
 *
 */
@Test
public class FLDCLWLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
        }
    }

    @Override
    protected String getLoadName() {
        return "clwload";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(CLW_ROLE_ID, CLWWorkStationLoadRole.CLW_START_LOAD, TimeUnit.DAYS, 28);
        shortWait(15000L);
        runSerializedCommandFlowFromRoleAsync(CLW_ROLE_ID, CLWWorkStationLoadRole.HCLW_START_LOAD, TimeUnit.DAYS, 28);
        shortWait(30000L);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(CLW_ROLE_ID, CLWWorkStationLoadRole.CLW_STOP_LOAD);
        runSerializedCommandFlowFromRoleAsync(CLW_ROLE_ID, CLWWorkStationLoadRole.HCLW_STOP_LOAD);
    }
}
