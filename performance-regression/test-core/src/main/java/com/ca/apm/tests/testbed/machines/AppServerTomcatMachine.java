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
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.machines.template.AppServerMachineAbs;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * Machine containing Tomcat Server + Konakart Web Application
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class AppServerTomcatMachine extends AppServerMachineAbs {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_ASTOMCAT";

    public static final String APP_SERVER_NAME = "tomcat8";
    public static final String APP_SERVER_IDENT = "tomcat-juli";
    public static final String APP_SERVER_NAME_STR = "tomcat";

    public static final String OS = "windows";

    public static final String DEFAULT_TOMCAT_INSTALL_PATH = "c:/sw/tomcat";

    public static final String TOMCAT_ROLE_ID = "_tomcatRoleId";
    public static final String ACC_CONTROLLER_ROLE_ID = "_accControllerRoleId";
    public static final String KONAKART_ROLE_ID = "_konakartRoleId";
    public static final String LOGS_GATHERER_ROLE_ID = "_logsGathererRoleId";

    public static final long DEFAULT_RUN_TIME = 30L;

    protected String testResultsShare;
    protected String emHostname;

    protected boolean undeploy;
    protected boolean predeployed;

    public AppServerTomcatMachine(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        super(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, sharePassword);
    }

    public AppServerTomcatMachine(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
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

        Map<String, String> gathererMap = new HashMap<>(); // map for LOGS Gatherer Role

        ///////////////////////
        // DEPLOY JAVA
        ///////////////////////

        JavaRole javaRole = new PerfJavaRole.Builder(machineId + "_javaRoleId", tasResolver)
                .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51)
                .dir("c:/sw/java8")
                .predeployed(predeployed)
                .build();
        machine.addRole(javaRole);

        ///////////////////////
        // DEPLOY APP SERVER
        ///////////////////////

        TomcatRole tomcatRole = new PerfTomcatRole.Builder(machineId + TOMCAT_ROLE_ID, tasResolver)
                .tomcatVersion(TomcatVersion.v80, com.ca.apm.tests.artifact.TomcatVersion.v80.getUnpackDir())
                .installDir(DEFAULT_TOMCAT_INSTALL_PATH)
                .customJava(javaRole)
                .undeployOnly(undeploy)
                .predeployed(predeployed)
                .build();
        tomcatRole.after(javaRole);
        machine.addRole(tomcatRole);

        if (undeploy) {
            ExecutionRole deleteAgentsRole = deleteAgentsRole();
            machine.addRole(deleteAgentsRole);
        } else {
            ///////////////////////
            // DEPLOY ACC CONTROLLER
            ///////////////////////

            PerfAccControllerRole accControllerRole = new PerfAccControllerRole.Builder(machineId + ACC_CONTROLLER_ROLE_ID, tasResolver)
                    .version("10.2.0.19")
                    .mockMode()
                    .predeployed(predeployed)
                    .build();
            machine.addRole(accControllerRole);
            gathererMap.put("acc_tomcat.zip", accControllerRole.getAccControllerInstallDir() + "\\logs");

            ///////////////////////
            // DEPLOY KONAKART
            ///////////////////////

            KonakartRole konakartRole = new KonakartRole.Builder(machineId + KONAKART_ROLE_ID, tasResolver)
                    .dbRole(dbTablesCreatedRole)
                    .tomcatRole(tomcatRole)
                    .javaRole(javaRole)
                    .predeployed(predeployed)
                    .build();

            konakartRole.after(dbTablesCreatedRole, javaRole, tomcatRole);
            machine.addRole(konakartRole);
            gathererMap.put("app_server_tomcat.zip", tomcatRole.getInstallDir() + "\\logs");

            ////////////////////////////
            // DEPLOY AGENTS
            ////////////////////////////

            deployAgents(machine, gathererMap, tomcatRole, konakartRole);

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

            // current no SI BRTM
            TypeperfRole perfMonitorCurrentJavaAgentNoSiBrtmRole = createPerfMonitorCurrentJavaAgentNoSiBrtmRole();
            machine.addRole(perfMonitorCurrentJavaAgentNoSiBrtmRole);

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

            // current no SI BRTM
            JstatRole jstatCurrentJavaAgentNoSiBrtmRole = createJstatCurrentJavaAgentNoSiBrtmRole(javaRole);
            machine.addRole(jstatCurrentJavaAgentNoSiBrtmRole);

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

    protected void deployAgents(TestbedMachine machine, Map<String, String> gathererMap, TomcatRole tomcatRole, KonakartRole konakartRole) {

        // current no SI
        JavaAgentRole currentJavaAgentNoSiRole = createCurrentJavaAgentNoSiRole(tomcatRole);
        currentJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiRole);
        gathererMap.put("agent_current_nosi_tomcat.zip", currentJavaAgentNoSiRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI ACC
        JavaAgentRole currentJavaAgentNoSiAccRole = createCurrentJavaAgentNoSiAccRole(tomcatRole);
        currentJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiAccRole);
        gathererMap.put("agent_current_acc_tomcat.zip", currentJavaAgentNoSiAccRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current no SI BRTM
        JavaAgentRole currentJavaAgentNoSiBrtmRole = createCurrentJavaAgentNoSiBrtmRole(tomcatRole);
        currentJavaAgentNoSiRole.after(konakartRole);
        machine.addRole(currentJavaAgentNoSiBrtmRole);
        gathererMap.put("agent_current_brtm_tomcat.zip", currentJavaAgentNoSiBrtmRole.getDeploySourcesLocation() + "\\wily\\logs");

        // current SI
        JavaAgentRole currentJavaAgentSiRole = createCurrentJavaAgentSiRole(tomcatRole);
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
                        currentJavaAgentNoSiBrtmRole, prevJavaAgentSiRole, prevJavaAgentNoSiRole)
                .outputFile(AGENT_STAT_OUTPUT_DIR + "\\agent_tomcat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("agent_tomcat.csv")
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
