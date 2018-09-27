package com.ca.apm.systemtest.fld.plugin.appdetect;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author jirji01
 */
public interface DetectionPlugin extends Plugin {
    static final String PLUGIN = "appsrvDetection";
    
    void runDetection();
}
