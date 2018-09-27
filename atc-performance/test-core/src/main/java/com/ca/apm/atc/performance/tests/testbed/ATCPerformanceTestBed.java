package com.ca.apm.atc.performance.tests.testbed;

import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.apm.commons.artifact.thirdparty.JMeterVersion;
import com.ca.apm.role.JMeterLoadRole;
import com.ca.apm.role.JMeterRole;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumEdgeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumInternetExplorerDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumStandaloneServer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.*;
import com.ca.tas.role.seleniumgrid.*;
import com.ca.tas.role.testapp.custom.TradeServiceAppRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive.CURRENT_USER;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;
import static java.lang.String.format;

/**
 *  Testbed for ATC Performance testing.
 *
 *  @author Alexander Sinyushkin (sinal04@ca.com)
 */
@TestBedDefinition
public class ATCPerformanceTestBed implements ITestbedFactory {
    public static final String REPORT_FOLDER = "C:\\atc-perf-report";
    public static final String INTROSCOPE_VERSION_PROP_NAME = "introscope.version";
    public static final String TEST_APP_BASE_URL_PROP_NAME = "test.applicationBaseURL";
    public static final String SELENIUM_GRID_MACHINE_ID = "SELENIUM_GRID_MACHINE";
    public static final String SELENIUM_HUB_ROLE_ID = "selenium_hub_role";
    public static final int TOMCAT_CATALINA_PORT = 7080;

    private static final Logger LOGGER = LoggerFactory.getLogger(ATCPerformanceTestBed.class);
    private static final String INTROSCOPE_VERSION = "10.5.2.16";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("ATCPerformanceTestbed");
        TestbedMachine emMachine = new TestbedMachine.Builder("EM_MACHINE")
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .build();

        // EM & Nowherebank - on the same host
        EmRole emRole = new EmRole.Builder("em_role", tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database", "WebView"))
                .nostartEM()
                .nostartWV()
                .wvPort(8080)
                .version(INTROSCOPE_VERSION)
                .build();

        testbed.addProperty(INTROSCOPE_VERSION_PROP_NAME, INTROSCOPE_VERSION);
        testbed.addProperty(TEST_APP_BASE_URL_PROP_NAME,
                format("http://%s:8080", tasResolver.getHostnameById("em_role")));

        String emInstallPath = emRole.getDeployEmFlowContext().getInstallDir();

        ManagementModuleRole mmRole = new ManagementModuleRole("status_mm", "/StatusTestMM.jar",
                emInstallPath);
        mmRole.after(emRole);

        IRole lastInAChainNowhereBankRole =
                RoleUtility.addNowhereBankRole(emMachine, emRole, null,
                        new RoleUtility.NowhereBankLoad[]{
                                RoleUtility.NowhereBankLoad.SlowWebService,
                                RoleUtility.NowhereBankLoad.DBFailing,
                                RoleUtility.NowhereBankLoad.Requests,
                                RoleUtility.NowhereBankLoad.NewSlowWebService,
                                RoleUtility.NowhereBankLoad.WebServiceDegredation
                        },
                        tasResolver);

        IRole delayRole =
                new DelayRole("delayAfterNowhereBankStart", (int) TimeUnit.MINUTES.toSeconds(2));
        delayRole.after(lastInAChainNowhereBankRole);

        IRole nowhereBankMMRole = RoleUtility.addMmRole(emMachine, "nowherebank_mm_role", emRole,
                "NowhereBankMM");

        ExecutionRole startEm =
                new ExecutionRole.Builder("start_em")
                        .asyncCommand(emRole.getEmRunCommandFlowContext())
                        .build();
        startEm.after(nowhereBankMMRole, delayRole);

        ExecutionRole startWv =
                new ExecutionRole.Builder("start_wv")
                        .asyncCommand(emRole.getWvRunCommandFlowContext())
                        .build();
        startWv.after(startEm);

        emMachine.addRole(emRole, mmRole, delayRole, startEm, startWv);

        // TIM
        TestbedMachine timMachine = TestBedUtils.createLinuxMachine("TIM_MACHINE", ITestbedMachine.TEMPLATE_CO65);
        TIMRole timRole = new TIMRole.Builder("tim_role", tasResolver).build();
        timMachine.addRole(timRole);

        // TradeService
        TestbedMachine tradeServiceAppMachine = TestBedUtils.createWindowsMachine("TRADE_SERVICE_MACHINE",
                ITestbedMachine.TEMPLATE_W64);

        JavaRole javaRole =
                addJavaRoleToMachine(tasResolver, tradeServiceAppMachine);

        TomcatRole tomcatRole = new TomcatRole.Builder("tomcat_role", tasResolver)
                .customJava(javaRole)
                .tomcatCatalinaPort(TOMCAT_CATALINA_PORT)
                .jdkHomeDir("C:/jdk8")
                .tomcatVersion(TomcatVersion.v80)
                .autoStart()
                .build();
        javaRole.before(tomcatRole);

        IRole tomcatAgentRole =
                new AgentRole.Builder("tomcat_agent_role", tasResolver)
                        .platform(IBuiltArtifact.ArtifactPlatform.WINDOWS)
                        .webAppServer(tomcatRole)
                        .emRole(emRole)
                        .webAppAutoStart()
                        .build();
        tomcatRole.before(tomcatAgentRole);

        TradeServiceAppRole tradeServiceAppRole =
                new TradeServiceAppRole.Builder("trade_service_app_role", tasResolver)
                        .tomcatRole(tomcatRole)
                        .build();

        TIMAttendeeRole tradeServiceTimAttendeeRole =
                new TIMAttendeeRole.Builder("trade_service_app_tim_attendee_role",
                        timRole, tasResolver).build();

        tradeServiceAppMachine.addRole(tomcatRole, tomcatAgentRole, tradeServiceAppRole,
                tradeServiceTimAttendeeRole);

        String tomcatHost = tasResolver.getHostnameById(tomcatRole.getRoleId());

        // every 5 minutes downloads all links from TradeService
        String cronEntry = "*/5 * * * * root wget -r --delete-after -nd http://" +
                tomcatHost +
                ":7080/TradeService/ 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_trade_service_role", cronEntry));

        // every minute downloads login
        cronEntry = "* * * * * root wget --delete-after -nd http://" +
                tasResolver.getHostnameById(tomcatRole.getRoleId()) +
                ":7080/AuthenticationService/ServletA6 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_login", cronEntry));

        // every even minute downloads TradeOptions 4 times
        String wgetStr = "wget --delete-after -nd http://" +
                tasResolver.getHostnameById(tomcatRole.getRoleId()) +
                ":7080/TradeService/TradeOptions 2>/dev/null";
        cronEntry = "*/2 * * * * root " + wgetStr + "\n" +
                "*/2 * * * * root sleep 15; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 30; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 45; " + wgetStr;
        timMachine.addRole(new CronEntryRole("cron_trade_options", cronEntry));


        // Selenium grid setup.
        final ITestbedMachine seleniumGridMachine = new TestbedMachine.Builder(SELENIUM_GRID_MACHINE_ID)
                .platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64)
                .build();
        addRoleToFixIE11Registry(seleniumGridMachine, tasResolver);

        JavaRole javaRoleSgMachine = addJavaRoleToMachine(tasResolver, seleniumGridMachine);

        final ITestbedMachine seleniumHubMachine = emMachine;
//        final ITestbedMachine seleniumHubMachine = new TestbedMachine.Builder("SELENIUM_HUB_MACHINE")
//                .templateId(ITestbedMachine.TEMPLATE_W64)
//                .bitness(Bitness.b64)
//                .build();

        SeleniumGridHubRole.Builder hubRoleBuilder =
                new SeleniumGridHubRole.Builder(SELENIUM_HUB_ROLE_ID, tasResolver)
                        .standaloneServerVersion(SeleniumStandaloneServer.V3_4_0);

        final String hubHost = tasResolver.getHostnameById(SELENIUM_HUB_ROLE_ID);

        testbed.addProperty("selenium.webdriverURL", "http://" + hubHost + ":4444/wd/hub");

        final SeleniumGridNodeRole seleniumGridNodeRole
                = createSeleniumNodeRole(tasResolver, seleniumGridMachine.getMachineId(), hubHost);
        seleniumGridMachine.addRole(seleniumGridNodeRole);

        configRemoteResources(seleniumGridMachine);

        seleniumGridMachine.addRemoteResource(RemoteResource.createFromName("atc-perf-report", REPORT_FOLDER));


//        seleniumGridMachine.addRemoteResource(
//                new RemoteResource.RemoteResourceBuilder(RemoteResource.TEMP_FOLDER)
//                        .name("atc-perf-report")
//                        .regExp(".*")
//                        .build());


        //Edge machine setup
        ITestbedMachine w10machine = new TestbedMachine.Builder("WINDOWS_10_MACHINE")
                .templateId(TestbedMachine.TEMPLATE_W10)
                .build();
        addRoleToFixIE11Registry(w10machine, tasResolver);
        final SeleniumGridNodeRole seleniumGridEdgeNodeRole
                = createSeleniumEdgeNodeRole(tasResolver, w10machine.getMachineId(), hubHost);
        w10machine.addRole(seleniumGridEdgeNodeRole);
        configRemoteResources(w10machine);
        addJavaRoleToMachine(tasResolver, w10machine);
        hubRoleBuilder.addNodeRole(seleniumGridEdgeNodeRole);


        hubRoleBuilder.addNodeRole(seleniumGridNodeRole);
        SeleniumGridHubRole sgHubRole = hubRoleBuilder.build();
        sgHubRole.after(seleniumGridNodeRole);
        seleniumHubMachine.addRole(sgHubRole);

        JMeterRole jMeterRole = new JMeterRole.Builder("jmeter_role", tasResolver)
                .jmeterVersion(JMeterVersion.v213)
                .testPlanResource("trade-service-load.jmx", "/trade-service-load.jmx")
                .customJava(javaRoleSgMachine)
                .jmeterLogFile("jmeterLogFile.log")
                .logFile("jmeter.log")
                .outputFile("jmeter-output.log")
                .build();
        jMeterRole.after(javaRoleSgMachine);

        JMeterLoadRole tradeServiceLoadRole = new JMeterLoadRole.Builder(
                "trade_service_jmeter_load_role", tasResolver)
                .jmeter(jMeterRole)
                .hostParameterName("targetHost")
                .host(tasResolver.getHostnameById(tomcatRole.getRoleId()))
                .portParameterName("targetPort")
                .port(TOMCAT_CATALINA_PORT)
                .resultFile("trade-service-load-jmeter-result.log")
                .script("trade-service-load.jmx")
                .isOutputToFile(true)
                .autoStart()
                .build();
        tradeServiceLoadRole.after(jMeterRole);
        seleniumGridMachine.addRole(jMeterRole, tradeServiceLoadRole);

        //testbed.addMachine(seleniumHubMachine);

        testbed.addMachine(emMachine, timMachine, tradeServiceAppMachine, seleniumGridMachine);
        testbed.addMachine(w10machine);
        return testbed;
    }

    private static JavaRole addJavaRoleToMachine(ITasResolver tasResolver,
                                                 ITestbedMachine machine) {
        JavaRole javaRole = new JavaRole.Builder(machine.getMachineId() + "_java_18_role", tasResolver)
                .dir("C:/jdk8")
                .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51)
                .build();

        machine.addRole(javaRole);

        return javaRole;
    }

    /**
     * This adds a role to a machine that sets required registry keys on Windows so that Internet
     * Explorer 11 works with Selenium at all.
     *
     * @param machine           machine to fix
     * @param tasResolver       TAS resolver
     * @return                  role which executes flow to set the registry keys for IE11 to work with Selenium
     */
    private IRole addRoleToFixIE11Registry(ITestbedMachine machine,
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

    private SeleniumGridNodeRole createSeleniumNodeRole(ITasResolver tasResolver, String machineId,
                                                        String hubHost) {
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
                .Builder(machineId + "_selenium_node_role", tasResolver)
                .nodeConfiguration(nodeConfiguration)
                .standaloneServerVersion(SeleniumStandaloneServer.V3_4_0)
                .chromeDriver(SeleniumChromeDriver.V2_29_B32)
                .internetExplorerDriver(SeleniumInternetExplorerDriver.V3_4_0_B64)
                .build();
    }

    private SeleniumGridNodeRole createSeleniumEdgeNodeRole(ITasResolver tasResolver,
                                                            String machineId, String hubHost) {
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
                .Builder(machineId + "_selenium_node_role", tasResolver)
                .nodeConfiguration(nodeConfiguration)
                .standaloneServerVersion(SeleniumStandaloneServer.V3_4_0)
                .edgeDriver(SeleniumEdgeDriver.V3_14393)
                .build();
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

}
