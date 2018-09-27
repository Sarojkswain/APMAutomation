package com.ca.apm.transactiontrace.appmap.test;

import com.ca.apm.transactiontrace.appmap.testbed.ClusteredTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

/**
 * @author Sundeep (bhusu01)
 */
@Test(groups = {"appmap"})
@Tas(testBeds = @TestBed(name = ClusteredTestbed.class, executeOn = ClusteredTestbed.MOM_MACHINE_ID), size = SizeType.MEDIUM, owner = "bhusu01")
public class ClusteredTracedAppMapTestChrome extends TracedAppMapTest {

    public ClusteredTracedAppMapTestChrome() {
        super(DesiredCapabilities.chrome(), ClusteredTestbed.MOM_ROLE_ID);
    }
}
