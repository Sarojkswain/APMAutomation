package com.ca.apm.tests.testbed.dcu;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.apm.tests.artifact.HammondDataVersion2;
import com.ca.apm.tests.role.RestoreDataRole;
import com.ca.apm.tests.role.ScpCopyRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.tests.annotations.TestBedDynamicField;
import com.ca.tas.type.Platform;
import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by jirji01 on 6/23/2017.
 */
@TestBedDefinition
public class OldDynamicTestbed implements ITestbedFactory {

    private static final List<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-XX:MaxPermSize=256m",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2g", "-Xmx2g");

    private static final List<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-Xms2g", "-Xmx2g");

    private static final List<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1g", "-Xmx1g");


    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTestbed.class);
    private static final String DCU_FOLDER = "dcuFolder";
    private static final String BUNDLE_VERSION = "upgradeBundleVersion";

    public static final String DB_ROLE_ID = "dbRole";
    public static final String DB_MACHINE_ID = "dbMachine";
    public static final String WV_ROLE_ID = "wvRole";
    public static final String WV_MACHINE_ID = "ccMachine";
    public static final String UPGRADE_BUNDLE_ROLE_ID = "upgradeBundle";

    @TestBedDynamicField(DCU_FOLDER)
    private String dcuFolder = "c:/replay/vw-small/";

    @TestBedDynamicField(BUNDLE_VERSION)
    private String upgradeBundleVersion = "99.99.bruceWillis-SNAPSHOT";

    private Testbed testbed;
    private ITasResolver resolver;
    private int idPrefix = 0;

    @Override
    public ITestbed create(ITasResolver resolver) {
        testbed = new Testbed(getClass().getSimpleName());
        this.resolver = resolver;

        idPrefix = 0;

        try {
            LOGGER.info(Paths.get(".").toAbsolutePath().toString());
            LOGGER.info("Is directory '" + dcuFolder + "' = " + Boolean.toString(Files.isDirectory(Paths.get(dcuFolder))));

            TestbedMachine dbMachine = getMachine(DB_MACHINE_ID);
            testbed.addMachine(dbMachine);
            TestbedMachine wvMachine = getMachine(WV_MACHINE_ID);
            testbed.addMachine(wvMachine);

            // add upgrade bundle
            Map<String, String> accConfig = new HashMap<>();
            accConfig.put("repository.local", "repository");
            accConfig.put("agentController.listener.port", "8889");

            UniversalRole upgradeBundleRole = new UniversalRole.Builder(UPGRADE_BUNDLE_ROLE_ID, resolver)
                    .unpack(new DefaultArtifact("com.ca.apm.delivery", "clusterupgrade-bundle-creator.unix", "tar", upgradeBundleVersion), "/opt/automation/deployed/acc")
                    .configuration("/opt/automation/deployed/acc/config/apmccsrv.properties", accConfig)
                    .syncCommand(new RunCommandFlowContext.Builder("chmod")
                            .args(Arrays.asList("+x", "/opt/automation/deployed/acc/apmccsrv.sh"))
                            .doNotPrependWorkingDirectory()
                            .build())
                    .syncCommand(new RunCommandFlowContext.Builder("apmccsrv.sh")
                            .args(Collections.singleton("start"))
//                            .environment(Collections.singletonMap("HOME", automation))
                            .workDir("/opt/automation/deployed/acc")
                            .build())
                    .build();
            wvMachine.addRole(upgradeBundleRole);


            DcuData dcuData = new DcuData(dcuFolder);
            List<EmRole> collectors = new ArrayList<>();
            while (dcuData.hasNextDcu()) {

                dcuData.processNextDcu();
                DcuData.EmConfigProperties config = dcuData.getConfigProperties();

                if (config.getClusterMode() == DcuData.EmConfigProperties.ClusterMode.Collector) {
                    idPrefix++;

                    if (idPrefix == 1) {
                        IRole dbRole = dbBuilder(dcuData).build();
                        dbMachine.addRole(dbRole);
                    }

                    TestbedMachine emMachine = getMachine(idPrefix + "Machine");

                    testbed.addMachine(emMachine);
                    EmRole emRole = collectorBuilder(dcuData)
                            .dbhost(resolver.getHostnameById(DB_ROLE_ID))
                            .build();
                    emRole.after(testbed.getRoleById(DB_ROLE_ID));
                    emMachine.addRole(emRole);
                    collectors.add(emRole);

                    createSupportRoles(resolver, dcuData.getDcuDataFile(), config, emMachine, emRole);
                } else if (config.getClusterMode() != DcuData.EmConfigProperties.ClusterMode.MOM) {
                    throw new IllegalStateException("EM role other than MOM and Collector are not in happy path scenario.");
                }
            }
            dcuData = new DcuData(dcuFolder);
            while (dcuData.hasNextDcu()) {

                dcuData.processNextDcu();
                DcuData.EmConfigProperties config = dcuData.getConfigProperties();

                if (config.getClusterMode() == DcuData.EmConfigProperties.ClusterMode.MOM) {
                    idPrefix++;

                    TestbedMachine momMachine = getMachine(idPrefix + "Machine");
                    testbed.addMachine(momMachine);

                    EmRole momRole = momBuilder(dcuData)
                            .dbhost(resolver.getHostnameById(DB_ROLE_ID))
                            .emCollectors(collectors)
                            .build();
                    momRole.after(testbed.getRoleById(DB_ROLE_ID));
                    momMachine.addRole(momRole);

                    EmRole wvRole = wvBuilder(dcuData)
                            .wvEmHost(resolver.getHostnameById(momRole.getRoleId()))
                            .wvEmPort(momRole.getEmPort())
                            .nostartEM()
                            .nostartWV()
                            .version(config.getVersion())
                            .build();
                    wvMachine.addRole(wvRole);

                    IRole momStartRole = createSupportRoles(resolver, dcuData.getDcuDataFile(), config, momMachine, momRole);

                    ExecutionRole wvStartRole = new ExecutionRole.Builder(WV_ROLE_ID + "_start")
                            .syncCommand(wvRole.getWvRunCommandFlowContext())
                            .build();
                    wvStartRole.after(momStartRole);
                    wvMachine.addRole(wvStartRole);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Cannot load DCU config data.", e);
        }

        return testbed;
    }

    private IRole createSupportRoles(ITasResolver resolver, Path dcuData, DcuData.EmConfigProperties config, TestbedMachine emMachine, EmRole emRole) {
        ScpCopyRole scpRole = new ScpCopyRole(idPrefix + "ScpRole", dcuData, resolver.getHostnameById(emRole.getRoleId()));
        emMachine.addRole(scpRole);

        HammondRole.Builder hammondBuilder = new HammondRole.LinuxBuilder(idPrefix + "HammondRole", resolver);
        HammondRole hammondRole = hammondBuilder
                .heapMemory("1024m")
                .installDir(hammondBuilder.getLinuxDeployBase())
                .collector(resolver.getHostnameById(emRole.getRoleId()))
                .data(HammondDataVersion2.UPGRADE_EMPTY)
                .build();
        emMachine.addRole(hammondRole);

        RestoreDataRole restoreDataRole = new RestoreDataRole.Builder(idPrefix + "RestoreRole", resolver)
                .em(config.getEmDir())
                .smartstor(dataAbsolutePath(config.getEmDir(), config.getSmartStorDir()))
                .smartstorArchive(dataAbsolutePath(config.getEmDir(), config.getSmartStorArchiveDir()))
                .smartstorMeta(dataAbsolutePath(config.getEmDir(), config.getSmartStorMetadataDir()))
                .traces(dataAbsolutePath(config.getEmDir(), config.getTracesDir()))
                .baseLine(dataAbsolutePath(config.getEmDir(), config.getBaseLineFile()))
                .hammondData(HammondRole.Builder.LINUX_SOFTWARE_LOC + "hammond/" +
                        HammondDataVersion2.UPGRADE_EMPTY.getArtifact().getArtifactId() + "/" +
                        HammondDataVersion2.UPGRADE_EMPTY.getArtifact().getClassifier())
                .dbHost(resolver.getHostnameById(DB_ROLE_ID))
                .dbVersion(config.getVersion())
                .sourceData(scpRole.getDestFile())
                .build();
        restoreDataRole.after(emRole, scpRole, hammondRole);
        emMachine.addRole(restoreDataRole);

        IRole startRole = RoleUtility.addStartEmRole(emMachine, emRole, false, restoreDataRole);

        UniversalRole startHammondRole = new UniversalRole.Builder(idPrefix + "StartHammondRole", resolver)
                .syncCommand(hammondRole.getStartCommandFlowContexts().get(0))
                .build();
        startHammondRole.after(startRole);
        emMachine.addRole(startHammondRole);

        return startRole;
    }

    private String dataAbsolutePath(String emFolder, String dataFolder) {
        String result;

        if (StringUtils.isEmpty(dataFolder)) {
            result = "";
        } else if (dataFolder.startsWith("/")) {
            result = dataFolder;
        } else {
            result = emFolder + '/' + dataFolder;
        }

        return result;
    }

    private EmRole.Builder collectorBuilder(DcuData dcuData) {

        DcuData.EmConfigProperties config = dcuData.getConfigProperties();

        return new EmRole.LinuxBuilder(idPrefix + "Role", resolver)
                .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .nostartEM()
                .version(config.getVersion())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION)
                .installDir(config.getEmDir())
                .configProperty("introscope.enterprisemanager.smartstor.directory", config.getSmartStorDir())
                .configProperty("introscope.enterprisemanager.smartstor.directory.metadata", config.getSmartStorMetadataDir())
                .configProperty("introscope.enterprisemanager.smartstor.directory.archive", config.getSmartStorArchiveDir())
                .configProperty("introscope.enterprisemanager.baseline.database", config.getBaseLineFile())
                .configProperty("introscope.enterprisemanager.transactionevents.storage.dir", config.getTracesDir());
    }

    private EmRole.Builder momBuilder(DcuData dcuData) {

        DcuData.EmConfigProperties config = dcuData.getConfigProperties();

        return new EmRole.LinuxBuilder(idPrefix + "Role", resolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .version(config.getVersion())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
                .installDir(config.getEmDir())
                .configProperty("introscope.enterprisemanager.smartstor.directory", config.getSmartStorDir())
                .configProperty("introscope.enterprisemanager.smartstor.directory.metadata", config.getSmartStorMetadataDir())
                .configProperty("introscope.enterprisemanager.smartstor.directory.archive", config.getSmartStorArchiveDir())
                .configProperty("introscope.enterprisemanager.baseline.database", config.getBaseLineFile())
                .configProperty("introscope.enterprisemanager.transactionevents.storage.dir", config.getTracesDir());
    }

    private EmRole.Builder dbBuilder(DcuData dcuData) {
        return new EmRole.LinuxBuilder(DB_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Collections.singletonList("Database"))
                .version(dcuData.getConfigProperties().getVersion())
                .nostartEM()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();
    }

    private EmRole.Builder wvBuilder(DcuData dcuData) {
        return new EmRole.LinuxBuilder(WV_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Collections.singleton("WebView"))
                .version(dcuData.getConfigProperties().getVersion())
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .nostartWV()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();
    }

    private TestbedMachine getMachine(String id) {
        return new TestbedMachine.Builder(id)
                .platform(Platform.LINUX)
                .templateId("rh7")
                .bitness(Bitness.b64)
                .build();
    }
}
