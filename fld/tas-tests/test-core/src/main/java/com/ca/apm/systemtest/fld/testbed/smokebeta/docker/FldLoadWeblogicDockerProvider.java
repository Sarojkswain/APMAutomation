/**
 *
 */
package com.ca.apm.systemtest.fld.testbed.smokebeta.docker;

import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.WeblogicWurlitzer1DomainTemplateVersion;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.loads.WebLogicDomainRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.built.docker.ApmDockerImageBase;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.docker.DockerRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;
import com.google.common.net.HostAndPort;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;

/**
 * @author keyja01
 */
public class FldLoadWeblogicDockerProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {
    //TODO - replace with script to pull latest Artifactory image
    String ARTIFACTORY_AGENT_TOMCAT_URL = "http://truss.ca.com/builds/InternalBuilds/99.99.sys-ISCP/build-000123(99.99.0.sys)/introscope99.99.0.sys/IntroscopeAgentFiles-NoInstaller99.99.0.sysweblogic.unix.tar";

    private ITestbedMachine wls01;
    private ITestbedMachine wls02;

    @Override
    public Collection<ITestbedMachine> initMachines() {
        wls01 = createLinuxMachine(WLS_01_MACHINE_ID);
        wls02 = createLinuxMachine(WLS_02_MACHINE_ID);

        return Arrays.asList(wls01, wls02);
    }

    private ITestbedMachine createLinuxMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId)
                .platform(Platform.LINUX)
                .templateId(ITestbedMachine.TEMPLATE_CO7)
                .build();
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String emHost=  tasResolver.getHostnameById(EM_MOM_ROLE_ID);

        IRole wlsv102_7001 = createWeblogicContainer(WLS_01_SERVER_01_ROLE_ID, "wurlitzer1v102agent", emHost, WEBLOGIC_WURLITZER_1_AGENT, 7001);
        IRole wlsv102_7002 = createWeblogicContainer(WLS_01_SERVER_02_ROLE_ID, "wurlitzer1v102agent", emHost, WEBLOGIC_WURLITZER_2_AGENT, 7002);
        IRole jmeterWls102_7001 = createJmeterContainer(WLS_01_MACHINE_ID + "-SOALoadRole_JM_7001", "soa_load_soapwurlitzer_7001", tasResolver.getHostnameById(WLS_01_SERVER_01_ROLE_ID), 7001);
        IRole jmeterWls102_7002 = createJmeterContainer(WLS_01_MACHINE_ID + "-SOALoadRole_JM_7002", "soa_load_soapwurlitzer_7002", tasResolver.getHostnameById(WLS_01_SERVER_02_ROLE_ID), 7002);

        wls01.addRole(wlsv102_7001, wlsv102_7002, jmeterWls102_7001, jmeterWls102_7002);

        IRole wlsv104_7001 = createWeblogicContainer(WLS_02_SERVER_01_ROLE_ID, "wurlitzer1v104agent", emHost, WEBLOGIC_WURLITZER_1_AGENT, 7001);
        IRole wlsv104_7002 = createWeblogicContainer(WLS_02_SERVER_02_ROLE_ID, "wurlitzer1v104agent", emHost, WEBLOGIC_WURLITZER_2_AGENT, 7002);
        IRole jmeterWls104_7001 = createJmeterContainer(WLS_02_MACHINE_ID + "-SOALoadRole_JM_7001", "soa_load_soapwurlitzer_7001", tasResolver.getHostnameById(WLS_02_SERVER_01_ROLE_ID), 7001);
        IRole jmeterWls104_7002 = createJmeterContainer(WLS_02_MACHINE_ID + "-SOALoadRole_JM_7002", "soa_load_soapwurlitzer_7002", tasResolver.getHostnameById(WLS_02_SERVER_02_ROLE_ID), 7002);
        wls02.addRole(wlsv104_7001, wlsv104_7002, jmeterWls104_7001, jmeterWls104_7002);
    }

    private DockerRole createWeblogicContainer(String roleId, String tag, String emHost, String agentName, int port) {
        return new DockerRole.LinuxBuilder(roleId)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/weblogic")
                .version(tag)
                .port(5001, 5001)
                .port(port, port)
                .env("EM_HOST_PORT", emHost + ":" + EM_PORT)
                .env("AGENT_URL", ARTIFACTORY_AGENT_TOMCAT_URL)
                .env("AGENT_NAME", agentName)  //TODO - same agent name for all
                .env("DIRECTIVE_FILES", "weblogic-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd,browseragent.pbd")
                .build();
    }

    private DockerRole createJmeterContainer(String roleName, String tag, String hostname, int port) {
        return new DockerRole.LinuxBuilder(roleName)
                .registry(ApmDockerImageBase.REGISTRY_EMEA)
                .image("aquarius/jmeter")
                .version(tag)
                .env("HOST", hostname)
                .env("PORT", "" + port)
                .env("DURATION", "1209600")
                .build();
    }
}
