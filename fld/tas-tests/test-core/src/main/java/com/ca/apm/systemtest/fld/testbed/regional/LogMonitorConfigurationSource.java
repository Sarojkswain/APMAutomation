package com.ca.apm.systemtest.fld.testbed.regional;

import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlowContext.LogMonitorConfigSource;

/**
 * Instances of this interface are returned by
 * {@link FLDConfiguration#getMomLogMonitorConfiguration()},
 * {@link FLDConfiguration#getCollectorLogMonitorConfiguration(String)}  and by
 * {@link FLDConfiguration#getWebViewLogMonitorConfiguration()}.
 */
public interface LogMonitorConfigurationSource {
    /**
     * <p>This method returns source type of log monitor configuration (file or resource).</p>
     *
     * @return source type of log monitor configuration
     */
    LogMonitorConfigSource getSourceType();

    /**
     * <p>This method returns either path to disk file or path to resource.</p>
     *
     * @return path to configuration source
     */
    String getPath();

    /**
     * <p>This method returns stream name within log monitor configuration that is to be used by the monitor.</p>
     *
     * @return log monitor configuration stream name
     */
    String getStreamName();
}