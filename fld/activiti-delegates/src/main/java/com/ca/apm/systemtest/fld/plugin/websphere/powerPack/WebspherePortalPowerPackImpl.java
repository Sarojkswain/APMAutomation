package com.ca.apm.systemtest.fld.plugin.websphere.powerPack;

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
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentConfigureDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentEMConnectionDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentInstallDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.BuildPerformanceReportDelegateFactory;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.DbCleanupDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.GroupResultsDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.IAppServerPluginProvider;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JavaMonitorDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JmeterDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.MoveDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StartAppServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StopServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.UninstallAgentDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.perfjob.AbstractPerfJob;
import com.ca.apm.systemtest.fld.plugin.websphere.WebspherePlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

@Component("webspherePortalPP")
public class WebspherePortalPowerPackImpl extends AbstractPerfJob implements
    IAppServerPluginProvider {

    private static final Logger log = LoggerFactory.getLogger(WebspherePortalPowerPackImpl.class);

    @Autowired(required = false)
    protected FldLogger fldLogger;

    public WebspherePortalPowerPackImpl() {
    }

    @Override
    public AppServerPlugin getPlugin(DelegateExecution execution) {
        String nodeName = JavaDelegateUtils.getNodeExecutionVariable(execution,
            AbstractJavaDelegate.NODE);
        if (nodeName == null) {
            //Fallback
            log.info("Execution variable with name {} not found, trying {}",
                AbstractJavaDelegate.NODE,
                AbstractJavaDelegate.NODE_NAME);
            nodeName = JavaDelegateUtils.getStringExecutionVariable(execution,
                AbstractJavaDelegate.NODE_NAME);
            log.info("Execution variable {} value: {}", AbstractJavaDelegate.NODE_NAME, nodeName);
        }

        if (nodeName == null) {
            return null;
        }

        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        return proxy.getPlugin(WebspherePlugin.PLUGIN, WebspherePlugin.class);
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
        return new StopAppServerDelegate(nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate startAppServer() {
        return new StartDelegate(nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate uninstallAgent() {
        return new UninstallAgent(nodeManager, agentProxyFactory);
    }

    @Override
    public JavaDelegate configureAgent() {
        log.info("Configuring web-server to start with our agent");
        return new ConfigureAgentDelegate(nodeManager, agentProxyFactory, false, fldLogger);
    }

    @Override
    public JavaDelegate unConfigureAgent() {
        log.info("Configuring web-server to run without any agents");
        return new ConfigureAgentDelegate(nodeManager, agentProxyFactory, true, fldLogger);
    }

    @Override
    public JavaDelegate monitor() {
        return JavaMonitorDelegate.getStartMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkMonitoring() {
        return JavaMonitorDelegate.getCheckMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate stopMonitoring() {
        return JavaMonitorDelegate.getStopMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate runJmeterTests() {
        return JmeterDelegates.getJmeterRunDelegate(nodeManager, agentProxyFactory, fldLogger);
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
        return JmeterDelegates.getStopJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate cleanUp() {
        return new cleanupDelegate(nodeManager, agentProxyFactory, fldLogger);
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
    public JavaDelegate buildReport() {
        return BuildPerformanceReportDelegateFactory.createBuildReportDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate checkAgentConnectedEM() {
        return new AgentEMConnectionDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    public JavaDelegate groupResults() {
        return new GroupResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

    class ConfigureAgentDelegate extends AgentConfigureDelegate {
        public ConfigureAgentDelegate(NodeManager nodeManager,
            AgentProxyFactory agentProxyFactory, boolean unConfigure, FldLogger fldLogger) {
            super(nodeManager, agentProxyFactory, WebspherePortalPowerPackImpl.this, unConfigure, fldLogger);
        }
    }

    class StopAppServerDelegate extends StopServerDelegate {

        public StopAppServerDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, WebspherePortalPowerPackImpl.this, 
                WebspherePortalPowerPackImpl.this.fldLogger);
        }
    }

    class StartDelegate extends StartAppServerDelegate {
        public StartDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, WebspherePortalPowerPackImpl.this, 
                WebspherePortalPowerPackImpl.this.fldLogger);
        }
    }

    class UninstallAgent extends UninstallAgentDelegate {
        public UninstallAgent(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, WebspherePortalPowerPackImpl.this, 
                WebspherePortalPowerPackImpl.this.fldLogger);
        }
    }

    class cleanupDelegate extends DbCleanupDelegate {
        public cleanupDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
            super(nodeManager, agentProxyFactory, fldLogger);
        }
    }

}
