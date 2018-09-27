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

import com.ca.apm.tests.artifact.NetStockTraderVersion;
import com.ca.apm.tests.flow.netStockTrader.DeployNetStockTraderFlow;
import com.ca.apm.tests.flow.netStockTrader.NetStockTraderFlowContext;
import com.ca.apm.tests.flow.netStockTrader.UndeployNetStockTraderFlow;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import org.apache.http.util.Args;

import java.net.URL;

/**
 * @author meler02
 */
public class NetStockTraderRole extends AbstractRole {

    private final NetStockTraderFlowContext flowContext;

    private final boolean undeployOnly;
    private final boolean predeployed;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected NetStockTraderRole(NetStockTraderRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;

        undeployOnly = builder.undeployOnly;
        this.predeployed = builder.predeployed;
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
            this.runFlow(aaClient, UndeployNetStockTraderFlow.class, this.flowContext);
        } else if (!predeployed) {
            this.runFlow(aaClient, DeployNetStockTraderFlow.class, this.flowContext);
        }
    }

    public Integer getPortNumber() {
        return DeployNetStockTraderFlow.DEFAULT_STOCKTRADER_PORT;
    }

    public static class Builder extends BuilderBase<NetStockTraderRole.Builder, NetStockTraderRole> {

        private static final NetStockTraderVersion DEFAULT_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;

        protected NetStockTraderVersion version;
        protected NetStockTraderFlowContext.Builder flowContextBuilder;
        protected NetStockTraderFlowContext flowContext;

        protected boolean undeployOnly;
        protected boolean predeployed;

        static {
            DEFAULT_ARTIFACT = NetStockTraderVersion.VER_55;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_ARTIFACT);
        }

        protected void initFlowContext() {
            this.flowContextBuilder = new NetStockTraderFlowContext.Builder();
        }

        public NetStockTraderRole build() {
            this.initFlow();
            NetStockTraderRole role = this.getInstance();
            Args.notNull(role.flowContext, "Deploy flow context cannot be null.");
            return role;
        }

        protected NetStockTraderRole getInstance() {
            return new NetStockTraderRole(this);
        }

        protected void initFlow() {
            assert this.version != null;

            // Non-standard URL is resolved by the Artifact itself
            URL artifactUrl = this.version.getArtifactUrl(this.tasResolver.getRegionalArtifactory());
            this.flowContextBuilder.deployPackageUrl(artifactUrl);
            this.flowContext = this.flowContextBuilder.build();
        }

        public Builder deploySourcesLocation(String deploySourcesLocation) {
            this.flowContextBuilder.deploySourcesLocation(deploySourcesLocation);
            return this.builder();
        }

        public Builder dbRole(IRole dbRole) {
            this.flowContextBuilder.dbHostname(tasResolver.getHostnameById(dbRole.getRoleId()));
            return this.builder();
        }

        public Builder version(NetStockTraderVersion version) {
            this.version = version;
            this.flowContextBuilder.unpackDirName(version.getUnpackDir());
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