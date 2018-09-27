package com.ca.apm.systemtest.fld.test.devel;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.devel.LogMonitorTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

import static org.testng.Assert.assertTrue;


/**
 * @author haiva01
 */
public class LogMonitorTest extends TasTestNgTest {
    @Test(groups = {"windows", "linux"})
    @Tas(testBeds = @TestBed(name = LogMonitorTestbed.class,
        executeOn = LogMonitorTestbed.EM_WINDOWS_MOM_MACHINE_ID), owner = "haiva01")
    public void test() {
        assertTrue(true);
    }
}
