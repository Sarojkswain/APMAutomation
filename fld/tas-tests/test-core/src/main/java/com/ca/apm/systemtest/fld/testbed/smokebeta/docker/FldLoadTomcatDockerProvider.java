/**
 *
 */
package com.ca.apm.systemtest.fld.testbed.smokebeta.docker;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.built.docker.ApmDockerImageBase;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import java.util.Arrays;
import java.util.Collection;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;

/**
 * @author keyja01 - init version
 * @author shadm01 - convertion to Docker-based role
 */
public class FldLoadTomcatDockerProvider implements FldTestbedProvider, FLDConstants, FLDLoadConstants {
    private ITestbedMachine[] tomcats;

    //TODO - pull latest artifcat from artifactory instead of using hardcoded
    private final String TOMCAT_AGENT_ARTIFCATORY_URL = "http://truss.ca.com/builds/InternalBuilds/10.5.1-ISCP/build-990102(10.5.1.9)/introscope10.5.1.9/IntroscopeAgentFiles-NoInstaller10.5.1.9tomcat.unix.tar";
    private String emHostPort; //TODO - not sure if it will be fetched properly here


    @Override
    public Collection<ITestbedMachine> initMachines() {
        tomcats = new TestbedMachine[TOMCAT_MACHINE_IDS.length];

        for (int i = 0; i < TOMCAT_MACHINE_IDS.length; i++) {
            tomcats[i] = createLinuxMachine(TOMCAT_MACHINE_IDS[i]);
        }

        return Arrays.asList(tomcats);
    }

    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId)
                .platform(Platform.LINUX)
                .templateId(ITestbedMachine.TEMPLATE_CO7)
                .build();
    }


    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        emHostPort = tasResolver.getHostnameById(EM_MOM_ROLE_ID) + ":" + EM_PORT;

        IRole tomcat6Role = DockerRoleBuilder(TOMCAT_6_ROLE_ID, "aquarius/tomcat6", "axistesstestqa", 8080, TOMCAT6_AGENT);
        IRole tomcat7Role = DockerRoleBuilder(TOMCAT_7_ROLE_ID, "aquarius/tomcat7", "axisqahello", 9070, TOMCAT7_AGENT);
        IRole tomcat9080Role = DockerRoleBuilder(TOMCAT_9080_ROLE_ID, "aquarius/tomcat6", "axis2wurlitzer", 9080, TOMCAT_AGENT_9080);
        IRole tomcat9081Role = DockerRoleBuilder(TOMCAT_9081_ROLE_ID, "aquarius/tomcat6", "axis2wurlitzer", 9081, TOMCAT_AGENT_9081);

        tomcat7Role.before(tomcat6Role);
        tomcat9081Role.before(tomcat9080Role);

        //TODO - all tomcats can reside on one machine, they take 400mb space, and memory usage is:
        tomcats[0].addRole(tomcat6Role);    // 256 mb ram
        tomcats[0].addRole(tomcat7Role);    // 256-512 mb ram
        tomcats[0].addRole(tomcat9080Role); // 1 GB ram
        tomcats[0].addRole(tomcat9081Role); // 1 GB ram

        testbed.addMachines(Arrays.asList(tomcats));
    }

    private IRole DockerRoleBuilder(String roleName, String image, String tag, int port, String agentName) {
        //NOTE: JVM params are taken into account from Dockerfile and can be changed via JAVA_OPTS env param
        return new DockerRole.LinuxBuilder(roleName)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image(image)
                .version(tag)
                .port(port, 8080)
                .port(7001, 7001)
                .env("AGENT_URL", TOMCAT_AGENT_ARTIFCATORY_URL)
                .env("AGENT_NAME", agentName)
                .env("HOSTNAME", TOMCAT_HOST_NAME) //TODO - same host name for multiple Tomcats, why ?
                .env("EM_HOST_PORT", emHostPort)
                .build();
    }


}
