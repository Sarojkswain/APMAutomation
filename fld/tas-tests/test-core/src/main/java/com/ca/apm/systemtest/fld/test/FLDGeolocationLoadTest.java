/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.GeolocationLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Starts the geolocation load in the FLD
 * @author filja01
 *
 */
@Test
public class FLDGeolocationLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    
    @Override
    protected String getLoadName() {
        return "geolocation";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(GEOLOCATION_LOAD_TIM01_ROLE_ID, GeolocationLoadRole.GEOLOCATION_START_LOAD, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(GEOLOCATION_LOAD_TIM01_ROLE_ID, GeolocationLoadRole.GEOLOCATION_STOP_LOAD);
    }
}
