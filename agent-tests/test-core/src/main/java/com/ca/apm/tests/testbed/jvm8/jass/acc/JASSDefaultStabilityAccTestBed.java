package com.ca.apm.tests.testbed.jvm8.jass.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.jass.JASSDefaultTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSDefaultStabilityAccTestBed extends JASSDefaultTestBed {

    public JASSDefaultStabilityAccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassSystemProperties(map, tasResolver);
        initAccSystemProperties(map);
        map.put("jass.test.duration", "864000000"); //10 days
        updateTestBedProps(map, testBed);
    }
    
    @Override
    protected void addDefaultAgentRole(ITasResolver tasResolver, TestbedMachine machine) {

        addDefaultAccAgentRole(tasResolver, machine);
    }
}
