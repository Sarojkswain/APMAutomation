/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.em.job;

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Execution;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Aggregates {@link JavaDelegate} instances for use in the FLD workflows
 * 
 * @author keyja01
 *
 */
@Component("startEmTaskBean")
public class StartEmJobsBean implements InitializingBean, ApplicationContextAware {
    
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory agentProxyFactory;
    
    private CheckAvailableDelegate checkAvailable;
    private StartEmDelegate startEm;
    private StartWebViewDelegate startWebView;
    private StopMonitorsDelegate stopMonitors;
    private StopJdbcClientQueriesDelegate stopJdbcClientQueries;
    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public CheckAvailableDelegate getCheckAvailable() {
        return checkAvailable;
    }
    
    public StartEmDelegate getStartEm() {
        return startEm;
    }
    
    public StartWebViewDelegate getStartWebView() {
        return startWebView;
    }
    
    public StopMonitorsDelegate getStopMonitors() {
        return stopMonitors;
    }

    public StopJdbcClientQueriesDelegate getStopJdbcClientQueries() {
        return stopJdbcClientQueries;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkAvailable = new CheckAvailableDelegate(nodeManager, agentProxyFactory);
        startEm = new StartEmDelegate(nodeManager, agentProxyFactory);
        startWebView = new StartWebViewDelegate(nodeManager, agentProxyFactory);
        stopMonitors = new StopMonitorsDelegate(nodeManager, agentProxyFactory);
        stopJdbcClientQueries = new StopJdbcClientQueriesDelegate(nodeManager, agentProxyFactory);
    }
    
    /**
     * Checks if the target node is available
     * @author keyja01
     *
     */
    public class CheckAvailableDelegate extends AbstractJavaDelegate {

        public CheckAvailableDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String node = getExecutionVariable(execution, "nodeName");
            boolean available = nodeManager.checkNodeAvailable(node);
            execution.setVariable("available", available);
        }
        
    }
    
    
    /**
     * Attempts to start the EM (or MOM) on the target node
     * @author keyja01
     *
     */
    public class StartEmDelegate extends AbstractJavaDelegate {

        private static final String NODE_NAME = "nodeName";

        public StartEmDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            EmPlugin plugin = null;
            String serverType = getExecutionVariable(execution, "serverType");
            if (serverType == null) {
                // log warning
                return;
            } else if (serverType.equals("WebView")) {
                plugin = loadPlugin(execution, NODE_NAME, "wvPlugin", EmPlugin.class);
            } else if (serverType.equals("EM")) {
                plugin = loadPlugin(execution, NODE_NAME, "emPlugin", EmPlugin.class);
            } else {
                // log warning
                return;
            }
                
            plugin.start(null);
        }
        
    }
    
    
    /**
     * Attempts to start the EM (or MOM) on the target node
     * @author keyja01
     *
     */
    public class StartWebViewDelegate extends AbstractJavaDelegate {

        public StartWebViewDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            EmPlugin plugin = loadPlugin(execution, "nodeName", "wvPlugin", EmPlugin.class); 
            plugin.start(null);
        }
        
    }
    
    
    /**
     * This delegate is used to signal to the monitor loads and run daily test workflows that
     * they should stop execution.
     * 
     * @author keyja01
     *
     */
    public class StopMonitorsDelegate extends AbstractJavaDelegate {

        public StopMonitorsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            Object dashboardId = execution.getVariable(DashboardIdStore.DASHBOARD_VARIABLE);
            
            RuntimeService runtime = applicationContext.getBean(RuntimeService.class);
            
            // look for call.monitor.loads execution
            List<Execution> list = runtime.createExecutionQuery()
                .messageEventSubscriptionName("stop.monitor.loads")
                .processVariableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, dashboardId)
                .list();
            Execution tgt = list.get(0);
            
            // and send stop.monitor.loads message 
            runtime.messageEventReceived("stop.monitor.loads", tgt.getId());

            // look for call.daily.tests execution
            list = runtime.createExecutionQuery()
                .messageEventSubscriptionName("stop.daily.tests")
                .processVariableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, dashboardId)
                .list();
            tgt = list.get(0);
            
            // stop.daily.tests
            runtime.messageEventReceived("stop.daily.tests", tgt.getId());
        }
        
    }

    /**
     * This delegate is used to signal to the jdbc.client.queries workflow that it should stop execution.
     * 
     * @author bocto01
     *
     */
    public class StopJdbcClientQueriesDelegate extends AbstractJavaDelegate {
        public StopJdbcClientQueriesDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            Object dashboardId = execution.getVariable(DashboardIdStore.DASHBOARD_VARIABLE);
            
            RuntimeService runtime = applicationContext.getBean(RuntimeService.class);
            
            // look for jdbc.client.queries execution
            List<Execution> list = runtime.createExecutionQuery()
                .messageEventSubscriptionName("fld.stop.load.jdbc.client.queries")
                .processVariableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, dashboardId)
                .list();
            Execution tgt = list.get(0);
            
            // and send fld.stop.load.jdbc.client.queries message 
            runtime.messageEventReceived("fld.stop.load.jdbc.client.queries", tgt.getId());
        }        
    }

}
