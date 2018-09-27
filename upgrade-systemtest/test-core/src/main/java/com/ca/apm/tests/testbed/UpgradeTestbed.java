package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.testapp.NowhereBankVersion;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.apm.tests.flow.AGCRegisterFlow;
import com.ca.apm.tests.loads.DotNetAgentHttpConnLoadProvider;
import com.ca.apm.tests.role.EmUpgradeRole;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.*;
import com.ca.tas.role.testapp.custom.TradeServiceAppRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author jirji01
 */
@TestBedDefinition
public abstract class UpgradeTestbed implements ITestbedFactory {

    public abstract Platform platform();
    public abstract String version();
    public abstract String template();

    public String upgradeVersion() {
        return "10.5.2.8";
    }

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

    public static final List<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-XX:MaxPermSize=256m",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2g", "-Xmx2g",
            "-Dappmap.user=admin", "-Dappmap.token=" + AGCRegisterFlow.ADMIN_AUX_TOKEN);

    public static final List<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-Xms2g", "-Xmx2g");

    public static final List<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2g", "-Xmx2g", "-Dappmap.user=admin",
            "-Dappmap.token=" + AGCRegisterFlow.ADMIN_AUX_TOKEN);

    public static final List<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1g", "-Xmx1g");

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String DB_MACHINE_ID = "dbMachine";
    public static final String C1_MACHINE_ID = "c1Machine";
    public static final String C2_MACHINE_ID = "c2Machine";
    public static final String TIM1_MACHINE_ID = "tim1Machine";
    public static final String TIM2_MACHINE_ID = "tim2Machine";
    public static final String LOAD_MACHINE_ID = "loadMachine";
    public static final String ORACLE_MACHINE_ID = "oracleMachine";

    public static final String C1_ROLE_ID = "c1Role";
    public static final String C2_ROLE_ID = "c2Role";
    public static final String MOM_ROLE_ID = "momRole";
    public static final String DB_ROLE_ID = "dbRole";
    public static final String TIM1_ROLE_ID = "tim1Role";
    public static final String TIM2_ROLE_ID = "tim2Role";
    public static final String EPAGENT_ROLE_ID = "epAgentRole";
    public static final String EPAGENT_TESTAPP_ROLE_ID = "epAgentTestAppRole";
    public static final String ORACLE_ROLE_ID = "emOracleRole";

    @Override
    public ITestbed create(ITasResolver resolver) {
        
        Testbed testbed = new Testbed(getClass().getSimpleName());

        ITestbedMachine momMachine = machine(MOM_MACHINE_ID);
        testbed.addMachine(momMachine);
        ITestbedMachine dbMachine = machine(DB_MACHINE_ID);
        testbed.addMachine(dbMachine);
        ITestbedMachine c1Machine = machine(C1_MACHINE_ID);
        testbed.addMachine(c1Machine);
        ITestbedMachine c2Machine = machine(C2_MACHINE_ID);
        testbed.addMachine(c2Machine);

        ITestbedMachine tim1Machine =
                new TestbedMachine.LinuxBuilder(TIM1_MACHINE_ID).platform(Platform.LINUX)
                        .templateId("co65_tim").bitness(Bitness.b64).build();
        testbed.addMachine(tim1Machine);
        ITestbedMachine tim2Machine =
                new TestbedMachine.LinuxBuilder(TIM2_MACHINE_ID).platform(Platform.LINUX)
                        .templateId("co65_tim").bitness(Bitness.b64).build();
        testbed.addMachine(tim2Machine);
        ITestbedMachine loadMachine =
                new TestbedMachine.Builder(LOAD_MACHINE_ID).platform(Platform.WINDOWS)
                        .templateId("w64").bitness(Bitness.b64).build();
        testbed.addMachine(loadMachine);

        TIMRole tim1Role = new TIMRole.Builder(TIM1_ROLE_ID, resolver).timVersion(version()).build();
        tim1Machine.addRole(tim1Role);

        TIMRole tim2Role = new TIMRole.Builder(TIM2_ROLE_ID, resolver).timVersion(version()).build();
        tim2Machine.addRole(tim2Role);

        addDbRole(testbed, resolver);

        EmRole.Builder c1Builder = emBuilder(C1_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .tim(tim1Role)
                .tim(tim2Role)
                .nostartEM()
                .version(version())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);

        EmRole c1Role = c1Builder.build();
        c1Role.before(tim1Role, tim2Role);
        c1Machine.addRole(c1Role);

        EmRole.Builder c2Builder = emBuilder(C2_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .nostartEM()
                .version(version())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);

        EmRole c2Role = c2Builder.build();
        c2Machine.addRole(c2Role);

        EmRole.Builder momBuilder = emBuilder(MOM_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .version(version())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emCollector(c1Role, c2Role)
                .configProperty("transport.buffer.input.maxNum", "2400")
                .configProperty("transport.outgoingMessageQueueSize", "6000")
                .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "10")
                .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "10")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", "5000")
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);
        setDb(resolver, momBuilder, MOM_ROLE_ID);
        EmRole momRole = momBuilder.build();
        momMachine.addRole(momRole);

        EmRole wvRole = addWvRole(testbed, resolver);

        RoleUtility.addMmRole(momMachine, momRole.getRoleId() + "_mm", momRole, "NowhereBankMM");

        IRole startMomRole = RoleUtility.addStartEmRole(momMachine, momRole, false, momRole);
        IRole startC1Role = RoleUtility.addStartEmRole(c1Machine, c1Role, false, c1Role);
        IRole startC2Role = RoleUtility.addStartEmRole(c2Machine, c2Role, false, c2Role);
        IRole startWvRole = addStartWvRole(dbMachine, wvRole, startMomRole);
        IRole startNbRole = addNowhereBankRole(c1Machine, c1Role, null, null, resolver);
        IRole startTsRole = addTradeServiceRole(loadMachine, resolver, momRole, tim1Role, tim1Machine);
        startNbRole.after(momRole);

        // .NET
        DotNetAgentHttpConnLoadProvider dotNetProvider = new DotNetAgentHttpConnLoadProvider(version());
        testbed.addMachines(dotNetProvider.initMachines());
        dotNetProvider.initTestbed(MOM_ROLE_ID, resolver);

        // EP Agent
//        addEPAgentRole(resolver, load1Machine);

        addUpgradeRole(resolver, momMachine, momRole);
        addUpgradeRole(resolver, c1Machine, c1Role);
        addUpgradeRole(resolver, c2Machine, c2Role);
        addUpgradeRole(resolver, dbMachine, wvRole);

        registerAGC(testbed, resolver);

        return testbed;
    }

    protected void registerAGC(Testbed testbed, ITasResolver resolver) {
    }

    protected void setDb(ITasResolver resolver, EmRole.Builder builder, String roleId) {
        builder.dbhost(resolver.getHostnameById(DB_ROLE_ID));
    }

    protected void addTimeSyncRole(ITestbedMachine machine) {
        RunCommandFlowContext timeSyncFlowContext =  new RunCommandFlowContext.Builder("cmd")
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

    protected IRole addDbRole(ITestbed testbed, ITasResolver resolver) {
        return null;
    }

    protected EmRole addWvRole(ITestbed testbed, ITasResolver resolver) {

        EmRole wvRole = emBuilder(DB_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("Database", "WebView"))
                .version(version())
                .wvEmHost(resolver.getHostnameById(MOM_ROLE_ID))
                .wvEmPort(((EmRole)testbed.getRoleById(MOM_ROLE_ID)).getEmPort())
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .nostartWV()
                .nostartEM()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .build();

        wvRole.before(testbed.getRoleById(MOM_ROLE_ID));
        testbed.getMachineById(DB_MACHINE_ID).addRole(wvRole);
        return wvRole;
    }


    @NotNull
    protected void addUpgradeRole(final ITasResolver tasResolver, final ITestbedMachine machine, final EmRole emRole) {
        final String upgradeRoleId = machine.getMachineId() + "_upgrade";

        EmUpgradeRole role = EmUpgradeRole.Builder.fromPlatform(machine.getPlatform(), upgradeRoleId, tasResolver)
                .olderEmInstallDir(emRole.getInstallDir())
                .silentInstallChosenFeatures(emRole.getSerializedSilentInstallChosenFeatures())
                .sampleResponseFile(machine.getAutomationBaseDir() + "installers/em/installer.properties")
//                .dbhost(emRole.getDbHost())
//                .dbname(emRole.getDbName())
//                .dbuser(emRole.getDbUser())
//                .dbpassword(emRole.getDbPassword())
//                .dbport(Integer.parseInt(emRole.getDbPort()))
//                .dbAdminPassword(emRole.getDbAdminPassword())
//                .dbAdminUser(emRole.getDbAdminUser())
//                .databaseDir(emRole.getDatabaseDir())
                .version(upgradeVersion())
                .nostartUpgrade()
                .caEulaPath("/ca-eula.silent.txt")
                .build();

        role.after(emRole);

        machine.addRole(role);
    }


    private IRole addTradeServiceRole(ITestbedMachine tradeServiceAppMachine, ITasResolver tasResolver, EmRole introscopeCollectorRole, TIMRole timRole, ITestbedMachine timMachine) {
        TomcatRole tomcatRole =
                new TomcatRole.Builder("tomcat60", tasResolver).tomcatVersion(TomcatVersion.v60)
                        .tomcatCatalinaPort(7080).jdkHomeDir("C:/Program Files/Java/jdk1.6.0_45")
                        .installDir("C:/sw/apache-tomcat-6.0.36").build();
        tradeServiceAppMachine.addRole(tomcatRole);
        RoleUtility.createSleepTxtFile(tradeServiceAppMachine, tomcatRole, tasResolver);

        Map<String, String> agentAdditionalProps = new HashMap<>();
        IRole createSnippetRole = RoleUtility.createBaSnippetFile(tradeServiceAppMachine,
                agentAdditionalProps, tasResolver);

        AgentRole agentRole = new AgentRole.Builder("agent-tomcat", tasResolver)
                .webAppServer(tomcatRole)
                .platform(IBuiltArtifact.ArtifactPlatform.WINDOWS)
                .emRole(introscopeCollectorRole)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL)
                .additionalProperties(agentAdditionalProps)
                .webAppAutoStart()
                .build();
        // TODO pospa02: avoid cyclic dependency as AgentRole defined dependency in other way
        // agentRole.after(tomcatRole);
        tradeServiceAppMachine.addRole(agentRole);
        agentRole.after(introscopeCollectorRole, createSnippetRole);

        // HACK: currently has to be defined after AgentRole to ensure it won't be overridden
        TradeServiceAppRole tradeServiceAppRole =
                new TradeServiceAppRole.Builder("trade-service", tasResolver)
                        .tomcatRole(tomcatRole).build();

        tradeServiceAppRole.after(agentRole);
        tradeServiceAppMachine.addRole(tradeServiceAppRole);
        RoleUtility.addNewTradeServiceWars(tradeServiceAppMachine, tomcatRole,
                tradeServiceAppRole, tasResolver);

        TIMAttendeeRole tsTimAttendeeRole =
                new TIMAttendeeRole.Builder("tsapp-tim-attendee", timRole, tasResolver).build();
        tradeServiceAppMachine.addRole(tsTimAttendeeRole);

        String baseUrl = "http://" + tasResolver.getHostnameById(tomcatRole.getRoleId()) + ":7080/";
        // every 4 hours downloads all links from TradeService
        String cronEntry = "* */4 * * * root wget -r --delete-after -nd " + baseUrl + "TradeService/ 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_tradeservice", cronEntry));

        // every minute downloads login
        cronEntry = "* * * * * root wget --delete-after -nd " + baseUrl + "AuthenticationService/ServletA6 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_login", cronEntry));

        // every even minute downloads TradeOptions 4 times
        String wgetStr = "wget --delete-after -nd " + baseUrl + "TradeService/TradeOptions 2>/dev/null";
        cronEntry = "*/2 * * * * root " + wgetStr + "\n" +
                "*/2 * * * * root sleep 15; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 30; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 45; " + wgetStr;
        timMachine.addRole(new CronEntryRole("cron_trade_options", cronEntry));

        return tradeServiceAppRole;
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

        Map<String, String> replaceAgentHostConfig = new HashMap<String, String>();
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

        Map<String, String> environmentProps = new HashMap<String, String>();
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

            Map<String, String> replaceNnPortConfig = new HashMap<String, String>();
            replaceNnPortConfig.put("nowherebank.command.port", "10301");
            UniversalRole configRole2 =
                    new UniversalRole.Builder(machine.getMachineId() + "_config2", tasResolver)
                            .configuration(nbPath2 + "/NowhereBank.properties", replaceNnPortConfig)
                            .build();
            machine.addRole(configRole2);
            configRole2.after(nowhereBankRole2/*copyRole2*/);

            // TODO: Set nowherebank.host here as is done for the first Nowhere bank host.

            Map<String, String> replaceAgentHostConfig2 = new HashMap<String, String>();
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
                .args(Arrays.asList(command))
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

    protected void addEPAgentRole(ITasResolver tasResolver, ITestbedMachine machine) {
        //epagent has no legacy mode, so no need to check
        String artifact_epa = "epagent-package";
        String artifact_testapp = "epatestapp";

        // epagent
        DefaultArtifact epagentArtifact = null;
        epagentArtifact = new DefaultArtifact("com.ca.apm.delivery", artifact_epa,
                "jsw-win","zip", version());

        //epagent_testapp
        DefaultArtifact agentArtifact_testapp =
                new DefaultArtifact("com.ca.apm.coda-projects.test-tools", artifact_testapp,"dist","zip", tasResolver.getDefaultVersion());


        String dir = machine.getAutomationBaseDir() + "epagent\\";
        Map<String, String> config = Collections.singletonMap("agentManager.url.1", tasResolver.getHostnameById(MOM_ROLE_ID) + ":5001");
        //get agent
        GenericRole epAgentRole = new GenericRole.Builder(EPAGENT_ROLE_ID, tasResolver)
                .unpack(epagentArtifact, dir)
                .configuration(dir + "IntroscopeEPAgent.properties", config)
                .build();

        GenericRole epAgentRole_Testapp = new GenericRole.Builder(EPAGENT_TESTAPP_ROLE_ID, tasResolver)
                .unpack(agentArtifact_testapp, machine.getAutomationBaseDir() + "epagent")
                .build();

        epAgentRole.before(epAgentRole_Testapp);

//        RunCommandFlowContext chmodContext = new RunCommandFlowContext.Builder("java ")
//                .args(Arrays.asList("a+x", cmd))
//                .workDir(workDir)
//                .doNotPrependWorkingDirectory()
//                .build();


        machine.addRole(epAgentRole, epAgentRole_Testapp);
    }

}
