package com.ca.apm.systemtest.fld.role.loads;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

/**
 * 
 * This Wurlitzer Load Role is capable of downloading the artifact from artifactory (preassigned 
 * location) and changing the target EM information and port numbers for all wurlitzer subscripts 
 * (including the second cluster loads). The load start and stop triggers (for both main and second 
 * clusters) are also defined. A test can call the four triggers to start/stop the loads. This
 * works in conjunction with com.ca.apm.systemtest.fld.testbed.FLDWurlitzerProvider which invokes this
 * role and adds that to a machine from testbed.
 * 
 * Typical invocation: new FLDWurlitzerLoadRole.Builder(LOAD_ROLE_ID, tasResolver)
								.updateAllCollectorRoleIDs(COLL_ROLES)
								.updateEMPortNumber(EM_PORT_NUMBER)
								.updateAllCollectorRoleIDsInSecondCluster(COLL_ROLES_SECOND_CLUSTER)
								.updateEMPortNumberInSecondCluster(EM_PORT_NUMBER_SECOND_CLUSTER)
								.build();
								
 * Ideally, FLDWurlitzerProvider should be used and not the above code directly.
 *
 * @author sinab10@ca.com
 */

public class FLDWurlitzerLoadRole extends AbstractFldLoadRole {

	private ITasResolver tasResolver;
	private String deployScriptsDir;

	private static String EM01_ROLE_ID = "localhost_Role_ID";
	private static String EM02_ROLE_ID = "localhost_Role_ID";
	private static String EM03_ROLE_ID = "localhost_Role_ID";
	private static String EM04_ROLE_ID = "localhost_Role_ID";
	private static String EM05_ROLE_ID = "localhost_Role_ID";
	private static String EM06_ROLE_ID = "localhost_Role_ID";
	private static String EM07_ROLE_ID = "localhost_Role_ID";
	private static String EM08_ROLE_ID = "localhost_Role_ID";
	private static String EM09_ROLE_ID = "localhost_Role_ID";
	private static String EM10_ROLE_ID = "localhost_Role_ID";

	private static String EM11_ROLE_ID = "localhost_Role_ID";
	private static String EM12_ROLE_ID = "localhost_Role_ID";
	private static String EM13_ROLE_ID = "localhost_Role_ID";

	private static String EM_PORT_NUMBER = "5001";
	private static String EM_PORT_NUMBER_CLUSTER2 = "5001";

	public static final String WURLITZER_START_TRIGGER = "Wurlitzer_Start_Trigger";
	public static final String WURLITZER_STOP_TRIGGER = "Wurlitzer_Stop_Trigger";
	public static final String SECOND_CLUSTER_START_TRIGGER = "Second_Cluster_Start_Trigger";
	public static final String SECOND_CLUSTER_STOP_TRIGGER = "Second_Cluster_Stop_Trigger";

	public FLDWurlitzerLoadRole(Builder builder) {
		super(builder.roleId, builder.getEnvProperties());
		this.tasResolver = builder.tasResolver;
		this.deployScriptsDir = builder.deployScriptsDir;
	}

	@Override
	public void deploy(IAutomationAgentClient aaClient) {
		deployWurlitzer(tasResolver, aaClient);
		updateEMInfoInAllScripts(tasResolver, aaClient);
		createStartWzScript(aaClient);
		createStopWzScript(aaClient);
		createStartSecondClusterLoadScript(aaClient);
		createStopSecondClusterLoadScript(aaClient);
	}

	private void deployWurlitzer(ITasResolver tasResolver,
			IAutomationAgentClient aaClient) {
		DefaultArtifact wurlitzerArtifact = new DefaultArtifact(
				"com.ca.apm.binaries", "wurlitzer", "zip", "1.0");
		URL url = tasResolver.getArtifactUrl(wurlitzerArtifact);

		GenericFlowContext getWzContext = new GenericFlowContext.Builder()
				.artifactUrl(url).destination(deployScriptsDir).build();

		runFlow(aaClient, GenericFlow.class, getWzContext);
	}

	private void updateEMInfoInAllScripts(ITasResolver tasResolver,
			IAutomationAgentClient aaClient) {

		updateEMInfoInTargetScript("FLD_MoM_baseline.bat", EM03_ROLE_ID,
				EM_PORT_NUMBER, tasResolver, aaClient);

		updateEMInfoInTargetScript("FLD_C1_fldcoll01_Complex100.bat",
				EM01_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C2_fldcoll02_Complex100.bat",
				EM02_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C3_fldcoll03_Portlet20.bat",
				EM03_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C4_fldcoll04_Complex200.bat",
				EM04_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C5_fldcoll05_2000Backends.bat",
				EM05_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C5_fldcoll05_Complex300.bat",
				EM05_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C6_fldcoll06_Complex350.bat",
				EM06_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C7_fldcoll07_Portlet20.bat",
				EM07_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C8_fldcoll08_Portlet20.bat",
				EM08_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C9_fldcoll09_2000Backends.bat",
				EM09_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C9_fldcoll09_Complex100.bat",
				EM09_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C10_fldcoll10_2000Backends.bat",
				EM10_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_C10_fldcoll10_Portlet20.bat",
				EM10_ROLE_ID, EM_PORT_NUMBER, tasResolver, aaClient);

		updateEMInfoInTargetScript("FLD_Cluster2_C1_500Apps.bat",
				EM11_ROLE_ID, EM_PORT_NUMBER_CLUSTER2, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_Cluster2_C2_Complex100.bat",
				EM12_ROLE_ID, EM_PORT_NUMBER_CLUSTER2, tasResolver, aaClient);
		updateEMInfoInTargetScript("FLD_Cluster3_C1_Complex100.bat",
				EM13_ROLE_ID, EM_PORT_NUMBER_CLUSTER2, tasResolver, aaClient);
	}

	private void updateEMInfoInTargetScript(String scriptFile,
			String targetEMRoleID, String targetEMMachinePort,
			ITasResolver tasResolver, IAutomationAgentClient aaClient) {

		String emMachineHost;

		if (targetEMRoleID == "localhost_Role_ID")
			emMachineHost = "localhost";
		else
			emMachineHost = tasResolver.getHostnameById(targetEMRoleID);

		Map<String, String> updateData = new HashMap<String, String>();
		updateData.put("\\[EM_HOST\\]", emMachineHost);
		updateData.put("\\[EM_PORT\\]", targetEMMachinePort);

		FileModifierFlowContext getFileModificationContext = new FileModifierFlowContext.Builder()
				.replace(deployScriptsDir + "\\" + scriptFile, updateData)
				.build();

		runFlow(aaClient, getFileModificationContext);
	}

	private void createStartWzScript(IAutomationAgentClient aaClient) {

		Collection<String> commands = Arrays.asList("start allStart.bat");
		FileModifierFlowContext createWzKickOffScript = new FileModifierFlowContext.Builder()
				.create(deployScriptsDir + "\\" + "StartWz.bat", commands)
				.build();

		runFlow(aaClient, createWzKickOffScript);

	}

	private void createStopWzScript(IAutomationAgentClient aaClient) {

		Collection<String> commands = Arrays
				.asList("wmic process where \"CommandLine like '%%appmap-stress%%' "
						+ "and not (CommandLine like '%%wmic%%')\" Call Terminate",
						"wmic process where \"CommandLine like '%%allStart%%' "
								+ "and not (CommandLine like '%%wmic%%')\" Call Terminate");

		FileModifierFlowContext createStopWzScript = new FileModifierFlowContext.Builder()
				.create(deployScriptsDir + "\\" + "StopWz.bat", commands)
				.build();

		runFlow(aaClient, createStopWzScript);

	}

	private void createStartSecondClusterLoadScript(
			IAutomationAgentClient aaClient) {

		Collection<String> commands = Arrays
				.asList("start StartNewClusterLoads.bat");
		FileModifierFlowContext createSecondClusterLoadStartScript = new FileModifierFlowContext.Builder()
				.create(deployScriptsDir + "\\" + "StartSecondClusterWz.bat",
						commands).build();

		runFlow(aaClient, createSecondClusterLoadStartScript);

	}

	private void createStopSecondClusterLoadScript(
			IAutomationAgentClient aaClient) {

		Collection<String> commands = Arrays
				.asList("wmic process where \"CommandLine like '%%appmap-stress%%' "
						+ "and not (CommandLine like '%%wmic%%')\" Call Terminate",
						"wmic process where \"CommandLine like '%%StartNewClusterLoads%%' "
								+ "and not (CommandLine like '%%wmic%%')\" Call Terminate");

		FileModifierFlowContext createStopSecondClusterLoadScript = new FileModifierFlowContext.Builder()
				.create(deployScriptsDir + "\\" + "StopSecondClusterWz.bat",
						commands).build();

		runFlow(aaClient, createStopSecondClusterLoadScript);

	}

	public static class Builder extends
			BuilderBase<Builder, FLDWurlitzerLoadRole> {

		private final String roleId;
		private final ITasResolver tasResolver;
		protected String deployScriptsDir = "C:\\Wurlitzer";

		public Builder(String roleId, ITasResolver tasResolver) {
			this.roleId = roleId;
			this.tasResolver = tasResolver;
		}

		@Override
		public FLDWurlitzerLoadRole build() {
			setupStartWurlitzerLoad();
			setupStopWurlitzerLoad();
			setupStartSecondClusterLoad();
			setupStopSecondClusterLoad();
			return getInstance();
		}

		@Override
		protected FLDWurlitzerLoadRole getInstance() {
			return new FLDWurlitzerLoadRole(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder deployScriptsDir(String deployScriptsDir) {
			this.deployScriptsDir = deployScriptsDir;
			return builder();
		}

		public Builder updateAllCollectorRoleIDs(String[] collRoleIDs) {
			EM01_ROLE_ID = collRoleIDs[0];
			EM02_ROLE_ID = collRoleIDs[1];
			EM03_ROLE_ID = collRoleIDs[2];
			EM04_ROLE_ID = collRoleIDs[3];
			EM05_ROLE_ID = collRoleIDs[4];
			EM06_ROLE_ID = collRoleIDs[5];
			EM07_ROLE_ID = collRoleIDs[6];
			EM08_ROLE_ID = collRoleIDs[7];
			EM09_ROLE_ID = collRoleIDs[8];
			EM10_ROLE_ID = collRoleIDs[9];
			return builder();
		}

		public Builder updateEMPortNumber(String portNo) {
			EM_PORT_NUMBER = portNo;
			return builder();
		}

		public Builder updateAllCollectorRoleIDsInSecondCluster(
				String[] collRoleIDs) {
			EM11_ROLE_ID = collRoleIDs[0];
			EM12_ROLE_ID = collRoleIDs[1];
			EM13_ROLE_ID = collRoleIDs[2];
			return builder();
		}

		public Builder updateEMPortNumberInSecondCluster(String portNo) {
			EM_PORT_NUMBER_CLUSTER2 = portNo;
			return builder();
		}

		private void setupStartWurlitzerLoad() {
			RunCommandFlowContext startWzFlowContext = new RunCommandFlowContext.Builder(
					"StartWz.bat").workDir(deployScriptsDir).build();
			getEnvProperties().add(WURLITZER_START_TRIGGER, startWzFlowContext);
		}

		private void setupStopWurlitzerLoad() {
			RunCommandFlowContext stopWzFlowContext = new RunCommandFlowContext.Builder(
					"StopWz.bat").workDir(deployScriptsDir).build();
			getEnvProperties().add(WURLITZER_STOP_TRIGGER, stopWzFlowContext);
		}

		private void setupStartSecondClusterLoad() {
			RunCommandFlowContext startSecondClusterFlowContext = new RunCommandFlowContext.Builder(
					"StartSecondClusterWz.bat").workDir(deployScriptsDir)
					.build();
			getEnvProperties().add(SECOND_CLUSTER_START_TRIGGER,
					startSecondClusterFlowContext);
		}

		private void setupStopSecondClusterLoad() {
			RunCommandFlowContext stopSecondClusterFlowContext = new RunCommandFlowContext.Builder(
					"StopSecondClusterWz.bat").workDir(deployScriptsDir)
					.build();
			getEnvProperties().add(SECOND_CLUSTER_STOP_TRIGGER,
					stopSecondClusterFlowContext);
		}
	}
}
