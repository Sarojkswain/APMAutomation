package com.ca.apm.systemtest.fld.testbed.devel;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getTimeSynchronizationRunDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getTimeSynchronizationRunJar;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.thirdparty.TasTestsCoreVersion;
import com.ca.apm.systemtest.fld.role.TimeSynchronizationRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class SampleTimeSynchronizationFldTestbedProvider
    implements
        FldTestbedProvider,
        FLDLoadConstants,
        FLDConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(SampleTimeSynchronizationFldTestbedProvider.class);

    public static final String TIME_SYNCHRONIZATION_MACHINE_ID = "timeSynchronizationMachine";
    public static final String[] TIME_SYNCHRONIZATION_MACHINE_IDS =
        {TIME_SYNCHRONIZATION_MACHINE_ID};

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;
    public static final TasTestsCoreVersion TAS_TESTS_CORE_VERSION =
        TasTestsCoreVersion.AQUARIUS_99_99_SNAPSHOT;

    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        return Collections.emptySet();
    }
    
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        LOGGER.info("SampleTimeSynchronizationFldTestbedProvider.initTestbed():: entry");
        ITestbedMachine[] timeSynchronizationMachines =
            new TestbedMachine[TIME_SYNCHRONIZATION_MACHINE_IDS.length];
        for (int i = 0; i < TIME_SYNCHRONIZATION_MACHINE_IDS.length; i++) {
            String machineId = TIME_SYNCHRONIZATION_MACHINE_IDS[i];

            TestbedMachine machine =
                (new TestbedMachine.Builder(machineId)).platform(Platform.WINDOWS)
                    .templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64).build();

            JavaRole javaRole =
                (new JavaRole.Builder("javaRole_" + machineId, tasResolver))
                    .dir(getJavaDir(JAVA_VERSION)).version(JAVA_VERSION).build();
            machine.addRole(javaRole);

            // tas-tests-core jar-with-dependencies
            UniversalRole tasTestsCoreRole =
                (new UniversalRole.Builder("tasTestsCoreRole_" + machineId, tasResolver)).download(
                    TAS_TESTS_CORE_VERSION.getArtifact(),
                    getTimeSynchronizationRunDir(TAS_TESTS_CORE_VERSION),
                    TAS_TESTS_CORE_VERSION.getFilename()).build();
            machine.addRole(tasTestsCoreRole);

            TimeSynchronizationRole timeSynchronizationRole =
                (new TimeSynchronizationRole.Builder("timeSynchronizationRole_" + machineId,
                    tasResolver)).javaHome(javaRole.getInstallDir())
                    .runCp(getTimeSynchronizationRunJar(TAS_TESTS_CORE_VERSION))
                    // .waitInterval(60000L) // 1 min.
                    .build();
            timeSynchronizationRole.after(javaRole, tasTestsCoreRole);
            machine.addRole(timeSynchronizationRole);

            timeSynchronizationMachines[i] = machine;
        }
        testbed.addMachines(Arrays.asList(timeSynchronizationMachines));
        LOGGER.info("SampleTimeSynchronizationFldTestbedProvider.initTestbed():: exit");
    }


}
