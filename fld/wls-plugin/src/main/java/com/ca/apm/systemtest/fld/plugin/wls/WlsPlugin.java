package com.ca.apm.systemtest.fld.plugin.wls;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

public interface WlsPlugin extends AppServerPlugin {
    String WLS_PLUGIN = "wlsPlugin";
    
    class Configuration {
        public String agentInstallDir;
        public String wlsConfigFile;
        public String wlsServerScriptPath;
        public String wlsServerName;
        public String emHost;

        public String codeName;
        public String buildId;
        public String buildNumber;
        public String trussServer;
        public OperatingSystemFamily platform;

        public String logs;
    }

    String WEBLOGIC_CONFIG_JVM_ELEMENT = "jvmEntries";
    String WEBLOGIC_CONFIG_JVM_ATTRIBUTE = "genericJvmArguments";


    /**
     * Unsets any agent settings from App server configuration so that the App server can be later
     * started without an agent.
     * 
     * @param config plugin configuration
     */
    void unsetAgent(Configuration config);

    /**
     * Adds the agent into App server configuration file so that the App server can be later started
     * with this agent.
     * 
     * @param config plugin configuration
     */
    void setAgent(Configuration config);

    /**
     * Starts the App server.
     * 
     * @param config plugin configuration
     * @return true if server was started with return code 0, false otherwise
     */
    boolean startServer(Configuration config);

    /**
     * Stops the App server.
     * 
     * @param config plugin configuration
     * @return true if server was stopped with return code 0, false otherwise
     */
    boolean stopServer(Configuration config);

    /**
     * Checks whether the App server is running.
     * 
     * @param urlString server URL
     * @param timeout wait timeout in milliseconds
     * @return true if the plugin was able to reach the server, false otherwise
     */
    boolean isServerRunning(String urlString, int timeout);

    /**
     * Checks whether the App server is stopped.
     * 
     * @param urlString server URL
     * @param timeout wait timeout in milliseconds
     * @return true if the plugin was unsable to reach the server, false otherwise
     */
    boolean isServerStopped(String urlString, int timeout);

    void installAgent(Configuration config);
}
