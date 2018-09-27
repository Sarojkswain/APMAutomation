/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.tests.role;

import com.ca.apm.tests.artifact.DotNetFxVersion;
import com.ca.apm.tests.flow.DotNetFxDeployFlow;
import com.ca.apm.tests.flow.DotNetFxFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Extension to WebSphere8Role allowing setting cell and node name during profile creation
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class DotNetFxRole extends AbstractRole {

    private final DotNetFxFlowContext flowContext;
    private final DotNetFxFlowContext undeployFlowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    protected DotNetFxRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        flowContext = builder.flowContext;
        undeployFlowContext = builder.undeployFlowContext;

        undeployOnly = builder.undeployOnly;
        this.predeployed = builder.predeployed;
    }

    public boolean isUndeployOnly() {
        return undeployOnly;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (undeployOnly) {
//            this.runFlow(aaClient, DotNetFxUndeployFlow.class, this.undeployFlowContext); // TODO
        } else if (!predeployed) {
            this.runFlow(aaClient, DotNetFxDeployFlow.class, this.flowContext);
        }
    }

    public static class Builder extends BuilderBase<DotNetFxRole.Builder, DotNetFxRole> {
        private static final DotNetFxVersion DEFAULT_ARTIFACT;

        private final String roleId;
        private final ITasResolver tasResolver;
        protected DotNetFxFlowContext.Builder flowContextBuilder;
        protected DotNetFxFlowContext flowContext;
        protected DotNetFxFlowContext.Builder undeployFlowContextBuilder;
        protected DotNetFxFlowContext undeployFlowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = DotNetFxVersion.VER_4_5;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            flowContextBuilder = new DotNetFxFlowContext.Builder();
            undeployFlowContextBuilder = new DotNetFxFlowContext.Builder();

            this.version(DEFAULT_ARTIFACT);
        }


        @Override
        protected DotNetFxRole getInstance() {
            return new DotNetFxRole(this);
        }

        @Override
        public DotNetFxRole build() {
            // deploy context
            this.flowContext = flowContextBuilder.build();
            // undeploy context
            undeployFlowContext = undeployFlowContextBuilder.build();

            return this.getInstance();
        }

        public Builder version(@NotNull DotNetFxVersion dotNetFxVersion) {
            URL artifactUrl = dotNetFxVersion.getArtifactUrl(this.tasResolver.getRegionalArtifactory(), dotNetFxVersion.getArtifact());
            String fileName = dotNetFxVersion.getFilename();
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.undeployFlowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContextBuilder.setupFileName(fileName);
            this.undeployFlowContextBuilder.setupFileName(fileName);
            return this.builder();
        }

        public Builder deploySourcesLocation(@NotNull String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            this.undeployFlowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public Builder undeployOnly(boolean undeployOnly) {
            this.undeployOnly = undeployOnly;
            return this.builder();
        }

        public Builder undeployOnly() {
            this.undeployOnly = true;
            return this.builder();
        }

        public Builder predeployed() {
            this.predeployed = true;
            return builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
