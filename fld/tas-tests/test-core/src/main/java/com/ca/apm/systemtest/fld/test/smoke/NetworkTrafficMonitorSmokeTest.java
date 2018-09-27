package com.ca.apm.systemtest.fld.test.smoke;

import static com.ca.apm.systemtest.fld.testbed.smoke.NetworkTrafficMonitorSmokeTestbed.MOM_MACHINE_ID;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.RunNetworkTrafficMonitorFlow;
import com.ca.apm.systemtest.fld.flow.RunNetworkTrafficMonitorFlowContext;
import com.ca.apm.systemtest.fld.role.NetworkTrafficMonitorRole;
import com.ca.apm.systemtest.fld.testbed.smoke.NetworkTrafficMonitorSmokeTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

/**
 * @author bocto01
 */
@Tas(testBeds = {@TestBed(name = NetworkTrafficMonitorSmokeTestbed.class, executeOn = MOM_MACHINE_ID)}, size = SizeType.MEDIUM)
@Test
public class NetworkTrafficMonitorSmokeTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(NetworkTrafficMonitorSmokeTest.class);

    private static final String NTM_SMOKE_ROLE = "networkTrafficMonitorRole_" + MOM_MACHINE_ID;

    /**
     * Test if the network traffic graph is being generated correctly for our hosts
     */
    public void runNetworkTrafficMonitorTest() throws Exception {
        String machineId = envProperties.getMachineIdByRoleId(NTM_SMOKE_ROLE);
        Class<? extends IAutomationFlow> flowClass = RunNetworkTrafficMonitorFlow.class;
        Map<String, String> roleProps =
            Maps.fromProperties(envProperties.getRolePropertiesById(NTM_SMOKE_ROLE));
        LOGGER.info("NetworkTrafficMonitorSmokeTest.runNetworkTrafficMonitorTest():: roleProps = "
            + roleProps);

        LOGGER
            .info("NetworkTrafficMonitorSmokeTest.runNetworkTrafficMonitorTest():: starting NetworkTrafficMonitor");
        IFlowContext startFlowContext =
            deserializeFromProperties(NTM_SMOKE_ROLE,
                NetworkTrafficMonitorRole.ENV_NETWORK_TRAFFIC_MONITOR_START, roleProps,
                RunNetworkTrafficMonitorFlowContext.class);
        runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.DAYS, 28);
        LOGGER
            .info("NetworkTrafficMonitorSmokeTest.runNetworkTrafficMonitorTest():: started: NetworkTrafficMonitor");

        TimeUnit.SECONDS.sleep(45);

        LOGGER
            .info("NetworkTrafficMonitorSmokeTest.runNetworkTrafficMonitorTest():: stopping NetworkTrafficMonitor");
        IFlowContext stopFlowContext =
            deserializeFromProperties(NTM_SMOKE_ROLE,
                NetworkTrafficMonitorRole.ENV_NETWORK_TRAFFIC_MONITOR_STOP, roleProps,
                RunNetworkTrafficMonitorFlowContext.class);
        runFlowByMachineId(machineId, flowClass, stopFlowContext);
        LOGGER
            .info("NetworkTrafficMonitorSmokeTest.runNetworkTrafficMonitorTest():: stopped: NetworkTrafficMonitor");
    }

}
