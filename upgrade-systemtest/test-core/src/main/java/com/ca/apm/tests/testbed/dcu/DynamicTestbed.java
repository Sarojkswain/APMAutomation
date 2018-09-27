package com.ca.apm.tests.testbed.dcu;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.apm.tests.artifact.HammondDataVersion2;
import com.ca.apm.tests.flow.HomeFileCreatorFlow;
import com.ca.apm.tests.flow.HomeFileCreatorFlowContext;
import com.ca.apm.tests.flow.RestorePermissionsFlow;
import com.ca.apm.tests.flow.RestorePermissionsFlowContext;
import com.ca.apm.tests.role.EmRoleBuilder;
import com.ca.apm.tests.role.EmRoleLinuxBuilder;
import com.ca.apm.tests.role.IEmRoleBuilder;
import com.ca.apm.tests.role.RestoreDataRole;
import com.ca.apm.tests.role.ScpCopyRole;
import com.ca.tas.agent.entities.RoleData;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.utility.UtilityRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.tests.annotations.TestBedDynamicField;
import com.ca.tas.type.Platform;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by jirji01 on 6/23/2017.
 */
@TestBedDefinition
public class DynamicTestbed implements ITestbedFactory {

    private static final List<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-XX:MaxPermSize=256m",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError");

    private static final List<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50");

    private static final List<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1g", "-Xmx1g");


    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTestbed.class);

    public static final String DB_ROLE_ID = "dbRole";
    public static final String WV_ROLE_ID = "wvRole";
    public static final String UPGRADE_BUNDLE_ROLE_ID = "upgradeBundle";

    private static final String DB_MACHINE_ID = "dbMachine";
    public static final String CC_MACHINE_ID = "ccMachine";


    @TestBedDynamicField("dcuFolder")
    private String dcuFolder = "c:/replay/vw-small/";

    @TestBedDynamicField("upgradeBundleVersion")
    private String upgradeBundleVersion = "99.99.bruceWillis-SNAPSHOT";

    @TestBedDynamicField("linuxRootFolder")
    private String linuxRootFolder = "";

    @TestBedDynamicField("windowsRootFolder")
    private String windowsRootFolder = "";

    @TestBedDynamicField("user")
    private String user = "root";

    @TestBedDynamicField("memory")
    private String memory = "2048m";

    @TestBedDynamicField("emVersion")
    private String emVersion;

    private String linuxAutomation = "/opt/automation/deployed";
    private String windowsAutomation = "C:\\automation\\deployed\\";

    private Testbed testbed;
    private ITasResolver resolver;

    private boolean databaseInstalled = false;
    private boolean webviewInstalled = false;
    private int index = 1;

    @Override
    public ITestbed create(ITasResolver resolver) {
        testbed = new Testbed(getClass().getSimpleName());
        this.resolver = resolver;

        if (StringUtils.isNotBlank(linuxRootFolder)) {
            linuxAutomation = linuxRootFolder + "/automation";
        }
        if (StringUtils.isNotBlank(windowsRootFolder)) {
            windowsAutomation = windowsRootFolder + "/automation";
        }

        LOGGER.info("Linux root folder: '{}'; Linux automation folder: '{}'.", linuxRootFolder, linuxAutomation);
        LOGGER.info("Windows root folder: '{}'; Windows automation folder: '{}'.", windowsRootFolder, windowsAutomation);

        try {
            if (! Files.isDirectory(Paths.get(dcuFolder))) {
                LOGGER.error("'{}' is not a directory", dcuFolder);
                return testbed;
            }

            LOGGER.info("'{}' is a directory", dcuFolder);

            TestbedMachine ccMachine = getMachine(CC_MACHINE_ID, Platform.LINUX);
            testbed.addMachine(ccMachine);

            Map<String, String> accConfig = Collections.singletonMap("agentController.listener.port", "8889");

            // add upgrade bundle
            UniversalRole upgradeBundleRole = new UniversalRole.Builder(UPGRADE_BUNDLE_ROLE_ID, resolver)
                    .unpack(new DefaultArtifact("com.ca.apm.delivery", "cluster-upgrade-bundle.unix", "tar", upgradeBundleVersion), linuxAutomation + "/acc")
                    .configuration(linuxAutomation + "/acc/config/apmccsrv.properties", accConfig)
                    .syncCommand(new RunCommandFlowContext.Builder("apmccsrv.sh")
                            .args(Collections.singleton("start"))
                            .workDir(linuxAutomation + "/acc")
                            .build())
                    .build();
            ccMachine.addRole(upgradeBundleRole);

            final UtilityRole dropKeyRole = UtilityRole.flow("drop-key", HomeFileCreatorFlow.class, new HomeFileCreatorFlowContext.Builder()
                    .permissions("rw-------")
                    .destinationDir("~/.ssh")
                    .destinationFilename("tasuser.key")
                    .fromResource("/tasuser.key")
                    .build());
            dropKeyRole.before(upgradeBundleRole);
            ccMachine.addRole(dropKeyRole);

            DcuData dcuData = new DcuData(dcuFolder);
            List<EmRole> collectors = new ArrayList<>();
            while (dcuData.hasNextDcu()) {

                dcuData.processNextDcu();
                DcuData.EmConfigProperties config = dcuData.getConfigProperties();

                if (config.getClusterMode() == DcuData.EmConfigProperties.ClusterMode.Collector) {
                    index++;

                    boolean startWebView = false;
                    String databaseDir = null;
                    Map<String, String> features = Collections.EMPTY_MAP;
                    String xms = "-Xms2048m";
                    String xmx = "-Xmx2048m";

                    for (RoleData data : dcuData.getRoleData()) {

                        new Gson().fromJson(data.getEmRoleBuilderCreator(), Map.class);

                        switch (data.getType()) {
                            case "webview":
                                startWebView = true;
                                break;
                            case "collector":
                                features = new Gson().fromJson(data.getEmRoleBuilderCreator(), Map.class);
                                if (StringUtils.isBlank(memory)) {
                                    for (String arg : data.getRuntimeArgs()) {
                                        if (arg.startsWith("-Xms")) {
                                            xms = arg;
                                        } else if (arg.startsWith("-Xmx")) {
                                            xmx = arg;
                                        }
                                    }
                                } else {
                                    xms = "-Xms" + memory;
                                    xmx = "-Xmx" + memory;
                                }
                                break;
                            case "database":
                                databaseDir = data.getInstallDir();
                                break;
                            default:
                                throw new RuntimeException("Unknown installation type");
                        }
                    }

                    List<String> lax = new ArrayList<>(COLL_LAXNL_JAVA_OPTION.size() + 2);
                    lax.addAll(COLL_LAXNL_JAVA_OPTION);
                    lax.add(xms);
                    lax.add(xmx);

                    EmRole emRole = emBuilder(dcuData, features, databaseDir, lax).build();
                    collectors.add(emRole);

                    TestbedMachine machine = getMachine(index + "Machine", features);
                    testbed.addMachine(machine);
                    machine.addRole(emRole);

                    createSupportRoles(resolver, dcuData.getDcuDataFile(), config, features, startWebView, machine, emRole);
                } else if (config.getClusterMode() == DcuData.EmConfigProperties.ClusterMode.CDV) {
                    throw new IllegalStateException("EM role other than MOM and Collector are not in happy path scenario.");
                }
            }

            dcuData = new DcuData(dcuFolder);
            while (dcuData.hasNextDcu()) {

                dcuData.processNextDcu();
                DcuData.EmConfigProperties config = dcuData.getConfigProperties();


                if (config.getClusterMode() == DcuData.EmConfigProperties.ClusterMode.MOM
                        || config.getClusterMode() == DcuData.EmConfigProperties.ClusterMode.StandAlone) {

                    index = 1;

                    boolean startWebView = false;
                    String databaseDir = null;
                    String webviewDir = null;
                    Map<String, String> features = null;
                    String xms = "-Xms2048";
                    String xmx = "-Xmx2048";

                    for (RoleData data : dcuData.getRoleData()) {

                        new Gson().fromJson(data.getEmRoleBuilderCreator(), Map.class);

                        switch (data.getType()) {
                            case "webview":
                                startWebView = true;
                                webviewDir = data.getInstallDir();
                                break;
                            case "mom":
                            case "standalone":
                                features = new Gson().fromJson(data.getEmRoleBuilderCreator(), Map.class);
                                if (StringUtils.isBlank(memory)) {
                                    for (String arg : data.getRuntimeArgs()) {
                                        if (arg.startsWith("-Xms")) {
                                            xms = arg;
                                        } else if (arg.startsWith("-Xmx")) {
                                            xmx = arg;
                                        }
                                    }
                                } else {
                                    xms = "-Xms" + memory;
                                    xmx = "-Xmx" + memory;
                                }
                                break;
                            case "database":
                                databaseDir = data.getInstallDir();
                                break;
                            default:
                                throw new RuntimeException("Unknown installation type");
                        }
                    }

                    List<String> lax = new ArrayList<>(MOM_LAXNL_JAVA_OPTION.size() + 2);
                    lax.addAll(MOM_LAXNL_JAVA_OPTION);
                    lax.add(xms);
                    lax.add(xmx);


                    EmRole momRole = emBuilder(dcuData, features, databaseDir, lax)
                            .emCollectors(collectors)
                            .build();

                    TestbedMachine machine = getMachine(index + "Machine", features);
                    testbed.addMachine(machine);
                    machine.addRole(momRole);

                    IRole dbRole;
                    if (! databaseInstalled) {
                        dbRole = dbRole(databaseDir, getVersion(config), false);

                        TestbedMachine dbMachine = getMachine(DB_MACHINE_ID, Platform.LINUX);
                        dbMachine.addRole(dbRole);
                        testbed.addMachine(dbMachine);
                    } else {
                        dbRole = new EmptyRole.LinuxBuilder(DB_ROLE_ID, resolver).build();
                        machine.addRole(dbRole);
                    }
                    ArrayList<IRole> dbDependencies = new ArrayList<>();
                    dbDependencies.addAll(collectors);
                    dbDependencies.add(momRole);
                    dbRole.before(dbDependencies);

                    IRole momStartRole = createSupportRoles(resolver, dcuData.getDcuDataFile(), config, features, startWebView, machine, momRole);

                    if (! webviewInstalled) {
                        EmRole wvRole = wvRole(webviewDir, getVersion(config), momRole);
                        ccMachine.addRole(wvRole);

                        ExecutionRole wvStartRole = new ExecutionRole.Builder(WV_ROLE_ID + "_start")
                                .syncCommand(wvRole.getWvRunCommandFlowContext())
                                .build();
                        wvStartRole.after(momStartRole);
                        ccMachine.addRole(wvStartRole);
                    }

                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot load DCU config data.", e);
        }

        final List<ScpCopyRole> rolesByType = new ArrayList<>(testbed.getRolesByType(ScpCopyRole.class));
        final int size = rolesByType.size();
        for (int i = 1; i < size; i++) {
            rolesByType.get(i).before((rolesByType.get(i-1)));
        }

        return testbed;
    }

    private IRole createSupportRoles(ITasResolver resolver, Path dcuData, DcuData.EmConfigProperties config, Map<String, String> features, boolean startWebView, TestbedMachine emMachine, EmRole emRole) {

        Platform platform = emMachine.getPlatform();

        String automation;
        String rootFolder;
        switch (platform) {
            case WINDOWS:
                automation = windowsAutomation;
                rootFolder = windowsRootFolder;
                break;
            default:
                automation = linuxAutomation;
                rootFolder = linuxRootFolder;
                break;
        }

        ScpCopyRole scpRole = new ScpCopyRole("ScpRole" + emRole.getId(), dcuData, user, resolver.getHostnameById(emRole.getRoleId()), automation, 22);
        emMachine.addRole(scpRole);

        /*
        HammondRole.Builder hammondBuilder = new HammondRole.LinuxBuilder("HammondRole" + emRole.getId(), resolver);
        HammondRole hammondRole = hammondBuilder
                .heapMemory("1024m")
                .installDir(automation)
                .collector(resolver.getHostnameById(emRole.getRoleId()))
                .data(HammondDataVersion2.UPGRADE_EMPTY)
                .build();
        emMachine.addRole(hammondRole);
        */
        RestoreDataRole restoreDataRole = new RestoreDataRole.Builder("RestoreRole" + emRole.getId(), resolver)
                .em(rootFolder + config.getEmDir())
                .smartstor(dataAbsolutePath(platform, config.getEmDir(), config.getSmartStorDir()))
                .smartstorArchive(dataAbsolutePath(platform, config.getEmDir(), config.getSmartStorArchiveDir()))
                .smartstorMeta(dataAbsolutePath(platform, config.getEmDir(), config.getSmartStorMetadataDir()))
                .traces(dataAbsolutePath(platform, config.getEmDir(), config.getTracesDir()))
                .baseLine(dataAbsolutePath(platform, config.getEmDir(), config.getBaseLineFile()))
                .hammondData(automation + "/hammond/" +
                        HammondDataVersion2.UPGRADE_EMPTY.getArtifact().getArtifactId() + "/" +
                        HammondDataVersion2.UPGRADE_EMPTY.getArtifact().getClassifier())
                .dbHost(resolver.getHostnameById(DB_ROLE_ID))
                .dbVersion(getVersion(config))
                .sourceData(scpRole.getDestFile())
                .build();
        restoreDataRole.after(emRole, scpRole);
        emMachine.addRole(restoreDataRole);

        IRole syncRole = restoreDataRole;

        if (platform != Platform.WINDOWS) {
            UniversalRole restorePermissionsRole = new UniversalRole.Builder("RestorePermissionsRole" + emRole.getId(), resolver)
                    .runFlow(RestorePermissionsFlow.class, new RestorePermissionsFlowContext.Builder()
                            .emFileListing(automation + "/em-file-listing.txt")
                            .baseFolder(rootFolder)
                            .build())
                    .build();
            restorePermissionsRole.after(restoreDataRole);
            emMachine.addRole(restorePermissionsRole);

            syncRole = restorePermissionsRole;
        }

        IRole startRole = RoleUtility.addStartEmRole(emMachine, emRole, startWebView, syncRole);
        /*
        UniversalRole startHammondRole = new UniversalRole.Builder("StartHammondRole" + emRole.getId(), resolver)
                .syncCommand(hammondRole.getStartCommandFlowContexts().get(0))
                .build();
        startHammondRole.after(hammondRole, restoreDataRole, startRole);
        emMachine.addRole(startHammondRole);
        */

        return startRole;
    }

    private String dataAbsolutePath(Platform platform, String emFolder, String dataFolder) {
        String result;

        switch (platform) {
            case WINDOWS:
                if (StringUtils.isEmpty(dataFolder)) {
                    result = "";
                } else if (dataFolder.substring(1, dataFolder.length()).startsWith(":")) {
                    result = windowsRootFolder + dataFolder.substring(2, dataFolder.length());
                } else {
                    result = windowsRootFolder + emFolder.substring(2, emFolder.length()) + '\\' + dataFolder.substring(2, dataFolder.length());
                }
                break;
            default:
                if (StringUtils.isEmpty(dataFolder)) {
                    result = "";
                } else if (dataFolder.startsWith("/")) {
                    result = linuxRootFolder + dataFolder;
                } else {
                    result = linuxRootFolder + emFolder + '/' + dataFolder;
                }
                break;
        }

        return result;
    }

    private EmRole.Builder emBuilder(DcuData dcuData, Map<String, String> features, String databaseDir, List<String> javaOptions) {
        String roleId = index + "EmRole";

        Platform platform;
        EmRole.Builder builder;
        String automation;
        String rootFolder;
        if ("windows".equalsIgnoreCase(features.get("platformName"))) {
            builder = new EmRoleBuilder(roleId, resolver);
            automation = windowsAutomation;
            rootFolder = windowsRootFolder;
            platform = Platform.WINDOWS;
        } else {
            builder = new EmRoleLinuxBuilder(roleId, resolver);
            automation = linuxAutomation;
            rootFolder = linuxRootFolder;
            platform = Platform.LINUX;
        }

        DcuData.EmConfigProperties config = dcuData.getConfigProperties();
        Collection<String> chosenFeatures = Arrays.asList(features.get("silentInstallChosenFeatures").split(","));

        ((IEmRoleBuilder) builder)
                .installerDir(automation + "/installer")
                .installerTgDir(automation + "/installer/em")
                .silentInstallChosenFeatures(chosenFeatures)
                .emPort(Integer.parseInt(features.get("emport")))
                .nostartEM()
                .nostartWV()
                .version(getVersion(config))
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emLaxNlClearJavaOption(javaOptions)
                .installDir(rootFolder + config.getEmDir())
                .dbhost(resolver.getHostnameById(DB_ROLE_ID))
                .configProperty("introscope.enterprisemanager.smartstor.directory", location(platform, config.getSmartStorDir()))
                .configProperty("introscope.enterprisemanager.smartstor.directory.metadata", location(platform, config.getSmartStorMetadataDir()))
                .configProperty("introscope.enterprisemanager.smartstor.directory.archive", location(platform, config.getSmartStorArchiveDir()))
                .configProperty("introscope.enterprisemanager.baseline.database", location(platform, config.getBaseLineFile()))
                .configProperty("introscope.enterprisemanager.transactionevents.storage.dir", location(platform, config.getTracesDir()))
                .configProperty("introscope.enterprisemanager.smartstor.tier1.frequency", "15")
                .configProperty("introscope.enterprisemanager.smartstor.tier1.age", "30")
                .configProperty("introscope.enterprisemanager.smartstor.tier2.frequency", "15")
                .configProperty("introscope.enterprisemanager.smartstor.tier2.age", "70")
                .configProperty("introscope.enterprisemanager.smartstor.tier3.frequency", "15")
                .configProperty("introscope.enterprisemanager.smartstor.tier3.age", "265");

        try {
            builder.emClusterRole(DeployEMFlowContext.EmRoleEnum.valueOf(features.get("emClusterRole").toUpperCase()));
        } catch (IllegalArgumentException e) {
            LOGGER.info("EM is installed in standalone mode.");
        }

        if (chosenFeatures.contains("Database")) {
            databaseInstalled = true;
            builder.databaseDir(rootFolder + databaseDir);

            String dbname = features.get("dbname");
            if (StringUtils.isNotBlank(dbname)) {
                builder.dbname(dbname);
            }
        }

        if (chosenFeatures.contains("WebView")) {
            webviewInstalled = true;
            builder.wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);
        }

        return builder;
    }

    private String location(Platform platform, String path) {
        String result;

        switch (platform) {
            case WINDOWS:
                if (path == null) {
                    result = null;
                } else if (path.substring(1, path.length()).startsWith(":")) {
                    result = windowsRootFolder + path.substring(2, path.length());
                } else {
                    result = path;
                }
                break;
            default:
                if (path == null) {
                    result = null;
                } else if (path.startsWith("/")) {
                    result = linuxRootFolder + path;
                } else {
                    result = path;
                }
                break;
        }
        return result;
    }

    private EmRole dbRole(String databaseDir, String version, boolean useOracle) {
        EmRole.Builder builder = new EmRoleLinuxBuilder(DB_ROLE_ID, resolver)
                .installerDir(linuxAutomation + "/installer")
                .installerTgDir(linuxAutomation + "/installer/em")
                .installDir(linuxRootFolder + "/em")
                .silentInstallChosenFeatures(Collections.singletonList("Database"))
                .version(version)
                .nostartEM()
                .nostartWV()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();

        if (StringUtils.isNotBlank(databaseDir)) {
            builder.databaseDir(databaseDir);
        } else {
            builder.databaseDir(linuxRootFolder + "/database");
        }

        if (useOracle) {
            builder.useOracle();
        }

        return builder.build();
    }

    private EmRole wvRole(String installDir, String version, EmRole momRole) {
        EmRole.Builder builder = new EmRoleLinuxBuilder(WV_ROLE_ID, resolver)
                .installerDir(linuxAutomation + "/installer")
                .installerTgDir(linuxAutomation + "/installer/em")
                .silentInstallChosenFeatures(Collections.singleton("WebView"))
                .version(version)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .wvEmHost(resolver.getHostnameById(momRole.getRoleId()))
                .wvEmPort(momRole.getEmPort())
                .nostartWV()
                .nostartEM()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();

        if (StringUtils.isNotBlank(installDir)) {
            builder.installDir(installDir);
        } else {
            builder.installDir(linuxRootFolder + "/wv");
        }

        return builder.build();
    }

    private String getVersion(DcuData.EmConfigProperties config) {
        if (StringUtils.isNotBlank(emVersion)) {
            return emVersion;
        }
        return config.getVersion();
    }

    private TestbedMachine getMachine(String id, Map<String, String> features) {
        Platform platform = Platform.LINUX;

        String platformName = features.get("platformName");
        if (StringUtils.isNotBlank(platformName)) {
            platform = Platform.fromString(platformName);
        }

        return getMachine(id, platform);
    }

    private TestbedMachine getMachine(String id, Platform platform) {
        String template;
        switch (platform) {
            case WINDOWS:
                template = TestbedMachine.TEMPLATE_WIN_SERVER_LATEST;
                break;
            default:
                template = TestbedMachine.TEMPLATE_RH_LATEST;
        }

        return new TestbedMachine.Builder(id)
                .platform(platform)
                .templateId(template)
                .bitness(Bitness.b64)
                .build();
    }
}
