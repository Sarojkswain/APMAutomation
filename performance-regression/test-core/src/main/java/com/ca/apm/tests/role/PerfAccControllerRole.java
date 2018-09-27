/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.acc.DeployAccControllerFlow;
import com.ca.apm.automation.action.flow.acc.DeployAccControllerFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.built.AccControllerArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.acc.AccControllerRole;
import com.ca.tas.role.acc.AccServerRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.utils.Port;
import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Code copied from AccControllerRole
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class PerfAccControllerRole extends AbstractRole {

    public static final String ACC_CONSOLE_START = "accConsoleStart";
    public static final String ACC_CONSOLE_STOP = "accConsoleStop";
    public static final String ACC_SERVICE_INSTALL = "accServiceInstall";
    public static final String ACC_SERVICE_UNINSTALL = "accServiceUninstall";
    public static final String ACC_SERVICE_RUN = "accServiceStart";
    public static final String ACC_SERVICE_STOP = "accServiceStop";

    private final DeployAccControllerFlowContext accControllerContext;
    private final RunCommandFlowContext serviceInstallFlowContext;
    private final RunCommandFlowContext serviceUninstallFlowContext;
    private final RunCommandFlowContext serviceStartFlowContext;
    private final RunCommandFlowContext serviceStopFlowContext;
    private final RunCommandFlowContext consoleStartFlowContext;
    private final RunCommandFlowContext consoleStopFlowContext;
    private final JavaRole customJava;
    private final boolean useConsole;
    private final int commandDelay;

    private final boolean autoStart;
    private final boolean predeployed;

    private PerfAccControllerRole(PerfAccControllerRole.Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.accControllerContext = builder.accControllerFlowCtx;
        this.serviceInstallFlowContext = builder.serviceInstallFlowContext;
        this.serviceUninstallFlowContext = builder.serviceUninstallFlowContext;
        this.serviceStartFlowContext = builder.serviceStartFlowContext;
        this.serviceStopFlowContext = builder.serviceStopFlowContext;
        this.consoleStartFlowContext = builder.consoleStartFlowContext;
        this.consoleStopFlowContext = builder.consoleStopFlowContext;
        this.customJava = builder.customJava;
        this.useConsole = builder.useConsole;
        this.commandDelay = builder.commandDelay;

        this.autoStart = builder.autoStart;
        this.predeployed = builder.predeployed;
    }

    public String getAccControllerInstallDir() {
        return accControllerContext.getAccControllerInstallDir();
    }

    public boolean isPredeployed() {
        return predeployed;
    }

    public void deploy(IAutomationAgentClient aaClient) {
        if (!predeployed) {
            this.runFlow(aaClient, DeployAccControllerFlow.class, this.accControllerContext);
        }
        if (autoStart) {
            this.start(aaClient);
        }
    }

    protected void start(IAutomationAgentClient client) {
        if (this.useConsole) {
            this.startFromConsole(client);
        } else {
            this.runAsService(client);
        }

    }

    protected void runAsService(IAutomationAgentClient client) {
        this.runCommandFlowAsync(client, this.serviceInstallFlowContext, this.commandDelay);
        this.runCommandFlowAsync(client, this.serviceStartFlowContext, this.commandDelay);
    }

    public void startFromConsole(IAutomationAgentClient client) {
        this.runCommandFlowAsync(client, this.consoleStartFlowContext, this.commandDelay);
    }

    @NotNull
    public Collection<? extends IRole> dependentRoles() {
        if (this.customJava == null) {
            return Collections.emptySet();
        } else {
            this.customJava.before(this, new IRole[0]);
            return Collections.singleton(this.customJava);
        }
    }

    public static class Builder extends BuilderBase<PerfAccControllerRole.Builder, PerfAccControllerRole> {
        public static final int COMMAND_DELAY = 30;
        private static final String RUN_CMD = "apmccctrl.cmd";
        @NotNull
        private final String roleId;
        @NotNull
        private final ITasResolver tasResolver;
        protected DeployAccControllerFlowContext.Builder accControllerFlowCtxBuilder = new DeployAccControllerFlowContext.Builder();
        protected IBuiltArtifact.ArtifactPlatform artifactPlatform;
        protected String cmd;
        protected DeployAccControllerFlowContext accControllerFlowCtx;
        protected RunCommandFlowContext serviceInstallFlowContext;
        protected RunCommandFlowContext serviceUninstallFlowContext;
        protected RunCommandFlowContext serviceStartFlowContext;
        protected RunCommandFlowContext serviceStopFlowContext;
        protected RunCommandFlowContext consoleStartFlowContext;
        protected RunCommandFlowContext consoleStopFlowContext;
        protected boolean autoStart;
        protected boolean mockMode;
        protected boolean predeployed;
        private Artifact artifact;
        private boolean useConsole;
        private JavaRole customJava;
        private String artifactVersion;
        private int commandDelay;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.artifactPlatform = IBuiltArtifact.ArtifactPlatform.WINDOWS;
            this.cmd = "apmccctrl.cmd";
            this.useConsole = true;
            this.commandDelay = 30;
            Args.notNull(roleId, "Role ID");
            Args.notNull(tasResolver, "Tas resolver");
            this.roleId = roleId;
            this.tasResolver = tasResolver;

            this.autoStart = false;
            this.mockMode = false;
        }

        public PerfAccControllerRole build() {
            this.initServerArtifact();
            this.initControllerDeployFlow();
            this.initServiceInstallFlowCtx();
            this.initStartFlowCtx();
            this.initConsoleStartFlowCtx();
            PerfAccControllerRole accControllerRole = this.getInstance();
            Args.notNull(this.accControllerFlowCtx, "accControllerFlowCtx");
            Args.notNull(this.serviceInstallFlowContext, "serviceInstallFlowContext");
            Args.notNull(this.serviceUninstallFlowContext, "serviceUninstallFlowContext");
            Args.notNull(this.serviceStartFlowContext, "serviceStartFlowContext");
            Args.notNull(this.serviceStopFlowContext, "serviceStopFlowContext");
            Args.notNull(this.consoleStartFlowContext, "consoleStartFlowContext");
            //Args.notNull(this.consoleStopFlowContext, "consoleStopFlowContext");
            return accControllerRole;
        }

        protected void initServerArtifact() {
            AccControllerArtifact serverInstaller = new AccControllerArtifact(this.artifactPlatform, this.tasResolver);
            this.artifact = serverInstaller.createArtifact(this.artifactVersion).getArtifact();
        }

        protected void initControllerDeployFlow() {
            this.accControllerFlowCtxBuilder.accControllerInstallerUrl(this.tasResolver.getArtifactUrl(this.artifact));
            this.accControllerFlowCtx = this.accControllerFlowCtxBuilder.build();
        }

        protected void initServiceInstallFlowCtx() {
            assert this.accControllerFlowCtx != null;

            this.serviceInstallFlowContext = (new RunCommandFlowContext.Builder(this.cmd))
                    .workDir(this.accControllerFlowCtx.getAccControllerInstallDir())
                    .args(Collections.singletonList("install"))
                    .name(this.roleId)
                    .terminateOnMatch("CA APM Command Center Agent Controller service installed")
                    .build();
            getEnvProperties().add(ACC_SERVICE_INSTALL, this.serviceInstallFlowContext);

            this.serviceUninstallFlowContext = (new RunCommandFlowContext.Builder(this.cmd))
                    .workDir(this.accControllerFlowCtx.getAccControllerInstallDir())
                    .args(Collections.singletonList("remove")).name(this.roleId)
                    /*.terminateOnMatch("CA APM Command Center Agent Controller service installed")*/
                    .build();
            getEnvProperties().add(ACC_SERVICE_UNINSTALL, this.serviceUninstallFlowContext);
        }

        protected void initStartFlowCtx() {
            assert this.accControllerFlowCtx != null;

            this.serviceStartFlowContext = (new RunCommandFlowContext.Builder(this.cmd))
                    .workDir(this.accControllerFlowCtx.getAccControllerInstallDir())
                    .args(Collections.singletonList("start"))
                    .name(this.roleId)
                    .terminateOnMatch("CA APM Command Center Agent Controller started")
                    .build();
            getEnvProperties().add(ACC_SERVICE_RUN, this.serviceStartFlowContext);

            this.serviceStopFlowContext = (new RunCommandFlowContext.Builder(this.cmd))
                    .workDir(this.accControllerFlowCtx.getAccControllerInstallDir())
                    .args(Collections.singletonList("stop"))
                    .name(this.roleId)
                    /*.terminateOnMatch("CA APM Command Center Agent Controller started")*/
                    .build();
            getEnvProperties().add(ACC_SERVICE_STOP, this.serviceStopFlowContext);
        }

        protected void initConsoleStartFlowCtx() {
            assert this.accControllerFlowCtx != null;

            this.consoleStartFlowContext = (new RunCommandFlowContext.Builder(this.cmd))
                    .workDir(this.accControllerFlowCtx.getAccControllerInstallDir())
                    .args(Collections.singletonList("console"))
                    .name(this.roleId)
                    .terminateOnMatch("Registration Listener Started")
                    .build();
            getEnvProperties().add(ACC_CONSOLE_START, this.consoleStartFlowContext);
        }

        protected PerfAccControllerRole.Builder builder() {
            return this;
        }

        protected PerfAccControllerRole getInstance() {
            return new PerfAccControllerRole(this);
        }

        public PerfAccControllerRole.Builder server(AccServerRole serverRole) {
            Args.notNull(serverRole, "ACC server role");
            this.accControllerFlowCtxBuilder.accControllerActiveMQHost(this.tasResolver.getHostnameById(serverRole.getRoleId()));
            return this;
        }

        public PerfAccControllerRole.Builder customJavaPath(String javaPath) {
            if (this.customJava != null) {
                throw new IllegalArgumentException("Cannot set both custom java role and custom java path.");
            } else {
                this.accControllerFlowCtxBuilder.javaPath(javaPath);
                return this;
            }
        }

        public PerfAccControllerRole.Builder customJava(JavaRole javaRole) {
            Args.notNull(javaRole, "javaRole");
            this.accControllerFlowCtxBuilder.javaPath(javaRole.getExecPath());
            this.customJava = javaRole;
            return this;
        }

        public PerfAccControllerRole.Builder version(String version) {
            this.artifactVersion = version;
            return this;
        }

        public PerfAccControllerRole.Builder jvmMaxMemory(int maxMemory) {
            this.accControllerFlowCtxBuilder.maxJvmMemory(maxMemory);
            return this;
        }

        public PerfAccControllerRole.Builder activeMQPort(int activeMQPort) {
            Args.check(Port.inUsableRange(activeMQPort), "Invalid active MQ port");
            this.accControllerFlowCtxBuilder.accControllerActiveMQPort(activeMQPort);
            return this;
        }

        public PerfAccControllerRole.Builder noConsole() {
            this.useConsole = false;
            return this;
        }

        public PerfAccControllerRole.Builder customConfig(Map<String, String> customConfig) {
            Args.notNull(customConfig, "customConfig");
            this.accControllerFlowCtxBuilder.customConfig(customConfig);
            return this;
        }

        public PerfAccControllerRole.Builder commandDelay(int delayInSeconds) {
            this.commandDelay = delayInSeconds;
            return this;
        }

        public PerfAccControllerRole.Builder autoStart() {
            this.autoStart = true;
            return this;
        }

        public PerfAccControllerRole.Builder mockMode() {
            this.mockMode = true;
            HashMap accConfig = new HashMap();
            accConfig.put("configurationServer.url", "http://dummyhost.org:8888");
            accConfig.put("com.ca.apm.acc.controller.mock", "true");
            customConfig(accConfig);
            return this;
        }

        public Builder predeployed() {
            this.predeployed = true;
            return builder();
        }

        public Builder predeployed(boolean predeployed) {
            this.predeployed = predeployed;
            return builder();
        }
    }

    public static class LinuxBuilder extends AccControllerRole.Builder {
        private static final String RUN_CMD = "/apmccctrl.sh";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            this.artifactPlatform = IBuiltArtifact.ArtifactPlatform.LINUX;
            this.cmd = "/apmccctrl.sh";
            this.accControllerFlowCtxBuilder = new com.ca.apm.automation.action.flow.acc.DeployAccControllerFlowContext.LinuxBuilder();
        }

        protected AccControllerRole.Builder builder() {
            return this;
        }
    }
}
