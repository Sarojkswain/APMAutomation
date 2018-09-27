package com.ca.apm.tests.utils;

import com.ca.apm.automation.common.mockem.RequestProcessor.IMetricNameValueValidator;

public interface IMetricAssertionData extends IAgentNameExpr {
	public long getDuration();

	public boolean isFailOnTimeOut();

	public IMetricNameValueValidator getValidator();
}
