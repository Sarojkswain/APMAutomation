package com.ca.apm.systemtest.fld.plugin.webm.powerPack;

import static com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate.DO_NOTHING_DELEGATE;

import java.nio.file.Paths;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.JavaDelegateUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentConfigureDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentInstallDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.BuildPerformanceReportDelegateFactory;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.IAppServerPluginProvider;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JavaMonitorDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JmeterDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.MoveDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.RunJmeterLoadDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StartAppServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StartMonitoringDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StopServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.UninstallAgentDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricsCheckerConfig;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricsCheckerPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.metrics.MetricsCheckerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.powerpack.perfjob.AbstractPerfJob;
import com.ca.apm.systemtest.fld.plugin.webmethods.WebmethodsPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Delegate classes for webmethods performance test execution.
 * 
 * @author rsssa02
 */
@Component("webmethodsPP")
public class WebmPowerPackImpl extends AbstractPerfJob implements IAppServerPluginProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebmPowerPackImpl.class);
    
    public static final String DEFAULT_CMD_LINE_KEY = "IntegrationServer";
    
    @Autowired(required = false)
    protected FldLogger fldLogger;

    @Override
    public AppServerPlugin getPlugin(DelegateExecution execution) {
        String nodeName = JavaDelegateUtils.getNodeExecutionVariable(execution,
                AbstractJavaDelegate.NODE);
        if (null != nodeName) {

            AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
            return proxy.getPlugin(WebmethodsPlugin.WEBM_PLUGIN, WebmethodsPlugin.class);
        } else {
            return null;
        }
    }

    @Override
    public JavaDelegate installAgent() {
        return new AgentInstallDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate installAgentWithPP() {
        return new AgentInstallDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate stopAppServer() {
        return new StopServerDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate startAppServer() {
        return new StartAppServerDelegate(nodeManager, agentProxyFactory, WebmPowerPackImpl.this, fldLogger);
    }

    @Override
    public JavaDelegate uninstallAgent() {
        return new UninstallAgentDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate runJmeterTests() {
        return new RunJmeterLoadDelegate(nodeManager,agentProxyFactory,fldLogger);
    }

    @Override
    public JavaDelegate downloadJmeter() {
        return JmeterDelegates.getDownloadJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate installJmeter() {
        return JmeterDelegates.getInstallJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkJmeterInstallationStatus() {
        return JmeterDelegates.getCheckJmeterInstallationStatusDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkJmeterRunStatus() {
        return JmeterDelegates.getCheckJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate killJmeter() {
        return JmeterDelegates.getStopJmeterDelegate(nodeManager,agentProxyFactory,fldLogger);
    }

    @Override
    public JavaDelegate cleanUp() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate moveTypePerfLogs() {
        return MoveDelegates.getMoveTypePerfResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate moveJmxLogs() {
        return MoveDelegates.getMoveJmxResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate moveJstatLogs() {
        return MoveDelegates.getMoveJstatResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate moveJmeterLogs() {
        return MoveDelegates.getMoveJmeterResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate moveIntroscopeAgentLogs() {
        return MoveDelegates.getMoveIntroscopeAgentLogsDelegate(nodeManager, agentProxyFactory, fldLogger, 
            this);
    }

    @Override
    public JavaDelegate archiveLogs() {
        return MoveDelegates.getArchiveLogsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate configureAgent() {
        return new AgentConfigureDelegate(nodeManager, agentProxyFactory, this, false, fldLogger);
    }

    @Override
    public JavaDelegate unConfigureAgent() {
        return new AgentConfigureDelegate(nodeManager, agentProxyFactory, this, true, fldLogger);
    }

    @Override
    public JavaDelegate monitor() {
        return new MonitorDelegate(nodeManager,agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkMonitoring() {
        return new WebmJavaMonitorDelegate(nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate stopMonitoring() {
        LOGGER.info("{}.stopMonitoring()", getClass().getSimpleName());
        return JavaMonitorDelegate.getStopMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate buildReport() {
        return BuildPerformanceReportDelegateFactory.createBuildReportDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate groupResults() {
        return DO_NOTHING_DELEGATE;
    }

    @Override
    public JavaDelegate checkAgentConnectedEM() {
        return DO_NOTHING_DELEGATE;
    }

    private class WebmJavaMonitorDelegate extends AbstractJavaDelegate {

        public WebmJavaMonitorDelegate(NodeManager nodeManager,
                                      AgentProxyFactory agentProxyFactory) {
            super(nodeManager,agentProxyFactory);
        }

        @Override
        public void handleExecution(DelegateExecution execution) throws Throwable {
            final String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
            final String typePerfLogFileName = getStringExecutionVariable(execution,
                    PowerPackConstants.TYPE_PERF_LOG_FILE_NAME_PARAM_NAME, 
                    PowerPackConstants.DEFAULT_TYPE_PERF_LOG_FILE_NAME);
            final String jstatLogFileName = getStringExecutionVariable(execution, 
                PowerPackConstants.JSTAT_LOG_FILE_NAME_PARAM_NAME,
                    PowerPackConstants.DEFAULT_JSTAT_LOG_FILE_NAME);
            
            MetricsCheckerConfig config = new MetricsCheckerConfig(Paths.get(logDir,
                    typePerfLogFileName).toString(),
                    Paths.get(logDir, jstatLogFileName).toString());

            String nodeName = getNodeExecutionVariable(execution, NODE);

            MetricsCheckerPlugin mc = getPluginForNode(nodeName, MetricsCheckerPlugin.PLUGIN,
                    MetricsCheckerPluginImpl.class);
            mc.check(config);
        }
    
    }
    
    class MonitorDelegate extends StartMonitoringDelegate {

        public MonitorDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
            super(nodeManager, agentProxyFactory, fldLogger);
        }

        @Override
        protected void handleExecution(final DelegateExecution execution) throws Throwable {
            final String cmdLineKey = getStringExecutionVariable(execution, 
                PowerPackConstants.PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME);
            if (StringUtils.isBlank(cmdLineKey)) {
                logInfo("Null or empty command line key found. Setting up default command line key: {0}", 
                    DEFAULT_CMD_LINE_KEY);
                execution.setVariable(PowerPackConstants.PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME, 
                    DEFAULT_CMD_LINE_KEY);
            }
            super.handleExecution(execution);
        }

        @Override
        protected Logger getLogger() {
            return LOGGER;
        }

    }
    
}
