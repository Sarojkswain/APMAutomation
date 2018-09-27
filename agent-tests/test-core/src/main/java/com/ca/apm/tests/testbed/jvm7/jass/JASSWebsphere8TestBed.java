package com.ca.apm.tests.testbed.jvm7.jass;

import java.util.HashMap;

import com.ca.apm.tests.testbed.JassTestBedUtil;
import com.ca.apm.tests.testbed.jvm7.Websphere8TestBed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Jass Automation - JVM 7
 *
 * @author kurma05
 */
@TestBedDefinition
public class JASSWebsphere8TestBed extends Websphere8TestBed {
    
    protected ITasResolver tasResolver = null;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        this.tasResolver = tasResolver;    
        emailRecipients = EMAIL_RECIPIENTS_JASS;
        defaultAgentTemplateId = TEMPLATE_W64_JASS;
        isJassEnabled = true;
                
        //init machines
        ITestbed testBed = new Testbed(getTestBedName());
        ITestbedMachine machine1 = initMachine1(tasResolver);
        ITestbedMachine machine2 = initMachine2(tasResolver);
        
        //add em roles (collector has to be added first)
        JassTestBedUtil.addCollectorRole(tasResolver, machine2, true);        
        JassTestBedUtil.addMomRole(tasResolver, machine1);
        testBed.addMachine(machine1, machine2);
        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());
        
        return testBed;
    }
    
    @Override
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {

        initJassSystemProperties(map, tasResolver);
        super.setTestngCustomJvmArgs(map, testBed);
    }
        
    @Override   
    protected void addEmRole(ITasResolver tasResolver, TestbedMachine machine) {
       
    }
}
