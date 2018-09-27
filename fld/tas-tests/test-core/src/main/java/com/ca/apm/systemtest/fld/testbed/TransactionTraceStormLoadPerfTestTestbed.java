package com.ca.apm.systemtest.fld.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@TestBedDefinition
public class TransactionTraceStormLoadPerfTestTestbed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("TransactionTraceStormLoadPerfTestTestbed");
        
        FldTestbedProvider ttStormLoadPerfTestTesbedProvider = new TTStormLoadPerfTestTestbedProvider();
        testbed.addMachines(ttStormLoadPerfTestTesbedProvider.initMachines());
        ttStormLoadPerfTestTesbedProvider.initTestbed(testbed, tasResolver);
        
        return testbed;
    }

}
