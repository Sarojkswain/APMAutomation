package com.ca.apm.systemtest.fld.plugin.weblogic.osb.powerPack;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
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
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentEMConnectionDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentInstallDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.BuildPerformanceReportDelegateFactory;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.GroupResultsDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.IAppServerPluginProvider;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JavaMonitorDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JmeterDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.MoveDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.RunJmeterLoadDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StartAppServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StartMonitoringDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StopServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.UninstallAgentDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.perfjob.AbstractPerfJob;
import com.ca.apm.systemtest.fld.plugin.weblogic.powerPack.ReCreateTradeDbDelegate;
import com.ca.apm.systemtest.fld.plugin.wls.powerPack.PowerPackWlsPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * OSB PowerPack test automation plugin.
 * 
 * @author rsssa02
 *
 */
@Component("weblogicosbPP")
public class OSBPowerPackImpl extends AbstractPerfJob implements IAppServerPluginProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSBPowerPackImpl.class);

    public static final String DEFAULT_JMX_METRICS = "java.lang:type=Memory|HeapMemoryUsage/used,max;java"
        + ".lang:type=GarbageCollector,name=Copy|CollectionCount|CollectionTime;java"
        + ".lang:type=GarbageCollector,name=MarkSweepCompact|CollectionCount|CollectionTime";

    public static final String DEFAULT_CMD_LINE_KEY = "ServiceBusExamples";
    
    @Autowired(required = false)
    protected FldLogger fldLogger;

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
        LOGGER.info(getClass().getSimpleName() + ".stopAppServer()");
        return new StopServerDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate startAppServer() {
        LOGGER.info(getClass().getSimpleName() + ".startAppServer()");
        return new StartAppServerOSBDelegate(nodeManager, agentProxyFactory, this);
    }

    @Override
    public JavaDelegate uninstallAgent() {
        LOGGER.info(getClass().getSimpleName() + ".uninstallAgent()");
        return new UninstallAgentDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate runJmeterTests() {
        LOGGER.info(getClass().getSimpleName() + ".runJmeterTests()");
        return new RunJmeterLoadDelegate(nodeManager, agentProxyFactory, fldLogger);
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
        LOGGER.info(getClass().getSimpleName() + ".checkJmeterStatus");
        return JmeterDelegates.getCheckJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate killJmeter() {
        LOGGER.info(getClass().getSimpleName() + ".killJmeter");
        return JmeterDelegates.getStopJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate cleanUp() {

        return new ReCreateTradeDbDelegate(nodeManager, agentProxyFactory, fldLogger);
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
        LOGGER.info(getClass().getSimpleName() + ".configureAgent()");
        return new AgentConfigureDelegate(nodeManager, agentProxyFactory, this, false, fldLogger);
    }

    @Override
    public JavaDelegate monitor() {
        LOGGER.info(getClass().getSimpleName() + ".monitor()");
        
        return new MonitorDelegate(nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate checkMonitoring() {
        LOGGER.info(getClass().getSimpleName() + ".checkMonitoring()");
        return JavaMonitorDelegate.getCheckMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate stopMonitoring() {
        LOGGER.info(getClass().getSimpleName() + ".stopMonitoring()");
        return JavaMonitorDelegate.getStopMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate buildReport() {
        LOGGER.info(getClass().getSimpleName() + ".buildReport");
        return BuildPerformanceReportDelegateFactory.createBuildReportDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate groupResults() {
        return new GroupResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public AppServerPlugin getPlugin(DelegateExecution execution) {
        String nodeName = JavaDelegateUtils.getNodeExecutionVariable(execution, AbstractJavaDelegate.NODE);

        if (nodeName == null) {
            return null;
        }

        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        return proxy.getPlugin(PowerPackWlsPlugin.POWERPACK_WLS_PLUGIN, PowerPackWlsPlugin.class);
    }

    @Override
    public JavaDelegate unConfigureAgent() {
        return new AgentConfigureDelegate(nodeManager, agentProxyFactory, this, true, fldLogger);
    }

    @Override
    public JavaDelegate checkAgentConnectedEM() {
        return new AgentEMConnectionDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    class MonitorDelegate extends StartMonitoringDelegate {

        public MonitorDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, OSBPowerPackImpl.this.fldLogger);
        }

        @Override
        protected void handleExecution(final DelegateExecution execution) throws Throwable {
            final String jmxMetrics = getStringExecutionVariable(execution, PowerPackConstants.JMX_METRICS_PARAM_NAME);
            if (jmxMetrics == null || "".equals(jmxMetrics.trim())) {
                logInfo("Null or empty JMX metrics found. Setting up default JMX metrics: {0}", DEFAULT_JMX_METRICS);
                execution.setVariable(PowerPackConstants.JMX_METRICS_PARAM_NAME, DEFAULT_JMX_METRICS);
            }
            final String cmdLineKey = getStringExecutionVariable(execution, PowerPackConstants.PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME);
            if (cmdLineKey == null || "".equals(cmdLineKey.trim())) {
                logInfo("Null or empty command line key found. Setting up default command line key: {0}", DEFAULT_CMD_LINE_KEY);
                execution.setVariable(PowerPackConstants.PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME, DEFAULT_CMD_LINE_KEY);
            }
            super.handleExecution(execution);
        }

        @Override
        protected Logger getLogger() {
            return LOGGER;
        }

    }

    class StartAppServerOSBDelegate extends StartAppServerDelegate{
        public StartAppServerOSBDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
                                      IAppServerPluginProvider appServerPluginProvider) {
            super(nodeManager, agentProxyFactory, appServerPluginProvider, OSBPowerPackImpl.this.fldLogger);
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
    }

}
