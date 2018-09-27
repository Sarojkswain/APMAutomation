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
 * Delegate to move TypePerf output metrics.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MoveTypePerfLogsDelegate extends AbstractJavaDelegate {

    public static final String LOG_CATEGORY = "PowerPack Move TypePerf Test Results Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(MoveTypePerfLogsDelegate.class);


    public MoveTypePerfLogsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String serverType =
            getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String nodeName = getNodeExecutionVariable(execution, NODE);
        final String logsFolderServer =
            getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_SERVER_PARAM_NAME);
        final String typePerfLogFileName =
            getStringExecutionVariable(execution,
                PowerPackConstants.TYPE_PERF_LOG_FILE_NAME_PARAM_NAME,
                PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME);
        final String logDir =
            getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);

        final String typePerfLogFilePath = Paths.get(logDir, typePerfLogFileName).toString();

        PerformanceTestResultsCollectorPlugin agentNodeCollector =
            getPluginForNode(nodeName, DefaultPerfResultsCollectorPluginImpl.PLUGIN,
                DefaultPerfResultsCollectorPluginImpl.class);

        StringBuffer buf =
            new StringBuffer("Moving TypePerf metrics to the server node.").append('\n')
                .append("Agent Node Name (TypePerf): {0}").append('\n')
                .append("Target Server Logs Directory: {1}").append('\n')
                .append("Source Agent Logs Directory: {2}").append('\n')
                .append("TypePerf Result Log File Name: {3}").append('\n')
                .append("Calculated TypePerf Log Path (logDir/typePerfLogFileName): {4}");

        logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, logsFolderServer, logDir,
            typePerfLogFileName, typePerfLogFilePath);

        Collection<CollectionItem> typePerfCollectionItems = new ArrayList<CollectionItem>(1);
        typePerfCollectionItems.add(new FileCollectionItem(typePerfLogFilePath, logsFolderServer,
            typePerfLogFileName, true));
        PerfTestResultCollectionConfig typePerfLogCollectionConfig =
            new PerfTestResultCollectionConfig(typePerfCollectionItems);

        agentNodeCollector.collect(typePerfLogCollectionConfig);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
