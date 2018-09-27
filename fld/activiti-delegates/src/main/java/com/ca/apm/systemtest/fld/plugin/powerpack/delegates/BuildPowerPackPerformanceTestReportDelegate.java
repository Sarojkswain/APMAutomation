package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.reporting.PowerPackReportPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.reporting.ResultParseConfig;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * This delegate creates an XLS report file with the performance result for 
 * Introscope Agent installed on WebLogic App Server. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class BuildPowerPackPerformanceTestReportDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Build Report Delegate";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildPowerPackPerformanceTestReportDelegate.class);
    
    public BuildPowerPackPerformanceTestReportDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String loNodeName = getNodeExecutionVariable(execution, PowerPackConstants.LOAD_ORCHESTRATOR_CONTROLLER_NODE_PARAM_NAME);
        String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        String reportFileUrl = getStringExecutionVariable(execution, PowerPackConstants.REPORT_TEMPLATE_URL_PARAM_NAME);
        
        String typePerfLogFileName = getStringExecutionVariable(execution, PowerPackConstants.TYPE_PERF_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME);
        String jmxLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JMX_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMX_LOG_FILE_NAME);
        String jstatLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JSTAT_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JSTAT_LOG_FILE_NAME);
        String resultReportFileName = getStringExecutionVariable(execution, PowerPackConstants.RESULT_REPORT_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_POWER_PACK_FINAL_RESULT_REPORT_FILE_NAME);
        
        String archiveFolder = getStringExecutionVariable(execution, PowerPackConstants.LOGS_ARCHIVE_FOLDER_PARAM_NAME);
        String groupedFolder = getStringExecutionVariable(execution, PowerPackConstants.GROUPED_LOGS_FOLDER_PARAM_NAME);
        Integer monitoredProcessId = getIntegerExecutionVariable(execution, PowerPackConstants.MONITORED_PROCESS_ID_PARAM_NAME);
        
        final Boolean runTypePerfMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_TYPEPERF_MONITORING_PARAM_NAME, false);
        final Boolean runJmxMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JMX_MONITORING_PARAM_NAME, false);
        final Boolean runJstatMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JSTAT_MONITORING_PARAM_NAME, false);

        final Boolean buildJmeterReport = getBooleanExecutionVariable(execution, PowerPackConstants.BUILD_JMETER_REPORT_PARAM_NAME, true); 
        final Boolean buildTypePerfReport = getBooleanExecutionVariable(execution, PowerPackConstants.BUILD_TYPEPERF_REPORT_PARAM_NAME, runTypePerfMonitoring);
        final Boolean buildJmxReport = getBooleanExecutionVariable(execution, PowerPackConstants.BUILD_JMX_REPORT_PARAM_NAME, runJmxMonitoring);
        final Boolean buildJstatReport = getBooleanExecutionVariable(execution, PowerPackConstants.BUILD_JSTAT_REPORT_PARAM_NAME, runJstatMonitoring);
            
        final Boolean runNewAgentWithPPTest = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_NEW_AGENT_WITH_POWER_PACK_TEST_PARAM_NAME, true);
        final Boolean runNewAgentTest = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_NEW_AGENT_TEST_PARAM_NAME, true);
        final Boolean runNoAgentTest = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_NO_AGENT_TEST_PARAM_NAME, true);
        final Boolean runOldAgentWithPPTest = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_OLD_AGENT_WITH_POWER_PACK_TEST_PARAM_NAME, true);
        final Boolean runOldAgentTest = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_OLD_AGENT_TEST_PARAM_NAME, true);
        
        StringBuffer strBuf = new StringBuffer().append("LO node name: {0}").
            append('\n').
            append("Report file URL: {1}").
            append('\n').
            append("TypePerf log file name: {2}").
            append('\n').
            append("JMX log file name: {3}").
            append('\n').
            append("Jstat log file name: {4}").
            append('\n').
            append("Final result report file name: {5}").
            append('\n').
            append("Archive folder: {6}").
            append('\n').
            append("Grouped folder: {7}").
            append('\n').
            append("Monitored process id: {8}").
            append('\n').
            append("Build Jmeter report: {9}").
            append('\n').
            append("Build TypePerf report: {10}").
            append('\n').
            append("Build JMX report: {11}").
            append('\n').
            append("Build Jstat report: {12}").
            append('\n').
            append("Run New Agent with PowerPack test: {13}").
            append('\n').
            append("Run New Agent test: {14}").
            append('\n').
            append("Run No Agent test: {15}").
            append('\n').
            append("Run Old Agent with PowerPack test: {16}").
            append('\n').
            append("Run Old Agent test: {17}");
        
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), loNodeName, reportFileUrl, 
            typePerfLogFileName, jmxLogFileName, jstatLogFileName, resultReportFileName, 
            archiveFolder, groupedFolder, monitoredProcessId, 
            buildJmeterReport, buildTypePerfReport, buildJmxReport, buildJstatReport,
            runNewAgentWithPPTest, runNewAgentTest, runNoAgentTest, runOldAgentWithPPTest, runOldAgentTest);
        
        PowerPackReportPlugin ppReportPlugin = getPluginForNode(loNodeName, PowerPackReportPlugin.PLUGIN,
            PowerPackReportPlugin.class);
        
        if (groupedFolder == null) {
            groupedFolder = archiveFolder;
        }

        logInfo(LOG_CATEGORY, serverType, "Building Excel report file started");

        ResultParseConfig parseConfig = new ResultParseConfig(typePerfLogFileName, jmxLogFileName, jstatLogFileName,
            reportFileUrl, null, groupedFolder, monitoredProcessId != null ? monitoredProcessId.longValue() : null, 
                resultReportFileName, buildTypePerfReport, buildJmxReport, buildJstatReport, buildJmeterReport,
                runNewAgentWithPPTest, runNewAgentTest, runOldAgentWithPPTest, runOldAgentTest, runNoAgentTest);
        ppReportPlugin.generateReport(parseConfig);

        logInfo(LOG_CATEGORY, serverType, "Building Excel report file finished");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
