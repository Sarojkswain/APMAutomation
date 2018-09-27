/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.role;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.transactiontrace.appmap.flow.LoadBalanceAgentsFlow;
import com.ca.apm.transactiontrace.appmap.flow.LoadBalanceAgentsFlowContext;
import com.ca.apm.transactiontrace.appmap.testbed.ClusteredTestbed;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;

/**
 * Role to add loadbalancing configuration to MOM
 *
 * Starts EM and WV after finishing configuration
 *
 * @author ...
 */
public class LoadBalanceAgentsRole extends AbstractRole {

    private static final int ASYNC_DELAY = 90;
    private static final String EM_STATUS = "Introscope Enterprise Manager started";
    private static final String WEBVIEW_STATUS = "Introscope WebView started";

    private final Builder builder;
    LoadBalanceAgentsFlowContext flowContext;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected LoadBalanceAgentsRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.builder = builder;
        flowContext = builder.flowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(LoadBalanceAgentsFlow.class, flowContext, getHostingMachine().getHostnameWithPort()));
        startEm(aaClient);
        startWv(aaClient);
    }

    private void startEm(IAutomationAgentClient client) {
        RunCommandFlowContext cmdFlowContext =
            new RunCommandFlowContext.Builder(builder.emExecutable)
                .workDir(flowContext.getApmRootDir()).name(getRoleId())
                .terminateOnMatch(EM_STATUS).build();

        client.runJavaFlow(
            new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, cmdFlowContext, getHostingMachine().getHostnameWithPort())
                .delay(ASYNC_DELAY).async()
        );
    }

    private void startWv(IAutomationAgentClient client) {
        RunCommandFlowContext cmdFlowContext =
            new RunCommandFlowContext.Builder(builder.wvExecutable)
                .workDir(flowContext.getApmRootDir()).name(getRoleId())
                .terminateOnMatch(WEBVIEW_STATUS).build();

        client.runJavaFlow(
            new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, cmdFlowContext, getHostingMachine().getHostnameWithPort())
                .delay(ASYNC_DELAY).async()
        );
    }


    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link LoadBalanceAgentsRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            emExecutable = EmRole.LinuxBuilder.INTROSCOPE_EXECUTABLE;
            wvExecutable = EmRole.LinuxBuilder.WEBVIEW_EXECUTABLE;
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
     * Builder responsible for holding all necessary properties to instantiate {@link LoadBalanceAgentsRole}
     */
    public static class Builder extends BuilderBase<Builder, LoadBalanceAgentsRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        private LoadBalanceAgentsFlowContext.Builder flowContextBuilder = new LoadBalanceAgentsFlowContext.Builder();

        private LoadBalanceAgentsFlowContext flowContext;

        protected String emExecutable = EmRole.Builder.INTROSCOPE_EXECUTABLE;
        protected String wvExecutable = EmRole.Builder.WEBVIEW_EXECUTABLE;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public LoadBalanceAgentsRole build() {
            String[] collectorInfo = new String[3];
            collectorInfo[0] = tasResolver.getHostnameById(ClusteredTestbed.COLLECTOR_1_ROLE_ID);
            collectorInfo[1] = tasResolver.getHostnameById(ClusteredTestbed.COLLECTOR_2_ROLE_ID);
            collectorInfo[2] = tasResolver.getHostnameById(ClusteredTestbed.COLLECTOR_3_ROLE_ID);
            flowContextBuilder.collectorInfo(collectorInfo);

            flowContext = flowContextBuilder.build();

            return getInstance();
        }

        @Override
        protected LoadBalanceAgentsRole getInstance() {
            return new LoadBalanceAgentsRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
