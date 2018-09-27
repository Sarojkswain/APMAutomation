package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH7;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed for Infrastructure agent (IA)
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IARedHat7Testbed extends IALinuxBaseTestbed {
  
    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        linuxTemplate = TEMPLATE_RH7;
        updateEmMM = true; //enforce alerts
        super.initMachines(tasResolver);
    }  
}