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
package com.ca.apm.test.em.metadata.hammond;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.apm.test.em.metadata.hammond.HammondArtifact.Data;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class HammondRole extends AbstractRole {
    public static final String ENV_HAMMOND_HOME = "home";
    public static final String ENV_HAMMOND_START = "startCommand";
    public static final String ENV_HAMMOND_STOP = "stopCommand";
    public static final String ENV_HAMMOND_DATASETS = "datasets";

    private final GenericFlowContext hammondInstallContext;
    private ArrayList<RunCommandFlowContext> startCommandFlowContexts;
    private ArrayList<RunCommandFlowContext> stopCommandFlowContexts;
    private UniversalFlowContext hammondDataContexts;

    public enum Player {
        SMARTSTORE("com.ca.apm.systemtest.fld.hammond.SmartstorPlayer"), SYNTHETIC(
            "com.ca.apm.systemtest.fld.hammond.SyntheticPlayer");

        public String playerClass;

        Player(String playerClass) {
            this.playerClass = playerClass;
        }
    }

    protected HammondRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        hammondInstallContext = builder.hammondInstallContext;
        startCommandFlowContexts = builder.startCommandFlowContexts;
        stopCommandFlowContexts = builder.stopCommandFlowContexts;
        hammondDataContexts = builder.hammondDataContexts;
    }

    public ArrayList<RunCommandFlowContext> getStartCommandFlowContexts() {
        return startCommandFlowContexts;
    }

    public ArrayList<RunCommandFlowContext> getStopCommandFlowContexts() {
        return stopCommandFlowContexts;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, GenericFlow.class, hammondInstallContext);
        runFlow(client, UniversalFlow.class, hammondDataContexts);

    }

    public static class Builder extends BuilderBase<Builder, HammondRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected GenericFlowContext hammondInstallContext;
        @Nullable
        protected UniversalFlowContext hammondDataContexts;
        @Nullable
        protected ArrayList<RunCommandFlowContext> startCommandFlowContexts = new ArrayList<>();
        @Nullable
        protected ArrayList<RunCommandFlowContext> stopCommandFlowContexts = new ArrayList<>();

        protected String deployBase = getWinDeployBase();
        protected String installPath;
        protected String collectorHost;
        protected Integer runDuration;
        protected Double scale;
        protected String prefix;
        protected String included;
        protected String excluded;
        protected String heapMemory = "4g";
        protected Data dataArtifacts;

        protected Integer rotationScale;
        protected Integer rotationInterval;

        protected Integer agentCount;
        protected Integer metricCount;

        protected Player player = Player.SMARTSTORE;

        public class LinuxBuilder extends Builder {
            public LinuxBuilder(String roleId, ITasResolver tasResolver) {
                super(roleId, tasResolver);
            }

            @Override
            protected String getPathSeparator() {
                return LINUX_SEPARATOR;
            }

            @Override
            protected String getJavaCmd() {
                return concatPaths(getLinuxJavaBase(), "bin", "java");
            }

            @Override
            protected LinuxBuilder builder() {
                return this;
            }
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public HammondRole build() {
            installPath = deployBase + "hammond";
            getEnvProperties().add(ENV_HAMMOND_HOME, installPath.toString());

            HammondArtifact artifact = new HammondArtifact(tasResolver);
            ITasArtifact tasArtifact = artifact.createArtifact();
            URL artifactUrl = tasResolver.getArtifactUrl(tasArtifact);

            String hammondFileName =
                tasArtifact.getArtifact().getArtifactId() + "."
                    + tasArtifact.getArtifact().getExtension();

            hammondInstallContext =
                new GenericFlowContext.Builder().artifactUrl(artifactUrl).destination(installPath)
                    .notArchive().targetFilename(hammondFileName).build();

            UniversalFlowContext.Builder dataBuilder = new UniversalFlowContext.Builder();



            Args.notNull(collectorHost, "collectorHost");

            List<String> args = new ArrayList<>();
            args.add("-cp");
            args.add(concatPaths(installPath, hammondFileName));
            args.add("-Xmx" + heapMemory);
            args.add("-XX:+UseConcMarkSweepGC");
            args.add("-XX:NewRatio=2");
            args.add("-XX:+UseParNewGC");
            args.add("-XX:MaxHeapFreeRatio=30");
            args.add("-XX:MinHeapFreeRatio=10");
            args.add("-DROLEID=" + roleId);
            args.add(player.playerClass);


            if (Player.SMARTSTORE.equals(player)) {
                Args.notNull(dataArtifacts, "dataArtifacts");
                String dataFolder = concatPaths(installPath, dataArtifacts.name());
                Artifact dataArtifact = dataArtifacts.getArtifact();
                URL dataArtifactUrl = tasResolver.getArtifactUrl(dataArtifact);
                dataBuilder.archive(dataArtifactUrl, installPath, dataArtifacts.name());

                args.add("-i");
                args.add(dataFolder);
            }

            args.add("-c");
            args.add(collectorHost);

            if (scale != null) {
                args.add("-s");
                args.add(scale.toString());
            }
            if (runDuration != null) {
                args.add("-d");
                args.add(runDuration.toString());
            }
            if (prefix != null) {
                args.add("-p");
                args.add(prefix);
            }
            if (included != null) {
                args.add("-inc");
                args.add(included);
            }
            if (excluded != null) {
                args.add("-exc");
                args.add(excluded);
            }
            if (rotationInterval != null) {
                args.add("-rt");
                args.add(rotationInterval.toString());
            }
            if (rotationScale != null) {
                args.add("-r");
                args.add(rotationScale.toString());
            }

            if (agentCount != null) {
                args.add("-a");
                args.add(agentCount.toString());
            }

            if (metricCount != null) {
                args.add("-m");
                args.add(metricCount.toString());
            }



            RunCommandFlowContext startCommandFlowContext =
                new RunCommandFlowContext.Builder("java").args(args).workDir(deployBase)
                    .doNotPrependWorkingDirectory().name(roleId)
                    .terminateOnMatch("[AgentReporter] Sending").build();
            startCommandFlowContexts.add(startCommandFlowContext);
            getEnvProperties().add(ENV_HAMMOND_START, startCommandFlowContext);

            RunCommandFlowContext stopCommandFlowContext =
                new RunCommandFlowContext.Builder("wmic")
                    .args(
                        Arrays.asList("Path", "win32_process", "Where",
                            "CommandLine Like '%-DROLEID=" + roleId + "%'", "Call", "Terminate"))
                    .doNotPrependWorkingDirectory().build();
            stopCommandFlowContexts.add(startCommandFlowContext);
            getEnvProperties().add(ENV_HAMMOND_STOP, stopCommandFlowContext);


            hammondDataContexts = dataBuilder.build();

            getEnvProperties().add(ENV_HAMMOND_DATASETS, Integer.toString(1));

            return getInstance();
        }

        @Override
        protected HammondRole getInstance() {
            return new HammondRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        protected String getJavaCmd() {
            return concatPaths(getWinJavaBase(), "bin", "java.exe");
        }

        public Builder installDir(String path) {
            Args.notBlank(path, "install dir");
            this.deployBase = path;
            return builder();
        }

        public Builder runDuration(int seconds) {
            Args.positive(seconds, "run duration");
            runDuration = seconds;
            return builder();
        }

        public Builder rotationScale(int rotationScale) {
            this.rotationScale = rotationScale;
            return builder();
        }

        public Builder rotationInterval(int rotationInterval) {
            this.rotationInterval = rotationInterval;
            return builder();
        }

        public Builder collector(String machine) {
            Args.notNull(machine, "collector machine name");
            this.collectorHost = machine;
            return builder();
        }

        public Builder scale(double scale) {
            Args.check(scale > 0, "load scale");
            this.scale = scale;
            return builder();
        }

        public Builder player(Player player) {
            this.player = player;
            return builder();
        }

        public Builder agentCount(Integer agentCount) {
            this.agentCount = agentCount;
            return builder();
        }

        public Builder metricCount(Integer metricCount) {
            this.metricCount = metricCount;
            return builder();
        }

        public Builder prefix(String prefix) {
            Args.notNull(prefix, "prefix");
            this.prefix = prefix;
            return builder();
        }

        public Builder included(@NotNull String... strings) {
            Args.notEmpty(Arrays.asList(strings), "included metrics");
            if (strings.length > 0) {
                included += strings[0];
            }
            if (strings.length > 1) {
                for (int i = 1; i < strings.length; i++) {

                    included += ",";
                    included += strings[i];
                }
            }
            return builder();
        }

        public Builder excluded(String... strings) {
            Args.notEmpty(Arrays.asList(strings), "excluded metrics");
            if (strings.length > 0) {
                excluded += strings[0];
            }
            if (strings.length > 1) {
                for (int i = 1; i < strings.length; i++) {

                    excluded += ",";
                    excluded += strings[i];
                }
            }
            return builder();
        }

        public Builder heapMemory(String memory) {
            Args.notBlank(memory, "heap memory");
            this.heapMemory = memory;
            return builder();
        }

        public Builder data(Data data) {
            this.dataArtifacts = data;
            return builder();
        }
    }
}
