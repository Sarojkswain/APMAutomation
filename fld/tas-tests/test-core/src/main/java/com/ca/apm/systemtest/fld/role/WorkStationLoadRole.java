package com.ca.apm.systemtest.fld.role;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * WorkStation Load Scripts for EM FLD.
 * 
 * Usage: new WorkStationLoadRole.Builder( WORKSTATION_LOAD_ROLE_ID,
 * tasResolver) .emHost(tasResolver.getHostnameById(EM_MOM_ROLE_ID))
 * .emPort(5001).branch("99.99.phoenix-SNAPSHOT").build();
 *
 * @author banra06@ca.com
 */

public class WorkStationLoadRole extends AbstractRole {

	private String installDir;
	public static final String WS_START_LOAD = "workstationloadStart";
	public static final String WS_STOP_LOAD = "workstationloadStop";
	private int emPort;
	private String emHost;
	private String branch;

	protected WorkStationLoadRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.installDir = builder.installDir;
		this.emHost = builder.emHost;
		this.emPort = builder.emPort;
		this.branch = builder.branch;

	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		createBatchFile(aaClient);
	}

	private void createBatchFile(IAutomationAgentClient aaClient) {

		Collection<String> ws = Arrays.asList(TasBuilder.WIN_SOFTWARE_LOC
				+ "/workstation-" + branch
				+ "/Introscope_Workstation.exe -loginimmediate -loginhost "
				+ emHost + " -loginport " + emPort + " -loginresponse admin");
		FileModifierFlowContext wsBatch = new FileModifierFlowContext.Builder()
				.create(installDir + "/workstationLoad.bat", ws).build();
		runFlow(aaClient, FileModifierFlow.class, wsBatch);

		Collection<String> kickoff1 = Arrays.asList(
				"start workstationLoad.bat", "ping -n 300 127.0.0.1",
				"start workstationLoad.bat", "ping -n 300 127.0.0.1",
				"start workstationLoad.bat");
		FileModifierFlowContext kickoffBatch1 = new FileModifierFlowContext.Builder()
				.create(installDir + "/kickoff.bat", kickoff1).build();
		runFlow(aaClient, FileModifierFlow.class, kickoffBatch1);

		Collection<String> kickoff2 = Arrays.asList("start kickoff.bat");
		FileModifierFlowContext kickoffBatch2 = new FileModifierFlowContext.Builder()
				.create(installDir + "/startkickoff.bat", kickoff2).build();
		runFlow(aaClient, FileModifierFlow.class, kickoffBatch2);
	}

	public static class Builder extends
			BuilderBase<Builder, WorkStationLoadRole> {

		private final String roleId;
		protected String installDir = "C:/WS/";
		protected String emHost = "localhost";
		protected int emPort = 5001;
		protected String branch = "99.99.sys-SNAPSHOT";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
		}

		@Override
		public WorkStationLoadRole build() {
			startWSLoad();
			stopWSLoad();
			return getInstance();
		}

		@Override
		protected WorkStationLoadRole getInstance() {
			return new WorkStationLoadRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder branch(String branch) {
			this.branch = branch;
			return builder();
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

		private void startWSLoad() {
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					"kickoff.bat").workDir(installDir).build();
			getEnvProperties().add(WS_START_LOAD, runCmdFlowContext);

		}

		private void stopWSLoad() {
			String stopCommand = "wmic process where \"CommandLine like '%loginimmediate%' and not (CommandLine like '%wmic%')\" Call Terminate";
			RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext.Builder(
					stopCommand).build();
			getEnvProperties().add(WS_STOP_LOAD, runCmdFlowContext);

		}

	}

}
