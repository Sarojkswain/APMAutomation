package com.ca.apm.tests.testbed.jvm7.acc.onprem;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.flow.CreateAccPackageFlowContext.Process;
import com.ca.apm.tests.role.WASAgentDeployRole;
import com.ca.apm.tests.testbed.jvm7.acc.Websphere8AccTestBed;
import com.ca.apm.tests.testbed.jvm8.acc.onprem.AccServerRoleHelper;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.WebSphere8Role;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;


/**
 *
 * @author sinka08
 */
@TestBedDefinition
public class Websphere8TestBed extends Websphere8AccTestBed {

	private static final Logger LOGGER = LoggerFactory.getLogger(Websphere8TestBed.class);
	private static final String LINUX_MACHINE_ID = "accServerMachine1";
	private static final String PACKAGE_NAME = "Websphere8-Windows";

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		ITestbed testBed = new Testbed(getTestBedName());
		TestbedMachine accServerMachine = new TestbedMachine.Builder(LINUX_MACHINE_ID).templateId(
		    TEMPLATE_CO66).build();

		// we have to initialize acc server roles first, agent role depends on
		// this
		Process packageProcess = Process.WEBSPHERE;
        if(isNoRedefEnabled) {
            packageProcess = Process.WEBSPHERENOREDEF;
        }
		addAccServerRoles(tasResolver, accServerMachine, packageProcess, PACKAGE_NAME);
		testBed.addMachine(accServerMachine);
		
		// add agent machine and roles
		ITestbedMachine machine1 = initMachine1(tasResolver);
		ITestbedMachine machine2 = initMachine2(tasResolver);
		
		IRole accRole = accServerMachine.getRoleById(ACC_CREATE_PACKAGE_ROLE_ID);
		IRole wasRole = machine1.getRoleById(WAS_85_ROLE_ID);
		IRole xjvmWasRole = machine2.getRoleById(WAS_85_ROLE2_ID);		
		accRole.before(wasRole, xjvmWasRole);
		
		testBed.addMachine(machine1, machine2);
		initSystemProperties(tasResolver, testBed, new HashMap<String, String>());

		return testBed;
	}	

	protected WASAgentDeployRole getWASAgentDeployRole(ITasResolver tasResolver,
	        WebSphere8Role was85Role, TestbedMachine machine) {
	    
		String accServerUrl = AccServerRoleHelper.getServerUrl(tasResolver
		        .getHostnameById(ACC_SERVER_ROLE_ID));

		LOGGER.info("ACC server url for fetching packages: {}", accServerUrl);

		return new WASAgentDeployRole.Builder(machine.getMachineId() + "_"
		        + "wasAgentDeployAppRole", tasResolver)
		        .appserverDir(codifyPath(was85Role.getInstallDir()))
		        .isNoRedefEnabled(isNoRedefEnabled)
		        .accPackageName(PACKAGE_NAME)
		        .accPackageOsName("windows")
		        .accServerUrl(accServerUrl)
		        .isLegacyMode(isLegacyMode)
		        .serverName("server1")
		        .installScriptPath(
		                codifyPath(was85Role.getInstallDir() + "/TestApps/ws_pipeorgan3")).build();
	}
}
