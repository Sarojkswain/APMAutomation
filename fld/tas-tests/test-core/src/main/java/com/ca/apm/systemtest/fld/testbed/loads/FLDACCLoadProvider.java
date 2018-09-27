/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.ACCLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;

public class FLDACCLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    
    private ITestbedMachine accMachine;

    @Override
    public Collection<ITestbedMachine> initMachines() {
        accMachine = TestBedUtils.createWindowsMachine(ACC_MACHINE_ID, "w64");
        return Arrays.asList(accMachine);
    }

	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        
        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
	    
		// Pass MOM EM host. Port is default. Install Directory is optional
		ACCLoadRole accRole = new ACCLoadRole.Builder(ACC_ROLE_ID, tasResolver)
				.emHost(emHost)
				.build();

		accMachine.addRole(accRole);
	}

}
