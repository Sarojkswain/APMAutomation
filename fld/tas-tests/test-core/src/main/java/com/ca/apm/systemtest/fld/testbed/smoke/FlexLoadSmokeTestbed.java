/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smoke;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.loads.FLDFlexLoadProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author keyja01
 *
 */
@TestBedDefinition
public class FlexLoadSmokeTestbed implements ITestbedFactory {

    private static final String DUMMY_ROLE_ID = "dummyRoleId";

    /* (non-Javadoc)
             * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
             */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("FlexLoadSmokeTestbed");
        
        ITestbedMachine timMachine =
            new TestbedMachine.LinuxBuilder(FLDConstants.TIM05_MACHINE_ID)
                              .templateId(FLDConstants.FLD_TIM_TMPL_ID).bitness(Bitness.b64)
                              .build();
        timMachine.addRole(new EmptyRole.LinuxBuilder("dummyRoleId", tasResolver).build());
        testbed.addMachine(timMachine);
        
        FLDFlexLoadProvider flexLoadProvider = new FLDFlexLoadProvider(
            testbed.getMachineById(FLDConstants.TIM05_MACHINE_ID),
            tasResolver.getHostnameById(DUMMY_ROLE_ID));
        testbed.addMachines(flexLoadProvider.initMachines());
        flexLoadProvider.initTestbed(testbed, tasResolver);
        
        return testbed;
    }

}
