package com.ca.apm.systemtest.fld.plugin.agenthvr;

import java.io.File;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * FLD AgentHVR plugin interface.
 *
 * @author meler02
 */
@PluginAnnotationComponent(pluginType = "agentHvrPlugin")
public interface AgentHvrPlugin extends Plugin {

    void createTempDir();

    void deleteTempDir();

    File downloadAgentHvr(String version);

    File unzipAgentHvrZip();

    void configureExecutables(String emDir, String emHost, String emPort, String userNameEm,
        String passwordEm, String agentHostName);

    String execute();

    boolean checkRunning();

    void stop();

    File downloadManagementModules(String version);

    File unzipManagementModules();

    void configureManagementModule(String emDir, String mmJarName);

    boolean checkManagementModuleInstalled(String emDir, String mmJarName);
}
