package com.ca.apm.systemtest.fld.plugin.tibco;

import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author rsssa02.
 */
public class TibcoPluginConfiguration
    extends AbstractAppServerPluginConfiguration<TibcoPluginConfiguration.TibcoServerConfiguration> {
    @JsonInclude(Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TibcoServerConfiguration extends AppServerConfiguration {
        String tibServerStartScript;
        String tibServerName;
        String tibcoDomainName;
        String tibTraBinLocation;
        String tibServiceNames;
        String tibBWHome;
        String tibTradeDir;
        String tibTraProcessName;
        String tibHeapSzie;
        String tibHTTPThreadSize;
        // Tibco version
        TIBVersion version;

        public String getTibHeapSzie() {
            return tibHeapSzie;
        }

        public void setTibHeapSzie(String tibHeapSzie) {
            this.tibHeapSzie = tibHeapSzie;
        }

        public String getTibHTTPThreadSize() {
            return tibHTTPThreadSize;
        }

        public void setTibHTTPThreadSize(String tibHTTPThreadSize) {
            this.tibHTTPThreadSize = tibHTTPThreadSize;
        }

        public String getTibBWHome() {
            return tibBWHome;
        }

        public void setTibBWHome(String tibBWHome) {
            this.tibBWHome = tibBWHome;
        }

        public String getTibTraProcessName() {
            return tibTraProcessName;
        }

        public void setTibTraProcessName(String tibTraProcessName) {
            this.tibTraProcessName = tibTraProcessName;
        }

        public String getTibTradeDir() {
            return tibTradeDir;
        }

        public void setTibTradeDir(String tibTradeDir) {
            this.tibTradeDir = tibTradeDir;
        }

        public String getTibTraBinLocation() {
            return tibTraBinLocation;
        }

        public void setTibTraBinLocation(String tibTraBinLocation) {
            this.tibTraBinLocation = tibTraBinLocation;
        }

        public void setTibServiceNames(String tibServiceNames) {
            this.tibServiceNames = tibServiceNames;
        }

        public String getTibServiceNames() {

            return tibServiceNames;
        }

        public void setTibcoDomainName(String tibcoDomainName) {
            this.tibcoDomainName = tibcoDomainName;
        }

        public String getTibcoDomainName() {

            return tibcoDomainName;
        }

        public String getTibServerStartScript() {
            return tibServerStartScript;
        }

        public void setTibServerStartScript(String tibServerStartScript) {
            this.tibServerStartScript = tibServerStartScript;
        }

        public void setTibServerName(String tibServerName) {
            this.tibServerName = tibServerName;
        }

        public String getTibServerName() {
            return tibServerName;
        }

        public TIBVersion getVersion() {
            return version;
        }

        public void setVersion(TIBVersion version) {
            this.version = version;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TibcoServerConfiguration [tibServerStartScript=" + tibServerStartScript
                + ", tibServerName=" + tibServerName + ", tibcoDomainName=" + tibcoDomainName
                + ", tibTraBinLocation=" + tibTraBinLocation + ", tibServiceNames="
                + tibServiceNames + ", tibBWHome=" + tibBWHome + ", tibTradeDir=" + tibTradeDir
                + ", tibTraProcessName=" + tibTraProcessName + ", tibHeapSzie=" + tibHeapSzie
                + ", tibHTTPThreadSize=" + tibHTTPThreadSize + ", version=" + version + ", id="
                + id + ", status=" + status + ", httpPort=" + httpPort + ", baseDir=" + baseDir
                + ", agentInstalled=" + agentInstalled + ", defaultAgentInstallDir="
                + defaultAgentInstallDir + ", currentAgentInstallDir=" + currentAgentInstallDir
                + ", startScriptWithAgent=" + startScriptWithAgent + "]";
        }

        
    }

    public enum TIBVersion {
        TIBCOBW_58, TIBCOBW_511;
    }

}
