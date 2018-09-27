package com.ca.apm.systemtest.sizingguidetest.testbed;

import com.ca.apm.systemtest.fld.testbed.MemoryMonitorTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldControllerLoadProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.apm.systemtest.sizingguidetest.testbed.regional.Configuration;
import com.ca.apm.systemtest.sizingguidetest.testbed.regional.ConfigurationService;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SizingGuideTestbed implements ITestbedFactory, Constants {

    private static Configuration configuration = ConfigurationService.getConfig();
    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("SizingGuideTestbed");
        SizingGuideTestbedProvider sizingGuideTestbedProvider =
            new SizingGuideTestbedProvider(configuration);
        sizingGuideTestbedProvider.setUsingOld97version(isUsingOld97version());

        MemoryMonitorTestbedProvider memoryMonitorTestbedProvider =
            new MemoryMonitorTestbedProvider(
                sizingGuideTestbedProvider.getMemoryMonitorMachineIds());
        memoryMonitorTestbedProvider
            .setMemoryMonitorWebappMachineId(MEMORY_MONITOR_WEBAPP_MACHINE_ID);
        memoryMonitorTestbedProvider.setLinuxMachine(false);
        memoryMonitorTestbedProvider.setGcLogFile(SizingGuideTestbedProvider.GC_LOG_FILE);

        FldControllerLoadProvider fldControllerLoadProvider =
            new FldControllerLoadProvider(fldConfig);

        // initMachines
        testbed.addMachines(sizingGuideTestbedProvider.initMachines());
        memoryMonitorTestbedProvider.initMachines();
        testbed.addMachines(fldControllerLoadProvider.initMachines());

        // initTestbed
        sizingGuideTestbedProvider.initTestbed(testbed, tasResolver);
        memoryMonitorTestbedProvider.initTestbed(testbed, tasResolver);
        fldControllerLoadProvider.initTestbed(testbed, tasResolver);
        return testbed;
    }

    private static boolean isUsingOld97version() {
        String s = configuration.getTestbedEmVersion();
        return s != null && s.startsWith("9.7")
        // && !s.startsWith("10.") && !s.startsWith("99.")
        ;
    }

}
