package com.ca.apm.tests.testbed.jvm7.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm7.Jboss7TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class Jboss7AccTestBed extends Jboss7TestBed {

    public Jboss7AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
