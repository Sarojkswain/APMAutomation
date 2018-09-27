package com.ca.apm.es;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsTraceComponentData {

    private int posId;
    private String traceId;
    private String resource;
    private long startTime;
    private int flags;
    private String description;
    private long duration;
    private int subNodeCount;
    private Map<String, String> parameters;

    public String getTraceId() {
        return traceId;
    }

    public int getPosId() {
        return posId;
    }

    public int getFlags() {
        return flags;
    }

    public String getResource() {
        return resource;
    }

    public String getDescription() {
        return description;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public int getSubNodeCount() {
        return subNodeCount;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setPosId(int posId) {
        this.posId = posId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSubNodeCount(int subNodeCount) {
        this.subNodeCount = subNodeCount;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
