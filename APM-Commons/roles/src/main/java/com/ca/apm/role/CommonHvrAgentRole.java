package com.ca.apm.role;

import static org.apache.commons.lang.Validate.notNull;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.commons.flow.DeployHvrAgentFlow;
import com.ca.apm.commons.flow.DeployHvrAgentFlowContext;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Created by nick on 1.10.14.
 */
public class CommonHvrAgentRole extends AbstractRole {

    private static final Logger LOG = LoggerFactory.getLogger(CommonHvrAgentRole.class);

    private static final String HVRAGENT_GROUP            = "com.ca.apm.coda-projects.test-tools";
    private static final String HVRAGENT_ID               = "hvragent";
    private static final String HVRAGENT_CLASSIFIER       = "dist";
    private static final TasExtension HVRAGENT_EXTENSION  = TasExtension.ZIP;

    @NotNull
    private final DeployHvrAgentFlowContext deployHvrAgentFlowContext;


    /**
     * @param builder Builder
     */
    private CommonHvrAgentRole(Builder builder) {
        super(builder.roleId);
        deployHvrAgentFlowContext = builder.deployHvrAgentFlowContextBuilder.build();

    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
      try {
        LOG.info("Deploying HVR Agent");
        deployAgent(aaClient);


      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void deployAgent(IAutomationAgentClient aaClient) throws Exception {
        FlowConfig.FlowConfigBuilder flowConfigBuilder =
                new FlowConfig.FlowConfigBuilder(DeployHvrAgentFlow.class,
                                                 deployHvrAgentFlowContext,
                                                 getHostingMachine().getHostnameWithPort());

        aaClient.runJavaFlow(flowConfigBuilder);
    }



    public static class Builder extends BuilderBase<Builder, CommonHvrAgentRole> {

        private final DeployHvrAgentFlowContext.Builder deployHvrAgentFlowContextBuilder;
        @NotNull private final ITasResolver tasResolver;
        private Version version;

        private String roleId;
        public Builder stagingDir(String value) {
            deployHvrAgentFlowContextBuilder.stagingDir(value);
            return this;
        }

        public Builder installDir(String value) {
            deployHvrAgentFlowContextBuilder.installDir(value);

            return this;
        }

        public Builder version(Version value) {
            version = value;
            return this;
        }


        public Builder(@NotNull String roleId,
                       @NotNull ITasResolver tasResolver) {
            notNull(roleId);
            notNull(tasResolver);
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            deployHvrAgentFlowContextBuilder = new DeployHvrAgentFlowContext.Builder();
        }

        @Override
        public CommonHvrAgentRole build() {
            deployHvrAgentFlowContextBuilder
                .hvrAgentUrl(tasResolver.getArtifactUrl(createHvrAgentArtifact()));


            return new CommonHvrAgentRole(this);
        }

        protected ITasArtifact createHvrAgentArtifact() {
            return new TasArtifact.Builder(HVRAGENT_ID)
                    .groupId(HVRAGENT_GROUP)
                    .extension(HVRAGENT_EXTENSION)
                    .classifier(HVRAGENT_CLASSIFIER)
                    .version(version == null ? tasResolver.getDefaultVersion() : version.toString())
                    .build();
        }

        @Override
        protected CommonHvrAgentRole getInstance() {
            // TODO Auto-generated method stub
            return new CommonHvrAgentRole(this);
        }

        @Override
        protected Builder builder() {
            // TODO Auto-generated method stub
            return this;
        }
    }
}
