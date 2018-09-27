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

import com.ca.apm.tests.artifact.Trade6Version;
import com.ca.apm.tests.flow.Trade6Flow;
import com.ca.apm.tests.flow.Trade6FlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class Trade6Role extends AbstractRole {

    private final Trade6FlowContext flowContext;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected Trade6Role(Trade6Role.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        this.predeployed = builder.predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        if (!predeployed) {
            this.runFlow(aaClient, Trade6Flow.class, this.flowContext);
        }
    }

    public Integer getPortNumber() {
        return 9080; // hardcoded WAS port TODO parametrize
    }

    public static class Builder extends BuilderBase<Trade6Role.Builder, Trade6Role> {

        private static final Trade6Version DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected Trade6Version version;
        protected Trade6FlowContext.Builder flowContextBuilder;
        protected Trade6FlowContext flowContext;
        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = Trade6Version.VER_6;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new Trade6FlowContext.Builder();
        }

        public Trade6Role build() {
            this.initFlow();
            Trade6Role role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected Trade6Role getInstance() {
            return new Trade6Role(this);
        }

        protected void initFlow() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
        }

        public Trade6Role.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public Trade6Role.Builder ojdbcRole(OjdbcRole ojdbcRole) {
            this.flowContextBuilder.ojdbcPath(ojdbcRole.getDeploySourcesLocation() + "/" + ojdbcRole.getJarName());
            return this.builder();
        }

        public Trade6Role.Builder websphereRole(WebSphere8Role websphereRole) {
            this.flowContextBuilder.websphereInstallPath(websphereRole.getInstallDir());
            this.flowContextBuilder.websphereProfileBinPath(websphereRole.getProfileBin());
            return this.builder();
        }

        public Trade6Role.Builder websphereRole(Websphere85Role websphereRole) {
            this.flowContextBuilder.websphereInstallPath(websphereRole.getInstallDir());
            this.flowContextBuilder.websphereProfileBinPath(websphereRole.getProfilePath() + "\\bin");
            return this.builder();
        }

        public Trade6Role.Builder dbRole(IRole dbRole) {
            this.flowContextBuilder.dbHostname(tasResolver.getHostnameById(dbRole.getRoleId()));
            return this.builder();
        }

        public Trade6Role.Builder version(Trade6Version version) {
            this.version = version;
            this.flowContextBuilder.applicationEarFileName(version.getApplicationEar());
            this.flowContextBuilder.installScriptFileName(version.getInstallScript());
            this.flowContextBuilder.resourcesScriptFileName(version.getResourcesScript());
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

        protected Trade6Role.Builder builder() {
            return this;
        }
    }


}