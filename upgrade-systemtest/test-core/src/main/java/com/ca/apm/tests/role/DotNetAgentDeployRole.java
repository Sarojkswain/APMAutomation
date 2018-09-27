package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.*;
import com.ca.apm.automation.utils.file.TasFileNameFilter;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * @author kurma05
 */
public class DotNetAgentDeployRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(DotNetAgentDeployRole.class);

    private ITasResolver tasResolver;
    private String installDir;
    private boolean isLegacyMode;
    private String emHost;
    private String agentHostName;
    private String agentVersion;
    private Map<String, String> configProperties;
    private List<String> commentOutLines;
    private List<String> appendLines;

    protected DotNetAgentDeployRole(Builder builder) {
        super(builder.roleId);
        this.tasResolver = builder.tasResolver;
        this.installDir = builder.installDir;
        this.isLegacyMode = builder.isLegacyMode;
        this.emHost = builder.emHost;
        this.agentHostName = builder.agentHostName;
        this.agentVersion = builder.agentVersion;
        this.configProperties = builder.configProperties;
        this.commentOutLines = builder.commentOutLines;
        this.appendLines = builder.appendLines;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployArtifact(aaClient);
        installAgent(aaClient);
        updateAgentProfile(aaClient);
        configureLegacyAgent(aaClient);
    }

    private void configureLegacyAgent(IAutomationAgentClient aaClient) {
        if (isLegacyMode) {
            FileModifierFlowContext copyFiles =
                new FileModifierFlowContext.Builder().copyFiltered(
                    installDir + "/wily/examples/legacy",
                    installDir + "/wily",
                    new TasFileNameFilter(".*",
                        TasFileNameFilter.FilterMatchType.REGULAR_EXPRESSION)).build();
            runFlow(aaClient, FileModifierFlow.class, copyFiles);
        }
    }

    private void updateAgentProfile(IAutomationAgentClient aaClient) {
        // stopping perfmon process to be able to update profile
        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("wmic").args(
                Arrays.asList("Path", "win32_process", "Where", "\"CommandLine", "Like",
                    "'%PerfMonCollectorAgent%'\"", "Call", "Terminate")).build();
        runFlow(aaClient, RunCommandFlow.class, command);

        // update profile
        Map<String, String> replacePairs = new HashMap<String, String>();
        if (isLegacyMode) {
            replacePairs.put("default-typical.pbl,hotdeploy",
                "default-full-legacy.pbl,hotdeploy\n\nintroscope.agent.configuration.old=true");
        } else {
            replacePairs.put("default-typical.pbl,hotdeploy", "default-full.pbl,hotdeploy");
        }

        String fileName = installDir + "/wily/IntroscopeAgent.profile";

        FileModifierFlowContext.Builder builder =
            new FileModifierFlowContext.Builder().replace(fileName, replacePairs);
        if (agentHostName != null && !agentHostName.isEmpty()) {
            builder.append(fileName,
                Collections.singletonList("introscope.agent.hostName=" + agentHostName));
        }

        if (commentOutLines != null && !commentOutLines.isEmpty()) {
            LOGGER.info("DotNetAgentDeployRole.updateAgentProfile():: commentOutLines = {}",
                commentOutLines);
            Map<String, String> m = new HashMap<String, String>();
            for (String s : commentOutLines) {
                m.put(s, "#" + s);
            }
            builder.replace(fileName, m);
        }

        FileModifierFlowContext context = builder.build();
        runFlow(aaClient, FileModifierFlow.class, context);

        if (!configProperties.isEmpty()) {
            ConfigureFlowContext configureEmFlowContext =
                (new ConfigureFlowContext.Builder()).configurationMap(fileName, configProperties)
                    .build();
            runFlow(aaClient, ConfigureFlow.class, configureEmFlowContext);
        }

        if (appendLines != null && !appendLines.isEmpty()) {
            LOGGER.info("DotNetAgentDeployRole.updateAgentProfile():: appendLines = {}",
                appendLines);
            FileModifierFlowContext.Builder builder2 = new FileModifierFlowContext.Builder();
            builder2.append(fileName, appendLines);
            FileModifierFlowContext context2 = builder2.build();
            runFlow(aaClient, FileModifierFlow.class, context2);
        }

        // uncomment perfmon startup if needed
        // keeping perfmon stopped for now
        // wait 15s for update
        try {
            Thread.sleep(15 * 1000);
        } catch (Exception e) {
            ;
        }
        command =
            new RunCommandFlowContext.Builder("net").args(
                Arrays.asList("start", "PerfMonCollectorAgent")).build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }

    private void deployArtifact(IAutomationAgentClient aaClient) {
        String artifact = "dotnet-agent-installer";

        URL url =
            tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.delivery", artifact, "64",
                "zip", agentVersion == null ? tasResolver.getDefaultVersion() : agentVersion));
        LOGGER.info("Downloading agent artifact " + url.toString());

        GenericFlowContext getAgentContext =
            new GenericFlowContext.Builder().artifactUrl(url).destination(installDir).build();
        runFlow(aaClient, GenericFlow.class, getAgentContext);
    }

    private void installAgent(IAutomationAgentClient aaClient) {
        // install agent
        RunCommandFlowContext command =
            new RunCommandFlowContext.Builder("IntroscopeDotNetAgentInstall64.exe")
                .workDir(installDir)
                .args(
                    Arrays.asList("/s", "/v\"", "/qn", "EMHOST=" + emHost, "INSTALLDIR=\""
                        + installDir + "\"\"")).build();
        runFlow(aaClient, RunCommandFlow.class, command);

        // reset iis
        command =
            new RunCommandFlowContext.Builder("C:\\Windows\\System32\\iisreset").args(
                Arrays.asList("/restart")).build();
        runFlow(aaClient, RunCommandFlow.class, command);
    }

    public static class Builder extends BuilderBase<Builder, DotNetAgentDeployRole> {
        private final String roleId;
        private final ITasResolver tasResolver;
        protected String installDir;
        protected boolean isLegacyMode;
        protected String emHost;
        private String agentHostName;
        private String agentVersion;
        private final Map<String, String> configProperties = new HashMap<>();
        private List<String> commentOutLines = new ArrayList<>();
        private List<String> appendLines = new ArrayList<>();

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public DotNetAgentDeployRole build() {
            return getInstance();
        }

        @Override
        protected DotNetAgentDeployRole getInstance() {
            return new DotNetAgentDeployRole(this);
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

        public Builder isLegacyMode(boolean isLegacyMode) {
            this.isLegacyMode = isLegacyMode;
            return builder();
        }

        /**
         * Override the agent host name displayed in WebView/Workstation
         * 
         * @param agentHostName
         * @return
         */
        public Builder agentHostName(String agentHostName) {
            this.agentHostName = agentHostName;
            return this;
        }

        public Builder agentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
            return this;
        }

        public Builder configProperty(String key, String value) {
            configProperties.put(key, value);
            return builder();
        }

        public Builder agentProfileCommentOutLines(List<String> commentOutLines) {
            this.commentOutLines.addAll(commentOutLines);
            return this;
        }

        public Builder agentProfileAppendLines(List<String> appendLines) {
            this.appendLines.addAll(appendLines);
            return this;
        }
    }

}
