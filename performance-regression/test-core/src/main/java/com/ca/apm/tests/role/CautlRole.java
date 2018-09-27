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
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.CautlDistribution;
import com.ca.apm.tests.role.ExecCmdRole.ExecContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;

public class CautlRole extends AbstractRole {
    public static final String ENV_CAUTL_HOME = "home";
    public static final String ENV_CAUTL_START = "start";
    public static final String ENV_CAUTL_STOP = "stop";

    private final GenericFlowContext cautlContext;
    private final FileModifierFlowContext configFileContext;
    private ArrayList<RunCommandFlowContext> startCommandFlowContexts;
    private RunCommandFlowContext stopCommandFlowContext;

    protected CautlRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        cautlContext = builder.cautlContext;
        configFileContext = builder.configFileContext;
        startCommandFlowContexts = builder.startCommandFlowContexts;
        stopCommandFlowContext = builder.stopCommandFlowContext;
    }

    @Override
    public Map<String, String> getEnvProperties() {
        return properties;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {

        runFlow(client, GenericFlow.class, cautlContext);
        runFlow(client, FileModifierFlow.class, configFileContext);
    }

    public List<RunCommandFlowContext> getStartCommandFlowContexts() {
        return Collections.unmodifiableList(startCommandFlowContexts);
    }

    public RunCommandFlowContext getStopCommandFlowContext() {
        return stopCommandFlowContext;
    }

    public static class Builder extends BuilderBase<Builder, CautlRole> {
        protected String roleId;
        @Nullable
        protected GenericFlowContext cautlContext;
        protected FileModifierFlowContext configFileContext;
        protected ITasResolver tasResolver;
        protected ExecCmdRole cmdRole;
        protected ArrayList<RunCommandFlowContext> startCommandFlowContexts = new ArrayList<>();
        protected RunCommandFlowContext stopCommandFlowContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public CautlRole build() {
            String installPath = cmdRole.getWorkDirecotry() + getPathSeparator() + "cautl";
            getEnvProperties().add(ENV_CAUTL_HOME, installPath.toString());

            CautlDistribution artifact = new CautlDistribution();
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.getArtifact());

            cautlContext =
                    new GenericFlowContext.Builder().artifactUrl(artifactUrl).destination(installPath)
                            .build();

            Collection<String> configData =
                    Arrays.asList("[CONFIG]", "SetInternalVar=start=on", "",
                            "SetInternalVar=CAUTL_MAX_WAIT_FOR_WARNING=" + cmdRole.getRunDuration(),
                            "SetInternalVar=CAUTL_MAX_WAIT_FOR_KILL=" + cmdRole.getRunDuration(), "",
                            "[start]", "SetErrorMode=Off", "SetUseJobs=On",
                            "CreateProcessWait=%CAUTL_CMDLINE%", "ExitOnRCNot=0");
            String configFile = installPath + getPathSeparator() + roleId + "_config.ini";
            configFileContext =
                    new FileModifierFlowContext.Builder().create(configFile, configData).build();

            int idx = 0;
            for (ExecContext context : cmdRole.getExecContext()) {
                String cautlExe = installPath + getPathSeparator() + "CAUTL64.exe";

                Map<String, String> envs = new HashMap<>();
                envs.putAll(context.getEnvironment());
                envs.put("CAUTL_INIFILE", configFile);

                RunCommandFlowContext startCommandFlowContext =
                        new RunCommandFlowContext.Builder(cautlExe).workDir(cmdRole.getWorkDirecotry())
                                .args(context.getCommand()).environment(envs).name(roleId)
                                .doNotPrependWorkingDirectory().dontUseWindowsShell()
                                .terminateOnMatch(cmdRole.getOkStatus()).build();
                getEnvProperties().add(ENV_CAUTL_START + idx++, startCommandFlowContext);
                startCommandFlowContexts.add(startCommandFlowContext);
            }
            stopCommandFlowContext =
                    new RunCommandFlowContext.Builder("start")
                            .args(Arrays.asList("taskkill", "/F", "/T", "/IM", "CAUTL64.exe"))
                            .doNotPrependWorkingDirectory().name(roleId).build();
            getEnvProperties().add(ENV_CAUTL_STOP, stopCommandFlowContext);

            return getInstance();
        }

        @Override
        protected CautlRole getInstance() {
            return new CautlRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder executedRole(ExecCmdRole role) {
            this.cmdRole = role;
            return this;
        }
    }
}
