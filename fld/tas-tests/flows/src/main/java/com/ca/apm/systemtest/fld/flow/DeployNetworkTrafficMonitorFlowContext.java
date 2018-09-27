package com.ca.apm.systemtest.fld.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class DeployNetworkTrafficMonitorFlowContext implements IFlowContext {

    private final String workDir;
    private final String tcpdumpFilter;
    private final long intervalDuration;

    protected DeployNetworkTrafficMonitorFlowContext(Builder builder) {
        this.workDir = builder.workDir;
        this.tcpdumpFilter = builder.tcpdumpFilter;
        this.intervalDuration = builder.intervalDuration;
    }

    public String getWorkDir() {
        return workDir;
    }

    public String getTcpdumpFilter() {
        return tcpdumpFilter;
    }

    public long getIntervalDuration() {
        return intervalDuration;
    }

    public static class Builder
        extends BuilderBase<Builder, DeployNetworkTrafficMonitorFlowContext> {
        private String workDir;
        private String tcpdumpFilter;
        private long intervalDuration;

        @Override
        public DeployNetworkTrafficMonitorFlowContext build() {
            DeployNetworkTrafficMonitorFlowContext ctx = getInstance();
            Args.notBlank(workDir, "workDir");
            Args.notBlank(tcpdumpFilter, "tcpdumpFilter");
            Args.positive(intervalDuration, "intervalDuration");
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeployNetworkTrafficMonitorFlowContext getInstance() {
            return new DeployNetworkTrafficMonitorFlowContext(this);
        }

        public Builder workDir(String workDir) {
            Args.notBlank(workDir, "workDir");
            this.workDir = workDir;
            return builder();
        }

        public Builder tcpdumpFilter(String tcpdumpFilter) {
            Args.notBlank(tcpdumpFilter, "tcpdumpFilter");
            this.tcpdumpFilter = tcpdumpFilter;
            return builder();
        }

        public Builder intervalDuration(long intervalDuration) {
            Args.positive(intervalDuration, "intervalDuration");
            this.intervalDuration = intervalDuration;
            return builder();
        }
    }

}
