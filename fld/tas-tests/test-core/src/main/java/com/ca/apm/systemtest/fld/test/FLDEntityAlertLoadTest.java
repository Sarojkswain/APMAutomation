/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.EntityAlertLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Starts the Entity Alert load in the FLD.
 * @author filja01
 *
 */
@Test
public class FLDEntityAlertLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    
    @Override
    protected String getLoadName() {
        return "entityalert";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(ENTITY_ALERT_ROLE_ID, EntityAlertLoadRole.ENTITYALERT_START_LOAD, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(ENTITY_ALERT_ROLE_ID, EntityAlertLoadRole.ENTITYALERT_STOP_LOAD);
    }
}
