package com.ca.apm.systemtest.fld.testbed.smokebeta.docker;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.built.docker.ApmDockerImageBase;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.role.docker.DockerRole.LinuxBuilder;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import java.util.Arrays;
import java.util.Collection;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;

/**
 * Installs three WebSphere instances. One
 *
 * @author banra06
 * @author keyja01
 * @author shadm01 - Docker implementation
 *
 */
public class FLDLoadWebSphereDockerProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {

    private ITestbedMachine was01Machine;
    private ITestbedMachine was02Machine;
    private ITestbedMachine was03Machine;

    @Override
    public Collection<ITestbedMachine> initMachines() {
        was01Machine = createLinuxMachine(WEBSPHERE_01_MACHINE_ID);
        was02Machine = createLinuxMachine(WEBSPHERE_02_MACHINE_ID);
        was03Machine = createLinuxMachine(WEBSPHERE_03_MACHINE_ID);

        return Arrays.asList(was01Machine, was02Machine, was03Machine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        String em2Host = tasResolver.getHostnameById(EM_MOM2_ROLE_ID);
        String agcHost = tasResolver.getHostnameById(AGC_ROLE_ID);
        String was3Host = tasResolver.getHostnameById(WEBSPHERE_03_ROLE_ID);

        IRole was1Role = WasDockerBuilder(WEBSPHERE_01_ROLE_ID, "initwas01", emHost).build();
        IRole was2Role = WasDockerBuilder(WEBSPHERE_02_ROLE_ID, "initwas02", em2Host).env("OTHER_HOST", was3Host).build();
        IRole was3Role = WasDockerBuilder(WEBSPHERE_03_ROLE_ID, "initwas03", agcHost).build();
        
        IRole stressApp1Role = StressAppDockerBuilder("StressAppRole_01", "stressapp", emHost).build();
        IRole stressApp2Role = StressAppDockerBuilder("StressAppRole_02", "stressapp", em2Host).build();
        IRole stressApp3Role = StressAppDockerBuilder("StressAppRole_03", "stressapp", agcHost).build();
        
        IRole crossClusterRole = crossClusterDockerBuilder("wasCrossCluster_02", "crossclusterload", em2Host, was3Host).build();
        
        IRole jmeterRole = JmeterAppDockerBuilder("JmeterAGCRole_03", "was_atst", agcHost, 8081).build();

        was01Machine.addRole(was1Role);
        stressApp1Role.after(was1Role);
        was01Machine.addRole(stressApp1Role);
        was02Machine.addRole(was2Role);
        stressApp2Role.after(was2Role);
        was02Machine.addRole(stressApp2Role);
        crossClusterRole.after(was2Role, was3Role);
        was02Machine.addRole(crossClusterRole);
        was03Machine.addRole(was3Role);
        stressApp3Role.after(was3Role);
        was03Machine.addRole(stressApp3Role);
        jmeterRole.after(stressApp3Role);
        was03Machine.addRole(jmeterRole);
    }

    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId)
                .platform(Platform.LINUX)
                .templateId(ITestbedMachine.TEMPLATE_RH7)
                .build();
    }

    private DockerRole.LinuxBuilder WasDockerBuilder(String roleName, String imageName, String emHost) {
        return new DockerRole.LinuxBuilder(roleName)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/fld", imageName)
                .version(imageName)
                .port(9080, 9080)
                .env("EM_HOST", emHost)
                //TODO - simplify image to use only one param and default port
                .env("EM_HOST_PORT", emHost + ":" + String.valueOf(EM_PORT));
    }
    
    private LinuxBuilder StressAppDockerBuilder(String roleName, String imageName, String emHost) {
        return new DockerRole.LinuxBuilder(roleName)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/fld", imageName)
                .version(imageName)
                .env("EM_HOST_PORT", emHost + ":" + String.valueOf(EM_PORT))
                .env("CUSTOM_PROCESS_NAME", "ErrorStallProcess")
                .env("AGENT_NAME", "ErrorStallAgent");
    }
    
    private LinuxBuilder JmeterAppDockerBuilder(String roleName, String imageName, String host, Integer port) {
        return new DockerRole.LinuxBuilder(roleName)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/jmeter", imageName)
                .version(imageName)
                .env("HOST", host)
                .env("PORT", port.toString())
                .env("DURATION", "1209600");
    }
    
    private LinuxBuilder crossClusterDockerBuilder(String roleName, String imageName, String targetHost,
                                                   String otherHost) {
        return new DockerRole.LinuxBuilder(roleName)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/fld", imageName)
                .version(imageName)
                .env("TARGET_HOST", targetHost)
                .env("OTHER_HOST", otherHost);
   }
}