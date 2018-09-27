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
import com.ca.apm.tests.artifact.NerdDinnerVersion;
import com.ca.apm.tests.artifact.NetAgentTrussVersion;
import com.ca.apm.tests.artifact.NetStockTraderVersion;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.machines.template.NetAppServerMachineAbs;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Machine containing IIS Server + NET StockTrader Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerIis104Machine extends NetAppServerMachineAbs {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_ASIIS";

    public static final String APP_SERVER_NAME = "iis75";

    public static final String IIS_ROLE_ID = "_iisRoleId";
    public static final String NET_STOCKTRADER_ROLE_ID = "_netStockTraderRoleId";
    public static final String NERD_DINNER_4_ROLE_ID = "_nerdDinner4RoleId";
    public static final String NERD_DINNER_5_ROLE_ID = "_nerdDinner5RoleId";

    public static final String LOGS_GATHERER_ROLE_ID = "_logsGathererRoleId";

    public static final long DEFAULT_RUN_TIME = 30L;

    protected String testResultsShare;
    protected String emHostname;

    protected boolean undeploy;
    protected boolean predeployed;

    public AppServerIis104Machine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerIis104Machine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion,
                                  String agentDllCurrentVersion, String agentDllPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, agentDllCurrentVersion, agentDllPrevVersion, sharePassword);
    }

    public AppServerIis104Machine(String machineId, ITasResolver tasResolver, NetAgentTrussVersion agentCurrentVersion, NetAgentTrussVersion agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public TestbedMachine undeploy() {
        this.undeploy = true;
        return init(null, null, null);
    }

    public TestbedMachine initPredeployed(MsSqlDbRole dbRole, IRole dbTablesCreatedRole, IRole emRole) {
        this.predeployed = true;
        return init(dbRole, dbTablesCreatedRole, emRole);
    }

    public TestbedMachine init(MsSqlDbRole dbRole, IRole dbTablesCreatedRole, IRole emRole) {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(DEFAULT_TEMPLATE).build();

        Map<String, String> gathererMap = new HashMap<>();

        if (!undeploy) {
            testResultsShare = "\\\\" + tasResolver.getHostnameById(emRole.getRoleId()) + "\\" + EmMachine.SHARE_DIR_NAME;
            emHostname = tasResolver.getHostnameById(emRole.getRoleId());

        }

        if (undeploy) {
            NetStockTraderRole netStockTraderRole = new NetStockTraderRole.Builder(machineId + NET_STOCKTRADER_ROLE_ID, tasResolver)
                    .undeployOnly()
                    .build();
            machine.addRole(netStockTraderRole);

            ExecutionRole deleteAgentsRole = deleteAgentsRole();
            machine.addRole(deleteAgentsRole);
        } else {
            ////////////////////////////
            // ENABLE IIS
            ////////////////////////////
            IisRole iisRole = new IisRole.Builder(machineId + IIS_ROLE_ID, tasResolver)
                    .predeployed(predeployed)
                    //.undeployOnly(undeploy) // todo needs machine restart, solve first, then uncomment
                    .build();
            machine.addRole(iisRole);
            gathererMap.put("app_server_iis.zip", "C:\\inetpub\\logs\\LogFiles");

            ////////////////////////////
            // ENABLE .NET
            ////////////////////////////

            RunCommandFlowContext dotNetFlowContext = new RunCommandFlowContext
                    .Builder("c:/Windows/Microsoft.NET/Framework64/v4.0.30319/aspnet_regiis.exe")
                    .args(Arrays.asList("-i")).build();
            ExecutionRole dotNetExecutionRole = new ExecutionRole.Builder(machineId + "_dotNetExecutionRoleId")
                    .syncCommand(dotNetFlowContext).build();

            dotNetExecutionRole.after(iisRole);
            machine.addRole(dotNetExecutionRole);

            ////////////////////////////
            // DEPLOY NET STOCKTRADER
            ////////////////////////////

            NetStockTraderRole netStockTraderRole = new NetStockTraderRole.Builder(machineId + NET_STOCKTRADER_ROLE_ID, tasResolver)
                    .version(NetStockTraderVersion.VER_55)
                    .dbRole(dbTablesCreatedRole)
                    .predeployed(predeployed)
                    .build();

            netStockTraderRole.after(dbTablesCreatedRole, iisRole, dotNetExecutionRole);
            machine.addRole(netStockTraderRole);

            ////////////////////////////
            // DEPLOY NERD DINNER
            ////////////////////////////

            NerdDinnerRole nerdDinner4Role = new NerdDinnerRole.Builder(machineId + NERD_DINNER_4_ROLE_ID, tasResolver)
                    .version("10.5.0-SNAPSHOT", NerdDinnerVersion.MvcVersion.VER_4) // TODO change to release version
                    .dbRole(dbRole)
                    .appName("NerdDinnerMVC4")
                    .appPort(9090)
                    .deploySourcesLocation("C:\\sw\\nerdDinner4")
                    .predeployed(predeployed)
                    .build();

            nerdDinner4Role.after(dbTablesCreatedRole, iisRole, dotNetExecutionRole);
            machine.addRole(nerdDinner4Role);

            NerdDinnerRole nerdDinner5Role = new NerdDinnerRole.Builder(machineId + NERD_DINNER_5_ROLE_ID, tasResolver)
                    .version("10.5.0-SNAPSHOT", NerdDinnerVersion.MvcVersion.VER_5) // TODO change to release version
                    .dbRole(dbRole)
                    .appName("NerdDinnerMVC5")
                    .appPort(9091)
                    .deploySourcesLocation("C:\\sw\\nerdDinner5")
                    .predeployed(predeployed)
                    .build();

            nerdDinner5Role.after(dbTablesCreatedRole, iisRole, dotNetExecutionRole);
            machine.addRole(nerdDinner5Role);

            ////////////////////////////
            // DEPLOY GACUTIL
            ////////////////////////////

            GacutilRole gacutilRole = new GacutilRole.Builder(machineId + "_gacutilRoleId", tasResolver).build();

            machine.addRole(gacutilRole);

            ////////////////////////////
            // DEPLOY AGENTS
            ////////////////////////////

            // current no DI
            NetAgentRole currentNetAgentNoDiRole = createCurrentNetAgentNoDiRole(gacutilRole);
            currentNetAgentNoDiRole.after(gacutilRole);
            machine.addRole(currentNetAgentNoDiRole);
            gathererMap.put("agent_current_nodi_iis.zip", currentNetAgentNoDiRole.getDeploySourcesLocation() + "\\wily\\logs");

            // current no DI MVC4 (only for 10.4+)
//            NetAgentRole currentNetAgentNoDiMvc4Role = createCurrentNetAgentNoDiMvc4Role(gacutilRole);
//            currentNetAgentNoDiMvc4Role.after(gacutilRole);
//            machine.addRole(currentNetAgentNoDiMvc4Role);
//            gathererMap.put("agent_current_nodi_mvc4_iis.zip", currentNetAgentNoDiMvc4Role.getDeploySourcesLocation() + "\\wily\\logs");

            // current no DI BT (only for 10.4+)
            NetAgentRole currentNetAgentNoDiBtRole = createCurrentNetAgentNoDiBtRole(gacutilRole);
            currentNetAgentNoDiBtRole.after(gacutilRole);
            machine.addRole(currentNetAgentNoDiBtRole);
            gathererMap.put("agent_current_nodi_bt_iis.zip", currentNetAgentNoDiBtRole.getDeploySourcesLocation() + "\\wily\\logs");

            // current DI
            NetAgentRole currentNetAgentDiRole = createCurrentNetAgentDiRole(gacutilRole);
            currentNetAgentDiRole.after(gacutilRole);
            machine.addRole(currentNetAgentDiRole);
            gathererMap.put("agent_current_di_iis.zip", currentNetAgentDiRole.getDeploySourcesLocation() + "\\wily\\logs");

            // prev no DI
            NetAgentRole prevNetAgentNoDiRole = createPrevNetAgentNoDiRole(gacutilRole);
            prevNetAgentNoDiRole.after(gacutilRole);
            machine.addRole(prevNetAgentNoDiRole);
            gathererMap.put("agent_prev_nodi_iis.zip", prevNetAgentNoDiRole.getDeploySourcesLocation() + "\\wily\\logs");

            // prev DI
            NetAgentRole prevNetAgentDiRole = createPrevNetAgentDiRole(gacutilRole);
            prevNetAgentDiRole.after(gacutilRole);
            machine.addRole(prevNetAgentDiRole);
            gathererMap.put("agent_prev_di_iis.zip", prevNetAgentDiRole.getDeploySourcesLocation() + "\\wily\\logs");

            ////////////////////////////
            // DEPLOY AGENT STATS
            ////////////////////////////

            AgentStatsRole agentStatsRole = new AgentStatsRole.Builder(machineId + "_agentStatsRoleId", tasResolver)
                    .agents(currentNetAgentDiRole, null, currentNetAgentNoDiRole,
                            null, prevNetAgentNoDiRole, prevNetAgentDiRole, currentNetAgentNoDiBtRole /*, currentNetAgentNoDiMvc4Role */)
                    .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_iis.csv")
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("agent_iis.csv")
                    .copyResultsDestinationPassword(sharePassword)
                    .build();
            machine.addRole(agentStatsRole);

            ////////////////////////////
            // DEPLOY TYPEPERF CPU
            ////////////////////////////

            // current no agent
            TypeperfRole perfMonitorCpuNoAgentRole = createPerfMonitorCpuNoAgentRole();
            machine.addRole(perfMonitorCpuNoAgentRole);

            // current no DI
            TypeperfRole perfMonitorCpuCurrentNetAgentNoDiRole = createPerfMonitorCpuCurrentNetAgentNoDiRole();
            machine.addRole(perfMonitorCpuCurrentNetAgentNoDiRole);

            // current no DI MVC4 (only for 10.4+)
//            TypeperfRole perfMonitorCpuCurrentNetAgentNoDiMvc4Role = createPerfMonitorCpuCurrentNetAgentNoDiMvc4Role();
//            machine.addRole(perfMonitorCpuCurrentNetAgentNoDiMvc4Role);

            // current no DI BT (only for 10.4+)
            TypeperfRole perfMonitorCpuCurrentNetAgentNoDiBtRole = createPerfMonitorCpuCurrentNetAgentNoDiBtRole();
            machine.addRole(perfMonitorCpuCurrentNetAgentNoDiBtRole);

            // current DI
            TypeperfRole perfMonitorCpuCurrentNetAgentDiRole = createPerfMonitorCpuCurrentNetAgentDiRole();
            machine.addRole(perfMonitorCpuCurrentNetAgentDiRole);

            // prev no DI
            TypeperfRole perfMonitorCpuPrevNetAgentNoDiRole = createPerfMonitorCpuPrevNetAgentNoDiRole();
            machine.addRole(perfMonitorCpuPrevNetAgentNoDiRole);

            // prev DI
            TypeperfRole perfMonitorCpuPrevNetAgentDiRole = createPerfMonitorCpuPrevNetAgentDiRole();
            machine.addRole(perfMonitorCpuPrevNetAgentDiRole);

            ////////////////////////////
            // DEPLOY TYPEPERF MEMORY
            ////////////////////////////

            // current no agent
            TypeperfRole perfMonitorMemNoAgentRole = createPerfMonitorMemNoAgentRole();
            machine.addRole(perfMonitorMemNoAgentRole);

            // current no DI
            TypeperfRole perfMonitorMemCurrentNetAgentNoDiRole = createPerfMonitorMemCurrentNetAgentNoDiRole();
            machine.addRole(perfMonitorMemCurrentNetAgentNoDiRole);

            // current no DI MVC4 (only for 10.4+)
//            TypeperfRole perfMonitorMemCurrentNetAgentNoDiMvc4Role = createPerfMonitorMemCurrentNetAgentNoDiMvc4Role();
//            machine.addRole(perfMonitorMemCurrentNetAgentNoDiMvc4Role);

            // current no DI BT (only for 10.4+)
            TypeperfRole perfMonitorMemCurrentNetAgentNoDiBtRole = createPerfMonitorMemCurrentNetAgentNoDiBtRole();
            machine.addRole(perfMonitorMemCurrentNetAgentNoDiBtRole);

            // current DI
            TypeperfRole perfMonitorMemCurrentNetAgentDiRole = createPerfMonitorMemCurrentNetAgentDiRole();
            machine.addRole(perfMonitorMemCurrentNetAgentDiRole);

            // prev no DI
            TypeperfRole perfMonitorMemPrevNetAgentNoDiRole = createPerfMonitorMemPrevNetAgentNoDiRole();
            machine.addRole(perfMonitorMemPrevNetAgentNoDiRole);

            // prev DI
            TypeperfRole perfMonitorMemPrevNetAgentDiRole = createPerfMonitorMemPrevNetAgentDiRole();
            machine.addRole(perfMonitorMemPrevNetAgentDiRole);

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

    @Override
    public String getAppServerName() {
        return APP_SERVER_NAME;
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

}
