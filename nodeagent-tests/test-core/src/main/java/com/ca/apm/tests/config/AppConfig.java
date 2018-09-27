package com.ca.apm.tests.config;

import java.io.File;

/**
 * @author sinka08
 *
 */
public interface AppConfig {
	public String getHome();

	public String getConfigFilePath();
	
	public String getConfigFileName();
	
	public String getConfigFileDir();
	
	public File getConfigFile();

	public String getLogPath();
	
	public void addProperty(String key, String value);

	public void updateProperty(String key, String value);
	
	public String getProperty(String key);
}

