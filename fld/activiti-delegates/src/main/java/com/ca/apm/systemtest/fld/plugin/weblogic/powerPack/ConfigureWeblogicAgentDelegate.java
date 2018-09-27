package com.ca.apm.systemtest.fld.plugin.weblogic.powerPack;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.delegates.IAppServerPluginProvider;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.wls.powerPack.PowerPackWlsPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Java delegate to configure Introscope Agent for Weblogic application server. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ConfigureWeblogicAgentDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Configure Weblogic Agent Delegate";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureWeblogicAgentDelegate.class);

    public static final String[] IP_MEM_OPTS_INSERT_TEXT_POINTS  = new String[] { "^(\\s)*set MEM_MAX_PERM_SIZE=.*" };

    protected boolean unConfigure;
    protected IAppServerPluginProvider appServerPluginProvider;

    public ConfigureWeblogicAgentDelegate(NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory, IAppServerPluginProvider pluginProvider, 
        FldLogger fldLogger, boolean unConfigure) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.unConfigure = unConfigure;
        this.appServerPluginProvider = pluginProvider;
    }
    
    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String nodeName = getNodeExecutionVariable(execution, NODE);
        final String serverType = getEnumExecutionVariable(execution, "serverType");
        
        if (unConfigure) {
            logInfo(LOG_CATEGORY, serverType, "Switching off Introscope agent on node {0}", nodeName);    
        } else {
            logInfo(LOG_CATEGORY, serverType, "Switching on Introscope agent on node {0}", nodeName);
        }

        PowerPackWlsPlugin plugin = (PowerPackWlsPlugin) appServerPluginProvider.getPlugin(execution);
        
        plugin.setInsertAfterPoints(WlsPluginConfiguration.WLVersion.WEBLOGIC_103.name(), 
            SystemUtil.OperatingSystemFamily.Windows.name(), 
            PowerPackWlsPlugin.INSERTPOINTKEY_JAVAOPTS, IP_MEM_OPTS_INSERT_TEXT_POINTS, true);
        
        String serverId = getStringExecutionVariable(execution, SERVER_ID);
        plugin.setupAgent(serverId, unConfigure, false);
    }
    
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
