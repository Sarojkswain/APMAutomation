package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.CollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DefaultPerfResultsCollectorPluginImpl;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.FileCollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerfTestResultCollectionConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerformanceTestResultsCollectorPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Delegate to move JMX output metrics.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MoveJmxLogsDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Move JMX Test Results Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(MoveJmxLogsDelegate.class);

    public MoveJmxLogsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
                               FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String nodeName = getNodeExecutionVariable(execution, NODE);
        final String serverType =
            getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String jmxLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JMX_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMX_LOG_FILE_NAME);
        final String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        final String logsFolderServer = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_SERVER_PARAM_NAME);
        
        final String jmxLogFilePath = Paths.get(logDir, jmxLogFileName).toString();

        StringBuffer buf = new StringBuffer("Moving JMX metrics to the server node.").
            append('\n').
            append("Agent Node Name: {0}").
            append('\n').
            append("Target Server Logs Directory: {1}").
            append('\n').
            append("Source Agent Logs Directory: {2}").
            append('\n').
            append("JMX Result Log File Name: {3}").
            append('\n').
            append("Full JMX Result Log File Path: {4}");

        logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName,  
            logsFolderServer, logDir, jmxLogFileName, jmxLogFilePath);

        PerformanceTestResultsCollectorPlugin agentNodeCollector = getPluginForNode(nodeName, 
            DefaultPerfResultsCollectorPluginImpl.PLUGIN,
            DefaultPerfResultsCollectorPluginImpl.class);

        Collection<CollectionItem> jmxCollectionItems = new ArrayList<CollectionItem>(1);
        jmxCollectionItems.add(new FileCollectionItem(jmxLogFilePath, logsFolderServer, jmxLogFileName, null, true));
        PerfTestResultCollectionConfig jmxLogCollectionConfig = new PerfTestResultCollectionConfig(jmxCollectionItems);

        agentNodeCollector.collect(jmxLogCollectionConfig);

    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
