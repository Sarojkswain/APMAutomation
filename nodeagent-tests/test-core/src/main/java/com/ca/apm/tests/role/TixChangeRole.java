/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.annotation.TasEnvironmentProperty;
import com.ca.tas.annotation.TasResource;
import com.ca.tas.artifact.built.tixchange.TixChangeNodeZipArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.NodeJsRole;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TixChangeRole class.
 *
 * TixChangeRole role
 *
 * @author pojja01@ca.com
 */
public class TixChangeRole extends AbstractRole implements NodeJSAppRole {

	public static final String ENV_TIXCHANGE_START = "tixChangeStart";
	public static final String ENV_TIXCHANGE_STOP = "tixChangeStop";
	public static final String ENV_TIXCHANGE_HOME_DIR = "tixChangeHomeDir";
	public static final String ENV_TIXCHANGE_SERVER_DIR = "tixChangeServerDir";
	public static final String ENV_TIXCHANGE_SERVER_LOG_FILE = "tixChangeServerLog";
	public static final String ENV_TIXCHANGE_STARTUP_SCRIPT_PATH = "tixChangeStartUpScript";
	public static final String ENV_TIXCHANGE_PORT = "tixChangePort";

	private final GenericFlowContext flowContext;
	private final RunCommandFlowContext startCommand;
	private final RunCommandFlowContext stopCommand;
	private final String startScriptPath;
	private final String logFile;
	private final boolean autoStart;
	private final int port;
	private final String home;

	/**
	 * @param builder
	 *            Builder object containing all necessary data
	 */
	protected TixChangeRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());

		flowContext = builder.flowContext;
		startCommand = builder.startCommand;
		stopCommand = builder.stopCommand;
		this.startScriptPath = builder.startScriptPath;
		logFile = builder.logFile;
		autoStart = builder.autoStart;
		port = builder.port;
		home = builder.destination;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		runFlow(aaClient, GenericFlow.class, flowContext);

		if (autoStart) {
			runCommandFlowAsync(aaClient, startCommand);
		}
	}

	@Nullable
	public RunCommandFlowContext getStartCommand() {
		return startCommand;
	}

	@Nullable
	public RunCommandFlowContext getStopCommand() {
		return stopCommand;
	}

	public GenericFlowContext getFlowContext() {
		return flowContext;
	}

	@TasResource("tixChangeLog")
	@TasEnvironmentProperty(ENV_TIXCHANGE_SERVER_LOG_FILE)
	public String getLogFile() {
		return logFile;
	}

	@TasEnvironmentProperty(ENV_TIXCHANGE_STARTUP_SCRIPT_PATH)
	public String getStartupScriptPath() {
		return this.startScriptPath;
	}

	@TasEnvironmentProperty(ENV_TIXCHANGE_HOME_DIR)
	public String getHomeDir() {
		return this.flowContext.getDestination();
	}

	@TasEnvironmentProperty(ENV_TIXCHANGE_PORT)
	public String getPort() {
		return String.valueOf(port);
	}

	/**
	 * Linux Builder responsible for holding all necessary properties to
	 * instantiate {@link TixChangeRole}
	 */
	public static class LinuxBuilder extends Builder {

		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}

		@Override
		protected Builder builder() {
			return this;
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

	/**
	 * Builder responsible for holding all necessary properties to instantiate
	 * {@link TixChangeRole}
	 */
	public static class Builder extends BuilderBase<Builder, TixChangeRole> {
		private static final int DEFAULT_PORT = 3000;

		private final String roleId;
		private final ITasResolver tasResolver;
		private final Map<String, String> datasourceData = new HashMap<>();
		private final Map<String, String> configData = new HashMap<>();
		private final Map<String, Map<String, String>> config = new HashMap<>();

		protected Artifact artifact;
		protected GenericFlowContext flowContext;
		protected String destination;
		protected RunCommandFlowContext startCommand;
		protected RunCommandFlowContext stopCommand;
		protected NodeJsRole nodeJsRole;
		protected String startScriptPath;
		protected String logFile;
		protected boolean autoStart = false;
		protected int port = DEFAULT_PORT;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public TixChangeRole build() {

			Args.notNull(artifact, "artifact");
			initGenericFlow();
			initCommands();

			TixChangeRole tixchangeRole = getInstance();
			Args.notNull(tixchangeRole.flowContext, "Generic flow context");
			Args.notNull(tixchangeRole.logFile, "Log file");

			return tixchangeRole;
		}

		protected void initGenericFlow() {
			String serverBase = destination + getPathSeparator() + "server" + getPathSeparator();
			getEnvProperties().add(ENV_TIXCHANGE_SERVER_DIR, serverBase);
			String datasourceFile = serverBase + "datasources.json";
			String configFile = serverBase + "config.json";

			GenericFlowContext.Builder flowContextBuilder = new GenericFlowContext.Builder(
			        tasResolver.getArtifactUrl(artifact))
			        .configurationMap(datasourceFile, datasourceData)
			        .configurationMap(configFile, configData).destination(destination);

			for (Map.Entry<String, Map<String, String>> configItem : config.entrySet()) {
				flowContextBuilder.configurationMap(configItem.getKey(), configItem.getValue());
			}

			flowContext = flowContextBuilder.build();
		}

		protected void initCommands() {
			if (nodeJsRole == null) {
				return;
			}
			assert flowContext != null;

			logFile = flowContext.getDestination() + getPathSeparator() + "out.log";

			String serverExecPath = flowContext.getDestination() + getPathSeparator() + "server"
			        + getPathSeparator() + "server.js";
			String workDir = nodeJsRole.getDeployContext().getNodeJsBinDirectory();
			initStartCommand(serverExecPath, workDir);
			initStopCommand(serverExecPath, workDir);
			startScriptPath = serverExecPath;
		}

		protected void initStartCommand(String serverExecPath, String workDir) {
			startCommand = new RunCommandFlowContext.Builder("forever").workDir(workDir)
			        .args(Arrays.asList("start", "-l", logFile, "-a", serverExecPath)).build();
			getEnvProperties().add(ENV_TIXCHANGE_START, startCommand);
		}

		protected void initStopCommand(String serverExecPath, String workDir) {
			stopCommand = new RunCommandFlowContext.Builder("forever").workDir(workDir)
			        .args(Arrays.asList("stop", serverExecPath)).build();
			getEnvProperties().add(ENV_TIXCHANGE_STOP, stopCommand);
		}

		@Override
		protected TixChangeRole getInstance() {
			return new TixChangeRole(this);
		}

		public Builder artifact(Artifact artifact) {
			this.artifact = artifact;

			return builder();
		}

		public Builder version(String tixchangeVersion) {
			artifact = new TixChangeNodeZipArtifact(tasResolver).createArtifact(tixchangeVersion)
			        .getArtifact();
			return builder();
		}

		public Builder destination(String unpackDest) {
			destination = unpackDest;

			return builder();
		}

		public Builder mysqlCreds(String username, String password) {
			datasourceData.put("nodetix.username", username);
			datasourceData.put("nodetix.password", password);

			return builder();
		}

		public Builder configDatasources(String key, String value) {
			Args.notNull(key, "Key");
			Args.notNull(value, "Value");
			datasourceData.put(key, value);

			return builder();
		}

		public Builder configConfig(String key, String value) {
			Args.notNull(key, "Key");
			Args.notNull(value, "Value");
			configData.put(key, value);

			return builder();
		}

		public Builder config(String filePath, Map<String, String> data) {
			Args.notNull(filePath, "File path");
			Args.notNull(data, "Data");
			config.put(filePath, data);

			return builder();
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder node(NodeJsRole nodeJsRole) {
			this.nodeJsRole = nodeJsRole;

			return builder();
		}

		public Builder autoStart() {
			this.autoStart = true;
			return builder();
		}
	}
}
