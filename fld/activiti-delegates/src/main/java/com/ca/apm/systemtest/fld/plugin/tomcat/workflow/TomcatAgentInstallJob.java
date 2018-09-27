/**
 *
 */
package com.ca.apm.systemtest.fld.plugin.tomcat.workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author KEYJA01
 */
@Component("tomcatAgentInstallJob")
public class TomcatAgentInstallJob extends AbstractJavaDelegate {
    @Autowired
    public TomcatAgentInstallJob(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
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
        String customName = (String) execution.getVariable("customName");
        //customName = "Tomcat6";
        String brtmExtension = (String) execution.getVariable("brtmExtension");
        String enableDynamicinstrument = (String) execution.getVariable("enableDynamicinstrument");
        String serverId = (String) execution.getVariable("serverId");
        String artifactSpecification = (String) execution.getVariable("artifactSpecification");

        AgentProxy proxy = agentProxyFactory.createProxy(node);
        TomcatPlugin pl = proxy.getPlugin("tomcatPlugin", TomcatPlugin.class);

        Map<String, String> extraProps = new LinkedHashMap<>(16);
        String dirFiles
            = "tomcat-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd";
        extraProps.put("introscope.autoprobe.directivesFile", dirFiles);
        if (enableDynamicinstrument.equals("yes")) {
            extraProps.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
            extraProps.put("introscope.agent.remoteagentdynamicinstrumentation.enabled", "true");
        }
        extraProps.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", momNode);
        extraProps.put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT",
            momPort.toString());
        extraProps.put("agentManager.url.1", momNode+":"+momPort.toString());
        extraProps.put("introscope.agent.agentAutoNamingEnabled", "false");
        extraProps.put("introscope.agent.customProcessName", customName);
        extraProps.put("introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds", "0");
        extraProps.put("introscope.agent.decorator.enabled", "true");

        if (pl.isAgentInstalled(serverId)) {
            pl.uninstallAgentNoInstaller(serverId, true);
        }

        pl.installAgentNoInstaller(serverId, artifactSpecification, extraProps,
            new ArrayList<String>(1), brtmExtension.equals("yes"));
    }
}
