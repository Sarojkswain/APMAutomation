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

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.WeblogicVersion;
import com.ca.apm.tests.flow.weblogic.PerfWeblogic12DeployFlow;
import com.ca.apm.tests.flow.weblogic.PerfWeblogic12UndeployFlow;
import com.ca.apm.tests.flow.weblogic.PerfWeblogicDeployFlow;
import com.ca.apm.tests.flow.weblogic.PerfWeblogicDeployFlowContext;
import com.ca.apm.tests.flow.weblogic.PerfWeblogicUndeployFlow;
import com.ca.apm.tests.flow.weblogic.PerfWeblogicUndeployFlowContext;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.IWebAppServerRole;

/**
 * Extension to WebSphere8Role allowing setting cell and node name during profile creation
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class Weblogic103Role extends AbstractRole implements IWebAppServerRole {

    public static final String ENV_WEBLOGIC_START = "weblogicStart";
    public static final String ENV_WEBLOGIC_STOP = "weblogicStop";

    private final PerfWeblogicDeployFlowContext flowContext;
    private final PerfWeblogicUndeployFlowContext undeployFlowContext;

    private final RunCommandFlowContext startFlowContext;
    private final RunCommandFlowContext stopFlowContext;

    private final boolean wls12c;

    private final boolean undeployOnly;
    private final boolean predeployed;

    protected Weblogic103Role(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        flowContext = builder.flowContext;
        undeployFlowContext = builder.undeployFlowContext;
        startFlowContext = builder.startFlowContext;
        stopFlowContext = builder.stopFlowContext;

        wls12c = builder.wls12c;

        undeployOnly = builder.undeployOnly;
        this.predeployed = builder.predeployed;
    }

    public RunCommandFlowContext getStartFlowContext() {
        return startFlowContext;
    }

    public RunCommandFlowContext getStopFlowContext() {
        return stopFlowContext;
    }

    public boolean isUndeployOnly() {
        return undeployOnly;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (undeployOnly) {
            if (wls12c) {
                this.runFlow(aaClient, PerfWeblogic12UndeployFlow.class, this.undeployFlowContext);
            } else {
                this.runFlow(aaClient, PerfWeblogicUndeployFlow.class, this.undeployFlowContext);
            }
        } else if (!predeployed) {
            if (wls12c) {
                this.runFlow(aaClient, PerfWeblogic12DeployFlow.class, this.flowContext);
            } else {
                this.runFlow(aaClient, PerfWeblogicDeployFlow.class, this.flowContext);
            }
        }
    }

    @Override
    public ApplicationServerType getApplicationServerType() {
        return ApplicationServerType.WEBLOGIC;
    }

    @NotNull
    @Override
    public Collection<IRole> getStopRoles() {
        return null; // todo
    }

    @NotNull
    @Override
    public Collection<IRole> getStartRoles() {
        return null; // todo
    }

    @Override
    public String getInstallDir() {
        return flowContext.getInstallDir();
    }

    @Override
    public String getWebappsDirectory() {
        return null; // todo
    }

    public static class Builder extends BuilderBase<Weblogic103Role.Builder, Weblogic103Role> {
        private static final WebLogicVersion DEFAULT_ARTIFACT;

        private final String roleId;
        private final ITasResolver tasResolver;
        protected PerfWeblogicDeployFlowContext.Builder flowContextBuilder;
        protected PerfWeblogicDeployFlowContext flowContext;
        protected PerfWeblogicUndeployFlowContext.Builder undeployFlowContextBuilder;
        protected PerfWeblogicUndeployFlowContext undeployFlowContext;

        protected RunCommandFlowContext.Builder startFlowContextBuilder;
        protected RunCommandFlowContext startFlowContext;
        protected RunCommandFlowContext.Builder stopFlowContextBuilder;
        protected RunCommandFlowContext stopFlowContext;

        protected boolean wls12c;

        protected boolean undeployOnly;
        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = WebLogicVersion.v103x86w;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            wls12c = false;

            flowContextBuilder = new PerfWeblogicDeployFlowContext.Builder();
            undeployFlowContextBuilder = new PerfWeblogicUndeployFlowContext.Builder();
            this.startFlowContextBuilder = new RunCommandFlowContext.Builder("startWebLogic.cmd");
            this.stopFlowContextBuilder = new RunCommandFlowContext.Builder("stopWebLogic.cmd");

            this.version(DEFAULT_ARTIFACT);
        }


        @Override
        protected Weblogic103Role getInstance() {
            return new Weblogic103Role(this);
        }

        @Override
        public Weblogic103Role build() {
            // deploy context
            this.flowContext = flowContextBuilder.build();
            // undeploy context
            undeployFlowContext = undeployFlowContextBuilder.parentDir(this.flowContext.getBeaHome())
                    .sourcesLocation(flowContext.getSourcesLocation())
                    .installDir(this.flowContext.getInstallDir()).build();
            // start context
            this.startFlowContext = this.startFlowContextBuilder
                    .workDir(this.concatPaths(this.flowContext.getInstallDir(), "samples", "domains", "wl_server", "bin"))
                    .terminateOnMatch("Server started in RUNNING mode")
                    .build();
            this.getEnvProperties().add(ENV_WEBLOGIC_START, this.startFlowContext);
            // stop context
            this.stopFlowContext = this.stopFlowContextBuilder
                    .workDir(this.concatPaths(this.flowContext.getInstallDir(), "samples", "domains", "wl_server", "bin"))
                    .build();
            this.getEnvProperties().add(ENV_WEBLOGIC_STOP, this.stopFlowContext);

            return this.getInstance();
        }

        public Builder version(@NotNull WebLogicVersion webLogicVersion) {
            URL artifactUrl = this.tasResolver.getArtifactUrl(webLogicVersion.getArtifact());
            String fileName = this.getFilename(webLogicVersion.getArtifact());
            this.flowContextBuilder.installerUrl(artifactUrl);
            this.flowContextBuilder.installerFileName(fileName);
            return this.builder();
        }

        public Builder version(@NotNull WeblogicVersion weblogicVersion) {
            URL artifactUrl = this.tasResolver.getArtifactUrl(weblogicVersion.getArtifact());
            String fileName = this.getFilename(weblogicVersion.getArtifact());
            this.flowContextBuilder.installerUrl(artifactUrl);
            this.flowContextBuilder.installerFileName(fileName);
            if (WeblogicVersion.v1213generic == weblogicVersion) {
                wls12c = true;
            }
            return this.builder();
        }

        public Builder version(@NotNull Artifact webLogicArtifact) {
            URL artifactUrl = this.tasResolver.getArtifactUrl(webLogicArtifact);
            String fileName = this.getFilename(webLogicArtifact);
            this.flowContextBuilder.installerUrl(artifactUrl);
            this.flowContextBuilder.installerFileName(fileName);
            return this.builder();
        }

        public Builder sourcesLocation(@NotNull String sourcesLocation) {
            this.flowContextBuilder.sourcesLocation(sourcesLocation);
            return this.builder();
        }

        public Builder beaHome(@NotNull String beaHome) {
            this.flowContextBuilder.beaHome(beaHome);
            return this.builder();
        }

        public Builder installDir(@NotNull String installDir) {
            this.flowContextBuilder.installDir(installDir);
            this.getEnvProperties().add("wls.home", installDir);
            return this.builder();
        }

        public Builder customJvm(String customJvm) {
            this.flowContextBuilder.customJvm(customJvm);
            return this.builder();
        }

        public Builder startEnvParams(Map<String, String> startEnvParams) {
            this.startFlowContextBuilder.environment(startEnvParams);
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
