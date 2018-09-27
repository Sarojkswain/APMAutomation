package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * This delegate check whether jMeter is running or not.
 * @todo This should be shared by all power pack tests implementations.
 */
public class CheckJmeterStatusDelegate extends AbstractJavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(CheckJmeterStatusDelegate.class);

    public CheckJmeterStatusDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    @Override
    protected void handleExecution(final DelegateExecution execution) throws Throwable {
        log.info("jMeter check status flow started");

        String nodeName = getNodeExecutionVariable(execution, NODE);
        
        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        JMeterPlugin jmeterPlugin = proxy.getPlugin("jmeterPlugin", JMeterPlugin.class);

        String jmeterTaskName = getStringExecutionVariable(execution, "jMeterTaskName");
        log.info("checking whether {} has already finished", jmeterTaskName);
        boolean jmeterRunning = jmeterPlugin.isRunning(jmeterTaskName);
        execution.setVariable("jMeterTaskFinished", !jmeterRunning);
        log.info("jMeter Task finished status is: {}", jmeterRunning);

        String jmeterLastResult = "";
        if (!jmeterRunning) {
            jmeterLastResult = jmeterPlugin.getLastResult(jmeterTaskName);
        }
        if (jmeterLastResult.contains("FATAL")) {
            throw new BpmnError(jmeterLastResult, "jMeter scheduled task has failed");
        }

        log.info("jMeter check status flow finished");
    }
}
