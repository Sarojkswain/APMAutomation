package com.ca.apm.tests.testbed.machines;

import com.ca.apm.tests.artifact.AgentTrussVersion;
import com.ca.apm.tests.role.AgentStatsRole;
import com.ca.apm.tests.role.JavaAgentRole;
import com.ca.apm.tests.role.KonakartRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.Map;

/**
 * Machine containing Tomcat Server + Konakart Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerTomcat104Machine extends AppServerTomcatMachine {

    public AppServerTomcat104Machine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerTomcat104Machine(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    protected void deployAgents(TestbedMachine machine, Map<String, String> gathererMap, TomcatRole tomcatRole, KonakartRole konakartRole) {

        // current no SI
        JavaAgentRole currentJavaAgentNoSiRole = createCurrentJavaAgentNoSiRole(tomcatRole, true, false);
        currentJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiRole);
        gathererMap.put("agent_current_nosi_tomcat.zip", currentJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI BT (only for 10.4+)
        JavaAgentRole currentJavaAgentNoSiBtRole = createCurrentJavaAgentNoSiBtRole(tomcatRole, true, true);
        currentJavaAgentNoSiBtRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiBtRole);
        gathererMap.put("agent_current_nosi_bt_tomcat.zip", currentJavaAgentNoSiBtRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI ACC
        JavaAgentRole currentJavaAgentNoSiAccRole = createCurrentJavaAgentNoSiAccRole(tomcatRole, true, true);
        currentJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiAccRole);
        gathererMap.put("agent_current_acc_tomcat.zip", currentJavaAgentNoSiAccRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI BRTM
        JavaAgentRole currentJavaAgentNoSiBrtmRole = createCurrentJavaAgentNoSiBrtmRole(tomcatRole, true, true);
        currentJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiBrtmRole);
        gathererMap.put("agent_current_brtm_tomcat.zip", currentJavaAgentNoSiBrtmRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current SI
        JavaAgentRole currentJavaAgentSiRole = createCurrentJavaAgentSiRole(tomcatRole, true, true);
        currentJavaAgentSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentSiRole);
        gathererMap.put("agent_current_si_tomcat.zip", currentJavaAgentSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // prev no SI
        JavaAgentRole prevJavaAgentNoSiRole = createPrevJavaAgentNoSiRole(tomcatRole);
        prevJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(prevJavaAgentNoSiRole);
        gathererMap.put("agent_prev_nosi_tomcat.zip", prevJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // prev SI
        JavaAgentRole prevJavaAgentSiRole = createPrevJavaAgentSiRole(tomcatRole);
        prevJavaAgentSiRole.after(konakartRole);
        machine.addRole(prevJavaAgentSiRole);
        gathererMap.put("agent_prev_si_tomcat.zip", prevJavaAgentSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        ////////////////////////////
        // DEPLOY AGENT STATS
        ////////////////////////////

        AgentStatsRole agentStatsRole = new AgentStatsRole.Builder(machineId + "_agentStatsRoleId", tasResolver)
                .agents(currentJavaAgentSiRole, currentJavaAgentNoSiAccRole, currentJavaAgentNoSiRole,
                        currentJavaAgentNoSiBrtmRole, prevJavaAgentSiRole, prevJavaAgentNoSiRole, currentJavaAgentNoSiBtRole)
                .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_tomcat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("agent_tomcat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
        machine.addRole(agentStatsRole);
    }
}
