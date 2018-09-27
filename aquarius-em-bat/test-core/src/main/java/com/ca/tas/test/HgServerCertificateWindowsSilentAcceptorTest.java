package com.ca.tas.test;


import com.ca.tas.testbed.HgServerCertificateWindowsSilentAcceptorTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

public class HgServerCertificateWindowsSilentAcceptorTest extends TasTestNgTest {
    @Tas(exclusivity = ExclusivityType.EXCLUSIVE, size = SizeType.SMALL, owner = "sinal04", suspended = "", 
        testBeds = { @TestBed(name = HgServerCertificateWindowsSilentAcceptorTestBed.class, executeOn = "machine") })
//    @TestBed(name = HgServerCertificateWindowsSilentAcceptorTestBed.class, executeOn = "machine")
//    @Test(groups = {"hgServCertAcceptor"})
    public void testRoleDeployment() throws Exception {
    	//?
    }

}
