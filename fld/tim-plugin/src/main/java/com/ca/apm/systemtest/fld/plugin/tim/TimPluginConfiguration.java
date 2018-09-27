/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.tim;

import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;

/**
 * @author keyja01
 *
 */
public class TimPluginConfiguration implements PluginConfiguration {
    /** The base directory for the installed components, usually /opt */
    private String currentInstallDirectory;
    private boolean installed = false;
    private String defaultInstallDirectory = "/opt";

    public String getCurrentInstallDirectory() {
        return currentInstallDirectory;
    }

    public void setCurrentInstallDirectory(String currentInstallDirectory) {
        this.currentInstallDirectory = currentInstallDirectory;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getDefaultInstallDirectory() {
        return defaultInstallDirectory;
    }

    public void setDefaultInstallDirectory(String defaultInstallDirectory) {
        this.defaultInstallDirectory = defaultInstallDirectory;
    }
}
