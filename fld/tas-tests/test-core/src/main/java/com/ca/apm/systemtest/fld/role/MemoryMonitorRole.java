package com.ca.apm.systemtest.fld.role;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getAbsolutePath;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getMemoryMonitorWorkDir;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class MemoryMonitorRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMonitorRole.class);

    public final static String ENV_MEMORY_MONITOR_START = "startMemoryMonitor";
    public final static String ENV_MEMORY_MONITOR_STOP = "stopMemoryMonitor";


    protected MemoryMonitorRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
    }

    
    public static class Builder extends BuilderBase<Builder, MemoryMonitorRole> {
        protected static final String MEMORY_MONITOR_RUNNER = "MemoryMonitorRunner";

        protected boolean onWindows = true;

        protected String workDir;
        protected String javaHome;

        private String roleId;
        private String gcLogFile;
        private String group = "";
        private String roleName;
        private String memoryMonitorWebappHost;
        private int memoryMonitorWebappPort;
        private String memoryMonitorWebappContextRoot;;
        private int chartWidth;
        private int chartHeight;
        private long waitInterval;
        private boolean isLinux = false;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            workDir = getMemoryMonitorWorkDir();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MemoryMonitorRole getInstance() {
            MemoryMonitorRole role = new MemoryMonitorRole(this);
            return role;
        }

        public Builder workDir(String workDir) {
            Args.notBlank(workDir, "workDir");
            this.workDir = workDir;
            return builder();
        }

        public Builder javaHome(String javaHome) {
            Args.notBlank(javaHome, "javaHome");
            this.javaHome = getAbsolutePath(javaHome);
            return builder();
        }
        
        public Builder isLinuxMachine(Boolean isLinux) {
            this.isLinux = isLinux;
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
            Args.notBlank(memoryMonitorWebappHost, "MemoryMonitor webapp host");
            this.memoryMonitorWebappHost = memoryMonitorWebappHost;
            return builder();
        }

        public Builder memoryMonitorWebappPort(int memoryMonitorWebappPort) {
            Args.positive(memoryMonitorWebappPort, "MemoryMonitor webapp port");
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

        @Override
        public MemoryMonitorRole build() {
            Args.notBlank(gcLogFile, "gcLogFile");
            Args.notBlank(roleName, "roleName");
            Args.notBlank(group, "group");
            Args.notBlank(memoryMonitorWebappHost, "memoryMonitorWebappHost");
            
            RunMemoryMonitorFlowContext.Builder builder = RunMemoryMonitorFlowContext.getBuilder();
            String shutdownFile = "";
            if (isLinux) {
                shutdownFile += getLinuxDeployBase();
            } else {
                shutdownFile += getDeployBase();
            }
            shutdownFile += "//" + group + "-" + roleName + "-shutdown";
            
            builder.gcLogFile(gcLogFile)
                .shutdownFile(shutdownFile)
                .roleName(roleName)
                .group(group)
                .memoryMonitorWebappHost(memoryMonitorWebappHost);
            if (memoryMonitorWebappPort > 0) {
                builder = builder.memoryMonitorWebappPort(memoryMonitorWebappPort);
            }
            if (StringUtils.isNotBlank(memoryMonitorWebappContextRoot)) {
                builder = builder.memoryMonitorWebappContextRoot(memoryMonitorWebappContextRoot);
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
            getEnvProperties().add(ENV_MEMORY_MONITOR_START, builder.build());
            
            builder = RunMemoryMonitorFlowContext.getBuilder()
                .shutdownFile(shutdownFile)
                .shutdown(true);
            getEnvProperties().add(ENV_MEMORY_MONITOR_STOP, builder.build());
            
            
            return getInstance();
        }
    }
}
