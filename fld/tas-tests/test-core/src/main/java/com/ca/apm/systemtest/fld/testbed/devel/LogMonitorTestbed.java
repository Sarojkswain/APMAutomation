package com.ca.apm.systemtest.fld.testbed.devel;

import java.util.Collections;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.fld.role.LogMonitorRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
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
public class LogMonitorTestbed implements ITestbedFactory {

    public static final String LOG_MONITOR_LINUX_ROLE_ID = "logMonitorLinuxRoleId";
    public static final String EM_LINUX_MOM_MACHINE_ID = "emLinuxMomMachineId";
    public static final String EM_WINDOWS_ROLE_ID = "emWindowsRoleId";
    public static final String LOG_MONITOR_WINDOWS_ROLE_ID = "logMonitorWindowsRoleId";
    public static final String EM_WINDOWS_MOM_MACHINE_ID = "emWindowsMomMachineId";
    public static final String LOG_MONITOR_CONFIG_JSON
        = "/com/ca/apm/systemtest/fld/testbed/devel/log-monitor-config.json";
    private static final String EM_VERSION = "10.2.0-SNAPSHOT";
    public static final String EM_LINUX_ROLE_ID = "emLinuxRoleId";
    public static final String PID_FILE_KEY = "pidFile";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Linux

        EmRole emLinuxMomRole = new EmRole.LinuxBuilder(EM_LINUX_ROLE_ID, tasResolver)
            .version(EM_VERSION)
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            .noTimeout()
            //.nostartEM()
            .nostartWV()
            .build();

        LogMonitorRole logMonitorLinuxRole = new LogMonitorRole.LinuxBuilder(
            LOG_MONITOR_LINUX_ROLE_ID, tasResolver)
            .configFileFromResource(LOG_MONITOR_CONFIG_JSON)
            .email("haiva01@ca.com")
            //.email("filja01@ca.com")
            .vars(Collections.singletonMap("currentLogDir",
                emLinuxMomRole.getDeployEmFlowContext().getInstallDir() + "/logs"))
            .build();
        logMonitorLinuxRole.addProperty(PID_FILE_KEY,
            logMonitorLinuxRole.getDeployLogMonitorFlowContext().getPidFile());
        logMonitorLinuxRole.after(emLinuxMomRole);

        ITestbedMachine emLinuxMomMachine = TestBedUtils.createLinuxMachine(EM_LINUX_MOM_MACHINE_ID,
            TEMPLATE_CO65, emLinuxMomRole, logMonitorLinuxRole);

        // Windows

        EmRole emWindowsMomRole = new EmRole.Builder(EM_WINDOWS_ROLE_ID, tasResolver)
            .version(EM_VERSION)
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            .noTimeout()
            //.nostartEM()
            .nostartWV()
            .build();

        LogMonitorRole logMonitorWindowsRole = new LogMonitorRole.Builder(
            LOG_MONITOR_WINDOWS_ROLE_ID, tasResolver)
            .configFileFromResource(LOG_MONITOR_CONFIG_JSON)
            .email("haiva01@ca.com")
            //.email("filja01@ca.com")
            .vars(Collections.singletonMap("currentLogDir",
                emWindowsMomRole.getDeployEmFlowContext().getInstallDir() + "/logs"))
            .build();
        logMonitorWindowsRole.addProperty(PID_FILE_KEY,
            logMonitorWindowsRole.getDeployLogMonitorFlowContext().getPidFile());
        logMonitorWindowsRole.after(emWindowsMomRole);

        ITestbedMachine emWindowsMomMachine
            = TestBedUtils
            .createWindowsMachine(EM_WINDOWS_MOM_MACHINE_ID, TEMPLATE_W64, emWindowsMomRole,
                logMonitorWindowsRole);

        // Testbed

        return new Testbed(getClass().getSimpleName())
            .addMachine(emLinuxMomMachine)
            .addMachine(emWindowsMomMachine);
    }
}
