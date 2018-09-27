package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * Interface for Load Orchestrator's performance monitoring plugins.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 * 
 */
public interface MetricGatheringPlugin extends Plugin {
    /**
     * Runs performance monitoring. 
     * 
     * @param    config    performance monitoring configuration 
     * @return   pid of the monitored process (if applied, <code>-1</code> otherwise)   
     */
    Long runMonitoring(PerfMonitoringConfig config);

    /**
     * Finishes performance monitoring.
     *  
     */
    void stopMonitoring();
}
