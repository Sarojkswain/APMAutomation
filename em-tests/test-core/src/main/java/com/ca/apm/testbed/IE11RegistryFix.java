package com.ca.apm.testbed;

import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbedMachine;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive.CURRENT_USER;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;

/**
 * @author haiva01
 */
public final class IE11RegistryFix {
    /**
     * This adds a role to a machine that sets required registry keys on Windows so that Internet
     * Explorer 11 works with Selenium at all.
     *
     * @param machine machine to fix
     * @return role which executes flow to set the registry keys for IE11 to work with Selenium
     */
    public static IRole addRoleToFixIE11Registry(ITestbedMachine machine,
        ITasResolver tasResolver) {
        Win32RegistryFlowContext context = new Win32RegistryFlowContext.Builder()
            .setValue(CURRENT_USER, "Software\\Microsoft\\Internet Explorer\\Main\\TabProcGrowth",
                DWORD, 0)
            .build();

        UniversalRole role = new UniversalRole.Builder(machine.getMachineId() + "_IE11RegistryFix",
            tasResolver)
            .runFlow(Win32RegistryFlow.class, context)
            .build();
        machine.addRole(role);

        return role;
    }
}
