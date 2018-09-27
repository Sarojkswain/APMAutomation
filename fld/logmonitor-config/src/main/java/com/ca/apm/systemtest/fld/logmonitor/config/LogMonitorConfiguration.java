/**
 *
 */

package com.ca.apm.systemtest.fld.logmonitor.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author KEYJA01
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogMonitorConfiguration {
    private Map<String, LogStream> logStreams;

    public Map<String, LogStream> getLogStreams() {
        return logStreams;
    }

    public void setLogStreams(Map<String, LogStream> logStreams) {
        this.logStreams = logStreams;
    }
}
