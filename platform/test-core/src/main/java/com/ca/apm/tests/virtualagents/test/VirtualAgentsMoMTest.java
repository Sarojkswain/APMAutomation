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
 * Author : GAMSA03/ SANTOSH GAMPA
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */

package com.ca.apm.tests.virtualagents.test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlow;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.common.AssertTests;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.testbed.VirtualAgentsWindowsClusterTestbed;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.TasTestNgTest;

public class VirtualAgentsMoMTest extends TasTestNgTest {

    CLWCommons clwCommon = new CLWCommons();
    TestUtils utility = new TestUtils();
    AssertTests assertTest = new AssertTests();

    String metric = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualAgentsMoMTest.class);
    private static final String DOMAINS_FILE_PATH = TasBuilder.WIN_SOFTWARE_LOC
        + "em/config/domains.xml";
    private static final String USERS_FILE_PATH = TasBuilder.WIN_SOFTWARE_LOC
        + "em/config/users.xml";
    private static final String AGENTCLUSTER_FILE_PATH = TasBuilder.WIN_SOFTWARE_LOC
        + "em/config/agentclusters.xml";
    private static final String LOCATION_PATH = VirtualAgentsWindowsClusterTestbed.CONFIG_FILES_LOC;
    private static final String DOMAINS_FILE_PATH_NEW = LOCATION_PATH + "domains.xml";
    private static final String USERS_FILE_PATH_NEW = LOCATION_PATH + "users.xml";
    private static final String AGENTCLUSTER_FILE_PATH_NEW = LOCATION_PATH + "agentclusters.xml";

    protected String momMachineId;
    protected String agentMachineId;

    protected String collector1MachineId;
    protected String collector2MachineId;
    protected String collector1RoleId;
    protected String collector2RoleId;

    protected String momRoleId;
    protected String tomcatRoleId;
    protected String jbossRoleId;
    protected String qaAppTomcatRoleId;
    protected String qaAppjbossRoleId;
    protected String tomcatAgentRoleId;
    protected String jbossAgentRoleId;

    protected String host;
    protected String agentHost;
    protected int port;
    protected String emLibDir;
    private static final String[] users = {"Admin", "fullaccessuser", "historicalagentuser",
            "liveagentuser", "readuser", "runtraceruser", "writeuser", "Guest", "cemadmin"};
    private static final String password = "quality";

    protected Integer metricValue;

    /**
     * Agent Expressions
     */
    private static final String tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
    private static final String jbossAgentExpression = "(.*)\\|JBoss\\|JBoss Agent";
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
    private static final String responsesPerIntervalRootContext =
        "Frontends\\|Apps\\|rootContext:Responses Per Interval";
    private static final String tomcatAsyncTimeoutActiveThreads =
        "Threads\\|org\\.apache\\.tomcat\\.util\\.net\\.JIoEndpoint\\$AsyncTimeout:Active Threads";
    private static final String jbossPollerActiveThreads =
        "Threads\\|org\\.apache\\.tomcat\\.util\\.net\\.JIoEndpoint\\$Poller:Active Threads";
    private static final String cpuMetricExpression =  "CPU\\|(.*)";
    private static final String frontendsMetricExpression =  "Frontends\\|(.*)";
    private static final String threadsMetricExpression =  "Threads\\|(.*)";

    /***
     * Constructor for Initialization
     */
    public VirtualAgentsMoMTest() {

        momMachineId = VirtualAgentsWindowsClusterTestbed.MOM_MACHINE_ID;

        momRoleId = VirtualAgentsWindowsClusterTestbed.MOM_ROLE_ID;

        collector1MachineId = VirtualAgentsWindowsClusterTestbed.COLLECTOR1_MACHINE_ID;
        collector2MachineId = VirtualAgentsWindowsClusterTestbed.COLLECTOR2_MACHINE_ID;

        collector1RoleId = VirtualAgentsWindowsClusterTestbed.COLLECTOR1_ROLE_ID;
        collector2RoleId = VirtualAgentsWindowsClusterTestbed.COLLECTOR2_ROLE_ID;

        tomcatRoleId = VirtualAgentsWindowsClusterTestbed.TOMCAT_ROLE_ID;
        jbossRoleId = VirtualAgentsWindowsClusterTestbed.JBOSS_ROLE_ID;

        qaAppTomcatRoleId = VirtualAgentsWindowsClusterTestbed.QA_APP_TOMCAT_ROLE_ID;
        qaAppjbossRoleId = VirtualAgentsWindowsClusterTestbed.QA_APP_JBOSS_ROLE_ID;

        tomcatAgentRoleId = VirtualAgentsWindowsClusterTestbed.TOMCAT_AGENT_ROLE_ID;
        jbossAgentRoleId = VirtualAgentsWindowsClusterTestbed.JBOSS_AGENT_ROLE_ID;

        host =
            envProperties
                .getMachineHostnameByRoleId(VirtualAgentsWindowsClusterTestbed.MOM_ROLE_ID);
        port =
            Integer.parseInt(envProperties.getRolePropertyById(momRoleId,
                DeployEMFlowContext.ENV_EM_PORT));
        emLibDir = envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);

        agentHost =
            envProperties
                .getMachineHostnameByRoleId(VirtualAgentsWindowsClusterTestbed.TOMCAT_AGENT_ROLE_ID);

    }

    /**
     * Replace EM Config files - domains.xml users.xml agentcluster.xml
     */
    public void replaceEMConfigFiles() {
        FileModifierFlowContext moveFlowContext =
            new FileModifierFlowContext.Builder()
                .move(DOMAINS_FILE_PATH, DOMAINS_FILE_PATH + ".orig")
                .move(USERS_FILE_PATH, USERS_FILE_PATH + ".orig")
                .move(AGENTCLUSTER_FILE_PATH, AGENTCLUSTER_FILE_PATH + ".orig").build();
        runFlowByMachineId(momMachineId, FileModifierFlow.class, moveFlowContext);

        FileModifierFlowContext copyFlowContext =
            new FileModifierFlowContext.Builder().copy(DOMAINS_FILE_PATH_NEW, DOMAINS_FILE_PATH)
                .copy(USERS_FILE_PATH_NEW, USERS_FILE_PATH)
                .copy(AGENTCLUSTER_FILE_PATH_NEW, AGENTCLUSTER_FILE_PATH).build();
        runFlowByMachineId(momMachineId, FileModifierFlow.class, copyFlowContext);
    }


    @BeforeClass(alwaysRun = true)
    public void VirtualAgentTests_MOM() {

        replaceEMConfigFiles();

        /**
         * This method is to start all services
         * MOM
         * COLLECTORS
         * TOMCAT
         * JBOSS
         */
        startAll();
		//wait for Agent to connect to EM
    }



    /**
     * Aggregate responses per interval using calendar object
     */
    @Test(groups = {"virtualagents", "bat"})
    private void verify_ALM_453770_MetricAggregation_ResponsePerInterval() {
        int maxOfAgentMetrics;
        Set<String> clusterFrontendsMetrics;
        Set<String> jbossFrontendsMetrics;
        Set<String> tomcatFrontendsMetrics;
        int i;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        /**
         * Access the web app to generate the frontends Metric node
         * Verify in loop until the node appears.
         */

        utility.connectToURL("http://" + agentHost + ":9091", 10);
        utility.connectToURL("http://" + agentHost + ":8080", 10);


        for (i = 0; i < 20; i++) {
            clusterFrontendsMetrics =
                getMetricPaths("Admin", "",
                    "(.*)\\|Custom Metric Process \\(Virtual\\)\\|virtualagent", "Frontends\\|(.*)");

            jbossFrontendsMetrics =
                getMetricPaths("Admin", "", "(.*)\\|(.*)JBoss(.*)\\|(.*)JBoss(.*)",
                    "Frontends\\|(.*)");

            tomcatFrontendsMetrics =
                getMetricPaths("Admin", "", "(.*)\\|(.*)Tomcat(.*)\\|(.*)Tomcat(.*)",
                    "Frontends\\|(.*)");
            // Validate if all the JBoss and Tomcat Frontends metrics are reported under virtual
            // agent
            if (!clusterFrontendsMetrics.isEmpty() && !jbossFrontendsMetrics.isEmpty()
                && !tomcatFrontendsMetrics.isEmpty()) {
                break;
            } else {
                harvestWait(60);
            }
        }

        if (i == 20) Assert.assertTrue(false);
        /**
         * caltemp and now are calendar objects. We are passing the same objects.
         * 
         * startFormatted and endFormatted to format the date for logging purposes.
         */
        Calendar now = Calendar.getInstance();
        Calendar calTemp;
        calTemp = (Calendar) now.clone();
        utility.connectToURL("http://" + agentHost + ":9091", 10);
        utility.connectToURL("http://" + agentHost + ":8080", 10);
        harvestWait(120);

        now.add(Calendar.MINUTE, 2);
        String startFormatted = formatter.format(calTemp.getTime());
        String endFormatted = formatter.format(now.getTime());
        String tomcatmetricValue = "";
        String jbossmetricValue = "";
        String virtualAgentmetricValue = "";

        LOGGER.info("start time\n" + startFormatted);
        LOGGER.info("end time\n" + endFormatted);

        /**
         * Tomcat metric values
         */

        List<String> metricData_Tomcat =
            clwCommon.getMetricValueInTimeRange(users[0], "", tomcatAgentExpression,
                responsesPerIntervalRootContext, host, port, emLibDir, calTemp, now);

        /**
         * JBoss metric values
         */

        List<String> metricData_JBoss =
            clwCommon.getMetricValueInTimeRange(users[0], "", jbossAgentExpression,
                responsesPerIntervalRootContext, host, port, emLibDir, calTemp, now);

        /**
         * Virtual Agent metric values
         */

        List<String> metricData_VirtualAgent =
            clwCommon.getMetricValueInTimeRange(users[0], "", multiAgentVirtualAgentExpression,
                responsesPerIntervalRootContext, host, port, emLibDir, calTemp, now);

        maxOfAgentMetrics =
            (metricData_Tomcat.size() > metricData_JBoss.size())
                ? metricData_Tomcat.size()
                : metricData_JBoss.size();

        if (metricData_VirtualAgent.size() == maxOfAgentMetrics) {
            /**
             * Creating Iterators
             */
            Iterator<String> tomcatIterator = metricData_Tomcat.listIterator(2);
            Iterator<String> jbossIterator = metricData_JBoss.listIterator(2);
            Iterator<String> virtualAgentIterator = metricData_VirtualAgent.listIterator(2);
            /**
             * Loop through the lists
             */

            if (metricData_Tomcat.size() >= 3 && metricData_JBoss.size() >= 3
                && metricData_VirtualAgent.size() >= 3) {
                while (tomcatIterator.hasNext() && jbossIterator.hasNext()
                    && virtualAgentIterator.hasNext()) {
                    tomcatmetricValue = tomcatIterator.next();
                    jbossmetricValue = jbossIterator.next();
                    virtualAgentmetricValue = virtualAgentIterator.next();

                    LOGGER.info("Tomcat Metric is ", tomcatmetricValue);
                    LOGGER.info("JBoss Metric is ", jbossmetricValue);
                    LOGGER.info("Virtual Agent Metric is", virtualAgentmetricValue);

                    /**
                     * skip if they have ?
                     */
                    if ((tomcatmetricValue.equalsIgnoreCase("?"))
                        && (jbossmetricValue.equalsIgnoreCase("?"))
                        && (virtualAgentmetricValue.equals("?")))
                        LOGGER.info("This has to be left Alone - ?");
                    /**
                     * Check for the aggregated value
                     */
                    else {
                        tomcatmetricValue = tomcatmetricValue.split(",")[13];
                        jbossmetricValue = jbossmetricValue.split(",")[13];
                        virtualAgentmetricValue = virtualAgentmetricValue.split(",")[13];

                        LOGGER
                            .info("TOMCAT+JBOSS = "
                                + (Long.parseLong(tomcatmetricValue) + Long
                                    .parseLong(jbossmetricValue)));
                        LOGGER.info("VIRTUAL = " + Long.parseLong(virtualAgentmetricValue));

                        if ((Long.parseLong(tomcatmetricValue) + Long.parseLong(jbossmetricValue)) == Long
                            .parseLong(virtualAgentmetricValue)) {
                            Assert.assertTrue(true);
                        } else {
                            LOGGER.info("Assertion Failed for aggregation");
                            Assert.assertTrue(false);
                        }
                    }
                }
            } else {
                LOGGER.info("Metrics are not present");
                Assert.assertTrue(false);
            }
        } else {
            LOGGER.info("Metrics are not properly commuted");
            Assert.assertTrue(false);
        }
    }

    /*******************************************************************************
     * JAMSA07
     * These test cases are :
     * MOM EM
     * TOMCAT AGENT Single Agent
     * Single Metric
     ******************************************************************************/

    /**
     * MOM EM Tomcat Agent
     * User with full permissions
     */
    @Test(groups = {"virtualagents", "smoke"})
    private void verify_ALM_295821_MOMTomcat_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Tomcat Agents_user with full access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[1], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM Tomcat Agent
     * User with historical agent permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295805_MOMTomcat_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Tomcat Agents_user with historical agent access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[2], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM Tomcat Agent
     * User with live agent permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295813_MOMTomcat_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Tomcat Agents_user with live agent access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[3], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM Tomcat Agent
     * User with read permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295781_MOMTomcat_readuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Tomcat Agents_user with read access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[4], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM Tomcat Agent
     * User with run tracer permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295797_MOMTomcat_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Tomcat Agents_user with run trace access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[5], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM Tomcat Agent
     * User with write permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295789_MOMTomcat_writeuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Tomcat Agents_user with write access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[6], password, tomcatVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /*******************************************************************************
     * JAMSA07
     * These test cases are :
     * MOM EM
     * JBoss AGENT (Single Agent)
     * Single Metric
     ******************************************************************************/

    /**
     * MOM EM JBoss Agent
     * User with full permissions
     */
    @Test(groups = {"virtualagents", "smoke"})
    private void verify_ALM_295820_MOMJBoss_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_JBOSS Agents_user with full access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[1], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM JBoss Agent
     * User with historicalagent permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295804_MOMJBoss_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_JBOSS Agents_user with historical agent access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[2], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM JBoss Agent
     * User with live agent permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295812_MOMJBoss_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_JBOSS Agents_user with live agent access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[3], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM JBoss Agent
     * User with read permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295780_MOMJBoss_readuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_JBOSS Agents_user with read access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[4], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM JBoss Agent
     * User with run tracer permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295796_MOMJBoss_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_JBOSS Agents_user with run trace access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[5], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }

    /**
     * MOM EM JBoss Agent
     * User with write permissions
     */
    @Test(groups = {"virtualagents", "deep"})
    private void verify_ALM_295788_MOMJBoss_writeuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_JBOSS Agents_user with write access  permissions_Targeted to Custom Domain");
        metric =
            getMetricValue(users[6], password, jbossVirtualAgentExpression,
                CPUProcessUtilizationAggregate);
        LOGGER.info("Test done" + metric);

        assertTest.assertMetricValue(metric);

    }


    /***********************************************************************************
     * GAMSA03:
     * These test cases are :
     * MOM EM
     * JBoss AGENT (Multiple Agents)
     * Multiple Metrics
     **********************************************************************************/

    @Test(groups = {"virtualagents", "smoke"})
    public void verify_ALM_295823_MoM_MultipleAgents_MultipleMetrics_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_ multiple metrics_user with  full   permissions_Targeted to Custom Domain");
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

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295807_MoM_MultipleAgents_MultipleMetrics_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_ multiple metrics_user with historical_agent_control  permissions_Targeted to Custom Domain");
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

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295815_MoM_MultipleAgents_MultipleMetrics_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_ multiple metrics_user with live_agent_control  permissions_Targeted to Custom Domain");
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

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295783_MoM_MultipleAgents_MultipleMetrics_readaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_ multiple metrics_user with read permissions_Targeted to Custom Domain");
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

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295799_MoM_MultipleAgents_MultipleMetrics_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_ multiple metrics_user with run_tracer  permissions_Targeted to Custom Domain");
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

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295791_MoM_MultipleAgents_MultipleMetrics_writeaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_ multiple metrics_user with write  permissions_Targeted to Custom Domain");
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

    @Test(groups = {"virtualagents", "smoke"})
    public void verify_ALM_295822_MoM_MultipleAgents_fullaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_user with full permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[1], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295806_MoM_MultipleAgents_historicalagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_user with historical_agent_control permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[2], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295814_MoM_MultipleAgents_liveagentuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_user with live_agent_control permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[3], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295782_MoM_MultipleAgents_readaccessuser() {
        LOGGER
            .info("Test Name : AgentClustersAgentClusters(MOM)_Multiple Agents_user with read permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[4], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295798_MoM_MultipleAgents_runtraceruser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_user with run_tracer  permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[5], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "deep"})
    public void verify_ALM_295790_MoM_MultipleAgents_writeaccessuser() {
        LOGGER
            .info("Test Name : AgentClusters(MOM)_Multiple Agents_user with write permissions_Targeted to Custom Domain");
        // Querying common metric of agents
        String metric1 =
            getMetricValue(users[6], password, cpuMetricVirtualAgentExpression,
                CPUProcessUtilizationAggregate);

        LOGGER.info("Queried metric is " + metric1);
        assertTest.assertMetricValue(metric1);
    }

    @Test(groups = {"virtualagents", "smoke"})
    public void Verify_ALM_295826_VirtualAgent_Number_of_Metrics() {

        boolean cpuMetricsReported = false;
        boolean frontendsMetricsReported = false;
        boolean threadsMetricsReported = false; 
        
        //Hitting Frontends of Jboss and Tomcat
        utility.connectToURL("http://" + agentHost + ":9091", 10);
        utility.connectToURL("http://" + agentHost + ":8080", 10);
        harvestWait(90);


        // Get the cpu metrics for each of the agent under test
        Set<String> clusterCPUMetrics =
            getMetricPaths(users[0], "",
                multiAgentVirtualAgentExpression, cpuMetricExpression);

        Set<String> jbossCPUMetrics =
            getMetricPaths(users[0], "", jbossAgentExpression, cpuMetricExpression);

        Set<String> tomcatCPUMetrics =
            getMetricPaths(users[0], "", tomcatAgentExpression, cpuMetricExpression);

        // Validate if all the JBoss and Tomcat CPU metrics are reported under virtual agent
        if (!clusterCPUMetrics.isEmpty() && !jbossCPUMetrics.isEmpty()
            && !tomcatCPUMetrics.isEmpty()) {
            if (clusterCPUMetrics.containsAll(jbossCPUMetrics)
                && clusterCPUMetrics.containsAll(tomcatCPUMetrics)) {
                LOGGER
                    .info("All the CPU metrics of Tomcat and JBoss are reported in Virtual Agent");
                cpuMetricsReported = true;
            } else {
                LOGGER
                    .info("Not all the CPU metrics of Tomcat and JBoss are reported in Virtual Agent");
            }
        } else {
            LOGGER.info("Not all the Agents under test reported CPU metrics.");
        }


        // Get the Frontends metrics for each of the agent under test
        Set<String> clusterFrontendsMetrics =
            getMetricPaths(users[0], "",
                multiAgentVirtualAgentExpression, frontendsMetricExpression);

        Set<String> jbossFrontendsMetrics =
            getMetricPaths(users[0], "", jbossAgentExpression, frontendsMetricExpression);

        Set<String> tomcatFrontendsMetrics =
            getMetricPaths(users[0], "", tomcatAgentExpression,
                frontendsMetricExpression);

        // Validate if all the JBoss and Tomcat Frontends metrics are reported under virtual agent
        if (!clusterFrontendsMetrics.isEmpty() && !jbossFrontendsMetrics.isEmpty()
            && !tomcatFrontendsMetrics.isEmpty()) {
            if (clusterFrontendsMetrics.containsAll(jbossFrontendsMetrics)
                && clusterFrontendsMetrics.containsAll(tomcatFrontendsMetrics)) {
                LOGGER
                    .info("All the Frontends metrics of Tomcat and JBoss are reported in Virtual Agent");
                frontendsMetricsReported = true;
            } else {
                LOGGER
                    .info("Not all the Frontends metrics of Tomcat and JBoss are reported in Virtual Agent");
            }
        } else {
            LOGGER.info("Not all the Agents under test reported Frontends metrics.");
        }

        // Get the Threads metrics for each of the agent under test
        Set<String> clusterThreadsMetrics =
            getMetricPaths(users[0], "",
                multiAgentVirtualAgentExpression, threadsMetricExpression);

        Set<String> jbossThreadsMetrics =
            getMetricPaths(users[0], "", jbossAgentExpression, threadsMetricExpression);

        Set<String> tomcatThreadsMetrics =
            getMetricPaths(users[0], "", tomcatAgentExpression, threadsMetricExpression);

        // Validate if all the JBoss and Tomcat Threads metrics are reported under virtual agent
        if (!clusterThreadsMetrics.isEmpty() && !jbossThreadsMetrics.isEmpty()
            && !tomcatThreadsMetrics.isEmpty()) {
            if (clusterThreadsMetrics.containsAll(jbossThreadsMetrics)
                && clusterThreadsMetrics.containsAll(tomcatThreadsMetrics)) {
                LOGGER
                    .info("All the Threads metrics of Tomcat and JBoss are reported in Virtual Agent");
                threadsMetricsReported = true;
            } else {
                LOGGER
                    .info("Not all the Threads metrics of Tomcat and JBoss are reported in Virtual Agent");
            }
        } else {
            LOGGER.info("Not all the Agents under test reported Threads metrics");
        }


        Assert.assertTrue(cpuMetricsReported && frontendsMetricsReported && threadsMetricsReported);

    }

    /**
     * Gets the Set of unique metric paths for the specified agent and metric expressions
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @return
     */
    private Set<String> getMetricPaths(String user, String password, String agentExpression,
        String metricExpression) {
        return clwCommon.getuniqueMetricPaths(user, password, agentExpression, metricExpression,
            host, port, emLibDir);
    }

    /**
     * /**
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


    private void syncTimeOnMachines(Collection<String> machineIds) {
        for (String machineId : machineIds) {
            runFlowByMachineId(machineId, TimeSyncFlow.class,
                new TimeSyncFlowContext.Builder().build());
        }
    }

    private void startAll() {

        syncTimeOnMachines(Arrays.asList(VirtualAgentsWindowsClusterTestbed.MOM_MACHINE_ID,
            VirtualAgentsWindowsClusterTestbed.COLLECTOR1_MACHINE_ID,
            VirtualAgentsWindowsClusterTestbed.COLLECTOR2_MACHINE_ID,
            VirtualAgentsWindowsClusterTestbed.AGENT_MACHINE_ID));

        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_START);
        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.JBOSS_ROLE_ID,
            JbossRole.ENV_JBOSS_START);

        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.MOM_ROLE_ID,
            EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.COLLECTOR1_ROLE_ID,
            EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.COLLECTOR2_ROLE_ID,
            EmRole.ENV_START_EM);

        waitForAgentNodes();
    }

    private void stopAll() {

        EmUtils emUtils = utilities.createEmUtils();
        ClwUtils clwUtilsMOM =
            utilities.createClwUtils(VirtualAgentsWindowsClusterTestbed.MOM_ROLE_ID);
        ClwUtils clwUtilsCollector1 =
            utilities.createClwUtils(VirtualAgentsWindowsClusterTestbed.COLLECTOR1_ROLE_ID);
        ClwUtils clwUtilsCollector2 =
            utilities.createClwUtils(VirtualAgentsWindowsClusterTestbed.COLLECTOR2_ROLE_ID);
        // Stop Collector2
        try {
            emUtils.stopRemoteEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
                clwUtilsCollector2.getClwRunner(), 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown Collector 2");
        }
        // Stop Collector1
        try {
            emUtils.stopRemoteEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
                clwUtilsCollector1.getClwRunner(), 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown Collector 1");
        }

        // Stop MOM
        try {
            emUtils.stopLocalEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
                VirtualAgentsWindowsClusterTestbed.MOM_ROLE_ID, 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown MOM");
        }

        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_STOP);
        runSerializedCommandFlowFromRole(VirtualAgentsWindowsClusterTestbed.JBOSS_ROLE_ID,
            JbossRole.ENV_JBOSS_STOP);

    }

    private void waitForAgentNodes() {
        final String tomcatNodeString = agentHost + "|Tomcat|Tomcat Agent";
        final String jbossNodeString = agentHost + "|JBoss|JBoss Agent";
        String value;
        int i = 0;
        List<String> nodeList;
        int count = 0;
        for (i = 0; i < 20; i++) {
            nodeList = clwCommon.getNodeList(users[0], "", ".*", host, port, emLibDir);

            Iterator<String> nodeListIterator = nodeList.iterator();
            while (nodeListIterator.hasNext()) {
                value = nodeListIterator.next();
                if (value.equalsIgnoreCase(tomcatNodeString))
                    count++;
                else if (value.equalsIgnoreCase(jbossNodeString)) count++;
            }
            if (count == 2)
                break;
            else {
                count = 0;
                harvestWait(60);
            }
        }
        if (i == 20) Assert.assertTrue(false);
    }

    private void harvestWait(int seconds) {
        try {
            LOGGER.info("Harvesting crops.");
            Thread.sleep(seconds * 1000);
            LOGGER.info("Crops harvested.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        stopAll();
    }


}
