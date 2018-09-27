package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedCleanerFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Arrays;

import static com.ca.apm.systemtest.fld.testbed.FLDConstants.DATABASE_MACHINE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDConstants.EM_DATABASE_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDConstants.EM_MOM_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDConstants.EM_WEBVIEW_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDConstants.MOM_MACHINE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDConstants.WEBVIEW_MACHINE_ID;
import static com.ca.apm.systemtest.fld.testbed.PerfMediumTestbed.EM_C_MACHINES;
import static com.ca.apm.systemtest.fld.testbed.PerfMediumTestbed.EM_C_ROLES;


@TestBedDefinition
public class PerfMediumTestbedCleaner implements TestBedCleanerFactory<PerfSmallTestbed> {

    @Override
    public ITestbed create(final ITasResolver tasResolver,
                           final PerfSmallTestbed javaTestBedFactory) {
        ITestbed deployedTestBed = javaTestBedFactory.create(tasResolver);
        Testbed testBed = new Testbed(PerfSmallTestbedCleaner.class.getSimpleName());

        testBed.addMachine(cleanApm(deployedTestBed, EM_MOM_ROLE_ID, MOM_MACHINE_ID));
        for (int i = 0; i < EM_C_ROLES.length; i++) {
            testBed.addMachine(cleanApm(deployedTestBed, EM_C_ROLES[i], EM_C_MACHINES[i]));
        }
        testBed.addMachine(cleanApm(deployedTestBed, EM_WEBVIEW_ROLE_ID, WEBVIEW_MACHINE_ID));
        testBed.addMachine(cleanApm(deployedTestBed, EM_DATABASE_ROLE_ID, DATABASE_MACHINE_ID));

        return testBed;
    }

    private ITestbedMachine cleanApm(ITestbed deloyedTestBed, String roleId, String machineId) {

        EmRole emRole = (EmRole) deloyedTestBed.getRoleById(roleId);

        ITestbedMachine emMachine = deloyedTestBed.getMachineById(machineId);
        emMachine.empty();

        ExecutionRole uninstallEmRole =
                new ExecutionRole.Builder("uninstall_" + roleId).flow(RunCommandFlow.class,
                        emRole.getEmUninstallCommandFlowContext()).build();

        FileModifierFlowContext deleteFlow =
                new FileModifierFlowContext.Builder().delete(TasBuilder.WIN_SOFTWARE_LOC).build();
        ExecutionRole deleteRole =
                new ExecutionRole.Builder("delete_" + roleId).flow(FileModifierFlow.class, deleteFlow)
                        .build();
        deleteRole.after(uninstallEmRole);

        ExecutionRole stopRole;
        stopRole =
                new ExecutionRole.Builder("stopWv_" + roleId).flow(RunCommandFlow.class,
                        emRole.getWvStopCommandFlowContext()).build();
        stopRole.before(uninstallEmRole);
        emMachine.addRole(stopRole);

        stopRole =
                new ExecutionRole.Builder("stopEm_" + roleId).flow(RunCommandFlow.class,
                        emRole.getEmStopCommandFlowContext()).build();
        stopRole.before(uninstallEmRole);

        emMachine.addRole(stopRole);

        ExecutionRole sleepRole = sleepCommand(roleId, uninstallEmRole, deleteRole);

        return emMachine.addRole(uninstallEmRole, deleteRole, sleepRole);
    }

    private ExecutionRole sleepCommand(String roleId, ExecutionRole beforeRole, ExecutionRole afterRole) {
        RunCommandFlowContext sleepFlowContext =
                new RunCommandFlowContext.Builder("ping").args(
                        Arrays.asList("127.0.0.1", "-n", "20", ">", "nul")).build();
        ExecutionRole sleepRole =
                new ExecutionRole.Builder("sleep_" + roleId).syncCommand(
                        sleepFlowContext).build();
        sleepRole.between(beforeRole, afterRole);
        return sleepRole;
    }
}
