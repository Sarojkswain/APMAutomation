package com.ca.apm.tests.utils;

public class FullAgentNameExpr implements IAgentNameExpr {
	private static final String MATCH_ALL_REGEX = ".*";
	private String hostNameExpr;
	private String processNameExpr;
	private String agentNameExpr;

	public FullAgentNameExpr() {
		hostNameExpr = MATCH_ALL_REGEX;
		processNameExpr = MATCH_ALL_REGEX;
		agentNameExpr = MATCH_ALL_REGEX;
	}

	public FullAgentNameExpr(String hostNameExpr, String processNameExpr, String agentNameExpr) {
		this.hostNameExpr = hostNameExpr;
		this.processNameExpr = processNameExpr;
		this.agentNameExpr = agentNameExpr;
	}

	@Override
	public String getProcessName() {
		return processNameExpr;
	}

	@Override
	public String getHostName() {
		return hostNameExpr;
	}

	@Override
	public String getAgentName() {
		return agentNameExpr;
	}

}
