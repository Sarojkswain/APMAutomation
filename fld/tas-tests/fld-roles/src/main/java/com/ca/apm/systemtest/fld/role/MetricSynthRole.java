/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.systemtest.fld.artifact.MetricSynthArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.H2DatabaseVersion;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author KEYJA01
 *
 */
public class MetricSynthRole extends AbstractRole {
    private RunCommandFlowContext runContext;
    private GenericFlowContext downloadCtx;
    private GenericFlowContext scenarioCtx;
    private GenericFlowContext h2DownloadCtx;
    private boolean autostart;
    private RunCommandFlowContext initDbCtx;
    private FileModifierFlowContext modifyCtx;
    
    public static class Builder extends BuilderBase<Builder, MetricSynthRole> {
        private static final String METRIC_SYNTH_START = "metric_synth_start";
        private String roleId;
        private ITasResolver tasResolver;
        protected String installDir;
        private int listenPort = 8080;
        private int maxHeapSizeMB = 1024;
        private String version;
        private boolean autostart = false;
        private RunCommandFlowContext runContext;
        private RunCommandFlowContext initDbContext;
        private ITasArtifact artifact;
        private GenericFlowContext downloadCtx;
        private String url;
        private Artifact scenario;
        private GenericFlowContext scenarioCtx;
        private GenericFlowContext h2DownloadCtx;
        private String dbFile;
        private FileModifierFlowContext modifyCtx;
        private Map<String, String> replaceMap;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }
        
        @Override
        public MetricSynthRole build() {
            artifact = new MetricSynthArtifact(tasResolver).createArtifact(version);
            
            String destDir = deployDir();
            String destFile = destDir + getPathSeparator() + "MetricSynth.jar";
            downloadCtx = new GenericFlowContext.Builder(tasResolver.getArtifactUrl(artifact))
                .notArchive().destination(destFile)
                .build();
            
            if (dbFile != null) {
                dbFile = "./db/" + dbFile;
                this.url = "jdbc:h2:" + dbFile + ";DB_CLOSE_ON_EXIT=FALSE";
            }
            
            if (scenario != null) {
                String h2File = destDir + getPathSeparator() + H2DatabaseVersion.v1_4_196.getFilename();
                h2DownloadCtx = new GenericFlowContext.Builder(tasResolver.getArtifactUrl(H2DatabaseVersion.v1_4_196.getArtifact()))
                    .destination(h2File).notArchive().build();
                
                String scenarioDestDir = destDir + getPathSeparator() + "scenario";
                scenarioCtx = new GenericFlowContext.Builder(tasResolver.getArtifactUrl(scenario))
                    .destination(scenarioDestDir).build();
                
                Map<String, String> replacePairs = new HashMap<>();
                if (replaceMap != null) {
                    replacePairs.putAll(replaceMap);
                }
                modifyCtx = new FileModifierFlowContext.Builder()
                    .replace(scenarioDestDir + getPathSeparator() + "load-scenario.sql", replacePairs)
                    .build();
                
                List<String> args = new ArrayList<>();
                args.add("-cp");
                args.add(h2File);
                args.add("org.h2.tools.RunScript");
                args.add("-url");
                args.add(url);
                args.add("-script");
                args.add(scenarioDestDir + getPathSeparator() + "load-scenario.sql");
                initDbContext = new RunCommandFlowContext.Builder(getJavaExecutable())
                    .workDir(destDir)
                    .doNotPrependWorkingDirectory().args(args)
                    .build();
            }
            
            // java -Xmx 2048m -Dserver.port=8080 -jar <pathToWar> 
            ArrayList<String> args = new ArrayList<>();
            args.add("-server");
            args.add("-XX:CompressedClassSpaceSize=256m");
            args.add("-Dmax.executor.threads=10");
            args.add("-Dserver.tomcat.max-threads=50");
            if (url != null) {
                args.add("-Dspring.datasource.url=\"" + url + "\"");
            }
//            args.add("-XX:G1ReservePercent=20");
            args.add("-Xmx" + Integer.toString(maxHeapSizeMB) + "m");
            if (listenPort != 8080) {
                args.add("-Dserver.port=" + listenPort);
            }
            args.add("-jar");
            args.add(destFile);
            runContext = new RunCommandFlowContext.Builder(getJavaExecutable())
                .doNotPrependWorkingDirectory()
                .workDir(destDir)
                .args(args).terminateOnMatch("Started Application in")
                .build();
            
            getEnvProperties().add(METRIC_SYNTH_START, runContext);
            
            return getInstance();
        }

        protected String deployDir() {
            return getDeployBase() + getPathSeparator() + installDir;
        }

        protected String getJavaExecutable() {
            return "javaw.exe";
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MetricSynthRole getInstance() {
            return new MetricSynthRole(this);
        }
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this;
        }
        
        public Builder databaseFile(String dbFile) {
            this.dbFile = dbFile;
            return this;
        }
        
        /**
         * Perform text substitutions
         * @param collector
         * @return
         */
        public Builder collectorMap(Map<String, String> replaceMap) {
            this.replaceMap = replaceMap;
            return this;
        }
        
        public Builder listenPort(int listenPort) {
            this.listenPort = listenPort;
            return this;
        }
        
        public Builder maxHeap(int maxHeapSizeMB) {
            this.maxHeapSizeMB = maxHeapSizeMB;
            return this;
        }
        
        public Builder metricSynthVersion(String version) {
            this.version = version;
            return this;
        }
        
        public Builder loadScenario(Artifact scenario) {
            this.scenario = scenario;
            return this;
        }
        
        /**
         * Automatically start the metric synth app
         * @return
         */
        public Builder autostart() {
            this.autostart = true;
            return this;
        }
    }
    
    
    public static class LinuxBuilder extends Builder {
        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }
        
        @Override
        protected String getJavaExecutable() {
            return "java";
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
    
    
    public MetricSynthRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        runContext = builder.runContext;
        downloadCtx = builder.downloadCtx;
        autostart = builder.autostart;
        h2DownloadCtx = builder.h2DownloadCtx;
        initDbCtx = builder.initDbContext;
        scenarioCtx = builder.scenarioCtx;
        modifyCtx = builder.modifyCtx;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, GenericFlow.class, downloadCtx);
        if (scenarioCtx != null) {
            // download the scenario
            runFlow(aaClient, GenericFlow.class, scenarioCtx);
            
            // download h2 jar
            runFlow(aaClient, GenericFlow.class, h2DownloadCtx);
            
            runFlow(aaClient, modifyCtx);
            
            // and init the DB
            runFlow(aaClient, initDbCtx);
        }
        
        if (autostart) {
            runFlow(aaClient, runContext);
        }
    }

}
