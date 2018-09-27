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
 */


package com.ca.apm.tests.test;

import static org.testng.Assert.assertEquals;

import com.ca.apm.tests.testbed.OneMomTwoCollectorsAbstractTestbed;
import com.ca.apm.tests.testbed.OneMomTwoCollectorsLinuxTestbed;
import com.ca.apm.tests.utils.FlowUtils;
import com.ca.apm.tests.utils.agents.AgentLocalUtils;
import com.ca.apm.tests.utils.clw.ClwUtils;
import com.ca.apm.tests.utils.configutils.PropertiesUtility;
import com.ca.apm.tests.utils.configutils.TimeUtility;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.apm.tests.utils.osutils.OsLocalUtils;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class HttpsTests351569 {

    private EnvironmentPropertyContext envProps;
    private String emInstallDir;
    private String agentLogFilePath;
    private EmConfiguration emConfig;
    private String c2Hostname;
    private String c1Hostname;
    private String momHostname;
    private String tomcat1InstallDir;
    private String agentPropertyFilePath;
    private String momIp;
    private final ClwUtils cu = new ClwUtils();


    private static final Logger log = LoggerFactory.getLogger(CcdvTests.class);

    @BeforeTest
    public void setUp() throws Exception {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();

        emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyEmInstallDir);

        tomcat1InstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);

        AgentLocalUtils agentLocalUtils = new AgentLocalUtils(tomcat1InstallDir);

        agentPropertyFilePath = agentLocalUtils.getProfilePath();

        agentLogFilePath = agentLocalUtils.getLogPath();

        momHostname =
            envProps.getMachinePropertyById(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        c1Hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        c2Hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        emConfig = new EmConfiguration(emInstallDir, 5001);
        cu.setClWorkstationJarFileLocation(emConfig.getClwPath());

        momIp = OsLocalUtils.getCurrentIp().getHostAddress();

        EmBatLocalUtils.setUpHttpsProperties(emConfig.getPropertiesPath());
        switchToHTTPS(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
        switchToHTTPS(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);

        // change rebalance interval to 2 minutes for faster tests
        PropertiesUtility.updateProperty(emConfig.getPropertiesPath(),
            "introscope.enterprisemanager.loadbalancing.interval", "120");

    }



    /**
     * Tests Agent's secured connection to MOM via HTTPS with use of IP instead of hostname.
     * Agent starts before collectors.
     * 
     * EM BAT tests # 351569 scenario #1 <br>
     * Author : Martin Batelka
     *
     * <h5>Pre-requisites:</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * <li>MOM, collectors and agents are configured for HTTPS communication</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Start MOM and wait until it starts</li>
     * <li>Configure agent with mom IP address and start agent. Verify if it is able to reach MOM</li>
     * <li>Now start 2 collectors and wait until they start successfully</li>
     * <li>Check-1: Verify if collectors are connected to MOM.</li>
     * <li>Check-2: Verify MOM has redirected agent to the any of the connected collectors.</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agent must connect to MOM and it must redirect to collector
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Collectors connection logic is broken or MOM redirection doesn't work.</li>
     * </ul>
     * </p>
     * 
     * @author batma08
     *
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"MOM", "HTTPS", "BAT"})
    public void startAgentBeforeCollectorsStarUpScen1() throws Exception {
        startMom();
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
        verifyAgentIsConnected(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        startCollector1();
        startCollector2();
        // Check-1
        verifyCollectorsAreConnectedToMom(c1Hostname, c2Hostname, momHostname);
        Thread.sleep(140 * 1000);
        // Check-2
        verifyAgentWasRedirectedToAnyCollector(c1Hostname, c2Hostname);

    }

    /**
     * Tests Agent's secured connection to MOM via HTTPS with use of IP instead of hostname.
     * Agent starts before cluster.
     * 
     * EM BAT tests # 351569 scenario #2 <br>
     * Author : Martin Batelka
     *
     * <h5>Pre-requisites:</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * <li>MOM, collectors and agents are configured for HTTPS communication</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Configure agent with mom ip address and start agent.</li>
     * <li>Start MOM and wait until it starts</li>
     * <li>Check-1: Verify agent is able to reach MOM address.</li>
     * <li>Now start 2 collectors and wait until they start successfully</li>
     * <li>Check-2: Verify if collectors are connected to MOM.</li>
     * <li>Check-3: Verify MOM has redirected agent to the any of the connected collectors.</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agent must connect to MOM and it must redirect to collector
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Collector's connection logic is broken or MOM redirection doesn't work.</li>
     * </ul>
     * </p>
     * 
     * @author batma08
     *
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"MOM", "HTTPS", "BAT"})
    public void startAgentBeforeClusterStarUpScen2() throws Exception {
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);

        startMom();

        // Check-1
        verifyAgentIsConnected(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);

        startCollector1();
        startCollector2();

        // Check-2
        verifyCollectorsAreConnectedToMom(c1Hostname, c2Hostname, momHostname);

        Thread.sleep(140 * 1000);
        // Check-3
        verifyAgentWasRedirectedToAnyCollector(c1Hostname, c2Hostname);
    }

    /**
     * Tests Agent's secured connection to MOM via HTTPS with use of IP instead of hostname.
     * Whole cluster is started and set up before agent.
     * 
     * EM BAT tests # 351569 scenario #3 <br>
     * Author : Martin Batelka
     *
     * <h5>Pre-requisites:</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * <li>MOM, collectors and agents are configured for HTTPS communication</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Start MOM and wait until it starts</li>
     * <li>Now start 2 collectors and wait until they start successfully</li>
     * <li>Check-1: Verify if collectors are connected to MOM.</li>
     * <li>Configure agent with mom ip address and start agent. Verify if it is able to reach MOM</li>
     * <li>Check-2: Verify MOM has redirected agent to the any of the connected collectors.</li>
     * </ol>
     * </p>
     *
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agent must connect to MOM and it must redirect to collector
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Collector's connection logic is broken or MOM redirection doesn't work.</li>
     * </ul>
     * </p>
     * 
     * @author batma08
     *
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"MOM", "HTTPS", "BAT"})
    public void startAgentAfterClusterSetUpScen3() throws Exception {

        // Start MOM and all collectors
        {
            startMom();
            startCollector1();
            startCollector2();
        }

        // Check-1
        verifyCollectorsAreConnectedToMom(c1Hostname, c2Hostname, momHostname);

        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);

        // Verify if it is able to reach MOM
        verifyAgentIsConnected(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        Thread.sleep(120 * 1000);

        // Check-2
        verifyAgentWasRedirectedToAnyCollector(c1Hostname, c2Hostname);
    }


    /**
     * Tests Agent's secured connection to MOM via HTTPS with use of IP instead of hostname.
     * Agent starts before cluster. After agent, collectors start and at the end MOM starts.
     * 
     * EM BAT tests # 351569 scenario #4 <br>
     * Author : Martin Batelka
     *
     * <h5>Pre-requisites:</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * <li>MOM, collectors and agents are configured for HTTPS communication</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Configure agent with mom ip address and start agent.</li>
     * <li>Now start 2 collectors and wait until they start successfully</li>
     * <li>Start MOM and wait until it starts</li>
     * <li>Check-1: Verify if collectors are connected to MOM</li>
     * <li>Check-2: Verify MOM has redirected agent to the any of the connected collectors.</li>
     * 
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agent must connect to MOM and it must redirect to collector
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Collector's connection logic is broken or MOM redirection doesn't work.</li>
     * </ul>
     * </p>
     * 
     * @author batma08
     *
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"MOM", "HTTPS", "BAT"})
    public void startAgentBeforeClusterStarUpScen4() throws Exception {
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);

        startCollector1();
        startCollector2();

        startMom();

        // Check-1
        verifyAgentIsConnected(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);

        // Check-2
        verifyCollectorsAreConnectedToMom(c1Hostname, c2Hostname, momHostname);

        Thread.sleep(140 * 1000);
        // Check-3
        verifyAgentWasRedirectedToAnyCollector(c1Hostname, c2Hostname);
    }

    /**
     * Tests Agent's secured connection to MOM via HTTPS with use of IP instead of hostname.
     * Collectors start before agent. After collectors, agent starts and at the end MOM starts.
     * 
     * EM BAT tests # 351569 scenario #5 <br>
     * Author : Martin Batelka
     *
     * <h5>Pre-requisites:</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * <li>MOM, collectors and agents are configured for HTTPS communication</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * 
     * <li>Start 2 collectors and wait until they start successfully</li>
     * <li>Configure agent with mom ip address and start agent.</li>
     * <li>Start MOM and wait until it starts</li>
     * <li>Check-1: Verify if collectors are connected to MOM</li>
     * <li>Check-2: Verify MOM has redirected agent to the any of the connected collectors.</li>
     * 
     * 
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agent must connect to MOM and it must redirect to collector
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Collector's connection logic is broken or MOM redirection doesn't work.</li>
     * </ul>
     * </p>
     * 
     * @author batma08
     *
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"MOM", "HTTPS", "BAT"})
    public void startCollectorsBeforeAgentScen5() throws Exception {

        startCollector1();
        startCollector2();

        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);


        startMom();

        // Check-1
        verifyCollectorsAreConnectedToMom(c1Hostname, c2Hostname, momHostname);

        // Check-2

        verifyAgentIsConnected(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momIp);
        Thread.sleep(140 * 1000);
        // Check-3
        verifyAgentWasRedirectedToAnyCollector(c1Hostname, c2Hostname);
    }

    private void verifyAgentWasRedirectedToAnyCollector(String c1Hostname, String c2Hostname)
        throws Exception {
        final int c1Agents = cu.getAgents(c1Hostname, "Active");
        final int c2Agents = cu.getAgents(c2Hostname, "Active");
        Assert.assertEquals(c1Agents + c2Agents, 1,
            "The agent should have been redirected to C1 or C2 by now");
    }

    private void verifyCollectorsAreConnectedToMom(String c1Hostname, String c2Hostname,
        String momHostname) throws Exception {
        Thread.sleep(60 * 1000);
        String[] collectors = {c1Hostname + "@5001", c2Hostname + "@5001"};
        // Check-1
        cu.checkClusterConfiguration(momHostname, collectors);;
    }

    private void verifyAgentIsConnected(String agentMachineId, String host) throws Exception {
        Thread.sleep(140 * 1000);// we should wait to preceding operation make impact
        FlowUtils.isKeywordInLog(envProps, agentMachineId, agentLogFilePath,
            "Connected controllable Agent to the Introscope Enterprise Manager at " + host);
    }

    private void startMom() throws Exception {
        log.info("Starting MOM");
        EmBatLocalUtils.startLocalEm(emConfig);
    }

    private void startCollector1() throws Exception {
        log.info("Starting Collector1");
        FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);
    }

    private void startCollector2() throws Exception {
        log.info("Starting Collector1");
        FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);
    }

    private void pointAgentToHost(String collectorMachineId, String hostname) throws Exception {
        FlowUtils.insertProperty(envProps, collectorMachineId, agentPropertyFilePath,
            "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", hostname);
    }

    private void switchToHTTPS(String machineId) throws Exception {
        // switch COLLECTOR 1 to secure connection
        FlowUtils.setUpEMHttpsProperties(envProps, machineId, emConfig.getPropertiesPath());
        // switch AGENT on COLLECTOR 1 to secure connection
        FlowUtils.setUpAgentHttpsProperties(envProps, machineId, agentPropertyFilePath);
    }

    private void startAgent(String machineId) throws Exception {
        FlowUtils.startTomcat(envProps, machineId, tomcat1InstallDir);
    }



    /**
     * Verifies that Agents can connect to cluster when MOM's fully qualified domain name is
     * specified.
     * ALM test #351569 - Scenario 6
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Configure HTTPS for MOM + C1 + C2</li>
     * <li>Configure HTTPS connection for agents</li>
     * <li>connect agent to mom with the mom name given with full domain details i.e xyz.ca.com</li>
     * <li>Start MOM, C1 and C2</li>
     * <li>Start A1 and A2</li>
     * <li>Agents must connect to MOM and it must redirect to collector</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * The agents get connected to collectors in the cluster when domain name is given in the mom
     * hostname.
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Agent Connectivity - HTTPS</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"HTTPS", "BAT"})
    public void testAgentConnectsToMomWithDomainName() throws Exception {
        // synchronize time
        {
            TimeUtility.synchronizeTimeWithCAServer();
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);
        }

        // connect agent to mom with the mom name given with full domain details i.e xyz.ca.com
        final String momfqdn = EmBatLocalUtils.hostnameToFqdn(momHostname);
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momfqdn);
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, momfqdn);

        // Bring up mom and collectors
        {
            startMom();
            startCollector1();
            startCollector2();

            // Start agents
            startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);

            Thread.sleep(250000);
        }

        // Agent must connect to MOM and it must redirect to collector
        FlowUtils.isKeywordInLog(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, agentLogFilePath,
            "Connected controllable Agent to the Introscope Enterprise Manager at " + momfqdn
                + ":8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory.");
        FlowUtils.isKeywordInLog(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, agentLogFilePath,
            "Connected controllable Agent to the Introscope Enterprise Manager at " + momfqdn
                + ":8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory.");

        // observe if the agent gets connected to collectors in the cluster when domain name is
        // given in the mom hostname.
        {
            log.info("Checking Agent connections - they should be redirected to Collectors by now");
            final int c1Agents = cu.getAgents(c1Hostname, "Active");
            final int c2Agents = cu.getAgents(c2Hostname, "Active");
            Assert.assertEquals(c1Agents + c2Agents, 2,
                "Both agents should have been redirected to C1 or C2 by now");

            assertEquals("8444", cu.getMetricFromAgent(".*" + c1Hostname + ".*", "EM Port"));
            assertEquals("8444", cu.getMetricFromAgent(".*" + c2Hostname + ".*", "EM Port"));
        }
    }

    /**
     * Verifies that Agents can connect to cluster when MOM's name is given in agent profile with
     * both lower case and upper case
     * ALM test #351569 - Scenario 7
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Configure HTTPS for MOM + C1 + C2</li>
     * <li>Configure HTTPS connection for agents</li>
     * <li>connect agent to mom with the mom name given in agent profile with both lower case and
     * upper cases. i.e for example xyzmachine is the hostname give it in profile file as XyZmAcHiNe
     * </li>
     * <li>Start MOM, C1 and C2</li>
     * <li>Start A1 and A2</li>
     * <li>Agents must connect to MOM and it must redirect to collector</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * The agents get connected to collectors in the cluster when MOM hostname is given in mixed
     * case
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Agent Connectivity - HTTPS</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"HTTPS", "BAT"})
    public void testAgentConnectsToMomWithCaseInsensitiveName() throws Exception {
        // synchronize time
        {
            TimeUtility.synchronizeTimeWithCAServer();
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);
        }

        // connect agent to mom with the mom name given in agent profile with both lower case and
        // upper cases. i.e for example xyzmachine is the hostname give it in profile file as
        // XyZmAcHiNe.
        final String momWrongCase = mixStringCase(momHostname);
        Assert.assertNotEquals(momWrongCase, momHostname);
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, momWrongCase);
        pointAgentToHost(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, momWrongCase);

        // Bring up mom and collectors
        {
            startMom();
            startCollector1();
            startCollector2();

            // Start agents
            startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            startAgent(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);

            Thread.sleep(250000);
        }

        // Agent must connect to MOM and it must redirect to collector
        FlowUtils.isKeywordInLog(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, agentLogFilePath,
            "Connected controllable Agent to the Introscope Enterprise Manager at " + momWrongCase
                + ":8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory.");
        FlowUtils.isKeywordInLog(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, agentLogFilePath,
            "Connected controllable Agent to the Introscope Enterprise Manager at " + momWrongCase
                + ":8444,com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory.");

        // observe if the agent gets connected to collectors in the cluster when mom
        // hostname is case sensitive.
        {
            log.info("Checking Agent connections - they should be redirected to Collectors by now");
            final int c1Agents = cu.getAgents(c1Hostname, "Active");
            final int c2Agents = cu.getAgents(c2Hostname, "Active");
            Assert.assertEquals(c1Agents + c2Agents, 2,
                "Both agents should have been redirected to C1 or C2 by now");

            assertEquals("8444", cu.getMetricFromAgent(".*" + c1Hostname + ".*", "EM Port"));
            assertEquals("8444", cu.getMetricFromAgent(".*" + c2Hostname + ".*", "EM Port"));
        }
    }

    private String mixStringCase(String src) {
        final StringBuilder ret = new StringBuilder();

        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);

            if (i % 2 == 1) {
                c = Character.toUpperCase(c);
            } else {
                c = Character.toLowerCase(c);
            }
            ret.append(c);
        }
        return ret.toString();
    }
}
