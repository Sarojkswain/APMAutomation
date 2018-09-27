package com.ca.apm.saas.standalone;

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

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ClientDeployRole.class);
	private ITasResolver tasResolver;
	private String xjvmhost;
	private String host;
	private String emHost;
	public static final String WLSCC_START_LOAD = "wlscrossclusterloadStart";
	public static final String WASCC_START_LOAD = "wascrossclusterloadStart";
	public static final String WLSCC_STOP_LOAD = "wlscrossclusterloadStop";
	public static final String WASCC_STOP_LOAD = "wascrossclusterloadStop";
	public static final String STRESSAPP_START_LOAD = "stressapploadStart";
	public static final String DEEPTT_START_LOAD = "deepttloadStart";

	protected ClientDeployRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.xjvmhost = builder.xjvmhost;
		this.emHost = builder.emHost;
		host = tasResolver.getHostnameById(builder.roleId);
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		
		
		deployJmeter(aaClient);
		deployTestngSuite(aaClient);
		deployStressApp(aaClient);
		updateXjvmhostWLS(aaClient);
		updateXjvmhostWAS(aaClient);
		createBatchFileWLS(aaClient);
		createBatchFileWAS(aaClient);
		deployDefaultagent(aaClient);
		updateAgentProfile(aaClient);
		createStressLoadBatchFile(aaClient);
		updateDeepTTCountAndCreateBatchWLS(aaClient);
		updateDeepTTCountAndCreateBatchWAS(aaClient);
		}

	private void deployTestngSuite(IAutomationAgentClient aaClient) {

		deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-projects",
				"javaagent_v2", "dist", "client");
		}

	private void createStressLoadBatchFile(IAutomationAgentClient aaClient) {
		String parameters = "/stressapp/StressApp.jar doSQL=true,doWideStructure=true,doErrors=true,doRandom=true,maxAppIndex=150,maxMetricIndex=50,stackDepth=10,maxBackendIndex=10,minSleepOnMethods=10,stallThreshold=650,sleepOnMethods=70,testDuration=1209600000,threadServiceMaxThreads=100,numberOfConcurrentUsers=10";
		Collection<String> createBatch = Arrays
				.asList("title StressLoad","java -javaagent:"
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/wily/Agent.jar -Dcom.wily.introscope.agentProfile="
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/wily/core/config/IntroscopeAgent.profile -Dcom.wily.autoprobe.logSizeInKB=100000  -Duser.dir="
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp  -Dlog4j.configuration=file:"
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/resources/log4j-StressApp.properties -classpath "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/lib/* -Xms512m -Xmx1024m -XX:+UseSerialGC -XX:+HeapDumpOnOutOfMemoryError -verbosegc -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:"
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/stressapp/RemoteConfig_agent_true.gc.log -Dcom.wily.autoprobe.logSizeInKB=100000 -jar "
						+ TasBuilder.WIN_SOFTWARE_LOC + parameters);

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/StressLoad.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void updateDeepTTCountAndCreateBatchWLS(IAutomationAgentClient aaClient) {

		FileModifierFlowContext context = null;
		Map<String, String> replacePairs = new HashMap<String, String>();

		replacePairs.put("count=\"1\"", "count=\"90000\"");
		replacePairs.put("interval=\"1000\"", "interval=\"10000\"");

		String fileName = TasBuilder.WIN_SOFTWARE_LOC
				+ "/client/resources/pipeorgan" + "/DeepTT4000.xml";

		context = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);

		Collection<String> createBatch = Arrays
				.asList("title deepTT4000WLS","java -jar -Dpipeorgan.urlfetcher.host="
						+ host
						+ " -Dpipeorgan.logging=VERBOSE -Dpipeorgan.sessionid.host="
						+ host
						+ " -Dpipeorgan.urlfetcher.port=7001 -Dpipeorgan.sessionid.port=7001 -Dpipeorgan.urlfetcher.contextroot=pipeorgan -Dpipeorgan.sessionid.contextroot=pipeorgan "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/webapp/pipeorgandomain/pipeorgan/pipeorgan.jar "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/client/resources/pipeorgan/DeepTT4000.xml");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/deepTT4000WLS.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void createBatchFileWLS(IAutomationAgentClient aaClient) {

		Collection<String> createBatch = Arrays
				.asList("title crossJVMWLS","java -jar -Dpipeorgan.urlfetcher.host="
						+ host
						+ " -Dpipeorgan.logging=VERBOSE -Dpipeorgan.sessionid.host="
						+ host
						+ " -Dpipeorgan.urlfetcher.port=7001 -Dpipeorgan.sessionid.port=7001 -Dpipeorgan.urlfetcher.contextroot=pipeorgan -Dpipeorgan.sessionid.contextroot=pipeorgan "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/webapp/pipeorgandomain/pipeorgan/pipeorgan.jar "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/client/resources/pipeorgan/jass/CrossJvm_EJB3.xml");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/crossJVMWLS.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void createBatchFileWAS(IAutomationAgentClient aaClient) {

		Collection<String> createBatch = Arrays
				.asList("title crossJVMWAS","java -jar -Dpipeorgan.urlfetcher.host="
						+ host
						+ " -Dpipeorgan.logging=VERBOSE -Dpipeorgan.sessionid.host="
						+ host
						+ " -Dpipeorgan.urlfetcher.port=9080 -Dpipeorgan.sessionid.port=9080 -Dpipeorgan.urlfetcher.contextroot=pipeorgan -Dpipeorgan.sessionid.contextroot=pipeorgan "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/was/lib/ext/pipeorgan.jar "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/client/resources/pipeorgan/jass/CrossJvm_EJB2.xml");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/crossJVMWAS.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}
	
	private void updateDeepTTCountAndCreateBatchWAS(IAutomationAgentClient aaClient) {

		FileModifierFlowContext context = null;
		Map<String, String> replacePairs = new HashMap<String, String>();

		replacePairs.put("count=\"1\"", "count=\"90000\"");
		replacePairs.put("interval=\"1000\"", "interval=\"10000\"");

		String fileName = TasBuilder.WIN_SOFTWARE_LOC
				+ "/client/resources/pipeorgan" + "/DeepTT4000.xml";

		context = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);

		Collection<String> createBatch = Arrays
				.asList("title deepTT4000WAS","java -jar -Dpipeorgan.urlfetcher.host="
						+ host
						+ " -Dpipeorgan.logging=VERBOSE -Dpipeorgan.sessionid.host="
						+ host
						+ " -Dpipeorgan.urlfetcher.port=9080 -Dpipeorgan.sessionid.port=9080 -Dpipeorgan.urlfetcher.contextroot=pipeorgan -Dpipeorgan.sessionid.contextroot=pipeorgan "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/was/lib/ext/pipeorgan.jar "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/client/resources/pipeorgan/DeepTT4000.xml");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/deepTT4000WAS.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void updateXjvmhostWLS(IAutomationAgentClient aaClient) {

		FileModifierFlowContext context = null;
		Map<String, String> replacePairs = new HashMap<String, String>();

		String url = "t3://" + xjvmhost + ":7001";

		replacePairs.put("\\{PROVIDER\\}", url);
		replacePairs.put("\\{DURATION\\}", "999999990");
		replacePairs.put("\\{THREADS_COUNT\\}", "100");
		replacePairs.put("\\{THREADS_INTERVAL\\}", "1000");

		String fileName = TasBuilder.WIN_SOFTWARE_LOC
				+ "/client/resources/pipeorgan/jass/" + "/CrossJvm_EJB3.xml";

		context = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);
	}

	private void updateXjvmhostWAS(IAutomationAgentClient aaClient) {

		FileModifierFlowContext context = null;
		Map<String, String> replacePairs = new HashMap<String, String>();

		String url = "iiop://" + xjvmhost + ":9100";

		replacePairs.put("\\{PROVIDER\\}", url);
		replacePairs.put("\\{DURATION\\}", "999999990");
		replacePairs.put("\\{THREADS_COUNT\\}", "100");
		replacePairs.put("\\{THREADS_INTERVAL\\}", "1000");

		String fileName = TasBuilder.WIN_SOFTWARE_LOC
				+ "/client/resources/pipeorgan/jass/" + "/CrossJvm_EJB2.xml";

		context = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);
	}

	private void deployDefaultagent(IAutomationAgentClient aaClient) {

		String artifact = "agent-noinstaller-default-windows";
		deployZipArtifact(aaClient, "com.ca.apm.delivery", artifact, "",
				"stressapp");
	}

	private void updateAgentProfile(IAutomationAgentClient aaClient) {

		FileModifierFlowContext context = null;
		Map<String, String> replacePairs = new HashMap<String, String>();
		String prop = "agentManager.url.1=http://" + emHost + ":8081";
		replacePairs.put("agentManager.url.1=localhost:5001", prop);
		replacePairs.put(
				"#introscope.agent.customProcessName=CustomProcessName",
				"introscope.agent.customProcessName=ErrorStallProcess");
		replacePairs.put("#introscope.agent.agentName=AgentName",
				"introscope.agent.agentName=ErrorStallAgent");
		replacePairs.put("introscope.agent.stalls.thresholdseconds=30",
				"introscope.agent.stalls.thresholdseconds=10");
		replacePairs.put("introscope.agent.stalls.resolutionseconds=10",
				"introscope.agent.stalls.resolutionseconds=5");

		String fileName = TasBuilder.WIN_SOFTWARE_LOC
				+ "stressapp/wily/core/config/IntroscopeAgent.profile";

		context = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);
	}

	private void deployStressApp(IAutomationAgentClient aaClient) {

		deployZipArtifact(aaClient, "stressapp", "jvm8-dist");
	}

	private void deployZipArtifact(IAutomationAgentClient aaClient,
			String artifactId, String classifier) {

		deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-tools",
				artifactId, classifier, artifactId);
	}

	private void deployZipArtifact(IAutomationAgentClient aaClient,
			String groupId, String artifactId, String classifier, String homeDir) {

		URL url = tasResolver
				.getArtifactUrl(new DefaultArtifact(groupId, artifactId,
						classifier, "zip", tasResolver.getDefaultVersion()));
		LOGGER.info("Downloading zip artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(TasBuilder.WIN_SOFTWARE_LOC + "/" + homeDir)
				.build();
		runFlow(aaClient, GenericFlow.class, context);
	}

	private void deployJmeter(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "apache-jmeter", "", "zip", "3.1"));
		LOGGER.info("Downloading artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(
						TasBuilder.WIN_SOFTWARE_LOC
								+ "/jmeter").build();
		runFlow(aaClient, GenericFlow.class, context);
		}

	public static class Builder extends BuilderBase<Builder, ClientDeployRole> {

		private final String roleId;
		private final ITasResolver tasResolver;		
		private String xjvmhost;
		private String emHost = "localhost";		

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public ClientDeployRole build() {
			startWLSLoad();
			startWASLoad();
			stopWASLoad();
			stopWLSLoad();
			startStressLoad();
			startDeepTTLoad();
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
		public Builder fldXjvmhost(String xjvmhost) {
			this.xjvmhost = xjvmhost;
			return builder();
		}
		
		 public Builder emHost(String emHost) {
	            this.emHost = emHost;
	            return builder();
	     }
		private void startWLSLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"crossJVMWLS.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Test Finished").build();
			getEnvProperties().add(WLSCC_START_LOAD, runCmdFlowContext);
		}
		private void startWASLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"crossJVMWAS.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Test Finished").build();
			getEnvProperties().add(WASCC_START_LOAD, runCmdFlowContext);
		}
		private void stopWASLoad() {
			String stopCommand = "wmic process where \"CommandLine like '%urlfetcher%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(WASCC_STOP_LOAD, runCmdFlowContext);

		}
		private void stopWLSLoad() {
			String stopCommand = "wmic process where \"CommandLine like '%urlfetcher%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(WLSCC_STOP_LOAD, runCmdFlowContext);
		}
		private void startStressLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"StressLoad.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Up and Running.").build();
			getEnvProperties().add(STRESSAPP_START_LOAD, runCmdFlowContext);
		}
		private void startDeepTTLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"deepTT4000.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Response").build();
			getEnvProperties().add(DEEPTT_START_LOAD, runCmdFlowContext);
		}
		
	}
}