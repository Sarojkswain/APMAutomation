package com.ca.tas.role;

import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.artifact.built.HammondArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.web.SeleniumRole;
import com.ca.tas.type.Platform;

public class CustomHammondRole extends AbstractRole {
    private final ITasResolver tasResolver;

    /**
     */
    public CustomHammondRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        tasResolver = builder.tasResolver;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        String targetDestination =
            normalizePath(getHostingMachine().getAutomationBaseDir() + "/hammond/hammond.jar");
        HammondArtifact hammondArtifact = new HammondArtifact(tasResolver);
        URL hammondArtifactURL =
            aaClient.getArtifactoryClient().getArtifactUrl(
                hammondArtifact.createArtifact("99.99.aquarius-SNAPSHOT").getArtifact());

        GenericFlowContext downoloadHammondCtx =
            new GenericFlowContext.Builder().artifactUrl(hammondArtifactURL)
                .destination(targetDestination).notArchive().build();
        runFlow(aaClient, GenericFlow.class, downoloadHammondCtx);

        properties.put("hammond_jar", targetDestination.toString());


        Artifact hammondData =
            new DefaultArtifact("com.ca.apm.systemtest", "atc-perf-data", null, "zip", "0.1");
        targetDestination = normalizePath(getHostingMachine().getAutomationBaseDir() + "/hammond");
        GenericFlowContext downoloadHammondDataCtx =
            new GenericFlowContext.Builder()
                .artifactUrl(aaClient.getArtifactoryClient().getArtifactUrl(hammondData))
                .destination(targetDestination).build();
        runFlow(aaClient, GenericFlow.class, downoloadHammondDataCtx);


        RunCommandFlowContext startHammondFlowCxt =
            new RunCommandFlowContext.Builder("java")
                .workDir(targetDestination)
                .terminateOnMatch("[AgentOrchestrator]")
                .doNotPrependWorkingDirectory()
                .args(
                    Arrays.asList("-cp", "hammond.jar",
                        "com.ca.apm.systemtest.fld.hammond.SmartstorPlayer", "-i",
                        normalizePath(targetDestination + "/playback"), "-c", "localhost")).build();

        runFlow(aaClient, RunCommandFlow.class, startHammondFlowCxt);
    }

    private String normalizePath(String targetDestination) {
        if (Platform.WINDOWS.equals(getHostingMachine().getPlatform())) {
            targetDestination = FilenameUtils.separatorsToWindows(targetDestination);
            targetDestination = FilenameUtils.normalize(targetDestination, false);
        } else {
            targetDestination = FilenameUtils.separatorsToUnix(targetDestination);
            targetDestination = FilenameUtils.normalize(targetDestination, true);
        }
        return targetDestination;
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link SeleniumRole}
     */
    public static class Builder extends BuilderBase<Builder, CustomHammondRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public CustomHammondRole build() {
            // Args.notNull(nodeJsRole, "nodeJsRole");

            CustomHammondRole hammond = getInstance();
            // hammond.after(nodeJsRole);

            return hammond;
        }

        @Override
        protected CustomHammondRole getInstance() {
            return new CustomHammondRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        // public Builder nodeJsRole(NodeJsRole nodeJsRole) {
        // this.nodeJsRole = nodeJsRole;
        // return this;
        // }
    }
}
