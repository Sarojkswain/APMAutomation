package com.ca.apm.tests.config;

import com.ca.apm.automation.utils.configuration.JsonConfiguration;

public class BaseJsonAppConfig extends BaseAppConfig {

	public BaseJsonAppConfig(String home, String configPath) {
		super(home, configPath);
	}

	public void addProperty(String key, Boolean value) {
		configFile.addProperty(key, value);
	}

	public void updateProperty(String key, Boolean value) {
		configFile.addOrUpdate(key, value);
	}

	public void addProperty(String key, Number value) {
		configFile.addProperty(key, value);
	}

	public void updateProperty(String key, Number value) {
		configFile.addOrUpdate(key, value);
	}

	@Override
	public String getProperty(String key) {
		if (configFile instanceof JsonConfiguration) {
			JsonConfiguration config = (JsonConfiguration) configFile;
			return String.valueOf(config.getProperty(key));
		}
		return null;
	}

}
