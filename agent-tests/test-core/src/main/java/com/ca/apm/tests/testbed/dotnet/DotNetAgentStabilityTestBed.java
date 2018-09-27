package com.ca.apm.tests.testbed.dotnet;

import java.util.HashMap;

import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * .Net Stability Test Bed
 *
 * @author kurma05
 */
@TestBedDefinition
public class DotNetAgentStabilityTestBed extends DotNetAgentSystemTestBed {
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {

        initJassSystemProperties(map, tasResolver);
        map.put("data.file", "testng_dotnet_system.csv");
        map.put("jass.test.duration", "864000000"); //10 days
        map.put("qcuploadtool.testset.name", "APM - " + getAgentArtifactVersion(tasResolver) + " - DotNetSystem");

        updateTestBedProps(map, testBed);
    }
}
