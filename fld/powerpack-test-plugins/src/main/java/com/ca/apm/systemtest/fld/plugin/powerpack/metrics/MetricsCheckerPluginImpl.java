package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

/**
 * Implementation of the Load Orchestrator plugin which checks existences of the specified files.
 * 
 * @author shadm01
 */
@PluginAnnotationComponent(pluginType = MetricsCheckerPlugin.PLUGIN)
public class MetricsCheckerPluginImpl extends AbstractPluginImpl implements MetricsCheckerPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsCheckerPluginImpl.class);

    @Override
    @ExposeMethod(description = "Check that metrics are present on platform")
    public void check(MetricsCheckerConfig config) throws FileNotFoundException {
        
        info("Checking monitoring files present started");

        for (String path : config.getPaths()) {
            File pathToCheck = new File(path);
            if (!pathToCheck.exists()) {
                error("Path {0} not found!", path);
                throw new FileNotFoundException("Path '" + path + "' not found!");
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
    
}
