package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * 
 * Configuration for file checking plugin.
 * Provides a number of files which existence needs to be checked.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class MetricsCheckerConfig {
    
    private Collection<String> paths = new LinkedList<String>();
   
    public MetricsCheckerConfig() {
    }

    public MetricsCheckerConfig(Collection<String> paths) {
        this.paths = paths;
    }
    
    public MetricsCheckerConfig(String...paths) {
        for (String path : paths) {
            addPath(path);
        }
    }
    
    public void addPath(String path) {
        paths.add(path);
    }
    
    public Collection<String> getPaths() {
        return Collections.unmodifiableCollection(paths);
    }
    
}
