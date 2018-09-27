package com.ca.apm.systemtest.fld.plugin.run;

import java.util.List;

public class CommandConfig { 
	String name;
	List<String> cmdLine;
	String workingDir;
	private Environment env;
	private String extractScript;
	public Process proc;


	public List<String> getCmdLine() {
		return cmdLine;
	}

	public void setCmdLine(List<String> cmdLine) {
		this.cmdLine = cmdLine;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public String getExtractScript() {
		return extractScript;
	}

	public void setExtractScript(String extractScript) {
		this.extractScript = extractScript;
	}
}
