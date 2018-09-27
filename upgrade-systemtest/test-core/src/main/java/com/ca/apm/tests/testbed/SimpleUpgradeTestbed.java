package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.testapp.NowhereBankVersion;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.apm.tests.flow.AGCRegisterFlow;
import com.ca.apm.tests.role.EmUpgradeRole;
import com.ca.tas.artifact.built.WurlitzerArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author jirji01
 */
@TestBedDefinition
public class SimpleUpgradeTestbed implements ITestbedFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUpgradeTestbed.class);
    public static final String UPGRADE_VERSION_1 = "10.5.1.6";
    public static final String UPGRADE_1_ROLE_ID = "upgrade1051Role";
    public static final String UPGRADE_VERSION_2 = "10.5.2.15";
    public static final String UPGRADE_2_ROLE_ID = "upgrade1052Role";

    public Platform platform() {
        return Platform.WINDOWS;
    };

    public String version() {
        return "10.3.0.16";
    };

    public String template() {
        return "w64_16gb";
    };

    public EmRole.Builder emBuilder(String roleId, ITasResolver resolver) {
        switch (platform()) {
            case LINUX:
                return new EmRole.LinuxBuilder(roleId, resolver);
            case WINDOWS:
                return new EmRole.Builder(roleId, resolver);
            default:
                throw new IllegalStateException("unsupported platform");
        }
    }

    public ITestbedMachine machine(String id) {
        TestbedMachine.Builder builder;
        switch (platform()) {
            case LINUX:
                builder = new TestbedMachine.LinuxBuilder(id);
                break;
            case WINDOWS:
                builder = new TestbedMachine.Builder(id);
                break;
            default:
                throw new IllegalStateException("unsupported platform");
        }

        ITestbedMachine machine = builder
                .platform(platform())
                .templateId(template())
                .bitness(Bitness.b64)
                .build();

        if (platform() == Platform.WINDOWS) {
            addTimeSyncRole(machine);
        }

        return machine;
    }

    public static final List<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-XX:MaxPermSize=256m",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms4g", "-Xmx4g",
            "-Dappmap.user=admin", "-Dappmap.token=" + AGCRegisterFlow.ADMIN_AUX_TOKEN);

    public static final List<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1g", "-Xmx1g");

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String LOAD_MACHINE_ID = "loadMachine";

    public static final String EM_ROLE_ID = "emRole";
    public static final String EPAGENT_ROLE_ID = "epAgentRole";
    public static final String EPAGENT_TESTAPP_ROLE_ID = "epAgentTestAppRole";
    public static final String ORACLE_ROLE_ID = "emOracleRole";

    @Override
    public ITestbed create(ITasResolver resolver) {

        Testbed testbed = new Testbed(getClass().getSimpleName());

        ITestbedMachine emMachine = machine(EM_MACHINE_ID);
        testbed.addMachine(emMachine);

        ITestbedMachine loadMachine =
                new TestbedMachine.Builder(LOAD_MACHINE_ID).platform(Platform.WINDOWS)
                        .templateId("w64_16gb").bitness(Bitness.b64).build();
        testbed.addMachine(loadMachine);

        EmRole.Builder momBuilder = emBuilder(EM_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database", "WebView"))
                .nostartEM()
                .nostartWV()
                .version(version())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .databaseDir("d:\\database")
                .configProperty("transport.buffer.input.maxNum", "2400")
                .configProperty("transport.outgoingMessageQueueSize", "6000")
                .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "10")
                .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "10")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", "5000")
                .emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .installerProperty("emDataStoreDir", "d:\\data");
        EmRole emRole = momBuilder.build();
        emMachine.addRole(emRole);

        RoleUtility.addMmRole(emMachine, emRole.getRoleId() + "_mm", emRole, "NowhereBankMM");

        IRole startMomRole = RoleUtility.addStartEmRole(emMachine, emRole, true, emRole);
        IRole startNbRole = addNowhereBankRole(emMachine, emRole, null, null, resolver);

        startNbRole.after(startMomRole);

        // wurlitzer
        addWurlitzerRole(resolver, loadMachine, emRole, startMomRole);

        addUpgradeRole(resolver, emMachine, emRole, UPGRADE_1_ROLE_ID, UPGRADE_VERSION_1);
        addUpgradeRole(resolver, emMachine, emRole, UPGRADE_2_ROLE_ID, UPGRADE_VERSION_2);

        return testbed;
    }

    protected void addTimeSyncRole(ITestbedMachine machine) {
        RunCommandFlowContext timeSyncFlowContext = new RunCommandFlowContext.Builder("cmd")
                .args(Arrays.asList(
                        "/C",
                        "net start w32time & " +
                                "w32tm /config /manualpeerlist:isltime02.ca.com & " +
                                "net stop w32time & " +
                                "net start w32time & " +
                                "w32tm /config /update & " +
                                "w32tm /resync /force"))
                .doNotPrependWorkingDirectory()
                .dontUseWindowsShell()
                .build();
        ExecutionRole timeSyncRole =
                new ExecutionRole.Builder("timesync_" + machine.getMachineId()).syncCommand(
                        timeSyncFlowContext).build();
        timeSyncRole.before(Arrays.asList(machine.getRoles()));
        machine.addRole(timeSyncRole);
    }

    public static IRole addStartWvRole(ITestbedMachine machine, EmRole emRole, IRole beforeRole) {
        // starts EM and WebView
        ExecutionRole startRole = new ExecutionRole.Builder(emRole.getRoleId() + "_start")
                .syncCommand(emRole.getWvRunCommandFlowContext())
                .build();
        startRole.after(beforeRole);
        machine.addRole(startRole);
        return startRole;
    }

    @NotNull
    protected void addUpgradeRole(final ITasResolver tasResolver, final ITestbedMachine machine, final EmRole emRole, final String roleId, final String upgradeVersion) {

        EmUpgradeRole role = EmUpgradeRole.Builder.fromPlatform(machine.getPlatform(), roleId, tasResolver)
                .olderEmInstallDir(emRole.getInstallDir())
                .silentInstallChosenFeatures(emRole.getSerializedSilentInstallChosenFeatures())
                .sampleResponseFile(machine.getAutomationBaseDir() + "installers/em/installer.properties")
                .version(upgradeVersion)
                .nostartUpgrade()
                .caEulaPath("/ca-eula.silent.txt")
                .build();

        role.after(emRole);

        machine.addRole(role);
    }


    private static final String BANKING_CONSOLE_CMD = "Banking-Console";
    private static final String BANKING_CONSOLE_FINAL_TEXT = "Please enter a command";
    private static final String BANKING_CONSOLE_CONTROL_CMD = "Banking-Console-Control";
    private static final String BANKING_CONSOLE_CONTROL_FINAL_TEXT = "Finished";
    private static final String START_REQ_ROLE_ID_SUFFIX = "_start_req";
    public static final String ENV_NB_START_REQUESTS = "nbStartRequests";

    public static IRole addNowhereBankRole(ITestbedMachine machine, EmRole emRole,
                                           String secondHost, RoleUtility.NowhereBankLoad[] nbLoads, final ITasResolver tasResolver) {

        String nbPath = machine.getAutomationBaseDir() + "nowherebank";
        String agentPath = machine.getAutomationBaseDir() + "agent";
        final String nowhereBankRoleName = machine.getMachineId() + "_nowherebank";
        final GenericRole nowhereBankRole =
                new GenericRole.Builder(nowhereBankRoleName, tasResolver)
                        .unpack(NowhereBankVersion.v13.getArtifact(), nbPath)
                        .configuration(nbPath + "/NowhereBank.properties",
                                new LinkedHashMap<String, String>() {{
                                    put("nowherebank.host", tasResolver.getHostnameById(nowhereBankRoleName));
                                    put("nowherebank.command.host", tasResolver.getHostnameById(nowhereBankRoleName));
                                }})
                        .build();
        machine.addRole(nowhereBankRole);
        nowhereBankRole.after(emRole);

        /*DefaultArtifact agentArtifact = new DefaultArtifact(AGENT_GROUP_ID, AGENT_ARTIFACT_ID,
                                            TasExtension.ZIP.getValue(), tasResolver.getDefaultVersion());
        GenericRole agentRole = new GenericRole.Builder(machine.getMachineId() + "_agent", tasResolver)
                                            .unpack(agentArtifact, agentPath).build();
        machine.addRole(agentRole);
        agentRole.after(nowhereBankRole);

        FileCreatorFlowContext copyContext = new FileCreatorFlowContext.Builder()
                                    .fromFile(agentPath + "/wily/Agent.jar")
                                    .destinationPath(nbPath + "/wily/Agent.jar").build();
        UniversalRole copyRole = new UniversalRole.Builder(machine.getMachineId() + "_agent_copy", tasResolver)
                                    .runFlow(FileCreatorFlow.class, copyContext).build();
        machine.addRole(copyRole);
        copyRole.after(agentRole);*/

        Map<String, String> replaceAgentHostConfig = new HashMap<>();
        replaceAgentHostConfig.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT",
                tasResolver.getHostnameById(emRole.getRoleId()));
        replaceAgentHostConfig.put("introscope.agent.deep.entrypoint.enabled", "false");
        replaceAgentHostConfig.put("introscope.agent.transactiontracer.sampling.perinterval.count", "1");
        replaceAgentHostConfig.put("introscope.agent.transactiontracer.sampling.interval.seconds", "120");
        UniversalRole configRole =
                new UniversalRole.Builder(machine.getMachineId() + "_config", tasResolver)
                        .configuration(nbPath + "/wily/core/config/IntroscopeAgent.profile", replaceAgentHostConfig)
                        .build();
        machine.addRole(configRole);
        configRole.after(nowhereBankRole/*copyRole*/);

        Map<String, String> environmentProps = new HashMap<>();
        environmentProps.put("_JAVA_OPTIONS", "-Xms64m");

        String cmd = BANKING_CONSOLE_CMD;

        UniversalRole.Builder startRoleBuilder = new UniversalRole.Builder(machine.getMachineId() + "_console", tasResolver);

        if (machine.getPlatform() != Platform.WINDOWS) {
            cmd += ".sh";

            RunCommandFlowContext chmodContext = new RunCommandFlowContext.Builder("/bin/chmod")
                    .args(Arrays.asList("a+x", cmd))
                    .workDir(nbPath)
                    .doNotPrependWorkingDirectory()
                    .build();

            startRoleBuilder.syncCommand(chmodContext);
        }
        RunCommandFlowContext consoleContext = new RunCommandFlowContext.Builder(cmd)
                .terminateOnMatch(BANKING_CONSOLE_FINAL_TEXT)
                .workDir(nbPath)
                .environment(environmentProps)
                .build();

        startRoleBuilder.syncCommand(consoleContext).build();

        IRole consoleRole = startRoleBuilder.build();

        machine.addRole(consoleRole);
        consoleRole.after(configRole);

        IRole lastStartRole;
        if (secondHost != null) {
            String nbPath2 = machine.getAutomationBaseDir() + "nowherebank2";
            GenericRole nowhereBankRole2 =
                    new GenericRole.Builder(machine.getMachineId() + "_nowherebank2", tasResolver)
                            .unpack(NowhereBankVersion.v11.getArtifact(), nbPath2).build();
            machine.addRole(nowhereBankRole2);
            nowhereBankRole2.after(consoleRole);

            /*FileCreatorFlowContext copyContext2 = new FileCreatorFlowContext.Builder()
                                                    .fromFile(agentPath + "/wily/Agent.jar")
                                                    .destinationPath(nbPath2 + "/wily/Agent.jar").build();
            UniversalRole copyRole2 = new UniversalRole.Builder(machine.getMachineId() + "_agent_copy2", tasResolver)
                                                    .runFlow(FileCreatorFlow.class, copyContext2).build();
            machine.addRole(copyRole2);
            copyRole2.after(nowhereBankRole2);*/

            Map<String, String> replaceNnPortConfig = new HashMap<>();
            replaceNnPortConfig.put("nowherebank.command.port", "10301");
            UniversalRole configRole2 =
                    new UniversalRole.Builder(machine.getMachineId() + "_config2", tasResolver)
                            .configuration(nbPath2 + "/NowhereBank.properties", replaceNnPortConfig)
                            .build();
            machine.addRole(configRole2);
            configRole2.after(nowhereBankRole2/*copyRole2*/);

            // TODO: Set nowherebank.host here as is done for the first Nowhere bank host.

            Map<String, String> replaceAgentHostConfig2 = new HashMap<>();
            replaceAgentHostConfig2.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", secondHost);
            replaceAgentHostConfig2.put("introscope.agent.deep.entrypoint.enabled", "false");
            replaceAgentHostConfig2.put("introscope.agent.transactiontracer.sampling.perinterval.count", "1");
            replaceAgentHostConfig2.put("introscope.agent.transactiontracer.sampling.interval.seconds", "120");
            UniversalRole configRole3 =
                    new UniversalRole.Builder(machine.getMachineId() + "_config3", tasResolver)
                            .configuration(nbPath2 + "/wily/core/config/IntroscopeAgent.profile", replaceAgentHostConfig2)
                            .build();
            machine.addRole(configRole3);
            configRole3.after(configRole2);

            UniversalRole.Builder startRoleBuilder2 = new UniversalRole.Builder(machine.getMachineId() + "_console2", tasResolver);

            if (machine.getPlatform() != Platform.WINDOWS) {
                RunCommandFlowContext chmodContext = new RunCommandFlowContext.Builder("/bin/chmod")
                        .args(Arrays.asList("a+x", cmd))
                        .workDir(nbPath2)
                        .doNotPrependWorkingDirectory()
                        .build();

                startRoleBuilder2.syncCommand(chmodContext);
            }

            RunCommandFlowContext consoleContext2 = new RunCommandFlowContext.Builder(cmd)
                    .terminateOnMatch("Please enter a command")
                    .workDir(nbPath2)
                    .build();
            startRoleBuilder2.syncCommand(consoleContext2);

            IRole consoleRole2 = startRoleBuilder2.build();
            machine.addRole(consoleRole2);
            consoleRole2.after(configRole3);

            IRole startMessagingRole = createStartNbPartRole(machine, "_start_messaging", nbPath,
                    "startMessaging", consoleRole2, null, tasResolver);
            IRole startPortalRole = createStartNbPartRole(machine, "_start_portal", nbPath,
                    "startPortal", startMessagingRole, null, tasResolver);
            IRole startMediatorRole = createStartNbPartRole(machine, "_start_mediator", nbPath2,
                    "startMediator", startPortalRole, null, tasResolver);
            lastStartRole = createStartNbPartRole(machine, "_start_engine", nbPath,
                    "startEngine", startMediatorRole, null, tasResolver);
        } else {
            lastStartRole = createStartNbPartRole(machine, "_start_all", nbPath,
                    "startAll", consoleRole, null, tasResolver);
        }

        lastStartRole = createStartNbPartRole(machine, START_REQ_ROLE_ID_SUFFIX, nbPath,
                "startRequests", lastStartRole, ENV_NB_START_REQUESTS, tasResolver);

        if (nbLoads != null && nbLoads.length > 0) {
            for (RoleUtility.NowhereBankLoad load : nbLoads) {
                lastStartRole = createStartNbPartRole(machine, "_" + load.getCommand(),
                        nbPath, load.getCommand(), lastStartRole, null, tasResolver);
            }
        }

        return lastStartRole;
    }

    private static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";

    private static IRole createStartNbPartRole(ITestbedMachine machine, String suffixRoleId, String workDir,
                                               String command, IRole beforeRole, String envKey, ITasResolver tasResolver) {

        String cmd = BANKING_CONSOLE_CONTROL_CMD;

        UniversalRole.Builder startRoleBuilder = new UniversalRole.Builder(machine.getMachineId() + suffixRoleId, tasResolver);

        if (machine.getPlatform() != Platform.WINDOWS) {
            cmd += ".sh";

            RunCommandFlowContext chmodContext = new RunCommandFlowContext.Builder("/bin/chmod")
                    .args(Arrays.asList("a+x", cmd))
                    .workDir(workDir)
                    .doNotPrependWorkingDirectory()
                    .build();

            startRoleBuilder.syncCommand(chmodContext);
        }
        RunCommandFlowContext startContext = new RunCommandFlowContext.Builder(cmd)
                .args(Collections.singletonList(command))
                .terminateOnMatch(BANKING_CONSOLE_CONTROL_FINAL_TEXT)
                .workDir(workDir)
                .build();

        startRoleBuilder.syncCommand(startContext);

        UniversalRole startRole = startRoleBuilder.build();
        machine.addRole(startRole);
        startRole.after(beforeRole);
        if (envKey != null) {
            startRoleBuilder.getEnvProperties().add(ENV_NB_START_REQUESTS, startContext);
        }
        return startRole;
    }

    private void addWurlitzerRole(ITasResolver resolver, ITestbedMachine wurlitzerMachine, EmRole emRole, IRole...after) {

        String wurlitzerDir = wurlitzerMachine.getAutomationBaseDir() + "wurlitzer";
        Artifact wurlitzerArtifact = new WurlitzerArtifact(resolver).createArtifact().getArtifact();
        GenericRole wurlitzerDeploy = new GenericRole.Builder("deployWurlitzerRole", resolver)
                .unpack(wurlitzerArtifact, wurlitzerDir)
                .build();

        wurlitzerMachine.addRole(wurlitzerDeploy);

        addWurlitzer(resolver, wurlitzerMachine, emRole,wurlitzerDir, "6Complex-100agents-2apps-40frontends-200EJBsession")
                .after(wurlitzerDeploy, after);
        addWurlitzer(resolver, wurlitzerMachine, emRole, wurlitzerDir, "1_1agent-1app-2000backends")
                .after(wurlitzerDeploy, after);
        addWurlitzer(resolver, wurlitzerMachine, emRole, wurlitzerDir, "WV4-6agents-050-apps-25-backends")
                .after(wurlitzerDeploy, after);
    }

    private IRole addWurlitzer(ITasResolver resolver, ITestbedMachine machine, EmRole emRole, String wurlitzerDir, String target) {

        Map<String, String> env = new HashMap<>();

        String host = resolver.getHostnameById(emRole.getRoleId());
        int port = emRole.getEmPort();

        env.put("WURLITZER_EM_HOST", host.trim());
        env.put("WURLITZER_EM_PORT", Integer.toString(port));
        env.put("ANT_OPTS", "-Xmx1536m");


        List<String> args = new ArrayList<>();
        args.add("-f");
        args.add(wurlitzerDir + "\\scripts\\" + SYSTEM_XML);
        LOGGER.info("Using buildFileLocation: " + wurlitzerDir + "\\scripts\\" + SYSTEM_XML);
        args.add(target);

        RunCommandFlowContext runCommand = new RunCommandFlowContext.Builder("ant")
                .environment(env)
                .args(args)
                .terminateOnMatch("CONNECTED to")
                .build();

        UniversalRole startWurlitzerRole = new UniversalRole.Builder("startWurlitzer" + target + "Role", resolver)
                .syncCommand(runCommand)
                .build();
        machine.addRole(startWurlitzerRole);

        return startWurlitzerRole;
    }
}
