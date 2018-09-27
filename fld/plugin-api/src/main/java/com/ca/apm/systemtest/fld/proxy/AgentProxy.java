/**
 * 
 */
package com.ca.apm.systemtest.fld.proxy;

import java.util.Map;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author keyja01
 *
 */
public interface AgentProxy {
	/**
	 * Returns a client-proxy for accessing the plugin on the remote client.
	 * @param klass The class of the required plugin
	 * @return
	 */
	public <T extends Plugin> T getPlugin(Class<? extends T> klass);
	
	/**
	 * Returns a client-proxy for accessing the plugin on the remote client.
	 * @param name The name of the required plugin - used to disambiguate when there are multiple plugins implementing the same class
	 * @param klass The class of the required plugin
	 * @return
	 */
	public <T extends Plugin> T getPlugin(String name, Class<? extends T> klass);
	
	public Map<String, Plugin> getPlugins();
	public void setPlugins(Map<String, Plugin> plugins);
}
