package com.ca.apm.systemtest.alertstateload.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.apm.systemtest.alertstateload.artifact.thirdparty.HammondDataVersion;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.built.HammondArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class AlertStateLoadHammondRole extends AbstractRole {

    public static final String ENV_HAMMOND_START = "startCommand";
    public static final String ENV_HAMMOND_STOP = "stopCommand";

    private final GenericFlowContext hammondInstallContext;
    private UniversalFlowContext hammondDataContext;
    private String killScriptFile;

    private enum Player {
        SMARTSTORE("com.ca.apm.systemtest.fld.hammond.SmartstorPlayer"), SYNTHETIC(
            "com.ca.apm.systemtest.fld.hammond.SyntheticPlayer");

        private String playerClass;

        private Player(String playerClass) {
            this.playerClass = playerClass;
        }
    }

    protected AlertStateLoadHammondRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.hammondInstallContext = builder.hammondInstallContext;
        this.hammondDataContext = builder.hammondDataContext;
        this.killScriptFile = builder.killScriptFile;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, GenericFlow.class, hammondInstallContext);
        runFlow(client, UniversalFlow.class, hammondDataContext);
        if (killScriptFile != null) {
            createKillScript(client);
        }
    }

    private void createKillScript(IAutomationAgentClient client) {
        Collection<String> lines =
            Arrays.asList("#!/bin/bash",
                "PID=`ps aux | fgrep -v 'grep' | fgrep -i java | fgrep -i hammond | fgrep -i 'DROLEID="
                    + getRoleId() + "' | awk 'NR==1 {print $2}'`", "if [ $PID ]; then",
                "  kill $PID", "fi");
        FileModifierFlowContext killSh =
            new FileModifierFlowContext.Builder().create(killScriptFile, lines).build();
        runFlow(client, FileModifierFlow.class, killSh);

        RunCommandFlowContext chmodRunCommandFlowContext =
            new RunCommandFlowContext.Builder("chmod").args(Arrays.asList("u+x", killScriptFile))
                .doNotPrependWorkingDirectory().build();
        runFlow(client, RunCommandFlow.class, chmodRunCommandFlowContext);
    }

    public static class LinuxBuilder extends Builder {
        private static final String KILL_HAMMOND_SCRIPT = "killHammond.sh";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            deployBase = getLinuxDeployBase();
            installPath = concatPaths(deployBase, "hammond", roleId);
            killScriptFile = concatPaths(installPath, KILL_HAMMOND_SCRIPT);
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected RunCommandFlowContext getStopCommandFlowContext() {
            RunCommandFlowContext stopCommandFlowContext =
                new RunCommandFlowContext.Builder(KILL_HAMMOND_SCRIPT).workDir(installPath).build();
            return stopCommandFlowContext;
        }
    }

    public static class Builder extends BuilderBase<Builder, AlertStateLoadHammondRole> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected GenericFlowContext hammondInstallContext;
        @Nullable
        protected UniversalFlowContext hammondDataContext;

        protected String deployBase;
        protected String installPath;
        protected String collectorHost;
        protected Integer runDuration;
        protected Double scale;
        protected String prefix;
        protected String included;
        protected String excluded;
        protected String heapMemory = "4g";
        protected HammondDataVersion dataArtifacts = HammondDataVersion.AlertStatusLoad;
        protected Integer rotationScale;
        protected Integer rotationInterval;
        protected Integer metricCount;
        protected Long from;
        protected Long to;

        protected String killScriptFile = null;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            deployBase = getWinDeployBase();
            installPath = concatPaths(deployBase, "hammond", roleId);
        }

        @Override
        public AlertStateLoadHammondRole build() {
            HammondArtifact hammondArtifact = new HammondArtifact(tasResolver);
            ITasArtifact hammondTasArtifact = hammondArtifact.createArtifact();
            URL hammondArtifactUrl = tasResolver.getArtifactUrl(hammondTasArtifact);

            String hammondFileName =
                hammondTasArtifact.getArtifact().getArtifactId() + "."
                    + hammondTasArtifact.getArtifact().getExtension();

            hammondInstallContext =
                (new GenericFlowContext.Builder()).artifactUrl(hammondArtifactUrl)
                    .destination(installPath).notArchive().targetFilename(hammondFileName).build();

            // Args.notNull(dataArtifacts, "dataArtifacts");
            String dataFolder = concatPaths(installPath, dataArtifacts.name());
            Artifact dataArtifact = dataArtifacts.getArtifact();
            URL dataArtifactUrl = tasResolver.getArtifactUrl(dataArtifact);

            UniversalFlowContext.Builder dataBuilder = new UniversalFlowContext.Builder();
            dataBuilder.archive(dataArtifactUrl, installPath, dataArtifacts.name());
            hammondDataContext = dataBuilder.build();

            Args.notNull(collectorHost, "collectorHost");

            List<String> args = getArgs(hammondFileName, dataFolder);
            getEnvProperties().add(ENV_HAMMOND_START, getStartCommandFlowContext(args));
            getEnvProperties().add(ENV_HAMMOND_STOP, getStopCommandFlowContext());
            return getInstance();
        }

        protected List<String> getArgs(String hammondFileName, String dataFolder) {
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
            args.add(Player.SMARTSTORE.playerClass);

            args.add("-i");
            args.add(dataFolder);

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
            if (metricCount != null) {
                args.add("-m");
                args.add(metricCount.toString());
            }
            if (from != null) {
                args.add("-f");
                args.add(from.toString());
            }
            if (to != null) {
                args.add("-t");
                args.add(to.toString());
            }
            return args;
        }

        protected RunCommandFlowContext getStartCommandFlowContext(List<String> args) {
            RunCommandFlowContext startCommandFlowContext =
                (new RunCommandFlowContext.Builder("java")).args(args)
                // .workDir(deployBase)
                    .doNotPrependWorkingDirectory()
                    // .name(roleId)
                    .terminateOnMatch("[AgentReporter] Sending").build();
            return startCommandFlowContext;
        }

        protected RunCommandFlowContext getStopCommandFlowContext() {
            RunCommandFlowContext stopCommandFlowContext =
                (new RunCommandFlowContext.Builder("wmic"))
                    .args(
                        Arrays.asList("Path", "win32_process", "Where",
                            "CommandLine Like '%-DROLEID=" + roleId + "%'", "Call", "Terminate"))
                    .doNotPrependWorkingDirectory().build();
            return stopCommandFlowContext;
        }

        @Override
        protected AlertStateLoadHammondRole getInstance() {
            return new AlertStateLoadHammondRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        // public Builder installDir(String path) {
        // Args.notBlank(path, "install dir");
        // deployBase = path;
        // installPath = concatPaths(deployBase, "hammond", roleId);
        // return builder();
        // }

        public Builder runDuration(int seconds) {
            Args.positive(seconds, "playback duration (s)");
            runDuration = seconds;
            return builder();
        }

        public Builder rotationScale(int rotationScale) {
            Args.notNegative(rotationScale, "rotate metrics in agents");
            this.rotationScale = rotationScale;
            return builder();
        }

        public Builder rotationInterval(int rotationInterval) {
            Args.notNegative(rotationInterval, "time for metrics rotation in minutes");
            this.rotationInterval = rotationInterval;
            return builder();
        }

        public Builder collector(String collectorHost) {
            Args.notNull(collectorHost, "collector host name (without domain)");
            this.collectorHost = collectorHost;
            return builder();
        }

        public Builder scale(double scale) {
            Args.check(scale > 0, "agent scaling ratio (double)");
            this.scale = scale;
            return builder();
        }

        public Builder metricCount(Integer metricCount) {
            Args.notNegative(rotationScale, "metric count");
            this.metricCount = metricCount;
            return builder();
        }

        public Builder prefix(String prefix) {
            Args.notNull(prefix, "prefix added to generated agent name");
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

        public Builder from(long from) {
            Args.notNegative(from, "read data from timestamp (millis)");
            this.from = from;
            return builder();
        }

        public Builder to(long to) {
            Args.notNegative(from, "read data to timestamp (millis)");
            this.to = to;
            return builder();
        }

        // public Builder data(HammondDataVersion data) {
        // Args.notNull(data, "Hammond data");
        // this.dataArtifacts = data;
        // return builder();
        // }
    }

}
