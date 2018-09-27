package com.ca.apm.tests.config;

import java.io.File;

import com.ca.apm.automation.utils.configuration.ConfigurationFile;
import com.ca.apm.automation.utils.configuration.ConfigurationFileFactory;

public abstract class BaseAppConfig implements AppConfig {
	protected String home;
	protected final String configPath;
	protected final ConfigurationFile configFile;
	protected final File file;
	protected String logPath;

	public BaseAppConfig(String home, String configPath) {
		this.home = home;
		this.configPath = configPath;
		this.file = new File(configPath);
		this.configFile = new ConfigurationFileFactory().create(file);
	}

	public String getHome() {
		return home;
	}

	public String getConfigFilePath() {
		return configPath;
	}

	@Override
	public String getConfigFileName() {
		return file.getName();
	}

	@Override
	public String getConfigFileDir() {
		return file.getParent();
	}

	@Override
	public File getConfigFile() {
		return file;
	}

	public String getLogPath() {
		return logPath;
	}

	protected void setLogFilePath(String path) {
		this.logPath = path;
	}

	public void addProperty(String key, String value) {
		configFile.addProperty(key, value);
	}

	public void updateProperty(String key, String value) {
		configFile.addOrUpdate(key, value);
	}

	public abstract String getProperty(String key);

	public String getBackupDir() {
		return getConfigFileDir() + File.separator + "backup";
	}
}
