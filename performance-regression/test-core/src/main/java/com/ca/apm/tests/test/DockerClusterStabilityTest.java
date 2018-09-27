/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.test;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.tests.flow.RunMemoryMonitorFlow;
import com.ca.apm.tests.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.tests.flow.RunWebViewLoadFlow;
import com.ca.apm.tests.flow.RunWebViewLoadFlowContext;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.DockerClusterStabilityTestBed;
import com.ca.tas.role.HammondRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

/**
 * EM regression test.
 *
 * starts loads, monitors collectors, mom and webview.
 */
public class DockerClusterStabilityTest extends MyTasTestNgTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClusterStabilityTest.class);

    @Tas(testBeds = @TestBed(name = DockerClusterStabilityTestBed.class, executeOn = DockerClusterStabilityTestBed.LOAD1_MACHINE_ID), size = SizeType.MAMMOTH, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"docker"})
    public void dockerStability() throws Exception {

        deployDocker();

        startWurlitzerLoad();
        startFakeWorkstationLoad();
        startMemoryMonitor();
        startHammondLoad();
        startDashboard();

        long endTimestamp =
                System.currentTimeMillis() + DockerClusterStabilityTestBed.getRunDuration(TimeUnit.MILLISECONDS);
        LOGGER.info("end timestamp: " + endTimestamp);

        try {
            while (endTimestamp > System.currentTimeMillis()) {
                checkServiceIsRunning(DockerClusterStabilityTestBed.MOM_DOCKER_ROLE_ID, 5001);
                checkServiceIsRunning(DockerClusterStabilityTestBed.COL1_DOCKER_ROLE_ID, 5001);
                checkServiceIsRunning(DockerClusterStabilityTestBed.COL2_DOCKER_ROLE_ID, 5001);
                checkServiceIsRunning(DockerClusterStabilityTestBed.COL3_DOCKER_ROLE_ID, 5001);

                LOGGER.info("cluster is running");

                Thread.sleep(60000);
            }
            LOGGER.info("test duration is over");
        }
        finally {
            runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.MOM_DOCKER_ROLE_ID, DockerComposeRole.STOP);
            runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.MOM_DOCKER_ROLE_ID, DockerComposeRole.RM);
            runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.WV_DOCKER_ROLE_ID, DockerComposeRole.STOP);
            runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.WV_DOCKER_ROLE_ID, DockerComposeRole.RM);
        }

        assertTrue(true);
    }

    private void deployDocker() throws InterruptedException {

        LOGGER.info("starting Docker db role deploy");
        runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.DB_DOCKER_ROLE_ID, DockerComposeRole.UP);

        ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
        taskExecutor.invokeAll(Arrays.asList(
        new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return startDockerDeploy(DockerClusterStabilityTestBed.COL1_DOCKER_ROLE_ID);
            }
        }, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return startDockerDeploy(DockerClusterStabilityTestBed.COL2_DOCKER_ROLE_ID);
            }
        }, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return startDockerDeploy(DockerClusterStabilityTestBed.COL3_DOCKER_ROLE_ID);
            }
        }));

        startDockerDeploy(DockerClusterStabilityTestBed.MOM_DOCKER_ROLE_ID);
        startDockerDeploy(DockerClusterStabilityTestBed.WV_DOCKER_ROLE_ID);

        LOGGER.info("Docker deploy has finished");
    }

    private Object startDockerDeploy(String roleId) {
        LOGGER.info("start Docker role '" + roleId + "' deploy");
        runSerializedCommandFlowFromRole(roleId, DockerComposeRole.UP);

        String host = envProperties.getMachineHostnameByRoleId(roleId);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(host, 5001, 600);
        return null;
    }

    protected void checkServiceIsRunning(String roleId, int port) {
        String host = envProperties.getMachineHostnameByRoleId(roleId);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(host, port, 120);
    }

    protected void startWurlitzerLoad() {
        runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_RUN_WURLITZER);
    }

    protected void startFakeWorkstationLoad() {
        for (String id : getSerializedIds(DockerClusterStabilityTestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID,
            CautlRole.ENV_CAUTL_START)) {
            runSerializedCommandFlowFromRole(
                    DockerClusterStabilityTestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID, id);
        }
    }

    protected void startHammondLoad() {
        for (final String id : getSerializedIds(DockerClusterStabilityTestBed.HAMMOND1_ROLE_ID,
            HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.HAMMOND1_ROLE_ID, id);
                }
            }.start();
        }
        for (final String id : getSerializedIds(DockerClusterStabilityTestBed.HAMMOND2_ROLE_ID,
                HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.HAMMOND2_ROLE_ID, id);
                }
            }.start();
        }
        for (final String id : getSerializedIds(DockerClusterStabilityTestBed.HAMMOND3_ROLE_ID,
                HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(DockerClusterStabilityTestBed.HAMMOND3_ROLE_ID, id);
                }
            }.start();
        }
    }

    protected void startMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        for (String machineId : Arrays.asList(
                DockerClusterStabilityTestBed.MOM_DOCKER_MACHINE_ID,
                DockerClusterStabilityTestBed.COL1_DOCKER_MACHINE_ID,
                DockerClusterStabilityTestBed.COL2_DOCKER_MACHINE_ID,
                DockerClusterStabilityTestBed.COL3_DOCKER_MACHINE_ID)) {
            String roleId = "memoryMonitorRole_" + machineId;
            try {
                Map<String, String> roleProps =
                        Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
                IFlowContext startFlowContext = deserializeFromProperties(roleId,
                        MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
                        RunMemoryMonitorFlowContext.class);
                runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.MINUTES, DockerClusterStabilityTestBed.getRunDuration(TimeUnit.MINUTES) + 30);

            } catch (Exception e) {
                LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                        e.getMessage());
            }
        }
    }

    protected void startDashboard() {
        String roleId = DockerClusterStabilityTestBed.WEB_VIEW_LOAD_ROLE;

        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Map<String, String> roleProps = Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        IFlowContext startFlowContext = deserializeFromProperties(roleId,
                WebViewLoadRole.ENV_WEBVIEW_LOAD_START, roleProps,
                RunWebViewLoadFlowContext.class);
        runFlowByMachineIdAsync(machineId, RunWebViewLoadFlow.class, startFlowContext, TimeUnit.MINUTES, DockerClusterStabilityTestBed.getRunDuration(TimeUnit.MINUTES) + 30);
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
