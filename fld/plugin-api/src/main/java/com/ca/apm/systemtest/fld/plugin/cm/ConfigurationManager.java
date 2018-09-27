package com.ca.apm.systemtest.fld.plugin.cm;

import com.ca.apm.systemtest.fld.plugin.PluginConfiguration;

public interface ConfigurationManager {
    /**
     * Persists changes to the plugin's configuration.  Any listeners are notified of changes.
     * @param pluginId
     * @param cfg
     */
    public <T extends PluginConfiguration> void savePluginConfiguration(String pluginId, T cfg);
    
    /**
     * Loads the requested configuration.
     * @param pluginId
     * @param klass
     * @return
     */
    public <T extends PluginConfiguration> T loadPluginConfiguration(String pluginId, Class<T> klass);
    
    
    public <T extends PluginConfiguration> void registerConfigurationChangeListener(String pluginId, ConfigurationChangeListener<T> listener, Class<T> klass);
}
