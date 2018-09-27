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
package com.ca.apm.siteminder;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.apm.automation.action.flow.IBuilder;
import org.jetbrains.annotations.NotNull;

public class PolicyServerConfigurationRole extends AbstractRole {

    private static IFlowContext flowContext;

    private PolicyServerConfigurationRole(Builder builder) {
        super(builder.roleId);
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(ConfigurePolicyStoreFlow.class,
                flowContext,
                getHostingMachine().getHostnameWithPort()));
    }

    public static class Builder extends BuilderBase<Builder, PolicyServerConfigurationRole> {

        public String roleId;
        private ITasResolver tasResolver;
        private final ConfigurePolicyStoreFlowContext.Builder contextBuilder =
                new ConfigurePolicyStoreFlowContext.Builder();
        private String pathToJre;


        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected PolicyServerConfigurationRole getInstance() {
            return new PolicyServerConfigurationRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public PolicyServerConfigurationRole build() {
            contextBuilder.jrePath(this.pathToJre);
            flowContext = contextBuilder.build();
            return getInstance();
        }

        public Builder jrePath(@NotNull final String pathToJre) {
            this.pathToJre = pathToJre;
            return this;
        }
    }
}
