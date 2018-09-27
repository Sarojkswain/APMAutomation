package com.ca.apm.systemtest.fld.plugin.windows.perfmon;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils.Sample;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author haiva01
 */
@PluginAnnotationComponent(pluginType = WindowsPerfmonPlugin.PLUGIN)
public interface WindowsPerfmonPlugin extends Plugin {
    public static final String PLUGIN = "windowsPerfmonPlugin";
    
    int monitor(Collection<String> metricsNames) throws Exception;

    void stopMonitoring(int handle) throws Exception;

    Map<String, Collection<Sample>> getSamples(int handle) throws Exception;

    List<List<String>> getRawSamples(int handle) throws Exception;

    List<String> getHeaders(int handle);

    void closeHandle(int handle) throws Exception;
}
