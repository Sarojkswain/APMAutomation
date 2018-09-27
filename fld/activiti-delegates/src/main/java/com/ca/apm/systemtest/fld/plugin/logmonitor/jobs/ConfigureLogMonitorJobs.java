/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.logmonitor.jobs;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.logmonitor.MonitoringPlugin;
import com.ca.apm.systemtest.fld.logmonitor.config.LogPeriodicity;
import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;
import com.ca.apm.systemtest.fld.logmonitor.config.Operator;
import com.ca.apm.systemtest.fld.logmonitor.config.Rule;
import com.ca.apm.systemtest.fld.logmonitor.config.SimpleCondition;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("configureLogMonitorJobs")
public class ConfigureLogMonitorJobs implements InitializingBean {
    private Logger log = LoggerFactory.getLogger(ConfigureLogMonitorJobs.class);
    
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory agentProxyFactory;
    
    private ConfigureLogMonitorDelegate configureJob;
    private StartLogMonitor startMonitors;

    
    @Override
    public void afterPropertiesSet() throws Exception {
        configureJob = new ConfigureLogMonitorDelegate(nodeManager, agentProxyFactory);
        startMonitors = new StartLogMonitor(nodeManager, agentProxyFactory);
    }

    
    public ConfigureLogMonitorDelegate getConfigureJob() {
        return configureJob;
    }
    
    
    public StartLogMonitor getStartMonitors() {
        return startMonitors;
    }
    
    
    public class ConfigureLogMonitorDelegate extends AbstractJavaDelegate {

        public ConfigureLogMonitorDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            MonitoringPlugin plugin = loadPlugin(execution, "nodeName", "logMonitorPlugin", MonitoringPlugin.class);
            String nodeName = getExecutionVariable(execution, "nodeName");
            String logFile = getExecutionVariable(execution, "logFile");
            String logStream = getExecutionVariable(execution, "logStream");
            
            if (plugin == null || logFile == null || logStream == null) {
                log.warn("Unable to configure log monitoring for {}", nodeName);
                return;
            }
            
            
            
            List<Rule> rules = new ArrayList<>();
            rules.add(new Rule(LogPeriodicity.Never, new SimpleCondition(Operator.Contains, "DEBUG", false)));
            rules.add(new Rule(LogPeriodicity.Always, new SimpleCondition(Operator.Contains, "Exception", false)));
            rules.add(new Rule(LogPeriodicity.Always, new SimpleCondition(Operator.Contains, "ERROR", false)));
            
            LogStream stream = new LogStream(logStream, logFile, rules);
            log.info("Setting log monitoring configuration for {}, file: {}, cond: {}", nodeName,
                logFile, rules);
            plugin.setStreamConfig(logStream, stream);
        }
    }
    
    
    public class StartLogMonitor extends AbstractJavaDelegate {
        public StartLogMonitor(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            startMonitor(execution, "fld.mom.node");
            startMonitor(execution, "fld.em01.node");
            startMonitor(execution, "fld.em02.node");
            startMonitor(execution, "fld.em03.node");
            startMonitor(execution, "fld.em04.node");
            startMonitor(execution, "fld.em05.node");
            startMonitor(execution, "fld.em06.node");
            startMonitor(execution, "fld.em07.node");
            startMonitor(execution, "fld.em08.node");
            startMonitor(execution, "fld.em09.node");
            startMonitor(execution, "fld.em10.node");
            startMonitor(execution, "fld.webview.node");
        }
        
        
        private void startMonitor(DelegateExecution execution, String nodeName) {
            try {
                String node = getExecutionVariable(execution, nodeName);
                MonitoringPlugin plugin = loadPlugin(execution, nodeName, "logMonitorPlugin", MonitoringPlugin.class);
                if (plugin == null ) {
                    log.warn("Unable to start/stop log monitoring for {}", node);
                    return;
                }
                
                plugin.enableMonitor("emLogStream");
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Unable to start log monitoring on {1}. Exception: {0}", nodeName);
            }
        }
        
    }
}
