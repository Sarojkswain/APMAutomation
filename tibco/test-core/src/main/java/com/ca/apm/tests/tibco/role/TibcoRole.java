/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.role;


import java.util.LinkedHashMap;

import java.util.Map;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.IFlowContext;

import com.ca.apm.tests.tibco.flow.DeployTibcoFlow;
import com.ca.apm.tests.tibco.flow.DeployTibcoFlowContext;
import com.ca.apm.tests.tibco.flow.StartBWServiceFlow;
import com.ca.apm.tests.tibco.flow.TibcoConstants;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * EmptyRole class.
 *
 * Template for creating new roles.
 *
 * @author Vashistha Singh (sinva01@ca.com)
 */
public class TibcoRole extends AbstractRole implements TibcoConstants {

    private final DeployTibcoFlowContext flowContext;
    private final Map<Class, IFlowContext> flowsToRun;
    private final ITasResolver tasResolver;
    private final RolePropertyContainer propertyContainer;

    /**
     * @param builder
     *        Builder object containing all necessary data
     */
    protected TibcoRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        flowContext = builder.flowContext;
        tasResolver = builder.tasResolver;
        flowsToRun = builder.flowsToRun;
        propertyContainer = builder.getEnvProperties();
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(DeployTibcoFlow.class, flowContext,
            getHostingMachine().getHostnameWithPort()));
        doPostInstallConfig(aaClient);

    }

    public void doPostInstallConfig(IAutomationAgentClient aaClient) {
        for (Class flow : flowsToRun.keySet()) {
            runFlow(aaClient, flow, flowsToRun.get(flow));
        }
        // Start the BWAdmin and Hawk Services
        if (flowContext.shouldAutoStartBWService()
            && flowContext.getRoleId().equals(TIBCO_ADMIN_ROLE_ID)) {
            runFlow(aaClient, StartBWServiceFlow.class, flowContext);
        }
    }

    @Override
    public Map<String, String> getEnvProperties() {
        properties.putAll(new RoleEnvironmentProperties(getRoleId(), propertyContainer
            .getTestPropertiesAsProperties()));

        return properties;
    }

    /**
     * Linux Builder responsible for holding all necessary properties to
     * instantiate {@link TibcoRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link TibcoRole}
     */
    public static class Builder extends BuilderBase<Builder, TibcoRole> {

        private final Map<Class, IFlowContext> flowsToRun = new LinkedHashMap<>();

        private final String roleId;

        private final ITasResolver tasResolver;

        private DeployTibcoFlowContext flowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public TibcoRole build() {

            return getInstance();
        }

        @Override
        protected TibcoRole getInstance() {
            return new TibcoRole(this);
        }

        public Builder flowContext(DeployTibcoFlowContext context) {
            Args.notNull(context, "Deployment Flow Context Not set");
            flowContext = context;
            return this;
        }

        public <T extends IAutomationFlow> Builder addPostInstallationFlow(Class<T> flowClass,
            IFlowContext context) {
            flowsToRun.put(flowClass, context);
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
