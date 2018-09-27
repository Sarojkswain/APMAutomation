/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

import java.net.URL;
import java.util.Arrays;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.flow.DeployNodeJSPackageFlowContext;
import com.ca.apm.tests.flow.DeployNodeJsPackageFlow;
import com.ca.tas.annotation.TasEnvironmentProperty;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.NodeJsRole;

/**
 * Node.js HelloWorld Application Role
 *
 * @author sinka08@ca.com
 */
public class HelloWorldAppRole extends AbstractRole implements NodeJSAppRole {

	public static final String ENV_HELLOWORLD_APP_START = "helloWorldAppStart";
	public static final String ENV_HELLOWORLD_APP_STOP = "helloWorldAppStop";
	public static final String ENV_HELLOWORLD_APP_HOME_DIR = "helloWorldAppHomeDir";
	public static final String ENV_HELLOWORLD_APP_LOG_FILE = "helloWorldAppConsoleLog";
	public static final String ENV_HELLOWORLD_APP_STARTUP_SCRIPT_PATH = "helloWorldAppStartUpScript";

	private final DeployNodeJSPackageFlowContext flowContext;
	private final RunCommandFlowContext startCommand;
	private final RunCommandFlowContext stopCommand;
	private final String homeDir;
	private final String startScriptPath;	
	private final String logFile;
	private final boolean autoStart;

	/**
	 * @param builder
	 *            Builder object containing all necessary data
	 */
	protected HelloWorldAppRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());

		flowContext = builder.flowContext;
		startCommand = builder.startCommand;
		stopCommand = builder.stopCommand;
		startScriptPath = builder.startScriptPath;
		homeDir = builder.homeDir;
		logFile = builder.logFile;
		autoStart = builder.autoStart;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		runFlow(aaClient, DeployNodeJsPackageFlow.class, flowContext);

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

	public DeployNodeJSPackageFlowContext getFlowContext() {
		return flowContext;
	}

	@TasEnvironmentProperty(ENV_HELLOWORLD_APP_LOG_FILE)
	public String getLogFile() {
		return logFile;
	}

	@TasEnvironmentProperty(ENV_HELLOWORLD_APP_STARTUP_SCRIPT_PATH)
	public String getStartupScriptPath() {
		return this.startScriptPath;
	}

	@TasEnvironmentProperty(ENV_HELLOWORLD_APP_HOME_DIR)
	public String getHomeDir() {
		return homeDir;
	}

	/**
	 * Linux Builder responsible for holding all necessary properties to
	 * instantiate {@link HelloWorldAppRole}
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
	 * {@link HelloWorldAppRole}
	 */
	public static class Builder extends BuilderBase<Builder, HelloWorldAppRole> {

		public static final String PACKAGE_NAME = "hello-world";
		private final String roleId;
		private final ITasResolver tasResolver;
		protected DeployNodeJSPackageFlowContext.Builder flowContextBuilder = new DeployNodeJSPackageFlowContext.Builder();
		protected DeployNodeJSPackageFlowContext flowContext;

		protected String installDir;
		protected String installerTgDir;
		protected Artifact artifact;

		protected String homeDir;
		protected String startScriptPath;
		protected String logFile;		
		protected NodeJsRole nodeJsRole;
		protected String nodeJsExecutableLocation;
		protected String nodeJsHomeDir;
		protected boolean skipBuildingNativeModules = false;
		protected boolean autoStart = false;
		protected RunCommandFlowContext startCommand;
		protected RunCommandFlowContext stopCommand;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public HelloWorldAppRole build() {

			Args.notNull(artifact, "artifact");
			initDeployContext();
			initCommands();

			HelloWorldAppRole appRole = getInstance();
			Args.notNull(appRole.flowContext, "DeployNodeJsPackage flow context");

			return appRole;
		}

		protected void initDeployContext() {
			if (installDir != null) {
				flowContextBuilder.installDir(installDir);
			}
			if (installerTgDir != null) {
				flowContextBuilder.installerTgDir(installerTgDir);
			}
			if (nodeJsHomeDir != null) {
				flowContextBuilder.nodeJsHomeDir(nodeJsHomeDir);
			}

			Args.notNull(nodeJsExecutableLocation, "nodeJsExecutableLocation");
			flowContextBuilder.nodeJsExecutableLocation(nodeJsExecutableLocation);

			URL installerUrl = tasResolver.getArtifactUrl(artifact);
			flowContextBuilder.installerUrl(installerUrl);
			flowContextBuilder.npmRegistryUrl(tasResolver.getCentralArtifactory()
			        + "/api/npm/npm-release");
			flowContextBuilder.setShouldNativeBuildFail(skipBuildingNativeModules);
			flowContext = flowContextBuilder.build();

			homeDir = flowContext.getInstallDir() + getPathSeparator() + "node_modules"
			        + getPathSeparator() + PACKAGE_NAME;
			getEnvProperties().add(ENV_HELLOWORLD_APP_HOME_DIR, homeDir);
		}

		protected void initCommands() {
			if (nodeJsRole == null) {
				return;
			}
			assert flowContext != null;

			logFile = homeDir + getPathSeparator() + "out.log";

			String serverExecPath = homeDir + getPathSeparator() + "server.js";
			String workDir = nodeJsRole.getDeployContext().getNodeJsBinDirectory();
			initStartCommand(serverExecPath, workDir);
			initStopCommand(serverExecPath, workDir);
			startScriptPath = serverExecPath;
		}

		protected void initStartCommand(String serverExecPath, String workDir) {
			startCommand = new RunCommandFlowContext.Builder("forever").workDir(workDir)
			        .args(Arrays.asList("start", "-l", logFile, "-a", serverExecPath)).build();
			getEnvProperties().add(ENV_HELLOWORLD_APP_START, startCommand);
		}

		protected void initStopCommand(String serverExecPath, String workDir) {
			stopCommand = new RunCommandFlowContext.Builder("forever").workDir(workDir)
			        .args(Arrays.asList("stop", serverExecPath)).build();
			getEnvProperties().add(ENV_HELLOWORLD_APP_STOP, stopCommand);
		}

		@Override
		protected HelloWorldAppRole getInstance() {
			return new HelloWorldAppRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}		

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder installerTgDir(String installerTgDir) {
			this.installerTgDir = installerTgDir;
			return builder();
		}		

		public Builder artifact(Artifact artifact) {
			this.artifact = artifact;
			return builder();
		}

		public Builder nodeJSRole(NodeJsRole role) {
			this.nodeJsRole = role;
			nodeJsExecutableLocation(role.getDeployContext().getNodeJsExecutableLocation());
			nodeJsHomeDir(role.getDeployContext().getDestination());
			return builder();
		}

		public Builder nodeJsExecutableLocation(String path) {
			this.nodeJsExecutableLocation = path;
			return builder();
		}

		public Builder nodeJsHomeDir(String path) {
			this.nodeJsHomeDir = path;
			return builder();
		}

		public Builder autoStart() {
			this.autoStart = true;
			return builder();
		}
	}
}
