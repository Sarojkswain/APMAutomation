package com.ca.apm.tests.testbed.jvm7.acc.onprem;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.flow.CreateAccPackageFlowContext.Process;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SampleAccTestBed extends AgentRegressionBaseTestBed {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleAccTestBed.class);
	private static final String LINUX_MACHINE_ID = "accServerMachine1";
	private static final String ACC_SERVER_ROLE_ID = "accServerRole1";
	private static final String PACKAGE_NAME = "Websphere8-Windows";
	private static boolean isNoRedefEnabled = false;
	
	@Override
	public ITestbed create(ITasResolver tasResolver) {

		ITestbed testBed = new Testbed("SampleAccTestBed");
		TestbedMachine accServerMachine = new TestbedMachine.Builder(LINUX_MACHINE_ID).templateId(
		    TEMPLATE_CO66).build();
		
		Process packageProcess = Process.WEBSPHERE;
        if(isNoRedefEnabled) {
            packageProcess = Process.WEBSPHERENOREDEF;
        }
        addAccServerRoles(tasResolver, accServerMachine, packageProcess, PACKAGE_NAME);
		testBed.addMachine(accServerMachine);

		return testBed;
	}	
}