/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.EntityAlertLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;

public class FLDEntityAlertLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    private ITestbedMachine entityAlertMachine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        entityAlertMachine = TestBedUtils.createWindowsMachine(ENTITY_ALERT_MACHINE_ID, "w64");
        
        return Arrays.asList(entityAlertMachine);
    }

	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

	    
	    String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
		// Pass MOM EM host. Install Directory is optional
	    EntityAlertLoadRole entityAlertRole = new EntityAlertLoadRole.Builder(
				ENTITY_ALERT_ROLE_ID, tasResolver)
		        .emHost(emHost)
				.build();

	    entityAlertMachine.addRole(entityAlertRole);
	}

}
