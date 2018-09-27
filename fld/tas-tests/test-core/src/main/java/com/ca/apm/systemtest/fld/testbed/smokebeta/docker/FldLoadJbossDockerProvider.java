/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smokebeta.docker;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.systemtest.fld.artifact.WurlitzerWebAppArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.Axis2WebappVersion;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.built.docker.ApmDockerImageBase;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import java.util.*;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJBossDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

/**
 * @author keyja01
 * @author shadm01
 *
 */
public class FldLoadJbossDockerProvider implements FLDLoadConstants, FLDConstants, FldTestbedProvider {
    private ITestbedMachine machine;

    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        machine = createLinuxMachine(JBOSS_MACHINE);
        return Collections.singletonList(machine);
    }

    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId)
                .platform(Platform.LINUX)
                .templateId(ITestbedMachine.TEMPLATE_CO7)
                .build();
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

        addJboss6(tasResolver);
        addJboss7(tasResolver);
    }

    private void addJboss7(ITasResolver tasResolver) {
        IRole role = new DockerRole.LinuxBuilder(JBOSS7_ROLE_ID)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/jboss7")
                .version("axis2")
                .port(8080, 8080)
                .env("EM_HOST_PORT", tasResolver.getHostnameById(EM_MOM_ROLE_ID) + ":" + EM_PORT)
                .build();

        machine.addRole(role);
    }

    private void addJboss6(ITasResolver tasResolver) {
        IRole role = new DockerRole.LinuxBuilder(JBOSS6_ROLE_ID)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/jboss6")
                .version("wurlitzer")
                .port(8180, 8180)
                .env("EM_HOST_PORT", tasResolver.getHostnameById(EM_MOM_ROLE_ID) + ":" + EM_PORT)
                .build();

        machine.addRole(role);
    }

}
