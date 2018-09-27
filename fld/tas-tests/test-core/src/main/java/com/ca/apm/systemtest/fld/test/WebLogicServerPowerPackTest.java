package com.ca.apm.systemtest.fld.test;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.WebLogicServerPowerPackTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

public class WebLogicServerPowerPackTest {

    @Tas(testBeds = @TestBed(name = WebLogicServerPowerPackTestbed.class,
        executeOn = WebLogicServerPowerPackTestbed.MACHINE_ID),
        owner = "sinal04", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"wlsPPDeployment"})
    public void testDeployment() {
    }
    
}
