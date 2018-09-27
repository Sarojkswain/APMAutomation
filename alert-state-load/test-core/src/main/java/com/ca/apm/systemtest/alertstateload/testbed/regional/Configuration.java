package com.ca.apm.systemtest.alertstateload.testbed.regional;

import com.ca.tas.resolver.ITasResolver;

public interface Configuration {

    String getTestbedEmVersion(ITasResolver tasResolver);

    boolean isTestbedLoadMachinesOnWindows();

    boolean isTestbedDbMachineOnWindows();

    boolean isTestRunInitPhase();

}
