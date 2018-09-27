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

package com.ca.apm.transactiontrace.appmap.role;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.transactiontrace.appmap.artifact.CollectorAgentArtifact;
import com.ca.apm.transactiontrace.appmap.artifact.NodeJsProbeArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.TixChangeRole;

/**
 * NodeJsAgentRole class.
 *
 * Deploys the Collector Agent and the NodeJS probe on the top of the TixChange test application.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class NodeJsAgentRole extends AbstractRole {

    private final GenericFlowContext collectorAgentDeployFlowContext;
    private final RunCommandFlowContext collectorAgentStartFlowContext;
    private final GenericFlowContext nodeJsProbeDeployFlowContext;
    private final RunCommandFlowContext nodeJsProbeNpmInstallFlowContext;
    private final FileModifierFlowContext nodeJsProbeActivation;

    protected NodeJsAgentRole(Builder builder) {
        super(builder.roleId);

        collectorAgentDeployFlowContext = builder.collectorAgentDeployFlowContext;
        collectorAgentStartFlowContext = builder.collectorAgentStartFlowContext;
        nodeJsProbeDeployFlowContext = builder.nodeJsProbeDeployFlowContext;
        nodeJsProbeNpmInstallFlowContext = builder.nodeJsProbeNpmInstallFlowContext;
        nodeJsProbeActivation = builder.nodeJsProbeActivation;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployCollector(aaClient);
        startCollector(aaClient);
        deployProbe(aaClient);
        activateProbe(aaClient);
    }

    protected void deployCollector(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class,
            collectorAgentDeployFlowContext, getHostingMachine().getHostnameWithPort()));
    }

    protected void startCollector(IAutomationAgentClient aaClient) {
        if (collectorAgentStartFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class,
                collectorAgentStartFlowContext, getHostingMachine().getHostnameWithPort()));
        }
    }

    protected void deployProbe(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class, nodeJsProbeDeployFlowContext,
            getHostingMachine().getHostnameWithPort()));

        aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class,
            nodeJsProbeNpmInstallFlowContext, getHostingMachine().getHostnameWithPort()));
    }

    protected void activateProbe(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(FileModifierFlow.class, nodeJsProbeActivation,
            getHostingMachine().getHostnameWithPort()));
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link com.ca.apm.transactiontrace.appmap.role.NodeJsAgentRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected ArtifactPlatform getArtifactsPlatform() {
            return ArtifactPlatform.LINUX_AMD_64;
        }

        @Override
        protected String getCollectorAgentControlCommand() {
            return "CollectorAgent.sh";
        }

        @Override
        protected Map<String, String> getNodeJsProbeNpmInstallEnvironmentProps() {
            return Collections.singletonMap("HOME", "/root");
        }

        @Override
        protected Builder builder() {
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
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate
     * {@link com.ca.apm.transactiontrace.appmap.role.NodeJsAgentRole}
     */
    public static class Builder extends BuilderBase<Builder, NodeJsAgentRole> {

        private final String roleId;
        private final ITasResolver tasResolver;

        protected GenericFlowContext collectorAgentDeployFlowContext;
        @Nullable
        protected RunCommandFlowContext collectorAgentStartFlowContext;
        protected GenericFlowContext nodeJsProbeDeployFlowContext;
        protected RunCommandFlowContext nodeJsProbeNpmInstallFlowContext;
        protected FileModifierFlowContext nodeJsProbeActivation;

        protected GenericFlowContext.Builder collectorAgentDeployFlowContextBuilder;
        protected GenericFlowContext.Builder nodeJsProbeDeployFlowContextBuilder;
        protected FileModifierFlowContext.Builder nodeJsProbeActivationBuilder;

        protected String collectorAgentVersion;
        protected boolean collectorAgentAutoStart = false;

        protected String nodeJsProbeVersion;

        protected NodeJsRole nodeJsRole;

        protected TixChangeRole tixChangeRole;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            collectorAgentDeployFlowContextBuilder = new GenericFlowContext.Builder();
            nodeJsProbeDeployFlowContextBuilder = new GenericFlowContext.Builder();
            nodeJsProbeActivationBuilder = new FileModifierFlowContext.Builder();
        }

        protected ArtifactPlatform getArtifactsPlatform() {
            return ArtifactPlatform.WINDOWS_AMD_64;
        }

        protected String getCollectorAgentControlCommand() {
            return "CollectorAgent.cmd";
        }

        protected Map<String, String> getNodeJsProbeNpmInstallEnvironmentProps() {
            return Collections.emptyMap();
        }

        @Override
        public NodeJsAgentRole build() {
            collectorAgentDeployFlowContext =
                collectorAgentDeployFlowContextBuilder
                    .artifactUrl(
                        tasResolver.getArtifactUrl(new CollectorAgentArtifact(
                            getArtifactsPlatform(), tasResolver)
                            .createArtifact(collectorAgentVersion)))
                    .destination(concatPaths(getDeployBase(), "collectorAgent")).build();

            if (collectorAgentAutoStart) {
                collectorAgentStartFlowContext =
                    new RunCommandFlowContext.Builder(getCollectorAgentControlCommand())
                        .args(Collections.singleton("start"))
                        .workDir(concatPaths(getDeployBase(), "collectorAgent", "bin")).build();
            }

            ITasArtifact nodeJsProbeArtifact =
                new NodeJsProbeArtifact(getArtifactsPlatform(), tasResolver)
                    .createArtifact(nodeJsProbeVersion);
            String nodeJsProbeArtifactDownloadLocation =
                concatPaths(getDeployBase(),
                    TasFileUtils.getBasename(tasResolver.getArtifactUrl(nodeJsProbeArtifact)) + "."
                        + nodeJsProbeArtifact.getArtifact().getExtension());

            nodeJsProbeDeployFlowContext =
                nodeJsProbeDeployFlowContextBuilder
                    .artifactUrl(tasResolver.getArtifactUrl(nodeJsProbeArtifact))
                    .destination(nodeJsProbeArtifactDownloadLocation).notArchive().build();

            nodeJsProbeNpmInstallFlowContext =
                new RunCommandFlowContext.Builder(concatPaths(nodeJsRole.getDeployContext()
                    .getNodeJsBinDirectory(), "npm"))
                    .args(Arrays.asList("install", nodeJsProbeArtifactDownloadLocation))
                    .workDir(tixChangeRole.getFlowContext().getDestination())
                    .doNotPrependWorkingDirectory()
                    .environment(getNodeJsProbeNpmInstallEnvironmentProps()).build();

            nodeJsProbeActivation =
                nodeJsProbeActivationBuilder.insertAt(
                    concatPaths(tixChangeRole.getFlowContext().getDestination(), "server",
                        "server.js"),
                    0,
                    Collections
                        .singleton("var probe = require('ca-apm-probe').start('localhost');"))
                    .build();

            return getInstance();
        }

        public Builder collectorAgentVersion(String collectorAgentVersion) {
            this.collectorAgentVersion = collectorAgentVersion;
            return builder();
        }

        public Builder collectorAgentAutoStart() {
            this.collectorAgentAutoStart = true;
            return builder();
        }

        public Builder nodeJsProbeVersion(String nodeJsProbeVersion) {
            this.nodeJsProbeVersion = nodeJsProbeVersion;
            return builder();
        }

        public Builder nodeJsRole(NodeJsRole nodeJsRole) {
            this.nodeJsRole = nodeJsRole;
            return builder();
        }

        public Builder tixChangeRole(TixChangeRole tixChangeRole) {
            this.tixChangeRole = tixChangeRole;
            return builder();
        }

        @Override
        protected NodeJsAgentRole getInstance() {
            return new NodeJsAgentRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
