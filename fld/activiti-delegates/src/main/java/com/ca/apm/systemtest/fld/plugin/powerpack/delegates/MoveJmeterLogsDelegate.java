package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.CollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DefaultPerfResultsCollectorPluginImpl;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DirectoryCollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerfTestResultCollectionConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerformanceTestResultsCollectorPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * 
 * Delegate to move Jmeter output metrics.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MoveJmeterLogsDelegate extends AbstractJavaDelegate {


    public static final String LOG_CATEGORY = "PowerPack Move Jmeter Test Results Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(MoveJmeterLogsDelegate.class);

    public MoveJmeterLogsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String jmeterNodeName =
            getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType =
            getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String logsFolderServer =
            getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_SERVER_PARAM_NAME);
        final String fileExtensionsToCollect = getStringExecutionVariable(execution, 
            PowerPackConstants.JMETER_COLLECT_FILE_EXTENSIONS_PARAM_NAME);
        String jmeterPath = getStringExecutionVariable(execution, JMeterPlugin.JMETER_PATH_KEY);

        StringBuffer buf =
            new StringBuffer("Moving Jmeter metrics to the server node.").append('\n')
                .append("Jmeter Node Name: {0}").append('\n')
                .append("Target Server Logs Directory: {1}").append('\n')
                .append("Jmeter File Extensions To Collect: {2}").append('\n')
                .append("Jmeter path (saved in execution variables): {3}");

        logInfo(LOG_CATEGORY, serverType, buf.toString(), jmeterNodeName, logsFolderServer,
            fileExtensionsToCollect, jmeterPath);

        PerformanceTestResultsCollectorPlugin jmeterCollectorPlugin =
            getPluginForNode(jmeterNodeName, DefaultPerfResultsCollectorPluginImpl.PLUGIN,
                DefaultPerfResultsCollectorPluginImpl.class);

        Collection<CollectionItem> jmeterCollectionItems = new ArrayList<CollectionItem>(1);

        if (jmeterPath == null) {
            logInfo(LOG_CATEGORY, serverType,
                "Jmeter path not found in execution variables, trying to get it from agent running at ''{0}''",
                jmeterNodeName);
            JMeterPlugin jmeterPlugin =
                getPluginForNode(jmeterNodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);
            jmeterPath = jmeterPlugin.getJmeterPath();
            logInfo(LOG_CATEGORY, serverType, "Jmeter path obtained from the agent: {0}", jmeterPath);
        }

        Collection<String> extensionsCollection = null;
        if (StringUtils.isBlank(fileExtensionsToCollect)) {
            extensionsCollection = PowerPackConstants.DEFAULT_JMETER_OUTPUT_FILE_SUFFIXES;
            logInfo(LOG_CATEGORY, serverType, "No Jmeter file extensions to collect provided, using defaults: {0}", 
                extensionsCollection);
        } else {
            String[] extArray = StringUtils.split(fileExtensionsToCollect, ',');
            extensionsCollection = Arrays.asList(extArray);
            logInfo(LOG_CATEGORY, serverType, "Parsed Jmeter file extensions: {0}", 
                extensionsCollection);
        }
        jmeterCollectionItems.add(new DirectoryCollectionItem(jmeterPath, logsFolderServer,
            extensionsCollection, null, true, true, true));
        PerfTestResultCollectionConfig jmeterLogCollectionConfig =
            new PerfTestResultCollectionConfig(jmeterCollectionItems);

        jmeterCollectorPlugin.collect(jmeterLogCollectionConfig);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
