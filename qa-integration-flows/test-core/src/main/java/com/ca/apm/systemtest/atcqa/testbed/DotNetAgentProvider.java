package com.ca.apm.systemtest.atcqa.testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.role.DotNetAgentDeployRole;
import com.ca.apm.systemtest.fld.role.DotNetAppsDeployRole;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class DotNetAgentProvider implements FldTestbedProvider, Constants {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(DotNetAgentProvider.class);

    private static final String DOTNET_APPS_ROLE_ID = "dotNetAppsRole";
    private static final String DOTNET_AGENT_ROLE_ID = "dotNetAgentRole";
    private static final String IIS_REGISTER_ROLE_ID = "iisRegisterRole";
    private static final String IIS_UPDATE_PORT_ROLE_ID = "iisUpdatePortRole";
    private static final String IIS_ENABLE_ROLE_ID = "iisEnableRole";
    private static final String ODP_NET_SCRIPTS_ROLE_ID = "odpNetScriptsRole";
    private static final String COPY_TNS_FILE_ROLE_ID = "copyTnsFileRole";
    private static final String PERFMON_REBUILD_ROLE_ID = "perfmonRebuildRole";

    private static final String MACHINE_TEMPLATE_ID_JASS = "jass";

    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    private String emHost;
    private String agentVersion = fldConfig.getEmVersion();

    private ITestbedMachine dotnetMachine;

    @Override
    public Collection<ITestbedMachine> initMachines() {
        dotnetMachine =
            (new TestbedMachine.Builder(DOTNET01_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_JASS)
                .platform(Platform.WINDOWS).bitness(Bitness.b64).build();

        Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(dotnetMachine);
        return machines;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        ExecutionRole enableIISRole = getEnableIISRole(IIS_ENABLE_ROLE_ID);
        dotnetMachine.addRole(enableIISRole);

        ExecutionRole updatePortRole = getUpdatePortRole(IIS_UPDATE_PORT_ROLE_ID);
        updatePortRole.after(enableIISRole);
        dotnetMachine.addRole(updatePortRole);

        ExecutionRole registerIISRole = getRegisterIISRole(IIS_REGISTER_ROLE_ID);
        registerIISRole.after(enableIISRole);
        dotnetMachine.addRole(registerIISRole);

        ExecutionRole perfmonRebuildRole = getPerfmonRebuildRole(PERFMON_REBUILD_ROLE_ID);
        dotnetMachine.addRole(perfmonRebuildRole);

        List<AbstractRole> tnsRoles = getTnsRoles(tasResolver);
        dotnetMachine.addRoles(tnsRoles);

        // .NET test apps
        DotNetAppsDeployRole dotNetAppsRole =
            getDotNetAppsDeployRole(DOTNET_APPS_ROLE_ID, tasResolver);
        dotNetAppsRole.after(updatePortRole, registerIISRole);
        dotnetMachine.addRole(dotNetAppsRole);

        // .NET agent
        DotNetAgentDeployRole dotNetAgentRole =
            getDotNetAgentDeployRole(DOTNET_AGENT_ROLE_ID, tasResolver);
        dotNetAgentRole.after(registerIISRole, dotNetAppsRole);
        dotnetMachine.addRole(dotNetAgentRole);
    }

    private ExecutionRole getEnableIISRole(String roleId) {
        // enable IIS components
        ArrayList<String> args = new ArrayList<String>();
        args.add("/online");
        args.add("/enable-feature");
        args.add("/featurename:IIS-WebServerRole");
        args.add("/featurename:IIS-WebServer");
        args.add("/featurename:IIS-CommonHttpFeatures");
        args.add("/featurename:IIS-StaticContent");
        args.add("/featurename:MSMQ-Server");
        args.add("/featurename:IIS-CGI");
        args.add("/featurename:IIS-ISAPIExtensions");
        args.add("/featurename:IIS-ISAPIFilter");
        RunCommandFlowContext enableIISCommand =
            (new RunCommandFlowContext.Builder("C:\\Windows\\System32\\Dism.exe")).args(args)
                .build();
        ExecutionRole enableIISRole =
            (new ExecutionRole.Builder(roleId)).flow(RunCommandFlow.class, enableIISCommand)
                .build();
        return enableIISRole;
    }

    private ExecutionRole getUpdatePortRole(String roleId) {
        // update port for default site
        RunCommandFlowContext updatePortCommand =
            (new RunCommandFlowContext.Builder("C:\\Windows\\system32\\inetsrv\\appcmd")).args(
                Arrays.asList("set", "site", "\"Default Web Site\"", "/bindings:\"http/*:85:\""))
                .build();
        ExecutionRole updatePortRole =
            (new ExecutionRole.Builder(roleId)).flow(RunCommandFlow.class, updatePortCommand)
                .build();
        return updatePortRole;
    }

    private ExecutionRole getRegisterIISRole(String roleId) {
        // register iis for .NET 4
        RunCommandFlowContext registerIISCommand =
            (new RunCommandFlowContext.Builder(
                "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\aspnet_regiis")).args(
                Arrays.asList("-i")).build();
        ExecutionRole registerIISRole =
            (new ExecutionRole.Builder(roleId)).flow(RunCommandFlow.class, registerIISCommand)
                .build();
        return registerIISRole;
    }

    private ExecutionRole getPerfmonRebuildRole(String roleId) {
        // applicable to windows vms - sometimes perfmon counters are
        // missing when vm gets cloned; have to rebuid them
        RunCommandFlowContext context =
            (new RunCommandFlowContext.Builder("lodctr")).workDir("C:\\Windows\\system32")
                .args(Arrays.asList("/r")).build();
        ExecutionRole perfmonRebuildRole =
            (new ExecutionRole.Builder(roleId)).flow(RunCommandFlow.class, context).build();
        return perfmonRebuildRole;
    }

    private List<AbstractRole> getTnsRoles(ITasResolver tasResolver) {
        String installBaseDir = TasBuilder.WIN_SOFTWARE_LOC + "oracle\\";
        // get dist package
        DefaultArtifact artifact =
            new DefaultArtifact("com.ca.apm.tests", "agent-tests-core", "dist_dotnet", "zip",
                tasResolver.getDefaultVersion());
        GenericRole distRole =
            (new GenericRole.Builder(ODP_NET_SCRIPTS_ROLE_ID, tasResolver)).unpack(artifact,
                installBaseDir).build();
        // copy tns file
        String source = installBaseDir + "tnsnames.sc.oracle.ora";
        String dest = "C:\\SW\\oracle\\product\\12.1.0\\client_1\\Network\\Admin\\tnsnames.ora";
        ExecutionRole tnsRole =
            (new ExecutionRole.Builder(COPY_TNS_FILE_ROLE_ID)).flow(FileModifierFlow.class,
                new FileModifierFlowContext.Builder().copy(source, dest).build()).build();
        distRole.before(tnsRole);
        return Arrays.asList(distRole, tnsRole);
    }

    private DotNetAppsDeployRole getDotNetAppsDeployRole(String roleId, ITasResolver tasResolver) {
        // .NET test apps
        DotNetAppsDeployRole dotNetAppsRole =
            (new DotNetAppsDeployRole.Builder(roleId, tasResolver))
                .installDir(TasBuilder.WIN_SOFTWARE_LOC + "testapps")
                .shouldDisableHttpLogging(true).build();
        return dotNetAppsRole;
    }

    private DotNetAgentDeployRole getDotNetAgentDeployRole(String roleId, ITasResolver tasResolver) {
        // .NET agent
        DotNetAgentDeployRole dotNetAgentRole =
            (new DotNetAgentDeployRole.Builder(roleId, tasResolver))
                .installDir(TasBuilder.WIN_SOFTWARE_LOC + "dotnet").isLegacyMode(false)
                .emHost(emHost).agentVersion(agentVersion).build();
        return dotNetAgentRole;
    }

    public void setEmHost(String emHost) {
        this.emHost = emHost;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

}
