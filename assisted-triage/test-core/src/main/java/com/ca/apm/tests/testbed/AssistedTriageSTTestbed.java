package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.systemtest.fld.artifact.FLDHvrAgentLoadExtractArtifact;
import com.ca.apm.systemtest.fld.role.AGCRegisterRole;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.tas.artifact.ITasArtifact;
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

/**
 * Assisted Traige System Test testbed.
 * 
 * @author banra06
 *
 */
@TestBedDefinition
public class AssistedTriageSTTestbed implements ITestbedFactory {

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLL_MACHINE_ID = "collMachine";
    public static final String COLL_ROLE_ID = "collRole";
    public static final String EM_ROLE_ID = "emRole";
    public static final String AGCDB_ROLE_ID = "agcdbRole";
    public static final String AGCDB_MACHINE_ID = "agcdbMachine";
    public static final String EM_MACHINE_ID = "emMachine";
    public static final String AGC_MACHINE_ID = "agcMachine";
    public static final String AGC_ROLE_ID = "agcRole";
    public static final String AGC_COLL_MACHINE_ID = "agcCollMachine";
    public static final String AGC_COLL_ROLE_ID = "agcCollRole";
    public static final String LOAD_MACHINE1_ID = "loadMachine1";
    public static final String LOAD_MACHINE2_ID = "loadMachine2";
    public static final String LOAD_MACHINE3_ID = "loadMachine3";
    public static final String LOAD_ROLE1_ID = "loadRole1";
    public static final String LOAD_ROLE2_ID = "loadRole2";
    public static final String LOAD_ROLE3_ID = "loadRole3";

    public static final String EM_TEMPLATE_ID = "co66";
    public static final String LOAD_TEMPLATE_ID = "w64";

    public static final String INSTALL_DIR = "/em/Introscope";
    public static final String INSTALL_TG_DIR = "/em/Installer";
    public static final String DATABASE_DIR = "/em/database";
    public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";
    public static final String INSTALL_TIM_DIR = "/opt";

    public static final String DB_PASSWORD = "password";
    public static final String DB_USERNAME = "cemadmin";
    public static final String DB_ADMIN_USERNAME = "postgres";

    public static final int WVPORT = 8084;
    // public static final int WVPORT2 = 8082;
    public static final int EMWEBPORT = 8081;

    public static final String ADMIN_AUX_TOKEN_HASHED =
        "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    public static final String LOG_MONITOR_CONFIG_JSON =
        "/com/ca/apm/systemtest/fld/testbed/devel/log-monitor-config.json";
    public static final String[] EMAILS_LOG = {"banra06@ca.com"};
    public static final String PID_FILE_KEY = "pidFile";
    private static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";
    public static final String LOG_MONITOR_LINUX_ROLE_ID = "logMonitorLinuxRoleId";

    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss256k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
        "-Xmx4096m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
        "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss256k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
        "-Xmx4096m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE);

    private static final Collection<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
        "-XX:+UseParNewGC", "-Xss256k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
        "-Xmx4096m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
        "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    public static final Collection<String> WV_LAXNL_JAVA_OPTION =
        Arrays
            .asList(
                "-Djava.awt.headless=true",
                "-Dorg.owasp.esapi.resources=./config/esapi",
                "-Dsun.java2d.noddraw=true",
                "-javaagent:./product/webview/agent/wily/Agent.jar",
                "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms2048m", "-Xmx2048m",
                "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
                "-Xloggc:" + GC_LOG_FILE);

    // machines IDs

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Testbed testbed = new Testbed("AssistedTriageSTTestbed");

        // MOM machine
        ITestbedMachine momMachine =
            new TestbedMachine.LinuxBuilder(MOM_MACHINE_ID).templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64).build();

        // MOM role
        EmRole.LinuxBuilder momBuilder = new EmRole.LinuxBuilder(MOM_ROLE_ID, tasResolver);

        // Collector machine
        ITestbedMachine collectorMachine =
            new TestbedMachine.LinuxBuilder(COLL_MACHINE_ID).templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64).build();
        // Collector
        EmRole collectorRole =
            new EmRole.LinuxBuilder(COLL_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                // .nostartEM()
                .nostartWV().dbpassword(DB_PASSWORD).dbAdminPassword(DB_PASSWORD)
                .dbuser(DB_USERNAME).dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
                .installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
                .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION).build();

        collectorMachine.addRole(collectorRole);

        // MOM
        momBuilder
            .silentInstallChosenFeatures(
                Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "Database", "WebView"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            // .nostartEM()
            // .nostartWV()
            .dbuser(DB_USERNAME).dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USERNAME)
            .dbAdminPassword(DB_PASSWORD).databaseDir(DATABASE_DIR)
            .emWebPort(EMWEBPORT).installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
            .wvPort(WVPORT).emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
            .emCollector(collectorRole);

        EmRole momRole = momBuilder.build();
        momMachine.addRole(momRole);

        // addStartEmRole(collectorMachine, collectorRole, false,
        // collectorRole);

        /*
         * Map<String, String> propsMap1 = new HashMap<String, String>();
         * propsMap1
         * .put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT"
         * ,tasResolver.getHostnameById(MOM_ROLE_ID));
         * propsMap1.put("agentManager.url.1",
         * tasResolver.getHostnameById(MOM_ROLE_ID)+":"+momRole.getEmPort());
         * 
         * ConfigureFlowContext ctx1 = new ConfigureFlowContext.Builder()
         * .configurationMap(FLDMainClusterTestbed.INSTALL_DIR+
         * "/product/webview/agent/wily/core/config/IntroscopeAgent.profile",
         * propsMap1) .build();
         * 
         * UniversalRole setWVAgent1 = new UniversalRole.Builder(MOM_ROLE_ID +
         * "_setupWVAgentProfile", tasResolver).runFlow(ConfigureFlow.class,
         * ctx1) .build();
         * 
         * setWVAgent1.after(momRole); momMachine.addRole(setWVAgent1);
         * 
         * //start MOM + Webview addStartEmRole(momMachine, momRole, true,
         * setWVAgent1);
         */
        testbed.addMachine(collectorMachine, momMachine);

        // StandAlone EM
        ITestbedMachine emMachine =
            new TestbedMachine.LinuxBuilder(EM_MACHINE_ID).templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64).build();

        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "Database",
                        "WebView"))
                // .nostartEM()
                // .nostartWV()
                .dbuser(DB_USERNAME).dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USERNAME)
                .dbAdminPassword(DB_PASSWORD).databaseDir(DATABASE_DIR)
                .emWebPort(EMWEBPORT).installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
                .wvPort(WVPORT).emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION).build();

        emMachine.addRole(emRole);

        /*
         * Map<String, String> propsMap2 = new HashMap<String, String>();
         * propsMap2
         * .put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT"
         * ,tasResolver.getHostnameById(EM_ROLE_ID));
         * propsMap2.put("agentManager.url.1",
         * tasResolver.getHostnameById(EM_ROLE_ID)+":"+momRole.getEmPort());
         * 
         * ConfigureFlowContext ctx2 = new ConfigureFlowContext.Builder()
         * .configurationMap(FLDMainClusterTestbed.INSTALL_DIR+
         * "/product/webview/agent/wily/core/config/IntroscopeAgent.profile",
         * propsMap2) .build();
         * 
         * UniversalRole setWVAgent2 = new UniversalRole.Builder(EM_ROLE_ID +
         * "_setupWVAgentProfile", tasResolver).runFlow(ConfigureFlow.class,
         * ctx2) .build();
         * 
         * setWVAgent2.after(emRole); emMachine.addRole(setWVAgent2);
         * 
         * //start EM + Webview addStartEmRole(emMachine, emRole, true,
         * setWVAgent2);
         */
        testbed.addMachine(emMachine);

        // DB Machine

        ITestbedMachine agcdatabaseMachine =
            new TestbedMachine.LinuxBuilder(AGCDB_MACHINE_ID).templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64).build();
        EmRole agcdatabaseRole =
            new EmRole.LinuxBuilder(AGCDB_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Database")).dbuser(DB_USERNAME)
                .dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USERNAME)
                .dbAdminPassword(DB_PASSWORD).nostartEM().nostartWV()
                .databaseDir(DATABASE_DIR).installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
                .build();

        agcdatabaseMachine.addRole(agcdatabaseRole);
        testbed.addMachine(agcdatabaseMachine);

        // AGC+MOM TestBed

        ITestbedMachine agcMachine =
            new TestbedMachine.LinuxBuilder(AGC_MACHINE_ID).templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64).build();
        EmRole.LinuxBuilder agcBuilder = new EmRole.LinuxBuilder(AGC_ROLE_ID, tasResolver);

        String emHost = tasResolver.getHostnameById(AGC_ROLE_ID);

        // Collectors machine
        ITestbedMachine agccollectorMachine =
            new TestbedMachine.LinuxBuilder(AGC_COLL_MACHINE_ID).templateId(EM_TEMPLATE_ID)
                .bitness(Bitness.b64).build();
        EmRole.LinuxBuilder collBuilder = new EmRole.LinuxBuilder(AGC_COLL_ROLE_ID, tasResolver);
        collBuilder
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
            // .nostartEM()
            .nostartWV().dbpassword(DB_PASSWORD).dbAdminPassword(DB_PASSWORD).dbuser(DB_USERNAME)
            .dbhost(tasResolver.getHostnameById(AGCDB_ROLE_ID))
            .installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
            .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);

        EmRole agccollectorRole = collBuilder.build();

        agccollectorMachine.addRole(agccollectorRole);

        // start COLLECTOR
        // addStartEmRole(agccollectorMachine, agccollectorRole, false,
        // agccollectorRole);

        agcBuilder.emCollector(agccollectorRole);
        testbed.addMachine(agccollectorMachine);

        // AGC role settings
        agcBuilder
            .silentInstallChosenFeatures(
                Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "WebView"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).nostartEM().nostartWV()
            .dbuser(DB_USERNAME).dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USERNAME)
            .dbAdminPassword(DB_PASSWORD).dbhost(tasResolver.getHostnameById(AGCDB_ROLE_ID))
            .emWebPort(EMWEBPORT).wvPort(WVPORT).installDir(INSTALL_DIR)
            .installerTgDir(INSTALL_TG_DIR).databaseDir(DATABASE_DIR)
            .configProperty("introscope.apmserver.teamcenter.master", "true")
            .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
            .configProperty("log4j.logger.Manager.AT", "INFO,console,logfile")
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);

        EmRole agcRole = agcBuilder.build();
        agcMachine.addRole(agcRole);

        // setup webview agent on WV
        Map<String, String> propsMap = new HashMap<String, String>();
        propsMap.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", emHost);
        propsMap.put("agentManager.url.1", emHost + ":" + agcRole.getEmPort());

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(
                INSTALL_DIR + "/product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                propsMap).build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(AGC_ROLE_ID + "_setupWVAgentProfile", tasResolver).runFlow(
                ConfigureFlow.class, ctx).build();

        setWVAgent.after(agcRole);
        agcMachine.addRole(setWVAgent);

        // start AGC + Webview
        IRole lastRole = addStartEmRole(agcMachine, agcRole, true, setWVAgent);

        // register MOM to AGC
        EmRole momRole1 = (EmRole) testbed.getRoleById(MOM_ROLE_ID);
        AGCRegisterRole agcRegister =
            new AGCRegisterRole.Builder("agcMOMRegister", tasResolver).agcHostName(emHost)
                .agcEmWvPort(new Integer(EMWEBPORT).toString())
                .agcWvPort(new Integer(WVPORT).toString())
                .hostName(tasResolver.getHostnameById(MOM_ROLE_ID))
                .emWvPort(new Integer(EMWEBPORT).toString())
                .wvHostName(tasResolver.getHostnameById(MOM_ROLE_ID))
                .wvPort(new Integer(WVPORT).toString())
                .startCommand(RunCommandFlow.class, momRole1.getEmRunCommandFlowContext())
                .stopCommand(RunCommandFlow.class, momRole1.getEmStopCommandFlowContext())
                .build();

        agcRegister.after(lastRole);
        agcRegister.after(new HashSet<IRole>(Arrays.asList(testbed.getMachineById(MOM_MACHINE_ID)
            .getRoles())));
        agcMachine.addRole(agcRegister);

        // register Standalone to AGC
        EmRole momRole2 = (EmRole) testbed.getRoleById(EM_ROLE_ID);
        AGCRegisterRole agcRegister2 =
            new AGCRegisterRole.Builder("agcMOM2Register", tasResolver).agcHostName(emHost)
                .agcEmWvPort(new Integer(EMWEBPORT).toString())
                .agcWvPort(new Integer(WVPORT).toString())
                .hostName(tasResolver.getHostnameById(EM_ROLE_ID))
                .emWvPort(new Integer(EMWEBPORT).toString())
                .wvHostName(tasResolver.getHostnameById(EM_ROLE_ID))
                .wvPort(new Integer(WVPORT).toString())
                .startCommand(RunCommandFlow.class, momRole2.getEmRunCommandFlowContext())
                .stopCommand(RunCommandFlow.class, momRole2.getEmStopCommandFlowContext())
                .build();

        agcRegister2.after(lastRole);
        agcRegister2.after(new HashSet<IRole>(Arrays.asList(testbed.getMachineById(EM_MACHINE_ID)
            .getRoles())));
        agcMachine.addRole(agcRegister2);

        testbed.addMachine(agcMachine);

        // Agent Testbed

        ITestbedMachine loadMachine1 =
            new TestbedMachine.Builder(LOAD_MACHINE1_ID).templateId(LOAD_TEMPLATE_ID)
                .bitness(Bitness.b64).build();

        ITestbedMachine loadMachine2 =
            new TestbedMachine.Builder(LOAD_MACHINE2_ID).templateId(LOAD_TEMPLATE_ID)
                .bitness(Bitness.b64).build();

        ITestbedMachine loadMachine3 =
            new TestbedMachine.Builder(LOAD_MACHINE3_ID).templateId(LOAD_TEMPLATE_ID)
                .bitness(Bitness.b64).build();

        addLoadMachines(testbed, loadMachine1, AGC_ROLE_ID, tasResolver, LOAD_ROLE1_ID,
            "2Complex-100agents-2apps-70frontends-100EJBsession");
        addLoadMachines(testbed, loadMachine2, MOM_ROLE_ID, tasResolver, LOAD_ROLE2_ID,
            "6Complex-100agents-2apps-40frontends-200EJBsession");
        addLoadMachines(testbed, loadMachine3, EM_ROLE_ID, tasResolver, LOAD_ROLE3_ID,
            "3Complex-200agents-2apps-25frontends-100EJBsession");

        testbed.addMachine(loadMachine1, loadMachine2, loadMachine3);

        return testbed;
    }

    public static IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWv,
        IRole beforeRole) {
        // starts EM or WebView
        ExecutionRole.Builder builder = new ExecutionRole.Builder(emRole.getRoleId() + "_start");

        builder.asyncCommand(emRole.getEmRunCommandFlowContext());
        if (startWv) {
            builder.asyncCommand(emRole.getWvRunCommandFlowContext());
        }

        ExecutionRole startRole = builder.build();
        startRole.after(beforeRole);
        machine.addRole(startRole);

        return startRole;
    }

    public static void addLoadMachines(Testbed testbed, ITestbedMachine machine, String em,
        ITasResolver tasResolver, String loadRole, String xml) {

        // StressApp
        ClientDeployRole stressApp =
            new ClientDeployRole.Builder(loadRole + "STRESS", tasResolver).emHost(
                tasResolver.getHostnameById(em)).build();


        // HVR Load
        FLDHvrAgentLoadExtractArtifact artifactFactory =
            new FLDHvrAgentLoadExtractArtifact(tasResolver);
        ITasArtifact artifact = artifactFactory.createArtifact("10.3");
        HVRAgentLoadRole hvrLoadRole =
            new HVRAgentLoadRole.Builder(loadRole + "HVR", tasResolver)
                .emHost(tasResolver.getHostnameById(em)).emPort("5001").cloneagents(4)
                .cloneconnections(15).agentHost("HVRAgent").secondspertrace(1)
                .addMetricsArtifact(artifact.getArtifact()).build();

        // Wurlitzer Load
        WurlitzerBaseRole wurlitzerBaseRole =
            new WurlitzerBaseRole.Builder(loadRole + "base", tasResolver)
                .deployDir("wurlitzerBase").build();

        machine.addRole(wurlitzerBaseRole);

        EmRole coll = (EmRole) testbed.getRoleById(em);
        WurlitzerLoadRole wurlitzerLoadrole =
            new WurlitzerLoadRole.Builder(loadRole + "load", tasResolver).emRole(coll)
                .buildFileLocation(SYSTEM_XML).target(xml).logFile(xml + ".log")
                .wurlitzerBaseRole(wurlitzerBaseRole).build();
        // machine.addRole(wurlitzerLoadrole);

        machine.addRole(stressApp, hvrLoadRole, wurlitzerLoadrole);
    }
}
