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

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.transactiontrace.appmap.artifact.NoWhereBankApplicationArtifact;
import com.ca.apm.transactiontrace.appmap.flow.NowhereBankFlow;
import com.ca.apm.transactiontrace.appmap.flow.NowhereBankFlowContext;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.net.URL;

/**
 * Starts all applications in Nowhere bank
 *
 * @author bhusu01
 */
public class NoWhereBankRole extends AbstractRole {

    private static final String MESSAGING_SERVER_START = "INFO: Starting listener";
    private static final String BANKING_ENGINE_START = "Listening on queue test";
    private static final String BANKING_MEDIATOR_START = "HTTP Server open on port 10500";
    private static final String BANKING_PORTAL_START = "Server listening on socket port 10666";
    private static final int ASYNC_DELAY = 30;
    private static final String BANKING_UI_GEN_START = "Wrote message to Port 10666";
    private static final String MESSAGING_SERVER_EXEC = "01_MessagingServer.cmd";
    private static final String BANKING_ENGINE_EXEC = "02_Banking-Engine-wily.cmd";
    private static final String BANKING_MEDIATOR_EXEC = "03_Banking-Mediator-wily.cmd";
    private static final String BANKING_PORTAL_EXEC = "04_Banking-Portal-wily.cmd";
    private static final String BANKING_UI_GENERATOR = "05_Banking-UI-Generator.cmd";


    NowhereBankFlowContext flowContext;
    private final boolean start;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected NoWhereBankRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        this.start = builder.start;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(NowhereBankFlow.class, flowContext,
            getHostingMachine().getHostnameWithPort()));

        if (start) {
            RunCommandFlowContext messagingServer =
                new RunCommandFlowContext.Builder(MESSAGING_SERVER_EXEC)
                    .workDir(flowContext.getExtractionDirectory()).name(getRoleId())
                    .terminateOnMatch(MESSAGING_SERVER_START).build();

            RunCommandFlowContext bankingEngine =
                new RunCommandFlowContext.Builder(BANKING_ENGINE_EXEC)
                    .workDir(flowContext.getExtractionDirectory()).name(getRoleId())
                    .terminateOnMatch(BANKING_ENGINE_START).build();

            RunCommandFlowContext bankingMediator =
                new RunCommandFlowContext.Builder(BANKING_MEDIATOR_EXEC)
                    .workDir(flowContext.getExtractionDirectory()).name(getRoleId())
                    .terminateOnMatch(BANKING_MEDIATOR_START).build();

            RunCommandFlowContext bankingPortal =
                new RunCommandFlowContext.Builder(BANKING_PORTAL_EXEC)
                    .workDir(flowContext.getExtractionDirectory()).name(getRoleId())
                    .terminateOnMatch(BANKING_PORTAL_START).build();

            RunCommandFlowContext bankingUIGenerator =
                new RunCommandFlowContext.Builder(BANKING_UI_GENERATOR)
                    .workDir(flowContext.getExtractionDirectory()).name(getRoleId())
                    .terminateOnMatch(BANKING_UI_GEN_START).build();

            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class,
                messagingServer, getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY)
                .async());

            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class,
                bankingEngine, getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY)
                .async());

            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class,
                bankingMediator, getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY)
                .async());

            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class,
                bankingPortal, getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY)
                .async());

            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class,
                bankingUIGenerator, getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY)
                .async());
        }

    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link com.ca.apm.transactiontrace.appmap.role.NoWhereBankRole}
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
     * Builder responsible for holding all necessary properties to instantiate
     * {@link com.ca.apm.transactiontrace.appmap.role.NoWhereBankRole}
     */
    public static class Builder extends BuilderBase<Builder, NoWhereBankRole> {

        private final String roleId;
        @SuppressWarnings("unused")
        private final ITasResolver tasResolver;
        private final IThirdPartyArtifact noWhereBankArtifact;
        private NowhereBankFlowContext.Builder flowContextBuilder =
            new NowhereBankFlowContext.Builder();
        private boolean start = true;

        private NowhereBankFlowContext flowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.noWhereBankArtifact = getDefaultNoWhereBankVersion();
        }

        public Builder nostart() {
            this.start = false;
            return this;
        }

        private IThirdPartyArtifact getDefaultNoWhereBankVersion() {
            return NoWhereBankApplicationArtifact.v1_0_2;
        }

        @Override
        public NoWhereBankRole build() {
            URL artifactURL = tasResolver.getArtifactUrl(noWhereBankArtifact.getArtifact());
            flowContextBuilder.artifactURL(artifactURL);
            flowContext = flowContextBuilder.build();
            return getInstance();
        }

        @Override
        protected NoWhereBankRole getInstance() {
            return new NoWhereBankRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
