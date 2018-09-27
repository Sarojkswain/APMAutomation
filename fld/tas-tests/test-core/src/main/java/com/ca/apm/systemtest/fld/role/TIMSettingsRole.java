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

package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.systemtest.fld.flow.TIMSettingsFlow;
import com.ca.apm.systemtest.fld.flow.TIMSettingsFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;


/**
 * Set installed TIM
 *
 * @author filja01
 */
public class TIMSettingsRole extends AbstractRole{

    private final TIMSettingsFlowContext flowContext;
    
    /**
     * @param builder a {@link com.ca.tas.role.TIMRole.Builder} object.
     */
    public TIMSettingsRole(Builder builder) {
        super(builder.roleId);

        flowContext = builder.flowContext;
    }

    /**
     * Main method driving role deployment.
     *
     * @param aaClient AA client used for triggering flows
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(TIMSettingsFlow.class, flowContext,
            getHostingMachine().getHostnameWithPort()));
    }

    public static class Builder extends BuilderBase<Builder, TIMSettingsRole> {

        private final String roleId;
        /*private final ITasResolver tasResolver;
        private String requestType;
        private String timHostname;
        private String settingName;
        private String settingValue;
        private String networkInterfaces;
        */
        private TIMSettingsFlowContext flowContext;
        private TIMSettingsFlowContext.Builder flowContextBuilder;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        //    this.tasResolver = tasResolver;
            flowContextBuilder = new TIMSettingsFlowContext.Builder();
        }

        @Override
        protected TIMSettingsRole getInstance() {
            return new TIMSettingsRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public TIMSettingsRole build() {
            flowContext = flowContextBuilder.build();
            return new TIMSettingsRole(this);
        }

        public Builder requestType(String requestType) {
            flowContextBuilder.requestType(requestType);
            return this;
        }

        public Builder timHostname(String timHostname) {
            flowContextBuilder.timHostname(timHostname);
            return this;
        }

        public Builder settingName(String settingName) {
            flowContextBuilder.settingName(settingName);
            return this;
        }

        public Builder settingValue(String settingValue) {
            flowContextBuilder.settingValue(settingValue);
            return this;
        }
        
        public Builder networkInterfaces(String networkInterfaces) {
            flowContextBuilder.networkInterfaces(networkInterfaces);
            return this;
        }

    }
}
