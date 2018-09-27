/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * @author keyja01
 *
 */
@TestBedDefinition
public class SampleTestbed implements ITestbedFactory {

    /* (non-Javadoc)
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("FldLoadTestbed");
        
        EmRole emRole = new EmRole.Builder("emRole", tasResolver)
            .emClusterRole(EmRoleEnum.COLLECTOR)
            .version("99.99.dev-SNAPSHOT")
            .build();
        
        TestbedMachine machine = new TestbedMachine.Builder("emMachine")
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId("w64")
            .build();

        machine.addRole(emRole);
        testbed.addMachine(machine);
        
        // and here we initialize the addition testbed configuration required by the individual loads
        new FldTestLoadBBBProvider().initTestbed(testbed, tasResolver);
        
        return testbed;
    }

}
