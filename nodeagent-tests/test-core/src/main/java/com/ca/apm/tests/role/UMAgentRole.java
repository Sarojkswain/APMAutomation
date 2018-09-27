package com.ca.apm.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.flow.DeployUMAgentFlow;
import com.ca.apm.tests.flow.DeployUMAgentFlowContext;
import com.ca.apm.tests.flow.DeployUMAgentFlowContext.Builder.CollectorAgentProperty;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

public class UMAgentRole extends AbstractAgentRole
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UMAgentRole.class);
    private final DeployUMAgentFlowContext UMAgentFlowContext;
    
    private static RunCommandFlowContext UMAgentStartCmdContext;
    private static RunCommandFlowContext UMAgentStopCmdContext;
    private static RunCommandFlowContext UMAgentRestartCmdContext;
    private List<RunCommandFlowContext> miscCmdCtxs;
    
    private boolean shouldStart = false;
    
    private int startDelayInSecs = 0;
    private int startTimeoutInSecs = 0;
    
    private static String iaDir = "apmia";
    
    protected UMAgentRole(Builder b)
    {
        super(b, b.getEnvProperties());
        UMAgentFlowContext = b.flowContext;
        UMAgentStartCmdContext = b.UMAStartCmdContext;
        UMAgentStopCmdContext = b.UMAStopCmdContext;
        UMAgentRestartCmdContext = b.UMARestartCmdContext;
        
        miscCmdCtxs = b.miscCmdCtxs;
        shouldStart = b.shouldAutoStart;
        startDelayInSecs = b.startDelayInSecs;
        startTimeoutInSecs = b.startTimeoutInSecs;
    }
    
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployUMA(aaClient);
        if (shouldStart) {
            startUMAgent(aaClient);
        }
    }

    protected void deployUMA(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployUMAgentFlow.class, UMAgentFlowContext);

        if (!miscCmdCtxs.isEmpty()) {
            for (RunCommandFlowContext ctx : miscCmdCtxs) {
                LOGGER.info("command: " + ctx.getExec());
                runCommandFlow(aaClient, ctx);
            }
        }
    }

    protected void startUMAgent(IAutomationAgentClient aaClient) {
        runCommandFlowAsync(aaClient, UMAgentStartCmdContext, startDelayInSecs);
    }

    protected void stopUMAgent(IAutomationAgentClient aaClient) {
        runCommandFlowAsync(aaClient, UMAgentStopCmdContext);
    }
    
    protected void restartUMAgent(IAutomationAgentClient aaClient) {
        runCommandFlowAsync(aaClient, UMAgentRestartCmdContext, startDelayInSecs);
    }

    public Map<String, String> getAdditionalProps() {
        return UMAgentFlowContext.getAdditionalProperties();
    }

    public static class LinuxBuilder extends Builder {
        public static final String UMA_AGENT_EXECUTABLE = "apmia-ca-installer.sh";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            flowContextBuilder = new DeployUMAgentFlowContext.LinuxBuilder();
            agentPlatform = ArtifactPlatform.UNIX;
            UMAExecutable = UMA_AGENT_EXECUTABLE;
        }

        @Override
        protected void initUMAgentStartContext() 
        {
            UMAgentStartCmdContext = new RunCommandFlowContext.Builder(UMAExecutable)
                    .args(Collections.singletonList(UMAStartArg))
                    .workDir(
                        flowContext.getInstallDir() + iaDir)
                    .terminateOnMatch(UMA_STARTUP_MESSAGE).build();
            getEnvProperties().add(ENV_START_UMA, UMAgentStartCmdContext);
        }

        @Override
        protected void initUMAgentStopContext() {
            UMAgentStopCmdContext = new RunCommandFlowContext.Builder(UMAExecutable)
                    .args(Collections.singletonList(UMAStopArg))
                    .workDir(
                        flowContext.getInstallDir() + iaDir)
                    .terminateOnMatch(UMA_STARTUP_MESSAGE).build();
            getEnvProperties().add(ENV_STOP_UMA, UMAgentStopCmdContext);
        }
        
        @Override
        protected void initUMAgentRestartContext() 
        {
            UMAgentRestartCmdContext = new RunCommandFlowContext.Builder(UMAExecutable)
                    .args(Collections.singletonList(UMARestartArg))
                    .workDir(
                        flowContext.getInstallDir() + iaDir)
                    .terminateOnMatch(UMA_STARTUP_MESSAGE).build();
            getEnvProperties().add(ENV_RESTART_UMA, UMAgentRestartCmdContext);
        }
    }
    
    public static class Builder extends AbstractAgentRole.AbstractBuilder<Builder, UMAgentRole>
    {
        public static final String UMA_EXECUTABLE = "apmia-ca-installer.cmd";
        private static final String START_ARG = "start";
        private static final String STOP_ARG = "stop";
        private static final String RESTART_ARG = "restart";
        
        public static final String UMA_STARTUP_MESSAGE = "Introscope Agent startup complete";
        public static final String ENV_UMA_AGENT_HOME = "UMAHome";
        public static final String ENV_START_UMA = "UMAStart";
        public static final String ENV_STOP_UMA = "UMAStop";
        public static final String ENV_RESTART_UMA = "UMARestart";
        protected String UMAExecutable = UMA_EXECUTABLE;
        protected String UMAStartArg = START_ARG;
        protected String UMAStopArg = STOP_ARG;
        protected String UMARestartArg = RESTART_ARG;
        
        protected DeployUMAgentFlowContext.Builder flowContextBuilder = new DeployUMAgentFlowContext.Builder();
        protected DeployUMAgentFlowContext flowContext;
        
        private boolean shouldAutoStart = false;
        private int startDelayInSecs = 0;
        private int startTimeoutInSecs = 0;
        
        protected RunCommandFlowContext UMAStartCmdContext;
        protected RunCommandFlowContext UMAStopCmdContext;
        protected RunCommandFlowContext UMARestartCmdContext;
        protected List<RunCommandFlowContext> miscCmdCtxs = new ArrayList<>();
        
        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }
        
        @Override
        public UMAgentRole build()
        {
            initUMAgentDeployContext();
            initUMAgentStartContext();
            initUMAgentStopContext();
            
            UMAgentRole role = (UMAgentRole) getInstance();
            
//            Args.notNull(role.UMAgentFlowContext, "UMA Agent deploy flow context");
//            Args.notNull(UMAgentStartCmdContext, "UMA Agent start command context");
            
            return role;
        }
        
        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected UMAgentRole getInstance() {
            return new UMAgentRole(this);
        }
        
        protected void initUMAgentDeployContext()
        {
            if (installDir != null) {
                flowContextBuilder.installDir(installDir);
            }
            
            if (installerTgDir != null) {
                flowContextBuilder.installerTgDir(installerTgDir);
            }
            
            flowContextBuilder.additionalProps(additionalProps);
            
            if (instrumentationLevel != null) {
                flowContextBuilder.intrumentationLevel(instrumentationLevel);
            }
            
            Artifact tasArtifact = initArtifact();
            URL installerUrl = tasResolver.getArtifactUrl(tasArtifact);
            flowContextBuilder.installerUrl(installerUrl);

            if (overrideEM) {
                flowContextBuilder.setupEm(emHostOverride, emPortOverride);
            } else {
                if (emRole != null) {
                    flowContextBuilder.setupEm(tasResolver.getHostnameById(emRole.getRoleId()),
                            emRole.getEmPort());
                }
            }
            
            flowContext = flowContextBuilder.build();
            getEnvProperties().add(ENV_UMA_AGENT_HOME, flowContext.getInstallDir());
        }
        
        protected Artifact initArtifact() {
            if (agentArtifact != null) {
                return agentArtifact;
            }

            if (tasAgentArtifact != null) {
                return tasAgentArtifact.createArtifact(agentVersion).getArtifact();
            }

            throw new IllegalArgumentException(
                    "agent artifact must be specified must be specified, when configuring UMA role.");
        }
        
        protected void initUMAgentStartContext() {
            UMAStartCmdContext = new RunCommandFlowContext.Builder(UMAExecutable)
                    .workDir(
                        flowContext.getInstallDir())
                    .name(roleId).terminateOnMatch(UMA_STARTUP_MESSAGE).build();
            getEnvProperties().add(ENV_START_UMA, UMAStartCmdContext);

        }
        
        protected void initUMAgentStopContext() {
            UMAgentStopCmdContext = new RunCommandFlowContext.Builder(UMAExecutable)
                    .workDir(
                        flowContext.getInstallDir())
                    .name(roleId).terminateOnMatch(UMA_STARTUP_MESSAGE).build();
            getEnvProperties().add(ENV_STOP_UMA, UMAgentStopCmdContext);

        }
        
        protected void initUMAgentRestartContext() {
            UMARestartCmdContext = new RunCommandFlowContext.Builder(UMAExecutable)
                    .workDir(
                        flowContext.getInstallDir())
                    .name(roleId).terminateOnMatch(UMA_STARTUP_MESSAGE).build();
            getEnvProperties().add(ENV_RESTART_UMA, UMARestartCmdContext);

        }
        
        public Builder listenProbeOnPort(int port) {
            this.additionalProps
                    .put(CollectorAgentProperty.TCP_PORT.getKey(), String.valueOf(port));
            return builder();
        }

        public Builder setTcpLocalMode(boolean mode) {
            this.additionalProps.put(CollectorAgentProperty.TCP_LOCAL_MODE.getKey(),
                    String.valueOf(mode));
            return builder();
        }

        public Builder autoStart() {
            this.shouldAutoStart = true;
            return builder();
        }

        public Builder setStartDelay(int delayInSecs) {
            this.startDelayInSecs = delayInSecs;
            return builder();
        }

        public Builder setStartTimeout(int timeout) {
            this.startTimeoutInSecs = timeout;
            return builder();
        }
    }
}
