/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.wls.workflow;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author filja01
 *
 */
@Component("startWlsJobBean")
public class StartWlsJob extends AbstractJavaDelegate {

    @Autowired
    public StartWlsJob(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate#handleExecution(org.activiti.engine.delegate.DelegateExecution)
     */
    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        WlsPlugin wlsPlugin = loadPlugin(execution, "node", "wlsPlugin", WlsPlugin.class);
        String serverId = getExecutionVariable(execution, "serverId");
        if (wlsPlugin != null && serverId != null) {
            wlsPlugin.startAppServer(serverId);
        }
    }

}
