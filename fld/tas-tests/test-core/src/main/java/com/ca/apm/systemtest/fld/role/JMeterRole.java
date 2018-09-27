package com.ca.apm.systemtest.fld.role;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.flow.DeployJMeterFlow;
import com.ca.apm.systemtest.fld.flow.DeployJMeterFlowContext;
import com.ca.apm.systemtest.fld.flow.StartJMeterFlow;
import com.ca.apm.systemtest.fld.flow.StopJMeterFlow;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.TestProperty;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;

/**
 * This role represents Apache JMeter.
 * 
 * @author Boch, Tomas (bocto01@ca.com)
 */
public class JMeterRole extends AbstractRole {
    @TasEnvironmentPropertyKey
    public static final String ENV_JMETER_START = "startJMeter";

    @TasEnvironmentPropertyKey
    public static final String ENV_JMETER_STOP = "stopJMeter";

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
        this.testPlan = builder.testPlan;
        this.testPlanArchive = builder.testPlanArchive;
        this.jmeterLogFile = builder.jmeterLogFile;
        this.logFile = builder.logFile;
        this.outputFile = builder.outputFile;
        this.jmeterProperties = builder.jmeterProperties;
        this.installDir = builder.installDir;
        this.testPlanDestPathToResourcePathMap = builder.testPlanDestPathToResourcePathMap;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployJMeter(aaClient);
        deployJMeterExtensions(aaClient);
        deployTestPlanArchive(aaClient);
        deployTestPlanResources(aaClient);
    }

    protected void deployTestPlanResources(IAutomationAgentClient aaClient) {
        if (testPlanDestPathToResourcePathMap != null && !testPlanDestPathToResourcePathMap.isEmpty()) {
            FileModifierFlowContext.Builder confResourceFlowContextBuilder = new FileModifierFlowContext.Builder();
            for (Entry<String, String> testPlanResourceEntry : testPlanDestPathToResourcePathMap.entrySet()) {
                confResourceFlowContextBuilder.resource(testPlanResourceEntry.getKey(), testPlanResourceEntry.getValue());
            }
            
            FileModifierFlowContext testPlanResourceCreatorContext = confResourceFlowContextBuilder.build();
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
                .destination(installDir + "/testplan")
                .build();
            runFlow(aaClient, GenericFlow.class, ctx);
        }
    }
    
    

    private static String getTargetFilename(URL artifactUrl, Artifact artifact) {
        // This is because org.eclipse.aether.artifact.DefaultArtifact (implementation of
        // org.eclipse.aether.artifact.Artifact) returns null for getFile()
        return TasFileUtils.getBasename(artifactUrl)
            + (StringUtils.hasText(artifact.getExtension()) ? ("." + artifact.getExtension()) : "");
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

    @Nullable
    public String getJMeterLogFile() {
        return jmeterLogFile;
    }

    @Nullable
    public String getLogFile() {
        return logFile;
    }
    
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
            defaultVersionedDir = getLinuxDeployBase() + DEFAULT_VERSIONED_INST_DIR;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    public static class Builder extends BuilderBase<Builder, JMeterRole> {
        protected static final String DEFAULT_VERSIONED_INST_DIR = "apache-jmeter-%s";

        private final String roleId;
        private final ITasResolver tasResolver;
        protected DeployJMeterFlowContext deployJMeterFlowContext;
        protected DeployJMeterFlowContext.Builder jmeterDeployFlowContextBuilder;
        protected RunCommandFlowContext startCommandFlowContext;
        protected AbstractRole startRole;
        protected RunCommandFlowContext stopCommandFlowContext;
        protected AbstractRole stopRole;
        protected String defaultVersionedDir;
        protected String installDir;
        protected ITasArtifact scriptsArtifact;
        private JMeterVersion jmeterVersion;
        private Artifact jmeterArtifact;
        private JavaRole customJava;
        private boolean autoStart;
        private boolean addJMeterExtension = false;
        private final Collection<ITasArtifact> extensionArtifacts = new ArrayList<>();
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
            defaultVersionedDir = getWinDeployBase() + DEFAULT_VERSIONED_INST_DIR;
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
            assert jmeterArtifact != null;
            jmeterDeployFlowContextBuilder = new DeployJMeterFlowContext.Builder();
            
            if (installDir == null) {
                jmeterDeployFlowContextBuilder.installDir(String.format(defaultVersionedDir,
                    jmeterArtifact.getVersion()));
            } else {
                jmeterDeployFlowContextBuilder.installDir(installDir);
            }
            URL artifactUrl = tasResolver.getArtifactUrl(jmeterArtifact);
            
            jmeterDeployFlowContextBuilder.jmeterBinariesArtifactURL(artifactUrl);
            
            if (scriptsArtifact != null) {
                jmeterDeployFlowContextBuilder.jmeterScriptsArchive(tasResolver.getArtifactUrl(scriptsArtifact));
            }
            if (jdkHomeDir != null) {
                jmeterDeployFlowContextBuilder.jdkHomeDir(jdkHomeDir);
            }
            if (testPlan != null) {
                jmeterDeployFlowContextBuilder.testPlan(this.testPlan);
            }
            if (outputFile != null) {
                jmeterDeployFlowContextBuilder.outputFile(outputFile);
            }
            if (jmeterProperties != null) {
                jmeterDeployFlowContextBuilder.jmeterProperties(this.jmeterProperties);
            }
            if (logFile != null) {
                jmeterDeployFlowContextBuilder.logFile(logFile);
            }
            if (jmeterLogFile != null) {
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
            assert deployJMeterFlowContext != null : "JMeter flow context must be already set to init run command flow";
            Map<String, String> environment = new HashMap<>();
            environment.put("JM_LAUNCH", deployJMeterFlowContext.getJmLaunch());
            RunCommandFlowContext.Builder runCommandFlowContextBuilder =
                new RunCommandFlowContext.Builder(command).environment(environment)
                    .workDir(deployJMeterFlowContext.getJMeterBinDir()).name(roleId);
            if (args != null) {
                runCommandFlowContextBuilder = runCommandFlowContextBuilder.args(args);
            }
            if (!prependWorkingDirectory) {
                runCommandFlowContextBuilder =
                    runCommandFlowContextBuilder.doNotPrependWorkingDirectory();
            }
            return runCommandFlowContextBuilder.build();
        }

        private AbstractRole startServerRole(ITasResolver tasResolver, RunCommandFlowContext context) {
            UniversalRole startServerRole =
                new UniversalRole.Builder(roleId + "_startRole", tasResolver).runFlow(StartJMeterFlow.class,
                    context).build();
            return startServerRole;
        }

        private AbstractRole stopServerRole(ITasResolver tasResolver, RunCommandFlowContext context) {
            UniversalRole stopServerRole =
                new UniversalRole.Builder(roleId + "_stopRole", tasResolver).runFlow(StopJMeterFlow.class,
                    context).build();
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
            return this;
        }

        public Builder installDir(@NotNull String installDir) {
            File path = new File(getDeployBase(), installDir);
            this.installDir = path.toString();
            getEnvProperties().add(new TestProperty<>("jmeter.home", this.installDir));
            return this;
        }

        public Builder jdkHomeDir(@NotNull String jdkHomeDir) {
            this.jdkHomeDir = jdkHomeDir;
            return this;
        }

        public Builder customJava(JavaRole customJava) {
            Args.notNull(customJava, "customJava");
            this.customJava = customJava;
            this.jdkHomeDir = customJava.getInstallDir();
            return this;
        }
        
        public Builder jmeterScriptsArchive(ITasArtifact artifact) {
            Args.notNull(artifact, "jmeterScriptsArchive");
            this.scriptsArtifact = artifact;
            return this;
        }

        public Builder autoStart() {
            this.autoStart = true;
            return this;
        }

        public Builder addJMeterExtension(ITasArtifact iTasArtifact) {
            Args.notNull(iTasArtifact, "Artifact");
            addJMeterExtension = true;
            extensionArtifacts.add(iTasArtifact);
            return this;
        }

        
        /**
         * @param testPlan
         * @return
         * @deprecated - use testPlan() instead
         */
        @Deprecated
        public Builder runTestPlan(String testPlan) {
            return testPlan(testPlan);
        }
        
        public Builder testPlan(String testPlan) {
            Args.notNull(testPlan, "Test plan");
            
            this.testPlan = installDir + "/testplan/" + testPlan;
            return this;
        }
        
        /**
         * Copies JMeter test plan from resources provided in classpath to the target 
         * path specified by <code>testPlanTargetPath</code>.
         *  
         * @param testPlanTargetPath
         * @param testPlanResourceURI
         * @return 
         */
        public Builder testPlanResource(String testPlanTargetPath, String testPlanResourcePath) {
            Args.notNull(testPlanTargetPath, "Test plan path");
            Args.notNull(testPlanResourcePath, "Test plan resource path");
            String fullPath = Paths.get(installDir, testPlanTargetPath).toString();
            testPlanDestPathToResourcePathMap.put(fullPath, testPlanResourcePath);
            return this;
        }
        
        public Builder testPlanArchive(ITasArtifact testPlanArchive) {
            Args.notNull(testPlanArchive, "Test plan archive");
            this.testPlanArchive = testPlanArchive;
            
            return this;
        }
        

        public Builder jmeterLogFile(String jmeterLogFile) {
            Args.notNull(jmeterLogFile, "JMeter log file");
            this.jmeterLogFile = installDir + "/" + jmeterLogFile;
            return this;
        }

        public Builder logFile(String logFile) {
            Args.notNull(logFile, "JMeter samples log file");
            this.logFile = installDir + "/" + logFile;
            return this;
        }
        
        public Builder outputFile(String outputFile) {
            Args.notNull(outputFile, "JMeter output file");
            this.outputFile = installDir + "/" + outputFile;
            return this;
        }

        public Builder jmeterProperties(Map<String, String> jmeterProperties) {
            Args.notNull(jmeterProperties, "JMeter properties");
            this.jmeterProperties = new HashMap<>(jmeterProperties);
            return this;
        }
    }

}
