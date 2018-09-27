package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.tas.artifact.built.HammondArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Downloads Hammond tool artifact. Prepares start and stop commands.
 * Use {@link com.ca.apm.systemtest.fld.role.Hammond2Role.Builder} to configure the deployment
 *
 * @author Jiri Jirinec (jirji01@ca.com)
 */
public class Hammond2Role  extends AbstractRole {
    public static final String ENV_HAMMOND_HOME = "home";
    public static final String ENV_HAMMOND_START = "startCommand";
    public static final String ENV_HAMMOND_STOP = "stopCommand";
    public static final String ENV_HAMMOND_DATASETS = "datasets";

    protected final UniversalFlowContext hammondInstallContext;
    protected ArrayList<RunCommandFlowContext> startCommandFlowContexts;
    protected ArrayList<RunCommandFlowContext> stopCommandFlowContexts;

    protected Hammond2Role(Hammond2Role.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        hammondInstallContext = builder.hammondInstallContext;
        startCommandFlowContexts = builder.startCommandFlowContexts;
        stopCommandFlowContexts = builder.stopCommandFlowContexts;
    }

    public List<RunCommandFlowContext> getStartCommandFlowContexts() {
        return startCommandFlowContexts;
    }

    public List<RunCommandFlowContext> getStopCommandFlowContexts() {
        return stopCommandFlowContexts;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {
        runFlow(client, UniversalFlow.class, hammondInstallContext);
    }

    public static class LinuxBuilder extends Hammond2Role.Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected Hammond2Role.LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getJavaCmd() {
            return "java";
        }
    }

    public static class Builder extends BuilderBase<Hammond2Role.Builder, Hammond2Role> {
        protected ITasResolver tasResolver;
        protected String roleId;

        @Nullable
        protected UniversalFlowContext hammondInstallContext;
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
        protected String[] dataFolders;
        protected Integer group;
        protected Integer groups;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public Hammond2Role build() {
            installPath = concatPaths(deployBase, "hammond");
            getEnvProperties().add(ENV_HAMMOND_HOME, installPath);

            HammondArtifact artifact = new HammondArtifact(tasResolver);
            String hammondJar = concatPaths(installPath, "hammond.jar");
            URL artifactUrl = tasResolver.getArtifactUrl(artifact.createArtifact());
            hammondInstallContext =
                    new UniversalFlowContext.Builder().artifact(artifactUrl, hammondJar).build();

            for (int i = 0; i < dataFolders.length; i++) {
                String dataFolder = dataFolders[i];

                Args.notNull(collectorHost, "collectorHost");

                List<String> args = new ArrayList<>();
                args.add("-cp");
                args.add(hammondJar);
                args.add("-Xmx" + heapMemory);
                args.add("-XX:+UseConcMarkSweepGC");
                args.add("-XX:NewRatio=2");
                args.add("-XX:+UseParNewGC");
                args.add("-XX:MaxHeapFreeRatio=30");
                args.add("-XX:MinHeapFreeRatio=10");
                args.add("-DROLEID=" + roleId + i);
                args.add("com.ca.apm.systemtest.fld.hammond.HammondPlayer");
                args.add("-d");
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
                if (group != null && groups != null) {
                    args.add("-g");
                    args.add("" + group + "/" + groups);
                }

                RunCommandFlowContext startCommandFlowContext =
                        new RunCommandFlowContext.Builder(getJavaCmd()).args(args).workDir(installPath)
                                .name(roleId).doNotPrependWorkingDirectory().dontUseWindowsShell()
                                .terminateOnMatch("Introscope Agent startup complete.").build();
                startCommandFlowContexts.add(startCommandFlowContext);
                getEnvProperties().add(ENV_HAMMOND_START + i, startCommandFlowContext);

                RunCommandFlowContext stopCommandFlowContext =
                        new RunCommandFlowContext.Builder("wmic")
                                .args(
                                        Arrays
                                                .asList("Path", "win32_process", "Where",
                                                        "CommandLine Like '%-DROLEID=" + roleId + "%'", "Call",
                                                        "Terminate")).doNotPrependWorkingDirectory().ignoreErrors()
                                .build();
                stopCommandFlowContexts.add(stopCommandFlowContext);
                getEnvProperties().add(ENV_HAMMOND_STOP + i, stopCommandFlowContext);
            }
            getEnvProperties().add(ENV_HAMMOND_DATASETS, Integer.toString(dataFolders.length));

            return getInstance();
        }

        protected String getJavaCmd() {
            return "java";
        }

        @Override
        protected Hammond2Role getInstance() {
            return new Hammond2Role(this);
        }

        @Override
        protected Hammond2Role.Builder builder() {
            return this;
        }

        public Hammond2Role.Builder installDir(String path) {
            Args.notBlank(path, "install dir");
            this.deployBase = path;
            return builder();
        }

        public Hammond2Role.Builder runDuration(int seconds) {
            Args.positive(seconds, "run duration");
            runDuration = seconds;
            return builder();
        }

        public Hammond2Role.Builder collector(String machine) {
            Args.notNull(machine, "collector machine name");
            this.collectorHost = machine;
            return builder();
        }

        public Hammond2Role.Builder scale(double scale) {
            Args.check(scale > 0, "load scale");
            this.scale = scale;
            return builder();
        }

        public Hammond2Role.Builder prefix(String prefix) {
            Args.notNull(prefix, "prefix");
            this.prefix = prefix;
            return builder();
        }

        public Hammond2Role.Builder included(@NotNull String... strings) {
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

        public Hammond2Role.Builder excluded(String... strings) {
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

        public Hammond2Role.Builder heapMemory(String memory) {
            Args.notBlank(memory, "heap memory");
            this.heapMemory = memory;
            return builder();
        }

        public Hammond2Role.Builder data(String... dataFolders) {
            Args.notEmpty(Arrays.asList(dataFolders), "data artifact");
            this.dataFolders = dataFolders;
            return builder();
        }

        public Hammond2Role.Builder group(int group, int groups) {
            Args.check(group > 0, "group is indexed from 1");
            Args.check(group <= groups, "group is out of range");
            Args.check(groups > 0, "there must be at least one group");

            this.group = group;
            this.groups = groups;

            return builder();
        }
    }
}

