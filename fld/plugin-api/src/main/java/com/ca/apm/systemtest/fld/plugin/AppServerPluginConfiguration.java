/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

import java.util.Map;

/**
 * @author keyja01
 *
 */
public interface AppServerPluginConfiguration<T extends AppServerConfiguration> extends PluginConfiguration {
    public Map<String, T> getServers();

    public T getServerConfig(String serverId);
    
    public void addServerConfig(String serverId, T config);
}
