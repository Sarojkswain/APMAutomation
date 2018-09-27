package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Common Java delegate for installing Introscope Agent. 
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class AgentInstallDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Introscope Agent Install Delegate";

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInstallDelegate.class);

    protected IAppServerPluginProvider appServerPluginProvider;

    public AgentInstallDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        IAppServerPluginProvider appServerPluginProvider, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
        this.appServerPluginProvider = appServerPluginProvider;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String nodeName = getNodeExecutionVariable(execution, NODE);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        logInfo(LOG_CATEGORY, serverType, "Installing Introscope agent on node: {0}", nodeName);

        String serverId = getStringExecutionVariable(execution, SERVER_ID);
        String agentName = getStringExecutionVariable(execution, PowerPackConstants.AGENT_NAME_PARAM_NAME);
        Boolean isBrtmExtensionOn = getBooleanExecutionVariable(execution, PowerPackConstants.BRTM_EXTENSION_PARAM_NAME);
        String artifactSpecification = getStringExecutionVariable(execution, PowerPackConstants.ARTIFACT_SPECIFICATION_PARAM_NAME);
        String logsDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        String directArtifactDownloadLink = getStringExecutionVariable(execution, PowerPackConstants.DIRECT_INTROSCOPE_AGENT_DOWNLOAD_URL_PARAM_NAME);
        String additionalAgentProperties = getStringExecutionVariable(execution, PowerPackConstants.ADDITIONAL_AGENT_PROFILE_PROPERTIES);
        String agentProbeDirectives = getStringExecutionVariable(execution, PowerPackConstants.AGENT_PROBE_DIRECTIVES);
        String agentExtraModules = getStringExecutionVariable(execution, PowerPackConstants.AGENT_EXTRA_MODULES);
        
        if (directArtifactDownloadLink != null) {
            artifactSpecification = directArtifactDownloadLink;
        }
        AppServerPlugin plugin = appServerPluginProvider.getPlugin(execution);

        Map<String, String> extraProps = new LinkedHashMap<>(16);

        
        extraProps.put("introscope.autoprobe.directivesFile", agentProbeDirectives);
        extraProps.put("introscope.agent.agentAutoNamingEnabled", "false");
        extraProps.put("introscope.agent.agentName", agentName);
        extraProps.put("introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds", "0");
        extraProps.put("introscope.agent.decorator.enabled", "true");

        AppServerConfiguration appSrvConfig = plugin.getAppServerConfiguration(serverId);

        extraProps.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
        extraProps.put("log4j.appender.logfile.MaxFileSize", "50MB");
        extraProps.put("log4j.appender.logfile.File",
            Paths.get(logsDir, appSrvConfig.defaultAgentInstallDir, agentName + ".IntroscopeAgent.log").toString());
        extraProps.put("introscope.autoprobe.logfile",
            Paths.get(logsDir, appSrvConfig.defaultAgentInstallDir, agentName + ".AutoProbe.log").toString());

        if (!StringUtils.isBlank(additionalAgentProperties)) {
            String[] props = StringUtils.split(additionalAgentProperties, ';');
            if (props != null) {
                logInfo(LOG_CATEGORY, serverType, "Additional Agent profile properties array: {0}", Arrays.toString(props));
                for (String prop : props) {
                    String[] keyAndValueArray = StringUtils.split(prop, '=');
                    if (keyAndValueArray == null) {
                        logWarn(LOG_CATEGORY, serverType, "Ignoring invalid agent property: {0}", prop);
                        continue;
                    }
                    
                    if (keyAndValueArray.length > 2) {
                        logWarn(LOG_CATEGORY, serverType, "Using suspecious agent property: {0}", prop);
                    }
                    
                    if (keyAndValueArray.length < 2) {
                        extraProps.remove(keyAndValueArray[0]);
                    } else {
                        extraProps.put(keyAndValueArray[0], keyAndValueArray[1]);
                    }
                }
                
            }
        }

        List<String> modules = new ArrayList<>(3);

        modules.add("examples\\SOAPerformanceManagement\\ext\\BoundaryOnlyTrace.jar");
        modules.add("examples\\SOAPerformanceManagement\\ext\\WebServicesAgent.jar");

        if (agentExtraModules != null) {
            String[] splitModules = StringUtils.split(agentExtraModules, ',');
            if (splitModules != null) {
                logInfo(LOG_CATEGORY, serverType, "Adding agent extra modules collection: {0}", Arrays.toString(splitModules));
                modules.addAll(Arrays.asList(splitModules));    
            }
        }

        if (plugin.isAgentInstalled(serverId)) {
            plugin.uninstallAgentNoInstaller(serverId, true);
        }
        
        plugin.installAgentNoInstaller(serverId, artifactSpecification, extraProps, modules,
            isBrtmExtensionOn);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


}
