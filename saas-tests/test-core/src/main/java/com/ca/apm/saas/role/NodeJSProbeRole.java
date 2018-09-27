package com.ca.apm.saas.role;

import java.net.URL;
import java.util.Collections;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.saas.flow.DeployNodeJSPackageFlowContext;
import com.ca.apm.saas.flow.DeployNodeJSProbeFlow;
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
    private final String collectorAgentHost;

    protected NodeJSProbeRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.flowContext = builder.flowContext;
        this.appStartUpScriptPath = builder.appStartUpScriptPath;
        this.collectorAgentHost = builder.collectorAgentHost;
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
                Builder.NODEJS_PROBE_PACKAGE_NAME, collectorAgentHost);
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
        protected String collectorAgentHost = "localhost";
        protected String packageName;
        protected String version;
        protected Artifact artifact;
        protected ITasArtifactFactory tasArtifact;
        protected String appStartUpScriptPath;
        protected String nodeJsExecutableLocation;
        protected String nodeJsHomeDir;

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

        public Builder collectorAgentHost(String host) {
            this.collectorAgentHost = host;
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
    }
}