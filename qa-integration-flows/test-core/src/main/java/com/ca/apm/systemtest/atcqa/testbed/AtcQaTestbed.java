package com.ca.apm.systemtest.atcqa.testbed;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.commons.artifact.thirdparty.JMeterVersion;
import com.ca.apm.role.JMeterLoadRole;
import com.ca.apm.role.JMeterRole;
import com.ca.apm.systemtest.atcqa.artifact.thirdparty.OrderEngineAppVersion;
import com.ca.apm.systemtest.atcqa.artifact.thirdparty.ReportingServiceAppVersion;
import com.ca.apm.systemtest.atcqa.artifact.thirdparty.TradeServiceAppVersion;
import com.ca.apm.systemtest.atcqa.role.TradeServiceAppRole;
import com.ca.apm.systemtest.fld.role.AGCRegisterRole;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.ManagementModuleRole;
import com.ca.tas.role.PhantomJSRole;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UtilityRole;
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

@TestBedDefinition
public class AtcQaTestbed implements ITestbedFactory, FldTestbedProvider, Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtcQaTestbed.class);

    private static final boolean AGC_USE_LINUX = false;
    private static final boolean COLL_USE_LINUX = false;
    private static final boolean MOM_USE_LINUX = false;

    private static final String AGC_ROLE_ID = "agcRole";
    private static final String COLL01_ROLE_ID = "coll01Role";
    private static final String COLL02_ROLE_ID = "coll02Role";
    private static final String COLL03_ROLE_ID = "coll03Role";
    private static final String COLL04_ROLE_ID = "coll04Role";
    private static final String MOM01_ROLE_ID = "mom01Role";
    private static final String MOM02_ROLE_ID = "mom02Role";
    private static final String AGC_MOM01_REGISTER_ROLE_ID = "agcMom01RegisterRole";
    private static final String AGC_MOM02_REGISTER_ROLE_ID = "agcMom02RegisterRole";
    private static final String PHANTOMJS_ROLE_ID = "phantomJSRole";
    private static final String TOMCAT_ROLE_ID = "tomcatRole";
    private static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    private static final String TRADE_SERVICE_APP_ROLE_ID = "tradeServiceAppRole";
    private static final String SELENIUM_GRID_HUB_ROLE_ID = "seleniumGridHubRole";
    private static final String SELENIUM_GRID_NODE_ROLE_ID = "seleniumGridNodeRole";
    private static final String JMETER_JAVA_ROLE_ID = "jMeterJavaRole";
    private static final String JMETER_ROLE_ID = "jMeterRole";
    private static final String STATUSTEST_MM01_ROLE_ID = "statusTestMM01Role";
    private static final String STATUSTEST_MM02_ROLE_ID = "statusTestMM02Role";
    private static final String SLEEP_CONFIG_FILE_ROLE_ID = "sleepConfigFileRole";

    private static final String EM_INSTALL_DIR_LINUX = "/opt/automation/deployed/em";
    private static final String EM_INSTALL_DIR_WINDOWS = "C:\\automation\\deployed\\em\\";
    private static final String EM_INSTALLER_DIR_LINUX = "/opt/automation/deployed/installer";
    private static final String EM_INSTALLER_DIR_WINDOWS = "C:\\automation\\deployed\\installer\\";

    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "wily";
    private static final String DB_ADMIN_USER = "postgres";
    private static final String DB_ADMIN_PASSWORD = "C@wilyapm90";
    // private static final String DB_NAME = "cemdb";

    private static final int EM_PORT = 5001;
    private static final int EM_WEBPORT = 8081;
    private static final int WV_PORT = 8080;

    private static final String MACHINE_TEMPLATE_ID_CO65 = "co65";
    private static final String MACHINE_TEMPLATE_ID_W64 = "w64";

    private static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private static final Collection<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
        "-Xmx1024m", "-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
        "-Xmx1024m", "-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> WV_LAXNL_JAVA_OPTION =
        Arrays
            .asList(
                "-Djava.awt.headless=true",
                "-Dorg.owasp.esapi.resources=./config/esapi",
                "-Dsun.java2d.noddraw=true",
                "-javaagent:./product/webview/agent/wily/Agent.jar",
                "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1024m", "-Xmx1024m",
                "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError");

    private static final String JAVA_INSTALL_DIR_WINDOWS = "C:\\sw\\java\\";
    private static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_18;
    private static final String TOMCAT_INSTALL_DIR = "C:/sw/apache-tomcat-6.0.36";
    private static final String TOMCAT_JDK_HOME_DIR = "C:/Program Files/Java/jdk1.6.0_45";
    private static final int TOMCAT_CATALINA_PORT = 7080;
    private static final int SELENIUM_GRID_HUB_PORT = 4444;
    private static final SeleniumChromeDriver SELENIUM_CHROME_DRIVER_VERSION =
        SeleniumChromeDriver.V2_22_B32;
    private static final String TEST_APPLICATION_BASE_URL_FORMAT = "http://%s:8082";
    private static final String SELENIUM_WEBDRIVER_URL_FORMAT = "http://%s:4444/wd/hub";
    private static final String SELENIUM_GRID_NODE_qResXResolution = "1920";
    private static final String SELENIUM_GRID_NODE_qResYResolution = "1080";
    private static final int SELENIUM_GRID_NODE_CONFIGURATION_maxSession = 7;

    private static final TradeServiceAppVersion TRADE_SERVICE_APP_VERSION =
        TradeServiceAppVersion.v_10_5_2_4;
    private static final ReportingServiceAppVersion REPORTING_SERVICE_APP_VERSION =
        ReportingServiceAppVersion.v_10_5_2_4;
    private static final OrderEngineAppVersion ORDER_ENGINE_APP_VERSION =
        OrderEngineAppVersion.v_10_5_2_4;

    private static final String STATUSTEST_MM_JAR = "/StatusTestMM.jar";

    private static final String SLEEP_CONFIG_FILE = "c:/sleep.txt";
    private static final Collection<String> SLEEP_CONFIG_FILE_CONTENT = Arrays
        .asList("0-24 200-1000");
    private static final String JMETER_SCRIPT = "trade-service-load.jmx";

    // private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    private static String emVersion;

    private ITestbedMachine agcMachine;
    private ITestbedMachine coll01Machine;
    private ITestbedMachine coll02Machine;
    private ITestbedMachine coll03Machine;
    private ITestbedMachine coll04Machine;
    private ITestbedMachine mom01Machine;
    private ITestbedMachine mom02Machine;
    private ITestbedMachine test01Machine;

    private DotNetAgentProvider dotNetAgentProvider;

    static {
        // emVersion = fldConfig.getEmVersion();
        emVersion = "10.5.2.4";
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("AtcQaTestbed");

        dotNetAgentProvider = new DotNetAgentProvider();
        dotNetAgentProvider.setAgentVersion(emVersion);

        Collection<ITestbedMachine> machines = initMachines();
        if (machines != null) {
            testbed.addMachines(machines);
        }

        initTestbed(testbed, tasResolver);
        return testbed;
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        if (AGC_USE_LINUX) {
            agcMachine =
                (new TestbedMachine.LinuxBuilder(AGC_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
        } else {
            agcMachine =
                (new TestbedMachine.Builder(AGC_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
        }

        if (COLL_USE_LINUX) {
            coll01Machine =
                (new TestbedMachine.LinuxBuilder(COLL01_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
            coll02Machine =
                (new TestbedMachine.LinuxBuilder(COLL02_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
            coll03Machine =
                (new TestbedMachine.LinuxBuilder(COLL03_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
            coll04Machine =
                (new TestbedMachine.LinuxBuilder(COLL04_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
        } else {
            coll01Machine =
                (new TestbedMachine.Builder(COLL01_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
            coll02Machine =
                (new TestbedMachine.Builder(COLL02_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
            coll03Machine =
                (new TestbedMachine.Builder(COLL03_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
            coll04Machine =
                (new TestbedMachine.Builder(COLL04_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
        }

        if (MOM_USE_LINUX) {
            mom01Machine =
                (new TestbedMachine.LinuxBuilder(MOM01_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
            mom02Machine =
                (new TestbedMachine.LinuxBuilder(MOM02_MACHINE_ID))
                    .templateId(MACHINE_TEMPLATE_ID_CO65).platform(Platform.CENTOS)
                    .bitness(Bitness.b64).build();
        } else {
            mom01Machine =
                (new TestbedMachine.Builder(MOM01_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
            mom02Machine =
                (new TestbedMachine.Builder(MOM02_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                    .platform(Platform.WINDOWS).bitness(Bitness.b64).build();
        }

        test01Machine =
            (new TestbedMachine.Builder(TEST01_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64)
                .platform(Platform.WINDOWS).bitness(Bitness.b64).build();

        Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(agcMachine);
        machines.add(coll01Machine);
        machines.add(coll02Machine);
        machines.add(coll03Machine);
        machines.add(coll04Machine);
        machines.add(mom01Machine);
        machines.add(mom02Machine);
        machines.add(test01Machine);

        Collection<ITestbedMachine> dotNetAgentProviderMachines =
            dotNetAgentProvider.initMachines();
        if (dotNetAgentProviderMachines != null) {
            machines.addAll(dotNetAgentProviderMachines);
        }

        return machines;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        LOGGER.info("AtcQaTestbed.initTestbed():: emVersion = " + emVersion);

        // AGC
        EmRole agcRole = getAgcRole(AGC_ROLE_ID, emVersion, tasResolver);
        agcMachine.addRole(agcRole);
        String agcHost = tasResolver.getHostnameById(agcRole.getId());

        // collectors
        EmRole coll01Role = getCollRole(COLL01_ROLE_ID, emVersion, tasResolver);
        coll01Machine.addRole(coll01Role);

        EmRole coll02Role = getCollRole(COLL02_ROLE_ID, emVersion, tasResolver);
        coll02Machine.addRole(coll02Role);

        EmRole coll03Role = getCollRole(COLL03_ROLE_ID, emVersion, tasResolver);
        coll03Machine.addRole(coll03Role);
        String coll03Host = tasResolver.getHostnameById(coll03Role.getId());

        EmRole coll04Role = getCollRole(COLL04_ROLE_ID, emVersion, tasResolver);
        coll04Machine.addRole(coll04Role);

        // MOMs
        EmRole mom01Role =
            getMomRole(MOM01_ROLE_ID, emVersion, Arrays.asList(coll01Role, coll02Role), tasResolver);
        mom01Role.after(coll01Role, coll02Role, coll03Role, coll03Role);
        mom01Machine.addRole(mom01Role);
        String mom01Host = tasResolver.getHostnameById(mom01Role.getId());
        AGCRegisterRole reg01Role =
            getAgcRegisterRole(AGC_MOM01_REGISTER_ROLE_ID, agcHost, mom01Host, mom01Role,
                tasResolver);

        EmRole mom02Role =
            getMomRole(MOM02_ROLE_ID, emVersion, Arrays.asList(coll03Role, coll04Role), tasResolver);
        mom02Role.after(coll01Role, coll02Role, coll03Role, coll03Role);
        mom02Machine.addRole(mom02Role);
        String mom02Host = tasResolver.getHostnameById(mom02Role.getId());
        AGCRegisterRole reg02Role =
            getAgcRegisterRole(AGC_MOM02_REGISTER_ROLE_ID, agcHost, mom02Host, mom02Role,
                tasResolver);

        reg01Role.after(mom01Role, mom02Role);
        mom01Machine.addRole(reg01Role);
        reg02Role.after(mom01Role, mom02Role);
        mom02Machine.addRole(reg02Role);

        ManagementModuleRole mom01MMRole =
            new ManagementModuleRole(STATUSTEST_MM01_ROLE_ID, STATUSTEST_MM_JAR,
                mom01Role.getInstallDir());
        mom01MMRole.after(mom01Role, mom02Role);
        mom01Machine.addRole(mom01MMRole);

        ManagementModuleRole mom02MMRole =
            new ManagementModuleRole(STATUSTEST_MM02_ROLE_ID, STATUSTEST_MM_JAR,
                mom02Role.getInstallDir());
        mom02MMRole.after(mom01Role, mom02Role);
        mom02Machine.addRole(mom02MMRole);

        // start collectors
        Collection<IRole> mom01Roles = Arrays.asList(mom01Machine.getRoles());
        Collection<IRole> mom02Roles = Arrays.asList(mom02Machine.getRoles());

        IRole coll01RoleStart = getStartEmRole(coll01Role, true, false);
        coll01RoleStart.after(mom01Roles);
        coll01Machine.addRole(coll01RoleStart);

        IRole coll02RoleStart = getStartEmRole(coll02Role, true, false);
        coll02RoleStart.after(mom01Roles);
        coll02Machine.addRole(coll02RoleStart);

        IRole coll03RoleStart = getStartEmRole(coll03Role, true, false);
        coll03RoleStart.after(mom02Roles);
        coll03Machine.addRole(coll03RoleStart);

        IRole coll04RoleStart = getStartEmRole(coll04Role, true, false);
        coll04RoleStart.after(mom02Roles);
        coll04Machine.addRole(coll04RoleStart);

        // test machine - Tomcat + Agent, TradeServiceApp(s), Selenium, JMeter, PhantomJS
        TomcatRole tomcatRole = getTomcatRole(TOMCAT_ROLE_ID, tasResolver);
        test01Machine.addRole(tomcatRole);
        String tomcatHost = tasResolver.getHostnameById(tomcatRole.getId());

        AgentRole tomcatAgentRole =
            getAgentRole(TOMCAT_AGENT_ROLE_ID, tomcatRole, coll01Role, tasResolver);
        test01Machine.addRole(tomcatAgentRole);

        TradeServiceAppRole tradeServiceAppRole =
            getTradeServiceAppRole(TRADE_SERVICE_APP_ROLE_ID, tomcatRole, tasResolver);
        test01Machine.addRole(tradeServiceAppRole);

        UtilityRole<FileModifierFlowContext> sleepConfigFileRole =
            getSleepConfigFileRole(SLEEP_CONFIG_FILE_ROLE_ID);
        test01Machine.addRole(sleepConfigFileRole);

        SeleniumGridNodeRole seleniumGridNodeRole =
            getSeleniumGridNodeRole(SELENIUM_GRID_NODE_ROLE_ID, tomcatHost, tasResolver);
        SeleniumGridHubRole seleniumGridHubRole =
            getSeleniumGridHubRole(SELENIUM_GRID_HUB_ROLE_ID, seleniumGridNodeRole, tasResolver);
        seleniumGridNodeRole.before(seleniumGridHubRole);
        test01Machine.addRole(seleniumGridNodeRole);
        test01Machine.addRole(seleniumGridHubRole);

        JavaRole jMeterJavaRole = addJavaRoleToMachine(JMETER_JAVA_ROLE_ID, tasResolver);
        test01Machine.addRole(jMeterJavaRole);

        JMeterRole jMeterRole =
            (new JMeterRole.Builder(JMETER_ROLE_ID, tasResolver)).jmeterVersion(JMeterVersion.v213)
                .testPlanResource(JMETER_SCRIPT, "/" + JMETER_SCRIPT).customJava(jMeterJavaRole)
                .build();
        jMeterRole.after(jMeterJavaRole, tomcatRole, tomcatAgentRole);
        test01Machine.addRole(jMeterRole);

        JMeterLoadRole jMeterLoadRole =
            (new JMeterLoadRole.Builder(JMETER_LOAD_ROLE_ID, tasResolver)).jmeter(jMeterRole)
                .hostParameterName("targetHost").host(tomcatHost).portParameterName("targetPort")
                .port(TOMCAT_CATALINA_PORT).resultFile("jmeter-result.log").script(JMETER_SCRIPT)
                .isOutputToFile(true)
                // .autoStart()
                .build();
        jMeterLoadRole.after(jMeterRole);
        test01Machine.addRole(jMeterLoadRole);

        PhantomJSRole phantomjsRole = getPhantomJSRole(PHANTOMJS_ROLE_ID, tasResolver);
        test01Machine.addRole(phantomjsRole);

        // set properties
        String testApplicationBaseURL = String.format(TEST_APPLICATION_BASE_URL_FORMAT, agcHost);
        testbed.addProperty("test.applicationBaseURL", testApplicationBaseURL);
        LOGGER.info("AtcQaTestbed.initTestbed():: test.applicationBaseURL = "
            + testApplicationBaseURL);

        String seleniumWebdriverURL = String.format(SELENIUM_WEBDRIVER_URL_FORMAT, tomcatHost);
        testbed.addProperty("selenium.webdriverURL", seleniumWebdriverURL);
        LOGGER.info("AtcQaTestbed.initTestbed():: selenium.webdriverURL = " + seleniumWebdriverURL);

        // .Net provider
        dotNetAgentProvider.setEmHost(coll03Host);
        dotNetAgentProvider.initTestbed(testbed, tasResolver);
    }

    private EmRole getAgcRole(String roleId, String version, ITasResolver tasResolver) {
        EmRole.Builder builder;
        if (AGC_USE_LINUX) {
            builder = (new EmRole.LinuxBuilder(roleId, tasResolver))
            // .installDir(EM_INSTALL_DIR_LINUX)
            ;
        } else {
            builder = (new EmRole.Builder(roleId, tasResolver))
            // .installDir(EM_INSTALL_DIR_WINDOWS)
            ;
        }
        builder =
            builder
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "WebView", "Database"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                // .emWebPort(EM_WEBPORT)
                // .wvPort(WV_PORT)
                // .emPort(EM_PORT)
                .version(version)
                .dbuser(DB_USER)
                .dbpassword(DB_PASSWORD)
                .dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                // .dbname(DB_NAME)
                // .dbhost()
                .configProperty("introscope.apmserver.teamcenter.master", "true")
                .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);
        EmRole agcRole = builder.build();
        return agcRole;
    }

    private EmRole getCollRole(String roleId, String version, ITasResolver tasResolver) {
        EmRole.Builder builder;
        if (COLL_USE_LINUX) {
            builder =
                (new EmRole.LinuxBuilder(roleId, tasResolver)).installDir(EM_INSTALL_DIR_LINUX)
                    .installerTgDir(EM_INSTALLER_DIR_LINUX);
        } else {
            builder =
                (new EmRole.Builder(roleId, tasResolver)).installDir(EM_INSTALL_DIR_WINDOWS)
                    .installerTgDir(EM_INSTALLER_DIR_WINDOWS);
        }
        builder =
            builder.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
                .emWebPort(EM_WEBPORT).emPort(EM_PORT).version(version).dbuser(DB_USER)
                .dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                // .dbname(DB_NAME)
                // .dbhost()
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);
        EmRole collRole = builder.build();
        return collRole;
    }

    private EmRole getMomRole(String roleId, String version, Collection<EmRole> collectors,
        ITasResolver tasResolver) {
        EmRole.Builder builder;
        if (MOM_USE_LINUX) {
            builder =
                (new EmRole.LinuxBuilder(roleId, tasResolver)).installDir(EM_INSTALL_DIR_LINUX)
                    .installerTgDir(EM_INSTALLER_DIR_LINUX);
        } else {
            builder =
                (new EmRole.Builder(roleId, tasResolver)).installDir(EM_INSTALL_DIR_WINDOWS)
                    .installerTgDir(EM_INSTALLER_DIR_WINDOWS);
        }
        builder =
            builder
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "WebView", "Database"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                // .configProperty("introscope.enterprisemanager.clustering.mode", "StandAlone")
                .emWebPort(EM_WEBPORT).wvPort(WV_PORT).emPort(EM_PORT).version(version)
                .dbuser(DB_USER).dbpassword(DB_PASSWORD)
                .dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                // .dbname(DB_NAME)
                // .dbhost()
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);
        if (collectors != null) {
            builder = builder.emCollectors(collectors);
        }
        EmRole momRole = builder.build();
        return momRole;
    }

    private AGCRegisterRole getAgcRegisterRole(String roleId, String agcHost, String momHost,
        EmRole momRole, ITasResolver tasResolver) {
        AGCRegisterRole agcRegisterRole =
            (new AGCRegisterRole.Builder(roleId, tasResolver)).agcHostName(agcHost)
                .agcEmWvPort("" + EM_WEBPORT).agcWvPort("" + WV_PORT).hostName(momHost)
                .emWvPort("" + EM_WEBPORT).wvHostName(momHost).wvPort("" + WV_PORT)
                .startCommand(RunCommandFlow.class, momRole.getEmRunCommandFlowContext())
                .stopCommand(RunCommandFlow.class, momRole.getEmStopCommandFlowContext()).build();
        return agcRegisterRole;
    }

    private IRole getStartEmRole(EmRole emRole, boolean startEm, boolean startWv) {
        ExecutionRole.Builder builder = new ExecutionRole.Builder(emRole.getRoleId() + "_start");
        if (startEm) {
            builder.asyncCommand(emRole.getEmRunCommandFlowContext());
        }
        if (startWv) {
            builder.asyncCommand(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startEmRole = builder.build();
        return startEmRole;
    }

    private PhantomJSRole getPhantomJSRole(String roleId, ITasResolver tasResolver) {
        PhantomJSRole phantomJSRole = (new PhantomJSRole.Builder(roleId, tasResolver)).build();
        return phantomJSRole;
    }

    private TomcatRole getTomcatRole(String roleId, ITasResolver tasResolver) {
        TomcatRole tomcatRole =
            (new TomcatRole.Builder(roleId, tasResolver)).tomcatVersion(TomcatVersion.v60)
                .tomcatCatalinaPort(TOMCAT_CATALINA_PORT).jdkHomeDir(TOMCAT_JDK_HOME_DIR)
                .installDir(TOMCAT_INSTALL_DIR).build();
        return tomcatRole;
    }

    private AgentRole getAgentRole(String roleId, TomcatRole tomcatRole, EmRole emRole,
        ITasResolver tasResolver) {
        AgentRole agentRole =
            (new AgentRole.Builder(roleId, tasResolver)).webAppServer(tomcatRole)
                .platform(ArtifactPlatform.WINDOWS).emRole(emRole)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL).webAppAutoStart().build();
        return agentRole;
    }

    private TradeServiceAppRole getTradeServiceAppRole(String roleId, TomcatRole tomcatRole,
        ITasResolver tasResolver) {
        TradeServiceAppRole tradeServiceAppRole =
            (new TradeServiceAppRole.Builder(roleId, tasResolver)).tomcatRole(tomcatRole)
                .tradeServiceAppVersion(TRADE_SERVICE_APP_VERSION)
                .reportingServiceAppVersion(REPORTING_SERVICE_APP_VERSION)
                .orderEngineAppVersion(ORDER_ENGINE_APP_VERSION).build();
        return tradeServiceAppRole;
    }

    private SeleniumGridNodeRole getSeleniumGridNodeRole(String roleId, String hubHost,
        ITasResolver tasResolver) {
        SeleniumGridNodeRole seleniumGridNodeRole =
            (new SeleniumGridNodeRole.Builder(roleId, tasResolver))
                .nodeConfiguration(getSeleniumGridNodeConfiguration(hubHost, tasResolver))
                .qResXResolution(SELENIUM_GRID_NODE_qResXResolution)
                .qResYResolution(SELENIUM_GRID_NODE_qResYResolution)
                .chromeDriver(SELENIUM_CHROME_DRIVER_VERSION).build();
        return seleniumGridNodeRole;
    }

    private SeleniumGridHubRole getSeleniumGridHubRole(String roleId,
        SeleniumGridNodeRole seleniumGridNodeRole, ITasResolver tasResolver) {
        SeleniumGridHubRole seleniumGridHubRole =
            (new SeleniumGridHubRole.Builder(roleId, tasResolver))
                .addNodeRole(seleniumGridNodeRole).build();
        return seleniumGridHubRole;
    }

    private NodeConfiguration getSeleniumGridNodeConfiguration(String seleniumGridHubHost,
        ITasResolver tasResolver) {
        List<NodeCapability> capabilities = new ArrayList<NodeCapability>();
        NodeCapability chromeCapability =
            (new NodeCapability.Builder()).browserType(BrowserType.CHROME)
                .platform(NodePlatform.WINDOWS).maxInstances(8).build();
        capabilities.add(chromeCapability);
        URL hubUrl;
        try {
            hubUrl =
                new URL("http", seleniumGridHubHost, SELENIUM_GRID_HUB_PORT, "/grid/register/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        NodeConfiguration nodeConfiguration =
            (new NodeConfiguration.Builder()).hub(hubUrl)
                .maxSession(SELENIUM_GRID_NODE_CONFIGURATION_maxSession)
                .addCapabilities(capabilities).build();
        return nodeConfiguration;
    }

    private JavaRole addJavaRoleToMachine(String roleId, ITasResolver tasResolver) {
        String javaDir =
            JAVA_INSTALL_DIR_WINDOWS + JAVA_VERSION.getJavaRuntime().name().toLowerCase()
                + JAVA_VERSION.getArtifact().getVersion();
        JavaRole javaRole =
            (new JavaRole.Builder(roleId, tasResolver)).dir(javaDir).version(JAVA_VERSION).build();
        return javaRole;
    }

    private UtilityRole<FileModifierFlowContext> getSleepConfigFileRole(String roleId) {
        FileModifierFlowContext sleepConfigFileFlowContext =
            (new FileModifierFlowContext.Builder()).create(SLEEP_CONFIG_FILE,
                SLEEP_CONFIG_FILE_CONTENT).build();
        UtilityRole<FileModifierFlowContext> sleepConfigFileRole =
            UtilityRole.flow(roleId, FileModifierFlow.class, sleepConfigFileFlowContext);
        return sleepConfigFileRole;
    }

}
