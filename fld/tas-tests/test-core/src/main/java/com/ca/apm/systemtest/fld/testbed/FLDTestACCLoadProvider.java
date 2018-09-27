/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.ACCLoadRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;

public class FLDTestACCLoadProvider implements FldTestbedProvider {

	public static final String ACC_MACHINE_ID = "accMachine";
	public static final String ACC_ROLE_ID = "accRole";
	private ITestbedMachine accMachine;
	
	@Override
	public Collection<ITestbedMachine> initMachines() {
        accMachine = TestBedUtils.createWindowsMachine(ACC_MACHINE_ID, "w64");
        
	    return Arrays.asList(accMachine);
	}

	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

		// Pass MOM EM host and port. Install Directory is optional
		ACCLoadRole accRole = new ACCLoadRole.Builder(ACC_ROLE_ID, tasResolver)
				.emHost("fldmom01").emPort(5001).build();
		accMachine.addRole(accRole);
	}

}
