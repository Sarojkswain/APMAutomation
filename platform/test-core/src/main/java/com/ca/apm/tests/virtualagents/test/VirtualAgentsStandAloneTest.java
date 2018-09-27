/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : GAMSA03/ SANTOSH JAMMI
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */
package com.ca.apm.tests.virtualagents.test;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.common.AssertTests;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.tests.testbed.VirtualAgentsLinuxStandaloneTestbed;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.TasTestNgTest;


public class VirtualAgentsStandAloneTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualAgentsStandAloneTest.class);
    private static final long HARVEST_TIMEOUT = 120000;
    protected String emMachineId;
    protected String agentMachineId;

    protected String emRoleId;
    protected String tomcatRoleId;
    protected String jbossRoleId;

    protected String host;
    protected int port;
    protected String emLibDir;
    protected CLWCommons clwCommon;
    protected AssertTests assertTest;

    private static final String DOMAINS_FILE_PATH = TasBuilder.LINUX_SOFTWARE_LOC
        + "em/config/domains.xml";
    private static final String USERS_FILE_PATH = TasBuilder.LINUX_SOFTWARE_LOC
        + "em/config/users.xml";
    private static final String AGENTCLUSTER_FILE_PATH = TasBuilder.LINUX_SOFTWARE_LOC
        + "em/config/agentclusters.xml";
    private static final String LOCATION_PATH =
        VirtualAgentsLinuxStandaloneTestbed.CONFIG_FILES_LOC;
    private static final String DOMAINS_FILE_PATH_NEW = LOCATION_PATH + "domains.xml";
    private static final String USERS_FILE_PATH_NEW = LOCATION_PATH + "users.xml";
    private static final String AGENTCLUSTER_FILE_PATH_NEW = LOCATION_PATH + "agentclusters.xml";
    private static final String[] users = {"Admin", "fullaccessuser", "historicalagentuser",
            "liveagentuser", "readuser", "runtraceruser", "writeuser", "Guest", "cemadmin"};
    private static final String password = "quality";
    protected String metric = "";
    protected Integer metricValue;

    /**
     * Agent Expressions
     */
    private static final String multiAgentVirtualAgentExpression =
        "(.*)\\|Custom Metric Process \\(Virtual\\)\\|virtualagent";
    private static final String tomcatVirtualAgentExpression =
        "(.*)\\|Custom Metric Process \\(Virtual\\)\\|virtualagent_tomcatmetric";
    private static final String jbossVirtualAgentExpression =
        "(.*)\\|Custom Metric Process \\(Virtual\\)\\|virtualagent_jbossmetric";
    private static final String cpuMetricVirtualAgentExpression =
        "(.*)\\|Custom Metric Process \\(Virtual\\)\\|virtualagent_cpumetric";

    /**
     * Metric Expressions
     */
    private static final String CPUProcessUtilizationAggregate =
        "CPU\\|Processor 0:Utilization % \\(aggregate\\)";
    private static final String tomcatAsyncTimeoutActiveThreads =
        "Threads\\|org\\.apache\\.tomcat\\.util\\.net\\.JIoEndpoint\\$AsyncTimeout:Active Threads";
    private static final String jbossPollerActiveThreads =
        "Threads\\|org\\.apache\\.tomcat\\.util\\.net\\.JIoEndpoint\\$Poller:Active Threads";

    /**
     * Constructor
     */
    public VirtualAgentsStandAloneTest() {
        emMachineId = VirtualAgentsLinuxStandaloneTestbed.EM_MACHINE_ID;
        agentMachineId = VirtualAgentsLinuxStandaloneTestbed.AGENT_MACHINE_ID;

        emRoleId = VirtualAgentsLinuxStandaloneTestbed.EM_ROLE_ID;
        tomcatRoleId = VirtualAgentsLinuxStandaloneTestbed.TOMCAT_ROLE_ID;
        jbossRoleId = VirtualAgentsLinuxStandaloneTestbed.JBOSS_ROLE_ID;

        host =
            envProperties
                .getMachineHostnameByRoleId(VirtualAgentsLinuxStandaloneTestbed.EM_ROLE_ID);
        port =
            Integer.parseInt(envProperties.getRolePropertyById(emRoleId,
                DeployEMFlowContext.ENV_EM_PORT));
        emLibDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        clwCommon = new CLWCommons();
        assertTest = new AssertTests();
    }


    /**
     * Before Class - runs all pre-requisite operations
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @BeforeClass(alwaysRun = true)
    public void VirtualAgentTests_Standalone() throws IOException, InterruptedException {
        replaceEMConfigFiles();
        startAll();
        //wait for Agent to connect to EM
		harvestWait();
    }

    /***********************************************************************************
     * KETSW01:
     * These test cases are :
     * Standalone EM
     * JBoss and Tomcat agents (Multiple Agents)
     * Multiple Metrics
     **********************************************************************************/

    /**
     * Standalone EM - Multiple Agents and Multiple Metrics
     * User with full permissions
     */
    @Test(groups = {"virtualagents", "smoke"})
    public void verify_ALM_295819_Standalone_MutlipleAgents_MultipleMetrics_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_ multiple metrics_user with  full   permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[1], password, multiAgentVirtualAgentExpression,
                CPUProcessUtilizationAggregate);



        // Querying Tomcat metric
        String metric2 =
            getMetricValue(users[1], password, multiAgentVirtualAgentExpression,
                tomcatAsyncTimeoutActiveThreads);

        // Querying JBoss metric
        String metric3 =
            getMetricValue(users[1], password, multiAgentVirtualAgentExpression,
                jbossPollerActiveThreads);

        LOGGER.info("Queried metrics are " + metric1 + metric2 + metric3);

        assertTest.assertMetricValue(metric1);
        assertTest.assertMetricValue(metric2);
        assertTest.assertMetricValue(metric3);
    }

    /**
     * Standalone EM - Multiple Agents and Multiple Metrics
     * User with historical_agent_control permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295803_Standalone_MutlipleAgents_MultipleMetrics_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_ multiple metrics_user with historical_agent_control  permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[2], password, multiAgentVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        // Querying Tomcat metric
        String metric2 =
            getMetricValue(users[2], password, multiAgentVirtualAgentExpression,
                tomcatAsyncTimeoutActiveThreads);

        // Querying JBoss metric
        String metric3 =
            getMetricValue(users[2], password, multiAgentVirtualAgentExpression,
                jbossPollerActiveThreads);

        LOGGER.info("Queried metrics are " + metric1 + metric2 + metric3);

        assertTest.assertMetricValue(metric1);
        assertTest.assertMetricValue(metric2);
        assertTest.assertMetricValue(metric3);
    }

    /**
     * Standalone EM - Multiple Agents and Multiple Metrics
     * User with live_agent_control permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295811_Standalone_MutlipleAgents_MultipleMetrics_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_ multiple metrics_user with live_agent_control  permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[3], password, multiAgentVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        // Querying Tomcat metric
        String metric2 =
            getMetricValue(users[3], password, multiAgentVirtualAgentExpression,
                tomcatAsyncTimeoutActiveThreads);

        // Querying JBoss metric
        String metric3 =
            getMetricValue(users[3], password, multiAgentVirtualAgentExpression,
                jbossPollerActiveThreads);

        LOGGER.info("Queried metrics are " + metric1 + metric2 + metric3);
        assertTest.assertMetricValue(metric1);
        assertTest.assertMetricValue(metric2);
        assertTest.assertMetricValue(metric3);
    }

    /**
     * Standalone EM - Multiple Agents and Multiple Metrics
     * User with read permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295779_Standalone_MutlipleAgents_MultipleMetrics_readuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_ multiple metrics_user with read permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[4], password, multiAgentVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        // Querying Tomcat metric
        String metric2 =
            getMetricValue(users[4], password, multiAgentVirtualAgentExpression,
                tomcatAsyncTimeoutActiveThreads);

        // Querying JBoss metric
        String metric3 =
            getMetricValue(users[4], password, multiAgentVirtualAgentExpression,
                jbossPollerActiveThreads);

        LOGGER.info("Queried metrics are " + metric1 + metric2 + metric3);
        assertTest.assertMetricValue(metric1);
        assertTest.assertMetricValue(metric2);
        assertTest.assertMetricValue(metric3);
    }

    /**
     * Standalone EM - Multiple Agents and Multiple Metrics
     * User with run_tracer permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295795_Standalone_MutlipleAgents_MultipleMetrics_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_ multiple metrics_user with run_tracer  permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[5], password, multiAgentVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        // Querying Tomcat metric
        String metric2 =
            getMetricValue(users[5], password, multiAgentVirtualAgentExpression,
                tomcatAsyncTimeoutActiveThreads);

        // Querying JBoss metric
        String metric3 =
            getMetricValue(users[5], password, multiAgentVirtualAgentExpression,
                jbossPollerActiveThreads);

        LOGGER.info("Queried metrics are " + metric1 + metric2 + metric3);
        assertTest.assertMetricValue(metric1);
        assertTest.assertMetricValue(metric2);
        assertTest.assertMetricValue(metric3);
    }

    /**
     * Standalone EM - Multiple Agents and Multiple Metrics
     * User with write permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295787_Standalone_MutlipleAgents_MultipleMetrics_writeuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_ multiple metrics_user with write  permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[6], password, multiAgentVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        // Querying Tomcat metric
        String metric2 =
            getMetricValue(users[6], password, multiAgentVirtualAgentExpression,
                tomcatAsyncTimeoutActiveThreads);

        // Querying JBoss metric
        String metric3 =
            getMetricValue(users[6], password, multiAgentVirtualAgentExpression,
                jbossPollerActiveThreads);

        LOGGER.info("Queried metrics are " + metric1 + metric2 + metric3);
        assertTest.assertMetricValue(metric1);
        assertTest.assertMetricValue(metric2);
        assertTest.assertMetricValue(metric3);
    }

    /**
     * Standalone EM - Multiple Agents and Single Metric
     * User with full permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295818_Standalone_MutlipleAgents_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_user with full permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[1], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Multiple Agents and Single Metric
     * User with historical_agent_control permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295802_Standalone_MutlipleAgents_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_user with historical_agent_control permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[2], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Multiple Agents and Single Metric
     * User with live_agent_control permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295810_Standalone_MutlipleAgents_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_user with live_agent_control permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[3], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Multiple Agents and Single Metric
     * User with read permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295778_Standalone_MutlipleAgents_readuser() {
        LOGGER
            .info("Test Name : AgentClustersAgentClusters_Multiple Agents_user with read permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[4], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Multiple Agents and Single Metric
     * User with run_tracer permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295794_Standalone_MutlipleAgents_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_user with run_tracer  permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[5], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Multiple Agents and Single Metric
     * User with write permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295786_Standalone_MutlipleAgents_writeuser() {
        LOGGER
            .info("Test Name : AgentClusters_Multiple Agents_user with write permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[6], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Tomcat Agent
     * User with full permissions
     */
    @Test(groups = {"virtualagents", "bat"})
    public void verify_ALM_295816_Standalone_TomcatAgent_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters_TomcatAgent_user with full permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[1], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    /**
     * Standalone EM - Tomcat Agent
     * User with live_agent_control permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295808_Standalone_TomcatAgent_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters_TomcatAgent_user with live_agent_control permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[3], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }


    /*******************************************************************************
     * JAMSA07
     * These test cases are :
     * standAlone EM
     * JBoss AGENT (Single Agent)
     * Single Metric
     ******************************************************************************/

    /**
     * standAlone EM JBoss Agent
     * User with full permissions
     */
    @Test(groups = {"virtualagents", "smoke"})
    private void verify_ALM_295817_StandAloneJBoss_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(standAlone)_JBOSS Agents_user with full access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[1], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * standAlone EM JBoss Agent
     * User with historicalagent permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295801_StandAloneJBoss_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(standAlone)_JBOSS Agents_user with historical agent access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[2], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * standAlone EM JBoss Agent
     * User with live agent permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295809_StandAloneJBoss_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(standAlone)_JBOSS Agents_user with live agent access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[3], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * standAlone EM JBoss Agent
     * User with read permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295777_StandAloneJBoss_readuser() {
        LOGGER
            .info("Test Name : AgentClusters(standAlone)_JBOSS Agents_user with read access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[4], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * standAlone EM JBoss Agent
     * User with run tracer permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295793_StandAloneJBoss_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters(standAlone)_JBOSS Agents_user with run trace access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[5], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * standAlone EM JBoss Agent
     * User with write permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295785_StandAloneJBoss_writeuser() {
        LOGGER
            .info("Test Name : AgentClusters(standAlone)_JBOSS Agents_user with write access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[6], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /*************
     * GAMSA03
     * Standalone Tomcat
     * 
     ************/

    @Test(groups = {"virtualagents", "smoke"})
    public void verify_ALM_295800_StanAloneTomcat_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters_Tomcat Agent_user with historical_agent_control permissions_Targeted to Custom Domain");
        // Querying Tomcat metric
        String metric1 =
            getMetricValue(users[2], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295825_StanAloneTomcat_readaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters_Tomcat Agent_user with read permissions_Targeted to Custom Domain");
        // Querying Tomcat metric
        String metric1 =
            getMetricValue(users[4], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295792_StanAloneTomcat_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters_Tomcat Agent_user with run_tracer permissions _Targeted to Custom Domain");
        // Querying Tomcat metric
        String metric1 =
            getMetricValue(users[5], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295784_StanAloneTomcat_writeaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters_Tomcat Agent_user with write permissions_Targeted to Custom Domain");
        // Querying Tomcat metric
        String metric1 =
            getMetricValue(users[6], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    private void replaceEMConfigFiles() throws IOException {
        FileModifierFlowContext moveFlowContext =
            new FileModifierFlowContext.Builder()
                .move(DOMAINS_FILE_PATH, DOMAINS_FILE_PATH + ".orig")
                .move(USERS_FILE_PATH, USERS_FILE_PATH + ".orig")
                .move(AGENTCLUSTER_FILE_PATH, AGENTCLUSTER_FILE_PATH + ".orig").build();
        runFlowByMachineId(emMachineId, FileModifierFlow.class, moveFlowContext);
        FileModifierFlowContext copyFlowContext =
            new FileModifierFlowContext.Builder().copy(DOMAINS_FILE_PATH_NEW, DOMAINS_FILE_PATH)
                .copy(USERS_FILE_PATH_NEW, USERS_FILE_PATH)
                .copy(AGENTCLUSTER_FILE_PATH_NEW, AGENTCLUSTER_FILE_PATH).build();
        runFlowByMachineId(emMachineId, FileModifierFlow.class, copyFlowContext);
    }

    private void startAll() {
		runSerializedCommandFlowFromRole(jbossRoleId, JbossRole.ENV_JBOSS_START);
        runSerializedCommandFlowFromRole(tomcatRoleId, TomcatRole.ENV_TOMCAT_START);
		runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_EM);
    }

    private void harvestWait() throws InterruptedException {
        LOGGER.info("Harvesting crops.");
        Thread.sleep(HARVEST_TIMEOUT);
        LOGGER.info("Crops harvested.");
    }


    /**
     * Gets the metric value for the requested agents and metrics
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @return
     */
    private String getMetricValue(String user, String password, String agentExpression,
        String metricExpression) {

        return clwCommon.getLatestMetricValue(user, password, agentExpression, metricExpression,
            host, port, emLibDir);

    }

    private void stopAll() {

        EmUtils emUtils = utilities.createEmUtils();
        ClwUtils clwUtils =
            utilities.createClwUtils(VirtualAgentsLinuxStandaloneTestbed.EM_ROLE_ID);
        // Stop EM
        emUtils.stopLocalEmWithTimeoutSec(clwUtils.getClwRunner(),
            VirtualAgentsLinuxStandaloneTestbed.EM_ROLE_ID, 240);
        // Stop Tomcat
        runSerializedCommandFlowFromRole(VirtualAgentsLinuxStandaloneTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_STOP);
        // Stop JBoss
        runSerializedCommandFlowFromRole(VirtualAgentsLinuxStandaloneTestbed.JBOSS_ROLE_ID,
            JbossRole.ENV_JBOSS_STOP);

    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        stopAll();
    }

}
