package com.ca.apm.tests.testbed.jvm7.jass;

import java.util.HashMap;

import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSDefaultStabilityTestBed extends JASSDefaultTestBed {
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassSystemProperties(map, tasResolver);
        map.put("jass.test.duration", "864000000"); //10 days
        updateTestBedProps(map, testBed);
    }
}
