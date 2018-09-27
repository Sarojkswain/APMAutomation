package com.ca.apm.systemtest.fld.testbed.smoke;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.ca.apm.systemtest.fld.role.DeadlockDetectionRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class EmDeadlockDetectionTestTestbed
    implements
        ITestbedFactory,
        FLDLoadConstants,
        FLDConstants {

    public static final String MACHINE_ID_windows = "emDeadlockDetectionMachine_windows";
    public static final String MACHINE_ID_linux = "emDeadlockDetectionMachine_linux";

    public static final String EM_DEADLOCK_DETECTION_ROLE_ID_windows =
        "emDeadlockDetectionRole_windows";
    public static final String EM_DEADLOCK_DETECTION_ROLE_ID_linux =
        "emDeadlockDetectionRole_linux";

    private ITestbedMachine windowsMachine;
    private ITestbedMachine linuxMachine;

    private boolean deployEm = false;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed(getClass().getSimpleName());
        FldTestbedProvider provider = new EmDeadlockDetectionTestbedProvider();
        provider.initMachines();
        provider.initTestbed(testbed, tasResolver);
        return testbed;
    }

    private class EmDeadlockDetectionTestbedProvider
        implements
            FldTestbedProvider,
            FLDLoadConstants,
            FLDConstants {

        private FLDConfiguration fldConfiguration = FLDConfigurationService.getConfig();

        @Override
        public Collection<ITestbedMachine> initMachines() {
            windowsMachine =
                (new TestbedMachine.Builder(MACHINE_ID_windows)).templateId("w64")
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
            linuxMachine =
                (new TestbedMachine.Builder(MACHINE_ID_linux)).templateId("co65")
                    .platform(Platform.CENTOS).bitness(Bitness.b64).build();
            return Collections.unmodifiableCollection(Arrays.asList(windowsMachine, linuxMachine));
        }

        @Override
        public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
            // windows machine
            IRole emRoleWindows;
            if (deployEm) {
                emRoleWindows = (new EmRole.Builder("emRole_windows", tasResolver)).build();
            } else {
                emRoleWindows = (new EmptyRole.Builder("emRole_windows", tasResolver)).build();
            }
            windowsMachine.addRole(emRoleWindows);

            DeadlockDetectionRole deadlockDetectionRoleWindows =
                (new DeadlockDetectionRole.Builder(EM_DEADLOCK_DETECTION_ROLE_ID_windows))
                    .smtpHost(fldConfiguration.getFldConfigSmtpHost())
                    .addEmailAddress("bocto01@ca.com")
                    // .addEmailAddress(fldConfiguration.getReportEmail())
                    .build();
            deadlockDetectionRoleWindows.after(emRoleWindows);
            windowsMachine.addRole(deadlockDetectionRoleWindows);

            testbed.addMachine(windowsMachine);
            // //


            // linux machine
            IRole emRoleLinux;
            if (deployEm) {
                emRoleLinux = (new EmRole.LinuxBuilder("emRole_linux", tasResolver)).build();
            } else {
                emRoleLinux = (new EmptyRole.LinuxBuilder("emRole_linux", tasResolver)).build();
            }
            linuxMachine.addRole(emRoleLinux);

            DeadlockDetectionRole deadlockDetectionRoleLinux =
                (new DeadlockDetectionRole.Builder(EM_DEADLOCK_DETECTION_ROLE_ID_linux))
                    .smtpHost(fldConfiguration.getFldConfigSmtpHost())
                    .addEmailAddress("bocto01@ca.com")
                    // .addEmailAddress(fldConfiguration.getReportEmail())
                    .build();
            deadlockDetectionRoleLinux.after(emRoleLinux);
            linuxMachine.addRole(deadlockDetectionRoleLinux);

            testbed.addMachine(linuxMachine);
            // //
        }
    }

}
