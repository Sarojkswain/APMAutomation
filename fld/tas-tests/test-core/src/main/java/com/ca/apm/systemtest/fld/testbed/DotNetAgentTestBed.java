package com.ca.apm.systemtest.fld.testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.role.DotNetAgentDeployRole;
import com.ca.apm.systemtest.fld.role.DotNetAppsDeployRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
//import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * .Net Agent FLD
 *
 * @author kurma05@ca.com
 */
@TestBedDefinition
public class DotNetAgentTestBed implements FldTestbedProvider, FLDConstants, FLDLoadConstants {
	public static final String DOTNET_AGENT_ROLE_ID = "dotNetAgentRole";
	public static final String DOTNET_APPS_ROLE_ID = "dotNetAppsRole";
	public static final String IIS_ENABLE_ROLE_ID = "iisEnableRole";
	public static final String IIS_UPDATE_PORT_ROLE_ID = "iisUpdatePortRole";
	public static final String IIS_REGISTER_ROLE_ID = "iisRegisterRole";
	public static final String ODP_NET_SCRIPTS_ROLE_ID = "odpNetScriptsRole";
	public static final String COPY_TNS_FILE_ROLE_ID = "copyTnsFileRole";
	public static final String DOTNET_MACHINE1 = "dotNetMachine1";
	public static final String DOTNET_MACHINE2 = "dotNetMachine2";
	public static final String TEMPLATE_W64_JASS = "jass";

	private ITestbedMachine machine1;
    private ITestbedMachine machine2;

	@Override
	public Collection<ITestbedMachine> initMachines() {
        machine1 = new TestbedMachine.Builder(DOTNET_MACHINE1)
                .templateId(TEMPLATE_W64_JASS).build();
        machine2 = new TestbedMachine.Builder(DOTNET_MACHINE2)
                .templateId(TEMPLATE_W64_JASS).build();
        
	    return Arrays.asList(machine1, machine2);
	}
	
	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

		initMachine(tasResolver, machine1);
		initMachine(tasResolver, machine2);
	}

	@NotNull
	protected void initMachine(ITasResolver tasResolver, ITestbedMachine machine) {

		addEnableWebServerRole(tasResolver, machine);
		addPerfmonRebuildRole(tasResolver, machine);
		addTnsRole(tasResolver, machine);

		// .net test apps
		DotNetAppsDeployRole dotNetAppsRole = new DotNetAppsDeployRole.Builder(
				machine.getMachineId() + "_" + DOTNET_APPS_ROLE_ID, tasResolver)
				.installDir(TasBuilder.WIN_SOFTWARE_LOC + "testapps")
				.shouldDisableHttpLogging(true).build();

		// .net agent
		DotNetAgentDeployRole dotNetAgentRole = new DotNetAgentDeployRole.Builder(
				machine.getMachineId() + "_" + DOTNET_AGENT_ROLE_ID,
				tasResolver).installDir(TasBuilder.WIN_SOFTWARE_LOC + "dotnet")
				.isLegacyMode(false).emHost("fldmom01").build();

		IRole registerIISRole = machine.getRoleById(machine.getMachineId()
				+ "_" + IIS_REGISTER_ROLE_ID);
		IRole updatePortRole = machine.getRoleById(machine.getMachineId() + "_"
				+ IIS_UPDATE_PORT_ROLE_ID);
		dotNetAppsRole.before(dotNetAgentRole);
		updatePortRole.before(dotNetAppsRole);
		registerIISRole.before(dotNetAppsRole, dotNetAgentRole);
		machine.addRole(dotNetAppsRole, dotNetAgentRole);
	}

	private void addEnableWebServerRole(ITasResolver tasResolver, ITestbedMachine machine) {

		// enable iis components
		ArrayList<String> args = new ArrayList<String>();
		args.add("/online");
		args.add("/enable-feature");
		args.add("/featurename:IIS-WebServerRole");
		args.add("/featurename:IIS-WebServer");
		args.add("/featurename:IIS-CommonHttpFeatures");
		args.add("/featurename:IIS-StaticContent");
		args.add("/featurename:MSMQ-Server");
		args.add("/featurename:IIS-CGI");
		args.add("/featurename:IIS-ISAPIExtensions");
		args.add("/featurename:IIS-ISAPIFilter");

		RunCommandFlowContext enableIISCommand = new RunCommandFlowContext.Builder(
				"C:\\Windows\\System32\\Dism.exe").args(args).build();
		ExecutionRole enableIISRole = new ExecutionRole.Builder(
				machine.getMachineId() + "_" + IIS_ENABLE_ROLE_ID).flow(
				RunCommandFlow.class, enableIISCommand).build();

		// update port for default site
		RunCommandFlowContext updatePortCommand = new RunCommandFlowContext.Builder(
				"C:\\Windows\\system32\\inetsrv\\appcmd").args(
				Arrays.asList("set", "site", "\"Default Web Site\"",
						"/bindings:\"http/*:85:\"")).build();
		ExecutionRole updatePortRole = new ExecutionRole.Builder(
				machine.getMachineId() + "_" + IIS_UPDATE_PORT_ROLE_ID).flow(
				RunCommandFlow.class, updatePortCommand).build();

		// register iis for .net 4
		RunCommandFlowContext registerIISCommand = new RunCommandFlowContext.Builder(
				"C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\aspnet_regiis")
				.args(Arrays.asList("-i")).build();
		ExecutionRole registerIISRole = new ExecutionRole.Builder(
				machine.getMachineId() + "_" + IIS_REGISTER_ROLE_ID).flow(
				RunCommandFlow.class, registerIISCommand).build();

		enableIISRole.before(updatePortRole, registerIISRole);
		machine.addRole(enableIISRole);
		machine.addRole(registerIISRole);
		machine.addRole(updatePortRole);
	}

	protected void addTnsRole(ITasResolver tasResolver, ITestbedMachine machine) {

		String installBaseDir = TasBuilder.WIN_SOFTWARE_LOC + "oracle\\";

		// get dist package
		DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.tests",
				"agent-tests-core", "dist_dotnet", "zip",
				tasResolver.getDefaultVersion());
		GenericRole distRole = new GenericRole.Builder(machine.getMachineId()
				+ "_" + ODP_NET_SCRIPTS_ROLE_ID, tasResolver).unpack(artifact,
				installBaseDir).build();

		// copy tns file
		String source = installBaseDir + "tnsnames.sc.oracle.ora";
		String dest = "C:\\SW\\oracle\\product\\12.1.0\\client_1\\Network\\Admin\\tnsnames.ora";

		ExecutionRole tnsRole = new ExecutionRole.Builder(
				machine.getMachineId() + "_" + COPY_TNS_FILE_ROLE_ID).flow(
				FileModifierFlow.class,
				new FileModifierFlowContext.Builder().copy(source, dest)
						.build()).build();

		distRole.before(tnsRole);
		machine.addRole(distRole);
		machine.addRole(tnsRole);
	}

	private void addPerfmonRebuildRole(ITasResolver tasResolver,
			ITestbedMachine machine) {

		// applicable to windows vms - sometimes perfmon counters are
		// missing when vm gets cloned; have to rebuid them
		RunCommandFlowContext context = new RunCommandFlowContext.Builder(
				"lodctr").workDir("C:\\Windows\\system32")
				.args(Arrays.asList("/r")).build();

		ExecutionRole role = new ExecutionRole.Builder(machine.getMachineId()
				+ "_perfmonRebuildRole").flow(RunCommandFlow.class, context)
				.build();

		machine.addRole(role);
	}
}
