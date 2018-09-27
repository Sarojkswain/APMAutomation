package com.ca.apm.webui.test.framework.tools.qc;

/**
 * The <code>TestCaseData</code> represents...
 * 
 * @author
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class TestCaseData
{

    private String fTestCaseId = null;

    private String fStatus     = null;

    public TestCaseData(String testCaseId, String status)
    {
        this.fTestCaseId = testCaseId;
        this.fStatus = status;
    }

    public String getStatus()
    {
        return fStatus;
    }

    public String getTestCaseId()
    {
        return fTestCaseId;
    }
} // end class