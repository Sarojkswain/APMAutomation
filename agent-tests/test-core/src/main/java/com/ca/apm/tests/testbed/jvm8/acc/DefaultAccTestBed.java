package com.ca.apm.tests.testbed.jvm8.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.DefaultTestBed;
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
public class DefaultAccTestBed extends DefaultTestBed {

    public DefaultAccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
    
    @Override
    protected void addDefaultAgentRole(ITasResolver tasResolver, TestbedMachine machine) {

        addDefaultAccAgentRole(tasResolver, machine);
    }
}
