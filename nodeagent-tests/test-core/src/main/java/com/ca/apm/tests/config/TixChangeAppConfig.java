package com.ca.apm.tests.config;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sinka08
 *
 */
public class TixChangeAppConfig extends BaseJsonAppConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(TixChangeAppConfig.class);
	private String serverDir;
	private String host = "localhost";
	private String port = "3000";
	private final String startupScriptPath;
	private String nodeModulesDir;

	public TixChangeAppConfig(String serverDir, String startupScriptPath, String logPath) {
		super(serverDir, serverDir + File.separator + "config.json");
		this.serverDir = serverDir;
		this.logPath = logPath;
		this.startupScriptPath = startupScriptPath;
		

		File f = new File(serverDir);
		try {
			this.home = f.getCanonicalFile().getParent();
			this.nodeModulesDir = home + File.separator + "node_modules";
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public TixChangeAppConfig(String serverDir, String startupScriptPath, String logPath,
	        String host, String port) {
		this(serverDir, startupScriptPath, logPath);
		this.host = host;
		this.port = port;
	}

	public String getServerDir() {
		return serverDir;
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

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getAppUrlBase() {
		return String.format("http://%s:%s", host, port);
	}
}
