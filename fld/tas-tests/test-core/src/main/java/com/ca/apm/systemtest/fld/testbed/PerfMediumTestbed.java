package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.fld.artifact.ManagementModulesArtifact;
import com.ca.apm.systemtest.fld.artifact.ManagementModulesTelefonicaArtifact;
import com.ca.apm.systemtest.fld.role.PerfMonitorRole;
import com.ca.apm.systemtest.fld.testbed.loads.FldHammond2Provider;
import com.ca.apm.systemtest.fld.testbed.loads.FldHammondProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.builder.TasBuilder;
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
import com.ca.tas.type.Platform;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@TestBedDefinition(cleanUpTestBed = FLDMainClusterTestBedCleaner.class)

public class PerfMediumTestbed implements ITestbedFactory, FLDConstants {

    private static final String EM_VERSION = "10.7.0.6";

    public static final String[] EM_C_ROLES = {EM_COLL01_ROLE_ID, EM_COLL02_ROLE_ID, EM_COLL03_ROLE_ID};
    public static final String[] EM_C_MACHINES = {COLL01_MACHINE_ID, COLL02_MACHINE_ID, COLL03_MACHINE_ID};

    private static final String EM_PERF_TEMPLATE = "w64p_16gb";

    private static final String GC_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog.txt";
    private static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final int DEBUG_PORT = 4444;

    private static String XM_MOM_MEM = "8g";
    private static String XM_COL_MEM = "4g";
    private static String XM_WV_MEM = "4g";

    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + XM_MOM_MEM,
            "-Xmx" + XM_MOM_MEM, "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-Xms" + XM_COL_MEM, "-Xmx" + XM_COL_MEM, "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    protected static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=" + DEBUG_PORT + ",server=y,suspend=n",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms" + XM_WV_MEM, "-Xmx" + XM_WV_MEM,
            "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError", "-verbose:gc",
            "-Xloggc:" + GC_LOG_FILE);

    private static final Logger logger = LoggerFactory.getLogger(PerfMediumTestbed.class);

    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    @Override
    public ITestbed create(ITasResolver resolver) {

        ITestbed testbed = new Testbed("ClusterRegressionTestBed");

        EmRole.Builder momBuilder = new EmRole.Builder(EM_MOM_ROLE_ID, resolver);

        for (int i = 0; i < EM_C_ROLES.length; i++) {
            ITestbedMachine collectorMachine = new TestbedMachine.Builder(EM_C_MACHINES[i])
                    .templateId(EM_PERF_TEMPLATE)
                    .bitness(Bitness.b64)
                    .build();

            EmRole collectorRole = new EmRole.Builder(EM_C_ROLES[i], resolver)
                    .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                    .dbhost(resolver.getHostnameById(EM_DATABASE_ROLE_ID))
                    .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                    .nostartEM()
                    .nostartWV()
                    .version(EM_VERSION)
                    .ignoreStopCommandErrors()
                    .ignoreUninstallCommandErrors()
                    .emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION)
                    .configProperty("transport.buffer.input.maxNum", "2400")
                    .configProperty("transport.outgoingMessageQueueSize", "6000")
                    .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "10")
                    .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "10")
                    .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", "5000")
                    .build();

            momBuilder.emCollector(collectorRole);
            addTimeSyncRole(collectorMachine);
            testbed.addMachine(collectorMachine);
        }

        EmRole momRole = momBuilder
                .silentInstallChosenFeatures(Collections.singletonList("Enterprise Manager"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .dbhost(resolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .version(fldConfig.getEmVersion())
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .configProperty("transport.buffer.input.maxNum", "2400")
                .configProperty("transport.outgoingMessageQueueSize", "6000")
                .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "10")
                .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "10")
                .configProperty("introscope.enterprisemanager.transactiontrace.arrivalbuffer.capacity", "5000")
                .configProperty("log4j.logger.Manager.Action", "WARN,console,logfile")
                .build();

        String momInstallDir = momRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR);

        // set management modules on MOM
        Artifact mm = new ManagementModulesTelefonicaArtifact(resolver).createArtifact().getArtifact();
        UniversalRole mmRole = new UniversalRole.Builder("mmConfigRol", resolver)
                .unpack(mm, momInstallDir + "\\config\\modules")
                .build();
        mmRole.after(momRole);

        ITestbedMachine momMachine = new TestbedMachine.Builder(MOM_MACHINE_ID)
                .templateId(EM_PERF_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        momMachine.addRole(momRole, mmRole);
        addTimeSyncRole(momMachine);
        testbed.addMachine(momMachine);
        IRole startMomRole = addStartEmRole(momMachine, momRole, false, true);

        ITestbedMachine dbMachine = new TestbedMachine.Builder(DATABASE_MACHINE_ID)
                .templateId(EM_PERF_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        EmRole dbRole = new EmRole.Builder(EM_DATABASE_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Collections.singletonList("Database"))
                .version(fldConfig.getEmVersion())
                .nostartEM()
                .nostartWV()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .build();
        dbRole.before(momRole);
        dbMachine.addRole(dbRole);
        addTimeSyncRole(dbMachine);
        testbed.addMachine(dbMachine);

        ITestbedMachine webviewMachine = new TestbedMachine.Builder(WEBVIEW_MACHINE_ID)
                .templateId(EM_PERF_TEMPLATE)
                .bitness(Bitness.b64)
                .build();
        EmRole webviewRole = new EmRole.Builder(EM_WEBVIEW_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("WebView", "Database"))
                .wvEmHost(resolver.getHostnameById(EM_MOM_ROLE_ID))
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .version(fldConfig.getEmVersion())
                .nostartEM()
                .nostartWV()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .build();
        webviewRole.before(momRole);
        webviewMachine.addRole(webviewRole);
        addTimeSyncRole(webviewMachine);
        testbed.addMachine(webviewMachine);

        IRole startWebviewRole = addStartEmRole(webviewMachine, webviewRole, true, false, startMomRole);

        List<FldTestbedProvider> testbedProviders = new ArrayList<>();
        testbedProviders.add(new FldHammond2Provider().updateCollRoleId(EM_MOM_ROLE_ID).loadSize(FldHammond2Provider.LoadSize.MEDIUM));

        // add all pre-requisite machines to the testbed
        for (FldTestbedProvider provider : testbedProviders) {
            Collection<ITestbedMachine> machines = provider.initMachines();
            if (machines != null) {
                testbed.addMachines(machines);
            }
        }
        // and initialize the roles
        for (FldTestbedProvider provider : testbedProviders) {
            provider.initTestbed(testbed, resolver);
        }


        return testbed;
    }

    private void addTimeSyncRole(ITestbedMachine machine) {
        RunCommandFlowContext timeSyncFlowContext = new RunCommandFlowContext.Builder("cmd")
                .args(Arrays.asList(
                        "/C",
                        "net start w32time & " +
                                "w32tm /config /manualpeerlist:isltime02.ca.com & " +
                                "net stop w32time & " +
                                "net start w32time & " +
                                "w32tm /config /update & " +
                                "w32tm /resync /force"))
                .doNotPrependWorkingDirectory()
                .dontUseWindowsShell()
                .build();
        ExecutionRole timeSyncRole =
                new ExecutionRole.Builder("timesync_" + machine.getMachineId()).syncCommand(
                        timeSyncFlowContext).build();
        timeSyncRole.before(Arrays.asList(machine.getRoles()));
        machine.addRole(timeSyncRole);
    }

    private IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWV,
                                 boolean startEM, IRole...beforeRole) {
        //starts EM or WebView
        ExecutionRole.Builder builder =
                new ExecutionRole.Builder(emRole.getRoleId() + "_start");

        if (startEM) {
            builder.syncCommand(emRole.getEmRunCommandFlowContext());
        }
        if (startWV) {
            builder.syncCommand(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        emRole.before(startRole);

        if (beforeRole != null) {
            startRole.after(Arrays.asList(beforeRole));
        }

        machine.addRole(startRole);
        return startRole;
    }

}
