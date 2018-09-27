package com.ca.apm.nextgen.tester;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.nextgen.HvrAgentTestbed;
import com.ca.apm.nextgen.role.HVRAgentRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * This is just a helper class so that the HvrAgentTestbed testbed can be deployed. TAS insists on
 * a test associated with a testbed and refuses to deploy the testbed without the test.
 * Created by haiva01 on 26.2.2016.
 */
public class HvrAgentTestbedTester extends TasTestNgTest {
    @Tas(testBeds = {
        @TestBed(name = HvrAgentTestbed.class, executeOn = HvrAgentTestbed.MACHINE_ID)},
        owner = "haiva01", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void test() throws InterruptedException {
        runSerializedCommandFlowFromRole(HvrAgentTestbed.HVR_ROLE, HVRAgentRole.HVR_LAUNCH_COMMAND);
        TimeUnit.SECONDS.sleep(2);
        runSerializedCommandFlowFromRole(HvrAgentTestbed.HVR_ROLE, HVRAgentRole.HVR_STOP_COMMAND);
    }
}
