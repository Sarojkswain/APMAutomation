/**
 * 
 */
package com.ca.apm.systemtest.fld.agent;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author keyja01
 *
 */
public interface PluginRepository {
	public Plugin findPlugin(String name);
}
