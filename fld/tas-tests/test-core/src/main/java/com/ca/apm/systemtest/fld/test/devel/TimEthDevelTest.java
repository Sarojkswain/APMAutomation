package com.ca.apm.systemtest.fld.test.devel;

import static com.ca.apm.systemtest.fld.testbed.devel.TimEthTestbed.SELENIUM_MACHINE_ID;
import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.devel.TimEthTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * @author haiva01
 */
public class TimEthDevelTest extends TasTestNgTest {
    private final Logger log = LoggerFactory.getLogger(TimEthDevelTest.class);

    @Test(groups = {"windows", "linux"})
    @Tas(testBeds = @TestBed(name = TimEthTestbed.class, executeOn = SELENIUM_MACHINE_ID),
        owner = "haiva01")
    public void test() {
        log.trace("Running TimEthDevelTest::test()");
        //FIXME - check on the UI that it is actually configured
        assertTrue(true);
    }
}
