/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FLDSingleCollectorTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


/**
 * Replaces the now broken groovy scripts used on fldwls01/02 for generating load on the JAXWS services
 * @author keyja01
 *
 */
@Test
@Tas(owner = "keyja01", size = SizeType.HUMONGOUS, testBeds = {@TestBed(executeOn = FLDConstants.MOM_MACHINE_ID, name = FLDSingleCollectorTestbed.class)})
public class WebLogicServerSOALoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    
    @Override
    protected String getLoadName() {
        return "weblogic-soaload";
    }

    @Override
    protected void startLoad() {
        System.out.println("FldLoadWeblogicTestbed.webLogicServerSOALoadTest()::START");
        runSerializedCommandFlowFromRoleAsync(WLS_01_MACHINE_ID + "-SOALoadRole_JM", JMeterRole.ENV_JMETER_START, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(WLS_02_MACHINE_ID + "-SOALoadRole_JM", JMeterRole.ENV_JMETER_START, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        System.out.println("FldLoadWeblogicTestbed.webLogicServerSOALoadTest()::STOP");
        runSerializedCommandFlowFromRoleAsync(WLS_01_MACHINE_ID + "-SOALoadRole_JM", JMeterRole.ENV_JMETER_STOP, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(WLS_02_MACHINE_ID + "-SOALoadRole_JM", JMeterRole.ENV_JMETER_STOP, TimeUnit.DAYS, 28);
    }
}
