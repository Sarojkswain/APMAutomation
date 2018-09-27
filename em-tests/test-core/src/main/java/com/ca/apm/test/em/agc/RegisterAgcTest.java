/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.em.agc;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.test.em.util.RestUtility;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.em.agc.CrossClusterTracesRevertTestBed;
import com.ca.tas.test.em.agc.CrossClusterTracesTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;

/**
 * Copied from {@link com.ca.apm.test.em.agc.AgcTest} and modified to suit
 * {@link com.ca.tas.test.em.agc.CrossClusterTracesTestBed}
 *
 */
public class RegisterAgcTest extends WebViewTestNgTest  {

    private final String REGISTRATION_RESULT = "Registration successful. Restart EM.";
    private final int SHUTDOWN_DELAY = 140;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private RestUtility utility = new RestUtility();

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
    }

    @Tas(testBeds = @TestBed(name = CrossClusterTracesTestBed.class, executeOn = "standalone"), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void configureTestbed() throws Exception {
        registerMoms();
        startTTs();
    }

    @Tas(testBeds = @TestBed(name = CrossClusterTracesRevertTestBed.class, executeOn = "standalone"), owner = "bhusu01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void configureRevertTestbed() throws Exception {
        registerMoms();
        startTTs();
    }

    private void registerMoms() throws Exception {
        String agcHost = envProperties.getMachineHostnameByRoleId(CrossClusterTracesTestBed.AGC_ROLE_ID);
        String standaloneHost = envProperties.getMachineHostnameByRoleId(CrossClusterTracesTestBed.STANDALONE_ROLE_ID);
        String momHost = envProperties.getMachineHostnameByRoleId(CrossClusterTracesTestBed.MOM_ROLE_ID);

        checkWebview(CrossClusterTracesTestBed.AGC_ROLE_ID);
        // register STANDALONE
        String agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for STANDALONE: " + agcToken);

        String resultStandalone = utility.registerMomtoAgc(standaloneHost, agcHost, agcToken);
        assertEquals(resultStandalone, REGISTRATION_RESULT);
        log.info(standaloneHost + ": " + resultStandalone);
        // restart STANDALONE
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(CrossClusterTracesTestBed.STANDALONE_ROLE_ID).getClwRunner();
        standaloneClwRunner.runClw("shutdown");
        try {
            emUtils.stopLocalEm(standaloneClwRunner, CrossClusterTracesTestBed.STANDALONE_ROLE_ID);
        } catch (Exception e) {
            log.warn("EM was not stopped properly!");
        }

        // register MOM
        agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for MOM: " + agcToken);

        String resultMom = utility.registerMomtoAgc(momHost, agcHost, agcToken);
        assertEquals(resultMom, REGISTRATION_RESULT);
        log.info(momHost + ": " + resultMom);
        // restart MOM
        ClwRunner momClwRunner =
            utilities.createClwUtils(CrossClusterTracesTestBed.MOM_ROLE_ID).getClwRunner();
        emUtils.stopRemoteEmWithTimeoutSec(standaloneClwRunner, momClwRunner, SHUTDOWN_DELAY);

        startEmAndWebview(CrossClusterTracesTestBed.STANDALONE_ROLE_ID);
        log.info(standaloneHost + " restarted.");

        startEmAndWebview(CrossClusterTracesTestBed.MOM_ROLE_ID);
        log.info(momHost + " restarted.");
    }

    @Override
    protected void killWebview(String roleId) {
        try {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("pkill").args(
                    Arrays.asList(EmRole.LinuxBuilder.WEBVIEW_EXECUTABLE)).build();
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } catch (Exception e) {
            // swallow all
        }
        ;
    }

    @Override
    protected void killEM(String roleId) {
        try {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("pkill").args(
                    Arrays.asList(EmRole.Builder.INTROSCOPE_EXECUTABLE)).build();
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } catch (Exception e) {
            // swallow all
        }

    }

    private void startTTs() {
        String command = "trace transactions exceeding 1 ms in agents matching \".*\" for 120 s";
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(CrossClusterTracesTestBed.STANDALONE_ROLE_ID).getClwRunner();

        standaloneClwRunner.runClw(command);
        runClwOnRemote(standaloneClwRunner, CrossClusterTracesTestBed.AGC_COLLECTOR_ROLE_ID, command);
        runClwOnRemote(standaloneClwRunner, CrossClusterTracesTestBed.MOM_ROLE_ID, command);
    }

    private void runClwOnRemote(ClwRunner localRunner, String roleId, String command) {
        ClwRunner runner =
            getRemoteClw(localRunner, utilities.createClwUtils(roleId).getClwRunner());
        runner.runClw(command);
    }

    private ClwRunner getRemoteClw(ClwRunner clwRunnerLocalEm, ClwRunner clwRunnerRemoteEm) {
         return new ClwRunner.Builder()
            .clwWorkStationDir(clwRunnerLocalEm.getClwWorkStationDir())
            .host(clwRunnerRemoteEm.getEmHost())
            .javaPath(clwRunnerLocalEm.getJavaPath())
            .password(clwRunnerRemoteEm.getPassword())
            .user(clwRunnerRemoteEm.getUser())
            .port(clwRunnerRemoteEm.getPort())
            .build();
    }
}
