package com.ca.apm.systemtest.fld.test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlow;
import com.ca.apm.systemtest.fld.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.systemtest.fld.role.MemoryMonitorRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.google.common.collect.Maps;

@Test
public class FLDMemoryMonitorLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "memory-monitor";
    }

    @Override
    protected void startLoad() {
        Map<String, String> map = new LinkedHashMap<>(30);
        for (String machineId : MEMORY_MONITOR_MAIN_CLUSTER_MACHINE_IDS) {
            map.put("memoryMonitorRole_" + machineId, machineId);
        }
        for (String machineId : MEMORY_MONITOR_SECOND_CLUSTER_MACHINE_IDS) {
            map.put("memoryMonitorRole_" + machineId, machineId);
        }
        for (String machineId : MEMORY_MONITOR_AGC_MACHINE_IDS) {
            map.put("memoryMonitorRole_" + machineId, machineId);
        }

        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        for (Entry<String, String> entry: map.entrySet()) {
            try {
                Map<String, String> roleProps = 
                    Maps.fromProperties(envProperties.getRolePropertiesById(entry.getKey()));
                IFlowContext startFlowContext = deserializeFromProperties(entry.getKey(),
                    MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
                    RunMemoryMonitorFlowContext.class);
                runFlowByMachineIdAsync(entry.getValue(), flowClass, startFlowContext, TimeUnit.DAYS, 28);

            } catch (Exception e) {
                logger.warn("Unable to start memory monitor on {}, {}", entry.getKey(),
                    e.getMessage());
            }
        }
    }

    @Override
    protected void stopLoad() {
        Map<String, String> map = new HashMap<>(1);
        for (String machineId : MEMORY_MONITOR_MAIN_CLUSTER_MACHINE_IDS) {
            map.put("memoryMonitorRole_" + machineId, machineId);
        }
        for (String machineId : MEMORY_MONITOR_SECOND_CLUSTER_MACHINE_IDS) {
            map.put("memoryMonitorRole_" + machineId, machineId);
        }
        for (String machineId : MEMORY_MONITOR_AGC_MACHINE_IDS) {
            map.put("memoryMonitorRole_" + machineId, machineId);
        }

        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        for (Entry<String, String> entry: map.entrySet()) {
            try {
                Map<String, String> roleProps = 
                    Maps.fromProperties(envProperties.getRolePropertiesById(entry.getKey()));
                IFlowContext stopFlowContext = deserializeFromProperties(entry.getKey(),
                    MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
                    RunMemoryMonitorFlowContext.class);
                runFlowByMachineId(entry.getValue(), flowClass, stopFlowContext);
            } catch (Exception e) {
                logger.warn("Unable to start memory monitor on {}, {}", entry.getKey(),
                    e.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    private IFlowContext deserializeStartFlowContext(String roleId, String envPropKey) {
        return deserializeFlowContextFromRole(roleId, envPropKey, RunMemoryMonitorFlowContext.class);
    }
}
