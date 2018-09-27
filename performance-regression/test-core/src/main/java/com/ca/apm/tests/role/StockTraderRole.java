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

import com.ca.apm.tests.artifact.StockTraderVersion;
import com.ca.apm.tests.flow.StockTraderFlow;
import com.ca.apm.tests.flow.StockTraderFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.WebLogicRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class StockTraderRole extends AbstractRole {

    private final StockTraderFlowContext flowContext;

    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected StockTraderRole(StockTraderRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        this.predeployed = builder.predeployed;
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (!predeployed) {
            this.runFlow(aaClient, StockTraderFlow.class, this.flowContext);
        }
    }

    public Integer getPortNumber() {
        return flowContext.getWeblogicPort();
    }

    public static class Builder extends BuilderBase<StockTraderRole.Builder, StockTraderRole> {

        private static final StockTraderVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected StockTraderVersion version;
        protected StockTraderFlowContext.Builder flowContextBuilder;
        protected StockTraderFlowContext flowContext;

        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = StockTraderVersion.VER_55;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new StockTraderFlowContext.Builder();
        }

        public StockTraderRole build() {
            this.initFlow();
            StockTraderRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected StockTraderRole getInstance() {
            return new StockTraderRole(this);
        }

        protected void initFlow() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
        }

        public StockTraderRole.Builder deploySourcesLocation(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public StockTraderRole.Builder weblogicUserName(String weblogicUserName) {
            this.flowContextBuilder.weblogicUserName(weblogicUserName);
            return this.builder();
        }

        public StockTraderRole.Builder weblogicUserPassword(String weblogicUserPassword) {
            this.flowContextBuilder.weblogicUserPassword(weblogicUserPassword);
            return this.builder();
        }

        public StockTraderRole.Builder weblogicTargetServer(String weblogicTargetServer) {
            this.flowContextBuilder.weblogicTargetServer(weblogicTargetServer);
            return this.builder();
        }

        public StockTraderRole.Builder weblogicRole(WebLogicRole wlsRole) {
            this.flowContextBuilder.weblogicInstallPath(wlsRole.getInstallDir());
            return this.builder();
        }

        public StockTraderRole.Builder weblogicRole(Weblogic103Role wlsRole) {
            this.flowContextBuilder.weblogicInstallPath(wlsRole.getInstallDir());
            return this.builder();
        }

        public StockTraderRole.Builder dbRole(IRole dbRole) {
            this.flowContextBuilder.dbHostname(tasResolver.getHostnameById(dbRole.getRoleId()));
            return this.builder();
        }

        public StockTraderRole.Builder version(StockTraderVersion version) {
            this.version = version;
            this.flowContextBuilder.applicationEarFileName(version.getApplicationEar());
            this.flowContextBuilder.applicationBslEarFileName(version.getApplicationBslEar());
            this.flowContextBuilder.createDatasourceScriptFileName(version.getCreateDatasourceScript());
            this.flowContextBuilder.deleteDatasourceScriptFileName(version.getDeleteDatasourceScript());
            this.flowContextBuilder.propertiesFileFileName(version.getPropertiesFile());
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

        protected StockTraderRole.Builder builder() {
            return this;
        }
    }


}