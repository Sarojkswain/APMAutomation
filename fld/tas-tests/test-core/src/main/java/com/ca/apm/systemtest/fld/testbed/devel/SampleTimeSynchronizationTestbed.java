package com.ca.apm.systemtest.fld.testbed.devel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SampleTimeSynchronizationTestbed implements ITestbedFactory {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(SampleTimeSynchronizationTestbed.class);

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        LOGGER.info("SampleTimeSynchronizationTestbed.create()::");
        Testbed testbed = new Testbed(getClass().getSimpleName());

        // (new SampleTimeSynchronizationFldTestbedProvider()).initTestbed(testbed, tasResolver);
        (new SampleTimeSynchronizationFldTestbedProvider4Linux()).initTestbed(testbed, tasResolver);

        return testbed;
    }

}
