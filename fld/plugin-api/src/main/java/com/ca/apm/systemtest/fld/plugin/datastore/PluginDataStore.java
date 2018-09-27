/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.datastore;

/**
 * Simple API for persisting configuration for the plugins.  Each plugin should have its own
 * isolated instance of the PluginDataStore.
 * 
 * @author keyja01
 *
 */
public interface PluginDataStore {
	/**
	 * Returns the value for a given key, or NULL if not found.
	 * @param key
	 * @return
	 */
	public String getValue(String key);
	
	/**
	 * Sets the value for a given key.  If value is NULL, the key is removed from the datastore
	 * @param key
	 * @param value
	 */
	public void putValue(String key, String value);
}
