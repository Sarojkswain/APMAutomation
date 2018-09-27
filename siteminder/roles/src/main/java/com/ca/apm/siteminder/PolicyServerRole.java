package com.ca.apm.siteminder;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.property.TestProperty;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class PolicyServerRole extends AbstractRole {
    
    private final RolePropertyContainer propertyContainer;
    private final DeployPolicyServerFlowContext policyServerFlowContext;
    private final ITasResolver tasResolver;
 
    private PolicyServerRole(Builder build) {
        super(build.roleId);
        policyServerFlowContext = build.policyServerFlowContext;
        propertyContainer = build.envPropertyContainer;
        tasResolver = build.tasResolver;
    }
    
    @Override
    public void deploy(IAutomationAgentClient client) {

        // set the Java home to the recently installed one
        final String newJavaHome = policyServerFlowContext.getJavaHomeDir();
        RunCommandFlowContext cmdFlow = new RunCommandFlowContext.Builder("setx")
                        .args(Arrays.asList("JAVA_HOME", newJavaHome, "/m")).workDir("C:\\Windows\\System32").build();
        client.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, cmdFlow, getHostingMachine().getHostnameWithPort()));

        client.runJavaFlow(new FlowConfigBuilder(DeployPolicyServerFlow.class, policyServerFlowContext,
                                                 getHostingMachine().getHostnameWithPort()));
    }

    @Override
    public Map<String, String> getEnvProperties() {
        properties.putAll(new RoleEnvironmentProperties(getRoleId(), propertyContainer.getTestPropertiesAsProperties()));
        return properties;
    }

    protected String getInstallDir() {
        return policyServerFlowContext.getPSHomeDir();
    }

    /**
     * This builder requires a java installation directory set to know where to look for the needed
     * 32-bit Java JRE.
     * 
     * @author surma04
     *
     */
    public static class Builder extends BuilderBase<Builder, PolicyServerRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        private PolicyServerVersion psPackedInstallSourcesVersion = PolicyServerVersion.v1252sp01x86w;
        private final DeployPolicyServerFlowContext.Builder psFlowCtxBuilder = new DeployPolicyServerFlowContext.Builder();
        private final RolePropertyContainer envPropertyContainer = new RolePropertyContainer();
        private DeployPolicyServerFlowContext policyServerFlowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        public Builder psInstallLocation(String pathToPsHome) {
            psFlowCtxBuilder.installLocation(pathToPsHome);
            envPropertyContainer.add(new TestProperty<>("ps.home", pathToPsHome));
            return this;
        }

        public Builder javaInstallDir(String javaHome) {
            psFlowCtxBuilder.javaLocation(javaHome);
            return this;
        }

        public Builder psPackedInstallSourcesLocation(String psLocation) {
            psFlowCtxBuilder.psPackedInstallSourcesLocation(new File(psLocation));
            return this;
        }

        public Builder psUnpackedInstallSourcesDir(String psUnpackedInstallSourcesDir) {
            psFlowCtxBuilder.psUnpackedInstallSourcesDir(new File(psUnpackedInstallSourcesDir));
            return this;
        }

        public Builder psPackedInstallSourcesVersion(PolicyServerVersion psVersion) {
            psPackedInstallSourcesVersion = psVersion;
            return this;
        }

        public Builder responseFileDir(String responseFileDir) {
            psFlowCtxBuilder.responseFileDir(new File(responseFileDir));
            return this;
        }

        @Override
        protected PolicyServerRole getInstance() {
            return new PolicyServerRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public PolicyServerRole build() {
            URL artifactUrl = tasResolver.getArtifactUrl(psPackedInstallSourcesVersion.getArtifact());
            psFlowCtxBuilder.psPackedInstallSourcesUrl(artifactUrl);
            psFlowCtxBuilder.psInstallExecutable(psPackedInstallSourcesVersion.getExecutable());
            policyServerFlowContext = psFlowCtxBuilder.build();
            return getInstance();
        }
    }
}
