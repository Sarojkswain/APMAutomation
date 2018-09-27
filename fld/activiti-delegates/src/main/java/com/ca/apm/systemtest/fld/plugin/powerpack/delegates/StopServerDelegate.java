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
 * Java delegate to stop application server.
 * 
 * @author shadm01
 *
 */
public class StopServerDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Application Server Stop Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(StopServerDelegate.class);

    protected IAppServerPluginProvider appServerPluginProvider;

    public StopServerDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        IAppServerPluginProvider appServerPluginProvider, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String serverId = getStringExecutionVariable(execution, SERVER_ID);
        String nodeName = getNodeExecutionVariable(execution, NODE);
        final String serverType = getEnumExecutionVariable(execution, "serverType");

        logInfo(LOG_CATEGORY, serverType, "Stopping application server (name={0}, id={1}) on node: {2}", 
            serverType, serverId, nodeName);

        AppServerPlugin plugin = appServerPluginProvider.getPlugin(execution);
        plugin.stopAppServer(serverId);
    }
    
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
