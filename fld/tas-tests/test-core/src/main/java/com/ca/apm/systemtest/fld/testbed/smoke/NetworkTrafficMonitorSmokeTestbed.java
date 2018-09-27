package com.ca.apm.systemtest.fld.testbed.smoke;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.NetworkTrafficMonitorTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class NetworkTrafficMonitorSmokeTestbed
    implements
        ITestbedFactory,
        FLDLoadConstants,
        FLDConstants {

    public static final String MOM_MACHINE_ID = "momMachine";
    private static final String COLL01_MACHINE_ID = "coll01Machine";

    private static final String[] COLL_MACHINES = {COLL01_MACHINE_ID};
    private static final String[] NETWORK_TRAFFIC_MONITOR_MACHINE_IDS = {MOM_MACHINE_ID};

    private static final String EM_MOM_ROLE_ID = "emMomRole";
    private static final String EM_COLL01_ROLE_ID = "emColl01Role";
    private static final String[] EM_COLL_ROLES = {EM_COLL01_ROLE_ID};

    private static final String FLD_LINUX_TMPL_ID = "co65";

    private static final String DEFAULT_EM_VERSION = "99.99.aquarius-SNAPSHOT"; // "99.99.sys-SNAPSHOT";
                                                                                // "10.2.0.13";
                                                                                // "99.99.metadata";
                                                                                // "10.2.0-SNAPSHOT";

    private static final String INSTALL_DIR = "/home/sw/em/Introscope";
    private static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    private static final String DATABASE_DIR = "/data/em/database";
    private static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";

    private static final String DB_PASSWORD = "password";
    private static final String DB_ADMIN_PASSWORD = "password123";
    private static final String DB_USERNAME = "cemadmin";
    private static final String DB_ADMIN_USERNAME = "postgres";

    private static final int WVPORT2 = 8082;
    private static final int EMWEBPORT = 8081;

    private static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private static final String GC_LOG_WV_FILE = INSTALL_DIR + "/logs/gclog_wv.txt";

    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2048m",
        "-Xms2048m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
        "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-Xms2048m", "-Xms2048m", "-verbose:gc",
        "-Xloggc:" + GC_LOG_FILE);

    private static final Collection<String> WV_LAXNL_JAVA_OPTION =
        Arrays
            .asList(
                "-Djava.awt.headless=true",
                "-Dorg.owasp.esapi.resources=./config/esapi",
                "-Dsun.java2d.noddraw=true",
                "-javaagent:./product/webview/agent/wily/Agent.jar",
                "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1024m", "-Xmx1024m",
                "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
                "-Xloggc:" + GC_LOG_WV_FILE);

    private String emVersion = DEFAULT_EM_VERSION;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed(getClass().getSimpleName());

        // MOM machine
        ITestbedMachine momMachine =
            new TestbedMachine.LinuxBuilder(MOM_MACHINE_ID).templateId(FLD_LINUX_TMPL_ID)
                .bitness(Bitness.b64).build();

        EmRole.LinuxBuilder momBuilder = new EmRole.LinuxBuilder(EM_MOM_ROLE_ID, tasResolver);

        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);

        // Collectors machines
        for (int i = 0; i < EM_COLL_ROLES.length; i++) {
            String collMachineName = COLL_MACHINES[i];
            ITestbedMachine collectorMachine =
                new TestbedMachine.LinuxBuilder(collMachineName).templateId(FLD_LINUX_TMPL_ID)
                    .bitness(Bitness.b64).build();

            EmRole.LinuxBuilder collBuilder =
                new EmRole.LinuxBuilder(EM_COLL_ROLES[i], tasResolver);

            collBuilder
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
                .dbpassword(DB_PASSWORD).dbAdminPassword(DB_ADMIN_PASSWORD).dbuser(DB_USERNAME)
                .dbhost(emHost).version(emVersion).installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR);

            collBuilder.emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);

            EmRole collectorRole = collBuilder.build();

            collectorMachine.addRole(collectorRole);

            // setup logging in config/IntroscopeEnterpriseManager.properties
            IRole loggingRole =
                addLoggingSetupRole(collectorMachine, collectorRole, tasResolver, 100);

            // start COLLECTOR
            addStartEmRole(collectorMachine, collectorRole, false, true, loggingRole);

            momBuilder.emCollector(collectorRole);
            testbed.addMachine(collectorMachine);
        }

        // MOM role settings
        momBuilder
            .silentInstallChosenFeatures(
                Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "Database", "WebView"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).nostartEM().nostartWV()
            .dbuser(DB_USERNAME).dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USERNAME)
            .dbAdminPassword(DB_ADMIN_PASSWORD).dbhost(emHost).emWebPort(EMWEBPORT).wvPort(WVPORT2)
            .version(emVersion).installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
            .databaseDir(DATABASE_DIR).emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);

        EmRole momRole = momBuilder.build();
        momMachine.addRole(momRole);

        // setup webview agent on WV
        Map<String, String> propsMap = new HashMap<String, String>();
        propsMap.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", emHost);
        propsMap.put("agentManager.url.1", emHost + ":" + momRole.getEmPort());

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(
                INSTALL_DIR + "/product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                propsMap).build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(EM_MOM_ROLE_ID + "_setupWVAgentProfile", tasResolver)
                .runFlow(ConfigureFlow.class, ctx).build();

        setWVAgent.after(momRole);
        momMachine.addRole(setWVAgent);

        // setup logging in config/IntroscopeEnterpriseManager.properties
        IRole loggingRole = addLoggingSetupRole(momMachine, setWVAgent, tasResolver, 100);

        // start MOM + Webview
        addStartEmRole(momMachine, momRole, true, true, loggingRole);

        testbed.addMachine(momMachine);

        FldTestbedProvider networkTrafficMonitorTestbedProvider =
            new NetworkTrafficMonitorTestbedProvider(NETWORK_TRAFFIC_MONITOR_MACHINE_IDS);
        testbed.addMachines(networkTrafficMonitorTestbedProvider.initMachines());
        networkTrafficMonitorTestbedProvider.initTestbed(testbed, tasResolver);

        return testbed;
    }

    private static IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWV,
        boolean startEM, IRole beforeRole) {
        // starts EM or WebView
        ExecutionRole.Builder builder = new ExecutionRole.Builder(emRole.getRoleId() + "_start");

        if (startWV) {
            builder.asyncCommand(emRole.getWvRunCommandFlowContext());
        }
        if (startEM) {
            builder.asyncCommand(emRole.getEmRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        startRole.after(beforeRole);
        machine.addRole(startRole);

        return startRole;
    }

    private static IRole addLoggingSetupRole(ITestbedMachine machine, IRole afterRole,
        ITasResolver tasResolver, Integer backupIndex) {
        Map<String, String> propsMap = new HashMap<String, String>();

        if (backupIndex == null) backupIndex = 4; // set default value

        propsMap.put("log4j.appender.logfile.MaxBackupIndex", backupIndex.toString());
        propsMap.put("log4j.logger.Manager",
            "VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile");

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(
                INSTALL_DIR + "/config/IntroscopeEnterpriseManager.properties", propsMap).build();

        UniversalRole configureLogSetup =
            new UniversalRole.Builder("configLogSetup_" + machine.getMachineId(), tasResolver)
                .runFlow(ConfigureFlow.class, ctx).build();
        configureLogSetup.after(afterRole);
        machine.addRole(configureLogSetup);

        return configureLogSetup;
    }

}
