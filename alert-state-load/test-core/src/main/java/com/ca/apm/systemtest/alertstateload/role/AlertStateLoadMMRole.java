package com.ca.apm.systemtest.alertstateload.role;

import org.apache.http.util.Args;

import com.ca.apm.systemtest.alertstateload.flow.DeployFileFlow;
import com.ca.apm.systemtest.alertstateload.flow.DeployFileFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

public class AlertStateLoadMMRole extends AbstractRole {

    private final EmRole emRole;

    protected AlertStateLoadMMRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.emRole = builder.emRole;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        DeployFileFlowContext deployFileFlowContext =
            (new DeployFileFlowContext.Builder())
                .srcFile("/mm/AlertStateLoadMM.jar")
                .dstFilePath(
                    new String[] {emRole.getDeployEmFlowContext().getInstallDir(), "config",
                            "modules", "AlertStateLoadMM.jar"}).build();
        runFlow(aaClient, DeployFileFlow.class, deployFileFlowContext);
    }

    public static class Builder extends BuilderBase<Builder, AlertStateLoadMMRole> {
        private String roleId;
        private EmRole emRole;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        @Override
        public AlertStateLoadMMRole build() {
            Args.notNull(emRole, "emRole");
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected AlertStateLoadMMRole getInstance() {
            AlertStateLoadMMRole role = new AlertStateLoadMMRole(this);
            return role;
        }

        public Builder emRole(EmRole emRole) {
            Args.notNull(emRole, "emRole");
            this.emRole = emRole;
            return builder();
        }
    }

}
