package com.ca.apm.tests.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Windows 2008 Testbed for Java & IA
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IATomcatWin2008Testbed extends IAWinBaseTestbed {
  
    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        super.initMachines(tasResolver);
    }  
}