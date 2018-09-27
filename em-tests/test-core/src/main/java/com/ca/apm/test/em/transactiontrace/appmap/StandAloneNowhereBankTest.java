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

package com.ca.apm.test.em.transactiontrace.appmap;

import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.testapp.custom.NowhereBankBTRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.test.em.agc.CrossClusterTracesTestBed;
import com.ca.tas.test.em.transactiontrace.appmap.StandAloneWithNowhereBankBTTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class StandAloneNowhereBankTest extends TasTestNgTest{

    @Tas(testBeds = @TestBed(name = StandAloneWithNowhereBankBTTestBed.class, executeOn = StandAloneWithNowhereBankBTTestBed.MACHINE_ID), owner = "bhusu01", size = SizeType.SMALL)
    @Test(groups = {"start"})
    public void testStart() {
        // Start EM
        runSerializedCommandFlowFromRole(StandAloneWithNowhereBankBTTestBed.EM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(StandAloneWithNowhereBankBTTestBed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);
        //Start nowhere bank
        startAllNowhereBankAgents(StandAloneWithNowhereBankBTTestBed.NWB_ROLE_ID);
        startTT();
    }

    @Tas(testBeds = @TestBed(name = StandAloneWithNowhereBankBTTestBed.class, executeOn = StandAloneWithNowhereBankBTTestBed.MACHINE_ID), owner = "bhusu01", size = SizeType.SMALL)
    @Test(groups = {"start"})
    public void testWithMediatorOnDifferentCluster() {
        // Start EM
        runSerializedCommandFlowFromRole(StandAloneWithNowhereBankBTTestBed.EM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(StandAloneWithNowhereBankBTTestBed.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);
        // configure Mediator to not report to our EM. Point to a different port so agent stays running
        configureAgentPort(6001, NowhereBankBTRole.NWB_AGENT_NAMES[1],StandAloneWithNowhereBankBTTestBed.MACHINE_ID);
        //Start nowhere bank
        startAllNowhereBankAgents(StandAloneWithNowhereBankBTTestBed.NWB_ROLE_ID);
        startTT();
    }

    private void startTT() {
        String command = "trace transactions exceeding 1 ms in agents matching \".*\" for 120 s";
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(StandAloneWithNowhereBankBTTestBed.EM_ROLE_ID)
                .getClwRunner();

        standaloneClwRunner.runClw(command);
    }

    private void startAllNowhereBankAgents(String nwbRoleId) {
        runSerializedCommandFlowFromRoleAsync(nwbRoleId, NowhereBankBTRole.MESSAGING_SERVER_01);
        runSerializedCommandFlowFromRoleAsync(nwbRoleId, NowhereBankBTRole.BANKING_ENGINE_02);
        runSerializedCommandFlowFromRoleAsync(nwbRoleId, NowhereBankBTRole.BANKING_MEDIATOR_03);
        runSerializedCommandFlowFromRoleAsync(nwbRoleId, NowhereBankBTRole.BANKING_PORTAL_04);
        runSerializedCommandFlowFromRoleAsync(nwbRoleId, NowhereBankBTRole.BANKING_GENERATOR_05);
    }

    /**
     * Configures the chosen agentName on the chosen machineID to report to the host on the chosen
     * host machine. The port is not modified and left as default - 5001.
     *
     * @param port
     * @param agentName
     * @param machineID
     */
    private void configureAgentPort(int port, String agentName, String machineID) {
        String installDirPath = envProperties.getRolePropertiesById(machineID
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX).getProperty(NowhereBankBTRole.INSTALL_DIR);
        String profileFileFormatter = envProperties.getRolePropertiesById(machineID
            + CrossClusterTracesTestBed.NWB_ROLE_ID_SUFFIX).getProperty(NowhereBankBTRole.PROFILE_FILE_FORMATTER);

        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig.put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT", String.valueOf(port));

        ConfigureFlowContext.Builder builder = new ConfigureFlowContext.Builder();
        builder.configurationMap(
            installDirPath + String.format(profileFileFormatter, agentName), replacePairsConfig);

        runConfigureFlowByMachineIdAsync(machineID, builder.build());
    }


}
