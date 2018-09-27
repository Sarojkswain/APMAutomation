/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.tas.role.testapp.custom;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.testapp.DeployTradeServiceAppFlow;
import com.ca.apm.automation.action.flow.testapp.DeployTradeServiceAppFlowContext;
import com.ca.tas.artifact.thirdParty.custom.TradeServiceAppVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.google.common.collect.Maps;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static org.apache.http.util.Args.notNull;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static org.apache.http.util.Args.notNull;

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
    @NotNull
    private final Map<String, URL> btExportLocations;

    /**
     * Sets up the WebSphere role and defines its properties
     * 
     * @param build Builder object containing all necessary data
     */
    private TradeServiceAppRole(Builder build) {
        super(build.roleId);

        flowContext = build.flowContext;
        btExportLocations = build.btExportLocations;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        FlowConfig.FlowConfigBuilder flowConfigBuilder =
                new FlowConfig.FlowConfigBuilder(DeployTradeServiceAppFlow.class,
                        flowContext,
                        getHostingMachine().getHostnameWithPort());

        aaClient.runJavaFlow(flowConfigBuilder);
    }

    @Override
    public Map<String, String> getEnvProperties() {
        Args.notNull(flowContext, "Flow Context");

        Properties props = new Properties();
        // fetch properties from context
        for (Map.Entry<String, URL> btExportLocation : btExportLocations.entrySet()) {
            props.setProperty(format("tbexports.%s.url", btExportLocation.getKey()),
                    btExportLocation.getValue().toString());
        }
        properties.putAll(new RoleEnvironmentProperties(getRoleId(), Maps.fromProperties(props)));

        return properties;
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link TradeServiceAppRole}
     */
    public static class Builder
            extends BuilderBase<Builder, TradeServiceAppRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        private TomcatRole tomcatRole;
        private final DeployTradeServiceAppFlowContext.Builder flowContextBuilder =
                new DeployTradeServiceAppFlowContext.Builder();

        private TradeServiceAppVersion tradeServiceAppVersion = TradeServiceAppVersion.v10;
        private DeployTradeServiceAppFlowContext flowContext;
        private Map<String, URL> btExportLocations = Collections.emptyMap();
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            flowContextBuilder.stagingDir(new File("tas-stage", roleId));
        }

        public Builder tradeServiceAppVersion(@NotNull TradeServiceAppVersion tradeServiceAppVersion) {
            this.tradeServiceAppVersion = tradeServiceAppVersion;
            return this;
        }

        public Builder installDir(@NotNull String installDir) {
            this.flowContextBuilder
                    .installDir(new File(installDir));
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

            URL artifactUrl = tasResolver.getArtifactUrl(tradeServiceAppVersion.getArtifact());
            flowContextBuilder.tradeServiceAppArtifactUrl(artifactUrl);

            flowContext = flowContextBuilder.build();
            
            // resolve the location of BT exports associated
            btExportLocations = new HashMap<String, URL>(tradeServiceAppVersion.getBusinessTransactionCEMExports().size());
            for (Map.Entry<String, Artifact> businessTransactionCEMExports : tradeServiceAppVersion.getBusinessTransactionCEMExports().entrySet()) {
                URL btExportUrl = tasResolver.getArtifactUrl(businessTransactionCEMExports.getValue());
                btExportLocations.put(businessTransactionCEMExports.getKey(), btExportUrl);
            }
            TradeServiceAppRole tradeServiceAppRole = getInstance();
            tradeServiceAppRole.after(tomcatRole);
            return tradeServiceAppRole;
        }
    }

}
