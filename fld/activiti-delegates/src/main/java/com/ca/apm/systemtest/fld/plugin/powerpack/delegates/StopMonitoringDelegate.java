package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;

import org.activiti.engine.delegate.DelegateExecution;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.JmxMetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.JstatMetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricGatheringPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.TypePerfMetricGatheringPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Common stop monitoring Java delegate.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class StopMonitoringDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Performance Stop Monitoring Delegate";


    public StopMonitoringDelegate(NodeManager nodeManager, 
                                  AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(final DelegateExecution execution) throws Throwable {
        final String serverType = getEnumExecutionVariable(execution, "serverType");
        final String nodeName = getNodeExecutionVariable(execution, NODE);
        final Boolean runTypePerfMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_TYPEPERF_MONITORING_PARAM_NAME, false);
        final Boolean runJmxMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JMX_MONITORING_PARAM_NAME, false);
        final Boolean runJstatMonitoring = getBooleanExecutionVariable(execution, PowerPackConstants.RUN_JSTAT_MONITORING_PARAM_NAME, false);

        StringBuffer strBuf = new StringBuffer("Stopping performance monitoring.").
            append('\n').
            append("Run typeperf monitoring: {0}").
            append('\n').
            append("Run JMX monitoring: {1}").
            append('\n').
            append("Run jstat monitoring: {2}");
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), runTypePerfMonitoring, runJmxMonitoring, runJstatMonitoring);
        
        Exception jmxGathererStopException = null;
        if (runJmxMonitoring) {
            try {
                stopJmxMonitoring(nodeName, serverType);
            } catch (Exception e) {
                logError(e);
                jmxGathererStopException = e;
            }
        }

        Exception typePerfStopException = null;
        if (runTypePerfMonitoring) {
            try {
                stopTypePerfMonitoring(nodeName, serverType);
            } catch (Exception e) {
                logError(e);
                typePerfStopException = e;
            }
        }

        Exception jstatGathererStopException = null;
        if (runJstatMonitoring) {
            try {
                stopJstatMonitoring(nodeName, serverType);
            } catch (Exception e) {
                logError(e);
                jstatGathererStopException = e;
            }
        }
        
        if (jmxGathererStopException != null) {
            throw jmxGathererStopException;
        }
        
        if (typePerfStopException != null) {
            throw typePerfStopException;
        }

        if (jstatGathererStopException != null) {
            throw jstatGathererStopException;
        }
    
    }

    protected void stopJmxMonitoring(String nodeName, String serverType) {
        logInfo(LOG_CATEGORY, serverType, "Stopping JMX monitoring on node {0}", nodeName);
        
        final MetricGatheringPlugin jmxMetricGatherer = getPluginForNode(nodeName, 
            JmxMetricGatheringPlugin.PLUGIN, JmxMetricGatheringPlugin.class);

        jmxMetricGatherer.stopMonitoring();

        
    }
    
    protected void stopTypePerfMonitoring(String nodeName, String serverType) {
        logInfo(LOG_CATEGORY, serverType, "Stopping TypePerf monitoring on node {0}", nodeName);

        final MetricGatheringPlugin typePerfMetricGatherer = getPluginForNode(nodeName, 
            TypePerfMetricGatheringPlugin.PLUGIN, TypePerfMetricGatheringPlugin.class);
        typePerfMetricGatherer.stopMonitoring();    
    }
    
    protected void stopJstatMonitoring(String nodeName, String serverType) {
        logInfo(LOG_CATEGORY, serverType, "Stopping Jstat monitoring on node {0}", nodeName);
        
        final MetricGatheringPlugin jstatMetricGatherer = getPluginForNode(nodeName, 
            JstatMetricGatheringPlugin.PLUGIN, JstatMetricGatheringPlugin.class);

        jstatMetricGatherer.stopMonitoring();
    }

}
