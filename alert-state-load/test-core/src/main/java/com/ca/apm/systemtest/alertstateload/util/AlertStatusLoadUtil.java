package com.ca.apm.systemtest.alertstateload.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ca.apm.systemtest.alertstateload.testbed.Constants;
import com.ca.tas.testbed.ITestbedMachine;

public class AlertStatusLoadUtil implements Constants {

    private static final String COLL_ROLE = "collRole";
    private static final String LOAD_ROLE = ASL_LOAD_ROLE;

    private AlertStatusLoadUtil() {}

    public static List<String> getLoadRoleIds() {
        List<String> loadRoleIds = new ArrayList<>();
        int loadMachinesLength = ASL_LOAD_MACHINES.length;
        int collectorMachinesLength = ASL_COLL_MACHINES.length;
        int loadMachinesIndex = 0;
        int collectorMachinesIndex = 0;
        if (loadMachinesLength <= collectorMachinesLength) {
            for (collectorMachinesIndex = 0; collectorMachinesIndex < collectorMachinesLength; collectorMachinesIndex++) {
                if (loadMachinesIndex == loadMachinesLength) {
                    loadMachinesIndex = 0;
                }
                String loadMachineId = ASL_LOAD_MACHINES[loadMachinesIndex++];
                String collectorMachineId = ASL_COLL_MACHINES[collectorMachinesIndex];
                String loadRoleId = getLoadRoleId(loadMachineId, collectorMachineId);
                loadRoleIds.add(loadRoleId);
            }
        } else {
            for (loadMachinesIndex = 0; loadMachinesIndex < loadMachinesLength; loadMachinesIndex++) {
                if (collectorMachinesIndex == collectorMachinesLength) {
                    collectorMachinesIndex = 0;
                }
                String loadMachineId = ASL_LOAD_MACHINES[loadMachinesIndex];
                String collectorMachineId = ASL_COLL_MACHINES[collectorMachinesIndex++];
                String loadRoleId = getLoadRoleId(loadMachineId, collectorMachineId);
                loadRoleIds.add(loadRoleId);
            }
        }
        Collections.sort(loadRoleIds);
        return loadRoleIds;
    }

    public static String getLoadRoleId(String loadMachineId, String collectorMachineId) {
        return LOAD_ROLE + "_" + loadMachineId + "_" + collectorMachineId;
    }

    public static String getCollRoleId(ITestbedMachine collectorMachine) {
        return getCollRoleId(collectorMachine.getMachineId());
    }

    public static String getCollRoleId(String collectorMachineId) {
        return COLL_ROLE + "_" + collectorMachineId;
    }

    public static String getLoadMachineId(String loadRoleId) {
        loadRoleId =
            loadRoleId.substring(loadRoleId.indexOf(LOAD_ROLE) + LOAD_ROLE.length() + 1,
                loadRoleId.length());
        loadRoleId = loadRoleId.substring(0, loadRoleId.indexOf("_"));
        return loadRoleId;
    }

    public static String getMemoryMonitorRoleId() {
        return "memoryMonitorRole_" + ASL_WV_MACHINE_ID;
    }

    public static void main(String[] args) {
        System.out.println("AlertStatusLoadUtil.main():: getLoadRoleIds() = " + getLoadRoleIds()
            + '\n');

        for (String s : getLoadRoleIds()) {
            System.out.println("AlertStatusLoadUtil.main():: getLoadMachineId('" + s + "') = '"
                + getLoadMachineId(s) + "'");
        }
    }

}
