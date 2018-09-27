package com.ca.apm.tests.testbed.jvm8;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.tests.role.ClientDeployRole;
import com.ca.apm.tests.role.CustomJavaBinary;
import com.ca.apm.tests.role.CustomJavaRole;
import com.ca.apm.tests.role.FetchAgentLogsRole;
import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author kurma05
 * TAS testbed without CODA bridge
 */
@TestBedDefinition
public class EpagentTestBed extends AgentRegressionBaseTestBed {
    
    private CustomJavaRole javaRole;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {         
        
        ITestbed testBed = new Testbed(getTestBedName());
        testBed.addMachine(initMachine(tasResolver));        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());

        return testBed;     
    }
   
    protected void initSystemProperties(ITasResolver tasResolver, 
                                        ITestbed testBed, 
                                        HashMap<String,String> props) {
     
        String host = tasResolver.getHostnameById(EPAGENT_ROLE_ID);
        initGenericSystemProperties(tasResolver, testBed, props);
        initEpagentSystemProperties(host, props, javaRole.getInstallDir());     
        setTestngCustomJvmArgs(props, testBed);   
    }
   
    @NotNull
    protected ITestbedMachine initMachine(ITasResolver tasResolver) {

        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1).templateId(defaultAgentTemplateId).build();
        javaRole = new CustomJavaRole.Builder("java8Role", tasResolver)
            .version(CustomJavaBinary.WINDOWS_64BIT_JDK_18_0_131)
            .shouldUpdateJavaSecurity(true)
            .build();
        
        addQCUploadRole(tasResolver, machine);
        addPerlRole(tasResolver, machine);
        addCygwinRole(tasResolver, machine); 
        addPerfmonRebuildRole(tasResolver, machine);     
        addEmRole(tasResolver, machine);
        addEPAgentRole(tasResolver, machine);  
        machine.addRole(javaRole);
        
        FetchAgentLogsRole fetchAgentLogs = new FetchAgentLogsRole(machine.getMachineId() + "_" + "fetchAgentLogs", codifyPath(DEPLOY_BASE + "/" + RESULTS_DIR));
        machine.addRole(fetchAgentLogs);
        
        machine.addRole(new ClientDeployRole.Builder("epagent_client01", tasResolver)
            .jvmVersion("8")
            .shouldDeployConsoleApps(false)
            .shouldDeployJassApps(isJassEnabled)
            .build());
        
        return machine;
    }   
}