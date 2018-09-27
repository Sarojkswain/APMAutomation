package com.ca.apm.systemtest.fld.testbed.devel;

import com.ca.apm.systemtest.fld.role.WebSphereLibertyDeployRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO65;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * @author haiva01
 */
@TestBedDefinition
public class WebSphereLibertyTestbed implements ITestbedFactory {
    public static final String WLP_LINUX_MACHINE_ID = "wlpLinuxMachineId";
    public static final String WLP_WINDOWS_ROLE_ID = "wlpWindowsRoleId";
    public static final String WLP_WINDOWS_MACHINE_ID = "wlpWindowsMachineId";
    public static final String WLP_LINUX_ROLE_ID = "wlpLinuxRoleId";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Linux

        WebSphereLibertyDeployRole emLinuxWlpRole
            = new WebSphereLibertyDeployRole.LinuxBuilder(WLP_LINUX_ROLE_ID, tasResolver)
            .build();

        ITestbedMachine emLinuxMomMachine = TestBedUtils.createLinuxMachine(WLP_LINUX_MACHINE_ID,
            TEMPLATE_CO65, emLinuxWlpRole);

        // Windows

        WebSphereLibertyDeployRole emWindowsWlpRole
            = new WebSphereLibertyDeployRole.Builder(WLP_WINDOWS_ROLE_ID, tasResolver)
            .build();

        ITestbedMachine emWindowsMomMachine
            = TestBedUtils
            .createWindowsMachine(WLP_WINDOWS_MACHINE_ID, TEMPLATE_W64, emWindowsWlpRole);

        // Testbed

        return new Testbed(getClass().getSimpleName())
            .addMachine(emLinuxMomMachine)
            .addMachine(emWindowsMomMachine);
    }
}
