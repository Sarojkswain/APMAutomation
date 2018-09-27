package com.ca.apm.systemtest.fld.plugin.vo;

import java.io.File;

public class AppServerConfig extends Configuration {

	private String baseDirectory;
	private String binDirectory;
	private String logDirectory;
	
	public AppServerConfig(String name, String baseDirectory) {
		super(name);
		this.baseDirectory = baseDirectory;
		this.binDirectory = baseDirectory + File.separator + "bin/";
		this.logDirectory = baseDirectory + File.separator + "log/";
	}

	public AppServerConfig(String name, String baseDirectory, String binDirectory, String logDirectory) {
		super(name);
		this.baseDirectory = baseDirectory;
		this.binDirectory = binDirectory;
		this.logDirectory = logDirectory;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String getBinDirectory() {
		return binDirectory;
	}
	public void setBinDirectory(String binDirectory) {
		this.binDirectory = binDirectory;
	}

	public String getLogDirectory() {
		return logDirectory;
	}
	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}
}
