/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import org.testng.annotations.Test;

import com.ca.tas.test.TasTestNgTest;

/**
 * @author keyja01
 *
 */
public class TestLoadAAATest extends TasTestNgTest {
    @Test(groups="fld-load-poc")
    public void proofOfConcept() {
        runSerializedCommandFlowFromRole("loadAAA", TestLoadAAARole.START_AAA_FLOW_KEY);
        
        System.out.println("In the test, about to wait!");
        synchronized (this) {
            try {
                this.wait(300000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }

        System.out.println("Done waiting!!!");
        runSerializedCommandFlowFromRole("loadAAA", TestLoadAAARole.STOP_AAA_FLOW_KEY);
    }
}
