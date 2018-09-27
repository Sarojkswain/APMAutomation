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
import com.ca.apm.tests.artifact.WeblogicVersion;
import com.ca.apm.tests.role.AgentStatsRole;
import com.ca.apm.tests.role.JavaAgentRole;
import com.ca.apm.tests.role.PerfJavaRole;
import com.ca.apm.tests.role.Weblogic103Role;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.TestbedMachine;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Machine containing Weblogic Server + StockTrader Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerWeblogic104Machine extends AppServerWeblogicMachine {

    public static final String APP_SERVER_NAME = "weblogic103"; // stays the same for export to XLS to work // TODO
    public static final String APP_SERVER_IDENT = "AdminServer"; // idetification for jstat, wmic

    public static final String DEFAULT_WLS12_BEA_HOME = "c:/sw/weblogic12";
    public static final String DEFAULT_WLS12_INSTALL_DIR = "c:/sw/weblogic12/wlserver";

    public AppServerWeblogic104Machine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerWeblogic104Machine(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    protected void deployAgents(TestbedMachine machine, Map<String, String> gathererMap, Weblogic103Role wlsRole, ExecutionRole wlsStoppingRole) {

        // current no SI
        JavaAgentRole currentJavaAgentNoSiRole = createCurrentJavaAgentNoSiRole(wlsRole, true, false);
        currentJavaAgentNoSiRole.after(wlsStoppingRole);
        machine.addRole(currentJavaAgentNoSiRole);
        gathererMap.put("agent_current_nosi_wls.zip", currentJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI BT (only for 10.4+)
        JavaAgentRole currentJavaAgentNoSiBtRole = createCurrentJavaAgentNoSiBtRole(wlsRole, true, true);
        currentJavaAgentNoSiBtRole.after(wlsStoppingRole);
        machine.addRole(currentJavaAgentNoSiBtRole);
        gathererMap.put("agent_current_nosi_bt_wls.zip", currentJavaAgentNoSiBtRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI ACC
        JavaAgentRole currentJavaAgentNoSiAccRole = createCurrentJavaAgentNoSiAccRole(wlsRole, true, true);
        currentJavaAgentNoSiRole.after(wlsStoppingRole);
        machine.addRole(currentJavaAgentNoSiAccRole);
        gathererMap.put("agent_current_acc_wls.zip", currentJavaAgentNoSiAccRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current SI
        JavaAgentRole currentJavaAgentSiRole = createCurrentJavaAgentSiRole(wlsRole, true, true);
        currentJavaAgentSiRole.after(wlsStoppingRole);
        machine.addRole(currentJavaAgentSiRole);
        gathererMap.put("agent_current_si_wls.zip", currentJavaAgentSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // prev no SI
        JavaAgentRole prevJavaAgentNoSiRole = createPrevJavaAgentNoSiRole(wlsRole);
        prevJavaAgentNoSiRole.after(wlsStoppingRole);
        machine.addRole(prevJavaAgentNoSiRole);
        gathererMap.put("agent_prev_nosi_wls.zip", prevJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // prev SI
        JavaAgentRole prevJavaAgentSiRole = createPrevJavaAgentSiRole(wlsRole);
        prevJavaAgentSiRole.after(wlsStoppingRole);
        machine.addRole(prevJavaAgentSiRole);
        gathererMap.put("agent_prev_si_wls.zip", prevJavaAgentSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        ////////////////////////////
        // DEPLOY AGENT STATS
        ////////////////////////////

        AgentStatsRole agentStatsRole = new AgentStatsRole.Builder(machineId + "_agentStatsRoleId", tasResolver)
                .agents(currentJavaAgentSiRole, currentJavaAgentNoSiAccRole, currentJavaAgentNoSiRole,
                        null, prevJavaAgentSiRole, prevJavaAgentNoSiRole, currentJavaAgentNoSiBtRole)
                .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_wls.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("agent_wls.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
        machine.addRole(agentStatsRole);
    }

    @NotNull
    @Override
    protected Weblogic103Role deployWeblogic(TestbedMachine machine, Map<String, String> gathererMap, JavaRole javaRole) {
        Weblogic103Role wls12Role = new Weblogic103Role.Builder(machineId + WEBLOGIC_ROLE_ID, tasResolver)
                .version(WeblogicVersion.v1213generic)
                .customJvm(javaRole.getInstallDir())
                .sourcesLocation("c:\\sw\\weblogic12_sources")
                // BEA HOME (parent dir)
                .beaHome(DEFAULT_WLS12_BEA_HOME)
                // WLS HOME (Install dir)
                .installDir(DEFAULT_WLS12_INSTALL_DIR)
                .undeployOnly(undeploy)
                .predeployed(predeployed)
                .build();
        wls12Role.after(javaRole);
        machine.addRole(wls12Role);
        gathererMap.put("app_server_wls12.zip", wls12Role.getInstallDir() + "\\samples\\domains\\wl_server\\servers\\AdminServer\\logs");
        return wls12Role;
    }

    @NotNull
    @Override
    protected JavaRole deployJava(TestbedMachine machine) {
        JavaRole java8Role = new PerfJavaRole.Builder(machineId + "_java8RoleId", tasResolver)
                .version(JavaBinary.WINDOWS_64BIT_JDK_18)
                .dir("c:/sw/java8")
                .predeployed(predeployed)
                .build();
        machine.addRole(java8Role);
        return java8Role;
    }

    @Override
    public String getAppServerName() {
        return APP_SERVER_NAME;
    }

    @Override
    protected String getAppServerIdent() {
        return APP_SERVER_IDENT;
    }

    @Override
    protected String getWlsInstallDir() {
        return DEFAULT_WLS12_INSTALL_DIR;
    }
}
