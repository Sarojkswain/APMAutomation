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

import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.tests.flow.TomcatDeployFlow;
import com.ca.apm.tests.flow.TomcatDeployFlowContext;
import com.ca.apm.tests.flow.TomcatUndeployFlow;
import com.ca.apm.tests.flow.TomcatUndeployFlowContext;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import org.jetbrains.annotations.NotNull;

/**
 * Extension to WebSphere8Role allowing setting cell and node name during profile creation
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class PerfTomcatRole extends TomcatRole {

    public static final String UNDEPLOY_TOMCAT = "undeployTomcat";


    private final TomcatUndeployFlowContext undeployFlowContext;
    @NotNull
    private final TomcatDeployFlowContext tomcatFlowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    protected PerfTomcatRole(Builder builder) {
        super(builder);

        undeployFlowContext = builder.undeployFlowContext;
        this.tomcatFlowContext = builder.tomcatFlowContext;
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
            this.runFlow(aaClient, TomcatUndeployFlow.class, this.undeployFlowContext);
        } else if (!predeployed) {
            this.deployTomcat(aaClient);
            this.fileModifierFlow(aaClient);
        }
    }

    protected void deployTomcat(IAutomationAgentClient aaClient) {
        this.runFlow(aaClient, TomcatDeployFlow.class, this.tomcatFlowContext);
    }

    public static class Builder extends TomcatRole.Builder {

        protected TomcatUndeployFlowContext.Builder undeployFlowContextBuilder;
        protected TomcatUndeployFlowContext undeployFlowContext;

        protected DeployTomcatFlowContext originalTomcatFlowContext;

        protected TomcatDeployFlowContext.Builder tomcatFlowContextBuilder;
        protected TomcatDeployFlowContext tomcatFlowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            tomcatFlowContextBuilder = new TomcatDeployFlowContext.Builder();
            undeployFlowContextBuilder = new TomcatUndeployFlowContext.Builder();
        }


        @Override
        protected PerfTomcatRole getInstance() {
            return new PerfTomcatRole(this);
        }

        @Override
        public PerfTomcatRole build() {
            super.build();

            undeployFlowContext = undeployFlowContextBuilder.installLocation(this.installDir).build();
            originalTomcatFlowContext = tomcatFlowCtxBuilder.installDir(this.installDir).build();
            tomcatFlowContext = tomcatFlowContextBuilder.originalContext(originalTomcatFlowContext).build();
//            getEnvProperties().add(UNDEPLOY_WEBSPHERE8, undeployFlowContext);

            return this.getInstance();
        }

        public Builder tomcatVersion(TomcatVersion tomcatVersion, String unpackDir) {
            super.tomcatVersion(tomcatVersion);
            tomcatFlowContextBuilder.unpackDir(unpackDir);
            return this.builder();
        }

        @Override
        public Builder installDir(String installDir) {
            super.installDir(installDir);
            return this.builder();
        }

        @Override
        public Builder customJava(JavaRole customJava) {
            super.customJava(customJava);
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

        protected Builder builder() {
            return this;
        }
    }
}
