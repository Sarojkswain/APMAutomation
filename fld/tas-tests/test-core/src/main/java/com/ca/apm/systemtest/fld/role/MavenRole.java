package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.DeployMavenFlow;
import com.ca.apm.systemtest.fld.flow.DeployMavenFlowContext;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author haiva01
 */
public class MavenRole extends AbstractRole {

    protected DeployMavenFlowContext flowContext;

    protected MavenRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployMavenFlow.class, flowContext);
    }

    public static class Builder extends BuilderBase<Builder, MavenRole> {

        private final String roleId;
        private final DeployMavenFlowContext.Builder flowContextBuilder;

        private DeployMavenFlowContext flowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this(roleId, tasResolver, new DeployMavenFlowContext.Builder(tasResolver));
        }

        protected Builder(String roleId, ITasResolver tasResolver,
            DeployMavenFlowContext.Builder flowContextBuilder) {
            this.roleId = roleId;
            this.flowContextBuilder = flowContextBuilder;
        }

        /**
         * Maven distribution artifact to be deployed.
         * @param artifact Maven distribution artifact
         */
        public Builder version(ITasArtifact artifact) {
            flowContextBuilder.mavenArtifact(artifact);
            return builder();
        }

        /**
         * Directory where Maven will be extracted. Maven itself is going to be a sub-directory
         * of this directory.
         *
         * @param dir    directory to extract Maven distribution into
         */
        public Builder destDir(String dir) {
            flowContextBuilder.destDir(dir);
            return builder();
        }

        @Override
        protected MavenRole getInstance() {
            return new MavenRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public MavenRole build() {
            flowContext = flowContextBuilder.build();
            return getInstance();
        }
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver, new DeployMavenFlowContext.LinuxBuilder(tasResolver));
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
