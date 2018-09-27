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
