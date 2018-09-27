package com.ca.apm.tests.testbed.jvm7.acc.onprem;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.flow.CreateAccPackageFlowContext.Process;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.role.WLSAgentAppDeployRole;
import com.ca.apm.tests.testbed.jvm7.acc.Weblogic12AccTestBed;
import com.ca.apm.tests.testbed.jvm8.acc.onprem.AccServerRoleHelper;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 */
@TestBedDefinition
public class Weblogic12TestBed extends Weblogic12AccTestBed {

	private static final Logger LOGGER = LoggerFactory.getLogger(Weblogic12TestBed.class);
	private static final String LINUX_MACHINE_ID = "accServerMachine1";
	private static final String PACKAGE_NAME = "Weblogic12-Windows";

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		ITestbed testBed = new Testbed(getTestBedName());
		TestbedMachine accServerMachine = new TestbedMachine.Builder(LINUX_MACHINE_ID).templateId(
		    TEMPLATE_CO66).build();
	
		Process packageProcess = Process.WEBLOGIC;
        if(isNoRedefEnabled) {
            packageProcess = Process.WEBLOGICNOREDEF;
        }
		addAccServerRoles(tasResolver, accServerMachine, packageProcess, PACKAGE_NAME);
		testBed.addMachine(accServerMachine);
		
		// add agent machine and roles
		ITestbedMachine machine1 = initMachine1(tasResolver);
		ITestbedMachine machine2 = initMachine2(tasResolver);
		
		IRole accRole = accServerMachine.getRoleById(ACC_CREATE_PACKAGE_ROLE_ID);
		IRole wlsRole = machine1.getRoleById(WLS_ROLE_ID);
		IRole xjvmWlsRole = machine2.getRoleById(WLS_ROLE2_ID);		
		accRole.before(wlsRole, xjvmWlsRole);
		
		testBed.addMachine(machine1, machine2);
		initSystemProperties(tasResolver, testBed, new HashMap<String, String>());

		return testBed;
	}	
	
	@Override
	protected void addWeblogicRoles(String wlsRoleId, 
                                    TestbedMachine machine, 
                                    ITasResolver tasResolver, 
                                    String agentVersion) {
        
	    String accServerUrl = AccServerRoleHelper.getServerUrl(tasResolver
            .getHostnameById(ACC_SERVER_ROLE_ID));
	    LOGGER.info("ACC server url for fetching packages: {}", accServerUrl);
	    
        //install wls
        javaRole = new CustomJavaRole.Builder(machine.getMachineId() + "_" + "java7Role", tasResolver)
             .version(CustomJavaBinary.WINDOWS_64BIT_JDK_17_0_80)
             .build();
        GenericRole wlsRole = getwls12cRole(tasResolver, wlsRoleId);        
        
        WLSAgentAppDeployRole wlsAgentPORole = new WLSAgentAppDeployRole.Builder(machine.getMachineId() + "_wlsAgentPORole", tasResolver)
            .classifier("jvm7-genericnodb")
            .isLegacyMode(isLegacyMode)
            .isNoRedefEnabled(isNoRedefEnabled)
            .accPackageName(PACKAGE_NAME)
            .accPackageOsName("windows")
            .accServerUrl(accServerUrl)            
            .isJassEnabled(isJassEnabled)
            .javaHome(javaRole.getInstallDir())
            .serverPort("7001")
            .wlsRole(wlsRoleId)
            .build();
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        javaRole.before(wlsRole);
        wlsRole.before(wlsAgentPORole);         
        machine.addRole(javaRole, wlsRole, wlsAgentPORole, fetchAgentLogs);
    }
}