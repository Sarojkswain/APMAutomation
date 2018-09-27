package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Delegate to download Jmeter distribution.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class DownloadJmeterDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Download Jmeter Delegate";

    protected static final Logger LOGGER = LoggerFactory.getLogger(DownloadJmeterDelegate.class);

    public DownloadJmeterDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory,
        FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String jmeterDistroHttpDownloadMethodUrl = getStringExecutionVariable(execution, PowerPackConstants.JMETER_DOWNLOAD_URL_PARAM_NAME);
        final String jmeterDownloadDirectory = getStringExecutionVariable(execution, PowerPackConstants.JMETER_DOWNLOAD_DIRECTORY_PARAM_NAME);
        
        final JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);
        String jmeterZipPath = null;
        if (!StringUtils.isBlank(jmeterDownloadDirectory)) {
            String jmeterDirPrefix = "apache_jmeter_" + serverType + "_";
            logInfo(LOG_CATEGORY, serverType, "Jmeter download directory will be placed under ''{0}'' with prefix ''{1}''", 
                jmeterDownloadDirectory, jmeterDirPrefix);
            jmeterPlugin.createTempDir(jmeterDownloadDirectory, jmeterDirPrefix);
        }
        if (jmeterDistroHttpDownloadMethodUrl != null) {
            logInfo(LOG_CATEGORY, serverType, "Downloading Jmeter distribution from ''{0}''", 
                jmeterDistroHttpDownloadMethodUrl);
            jmeterZipPath = jmeterPlugin.downloadJmeterByUrl(jmeterDistroHttpDownloadMethodUrl);
        } else {
            logInfo(LOG_CATEGORY, serverType, "Downloading latest available Jmeter distribution");
            jmeterZipPath = jmeterPlugin.downloadJMeter(null);
        }
        logInfo(LOG_CATEGORY, serverType, "Jmeter downloaded to: {0}", jmeterZipPath);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
