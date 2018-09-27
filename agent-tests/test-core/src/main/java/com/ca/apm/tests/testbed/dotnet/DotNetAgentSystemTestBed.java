package com.ca.apm.tests.testbed.dotnet;

import java.util.HashMap;

import com.ca.apm.tests.testbed.AgentRegressionBaseTestBed;
import com.ca.apm.tests.testbed.JassTestBedUtil;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * .NET System Test Bed
 *
 * @author kurma05
 */
@TestBedDefinition
public class DotNetAgentSystemTestBed extends DotNetAgentTestBed {
    
    protected ITasResolver tasResolver = null;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        this.tasResolver = tasResolver;    
        emailRecipients = EMAIL_RECIPIENTS_JASS;
        isJassEnabled = true;
                
        //init machines
        ITestbed testBed = new Testbed(getTestBedName());
        ITestbedMachine machine1 = initMachine(tasResolver);
        ITestbedMachine machine2 = new TestbedMachine.Builder(MACHINE_2)
                .templateId(TEMPLATE_W64_JASS)
                .build();
        addCygwinRole(tasResolver, machine2); 
        
        //add em roles (collector has to be added first)
        JassTestBedUtil.addCollectorRole(tasResolver, machine2, true, getEMArtifactVersion(tasResolver));        
        JassTestBedUtil.addMomRole(tasResolver, machine1, getEMArtifactVersion(tasResolver));
        testBed.addMachine(machine1, machine2);
        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());
        
        return testBed;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {

        initJassSystemProperties(map, tasResolver);
        map.put("data.file", "testng_dotnet_system.csv");
        map.put("qcuploadtool.testset.name", "APM - " + tasResolver.getDefaultVersion() + " - DotNetSystem");

        super.setTestngCustomJvmArgs(map, testBed);
    }
        
    @Override   
    protected void addEmRole(ITasResolver tasResolver, TestbedMachine machine) {
       
    }
}
