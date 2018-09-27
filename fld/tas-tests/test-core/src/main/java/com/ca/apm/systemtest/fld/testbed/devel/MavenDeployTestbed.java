package com.ca.apm.systemtest.fld.testbed.devel;

import com.ca.apm.systemtest.fld.artifact.thirdparty.MavenDistributionArtifact;
import com.ca.apm.systemtest.fld.role.MavenRole;
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
public class MavenDeployTestbed implements ITestbedFactory {
    public static final String MAVEN_LINUX_ROLE_ID = "mavenLinuxRoleId";
    public static final String MAVEN_LINUX_MACHINE_ID = "mavenLinuxMachineId";
    public static final String MAVEN_WINDOWS_ROLE_ID = "mavenWindowsRoleId";
    public static final String MAVEN_WINDOWS_MACHINE_ID = "mavenWindowsMachineId";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Linux

        MavenRole mavenLinuxRole = new MavenRole.LinuxBuilder(MAVEN_LINUX_ROLE_ID, tasResolver)
            .version(MavenDistributionArtifact.v3_3_9_TarGz)
            .build();

        ITestbedMachine mavenLinuxMachine = TestBedUtils.createLinuxMachine(MAVEN_LINUX_MACHINE_ID,
            TEMPLATE_CO65, mavenLinuxRole);

        // Windows

        MavenRole mavenWindowsRole = new MavenRole.Builder(MAVEN_WINDOWS_ROLE_ID, tasResolver)
            .version(MavenDistributionArtifact.v3_3_9_Zip)
            .build();

        ITestbedMachine mavenWindowsMachine
            = TestBedUtils
            .createWindowsMachine(MAVEN_WINDOWS_MACHINE_ID, TEMPLATE_W64, mavenWindowsRole);

        // Testbed

        return new Testbed(getClass().getSimpleName())
            .addMachine(mavenLinuxMachine)
            .addMachine(mavenWindowsMachine);
    }
}
