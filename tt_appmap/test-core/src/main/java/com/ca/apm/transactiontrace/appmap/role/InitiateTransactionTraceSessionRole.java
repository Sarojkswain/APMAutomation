/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.role;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.transactiontrace.appmap.flow.InitiateTransactionTraceSessionFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Initiates a transaction trace session by running a CLW command
 *
 * @author bhusu01
 */
public class InitiateTransactionTraceSessionRole extends AbstractRole {


    InitiateTransactionTraceSessionFlowContext flowContext;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected InitiateTransactionTraceSessionRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        String clwCommand =
            "java -jar " + flowContext.getApmLibDir() + flowContext.getClwJarFile() + " "
                + flowContext.getClwCommand();
        RunCommandFlowContext startTraceSession =
            new RunCommandFlowContext.Builder(clwCommand).workDir(flowContext.getJreBinDir()).name(getRoleId()).terminateOnMatch("").build();

        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, startTraceSession, getHostingMachine().getHostnameWithPort()).async());
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link InitiateTransactionTraceSessionRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
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
     * Builder responsible for holding all necessary properties to instantiate {@link InitiateTransactionTraceSessionRole}
     */
    public static class Builder extends BuilderBase<Builder, InitiateTransactionTraceSessionRole> {

        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        public InitiateTransactionTraceSessionFlowContext flowContext;
        private InitiateTransactionTraceSessionFlowContext.Builder contextBuilder =
            new InitiateTransactionTraceSessionFlowContext.Builder();

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            flowContext = contextBuilder.build();
        }

        @Override
        public InitiateTransactionTraceSessionRole build() {

            return getInstance();
        }

        @Override
        protected InitiateTransactionTraceSessionRole getInstance() {
            return new InitiateTransactionTraceSessionRole(this);
        }

        public Builder timeFilterInMillis(int timeFilter) {
            contextBuilder.timeFilterInMillis(timeFilter);
            return this;
        }

        public Builder traceSessionTime(int sessionTimeInSeconds) {
            contextBuilder.traceSessionTime(sessionTimeInSeconds);
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
