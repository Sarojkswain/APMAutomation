package com.ca.apm.systemtest.fld.testbed.devel;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.systemtest.fld.role.ConfigureTimRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.TIMRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author haiva01
 */
@TestBedDefinition
public class TimEthTestbed implements ITestbedFactory {

    public static final String TIM_ROLE_ID = "timRoleId";
    public static final String TIM_MACHINE_ID = "timMachineId";
    public static final String CO_65_TIM = "co65_tim";
    public static final String SELENIUM_MACHINE_ID = "seleniumMachineId";
    public static final String SELENIUM_ROLE_ID = "seleniumRoleId";
    public static final String TIM_CONFIGURATION_CONTEXT_KEY = "timConfigurationContext";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // TIM role.

        TIMRole timRole
            = new TIMRole.Builder(TIM_ROLE_ID, tasResolver)
            .installDir("/opt/tim")
//            .timVersion("10.3.0.13")
            .build();

        ITestbedMachine timMachine = TestBedUtils
            .createLinuxMachine(TIM_MACHINE_ID, CO_65_TIM, timRole);

        String timHostname = tasResolver.getHostnameById(TIM_ROLE_ID);

        // Windows host doing the configuration through Selenium.

        ConfigureTimRole seleniumRole = new ConfigureTimRole.Builder(SELENIUM_ROLE_ID)
            .timHostname(timHostname)
            .requiredInterface("eth2")
            .disallowedInterface("eth0")
            .additionalProperty("MaxFlexRequestBodySize", "100000")
            .additionalProperty("MaxFlexResponseBodySize", "100000")
            .build();
        
        seleniumRole.after(timRole);

        ITestbedMachine seleniumMachine = TestBedUtils.createWindowsMachine(SELENIUM_MACHINE_ID,
            TEMPLATE_W64, seleniumRole);


        return new Testbed(getClass().getSimpleName())
            .addMachine(timMachine)
            .addMachine(seleniumMachine);
    }
}
