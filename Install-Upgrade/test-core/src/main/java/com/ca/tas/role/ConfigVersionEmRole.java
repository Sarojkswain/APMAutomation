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
package com.ca.tas.role;

import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.LINUX_AMD_64;
import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.UNIX;
import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS;
import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS_AMD_64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlow;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.tas.annotation.TasDocRole;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.built.IntroscopeInstaller;
import com.ca.tas.artifact.built.OsgiDistribution;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.type.Platform;

/**
 * Installs Enterprise Manager and WebView on Windows with given parameters and starts them.
 * Use {@link com.ca.tas.role.ConfigVersionEmRole.Builder} to configure the installation
 *
 * @author pojja01@ca.com
 * @version $Id: $Id
 */
@TasDocRole(platform = {Platform.LINUX, Platform.WINDOWS})
public class ConfigVersionEmRole extends AbstractRole {

    /**
     * Constant <code>EM_STATUS="Introscope Enterprise Manager started"</code>
     */
    public static final String EM_STATUS = "Introscope Enterprise Manager started";
    /**
     * Constant <code>WEBVIEW_STATUS="Introscope WebView started"</code>
     */
    public static final String WEBVIEW_STATUS = "Introscope WebView started";
    /**
     * Constant <code>ENV_PROPERTY_INSTALL_DIR="installDir"</code>
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_PROPERTY_INSTALL_DIR = "installDir";
    /**
     * Constant <code>ENV_START_EM="emStart"</code>
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_START_EM = "emStart";
    /** 
     * Constant <code>ENV_STOP_EM="emStop"</code> 
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_STOP_EM = "emStop";
    /**
     * Constant <code>ENV_KILL_EM="emKill"</code>
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_KILL_EM = "emKill";
    /**
     * Constant <code>ENV_UNINSTALL_EM="emUninstall"</code> 
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_UNINSTALL_EM = "emUninstall";
    /**
     * Constant <code>ENV_START_WEBVIEW="wvStart"</code>
     */
    @TasEnvironmentPropertyKey
    public static final String ENV_START_WEBVIEW = "wvStart";
    /** 
     * Constant <code>ENV_STOP_WEBVIEW="wvStop"</code> 
     */
    public static final String ENV_STOP_WEBVIEW = "wvStop";
    
    // private static final Logger LOGGER = LoggerFactory.getLogger(EmRole.class);
    private static final int ASYNC_DELAY = 90;
    @NotNull
    private final DeployEMFlowContext flowContext;
    private final ConfigureFlowContext configureEmFlowContext;
    private final RunCommandFlowContext emRunCommandFlowContext;
    private final RunCommandFlowContext emStopCommandFlowContext;
    private final RunCommandFlowContext emKillCmdFlowContext;
    private final RunCommandFlowContext emUninstallCommandFlowContext;
    private final RunCommandFlowContext wvRunCommandFlowContext;
    private final RunCommandFlowContext wvStopCommandFlowContext;
    private final PhantomJSRole phantomJSRole;
    private final boolean nostartEM;
    private final boolean nostartWV;
    private final int installTimeout;
    private final long startTimeout;

    /**
     * <p>Constructor for ConfigVersionEmRole.</p>
     *
     * @param builder a {@link com.ca.tas.role.ConfigVersionEmRole.Builder} object.
     */
    protected ConfigVersionEmRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        flowContext = builder.deployFlowContext;
        configureEmFlowContext = builder.configureEmFlowContext;
        nostartEM = builder.nostartEM;
        nostartWV = builder.nostartWV;
        emRunCommandFlowContext = builder.emRunCmdFlowContext;
        emStopCommandFlowContext = builder.emStopCmdFlowContext;
        emUninstallCommandFlowContext = builder.emUninstallCmdFlowContext;
        wvRunCommandFlowContext = builder.wvRunCmdFlowContext;
        wvStopCommandFlowContext = builder.wvStopCmdFlowContext;
        phantomJSRole = builder.phantomJSRole;
        installTimeout = builder.installTimeout;
        startTimeout = builder.startTimeout;
        emKillCmdFlowContext = builder.emKillCmdFlowCtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // run install
        runFlow(aaClient, DeployEMFlow.class, flowContext, installTimeout);

        modifyConfigFile(aaClient);
        startEm(aaClient);
        startWebView(aaClient);
    }

    @NotNull
    @Override
    public Collection<? extends IRole> dependentRoles() {
        Collection<IRole> dependentRoles = new ArrayList<>();
        if (null != phantomJSRole) {
            phantomJSRole.after(this);
            dependentRoles.add(phantomJSRole);
        }
        return dependentRoles;
    }

    /**
     * Modify properties in the EM configuration file
     */
    private void modifyConfigFile(IAutomationAgentClient aaClient) {
        if (configureEmFlowContext != null) {
            runFlow(aaClient, ConfigureFlow.class, configureEmFlowContext);
        }
    }

    /**
     * Starts EM in the background
     */
    private void startEm(IAutomationAgentClient aaClient) {
        if (nostartEM) {
            return;
        }

        FlowConfigBuilder timeout = new FlowConfigBuilder(RunCommandFlow.class, emRunCommandFlowContext, getHostWithPort())
            .delay(ASYNC_DELAY)
            .async()
            .timeout(startTimeout);

        aaClient.runJavaFlow(timeout);
    }

    /**
     * Starts Web View in the background
     */
    private void startWebView(IAutomationAgentClient aaClient) {
        if (nostartWV) {
            return;
        }

        runCommandFlowAsync(aaClient, wvRunCommandFlowContext, ASYNC_DELAY);
    }

    /**
     * <p>getEmPort.</p>
     *
     * @return a int.
     */
    public int getEmPort() {
        return flowContext.getEmPort();
    }

    /**
     * <p>getDeployEmFlowContext.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.em.DeployEMFlowContext} object.
     */
    public DeployEMFlowContext getDeployEmFlowContext() {
        return flowContext;
    }

    /**
     * <p>Getter for the field <code>emRunCommandFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getEmRunCommandFlowContext() {
        return emRunCommandFlowContext;
    }
    
    /**
     * <p>Getter for the field <code>emStopCommandFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getEmStopCommandFlowContext() {
        return emStopCommandFlowContext;
    }

    /**
     * <p>Getter for the field <code>emKillCmdFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getEmKillCommandFlowContext() {
        return emKillCmdFlowContext;
    }

    /**
     * <p>Getter for the field <code>emUninstallCommandFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getEmUninstallCommandFlowContext() {
        return emUninstallCommandFlowContext;
    }

    /**
     * <p>Getter for the field <code>installTimeout</code>.</p>
     *
     * @return a int.
     */
    public int getInstallTimeout() {
        return installTimeout;
    }

    /**
     * <p>isNostartEM.</p>
     *
     * @return a boolean.
     */
    public boolean isNostartEM() {
        return nostartEM;
    }

    /**
     * <p>isNostartWV.</p>
     *
     * @return a boolean.
     */
    public boolean isNostartWV() {
        return nostartWV;
    }

    /**
     * <p>Getter for the field <code>startTimeout</code>.</p>
     *
     * @return a int.
     */
    public int getStartTimeout() {
        return (int) startTimeout;
    }

    /**
     * <p>Getter for the field <code>wvRunCommandFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getWvRunCommandFlowContext() {
        return wvRunCommandFlowContext;
    }
    
    /**
     * <p>Getter for the field <code>wvStopCommandFlowContext</code>.</p>
     *
     * @return a {@link com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext} object.
     */
    public RunCommandFlowContext getWvStopCommandFlowContext() {
        return wvStopCommandFlowContext;
    }

    /**
     * Builder responsible for linux specific things
     *
     * @author turlu01
     */
    public static class LinuxBuilder extends Builder {

        public static final String INTROSCOPE_EXECUTABLE = "Introscope_Enterprise_Manager";
        public static final String UNINSTALL_EXECUTABLE = "Uninstall_Introscope";
        public static final String WEBVIEW_EXECUTABLE = "Introscope_WebView";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            introscopeExecutable = INTROSCOPE_EXECUTABLE;
            uninstallExecutable = UNINSTALL_EXECUTABLE;
            webviewExecutable = WEBVIEW_EXECUTABLE;
            osgiDistributionPlatform(UNIX);
            introscopePlatform(LINUX_AMD_64);
            flowContextBuilder = new DeployEMFlowContext.LinuxBuilder(getEnvProperties());
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected PhantomJSRole.Builder phantomJSRoleBuilder(String roleId, ITasResolver tasResolver) {
            return new PhantomJSRole.LinuxBuilder(roleId + PHANTOMJS_ROLE_SUFFIX, tasResolver);
        }
        
        @Override
        protected void initEmStopCommand() {
            emStopCmdFlowContext =
                new RunCommandFlowContext.Builder("java")
                    .args(
                        Arrays.asList(
                            "-jar", concatPaths(deployFlowContext.getInstallDir(), "lib"
                                , "CLWorkstation.jar"), "shutdown"))
                    .name(roleId).build();
            getEnvProperties().add(ENV_STOP_EM, emStopCmdFlowContext);
        }

        @Override
        protected void initEmKillCommand() {
            emKillCmdFlowCtx = new RunCommandFlowContext.Builder("pkill").ignoreErrors().args(
                    Arrays.asList("-f", "Introscope_Enterprise_Manager.lax")
            ).build();
            getEnvProperties().add(ENV_KILL_EM, emKillCmdFlowCtx);
        }

        @Override
        protected void initWvStopCommand() {
            wvStopCmdFlowContext =
                new RunCommandFlowContext.Builder("WVCtrl.sh").args(Arrays.asList("stop"))
                    .workDir(concatPaths(deployFlowContext.getInstallDir(), "bin")).name(roleId)
                    .build();
            getEnvProperties().add(ENV_STOP_WEBVIEW, wvStopCmdFlowContext);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    /**
     * Builds instance of {@link ConfigVersionEmRole} with given parameters for EM installation
     *
     * @author turyu01
     * @author pojja01
     */
    public static class Builder extends BuilderBase<Builder, ConfigVersionEmRole> {

        public static final String INTROSCOPE_EXECUTABLE = "Introscope_Enterprise_Manager.exe";
        public static final String UNINSTALL_EXECUTABLE = "Uninstall_Introscope.exe";
        public static final String WEBVIEW_EXECUTABLE = "Introscope_WebView.exe";
        public static final String EM_CONFIG_DIR = "config";
        public static final String EM_CONFIG_FILE = "IntroscopeEnterpriseManager.properties";
        protected static final String PHANTOMJS_ROLE_SUFFIX = "phantomJS";
        private static final String CONFIGURE_TIM_SCRIPT = "/com/ca/tas/role/configureTimPhantomJs.js";
        protected final ITasResolver tasResolver;
        protected final String roleId;
        // mandatory fields
        private final Map<String, String> configProperties = new HashMap<>();
        private final Map<String, String> transactionMonitors = new HashMap<>();
        // optional
        protected DeployEMFlowContext.Builder flowContextBuilder;
        protected String introscopeExecutable = INTROSCOPE_EXECUTABLE;
        protected String uninstallExecutable = UNINSTALL_EXECUTABLE;
        protected String webviewExecutable = WEBVIEW_EXECUTABLE;
        protected ArtifactPlatform osgiDistPlatform = WINDOWS;
        protected ArtifactPlatform introscopePlatform = WINDOWS_AMD_64;
        protected Artifact introscopeArtifact;
        protected Artifact osgiDistributionArtifact;
        protected Artifact eulaArtifact;
        @Nullable
        public String instroscopeVersion = "10.3.0.1";
        @Nullable
        protected String osgiDistVersion;
        @Nullable
        protected String eulaVersion;
        protected DeployEMFlowContext deployFlowContext;
        protected boolean nostartEM;
        protected boolean nostartWV;
        protected int installTimeout;
        protected int startTimeout;
        private boolean ignoreStopCommandErrors;
        private boolean ignoreUninstallCommandErrors;
        protected RunCommandFlowContext emKillCmdFlowCtx;
        protected RunCommandFlowContext emRunCmdFlowContext;
        protected RunCommandFlowContext emStopCmdFlowContext;
        protected RunCommandFlowContext emUninstallCmdFlowContext;
        protected RunCommandFlowContext wvRunCmdFlowContext;
        protected RunCommandFlowContext wvStopCmdFlowContext;
        protected ConfigureFlowContext configureEmFlowContext;

        protected PhantomJSRole phantomJSRole;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            flowContextBuilder = new DeployEMFlowContext.Builder(getEnvProperties());
        }

        /**
         * Builds instance of {@link ConfigVersionEmRole}
         */
        @Override
        public ConfigVersionEmRole build() {

            verifyTimeouts();
            initOsgiArtifact();
            initIntroscopeArtifact();
            initEulaArtifact();
            initFlowContext();
            initConfigureFlowContext(); // has to be after initFlowContext (it uses deployFlowContext)
            initEmRunCommand();
            initEmStopCommand();
            initEmKillCommand();
            initEmUninstallCommand();
            initWvRunCommand();
            initWvStopCommand();
            initEnvProperties();
            initTimConfiguration(); // has to be after initFlowContext (it uses deployFlowContext)

            ConfigVersionEmRole configEmRole = getInstance();
            Args.notNull(configEmRole.flowContext, "Em deploy flow context");
            Args.notNull(configEmRole.emRunCommandFlowContext, "Em run command flow context");
            Args.notNull(configEmRole.emStopCommandFlowContext, "Em stop command flow context");
            Args.notNull(configEmRole.emKillCmdFlowContext, "Em stop command flow context");
            Args.notNull(configEmRole.emUninstallCommandFlowContext, "Em uninstall command flow context");
            Args.notNull(configEmRole.wvRunCommandFlowContext, "Web view run command flow context");
            Args.notNull(configEmRole.wvStopCommandFlowContext, "Web view stop command flow context");

            return configEmRole;
        }

        private void initTimConfiguration() {
            // Configuring as part of deploy is not possible if EM is not started during deploy
            if (!nostartEM && !transactionMonitors.isEmpty()) {
                PhantomJSRole.Builder phantomJSRoleBuilder = phantomJSRoleBuilder(roleId, tasResolver);

                for (String timHost : transactionMonitors.keySet()) {
                    String timIPAddress = transactionMonitors.get(timHost);
                    String emHost = tasResolver.getHostnameById(roleId);
                    String emWebPort = String.valueOf(deployFlowContext.getEmWebPort());

                    phantomJSRoleBuilder.scripts(CONFIGURE_TIM_SCRIPT, emHost, emWebPort, timHost, timIPAddress);
                }

                phantomJSRole = phantomJSRoleBuilder.build();
            }
        }

        protected PhantomJSRole.Builder phantomJSRoleBuilder(String roleId, ITasResolver tasResolver) {
            return new PhantomJSRole.Builder(roleId + PHANTOMJS_ROLE_SUFFIX, tasResolver);
        }

        protected void verifyTimeouts() {
            if (startTimeout != 0 && installTimeout != startTimeout) {
                throw new IllegalStateException("Use noTimeout OR installTimeout not both of them at one time.");
            }
        }

        protected void initOsgiArtifact() {
            if (osgiDistributionArtifact != null) {
                return;
            }
            osgiDistributionArtifact = new OsgiDistribution(osgiDistPlatform, tasResolver).createArtifact(osgiDistVersion).getArtifact();
        }

        protected void initIntroscopeArtifact() {
            if (introscopeArtifact != null) {
                return;
            }
            introscopeArtifact = new IntroscopeInstaller(introscopePlatform, tasResolver).createArtifact(instroscopeVersion).getArtifact();
        }

        protected void initEulaArtifact() {
            if (eulaArtifact != null) {
                return;
            }
            String artEulaVersion = (eulaVersion == null) ? tasResolver.getDefaultVersion() : eulaVersion;
            if (artEulaVersion.startsWith("10.0.")) {
                artEulaVersion = "10.0";
            }
            eulaArtifact =
                new TasArtifact.Builder("eula").extension(IBuiltArtifact.TasExtension.ZIP)
                    .version(artEulaVersion)
                    .build().getArtifact();
        }

        protected void initFlowContext() {
            assert introscopeArtifact != null : "Missing introscope artifact";
            assert eulaArtifact != null : "Missing EULA artifact";

            flowContextBuilder.introscopeVersion(introscopeArtifact.getVersion());
            flowContextBuilder.introscopeUrl(tasResolver.getArtifactUrl(introscopeArtifact));

            boolean useOsgi = shouldUseOsgi(introscopeArtifact.getVersion());

            if (useOsgi) {
                assert osgiDistributionArtifact != null : "Missing OSGI dist artifact";
                assert osgiDistPlatform != null : "Missing OSGI platform";
                flowContextBuilder.osgiDistData(osgiDistributionArtifact.getVersion(), osgiDistPlatform.toString().toLowerCase());
                flowContextBuilder.osgiUrl(tasResolver.getArtifactUrl(osgiDistributionArtifact));
            } else {
                flowContextBuilder.dontUseOsgi();
            }

            flowContextBuilder.eulaVersion(eulaArtifact.getVersion());
            flowContextBuilder.eulaUrl(tasResolver.getArtifactUrl(eulaArtifact));

            deployFlowContext = flowContextBuilder.build();
        }

        protected boolean shouldUseOsgi(String introscopeVersion) {
            String[] versionParts = introscopeVersion.split("\\.");
            if (versionParts.length >= 2) {
                try {
                    int firstNum = Integer.parseInt(versionParts[0]);
                    if (firstNum >= 10) {
                        int secondNum = Integer.parseInt(versionParts[1]);
                        if (secondNum >= 2) {
                            return false;
                        }
                    }
                } catch (NumberFormatException e) {
                    // swallow all
                }
            }
            return true;
        }

        protected void initConfigureFlowContext() {
            if (!configProperties.isEmpty()) {
                configureEmFlowContext =
                    new ConfigureFlowContext.Builder().configurationMap(
                        deployFlowContext.getInstallDir() + getPathSeparator() + EM_CONFIG_DIR
                            + getPathSeparator() + EM_CONFIG_FILE, configProperties).build();

            }
        }

        protected void initEmRunCommand() {
            emRunCmdFlowContext = new RunCommandFlowContext.Builder(introscopeExecutable)
                .workDir(deployFlowContext.getInstallDir()).name(roleId)
                .terminateOnMatch(EM_STATUS).build();
            getEnvProperties().add(ENV_START_EM, emRunCmdFlowContext);
        }
        
        protected void initEmStopCommand() {
            RunCommandFlowContext.Builder builder = 
                new RunCommandFlowContext.Builder("java")
                    .args(
                        Arrays.asList(
                            "-jar",
                            concatPaths(deployFlowContext.getInstallDir(), "lib",
                                "CLWorkstation.jar"), "shutdown"))
                    .name(roleId);
            
            if (ignoreStopCommandErrors) {
                builder.ignoreErrors();
            }
            
            emStopCmdFlowContext = builder.build();
            getEnvProperties().add(ENV_STOP_EM, emStopCmdFlowContext);
        }

        protected void initEmKillCommand() {
            emKillCmdFlowCtx = new RunCommandFlowContext.Builder("taskkill").args(
                            Arrays.asList("/IM", "Introscope_Enterprise_Manager.exe", "/F", "/T")
                    ).name(roleId).ignoreErrors().build();
            getEnvProperties().add(ENV_KILL_EM, emKillCmdFlowCtx);
        }

        protected void initEmUninstallCommand() {
            RunCommandFlowContext.Builder builder =
                new RunCommandFlowContext.Builder(uninstallExecutable)
                    .args(Arrays.asList("-i", "silent"))
                    .workDir(
                        concatPaths(deployFlowContext.getInstallDir(), "UninstallerData", "base"))
                    .name(roleId);
            
            if (ignoreUninstallCommandErrors) {
                builder.ignoreErrors();
            }
            
            emUninstallCmdFlowContext = builder.build();
            getEnvProperties().add(ENV_UNINSTALL_EM, emStopCmdFlowContext);
        }

        protected void initWvRunCommand() {
            wvRunCmdFlowContext = new RunCommandFlowContext.Builder(webviewExecutable)
                .workDir(deployFlowContext.getInstallDir()).name(roleId)
                .terminateOnMatch(WEBVIEW_STATUS).build();
            getEnvProperties().add(ENV_START_WEBVIEW, wvRunCmdFlowContext);
        }
        
        protected void initWvStopCommand() {
            RunCommandFlowContext.Builder builder =
                new RunCommandFlowContext.Builder("taskkill")
                    .args(Arrays.asList("/F", "/T", "/IM", webviewExecutable))
                    .name(roleId);
            
            if (ignoreStopCommandErrors) {
                builder.ignoreErrors();
            }

            wvStopCmdFlowContext = builder.build();
            getEnvProperties().add(ENV_STOP_WEBVIEW, wvStopCmdFlowContext);
        }

        protected void initEnvProperties() {
            assert tasResolver != null;
            assert deployFlowContext != null;

            RolePropertyContainer envProperties = getEnvProperties();
            envProperties.add("em_hostname", tasResolver.getHostnameById(roleId));
            envProperties.add(ENV_PROPERTY_INSTALL_DIR, deployFlowContext.getInstallDir());
            for (Map.Entry<String, String> installerProperty : deployFlowContext.getInstallerProperties().entrySet()) {
                envProperties.add(installerProperty.getKey(), installerProperty.getValue());
            }
        }

        @Deprecated
        public Builder instroscopeVersion(IArtifactVersion instroscopeVersion) {
            Args.notNull(instroscopeVersion, "instroscopeVersion");
            this.instroscopeVersion = instroscopeVersion.getValue();
            return builder();
        }

        @Override
        protected ConfigVersionEmRole getInstance() {
            return new ConfigVersionEmRole(this);
        }

        @Deprecated
        public Builder instroscopeVersion(String instroscopeVersion) {
            Args.notNull(instroscopeVersion, "Introscope version");
            this.instroscopeVersion = instroscopeVersion;
            return builder();
        }

        public Builder introscopePlatform(ArtifactPlatform introscopePlatform) {
            this.introscopePlatform = introscopePlatform;
            return builder();
        }

        public Builder introscopeArtifact(Artifact introscopeArtifact) {
            this.introscopeArtifact = introscopeArtifact;
            return builder();
        }

        public Builder osgiDistributionPlatform(ArtifactPlatform osgiDistPlatform) {
            this.osgiDistPlatform = osgiDistPlatform;
            return builder();
        }

        @Deprecated
        public Builder osgiDistributionVersion(IArtifactVersion osgiDistVersion) {
            this.osgiDistVersion = osgiDistVersion.getValue();
            return builder();
        }

        public Builder osgiDistributionArtifact(Artifact osgiDistArtifact) {
            osgiDistributionArtifact = osgiDistArtifact;
            return builder();
        }

        @Deprecated
        public Builder osgiDistributionVersion(@NotNull String osgiDistVersion) {
            Args.notNull(osgiDistVersion, "Osgi distribution version");
            this.osgiDistVersion = osgiDistVersion;
            return builder();
        }

        public Builder eulaArtifact(Artifact eulaArtifact) {
            this.eulaArtifact = eulaArtifact;
            return builder();
        }

        @Deprecated
        public Builder eulaVersion(@NotNull String eulaVersion) {
            Args.notNull(eulaVersion, "EULA version");
            this.eulaVersion = eulaVersion;
            return builder();
        }

        public Builder version(@NotNull String version) {
            Args.notNull(version, "Version");
            this.instroscopeVersion = version;
            this.osgiDistVersion = version;
            this.eulaVersion = version;
            return this;
        }

        public Builder version(IArtifactVersion version) {
            this.instroscopeVersion = version.getValue();
            this.osgiDistVersion = version.getValue();
            this.eulaVersion = version.getValue();
            return builder();
        }

        public Builder installDir(String installDir) {
            flowContextBuilder.installDir(installDir);
            return builder();
        }

        public Builder installSubDir(String dir) {
            flowContextBuilder.installSubDir(dir);
            return builder();
        }

        public Builder installerTgDir(String installerTgDir) {
            flowContextBuilder.installerTgDir(installerTgDir);
            return builder();
        }

        public Builder emPort(int emPort) {
            flowContextBuilder.emPort(emPort);
            return builder();
        }

        public Builder emWebPort(int emWebPort) {
            flowContextBuilder.emWebPort(emWebPort);
            return builder();
        }

        public Builder wvPort(int wvPort) {
            flowContextBuilder.wvPort(wvPort);
            return builder();
        }

        public Builder wvEmHost(String wvEmHost) {
            flowContextBuilder.wvEmHost(wvEmHost);
            return builder();
        }

        public Builder wvEmPort(int wvEmPort) {
            flowContextBuilder.wvEmPort(wvEmPort);
            return builder();
        }

        public Builder wvEmWebPort(int wvEmWebPort) {
            flowContextBuilder.wvEmWebPort(wvEmWebPort);
            return builder();
        }

        public Builder installerProperty(String key, String value) {
            flowContextBuilder.installerProp(key, value);
            return builder();
        }

        @SuppressWarnings("deprecation")
        @Deprecated
        public Builder usePostgres(boolean usePostgres) {

            flowContextBuilder.usePostgres(usePostgres);
            return builder();
        }

        public Builder useOracle() {
            flowContextBuilder.useOracle();
            return builder();
        }

        public Builder dbhost(String dbhost) {
            flowContextBuilder.dbhost(dbhost);
            return builder();
        }

        public Builder dbport(int dbport) {
            flowContextBuilder.dbport(dbport);
            return builder();
        }

        public Builder dbname(String dbname) {
            flowContextBuilder.dbname(dbname);
            return builder();
        }

        public Builder dbuser(String dbuser) {
            flowContextBuilder.dbuser(dbuser);
            return builder();
        }

        public Builder dbpassword(String dbpassword) {
            flowContextBuilder.dbpassword(dbpassword);
            return builder();
        }

        public Builder dbAdminUser(String dbAdminUser) {
            flowContextBuilder.dbAdminUser(dbAdminUser);
            return builder();
        }

        public Builder dbAdminPassword(String dbAdminPassword) {
            flowContextBuilder.dbAdminPassword(dbAdminPassword);
            return builder();
        }

        public Builder databaseDir(String databaseDir) {
            flowContextBuilder.databaseDir(databaseDir);
            return builder();
        }
        
        public Builder oracleDbHost(String oracleDbHost) {
            flowContextBuilder.oracleDbHost(oracleDbHost);
            return builder();
        }

        public Builder oracleDbPort(int oracleDbPort) {
            flowContextBuilder.oracleDbPort(oracleDbPort);
            return builder();
        }

        public Builder oracleDbSidName(String oracleDbSidName) {
            flowContextBuilder.oracleDbSidName(oracleDbSidName);
            return builder();
        }

        public Builder oracleDbUsername(String oracleDbUsername) {
            flowContextBuilder.oracleDbUsername(oracleDbUsername);
            return builder();
        }

        public Builder oracleDbPassword(String oracleDbPassword) {
            flowContextBuilder.oracleDbPassword(oracleDbPassword);
            return builder();
        }

        public Builder emLaxNlJavaOption(Collection<String> emLaxNlJavaOptionAdditional) {
            flowContextBuilder.emLaxNlJavaOptionAdditional(emLaxNlJavaOptionAdditional);
            return builder();
        }
        
        public Builder emLaxNlClearJavaOption(Collection<String> emLaxNlJavaOptionAdditional) {
            flowContextBuilder.emLaxNlClearJavaOptionAdditional(emLaxNlJavaOptionAdditional);
            return builder();
        }

        public Builder wvLaxNlJavaOption(Collection<String> wvLaxNlJavaOptionAdditional) {
            flowContextBuilder.wvLaxNlJavaOptionAdditional(wvLaxNlJavaOptionAdditional);
            return builder();
        }
        
        public Builder wvLaxNlClearJavaOption(Collection<String> wvLaxNlJavaOptionAdditional) {
            flowContextBuilder.wvLaxNlClearJavaOptionAdditional(wvLaxNlJavaOptionAdditional);
            return builder();
        } 

        public Builder silentInstallChosenFeatures(Collection<String> silentInstallChosenFeatures) {
            flowContextBuilder.silentInstallChosenFeatures(silentInstallChosenFeatures);
            return builder();
        }

        /**
         * Disable EM start after deployment
         */
        public Builder nostartEM() {
            nostartEM = true;
            nostartWV = true;
            return builder();
        }

        /**
         * Disable Webview start after deployment
         */
        public Builder nostartWV() {
            nostartWV = true;
            return builder();
        }


        public Builder dbInstallScriptTimeoutInMillis(long dbInstallScriptTimeoutInMillis) {
            flowContextBuilder.dbInstallScriptTimeoutInMillis(dbInstallScriptTimeoutInMillis);
            return builder();
        }

        public Builder sampleResponseFile(String sampleResponseFile) {
            flowContextBuilder.sampleResponseFile(sampleResponseFile);
            return builder();
        }

        public Builder licenceName(String licenceName) {
            flowContextBuilder.licenceName(licenceName);
            return builder();
        }

        public Builder eulaName(String eulaName) {
            flowContextBuilder.eulaName(eulaName);
            return builder();
        }

        /**
         * CA EULA file name in case you want to provide it as part of installer archive. <code>null</code> if CA EULA is not needed
         */
        public Builder caEulaName(String caEulaName) {
            flowContextBuilder.caEulaName(caEulaName);
            return builder();
        }

        /**
         * Sets no custom timeout on em installation, the time out is going to be determined by max polling agent timeout
         */
        public Builder noTimeout() {
            // Maximum value has to be lower than Int max, because of FlowExecutionBase works with
            // value in millis and after multiplying the value would overflow
            installTimeout = FlowConfigBuilder.MAX_TIMEOUT_FLAG;
            startTimeout = FlowConfigBuilder.MAX_TIMEOUT_FLAG;
            return builder();
        }

        /**
         * Sets EM installation timeout in seconds.
         *
         * @param timeout - timeout value in seconds
         */
        public Builder installTimeout(int timeout) {
            Args.check(timeout > 0, "Timeout cannot be less or equal to 0");
            installTimeout = timeout;
            return builder();
        }

        /**
         * Sets EM installation timeout in custom units.
         *
         * @param timeout - timeout value in seconds
         */
        public Builder installTimeout(int timeout, TimeUnit unit) {
            installTimeout((int) unit.toSeconds(timeout));
            return builder();
        }

        public Builder configProperty(String key, String value) {
            configProperties.put(key, value);
            return builder();
        }

        public Builder ignoreStopCommandErrors() {
            ignoreStopCommandErrors = true;
            return builder();
        }
        
        public Builder ignoreUninstallCommandErrors() {
            ignoreUninstallCommandErrors = true;
            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
