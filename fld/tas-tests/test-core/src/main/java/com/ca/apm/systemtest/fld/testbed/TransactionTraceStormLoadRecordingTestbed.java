package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.systemtest.fld.testbed.loads.TTStormLoadProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed for generating and recording transaction trace storm load.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@TestBedDefinition
public class TransactionTraceStormLoadRecordingTestbed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("TransactionTraceStormLoadRecordingTestbed");
        
        FldTestbedProvider ttStormLoadRecordingTesbedProvider = new TTStormLoadRecordingTestbedProvider();
        testbed.addMachines(ttStormLoadRecordingTesbedProvider.initMachines());
        ttStormLoadRecordingTesbedProvider.initTestbed(testbed, tasResolver);
        
        FldTestbedProvider ttStormLoadProvider = new TTStormLoadProvider();
        testbed.addMachines(ttStormLoadProvider.initMachines());
        ttStormLoadProvider.initTestbed(testbed, tasResolver);
        
        return testbed;
    }

    
}
