/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.cm;

import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;

/**
 * @author KEYJA01
 *
 */
public interface ConfigurationChangeListener <T extends PluginConfiguration> {
    public void onChange(PluginConfiguration config);
    
    public Class<T> getConfigurationClass();
}
