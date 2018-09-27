package com.ca.apm.tests.utils;


import com.ca.apm.automation.common.mockem.MetricValidatorFactory;
import com.ca.apm.automation.common.mockem.MetricValidatorFactory.SimpleMetricValidator;
import com.ca.apm.automation.common.mockem.RequestProcessor.IMetricNameValueValidator;

/**
 * @author sinka08
 *
 */
public class MetricAssertionData implements IMetricAssertionData{
	private IMetricNameValueValidator validator;
	private long duration;
	private boolean failOnTimeOut;
	private String hostNameExpr ;
	private String processNameExpr ;
	private String agentNameExpr ;

	private MetricAssertionData(BaseBuilder b) {
		this.validator = b.validator;
		this.duration = b.duration;
		this.failOnTimeOut = b.failOnTimeOut;		
		this.hostNameExpr = b.hostNameExpr;
		this.processNameExpr = b.processNameExpr;
		this.agentNameExpr = b.agentNameExpr;
	}

	public long getDuration() {
		return duration;
	}

	public boolean isFailOnTimeOut() {
		return failOnTimeOut;
	}

	public IMetricNameValueValidator getValidator() {
		return validator;
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

	public static class Builder extends BaseBuilder {

		public Builder(String metricName, long expectedValue) {
			super(new SimpleMetricValidator(metricName, expectedValue));
		}
	}

	public static class MinMaxBuilder extends BaseBuilder {

		public MinMaxBuilder(String metricName, long min, long max) {
			super(new MetricValidatorFactory.MinMaxMetricValidator(metricName, min, max));
		}
	}
	
	public static class MaxDataPointBuilder extends BaseBuilder {

        public MaxDataPointBuilder(String metricName, long max) {
            super(new MetricValidatorFactory.MaxDataPointValueValidator(metricName, max));
        }
    }
	
	public static class RegexMetricNameBuilder extends BaseBuilder {

		public RegexMetricNameBuilder(String metricName, long expectedValue) {
			super(new MetricValidatorFactory.RegexMetricValidator(metricName, expectedValue));
		}
	}

	public abstract static class BaseBuilder {
		private static final String MATCH_ALL_REGEX = ".*";
		protected IMetricNameValueValidator validator;
		private long duration = 60000;
		private boolean failOnTimeOut = true;
		private String hostNameExpr ;
		private String processNameExpr ;
		private String agentNameExpr ;


		public BaseBuilder(IMetricNameValueValidator validator) {
			hostNameExpr = MATCH_ALL_REGEX;
			 processNameExpr = MATCH_ALL_REGEX;
			agentNameExpr = MATCH_ALL_REGEX;
			this.validator = validator;
		}
		
		public BaseBuilder setDuration(long duration) {
			this.duration = duration;
			return this;
		}

		public BaseBuilder setFailOnTimeOut(boolean failOnTimeOut) {
			this.failOnTimeOut = failOnTimeOut;
			return this;
		}

		public MetricAssertionData build() {
			return new MetricAssertionData(this);
		}

		public BaseBuilder setHostName(String hostNameExpr) {
			this.hostNameExpr = hostNameExpr;
			return this;
		}

		public BaseBuilder setProcessName(String processNameExpr) {
			this.processNameExpr = processNameExpr;
			return this;
		}

		public BaseBuilder setAgentName(String agentNameExpr) {
			this.agentNameExpr = agentNameExpr;
			return this;
		}	
	}

}
