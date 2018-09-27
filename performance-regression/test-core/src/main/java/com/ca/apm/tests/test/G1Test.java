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
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.tests.flow.RunMemoryMonitorFlow;
import com.ca.apm.tests.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.tests.flow.RunWebViewLoadFlow;
import com.ca.apm.tests.flow.RunWebViewLoadFlowContext;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.G1TestBed;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

/**
 * EM regression test.
 *
 * starts loads, monitors collectors, mom and webview.
 */
public class G1Test extends MyTasTestNgTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(G1Test.class);


    private static final String EM_LAXNL_JAVA_OPTION_G1 =
            "-Djava.awt.headless=true " +
            "-Dmail.mime.charset=UTF-8 " +
            "-Dorg.owasp.esapi.resources=./config/esapi " +
            "-Xss512k " +
            "-Dcom.wily.assert=false " +
            "-showversion " +
            "-XX:+UseG1GC " +
            "-Dcom.sun.management.jmxremote " +
            "-Dcom.sun.management.jmxremote.port=4444 " +
            "-Dcom.sun.management.jmxremote.authenticate=false " +
            "-Dcom.sun.management.jmxremote.ssl=false " +
            "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
            "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
            "-verbose:gc " +
            "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE ;


    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_50ms() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:MaxGCPauseMillis=50";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_100ms() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:MaxGCPauseMillis=100";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_200ms() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:MaxGCPauseMillis=200";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_400ms() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:MaxGCPauseMillis=400";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_NewSize10() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=10 -XX:G1MaxNewSizePercent=75";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_NewSize3() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=3 -XX:G1MaxNewSizePercent=50";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_Threads8() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:ParallelGCThreads=8";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_Threads2() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:ParallelGCThreads=2";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_Threads16() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:ParallelGCThreads=16";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_G1Default() throws Exception {
        runTest(EM_LAXNL_JAVA_OPTION_G1);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_G1Region4M() throws Exception {
        String opts = EM_LAXNL_JAVA_OPTION_G1 +
                " -XX:G1HeapRegionSize=4m";
        runTest(opts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_ParNewGC() throws  Exception {
        String opts = 
                "-Djava.awt.headless=true " +
                "-XX:MaxPermSize=256m " +
                "-Dmail.mime.charset=UTF-8 " +
                "-Dorg.owasp.esapi.resources=./config/esapi " +
                "-XX:+UseConcMarkSweepGC " +
                "-XX:+UseParNewGC " +
                "-Xss512k " +
                "-Dcom.wily.assert=false " +
                "-showversion " +
                "-Dcom.sun.management.jmxremote " +
                "-Dcom.sun.management.jmxremote.port=4444 " +
                "-Dcom.sun.management.jmxremote.authenticate=false " +
                "-Dcom.sun.management.jmxremote.ssl=false " +
                "-XX:CMSInitiatingOccupancyFraction=50 " +
                "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                "-verbose:gc " +
                "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE;
        runTest(opts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"G1"})
    public void test_NoCMS() throws  Exception {
        String opts =
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE;
        runTest(opts);
    }

    private void runTest(String javaOpts) throws Exception {
        // set java opts
        String installDir = envProperties.getRolePropertyById(G1TestBed.EM_ROLE_ID, EmRole.ENV_PROPERTY_INSTALL_DIR);
        ConfigureFlowContext fixLaxCtx = new ConfigureFlowContext.Builder()
                .configurationMap(
                        installDir + "\\Introscope_Enterprise_Manager.lax",
                        Collections.singletonMap("lax.nl.java.option.additional", javaOpts))
                .build();
        runFlowByMachineId(G1TestBed.EM_MACHINE_ID, ConfigureFlow.class, fixLaxCtx);

        startEm();
        startPerfMon();

        startHammondLoad();
        startFakeWorkstationLoad();
        startWurlitzerLoad();
        startMemoryMonitor();
        startDashboard();

        long endTimestamp =
                System.currentTimeMillis() + G1TestBed.getRunDuration(TimeUnit.MILLISECONDS);
        LOGGER.info("end timestamp: " + endTimestamp);

        while (endTimestamp > System.currentTimeMillis()) {

            checkServiceIsRunning(G1TestBed.EM_ROLE_ID, 5001);
            checkServiceIsRunning(G1TestBed.EM_ROLE_ID, 8082);

            LOGGER.info("cluster is running. Test ends in {} minutes.", (endTimestamp - System.currentTimeMillis()) / 60000);
            Thread.sleep(60000);
        }
        LOGGER.info("test duration is over");

        runSerializedCommandFlowFromRole(G1TestBed.PERFMON_EM_ROLE_ID, PerfMonitorRole.ENV_GET_PERF_LOG);

        assertTrue(true);
    }

    private void checkServiceIsRunning(String roleId, int port) {
        String host = envProperties.getMachineHostnameByRoleId(roleId);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(host, port, 10);
    }

    @AfterTest
    public void stopProcesses() throws Exception {
        stopPerfMon();
        stopMemoryMonitor();
        stopEm();
    }

    private void startEm() {
        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_START_EM);

        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_START_WEBVIEW);
    }

    private void stopEm() {
        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_STOP_WEBVIEW);
        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_STOP_EM);
    }

    private void startWurlitzerLoad() {
        runSerializedCommandFlowFromRole(G1TestBed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_RUN_WURLITZER);
    }

    private void startFakeWorkstationLoad() {
        for (String id : getSerializedIds(G1TestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID,
                CautlRole.ENV_CAUTL_START)) {
            runSerializedCommandFlowFromRole(
                    G1TestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID, id);
        }
    }

    private void startHammondLoad() {
        for (final String id : getSerializedIds(G1TestBed.HAMMOND_ROLE_ID,
            HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(G1TestBed.HAMMOND_ROLE_ID, id);
                }
            }.start();
        }
    }

    private void startPerfMon() {
        try {
            runSerializedCommandFlowFromRole(
                    G1TestBed.PERFMON_EM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        } catch (Exception e) {
            runSerializedCommandFlowFromRole(
                    G1TestBed.PERFMON_EM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_STOP);
            runSerializedCommandFlowFromRole(
                    G1TestBed.PERFMON_EM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        }
    }

    private void startMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;

        String roleId = "memoryMonitorRole_" + G1TestBed.EM_MACHINE_ID;
        try {
            Map<String, String> roleProps =
                    Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
            IFlowContext startFlowContext = deserializeFromProperties(roleId,
                    MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
                    RunMemoryMonitorFlowContext.class);
            runFlowByMachineIdAsync(G1TestBed.EM_MACHINE_ID, flowClass, startFlowContext, TimeUnit.DAYS, 2);

        } catch (Exception e) {
            LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                    e.getMessage());
        }
    }

    private void stopMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;

        String roleId = "memoryMonitorRole_" + G1TestBed.EM_MACHINE_ID;
        try {
            Map<String, String> roleProps =
                    Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
            IFlowContext startFlowContext = deserializeFromProperties(roleId,
                    MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
                    RunMemoryMonitorFlowContext.class);
            runFlowByMachineIdAsync(G1TestBed.EM_MACHINE_ID, flowClass, startFlowContext, TimeUnit.MINUTES, G1TestBed.getRunDuration(TimeUnit.MINUTES) + 30);

        } catch (Exception e) {
            LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                    e.getMessage());
        }
    }

    private void stopPerfMon() {
        runSerializedCommandFlowFromRole(G1TestBed.PERFMON_EM_ROLE_ID,
            PerfMonitorRole.ENV_PERF_MONITOR_STOP);
    }

    private void startDashboard() {
        String roleId = G1TestBed.WEB_VIEW_LOAD_ROLE;
        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Map<String, String> roleProps = Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        IFlowContext startFlowContext = deserializeFromProperties(roleId,
                WebViewLoadRole.ENV_WEBVIEW_LOAD_START, roleProps,
                RunWebViewLoadFlowContext.class);
        runFlowByMachineIdAsync(machineId, RunWebViewLoadFlow.class, startFlowContext, TimeUnit.MINUTES, G1TestBed.getRunDuration(TimeUnit.MINUTES) + 30);
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
