package com.ca.apm.systemtest.fld.plugin.tim;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * This is FLD TIM plugin interface.
 *
 * @author haiva01
 */

@PluginAnnotationComponent(pluginType = "timPlugin")
public interface TimPlugin extends Plugin {
    public enum InstallStatus {
        None, Installing, Installed, Error
    }
    
    /**
     * Starts installing the TIM into the customary location
     */
    public void startInstall(String trussBaseUrl, String timCodeName, String timBuildNumber, String timBuildId, String timFilename);
    
    public InstallStatus checkInstallStatus();
    
    void timUninstall(String prefixPath);

    Integer executeScript(boolean waitFor, String... command);

    void prepareFldScripts(String groupId, String artifactId, String version, String classifier, String type);

    void setFldScriptsWorkDir(String fldScriptsWorkDir);

    String getFldScriptsWorkDir();

    boolean isScriptAvailable(String script);

}
