package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Java delegate to uninstall Introscope agent from an application server.
 * 
 * @author shadm01
 */
public class UninstallAgentDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Introscope Agent Uninstall Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(UninstallAgentDelegate.class);

    protected IAppServerPluginProvider appServerPluginProvider;

    public UninstallAgentDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        IAppServerPluginProvider appServerPluginProvider, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String nodeName = getNodeExecutionVariable(execution, NODE);
        String serverId = getStringExecutionVariable(execution, SERVER_ID);
        final String serverType = getEnumExecutionVariable(execution, "serverType");

        logInfo(LOG_CATEGORY, serverType, 
            "Uninstalling Introscope agent from application server (type={0}, id={1}) on node: {2}", 
            serverType, serverId, nodeName);

        AppServerPlugin plugin = appServerPluginProvider.getPlugin(execution);
        plugin.uninstallAgentNoInstaller(serverId, false);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
