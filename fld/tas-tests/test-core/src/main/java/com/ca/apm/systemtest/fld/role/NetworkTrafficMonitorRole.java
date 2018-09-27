package com.ca.apm.systemtest.fld.role;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getLinuxNetworkTrafficMonitorWorkDir;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;

import com.ca.apm.systemtest.fld.flow.DeployNetworkTrafficMonitorFlow;
import com.ca.apm.systemtest.fld.flow.DeployNetworkTrafficMonitorFlowContext;
import com.ca.apm.systemtest.fld.flow.RunNetworkTrafficMonitorFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class NetworkTrafficMonitorRole extends AbstractRole {

    public final static String ENV_NETWORK_TRAFFIC_MONITOR_START = "startNetworkTrafficMonitor";
    public final static String ENV_NETWORK_TRAFFIC_MONITOR_STOP = "stopNetworkTrafficMonitor";

    private final String workDir;
    private final String tcpdumpFilter;
    private final long intervalDuration;

    protected NetworkTrafficMonitorRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.workDir = builder.workDir;
        this.tcpdumpFilter = builder.tcpdumpFilter;
        this.intervalDuration = builder.intervalDuration;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        DeployNetworkTrafficMonitorFlowContext context =
            (new DeployNetworkTrafficMonitorFlowContext.Builder()).workDir(workDir)
                .tcpdumpFilter(tcpdumpFilter).intervalDuration(intervalDuration).build();
        runFlow(aaClient, DeployNetworkTrafficMonitorFlow.class, context);
    }

    public static class Builder extends BuilderBase<Builder, NetworkTrafficMonitorRole> {
        private String roleId;
        private String networkTrafficMonitorWebappHost;
        private int networkTrafficMonitorWebappPort;
        private String networkTrafficMonitorWebappContextRoot;
        private int chartWidth;
        private int chartHeight;
        private long waitInterval;
        private String workDir = getLinuxNetworkTrafficMonitorWorkDir();
        private String tcpdumpFilter = "tcp port 5001 or tcp port 5443";
        private long intervalDuration = 5000L;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected NetworkTrafficMonitorRole getInstance() {
            NetworkTrafficMonitorRole role = new NetworkTrafficMonitorRole(this);
            return role;
        }

        public Builder networkTrafficMonitorWebappHost(String networkTrafficMonitorWebappHost) {
            Args.notBlank(networkTrafficMonitorWebappHost, "NetworkTrafficMonitor webapp host");
            this.networkTrafficMonitorWebappHost = networkTrafficMonitorWebappHost;
            return builder();
        }

        public Builder networkTrafficMonitorWebappPort(int networkTrafficMonitorWebappPort) {
            Args.positive(networkTrafficMonitorWebappPort, "NetworkTrafficMonitor webapp port");
            this.networkTrafficMonitorWebappPort = networkTrafficMonitorWebappPort;
            return builder();
        }

        public Builder networkTrafficMonitorWebappContextRoot(
            String networkTrafficMonitorWebappContextRoot) {
            Args.notBlank(networkTrafficMonitorWebappContextRoot,
                "NetworkTrafficMonitor webapp context root");
            this.networkTrafficMonitorWebappContextRoot = networkTrafficMonitorWebappContextRoot;
            return builder();
        }

        public Builder chartWidth(int chartWidth) {
            Args.positive(chartWidth, "Chart width");
            this.chartWidth = chartWidth;
            return builder();
        }

        public Builder chartHeight(int chartHeight) {
            Args.positive(chartHeight, "Chart height");
            this.chartHeight = chartHeight;
            return builder();
        }

        public Builder waitInterval(long waitInterval) {
            Args.positive(waitInterval, "Wait interval between generating graphs");
            this.waitInterval = waitInterval;
            return builder();
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

        @Override
        public NetworkTrafficMonitorRole build() {
            Args.notBlank(networkTrafficMonitorWebappHost, "networkTrafficMonitorWebappHost");

            RunNetworkTrafficMonitorFlowContext.Builder builder =
                RunNetworkTrafficMonitorFlowContext.getBuilder();
            String shutdownFile = getDeployBase() + "//network-traffic-monitor-shutdown";
            builder.shutdownFile(shutdownFile).networkTrafficMonitorWebappHost(
                networkTrafficMonitorWebappHost);
            if (networkTrafficMonitorWebappPort > 0) {
                builder = builder.networkTrafficMonitorWebappPort(networkTrafficMonitorWebappPort);
            }
            if (StringUtils.isNotBlank(networkTrafficMonitorWebappContextRoot)) {
                builder =
                    builder
                        .networkTrafficMonitorWebappContextRoot(networkTrafficMonitorWebappContextRoot);
            }
            if (chartWidth > 0) {
                builder = builder.chartWidth(chartWidth);
            }
            if (chartHeight > 0) {
                builder = builder.chartHeight(chartHeight);
            }
            if (waitInterval > 0) {
                builder = builder.waitInterval(waitInterval);
            }
            builder = builder.workDir(workDir);
            getEnvProperties().add(ENV_NETWORK_TRAFFIC_MONITOR_START, builder.build());

            builder =
                RunNetworkTrafficMonitorFlowContext.getBuilder().shutdownFile(shutdownFile)
                    .shutdown(true);
            getEnvProperties().add(ENV_NETWORK_TRAFFIC_MONITOR_STOP, builder.build());
            return getInstance();
        }
    }

}
