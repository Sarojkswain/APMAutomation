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

import java.net.URL;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.apm.automation.action.flow.IBuilder;

/**
 * @author surma04
 *
 */
public class WebAgentRole extends AbstractRole {

    private final DeployWebAgentFlowContext webAgentFlowContext;

    /**
     * @param roleId
     */
    public WebAgentRole(Builder builder) {
        super(builder.roleId);
        webAgentFlowContext = builder.webAgentFlowContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.role.IRole#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(DeployWebAgentFlow.class, webAgentFlowContext,
                getHostingMachine().getHostnameWithPort()));
        client.runJavaFlow(new FlowConfigBuilder(ConfigureWebAgentFlow.class, webAgentFlowContext,
            getHostingMachine().getHostnameWithPort()));

    }

    public static class Builder extends BuilderBase<Builder, WebAgentRole> {

        public DeployWebAgentFlowContext webAgentFlowContext;
        private String roleId;
        private ITasResolver tasResolver;
        private WebAgentVersion webAgentVersion;
        private final DeployWebAgentFlowContext.Builder contextBuilder =
                new DeployWebAgentFlowContext.Builder();
        private String javaLocation;
        private boolean optionPackRequired = true;
        private String installDir;


        public Builder(final String roleId, final ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        public Builder version(@NotNull final WebAgentVersion version) {
            this.webAgentVersion = version;
            return this;
        }

        public Builder javaLocation(@NotNull final String java32jdkPath) {
            this.javaLocation = java32jdkPath;
            return this;
        }

        /**
         * @param isRequired specify whether to install the Web Agent Option Pack as well. By
         *        default it is installed.
         * @return this builder
         */
        public Builder optionPackRequired(final boolean isRequired) {
            this.optionPackRequired = isRequired;
            return this;
        }

        /** @param installDir where to install web agent */
        public Builder installDir(@NotNull String installDir) {
            this.installDir = installDir;
            return this;
        }

        @Override
        protected WebAgentRole getInstance() {
            return new WebAgentRole(this);
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
        public WebAgentRole build() {
            final URL artifactUrl = tasResolver.getArtifactUrl(webAgentVersion.getArtifact());
            contextBuilder.artifactUrl(artifactUrl);
            contextBuilder.javaLocation(this.javaLocation);
            contextBuilder.installLocation(installDir);
            contextBuilder.installerFileName(webAgentVersion.getInstallerFilename());
            contextBuilder.optionPackRequired(this.optionPackRequired);
            if (optionPackRequired) {
                // no need to set if the OP will not be installed
                final URL optionPackUrl =
                        tasResolver.getArtifactUrl(webAgentVersion.getOptionPack());
                contextBuilder.optionPackUrl(optionPackUrl);
                contextBuilder.optionPackFileName(webAgentVersion.getOptionPackInstallerFilename());
            }
            webAgentFlowContext = contextBuilder.build();
            return getInstance();
        }
    }
}
