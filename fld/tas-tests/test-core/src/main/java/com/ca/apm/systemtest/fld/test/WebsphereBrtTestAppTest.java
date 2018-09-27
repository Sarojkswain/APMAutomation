package com.ca.apm.systemtest.fld.test;

import com.ca.apm.systemtest.fld.testbed.EmPluginTestBed;
import com.ca.apm.systemtest.fld.testbed.WebsphereBrtTestAppTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.testng.annotations.Test;

public class WebsphereBrtTestAppTest {

    @Tas(testBeds = @TestBed(name = WebsphereBrtTestAppTestbed.class,
            executeOn = WebsphereBrtTestAppTestbed.WAS_MACHINE_ID + "," + EmPluginTestBed.MOM_MACHINE_ID),
            owner = "meler02",
            size = SizeType.DEBUG,
            exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"wasBrtDeployment"})
    public void testDeployment() {
    }

}
