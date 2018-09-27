package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack;

import static com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate.DO_NOTHING_DELEGATE;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils.MonitoringNotRunning;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.JavaDelegateUtils;
import com.ca.apm.systemtest.fld.plugin.dotnet.DotNetPlugin;
import com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate.AbstractSharePointPpDelegate;
import com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate.InstallAgentDelegate;
import com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate.UninstallAgentDelegate;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin.ConfigurationFormat;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.IAppServerPluginProvider;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JmeterDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.perfjob.AbstractPerfJob;
import com.ca.apm.systemtest.fld.plugin.windows.perfmon.WindowsPerfmonPlugin;

/**
 * PowerPack delegates pack for SharePoint.
 * 
 * @author haiva01
 */
@Component("sharePointPP")
public class SharePointPowerPackImpl extends AbstractPerfJob implements IAppServerPluginProvider {
    public static final DateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat(
        "yyyyMMdd-HHmmss.SSS'Z'", Locale.ENGLISH);
    // E.g., \\AQPP-SP01\Process(w3wp#1)\ID Process
    static final Pattern PROCESS_ID_PATTERN = Pattern
        .compile("^(?:(?:\\\\\\\\[^\\\\]+)?\\\\Process\\()(.+)(?:\\)\\\\ID Process)$");
    static final Pattern METRIC_WITH_INSTANCE_PATTERN = Pattern
        .compile("^((?:\\\\\\\\[^\\\\]+)?\\\\[^(]+\\()(.+)(\\)\\\\.+)$");
    private static final Logger log = LoggerFactory.getLogger(SharePointPowerPackImpl.class);
    private static final Map<String, String> GLOBAL_METRICS_TO_MONITOR =
        new TreeMap<String, String>() {

            private static final long serialVersionUID = -3832569932715400897L;
            {
                put("\\Processor Information(_Total)\\% Processor Time", "%CPU");
                put("\\Memory\\% Committed Bytes In Use", "Mem%CBIU");
                put("\\Memory\\Available Bytes", "MemAB");
                put("\\Memory\\Cache Bytes", "MemCach");
                put("\\Memory\\Commit Limit", "MemCL");
                put("\\Memory\\Committed Bytes", "MemCB");
            }
        };

    private static final String PERF_MONITOR_HANDLE_VAR = "perfMonitorHandle";
    private static final String TEST_TYPE_NO_AGENT = "noAgent";
    private static final String TEST_TYPE_AGENT = "Agent";
    private static final String TEST_TYPE_AGENT_PLUS_PP = "AgentPlusPP";

    static {
        FILE_NAME_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Autowired(required = false)
    protected FldLogger fldLogger;

    private static Map<String, String> metricsToMonitorForProcess(String processName) {
        final String metricPrefix = String.format("\\Process(%s)\\", processName);
        final String shortNamePrefix = processName.substring(1, Math.min(5, processName.length()));

        Map<String, String> metrics = new TreeMap<String, String>() {
            /**
             * 
             */
            private static final long serialVersionUID = -3622227263683155313L;

            {
                put(metricPrefix + "ID Process", shortNamePrefix + "PID");
                put(metricPrefix + "% Processor Time", shortNamePrefix + "%CPU");
                put(metricPrefix + "% User Time", shortNamePrefix + "%USR");
                put(metricPrefix + "% Privileged Time", shortNamePrefix + "%PRVLG");
                put(metricPrefix + "Virtual Bytes", shortNamePrefix + "VirtB");
                put(metricPrefix + "Private Bytes", shortNamePrefix + "PrivB");
            }
        };

        return metrics;
    }

    private static Map<String, String> metricsToMonitorForDotNetProcess(String processName) {
        final String metricPrefix = String.format("\\.NET CLR Memory(%s)\\", processName);
        final String shortNamePrefix = processName.substring(1, Math.min(5, processName.length()));

        Map<String, String> metrics = new TreeMap<String, String>() {
            private static final long serialVersionUID = -302809265365254651L;

            {
                put(metricPrefix + "# Bytes in all Heaps", shortNamePrefix + "HeapTB");
                put(metricPrefix + "% Time in GC", shortNamePrefix + "GC%Tim");
                put(metricPrefix + "# Induced GC", shortNamePrefix + "GC#Ind");
                put(metricPrefix + "# Gen 0 Collections", shortNamePrefix + "GCG0C");
                put(metricPrefix + "# Gen 1 Collections", shortNamePrefix + "GCG1C");
                put(metricPrefix + "# Gen 2 Collections", shortNamePrefix + "GCG2C");
                put(metricPrefix + "# Total committed Bytes", shortNamePrefix + "#TComB");
            }
        };

        metrics.putAll(metricsToMonitorForProcess(processName));

        return metrics;
    }

    private static String stripMachineNameFromMetric(String metricName) {
        if (StringUtils.startsWith("\\\\", metricName)) {
            final int bsIndex = metricName.indexOf('\\', 2);
            return metricName.substring(bsIndex);
        } else {
            return metricName;
        }
    }

    private static Map<String, String> metricsToMonitor(final boolean withAgent,
        final boolean withPp) {
        Map<String, String> metrics = new TreeMap<String, String>() {
            private static final long serialVersionUID = -6737736912955500655L;

            {
                if (withAgent) {
                    putAll(metricsToMonitorForDotNetProcess("PerfMonCollectorAgent"));
                    if (withPp) {
                        putAll(metricsToMonitorForDotNetProcess("SPMonitor"));
                    }
                }
                putAll(metricsToMonitorForDotNetProcess("w3wp*"));
                putAll(GLOBAL_METRICS_TO_MONITOR);
            }
        };
        return metrics;
    }

    private static void remapHeaders(List<String> headers, List<List<String>> samples,
        Map<String, String> iisWorkers) {
        // Map Perfmon process instance name (e.g., "w3wp#1") to process ID (e.g., "8812") and
        // then to IIS worker process name (e.g., "(applicationPool:SharePoint - 80)").

        final Map<String, String> old2newNamesMap = new TreeMap<>();
        for (int i = 0; i != headers.size(); ++i) {
            final String header = headers.get(i);

            // Is this a "Process ID" column?

            final Matcher headerMatcher = PROCESS_ID_PATTERN.matcher(header);
            if (!headerMatcher.matches()) {
                continue;
            }

            // Find first non-empty PID string in the i-th column.

            String pid = null;
            for (int row = 0; row != samples.size(); ++row) {
                final List<String> values = samples.get(row);
                String pidStr = values.get(i);
                if (StringUtils.isNotBlank(pidStr)) {
                    pid = pidStr;
                    break;
                }
            }
            if (pid == null) {
                continue;
            }

            // Get the worker name.

            final String processName = iisWorkers.get(pid);
            if (processName == null) {
                continue;
            }

            // Piece together new name.

            final String instanceName = headerMatcher.group(1);
            old2newNamesMap.put(instanceName, processName);
        }

        // Now map all instance names (e.g., "w3wp#1") to IIS worker process name (e.g., "
        // (applicationPool:SharePoint - 80)").

        for (int i = 0; i != headers.size(); ++i) {
            final String header = headers.get(i);
            final Matcher headerMatcher = METRIC_WITH_INSTANCE_PATTERN.matcher(header);
            if (!headerMatcher.matches()) {
                continue;
            }

            final String prefix = headerMatcher.group(1);
            final String instanceName = headerMatcher.group(2);
            final String suffix = headerMatcher.group(3);
            final String processName = old2newNamesMap.get(instanceName);
            String renamedColumn;
            if (processName != null) {
                renamedColumn = stripMachineNameFromMetric(prefix) + processName
                // + "/" + instanceName
                    + suffix;
            } else {
                renamedColumn = header;
            }
            headers.set(i, renamedColumn);
        }
    }

    @Override
    public AppServerPlugin getPlugin(DelegateExecution execution) {
        return null;
    }

    @Override
    public JavaDelegate installAgent() {
        return new InstallAgentDelegate(false, nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate installAgentWithPP() {
        return new InstallAgentDelegate(true, nodeManager, agentProxyFactory);
    }

    private void processSamples(File destDir, String testType, List<String> headers,
        List<List<String>> samples, Map<String, String> iisWorkers) throws IOException {
        remapHeaders(headers, samples, iisWorkers);

        FileUtils.forceMkdir(destDir);
        final Date now = new Date();
        final String fileName =
            String.format("SharePointPP-%s-%s", testType, FILE_NAME_DATE_FORMAT.format(now));

        final File csvFileName = new File(destDir, fileName + ".csv");
        log.info("Storing results into {}.", csvFileName.getAbsolutePath());
        CSVFormat csvFormat =
            CSVFormat.EXCEL.withHeader(headers.toArray(new String[headers.size()]));
        try (Writer sink = new FileWriter(csvFileName);
            CSVPrinter cvsPrinter = new CSVPrinter(sink, csvFormat)) {
            cvsPrinter.printRecords(samples);
        }

        final File workersMapFile = new File(destDir, fileName + ".workers");
        log.info("Storing workers map into {}.", workersMapFile.getAbsolutePath());
        try (Writer sink = new FileWriter(workersMapFile);
            CSVPrinter cvsPrinter = new CSVPrinter(sink, CSVFormat.EXCEL)) {
            for (Map.Entry<String, String> entry : iisWorkers.entrySet()) {
                cvsPrinter.printRecord(entry.getKey(), entry.getValue());
            }
        }
    }

    private void stopMonitoringWorker(DelegateExecution execution,
        AbstractSharePointPpDelegate delegate, Integer handleIndex) throws Exception {
        log.info("Stopping monitoring. WindowsPerfmonPlugin handle is {}", handleIndex);
        if (handleIndex != null) {
            dumpVariables(execution);
            final String destDir =
                JavaDelegateUtils.getStringExecutionVariable(execution, "logDirServer");
            // The following needs to use .toString() because testType is an enum.
            final String testType =
                JavaDelegateUtils.getEnumExecutionVariable(execution, "testType");
            final WindowsPerfmonPlugin perfmonPlugin = delegate.getWindowsPerfmonPlugin(execution);
            try {
                perfmonPlugin.stopMonitoring(handleIndex);
            } catch (MonitoringNotRunning e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Monitoring with handle {1} is not running. Exception: {0}", handleIndex);
                return;
            }
            final List<List<String>> samples = perfmonPlugin.getRawSamples(handleIndex);
            final List<String> headers = perfmonPlugin.getHeaders(handleIndex);
            if (log.isDebugEnabled()) {
                log.debug("Headers:\n{}", headers);
                log.debug("Data:\n{}", samples);
            }
            DotNetPlugin dotNetPlugin = delegate.getDotNetPlugin(execution);
            Map<String, String> iisWorkers = dotNetPlugin.getIisWorkers();
            processSamples(new File(destDir), testType, headers, samples, iisWorkers);
        }
    }

    @Override
    public JavaDelegate stopAppServer() {
        return new AbstractSharePointPpDelegate(nodeManager, agentProxyFactory) {
            @Override
            protected void handleExecution(DelegateExecution execution) throws Throwable {
                // Stop IIS.

                DotNetPlugin dotnet = getDotNetPlugin(execution);
                dotnet.stopIis();
            }
        };
    }

    @Override
    public JavaDelegate startAppServer() {
        return new AbstractSharePointPpDelegate(nodeManager, agentProxyFactory) {
            @Override
            protected void handleExecution(DelegateExecution execution) throws Throwable {
                DotNetPlugin dotnet = getDotNetPlugin(execution);
                dotnet.startIis();
            }
        };
    }

    @Override
    public JavaDelegate uninstallAgent() {
        return new UninstallAgentDelegate(nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate runJmeterTests() {
        return new AbstractSharePointPpDelegate(nodeManager, agentProxyFactory) {
            @Override
            protected void handleExecution(DelegateExecution execution) throws Throwable {
                JMeterPlugin jmeterPlugin = getJMeterPlugin(execution);
                if (!jmeterPlugin.checkIfJmeterIsInstalled()) {
                    jmeterPlugin.downloadJMeter(null);
                    jmeterPlugin.unzipJMeterZip();
                }
                jmeterPlugin.setScenarioUrl(getStringExecutionVariable(execution,
                    "jMeterScenarioUrl"));

                Integer jmeterStoppingPort =
                    getIntegerExecutionVariable(execution, JMeterPlugin.JMETER_STOPPING_PORT_KEY);
                Integer testDurationInSeconds =
                    getIntegerExecutionVariable(execution, "testDurationInSeconds");
                Integer testNumberOfCVUS =
                    getIntegerExecutionVariable(execution, "testNumberOfCVUS");

                Map<String, String> params = new TreeMap<>();
                params.put("logDir", ".");
                params.put("testDuration", testDurationInSeconds.toString());
                params.put("concurrency", testNumberOfCVUS.toString());
                params.put(JMeterPlugin.JMETER_STOPPING_PORT_KEY,
                    Integer.toString(jmeterStoppingPort));
                params.put("jmeterengine.nongui.port", Integer.toString(jmeterStoppingPort));

                String jmeterProcess = jmeterPlugin.execute(params);
                setExecutionVariable(execution, "jMeterTaskName", jmeterProcess);
                setExecutionVariable(execution, JMeterPlugin.JMETER_STOPPING_PORT_KEY,
                    jmeterStoppingPort);
            }
        };
    }

    @Override
    public JavaDelegate downloadJmeter() {
        return JmeterDelegates.getDownloadJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate installJmeter() {
        return JmeterDelegates.getInstallJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkJmeterInstallationStatus() {
        return JmeterDelegates.getCheckJmeterInstallationStatusDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkJmeterRunStatus() {
        return JmeterDelegates.getCheckJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate killJmeter() {
        return JmeterDelegates.getStopJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate cleanUp() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate moveTypePerfLogs() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate moveJmxLogs() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate moveJstatLogs() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate moveJmeterLogs() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate moveIntroscopeAgentLogs() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate archiveLogs() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate configureAgent() {
        return new AbstractSharePointPpDelegate(nodeManager, agentProxyFactory) {
            private static final String INTROSCOPEAGENT_PROFILE_TRANS =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<configuration>\n"
                    + "    <transformation id=\"1\">\n"
                    + "        <set-property name=\"introscope.autoprobe.directivesFile\" "
                    + "value=\"${#_.replace('default-full.pbl','default-typical.pbl')}\"/>\n"
                    + "    </transformation>\n"
                    + "\n"
                    + "    <files id=\"group1\">\n"
                    + "        <file>${#agentDir + '/agent/wily/IntroscopeAgent.profile'}</file>\n"
                    + "    </files>\n"
                    + "\n"
                    + "    <binding>\n"
                    + "        <transformation-ref id=\"1\"/>\n"
                    + "        <files-ref id=\"group1\"/>\n"
                    + "    </binding>\n"
                    + "</configuration>\n";

            @Override
            protected void handleExecution(DelegateExecution execution) throws Throwable {
                final FileTransformationPlugin ftPlugin = getFileTransformationPlugin(execution);
                final DotNetPlugin dotNetPlugin = getDotNetPlugin(execution);
                Map<String, Object> vars = new HashMap<>(1);
                vars.put("agentDir", dotNetPlugin.getInstallPrefix());
                ftPlugin.transform(INTROSCOPEAGENT_PROFILE_TRANS, ConfigurationFormat.XML, vars);
            }
        };
    }

    @Override
    public JavaDelegate unConfigureAgent() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate monitor() {
        // Start monitoring.
        return new AbstractSharePointPpDelegate(nodeManager, agentProxyFactory) {
            @Override
            protected void handleExecution(DelegateExecution execution) throws Throwable {
                // Log IIS worker process PIDs for later identification.

                DotNetPlugin dotNetPlugin = getDotNetPlugin(execution);
                log.info("IIS workers:\n{}", dotNetPlugin.getIisWorkers());

                // Find out what variant of the test we are running now.

                final String testType =
                    defaultString(getEnumExecutionVariable(execution, "testType"), "noAgent");

                boolean withAgent = false;
                boolean withPp = false;

                switch (testType) {
                    case TEST_TYPE_AGENT:
                        withAgent = true;
                        break;
                    case TEST_TYPE_AGENT_PLUS_PP:
                        withPp = withAgent = true;
                        break;
                    case TEST_TYPE_NO_AGENT:
                        break;
                    default:
                        throw ErrorUtils.logErrorAndReturnException(log, "Unknown test type: {0}",
                            testType);
                }

                // Start the measurements.

                int handle;
                try {
                    WindowsPerfmonPlugin perfmonPlugin = getWindowsPerfmonPlugin(execution);
                    // The type `java.util.TreeMap.KeySet`, returned by `keySet()`, is
                    // not deserializable because it requires `this` pointer. We have to
                    // provide normal List to the call.
                    handle =
                        perfmonPlugin.monitor(new ArrayList<>(metricsToMonitor(withAgent, withPp)
                            .keySet()));
                } catch (Exception ex) {
                    throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                        "Failed to start performance monitoring. Exception: {0}");
                }

                setExecutionVariable(execution, PERF_MONITOR_HANDLE_VAR, handle);
                dumpVariables(execution);
            }
        };
    }

    @Override
    public JavaDelegate checkMonitoring() {
        log.error("NOT IMPLEMENTED");
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate stopMonitoring() {
        return new AbstractSharePointPpDelegate(nodeManager, agentProxyFactory) {

            @Override
            protected void handleExecution(DelegateExecution execution) throws Throwable {
                dumpVariables(execution);
                stopMonitoringWorker(execution, this,
                    (Integer) getExecutionVariableObject(execution, PERF_MONITOR_HANDLE_VAR));
                setExecutionVariable(execution, PERF_MONITOR_HANDLE_VAR, null);
            }
        };
    }

    @Override
    public JavaDelegate buildReport() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate checkAgentConnectedEM() {
        // TODO - implement
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate groupResults() {
        return DO_NOTHING_DELEGATE;
    }

}
