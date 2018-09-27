package com.ca.apm.systemtest.fld.test.devel;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.systemtest.fld.test.FldHammondLoadTest;
import com.ca.apm.systemtest.fld.testbed.devel.HammondTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;


/**
 * @author jirji01
 */
public class HammondTest extends TasTestNgTest {
    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = HammondTestbed.class, executeOn = HammondTestbed.EM_TEST_MACHINE_ID), owner = "jirji01")
    public void test() throws Exception {

        ClwUtils clw = utilities.createClwUtils(HammondTestbed.EM_ROLE_ID);
        String collectorHostName = envProperties.getMachineHostnameByRoleId(HammondTestbed.EM_ROLE_ID);

        new FldHammondLoadTest() { {System.out.println("starting Hammond"); super.startLoad();} };

        Thread.sleep(30000);
        Assert.assertEquals("Expecting more than one active agent with running Hammond.", 9, clw.getAgents(collectorHostName, "Active"));
        
        new FldHammondLoadTest() { {System.out.println("stopping Hammond"); super.stopLoad();} };
        
        Thread.sleep(30000);
        Assert.assertEquals("No active agent when Hammond is shutdown.", 0, clw.getAgents(collectorHostName, "Active"));
    }
}
