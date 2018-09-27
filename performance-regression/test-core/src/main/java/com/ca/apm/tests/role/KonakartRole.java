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

import com.ca.apm.tests.artifact.KonakartVersion;
import com.ca.apm.tests.flow.KonakartFlow;
import com.ca.apm.tests.flow.KonakartFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class KonakartRole extends AbstractRole {

    private final KonakartFlowContext flowContext;

    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected KonakartRole(KonakartRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        this.predeployed = builder.predeployed;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    public Integer getPortNumber() {
        return flowContext.getPortNumber();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (!predeployed) {
            this.runFlow(aaClient, KonakartFlow.class, this.flowContext);
        }
    }

    public static class Builder extends BuilderBase<KonakartRole.Builder, KonakartRole> {

        private static final KonakartVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected KonakartVersion version;
        protected KonakartFlowContext.Builder flowContextBuilder;
        protected KonakartFlowContext flowContext;

        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = KonakartVersion.VER_5_2_0_0_WIN;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new KonakartFlowContext.Builder();
        }

        public KonakartRole build() {
            this.initFlow();
            KonakartRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected KonakartRole getInstance() {
            return new KonakartRole(this);
        }

        protected void initFlow() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return builder();
        }

        public Builder tomcatRole(TomcatRole tomcatRole) {
            this.flowContextBuilder.tomcatInstallPath(tomcatRole.getInstallDir());
            return builder();
        }

        public Builder dbRole(IRole dbRole) {
            this.flowContextBuilder.dbHostname(tasResolver.getHostnameById(dbRole.getRoleId()));
            return builder();
        }

        public Builder javaRole(JavaRole javaRole) {
            this.flowContextBuilder.javaJRE(javaRole.getInstallDir());
            return builder();
        }

        public Builder installationDir(String installationDir) {
            this.flowContextBuilder.installationDir(installationDir);
            return builder();
        }

        public Builder version(KonakartVersion version) {
            this.version = version;
            this.flowContextBuilder.setupFileName(version.getSetupFileName());
            return builder();
        }

        public Builder predeployed() {
            this.predeployed = true;
            return builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return builder();
        }

        protected KonakartRole.Builder builder() {
            return this;
        }
    }


}