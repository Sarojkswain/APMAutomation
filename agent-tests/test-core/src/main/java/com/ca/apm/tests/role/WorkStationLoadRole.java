package com.ca.apm.tests.role;


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
 * JDBC Query Load Scripts for EM FLD.
 * 
 * Usage: new JDBCQueryLoadRole.Builder(JDBC_ROLE_ID,
 * tasResolver).emPort(5001).emHost("fldmom01").build();
 *
 * @author banra06@ca.com
 */

public class WorkStationLoadRole extends AbstractRole {

	private String installDir;
	private ITasResolver tasResolver;
	public static final String WS_START_LOAD = "workstationloadStart";
	public static final String WS_STOP_LOAD = "workstationloadStop";
	private int emPort;
	private String emHost;
	private String branch;
	

	protected WorkStationLoadRole(Builder builder) {

		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.installDir = builder.installDir;
		this.emHost = builder.emHost;
		this.emPort = builder.emPort;
		this.branch=builder.branch;
		
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {

		createBatchFile(aaClient);
	}

	private void createBatchFile(IAutomationAgentClient aaClient) {

		Collection<String> ws = Arrays.asList(TasBuilder.WIN_SOFTWARE_LOC+"/workstation-"+branch+"/Introscope_Workstation.exe -loginimmediate -loginhost "+emHost+" -loginport "+emPort+" -loginresponse admin");
		FileModifierFlowContext wsBatch = new FileModifierFlowContext.Builder()
				.create(installDir + "/workstationLoad.bat", ws).build();
		runFlow(aaClient, FileModifierFlow.class, wsBatch);
		
		Collection<String> kickoff = Arrays.asList("start workstationLoad.bat","ping -n 180 127.0.0.1","start workstationLoad.bat","ping -n 180 127.0.0.1","start workstationLoad.bat");
		FileModifierFlowContext kickoffBatch = new FileModifierFlowContext.Builder()
		.create(installDir + "/kickoff.bat", kickoff).build();
		runFlow(aaClient, FileModifierFlow.class, kickoffBatch);
		
		
	}

	
	public static class Builder extends BuilderBase<Builder, WorkStationLoadRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		protected String installDir = "C:/WS/";
		protected String emHost = "localhost";
		protected int emPort = 5001;
		protected String branch="99.99.sys-SNAPSHOT";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public WorkStationLoadRole build() {
			// TODO Auto-generated method stub
			startWSLoad();
			stopWSLoad();
			return getInstance();
		}

		@Override
		protected WorkStationLoadRole getInstance() {
			// TODO Auto-generated method stub
			return new WorkStationLoadRole(this);
		}

		@Override
		protected Builder builder() {
			// TODO Auto-generated method stub
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
