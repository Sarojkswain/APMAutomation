package com.ca.apm.tests.utils;

import com.ca.apm.automation.common.mockem.RequestProcessor;

public interface ITraceValidationData extends IAgentNameExpr{

	public int getNumExpected();

	public long getDuration();

	public boolean getShouldCheckTimeStamp();

	public RequestProcessor.ITransactionTraceValidator getValidator();

}
