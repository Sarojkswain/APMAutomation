
/*
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

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
