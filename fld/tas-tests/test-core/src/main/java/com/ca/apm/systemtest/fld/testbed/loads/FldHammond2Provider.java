package com.ca.apm.systemtest.fld.testbed.loads;

import com.ca.apm.systemtest.fld.role.Hammond2Role;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.systemtest.fld.testbed.loads.FldHammond2Provider.LoadSize.BIG;

/**
 * This is the Hammond Load Role provider for use in a FLD environment. This code can
 * initiate a hammond role and make necessary customizations and add that to a new machine
 * which then is added to the testbed that invokes this code. The necessary details are provided
 * below as inline comments.
 *
 * @author jirji01@ca.com
 */

public class FldHammond2Provider implements FldTestbedProvider, FLDLoadConstants {

    public enum LoadSize {
        SMALL, MEDIUM, BIG
    }

    private String emRoleId = null;
    private LoadSize loadSize = BIG;

    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    private ITestbedMachine loadMachine1;
    private ITestbedMachine loadMachine2;

    @Override
    public Collection<ITestbedMachine> initMachines() {
        ArrayList<ITestbedMachine> result = new ArrayList<>();

        switch (loadSize) {
            case BIG:
                loadMachine2 = new TestbedMachine.Builder(HAMMOND_MACHINE_2_ID).templateId("w64")
                        .bitness(Bitness.b64).build();
                result.add(loadMachine2);
            case SMALL:
            case MEDIUM:
                loadMachine1 = new TestbedMachine.Builder(HAMMOND_MACHINE_1_ID).templateId("w64")
                        .bitness(Bitness.b64).build();
                result.add(loadMachine1);
        }

        return result;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver resolver) {

        int runDuration = fldConfig.getRunDuration(TimeUnit.SECONDS);

        switch (loadSize) {
            case SMALL:
                loadMachine1.addRole(new Hammond2Role.Builder(HAMMOND_LOAD_ROLE_1_ID, resolver).heapMemory("12g")
                        .collector(resolver.getHostnameById(emRoleId))
                        .data("\\\\truss-emea-cz\\synced\\test_data\\telefonica\\data")
                        .group(1, 8)
                        .scale(1)
                        .runDuration(runDuration).build());
                break;

            case MEDIUM:
                loadMachine1.addRole(new Hammond2Role.Builder(HAMMOND_LOAD_ROLE_1_ID, resolver).heapMemory("12g")
                        .collector(resolver.getHostnameById(emRoleId))
                        .data("\\\\truss-emea-cz\\synced\\test_data\\telefonica\\data")
                        .group(1, 2)
                        .scale(0.8)
                        .runDuration(runDuration).build());
                break;

            case BIG:
                loadMachine1.addRole(new Hammond2Role.Builder(HAMMOND_LOAD_ROLE_1_ID, resolver).heapMemory("12g")
                        .collector(resolver.getHostnameById(emRoleId))
                        .data("\\\\truss-emea-cz\\synced\\test_data\\telefonica\\data")
                        .group(1, 2)
                        .scale(1)
                        .runDuration(runDuration).build());
                loadMachine2.addRole(new Hammond2Role.Builder(HAMMOND_LOAD_ROLE_2_ID, resolver).heapMemory("12g")
                        .collector(resolver.getHostnameById(emRoleId))
                        .data("\\\\truss-emea-cz\\synced\\test_data\\telefonica\\data")
                        .group(2, 2)
                        .scale(1)
                        .runDuration(runDuration).build());
                break;
        }
    }

    public FldHammond2Provider updateCollRoleId(String emRoleId) {
        this.emRoleId = emRoleId;
        return this;
    }

    public FldHammond2Provider loadSize(LoadSize size) {
        this.loadSize = size;
        return this;
    }
}
