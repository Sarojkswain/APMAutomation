package com.ca.apm.transactiontrace.appmap.test;

import com.ca.apm.transactiontrace.appmap.testbed.StandAloneTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

/**
 * @author Sundeep (bhusu01)
 */
@Test(groups = {"appmap"})
@Tas(testBeds = @TestBed(name = StandAloneTestbed.class, executeOn = StandAloneTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "bhusu01")
public class TracedAppMapTestChrome extends TracedAppMapTest {

    public TracedAppMapTestChrome() {
        super(DesiredCapabilities.chrome(), StandAloneTestbed.EM_ROLE_ID);
    }
}
