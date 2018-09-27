package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Common agent switcher delegate.
 * 
 * @author shadm01
 */
public class AgentConfigureDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Introscope Agent Configure Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(AgentConfigureDelegate.class);
    protected boolean unConfigure;
    protected IAppServerPluginProvider appServerPluginProvider;

    public AgentConfigureDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        IAppServerPluginProvider appServerPluginProvider, boolean unConfigure, FldLogger fldLogger)  {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
        this.unConfigure = unConfigure;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        String nodeName = getNodeExecutionVariable(execution, NODE);

        logInfo(LOG_CATEGORY, serverType, "Configuring Introscope agent on node: {0}", nodeName);
        AppServerPlugin plugin = appServerPluginProvider.getPlugin(execution);

        String serverId = getStringExecutionVariable(execution, SERVER_ID);
        String artifactSpecification = getStringExecutionVariable(execution, 
            PowerPackConstants.ARTIFACT_SPECIFICATION_PARAM_NAME);

        Boolean legacy = artifactSpecification.endsWith("legacy");
        plugin.setupAgent(serverId, unConfigure, legacy);

    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
