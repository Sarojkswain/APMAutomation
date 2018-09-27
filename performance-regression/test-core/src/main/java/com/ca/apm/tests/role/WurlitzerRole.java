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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.tests.artifact.WurlitzerArtifact;
import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.thirdParty.AntVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * WurlitzerRole class.
 *
 * Deploys Wurlitzer and starts a script according to the given parameters.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class WurlitzerRole extends AbstractRole {

//    private static final int UNPACK_TIMEOUT = 60;
    public static final String ENV_RUN_WURLITZER = "execWurlitzer";
    public static final String ENV_STOP_WURLITZER = "stopWurlitzer";
    public static final String WURLITZER_DESTINATION = "wurlitzerDestination";

    @Nullable
    private final GenericFlowContext antFlowContext;
    private final GenericFlowContext wurlitzerFlowContext;
    private final RunCommandFlowContext runCommandFlowContext;
    private final RunCommandFlowContext stopCommandFlowContext;
    private final boolean executeWurlitzer;

    /**
     * @param builder Builder object containing all necessary data
     */
    protected WurlitzerRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        antFlowContext = builder.antFlowContext;
        wurlitzerFlowContext = builder.wurlitzerFlowContext;
        runCommandFlowContext = builder.runCommandFlowContext;
        stopCommandFlowContext = builder.stopCommandFlowContext;
        executeWurlitzer = builder.execWurlitzer;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        installAnt(aaClient);
        installWurlitzer(aaClient);
        executeWurlitzer(aaClient);
    }

    protected void installAnt(IAutomationAgentClient aaClient) {
        if (antFlowContext == null) {
            return;
        }

        runFlow(aaClient, GenericFlow.class, antFlowContext);
    }

    protected void installWurlitzer(IAutomationAgentClient aaClient) {
        runFlow(aaClient, GenericFlow.class, wurlitzerFlowContext);
    }

    protected void executeWurlitzer(IAutomationAgentClient aaClient) {
        if (!executeWurlitzer) {
            return;
        }
        aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, runCommandFlowContext,
            getHostWithPort()));
    }

    @Nullable
    public GenericFlowContext getAntFlowContext() {
        return antFlowContext;
    }

    public RunCommandFlowContext getRunCommandFlowContext() {
        return runCommandFlowContext;
    }

    public RunCommandFlowContext getStopCommandFlowContext() {
        return stopCommandFlowContext;
    }

    public boolean isExecuteWurlitzer() {
        return executeWurlitzer;
    }

    public GenericFlowContext getWurlitzerFlowContext() {
        return wurlitzerFlowContext;
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link WurlitzerRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected LinuxBuilder builder() {
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

        protected void initStopCommandFlow() {
            stopCommandFlowContext =
                new RunCommandFlowContext.Builder("pkill")
                    .args(Arrays.asList("-f", "'-DROLEID=" + roleId + "'"))
                    .doNotPrependWorkingDirectory().build();

            getEnvProperties().add(ENV_STOP_WURLITZER, stopCommandFlowContext);
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link WurlitzerRole}
     */
    public static class Builder extends BuilderBase<Builder, WurlitzerRole> {

        protected final String roleId;
        protected final ITasResolver tasResolver;

        protected GenericFlowContext.Builder wurlitzerFlowContextBuilder;
        protected RunCommandFlowContext.Builder runCommandFlowContextBuilder;

        @Nullable
        protected GenericFlowContext.Builder antFlowContextBuilder;
        protected AntVersion antVersionToDeploy = AntVersion.v1_7_1;
        @Nullable
        protected GenericFlowContext antFlowContext;
        @Nullable
        protected GenericFlowContext wurlitzerFlowContext;
        @Nullable
        protected RunCommandFlowContext runCommandFlowContext;
        @Nullable
        protected RunCommandFlowContext stopCommandFlowContext;
        @Nullable
        protected String wurlitzerVersion;
        @Nullable
        protected String wurlitzerClassifier;
        @Nullable
        protected IArtifactExtension wurlitzerExtension;
        @Nullable
        protected Artifact wurlitzerArtifact;
        @Nullable
        protected String[] wurlitzerAntScriptPathSegments;
        @Nullable
        protected String[] wurlitzerAntScriptArgs;
        protected String wurlitzerTerminateOnMatch = "";
        protected boolean execWurlitzer;
        protected String wurlitzerInstallDir;
        private String wurlitzerMachine;
        private String targetMachine;
        private Integer wurlitzerRunDuration;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            initFlowContextBuilders();
        }

        protected void initFlowContextBuilders() {
            wurlitzerFlowContextBuilder = new GenericFlowContext.Builder();
            runCommandFlowContextBuilder = new RunCommandFlowContext.Builder("ant");
            antFlowContextBuilder = new GenericFlowContext.Builder();
        }

        @Override
        public WurlitzerRole build() {
            initAntFlow();
            initWurlitzerArtifact();
            initWurlitzerFlow();
            initRunCommandFlow();
            initStopCommandFlow();

            WurlitzerRole wurlitzerRole = getInstance();
            Args.notNull(wurlitzerRole.wurlitzerFlowContext, "wurlitzerFlowContext");
            Args.notNull(wurlitzerRole.runCommandFlowContext, "runCommandFlowContext");
            Args.notNull(wurlitzerRole.stopCommandFlowContext, "stopCommandFlowContext");

            return wurlitzerRole;
        }

        protected void initAntFlow() {
            URL artifactURL = tasResolver.getArtifactUrl(antVersionToDeploy);
            assert antFlowContextBuilder != null : "Ant flow context builder must be set for linux OS";

            antFlowContext =
                antFlowContextBuilder.artifactUrl(artifactURL)
                    .destination(getDeployBase() + TasFileUtils.getBasename(artifactURL)).build();
        }

        protected void initWurlitzerArtifact() {
            if (wurlitzerArtifact != null) {
                return;
            }
            if (wurlitzerClassifier != null || wurlitzerExtension != null) {
                wurlitzerArtifact =
                    new WurlitzerArtifact(tasResolver).createArtifact(wurlitzerVersion,
                        wurlitzerClassifier, wurlitzerExtension).getArtifact();
            } else {
                wurlitzerArtifact =
                    new WurlitzerArtifact(tasResolver).createArtifact(wurlitzerVersion)
                        .getArtifact();
            }
        }

        protected void initWurlitzerFlow() {
            URL artifactURL = tasResolver.getArtifactUrl(wurlitzerArtifact);
            String wurlitzerDestination =
                wurlitzerInstallDir != null ? wurlitzerInstallDir : getDeployBase()
                    + TasFileUtils.getBasename(artifactURL);
            wurlitzerFlowContext =
                wurlitzerFlowContextBuilder.artifactUrl(artifactURL)
                    .destination(wurlitzerDestination).build();

            getEnvProperties().add(WURLITZER_DESTINATION, wurlitzerDestination);
        }

        protected void initRunCommandFlow() {
            assert wurlitzerFlowContext != null;

            Map<String, String> env = new HashMap<>();
            env.put("WURLITZER_AGENT_HOSTNAME", wurlitzerMachine);

            List<String> args = new ArrayList<>();

            if (targetMachine != null) {
                args.add("-DWURLITZER.TARGET.EM.HOST=" + targetMachine);
            }
            if (wurlitzerRunDuration != null) {
                args.add("-DWURLITZER.SCENARIO.DURATION.MIN=" + wurlitzerRunDuration);
            }
            args.add("-DROLEID=" + roleId);
            args.add("-f");
            String[] extendedPathSegments =
                (String[]) ArrayUtils.add(wurlitzerAntScriptPathSegments, 0,
                    wurlitzerFlowContext.getDestination());
            args.add(concatPaths(extendedPathSegments));

            if (wurlitzerAntScriptArgs != null) {
                args.addAll(Arrays.asList(wurlitzerAntScriptArgs));
            }

            initRunCommandFlowWithAntWorkDir();

            runCommandFlowContext =
                runCommandFlowContextBuilder.args(args).environment(env)
                    .terminateOnMatch(wurlitzerTerminateOnMatch).build();

            getEnvProperties().add(ENV_RUN_WURLITZER, runCommandFlowContext);
        }

        protected void initStopCommandFlow() {
            stopCommandFlowContext =
                new RunCommandFlowContext.Builder("wmic")
                    .args(
                        Arrays.asList("Path", "win32_process", "Where",
                            "CommandLine Like '%-DROLEID " + roleId + "%'", "Call", "Terminate"))
                    .doNotPrependWorkingDirectory().build();

            getEnvProperties().add(ENV_STOP_WURLITZER, stopCommandFlowContext);
        }

        protected void initRunCommandFlowWithAntWorkDir() {
            assert antFlowContext != null : "Ant flow context";

            runCommandFlowContextBuilder
                .workDir(concatPaths(antFlowContext.getDestination(), "bin"));
        }


        @Override
        protected WurlitzerRole getInstance() {
            return new WurlitzerRole(this);
        }

        public Builder version(IArtifactVersion version) {
            Args.notNull(version, "version");
            wurlitzerVersion = version.toString();
            return builder();
        }

        public Builder version(String version) {
            Args.notNull(version, "version");
            wurlitzerVersion = version;
            return builder();
        }

        public Builder version(String version, String classifier, IArtifactExtension extension) {
            Args.notNull(version, "version");
            wurlitzerVersion = version;
            wurlitzerClassifier = classifier;
            wurlitzerExtension = extension;

            execWurlitzer = false;

            return builder();
        }

        public Builder installDir(String installDir) {
            Args.notNull(installDir, "installDir");
            wurlitzerInstallDir = installDir;
            return builder();
        }

        public Builder antVersionToDeploy(AntVersion antVersionToDeploy) {
            Args.notNull(antVersionToDeploy, "antVersionToDeploy");
            this.antVersionToDeploy = antVersionToDeploy;

            return builder();
        }

        public Builder antScriptPathSegments(String... antScriptPathSegments) {
            Args.notNull(antScriptPathSegments, "antScriptPathSegments");
            wurlitzerAntScriptPathSegments = antScriptPathSegments;
            return builder();
        }

        public Builder antScriptArgs(String... antScriptArgs) {
            Args.notNull(antScriptArgs, "antScriptArgs");
            wurlitzerAntScriptArgs = antScriptArgs;
            return builder();
        }

        public Builder terminateOnMatch(String stringToMatch) {
            Args.notNull(stringToMatch, "stringToMatch");
            wurlitzerTerminateOnMatch = stringToMatch;
            return builder();
        }

        public Builder wurlitzerMachine(String machine) {
            Args.notNull(machine, "wurlitzer machine");
            wurlitzerMachine = machine;
            return builder();
        }

        public Builder targetMachine(String machine) {
            Args.notNull(machine, "target machine");
            targetMachine = machine;
            return builder();
        }

        public Builder runDuration(int minutes) {
            wurlitzerRunDuration = minutes;

            return builder();
        }

        public Builder executeWurlitzer() {
            execWurlitzer = true;

            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
