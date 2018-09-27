package com.ca.apm.systemtest.fld.testbed;

import java.util.Collections;

import com.ca.apm.systemtest.fld.artifact.thirdparty.BrtmTestAppArtifact;
import com.ca.apm.systemtest.fld.role.WebAppWebSphereRole;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8JavaVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class WebsphereBrtTestAppTestbed implements ITestbedFactory {
    public static final String WAS_MACHINE_ID = "websphereBrtTestAppMachine";
    public static final String WAS_MACHINE_TEMPLATE_ID = ITestbedMachine.TEMPLATE_W64;

    public static final String MOM_MACHINE_TEMPLATE_ID = ITestbedMachine.TEMPLATE_RH66;

    public static final String WAS_INSTALL_PATH = "C:/sw/ibm/WebSphere85/AppServer";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("WebsphereBrtTestAppTestbed");

        // EM Machine
        ITestbedMachine momMachine =
            new TestbedMachine.Builder(EmPluginTestBed.MOM_MACHINE_ID).templateId(
                MOM_MACHINE_TEMPLATE_ID).build();

        EmptyRole emRole = new EmptyRole.Builder("momRole", tasResolver).build();

        momMachine.addRole(emRole);

        testbed.addMachine(momMachine);

        // WAS Machine
        ITestbedMachine websphereBrtTestAppMachine =
            new TestbedMachine.Builder(WAS_MACHINE_ID).templateId(WAS_MACHINE_TEMPLATE_ID).build();

        WebSphere8Role wasRole = new WebSphere8Role.Builder("was", tasResolver)
        // WAS BASE
            .wasVersion(WebSphere8Version.v85base)
            // WAS fix 8557 BASE
            .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
            // Java 7.1 for WAS BASE (x64 is for BASE)
            .wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64).wasDeployDir(WAS_INSTALL_PATH)
            // .addWebAppRole(brtmTestAppRole)
            .autoStart().build();
        websphereBrtTestAppMachine.addRole(wasRole);

        // BRTTestApp web application
        BrtmTestAppArtifact brtmArtifact = new BrtmTestAppArtifact(tasResolver);
        WebAppWebSphereRole brtmTestAppRole =
            new WebAppWebSphereRole.Builder("brtmTestAppRole", tasResolver)
                .artifact(brtmArtifact.createArtifact())
                // used for naming the WAR file (can be anything)
                .contextName("brtmTestApp")
                // name of the app in WAS Applications browse (can be anything)
                .webAppName("BRTTestApp")
                // context root, with or without leading forward slash
                .contextRoot("BRTTestApp").appServerRole(wasRole).build();
        brtmTestAppRole.after(wasRole);
        websphereBrtTestAppMachine.addRole(brtmTestAppRole);

        String momMachineHost = tasResolver.getHostnameById("momRole");

        AgentRole agentRole =
            new AgentRole.Builder("brtmAgent", tasResolver).webAppServer(wasRole)
                .overrideEM(momMachineHost, 5001)
                .additionalProperties(Collections.singletonMap("introscope.agent.hostName", "WebSphere8_BRTTestApp_" + tasResolver.getHostnameById("was")))
                .build();
        websphereBrtTestAppMachine.addRole(agentRole);

        testbed.addMachine(websphereBrtTestAppMachine);

        return testbed;
    }

}
