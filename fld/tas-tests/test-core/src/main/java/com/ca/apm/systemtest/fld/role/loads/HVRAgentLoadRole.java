/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.systemtest.fld.artifact.EmClient14Artifact;
import com.ca.apm.systemtest.fld.artifact.EmClientArtifact;
import com.ca.apm.systemtest.fld.artifact.HVRAgentArtifact;
import com.ca.apm.systemtest.fld.artifact.IntroscopeCommonArtifact;
import com.ca.apm.systemtest.fld.artifact.IsengardClientArtifact;
import com.ca.apm.systemtest.fld.artifact.JDBCFeatureArtifact;
import com.ca.apm.systemtest.fld.artifact.WilyCoreArtifact;
import com.ca.apm.systemtest.fld.artifact.Win32HelperAgentArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.artifact.Artifact;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Min;

/**
 * Adaptation of HVRAgentRole for use in FLD
 * @author keyja01
 *
 */
public class HVRAgentLoadRole extends AbstractFldLoadRole {
    public enum Mode {
        REPLAY, EXTRACT
    }

    public static final String START_HVR_LOAD_KEY = "START_HVR_LOAD";
    public static final String STOP_HVR_LOAD_KEY = "STOP_HVR_LOAD";
    private GenericFlowContext hvrAgentFlowContext;
    private Set<GenericFlowContext> libraryArtifactsFlowsCtxs;
    private boolean start = false;
    private RunCommandFlowContext startLoadContext;
    
    public HVRAgentLoadRole(Builder builder) {
        super(builder.getRoleId(), builder.getEnvProperties());
        hvrAgentFlowContext = builder.hvrAgentFlowContext;
        libraryArtifactsFlowsCtxs = builder.libraryArtifactsFlowsCtxs;
        start = builder.start;
        startLoadContext = builder.startLoadContext;
    }
    
    public static class Builder extends FldLoadBuilderBase<Builder, HVRAgentLoadRole> {
        public static final String LIB_PATH = "\\lib\\";
        private GenericFlowContext hvrAgentFlowContext;
        private GenericFlowContext win32HelperAgentContext;
        private Set<GenericFlowContext> libraryArtifactsFlowsCtxs = new HashSet<>(10);
        private Set<Artifact> metricsArtifacts = new HashSet<>(1);
        private RunCommandFlowContext.Builder runHvrAgentFlowContext;
        private RunCommandFlowContext.Builder stopHvrAgentFlowContext;
        
        protected String version;
        private int javaMaxHeap = 1024;
        private String host = "localhost";
        private String port = "5001";
        private String user = "Admin";
        private String password = "\"\"";
        private String loadfile = "extract";
        private int cloneconnections = 1;
        private int cloneagents = 1;
        private int secondspertrace = 15;
        private String agenthost = null;

        private Mode mode = Mode.REPLAY;
        private String deployPath;
        private UUID hvrInstanceUuid;
        private boolean hideConsole = false;
        private boolean start = false;

        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            hvrInstanceUuid = UUID.randomUUID();
            startLoadKey = START_HVR_LOAD_KEY;
            stopLoadKey = STOP_HVR_LOAD_KEY;
        }

        
        @Override
        protected void preBuildInit() {
            initFlowContextBuilder();
            initLibArtifacts();
        }
        
        @Override
        protected HVRAgentLoadRole buildRole() {
            HVRAgentLoadRole role = getInstance();
            return role;
        }


        @Override
        protected RunCommandFlowContext.Builder createStartLoadFlowContextBuilder() {
            String hvrInstanceUuidStr = hvrInstanceUuid.toString();
            String win32HelperAgentJar = win32HelperAgentContext.getDestination();

            List<String> args = new ArrayList<>(
                Arrays.asList(
                    "java",
                    String.format("-Xmx%dm", javaMaxHeap),
                    "-javaagent:" + win32HelperAgentJar,
                    "-Dwin32.console.title=" + hvrInstanceUuidStr + " ",
                    "-Dwin32.console.hide=" + hideConsole,
                    "-classpath",
                    "HVRAgent.jar;lib\\*",
                    "com.wily.introscope.tools.fakeagent.FakeAgent",
                    mode.name().toLowerCase(),
                    "-host", host,
                    "-port", port,
                    "-username", user,
                    "-password", password,
                    "-loadfile", loadfile,
                    "-cloneconnections", Integer.toString(cloneconnections),
                    "-cloneagents", Integer.toString(cloneagents),
                    "-secondspertrace", Integer.toString(secondspertrace)));

            if (StringUtils.isNotBlank(agenthost)) {
                args.add("-agenthost");
                args.add(agenthost);
            }

            runHvrAgentFlowContext =
                new RunCommandFlowContext.Builder("start")
                    .doNotPrependWorkingDirectory()
                    .workDir(deployPath)
                    .args(args);
            
            return runHvrAgentFlowContext;
        }

        @Override
        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
            String hvrInstanceUuidStr = hvrInstanceUuid.toString();

            stopHvrAgentFlowContext =
                new RunCommandFlowContext.Builder("taskkill")
                    .doNotPrependWorkingDirectory()
                    .workDir(deployPath)
                    .args(Arrays.asList(
                        "/F",
                        "/T",
                        "/FI",
                        "WINDOWTITLE eq " + hvrInstanceUuidStr + "*"));
            return stopHvrAgentFlowContext;
        }
        
        private void initFlowContextBuilder() {
            Artifact hvrAgentArtifact =
                new HVRAgentArtifact(tasResolver).createArtifact(version).getArtifact();
            URL artifactURL = tasResolver.getArtifactUrl(hvrAgentArtifact);
            deployPath = getDeployBase() + TasFileUtils.getBasename(artifactURL);
            hvrAgentFlowContext =
                new GenericFlowContext.Builder().artifactUrl(artifactURL).destination(deployPath)
                    .build();
        }

        private GenericFlowContext createFlowContextForArtifact(Artifact artifact, String path,
                boolean archive) {

            URL artifactURL = tasResolver.getArtifactUrl(artifact);

            GenericFlowContext.Builder builder =
                new GenericFlowContext.Builder().artifactUrl(artifactURL).destination(
                    archive ? path : path + artifact.getArtifactId() + "."
                        + artifact.getExtension());

            if (!archive) {
                builder.notArchive();
            }

            GenericFlowContext gfc = builder.build();
            libraryArtifactsFlowsCtxs.add(gfc);
            return gfc;
        }

        private void initLibArtifacts() {
            // deploys jars fro classpath into lib folder
            String pth = deployPath + LIB_PATH;
            createFlowContextForArtifact(new WilyCoreArtifact(tasResolver).createArtifact(version)
                .getArtifact(), pth, false);

            createFlowContextForArtifact(
                new IntroscopeCommonArtifact(tasResolver).createArtifact(version).getArtifact(),
                pth, false);


            createFlowContextForArtifact(new EmClientArtifact(tasResolver).createArtifact(version)
                .getArtifact(), pth, false);

            createFlowContextForArtifact(new EmClient14Artifact(tasResolver)
                .createArtifact(version).getArtifact(), pth, false);

            createFlowContextForArtifact(
                new IsengardClientArtifact(tasResolver).createArtifact(version).getArtifact(), pth,
                false);

            createFlowContextForArtifact(
                new JDBCFeatureArtifact(tasResolver).createArtifact(version).getArtifact(), pth,
                false);
            
            win32HelperAgentContext = createFlowContextForArtifact(
                new Win32HelperAgentArtifact(tasResolver).createArtifact(version).getArtifact(),
                deployPath + "\\", false);

            // metrics artifacts are on root of HVRAgent deploy
            // also its supposed that all metrics are in .zip archives
            for (Artifact a : metricsArtifacts) {
                createFlowContextForArtifact(a, deployPath, true);
            }

        }

        public Builder version(String version) {
            this.version = version;
            return builder();
        }
        
        
        public Builder hideConsole() {
            this.hideConsole = true;
            return this;
        }

        public Builder addMetricsArtifact(ITasArtifactFactory metricsArtifact) {
            Artifact artifact = metricsArtifact.createArtifact().getArtifact();
            metricsArtifacts.add(artifact);
            return builder();
        }
        
        
        public Builder addMetricsArtifact(Artifact artifact) {
            metricsArtifacts.add(artifact);
            return this;
        }

        public Builder emHost(String host) {
            this.host = host;
            return builder();
        }

        public Builder emPort(String port) {
            this.port = port;
            return builder();
        }

        public Builder user(String user) {
            this.user = user;
            return builder();
        }

        public Builder loadFile(String filename) {
            this.loadfile = filename;
            return builder();
        }

        public Builder agentHost(String agenthost) {
            this.agenthost = agenthost;
            return builder();
        }
        
        public Builder cloneconnections(@Min(value = 1) int cloneconnections) {
            this.cloneconnections = cloneconnections;
            return builder();
        }

        public Builder cloneagents(@Min(value = 1) int cloneagents) {
            this.cloneagents = cloneagents;
            return builder();
        }

        public Builder secondspertrace(@Min(value = 1) int secondspertrace) {
            this.secondspertrace = secondspertrace;
            return builder();
        }

        /**
         * @param maxHeap Maximum heap in MiB for Java process.
         * @return Builder
         */
        public Builder maxHeap(int maxHeap) {
            this.javaMaxHeap = maxHeap;
            return builder();
        }

        public Builder extract() {
            this.mode = Mode.EXTRACT;
            return builder();
        }

        public Builder replay() {
            this.mode = Mode.REPLAY;
            return builder();
        }

        public Builder start() {
            this.start = true;
            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected HVRAgentLoadRole getInstance() {
            return new HVRAgentLoadRole(this);
        }
        
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, GenericFlow.class, hvrAgentFlowContext);
        for (GenericFlowContext ctx: libraryArtifactsFlowsCtxs) {
            runFlow(aaClient, GenericFlow.class, ctx);
        }

        if (start) {
            runCommandFlow(aaClient, startLoadContext);
        }
    }

}
