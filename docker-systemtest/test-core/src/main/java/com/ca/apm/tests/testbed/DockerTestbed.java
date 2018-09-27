package com.ca.apm.tests.testbed;

import com.ca.apm.tests.artifact.HammondDataVersion;
import com.ca.apm.tests.role.DockerComposeRole;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.HammondRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.concurrent.TimeUnit;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO7;

/**
 * @author jirji01
 */
@TestBedDefinition
public class DockerTestbed implements ITestbedFactory {

    public static final String EM_CLUSTER_ROLE_ID = "emClusterRole";

    private static final String EM_VERSION = "99.99.docker-SNAPSHOT";

    public static final String DOCKER_MACHINE_ID = "dockerMachineId";
    public static final String LOAD_MACHINE_ID = "hammondLoadMachineId";
    public static final String HAMMOND_ROLE_ID = "hammondLoadRole";

    public static int getRunDuration(TimeUnit unit) {
        return (int) unit.convert(30, TimeUnit.MINUTES);
    }

    @Override
    public ITestbed create(ITasResolver resolver) {
        
        Testbed testbed = new Testbed(getClass().getSimpleName());
        
        // collector machine
        DockerComposeRole emClusterRole = new DockerComposeRole.Builder(EM_CLUSTER_ROLE_ID, resolver)
                .version(EM_VERSION)
                .composeYmlTemplateName(DockerComposeRole.Template.cluster)
                .build();

        ITestbedMachine dockerMachine =
            TestBedUtils.createLinuxMachine(DOCKER_MACHINE_ID, TEMPLATE_CO7, emClusterRole);
        testbed.addMachine(dockerMachine);

        // hammond machine
        HammondRole hammondRole = new HammondRole.LinuxBuilder(HAMMOND_ROLE_ID, resolver)
                .installDir(BuilderBase.LINUX_SOFTWARE_LOC)
                .heapMemory("3g")
                .scale(1)
                .collector(resolver.getHostnameById(EM_CLUSTER_ROLE_ID))
                .data(HammondDataVersion.FLD_mainframe)
                .runDuration(getRunDuration(TimeUnit.SECONDS))
                .build();
        TestbedMachine loadMachine = new TestbedMachine.Builder(LOAD_MACHINE_ID)
                .templateId(TEMPLATE_CO7)
                .bitness(Bitness.b64)
                .build();
        loadMachine.addRole(hammondRole);
        testbed.addMachine(loadMachine);

        return testbed;
    }
}
