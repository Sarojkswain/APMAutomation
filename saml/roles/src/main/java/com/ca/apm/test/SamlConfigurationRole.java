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
package com.ca.apm.test;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class SamlConfigurationRole extends AbstractRole {
    private static final int ASYNC_DELAY = 90;
    private static final String EM_STATUS = "Introscope Enterprise Manager started";
    private static final String WEBVIEW_STATUS = "Introscope WebView started";

    private static SamlConfigureFlowContext flowContext;

    private Builder builder;

    private SamlConfigurationRole(Builder builder) {
        super(builder.roleId);
        this.builder = builder;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        client.runJavaFlow(new FlowConfigBuilder(SamlConfigureFlow.class,
                flowContext,
                getHostingMachine().getHostnameWithPort()));

        startEm(client);
        startWv(client);
    }

    public static class Builder extends BuilderBase<Builder, SamlConfigurationRole> {

        public String emExecutable = "Introscope_Enterprise_Manager.exe";
        public String wvExecutable = "Introscope_Webview.exe";
        public String roleId;

        private final SamlConfigureFlowContext.Builder contextBuilder =
                new SamlConfigureFlowContext.Builder();

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        public Builder apmRootDir(String apmRootDir) {
            contextBuilder.apmRootDir(apmRootDir);
            return this;
        }

        public Builder wvPort(int wvPort) {
            contextBuilder.wvPort(wvPort);
            return this;
        }

        public Builder wsPort(int wsPort) {
            contextBuilder.wsPort(wsPort);
            return this;
        }

        @Override
        public SamlConfigurationRole build() {
            flowContext = contextBuilder.build();
            return getInstance();
        }

        @Override
        @NotNull
        public SamlConfigurationRole getInstance() {
            return new SamlConfigurationRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId) {
            super(roleId);
            emExecutable = "Introscope_Enterprise_Manager";
            wvExecutable = "Introscope_WebView";
        }

    }

    private void startEm(IAutomationAgentClient client) {
        RunCommandFlowContext cmdFlowContext =
                new RunCommandFlowContext.Builder(builder.emExecutable)
                        .workDir(flowContext.getApmRootDir()).name(getRoleId())
                        .terminateOnMatch(EM_STATUS).build();

        client.runJavaFlow(
                new FlowConfigBuilder(RunCommandFlow.class, cmdFlowContext, getEmHostWithPort())
                        .delay(ASYNC_DELAY).async()
                );
    }

    private void startWv(IAutomationAgentClient client) {
        RunCommandFlowContext cmdFlowContext =
                new RunCommandFlowContext.Builder(builder.wvExecutable)
                        .workDir(flowContext.getApmRootDir()).name(getRoleId())
                        .terminateOnMatch(WEBVIEW_STATUS).build();

        client.runJavaFlow(
                new FlowConfigBuilder(RunCommandFlow.class, cmdFlowContext, getEmHostWithPort())
                        .delay(ASYNC_DELAY).async()
                );
    }

    private String getEmHostWithPort() {
        return getHostingMachine().getHostnameWithPort();
    }
}
