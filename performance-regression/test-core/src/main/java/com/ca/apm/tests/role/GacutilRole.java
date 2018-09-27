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

import com.ca.apm.tests.artifact.GacutilVersion;
import com.ca.apm.tests.flow.GacutilFlow;
import com.ca.apm.tests.flow.GacutilFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class GacutilRole extends AbstractRole {

    private final GacutilFlowContext flowContext;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected GacutilRole(GacutilRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
    }

    public String getDeploySourcesLocation() {
        return this.flowContext.getDeploySourcesLocation();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        this.runFlow(aaClient, GacutilFlow.class, this.flowContext);
    }

    public static class Builder extends BuilderBase<GacutilRole.Builder, GacutilRole> {

        private static final GacutilVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected GacutilVersion version;
        protected GacutilFlowContext.Builder flowContextBuilder;
        protected GacutilFlowContext flowContext;

        static {
            DEFAULT_ARTIFACT = GacutilVersion.VER_3_5;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new GacutilFlowContext.Builder();
        }

        public GacutilRole build() {
            this.initFlow();
            GacutilRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected GacutilRole getInstance() {
            return new GacutilRole(this);
        }

        protected void initFlow() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
        }

        public GacutilRole.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public GacutilRole.Builder version(GacutilVersion version) {
            this.version = version;
            return this.builder();
        }

        protected GacutilRole.Builder builder() {
            return this;
        }
    }


}