package com.ca.apm.tests.testbed;

import java.util.Arrays;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedCleanerFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class ClusterRegressionTestBedCleaner
    implements
        TestBedCleanerFactory<ClusterRegressionTestBed> {

    @Override
    public ITestbed create(final ITasResolver tasResolver,
        final ClusterRegressionTestBed javaTestBedFactory) {
        ITestbed deployedTestBed = javaTestBedFactory.create(tasResolver);
        Testbed testBed = new Testbed(ClusterRegressionTestBedCleaner.class.getSimpleName());

        ITestbedMachine dbMachine =
            cleanApm(deployedTestBed, ClusterRegressionTestBed.EM_WV_DB_ROLE_ID,
                ClusterRegressionTestBed.DB_MACHINE_ID, null);
        testBed.addMachine(dbMachine);
        IRole uninstallDbRole =
            dbMachine.getRoleById("uninstall_" + ClusterRegressionTestBed.EM_WV_DB_ROLE_ID);

        for (int i = 0; i < ClusterRegressionTestBed.EM_C_ROLES.length; i++) {
            testBed.addMachine(cleanApm(deployedTestBed, ClusterRegressionTestBed.EM_C_ROLES[i],
                ClusterRegressionTestBed.C_MACHINES[i], uninstallDbRole));
        }
        testBed.addMachine(cleanApm(deployedTestBed, ClusterRegressionTestBed.EM_MOM_ROLE_ID,
            ClusterRegressionTestBed.MOM_MACHINE_ID, uninstallDbRole));

//        testBed.addMachine(cleanApm(deployedTestBed, ClusterRegressionTestBed.AGC_C_ROLE_ID,
//                ClusterRegressionTestBed.AGC_C_MACHINE_ID, null));
//
//        testBed.addMachine(cleanApm(deployedTestBed, ClusterRegressionTestBed.AGC_ROLE_ID,
//                ClusterRegressionTestBed.AGC_MACHINE_ID, null));

//        // clean load machine 1
//        WurlitzerRole wurlitzerRole =
//            (WurlitzerRole) deployedTestBed.getRoleById(ClusterRegressionTestBed.WURLITZER_ROLE_ID);
//        ExecutionRole stopWurlitzerRole =
//            new ExecutionRole.Builder("stopWurlitzer").flow(RunCommandFlow.class,
//                wurlitzerRole.getStopCommandFlowContext()).build();
//        ITestbedMachine load1Machine =
//            deployedTestBed.getMachineById(ClusterRegressionTestBed.LOAD1_MACHINE_ID);
//
//        HammondRole hammondAgcRole =
//                (HammondRole) deployedTestBed.getRoleById(ClusterRegressionTestBed.HAMMOND_ROLE_ID);
//
//        TomcatRole tomcatRole =
//                (TomcatRole) deployedTestBed.getRoleById(ClusterRegressionTestBed.MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID);
//
//        load1Machine.empty();
//
//        FileModifierFlowContext deleteMachine1Flow =
//                new FileModifierFlowContext.Builder().delete(TasBuilder.WIN_SOFTWARE_LOC).build();
//        ExecutionRole deleteMachine1Role =
//                new ExecutionRole.Builder("deleteMachine1").flow(FileModifierFlow.class,
//                        deleteMachine1Flow).build();
//        deleteMachine1Role.after(stopWurlitzerRole);
//        int idx = 0;
//        for (RunCommandFlowContext stopContext : hammondAgcRole.getStopCommandFlowContexts()) {
//            ExecutionRole stopHammondRole =
//                    new ExecutionRole.Builder("stopAgcHammond" + idx++).flow(RunCommandFlow.class,
//                            stopContext).build();
//            deleteMachine1Role.after(stopHammondRole);
//            load1Machine.addRole(stopHammondRole);
//        }
//        ExecutionRole stopTomcatRole =
//                new ExecutionRole.Builder("stopTomcat").flow(RunCommandFlow.class,
//                        tomcatRole.getStopCmdFlowContext()).build();
//        stopTomcatRole.before(deleteMachine1Role);
//        testBed.addMachine(load1Machine.addRole(stopWurlitzerRole, stopTomcatRole, deleteMachine1Role));
//
//        // clean load machine 2
//        CautlRole caultRole =
//            (CautlRole) deployedTestBed
//                .getRoleById(ClusterRegressionTestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID);
//        ExecutionRole stopFwRole =
//            new ExecutionRole.Builder("stopFakeWorkstation").flow(RunCommandFlow.class,
//                caultRole.getStopCommandFlowContext()).build();
//
//        FileModifierFlowContext deleteMachine2Flow =
//            new FileModifierFlowContext.Builder().delete(TasBuilder.WIN_SOFTWARE_LOC).build();
//        ExecutionRole deleteMachine2Role =
//            new ExecutionRole.Builder("deleteMachine2").flow(FileModifierFlow.class,
//                deleteMachine2Flow).build();
//        deleteMachine2Role.after(stopFwRole);
//
//        ExecutionRole sleepStopFakeWorkstation = sleepCommand("stopFakeWorkstation", stopFwRole, deleteMachine2Role);
//
//        ITestbedMachine load2Machine =
//            deployedTestBed.getMachineById(ClusterRegressionTestBed.LOAD2_MACHINE_ID);
//
//        load2Machine.empty();
//        testBed.addMachine(load2Machine.addRole(stopFwRole, sleepStopFakeWorkstation,deleteMachine2Role));
//
//        // clean load machine 3
//        HammondRole hammondRole =
//            (HammondRole) deployedTestBed.getRoleById(ClusterRegressionTestBed.HAMMOND_ROLE_ID);
//        ITestbedMachine load3Machine =
//            deployedTestBed.getMachineById(ClusterRegressionTestBed.LOAD3_MACHINE_ID);
//        FileModifierFlowContext deleteMachine3Flow =
//            new FileModifierFlowContext.Builder().delete(TasBuilder.WIN_SOFTWARE_LOC).build();
//        ExecutionRole deleteMachine3Role =
//            new ExecutionRole.Builder("deleteMachine3").flow(FileModifierFlow.class,
//                deleteMachine3Flow).build();
//
//        load3Machine.empty();
//        idx = 0;
//        for (RunCommandFlowContext stopContext : hammondRole.getStopCommandFlowContexts()) {
//            ExecutionRole stopHammondRole =
//                new ExecutionRole.Builder("stopHammond" + idx++).flow(RunCommandFlow.class,
//                    stopContext).build();
//            deleteMachine3Role.after(stopHammondRole);
//            load3Machine.addRole(stopHammondRole, deleteMachine3Role);
//        }
//        testBed.addMachine(load3Machine);

        return testBed;
    }

    private ITestbedMachine cleanApm(ITestbed deloyedTestBed, String roleId, String machineId,
        IRole uninstallDbRole) {
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

        ExecutionRole stopRole = null;
        if (roleId.equals(ClusterRegressionTestBed.EM_WV_DB_ROLE_ID) || roleId.equals(ClusterRegressionTestBed.AGC_ROLE_ID)) {
            stopRole =
                new ExecutionRole.Builder("stopWv_" + roleId).flow(RunCommandFlow.class,
                    emRole.getWvStopCommandFlowContext()).build();
            stopRole.before(uninstallEmRole);
            emMachine.addRole(stopRole);
        }
        if (!roleId.equals(ClusterRegressionTestBed.EM_WV_DB_ROLE_ID)) {
            stopRole =
                new ExecutionRole.Builder("stopEm_" + roleId).flow(RunCommandFlow.class,
                    emRole.getEmStopCommandFlowContext()).build();
            stopRole.before(uninstallEmRole);
            if (uninstallDbRole != null) {
                stopRole.before(uninstallDbRole);
            }
            emMachine.addRole(stopRole);
        }

        ExecutionRole sleepRole1 = sleepCommand(60, roleId, stopRole, uninstallEmRole);
        ExecutionRole sleepRole2 = sleepCommand(90, roleId, uninstallEmRole, deleteRole);
        
        return emMachine.addRole(uninstallEmRole, deleteRole, sleepRole1, sleepRole2);
    }

    private ExecutionRole sleepCommand(int sleep, String roleId, ExecutionRole beforeRole, ExecutionRole afterRole) {
        RunCommandFlowContext sleepFlowContext =
            new RunCommandFlowContext.Builder("ping").args(
                Arrays.asList("127.0.0.1", "-n", Integer.toString(sleep), ">", "nul")).build();
        ExecutionRole sleepRole =
            new ExecutionRole.Builder("sleep_" + sleep + "_" + roleId).syncCommand(
                sleepFlowContext).build();
        sleepRole.between(beforeRole, afterRole);
        return sleepRole;
    }
}
