/**
 * 
 */
package com.ca.apm.systemtest.fld.sample;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FiftyProvidersTestbed;
import com.ca.tas.role.HammondRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

/**
 * 
 * @author filja01
 *
 */
public class FiftyProviderLoadTest extends TasTestNgTest implements FLDConstants {
    @Tas(owner="filja01", size=SizeType.BIG, testBeds = {@TestBed(name=FiftyProvidersTestbed.class, executeOn=AGC_MACHINE_ID)})
    @Test(groups="fld-load-test")
    public void testRun() throws Exception {
        //runSerializedCommandFlowFromRoleAsync("wls01MachineId-SOALoadRole_JM", JMeterRole.ENV_JMETER_START, TimeUnit.DAYS, 28);
        //shortWait(600000L);
        
        for (int i = 0; i < 50; i++) {
            for (final String id : getSerializedIds("hammond"+i, HammondRole.ENV_HAMMOND_START)) {
                runSerializedCommandFlowFromRoleAsync("hammond"+i, id, TimeUnit.DAYS, 28);
            }
        }
    }
    
    private synchronized void shortWait(long ms) throws InterruptedException {
        wait(ms);
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

}
