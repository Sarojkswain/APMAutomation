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
 * Geo Location Load Script for EM FLD. This role should be added to any of the
 * one FLD TIM machine. So that the btstat files are generated and placed in
 * tim/out/data/btstats folder.
 * 
 * Usage: new GeolocationLoadRole.LinuxBuilder(ROLE_ID,
 * tasResolver).dbHost("flddb01")
 * .dbPort(5432).dbAdmin("admin").dbPassword("quality")
 * .dbName("cemdb").build();
 *
 * @author banra06@ca.com
 */

public class GeolocationLoadRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String GEOLOCATION_START_LOAD = "geoloadStart";
	public static final String GEOLOCATION_STOP_LOAD = "geoloadStop";
	private static final String COMMAND = "yum";
	private String dbHost;
	private int dbPort;
	private String dbAdmin;
	private String dbPassword;
	private String dbName;

	protected GeolocationLoadRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		this.dbHost = builder.dbHost;
		this.dbPort = builder.dbPort;
		this.dbAdmin = builder.dbAdmin;
		this.dbPassword = builder.dbPassword;
		this.dbName = builder.dbName;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		getArtifacts(aaClient);
		updateDBHostName(aaClient);
		installYumPackages(aaClient);
		createStartPython(aaClient);
		createKillPython(aaClient);

	}

	private void updateDBHostName(IAutomationAgentClient aaClient) {

		Map<String, String> updateArgs = new HashMap<String, String>();

		updateArgs.put("flddb01", dbHost);
		updateArgs.put("5432", Integer.toString(dbPort));
		updateArgs.put("uid='admin'", "uid='" + dbAdmin + "'");
		updateArgs.put("pwd='quality'", "pwd='" + dbPassword + "'");
		updateArgs.put("cemdb", dbName);

		FileModifierFlowContext updateHost = new FileModifierFlowContext.Builder()
				.replace(installDir + "demo.py", updateArgs).build();
		runFlow(aaClient, FileModifierFlow.class, updateHost);
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

	private void createKillPython(IAutomationAgentClient aaClient) {
		Collection<String> createSHFile = Arrays
				.asList("kill $(ps aux | grep '[p]ython2.6 demo.py' | awk '{print $2}')");
		FileModifierFlowContext killSh = new FileModifierFlowContext.Builder()
				.create(installDir + "/killGeo.sh", createSHFile).build();
		runFlow(aaClient, FileModifierFlow.class, killSh);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "fld", "fld", "tar.gz", "1.0"));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	private void createStartPython(IAutomationAgentClient aaClient) {
		Collection<String> createSHFile = Arrays.asList("cd " + installDir,
				"nohup /usr/bin/python2.6 demo.py > out.log 2>&1 &");
		FileModifierFlowContext kickoffSH = new FileModifierFlowContext.Builder()
				.create(installDir + "/runGeo.sh", createSHFile).build();
		runFlow(aaClient, FileModifierFlow.class, kickoffSH);
	}

	public static class LinuxBuilder extends Builder {

		public LinuxBuilder(String roleId, ITasResolver tasResolver) {
			super(roleId, tasResolver);
		}
	}

	public static class Builder extends
			BuilderBase<Builder, GeolocationLoadRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		protected String installDir = "/opt/geolocation/";
		protected String dbHost = "localhost";
		protected int dbPort = 5432;
		protected String dbAdmin = "admin";
		protected String dbPassword = "quality";
		protected String dbName = "cemdb";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public GeolocationLoadRole build() {
			// TODO Auto-generated method stub
			startGeoLoad();
			stopGeoLoad();
			return getInstance();
		}

		@Override
		protected GeolocationLoadRole getInstance() {
			// TODO Auto-generated method stub
			return new GeolocationLoadRole(this);
		}

		@Override
		protected Builder builder() {
			// TODO Auto-generated method stub
			return this;
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		public Builder dbHost(String dbHost) {
			this.dbHost = dbHost;
			return builder();
		}

		public Builder dbPort(int dbPort) {
			this.dbPort = dbPort;
			return builder();
		}

		public Builder dbAdmin(String dbAdmin) {
			this.dbAdmin = dbAdmin;
			return builder();
		}

		public Builder dbName(String dbName) {
			this.dbName = dbName;
			return builder();
		}

		public Builder dbPassword(String dbPassword) {
			this.dbPassword = dbPassword;
			return builder();
		}

		private void startGeoLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"runGeo.sh").workDir(installDir).build();
			getEnvProperties().add(GEOLOCATION_START_LOAD, runCmdFlowContext);

		}

		private void stopGeoLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"killGeo.sh").workDir(installDir).build();
			getEnvProperties().add(GEOLOCATION_STOP_LOAD, runCmdFlowContext);

		}

	}

}
