package com.ca.apm.systemtest.fld.plugin.powerpack.logcollectors;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * FLD LoadOrchestrator Plugin interface for log file collectors.  
 * 
 * @author shadm01
 */
public interface PerformanceTestResultsCollectorPlugin extends Plugin {
    
    /**
     * Collects (copies or moves from one place to another) test result log files.
     * 
     * @param config  config
     */
    void collect(PerfTestResultCollectionConfig config);
    
}
