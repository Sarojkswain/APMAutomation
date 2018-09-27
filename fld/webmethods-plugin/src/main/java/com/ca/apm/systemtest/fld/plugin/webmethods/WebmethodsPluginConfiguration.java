package com.ca.apm.systemtest.fld.plugin.webmethods;

import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginConfiguration;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @Author rsssa02
 */
public class WebmethodsPluginConfiguration extends AbstractAppServerPluginConfiguration<WebmethodsPluginConfiguration.WebmethodsServerConfiguration> {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebmethodsServerConfiguration extends AppServerConfiguration {
        String webmBinLocation;
        String webmStartScript;
        String webmStopScript;

        public String getWebmEnvScript() {
            return webmEnvScript;
        }

        public void setWebmEnvScript(String webmEnvScript) {
            this.webmEnvScript = webmEnvScript;
        }

        String webmEnvScript;

        public WMVersion getVersion() {
            return version;
        }

        public void setVersion(WMVersion version) {
            this.version = version;
        }

        WMVersion version;

        public String getWebmInstanceHome() {
            return webmInstanceHome;
        }

        public void setWebmInstanceHome(String webmInstanceHome) {
            this.webmInstanceHome = webmInstanceHome;
        }

        String webmInstanceHome;

        public String getWebmBinLocation() {
            return webmBinLocation;
        }

        public void setWebmBinLocation(String webmBinLocation) {
            this.webmBinLocation = webmBinLocation;
        }

        public String getWebmStartScript() {
            return webmStartScript;
        }

        public void setWebmStartScript(String webmStartScript) {
            this.webmStartScript = webmStartScript;
        }

        public String getWebmStopScript() {
            return webmStopScript;
        }

        public void setWebmStopScript(String webmStopScript) {
            this.webmStopScript = webmStopScript;
        }
    }

    public enum WMVersion {
        WM_VERSION_96, WM_VERSION_97
    }
}
