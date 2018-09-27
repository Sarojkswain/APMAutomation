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

import com.ca.apm.tests.artifact.StocktraderTradeDbScriptVersion;
import com.ca.apm.tests.flow.oracleDb.WebAppTradeDbScriptFlow;
import com.ca.apm.tests.flow.oracleDb.WebAppTradeDbScriptFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class StocktraderTradeDbScriptRole extends AbstractRole {

    public static final String RECREATE_DB_TABLES = "recreateDbTables";

    private final WebAppTradeDbScriptFlowContext flowContext;

    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected StocktraderTradeDbScriptRole(StocktraderTradeDbScriptRole.Builder builder) {
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
            this.runFlow(aaClient, WebAppTradeDbScriptFlow.class, this.flowContext);
        }
    }

    public static class Builder extends BuilderBase<StocktraderTradeDbScriptRole.Builder, StocktraderTradeDbScriptRole> {

        private static final StocktraderTradeDbScriptVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected StocktraderTradeDbScriptVersion version;
        protected WebAppTradeDbScriptFlowContext.Builder flowContextBuilder;
        protected WebAppTradeDbScriptFlowContext flowContext;

        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = StocktraderTradeDbScriptVersion.VER_55;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new WebAppTradeDbScriptFlowContext.Builder();
        }

        public StocktraderTradeDbScriptRole build() {
            this.initFlow();
            StocktraderTradeDbScriptRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected StocktraderTradeDbScriptRole getInstance() {
            return new StocktraderTradeDbScriptRole(this);
        }

        protected void initFlow() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
            getEnvProperties().add(RECREATE_DB_TABLES, flowContext);
        }

        public StocktraderTradeDbScriptRole.Builder unpackDir(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public StocktraderTradeDbScriptRole.Builder runAsUser(String user) {
            this.flowContextBuilder.runAsUser(user);
            return this.builder();
        }

        public StocktraderTradeDbScriptRole.Builder runAsPassword(String password) {
            this.flowContextBuilder.runAsPassword(password);
            return this.builder();
        }

        public StocktraderTradeDbScriptRole.Builder runAsSysdba(boolean runAsSysdba) {
            this.flowContextBuilder.runAsSysdba(runAsSysdba);
            return this.builder();
        }

        public StocktraderTradeDbScriptRole.Builder schemaName(String schemaName) {
            this.flowContextBuilder.schemaName(schemaName);
            return this.builder();
        }

        public StocktraderTradeDbScriptRole.Builder plsqlExecutableLocation(String plsqlExecutableLocation) {
            this.flowContextBuilder.plsqlExecutableLocation(plsqlExecutableLocation);
            return this.builder();
        }

        public StocktraderTradeDbScriptRole.Builder version(StocktraderTradeDbScriptVersion version) {
            this.version = version;
            this.flowContextBuilder.sqlFileName(version.getSqlFileName());
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

        protected StocktraderTradeDbScriptRole.Builder builder() {
            return this;
        }
    }


}