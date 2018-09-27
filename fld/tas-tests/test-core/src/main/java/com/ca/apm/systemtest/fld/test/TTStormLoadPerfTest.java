package com.ca.apm.systemtest.fld.test;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.TTStormLoadPerfTestTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.TransactionTraceStormLoadPerfTestTestbed;
import com.ca.tas.role.HammondRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

/**
 * Performance test for EM being under transaction trace storm load.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
@Test
public class TTStormLoadPerfTest extends TasTestNgTest implements FLDLoadConstants, FLDConstants {
    private static final Logger LOGGER = LoggerFactory.getLogger(TTStormLoadPerfTest.class);

    @Tas(testBeds = @TestBed(name = TransactionTraceStormLoadPerfTestTestbed.class, executeOn = TTStormLoadPerfTestTestbedProvider.EXEC_MACHINE_ID), 
        owner = "sinal04", 
        size = SizeType.DEBUG, 
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void testTTStormLoadPerformance() throws Exception {
        //wait for 5 mins
        Thread.sleep(5*60*1000);

        //Gradually start the load increasing the total number of agents by 40 each 10 minutes
        startLoad();

        //Let it run for 20 more minutes
        Thread.sleep(20*60*1000);
        
        //Stop the load
        stopLoad();
    }
    
    protected void startLoad() throws InterruptedException {
        for (int i = 1; i <= TTStormLoadPerfTestTestbedProvider.NUM_OF_HAMMONDS; i++) {
            String roleId = String.format(TTStormLoadPerfTestTestbedProvider.HAMMOND_ROLE_ID_TEMPLATE, i);
            String hostname = envProperties.getMachineHostnameByRoleId(roleId);
            for (String envProp: getSerializedEnvPropIds(roleId, HammondRole.ENV_HAMMOND_START)) {
                LOGGER.info("Launching command '{}' on {}", envProp, hostname);
                runSerializedCommandFlowFromRoleAsync(roleId, envProp, TimeUnit.HOURS, 2);    
                LOGGER.info("Sleeping for 10 mins");
                Thread.sleep(10*60*1000);
            }
        }
    }

    protected void stopLoad() throws InterruptedException {
        for (int i = 1; i <= TTStormLoadPerfTestTestbedProvider.NUM_OF_HAMMONDS; i++) {
            String roleId = String.format(TTStormLoadPerfTestTestbedProvider.HAMMOND_ROLE_ID_TEMPLATE, i);
            
            for (String envProp: getSerializedEnvPropIds(roleId, HammondRole.ENV_HAMMOND_STOP)) {
                runSerializedCommandFlowFromRole(roleId, envProp, TimeUnit.MINUTES, 5);
                Thread.sleep(3000);
            }
        }
    }
    
    private Iterable<String> getSerializedEnvPropIds(String roleId, String prefix) {
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
