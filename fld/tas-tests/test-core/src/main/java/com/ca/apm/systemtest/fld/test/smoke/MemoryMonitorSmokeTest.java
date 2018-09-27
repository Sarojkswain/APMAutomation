/**
 *
 */
package com.ca.apm.systemtest.fld.test.smoke;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlow;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.systemtest.fld.role.MemoryMonitorRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.smoke.MemoryMonitorSmokeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.systemtest.fld.testbed.smoke.MemoryMonitorSmokeTestbed.MMSMOKEROLE;

/**
 * @author keyja01
 */
@Tas(testBeds = {
    @TestBed(name = MemoryMonitorSmokeTestbed.class,
        executeOn = FLDConstants.MEMORY_MONITOR_WEBAPP_MACHINE_ID)}, size = SizeType.MEDIUM)
@Test
public class MemoryMonitorSmokeTest extends TasTestNgTest {

    /**
     * Test if the memory graph is being generated correctly for our hosts
     */
    public void runMemoryMonitorTest() throws Exception {
        String machineId = envProperties.getMachineIdByRoleId(MMSMOKEROLE);
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;

        Map<String, String> roleProps = Maps
            .fromProperties(envProperties.getRolePropertiesById(MMSMOKEROLE));

        IFlowContext startFlowContext = deserializeFromProperties(MMSMOKEROLE,
            MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
            RunMemoryMonitorFlowContext.class);
        runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.DAYS, 28);

        TimeUnit.SECONDS.sleep(45);

        IFlowContext stopFlowContext = deserializeFromProperties(MMSMOKEROLE,
            MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
            RunMemoryMonitorFlowContext.class);
        runFlowByMachineId(machineId, flowClass, stopFlowContext);
    }
}
