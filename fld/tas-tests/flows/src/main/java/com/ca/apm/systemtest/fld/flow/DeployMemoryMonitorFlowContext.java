package com.ca.apm.systemtest.fld.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class DeployMemoryMonitorFlowContext implements IFlowContext {

    private final boolean onWindows;

    private final String batchFileTitle;
    private final String logFile;

    private final String workDir;
    private final String javaHome;
    private final String runCp;

    private final String gcLogFile;
    private final String group;
    private final String roleName;
    private final String memoryMonitorWebappHost;
    private final int memoryMonitorWebappPort;
    private final String memoryMonitorWebappContextRoot;
    private final int chartWidth;
    private final int chartHeight;
    private final long waitInterval;
    private final int iterationCount;

    protected DeployMemoryMonitorFlowContext(Builder builder) {
        this.onWindows = builder.onWindows;
        this.batchFileTitle = builder.batchFileTitle;
        this.logFile = builder.logFile;
        this.workDir = builder.workDir;
        this.javaHome = builder.javaHome;
        this.runCp = builder.runCp;
        this.gcLogFile = builder.gcLogFile;
        this.group = builder.group;
        this.roleName = builder.roleName;
        this.memoryMonitorWebappHost = builder.memoryMonitorWebappHost;
        this.memoryMonitorWebappPort = builder.memoryMonitorWebappPort;
        this.memoryMonitorWebappContextRoot = builder.memoryMonitorWebappContextRoot;
        this.chartWidth = builder.chartWidth;
        this.chartHeight = builder.chartHeight;
        this.waitInterval = builder.waitInterval;
        this.iterationCount = builder.iterationCount;
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

    public String getGcLogFile() {
        return gcLogFile;
    }

    public String getGroup() {
        return group;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getMemoryMonitorWebappHost() {
        return memoryMonitorWebappHost;
    }

    public int getMemoryMonitorWebappPort() {
        return memoryMonitorWebappPort;
    }

    public String getMemoryMonitorWebappContextRoot() {
        return memoryMonitorWebappContextRoot;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public long getWaitInterval() {
        return waitInterval;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public String getRunCp() {
        return runCp;
    }

    public static class LinuxBuilder extends Builder {
        private static final String LOG_FILE_LINUX = "./memory-monitor-log.txt";

        public LinuxBuilder() {
            super();
            onWindows = false;
            logFile = LOG_FILE_LINUX;
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployMemoryMonitorFlowContext> {
        private static final String TITLE = "MemoryMonitor";
        private static final String LOG_FILE = ".\\memory-monitor-log.txt";

        protected boolean onWindows = true;

        private String batchFileTitle = TITLE;
        protected String logFile = LOG_FILE;

        private String workDir;
        private String javaHome;
        private String runCp;

        private String gcLogFile;
        private String group;
        private String roleName;
        private String memoryMonitorWebappHost;
        private int memoryMonitorWebappPort;
        private String memoryMonitorWebappContextRoot;;
        private int chartWidth;
        private int chartHeight;
        private long waitInterval;
        private int iterationCount;

        @Override
        public DeployMemoryMonitorFlowContext build() {
            DeployMemoryMonitorFlowContext ctx = getInstance();
            Args.notBlank(workDir, "workDir");
            Args.notBlank(javaHome, "javaHome");
            Args.notBlank(runCp, "runCp");
            Args.notBlank(gcLogFile, "gcLogFile");
            Args.notBlank(group, "group");
            Args.notBlank(roleName, "roleName");
            Args.notBlank(memoryMonitorWebappHost, "memoryMonitorWebappHost");
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeployMemoryMonitorFlowContext getInstance() {
            return new DeployMemoryMonitorFlowContext(this);
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

        public Builder gcLogFile(String gcLogFile) {
            Args.notBlank(gcLogFile, "GC Log file");
            this.gcLogFile = gcLogFile;
            return builder();
        }

        public Builder memoryMonitorGroup(String group) {
            Args.notBlank(group, "MemoryMonitor group");
            this.group = group;
            return builder();
        }

        public Builder memoryMonitorRoleName(String roleName) {
            Args.notBlank(roleName, "MemoryMonitor role name");
            this.roleName = roleName;
            return builder();
        }

        public Builder memoryMonitorWebappHost(String memoryMonitorWebappHost) {
            Args.notBlank(memoryMonitorWebappHost, "MemoryMonitor webapp server host");
            this.memoryMonitorWebappHost = memoryMonitorWebappHost;
            return builder();
        }

        public Builder memoryMonitorWebappPort(int memoryMonitorWebappPort) {
            Args.positive(memoryMonitorWebappPort, "MemoryMonitor webapp server port");
            this.memoryMonitorWebappPort = memoryMonitorWebappPort;
            return builder();
        }

        public Builder memoryMonitorWebappContextRoot(String memoryMonitorWebappContextRoot) {
            Args.notBlank(memoryMonitorWebappContextRoot, "MemoryMonitor webapp context root");
            this.memoryMonitorWebappContextRoot = memoryMonitorWebappContextRoot;
            return builder();
        }

        public Builder chartWidth(int chartWidth) {
            Args.positive(chartWidth, "GC chart width");
            this.chartWidth = chartWidth;
            return builder();
        }

        public Builder chartHeight(int chartHeight) {
            Args.positive(chartHeight, "GC chart height");
            this.chartHeight = chartHeight;
            return builder();
        }

        public Builder waitInterval(long waitInterval) {
            Args.positive(waitInterval, "Wait interval between generating heap graph");
            this.waitInterval = waitInterval;
            return builder();
        }

        public Builder iterationCount(int iterationCount) {
            this.iterationCount = iterationCount;
            return builder();
        }
    }

}
