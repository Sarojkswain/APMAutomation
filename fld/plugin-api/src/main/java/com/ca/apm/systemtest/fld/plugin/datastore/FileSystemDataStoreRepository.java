/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.datastore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author keyja01
 *
 */
public class FileSystemDataStoreRepository implements InitializingBean {
	private String repoDirectory;
	private Map<String, PluginDataStore> dataStoreMap = new HashMap<String, PluginDataStore>();

	/**
	 * 
	 */
	public FileSystemDataStoreRepository() {
	}

	public void afterPropertiesSet() throws Exception {
		if (repoDirectory == null) {
			throw new IllegalArgumentException("Attribute repoDirectory is mandatory");
		}
		
		File f = new File(repoDirectory);
		if (!f.exists() || !f.isDirectory()) {
			throw new IllegalArgumentException("repoDirectory must represent a writable directory");
		}
		
		// test if we can write to this directory
		File tmpFile = File.createTempFile("FLD", "tmp", f);
		if (!tmpFile.exists()) {
			throw new IllegalArgumentException("Cannot write to repoDirectory " + repoDirectory);
		}
		tmpFile.deleteOnExit();
	}
	

	/**
	 * Returns a {@link PluginDataStore} from the repository.
	 * @param name
	 * @return
	 */
	public PluginDataStore getDataStoreForPlugin(String name) {
		PluginDataStore ds = dataStoreMap.get(name);
		if (ds == null) {
			// create new ds and put in map
			File rootDir = new File(name, "repoDirectory");
			ds = new FileSystemPluginDataStore(rootDir);
			dataStoreMap.put(name, ds);
		}
		
		return ds;
	}
	

	public String getRepoDirectory() {
		return repoDirectory;
	}

	public void setRepoDirectory(String repoDirectory) {
		this.repoDirectory = repoDirectory;
	}
}
