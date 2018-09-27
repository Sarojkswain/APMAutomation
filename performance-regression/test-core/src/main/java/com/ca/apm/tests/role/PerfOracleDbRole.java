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
import com.ca.apm.automation.action.flow.oracle.DeployOracleDbFlowContext;
import com.ca.apm.tests.flow.oracleDb.MyOracleDbDeployFlow;
import com.ca.apm.tests.flow.oracleDb.OracleDbStopFlow;
import com.ca.apm.tests.flow.oracleDb.OracleDbUndeployFlow;
import com.ca.apm.tests.flow.oracleDb.OracleDbUndeployFlowContext;
import com.ca.tas.artifact.thirdParty.OracleDbVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class PerfOracleDbRole extends AbstractRole {

    public static final String UNDEPLOY_ORACLE_DB = "undeployOracleDb";

    private final DeployOracleDbFlowContext flowContext;

    private final OracleDbUndeployFlowContext undeployFlowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    public PerfOracleDbRole(Builder builder) {
        super(builder.roleId);
        this.flowContext = builder.flowContext;

        undeployFlowContext = builder.undeployFlowContext;
        undeployOnly = builder.undeployOnly;
        this.predeployed = builder.predeployed;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (undeployOnly) {
            this.runFlow(aaClient, OracleDbStopFlow.class, new DeployEmptyFlowContext.Builder().build());
            this.runFlow(aaClient, OracleDbUndeployFlow.class, this.undeployFlowContext);
        } else if (!predeployed) {
            this.runFlow(aaClient, MyOracleDbDeployFlow.class, this.flowContext);
        }
    }

    public static class Builder extends BuilderBase<PerfOracleDbRole.Builder, PerfOracleDbRole> {

        private static final OracleDbVersion DEFAULT_DB_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;
        protected Artifact oracleDbArtifact;
        protected DeployOracleDbFlowContext.Builder flowContextBuilder;
        protected DeployOracleDbFlowContext flowContext;

        protected OracleDbUndeployFlowContext.Builder undeployFlowContextBuilder;
        protected OracleDbUndeployFlowContext undeployFlowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        static {
            DEFAULT_DB_ARTIFACT = OracleDbVersion.Oracle11gR1w;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initOracleDbDeployFlowContext();
            this.version((Artifact) this.getDefaultVersionArtifact());

            undeployFlowContextBuilder = new OracleDbUndeployFlowContext.Builder();
        }

        protected void initOracleDbDeployFlowContext() {
            this.flowContextBuilder = new DeployOracleDbFlowContext.Builder();
        }

        @Override
        public PerfOracleDbRole build() {
            this.initOracleDbDeployFlow();
            PerfOracleDbRole oracleDbRole = this.getInstance();
            Args.notNull(oracleDbRole.flowContext, "Deploy flow context cannot be null.");

            undeployFlowContext = undeployFlowContextBuilder
                    .responseFileDir(flowContext.getResponseFileDir()).responseFileName(flowContext.getResponseFileName())
                    .installSourcesPath(flowContext.getInstallSourcesLocation()).homePath(flowContext.getOracleHome())
                    .dbSid(flowContext.getDbSid()).superAdminSamePassword(flowContext.getSuperAdminSamePassword())
                    .installLocation(flowContext.getInstallLocation()).build();
            getEnvProperties().add(UNDEPLOY_ORACLE_DB, undeployFlowContext);

            return getInstance();
        }

        @Override
        protected PerfOracleDbRole getInstance() {
            return new PerfOracleDbRole(this);
        }

        protected void initOracleDbDeployFlow() {
            assert this.oracleDbArtifact != null;

            this.flowContextBuilder.installPackageUrl(this.tasResolver.getArtifactUrl(this.oracleDbArtifact));
            this.flowContext = this.flowContextBuilder.build();
        }

        protected Artifact getDefaultVersionArtifact() {
            return DEFAULT_DB_ARTIFACT.getArtifact();
        }

        public Builder installPath(String installLocation) {
            this.flowContextBuilder.installPath(installLocation);
            return this.builder();
        }

        public Builder installSourcesPath(String installSourcesLocation) {
            this.flowContextBuilder.installSourcesPath(installSourcesLocation);
            return this.builder();
        }

        public Builder homePath(String oracleHome) {
            this.flowContextBuilder.homePath(oracleHome);
            return this.builder();
        }

        public Builder homeName(String oracleHomeName) {
            this.flowContextBuilder.homeName(oracleHomeName);
            return this.builder();
        }

        public Builder responseFileDir(String responseFileDir) {
            this.flowContextBuilder.responseFileDir(responseFileDir);
            return this.builder();
        }

        public Builder version(OracleDbVersion version) {
            return this.version((Artifact) version.getArtifact());
        }

        public Builder version(Artifact oracleDbInstallerArtifact) {
            Args.notNull(oracleDbInstallerArtifact, "Oracle DB version artifact");
            this.oracleDbArtifact = oracleDbInstallerArtifact;
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

        protected Builder builder() {
            return this;
        }
    }
}
