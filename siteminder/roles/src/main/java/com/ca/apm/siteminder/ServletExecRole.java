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
import com.ca.tas.role.AbstractRole;
import com.ca.apm.automation.action.flow.IBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author surma04
 *
 */
public class ServletExecRole extends AbstractRole {

    private static IFlowContext flowContext;

    /**
     * @param builder
     */
    public ServletExecRole(Builder builder) {
        super(builder.roleId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.tas.role.IRole#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(DeployServletExecFlow.class,
            flowContext, getHostingMachine().getHostnameWithPort()));
    }

    public static class Builder extends BuilderBase<Builder, ServletExecRole> {

        private String roleId;
        private ServletExecVersion version;
        private final ServletExecFlowContext.Builder ctxBuilder =
                new ServletExecFlowContext.Builder();
        private String javaPath;
        private String apacheDir;
        private String webAgentDir;

        public Builder(final String roleId) {
            this.roleId = roleId;
        }

        /**
         * @param seVersion
         * @return
         */
        public Builder version(@NotNull final ServletExecVersion seVersion) {
            this.version = seVersion;
            return this;
        }

        public Builder javaPath(@NotNull final String pathToJRE) {
            this.javaPath = pathToJRE;
            return this;
        }

        public Builder apacheDir(@NotNull final String apacheLocation) {
            this.apacheDir = apacheLocation;
            return this;
        }

        public Builder webAgentDir(@NotNull final String webAgentLocation) {
            this.webAgentDir = webAgentLocation;
            return this;
        }

        @Override
        protected ServletExecRole getInstance() {
            return new ServletExecRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public ServletExecRole build() {
            ctxBuilder.installer(version.getFilename());
            ctxBuilder.javaPath(this.javaPath);
            ctxBuilder.installerDir(version.getFolderName());
            ctxBuilder.apacheDir(this.apacheDir);
            ctxBuilder.webAgentDir(this.webAgentDir);

            flowContext = ctxBuilder.build();
            return getInstance();
        }
    }
}
