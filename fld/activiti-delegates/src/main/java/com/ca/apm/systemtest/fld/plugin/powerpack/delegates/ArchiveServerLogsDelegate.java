package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

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
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ArchiveServerLogsDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Archive Logs Delegate";

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM__dd_HH_mm");
    protected static final Logger LOGGER = LoggerFactory.getLogger(ArchiveServerLogsDelegate.class);

    public ArchiveServerLogsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String loNodeName = getNodeExecutionVariable(execution, PowerPackConstants.LOAD_ORCHESTRATOR_CONTROLLER_NODE_PARAM_NAME);
        final String logArchiveFolder = getStringExecutionVariable(execution, PowerPackConstants.LOGS_ARCHIVE_FOLDER_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String testType = getEnumExecutionVariable(execution, PowerPackConstants.TEST_TYPE_PARAM_NAME);
        final String logsFolderServer = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_SERVER_PARAM_NAME);
        final String agentVersion = getEnumExecutionVariable(execution, PowerPackConstants.AGENT_VERSION_PARAM_NAME);

        PerformanceTestResultsCollectorPlugin loNodeCollector = getPluginForNode(loNodeName, 
            DefaultPerfResultsCollectorPluginImpl.PLUGIN, 
            DefaultPerfResultsCollectorPluginImpl.class);

        
        String destFolder = Paths.get(logArchiveFolder, 
            DATE_FORMAT.format(new Date()) + "_" + serverType + "_" + (agentVersion != null ? agentVersion + "_" + testType : testType)).toString();

        StringBuffer buf = new StringBuffer("Archiving collected results under path: ''{0}''.").append('\n')
            .append("Creating execution variable ''{1}'' to save the path.").append('\n')
            .append("LO Node Name: {2}").append('\n')
            .append("Source Logs Directory: {3}").append('\n')
            .append("Logs Archive Directory: {4}").append('\n')
            .append("Test Type: {5}").append('\n')
            .append("Introscope Agent Version: {6}");
            

        logInfo(LOG_CATEGORY, serverType, buf.toString(), 
            destFolder, 
            PowerPackConstants.RESULTS_FOLDER_PARAM_NAME,
            loNodeName, 
            logsFolderServer,
            logArchiveFolder,
            testType,
            agentVersion);
        
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
        Path testParamsPath = Paths.get(folderPath, "testParams.txt");
        
        logInfo(LOG_CATEGORY, serverType, "Writing test params to: {0}", testParamsPath.toString());
        
        StringBuilder sb = new StringBuilder();

        final String NEWLINE = System.getProperty("line.separator");

        Set<String> keysSet = execution.getVariables().keySet();
        String[] keys = keysSet.toArray(new String[keysSet.size()]);
        for (int z = 0; z < keys.length; z++) {
            sb.append(z).append(" : ").append(keys[z]).append(" : ")
                .append(execution.getVariable(keys[z])).append(NEWLINE);
        }

        org.apache.commons.io.FileUtils.writeStringToFile(testParamsPath.toFile(), sb.toString());
    }

}
