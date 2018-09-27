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
package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author meler02
 */
public class MQMonitorRole extends AbstractRole {

    public static final String ENV_RUN_MQMONITOR = "execMqMonitor";
    public static final String ENV_STOP_MQMONITOR = "stopMqMonitor";

    private final RunCommandFlowContext runCommandFlowContext;
    private final RunCommandFlowContext stopCommandFlowContext;
    private final boolean executeMqMonitor;
    private final String mqMonitorInstallDir;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected MQMonitorRole(MQMonitorRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        runCommandFlowContext = builder.runCommandFlowContext;
        stopCommandFlowContext = builder.stopCommandFlowContext;
        executeMqMonitor = builder.executeMqMonitor;
        mqMonitorInstallDir = builder.mqMonitorInstallDir;
    }

    public String getMqMonitorInstallDir() {
        return mqMonitorInstallDir;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        executeMqMonitor(aaClient);
    }

    protected void executeMqMonitor(IAutomationAgentClient aaClient) {
        if (executeMqMonitor) {
            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, runCommandFlowContext,
                    getHostWithPort()));
        }
    }

    public RunCommandFlowContext getRunCommandFlowContext() {
        return runCommandFlowContext;
    }

    public RunCommandFlowContext getStopCommandFlowContext() {
        return stopCommandFlowContext;
    }

    public boolean isExecuteMqMonitor() {
        return executeMqMonitor;
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link MQMonitorRole}
     */
    public static class Builder extends BuilderBase<MQMonitorRole.Builder, MQMonitorRole> {

        protected final String roleId;
        protected final ITasResolver tasResolver;

        protected RunCommandFlowContext.Builder runCommandFlowContextBuilder;

        @Nullable
        protected RunCommandFlowContext runCommandFlowContext;
        @Nullable
        protected RunCommandFlowContext stopCommandFlowContext;
        protected String mqMonitorTerminateOnMatch = "";
        protected boolean executeMqMonitor;
        protected String mqMonitorInstallDir;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            initFlowContextBuilders();
        }

        protected void initFlowContextBuilders() {
            runCommandFlowContextBuilder = new RunCommandFlowContext.Builder("startMQMonitor.bat");
        }

        @Override
        public MQMonitorRole build() {
            initRunCommandFlow();
            initStopCommandFlow();

            MQMonitorRole mqMonitorRole = getInstance();
            Args.notNull(mqMonitorRole.runCommandFlowContext, "runCommandFlowContext");
            Args.notNull(mqMonitorRole.stopCommandFlowContext, "stopCommandFlowContext");

            return mqMonitorRole;
        }

        protected void initRunCommandFlow() {
            List<String> args = new ArrayList<>();
            args.add("-DROLEID=" + roleId);

            runCommandFlowContext =
                    runCommandFlowContextBuilder.args(args)
                            .workDir(mqMonitorInstallDir)
//                            .terminateOnMatch(mqMonitorTerminateOnMatch)
                            .terminateOnMatch("Processing MQMonitor.properties file")
                            .build();

            getEnvProperties().add(ENV_RUN_MQMONITOR, runCommandFlowContext);
        }

        protected void initStopCommandFlow() {
            stopCommandFlowContext =
                    new RunCommandFlowContext.Builder("wmic")
                            .args(
                                    Arrays.asList("PROCESS", "Where",
                                            "name like '%java%' and CommandLine like '%MqMonitor.jar%'", "Call", "Terminate"))
                            .doNotPrependWorkingDirectory().build();

            getEnvProperties().add(ENV_STOP_MQMONITOR, stopCommandFlowContext);
        }


        @Override
        protected MQMonitorRole getInstance() {
            return new MQMonitorRole(this);
        }


        public MQMonitorRole.Builder installDir(String installDir) {
            Args.notNull(installDir, "installDir");
            mqMonitorInstallDir = installDir;
            return builder();
        }

        public MQMonitorRole.Builder terminateOnMatch(String stringToMatch) {
            Args.notNull(stringToMatch, "stringToMatch");
            mqMonitorTerminateOnMatch = stringToMatch;
            return builder();
        }

        public MQMonitorRole.Builder executeMqMonitor() {
            executeMqMonitor = true;

            return builder();
        }

        @Override
        protected MQMonitorRole.Builder builder() {
            return this;
        }
    }
}