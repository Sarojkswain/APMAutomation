package com.ca.apm.systemtest.fld.plugin.run;

import java.util.List;
import java.util.Map;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author tavpa01
 */
@PluginAnnotationComponent(pluginType = "runPlugin")
public interface RunPlugin extends Plugin {
    long STILL_RUNNING = Long.MAX_VALUE;
    long NO_SUCH_PROCESS = Long.MIN_VALUE;
    int OK = 0;

    String runProcess(String commandName, Map<String, Object> params);

    void stopProcess(String procId);

    long exitValue(String procId);

    String getLog(String procId, int sizeKiB);

    List<String> listRunningProcesses();

    void stopAllProcesses();
}
