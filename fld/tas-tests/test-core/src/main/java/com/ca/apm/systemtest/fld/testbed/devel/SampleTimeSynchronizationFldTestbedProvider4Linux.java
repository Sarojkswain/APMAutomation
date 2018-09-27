package com.ca.apm.systemtest.fld.testbed.devel;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getLinuxTimeSynchronizationRunDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getLinuxTimeSynchronizationRunJar;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.role.TimeSynchronizationRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class SampleTimeSynchronizationFldTestbedProvider4Linux
    extends SampleTimeSynchronizationFldTestbedProvider {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(SampleTimeSynchronizationFldTestbedProvider4Linux.class);

    private static final String NTPDATE_PACKAGE_NAME = "ntpdate";

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        LOGGER.info("SampleTimeSynchronizationFldTestbedProvider4Linux.initTestbed():: entry");
        ITestbedMachine[] timeSynchronizationMachines =
            new TestbedMachine[TIME_SYNCHRONIZATION_MACHINE_IDS.length];
        for (int i = 0; i < TIME_SYNCHRONIZATION_MACHINE_IDS.length; i++) {
            String machineId = TIME_SYNCHRONIZATION_MACHINE_IDS[i];

            TestbedMachine machine =
                (new TestbedMachine.Builder(machineId)).platform(Platform.LINUX)
                    .templateId(ITestbedMachine.TEMPLATE_CO65).bitness(Bitness.b64).build();

            // tas-tests-core jar-with-dependencies
            UniversalRole tasTestsCoreRole =
                (new UniversalRole.Builder("tasTestsCoreRole_" + machineId, tasResolver)).download(
                    TAS_TESTS_CORE_VERSION.getArtifact(),
                    getLinuxTimeSynchronizationRunDir(TAS_TESTS_CORE_VERSION),
                    TAS_TESTS_CORE_VERSION.getFilename()).build();
            machine.addRole(tasTestsCoreRole);

            YumInstallPackageRole ntpdateYumInstallRole =
                new YumInstallPackageRole.Builder("ntpdateYumInstallRole_" + machineId).addPackage(
                    NTPDATE_PACKAGE_NAME).build();
            machine.addRole(ntpdateYumInstallRole);

            TimeSynchronizationRole timeSynchronizationRole =
                (new TimeSynchronizationRole.LinuxBuilder("timeSynchronizationRole_" + machineId,
                    tasResolver)).javaHome("/usr")
                    .runCp(getLinuxTimeSynchronizationRunJar(TAS_TESTS_CORE_VERSION))
                    // .waitInterval(60000L) // 1 min.
                    .build();
            timeSynchronizationRole.after(tasTestsCoreRole, ntpdateYumInstallRole);
            machine.addRole(timeSynchronizationRole);

            timeSynchronizationMachines[i] = machine;
        }
        testbed.addMachines(Arrays.asList(timeSynchronizationMachines));
        LOGGER.info("SampleTimeSynchronizationFldTestbedProvider4Linux.initTestbed():: exit");
    }

}
