/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.websphere;

import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author keyja01
 *
 */
public class WebSpherePluginConfiguration extends AbstractAppServerPluginConfiguration<WebSpherePluginConfiguration.WebSphereServerConfiguration> {
    @JsonInclude(Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebSphereServerConfiguration extends AppServerConfiguration {
        String wasConfigFile;
        String wasServerStartScript;
        String wasServerStopScript;
        String wasServerName;


        public String getWasConfigFile() {
            return wasConfigFile;
        }

        public void setWasConfigFile(String wasConfigFile) {
            this.wasConfigFile = wasConfigFile;
        }

        public String getWasServerStartScript() {
            return wasServerStartScript;
        }

        public void setWasServerStartScript(String wasServerStartScript) {
            this.wasServerStartScript = wasServerStartScript;
        }

        public String getWasServerStopScript() {
            return wasServerStopScript;
        }

        public void setWasServerStopScript(String wasServerStopScript) {
            this.wasServerStopScript = wasServerStopScript;
        }

        public void setWasServerName(String wasServerName) {
            this.wasServerName = wasServerName;
        }

        public String getWasServerName() {
            return wasServerName;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "WebSphereServerConfiguration [wasConfigFile=" + wasConfigFile
                + ", wasServerStartScript=" + wasServerStartScript + ", wasServerStopScript="
                + wasServerStopScript + ", wasServerName=" + wasServerName + ", id=" + id
                + ", status=" + status + ", httpPort=" + httpPort + ", baseDir=" + baseDir
                + ", agentInstalled=" + agentInstalled + ", defaultAgentInstallDir="
                + defaultAgentInstallDir + ", currentAgentInstallDir=" + currentAgentInstallDir
                + ", startScriptWithAgent=" + startScriptWithAgent + "]";
        }
        
    }
}
