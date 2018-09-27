package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.artifact.thirdparty.HammondDataVersion;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.HammondRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * 
 * This is the Hammond Load Role provider for use in a FLD environment. This code can
 * initiate a hammond role and make necessary customizations and add that to a new machine
 * which then is added to the testbed that invokes this code. The necessary details are provided
 * below as inline comments.
 * 
 * @author jirji01@ca.com
 */

public class FldHammondProvider implements FldTestbedProvider, FLDLoadConstants {

    private int RUN_DURATION_SECONDS = 14 * 24 * 60 * 60 * 1000;
    private String emRoleId = null;
    
    private ITestbedMachine loadMachine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        loadMachine = new TestbedMachine.Builder(HAMMOND_MACHINE_ID).templateId("w64")
            .bitness(Bitness.b64).build();
        return Arrays.asList(loadMachine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver resolver) {
        HammondRole hammondRole =
            new HammondRole.Builder(HAMMOND_LOAD_ROLE_ID, resolver).heapMemory("1024m").scale(1)
                .collector(resolver.getHostnameById(emRoleId))
                .data(HammondDataVersion.FLD_mainframe, HammondDataVersion.FLD_tomcat)
                .runDuration(RUN_DURATION_SECONDS).build();
        loadMachine.addRole(hammondRole);
    }

    public FldHammondProvider updateCollRoleId(String emRoleId) {
        this.emRoleId = emRoleId;
        return this;
    }
}
