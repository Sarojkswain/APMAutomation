package com.ca.apm.systemtest.fld.plugin.logmonitor;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;

@PluginAnnotationComponent(pluginType = MonitoringPlugin.PLUGIN)
public interface MonitoringPlugin extends Plugin {
    public static final String PLUGIN = "logMonitorPlugin";

	void enableMonitor(String streamId);
	
	void disableMonitor(String streamId);
	
	LogStream getStreamConfig(String streamId);
	
	void setStreamConfig(String streamId, LogStream streamConfig);
}
