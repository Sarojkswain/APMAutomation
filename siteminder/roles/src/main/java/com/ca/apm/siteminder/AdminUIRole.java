package com.ca.apm.siteminder;

import java.io.File;
import java.net.URL;
import java.util.Map;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.property.TestProperty;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;

public class AdminUIRole extends AbstractRole
{
    private final RolePropertyContainer propertyContainer;
    private final DeployAdminUIFlowContext adminUIFlowContext;
    // private final ITasResolver tasResolver;
    
    private AdminUIRole(Builder build) {
        super(build.roleId);
        adminUIFlowContext = build.adminUIFlowContext;
        propertyContainer = build.envPropertyContainer;
        // tasResolver = build.tasResolver;
    }

    @Override
    public void deploy(IAutomationAgentClient client)
    {
        client.runJavaFlow(new FlowConfigBuilder(DeployAdminUIFlow.class, adminUIFlowContext,
                                                 getHostingMachine().getHostnameWithPort()));
        
    }
    
    @Override
    public Map<String, String> getEnvProperties() {
        properties.putAll(new RoleEnvironmentProperties(getRoleId(), propertyContainer.getTestPropertiesAsProperties()));
        return properties;
    }
    
    public static class Builder implements IBuilder<AdminUIRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        
        private AdminUIVersion auiPackedInstallSourcesVersion = AdminUIVersion.v1252sp01x86w;
        private AdminUIPreReqVersion auiprPackedInstallSourcesVersion = AdminUIPreReqVersion.v1252sp01x86w;
        
        private final DeployAdminUIFlowContext.Builder auiFlowCtxBuilder = new DeployAdminUIFlowContext.Builder(); 
        private final RolePropertyContainer envPropertyContainer = new RolePropertyContainer();
        private DeployAdminUIFlowContext adminUIFlowContext;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }
        
        public Builder auiInstallLocation(String pathToAuiHome) {
            auiFlowCtxBuilder.installLocation(new File(pathToAuiHome));
            envPropertyContainer.add(new TestProperty<>("aui.home", pathToAuiHome));
            return this;
        }
        
        public Builder auiPackedInstallSourcesLocation(String auiLocation) {
            auiFlowCtxBuilder.auiPackedInstallSourcesLocation(new File(auiLocation));
            return this;
        }
        
        public Builder auiUnpackedInstallSourcesDir(String auiUnpackedInstallSourcesDir) {
            auiFlowCtxBuilder.auiUnpackedInstallSourcesDir(new File(auiUnpackedInstallSourcesDir));
            return this;
        }
        
        public Builder auiPackedInstallSourcesVersion(AdminUIVersion auiVersion) {
            auiPackedInstallSourcesVersion = auiVersion;
            return this;
        }
        
        public Builder auiprPackedInstallSourcesVersion(AdminUIPreReqVersion auiprVersion) {
            auiprPackedInstallSourcesVersion = auiprVersion;
            return this;
        }
        
        public Builder responseFileDir(String responseFileDir) {
            auiFlowCtxBuilder.responseFileDir(new File(responseFileDir));
            return this;
        }
        
        @Override
        public AdminUIRole build()
        {
            URL auiArtifactUrl = tasResolver.getArtifactUrl(auiPackedInstallSourcesVersion.getArtifact());
            URL auiprArtifactUrl = tasResolver.getArtifactUrl(auiprPackedInstallSourcesVersion.getArtifact());
            
            auiFlowCtxBuilder.auiPackedInstallSourcesUrl(auiArtifactUrl);
            auiFlowCtxBuilder.auiprPackedInstallSourcesUrl(auiprArtifactUrl);
            auiFlowCtxBuilder.auiExecutable(auiPackedInstallSourcesVersion.getExecutable());
            auiFlowCtxBuilder.auiPrereqExecutable(auiprPackedInstallSourcesVersion.getExecutable());
            
            adminUIFlowContext = auiFlowCtxBuilder.build();
            return new AdminUIRole(this);
        }
        
    }
    
}
