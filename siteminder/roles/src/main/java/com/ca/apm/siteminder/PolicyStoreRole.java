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
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.net.URL;


public class PolicyStoreRole extends AbstractRole {

    private static IFlowContext policyStoreFlowContext;
    private static PolicyStoreVersion policyStoreVersion = PolicyStoreVersion.v125x86w;

    private PolicyStoreRole(Builder builder) {
        super(builder.roleId);
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(DeployPolicyStoreFlow.class,
                policyStoreFlowContext,
                getHostingMachine().getHostnameWithPort()));
    }

    public static class Builder extends BuilderBase<Builder, PolicyStoreRole> {

        public String roleId;
        private ITasResolver tasResolver;
        private final DeployPolicyStoreFlowContext.Builder contextBuilder =
                new DeployPolicyStoreFlowContext.Builder();


        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected PolicyStoreRole getInstance() {
            return new PolicyStoreRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public PolicyStoreRole build() {
            URL artifactUrl = tasResolver.getArtifactUrl(policyStoreVersion.getArtifact());
            contextBuilder.storeArtifactUrl(artifactUrl);
            policyStoreFlowContext = contextBuilder.build();
            return getInstance();
        }

    }
}
