package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.FakeWorkStationLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;

public class FLDFakeWorkStationLoadProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {

    private ITestbedMachine fakeWSMachine1;
    private ITestbedMachine fakeWSMachine2;
    private String version;

    public FLDFakeWorkStationLoadProvider(String emVersion) {
        version = emVersion;
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        fakeWSMachine1 = TestBedUtils.createWindowsMachine(FAKEWS01_MACHINE_ID, "w64");
        fakeWSMachine2 = TestBedUtils.createWindowsMachine(FAKEWS02_MACHINE_ID, "w64");
        return Arrays.asList(fakeWSMachine1, fakeWSMachine2);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);

        FakeWorkStationLoadRole fakeWSRole1 = new FakeWorkStationLoadRole.Builder(FAKEWS01_ROLE_ID, tasResolver).emHost(emHost).version(version).performLiveQueries().build();
        fakeWSMachine1.addRole(fakeWSRole1);

        FakeWorkStationLoadRole fakeWSRole2 = new FakeWorkStationLoadRole.Builder(FAKEWS02_ROLE_ID, tasResolver).emHost(emHost).version(version).performHistoricalQueries().build();
        fakeWSMachine2.addRole(fakeWSRole2);
    }

}
