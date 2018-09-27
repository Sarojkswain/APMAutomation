/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.tim.jobs;

import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.tim.TimPlugin;
import com.ca.apm.systemtest.fld.plugin.tim.TimPlugin.InstallStatus;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("timInstallJobTaskBean")
public class TimInstallTaskBean implements InitializingBean {
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory agentProxyFactory;

    private TimCheckRequirementsDelegate checkRequirementsDelegate;
    private StartInstallDelegate startInstallDelegate;
    private CheckInstallDelegate checkInstallDelegate;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        checkRequirementsDelegate = new TimCheckRequirementsDelegate(nodeManager, agentProxyFactory);
        startInstallDelegate = new StartInstallDelegate(nodeManager, agentProxyFactory);
        checkInstallDelegate = new CheckInstallDelegate(nodeManager, agentProxyFactory);
    }
    
    public TimInstallTaskBean() {
    }
    
    public TimCheckRequirementsDelegate getCheckRequirementsDelegate() {
        return checkRequirementsDelegate;
    }
    
    public StartInstallDelegate getStartInstallDelegate() {
        return startInstallDelegate;
    }
    
    public CheckInstallDelegate getCheckInstallDelegate() {
        return checkInstallDelegate;
    }

    private TimPlugin getPlugin(String nodeName) {
        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        TimPlugin timPlugin = proxy.getPlugin(TimPlugin.class);
        return timPlugin;
    }

    public class TimCheckRequirementsDelegate extends AbstractJavaDelegate implements JavaDelegate {

        public TimCheckRequirementsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String node = getExecutionVariable(execution, "node");
            execution.setVariable("nodeName", node);
            boolean nodeAvailable = false;
            if (node != null) {
                nodeAvailable = nodeManager.checkNodeAvailable(node);
            }
            execution.setVariable("nodeAvailable", nodeAvailable);
        }
    }
    
    public class StartInstallDelegate extends AbstractJavaDelegate implements JavaDelegate {
        public StartInstallDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            Set<String> names = execution.getVariableNames();
            for (String name: names) {
                Object value = execution.getVariable(name);
                System.out.println(name + " --> " + value);
            }
            
            String nodeName = getExecutionVariable(execution, "nodeName");
            TimPlugin timPlugin = getPlugin(nodeName);
            String trussBaseUrl = getExecutionVariable(execution, "timTrussBaseUrl");
            String timCodeName = getExecutionVariable(execution, "timCodeName");
            String timBuildNumber = getExecutionVariable(execution, "timBuildNumber");
            String timBuildId = getExecutionVariable(execution, "timBuildId");
            String timFilename = getExecutionVariable(execution, "timFilename");
            execution.setVariable("installStatus", InstallStatus.Installing.toString());
            timPlugin.startInstall(trussBaseUrl, timCodeName, timBuildNumber, timBuildId, timFilename);
        }
    }
    
    public class CheckInstallDelegate extends AbstractJavaDelegate implements JavaDelegate {
        public CheckInstallDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String nodeName = getExecutionVariable(execution, "nodeName");
            TimPlugin timPlugin = getPlugin(nodeName);
            InstallStatus status = timPlugin.checkInstallStatus();
            execution.setVariable("installStatus", status.toString());
        }
    }
}
