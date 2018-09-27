package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import java.io.FileNotFoundException;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * 
 * @author shadm01
 * 
 */
public interface MetricsCheckerPlugin extends Plugin {
    public static String PLUGIN = "MetricsCheckerPluginImpl";

    /**
     * 
     * @param config
     * @throws FileNotFoundException
     */
    void check(MetricsCheckerConfig config) throws FileNotFoundException;
}
