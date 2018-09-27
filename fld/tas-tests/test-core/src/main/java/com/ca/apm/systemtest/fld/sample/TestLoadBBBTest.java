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
public class TestLoadBBBTest extends TasTestNgTest {
    @Test(groups="fld-load-poc")
    public void proofOfConcept() {
        
        System.out.println("In the test, about to wait!");
        /*
         * here we would actually perform some necessary test steps, likely by running a command on a particular machine
         * such as:
         *   runSerializedCommandFlowFromRole("loadAAA", TestLoadAAARole.START_AAA_FLOW_KEY);
         * 
         */

        synchronized (this) {
            try {
                this.wait(30000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }

        System.out.println("Done waiting!!!");
    }

}
