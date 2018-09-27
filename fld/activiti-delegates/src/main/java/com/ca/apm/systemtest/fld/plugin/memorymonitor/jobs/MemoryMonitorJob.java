/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.memorymonitor.jobs;

import java.net.InetAddress;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.memorymonitor.MemoryMonitorPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("memoryMonitorJob")
public class MemoryMonitorJob extends AbstractJavaDelegate {
    private Logger log = LoggerFactory.getLogger(MemoryMonitorJob.class);
    private String orchestratorHost = "";

    @Autowired
    public MemoryMonitorJob(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
        try {
            orchestratorHost = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.warn("Unable to determine server host name", e);
        }
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String node = getExecutionVariable(execution, "node");
        MemoryMonitorPlugin plugin = loadPlugin(execution, "node", "memoryMonitorPlugin", MemoryMonitorPlugin.class);
        if (plugin != null) {
            try {
                String group = getExecutionVariable(execution, "group");
                String roleName = getExecutionVariable(execution, "roleName");
                plugin.createChart(group, roleName, orchestratorHost);
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Unable to generate heap memory usage graph on {1}. Exception: {0}", node);
            }
        }
    }
    
    

}
