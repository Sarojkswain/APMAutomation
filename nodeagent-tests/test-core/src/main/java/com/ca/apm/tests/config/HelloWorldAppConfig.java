package com.ca.apm.tests.config;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sinka08
 *
 */
public class HelloWorldAppConfig extends BaseJsonAppConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldAppConfig.class);
	public static final String HTTP_SEVER_PORT_PROPERTY_KEY = "servers.httpServer.port";
	public static final String HTTPS_SEVER_PORT_PROPERTY_KEY = "servers.httpsServer.port";
	private String homeDir;
	private String host = "localhost";
	private final String startupScriptPath;
	private String nodeModulesDir;

	public HelloWorldAppConfig(String homeDir, String startupScriptPath, String logPath) {
		super(homeDir, homeDir + File.separator + "config.json");
		this.homeDir = homeDir;
		this.logPath = logPath;
		this.startupScriptPath = startupScriptPath;

		File f = new File(homeDir);
		try {
			this.home = f.getCanonicalFile().getParent();
			this.nodeModulesDir = home + File.separator + "node_modules";
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public String getServerDir() {
		return homeDir;
	}

	public String getStartupScriptPath() {
		return startupScriptPath;
	}

	public String getNodeModulesDir() {
		return nodeModulesDir;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHttpServerPort() {
		return super.getProperty(HTTP_SEVER_PORT_PROPERTY_KEY);
	}

	public String getHttpsServerPort() {
		return super.getProperty(HTTPS_SEVER_PORT_PROPERTY_KEY);
	}
}
