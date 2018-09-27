package com.ca.apm.tests.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Windows 2016 Testbed for Java & IA (Selenium runs on Win 2k8)
 * 
 * @author kurma05
 */
@TestBedDefinition
public class IATomcatWin2016Testbed extends IAWinBaseTestbed {

    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        super.initMachines(tasResolver, "w16");
    }
}