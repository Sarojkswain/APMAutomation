package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * CEM User Group Load Script for EM FLD. This role should be started after
 * Tomcat Server is started.
 * 
 * Usage: new CemUserGroupLoadRole.LinuxBuilder(USERGROUP_ROLE_ID,
 * tasResolver).dbHost("flddb01").dbAdmin("admin")
 * .dbPassword("quality").webServerHost("fldtomcat01").dbName("cemdb")
 * .webServerPort(9091).build();
 *
 * @author banra06@ca.com
 */

public class CemUserGroupLoadRole extends AbstractRole {

	private ITasResolver tasResolver;
	private String installDir;
	private static final String COMMAND = "yum";
	private String dbHost;
	private String dbAdmin;
	private String dbPassword;
	private String dbName;
	private String webServerHost;
	private int webServerPort;

	private static String USERGROUP_START_LOAD = "usergrouploadStart";
	private static String USERGROUP_STOP_LOAD = "usergrouploadStop";

	public CemUserGroupLoadRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		this.dbHost = builder.dbHost;
		this.dbAdmin = builder.dbAdmin;
		this.dbPassword = builder.dbPassword;
		this.dbName = builder.dbName;
		this.webServerHost = builder.webServerHost;
		this.webServerPort = builder.webServerPort;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		getArtifacts(aaClient);
		updateDBName(aaClient);
		installYumPackages(aaClient);
		createStartPython(aaClient);
		createKillPython(aaClient);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "fld", "usergroup", "zip", "1.0"));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	private void installYumPackages(IAutomationAgentClient aaClient) {

		List<String> arguments = new ArrayList<>();
		arguments.add("install");
		arguments.add("-y");
		arguments.add("pyodbc.x86_64");
		arguments.add("python-psycopg2.x86_64");

		RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
				COMMAND).args(arguments).build();
		runCommandFlow(aaClient, runCmdFlowContext);
	}

	private void updateDBName(IAutomationAgentClient aaClient) {

		Map<String, String> updateArgs = new HashMap<String, String>();
		updateArgs.put("cemdb", dbName);

		FileModifierFlowContext updateHost = new FileModifierFlowContext.Builder()
				.replace(installDir + "/run_loadtest.py", updateArgs).build();
		runFlow(aaClient, FileModifierFlow.class, updateHost);
	}

	private void createStartPython(IAutomationAgentClient aaClient) {
		Collection<String> commands = Arrays.asList("cd " + installDir,
				"nohup /usr/bin/python2.6 run_loadtest.py -dsn " + dbHost
						+ " -uid " + dbAdmin + " -pwd " + dbPassword
						+ " 150 20 0 360:00:00 " + webServerHost + ":"
						+ webServerPort + " > one.log 2>&1 &",
				"nohup /usr/bin/python2.6 run_loadtest.py -dsn " + dbHost
						+ " -uid " + dbAdmin + " -pwd " + dbPassword
						+ " 150 20 0 360:00:00 " + webServerHost + ":"
						+ webServerPort + " > two.log 2>&1 &");
		FileModifierFlowContext createCemUGLoadStartScript = new FileModifierFlowContext.Builder()
				.create(installDir + "/startUGLoad.sh", commands).build();

		runFlow(aaClient, createCemUGLoadStartScript);
	}

	private void createKillPython(IAutomationAgentClient aaClient) {
		Collection<String> createSHFile = Arrays
				.asList("kill $(ps aux | grep '[p]ython2.6 run_loadtest.py' | awk '{print $2}')");
		FileModifierFlowContext killSh = new FileModifierFlowContext.Builder()
				.create(installDir + "/killUGLoad.sh", createSHFile).build();
		runFlow(aaClient, FileModifierFlow.class, killSh);
	}

	public static class LinuxBuilder extends Builder {

		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends
			BuilderBase<Builder, CemUserGroupLoadRole> {
		private final String roleId;
		private final ITasResolver tasResolver;
		protected String installDir = "/opt/usergroup";
		protected String dbHost = "flddb01";
		protected String dbAdmin = "admin";
		protected String dbPassword = "quality";
		protected String dbName = "cemdb";
		protected String webServerHost = "localhost";
		protected int webServerPort = 9091;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public CemUserGroupLoadRole build() {
			setupStartCemUGLoad();
			setupStopCemUGLoad();
			return getInstance();
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected CemUserGroupLoadRole getInstance() {
			return new CemUserGroupLoadRole(this);
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder dbHost(String dbHost) {
			this.dbHost = dbHost;
			return builder();
		}

		public Builder dbAdmin(String dbAdmin) {
			this.dbAdmin = dbAdmin;
			return builder();
		}

		public Builder dbPassword(String dbPassword) {
			this.dbPassword = dbPassword;
			return builder();
		}

		public Builder dbName(String dbName) {
			this.dbName = dbName;
			return builder();
		}

		public Builder webServerHost(String webServerHost) {
			this.webServerHost = webServerHost;
			return builder();
		}

		public Builder webServerPort(int webServerPort) {
			this.webServerPort = webServerPort;
			return builder();
		}

		private void setupStartCemUGLoad() {
			RunCommandFlowContext startCemFlowContext = new RunCommandFlowContext.Builder(
					"startUGLoad.sh").workDir(installDir).build();
			getEnvProperties().add(USERGROUP_START_LOAD, startCemFlowContext);
		}

		private void setupStopCemUGLoad() {
			RunCommandFlowContext stopCemFlowContext = new RunCommandFlowContext.Builder(
					"killUGLoad.sh").workDir(installDir).build();
			getEnvProperties().add(USERGROUP_STOP_LOAD, stopCemFlowContext);
		}

	}
}