package com.ca.apm.tests.role;

import com.ca.apm.tests.flow.EmEmptyConfigureFlow;
import com.ca.apm.tests.flow.EmEmptyDeployFlowContext;
import com.ca.apm.tests.flow.EmEmptyRestartFlow;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.util.Map;

/**
 * Created by meler02 on 8/31/2016.
 */
public class EmEmptyRole extends AbstractRole {

    public static final String ENV_EM_CTX = "emContext";

    private final EmEmptyDeployFlowContext flowContext;

    private final boolean reStartAfterDeploy;

    protected EmEmptyRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        reStartAfterDeploy = builder.reStartAfterDeploy;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // configure
        runFlow(aaClient, EmEmptyConfigureFlow.class, flowContext);
        if (reStartAfterDeploy) {
            runFlow(aaClient, EmEmptyRestartFlow.class, flowContext);
        }
    }

    public static class Builder extends BuilderBase<EmEmptyRole.Builder, EmEmptyRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected EmEmptyDeployFlowContext.Builder flowContextBuilder;
        protected EmEmptyDeployFlowContext flowContext;

        protected boolean reStartAfterDeploy;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            reStartAfterDeploy = false;

            this.flowContextBuilder = new EmEmptyDeployFlowContext.Builder();
        }

        public EmEmptyRole build() {
            this.flowContext = this.flowContextBuilder.build();
            getEnvProperties().add(ENV_EM_CTX, flowContext);

            return getInstance();
        }

        protected EmEmptyRole getInstance() {
            return new EmEmptyRole(this);
        }

        protected EmEmptyRole.Builder builder() {
            return this;
        }

        public Builder installLocation(String installLocation) {
            flowContextBuilder.installLocation(installLocation);
            return this.builder();
        }

        public Builder properties(Map<String, String> properties) {
            flowContextBuilder.properties(properties);
            return this.builder();
        }

        public Builder reStartAfterDeploy() {
            this.reStartAfterDeploy = true;
            return this.builder();
        }
    }
}
