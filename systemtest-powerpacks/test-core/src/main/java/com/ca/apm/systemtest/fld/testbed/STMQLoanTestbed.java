package com.ca.apm.systemtest.fld.testbed;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentMonitoringOption;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlow;
import com.ca.apm.automation.action.flow.agent.DeployAgentNoinstFlowContext;
import com.ca.apm.systemtest.fld.artifact.thirdparty.MQExplorerVersion;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.IBMMQRole;
import com.ca.apm.systemtest.fld.role.MqJmsAppRole;
import com.ca.apm.systemtest.fld.testbed.machines.JMeterLoadMachine;
import com.ca.apm.tests.role.TypeperfRole;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8JavaVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @Author rsssa02
 */
@TestBedDefinition
public class STMQLoanTestbed extends PowerPackSystemTestBase {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    public ITasResolver tasResolver;

    public static final String DEFAULT_INTROSCOPE_VERSION = "99.99.sys-SNAPSHOT";

    public static final String WAS_85_ROLE_ID = "wasRoleId";
    public static final String AGENT_ROLE_ID = "wasAgentID";
    public static final String WAS_MQAPP_ID = "mqJmsAppRole";
    public static final String WAS_TESTBED_ID = "mqtestbed";
    public static final String MQ_TESTBED_ID = "ibmmqRole";

    public static final String APP_SERVER_LOG_DIR_PROPERTY_NAME = "role_webapp.appserver.log.path";
    
    // MQ Queue properties
    private static final String creditReplySOJQueue = "creditReplySOJQueue";
    private static final String creditRequestSOJQueue = "creditRequestSOJQueue";
    private static final String replySOJQueue = "replySOJQueue";
    private static final String requestSOJQueue = "requestSOJQueue";
    private static final String qManagerName = "QMGR1";

    // general properties
    protected static final String INSTALL_LOC = "c:\\sw";
    protected static final String WAS_BASE_LOC = INSTALL_LOC + "\\was";
    protected static final String WILY_BASE_DIR = INSTALL_LOC + "\\mqagent";
    protected static final String RESULTS_LOC = "c:\\sw\\results\\";
    protected static final int EM_PORT = 5001;
    protected static final String AGENT_NAME = "ppsystem_mqextension";
    protected static final String PROC_NAME = "ppsystem_mqextension_process";

    
    protected static HashMap<String, String> additionalProperties = new HashMap<>();
    
    public boolean isLegacyMode = false;


    protected HashMap<String, String> qNamesAsList = new HashMap<>();
    protected HashMap<String, Integer> qmanagerPort = new HashMap<>();

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        qNamesAsList.put(qManagerName, creditReplySOJQueue + ":" + creditRequestSOJQueue + ":"
            + replySOJQueue + ":" + requestSOJQueue);
        qmanagerPort.put(qManagerName, 1515);

        ITestbed testBed = new Testbed(WAS_TESTBED_ID);
        TestbedMachine mqTestbedMachine =
            new TestbedMachine.Builder(appServerMachine).templateId(MACHINE_TEMPLATE_ID).build();
        TestbedMachine loadTestbedMachine = (new JMeterLoadMachine(tasResolver)).init(loadMachine);

        String introscopeVersion = null;
        
        try (InputStream propsStream = getClass().getClassLoader().getResourceAsStream("tas_run.properties")) {
            if (propsStream != null) {
                LOGGER.info("Found tas_run.properties!");
                Properties props = new Properties();
                props.load(propsStream);
                introscopeVersion = props.getProperty("introscope.version");
            } else {
                LOGGER.info("tas_run.properties not found.");
            }
        } catch (IOException e) {
            LOGGER.error("Exception occurred while reading tas_run.properties: ", e);
        }

        if (introscopeVersion == null || introscopeVersion.startsWith("${")) {
            introscopeVersion = DEFAULT_INTROSCOPE_VERSION;
        }
        
        LOGGER.info("Using Introscope version: {}", introscopeVersion);
        
        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .installDir(TasBuilder.WIN_SOFTWARE_LOC + "\\em")
                .installerProperty("shouldEnableCAAPMForWebSphereMQandMB", "true")
                .installerProperty("shouldEnableCAAPMForWebSphere", "true").emPort(5001)
                .version(introscopeVersion)
                .build();
        loadTestbedMachine.addRole(emRole);

        // ///////////////////////////
        // /// Agent installer
        // ///////////////////////////
        // set version
        String artifact = "agent-noinstaller-websphere-windows";
        String legacy = "";
        // set legacy param to -legacy to download legacy installer and to
        // configure legacy directives.
        if (isLegacyMode) {
            artifact = "agent-legacy-noinstaller-websphere-windows";
            legacy = "-legacy";
        }
        DefaultArtifact agentArtifact =
            new DefaultArtifact("com.ca.apm.delivery", artifact, "", "zip", introscopeVersion);
        URL introscopeAgentURL = tasResolver.getArtifactUrl(agentArtifact);

        // Additional profile file changes
        additionalProperties.put("log4j.appender.logfile.File", AGENT_LOGS
            + "\\IntroscopeAgent.log");
        additionalProperties.put("introscope.autoprobe.logfile", AGENT_LOGS + "\\AutoProbe.log");
        additionalProperties.put("introscope.agent.agentName", AGENT_NAME);
        additionalProperties.put("introscope.agent.customProcessName", PROC_NAME);
        additionalProperties.put("introscope.autoprobe.directivesFile", "websphere-full" + legacy
            + ".pbl,hotdeploy,webspheremq" + legacy + ".pbl");
        additionalProperties.put("introscope.agent.agentAutoNamingEnabled", "false");

        DeployAgentNoinstFlowContext deployAgent1FlowContext =
            new DeployAgentNoinstFlowContext.Builder()
                .installDir(WILY_BASE_DIR)
                .installerUrl(introscopeAgentURL)
                .monitoringOptions(
                    (Arrays.asList(new AgentMonitoringOption("webspheremq.pbl",
                        "PowerPackForWebSphereMQ")))).additionalProps(additionalProperties)
                .setupEm(tasResolver.getHostnameById(emRole.getRoleId()), 5001).build();

        ExecutionRole agentInstallExe =
            new ExecutionRole.Builder(AGENT_ROLE_ID).flow(DeployAgentNoinstFlow.class,
                deployAgent1FlowContext).build();
        mqTestbedMachine.addRole(agentInstallExe);

        // //////////////////////////
        // DEPLOY APP SERVER
        // //////////////////////////
        WebSphere8Role was85Role =
            new WebSphere8Role.Builder(WAS_85_ROLE_ID, tasResolver)
                .wasVersion(WebSphere8Version.v85base)
                .wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
                .wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64)
                .wasInstallerDir(TasBuilder.WIN_SOFTWARE_LOC + "\\was85")
                .wasDeployDir(WAS_BASE_LOC).autoStart().build();
        mqTestbedMachine.addRole(was85Role);

        // ////////////////////////////////////////
        // TYPE PERF ROLE TO RUN TYPEPERF MONITOR
        // ////////////////////////////////////////
        // String testResultsShare = "\\\\" +
        // tasResolver.getHostnameById(emRole.getRoleId()) + "\\share";
        TypeperfRole typeperfRole =
            new TypeperfRole.Builder(TYPE_PERF_ROLE_ID, tasResolver)
                .metrics(new String[] {"\\Processor(_Total)\\% Processor Time"}).runTime(300L)
                .outputFileName(RESULTS_LOC + "cpu_out.csv").build();
        mqTestbedMachine.addRole(typeperfRole);

        // //////////////////////////
        // / Deploy MQ server
        // //////////////////////////
        IBMMQRole ibmmqRole =
            new IBMMQRole.Builder(MQ_TESTBED_ID, tasResolver).installPath("c:\\sw\\mq\\")
                .version(MQExplorerVersion.VER_75).createQueue(true).queueMap(qNamesAsList)
                .portMap(qmanagerPort).build();
        mqTestbedMachine.addRole(ibmmqRole);
        String nodeName = tasResolver.getHostnameById(was85Role.getRoleId()) + "Node01";

        // ///////////////////////////
        // / Install JMS Loan App
        // ///////////////////////////
        MqJmsAppRole mqJmsAppRole =
            new MqJmsAppRole.Builder(WAS_MQAPP_ID, tasResolver).websphereRole(was85Role)
                .ibmmqRole(ibmmqRole).nodeName(nodeName).profileName("AppSrv01")
                .serverName("server1").replyQ(replySOJQueue).creditReplyQ(creditReplySOJQueue)
                .creditRequestQ(creditRequestSOJQueue).requestQ(requestSOJQueue)
                .qmanagerName(qManagerName).setupAgent(true).agentDirPath(WILY_BASE_DIR)
                .wasIsAutostart(true).build();
        mqJmsAppRole.after(ibmmqRole, was85Role, agentInstallExe);
        mqTestbedMachine.addRole(mqJmsAppRole);

        ClientDeployRole clientDeployRole =
            new ClientDeployRole.Builder(CLIENT_ID_APPSERVER, tasResolver).build();
        loadTestbedMachine.addRole(clientDeployRole);

        ClientDeployRole clientDeployLoadRole =
            new ClientDeployRole.Builder(CLIENT_ID_LOAD, tasResolver).build();
        mqTestbedMachine.addRole(clientDeployLoadRole);

        testBed.addMachine(mqTestbedMachine, loadTestbedMachine);

        // init system and machine properties
        initSystemProperties(tasResolver, mqTestbedMachine, loadTestbedMachine);
        initMQSystemProperties(tasResolver.getHostnameById(MQ_TESTBED_ID), INSTALL_LOC, "7.5",
            props);
        initGenericSystemProperties(tasResolver, testBed, props, "MQ");
        updateTestBedProps(props, testBed);

        // Attach agent logs to Resman job remoteResources folder
        RemoteResource agentLogsRemoteResource =
            RemoteResource.createFromRegExp(".*log$", AGENT_LOGS);
        mqTestbedMachine.addRemoteResource(agentLogsRemoteResource);

        return testBed;
    }

    @Override
    protected void initGenericSystemProperties(ITasResolver tasResolver, ITestbed testBed,
        HashMap<String, String> props, String containerName) {
        super.initGenericSystemProperties(tasResolver, testBed, props, containerName);
        props.put("java_v2.email.recipients", "sinal04@ca.com");
        props.put("java_v2.email.sender", "mq_power_pack_system_test_automation@ca.com");
    }

    protected void initMQSystemProperties(String host, String home, String version,
        HashMap<String, String> props) {

        props.put("testbed_client.hostname", host);
        props.put("role_agent.install.dir", WILY_BASE_DIR + "/wily");
        props.put("role_webapp.container.type", "mqpp");
        props.put("role_webapp.home.dir", WAS_BASE_LOC);
        props.put("role_webapp.port", "9080");
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.appserver.dir", WAS_BASE_LOC);
        props.put("role_webapp.appserver.version", version);
        props.put(APP_SERVER_LOG_DIR_PROPERTY_NAME, WAS_BASE_LOC
            + "/profiles/AppSrv01/logs/server1");
    }

    protected void initSystemProperties(ITasResolver tasResolver,
        TestbedMachine serverTestbedMachine, TestbedMachine emTestbedMachine) {
        serverTestbedMachine.addProperty("machine.appserver.hostname",
            tasResolver.getHostnameById(WAS_85_ROLE_ID));
        serverTestbedMachine.addProperty("machine.appserver.agent.name", AGENT_NAME);
        serverTestbedMachine.addProperty("machine.appserver.process.name", PROC_NAME);
        serverTestbedMachine.addProperty("machine.appserver.results.dir", RESULTS_LOC);
        emTestbedMachine.addProperty("machine.appserver.results.dir", RESULTS_LOC);
        emTestbedMachine
            .addProperty("machine.em.hostname", tasResolver.getHostnameById(EM_ROLE_ID));
        emTestbedMachine.addProperty("machine.em.port", 5001);
    }

}
