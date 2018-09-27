package com.ca.apm.systemtest.fld.testbed;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.agent.AgentMonitoringOption;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.systemtest.fld.artifact.thirdparty.OSBVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.PPWebLogicVersion;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.OSBRole;
import com.ca.apm.systemtest.fld.role.OSBTradeImportRole;
import com.ca.apm.systemtest.fld.testbed.machines.JMeterLoadMachine;
import com.ca.apm.systemtest.fld.testbed.machines.WLSTradeTestbed;
import com.ca.apm.tests.role.TypeperfRole;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @Author rsssa02
 */
@TestBedDefinition
public class STOracleSBTestbed extends PowerPackSystemTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(STOracleSBTestbed.class);

    // All role ID's as constants to use within the test classes.
    public static final String WLS_ROLE_ID = "weblogicRole";
    public static final String OSB_ROLE_ID = "osbRole";
    public static final String AGENT_ROLE_ID = "agentRole";
    public static final String OSB_JVM_ROLE_ID = "jvmRole";

    protected boolean isLegacyMode = false;
    protected static final String INSTALL_LOC = "c:\\sw";
    protected static final String RESULTS_LOC = "c:\\sw\\results\\";
    protected static final int EM_PORT = 5001;
    protected static final String AGENT_NAME = "ppsystem_osbweblogic";
    protected static final String PROC_NAME = "ppsystem_osbweblogic_process";
    protected static final String JVM_DEPLOY_DIR = INSTALL_LOC + "\\Java\\jdk1.7";
    protected static final PPWebLogicVersion WLS_VERSION = PPWebLogicVersion.v1036x86w;

    public ITasResolver tasResolver;
    protected static HashMap<String, String> csvExcelMapper = new HashMap<>();

    protected static HashMap<String, String> additionalProperties = new HashMap<>();

    static {
        csvExcelMapper.put(RESULTS_LOC + "test_memory.csv", "osb_memory");
        csvExcelMapper.put(RESULTS_LOC + "test_cpu.csv", "osb_cpu");
    }

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

        ITestbed testBed = new Testbed("OSBSystemTest");
        String APPSERVER_HOME = INSTALL_LOC + "\\OSB";
        String WLS_HOME = APPSERVER_HOME + "\\wlserver_10.3";

        TestbedMachine testMachine =
            new TestbedMachine.Builder(appServerMachine).templateId(MACHINE_TEMPLATE_ID).build();
        testBed.addMachine(testMachine);
        ITestbedMachine testMachine2 =
            (new WLSTradeTestbed(tasResolver)).init(dbMachine, INSTALL_LOC);

        TestbedMachine testMachine3 = (new JMeterLoadMachine(tasResolver)).init(loadMachine);
        testBed.addMachine(testMachine2, testMachine3);

        // JVM to run OSB setup.exe file
        JavaRole jvmRole =
            new JavaRole.Builder(OSB_JVM_ROLE_ID, tasResolver)
                .version(JavaBinary.WINDOWS_32BIT_JDK_17_0_25).dir(JVM_DEPLOY_DIR).build();
        testMachine.addRole(jvmRole);

        WebLogicRole wlRole =
            new WebLogicRole.Builder(WLS_ROLE_ID, tasResolver).installLocation(APPSERVER_HOME)
                .customJvm(jvmRole.getInstallDir()).installDir(WLS_HOME)
                .version(WLS_VERSION.getArtifact()).build();
        wlRole.after(jvmRole);
        testMachine.addRole(wlRole);

        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver).installDir(INSTALL_LOC + "\\em")
                .installerProperty("shouldEnableCAAPMForOSB", "true").emPort(EM_PORT)
                .version(productVersion).build();
        testMachine3.addRole(emRole);

        // ////////////////////////////////////////
        // TYPE PERF ROLE TO RUN TYPEPERF MONITOR
        // ////////////////////////////////////////
        // String testResultsShare = "\\\\" + tasResolver.getHostnameById(emRole.getRoleId()) +
        // "\\share";
        TypeperfRole typeperfRole =
            new TypeperfRole.Builder(TYPE_PERF_ROLE_ID, tasResolver)
                .metrics(new String[] {"\\Processor(_Total)\\% Processor Time"}).runTime(300L)
                .outputFileName(RESULTS_LOC + "cpu_out.csv").build();
        testMachine.addRole(typeperfRole);

        // install OSB
        OSBRole osbRole =
            new OSBRole.Builder(OSB_ROLE_ID, tasResolver).version(OSBVersion.VER_11G)
                .installPath(APPSERVER_HOME).autoStart().wlsInstallPath(WLS_HOME)
                .jreHomePath(jvmRole.getInstallDir() + "\\jre").build();

        // Additional profile file changes
        additionalProperties.put("log4j.appender.logfile.File", AGENT_LOGS
            + "\\IntroscopeAgent.log");
        additionalProperties.put("introscope.autoprobe.logfile", AGENT_LOGS + "\\AutoProbe.log");
        additionalProperties.put("introscope.agent.agentName", AGENT_NAME);
        additionalProperties.put("introscope.agent.customProcessName", PROC_NAME);
        additionalProperties.put("introscope.agent.agentAutoNamingEnabled", "false");

        /*
         * ITasArtifact introscopeAgentArtifact = new
         * AgentNoInstaller(AgentNoInstaller.Type.WEBLOGIC,
         * IBuiltArtifact.ArtifactPlatform.WINDOWS,
         * tasResolver).createArtifact("99.99.sys-SNAPSHOT");
         * URL introscopeAgentURL = tasResolver.getArtifactUrl(introscopeAgentArtifact);
         */

        // set version
        String artifact = "agent-noinstaller-weblogic-windows";
        String legacy = "";
        if (isLegacyMode) {
            artifact = "agent-legacy-noinstaller-weblogic-windows";
            legacy = "-legacy";
        }
        String osbMonitoringPbl = "OSB-full" + legacy;
        additionalProperties.put("introscope.autoprobe.directivesFile", "weblogic-full" + legacy
            + ".pbl,hotdeploy," + osbMonitoringPbl + ".pbl");

        DefaultArtifact agentArtifact =
            new DefaultArtifact("com.ca.apm.delivery", artifact, "", "zip", productVersion);
        URL introscopeAgentURL = tasResolver.getArtifactUrl(agentArtifact);
        LOGGER.info("introscopeAgentURL = " + introscopeAgentURL);

        DeployAgentNoinstFlowContext deployAgent1FlowContext =
            new DeployAgentNoinstFlowContext.Builder()
                .installDir(APPSERVER_HOME)
                .installerUrl(introscopeAgentURL)
                .monitoringOptions(
                    (Arrays.asList(new AgentMonitoringOption(osbMonitoringPbl + ".pbl",
                        "SOAExtensionForOSB"))))
                .intrumentationLevel(AgentInstrumentationLevel.FULL)
                .setupEm(tasResolver.getHostnameById(EM_ROLE_ID), emRole.getEmPort())
                .additionalProps(additionalProperties).build();
        ExecutionRole agentInstallExe =
            new ExecutionRole.Builder(AGENT_ROLE_ID).flow(DeployAgentNoinstFlow.class,
                deployAgent1FlowContext).build();

        testMachine.addRole(agentInstallExe);

        // OSB installation needs WLS 10.3 to be installed.
        // Install webapp resource to OSB. ///
        OSBTradeImportRole osbImportRole =
            new OSBTradeImportRole.Builder("webapp_role", tasResolver)
                .osbHome(APPSERVER_HOME + "/Oracle_Home").wlsServerHome(WLS_HOME)
                .javaHome(jvmRole.getInstallDir()).wlsPort("7021")
                .wlsRoleID(WLSTradeTestbed.wlsRole.getRoleId()).build();
        osbImportRole.after(osbRole);
        testMachine.addRole(osbImportRole);

        @SuppressWarnings("static-access")
        ClientDeployRole clientDeployRole =
            new ClientDeployRole.Builder(STOracleSBTestbed.CLIENT_ID_APPSERVER, tasResolver)
                .build();
        testMachine.addRole(clientDeployRole);

        osbRole.after(wlRole);
        testMachine.addRole(osbRole);
        initSystemProperties(tasResolver, testMachine, testMachine3);
        initGenericSystemProperties(tasResolver, testBed, props, "OSB");
        updateTestBedProps(props, testBed);

        // Attach agent logs to Resman job remoteResources folder
        RemoteResource agentLogsRemoteResource =
            RemoteResource.createFromRegExp(".*log$", AGENT_LOGS);
        testMachine.addRemoteResource(agentLogsRemoteResource);

        return testBed;
    }

    private void initSystemProperties(ITasResolver tasResolver, TestbedMachine serverTestbed,
        TestbedMachine emTestbed) {
        serverTestbed.addProperty("machine.appserver.hostname",
            tasResolver.getHostnameById(OSB_ROLE_ID));
        serverTestbed.addProperty("machine.appserver.agent.name", AGENT_NAME);
        serverTestbed.addProperty("machine.appserver.process.name", PROC_NAME);
        serverTestbed.addProperty("machine.appserver.results.dir", RESULTS_LOC);
        emTestbed.addProperty("machine.appserver.results.dir", RESULTS_LOC);
        emTestbed.addProperty("machine.em.hostname", tasResolver.getHostnameById(EM_ROLE_ID));
        emTestbed.addProperty("machine.em.port", EM_PORT);
    }

}
