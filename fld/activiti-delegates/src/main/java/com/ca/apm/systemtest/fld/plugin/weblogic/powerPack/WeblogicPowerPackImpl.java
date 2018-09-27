package com.ca.apm.systemtest.fld.plugin.weblogic.powerPack;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.JavaDelegateUtils;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentEMConnectionDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.AgentInstallDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.BuildPerformanceReportDelegateFactory;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.GroupResultsDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.IAppServerPluginProvider;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JavaMonitorDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.JmeterDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.MoveDelegates;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StartAppServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.StopServerDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.UninstallAgentDelegate;
import com.ca.apm.systemtest.fld.plugin.powerpack.perfjob.AbstractPerfJob;
import com.ca.apm.systemtest.fld.plugin.wls.powerPack.PowerPackWlsPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;

/**
 * Factory to create Java delegates for PowerPack performance testing with Oracle Weblogic application server. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Component("weblogicPP")
public class WeblogicPowerPackImpl extends AbstractPerfJob implements IAppServerPluginProvider {

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
        return new StopServerDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate startAppServer() {
        return new StartAppServerDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate uninstallAgent() {
        return new UninstallAgentDelegate(nodeManager, agentProxyFactory, this, fldLogger);
    }

    @Override
    public JavaDelegate runJmeterTests() {
        AbstractJavaDelegate delegate = JmeterDelegates.getJmeterRunDelegate(nodeManager, agentProxyFactory, fldLogger);
        return delegate;
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
        AbstractJavaDelegate delegate = JmeterDelegates.getCheckJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
        return delegate;
    }

    @Override
    public JavaDelegate killJmeter() {
        AbstractJavaDelegate delegate = JmeterDelegates.getStopJmeterDelegate(nodeManager, agentProxyFactory, fldLogger);
        return delegate;
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
        return new ConfigureWeblogicAgentDelegate(nodeManager, agentProxyFactory, this, fldLogger, false);
    }

    @Override
    public JavaDelegate unConfigureAgent() {
        return new ConfigureWeblogicAgentDelegate(nodeManager, agentProxyFactory, this, fldLogger, true);
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
        AbstractJavaDelegate delegate = JavaMonitorDelegate.getStopMonitoringDelegate(nodeManager, agentProxyFactory, fldLogger);
        return delegate;
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
    public AppServerPlugin getPlugin(DelegateExecution execution) {
        String nodeName = JavaDelegateUtils.getNodeExecutionVariable(execution, AbstractJavaDelegate.NODE);

        if (nodeName == null) {
            return null;
        }

        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        return proxy.getPlugin(PowerPackWlsPlugin.POWERPACK_WLS_PLUGIN, PowerPackWlsPlugin.class);
    }

    @Override
    public JavaDelegate groupResults() {
        return new GroupResultsDelegate(nodeManager, agentProxyFactory, fldLogger);
    }

}
