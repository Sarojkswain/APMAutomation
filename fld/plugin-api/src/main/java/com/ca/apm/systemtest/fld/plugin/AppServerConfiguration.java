/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author keyja01
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.ALWAYS)
public abstract class AppServerConfiguration {
    public String id;
    public ServerStatus status;
    public int httpPort;
    public String baseDir;
    
    public boolean agentInstalled = false;
    public String defaultAgentInstallDir = "wily";
    public String currentAgentInstallDir;
    public String startScriptWithAgent;
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AppServerConfiguration [id=" + id + ", status=" + status + ", httpPort=" + httpPort
            + ", baseDir=" + baseDir + ", agentInstalled=" + agentInstalled
            + ", defaultAgentInstallDir=" + defaultAgentInstallDir + ", currentAgentInstallDir="
            + currentAgentInstallDir + ", startScriptWithAgent=" + startScriptWithAgent + "]";
    }
    
    
}
