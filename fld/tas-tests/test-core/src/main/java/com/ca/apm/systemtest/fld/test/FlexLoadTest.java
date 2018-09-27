package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

@Test
public class FlexLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    
    @Override
    protected String getLoadName() {
        return "flexload";
    }

    @Override
    protected void startLoad() {
        System.out.println("FlexLoadTestbed.flexLoadTest()::START");
        runSerializedCommandFlowFromRoleAsync(JMETER_ROLE_AMF_ID, JMeterRole.ENV_JMETER_START, TimeUnit.DAYS, 28);
        runSerializedCommandFlowFromRoleAsync(JMETER_ROLE_AMFX_ID, JMeterRole.ENV_JMETER_START, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        System.out.println("FlexLoadTestbed.flexLoadTest()::STOP");
        runSerializedCommandFlowFromRole(JMETER_ROLE_AMF_ID, JMeterRole.ENV_JMETER_STOP, TimeUnit.MINUTES, 5);
        runSerializedCommandFlowFromRole(JMETER_ROLE_AMFX_ID, JMeterRole.ENV_JMETER_STOP, TimeUnit.MINUTES, 5);
    }

}
