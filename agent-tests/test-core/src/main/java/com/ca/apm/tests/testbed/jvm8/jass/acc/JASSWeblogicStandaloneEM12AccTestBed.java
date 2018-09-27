package com.ca.apm.tests.testbed.jvm8.jass.acc;

import java.util.HashMap;

import com.ca.apm.tests.testbed.jvm8.jass.JASSWeblogic12StandaloneEMTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSWeblogicStandaloneEM12AccTestBed extends JASSWeblogic12StandaloneEMTestBed {

    public JASSWeblogicStandaloneEM12AccTestBed () {
        isAccAgentBundle = true;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        
        initJassStandaloneEMSystemProperties(map, tasResolver);
        initAccSystemProperties(map);
        updateTestBedProps(map, testBed);
    }
}