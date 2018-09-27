package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.fld.artifact.ManagementModulesArtifact;
import com.ca.apm.systemtest.fld.role.PerfMonitorRole;
import com.ca.apm.systemtest.fld.testbed.loads.FldHammond2Provider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
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

public class PerfSmallTestbed implements ITestbedFactory, FLDConstants {

    private static final String GC_EM_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog.txt";
    private static final String GC_WV_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog_wv.txt";

    private static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final int DEBUG_PORT = 4444;

    private static String XM_EM_MEM = "8g";

    private static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8",
            "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseConcMarkSweepGC",
            "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=" + DEBUG_PORT, "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms" + XM_EM_MEM,
            "-Xmx" + XM_EM_MEM, "-verbose:gc", "-Xloggc:" + GC_EM_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=200",
            "-Dorg.owasp.esapi.resources=./config/esapi",
            "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath",
            "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Xdebug",
            "-agentlib:jdwp=transport=dt_socket,address=4445,server=y,suspend=n",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily",
            "-Xms4g",
            "-Xmx4g",
            "-XX:+PrintGCDateStamps",
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-verbose:gc",
            "-Xloggc:" + GC_WV_LOG_FILE);

    private static final Logger logger = LoggerFactory.getLogger(PerfSmallTestbed.class);
    public static final String EM_ROLE_ID = "emRole";
    private static final String PERFMON_EM_ROLE_ID = "perfmonEmRole";

    public static final String EM_MACHINE_ID = "emMachine";
    private static final String EM_PERF_TEMPLATE = "w64p_16gb";

    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        final String memoryMonitorWorkDir = TasBuilder.WIN_SOFTWARE_LOC + "memory_monitor\\";
        final ITestbed testbed = new Testbed("PerfSmallTestbed");

        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, tasResolver);

        emBuilder.silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "Database", "WebView"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                .nostartEM()
                .nostartWV()
                .version(fldConfig.getEmVersion())
                .emLaxNlClearJavaOption(EM_LAXNL_JAVA_OPTION)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();
        EmRole emRole = emBuilder.build();

        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, String> mapping = new HashMap<>();
        mapping.put("performance*.csv", timestamp + "_pm.csv");
        String emInstallDir = emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR);
        mapping.put(emInstallDir + "\\logs\\perflog.txt", timestamp + "_em.csv");
        mapping.put(emInstallDir + "\\logs\\gclog.txt", timestamp + "_gclog.txt");
        mapping.put(memoryMonitorWorkDir + "gc.png", timestamp + "_mem.png");
        mapping.put(memoryMonitorWorkDir + "mm.csv", timestamp + "_mm.csv");
        List<String> command = Collections.singletonList(
                "java -Duser=admin -jar " +
                        emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\lib\\CLWorkstation.jar " +
                        "get historical data from agents matching \".*Virtual.$\" " +
                        "and metrics matching \".*In Use Post GC.*|.*SmartStor Disk Usage.*|.*EM CPU Used.*\" " +
                        "for past " + (fldConfig.getRunDuration(TimeUnit.MINUTES) + 20) + " minutes with frequency of 15 sec " +
                        ">" + memoryMonitorWorkDir + "mm.csv");
        PerfMonitorRole perfmonMomRole = new PerfMonitorRole.Builder(PERFMON_EM_ROLE_ID, tasResolver)
                .sharedFolder(null, "C:")
                .perfLogFileMapping(mapping)
                .sampleInterval("15")
                .perfLogPrepareCommands(command)
                .build();

        // set management modules on MOM
        ManagementModulesArtifact mmArtifact = new ManagementModulesArtifact(tasResolver);
        Artifact mm = mmArtifact.createArtifact().getArtifact();
        UniversalRole mmRole =
                new UniversalRole.Builder("mmConfigRol", tasResolver)
                        .unpack(mm, emInstallDir + "/modules").build();

        mmRole.before(emRole);

        ITestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
                .templateId(EM_PERF_TEMPLATE)
                .bitness(Bitness.b64)
                .platform(Platform.WINDOWS)
                .build();
        emMachine.addRole(emRole, mmRole, perfmonMomRole);
        testbed.addMachine(emMachine);

        List<FldTestbedProvider> testbedProviders = new ArrayList<>();
        testbedProviders.add(new FldHammond2Provider().updateCollRoleId(EM_ROLE_ID).loadSize(FldHammond2Provider.LoadSize.SMALL));


        // add all pre-requisite machines to the testbed
        for (FldTestbedProvider provider: testbedProviders) {
            Collection<ITestbedMachine> machines = provider.initMachines();
            if (machines != null) {
                testbed.addMachines(machines);
            }
        }
        // and initialize the roles
        for (FldTestbedProvider provider: testbedProviders) {
            provider.initTestbed(testbed, tasResolver);
        }


        return testbed;
    }
}
