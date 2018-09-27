package com.ca.apm.systemtest.fld.testbed.legacy;

import com.ca.apm.systemtest.fld.testbed.STMQLoanTestbed;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Legacy mode system test for MQ PowerPack.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@TestBedDefinition
public class STMQLoanLegacyTestbed extends STMQLoanTestbed {

	@Override
    public ITestbed create(ITasResolver tasResolver){
        isLegacyMode = true;
        return super.create(tasResolver);
    }

}
