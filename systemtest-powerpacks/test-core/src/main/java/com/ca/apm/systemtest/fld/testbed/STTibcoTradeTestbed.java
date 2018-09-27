package com.ca.apm.systemtest.fld.testbed;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.agent.AgentMonitoringOption;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.TibcoTradeAppRole;
import com.ca.apm.systemtest.fld.testbed.machines.WASTradeAppTestbed;
import com.ca.apm.tests.artifact.JMeterVersion;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.apm.tests.role.TypeperfRole;
import com.ca.apm.tests.testbed.machines.DbOracleTradeDbMachine;
import com.ca.apm.tests.tibco.artifact.TibcoSoftwareComponentVersions;
import com.ca.apm.tests.tibco.testbed.TibcoTestBedUtil;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @Author rsssa02
 */
@TestBedDefinition
public class STTibcoTradeTestbed extends PowerPackSystemTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(STTibcoTradeTestbed.class);

    // machine and test bed
    public static final String TIBCO_TESTBED_ID = "tibcoTestBed";

    // Role Ids
    public static final String TIBCO_SERVER_ROLE_ID = "tibTradeApp_role";
    public static final String AGENT_ROLE_ID = "tib_agent_role";

    // Variables
    public static final String INSTALL_UNPACK_DIR = "C:\\temp\\tibco";
    public static final String INSTALL_LOGFILE = "installer.log";
    public static final String AGENT_NAME = "TibcoBWPP";
    public static final String PROC_NAME = "tibco";

    public boolean isLegacyMode = false;

    public static String TIBCO_ROLE_ID = null;

    public static HashMap<String, String> additionalProperties = new HashMap<>();

    protected String getProductVersion(ITasResolver tasResolver) {
        String productVersion;
        // productVersion = tasResolver.getDefaultVersion();
        productVersion = "99.99.sys-SNAPSHOT";
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        LOGGER.info("XXXXXXXXXX Using productVersion = " + productVersion);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        return productVersion;
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        String productVersion = getProductVersion(tasResolver);

        ITestbed testBed = new Testbed(TIBCO_TESTBED_ID);
        // DB
        TestbedMachine orclMachine =
            (new DbOracleTradeDbMachine(dbMachine, tasResolver, MACHINE_TEMPLATE_ID)).init();
        // WAS MACHINE
        TestbedMachine tradeMachine =
            (new WASTradeAppTestbed(loadMachine, tasResolver).init(dbMachine));

        // create a machine for EM using the w64 template id
        TestbedMachine machine =
            TestBedUtils.createWindowsMachine(appServerMachine, MACHINE_TEMPLATE_ID);

        IRole trade6Role =
            tradeMachine.getRoleById(loadMachine + WASTradeAppTestbed.TRADE_APP_ROLE_ID);
        IRole was85Role = tradeMachine.getRoleById(loadMachine + WASTradeAppTestbed.WAS_85_ROLE_ID);
        IRole orclRole =
            orclMachine.getRoleById(dbMachine + DbOracleTradeDbMachine.STOCKTRADER_SCRIPT_ROLE_ID);
        testBed.addMachine(orclMachine);
        testBed.addMachine(tradeMachine);
        // Jmeter load deploy
        ClientDeployRole clientDeployRole =
            new ClientDeployRole.Builder(CLIENT_ID_APPSERVER, tasResolver).build();
        tradeMachine.addRole(clientDeployRole);
        // clw workstation metric validation deploy
        ClientDeployRole clientDeployRole2 =
            new ClientDeployRole.Builder(CLIENT_ID_LOAD, tasResolver).build();
        machine.addRole(clientDeployRole2);

        JMeterRole jMeterRole =
            new JMeterRole.Builder(JMETER_ROLE_ID, tasResolver)
                .scriptFilePath(TasBuilder.WIN_SOFTWARE_LOC).version(JMeterVersion.VER_2_11)
                .build();
        tradeMachine.addRole(jMeterRole);

        JavaRole javaRole =
            new JavaRole.Builder(JAVA7_ROLE_ID, tasResolver)
                .version(JavaBinary.WINDOWS_64BIT_JDK_17).dir(INSTALL_BASE + "\\java7").build();
        machine.addRole(javaRole);
        machine.addProperty("server.java7.home.dir", javaRole.getInstallDir());

        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver).installDir(INSTALL_BASE + "\\em")
                .installerProperty("shouldEnableCAAPMForTibcoBW", "true")
                .installerProperty("shouldEnableCAAPMForTibcoEMS", "true").emPort(5001)
                .version(productVersion).build();
        tradeMachine.addRole(emRole);

        // ////////////////////////////////////////
        // TYPE PERF ROLE TO RUN TYPEPERF MONITOR
        // ////////////////////////////////////////
        TypeperfRole typeperfRole =
            new TypeperfRole.Builder(TYPE_PERF_ROLE_ID, tasResolver)
                .metrics(new String[] {"\\Processor(_Total)\\% Processor Time"}).runTime(300L)
                .outputFileName(RESULTS_LOC + "\\cpu_out.csv").build();
        machine.addRole(typeperfRole);

        // Add roles
        IRole rvRole =
            TibcoTestBedUtil.getTibcoRVRole(TibcoSoftwareComponentVersions.TibcoRVWindowsx64v8_4_0,
                tasResolver, INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole emsRole =
            TibcoTestBedUtil.getTibcoEMSRole(
                TibcoSoftwareComponentVersions.TibcoEMSWindowsx64v6_3_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole traRole =
            TibcoTestBedUtil.getTibcoTRARole(
                TibcoSoftwareComponentVersions.TibcoTRAWindowsx64v5_8_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);
        IRole bwRole =
            TibcoTestBedUtil.getTibcoBWRole(
                TibcoSoftwareComponentVersions.TibcoBWWindowsx64v5_11_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR);

        final String domainName = getIndependentDomainName();

        IRole bwAdminRole =
            TibcoTestBedUtil.getTibcoBWAdminRole(
                TibcoSoftwareComponentVersions.TibcoAdminWindowsx64v5_8_0, tasResolver,
                INSTALL_LOGFILE, INSTALL_DIR, INSTALL_UNPACK_DIR, domainName);

        // Fix the role dependencies
        bwAdminRole.after(traRole, rvRole, bwRole);
        bwRole.after(rvRole, traRole);
        emsRole.after(rvRole);
        traRole.after(rvRole, emsRole);
        // Add roles to the machine
        machine.addRole(rvRole);
        machine.addRole(emsRole);
        machine.addRole(traRole);
        machine.addRole(bwRole);
        machine.addRole(bwAdminRole);

        // Deploy and tradeapp for tibco
        @SuppressWarnings("static-access")
        TibcoTradeAppRole tibcoTradeAppRole =
            new TibcoTradeAppRole.Builder(TIBCO_SERVER_ROLE_ID, tasResolver)
                .bwRole(TibcoTestBedUtil.TIBCO_BW_ROLE_ID)
                .emsRole(TibcoTestBedUtil.TIBCO_EMS_ROLE_ID)
                .dbRole(dbMachine + DbOracleTradeDbMachine.DB_ROLE_ID).tibcoHomeDir(INSTALL_DIR)
                .tibEMSConfigDir(TibcoTestBedUtil.TIBCO_EMS_CONFIG_DIR)
                .tibcoDomainName(domainName).setupAgent(true) // to setup java agent
                                                                         // param in the startup
                                                                         // script
                .agentInstallDir(INSTALL_DIR).wasRole(was85Role.getRoleId()).build();
        tibcoTradeAppRole
            .after(orclRole, traRole, rvRole, bwRole, emsRole, bwAdminRole, trade6Role);
        machine.addRole(tibcoTradeAppRole);
        TIBCO_ROLE_ID = tibcoTradeAppRole.getRoleId();

        // Additional profile file changes
        additionalProperties.put("log4j.appender.logfile.File", AGENT_LOGS
            + "\\IntroscopeAgent.log");
        additionalProperties.put("introscope.autoprobe.logfile", AGENT_LOGS + "\\AutoProbe.log");
        additionalProperties.put("introscope.agent.agentName", AGENT_NAME);
        additionalProperties.put("introscope.agent.customProcessName", PROC_NAME);
        additionalProperties.put("introscope.agent.agentAutoNamingEnabled", "false");

        // set version
        String artifact = "agent-noinstaller-default-windows";
        String legacy = "";
        // set legacy param to -legacy to download legacy installer and to configure legacy
        // directives.
        if (isLegacyMode) {
            artifact = "agent-legacy-noinstaller-default-windows";
            legacy = "-legacy";
        }
        String tibMonitoringPbl = "tibcobw-full" + legacy;
        additionalProperties.put("introscope.autoprobe.directivesFile", "default-full" + legacy
            + ".pbl,hotdeploy,tibcobw-full" + legacy + ".pbl");

        // downloading 99.99.sys for now.
        DefaultArtifact agentArtifact =
            new DefaultArtifact("com.ca.apm.delivery", artifact, "", "zip", productVersion);
        URL introscopeAgentURL = tasResolver.getArtifactUrl(agentArtifact);
        LOGGER.info("introscopeAgentURL = " + introscopeAgentURL);

        DeployAgentNoinstFlowContext deployAgent1FlowContext =
            new DeployAgentNoinstFlowContext.Builder()
                .installDir(INSTALL_DIR)
                .installerUrl(introscopeAgentURL)
                .monitoringOptions(
                    (Arrays.asList(new AgentMonitoringOption(tibMonitoringPbl + ".pbl",
                        "SOAExtensionForTibcoBW"))))
                .intrumentationLevel(AgentInstrumentationLevel.FULL)
                . // instrumentation level is always full for system test or performance run
                setupEm(tasResolver.getHostnameById(EM_ROLE_ID), emRole.getEmPort())
                .additionalProps(additionalProperties).build();

        ExecutionRole agentInstallExe =
            new ExecutionRole.Builder(AGENT_ROLE_ID).flow(DeployAgentNoinstFlow.class,
                deployAgent1FlowContext).build();

        agentInstallExe.after(bwAdminRole);
        machine.addRole(agentInstallExe);

        testBed.addMachine(machine);

        initSystemProperties(tasResolver, machine, tradeMachine);
        initGenericSystemProperties(tasResolver, testBed, props, "TibcoBW");
        updateTestBedProps(props, testBed);

        // Attach agent logs to Resman job remoteResources folder
        RemoteResource agentLogsRemoteResource =
            RemoteResource.createFromRegExp(".*log$", AGENT_LOGS);
        machine.addRemoteResource(agentLogsRemoteResource);

        return testBed;
    }

    private void initSystemProperties(ITasResolver tasResolver, TestbedMachine serverTestbed,
        TestbedMachine emTestbed) {
        serverTestbed.addProperty("machine.appserver.hostname",
            tasResolver.getHostnameById(TIBCO_SERVER_ROLE_ID));
        serverTestbed.addProperty("machine.appserver.agent.name", AGENT_NAME);
        serverTestbed.addProperty("machine.appserver.process.name", PROC_NAME);
        serverTestbed.addProperty("machine.agent.wily.install.dir", INSTALL_DIR + "/wily");
        serverTestbed.addProperty("machine.appserver.results.dir", RESULTS_LOC);
        emTestbed.addProperty("machine.appserver.results.dir", RESULTS_LOC);
        emTestbed.addProperty("machine.em.hostname", tasResolver.getHostnameById(EM_ROLE_ID));
        emTestbed.addProperty("machine.em.port", 5001);
    }

    private String getIndependentDomainName(){
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        return dFormat.format(new Date());
    }
}
