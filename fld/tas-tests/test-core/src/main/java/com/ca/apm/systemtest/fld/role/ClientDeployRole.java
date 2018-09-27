package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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
	private boolean shouldDeployJassApps;
	private boolean shouldDeployConsoleApps;
	private String jvmVersion;
	private String xjvmhost;
	private String host;
	private String pipeorganLoggingLevel;
	public static final String WLSCC_START_LOAD = "wlscrossclusterloadStart";
	public static final String WASCC_START_LOAD = "wascrossclusterloadStart";
	public static final String WLSCC_STOP_LOAD = "wlscrossclusterloadStop";
	public static final String WASCC_STOP_LOAD = "wascrossclusterloadStop";
	private String emHost;
	public static final String STRESSAPP_START_LOAD = "stressapploadStart";
	public static final String JMETER_START_LOAD = "jmeterloadStart";

	protected ClientDeployRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.jvmVersion = builder.jvmVersion;
		this.shouldDeployJassApps = builder.shouldDeployJassApps;
		this.shouldDeployConsoleApps = builder.shouldDeployConsoleApps;
		this.xjvmhost = builder.xjvmhost;
		host = tasResolver.getHostnameById(builder.roleId);
		this.pipeorganLoggingLevel = builder.pipeorganLoggingLevel;
		this.emHost = builder.emHost;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		deployTestngSuite(aaClient);
		deployJmeter(aaClient);
		deployDefaultagent(aaClient);
		createLoadBatchFile(aaClient);
		updateAgentProfile(aaClient);
		downloadJmeterScript(aaClient);

		if (shouldDeployJassApps) {
		    deployStressApp(aaClient);
            deployProbebuilderApp(aaClient);
		}
		
		if (shouldDeployConsoleApps) {
			deploySqlmetricgen(aaClient);
			deployDITestApp(aaClient);
			deployDeepInheritanceApp(aaClient);
			updateXjvmhostWLS(aaClient);
			updateXjvmhostWAS(aaClient);
			createBatchFileWLS(aaClient);
			createBatchFileWAS(aaClient);
		}
	}

	private void createLoadBatchFile(IAutomationAgentClient aaClient) {
		String parameters = "/stressapp/StressApp.jar doSQL=true,doWideStructure=true,doErrors=true,doRandom=true,maxAppIndex=250,maxMetricIndex=50,stackDepth=10,maxBackendIndex=10,minSleepOnMethods=10,stallThreshold=650,sleepOnMethods=70,testDuration=1209600000,threadServiceMaxThreads=100,numberOfConcurrentUsers=10";
		Collection<String> createBatch = Arrays
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

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/load.bat", createBatch)
				.build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void updateAgentProfile(IAutomationAgentClient aaClient) {

		FileModifierFlowContext context = null;
		Map<String, String> replacePairs = new HashMap<String, String>();
		String prop = "agentManager.url.1=" + emHost + ":5001";
		replacePairs.put("agentManager.url.1=localhost:5001", prop);
		replacePairs.put(
				"#introscope.agent.customProcessName=CustomProcessName",
				"introscope.agent.customProcessName=ErrorStallProcess");
		replacePairs.put("#introscope.agent.agentName=AgentName",
				"introscope.agent.agentName=ErrorStallAgent");

		String fileName = TasBuilder.WIN_SOFTWARE_LOC
				+ "stressapp/wily/core/config/IntroscopeAgent.profile";

		context = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context);
	}

	private void downloadJmeterScript(IAutomationAgentClient aaClient) {
		// download jmx file
		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "AssistedTriage", "ATST_Load", "zip",
				"1.0"));
		LOGGER.info("Downloading artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/").build();
		runFlow(aaClient, GenericFlow.class, context);
		// change host to master
		FileModifierFlowContext context1 = null;
		Map<String, String> replacePairs = new HashMap<String, String>();
		replacePairs.put("localhost", emHost);
		String fileName = TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/ATST_Load.jmx";
		context1 = new FileModifierFlowContext.Builder().replace(fileName,
				replacePairs).build();
		runFlow(aaClient, FileModifierFlow.class, context1);
		// create jmeter batch file
		Collection<String> createBatch = Arrays.asList("cd "
				+ TasBuilder.WIN_SOFTWARE_LOC
				+ "/jmeter/apache-jmeter-2.11/bin && jmeter -n -t "
				+ TasBuilder.WIN_SOFTWARE_LOC + "/jmeter/ATST_Load.JMX");
		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/jmeter.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void deployDefaultagent(IAutomationAgentClient aaClient) {

		String artifact = "agent-noinstaller-default-windows";
		deployZipArtifact(aaClient, "com.ca.apm.delivery", artifact, "",
				"stressapp");
	}

	private void createBatchFileWLS(IAutomationAgentClient aaClient) {

		Collection<String> createBatch = Arrays
				.asList("java -jar -Dpipeorgan.urlfetcher.host="
						+ host
						+ " -Dpipeorgan.logging="
						+ (StringUtils.hasText(pipeorganLoggingLevel) ? pipeorganLoggingLevel
								: "VERBOSE")
						+ " -Dpipeorgan.sessionid.host="
						+ host
						+ " -Dpipeorgan.urlfetcher.port=7001 -Dpipeorgan.sessionid.port=7001 -Dpipeorgan.urlfetcher.contextroot=pipeorgan -Dpipeorgan.sessionid.contextroot=pipeorgan "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/webapp/pipeorgandomain/pipeorgan/pipeorgan.jar "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/client/resources/pipeorgan/jass/CrossJvm_EJB3.xml");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/crossclusterWLS.bat",
						createBatch).build();
		runFlow(aaClient, FileModifierFlow.class, BatchFile);
	}

	private void createBatchFileWAS(IAutomationAgentClient aaClient) {

		Collection<String> createBatch = Arrays
				.asList("java -jar -Dpipeorgan.urlfetcher.host="
						+ host
						+ " -Dpipeorgan.logging="
						+ (StringUtils.hasText(pipeorganLoggingLevel) ? pipeorganLoggingLevel
								: "VERBOSE")
						+ " -Dpipeorgan.sessionid.host="
						+ host
						+ " -Dpipeorgan.urlfetcher.port=9080 -Dpipeorgan.sessionid.port=9080 -Dpipeorgan.urlfetcher.contextroot=pipeorgan -Dpipeorgan.sessionid.contextroot=pipeorgan "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/was/lib/ext/pipeorgan.jar "
						+ TasBuilder.WIN_SOFTWARE_LOC
						+ "/client/resources/pipeorgan/jass/CrossJvm_EJB2.xml");

		FileModifierFlowContext BatchFile = new FileModifierFlowContext.Builder()
				.create(TasBuilder.WIN_SOFTWARE_LOC + "/crossclusterWAS.bat",
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

	private void deployTestngSuite(IAutomationAgentClient aaClient) {

		deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-projects",
				"javaagent_v2", "dist", "client");
		deployJarArtifact(aaClient, "com.ca.apm.em",
				"com.wily.introscope.clw.feature", "CLWorkstation.jar");
		deployJarArtifact(aaClient, "com.ca.apm.em",
				"com.wily.introscope.jdbc.feature", "IntroscopeJDBC.jar");
		deployJarArtifact(aaClient, "com.ca.apm.agent", "pbddoclet",
				"WilyPBDGenerator.jar");

	}

	private void deployProbebuilderApp(IAutomationAgentClient aaClient) {

		if (shouldDeployJassApps) {
			deployZipArtifact(aaClient, "probebuilderapp", "dist");
		}
	}

	private void deployStressApp(IAutomationAgentClient aaClient) {

		if (shouldDeployJassApps) {
			String classifier = "jvm7-dist";
			if (jvmVersion.equals("8")) {
				classifier = "jvm8-dist";
			}
			deployZipArtifact(aaClient, "stressapp", classifier);
		}
	}

	private void deploySqlmetricgen(IAutomationAgentClient aaClient) {

		String classifier = "jvm7-dist";
		if (jvmVersion.equals("8")) {
			classifier = "jvm8-dist";
		}

		deployZipArtifact(aaClient, "sqlmetricgen", classifier);
	}

	private void deployDeepInheritanceApp(IAutomationAgentClient aaClient) {

		deployZipArtifact(aaClient, "deepInheritance", "dist");
	}

	private void deployDITestApp(IAutomationAgentClient aaClient) {

		deployZipArtifact(aaClient, "ditestapp", "dist");
	}

	private void deployZipArtifact(IAutomationAgentClient aaClient,
			String artifactId, String classifier) {

		deployZipArtifact(aaClient, "com.ca.apm.coda-projects.test-tools",
				artifactId, classifier, artifactId);
	}

	private void deployJarArtifact(IAutomationAgentClient aaClient,
			String groupId, String artifactId, String dest) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(groupId,
				artifactId, "", "jar", tasResolver.getDefaultVersion()));
		LOGGER.info("Downloading jar artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.notArchive()
				.artifactUrl(url)
				.destination(
						TasBuilder.WIN_SOFTWARE_LOC + "/client/lib/em/" + dest)
				.build();
		runFlow(aaClient, GenericFlow.class, context);
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
				"com.ca.apm.binaries", "apache-jmeter", "", "zip", "2.11"));
		LOGGER.info("Downloading artifact " + url.toString());

		GenericFlowContext context = new GenericFlowContext.Builder()
				.artifactUrl(url)
				.destination(
						TasBuilder.WIN_SOFTWARE_LOC
								+ "/jmeter/apache-jmeter-2.11").build();
		runFlow(aaClient, GenericFlow.class, context);
	}

	public static class Builder extends BuilderBase<Builder, ClientDeployRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		protected boolean shouldDeployJassApps;
		protected boolean shouldDeployConsoleApps;
		private String jvmVersion;
		private String xjvmhost;
		protected String pipeorganLoggingLevel;
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

		public Builder jvmVersion(String jvmVersion) {
			this.jvmVersion = jvmVersion;
			return builder();
		}

		public Builder shouldDeployJassApps(boolean shouldDeployJassApps) {
			this.shouldDeployJassApps = shouldDeployJassApps;
			return builder();
		}

		public Builder emHost(String emHost) {
			this.emHost = emHost;
			return builder();
		}
		
		public Builder fldXjvmhost(String xjvmhost) {
			this.xjvmhost = xjvmhost;
			return builder();
		}

		public Builder shouldDeployConsoleApps(boolean shouldDeployConsoleApps) {
			this.shouldDeployConsoleApps = shouldDeployConsoleApps;
			return builder();
		}

		/**
		 * possible values: [debug, verbose, quiet, nologging]
		 * 
		 * @param pipeorganLoggingLevel
		 * @return
		 */
		public Builder pipeorganLoggingLevel(String pipeorganLoggingLevel) {
			this.pipeorganLoggingLevel = pipeorganLoggingLevel;
			return builder();
		}

		private void startWLSLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"crossclusterWLS.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Test Finished").build();
			getEnvProperties().add(WLSCC_START_LOAD, runCmdFlowContext);

		}

		private void startWASLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"crossclusterWAS.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
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
					"load.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Up and Running.").build();
			getEnvProperties().add(STRESSAPP_START_LOAD, runCmdFlowContext);
		}

		private void startJmeterLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"jmeter.bat").workDir(TasBuilder.WIN_SOFTWARE_LOC)
					.terminateOnMatch("Started").build();
			getEnvProperties().add(JMETER_START_LOAD, runCmdFlowContext);
		}
	}
}