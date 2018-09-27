package com.ca.apm.systemtest.fld.role;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getAbsolutePath;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getLinuxAbsolutePath;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getLinuxTimeSynchronizationWorkDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getTimeSynchronizationWorkDir;

import org.apache.http.util.Args;

import com.ca.apm.systemtest.fld.flow.TimeSynchronizationFlow;
import com.ca.apm.systemtest.fld.flow.TimeSynchronizationFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class TimeSynchronizationRole extends AbstractRole {

    private final boolean onWindows;

    private final String workDir;
    private final String javaHome;
    private final String runCp;

    private final long waitInterval;

    protected TimeSynchronizationRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.onWindows = builder.onWindows;
        this.workDir = builder.workDir;
        this.javaHome = builder.javaHome;
        this.runCp = builder.runCp;
        this.waitInterval = builder.waitInterval;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runTimeSynchronization(aaClient);
    }

    private void runTimeSynchronization(IAutomationAgentClient aaClient) {
        TimeSynchronizationFlowContext.Builder builder =
            onWindows
                ? new TimeSynchronizationFlowContext.Builder()
                : new TimeSynchronizationFlowContext.LinuxBuilder();
        builder = builder.workDir(workDir).javaHome(javaHome).runCp(runCp);
        if (waitInterval > 0) {
            builder = builder.waitInterval(waitInterval);
        }
        TimeSynchronizationFlowContext context = builder.build();
        runFlow(aaClient, TimeSynchronizationFlow.class, context);
    }

    public static class LinuxBuilder extends Builder {
        private static final String JAVA_HOME = "/usr";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            onWindows = false;
            workDir = getLinuxTimeSynchronizationWorkDir();
            javaHome = JAVA_HOME;
        }

        @Override
        public Builder javaHome(String javaHome) {
            Args.notBlank(javaHome, "javaHome");
            this.javaHome = getLinuxAbsolutePath(javaHome);
            return builder();
        }
    }

    public static class Builder extends BuilderBase<Builder, TimeSynchronizationRole> {
        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;

        protected boolean onWindows = true;

        protected String workDir;
        protected String javaHome;
        private String runCp;

        private long waitInterval;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            workDir = getTimeSynchronizationWorkDir();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected TimeSynchronizationRole getInstance() {
            return new TimeSynchronizationRole(this);
        }

        @Override
        public TimeSynchronizationRole build() {
            TimeSynchronizationRole role = getInstance();
            Args.notBlank(workDir, "workDir");
            Args.notBlank(javaHome, "javaHome");
            Args.notBlank(runCp, "runCp");
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

        public Builder runCp(String runCp) {
            Args.notBlank(runCp, "runCp");
            this.runCp = runCp;
            return builder();
        }

        public Builder waitInterval(long waitInterval) {
            Args.positive(waitInterval, "Wait interval between time synchronization attempts");
            this.waitInterval = waitInterval;
            return builder();
        }
    }

}
