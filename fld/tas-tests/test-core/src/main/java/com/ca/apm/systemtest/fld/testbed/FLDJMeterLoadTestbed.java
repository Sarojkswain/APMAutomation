/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.systemtest.fld.testbed.loads.FldJMeterLoadProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author keyja01
 *
 */
@TestBedDefinition
public class FLDJMeterLoadTestbed implements ITestbedFactory {

    /* (non-Javadoc)
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("jmeterLoadTestbed");

        // This part of code is intentionally disabled.
        if (false) {
            FldTestbedProvider fldJMeterLoadProvider = new FldJMeterLoadProvider();
            fldJMeterLoadProvider.initMachines();
            fldJMeterLoadProvider.initTestbed(testbed, tasResolver);
        }
        
        return testbed;
    }

}
