package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class PerfMonitorRole extends AbstractRole {

    public static final String ENV_PERF_MONITOR_START = "perfmon_start";
    public static final String ENV_PERF_MONITOR_STOP = "perfmon_stop";
    public static final String ENV_GET_PERF_LOG = "get_perf_log";

    @NotNull
    private ITasResolver tasResolver;
    @NotNull
    private String swPath;

    private IFlowContext configFileContext;
    private IFlowContext startFileContext;
    private IFlowContext stopFileContext;
    private IFlowContext getPerfLogFileContext;

    public PerfMonitorRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        tasResolver = builder.tasResolver;

        configFileContext = builder.configFileContext;
        startFileContext = builder.startFileContext;
        stopFileContext = builder.stopFileContext;
        getPerfLogFileContext = builder.getPerfLogFileContext;
        swPath = builder.swPath;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        // run script creation flow
        runFlow(aaClient, FileModifierFlow.class, startFileContext);
        runFlow(aaClient, FileModifierFlow.class, stopFileContext);
        runFlow(aaClient, FileModifierFlow.class, configFileContext);
        if (getPerfLogFileContext != null) {
            runFlow(aaClient, FileModifierFlow.class, getPerfLogFileContext);
        }
    }

    public static class Builder extends BuilderBase<Builder, PerfMonitorRole> {

        public static final String PERFMON_CONFIG_TXT = "perfmonConfig.txt";
        public static final String PERFMON_START_BAT = "perfmon_start.bat";
        public static final String PERFMON_STOP_BAT = "perfmon_stop.bat";
        public static final String GET_PERF_LOG_BAT = "get_perf_log.bat";

        protected FileModifierFlowContext configFileContext;
        protected FileModifierFlowContext startFileContext;
        protected FileModifierFlowContext stopFileContext;
        protected FileModifierFlowContext getPerfLogFileContext;
        protected String swPath = getWinDeployBase();
        protected RunCommandFlowContext stopCommandFlowContext;
        protected RunCommandFlowContext startCommandFlowContext;
        protected RunCommandFlowContext getPerfLogsCommandFlowContext;

        protected String roleId;
        protected ITasResolver tasResolver;

        protected String[] metrics;
        protected String sampleInterval;
        protected Long samplesNumber;
        protected String outputCsvFile;
        protected String configPrefix;
        private Map<String, String> fileMapping;
        private List<String> prepareCommands;
        private String shareHost;
        private String shareFolder;
        private String sharePassword;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.sharePassword = "Lister@123";
            this.metrics =
                    new String[]{"\"\\Memory\\Available MBytes\"",
                            "\"\\Memory\\Pool Nonpaged Bytes\"", "\"\\Memory\\Pool Paged Bytes\"",
                            "\"\\Processor(_Total)\\% Processor Time\"",
                            "\"\\PhysicalDisk(_Total)\\Current Disk Queue Length\"",
                            "\"\\PhysicalDisk(_Total)\\Disk Reads/sec\"",
                            "\"\\PhysicalDisk(_Total)\\Disk Read Bytes/sec\"",
                            "\"\\PhysicalDisk(_Total)\\Disk Writes/sec\"",
                            "\"\\PhysicalDisk(_Total)\\Disk Write Bytes/sec\"",
                            "\"\\PhysicalDisk(_Total)\\Disk Transfers/sec\"",
                            "\"\\PhysicalDisk(_Total)\\% Idle Time\"",
                            "\"\\Process(java*)\\% Processor Time\"",
                            "\"\\Process(java*)\\Private Bytes\"",
                            "\"\\Process(java*)\\Virtual Bytes\"",
                            "\"\\System\\Processor Queue Length\""};
            this.sampleInterval = "5";
            this.outputCsvFile = "performance.csv";
            this.configPrefix = "";
        }

        @Override
        public PerfMonitorRole build() {

            // configuration command
            String configFile = configPrefix + PERFMON_CONFIG_TXT;
            configFileContext =
                    new FileModifierFlowContext.Builder().create(concatPaths(swPath, configFile),
                            Arrays.asList(metrics)).build();

            String startScriptFile = configPrefix + PERFMON_START_BAT;
            Collection<String> startScriptData =
                    Arrays.asList(
                            "logman create counter PERFORMANCE -f csv "
                                    + (sampleInterval == null ? "" : " -si ")
                                    + (sampleInterval == null ? "" : sampleInterval)
                                    + (samplesNumber == null ? "" : " -sc ")
                                    + (samplesNumber == null ? "" : String.valueOf(samplesNumber))
                                    + " -v mmddhhmm -ow -o " + outputCsvFile + " -cf \"" + configFile + "\"",
                            "logman start PERFORMANCE");
            startFileContext =
                    new FileModifierFlowContext.Builder().create(concatPaths(swPath, startScriptFile),
                            startScriptData).build();

            String stopScriptFile = configPrefix + PERFMON_STOP_BAT;
            Collection<String> stopScriptData =
                    Arrays.asList("logman stop PERFORMANCE", "logman delete PERFORMANCE");
            stopFileContext =
                    new FileModifierFlowContext.Builder().create(concatPaths(swPath, stopScriptFile),
                            stopScriptData).build();

            String getPerfLogScriptFile = null;
            if (fileMapping != null && !fileMapping.isEmpty() && shareFolder != null) {
                getPerfLogScriptFile = configPrefix + GET_PERF_LOG_BAT;
                List<String> getPerfLogData = new ArrayList<>();
                if (prepareCommands != null && !prepareCommands.isEmpty()) {
                    getPerfLogData.addAll(prepareCommands);
                }

                //TODO - DS - handle error - and move mounts creating in the beginning of flow
                //System error 1312 has occurred.
                //A specified logon session does not exist. It may already have been terminated.

                //System error 1219 has occurred.
                //Multiple connections to a server or shared resource by the same user, using more than one user name, are not allowed. Disconnect all previous connections to the server or shared resource and try again.

                if (shareHost != null) {
                    getPerfLogData.add("net use \\\\" + shareHost + "\\" + shareFolder + " "
                            + sharePassword + " /USER:WORKGROUP\\administrator"); //TODO - DS - use variable user
                }

                for (Entry<String, String> mapping : fileMapping.entrySet()) {
                    getPerfLogData.add("copy " + mapping.getKey() +
                            (shareHost != null ? " \\\\" + shareHost + "\\" : " ") +
                            shareFolder + "\\" + mapping.getValue());
                }
                getPerfLogFileContext =
                        new FileModifierFlowContext.Builder().create(
                                concatPaths(swPath, getPerfLogScriptFile), getPerfLogData).build();
            }

            // start command
            startCommandFlowContext =
                    new RunCommandFlowContext.Builder(startScriptFile).workDir(swPath).name(roleId)
                            .build();
            getEnvProperties().add(ENV_PERF_MONITOR_START, startCommandFlowContext);

            // stop commands
            stopCommandFlowContext =
                    new RunCommandFlowContext.Builder(stopScriptFile).workDir(swPath).name(roleId)
                            .build();
            getEnvProperties().add(ENV_PERF_MONITOR_STOP, stopCommandFlowContext);

            // get perf logs
            if (getPerfLogScriptFile != null) {
                getPerfLogsCommandFlowContext =
                        new RunCommandFlowContext.Builder(getPerfLogScriptFile).workDir(swPath)
                                .name(roleId).build();
                getEnvProperties().add(ENV_GET_PERF_LOG, getPerfLogsCommandFlowContext);
            }

            return getInstance();
        }

        @Override
        protected PerfMonitorRole getInstance() {
            return new PerfMonitorRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installDir(String path) {
            this.swPath = path;
            return builder();
        }

        public Builder metrics(String[] metrics) {
            this.metrics = metrics;
            return builder();
        }

        public Builder sampleInterval(String sampleInterval) {
            this.sampleInterval = sampleInterval;
            return builder();
        }

        public Builder samplesNumber(Long samplesNumber) {
            this.samplesNumber = samplesNumber;
            return builder();
        }

        public Builder outputCsvFile(String outputCsvFile) {
            this.outputCsvFile = outputCsvFile;
            return builder();
        }

        public Builder configPrefix(String configPrefix) {
            this.configPrefix = configPrefix;
            return builder();
        }

        public Builder sharedFolder(String host, String folder) {
            this.shareHost = host;
            this.shareFolder = folder;
            return builder();
        }

        public Builder sharePassword(String password) {
            this.sharePassword = password;
            return builder();
        }

        public Builder perfLogFileMapping(Map<String, String> mapping) {
            this.fileMapping = mapping;
            return builder();
        }

        public Builder perfLogPrepareCommands(List<String> commands) {
            this.prepareCommands = commands;
            return builder();
        }
    }
}
