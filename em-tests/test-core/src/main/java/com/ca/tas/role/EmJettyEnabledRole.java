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
package com.ca.tas.role;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.appmap.EmEnablePublicRestApiContext;
import com.ca.apm.automation.action.flow.appmap.EmEnablePublicRestApiFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author surma04
 *
 */
public class EmJettyEnabledRole extends AbstractRole {

    private static EmEnablePublicRestApiContext flowContext;

    private RunCommandFlowContext emRunCommandFlowContext;
    private RunCommandFlowContext wvRunCommandFlowContext;

    private static final int ASYNC_DELAY = 90;

    /**
     * @param roleId
     */
    public EmJettyEnabledRole(Builder builder) {
        super(builder.roleID);
        emRunCommandFlowContext = builder.getEmCtx();
        wvRunCommandFlowContext = builder.getWvCtx();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(EmEnablePublicRestApiFlow.class, flowContext,
            getHostingMachine().getHostnameWithPort()));

        startEm(client);
        startWv(client);
    }

    private void startEm(IAutomationAgentClient client) {
        FlowConfigBuilder timeout =
            new FlowConfigBuilder(RunCommandFlow.class, emRunCommandFlowContext, getHostWithPort())
                .delay(ASYNC_DELAY).async();

        client.runJavaFlow(timeout);
    }

    private void startWv(IAutomationAgentClient client) {
        runCommandFlowAsync(client, wvRunCommandFlowContext, 300);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.envproperty.IEnvironmentFriendly#addProperty(java.lang.String,
     * java.lang.String)
     */

    public static class Builder extends BuilderBase<Builder, EmJettyEnabledRole> {

        private String roleID;
        private ITasResolver tasResolver;
        private EmEnablePublicRestApiContext.Builder contextBuilder;
        private String apmRootDir;

        private RunCommandFlowContext emCtx;

        public RunCommandFlowContext getEmCtx() {
            return emCtx;
        }

        public RunCommandFlowContext getWvCtx() {
            return wvCtx;
        }

        private RunCommandFlowContext wvCtx;

        public Builder(String roleID, ITasResolver tasResolver) {
            contextBuilder = new EmEnablePublicRestApiContext.Builder();
            this.roleID = roleID;
            this.tasResolver = tasResolver;
        }

        public Builder apmRootDir(String apmRootDir) {
            this.apmRootDir = apmRootDir;
            return this;
        }

        public Builder runEmContext(RunCommandFlowContext emCtx) {
            this.emCtx = emCtx;
            return this;
        }

        public Builder runWvContext(RunCommandFlowContext emCtx) {
            this.wvCtx = emCtx;
            return this;
        }

        @Override
        protected EmJettyEnabledRole getInstance() {
            return new EmJettyEnabledRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public EmJettyEnabledRole build() {

            contextBuilder = new EmEnablePublicRestApiContext.Builder();
            final String hostname = tasResolver.getHostnameById(roleID);
            flowContext = contextBuilder.hostname(hostname).apmRootDir(apmRootDir).build();
            return getInstance();
        }
    }

}
