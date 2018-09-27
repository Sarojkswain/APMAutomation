package com.ca.tas.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.mercurial.HgServerCertificateWindowsSilentAcceptorRole;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Functional test to test {@link HgServerCertificateWindowsSilentAcceptorRole}.
 *  
 * @author sinal04
 *
 */
@TestBedDefinition
public class HgServerCertificateWindowsSilentAcceptorTestBed implements ITestbedFactory {

	@Override
	public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("HgServerCertificateWindowsSilentAcceptorTestBed");
        ITestbedMachine machine = new TestbedMachine.Builder("machine")
                .templateId("w64c").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
        machine.addRole(new HgServerCertificateWindowsSilentAcceptorRole("HgServerCertificateWindowsSilentAcceptorRole"));
        testbed.addMachine(machine);
		return testbed;
	}

}
