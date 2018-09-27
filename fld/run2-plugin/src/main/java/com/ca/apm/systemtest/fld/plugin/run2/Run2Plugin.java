package com.ca.apm.systemtest.fld.plugin.run2;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author haiva01
 */
@PluginAnnotationComponent(pluginType = "run2Plugin")
public interface Run2Plugin extends Plugin {
    long STILL_RUNNING = Long.MAX_VALUE;

    Integer runProcess(List<String> commandLine,
        Map<String, String> environmentChanges) throws IOException;

    Integer runProcess2(List<String> commandLine, String logFileName,
        String workingDirectoryName, Map<String, String> environmentChanges) throws IOException;

    void stopProcess(Integer procId);

    long exitValue(Integer procId);

    void closeHandle(Integer procId);

    List<Integer> listRunningProcesses();

    void stopAllProcesses();
}
