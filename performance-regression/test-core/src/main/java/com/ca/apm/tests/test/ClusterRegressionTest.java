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

import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.tests.flow.*;
import com.ca.apm.tests.role.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.ClusterRegressionBaseTestBed;
import com.ca.apm.tests.testbed.ClusterRegressionCurrentTestBed;
import com.ca.apm.tests.testbed.ClusterRegressionTestBed;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;

/**
 * EM regression test.
 *
 * starts loads, monitors collectors, mom and webview.
 */
public class ClusterRegressionTest extends MyTasTestNgTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterRegressionTest.class);

    @Tas(testBeds = @TestBed(name = ClusterRegressionBaseTestBed.class, executeOn = ClusterRegressionTestBed.LOAD1_MACHINE_ID), size = SizeType.MAMMOTH, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void baseRegressionTest() {
        regression();
    }

    @Tas(testBeds = @TestBed(name = ClusterRegressionCurrentTestBed.class, executeOn = ClusterRegressionTestBed.LOAD1_MACHINE_ID), size = SizeType.MAMMOTH, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void currentRegressionTest() {
        regression();
    }

    @Tas(testBeds = @TestBed(name = ClusterRegressionCurrentTestBed.class, executeOn = ClusterRegressionTestBed.LOAD1_MACHINE_ID), size = SizeType.MAMMOTH, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void performanceTest() {
        startCluster();
        startPerfMon();

        startFakeWorkstationLoad();
        startMemoryMonitor();
        startDashboard();

        long endTimestamp =
                System.currentTimeMillis() + ClusterRegressionTestBed.getRunDuration(TimeUnit.MILLISECONDS);
        LOGGER.info("end timestamp: " + endTimestamp);

        while (endTimestamp > System.currentTimeMillis()) {

            checkServiceIsRunning(ClusterRegressionTestBed.EM_MOM_ROLE_ID, 5001);
            for (String roleId : ClusterRegressionTestBed.EM_C_ROLES) {
                checkServiceIsRunning(roleId, 5001);
            }
            checkServiceIsRunning(ClusterRegressionBaseTestBed.EM_WV_DB_ROLE_ID, 8082);
            LOGGER.info("cluster is running. Test ends in {} minutes.", (endTimestamp - System.currentTimeMillis()) / 60000);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
            }
        }

        LOGGER.info("test duration is over");

        getPerfLogFiles();

        assertTrue(true);
    }




    private void regression() {
        startCluster();
        startPerfMon();

        startWurlitzerLoad();
        startFakeWorkstationLoad();
        startMemoryMonitor();
        startHammondLoad();
        startDashboard();

        long endTimestamp =
                System.currentTimeMillis() + ClusterRegressionTestBed.getRunDuration(TimeUnit.MILLISECONDS);
        LOGGER.info("end timestamp: " + endTimestamp);

        while (endTimestamp > System.currentTimeMillis()) {

            checkServiceIsRunning(ClusterRegressionTestBed.EM_MOM_ROLE_ID, 5001);
            for (String roleId : ClusterRegressionTestBed.EM_C_ROLES) {
                checkServiceIsRunning(roleId, 5001);
            }
            checkServiceIsRunning(ClusterRegressionBaseTestBed.EM_WV_DB_ROLE_ID, 8082);
            LOGGER.info("cluster is running. Test ends in {} minutes.", (endTimestamp - System.currentTimeMillis()) / 60000);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
            }
        }

        LOGGER.info("test duration is over");

        getPerfLogFiles();

        assertTrue(true);
    }

    protected void checkServiceIsRunning(String roleId, int port) {
        String host = envProperties.getMachineHostnameByRoleId(roleId);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(host, port, 10);
    }

    @AfterTest
    public void stopProcesses() throws Exception {
        stopPerfMon();
        stopWurlitzerLoad();
        stopFakeWorkstationLoad();
        stopHammondLoad();
        stopMemoryMonitor();
        stopDashboard();
        stopCluster();
    }

    protected void startCluster() {
        new Thread() {
            public void run() {
                runSerializedCommandFlowFromRole(ClusterRegressionTestBed.EM_MOM_ROLE_ID,
                    EmRole.ENV_START_EM);
            }
        }.start();

        for (final String roleId : ClusterRegressionTestBed.EM_C_ROLES) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_EM);
                }
            }.start();
        }

        new Thread() {
            public void run() {
                runSerializedCommandFlowFromRole(ClusterRegressionTestBed.AGC_ROLE_ID,
                        EmRole.ENV_START_EM);
                runSerializedCommandFlowFromRole(ClusterRegressionTestBed.AGC_ROLE_ID,
                        EmRole.ENV_START_WEBVIEW);
            }
        }.start();

        new Thread() {
            public void run() {
                runSerializedCommandFlowFromRole(ClusterRegressionTestBed.AGC_C_ROLE_ID,
                        EmRole.ENV_START_EM);
            }
        }.start();

        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(
            envProperties.getMachineHostnameByRoleId(ClusterRegressionTestBed.EM_MOM_ROLE_ID),
            5001, 600);

        runSerializedCommandFlowFromRole(ClusterRegressionTestBed.EM_WV_DB_ROLE_ID,
            EmRole.ENV_START_WEBVIEW);

        for (String roleId : ClusterRegressionTestBed.EM_C_ROLES) {
            utilities.createPortUtils().waitTillRemotePortIsBusyInSec(
                envProperties.getMachineHostnameByRoleId(roleId), 5001, 600);
        }
    }

    protected void stopCluster() {
        runSerializedCommandFlowFromRole(ClusterRegressionTestBed.EM_WV_DB_ROLE_ID,
            EmRole.ENV_STOP_WEBVIEW);
        runSerializedCommandFlowFromRole(ClusterRegressionTestBed.EM_MOM_ROLE_ID,
            EmRole.ENV_STOP_EM);
        for (int i = 0; i < ClusterRegressionTestBed.EM_C_ROLES.length; i++) {
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.EM_C_ROLES[i],
                EmRole.ENV_STOP_EM);
        }
    }

    protected void startWurlitzerLoad() {
        runSerializedCommandFlowFromRole(ClusterRegressionTestBed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_RUN_WURLITZER);
    }

    protected void stopWurlitzerLoad() {
        try {
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.WURLITZER_ROLE_ID,
                WurlitzerRole.ENV_STOP_WURLITZER);
        } catch (Exception e) {}
    }

    protected void startFakeWorkstationLoad() {
        for (String id : getSerializedIds(ClusterRegressionTestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID,
            CautlRole.ENV_CAUTL_START)) {
            runSerializedCommandFlowFromRole(
                ClusterRegressionTestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID, id);
        }
    }

    protected void stopFakeWorkstationLoad() {
        try {
            runSerializedCommandFlowFromRole(
                ClusterRegressionTestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID, CautlRole.ENV_CAUTL_STOP);
        } catch (Exception e) {}
    }

    protected void startHammondLoad() {
        for (final String id : getSerializedIds(ClusterRegressionTestBed.HAMMOND_ROLE_ID,
            HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(ClusterRegressionTestBed.HAMMOND_ROLE_ID, id);
                }
            }.start();
        }

        for (final String id : getSerializedIds(ClusterRegressionTestBed.HAMMOND_AGC_ROLE_ID,
                HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(ClusterRegressionTestBed.HAMMOND_AGC_ROLE_ID, id);
                }
            }.start();
        }
    }

    protected void stopHammondLoad() {
        for (String id : getSerializedIds(ClusterRegressionTestBed.HAMMOND_ROLE_ID,
                HammondRole.ENV_HAMMOND_STOP)) {
            try {
                runSerializedCommandFlowFromRole(ClusterRegressionTestBed.HAMMOND_ROLE_ID, id);
            } catch (Exception e) {
            }
        }
        for (final String id : getSerializedIds(ClusterRegressionTestBed.HAMMOND_AGC_ROLE_ID,
                HammondRole.ENV_HAMMOND_STOP)) {
            try {
                runSerializedCommandFlowFromRole(ClusterRegressionTestBed.HAMMOND_AGC_ROLE_ID, id);
            } catch (Exception e) {
            }
        }
    }

    protected void startPerfMon() {
        for (int i = 0; i < ClusterRegressionTestBed.PERFMON_C_ROLES.length; i++) {
            final int param = i;
            new Thread() {
                public void run() {
                    try {
                        runSerializedCommandFlowFromRole(
                            ClusterRegressionTestBed.PERFMON_C_ROLES[param],
                            PerfMonitorRole.ENV_PERF_MONITOR_START);
                    } catch (Exception e) {
                        runSerializedCommandFlowFromRole(
                            ClusterRegressionTestBed.PERFMON_C_ROLES[param],
                            PerfMonitorRole.ENV_PERF_MONITOR_STOP);
                        runSerializedCommandFlowFromRole(
                            ClusterRegressionTestBed.PERFMON_C_ROLES[param],
                            PerfMonitorRole.ENV_PERF_MONITOR_START);
                    }
                }
            }.start();
        }
        try {
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        } catch (Exception e) {
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_STOP);
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        }
    }

    protected void startMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        for (String machineId : ClusterRegressionTestBed.MEMORY_MONITOR_MACHINES) {
            String roleId = "memoryMonitorRole_" + machineId;
            try {
                Map<String, String> roleProps =
                        Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
                IFlowContext startFlowContext = deserializeFromProperties(roleId,
                        MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
                        RunMemoryMonitorFlowContext.class);
                runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.DAYS, 2);

            } catch (Exception e) {
                LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                        e.getMessage());
            }
        }
    }

    protected void stopMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;
        for (String machineId : ClusterRegressionTestBed.MEMORY_MONITOR_MACHINES) {
            String roleId = "memoryMonitorRole_" + machineId;
            try {
                Map<String, String> roleProps =
                        Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
                IFlowContext startFlowContext = deserializeFromProperties(roleId,
                        MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
                        RunMemoryMonitorFlowContext.class);
                runFlowByMachineIdAsync(machineId, flowClass, startFlowContext, TimeUnit.MINUTES, ClusterRegressionTestBed.getRunDuration(TimeUnit.MINUTES) + 30);

            } catch (Exception e) {
                LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                        e.getMessage());
            }
        }
    }

    protected void getPerfLogFiles() {
        Thread[] threads = new Thread[ClusterRegressionTestBed.PERFMON_C_ROLES.length];
        for (int i = 0; i < ClusterRegressionTestBed.PERFMON_C_ROLES.length; i++) {
            final int param = i;
            threads[i] = new Thread() {
                public void run() {
                    try {
                        runSerializedCommandFlowFromRole(
                            ClusterRegressionTestBed.PERFMON_C_ROLES[param],
                            PerfMonitorRole.ENV_GET_PERF_LOG);
                    } catch (Exception e) {}
                }
            };
            threads[i].start();
        }
        try {
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.PERFMON_MOM_ROLE_ID,
                PerfMonitorRole.ENV_GET_PERF_LOG);
        } catch (Exception e) {}

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {}
        }

        CsvToXlsFlowContext context =
            (CsvToXlsFlowContext) deserializeFlowContextFromRole(
                ClusterRegressionTestBed.CSV_TO_XLS_ROLE_ID, CsvToXlsRole.RUN_CSV_TO_XLS,
                CsvToXlsFlowContext.class);
        runFlowByMachineId(ClusterRegressionTestBed.LOAD1_MACHINE_ID, CsvToXlsFlow.class, context);
    }


    protected void stopPerfMon() {
        for (int i = 0; i < ClusterRegressionTestBed.PERFMON_C_ROLES.length; i++) {
            runSerializedCommandFlowFromRole(ClusterRegressionTestBed.PERFMON_C_ROLES[i],
                PerfMonitorRole.ENV_PERF_MONITOR_STOP);
        }
        runSerializedCommandFlowFromRole(ClusterRegressionTestBed.PERFMON_MOM_ROLE_ID,
            PerfMonitorRole.ENV_PERF_MONITOR_STOP);
    }

    protected void startDashboard() {
        String roleId = ClusterRegressionTestBed.WEB_VIEW_LOAD_ROLE;

        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Map<String, String> roleProps = Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        IFlowContext startFlowContext = deserializeFromProperties(roleId,
                WebViewLoadRole.ENV_WEBVIEW_LOAD_START, roleProps,
                RunWebViewLoadFlowContext.class);
        runFlowByMachineIdAsync(machineId, RunWebViewLoadFlow.class, startFlowContext, TimeUnit.MINUTES, ClusterRegressionTestBed.getRunDuration(TimeUnit.MINUTES) + 30);
    }

    protected void stopDashboard() {
        String roleId = ClusterRegressionTestBed.WEB_VIEW_LOAD_ROLE;

        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Map<String, String> roleProps = Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        IFlowContext startFlowContext = deserializeFromProperties(roleId,
                WebViewLoadRole.ENV_WEBVIEW_LOAD_STOP, roleProps,
                RunWebViewLoadFlowContext.class);
        runFlowByMachineIdAsync(machineId, RunWebViewLoadFlow.class, startFlowContext, TimeUnit.MINUTES, 10);
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
