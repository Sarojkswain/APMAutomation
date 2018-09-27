/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.websphere;

import java.nio.file.Path;
import java.util.Map;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;

/**
 * @author meler02
 *
 */
@PluginAnnotationComponent(pluginType = WebspherePlugin.PLUGIN)
public interface WebspherePlugin extends AppServerPlugin {
    String PLUGIN = "webspherePlugin";

    String WEBSPHERE_CONFIG_JVM_ELEMENT = "jvmEntries";
    String WEBSPHERE_CONFIG_JVM_ATTRIBUTE = "genericJvmArguments";

    /**
     * Unsets any agent settings from App server configuration so that the App server can be later
     * started without an agent
     * 
     * @param serverName Server Name
     */

    boolean startServer(String serverName);

    /**
     * Stops the App server
     * 
     * @param serverName Server Name
     * @return true if server was stopped with return code 0, false otherwise
     */
    boolean stopServer(String serverName);

    /**
     * Checks whether the App server is running
     * 
     * @param serverName server name
     * @return true if the plugin was able to reach the server, false otherwise
     */
    boolean isServerRunning(String serverName);
    
    /**
     * Checks whether the App server is stopped
     * 
     * @param serverName server name
     * @return true if the plugin was unsable to reach the server, false otherwise
     */
    boolean isServerStopped(String serverName);
    
    /**
     * Checks whether the App server is running
     * 
     * @param urlString server URL
     * @param timeout wait timeout in milliseconds
     * @return true if the plugin was able to reach the server, false otherwise
     */
    boolean isServerRunning(String urlString, int timeout);
    
    /**
     * Checks whether the App server is stopped
     * 
     * @param urlString server URL
     * @param timeout wait timeout in milliseconds
     * @return true if the plugin was unsable to reach the server, false otherwise
     */
    boolean isServerStopped(String urlString, int timeout);
    
    @Override
    boolean installAgent(String agentInstallationFile, String responseFilePath,
        Map<String, String> propertiesMap);

    @Override
    void configureAgent(Path agentInstallDir, Map<String, String> propertiesMap);

    /**
     * Updates Configuration file by adding customServices node with some attributes.
     * @param serverId - configuration name from .json file
     */
    void addPowerPackAttributeToServerConfigFile(String serverId);

    /**
     * Removes customServices node needed for powerpack from server config file
     * @param serverId - configuration name from .json file
     */
    void removePowerPackAttributeToServerConfigFile(String serverId);
}
