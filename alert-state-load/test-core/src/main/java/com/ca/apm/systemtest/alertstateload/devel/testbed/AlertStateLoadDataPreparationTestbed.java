package com.ca.apm.systemtest.alertstateload.devel.testbed;

import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class AlertStateLoadDataPreparationTestbed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("AlertStateLoadDataPreparationTestbed");
        FldTestbedProvider fldTestbedProvider = new AlertStateLoadDataPreparationProvider();
        fldTestbedProvider.initMachines();
        fldTestbedProvider.initTestbed(testbed, tasResolver);
        return testbed;
    }

}
