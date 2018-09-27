package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.nio.file.Paths;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricsCheckerConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricsCheckerPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricsCheckerPluginImpl;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Common check monitoring Java delegate.
 *
 * @author shadm01
 */
public class CheckMonitoringDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Performance Check Monitoring Delegate";

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckMonitoringDelegate.class);

    public CheckMonitoringDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(final DelegateExecution execution) throws Throwable {
        final String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        final String typePerfLogFileName = getStringExecutionVariable(execution, PowerPackConstants.TYPE_PERF_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME);
        final String jmxLogFileName = getStringExecutionVariable(execution, PowerPackConstants.JMX_LOG_FILE_NAME_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMX_LOG_FILE_NAME);
        MetricsCheckerConfig config = new MetricsCheckerConfig(Paths.get(logDir, typePerfLogFileName).toString(),
            Paths.get(logDir, jmxLogFileName).toString());
        
        String nodeName = getNodeExecutionVariable(execution, NODE);

        MetricsCheckerPlugin mc = getPluginForNode(nodeName, MetricsCheckerPlugin.PLUGIN,
            MetricsCheckerPluginImpl.class);
        mc.check(config);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
