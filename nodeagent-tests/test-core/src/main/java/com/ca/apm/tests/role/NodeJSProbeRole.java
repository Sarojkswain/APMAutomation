/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.role;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.flow.DeployCollectorAgentFlowContext.Builder.CollectorAgentProperty;
import com.ca.apm.tests.flow.DeployNodeJSPackageFlowContext;
import com.ca.apm.tests.flow.DeployNodeJSProbeFlow;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.NodeJsRole;

public class NodeJSProbeRole extends AbstractRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeJSProbeRole.class);
    public static final String ENV_PROBE_REQUIRE_STMT = "probeRequireStatement";
    private final DeployNodeJSPackageFlowContext flowContext;
    private final String appStartUpScriptPath;
    private final String umAgentHost;
    @SuppressWarnings("unused")
    private final String umAgentPort;

    protected NodeJSProbeRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.flowContext = builder.flowContext;
        this.appStartUpScriptPath = builder.appStartUpScriptPath;
        this.umAgentHost = builder.umAgentHost;
        this.umAgentPort= builder.umAgentPort;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployProbe(aaClient);
        modifyStartUpScript(aaClient);
    }

    private void deployProbe(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployNodeJSProbeFlow.class, flowContext);
    }

    private void modifyStartUpScript(IAutomationAgentClient aaClient) {
        LOGGER.info("Adding nodejs probe hook into application startup script: {}",
                appStartUpScriptPath);

        // require statement to be inserted in beginning of nodejs app startup
        // script
        String probeStmt = String.format("var probe = require('%s').start('%s');",
                Builder.NODEJS_PROBE_PACKAGE_NAME, umAgentHost);
        int index = 0;

        FileModifierFlowContext context = new FileModifierFlowContext.Builder().insertAt(
                appStartUpScriptPath, index, Collections.singletonList(probeStmt)).build();
        runFlow(aaClient, FileModifierFlow.class, context);
        
        getEnvProperties().put(ENV_PROBE_REQUIRE_STMT, probeStmt);
    }

    /**
     * Linux Builder responsible for holding all necessary properties to
     * instantiate {@link NodeJSProbeRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            flowContextBuilder = new DeployNodeJSPackageFlowContext.LinuxBuilder();
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
     * {@link NodeJSProbeRole}
     */
    public static class Builder extends BuilderBase<Builder, NodeJSProbeRole> {

        public static final String ENV_NODEJS_PROBE_HOME = "nodeJsProbeHome";
        public static final String NODEJS_PROBE_PACKAGE_NAME = "ca-apm-probe";
        private final String roleId;
        private final ITasResolver tasResolver;
        protected DeployNodeJSPackageFlowContext.Builder flowContextBuilder = new DeployNodeJSPackageFlowContext.Builder();
        protected DeployNodeJSPackageFlowContext flowContext;

        protected String installDir;
        protected String installerTgDir;
        protected String umAgentHost = "localhost";
        protected String umAgentPort;
        protected String packageName;
        protected String version;
        protected Artifact artifact;
        protected ITasArtifactFactory tasArtifact;

        protected String appStartUpScriptPath;
        protected String nodeJsExecutableLocation;
        protected String nodeJsHomeDir;
        protected boolean shouldNativeBuildFail = false;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public NodeJSProbeRole build() {
            initDeployContext();
            return getInstance();
        }

        @Override
        protected NodeJSProbeRole getInstance() {
            return new NodeJSProbeRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        protected void initDeployContext() {
            if (installDir != null) {
                flowContextBuilder.installDir(installDir);
            }
            if (installerTgDir != null) {
                flowContextBuilder.installerTgDir(installerTgDir);
            }
            if (nodeJsHomeDir != null) {
                flowContextBuilder.nodeJsHomeDir(nodeJsHomeDir);
            }

            Args.notNull(nodeJsExecutableLocation, "nodeJsExecutableLocation");
            flowContextBuilder.nodeJsExecutableLocation(nodeJsExecutableLocation);

            if (packageName != null) {
                flowContextBuilder.packageName(packageName);
                flowContextBuilder.version(version);
            } else {
                Artifact tasArtifact = initArtifact();
                URL installerUrl = tasResolver.getArtifactUrl(tasArtifact);
                flowContextBuilder.installerUrl(installerUrl);
            }

            flowContextBuilder
                    .npmRegistryUrl(tasResolver.getCentralArtifactory() + "/api/npm/npm-release");
            flowContextBuilder.setShouldNativeBuildFail(shouldNativeBuildFail);
            flowContext = flowContextBuilder.build();

            getEnvProperties().add(
                    ENV_NODEJS_PROBE_HOME,
                    flowContext.getInstallDir() + getPathSeparator() + "node_modules" + getPathSeparator()
                            + NODEJS_PROBE_PACKAGE_NAME);
        }

        protected Artifact initArtifact() {
            if (artifact != null) {
                return artifact;
            }

            if (tasArtifact != null) {
                return tasArtifact.createArtifact(version).getArtifact();
            }

            throw new IllegalArgumentException(
                    "nodejs probe artifact must be specified must be specified, when configuring NodeJSProbe role.");
        }

        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return builder();
        }

        public Builder installerTgDir(String installerTgDir) {
            this.installerTgDir = installerTgDir;
            return builder();
        }

        public Builder nodeJSAppRole(NodeJSAppRole role) {
            appStartUpScriptPath(role.getStartupScriptPath());
            installDir(role.getHomeDir());
            return builder();
        }

        public Builder appStartUpScriptPath(String path) {
            this.appStartUpScriptPath = path;
            return builder();
        }

        public Builder nodeJSRole(NodeJsRole role) {
            nodeJsExecutableLocation(role.getDeployContext().getNodeJsExecutableLocation());
            String dest = role.getDeployContext().getDestination();
            nodeJsHomeDir(dest);
            // installDir(dest);
            return builder();
        }

        public Builder nodeJsExecutableLocation(String path) {
            this.nodeJsExecutableLocation = path;
            return builder();
        }

        public Builder nodeJsHomeDir(String path) {
            this.nodeJsHomeDir = path;
            return builder();
        }

        // public Builder collectorAgentRole(CollectorAgentRole role) {
        //     Map<String, String> props = role.getAdditionalProps();

        //     String host = tasResolver.getHostnameById(role.getRoleId());
        //     if (host != null) {
        //         // FIXME issue with probe socket connection when using host name
        //         // collectorAgentHost(host);
        //     }

        //     if (props.containsKey(CollectorAgentProperty.TCP_PORT.getKey())) {
        //         collectorAgentPort(props.get(CollectorAgentProperty.TCP_PORT.getKey()));
        //     }
        //     return builder();
        // }

        public Builder UMAgentRole(UMAgentRole role) {
            Map<String, String> props = role.getAdditionalProps();

            String host = tasResolver.getHostnameById(role.getRoleId());
            if (host != null) {
                // FIXME issue with probe socket connection when using host name
                // collectorAgentHost(host);
            }

            if (props.containsKey(CollectorAgentProperty.TCP_PORT.getKey())) {
                umAgentPort(props.get(CollectorAgentProperty.TCP_PORT.getKey()));
            }
            return builder();
        }

        public Builder umAgentHost(String host) {
            this.umAgentHost = host;
            return builder();
        }

        public Builder umAgentPort(int port) {
            umAgentPort(String.valueOf(port));
            return builder();
        }

        private Builder umAgentPort(String port) {
            this.umAgentPort = String.valueOf(port);
            return builder();
        }

        public Builder version(Artifact artifact) {
            this.artifact = artifact;
            return builder();
        }

        public Builder version(ITasArtifactFactory tasArtifact) {
            this.tasArtifact = tasArtifact;
            return builder();
        }

        public Builder version(IArtifactVersion version) {
            version(version);
            return builder();
        }

        public Builder version(String version) {
            this.version = version;
            return builder();
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return builder();
        }
        
        public Builder setShouldNativeBuildFail(boolean flag) {
            this.shouldNativeBuildFail = flag;
            return builder();
        }
    }
}