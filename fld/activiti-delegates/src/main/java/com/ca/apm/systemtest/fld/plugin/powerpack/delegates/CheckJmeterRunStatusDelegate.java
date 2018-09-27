package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Java delegate to check the run status of the Jmeter program.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class CheckJmeterRunStatusDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Jmeter Status Check Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(CheckJmeterRunStatusDelegate.class);

    public CheckJmeterRunStatusDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String jMeterTaskName = getStringExecutionVariable(execution, PowerPackConstants.JMETER_TASK_NAME_PARAM_NAME);


        StringBuffer buf = new StringBuffer("Checking Jmeter Run Status.").append('\n').
            append("Jmeter Node: {0}").append('\n').
            append("Jmeter Task Name: {1}").append('\n');
        
        logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, jMeterTaskName);

        JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);
        boolean jmeterRunning = jmeterPlugin.isRunning(jMeterTaskName);

        execution.setVariable(PowerPackConstants.JMETER_TASK_FINISHED_PARAM_NAME, !jmeterRunning);
        logInfo(LOG_CATEGORY, serverType, "Jmeter is running: {0}", jmeterRunning);

        String jmeterLastResult = "";
        if (!jmeterRunning) {
            jmeterLastResult = jmeterPlugin.getLastResult(jMeterTaskName);
        }
        if (jmeterLastResult.contains("FATAL")) {
            logError(LOG_CATEGORY, serverType, "Jmeter scheduled task has failed! Last result: {0}", 
                jmeterLastResult);
            throw new BpmnError(jmeterLastResult, "Jmeter scheduled task has failed!");
        } else {
            logInfo(LOG_CATEGORY, serverType, "Jmeter last result: {0}", jmeterLastResult);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
