package com.ca.apm.systemtest.fld.plugin;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.ca.apm.systemtest.fld.plugin.vo.KeyValuePair;


public interface AppServerPlugin extends Plugin {
    /**
     * Starts the specified instance of the application server
     *
     * @param serverId
     */
    void startAppServer(String serverId);

    /**
     * Stops the specified instance of the application server
     *
     * @param serverId
     */
    void stopAppServer(String serverId);


    /**
     * Downloads and installs the Introscope agent using the proper "no-installer" package
     * for the specific app server.  It parses the specification, and either downloads the
     * installation .zip file from truss or artifactory
     *
     * @param serverId
     * @param noInstallerSpecification "truss:branch:buildNumber[:version]" or "maven:version"
     * @param extraProperties          Properties that should be set in agent's Introscope_Agent
     *                                 .properties config file
     * @param extraModules             a list of extra modules that should be copied into
     *                                 ${agent}/core/ext
     * @param brtmExt                  Install BRTM (old agent) or Browser Agent (new agent)
     *                                 extension.
     */
    void installAgentNoInstaller(String serverId, String noInstallerSpecification,
        Map<String, String> extraProperties,
        List<String> extraModules, boolean brtmExt);


    /**
     * Uninstalls the current agent from the specified server instance. If the agent is not
     * currently installed,
     * the method simply returns.
     *
     * @param serverId
     * @param archiveExisting if true, the existing "wily" directory is renamed to
     *                        wily-${timestamp}, otherwise it is deleted
     */
    void uninstallAgentNoInstaller(String serverId, boolean archiveExisting);

    /**
     * Installs an Agent using the installer for the platform, and a specified response file and
     * properties.
     *
     * @param agentInstallationFile executable file for agent installation
     * @param responseFilePath      response file. If NULL or nonexistent it will be created
     * @param propertiesMap         parameters for the response file
     * @return true if the agent was installed with return code 0, false otherwise
     */
    boolean installAgent(String agentInstallationFile, String responseFilePath,
        Map<String, String> propertiesMap);


    /**
     * Configures an existing agent
     *
     * @param agentInstallDir installation directory
     * @param propertiesMap   properties map with parameters for the configuration (profile) file
     */
    void configureAgent(Path agentInstallDir, Map<String, String> propertiesMap);

    /**
     * Configures a server instance in the plugin's configuration.  If the server configuration
     * already exists,
     * it is updated, otherwise it is created.
     *
     * @param serverId
     * @param httpPort
     */
    void configureServerInstance(String serverId, int httpPort, KeyValuePair... values);

    /**
     * Returns true if the APM agent is already installed
     *
     * @param serverId
     * @return
     */
    boolean isAgentInstalled(String serverId);

    /**
     * @param serverName - name of server configuration
     * @param unset      - boolean flag to enable / disable agent integration
     * @param legacy     - boolean flag to use legacy .NoRedef.profile
     */
    void setupAgent(String serverName, boolean unset, boolean legacy);

    /**
     * Returns application server configuration owned by a specific LO application server plugin. 
     * 
     * 
     * @param serverId    id of the app server configuration under which 
     *                    it is stored in the LO app server plugin configuration
     * @return            app server configuration
     */
    AppServerConfiguration getAppServerConfiguration(String serverId);
    
}
