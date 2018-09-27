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

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.artifact.AgentTrussVersion;
import com.ca.apm.tests.artifact.StockTraderVersion;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.machines.template.AppServerMachineAbs;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.TestbedMachine;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Machine containing Weblogic Server + StockTrader Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerWeblogicMachine extends AppServerMachineAbs {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_ASWEBLOGIC";

    public static final String APP_SERVER_NAME = "weblogic103";
    public static final String APP_SERVER_IDENT = "examplesServer"; // idetification for jstat, wmic
    public static final String APP_SERVER_NAME_STR = "weblogic";

    public static final String OS = "windows";

    public static final String DEFAULT_WLS_BEA_HOME = "c:/sw/weblogic";
    public static final String DEFAULT_WLS_INSTALL_DIR = "c:/sw/weblogic/wlserver_10.3";

    public static final String WEBLOGIC_ROLE_ID = "_wlsRoleId";
    public static final String ACC_CONTROLLER_ROLE_ID = "_accControllerRoleId";
    public static final String STOCKTRADER_ROLE_ID = "_stockTraderRoleId";
    public static final String LOGS_GATHERER_ROLE_ID = "_logsGathererRoleId";

    public static final long DEFAULT_RUN_TIME = 30L;

    protected String testResultsShare;
    protected String emHostname;

    protected boolean undeploy;
    protected boolean predeployed;

    public AppServerWeblogicMachine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerWeblogicMachine(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public TestbedMachine undeploy() {
        this.undeploy = true;
        return init(null, null);
    }

    public TestbedMachine initPredeployed(IRole dbTablesCreatedRole, IRole emRole) {
        this.predeployed = true;
        return init(dbTablesCreatedRole, emRole);
    }

    public TestbedMachine init(IRole dbTablesCreatedRole, IRole emRole) {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(DEFAULT_TEMPLATE).build();
        if (!undeploy) {
            testResultsShare = "\\\\" + tasResolver.getHostnameById(emRole.getRoleId()) + "\\" + EmMachine.SHARE_DIR_NAME;
            emHostname = tasResolver.getHostnameById(emRole.getRoleId());

        }

        Map<String, String> gathererMap = new HashMap<>();

        ///////////////////////
        // DEPLOY JAVA
        ///////////////////////

        JavaRole javaRole = deployJava(machine);

        ////////////////////////////
        // DEPLOY APP SERVER
        ////////////////////////////

        Weblogic103Role wlsRole = deployWeblogic(machine, gathererMap, javaRole);

        if (undeploy) {
            ExecutionRole deleteAgentsRole = deleteAgentsRole();
            machine.addRole(deleteAgentsRole);
        } else {

            ////////////////////////////
            // DEPLOY ACC CONTROLLER
            ////////////////////////////

            PerfAccControllerRole accControllerRole = new PerfAccControllerRole.Builder(machineId + ACC_CONTROLLER_ROLE_ID, tasResolver)
                    .version("10.2.0.19")
                    .mockMode()
                    .predeployed(predeployed)
                    .build();
            machine.addRole(accControllerRole);
            gathererMap.put("acc_wls.zip", accControllerRole.getAccControllerInstallDir() + "\\logs");

            ////////////////////////////
            // START APP SERVER
            ////////////////////////////

            String startWls = getWlsInstallDir() + "/samples/domains/wl_server/bin/startWebLogic.cmd";

            Map<String, String> wlsEnv = new HashMap<>();
            wlsEnv.put("USER_MEM_ARGS", "-XX:PermSize=512m -XX:MaxPermSize=512m");
            RunCommandFlowContext runWlsFlowContext = new RunCommandFlowContext.Builder(startWls).environment(wlsEnv)
                    .terminateOnMatch("Server started in RUNNING mode").build();
            ExecutionRole wlsExecutionRole = new ExecutionRole.Builder(machineId + "_wlsExecutionRoleId")
                    .syncCommand(runWlsFlowContext).build();

            wlsExecutionRole.after(wlsRole);

            machine.addRole(wlsExecutionRole);

            ///////////////////////////////////////////
            // KILL IE THAT WAS STARTED AUTOMATICALLY
            ///////////////////////////////////////////

            RunCommandFlowContext killIeFlowContext = new RunCommandFlowContext.Builder("taskkill")
                    .args(Arrays.asList("/F", "/FI", "IMAGENAME eq iexplore.exe")).build();
            ExecutionRole killIeRole = new ExecutionRole.Builder(machineId + "_killIeRoleId")
                    .syncCommand(killIeFlowContext).build();

            killIeRole.after(wlsExecutionRole);

            machine.addRole(killIeRole);

            ///////////////////////////////////////////
            // DEPLOY STOCKTRADER (AND LIBRARIES)
            ///////////////////////////////////////////

            StockTraderRole stockTraderRole = new StockTraderRole.Builder(machineId + STOCKTRADER_ROLE_ID, tasResolver)
                    .version(StockTraderVersion.VER_55)
                    .weblogicRole(wlsRole)
                    .weblogicUserName("weblogic")
                    .weblogicUserPassword("weblogic1")
                    .weblogicTargetServer("AdminServer")
                    .dbRole(dbTablesCreatedRole)
                    .predeployed(predeployed)
                    .build();

            stockTraderRole.after(dbTablesCreatedRole, javaRole, wlsRole, wlsExecutionRole);

            machine.addRole(stockTraderRole);

            ///////////////////////////////////////////
            // STOP APP SERVER
            ///////////////////////////////////////////

            String stopWls = getWlsInstallDir() + "/samples/domains/wl_server/bin/stopWebLogic.cmd";

            RunCommandFlowContext stopWlsFlowContext = new RunCommandFlowContext.Builder(stopWls).build();
            ExecutionRole wlsStoppingRole = new ExecutionRole.Builder(machineId + "_wlsStoppingRoleId")
                    .syncCommand(stopWlsFlowContext).build();

            wlsStoppingRole.after(stockTraderRole);

            machine.addRole(wlsStoppingRole);

            ///////////////////////////////////////////
            // DEPLOY AGENTS
            ///////////////////////////////////////////

            deployAgents(machine, gathererMap, wlsRole, wlsStoppingRole);


            ////////////////////////////
            // DEPLOY TYPEPERF
            ////////////////////////////

            // no agent
            TypeperfRole perfMonitorNoAgentRole = createPerfMonitorNoAgentRole();
            machine.addRole(perfMonitorNoAgentRole);

            // current no SI
            TypeperfRole perfMonitorCurrentJavaAgentNoSiRole = createPerfMonitorCurrentJavaAgentNoSiRole();
            machine.addRole(perfMonitorCurrentJavaAgentNoSiRole);

            // current no SI BT (only for 10.4+)
            TypeperfRole perfMonitorCurrentJavaAgentNoSiBtRole = createPerfMonitorCurrentJavaAgentNoSiBtRole();
            machine.addRole(perfMonitorCurrentJavaAgentNoSiBtRole);

            // current no SI ACC
            TypeperfRole perfMonitorCurrentJavaAgentNoSiAccRole = createPerfMonitorCurrentJavaAgentNoSiAccRole();
            machine.addRole(perfMonitorCurrentJavaAgentNoSiAccRole);

            // current SI
            TypeperfRole perfMonitorCurrentJavaAgentSiRole = createPerfMonitorCurrentJavaAgentSiRole();
            machine.addRole(perfMonitorCurrentJavaAgentSiRole);

            // prev no SI
            TypeperfRole perfMonitorPrevJavaAgentNoSiRole = createPerfMonitorPrevJavaAgentNoSiRole();
            machine.addRole(perfMonitorPrevJavaAgentNoSiRole);

            // prev SI
            TypeperfRole perfMonitorPrevJavaAgentSiRole = createPerfMonitorPrevJavaAgentSiRole();
            machine.addRole(perfMonitorPrevJavaAgentSiRole);

            ////////////////////////////
            // DEPLOY JSTAT
            ////////////////////////////

            // no agent
            JstatRole jstatNoAgentRole = createJstatNoAgentRole(javaRole);
            machine.addRole(jstatNoAgentRole);

            // current no SI
            JstatRole jstatCurrentJavaAgentNoSiRole = createJstatCurrentJavaAgentNoSiRole(javaRole);
            machine.addRole(jstatCurrentJavaAgentNoSiRole);

            // current no SI BT (only for 10.4+)
            JstatRole jstatCurrentJavaAgentNoSiBtRole = createJstatCurrentJavaAgentNoSiBtRole(javaRole);
            machine.addRole(jstatCurrentJavaAgentNoSiBtRole);

            // current no SI ACC
            JstatRole jstatCurrentJavaAgentNoSiAccRole = createJstatCurrentJavaAgentNoSiAccRole(javaRole);
            machine.addRole(jstatCurrentJavaAgentNoSiAccRole);

            // current SI
            JstatRole jstatCurrentJavaAgentSiRole = createJstatCurrentJavaAgentSiRole(javaRole);
            machine.addRole(jstatCurrentJavaAgentSiRole);

            // prev no SI
            JstatRole jstatPrevJavaAgentNoSiRole = createJstatPrevJavaAgentNoSiRole(javaRole);
            machine.addRole(jstatPrevJavaAgentNoSiRole);

            // prev SI
            JstatRole jstatPrevJavaAgentSiRole = createJstatPrevJavaAgentSiRole(javaRole);
            machine.addRole(jstatPrevJavaAgentSiRole);

            ////////////////////////////
            // DEPLOY LOGS GATHERER
            ////////////////////////////

            LogsGathererRole logsGathererRole = new LogsGathererRole.Builder(machineId + LOGS_GATHERER_ROLE_ID, tasResolver)
                    .targetDir(LOGS_GATHERER_OUTPUT_DIR)
                    .filesMapping(gathererMap)
                    .deleteSource(true)
                    .addTimestamp(true)
                    .ignoreDeletionErrors(true)
                    .ignoreEmpty(true)
                    .build();
            machine.addRole(logsGathererRole);

            ///////////////////////////////////////////
            // INCREASE NETWORK BUFFER
            ///////////////////////////////////////////

            configureNetworkBuffer(machine);

        }
        return machine;
    }

    @NotNull
    protected JavaRole deployJava(TestbedMachine machine) {
        JavaRole javaRole = new PerfJavaRole.Builder(machineId + "_javaRoleId", tasResolver)
                .version(JavaBinary.WINDOWS_64BIT_JDK_16)
                .dir("c:/sw/java6")
                .predeployed(predeployed)
                .build();
        machine.addRole(javaRole);
        return javaRole;
    }

    @NotNull
    protected Weblogic103Role deployWeblogic(TestbedMachine machine, Map<String, String> gathererMap, JavaRole javaRole) {
        Weblogic103Role wlsRole = new Weblogic103Role.Builder(machineId + WEBLOGIC_ROLE_ID, tasResolver)
                .version(WebLogicVersion.v103x86w)
                .customJvm(javaRole.getInstallDir())
                .sourcesLocation("c:\\sw\\weblogic_sources")
                // BEA HOME (parent dir)
                .beaHome(DEFAULT_WLS_BEA_HOME)
                // WLS HOME (Install dir)
                .installDir(DEFAULT_WLS_INSTALL_DIR)
                .undeployOnly(undeploy)
                .predeployed(predeployed)
                .build();
        wlsRole.after(javaRole);
        machine.addRole(wlsRole);
        gathererMap.put("app_server_wls.zip", wlsRole.getInstallDir() + "\\samples\\domains\\wl_server\\servers\\examplesServer\\logs");
        return wlsRole;
    }

    protected void deployAgents(TestbedMachine machine, Map<String, String> gathererMap, Weblogic103Role wlsRole, ExecutionRole wlsStoppingRole) {

        // current no SI
        JavaAgentRole currentJavaAgentNoSiRole = createCurrentJavaAgentNoSiRole(wlsRole);
        currentJavaAgentNoSiRole.after(wlsStoppingRole);
        machine.addRole(currentJavaAgentNoSiRole);
        gathererMap.put("agent_current_nosi_wls.zip", currentJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current SI
        JavaAgentRole currentJavaAgentSiRole = createCurrentJavaAgentSiRole(wlsRole);
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
                .agents(currentJavaAgentSiRole, null, currentJavaAgentNoSiRole,
                        null, prevJavaAgentSiRole, prevJavaAgentNoSiRole)
                .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_wls.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("agent_wls.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
        machine.addRole(agentStatsRole);
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
    protected String getAppServerNameString() {
        return APP_SERVER_NAME_STR;
    }

    @Override
    protected String getOs() {
        return OS;
    }

    @Override
    public String getTestResultsShare() {
        return testResultsShare;
    }

    @Override
    protected String getEmHostname() {
        return emHostname;
    }

    @Override
    protected Long getRuntimee() {
        return DEFAULT_RUN_TIME;
    }

    protected String getWlsInstallDir() {
        return DEFAULT_WLS_INSTALL_DIR;
    }

}
