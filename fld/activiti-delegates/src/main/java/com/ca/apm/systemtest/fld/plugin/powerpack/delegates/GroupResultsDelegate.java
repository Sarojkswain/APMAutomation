package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.CollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DefaultPerfResultsCollectorPluginImpl;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.DirectoryCollectionItem;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerfTestResultCollectionConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors.PerformanceTestResultsCollectorPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class GroupResultsDelegate extends AbstractJavaDelegate {

    public static final String LOG_CATEGORY = "PowerPack Group Results Delegate";

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupResultsDelegate.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd__HH_mm");
    private static final String RESULT_FOLDER_PREFIX = "resultsFolder";
    
    public GroupResultsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String loNodeName = getNodeExecutionVariable(execution, PowerPackConstants.LOAD_ORCHESTRATOR_CONTROLLER_NODE_PARAM_NAME);
        String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        String archiveFolderPath = getStringExecutionVariable(execution, PowerPackConstants.LOGS_ARCHIVE_FOLDER_PARAM_NAME);

        StringBuffer buf = new StringBuffer("Grouping sub-test folders under common parent").
            append('\n').
            append("LO Node: {0}").
            append('\n').
            append("Logs Archive Folder: {1}");
        
        logInfo(LOG_CATEGORY, serverType, 
            buf.toString(), 
            loNodeName, 
            archiveFolderPath);

        PerformanceTestResultsCollectorPlugin loFSCollectorPlugin = getPluginForNode(loNodeName, 
            DefaultPerfResultsCollectorPluginImpl.PLUGIN, 
            DefaultPerfResultsCollectorPluginImpl.class);

        Collection<CollectionItem> subTestFolders = new ArrayList<CollectionItem>(5);

        String groupFolderName = "Grouped_" + DATE_FORMAT.format(new Date()) + "_" + serverType;

        String targetGroupFolderPath = Paths.get(archiveFolderPath, groupFolderName).toString();
        
        for (String key : execution.getVariables().keySet()) {
            if (key.startsWith(RESULT_FOLDER_PREFIX)) {
                String srcPath = execution.getVariable(key).toString();
                subTestFolders.add(new DirectoryCollectionItem(srcPath, targetGroupFolderPath, 
                    null, null, true, true, false));
                
            }
        }

        PerfTestResultCollectionConfig fsCollectionConfig = new PerfTestResultCollectionConfig(subTestFolders);
        loFSCollectorPlugin.collect(fsCollectionConfig);
        
        execution.setVariable(PowerPackConstants.GROUPED_LOGS_FOLDER_PARAM_NAME, targetGroupFolderPath);
        logInfo(LOG_CATEGORY, serverType, "Grouped test results under folder: {0}", targetGroupFolderPath);
    }


    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
