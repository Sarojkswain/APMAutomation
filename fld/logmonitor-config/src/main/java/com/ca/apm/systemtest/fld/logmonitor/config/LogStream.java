package com.ca.apm.systemtest.fld.logmonitor.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogStream {
    private boolean enabled = false;
    private String logStreamId;
    private String fileName;
    private List<Rule> rules;
    private int concatLines = 100;
    private int waitForLines = 1000 * 10; // 10 seconds

    public LogStream() {
    }

    public LogStream(String logStreamId, String fileName, List<Rule> rules) {
        this.logStreamId = logStreamId;
        this.fileName = fileName;
        this.rules = rules;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public int getConcatLines() {
        return concatLines;
    }

    public void setConcatLines(int concatLines) {
        this.concatLines = concatLines;
    }

    public int getWaitForLines() {
        return waitForLines;
    }

    public void setWaitForLines(int waitForLines) {
        this.waitForLines = waitForLines;
    }

    public String getLogStreamId() {
        return logStreamId;
    }

    public void setLogStreamId(String logStreamId) {
        this.logStreamId = logStreamId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
