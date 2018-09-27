package com.ca.apm.tests.config;

public enum LoggingLevel {
	ERROR("ERROR"), INFO("INFO"), DEBUG("DEBUG"), TRACE("TRACE");

	private String level;

	LoggingLevel(String level) {
		this.level = level;
	}

	public String getLevel() {
		return level;
	}

	public String toString() {
		return getLevel();
	}

}
