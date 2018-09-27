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

package com.ca.tas.role.testapp.custom;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.testapp.DeployNowhereBankBTFlowContext;
import com.ca.apm.automation.action.flow.testapp.DeployNowhereBankBTFlow;
import com.ca.apm.automation.action.flow.testapp.NowhereBankVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Deploys Nowherebank version 1.0.3, that has Business Transaction segments
 *
 * In 1.0.3, each agent has its own profile file and nowherebank can be deployed on linux also.
 *
 * Note: Developed primarily for linux. Will need some tweaking to work on Windows
 *
 */
public class NowhereBankBTRole extends AbstractRole {

    private static final int ASYNC_DELAY = 10;

    public static final String MESSAGING_SERVER_01 = "01_MessagingServer";
    public static final String BANKING_ENGINE_02 = "02_Banking-Engine-wily";
    public static final String BANKING_MEDIATOR_03 = "03_Banking-Mediator-wily";
    public static final String BANKING_PORTAL_04 = "04_Banking-Portal-wily";
    public static final String BANKING_GENERATOR_05 = "05_Banking-UI-Generator";
    public static final String STOP_ALL_COMPONENTS = "stopAllComponents";
    public static final String PROFILE_FILE_FORMATTER = "profileFileFormatter";
    public static final String INSTALL_DIR = "installDir";
    public static final String WINDOWS_CMD_EXTENSION = ".cmd";
    public static final String LINUX_CMD_EXTENSION = ".command";


    /** NowhereBank agent names */
    public static final String[] NWB_AGENT_NAMES = {"Engine", "Mediator", "Portal"};

    @NotNull
    private final DeployNowhereBankBTFlowContext flowContext;
    private final RunCommandFlowContext[] startCommands;

    /**
     * Sets up the NowhereBank role and defines its properties
     *
     * @param build Builder object containing all necessary data
     */
    private NowhereBankBTRole(Builder build, DeployNowhereBankBTFlowContext flowContext) {
        super(build.roleId, build.getEnvProperties());

        this.flowContext = flowContext;
        this.startCommands = build.startCommands;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // deploy
        FlowConfig.FlowConfigBuilder flowConfigBuilder =
            new FlowConfig.FlowConfigBuilder(DeployNowhereBankBTFlow.class, flowContext,
                getHostingMachine().getHostnameWithPort());

        aaClient.runJavaFlow(flowConfigBuilder);

        if(flowContext.isAutoStart() && startCommands!=null && startCommands.length > 0) {
            for(int i = 0; i< startCommands.length; i++) {
                FlowConfig.FlowConfigBuilder commandFlowConfigBuilder =
                    new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, startCommands[i],
                        getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY).async();

                aaClient.runJavaFlow(commandFlowConfigBuilder);
            }
        }
    }

    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            this.flowContextBuilder = new DeployNowhereBankBTFlowContext.LinuxBuilder(tasResolver);
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected String fetchCommandExtension() {
            return LINUX_CMD_EXTENSION;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link NowhereBankBTRole}
     */
    public static class Builder extends BuilderBase<Builder, NowhereBankBTRole> {

        private final String roleId;
        protected DeployNowhereBankBTFlowContext.Builder flowContextBuilder;

        private Map<String, String> files = new HashMap<>();
        private ArrayList<Map<String, String>> replacePairs = new ArrayList<Map<String, String>>();
        private DeployNowhereBankBTFlowContext flowContext;
        private RunCommandFlowContext[] startCommands = new RunCommandFlowContext[5];

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.flowContextBuilder = new DeployNowhereBankBTFlowContext.Builder(tasResolver);
        }

        public Builder nowhereBankVersion(@NotNull NowhereBankVersion nowhereBankVersion) {
            this.flowContextBuilder.nowhereBankVersion(nowhereBankVersion);
            return this;
        }

        public Builder installDir(@NotNull String installDir) {
            this.flowContextBuilder.installDir(new File(installDir));
            return this;
        }

        public Builder stagingBaseDir(@NotNull String stagingDir) {
            this.flowContextBuilder.stagingBaseDir(new File(stagingDir));
            return this;
        }

        public Builder noStart() {
            this.flowContextBuilder.noStart();
            return this;
        }

        private void initAndSaveCommandFlow() {
            int i = 0;
            for(String fileName : files.keySet()) {
                String executable = fileName + fetchCommandExtension();
                RunCommandFlowContext runCmdFlowContext =
                    new RunCommandFlowContext.Builder(executable).workDir(
                        flowContext.getInstallDir().getPath().replace("\\", "/")
                            + "/App").name(fileName)
                        .terminateOnMatch(files.get(fileName)).build();

                getEnvProperties().add(fileName, runCmdFlowContext);
                startCommands[i] = runCmdFlowContext;
                i++;
            }

            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder(DeployNowhereBankBTFlow.STOP_ALL)
                    .workDir(flowContext.getInstallDir().getPath().replace("\\", "/")
                    + "/App")
                .build();

            getEnvProperties().add(STOP_ALL_COMPONENTS, runCommandFlowContext);
        }

        protected String fetchCommandExtension() {
            return WINDOWS_CMD_EXTENSION;
        }

        /**
         * Builds instance of {@link NowhereBankBTRole}
         */
        @Override
        public NowhereBankBTRole build() {
            files.put(MESSAGING_SERVER_01, "Engine deployed");
            files.put(BANKING_ENGINE_02, "## Monitor Report ##");
            files.put(BANKING_MEDIATOR_03, "## Monitor Report ##");
            files.put(BANKING_PORTAL_04, "## Monitor Report ##");
            files.put(BANKING_GENERATOR_05, "Starting Test Cycle");
            flowContextBuilder.files(files);
            // Build flow context first to populate installDir
            flowContext = flowContextBuilder.build();
            String profileFileFormatter =
                getPathSeparator() + "App" + getPathSeparator() + "wily" + getPathSeparator() + "core" + getPathSeparator() + "config" + getPathSeparator() + "IntroscopeAgent-%s.profile";
            String installDir = flowContext.getInstallDir().getPath().replace("\\","/");
            getEnvProperties().add(PROFILE_FILE_FORMATTER, profileFileFormatter);
            getEnvProperties().add(INSTALL_DIR, installDir);
            initAndSaveCommandFlow();
            return new NowhereBankBTRole(this, flowContext);
        }

        @Override
        protected NowhereBankBTRole getInstance() {
            return build();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
