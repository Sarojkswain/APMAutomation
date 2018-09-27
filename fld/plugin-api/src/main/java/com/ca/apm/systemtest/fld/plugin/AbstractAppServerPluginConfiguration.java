/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author keyja01
 *
 */
public class AbstractAppServerPluginConfiguration<T extends AppServerConfiguration> implements AppServerPluginConfiguration<T> {
    private Map<String, T> servers = new HashMap<>();

    @Override
    public Map<String, T> getServers() {
        return Collections.unmodifiableMap(servers);
    }
    
    public void setServers(Map<String, T> servers) {
        this.servers.clear();
        if (servers != null) {
            this.servers.putAll(servers);
        }
    }

    @Override
    public T getServerConfig(String serverId) {
        return servers.get(serverId);
    }

    @Override
    public void addServerConfig(String serverId, T config) {
        servers.put(serverId, config);
    }
}
