package com.ca.apm.systemtest.fld.test.smoke;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.FakeWorkStationLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.FLDFakeWorkStationLoadSmokeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

@Tas(testBeds = {@TestBed(name = FLDFakeWorkStationLoadSmokeTestbed.class, executeOn = FLDLoadConstants.FAKEWS01_MACHINE_ID)}, size = SizeType.MEDIUM)
@Test
public class FLDFakeWorkStationLoadSmokeTest extends TasTestNgTest
    implements
        FLDConstants,
        FLDLoadConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(FLDFakeWorkStationLoadSmokeTest.class);

    public void runTest() throws Exception {
        startLoad();
        shortWait(300000L);
        stopLoad();
    }

    private void startLoad() {
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.startLoad():: entry");

        runSerializedCommandFlowFromRoleAsync(FAKEWS01_ROLE_ID, FakeWorkStationLoadRole.FW_LIVEQUERIES_START_LOAD, TimeUnit.DAYS, 28);
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.startLoad():: executed runSerializedCommandFlowFromRoleAsync: {} - {}", FAKEWS01_ROLE_ID, FakeWorkStationLoadRole.FW_LIVEQUERIES_START_LOAD);

        runSerializedCommandFlowFromRoleAsync(FAKEWS02_ROLE_ID, FakeWorkStationLoadRole.FW_HISTORICALQUERY_START_LOAD, TimeUnit.DAYS, 28);
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.startLoad():: executed runSerializedCommandFlowFromRoleAsync: {} - {}", FAKEWS02_ROLE_ID, FakeWorkStationLoadRole.FW_HISTORICALQUERY_START_LOAD);

        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.startLoad():: exit");
    }

    private void stopLoad() {
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.stopLoad():: entry");

        runSerializedCommandFlowFromRoleAsync(FAKEWS01_ROLE_ID, FakeWorkStationLoadRole.FW_LIVEQUERIES_STOP_LOAD);
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.stopLoad():: executed runSerializedCommandFlowFromRoleAsync: {} - {}", FAKEWS01_ROLE_ID, FakeWorkStationLoadRole.FW_LIVEQUERIES_STOP_LOAD);

        runSerializedCommandFlowFromRoleAsync(FAKEWS02_ROLE_ID, FakeWorkStationLoadRole.FW_HISTORICALQUERY_STOP_LOAD);
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.stopLoad():: executed runSerializedCommandFlowFromRoleAsync: {} - {}", FAKEWS02_ROLE_ID, FakeWorkStationLoadRole.FW_HISTORICALQUERY_STOP_LOAD);

        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.stopLoad():: exit");
    }

    private synchronized void shortWait(long ms) {
        LOGGER.info("FLDFakeWorkStationLoadSmokeTest.shortWait():: waiting for {} [ms]", ms);
        try {
            wait(ms);
        } catch (Exception e) {}
    }

}
