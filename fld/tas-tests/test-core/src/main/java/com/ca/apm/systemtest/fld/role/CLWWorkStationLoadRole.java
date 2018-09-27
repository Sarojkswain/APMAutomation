package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.systemtest.fld.flow.ClwCleanupFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import org.eclipse.aether.artifact.DefaultArtifact;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Command Line Workstation Load Scripts for EM FLD. Real agent load should be
 * up and running before running these queries
 *
 * @author banra06@ca.com
 */

public class CLWWorkStationLoadRole extends AbstractRole {

    public static final String CLEANUP_CONTEXT_KEY = "cleanupContext";
    public static final String CLW_START_LOAD = "clwloadStart";
    public static final String CLW_STOP_LOAD = "clwloadStop";
    public static final String HCLW_START_LOAD = "historicalclwloadStart";
    public static final String HCLW_STOP_LOAD = "historicalclwloadStop";
    public static final String HCLW_FILENAME_KEY = "histQueryFilename";
    public static final String HCLW_FILENAME = "FLD.historical.past602.txt";
    public static final String INSTALL_DIR_KEY = "installDir";
    public static final String TRACES_DIR_KEY = "tracesDir";

    private String installDir;
    private int emPort;
    private String emHost;
    private ITasResolver tasResolver;
    private static final String EM_USER = "cemadmin";
    private static final String EM_PASSWORD = "quality";
    private Set<String> agentNames;
    private String tracesDir;

    protected CLWWorkStationLoadRole(Builder builder) {

        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.emHost = builder.emHost;
        this.emPort = builder.emPort;
        this.agentNames = builder.agentNames;
        this.tracesDir = builder.tracesDir;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        getArtifacts(aaClient);
        createBatchFiles(aaClient);
    }

    private void createBatchFiles(IAutomationAgentClient aaClient) {

        String cmd = "java -Xmx256m -Duser=%s -Dpassword=%s -Dhost=%s -Dport=%04d "
            + "-jar %%cd%%/CLWorkstation.jar get historical data from agents "
            + "matching .*1PortletAgent_11.* and metrics matching \"Backends\\|.*:"
            + "Average Response Time \\(ms\\)\" for past 60 minutes > %s";
        cmd = String.format(cmd, EM_USER, EM_PASSWORD, emHost, emPort, HCLW_FILENAME);
        Collection<String> historyicalQuery = Arrays
                .asList("echo Started",
                        "title CLW Historical-FLD past60",
                        ":Run",
                        cmd,
                        "ping 127.0.0.1 -n 60", "GOTO Run", "pause");
        Collection<String> CLWQuery = Arrays.asList("echo Started", "title TT launcher",
                "if not exist \"" + tracesDir + "\" mkdir " + tracesDir,
                "", ":start", "");
        FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
                .create(installDir + "/FLD.MOM.clw.past60.historical.bat",
                        historyicalQuery)
                .create(installDir + "/TTLauncher.bat", CLWQuery).build();
        runFlow(aaClient, FileModifierFlow.class, BatchFile);
        for (String name: agentNames) {
            Collection<String> command = Arrays
                    .asList("start java -Xmx256m -Duser="
                            + EM_USER
                            + " -Dpassword="
                            + EM_PASSWORD
                            + " -Dhost="
                            + emHost
                            + " -Dport="
                            + emPort
                            + " -Dintroscope.clw.tt.dirname=" + tracesDir
                            + " -jar %cd%/CLWorkstation.jar trace transactions exceeding 1 ms in agents matching \"("
                            + name + ")\" for 180 s",
                            "ping -n 60 127.0.0.1");
            FileModifierFlowContext createBatchFlow = new FileModifierFlowContext.Builder()
                    .append(installDir + "/TTLauncher.bat", command).build();
            runFlow(aaClient, FileModifierFlow.class, createBatchFlow);

            Collection<String> kickoffhistory = Arrays
                    .asList("start FLD.MOM.clw.past60.historical.bat");
            Collection<String> kickofflauncher = Arrays
                    .asList("start TTLauncher.bat");
            FileModifierFlowContext kickoffBatch = new FileModifierFlowContext.Builder()
                    .create(installDir + "/kickoffhistory.bat", kickoffhistory)
                    .create(installDir + "/kickofflauncher.bat",
                            kickofflauncher).build();
            runFlow(aaClient, FileModifierFlow.class, kickoffBatch);

        }
        Collection<String> command = Arrays.asList(":Wait 30 minutes",
                "ping 127.0.0.1 -n 1800", "cls", "GOTO start");
        FileModifierFlowContext createBatchFlow = new FileModifierFlowContext.Builder()
                .append(installDir + "/TTLauncher.bat", command).build();
        runFlow(aaClient, FileModifierFlow.class, createBatchFlow);
    }

    private void getArtifacts(IAutomationAgentClient aaClient) {

        URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
                "com.ca.apm.em", "com.wily.introscope.clw.feature", "", "jar",
                tasResolver.getDefaultVersion()));

        GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
                .artifactUrl(url)
                .destination(installDir + "/CLWorkstation.jar").notArchive()
                .build();
        runFlow(aaClient, GenericFlow.class, getAgentContext);

    }

    public static class Builder extends
            BuilderBase<Builder, CLWWorkStationLoadRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        protected String installDir;
        protected String emHost = "localhost";
        protected int emPort = 5001;
        private final Set<String> agentNames = new LinkedHashSet<>(1);
        private String tracesDir;
        protected long cleanupPeriod = 0;
        protected TimeUnit cleanupPeriodUnit;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public CLWWorkStationLoadRole build() {
            installDir = defaultIfBlank(installDir, concatPaths(getDeployBase(), "CLWLoad"));

            startTTLauncher();
            startHistoricalCLW();
            stopHistoricalCLW();
            stopTTLauncher();

            getEnvProperties().add(INSTALL_DIR_KEY, installDir);
            getEnvProperties().add(HCLW_FILENAME_KEY, HCLW_FILENAME);
            tracesDir = concatPaths(installDir, "traces");
            getEnvProperties().add(TRACES_DIR_KEY, tracesDir);

            ClwCleanupFlowContext.Builder cleanupFlowContextBuilder
                = new ClwCleanupFlowContext.Builder()
                .cleanupPeriod(cleanupPeriod, cleanupPeriodUnit)
                .dir(tracesDir);
            getEnvProperties().add(CLEANUP_CONTEXT_KEY, cleanupFlowContextBuilder.build());
            
            return getInstance();
        }

        @Override
        protected CLWWorkStationLoadRole getInstance() {
            return new CLWWorkStationLoadRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }

        public Builder emHost(String emHost) {
            this.emHost = emHost;
            return builder();
        }

        public Builder emPort(int emPort) {
            this.emPort = emPort;
            return builder();
        }

        public Builder agentName(String agentName) {
            agentNames.add(agentName);
            return builder();
        }

        /**
         * This function sets time between periodic cleanups of CLW's XML files.
         *
         * @param period   amount of time in units specified by second parameter
         * @param timeUnit time unit of the first parameter
         */
        public Builder cleanupPeriod(long period, TimeUnit timeUnit) {
            cleanupPeriod = period;
            cleanupPeriodUnit = timeUnit;
            return builder();
        }

        private void startTTLauncher() {
            RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
                    "kickofflauncher.bat").workDir(installDir).build();
            getEnvProperties().add(CLW_START_LOAD, runCmdFlowContext);

        }

        private void stopTTLauncher() {
            String stopCommand = "wmic process where \"CommandLine like '%TTLauncher%' and not (CommandLine like '%wmic%')\" Call Terminate";
            RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
                    stopCommand).build();
            getEnvProperties().add(CLW_STOP_LOAD, runCmdFlowContext);

        }

        private void stopHistoricalCLW() {
            String stopCommand = "wmic process where \"CommandLine like '%FLD.MOM%' and not (CommandLine like '%wmic%')\" Call Terminate";
            RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
                    stopCommand).build();
            getEnvProperties().add(HCLW_STOP_LOAD, runCmdFlowContext);

        }

        private void startHistoricalCLW() {
            RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
                    "kickoffhistory.bat").workDir(installDir).build();
            getEnvProperties().add(HCLW_START_LOAD, runCmdFlowContext);
        }

    }

}
