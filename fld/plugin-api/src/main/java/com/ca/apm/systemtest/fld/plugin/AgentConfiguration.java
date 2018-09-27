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
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
public class AgentConfiguration implements PluginConfiguration {
    private String defaultWorkDir;

    public String getDefaultWorkDir() {
        return defaultWorkDir;
    }

    public void setDefaultWorkDir(String defaultWorkDir) {
        this.defaultWorkDir = defaultWorkDir;
    }
}
