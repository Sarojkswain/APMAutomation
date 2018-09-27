/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.role;

import java.util.Arrays;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

/**
 * Creates a serialized command for a future use in tests that initiates a transaction trace session
 * by running a CLW command
 *
 * @author Jan Zak (zakja01)
 */
public class DeferredInitiateTransactionTraceSessionRole extends AbstractRole {

    public static final String ENV_INITIATE_TT_SESSION_COMMAND = "initiateTTSessionCommand";

    private static final String TRANSACTION_TRACE_CLW_COMMAND =
        "trace transactions exceeding %d ms in agents matching \"%s\" for %d s";

    private static final String CLW_JAR_NAME = "CLWorkstation.jar";

    /**
     * @param builder Builder object containing all necessary data
     */
    protected DeferredInitiateTransactionTraceSessionRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // noop
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link DeferredInitiateTransactionTraceSessionRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId) {
            super(roleId);
        }

        @Override
        protected RunCommandFlowContext.Builder createClwCommandBuilder() {
            return new RunCommandFlowContext.Builder("java").args(Arrays.asList("-jar",
                getClwJarFile(), getClwCommand()));

        }

        @Override
        protected Builder builder() {
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


    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link DeferredInitiateTransactionTraceSessionRole}
     */
    public static class Builder
        extends BuilderBase<Builder, DeferredInitiateTransactionTraceSessionRole> {

        private final String roleId;

        protected RunCommandFlowContext clwCommand;

        protected int timeFilterInMillis = 10;
        protected int traceSessionTimeInSeconds = 300;
        protected String agentSpecifier = ".*|.*|.*";
        protected EmRole emRole;

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        public Builder timeFilterInMillis(int timeFilterInMillis) {
            this.timeFilterInMillis = timeFilterInMillis;
            return builder();
        }

        public Builder traceSessionTimeInSeconds(int traceSessionTimeInSeconds) {
            this.traceSessionTimeInSeconds = traceSessionTimeInSeconds;
            return builder();
        }

        public Builder agentSpecifier(String agentSpecifier) {
            this.agentSpecifier = agentSpecifier;
            return builder();
        }

        public Builder emRole(EmRole emRole) {
            this.emRole = emRole;
            return builder();
        }

        @Override
        public DeferredInitiateTransactionTraceSessionRole build() {
            initCommand();
            return getInstance();
        }

        protected void initCommand() {
            clwCommand =
                createClwCommandBuilder()
                    .workDir(
                        concatPaths(emRole.getDeployEmFlowContext().getInstallDir(), "jre", "bin"))
                    .name(roleId).build();
            getEnvProperties().add(ENV_INITIATE_TT_SESSION_COMMAND, clwCommand);
        }

        protected RunCommandFlowContext.Builder createClwCommandBuilder() {
            return new RunCommandFlowContext.Builder("java -jar " + getClwJarFile() + " "
                + getClwCommand());
        }

        protected String getClwJarFile() {
            return concatPaths(emRole.getDeployEmFlowContext().getInstallDir(), "lib", CLW_JAR_NAME);
        }

        protected String getClwCommand() {
            return String.format(TRANSACTION_TRACE_CLW_COMMAND, timeFilterInMillis, agentSpecifier,
                traceSessionTimeInSeconds);
        }

        @Override
        protected DeferredInitiateTransactionTraceSessionRole getInstance() {
            return new DeferredInitiateTransactionTraceSessionRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
