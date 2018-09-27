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

import com.ca.apm.tests.artifact.AgentTrussVersion;
import com.ca.apm.tests.flow.agent.DeployJavaAgentFlow;
import com.ca.apm.tests.flow.agent.DeployJavaAgentFlowContext;
import com.ca.apm.tests.flow.agent.RegisterJavaAgentFlow;
import com.ca.apm.tests.flow.agent.RegisterJavaAgentFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.IWebAppServerRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class JavaAgentRole extends AbstractRole {

    public static final String REGISTER_AGENT = "registerAgent";
    public static final String UNREGISTER_AGENT = "unregisterAgent";

    private final DeployJavaAgentFlowContext flowContext;
    private final RegisterJavaAgentFlowContext registerFlowContext;

    private final boolean registerAgent;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected JavaAgentRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        registerFlowContext = builder.registerFlowContext;

        registerAgent = builder.registerAgent;
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

    public Boolean isSiEnabled() {
        return flowContext.isSiEnabled();
    }

    public Boolean isAccEnabled() {
        return flowContext.isAccEnabled();
    }

    public Boolean isAccDefault() {
        return flowContext.isAccDefault();
    }

    public Boolean isAccMockOn() {
        return flowContext.isAccMockOn();
    }

    public Boolean isBtOn() {
        return flowContext.isBtOn();
    }

    public Boolean isBrtmEnabled() {
        return flowContext.isBrtmEnabled();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        this.runFlow(aaClient, DeployJavaAgentFlow.class, flowContext);
        if (registerAgent) {
            this.runFlow(aaClient, RegisterJavaAgentFlow.class, this.registerFlowContext);
        }
    }

    public static class Builder extends BuilderBase<Builder, JavaAgentRole> {

        private static final AgentTrussVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected URL artifactUrl;

        protected DeployJavaAgentFlowContext.Builder flowContextBuilder;
        protected DeployJavaAgentFlowContext flowContext;

        protected RegisterJavaAgentFlowContext.Builder registerFlowContextBuilder;
        protected RegisterJavaAgentFlowContext registerFlowContext;

        protected boolean registerAgent;


        static {
            DEFAULT_ARTIFACT = AgentTrussVersion.ISCP_10_0_GA_TOMCAT_WIN; // TODO
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.initFlowContext();

            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            flowContextBuilder = new DeployJavaAgentFlowContext.Builder();
            registerFlowContextBuilder = new RegisterJavaAgentFlowContext.Builder();

            this.registerAgent = false;
        }

        public JavaAgentRole build() {
            this.initFlow();
            initRegisterAgentFlow();

            JavaAgentRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected JavaAgentRole getInstance() {
            return new JavaAgentRole(this);
        }

        protected void initFlow() {
            assert this.artifactUrl != null;

            // Truss URL is resolved by the Artifact itself
            flowContextBuilder.deployPackageUrl(artifactUrl);
            flowContext = flowContextBuilder.build();
        }

        protected void initRegisterAgentFlow() {
            assert flowContext != null;
            registerFlowContext = registerFlowContextBuilder.build();
            getEnvProperties().add(REGISTER_AGENT, registerFlowContext);
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            registerFlowContextBuilder.agentPath(deploySourcesLocation);
            return this.builder();
        }

        public Builder enableSmartInstrumentation() {
            flowContextBuilder.siEnabled(true);
            return this.builder();
        }

        public Builder disableSmartInstrumentation() {
            flowContextBuilder.siEnabled(false);
            return this.builder();
        }

        public Builder enableAcc() {
            flowContextBuilder.accEnabled(true);
            return this.builder();
        }

        public Builder accDefault() {
            flowContextBuilder.accDefault(true);
            return this.builder();
        }

        public Builder disableAcc() {
            flowContextBuilder.accEnabled(false);
            return this.builder();
        }

        public Builder accDefault(boolean accDefault) {
            flowContextBuilder.accDefault(accDefault);
            return this.builder();
        }

        public Builder accMockOn(boolean accMockOn) {
            flowContextBuilder.accMockOn(accMockOn);
            return this.builder();
        }

        public Builder btOn() {
            flowContextBuilder.btOn(true);
            return this.builder();
        }

        public Builder enableBrtm() {
            flowContextBuilder.brtmEnabled(true);
            return this.builder();
        }

        public Builder disableBrtm() {
            flowContextBuilder.brtmEnabled(true);
            return this.builder();
        }

        public Builder emLocation(String emLocation) {
            flowContextBuilder.emLocation(emLocation);
            return this.builder();
        }

        public Builder agentName(String agentName) {
            flowContextBuilder.agentName(agentName);
            return this.builder();
        }

        public Builder appServer(IWebAppServerRole appServer) {
            registerFlowContextBuilder.serverType(appServer.getApplicationServerType());
            if (appServer instanceof Websphere85Role) {
                Websphere85Role wasRole = (Websphere85Role) appServer;
                registerFlowContextBuilder.serverXmlFilePath(wasRole.getProfilePath() + "/config/cells/" + wasRole.getCellName() + "/nodes/" +
                        wasRole.getNodeName() + "/servers/server1");
            } else {
                registerFlowContextBuilder.serverXmlFilePath(appServer.getInstallDir()); // todo resolve
            }
            return this.builder();
        }

        public Builder registerAgent() {
            this.registerAgent = true;
            return this.builder();
        }

        public Builder version(AgentTrussVersion version) {
            this.artifactUrl = version.getArtifactUrl();
            registerFlowContextBuilder.agentVersion(version.getArtifact().getVersion());
            flowContextBuilder.buildNumber(version.getBuildNumber());
            return this.builder();
        }

//        public Builder version(String version, String appServer, String os) {
//            artifactUrl = AgentTrussVersion.getArtifactUrl(this.tasResolver.getRegionalArtifactory(), version, appServer, os);
//            registerFlowContextBuilder.agentVersion(version);
//            return this.builder();
//        }

        public Builder version(String version, String appServer, String os) {
            artifactUrl = AgentTrussVersion.getArtifactUrl(this.tasResolver, version, appServer, os);
            registerFlowContextBuilder.agentVersion(version);
            return this.builder();
        }

        public Builder undeployExistingBeforeInstall(boolean undeployExistingBeforeInstall) {
            flowContextBuilder.undeployExistingBeforeInstall(undeployExistingBeforeInstall);
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }


}