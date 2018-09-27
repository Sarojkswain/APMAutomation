package com.ca.apm.systemtest.fld.plugin.memorymonitor;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

@PluginAnnotationComponent(pluginType = "memoryMonitorPlugin")
public interface MemoryMonitorPlugin extends Plugin {
    void createChart(String gcLogFile, String roleName, String orchestratorHost);
}
