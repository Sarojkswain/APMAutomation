package com.ca.apm.systemtest.fld.role;

import java.net.URL;

import com.ca.apm.systemtest.fld.artifact.thirdparty.WebSphereLibertyArtifact;
import com.ca.apm.systemtest.fld.flow.DeployWebSphereLiberty;
import com.ca.apm.systemtest.fld.flow.DeployWebSphereLibertyContext;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author haiva01
 */
public class WebSphereLibertyDeployRole extends AbstractRole {
    public static final String DEPLOY_WEB_SPHERE_LIBERTY_CONTEXT_DATA
        = "deployWebSphereLibertyContextData";

    protected DeployWebSphereLibertyContext flowContext;

    protected WebSphereLibertyDeployRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployWebSphereLiberty.class, flowContext);
    }

    public static class Builder extends BuilderBase<Builder, WebSphereLibertyDeployRole> {
        private final String roleId;
        private final ITasResolver tasResolver;
        private final DeployWebSphereLibertyContext.Builder flowContextBuilder;
        private DeployWebSphereLibertyContext flowContext;

        public Builder(String roleId, ITasResolver tasResolver,
            DeployWebSphereLibertyContext.Builder flowContextBuilder) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.flowContextBuilder = flowContextBuilder;

            // Initialize some defaults.

            wlpArtifact(WebSphereLibertyArtifact.v8_5_5_9_javaee7);
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this(roleId, tasResolver, new DeployWebSphereLibertyContext.Builder(tasResolver));
        }

        public Builder wlpArtifact(ITasArtifact artifact) {
            return wlpArtifact(tasResolver.getArtifactUrl(artifact));
        }

        public Builder wlpArtifact(URL wlpArtifactUrl) {
            flowContextBuilder.wlpArtifact(wlpArtifactUrl);
            return builder();
        }

        public Builder destDir(String destDir) {
            flowContextBuilder.destDir(destDir);
            return builder();
        }

        @Override
        protected WebSphereLibertyDeployRole getInstance() {
            return new WebSphereLibertyDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public WebSphereLibertyDeployRole build() {
            flowContext = flowContextBuilder.build();
            getEnvProperties().add(DEPLOY_WEB_SPHERE_LIBERTY_CONTEXT_DATA, flowContext);
            return getInstance();
        }
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver, new DeployWebSphereLibertyContext.LinuxBuilder(tasResolver));
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }
}
