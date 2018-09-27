/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.systemtest.atcqa.role;

import static org.apache.http.util.Args.notNull;

import java.io.File;
import java.util.Map;

import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.systemtest.atcqa.artifact.thirdparty.OrderEngineAppVersion;
import com.ca.apm.systemtest.atcqa.artifact.thirdparty.ReportingServiceAppVersion;
import com.ca.apm.systemtest.atcqa.artifact.thirdparty.TradeServiceAppVersion;
import com.ca.apm.systemtest.atcqa.flow.DeployTradeServiceAppFlow;
import com.ca.apm.systemtest.atcqa.flow.DeployTradeServiceAppFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.TomcatRole;

/**
 * This role represents Trade Service web application for testing purposes which has to be deployed
 * into any supported JEE web/application server.
 * 
 * Role is immutable and is designed to be instantiated via Builder attached to the class.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
public class TradeServiceAppRole extends AbstractRole {

    @NotNull
    private final DeployTradeServiceAppFlowContext flowContext;

    /**
     * Sets up the WebSphere role and defines its properties
     * 
     * @param build Builder object containing all necessary data
     */
    private TradeServiceAppRole(Builder build) {
        super(build.roleId);
        flowContext = build.flowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        FlowConfig.FlowConfigBuilder flowConfigBuilder =
            new FlowConfig.FlowConfigBuilder(DeployTradeServiceAppFlow.class, flowContext,
                getHostingMachine().getHostnameWithPort());
        aaClient.runJavaFlow(flowConfigBuilder);
    }

    @Override
    public Map<String, String> getEnvProperties() {
        Args.notNull(flowContext, "Flow Context");
        return properties;
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link TradeServiceAppRole}
     */
    public static class Builder extends BuilderBase<Builder, TradeServiceAppRole> {
        private final String roleId;
        private final ITasResolver tasResolver;
        private TomcatRole tomcatRole;
        private final DeployTradeServiceAppFlowContext.Builder flowContextBuilder =
            new DeployTradeServiceAppFlowContext.Builder();

        private TradeServiceAppVersion tradeServiceAppVersion = TradeServiceAppVersion.v_10_5_2_4;
        private ReportingServiceAppVersion reportingServiceAppVersion =
            ReportingServiceAppVersion.v_10_5_2_4;
        private OrderEngineAppVersion orderEngineAppVersion = OrderEngineAppVersion.v_10_5_2_4;
        private DeployTradeServiceAppFlowContext flowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            flowContextBuilder.stagingDir(new File("tas-stage", roleId));
        }

        public Builder tradeServiceAppVersion(@NotNull TradeServiceAppVersion tradeServiceAppVersion) {
            this.tradeServiceAppVersion = tradeServiceAppVersion;
            return this;
        }

        public Builder reportingServiceAppVersion(
            @NotNull ReportingServiceAppVersion reportingServiceAppVersion) {
            this.reportingServiceAppVersion = reportingServiceAppVersion;
            return this;
        }

        public Builder orderEngineAppVersion(@NotNull OrderEngineAppVersion orderEngineAppVersion) {
            this.orderEngineAppVersion = orderEngineAppVersion;
            return this;
        }

        public Builder installDir(@NotNull String installDir) {
            this.flowContextBuilder.installDir(new File(installDir));
            return this;
        }

        public Builder stagingDir(@NotNull String stagingDir) {
            this.flowContextBuilder.stagingDir(new File(stagingDir));
            return this;
        }

        public Builder tomcatRole(@NotNull TomcatRole tomcatRole) {
            this.tomcatRole = tomcatRole;
            return this;
        }

        @Override
        protected TradeServiceAppRole getInstance() {
            return new TradeServiceAppRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        /**
         * Builds instance of {@link TradeServiceAppRole}
         */
        @Override
        public TradeServiceAppRole build() {
            notNull(tomcatRole, "The role of Tomcat web server");
            flowContextBuilder.installDir(tomcatRole.getWebappsDirectory());

            flowContextBuilder.tradeServiceAppArtifactUrl(tasResolver
                .getArtifactUrl(tradeServiceAppVersion.getArtifact()));
            flowContextBuilder.reportingServiceAppArtifactURL(tasResolver
                .getArtifactUrl(reportingServiceAppVersion.getArtifact()));
            flowContextBuilder.orderEngineAppArtifactURL(tasResolver
                .getArtifactUrl(orderEngineAppVersion.getArtifact()));

            flowContext = flowContextBuilder.build();

            TradeServiceAppRole tradeServiceAppRole = getInstance();
            tradeServiceAppRole.after(tomcatRole);
            return tradeServiceAppRole;
        }
    }

}
