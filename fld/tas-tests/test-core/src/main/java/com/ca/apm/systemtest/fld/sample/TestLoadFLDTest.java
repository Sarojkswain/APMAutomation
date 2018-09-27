/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.CleanFLDMainClusterTestBed;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;

/**
 * Simple test for FLDMainTestbed cleaner
 * 
 * @author filja01
 *
 */
public class TestLoadFLDTest extends TasTestNgTest implements FLDConstants {
    @Tas(owner="filja01", testBeds = {@TestBed(name=CleanFLDMainClusterTestBed.class, executeOn=MOM_MACHINE_ID)})
    @Test(groups="fld-cleaner-test")
    public void testRun() throws Exception {
        shortWait(30000L);
    }
    
    private synchronized void shortWait(long ms) throws InterruptedException {
        wait(ms);
    }

}
