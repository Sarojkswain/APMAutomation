package com.ca.apm.systemtest.fld.test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.RunNetworkTrafficMonitorFlow;
import com.ca.apm.systemtest.fld.flow.RunNetworkTrafficMonitorFlowContext;
import com.ca.apm.systemtest.fld.role.NetworkTrafficMonitorRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.google.common.collect.Maps;

@Test
public class FLDNetworkTrafficMonitorLoadTest extends BaseFldLoadTest
    implements
        FLDConstants,
        FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "network-traffic-monitor";
    }

    @Override
    protected void startLoad() {
        Map<String, String> map = new LinkedHashMap<>(30);
        for (String machineId : NETWORK_TRAFFIC_MONITOR_MAIN_CLUSTER_MACHINE_IDS) {
            map.put("networkTrafficMonitorRole_" + machineId, machineId);
        }
        for (String machineId : NETWORK_TRAFFIC_MONITOR_SECOND_CLUSTER_MACHINE_IDS) {
            map.put("networkTrafficMonitorRole_" + machineId, machineId);
        }
        for (String machineId : NETWORK_TRAFFIC_MONITOR_AGC_MACHINE_IDS) {
            map.put("networkTrafficMonitorRole_" + machineId, machineId);
        }

        for (Entry<String, String> entry : map.entrySet()) {
            try {
                Map<String, String> roleProps =
                    Maps.fromProperties(envProperties.getRolePropertiesById(entry.getKey()));
                IFlowContext startFlowContext =
                    deserializeFromProperties(entry.getKey(),
                        NetworkTrafficMonitorRole.ENV_NETWORK_TRAFFIC_MONITOR_START, roleProps,
                        RunNetworkTrafficMonitorFlowContext.class);
                runFlowByMachineIdAsync(entry.getValue(), RunNetworkTrafficMonitorFlow.class,
                    startFlowContext, TimeUnit.DAYS, 28);
            } catch (Exception e) {
                logger.warn("Unable to start network-traffic monitor on {}, {}", entry.getKey(),
                    e.getMessage());
            }
        }
    }

    @Override
    protected void stopLoad() {
        Map<String, String> map = new HashMap<>(1);
        for (String machineId : NETWORK_TRAFFIC_MONITOR_MAIN_CLUSTER_MACHINE_IDS) {
            map.put("networkTrafficMonitorRole_" + machineId, machineId);
        }
        for (String machineId : NETWORK_TRAFFIC_MONITOR_SECOND_CLUSTER_MACHINE_IDS) {
            map.put("networkTrafficMonitorRole_" + machineId, machineId);
        }
        for (String machineId : NETWORK_TRAFFIC_MONITOR_AGC_MACHINE_IDS) {
            map.put("networkTrafficMonitorRole_" + machineId, machineId);
        }

        for (Entry<String, String> entry : map.entrySet()) {
            try {
                Map<String, String> roleProps =
                    Maps.fromProperties(envProperties.getRolePropertiesById(entry.getKey()));
                IFlowContext stopFlowContext =
                    deserializeFromProperties(entry.getKey(),
                        NetworkTrafficMonitorRole.ENV_NETWORK_TRAFFIC_MONITOR_STOP, roleProps,
                        RunNetworkTrafficMonitorFlowContext.class);
                runFlowByMachineId(entry.getValue(), RunNetworkTrafficMonitorFlow.class,
                    stopFlowContext);
            } catch (Exception e) {
                logger.warn("Unable to start network-traffic monitor on {}, {}", entry.getKey(),
                    e.getMessage());
            }
        }
    }

}
