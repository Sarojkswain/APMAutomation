/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.tomcat;

import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;

/**
 * @author keyja01
 *
 */
public class TomcatPluginConfiguration extends AbstractAppServerPluginConfiguration<TomcatPluginConfiguration.TomcatServerConfig> {
    public enum TomcatVersion {
        Tomcat6, Tomcat7, Tomcat8
    }
    public static class TomcatServerConfig extends AppServerConfiguration {
        public TomcatVersion version;

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TomcatServerConfig [version=" + version + ", id=" + id + ", status=" + status
                + ", httpPort=" + httpPort + ", baseDir=" + baseDir + ", agentInstalled="
                + agentInstalled + ", defaultAgentInstallDir=" + defaultAgentInstallDir
                + ", currentAgentInstallDir=" + currentAgentInstallDir + ", startScriptWithAgent="
                + startScriptWithAgent + "]";
        }
        
    }
}
