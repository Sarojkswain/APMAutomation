package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.CollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DefaultPerfResultsCollectorPluginImpl;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DirectoryCollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerfTestResultCollectionConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerformanceTestResultsCollectorPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Delegate to move Introscope Agent logs.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MoveWilyLogsDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Move Introscope Agent Logs Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(MoveWilyLogsDelegate.class);

    protected IAppServerPluginProvider appServerPluginProvider;

    public MoveWilyLogsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        FldLogger fldLogger, IAppServerPluginProvider appServerPluginProvider) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String serverType =
            getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String testType =
            getEnumExecutionVariable(execution, PowerPackConstants.TEST_TYPE_PARAM_NAME);

        if (!PowerPackConstants.NO_AGENT_TEST_TYPE.equals(testType)) {
            final String nodeName = getNodeExecutionVariable(execution, NODE);
            final String serverId = getStringExecutionVariable(execution, SERVER_ID);
            final String logDir =
                getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
            final String logsFolderServer =
                getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_SERVER_PARAM_NAME);

            AppServerPlugin appServerPlugin = appServerPluginProvider.getPlugin(execution);

            AppServerConfiguration appServerConfig =
                appServerPlugin.getAppServerConfiguration(serverId);

            if (appServerConfig == null) {
                StringBuffer buf =
                    new StringBuffer(
                        "Can not move Introscope Agent logs to the server node since application "
                            + "server configuration is null!").append('\n')
                        .append("Agent Node Name: {0}").append('\n')
                        .append("Target Server Logs Directory: {1}").append('\n')
                        .append("Source Agent Logs Directory: {2}").append('\n')
                        .append("Server id: {3}").append('\n')
                        .append("Test Type: {4}");

                logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, logsFolderServer,
                    logDir, serverId, testType);
            } else {
                String srcPath = null;
                if (appServerConfig.defaultAgentInstallDir != null) {
                    logInfo(
                        LOG_CATEGORY,
                        serverType,
                        "Using defaultAgentInstallDir to copy Wily logs");
                    srcPath = Paths.get(logDir, appServerConfig.defaultAgentInstallDir).toString();
                } else if (appServerConfig.currentAgentInstallDir != null) {
                    logInfo(
                        LOG_CATEGORY,
                        serverType,
                        "Using currentAgentInstallDir to copy Wily logs");
                    srcPath = Paths.get(appServerConfig.currentAgentInstallDir, "logs").toString();
                } 

                if (srcPath == null) {
                    StringBuffer buf =
                        new StringBuffer("Can not move Introscope Agent logs to the server node.")
                            .append('\n').append("Agent Node Name: {0}").append('\n')
                            .append("Target Server Logs Directory: {1}").append('\n')
                            .append("Source Agent Logs Directory: {2}").append('\n')
                            .append("Server id: {3}").append('\n').append("Test Type: {4}")
                            .append('\n').append("App Server Config: {5}");

                    logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, logsFolderServer,
                        logDir, serverId, testType, appServerConfig);

                } else {
                    StringBuffer buf =
                        new StringBuffer("Moving Introscope Agent logs to the server node.")
                            .append('\n').append("Agent Node Name: {0}").append('\n')
                            .append("Target Server Logs Directory: {1}").append('\n')
                            .append("Source Agent Logs Directory: {2}").append('\n')
                            .append("Server Id: {3}").append('\n').append("Test Type: {4}")
                            .append('\n').append("Full Wily Logs Directory Path: {5}").append('\n')
                            .append("App Server Config: {6}");

                    logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, logsFolderServer,
                        logDir, serverId, testType, srcPath, appServerConfig);

                    PerformanceTestResultsCollectorPlugin agentNodeCollector =
                        getPluginForNode(nodeName, DefaultPerfResultsCollectorPluginImpl.PLUGIN,
                            DefaultPerfResultsCollectorPluginImpl.class);

                    Collection<CollectionItem> wilyCollectionItems =
                        new ArrayList<CollectionItem>(1);
                    wilyCollectionItems.add(new DirectoryCollectionItem(srcPath,
                        logsFolderServer, true));
                    PerfTestResultCollectionConfig wilyLogCollectionConfig =
                        new PerfTestResultCollectionConfig(wilyCollectionItems);
                    agentNodeCollector.collect(wilyLogCollectionConfig);
                }
            }
        } else {
            logInfo(LOG_CATEGORY, serverType, "Skipping collecting Wily logs for no-agent test");
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
