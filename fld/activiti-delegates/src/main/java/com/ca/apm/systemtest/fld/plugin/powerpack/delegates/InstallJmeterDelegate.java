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
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class InstallJmeterDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Unzip Jmeter Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(InstallJmeterDelegate.class);

    public InstallJmeterDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);

        final JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);

        String unzippedFolder = jmeterPlugin.unzipJMeterZip();
        logInfo(LOG_CATEGORY, serverType, "Unzipped Jmeter into ''{0}''", unzippedFolder);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
