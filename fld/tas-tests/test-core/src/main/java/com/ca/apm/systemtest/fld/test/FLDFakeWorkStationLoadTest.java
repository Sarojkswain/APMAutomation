package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.FakeWorkStationLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Starts the Fake Workstation load in the FLD.
 * Should start after wurlitzer loads.
 * 
 * @author filja01
 *
 */
@Test
public class FLDFakeWorkStationLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "fakeworkstation";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(FAKEWS01_ROLE_ID, FakeWorkStationLoadRole.FW_LIVEQUERIES_START_LOAD, TimeUnit.DAYS, 28);
        //shortWait(15000L);
        runSerializedCommandFlowFromRoleAsync(FAKEWS02_ROLE_ID, FakeWorkStationLoadRole.FW_HISTORICALQUERY_START_LOAD, TimeUnit.DAYS, 28);
        //shortWait(30000L);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(FAKEWS01_ROLE_ID, FakeWorkStationLoadRole.FW_LIVEQUERIES_STOP_LOAD);
        runSerializedCommandFlowFromRoleAsync(FAKEWS02_ROLE_ID, FakeWorkStationLoadRole.FW_HISTORICALQUERY_STOP_LOAD);
    }

    /*
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {}
    }
    */

}
