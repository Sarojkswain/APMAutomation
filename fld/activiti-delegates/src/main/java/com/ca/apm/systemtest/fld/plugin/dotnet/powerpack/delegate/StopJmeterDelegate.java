package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Stop jMeter delegate.
 *
 * @author haiva01
 */
public class StopJmeterDelegate extends AbstractJavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(StopJmeterDelegate.class);

    public StopJmeterDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    @Override
    protected void handleExecution(final DelegateExecution execution) throws Throwable {
        log.info("jMeter kill task started");

        String nodeName = getNodeExecutionVariable(execution, NODE);
        JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN,
            JMeterPlugin.class);
        String jmeterTaskName = getStringExecutionVariable(execution, "jMeterTaskName");
        boolean jmeterRunning = jmeterPlugin.isRunning(jmeterTaskName);

        if (jmeterRunning) {
            final String stopPortStr = getStringExecutionVariable(execution,
                JMeterPlugin.JMETER_STOPPING_PORT_KEY);
            int stopPort = Integer.parseInt(stopPortStr);
            jmeterPlugin.shutDown(stopPort);
            boolean stopped = jmeterPlugin.shutDown(stopPort);
            if (stopped) {
                log.info("jMeter stopped successfully.");
            } else {
                log.error("Failed to stop jMeter!");
            }
        }

        log.info("jMeter kill task finished");
    }
}