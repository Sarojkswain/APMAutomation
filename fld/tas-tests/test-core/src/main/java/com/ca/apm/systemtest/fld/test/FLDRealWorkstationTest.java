package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.tas.role.WorkstationRole;

@Test
public class FLDRealWorkstationTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "realworkstation";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(REAL_WORKSTATION_01_ROLE_ID, WorkstationRole.LAUNCH_COMMAND, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(REAL_WORKSTATION_02_ROLE_ID, WorkstationRole.LAUNCH_COMMAND, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRole(REAL_WORKSTATION_01_ROLE_ID, WorkstationRole.STOP_COMMAND);
        runSerializedCommandFlowFromRole(REAL_WORKSTATION_02_ROLE_ID, WorkstationRole.STOP_COMMAND);
    }
}