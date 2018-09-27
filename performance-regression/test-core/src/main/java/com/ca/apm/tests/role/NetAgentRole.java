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

import com.ca.apm.automation.action.flow.DeployEmptyFlowContext;
import com.ca.apm.tests.artifact.NetAgentTrussVersion;
import com.ca.apm.tests.flow.agent.*;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class NetAgentRole extends AbstractRole {

    public static final String REGISTER_AGENT = "registerAgent";
    public static final String UNREGISTER_AGENT = "unregisterAgent";

    private final DeployNetAgentFlowContext flowContext;
    private final RegisterNetAgentFlowContext registerFlowContext;

    private final boolean registerAgent;
    private final boolean startAgent;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected NetAgentRole(NetAgentRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        registerFlowContext = builder.registerFlowContext;

        registerAgent = builder.registerAgent;
        startAgent = builder.startAgent;
    }

    public String getDeploySourcesLocation() {
        return flowContext.getDeploySourcesLocation();
    }

    public String getAgentName() {
        return flowContext.getAgentName();
    }

    public String getBuildNumber() {
        return flowContext.getBuildNumber();
    }

    public String getAgentVersion() {
        return registerFlowContext.getAgentVersion();
    }

    public boolean isDiEnabled() {
        return flowContext.isDiEnabled();
    }

    public boolean isBtOn() {
        return flowContext.isBtOn();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        this.runFlow(aaClient, DeployNetAgentFlow.class, this.flowContext);
        if (registerAgent) {
            this.runFlow(aaClient, RegisterNetAgentFlow.class, this.registerFlowContext);
        }
        if (startAgent) {
            this.runFlow(aaClient, StartNetAgentFlow.class, new DeployEmptyFlowContext.Builder().build());
        }
    }

    public static class Builder extends BuilderBase<NetAgentRole.Builder, NetAgentRole> {

        private static final NetAgentTrussVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected URL artifactUrl;

        protected DeployNetAgentFlowContext.Builder flowContextBuilder;
        protected DeployNetAgentFlowContext flowContext;

        protected RegisterNetAgentFlowContext.Builder registerFlowContextBuilder;
        protected RegisterNetAgentFlowContext registerFlowContext;

        protected boolean registerAgent;
        protected boolean startAgent;


        static {
            DEFAULT_ARTIFACT = NetAgentTrussVersion.NET_10_0_GA_x64;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();

            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            flowContextBuilder = new DeployNetAgentFlowContext.Builder();
            registerFlowContextBuilder = new RegisterNetAgentFlowContext.Builder();

            this.registerAgent = false;
            this.startAgent = false;
        }

        public NetAgentRole build() {
            this.initFlow();
            initRegisterAgentFlow();

            NetAgentRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected NetAgentRole getInstance() {
            return new NetAgentRole(this);
        }

        protected void initFlow() {
            assert this.artifactUrl != null;

            // Truss URL is resolved by the Artifact itself
//            URL artifactUrl = this.version.getArtifactUrl();
            flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = flowContextBuilder.build();
        }

        protected void initRegisterAgentFlow() {
            assert flowContext != null;
            registerFlowContext = registerFlowContextBuilder.build();
            getEnvProperties().add(REGISTER_AGENT, registerFlowContext);
        }

        public NetAgentRole.Builder deploySourcesLocation(String deploySourcesLocation) {
            flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            this.registerFlowContextBuilder.agentPath(deploySourcesLocation);
            return this.builder();
        }

        public NetAgentRole.Builder enableDeepInstrumentation() {
            flowContextBuilder.diEnabled(true);
            return this.builder();
        }

        public NetAgentRole.Builder btOn() {
            flowContextBuilder.btOn(true);
            return this.builder();
        }

        public NetAgentRole.Builder emLocation(String emLocation) {
            flowContextBuilder.emLocation(emLocation);
            return this.builder();
        }

        public NetAgentRole.Builder agentName(String agentName) {
            flowContextBuilder.agentName(agentName);
            return this.builder();
        }

        public NetAgentRole.Builder gacutilPath(String gacutilPath) {
            registerFlowContextBuilder.gacutilPath(gacutilPath);
            return this.builder();
        }

        public NetAgentRole.Builder registerAgent() {
            this.registerAgent = true;
            return this.builder();
        }

        public NetAgentRole.Builder startAgent() {
            this.registerAgent = true;
            this.startAgent = true;
            return this.builder();
        }

        public NetAgentRole.Builder version(NetAgentTrussVersion version) {
            this.artifactUrl = version.getArtifactUrl();
            registerFlowContextBuilder.agentVersion(version.getArtifact().getVersion());
            flowContextBuilder.buildNumber(version.getBuildNumber());
            return this.builder();
        }

//        public NetAgentRole.Builder version(String version, int bitMode) {
//            artifactUrl = NetAgentTrussVersion.getArtifactUrl(this.tasResolver.getRegionalArtifactory(), version, bitMode);
//            this.registerFlowContextBuilder.agentVersion(version);
//            return this.builder();
//        }

        public NetAgentRole.Builder version(String version, int bitMode) {
            artifactUrl = NetAgentTrussVersion.getArtifactUrl(this.tasResolver, version, bitMode);
            registerFlowContextBuilder.agentVersion(version);
            return this.builder();
        }

        public NetAgentRole.Builder version(String version, String dllVersion, int bitMode) {
            artifactUrl = NetAgentTrussVersion.getArtifactUrl(this.tasResolver, version, bitMode);
            registerFlowContextBuilder.agentVersion(version);
            registerFlowContextBuilder.agentDllVersion(dllVersion);
            return this.builder();
        }

        public Builder undeployExistingBeforeInstall(boolean undeployExistingBeforeInstall) {
            flowContextBuilder.undeployExistingBeforeInstall(undeployExistingBeforeInstall);
            return this.builder();
        }

        protected NetAgentRole.Builder builder() {
            return this;
        }
    }


}