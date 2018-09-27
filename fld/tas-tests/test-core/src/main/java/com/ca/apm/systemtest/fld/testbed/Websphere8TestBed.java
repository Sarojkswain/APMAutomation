package com.ca.apm.systemtest.fld.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.WASAgentDeployRole;
import com.ca.apm.systemtest.fld.role.WASWebappDeployRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.WebSphere8FixPacksVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8JavaVersion;
import com.ca.tas.artifact.thirdParty.WebSphere8Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author banra06
 */
@TestBedDefinition
public class Websphere8TestBed implements FLDConstants, FLDLoadConstants,
		FldTestbedProvider {

	public static final String WAS_85_ROLE_ID = "was85";
	public static final String WAS_85_ROLE2_ID = "was85_role2";
	private JavaRole javaRole;
	public static final String WASMACHINE_1 = "wasmachine1";
	public static final String WASMACHINE_2 = "wasmachine2";
	protected boolean isJassEnabled = false;
	protected boolean isLegacyMode = false;
	public String defaultAgentTemplateId = TEMPLATE_W64;
	
	
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

		TestbedMachine machine = new TestbedMachine.Builder(WASMACHINE_1)
				.templateId(defaultAgentTemplateId).build();
		javaRole = new JavaRole.Builder("java7Role", tasResolver).version(
				JavaBinary.WINDOWS_64BIT_JDK_17).build();

		// Adding client resources
		machine.addRole(new ClientDeployRole.Builder("was_client01",
				tasResolver).jvmVersion("7").shouldDeployConsoleApps(true)
				.shouldDeployJassApps(isJassEnabled)
				.fldXjvmhost(tasResolver.getHostnameById(WAS_85_ROLE2_ID))
				.build());

		// String emHost = tasResolver.getHostnameById(EM_MOM2_ROLE_ID);
		// Uncomment above line and replace fldmom01 with emHost

		addWebsphereRoles(WAS_85_ROLE_ID, machine, tasResolver, "fldmom01");
		return machine.addRole(javaRole);
	}

	@NotNull
	protected ITestbedMachine initMachine2(ITasResolver tasResolver) {

		TestbedMachine machine = new TestbedMachine.Builder(WASMACHINE_2)
				.templateId(defaultAgentTemplateId).build();

		// String emHost = tasResolver.getHostnameById(AGC_ROLE_ID);
		// Uncomment above line and replace fldmom01 with emHost

		addWebsphereRoles(WAS_85_ROLE2_ID, machine, tasResolver, "fldmom01");

		return machine;
	}

	private void addWebsphereRoles(String role, TestbedMachine machine,
			ITasResolver tasResolver, String emHost) {

		// install was
		WebSphere8Role was85Role = new WebSphere8Role.Builder(role, tasResolver)
				.wasVersion(WebSphere8Version.v85base)
				.wasFixPackVersion(WebSphere8FixPacksVersion.v8557base)
				.wasJavaVersion(WebSphere8JavaVersion.v71310for8557x64).build();

		// install was webapps
		WASWebappDeployRole wasWebappDeployAppRole = new WASWebappDeployRole.Builder(
				machine.getMachineId() + "_" + "wasWebappDeployAppRole",
				tasResolver)
				.appserverDir(codifyPath(was85Role.getInstallDir()))
				.profileName("AppSrv01").serverName("server1")
				.nodeName(tasResolver.getHostnameById(role) + "Node01")
				.minHeapSize("512").maxHeapSize("1024").permSpaceSize("256")
				.maxPermSpaceSize("1024").shouldDeployJassApps(isJassEnabled)
				.build();

		// install was agent
		WASAgentDeployRole wasAgentDeployAppRole = new WASAgentDeployRole.Builder(
				machine.getMachineId() + "_" + "wasAgentDeployAppRole",
				tasResolver)
				.appserverDir(codifyPath(was85Role.getInstallDir()))
				.isLegacyMode(isLegacyMode)
				.serverName("server1")
				.installScriptPath(
						codifyPath(was85Role.getInstallDir()
								+ "/TestApps/ws_pipeorgan3")).emHost(emHost)
				.build();

		// add roles
		wasWebappDeployAppRole.after(was85Role);
		wasAgentDeployAppRole.after(wasWebappDeployAppRole);
		machine.addRole(was85Role, wasWebappDeployAppRole,
				wasAgentDeployAppRole);
	}

	@NotNull
	protected String codifyPath(String path) {
		return FilenameUtils.separatorsToUnix(path);
	}
}