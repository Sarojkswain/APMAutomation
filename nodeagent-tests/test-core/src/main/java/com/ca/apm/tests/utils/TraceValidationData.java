package com.ca.apm.tests.utils;

import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.RequestProcessor.ITransactionTraceValidator;

public class TraceValidationData implements ITraceValidationData{
	private ITransactionTraceValidator validator;
	private long duration;
	private boolean shouldCheckTimeStamp;
	private int numExpected;
	private String hostNameExpr ;
	private String processNameExpr ;
	private String agentNameExpr ;

	private TraceValidationData(Builder b) {
		this.validator = b.validator;
		this.duration = b.duration;
		this.shouldCheckTimeStamp = b.shouldCheckTimeStamp;
		this.numExpected = b.numExpected;
		this.hostNameExpr = b.hostNameExpr;
		this.processNameExpr = b.processNameExpr;
		this.agentNameExpr = b.agentNameExpr;
	}

	public long getDuration() {
		return duration;
	}

	public boolean getShouldCheckTimeStamp() {
		return shouldCheckTimeStamp;
	}

	public RequestProcessor.ITransactionTraceValidator getValidator() {
		return validator;
	}

	public int getNumExpected(){
		return numExpected;
	}

	@Override
	public String getHostName() {
		return hostNameExpr;
	}

	@Override
	public String getProcessName() {
		return processNameExpr;
	}

	@Override
	public String getAgentName() {
		return agentNameExpr;
	}

	public static class Builder {
		protected ITransactionTraceValidator validator;
		private long duration = 60000;
		private boolean shouldCheckTimeStamp = true;
	    private int numExpected;
		private String hostNameExpr ;
		private String processNameExpr ;
		private String agentNameExpr ;
		private static final String MATCH_ALL_REGEX = ".*";

		public Builder(int numExpected, ITransactionTraceValidator validator) {
			hostNameExpr = MATCH_ALL_REGEX;
			processNameExpr = MATCH_ALL_REGEX;
			agentNameExpr = MATCH_ALL_REGEX;
			this.numExpected = numExpected;
			this.validator = validator;
		}
		
		public Builder setDuration(long duration) {
			this.duration = duration;
			return this;
		}

		public Builder setShouldCheckTimeStamp(boolean shouldCheckTimeStamp) {
			this.shouldCheckTimeStamp = shouldCheckTimeStamp;
			return this;
		}

		public Builder setAgentExpr(IAgentNameExpr fullAgentNameExpr){
			this.hostNameExpr = fullAgentNameExpr.getHostName();
			this.processNameExpr = fullAgentNameExpr.getProcessName();
			this.agentNameExpr = fullAgentNameExpr.getAgentName();
			return this;
		}
		
		public Builder setHostName(String hostNameExpr) {
			this.hostNameExpr = hostNameExpr;
			return this;
		}

		public Builder setProcessName(String processNameExpr) {
			this.processNameExpr = processNameExpr;
			return this;
		}

		public Builder setAgentName(String agentNameExpr) {
			this.agentNameExpr = agentNameExpr;
			return this;
		}

		public TraceValidationData build() {
			return new TraceValidationData(this);
		}

	}

}
