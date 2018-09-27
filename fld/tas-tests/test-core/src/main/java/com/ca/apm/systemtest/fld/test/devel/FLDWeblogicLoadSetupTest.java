package com.ca.apm.systemtest.fld.test.devel;

import java.io.IOException;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.FLDWeblogicLoadSetupTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Unit test which deploys {@link FLDWeblogicLoadSetupTestbed} and checks if both Weblogic server instances are found listening 
 * on their ports. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class FLDWeblogicLoadSetupTest extends TasTestNgTest {

    @Tas(testBeds = @TestBed(name = FLDWeblogicLoadSetupTestbed.class, executeOn = FLDWeblogicLoadSetupTestbed.MACHINE_ID), size = SizeType.BIG, owner = "sinal04")
    @Test(groups = { "fldWlsLoadSetupTest" })
    public void testCreate2WlsInstancesWithWurlitzersAndIntroscopeAgens() throws IOException {
        String wlsHostName = envProperties.getMachineHostnameByRoleId(FLDWeblogicLoadSetupTestbed.WLS_ROLE_ID);
        
        System.out.println("Waiting for Weblogic instance 1 at: " + wlsHostName + ":" + FLDWeblogicLoadSetupTestbed.WLS_INSTANCE1_PORT);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(wlsHostName, FLDWeblogicLoadSetupTestbed.WLS_INSTANCE1_PORT, 300);

        System.out.println("Waiting for Weblogic instance 2 at: " + wlsHostName + ":" + FLDWeblogicLoadSetupTestbed.WLS_INSTANCE2_PORT);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(wlsHostName, FLDWeblogicLoadSetupTestbed.WLS_INSTANCE2_PORT, 300);
        Assert.assertTrue(true);
    }

}
