package com.ca.apm.systemtest.fld.test.devel;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.devel.MavenDeployTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

import static org.testng.Assert.assertTrue;

/**
 * @author haiva01
 */
public class MavenTest extends TasTestNgTest {
    @Test(groups = {"windows", "linux"})
    @Tas(testBeds = @TestBed(name = MavenDeployTestbed.class,
        executeOn = MavenDeployTestbed.MAVEN_WINDOWS_MACHINE_ID), owner = "haiva01")
    public void test() {
        assertTrue(true);
    }
}
