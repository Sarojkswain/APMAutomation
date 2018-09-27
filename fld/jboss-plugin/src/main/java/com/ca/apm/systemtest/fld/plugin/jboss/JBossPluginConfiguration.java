/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.jboss;

import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author keyja01
 *
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
public class JBossPluginConfiguration extends AbstractAppServerPluginConfiguration<JBossPluginConfiguration.ServerConfig> {
    public enum JBossVersion {
        JBossAS6_1, JBossAS7_1
    }
    
    public static class ServerConfig extends AppServerConfiguration {
        public JBossVersion version;
        public String agentVersion;
        /**
         * JBoss6 - what server config to use (default, all, etc), JBoss7 - which profile to use (standalone, standalone-full)
         */
        public String variant;
        public long startupTimeout = 300000L;
        public int rmiPort = 1090;
        public String extraArgs;
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "ServerConfig [version=" + version + ", agentVersion=" + agentVersion
                + ", variant=" + variant + ", startupTimeout=" + startupTimeout + ", rmiPort="
                + rmiPort + ", extraArgs=" + extraArgs + ", id=" + id + ", status=" + status
                + ", httpPort=" + httpPort + ", baseDir=" + baseDir + ", agentInstalled="
                + agentInstalled + ", defaultAgentInstallDir=" + defaultAgentInstallDir
                + ", currentAgentInstallDir=" + currentAgentInstallDir + ", startScriptWithAgent="
                + startScriptWithAgent + "]";
        }
        
    }
}
