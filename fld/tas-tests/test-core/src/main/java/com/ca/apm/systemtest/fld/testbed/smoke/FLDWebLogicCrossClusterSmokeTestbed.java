package com.ca.apm.systemtest.fld.testbed.smoke;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDWebLogicCrossClusterProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class FLDWebLogicCrossClusterSmokeTestbed
    implements
        ITestbedFactory,
        FLDLoadConstants,
        FLDConstants {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("FLDWebLogicCrossClusterSmokeTestbed");

        FldTestbedProvider fldTestbedProvider = new FLDWebLogicCrossClusterProvider();
        testbed.addMachines(fldTestbedProvider.initMachines());
        fldTestbedProvider.initTestbed(testbed, tasResolver);

        return testbed;
    }

}
