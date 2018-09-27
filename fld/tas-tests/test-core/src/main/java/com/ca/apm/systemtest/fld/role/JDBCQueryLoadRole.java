package com.ca.apm.systemtest.fld.role;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
 * JDBC Query Load Scripts for EM FLD.
 * 
 * Usage: new JDBCQueryLoadRole.Builder(JDBC_ROLE_ID,
 * tasResolver).emPort(5001).emHost("fldmom01").build();
 *
 * @author banra06@ca.com
 */

public class JDBCQueryLoadRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String JDBCQUERY_START_LOAD = "jdbcQueryloadStart";
	public static final String JDBCQUERY_STOP_LOAD = "jdbcQueryloadStop";
	public static final String javaFile = "TestJDBC.java";
	private int emPort;
	private String emHost;

	protected JDBCQueryLoadRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		this.emHost = builder.emHost;
		this.emPort = builder.emPort;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		getArtifacts(aaClient);
		updateMOMHostName(aaClient);
	}

	private void updateMOMHostName(IAutomationAgentClient aaClient) {

		Map<String, String> updateArgs = new HashMap<String, String>();

		updateArgs.put("fldmom01", emHost);
		updateArgs.put("5001", Integer.toString(emPort));

		FileModifierFlowContext updateHost = new FileModifierFlowContext.Builder()
				.replace(installDir + "/" + javaFile, updateArgs).build();
		runFlow(aaClient, FileModifierFlow.class, updateHost);

		Collection<String> kickoff = Arrays.asList("start runJDBC.bat");
		FileModifierFlowContext kickoffBatch = new FileModifierFlowContext.Builder()
				.create(installDir + "/kickoff.bat", kickoff).build();
		runFlow(aaClient, FileModifierFlow.class, kickoffBatch);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver.getArtifactUrl(new DefaultArtifact(
				"com.ca.apm.binaries", "fld", "JDBC", "zip", "1.0"));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	public static class Builder extends BuilderBase<Builder, JDBCQueryLoadRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		protected String installDir = "C:\\JDBC";
		protected String emHost = "localhost";
		protected int emPort = 5001;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public JDBCQueryLoadRole build() {
			// TODO Auto-generated method stub
			startJDBCLoad();
			stopJDBCLoad();
			return getInstance();
		}

		@Override
		protected JDBCQueryLoadRole getInstance() {
			// TODO Auto-generated method stub
			return new JDBCQueryLoadRole(this);
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

		public Builder emHost(String emHost) {
			this.emHost = emHost;
			return builder();
		}

		public Builder emPort(int emPort) {
			this.emPort = emPort;
			return builder();
		}

		private void startJDBCLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"kickoff.bat").workDir(installDir).build();
			getEnvProperties().add(JDBCQUERY_START_LOAD, runCmdFlowContext);

		}

		private void stopJDBCLoad() {
			String stopCommand = "wmic process where \"CommandLine like '%JDBC%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(JDBCQUERY_STOP_LOAD, runCmdFlowContext);

		}

	}

}
