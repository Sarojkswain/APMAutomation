package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.webapp.weblogic.DeployWebLogicFlowContext;
import com.ca.apm.tests.flow.PerfWeblogicDeployFlow;
import com.ca.apm.tests.flow.weblogic.PerfWeblogicUndeployFlow;
import com.ca.apm.tests.flow.weblogic.PerfWeblogicUndeployFlowContext;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.WebLogicRole;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Extension to WebSphere8Role allowing setting cell and node name during profile creation
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Deprecated
public class PerfWeblogicRole extends WebLogicRole {

    public static final String UNDEPLOY_WEBSLOGIC = "undeployWeblogic";

    private final DeployWebLogicFlowContext webLogicFlowContext;
    private final PerfWeblogicUndeployFlowContext undeployFlowContext;

    @NotNull
    private final ITasResolver tasResolver;
    private final boolean undeployOnly;
    private final boolean predeployed;

    protected PerfWeblogicRole(Builder builder) {
        super(builder);

        this.tasResolver = builder.tasResolver;
        webLogicFlowContext = builder.webLogicFlowContext;
        undeployFlowContext = builder.undeployFlowContext;
        undeployOnly = builder.undeployOnly;
        this.predeployed = builder.predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (undeployOnly) {
            this.runFlow(aaClient, PerfWeblogicUndeployFlow.class, this.undeployFlowContext);
        } else if (!predeployed) {
            this.deployWlsContext(aaClient);
        }
    }

    @Override
    protected void deployWlsContext(IAutomationAgentClient aaClient) {
        this.runFlow(aaClient, PerfWeblogicDeployFlow.class, this.webLogicFlowContext);
    }

    public static class Builder extends WebLogicRole.Builder {

        private final DeployWebLogicFlowContext.Builder webLogicFlowCtxBuilder;
        private final ITasResolver tasResolver;
        protected DeployWebLogicFlowContext webLogicFlowContext;
        protected PerfWeblogicUndeployFlowContext.Builder undeployFlowContextBuilder;
        protected PerfWeblogicUndeployFlowContext undeployFlowContext;
        protected boolean undeployOnly;
        protected boolean predeployed;

        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);

            webLogicFlowCtxBuilder = new DeployWebLogicFlowContext.Builder();
            undeployFlowContextBuilder = new PerfWeblogicUndeployFlowContext.Builder();

            this.tasResolver = tasResolver;
        }


        @Override
        protected PerfWeblogicRole getInstance() {
            return new PerfWeblogicRole(this);
        }

        @Override
        public PerfWeblogicRole build() {
            super.build();
            this.initWebLogicDeployContext();
            undeployFlowContext = undeployFlowContextBuilder.parentDir(this.webLogicFlowContext.getInstallLocation())
                    .installDir(this.webLogicFlowContext.getWlsInstallDir()).build();
//            getEnvProperties().add(UNDEPLOY_WEBSPHERE8, undeployFlowContext);

            return this.getInstance();
        }

        @Override
        protected void initWebLogicDeployContext() {
            super.initWebLogicDeployContext();
            URL artifactUrl = this.tasResolver.getArtifactUrl(this.webLogicArtifact);
            String fileName = this.getFilename(this.webLogicArtifact);
            Args.notNull(fileName, "File name");
            this.webLogicFlowContext = this.webLogicFlowCtxBuilder.webLogicInstallerUrl(artifactUrl).webLogicInstallerFilename(fileName).build();
        }

        @Override
        public Builder version(@NotNull WebLogicVersion webLogicVersion) {
            super.version(webLogicVersion);
            this.webLogicArtifact = webLogicVersion.getArtifact();
            return this.builder();
        }

        @Override
        public Builder version(@NotNull Artifact webLogicArtifact) {
            super.version(webLogicArtifact);
            this.webLogicArtifact = webLogicArtifact;
            return this.builder();
        }

        @Override
        public Builder webLogicInstallerDir(@NotNull String webLogicInstallerDir) {
            super.webLogicInstallerDir(webLogicInstallerDir);
            this.webLogicFlowCtxBuilder.webLogicInstallerDir(webLogicInstallerDir);
            return this.builder();
        }

        @Override
        public Builder installLocation(@NotNull String installLocation) {
            super.installLocation(installLocation);
            this.webLogicFlowCtxBuilder.installLocation(installLocation);
            return this.builder();
        }

        @Override
        public Builder installDir(@NotNull String wlsInstallDir) {
            super.installDir(wlsInstallDir);
            this.webLogicFlowCtxBuilder.wlsInstallDir(wlsInstallDir);
            this.getEnvProperties().add("wls.home", wlsInstallDir);
            return this.builder();
        }

        @Override
        public Builder customJvm(String customJvm) {
            super.customJvm(customJvm);
            this.webLogicFlowCtxBuilder.localJvm(customJvm);
            return this.builder();
        }

        public Builder undeployOnly(boolean undeployOnly) {
            this.undeployOnly = undeployOnly;
            return this.builder();
        }

        public Builder undeployOnly() {
            this.undeployOnly = true;
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

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
