package com.ca.apm.tests.testbed.jvm8.jass.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.jass.JASSWeblogic12TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSWeblogic12AccTestBed extends JASSWeblogic12TestBed {

    public JASSWeblogic12AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassSystemProperties(map, tasResolver);
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
