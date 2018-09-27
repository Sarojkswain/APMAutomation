package com.ca.apm.tests.testbed.jvm8.jass.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.jass.JASSTomcat8TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSTomcat8AccTestBed extends JASSTomcat8TestBed {

    public JASSTomcat8AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassSystemProperties(map, tasResolver);
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
