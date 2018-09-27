package com.ca.apm.systemtest.alertstateload.testbed;

import com.ca.apm.systemtest.alertstateload.testbed.regional.ConfigurationService;
import com.ca.apm.systemtest.fld.testbed.MemoryMonitorTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.TimeSynchronizationTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class AlertStateLoadTestbed implements ITestbedFactory, Constants {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("AlertStateLoadTestbed");

        boolean hammondMachinesOnLinux =
            !ConfigurationService.getConfig().isTestbedLoadMachinesOnWindows();

        AlertStateLoadProvider alertStateLoadProvider = new AlertStateLoadProvider();

        MemoryMonitorTestbedProvider memoryMonitorTestbedProvider =
            new MemoryMonitorTestbedProvider(alertStateLoadProvider.getMemoryMonitorMachineIds());
        memoryMonitorTestbedProvider
            .setMemoryMonitorWebappMachineId(ASL_MEMORY_MONITOR_WEBAPP_MACHINE_ID);
        memoryMonitorTestbedProvider.setLinuxMachine(false);
        memoryMonitorTestbedProvider.setGcLogFile(AlertStateLoadProvider.GC_LOG_FILE);

        TimeSynchronizationTestbedProvider timeSynchronizationTestbedProvider =
            hammondMachinesOnLinux ? new TimeSynchronizationTestbedProvider(
                alertStateLoadProvider.getTimeSynchronizationMachineIds()) : null;

        // initMachines
        alertStateLoadProvider.initMachines();
        memoryMonitorTestbedProvider.initMachines();
        if (hammondMachinesOnLinux && timeSynchronizationTestbedProvider != null) {
            timeSynchronizationTestbedProvider.initMachines();
        }

        // initTestbed
        alertStateLoadProvider.initTestbed(testbed, tasResolver);
        memoryMonitorTestbedProvider.initTestbed(testbed, tasResolver);
        if (hammondMachinesOnLinux && timeSynchronizationTestbedProvider != null) {
            timeSynchronizationTestbedProvider.initTestbed(testbed, tasResolver);
        }

        return testbed;
    }

}
