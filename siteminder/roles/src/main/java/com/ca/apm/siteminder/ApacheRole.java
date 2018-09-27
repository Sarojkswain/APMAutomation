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
import org.jetbrains.annotations.NotNull;

import java.net.URL;

import java.net.URL;

/**
 * @author surma04
 *
 */
public class ApacheRole extends AbstractRole {

    private static IFlowContext apacheFlowContext;

    /**
     * @param b
     */
    public ApacheRole(final Builder b) {
        super(b.roleId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.role.IRole#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(DeployApacheFlow.class,
                apacheFlowContext,
                getHostingMachine().getHostnameWithPort()));
    }

    public static class Builder extends BuilderBase<Builder, ApacheRole> {

        private String roleId;
        private ApacheVersion apacheVersion = ApacheVersion.v2225x32w;
        private ITasResolver tasResolver;
        private final ApacheFlowContext.Builder contextBuilder = new ApacheFlowContext.Builder();
        private String installDir;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        protected ApacheRole getInstance() {
            return new ApacheRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ca.tas.role.IBuilder#build()
         */
        @Override
        public ApacheRole build() {
            final URL artifactUrl = tasResolver.getArtifactUrl(apacheVersion.getArtifact());
            contextBuilder.artifactUrl(artifactUrl);
            contextBuilder.installDir(installDir);
            apacheFlowContext = contextBuilder.build();
            return getInstance();
        }

        public Builder version(@NotNull final ApacheVersion desiredVersion) {
            this.apacheVersion = desiredVersion;
            return this;
        }

        /**
         * @param apacheDir
         * @return
         */
        public Builder installDir(@NotNull final String apacheDir) {
            this.installDir = apacheDir;
            return this;
        }

    }
}
