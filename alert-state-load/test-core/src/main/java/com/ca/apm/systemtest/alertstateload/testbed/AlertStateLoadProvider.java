package com.ca.apm.systemtest.alertstateload.testbed;

import static com.ca.apm.systemtest.alertstateload.util.AlertStatusLoadUtil.getCollRoleId;
import static com.ca.apm.systemtest.alertstateload.util.AlertStatusLoadUtil.getLoadRoleId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.alertstateload.role.AlertStateLoadHammondRole;
import com.ca.apm.systemtest.alertstateload.role.AlertStateLoadMMRole;
import com.ca.apm.systemtest.alertstateload.role.MetricSynthRole;
import com.ca.apm.systemtest.alertstateload.role.TypeperfRole;
import com.ca.apm.systemtest.alertstateload.testbed.regional.ConfigurationService;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class AlertStateLoadProvider implements Constants, FldTestbedProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertStateLoadProvider.class);

    private static final boolean LOAD_MACHINES_ON_WINDOWS = ConfigurationService.getConfig()
        .isTestbedLoadMachinesOnWindows();
    private static final boolean DB_MACHINE_ON_WINDOWS = ConfigurationService.getConfig()
        .isTestbedDbMachineOnWindows();

    private static final String MACHINE_TEMPLATE_ID_W64_16GB = "w64_16gb";
    private static final String MACHINE_TEMPLATE_ID_CO65_16GB = "co65_16gb";
    private static final String MACHINE_TEMPLATE_ID_W64 = "w64";
    private static final String MACHINE_TEMPLATE_ID_CO66 = "co65";

    private static final String DB_USER = "cemadmin";
    private static final String DB_PASSWORD = "FrankfurtskaP0levka";
    private static final String DB_ADMIN_USER = "postgres";
    private static final String DB_ADMIN_PASSWORD = "OoohLaLa1234";

    private static final String DB_ROLE = "dbRole";
    private static final String MOM_ROLE = "momRole";
    private static final String WV_ROLE = "wvRole";

    private static final int EM_WEB_PORT = 8081;
    private static final int WV_PORT = 8080;

    private static final String INSTALL_DIR = "C:\\automation\\deployed\\em";
    public static final String GC_LOG_FILE = INSTALL_DIR + "\\logs\\gclog.txt";

    private static final String RESULTS_LOC = "c:\\sw\\results\\typeperf\\";

    private static final Collection<String> MOM_LAX_OPTIONS = Arrays.asList("-Xmx12g", "-Xms1g",
        "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc", "-Dcom.wily.assert=false",
        "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseParNewGC", "-XX:CMSInitiatingOccupancyFraction=50",
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xss256k");

    private static final Collection<String> COLLECTOR_LAX_OPTIONS = Arrays.asList("-Xmx8g",
        "-Xms1g", "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc",
        "-Dcom.wily.assert=false", "-Dmail.mime.charset=UTF-8",
        "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseParNewGC",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xss256k");

    private static final Collection<String> WV_LAX_OPTIONS = Arrays.asList("-Xmx8g", "-Xms1g",
        "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc", "-Dcom.wily.assert=false",
        "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseParNewGC", "-XX:CMSInitiatingOccupancyFraction=50",
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xss256k", "-Xloggc:" + GC_LOG_FILE);

    private ITestbedMachine momMachine;
    private ITestbedMachine[] collectorMachines;
    private ITestbedMachine dbMachine;
    private ITestbedMachine wvMachine;
    private ITestbedMachine[] loadMachines;

    protected String getVersion(ITasResolver tasResolver) {
        String version = ConfigurationService.getConfig().getTestbedEmVersion(tasResolver);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        LOGGER.info("XXXXXXXXXX Using version " + version);
        LOGGER
            .info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        return version;
    }

    protected String[] getTimeSynchronizationMachineIds() {
        return getMemoryMonitorMachineIds();
    }

    protected String[] getMemoryMonitorMachineIds() {
        return new String[] {ASL_WV_MACHINE_ID};
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        dbMachine =
            (DB_MACHINE_ON_WINDOWS
                ? (new TestbedMachine.Builder(ASL_DB_MACHINE_ID))
                : (new TestbedMachine.LinuxBuilder(ASL_DB_MACHINE_ID)))
                .platform(LOAD_MACHINES_ON_WINDOWS ? Platform.WINDOWS : Platform.CENTOS)
                .templateId(
                    DB_MACHINE_ON_WINDOWS
                        ? MACHINE_TEMPLATE_ID_W64_16GB
                        : MACHINE_TEMPLATE_ID_CO65_16GB).bitness(Bitness.b64).build();

        momMachine =
            (new TestbedMachine.Builder(ASL_MOM_MACHINE_ID)).platform(Platform.WINDOWS)
                .templateId(MACHINE_TEMPLATE_ID_W64_16GB).bitness(Bitness.b64).build();

        collectorMachines = new ITestbedMachine[ASL_COLL_MACHINES.length];
        for (int i = 0; i < ASL_COLL_MACHINES.length; i++) {
            collectorMachines[i] =
                (new TestbedMachine.Builder(ASL_COLL_MACHINES[i])).platform(Platform.WINDOWS)
                    .templateId(MACHINE_TEMPLATE_ID_W64_16GB).bitness(Bitness.b64).build();
        }

        wvMachine =
            (new TestbedMachine.Builder(ASL_WV_MACHINE_ID)).platform(Platform.WINDOWS)
                .templateId(MACHINE_TEMPLATE_ID_W64_16GB).bitness(Bitness.b64).build();

        loadMachines = new ITestbedMachine[ASL_LOAD_MACHINES.length];
        for (int i = 0; i < ASL_LOAD_MACHINES.length; i++) {
            loadMachines[i] =
                (LOAD_MACHINES_ON_WINDOWS
                    ? (new TestbedMachine.Builder(ASL_LOAD_MACHINES[i]))
                    : (new TestbedMachine.LinuxBuilder(ASL_LOAD_MACHINES[i])))
                    .platform(LOAD_MACHINES_ON_WINDOWS ? Platform.WINDOWS : Platform.CENTOS)
                    .templateId(
                        LOAD_MACHINES_ON_WINDOWS
                            ? MACHINE_TEMPLATE_ID_W64
                            : MACHINE_TEMPLATE_ID_CO66).bitness(Bitness.b64).build();
        }

        Collection<ITestbedMachine> machines = new ArrayList<>();
        machines.add(dbMachine);
        machines.add(momMachine);
        machines.addAll(Arrays.asList(collectorMachines));
        machines.add(wvMachine);
        machines.addAll(Arrays.asList(loadMachines));
        return machines;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String version = getVersion(tasResolver);

        // DB
        EmRole dbRole =
            (DB_MACHINE_ON_WINDOWS
                ? (new EmRole.Builder(DB_ROLE, tasResolver))
                : (new EmRole.LinuxBuilder(DB_ROLE, tasResolver)))
                .silentInstallChosenFeatures(Arrays.asList("Database")).nostartEM().nostartWV()
                .dbAdminUser(DB_ADMIN_USER).dbAdminPassword(DB_ADMIN_PASSWORD).dbuser(DB_USER)
                .dbpassword(DB_PASSWORD).version(version).build();

        dbMachine.addRole(dbRole);
        testbed.addMachine(dbMachine);
        String dbhost = tasResolver.getHostnameById(DB_ROLE);

        // MOM
        EmRole.Builder momBuilder = new EmRole.Builder(MOM_ROLE, tasResolver);

        // collectors
        List<IRole> collectorRoles = new ArrayList<>();
        for (int i = 0; i < collectorMachines.length; i++) {
            ITestbedMachine collectorMachine = collectorMachines[i];
            EmRole.Builder collBuilder =
                new EmRole.Builder(getCollRoleId(collectorMachine), tasResolver);

            collBuilder.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR).nostartEM().nostartWV()
                .emLaxNlClearJavaOption(COLLECTOR_LAX_OPTIONS).dbhost(dbhost).dbuser(DB_USER)
                .dbpassword(DB_PASSWORD).dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbAdminUser(DB_ADMIN_USER);

            EmRole collectorRole = collBuilder.version(version).build();
            collectorRoles.add(collectorRole);
            collectorRole.after(new HashSet<IRole>(Arrays.asList(dbMachine.getRoles())));
            collectorMachine.addRole(collectorRole);

            // start EM
            ExecutionRole startCollRole =
                (new ExecutionRole.Builder(collectorRole.getRoleId() + "_start")).asyncCommand(
                    collectorRole.getEmRunCommandFlowContext()).build();

            startCollRole.after(collectorRole);
            collectorMachine.addRole(startCollRole);

            momBuilder.emCollector(collectorRole);
            testbed.addMachine(collectorMachine);
        }

        // MOM
        momBuilder.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER).nostartEM().nostartWV()
            .autostartApmSqlServer().apmSqlServerBindAddress("0.0.0.0")
            .emLaxNlClearJavaOption(MOM_LAX_OPTIONS).dbhost(dbhost).dbuser(DB_USER)
            .dbpassword(DB_PASSWORD).dbAdminUser(DB_ADMIN_USER).dbAdminPassword(DB_ADMIN_PASSWORD)
            .emWebPort(EM_WEB_PORT);

        EmRole momRole = momBuilder.version(version).build();
        momRole.after(new HashSet<IRole>(Arrays.asList(dbMachine.getRoles())));
        momRole.before(collectorRoles);
        momMachine.addRole(momRole);
        String momHost = tasResolver.getHostnameById(MOM_ROLE);

        // start MOM
        ExecutionRole startMomRole =
            (new ExecutionRole.Builder(momRole.getRoleId() + "_start")).asyncCommand(
                momRole.getEmRunCommandFlowContext()).build();

        startMomRole.after(momRole);
        momMachine.addRole(startMomRole);

        testbed.addMachine(momMachine);

        // WV
        EmRole wvRole =
            (new EmRole.Builder(WV_ROLE, tasResolver))
                .silentInstallChosenFeatures(Arrays.asList("WebView"))
                .wvLaxNlClearJavaOption(WV_LAX_OPTIONS).wvEmHost(momHost).wvPort(WV_PORT)
                .nostartEM().nostartWV().version(version).build();

        wvRole.after(new HashSet<IRole>(Arrays.asList(momMachine.getRoles())));
        wvMachine.addRole(wvRole);

        // start WV
        ExecutionRole startWvRole =
            (new ExecutionRole.Builder(wvRole.getRoleId() + "_start")).asyncCommand(
                wvRole.getWvRunCommandFlowContext()).build();

        startWvRole.after(wvRole);
        startWvRole.after(startMomRole);
        wvMachine.addRole(startWvRole);

        testbed.addMachine(wvMachine);

        // deploy management module
        AlertStateLoadMMRole momAlertStateLoadRole =
            (new AlertStateLoadMMRole.Builder("alertStateLoadMMRole", tasResolver)).emRole(momRole)
                .build();
        momAlertStateLoadRole.after(momRole);
        momAlertStateLoadRole.before(startMomRole, startWvRole);
        momMachine.addRole(momAlertStateLoadRole);

        // MetricSynth
        addMetricSynthRoles(testbed, tasResolver);

        // Hammond roles
        // addHammondRoles(testbed, tasResolver);

        // CPU monitoring
        TypeperfRole typeperfRole =
            new TypeperfRole.Builder(ASL_TYPEPERFROLE_ROLE, tasResolver)
                .metrics(new String[] {"\\Processor(_Total)\\% Processor Time"}).runTime(300L)
                .outputFileName(RESULTS_LOC + "typeperf.csv").build();
        wvMachine.addRole(typeperfRole);
    }

    private void addMetricSynthRoles(ITestbed testbed, ITasResolver tasResolver) {
        int loadMachinesLength = loadMachines.length;
        int collectorMachinesLength = collectorMachines.length;
        int loadMachinesIndex = 0;
        int collectorMachinesIndex = 0;
        if (loadMachinesLength <= collectorMachinesLength) {
            for (collectorMachinesIndex = 0; collectorMachinesIndex < collectorMachinesLength; collectorMachinesIndex++) {
                if (loadMachinesIndex == loadMachinesLength) {
                    loadMachinesIndex = 0;
                }
                ITestbedMachine collectorMachine = collectorMachines[collectorMachinesIndex];
                String collectorMachineId = collectorMachine.getMachineId();
                String collectorHost =
                    tasResolver.getHostnameById(getCollRoleId(collectorMachineId));

                ITestbedMachine loadMachine = loadMachines[loadMachinesIndex++];
                String loadMachineId = loadMachine.getMachineId();
                String loadRoleId = getLoadRoleId(loadMachineId, collectorMachineId);
                MetricSynthRole metricSynthRole =
                    (new MetricSynthRole.Builder(loadRoleId, tasResolver)).collectorHost(
                        collectorHost).build();
                loadMachine.addRole(metricSynthRole);
                testbed.addMachine(loadMachine);
                LOGGER
                    .info(
                        "AlertStateLoadProvider.addMetricSynthRoles():: loadMachineId = {}, loadRoleId = {}, collectorHost = {}",
                        loadMachineId, loadRoleId, collectorHost);
            }
        } else {
            for (loadMachinesIndex = 0; loadMachinesIndex < loadMachinesLength; loadMachinesIndex++) {
                if (collectorMachinesIndex == collectorMachinesLength) {
                    collectorMachinesIndex = 0;
                }
                ITestbedMachine collectorMachine = collectorMachines[collectorMachinesIndex++];
                String collectorMachineId = collectorMachine.getMachineId();
                String collectorHost =
                    tasResolver.getHostnameById(getCollRoleId(collectorMachineId));

                ITestbedMachine loadMachine = loadMachines[loadMachinesIndex];
                String loadMachineId = loadMachine.getMachineId();
                String loadRoleId = getLoadRoleId(loadMachineId, collectorMachineId);
                MetricSynthRole metricSynthRole =
                    (new MetricSynthRole.Builder(loadRoleId, tasResolver)).collectorHost(
                        collectorHost).build();
                loadMachine.addRole(metricSynthRole);
                testbed.addMachine(loadMachine);
                LOGGER
                    .info(
                        "AlertStateLoadProvider.addMetricSynthRoles():: loadMachineId = {}, loadRoleId = {}, collectorHost = {}",
                        loadMachineId, loadRoleId, collectorHost);
            }
        }
    }

    @SuppressWarnings("unused")
    private void addHammondRoles(ITestbed testbed, ITasResolver tasResolver) {
        int hammondMachinesLength = loadMachines.length;
        int collectorMachinesLength = collectorMachines.length;
        int hammondMachinesIndex = 0;
        int collectorMachinesIndex = 0;
        if (hammondMachinesLength <= collectorMachinesLength) {
            for (collectorMachinesIndex = 0; collectorMachinesIndex < collectorMachinesLength; collectorMachinesIndex++) {
                if (hammondMachinesIndex == hammondMachinesLength) {
                    hammondMachinesIndex = 0;
                }
                ITestbedMachine collectorMachine = collectorMachines[collectorMachinesIndex];
                String collectorMachineId = collectorMachine.getMachineId();
                String collectorHost =
                    tasResolver.getHostnameById(getCollRoleId(collectorMachineId));

                ITestbedMachine hammondMachine = loadMachines[hammondMachinesIndex++];
                String hammondMachineId = hammondMachine.getMachineId();
                String hammondRoleId = getLoadRoleId(hammondMachineId, collectorMachineId);
                String prefix = null;// "" + collectorMachinesIndex;
                AlertStateLoadHammondRole hammondRole =
                    getAlertStateLoadHammondRole(tasResolver, hammondRoleId, collectorHost, prefix);
                hammondMachine.addRole(hammondRole);
                testbed.addMachine(hammondMachine);
                LOGGER
                    .info(
                        "AlertStateLoadProvider.addHammondRoles():: hammondMachineId = {}, hammondRoleId = {}, collectorHost = {}",
                        hammondMachineId, hammondRoleId, collectorHost);
            }
        } else {
            for (hammondMachinesIndex = 0; hammondMachinesIndex < hammondMachinesLength; hammondMachinesIndex++) {
                if (collectorMachinesIndex == collectorMachinesLength) {
                    collectorMachinesIndex = 0;
                }
                ITestbedMachine collectorMachine = collectorMachines[collectorMachinesIndex++];
                String collectorMachineId = collectorMachine.getMachineId();
                String collectorHost =
                    tasResolver.getHostnameById(getCollRoleId(collectorMachineId));

                ITestbedMachine hammondMachine = loadMachines[hammondMachinesIndex];
                String hammondMachineId = hammondMachine.getMachineId();
                String hammondRoleId = getLoadRoleId(hammondMachineId, collectorMachineId);
                String prefix = null;// "" + hammondMachinesIndex;
                AlertStateLoadHammondRole hammondRole =
                    getAlertStateLoadHammondRole(tasResolver, hammondRoleId, collectorHost, prefix);
                hammondMachine.addRole(hammondRole);
                testbed.addMachine(hammondMachine);
                LOGGER
                    .info(
                        "AlertStateLoadProvider.addHammondRoles():: hammondMachineId = {}, hammondRoleId = {}, collectorHost = {}",
                        hammondMachineId, hammondRoleId, collectorHost);
            }
        }
    }

    private static AlertStateLoadHammondRole getAlertStateLoadHammondRole(ITasResolver tasResolver,
        String hammondRoleId, String collectorHost, String prefix) {
        AlertStateLoadHammondRole.Builder builder =
            (LOAD_MACHINES_ON_WINDOWS ? (new AlertStateLoadHammondRole.Builder(hammondRoleId,
                tasResolver)) : (new AlertStateLoadHammondRole.LinuxBuilder(hammondRoleId,
                tasResolver)));
        if (prefix != null) {
            builder = builder.prefix(prefix);
        }
        builder = builder
        // .data(HammondDataVersion.AlertStatusLoad)
            .collector(collectorHost)
        // .scale(1)
        // .runDuration(600)
        // .from(0)
        // .to(Long.MAX_VALUE)
        ;
        AlertStateLoadHammondRole hammondRole = builder.build();
        return hammondRole;
    }

}
