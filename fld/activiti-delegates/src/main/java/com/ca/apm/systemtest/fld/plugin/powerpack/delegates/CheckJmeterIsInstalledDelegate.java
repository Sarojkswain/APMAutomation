package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Delegate to check if Jmeter is installed on a node.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class CheckJmeterIsInstalledDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Jmeter Installation Status Check Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(CheckJmeterIsInstalledDelegate.class);

    public CheckJmeterIsInstalledDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);

        StringBuffer buf = new StringBuffer("Checking if Jmeter Is Installed On Node: {0}");
        
        logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName);

        JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);
        boolean isJmeterInstalled = jmeterPlugin.checkIfJmeterIsInstalled();

        execution.setVariable(PowerPackConstants.JMETER_IS_INSTALLED_PARAM_NAME, isJmeterInstalled);

        logInfo(LOG_CATEGORY, serverType, "Is Jmeter Installed: " + isJmeterInstalled);        
    }
    
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


}
