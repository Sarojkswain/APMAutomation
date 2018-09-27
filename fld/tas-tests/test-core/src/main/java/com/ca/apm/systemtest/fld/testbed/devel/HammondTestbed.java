package com.ca.apm.systemtest.fld.testbed.devel;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.fld.testbed.loads.FldHammondProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author jirji01
 */
@TestBedDefinition
public class HammondTestbed implements ITestbedFactory {

    public static final String EM_ROLE_ID = "emWindowsRoleId";
    public static final String EM_TEST_MACHINE_ID = "emTestMachineId";

    private static final String EM_VERSION = "10.2.0-SNAPSHOT";

    @Override
    public ITestbed create(ITasResolver resolver) {
        
        Testbed testbed = new Testbed(getClass().getSimpleName());
        
        // collector machine
        EmRole emWindowsMomRole =
            new EmRole.Builder(EM_ROLE_ID, resolver).version(EM_VERSION)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).noTimeout().nostartWV()
                .build();
        ITestbedMachine emWindowsMomMachine =
            TestBedUtils.createWindowsMachine(EM_TEST_MACHINE_ID, TEMPLATE_W64, emWindowsMomRole);
        testbed.addMachine(emWindowsMomMachine);

        // hammond machine
        FldHammondProvider provider = new FldHammondProvider().updateCollRoleId(EM_ROLE_ID);
        
        testbed.addMachines(provider.initMachines());
        provider.initTestbed(testbed, resolver);

        return testbed;
    }
}
