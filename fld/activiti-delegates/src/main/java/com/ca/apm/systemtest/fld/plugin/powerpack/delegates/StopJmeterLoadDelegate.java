package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate to stop Jmeter load.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class StopJmeterLoadDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Stop Jmeter Load Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(StopJmeterLoadDelegate.class);

    public StopJmeterLoadDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        String jmeterTaskName = getStringExecutionVariable(execution, PowerPackConstants.JMETER_TASK_NAME_PARAM_NAME);
        Integer jmeterStopPort =  getIntegerExecutionVariable(execution, JMeterPlugin.JMETER_STOPPING_PORT_KEY);
        if (jmeterTaskName == null) {
            //Set to null just to have this mapping in execution
            execution.setVariable(PowerPackConstants.JMETER_TASK_NAME_PARAM_NAME, null);
        }
        if (jmeterStopPort == null) {
            //Set to null just to have this mapping in execution
            execution.setVariable(JMeterPlugin.JMETER_STOPPING_PORT_KEY, null);
        }

        final String buf = "Stopping Jmeter running on node: {0}\n"
            + "Jmeter Task Name: {1}\n"
            + "Jmeter Stop Port: {2,number,#}";

        logInfo(LOG_CATEGORY, serverType, buf, nodeName, jmeterTaskName, jmeterStopPort);

        JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);
        boolean stopped = false;
        
        if (jmeterTaskName == null) {
            logWarn(LOG_CATEGORY, serverType, "Jmeter task name is null, can not find out if Jmeter is running or not.");
            if (jmeterStopPort != null) {
                logInfo(LOG_CATEGORY, serverType, "Jmeter Stop Port is not null, trying to stop Jmeter anyway.");
                stopped = jmeterPlugin.shutDown(jmeterStopPort);
                if (stopped) {
                    logInfo(LOG_CATEGORY, serverType, "Jmeter stopped!");
                } else {
                    logInfo(LOG_CATEGORY, serverType, "Jmeter was not stopped!");
                }
            } else {
                logWarn(LOG_CATEGORY, serverType, "Jmeter stop port is also null. Skipping.");
            }
        } else {
            logInfo(LOG_CATEGORY, serverType, "Jmeter task name is not null, finding out if Jmeter is running.");
            boolean isJmeterRunning = jmeterPlugin.isRunning(jmeterTaskName);
            if (isJmeterRunning) {
                if (jmeterStopPort != null) {
                    logInfo(LOG_CATEGORY, serverType, "Jmeter is running and stop port is not null! Trying to stop it.");
                    stopped = jmeterPlugin.shutDown(jmeterStopPort);
                } else {
                    logInfo(LOG_CATEGORY, serverType, "Jmeter is running but stop port is null! Can not stop, skipping.");                    
                }
                if (stopped) {
                    logInfo(LOG_CATEGORY, serverType, "Jmeter stopped successfully!");
                } else {
                    logError(LOG_CATEGORY, serverType, "Failed to stop Jmeter!");
                }
            } else {
                logInfo(LOG_CATEGORY, serverType, "Jmeter is not running! No need to stop it.");
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
