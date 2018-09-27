package com.ca.apm.systemtest.fld.plugin.agentdownload;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author tavpa01
 *
 */
@PluginAnnotationComponent(pluginType = "agentDownloadPlugin")
public interface AgentDownloadPlugin extends Plugin {
    String VERSION_DATE_FORMAT = "yyMMddHHmm";
    void downloadNewVersion();
    long getCurrentVersion();
}
