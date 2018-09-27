package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import com.ca.apm.systemtest.fld.plugin.RemoteCallException;

/**
 * Common exception class for Performance Metric Log Collector implementations.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class PerfResultCollectorPluginException extends RemoteCallException {

    public static final String ERR_RESULT_COLLECTION_CONFIG_IS_INVALID = "ERR_RESULT_COLLECTION_CONFIG_IS_INVALID";
    public static final String ERR_RESULT_LOG_COLLECTION_FAILED = "ERR_RESULT_LOG_COLLECTION_FAILED";

    public static final String ERR_JMETER_RESULT_COLLECTION_CONFIG_IS_INVALID = "ERR_JMETER_RESULT_COLLECTION_CONFIG_IS_INVALID";
    public static final String ERR_JMETER_RESULT_COLLECTION_FAILED = "ERR_JMETER_RESULT_COLLECTION_FAILED";

    public static final String ERR_JMX_PERF_RESULT_COLLECTION_CONFIG_IS_INVALID = "ERR_JMX_PERF_RESULT_COLLECTION_CONFIG_IS_INVALID";
    public static final String ERR_JMX_LOG_COLLECTION_FAILED = "ERR_JMX_LOG_COLLECTION_FAILED";

    public static final String ERR_TYPE_PERF_RESULT_COLLECTION_CONFIG_IS_INVALID = "ERR_TYPE_PERF_RESULT_COLLECTION_CONFIG_IS_INVALID";
    public static final String ERR_TYPE_PERF_LOG_COLLECTION_FAILED = "ERR_TYPE_PERF_LOG_COLLECTION_FAILED";

    public static final String ERR_WILY_LOG_COLLECTION_CONFIG_IS_INVALID = "ERR_WILY_LOG_COLLECTION_CONFIG_IS_INVALID";
    public static final String ERR_WILY_LOG_COLLECTION_FAILED = "ERR_WILY_LOG_COLLECTION_FAILED";

    public static final String ERR_SERVER_LOG_COLLECTION_CONFIG_IS_INVALID = "ERR_SERVER_LOG_COLLECTION_CONFIG_IS_INVALID";
    public static final String ERR_SERVER_LOG_COLLECTION_FAILED = "ERR_SERVER_LOG_COLLECTION_FAILED";

    
    /**
     * 
     */
    private static final long serialVersionUID = 8376109652264615805L;

    public PerfResultCollectorPluginException() {
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(String msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(String msg, String errorCode) {
        super(msg, errorCode);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(String errorCode, String pattern, Object... arguments) {
        super(errorCode, pattern, arguments);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(Throwable ex, String errorCode, String pattern,
        Object... arguments) {
        super(ex, errorCode, pattern, arguments);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(Throwable ex) {
        super(ex);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(Throwable ex, String errorCode) {
        super(ex, errorCode);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(String msg, Throwable ex) {
        super(msg, ex);
        // TODO Auto-generated constructor stub
    }

    public PerfResultCollectorPluginException(String msg, Throwable ex, String errorCode) {
        super(msg, ex, errorCode);
        // TODO Auto-generated constructor stub
    }

}
