package com.ca.apm.tests.testbed.jvm8.jass;

import java.util.HashMap;

import com.ca.apm.tests.testbed.JassTestBedUtil;
import com.ca.apm.tests.testbed.jvm8.DefaultTestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 8
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSDefaultStandaloneEMTestBed extends DefaultTestBed {
    
    protected ITasResolver tasResolver = null;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        this.tasResolver = tasResolver;    
        emailRecipients = EMAIL_RECIPIENTS_JASS;
        defaultAgentTemplateId = TEMPLATE_W64_JASS;
        isJassEnabled = true;
                
        //init machines
        ITestbed testBed = new Testbed(getTestBedName());
        ITestbedMachine machine1 = initMachine(tasResolver);
        ITestbedMachine machine2 = new TestbedMachine.Builder(MACHINE_2)
                .templateId(defaultAgentTemplateId)
                .build();
        addCygwinRole(tasResolver, machine2); 

        //add em role
        JassTestBedUtil.addCollectorRole(tasResolver, machine2, false);        
        testBed.addMachine(machine1, machine2);
        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());
        
        return testBed;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {

        initJassStandaloneEMSystemProperties(map, tasResolver);
        super.setTestngCustomJvmArgs(map, testBed);
    }
        
    @Override   
    protected void addEmRole(ITasResolver tasResolver, TestbedMachine machine) {
       
    }
}
