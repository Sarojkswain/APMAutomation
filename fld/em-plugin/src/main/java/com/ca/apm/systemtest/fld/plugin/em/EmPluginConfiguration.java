/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.em;

import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Configuration class for the {@link EmPlugin}
 * @author keyja01
 *
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EmPluginConfiguration implements PluginConfiguration {
    private String defaultLogDir;
    private String defaultInstallDir;
    private String defaultSmartStorDir;
    private String defaultGcLogFile;
    private int defaultEmPort = 5001;
    
    private int defaultDbPort = 5432;
    private String defaultDbSchema = "cemdb";
    private String defaultDbUserName = "cemadmin";
    private String defaultDbUserPass = "password";
    private String defaultDbAdminName = "postgres";
    private String defaultDbAdminPass = "password";
    
    private boolean installed = false;
    private boolean started = false;
    private String currentInstallDir;
    private String currentLogDir;
    private String currentSmartStorDir;
    private Integer currentPort;
    private String currentGcLogFile;
    private String uninstallerFile;
    

    /**
     * 
     */
    public EmPluginConfiguration() {
    }

    public String getDefaultLogDir() {
        return defaultLogDir;
    }

    public void setDefaultLogDir(String defaultLogDir) {
        this.defaultLogDir = defaultLogDir;
    }

    public String getDefaultInstallDir() {
        return defaultInstallDir;
    }

    public void setDefaultInstallDir(String defaultInstallDir) {
        this.defaultInstallDir = defaultInstallDir;
    }

    public String getDefaultSmartStorDir() {
        return defaultSmartStorDir;
    }

    public void setDefaultSmartStorDir(String defaultSmartStorDir) {
        this.defaultSmartStorDir = defaultSmartStorDir;
    }

    public int getDefaultEmPort() {
        return defaultEmPort;
    }

    public void setDefaultEmPort(int defaultPort) {
        this.defaultEmPort = defaultPort;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getCurrentInstallDir() {
        return currentInstallDir;
    }

    public void setCurrentInstallDir(String currentInstallDir) {
        this.currentInstallDir = currentInstallDir;
    }

    public String getCurrentLogDir() {
        return currentLogDir;
    }

    public void setCurrentLogDir(String currentLogDir) {
        this.currentLogDir = currentLogDir;
    }

    public String getCurrentSmartStorDir() {
        return currentSmartStorDir;
    }

    public void setCurrentSmartStorDir(String currentSmartStorDir) {
        this.currentSmartStorDir = currentSmartStorDir;
    }

    public Integer getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(Integer currentPort) {
        this.currentPort = currentPort;
    }

    public String getUninstallerFile() {
        return uninstallerFile;
    }

    public void setUninstallerFile(String uninstallerFile) {
        this.uninstallerFile = uninstallerFile;
    }

    public String getDefaultGcLogFile() {
        return defaultGcLogFile;
    }

    public void setDefaultGcLogFile(String defaultGcLogFile) {
        this.defaultGcLogFile = defaultGcLogFile;
    }

    public String getCurrentGcLogFile() {
        return currentGcLogFile;
    }

    public void setCurrentGcLogFile(String currentGcLogFile) {
        this.currentGcLogFile = currentGcLogFile;
    }

    public int getDefaultDbPort() {
        return defaultDbPort;
    }

    public void setDefaultDbPort(int defaultDbPort) {
        this.defaultDbPort = defaultDbPort;
    }

    public String getDefaultDbSchema() {
        return defaultDbSchema;
    }

    public void setDefaultDbSchema(String defaultDbSchema) {
        this.defaultDbSchema = defaultDbSchema;
    }

    public String getDefaultDbUserName() {
        return defaultDbUserName;
    }

    public void setDefaultDbUserName(String defaultDbUserName) {
        this.defaultDbUserName = defaultDbUserName;
    }

    public String getDefaultDbUserPass() {
        return defaultDbUserPass;
    }

    public void setDefaultDbUserPass(String defaultDbUserPass) {
        this.defaultDbUserPass = defaultDbUserPass;
    }

    public String getDefaultDbAdminName() {
        return defaultDbAdminName;
    }

    public void setDefaultDbAdminName(String defaultDbAdminName) {
        this.defaultDbAdminName = defaultDbAdminName;
    }

    public String getDefaultDbAdminPass() {
        return defaultDbAdminPass;
    }

    public void setDefaultDbAdminPass(String defaultDbAdminPass) {
        this.defaultDbAdminPass = defaultDbAdminPass;
    }
}
