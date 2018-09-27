package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W12;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Windows 2012 Testbed for Java & IA (Selenium runs on Win 2k8)
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IATomcatWin2012Testbed extends IAWinBaseTestbed {

    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        super.initMachines(tasResolver, TEMPLATE_W12);
    }
}