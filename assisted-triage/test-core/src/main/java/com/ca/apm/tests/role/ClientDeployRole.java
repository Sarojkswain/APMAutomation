package com.ca.apm.tests.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * @author banra06
 */
public class ClientDeployRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDeployRole.class);
    private ITasResolver tasResolver;

    private String emHost;
    public static final String STRESSAPP_START_LOAD = "stressapploadStart";
    public static final String JMETER_START_LOAD = "jmeterloadStart";

    protected ClientDeployRole(Builder builder) {

        super(builder.roleId, builder.getEnvProperties());
        this.tasResolver = builder.tasResolver;
        this.emHost = builder.emHost;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {

        deployJmeter(aaClient);
        deployStressApp(aaClient);
        deployDefaultagent(aaClient);
        createLoadBatchFile(aaClient);
        updateAgentProfile(aaClient);
        downloadJmeterScript(aaClient);
    }

    private void downloadJmeterScript(IAutomationAgentClient aaClient) {
        // download jmx file
        URL url =
            tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", "AssistedTriage",
                "ATST_Load", "zip", "1.0"));
        LOGGER.info("Downloading artifact " + url.toString());

        GenericFlowContext context =
            new GenericFlowContext.Builder().artifactUrl(url)
                .destination(TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/").build();
        runFlow(aaClient, GenericFlow.class, context);
        // change host to master
        FileModifierFlowContext context1 = null;
        Map<String, String> replacePairs = new HashMap<String, String>();
        replacePairs.put("localhost", emHost);
        String fileName = TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/ATST_Load.jmx";
        context1 = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context1);
        // create jmeter batch file
        Collection<String> createBatch =
            Arrays.asList("cd " + TasBuilder.WIN_SOFTWARE_LOC
                + "/jmeter/apache-jmeter-2.11/bin && jmeter -n -t " + TasBuilder.WIN_SOFTWARE_LOC
                + "/jmeter/ATST_Load.JMX");
        FileModifierFlowContext BatchFile =
            new FileModifierFlowContext.Builder().create(
                TasBuilder.WIN_SOFTWARE_LOC + "/jmeter.bat", createBatch).build();
        runFlow(aaClient, FileModifierFlow.class, BatchFile);
    }

    private void deployDefaultagent(IAutomationAgentClient aaClient) {

        String artifact = "agent-noinstaller-default-windows";
        deployZipArtifact(aaClient, "com.ca.apm.delivery", artifact, "", "stressapp");
    }

    private void createLoadBatchFile(IAutomationAgentClient aaClient) {
        String parameters =
            "/stressapp/StressApp.jar doSQL=true,doWideStructure=true,doErrors=true,doRandom=true,maxAppIndex=250,maxMetricIndex=50,stackDepth=10,maxBackendIndex=10,minSleepOnMethods=10,stallThreshold=650,sleepOnMethods=70,testDuration=1209600000,threadServiceMaxThreads=100,numberOfConcurrentUsers=10";
        Collection<String> createBatch =
            Arrays
                .asList("java -javaagent:"
                    + TasBuilder.WIN_SOFTWARE_LOC
                    + "/stressapp/wily/Agent.jar -Dcom.wily.introscope.agentProfile="
                    + TasBuilder.WIN_SOFTWARE_LOC
                    + "/stressapp/wily/core/config/IntroscopeAgent.profile -Dcom.wily.autoprobe.logSizeInKB=100000  -Duser.dir="
                    + TasBuilder.WIN_SOFTWARE_LOC
                    + "/stressapp  -Dlog4j.configuration=file:"
                    + TasBuilder.WIN_SOFTWARE_LOC
                    + "/stressapp/resources/log4j-StressApp.properties -classpath "
                    + TasBuilder.WIN_SOFTWARE_LOC
                    + "/stressapp/lib/* -Xms256m -Xmx512m -XX:PermSize=20m -XX:MaxPermSize=30m -XX:+UseSerialGC -XX:+HeapDumpOnOutOfMemoryError -verbosegc -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:"
                    + TasBuilder.WIN_SOFTWARE_LOC
                    + "/stressapp/RemoteConfig_agent_true.gc.log -Dcom.wily.autoprobe.logSizeInKB=100000 -jar "
                    + TasBuilder.WIN_SOFTWARE_LOC + parameters);

        FileModifierFlowContext BatchFile =
            new FileModifierFlowContext.Builder().create(TasBuilder.WIN_SOFTWARE_LOC + "/load.bat",
                createBatch).build();
        runFlow(aaClient, FileModifierFlow.class, BatchFile);
    }

    private void updateAgentProfile(IAutomationAgentClient aaClient) {

        FileModifierFlowContext context = null;
        Map<String, String> replacePairs = new HashMap<String, String>();
        String prop = "agentManager.url.1=" + emHost + ":5001";
        replacePairs.put("agentManager.url.1=localhost:5001", prop);
        replacePairs.put("#introscope.agent.customProcessName=CustomProcessName",
            "introscope.agent.customProcessName=ErrorStallProcess");
        replacePairs.put("#introscope.agent.agentName=AgentName",
            "introscope.agent.agentName=ErrorStallAgent");
        replacePairs.put("introscope.agent.stalls.thresholdseconds=30",
            "introscope.agent.stalls.thresholdseconds=10");
        replacePairs.put("introscope.agent.stalls.resolutionseconds=10",
            "introscope.agent.stalls.resolutionseconds=5");

        String fileName =
            TasBuilder.WIN_SOFTWARE_LOC + "stressapp/wily/core/config/IntroscopeAgent.profile";

        context = new FileModifierFlowContext.Builder().replace(fileName, replacePairs).build();
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    private void deployStressApp(IAutomationAgentClient aaClient) {

        deployZipArtifact(aaClient, "stressapp", "jvm8-dist");
    }

    private void deployZipArtifact(IAutomationAgentClient aaClient, String artifactId,
        String classifier) {

        deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-tools", artifactId, classifier,
            artifactId);
    }

    private void deployZipArtifact(IAutomationAgentClient aaClient, String groupId,
        String artifactId, String classifier, String homeDir) {

        URL url =
            tasResolver.getArtifactUrl(new DefaultArtifact(groupId, artifactId, classifier, "zip",
                tasResolver.getDefaultVersion()));
        LOGGER.info("Downloading zip artifact " + url.toString());

        GenericFlowContext context =
            new GenericFlowContext.Builder().artifactUrl(url)
                .destination(TasBuilder.WIN_SOFTWARE_LOC + "/" + homeDir).build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    private void deployJmeter(IAutomationAgentClient aaClient) {

        URL url =
            tasResolver.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries", "apache-jmeter",
                "", "zip", "2.13"));
        LOGGER.info("Downloading artifact " + url.toString());

        GenericFlowContext context =
            new GenericFlowContext.Builder().artifactUrl(url)
                .destination(TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/apache-jmeter-2.11").build();
        runFlow(aaClient, GenericFlow.class, context);
    }

    public static class Builder extends BuilderBase<Builder, ClientDeployRole> {

        private final String roleId;
        private final ITasResolver tasResolver;
        private String emHost = "localhost";

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }

        @Override
        public ClientDeployRole build() {
            startStressLoad();
            startJmeterLoad();
            return getInstance();
        }

        @Override
        protected ClientDeployRole getInstance() {
            return new ClientDeployRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder emHost(String emHost) {
            this.emHost = emHost;
            return builder();
        }

        private void startStressLoad() {
            RunCommandFlowContext runCmdFlowContext =
                new RunCommandFlowContext.Builder("load.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
                    .terminateOnMatch("Up and Running.").build();
            getEnvProperties().add(STRESSAPP_START_LOAD, runCmdFlowContext);
        }

        private void startJmeterLoad() {
            RunCommandFlowContext runCmdFlowContext =
                new RunCommandFlowContext.Builder("jmeter.bat")
                    .workDir(TasBuilder.WIN_SOFTWARE_LOC).terminateOnMatch("Started").build();
            getEnvProperties().add(JMETER_START_LOAD, runCmdFlowContext);
        }
    }
}
