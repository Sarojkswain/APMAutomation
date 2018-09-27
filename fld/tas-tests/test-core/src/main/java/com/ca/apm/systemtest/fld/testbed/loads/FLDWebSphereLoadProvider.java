/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.thirdparty.BrtmTestAppArtifact;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.WASAgentDeployRole;
import com.ca.apm.systemtest.fld.role.WASWebappDeployRole;
import com.ca.apm.systemtest.fld.role.WebAppWebSphereRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8JavaVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.IWebSphereRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Installs three WebSphere instances. One
 * 
 * @author banra06
 * @author keyja01
 *
 */
public class FLDWebSphereLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    private static final String AGENT_NAME = "WebSphere";

    private static final Logger log = LoggerFactory.getLogger(FLDWebSphereLoadProvider.class);
    
    private ITestbedMachine was01Machine; 
    private ITestbedMachine was02Machine; 
    private ITestbedMachine was03Machine;
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        was01Machine = new TestbedMachine.Builder(WEBSPHERE_01_MACHINE_ID).platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64).build();
        was02Machine = new TestbedMachine.Builder(WEBSPHERE_02_MACHINE_ID).platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64).build();
        was03Machine = new TestbedMachine.Builder(WEBSPHERE_03_MACHINE_ID).platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64).build();
        
        return Arrays.asList(was01Machine, was02Machine, was03Machine);
    }
    

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        log.debug("Initializing FLDWebSphereLoadProvider with 3 websphere instances");
        initWas01(testbed, tasResolver);
        initWas02(testbed, tasResolver);
        initWas03(testbed, tasResolver);
    }


    /**
     * Connects the first WebSphere to the MOM in the main cluster
     * 
     * @param testbed
     * @param tasResolver
     */
    private void initWas01(ITestbed testbed, ITasResolver tasResolver) {

        WebSphere8Role was85Role =
            new WebSphere8Role.Builder(WEBSPHERE_01_ROLE_ID, tasResolver)
                .wasVersion(WebSphere8Version.v85base)
                .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
                .wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64).autoStart()
                // .addWebAppRole(brtmTestAppRole)
                .build();

        WebAppRole<IWebSphereRole> brtmTestAppRole =
            new WebAppWebSphereRole.Builder("brtmTestAppRole", tasResolver)
                .contextName("brtmTestApp").webAppName("BRTTestApp").contextRoot("BRTTestApp")
                // .cargoDeploy()
                .artifact(new BrtmTestAppArtifact(tasResolver).createArtifact())
                .appServerRole(was85Role).build();
        brtmTestAppRole.after(was85Role);

        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        Map<String,String> additionalProps = new HashMap<>();
        additionalProps.put("introscope.agent.hostName", WAS_HOST_NAME);
        additionalProps.put("introscope.agent.agentName", WAS85_AGENT);
        AgentRole agentRole =
            new AgentRole.Builder(WEBSPHERE_01_ROLE_ID + "-Agent", tasResolver)
                .webAppServer(was85Role).overrideEM(emHost, 5001)
                .version(fldConfig.getEmVersion())
                .additionalProperties(additionalProps)
                .build();
        
        ClientDeployRole client1 = new ClientDeployRole.Builder(LOAD1_ROLE_ID,
            tasResolver).emHost(emHost).shouldDeployJassApps(true).jvmVersion("7").build();

        was01Machine.addRole(brtmTestAppRole, was85Role, agentRole, client1);
    }


    /**
     * Connects the second WebSphere to the MOM in the second cluster
     * 
     * @param testbed
     * @param tasResolver
     */
    private void initWas02(ITestbed testbed, ITasResolver tasResolver) {
        String emHost = tasResolver.getHostnameById(EM_MOM2_ROLE_ID);
        ClientDeployRole clientRole =
            new ClientDeployRole.Builder(WAS_XCLUSTER_CLIENT_ROLE_ID + "_"+ LOAD2_ROLE_ID, tasResolver).jvmVersion("7")
                .shouldDeployConsoleApps(true).shouldDeployJassApps(true).emHost(emHost)
                .fldXjvmhost(tasResolver.getHostnameById(WEBSPHERE_03_ROLE_ID)).build();
        was02Machine.addRole(clientRole);

        addWebsphereRoles(WEBSPHERE_02_ROLE_ID, was02Machine, tasResolver, emHost, "WASCrossCluster1");
    }



    /**
     * Connects a WebSphere to the second cluster
     * 
     * @param testbed
     * @param tasResolver
     */
    private void initWas03(ITestbed testbed, ITasResolver tasResolver) {
        String emHost = tasResolver.getHostnameById(AGC_ROLE_ID);
        ClientDeployRole client3 = new ClientDeployRole.Builder(LOAD3_ROLE_ID,
            tasResolver).emHost(emHost).shouldDeployJassApps(true).jvmVersion("7").build();
        was03Machine.addRole(client3);
        
        addWebsphereRoles(WEBSPHERE_03_ROLE_ID, was03Machine, tasResolver, emHost, "WASCrossCluster2");
    }


    private void addWebsphereRoles(String role, ITestbedMachine machine, ITasResolver tasResolver,
        String emHost, String customProcessName) {

        // install was
        WebSphere8Role was85Role =
            new WebSphere8Role.Builder(role, tasResolver).wasVersion(WebSphere8Version.v85base)
                .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
                .wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64).build();

        // install was webapps
        WASWebappDeployRole wasWebappDeployAppRole =
            new WASWebappDeployRole.Builder(
                machine.getMachineId() + "_" + "wasWebappDeployAppRole", tasResolver)
                .appserverDir(codifyPath(was85Role.getInstallDir())).profileName("AppSrv01")
                .serverName("server1").nodeName(tasResolver.getHostnameById(role) + "Node01")
                .minHeapSize("512").maxHeapSize("1024").permSpaceSize("256")
                .maxPermSpaceSize("1024").shouldDeployJassApps(false).build();

        // install was agent
        WASAgentDeployRole wasAgentDeployAppRole =
            new WASAgentDeployRole.Builder(machine.getMachineId() + "_" + "wasAgentDeployAppRole",
                tasResolver)
                .appserverDir(codifyPath(was85Role.getInstallDir()))
                .isLegacyMode(false)
                .serverName("server1")
                .customProcessName(AGENT_NAME)
                .agentName(customProcessName)
                .agentVersion(fldConfig.getEmVersion())
                .installScriptPath(
                    codifyPath(was85Role.getInstallDir() + "/TestApps/ws_pipeorgan3"))
                .emHost(emHost).build();
        

        // ensure that the websphere instances start after the agent is deployed
        Collection<IRole> startRoles = was85Role.getStartRoles();
        if (startRoles != null) {
            for (IRole r : startRoles) {
                r.after(wasAgentDeployAppRole);
            }
            machine.addRoles(startRoles);
        }

        // add roles
        wasWebappDeployAppRole.after(was85Role);
        wasAgentDeployAppRole.after(wasWebappDeployAppRole);
        machine.addRole(was85Role, wasWebappDeployAppRole, wasAgentDeployAppRole);
    }

    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }
}
