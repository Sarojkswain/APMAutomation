/**
 *
 */
package com.ca.apm.systemtest.fld.plugin.wls.workflow;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author FILJA01
 */
@Component("wlsAgentInstallJob")
public class WlsAgentInstallJob extends AbstractJavaDelegate {
    @Autowired
    public WlsAgentInstallJob(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
    }

    /* (non-Javadoc)
     * @see org.activiti.engine.delegate.JavaDelegate#execute(org.activiti.engine.delegate
     * .DelegateExecution)
     */
    @Override
    public void handleExecution(DelegateExecution execution) throws Exception {
        String node = getExecutionVariable(execution, "node");
        String momNode = getExecutionVariable(execution, "momNode");
        Number momPort = (Number) execution.getVariable("momPort");
        String agentName = (String) execution.getVariable("agentName");
        String brtmExtension = (String) execution.getVariable("brtmExtension");
        String serverId = (String) execution.getVariable("serverId");
        String artifactSpecification = (String) execution.getVariable("artifactSpecification");
        String logsDir = (String) execution.getVariable("logsDir");

        AgentProxy proxy = agentProxyFactory.createProxy(node);
        WlsPlugin pl = proxy.getPlugin("wlsPlugin", WlsPlugin.class);

        Map<String, String> extraProps = new LinkedHashMap<>(17);
        String dirFiles
            = "weblogic-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd";
        extraProps.put("introscope.autoprobe.directivesFile", dirFiles);
        extraProps.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", momNode);
        extraProps.put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT",
            momPort.toString());
        extraProps.put("agentManager.url.1", momNode+":"+momPort.toString());
        extraProps.put("introscope.agent.agentAutoNamingEnabled", "false");
        extraProps.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
        extraProps.put("introscope.agent.agentName", agentName);//WLS103WinAgent1
        extraProps.put("introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds", "0");
        extraProps.put("introscope.agent.decorator.enabled", "true");
        extraProps.put("log4j.appender.logfile.MaxFileSize", "50MB");
        extraProps.put("log4j.appender.logfile.File",
            Paths.get(logsDir, agentName + ".IntroscopeAgent.log").toString());
        extraProps.put("introscope.autoprobe.logfile",
            Paths.get(logsDir, agentName + ".AutoProbe.log").toString());

        if (pl.isAgentInstalled(serverId)) {
            pl.uninstallAgentNoInstaller(serverId, true);
        }

        pl.installAgentNoInstaller(serverId, artifactSpecification, extraProps,
            new ArrayList<String>(1), brtmExtension.equals("yes"));
    }
}
