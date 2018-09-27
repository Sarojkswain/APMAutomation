package com.ca.apm.systemtest.fld.role;

import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.artifact.thirdparty.OSBVersion;
import com.ca.apm.systemtest.fld.flow.ConfigureOrclSrvBusAgentFlowContext;
import com.ca.apm.systemtest.fld.flow.OrclSrvcBusFlow;
import com.ca.apm.systemtest.fld.flow.OrclSrvcBusFlowContext;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.built.AgentNoInstaller;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.*;
import com.ca.tas.role.webapp.AgentCapable;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Note .. autoStart switch is used in case user wants to start OSB and install app using a different role (or something else)
 * otherwise user can use env variable to start the appserver with Agent and Profile file.
 *
 * @Author rsssa02
 */
public class OSBRole extends AbstractRole {
    public static final String EP_OSB_START = "osbStart";
    public static final String EP_OSB_STOP = "osbStop";
    public static final String EP_AGENT_ARGS = "javaAgentArgument";

    private final OrclSrvcBusFlowContext flowContext;
    private final ConfigureOrclSrvBusAgentFlowContext configureOSBFlowContext;
    private final ITasResolver tasResolver;
    protected RunCommandFlowContext startCommandFlowContext;
    protected RunCommandFlowContext stopCommandFlowContext;
    protected boolean autoStart;
    private static final Logger LOGGER = LoggerFactory.getLogger(OSBRole.class);

    public OSBRole(OSBRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.startCommandFlowContext = builder.startCommandFlowContext;
        this.stopCommandFlowContext = builder.stopCommandFlowContext;
        this.configureOSBFlowContext = builder.configureOSBFlowContext;
        this.flowContext = builder.flowContext;
        autoStart = builder.autoStart;
    }


    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        this.runFlow(aaClient, OrclSrvcBusFlow.class, this.flowContext);
        start(aaClient);
    }

    public ConfigureOrclSrvBusAgentFlowContext getConfigureWebLogicFlowContext() {
        return this.configureOSBFlowContext;
    }

    public ITasArtifact getAgentNoInstallerArtifact(IBuiltArtifact.ArtifactPlatform platform, @Nullable String version) {
        return (new AgentNoInstaller(AgentNoInstaller.Type.WEBLOGIC, platform, this.tasResolver)).createArtifact(version);
    }

    public ApplicationServerType getApplicationServerType() {
        return ApplicationServerType.WEBLOGIC;
    }

    public void start(IAutomationAgentClient aaClient) {
        if(autoStart) {
            LOGGER.info("Starting WebLogic application server.");
            this.runCommandFlowAsync(aaClient, this.startCommandFlowContext);
        }
    }

    public void stop(IAutomationAgentClient aaClient) {
        LOGGER.info("Stopping WebLogic application server.");
        this.runCommandFlowAsync(aaClient, this.stopCommandFlowContext);
    }

    public void configure(IAutomationAgentClient aaClient, AgentRole agentRole) {
        //TODO: what is the best approach? configure agent parameter or leave it as it is?
        LOGGER.info("*** Only Agent deployment is taken care for now, please use configureOSB to get serialized agent parameters!");
        LOGGER.debug("*** It is a better option to leave server un-configured for system test purpose since we may need Agent less server to perform baseline tests");
    }


    public static class Builder extends BuilderBase<OSBRole.Builder, OSBRole> {
        private static final OSBVersion DEFAULT_OSB_ARTIFACT;
        private final String roleId;
        private final ITasResolver tasResolver;
        protected Artifact osbArtifact;
        //private static final String DEFAULT_DOMAIN_HOME = "user_projects\\samples\\domains\\servicebus";

        protected RunCommandFlowContext startCommandFlowContext;
        protected RunCommandFlowContext stopCommandFlowContext;

        protected ConfigureOrclSrvBusAgentFlowContext.Builder configureOSBBuilder;
        protected ConfigureOrclSrvBusAgentFlowContext configureOSBFlowContext;
        protected OrclSrvcBusFlowContext.Builder flowContextBuilder;
        protected OrclSrvcBusFlowContext flowContext;
        protected boolean autoStart;

        static {
            DEFAULT_OSB_ARTIFACT = OSBVersion.VER_11G;
        }

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            this.initFlowContext();
            this.version(DEFAULT_OSB_ARTIFACT);
        }

        private void initFlowContext() {
            this.flowContextBuilder = new OrclSrvcBusFlowContext.Builder();
            configureOSBBuilder = new ConfigureOrclSrvBusAgentFlowContext.Builder();
        }

        protected void initOrclSrvBusDeployFlowContext() {
            URL artifactUrl = this.tasResolver.getArtifactUrl(this.osbArtifact);
            String fileName = this.getFilename(this.osbArtifact);
            Args.notNull(fileName, "File name");
            this.flowContext = this.flowContextBuilder.installPackageUrl(artifactUrl).installerFileName(fileName).build();
        }

        public OSBRole build() {
            initOrclSrvBusDeployFlowContext();
            initStartStopCommandFlow();
            initConfigureFlowContext();
            OSBRole osbole = this.getInstance();
            Args.notNull(osbole.flowContext, "Deploy flow context cannot be null.");
            return osbole;
        }

        private void initConfigureFlowContext() {
            configureOSBFlowContext = (new ConfigureOrclSrvBusAgentFlowContext.Builder(this.flowContext.getAgentJarPath(), this.flowContext.getProfileFilePath(), this.flowContext.getDomainDirRelativePath())).build();
            getEnvProperties().add(EP_AGENT_ARGS, configureOSBFlowContext);
        }

        private void initStartStopCommandFlow() {
            //Map envArgs = Collections.singletonMap("JAVA_OPTIONS", this.flowContext.getJavaAgentArgument());
            this.startCommandFlowContext = (new com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext.Builder(this.flowContext.getServerStartCommand())).terminateOnMatch("Server started in RUNNING mode").build();
            this.getEnvProperties().add("osbStart", this.startCommandFlowContext);
            this.stopCommandFlowContext = (new com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext.Builder(this.flowContext.getServerStopCommand())).build();
            this.getEnvProperties().add("osbStop", this.stopCommandFlowContext);
        }

        private OSBRole.Builder domainDirRelativePath(String defaultDomainHome) {
            this.flowContextBuilder.defaultDomainHome(defaultDomainHome);
            return this.builder();
        }
        protected OSBRole getInstance() {
            return new OSBRole(this);
        }


        public OSBRole.Builder installPath(String installLocation) {
            this.flowContextBuilder.installPath(installLocation);
            this.getEnvProperties().add("osb.home", installLocation);
            return this.builder();
        }

        // -jreLoc argument is needed for installer to run
        public OSBRole.Builder jreHomePath(String jreHomePath) {
            this.flowContextBuilder.jreHomeLocation(jreHomePath);
            return this.builder();
        }


        public OSBRole.Builder version(@NotNull OSBVersion osbVersion) {
            this.osbArtifact = osbVersion.getArtifact();
            this.flowContextBuilder.setupInstallerName(osbVersion.getSetupInstallerName());
            return this.builder();
        }

        public OSBRole.Builder wlsInstallPath(String wlsInstallPath)
        {
            this.flowContextBuilder.wlsServerHome(wlsInstallPath);
            return this.builder();
        }

        public OSBRole.Builder autoStart(){
            autoStart = true;
            return this.builder();
        }

        protected OSBRole.Builder builder() {
            return this;
        }
    }
}
