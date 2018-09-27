package com.ca.apm.tests.testbed.jvm7.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm7.Tomcat7TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class Tomcat7AccTestBed extends Tomcat7TestBed {

    public Tomcat7AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
