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
import com.ca.apm.tests.artifact.OjdbcVersion;
import com.ca.apm.tests.artifact.Websphere85Version;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.machines.template.AppServerMachineAbs;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * Machine containing Websphere Server + Trade6 Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerWebsphereMachine extends AppServerMachineAbs {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_ASWEBSPHERE";

    public static final String APP_SERVER_NAME = "websphere85";
    public static final String APP_SERVER_IDENT = "dont_need_uses_jmx"; // No Jstat, we don't need this value
    public static final String APP_SERVER_NAME_STR = "websphere";

    public static final String OS = "windows";

    public static final String DEFAULT_WAS_INSTALL_PATH = "c:/sw/websphere";

    public static final String JMX_MONITOR_INSTALL_DIR = "c:/sw/jmxMonitor";
    public static final String JMX_MONITOR_OUTPUT_DIR = "C:\\automation\\test_results";

    public static final String WEBSPHERE_ROLE_ID = "_wasRoleId";
    public static final String ACC_CONTROLLER_ROLE_ID = "_accControllerRoleId";
    public static final String TRADE6_ROLE_ID = "_trade6RoleId";
    public static final String LOGS_GATHERER_ROLE_ID = "_logsGathererRoleId";

    public static final long DEFAULT_RUN_TIME = 30L;

    protected String testResultsShare;
    protected String emHostname;

    protected boolean undeploy;
    protected boolean predeployed;

    public AppServerWebsphereMachine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerWebsphereMachine(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
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

        ////////////////////////////
        // DEPLOY APP SERVER
        ////////////////////////////

        Websphere85Role wasRole = new Websphere85Role.Builder(machineId + "_wasRoleId", tasResolver)
                .version(Websphere85Version.VER_85_JAVA7)
                .installWasLocation(DEFAULT_WAS_INSTALL_PATH + "\\websphere85")
                .installManagerLocation(DEFAULT_WAS_INSTALL_PATH + "\\websphere85_manager")
                .imSharedLocation(DEFAULT_WAS_INSTALL_PATH + "\\IMShared")
                .hostName("localhost") // todo put this machine hostname
                .undeployOnly(undeploy)
                .predeployed(predeployed)
                .build();
        machine.addRole(wasRole);
        gathererMap.put("app_server_was.zip", wasRole.getProfilePath() + "\\logs");

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
            gathererMap.put("acc_was.zip", accControllerRole.getAccControllerInstallDir() + "\\logs");


            ////////////////////////////
            // DEPLOY OJDBC
            ////////////////////////////

            OjdbcRole ojdbcRole = new OjdbcRole.Builder(machineId + "_ojdbcRoleId", tasResolver)
                    .version(OjdbcVersion.VER_6).build();

            machine.addRole(ojdbcRole);

            ////////////////////////////
            // START APP SERVER
            ////////////////////////////

            ExecutionRole wasExecutionRole = new ExecutionRole.Builder(machineId + "_wasExecutionRoleId")
                    .syncCommand(wasRole.getStartFlowContext()).build();

            wasExecutionRole.after(wasRole, ojdbcRole);
            machine.addRole(wasExecutionRole);

            ////////////////////////////
            // DEPLOY TRADE6
            ////////////////////////////

            Trade6Role trade6Role = new Trade6Role.Builder(machineId + TRADE6_ROLE_ID, tasResolver)
                    .dbRole(dbTablesCreatedRole)
                    .ojdbcRole(ojdbcRole)
                    .websphereRole(wasRole)
                    .predeployed(predeployed)
                    .build();

            trade6Role.after(dbTablesCreatedRole, wasRole, ojdbcRole, wasExecutionRole);
            machine.addRole(trade6Role);

            ////////////////////////////
            // STOP APP SERVER
            ////////////////////////////

            ExecutionRole wasStoppingRole = new ExecutionRole.Builder(machineId + "_wasStoppingRoleId")
                    .syncCommand(wasRole.getStopFlowContext()).build();

            wasStoppingRole.after(trade6Role);
            machine.addRole(wasStoppingRole);

            ////////////////////////////
            // DEPLOY AGENTS
            ////////////////////////////

            deployAgents(machine, gathererMap, wasRole, wasStoppingRole);


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
            // DEPLOY JMX MONITOR
            ////////////////////////////

            // no agent
            JmxMonitorRole jmxMonitorNoAgentRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_NO_AGENT_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memNoAgent.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".noagent.csv")
                    .build();
            machine.addRole(jmxMonitorNoAgentRole);

            // current no SI
            JmxMonitorRole jmxMonitorCurrentJavaAgentNoSiRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_CURRENT_NO_SI_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memCurrentJavaAgentNoSi.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.CURRENT.csv")
                    .build();
            machine.addRole(jmxMonitorCurrentJavaAgentNoSiRole);

            // current no SI BT (only for 10.4+)
            JmxMonitorRole jmxMonitorCurrentJavaAgentNoSiBtRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_CURRENT_NO_SI_BT_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memCurrentJavaAgentNoSiBt.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.bt.CURRENT.csv")
                    .build();
            machine.addRole(jmxMonitorCurrentJavaAgentNoSiBtRole);

            // current no SI ACC
            JmxMonitorRole jmxMonitorCurrentJavaAgentNoSiAccRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_CURRENT_NO_SI_ACC_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memCurrentJavaAgentNoSiAcc.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".acc.CURRENT.csv")
                    .build();
            machine.addRole(jmxMonitorCurrentJavaAgentNoSiAccRole);

            // current SI
            JmxMonitorRole jmxMonitorCurrentJavaAgentSiRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_CURRENT_SI_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memCurrentJavaAgentSi.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".si.CURRENT.csv")
                    .build();
            machine.addRole(jmxMonitorCurrentJavaAgentSiRole);

            // prev no SI
            JmxMonitorRole jmxMonitorPrevJavaAgentNoSiRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_PREV_NO_SI_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memPrevJavaAgentNoSi.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.PREV.csv")
                    .build();
            machine.addRole(jmxMonitorPrevJavaAgentNoSiRole);

            // prev SI
            JmxMonitorRole jmxMonitorPrevJavaAgentSiRole = new JmxMonitorRole
                    .Builder(machineId + JMXMON_PREV_SI_ROLE_ID, tasResolver)
                    .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memPrevJavaAgentSi.csv").runTime(DEFAULT_RUN_TIME)
                    .copyResultsDestinationDir(getTestResultsShare())
                    .copyResultsDestinationFileName("mem_" + getAppServerName() + ".si.PREV.csv")
                    .build();
            machine.addRole(jmxMonitorPrevJavaAgentSiRole);

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

    protected void deployAgents(TestbedMachine machine, Map<String, String> gathererMap, Websphere85Role wasRole, ExecutionRole wasStoppingRole) {

        // current no SI
        JavaAgentRole currentJavaAgentNoSiRole = createCurrentJavaAgentNoSiRole(wasRole);
        currentJavaAgentNoSiRole.after(wasStoppingRole);
        machine.addRole(currentJavaAgentNoSiRole);
        gathererMap.put("agent_current_nosi_was.zip", currentJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI ACC
        JavaAgentRole currentJavaAgentNoSiAccRole = createCurrentJavaAgentNoSiAccRole(wasRole);
        currentJavaAgentNoSiRole.after(wasStoppingRole);
        machine.addRole(currentJavaAgentNoSiAccRole);
        gathererMap.put("agent_current_acc_was.zip", currentJavaAgentNoSiAccRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current SI
        JavaAgentRole currentJavaAgentSiRole = createCurrentJavaAgentSiRole(wasRole);
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
                        null, prevJavaAgentSiRole, prevJavaAgentNoSiRole)
                .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_was.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("agent_was.csv")
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

}
