package com.ca.apm.systemtest.fld.role;

import java.net.URL;
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
 * Disallowed Agent Load Scripts for EM FLD.
 *
 * @author banra06@ca.com
 */

public class ACCLoadRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String ACC_START_LOAD = "accloadStart";
	public static final String ACC_STOP_LOAD = "accloadStop";
	public String serverFile = "mini-agent.properties";
	private int emPort;
	private String emHost;

	protected ACCLoadRole(Builder builder) {

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
				.replace(
						installDir + "\\scripts\\xml\\appmap-btc-miniagent\\"
								+ serverFile, updateArgs).build();
		runFlow(aaClient, FileModifierFlow.class, updateHost);
	}

	private void getArtifacts(IAutomationAgentClient aaClient) {

		URL url = tasResolver
				.getArtifactUrl(new DefaultArtifact("com.ca.apm.binaries",
						"fld", "DisallowedAgents", "zip", "1.0"));
		GenericFlowContext getAgentContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(installDir).build();
		runFlow(aaClient, GenericFlow.class, getAgentContext);
	}

	public static class Builder extends BuilderBase<Builder, ACCLoadRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		protected String installDir = "C:\\DisallowedAgents";
		protected String emHost = "localhost";
		protected int emPort = 5001;

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public ACCLoadRole build() {
			// TODO Auto-generated method stub
			startACCLoad();
			stopACCLoad();
			return getInstance();
		}

		@Override
		protected ACCLoadRole getInstance() {
			// TODO Auto-generated method stub
			return new ACCLoadRole(this);
		}

		@Override
		protected Builder builder() {
			// TODO Auto-generated method stub
			return this;
		}

		public Builder emHost(String emHost) {
			this.emHost = emHost;
			return builder();
		}

		public Builder emPort(int emPort) {
			this.emPort = emPort;
			return builder();
		}

		public Builder installDir(String installDir) {
			this.installDir = installDir;
			return builder();
		}

		private void startACCLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"RunACC.bat")
					.workDir(
							installDir
									+ "\\scripts\\xml\\appmap-btc-miniagent\\")
					.terminateOnMatch("Introscope Agent startup complete")
					.build();
			getEnvProperties().add(ACC_START_LOAD, runCmdFlowContext);

		}

		private void stopACCLoad() {
			String stopCommand = "wmic process where \"CommandLine like '%disallowedagent%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(ACC_STOP_LOAD, runCmdFlowContext);

		}

	}

}
