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
package com.ca.apm.tests.testbed.machines;

import com.ca.apm.tests.artifact.AgentTrussVersion;
import com.ca.apm.tests.role.AgentStatsRole;
import com.ca.apm.tests.role.JavaAgentRole;
import com.ca.apm.tests.role.Websphere85Role;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.Map;

/**
 * Machine containing Websphere Server + Trade6 Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerWebsphere104Machine extends AppServerWebsphereMachine {

    public AppServerWebsphere104Machine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerWebsphere104Machine(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    protected void deployAgents(TestbedMachine machine, Map<String, String> gathererMap, Websphere85Role wasRole, ExecutionRole wasStoppingRole) {

        // current no SI
        JavaAgentRole currentJavaAgentNoSiRole = createCurrentJavaAgentNoSiRole(wasRole, true, false);
        currentJavaAgentNoSiRole.after(wasStoppingRole);
        machine.addRole(currentJavaAgentNoSiRole);
        gathererMap.put("agent_current_nosi_was.zip", currentJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI BT (only for 10.4+)
        JavaAgentRole currentJavaAgentNoSiBtRole = createCurrentJavaAgentNoSiBtRole(wasRole, true, true);
        currentJavaAgentNoSiBtRole.after(wasStoppingRole);
        machine.addRole(currentJavaAgentNoSiBtRole);
        gathererMap.put("agent_current_nosi_bt_was.zip", currentJavaAgentNoSiBtRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI ACC
        JavaAgentRole currentJavaAgentNoSiAccRole = createCurrentJavaAgentNoSiAccRole(wasRole, true, true);
        currentJavaAgentNoSiRole.after(wasStoppingRole);
        machine.addRole(currentJavaAgentNoSiAccRole);
        gathererMap.put("agent_current_acc_was.zip", currentJavaAgentNoSiAccRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current SI
        JavaAgentRole currentJavaAgentSiRole = createCurrentJavaAgentSiRole(wasRole, true, true);
        currentJavaAgentSiRole.after(wasStoppingRole);
        machine.addRole(currentJavaAgentSiRole);
        gathererMap.put("agent_current_si_was.zip", currentJavaAgentSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // prev no SI
        JavaAgentRole prevJavaAgentNoSiRole = createPrevJavaAgentNoSiRole(wasRole);
        prevJavaAgentNoSiRole.after(wasStoppingRole);
        machine.addRole(prevJavaAgentNoSiRole);
        gathererMap.put("agent_prev_nosi_was.zip", prevJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // prev SI
        JavaAgentRole prevJavaAgentSiRole = createPrevJavaAgentSiRole(wasRole);
        prevJavaAgentSiRole.after(wasStoppingRole);
        machine.addRole(prevJavaAgentSiRole);
        gathererMap.put("agent_prev_si_was.zip", prevJavaAgentSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        ////////////////////////////
        // DEPLOY AGENT STATS
        ////////////////////////////

        AgentStatsRole agentStatsRole = new AgentStatsRole.Builder(machineId + "_agentStatsRoleId", tasResolver)
                .agents(currentJavaAgentSiRole, currentJavaAgentNoSiAccRole, currentJavaAgentNoSiRole,
                        null, prevJavaAgentSiRole, prevJavaAgentNoSiRole, currentJavaAgentNoSiBtRole)
                .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_was.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("agent_was.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
        machine.addRole(agentStatsRole);
    }

}
