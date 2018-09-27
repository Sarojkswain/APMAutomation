package com.ca.apm.tests.loads;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.role.DotNetAgentDeployRole;
import com.ca.apm.tests.role.DotNetAppsDeployRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotNetAgentHttpConnLoadProvider {

    public static final String DOTNET_HTTP_CONN_MACHINE1 = "dotNetHttpConnMachine1";
    public static final String DOTNET_HTTP_CONN_MACHINE2 = "dotNetHttpConnMachine2";
    public static final String[] DOTNET_HTTP_CONN_MACHINES = {
            DOTNET_HTTP_CONN_MACHINE1,
            DOTNET_HTTP_CONN_MACHINE2
    };
    public static final String DOTNET_AGENT_ROLE_ID = "dotNetAgentRole";
    public static final String DOTNET_APPS_ROLE_ID = "dotNetAppsRole";
    public static final String IIS_ENABLE_ROLE_ID = "iisEnableRole";
    public static final String IIS_UPDATE_PORT_ROLE_ID = "iisUpdatePortRole";
    public static final String IIS_REGISTER_ROLE_ID = "iisRegisterRole";
    public static final String ODP_NET_SCRIPTS_ROLE_ID = "odpNetScriptsRole";
    public static final String COPY_TNS_FILE_ROLE_ID = "copyTnsFileRole";


    private static final Logger LOGGER = LoggerFactory
        .getLogger(DotNetAgentHttpConnLoadProvider.class);

    private static final String WORKDIR = "c:\\sw\\dotnet-agent-workdir";

    private static final String KEYSTORE = WORKDIR + "\\agentCertificate.pfx";
    private static final String KEYSTORE_ESCAPED;
    private static final String KEYSTORE_PASSWORD = "password";

    private static final String TRUSTSTORE = WORKDIR + "\\agentTruststore";
    private static final String TRUSTSTORE_ESCAPED;
    private static final String TRUSTSTORE_PASSWORD = "password";

    private static final String EM_CERTIFICATE = WORKDIR + "\\EMcert.pem";

    private final String[] machineIds;
    private final List<ITestbedMachine> machines;

    private final Agent2EmConnectionType agent2EmConnectionType;
    private final boolean useAgentTruststore;

    static {
        KEYSTORE_ESCAPED =
            KEYSTORE.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"));
        TRUSTSTORE_ESCAPED =
            TRUSTSTORE.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"));
    }

    private String emVersion;

    public DotNetAgentHttpConnLoadProvider(String emVersion) {
        this.machineIds = DOTNET_HTTP_CONN_MACHINES;
        this.machines = new ArrayList<>(machineIds.length);
        agent2EmConnectionType = Agent2EmConnectionType.HTTP;
        useAgentTruststore = false;
        this.emVersion = emVersion;
    }

    public Collection<ITestbedMachine> initMachines() {
        machines.clear();
        for (int i = 0; i < machineIds.length; i++) {
            ITestbedMachine machine =
                new TestbedMachine.Builder(machineIds[i]).templateId("jass")
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
            machines.add(machine);
        }
        return Collections.unmodifiableCollection(machines);
    }

    public void initTestbed(String momId, ITasResolver tasResolver) {
        String emHost = tasResolver.getHostnameById(momId);
        LOGGER.info("DotNetAgentHttpConnLoadProvider.initTestbed():: emHost = {}", emHost);
        for (int i = 0; i < machines.size(); i++) {
            initMachine(tasResolver, machines.get(i), machines.get(i).getHostname(), emHost);
        }
    }

    private void initMachine(ITasResolver tasResolver, ITestbedMachine machine,
                             String agentHostName, String emHost) {
        String machineId = machine.getMachineId();
        addEnableWebServerRole(machine);
        addPerfmonRebuildRole(machine);
        addTnsRole(tasResolver, machine);

        // .NET test apps
        DotNetAppsDeployRole dotNetAppsRole =
            new DotNetAppsDeployRole.Builder(roleId(machineId, DOTNET_APPS_ROLE_ID),
                tasResolver).installDir(TasBuilder.WIN_SOFTWARE_LOC + "testapps")
                .shouldDisableHttpLogging(true).build();

        // .NET agent
        DotNetAgentDeployRole.Builder dotNetAgentRoleBuilder =
            new DotNetAgentDeployRole.Builder(roleId(machineId, DOTNET_AGENT_ROLE_ID),
                tasResolver).installDir(TasBuilder.WIN_SOFTWARE_LOC + "dotnet").isLegacyMode(false)
                .emHost(emHost).agentHostName(agentHostName).agentVersion(emVersion);

        if (agent2EmConnectionType != null) {
            List<String> commentOutLines =
                new ArrayList<>(Arrays.asList(
                    "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=",
                    "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=",
                    "introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT="));
            if (agent2EmConnectionType.secure) {
                commentOutLines.addAll(Arrays.asList(
                    "introscope.agent.enterprisemanager.transport.tcp.keystore.DEFAULT=",
                    "introscope.agent.enterprisemanager.transport.tcp.keypassword.DEFAULT="));
            }
            dotNetAgentRoleBuilder =
                dotNetAgentRoleBuilder.agentProfileCommentOutLines(commentOutLines);

            List<String> appendLines =
                new ArrayList<>(Arrays.asList("",
                    "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=" + emHost,
                    "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT="
                        + agent2EmConnectionType.port,
                    "introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT="
                        + agent2EmConnectionType.socketfactory));
            if (agent2EmConnectionType.secure) {
                appendLines.addAll(Arrays.asList(
                    "introscope.agent.enterprisemanager.transport.tcp.keystore.DEFAULT="
                        + KEYSTORE_ESCAPED,
                    "introscope.agent.enterprisemanager.transport.tcp.keypassword.DEFAULT="
                        + KEYSTORE_PASSWORD));

                if (useAgentTruststore) {
                    appendLines.addAll(Arrays.asList(
                        "introscope.agent.enterprisemanager.transport.tcp.truststore.DEFAULT="
                            + TRUSTSTORE_ESCAPED,
                        "introscope.agent.enterprisemanager.transport.tcp.trustpassword.DEFAULT="
                            + TRUSTSTORE_PASSWORD));
                }
            }
            dotNetAgentRoleBuilder = dotNetAgentRoleBuilder.agentProfileAppendLines(appendLines);
        }

        DotNetAgentDeployRole dotNetAgentRole = dotNetAgentRoleBuilder.build();

        if (agent2EmConnectionType != null && agent2EmConnectionType.secure) {
            // copy agent certificate
            FileCreatorFlowContext copyCertificateContext =
                new FileCreatorFlowContext.Builder()
                    .fromResource("/dotnet-agent/agentCertificate/agentCertificate.pfx")
                    .destinationPath(KEYSTORE).build();
            IRole copyCertificateRole =
                (new UniversalRole.Builder(roleId(machineId, "copyCertificateRole"), tasResolver))
                    .runFlow(FileCreatorFlow.class, copyCertificateContext).build();
            copyCertificateRole.before(dotNetAgentRole);
            machine.addRole(copyCertificateRole);

            if (useAgentTruststore) {
                // copy agent trust store (contains EM certificate)
                FileCreatorFlowContext copyAgentTruststoreContext =
                    new FileCreatorFlowContext.Builder()
                        .fromResource("/dotnet-agent/agentTruststore/agentTruststore")
                        .destinationPath(TRUSTSTORE).build();
                IRole copyAgentTruststoreRole =
                    (new UniversalRole.Builder(roleId(machineId, "copyAgentTruststoreRole"),
                        tasResolver)).runFlow(FileCreatorFlow.class, copyAgentTruststoreContext)
                        .build();
                copyAgentTruststoreRole.before(dotNetAgentRole);
                machine.addRole(copyAgentTruststoreRole);
            }

            // copy EM certificate
            FileCreatorFlowContext copyEmCertificateContext =
                new FileCreatorFlowContext.Builder()
                    .fromResource("/dotnet-agent/emCertificate/EMcert.pem")
                    .destinationPath(EM_CERTIFICATE).build();
            IRole copyEmCertificateRole =
                (new UniversalRole.Builder(roleId(machineId, "copyEmCertificateRole"), tasResolver))
                    .runFlow(FileCreatorFlow.class, copyEmCertificateContext).build();
            copyEmCertificateRole.before(dotNetAgentRole);
            machine.addRole(copyEmCertificateRole);

            // add EM self-signed certificate to Windows trusted cerificates
            RunCommandFlowContext certutilContext =
                (new RunCommandFlowContext.Builder("certutil"))
                    .args(Arrays.asList("-addstore", "TrustedPeople", EM_CERTIFICATE))
                    .doNotPrependWorkingDirectory().build();
            IRole certutilRole =
                (new UniversalRole.Builder(roleId(machineId, "certutilRole"), tasResolver))
                    .runFlow(RunCommandFlow.class, certutilContext).build();
            certutilRole.after(copyEmCertificateRole);
            copyEmCertificateRole.before(dotNetAgentRole);
            machine.addRole(certutilRole);
        }

        IRole registerIISRole =
            machine.getRoleById(roleId(machineId, IIS_REGISTER_ROLE_ID));
        IRole updatePortRole =
            machine.getRoleById(roleId(machineId, IIS_UPDATE_PORT_ROLE_ID));
        dotNetAppsRole.before(dotNetAgentRole);
        updatePortRole.before(dotNetAppsRole);
        registerIISRole.before(dotNetAppsRole, dotNetAgentRole);
        machine.addRole(dotNetAppsRole, dotNetAgentRole);
    }

    private void addEnableWebServerRole(ITestbedMachine machine) {
        String machineId = machine.getMachineId();
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
            new RunCommandFlowContext.Builder("C:\\Windows\\System32\\Dism.exe").args(args).build();
        ExecutionRole enableIISRole =
            new ExecutionRole.Builder(machineId + "_" + IIS_ENABLE_ROLE_ID).flow(
                RunCommandFlow.class, enableIISCommand).build();

        // update port for default site
        RunCommandFlowContext updatePortCommand =
            new RunCommandFlowContext.Builder("C:\\Windows\\system32\\inetsrv\\appcmd").args(
                Arrays.asList("set", "site", "\"Default Web Site\"", "/bindings:\"http/*:85:\""))
                .build();
        ExecutionRole updatePortRole =
            new ExecutionRole.Builder(roleId(machineId, IIS_UPDATE_PORT_ROLE_ID))
                .flow(RunCommandFlow.class, updatePortCommand).build();

        // register iis for .NET 4
        RunCommandFlowContext registerIISCommand =
            new RunCommandFlowContext.Builder(
                "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\aspnet_regiis").args(
                Arrays.asList("-i")).build();
        ExecutionRole registerIISRole =
            new ExecutionRole.Builder(roleId(machineId, IIS_REGISTER_ROLE_ID))
                .flow(RunCommandFlow.class, registerIISCommand).build();

        enableIISRole.before(updatePortRole, registerIISRole);
        machine.addRole(enableIISRole);
        machine.addRole(registerIISRole);
        machine.addRole(updatePortRole);
    }

    private void addTnsRole(ITasResolver tasResolver, ITestbedMachine machine) {
        String machineId = machine.getMachineId();
        String installBaseDir = TasBuilder.WIN_SOFTWARE_LOC + "oracle\\";

        // get dist package
        DefaultArtifact artifact =
            new DefaultArtifact("com.ca.apm.tests", "agent-tests-core", "dist_dotnet", "zip",
                tasResolver.getDefaultVersion());
        GenericRole distRole =
            new GenericRole.Builder(machineId + "_" + ODP_NET_SCRIPTS_ROLE_ID,
                tasResolver).unpack(artifact, installBaseDir).build();

        // copy tns file
        String source = installBaseDir + "tnsnames.sc.oracle.ora";
        String dest = "C:\\SW\\oracle\\product\\12.1.0\\client_1\\Network\\Admin\\tnsnames.ora";

        ExecutionRole tnsRole =
            new ExecutionRole.Builder(roleId(machineId, COPY_TNS_FILE_ROLE_ID))
                .flow(FileModifierFlow.class,
                    new FileModifierFlowContext.Builder().copy(source, dest).build()).build();

        distRole.before(tnsRole);
        machine.addRole(distRole);
        machine.addRole(tnsRole);
    }

    private void addPerfmonRebuildRole(ITestbedMachine machine) {
        String machineId = machine.getMachineId();
        // applicable to windows vms - sometimes perfmon counters are
        // missing when vm gets cloned; have to rebuid them
        RunCommandFlowContext context =
            new RunCommandFlowContext.Builder("lodctr").workDir("C:\\Windows\\system32")
                .args(Arrays.asList("/r")).build();

        ExecutionRole role =
            new ExecutionRole.Builder(machineId + "_perfmonRebuildRole").flow(RunCommandFlow.class,
                context).build();

        machine.addRole(role);
    }

    private static String roleId(String machineId, String roleId) {
        return (new StringBuilder(machineId)).append('_').append(roleId).toString();
    }

    public static enum Agent2EmConnectionType {
        SOCKET(5001, "com.wily.isengard.postofficehub.link.net.DefaultSocketFactory", false), SSL(
            5443, "com.wily.isengard.postofficehub.link.net.SSLSocketFactory", true), HTTP(8081,
            "com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory", false), HTTPS(
            8444, "com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory", true);

        private final int port;
        private final String socketfactory;
        private final boolean secure;

        private Agent2EmConnectionType(int port, String socketfactory, boolean secure) {
            this.port = port;
            this.socketfactory = socketfactory;
            this.secure = secure;
        }

        public static Agent2EmConnectionType fromString(String s) {
            for (Agent2EmConnectionType value : Agent2EmConnectionType.values()) {
                if (value.name().equalsIgnoreCase(s)) {
                    return value;
                }
            }
            return null;
        }
    }

}
