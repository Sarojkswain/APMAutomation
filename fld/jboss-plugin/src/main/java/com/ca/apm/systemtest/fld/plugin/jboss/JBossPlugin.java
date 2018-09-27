/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.jboss;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

/**
 * @author filja01
 *
 */
@PluginAnnotationComponent(pluginType = JBossPlugin.PLUGIN)
public interface JBossPlugin extends AppServerPlugin {
    String PLUGIN = "jbossPlugin";
    class InstallationParameters {
//        public String agentInstallDir;
//        
//        public String jdkHomeDir;
//        public String jbossInstallDir;
//        public String jbossServerName;
//        public String jbossConfigFile;
        public String emHost;
        public String serverid;
        public boolean enableBRTM = false;
        public boolean enableSOA = true;
        
//        public String envJava;

        public String codeName;
        public String buildId;
        public String buildNumber;
        public String agentExecute;
        public String trussServer;
        public OperatingSystemFamily platform;
       
//        public String logs;
    }

    /**
     * Starts the App server
     * 
     * @param serverId the id of the configured server instance
     * @param withAgent if true, runs the server with the configured agent, otherwise without the agent
     * @return true if server was started with return code 0, false otherwise
     */
    boolean startServer(String serverId, boolean withAgent);

    /**
     * Stops the App server
     * 
     * @param serverId the id of the configured server instance
     * @return true if server was stopped with return code 0, false otherwise
     */
    boolean stopServer(String serverId);

    /**
     * Checks whether the App server is running
     * 
     * @param serverId the id of the configured server instance
     * @param timeout wait timeout in milliseconds
     * @return true if the plugin was able to reach the server, false otherwise
     */
    boolean isServerRunning(String serverId, int timeout);

    /**
     * Checks whether the App server is stopped
     * 
     * @param serverId the id of the configured server instance
     * @param timeout wait timeout in milliseconds
     * @return true if the plugin was unsable to reach the server, false otherwise
     */
    boolean isServerStopped(String serverId, int timeout);
}
