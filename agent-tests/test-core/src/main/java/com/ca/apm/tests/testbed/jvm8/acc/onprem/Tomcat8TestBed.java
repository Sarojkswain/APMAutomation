package com.ca.apm.tests.testbed.jvm8.acc.onprem;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.flow.CreateAccPackageFlowContext.Process;
import com.ca.apm.tests.role.AccAgentSetupRole;
import com.ca.apm.tests.testbed.jvm8.acc.Tomcat8AccTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.TomcatRole;
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
public class Tomcat8TestBed extends Tomcat8AccTestBed {
    
	private static final Logger LOGGER = LoggerFactory.getLogger(Tomcat8TestBed.class);
	private static final String LINUX_MACHINE_ID = "accServerMachine1";
	private static final String PACKAGE_NAME = "Tomcat-Windows";

	@Override
	public ITestbed create(ITasResolver tasResolver) {
		ITestbed testBed = new Testbed(getTestBedName());
		TestbedMachine accServerMachine = new TestbedMachine.Builder(LINUX_MACHINE_ID).templateId(
		    TEMPLATE_CO66).build();

		// we have to initialize acc server roles first, agent role depends on
		// this
        Process packageProcess = Process.TOMCAT;
        if(isNoRedefEnabled) {
            packageProcess = Process.TOMCATNOREDEF;
        }
		addAccServerRoles(tasResolver, accServerMachine, packageProcess, PACKAGE_NAME);
		testBed.addMachine(accServerMachine);

		// add agent machine and roles
		ITestbedMachine machine1 = initMachine(tasResolver);
		testBed.addMachine(machine1);
		IRole accRole = accServerMachine.getRoleById(ACC_CREATE_PACKAGE_ROLE_ID);
        IRole tomcatRole = machine1.getRoleById(TOMCAT_ROLE_ID);
        accRole.before(tomcatRole);
		initSystemProperties(tasResolver, testBed, new HashMap<String, String>());

		return testBed;
	}

	protected void getTomcatAccAgent(ITasResolver tasResolver, TomcatRole tomcat,
	        TestbedMachine machine) {
		String accServerUrl = AccServerRoleHelper.getServerUrl(tasResolver
		        .getHostnameById(ACC_SERVER_ROLE_ID));

		LOGGER.info("ACC server url for fetching packages: {}", accServerUrl);

		AccAgentSetupRole tomcatAgentRole = new AccAgentSetupRole.Builder("tomcatAgentRole",
		        tasResolver).installDir(tomcat.getInstallDir()).shouldSetup(false)
		        .accServerUrl(accServerUrl).packageName(PACKAGE_NAME).osName("windows").build();
		tomcat.before(tomcatAgentRole);
		machine.addRole(tomcatAgentRole);
	}	
}
