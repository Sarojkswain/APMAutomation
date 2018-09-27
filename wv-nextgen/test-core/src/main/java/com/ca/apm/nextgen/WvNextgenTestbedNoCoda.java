/**
 *
 */
package com.ca.apm.nextgen;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.apm.commons.artifact.thirdparty.JMeterVersion;
import com.ca.apm.nextgen.qatng.artifacts.MMArtifact;
import com.ca.apm.nextgen.role.HVRAgentRole;
import com.ca.apm.nextgen.role.artifacts.The100_TSDMetricsArtifact;
import com.ca.apm.role.JMeterLoadRole;
import com.ca.apm.role.JMeterRole;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.TradeServiceAppVersion;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumEdgeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumInternetExplorerDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumStandaloneServer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.web.TradeServiceAppRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive
    .CURRENT_USER;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Testbed for the TASified WV BAT tests
 *
 * @author keyja01
 */
@TestBedDefinition
public class WvNextgenTestbedNoCoda implements ITestbedFactory {

    public static final String EM_MACHINE = "em01Machine";
    public static final String EM_ROLE = "em01";
    public static final String HVR_ROLE = "hvrRole";
    public static final String TRADE_SERVICE_ROLE = "tradeServiceRole";
    public static final String TRADE_SERVICE_CONTEXT_NAME = "TradeService";
    public static final String TOMCAT_ROLE = "webapp01";
    public static final String AGENT_ROLE = "webapp01_agent";
    public static final String SELENIUM_HUB_ROLE_ID = "seleniumHubRole";
    public static final int TOMCAT_CATALINA_PORT = 7080;
    public static final String DRIVERS_PATH = "C:\\sw\\seleniumdrivers";
    public static final String CHROME_DRIVER_PATH = DRIVERS_PATH + "\\chrome";
    public static final String MSIE_DRIVER_PATH = DRIVERS_PATH + "\\msie32b";
    public static final String SELENIUM_GRID_MACHINE_ID = "seleniumGridMachineId";
    //public static final String SELENIUM_HUB_MACHINE_ID = "seleniumHubMachineId";
    public static final String SELENIUM_HUB_MACHINE_ID = EM_MACHINE;
    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_18;
    public static final SeleniumStandaloneServer SELENIUM_STANDALONE_SERVER_VERSION
        = SeleniumStandaloneServer.V3_4_0;
    private static final Logger LOGGER = LoggerFactory.getLogger(WvNextgenTestbedNoCoda.class);
    private static final String TRADE_SERVICE_JMETER_LOAD_ROLE_ID = "tradeServiceJMeterLoadRole";
    private static final String JMETER_ROLE_ID = "jMeterRole";
    private static final String W10_MACHINE_ID = "w10machineId";

    @Override
    public ITestbed create(final ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("WvNextgenTestbedNoCoda");

        String introscopeVersion = null;

        Properties props;
        String browser = null;
        try (InputStream propsStream = getClass().getClassLoader()
            .getResourceAsStream("tas_run.properties")) {
            if (propsStream != null) {
                LOGGER.info("Found tas_run.properties!");
                props = new Properties();
                props.load(propsStream);
                introscopeVersion = props.getProperty("introscope.version");
                browser = props.getProperty("browser");
                if (isBlank(browser)) {
                    LOGGER.info("browser property in tas_run.properties is blank or unset");
                }
            } else {
                LOGGER.info("tas_run.properties not found.");
            }
        } catch (IOException e) {
            LOGGER.error("Exception occurred while reading tas_run.properties: ", e);
        }

        if (isBlank(browser) || browser.startsWith("${")) {
            LOGGER.info("browser unset");
            browser = "CHROME";
        }
        testbed.addProperty("browser", browser);
        LOGGER.info("Chosen browser: {}", browser);

        if (isBlank(introscopeVersion) || introscopeVersion.startsWith("${")) {
            introscopeVersion = tasResolver.getDefaultVersion();
        }

        LOGGER.info("Using Introscope version: {}", introscopeVersion);

        EmRole emRole = new EmRole.Builder(EM_ROLE, tasResolver)
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database", "WebView"))
            .nostartEM()
            .nostartWV()
            .wvPort(8080)
            .version(introscopeVersion)
            .build();
        TestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        emMachine.addRole(emRole);

        String emPath = emRole.getDeployEmFlowContext().getInstallDir();

//        Artifact chromeDriverArtifact =
//            new DefaultArtifact("com.ca.apm.binaries.selenium:chromedriver:zip:win32:2.24");
//        Artifact ieDriverArtifact =
//            new DefaultArtifact("com.ca.apm.binaries.selenium:IEDriverServer:zip:win32:2.45.0");

        // management modules for EM
        Artifact mm = new MMArtifact().createArtifact().getArtifact();
        UniversalRole mmRole = new UniversalRole.Builder("dl", tasResolver)
            .unpack(mm, emPath + "\\config\\modules")
//            .unpack(chromeDriverArtifact, CHROME_DRIVER_PATH)
//            .unpack(ieDriverArtifact, MSIE_DRIVER_PATH)
            .build();

        mmRole.after(emRole);
        emMachine.addRole(mmRole);

        String configPath = Paths.get(emPath, "config").toString();
        String userspath = Paths.get(emPath, "config", "users.xml").toString();
        String domainspath = Paths.get(emPath, "config", "domains.xml").toString();
        String clusterPath = Paths.get(emPath, "config", "agentclusters.xml").toString();

        FileModifierFlowContext modifyConfigs =
            new FileModifierFlowContext.Builder().resource(userspath, "/em-config/users.xml")
                .resource(domainspath, "/em-config/domains.xml")
                .resource(clusterPath, "/em-config/agentclusters.xml").build();

        ExecutionRole configuretestNg =
            new ExecutionRole.Builder("configureTestNg")
                .flow(FileModifierFlow.class, modifyConfigs).build();

        configuretestNg.after(mmRole);
        emMachine.addRole(configuretestNg);

        ExecutionRole startEm =
            new ExecutionRole.Builder("start_em")
                .asyncCommand(emRole.getEmRunCommandFlowContext())
                .build();
        startEm.after(configuretestNg);
        emMachine.addRole(startEm);

        ExecutionRole startWv =
            new ExecutionRole.Builder("start_wv")
                .asyncCommand(emRole.getWvRunCommandFlowContext())
                .build();
        startWv.after(startEm);
        emMachine.addRole(startWv);

        HVRAgentRole hvrRole =
            new HVRAgentRole.Builder(HVR_ROLE, tasResolver)
                .addMetricsArtifact(new The100_TSDMetricsArtifact())
                .loadFile("100_TSD")
                .start()
                .build();
        emMachine.addRole(hvrRole);

        emMachine.addRole(new DeployFreeRole("agentwebapp"));
        emMachine.addRole(new DeployFreeRole("tomcatagent"));

        WebAppRole<TomcatRole> tradeService =
            new TradeServiceAppRole.Builder(TRADE_SERVICE_ROLE, tasResolver)
                .version(TradeServiceAppVersion.v100)
                .contextName(TRADE_SERVICE_CONTEXT_NAME)
                .build();

        final TomcatRole tomcat1 =
            new TomcatRole.Builder(TOMCAT_ROLE, tasResolver)
                .tomcatVersion(TomcatVersion.v60)
                .autoStart()
                .tomcatCatalinaPort(TOMCAT_CATALINA_PORT)
                .webApp(tradeService)
                .build();

        IRole tomcatAgentRole = new AgentRole.Builder(AGENT_ROLE, tasResolver)
            .webAppServer(tomcat1)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .emRole(emRole)
            .build();

        emMachine.addRole(tradeService, tomcat1, tomcatAgentRole);

        emMachine.addRole(new DeployFreeRole("client01"));
        emMachine.addRole(new DeployFreeRole("webapp02"));
        emMachine.addRole(new DeployFreeRole("client02"));

        testbed.addMachine(emMachine);

        // Selenium Grid setup.

        final ITestbedMachine sgMachine = new TestbedMachine.Builder(SELENIUM_GRID_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .bitness(Bitness.b64)
            .build();
        addRoleToFixIE11Registry(sgMachine, tasResolver);

        JavaRole javaRoleSgMachine = addJavaRoleToMachine(tasResolver, sgMachine);

        final ITestbedMachine seleniumHubMachine = emMachine;
//        final ITestbedMachine seleniumHubMachine = new TestbedMachine.Builder(
//            SELENIUM_HUB_MACHINE_ID)
//            .templateId(ITestbedMachine.TEMPLATE_W64)
//            .bitness(Bitness.b64)
//            .build();

        SeleniumGridHubRole.Builder hubRoleBuilder =
            new SeleniumGridHubRole.Builder(SELENIUM_HUB_ROLE_ID, tasResolver)
                .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION);

        final String hubHost = tasResolver.getHostnameById(SELENIUM_HUB_ROLE_ID);
        LOGGER.info("Selenium Hub host at {}", hubHost);

        final SeleniumGridNodeRole sgNodeRole
            = createSeleniumNodeRole(tasResolver, sgMachine.getMachineId(), hubHost);
        sgMachine.addRole(sgNodeRole);
        configRemoteResources(sgMachine);

        ITestbedMachine w10machine = new TestbedMachine.Builder(W10_MACHINE_ID)
            .templateId(TestbedMachine.TEMPLATE_W10)
            .build();
        addRoleToFixIE11Registry(w10machine, tasResolver);
        final SeleniumGridNodeRole sgNodeEdgeRole
            = createSeleniumNodeRoleEdge(tasResolver, w10machine.getMachineId(), hubHost);
        w10machine.addRole(sgNodeEdgeRole);
        configRemoteResources(w10machine);
        JavaRole javaRoleW10Machine = addJavaRoleToMachine(tasResolver, w10machine);
        testbed.addMachine(w10machine);

        hubRoleBuilder.addNodeRole(sgNodeRole);
        hubRoleBuilder.addNodeRole(sgNodeEdgeRole);
        SeleniumGridHubRole sgHubRole = hubRoleBuilder.build();
        sgHubRole.after(sgNodeRole);
        seleniumHubMachine.addRole(sgHubRole);

        JMeterRole jMeterRole = new JMeterRole.Builder(JMETER_ROLE_ID, tasResolver)
            .jmeterVersion(JMeterVersion.v213)
            .testPlanResource("trade-service-load.jmx", "/trade-service-load.jmx")
            .customJava(javaRoleSgMachine)
            .jmeterLogFile("jmeterLogFile.log")
            .logFile("jmeter.log")
            .outputFile("jmeter-output.log")
            .build();
        jMeterRole.after(javaRoleSgMachine, tomcat1, tomcatAgentRole);

        JMeterLoadRole tradeServiceLoadRole = new JMeterLoadRole.Builder(
            TRADE_SERVICE_JMETER_LOAD_ROLE_ID, tasResolver)
            .jmeter(jMeterRole)
            .hostParameterName("targetHost")
            .host(tasResolver.getHostnameById(tomcat1.getRoleId()))
            .portParameterName("targetPort")
            .port(TOMCAT_CATALINA_PORT)
            .resultFile("jmeter-result.log")
            .script("trade-service-load.jmx")
            .isOutputToFile(true)
            .autoStart()
            .build();
        tradeServiceLoadRole.after(jMeterRole);
        sgMachine.addRole(jMeterRole, tradeServiceLoadRole);

        //testbed.addMachine(seleniumHubMachine);
        testbed.addMachine(sgMachine);

        return testbed;
    }

    private static JavaRole addJavaRoleToMachine(ITasResolver tasResolver,
        ITestbedMachine machine) {
        final String javaDir = "C:\\sw\\java\\"
            + JAVA_VERSION.getJavaRuntime().name().toLowerCase()
            + JAVA_VERSION.getArtifact().getVersion();
        JavaRole javaRole = new JavaRole.Builder("javaRole_" + machine.getMachineId(), tasResolver)
            .dir(javaDir)
            .version(JAVA_VERSION)
            .build();
        machine.addRole(javaRole);
        return javaRole;
    }

    private static void configRemoteResources(ITestbedMachine machine) {
        machine.addRemoteResource(
            new RemoteResource.RemoteResourceBuilder(RemoteResource.TEMP_FOLDER)
                .name("screenshots")
                .regExp(".*screenshot.*\\.png")
                .build());
        machine.addRemoteResource(
            new RemoteResource.RemoteResourceBuilder(RemoteResource.TEMP_FOLDER)
                .name("log4j")
                .regExp(".*\\.log")
                .build());
    }

    private SeleniumGridNodeRole createSeleniumNodeRole(ITasResolver tasResolver, String machineId,
        String hubHost) {
        LOGGER.info("Using HUB host {}", hubHost);
        URL hubUrl = null;
        try {
            hubUrl = new URL("http", hubHost, 4444, "/grid/register/");
        } catch (MalformedURLException ex) {
            LOGGER.error("HUB URL IS malformed", ex);
            throw new RuntimeException(ex);
        }

        NodeCapability firefoxCapability = new NodeCapability.Builder()
            .browserType(BrowserType.FIREFOX)
            .platform(NodePlatform.WINDOWS)
            .maxInstances(1)
            .build();

        NodeCapability internetExplorerCapability = new NodeCapability.Builder()
            .browserType(BrowserType.INTERNET_EXPLORER)
            .platform(NodePlatform.WINDOWS)
            .version("11")
            .maxInstances(1)
            .build();

        NodeCapability chromeCapability = new NodeCapability.Builder()
            .browserType(BrowserType.CHROME)
            .platform(NodePlatform.WINDOWS)
            .maxInstances(1)
            .build();

        // Define the whole node configuration
        NodeConfiguration nodeConfiguration = new NodeConfiguration.Builder()
            .addCapability(firefoxCapability)
            .addCapability(internetExplorerCapability)
            .addCapability(chromeCapability)
            .maxSession(100)
            .hub(hubUrl)
            .build();

        return new SeleniumGridNodeRole
            .Builder(machineId + "_seleniumNodeRole", tasResolver)
            .nodeConfiguration(nodeConfiguration)
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .chromeDriver(SeleniumChromeDriver.V2_29_B32)
            .internetExplorerDriver(SeleniumInternetExplorerDriver.V3_4_0_B64)
            .build();
    }

    private SeleniumGridNodeRole createSeleniumNodeRoleEdge(ITasResolver tasResolver,
        String machineId, String hubHost) {
        LOGGER.info("Using HUB host {}", hubHost);
        URL hubUrl = null;
        try {
            hubUrl = new URL("http", hubHost, 4444, "/grid/register/");
        } catch (MalformedURLException ex) {
            LOGGER.error("HUB URL IS malformed", ex);
            throw new RuntimeException(ex);
        }

        NodeCapability edgeCapability = new NodeCapability.Builder()
            .browserType(BrowserType.EDGE)
            .platform(NodePlatform.WINDOWS)
            .maxInstances(1)
            .build();

        // Define the whole node configuration
        NodeConfiguration nodeConfiguration = new NodeConfiguration.Builder()
            .addCapability(edgeCapability)
            .maxSession(100)
            .hub(hubUrl)
            .build();

        return new SeleniumGridNodeRole
            .Builder(machineId + "_seleniumNodeRole", tasResolver)
            .nodeConfiguration(nodeConfiguration)
            .standaloneServerVersion(SELENIUM_STANDALONE_SERVER_VERSION)
            .edgeDriver(SeleniumEdgeDriver.V3_14393)
            .build();
    }

    /**
     * This adds a role to a machine that sets required registry keys on Windows so that Internet
     * Explorer 11 works with Selenium at all.
     *
     * @param machine machine to fix
     * @return role which executes flow to set the registry keys for IE11 to work with Selenium
     */
    public static IRole addRoleToFixIE11Registry(ITestbedMachine machine,
        ITasResolver tasResolver) {
        Win32RegistryFlowContext context = new Win32RegistryFlowContext.Builder()
            .setValue(CURRENT_USER, "Software\\Microsoft\\Internet Explorer\\Main\\TabProcGrowth",
                DWORD, 0)
            .build();

        UniversalRole role = new UniversalRole.Builder(machine.getMachineId() + "_IE11RegistryFix",
            tasResolver)
            .runFlow(Win32RegistryFlow.class, context)
            .build();
        machine.addRole(role);

        return role;
    }
}
