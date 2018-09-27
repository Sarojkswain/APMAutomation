/**
 * 
 */
package com.ca.apm.systemtest.fld.test.smoke;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.FlexLoadSmokeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * @author keyja01
 *
 */
@Test
@Tas(testBeds = {@TestBed(name=FlexLoadSmokeTestbed.class, executeOn=FLDLoadConstants.JMETER_MACHINE_ID)})
public class FlexLoadSmokeTest extends TasTestNgTest implements FLDLoadConstants {

    public void runTest() throws Exception {
        runSerializedCommandFlowFromRoleAsync(JMETER_ROLE_AMF_ID, JMeterRole.ENV_JMETER_START);
        
        Thread.sleep(120000L);
        
        runSerializedCommandFlowFromRoleAsync(JMETER_ROLE_AMF_ID, JMeterRole.ENV_JMETER_STOP);
    }

}
