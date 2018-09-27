package com.ca.apm.tests.testbed.jvm8.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.Tomcat8TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class Tomcat8AccTestBed extends Tomcat8TestBed {

    public Tomcat8AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
