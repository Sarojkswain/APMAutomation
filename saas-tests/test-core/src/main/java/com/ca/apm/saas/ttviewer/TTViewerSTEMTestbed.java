package com.ca.apm.saas.ttviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.artifact.FLDHvrAgentLoadExtractArtifact;
import com.ca.apm.systemtest.fld.role.CLWWorkStationLoadRole;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerLoadRole;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * TTViewer Backend SystemTest testbed.
 * 
 * @author banra06
 *
 */
@TestBedDefinition
public class TTViewerSTEMTestbed implements ITestbedFactory {

    public static final String EM_VERSION = "99.99.ttvi_stable-SNAPSHOT";
    public static final String WLS_ROLE_ID = "wls12c";
    public static final String WLS_ROLE2_ID = "wls12c_role2";
    public static final String WURLITZER_ROLE_ID = "wurlitzerRole";
    public static final String CLW_ROLE_ID = "clwRole";
    public static final String HVR_ROLE_ID = "hvrRole";
    private JavaRole javaRole;
    public static final String WLSMACHINE_1 = "wlsmachine1";
    public static final String WLSMACHINE_2 = "wlsmachine2";
    public static final String WLSCLIENT_1 = "wlsclient1";
    public static final String WLSCLIENT_2 = "wlsclient2";
    protected boolean isJassEnabled = false;
    protected boolean isLegacyMode = false;
    public static final String WIN_TEMPLATE_ID = "w64";
    protected static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
    protected static final String WLS12C_INSTALL_HOME = DEPLOY_BASE
            + "Oracle/Middleware12.1.3";
    public static final String EM_ROLE_ID = "emRole";
    public static final String EM_MACHINE_ID = "emMachine";
    public static final String LOAD_MACHINE1_ID = "loadMachine1";
    public static final String EM_TEMPLATE_ID = "co66";
    private static final String SYSTEM_XML = "xml/appmap-stress/load-test/system.xml";
    public static final String INSTALL_DIR = "/em/Introscope";
    public static final String INSTALL_TG_DIR = "/em/Installer";
    public static final String DATABASE_DIR = "/em/database";
    public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";
    public static final String DB_PASSWORD = "password";
    public static final String DB_USERNAME = "cemadmin";
    public static final String DB_ADMIN_USERNAME = "postgres";
    public static final int WVPORT = 8084;
    public static final int EMWEBPORT = 8081;
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays
            .asList("-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
                    "-Dorg.owasp.esapi.resources=./config/esapi",
                    "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k",
                    "-Dcom.wily.assert=false", "-showversion",
                    "-XX:CMSInitiatingOccupancyFraction=50",
                    "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m",
                    "-Xmx4096m", "-verbose:gc", "-Xloggc:" + GC_LOG_FILE,
                    "-Dappmap.user=admin", "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    public static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays
            .asList("-Djava.awt.headless=true",
                    "-Dorg.owasp.esapi.resources=./config/esapi",
                    "-Dsun.java2d.noddraw=true",
                    "-javaagent:./product/webview/agent/wily/Agent.jar",
                    "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                    "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily",
                    "-Xms2048m", "-Xmx2048m", "-XX:+PrintGCDateStamps",
                    "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
                    "-Xloggc:" + GC_LOG_FILE);

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Testbed testbed = new Testbed("TTViewerTestBed");
        // StandAlone EM
        ITestbedMachine emMachine = new TestbedMachine.LinuxBuilder(
                EM_MACHINE_ID).templateId(EM_TEMPLATE_ID).bitness(Bitness.b64)
                .build();
        EmRole emRole = new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(
                        Arrays.asList("Enterprise Manager", "ProbeBuilder",
                                "EPA", "Database", "WebView"))
                .dbuser(DB_USERNAME).dbpassword(DB_PASSWORD)
                .dbAdminUser(DB_ADMIN_USERNAME).dbAdminPassword(DB_PASSWORD)
                .databaseDir(DATABASE_DIR).emWebPort(EMWEBPORT)
                .installDir(INSTALL_DIR).installerTgDir(INSTALL_TG_DIR)
                .wvPort(WVPORT).emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION).version(EM_VERSION)
                .configProperty("introscope.apmserver.teamcenter.saas", "true")
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION).build();
        emMachine.addRole(emRole);
        testbed.addMachine(emMachine);

        // Weblogic Agent Testbed
        // Weblogic1
        ITestbedMachine wlsMachine1 = new TestbedMachine.Builder(WLSMACHINE_1)
                .templateId(WIN_TEMPLATE_ID).bitness(Bitness.b64).build();
        wlsMachine1.addRole(new ClientDeployRole.Builder(WLSCLIENT_1,
                tasResolver)
                .fldXjvmhost(tasResolver.getHostnameById(WLS_ROLE2_ID))
                .emHost(tasResolver.getHostnameById(EM_ROLE_ID)).build());

        addWeblogicRoles(WLS_ROLE_ID, wlsMachine1, tasResolver,
                tasResolver.getHostnameById(EM_ROLE_ID));

        // Weblogic2
        ITestbedMachine wlsMachine2 = new TestbedMachine.Builder(WLSMACHINE_2)
                .templateId(WIN_TEMPLATE_ID).build();
        wlsMachine2.addRole(new ClientDeployRole.Builder(WLSCLIENT_2,
                tasResolver).emHost(tasResolver.getHostnameById(EM_ROLE_ID))
                .build());

        addWeblogicRoles(WLS_ROLE2_ID, wlsMachine2, tasResolver,
                tasResolver.getHostnameById(EM_ROLE_ID));

        // Load machine
        ITestbedMachine loadMachine = new TestbedMachine.Builder(
                LOAD_MACHINE1_ID).templateId(WIN_TEMPLATE_ID).build();
        // HVR Load
        FLDHvrAgentLoadExtractArtifact artifactFactory = new FLDHvrAgentLoadExtractArtifact(
                tasResolver);
        ITasArtifact artifact = artifactFactory.createArtifact("10.3");
        HVRAgentLoadRole hvrLoadRole = new HVRAgentLoadRole.Builder(
                HVR_ROLE_ID, tasResolver)
                .emHost(tasResolver.getHostnameById(EM_ROLE_ID)).emPort("5001")
                .cloneagents(25).cloneconnections(8).agentHost("HVRAgent")
                .secondspertrace(1).addMetricsArtifact(artifact.getArtifact())
                .build();
        // Wurlitzer Load
        WurlitzerBaseRole wurlitzerBaseRole = new WurlitzerBaseRole.Builder(
                "wurlitzer_base", tasResolver).deployDir("wurlitzerBase")
                .build();
        loadMachine.addRole(wurlitzerBaseRole);
        EmRole coll = (EmRole) testbed.getRoleById(EM_ROLE_ID);
        String xml = "3Complex-200agents-2apps-25frontends-100EJBsession";
        WurlitzerLoadRole wurlitzerLoadrole = new WurlitzerLoadRole.Builder(
                WURLITZER_ROLE_ID, tasResolver).emRole(coll)
                .buildFileLocation(SYSTEM_XML).target(xml)
                .logFile(xml + ".log").wurlitzerBaseRole(wurlitzerBaseRole)
                .build();
        // CLW Load
        CLWWorkStationLoadRole clwRole = new CLWWorkStationLoadRole.Builder(
                CLW_ROLE_ID, tasResolver)
                .emHost(tasResolver.getHostnameById(EM_ROLE_ID))
                .agentName(tasResolver.getHostnameById(WLS_ROLE_ID) + ".*")
                .agentName(tasResolver.getHostnameById(WLS_ROLE2_ID) + ".*")
                .agentName("HVRAgent.*").agentName(".*ErrorStallAgent.*")
                .agentName("pipeorgan")
                .agentName("domain")
                .agentName("server")
                .build();
        loadMachine.addRole(clwRole, wurlitzerLoadrole, hvrLoadRole);
        testbed.addMachine(loadMachine, wlsMachine1, wlsMachine2);
        return testbed;
    }

    protected void addWeblogicRoles(String wlsRoleId, ITestbedMachine machine,
            ITasResolver tasResolver, String emHost) {

        addWeblogicRoles(wlsRoleId, machine, tasResolver,
                tasResolver.getDefaultVersion(), emHost);
    }

    protected void addWeblogicRoles(String wlsRoleId, ITestbedMachine machine,
            ITasResolver tasResolver, String agentVersion, String emHost) {

        // install wls
        javaRole = new JavaRole.Builder(machine.getMachineId() + "_"
                + "java8Role", tasResolver).version(
                JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();

        GenericRole wlsRole = getwls12cRole(tasResolver, wlsRoleId);

        WLSAgentAppDeployRole wlsAgentPORole = new WLSAgentAppDeployRole.Builder(
                machine.getMachineId() + "_" + wlsRoleId, tasResolver)
                .agentVersion(agentVersion).classifier("jvm7-genericnodb")
                .isLegacyMode(isLegacyMode).isJassEnabled(isJassEnabled)
                .javaRole(javaRole).serverPort("7001").wlsRole(wlsRoleId)
                .emHost(emHost).build();

        javaRole.before(wlsRole);
        wlsRole.before(wlsAgentPORole);
        machine.addRole(javaRole, wlsRole, wlsAgentPORole);
    }

    @NotNull
    protected GenericRole getwls12cRole(ITasResolver tasResolver, String wlsRole) {

        ArrayList<String> args = new ArrayList<String>();
        args.add("-silent");

        RunCommandFlowContext installWlc12cCommand = new RunCommandFlowContext.Builder(
                "configure.cmd").workDir(WLS12C_INSTALL_HOME).args(args)
                .build();

        GenericRole wls12cInstallRole = new GenericRole.Builder(wlsRole,
                tasResolver)
                .unpack(new DefaultArtifact("com.ca.apm.binaries", "weblogic",
                        "dev", "zip", "12.1.3"),
                        codifyPath(WLS12C_INSTALL_HOME))
                .runCommand(installWlc12cCommand).build();
        return wls12cInstallRole;
    }

    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }
}