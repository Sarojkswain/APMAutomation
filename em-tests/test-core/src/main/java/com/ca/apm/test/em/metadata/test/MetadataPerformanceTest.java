/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.em.metadata.test;



import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.test.em.metadata.MetadataPerformanceStandaloneTestbed;
import com.ca.apm.test.em.metadata.MetadataStandaloneTestbedMetricRotation;
import com.ca.apm.test.em.metadata.hammond.HammondRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

public class MetadataPerformanceTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataPerformanceTest.class);

    private static final String AGENT =
        "SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)";
    private static final String MATCHES_PER_INTERVAL =
        "Enterprise Manager|Internal|Metric Group:Metric Matches Per Interval";

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * this test generates infinite amount historical of Metrics by restarting hammond with
     * different prefixes
     * 
     * @throws InterruptedException
     */
    @Tas(testBeds = @TestBed(name = MetadataStandaloneTestbedMetricRotation.class, executeOn = MetadataPerformanceStandaloneTestbed.HAMMOND_MACHINE_1), size = SizeType.COLOSSAL, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"metadata"})
    public void metadataMultipleAgentReconnectionTest() throws InterruptedException {

        executor.execute(new HammondStartStopTask(
            MetadataPerformanceStandaloneTestbed.HAMMOND_ROLE_1,
            MetadataPerformanceStandaloneTestbed.HAMMOND_MACHINE_1));
        executor.execute(new HammondStartStopTask(
            MetadataPerformanceStandaloneTestbed.HAMMOND_ROLE_2,
            MetadataPerformanceStandaloneTestbed.HAMMOND_MACHINE_2));


        measureForTime(48, TimeUnit.HOURS, 120, TimeUnit.SECONDS);


    }

    /**
     * this test generates infinite amount of historical Metrics by periodicaly adding suffixes to
     * metric paths inside agents
     * 
     * @throws InterruptedException
     */
    @Tas(testBeds = @TestBed(name = MetadataStandaloneTestbedMetricRotation.class, executeOn = MetadataPerformanceStandaloneTestbed.HAMMOND_MACHINE_1), size = SizeType.COLOSSAL, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"metadata"})
    public void metadataMetricRotationTest() throws InterruptedException {

        startHammondLoad(MetadataPerformanceStandaloneTestbed.HAMMOND_ROLE_1,
            MetadataPerformanceStandaloneTestbed.HAMMOND_MACHINE_1, null);
        startHammondLoad(MetadataPerformanceStandaloneTestbed.HAMMOND_ROLE_2,
            MetadataPerformanceStandaloneTestbed.HAMMOND_MACHINE_2, null);



        measureForTime(72, TimeUnit.HOURS, 120, TimeUnit.SECONDS);


    }

    private void measureForTime(int count, TimeUnit unit, int interval, TimeUnit interfvalunit)
        throws InterruptedException {
        
        for (long stop=System.currentTimeMillis()+unit.toMillis(count);stop>System.currentTimeMillis();) {
            Thread.sleep(interfvalunit.toMillis(interval));
            LOGGER
                .error("************************* WAIT WAIT WAIT ******************************************");
        }
    }

    private class HammondStartStopTask implements Runnable {

        String roleId;
        String machineId;

        public HammondStartStopTask(String roleId, String machineId) {
            this.roleId = roleId;
            this.machineId = machineId;
        }


        @Override
        public void run() {
            try {

                while (true) {
                    startHammondLoad(roleId, machineId, randomString());
                    Thread.sleep(TimeUnit.MINUTES.toMillis(15));
                    stopHammondLoad(roleId);
                    Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                }

            } catch (Throwable t) {
                System.out.println(t.toString());
            }

        }

    }

    private String randomString() {
        SecureRandom random = new SecureRandom();
        return "_" + new BigInteger(32, random).toString(32);

    }



    public void startHammondLoad(String roleId, String machineId, String prefix) {
        LOGGER.info("***************** STARTING HAMMOND ON " + roleId
            + " ********************************");
        for (String id : getSerializedIds(roleId, HammondRole.ENV_HAMMOND_START)) {
            RunCommandFlowContext rfc = deserializeCommandFlowFromRole(roleId, id);
            List<String> args = (List<String>) rfc.getArgs();
            LOGGER.info(args.toString());
            for (ListIterator<String> it = args.listIterator(); it.hasNext();) {
                String curr = it.next();
                if (prefix != null && "-p".equals(curr)) {
                    it.next();
                    it.set(prefix);
                }

                if ("-s".equals(curr)) {
                    it.next();
                    it.set("1");
                }
            }
            runCommandFlowByMachineIdAsync(machineId, rfc);
        }


    }

    protected void stopHammondLoad(String roleId) {
        for (String id : getSerializedIds(roleId, HammondRole.ENV_HAMMOND_STOP)) {
            runSerializedCommandFlowFromRoleAsync(roleId, id);
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
}
