package com.ca.apm.tests.test;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.testbed.AssistedTriageSTTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * 
 * @author banra06
 */
public class StartATSTLoad extends TasTestNgTest {

    @Tas(testBeds = @TestBed(name = AssistedTriageSTTestbed.class, executeOn = AssistedTriageSTTestbed.AGC_MACHINE_ID), size = SizeType.BIG, owner = "banra06")
    @Test
    public void startLoads() throws IOException {
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE1_ID + "HVR",
            HVRAgentLoadRole.START_HVR_LOAD_KEY);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE1_ID + "load",
            WurlitzerLoadRole.START_WURLITZER_FLOW_KEY);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE2_ID + "HVR",
            HVRAgentLoadRole.START_HVR_LOAD_KEY);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE2_ID + "load",
            WurlitzerLoadRole.START_WURLITZER_FLOW_KEY);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE3_ID + "HVR",
            HVRAgentLoadRole.START_HVR_LOAD_KEY);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE3_ID + "load",
            WurlitzerLoadRole.START_WURLITZER_FLOW_KEY);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE1_ID + "STRESS",
            ClientDeployRole.STRESSAPP_START_LOAD);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE2_ID + "STRESS",
            ClientDeployRole.STRESSAPP_START_LOAD);
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE3_ID + "STRESS",
            ClientDeployRole.STRESSAPP_START_LOAD);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runSerializedCommandFlowFromRoleAsync(AssistedTriageSTTestbed.LOAD_ROLE1_ID + "STRESS",
            ClientDeployRole.JMETER_START_LOAD);
        assertTrue(true);
    }
}
