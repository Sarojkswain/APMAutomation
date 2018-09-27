package com.ca.apm.systemtest.fld.test.generic;

/**
 * @Author rsssa02
 */
public abstract class PPSystemTestConstants {

    protected final static String PERF_THREAD_NAME                          = "typePerfThread";

    protected final static String JMX_THREAD_NAME                           = "jmxCollThread";

    protected final static String VALIDATOR_THREAD_NAME                     = "metricValThread";

    protected final static String JM_LOAD_THREAD_NAME                       = "jmeterLoadThread";

    protected final static String JSTAT_THREAD_NAME                         = "jstatThread";

    protected final static int VERIFY_METRICS_MAX_ALLOWED_FAILURES          = 2;

    //constants for results excel report
    protected final static String RESULTS_CPU_SHEET                         = "agent_cpu";

    protected  final static String RESULTS_MEMORY_JMX_SHEET                 = "agent_mem_jmx";

    protected  final static String RESULTS_MEMORY_JSTAT_SHEET               = "agent_mem_jstat";

    protected  final static String xlsOutputFile                            = "saved_results.xls";

    protected final static String EMAIL_SMTP_SERVER                         = "mail.ca.com";


}
