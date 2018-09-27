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

package com.ca.apm.nextgen.role;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Min;

import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.nextgen.role.artifacts.EmClient14Artifact;
import com.ca.apm.nextgen.role.artifacts.EmClientArtifact;
import com.ca.apm.nextgen.role.artifacts.HVRAgentArtifact;
import com.ca.apm.nextgen.role.artifacts.IntroscopeCommonArtifact;
import com.ca.apm.nextgen.role.artifacts.IsengardClientArtifact;
import com.ca.apm.nextgen.role.artifacts.JDBCFEatureArtifact;
import com.ca.apm.nextgen.role.artifacts.WilyCoreArtifact;
import com.ca.apm.nextgen.role.artifacts.Win32HelperAgentArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class HVRAgentRole extends AbstractRole {

    public enum Mode {
        REPLAY, EXTRACT
    }

    public static final String HVR_LAUNCH_COMMAND = "hvrLaunchCommand";

    public static final String HVR_STOP_COMMAND = "hvrStopCommand";

    // private static final Logger LOGGER = LoggerFactory.getLogger(EmRole.class);
    private static final int ASYNC_DELAY = 90;

    @Nullable
    protected String win32HelperAgentJar;

    @Nullable
    private UniversalFlowContext hvrAgentflowContext;

    @Nullable
    private RunCommandFlowContext runHvrAgentFlowContext;

    @Nullable
    private RunCommandFlowContext stopHvrAgentFlowContext;

    @Nullable
    private UniversalFlowContext libraryArtifactsFlowsCtxs;
    private boolean start;

    public HVRAgentRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        hvrAgentflowContext = builder.hvrAgentFlowContext;
        libraryArtifactsFlowsCtxs = builder.libraryArtifactsFlowsCtxs;
        runHvrAgentFlowContext = builder.runHvrAgentFlowContext;
        stopHvrAgentFlowContext = builder.stopHvrAgentFlowContext;
        win32HelperAgentJar = builder.win32HelperAgentJar;

        start = builder.start;
    }

    @Nullable
    public RunCommandFlowContext getRunHvrAgentFlowContext() {
        return runHvrAgentFlowContext;
    }

    @Nullable
    public RunCommandFlowContext getStopHvrAgentFlowContext() {
        return stopHvrAgentFlowContext;
    }

    @Nullable
    public String getWin32HelperAgentJar() {
        return win32HelperAgentJar;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        runFlow(aaClient, UniversalFlow.class, hvrAgentflowContext);
        runFlow(aaClient, UniversalFlow.class, libraryArtifactsFlowsCtxs);

        addProperty(HVR_LAUNCH_COMMAND, runHvrAgentFlowContext);
        if (start) {
            runCommandFlow(aaClient, runHvrAgentFlowContext);
        }
    }


    public static class Builder extends BuilderBase<Builder, HVRAgentRole> {
        public static final String LIB_PATH = File.separator + "lib" + File.separator;

        protected final String roleId;
        protected final ITasResolver tasResolver;

        protected boolean start = false;

        private int javaMaxHeap = 1024;
        private String host = "localhost";
        private String port = "5001";
        private String user = "Admin";
        private String password = "\"\"";
        private String loadfile = "extract";
        private int cloneconnections = 1;
        private int cloneagents = 1;
        private int secondspertrace = 15;
        private String agenthost = null;

        private boolean hideConsole = false;

        private Mode mode = Mode.REPLAY;

        @Nullable
        private String deployPath;

        @Nullable
        protected UniversalFlowContext hvrAgentFlowContext;

        @Nullable
        protected String win32HelperAgentJar;

        @Nullable
        protected UniversalFlowContext libraryArtifactsFlowsCtxs;

        @Nullable
        protected Set<Artifact> metricsArtifacts = new HashSet<>(1);

        @Nullable
        protected RunCommandFlowContext runHvrAgentFlowContext;

        @Nullable
        protected RunCommandFlowContext stopHvrAgentFlowContext;

        protected String version;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public HVRAgentRole build() {
            // must be called first
            initFlowContextBuilder();
            initLibArtifacts();
            initRunFlowContext();
            return new HVRAgentRole(this);
        }

        public Builder version(String version) {
            this.version = version;
            return builder();
        }

        private void initRunFlowContext() {
            UUID hvrInstanceUuid = UUID.randomUUID();
            String hvrInstanceUuidStr = hvrInstanceUuid.toString();

            Collection<String> args = new ArrayList<>(
                Arrays.asList(
                    "java",
                    String.format("-Xmx%dm", javaMaxHeap),
                    "-javaagent:" + win32HelperAgentJar,
                    "-Dwin32.console.title=" + hvrInstanceUuidStr + " ",
                    "-Dwin32.console.hide=" + hideConsole,
                    "-classpath",
                    "HVRAgent.jar;lib\\*",
                    "com.wily.introscope.tools.fakeagent.FakeAgent",
                    mode.name().toLowerCase(),
                    "-host", host,
                    "-port", port,
                    "-username", user,
                    "-password", password,
                    "-loadfile", loadfile,
                    "-cloneconnections", Integer.toString(cloneconnections),
                    "-cloneagents", Integer.toString(cloneagents),
                    "-secondspertrace", Integer.toString(secondspertrace))
            );

            if (StringUtils.isNotBlank(agenthost)) {
                args.add("-agenthost");
                args.add(agenthost);
            }

            runHvrAgentFlowContext =
                new RunCommandFlowContext.Builder("start")
                    .doNotPrependWorkingDirectory()
                    .workDir(deployPath)
                    .args(args)
                    .build();
            getEnvProperties().add(HVR_LAUNCH_COMMAND, runHvrAgentFlowContext);

            stopHvrAgentFlowContext =
                new RunCommandFlowContext.Builder("taskkill")
                    .doNotPrependWorkingDirectory()
                    .workDir(deployPath)
                    .args(Arrays.asList(
                        "/F",
                        "/T",
                        "/FI",
                        "WINDOWTITLE eq " + hvrInstanceUuidStr + "*"))
                    .build();
            getEnvProperties().add(HVR_STOP_COMMAND, stopHvrAgentFlowContext);
        }

        private void initFlowContextBuilder() {
            Artifact hvrAgentArtifact =
                new HVRAgentArtifact(tasResolver).createArtifact(version).getArtifact();
            URL artifactURL = tasResolver.getArtifactUrl(hvrAgentArtifact);
            deployPath = getDeployBase() + TasFileUtils.getBasename(artifactURL);
            hvrAgentFlowContext =
                new UniversalFlowContext.Builder().archive(artifactURL, deployPath).build();
        }

        private void addArtifact(UniversalFlowContext.Builder builder, String pth,
            Artifact artifact) {
            builder.artifact(tasResolver.getArtifactUrl(artifact),
                builder.concatPaths(pth,
                    artifact.getArtifactId().concat(".").concat(artifact.getExtension())));
        }


        private void initLibArtifacts() {
            // deploys jars fro classpath into lib folder
            String pth = deployPath + LIB_PATH;

            UniversalFlowContext.Builder builder = new UniversalFlowContext.Builder();

            Artifact artifact =
                new WilyCoreArtifact(tasResolver).createArtifact(version).getArtifact();
            addArtifact(builder, pth, artifact);

            artifact =
                new IntroscopeCommonArtifact(tasResolver).createArtifact(version).getArtifact();
            addArtifact(builder, pth, artifact);

            artifact = new EmClientArtifact(tasResolver).createArtifact(version).getArtifact();
            addArtifact(builder, pth, artifact);

            artifact = new EmClient14Artifact(tasResolver).createArtifact(version).getArtifact();
            addArtifact(builder, pth, artifact);

            artifact =
                new IsengardClientArtifact(tasResolver).createArtifact(version).getArtifact();
            addArtifact(builder, pth, artifact);

            artifact = new JDBCFEatureArtifact(tasResolver).createArtifact(version).getArtifact();
            addArtifact(builder, pth, artifact);

            // metrics artifacts are on root of HVRAgent deploy
            // also its supposed that all metrics are in .zip archives
            for (Artifact a : metricsArtifacts) {
                builder.archive(tasResolver.getArtifactUrl(a), deployPath);
            }

            artifact = new Win32HelperAgentArtifact(tasResolver).createArtifact(version).getArtifact();
            win32HelperAgentJar = builder.concatPaths(pth,
                artifact.getArtifactId().concat(".").concat(artifact.getExtension()));
            builder.artifact(tasResolver.getArtifactUrl(artifact), win32HelperAgentJar);

            libraryArtifactsFlowsCtxs = builder.build();
        }

        public Builder start() {
            start = true;
            return builder();
        }

        public Builder addMetricsArtifact(ITasArtifactFactory metricsArtifact) {
            Artifact artifact = metricsArtifact.createArtifact().getArtifact();
            metricsArtifacts.add(artifact);
            return builder();
        }

        public Builder emHost(String host) {
            this.host = host;
            return builder();
        }

        public Builder emPort(String port) {
            this.port = port;
            return builder();
        }

        public Builder user(String user) {
            this.user = user;
            return builder();
        }

        public Builder loadFile(String filename) {
            this.loadfile = filename;
            return builder();
        }

        public Builder agentHost(String agenthost) {
            this.agenthost = agenthost;
            return builder();
        }

        public Builder cloneconnections(@Min(value = 1) int cloneconnections) {
            this.cloneconnections = cloneconnections;
            return builder();
        }

        public Builder cloneagents(@Min(value = 1) int cloneagents) {
            this.cloneagents = cloneagents;
            return builder();
        }

        public Builder secondspertrace(@Min(value = 1) int secondspertrace) {
            this.secondspertrace = secondspertrace;
            return builder();
        }

        public Builder hideConsole() {
            this.hideConsole = true;
            return this;
        }

        /**
         * @param maxHeap Maximum heap in MiB for Java process.
         * @return Builder
         */
        public Builder maxHeap(@Min(value = 1) int maxHeap) {
            this.javaMaxHeap = maxHeap;
            return builder();
        }

        public Builder extract() {
            this.mode = Mode.EXTRACT;
            return builder();
        }

        public Builder replay() {
            this.mode = Mode.REPLAY;
            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected HVRAgentRole getInstance() {
            return new HVRAgentRole(this);
        }

    }

}
