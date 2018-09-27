package com.ca.apm.systemtest.fld.test;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.tas.role.HammondRole;
import com.google.common.collect.Maps;

public class FldHammondLoadTest extends BaseFldLoadTest implements FLDLoadConstants {

    @Override
    protected String getLoadName() {
        return "hammondload";
    }

    @Override
    protected void startLoad() {
        //wait 10 minutes with start
        shortWait(600000L);
        
        for (final String id : getSerializedIds(HAMMOND_LOAD_ROLE_ID, HammondRole.ENV_HAMMOND_START)) {
            runSerializedCommandFlowFromRoleAsync(HAMMOND_LOAD_ROLE_ID, id, TimeUnit.DAYS, 28);
        }
    }

    @Override
    protected void stopLoad() {
        for (String id : getSerializedIds(HAMMOND_LOAD_ROLE_ID, HammondRole.ENV_HAMMOND_STOP)) {
            runSerializedCommandFlowFromRole(HAMMOND_LOAD_ROLE_ID, id);
        }
    }

    private Iterable<String> getSerializedIds(String roleId, String prefix) {
        Map<String, String> roleProperties =
            Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        HashSet<String> startIds = new HashSet<>();
        for (String key : roleProperties.keySet()) {
            if (key.startsWith(prefix)) {
                startIds.add(key.split("::")[0]);
            }
        }
        return startIds;
    }
    
    private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
        }
    }
}
