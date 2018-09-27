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
 * Java delegate to start an application server.
 * 
 * @author shadm01
 */
public class StartAppServerDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Application Server Start Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(StartAppServerDelegate.class);

    protected IAppServerPluginProvider appServerPluginProvider;

    public StartAppServerDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        IAppServerPluginProvider appServerPluginProvider, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
    }


    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String serverType = getEnumExecutionVariable(execution, "serverType");
        String nodeName = getNodeExecutionVariable(execution, NODE);
        String serverId = getStringExecutionVariable(execution, SERVER_ID);

        logInfo(LOG_CATEGORY, serverType, "Starting application server (name={0}, id={1}) on node: {2}", 
            serverType, serverId, nodeName);

        AppServerPlugin plugin = appServerPluginProvider.getPlugin(execution);
        plugin.startAppServer(serverId);
    }
    
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
