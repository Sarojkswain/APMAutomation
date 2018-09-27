package com.ca.apm.tests.testbed.jvm6.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm6.Weblogic103TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 6
 *
 * @author kurma05
 */
@TestBedDefinition
public class Weblogic103AccTestBed extends Weblogic103TestBed {

    public Weblogic103AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
