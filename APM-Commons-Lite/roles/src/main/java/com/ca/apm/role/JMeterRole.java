package com.ca.apm.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.commons.artifact.thirdparty.JMeterVersion;
import com.ca.apm.commons.flow.DeployJMeterFlow;
import com.ca.apm.commons.flow.DeployJMeterFlowContext;
import com.ca.apm.commons.flow.StartJMeterFlow;
import com.ca.apm.commons.flow.StopJMeterFlow;
import com.ca.tas.annotation.TasDocRole;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.annotation.TasResource;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.TestProperty;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.type.Platform;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * This role represents Apache JMeter.
 *
 * @author Boch, Tomas (bocto01@ca.com)
 * @author haiva01
 */
@TasDocRole(platform = {Platform.WINDOWS})
public class JMeterRole extends AbstractRole {
    @TasEnvironmentPropertyKey
    public static final String ENV_JMETER_START = "startJMeter";
    @TasEnvironmentPropertyKey
    public static final String ENV_JMETER_STOP = "stopJMeter";
    private static final Logger log = LoggerFactory.getLogger(JMeterRole.class);
    @NotNull
    private final DeployJMeterFlowContext deployJMeterFlowContext;

    private final boolean autoStart;

    @Nullable
    private final JavaRole customJava;

    @NotNull
    private final AbstractRole startRole;

    @NotNull
    private final AbstractRole stopRole;

    private final boolean addJMeterExtension;
    private final Collection<ITasArtifact> extensionArtifacts;
    private final String testPlanDir;
    private final String testPlan;
    private final String jmeterLogFile;
    private final String logFile;
    private final String outputFile;
    private final Map<String, String> jmeterProperties;
    private final Map<String, String> testPlanDestPathToResourcePathMap;
    private ITasArtifact testPlanArchive;

    private String installDir;

    protected JMeterRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.deployJMeterFlowContext = builder.deployJMeterFlowContext;
        this.autoStart = builder.autoStart;
        this.customJava = builder.customJava;
        this.startRole = builder.startRole;
        this.stopRole = builder.stopRole;
        this.addJMeterExtension = builder.addJMeterExtension;
        this.extensionArtifacts = builder.extensionArtifacts;
        this.testPlanDir = builder.testPlanDir;
        this.testPlan = builder.testPlan;
        this.testPlanArchive = builder.testPlanArchive;
        this.jmeterLogFile = builder.jmeterLogFile;
        this.logFile = builder.logFile;
        this.outputFile = builder.outputFile;
        this.jmeterProperties = builder.jmeterProperties;
        this.installDir = builder.installDir;
        this.testPlanDestPathToResourcePathMap = builder.testPlanDestPathToResourcePathMap;
    }

    private static String getTargetFilename(URL artifactUrl, Artifact artifact) {
        // This is because org.eclipse.aether.artifact.DefaultArtifact (implementation of
        // org.eclipse.aether.artifact.Artifact) returns null for getFile()
        return TasFileUtils.getBasename(artifactUrl)
            + (isNotEmpty(artifact.getExtension()) ? ("." + artifact.getExtension()) : "");
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployJMeter(aaClient);
        deployJMeterExtensions(aaClient);
        deployTestPlanArchive(aaClient);
        deployTestPlanResources(aaClient);
    }

    protected void deployTestPlanResources(IAutomationAgentClient aaClient) {
        if (testPlanDestPathToResourcePathMap != null
            && !testPlanDestPathToResourcePathMap.isEmpty()) {
            FileModifierFlowContext.Builder confResourceFlowContextBuilder
                = new FileModifierFlowContext.Builder();
            for (Entry<String, String> testPlanResourceEntry : testPlanDestPathToResourcePathMap
                .entrySet()) {
                confResourceFlowContextBuilder
                    .resource(testPlanResourceEntry.getKey(), testPlanResourceEntry.getValue());
            }

            FileModifierFlowContext testPlanResourceCreatorContext = confResourceFlowContextBuilder
                .build();
            runFlow(aaClient, FileModifierFlow.class, testPlanResourceCreatorContext);
        }
    }

    protected void deployJMeter(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeployJMeterFlow.class, deployJMeterFlowContext);
    }

    protected void deployJMeterExtensions(IAutomationAgentClient aaClient) {
        if (addJMeterExtension) {
            String jmeterExtDir = deployJMeterFlowContext.getJMeterExtDir();
            for (ITasArtifact tasArtifact : extensionArtifacts) {
                Artifact artifact = tasArtifact.getArtifact();
                URL artifactUrl = aaClient.getArtifactoryClient().getArtifactUrl(artifact);
                String targetFilename = getTargetFilename(artifactUrl, artifact);
                GenericFlowContext installJMeterExtensionFlowContext =
                    (new GenericFlowContext.Builder()).artifactUrl(artifactUrl)
                        .destination(jmeterExtDir).targetFilename(targetFilename).notArchive()
                        .build();
                runFlow(aaClient, GenericFlow.class, installJMeterExtensionFlowContext);
            }
        }
    }

    protected void deployTestPlanArchive(IAutomationAgentClient aaClient) {
        if (testPlanArchive != null) {
            Artifact artifact = testPlanArchive.getArtifact();
            URL url = aaClient.getArtifactoryClient().getArtifactUrl(artifact);
            GenericFlowContext ctx = new GenericFlowContext.Builder(url)
                .destination(testPlanDir)
                .build();
            runFlow(aaClient, GenericFlow.class, ctx);
        }
    }

    @NotNull
    @Override
    public Collection<? extends IRole> dependentRoles() {
        Collection<IRole> innerRoles = new ArrayList<>(2);
        if (customJava != null) {
            customJava.before(this);
            innerRoles.add(customJava);
        }
        if (autoStart) {
            startRole.after(this);
            innerRoles.add(startRole);
        }
        return innerRoles;
    }

    public String getInstallDir() {
        return deployJMeterFlowContext.getJMeterInstallDir();
    }

    @NotNull
    public DeployJMeterFlowContext getJMeterDeployFlowContext() {
        return deployJMeterFlowContext;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    @Nullable
    public JavaRole getCustomJava() {
        return customJava;
    }

    @Nullable
    public String getTestPlan() {
        return testPlan;
    }

    @TasResource(value = "jmeter", regExp = ".*")
    @Nullable
    public String getJMeterLogFile() {
        return jmeterLogFile;
    }

    @TasResource(value = "jmeter", regExp = ".*")
    @Nullable
    public String getLogFile() {
        return logFile;
    }

    @TasResource(value = "jmeter", regExp = ".*")
    @Nullable
    public String getOutputFile() {
        return outputFile;
    }

    @Nullable
    public Map<String, String> getJMeterProperties() {
        return Collections.unmodifiableMap(jmeterProperties);
    }

    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            jmeterDeployFlowContextBuilder = new DeployJMeterFlowContext.LinuxBuilder();
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getJavaBase() {
            return getLinuxJavaBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }
    }

    public static class Builder extends BuilderBase<Builder, JMeterRole> {
        protected static final String DEFAULT_VERSIONED_INST_DIR = "apache-jmeter-%s";

        private final String roleId;
        private final ITasResolver tasResolver;
        private final Collection<ITasArtifact> extensionArtifacts = new ArrayList<>();
        protected DeployJMeterFlowContext deployJMeterFlowContext;
        protected DeployJMeterFlowContext.Builder jmeterDeployFlowContextBuilder;
        protected RunCommandFlowContext startCommandFlowContext;
        protected AbstractRole startRole;
        protected RunCommandFlowContext stopCommandFlowContext;
        protected AbstractRole stopRole;
        protected String installDir;
        protected ITasArtifact scriptsArtifact;
        private JMeterVersion jmeterVersion;
        private Artifact jmeterArtifact;
        private JavaRole customJava;
        private boolean autoStart;
        private boolean addJMeterExtension = false;
        private String testPlanDir;
        private String testPlan;
        private String jmeterLogFile;
        private String logFile;
        private String outputFile;
        private Map<String, String> jmeterProperties;
        private Map<String, String> testPlanDestPathToResourcePathMap = new HashMap<>();
        private ITasArtifact testPlanArchive;
        private String jdkHomeDir;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            jmeterVersion = getDefaultJMeterVersion();
            jmeterArtifact = jmeterVersion.getArtifact();
        }

        @Override
        public JMeterRole build() {
            initJMeterDeployFlow();
            initScriptsArchiveDeployFlow();
            initStartStop();

            JMeterRole jmeterRole = getInstance();
            Args.notNull(jmeterRole.deployJMeterFlowContext, "Deploy JMeter flow context");
            Args.notNull(jmeterRole.startRole, "JMeter start role flow");
            Args.notNull(jmeterRole.stopRole, "JMeter start role flow");
            return jmeterRole;
        }

        private void initScriptsArchiveDeployFlow() {
        }

        JMeterVersion getDefaultJMeterVersion() {
            return JMeterVersion.v212;
        }

        protected void initJMeterDeployFlow() {
            Args.notNull(jmeterArtifact, "jMeter artifact must be specified");
            jmeterDeployFlowContextBuilder = new DeployJMeterFlowContext.Builder();

            if (isBlank(installDir)) {
                installDir = concatPaths(getDeployBase(),
                    String.format(Locale.US, DEFAULT_VERSIONED_INST_DIR, jmeterVersion));
            }
            jmeterDeployFlowContextBuilder.installDir(installDir);
            URL artifactUrl = tasResolver.getArtifactUrl(jmeterArtifact);

            jmeterDeployFlowContextBuilder.jmeterBinariesArtifactURL(artifactUrl);

            if (scriptsArtifact != null) {
                jmeterDeployFlowContextBuilder
                    .jmeterScriptsArchive(tasResolver.getArtifactUrl(scriptsArtifact));
            }
            if (jdkHomeDir != null) {
                jmeterDeployFlowContextBuilder.jdkHomeDir(jdkHomeDir);
            }
            final String testPlanDir = concatPaths(installDir, "testplan");
            if (testPlan != null) {
                testPlan = concatPaths(testPlanDir, testPlan);
                jmeterDeployFlowContextBuilder.testPlan(testPlan);
            }

            Map<String, String> fromResoucesTestPlans = new LinkedHashMap<>(
                testPlanDestPathToResourcePathMap.size());
            for (Entry<String, String> entry : testPlanDestPathToResourcePathMap.entrySet()) {
                fromResoucesTestPlans.put(
                    concatPaths(testPlanDir, entry.getKey()), entry.getValue());
            }
            testPlanDestPathToResourcePathMap = fromResoucesTestPlans;

            if (outputFile != null) {
                outputFile = concatPaths(installDir, outputFile);
                jmeterDeployFlowContextBuilder.outputFile(outputFile);
            }
            if (jmeterProperties != null) {
                jmeterDeployFlowContextBuilder.jmeterProperties(this.jmeterProperties);
            }
            if (logFile != null) {
                logFile = concatPaths(installDir, logFile);
                jmeterDeployFlowContextBuilder.logFile(logFile);
            }
            if (jmeterLogFile != null) {
                jmeterLogFile = concatPaths(installDir, jmeterLogFile);
                jmeterDeployFlowContextBuilder.jmeterLogFile(jmeterLogFile);
            }

            deployJMeterFlowContext = jmeterDeployFlowContextBuilder.build();
        }

        protected void initStartStop() {
            initStartCommandFlowContext();
            initStopCommandFlowContext();
        }

        protected void initStartCommandFlowContext() {
            assert deployJMeterFlowContext != null;
            startCommandFlowContext =
                initCommandFlowContext(deployJMeterFlowContext.getStartCommand(),
                    deployJMeterFlowContext.getStartCommandParams(), true);
            getEnvProperties().add(ENV_JMETER_START, startCommandFlowContext);
            startRole = startServerRole(tasResolver, startCommandFlowContext);
        }

        protected void initStopCommandFlowContext() {
            assert deployJMeterFlowContext != null;
            stopCommandFlowContext =
                initCommandFlowContext(deployJMeterFlowContext.getStopCommand(), null, false);
            getEnvProperties().add(ENV_JMETER_STOP, stopCommandFlowContext);
            stopRole = stopServerRole(tasResolver, stopCommandFlowContext);
        }

        protected RunCommandFlowContext initCommandFlowContext(String command, List<String> args,
            boolean prependWorkingDirectory) {
            Args.notNull(deployJMeterFlowContext,
                "JMeter flow context must be already set to init run command flow");
            Map<String, String> environment = Collections.singletonMap("JM_LAUNCH",
                deployJMeterFlowContext.getJmLaunch());
            RunCommandFlowContext.Builder runCommandFlowContextBuilder =
                new RunCommandFlowContext.Builder(command)
                    .environment(environment)
                    .workDir(deployJMeterFlowContext.getJMeterBinDir())
                    .name(roleId);
            if (args != null) {
                runCommandFlowContextBuilder = runCommandFlowContextBuilder.args(args);
            }
            if (!prependWorkingDirectory) {
                runCommandFlowContextBuilder =
                    runCommandFlowContextBuilder.doNotPrependWorkingDirectory();
            }
            return runCommandFlowContextBuilder.build();
        }

        private AbstractRole startServerRole(ITasResolver tasResolver,
            RunCommandFlowContext context) {
            UniversalRole startServerRole =
                new UniversalRole.Builder(roleId + "_startRole", tasResolver)
                    .runFlow(StartJMeterFlow.class, context)
                    .build();
            return startServerRole;
        }

        private AbstractRole stopServerRole(ITasResolver tasResolver,
            RunCommandFlowContext context) {
            UniversalRole stopServerRole =
                new UniversalRole.Builder(roleId + "_stopRole", tasResolver)
                    .runFlow(StopJMeterFlow.class, context)
                    .build();
            return stopServerRole;
        }

        @Override
        protected JMeterRole getInstance() {
            return new JMeterRole(this);
        }

        public Builder jmeterArtifact(Artifact jmeterArtifact, JMeterVersion jmeterVersion) {
            this.jmeterArtifact = jmeterArtifact;
            this.jmeterVersion = jmeterVersion;
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder jmeterVersion(JMeterVersion jmeterVersion) {
            Args.notNull(jmeterVersion, "JMeter version");
            this.jmeterArtifact = jmeterVersion.getArtifact();
            this.jmeterVersion = jmeterVersion;
            return builder();
        }

        public Builder installDir(@NotNull String installDir) {
            this.installDir = concatPaths(getDeployBase(), installDir);
            getEnvProperties().add(new TestProperty<>("jmeter.home", this.installDir));
            return builder();
        }

        public Builder jdkHomeDir(@NotNull String jdkHomeDir) {
            this.jdkHomeDir = jdkHomeDir;
            return builder();
        }

        public Builder customJava(JavaRole customJava) {
            Args.notNull(customJava, "customJava");
            this.customJava = customJava;
            this.jdkHomeDir = customJava.getInstallDir();
            return builder();
        }

        public Builder jmeterScriptsArchive(ITasArtifact artifact) {
            Args.notNull(artifact, "jmeterScriptsArchive");
            this.scriptsArtifact = artifact;
            return builder();
        }

        public Builder autoStart() {
            this.autoStart = true;
            return builder();
        }

        public Builder addJMeterExtension(ITasArtifact iTasArtifact) {
            Args.notNull(iTasArtifact, "Artifact");
            addJMeterExtension = true;
            extensionArtifacts.add(iTasArtifact);
            return builder();
        }


        public Builder testPlan(String testPlan) {
            Args.notNull(testPlan, "Test plan");
            this.testPlan = testPlan;
            return builder();
        }

        /**
         * Copies JMeter test plan from resources provided in classpath to the target
         * path specified by <code>testPlanTargetPath</code>.
         *
         * @param testPlanTargetPath
         * @param testPlanResourcePath
         * @return
         */
        public Builder testPlanResource(String testPlanTargetPath, String testPlanResourcePath) {
            Args.notNull(testPlanTargetPath, "Test plan path");
            Args.notNull(testPlanResourcePath, "Test plan resource path");
            testPlanDestPathToResourcePathMap.put(testPlanTargetPath, testPlanResourcePath);
            return builder();
        }

        public Builder testPlanArchive(ITasArtifact testPlanArchive) {
            Args.notNull(testPlanArchive, "Test plan archive");
            this.testPlanArchive = testPlanArchive;

            return builder();
        }


        public Builder jmeterLogFile(String jmeterLogFile) {
            Args.notNull(jmeterLogFile, "JMeter log file");
            this.jmeterLogFile = jmeterLogFile;
            return builder();
        }

        public Builder logFile(String logFile) {
            Args.notNull(logFile, "JMeter samples log file");
            this.logFile = logFile;
            return builder();
        }

        public Builder outputFile(String outputFile) {
            Args.notNull(outputFile, "JMeter output file");
            this.outputFile = outputFile;
            return builder();
        }

        public Builder jmeterProperties(Map<String, String> jmeterProperties) {
            Args.notNull(jmeterProperties, "JMeter properties");
            this.jmeterProperties = new LinkedHashMap<>(jmeterProperties);
            return builder();
        }
    }

}
