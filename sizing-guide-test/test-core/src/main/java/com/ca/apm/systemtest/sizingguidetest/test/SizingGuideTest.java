package com.ca.apm.systemtest.sizingguidetest.test;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlow;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.systemtest.fld.role.MemoryMonitorRole;
import com.ca.apm.systemtest.fld.test.FLDCEMTessLoadTest;
import com.ca.apm.systemtest.sizingguidetest.flow.DeployFileFlow;
import com.ca.apm.systemtest.sizingguidetest.flow.DeployFileFlowContext;
import com.ca.apm.systemtest.sizingguidetest.flow.GenerateExcelReportFlow;
import com.ca.apm.systemtest.sizingguidetest.flow.GenerateExcelReportFlowContext;
import com.ca.apm.systemtest.sizingguidetest.flow.MetricSynthFlow;
import com.ca.apm.systemtest.sizingguidetest.flow.MetricSynthFlowContext;
import com.ca.apm.systemtest.sizingguidetest.flow.TypeperfFlow;
import com.ca.apm.systemtest.sizingguidetest.flow.TypeperfFlowContext;
import com.ca.apm.systemtest.sizingguidetest.role.TypeperfRole;
import com.ca.apm.systemtest.sizingguidetest.testbed.Constants;
import com.ca.apm.systemtest.sizingguidetest.testbed.SizingGuideTestbed;
import com.ca.apm.systemtest.sizingguidetest.testbed.regional.ConfigurationService;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

@Test
public class SizingGuideTest extends FLDCEMTessLoadTest implements Constants {

    private static Logger LOGGER = LoggerFactory.getLogger(SizingGuideTest.class);

    private static final long TEST_DURATION_MS;

    private static final String EM_MEMORY_MONITOR_ROLE_ID = "memoryMonitorRole_" + EM_MACHINE_ID;
    private static final String WV_MEMORY_MONITOR_ROLE_ID = "memoryMonitorRole_" + WV_MACHINE_ID;

    private static final int METRICSYNTH_NUMBEROFCONNECTIONGROUPS = 10;
    private static final int METRICSYNTH_NUMBEROFHOSTS = 100;
    private static final int METRICSYNTH_AGENT_COUNT = 397;
    private static final int METRICSYNTH_MINVALUEAVERAGERESPONSETIME = 5;
    private static final int METRICSYNTH_MAXVALUEAVERAGERESPONSETIME = 30;
    private static final int METRICSYNTH_NUMWARS = 40;
    private static final int METRICSYNTH_NUMEJBS = 30;
    private static final int METRICSYNTH_CREATETTCHAINSNUMCHAINS = 5;
    private static final int METRICSYNTH_CREATETTCHAINSDEPTH = 3;

    private static final String RESULTS_LOC = "c:\\sw\\results\\";
    private static final String TYPEPERF_CSV_DIR = RESULTS_LOC + "typeperf\\";
    private static final String TYPEPERF_CSV_FILE = TYPEPERF_CSV_DIR + "typeperf.csv";
    private static final String XLS_OUTPUT_FILE = TYPEPERF_CSV_DIR + "results.xls";
    private static final String RESULTS_CPU_SHEET = "agent_cpu";

    static {
        Long testDurationMs = ConfigurationService.getConfig().getTestDurationMs();
        TEST_DURATION_MS =
            testDurationMs == null
                ? TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
                : testDurationMs;
    }

    @BeforeMethod
    public void setUp() throws Exception {
        LOGGER.info("SizingGuideTest.setUp():: entry");
        runMemoryMonitoring(EM_MEMORY_MONITOR_ROLE_ID);
        runMemoryMonitoring(WV_MEMORY_MONITOR_ROLE_ID);

        startCpuMonitoring(EM_MACHINE_TYPEPERFROLE_ROLE, EM_MACHINE_ID);
        startCpuMonitoring(WV_MACHINE_TYPEPERFROLE_ROLE, WV_MACHINE_ID);
        LOGGER.info("SizingGuideTest.setUp():: exit");
    }

    @AfterMethod
    public void tearDown() throws Exception {
        LOGGER.info("SizingGuideTest.tearDown():: entry");
        stopMemoryMonitoring(EM_MEMORY_MONITOR_ROLE_ID);
        stopMemoryMonitoring(WV_MEMORY_MONITOR_ROLE_ID);

        generateExcelReport(EM_MACHINE_ID);
        generateExcelReport(WV_MACHINE_ID);
        LOGGER.info("SizingGuideTest.tearDown():: exit");
    }

    @Tas(testBeds = @TestBed(name = SizingGuideTestbed.class, executeOn = TEST_MACHINE_ID), owner = "bocto01", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"sizingGuideTest"})
    public void sizingGuideTest() throws Exception {
        LOGGER.info("SizingGuideTest.sizingGuideTest():: entry");
        LOGGER.info("SizingGuideTest.sizingGuideTest():: TEST_DURATION_MS = {} ms",
            TEST_DURATION_MS);

        startLoad();
        sleep(TEST_DURATION_MS);
        stopLoad();

        LOGGER.info("SizingGuideTest.sizingGuideTest():: exit");
    }

    @Override
    protected void startLoad() {
        LOGGER.info("SizingGuideTest.startLoad():: entry");
        long start = System.currentTimeMillis();
        runMetricSynth();
        LOGGER.info("SizingGuideTest.startLoad():: after runMetricSynth(), took {} ms",
            (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        super.startLoad(CEM_TESS_LOAD01_ROLE_ID);
        LOGGER
            .info(
                "SizingGuideTest.startLoad():: after super.startLoad(CEM_TESS_LOAD01_ROLE_ID), took {} ms",
                (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        super.startLoad(CEM_TESS_LOAD02_ROLE_ID);
        LOGGER
            .info(
                "SizingGuideTest.startLoad():: after super.startLoad(CEM_TESS_LOAD02_ROLE_ID), took {} ms",
                (System.currentTimeMillis() - start));
        LOGGER.info("SizingGuideTest.startLoad():: exit");
    }

    @Override
    protected String getLoadName() {
        return "SizingGuideTest_CEMTessLoad";
    }

    private void sleep(long duration) {
        try {
            synchronized (this) {
                LOGGER.info("SizingGuideTest.sleep():: sleeping for {} ms until {}", duration,
                    new Date(System.currentTimeMillis() + duration));
                wait(duration);
            }
        } catch (InterruptedException e) {}
    }

    private void runMemoryMonitoring(String roleId) {
        LOGGER.info("SizingGuideTest.runMemoryMonitoring():: entry");
        LOGGER.info("SizingGuideTest.runMemoryMonitoring():: roleId = {}", roleId);
        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        Map<String, String> roleProps =
            Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
        IFlowContext startFlowContext =
            deserializeFromProperties(roleId, MemoryMonitorRole.ENV_MEMORY_MONITOR_START,
                roleProps, RunMemoryMonitorFlowContext.class);
        runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.DAYS, 28);
        LOGGER.info("SizingGuideTest.runMemoryMonitoring():: exit");
    }

    private void stopMemoryMonitoring(String roleId) {
        LOGGER.info("SizingGuideTest.stopMemoryMonitoring():: entry");
        LOGGER.info("SizingGuideTest.stopMemoryMonitoring():: roleId = {}", roleId);
        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        Map<String, String> roleProps =
            Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
        IFlowContext stopFlowContext =
            deserializeFromProperties(roleId, MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
                RunMemoryMonitorFlowContext.class);
        runFlowByMachineId(machineId, flowClass, stopFlowContext);
        LOGGER.info("SizingGuideTest.stopMemoryMonitoring():: exit");
    }

    private void runMetricSynth() {
        LOGGER.info("SizingGuideTest.runMetricSynth():: entry");
        String machineId = envProperties.getMachineIdByRoleId(METRIC_SYNTH_ROLE);
        String emHost = envProperties.getMachineHostnameByRoleId(EM_ROLE);

        int numberOfConnectionGroups = METRICSYNTH_NUMBEROFCONNECTIONGROUPS;
        int numberOfHosts = METRICSYNTH_NUMBEROFHOSTS;
        int agentCount = METRICSYNTH_AGENT_COUNT;
        // long duration = TEST_DURATION_MS;
        int minValueAverageResponseTime = METRICSYNTH_MINVALUEAVERAGERESPONSETIME;
        int maxValueAverageResponseTime = METRICSYNTH_MAXVALUEAVERAGERESPONSETIME;
        int numWars = METRICSYNTH_NUMWARS;
        int numEjbs = METRICSYNTH_NUMEJBS;
        int createTTChainsNumChains = METRICSYNTH_CREATETTCHAINSNUMCHAINS;
        int createTTChainsDepth = METRICSYNTH_CREATETTCHAINSDEPTH;

        MetricSynthFlowContext metricSynthFlowContext =
            (new MetricSynthFlowContext.Builder())
                .emHost(emHost)
                .numberOfConnectionGroups(numberOfConnectionGroups)
                .numberOfHosts(numberOfHosts)
                .numberOfAgents(agentCount)
                // .duration(duration)
                .minValueAverageResponseTime(minValueAverageResponseTime)
                .maxValueAverageResponseTime(maxValueAverageResponseTime).numWars(numWars)
                .numEjbs(numEjbs).createTTChainsNumChains(createTTChainsNumChains)
                .createTTChainsDepth(createTTChainsDepth).build();
        runFlowByMachineIdAsync(machineId, MetricSynthFlow.class, metricSynthFlowContext);
        LOGGER.info("SizingGuideTest.runMetricSynth():: exit");
    }

    private void startCpuMonitoring(String roleId, String machineId) throws Exception {
        LOGGER.info("SizingGuideTest.startCpuMonitoring():: entry");
        LOGGER.info("SizingGuideTest.startCpuMonitoring():: roleId = {}, machineId = {}", roleId,
            machineId);
        RunCommandFlowContext runCommandFlowContext =
            (new RunCommandFlowContext.Builder("mkdir")).args(Arrays.asList(TYPEPERF_CSV_DIR))
                .doNotPrependWorkingDirectory().build();
        runCommandFlowByMachineId(machineId, runCommandFlowContext);
        LOGGER.info("SizingGuideTest.startCpuMonitoring():: after mkdir {}", TYPEPERF_CSV_DIR);

        TypeperfFlowContext typeperfContext =
            (TypeperfFlowContext) deserializeFlowContextFromRole(roleId, TypeperfRole.RUN_TYPEPERF,
                TypeperfFlowContext.class);
        LOGGER.info("SizingGuideTest.startCpuMonitoring():: typeperfContext = {}", typeperfContext);
        typeperfContext.setRunTime(TimeUnit.MILLISECONDS.toSeconds(TEST_DURATION_MS));
        typeperfContext.setSamplesInterval(10L);
        typeperfContext.setOutputFileName(TYPEPERF_CSV_FILE);
        runFlowByMachineIdAsync(machineId, TypeperfFlow.class, typeperfContext, TimeUnit.DAYS, 28);
        LOGGER.info("SizingGuideTest.startCpuMonitoring():: exit");
    }

    private void generateExcelReport(String machineId) {
        LOGGER.info("SizingGuideTest.generateExcelReport():: entry");
        LOGGER.info("SizingGuideTest.generateExcelReport():: machineId = {}", machineId);
        DeployFileFlowContext deployFileFlowContext =
            (new DeployFileFlowContext.Builder()).srcFile("/results_template.xls")
                .dstFilePath(new String[] {XLS_OUTPUT_FILE}).build();
        runFlowByMachineIdAsync(machineId, DeployFileFlow.class, deployFileFlowContext);
        LOGGER.info("SizingGuideTest.generateExcelReport():: after copying {} to {}",
            XLS_OUTPUT_FILE, machineId);

        GenerateExcelReportFlowContext generateExcelReportFlowContext =
            (new GenerateExcelReportFlowContext.Builder()).resultsFile(TYPEPERF_CSV_FILE)
                .templateFile(XLS_OUTPUT_FILE).reportFile(XLS_OUTPUT_FILE)
                .sheetName(RESULTS_CPU_SHEET).expectHeader(true).build();
        runFlowByMachineIdAsync(machineId, GenerateExcelReportFlow.class,
            generateExcelReportFlowContext);
        LOGGER.info("SizingGuideTest.generateExcelReport():: exit");
    }

}
