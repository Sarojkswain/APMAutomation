package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed for Infrastructure agent (IA)
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IARedHat66Testbed extends IALinuxBaseTestbed {
    
    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        //add machines
        TestbedMachine machine1 =
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64).build();
        TestbedMachine machine2 =
            new TestbedMachine.Builder(MACHINE2_ID).templateId(TEMPLATE_RH66).build();
        
        //add client, em & agents
        addClientRoles(tasResolver, machine1);
        addEMLinuxRole(tasResolver, machine2);
        addTomcatAgentRole(tasResolver, machine2);
        addIARole(tasResolver, machine2);
        
        testbed.addMachine(machine1, machine2);
    }
}