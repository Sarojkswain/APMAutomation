package com.ca.apm.tests.config;

import java.io.File;
import java.io.IOException;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wily.org.apache.commons.io.FileUtils;

/**
 * @author sinka08
 *
 */
public class NodeJSProbeConfig extends BaseJsonAppConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeJSProbeConfig.class);
	private static final String DEFAULT_LOG_FILE_NAME = "Probe.log";
	private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";
	public static final String LOG_PATH_PROPERTY_KEY = "logging.logFile";
	public static final String LOG_LEVEL_PROPERTY_KEY = "logging.logLevel";
	public static final String PROBE_NAME_PROPERTY_KEY = "probeName";
	public static final String PROBE_NAME_ENV_PROPERTY_KEY ="probeNameEnvKey";
	public static final String HTTP_REQ_DEC_PROPERTY_KEY = "http.client.requestDecorationEnabled";
	
	private final String logFileDir;
	private final String probeHome;

	public NodeJSProbeConfig(String home) {
		super(home, home + File.separator + DEFAULT_CONFIG_FILE_NAME);
		this.logFileDir = home + File.separator + "logs";
		this.probeHome = new File(getHome(), "/lib/probes").getAbsolutePath();
		
		try {
			// create logs dir under probe home
			FileUtils.forceMkdir(new File(this.logFileDir));
			this.logPath = logFileDir + File.separator + DEFAULT_LOG_FILE_NAME;
			updateLogFilePath(logPath);
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void updateLogLevel(LoggingLevel level) {
		super.updateProperty(LOG_LEVEL_PROPERTY_KEY, level.getLevel().toString().toLowerCase());
	}

	public void updateLogFilePath(String path) {
		Args.notNull(path, "NodeJS probe log file path");
		super.updateProperty(LOG_PATH_PROPERTY_KEY, path);
		setLogFilePath(path);
	}

	public void updateLogFileName(String name) {
		String path = logFileDir + File.separator + name;
		updateLogFilePath(path);
	}

	@Override
	public String getLogPath() {
		String path = super.getLogPath();

		if (path == null) {
			path = getProperty(LOG_PATH_PROPERTY_KEY);
		}
		return path;
	}
	
	public String getProbeHome() {
		return probeHome;
	}
}
