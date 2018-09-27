package com.ca.apm.systemtest.sizingguidetest.testbed;

import static com.ca.apm.systemtest.sizingguidetest.role.FakeEulaRole.getFakeEulaArtifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.ImportDomainConfigFlowContext;
import com.ca.apm.systemtest.fld.artifact.TessTestArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FldDomainConfigArtifact;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.PreferredBrowser;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.TessService;
import com.ca.apm.systemtest.fld.role.CEMTessLoadRole;
import com.ca.apm.systemtest.fld.role.ConfigureTimRole;
import com.ca.apm.systemtest.fld.role.PortForwardingRole;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.sizingguidetest.role.CustomEmRole;
import com.ca.apm.systemtest.sizingguidetest.role.FakeEulaRole;
import com.ca.apm.systemtest.sizingguidetest.role.MetricSynthRole;
import com.ca.apm.systemtest.sizingguidetest.role.PrepareConfigimportRole;
import com.ca.apm.systemtest.sizingguidetest.role.TypeperfRole;
import com.ca.apm.systemtest.sizingguidetest.testbed.regional.Configuration;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.ImportDomainConfigRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class SizingGuideTestbedProvider implements FldTestbedProvider, Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(SizingGuideTestbedProvider.class);

    private static final String MACHINE_TEMPLATE_ID_W64_16GB = "w64_16gb";
    private static final String MACHINE_TEMPLATE_ID_CO65_16GB = "co65_16gb";
    private static final String MACHINE_TEMPLATE_ID_CO65_TIM = "co65_tim";

    private static final String EM_INSTALL_DIR = "C:\\automation\\deployed\\em";
    protected static final String GC_LOG_FILE = EM_INSTALL_DIR + "\\logs\\gclog.txt";
    private static final String TIM_INSTALL_DIR = "/opt/tim";
    private static final String JAVA_INSTALL_DIR = "c:\\java\\jdk1.8";
    private static final String IMPORTDOMAINCONFIG_EM_INSTALL_DIR = "/opt/automation/deployed/em";
    private static final String DATABASE_DIR = "/opt/automation/deployed/database";
    private static final String JAVA_DBMACHINE_DIR = "/opt/java8";
    private static final String RESULTS_LOC = "c:\\sw\\results\\typeperf\\";

    private static final String DB_USER = "cemadmin";
    private static final String DB_PASSWORD = "FrankfurtskaP0levka";
    private static final String DB_ADMIN_USER = "postgres";
    private static final String DB_ADMIN_PASSWORD = "OoohLaLa1234";

    private static final String CEMDB = "cemdb";
    private static final int DB_PORT = 5432;

    private static final Collection<String> EM_LAX_OPTIONS = Arrays.asList("-Xms1g", "-Xmx12g",
        "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc", "-Dcom.wily.assert=false",
        "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseParNewGC", "-XX:CMSInitiatingOccupancyFraction=50",
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xss256k", "-Xloggc:" + GC_LOG_FILE
    // , "-XX:PermSize=256m", "-XX:MaxPermSize=512m"
        );

    private static final Collection<String> WV_LAX_OPTIONS = Arrays.asList("-Xms1g", "-Xmx8g",
        "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc", "-Dcom.wily.assert=false",
        "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseParNewGC", "-XX:CMSInitiatingOccupancyFraction=50",
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xss256k", "-Xloggc:" + GC_LOG_FILE
    // , "-XX:PermSize=256m", "-XX:MaxPermSize=512m"
        );

    private static final Collection<String> TOMCAT01_OPTIONS = Arrays.asList("-Xms256m",
        "-Xmx512m", "-XX:PermSize=256m", "-XX:MaxPermSize=512m", "-server",
        "-Dcom.wily.introscope.agent.agentName=Tomcat01");

    private static final Collection<String> TOMCAT02_OPTIONS = Arrays.asList("-Xms256m",
        "-Xmx512m", "-XX:PermSize=256m", "-XX:MaxPermSize=512m", "-server",
        "-Dcom.wily.introscope.agent.agentName=Tomcat02");

    private static final int TOMCAT_APP_SERVER_PORT = 8080;
    private static final int CEM_TESS_LOAD_FRW_PORT = 8011;

    private static final Artifact FAKE_EULA_ARTIFACT = getFakeEulaArtifact();

    private Configuration configuration;
    private String emVersion;
    private String domainConfigVersion;
    private String dbTargetReleaseVersion;
    private String smtpHost;
    private String reportEmail;

    private ITestbedMachine dbMachine;
    private ITestbedMachine emMachine;
    private ITestbedMachine wvMachine;
    private ITestbedMachine tim01Machine;
    private ITestbedMachine tim02Machine;
    private ITestbedMachine tomcat01Machine;
    private ITestbedMachine tomcat02Machine;
    private ITestbedMachine testMachine;

    private boolean usingOld97version = false;

    public SizingGuideTestbedProvider(Configuration configuration) {
        this.configuration = configuration;
        initConfig();
    }

    private void initConfig() {
        emVersion = configuration.getTestbedEmVersion();
        domainConfigVersion = configuration.getTestbedDomainConfigVersion();
        dbTargetReleaseVersion = configuration.getTestbedDbTargetReleaseVersion();
        smtpHost = configuration.getTestbedTessSmtpHost();
        reportEmail = configuration.getTestbedReportEmail();
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        LOGGER.info("XXXXXXXXXX Loaded configuration:");
        LOGGER.info("XXXXXXXXXX emVersion              = {}", emVersion);
        LOGGER.info("XXXXXXXXXX domainConfigVersion    = {}", domainConfigVersion);
        LOGGER.info("XXXXXXXXXX dbTargetReleaseVersion = {}", dbTargetReleaseVersion);
        LOGGER.info("XXXXXXXXXX smtpHost               = {}", smtpHost);
        LOGGER.info("XXXXXXXXXX reportEmail            = {}", reportEmail);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

    private String getVersion(ITasResolver tasResolver) {
        String version = emVersion == null ? tasResolver.getDefaultVersion() : emVersion;
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        LOGGER.info("XXXXXXXXXX Using version {}", version);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        return version;
    }

    protected String[] getMemoryMonitorMachineIds() {
        return new String[] {EM_MACHINE_ID, WV_MACHINE_ID};
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        dbMachine =
            (new TestbedMachine.LinuxBuilder(DB_MACHINE_ID))
                .templateId(MACHINE_TEMPLATE_ID_CO65_16GB).platform(Platform.CENTOS)
                .bitness(Bitness.b64).build();

        tim01Machine =
            (new TestbedMachine.LinuxBuilder(TIM01_MACHINE_ID))
                .templateId(MACHINE_TEMPLATE_ID_CO65_TIM).platform(Platform.CENTOS)
                .bitness(Bitness.b64).build();

        tim02Machine =
            (new TestbedMachine.LinuxBuilder(TIM02_MACHINE_ID))
                .templateId(MACHINE_TEMPLATE_ID_CO65_TIM).platform(Platform.CENTOS)
                .bitness(Bitness.b64).build();

        emMachine =
            (new TestbedMachine.Builder(EM_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64_16GB)
                .platform(Platform.WINDOWS).bitness(Bitness.b64).build();

        wvMachine =
            (new TestbedMachine.Builder(WV_MACHINE_ID)).templateId(MACHINE_TEMPLATE_ID_W64_16GB)
                .platform(Platform.WINDOWS).bitness(Bitness.b64).build();

        tomcat01Machine =
            (new TestbedMachine.Builder(TOMCAT01_MACHINE_ID))
                .templateId(ITestbedMachine.TEMPLATE_W64).platform(Platform.WINDOWS)
                .bitness(Bitness.b64).build();

        tomcat02Machine =
            (new TestbedMachine.Builder(TOMCAT02_MACHINE_ID))
                .templateId(ITestbedMachine.TEMPLATE_W64).platform(Platform.WINDOWS)
                .bitness(Bitness.b64).build();

        testMachine =
            (new TestbedMachine.Builder(TEST_MACHINE_ID)).templateId(ITestbedMachine.TEMPLATE_W64)
                .platform(Platform.WINDOWS).bitness(Bitness.b64).build();

        Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(dbMachine);
        machines.add(tim01Machine);
        machines.add(tim02Machine);
        machines.add(emMachine);
        machines.add(wvMachine);
        machines.add(tomcat01Machine);
        machines.add(tomcat02Machine);
        machines.add(testMachine);
        return machines;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String version = getVersion(tasResolver);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        LOGGER.info("XXXXXXXXXX usingOld97version = {}", usingOld97version);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");


        // testbed.addMachine(dbMachine, tim01Machine, tim02Machine, emMachine, wvMachine,
        // tomcat01Machine, tomcat02Machine, testMachine);


        YumInstallPackageRole edYumInstallRole =
            new YumInstallPackageRole.Builder("edYumInstallRole").addPackage("ed")
                .addPackage("unzip").build();
        dbMachine.addRole(edYumInstallRole);

        FakeEulaRole eulaDbMachineRole = null;
        if (usingOld97version) {
            eulaDbMachineRole =
                (new FakeEulaRole.LinuxBuilder("eulaDbMachineRole", tasResolver)).build();
            dbMachine.addRole(eulaDbMachineRole);
        }

        // DB
        EmRole.Builder dbRoleBuilder =
            usingOld97version
                ? (new CustomEmRole.CustomEmRoleLinuxBuilder(DB_ROLE, tasResolver))
                : (new EmRole.LinuxBuilder(DB_ROLE, tasResolver));
        dbRoleBuilder =
            dbRoleBuilder.silentInstallChosenFeatures(Arrays.asList("Database"))
                .databaseDir(DATABASE_DIR).nostartEM().nostartWV().dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD).dbuser(DB_USER).dbpassword(DB_PASSWORD)
                .version(version);
        if (usingOld97version) {
            dbRoleBuilder = dbRoleBuilder.eulaArtifact(FAKE_EULA_ARTIFACT); // eula zip will be
                                                                            // downloaded but not
                                                                            // used for installation
        }
        EmRole dbRole = dbRoleBuilder.build();
        dbRole.after(edYumInstallRole);
        if (usingOld97version) {
            dbRole.after(eulaDbMachineRole);
        }
        dbMachine.addRole(dbRole);
        String dbHost = tasResolver.getHostnameById(DB_ROLE);

        // install FLD domain config export
        JavaRole javaDbMachineRole =
            (new JavaRole.Builder(JAVA_DBMACHINE_ROLE, tasResolver)).dir(JAVA_DBMACHINE_DIR)
                .version(JavaBinary.LINUX_64BIT_JRE_18).build();
        javaDbMachineRole.after(dbRole);
        dbMachine.addRole(javaDbMachineRole);

        PrepareConfigimportRole prepareConfigimportRole = null;
        if (usingOld97version) {
            prepareConfigimportRole =
                (new PrepareConfigimportRole.Builder("prepareConfigimportRole", tasResolver))
                    .build();
            prepareConfigimportRole.after(dbRole);
            dbMachine.addRole(prepareConfigimportRole);
        }

        ITasArtifact domainConfigArtifact =
            (new FldDomainConfigArtifact()).createArtifact(domainConfigVersion);
        ImportDomainConfigRole importDomainConfigRole =
            (new ImportDomainConfigRole.Builder(IMPORT_DOMAIN_CONFIG_ROLE, tasResolver))
                .dbHost(dbHost).dbName(CEMDB).dbPort(DB_PORT)
                .dbType(ImportDomainConfigFlowContext.DbType.PostgreSql).dbUser(DB_USER)
                .dbPassword(DB_PASSWORD).emDir(IMPORTDOMAINCONFIG_EM_INSTALL_DIR)
                .dbServiceUser(DB_ADMIN_USER).dbServicePwd(DB_ADMIN_PASSWORD)
                .dbInstallDir(DATABASE_DIR).targetRelease(dbTargetReleaseVersion)
                .importFile(domainConfigArtifact).javaRole(javaDbMachineRole)
                .runtimeEnvProperties(Collections.singletonMap("JAVA_HOME", JAVA_DBMACHINE_DIR))
                .build();
        // importDomainConfigRole.after(dbRole, javaDbMachineRole);
        if (usingOld97version) {
            importDomainConfigRole.after(prepareConfigimportRole);
        }
        importDomainConfigRole.after(Arrays.asList(dbMachine.getRoles()));
        dbMachine.addRole(importDomainConfigRole);



        // TIM 1
        TIMRole tim01Role =
            (new TIMRole.Builder(TIM01_ROLE, tasResolver)).timVersion(version)
                .installDir(TIM_INSTALL_DIR).build();
        tim01Role.after(new HashSet<>(Arrays.asList(dbMachine.getRoles())));
        tim01Machine.addRole(tim01Role);
        String tim01Host = tasResolver.getHostnameById(TIM01_ROLE);



        // TIM 2
        TIMRole tim02Role =
            (new TIMRole.Builder(TIM02_ROLE, tasResolver)).timVersion(version)
                .installDir(TIM_INSTALL_DIR).build();
        tim02Role.after(new HashSet<>(Arrays.asList(dbMachine.getRoles())));
        tim02Machine.addRole(tim02Role);
        String tim02Host = tasResolver.getHostnameById(TIM02_ROLE);



        FakeEulaRole eulaEmMachineRole = null;
        if (usingOld97version) {
            eulaEmMachineRole =
                (new FakeEulaRole.Builder("eulaEmMachineRole", tasResolver)).build();
            emMachine.addRole(eulaEmMachineRole);
        }

        // EM
        EmRole.Builder emRoleBuilder =
            usingOld97version ? (new CustomEmRole.CustomEmRoleBuilder(EM_ROLE, tasResolver))
            // .eulaUrl(FakeEulaRole.Builder.DEFAULT_WINDOWS_EULA_URL)
                : (new EmRole.Builder(EM_ROLE, tasResolver));
        emRoleBuilder =
            emRoleBuilder
                // .installDir(EM_INSTALL_DIR)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager"))
                .emLaxNlClearJavaOption(EM_LAX_OPTIONS).nostartEM().nostartWV().dbhost(dbHost)
                .dbuser(DB_USER).dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD).tim(tim01Role).tim(tim02Role).version(version);
        if (usingOld97version) {
            emRoleBuilder = emRoleBuilder.eulaArtifact(FAKE_EULA_ARTIFACT); // eula zip will be
                                                                            // downloaded but not
                                                                            // used for installation
        }
        EmRole emRole = emRoleBuilder.build();
        if (usingOld97version) {
            emRole.after(eulaEmMachineRole);
        }
        emRole.after(new HashSet<IRole>(Arrays.asList(dbMachine.getRoles())));
        emRole.after(new HashSet<IRole>(Arrays.asList(tim01Machine.getRoles())));
        emRole.after(new HashSet<IRole>(Arrays.asList(tim02Machine.getRoles())));
        emMachine.addRole(emRole);
        String emHost = tasResolver.getHostnameById(EM_ROLE);
        // start EM
        ExecutionRole startEmRole =
            (new ExecutionRole.Builder(emRole.getRoleId() + "_start")).asyncCommand(
                emRole.getEmRunCommandFlowContext()).build();
        startEmRole.after(emRole);
        emMachine.addRole(startEmRole);



        FakeEulaRole eulaWvMachineRole = null;
        if (usingOld97version) {
            eulaWvMachineRole =
                (new FakeEulaRole.Builder("eulaWvMachineRole", tasResolver)).build();
            wvMachine.addRole(eulaWvMachineRole);
        }

        // WV
        EmRole.Builder wvRoleBuilder =
            usingOld97version ? (new CustomEmRole.CustomEmRoleBuilder(WV_ROLE, tasResolver))
            // .eulaUrl(FakeEulaRole.Builder.DEFAULT_WINDOWS_EULA_URL)
                : (new EmRole.Builder(WV_ROLE, tasResolver));
        wvRoleBuilder =
            wvRoleBuilder
                // .installDir(EM_INSTALL_DIR)
                .silentInstallChosenFeatures(Arrays.asList("WebView"))
                .wvLaxNlClearJavaOption(WV_LAX_OPTIONS).nostartEM().nostartWV().wvEmHost(emHost)
                .version(version);
        if (usingOld97version) {
            wvRoleBuilder = wvRoleBuilder.eulaArtifact(FAKE_EULA_ARTIFACT); // eula zip will be
                                                                            // downloaded but not
                                                                            // used for installation
        }
        EmRole wvRole = wvRoleBuilder.build();
        if (usingOld97version) {
            wvRole.after(eulaWvMachineRole);
        }
        wvRole.after(new HashSet<IRole>(Arrays.asList(emMachine.getRoles())));
        wvMachine.addRole(wvRole);
        // start WV
        ExecutionRole startWvRole =
            (new ExecutionRole.Builder(wvRole.getRoleId() + "_start")).asyncCommand(
                wvRole.getWvRunCommandFlowContext()).build();
        startWvRole.after(wvRole, startEmRole);
        wvMachine.addRole(startWvRole);



        // Tomcat 1
        JavaRole java01Role =
            (new JavaRole.Builder("java01Role", tasResolver)).dir(JAVA_INSTALL_DIR)
                .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();
        WebAppRole<TomcatRole> tessTest01Role =
            (new WebAppRole.Builder<TomcatRole>("tessTest01Role"))
                .artifact(new TessTestArtifact(tasResolver).createArtifact()).cargoDeploy()
                .contextName("tesstest").build();
        TomcatRole tomcat01Role =
            (new TomcatRole.Builder(TOMCAT01_ROLE, tasResolver)).customJava(java01Role)
                .additionalVMOptions(TOMCAT01_OPTIONS).webApp(tessTest01Role)
                .tomcatVersion(TomcatVersion.v80).autoStart()
                .tomcatCatalinaPort(TOMCAT_APP_SERVER_PORT).build();
        tomcat01Role.after(java01Role);
        tomcat01Role.after(new HashSet<IRole>(Arrays.asList(emMachine.getRoles())));
        tomcat01Role.after(new HashSet<IRole>(Arrays.asList(wvMachine.getRoles())));
        AgentRole tomcatAgent01Role =
            (new AgentRole.Builder("tomcatAgent01Role", tasResolver)).webAppServer(tomcat01Role)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(emRole)
                .customName("Tomcat01").version(version).build();
        tomcatAgent01Role.after(tomcat01Role);
        tomcat01Machine.addRole(java01Role, tessTest01Role, tomcat01Role, tomcatAgent01Role);
        String tomcat01Host = tasResolver.getHostnameById(TOMCAT01_ROLE);



        // Tomcat 2
        JavaRole java02Role =
            (new JavaRole.Builder("java02Role", tasResolver)).dir(JAVA_INSTALL_DIR)
                .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();
        WebAppRole<TomcatRole> tessTest02Role =
            (new WebAppRole.Builder<TomcatRole>("tessTest02Role"))
                .artifact(new TessTestArtifact(tasResolver).createArtifact()).cargoDeploy()
                .contextName("tesstest").build();
        TomcatRole tomcat02Role =
            (new TomcatRole.Builder(TOMCAT02_ROLE, tasResolver)).customJava(java02Role)
                .additionalVMOptions(TOMCAT02_OPTIONS).webApp(tessTest02Role)
                .tomcatVersion(TomcatVersion.v80).autoStart()
                .tomcatCatalinaPort(TOMCAT_APP_SERVER_PORT).build();
        tomcat02Role.after(java02Role);
        tomcat02Role.after(new HashSet<IRole>(Arrays.asList(emMachine.getRoles())));
        tomcat02Role.after(new HashSet<IRole>(Arrays.asList(wvMachine.getRoles())));
        AgentRole tomcatAgent02Role =
            (new AgentRole.Builder("tomcatAgent02Role", tasResolver)).webAppServer(tomcat02Role)
                .intrumentationLevel(AgentInstrumentationLevel.FULL).emRole(emRole)
                .customName("Tomcat02").version(version).build();
        tomcatAgent02Role.after(tomcat02Role);
        tomcat02Machine.addRole(java02Role, tessTest02Role, tomcat02Role, tomcatAgent02Role);
        String tomcat02Host = tasResolver.getHostnameById(TOMCAT02_ROLE);



        // configure Tess
        ConfigureTessRole configTessRole =
            (new ConfigureTessRole.Builder("configTessRole", tasResolver))
                .removeOldWebServerFilters().removeOldTims().mom(emRole).tim(tim01Role)
                .tim(tim02Role).tessService(TessService.DbCleanup, emRole)
                .tessService(TessService.TimCollection, emRole)
                .tessService(TessService.StatsAggregation, emRole)
                .preferredBrowser(PreferredBrowser.Firefox).autostart().reportEmail(reportEmail)
                .smtpHost(smtpHost)
                .webServerFilter(TOMCAT01_ROLE, tim01Role, tomcat01Role, TOMCAT_APP_SERVER_PORT)
                .webServerFilter(TOMCAT02_ROLE, tim02Role, tomcat02Role, TOMCAT_APP_SERVER_PORT)
                .build();
        configTessRole.after(new HashSet<IRole>(Arrays.asList(tim01Machine.getRoles())));
        configTessRole.after(new HashSet<IRole>(Arrays.asList(tim02Machine.getRoles())));
        configTessRole.after(new HashSet<IRole>(Arrays.asList(emMachine.getRoles())));
        testMachine.addRole(configTessRole);



        // configure TIM 1
        ConfigureTimRole configureTim01Role =
            (new ConfigureTimRole.Builder(TIM01_ROLE_CONFIGURE_ETH02)).timHostname(tim01Host)
                .requiredInterface("eth2").disallowedInterface("eth0")
                .additionalProperty("MaxFlexRequestBodySize", "100000")
                .additionalProperty("MaxFlexResponseBodySize", "100000").build();
        configureTim01Role.after(configTessRole);
        testMachine.addRole(configureTim01Role);



        // configure TIM 2
        ConfigureTimRole configureTim02Role =
            (new ConfigureTimRole.Builder(TIM02_ROLE_CONFIGURE_ETH02)).timHostname(tim02Host)
                .requiredInterface("eth2").disallowedInterface("eth0")
                .additionalProperty("MaxFlexRequestBodySize", "100000")
                .additionalProperty("MaxFlexResponseBodySize", "100000").build();
        configureTim02Role.after(configTessRole, configureTim01Role);
        testMachine.addRole(configureTim02Role);



        LOGGER.info("Configuring forwarding -> {}:{}", tomcat01Host, TOMCAT_APP_SERVER_PORT);
        PortForwardingRole pf01Role =
            (new PortForwardingRole.Builder(PORTFORWARD_CEM_TESS_LOAD01_ROLE_ID))
                .listenPort(CEM_TESS_LOAD_FRW_PORT).targetIpAddress(tomcat01Host)
                .targetPort(TOMCAT_APP_SERVER_PORT).workDir(PORTFORWARD_CEM_TESS_LOAD01_ROLE_ID)
                .build();
        pf01Role.after(new HashSet<IRole>(Arrays.asList(tim01Machine.getRoles())));
        tim01Machine.addRole(pf01Role);
        LOGGER
            .info(
                "Configuring port forwarding: tim={}, listen={}, targetAddr={}, targetPort={}, workDir={}",
                tim01Machine, CEM_TESS_LOAD_FRW_PORT, tomcat01Host, CEM_TESS_LOAD_FRW_PORT,
                PORTFORWARD_CEM_TESS_LOAD01_ROLE_ID);

        LOGGER.info("Configuring forwarding -> {}:{}", tomcat02Host, TOMCAT_APP_SERVER_PORT);
        PortForwardingRole pf02Role =
            (new PortForwardingRole.Builder(PORTFORWARD_CEM_TESS_LOAD02_ROLE_ID))
                .listenPort(CEM_TESS_LOAD_FRW_PORT).targetIpAddress(tomcat01Host)
                .targetPort(TOMCAT_APP_SERVER_PORT).workDir(PORTFORWARD_CEM_TESS_LOAD02_ROLE_ID)
                .build();
        pf02Role.after(new HashSet<IRole>(Arrays.asList(tim02Machine.getRoles())));
        tim02Machine.addRole(pf02Role);
        LOGGER
            .info(
                "Configuring port forwarding: tim={}, listen={}, targetAddr={}, targetPort={}, workDir={}",
                tim02Machine, CEM_TESS_LOAD_FRW_PORT, tomcat02Host, CEM_TESS_LOAD_FRW_PORT,
                PORTFORWARD_CEM_TESS_LOAD02_ROLE_ID);



        String tessAppUrl01 = getTessAppUrl(tim01Host);
        CEMTessLoadRole cemTessLoad01Role =
            (new CEMTessLoadRole.Builder(CEM_TESS_LOAD01_ROLE_ID, tasResolver))
                .setTestAppUrl(tessAppUrl01)// socat on tim
                .setDatabase(dbHost)// db host name
                .setDbUser(DB_ADMIN_USER)// db user
                .setDbPass(DB_ADMIN_PASSWORD)// db pass
                // .setDefects(15) // 15% of transactions are with defect
                .setSpeedRate(2) // higher number = longer sleep between iterations -> lower load
                .build();
        cemTessLoad01Role.after(new HashSet<IRole>(Arrays.asList(testMachine.getRoles())));
        testMachine.addRole(cemTessLoad01Role);



        String tessAppUrl02 = getTessAppUrl(tim02Host);
        CEMTessLoadRole cemTessLoad02Role =
            (new CEMTessLoadRole.Builder(CEM_TESS_LOAD02_ROLE_ID, tasResolver))
                .setTestAppUrl(tessAppUrl02)// socat on tim
                .setDatabase(dbHost)// db host name
                .setDbUser(DB_ADMIN_USER)// db user
                .setDbPass(DB_ADMIN_PASSWORD)// db pass
                // .setDefects(15) // 15% of transactions are with defect
                .setSpeedRate(2) // higher number = longer sleep between iterations -> lower load
                .build();
        cemTessLoad02Role.after(new HashSet<IRole>(Arrays.asList(testMachine.getRoles())));
        testMachine.addRole(cemTessLoad02Role);



        // MetricSynth
        MetricSynthRole metricSynthRole =
            (new MetricSynthRole.Builder(METRIC_SYNTH_ROLE, tasResolver)).collectorHost(emHost)
                .build();
        testMachine.addRole(metricSynthRole);

        // CPU monitoring
        TypeperfRole emMachineTypeperfRole =
            new TypeperfRole.Builder(EM_MACHINE_TYPEPERFROLE_ROLE, tasResolver)
                .metrics(new String[] {"\\Processor(_Total)\\% Processor Time"}).runTime(300L)
                .outputFileName(RESULTS_LOC + "typeperf.csv").build();
        emMachine.addRole(emMachineTypeperfRole);

        TypeperfRole wvMachineTypeperfRole =
            new TypeperfRole.Builder(WV_MACHINE_TYPEPERFROLE_ROLE, tasResolver)
                .metrics(new String[] {"\\Processor(_Total)\\% Processor Time"}).runTime(300L)
                .outputFileName(RESULTS_LOC + "typeperf.csv").build();
        wvMachine.addRole(wvMachineTypeperfRole);
    }

    private static String getTessAppUrl(String timHost) {
        return timHost + ":" + CEM_TESS_LOAD_FRW_PORT;
    }

    public void setUsingOld97version(boolean usingOld97version) {
        this.usingOld97version = usingOld97version;
    }

}
