/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.util.StreamUtils;

/**
 * @author keyja01
 *
 */
public class FileSystemPluginDataStore implements PluginDataStore {
	private File rootDirectory;

	/**
	 * 
	 */
	protected FileSystemPluginDataStore(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.plugin.datastore.PluginDataStore#getValue(java.lang.String)
	 */
	public String getValue(String key) {
		File f = new File(rootDirectory, key + ".txt");
		if (!f.exists()) {
			return null;
		}
		
		try {
			return StreamUtils.copyToString(new FileInputStream(f), Charset.defaultCharset());
		} catch (IOException ex) {
			throw new PluginDataStoreException("Unable to read value for key " + key, ex);
		}
	}

	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.plugin.datastore.PluginDataStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String key, String value) {
		
		try {
			File f = new File(rootDirectory, key + ".txt");
			if (value == null) {
				if (f.exists()) {
					f.delete();
				}
			} else {
				FileOutputStream out = null;
				if (f.exists()) {
					// if the file exists, we overwrite it
					out = new FileOutputStream(f);
				} else {
					// otherwise we create it
					f.createNewFile();
					out = new FileOutputStream(f);
				}
				out.write(value.getBytes());
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			throw new PluginDataStoreException("Unable to store value to plugin data store", e);
		}
		
	}

}
