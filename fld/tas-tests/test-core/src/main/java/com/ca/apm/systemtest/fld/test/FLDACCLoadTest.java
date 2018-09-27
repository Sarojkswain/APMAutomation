/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.ACCLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Disallowed Agent Load Scripts for EM FLD.
 * @author filja01
 *
 */
@Test
public class FLDACCLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "disallowedagent";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(ACC_ROLE_ID, ACCLoadRole.ACC_START_LOAD, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(ACC_ROLE_ID, ACCLoadRole.ACC_STOP_LOAD);
    }
}
