package com.ca.apm.systemtest.fld.testbed.util;

import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlowContext.LogMonitorConfigSource;
import com.ca.apm.systemtest.fld.testbed.regional.LogMonitorConfigurationSource;

/**
 * This is convenience implementation of {@link LogMonitorConfigurationSource}.
 *
 * @author haiva01
 */
public class LogMonitorConfigurationSourceImpl implements LogMonitorConfigurationSource {
    LogMonitorConfigSource sourceType;
    String path;
    String stream;

    public LogMonitorConfigurationSourceImpl(
        LogMonitorConfigSource sourceType, String path, String stream) {
        this.sourceType = sourceType;
        this.path = path;
        this.stream = stream;
    }

    /**
     * <p>This method returns source type of log monitor configuration (file or resource).</p>
     *
     * @return source type of log monitor configuration
     */
    @Override
    public LogMonitorConfigSource getSourceType() {
        return sourceType;
    }

    /**
     * <p>This method returns either path to disk file or path to resource.</p>
     *
     * @return path to configuration source
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * <p>This method returns stream name within log monitor configuration that is to be used by
     * the monitor.</p>
     *
     * @return log monitor configuration stream name
     */
    @Override
    public String getStreamName() {
        return stream;
    }
}
