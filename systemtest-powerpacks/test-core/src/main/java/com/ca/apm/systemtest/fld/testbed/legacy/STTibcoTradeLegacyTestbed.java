package com.ca.apm.systemtest.fld.testbed.legacy;

import com.ca.apm.systemtest.fld.testbed.STTibcoTradeTestbed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @Author rsssa02
 */
@TestBedDefinition
public class STTibcoTradeLegacyTestbed extends STTibcoTradeTestbed{

    @Override
    public ITestbed create(ITasResolver tasResolver){
        isLegacyMode = true;
        return super.create(tasResolver);
    }
}
