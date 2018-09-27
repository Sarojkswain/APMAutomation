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

import com.ca.apm.tests.artifact.NerdDinnerVersion;
import com.ca.apm.tests.flow.NerdDinnerDeployFlow;
import com.ca.apm.tests.flow.NerdDinnerFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class NerdDinnerRole extends AbstractRole {

    private final NerdDinnerFlowContext flowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected NerdDinnerRole(NerdDinnerRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;

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
            // this.runFlow(aaClient, UndeployNerdDinnerFlow.class, this.flowContext); TODO create
        } else if (!predeployed) {
            this.runFlow(aaClient, NerdDinnerDeployFlow.class, this.flowContext);
        }
    }

    public Integer getPortNumber() {
        return flowContext.getAppPort();
    }

    public static class Builder extends BuilderBase<NerdDinnerRole.Builder, NerdDinnerRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected NerdDinnerVersion version;
        protected NerdDinnerFlowContext.Builder flowContextBuilder;
        protected NerdDinnerFlowContext flowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new NerdDinnerFlowContext.Builder();
        }

        public NerdDinnerRole build() {
            this.initFlow();
            NerdDinnerRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected NerdDinnerRole getInstance() {
            return new NerdDinnerRole(this);
        }

        protected void initFlow() {
            assert this.version != null;

            URL artifactUrl = tasResolver.getArtifactUrl(this.version.createArtifact());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public Builder dbRole(MsSqlDbRole dbRole) {
            this.flowContextBuilder.dbHostname(tasResolver.getHostnameById(dbRole.getRoleId()));
//            this.flowContextBuilder.dbAdminUserName("sa"/*dbRole.getAdminUserName()*/);
            this.flowContextBuilder.dbAdminUserPassword(dbRole.getAdminUserPassword());
            return this.builder();
        }

        public Builder version(NerdDinnerVersion version) {
            this.version = version;
            return this.builder();
        }

        public Builder version(String version, NerdDinnerVersion.MvcVersion mvcVersion) {
            this.version = new NerdDinnerVersion(tasResolver, mvcVersion, version);
            return this.builder();
        }

        public Builder undeployOnly(boolean undeployOnly) {
            this.undeployOnly = undeployOnly;
            return this.builder();
        }

        public Builder appName(String appName) {
            this.flowContextBuilder.appName(appName);
            return this.builder();
        }

        public Builder appPort(Integer appPort) {
            this.flowContextBuilder.appPort(appPort);
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