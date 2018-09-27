package com.ca.apm.tests.testbed.jvm7.acc;

import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Sample Test Bed for ACC bundles.
 * This test bed deploys only agent default bundle.
 *
 * @author kurma05
 */
@TestBedDefinition
public class SampleAccTestBed extends AgentRegressionBaseTestBed {

    private ITasResolver tasResolver;


    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        this.tasResolver = tasResolver;            
        ITestbed testBed = new Testbed(getTestBedName());
       
        TestbedMachine machine = new TestbedMachine.Builder(MACHINE_1)
                .templateId("w64")
                .build(); 
      
        addDefaultAccAgentRole(tasResolver, machine);
        
       // machine.addRole(role);        
        testBed.addMachine(machine);        
        
        return testBed;
    }    
}
