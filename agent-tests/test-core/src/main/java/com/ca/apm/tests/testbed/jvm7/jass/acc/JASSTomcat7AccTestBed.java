package com.ca.apm.tests.testbed.jvm7.jass.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm7.jass.JASSTomcat7TestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSTomcat7AccTestBed extends JASSTomcat7TestBed {

    public JASSTomcat7AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassSystemProperties(map, tasResolver);
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}
