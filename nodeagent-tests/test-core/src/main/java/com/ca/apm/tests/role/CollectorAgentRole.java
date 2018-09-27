package com.ca.apm.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.flow.DeployCollectorAgentFlow;
import com.ca.apm.tests.flow.DeployCollectorAgentFlowContext;
import com.ca.apm.tests.flow.DeployCollectorAgentFlowContext.Builder.CollectorAgentProperty;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

/**
 * @author sinka08
 *
 */
public class CollectorAgentRole extends AbstractAgentRole {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectorAgentRole.class);
	private final DeployCollectorAgentFlowContext collAgentDeployFlowContext;
	private final RunCommandFlowContext collAgentStartCmdContext;
	private final RunCommandFlowContext collAgentStopCmdContext;
	private List<RunCommandFlowContext> miscCmdCtxs;
	private boolean shouldStart = false;
	private int startDelayInSecs = 0;
	@SuppressWarnings("unused")
    private int startTimeoutInSecs = 0;

	protected CollectorAgentRole(Builder b) {
		super(b, b.getEnvProperties());
		collAgentDeployFlowContext = b.collAgentFlowContext;
		collAgentStartCmdContext = b.collAgentStartCmdContext;
		collAgentStopCmdContext = b.collAgentStopCmdContext;
		miscCmdCtxs = b.miscCmdCtxs;
		shouldStart = b.shouldAutoStart;
		startDelayInSecs = b.startDelayInSecs;
		startTimeoutInSecs = b.startTimeoutInSecs;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		deployCollector(aaClient);
		if (shouldStart) {
			startCollectorAgent(aaClient);
		}
	}

	protected void deployCollector(IAutomationAgentClient aaClient) {
		runFlow(aaClient, DeployCollectorAgentFlow.class, collAgentDeployFlowContext);

		if (!miscCmdCtxs.isEmpty()) {
			for (RunCommandFlowContext ctx : miscCmdCtxs) {
				LOGGER.info("command: " + ctx.getExec());
				runCommandFlow(aaClient, ctx);
			}
		}

	}

	protected void startCollectorAgent(IAutomationAgentClient aaClient) {
		runCommandFlowAsync(aaClient, collAgentStartCmdContext, startDelayInSecs);
	}

	protected void stopCollectorAgent(IAutomationAgentClient aaClient) {
		runCommandFlowAsync(aaClient, collAgentStopCmdContext);
	}

	public Map<String, String> getAdditionalProps() {
		return collAgentDeployFlowContext.getAdditionalProperties();
	}

	public static class LinuxBuilder extends Builder {
		public static final String COLLECTOR_AGENT_EXECUTABLE = "CollectorAgent.sh";

		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
			flowContextBuilder = new DeployCollectorAgentFlowContext.LinuxBuilder();
			agentPlatform = ArtifactPlatform.UNIX;
			collAgentExecutable = COLLECTOR_AGENT_EXECUTABLE;
		}

		@Override
		protected void initCollectorAgentStartContext() {
			collAgentStartCmdContext = new RunCommandFlowContext.Builder(collAgentExecutable)
			        .args(Collections.singletonList(collAgentStartArg))
			        .workDir(
			                collAgentFlowContext.getInstallDir() + TasBuilder.LINUX_SEPARATOR
			                        + "bin").name(roleId)
			        .terminateOnMatch(COLLECTOR_AGENT_STARTUP_MESSAGE).build();
			getEnvProperties().add(ENV_START_COLLECTOR_AGENT, collAgentStartCmdContext);

		}

		@Override
		protected void initCollectorAgentStopContext() {
			collAgentStopCmdContext = new RunCommandFlowContext.Builder(collAgentExecutable)
			        .args(Collections.singletonList(collAgentStopArg))
			        .workDir(
			                collAgentFlowContext.getInstallDir() + TasBuilder.LINUX_SEPARATOR
			                        + "bin").name(roleId)
			        .terminateOnMatch(COLLECTOR_AGENT_STARTUP_MESSAGE).build();
			getEnvProperties().add(ENV_STOP_COLLECTOR_AGENT, collAgentStopCmdContext);

		}

		@Override
		protected void addMiscCommandContexts() {
			// commenting for now as we have added similar logic in
			// DeployCollectorAgentFlow

			/*String installDir = collAgentFlowContext.getInstallDir();

			// "chmod 755 file"
			String cmd = "chmod";
			String perm = "755";
			String file = installDir + "bin" + TasBuilder.LINUX_SEPARATOR + collAgentExecutable;
			Collection<String> args = new ArrayList<>();
			args.add(perm);
			args.add(file);

			// update permission for collectorAgent.sh
			miscCmdCtxs.add(new RunCommandFlowContext.Builder(cmd).args(args).build());

			// update permission for java executable
			file = installDir + "jre" + TasBuilder.LINUX_SEPARATOR + "bin"
			        + TasBuilder.LINUX_SEPARATOR + "java";
			args = new ArrayList<>();
			args.add(perm);
			args.add(file);
			miscCmdCtxs.add(new RunCommandFlowContext.Builder(cmd).args(args).build());*/

		}

	}

	public static class Builder extends
	        AbstractAgentRole.AbstractBuilder<Builder, CollectorAgentRole> {
		public static final String COLLECTOR_AGENT_EXECUTABLE = "CollectorAgent.cmd";
		private static final String START_ARG = "start";
		private static final String STOP_ARG = "stop";
		public static final String COLLECTOR_AGENT_STARTUP_MESSAGE = "Introscope Agent startup complete";
		public static final String ENV_COLLECTOR_AGENT_HOME = "collectorAgentHome";
		public static final String ENV_START_COLLECTOR_AGENT = "collectorAgentStart";
		public static final String ENV_STOP_COLLECTOR_AGENT = "collectorAgentStop";
		protected String collAgentExecutable = COLLECTOR_AGENT_EXECUTABLE;
		protected String collAgentStartArg = START_ARG;
		protected String collAgentStopArg = STOP_ARG;

		protected DeployCollectorAgentFlowContext.Builder flowContextBuilder = new DeployCollectorAgentFlowContext.Builder();
		protected DeployCollectorAgentFlowContext collAgentFlowContext;
		private boolean shouldAutoStart = false;
		private int startDelayInSecs = 0;
		private int startTimeoutInSecs = 0;
		protected RunCommandFlowContext collAgentStartCmdContext;
		protected RunCommandFlowContext collAgentStopCmdContext;
		protected List<RunCommandFlowContext> miscCmdCtxs = new ArrayList<>();

		public Builder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}

		@Override
		public CollectorAgentRole build() {
			initCollectorAgentDeployContext();
			initCollectorAgentStartContext();
			initCollectorAgentStopContext();
			addMiscCommandContexts();

			CollectorAgentRole collectorAgentRole = (CollectorAgentRole) getInstance();
			Args.notNull(collectorAgentRole.collAgentDeployFlowContext,
			        "Collector Agent deploy flow context");
			Args.notNull(collectorAgentRole.collAgentStartCmdContext,
			        "Collector Agent start command context");
			return collectorAgentRole;
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected CollectorAgentRole getInstance() {
			return new CollectorAgentRole(this);
		}

		protected void initCollectorAgentDeployContext() {
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
			collAgentFlowContext = flowContextBuilder.build();

			getEnvProperties().add(ENV_COLLECTOR_AGENT_HOME, collAgentFlowContext.getInstallDir());
		}

		protected Artifact initArtifact() {
			if (agentArtifact != null) {
				return agentArtifact;
			}

			if (tasAgentArtifact != null) {
				return tasAgentArtifact.createArtifact(agentVersion).getArtifact();
			}

			throw new IllegalArgumentException(
			        "agent artifact must be specified must be specified, when configuring Collector Agent role.");
		}

		protected void addMiscCommandContexts() {

		}

		protected void initCollectorAgentStartContext() {
			collAgentStartCmdContext = new RunCommandFlowContext.Builder(collAgentExecutable)
			        .workDir(
			                collAgentFlowContext.getInstallDir() + TasBuilder.WIN_SEPARATOR + "bin")
			        .name(roleId).terminateOnMatch(COLLECTOR_AGENT_STARTUP_MESSAGE).build();
			getEnvProperties().add(ENV_START_COLLECTOR_AGENT, collAgentStartCmdContext);

		}

		protected void initCollectorAgentStopContext() {
			// TODO still to figure out how to stop collector agent on windows
			collAgentStopCmdContext = new RunCommandFlowContext.Builder(collAgentExecutable)
			        .workDir(
			                collAgentFlowContext.getInstallDir() + TasBuilder.WIN_SEPARATOR + "bin")
			        .name(roleId).terminateOnMatch(COLLECTOR_AGENT_STARTUP_MESSAGE).build();
			getEnvProperties().add(ENV_STOP_COLLECTOR_AGENT, collAgentStopCmdContext);

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
