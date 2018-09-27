package com.ca.apm.systemtest.fld.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.WLSAgentAppDeployRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author banra06
 * 
 */
@TestBedDefinition
public class Weblogic12TestBed implements FLDConstants, FLDLoadConstants, FldTestbedProvider {

	public static final String WLS_ROLE_ID = "wls12c";
	public static final String WLS_ROLE2_ID = "wls12c_role2";
	private JavaRole javaRole;
	public static final String WLSMACHINE_1 = "wlsmachine1";
	public static final String WLSMACHINE_2 = "wlsmachine2";
	protected boolean isJassEnabled = false;
	protected boolean isLegacyMode = false;
	public String defaultAgentTemplateId = TEMPLATE_W64;
	protected static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
	protected static final String WLS12C_INSTALL_HOME = DEPLOY_BASE
			+ "Oracle/Middleware12.1.3";
	protected final String PIPEORGAN_LOGGING_LEVEL = "quiet"; // [debug, verbose, quiet, nologging]

	@Override
	public Collection<ITestbedMachine> initMachines() {
	    return Collections.emptySet();
	}
	
	@Override
	public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

		ITestbedMachine machine1 = initMachine1(tasResolver);
		ITestbedMachine machine2 = initMachine2(tasResolver);
		testbed.addMachine(machine1, machine2);

	}

	@NotNull
	protected ITestbedMachine initMachine1(ITasResolver tasResolver) {

		TestbedMachine machine = new TestbedMachine.Builder(WLSMACHINE_1)
				.templateId(defaultAgentTemplateId).build();

		// Client resources
		machine.addRole(new ClientDeployRole.Builder("wls_client01",
				tasResolver).jvmVersion("8").shouldDeployConsoleApps(true)
				.shouldDeployJassApps(isJassEnabled)
				.fldXjvmhost(tasResolver.getHostnameById(WLS_ROLE2_ID))
				.pipeorganLoggingLevel(PIPEORGAN_LOGGING_LEVEL)
				.build());

		// String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
		// Uncomment above line and replace fldmom01 with emHost

		addWeblogicRoles(WLS_ROLE_ID, machine, tasResolver, "fldmom01");
		return machine;
	}

	@NotNull
	protected ITestbedMachine initMachine2(ITasResolver tasResolver) {

		TestbedMachine machine = new TestbedMachine.Builder(WLSMACHINE_2)
				.templateId(defaultAgentTemplateId).build();

		// String emHost = tasResolver.getHostnameById(AGC_ROLE_ID);
		// Uncomment above line and replace fldmom01 with emHost
		addWeblogicRoles(WLS_ROLE2_ID, machine, tasResolver, "fldmom01");

		return machine;
	}

	protected void addWeblogicRoles(String wlsRoleId, TestbedMachine machine,
			ITasResolver tasResolver, String emHost) {

		addWeblogicRoles(wlsRoleId, machine, tasResolver,
				tasResolver.getDefaultVersion(), emHost);
	}

	protected void addWeblogicRoles(String wlsRoleId, TestbedMachine machine,
			ITasResolver tasResolver, String agentVersion, String emHost) {

		// install wls
		javaRole = new JavaRole.Builder(machine.getMachineId() + "_"
				+ "java8Role", tasResolver).version(
				JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();

		GenericRole wlsRole = getwls12cRole(tasResolver, wlsRoleId);

		WLSAgentAppDeployRole wlsAgentPORole = new WLSAgentAppDeployRole.Builder(
				machine.getMachineId() + "_" + wlsRoleId, tasResolver)
				.agentVersion(agentVersion).classifier("jvm7-genericnodb")
				.isLegacyMode(isLegacyMode).isJassEnabled(isJassEnabled)
				.javaRole(javaRole).serverPort("7001").wlsRole(wlsRoleId)
				.pipeorganLoggingLevel(PIPEORGAN_LOGGING_LEVEL)
				.redirectStdoutToServerLog()
				.emHost(emHost).build();

		javaRole.before(wlsRole);
		wlsRole.before(wlsAgentPORole);
		machine.addRole(javaRole, wlsRole, wlsAgentPORole);
	}

	@NotNull
	protected GenericRole getwls12cRole(ITasResolver tasResolver, String wlsRole) {

		ArrayList<String> args = new ArrayList<String>();
		args.add("-silent");

		RunCommandFlowContext installWlc12cCommand = new RunCommandFlowContext.Builder(
				"configure.cmd").workDir(WLS12C_INSTALL_HOME).args(args)
				.build();

		GenericRole wls12cInstallRole = new GenericRole.Builder(wlsRole,
				tasResolver)
				.unpack(new DefaultArtifact("com.ca.apm.binaries", "weblogic",
						"dev", "zip", "12.1.3"),
						codifyPath(WLS12C_INSTALL_HOME))
				.runCommand(installWlc12cCommand).build();

		return wls12cInstallRole;
	}

	@NotNull
	protected String codifyPath(String path) {
		return FilenameUtils.separatorsToUnix(path);
	}

}