package com.ca.apm.es;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsTraceSummaryData {

    public static final int PARENT_MASK = 0xFFFF0000;
    public static final int CHILD_MASK = 0x0000FFFF;

    private String traceId;
    private String type;
    private String agent;
    private String callerTxnTraceId;
    private Set<String> corKeys;
    private String userId;
    private String description;
    private int flags;
    private int compCount;

    private String appName;
    private String resource;
    private long startTime;
    private long duration;

    public EsTraceSummaryData() {
    }

    public EsTraceSummaryData(String id) {

        this.traceId = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCallerTxnTraceId() {
        return callerTxnTraceId;
    }

    public void setCallerTxnTraceId(String callerTxnTraceId) {
        this.callerTxnTraceId = callerTxnTraceId;
    }

    public void setTraceId(String id) {
        this.traceId = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public void setCorKeys(Set<String> corKeys) {
        this.corKeys = corKeys;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setCompCount(int compCount) {
        this.compCount = compCount;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getType() {
        return type;
    }

    public String getAgent() {
        return agent;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public int getFlags() {
        return flags;
    }

    public Set<String> getCorKeys() {
        return corKeys;
    }

    public int getCompCount() {
        return compCount;
    }
}
