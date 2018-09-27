package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.io.IOException;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.JmxMetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.JmxMonitoringConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.JstatMetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.JstatMonitoringConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.TypePerfMetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.TypePerfMonitoringConfig;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/** 
 * Common start monitoring Java delegate.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class StartMonitoringDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = StartMonitoringDelegate.class.getSimpleName();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StartMonitoringDelegate.class);
    
    public StartMonitoringDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(final DelegateExecution execution) throws Throwable {
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        logInfo(LOG_CATEGORY, serverType, "Starting performance monitoring");
        
        final Boolean runTypePerfMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_TYPEPERF_MONITORING_PARAM_NAME, false);
        final Boolean runJmxMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JMX_MONITORING_PARAM_NAME, false);
        final Boolean runJstatMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JSTAT_MONITORING_PARAM_NAME, false);

        StringBuffer strBuf = new StringBuffer().append("Starting performance monitoring.").
            append('\n').
            append("Run typeperf monitoring: {0}").
            append('\n').
            append("Run JMX monitoring: {1}").
            append('\n').
            append("Run jstat monitoring: {2}");
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), runTypePerfMonitoring, runJmxMonitoring, runJstatMonitoring);
        
        if (!runTypePerfMonitoring && !runJmxMonitoring && !runJstatMonitoring) {
            String msg = "No monitor types provided! You must specify at least one of them!";
            logError(LOG_CATEGORY, serverType, msg);
            throw new BpmnError(msg);
        }

        if (runTypePerfMonitoring) {
            runTypePerfCollection(execution, serverType);
        }
        if (runJmxMonitoring) {
            runJmxCollection(execution, serverType);
        }
        if (runJstatMonitoring) {
            runJstatCollection(execution, serverType);
        }
    }

    protected void runJstatCollection(DelegateExecution execution, final String serverType) throws IOException {
        String nodeName = getNodeExecutionVariable(execution, NODE);
        
        MetricGatheringPlugin jstatMetricGatherer = getPluginForNode(nodeName,
            JstatMetricGatheringPlugin.PLUGIN, JstatMetricGatheringPlugin.class);

        final String cmdLineKey = getStringExecutionVariable(execution, PowerPackConstants.PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME, 
            PowerPackConstants.DEFAULT_PROCESS_COMMAND_LINE_PATTERN);
        final String procNamePattern = getStringExecutionVariable(execution, PowerPackConstants.PROCESS_NAME_PARAM_NAME);
        final Integer sampleIntervalMillis = getIntegerExecutionVariable(execution, PowerPackConstants.SAMPLE_INTERVAL_PARAM_NAME, 
            PowerPackConstants.DEFAULT_SAMPLES_INTERVAL_MILLIS);
        final Integer samplesCount = getIntegerExecutionVariable(execution, PowerPackConstants.SAMPLES_COUNT_PARAM_NAME, 
            PowerPackConstants.DEFAULT_SAMPLES_COUNT);
        
        String outputLogDirPath = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        final String jstatLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JSTAT_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JSTAT_LOG_FILE_NAME);

        StringBuffer strBuf = new StringBuffer("Starting Jstat monitoring on node {0}").
            append('\n').
            append("Cmd line key: {1}").
            append('\n').
            append("Process name pattern: {2}").
            append('\n').
            append("Sample interval: {3,number,#} milliseconds").
            append('\n').
            append("Samples count: {4,number,#}").
            append('\n').
            append("Log dir: {5}").
            append('\n').
            append("Jstat log file name: {6}");
            
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), nodeName, cmdLineKey, procNamePattern, 
            sampleIntervalMillis, samplesCount, outputLogDirPath, jstatLogFileName);
        
        JstatMonitoringConfig config = new JstatMonitoringConfig(outputLogDirPath, sampleIntervalMillis,
            samplesCount, jstatLogFileName, procNamePattern, cmdLineKey);

        jstatMetricGatherer.runMonitoring(config);

        logInfo("Run jstat collection - end");
    }

    protected void runJmxCollection(final DelegateExecution execution, final String serverType) {
        String nodeName = getNodeExecutionVariable(execution, NODE);

        logInfo(LOG_CATEGORY, serverType, "Starting JMX monitoring on node {0}", nodeName);

        final MetricGatheringPlugin jmxMetricGatherer = getPluginForNode(nodeName, 
            JmxMetricGatheringPlugin.PLUGIN, JmxMetricGatheringPlugin.class);
        
        final String host = getStringExecutionVariable(execution, PowerPackConstants.APP_SERVER_HOST_PARAM_NAME);
        final Integer port = getIntegerExecutionVariable(execution, PowerPackConstants.JMX_PORT_PARAM_NAME);
        final String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        final Integer sampleIntervalMillis = getIntegerExecutionVariable(execution, PowerPackConstants.SAMPLE_INTERVAL_PARAM_NAME, 
            PowerPackConstants.DEFAULT_SAMPLES_INTERVAL_MILLIS);
        final Integer samplesCount = getIntegerExecutionVariable(execution, PowerPackConstants.SAMPLES_COUNT_PARAM_NAME, 
            PowerPackConstants.DEFAULT_SAMPLES_COUNT);
        
        
        final String jmxMetrics = getStringExecutionVariable(execution, PowerPackConstants.JMX_METRICS_PARAM_NAME);
        final String jmxLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JMX_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMX_LOG_FILE_NAME);

        StringBuffer strBuf = new StringBuffer().append("Host: {0}").
            append('\n').
            append("Port: {1,number,#}").
            append('\n').
            append("Sample interval: {2,number,#} milliseconds").
            append('\n').
            append("Samples count: {3,number,#}").
            append('\n').
            append("Log dir: {4}").
            append('\n').
            append("JMX metrics: {5}").
            append('\n').
            append("JMX log file name: {6}");
            
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), host, port, sampleIntervalMillis, samplesCount, logDir, jmxMetrics, jmxLogFileName);
            
        JmxMonitoringConfig config = new JmxMonitoringConfig(logDir, jmxMetrics, host, jmxLogFileName, 
            port, sampleIntervalMillis, samplesCount);
        jmxMetricGatherer.runMonitoring(config);
    }

    protected void runTypePerfCollection(final DelegateExecution execution, final String serverType) {
        String nodeName = getNodeExecutionVariable(execution, NODE);

        logInfo(LOG_CATEGORY, serverType, "Starting TypePerf monitoring on node {0}", nodeName);

        final MetricGatheringPlugin typePerfMetricGatherer = getPluginForNode(nodeName, 
            TypePerfMetricGatheringPlugin.PLUGIN, TypePerfMetricGatheringPlugin.class);

        final String cmdLineKey = getStringExecutionVariable(execution, PowerPackConstants.PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME, 
            PowerPackConstants.DEFAULT_PROCESS_COMMAND_LINE_PATTERN);
        
        final String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        final String typePerfLogFileName = getStringExecutionVariable(execution, PowerPackConstants.TYPE_PERF_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME);
        final Integer sampleIntervalMillis = getIntegerExecutionVariable(execution, PowerPackConstants.SAMPLE_INTERVAL_PARAM_NAME, 
            PowerPackConstants.DEFAULT_SAMPLES_INTERVAL_MILLIS);
        final Integer samplesCount = getIntegerExecutionVariable(execution, PowerPackConstants.SAMPLES_COUNT_PARAM_NAME, 
            PowerPackConstants.DEFAULT_SAMPLES_COUNT);

        String processInstanceName = getStringExecutionVariable(execution, PowerPackConstants.PROCESS_INSTANCE_NAME_PARAM_NAME);
        String procNamePattern = getStringExecutionVariable(execution, PowerPackConstants.PROCESS_NAME_PARAM_NAME);
        String javaProcNamePattern = getStringExecutionVariable(execution, PowerPackConstants.JAVA_PROCESS_NAME_PARAM_NAME);
        
        String typePerfCounters = getStringExecutionVariable(execution, PowerPackConstants.TYPE_PERF_COUNTERS_PARAM_NAME, 
            PowerPackConstants.DEFAULT_TYPE_PERF_COUNTERS);
        
        StringBuffer strBuf = new StringBuffer().append("Command line arguments: {0}").
            append('\n').
            append("Process instance name: {1}").
            append('\n').
            append("Process name pattern: {2}").
            append('\n').
            append("Java process name pattern: {3}").
            append('\n').
            append("Sample interval: {4,number,#} milliseconds").
            append('\n').
            append("Samples count: {5,number,#}").
            append('\n').
            append("Log dir: {6}").
            append('\n').
            append("TypePerf log file name: {7}").
            append('\n').
            append("TypePerf counters: {8}");
        
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), cmdLineKey, processInstanceName, procNamePattern, 
            javaProcNamePattern, sampleIntervalMillis, samplesCount, logDir, typePerfLogFileName, typePerfCounters);
        

        TypePerfMonitoringConfig config = new TypePerfMonitoringConfig(sampleIntervalMillis, samplesCount, cmdLineKey, logDir, 
            typePerfLogFileName, procNamePattern, javaProcNamePattern, processInstanceName, typePerfCounters);
        
        Long pid = typePerfMetricGatherer.runMonitoring(config);
        execution.setVariable(PowerPackConstants.MONITORED_PROCESS_ID_PARAM_NAME, pid);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
