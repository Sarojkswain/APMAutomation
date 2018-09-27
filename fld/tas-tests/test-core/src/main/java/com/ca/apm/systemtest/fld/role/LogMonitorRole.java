package com.ca.apm.systemtest.fld.role;

import java.util.Collection;
import java.util.Map;

import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlow;
import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlowContext;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Log monitoring role. This role is used by FLD to monitor EM log files for specific messages
 * based on sophisticated configuration file.
 *
 * @author haiva01
 */
public class LogMonitorRole extends AbstractRole {
    @TasEnvironmentPropertyKey
    public static final String DEPLOY_LOG_MONITOR_FLOW_CONTEXT_DATA
        = "deployLogMonitorFlowContextData";

    protected DeployLogMonitorFlowContext flowContext;


    protected LogMonitorRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
    }

    public DeployLogMonitorFlowContext getDeployLogMonitorFlowContext() {
        return flowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployLogMonitorFlow.class, flowContext);
    }

    public static class Builder extends BuilderBase<Builder, LogMonitorRole> {
        String roleId;
        ITasResolver tasResolver;

        private DeployLogMonitorFlowContext.Builder flowContextBuilder;
        private DeployLogMonitorFlowContext flowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this(roleId, tasResolver, new DeployLogMonitorFlowContext.Builder(tasResolver));
        }

        protected Builder(String roleId, ITasResolver tasResolver,
            DeployLogMonitorFlowContext.Builder flowContextBuilder) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.flowContextBuilder = flowContextBuilder;
        }

        /**
         * @param file JSON config file for Tailer tool as on-disk file
         */
        public Builder configFile(String file) {
            flowContextBuilder.configFile(file);
            return builder();
        }

        /**
         * @param resource JSON config file for Tailer tool as resource file
         */
        public Builder configFileFromResource(String resource) {
            flowContextBuilder.configFileFromResource(resource);
            return builder();
        }

        /**
         * @param vars variables values that will be used to fill in placeholders in JSON config
         *             file.
         */
        public Builder vars(Map<String, String> vars) {
            flowContextBuilder.vars(vars);
            return builder();
        }

        /**
         * @param pidFile PID file path
         */
        public Builder pidFile(String pidFile) {
            flowContextBuilder.pidFile(pidFile);
            return builder();
        }

        public Builder tailerVersion(String version) {
            flowContextBuilder.tailerVersion(version);
            return builder();
        }

        /**
         * Do not start monitoring in deploy phase.
         */
        public Builder nostart() {
            flowContextBuilder.nostart();
            return builder();
        }

        public Builder maxMatchesPerPeriod(int maxMatchesPerPeriod) {
            flowContextBuilder.maxMatchesPerPeriod(maxMatchesPerPeriod);
            return builder();
        }

        public Builder previousLines(int numberOfPreviousLines) {
            flowContextBuilder.previousLines(numberOfPreviousLines);
            return builder();
        }

        public Builder emails(Collection<String> emails) {
            flowContextBuilder.emails(emails);
            return builder();
        }

        public Builder email(String email) {
            flowContextBuilder.email(email);
            return builder();
        }

        @Override
        protected LogMonitorRole getInstance() {
            return new LogMonitorRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public LogMonitorRole build() {
            flowContext = flowContextBuilder.build();
            getEnvProperties().add(DEPLOY_LOG_MONITOR_FLOW_CONTEXT_DATA, flowContext);
            return getInstance();
        }
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver, new DeployLogMonitorFlowContext.LinuxBuilder(tasResolver));
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }
}