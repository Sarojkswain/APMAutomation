package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.dotnet.DotNetPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Uninstall APM .NET agent delegate.
 */
public class UninstallAgentDelegate extends AbstractSharePointPpDelegate {
    private static final Logger log = LoggerFactory.getLogger(UninstallAgentDelegate.class);

    public UninstallAgentDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        try {
            DotNetPlugin dotnet = getDotNetPlugin(execution);
            dotnet.makeInstallPrefix();
            dotnet.uninstallAgent();
            dotnet.deleteAgentDirectory();
        } catch (Exception ex) {
            throw ErrorUtils
                .logExceptionAndWrapFmt(log, ex, "Failure during agent uninstall. Exception: {0}");
        }
    }
}
