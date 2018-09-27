package com.ca.apm.tests.testbed.jvm8.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.Weblogic12TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class Weblogic12AccTestBed extends Weblogic12TestBed {

    public Weblogic12AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
