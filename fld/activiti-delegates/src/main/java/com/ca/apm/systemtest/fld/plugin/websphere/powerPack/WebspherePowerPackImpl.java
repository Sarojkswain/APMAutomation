package com.ca.apm.systemtest.fld.plugin.websphere.powerPack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.files.FileUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.JavaDelegateUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin;
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
import com.ca.apm.systemtest.fld.plugin.powerpack.perfjob.PerfJob;
import com.ca.apm.systemtest.fld.plugin.websphere.WebspherePlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

@Component("webspherePP")
public class WebspherePowerPackImpl extends AbstractPerfJob implements IAppServerPluginProvider {

    private static final Logger log = LoggerFactory.getLogger(WebspherePowerPackImpl.class);

    @Autowired(required = false)
    protected FldLogger fldLogger;

    public WebspherePowerPackImpl() {
    }

    @Override
    public AppServerPlugin getPlugin(DelegateExecution execution) {
        String nodeName = JavaDelegateUtils.getNodeExecutionVariable(execution, AbstractJavaDelegate.NODE);

        if (nodeName == null) {
            return null;
        }

        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        return proxy.getPlugin(WebspherePlugin.PLUGIN, WebspherePlugin.class);
    }

    @Override
    public JavaDelegate installAgent() {
        return new InstallWebsphereAgentDelegate(nodeManager, agentProxyFactory, false);
    }

    @Override
    public JavaDelegate installAgentWithPP() {
        return new InstallWebsphereAgentDelegate(nodeManager, agentProxyFactory, true);
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

    class InstallWebsphereAgentDelegate extends AgentInstallDelegate {
        boolean powerPack = false;

        public InstallWebsphereAgentDelegate(NodeManager nodeManager,
            AgentProxyFactory agentProxyFactory, boolean powerPack) {
            super(nodeManager, agentProxyFactory, WebspherePowerPackImpl.this, WebspherePowerPackImpl.this.fldLogger);
            this.powerPack = powerPack;
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            super.handleExecution(execution);

            configureAgentProfileFile(execution);
        }

        public void configureAgentProfileFile(DelegateExecution execution) throws IOException {
            log.info("Configuration of IntroscopeAgent.profile started");

            String testType = getEnumExecutionVariable(execution, "testType").toLowerCase();
            String serverId = getStringExecutionVariable(execution, "serverId");
            final String prefixPath = "/com/ca/apm/systemtest/fld/plugin/powerpack/websphere/";

            WebspherePlugin pl = (WebspherePlugin) getPlugin(execution);

            //TODO - fix it to take from server config file

            log.info("Test Type {} is used", testType);

            if (!serverId.endsWith("Portal")) { //TODO: DM - Not very good check
                if (testType.contains("noagent")){
                    pl.removePowerPackAttributeToServerConfigFile(serverId);
                    log.info(
                            "No transformation of config file is done, since noAgent is selected");
                    return;
                }

                if (testType.contains("agentpluspp")){
                    pl.addPowerPackAttributeToServerConfigFile(serverId);
                    String configuration = FileUtils.readFileFromResourcePath(
                            prefixPath + "agentPlusPP.xml", PerfJob.class.getClassLoader());
                    transformConfigFileWithXml(execution, configuration);
                    return;
                }

                if (testType.contains("agent")){
                    pl.removePowerPackAttributeToServerConfigFile(serverId);
                    String configuration = FileUtils.readFileFromResourcePath(
                            prefixPath + "agentOnly.xml", PerfJob.class.getClassLoader());
                    transformConfigFileWithXml(execution, configuration);
                    return;
                }

                throw ErrorUtils.logErrorAndThrowException(log, "Test type is unknown ");
            }

            log.info("Configuration of IntroscopeAgent.profile finished");
        }

        private void transformConfigFileWithXml(DelegateExecution execution, String configuration) {
            String serverId = getStringExecutionVariable(execution, SERVER_ID);
            WebspherePlugin pl = (WebspherePlugin) getPlugin(execution);
            
            AppServerConfiguration appServerConfig = pl.getAppServerConfiguration(serverId);
            String agentDir = appServerConfig.currentAgentInstallDir;
            String path = agentDir + "/core/config/IntroscopeAgent.profile";

            Map<String, Object> vars = new HashMap<>();
            vars.put("path", path);

            String nodeName = getNodeExecutionVariable(execution, NODE);
            FileTransformationPlugin transPlugin = getPluginForNode(nodeName, 
                FileTransformationPlugin.PLUGIN, FileTransformationPlugin.class);
 
            transPlugin
                .transform(configuration, FileTransformationPlugin.ConfigurationFormat.XML, vars);

        }
    }

    class ConfigureAgentDelegate extends AgentConfigureDelegate {
        public ConfigureAgentDelegate(NodeManager nodeManager,
            AgentProxyFactory agentProxyFactory, boolean unConfigure, FldLogger fldLogger) {
            super(nodeManager, agentProxyFactory, WebspherePowerPackImpl.this, unConfigure, fldLogger);
        }
    }

    class StopAppServerDelegate extends StopServerDelegate {

        public StopAppServerDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, WebspherePowerPackImpl.this, WebspherePowerPackImpl.this.fldLogger);
        }
    }

    class StartDelegate extends StartAppServerDelegate {
        public StartDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, WebspherePowerPackImpl.this, WebspherePowerPackImpl.this.fldLogger);
        }
    }

    class UninstallAgent extends UninstallAgentDelegate {
        public UninstallAgent(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory, WebspherePowerPackImpl.this, WebspherePowerPackImpl.this.fldLogger);
        }
    }

    class cleanupDelegate extends DbCleanupDelegate {

        public cleanupDelegate(NodeManager nodeManager,
            AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
            super(nodeManager, agentProxyFactory, fldLogger);
        }

        @Override
        public void stopServerBeforeCleanup(DelegateExecution execution) {
            log.info("Stopping server before cleanup start");

            String serverId = getStringExecutionVariable(execution, SERVER_ID);

            WebspherePlugin plugin = (WebspherePlugin) getPlugin(execution);

            if (!plugin.isServerRunning(serverId)) {
                log.info("Server is not running, starting server");
                plugin.startServer(serverId);
            }

            log.info("Stopping server before cleanup end");
        }
    }

}
