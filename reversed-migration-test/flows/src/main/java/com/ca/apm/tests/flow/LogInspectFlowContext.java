package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;

import java.util.List;

public class LogInspectFlowContext implements IFlowContext {
    private final String logFile;
    private final List<String> acceptedRegexps;
    private final List<String> ignoredRegexps;

    public static LogInspectFlowContext create(String logFile, List<String> acceptedRegexps, List<String> ignoredRegexps) {
        return new LogInspectFlowContext(logFile, acceptedRegexps, ignoredRegexps);
    }

    protected LogInspectFlowContext(String logFile, List<String> acceptedRegexps, List<String> ignoredRegexps) {
        this.logFile = logFile;
        this.acceptedRegexps = acceptedRegexps;
        this.ignoredRegexps = ignoredRegexps;
    }

    public String getLogFile() {
        return logFile;
    }

    public List<String> getAcceptedRegexps() {
        return acceptedRegexps;
    }

    public List<String> getIgnoredRegexps() {
        return ignoredRegexps;
    }
}
