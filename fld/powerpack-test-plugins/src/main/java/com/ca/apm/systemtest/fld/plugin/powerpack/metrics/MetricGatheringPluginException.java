package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import com.ca.apm.systemtest.fld.plugin.RemoteCallException;

/**
 * Exception class for metric gathering plugins.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 * 
 */
public class MetricGatheringPluginException extends RemoteCallException {

    public static final String ERR_MONITORING_CONFIG_IS_INVALID = "ERR_MONITORING_CONFIG_IS_INVALID";
    public static final String ERR_JMX_MONITORING_FAILED = "ERR_JMX_MONITORING_FAILED";
    public static final String ERR_TYPE_PERF_MONITORING_FAILED = "ERR_TYPE_PERF_MONITORING_FAILED";
    public static final String ERR_TYPE_PERF_STOP_FAILED = "ERR_TYPE_PERF_STOP_FAILED";
    public static final String ERR_JSTAT_MONITORING_FAILED = "ERR_JSTAT_MONITORING_FAILED";
    public static final String ERR_JSTAT_STOP_FAILED = "ERR_JSTAT_STOP_FAILED";
    
    private static final long serialVersionUID = 8354659763253285983L;

    /**
     * Default constructor.
     */
    public MetricGatheringPluginException() {
    }

    /**
     * 
     * @param msg
     */
    public MetricGatheringPluginException(String msg) {
        super(msg);
    }

    /**
     * 
     * @param msg
     * @param errorCode
     */
    public MetricGatheringPluginException(String msg, String errorCode) {
        super(msg, errorCode);
    }

    /**
     * 
     * @param errorCode
     * @param pattern
     * @param arguments
     */
    public MetricGatheringPluginException(String errorCode, 
                                          String pattern, Object...arguments) {
        super(errorCode, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param errorCode
     * @param pattern
     * @param arguments
     */
    public MetricGatheringPluginException(Throwable ex, String errorCode, 
                                          String pattern, Object...arguments) {
        super(ex, errorCode, pattern, arguments);
    }
    
    /**
     * 
     * @param ex
     */
    public MetricGatheringPluginException(Throwable ex) {
        super(ex);
    }

    /**
     * 
     * @param ex
     * @param errorCode
     */
    public MetricGatheringPluginException(Throwable ex, String errorCode) {
        super(ex, errorCode);
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public MetricGatheringPluginException(String msg, Throwable ex) {
        super(msg, ex);
    }

    /**
     * 
     * @param msg
     * @param ex
     * @param errorCode
     */
    public MetricGatheringPluginException(String msg, Throwable ex, String errorCode) {
        super(msg, ex, errorCode);
    }

}
