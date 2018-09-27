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

import com.ca.apm.tests.artifact.MsSqlDbVersion;
import com.ca.apm.tests.flow.msSqlDb.MsSqlDbDeployFlow;
import com.ca.apm.tests.flow.msSqlDb.MsSqlDbDeployFlowContext;
import com.ca.apm.tests.flow.msSqlDb.MsSqlDbUndeployFlow;
import com.ca.apm.tests.flow.msSqlDb.MsSqlDbUndeployFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

/**
 * MSSQL DB role class
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class MsSqlDbRole extends AbstractRole {

    private final MsSqlDbDeployFlowContext flowContext;
    private final MsSqlDbUndeployFlowContext undeployFlowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected MsSqlDbRole(MsSqlDbRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        undeployFlowContext = builder.undeployFlowContext;

        undeployOnly = builder.undeployOnly;
        predeployed = builder.predeployed;
    }

    public String getInstallSourcesLocation() {
        return this.flowContext.getInstallSourcesLocation();
    }

    public String getUnpackDirName() {
        return this.flowContext.getUnpackDirName();
    }

    public String getAdminUserPassword() {
        return flowContext.getAdminUserPassword();
    }

    public String getAdminUserName() {
        return flowContext.getAdminUserName();
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
            this.runFlow(aaClient, MsSqlDbUndeployFlow.class, this.undeployFlowContext);
        } else if (!predeployed) {
            this.runFlow(aaClient, MsSqlDbDeployFlow.class, this.flowContext);
        }
    }

    public static class Builder extends BuilderBase<MsSqlDbRole.Builder, MsSqlDbRole> {
        private static final MsSqlDbVersion DEFAULT_DB_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;
        protected MsSqlDbVersion version;
        protected MsSqlDbDeployFlowContext.Builder flowContextBuilder;
        protected MsSqlDbDeployFlowContext flowContext;
        protected MsSqlDbUndeployFlowContext.Builder undeployFlowContextBuilder;
        protected MsSqlDbUndeployFlowContext undeployFlowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        static {
            DEFAULT_DB_ARTIFACT = MsSqlDbVersion.VER_2008;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initMsSqlDbFlowContext();
            this.version(DEFAULT_DB_ARTIFACT);
        }

        protected void initMsSqlDbFlowContext() {
            this.flowContextBuilder = new MsSqlDbDeployFlowContext.Builder();
            this.undeployFlowContextBuilder = new MsSqlDbUndeployFlowContext.Builder();
        }

        public MsSqlDbRole build() {
            this.initMsSqlDbFlows();
            MsSqlDbRole msSqlDbRole = this.getInstance();
            Args.notNull(msSqlDbRole.flowContext, "Deploy flow context cannot be null.");
            return msSqlDbRole;
        }

        protected MsSqlDbRole getInstance() {
            return new MsSqlDbRole(this);
        }

        protected void initMsSqlDbFlows() {
            assert this.version != null;
            this.flowContextBuilder.installPackageUrl(this.tasResolver.getArtifactUrl(this.version.getArtifact()));
            this.flowContext = this.flowContextBuilder.build();

            undeployFlowContext = undeployFlowContextBuilder
                    .installSourcesPath(flowContext.getInstallSourcesLocation())
                    .unpackDirName(flowContext.getUnpackDirName()).installerFileName(flowContext.getInstallerFileName())
                    .installPath(flowContext.getInstallLocation()).build();
//            getEnvProperties().add(UNDEPLOY_ORACLE_DB, undeployFlowContext);
        }

        public Builder installPath(String installLocation) {
            this.flowContextBuilder.installPath(installLocation);
            return this.builder();
        }

        public Builder installSourcesPath(String installSourcesLocation) {
            this.flowContextBuilder.installSourcesPath(installSourcesLocation);
            return this.builder();
        }

        public Builder adminUserName(String adminUserName) {
            this.flowContextBuilder.adminUserName(adminUserName);
            return this.builder();
        }

        public Builder adminUserPassword(String adminUserPassword) {
            this.flowContextBuilder.adminUserPassword(adminUserPassword);
            return this.builder();
        }

        public Builder version(MsSqlDbVersion version) {
            this.version = version;
            this.flowContextBuilder.version(version.getArtifact().getVersion());
            this.flowContextBuilder.responseFileName(version.getInstallResponseFile());
            this.flowContextBuilder.unpackDirName(version.getUnpackDir());
            this.flowContextBuilder.installerFileName(version.getInstallerFile());

            this.undeployFlowContextBuilder.responseFileName(version.getUninstallResponseFile());
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

        public Builder deleteSourcesOnUndeploy(boolean deleteSourcesOnUndeploy) {
            this.undeployFlowContextBuilder.deleteSources(deleteSourcesOnUndeploy);
            return this.builder();
        }

        protected Builder builder() {
            return this;
        }
    }
}