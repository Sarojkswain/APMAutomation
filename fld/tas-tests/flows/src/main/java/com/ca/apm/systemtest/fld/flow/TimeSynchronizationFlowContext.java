package com.ca.apm.systemtest.fld.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class TimeSynchronizationFlowContext implements IFlowContext {

    private final boolean onWindows;

    private final String batchFileTitle;
    private final String logFile;

    private final String workDir;
    private final String javaHome;
    private final String runCp;

    private final long waitInterval;

    protected TimeSynchronizationFlowContext(Builder builder) {
        this.onWindows = builder.onWindows;
        this.batchFileTitle = builder.batchFileTitle;
        this.logFile = builder.logFile;
        this.workDir = builder.workDir;
        this.javaHome = builder.javaHome;
        this.runCp = builder.runCp;
        this.waitInterval = builder.waitInterval;
    }

    public boolean isOnWindows() {
        return onWindows;
    }

    public String getBatchFileTitle() {
        return batchFileTitle;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getWorkDir() {
        return workDir;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public long getWaitInterval() {
        return waitInterval;
    }

    public String getRunCp() {
        return runCp;
    }

    public static class LinuxBuilder extends Builder {
        private static final String LOG_FILE_LINUX = "./logs/console.txt";

        public LinuxBuilder() {
            super();
            onWindows = false;
            logFile = LOG_FILE_LINUX;
        }
    }

    public static class Builder extends BuilderBase<Builder, TimeSynchronizationFlowContext> {
        private static final String TITLE = "Time synchronization";
        private static final String LOG_FILE = ".\\logs\\console.txt";

        protected boolean onWindows = true;

        private String batchFileTitle = TITLE;
        protected String logFile = LOG_FILE;

        private String workDir;
        private String javaHome;
        private String runCp;

        private long waitInterval;

        @Override
        public TimeSynchronizationFlowContext build() {
            TimeSynchronizationFlowContext ctx = getInstance();
            Args.notBlank(workDir, "workDir");
            Args.notBlank(javaHome, "javaHome");
            Args.notBlank(runCp, "runCp");
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected TimeSynchronizationFlowContext getInstance() {
            return new TimeSynchronizationFlowContext(this);
        }

        public Builder workDir(String workDir) {
            Args.notBlank(workDir, "workDir");
            this.workDir = workDir;
            return builder();
        }

        public Builder javaHome(String javaHome) {
            Args.notBlank(javaHome, "javaHome");
            this.javaHome = javaHome;
            return builder();
        }

        public Builder runCp(String runCp) {
            Args.notBlank(runCp, "runCp");
            this.runCp = runCp;
            return builder();
        }

        public Builder waitInterval(long waitInterval) {
            Args.positive(waitInterval, "Wait interval between time synchronization");
            this.waitInterval = waitInterval;
            return builder();
        }
    }

}
