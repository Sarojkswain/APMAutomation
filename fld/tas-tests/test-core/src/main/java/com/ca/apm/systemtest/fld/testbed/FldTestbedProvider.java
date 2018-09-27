/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;

import java.util.Collection;

/**
 * Allows for individual loads in the FLD to set up their required loads.  Implementors will
 * be able to instantiate new roles and machines in the testbed.  Access to the {@link Testbed}
 * ensures that they can make use of roles already created on other machines.
 * @author keyja01
 *
 */
public interface FldTestbedProvider {
    /**
     * Create the machines provided by this testbed provider.  These will be added to the 
     * testbed automatically.  This method is called on all testbed providers PRIOR
     * to invoking initTestbed - this is to ensure that machines used by multiple providers
     * are created only once, and available when necessary.
     * @return
     */
    public Collection<ITestbedMachine> initMachines();
    
    /**
     * Initialize all roles provided by this testbed provider.  This method is always called after
     * initMachines() has been invoked on all providers in the testbed.
     * @param testbed
     * @param tasResolver
     */
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver);
}
