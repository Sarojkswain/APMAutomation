package com.ca.apm.systemtest.fld.plugin.powerpack.reporting;

import java.io.IOException;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public interface PowerPackReportPlugin extends Plugin {
    String PLUGIN = "powerPackReportPlugin";
    
    /**
     * Generates power pack performance test results.
     * 
     * @param  config
     * @throws IOException
     */
    void generateReport(ResultParseConfig config) throws IOException;
    
}
