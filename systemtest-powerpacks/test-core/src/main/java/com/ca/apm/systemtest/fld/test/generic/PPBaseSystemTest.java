package com.ca.apm.systemtest.fld.test.generic;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;

import com.ca.apm.automation.common.EmailUtil;
import com.ca.apm.automation.common.SystemProperties;
import com.ca.apm.automation.common.Util;
import com.ca.apm.systemtest.fld.common.ServerStateException;
import com.ca.apm.systemtest.fld.common.WmicUtils;
import com.ca.apm.systemtest.fld.common.file.GenerateExcelReport;
import com.ca.apm.systemtest.fld.monitor.plugin.JMXMonitorPlugin;
import com.ca.apm.systemtest.fld.testbed.PowerPackSystemTestBase;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.CLWResult;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.flow.TypeperfFlow;
import com.ca.apm.tests.flow.TypeperfFlowContext;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlow;
import com.ca.apm.tests.flow.jMeter.JMeterRunFlowContext;
import com.ca.apm.tests.role.TypeperfRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Basic functionality for all TAS testNg tests intended for PowerPack system or performance
 * testing.
 * 
 * @Author rsssa02
 */
public class PPBaseSystemTest extends TasTestNgTest {

    public static final String ERROR_PATTERN = "[ERROR]";
    public static final String WARN_PATTERN = "[WARN]";

    private static final Logger LOGGER = LoggerFactory.getLogger(PPBaseSystemTest.class);

    JMXMonitorPlugin jmx = new JMXMonitorPlugin();
    private CLWBean clwBean;
    public String resultsDir;
    protected static String CLIENT_HOME = TasBuilder.WIN_SOFTWARE_LOC + "/client/";
    protected Exception error = null;
    protected int metricCheckFailCount = 0;
    public String jmxOutDir = "/jmx/";
    public String typeperfOutDir = "/typeperf/";
    public String jmeterOutDir = "/jmeter/";
    public String jstatOutDir = "/jstat/";
    public boolean typeperfReport = false;
    public boolean jstatReport = false;
    public boolean jmxReport = false;

    protected String clwJar;
    protected String containerName;
    private Set<String> agentLogFilePatterns = null;
    protected String emHost;
    protected int emPort;
    protected String emUser;
    protected String emPassword;
    protected String appserverHostname;
    protected String metric;
    protected String jmxMetrics;
    protected final String emailSubjectPattern = "[%s] [%s] PowerPack System Test";

    protected enum MetricValueType {
        MIN, MAX, AVG
    }

    protected enum MetricVerifyType {
        EXPECTED, MINEXPECTED, MAXEXPECTED, EXISTS
    }

    protected static final long STARTUP_SLEEP_MS = 30000;

    protected ArrayList<String> attachmentList = new ArrayList<>();
    protected Properties properties = new Properties(System.getProperties());
    protected long startTime;
    protected long defaultSamplesCount;
    protected long verifyMetricSleep = 600000L;//5 mins

    public final long runTime = 86400000L;//24 hours

    protected Thread jmxThread;
    protected Thread typePerfThread;
    protected Thread jmeterLoadThread;
    protected Thread jstatThread;
    private static final ThreadLocal<Long> jstatThreadId = new ThreadLocal<Long>();

    public List<Thread> activeThreads = new ArrayList<>();
    public HashMap<String, String> csvFiles = new HashMap<>();

    public PPBaseSystemTest() throws Exception {
        super();
        this.resultsDir = SystemProperties.getTestResultsDir();
        containerName = SystemProperties.getAppServerContainerType();
        jmxOutDir = resultsDir + jmxOutDir;
        typeperfOutDir = resultsDir + typeperfOutDir;
        jstatOutDir = resultsDir + jstatOutDir;
        jmeterOutDir = resultsDir + jmeterOutDir;
        this.defaultSamplesCount = (getRunTimeInSeconds() / 10);
        this.clwJar = CLIENT_HOME + "/lib/em/CLWorkstation.jar";
        this.emPort = 5001;
        this.emUser = "admin";
        this.emPassword = "";
    }

    protected void initCLWBean() {
        this.clwBean = new CLWBean(emHost, emUser, emPassword, emPort, clwJar);
    }

    @BeforeTest(alwaysRun = true)
    protected void cleanUpTestbed() {
        try {
            File dir1 = new File(jmxOutDir);
            if (!dir1.exists()) {
                LOGGER.info("Creating jmx out dir");
                dir1.mkdirs();
            } else
                Util.copyIntoIndexedDir(jmxOutDir);
            File dir3 = new File(typeperfOutDir);
            if (!dir3.exists()) {
                LOGGER.info("Creating typeperf out dir");
                dir3.mkdirs();
            } else
                Util.copyIntoIndexedDir(typeperfOutDir);
            File dir2 = new File(jstatOutDir);
            if (!dir2.exists()) {
                LOGGER.info("Creating jstat out dir");
                dir2.mkdirs();
            } else
                Util.copyIntoIndexedDir(jstatOutDir);
        } catch (IOException e) {
            LOGGER.info("Error while copying the directory into indexed dir");
            LOGGER.error("Stack Trace: " + e);
        }
    }

    protected void initLogFiles() throws Exception {
        if (agentLogFilePatterns == null) {
            agentLogFilePatterns = new HashSet<String>();
        }
        agentLogFilePatterns.add(".*IntroscopeAgent.log(.*)");
        agentLogFilePatterns.add(".*AutoProbe.log(.*)");
    }

    protected long getRunTimeInHours() {
        return TimeUnit.MILLISECONDS.toHours(runTime);
    }

    protected long getRunTimeInMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(runTime);
    }

    protected long getRunTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(runTime);
    }

    // to run flow context with extended timeout (since system test runs all day
    // long (12 - 24 hours)
    protected int flowExecutionTimeout() {
        long runTimeSeconds = (runTime / 1000) * 2;
        return (int) runTimeSeconds;
    }

    /**
     *
     * @param runTime
     */
    public void runJMXMonitoring(final long runTime) {
        LOGGER.info("Running jmx tool for memory collection!! ..");
        // final String jmxOutDir = resultsDir + "mem_collection/";
        jmxOutDir += "jmx_mem.csv";
        // adding file to generate results
        csvFiles.put(jmxOutDir, PPSystemTestConstants.RESULTS_MEMORY_JMX_SHEET);
        jmxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Starting the thread");
                jmx.monitorJmx(appserverHostname, 1099, 10000, (int) defaultSamplesCount,
                    jmxOutDir, jmxMetrics);
            }
        });
        jmxReport = true;
        jmxThread.start();
        jmxThread.setName(PPSystemTestConstants.JMX_THREAD_NAME);
        activeThreads.add(jmxThread);
    }

    /**
     *
     * @param jvmPath
     * @param processName
     * @param cmdLineName
     */
    public void runJstatMonitoring(String jvmPath, String processName, String cmdLineName) {
        Long pid = null;
        Long jstatSamples = defaultSamplesCount;
        jstatOutDir += "jstat.csv";
        try {
            pid = WmicUtils.getPid(processName, cmdLineName);
            if (pid == null) {
                throw new UnknownError("pid is null!");
            }

            final List<String> args = new ArrayList<>();
            args.add((jvmPath != null ? jvmPath + "\\bin\\" : "") + "jstat.exe");
            args.add("-gc");
            args.add(pid.toString());
            args.add("10000ms");
            args.add(jstatSamples.toString());

            jstatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runJstatThread(args, jstatOutDir);
                }
            });
            jstatThread.setName(PPSystemTestConstants.JSTAT_THREAD_NAME);
            jstatThread.start();
            jstatReport = true;
            csvFiles.put(Paths.get(jstatOutDir).toString(),
                PPSystemTestConstants.RESULTS_MEMORY_JSTAT_SHEET);
            jstatThreadId.set(jstatThread.getId());
            activeThreads.add(jstatThread);
        } catch (Exception e) {
            String msg = "Exception during jstat metric collection";
            LOGGER.error("Error", msg);
        }

    }

    /**
     *
     * @param machineId
     * @param flowContext
     */
    protected void runJmeterScript(final String machineId, final JMeterRunFlowContext flowContext) {
        LOGGER.info("Starting jmeter thread");
        jmeterLoadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runFlowByMachineId(machineId, JMeterRunFlow.class, flowContext, TimeUnit.SECONDS,
                    (int) SizeType.MAMMOTH.getTimeout());
            }
        });
        jmeterLoadThread.setName(PPSystemTestConstants.JM_LOAD_THREAD_NAME);
        activeThreads.add(jmeterLoadThread);
        jmeterLoadThread.start();
    }

    private void runJstatThread(List<String> args, String jstatLogFileName) {
        Process jstatProcess = null;
        String absoluteFilePath = new File(jstatLogFileName).getAbsolutePath();
        String jstatDir =
            absoluteFilePath.substring(0, absoluteFilePath.lastIndexOf(File.separator));

        LOGGER.info("Running cmd args " + args.toString() + " output redirected to "
            + jstatLogFileName);

        try {
            ProcessBuilder ps = new ProcessBuilder();

            ps.command(args).directory(new File(jstatDir)).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.to(new File(jstatLogFileName)));
            jstatProcess = ps.start();

            LOGGER.info("Waiting for Jstat to finish...");
            int exitCode = jstatProcess.waitFor();
            LOGGER.info("Jstat completed with exit code: {}", exitCode);
        } catch (Exception e) {
            String msg = "Error while running jstat ";
            LOGGER.error(msg + e);
        } finally {
            if (jstatProcess != null) {
                jstatProcess.destroy();
            }
        }
    }

    protected void validateMetricsMin(int minutes, int expValue) throws Exception {
        startMetricMonitor(minutes, MetricVerifyType.MINEXPECTED, expValue);
    }

    protected void validateMetricsMax(int minutes, int expValue) throws Exception {
        startMetricMonitor(minutes, MetricVerifyType.MAXEXPECTED, expValue);
    }

    protected void validateMetricsExpected(int minutes, int expValue) throws Exception {
        startMetricMonitor(minutes, MetricVerifyType.EXPECTED, expValue);
    }

    protected void validateMetricsExists(int minutes, int expValue) throws Exception {
        startMetricMonitor(minutes, MetricVerifyType.EXISTS, 0);
    }

    /**
     *
     * @param minutes
     * @param verifyType
     * @param metricVal
     */
    protected void startMetricMonitor(final int minutes, final MetricVerifyType verifyType,
        final int metricVal) throws Exception {
        if (runTime < verifyMetricSleep) verifyMetricSleep = runTime;
        try {
            do {
                try {
                    if (metric == null) {
                        throw new UnknownError("Metric returned " + metric);
                    }

                    LOGGER.info("[MetricValidator] Validator Thread will sleep until: "
                        + (verifyMetricSleep / 1000) + "s");
                    LOGGER.info("[MetricValidator] Total Run time in seconds: "
                        + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
                        + "s, approx time remaining in seconds: "
                        + TimeUnit.MILLISECONDS.toSeconds(runTime
                            - (System.currentTimeMillis() - startTime)));
                    Thread.sleep(verifyMetricSleep);
                    verifyMetrics(minutes, verifyType, metricVal);

                    //Now check if the server is not down
                    checkServerState();
                } catch (AssertionError ae) {
                    String message =
                        "[MetricValidator] Metric validation failed for: "
                            + metric
                            + " - FAILURE #"
                            + (++metricCheckFailCount)
                            + ". Max allowed failures is "
                            + (PPSystemTestConstants.VERIFY_METRICS_MAX_ALLOWED_FAILURES + " & test will be terminated!");
                    LOGGER.warn(message);
                    if (metricCheckFailCount > PPSystemTestConstants.VERIFY_METRICS_MAX_ALLOWED_FAILURES) {
                        String msg =
                            "[MetricValidator] stopped as # of metric failures exceeded max allowed.";
                        error = new Exception(msg + " Error: ", ae);
                        Thread.currentThread().interrupt();
                        break;
                    } else {
                        LOGGER.info("[MetricValidator] Validation failed, sending email");
                        EmailUtil.sendEmail("[" + containerName + "] Metric Validation failed",
                            message);
                    }
                } 
            } while (((System.currentTimeMillis() - startTime) < runTime)
                && jmeterLoadThread.isAlive());
        } catch (InterruptedException ie) {
            LOGGER.info("[startMetricValidator] thread has been interrupted: " + ie.getMessage());
        } catch (ServerStateException e) {
            String message = "ServerStateException occurred: " + e.getMessage(); 
            LOGGER.error("ServerStateException occurred, sending email: ", e);
            EmailUtil.sendEmail("ServerStateException occurred",
                message);
            testTeardown(activeThreads);
        } catch (Exception e) {
            error =
                new Exception(
                    "[MetricValidator] stopped as # of metric failures exceeded max allowed. Error: "
                        + e);
            testTeardown(activeThreads);
        }
        LOGGER.info("Metric validator stopped...!");
    }

    /**
     *
     * @param minutes
     * @param verifyType
     * @param metricVal
     * @throws Exception
     */
    @SuppressWarnings("static-access")
    protected void verifyMetrics(int minutes, MetricVerifyType verifyType, int metricVal)
        throws Exception {
        String result = null;
        long actualValue = getClwData(metric, minutes, true, MetricValueType.AVG);

        if (verifyType == verifyType.MAXEXPECTED) {
            result = "[verifyMetrics] max expected " + metricVal + ", actual " + actualValue;
            LOGGER.info(result);
            Assert.assertTrue(actualValue <= metricVal, "Metric " + metric
                + " value was less than expected, " + result);
        }
        if (verifyType == verifyType.MINEXPECTED) {
            result = "[verifyMetrics] min expected " + metricVal + ", actual " + actualValue;
            LOGGER.info(result);
            Assert.assertTrue(actualValue >= metricVal, "Metric " + metric
                + " value was more than expected: " + result);
        }
        if (verifyType == verifyType.EXPECTED) {
            result = "[verifyMetrics] expected " + metricVal + ", actual " + actualValue;
            LOGGER.info(result);
            Assert.assertEquals(actualValue, metricVal,
                "Metric expected and actual values didn't match: " + result);
        }
        if (verifyType == verifyType.EXISTS) {
            LOGGER.info("[verifyMetrics] checking for metric existence, complete, metric exists");
            Assert.assertTrue(actualValue >= metricVal, "Metric " + metric
                + " doesn't exist, actualValue: " + actualValue);
        }
    }

    protected long getClwData(String metric) throws Exception {

        String data = (new MetricUtil(metric, clwBean)).getMetricValue();

        try {
            if (data != null) {
                return Long.parseLong(data);
            }
        } catch (NumberFormatException e) {
            throw new Exception("\ngetClwData method is used to retrieve numeric data only!", e);
        }

        return -1;
    }

    protected long getClwData(String metric, int minutes, boolean average, MetricValueType valueType)
        throws Exception {

        if (minutes == 0) return getClwData(metric); // return last time slice value only
        LOGGER.info("getting clw data");
        int initValue = -1;
        long actualValue = 0;
        int countValues = 0;
        long totalTimeSliceValues = 0;
        List<CLWResult> data =
            (new MetricUtil(metric, clwBean)).getLastNMinutesMetricResults(minutes);

        try {
            if (data != null && data.size() != 0) {
                for (CLWResult clwResult : data) {
                    String value = null;

                    switch (valueType) {
                        case MIN:
                            value = clwResult.getMINValue();
                            break;
                        case MAX:
                            value = clwResult.getMAXValue();
                            break;
                        case AVG:
                            value = clwResult.getValue();
                            break;
                        default:
                            value = clwResult.getValue();
                            break;
                    }

                    if (value != null) {
                        initValue = 0;
                        if (Long.parseLong(value) != 0) {
                            actualValue += Long.parseLong(value);
                            long count = Long.parseLong(clwResult.getValueCount());
                            totalTimeSliceValues += Long.parseLong(value) * count; // computed as
                                                                                   // value * count
                                                                                   // for
                                                                                   // each time
                                                                                   // slice
                            countValues += count; // sum of all value counts to
                                                  // be used for average
                                                  // calculation
                        }
                    }
                }
                if (average && actualValue != 0) {
                    if (countValues == 0) {
                        String message =
                            "Unable to compute average for metric " + metric
                                + " as total 'Value Count' value is 0!";
                        Assert.fail(message);

                    } else {
                        actualValue =
                            Math.round((double) totalTimeSliceValues / (double) countValues);
                    }
                }
            }

        } catch (NumberFormatException e) {
            throw new Exception("\ngetClwData method is used to retrieve numeric data only!", e);
        }

        return initValue + actualValue;
    }

    protected void testTeardown(List<Thread> activeThreads) {
        if (!activeThreads.isEmpty()) {
            for (Thread activeT : activeThreads) {
                if (activeT.isAlive()) {
                    LOGGER.info("Killing active thread : " + activeT.getName());
                    activeT.interrupt();
                    // activeThreads.remove(activeT);
                }
            }
        }
    }

    /**
     *
     * @param typePerfRoleId
     * @param typePerfMachineId
     * @throws InterruptedException
     */
    protected void startTypePerfMonitor(final String typePerfRoleId, final String typePerfMachineId)
        throws InterruptedException {
        @SuppressWarnings("unused")
        boolean file = new File(typeperfOutDir).mkdirs();
        typeperfOutDir += "typeperf.csv";
        typePerfThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TypeperfFlowContext typeperfContext =
                        (TypeperfFlowContext) deserializeFlowContextFromRole(typePerfRoleId,
                            TypeperfRole.RUN_TYPEPERF, TypeperfFlowContext.class);
                    typeperfContext.setRunTime(TimeUnit.MILLISECONDS.toSeconds(runTime));
                    typeperfContext.setSamplesInterval(10L);
                    typeperfContext.setOutputFileName(typeperfOutDir);

                    runFlowByMachineIdAsync(typePerfMachineId, TypeperfFlow.class, typeperfContext,
                        TimeUnit.SECONDS, (int) SizeType.MAMMOTH.getTimeout());
                } catch (Exception e) {
                    LOGGER.info("Exception while running thread" + e);
                }
            }
        });
        typePerfThread.setName(PPSystemTestConstants.PERF_THREAD_NAME);
        typeperfReport = true;
        activeThreads.add(typePerfThread);
        typePerfThread.start();
    }

    protected void convertSpacesToCommas(String file) {
        try {
            Path path = Paths.get(file);

            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("[\\s&&[^\n\r]]+", ",");
            Files.write(path, content.getBytes(charset));
        } catch (Exception e) {
            LOGGER.error("Error while converting spaces to comma");
        }
    }

    /**
     * Collects unique lines from the provided file filtering the contents by the provided non-regex
     * token
     * and then returns the resultant collection.
     *
     * @param filePath file location
     * @param token non-regex token to filter by
     * @param cutOffToken <code>true</code> to cut off the token itself from each matched line,
     *        otherwise <code>false</code>
     * @return all matching lines
     * @throws Exception IO error occurred
     */
    public static TreeSet<String> collectUniqueLinesPerPattern(String filePath, String token,
        boolean cutOffToken) throws Exception {
        File file = new File(filePath);
        TreeSet<String> linesCol = new TreeSet<String>();
        int tokenLength = token.length();

        try (LineNumberReader lineReader = new LineNumberReader(new FileReader(file))) {
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                int tokenInd = line.indexOf(token);
                if (tokenInd != -1) {
                    if (cutOffToken) {
                        line = line.substring(tokenInd + tokenLength);
                    }
                    linesCol.add(line);
                }
            }
        }
        return linesCol;
    }

    /**
     * Generates and sends a result email containing an Excel report generated from the CSV metric
     * files
     * generated by different monitoring programs such as typeperf, jstat, etc. The result email
     * will also contain statistics on errors
     * and warnings per each agent log found if such are discovered. The log files themselves are
     * not attached to the email, instead they
     * could be found under the Resman job remoteResources' folder. This is due to the final size of
     * the log files could be quite big,
     * especially when DEBUG log level is used.
     * 
     * @return collection of overall error messages found in all the agent log files; empty
     *         collection if no errors found
     * @throws Exception
     */
    protected Collection<String> sendResultEmail() throws Exception {
        GenerateExcelReport ger = new GenerateExcelReport();
        File xlsTemplateFile = new File(CLIENT_HOME + "st_results_template.xls");
        File xlsOutputFile =
            new File(Paths.get(resultsDir, PPSystemTestConstants.xlsOutputFile).toString());

        FileUtils.copyFile(xlsTemplateFile, xlsOutputFile);

        File jstatResults = new File(jstatOutDir);
        File typePerfResults = new File(typeperfOutDir);
        File jmxResults = new File(jmxOutDir);
        String msg = "Report has been generated successfully! \n";

        // Typeperf
        if (typePerfResults.exists() && typeperfReport) {
            LOGGER.info("Generating typeperf report...");
            ger.copyResults(typePerfResults, xlsOutputFile, xlsOutputFile,
                PPSystemTestConstants.RESULTS_CPU_SHEET, true);
        }
        // JMX
        if (jmxResults.exists() && jmxReport) {
            LOGGER.info("Generating Jmx report...");
            ger.copyResults(jmxResults, xlsOutputFile, xlsOutputFile,
                PPSystemTestConstants.RESULTS_MEMORY_JMX_SHEET, true);
        }
        // JSTAT
        if (jstatResults.exists() && jstatReport) {
            LOGGER.info("Generating Jstat report...");
            ger.copyJstatResults(jstatResults, xlsOutputFile, xlsOutputFile,
                PPSystemTestConstants.RESULTS_MEMORY_JSTAT_SHEET);
        }
        if (typeperfReport) {
            if (jstatReport || jmxReport) {
                LOGGER.info("Required reports are generated and will be sent in E-Mail to - "
                    + SystemProperties.getEmailRecipients());
                msg = "Required reports were generated successfully! \n";
            }
            attachmentList.add(xlsOutputFile.toString());
        } else {
            msg = "\n May have failed to add one of the metrics to the final report. \n";
            LOGGER
                .error("Report generation failed somewhere, not the desired combination. May have failed to add one of the metrics to the final report...");
        }

        TreeSet<String> allErrors = new TreeSet<String>();

        // Find the agent logs. Parse them to include brief info on errors and warnings into the
        // email body.
        if (agentLogFilePatterns != null && agentLogFilePatterns.size() != 0) {
            String[] agentLogFiles =
                Util.getFilesMatchingPattern(SystemProperties.getAgentLogDir(),
                    agentLogFilePatterns);
            Assert.assertNotNull(agentLogFiles, "Agent log files matching patterns "
                + agentLogFilePatterns + " don't exist.");
            for (String agentLog : agentLogFiles) {
                LOGGER.info("Checking agent log " + agentLog + " for errors and warnings.");

                TreeSet<String> errors =
                    collectUniqueLinesPerPattern(agentLog, ERROR_PATTERN, true);
                TreeSet<String> warnings =
                    collectUniqueLinesPerPattern(agentLog, WARN_PATTERN, true);

                String unixStylePath = convertPathToUnixStyle(agentLog);
                msg += "\n\n Error and Warning statistics for agent log '" + unixStylePath + "': ";
                if (errors != null && !errors.isEmpty()) {
                    allErrors.addAll(errors);
                    for (String error : errors) {
                        msg += "\n    - ERROR: " + error;
                    }
                } else {
                    msg += "\n    - No errors found \n";
                }

                if (warnings != null && !warnings.isEmpty()) {
                    for (String warning : warnings) {
                        msg += "\n    - WARNING: " + warning;
                    }
                } else {
                    msg += "\n    - No warnings found \n";
                }
            }
        } else {
            LOGGER.warn("No agent logs found. Skipping agent log error and warning check.");
        }

        String testbedName =
            super.envProperties.getTestbedProperties().get(
                PowerPackSystemTestBase.TESTBED_NAME_PROP);
        if (testbedName == null) {
            testbedName = super.envProperties.getTestbedId();
        }
        
        String emailSubject = String.format(emailSubjectPattern, testbedName, containerName);

        Map<String, String> testbedEnvPropsMap = envProperties.getTestbedProperties();
        
        String resmanBuildUrl = null;//http://tas-cz-res-man:8080/resman/#/builds/EM_SAML-SiteMinder(99-99-dev)_1476365082504
        String resmanTaskUrl = null;//http://tas-cz-res-man/tasks/57ff8b1ae4b0e0e6c7ebb157

        String taskId = testbedEnvPropsMap.get("taskId");
        if (taskId != null) {
            try {
                String resmanAPIUrl = testbedEnvPropsMap.get("resmanApi");
                int resmanPortInd = resmanAPIUrl.indexOf(":8080");
                String resmanHost = resmanAPIUrl.substring(0, resmanPortInd);

                if (!resmanAPIUrl.endsWith("api")) {
                    resmanAPIUrl += "/api";
                }
                String taskRESTUrl = resmanAPIUrl + "/tasks/" + taskId;
                LOGGER.info("Fetching a task Json from the resman using REST URL: {}", taskRESTUrl);
                String rawTaskJson = IOUtils.toString(new URL(taskRESTUrl));
                if (rawTaskJson == null) {
                    LOGGER.warn("Was unable to get a task Json for taskId={} calling REST={}", taskId, taskRESTUrl);
                } else {
                    LOGGER.info("Got a task Json object: {}", rawTaskJson);
                    JsonParser jsonParser = new JsonParser();
                    JsonElement taskJsonElement = jsonParser.parse(rawTaskJson);
                    if (taskJsonElement.isJsonObject()) {
                        JsonObject taskJsonObj = taskJsonElement.getAsJsonObject();
                        String buildName = taskJsonObj.get("buildName").getAsString();
                        if (buildName != null) {
                            resmanBuildUrl = resmanHost + ":8080/resman/#/builds/" + buildName;    
                        } else {
                            LOGGER.warn("buildName not found in the obtained task Json object!");
                        }
                    }
                    resmanTaskUrl = resmanHost + "/tasks/" + taskId;
                }
            } catch (Exception e) {
                LOGGER.error("Ignoring Exception which occurred while trying to detect task and build folder URLs on Resman:", e);
            }
        }
        
        if (resmanBuildUrl != null) {
            msg += "\n\n\nResman build: " + resmanBuildUrl + " \n";    
                
        }
        if (resmanTaskUrl != null) {
            msg += "\nResman task resources: " + resmanTaskUrl + " \n"; 
        }
        
        EmailUtil.sendEmail(PPSystemTestConstants.EMAIL_SMTP_SERVER,
            SystemProperties.getEmailSender(), SystemProperties.getEmailRecipients(), emailSubject,
            msg, attachmentList);

        return allErrors;
    }
    
    /**
     * Checks an application server state (e.g. by checking its logs) 
     * and throws an exception if some problem is detected on the server side. 
     */
    protected void checkServerState() throws ServerStateException {
        //Default implementation does nothing
    }
    
    /**
     * Converts Windows-style paths into Unix-style paths.
     * 
     * E.g. converts a path like <code>"C:\\some\\\\path\\/to//elem/"</code> into
     * <code>"C:/some/path/to/elem"</code>. Any trailing slashes get cut off.
     * 
     * @param path a path which may contain both Windows-style and Unix-style path delimiters
     * @return Unix-style path
     */
    public static String convertPathToUnixStyle(String path) {
        String[] splitPath = path.split("\\\\");
        StringBuffer buf = new StringBuffer();
        for (String pathElem : splitPath) {
            if (!"".equals(pathElem.trim())) {
                buf.append(pathElem);

                buf.append('/');
            }
        }

        path = buf.toString();
        buf.setLength(0);
        splitPath = path.split("/");
        for (String pathElem : splitPath) {
            if (!"".equals(pathElem.trim())) {
                buf.append(pathElem);
                buf.append('/');
            }
        }

        path = buf.toString();
        while (path.charAt(path.length() - 1) == '/' && (path.length() - 2 > 0)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    @AfterSuite(alwaysRun = true)
    protected void activeThreadsCleanup() {
        if (!activeThreads.isEmpty()) {
            for (Thread tActive : activeThreads) {
                tActive.interrupt();
            }
        }
    }

}
