package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.CollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DefaultPerfResultsCollectorPluginImpl;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DirectoryCollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.FileCollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerfTestResultCollectionConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerformanceTestResultsCollectorPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Java delegate class to move performance log files from separate nodes to a centralized location on server.
 *  
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class MoveLogsDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Move Test Results Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(MoveLogsDelegate.class);
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd-HH_mm");

    protected IAppServerPluginProvider appServerPluginProvider;

    public MoveLogsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, 
                            IAppServerPluginProvider appServerPluginProvider, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String nodeName = getNodeExecutionVariable(execution, NODE);
        final String loNodeName = getNodeExecutionVariable(execution, PowerPackConstants.LOAD_ORCHESTRATOR_CONTROLLER_NODE_PARAM_NAME);
        final String jmeterNodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String logsFolderServer = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_SERVER_PARAM_NAME);
        final String logArchiveFolder = getStringExecutionVariable(execution, PowerPackConstants.LOGS_ARCHIVE_FOLDER_PARAM_NAME);
        final String serverId = getStringExecutionVariable(execution, SERVER_ID);
        final String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        
        AppServerPlugin appServerPlugin = appServerPluginProvider.getPlugin(execution);

        AppServerConfiguration appServerConfig = appServerPlugin.getAppServerConfiguration(serverId);
        
        final String testType = getEnumExecutionVariable(execution, PowerPackConstants.TEST_TYPE_PARAM_NAME);
        String version = getEnumExecutionVariable(execution, PowerPackConstants.ARTIFACT_SPECIFICATION_PARAM_NAME);
        version = version.substring("truss:".length(), "truss:10.0".length());

        final Boolean runTypePerfMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_TYPEPERF_MONITORING_PARAM_NAME, false);
        final Boolean runJmxMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JMX_MONITORING_PARAM_NAME, false);
        final Boolean runJstatMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JSTAT_MONITORING_PARAM_NAME, false);

        final String jmxLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JMX_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMX_LOG_FILE_NAME);
        final String typePerfLogFileName = getStringExecutionVariable(execution, PowerPackConstants.TYPE_PERF_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME);
        final String jstatLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JSTAT_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JSTAT_LOG_FILE_NAME);
        final String jmeterPath = getStringExecutionVariable(execution, JMeterPlugin.JMETER_PATH_KEY);

        final String jmxLogFilePath = Paths.get(logDir, jmxLogFileName).toString();
        final String typePerfLogFilePath = Paths.get(logDir, typePerfLogFileName).toString();
        final String jstatLogFilePath = Paths.get(logDir, jstatLogFileName).toString();

        StringBuffer buf = new StringBuffer("Moving collected metrics to the server node.").
            append('\n').
            append("Agent Node Name: {0}").
            append('\n').
            append("LO Node Name: {1}").
            append('\n').
            append("Jmeter Node Name: {2}").
            append('\n').
            append("Target Server Logs Directory: {3}").
            append('\n').
            append("Server Logs Archive Directory: {4}").
            append('\n').
            append("Source Agent Log Dir: {5}").
            append('\n').
            append("Server id: {6}").
            append('\n').
            append("Test Type: {7}").
            append('\n').
            append("Artifact Version: {8}").
            append('\n').
            append("Run TypePerf monitoring: {9}").
            append('\n').
            append("Run JMX monitoring: {10}").
            append('\n').
            append("Run Jstat monitoring: {11}").
            append('\n').
            append("JMX Result Log File: {12}").
            append('\n').
            append("TypePerf Result Log File: {13}").
            append('\n').
            append("Jstat Result Log File: {14}").
            append('\n').
            append("Jmeter path: {15}");

        logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, loNodeName, jmeterNodeName, 
            logsFolderServer, logArchiveFolder, logDir, serverId, testType, version, runTypePerfMonitoring, 
            runJmxMonitoring, runJstatMonitoring, jmxLogFilePath, typePerfLogFilePath, jstatLogFilePath, jmeterPath);
        
        PerformanceTestResultsCollectorPlugin jmeterCollectorPlugin = getPluginForNode(jmeterNodeName, 
            DefaultPerfResultsCollectorPluginImpl.PLUGIN, 
            DefaultPerfResultsCollectorPluginImpl.class);

        PerformanceTestResultsCollectorPlugin agentNodeCollector = getPluginForNode(nodeName, 
            DefaultPerfResultsCollectorPluginImpl.PLUGIN,
            DefaultPerfResultsCollectorPluginImpl.class);
        
        PerformanceTestResultsCollectorPlugin loNodeCollector = getPluginForNode(loNodeName, 
            DefaultPerfResultsCollectorPluginImpl.PLUGIN, 
            DefaultPerfResultsCollectorPluginImpl.class);

        Collection<CollectionItem> jmeterCollectionItems = new ArrayList<CollectionItem>(1);
        List<String> jmeterRelatedSuffixes = Arrays.asList(".log", ".jtl", ".csv");
        jmeterCollectionItems.add(new DirectoryCollectionItem(jmeterPath, logsFolderServer, jmeterRelatedSuffixes, 
            null, true, true, true));
        PerfTestResultCollectionConfig jmeterLogCollectionConfig = new PerfTestResultCollectionConfig(jmeterCollectionItems);

        //jmeter output 
        jmeterCollectorPlugin.collect(jmeterLogCollectionConfig);
        
        if (runJmxMonitoring) {
            Collection<CollectionItem> jmxCollectionItems = new ArrayList<CollectionItem>(1);
            jmxCollectionItems.add(new FileCollectionItem(jmxLogFilePath, logsFolderServer, jmxLogFileName, null, true));
            PerfTestResultCollectionConfig jmxLogCollectionConfig = new PerfTestResultCollectionConfig(jmxCollectionItems);
            
            //jmx output
            agentNodeCollector.collect(jmxLogCollectionConfig);
        }

        if (runTypePerfMonitoring) {
            Collection<CollectionItem> typePerfCollectionItems = new ArrayList<CollectionItem>(1);
            typePerfCollectionItems.add(new FileCollectionItem(typePerfLogFilePath, logsFolderServer, typePerfLogFileName, true));
            PerfTestResultCollectionConfig typePerfLogCollectionConfig = new PerfTestResultCollectionConfig(typePerfCollectionItems);

            //typeperf output
            agentNodeCollector.collect(typePerfLogCollectionConfig);
            
        }
        
        if (runJstatMonitoring) {
            Collection<CollectionItem> jstatCollectionItems = new ArrayList<CollectionItem>(1);
            jstatCollectionItems.add(new FileCollectionItem(jstatLogFilePath, logsFolderServer, jstatLogFileName, true));
            PerfTestResultCollectionConfig jstatLogCollectionConfig = new PerfTestResultCollectionConfig(jstatCollectionItems);
            
            //jstat output
            agentNodeCollector.collect(jstatLogCollectionConfig);
        }
        
        if (!testType.equals("noAgent")) {
            String wilyLogsFolder = Paths.get(logDir, appServerConfig.defaultAgentInstallDir).toString();

            Collection<CollectionItem> wilyCollectionItems = new ArrayList<CollectionItem>(1);
            wilyCollectionItems.add(new DirectoryCollectionItem(wilyLogsFolder, logsFolderServer, true));
            PerfTestResultCollectionConfig wilyLogCollectionConfig = new PerfTestResultCollectionConfig(wilyCollectionItems);
            agentNodeCollector.collect(wilyLogCollectionConfig);
        }

        String destFolder = Paths.get(logArchiveFolder, DATE_FORMAT.format(new Date()) + "_" + serverType + 
            "_" + version + "_" + testType).toString();


        logInfo(LOG_CATEGORY, serverType, 
            "Archiving collected items into ''{0}''. This path gets saved in ''{1}'' variable.", 
            destFolder, 
            PowerPackConstants.RESULTS_FOLDER_PARAM_NAME);
        
        execution.setVariable(PowerPackConstants.RESULTS_FOLDER_PARAM_NAME, destFolder);
        
        Collection<CollectionItem> serverCollectionItems = new ArrayList<CollectionItem>(1);
        serverCollectionItems.add(new DirectoryCollectionItem(logsFolderServer, destFolder, true));
        PerfTestResultCollectionConfig serverLogCollectionConfig = new PerfTestResultCollectionConfig(serverCollectionItems);
        loNodeCollector.collect(serverLogCollectionConfig);

        writeConfigParamsToFile(execution, destFolder, serverType);

    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private void writeConfigParamsToFile(DelegateExecution execution, 
                                         String folderPath, String serverType) throws IOException {
        String testParamsPath = folderPath + "/testParams.txt";
        logInfo(LOG_CATEGORY, serverType, "Writing test params to: {0}", testParamsPath);
        
        StringBuilder sb = new StringBuilder();

        final String NEWLINE = System.getProperty("line.separator");

        Set<String> keysSet = execution.getVariables().keySet();
        String[] keys = keysSet.toArray(new String[keysSet.size()]);
        for (int z = 0; z < keys.length; z++) {
            sb.append(z).append(" : ").append(keys[z]).append(" : ")
                .append(execution.getVariable(keys[z])).append(NEWLINE);
        }

        org.apache.commons.io.FileUtils
            .writeStringToFile(new File(folderPath, "testParams.txt"), sb.toString());
    }
    
    
}