/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.wls;

import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author keyja01
 *
 */
public class WlsPluginConfiguration
    extends AbstractAppServerPluginConfiguration<WlsPluginConfiguration.WlsServerConfiguration> {

    @JsonInclude(Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WlsServerConfiguration extends AppServerConfiguration {
        String wlsServerStartScript;
        String wlsServerStopScript;
        String wlsServerName;
        WLVersion version;
        int jmxPort = 1099;

        public String getWlsServerStartScript() {
            return wlsServerStartScript;
        }

        public void setWlsServerStartScript(String wlsServerStartScript) {
            this.wlsServerStartScript = wlsServerStartScript;
        }

        public String getWlsServerStopScript() {
            return wlsServerStopScript;
        }

        public void setWlsServerStopScript(String wlsServerStopScript) {
            this.wlsServerStopScript = wlsServerStopScript;
        }

        public void setWlsServerName(String wlsServerName) {
            this.wlsServerName = wlsServerName;
        }

        public String getWlsServerName() {
            return wlsServerName;
        }

        public WLVersion getVersion() {
            return version;
        }

        public void setVersion(WLVersion version) {
            this.version = version;
        }

        public int getJmxPort() {
            return jmxPort;
        }

        public String getJmxPortString() {
            return "" + jmxPort;
        }

        public void setJmxPort(int jmxPort) {
            this.jmxPort = jmxPort;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "WlsServerConfiguration [wlsServerStartScript=" + wlsServerStartScript
                + ", wlsServerStopScript=" + wlsServerStopScript + ", wlsServerName="
                + wlsServerName + ", version=" + version + ", jmxPort=" + jmxPort + ", id=" + id
                + ", status=" + status + ", httpPort=" + httpPort + ", baseDir=" + baseDir
                + ", agentInstalled=" + agentInstalled + ", defaultAgentInstallDir="
                + defaultAgentInstallDir + ", currentAgentInstallDir=" + currentAgentInstallDir
                + ", startScriptWithAgent=" + startScriptWithAgent + "]";
        }

        
    }

    public enum WLVersion {
        WEBLOGICPORTAL_103, WEBLOGIC_103;
    }

}
