/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.EntityAlertLoadRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;

public class FLDTestEntityLoadProvider implements FldTestbedProvider {

	public static final String ENTITY_MACHINE_ID = "entityMachine";
	public static final String ENTITY_ROLE_ID = "entityRole";
	private ITestbedMachine entityMachine;
	
	@Override
	public Collection<ITestbedMachine> initMachines() {
	    entityMachine = TestBedUtils.createWindowsMachine(ENTITY_MACHINE_ID, "w64");
	    return Arrays.asList(entityMachine);
	}
	
	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

		// Pass MOM EM host and port. Install Directory is optional
		EntityAlertLoadRole entityRole = new EntityAlertLoadRole.Builder(
				ENTITY_ROLE_ID, tasResolver).emHost("fldmom01").emPort(5001)
				.build();
		entityMachine.addRole(entityRole);
	}

}
