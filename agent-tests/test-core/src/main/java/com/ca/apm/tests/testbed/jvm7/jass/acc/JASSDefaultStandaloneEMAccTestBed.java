package com.ca.apm.tests.testbed.jvm7.jass.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm7.jass.JASSDefaultStandaloneEMTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSDefaultStandaloneEMAccTestBed extends JASSDefaultStandaloneEMTestBed {

    public JASSDefaultStandaloneEMAccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassStandaloneEMSystemProperties(map, tasResolver);
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
    
    @Override
    protected void addDefaultAgentRole(ITasResolver tasResolver, TestbedMachine machine) {
        
        addDefaultAccAgentRole(tasResolver, machine);
    }
}
