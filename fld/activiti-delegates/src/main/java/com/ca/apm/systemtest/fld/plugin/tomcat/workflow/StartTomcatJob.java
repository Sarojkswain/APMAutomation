/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.tomcat.workflow;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("startTomcatJobBean")
public class StartTomcatJob extends AbstractJavaDelegate {

    @Autowired
    public StartTomcatJob(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate#handleExecution(org.activiti.engine.delegate.DelegateExecution)
     */
    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        TomcatPlugin tomcatPlugin = loadPlugin(execution, "node", "tomcatPlugin", TomcatPlugin.class);
        String serverId = getExecutionVariable(execution, "serverId");
        if (tomcatPlugin != null && serverId != null) {
            tomcatPlugin.startAppServer(serverId);
        }
    }

}
