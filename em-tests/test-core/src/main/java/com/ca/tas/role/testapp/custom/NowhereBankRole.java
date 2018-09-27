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
import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.testapp.DeployNowhereBankFlow;
import com.ca.apm.automation.action.flow.testapp.DeployNowhereBankFlowContext;
import com.ca.apm.automation.action.flow.testapp.NowhereBankVersion;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map;

/**
 * This role represents NowhereBank application for testing purposes .
 * 
 * Role is immutable and is designed to be instantiated via Builder attached to the class.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 */
public class NowhereBankRole extends AbstractRole {

    private static final int ASYNC_DELAY = 10;

    @NotNull
    private final DeployNowhereBankFlowContext flowContext;
    private final ArrayList<String> commands;
    private final ArrayList<String> terminationTexts;
    private String duplicateWilyDir;
    private ArrayList<String> files;
    private ArrayList<Map<String, String>> replacePairs;

    /**
     * Sets up the NowhereBank role and defines its properties
     * 
     * @param build Builder object containing all necessary data
     */
    private NowhereBankRole(Builder build, DeployNowhereBankFlowContext flowContext) {
        super(build.roleId);

        this.flowContext = flowContext;
        this.commands = build.commands;
        this.terminationTexts = build.terminationTexts;
        this.duplicateWilyDir = build.duplicateWilyDir;
        this.files = build.files;
        this.replacePairs = build.replacePairs;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // deploy
        FlowConfig.FlowConfigBuilder flowConfigBuilder =
            new FlowConfig.FlowConfigBuilder(DeployNowhereBankFlow.class, flowContext,
                getHostingMachine().getHostnameWithPort());

        aaClient.runJavaFlow(flowConfigBuilder);

        if (duplicateWilyDir != null) {
            FileModifierFlowContext.Builder copyBuilder = new FileModifierFlowContext.Builder();
            copyBuilder.copy(flowContext.getInstallDir() + "\\wily", flowContext.getInstallDir()
                + "\\" + duplicateWilyDir);
            FileModifierFlowContext copyContext = copyBuilder.build();
            runFlow(aaClient, FileModifierFlow.class, copyContext);
        }

        // file modification
        if (!files.isEmpty())
        {
            FileModifierFlowContext.Builder modifier = new FileModifierFlowContext.Builder();
            for (int i = 0; i < files.size(); ++i) {
                modifier.replace(flowContext.getInstallDir() + "\\" + files.get(i),
                    replacePairs.get(i));
            }
            FileModifierFlowContext fileModifierContext = modifier.build();
            runFlow(aaClient, FileModifierFlow.class, fileModifierContext);
        }

        // command execution
        File workDir = flowContext.getInstallDir();
        for (int i = 0; i < commands.size(); ++i) {
            RunCommandFlowContext runCmdFlowContext =
                new RunCommandFlowContext.Builder(commands.get(i))
                    .workDir(workDir.getPath())
                    .name(getRoleId())
                    .terminateOnMatch(terminationTexts.get(i))
                    .build();

            FlowConfig.FlowConfigBuilder commandFlowConfigBuilder =
                new FlowConfig.FlowConfigBuilder(RunCommandFlow.class, runCmdFlowContext,
                    getHostingMachine().getHostnameWithPort()).delay(ASYNC_DELAY).async();

            // runCommandFlowAsync(aaClient, runCmdFlowContext, ASYNC_DELAY);
            aaClient.runJavaFlow(commandFlowConfigBuilder);
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link NowhereBankRole}
     */
    public static class Builder extends BuilderBase<Builder, NowhereBankRole> {

        private final String roleId;
        private final DeployNowhereBankFlowContext.Builder flowContextBuilder;

        private ArrayList<String> commands = new ArrayList<String>();
        private ArrayList<String> terminationTexts = new ArrayList<String>();

        private String duplicateWilyDir = null;

        private ArrayList<String> files = new ArrayList<String>();
        private ArrayList<Map<String, String>> replacePairs = new ArrayList<Map<String, String>>();
 
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.flowContextBuilder = new DeployNowhereBankFlowContext.Builder(tasResolver);
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

        public Builder copyWilyTo(String duplicateWilyDir) {
            this.duplicateWilyDir = duplicateWilyDir;
            return this;
        }

        public Builder command(@NotNull String command, @NotNull String terminationText) {
            this.commands.add(command);
            this.terminationTexts.add(terminationText);
            return this;
        }

        public Builder replaceContent(@NotNull String file, @NotNull Map<String, String> replacePair) {
            this.files.add(file);
            this.replacePairs.add(replacePair);
            return this;
        }

        @Override
        protected NowhereBankRole getInstance() {
            return new NowhereBankRole(this, flowContextBuilder.build());
        }

        @Override
        protected Builder builder() {
            return this;
        }

        /**
         * Builds instance of {@link NowhereBankRole}
         */
        @Override
        public NowhereBankRole build() {
            return getInstance();
        }
    }
}
