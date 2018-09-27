package com.ca.apm.systemtest.alertstateload.devel.testbed;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.alertstateload.role.AlertStateLoadMMRole;
import com.ca.apm.systemtest.alertstateload.testbed.Constants;
import com.ca.apm.systemtest.fld.artifact.TessTestArtifact;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

public class AlertStateLoadDataPreparationProvider implements Constants, FldTestbedProvider {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AlertStateLoadDataPreparationProvider.class);

    private static final String DB_USER = "cemadmin";
    private static final String DB_PASSWORD = "FrankfurtskaP0levka";
    private static final String DB_ADMIN_USER = "postgres";
    private static final String DB_ADMIN_PASSWORD = "OoohLaLa";

    private static final String DB_ROLE = "dbRole";
    private static final String EM_ROLE = "emRole";
    private static final String WV_ROLE = "wvRole";

    private static final String INTROSCOPE_AGENT_HOSTNAME = "tomcat01";

    private ITestbedMachine emMachine;
    private ITestbedMachine dbMachine;
    private ITestbedMachine wvMachine;
    private ITestbedMachine tomcatMachine;

    protected String getVersion(ITasResolver tasResolver) {
        String version;
        version = tasResolver.getDefaultVersion();
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        LOGGER.info("XXXXXXXXXX Using version " + version);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        return version;
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        emMachine = new TestbedMachine.LinuxBuilder(ASL_EM_MACHINE_ID).templateId("co65").build();

        dbMachine = new TestbedMachine.LinuxBuilder(ASL_DB_MACHINE_ID).templateId("co65").build();

        wvMachine = new TestbedMachine.LinuxBuilder(ASL_WV_MACHINE_ID).templateId("co65").build();

        tomcatMachine = new TestbedMachine.Builder(ASL_TOMCAT_MACHINE_ID).templateId("w64").build();

        return Arrays.asList(emMachine, dbMachine, wvMachine, tomcatMachine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String version = getVersion(tasResolver);

        EmRole dbRole =
            new EmRole.LinuxBuilder(DB_ROLE, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Database")).nostartEM().nostartWV()
                .dbAdminUser(DB_ADMIN_USER).dbAdminPassword(DB_ADMIN_PASSWORD).dbuser(DB_USER)
                .dbpassword(DB_PASSWORD).version(version).build();

        String dbhost = tasResolver.getHostnameById(DB_ROLE);
        Collection<String> laxOptions =
            Arrays.asList("-Xms2048m", "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc",
                "-Dcom.wily.assert=false", "-Xmx2048m", "-Dmail.mime.charset=UTF-8",
                "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseParNewGC",
                "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError",
                "-Xss256k");
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager")).nostartEM()
                .nostartWV().emLaxNlClearJavaOption(laxOptions).dbhost(dbhost).dbuser(DB_USER)
                .dbpassword(DB_PASSWORD).dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbAdminUser(DB_ADMIN_USER).version(version).build();

        String emHost = tasResolver.getHostnameById(EM_ROLE);
        EmRole wvRole =
            new EmRole.LinuxBuilder(WV_ROLE, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("WebView")).wvEmHost(emHost)
                .wvEmPort(5001).wvPort(8080).nostartEM().nostartWV().version(version).build();

        ExecutionRole startWv =
            new ExecutionRole.Builder("startWvRole").asyncCommand(
                wvRole.getWvRunCommandFlowContext()).build();
        ExecutionRole startEm =
            new ExecutionRole.Builder("startEmRole").asyncCommand(
                emRole.getEmRunCommandFlowContext()).build();

        dbMachine.addRole(dbRole);
        wvMachine.addRole(wvRole, startWv);
        emMachine.addRole(emRole, startEm);
        startEm.after(emRole, dbRole);
        startEm.before(startWv);
        startWv.after(wvRole);

        JavaRole javaRole =
            new JavaRole.Builder("javaRole", tasResolver).dir("c:\\java\\jdk1.8")
                .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();
        WebAppRole<TomcatRole> tessTestRole =
            new WebAppRole.Builder<TomcatRole>("tessTestRole")
                .artifact(new TessTestArtifact(tasResolver).createArtifact()).cargoDeploy()
                .contextName("tesstest").build();
        TomcatRole tomcatRole =
            new TomcatRole.Builder(ASL_TOMCAT_ROLE_ID, tasResolver).customJava(javaRole)
                .webApp(tessTestRole).tomcatVersion(TomcatVersion.v80).autoStart().build();
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.remoteagentdynamicinstrumentation.enabled",
            "true");
        additionalProperties.put("introscope.agent.hostName", INTROSCOPE_AGENT_HOSTNAME
        // + "_" + tasResolver.getHostnameById(ASL_TOMCAT_ROLE_ID)
            );
        AgentRole tomcatAgentRole =
            new AgentRole.Builder("tomcatAgentRole", tasResolver).webAppServer(tomcatRole)
                .emRole(emRole).customName("Tomcat-X").additionalProperties(additionalProperties)
                .version(version).build();

        tomcatMachine.addRole(tomcatRole, javaRole, tomcatAgentRole);

        AlertStateLoadMMRole alertStateLoadMMRole =
            (new AlertStateLoadMMRole.Builder("alertStateLoadMMRole", tasResolver)).emRole(emRole)
                .build();
        alertStateLoadMMRole.after(emRole, dbRole);
        alertStateLoadMMRole.before(startEm, startWv);
        emMachine.addRole(alertStateLoadMMRole);

        testbed.addMachine(emMachine, dbMachine, wvMachine, tomcatMachine);
    }

}
