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

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.Websphere85Version;
import com.ca.apm.tests.flow.websphere85.Websphere85DeployFlow;
import com.ca.apm.tests.flow.websphere85.Websphere85DeployFlowContext;
import com.ca.apm.tests.flow.websphere85.Websphere85UndeployFlow;
import com.ca.apm.tests.flow.websphere85.Websphere85UndeployFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.IWebAppServerRole;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * MSSQL DB role class
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class Websphere85Role extends AbstractRole implements IWebAppServerRole {

    public static final String ENV_WEBSPHERE_START = "webSphereStart";
    public static final String ENV_WEBSPHERE_STOP = "webSphereStop";

    private final Websphere85DeployFlowContext flowContext;
    private final Websphere85UndeployFlowContext undeployFlowContext;

    private final RunCommandFlowContext startFlowContext;
    private final RunCommandFlowContext stopFlowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected Websphere85Role(Websphere85Role.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        flowContext = builder.flowContext;
        undeployFlowContext = builder.undeployFlowContext;
        startFlowContext = builder.startFlowContext;
        stopFlowContext = builder.stopFlowContext;

        undeployOnly = builder.undeployOnly;
        predeployed = builder.predeployed;
    }

    public String getProfileName() {
        return flowContext.getProfileName();
    }

    public String getCellName() {
        return flowContext.getCellName();
    }

    public String getNodeName() {
        return flowContext.getNodeName();
    }

    public String getProfilePath() {
        return flowContext.getProfilePath();
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
            this.runFlow(aaClient, Websphere85UndeployFlow.class, this.undeployFlowContext);
        } else if (!predeployed) {
            this.runFlow(aaClient, Websphere85DeployFlow.class, this.flowContext);
        }
    }

    @Override
    public ApplicationServerType getApplicationServerType() {
        return ApplicationServerType.WEBSPHERE;
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
        return flowContext.getInstallWasLocation();
    }

    @Override
    public String getWebappsDirectory() {
        return null; // todo
    }

    public static class Builder extends BuilderBase<Websphere85Role.Builder, Websphere85Role> {
        private static final Websphere85Version DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected Websphere85DeployFlowContext.Builder flowContextBuilder;
        protected Websphere85DeployFlowContext flowContext;
        protected Websphere85UndeployFlowContext.Builder undeployFlowContextBuilder;
        protected Websphere85UndeployFlowContext undeployFlowContext;

        protected RunCommandFlowContext.Builder startFlowContextBuilder;
        protected RunCommandFlowContext startFlowContext;
        protected RunCommandFlowContext.Builder stopFlowContextBuilder;
        protected RunCommandFlowContext stopFlowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = Websphere85Version.VER_85_JAVA7;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.flowContextBuilder = new Websphere85DeployFlowContext.Builder();
            this.undeployFlowContextBuilder = new Websphere85UndeployFlowContext.Builder();
            this.startFlowContextBuilder = new RunCommandFlowContext.Builder("startServer.bat");
            this.stopFlowContextBuilder = new RunCommandFlowContext.Builder("stopServer.bat");

            this.version(DEFAULT_ARTIFACT);
        }

        public Websphere85Role build() {
            // deploy context
            this.flowContext = this.flowContextBuilder.build();
            // undeploy context
            undeployFlowContext = undeployFlowContextBuilder
                    .installManagerDir(flowContext.getManagerInstallLocation())
                    .installLocation(flowContext.getInstallWasLocation()).build();
            // start context
            this.startFlowContext = this.startFlowContextBuilder
                    .workDir(this.concatPaths(new String[]{this.flowContext.getProfilePath(), "bin"}))
                    .args(Collections.singletonList("server1")).terminateOnMatch(String.format("Server %s open for e-business", new Object[]{"server1"}))
                    .build();
            this.getEnvProperties().add(ENV_WEBSPHERE_START, this.startFlowContext);
            // stop context
            this.stopFlowContext = this.stopFlowContextBuilder
                    .workDir(this.concatPaths(new String[]{this.flowContext.getProfilePath(), "bin"}))
                    .args(Collections.singletonList("server1"))
                    .build();
            this.getEnvProperties().add(ENV_WEBSPHERE_STOP, this.stopFlowContext);

            Websphere85Role role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected Websphere85Role getInstance() {
            return new Websphere85Role(this);
        }

        public Builder installManagerLocation(String installManagerLocation) {
            this.flowContextBuilder.installManagerLocation(installManagerLocation);
            this.undeployFlowContextBuilder.installManagerDir(installManagerLocation);
            return this.builder();
        }

        public Builder installWasLocation(String installWasLocation) {
            this.flowContextBuilder.installWasLocation(installWasLocation);
            this.undeployFlowContextBuilder.installLocation(installWasLocation);
            return this.builder();
        }

        public Builder imSharedLocation(String imSharedLocation) {
            this.flowContextBuilder.imSharedLocation(imSharedLocation);
            return this.builder();
        }

        public Builder hostName(String hostName) {
            this.flowContextBuilder.hostName(hostName);
            return this.builder();
        }

        public Builder version(Websphere85Version version) {

            this.flowContextBuilder.managerZipPackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getInstallMgrArtifact()));
            this.flowContextBuilder.java7Zip1PackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getJavaZip1Artifact()));
            this.flowContextBuilder.java7Zip2PackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getJavaZip2Artifact()));
            this.flowContextBuilder.java7Zip3PackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getJavaZip3Artifact()));
            this.flowContextBuilder.was85Zip1PackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getWasZip1Artifact()));
            this.flowContextBuilder.was85Zip2PackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getWasZip2Artifact()));
            this.flowContextBuilder.was85Zip3PackageUrl(version.getArtifactUrl(this.tasResolver.getRegionalArtifactory(),
                    version.getWasZip3Artifact()));
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

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return this.builder();
        }

        public Builder predeployed() {
            this.predeployed = true;
            return this.builder();
        }

//        public Builder deleteSourcesOnUndeploy(boolean deleteSourcesOnUndeploy) {
//            this.undeployFlowContextBuilder.deleteSources(deleteSourcesOnUndeploy);
//            return this.builder();
//        }

        protected Builder builder() {
            return this;
        }
    }
}