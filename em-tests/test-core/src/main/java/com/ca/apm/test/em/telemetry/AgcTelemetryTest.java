/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.apm.test.em.telemetry;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.test.em.util.RestUtility;
import com.ca.tas.test.em.agc.AgcTelemetryTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

@Tas(testBeds = @TestBed(name = AgcTelemetryTestbed.class, executeOn = AgcTelemetryTestbed.STANDALONE_MACHINE), owner = "korzd01", size = SizeType.MEDIUM)
@Test(groups = {"telemetry", "bat"})
public class AgcTelemetryTest extends WebViewTestNgTest {

    private final String REGISTRATION_RESULT = "Registration successful. Restart EM.";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private RestUtility utility = new RestUtility();
    private final int SHUTDOWN_DELAY = 140;

    public void testLogs() throws Exception {
        registerProviders();
        log.info("Waiting 5 minutes ...");
        Utils.sleep(300000);
        assertTrue(new LogUtils(AgcTelemetryTestbed.POST_DATA_PATH, "\\x22Harvest Duration (ms)\\x22").isKeywordInLog(),
            "Missing data for metric \"Harvest Duration (ms)\"");
        assertTrue(new LogUtils(AgcTelemetryTestbed.POST_DATA_PATH, "\\x22Number of Historical Metrics\\x22").isKeywordInLog(),
            "Missing data for metric \"Number of Historical Metrics\"");
        testInstanceId(AgcTelemetryTestbed.MASTER_MACHINE + AgcTelemetryTestbed.COLLECTOR_ID_SUFFIX);
        testInstanceId(AgcTelemetryTestbed.MASTER_MACHINE + AgcTelemetryTestbed.MOM_ID_SUFFIX);
        testInstanceId(AgcTelemetryTestbed.PROVIDER_MACHINE + AgcTelemetryTestbed.COLLECTOR_ID_SUFFIX);
        testInstanceId(AgcTelemetryTestbed.PROVIDER_MACHINE + AgcTelemetryTestbed.MOM_ID_SUFFIX);
        testInstanceId(AgcTelemetryTestbed.STANDALONE_ID);
    }
    
    private void testInstanceId(String instanceId) {
        assertTrue(new LogUtils(AgcTelemetryTestbed.POST_DATA_PATH, "\\x22" + instanceId + "\\x22").isKeywordInLog(),
            "Missing instance ID");
    }

    private void registerProviders() throws Exception {
        String agcHost = envProperties.getMachineHostnameByRoleId(AgcTelemetryTestbed.MASTER_ROLE_ID);
        String momHost = envProperties.getMachineHostnameByRoleId(AgcTelemetryTestbed.PROVIDER_ROLE_ID);
        String standaloneHost = envProperties.getMachineHostnameByRoleId(AgcTelemetryTestbed.STANDALONE_ROLE_ID);

        checkWebview(AgcTelemetryTestbed.MASTER_ROLE_ID);

        // register STANDALONE
        String agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for STANDALONE: " + agcToken);
        String resultStandalone = utility.registerMomtoAgc(standaloneHost, agcHost, agcToken);
        assertEquals(resultStandalone, REGISTRATION_RESULT);
        log.info(AgcTelemetryTestbed.STANDALONE_ROLE_ID + ": " + resultStandalone);
        
        // restart STANDALONE
        killWebview(AgcTelemetryTestbed.STANDALONE_ROLE_ID);
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(AgcTelemetryTestbed.STANDALONE_ROLE_ID).getClwRunner();
        standaloneClwRunner.runClw("shutdown");
        try {
            emUtils.stopLocalEm(standaloneClwRunner, AgcTelemetryTestbed.STANDALONE_ROLE_ID);
        } catch (Exception e) {
            log.warn("EM was not stopped properly!");
        }
        startEmAndWebview(AgcTelemetryTestbed.STANDALONE_ROLE_ID);
        log.info(AgcTelemetryTestbed.STANDALONE_ROLE_ID + " restarted.");

        // register MOM
        agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for MOM: " + agcToken);
        String resultMom = utility.registerMomtoAgc(momHost, agcHost, agcToken);
        assertEquals(resultMom, REGISTRATION_RESULT);
        log.info(AgcTelemetryTestbed.PROVIDER_ROLE_ID + ": " + resultMom);
        
        // restart MOM
        killWebview(AgcTelemetryTestbed.PROVIDER_ROLE_ID);
        ClwRunner momClwRunner =
            utilities.createClwUtils(AgcTelemetryTestbed.PROVIDER_ROLE_ID).getClwRunner();
        emUtils.stopRemoteEmWithTimeoutSec(standaloneClwRunner, momClwRunner, SHUTDOWN_DELAY);
        
        startEmAndWebview(AgcTelemetryTestbed.PROVIDER_ROLE_ID);
        log.info(AgcTelemetryTestbed.PROVIDER_ROLE_ID + " restarted.");
    }
}
