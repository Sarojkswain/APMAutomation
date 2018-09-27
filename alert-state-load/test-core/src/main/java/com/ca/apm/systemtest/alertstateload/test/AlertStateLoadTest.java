package com.ca.apm.systemtest.alertstateload.test;

import static com.ca.apm.systemtest.alertstateload.role.MetricSynthRole.ENV_COLLECTOR_HOST;
import static com.ca.apm.systemtest.alertstateload.util.AlertStatusLoadUtil.getLoadRoleIds;
import static com.ca.apm.systemtest.alertstateload.util.AlertStatusLoadUtil.getMemoryMonitorRoleId;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.alertstateload.flow.DeployFileFlow;
import com.ca.apm.systemtest.alertstateload.flow.DeployFileFlowContext;
import com.ca.apm.systemtest.alertstateload.flow.GenerateExcelReportFlow;
import com.ca.apm.systemtest.alertstateload.flow.GenerateExcelReportFlowContext;
import com.ca.apm.systemtest.alertstateload.flow.MetricSynthFlow;
import com.ca.apm.systemtest.alertstateload.flow.MetricSynthFlowContext;
import com.ca.apm.systemtest.alertstateload.flow.TypeperfFlow;
import com.ca.apm.systemtest.alertstateload.flow.TypeperfFlowContext;
import com.ca.apm.systemtest.alertstateload.role.TypeperfRole;
import com.ca.apm.systemtest.alertstateload.testbed.AlertStateLoadTestbed;
import com.ca.apm.systemtest.alertstateload.testbed.Constants;
import com.ca.apm.systemtest.alertstateload.testbed.regional.ConfigurationService;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlow;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.systemtest.fld.role.MemoryMonitorRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

@Test
public class AlertStateLoadTest extends TasTestNgTest implements Constants {

    private static Logger LOGGER = LoggerFactory.getLogger(AlertStateLoadTest.class);

    private static final long TEST_MAX_DURATION_MS = 30 * 3600000L; // 30 h

    private static final long ONE_MINUTE_MS = 1 * 60000L; // 1 min
    @SuppressWarnings("unused")
    private static final long FIVE_MINUTES_MS = 5 * 60000L; // 5 min
    private static final long TEN_MINUTES_MS = 10 * 60000L; // 10 min
    private static final long TWENTY_MINUTES_MS = 20 * 60000L; // 20 min

    private static final int AGENT_COUNT_INIT = 100;
    private static final int AGENT_COUNT_MINIMAL_INCREMENT = 100;
    private static final int AGENT_COUNT_PERCENTAGE_INCREMENT = 5;

    private static final int AGENT_COUNT_MAX = 5000;

    private static final int MINVALUEAVERAGERESPONSETIME_NOALERTS = 5;
    private static final int MAXVALUEAVERAGERESPONSETIME_NOALERTS = 30;
    private static final int MINVALUEAVERAGERESPONSETIME_ALERTS = 200;
    private static final int MAXVALUEAVERAGERESPONSETIME_ALERTS = 300;

    private static final int NUMBER_OF_CONNECTION_GROUPS = 23; // TODO - magic number

    private static final String MEMORY_MONITOR_ROLE_ID = getMemoryMonitorRoleId();

    private static final String RESULTS_LOC = "c:\\sw\\results\\";
    private static final String TYPEPERF_CSV_DIR = RESULTS_LOC + "typeperf\\";
    private static final String TYPEPERF_CSV_FILE = TYPEPERF_CSV_DIR + "typeperf.csv";

    private static final String XLS_OUTPUT_FILE = TYPEPERF_CSV_DIR + "results_template.xls";
    private static final String RESULTS_CPU_SHEET = "agent_cpu";

    private ExecutorService executor;

    private boolean runInitPhase = ConfigurationService.getConfig().isTestRunInitPhase();
    private List<String> loadRoleIds;

    @BeforeMethod
    public void setUp() throws Exception {
        LOGGER.info("AlertStateLoadTest.setUp()::");
        executor = Executors.newCachedThreadPool();
        loadRoleIds = getLoadRoleIds();
        LOGGER.info("AlertStateLoadTest.setUp():: loadRoleIds = {}", loadRoleIds);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        LOGGER.info("AlertStateLoadTest.tearDown()::");
        try {
            executor.shutdownNow();
        } catch (Exception e) {
            LOGGER.info("AlertStateLoadTest.tearDown():: exception: {}", e);
        }
    }

    @Tas(testBeds = @TestBed(name = AlertStateLoadTestbed.class, executeOn = ASL_TEST_MACHINE_ID), owner = "bocto01", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"alertStateLoad"})
    public void alertStateLoadTest() throws Exception {
        LOGGER.info("AlertStateLoadTest.alertStateLoadTest():: entry");

        runMemoryMonitoring();
        Future<?> typePerfMonitorFuture = startTypePerfMonitor();
        sleep("AlertStateLoadTest - delay after start monitoring", ONE_MINUTE_MS);

        long endTime = System.currentTimeMillis() + TEST_MAX_DURATION_MS;
        int agentCount = AGENT_COUNT_INIT;
        int agentCountPerMachine = getAgentCountPerMachine(agentCount);

        if (runInitPhase) {
            LOGGER
                .info("AlertStateLoadTest.alertStateLoadTest():: init phase - start the Hammond load and let run normally for 20 minutes without any alerts");
            // start the Hammond load and let run normally for 20 minutes without any alerts
            // 20 min
            // runHammondsNoAlerts(loadRoleIds, TWENTY_MINUTES_MS, agentCountPerMachine);
            runMetricSynthNoAlerts(loadRoleIds, TWENTY_MINUTES_MS, agentCountPerMachine);
        }

        int iteration = 0;
        while (true) {
            if (agentCount > AGENT_COUNT_MAX) {
                LOGGER
                    .info("AlertStateLoadTest.alertStateLoadTest():: max agent count was reached");
                break;
            }
            if (System.currentTimeMillis() >= endTime) {
                LOGGER
                    .info("AlertStateLoadTest.alertStateLoadTest():: max test duration was reached");
                break;
            }
            iteration++;
            LOGGER
                .info(
                    "AlertStateLoadTest.alertStateLoadTest():: iteration {}, using agentCount = {}, agentCountPerMachine = {}, max time to the end of test: {} s",
                    iteration, agentCount, agentCountPerMachine,
                    ((endTime - System.currentTimeMillis()) / 1000));

            // gradually start triggering alerts
            // - trigger continual status changes on 5% of nodes for 10 minutes
            // 10 min
            // runHammondsWithAlerts(loadRoleIds, TEN_MINUTES_MS, agentCountPerMachine);
            runMetricSynthWithAlerts(loadRoleIds, TEN_MINUTES_MS, agentCountPerMachine);

            if (System.currentTimeMillis() >= endTime) {
                LOGGER
                    .info("AlertStateLoadTest.alertStateLoadTest():: max test duration was reached");
                break;
            }

            // - let server recover for 10 minutes afterward
            LOGGER
                .info("AlertStateLoadTest.alertStateLoadTest():: let server recover for 10 minutes");
            // runHammondsNoAlerts(hammondRoleIds, TEN_MINUTES_MS, agentCountPerMachine);
            // runMetricSynthNoAlerts(hammondRoleIds, TEN_MINUTES_MS, agentCountPerMachine);
            sleep("AlertStateLoadTest.alertStateLoadTest", TEN_MINUTES_MS);

            agentCount = coumputeAgentCount(agentCount, AGENT_COUNT_PERCENTAGE_INCREMENT);
            agentCountPerMachine = getAgentCountPerMachine(agentCount);
        }

        stopMemoryMonitoring();
        typePerfMonitorFuture.cancel(true);
        sleep("AlertStateLoadTest - delay after stop monitoring", ONE_MINUTE_MS);

        generateExcelReport();

        LOGGER.info("AlertStateLoadTest.alertStateLoadTest():: exit");
    }

    private void runMetricSynthNoAlerts(List<String> roleIds, long duration,
        int agentCountPerMachine) throws Exception {
        LOGGER.info("AlertStateLoadTest.runMetricSynthNoAlerts()::");
        runMetricSynth(roleIds, duration, agentCountPerMachine, false);
    }

    private void runMetricSynthWithAlerts(List<String> roleIds, long duration,
        int agentCountPerMachine) throws Exception {
        LOGGER.info("AlertStateLoadTest.runMetricSynthWithAlerts()::");
        runMetricSynth(roleIds, duration, agentCountPerMachine, true);
    }

    private void runMetricSynth(List<String> roleIds, long duration, int agentCountPerMachine,
        boolean alerts) throws Exception {
        LOGGER.info("AlertStateLoadTest.runMetricSynth()::");
        int minValueAverageResponseTime =
            alerts ? MINVALUEAVERAGERESPONSETIME_ALERTS : MINVALUEAVERAGERESPONSETIME_NOALERTS;
        int maxValueAverageResponseTime =
            alerts ? MAXVALUEAVERAGERESPONSETIME_ALERTS : MAXVALUEAVERAGERESPONSETIME_NOALERTS;

        int numberOfConnectionGroups = NUMBER_OF_CONNECTION_GROUPS;
        int numberOfHosts = loadRoleIds.size(); // TODO - verify

        for (String roleId : loadRoleIds) {
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            String collectorHost = envProperties.getRolePropertyById(roleId, ENV_COLLECTOR_HOST);
            executor.execute(new MetricSynthRunTask(roleId, machineId, duration,
                minValueAverageResponseTime, maxValueAverageResponseTime, agentCountPerMachine,
                collectorHost, numberOfConnectionGroups, numberOfHosts));
        }
        sleep("AlertStateLoadTest.runMetricSynth", duration);
    }

    private static int coumputeAgentCount(int agentCount, int percentageIncrease) {
        return Math.max(
            Long.valueOf(Math.round(agentCount * (1 + (1.0 * percentageIncrease / 100))))
                .intValue(), AGENT_COUNT_MINIMAL_INCREMENT);
    }

    private int getAgentCountPerMachine(int agentCount) {
        return Math.max(agentCount / loadRoleIds.size(), 1);
    }

    private void runMemoryMonitoring() {
        LOGGER.info("AlertStateLoadTest.runMemoryMonitoring():: entry");
        String machineId = envProperties.getMachineIdByRoleId(MEMORY_MONITOR_ROLE_ID);
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        Map<String, String> roleProps =
            Maps.fromProperties(envProperties.getRolePropertiesById(MEMORY_MONITOR_ROLE_ID));
        IFlowContext startFlowContext =
            deserializeFromProperties(MEMORY_MONITOR_ROLE_ID,
                MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
                RunMemoryMonitorFlowContext.class);
        runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.DAYS, 28);
        LOGGER.info("AlertStateLoadTest.runMemoryMonitoring():: exit");
    }

    private void stopMemoryMonitoring() {
        LOGGER.info("AlertStateLoadTest.stopMemoryMonitoring():: entry");
        String machineId = envProperties.getMachineIdByRoleId(MEMORY_MONITOR_ROLE_ID);
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        Map<String, String> roleProps =
            Maps.fromProperties(envProperties.getRolePropertiesById(MEMORY_MONITOR_ROLE_ID));
        IFlowContext stopFlowContext =
            deserializeFromProperties(MEMORY_MONITOR_ROLE_ID,
                MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
                RunMemoryMonitorFlowContext.class);
        runFlowByMachineId(machineId, flowClass, stopFlowContext);
        LOGGER.info("AlertStateLoadTest.stopMemoryMonitoring():: exit");
    }

    private Future<?> startTypePerfMonitor() throws Exception {
        LOGGER.info("AlertStateLoadTest.startTypePerfMonitor():: entry");
        RunCommandFlowContext runCommandFlowContext =
            (new RunCommandFlowContext.Builder("mkdir")).args(Arrays.asList(TYPEPERF_CSV_DIR))
                .doNotPrependWorkingDirectory().build();
        runCommandFlowByMachineId(ASL_WV_MACHINE_ID, runCommandFlowContext);
        LOGGER.info("AlertStateLoadTest.startTypePerfMonitor():: after mkdir {}", TYPEPERF_CSV_DIR);

        Future<?> future =
            executor.submit(new TypePerfMonitorTask(ASL_TYPEPERFROLE_ROLE, ASL_WV_MACHINE_ID));
        future.get();
        LOGGER.info("AlertStateLoadTest.startTypePerfMonitor():: exit");
        return future;
    }

    private void generateExcelReport() {
        LOGGER.info("AlertStateLoadTest.generateExcelReport():: entry");
        DeployFileFlowContext deployFileFlowContext =
            (new DeployFileFlowContext.Builder()).srcFile("/results_template.xls")
                .dstFilePath(new String[] {XLS_OUTPUT_FILE}).build();

        runFlowByMachineIdAsync(ASL_WV_MACHINE_ID, DeployFileFlow.class, deployFileFlowContext);
        LOGGER.info("AlertStateLoadTest.generateExcelReport():: after copying {} to {}",
            XLS_OUTPUT_FILE, ASL_WV_MACHINE_ID);

        GenerateExcelReportFlowContext generateExcelReportFlowContext =
            (new GenerateExcelReportFlowContext.Builder()).resultsFile(TYPEPERF_CSV_FILE)
                .templateFile(XLS_OUTPUT_FILE).reportFile(XLS_OUTPUT_FILE)
                .sheetName(RESULTS_CPU_SHEET).expectHeader(true).build();
        runFlowByMachineIdAsync(ASL_WV_MACHINE_ID, GenerateExcelReportFlow.class,
            generateExcelReportFlowContext);
        LOGGER.info("AlertStateLoadTest.generateExcelReport():: exit");
    }

    // private void runHammondsNoAlerts(List<String> hammondRoleIds, long duration,
    // int agentCountPerMachine) throws Exception {
    // LOGGER.info("AlertStateLoadTest.runHammondsNoAlerts():: hammondInterval = {}",
    // TEN_MINUTES_MS);
    // runHammonds(hammondRoleIds, duration, TEN_MINUTES_MS, agentCountPerMachine, false);
    // }
    //
    // private void runHammondsWithAlerts(List<String> hammondRoleIds, long duration,
    // int agentCountPerMachine) throws Exception {
    // LOGGER.info("AlertStateLoadTest.runHammondsWithAlerts():: hammondInterval = {}",
    // FIVE_MINUTES_MS);
    // runHammonds(hammondRoleIds, duration, FIVE_MINUTES_MS, agentCountPerMachine, true);
    // }
    //
    // private void runHammonds(List<String> hammondRoleIds, long duration, long hammondInterval,
    // int agentCountPerMachine, boolean alerts) throws Exception {
    // LOGGER.info("AlertStateLoadTest.runHammonds()::");
    // Long from = alerts ? getRandomIterationStartAlerts() : getRandomIterationStart();
    // Long to = from == null ? null : (from + hammondInterval);
    // for (String hammondRoleId : hammondRoleIds) {
    // String machineId = envProperties.getMachineIdByRoleId(hammondRoleId);
    // executor.execute(new HammondRunTask(hammondRoleId, machineId, duration, from, to,
    // agentCountPerMachine));
    // }
    // sleep("AlertStateLoadTest.runHammonds", duration);
    // }
    //
    // private void startHammondLoad(String roleId, String machineId, String prefix, Long duration,
    // Long from, Long to, Integer agentCount) {
    // LOGGER
    // .info(
    // "AlertStateLoadTest.startHammondLoad():: starting hammond: roleId = {}, machineId = {}, prefix = {}, duration = {}, from = {}, to = {}, agentCount = {}",
    // roleId, machineId, prefix, duration, from, to, agentCount);
    // for (String id : getSerializedIds(roleId, ENV_HAMMOND_START)) {
    // RunCommandFlowContext rfc = deserializeCommandFlowFromRole(roleId, id);
    // rfc = modifyParamaters(rfc, prefix, duration, from, to, agentCount);
    // runCommandFlowByMachineIdAsync(machineId, rfc);
    // }
    // }
    //
    // private void stopHammondLoad(String roleId) {
    // for (String id : getSerializedIds(roleId, ENV_HAMMOND_STOP)) {
    // LOGGER.info("AlertStateLoadTest.stopHammondLoad():: stopping hammond: {}, {}", roleId,
    // id);
    // runSerializedCommandFlowFromRoleAsync(roleId, id);
    // }
    // }
    //
    // private static RunCommandFlowContext modifyParamaters(RunCommandFlowContext template,
    // String prefix, Long duration, Long from, Long to, Integer agentCount) {
    // Collection<String> args = new ArrayList<>();
    //
    // boolean prefixFilled = false;
    // boolean agentCountFilled = false;
    // boolean durationFilled = false;
    // boolean fromFilled = false;
    // boolean toFilled = false;
    //
    // Iterator<String> iterator = template.getArgs().iterator();
    // while (iterator.hasNext()) {
    // String current = iterator.next();
    // // prefix added to generated agent name
    // if ("-p".equals(current)) {
    // args.add("-p");
    // current = iterator.next();
    // if (prefix != null && !"".equals(prefix.trim())) {
    // args.add(prefix);
    // prefixFilled = true;
    // } else {
    // args.add(current);
    // }
    // }
    //
    // // agent scaling ratio
    // else if ("-s".equals(current)) {
    // args.add("-s");
    // current = iterator.next();
    // if (agentCount != null && agentCount > 0) {
    // args.add("" + agentCount);
    // agentCountFilled = true;
    // } else {
    // args.add(current);
    // }
    // }
    //
    // // playback duration (s)
    // else if ("-d".equals(current)) {
    // args.add("-d");
    // current = iterator.next();
    // if (duration != null && duration > 0) {
    // args.add("" + duration);
    // durationFilled = true;
    // } else {
    // args.add(current);
    // }
    // }
    //
    // // read data from timestamp (millis)
    // else if ("-f".equals(current)) {
    // args.add("-f");
    // current = iterator.next();
    // if (from != null && from > 0) {
    // args.add("" + from);
    // fromFilled = true;
    // } else {
    // args.add(current);
    // }
    // }
    //
    // // read data to timestamp (millis)
    // else if ("-t".equals(current)) {
    // args.add("-t");
    // current = iterator.next();
    // if (to != null && to > 0) {
    // args.add("" + to);
    // toFilled = true;
    // } else {
    // args.add(current);
    // }
    // }
    //
    // else {
    // args.add(current);
    // }
    // }
    //
    // if (!prefixFilled && prefix != null && !"".equals(prefix.trim())) {
    // args.add("-p");
    // args.add(prefix);
    // }
    // if (!agentCountFilled && agentCount != null && agentCount > 0) {
    // args.add("-s");
    // args.add("" + agentCount);
    // }
    // if (!durationFilled && duration != null && duration > 0) {
    // args.add("-d");
    // args.add("" + duration);
    // }
    // if (!fromFilled && from != null && from > 0) {
    // args.add("-f");
    // args.add("" + from);
    // }
    // if (!toFilled && to != null && to > 0) {
    // args.add("-t");
    // args.add("" + to);
    // }
    //
    // RunCommandFlowContext runCommandFlowContext =
    // (new RunCommandFlowContext.Builder(template.getExec())).args(args)
    // // .workDir(template.getWorkDir())
    // .doNotPrependWorkingDirectory()
    // // .name(template.getName())
    // .terminateOnMatch(template.getTextToMatch()).build();
    // LOGGER.info("AlertStateLoadTest.modifyParamaters():: runCommandFlowContext.getArgs() = "
    // + runCommandFlowContext.getArgs());
    // return runCommandFlowContext;
    // }
    //
    // private Iterable<String> getSerializedIds(String roleId, String prefix) {
    // Map<String, String> roleProperties =
    // Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
    // Set<String> startIds = new HashSet<>();
    // for (String key : roleProperties.keySet()) {
    // if (key.startsWith(prefix)) {
    // startIds.add(key.split("::")[0]);
    // }
    // }
    // LOGGER.debug("AlertStateLoadTest.getSerializedIds():: startIds = " + startIds);
    // return startIds;
    // }
    //
    // // this is ugly
    // private List<String> getCollectorHosts() {
    // SortedSet<String> collectorHosts = new TreeSet<>();
    // for (String roleId : loadRoleIds) {
    // for (String id : getSerializedIds(roleId, ENV_HAMMOND_START)) {
    // RunCommandFlowContext rfc = deserializeCommandFlowFromRole(roleId, id);
    // Iterator<String> iterator = rfc.getArgs().iterator();
    // while (iterator.hasNext()) {
    // String current = iterator.next();
    // if ("-c".equals(current)) {
    // current = iterator.next();
    // collectorHosts.add(current);
    // break;
    // }
    // }
    // }
    // }
    // return new ArrayList<>(collectorHosts);
    // }
    //
    // @SuppressWarnings("unused")
    // private void updateMM() {
    // LOGGER.info("AlertStateLoadTest.updateMM():: entry");
    // AlertStateLoadMMFlowContext alertStateLoadMMFlowContext =
    // (new AlertStateLoadMMFlowContext.Builder()).build();
    // runFlowByMachineIdAsync(ASL_MOM_MACHINE_ID, AlertStateLoadMMFlow.class,
    // alertStateLoadMMFlowContext);
    // LOGGER.info("AlertStateLoadTest.updateMM():: exit");
    // }
    //
    // @SuppressWarnings("unused")
    // private void sleep(long duration) {
    // sleep(null, duration);
    // }

    private synchronized void sleep(String message, long duration) {
        try {
            LOGGER.info("AlertStateLoadTest.sleep():: " + (message == null ? "" : (message + ", "))
                + "thread {} {} - sleeping for {} s", Thread.currentThread().getId(), Thread
                .currentThread().getName(), (duration / 1000));
            // Thread.sleep(duration);
            wait(duration);
            LOGGER.info("AlertStateLoadTest.sleep():: " + (message == null ? "" : (message + ", "))
                + "thread {} {} - wake up after {} s", Thread.currentThread().getId(), Thread
                .currentThread().getName(), (duration / 1000));
        } catch (InterruptedException e) {
            LOGGER.warn("AlertStateLoadTest.sleep():: could not sleep: " + e, e);
        }
    }

    private class TypePerfMonitorTask implements Runnable {
        private String roleId;
        private String machineId;

        private TypePerfMonitorTask(String typePerfRoleId, String typePerfMachineId) {
            this.roleId = typePerfRoleId;
            this.machineId = typePerfMachineId;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setName("TypePerfMonitorTask." + roleId);
                LOGGER.info("AlertStateLoadTest.TypePerfMonitorTask.run():: entry");
                TypeperfFlowContext typeperfContext =
                    (TypeperfFlowContext) deserializeFlowContextFromRole(roleId,
                        TypeperfRole.RUN_TYPEPERF, TypeperfFlowContext.class);
                LOGGER.info("AlertStateLoadTest.TypePerfMonitorTask.run():: typeperfContext = {}",
                    typeperfContext);
                typeperfContext.setRunTime(TimeUnit.MILLISECONDS.toSeconds(TEST_MAX_DURATION_MS));
                typeperfContext.setSamplesInterval(10L);
                typeperfContext.setOutputFileName(TYPEPERF_CSV_FILE);

                runFlowByMachineIdAsync(machineId, TypeperfFlow.class, typeperfContext,
                    TimeUnit.DAYS, 28);
            } catch (Exception e) {
                LOGGER.error(
                    "AlertStateLoadTest.TypePerfMonitorTask.run():: exception while running TypePerf: "
                        + e, e);
            } finally {
                LOGGER.info("AlertStateLoadTest.TypePerfMonitorTask.run():: exit");
            }
        }
    }

    private class MetricSynthRunTask implements Runnable {
        private String roleId;
        private String machineId;
        private long duration;
        private int minValueAverageResponseTime;
        private int maxValueAverageResponseTime;
        private int agentCount;
        private String emHost;
        private int numberOfConnectionGroups;
        private int numberOfHosts;

        private MetricSynthRunTask(String roleId, String machineId, long duration,
            int minValueAverageResponseTime, int maxValueAverageResponseTime, int agentCount,
            String emHost, int numberOfConnectionGroups, int numberOfHosts) {
            this.roleId = roleId;
            this.machineId = machineId;
            this.duration = duration;
            this.minValueAverageResponseTime = minValueAverageResponseTime;
            this.maxValueAverageResponseTime = maxValueAverageResponseTime;
            this.agentCount = agentCount;
            this.emHost = emHost;
            this.numberOfConnectionGroups = numberOfConnectionGroups;
            this.numberOfHosts = numberOfHosts;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setName("MetricSynthRunTask." + roleId);
                LOGGER
                    .info(
                        "AlertStateLoadTest.MetricSynthRunTask.run():: startMetricSynthLoad: roleId {}, machineId {}, minValueAverageResponseTime {}, maxValueAverageResponseTime {}, agentCount {}, emHost {}, "
                            + "numberOfConnectionGroups {}, numberOfHosts {}", roleId, machineId,
                        minValueAverageResponseTime, maxValueAverageResponseTime, agentCount,
                        emHost, numberOfConnectionGroups, numberOfHosts);

                MetricSynthFlowContext metricSynthFlowContext =
                    (new MetricSynthFlowContext.Builder()).emHost(emHost)
                        .numberOfConnectionGroups(numberOfConnectionGroups)
                        .numberOfHosts(numberOfHosts).numberOfAgents(agentCount).duration(duration)
                        .minValueAverageResponseTime(minValueAverageResponseTime)
                        .maxValueAverageResponseTime(maxValueAverageResponseTime).build();

                runFlowByMachineIdAsync(machineId, MetricSynthFlow.class, metricSynthFlowContext);
            } catch (Exception e) {
                LOGGER.error(
                    "AlertStateLoadTest.MetricSynthRunTask.run():: exception while running MetricSynth: "
                        + e, e);
            } finally {
                LOGGER.info("AlertStateLoadTest.MetricSynthRunTask.run():: exit");
            }
        }
    }

    // private class HammondRunTask implements Runnable {
    // private String roleId;
    // private String machineId;
    // private long duration;
    // private Long from;
    // private Long to;
    // private int agentCount;
    //
    // private HammondRunTask(String roleId, String machineId, long duration, Long from, Long to,
    // int agentCount) {
    // this.roleId = roleId;
    // this.machineId = machineId;
    // this.duration = duration;
    // this.from = from;
    // this.to = to;
    // this.agentCount = agentCount;
    // }
    //
    // @Override
    // public void run() {
    // try {
    // Thread.currentThread().setName("HammondRunTask." + roleId);
    //
    // LOGGER
    // .info(
    // "AlertStateLoadTest.HammondRunTask.run():: startHammondLoad: roleId {}, machineId {}, from {}, to {}, agentCount {}",
    // roleId, machineId, from, to, agentCount);
    // startHammondLoad(roleId, machineId, null, null, from, to, agentCount);
    //
    // sleep("AlertStateLoadTest.HammondRunTask.run", duration);
    //
    // LOGGER.info("AlertStateLoadTest.HammondRunTask.run():: stopHammondLoad: roleId {}",
    // roleId);
    // stopHammondLoad(roleId);
    // } catch (Exception e) {
    // LOGGER.error(
    // "AlertStateLoadTest.HammondRunTask.run():: exception while running Hammond: "
    // + e, e);
    // } finally {
    // LOGGER.info("AlertStateLoadTest.HammondRunTask.run():: exit");
    // }
    // }
    // }

}
